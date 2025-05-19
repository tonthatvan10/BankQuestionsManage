package BankQuestionManagement.TestDAO;

import BankQuestionManagement.DAO.GeneratedExamQuestionDAO;
import BankQuestionManagement.Model.GeneratedExamQuestion;

import java.util.List;

/**
 * Test đơn giản cho GeneratedExamQuestionDAO:
 * - Thêm mới (GeneratedExamID, QuestionID)
 * - Lấy theo GeneratedExamID
 * - Xóa (GeneratedExamID, QuestionID)
 */
public class TestGeneratedExamQuestionDAO {
    public static void main(String[] args) {
        GeneratedExamQuestionDAO dao = new GeneratedExamQuestionDAO();

        // Giả sử đã có 1 GeneratedExam (ID = 1) và 2 Question (IDs = 1, 2)
        int exampleGenExamID = 1;
        int qId1 = 1;
        int qId2 = 2;

        // 1. Thêm 2 dòng
        boolean add1 = dao.addGeneratedExamQuestion(exampleGenExamID, qId1);
        boolean add2 = dao.addGeneratedExamQuestion(exampleGenExamID, qId2);
        System.out.println("Thêm GeneratedExamQuestion (1,1)? " + add1);
        System.out.println("Thêm GeneratedExamQuestion (1,2)? " + add2);

        // 2. Lấy danh sách theo generatedExamID = 1
        List<GeneratedExamQuestion> list = dao.getByGeneratedExamID(exampleGenExamID);
        System.out.println("Các GeneratedExamQuestion của GeneratedExamID = " + exampleGenExamID + ":");
        for (GeneratedExamQuestion geq : list) {
            System.out.println("  • " + geq);
        }

        // 3. Xóa một dòng (1,1)
        boolean delete1 = dao.deleteGeneratedExamQuestion(exampleGenExamID, qId1);
        System.out.println("Xóa GeneratedExamQuestion (1,1)? " + delete1);

        // 4. Lấy lại danh sách để kiểm tra
        List<GeneratedExamQuestion> after = dao.getByGeneratedExamID(exampleGenExamID);
        System.out.println("Sau khi xóa (1,1), danh sách:");
        for (GeneratedExamQuestion geq : after) {
            System.out.println("  • " + geq);
        }
    }
}
