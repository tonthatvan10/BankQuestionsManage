package BankQuestionManagement.DAO;

import BankQuestionManagement.Data.DatabaseConnector;
import BankQuestionManagement.Model.GeneratedExamQuestion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GeneratedExamQuestionDAO {

    /**
     * Thêm một dòng vào GeneratedExamQuestions (GeneratedExamID, QuestionID).
     */
    public boolean addGeneratedExamQuestion(int generatedExamID, int questionID) {
        String sql = "INSERT INTO GeneratedExamQuestions (GeneratedExamID, QuestionID) VALUES (?, ?)";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, generatedExamID);
            pst.setInt(2, questionID);
            int affected = pst.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa một dòng trong GeneratedExamQuestions theo composite key (GeneratedExamID, QuestionID)
     */
    public boolean deleteGeneratedExamQuestion(int generatedExamID, int questionID) {
        String sql = "DELETE FROM GeneratedExamQuestions WHERE GeneratedExamID = ? AND QuestionID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, generatedExamID);
            pst.setInt(2, questionID);
            int affected = pst.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy danh sách GeneratedExamQuestion theo generatedExamID
     */
    public List<GeneratedExamQuestion> getByGeneratedExamID(int generatedExamID) {
        List<GeneratedExamQuestion> list = new ArrayList<>();
        String sql = "SELECT GeneratedExamID, QuestionID FROM GeneratedExamQuestions WHERE GeneratedExamID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, generatedExamID);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    GeneratedExamQuestion geq = new GeneratedExamQuestion();
                    geq.setGeneratedExamID(rs.getInt("GeneratedExamID"));
                    geq.setQuestionID(rs.getInt("QuestionID"));
                    list.add(geq);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy tất cả GeneratedExamQuestions (nếu cần).
     */
    public List<GeneratedExamQuestion> getAllGeneratedExamQuestions() {
        List<GeneratedExamQuestion> list = new ArrayList<>();
        String sql = "SELECT GeneratedExamID, QuestionID FROM GeneratedExamQuestions";
        try (
                Connection conn = DatabaseConnector.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                GeneratedExamQuestion geq = new GeneratedExamQuestion();
                geq.setGeneratedExamID(rs.getInt("GeneratedExamID"));
                geq.setQuestionID(rs.getInt("QuestionID"));
                list.add(geq);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
