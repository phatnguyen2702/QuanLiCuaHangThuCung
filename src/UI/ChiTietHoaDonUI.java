package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import entity.ChiTietHoaDon;
import entity.HoaDon;

public class ChiTietHoaDonUI extends JPanel {

    private static final Color NAVY = new Color(18, 48, 82);
    private static final Color GREEN = new Color(25, 157, 107);
    private static final Color BORDER = new Color(210, 220, 230);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    private final Runnable quayLai;
    private final JLabel lblMaHoaDon = new JLabel();
    private final JLabel lblNgayLap = new JLabel();
    private final JLabel lblKhachHang = new JLabel();
    private final JLabel lblNhanVienLap = new JLabel();
    private final JLabel lblTrangThai = new JLabel("Đã thanh toán");
    private final JLabel lblMaKhachHang = new JLabel();
    private final JLabel lblTenKhachHang = new JLabel();
    private final JLabel lblSoDienThoai = new JLabel();
    private final JLabel lblThuCung = new JLabel("-");
    private final JLabel lblTongTien = new JLabel();
    private final JLabel lblGiamGia = new JLabel();
    private final JLabel lblPhuongThuc = new JLabel();
    private final DetailTableModel tableModel = new DetailTableModel();
    private final JTable tblChiTiet = new JTable(tableModel);

    private HoaDon hoaDon;
    private List<ChiTietHoaDon> chiTiet = List.of();
    private double tongTien;

    public ChiTietHoaDonUI(Runnable quayLai) {
        this.quayLai = quayLai;
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);

        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("Chi tiết hóa đơn");
        lblTitle.setForeground(NAVY);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        pnlTitle.add(lblTitle, BorderLayout.WEST);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 10));
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        pnlContent.add(createInvoiceInformation(), BorderLayout.NORTH);
        configureDetailTable();
        pnlContent.add(new JScrollPane(tblChiTiet), BorderLayout.CENTER);
        pnlContent.add(createFooter(), BorderLayout.SOUTH);

        add(pnlTitle, BorderLayout.NORTH);
        add(pnlContent, BorderLayout.CENTER);
    }

    public void hienThi(
            HoaDon hoaDon,
            List<ChiTietHoaDon> chiTiet,
            double tongTien) {
        this.hoaDon = hoaDon;
        this.chiTiet = List.copyOf(chiTiet);
        this.tongTien = tongTien;

        lblMaHoaDon.setText(hoaDon.getMaHoaDon());
        lblNgayLap.setText(formatDate(hoaDon.getNgayLap()));
        lblKhachHang.setText(hoaDon.getTenKhachHang());
        lblNhanVienLap.setText(safe(hoaDon.getTenNhanVien()));
        lblMaKhachHang.setText(hoaDon.getMaKhachHang());
        lblTenKhachHang.setText(hoaDon.getTenKhachHang());
        lblSoDienThoai.setText(hoaDon.getSoDienThoai());
        lblThuCung.setText(layTenThuCung(this.chiTiet));
        lblPhuongThuc.setText(hoaDon.getTenPhuongThuc());
        lblTongTien.setText(formatMoney(this.tongTien) + " ₫");
        lblGiamGia.setText(formatMoney(tinhTongTienGiam()) + " ₫");
        tableModel.setDetails(this.chiTiet);
    }

    private JPanel createInvoiceInformation() {
        JPanel pnlInformation = new JPanel(new GridLayout(1, 2, 26, 0));
        pnlInformation.setOpaque(false);

        JPanel pnlInvoiceInfo = new JPanel(new GridLayout(0, 2, 8, 6));
        pnlInvoiceInfo.setOpaque(false);
        pnlInvoiceInfo.add(sectionLabel("Thông tin hóa đơn"));
        pnlInvoiceInfo.add(new JLabel());
        addInfo(pnlInvoiceInfo, "Mã hóa đơn:", lblMaHoaDon);
        addInfo(pnlInvoiceInfo, "Ngày:", lblNgayLap);
        addInfo(pnlInvoiceInfo, "Khách hàng:", lblKhachHang);
        addInfo(pnlInvoiceInfo, "Nhân viên lập:", lblNhanVienLap);
        addInfo(pnlInvoiceInfo, "Trạng thái:", lblTrangThai);
        lblTrangThai.setForeground(GREEN);
        lblTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel pnlCustomerInfo = new JPanel(new GridLayout(0, 2, 8, 6));
        pnlCustomerInfo.setOpaque(false);
        pnlCustomerInfo.add(sectionLabel("Thông tin khách hàng"));
        pnlCustomerInfo.add(new JLabel());
        addInfo(pnlCustomerInfo, "Mã khách hàng:", lblMaKhachHang);
        addInfo(pnlCustomerInfo, "Tên khách hàng:", lblTenKhachHang);
        addInfo(pnlCustomerInfo, "Số điện thoại:", lblSoDienThoai);
        addInfo(pnlCustomerInfo, "Thú cưng:", lblThuCung);
        addInfo(pnlCustomerInfo, "Thanh toán:", lblPhuongThuc);

        pnlInformation.add(pnlInvoiceInfo);
        pnlInformation.add(pnlCustomerInfo);
        return pnlInformation;
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
        tblChiTiet.getColumnModel().getColumn(4).setPreferredWidth(140);
        tblChiTiet.getColumnModel().getColumn(5).setPreferredWidth(70);
        tblChiTiet.getColumnModel().getColumn(6).setPreferredWidth(120);
        tblChiTiet.getColumnModel().getColumn(7).setPreferredWidth(135);
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JPanel totals = new JPanel(new GridLayout(2, 2, 8, 4));
        totals.setOpaque(false);
        JLabel discountTitle = new JLabel("Giảm:", SwingConstants.RIGHT);
        JLabel totalTitle = new JLabel("Tổng cộng:", SwingConstants.RIGHT);
        lblGiamGia.setHorizontalAlignment(SwingConstants.RIGHT);
        totalTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTongTien.setHorizontalAlignment(SwingConstants.RIGHT);
        totals.add(discountTitle);
        totals.add(lblGiamGia);
        totals.add(totalTitle);
        totals.add(lblTongTien);
        JPanel totalsContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        totalsContainer.setOpaque(false);
        totalsContainer.add(totals);
        footer.add(totalsContainer, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.setOpaque(false);
        JButton exportButton = new JButton("Xuất hóa đơn");
        exportButton.addActionListener(event -> xuatHoaDon());
        JButton backButton = new JButton("Quay lại");
        backButton.addActionListener(event -> quayLai.run());
        actions.add(exportButton);
        actions.add(backButton);
        footer.add(actions, BorderLayout.SOUTH);
        return footer;
    }

    private double tinhTongTienGiam() {
        double tongTruocGiam = 0;
        for (ChiTietHoaDon item : chiTiet) {
            tongTruocGiam += tinhThanhTienChuaGiam(item);
        }
        return Math.max(tongTruocGiam - tongTien, 0);
    }

    private static double tinhThanhTienChuaGiam(ChiTietHoaDon item) {
        return item.getDonGia() * item.getSoLuong();
    }

    private String layTenThuCung(List<ChiTietHoaDon> chiTiet) {
        List<String> names = new ArrayList<>();
        for (ChiTietHoaDon detail : chiTiet) {
            if ("Thú cưng".equals(detail.getLoaiHang())) {
                names.add(detail.getTenHang());
            } else if (detail.getTenThuCung() != null
                    && !names.contains(detail.getTenThuCung())) {
                names.add(detail.getTenThuCung());
            }
        }
        return names.isEmpty() ? "-" : String.join(", ", names);
    }

    private void xuatHoaDon() {
        if (hoaDon == null) {
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu hóa đơn TXT");
        fileChooser.setSelectedFile(new java.io.File(hoaDon.getMaHoaDon() + ".txt"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Tệp văn bản (*.txt)", "txt"));
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path file = fileChooser.getSelectedFile().toPath();
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
            Files.write(file, taoNoiDungHoaDon().getBytes(StandardCharsets.UTF_8));
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

    private String taoNoiDungHoaDon() {
        StringBuilder text = new StringBuilder();
        text.append("PET SHOP PRO\n");
        text.append("Địa chỉ: 12 Nguyễn Văn Bảo, Hạnh Thông, Hồ Chí Minh\n");
        text.append("HÓA ĐƠN THANH TOÁN\n");
        text.append("Mã hóa đơn: ").append(safe(hoaDon.getMaHoaDon())).append('\n');
        text.append("Ngày lập: ").append(formatDate(hoaDon.getNgayLap())).append('\n');
        text.append("Khách hàng: ").append(safe(hoaDon.getTenKhachHang())).append('\n');
        text.append("Nhân viên lập: ").append(safe(hoaDon.getTenNhanVien())).append('\n');
        text.append("Mã khách hàng: ").append(safe(hoaDon.getMaKhachHang())).append('\n');
        text.append("Số điện thoại: ").append(safe(hoaDon.getSoDienThoai())).append('\n');
        text.append("Thú cưng: ").append(layTenThuCung(chiTiet)).append('\n');
        text.append("Thanh toán: ").append(safe(hoaDon.getTenPhuongThuc())).append('\n');
        text.append("------------------------------------------------------------\n");
        text.append("STT | Mã hàng | Loại | Tên hàng | Thú cưng | SL | Đơn giá | Thành tiền\n");
        text.append("------------------------------------------------------------\n");
        for (int index = 0; index < chiTiet.size(); index++) {
            ChiTietHoaDon item = chiTiet.get(index);
            text.append(index + 1).append(" | ")
                    .append(safe(item.getMaHang())).append(" | ")
                    .append(safe(item.getLoaiHang())).append(" | ")
                    .append(safe(item.getTenHang())).append(" | ")
                    .append(safe(item.getTenThuCung())).append(" | ")
                    .append(item.getSoLuong()).append(" | ")
                    .append(formatMoney(item.getDonGia())).append(" ₫ | ")
                    .append(formatMoney(tinhThanhTienChuaGiam(item))).append(" ₫\n");
        }
        text.append("------------------------------------------------------------\n");
        text.append("TỔNG CỘNG: ").append(formatMoney(tongTien)).append(" ₫\n\n");
        text.append("Cảm ơn quý khách đã mua hàng!");
        return text.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME);
    }

    private static String formatMoney(double amount) {
        return MONEY.format(amount);
    }

    private static class DetailTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
                "STT", "Mã sản phẩm / Dịch vụ", "Loại", "Sản phẩm / Dịch vụ", "Thú cưng",
                "Số lượng", "Đơn giá", "Thành tiền"
        };
        private List<ChiTietHoaDon> details = List.of();

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
            ChiTietHoaDon detail = details.get(row);
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
                    return detail.getTenThuCung() == null ? "" : detail.getTenThuCung();
                case 5:
                    return detail.getSoLuong();
                case 6:
                    return formatMoney(detail.getDonGia()) + " ₫";
                case 7:
                    return formatMoney(tinhThanhTienChuaGiam(detail)) + " ₫";
                default:
                    return "";
            }
        }

        private void setDetails(List<ChiTietHoaDon> details) {
            this.details = List.copyOf(details);
            fireTableDataChanged();
        }
    }
}
