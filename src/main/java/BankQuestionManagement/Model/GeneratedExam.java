package BankQuestionManagement.Model;

import java.sql.Timestamp;
import java.util.List;

public class GeneratedExam {
    private int generatedExamID;
    private String examName;      // Tên tạm của đề được sinh
    private String exportPath;    // Đường dẫn lưu file PDF/DOC
    private Timestamp createdDate;
    private List<Question> questions;


    public GeneratedExam() {
    }

    // Constructor chèn mới (chưa có generatedExamID, createdDate)
    public GeneratedExam(String examName, String exportPath) {
        this.examName = examName;
        this.exportPath = exportPath;
    }

    // Constructor đầy đủ (nếu cần đọc từ DB)
    public GeneratedExam(int generatedExamID, String examName, String exportPath, Timestamp createdDate) {
        this.generatedExamID = generatedExamID;
        this.examName = examName;
        this.exportPath = exportPath;
        this.createdDate = createdDate;
    }

    // Getter & Setter
    public int getGeneratedExamID() {
        return generatedExamID;
    }

    public void setGeneratedExamID(int generatedExamID) {
        this.generatedExamID = generatedExamID;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getExportPath() {
        return exportPath;
    }

    public void setExportPath(String exportPath) {
        this.exportPath = exportPath;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    @Override
    public String toString() {
        return examName;
    }
}
