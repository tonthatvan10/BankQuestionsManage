package BankQuestionManagement.Util;

import BankQuestionManagement.DAO.AnswerDAO;
import BankQuestionManagement.DAO.ExamDAO;
import BankQuestionManagement.DAO.GeneratedExamDAO;
import BankQuestionManagement.DAO.GeneratedExamQuestionDAO;
import BankQuestionManagement.DAO.QuestionDAO;
import BankQuestionManagement.Model.Answer;
import BankQuestionManagement.Model.Exam;
import BankQuestionManagement.Model.GeneratedExam;
import BankQuestionManagement.Model.Question;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
//import com.itextpdf.layout.property.TextAlignment;
//import com.itextpdf.layout.property.UnitValue;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Utility class để xuất đề thi và file đáp án.
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
        // Thay ký tự không hợp lệ bằng dấu gạch dưới
        return name.replaceAll("[^a-zA-Z0-9\\-_]", "_").replaceAll("_+", "_").trim();
    }

    /**
     * Xuất đề thi ra file DOCX và lưu metadata
     */
    public String exportToDocx(int examId, String outputFolder) throws Exception {
        Exam exam = examDAO.getExamByID(examId);
        if (exam == null) {
            throw new IllegalArgumentException("Không tìm thấy đề thi với ID: " + examId);
        }
        List<Question> questions = questionDAO.getQuestionsByExamID(examId);

        XWPFDocument doc = new XWPFDocument();
        // Tiêu đề
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = title.createRun();
        run.setText(exam.getExamName());
        run.setBold(true);
        run.setFontSize(20);
        doc.createParagraph();

        int num = 1;
        for (Question q : questions) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun r = p.createRun();
            r.setText(num++ + ". " + q.getContent());
            r.addBreak();

            // Xử lý hình ảnh
            if (q.getImagePath() != null && !q.getImagePath().isEmpty()) {
                File imgFile = new File(q.getImagePath());
                if (imgFile.exists()) {
                    try (FileInputStream is = new FileInputStream(imgFile)) {
                        r.addPicture(is, XWPFDocument.PICTURE_TYPE_JPEG,
                                imgFile.getName(), Units.toEMU(200), Units.toEMU(150));
                    } catch (Exception e) {
                        System.err.println("Lỗi khi thêm hình ảnh vào DOCX: " + e.getMessage());
                    }
                }
            }
        }

        // Tạo thư mục đầu ra
        new File(outputFolder).mkdirs();
        String sanitizedName = sanitizeFileName(exam.getExamName());
        String fileName = outputFolder + File.separator + sanitizedName + ".docx";
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            doc.write(out);
        }

        int genExamId = genExamDAO.addGeneratedExam(
                new GeneratedExam(exam.getExamName(), fileName)
        );
        if (genExamId > 0) {
            for (Question q : questions) {
                genQuestionDAO.addGeneratedExamQuestion(genExamId, q.getQuestionID());
            }
        }
        return fileName;
    }

    /**
     * Xuất đáp án ra file DOCX và lưu metadata
     */
    public String exportAnswersToDocx(int examId, String outputFolder) throws Exception {
        Exam exam = examDAO.getExamByID(examId);
        if (exam == null) {
            throw new IllegalArgumentException("Không tìm thấy đề thi với ID: " + examId);
        }
        List<Question> questions = questionDAO.getQuestionsByExamID(examId);

        XWPFDocument doc = new XWPFDocument();
        // Tiêu đề đáp án
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
            XWPFRun r = p.createRun();
            r.setText(num++ + ". " + q.getContent());
            r.addBreak();
            List<Answer> answers = answerDAO.getAnswersByQuestionID(q.getQuestionID());
            for (Answer a : answers) {
                XWPFParagraph ansP = doc.createParagraph();
                XWPFRun ansR = ansP.createRun();
                ansR.setText("- " + a.getAnswerText() + (a.isCorrect() ? " (Correct)" : ""));
            }
        }

        // Tạo thư mục đầu ra
        new File(outputFolder).mkdirs();
        String sanitizedName = sanitizeFileName(exam.getExamName());
        String fileName = outputFolder + File.separator + sanitizedName + "_Answers.docx";
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            doc.write(out);
        }

        int genExamId = genExamDAO.addGeneratedExam(
                new GeneratedExam(exam.getExamName() + "_Answers", fileName)
        );
        return fileName;
    }

    /**
     * Xuất đề thi ra file PDF và lưu metadata
     */
    public String exportToPdf(int examId, String outputFolder) throws Exception {
        Exam exam = examDAO.getExamByID(examId);
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
        document.add(new Paragraph(" ")); // dòng trắng

        int num = 1;
        for (Question q : questions) {
            document.add(new Paragraph(num++ + ". " + q.getContent()));
        }

        document.close();

        int genExamId = genExamDAO.addGeneratedExam(
                new GeneratedExam(exam.getExamName(), fileName)
        );
        if (genExamId > 0) {
            for (Question q : questions) {
                genQuestionDAO.addGeneratedExamQuestion(genExamId, q.getQuestionID());
            }
        }
        return fileName;
    }


    /**
     * Xuất đáp án ra file PDF và lưu metadata
     */
    public String exportAnswersToPdf(int examId, String outputFolder) throws Exception {
        Exam exam = examDAO.getExamByID(examId);
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
            for (Answer a : answers) {
                document.add(new Paragraph("- " + a.getAnswerText() + (a.isCorrect() ? " (Correct)" : "")));
            }
            document.add(new Paragraph(" "));
        }

        document.close();

        genExamDAO.addGeneratedExam(
                new GeneratedExam(exam.getExamName() + "_Answers", fileName)
        );
        return fileName;
    }
}