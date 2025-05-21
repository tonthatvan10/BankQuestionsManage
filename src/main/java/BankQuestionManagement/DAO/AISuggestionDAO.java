package BankQuestionManagement.DAO;

import BankQuestionManagement.Model.AISuggestion;
import BankQuestionManagement.Data.DatabaseConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AISuggestionDAO {

    /**
     * Thêm một AISuggestion mới. Trả về suggestionID tự sinh.
     */
    public int addAISuggestion(AISuggestion suggestion) {
        String sql = "INSERT INTO AI_Suggestions (QuestionID, SuggestedAnswer, Confidence) VALUES (?, ?, ?)";
        String[] generatedCols = {"SuggestionID"};

        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql, generatedCols)
        ) {
            pst.setInt(1, suggestion.getQuestionID());
            pst.setString(2, suggestion.getSuggestedAnswer());
            pst.setFloat(3, suggestion.getConfidence());

            int affected = pst.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Tạo AISuggestion thất bại, không có dòng nào bị ảnh hưởng.");
            }
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    suggestion.setSuggestionID(newId);
                    return newId;
                } else {
                    throw new SQLException("Không lấy được SuggestionID sau khi chèn.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Cập nhật một AISuggestion (SuggestedAnswer và Confidence).
     */
    public boolean updateAISuggestion(AISuggestion suggestion) {
        String sql = "UPDATE AI_Suggestions SET SuggestedAnswer = ?, Confidence = ? WHERE SuggestionID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setString(1, suggestion.getSuggestedAnswer());
            pst.setFloat(2, suggestion.getConfidence());
            pst.setInt(3, suggestion.getSuggestionID());

            int affected = pst.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa AISuggestion theo suggestionID
     */
    public boolean deleteAISuggestion(int suggestionID) {
        String sql = "DELETE FROM AI_Suggestions WHERE SuggestionID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, suggestionID);
            int affected = pst.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy AISuggestion theo suggestionID
     */
    public AISuggestion getAISuggestionByID(int suggestionID) {
        String sql = "SELECT SuggestionID, QuestionID, SuggestedAnswer, Confidence FROM AI_Suggestions WHERE SuggestionID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, suggestionID);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    AISuggestion as = new AISuggestion();
                    as.setSuggestionID(rs.getInt("SuggestionID"));
                    as.setQuestionID(rs.getInt("QuestionID"));
                    as.setSuggestedAnswer(rs.getString("SuggestedAnswer"));
                    as.setConfidence(rs.getFloat("Confidence"));
                    return as;
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
     * Lấy tất cả AISuggestion của một questionID
     */
    public List<AISuggestion> getAISuggestionsByQuestionID(int questionID) {
        List<AISuggestion> list = new ArrayList<>();
        String sql = "SELECT SuggestionID, QuestionID, SuggestedAnswer, Confidence FROM AI_Suggestions WHERE QuestionID = ?";
        try (
                Connection conn = DatabaseConnector.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)
        ) {
            pst.setInt(1, questionID);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    AISuggestion as = new AISuggestion();
                    as.setSuggestionID(rs.getInt("SuggestionID"));
                    as.setQuestionID(rs.getInt("QuestionID"));
                    as.setSuggestedAnswer(rs.getString("SuggestedAnswer"));
                    as.setConfidence(rs.getFloat("Confidence"));
                    list.add(as);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy tất cả AISuggestions (nếu cần)
     */
    public List<AISuggestion> getAllAISuggestions() {
        List<AISuggestion> list = new ArrayList<>();
        String sql = "SELECT SuggestionID, QuestionID, SuggestedAnswer, Confidence FROM AI_Suggestions";
        try (
                Connection conn = DatabaseConnector.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                AISuggestion as = new AISuggestion();
                as.setSuggestionID(rs.getInt("SuggestionID"));
                as.setQuestionID(rs.getInt("QuestionID"));
                as.setSuggestedAnswer(rs.getString("SuggestedAnswer"));
                as.setConfidence(rs.getFloat("Confidence"));
                list.add(as);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Xóa tất cả AISuggestion theo questionID
    public boolean deleteByQuestionID(int questionID) {
        String sql = "DELETE FROM AI_Suggestions WHERE QuestionID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, questionID);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
