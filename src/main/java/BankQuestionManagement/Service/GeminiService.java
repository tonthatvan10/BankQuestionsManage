package BankQuestionManagement.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import BankQuestionManagement.DAO.AISuggestionDAO;
import BankQuestionManagement.DAO.AnswerDAO;
import BankQuestionManagement.DAO.ExamDAO;
import BankQuestionManagement.DAO.QuestionDAO;
import BankQuestionManagement.Model.AISuggestion;
import BankQuestionManagement.Model.Answer;
import BankQuestionManagement.Model.Exam;
import BankQuestionManagement.Model.Question;

/**
 * Dịch vụ tương tác với Google AI Gemini API cho:
 *  - OCR (scanImage)
 *  - QA (suggestAnswer)
 *  - text-to-JSON (parseQuestionsToJson)
 *  - Lưu Question, Answer, AISuggestion vào DB
 */
public class GeminiService {
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String OCR_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String QA_ENDPOINT = OCR_ENDPOINT;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final AnswerDAO answerDAO = new AnswerDAO();
    private final AISuggestionDAO suggestionDAO = new AISuggestionDAO();

    // Class hỗ trợ để Jackson map JSON trả về từ Gemini
    private static class ParsedQuestion {
        public int questionNumber;
        public String questionText;
        public List<String> options;
    }

    /**
     * 1) Thực hiện OCR trên tệp hình ảnh và trả về raw text.
     */
    public String scanImage(File imageFile) throws Exception {
        HttpPost post = new HttpPost(OCR_ENDPOINT + "?key=" + API_KEY);
        post.addHeader("Content-Type", "application/json");

        // Mã hoá image thành base64
        String base64Image = encodeImageToBase64(imageFile);
        JsonNode payload = mapper.createObjectNode()
                .set("contents", mapper.createArrayNode()
                        .add(mapper.createObjectNode()
                                .set("parts", mapper.createArrayNode()
                                        .add(mapper.createObjectNode().put("text", "Extract text from this image"))
                                        .add(mapper.createObjectNode()
                                                .set("inline_data", mapper.createObjectNode()
                                                        .put("mime_type", "image/jpeg")
                                                        .put("data", base64Image))))));
        String json = mapper.writeValueAsString(payload);
        post.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse resp = client.execute(post);
            if (resp.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Yêu cầu OCR thất bại: HTTP " + resp.getStatusLine().getStatusCode());
            }
            JsonNode root = mapper.readTree(resp.getEntity().getContent());
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        }
    }

    /**
     * 2) Gửi prompt đến Gemini để QA, nhận về text gợi ý đáp án.
     */
    public String suggestAnswer(String prompt) throws Exception {
        HttpPost post = new HttpPost(QA_ENDPOINT + "?key=" + API_KEY);
        post.addHeader("Content-Type", "application/json");

        // Payload chỉ gồm prompt ở “parts”
        JsonNode payload = mapper.createObjectNode()
                .set("contents", mapper.createArrayNode()
                        .add(mapper.createObjectNode()
                                .set("parts", mapper.createArrayNode()
                                        .add(mapper.createObjectNode().put("text", prompt)))));
        String json = mapper.writeValueAsString(payload);
        post.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse resp = client.execute(post);
            if (resp.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Yêu cầu QA thất bại: HTTP " + resp.getStatusLine().getStatusCode());
            }
            JsonNode root = mapper.readTree(resp.getEntity().getContent());
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        }
    }

    /**
     * 3) Gửi rawText (OCR) đến Gemini, yêu cầu trả về mảng JSON gồm questionNumber, questionText, options[4].
     */
    public String parseQuestionsToJson(String rawText) throws Exception {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là một trợ lý tinh thông tiếng Nhật và cấu trúc đề trắc nghiệm.\n");
        prompt.append("Dưới đây là nội dung đề tải lên (raw OCR):\n\n");
        prompt.append(rawText).append("\n\n");
        prompt.append("Hãy trích xuất tất cả các câu hỏi (選択肢付き) thành định dạng JSON như sau:\n");
        prompt.append("[\n");
        prompt.append("  { \"questionNumber\": 1, \"questionText\": \"...\", \"options\": [\"...\",\"...\",\"...\",\"...\"] },\n");
        prompt.append("  ...\n");
        prompt.append("]\n");
        prompt.append("Mỗi phần tử của mảng JSON chứa:\n");
        prompt.append("  - questionNumber: số thứ tự (1,2,3,...)\n");
        prompt.append("  - questionText: chỉ phần nội dung chính (tiếng Nhật)\n");
        prompt.append("  - options: mảng 4 phương án (chỉ text của phương án)\n");
        prompt.append("Hãy chỉ trả về đúng chuỗi JSON, KHÔNG thêm bình luận hay giải thích.\n");

        return sendGeminiPrompt(prompt.toString());
    }

    /**
     * 4) Hàm chung để gửi một prompt text đến Gemini và nhận lại text response.
     */
    private String sendGeminiPrompt(String prompt) throws Exception {
        HttpPost post = new HttpPost(QA_ENDPOINT + "?key=" + API_KEY);
        post.addHeader("Content-Type", "application/json");

        JsonNode payload = mapper.createObjectNode()
                .set("contents", mapper.createArrayNode()
                        .add(mapper.createObjectNode()
                                .set("parts", mapper.createArrayNode()
                                        .add(mapper.createObjectNode().put("text", prompt)))));
        String json = mapper.writeValueAsString(payload);
        post.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse resp = client.execute(post);
            if (resp.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Yêu cầu Gemini parsing thất bại: HTTP " + resp.getStatusLine().getStatusCode());
            }
            JsonNode root = mapper.readTree(resp.getEntity().getContent());
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        }
    }

    /**
     * 5) Lọc (sanitize) chuỗi rawJson để chỉ giữ đúng phần mảng JSON (loại bỏ backticks, markdown, v.v.).
     */
    private String extractJsonArray(String rawJson) {
        if (rawJson == null) {
            return null;
        }
        String text = rawJson.trim();
        int firstBracket = text.indexOf('[');
        int lastBracket = text.lastIndexOf(']');
        if (firstBracket >= 0 && lastBracket > firstBracket) {
            return text.substring(firstBracket, lastBracket + 1);
        }
        return text;
    }

    /**
     * 6) Tạo mới một Exam, lưu vào DB rồi scan để parse và lưu Question/Answer/AISuggestion.
     */
    public Exam scanAndCreateExam(File imageFile, String examName, String description) throws Exception {
        // 6.1 Lưu Exam mới (ExamName, Description, ImagePath)
        Exam exam = new Exam();
        exam.setExamName(examName);
        exam.setDescription(description);
        exam.setImagePath(imageFile.getAbsolutePath());
        int examId = examDAO.addExam(exam);
        exam.setExamID(examId);

        // 6.2 Scan ảnh, parse và lưu nội dung câu hỏi
        processImageForExam(imageFile, examId);
        return exam;
    }

    /**
     * 7) Dùng khi Exam đã tồn tại (có ExamID, imagePath trong DB).
     *    Lấy imagePath từ DB rồi scan để parse, lưu Question/Answer/AISuggestion.
     */
    public void scanAndPopulateExistingExam(int examId) throws Exception {
        Exam exam = examDAO.getExamById(examId);
        if (exam == null) {
            throw new RuntimeException("Exam với ID " + examId + " không tồn tại.");
        }
        String imagePath = exam.getImagePath();
        if (imagePath == null || imagePath.isEmpty()) {
            throw new RuntimeException("Exam ID " + examId + " chưa có imagePath.");
        }
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new RuntimeException("Tệp ảnh không tồn tại: " + imagePath);
        }
        processImageForExam(imageFile, examId);
    }

    /**
     * 8) Phương thức chính: OCR → parse JSON → lưu Question + Answer + AISuggestion.
     */
    private void processImageForExam(File imageFile, int examId) throws Exception {
        // 8.1 OCR để lấy rawText
        String rawText = scanImage(imageFile);

        // 8.2 Gọi Gemini để parse thành raw JSON (có thể kèm backticks hoặc markdown)
        String questionsRawJson = parseQuestionsToJson(rawText);

        // 8.3 Lọc (sanitize) chỉ lấy phần mảng JSON
        String questionsJson = extractJsonArray(questionsRawJson);
        if (questionsJson == null || questionsJson.isBlank()) {
            throw new RuntimeException("Không tìm thấy JSON hợp lệ trong response của Gemini:\n" + questionsRawJson);
        }

        // 8.4 Dùng Jackson để map JSON thành List<ParsedQuestion>
        List<ParsedQuestion> parsedList = mapper.readValue(
                questionsJson,
                new TypeReference<List<ParsedQuestion>>() {}
        );

        // 8.5 Lưu từng ParsedQuestion
        for (ParsedQuestion pq : parsedList) {
            try {
                // 8.5.1 Lưu Question
                String content = pq.questionText.trim();
                Question q = new Question(examId, content, /* audioPath= */ null);
                int qId = questionDAO.addQuestion(q);
                q.setQuestionID(qId);
                System.out.println("Đã lưu QuestionID = " + qId + ", text = " + content);

                // 8.5.2 Lưu 4 phương án (isCorrect = false tạm thời)
                for (String opt : pq.options) {
                    String optText = opt.trim();
                    Answer a = new Answer(qId, optText, false);
                    int aId = answerDAO.addAnswer(a);
                    System.out.println("    → Lưu AnswerID = " + aId + ", text = " + optText);
                }

                // 8.5.3 Gọi Gemini để dự đoán đáp án (AI gợi ý)
                String aiAnswer = suggestAnswer(content);
                float confidence = 1.0f; // Giữ mặc định, hoặc parse nếu muốn
                AISuggestion suggestion = new AISuggestion(qId, aiAnswer, confidence);
                int sId = suggestionDAO.addAISuggestion(suggestion);
                System.out.println("    → Lưu AISuggestionID = " + sId + ", gợi ý = " + aiAnswer);

            } catch (Exception e) {
                // Nếu có lỗi với câu hỏi này, log và tiếp tục câu kế tiếp
                System.err.println("Lỗi khi xử lý câu hỏi số " + pq.questionNumber + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 9) Mã hóa File ảnh thành Base64.
     */
    private String encodeImageToBase64(File imageFile) throws Exception {
        byte[] fileContent = java.nio.file.Files.readAllBytes(imageFile.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }
}
