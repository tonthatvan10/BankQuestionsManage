package BankQuestionManagement.Controller;

import BankQuestionManagement.DAO.AnswerDAO;
import BankQuestionManagement.Model.Answer;
import BankQuestionManagement.Model.Question;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AnswerManagementDialog extends JDialog {
    private final Question question;
    private final AnswerDAO answerDAO = new AnswerDAO();

    private final DefaultTableModel tableModel;
    private final JTable tblAnswers;
    private final JTextField txtAnswerText;
    private final JCheckBox chkCorrect;
    private final JButton btnAdd, btnUpdate, btnDelete;

    public AnswerManagementDialog(Frame owner, Question question) {
        super(owner, "Manage Answers for Q" + question.getQuestionID(), true);
        this.question = question;

        // Table
        tableModel = new DefaultTableModel(new Object[]{"ID", "Text", "Correct"}, 0);
        tblAnswers  = new JTable(tableModel);
        loadAnswers();

        // Form controls
        txtAnswerText = new JTextField(25);
        chkCorrect    = new JCheckBox("Is Correct");
        btnAdd    = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");

        layoutComponents();
        attachListeners();

        pack();
        setLocationRelativeTo(owner);
    }

    private void layoutComponents() {
        JPanel pnlForm = new JPanel();
        pnlForm.add(new JLabel("Answer:"));
        pnlForm.add(txtAnswerText);
        pnlForm.add(chkCorrect);
        pnlForm.add(btnAdd);
        pnlForm.add(btnUpdate);
        pnlForm.add(btnDelete);

        setLayout(new BorderLayout());
        add(new JScrollPane(tblAnswers), BorderLayout.CENTER);
        add(pnlForm, BorderLayout.SOUTH);
    }

    private void loadAnswers() {
        tableModel.setRowCount(0);
        List<Answer> list = answerDAO.getAnswersByQuestionID(question.getQuestionID());
        for (Answer a : list) {
            tableModel.addRow(new Object[]{
                    a.getAnswerID(),
                    a.getAnswerText(),
                    a.isCorrect()
            });
        }
    }

    private void attachListeners() {
        // Khi chọn dòng trên table, đổ về form
        tblAnswers.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblAnswers.getSelectedRow() >= 0) {
                int row = tblAnswers.getSelectedRow();
                txtAnswerText.setText(tableModel.getValueAt(row, 1).toString());
                chkCorrect.setSelected((Boolean)tableModel.getValueAt(row, 2));
            }
        });

        btnAdd.addActionListener(e -> {
            String text = txtAnswerText.getText().trim();
            boolean correct = chkCorrect.isSelected();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Answer text cannot be empty");
                return;
            }
            Answer a = new Answer();
            a.setQuestionID(question.getQuestionID());
            a.setAnswerText(text);
            a.setCorrect(correct);
            int id = answerDAO.addAnswer(a);
            if (id > 0) loadAnswers();
        });

        btnUpdate.addActionListener(e -> {
            int row = tblAnswers.getSelectedRow();
            if (row < 0) return;
            int id = (Integer)tableModel.getValueAt(row, 0);
            Answer a = new Answer();
            a.setAnswerID(id);
            a.setQuestionID(question.getQuestionID());
            a.setAnswerText(txtAnswerText.getText().trim());
            a.setCorrect(chkCorrect.isSelected());
            if (answerDAO.updateAnswer(a)) loadAnswers();
        });

        btnDelete.addActionListener(e -> {
            int row = tblAnswers.getSelectedRow();
            if (row < 0) return;
            int id = (Integer)tableModel.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete this answer?") == JOptionPane.YES_OPTION) {
                if (answerDAO.deleteAnswer(id)) loadAnswers();
            }
        });
    }
}
