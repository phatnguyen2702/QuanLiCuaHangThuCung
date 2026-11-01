package UI;

import ConnectDB.connectDB;
import dao.NhapKhoDAO;
import entity.PhieuNhapKho;
import util.PhieuNhapPdfExporter;
import entity.ChiTietPhieuNhap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Tra cứu phiếu nhập kho hoặc phiếu kiểm kê kho. */
public class TraCuuKhoPanel extends JFrame implements ActionListener, MouseListener {
    @Override public void actionPerformed(ActionEvent e) { }
    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mousePressed(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
    // NAVY: Màu dùng cho giao diện: xanh đậm.
    private static final Color NAVY = new Color(18, 48, 82);
    // DATE_TIME: Bộ định dạng ngày/giờ theo mẫu dd/MM/yyyy HH:mm để chuyển giữa dữ
    // liệu thời gian và chuỗi.
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    // kiemKe: Cờ cho biết kiểm kê.
    private final boolean kiemKe;
    // txtTuKhoa: Ô nhập/hiển thị văn bản từ khóa tìm kiếm.
    private final JTextField txtTuKhoa = new JTextField(20);
    // txtTuNgay: Ô nhập/hiển thị văn bản ngày bắt đầu bộ lọc.
    private final JTextField txtTuNgay = new JTextField(10);
    // txtDenNgay: Ô nhập/hiển thị văn bản ngày kết thúc bộ lọc.
    private final JTextField txtDenNgay = new JTextField(10);
    // cmbTrangThai2: Hộp lựa chọn trạng thái.
    private final JComboBox<String> cmbTrangThai2 = new JComboBox<>(
            new String[] { "Tất cả trạng thái", "Đã hoàn tất", "Phiếu nháp" });
    // cmbNhaCungCap2: Hộp lựa chọn nhà cung cấp.
    private final JComboBox<String> cmbNhaCungCap2 = new JComboBox<>(new String[] { "Tất cả nhà cung cấp" });
    // cmbKetQuaKiemKe: Hộp lựa chọn kết quả kiểm kê.
    private final JComboBox<String> cmbKetQuaKiemKe = new JComboBox<>(
            new String[] { "Tất cả kết quả", "Có chênh lệch", "Không chênh lệch", "Chưa kiểm đủ" });
    // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private final DefaultTableModel model = new DefaultTableModel() {
        // Không cho phép sửa trực tiếp các ô trong bảng.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: false, bảng chỉ dùng để xem.
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    // tblDuLieu: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
    private final JTable tblDuLieu = new JTable(model);
    // ketQua: Lưu kết quả; phần tử có kiểu String[].
    private List<String[]> ketQua = new ArrayList<>();

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    // Tham số kiemKe: kiểm kê.
    // Tham số quayLai: quay lại màn hình trước.
    public TraCuuKhoPanel(boolean kiemKe, Runnable quayLai) {
        this.kiemKe = kiemKe;
        setLayout(new BorderLayout(0, 12));
        getContentPane().setBackground(new Color(229, 241, 252));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(20, 24, 20, 24));
        // lblTitle2: Nhãn hiển thị tiêu đề.
        JLabel lblTitle2 = new JLabel(kiemKe ? "Tra cứu kiểm kê kho" : "Tra cứu nhập kho");
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblTitle2.setForeground(NAVY);
        add(lblTitle2, BorderLayout.NORTH);

        // pnlContent: Khung chứa thành phần giao diện nội dung.
        JPanel pnlContent = new JPanel(new BorderLayout(0, 10));
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new EmptyBorder(14, 14, 14, 14));
        pnlContent.add(taoBoLoc(), BorderLayout.NORTH);
        tblDuLieu.setRowHeight(29);
        tblDuLieu.setFillsViewportHeight(true);
        tblDuLieu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDuLieu.getTableHeader().setBackground(NAVY);
        tblDuLieu.getTableHeader().setForeground(Color.WHITE);
        tblDuLieu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDuLieu.addMouseListener(new java.awt.event.MouseAdapter() {
            // Xử lý thao tác nhấn chuột trên thành phần đã gắn listener.
            // Tham số e: thông tin sự kiện.
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2)
                    xemChiTiet();
            }
        });
        pnlContent.add(new JScrollPane(tblDuLieu), BorderLayout.CENTER);

        // pnlActions: Khung chứa thành phần giao diện thao tác.
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlActions.setOpaque(false);
        // btnDetail: Nút thao tác: Xem chi tiết.
        JButton btnDetail = new JButton("Xem chi tiết");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnDetail.addActionListener(e -> xemChiTiet());
        // btnBack: Nút thao tác: Quay lại.
        JButton btnBack = new JButton("Quay lại");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnBack.addActionListener(e -> quayLai.run());
        pnlActions.add(btnDetail);
        pnlActions.add(btnBack);
        pnlContent.add(pnlActions, BorderLayout.SOUTH);
        add(pnlContent, BorderLayout.CENTER);

        if (kiemKe) {
            cmbTrangThai2.setVisible(false);
            cmbNhaCungCap2.setVisible(false);
        } else {
            cmbNhaCungCap2.setPreferredSize(new Dimension(180, 26));
        }
        model.setColumnIdentifiers(kiemKe
                ? new String[] { "Mã phiếu", "Ngày chốt", "Mã NV lập", "Trạng thái", "Số mặt hàng", "Kết quả" }
                : new String[] { "Mã phiếu", "Ngày nhập", "Mã NV lập", "Mã NCC", "Nhà cung cấp", "Số mặt hàng",
                        "Tổng tiền", "Trạng thái" });
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        txtTuKhoa.addActionListener(e -> timKiem());
        napNhaCungCap();
        timKiem();
    }

    // Tạo bỏ lọc.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoBoLoc() {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new GridBagLayout());
        pnlKhung.setOpaque(false);
        // c: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng
        // GridBagLayout.
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 5, 4, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        them(pnlKhung, c, 0, 0, new JLabel("Từ khóa (mã phiếu, nhân viên, sản phẩm)"));
        them(pnlKhung, c, 1, 0, new JLabel("Từ ngày (dd/MM/yyyy)"));
        them(pnlKhung, c, 2, 0, new JLabel("Đến ngày (dd/MM/yyyy)"));
        them(pnlKhung, c, 3, 0, new JLabel(kiemKe ? "Kết quả kiểm kê" : "Trạng thái"));
        them(pnlKhung, c, 0, 1, txtTuKhoa);
        them(pnlKhung, c, 1, 1, taoOChonNgay(txtTuNgay));
        them(pnlKhung, c, 2, 1, taoOChonNgay(txtDenNgay));
        them(pnlKhung, c, 3, 1, kiemKe ? cmbKetQuaKiemKe : cmbTrangThai2);
        if (!kiemKe) {
            them(pnlKhung, c, 4, 0, new JLabel("Nhà cung cấp"));
            them(pnlKhung, c, 4, 1, cmbNhaCungCap2);
        }
        // btnSearch: Nút thao tác: Tìm kiếm.
        JButton btnSearch = new JButton("Tìm kiếm");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnSearch.addActionListener(e -> timKiem());
        // btnReset: Nút thao tác: Làm mới.
        JButton btnReset = new JButton("Làm mới");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnReset.addActionListener(e -> {
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtTuKhoa.setText("");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtTuNgay.setText("");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtDenNgay.setText("");
            cmbTrangThai2.setSelectedIndex(0);
            cmbKetQuaKiemKe.setSelectedIndex(0);
            cmbNhaCungCap2.setSelectedIndex(0);
            timKiem();
        });
        them(pnlKhung, c, 5, 1, btnSearch);
        them(pnlKhung, c, 6, 1, btnReset);
        return pnlKhung;
    }

    // Thêm thành phần vào khung giao diện và bố trí vị trí hiển thị.
    // Tham số pnlKhung: khung.
    // Tham số c: cột/thành phần được xử lý.
    // Tham số x: tọa độ ngang.
    // Tham số y: tọa độ dọc.
    // Tham số component: thành phần giao diện.
    private void them(JPanel pnlKhung, GridBagConstraints c, int x, int y, Component component) {
        c.gridx = x;
        c.gridy = y;
        c.weightx = component instanceof JTextField || component instanceof JPanel ? 1 : 0;
        pnlKhung.add(component, c);
    }

    private JPanel taoOChonNgay(JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.setOpaque(false);
        JButton button = new JButton("Chọn ngày");
        button.setToolTipText("Mở lịch để chọn ngày");
        button.addActionListener(e -> hienLich(field, button));
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    private void hienLich(JTextField field, Component anchor) {
        LocalDate selected = null;
        try {
            if (!field.getText().trim().isEmpty()) selected = parseDate(field.getText());
        } catch (IllegalArgumentException ignored) { }
        NhapKhoPanel.LichChonNgayPopup.mo(anchor, selected, date -> field.setText(
                date == null ? "" : date.format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))));
    }

    // Nạp nhà cung cấp.
    private void napNhaCungCap() {
        if (kiemKe)
            return;
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection c = connectDB.getConnection();
                PreparedStatement p = c
                        .prepareStatement("SELECT maNhaCungCap,tenNhaCungCap FROM Nhacungcap ORDER BY tenNhaCungCap");
                ResultSet r = p.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (r.next())
                cmbNhaCungCap2.addItem(r.getString(1) + " - " + r.getString(2));
        } catch (SQLException e) {
            baoLoi(e);
        }
    }

    // Tìm kiểm.
    private void timKiem() {
        try {
            if (kiemKe) damBaoCotThoiDiemChot();
            // from: Lưu chỉ số bắt đầu dưới dạng dữ liệu thời gian.
            // to: Lưu chỉ số kết thúc dưới dạng dữ liệu thời gian.
            LocalDate from = parseDate(txtTuNgay.getText()), to = parseDate(txtDenNgay.getText());
            if (from != null && to != null && from.isAfter(to)) {
                JOptionPane.showMessageDialog(this, "Từ ngày phải trước hoặc bằng đến ngày.", "Bộ lọc ngày",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
            // tham số nếu có.
            String sql = kiemKe
                    ? "SELECT p.maPhieuKiemKe,COALESCE(p.ngayChot,p.ngayKiemKe),p.maNhanVien,p.trangThai,COUNT(d.maSanPham),SUM(CASE WHEN d.soLuongThucTe IS NOT NULL AND d.soLuongThucTe<>d.soLuongHeThong THEN 1 ELSE 0 END),MAX(CASE WHEN d.soLuongThucTe IS NULL THEN 1 ELSE 0 END) FROM PhieuKiemKe p LEFT JOIN ChiTietPhieuKiemKe d ON d.maPhieuKiemKe=p.maPhieuKiemKe WHERE ISNULL(p.trangThai,0)=1 GROUP BY p.maPhieuKiemKe,COALESCE(p.ngayChot,p.ngayKiemKe),p.maNhanVien,p.trangThai ORDER BY COALESCE(p.ngayChot,p.ngayKiemKe) DESC"
                    : "SELECT p.maPhieuNhap,p.ngayNhap,p.maNhanVien,p.maNhaCungCap,n.tenNhaCungCap,COUNT(d.maSanPham),ISNULL(SUM(d.soLuongNhap*d.donGiaNhap),0),p.trangThai FROM PhieuNhapKho p LEFT JOIN Nhacungcap n ON n.maNhaCungCap=p.maNhaCungCap LEFT JOIN ChiTietPhieuNhap d ON d.maPhieuNhap=p.maPhieuNhap GROUP BY p.maPhieuNhap,p.ngayNhap,p.maNhanVien,p.maNhaCungCap,n.tenNhaCungCap,p.trangThai ORDER BY p.ngayNhap DESC";
            // result: Lưu kết quả; phần tử có kiểu String[].
            List<String[]> result = new ArrayList<>();
            // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
            // lệnh.
            try (Connection c = connectDB.getConnection();
                    PreparedStatement p = c.prepareStatement(sql);
                    ResultSet r = p.executeQuery()) {
                // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
                while (r.next()) {
                    String id = r.getString(1), staff = r.getString(3);
                    String status = r.getBoolean(kiemKe ? 4 : 8)
                            ? (kiemKe ? "Đã chốt" : "Đã hoàn tất")
                            : "Phiếu nháp";
                    // ts: Lưu thời điểm dưới dạng dữ liệu thời gian.
                    Timestamp ts = r.getTimestamp(2);
                    // date: Lưu ngày dưới dạng dữ liệu thời gian.
                    LocalDate date = ts == null ? null : ts.toLocalDateTime().toLocalDate();
                    String supplier = kiemKe ? "" : r.getString(5);
                    // count: Lưu số phần tử.
                    int count = r.getInt(kiemKe ? 5 : 6);
                    String delta = kiemKe ? (r.getInt(6) > 0 ? "Có chênh lệch"
                            : r.getInt(7) > 0 ? "Chưa kiểm đủ" : "Không chênh lệch") : "";
                    // keyword: Đọc nội dung chữ từ ô nhập để xử lý từ khóa tìm kiếm.
                    String keyword = txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);
                    boolean match = keyword.isEmpty()
                            || (id + " " + staff + " " + r.getString(kiemKe ? 3 : 4) + supplier)
                                    .toLowerCase(Locale.ROOT).contains(keyword);
                    if (!match && !keyword.isEmpty())
                        match = timTrongNhanVien(staff, keyword) || timTrongSanPham(id, keyword);
                    if (!match || from != null && (date == null || date.isBefore(from))
                            || to != null && (date == null || date.isAfter(to)))
                        continue;
                    if (!kiemKe) {
                        if (cmbTrangThai2.getSelectedIndex() == 1 && !status.equals("Đã hoàn tất")
                                || cmbTrangThai2.getSelectedIndex() == 2 && !status.equals("Phiếu nháp"))
                            continue;
                        if (cmbNhaCungCap2.getSelectedIndex() > 0 && !Objects
                                .equals(cmbNhaCungCap2.getSelectedItem().toString().split(" - ", 2)[0], r.getString(4)))
                            continue;
                        result.add(new String[] { id, ts == null ? "" : DATE_TIME.format(ts.toLocalDateTime()), staff,
                                r.getString(4), supplier, String.valueOf(count),
                                String.format(Locale.forLanguageTag("vi-VN"), "%,.0f ₫", r.getDouble(7)), status });
                    } else {
                        int filter = cmbKetQuaKiemKe.getSelectedIndex();
                        if (filter == 1 && !delta.equals("Có chênh lệch")
                                || filter == 2 && !delta.equals("Không chênh lệch")
                                || filter == 3 && !delta.equals("Chưa kiểm đủ"))
                            continue;
                        result.add(new String[] { id, ts == null ? "" : DATE_TIME.format(ts.toLocalDateTime()), staff,
                                status, String.valueOf(count), delta });
                    }
                }
            }
            ketQua = result;
            model.setRowCount(0);
            for (String[] row : ketQua)
                model.addRow(row);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Ngày không hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            baoLoi(e);
        }
    }

    private void damBaoCotThoiDiemChot() throws SQLException {
        try (Connection connection = connectDB.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("IF COL_LENGTH('dbo.PhieuKiemKe','ngayChot') IS NULL "
                    + "ALTER TABLE dbo.PhieuKiemKe ADD ngayChot DATETIME2 NULL");
        }
    }

    // Phân tích chuỗi ngày nhập thành đối tượng LocalDate.
    // Tham số value: giá trị.
    // Kết quả: đối tượng/giá trị kiểu LocalDate.
    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty())
            return null;
        try {
            return LocalDate.parse(value.trim(), DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày phải đúng định dạng dd/MM/yyyy.");
        }
    }

    // Tìm trong nhân viên.
    // Tham số ma: mã.
    // Tham số keyword: từ khóa tìm kiếm.
    // Kết quả: true/false theo điều kiện xử lý.
    private boolean timTrongNhanVien(String ma, String keyword) throws SQLException {
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection c = connectDB.getConnection();
                PreparedStatement p = c.prepareStatement("SELECT hoTen FROM NhanVien WHERE maNhanVien=?")) {
            p.setString(1, ma);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet r = p.executeQuery()) {
                return r.next() && r.getString(1) != null && r.getString(1).toLowerCase(Locale.ROOT).contains(keyword);
            }
        }
    }

    // Tìm trong sản phẩm.
    // Tham số maPhieu: mã phiếu.
    // Tham số keyword: từ khóa tìm kiếm.
    // Kết quả: true/false theo điều kiện xử lý.
    private boolean timTrongSanPham(String maPhieu, String keyword) throws SQLException {
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = kiemKe
                ? "SELECT TOP 1 1 FROM ChiTietPhieuKiemKe d JOIN SanPham s ON s.maSanPham=d.maSanPham WHERE d.maPhieuKiemKe=? AND (LOWER(d.maSanPham) LIKE ? OR LOWER(s.tenSanPham) LIKE ?)"
                : "SELECT TOP 1 1 FROM ChiTietPhieuNhap d JOIN SanPham s ON s.maSanPham=d.maSanPham WHERE d.maPhieuNhap=? AND (LOWER(d.maSanPham) LIKE ? OR LOWER(s.tenSanPham) LIKE ?)";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection c = connectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maPhieu);
            p.setString(2, "%" + keyword + "%");
            p.setString(3, "%" + keyword + "%");
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    // Xem chi tiết.
    private void xemChiTiet() {
        // row: Lưu chỉ số dòng.
        int row = tblDuLieu.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một phiếu để xem chi tiết.");
            return;
        }
        String id = String.valueOf(model.getValueAt(tblDuLieu.convertRowIndexToModel(row), 0));
        if (!kiemKe) {
            xemChiTietPhieuNhap(id);
            return;
        }
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = kiemKe
                ? "SELECT d.maSanPham,s.tenSanPham,d.soLuongHeThong,d.soLuongThucTe FROM ChiTietPhieuKiemKe d JOIN SanPham s ON s.maSanPham=d.maSanPham WHERE d.maPhieuKiemKe=? ORDER BY d.maSanPham"
                : "SELECT d.maSanPham,s.tenSanPham,d.soLuongNhap,d.donGiaNhap,d.hanSuDung FROM ChiTietPhieuNhap d JOIN SanPham s ON s.maSanPham=d.maSanPham WHERE d.maPhieuNhap=? ORDER BY d.maSanPham";
        // btnDetail: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
        DefaultTableModel detail = new DefaultTableModel(kiemKe
                ? new String[] { "Mã SP", "Tên sản phẩm", "Tồn hệ thống", "Tồn thực tế", "Chênh lệch", "Kết quả" }
                : new String[] { "Mã SP", "Tên sản phẩm", "Số lượng nhập", "Đơn giá", "Thành tiền", "Hạn sử dụng" },
                0) {
            // Không cho phép sửa trực tiếp các ô trong bảng.
            // Tham số r: chỉ số dòng.
            // Tham số c: chỉ số cột.
            // Kết quả: false, bảng chỉ dùng để xem.
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection c = connectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, id);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet r = p.executeQuery()) {
                // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
                while (r.next()) {
                    if (kiemKe) {
                        int system = r.getInt(3);
                        Object raw = r.getObject(4);
                        Integer actual = raw == null ? null : ((Number) raw).intValue();
                        detail.addRow(new Object[] { r.getString(1), r.getString(2), system,
                                actual == null ? "Chưa kiểm" : actual,
                                actual == null ? "—" : actual - system,
                                actual == null ? "Chưa kiểm" : actual == system ? "Khớp" : "Chênh lệch" });
                    } else {
                        int quantity = r.getInt(3);
                        double price = r.getDouble(4);
                        detail.addRow(new Object[] { r.getString(1), r.getString(2), quantity,
                                String.format(Locale.forLanguageTag("vi-VN"), "%,.0f ₫", price),
                                String.format(Locale.forLanguageTag("vi-VN"), "%,.0f ₫", quantity * price),
                                r.getString(5) });
                    }
                }
            }
            // tblDetailTable: Bảng hiển thị dữ liệu chi tiết bảng.
            JTable tblDetailTable = new JTable(detail);
            tblDetailTable.setRowHeight(28);
            tblDetailTable.setFillsViewportHeight(true);
            // scrCuon2: Khung cuộn khung cuộn.
            JScrollPane scrCuon2 = new JScrollPane(tblDetailTable);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            scrCuon2.setPreferredSize(new Dimension(
                    Math.min(950, (int) (screen.width * 0.85)),
                    Math.min(520, (int) (screen.height * 0.72))));
            JOptionPane.showMessageDialog(this, scrCuon2, "Chi tiết phiếu " + id, JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            baoLoi(e);
        }
    }

    /** Chi tiet phieu nhap: tai du lieu da luu tu CSDL, sau do cho xuat PDF. */
    private void xemChiTietPhieuNhap(String maPhieu) {
        // dao: Đối tượng truy cập dữ liệu nhập kho.
        final NhapKhoDAO dao = new NhapKhoDAO();
        try {
            NhapKhoDAO.BanInPhieuNhap banIn = dao.layBanInPhieuNhap(maPhieu);
            PhieuNhapKho phieu = banIn.getPhieu();
            String trangThaiPhieu = phieu.isTrangThai() ? "ĐÃ HOÀN TẤT" : "PHIẾU NHÁP – CHƯA HOÀN TẤT";

            Window winWin = SwingUtilities.getWindowAncestor(this);
            // winDialog: Hộp thoại hộp thoại.
            JDialog winDialog = new JDialog(winWin, "Chi tiết phiếu nhập " + maPhieu,
                    Dialog.ModalityType.APPLICATION_MODAL);
            winDialog.setSize(1040, 650);
            winDialog.setMinimumSize(new Dimension(850, 480));
            winDialog.setLocationRelativeTo(this);
            winDialog.setLayout(new BorderLayout(0, 12));
            // pnlHeader: Khung chứa thành phần giao diện đầu trang.
            JPanel pnlHeader = new JPanel(new GridLayout(0, 2, 18, 8));
            pnlHeader.setBackground(Color.WHITE);
            pnlHeader.setBorder(new EmptyBorder(16, 20, 8, 20));
            pnlHeader.add(new JLabel("Mã phiếu nhập: " + phieu.getMaPhieuNhap()));
            pnlHeader.add(new JLabel("Trạng thái: " + trangThaiPhieu));
            pnlHeader.add(new JLabel("Ngày nhập: " +
                    (phieu.getNgayNhap() == null ? "Chưa có" : DATE_TIME.format(phieu.getNgayNhap()))));
            pnlHeader.add(new JLabel("Nhân viên lập: " + hienThi(banIn.getTenNhanVien())
                    + " (" + hienThi(phieu.getMaNhanVien()) + ")"));
            pnlHeader.add(new JLabel("Nhà cung cấp: " + hienThi(phieu.getTenNhaCungCap())
                    + " (" + hienThi(phieu.getMaNhaCungCap()) + ")"));
            pnlHeader.add(new JLabel("Số điện thoại NCC: " + hienThi(banIn.getSoDienThoaiNhaCungCap())));
            pnlHeader.add(new JLabel("Địa chỉ NCC: " + hienThi(banIn.getDiaChiNhaCungCap())));
            winDialog.add(pnlHeader, BorderLayout.NORTH);

            // mdl: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
            DefaultTableModel mdl = new DefaultTableModel(new String[] {
                    "STT", "Mã SP", "Tên sản phẩm", "Số lượng", "Đơn giá",
                    "Thành tiền", "Hạn sử dụng"
            }, 0) {
                // Không cho phép sửa trực tiếp các ô trong bảng.
                // Tham số r: chỉ số dòng.
                // Tham số c: chỉ số cột.
                // Kết quả: false, bảng chỉ dùng để xem.
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            int stt = 0;
            for (ChiTietPhieuNhap ct : banIn.getChiTiet()) {
                mdl.addRow(new Object[] {
                        ++stt, ct.getMaSanPham(), ct.getTenSanPham(), ct.getSoLuongNhap(),
                        tien(ct.getDonGiaNhap()), tien(ct.getThanhTien()),
                        ct.getHanSuDung() == null ? "—"
                                : ct.getHanSuDung().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                });
            }
            // tblChiTiet2: Bảng hiển thị dữ liệu chi tiết.
            JTable tblChiTiet2 = new JTable(mdl);
            tblChiTiet2.setRowHeight(30);
            tblChiTiet2.setFillsViewportHeight(true);
            tblChiTiet2.setAutoCreateRowSorter(true);
            winDialog.add(new JScrollPane(tblChiTiet2), BorderLayout.CENTER);

            // pnlFooter: Khung chứa thành phần giao diện cuối trang.
            JPanel pnlFooter = new JPanel(new BorderLayout());
            pnlFooter.setBorder(new EmptyBorder(5, 20, 15, 20));
            pnlFooter.setBackground(Color.WHITE);
            // lblTongTien: Nhãn hiển thị: Tổng số lượng: .
            JLabel lblTongTien = new JLabel("Tổng số lượng: " + banIn.getTongSoLuong()
                    + "     |     Tổng tiền: " + tien(banIn.getTongTien()));
            lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTongTien.setForeground(NAVY);
            pnlFooter.add(lblTongTien, BorderLayout.WEST);
            // pnlBtns: Khung chứa thành phần giao diện các nút.
            JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnlBtns.setOpaque(false);
            // btnPdf: Nút thao tác: Xuất phiếu nhập.
            JButton btnPdf = new JButton("Xuất phiếu nhập");
            btnPdf.setBackground(NAVY);
            btnPdf.setForeground(Color.WHITE);
            btnPdf.setFocusPainted(false);
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnPdf.addActionListener(e -> {
                try {
                    // Nap lai theo ma phieu khi xuat de tranh in du lieu da thay doi.
                    PhieuNhapPdfExporter.xuat(winDialog, dao.layBanInPhieuNhap(maPhieu));
                } catch (SQLException | SecurityException ex) {
                    JOptionPane.showMessageDialog(winDialog, ex.getMessage(),
                            "Không thể xuất phiếu nhập", JOptionPane.ERROR_MESSAGE);
                }
            });
            // btnDong: Nút thao tác: Đóng.
            JButton btnDong = new JButton("Đóng");
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnDong.addActionListener(e -> winDialog.dispose());
            pnlBtns.add(btnPdf);
            pnlBtns.add(btnDong);
            pnlFooter.add(pnlBtns, BorderLayout.EAST);
            winDialog.add(pnlFooter, BorderLayout.SOUTH);
            winDialog.setVisible(true);
        } catch (SQLException | SecurityException e) {
            baoLoi(e);
        }
    }

    // Chuẩn hóa chuỗi để hiển thị khi dữ liệu có thể bị thiếu.
    // Tham số value: giá trị.
    // Kết quả: chuỗi kết quả.
    private String hienThi(String value) {
        return value == null || value.isBlank() ? "Chưa có dữ liệu" : value;
    }

    // Định dạng số tiền thành chuỗi để hiển thị.
    // Tham số value: giá trị.
    // Kết quả: chuỗi kết quả.
    private String tien(double value) {
        return String.format(Locale.forLanguageTag("vi-VN"), "%,.0f ₫", value);
    }

    // Báo lỗi dữ liệu của màn hình.
    // Tham số e: ngoại lệ cần xử lý.
    private void baoLoi(Exception e) {
        JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu kho.\n" + e.getMessage(), "Lỗi tra cứu",
                JOptionPane.ERROR_MESSAGE);
    }

    // Làm mới dữ liệu hoặc trạng thái của màn hình.
    public void lamMoi() {
        timKiem();
    }
}
