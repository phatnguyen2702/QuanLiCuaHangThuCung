package UI;
import UI.TrangChuUI.UIStyle;
import control.SanPhamControl;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
public class CanhBaoHanSuDungUI extends JFrame {
private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã SP", "Tên SP", "Danh mục", "Đợt nhập", "Ngày nhập", "Hạn sử dụng", "Còn lại",
                    "Trạng thái" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblDuLieu = new JTable(model);
    private final JComboBox<String> cmbStatus = new JComboBox<>(
            new String[] { "Tất cả", "Đã hết hạn", "Sắp hết hạn (30 ngày)" });
    private final JTextField txtSearch = new JTextField(18);
    private final JLabel lblExpiredCount = new JLabel("0");
    private final JLabel lblSoonCount = new JLabel("0");
    private final JLabel lblTotalCount = new JLabel("0");
    public CanhBaoHanSuDungUI() {
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(UIStyle.LIGHT_BLUE);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(22, 25, 22, 25));
        JLabel lblTitle2 = new JLabel("Tra cứu hạn sử dụng sản phẩm");
        lblTitle2.setFont(UIStyle.font(Font.BOLD, 24));
        JButton btnRefresh = UIStyle.nutChinh("Làm mới");
        btnRefresh.addActionListener(e -> napLai());
        JButton btnNgungBan = UIStyle.nutPhu("Ngừng kinh doanh");
        btnNgungBan.addActionListener(e -> ngungKinhDoanhLoCanhBao());
        JPanel pnlHeader2 = new JPanel(new BorderLayout());
        pnlHeader2.setOpaque(false);
        pnlHeader2.add(lblTitle2, BorderLayout.WEST);
        JPanel pnlHeaderActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlHeaderActions.setOpaque(false);
        pnlHeaderActions.add(btnNgungBan);
        pnlHeaderActions.add(btnRefresh);
        pnlHeader2.add(pnlHeaderActions, BorderLayout.EAST);
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        pnlFilters.setOpaque(false);
        pnlFilters.add(new JLabel("Trạng thái:"));
        pnlFilters.add(cmbStatus);
        pnlFilters.add(new JLabel("Tên / mã sản phẩm:"));
        pnlFilters.add(txtSearch);
        JButton btnFind = UIStyle.nutChinh("Tìm kiếm");
        pnlFilters.add(btnFind);
        JPanel pnlStats = new JPanel(new GridLayout(1, 3, 14, 0));
        pnlStats.setOpaque(false);
        pnlStats.add(theThongKe("Đã hết hạn", lblExpiredCount, new Color(255, 241, 243), new Color(240, 72, 92)));
        pnlStats.add(
                theThongKe("Sắp hết hạn (≤ 30 ngày)", lblSoonCount, new Color(255, 249, 232), new Color(218, 145, 14)));
        pnlStats.add(theThongKe("Tổng số sản phẩm cần lưu ý", lblTotalCount, new Color(237, 246, 255),
                new Color(31, 105, 181)));
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
        btnFind.addActionListener(e -> napLai());
        txtSearch.addActionListener(e -> napLai());
        cmbStatus.addActionListener(e -> napLai());
        napLai();
    }
    private JPanel theThongKe(String title, JLabel lblValue, Color background, Color accent) {
        JPanel pnlCard = new JPanel(new BorderLayout(0, 8));
        pnlCard.setBackground(background);
        pnlCard.setBorder(
                BorderFactory.createCompoundBorder(new LineBorder(accent.brighter()), new EmptyBorder(12, 15, 12, 15)));
        JLabel lblCaption = new JLabel(title);
        lblCaption.setForeground(accent.darker());
        lblCaption.setFont(UIStyle.font(Font.BOLD, 12));
        lblValue.setForeground(accent.darker());
        lblValue.setFont(UIStyle.font(Font.BOLD, 24));
        pnlCard.add(lblCaption, BorderLayout.NORTH);
        pnlCard.add(lblValue, BorderLayout.CENTER);
        return pnlCard;
    }
    public void napLai() {
        model.setRowCount(0);
        Set<String> expired = new HashSet<>();
        Set<String> soon = new HashSet<>();
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selectedStatus = (String) cmbStatus.getSelectedItem();
        try {
            for (dao.SanPhamDAO.LoCanhBaoHanSuDung lo : SanPhamControl.layDanhSachLoCanhBaoHanSuDung()) {
                int days = lo.getSoNgayCon();
                int quantity = lo.getSoLuongCon();
                String code = lo.getMaSanPham();
                String name = lo.getTenSanPham();
                boolean isExpired = days < 0;
                (isExpired ? expired : soon).add(code);
                if ("Đã hết hạn".equals(selectedStatus) && !isExpired)
                    continue;
                if ("Sắp hết hạn (30 ngày)".equals(selectedStatus) && isExpired)
                    continue;
                if (!keyword.isEmpty() && !code.toLowerCase().contains(keyword)
                        && !name.toLowerCase().contains(keyword))
                    continue;
                String status = lo.isNgungKinhDoanh() ? "Ngừng kinh doanh"
                        : isExpired ? "Đã hết hạn" : "Sắp hết hạn · " + days + " ngày";
                model.addRow(new Object[] { code, name, lo.getTenDanhMuc(), lo.getMaPhieuNhap(),
                        lo.getNgayNhap(), lo.getHanSuDung(), quantity, status });
            }
            lblExpiredCount.setText(String.valueOf(expired.size()));
            lblSoonCount.setText(String.valueOf(soon.size()));
            Set<String> all = new HashSet<>(expired);
            all.addAll(soon);
            lblTotalCount.setText(String.valueOf(all.size()));
        } catch (Exception ex) {
            showError("Không tải được dữ liệu cảnh báo", ex);
        }
    }
    private void ngungKinhDoanhLoCanhBao() {
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
            if (!SanPhamControl.ngungKinhDoanhLo(maPhieuNhap, maSanPham))
                throw new SQLException("Không tìm thấy dòng hàng thuộc đợt nhập đã chọn.");
            napLai();
        } catch (SQLException | SecurityException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể ngừng kinh doanh",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void showError(String title, Exception ex) {
        JOptionPane.showMessageDialog(this, title + ".\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
