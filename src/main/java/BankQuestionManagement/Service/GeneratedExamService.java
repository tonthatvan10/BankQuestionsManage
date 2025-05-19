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
     * Sinh ngẫu nhiên một đề gồm `count` câu hỏi từ ngân hàng đề gốc `examId`.
     * - Lấy tất cả câu hỏi
     * - Xáo trộn
     * - Chọn count câu
     * - Tạo bản ghi GeneratedExam, rồi lưu vào GeneratedExamQuestions
     *
     * @param examId mã đề gốc
     * @param count  số câu hỏi muốn lấy
     * @return GeneratedExam có generatedExamID và danh sách câu hỏi đã chọn (setQuestions)
     */
    public GeneratedExam generateRandomExam(int examId, int count) {
        // 1. Lấy toàn bộ
        List<Question> all = questionDAO.getQuestionsByExamID(examId);
        if (all.size() < count) {
            throw new IllegalArgumentException(
                    "Không đủ câu hỏi (" + all.size() + ") để tạo đề " + count + " câu."
            );
        }

        // 2. Xáo trộn và chọn
        Collections.shuffle(all);
        List<Question> chosen = all.subList(0, count);

        // 3. Tạo GeneratedExam (chưa có exportPath)
        String title = "Random from Exam#" + examId + " (" + count + " items)";
        GeneratedExam ge = new GeneratedExam(title, "");
        int genId = genExamDAO.addGeneratedExam(ge);
        ge.setGeneratedExamID(genId);

        // 4. Lưu mỗi câu vào GeneratedExamQuestions
        for (Question q : chosen) {
            genExamQDAO.addGeneratedExamQuestion(genId, q.getQuestionID());
        }

        // 5. Gắn lại danh sách đã chọn để trả về
        ge.setQuestions(chosen);  // bạn có thể thêm trường List<Question> vào GeneratedExam model
        return ge;
    }
}
