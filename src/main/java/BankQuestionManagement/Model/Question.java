package BankQuestionManagement.Model;

import java.sql.Timestamp;

public class Question {
    private int questionID;
    private int examID;
    private String content;    // NVARCHAR(MAX)
    private String audioPath;  // NVARCHAR(500)
    private Timestamp createdDate;

    public Question() {
    }

    // Constructor để chèn mới (chưa có questionID, createdDate)
    public Question(int examID, String content, String audioPath) {
        this.examID = examID;
        this.content = content;
        this.audioPath = audioPath;
    }

    // Full constructor (nếu cần đọc từ DB)
    public Question(int questionID, int examID, String content, String imagePath, String audioPath, Timestamp createdDate) {
        this.questionID = questionID;
        this.examID = examID;
        this.content = content;
        this.audioPath = audioPath;
        this.createdDate = createdDate;
    }

    // Getter & Setter
    public int getQuestionID() {
        return questionID;
    }

    public void setQuestionID(int questionID) {
        this.questionID = questionID;
    }

    public int getExamID() {
        return examID;
    }

    public void setExamID(int examID) {
        this.examID = examID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "Question{" +
                "questionID=" + questionID +
                ", examID=" + examID +
                ", content='" + content + '\'' +
                ", audioPath='" + audioPath + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}
