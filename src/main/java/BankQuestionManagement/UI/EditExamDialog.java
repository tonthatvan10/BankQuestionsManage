package BankQuestionManagement.UI;

import BankQuestionManagement.DAO.*;
import BankQuestionManagement.Model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dialog chung để sửa cả Exam và GeneratedExam.
 */
public class EditExamDialog extends JDialog {
    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final GeneratedExamDAO genExamDAO = new GeneratedExamDAO();
    private final GeneratedExamQuestionDAO genQDAO = new GeneratedExamQuestionDAO();

    private Exam exam;
    private GeneratedExam genExam;
    private boolean isGenerated;

    private JTextField nameField;
    private JTextArea descArea;
    private JTextField imageField;
    private DefaultTableModel questionModel;
    private JTable questionTable;

    public EditExamDialog(Frame owner, Exam exam) {
        super(owner, "Sửa Exam", true);
        this.exam = exam;
        this.isGenerated = false;
        initComponents();
        loadDetails();
        pack(); setLocationRelativeTo(owner);
    }

    public EditExamDialog(Frame owner, GeneratedExam genExam) {
        super(owner, "Sửa GeneratedExam", true);
        this.genExam = genExam;
        this.isGenerated = true;
        initComponents();
        loadDetails();
        pack(); setLocationRelativeTo(owner);
    }

    private void initComponents() {
        // Common: Name
        nameField = new JTextField(30);
        JPanel topP = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topP.add(new JLabel("Tên:"));
        topP.add(nameField);

        // Specific for Exam: description & image
        JPanel infoP = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        if (!isGenerated) {
            descArea = new JTextArea(4, 20);
            imageField = new JTextField(20);
            JButton btnChooseImage = new JButton("Chọn ảnh");
            btnChooseImage.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                    imageField.setText(fc.getSelectedFile().getAbsolutePath());
            });
            c.anchor = GridBagConstraints.WEST;
            c.gridx=0; c.gridy=0; infoP.add(new JLabel("Mô tả:"), c);
            c.gridx=1; infoP.add(new JScrollPane(descArea), c);
            c.gridx=0; c.gridy=1; infoP.add(new JLabel("Ảnh:"), c);
            c.gridx=1; infoP.add(imageField, c);
            c.gridx=2; infoP.add(btnChooseImage, c);
        }

        // Table câu hỏi
        if (isGenerated) {
            questionModel = new DefaultTableModel(new String[]{"QID"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
        } else {
            questionModel = new DefaultTableModel(new String[]{"ID","Nội dung","AudioPath"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return c == 1; }
            };
        }
        questionTable = new JTable(questionModel);
        JScrollPane tableScroll = new JScrollPane(questionTable);
        tableScroll.setPreferredSize(new Dimension(400, 200));

        // Nút thêm/xóa câu
        JPanel btnQPanel = new JPanel();
        JButton btnAddQ = new JButton(isGenerated? "Thêm câu": "Thêm câu hỏi");
        JButton btnDelQ = new JButton(isGenerated? "Xóa câu": "Xóa câu hỏi");
        btnQPanel.add(btnAddQ);
        btnQPanel.add(btnDelQ);
        if (!isGenerated) {
            JButton btnChooseAudio = new JButton("Chọn audio");
            btnChooseAudio.addActionListener(e -> selectAudioForSelectedRow());
            btnQPanel.add(btnChooseAudio);
        }
        btnAddQ.addActionListener(e -> addQuestion());
        btnDelQ.addActionListener(e -> deleteQuestion());

        // Buttons Save/Cancel
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());
        JPanel btnP = new JPanel(); btnP.add(btnSave); btnP.add(btnCancel);

        // Layout tổng
        JPanel north = new JPanel(new BorderLayout());
        north.add(topP, BorderLayout.NORTH);
        if (!isGenerated) north.add(infoP, BorderLayout.CENTER);
        setLayout(new BorderLayout(5,5));
        add(north, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(btnQPanel, BorderLayout.EAST);
        add(btnP, BorderLayout.SOUTH);
    }

    private void loadDetails() {
        if (isGenerated) {
            nameField.setText(genExam.getExamName());
            questionModel.setRowCount(0);
            genQDAO.getByGeneratedExamID(genExam.getGeneratedExamID()).forEach(geq ->
                    questionModel.addRow(new Object[]{ geq.getQuestionID() })
            );
        } else {
            nameField.setText(exam.getExamName());
            descArea.setText(exam.getDescription());
            imageField.setText(exam.getImagePath());
            questionModel.setRowCount(0);
            questionDAO.getQuestionsByExamID(exam.getExamID()).forEach(q ->
                    questionModel.addRow(new Object[]{ q.getQuestionID(), q.getContent(), q.getAudioPath() })
            );
        }
    }

    private void addQuestion() {
        if (isGenerated) {
            String input = JOptionPane.showInputDialog(this, "Nhập QID muốn thêm:");
            try {
                int qid = Integer.parseInt(input.trim());
                genQDAO.addGeneratedExamQuestion(genExam.getGeneratedExamID(), qid);
                questionModel.addRow(new Object[]{ qid });
            } catch(Exception e){ /* bỏ qua */ }
        } else {
            // tương tự như trước, form nhập nội dung & audio
            // tạm gọi lại code cũ:
            JTextArea txtContent = new JTextArea(3,20);
            JTextField txtAudio = new JTextField(20);
            JButton btnPick = new JButton("Chọn file");
            btnPick.addActionListener(ev -> {
                JFileChooser fc = new JFileChooser();
                if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)
                    txtAudio.setText(fc.getSelectedFile().getAbsolutePath());
            });
            JPanel pnl = new JPanel(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(5,5,5,5); gc.anchor = GridBagConstraints.WEST;
            gc.gridx=0; gc.gridy=0; pnl.add(new JLabel("Nội dung:"), gc);
            gc.gridx=1; pnl.add(new JScrollPane(txtContent), gc);
            gc.gridy=1; gc.gridx=0; pnl.add(new JLabel("AudioPath:"), gc);
            gc.gridx=1; pnl.add(txtAudio, gc);
            gc.gridx=2; pnl.add(btnPick, gc);
            int res = JOptionPane.showConfirmDialog(this, pnl, "Thêm câu hỏi", JOptionPane.OK_CANCEL_OPTION);
            if(res == JOptionPane.OK_OPTION) {
                String content = txtContent.getText().trim();
                String audio = txtAudio.getText().trim();
                if(!content.isEmpty()) {
                    Question qNew = new Question(exam.getExamID(), content, audio);
                    int newId = questionDAO.addQuestion(qNew);
                    questionModel.addRow(new Object[]{ newId, content, audio });
                }
            }
        }
    }

    private void deleteQuestion() {
        int r = questionTable.getSelectedRow();
        if (r < 0) return;
        if (isGenerated) {
            int qid = (Integer) questionModel.getValueAt(r, 0);
            genQDAO.deleteGeneratedExamQuestion(genExam.getGeneratedExamID(), qid);
            questionModel.removeRow(r);
        } else {
            int qid = (Integer) questionModel.getValueAt(r, 0);
            questionDAO.deleteQuestion(qid);
            questionModel.removeRow(r);
        }
    }

    private void selectAudioForSelectedRow() {
        int r = questionTable.getSelectedRow();
        if(r < 0) return;
        JFileChooser fc = new JFileChooser();
        if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            questionModel.setValueAt(path, r, 2);
            int qid = (Integer) questionModel.getValueAt(r,0);
            Question q = questionDAO.getQuestionByID(qid);
            q.setAudioPath(path);
            questionDAO.updateQuestion(q);
        }
    }

    private void onSave() {
        if (isGenerated) {
            genExam.setExamName(nameField.getText().trim());
            if (!genExamDAO.updateGeneratedExam(genExam)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            exam.setExamName(nameField.getText().trim());
            exam.setDescription(descArea.getText().trim());
            exam.setImagePath(imageField.getText().trim());
            examDAO.updateExam(exam);
            // cập nhật nội dung và audio các câu
            for (int i = 0; i < questionModel.getRowCount(); i++) {
                int qid = (Integer) questionModel.getValueAt(i,0);
                Question q = questionDAO.getQuestionByID(qid);
                q.setContent((String) questionModel.getValueAt(i,1));
                q.setAudioPath((String) questionModel.getValueAt(i,2));
                questionDAO.updateQuestion(q);
            }
        }
        JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
