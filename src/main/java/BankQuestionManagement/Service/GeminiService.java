package BankQuestionManagement.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType; // Giữ lại nhưng không dùng trong mã mới
import org.apache.http.entity.mime.MultipartEntityBuilder; // Không còn dùng
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64; // Thêm import để mã hóa base64

/**
 * Dịch vụ để tương tác với Google AI Gemini API cho OCR và gợi ý QA.
 */
public class GeminiService {
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String OCR_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String QA_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Thực hiện OCR trên tệp hình ảnh đã cho và trả về văn bản được trích xuất.
     * @param imageFile hình ảnh cần quét
     * @return văn bản được trích xuất
     */
    public String scanImage(File imageFile) throws Exception {
        // Sửa: Thêm query parameter ?key= thay vì header Authorization
        HttpPost post = new HttpPost(OCR_ENDPOINT + "?key=" + API_KEY);
        // Sửa: Chỉ cần header Content-Type, không cần Authorization
        post.addHeader("Content-Type", "application/json");

        // Sửa: Loại bỏ MultipartEntityBuilder, thay bằng JSON payload
        // Đọc và mã hóa hình ảnh thành base64
        String base64Image = encodeImageToBase64(imageFile);

        // Tạo payload JSON cho OCR theo định dạng Gemini API
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
            // Sửa: Xử lý phản hồi JSON theo cấu trúc Gemini API
            JsonNode root = mapper.readTree(resp.getEntity().getContent());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        }
    }

    /**
     * Gửi một câu hỏi tới Gemini QA và trả về câu trả lời được gợi ý.
     * @param prompt văn bản câu hỏi
     * @return câu trả lời được gợi ý
     */
    public String suggestAnswer(String prompt) throws Exception {
        // Sửa: Thêm query parameter ?key= thay vì header Authorization
        HttpPost post = new HttpPost(QA_ENDPOINT + "?key=" + API_KEY);
        post.addHeader("Content-Type", "application/json");

        // Sửa: Tạo payload JSON theo định dạng Gemini API, loại bỏ trường model
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
            // Sửa: Xử lý phản hồi JSON theo cấu trúc Gemini API
            JsonNode root = mapper.readTree(resp.getEntity().getContent());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        }
    }

    // Thêm: Phương thức phụ để mã hóa hình ảnh thành base64
    /**
     * Mã hóa tệp hình ảnh thành chuỗi base64.
     * @param imageFile tệp hình ảnh
     * @return chuỗi base64
     */
    private String encodeImageToBase64(File imageFile) throws Exception {
        byte[] fileContent = java.nio.file.Files.readAllBytes(imageFile.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }
}