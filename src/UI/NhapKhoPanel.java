package UI;

import dao.NhaCungCapDAO;
import dao.PhieuNhapKhoDAO;
import dao.SanPhamDAO;
import entity.ChiTietPhieuNhap;
import entity.NhaCungCap;
import entity.PhieuNhapKho;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Tab "Nhap kho" - Lap phieu nhap hang. */
public class NhapKhoPanel extends JPanel implements ManHinhKho {

    private final PhieuNhapKhoDAO phieuDAO = new PhieuNhapKhoDAO();
    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAO();
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();

    private static final DateTimeFormatter DINH_DANG_NGAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ---- thong tin phieu ----
    private final JTextField txtMaPhieu = UIStyle.oChiDoc("");
    private final JTextField txtNgayNhap = UIStyle.oChiDoc("");
    private final JTextField txtNhanVien = UIStyle.oChiDoc(PhienLamViec.hoTen);
    private final JComboBox<NhaCungCap> cboNhaCungCap = new JComboBox<>();

    // ---- chi tiet ----
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã SP", "Tên mặt hàng", "Số lượng",
                    "Đơn giá nhập", "Thành tiền"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable bang = new JTable(model);
    private final JLabel lbTong = new JLabel("Tổng thanh toán: 0 ₫");

    private final List<ChiTietPhieuNhap> dong = new ArrayList<>();

    private LocalDateTime ngayNhap = LocalDateTime.now();
    private NhaCungCap nccHienTai = null;
    private boolean dangNapCombo = false;

    public NhapKhoPanel() {

        setLayout(new BorderLayout(0, 12));
        setBackground(UIStyle.LIGHT_BLUE);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel tieuDe = new JLabel("Lập phiếu nhập hàng");
        tieuDe.setFont(UIStyle.font(Font.BOLD, 26));

        JPanel phanDau = new JPanel(new BorderLayout(0, 12));
        phanDau.setOpaque(false);
        phanDau.add(tieuDe, BorderLayout.NORTH);
        phanDau.add(taoThongTinPhieu(), BorderLayout.CENTER);

        add(phanDau, BorderLayout.NORTH);
        add(taoPhanChiTiet(), BorderLayout.CENTER);
        add(taoPhanCuoi(), BorderLayout.SOUTH);

        cboNhaCungCap.addActionListener(e -> doiNhaCungCap());

        napLai();
    }

    // =========================================================
    // GIAO DIEN
    // =========================================================

    private JPanel taoThongTinPhieu() {

        JPanel the = UIStyle.the();
        the.setLayout(new GridBagLayout());

        cboNhaCungCap.setFont(UIStyle.font(Font.PLAIN, 13));

        JButton btnThemNcc = UIStyle.nutChinh("+");
        btnThemNcc.setToolTipText("Thêm nhà cung cấp mới");
        btnThemNcc.addActionListener(e -> themNhaCungCap());

        JPanel hangNcc = new JPanel(new BorderLayout(6, 0));
        hangNcc.setOpaque(false);
        hangNcc.add(cboNhaCungCap, BorderLayout.CENTER);
        hangNcc.add(btnThemNcc, BorderLayout.EAST);

        themHang(the, 0, "Mã phiếu", txtMaPhieu, "Ngày nhập", txtNgayNhap);
        themHang(the, 1, "Nhà cung cấp", hangNcc, "Nhân viên lập", txtNhanVien);

        return the;
    }

    private void themHang(JPanel the, int dong, String nhan1, JComponent c1,
                          String nhan2, JComponent c2) {

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = dong;
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0;
        g.weightx = 0;
        g.insets = new Insets(8, 5, 8, 25);
        the.add(UIStyle.nhan(nhan1), g);

        g.gridx = 1;
        g.weightx = 1;
        g.insets = new Insets(8, 0, 8, 40);
        the.add(c1, g);

        g.gridx = 2;
        g.weightx = 0;
        g.insets = new Insets(8, 0, 8, 25);
        the.add(UIStyle.nhan(nhan2), g);

        g.gridx = 3;
        g.weightx = 1;
        g.insets = new Insets(8, 0, 8, 5);
        the.add(c2, g);
    }

    private JPanel taoPhanChiTiet() {

        JPanel the = UIStyle.the();
        the.setLayout(new BorderLayout(0, 12));

        JButton btnThem = UIStyle.nutChinh("+ Thêm mặt hàng");
        JButton btnXoaDong = UIStyle.nutXoa("Xóa dòng");
        btnThem.addActionListener(e -> themMatHang());
        btnXoaDong.addActionListener(e -> xoaDong());

        JPanel thanhCongCu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        thanhCongCu.setOpaque(false);
        thanhCongCu.add(btnThem);
        thanhCongCu.add(btnXoaDong);

        UIStyle.taoBang(bang, 2, 3, 4);

        the.add(thanhCongCu, BorderLayout.NORTH);
        the.add(UIStyle.cuon(bang), BorderLayout.CENTER);

        return the;
    }

    private JPanel taoPhanCuoi() {

        lbTong.setFont(UIStyle.font(Font.BOLD, 22));

        JButton btnLuuNhap = UIStyle.nutPhu("Lưu nháp");
        JButton btnHoanTat = UIStyle.nutXanh("Hoàn tất nhập kho");
        btnLuuNhap.addActionListener(e -> luu(false));
        btnHoanTat.addActionListener(e -> luu(true));

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nut.setOpaque(false);
        nut.add(btnLuuNhap);
        nut.add(btnHoanTat);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(lbTong, BorderLayout.WEST);
        p.add(nut, BorderLayout.EAST);

        return p;
    }

    // =========================================================
    // NAP DU LIEU
    // =========================================================

    @Override
    public void napLai() {

        napNhaCungCap(null);

        // Chi doi ma phieu khi phieu dang trong
        if (dong.isEmpty()) {
            phieuMoi();
        }
    }

    private void napNhaCungCap(String maCanChon) {

        try {
            String maDangChon = maCanChon != null ? maCanChon
                    : (nccHienTai == null ? null : nccHienTai.getMaNhaCungCap());

            dangNapCombo = true;
            cboNhaCungCap.removeAllItems();

            NhaCungCap chon = null;

            for (NhaCungCap n : nhaCungCapDAO.getAll()) {
                cboNhaCungCap.addItem(n);
                if (n.getMaNhaCungCap().equals(maDangChon)) {
                    chon = n;
                }
            }

            if (chon != null) {
                cboNhaCungCap.setSelectedItem(chon);
            }

            nccHienTai = (NhaCungCap) cboNhaCungCap.getSelectedItem();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        } finally {
            dangNapCombo = false;
        }
    }

    private void phieuMoi() {

        try {
            txtMaPhieu.setText(phieuDAO.taoMaMoi());
        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }

        ngayNhap = LocalDateTime.now();
        txtNgayNhap.setText(ngayNhap.format(DINH_DANG_NGAY));
        txtNhanVien.setText(PhienLamViec.hoTen);

        dong.clear();
        veLaiBang();
    }

    private void veLaiBang() {

        model.setRowCount(0);

        double tong = 0;

        for (ChiTietPhieuNhap ct : dong) {
            model.addRow(new Object[]{
                    ct.getMaSanPham(),
                    ct.getTenSanPham(),
                    ct.getSoLuongNhap(),
                    UIStyle.tien(ct.getDonGiaNhap()),
                    UIStyle.tien(ct.getThanhTien())
            });
            tong += ct.getThanhTien();
        }

        lbTong.setText("Tổng thanh toán: " + UIStyle.tien(tong));
    }

    // =========================================================
    // NHA CUNG CAP
    // =========================================================

    // Doi nha cung cap: san pham trong phieu phai cung 1 nha cung cap
    private void doiNhaCungCap() {

        if (dangNapCombo) {
            return;
        }

        NhaCungCap moi = (NhaCungCap) cboNhaCungCap.getSelectedItem();

        if (moi == null || nccHienTai == null
                || moi.getMaNhaCungCap().equals(nccHienTai.getMaNhaCungCap())) {
            nccHienTai = moi;
            return;
        }

        if (!dong.isEmpty()) {
            if (!UIStyle.xacNhan(this, "Đổi nhà cung cấp sẽ xóa các mặt hàng "
                    + "đã thêm trong phiếu. Tiếp tục?")) {
                dangNapCombo = true;
                cboNhaCungCap.setSelectedItem(nccHienTai);
                dangNapCombo = false;
                return;
            }
            dong.clear();
            veLaiBang();
        }

        nccHienTai = moi;
    }

    private void themNhaCungCap() {

        try {
            NhaCungCapDialog dlg = new NhaCungCapDialog(
                    SwingUtilities.getWindowAncestor(this),
                    nhaCungCapDAO.taoMaMoi());
            dlg.setVisible(true);

            NhaCungCap moi = dlg.getKetQua();

            if (moi != null) {
                // Dang co hang trong phieu thi giu nguyen nha cung cap cu
                if (dong.isEmpty()) {
                    napNhaCungCap(moi.getMaNhaCungCap());
                } else {
                    napNhaCungCap(null);
                }
            }

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // =========================================================
    // MAT HANG
    // =========================================================

    private void themMatHang() {

        NhaCungCap ncc = (NhaCungCap) cboNhaCungCap.getSelectedItem();

        if (ncc == null) {
            UIStyle.canhBao(this, "Vui lòng chọn nhà cung cấp trước.");
            return;
        }

        try {
            ArrayList<SanPham> dsSanPham = sanPhamDAO.timKiem(
                    "", null, ncc.getMaNhaCungCap());

            if (dsSanPham.isEmpty()) {
                UIStyle.canhBao(this, "Nhà cung cấp \"" + ncc.getTenNhaCungCap()
                        + "\" chưa có sản phẩm nào.\n"
                        + "Hãy thêm sản phẩm thuộc nhà cung cấp này ở tab Sản phẩm.");
                return;
            }

            ChiTietNhapDialog dlg = new ChiTietNhapDialog(
                    SwingUtilities.getWindowAncestor(this), dsSanPham);
            dlg.setVisible(true);

            ChiTietPhieuNhap moi = dlg.getKetQua();

            if (moi == null) {
                return;
            }

            // Cung 1 san pham: cong don so luong (khoa chinh la ma phieu + ma SP)
            for (ChiTietPhieuNhap ct : dong) {
                if (ct.getMaSanPham().equals(moi.getMaSanPham())) {
                    ct.setSoLuongNhap(ct.getSoLuongNhap() + moi.getSoLuongNhap());
                    ct.setDonGiaNhap(moi.getDonGiaNhap());
                    veLaiBang();
                    return;
                }
            }

            dong.add(moi);
            veLaiBang();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    private void xoaDong() {

        int row = bang.getSelectedRow();

        if (row < 0) {
            UIStyle.canhBao(this, "Vui lòng chọn dòng cần xóa.");
            return;
        }

        dong.remove(row);
        veLaiBang();
    }

    // =========================================================
    // LUU PHIEU
    // =========================================================

    // hoanTat = true: trangThai 1 (tinh vao ton kho); false: luu nhap (trangThai 0)
    private void luu(boolean hoanTat) {

        NhaCungCap ncc = (NhaCungCap) cboNhaCungCap.getSelectedItem();

        if (ncc == null) {
            UIStyle.canhBao(this, "Vui lòng chọn nhà cung cấp.");
            return;
        }

        if (dong.isEmpty()) {
            UIStyle.canhBao(this, "Phiếu chưa có mặt hàng nào.");
            return;
        }

        if (hoanTat && !UIStyle.xacNhan(this,
                "Hoàn tất nhập kho phiếu " + txtMaPhieu.getText() + "?")) {
            return;
        }

        PhieuNhapKho phieu = new PhieuNhapKho(
                txtMaPhieu.getText(),
                ngayNhap,
                hoanTat,
                PhienLamViec.maNV,
                ncc.getMaNhaCungCap());

        try {
            phieuDAO.luu(phieu, dong);

            UIStyle.thongBao(this, hoanTat
                    ? "Đã nhập kho phiếu " + phieu.getMaPhieuNhap() + "."
                    : "Đã lưu nháp phiếu " + phieu.getMaPhieuNhap() + ".");

            phieuMoi();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }
}
