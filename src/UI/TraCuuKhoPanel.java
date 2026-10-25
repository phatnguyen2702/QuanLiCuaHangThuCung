package UI;

import ConnectDB.connectDB;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Tra cứu phiếu nhập kho hoặc phiếu kiểm kê kho. */
public class TraCuuKhoPanel extends JPanel {
    private static final Color NAVY = new Color(18, 48, 82);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final boolean kiemKe;
    private final Runnable quayLai;
    private final JTextField tuKhoa = new JTextField(20);
    private final JTextField tuNgay = new JTextField(10);
    private final JTextField denNgay = new JTextField(10);
    private final JComboBox<String> trangThai = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đã hoàn tất", "Phiếu nháp"});
    private final JComboBox<String> nhaCungCap = new JComboBox<>(new String[]{"Tất cả nhà cung cấp"});
    private final JComboBox<String> ketQuaKiemKe = new JComboBox<>(new String[]{"Tất cả kết quả", "Có chênh lệch", "Không chênh lệch", "Chưa kiểm đủ"});
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<String[]> ketQua = new ArrayList<>();

    public TraCuuKhoPanel(boolean kiemKe, Runnable quayLai) {
        this.kiemKe = kiemKe;
        this.quayLai = quayLai;
        setLayout(new BorderLayout(0, 12));
        setBackground(new Color(229, 241, 252));
        setBorder(new EmptyBorder(20, 24, 20, 24));
        JLabel title = new JLabel(kiemKe ? "Tra cứu kiểm kê kho" : "Tra cứu nhập kho");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(NAVY);
        add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(14, 14, 14, 14));
        content.add(taoBoLoc(), BorderLayout.NORTH);
        table.setRowHeight(29);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) xemChiTiet();
            }
        });
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton detail = new JButton("Xem chi tiết");
        detail.addActionListener(e -> xemChiTiet());
        JButton back = new JButton("Quay lại");
        back.addActionListener(e -> quayLai.run());
        actions.add(detail);
        actions.add(back);
        content.add(actions, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        if (kiemKe) {
            trangThai.setVisible(false);
            nhaCungCap.setVisible(false);
        } else {
            nhaCungCap.setPreferredSize(new Dimension(180, 26));
        }
        model.setColumnIdentifiers(kiemKe
                ? new String[]{"Mã phiếu", "Ngày kiểm kê", "Mã NV lập", "Trạng thái", "Số mặt hàng", "Kết quả"}
                : new String[]{"Mã phiếu", "Ngày nhập", "Mã NV lập", "Mã NCC", "Nhà cung cấp", "Số mặt hàng", "Tổng tiền", "Trạng thái"});
        tuKhoa.addActionListener(e -> timKiem());
        napNhaCungCap();
        timKiem();
    }

    private JPanel taoBoLoc() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 5, 4, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        them(panel, c, 0, 0, new JLabel("Từ khóa (mã phiếu, nhân viên, sản phẩm)"));
        them(panel, c, 1, 0, new JLabel("Từ ngày (dd/MM/yyyy)"));
        them(panel, c, 2, 0, new JLabel("Đến ngày (dd/MM/yyyy)"));
        them(panel, c, 3, 0, new JLabel(kiemKe ? "Kết quả kiểm kê" : "Trạng thái"));
        them(panel, c, 0, 1, tuKhoa);
        them(panel, c, 1, 1, tuNgay);
        them(panel, c, 2, 1, denNgay);
        them(panel, c, 3, 1, kiemKe ? ketQuaKiemKe : trangThai);
        if (!kiemKe) {
            them(panel, c, 4, 0, new JLabel("Nhà cung cấp"));
            them(panel, c, 4, 1, nhaCungCap);
        }
        JButton search = new JButton("Tìm kiếm");
        search.addActionListener(e -> timKiem());
        JButton reset = new JButton("Làm mới");
        reset.addActionListener(e -> {
            tuKhoa.setText(""); tuNgay.setText(""); denNgay.setText("");
            trangThai.setSelectedIndex(0); ketQuaKiemKe.setSelectedIndex(0); nhaCungCap.setSelectedIndex(0);
            timKiem();
        });
        them(panel, c, 5, 1, search);
        them(panel, c, 6, 1, reset);
        return panel;
    }

    private void them(JPanel panel, GridBagConstraints c, int x, int y, Component component) {
        c.gridx = x; c.gridy = y; c.weightx = component instanceof JTextField ? 1 : 0;
        panel.add(component, c);
    }

    private void napNhaCungCap() {
        if (kiemKe) return;
        try (Connection c = connectDB.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT maNhaCungCap,tenNhaCungCap FROM Nhacungcap ORDER BY tenNhaCungCap");
             ResultSet r = p.executeQuery()) {
            while (r.next()) nhaCungCap.addItem(r.getString(1) + " - " + r.getString(2));
        } catch (SQLException e) { baoLoi(e); }
    }

    private void timKiem() {
        try {
            LocalDate from = parseDate(tuNgay.getText()), to = parseDate(denNgay.getText());
            if (from != null && to != null && from.isAfter(to)) {
                JOptionPane.showMessageDialog(this, "Từ ngày phải trước hoặc bằng đến ngày.", "Bộ lọc ngày", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sql = kiemKe
                    ? "SELECT p.maPhieuKiemKe,p.ngayKiemKe,p.maNhanVien,p.trangThai,COUNT(d.maSanPham),SUM(CASE WHEN d.soLuongThucTe IS NOT NULL AND d.soLuongThucTe<>d.soLuongHeThong THEN 1 ELSE 0 END),MAX(CASE WHEN d.soLuongThucTe IS NULL THEN 1 ELSE 0 END) FROM PhieuKiemKe p LEFT JOIN ChiTietPhieuKiemKe d ON d.maPhieuKiemKe=p.maPhieuKiemKe WHERE ISNULL(p.trangThai,0)=1 GROUP BY p.maPhieuKiemKe,p.ngayKiemKe,p.maNhanVien,p.trangThai ORDER BY p.ngayKiemKe DESC"
                    : "SELECT p.maPhieuNhap,p.ngayNhap,p.maNhanVien,p.maNhaCungCap,n.tenNhaCungCap,COUNT(d.maSanPham),ISNULL(SUM(d.soLuongNhap*d.donGiaNhap),0),p.trangThai FROM PhieuNhapKho p LEFT JOIN Nhacungcap n ON n.maNhaCungCap=p.maNhaCungCap LEFT JOIN ChiTietPhieuNhap d ON d.maPhieuNhap=p.maPhieuNhap GROUP BY p.maPhieuNhap,p.ngayNhap,p.maNhanVien,p.maNhaCungCap,n.tenNhaCungCap,p.trangThai ORDER BY p.ngayNhap DESC";
            List<String[]> result = new ArrayList<>();
            try (Connection c = connectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    String id = r.getString(1), staff = r.getString(3);
                    String status = r.getBoolean(kiemKe ? 4 : 8) ? "Đã chốt" : "Phiếu nháp";
                    Timestamp ts = r.getTimestamp(2);
                    LocalDate date = ts == null ? null : ts.toLocalDateTime().toLocalDate();
                    String supplier = kiemKe ? "" : r.getString(5);
                    int count = r.getInt(kiemKe ? 5 : 6);
                    String delta = kiemKe ? (r.getInt(6) > 0 ? "Có chênh lệch" : r.getInt(7) > 0 ? "Chưa kiểm đủ" : "Không chênh lệch") : "";
                    String keyword = tuKhoa.getText().trim().toLowerCase(Locale.ROOT);
                    boolean match = keyword.isEmpty() || (id + " " + staff + " " + r.getString(kiemKe ? 3 : 4) + supplier).toLowerCase(Locale.ROOT).contains(keyword);
                    if (!match && !keyword.isEmpty()) match = timTrongNhanVien(staff, keyword) || timTrongSanPham(id, keyword);
                    if (!match || from != null && (date == null || date.isBefore(from)) || to != null && (date == null || date.isAfter(to))) continue;
                    if (!kiemKe) {
                        if (trangThai.getSelectedIndex() == 1 && !status.equals("Đã chốt") || trangThai.getSelectedIndex() == 2 && !status.equals("Phiếu nháp")) continue;
                        if (nhaCungCap.getSelectedIndex() > 0 && !Objects.equals(nhaCungCap.getSelectedItem().toString().split(" - ", 2)[0], r.getString(4))) continue;
                        result.add(new String[]{id, ts == null ? "" : DATE_TIME.format(ts.toLocalDateTime()), staff, r.getString(4), supplier, String.valueOf(count), String.format(Locale.forLanguageTag("vi-VN"), "%,.0f ₫", r.getDouble(7)), status});
                    } else {
                        int filter = ketQuaKiemKe.getSelectedIndex();
                        if (filter == 1 && !delta.equals("Có chênh lệch") || filter == 2 && !delta.equals("Không chênh lệch") || filter == 3 && !delta.equals("Chưa kiểm đủ")) continue;
                        result.add(new String[]{id, ts == null ? "" : DATE_TIME.format(ts.toLocalDateTime()), staff, status, String.valueOf(count), delta});
                    }
                }
            }
            ketQua = result;
            model.setRowCount(0);
            for (String[] row : ketQua) model.addRow(row);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Ngày không hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) { baoLoi(e); }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return LocalDate.parse(value.trim(), DateTimeFormatter.ofPattern("dd/MM/uuuu")); }
        catch (Exception e) { throw new IllegalArgumentException("Ngày phải đúng định dạng dd/MM/yyyy."); }
    }

    private boolean timTrongNhanVien(String ma, String keyword) throws SQLException {
        try (Connection c = connectDB.getConnection(); PreparedStatement p = c.prepareStatement("SELECT hoTen FROM NhanVien WHERE maNhanVien=?")) {
            p.setString(1, ma);
            try (ResultSet r = p.executeQuery()) { return r.next() && r.getString(1) != null && r.getString(1).toLowerCase(Locale.ROOT).contains(keyword); }
        }
    }

    private boolean timTrongSanPham(String maPhieu, String keyword) throws SQLException {
        String sql = kiemKe
                ? "SELECT TOP 1 1 FROM ChiTietPhieuKiemKe d JOIN SanPham s ON s.maSanPham=d.maSanPham WHERE d.maPhieuKiemKe=? AND (LOWER(d.maSanPham) LIKE ? OR LOWER(s.tenSanPham) LIKE ?)"
                : "SELECT TOP 1 1 FROM ChiTietPhieuNhap d JOIN SanPham s ON s.maSanPham=d.maSanPham WHERE d.maPhieuNhap=? AND (LOWER(d.maSanPham) LIKE ? OR LOWER(s.tenSanPham) LIKE ?)";
        try (Connection c = connectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maPhieu); p.setString(2, "%" + keyword + "%"); p.setString(3, "%" + keyword + "%");
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        }
    }

    private void xemChiTiet() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn một phiếu để xem chi tiết."); return; }
        String id = String.valueOf(model.getValueAt(row, 0));
        String sql = kiemKe
                ? "SELECT d.maSanPham,s.tenSanPham,d.soLuongHeThong,d.soLuongThucTe FROM ChiTietPhieuKiemKe d JOIN SanPham s ON s.maSanPham=d.maSanPham WHERE d.maPhieuKiemKe=? ORDER BY d.maSanPham"
                : "SELECT d.maSanPham,s.tenSanPham,d.soLuongNhap,d.donGiaNhap,d.hanSuDung FROM ChiTietPhieuNhap d JOIN SanPham s ON s.maSanPham=d.maSanPham WHERE d.maPhieuNhap=? ORDER BY d.maSanPham";
        DefaultTableModel detail = new DefaultTableModel(kiemKe
                ? new String[]{"Mã SP", "Tên sản phẩm", "Tồn hệ thống", "Tồn thực tế", "Chênh lệch", "Kết quả"}
                : new String[]{"Mã SP", "Tên sản phẩm", "Số lượng nhập", "Đơn giá", "Thành tiền", "Hạn sử dụng"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        try (Connection c = connectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, id);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    if (kiemKe) {
                        int system = r.getInt(3); Object raw = r.getObject(4);
                        Integer actual = raw == null ? null : ((Number) raw).intValue();
                        detail.addRow(new Object[]{r.getString(1), r.getString(2), system, actual == null ? "Chưa kiểm" : actual,
                                actual == null ? "—" : actual - system, actual == null ? "Chưa kiểm" : actual == system ? "Khớp" : "Chênh lệch"});
                    } else {
                        int quantity = r.getInt(3); double price = r.getDouble(4);
                        detail.addRow(new Object[]{r.getString(1), r.getString(2), quantity,
                                String.format(Locale.forLanguageTag("vi-VN"), "%,.0f ₫", price),
                                String.format(Locale.forLanguageTag("vi-VN"), "%,.0f ₫", quantity * price), r.getString(5)});
                    }
                }
            }
            JTable detailTable = new JTable(detail);
            detailTable.setRowHeight(28);
            detailTable.setFillsViewportHeight(true);
            JScrollPane scroll = new JScrollPane(detailTable);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            scroll.setPreferredSize(new Dimension(
                    Math.min(950, (int) (screen.width * 0.85)),
                    Math.min(520, (int) (screen.height * 0.72))));
            JOptionPane.showMessageDialog(this, scroll, "Chi tiết phiếu " + id, JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) { baoLoi(e); }
    }

    private void baoLoi(Exception e) {
        JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu kho.\n" + e.getMessage(), "Lỗi tra cứu", JOptionPane.ERROR_MESSAGE);
    }

    public void lamMoi() { timKiem(); }
}
