package BankQuestionManagement.Controller;

import BankQuestionManagement.DAO.ExamDAO;
import BankQuestionManagement.DAO.QuestionDAO;
import BankQuestionManagement.Model.Exam;
import BankQuestionManagement.Model.Question;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MainUI extends JFrame {
    private final JComboBox<Exam> cmbExams;
    private final JTextArea txtContent;
    private final JTextField txtImagePath;
    private final JButton btnBrowseImage;
    private final JButton btnSave;
    private final JTable tblQuestions;
    private final DefaultTableModel tableModel;
    private final QuestionDAO questionDAO;

    public MainUI() {
        super("Bank Question Management");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Load Exams for combo
        ExamDAO examDAO = new ExamDAO();
        List<Exam> exams = examDAO.getAllExams();
        cmbExams = new JComboBox<>(exams.toArray(new Exam[0]));


        // Initialize QuestionDAO
        try {
            questionDAO = new QuestionDAO();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to database: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(ex);
        }

        // Initialize UI components
        txtContent = new JTextArea(5, 30);
        txtImagePath = new JTextField(20);
        txtImagePath.setEditable(false);
        btnBrowseImage = new JButton("Browse Image");
        btnSave = new JButton("Save Question");

        tableModel = new DefaultTableModel(new Object[]{"ID", "Content", "Image Path"}, 0);
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

        // Exam selection
        gbc.gridx = 0; gbc.gridy = 0;
        pnlInput.add(new JLabel("Select Exam:"), gbc);
        gbc.gridx = 1;
        pnlInput.add(cmbExams, gbc);

        // Question content
        gbc.gridx = 0; gbc.gridy = 1;
        pnlInput.add(new JLabel("Question Content:"), gbc);
        gbc.gridx = 1;
        pnlInput.add(new JScrollPane(txtContent), gbc);

        // Image file
        gbc.gridx = 0; gbc.gridy = 2;
        pnlInput.add(new JLabel("Image File:"), gbc);
        gbc.gridx = 1;
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.add(txtImagePath);
        filePanel.add(btnBrowseImage);
        pnlInput.add(filePanel, gbc);

        // Save button
        gbc.gridx = 1; gbc.gridy = 3;
        pnlInput.add(btnSave, gbc);

        add(pnlInput, BorderLayout.NORTH);
        add(new JScrollPane(tblQuestions), BorderLayout.CENTER);
    }

    private void attachListeners() {
        btnBrowseImage.addActionListener(evt -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                txtImagePath.setText(file.getAbsolutePath());
            }
        });

        btnSave.addActionListener(evt -> saveQuestion());
    }

    private void saveQuestion() {
        Exam selectedExam = (Exam) cmbExams.getSelectedItem();
        if (selectedExam == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an exam first.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String content = txtContent.getText().trim();
        String path = txtImagePath.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Content cannot be empty.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Question q = new Question();
        q.setExamID(selectedExam.getExamID());
        q.setContent(content);
        q.setImagePath(path.isEmpty() ? null : path);

        int newId = questionDAO.addQuestion(q);
        if (newId > 0) {
            JOptionPane.showMessageDialog(this, "Saved successfully. ID: " + newId);
            clearInput();
            loadQuestions();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error saving question.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadQuestions() {
        List<Question> questions = questionDAO.getAllQuestions();
        tableModel.setRowCount(0);
        for (Question q : questions) {
            tableModel.addRow(new Object[]{
                    q.getQuestionID(), q.getContent(), q.getImagePath()
            });
        }
    }

    private void clearInput() {
        txtContent.setText("");
        txtImagePath.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainUI().setVisible(true));
    }
}
