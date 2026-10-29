package UI;

import ConnectDB.connectDB;
import UI.TrangChu.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/** Cảnh báo một dòng cho mỗi sản phẩm, dựa trên lô còn tồn gần hết hạn nhất. */
public class CanhBaoHanSuDungPanel extends JPanel {
    // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã SP", "Tên SP", "Danh mục", "Ngày nhập", "Hạn sử dụng", "Còn lại", "Trạng thái"}, 0) {
        // Không cho phép sửa trực tiếp các ô trong bảng.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: false, bảng chỉ dùng để xem.
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    // tblDuLieu: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
    private final JTable tblDuLieu = new JTable(model);
    // cmbStatus: Hộp lựa chọn trạng thái.
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Tất cả", "Đã hết hạn", "Sắp hết hạn (30 ngày)"});
    // txtSearch: Ô nhập/hiển thị văn bản tìm kiếm.
    private final JTextField txtSearch = new JTextField(18);
    // lblExpiredCount: Nhãn hiển thị: 0.
    private final JLabel lblExpiredCount = new JLabel("0");
    // lblSoonCount: Nhãn hiển thị: 0.
    private final JLabel lblSoonCount = new JLabel("0");
    // lblTotalCount: Nhãn hiển thị: 0.
    private final JLabel lblTotalCount = new JLabel("0");

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    public CanhBaoHanSuDungPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UIStyle.LIGHT_BLUE);
        setBorder(new EmptyBorder(22, 25, 22, 25));

        // lblTitle2: Nhãn hiển thị: Cảnh báo sản phẩm sắp hết hạn.
        JLabel lblTitle2 = new JLabel("Cảnh báo sản phẩm sắp hết hạn");
        lblTitle2.setFont(UIStyle.font(Font.BOLD, 24));
        // btnRefresh: Nút thao tác làm mới.
        JButton btnRefresh = UIStyle.nutChinh("Làm mới");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnRefresh.addActionListener(e -> napLai());
        // pnlHeader2: Khung chứa thành phần giao diện đầu trang.
        JPanel pnlHeader2 = new JPanel(new BorderLayout());
        pnlHeader2.setOpaque(false);
        pnlHeader2.add(lblTitle2, BorderLayout.WEST);
        pnlHeader2.add(btnRefresh, BorderLayout.EAST);

        // pnlFilters: Khung chứa thành phần giao diện các bộ lọc.
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        pnlFilters.setOpaque(false);
        pnlFilters.add(new JLabel("Trạng thái:"));
        pnlFilters.add(cmbStatus);
        pnlFilters.add(new JLabel("Tên / mã sản phẩm:"));
        pnlFilters.add(txtSearch);
        // btnFind: Nút thao tác tìm.
        JButton btnFind = UIStyle.nutChinh("Tìm kiếm");
        pnlFilters.add(btnFind);

        // pnlStats: Khung chứa thành phần giao diện thống kê.
        JPanel pnlStats = new JPanel(new GridLayout(1, 3, 14, 0));
        pnlStats.setOpaque(false);
        pnlStats.add(theThongKe("Đã hết hạn", lblExpiredCount, new Color(255, 241, 243), new Color(240, 72, 92)));
        pnlStats.add(theThongKe("Sắp hết hạn (≤ 30 ngày)", lblSoonCount, new Color(255, 249, 232), new Color(218, 145, 14)));
        pnlStats.add(theThongKe("Tổng số sản phẩm cần lưu ý", lblTotalCount, new Color(237, 246, 255), new Color(31, 105, 181)));

        // pnlNorth: Khung chứa thành phần giao diện phía trên.
        JPanel pnlNorth = new JPanel();
        pnlNorth.setOpaque(false);
        pnlNorth.setLayout(new BoxLayout(pnlNorth, BoxLayout.Y_AXIS));
        pnlNorth.add(pnlHeader2);
        pnlNorth.add(Box.createVerticalStrut(12));
        pnlNorth.add(pnlFilters);
        pnlNorth.add(Box.createVerticalStrut(10));
        pnlNorth.add(pnlStats);
        add(pnlNorth, BorderLayout.NORTH);

        UIStyle.taoBang(tblDuLieu, 1, 2, 3);
        add(UIStyle.cuon(tblDuLieu), BorderLayout.CENTER);
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnFind.addActionListener(e -> napLai());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        txtSearch.addActionListener(e -> napLai());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbStatus.addActionListener(e -> napLai());
        napLai();
    }

    // Tạo thẻ hiển thị tiêu đề và giá trị thống kê.
    // Tham số lblTitle2: tiêu đề.
    // Tham số lblValue: giá trị.
    // Tham số background: màu nền.
    // Tham số accent: màu nhấn.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel theThongKe(String title, JLabel lblValue, Color background, Color accent) {
        // pnlCard: Khung chứa thành phần giao diện thẻ.
        JPanel pnlCard = new JPanel(new BorderLayout(0, 8));
        pnlCard.setBackground(background);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(new LineBorder(accent.brighter()), new EmptyBorder(12, 15, 12, 15)));
        // lblCaption: Nhãn hiển thị chú thích.
        JLabel lblCaption = new JLabel(title);
        lblCaption.setForeground(accent.darker());
        lblCaption.setFont(UIStyle.font(Font.BOLD, 12));
        lblValue.setForeground(accent.darker());
        lblValue.setFont(UIStyle.font(Font.BOLD, 24));
        pnlCard.add(lblCaption, BorderLayout.NORTH);
        pnlCard.add(lblValue, BorderLayout.CENTER);
        return pnlCard;
    }

    // Nạp lại dữ liệu và cập nhật nội dung màn hình.
    public void napLai() {
        model.setRowCount(0);
        // expired: Lưu hết hạn; phần tử có kiểu String.
        Set<String> expired = new HashSet<>();
        // soon: Lưu sắp đến; phần tử có kiểu String.
        Set<String> soon = new HashSet<>();
        // keyword: Đọc nội dung chữ từ ô nhập để xử lý từ khóa tìm kiếm.
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selectedStatus = (String) cmbStatus.getSelectedItem();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = """
            WITH Lots AS (
              SELECT ct.maSanPham, sp.tenSanPham, dm.tenDanhMuc, pn.maPhieuNhap,
                     pn.ngayNhap, ct.hanSuDung, ct.soLuongNhap,
                     SUM(ct.soLuongNhap) OVER(PARTITION BY ct.maSanPham ORDER BY pn.ngayNhap, pn.maPhieuNhap ROWS UNBOUNDED PRECEDING) AS cumQty,
                     SUM(ct.soLuongNhap) OVER(PARTITION BY ct.maSanPham ORDER BY pn.ngayNhap, pn.maPhieuNhap ROWS UNBOUNDED PRECEDING) - ct.soLuongNhap AS prevQty,
                     ISNULL((SELECT SUM(hd.soLuong) FROM ChiTietHoaDon hd WHERE hd.maSanPham = ct.maSanPham), 0)
                     - ISNULL((SELECT SUM(kk.soLuongThucTe - kk.soLuongHeThong)
                               FROM ChiTietPhieuKiemKe kk
                               JOIN PhieuKiemKe pk ON pk.maPhieuKiemKe = kk.maPhieuKiemKe
                               WHERE kk.maSanPham = ct.maSanPham AND pk.trangThai = 1
                                 AND kk.soLuongThucTe IS NOT NULL), 0) AS sold
              FROM ChiTietPhieuNhap ct
              JOIN PhieuNhapKho pn ON pn.maPhieuNhap = ct.maPhieuNhap
              JOIN SanPham sp ON sp.maSanPham = ct.maSanPham
              JOIN DanhMuc dm ON dm.maDanhMuc = sp.maDanhMuc
              WHERE pn.trangThai = 1 AND ISNULL(ct.trangThai, 1) = 1 AND ct.hanSuDung IS NOT NULL
            ), RemainingLots AS (
              SELECT maSanPham, tenSanPham, tenDanhMuc, maPhieuNhap, ngayNhap, hanSuDung,
                     CASE WHEN sold <= prevQty THEN soLuongNhap WHEN sold >= cumQty THEN 0 ELSE cumQty - sold END AS conLai,
                     DATEDIFF(day, CAST(GETDATE() AS date), hanSuDung) AS ngayCon
              FROM Lots
            ), AlertLots AS (
              SELECT *, ROW_NUMBER() OVER(
                       PARTITION BY maSanPham ORDER BY hanSuDung, ngayNhap, maPhieuNhap) AS stt
              FROM RemainingLots
              WHERE conLai > 0
                AND hanSuDung <= DATEADD(day, 30, CAST(GETDATE() AS date))
            )
            SELECT maSanPham, tenSanPham, tenDanhMuc, ngayNhap, hanSuDung, conLai, ngayCon
            FROM AlertLots
            WHERE stt = 1
            ORDER BY hanSuDung, tenSanPham
            """;

        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection connection = connectDB.getConnection(); Statement migration = connection.createStatement()) {
            migration.execute("IF COL_LENGTH('dbo.ChiTietPhieuNhap', 'hanSuDung') IS NULL ALTER TABLE dbo.ChiTietPhieuNhap ADD hanSuDung DATE NULL");
        } catch (SQLException ex) {
            showError("Không thể cập nhật cấu trúc hạn sử dụng", ex);
            return;
        }

        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection connection = connectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (result.next()) {
                int days = result.getInt("ngayCon");
                int quantity = result.getInt("conLai");
                if (quantity <= 0) continue;
                String code = result.getString("maSanPham");
                // name: Lưu tên.
                String name = result.getString("tenSanPham");
                boolean isExpired = days < 0;
                (isExpired ? expired : soon).add(code);
                if ("Đã hết hạn".equals(selectedStatus) && !isExpired) continue;
                if ("Sắp hết hạn (30 ngày)".equals(selectedStatus) && isExpired) continue;
                if (!keyword.isEmpty() && !code.toLowerCase().contains(keyword) && !name.toLowerCase().contains(keyword)) continue;
                model.addRow(new Object[]{code, name, result.getString("tenDanhMuc"), result.getDate("ngayNhap"),
                        result.getDate("hanSuDung"), quantity, isExpired ? "Đã hết hạn" : "Sắp hết hạn · " + days + " ngày"});
            }
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblExpiredCount.setText(String.valueOf(expired.size()));
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblSoonCount.setText(String.valueOf(soon.size()));
            // all: Lưu tất cả; phần tử có kiểu String.
            Set<String> all = new HashSet<>(expired);
            all.addAll(soon);
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblTotalCount.setText(String.valueOf(all.size()));
        } catch (SQLException ex) {
            showError("Không tải được dữ liệu cảnh báo", ex);
        }
    }

    // Hiển thị lỗi.
    // Tham số lblTitle2: tiêu đề.
    // Tham số ex: ngoại lệ cần xử lý.
    private void showError(String title, SQLException ex) {
        JOptionPane.showMessageDialog(this, title + ".\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
