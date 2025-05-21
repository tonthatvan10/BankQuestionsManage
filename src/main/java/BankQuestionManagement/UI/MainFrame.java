package BankQuestionManagement.UI;

import BankQuestionManagement.DAO.*;
import BankQuestionManagement.Model.*;
import BankQuestionManagement.Service.GeminiService;
import BankQuestionManagement.Service.GeneratedExamService;
import BankQuestionManagement.Util.DocumentExporter;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.List;
import java.util.Comparator;
import javax.swing.SwingWorker;
import static javax.swing.JOptionPane.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel = new JPanel(cardLayout);

    private DashboardPanel dashboardPanel;
    private ManageAllExamsPanel managePanel;
    private GeneratedExamPanel generatedPanel;
    private ExportPanel exportPanel;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    public MainFrame() {
        setTitle("Quản lý ngân hàng đề Tiếng Nhật");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10,10,10,10));
        JLabel lblTitle = new JLabel("🌸 Quản lý ngân hàng đề Tiếng Nhật", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD,26));
        header.add(lblTitle,BorderLayout.CENTER);

        DefaultListModel<MenuItem> menuModel = new DefaultListModel<>();
        menuModel.addElement(new MenuItem("Dashboard",0));
        menuModel.addElement(new MenuItem("Quản lý Đề",1));
        menuModel.addElement(new MenuItem("Đề đã sinh",2));
        menuModel.addElement(new MenuItem("Xuất File",3));
        JList<MenuItem> menuList = new JList<>(menuModel);
        menuList.setCellRenderer(new MenuCellRenderer());
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuList.setFixedCellWidth(160);
        menuList.setBorder(new EmptyBorder(10,0,10,0));

        dashboardPanel = new DashboardPanel();
        managePanel   = new ManageAllExamsPanel();
        generatedPanel= new GeneratedExamPanel();
        exportPanel   = new ExportPanel();

        contentPanel.add(dashboardPanel,"0");
        contentPanel.add(managePanel,"1");
        contentPanel.add(generatedPanel,"2");
        contentPanel.add(exportPanel,"3");

        menuList.addListSelectionListener(e->{
            if(!e.getValueIsAdjusting()){
                int idx = menuList.getSelectedValue().getIndex();
                cardLayout.show(contentPanel,String.valueOf(idx));
                switch(idx){
                    case 0: dashboardPanel.refresh(); break;
                    case 1: managePanel.refresh(); break;
                    case 2: generatedPanel.refresh(); break;
                    case 3: exportPanel.refresh(); break;
                }
            }
        });
        menuList.setSelectedIndex(0);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header,BorderLayout.NORTH);
        getContentPane().add(menuList,BorderLayout.WEST);
        getContentPane().add(contentPanel,BorderLayout.CENTER);
    }

    static class MenuItem{ private String name; private int idx; public MenuItem(String n,int i){name=n;idx=i;} @Override public String toString(){return name;} public int getIndex(){return idx;} }
    static class MenuCellRenderer extends DefaultListCellRenderer{ @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean isSelected,boolean cellHasFocus){ JLabel lbl=(JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus); MenuItem item=(MenuItem)value; lbl.setBorder(BorderFactory.createEmptyBorder(8,12,8,12)); switch(item.getIndex()){case 0: lbl.setIcon(UIManager.getIcon("FileView.homeFolderIcon"));break;case 1: lbl.setIcon(UIManager.getIcon("FileView.directoryIcon"));break;case 2: lbl.setIcon(UIManager.getIcon("FileView.fileIcon"));break;case 3: lbl.setIcon(UIManager.getIcon("FileChooser.detailsViewIcon"));break;} return lbl; }}

    static class DashboardPanel extends JPanel{
        private final ExamDAO examDAO=new ExamDAO();
        private final GeneratedExamDAO genDAO=new GeneratedExamDAO();
        private final JPanel cards;
        public DashboardPanel(){ setLayout(new BorderLayout()); setBorder(new EmptyBorder(10,10,10,10)); JLabel title=new JLabel("Thống kê chung",SwingConstants.CENTER);title.setFont(new Font("SansSerif",Font.BOLD,24));add(title,BorderLayout.NORTH); cards=new JPanel(new FlowLayout(FlowLayout.CENTER,10,10)); cards.setOpaque(false); initCards(); add(cards,BorderLayout.CENTER);}
        private void initCards(){ cards.add(createCard("Tổng số Exam",examDAO.getAllExams().size(), UIManager.getIcon("OptionPane.questionIcon"), new Color(0xAED581))); cards.add(createCard("Tổng số GeneratedExam",genDAO.getAllGeneratedExams().size(), UIManager.getIcon("OptionPane.informationIcon"), new Color(0x81D4FA))); }
        private JPanel createCard(String text,int count,Icon icon,Color bg){ JPanel card=new JPanel(new FlowLayout(FlowLayout.LEFT,8,8)); card.setBackground(bg); card.setBorder(BorderFactory.createEmptyBorder(8,12,8,12)); card.add(new JLabel(icon)); JLabel num=new JLabel(String.valueOf(count)); num.setFont(new Font("SansSerif",Font.BOLD,28)); card.add(num); JLabel lbl=new JLabel(text); lbl.setFont(new Font("SansSerif",Font.BOLD,16)); card.add(lbl); return card; }
        public void refresh(){ cards.removeAll(); initCards(); cards.revalidate(); cards.repaint(); }
    }

    static class ManageAllExamsPanel extends JPanel {
        private final ExamDAO examDAO = new ExamDAO();
        private final GeneratedExamDAO genDAO = new GeneratedExamDAO();

        // 1. Thêm "ExportPath" vào header cột
        private final DefaultTableModel model = new DefaultTableModel(
                new String[]{"Loại", "ID", "Tên", "Ngày Tạo", "ExportPath"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        private final JTable table = new JTable(model);

        public ManageAllExamsPanel() {
            setLayout(new BorderLayout(10,10));
            setBorder(new EmptyBorder(10,10,10,10));
            JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
            JButton addBtn = new JButton("Thêm Exam");
            JButton detailBtn = new JButton("Chi tiết");
            bar.add(addBtn);
            bar.add(detailBtn);
            add(bar, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);

            loadData();

            addBtn.addActionListener(e -> {
                AddExamDialog dlg = new AddExamDialog((Frame)SwingUtilities.getWindowAncestor(this));
                dlg.setVisible(true);
                loadData();
            });
            detailBtn.addActionListener(e -> showDetails());
        }

        private void loadData() {
            model.setRowCount(0);

            // Load Exam thường
            for (Exam ex : examDAO.getAllExams()) {
                model.addRow(new Object[]{
                        "Exam",
                        ex.getExamID(),
                        ex.getExamName(),
                        ex.getModifiedDate(),
                        ex.getExportPath()   // lấy exportPath từ model
                });
            }

            // Load GeneratedExam
            for (GeneratedExam ge : genDAO.getAllGeneratedExams()) {
                model.addRow(new Object[]{
                        "Generated",
                        ge.getGeneratedExamID(),
                        ge.getExamName(),
                        ge.getCreatedDate(),
                        ge.getExportPath()   // lấy exportPath từ model
                });
            }
        }

        public void refresh() {
            loadData();
        }

        private void showDetails() {
            int r = table.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Chọn dòng để xem chi tiết.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String type = (String)model.getValueAt(r, 0);
            int id      = (int)model.getValueAt(r, 1);
            if ("Exam".equals(type)) {
                Exam ex = examDAO.getExamById(id);
                new DetailDialog((Frame)SwingUtilities.getWindowAncestor(this), ex).setVisible(true);
            } else {
                GeneratedExam ge = genDAO.getGeneratedExamByID(id);
                new DetailDialog((Frame)SwingUtilities.getWindowAncestor(this), ge).setVisible(true);
            }
            loadData();
        }
    }

    // -------- GeneratedExamPanel --------
    static class GeneratedExamPanel extends JPanel {
        private final GeneratedExamService genService = new GeneratedExamService();
        private final GeneratedExamDAO genDAO = new GeneratedExamDAO();
        private final GeneratedExamQuestionDAO genQDAO = new GeneratedExamQuestionDAO();
        private final JSpinner countSp = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        private final JButton createBtn = new JButton("Tạo Random Exam", UIManager.getIcon("FileChooser.newFolderIcon"));
        private final DefaultTableModel genModel = new DefaultTableModel(
                new String[]{"ID","Tên","Ngày"}, 0);
        private final JTable genTable = new JTable(genModel);
        private final DefaultListModel<String> qListModel = new DefaultListModel<>();
        private final JList<String> qList = new JList<>(qListModel);

        public GeneratedExamPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JToolBar bar = new JToolBar(); bar.setFloatable(false);
            bar.add(new JLabel("Số câu:")); bar.add(countSp);
            bar.add(Box.createHorizontalStrut(10)); bar.add(createBtn);
            add(bar, BorderLayout.NORTH);

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    new JScrollPane(genTable), new JScrollPane(qList));
            split.setDividerLocation(300);
            split.setBorder(BorderFactory.createTitledBorder("Generated Exams / Questions"));
            add(split, BorderLayout.CENTER);

            loadGenerated();
            createBtn.addActionListener(e -> { try { genService.generateRandomExam((int)countSp.getValue()); loadGenerated(); } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); } });
            genTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && genTable.getSelectedRow()>=0) {
                    int gid=(int)genModel.getValueAt(genTable.getSelectedRow(),0);
                    qListModel.clear();
                    for (GeneratedExamQuestion geq : genQDAO.getByGeneratedExamID(gid)) {
                        qListModel.addElement("QID: " + geq.getQuestionID());
                    }
                }
            });
        }

        private void loadGenerated() {
            genModel.setRowCount(0);
            for (GeneratedExam ge : genDAO.getAllGeneratedExams()) {
                genModel.addRow(new Object[]{ge.getGeneratedExamID(), ge.getExamName(), ge.getCreatedDate()});
            }
        }

        public void refresh() {
            loadGenerated();
        }
    }

    // -------- ExportPanel --------
    static class ExportPanel extends JPanel {
        private final ExamDAO examDAO = new ExamDAO();
        private final GeneratedExamDAO genDAO = new GeneratedExamDAO();
        private final DocumentExporter exporter = new DocumentExporter();
        private final JComboBox<Object> combo = new JComboBox<>();
        private final JComboBox<String> formatBox = new JComboBox<>(new String[]{"PDF","DOCX"});

        public ExportPanel() {
            setLayout(new BorderLayout(10,10));
            setBorder(new EmptyBorder(10,10,10,10));

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,8,5));
            top.add(new JLabel("Chọn đề:"));
            combo.setPreferredSize(new Dimension(300,24)); top.add(combo);
            top.add(new JLabel("Định dạng:"));
            formatBox.setPreferredSize(new Dimension(80,24)); top.add(formatBox);

            JButton btn = new JButton("Xuất File");
            btn.setIcon(UIManager.getIcon("FileChooser.detailsViewIcon"));
            top.add(Box.createHorizontalStrut(20)); top.add(btn);

            add(top, BorderLayout.NORTH);
            loadItems();

            btn.addActionListener(e -> doExport());
        }

        private void loadItems() {
            combo.removeAllItems();
            for (Exam e : examDAO.getAllExams()) combo.addItem(e);
            for (GeneratedExam ge : genDAO.getAllGeneratedExams()) combo.addItem(ge);
        }

        public void refresh() {
            loadItems();
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

                @Override
                protected Void doInBackground() {
                    try {
                        // 1) Xuất file
                        if (sel instanceof Exam) {
                            Exam exam = (Exam) sel;
                            if ("DOCX".equals(formatBox.getSelectedItem())) {
                                exporter.exportExamToDocx(exam.getExamID(), dir);
                                exporter.exportExamAnswersToDocx(exam.getExamID(), dir);
                            } else {
                                exporter.exportExamToPdf(exam.getExamID(), dir);
                                exporter.exportExamAnswersToPdf(exam.getExamID(), dir);
                            }
                            // 2) Lưu exportPath vào DB
                            String ep = dir + File.separator + "Exam_" + exam.getExamID() + "." +
                                    (formatBox.getSelectedItem().equals("DOCX") ? "docx" : "pdf");
                            exam.setExportPath(ep);
                            new ExamDAO().updateExportPath(exam.getExamID(), ep);

                        } else {
                            GeneratedExam ge = (GeneratedExam) sel;
                            if ("DOCX".equals(formatBox.getSelectedItem())) {
                                exporter.exportGeneratedExamToDocx(ge.getGeneratedExamID(), dir);
                                exporter.exportGeneratedExamAnswersToDocx(ge.getGeneratedExamID(), dir);
                            } else {
                                exporter.exportGeneratedExamToPdf(ge.getGeneratedExamID(), dir);
                                exporter.exportGeneratedExamAnswersToPdf(ge.getGeneratedExamID(), dir);
                            }
                            // Lưu exportPath
                            String ep = dir + File.separator + "GeneratedExam_" + ge.getGeneratedExamID() + "." +
                                    (formatBox.getSelectedItem().equals("DOCX") ? "docx" : "pdf");
                            ge.setExportPath(ep);
                            new GeneratedExamDAO().updateExportPath(ge.getGeneratedExamID(), ep);
                        }
                    } catch (Exception e) {
                        ex = e;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    if (ex != null) {
                        JOptionPane.showMessageDialog(ExportPanel.this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ExportPanel.this, "Xuất thành công!", "OK",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    // DIALOG thêm Exam + Scan
    static class AddExamDialog extends JDialog {
        private final JTextField nameField = new JTextField(20);
        private final JTextArea descArea = new JTextArea(5, 20);
        private final JTextField imageField = new JTextField(20);
        private final GeminiService gemini = new GeminiService();

        public AddExamDialog(Frame owner) {
            super(owner, "Thêm Exam mới", true);
            setLayout(new BorderLayout());
            JPanel p = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(5, 5, 5, 5);
            c.anchor = GridBagConstraints.WEST;

            c.gridy = 0; c.gridx = 0; p.add(new JLabel("Tên:"), c);
            c.gridx = 1; p.add(nameField, c);

            c.gridy = 1; c.gridx = 0; p.add(new JLabel("Mô tả:"), c);
            c.gridx = 1; p.add(new JScrollPane(descArea), c);

            c.gridy = 2; c.gridx = 0; p.add(new JLabel("Ảnh (scan):"), c);
            c.gridx = 1; p.add(imageField, c);

            JButton bi = new JButton("Chọn ảnh");
            bi.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)
                    imageField.setText(fc.getSelectedFile().getAbsolutePath());
            });
            c.gridx = 2; p.add(bi, c);
            add(p, BorderLayout.CENTER);

            JButton scanBtn = new JButton("Scan & Lưu");
            JPanel bp = new JPanel(); bp.add(scanBtn);
            add(bp, BorderLayout.SOUTH);

            scanBtn.addActionListener(e -> {
                String name = nameField.getText().trim();
                String desc = descArea.getText().trim();
                String img = imageField.getText().trim();
                if(name.isEmpty() || img.isEmpty()){
                    JOptionPane.showMessageDialog(this, "Tên và ảnh không được trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                new SwingWorker<Void,Void>(){
                    @Override protected Void doInBackground() throws Exception {
                        gemini.scanAndCreateExam(new File(img), name, desc);
                        return null;
                    }
                    @Override protected void done(){
                        JOptionPane.showMessageDialog(AddExamDialog.this, "Thêm và scan đề thi thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    }
                }.execute();
            });

            pack();
            setLocationRelativeTo(owner);
        }
    }
    static class DetailDialog extends JDialog {
        private final Frame ownerFrame;
        private final GeminiService geminiService = new GeminiService();
        private final QuestionDAO questionDAO = new QuestionDAO();
        private final AnswerDAO answerDAO = new AnswerDAO();
        private final AISuggestionDAO suggestionDAO = new AISuggestionDAO();
        private final ExamDAO examDAO = new ExamDAO();
        private final GeneratedExamDAO genDAO = new GeneratedExamDAO();
        private final GeneratedExamQuestionDAO genQDAO = new GeneratedExamQuestionDAO();


        private final boolean isGenerated;
        private final Exam exam;
        private final GeneratedExam genExam;

        public DetailDialog(Frame owner, Exam exam) {
            super(owner, "Chi tiết đề Exam", true);
            this.ownerFrame = owner;
            this.exam = exam;
            this.genExam = null;
            this.isGenerated = false;
            initUI(exam.getExamID());
        }

        public DetailDialog(Frame owner, GeneratedExam gen) {
            super(owner, "Chi tiết Generated Exam", true);
            this.ownerFrame = owner;
            this.exam = null;
            this.genExam = gen;
            this.isGenerated = true;
            initUI(gen.getGeneratedExamID());
        }

        private void initUI(int parentID) {
            setLayout(new BorderLayout());
            JTabbedPane tabs = new JTabbedPane();
            tabs.setBorder(new EmptyBorder(10,10,10,10));

            // Lấy câu hỏi: phân biệt Exam thường và GeneratedExam
            List<Question> questions;
            if (isGenerated) {
                // với GeneratedExam, lấy danh sách liên kết qua GeneratedExamQuestionDAO
                List<GeneratedExamQuestion> geqs = genQDAO.getByGeneratedExamID(parentID);
                questions = new ArrayList<>();
                for (GeneratedExamQuestion geq : geqs) {
                    questions.add(questionDAO.getQuestionByID(geq.getQuestionID()));
                }
            } else {
                // với Exam thường thì như cũ
                questions = questionDAO.getQuestionsByExamID(parentID);
            }

            // Tạo tab cho mỗi câu hỏi
            for (Question q : questions) {
                JPanel panel = new JPanel(new BorderLayout(5,5));
                panel.setBorder(new EmptyBorder(10,10,10,10));

                // 1) Nội dung câu hỏi
                JTextArea txtQ = new JTextArea(q.getContent());
                txtQ.setLineWrap(true);
                txtQ.setWrapStyleWord(true);
                txtQ.setEditable(false);
                panel.add(new JScrollPane(txtQ), BorderLayout.NORTH);

                // 2) Danh sách đáp án
                DefaultListModel<String> mAns = new DefaultListModel<>();
                List<Answer> answers = answerDAO.getAnswersByQuestionID(q.getQuestionID());
                for (Answer a : answers) {
                    String prefix = a.isCorrect() ? "✔ " : "✘ ";
                    mAns.addElement(prefix + a.getAnswerText());
                }
                JList<String> lstAns = new JList<>(mAns);
                lstAns.setBorder(BorderFactory.createTitledBorder("Đáp án"));
                // Sự kiện double-click để sửa đáp án
                lstAns.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2 && !lstAns.isSelectionEmpty()) {
                            int idx = lstAns.getSelectedIndex();
                            Answer selected = answers.get(idx);
                            String oldText = selected.getAnswerText();
                            String newText = JOptionPane.showInputDialog(
                                    DetailDialog.this,
                                    "Sửa nội dung đáp án:",
                                    oldText
                            );
                            if (newText != null && !newText.trim().isEmpty() && !newText.equals(oldText)) {
                                selected.setAnswerText(newText.trim());
                                answerDAO.updateAnswer(selected);
                                String prefix = selected.isCorrect() ? "✔ " : "✘ ";
                                mAns.set(idx, prefix + newText.trim());
                            }
                        }
                    }
                });
                panel.add(new JScrollPane(lstAns), BorderLayout.CENTER);

                // 3) Gợi ý AI
                JTextArea aiArea = new JTextArea();
                aiArea.setLineWrap(true);
                aiArea.setWrapStyleWord(true);
                aiArea.setEditable(false);
                aiArea.setBorder(BorderFactory.createTitledBorder("Gợi ý từ AI"));

                new SwingWorker<Void,Void>() {
                    String hint;
                    @Override protected Void doInBackground() throws Exception {
                        List<AISuggestion> ss = suggestionDAO.getAISuggestionsByQuestionID(q.getQuestionID());
                        if (!ss.isEmpty()) {
                            hint = ss.stream()
                                    .max(Comparator.comparing(AISuggestion::getConfidence))
                                    .get()
                                    .getSuggestedAnswer();
                        } else {
                            hint = geminiService.suggestAnswer(q.getContent());
                            suggestionDAO.addAISuggestion(
                                    new AISuggestion(q.getQuestionID(), hint, 1.0f)
                            );
                        }
                        return null;
                    }
                    @Override protected void done() {
                        aiArea.setText(hint != null ? hint : "Không có gợi ý.");
                    }
                }.execute();

                panel.add(new JScrollPane(aiArea), BorderLayout.SOUTH);
                tabs.addTab("QID " + q.getQuestionID(), panel);
            }

            add(tabs, BorderLayout.CENTER);

            // Nút Sửa, Xóa, Đóng (giữ nguyên như cũ)
            JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            JButton btnEdit   = new JButton("Sửa");
            JButton btnDelete = new JButton("Xóa");
            JButton btnClose  = new JButton("Đóng");

            btnEdit.addActionListener(e -> {
                dispose();
                if (isGenerated) new EditExamDialog(ownerFrame, genExam).setVisible(true);
                else            new EditExamDialog(ownerFrame, exam).setVisible(true);
            });
            btnDelete.addActionListener(e -> {
                int res = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc chắn muốn xóa đề này?",
                        "Xác nhận xóa",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (res == JOptionPane.OK_OPTION) {
                    boolean ok = isGenerated
                            ? genDAO.deleteGeneratedExam(genExam.getGeneratedExamID())
                            : examDAO.deleteExam(exam.getExamID());
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Xóa thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            btnClose.addActionListener(e -> dispose());

            btnP.add(btnEdit);
            btnP.add(btnDelete);
            btnP.add(btnClose);
            add(btnP, BorderLayout.SOUTH);

            setSize(700, 600);
            setLocationRelativeTo(ownerFrame);
        }
    }
}
