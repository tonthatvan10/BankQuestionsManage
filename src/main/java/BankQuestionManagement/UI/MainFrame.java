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
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel = new JPanel(cardLayout);

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

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel lblTitle = new JLabel("🌸 Quản lý ngân hàng đề Tiếng Nhật", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        header.add(lblTitle, BorderLayout.CENTER);

        // Sidebar menu
        DefaultListModel<MenuItem> menuModel = new DefaultListModel<>();
        menuModel.addElement(new MenuItem("Dashboard", 0));
        menuModel.addElement(new MenuItem("Quản lý Đề", 1));
        menuModel.addElement(new MenuItem("Đề đã sinh", 2));
        menuModel.addElement(new MenuItem("Xuất File", 3));
        JList<MenuItem> menuList = new JList<>(menuModel);
        menuList.setCellRenderer(new MenuCellRenderer());
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuList.setFixedCellWidth(160);
        menuList.setBorder(new EmptyBorder(10, 0, 10, 0));
        menuList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cardLayout.show(contentPanel, String.valueOf(menuList.getSelectedValue().getIndex()));
            }
        });
        menuList.setSelectedIndex(0);

        // Content cards
        contentPanel.add(new DashboardPanel(), "0");
        contentPanel.add(new ManageAllExamsPanel(), "1");
        contentPanel.add(new GeneratedExamPanel(), "2");
        contentPanel.add(new ExportPanel(), "3");

        // Main layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(menuList, BorderLayout.WEST);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
    }

    // Sidebar helper classes
    static class MenuItem {
        private String name;
        private int idx;
        public MenuItem(String n, int i) { name = n; idx = i; }
        @Override public String toString() { return name; }
        public int getIndex() { return idx; }
    }

    static class MenuCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            MenuItem item = (MenuItem) value;
            lbl.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            switch (item.getIndex()) {
                case 0: lbl.setIcon(UIManager.getIcon("FileView.homeFolderIcon")); break;
                case 1: lbl.setIcon(UIManager.getIcon("FileView.directoryIcon")); break;
                case 2: lbl.setIcon(UIManager.getIcon("FileView.fileIcon")); break;
                case 3: lbl.setIcon(UIManager.getIcon("FileChooser.detailsViewIcon")); break;
            }
            return lbl;
        }
    }

    // -------- DashboardPanel --------
    static class DashboardPanel extends JPanel {
        private final ExamDAO examDAO = new ExamDAO();
        private final GeneratedExamDAO genDAO = new GeneratedExamDAO();

        public DashboardPanel() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JLabel title = new JLabel("Thống kê chung", SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 24));
            add(title, BorderLayout.NORTH);

            JPanel cards = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            cards.setOpaque(false);
            cards.add(createCard("Tổng số Exam", examDAO.getAllExams().size(),
                    UIManager.getIcon("OptionPane.questionIcon"), new Color(0xAED581)));
            cards.add(createCard("Tổng số GeneratedExam", genDAO.getAllGeneratedExams().size(),
                    UIManager.getIcon("OptionPane.informationIcon"), new Color(0x81D4FA)));
            add(cards, BorderLayout.CENTER);
        }

        private JPanel createCard(String text, int count, Icon icon, Color bg) {
            JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
            card.setBackground(bg);
            card.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            JLabel iconLbl = new JLabel(icon);
            iconLbl.setPreferredSize(new Dimension(24, 24));
            card.add(iconLbl);
            JLabel num = new JLabel(String.valueOf(count));
            num.setFont(new Font("SansSerif", Font.BOLD, 28));
            card.add(num);
            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
            card.add(lbl);
            return card;
        }
    }

    // -------- ManageAllExamsPanel --------
    static class ManageAllExamsPanel extends JPanel {
        private final ExamDAO examDAO = new ExamDAO();
        private final GeneratedExamDAO genDAO = new GeneratedExamDAO();
        private final DefaultTableModel model = new DefaultTableModel(
                new String[]{"Loại", "ID", "Tên", "Ngày Tạo"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        private final JTable table = new JTable(model);

        public ManageAllExamsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            // Toolbar
            JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            JButton addExamBtn = new JButton("Thêm Exam", UIManager.getIcon("FileChooser.newFolderIcon"));
            JButton addGenBtn = new JButton("Thêm GeneratedExam");
            JButton editBtn = new JButton("Sửa");
            JButton deleteBtn = new JButton("Xóa");
            bar.add(addExamBtn);
            bar.add(addGenBtn);
            bar.add(editBtn);
            bar.add(deleteBtn);
            add(bar, BorderLayout.NORTH);

            add(new JScrollPane(table), BorderLayout.CENTER);
            loadData();

            addExamBtn.addActionListener(e -> {
                AddExamDialog dlg = new AddExamDialog((Frame) SwingUtilities.getWindowAncestor(this));
                dlg.setVisible(true);
                loadData();
            });
            addGenBtn.addActionListener(e -> {
                String input = JOptionPane.showInputDialog(this, "Số câu cho đề mới:");
                if (input == null) return;
                try {
                    int count = Integer.parseInt(input.trim());
                    new GeneratedExamService().generateRandomExam(count);
                    loadData();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Giá trị không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
            editBtn.addActionListener(e -> editSelected());
            deleteBtn.addActionListener(e -> deleteSelected());
        }

        private void loadData() {
            model.setRowCount(0);
            for (Exam ex : examDAO.getAllExams()) {
                model.addRow(new Object[]{"Exam", ex.getExamID(), ex.getExamName(), ex.getModifiedDate()});
            }
            for (GeneratedExam ge : genDAO.getAllGeneratedExams()) {
                model.addRow(new Object[]{"Generated", ge.getGeneratedExamID(), ge.getExamName(), ge.getCreatedDate()});
            }
        }

        private void editSelected() {
            int r = table.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Chọn dòng để sửa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String type = (String) model.getValueAt(r, 0);
            int id = (int) model.getValueAt(r, 1);
            if ("Exam".equals(type)) {
                Exam ex = examDAO.getExamById(id);
                new EditExamDialog((Frame) SwingUtilities.getWindowAncestor(this), ex).setVisible(true);
            } else {
                GeneratedExam ge = genDAO.getGeneratedExamByID(id);
                new EditExamDialog((Frame) SwingUtilities.getWindowAncestor(this), ge).setVisible(true);
            }
            loadData();
        }

        private void deleteSelected() {
            int r = table.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Chọn dòng để xóa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa?", "Xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                String type = (String) model.getValueAt(r, 0);
                int id = (int) model.getValueAt(r, 1);
                if ("Exam".equals(type)) examDAO.deleteExam(id);
                else genDAO.deleteGeneratedExam(id);
                loadData();
            }
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

            JToolBar bar = new JToolBar();
            bar.setFloatable(false);
            bar.add(new JLabel("Số câu:"));
            bar.add(countSp);
            bar.add(Box.createHorizontalStrut(10));
            bar.add(createBtn);
            add(bar, BorderLayout.NORTH);

            JSplitPane split = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT,
                    new JScrollPane(genTable),
                    new JScrollPane(qList)
            );
            split.setDividerLocation(300);
            split.setBorder(BorderFactory.createTitledBorder("Generated Exams / Questions"));
            add(split, BorderLayout.CENTER);

            loadGenerated();
            createBtn.addActionListener(e -> {
                try {
                    genService.generateRandomExam((int) countSp.getValue());
                    loadGenerated();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
            genTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && genTable.getSelectedRow() >= 0) {
                    int gid = (int) genModel.getValueAt(genTable.getSelectedRow(), 0);
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

            // Thêm button Xuất File vào cùng hàng với combo và format
            JButton btn = new JButton("Xuất File");
            btn.setIcon(UIManager.getIcon("FileChooser.detailsViewIcon"));
            top.add(Box.createHorizontalStrut(20));
            top.add(btn);

            add(top, BorderLayout.NORTH);
            loadItems();

            btn.addActionListener(e -> doExport());
        }

        private void loadItems() {
            combo.removeAllItems();
            for (Exam e : examDAO.getAllExams()) combo.addItem(e);
            for (GeneratedExam ge : genDAO.getAllGeneratedExams()) combo.addItem(ge);
        }

        private void doExport() {
            Object sel = combo.getSelectedItem();
            if (sel == null) { JOptionPane.showMessageDialog(this,"Chọn đề để xuất","Lỗi",JOptionPane.ERROR_MESSAGE); return; }
            JFileChooser fc = new JFileChooser(); fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            String dir = fc.getSelectedFile().getAbsolutePath();
            new SwingWorker<Void,Void>() {
                Exception ex;
                @Override protected Void doInBackground() {
                    try {
                        if (sel instanceof Exam) {
                            Exam exam = (Exam) sel;
                            if ("DOCX".equals(formatBox.getSelectedItem())) {
                                exporter.exportExamToDocx(exam.getExamID(), dir);
                                exporter.exportExamAnswersToDocx(exam.getExamID(), dir);
                            } else {
                                exporter.exportExamToPdf(exam.getExamID(), dir);
                                exporter.exportExamAnswersToPdf(exam.getExamID(), dir);
                            }
                        } else {
                            GeneratedExam ge = (GeneratedExam) sel;
                            if ("DOCX".equals(formatBox.getSelectedItem())) {
                                exporter.exportGeneratedExamToDocx(ge.getGeneratedExamID(), dir);
                                exporter.exportGeneratedExamAnswersToDocx(ge.getGeneratedExamID(), dir);
                            } else {
                                exporter.exportGeneratedExamToPdf(ge.getGeneratedExamID(), dir);
                                exporter.exportGeneratedExamAnswersToPdf(ge.getGeneratedExamID(), dir);
                            }
                        }
                    } catch (Exception e) { ex = e; }
                    return null;
                }
                @Override protected void done() {
                    if (ex != null) JOptionPane.showMessageDialog(ExportPanel.this,ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);
                    else JOptionPane.showMessageDialog(ExportPanel.this,"Xuất thành công!","OK",JOptionPane.INFORMATION_MESSAGE);
                }
            }.execute();
        }
    }

    // DIALOG thêm Exam + Scan
    static class AddExamDialog extends JDialog {
        private final JTextField nameField = new JTextField(20);
        private final JTextArea descArea = new JTextArea(5, 20);
        private final JTextField imageField = new JTextField(20);
        private final ExamDAO examDAO = new ExamDAO();
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

            JButton bi = new JButton("Chọn");
            bi.addActionListener(e -> choose(imageField));
            c.gridx = 2; p.add(bi, c);
            add(p, BorderLayout.CENTER);

            JButton save = new JButton("Lưu & Scan");
            save.addActionListener(e -> onSave());
            JPanel bp = new JPanel(); bp.add(save);
            add(bp, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(owner);
        }

        private void choose(JTextField f) {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                f.setText(fc.getSelectedFile().getAbsolutePath());
            }
        }

        private void onSave() {
            String name = nameField.getText().trim();
            String desc = descArea.getText().trim();
            String img  = imageField.getText().trim();
            if (name.isEmpty() || img.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên và ảnh không được trống", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new SwingWorker<Void, Void>() {
                Exception ex;
                @Override protected Void doInBackground() {
                    try {
                        int id = examDAO.addExam(new Exam(0, name, desc, img, null, null));
                        gemini.scanAndPopulateExistingExam(id);
                    } catch (Exception e) {
                        ex = e;
                    }
                    return null;
                }
                @Override protected void done() {
                    if (ex != null) {
                        JOptionPane.showMessageDialog(AddExamDialog.this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } else {
                        dispose();
                    }
                }
            }.execute();
        }
    }

    // Assume EditExamDialog is defined elsewhere in the same package
}