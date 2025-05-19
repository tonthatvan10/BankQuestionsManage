package BankQuestionManagement.Service;

import java.io.File;

public class GeminiServiceTest {
    public static void main(String[] args) {
        // Khởi tạo GeminiService
        GeminiService geminiService = new GeminiService();

        // ID của Exam đã tồn tại trong database
        int existingExamId = 1;

        try {
            System.out.println("Bắt đầu xử lý Exam với ID = " + existingExamId + "...");

            // Gọi phương thức để quét ảnh và lưu Question, Answer, AISuggestion cho Exam đã tồn tại
            geminiService.scanAndPopulateExistingExam(existingExamId);

            System.out.println("Hoàn thành: Dữ liệu cho Exam ID = " + existingExamId + " đã được lưu vào database.");
        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý Exam ID = " + existingExamId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
