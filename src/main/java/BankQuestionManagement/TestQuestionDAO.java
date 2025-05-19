package BankQuestionManagement;

import BankQuestionManagement.*;

import java.util.List;

/**
 * Test đơn giản cho QuestionDAO:
 * - Thêm mới question
 * - Lấy question theo ID
 * - Cập nhật question
 * - Lấy tất cả question
 * - Xóa question
 */
public class TestQuestionDAO {
    public static void main(String[] args) {
        QuestionDAO dao = new QuestionDAO();

        // 1. Giả sử đã có 1 Exam (ExamID = 1) trong bảng Exams, nếu chưa, bạn có thể chạy TestExamDAO trước
        int exampleExamID = 1;

        // 2. Thêm mới Question
        Question q1 = new Question(exampleExamID,
                "これはテスト問題です。What is the capital of Vietnam?",
                "C:/images/q1.png",
                "C:/audio/q1.mp3");
        int newQuestionId = dao.addQuestion(q1);
        System.out.println("Tạo question mới, ID = " + newQuestionId);

        // 3. Lấy Question vừa tạo
        Question fetched = dao.getQuestionByID(newQuestionId);
        System.out.println("Question fetched: " + fetched);

        // 4. Cập nhật Question
        fetched.setContent("Cập nhật nội dung: ベトナムの首都は何ですか？");
        fetched.setImagePath("C:/images/q1_updated.png");
        boolean updated = dao.updateQuestion(fetched);
        System.out.println("Cập nhật thành công? " + updated);

        // 5. Lấy lại để kiểm tra
        Question check2 = dao.getQuestionByID(newQuestionId);
        System.out.println("Sau khi update: " + check2);

        // 6. Lấy tất cả Question (in ra console)
        List<Question> all = dao.getAllQuestions();
        System.out.println("Danh sách tất cả questions:");
        for (Question q : all) {
            System.out.println("  • " + q);
        }

        // 7. Xóa question
//        boolean deleted = dao.deleteQuestion(newQuestionId);
//        System.out.println("Xóa question có ID = " + newQuestionId + " thành công? " + deleted);
//
//        // 8. Kiểm tra xem còn tồn tại không
//        Question afterDel = dao.getQuestionByID(newQuestionId);
//        System.out.println("Sau khi xóa, getQuestionByID trả về: " + afterDel);
    }
}
