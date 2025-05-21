package BankQuestionManagement.UI;


import BankQuestionManagement.DAO.*;
import BankQuestionManagement.Model.*;
import BankQuestionManagement.Service.GeminiService;
import BankQuestionManagement.Service.GeneratedExamService;
import BankQuestionManagement.Util.DocumentExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    public MainFrame() {
        setTitle("Quản lý ngân hàng đề Tiếng Nhật");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Quản lý Đề", new ExamPanel());
        tabs.addTab("Đề đã sinh", new GeneratedExamPanel());
        tabs.addTab("Xuất File", new ExportPanel());
        add(tabs);
    }

    // Panel quản lý Exam
    static class ExamPanel extends JPanel {
        private final ExamDAO examDAO = new ExamDAO();
        private final QuestionDAO questionDAO = new QuestionDAO();
        private final AISuggestionDAO suggestionDAO = new AISuggestionDAO();
        private final AnswerDAO answerDAO = new AnswerDAO();

        private final DefaultTableModel examModel = new DefaultTableModel(new String[]{"ID","Tên","Ngày tạo"},0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        private final JTable examTable = new JTable(examModel);
        private final DefaultTableModel questionModel = new DefaultTableModel(new String[]{"ID","Câu hỏi","Gợi ý AI","AudioPath"},0) {
            @Override public boolean isCellEditable(int row, int col) { return col==1 || col==3; }
        };
        private final JTable questionTable = new JTable(questionModel);
        private final DefaultTableModel answerModel = new DefaultTableModel(new String[]{"ID","Đáp án","Đúng"},0) {
            @Override public boolean isCellEditable(int row, int col) { return col==1 || col==2; }
        };
        private final JTable answerTable = new JTable(answerModel);

        public ExamPanel() {
            setLayout(new BorderLayout());
            initToolbar();
            initTables();
            initSplit();
            loadExams();
        }

        private void initToolbar() {
            JToolBar bar = new JToolBar();
            JButton addBtn = new JButton("Thêm Exam");
            JButton editBtn = new JButton("Sửa Exam");
            JButton deleteBtn = new JButton("Xóa Exam");
            addBtn.addActionListener(e -> addExam());
            editBtn.addActionListener(e -> editExam());
            deleteBtn.addActionListener(e -> deleteExam());
            bar.add(addBtn);
            bar.add(editBtn);
            bar.add(deleteBtn);
            add(bar, BorderLayout.NORTH);
        }

        private void initTables() {
            examTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            examTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && examTable.getSelectedRow() >= 0) {
                    int examId = (int) examModel.getValueAt(examTable.getSelectedRow(), 0);
                    loadQuestions(examId);
                }
            });

            questionTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && questionTable.getSelectedRow() >= 0) {
                    int qId = (int) questionModel.getValueAt(questionTable.getSelectedRow(), 0);
                    loadAnswers(qId);
                }
            });

            // Save edits inline
            questionModel.addTableModelListener(e -> {
                if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int qId = (int) questionModel.getValueAt(row, 0);
                    Question q = questionDAO.getQuestionByID(qId);
                    q.setContent((String) questionModel.getValueAt(row,1));
                    q.setAudioPath((String) questionModel.getValueAt(row,3));
                    questionDAO.updateQuestion(q);
                }
            });

            answerModel.addTableModelListener(e -> {
                if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int aId = (int) answerModel.getValueAt(row, 0);
                    Answer a = answerDAO.getAnswerByID(aId);
                    a.setAnswerText((String) answerModel.getValueAt(row,1));
                    a.setCorrect((Boolean) answerModel.getValueAt(row,2));
                    answerDAO.updateAnswer(a);
                }
            });
        }

        private void initSplit() {
            JSplitPane qaSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                    new JScrollPane(questionTable), new JScrollPane(answerTable));
            qaSplit.setResizeWeight(0.6);
            JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    new JScrollPane(examTable), qaSplit);
            mainSplit.setDividerLocation(300);
            add(mainSplit, BorderLayout.CENTER);
        }

        private void loadExams() {
            examModel.setRowCount(0);
            for (Exam ex : examDAO.getAllExams()) {
                examModel.addRow(new Object[]{ex.getExamID(), ex.getExamName(), ex.getModifiedDate()});
            }
        }

        private void loadQuestions(int examId) {
            questionModel.setRowCount(0);
            answerModel.setRowCount(0);
            List<Question> qs = questionDAO.getQuestionsByExamID(examId);
            for (Question q : qs) {
                String aiHint = suggestionDAO.getAISuggestionsByQuestionID(q.getQuestionID())
                        .stream().findFirst().map(AISuggestion::getSuggestedAnswer).orElse("");
                questionModel.addRow(new Object[]{q.getQuestionID(), q.getContent(), aiHint, q.getAudioPath()});
            }
        }

        private void loadAnswers(int qId) {
            answerModel.setRowCount(0);
            for (Answer a : answerDAO.getAnswersByQuestionID(qId)) {
                answerModel.addRow(new Object[]{a.getAnswerID(), a.getAnswerText(), a.isCorrect()});
            }
        }

        private void addExam() {
            AddExamDialog dlg = new AddExamDialog((Frame) SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
            loadExams(); questionModel.setRowCount(0); answerModel.setRowCount(0);
        }

        private void editExam() {
            int row = examTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn Exam để sửa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) examModel.getValueAt(row, 0);
            Exam ex = examDAO.getExamById(id);
            EditExamDialog dlg = new EditExamDialog((Frame) SwingUtilities.getWindowAncestor(this), ex);
            dlg.setVisible(true);
            // Sau khi dialog đóng, làm mới table
            loadExams(); questionModel.setRowCount(0); answerModel.setRowCount(0);
        }

        private void deleteExam() {
            int row = examTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn Exam cần xóa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) examModel.getValueAt(row,0);
            if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa?","Xóa",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                examDAO.deleteExam(id);
                loadExams(); questionModel.setRowCount(0); answerModel.setRowCount(0);
            }
        }
    }

    // Panel sinh đề ngẫu nhiên
    static class GeneratedExamPanel extends JPanel {
        private final QuestionDAO questionDAO = new QuestionDAO();
        private final GeneratedExamService genService = new GeneratedExamService();
        private final GeneratedExamDAO genDAO = new GeneratedExamDAO();
        private final GeneratedExamQuestionDAO genQDAO = new GeneratedExamQuestionDAO();

        private final JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        private final JButton createBtn = new JButton("Tạo Random Exam");
        private final DefaultTableModel genModel = new DefaultTableModel(new String[]{"ID","Tên","Ngày"},0);
        private final JTable genTable = new JTable(genModel);
        private final DefaultListModel<String> qListModel = new DefaultListModel<>();
        private final JList<String> qList = new JList<>(qListModel);

        public GeneratedExamPanel() {
            setLayout(new BorderLayout());
            JToolBar bar = new JToolBar();
            bar.add(new JLabel("Số câu:"));
            bar.add(countSpinner);
            bar.add(createBtn);
            add(bar, BorderLayout.NORTH);

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    new JScrollPane(genTable), new JScrollPane(qList));
            split.setDividerLocation(300);
            add(split, BorderLayout.CENTER);

            loadGenerated();

            createBtn.addActionListener(e -> {
                int c = (int) countSpinner.getValue();
                try {
                    // gọi service mới, chỉ truyền count
                    genService.generateRandomExam(c);
                    loadGenerated();
                } catch(Exception ex){
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            genTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && genTable.getSelectedRow() >= 0) {
                    int gid = (int) genModel.getValueAt(genTable.getSelectedRow(),0);
                    qListModel.clear();
                    for (GeneratedExamQuestion geq : genQDAO.getByGeneratedExamID(gid)) {
                        qListModel.addElement("QID: " + geq.getQuestionID());
                    }
                }
            });
        }

        private void loadGenerated() {
            genModel.setRowCount(0);
            for (GeneratedExam ge: genDAO.getAllGeneratedExams()) {
                genModel.addRow(new Object[]{ge.getGeneratedExamID(), ge.getExamName(), ge.getCreatedDate()});
            }
        }
    }


    // Panel xuất file
    static class ExportPanel extends JPanel {
        private final ExamDAO examDAO = new ExamDAO();
        private final GeneratedExamDAO genDAO = new GeneratedExamDAO();
        private final DocumentExporter exporter = new DocumentExporter();

        // Combo chứa cả Exam và GeneratedExam
        private final JComboBox<Object> combo = new JComboBox<>();
        private final JComboBox<String> formatBox = new JComboBox<>(new String[]{"PDF", "DOCX"});

        public ExportPanel() {
            setLayout(new BorderLayout());

            // Phần trên: selector & format
            JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
            topBar.add(new JLabel("Chọn đề:"));
            combo.setPreferredSize(new Dimension(300, 24));
            topBar.add(combo);

            topBar.add(new JLabel("Định dạng:"));
            formatBox.setPreferredSize(new Dimension(80, 24));
            topBar.add(formatBox);

            add(topBar, BorderLayout.NORTH);

            // Phần dưới: nút xuất
            JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
            JButton btn = new JButton("Xuất File");
            btn.addActionListener(e -> doExport());
            bottomBar.add(btn);
            add(bottomBar, BorderLayout.SOUTH);

            // Nạp dữ liệu vào combo
            loadItems();
        }

        private void loadItems() {
            combo.removeAllItems();
            for (Exam e : examDAO.getAllExams()) combo.addItem(e);
            for (GeneratedExam ge : genDAO.getAllGeneratedExams()) combo.addItem(ge);
        }

        private void doExport() {
            Object sel = combo.getSelectedItem();
            if (sel == null) {
                JOptionPane.showMessageDialog(this, "Chọn đề để xuất", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            String dir = fc.getSelectedFile().getAbsolutePath();

            new SwingWorker<Void, Void>() {
                Exception ex;
                @Override protected Void doInBackground() {
                    try {
                        if (sel instanceof Exam) {
                            Exam exam = (Exam) sel;
                            // Xuất một lần
                            if ("DOCX".equals(formatBox.getSelectedItem())) {
                                exporter.exportExamToDocx(exam.getExamID(), dir);
                                exporter.exportExamAnswersToDocx(exam.getExamID(), dir);
                            } else {
                                exporter.exportExamToPdf(exam.getExamID(), dir);
                                exporter.exportExamAnswersToPdf(exam.getExamID(), dir);
                            }
                        } else {
                            GeneratedExam ge = (GeneratedExam) sel;
                            // Xuất một lần
                            if ("DOCX".equals(formatBox.getSelectedItem())) {
                                exporter.exportGeneratedExamToDocx(ge.getGeneratedExamID(), dir);
                                exporter.exportGeneratedExamAnswersToDocx(ge.getGeneratedExamID(), dir);
                            } else {
                                exporter.exportGeneratedExamToPdf(ge.getGeneratedExamID(), dir);
                                exporter.exportGeneratedExamAnswersToPdf(ge.getGeneratedExamID(), dir);
                            }
                        }
                    } catch (Exception e) {
                        ex = e;
                    }
                    return null;
                }
                @Override protected void done() {
                    if (ex != null) {
                        JOptionPane.showMessageDialog(ExportPanel.this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ExportPanel.this, "Xuất thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }.execute();
        }
    }




    // Dialog thêm Exam và scan OCR
    static class AddExamDialog extends JDialog {
        private final JTextField nameField = new JTextField(20);
        private final JTextArea descArea = new JTextArea(5,20);
        private final JTextField imageField = new JTextField(20);
        private final ExamDAO examDAO = new ExamDAO();
        private final GeminiService gemini = new GeminiService();

        public AddExamDialog(Frame owner) {
            super(owner, "Thêm Exam mới", true);
            setLayout(new BorderLayout());

            JPanel p = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints(); c.insets=new Insets(5,5,5,5); c.anchor=GridBagConstraints.WEST;
            c.gridx=0; c.gridy=0; p.add(new JLabel("Tên:"),c); c.gridx=1; p.add(nameField,c);
            c.gridy=1; c.gridx=0; p.add(new JLabel("Mô tả:"),c); c.gridx=1; p.add(new JScrollPane(descArea),c);
            c.gridy=2; c.gridx=0; p.add(new JLabel("Ảnh (scan):"),c); c.gridx=1; p.add(imageField,c); JButton bi=new JButton("Chọn"); bi.addActionListener(e->choose(imageField)); c.gridx=2; p.add(bi,c);

            add(p, BorderLayout.CENTER);
            JButton save = new JButton("Lưu & Scan"); save.addActionListener(e->onSave());
            JPanel bp = new JPanel(); bp.add(save);
            add(bp, BorderLayout.SOUTH);

            pack(); setLocationRelativeTo(owner);
        }

        private void choose(JTextField f) {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)
                f.setText(fc.getSelectedFile().getAbsolutePath());
        }

        private void onSave() {
            String name = nameField.getText().trim(), desc = descArea.getText().trim(), img = imageField.getText().trim();
            if (name.isEmpty()||img.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên và ảnh không được trống", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new SwingWorker<Void,Void>(){ Exception ex; @Override protected Void doInBackground(){ try{ int id=examDAO.addExam(new Exam(0,name,desc,img,null,null)); gemini.scanAndPopulateExistingExam(id);}catch(Exception e){ex=e;}return null;} @Override protected void done(){ if(ex!=null) JOptionPane.showMessageDialog(AddExamDialog.this,ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE); else dispose(); }}.execute();
        }
    }
}
