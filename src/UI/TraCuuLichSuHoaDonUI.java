package UI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import control.HoaDonControl;
import dao.HoaDonDAO;
import entity.HoaDon;

public class TraCuuLichSuHoaDonUI extends JFrame implements ActionListener, MouseListener {

    private static final Color NAVY = new Color(18, 48, 82);
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);
    private static final Color GREEN = new Color(25, 157, 107);
    private static final Color BORDER = new Color(210, 220, 230);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private final HoaDonControl hoaDonControl = new HoaDonControl();
    private final Runnable quayLai;
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout());
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pnlScreens = new JPanel(cardLayout);
    private final ChiTietHoaDonUI chiTietHoaDonUI = new ChiTietHoaDonUI(
            () -> cardLayout.show(pnlScreens, "LIST"));
    private final InvoiceTableModel invoiceTableModel = new InvoiceTableModel();
    private final JComboBox<String> cmbMaHoaDon = new JComboBox<>();
    private final List<String> danhSachMaHoaDon = new ArrayList<>();
    private final JTable tblHoaDon = new JTable(invoiceTableModel);
    private final JLabel lblHoaDonHomNay = new JLabel("0");
    private final JLabel lblTongHoaDon = new JLabel("0");
    private final JLabel lblKhachHangHomNay = new JLabel("0");
    private final JLabel lblDoanhThuHomNay = new JLabel("0 ₫");
    private boolean dangCapNhatGoiYMaHoaDon;

    public TraCuuLichSuHoaDonUI(Runnable quayLai) {
        this.quayLai = quayLai;
        setTitle("Tra cứu lịch sử hóa đơn");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pnlNoiDung.setBackground(LIGHT_BLUE);
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(14, 18, 18, 18));
        pnlScreens.setOpaque(false);
        pnlScreens.add(createListScreen(), "LIST");
        pnlScreens.add(chiTietHoaDonUI, "DETAIL");
        pnlNoiDung.add(pnlScreens, BorderLayout.CENTER);
        setContentPane(pnlNoiDung);
    }

    public JPanel getContentPanel() {
        return pnlNoiDung;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        switch (event.getActionCommand()) {
            case "RETURN_TO_PAYMENT":
                quayLai.run();
                break;
            case "SEARCH_INVOICES":
                timKiem();
                break;
            case "REFRESH_INVOICES":
                cmbMaHoaDon.getEditor().setItem("");
                napDanhSach();
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() != 2) {
            return;
        }
        int row = tblHoaDon.rowAtPoint(event.getPoint());
        int column = tblHoaDon.columnAtPoint(event.getPoint());
        if (row >= 0 && column >= 0 && column != 6) {
            moChiTiet(invoiceTableModel.getInvoice(tblHoaDon.convertRowIndexToModel(row)));
        }
    }

    @Override
    public void mousePressed(MouseEvent event) {
    }

    @Override
    public void mouseReleased(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }

    public void hienDanhSach() {
        cardLayout.show(pnlScreens, "LIST");
        napDanhSach();
    }

    private JPanel createListScreen() {
        JPanel pnlScreen = new JPanel(new BorderLayout(0, 14));
        pnlScreen.setOpaque(false);
        pnlScreen.add(createTitle("Tra cứu lịch sử hóa đơn"), BorderLayout.NORTH);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 12));
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        pnlContent.add(createStatistics(), BorderLayout.NORTH);

        JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 10));
        pnlDanhSach.setOpaque(false);
        pnlDanhSach.add(createSearchBar(), BorderLayout.NORTH);
        configureInvoiceTable();
        pnlDanhSach.add(new JScrollPane(tblHoaDon), BorderLayout.CENTER);
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        pnlActions.setOpaque(false);
        JButton btnQuayLai = new JButton("Quay lại");
        btnQuayLai.setActionCommand("RETURN_TO_PAYMENT");
        btnQuayLai.addActionListener(this);
        pnlActions.add(btnQuayLai);
        pnlDanhSach.add(pnlActions, BorderLayout.SOUTH);
        pnlContent.add(pnlDanhSach, BorderLayout.CENTER);
        pnlScreen.add(pnlContent, BorderLayout.CENTER);
        return pnlScreen;
    }

    private JPanel createTitle(String text) {
        JPanel pnlTitlePanel = new JPanel(new BorderLayout());
        pnlTitlePanel.setOpaque(false);
        JLabel lblTitle2 = new JLabel(text);
        lblTitle2.setForeground(NAVY);
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 25));
        pnlTitlePanel.add(lblTitle2, BorderLayout.WEST);
        return pnlTitlePanel;
    }

    private JPanel createStatistics() {
        JPanel pnlKhung = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlKhung.setOpaque(false);
        pnlKhung.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        pnlKhung.add(createStatCard("Hóa đơn hôm nay", lblHoaDonHomNay));
        pnlKhung.add(createStatCard("Tổng hóa đơn", lblTongHoaDon));
        pnlKhung.add(createStatCard("Khách hàng hôm nay", lblKhachHangHomNay));
        pnlKhung.add(createStatCard("Doanh thu trong ngày", lblDoanhThuHomNay));
        return pnlKhung;
    }

    private JPanel createStatCard(String title, JLabel lblValue) {
        JPanel pnlCard = new JPanel(new BorderLayout(4, 3));
        pnlCard.setBackground(new Color(245, 247, 250));
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        JLabel lblTitleLabel = new JLabel(title);
        lblTitleLabel.setForeground(new Color(80, 80, 80));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValue.setForeground(NAVY);
        pnlCard.add(lblTitleLabel, BorderLayout.NORTH);
        pnlCard.add(lblValue, BorderLayout.CENTER);
        pnlCard.setPreferredSize(new Dimension(170, 60));
        return pnlCard;
    }

    private JPanel createSearchBar() {
        JPanel pnlKhung = new JPanel(new BorderLayout(8, 0));
        pnlKhung.setOpaque(false);
        JPanel pnlSearch = new JPanel(new BorderLayout(6, 0));
        pnlSearch.setOpaque(false);
        cmbMaHoaDon.setEditable(true);
        cmbMaHoaDon.setToolTipText("Nhập mã hóa đơn hoặc tên khách hàng");
        Component editor = cmbMaHoaDon.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            JTextField txtMaHoaDon = (JTextField) editor;
            txtMaHoaDon.setActionCommand("SEARCH_INVOICES");
            txtMaHoaDon.addActionListener(this);
            txtMaHoaDon.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    capNhatGoiYMaHoaDon();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    capNhatGoiYMaHoaDon();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    capNhatGoiYMaHoaDon();
                }
            });
        }
        pnlSearch.add(new JLabel("Tìm mã hóa đơn / Khách hàng:"), BorderLayout.WEST);
        pnlSearch.add(cmbMaHoaDon, BorderLayout.CENTER);
        JButton btnTimKiem = new JButton("Xác nhận");
        btnTimKiem.setActionCommand("SEARCH_INVOICES");
        btnTimKiem.addActionListener(this);
        pnlSearch.add(btnTimKiem, BorderLayout.EAST);
        pnlKhung.add(pnlSearch, BorderLayout.CENTER);

        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setActionCommand("REFRESH_INVOICES");
        btnLamMoi.addActionListener(this);
        pnlKhung.add(btnLamMoi, BorderLayout.EAST);
        return pnlKhung;
    }

    private String layTuKhoa() {
        Object item = cmbMaHoaDon.getEditor().getItem();
        return item == null ? "" : item.toString().trim();
    }

    private void configureInvoiceTable() {
        tblHoaDon.addMouseListener(this);
        tblHoaDon.setRowHeight(30);
        tblHoaDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHoaDon.setFillsViewportHeight(true);
        tblHoaDon.setGridColor(new Color(232, 236, 240));
        tblHoaDon.getTableHeader().setBackground(NAVY);
        tblHoaDon.getTableHeader().setForeground(Color.WHITE);
        tblHoaDon.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblHoaDon.getColumnModel().getColumn(0).setPreferredWidth(95);
        tblHoaDon.getColumnModel().getColumn(1).setPreferredWidth(155);
        tblHoaDon.getColumnModel().getColumn(2).setPreferredWidth(200);
        tblHoaDon.getColumnModel().getColumn(3).setPreferredWidth(120);
        tblHoaDon.getColumnModel().getColumn(4).setPreferredWidth(140);
        tblHoaDon.getColumnModel().getColumn(5).setPreferredWidth(135);
        tblHoaDon.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        tblHoaDon.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(
                tblHoaDon, row -> moChiTiet(invoiceTableModel.getInvoice(row))));
        tblHoaDon.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
    }

    private void timKiem() {
        napDanhSach();
    }

    private void capNhatGoiYMaHoaDon() {
        if (dangCapNhatGoiYMaHoaDon) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (dangCapNhatGoiYMaHoaDon) {
                return;
            }

            Component editorComponent = cmbMaHoaDon.getEditor().getEditorComponent();
            if (!(editorComponent instanceof JTextField)) {
                return;
            }
            JTextField txtEditor = (JTextField) editorComponent;
            String tuKhoa = txtEditor.getText();
            List<String> goiY = new ArrayList<>();
            String tuKhoaChuanHoa = tuKhoa.toLowerCase(Locale.ROOT);
            for (String ma : danhSachMaHoaDon) {
                if (ma.toLowerCase(Locale.ROOT).contains(tuKhoaChuanHoa)) {
                    goiY.add(ma);
                }
            }

            int viTriConTro = txtEditor.getCaretPosition();
            dangCapNhatGoiYMaHoaDon = true;
            try {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                for (String ma : goiY) {
                    model.addElement(ma);
                }
                cmbMaHoaDon.setModel(model);
                cmbMaHoaDon.setSelectedItem(tuKhoa);
                txtEditor.setText(tuKhoa);
                txtEditor.setCaretPosition(Math.min(viTriConTro, tuKhoa.length()));
            } finally {
                dangCapNhatGoiYMaHoaDon = false;
            }

            if (!tuKhoa.isEmpty() && !goiY.isEmpty()
                    && !(goiY.size() == 1 && goiY.get(0).equalsIgnoreCase(tuKhoa))) {
                cmbMaHoaDon.showPopup();
            } else if (cmbMaHoaDon.isPopupVisible()) {
                cmbMaHoaDon.hidePopup();
            }
        });
    }

    private void napDanhSach() {
        try {
            HoaDonDAO.ThongKe thongKe = hoaDonControl.layThongKe();
            String tuKhoa = layTuKhoa();
            List<HoaDon> hoaDons = hoaDonControl.timHoaDon(tuKhoa);
            List<HoaDon> hoaDonGoiY = tuKhoa.isEmpty()
                    ? hoaDons
                    : hoaDonControl.timHoaDon("");
            danhSachMaHoaDon.clear();
            for (HoaDon hoaDon : hoaDonGoiY) {
                danhSachMaHoaDon.add(hoaDon.getMaHoaDon());
            }
            invoiceTableModel.setInvoices(hoaDons, hoaDonControl.layTongTienHoaDon(hoaDons));
            capNhatThongKe(thongKe);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    pnlNoiDung,
                    "Không thể tải danh sách hóa đơn.\n" + e.getMessage(),
                    "Lỗi tra cứu hóa đơn",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void capNhatThongKe(HoaDonDAO.ThongKe thongKe) {
        lblHoaDonHomNay.setText(Integer.toString(thongKe.getHoaDonHomNay()));
        lblTongHoaDon.setText(Integer.toString(thongKe.getTongHoaDon()));
        lblKhachHangHomNay.setText(Integer.toString(thongKe.getKhachHangHomNay()));
        lblDoanhThuHomNay.setText(formatMoney(thongKe.getDoanhThuHomNay()) + " ₫");
    }

    private void moChiTiet(HoaDon selected) {
        if (selected == null) {
            return;
        }
        try {
            HoaDon fullInvoice = hoaDonControl.layChiTietHoaDon(selected.getMaHoaDon());
            if (fullInvoice == null) {
                JOptionPane.showMessageDialog(
                        pnlNoiDung,
                        "Hóa đơn không còn tồn tại trong hệ thống.",
                        "Không tìm thấy hóa đơn",
                        JOptionPane.WARNING_MESSAGE);
                napDanhSach();
                return;
            }
            chiTietHoaDonUI.hienThi(
                    fullInvoice,
                    fullInvoice.chiTiet(),
                    fullInvoice.tinhTongTien());
            cardLayout.show(pnlScreens, "DETAIL");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    pnlNoiDung,
                    "Không thể tải chi tiết hóa đơn.\n" + e.getMessage(),
                    "Lỗi tra cứu hóa đơn",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class InvoiceTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
                "MÃ", "THỜI GIAN", "KHÁCH HÀNG", "TỔNG TIỀN",
                "THANH TOÁN", "TRẠNG THÁI", ""
        };
        private List<HoaDon> invoices = new ArrayList<>();
        private Map<String, Double> tongTienTheoMa = Map.of();

        @Override
        public int getRowCount() {
            return invoices.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 6;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            HoaDon invoice = invoices.get(row);
            switch (column) {
                case 0:
                    return invoice.getMaHoaDon();
                case 1:
                    return invoice.getNgayLap() == null ? "" : invoice.getNgayLap().format(DATE_TIME);
                case 2:
                    return invoice.getTenKhachHang();
                case 3:
                    return formatMoney(tongTienTheoMa.get(invoice.getMaHoaDon())) + " ₫";
                case 4:
                    return invoice.getTenPhuongThuc();
                case 5:
                    return "Đã thanh toán";
                case 6:
                    return "Xem thêm";
                default:
                    return "";
            }
        }

        private HoaDon getInvoice(int row) {
            return row < 0 || row >= invoices.size() ? null : invoices.get(row);
        }

        private void setInvoices(List<HoaDon> invoices, Map<String, Double> tongTienTheoMa) {
            this.invoices = new ArrayList<>(invoices);
            this.tongTienTheoMa = Map.copyOf(tongTienTheoMa);
            fireTableDataChanged();
        }
    }

    private static String formatMoney(double amount) {
        return MONEY.format(amount);
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable tblDuLieu, Object value, boolean selected, boolean focused, int row, int column) {
            Component component = super.getTableCellRendererComponent(
                    tblDuLieu, value, selected, focused, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(selected ? tblDuLieu.getSelectionForeground() : GREEN);
            setFont(tblDuLieu.getFont().deriveFont(Font.BOLD));
            return component;
        }
    }

    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        private ButtonRenderer() {
            setText("Xem thêm");
            setFocusable(false);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable tblDuLieu, Object value, boolean selected, boolean focused, int row, int column) {
            return this;
        }
    }

    private interface RowAction {
        void perform(int row);
    }

    private static class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btnThaoTac = new JButton("Xem thêm");
        private final JTable tblDuLieu;
        private final RowAction rowAction;
        private int row;

        private ButtonEditor(JTable tblDuLieu, RowAction rowAction) {
            this.tblDuLieu = tblDuLieu;
            this.rowAction = rowAction;
            btnThaoTac.setFocusable(false);
            btnThaoTac.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    fireEditingStopped();
                    ButtonEditor.this.rowAction.perform(
                            ButtonEditor.this.tblDuLieu.convertRowIndexToModel(ButtonEditor.this.row));
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable tblDuLieu, Object value, boolean selected, int row, int column) {
            this.row = row;
            return btnThaoTac;
        }

        @Override
        public Object getCellEditorValue() {
            return "Xem thêm";
        }
    }
}
