package BankQuestionManagement.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import BankQuestionManagement.DAO.AISuggestionDAO;
import BankQuestionManagement.DAO.AnswerDAO;
import BankQuestionManagement.DAO.ExamDAO;
import BankQuestionManagement.DAO.QuestionDAO;
import BankQuestionManagement.Model.AISuggestion;
import BankQuestionManagement.Model.Answer;
import BankQuestionManagement.Model.Exam;
import BankQuestionManagement.Model.Question;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Dịch vụ tương tác với Google AI Gemini API để thực hiện các chức năng:
 * 1) OCR (scanImage)
 * 2) QA (suggestAnswer)
 * 3) Chuyển đổi text thành JSON cấu trúc câu hỏi (parseQuestionsToJson)
 * 4) Lưu Question, Answer, AISuggestion vào DB
 */
public class GeminiService {
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String OCR_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String QA_ENDPOINT = OCR_ENDPOINT;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final AnswerDAO answerDAO = new AnswerDAO();
    private final AISuggestionDAO suggestionDAO = new AISuggestionDAO();

    /**
     * Model phụ trợ để Jackson ánh xạ JSON trả về của Gemini.
     */
    private static class ParsedQuestion {
        public int questionNumber;
        public String questionText;
        public List<String> options;
        public String suggestedAnswer;
    }

    /**
     * 1) Tạo đề mới:
     * - Lưu metadata đề thi (tên, mô tả, đường dẫn ảnh) vào DB.
     * - Gọi processImageForExam để scan và lưu câu hỏi.
     *
     * @param imageFile   file ảnh chứa đề thi cột hỏi trắc nghiệm
     * @param examName    tên đề thi
     * @param description mô tả đề thi
     * @return đối tượng Exam đã được lưu với ExamID
     */
    public Exam scanAndCreateExam(File imageFile, String examName, String description) throws Exception {
        Exam exam = new Exam();
        exam.setExamName(examName);
        exam.setDescription(description);
        exam.setImagePath(imageFile.getAbsolutePath());
        int examId = examDAO.addExam(exam);
        exam.setExamID(examId);

        // Scan, phân tích và lưu câu hỏi
        processImageForExam(imageFile, examId);
        return exam;
    }

    /**
     * 2) Scan lại đề cho Exam đã tồn tại:
     * - Lấy exam theo ID, kiểm tra tồn tại và đường dẫn ảnh.
     * - Gọi processImageForExam để xử lý.
     *
     * @param examId ID của exam cần scan
     */
    public void scanAndPopulateExistingExam(int examId) throws Exception {
        Exam exam = examDAO.getExamById(examId);
        if (exam == null) {
            throw new RuntimeException("Exam với ID " + examId + " không tồn tại.");
        }
        File imageFile = new File(exam.getImagePath());
        if (!imageFile.exists()) {
            throw new RuntimeException("Tệp ảnh không tồn tại: " + exam.getImagePath());
        }

        // Xử lý OCR và lưu dữ liệu
        processImageForExam(imageFile, examId);
    }

    /**
     * 3) Phương thức chính:
     * - OCR lấy rawText từ ảnh
     * - Gọi Gemini parse thành JSON câu hỏi
     * - Lưu Question, Answers
     * - Gợi ý đáp án, đánh dấu IsCorrect
     * - Lưu AISuggestion
     *
     * @param imageFile file ảnh đề thi
     * @param examId    ID exam
     */
    private void processImageForExam(File imageFile, int examId) throws Exception {
        // 3.1. OCR → rawText
        String rawText = scanImage(imageFile);

        // 3.2. Parse JSON từ rawText
        String questionsRawJson = parseQuestionsToJson(rawText);
        String questionsJson = extractJsonArray(questionsRawJson);
        if (questionsJson == null || questionsJson.isBlank()) {
            throw new RuntimeException(
                    "Không tìm thấy JSON hợp lệ trong response của Gemini:\n" + questionsRawJson);
        }
        List<ParsedQuestion> parsedList = mapper.readValue(
                questionsJson,
                new TypeReference<List<ParsedQuestion>>() {}
        );

        // 3.3. Lưu dữ liệu cho mỗi câu hỏi
        for (ParsedQuestion pq : parsedList) {
            try {
                // 3.3.1. Lưu Question
                Question q = new Question(examId, pq.questionText.trim(), null);
                int qId = questionDAO.addQuestion(q);
                q.setQuestionID(qId);

                // 3.3.2. Lưu Answers (isCorrect mặc định false)
                List<Answer> savedAnswers = new ArrayList<>();
                for (String opt : pq.options) {
                    Answer a = new Answer(qId, opt.trim(), false);
                    int aId = answerDAO.addAnswer(a);
                    a.setAnswerID(aId);
                    savedAnswers.add(a);
                }

                // 3.3.3. Lấy suggestedAnswer trực tiếp từ JSON
                String aiAnswer = pq.suggestedAnswer != null ? pq.suggestedAnswer.trim() : "";
                System.out.println("Gemini gợi ý (từ JSON): " + aiAnswer);

                // 3.3.4. Match đáp án và cập nhật cờ đúng
                Answer matched = savedAnswers.stream()
                        .filter(a -> a.getAnswerText().equalsIgnoreCase(aiAnswer))
                        .findFirst()
                        .orElse(null);
                if (matched != null) {
                    matched.setCorrect(true);
                    answerDAO.updateAnswer(matched);
                    System.out.println("Đánh dấu AnswerID " + matched.getAnswerID() + " là đúng");
                } else {
                    System.out.println("Không tìm thấy đáp án khớp với suggestedAnswer");
                }

                // 3.3.5. Lưu AISuggestion
                AISuggestion suggestion = new AISuggestion(qId, aiAnswer, 1.0f);
                int sId = suggestionDAO.addAISuggestion(suggestion);
                System.out.println("Lưu AISuggestionID: " + sId);

            } catch (Exception e) {
                System.err.println("Lỗi khi xử lý câu hỏi số " + pq.questionNumber + ": "
                        + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 4) Thực hiện OCR: gửi ảnh lên Gemini và trả về chuỗi raw text.
     *
     * @param imageFile file ảnh
     * @return văn bản thu được
     */
    public String scanImage(File imageFile) throws Exception {
        HttpPost post = new HttpPost(OCR_ENDPOINT + "?key=" + API_KEY);
        post.addHeader("Content-Type", "application/json");

        // Mã hoá ảnh base64
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
        post.setEntity(new StringEntity(mapper.writeValueAsString(payload), StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse resp = client.execute(post);
            if (resp.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Yêu cầu OCR thất bại: HTTP "
                        + resp.getStatusLine().getStatusCode());
            }
            JsonNode root = mapper.readTree(resp.getEntity().getContent());
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        }
    }

    /**
     * 5) Gửi prompt để Gemini gợi ý đáp án (QA).
     *
     * @param prompt văn bản câu hỏi
     * @return đáp án gợi ý
     */
    public String suggestAnswer(String prompt) throws Exception {
        HttpPost post = new HttpPost(QA_ENDPOINT + "?key=" + API_KEY);
        post.addHeader("Content-Type", "application/json");

        JsonNode payload = mapper.createObjectNode()
                .set("contents", mapper.createArrayNode()
                        .add(mapper.createObjectNode()
                                .set("parts", mapper.createArrayNode()
                                        .add(mapper.createObjectNode().put("text", prompt)))));
        post.setEntity(new StringEntity(mapper.writeValueAsString(payload), StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse resp = client.execute(post);
            if (resp.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Yêu cầu QA thất bại: HTTP "
                        + resp.getStatusLine().getStatusCode());
            }
            JsonNode root = mapper.readTree(resp.getEntity().getContent());
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        }
    }

    /**
     * 6) Gửi rawOCR đến Gemini để parse thành JSON cấu trúc câu hỏi.
     *
     * @param rawText văn bản OCR
     * @return chuỗi JSON mảng questions
     */
    public String parseQuestionsToJson(String rawText) throws Exception {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là một trợ lý tinh thông tiếng Nhật và cấu trúc đề trắc nghiệm.\n");
        prompt.append("Dưới đây là nội dung đề tải lên (raw OCR):\n\n");
        prompt.append(rawText).append("\n\n");
        prompt.append("Hãy trích xuất tất cả các câu hỏi (選択肢付き) thành định dạng JSON như sau:\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"questionNumber\": 1,\n");
        prompt.append("    \"questionText\": \"...\",\n");
        prompt.append("    \"options\": [\"...\",\"...\",\"...\",\"...\"],\n");
        prompt.append("    \"suggestedAnswer\": \"...\"  // Phải khớp chính xác với một trong các giá trị trong mảng options\n");
        prompt.append("  },\n");
        prompt.append("  ...\n");
        prompt.append("]\n");
        prompt.append("LƯU Ý:\n");
        prompt.append("- Trường \"suggestedAnswer\" phải nằm trong mảng \"options\".\n");
        prompt.append("- Chỉ trả về chính xác chuỗi JSON (không kèm bình luận hay văn bản giải thích).\n");

        return sendGeminiPrompt(prompt.toString());
    }


    /**
     * 7) Gửi prompt chung đến Gemini và nhận text response.
     */
    private String sendGeminiPrompt(String prompt) throws Exception {
        HttpPost post = new HttpPost(QA_ENDPOINT + "?key=" + API_KEY);
        post.addHeader("Content-Type", "application/json");

        JsonNode payload = mapper.createObjectNode()
                .set("contents", mapper.createArrayNode()
                        .add(mapper.createObjectNode()
                                .set("parts", mapper.createArrayNode()
                                        .add(mapper.createObjectNode().put("text", prompt)))));
        post.setEntity(new StringEntity(mapper.writeValueAsString(payload), StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse resp = client.execute(post);
            if (resp.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Yêu cầu Gemini parsing thất bại: HTTP "
                        + resp.getStatusLine().getStatusCode());
            }
            JsonNode root = mapper.readTree(resp.getEntity().getContent());
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        }
    }

    /**
     * 8) Lọc (sanitize) chuỗi raw JSON để chỉ giữ mảng [...].
     */
    private String extractJsonArray(String rawJson) {
        if (rawJson == null) return null;
        String text = rawJson.trim();
        int first = text.indexOf('[');
        int last = text.lastIndexOf(']');
        return (first >= 0 && last > first) ? text.substring(first, last + 1) : text;
    }

    /**
     * 9) Encode ảnh thành Base64.
     */
    private String encodeImageToBase64(File imageFile) throws Exception {
        byte[] content = java.nio.file.Files.readAllBytes(imageFile.toPath());
        return Base64.getEncoder().encodeToString(content);
    }
}
