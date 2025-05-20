package BankQuestionManagement.Service;

import BankQuestionManagement.DAO.GeneratedExamDAO;
import BankQuestionManagement.DAO.GeneratedExamQuestionDAO;
import BankQuestionManagement.DAO.QuestionDAO;
import BankQuestionManagement.Model.GeneratedExam;
import BankQuestionManagement.Model.Question;

import java.util.Collections;
import java.util.List;

public class GeneratedExamService {
    private final QuestionDAO questionDAO              = new QuestionDAO();
    private final GeneratedExamDAO genExamDAO          = new GeneratedExamDAO();
    private final GeneratedExamQuestionDAO genExamQDAO = new GeneratedExamQuestionDAO();

    /**
     * Sinh ngẫu nhiên một đề gồm `count` câu hỏi từ toàn bộ ngân hàng câu hỏi.
     * - Lấy tất cả câu hỏi trong Question
     * - Xáo trộn
     * - Chọn count câu
     * - Tạo bản ghi GeneratedExam, rồi lưu vào GeneratedExamQuestions
     *
     * @param count  số câu hỏi muốn lấy
     * @return GeneratedExam với generatedExamID và danh sách câu hỏi đã chọn
     */
    public GeneratedExam generateRandomExam(int count) {
        // 1. Lấy toàn bộ câu hỏi
        List<Question> all = questionDAO.getAllQuestions();
        if (all.size() < count) {
            throw new IllegalArgumentException(
                    "Không đủ câu hỏi (" + all.size() + ") để tạo đề " + count + " câu."
            );
        }

        // 2. Xáo trộn và chọn
        Collections.shuffle(all);
        List<Question> chosen = all.subList(0, count);

        // 3. Tạo GeneratedExam (chưa có exportPath)
        String title = "Random Exam (" + count + " items)";
        GeneratedExam ge = new GeneratedExam(title, "");
        int genId = genExamDAO.addGeneratedExam(ge);
        ge.setGeneratedExamID(genId);

        // 4. Lưu mỗi câu vào GeneratedExamQuestions
        for (Question q : chosen) {
            genExamQDAO.addGeneratedExamQuestion(genId, q.getQuestionID());
        }

        // 5. Gắn lại danh sách đã chọn để trả về
        ge.setQuestions(chosen);
        return ge;
    }
}