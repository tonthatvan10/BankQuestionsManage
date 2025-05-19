//package BankQuestionManagement.Controller;
//
//import BankQuestionManagement.DAO.ExamDAO;
//import BankQuestionManagement.DAO.QuestionDAO;
//import BankQuestionManagement.Model.Exam;
//import BankQuestionManagement.Model.Question;
//import BankQuestionManagement.Model.GeneratedExam;
//import BankQuestionManagement.Service.GeminiService;
//import BankQuestionManagement.Service.GeneratedExamService;
//import BankQuestionManagement.Util.DocumentExporter;
//
//import javax.swing.*;
//import javax.swing.filechooser.FileNameExtensionFilter;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.StandardCopyOption;
//import java.util.List;
//
//public class MainUI extends JFrame {
//    private final JComboBox<Exam> cmbExams;
//    private final JTextArea txtContent;
//    private final JTextField txtImagePath;
//    private final JButton btnBrowseImage;
//    private final JTextField txtAudioPath;
//    private final JButton btnBrowseAudio;
//    private final JButton btnSave;
//    private final JButton btnManageAnswers;
//    private final JButton btnScanAndSave;
//    private final JButton btnGenerateExam;
//    private final JButton btnExport;
//    private final JTable tblQuestions;
//    private final DefaultTableModel tableModel;
//    private final QuestionDAO questionDAO;
//    private final DocumentExporter exporter = new DocumentExporter();
//    private final GeminiService geminiService = new GeminiService();
//    private final GeneratedExamService genExamService = new GeneratedExamService();
//
//    public MainUI() {
//        super("Bank Question Management");
//        setDefaultCloseOperation(EXIT_ON_CLOSE);
//        setSize(1000, 700);
//        setLocationRelativeTo(null);
//
//        ExamDAO examDAO = new ExamDAO();
//        List<Exam> exams = examDAO.getAllExams();
//        cmbExams = new JComboBox<>(exams.toArray(new Exam[0]));
//
//        questionDAO = new QuestionDAO();
//
//        txtContent = new JTextArea(5, 30);
//        txtImagePath = new JTextField(20);
//        txtImagePath.setEditable(false);
//        btnBrowseImage = new JButton("Browse Image");
//        txtAudioPath = new JTextField(20);
//        txtAudioPath.setEditable(false);
//        btnBrowseAudio = new JButton("Browse Audio");
//        btnSave = new JButton("Save Question");
//        btnManageAnswers = new JButton("Manage Answers");
//        btnScanAndSave = new JButton("Scan & Save QA");
//        btnGenerateExam = new JButton("Generate Exam");
//        btnExport = new JButton("Export...");
//
//        tableModel = new DefaultTableModel(new Object[]{"ID", "Content", "Image Path", "Audio Path"}, 0);
//        tblQuestions = new JTable(tableModel);
//
//        layoutComponents();
//        attachListeners();
//        loadQuestions();
//    }
//
//    private void layoutComponents() {
//        JPanel pnlInput = new JPanel(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(5, 5, 5, 5);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        int row = 0;
//        gbc.gridx = 0; gbc.gridy = row;
//        pnlInput.add(new JLabel("Select Exam:"), gbc);
//        gbc.gridx = 1;
//        pnlInput.add(cmbExams, gbc);
//
//        row++;
//        gbc.gridx = 0; gbc.gridy = row;
//        pnlInput.add(new JLabel("Question Content:"), gbc);
//        gbc.gridx = 1;
//        pnlInput.add(new JScrollPane(txtContent), gbc);
//
//        row++;
//        gbc.gridx = 0; gbc.gridy = row;
//        pnlInput.add(new JLabel("Image File:"), gbc);
//        gbc.gridx = 1;
//        JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        imgPanel.add(txtImagePath);
//        imgPanel.add(btnBrowseImage);
//        pnlInput.add(imgPanel, gbc);
//
//        row++;
//        gbc.gridx = 0; gbc.gridy = row;
//        pnlInput.add(new JLabel("Audio File:"), gbc);
//        gbc.gridx = 1;
//        JPanel audioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        audioPanel.add(txtAudioPath);
//        audioPanel.add(btnBrowseAudio);
//        pnlInput.add(audioPanel, gbc);
//
//        row++;
//        gbc.gridx = 1; gbc.gridy = row;
//        pnlInput.add(btnSave, gbc);
//
//        row++;
//        gbc.gridy = row;
//        pnlInput.add(btnManageAnswers, gbc);
//
//        row++;
//        gbc.gridy = row;
//        pnlInput.add(btnScanAndSave, gbc);
//
//        row++;
//        gbc.gridy = row;
//        pnlInput.add(btnGenerateExam, gbc);
//
//        row++;
//        gbc.gridy = row;
//        pnlInput.add(btnExport, gbc);
//
//        add(pnlInput, BorderLayout.WEST);
//        add(new JScrollPane(tblQuestions), BorderLayout.CENTER);
//    }
//
//    private void attachListeners() {
//        btnBrowseImage.addActionListener(evt -> selectFile(txtImagePath, "Image Files", "jpg", "png", "jpeg", "gif"));
//        btnBrowseAudio.addActionListener(evt -> selectFile(txtAudioPath, "Audio Files", "mp3", "wav"));
//        btnSave.addActionListener(evt -> saveQuestion());
//        btnManageAnswers.addActionListener(evt -> manageAnswers());
//        btnScanAndSave.addActionListener(evt -> scanAndSaveQA());
//        btnGenerateExam.addActionListener(evt -> generateExam());
//        btnExport.addActionListener(evt -> showExportOptions());
//    }
//
//    private void selectFile(JTextField target, String desc, String... exts) {
//        JFileChooser chooser = new JFileChooser();
//        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        chooser.setFileFilter(new FileNameExtensionFilter(desc, exts));
//        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
//            target.setText(chooser.getSelectedFile().getAbsolutePath());
//        }
//    }
//
//    private void manageAnswers() {
//        int row = tblQuestions.getSelectedRow();
//        if (row < 0) {
//            JOptionPane.showMessageDialog(this, "Please select a question first.");
//            return;
//        }
//        int qid = (Integer) tableModel.getValueAt(row, 0);
//        new AnswerManagementDialog(this, questionDAO.getQuestionByID(qid)).setVisible(true);
//    }
//
//    private void scanAndSaveQA() {
//        Exam selected = (Exam) cmbExams.getSelectedItem();
//        if (selected == null) {
//            JOptionPane.showMessageDialog(this, "Please select an exam first.");
//            return;
//        }
//        JFileChooser chooser = new JFileChooser();
//        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg", "gif"));
//        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
//            File img = chooser.getSelectedFile();
//            try {
//                List<Question> saved = geminiService.scanAndSave(img, selected.getExamID());
//                JOptionPane.showMessageDialog(this, "Saved " + saved.size() + " questions from scan.");
//                loadQuestions();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                JOptionPane.showMessageDialog(this, "Error scanning image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//    }
//
//    private void generateExam() {
//        Exam selected = (Exam) cmbExams.getSelectedItem();
//        if (selected == null) {
//            JOptionPane.showMessageDialog(this, "Please select an exam first.");
//            return;
//        }
//        String cntStr = JOptionPane.showInputDialog(this, "Enter number of questions to generate:");
//        if (cntStr == null) return;
//        try {
//            int cnt = Integer.parseInt(cntStr.trim());
//            GeneratedExam ge = genExamService.generateRandomExam(selected.getExamID(), cnt);
//            JOptionPane.showMessageDialog(this,
//                    "Generated Exam ID=" + ge.getGeneratedExamID() + ", count=" + cnt
//            );
//        } catch (NumberFormatException nfe) {
//            JOptionPane.showMessageDialog(this, "Invalid number.", "Error", JOptionPane.ERROR_MESSAGE);
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    private void showExportOptions() {
//        String[] types = {"Exam", "Answer Key"};
//        String type = (String) JOptionPane.showInputDialog(this,
//                "Choose output:", "Export",
//                JOptionPane.PLAIN_MESSAGE, null,
//                types, types[0]);
//        if (type == null) return;
//
//        String[] formats = {"DOCX", "PDF"};
//        String fmt = (String) JOptionPane.showInputDialog(this,
//                "Choose format:", "Export Format",
//                JOptionPane.PLAIN_MESSAGE, null,
//                formats, formats[0]);
//        if (fmt == null) return;
//
//        Exam selected = (Exam) cmbExams.getSelectedItem();
//        JFileChooser chooser = new JFileChooser();
//        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
//            String dir = chooser.getSelectedFile().getAbsolutePath();
//            try {
//                String path;
//                boolean pdf = fmt.equals("PDF");
//                boolean answers = type.equals("Answer Key");
//                if (answers) {
//                    path = pdf
//                            ? exporter.exportAnswersToPdf(selected.getExamID(), dir)
//                            : exporter.exportAnswersToDocx(selected.getExamID(), dir);
//                } else {
//                    path = pdf
//                            ? exporter.exportToPdf(selected.getExamID(), dir)
//                            : exporter.exportToDocx(selected.getExamID(), dir);
//                }
//                JOptionPane.showMessageDialog(this, "Export successful:\n" + path);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//    }
//
//    private void saveQuestion() {
//        Exam selectedExam = (Exam) cmbExams.getSelectedItem();
//        if (selectedExam == null) {
//            JOptionPane.showMessageDialog(this, "Please select an exam first.", "Warning", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//        String content = txtContent.getText().trim();
//        if (content.isEmpty()) {
//            JOptionPane.showMessageDialog(this, "Content cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//        String imageSource = txtImagePath.getText().trim();
//        String audioSource = txtAudioPath.getText().trim();
//        Question q = new Question();
//        q.setExamID(selectedExam.getExamID());
//        q.setContent(content);
//        q.setImagePath(imageSource.isEmpty() ? null : imageSource);
//        q.setAudioPath(null);
//        int newId = questionDAO.addQuestion(q);
//        if (newId <= 0) {
//            JOptionPane.showMessageDialog(this, "Error saving question.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//        if (!audioSource.isEmpty()) {
//            try {
//                File src = new File(audioSource);
//                String ext = audioSource.substring(audioSource.lastIndexOf('.'));
//                File dir = new File("audio" + File.separator + selectedExam.getExamID());
//                dir.mkdirs();
//                File dest = new File(dir, newId + ext);
//                Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
//                q.setQuestionID(newId);
//                q.setAudioPath(dest.getPath());
//                questionDAO.updateQuestion(q);
//            } catch (IOException ex) {
//                ex.printStackTrace();
//                JOptionPane.showMessageDialog(this, "Error saving audio file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//        JOptionPane.showMessageDialog(this, "Saved successfully. ID: " + newId);
//        clearInput();
//        loadQuestions();
//    }
//
//    private void loadQuestions() {
//        tableModel.setRowCount(0);
//        List<Question> questions = questionDAO.getAllQuestions();
//        for (Question q : questions) {
//            tableModel.addRow(new Object[]{q.getQuestionID(), q.getContent(), q.getImagePath(), q.getAudioPath()});
//        }
//    }
//
//    private void clearInput() {
//        txtContent.setText("");
//        txtImagePath.setText("");
//        txtAudioPath.setText("");
//    }
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new MainUI().setVisible(true));
//    }
//}
