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

/** Cảnh báo hạn sử dụng theo lô nhập còn tồn. */
public class CanhBaoHanSuDungPanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã SP", "Tên SP", "Danh mục", "Ngày nhập", "Hạn sử dụng", "Còn lại", "Trạng thái"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<String> status = new JComboBox<>(new String[]{"Tất cả", "Đã hết hạn", "Sắp hết hạn (30 ngày)"});
    private final JTextField search = new JTextField(18);
    private final JLabel expiredCount = new JLabel("0");
    private final JLabel soonCount = new JLabel("0");
    private final JLabel totalCount = new JLabel("0");

    public CanhBaoHanSuDungPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UIStyle.LIGHT_BLUE);
        setBorder(new EmptyBorder(22, 25, 22, 25));

        JLabel title = new JLabel("Cảnh báo sản phẩm sắp hết hạn");
        title.setFont(UIStyle.font(Font.BOLD, 24));
        JButton refresh = UIStyle.nutChinh("Làm mới");
        refresh.addActionListener(e -> napLai());
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filters.setOpaque(false);
        filters.add(new JLabel("Trạng thái:"));
        filters.add(status);
        filters.add(new JLabel("Tên / mã sản phẩm:"));
        filters.add(search);
        JButton find = UIStyle.nutChinh("Tìm kiếm");
        filters.add(find);

        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));
        stats.setOpaque(false);
        stats.add(theThongKe("Đã hết hạn", expiredCount, new Color(255, 241, 243), new Color(240, 72, 92)));
        stats.add(theThongKe("Sắp hết hạn (≤ 30 ngày)", soonCount, new Color(255, 249, 232), new Color(218, 145, 14)));
        stats.add(theThongKe("Tổng số sản phẩm cần lưu ý", totalCount, new Color(237, 246, 255), new Color(31, 105, 181)));

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(header);
        north.add(Box.createVerticalStrut(12));
        north.add(filters);
        north.add(Box.createVerticalStrut(10));
        north.add(stats);
        add(north, BorderLayout.NORTH);

        UIStyle.taoBang(table, 1, 2, 3);
        add(UIStyle.cuon(table), BorderLayout.CENTER);
        find.addActionListener(e -> napLai());
        search.addActionListener(e -> napLai());
        status.addActionListener(e -> napLai());
        napLai();
    }

    private JPanel theThongKe(String title, JLabel value, Color background, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(background);
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(accent.brighter()), new EmptyBorder(12, 15, 12, 15)));
        JLabel caption = new JLabel(title);
        caption.setForeground(accent.darker());
        caption.setFont(UIStyle.font(Font.BOLD, 12));
        value.setForeground(accent.darker());
        value.setFont(UIStyle.font(Font.BOLD, 24));
        card.add(caption, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    public void napLai() {
        model.setRowCount(0);
        Set<String> expired = new HashSet<>();
        Set<String> soon = new HashSet<>();
        String keyword = search.getText().trim().toLowerCase();
        String selectedStatus = (String) status.getSelectedItem();
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
            )
            SELECT maSanPham, tenSanPham, tenDanhMuc, ngayNhap, hanSuDung,
                   CASE WHEN sold <= prevQty THEN soLuongNhap WHEN sold >= cumQty THEN 0 ELSE cumQty - sold END AS conLai,
                   DATEDIFF(day, CAST(GETDATE() AS date), hanSuDung) AS ngayCon
            FROM Lots
            WHERE hanSuDung <= DATEADD(day, 30, CAST(GETDATE() AS date))
            ORDER BY hanSuDung, tenSanPham
            """;

        try (Connection connection = connectDB.getConnection(); Statement migration = connection.createStatement()) {
            migration.execute("IF COL_LENGTH('dbo.ChiTietPhieuNhap', 'hanSuDung') IS NULL ALTER TABLE dbo.ChiTietPhieuNhap ADD hanSuDung DATE NULL");
        } catch (SQLException ex) {
            showError("Không thể cập nhật cấu trúc hạn sử dụng", ex);
            return;
        }

        try (Connection connection = connectDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                int days = result.getInt("ngayCon");
                int quantity = result.getInt("conLai");
                if (quantity <= 0) continue;
                String code = result.getString("maSanPham");
                String name = result.getString("tenSanPham");
                boolean isExpired = days < 0;
                (isExpired ? expired : soon).add(code);
                if ("Đã hết hạn".equals(selectedStatus) && !isExpired) continue;
                if ("Sắp hết hạn (30 ngày)".equals(selectedStatus) && isExpired) continue;
                if (!keyword.isEmpty() && !code.toLowerCase().contains(keyword) && !name.toLowerCase().contains(keyword)) continue;
                model.addRow(new Object[]{code, name, result.getString("tenDanhMuc"), result.getDate("ngayNhap"),
                        result.getDate("hanSuDung"), quantity, isExpired ? "Đã hết hạn" : "Sắp hết hạn · " + days + " ngày"});
            }
            expiredCount.setText(String.valueOf(expired.size()));
            soonCount.setText(String.valueOf(soon.size()));
            Set<String> all = new HashSet<>(expired);
            all.addAll(soon);
            totalCount.setText(String.valueOf(all.size()));
        } catch (SQLException ex) {
            showError("Không tải được dữ liệu cảnh báo", ex);
        }
    }

    private void showError(String title, SQLException ex) {
        JOptionPane.showMessageDialog(this, title + ".\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
