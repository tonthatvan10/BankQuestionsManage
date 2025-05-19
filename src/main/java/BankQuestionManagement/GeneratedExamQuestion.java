package BankQuestionManagement;

public class GeneratedExamQuestion {
    private int generatedExamID;
    private int questionID;

    public GeneratedExamQuestion() {
    }

    public GeneratedExamQuestion(int generatedExamID, int questionID) {
        this.generatedExamID = generatedExamID;
        this.questionID = questionID;
    }

    // Getter & Setter
    public int getGeneratedExamID() {
        return generatedExamID;
    }

    public void setGeneratedExamID(int generatedExamID) {
        this.generatedExamID = generatedExamID;
    }

    public int getQuestionID() {
        return questionID;
    }

    public void setQuestionID(int questionID) {
        this.questionID = questionID;
    }

    @Override
    public String toString() {
        return "GeneratedExamQuestion{" +
                "generatedExamID=" + generatedExamID +
                ", questionID=" + questionID +
                '}';
    }
}
