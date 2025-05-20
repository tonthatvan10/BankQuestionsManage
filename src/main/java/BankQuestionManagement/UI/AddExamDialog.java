package BankQuestionManagement.UI;

import BankQuestionManagement.DAO.ExamDAO;
import BankQuestionManagement.Service.GeminiService;
import BankQuestionManagement.Model.Exam;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Dialog thêm Exam mới và thực hiện scan OCR.
 */
public class AddExamDialog extends JDialog {
    private final JTextField nameField = new JTextField(20);
    private final JTextArea descArea = new JTextArea(5,20);
    private final JTextField imagePathField = new JTextField(20);
    private final JTextField audioPathField = new JTextField(20);
    private final JButton browseImageBtn = new JButton("Chọn ảnh");
    private final JButton browseAudioBtn = new JButton("Chọn audio");
    private final JButton saveScanBtn = new JButton("Lưu & Scan");

    private final ExamDAO examDAO = new ExamDAO();
    private final GeminiService geminiService = new GeminiService();

    public AddExamDialog(JFrame parent) {
        super(parent, "Thêm Exam mới", true);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx=0; gbc.gridy=0; formPanel.add(new JLabel("Tên đề:"), gbc);
        gbc.gridx=1; formPanel.add(nameField, gbc);
        gbc.gridx=0; gbc.gridy=1; formPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx=1; formPanel.add(new JScrollPane(descArea), gbc);
        gbc.gridx=0; gbc.gridy=2; formPanel.add(new JLabel("Ảnh đề:"), gbc);
        gbc.gridx=1; formPanel.add(imagePathField, gbc);
        gbc.gridx=2; formPanel.add(browseImageBtn, gbc);
        gbc.gridx=0; gbc.gridy=3; formPanel.add(new JLabel("Audio (nếu có):"), gbc);
        gbc.gridx=1; formPanel.add(audioPathField, gbc);
        gbc.gridx=2; formPanel.add(browseAudioBtn, gbc);
        add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        btnPanel.add(saveScanBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Action listeners
        browseImageBtn.addActionListener(this::onBrowseImage);
        browseAudioBtn.addActionListener(this::onBrowseAudio);
        saveScanBtn.addActionListener(this::onSaveAndScan);
    }

    private void onBrowseImage(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile(); imagePathField.setText(f.getAbsolutePath());
        }
    }
    private void onBrowseAudio(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile(); audioPathField.setText(f.getAbsolutePath());
        }
    }
    private void onSaveAndScan(ActionEvent e) {
        String name = nameField.getText().trim();
        if (name.isEmpty() || imagePathField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên và ảnh đề không được để trống.","Lỗi",JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Disable button
        saveScanBtn.setEnabled(false);
        SwingWorker<Void,Void> worker = new SwingWorker<>() {
            private Exception ex;
            @Override protected Void doInBackground() {
                try {
                    // Lưu metadata
                    Exam exam = new Exam();
                    exam.setExamName(name);
                    exam.setDescription(descArea.getText());
                    exam.setImagePath(imagePathField.getText());
                    int id = examDAO.addExam(exam);
                    exam.setExamID(id);
                    // Scan OCR
                    geminiService.scanAndCreateExam(new File(imagePathField.getText()), name, descArea.getText());
                } catch(Exception ex) { this.ex = ex; }
                return null;
            }
            @Override protected void done() {
                saveScanBtn.setEnabled(true);
                if (ex!=null) {
                    JOptionPane.showMessageDialog(AddExamDialog.this, "Lỗi: " + ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(AddExamDialog.this, "Tạo đề và scan thành công!","Thành công",JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            }
        };
        worker.execute();
    }
}
