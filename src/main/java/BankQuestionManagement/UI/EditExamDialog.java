package BankQuestionManagement.UI;

import BankQuestionManagement.DAO.ExamDAO;
import BankQuestionManagement.DAO.QuestionDAO;
import BankQuestionManagement.Model.Exam;
import BankQuestionManagement.Model.Question;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class EditExamDialog extends JDialog {
    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();

    private Exam exam;
    private JTextField nameField;
    private JTextArea descArea;
    private JTextField imageField;
    private DefaultTableModel questionModel;
    private JTable questionTable;

    public EditExamDialog(Frame owner, Exam exam) {
        super(owner, "Sửa Exam", true);
        this.exam = exam;
        initComponents();
        loadExamDetails();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        // Thông tin Exam
        nameField = new JTextField(20);
        descArea   = new JTextArea(4,20);
        imageField = new JTextField(20);
        JButton btnChooseImage = new JButton("Chọn ảnh");
        btnChooseImage.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                imageField.setText(fc.getSelectedFile().getAbsolutePath());
        });

        JPanel infoP = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5); c.anchor = GridBagConstraints.WEST;
        c.gridx=0; c.gridy=0; infoP.add(new JLabel("Tên:"), c);
        c.gridx=1; infoP.add(nameField, c);
        c.gridy=1; c.gridx=0; infoP.add(new JLabel("Mô tả:"), c);
        c.gridx=1; infoP.add(new JScrollPane(descArea), c);
        c.gridy=2; c.gridx=0; infoP.add(new JLabel("Ảnh:"), c);
        c.gridx=1; infoP.add(imageField, c);
        c.gridx=2; infoP.add(btnChooseImage, c);

        // Bảng câu hỏi
        questionModel = new DefaultTableModel(new String[]{"ID","Nội dung","AudioPath"}, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col == 1; // chỉ cho sửa nội dung
            }
        };
        questionTable = new JTable(questionModel);
        JScrollPane tableScroll = new JScrollPane(questionTable);

        // Nút thêm/xóa/chọn audio
        JButton btnAddQ = new JButton("Thêm câu hỏi");
        JButton btnDelQ = new JButton("Xóa câu hỏi");
        JButton btnChooseAudio = new JButton("Chọn audio");

        btnAddQ.addActionListener(e -> {
            // Form nhập nội dung và chọn audio
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
                    // Lưu DB ngay lập tức
                    Question qNew = new Question(exam.getExamID(), content, audio);
                    int newId = questionDAO.addQuestion(qNew);
                    questionModel.addRow(new Object[]{newId, content, audio});
                }
            }
        });

        btnDelQ.addActionListener(e -> {
            int r = questionTable.getSelectedRow();
            if(r >= 0) {
                int qId = (Integer) questionModel.getValueAt(r,0);
                if(qId > 0) questionDAO.deleteQuestion(qId);
                questionModel.removeRow(r);
            }
        });

        btnChooseAudio.addActionListener(e -> {
            int r = questionTable.getSelectedRow();
            if(r >= 0) {
                JFileChooser fc = new JFileChooser();
                if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
                    String path = fc.getSelectedFile().getAbsolutePath();
                    questionModel.setValueAt(path, r, 2);
                    int qId = (Integer) questionModel.getValueAt(r,0);
                    Question q = questionDAO.getQuestionByID(qId);
                    q.setAudioPath(path);
                    questionDAO.updateQuestion(q);
                }
            }
        });

        JPanel btnQPanel = new JPanel();
        btnQPanel.add(btnAddQ);
        btnQPanel.add(btnDelQ);
        btnQPanel.add(btnChooseAudio);

        // Nút Lưu/Hủy
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());
        JPanel btnP = new JPanel();
        btnP.add(btnSave);
        btnP.add(btnCancel);

        setLayout(new BorderLayout());
        add(infoP, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(btnQPanel, BorderLayout.WEST);
        add(btnP, BorderLayout.SOUTH);
    }

    private void loadExamDetails() {
        // Load Exam
        nameField.setText(exam.getExamName());
        descArea.setText(exam.getDescription());
        imageField.setText(exam.getImagePath());

        // Load Questions
        questionModel.setRowCount(0);
        List<Question> qs = questionDAO.getQuestionsByExamID(exam.getExamID());
        for (Question q : qs) {
            questionModel.addRow(new Object[]{q.getQuestionID(), q.getContent(), q.getAudioPath()});
        }
    }

    private void onSave() {
        // 1) Cập nhật Exam
        exam.setExamName(nameField.getText().trim());
        exam.setDescription(descArea.getText().trim());
        exam.setImagePath(imageField.getText().trim());
        examDAO.updateExam(exam);

        // 2) Cập nhật nội dung câu hỏi đã sửa
        for(int i=0; i<questionModel.getRowCount(); i++) {
            int qId = (Integer) questionModel.getValueAt(i,0);
            String content = (String) questionModel.getValueAt(i,1);
            String audio = (String) questionModel.getValueAt(i,2);
            Question q = questionDAO.getQuestionByID(qId);
            q.setContent(content);
            q.setAudioPath(audio);
            questionDAO.updateQuestion(q);
        }

        JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
