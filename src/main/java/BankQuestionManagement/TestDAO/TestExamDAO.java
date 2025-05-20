package BankQuestionManagement.TestDAO;

import BankQuestionManagement.DAO.ExamDAO;
import BankQuestionManagement.Model.Exam;

import java.util.List;

public class TestExamDAO {
    public static void main(String[] args) {
        ExamDAO dao = new ExamDAO();

        // Thêm đề mới
        Exam newExam = new Exam();
        newExam.setExamName("DE_THI_JLPT_N2");
        newExam.setDescription("Đề luyện tập N2 tháng 7");
        newExam.setImagePath("C:/Users/Ton That Van/Downloads/DE_THI_TIENG_NHAT/1 (2).jpg");
        dao.addExam(newExam);

//        // Hiển thị tất cả đề thi
//        List<Exam> list = dao.getAllExams();
//        for (Exam e : list) {
//            System.out.println(e.getExamID() + ": " + e.getExamName() + " - " + e.getDescription());
//        }
//
//        // Cập nhật đề
//        if (!list.isEmpty()) {
//            Exam exam = list.get(0);
//            exam.setExamName("Đề thi JLPT N3 - UPDATED");
//            exam.setDescription("Mô tả mới cho đề thi N3");
//            dao.updateExam(exam);
//        }

        // Xoá đề
        // dao.deleteExam(1);
    }
}
