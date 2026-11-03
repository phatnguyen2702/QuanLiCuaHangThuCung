package UI;

import control.PhanQuyenControl;

import ConnectDB.connectDB;
import UI.TrangChuUI.UIStyle;
import dao.SanPhamDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/** Cảnh báo theo từng lô còn tồn, không gộp các hạn sử dụng khác nhau. */
public class CanhBaoHanSuDungUI extends JFrame implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
    }

    // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã SP", "Tên SP", "Danh mục", "Đợt nhập", "Ngày nhập", "Hạn sử dụng", "Còn lại",
                    "Trạng thái" },
            0) {
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
    // cmbStatus: Hộp lựa chọn trạng thái.
    private final JComboBox<String> cmbStatus = new JComboBox<>(
            new String[] { "Tất cả", "Đã hết hạn", "Sắp hết hạn (30 ngày)" });
    // txtSearch: Ô nhập/hiển thị văn bản tìm kiếm.
    private final JTextField txtSearch = new JTextField(18);
    // lblExpiredCount: Nhãn hiển thị: 0.
    private final JLabel lblExpiredCount = new JLabel("0");
    // lblSoonCount: Nhãn hiển thị: 0.
    private final JLabel lblSoonCount = new JLabel("0");
    // lblTotalCount: Nhãn hiển thị: 0.
    private final JLabel lblTotalCount = new JLabel("0");

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    public CanhBaoHanSuDungUI() {
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(UIStyle.LIGHT_BLUE);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(22, 25, 22, 25));

        // lblTitle2: Nhãn hiển thị: Tra cứu hạn sử dụng sản phẩm.
        JLabel lblTitle2 = new JLabel("Tra cứu hạn sử dụng sản phẩm");
        lblTitle2.setFont(UIStyle.font(Font.BOLD, 24));
        // btnRefresh: Nút thao tác làm mới.
        JButton btnRefresh = UIStyle.nutChinh("Làm mới");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnRefresh.addActionListener(e -> napLai());
        JButton btnNgungBan = UIStyle.nutPhu("Ngừng kinh doanh");
        btnNgungBan.addActionListener(e -> ngungKinhDoanhLo());
        // pnlHeader2: Khung chứa thành phần giao diện đầu trang.
        JPanel pnlHeader2 = new JPanel(new BorderLayout());
        pnlHeader2.setOpaque(false);
        pnlHeader2.add(lblTitle2, BorderLayout.WEST);
        JPanel pnlHeaderActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlHeaderActions.setOpaque(false);
        pnlHeaderActions.add(btnNgungBan);
        pnlHeaderActions.add(btnRefresh);
        pnlHeader2.add(pnlHeaderActions, BorderLayout.EAST);

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
        pnlStats.add(
                theThongKe("Sắp hết hạn (≤ 30 ngày)", lblSoonCount, new Color(255, 249, 232), new Color(218, 145, 14)));
        pnlStats.add(theThongKe("Tổng số sản phẩm cần lưu ý", lblTotalCount, new Color(237, 246, 255),
                new Color(31, 105, 181)));

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
        pnlCard.setBorder(
                BorderFactory.createCompoundBorder(new LineBorder(accent.brighter()), new EmptyBorder(12, 15, 12, 15)));
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
        Set<String> expired = new HashSet<>();
        Set<String> soon = new HashSet<>();
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selectedStatus = (String) cmbStatus.getSelectedItem();
        String sql = """
                WITH NguonLo AS (
                  SELECT ct.maSanPham,sp.tenSanPham,dm.tenDanhMuc,pn.maPhieuNhap,pn.ngayNhap,ct.hanSuDung,
                         CASE WHEN ISNULL(ct.soLuongNhap,0)>ISNULL(ct.soLuongDaXuLy,0)
                              THEN ISNULL(ct.soLuongNhap,0)-ISNULL(ct.soLuongDaXuLy,0) ELSE 0 END AS soLuongNhap,
                         ISNULL(ct.ngungKinhDoanh,0) AS ngungKinhDoanh,
                         ISNULL((SELECT SUM(h.soLuong) FROM ChiTietHoaDon h WHERE h.maSanPham=ct.maSanPham),0)
                           - ISNULL((SELECT SUM(k.soLuongThucTe-k.soLuongHeThong)
                                     FROM ChiTietPhieuKiemKe k JOIN PhieuKiemKe p ON p.maPhieuKiemKe=k.maPhieuKiemKe
                                     WHERE k.maSanPham=ct.maSanPham AND p.trangThai=1 AND k.soLuongThucTe IS NOT NULL),0) AS daBan
                  FROM ChiTietPhieuNhap ct
                  JOIN PhieuNhapKho pn ON pn.maPhieuNhap=ct.maPhieuNhap
                  JOIN SanPham sp ON sp.maSanPham=ct.maSanPham
                  JOIN DanhMuc dm ON dm.maDanhMuc=sp.maDanhMuc
                  WHERE pn.trangThai=1 AND ISNULL(ct.trangThai,1)=1 AND ct.hanSuDung IS NOT NULL
                ), Lots AS (
                  SELECT *,SUM(soLuongNhap) OVER(PARTITION BY maSanPham ORDER BY ngayNhap,maPhieuNhap ROWS UNBOUNDED PRECEDING) AS tonLuyKe,
                         SUM(soLuongNhap) OVER(PARTITION BY maSanPham ORDER BY ngayNhap,maPhieuNhap ROWS UNBOUNDED PRECEDING)-soLuongNhap AS tonTruoc
                  FROM NguonLo
                ), TonLo AS (
                  SELECT *,CASE WHEN daBan<=tonTruoc THEN soLuongNhap WHEN daBan>=tonLuyKe THEN 0
                                ELSE tonLuyKe-daBan END
                         + ISNULL((SELECT SUM(k.soLuongThucTe-k.soLuongHeThong)
                                   FROM ChiTietPhieuKiemKeLo k JOIN PhieuKiemKe p ON p.maPhieuKiemKe=k.maPhieuKiemKe
                                   WHERE k.maPhieuNhap=Lots.maPhieuNhap AND k.maSanPham=Lots.maSanPham
                                     AND p.trangThai=1 AND k.soLuongThucTe IS NOT NULL),0) AS conLai
                  FROM Lots
                )
                SELECT maSanPham,tenSanPham,tenDanhMuc,maPhieuNhap,ngayNhap,hanSuDung,conLai,
                       DATEDIFF(day,CAST(GETDATE() AS date),hanSuDung) AS ngayCon,ngungKinhDoanh
                FROM TonLo
                WHERE conLai>0 AND hanSuDung<=DATEADD(day,30,CAST(GETDATE() AS date))
                ORDER BY hanSuDung,tenSanPham
                """;

        try (Connection connection = connectDB.getConnection(); Statement migration = connection.createStatement()) {
            migration.execute(
                    "IF COL_LENGTH('dbo.ChiTietPhieuNhap','hanSuDung') IS NULL ALTER TABLE ChiTietPhieuNhap ADD hanSuDung DATE NULL");
            SanPhamDAO.damBaoCotKinhDoanh();
        } catch (SQLException ex) {
            showError("Không thể cập nhật cấu trúc hạn sử dụng", ex);
            return;
        }

        try (Connection connection = connectDB.getConnection();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                int days = result.getInt("ngayCon");
                int quantity = result.getInt("conLai");
                String code = result.getString("maSanPham");
                String name = result.getString("tenSanPham");
                boolean isExpired = days < 0;
                (isExpired ? expired : soon).add(code);
                if ("Đã hết hạn".equals(selectedStatus) && !isExpired)
                    continue;
                if ("Sắp hết hạn (30 ngày)".equals(selectedStatus) && isExpired)
                    continue;
                if (!keyword.isEmpty() && !code.toLowerCase().contains(keyword)
                        && !name.toLowerCase().contains(keyword))
                    continue;
                String status = result.getBoolean("ngungKinhDoanh") ? "Ngừng kinh doanh"
                        : isExpired ? "Đã hết hạn" : "Sắp hết hạn · " + days + " ngày";
                model.addRow(new Object[] { code, name, result.getString("tenDanhMuc"), result.getString("maPhieuNhap"),
                        result.getDate("ngayNhap"), result.getDate("hanSuDung"), quantity, status });
            }
            lblExpiredCount.setText(String.valueOf(expired.size()));
            lblSoonCount.setText(String.valueOf(soon.size()));
            Set<String> all = new HashSet<>(expired);
            all.addAll(soon);
            lblTotalCount.setText(String.valueOf(all.size()));
        } catch (SQLException ex) {
            showError("Không tải được dữ liệu cảnh báo", ex);
        }
    }

    private void ngungKinhDoanhLo() {
        int row = tblDuLieu.getSelectedRow();
        if (row < 0) {
            UIStyle.canhBao(this, "Chọn lô sản phẩm cần ngừng kinh doanh.");
            return;
        }
        int modelRow = tblDuLieu.convertRowIndexToModel(row);
        String maSanPham = String.valueOf(model.getValueAt(modelRow, 0));
        String maPhieuNhap = String.valueOf(model.getValueAt(modelRow, 3));
        if (!UIStyle.xacNhan(this, "Ngừng kinh doanh sản phẩm " + maSanPham + " của đợt " + maPhieuNhap + "?"))
            return;
        try {
            PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.CANH_BAO_HAN_SU_DUNG);
            SanPhamDAO.damBaoCotKinhDoanh();
            try (Connection connection = connectDB.getConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "UPDATE ChiTietPhieuNhap SET ngungKinhDoanh=1 WHERE maPhieuNhap=? AND maSanPham=?")) {
                statement.setString(1, maPhieuNhap);
                statement.setString(2, maSanPham);
                if (statement.executeUpdate() != 1)
                    throw new SQLException("Không tìm thấy dòng hàng thuộc đợt nhập đã chọn.");
            }
            napLai();
        } catch (SQLException | SecurityException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể ngừng kinh doanh",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Hiển thị lỗi.
    // Tham số lblTitle2: tiêu đề.
    // Tham số ex: ngoại lệ cần xử lý.
    private void showError(String title, SQLException ex) {
        JOptionPane.showMessageDialog(this, title + ".\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
