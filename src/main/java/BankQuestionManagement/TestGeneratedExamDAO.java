package BankQuestionManagement;

import BankQuestionManagement.*;

import java.util.List;

/**
 * Test đơn giản cho GeneratedExamDAO:
 * - Thêm mới GeneratedExam
 * - Lấy GeneratedExam theo ID
 * - Cập nhật GeneratedExam
 * - Lấy danh sách tất cả GeneratedExams
 * - Xóa GeneratedExam
 */
public class TestGeneratedExamDAO {
    public static void main(String[] args) {
        GeneratedExamDAO dao = new GeneratedExamDAO();

        // 1. Thêm mới GeneratedExam
        GeneratedExam ge1 = new GeneratedExam("Đề tập 1", "C:/exports/DeTap1.pdf");
        int newid = dao.addGeneratedExam(ge1);
        System.out.println("Tạo GeneratedExam mới, ID = " + newid);

        // 2. Lấy GeneratedExam vừa tạo
        GeneratedExam fetched = dao.getGeneratedExamByID(newid);
        System.out.println("GeneratedExam fetched: " + fetched);

        // 3. Cập nhật GeneratedExam
        fetched.setExamName("Đề tập 1 (Cập nhật)");
        fetched.setExportPath("C:/exports/DeTap1_v2.pdf");
        boolean updated = dao.updateGeneratedExam(fetched);
        System.out.println("Cập nhật GeneratedExam thành công? " + updated);

        // 4. Lấy lại để kiểm tra
        GeneratedExam check2 = dao.getGeneratedExamByID(newid);
        System.out.println("Sau khi update: " + check2);

        // 5. Lấy tất cả GeneratedExams
        List<GeneratedExam> all = dao.getAllGeneratedExams();
        System.out.println("Danh sách tất cả GeneratedExams:");
        for (GeneratedExam g : all) {
            System.out.println("  • " + g);
        }

        // 6. Xóa GeneratedExam
        boolean deleted = dao.deleteGeneratedExam(newid);
        System.out.println("Xóa GeneratedExam ID=" + newid + " thành công? " + deleted);

        // 7. Kiểm tra sau xóa
        GeneratedExam afterDel = dao.getGeneratedExamByID(newid);
        System.out.println("Sau khi xóa, getGeneratedExamByID trả về: " + afterDel);
    }
}
