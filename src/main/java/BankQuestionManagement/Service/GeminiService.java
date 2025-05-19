package BankQuestionManagement.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Dịch vụ để tương tác với API Gemini 2.5 cho OCR và gợi ý QA.
 */
public class GeminiService {
    private static final String API_KEY          = System.getenv("GEMINI_API_KEY");
    private static final String OCR_ENDPOINT     = "https://api.gemini.example/v2.5/ocr";
    private static final String QA_ENDPOINT      = "https://api.gemini.example/v2.5/generate";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Thực hiện OCR trên tệp hình ảnh đã cho và trả về văn bản được trích xuất.
     * @param imageFile hình ảnh cần quét
     * @return văn bản được trích xuất
     */
    public String scanImage(File imageFile) throws Exception {
        HttpPost post = new HttpPost(OCR_ENDPOINT);
        post.addHeader("Authorization", "Bearer " + API_KEY);

        var builder = MultipartEntityBuilder.create()
                .addBinaryBody("file", imageFile)
                .addTextBody("model", "gemini-ocr-2.5", ContentType.TEXT_PLAIN);
        post.setEntity(builder.build());

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse resp = client.execute(post);

            if (resp.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Yêu cầu OCR thất bại: HTTP " + resp.getStatusLine().getStatusCode());
            }
            InputStream content = resp.getEntity().getContent();
            JsonNode root = mapper.readTree(content);
            return root.path("text").asText();
        }
    }

    /**
     * Gửi một câu hỏi tới Gemini QA và trả về câu trả lời được gợi ý.
     * @param prompt văn bản câu hỏi
     * @return câu trả lời được gợi ý
     */
    public String suggestAnswer(String prompt) throws Exception {
        HttpPost post = new HttpPost(QA_ENDPOINT);
        post.addHeader("Authorization", "Bearer " + API_KEY);
        post.addHeader("Content-Type", "application/json");

        JsonNode payload = mapper.createObjectNode()
                .put("model", "gemini-qa-2.5")
                .put("prompt", prompt);
        String json = mapper.writeValueAsString(payload);
        post.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse resp = client.execute(post);

            if (resp.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Yêu cầu QA thất bại: HTTP " + resp.getStatusLine().getStatusCode());
            }
            JsonNode root = mapper.readTree(resp.getEntity().getContent());
            return root.path("answer").asText();
        }
    }
}