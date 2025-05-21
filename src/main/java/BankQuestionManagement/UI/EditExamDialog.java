package BankQuestionManagement.UI;

import BankQuestionManagement.DAO.*;
import BankQuestionManagement.Model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EditExamDialog extends JDialog {
    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final GeneratedExamDAO genExamDAO = new GeneratedExamDAO();
    private final GeneratedExamQuestionDAO genQDAO = new GeneratedExamQuestionDAO();
    private final AnswerDAO answerDAO = new AnswerDAO();

    private Exam exam;
    private GeneratedExam genExam;
    private boolean isGenerated;

    private JTextField nameField;
    private JTextArea descArea;       // only for Exam
    private JTextField imageField;    // for imagePath when Exam, or exportPath when GeneratedExam
    private DefaultTableModel questionModel;
    private JTable questionTable;

    public EditExamDialog(Frame owner, Exam exam) {
        super(owner, "Sửa Exam", true);
        this.exam = exam;
        this.isGenerated = false;
        initComponents();
        loadDetails();
        pack();
        setLocationRelativeTo(owner);
    }

    public EditExamDialog(Frame owner, GeneratedExam genExam) {
        super(owner, "Sửa GeneratedExam", true);
        this.genExam = genExam;
        this.isGenerated = true;
        initComponents();
        loadDetails();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);

        // Header panel
        JPanel header = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        // Row 0: Label "Tên:" and nameField
        c.gridx = 0;
        c.gridy = 0;
        header.add(new JLabel("Tên:"), c);
        nameField = new JTextField(isGenerated ? genExam.getExamName() : exam.getExamName(), 25);
        c.gridx = 1;
        header.add(nameField, c);

        if (!isGenerated) {
            // Row 1: Label "Mô tả:" and descArea
            c.gridx = 0;
            c.gridy = 1;
            header.add(new JLabel("Mô tả:"), c);
            descArea = new JTextArea(exam.getDescription(), 3, 25);
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            c.gridx = 1;
            header.add(new JScrollPane(descArea), c);

            // Row 2: Label "Ảnh:" and imageField + button
            c.gridx = 0;
            c.gridy = 2;
            header.add(new JLabel("Ảnh:"), c);
            imageField = new JTextField(exam.getImagePath(), 20);
            JButton btnImg = new JButton("Chọn ảnh");
            btnImg.addActionListener(e -> selectImage());
            JPanel imgP = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            imgP.add(imageField);
            imgP.add(btnImg);
            c.gridx = 1;
            header.add(imgP, c);
        }
        else{
            c.gridx = 0;
            c.gridy = 1;
            header.add(new JLabel("Đường dẫn xuất:"), c);
            imageField = new JTextField(genExam.getExportPath(), 25);
            c.gridx = 1;
            header.add(imageField, c);
        }

        contentPane.add(header, BorderLayout.NORTH);

        // Table setup
        String[] cols = {"ID", "Nội dung", "AudioPath"};
        questionModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return !isGenerated && col > 0;
            }
        };
        questionTable = new JTable(questionModel);
        JScrollPane scroll = new JScrollPane(questionTable);
        scroll.setPreferredSize(new Dimension(600, 300));
        contentPane.add(scroll, BorderLayout.CENTER);

        // Controls panel (Add/Delete/Select Audio) - vertically stacked and larger buttons
        JPanel ctrl = new JPanel();
        ctrl.setLayout(new BoxLayout(ctrl, BoxLayout.Y_AXIS));
        JButton btnAdd = new JButton(isGenerated ? "Thêm câu" : "Thêm câu hỏi");
        JButton btnDel = new JButton(isGenerated ? "Xóa câu" : "Xóa câu hỏi");
        JButton btnAudio = new JButton("Chọn audio");

        Dimension btnSize = new Dimension(140, 40);
        Font font = new Font("Arial", Font.PLAIN, 14);
        for (JButton btn : new JButton[]{btnAdd, btnDel, btnAudio}) {
            btn.setMaximumSize(btnSize);
            btn.setPreferredSize(btnSize);
            btn.setFont(font);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            ctrl.add(btn);
            ctrl.add(Box.createVerticalStrut(10)); // spacing
        }

        btnAdd.addActionListener(e -> addQuestion());
        btnDel.addActionListener(e -> deleteQuestion());
        btnAudio.addActionListener(e -> selectAudioForRow());
        contentPane.add(ctrl, BorderLayout.EAST);

        // Footer panel (Save/Cancel)
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        foot.add(btnSave);
        foot.add(btnCancel);
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());
        contentPane.add(foot, BorderLayout.PAGE_END);
    }


    private void loadDetails() {
        questionModel.setRowCount(0);

        if (isGenerated) {
            // Set exportPath into imageField
            nameField.setText(genExam.getExamName());
            imageField.setText(genExam.getExportPath());

            // Load questions via GeneratedExamQuestion
            List<GeneratedExamQuestion> lst = genQDAO.getByGeneratedExamID(genExam.getGeneratedExamID());
            for (GeneratedExamQuestion geq : lst) {
                Question q = questionDAO.getQuestionByID(geq.getQuestionID());
                questionModel.addRow(new Object[]{q.getQuestionID(), q.getContent(), q.getAudioPath()});
            }
        } else {
            // Normal Exam: load description and image
            nameField.setText(exam.getExamName());
            descArea.setText(exam.getDescription());
            imageField.setText(exam.getImagePath());

            // Load questions by examID
            List<Question> lst = questionDAO.getQuestionsByExamID(exam.getExamID());
            for (Question q : lst) {
                questionModel.addRow(new Object[]{q.getQuestionID(), q.getContent(), q.getAudioPath()});
            }
        }
    }

    private void addQuestion() {
        JTextArea txtQ = new JTextArea(3, 20);
        JTextField[] txtA = new JTextField[4];
        JRadioButton[] rb = new JRadioButton[4];
        ButtonGroup bg = new ButtonGroup();
        JTextField txtAudio = new JTextField(20);
        JButton pickAudio = new JButton("Chọn audio");
        pickAudio.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtAudio.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel pnl = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        pnl.add(new JLabel("Nội dung câu hỏi:"), c);
        c.gridx = 1;
        pnl.add(new JScrollPane(txtQ), c);

        for (int i = 0; i < 4; i++) {
            txtA[i] = new JTextField(20);
            rb[i] = new JRadioButton("Đáp án đúng");
            bg.add(rb[i]);
            c.gridy = i + 1;
            c.gridx = 0;
            pnl.add(new JLabel("Đáp án " + (i + 1) + ":"), c);
            c.gridx = 1;
            pnl.add(txtA[i], c);
            c.gridx = 2;
            pnl.add(rb[i], c);
        }

        c.gridy = 5;
        c.gridx = 0;
        pnl.add(new JLabel("AudioPath:"), c);
        c.gridx = 1;
        pnl.add(txtAudio, c);
        c.gridx = 2;
        pnl.add(pickAudio, c);

        int res = JOptionPane.showConfirmDialog(this, pnl, "Thêm câu hỏi", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        String content = txtQ.getText().trim();
        String audio = txtAudio.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nội dung câu hỏi không được để trống!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int parentExamID = isGenerated ? genExam.getGeneratedExamID() : exam.getExamID();
        Question newQ = new Question(parentExamID, content, audio);

        // 1) Thêm Question và kiểm tra ID trả về
        int newId = questionDAO.addQuestion(newQ);
        if (newId <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Không tạo được câu hỏi mới trong cơ sở dữ liệu!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2) Thêm các Answer, bạn có thể kiểm tra lỗi tương tự nếu muốn
        for (int i = 0; i < 4; i++) {
            String at = txtA[i].getText().trim();
            if (!at.isEmpty()) {
                Answer a = new Answer(newId, at, rb[i].isSelected());
                int aid = answerDAO.addAnswer(a);
                if (aid <= 0) {
                    // Nếu thêm answer thất bại, bạn có thể log hoặc báo warning
                    System.err.println("Không thêm được answer cho QuestionID=" + newId);
                }
            }
        }

        // 3) Với GeneratedExam: liên kết vào GeneratedExamQuestions
        if (isGenerated) {
            boolean linked = genQDAO.addGeneratedExamQuestion(genExam.getGeneratedExamID(), newId);
            if (!linked) {
                JOptionPane.showMessageDialog(this,
                        "Không tạo được liên kết giữa GeneratedExam và Question!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                // (tuỳ chọn) bạn có thể gọi questionDAO.deleteQuestion(newId) để rollback
                return;
            }
        }

        // 4) Cập nhật UI
        questionModel.addRow(new Object[]{newId, content, audio});
        JOptionPane.showMessageDialog(this,
                "Thêm câu hỏi thành công!",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }


    private void deleteQuestion() {
        int r = questionTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this,
                    "Chọn câu hỏi để xóa!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa câu hỏi này?",
                "Xác nhận",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.OK_OPTION) {
            int qid = (int) questionModel.getValueAt(r, 0);
            if (isGenerated) {
                // Remove link, then optionally remove question itself:
                genQDAO.deleteGeneratedExamQuestion(genExam.getGeneratedExamID(), qid);
                // If you want to delete the question record too, uncomment:
                // questionDAO.deleteQuestion(qid);
            } else {
                questionDAO.deleteQuestion(qid);
            }
            questionModel.removeRow(r);
            JOptionPane.showMessageDialog(this,
                    "Xóa câu hỏi thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void selectAudioForRow() {
        int r = questionTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this,
                    "Chọn câu hỏi để thêm audio!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            questionModel.setValueAt(path, r, 2);
            int qid = (int) questionModel.getValueAt(r, 0);
            Question q = questionDAO.getQuestionByID(qid);
            q.setAudioPath(path);
            questionDAO.updateQuestion(q);
            JOptionPane.showMessageDialog(this,
                    "Chọn audio thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void selectImage() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            imageField.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private void onSave() {
        if (isGenerated) {
            genExam.setExamName(nameField.getText().trim());
            genExam.setExportPath(imageField.getText().trim());
            genExamDAO.updateGeneratedExam(genExam);
        } else {
            exam.setExamName(nameField.getText().trim());
            exam.setDescription(descArea.getText().trim());
            exam.setImagePath(imageField.getText().trim());
            examDAO.updateExam(exam);
        }
        // Update modified questions if content or audio changed
        for (int i = 0; i < questionModel.getRowCount(); i++) {
            int qid = (int) questionModel.getValueAt(i, 0);
            String content = (String) questionModel.getValueAt(i, 1);
            String audio = (String) questionModel.getValueAt(i, 2);
            Question q = questionDAO.getQuestionByID(qid);
            if (!q.getContent().equals(content) ||
                    (q.getAudioPath() == null ? audio != null : !q.getAudioPath().equals(audio))) {
                q.setContent(content);
                q.setAudioPath(audio);
                questionDAO.updateQuestion(q);
            }
        }
        JOptionPane.showMessageDialog(this, "Đã lưu thành công.");
        dispose();
    }
}
