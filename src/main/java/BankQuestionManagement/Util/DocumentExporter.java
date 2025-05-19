package BankQuestionManagement.Util;

import BankQuestionManagement.DAO.AnswerDAO;
import BankQuestionManagement.DAO.ExamDAO;
import BankQuestionManagement.DAO.GeneratedExamDAO;
import BankQuestionManagement.DAO.GeneratedExamQuestionDAO;
import BankQuestionManagement.DAO.QuestionDAO;
import BankQuestionManagement.Model.Answer;
import BankQuestionManagement.Model.Exam;
import BankQuestionManagement.Model.GeneratedExam;
import BankQuestionManagement.Model.GeneratedExamQuestion;
import BankQuestionManagement.Model.Question;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Utility class để xuất:
 * 1) Đề thi gốc (Exam) cùng các Question/Answer thuộc ExamID cho trước.
 * 2) Generated Exam (GeneratedExam) dựa vào các Question được cấp phát sẵn.
 */
public class DocumentExporter {
    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final AnswerDAO answerDAO = new AnswerDAO();
    private final GeneratedExamDAO genExamDAO = new GeneratedExamDAO();
    private final GeneratedExamQuestionDAO genQuestionDAO = new GeneratedExamQuestionDAO();

    // Hàm chuẩn hóa tên file
    private String sanitizeFileName(String name) {
        if (name == null) return "exam";
        return name.replaceAll("[^a-zA-Z0-9\\-_]", "_")
                .replaceAll("_+", "_")
                .trim();
    }

    // ================================ 1) Export Exam Gốc ================================

    /**
     * Xuất đề thi (Exam) ra file DOCX, bao gồm câu hỏi và từng đáp án (không đánh dấu đáp án đúng).
     *
     * @param examId       ID của Exam trong DB
     * @param outputFolder Thư mục đầu ra
     * @return Đường dẫn file DOCX vừa tạo
     */
    public String exportExamToDocx(int examId, String outputFolder) throws Exception {
        Exam exam = examDAO.getExamById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("Không tìm thấy đề thi với ID: " + examId);
        }
        List<Question> questions = questionDAO.getQuestionsByExamID(examId);

        XWPFDocument doc = new XWPFDocument();

        // Tiêu đề đề thi
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = title.createRun();
        run.setText(exam.getExamName());
        run.setBold(true);
        run.setFontSize(20);
        doc.createParagraph(); // Dòng trống

        int num = 1;
        for (Question q : questions) {
            // Phần câu hỏi
            XWPFParagraph p = doc.createParagraph();
            p.setSpacingBetween(1.2);
            XWPFRun r = p.createRun();
            r.setText(num++ + ". " + q.getContent());
            r.addBreak();

            // Lấy danh sách đáp án (không đánh dấu đúng/sai, chỉ liệt kê)
            List<Answer> answers = answerDAO.getAnswersByQuestionID(q.getQuestionID());
            char optionLabel = 'a';
            for (Answer a : answers) {
                XWPFParagraph ansP = doc.createParagraph();
                ansP.setIndentationLeft(300); // Thụt vào
                XWPFRun ansR = ansP.createRun();
                ansR.setText(optionLabel + ". " + a.getAnswerText());
                optionLabel++;
            }
            doc.createParagraph(); // Dòng trống giữa các câu
        }

        // Tạo thư mục đầu ra nếu chưa tồn tại
        new File(outputFolder).mkdirs();
        String sanitizedName = sanitizeFileName(exam.getExamName());
        String fileName = outputFolder + File.separator + sanitizedName + ".docx";
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            doc.write(out);
        }

        return fileName;
    }

    /**
     * Xuất đáp án của một Exam ra file DOCX (liệt kê câu hỏi + từng đáp án, đánh dấu (Correct) vào đáp án đúng).
     *
     * @param examId       ID của Exam
     * @param outputFolder Thư mục đầu ra
     * @return Đường dẫn file DOCX đáp án
     */
    public String exportExamAnswersToDocx(int examId, String outputFolder) throws Exception {
        Exam exam = examDAO.getExamById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("Không tìm thấy đề thi với ID: " + examId);
        }
        List<Question> questions = questionDAO.getQuestionsByExamID(examId);

        XWPFDocument doc = new XWPFDocument();
        // Tiêu đề Answer Key
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = title.createRun();
        run.setText(exam.getExamName() + " - Answer Key");
        run.setBold(true);
        run.setFontSize(18);
        doc.createParagraph();

        int num = 1;
        for (Question q : questions) {
            XWPFParagraph p = doc.createParagraph();
            p.setSpacingBetween(1.2);
            XWPFRun r = p.createRun();
            r.setText(num++ + ". " + q.getContent());
            r.addBreak();

            List<Answer> answers = answerDAO.getAnswersByQuestionID(q.getQuestionID());
            char optionLabel = 'a';
            for (Answer a : answers) {
                XWPFParagraph ansP = doc.createParagraph();
                ansP.setIndentationLeft(300);
                XWPFRun ansR = ansP.createRun();
                String text = optionLabel + ". " + a.getAnswerText();
                if (a.isCorrect()) {
                    text += " (Correct)";
                }
                ansR.setText(text);
                optionLabel++;
            }
            doc.createParagraph();
        }

        new File(outputFolder).mkdirs();
        String sanitizedName = sanitizeFileName(exam.getExamName());
        String fileName = outputFolder + File.separator + sanitizedName + "_Answers.docx";
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            doc.write(out);
        }

        return fileName;
    }

    /**
     * Xuất đề thi (Exam) ra file PDF, chỉ liệt kê câu hỏi (không liệt kê đáp án).
     *
     * @param examId       ID của Exam
     * @param outputFolder Thư mục đầu ra
     * @return Đường dẫn file PDF vừa tạo
     */
    public String exportExamToPdf(int examId, String outputFolder) throws Exception {
        Exam exam = examDAO.getExamById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("Không tìm thấy đề thi với ID: " + examId);
        }
        List<Question> questions = questionDAO.getQuestionsByExamID(examId);

        new File(outputFolder).mkdirs();
        String sanitizedName = sanitizeFileName(exam.getExamName());
        String fileName = outputFolder + File.separator + sanitizedName + ".pdf";

        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Tiêu đề
        document.add(new Paragraph(exam.getExamName()).setBold().setFontSize(16));
        document.add(new Paragraph(" "));

        int num = 1;
        for (Question q : questions) {
            document.add(new Paragraph(num++ + ". " + q.getContent()));
        }

        document.close();
        return fileName;
    }

    /**
     * Xuất đáp án của Exam ra file PDF (liệt kê câu hỏi + các đáp án, đánh dấu đáp án đúng).
     *
     * @param examId       ID của Exam
     * @param outputFolder Thư mục đầu ra
     * @return Đường dẫn file PDF đáp án
     */
    public String exportExamAnswersToPdf(int examId, String outputFolder) throws Exception {
        Exam exam = examDAO.getExamById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("Không tìm thấy đề thi với ID: " + examId);
        }
        List<Question> questions = questionDAO.getQuestionsByExamID(examId);

        new File(outputFolder).mkdirs();
        String sanitizedName = sanitizeFileName(exam.getExamName());
        String fileName = outputFolder + File.separator + sanitizedName + "_Answers.pdf";

        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Tiêu đề
        document.add(new Paragraph(exam.getExamName() + " - Answer Key").setBold().setFontSize(16));
        document.add(new Paragraph(" "));

        int num = 1;
        for (Question q : questions) {
            document.add(new Paragraph(num++ + ". " + q.getContent()));
            List<Answer> answers = answerDAO.getAnswersByQuestionID(q.getQuestionID());
            char optionLabel = 'a';
            for (Answer a : answers) {
                String text = optionLabel + ". " + a.getAnswerText();
                if (a.isCorrect()) {
                    text += " (Correct)";
                }
                document.add(new Paragraph("   " + text));
                optionLabel++;
            }
            document.add(new Paragraph(" "));
        }

        document.close();
        return fileName;
    }

    // ================================ 2) Export GeneratedExam ================================

    /**
     * Xuất GeneratedExam (đã định nghĩa trước trong DB) ra DOCX, liệt kê các Question theo GeneratedExamID.
     *
     * @param generatedExamId ID của GeneratedExam
     * @param outputFolder    Thư mục đầu ra
     * @return Đường dẫn file DOCX vừa tạo
     */
    public String exportGeneratedExamToDocx(int generatedExamId, String outputFolder) throws Exception {
        GeneratedExam genExam = genExamDAO.getGeneratedExamByID(generatedExamId);
        if (genExam == null) {
            throw new IllegalArgumentException("Không tìm thấy GeneratedExam với ID: " + generatedExamId);
        }
        // Lấy danh sách GeneratedExamQuestion (các questionID)
        List<GeneratedExamQuestion> genQuestions = genQuestionDAO.getByGeneratedExamID(generatedExamId);

        XWPFDocument doc = new XWPFDocument();

        // Tiêu đề GeneratedExam: dùng tên tạm (genExam.getExamName())
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = title.createRun();
        run.setText(genExam.getExamName());
        run.setBold(true);
        run.setFontSize(20);
        doc.createParagraph();

        int num = 1;
        for (GeneratedExamQuestion geq : genQuestions) {
            Question q = questionDAO.getQuestionByID(geq.getQuestionID());
            if (q == null) continue;
            XWPFParagraph p = doc.createParagraph();
            p.setSpacingBetween(1.2);
            XWPFRun r = p.createRun();
            r.setText(num++ + ". " + q.getContent());
            r.addBreak();

            List<Answer> answers = answerDAO.getAnswersByQuestionID(q.getQuestionID());
            char optionLabel = 'a';
            for (Answer a : answers) {
                XWPFParagraph ansP = doc.createParagraph();
                ansP.setIndentationLeft(300);
                XWPFRun ansR = ansP.createRun();
                ansR.setText(optionLabel + ". " + a.getAnswerText());
                optionLabel++;
            }
            doc.createParagraph();
        }

        new File(outputFolder).mkdirs();
        String sanitizedName = sanitizeFileName(genExam.getExamName());
        String fileName = outputFolder + File.separator + sanitizedName + ".docx";
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            doc.write(out);
        }

        // Cập nhật lại exportPath cho GeneratedExam
        genExam.setExportPath(fileName);
        genExamDAO.updateGeneratedExam(genExam);

        return fileName;
    }

    /**
     * Xuất đáp án của GeneratedExam ra DOCX, liệt kê câu hỏi + các đáp án (đánh dấu đáp án đúng nếu có).
     *
     * @param generatedExamId ID của GeneratedExam
     * @param outputFolder    Thư mục đầu ra
     * @return Đường dẫn file DOCX đáp án
     */
    public String exportGeneratedExamAnswersToDocx(int generatedExamId, String outputFolder) throws Exception {
        GeneratedExam genExam = genExamDAO.getGeneratedExamByID(generatedExamId);
        if (genExam == null) {
            throw new IllegalArgumentException("Không tìm thấy GeneratedExam với ID: " + generatedExamId);
        }
        List<GeneratedExamQuestion> genQuestions = genQuestionDAO.getByGeneratedExamID(generatedExamId);

        XWPFDocument doc = new XWPFDocument();

        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = title.createRun();
        run.setText(genExam.getExamName() + " - Answer Key");
        run.setBold(true);
        run.setFontSize(18);
        doc.createParagraph();

        int num = 1;
        for (GeneratedExamQuestion geq : genQuestions) {
            Question q = questionDAO.getQuestionByID(geq.getQuestionID());
            if (q == null) continue;
            XWPFParagraph p = doc.createParagraph();
            p.setSpacingBetween(1.2);
            XWPFRun r = p.createRun();
            r.setText(num++ + ". " + q.getContent());
            r.addBreak();

            List<Answer> answers = answerDAO.getAnswersByQuestionID(q.getQuestionID());
            char optionLabel = 'a';
            for (Answer a : answers) {
                XWPFParagraph ansP = doc.createParagraph();
                ansP.setIndentationLeft(300);
                XWPFRun ansR = ansP.createRun();
                String text = optionLabel + ". " + a.getAnswerText();
                if (a.isCorrect()) {
                    text += " (Correct)";
                }
                ansR.setText(text);
                optionLabel++;
            }
            doc.createParagraph();
        }

        new File(outputFolder).mkdirs();
        String sanitizedName = sanitizeFileName(genExam.getExamName());
        String fileName = outputFolder + File.separator + sanitizedName + "_Answers.docx";
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            doc.write(out);
        }

        genExam.setExportPath(fileName);
        genExamDAO.updateGeneratedExam(genExam);

        return fileName;
    }

    /**
     * Xuất GeneratedExam ra PDF (chỉ list câu hỏi, không liệt kê đáp án).
     */
    public String exportGeneratedExamToPdf(int generatedExamId, String outputFolder) throws Exception {
        GeneratedExam genExam = genExamDAO.getGeneratedExamByID(generatedExamId);
        if (genExam == null) {
            throw new IllegalArgumentException("Không tìm thấy GeneratedExam với ID: " + generatedExamId);
        }
        List<GeneratedExamQuestion> genQuestions = genQuestionDAO.getByGeneratedExamID(generatedExamId);

        new File(outputFolder).mkdirs();
        String sanitizedName = sanitizeFileName(genExam.getExamName());
        String fileName = outputFolder + File.separator + sanitizedName + ".pdf";

        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph(genExam.getExamName()).setBold().setFontSize(16));
        document.add(new Paragraph(" "));

        int num = 1;
        for (GeneratedExamQuestion geq : genQuestions) {
            Question q = questionDAO.getQuestionByID(geq.getQuestionID());
            if (q == null) continue;
            document.add(new Paragraph(num++ + ". " + q.getContent()));
        }

        document.close();
        genExam.setExportPath(fileName);
        genExamDAO.updateGeneratedExam(genExam);
        return fileName;
    }

    /**
     * Xuất đáp án của GeneratedExam ra PDF (list câu hỏi + đáp án, đánh dấu đáp án đúng).
     */
    public String exportGeneratedExamAnswersToPdf(int generatedExamId, String outputFolder) throws Exception {
        GeneratedExam genExam = genExamDAO.getGeneratedExamByID(generatedExamId);
        if (genExam == null) {
            throw new IllegalArgumentException("Không tìm thấy GeneratedExam với ID: " + generatedExamId);
        }
        List<GeneratedExamQuestion> genQuestions = genQuestionDAO.getByGeneratedExamID(generatedExamId);

        new File(outputFolder).mkdirs();
        String sanitizedName = sanitizeFileName(genExam.getExamName());
        String fileName = outputFolder + File.separator + sanitizedName + "_Answers.pdf";

        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph(genExam.getExamName() + " - Answer Key").setBold().setFontSize(16));
        document.add(new Paragraph(" "));

        int num = 1;
        for (GeneratedExamQuestion geq : genQuestions) {
            Question q = questionDAO.getQuestionByID(geq.getQuestionID());
            if (q == null) continue;
            document.add(new Paragraph(num++ + ". " + q.getContent()));
            List<Answer> answers = answerDAO.getAnswersByQuestionID(q.getQuestionID());
            char optionLabel = 'a';
            for (Answer a : answers) {
                String text = optionLabel + ". " + a.getAnswerText();
                if (a.isCorrect()) {
                    text += " (Correct)";
                }
                document.add(new Paragraph("   " + text));
                optionLabel++;
            }
            document.add(new Paragraph(" "));
        }

        document.close();
        genExam.setExportPath(fileName);
        genExamDAO.updateGeneratedExam(genExam);
        return fileName;
    }
}
