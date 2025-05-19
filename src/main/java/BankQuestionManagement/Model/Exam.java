package BankQuestionManagement.Model;

import java.sql.Timestamp;

public class Exam {
    private int examID;
    private String examName;
    private String description;
    private String imagePath;  // NVARCHAR(500)
    private Timestamp createdDate;
    private Timestamp modifiedDate;

    public Exam() {
    }

    public Exam(int examID, String examName, String description, String imagePath, Timestamp createdDate, Timestamp modifiedDate) {
        this.examID = examID;
        this.examName = examName;
        this.description = description;
        this.imagePath = imagePath;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }

    public int getExamID() {
        return this.examID;
    }

    public void setExamID(int examID) {
        this.examID = examID;
    }

    public String getExamName() {
        return this.examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    Timestamp getCreatedDate() {
        return this.createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Timestamp getModifiedDate() {
        return this.modifiedDate;
    }

    public void setModifiedDate(Timestamp modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String toString() {
        return examName;
    }
}

