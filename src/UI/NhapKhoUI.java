package UI;
import UI.TrangChuUI.UIStyle;
import control.NhapKhoControl;
import control.TrangChuControl;
import entity.ChiTietPhieuNhap;
import entity.NhaCungCap;
import entity.PhieuNhapKho;
import entity.SanPham;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
public class NhapKhoUI extends JFrame {
private final Runnable moTraCuu;
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter NGAY_THANG = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private final JTextField txtMaPhieu = UIStyle.oChiDoc("");
    private final JTextField txtNgayNhap = UIStyle.oChiDoc("");
    private final JTextField txtNhanVien = UIStyle.oChiDoc(TrangChuControl.hoTen);
    private final JComboBox<NhaCungCap> cmbNhaCungCap = new JComboBox<>();
    private final DefaultTableModel model = modelChiDoc("Mã SP", "Tên mặt hàng", "Số lượng",
            "Đơn giá nhập", "Hạn sử dụng", "Thành tiền");
    private final JTable tblPhieuNhap = new JTable(model);
    private final JLabel lblTong = new JLabel("Tổng thanh toán: 0 ₫");
    private final List<ChiTietPhieuNhap> dong = new ArrayList<>();
    private LocalDateTime ngayNhap = LocalDateTime.now();
    private NhaCungCap nccHienTai = null;
    private boolean dangNapCombo = false;
    private boolean daNapDongMau = false;
    private static DefaultTableModel modelChiDoc(String... tenCot) {
        return new DefaultTableModel(tenCot, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }
    private static String ngay(LocalDate date) { return date == null ? "—" : date.format(NGAY_THANG); }
    private static void trongSuot(JComponent... thanhPhan) {
        for (JComponent item : thanhPhan) item.setOpaque(false);
    }
    public NhapKhoUI() {
        this(() -> {
        });
    }
    public NhapKhoUI(Runnable moTraCuu) {
        this.moTraCuu = moTraCuu;
        setLayout(new BorderLayout(0, 12));
        getContentPane().setBackground(UIStyle.LIGHT_BLUE);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(20, 25, 20, 25));
        JLabel lblTieuDe = new JLabel("Lập phiếu nhập hàng");
        lblTieuDe.setFont(UIStyle.font(Font.BOLD, 26));
        JPanel pnlPhanDau = new JPanel(new BorderLayout(0, 12));
        trongSuot(pnlPhanDau);
        pnlPhanDau.add(lblTieuDe, BorderLayout.NORTH);
        pnlPhanDau.add(taoThongTinPhieu(), BorderLayout.CENTER);
        add(pnlPhanDau, BorderLayout.NORTH);
        add(taoPhanChiTiet(), BorderLayout.CENTER);
        add(taoPhanCuoi(), BorderLayout.SOUTH);
        cmbNhaCungCap.addActionListener(e -> capNhatSanPhamTheoNhaCungCap());
        napLai();
    }
    private JPanel taoThongTinPhieu() {
        JPanel pnlThe = UIStyle.the();
        pnlThe.setLayout(new GridBagLayout());
        cmbNhaCungCap.setFont(UIStyle.font(Font.PLAIN, 13));
        JButton btnThemNcc = UIStyle.nutChinh("+");
        btnThemNcc.setToolTipText("Thêm nhà cung cấp mới");
        btnThemNcc.addActionListener(e -> themNhaCungCapMoi());
        JPanel pnlHangNcc = new JPanel(new BorderLayout(6, 0));
        trongSuot(pnlHangNcc);
        pnlHangNcc.add(cmbNhaCungCap, BorderLayout.CENTER);
        pnlHangNcc.add(btnThemNcc, BorderLayout.EAST);
        themHang(pnlThe, 0, "Mã phiếu", txtMaPhieu, "Ngày nhập", txtNgayNhap);
        themHang(pnlThe, 1, "Nhà cung cấp", pnlHangNcc, "Nhân viên lập", txtNhanVien);
        return pnlThe;
    }
    private void themHang(JPanel pnlThe, int dong, String nhan1, JComponent c1,
            String nhanPhai, JComponent thanhPhanPhai) {
        themO(pnlThe, dong, 0, UIStyle.nhan(nhan1), 0, new Insets(8, 5, 8, 25));
        themO(pnlThe, dong, 1, c1, 1, new Insets(8, 0, 8, 40));
        themO(pnlThe, dong, 2, UIStyle.nhan(nhanPhai), 0, new Insets(8, 0, 8, 25));
        themO(pnlThe, dong, 3, thanhPhanPhai, 1, new Insets(8, 0, 8, 5));
    }
    private void themO(JPanel panel, int hang, int cot, JComponent component, double trongSo, Insets le) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = hang;
        gbc.gridx = cot;
        gbc.weightx = trongSo;
        gbc.insets = le;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }
    private JPanel taoPhanChiTiet() {
        JPanel pnlThe = UIStyle.the();
        pnlThe.setLayout(new BorderLayout(0, 12));
        JButton btnThem = UIStyle.nutChinh("+ Thêm mặt hàng");
        JButton btnXoaDong = UIStyle.nutXoa("Xóa mặt hàng");
        btnThem.addActionListener(e -> themSanPhamVaoPhieuNhap());
        btnXoaDong.addActionListener(e -> xoaSanPhamKhoiPhieuNhap());
        JButton btnNhap = UIStyle.nutPhu("Phiếu nháp đã lưu");
        btnNhap.addActionListener(e -> moDanhSachPhieuNhap());
        JButton btnTraCuu = UIStyle.nutPhu("Tra cứu nhập kho");
        btnTraCuu.addActionListener(e -> moTraCuu.run());
        JPanel pnlNhomTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        trongSuot(pnlNhomTrai);
        pnlNhomTrai.add(btnThem);
        pnlNhomTrai.add(btnXoaDong);
        pnlNhomTrai.add(btnNhap);
        pnlNhomTrai.add(btnTraCuu);
        UIStyle.taoBang(tblPhieuNhap, 2, 3, 4);
        JLabel lblHuongDanPdf = new JLabel("Xuất PDF: Tra cứu nhập kho → Xem chi tiết");
        lblHuongDanPdf.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHuongDanPdf.setForeground(new Color(85, 101, 120));
        JPanel pnlThanhCongCu = new JPanel(new BorderLayout());
        trongSuot(pnlThanhCongCu);
        pnlThanhCongCu.add(pnlNhomTrai, BorderLayout.WEST);
        pnlThanhCongCu.add(lblHuongDanPdf, BorderLayout.EAST);
        pnlThe.add(pnlThanhCongCu, BorderLayout.NORTH);
        pnlThe.add(UIStyle.cuon(tblPhieuNhap), BorderLayout.CENTER);
        return pnlThe;
    }
    private JPanel taoPhanCuoi() {
        lblTong.setFont(UIStyle.font(Font.BOLD, 22));
        JButton btnLuuNhap = UIStyle.nutPhu("Lưu nháp");
        JButton btnHoanTat = UIStyle.nutXanh("Hoàn tất nhập kho");
        btnLuuNhap.addActionListener(e -> luuPhieuNhap(false));
        btnHoanTat.addActionListener(e -> luuPhieuNhap(true));
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        trongSuot(pnlNut);
        pnlNut.add(btnLuuNhap);
        pnlNut.add(btnHoanTat);
        JPanel pnlKhung = new JPanel(new BorderLayout());
        trongSuot(pnlKhung);
        pnlKhung.add(lblTong, BorderLayout.WEST);
        pnlKhung.add(pnlNut, BorderLayout.EAST);
        return pnlKhung;
    }
    public void napLai() {
        napNhaCungCap(null);
        if (dong.isEmpty()) {
            taoPhieuNhapMoi();
            if (!daNapDongMau) {
                napDongSanPhamMau();
                daNapDongMau = cmbNhaCungCap.getSelectedItem() != null;
            }
        }
    }
    private void napDongSanPhamMau() {
        NhaCungCap ncc = (NhaCungCap) cmbNhaCungCap.getSelectedItem();
        if (ncc == null)
            return;
        try {
            ArrayList<SanPham> sanPham = NhapKhoControl.timSanPhamTheoNhaCungCap(ncc.getMaNhaCungCap());
            int soDong = Math.min(3, sanPham.size());
            for (int i = 0; i < soDong; i++) {
                SanPham sp = sanPham.get(i);
                ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
                ct.setMaPhieuNhap(txtMaPhieu.getText());
                ct.setMaSanPham(sp.getMaSanPham());
                ct.setTenSanPham(sp.getTenSanPham());
                ct.setSoLuongNhap(1);
                ct.setDonGiaNhap(sp.getGiaNhap());
                dong.add(ct);
            }
            capNhatBangChiTietPhieuNhap();
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private void napNhaCungCap(String maCanChon) {
        try {
            String maDangChon = maCanChon != null ? maCanChon
                    : (nccHienTai == null ? null : nccHienTai.getMaNhaCungCap());
            dangNapCombo = true;
            cmbNhaCungCap.removeAllItems();
            NhaCungCap chon = null;
            for (NhaCungCap n : NhapKhoControl.layDanhSachNhaCungCap()) {
                cmbNhaCungCap.addItem(n);
                if (n.getMaNhaCungCap().equals(maDangChon)) {
                    chon = n;
                }
            }
            if (chon != null) {
                cmbNhaCungCap.setSelectedItem(chon);
            }
            nccHienTai = (NhaCungCap) cmbNhaCungCap.getSelectedItem();
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        } finally {
            dangNapCombo = false;
        }
    }
    private void taoPhieuNhapMoi() {
        try {
            txtMaPhieu.setText(NhapKhoControl.taoMaPhieuNhapMoi());
        } catch (Exception ex) {
            txtMaPhieu.setText("");
            UIStyle.loi(this, ex);
        }
        ngayNhap = LocalDateTime.now();
        txtNgayNhap.setText(ngayNhap.format(NGAY_GIO));
        txtNhanVien.setText(TrangChuControl.hoTen);
        dong.clear();
        capNhatBangChiTietPhieuNhap();
    }
    private void capNhatBangChiTietPhieuNhap() {
        model.setRowCount(0);
        double tong = 0;
        for (ChiTietPhieuNhap ct : dong) {
            model.addRow(new Object[] {
                    ct.getMaSanPham(),
                    ct.getTenSanPham(),
                    ct.getSoLuongNhap(),
                    UIStyle.tien(ct.getDonGiaNhap()),
                    ct.getHanSuDung() == null ? "—"
                            : ngay(ct.getHanSuDung()),
                    UIStyle.tien(ct.getThanhTien())
            });
            tong += ct.getThanhTien();
        }
        lblTong.setText("Tổng thanh toán: " + UIStyle.tien(tong));
    }
    private void capNhatSanPhamTheoNhaCungCap() {
        if (dangNapCombo) {
            return;
        }
        NhaCungCap moi = (NhaCungCap) cmbNhaCungCap.getSelectedItem();
        if (moi == null || nccHienTai == null
                || moi.getMaNhaCungCap().equals(nccHienTai.getMaNhaCungCap())) {
            nccHienTai = moi;
            return;
        }
        if (!dong.isEmpty()) {
            if (!UIStyle.xacNhan(this, "Đổi nhà cung cấp sẽ xóa các mặt hàng "
                    + "đã thêm trong phiếu. Tiếp tục?")) {
                dangNapCombo = true;
                cmbNhaCungCap.setSelectedItem(nccHienTai);
                dangNapCombo = false;
                return;
            }
            dong.clear();
            capNhatBangChiTietPhieuNhap();
        }
        nccHienTai = moi;
    }
    private void themNhaCungCapMoi() {
        try {
            NhaCungCapDialog winDlg = new NhaCungCapDialog(
                    SwingUtilities.getWindowAncestor(this),
                    NhapKhoControl.taoMaNhaCungCapMoi());
            winDlg.setVisible(true);
            NhaCungCap moi = winDlg.getKetQua();
            if (moi != null) {
                napNhaCungCap(dong.isEmpty() ? moi.getMaNhaCungCap() : null);
            }
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private void themSanPhamVaoPhieuNhap() {
        NhaCungCap ncc = (NhaCungCap) cmbNhaCungCap.getSelectedItem();
        if (ncc == null) {
            UIStyle.canhBao(this, "Vui lòng chọn nhà cung cấp trước.");
            return;
        }
        try {
            ArrayList<SanPham> dsSanPham = NhapKhoControl.timSanPhamTheoNhaCungCap(
                    ncc.getMaNhaCungCap());
            if (dsSanPham.isEmpty()) {
                UIStyle.canhBao(this, "Nhà cung cấp \"" + ncc.getTenNhaCungCap()
                        + "\" chưa có sản phẩm nào.\n"
                        + "Hãy thêm sản phẩm thuộc nhà cung cấp này ở tab Sản phẩm.");
                return;
            }
            ChiTietNhapDialog winDlg = new ChiTietNhapDialog(
                    SwingUtilities.getWindowAncestor(this), dsSanPham);
            winDlg.setVisible(true);
            ChiTietPhieuNhap moi = winDlg.getKetQua();
            if (moi == null) {
                return;
            }
            for (ChiTietPhieuNhap ct : dong) {
                if (ct.getMaSanPham().equals(moi.getMaSanPham())) {
                    if (!java.util.Objects.equals(ct.getHanSuDung(), moi.getHanSuDung())) {
                        UIStyle.canhBao(this, "Sản phẩm này đã có trong phiếu với hạn sử dụng khác. "
                                + "Mỗi sản phẩm chỉ có một dòng; hãy xóa dòng cũ nếu cần thay hạn sử dụng.");
                        return;
                    }
                    ct.setSoLuongNhap(ct.getSoLuongNhap() + moi.getSoLuongNhap());
                    ct.setDonGiaNhap(moi.getDonGiaNhap());
                    capNhatBangChiTietPhieuNhap();
                    return;
                }
            }
            dong.add(moi);
            capNhatBangChiTietPhieuNhap();
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private void xoaSanPhamKhoiPhieuNhap() {
        int row = tblPhieuNhap.getSelectedRow();
        if (row < 0) {
            UIStyle.canhBao(this, "Vui lòng chọn dòng cần xóa.");
            return;
        }
        dong.remove(row);
        capNhatBangChiTietPhieuNhap();
    }
    private void moDanhSachPhieuNhap() {
        PhieuNhapNhapDialog winDlg = new PhieuNhapNhapDialog(
                SwingUtilities.getWindowAncestor(this));
        winDlg.setVisible(true);
        PhieuNhapKho canSua = winDlg.getPhieuSua();
        if (canSua != null) {
            if (!dong.isEmpty() && !UIStyle.xacNhan(this, "Nạp phiếu nháp sẽ thay nội dung đang nhập. Tiếp tục?"))
                return;
            try {
                napNhaCungCap(canSua.getMaNhaCungCap());
                txtMaPhieu.setText(canSua.getMaPhieuNhap());
                ngayNhap = canSua.getNgayNhap() == null ? LocalDateTime.now() : canSua.getNgayNhap();
                txtNgayNhap.setText(ngayNhap.format(NGAY_GIO));
                txtNhanVien.setText(canSua.getMaNhanVien());
                dong.clear();
                dong.addAll(NhapKhoControl.layChiTietPhieuNhap(canSua.getMaPhieuNhap()));
                capNhatBangChiTietPhieuNhap();
                return;
            } catch (Exception ex) {
                UIStyle.loi(this, ex);
            }
        }
        if (winDlg.isCoThayDoi() && dong.isEmpty()) {
            taoPhieuNhapMoi();
        }
    }
    private void luuPhieuNhap(boolean hoanTatPhieuNhap) {
        NhaCungCap ncc = (NhaCungCap) cmbNhaCungCap.getSelectedItem();
        if (ncc == null) {
            UIStyle.canhBao(this, "Vui lòng chọn nhà cung cấp.");
            return;
        }
        String maPhieu = txtMaPhieu.getText().trim();
        if (maPhieu.isEmpty()) {
            UIStyle.canhBao(this, "Chưa tạo được mã phiếu. Vui lòng tải lại màn hình.");
            return;
        }
        if (dong.isEmpty()) {
            UIStyle.canhBao(this, "Phiếu chưa có mặt hàng nào.");
            return;
        }
        if (hoanTatPhieuNhap && !UIStyle.xacNhan(this,
                "Hoàn tất nhập kho phiếu " + maPhieu + "?")) {
            return;
        }
        PhieuNhapKho phieu = new PhieuNhapKho(
                maPhieu,
                ngayNhap,
                hoanTatPhieuNhap,
                TrangChuControl.maNV,
                ncc.getMaNhaCungCap());
        try {
            if (!NhapKhoControl.luuPhieuNhap(phieu, dong)) throw new IllegalStateException("Không lưu được phiếu nhập.");
            String ketQua = hoanTatPhieuNhap ? "Đã nhập kho phiếu " : "Đã lưu nháp phiếu ";
            UIStyle.thongBao(this, ketQua + phieu.getMaPhieuNhap() + ".");
            taoPhieuNhapMoi();
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private static class NhaCungCapDialog extends JDialog {
        private final JTextField txtMa = UIStyle.o(22);
        private final JTextField txtTen = UIStyle.o(22);
        private final JTextField txtSdt = UIStyle.o(22);
        private final JTextField txtEmail = UIStyle.o(22);
        private final JTextField txtDiaChi = UIStyle.o(22);
        private NhaCungCap ketQua = null;
        public NhaCungCapDialog(Window winCha, String maMoi) {
            super(winCha, "Thêm nhà cung cấp", ModalityType.APPLICATION_MODAL);
            JPanel pnlForm = new JPanel(new GridBagLayout());
            pnlForm.setBorder(new EmptyBorder(20, 25, 10, 25));
            UIStyle.hang(pnlForm, 0, "Mã NCC", txtMa, false);
            UIStyle.hang(pnlForm, 1, "Tên NCC", txtTen, false);
            UIStyle.hang(pnlForm, 2, "Số điện thoại", txtSdt, false);
            UIStyle.hang(pnlForm, 3, "Email", txtEmail, false);
            UIStyle.hang(pnlForm, 4, "Địa chỉ", txtDiaChi, false);
            txtMa.setText(maMoi);
            JButton btnHuy = UIStyle.nutPhu("Hủy");
            JButton btnLuu = UIStyle.nutXanh("Lưu");
            btnHuy.addActionListener(e -> dispose());
            btnLuu.addActionListener(e -> luuNhaCungCap());
            JPanel pnlNut = UIStyle.hangNut(btnHuy, btnLuu);
            pnlNut.setBorder(new EmptyBorder(5, 25, 20, 25));
            setLayout(new BorderLayout());
            add(pnlForm, BorderLayout.CENTER);
            add(pnlNut, BorderLayout.SOUTH);
            getRootPane().setDefaultButton(btnLuu);
            pack();
            setResizable(false);
            setLocationRelativeTo(winCha);
        }
        private static String chuyenChuoiRongThanhNull(String s) {
            return s.isEmpty() ? null : s;
        }
        private void luuNhaCungCap() {
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
                    chuyenChuoiRongThanhNull(sdt), chuyenChuoiRongThanhNull(email), chuyenChuoiRongThanhNull(diaChi));
            try {
                if (!NhapKhoControl.themNhaCungCap(n)) throw new IllegalStateException("Không thêm được nhà cung cấp.");
                ketQua = n;
                dispose();
            } catch (Exception ex) {
                UIStyle.loi(this, ex);
            }
        }
        public NhaCungCap getKetQua() {
            return ketQua;
        }
    }
    private static class ChiTietNhapDialog extends JDialog {
        private final JComboBox<SanPham> cmbSanPham = new JComboBox<>();
        private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
        private final JTextField txtDonGia = UIStyle.o(22);
        private final JTextField txtHanSuDung = UIStyle.o(12);
        private ChiTietPhieuNhap ketQua = null;
        public ChiTietNhapDialog(Window winCha, List<SanPham> dsSanPham) {
            super(winCha, "Thêm mặt hàng vào phiếu nhập",
                    ModalityType.APPLICATION_MODAL);
            for (SanPham sp : dsSanPham) {
                cmbSanPham.addItem(sp);
            }
            cmbSanPham.setFont(UIStyle.font(Font.PLAIN, 13));
            spnSoLuong.setFont(UIStyle.font(Font.PLAIN, 13));
            cmbSanPham.addActionListener(e -> {
                SanPham sp = (SanPham) cmbSanPham.getSelectedItem();
                if (sp != null) {
                    txtDonGia.setText(String.valueOf((long) sp.getGiaNhap()));
                }
            });
            JPanel pnlForm = new JPanel(new GridBagLayout());
            pnlForm.setBorder(new EmptyBorder(20, 25, 10, 25));
            UIStyle.hang(pnlForm, 0, "Sản phẩm", cmbSanPham, false);
            UIStyle.hang(pnlForm, 1, "Số lượng nhập", spnSoLuong, false);
            UIStyle.hang(pnlForm, 2, "Đơn giá nhập", txtDonGia, false);
            JPanel pnlHanSuDung = new JPanel(new BorderLayout(5, 0));
            trongSuot(pnlHanSuDung);
            JButton btnChonNgay = UIStyle.nutPhu("Chọn ngày");
            btnChonNgay.setToolTipText("Mở lịch để chọn hạn sử dụng");
            btnChonNgay.addActionListener(e -> chonHanSuDungSanPham(btnChonNgay));
            pnlHanSuDung.add(txtHanSuDung, BorderLayout.CENTER);
            pnlHanSuDung.add(btnChonNgay, BorderLayout.EAST);
            UIStyle.hang(pnlForm, 3, "Hạn sử dụng (tùy chọn)", pnlHanSuDung, false);
            if (cmbSanPham.getItemCount() > 0) {
                txtDonGia.setText(String.valueOf(
                        (long) dsSanPham.get(0).getGiaNhap()));
            }
            JButton btnHuy = UIStyle.nutPhu("Hủy");
            JButton btnThem = UIStyle.nutXanh("Thêm vào phiếu");
            btnHuy.addActionListener(e -> dispose());
            btnThem.addActionListener(e -> themChiTietPhieuNhap());
            JPanel pnlNut = UIStyle.hangNut(btnHuy, btnThem);
            pnlNut.setBorder(new EmptyBorder(5, 25, 20, 25));
            setLayout(new BorderLayout());
            add(pnlForm, BorderLayout.CENTER);
            add(pnlNut, BorderLayout.SOUTH);
            getRootPane().setDefaultButton(btnThem);
            pack();
            setMinimumSize(new Dimension(420, getHeight()));
            setResizable(false);
            setLocationRelativeTo(winCha);
        }
        private void themChiTietPhieuNhap() {
            SanPham sp = (SanPham) cmbSanPham.getSelectedItem();
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
            LocalDate han;
            try {
                han = docHanSuDung();
            } catch (DateTimeParseException e) {
                UIStyle.canhBao(this, "Hạn sử dụng phải theo định dạng dd/MM/yyyy.");
                return;
            }
            if (han != null && han.isBefore(LocalDate.now())) {
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
        private LocalDate docHanSuDung() {
            String text = txtHanSuDung.getText().trim();
            return text.isEmpty() ? null : LocalDate.parse(text, NGAY_THANG);
        }
        public ChiTietPhieuNhap getKetQua() {
            return ketQua;
        }
        private void chonHanSuDungSanPham(Component anchor) {
            LocalDate selected = null;
            try {
                selected = docHanSuDung();
            } catch (DateTimeParseException ignored) {
            }
            LocalDate date = LichChonNgay.chonNgayLichHen(anchor, selected, false);
            if (date != null) txtHanSuDung.setText(date.format(NGAY_THANG));
        }
    }
    private static class PhieuNhapNhapDialog extends JDialog {
        private final DefaultTableModel model = modelChiDoc("Mã phiếu", "Ngày lập", "Nhà cung cấp",
                "Số mặt hàng", "Tổng tiền");
        private final JTable tblPhieuNhap = new JTable(model);
        private ArrayList<PhieuNhapKho> ds = new ArrayList<>();
        private boolean coThayDoi = false;
        private PhieuNhapKho phieuSua;
        public PhieuNhapNhapDialog(Window winCha) {
            super(winCha, "Phiếu nhập đang lưu nháp", ModalityType.APPLICATION_MODAL);
            UIStyle.taoBang(tblPhieuNhap, 3, 4);
            JScrollPane scrCuon = UIStyle.cuon(tblPhieuNhap);
            scrCuon.setPreferredSize(new Dimension(700, 280));
            JButton btnHoanTat = UIStyle.nutXanh("Hoàn tất nhập kho");
            JButton btnSua = UIStyle.nutChinh("Sửa / tiếp tục");
            JButton btnXoa = UIStyle.nutXoa("Xóa phiếu nháp");
            JButton btnDong = UIStyle.nutPhu("Đóng");
            btnHoanTat.addActionListener(e -> hoanTatPhieuNhap());
            btnSua.addActionListener(e -> suaPhieuNhap());
            btnXoa.addActionListener(e -> xoaPhieuNhap());
            btnDong.addActionListener(e -> dispose());
            JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            trongSuot(pnlNut);
            pnlNut.add(btnHoanTat);
            pnlNut.add(btnSua);
            pnlNut.add(btnXoa);
            pnlNut.add(btnDong);
            JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
            pnlNoiDung.setBorder(new EmptyBorder(18, 20, 18, 20));
            pnlNoiDung.add(scrCuon, BorderLayout.CENTER);
            pnlNoiDung.add(pnlNut, BorderLayout.SOUTH);
            setContentPane(pnlNoiDung);
            pack();
            setLocationRelativeTo(winCha);
            napDanhSachPhieuNhap();
        }
        private void napDanhSachPhieuNhap() {
            try {
                ds = NhapKhoControl.layDanhSachPhieuNhap();
                model.setRowCount(0);
                for (PhieuNhapKho p : ds) {
                    model.addRow(new Object[] {
                            p.getMaPhieuNhap(),
                            p.getNgayNhap() == null ? "" : p.getNgayNhap().format(NGAY_GIO),
                            p.getTenNhaCungCap(),
                            p.getSoMatHang(),
                            UIStyle.tien(p.getTongTien())
                    });
                }
            } catch (Exception ex) {
                UIStyle.loi(this, ex);
            }
        }
        private PhieuNhapKho layPhieuNhapDangChon() {
            int row = tblPhieuNhap.getSelectedRow();
            if (row < 0) {
                UIStyle.canhBao(this, "Vui lòng chọn một phiếu nháp trong bảng.");
                return null;
            }
            return ds.get(row);
        }
        private void hoanTatPhieuNhap() {
            PhieuNhapKho p = layPhieuNhapDangChon();
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
                if (!NhapKhoControl.hoanTatPhieuNhap(p.getMaPhieuNhap())) throw new IllegalStateException("Không hoàn tất được phiếu nhập.");
                coThayDoi = true;
                napDanhSachPhieuNhap();
            } catch (Exception ex) {
                UIStyle.loi(this, ex);
            }
        }
        private void suaPhieuNhap() {
            PhieuNhapKho p = layPhieuNhapDangChon();
            if (p == null)
                return;
            phieuSua = p;
            dispose();
        }
        private void xoaPhieuNhap() {
            PhieuNhapKho p = layPhieuNhapDangChon();
            if (p == null) {
                return;
            }
            if (!UIStyle.xacNhan(this,
                    "Xóa phiếu nháp " + p.getMaPhieuNhap() + "?")) {
                return;
            }
            try {
                if (!NhapKhoControl.xoaPhieuNhap(p.getMaPhieuNhap())) throw new IllegalStateException("Không xóa được phiếu nhập.");
                coThayDoi = true;
                napDanhSachPhieuNhap();
            } catch (Exception ex) {
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
