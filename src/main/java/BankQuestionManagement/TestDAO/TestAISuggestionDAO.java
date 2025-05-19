package BankQuestionManagement.TestDAO;

import BankQuestionManagement.DAO.AISuggestionDAO;
import BankQuestionManagement.Model.AISuggestion;

import java.util.List;

/**
 * Test đơn giản cho AISuggestionDAO:
 * - Thêm mới suggestion
 * - Lấy suggestion theo ID
 * - Cập nhật suggestion
 * - Lấy danh sách suggestion theo questionID
 * - Xóa suggestion
 */
public class TestAISuggestionDAO {
    public static void main(String[] args) {
        AISuggestionDAO dao = new AISuggestionDAO();

        // Giả sử đã có 1 Question (QuestionID = 1) trong bảng Questions, nếu chưa, chạy TestQuestionDAO trước
        int exampleQuestionID = 1;

        // 1. Thêm mới 2 AISuggestions
        AISuggestion as1 = new AISuggestion(exampleQuestionID, "Suggested: Ha Noi", 0.85f);
        AISuggestion as2 = new AISuggestion(exampleQuestionID, "Suggested: Hue", 0.60f);

        int sid1 = dao.addAISuggestion(as1);
        int sid2 = dao.addAISuggestion(as2);
        System.out.printf("Tạo AI suggestions xong: IDs = %d, %d%n", sid1, sid2);

        // 2. Lấy một AISuggestion theo ID
        AISuggestion fetched = dao.getAISuggestionByID(sid1);
        System.out.println("AISuggestion fetched: " + fetched);

        // 3. Cập nhật một AISuggestion
        fetched.setSuggestedAnswer("Suggested: Ho Chi Minh City (Cập nhật)");
        fetched.setConfidence(0.90f);
        boolean updated = dao.updateAISuggestion(fetched);
        System.out.println("Cập nhật AISuggestion ID=" + fetched.getSuggestionID() + ": " + updated);

        // 4. Lấy lại để kiểm tra
        AISuggestion check = dao.getAISuggestionByID(fetched.getSuggestionID());
        System.out.println("Sau khi update: " + check);

        // 5. Lấy danh sách AISuggestions của questionID
        List<AISuggestion> list = dao.getAISuggestionsByQuestionID(exampleQuestionID);
        System.out.println("Các AISuggestions của questionID=" + exampleQuestionID + ":");
        for (AISuggestion a : list) {
            System.out.println("  • " + a);
        }

        // 6. Xóa một AISuggestion
        boolean deleted = dao.deleteAISuggestion(sid2);
        System.out.println("Xóa AISuggestion ID=" + sid2 + " thành công? " + deleted);

        // 7. Kiểm tra sau khi xóa
        AISuggestion afterDel = dao.getAISuggestionByID(sid2);
        System.out.println("Sau khi xóa, getAISuggestionByID trả về: " + afterDel);
    }
}
