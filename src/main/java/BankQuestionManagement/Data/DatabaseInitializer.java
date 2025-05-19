package BankQuestionManagement.Data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://DESKTOP-T0OKSLM;databaseName=BankQuestions;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()){

            // Tạo bảng Exams
            statement.executeUpdate(
                    """
                    IF NOT EXISTS (
                       SELECT * FROM sys.objects
                       WHERE object_id = OBJECT_ID(N'Exams') AND type = N'U'
                    )
                    BEGIN
                        CREATE TABLE Exams (
                            ExamID INT PRIMARY KEY IDENTITY,
                            ExamName NVARCHAR(255) NOT NULL,
                            Description NVARCHAR(MAX),
                            ImagePath NVARCHAR(500),
                            CreatedDate DATETIME DEFAULT GETDATE(),
                            ModifiedDate DATETIME DEFAULT GETDATE()
                        );
                    END
                    """
            );

            // Tạo bảng Questions
            statement.executeUpdate(
                    """
                        IF NOT EXISTS (
                            SELECT * FROM sys.objects
                            WHERE object_id = OBJECT_ID(N'Questions') AND type = N'U'
                        )
                        BEGIN
                            CREATE TABLE Questions (
                                QuestionID INT PRIMARY KEY IDENTITY,
                                ExamID INT FOREIGN KEY REFERENCES Exams(ExamID) ON DELETE CASCADE,
                                Content NVARCHAR(MAX) NOT NULL,
                                AudioPath NVARCHAR(500),
                                CreatedDate DATETIME DEFAULT GETDATE()
                            );
                        END
                        """
            );

            //Tạo bảng Answers
            statement.executeUpdate(
                    """
                        IF NOT EXISTS (
                            SELECT * FROM sys.objects
                            WHERE object_id = OBJECT_ID(N'Answers') AND type = N'U'
                        )
                        BEGIN
                            CREATE TABLE Answers (
                                AnswerID INT PRIMARY KEY IDENTITY,
                                QuestionID INT FOREIGN KEY REFERENCES Questions(QuestionID) ON DELETE CASCADE,
                                AnswerText NVARCHAR(MAX) NOT NULL,
                                IsCorrect BIT DEFAULT 0
                            );
                        END
                          
                        """
            );

            // Tạo bảng AI_Suggestions
            statement.executeUpdate(
                    """
                            IF NOT EXISTS (
                               SELECT * FROM sys.objects
                               WHERE object_id = OBJECT_ID(N'AI_Suggestions') AND type = N'U'
                            )
                            BEGIN
                                CREATE TABLE AI_Suggestions (
                                    SuggestionID INT PRIMARY KEY IDENTITY,
                                    QuestionID INT FOREIGN KEY REFERENCES Questions(QuestionID) ON DELETE CASCADE,
                                    SuggestedAnswer NVARCHAR(MAX) NOT NULL,
                                    Confidence FLOAT
                                );
                            END
                        """
            );

            //Tạo bảng GeneratedExams
            statement.executeUpdate(
                    """
                            IF NOT EXISTS (
                                SELECT * FROM sys.objects
                                WHERE object_id = OBJECT_ID(N'GeneratedExams') AND type = N'U'
                            )
                            BEGIN
                                CREATE TABLE GeneratedExams (
                                    GeneratedExamID INT PRIMARY KEY IDENTITY,
                                    ExamName NVARCHAR(255) NOT NULL,
                                    ExportPath NVARCHAR(500) NOT NULL,
                                    CreatedDate DATETIME DEFAULT GETDATE()
                                );
                            END
                        """
            );

            // Tạo bảng GeneratedExamQuestions
            statement.executeUpdate(
                    """
                            IF NOT EXISTS (
                               SELECT * FROM sys.objects
                               WHERE object_id = OBJECT_ID(N'GeneratedExamQuestions') AND type = N'U'
                            )
                            BEGIN
                                CREATE Table GeneratedExamQuestions (
                                    GeneratedExamID INT FOREIGN KEY REFERENCES GeneratedExams(GeneratedExamID) ON DELETE CASCADE,
                                    QuestionID INT FOREIGN KEY REFERENCES Questions(QuestionID),
                                    PRIMARY KEY (GeneratedExamID, QuestionID)
                                );
                            END
                        """
            );

            System.out.println("Tao bang thanh cong");
            connection.close();
        } catch(SQLException e){
            System.out.println("Loi ket noi");
            e.printStackTrace();
        }
    }
}

