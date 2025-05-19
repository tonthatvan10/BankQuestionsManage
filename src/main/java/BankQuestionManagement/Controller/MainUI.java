package BankQuestionManagement.Controller;

import BankQuestionManagement.DAO.ExamDAO;
import BankQuestionManagement.DAO.QuestionDAO;
import BankQuestionManagement.Model.Exam;
import BankQuestionManagement.Model.Question;
import BankQuestionManagement.Util.DocumentExporter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class MainUI extends JFrame {
    private final JComboBox<Exam> cmbExams;
    private final JTextArea txtContent;
    private final JTextField txtImagePath;
    private final JButton btnBrowseImage;
    private final JTextField txtAudioPath;
    private final JButton btnBrowseAudio;
    private final JButton btnSave;
    private final JButton btnManageAnswers;
    private final JButton btnExportDocx;
    private final JButton btnExportPdf;
    private final JButton btnExportAnswersDocx;
    private final JButton btnExportAnswersPdf;
    private final JTable tblQuestions;
    private final DefaultTableModel tableModel;
    private final QuestionDAO questionDAO;
    private final DocumentExporter exporter = new DocumentExporter();

    public MainUI() {
        super("Bank Question Management");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        ExamDAO examDAO = new ExamDAO();
        List<Exam> exams = examDAO.getAllExams();
        cmbExams = new JComboBox<>(exams.toArray(new Exam[0]));

        try {
            questionDAO = new QuestionDAO();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to database: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(ex);
        }

        txtContent = new JTextArea(5, 30);
        txtImagePath = new JTextField(20);
        txtImagePath.setEditable(false);
        btnBrowseImage = new JButton("Browse Image");
        txtAudioPath = new JTextField(20);
        txtAudioPath.setEditable(false);
        btnBrowseAudio = new JButton("Browse Audio");
        btnSave = new JButton("Save Question");
        btnManageAnswers = new JButton("Manage Answers");
        btnExportDocx = new JButton("Export Exam DOCX");
        btnExportPdf = new JButton("Export Exam PDF");
        btnExportAnswersDocx = new JButton("Export Answers DOCX");
        btnExportAnswersPdf = new JButton("Export Answers PDF");

        tableModel = new DefaultTableModel(new Object[]{"ID", "Content", "Image Path", "Audio Path"}, 0);
        tblQuestions = new JTable(tableModel);

        layoutComponents();
        attachListeners();
        loadQuestions();
    }

    private void layoutComponents() {
        JPanel pnlInput = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        pnlInput.add(new JLabel("Select Exam:"), gbc);
        gbc.gridx = 1;
        pnlInput.add(cmbExams, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        pnlInput.add(new JLabel("Question Content:"), gbc);
        gbc.gridx = 1;
        pnlInput.add(new JScrollPane(txtContent), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        pnlInput.add(new JLabel("Image File:"), gbc);
        gbc.gridx = 1;
        JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        imgPanel.add(txtImagePath);
        imgPanel.add(btnBrowseImage);
        pnlInput.add(imgPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        pnlInput.add(new JLabel("Audio File:"), gbc);
        gbc.gridx = 1;
        JPanel audioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        audioPanel.add(txtAudioPath);
        audioPanel.add(btnBrowseAudio);
        pnlInput.add(audioPanel, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        pnlInput.add(btnSave, gbc);

        gbc.gridy = 5;
        pnlInput.add(btnManageAnswers, gbc);

        gbc.gridy = 6;
        pnlInput.add(btnExportDocx, gbc);
        gbc.gridy = 7;
        pnlInput.add(btnExportPdf, gbc);
        gbc.gridy = 8;
        pnlInput.add(btnExportAnswersDocx, gbc);
        gbc.gridy = 9;
        pnlInput.add(btnExportAnswersPdf, gbc);

        add(pnlInput, BorderLayout.WEST);
        add(new JScrollPane(tblQuestions), BorderLayout.CENTER);
    }

    private void attachListeners() {
        btnBrowseImage.addActionListener(evt -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg", "gif"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtImagePath.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        btnBrowseAudio.addActionListener(evt -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter("Audio Files", "mp3", "wav"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtAudioPath.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        btnSave.addActionListener(evt -> saveQuestion());

        btnManageAnswers.addActionListener(evt -> {
            int row = tblQuestions.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a question first."); return;
            }
            int qid = (Integer) tableModel.getValueAt(row, 0);
            Question q = questionDAO.getQuestionByID(qid);
            new AnswerManagementDialog(this, q).setVisible(true);
        });

        btnExportDocx.addActionListener(evt -> exportAction(false, false));
        btnExportPdf.addActionListener(evt -> exportAction(true, false));
        btnExportAnswersDocx.addActionListener(evt -> exportAction(false, true));
        btnExportAnswersPdf.addActionListener(evt -> exportAction(true, true));
    }

    private void exportAction(boolean pdf, boolean answers) {
        Exam selected = (Exam) cmbExams.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an exam first."); return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String dir = chooser.getSelectedFile().getAbsolutePath();
            try {
                String path;
                if (answers) {
                    if (pdf) path = exporter.exportAnswersToPdf(selected.getExamID(), dir);
                    else     path = exporter.exportAnswersToDocx(selected.getExamID(), dir);
                } else {
                    if (pdf) path = exporter.exportToPdf(selected.getExamID(), dir);
                    else     path = exporter.exportToDocx(selected.getExamID(), dir);
                }
                JOptionPane.showMessageDialog(this, "Export thành công:\n" + path);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Export lỗi: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveQuestion() {
        Exam selectedExam = (Exam) cmbExams.getSelectedItem();
        if (selectedExam == null) {
            JOptionPane.showMessageDialog(this, "Please select an exam first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String content = txtContent.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Content cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String imageSource = txtImagePath.getText().trim();
        String audioSource = txtAudioPath.getText().trim();
        Question q = new Question();
        q.setExamID(selectedExam.getExamID());
        q.setContent(content);
        q.setImagePath(imageSource.isEmpty() ? null : imageSource);
        q.setAudioPath(null);
        int newId = questionDAO.addQuestion(q);
        if (newId <= 0) {
            JOptionPane.showMessageDialog(this, "Error saving question.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!audioSource.isEmpty()) {
            try {
                File src = new File(audioSource);
                String ext = audioSource.substring(audioSource.lastIndexOf('.'));
                File dir = new File("audio" + File.separator + selectedExam.getExamID());
                dir.mkdirs();
                File dest = new File(dir, newId + ext);
                Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                q.setQuestionID(newId);
                q.setAudioPath(dest.getPath());
                questionDAO.updateQuestion(q);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving audio file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        JOptionPane.showMessageDialog(this, "Saved successfully. ID: " + newId);
        clearInput();
        loadQuestions();
    }

    private void loadQuestions() {
        tableModel.setRowCount(0);
        List<Question> questions = questionDAO.getAllQuestions();
        for (Question q : questions) {
            tableModel.addRow(new Object[]{q.getQuestionID(), q.getContent(), q.getImagePath(), q.getAudioPath()});
        }
    }

    private void clearInput() {
        txtContent.setText("");
        txtImagePath.setText("");
        txtAudioPath.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainUI().setVisible(true));
    }
}
