package BankQuestionManagement.TestDAO;

import BankQuestionManagement.DAO.AnswerDAO;
import BankQuestionManagement.Model.Answer;

import java.util.List;

public class TestAnswerDAO {
    public static void main(String[] args) {
        AnswerDAO dao = new AnswerDAO();

        // Đặt bằng questionID thực tế (đã khởi tạo ở TestQuestionDAO)
        int exampleQuestionID = 4;

        // 1. Thêm mới 4 đáp án
        Answer a1 = new Answer(exampleQuestionID, "A: Ha Noi", true);
        Answer a2 = new Answer(exampleQuestionID, "B: Ho Chi Minh City", false);
        Answer a3 = new Answer(exampleQuestionID, "C: Da Nang", false);
        Answer a4 = new Answer(exampleQuestionID, "D: Hue", true);

        int aid1 = dao.addAnswer(a1);
        int aid2 = dao.addAnswer(a2);
        int aid3 = dao.addAnswer(a3);
        int aid4 = dao.addAnswer(a4);
        System.out.printf("Tạo answers xong: IDs = %d, %d, %d, %d%n", aid1, aid2, aid3, aid4);

        // 2. Lấy một answer theo ID
        Answer fetched = dao.getAnswerByID(aid4);
        System.out.println("Answer fetched: " + fetched);

        // 3. Cập nhật một answer
        fetched.setAnswerText("D: Hue (Cập nhật)");
        fetched.setCorrect(false);
        boolean updated = dao.updateAnswer(fetched);
        System.out.println("Cập nhật answer ID=" + fetched.getAnswerID() + ": " + updated);

        // 4. Lấy lại để kiểm tra
        Answer check = dao.getAnswerByID(fetched.getAnswerID());
        System.out.println("Sau khi update: " + check);

        // 5. Lấy danh sách tất cả answer của questionID
        List<Answer> listOfAnswers = dao.getAnswersByQuestionID(exampleQuestionID);
        System.out.println("Các answers của questionID=" + exampleQuestionID + ":");
        for (Answer a : listOfAnswers) {
            System.out.println("  • " + a);
        }

//        // 6. Xóa thử một answer
//        boolean deleted = dao.deleteAnswer(aid1);
//        System.out.println("Xóa answer ID=" + aid1 + " thành công? " + deleted);
//
//        // 7. Kiểm tra xem còn tồn tại không
//        Answer afterDel = dao.getAnswerByID(aid1);
//        System.out.println("Sau khi xóa, getAnswerByID trả về: " + afterDel);
    }
}
