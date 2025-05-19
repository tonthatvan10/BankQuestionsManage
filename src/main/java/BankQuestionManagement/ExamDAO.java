package BankQuestionManagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamDAO {

    public void addExam(Exam exam) {
        String sql = "INSERT INTO Exams (ExamName, Description) VALUES (?, ?)";

        try (
                Connection connection = DatabaseConnector.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, exam.getExamName());
            statement.setString(2, exam.getDescription());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Exam> getAllExams() {
        List<Exam> exams = new ArrayList<>();
        String sql = "SELECT * FROM Exams";

        try (
                Connection connection = DatabaseConnector.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            while (resultSet.next()) {
                Exam exam = new Exam();
                exam.setExamID(resultSet.getInt("ExamID"));
                exam.setExamName(resultSet.getString("ExamName"));
                exam.setDescription(resultSet.getString("Description"));
                exam.setCreatedDate(resultSet.getTimestamp("CreatedDate"));
                exam.setModifiedDate(resultSet.getTimestamp("ModifiedDate"));
                exams.add(exam);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exams;
    }

    public void updateExam(Exam exam) {
        String sql = "UPDATE Exams SET ExamName = ?, Description = ?, ModifiedDate = GETDATE() WHERE ExamID = ?";

        try (
                Connection connection = DatabaseConnector.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, exam.getExamName());
            statement.setString(2, exam.getDescription());
            statement.setInt(3, exam.getExamID());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteExam(int examID) {
        String sql = "DELETE FROM Exams WHERE ExamID = ?";

        try (
                Connection connection = DatabaseConnector.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, examID);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
