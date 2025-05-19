package BankQuestionManagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnswerDAO {

    /**
     * Thêm mới một Answer. Trả về answerID tự sinh nếu thành công.
     */
    public int addAnswer(Answer answer) {
        String sql = "INSERT INTO Answers (QuestionID, AnswerText, IsCorrect) VALUES (?, ?, ?)";
        String[] generatedCols = {"AnswerID"};

        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql, generatedCols)
        ) {
            pst.setInt(1, answer.getQuestionID());
            pst.setString(2, answer.getAnswerText());
            pst.setBoolean(3, answer.isCorrect());

            int affected = pst.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Tạo answer thất bại, không có dòng nào bị ảnh hưởng.");
            }
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    answer.setAnswerID(newId);
                    return newId;
                } else {
                    throw new SQLException("Không lấy được AnswerID sau khi chèn.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Cập nhật một Answer (chỉ cập nhật AnswerText và IsCorrect).
     */
    public boolean updateAnswer(Answer answer) {
        String sql = "UPDATE Answers SET AnswerText = ?, IsCorrect = ? WHERE AnswerID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setString(1, answer.getAnswerText());
            pst.setBoolean(2, answer.isCorrect());
            pst.setInt(3, answer.getAnswerID());

            int affected = pst.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa Answer theo answerID.
     * (Không có ON DELETE CASCADE ngược lại vì Answers là bảng con, ta chỉ xóa Answer một cách trực tiếp.)
     */
    public boolean deleteAnswer(int answerID) {
        String sql = "DELETE FROM Answers WHERE AnswerID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, answerID);
            int affected = pst.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy Answer theo answerID
     */
    public Answer getAnswerByID(int answerID) {
        String sql = "SELECT AnswerID, QuestionID, AnswerText, IsCorrect FROM Answers WHERE AnswerID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, answerID);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Answer a = new Answer();
                    a.setAnswerID(rs.getInt("AnswerID"));
                    a.setQuestionID(rs.getInt("QuestionID"));
                    a.setAnswerText(rs.getString("AnswerText"));
                    a.setCorrect(rs.getBoolean("IsCorrect"));
                    return a;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy danh sách tất cả Answers của một questionId
     */
    public List<Answer> getAnswersByQuestionID(int questionID) {
        List<Answer> list = new ArrayList<>();
        String sql = "SELECT AnswerID, QuestionID, AnswerText, IsCorrect FROM Answers WHERE QuestionID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, questionID);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Answer a = new Answer();
                    a.setAnswerID(rs.getInt("AnswerID"));
                    a.setQuestionID(rs.getInt("QuestionID"));
                    a.setAnswerText(rs.getString("AnswerText"));
                    a.setCorrect(rs.getBoolean("IsCorrect"));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy tất cả Answers (nếu cần)
     */
    public List<Answer> getAllAnswers() {
        List<Answer> list = new ArrayList<>();
        String sql = "SELECT AnswerID, QuestionID, AnswerText, IsCorrect FROM Answers";
        try (
                Connection conn = DatabaseConnector.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                Answer a = new Answer();
                a.setAnswerID(rs.getInt("AnswerID"));
                a.setQuestionID(rs.getInt("QuestionID"));
                a.setAnswerText(rs.getString("AnswerText"));
                a.setCorrect(rs.getBoolean("IsCorrect"));
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
