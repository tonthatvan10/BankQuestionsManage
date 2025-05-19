package BankQuestionManagement;

public class AISuggestion {
    private int suggestionID;
    private int questionID;
    private String suggestedAnswer;  // NVARCHAR(MAX)
    private float confidence;        // FLOAT

    public AISuggestion() {
    }

    // Constructor chèn mới
    public AISuggestion(int questionID, String suggestedAnswer, float confidence) {
        this.questionID = questionID;
        this.suggestedAnswer = suggestedAnswer;
        this.confidence = confidence;
    }

    // Constructor đầy đủ
    public AISuggestion(int suggestionID, int questionID, String suggestedAnswer, float confidence) {
        this.suggestionID = suggestionID;
        this.questionID = questionID;
        this.suggestedAnswer = suggestedAnswer;
        this.confidence = confidence;
    }

    // Getter & Setter
    public int getSuggestionID() {
        return suggestionID;
    }

    public void setSuggestionID(int suggestionID) {
        this.suggestionID = suggestionID;
    }

    public int getQuestionID() {
        return questionID;
    }

    public void setQuestionID(int questionID) {
        this.questionID = questionID;
    }

    public String getSuggestedAnswer() {
        return suggestedAnswer;
    }

    public void setSuggestedAnswer(String suggestedAnswer) {
        this.suggestedAnswer = suggestedAnswer;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "AISuggestion{" +
                "suggestionID=" + suggestionID +
                ", questionID=" + questionID +
                ", suggestedAnswer='" + suggestedAnswer + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
