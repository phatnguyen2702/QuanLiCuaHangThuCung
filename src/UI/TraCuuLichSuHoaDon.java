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
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import dao.HoaDonDAO;
import entity.HoaDon;

public class TraCuuLichSuHoaDon extends JPanel {

    private static final Color NAVY = new Color(18, 48, 82);
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);
    private static final Color GREEN = new Color(25, 157, 107);
    private static final Color BORDER = new Color(210, 220, 230);
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat MONEY =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private final HoaDonDAO dao = new HoaDonDAO();
    private final Runnable quayLai;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel screens = new JPanel(cardLayout);
    private final InvoiceTableModel invoiceTableModel = new InvoiceTableModel();
    private final DetailTableModel detailTableModel = new DetailTableModel();
    private final JComboBox<String> cboMaHoaDon = new JComboBox<>();
    private final List<String> danhSachMaHoaDon = new ArrayList<>();
    private final JTable tblHoaDon = new JTable(invoiceTableModel);
    private final JTable tblChiTiet = new JTable(detailTableModel);
    private final JLabel lblHoaDonHomNay = new JLabel("0");
    private final JLabel lblTongHoaDon = new JLabel("0");
    private final JLabel lblKhachHangHomNay = new JLabel("0");
    private final JLabel lblDoanhThuHomNay = new JLabel("0 ₫");
    private final JLabel lblMaHoaDon = new JLabel();
    private final JLabel lblNgayLap = new JLabel();
    private final JLabel lblKhachHang = new JLabel();
    private final JLabel lblTrangThai = new JLabel("Đã thanh toán");
    private final JLabel lblMaKhachHang = new JLabel();
    private final JLabel lblTenKhachHang = new JLabel();
    private final JLabel lblSoDienThoai = new JLabel();
    private final JLabel lblThuCung = new JLabel("-");
    private final JLabel lblTongTien = new JLabel();
    private final JLabel lblPhuongThuc = new JLabel();
    private HoaDon hoaDonDangXem;
    private boolean dangCapNhatGoiYMaHoaDon;

    public TraCuuLichSuHoaDon(Runnable quayLai) {
        this.quayLai = quayLai;
        setLayout(new BorderLayout());
        setBackground(LIGHT_BLUE);
        setBorder(BorderFactory.createEmptyBorder(14, 18, 18, 18));
        screens.setOpaque(false);
        screens.add(createListScreen(), "LIST");
        screens.add(createDetailScreen(), "DETAIL");
        add(screens, BorderLayout.CENTER);
    }

    public void hienDanhSach() {
        cardLayout.show(screens, "LIST");
        napDanhSach();
    }

    private JPanel createListScreen() {
        JPanel screen = new JPanel(new BorderLayout(0, 14));
        screen.setOpaque(false);
        screen.add(createTitle("Tra cứu lịch sử hóa đơn"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        content.add(createStatistics(), BorderLayout.NORTH);

        JPanel list = new JPanel(new BorderLayout(0, 10));
        list.setOpaque(false);
        list.add(createSearchBar(), BorderLayout.NORTH);
        configureInvoiceTable();
        list.add(new JScrollPane(tblHoaDon), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        actions.setOpaque(false);
        JButton btnQuayLai = new JButton("Quay lại");
        btnQuayLai.addActionListener(e -> quayLai.run());
        actions.add(btnQuayLai);
        list.add(actions, BorderLayout.SOUTH);
        content.add(list, BorderLayout.CENTER);
        screen.add(content, BorderLayout.CENTER);
        return screen;
    }

    private JPanel createTitle(String text) {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel title = new JLabel(text);
        title.setForeground(NAVY);
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        titlePanel.add(title, BorderLayout.WEST);
        return titlePanel;
    }

    private JPanel createStatistics() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        panel.add(createStatCard("Hóa đơn hôm nay", lblHoaDonHomNay));
        panel.add(createStatCard("Tổng hóa đơn", lblTongHoaDon));
        panel.add(createStatCard("Khách hàng hôm nay", lblKhachHangHomNay));
        panel.add(createStatCard("Doanh thu trong ngày", lblDoanhThuHomNay));
        return panel;
    }

    private JPanel createStatCard(String title, JLabel value) {
        JPanel card = new JPanel(new BorderLayout(4, 3));
        card.setBackground(new Color(245, 247, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(80, 80, 80));
        value.setFont(new Font("Segoe UI", Font.BOLD, 16));
        value.setForeground(NAVY);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(170, 60));
        return card;
    }

    private JPanel createSearchBar() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        JPanel search = new JPanel(new BorderLayout(6, 0));
        search.setOpaque(false);
        cboMaHoaDon.setEditable(true);
        cboMaHoaDon.setToolTipText("Nhập mã hóa đơn hoặc tên khách hàng");
        Component editor = cboMaHoaDon.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            JTextField txtMaHoaDon = (JTextField) editor;
            txtMaHoaDon.addActionListener(e -> timKiem());
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
        search.add(new JLabel("Tìm hóa đơn:"), BorderLayout.WEST);
        search.add(cboMaHoaDon, BorderLayout.CENTER);
        JButton btnTimKiem = new JButton("Xác nhận");
        btnTimKiem.addActionListener(e -> timKiem());
        search.add(btnTimKiem, BorderLayout.EAST);
        panel.add(search, BorderLayout.CENTER);

        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.addActionListener(e -> {
            cboMaHoaDon.getEditor().setItem("");
            napDanhSach();
        });
        panel.add(btnLamMoi, BorderLayout.EAST);
        return panel;
    }

    private String layTuKhoa() {
        Object item = cboMaHoaDon.getEditor().getItem();
        return item == null ? "" : item.toString().trim();
    }

    private void configureInvoiceTable() {
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

    private JPanel createDetailScreen() {
        JPanel screen = new JPanel(new BorderLayout(0, 12));
        screen.setOpaque(false);
        JPanel title = createTitle("Chi tiết hóa đơn");

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        content.add(createInvoiceInformation(), BorderLayout.NORTH);

        configureDetailTable();
        content.add(new JScrollPane(tblChiTiet), BorderLayout.CENTER);
        content.add(createDetailFooter(), BorderLayout.SOUTH);
        screen.add(title, BorderLayout.NORTH);
        screen.add(content, BorderLayout.CENTER);
        return screen;
    }

    private JPanel createInvoiceInformation() {
        JPanel information = new JPanel(new GridLayout(1, 2, 26, 0));
        information.setOpaque(false);
        JPanel invoiceInfo = new JPanel(new GridLayout(0, 2, 8, 6));
        invoiceInfo.setOpaque(false);
        invoiceInfo.add(sectionLabel("Thông tin hóa đơn"));
        invoiceInfo.add(new JLabel());
        addInfo(invoiceInfo, "Mã hóa đơn:", lblMaHoaDon);
        addInfo(invoiceInfo, "Ngày:", lblNgayLap);
        addInfo(invoiceInfo, "Khách hàng:", lblKhachHang);
        addInfo(invoiceInfo, "Trạng thái:", lblTrangThai);
        lblTrangThai.setForeground(GREEN);
        lblTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel customerInfo = new JPanel(new GridLayout(0, 2, 8, 6));
        customerInfo.setOpaque(false);
        customerInfo.add(sectionLabel("Thông tin khách hàng"));
        customerInfo.add(new JLabel());
        addInfo(customerInfo, "Mã khách hàng:", lblMaKhachHang);
        addInfo(customerInfo, "Tên khách hàng:", lblTenKhachHang);
        addInfo(customerInfo, "Số điện thoại:", lblSoDienThoai);
        addInfo(customerInfo, "Thú cưng:", lblThuCung);
        addInfo(customerInfo, "Thanh toán:", lblPhuongThuc);
        information.add(invoiceInfo);
        information.add(customerInfo);
        return information;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(NAVY);
        return label;
    }

    private void addInfo(JPanel panel, String title, JLabel value) {
        panel.add(new JLabel(title));
        panel.add(value);
    }

    private void configureDetailTable() {
        tblChiTiet.setRowHeight(28);
        tblChiTiet.setFillsViewportHeight(true);
        tblChiTiet.setGridColor(new Color(190, 190, 190));
        tblChiTiet.setShowGrid(true);
        tblChiTiet.getTableHeader().setBackground(NAVY);
        tblChiTiet.getTableHeader().setForeground(Color.WHITE);
        tblChiTiet.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblChiTiet.getColumnModel().getColumn(0).setPreferredWidth(48);
        tblChiTiet.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblChiTiet.getColumnModel().getColumn(2).setPreferredWidth(115);
        tblChiTiet.getColumnModel().getColumn(3).setPreferredWidth(240);
        tblChiTiet.getColumnModel().getColumn(4).setPreferredWidth(70);
        tblChiTiet.getColumnModel().getColumn(5).setPreferredWidth(120);
        tblChiTiet.getColumnModel().getColumn(6).setPreferredWidth(135);
    }

    private JPanel createDetailFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        totalPanel.setOpaque(false);
        JLabel totalTitle = new JLabel("Tổng cộng:");
        totalTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalPanel.add(totalTitle);
        totalPanel.add(lblTongTien);
        footer.add(totalPanel, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.setOpaque(false);
        JButton btnXuatHoaDon = new JButton("Xuất hóa đơn");
        btnXuatHoaDon.addActionListener(e -> xuatHoaDon());
        JButton btnQuayLai = new JButton("Quay lại");
        btnQuayLai.addActionListener(e -> cardLayout.show(screens, "LIST"));
        actions.add(btnXuatHoaDon);
        actions.add(btnQuayLai);
        footer.add(actions, BorderLayout.SOUTH);
        return footer;
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

            Component editorComponent = cboMaHoaDon.getEditor().getEditorComponent();
            if (!(editorComponent instanceof JTextField)) {
                return;
            }
            JTextField editor = (JTextField) editorComponent;
            String tuKhoa = editor.getText();
            List<String> goiY = new ArrayList<>();
            String tuKhoaChuanHoa = tuKhoa.toLowerCase(Locale.ROOT);
            for (String ma : danhSachMaHoaDon) {
                if (maCoChuaTatCaKyTu(ma, tuKhoaChuanHoa)) {
                    goiY.add(ma);
                }
            }

            int viTriConTro = editor.getCaretPosition();
            dangCapNhatGoiYMaHoaDon = true;
            try {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                for (String ma : goiY) {
                    model.addElement(ma);
                }
                cboMaHoaDon.setModel(model);
                cboMaHoaDon.setSelectedItem(tuKhoa);
                editor.setText(tuKhoa);
                editor.setCaretPosition(Math.min(viTriConTro, tuKhoa.length()));
            } finally {
                dangCapNhatGoiYMaHoaDon = false;
            }

            if (!tuKhoa.isEmpty() && !goiY.isEmpty()
                    && !(goiY.size() == 1 && goiY.get(0).equalsIgnoreCase(tuKhoa))) {
                cboMaHoaDon.showPopup();
            } else if (cboMaHoaDon.isPopupVisible()) {
                cboMaHoaDon.hidePopup();
            }
        });
    }

    private boolean maCoChuaTatCaKyTu(String ma, String tuKhoaChuanHoa) {
        String maChuanHoa = ma.toLowerCase(Locale.ROOT);
        for (int i = 0; i < tuKhoaChuanHoa.length(); i++) {
            if (maChuanHoa.indexOf(tuKhoaChuanHoa.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }

    private void napDanhSach() {
        try {
            HoaDonDAO.ThongKe thongKe = dao.layThongKe();
            String tuKhoa = layTuKhoa();
            List<HoaDon> hoaDons = dao.timHoaDon(tuKhoa);
            if (tuKhoa.isEmpty()) {
                danhSachMaHoaDon.clear();
                for (HoaDon hoaDon : hoaDons) {
                    danhSachMaHoaDon.add(hoaDon.getMaHoaDon());
                }
            }
            invoiceTableModel.setInvoices(hoaDons);
            capNhatThongKe(thongKe);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
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
            HoaDon fullInvoice = dao.layChiTietHoaDon(selected.getMaHoaDon());
            if (fullInvoice == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Hóa đơn không còn tồn tại trong hệ thống.",
                        "Không tìm thấy hóa đơn",
                        JOptionPane.WARNING_MESSAGE);
                napDanhSach();
                return;
            }
            hoaDonDangXem = fullInvoice;
            hienThongTinChiTiet(fullInvoice);
            cardLayout.show(screens, "DETAIL");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải chi tiết hóa đơn.\n" + e.getMessage(),
                    "Lỗi tra cứu hóa đơn",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hienThongTinChiTiet(HoaDon invoice) {
        lblMaHoaDon.setText(invoice.getMaHoaDon());
        lblNgayLap.setText(formatDate(invoice.getNgayLap()));
        lblKhachHang.setText(invoice.getTenKhachHang());
        lblMaKhachHang.setText(invoice.getMaKhachHang());
        lblTenKhachHang.setText(invoice.getTenKhachHang());
        lblSoDienThoai.setText(invoice.getSoDienThoai());
        lblThuCung.setText(layTenThuCung(invoice));
        lblPhuongThuc.setText(invoice.getTenPhuongThuc());
        lblTongTien.setText(formatMoney(invoice.getTongTien()) + " ₫");
        detailTableModel.setDetails(invoice.getChiTiet());
    }

    private String layTenThuCung(HoaDon invoice) {
        List<String> names = new ArrayList<>();
        for (HoaDon.ChiTiet detail : invoice.getChiTiet()) {
            if ("Thú cưng".equals(detail.getLoaiHang())) {
                names.add(detail.getTenHang());
            }
        }
        return names.isEmpty() ? "-" : String.join(", ", names);
    }

    private void xuatHoaDon() {
        if (hoaDonDangXem == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hóa đơn TXT");
        chooser.setSelectedFile(new java.io.File(hoaDonDangXem.getMaHoaDon() + ".txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp văn bản (*.txt)", "txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path file = chooser.getSelectedFile().toPath();
        if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".txt")) {
            file = file.resolveSibling(file.getFileName() + ".txt");
        }
        if (Files.exists(file)
                && JOptionPane.showConfirmDialog(
                        this,
                        "Tệp đã tồn tại. Bạn có muốn ghi đè không?",
                        "Xác nhận ghi đè",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Files.write(
                    file,
                    taoNoiDungHoaDon(hoaDonDangXem).getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(
                    this,
                    "Đã lưu hóa đơn TXT:\n" + file.toAbsolutePath(),
                    "Xuất hóa đơn thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể lưu hóa đơn.\n" + e.getMessage(),
                    "Lỗi xuất hóa đơn",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String taoNoiDungHoaDon(HoaDon invoice) {
        StringBuilder text = new StringBuilder();
        text.append("PET SHOP PRO\n");
        text.append("HÓA ĐƠN THANH TOÁN\n");
        text.append("Mã hóa đơn: ").append(safe(invoice.getMaHoaDon())).append('\n');
        text.append("Ngày lập: ").append(formatDate(invoice.getNgayLap())).append('\n');
        text.append("Khách hàng: ").append(safe(invoice.getTenKhachHang())).append('\n');
        text.append("Mã khách hàng: ").append(safe(invoice.getMaKhachHang())).append('\n');
        text.append("Số điện thoại: ").append(safe(invoice.getSoDienThoai())).append('\n');
        text.append("Thú cưng: ").append(layTenThuCung(invoice)).append('\n');
        text.append("Thanh toán: ").append(safe(invoice.getTenPhuongThuc())).append('\n');
        text.append("------------------------------------------------------------\n");
        text.append("STT | Mã hàng | Loại | Tên hàng | SL | Đơn giá | Thành tiền\n");
        text.append("------------------------------------------------------------\n");
        List<HoaDon.ChiTiet> details = invoice.getChiTiet();
        for (int index = 0; index < details.size(); index++) {
            HoaDon.ChiTiet item = details.get(index);
            text.append(index + 1).append(" | ")
                    .append(safe(item.getMaHang())).append(" | ")
                    .append(safe(item.getLoaiHang())).append(" | ")
                    .append(safe(item.getTenHang())).append(" | ")
                    .append(item.getSoLuong()).append(" | ")
                    .append(formatMoney(item.getDonGia())).append(" ₫ | ")
                    .append(formatMoney(item.getThanhTien())).append(" ₫\n");
        }
        text.append("------------------------------------------------------------\n");
        text.append("TỔNG CỘNG: ")
                .append(formatMoney(invoice.getTongTien())).append(" ₫\n\n");
        text.append("Cảm ơn quý khách đã mua hàng!");
        return text.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME);
    }

    private static class InvoiceTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
                "MÃ", "THỜI GIAN", "KHÁCH HÀNG", "TỔNG TIỀN",
                "THANH TOÁN", "TRẠNG THÁI", ""
        };
        private List<HoaDon> invoices = new ArrayList<>();

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
                    return formatMoney(invoice.getTongTien()) + " ₫";
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

        private void setInvoices(List<HoaDon> invoices) {
            this.invoices = new ArrayList<>(invoices);
            fireTableDataChanged();
        }
    }

    private static class DetailTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
                "STT", "Mã sản phẩm / Dịch vụ", "Loại", "Sản phẩm / Dịch vụ",
                "Số lượng", "Đơn giá", "Thành tiền"
        };
        private List<HoaDon.ChiTiet> details = new ArrayList<>();

        @Override
        public int getRowCount() {
            return details.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            HoaDon.ChiTiet detail = details.get(row);
            switch (column) {
                case 0:
                    return row + 1;
                case 1:
                    return detail.getMaHang();
                case 2:
                    return detail.getLoaiHang();
                case 3:
                    return detail.getTenHang();
                case 4:
                    return detail.getSoLuong();
                case 5:
                    return formatMoney(detail.getDonGia()) + " ₫";
                case 6:
                    return formatMoney(detail.getThanhTien()) + " ₫";
                default:
                    return "";
            }
        }

        private void setDetails(List<HoaDon.ChiTiet> details) {
            this.details = new ArrayList<>(details);
            fireTableDataChanged();
        }
    }

    private static String formatMoney(BigDecimal amount) {
        return MONEY.format(amount == null ? BigDecimal.ZERO : amount);
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected, boolean focused, int row, int column) {
            Component component = super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(selected ? table.getSelectionForeground() : GREEN);
            setFont(table.getFont().deriveFont(Font.BOLD));
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
                JTable table, Object value, boolean selected, boolean focused, int row, int column) {
            return this;
        }
    }

    private interface RowAction {
        void perform(int row);
    }

    private static class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton("Xem thêm");
        private final JTable table;
        private final RowAction rowAction;
        private int row;

        private ButtonEditor(JTable table, RowAction rowAction) {
            this.table = table;
            this.rowAction = rowAction;
            button.setFocusable(false);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    fireEditingStopped();
                    ButtonEditor.this.rowAction.perform(
                            ButtonEditor.this.table.convertRowIndexToModel(ButtonEditor.this.row));
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean selected, int row, int column) {
            this.row = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Xem thêm";
        }
    }
}
