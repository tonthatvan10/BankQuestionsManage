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

        // 3. Tạo GeneratedExam với tên tạm (rỗng hoặc bất kỳ)
        GeneratedExam ge = new GeneratedExam("", "");
        int genId = genExamDAO.addGeneratedExam(ge);
        if (genId < 0) {
            throw new RuntimeException("Tạo GeneratedExam thất bại.");
        }
        ge.setGeneratedExamID(genId);

        // 4. Cập nhật lại ExamName theo ID vừa sinh
        String logicalName = "RandomExam_" + genId;
        ge.setExamName(logicalName);
        // (nếu muốn thay đổi exportPath lúc này cũng gọi ge.setExportPath(...))
        boolean updated = genExamDAO.updateGeneratedExam(ge);
        if (!updated) {
            // xử lý nếu update thất bại
            System.err.println("Cảnh báo: không cập nhật được tên đề cho GeneratedExamID=" + genId);
        }

        // 5. Lưu mỗi câu vào GeneratedExamQuestions
        for (Question q : chosen) {
            genExamQDAO.addGeneratedExamQuestion(genId, q.getQuestionID());
        }

        // 6. Gán danh sách câu hỏi để trả về
        ge.setQuestions(chosen);
        return ge;
    }
}