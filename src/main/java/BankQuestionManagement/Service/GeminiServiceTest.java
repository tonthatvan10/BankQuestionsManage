package BankQuestionManagement.Service;

import java.io.File;

public class GeminiServiceTest {
    public static void main(String[] args) {
        // Khởi tạo GeminiService
        GeminiService geminiService = new GeminiService();

        // Đường dẫn tới tệp hình ảnh kiểm tra
        String imagePath = "C:/Users/Ton That Van/Downloads/Japanese/part2/4.jpg"; // Thay bằng đường dẫn thực tế
        File imageFile = new File(imagePath);

        try {
            // Kiểm tra phương thức scanImage
            System.out.println("Kiểm tra OCR (scanImage)...");
            if (!imageFile.exists()) {
                System.out.println("Lỗi: Tệp hình ảnh không tồn tại tại " + imagePath);
                return;
            }
            String extractedText = geminiService.scanImage(imageFile);
            System.out.println("Văn bản trích xuất từ hình ảnh:");
            System.out.println(extractedText);

            // Kiểm tra phương thức suggestAnswer
            System.out.println("\nKiểm tra QA (suggestAnswer)...");
            String prompt = extractedText.isEmpty() ? "What is AI in a few words?" : extractedText;
            String answer = geminiService.suggestAnswer(prompt);
            System.out.println("Câu hỏi: " + prompt);
            System.out.println("Gợi ý đáp án:");
            System.out.println(answer);

        } catch (Exception e) {
            System.err.println("Lỗi khi kiểm tra GeminiService: " + e.getMessage());
            e.printStackTrace();
        }
    }
}