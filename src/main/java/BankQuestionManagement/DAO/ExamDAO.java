package BankQuestionManagement.DAO;

import BankQuestionManagement.Data.DatabaseConnector;
import BankQuestionManagement.Model.Exam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExamDAO {

    /**
     * Thêm một Exam mới, bao gồm đường dẫn imagePath, và trả về examID sinh tự động
     */
    public int addExam(Exam exam) {
        String sql = "INSERT INTO Exams (ExamName, Description, ImagePath) VALUES (?, ?, ?)";

        try (
                Connection connection = DatabaseConnector.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, exam.getExamName());
            ps.setString(2, exam.getDescription());
            ps.setString(3, exam.getImagePath());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Tạo Exam thất bại, không có dòng nào bị ảnh hưởng.");
            }

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    exam.setExamID(newId);
                    return newId;
                } else {
                    throw new SQLException("Không lấy được ExamID sau khi chèn.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Lấy danh sách tất cả Exam, bao gồm imagePath
     */
    public List<Exam> getAllExams() {
        List<Exam> exams = new ArrayList<>();
        String sql = "SELECT ExamID, ExamName, Description, ImagePath, CreatedDate, ModifiedDate FROM Exams";

        try (
                var connection = DatabaseConnector.getConnection();
                var stmt = connection.createStatement();
                var rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                Exam exam = new Exam();
                exam.setExamID(rs.getInt("ExamID"));
                exam.setExamName(rs.getString("ExamName"));
                exam.setDescription(rs.getString("Description"));
                exam.setImagePath(rs.getString("ImagePath"));
                exam.setCreatedDate(rs.getTimestamp("CreatedDate"));
                exam.setModifiedDate(rs.getTimestamp("ModifiedDate"));
                exams.add(exam);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    /**
     * Cập nhật Exam (ExamName, Description, ImagePath) và ModifiedDate tự động
     */
    public boolean updateExam(Exam exam) {
        String sql = "UPDATE Exams SET ExamName = ?, Description = ?, ImagePath = ?, ModifiedDate = GETDATE() WHERE ExamID = ?";

        try (
                var connection = DatabaseConnector.getConnection();
                var ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, exam.getExamName());
            ps.setString(2, exam.getDescription());
            ps.setString(3, exam.getImagePath());
            ps.setInt(4, exam.getExamID());

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa Exam theo examID cùng tất cả Question và Answer (với cascade DB) liên quan.
     */
    public boolean deleteExam(int examID) {
        String delQuestionsSql = "DELETE FROM Questions WHERE ExamID = ?";
        String delExamSql = "DELETE FROM Exams WHERE ExamID = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            // Xóa câu hỏi; các Answers, AISuggestions liên quan tự động xóa theo ON DELETE CASCADE
            try (PreparedStatement pstQ = conn.prepareStatement(delQuestionsSql)) {
                pstQ.setInt(1, examID);
                pstQ.executeUpdate();
            }

            // Xóa Exam
            int affected;
            try (PreparedStatement pstE = conn.prepareStatement(delExamSql)) {
                pstE.setInt(1, examID);
                affected = pstE.executeUpdate();
            }

            if (affected == 0) {
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Lấy Exam theo ID, bao gồm imagePath
     */
    public Exam getExamById(int examID) {
        String sql = "SELECT ExamID, ExamName, Description, ImagePath, CreatedDate, ModifiedDate "
                + "FROM Exams WHERE ExamID = ?";

        try (
                var connection = DatabaseConnector.getConnection();
                var ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, examID);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    Exam exam = new Exam();
                    exam.setExamID(rs.getInt("ExamID"));
                    exam.setExamName(rs.getString("ExamName"));
                    exam.setDescription(rs.getString("Description"));
                    exam.setImagePath(rs.getString("ImagePath"));
                    exam.setCreatedDate(rs.getTimestamp("CreatedDate"));
                    exam.setModifiedDate(rs.getTimestamp("ModifiedDate"));
                    return exam;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
