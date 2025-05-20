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
import java.util.Objects;
import java.util.stream.Collectors;

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

    private String sanitizeFileName(String name) {
        if (name == null) return "exam";
        return name.replaceAll("[^a-zA-Z0-9\\-_]", "_")
                .replaceAll("_+", "_")
                .trim();
    }

    // ============ Common helpers ============

    private void writeQuestionsDocx(XWPFDocument doc, List<Question> questions) {
        int num = 1;
        for (Question q : questions) {
            XWPFParagraph p = doc.createParagraph();
            p.setSpacingBetween(1.2);
            XWPFRun r = p.createRun();
            r.setText(num++ + ". " + q.getContent());
            r.addBreak();

            char label = 'a';
            List<Answer> answers = answerDAO.getAnswersByQuestionID(q.getQuestionID());
            for (Answer a : answers) {
                XWPFParagraph ansP = doc.createParagraph();
                ansP.setIndentationLeft(300);
                XWPFRun ansR = ansP.createRun();
                ansR.setText(label++ + ". " + a.getAnswerText());
            }
            doc.createParagraph();
        }
    }

    private void writeAnswerKeyDocx(XWPFDocument doc, String titleText, List<Question> questions) {
        // Title already written outside
        int num = 1;
        for (Question q : questions) {
            XWPFParagraph p = doc.createParagraph();
            p.setSpacingBetween(1.2);
            XWPFRun r = p.createRun();
            r.setText(num++ + ". " + q.getContent());
            r.addBreak();

            char label = 'a';
            List<Answer> answers = answerDAO.getAnswersByQuestionID(q.getQuestionID());
            for (Answer a : answers) {
                XWPFParagraph ansP = doc.createParagraph();
                ansP.setIndentationLeft(300);
                XWPFRun ansR = ansP.createRun();
                String text = label++ + ". " + a.getAnswerText();
                if (a.isCorrect()) text += " (Correct)";
                ansR.setText(text);
            }
            doc.createParagraph();
        }
    }

    private void writeQuestionsPdf(Document document, List<Question> questions) {
        int num = 1;
        for (Question q : questions) {
            document.add(new Paragraph(num++ + ". " + q.getContent()));
        }
    }

    private void writeAnswerKeyPdf(Document document, List<Question> questions) {
        int num = 1;
        for (Question q : questions) {
            document.add(new Paragraph(num++ + ". " + q.getContent()));
            char label = 'a';
            for (Answer a : answerDAO.getAnswersByQuestionID(q.getQuestionID())) {
                String text = label++ + ". " + a.getAnswerText();
                if (a.isCorrect()) text += " (Correct)";
                document.add(new Paragraph("   " + text));
            }
            document.add(new Paragraph(" "));
        }
    }

    // ============ Export Exam gốc ============

    public String exportExamToDocx(int examId, String outputFolder) throws Exception {
        Exam exam = examDAO.getExamById(examId);
        if (exam == null) throw new IllegalArgumentException("Không tìm thấy Exam: " + examId);
        List<Question> questions = questionDAO.getQuestionsByExamID(examId);

        XWPFDocument doc = new XWPFDocument();
        // Title
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = title.createRun();
        run.setText(exam.getExamName()); run.setBold(true); run.setFontSize(20);
        doc.createParagraph();

        writeQuestionsDocx(doc, questions);
        // Save
        new File(outputFolder).mkdirs();
        String name = sanitizeFileName(exam.getExamName());
        String file = outputFolder + File.separator + name + ".docx";
        try (FileOutputStream out = new FileOutputStream(file)) { doc.write(out); }
        return file;
    }

    public String exportExamAnswersToDocx(int examId, String outputFolder) throws Exception {
        Exam exam = examDAO.getExamById(examId);
        if (exam == null) throw new IllegalArgumentException("Không tìm thấy Exam: " + examId);
        List<Question> questions = questionDAO.getQuestionsByExamID(examId);

        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = title.createRun();
        run.setText(exam.getExamName() + " - Answer Key"); run.setBold(true); run.setFontSize(18);
        doc.createParagraph();

        writeAnswerKeyDocx(doc, exam.getExamName(), questions);
        new File(outputFolder).mkdirs();
        String name = sanitizeFileName(exam.getExamName());
        String file = outputFolder + File.separator + name + "_Answers.docx";
        try (FileOutputStream out = new FileOutputStream(file)) { doc.write(out); }
        return file;
    }

    public String exportExamToPdf(int examId, String outputFolder) throws Exception {
        Exam exam = examDAO.getExamById(examId);
        if (exam == null) throw new IllegalArgumentException("Không tìm thấy Exam: " + examId);
        List<Question> questions = questionDAO.getQuestionsByExamID(examId);

        new File(outputFolder).mkdirs();
        String name = sanitizeFileName(exam.getExamName());
        String file = outputFolder + File.separator + name + ".pdf";

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        doc.add(new Paragraph(exam.getExamName()).setBold().setFontSize(16));
        doc.add(new Paragraph(" "));
        writeQuestionsPdf(doc, questions);
        doc.close();
        return file;
    }

    public String exportExamAnswersToPdf(int examId, String outputFolder) throws Exception {
        Exam exam = examDAO.getExamById(examId);
        if (exam == null) throw new IllegalArgumentException("Không tìm thấy Exam: " + examId);
        List<Question> questions = questionDAO.getQuestionsByExamID(examId);

        new File(outputFolder).mkdirs();
        String name = sanitizeFileName(exam.getExamName());
        String file = outputFolder + File.separator + name + "_Answers.pdf";

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        doc.add(new Paragraph(exam.getExamName() + " - Answer Key").setBold().setFontSize(16));
        doc.add(new Paragraph(" "));
        writeAnswerKeyPdf(doc, questions);
        doc.close();
        return file;
    }

    // ============ Export GeneratedExam ============

    public String exportGeneratedExamToDocx(int genId, String outputFolder) throws Exception {
        GeneratedExam ge = genExamDAO.getGeneratedExamByID(genId);
        if (ge == null) throw new IllegalArgumentException("Không tìm thấy GeneratedExam: " + genId);
        List<Question> questions = genQuestionDAO.getByGeneratedExamID(genId).stream()
                .map(gq -> questionDAO.getQuestionByID(gq.getQuestionID()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = title.createRun();
        run.setText(ge.getExamName()); run.setBold(true); run.setFontSize(20);
        doc.createParagraph();

        writeQuestionsDocx(doc, questions);
        new File(outputFolder).mkdirs();
        String name = sanitizeFileName(ge.getExamName());
        String file = outputFolder + File.separator + name + ".docx";
        try (FileOutputStream out = new FileOutputStream(file)) { doc.write(out); }
        ge.setExportPath(file);
        genExamDAO.updateGeneratedExam(ge);
        return file;
    }

    public String exportGeneratedExamAnswersToDocx(int genId, String outputFolder) throws Exception {
        GeneratedExam ge = genExamDAO.getGeneratedExamByID(genId);
        if (ge == null) throw new IllegalArgumentException("Không tìm thấy GeneratedExam: " + genId);
        List<Question> questions = genQuestionDAO.getByGeneratedExamID(genId).stream()
                .map(gq -> questionDAO.getQuestionByID(gq.getQuestionID()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = title.createRun();
        run.setText(ge.getExamName() + " - Answer Key"); run.setBold(true); run.setFontSize(18);
        doc.createParagraph();

        writeAnswerKeyDocx(doc, ge.getExamName(), questions);
        new File(outputFolder).mkdirs();
        String name = sanitizeFileName(ge.getExamName());
        String file = outputFolder + File.separator + name + "_Answers.docx";
        try (FileOutputStream out = new FileOutputStream(file)) { doc.write(out); }
        ge.setExportPath(file);
        genExamDAO.updateGeneratedExam(ge);
        return file;
    }

    public String exportGeneratedExamToPdf(int genId, String outputFolder) throws Exception {
        GeneratedExam ge = genExamDAO.getGeneratedExamByID(genId);
        if (ge == null) throw new IllegalArgumentException("Không tìm thấy GeneratedExam: " + genId);
        List<Question> questions = genQuestionDAO.getByGeneratedExamID(genId).stream()
                .map(gq -> questionDAO.getQuestionByID(gq.getQuestionID()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        new File(outputFolder).mkdirs();
        String name = sanitizeFileName(ge.getExamName());
        String file = outputFolder + File.separator + name + ".pdf";
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        doc.add(new Paragraph(ge.getExamName()).setBold().setFontSize(16));
        doc.add(new Paragraph(" "));
        writeQuestionsPdf(doc, questions);
        doc.close();
        ge.setExportPath(file);
        genExamDAO.updateGeneratedExam(ge);
        return file;
    }

    public String exportGeneratedExamAnswersToPdf(int genId, String outputFolder) throws Exception {
        GeneratedExam ge = genExamDAO.getGeneratedExamByID(genId);
        if (ge == null) throw new IllegalArgumentException("Không tìm thấy GeneratedExam: " + genId);
        List<Question> questions = genQuestionDAO.getByGeneratedExamID(genId).stream()
                .map(gq -> questionDAO.getQuestionByID(gq.getQuestionID()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        new File(outputFolder).mkdirs();
        String name = sanitizeFileName(ge.getExamName());
        String file = outputFolder + File.separator + name + "_Answers.pdf";
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        doc.add(new Paragraph(ge.getExamName() + " - Answer Key").setBold().setFontSize(16));
        doc.add(new Paragraph(" "));
        writeAnswerKeyPdf(doc, questions);
        doc.close();
        ge.setExportPath(file);
        genExamDAO.updateGeneratedExam(ge);
        return file;
    }
}
