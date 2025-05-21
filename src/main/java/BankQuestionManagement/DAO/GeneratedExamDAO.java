package BankQuestionManagement.DAO;

import BankQuestionManagement.Data.DatabaseConnector;
import BankQuestionManagement.Model.GeneratedExam;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GeneratedExamDAO {

    /**
     * Thêm mới GeneratedExam. Trả về generatedExamID.
     */
    public int addGeneratedExam(GeneratedExam gen) {
        String sql = "INSERT INTO GeneratedExams (ExamName, ExportPath) VALUES (?, ?)";
        String[] generatedCols = {"GeneratedExamID"};

        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql, generatedCols)
        ) {
            pst.setString(1, gen.getExamName());
            pst.setString(2, gen.getExportPath());

            int affected = pst.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Tạo GeneratedExam thất bại, không có dòng nào bị ảnh hưởng.");
            }
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    gen.setGeneratedExamID(newId);
                    return newId;
                } else {
                    throw new SQLException("Không lấy được GeneratedExamID sau khi chèn.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Cập nhật GeneratedExam (ExamName, ExportPath).
     */
    public boolean updateGeneratedExam(GeneratedExam gen) {
        String sql = "UPDATE GeneratedExams SET ExamName = ?, ExportPath = ?, CreatedDate = GETDATE() WHERE GeneratedExamID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setString(1, gen.getExamName());
            pst.setString(2, gen.getExportPath());
            pst.setInt(3, gen.getGeneratedExamID());

            int affected = pst.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa GeneratedExam theo generatedExamID (GeneratedExamQuestions liên quan bị xóa do cascade).
     */
    public boolean deleteGeneratedExam(int genID) {
        String sql = "DELETE FROM GeneratedExams WHERE GeneratedExamID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, genID);
            int affected = pst.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy GeneratedExam theo ID
     */
    public GeneratedExam getGeneratedExamByID(int genID) {
        String sql = "SELECT GeneratedExamID, ExamName, ExportPath, CreatedDate FROM GeneratedExams WHERE GeneratedExamID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, genID);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    GeneratedExam ge = new GeneratedExam();
                    ge.setGeneratedExamID(rs.getInt("GeneratedExamID"));
                    ge.setExamName(rs.getString("ExamName"));
                    ge.setExportPath(rs.getString("ExportPath"));
                    ge.setCreatedDate(rs.getTimestamp("CreatedDate"));
                    return ge;
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
     * Lấy tất cả GeneratedExams
     */
    public List<GeneratedExam> getAllGeneratedExams() {
        List<GeneratedExam> list = new ArrayList<>();
        String sql = "SELECT GeneratedExamID, ExamName, ExportPath, CreatedDate FROM GeneratedExams";
        try (
                Connection conn = DatabaseConnector.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                GeneratedExam ge = new GeneratedExam();
                ge.setGeneratedExamID(rs.getInt("GeneratedExamID"));
                ge.setExamName(rs.getString("ExamName"));
                ge.setExportPath(rs.getString("ExportPath"));
                ge.setCreatedDate(rs.getTimestamp("CreatedDate"));
                list.add(ge);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Cập nhật trường ExportPath cho GeneratedExam.
     */
    public boolean updateExportPath(int genExamID, String exportPath) {
        String sql = "UPDATE GeneratedExams SET ExportPath = ? WHERE GeneratedExamID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, exportPath);
            ps.setInt(2, genExamID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
