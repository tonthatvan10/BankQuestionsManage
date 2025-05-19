package BankQuestionManagement.DAO;

import BankQuestionManagement.Data.DatabaseConnector;
import BankQuestionManagement.Model.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    /**
     * Thêm mới một Question.
     * Bảng chỉ có (ExamID, Content), AudioPath tự động null hoặc có thể set.
     * Trả về QuestionID vừa được tạo (sử dụng getGeneratedKeys).
     */
    public int addQuestion(Question question) {
        // Chỉ chèn ExamID, Content. Nếu muốn set AudioPath, thêm vào VALUES và gọi setString(3,...)
        String sql = "INSERT INTO Questions (ExamID, Content, AudioPath) VALUES (?, ?, ?)";
        String[] generatedCols = {"QuestionID"};

        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql, generatedCols)
        ) {
            pst.setInt(1, question.getExamID());
            pst.setString(2, question.getContent());
            pst.setString(3, question.getAudioPath()); // Nếu không có audio thì bạn có thể truyền null

            int affected = pst.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Tạo question thất bại, không có dòng nào bị ảnh hưởng.");
            }
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    question.setQuestionID(newId);
                    return newId;
                } else {
                    throw new SQLException("Không lấy được QuestionID sau khi chèn.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Cập nhật thông tin Question (chỉ update Content, AudioPath).
     * SQL không còn tham chiếu đến ImagePath nữa.
     */
    public boolean updateQuestion(Question question) {
        String sql = "UPDATE Questions SET Content = ?, AudioPath = ? WHERE QuestionID = ?";

        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setString(1, question.getContent());
            pst.setString(2, question.getAudioPath());
            pst.setInt(3, question.getQuestionID());

            int affected = pst.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa Question theo questionID (các Answers, AISuggestions liên quan tự động xóa do ON DELETE CASCADE).
     */
    public boolean deleteQuestion(int questionID) {
        String sql = "DELETE FROM Questions WHERE QuestionID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, questionID);
            int affected = pst.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy một Question theo questionID
     */
    public Question getQuestionByID(int questionID) {
        String sql = "SELECT QuestionID, ExamID, Content, AudioPath, CreatedDate FROM Questions WHERE QuestionID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, questionID);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Question q = new Question();
                    q.setQuestionID(rs.getInt("QuestionID"));
                    q.setExamID(rs.getInt("ExamID"));
                    q.setContent(rs.getString("Content"));
                    q.setAudioPath(rs.getString("AudioPath"));
                    q.setCreatedDate(rs.getTimestamp("CreatedDate"));
                    return q;
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
     * Lấy danh sách tất cả Question
     */
    public List<Question> getAllQuestions() {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT QuestionID, ExamID, Content, AudioPath, CreatedDate FROM Questions";
        try (
                Connection conn = DatabaseConnector.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                Question q = new Question();
                q.setQuestionID(rs.getInt("QuestionID"));
                q.setExamID(rs.getInt("ExamID"));
                q.setContent(rs.getString("Content"));
                q.setAudioPath(rs.getString("AudioPath"));
                q.setCreatedDate(rs.getTimestamp("CreatedDate"));
                list.add(q);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy danh sách Question theo examID (tất cả câu hỏi của một đề)
     */
    public List<Question> getQuestionsByExamID(int examID) {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT QuestionID, ExamID, Content, AudioPath, CreatedDate " +
                "FROM Questions WHERE ExamID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, examID);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setQuestionID(rs.getInt("QuestionID"));
                    q.setExamID(rs.getInt("ExamID"));
                    q.setContent(rs.getString("Content"));
                    q.setAudioPath(rs.getString("AudioPath"));
                    q.setCreatedDate(rs.getTimestamp("CreatedDate"));
                    list.add(q);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
