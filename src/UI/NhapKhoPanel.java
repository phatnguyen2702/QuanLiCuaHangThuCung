package UI;

import UI.TrangChu.PhienLamViec;
import UI.TrangChu.UIStyle;
import dao.NhapKhoDAO;
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

/**
 * Man hinh "Nhap kho" - Lap phieu nhap hang (nam trong khung TrangChu).
 * Cac form phu (nha cung cap, chi tiet nhap, phieu nhap nhap) la lop long o
 * cuoi file.
 */
public class NhapKhoPanel extends JPanel {

    private final NhapKhoDAO dao = new NhapKhoDAO();
    private final Runnable moTraCuu;

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ---- thong tin phieu ----
    private final JTextField txtMaPhieu = UIStyle.oChiDoc("");
    private final JTextField txtNgayNhap = UIStyle.oChiDoc("");
    private final JTextField txtNhanVien = UIStyle.oChiDoc(PhienLamViec.hoTen);
    private final JComboBox<NhaCungCap> cboNhaCungCap = new JComboBox<>();

    // ---- chi tiet ----
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã SP", "Tên mặt hàng", "Số lượng",
                    "Đơn giá nhập", "Hạn sử dụng", "Thành tiền" },
            0) {
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
        this(() -> { });
    }

    public NhapKhoPanel(Runnable moTraCuu) {
        this.moTraCuu = moTraCuu;

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

        JButton btnNhap = UIStyle.nutPhu("Phiếu nháp đã lưu");
        btnNhap.addActionListener(e -> moPhieuNhap());
        JButton btnTraCuu = UIStyle.nutPhu("Tra cứu nhập kho");
        btnTraCuu.addActionListener(e -> moTraCuu.run());
        JPanel nhomTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        nhomTrai.setOpaque(false);
        nhomTrai.add(btnThem);
        nhomTrai.add(btnXoaDong);
        nhomTrai.add(btnNhap);
        nhomTrai.add(btnTraCuu);

        UIStyle.taoBang(bang, 2, 3, 4);

        JButton btnPdf = UIStyle.nutPhu("Xuất phiếu nhập kho");
        btnPdf.addActionListener(e -> UIStyle.xuatPdf(this, "Phiếu nhập kho", bang));
        JPanel thanhCongCu = new JPanel(new BorderLayout());
        thanhCongCu.setOpaque(false);
        thanhCongCu.add(nhomTrai, BorderLayout.WEST);
        thanhCongCu.add(btnPdf, BorderLayout.EAST);

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

            for (NhaCungCap n : dao.layDanhSachNhaCungCap()) {
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
            txtMaPhieu.setText(dao.taoMaMoi());
        } catch (SQLException ex) {
            txtMaPhieu.setText("");
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
            model.addRow(new Object[] {
                    ct.getMaSanPham(),
                    ct.getTenSanPham(),
                    ct.getSoLuongNhap(),
                    UIStyle.tien(ct.getDonGiaNhap()),
                    ct.getHanSuDung() == null ? "—"
                            : ct.getHanSuDung().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
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
                    dao.taoMaNhaCungCap());
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
            ArrayList<SanPham> dsSanPham = dao.timSanPhamTheoNhaCungCap(
                    ncc.getMaNhaCungCap());

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
                    if (!java.util.Objects.equals(ct.getHanSuDung(), moi.getHanSuDung())) {
                        UIStyle.canhBao(this, "Sản phẩm này đã có trong phiếu với hạn sử dụng khác. "
                                + "Mỗi sản phẩm chỉ có một dòng; hãy xóa dòng cũ nếu cần thay hạn sử dụng.");
                        return;
                    }
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

    // Xem / hoan tat / xoa cac phieu da "Luu nhap"
    private void moPhieuNhap() {

        PhieuNhapNhapDialog dlg = new PhieuNhapNhapDialog(
                SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);

        PhieuNhapKho canSua = dlg.getPhieuSua();
        if (canSua != null) {
            if (!dong.isEmpty() && !UIStyle.xacNhan(this, "Nạp phiếu nháp sẽ thay nội dung đang nhập. Tiếp tục?"))
                return;
            try {
                napNhaCungCap(canSua.getMaNhaCungCap());
                txtMaPhieu.setText(canSua.getMaPhieuNhap());
                ngayNhap = canSua.getNgayNhap() == null ? LocalDateTime.now() : canSua.getNgayNhap();
                txtNgayNhap.setText(ngayNhap.format(DINH_DANG_NGAY));
                txtNhanVien.setText(canSua.getMaNhanVien());
                dong.clear();
                dong.addAll(dao.layChiTiet(canSua.getMaPhieuNhap()));
                veLaiBang();
                return;
            } catch (SQLException ex) {
                UIStyle.loi(this, ex);
            }
        }

        // Ma phieu moi co the bi doi (phieu nhap bi xoa / hoan tat)
        if (dlg.isCoThayDoi() && dong.isEmpty()) {
            phieuMoi();
        }
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

        String maPhieu = txtMaPhieu.getText().trim();

        if (maPhieu.isEmpty()) {
            UIStyle.canhBao(this, "Chưa tạo được mã phiếu. Vui lòng tải lại màn hình.");
            return;
        }

        if (maPhieu.length() > 10) {
            UIStyle.canhBao(this, "Mã phiếu tối đa 10 ký tự.");
            return;
        }

        if (dong.isEmpty()) {
            UIStyle.canhBao(this, "Phiếu chưa có mặt hàng nào.");
            return;
        }

        if (hoanTat && !UIStyle.xacNhan(this,
                "Hoàn tất nhập kho phiếu " + maPhieu + "?")) {
            return;
        }

        PhieuNhapKho phieu = new PhieuNhapKho(
                maPhieu,
                ngayNhap,
                hoanTat,
                PhienLamViec.maNV,
                ncc.getMaNhaCungCap());

        try {
            dao.luu(phieu, dong);

            UIStyle.thongBao(this, hoanTat
                    ? "Đã nhập kho phiếu " + phieu.getMaPhieuNhap() + "."
                    : "Đã lưu nháp phiếu " + phieu.getMaPhieuNhap() + ".");

            phieuMoi();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // =========================================================
    // FORM THEM NHA CUNG CAP
    // =========================================================

    /** Form Them nha cung cap: ma, ten, so dien thoai, email, dia chi. */
    private static class NhaCungCapDialog extends JDialog {

        private final NhapKhoDAO dao = new NhapKhoDAO();

        private final JTextField txtMa = UIStyle.o(22);
        private final JTextField txtTen = UIStyle.o(22);
        private final JTextField txtSdt = UIStyle.o(22);
        private final JTextField txtEmail = UIStyle.o(22);
        private final JTextField txtDiaChi = UIStyle.o(22);

        private NhaCungCap ketQua = null;

        public NhaCungCapDialog(Window cha, String maMoi) {

            super(cha, "Thêm nhà cung cấp", ModalityType.APPLICATION_MODAL);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(new EmptyBorder(20, 25, 10, 25));

            UIStyle.hang(form, 0, "Mã NCC", txtMa, false);
            UIStyle.hang(form, 1, "Tên NCC", txtTen, false);
            UIStyle.hang(form, 2, "Số điện thoại", txtSdt, false);
            UIStyle.hang(form, 3, "Email", txtEmail, false);
            UIStyle.hang(form, 4, "Địa chỉ", txtDiaChi, false);

            txtMa.setText(maMoi);

            JButton btnHuy = UIStyle.nutPhu("Hủy");
            JButton btnLuu = UIStyle.nutXanh("Lưu");
            btnHuy.addActionListener(e -> dispose());
            btnLuu.addActionListener(e -> luu());

            JPanel nut = UIStyle.hangNut(btnHuy, btnLuu);
            nut.setBorder(new EmptyBorder(5, 25, 20, 25));

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(nut, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(btnLuu);

            pack();
            setResizable(false);
            setLocationRelativeTo(cha);
        }

        private String rongThanhNull(String s) {
            s = s.trim();
            return s.isEmpty() ? null : s;
        }

        private void luu() {

            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String sdt = txtSdt.getText().trim();
            String email = txtEmail.getText().trim();
            String diaChi = txtDiaChi.getText().trim();

            if (ma.isEmpty() || ten.isEmpty()) {
                UIStyle.canhBao(this, "Vui lòng nhập mã và tên nhà cung cấp.");
                return;
            }

            if (ma.length() > 10 || ten.length() > 50
                    || sdt.length() > 15 || email.length() > 50
                    || diaChi.length() > 50) {
                UIStyle.canhBao(this,
                        "Độ dài tối đa: mã 10, tên 50, SĐT 15, email 50, địa chỉ 50 ký tự.");
                return;
            }

            if (!sdt.isEmpty() && !sdt.matches("[0-9+ .-]+")) {
                UIStyle.canhBao(this, "Số điện thoại chỉ gồm chữ số.");
                return;
            }

            if (!email.isEmpty() && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
                UIStyle.canhBao(this, "Email chưa đúng định dạng.");
                return;
            }

            NhaCungCap n = new NhaCungCap(ma, ten,
                    rongThanhNull(sdt), rongThanhNull(email), rongThanhNull(diaChi));

            try {
                dao.themNhaCungCap(n);
                ketQua = n;
                dispose();

            } catch (SQLException ex) {
                UIStyle.loi(this, ex);
            }
        }

        /** Nha cung cap vua them, hoac null neu bam Huy. */
        public NhaCungCap getKetQua() {
            return ketQua;
        }
    }

    // =========================================================
    // FORM THEM MAT HANG VAO PHIEU NHAP
    // =========================================================

    /**
     * Form "Them mat hang vao phieu nhap": san pham, so luong nhap, don gia nhap.
     */
    private static class ChiTietNhapDialog extends JDialog {

        private final JComboBox<SanPham> cboSanPham = new JComboBox<>();
        private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
        private final JTextField txtDonGia = UIStyle.o(22);
        private final JTextField txtHanSuDung = UIStyle.o(12);

        private ChiTietPhieuNhap ketQua = null;

        public ChiTietNhapDialog(Window cha, List<SanPham> dsSanPham) {

            super(cha, "Thêm mặt hàng vào phiếu nhập",
                    ModalityType.APPLICATION_MODAL);

            for (SanPham sp : dsSanPham) {
                cboSanPham.addItem(sp);
            }
            cboSanPham.setFont(UIStyle.font(Font.PLAIN, 13));
            spnSoLuong.setFont(UIStyle.font(Font.PLAIN, 13));

            // Chon san pham -> tu dien gia nhap dang co trong bang SanPham
            cboSanPham.addActionListener(e -> {
                SanPham sp = (SanPham) cboSanPham.getSelectedItem();
                if (sp != null) {
                    txtDonGia.setText(String.valueOf((long) sp.getGiaNhap()));
                }
            });

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(new EmptyBorder(20, 25, 10, 25));

            UIStyle.hang(form, 0, "Sản phẩm", cboSanPham, false);
            UIStyle.hang(form, 1, "Số lượng nhập", spnSoLuong, false);
            UIStyle.hang(form, 2, "Đơn giá nhập", txtDonGia, false);
            UIStyle.hang(form, 3, "Hạn sử dụng (dd/MM/yyyy, tùy chọn)", txtHanSuDung, false);

            if (cboSanPham.getItemCount() > 0) {
                cboSanPham.setSelectedIndex(0);
                txtDonGia.setText(String.valueOf(
                        (long) dsSanPham.get(0).getGiaNhap()));
            }

            JButton btnHuy = UIStyle.nutPhu("Hủy");
            JButton btnThem = UIStyle.nutXanh("Thêm vào phiếu");
            btnHuy.addActionListener(e -> dispose());
            btnThem.addActionListener(e -> them());

            JPanel nut = UIStyle.hangNut(btnHuy, btnThem);
            nut.setBorder(new EmptyBorder(5, 25, 20, 25));

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(nut, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(btnThem);

            pack();
            setMinimumSize(new Dimension(420, getHeight()));
            setResizable(false);
            setLocationRelativeTo(cha);
        }

        private void them() {

            SanPham sp = (SanPham) cboSanPham.getSelectedItem();

            if (sp == null) {
                UIStyle.canhBao(this, "Vui lòng chọn sản phẩm.");
                return;
            }

            long donGia;

            try {
                donGia = UIStyle.docTien(txtDonGia.getText());
            } catch (NumberFormatException e) {
                UIStyle.canhBao(this, "Đơn giá nhập phải là số không âm.");
                return;
            }

            if (donGia <= 0) {
                UIStyle.canhBao(this, "Đơn giá nhập phải lớn hơn 0.");
                return;
            }
            java.time.LocalDate han = null;
            try {
                if (!txtHanSuDung.getText().trim().isEmpty())
                    han = java.time.LocalDate.parse(txtHanSuDung.getText().trim(),
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu"));
            } catch (java.time.format.DateTimeParseException e) {
                UIStyle.canhBao(this, "Hạn sử dụng phải theo định dạng dd/MM/yyyy.");
                return;
            }
            if (han != null && han.isBefore(java.time.LocalDate.now())) {
                UIStyle.canhBao(this, "Hạn sử dụng không thể ở quá khứ.");
                return;
            }
            ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
            ct.setHanSuDung(han);
            ct.setMaSanPham(sp.getMaSanPham());
            ct.setTenSanPham(sp.getTenSanPham());
            ct.setSoLuongNhap((Integer) spnSoLuong.getValue());
            ct.setDonGiaNhap(donGia);

            ketQua = ct;
            dispose();
        }

        /** Dong chi tiet vua nhap, hoac null neu bam Huy. */
        public ChiTietPhieuNhap getKetQua() {
            return ketQua;
        }
    }

    // =========================================================
    // DANH SACH PHIEU NHAP DANG LUU NHAP
    // =========================================================

    /**
     * Danh sach phieu nhap kho da "Luu nhap" (trangThai = 0, chua tinh vao ton):
     * cho phep Hoan tat nhap kho hoac Xoa phieu nhap.
     */
    private static class PhieuNhapNhapDialog extends JDialog {

        private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        private final NhapKhoDAO dao = new NhapKhoDAO();

        private final DefaultTableModel model = new DefaultTableModel(
                new String[] { "Mã phiếu", "Ngày lập", "Nhà cung cấp",
                        "Số mặt hàng", "Tổng tiền" },
                0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        private final JTable bang = new JTable(model);
        private ArrayList<PhieuNhapKho> ds = new ArrayList<>();

        private boolean coThayDoi = false;
        private PhieuNhapKho phieuSua;

        public PhieuNhapNhapDialog(Window cha) {

            super(cha, "Phiếu nhập đang lưu nháp", ModalityType.APPLICATION_MODAL);

            UIStyle.taoBang(bang, 3, 4);

            JScrollPane cuon = UIStyle.cuon(bang);
            cuon.setPreferredSize(new Dimension(700, 280));

            JButton btnHoanTat = UIStyle.nutXanh("Hoàn tất nhập kho");
            JButton btnSua = UIStyle.nutChinh("Sửa / tiếp tục");
            JButton btnXoa = UIStyle.nutXoa("Xóa phiếu nháp");
            JButton btnDong = UIStyle.nutPhu("Đóng");

            btnHoanTat.addActionListener(e -> hoanTat());
            btnSua.addActionListener(e -> sua());
            btnXoa.addActionListener(e -> xoa());
            btnDong.addActionListener(e -> dispose());

            JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            nut.setOpaque(false);
            nut.add(btnHoanTat);
            nut.add(btnSua);
            nut.add(btnXoa);
            nut.add(btnDong);

            JPanel noiDung = new JPanel(new BorderLayout(0, 12));
            noiDung.setBorder(new EmptyBorder(18, 20, 18, 20));
            noiDung.add(cuon, BorderLayout.CENTER);
            noiDung.add(nut, BorderLayout.SOUTH);

            setContentPane(noiDung);
            pack();
            setLocationRelativeTo(cha);

            nap();
        }

        private void nap() {

            try {
                ds = dao.dsPhieuNhap();

                model.setRowCount(0);
                for (PhieuNhapKho p : ds) {
                    model.addRow(new Object[] {
                            p.getMaPhieuNhap(),
                            p.getNgayNhap() == null ? "" : p.getNgayNhap().format(NGAY),
                            p.getTenNhaCungCap(),
                            p.getSoMatHang(),
                            UIStyle.tien(p.getTongTien())
                    });
                }

            } catch (SQLException ex) {
                UIStyle.loi(this, ex);
            }
        }

        private PhieuNhapKho dangChon() {

            int row = bang.getSelectedRow();

            if (row < 0) {
                UIStyle.canhBao(this, "Vui lòng chọn một phiếu nháp trong bảng.");
                return null;
            }

            return ds.get(row);
        }

        private void hoanTat() {

            PhieuNhapKho p = dangChon();
            if (p == null) {
                return;
            }

            if (p.getSoMatHang() == 0) {
                UIStyle.canhBao(this, "Phiếu này không có mặt hàng nào để nhập.");
                return;
            }

            if (!UIStyle.xacNhan(this, "Hoàn tất nhập kho phiếu "
                    + p.getMaPhieuNhap() + "? Số lượng sẽ được cộng vào tồn kho.")) {
                return;
            }

            try {
                dao.hoanTatPhieuNhap(p.getMaPhieuNhap());
                coThayDoi = true;
                nap();

            } catch (SQLException ex) {
                UIStyle.loi(this, ex);
            }
        }

        private void sua() {
            PhieuNhapKho p = dangChon();
            if (p == null)
                return;
            phieuSua = p;
            dispose();
        }

        private void xoa() {

            PhieuNhapKho p = dangChon();
            if (p == null) {
                return;
            }

            if (!UIStyle.xacNhan(this,
                    "Xóa phiếu nháp " + p.getMaPhieuNhap() + "?")) {
                return;
            }

            try {
                dao.xoaPhieuNhap(p.getMaPhieuNhap());
                coThayDoi = true;
                nap();

            } catch (SQLException ex) {
                UIStyle.loi(this, ex);
            }
        }

        public boolean isCoThayDoi() {
            return coThayDoi;
        }

        public PhieuNhapKho getPhieuSua() {
            return phieuSua;
        }
    }
}
