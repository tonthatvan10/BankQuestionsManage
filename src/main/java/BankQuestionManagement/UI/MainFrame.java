package BankQuestionManagement.UI;

import BankQuestionManagement.DAO.ExamDAO;
import BankQuestionManagement.DAO.QuestionDAO;
import BankQuestionManagement.DAO.AnswerDAO;
import BankQuestionManagement.DAO.GeneratedExamDAO;
import BankQuestionManagement.DAO.GeneratedExamQuestionDAO;
import BankQuestionManagement.Model.Exam;
import BankQuestionManagement.Model.Question;
import BankQuestionManagement.Model.Answer;
import BankQuestionManagement.Model.GeneratedExam;
import BankQuestionManagement.Model.GeneratedExamQuestion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * MainFrame: Khung chính của ứng dụng GUI quản lý ngân hàng đề thi.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        initialize();
    }

    /**
     * Thiết lập cấu hình cơ bản cho JFrame và thêm NavigationPane + ContentPane
     */
    private void initialize() {
        setTitle("Bank Question Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        ContentPanel contentPanel = new ContentPanel();
        NavigationPanel navigationPanel = new NavigationPanel(contentPanel);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                navigationPanel,
                contentPanel
        );
        splitPane.setDividerLocation(200);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }

    /**
     * Panel điều hướng (Navigation) với các nút chuyển giữa các module
     */
    static class NavigationPanel extends JPanel {
        public NavigationPanel(ContentPanel contentPanel) {
            setLayout(new GridLayout(2, 1, 5, 5));
            JToggleButton examsBtn = new JToggleButton("Exams");
            JToggleButton genExamsBtn = new JToggleButton("Generated Exams");
            ButtonGroup group = new ButtonGroup();
            group.add(examsBtn);
            group.add(genExamsBtn);
            examsBtn.setSelected(true);

            examsBtn.addActionListener(e -> contentPanel.showCard("Exams"));
            genExamsBtn.addActionListener(e -> contentPanel.showCard("GeneratedExams"));

            add(examsBtn);
            add(genExamsBtn);
        }
    }

    /**
     * Panel nội dung chính dùng CardLayout để chứa các module
     */
    static class ContentPanel extends JPanel {
        private final CardLayout cardLayout = new CardLayout();

        public ContentPanel() {
            setLayout(cardLayout);
            add(new ExamManagementPanel(), "Exams");
            add(new GeneratedExamPanel(), "GeneratedExams");
            cardLayout.show(this, "Exams");
        }

        public void showCard(String name) {
            cardLayout.show(this, name);
        }
    }

    /**
     * Panel quản lý Exams: toolbar + split panel
     */
    static class ExamManagementPanel extends JPanel {
        private final ExamDAO examDAO = new ExamDAO();
        private final QuestionDAO questionDAO = new QuestionDAO();
        private final AnswerDAO answerDAO = new AnswerDAO();

        private JTable examsTable;
        private DefaultTableModel examsModel;
        private JTable questionsTable;
        private DefaultTableModel questionsModel;
        private JTable answersTable;
        private DefaultTableModel answersModel;

        public ExamManagementPanel() {
            setLayout(new BorderLayout());
            initToolbar();
            initMainSplit();
            loadExams();
            registerExamSelection();
        }

        private void initToolbar() {
            JToolBar toolBar = new JToolBar();
            toolBar.add(new JButton("Thêm Exam"));
            toolBar.add(new JButton("Scan lại"));
            add(toolBar, BorderLayout.NORTH);
        }

        private void initMainSplit() {
            examsModel = new DefaultTableModel(new String[]{"ID", "Tên Exam", "Ngày sửa"}, 0);
            examsTable = new JTable(examsModel);
            JScrollPane examsScroll = new JScrollPane(examsTable);

            JTabbedPane tabbedPane = new JTabbedPane();
            // Questions Tab
            questionsModel = new DefaultTableModel(new String[]{"ID", "Nội dung", "Has Audio"}, 0);
            questionsTable = new JTable(questionsModel);
            answersModel = new DefaultTableModel(new String[]{"ID", "AnswerText", "IsCorrect", "AISuggestion"}, 0);
            answersTable = new JTable(answersModel);

            JPanel questionsPanel = new JPanel(new BorderLayout());
            JSplitPane qaSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                    new JScrollPane(questionsTable), new JScrollPane(answersTable));
            qaSplit.setDividerLocation(300);
            questionsPanel.add(qaSplit, BorderLayout.CENTER);
            tabbedPane.addTab("Questions", questionsPanel);

            // Detail Tab
            JPanel detailPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.gridx = 0; gbc.gridy = 0;
            detailPanel.add(new JLabel("Tên:"), gbc);
            gbc.gridx = 1;
            JTextField nameField = new JTextField(20);
            nameField.setEditable(false);
            detailPanel.add(nameField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            detailPanel.add(new JLabel("Mô tả:"), gbc);
            gbc.gridx = 1;
            JTextArea descArea = new JTextArea(5, 20);
            descArea.setEditable(false);
            detailPanel.add(new JScrollPane(descArea), gbc);

            tabbedPane.addTab("Detail", detailPanel);

            JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, examsScroll, tabbedPane);
            mainSplit.setDividerLocation(300);
            add(mainSplit, BorderLayout.CENTER);
        }

        private void loadExams() {
            examsModel.setRowCount(0);
            List<Exam> exams = examDAO.getAllExams();
            for (Exam ex : exams) {
                String dateStr = ex.getModifiedDate() != null
                        ? ex.getModifiedDate().toString() : "";
                examsModel.addRow(new Object[]{ex.getExamID(), ex.getExamName(), dateStr});
            }
        }

        private void registerExamSelection() {
            examsTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = examsTable.getSelectedRow();
                    if (row >= 0) {
                        int examId = (int) examsModel.getValueAt(row, 0);
                        loadQuestions(examId);
                    }
                }
            });
            questionsTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = questionsTable.getSelectedRow();
                    if (row >= 0) {
                        int qId = (int) questionsModel.getValueAt(row, 0);
                        loadAnswers(qId);
                    }
                }
            });
        }

        private void loadQuestions(int examId) {
            questionsModel.setRowCount(0);
            answersModel.setRowCount(0);
            List<Question> questions = questionDAO.getQuestionsByExamID(examId);
            for (Question q : questions) {
                questionsModel.addRow(new Object[]{q.getQuestionID(), q.getContent(), q.getAudioPath() != null});
            }
        }

        private void loadAnswers(int questionId) {
            answersModel.setRowCount(0);
            List<Answer> answers = answerDAO.getAnswersByQuestionID(questionId);
            for (Answer a : answers) {
                answersModel.addRow(new Object[]{a.getAnswerID(), a.getAnswerText(), a.isCorrect(), ""});
            }
        }
    }

    /**
     * Panel quản lý Generated Exams
     */
    static class GeneratedExamPanel extends JPanel {
        private final GeneratedExamDAO genExamDAO = new GeneratedExamDAO();
        private final GeneratedExamQuestionDAO genQDAO = new GeneratedExamQuestionDAO();

        private JTable genExamTable;
        private DefaultTableModel genExamModel;
        private JList<String> genQuestionsList;

        public GeneratedExamPanel() {
            setLayout(new BorderLayout());
            initToolbar();
            initMainSplit();
            loadGeneratedExams();
            registerGenSelection();
        }

        private void initToolbar() {
            JToolBar toolBar = new JToolBar();
            toolBar.add(new JButton("Tạo Random Exam"));
            toolBar.add(new JButton("Xuất File"));
            add(toolBar, BorderLayout.NORTH);
        }

        private void initMainSplit() {
            genExamModel = new DefaultTableModel(new String[]{"ID", "Tên Exam", "Ngày tạo"}, 0);
            genExamTable = new JTable(genExamModel);
            JScrollPane leftScroll = new JScrollPane(genExamTable);

            genQuestionsList = new JList<>(new DefaultListModel<>());
            JScrollPane rightScroll = new JScrollPane(genQuestionsList);

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
            split.setDividerLocation(300);
            add(split, BorderLayout.CENTER);
        }

        private void loadGeneratedExams() {
            genExamModel.setRowCount(0);
            List<GeneratedExam> list = genExamDAO.getAllGeneratedExams();
            for (GeneratedExam ge : list) {
                String dateStr = ge.getCreatedDate() != null
                        ? ge.getCreatedDate().toString() : "";
                genExamModel.addRow(new Object[]{ge.getGeneratedExamID(), ge.getExamName(), dateStr});
            }
        }

        private void registerGenSelection() {
            genExamTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = genExamTable.getSelectedRow();
                    if (row >= 0) {
                        int genId = (int) genExamModel.getValueAt(row, 0);
                        loadGenQuestions(genId);
                    }
                }
            });
        }

        private void loadGenQuestions(int genId) {
            DefaultListModel<String> model = (DefaultListModel<String>) genQuestionsList.getModel();
            model.clear();
            List<GeneratedExamQuestion> geqs = genQDAO.getByGeneratedExamID(genId);
            for (GeneratedExamQuestion geq : geqs) {
                model.addElement("QID: " + geq.getQuestionID());
            }
        }
    }
}
