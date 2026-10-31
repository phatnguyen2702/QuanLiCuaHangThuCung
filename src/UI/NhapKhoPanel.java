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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Man hinh "Nhap kho" - Lap phieu nhap hang (nam trong khung TrangChu).
 * Cac pnlForm2 phu (nha cung cap, chi tiet nhap, phieu nhap nhap) la lop long o
 * cuoi file.
 */
public class NhapKhoPanel extends JFrame implements ActionListener, MouseListener {
    @Override public void actionPerformed(ActionEvent e) { }
    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mousePressed(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }

    // dao: Đối tượng truy cập dữ liệu nhập kho.
    private final NhapKhoDAO dao = new NhapKhoDAO();
    // moTraCuu: Hành động được gọi khi cần mở màn hình tra cứu.
    private final Runnable moTraCuu;

    // DINH_DANG_NGAY: Bộ định dạng ngày/giờ theo mẫu dd/MM/yyyy HH:mm để chuyển giữa dữ liệu thời gian và chuỗi.
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ---- thong tin phieu ----
    // txtMaPhieu: Ô nhập/hiển thị văn bản mã phiếu.
    private final JTextField txtMaPhieu = UIStyle.oChiDoc("");
    // txtNgayNhap: Ô nhập/hiển thị văn bản ngày giờ nhập kho.
    private final JTextField txtNgayNhap = UIStyle.oChiDoc("");
    // txtNhanVien: Ô nhập/hiển thị văn bản nhân viên.
    private final JTextField txtNhanVien = UIStyle.oChiDoc(PhienLamViec.hoTen);
    // cmbNhaCungCap: Hộp lựa chọn nhà cung cấp.
    private final JComboBox<NhaCungCap> cmbNhaCungCap = new JComboBox<>();

    // ---- chi tiet ----
    // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã SP", "Tên mặt hàng", "Số lượng",
                    "Đơn giá nhập", "Hạn sử dụng", "Thành tiền" },
            0) {
        // Không cho phép sửa trực tiếp các ô trong bảng.
        // Tham số row: chỉ số dòng.
        // Tham số col: chỉ số cột.
        // Kết quả: false, bảng chỉ dùng để xem.
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    // tblDuLieu2: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
    private final JTable tblDuLieu2 = new JTable(model);
    // lblTong: Nhãn hiển thị: Tổng thanh toán: 0 ₫.
    private final JLabel lblTong = new JLabel("Tổng thanh toán: 0 ₫");

    // dong: Lưu dòng dữ liệu; phần tử có kiểu ChiTietPhieuNhap.
    private final List<ChiTietPhieuNhap> dong = new ArrayList<>();

    // ngayNhap: Lưu ngày giờ nhập kho dưới dạng dữ liệu thời gian.
    private LocalDateTime ngayNhap = LocalDateTime.now();
    // nccHienTai: Đối tượng nhà cung cấp hiện tại có kiểu NhaCungCap.
    private NhaCungCap nccHienTai = null;
    // dangNapCombo: Cờ cho biết đang nạp danh sách lựa chọn.
    private boolean dangNapCombo = false;
    // daNapDongMau: Cờ cho biết đã nạp các dòng minh họa.
    private boolean daNapDongMau = false;

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public NhapKhoPanel() {
        this(() -> {
        });
    }

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    // Tham số moTraCuu: mở màn hình tra cứu.
    public NhapKhoPanel(Runnable moTraCuu) {
        this.moTraCuu = moTraCuu;

        setLayout(new BorderLayout(0, 12));
        getContentPane().setBackground(UIStyle.LIGHT_BLUE);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(20, 25, 20, 25));

        // lblTieuDe2: Nhãn hiển thị: Lập phiếu nhập hàng.
        JLabel lblTieuDe2 = new JLabel("Lập phiếu nhập hàng");
        lblTieuDe2.setFont(UIStyle.font(Font.BOLD, 26));

        // pnlPhanDau: Khung chứa thành phần giao diện phần đầu.
        JPanel pnlPhanDau = new JPanel(new BorderLayout(0, 12));
        pnlPhanDau.setOpaque(false);
        pnlPhanDau.add(lblTieuDe2, BorderLayout.NORTH);
        pnlPhanDau.add(taoThongTinPhieu(), BorderLayout.CENTER);

        add(pnlPhanDau, BorderLayout.NORTH);
        add(taoPhanChiTiet(), BorderLayout.CENTER);
        add(taoPhanCuoi(), BorderLayout.SOUTH);

        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbNhaCungCap.addActionListener(e -> doiNhaCungCap());

        napLai();
    }

    // =========================================================
    // GIAO DIEN
    // =========================================================

    // Tạo thông tin phiếu.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoThongTinPhieu() {

        // pnlThe: Khung chứa thành phần giao diện thẻ.
        JPanel pnlThe = UIStyle.the();
        pnlThe.setLayout(new GridBagLayout());

        cmbNhaCungCap.setFont(UIStyle.font(Font.PLAIN, 13));

        // btnThemNcc: Nút thao tác thêm NCC.
        JButton btnThemNcc = UIStyle.nutChinh("+");
        btnThemNcc.setToolTipText("Thêm nhà cung cấp mới");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnThemNcc.addActionListener(e -> themNhaCungCap());

        // pnlHangNcc: Khung chứa thành phần giao diện hàng NCC.
        JPanel pnlHangNcc = new JPanel(new BorderLayout(6, 0));
        pnlHangNcc.setOpaque(false);
        pnlHangNcc.add(cmbNhaCungCap, BorderLayout.CENTER);
        pnlHangNcc.add(btnThemNcc, BorderLayout.EAST);

        themHang(pnlThe, 0, "Mã phiếu", txtMaPhieu, "Ngày nhập", txtNgayNhap);
        themHang(pnlThe, 1, "Nhà cung cấp", pnlHangNcc, "Nhân viên lập", txtNhanVien);

        return pnlThe;
    }

    // Thêm hàng.
    // Tham số pnlThe: thẻ.
    // Tham số dong: dòng dữ liệu.
    // Tham số nhan1: nhân 1.
    // Tham số c1: c 1.
    // Tham số nhan2: nhân 2.
    // Tham số c2: c 2.
    private void themHang(JPanel pnlThe, int dong, String nhan1, JComponent c1,
            String nhan2, JComponent c2) {

        // g: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng GridBagLayout.
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = dong;
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0;
        g.weightx = 0;
        g.insets = new Insets(8, 5, 8, 25);
        pnlThe.add(UIStyle.nhan(nhan1), g);

        g.gridx = 1;
        g.weightx = 1;
        g.insets = new Insets(8, 0, 8, 40);
        pnlThe.add(c1, g);

        g.gridx = 2;
        g.weightx = 0;
        g.insets = new Insets(8, 0, 8, 25);
        pnlThe.add(UIStyle.nhan(nhan2), g);

        g.gridx = 3;
        g.weightx = 1;
        g.insets = new Insets(8, 0, 8, 5);
        pnlThe.add(c2, g);
    }

    // Tạo phần chi tiết.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPhanChiTiet() {

        // pnlThe: Khung chứa thành phần giao diện thẻ.
        JPanel pnlThe = UIStyle.the();
        pnlThe.setLayout(new BorderLayout(0, 12));

        // btnThem: Nút thao tác thêm.
        JButton btnThem = UIStyle.nutChinh("+ Thêm mặt hàng");
        // btnXoaDong: Nút thao tác xóa dòng.
        JButton btnXoaDong = UIStyle.nutXoa("Xóa dòng");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnThem.addActionListener(e -> themMatHang());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnXoaDong.addActionListener(e -> xoaDong());

        // btnNhap: Nút thao tác nhập.
        JButton btnNhap = UIStyle.nutPhu("Phiếu nháp đã lưu");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnNhap.addActionListener(e -> moPhieuNhap());
        // btnTraCuu: Nút thao tác tra cứu.
        JButton btnTraCuu = UIStyle.nutPhu("Tra cứu nhập kho");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTraCuu.addActionListener(e -> moTraCuu.run());
        // pnlNhomTrai: Khung chứa thành phần giao diện nhóm trái.
        JPanel pnlNhomTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlNhomTrai.setOpaque(false);
        pnlNhomTrai.add(btnThem);
        pnlNhomTrai.add(btnXoaDong);
        pnlNhomTrai.add(btnNhap);
        pnlNhomTrai.add(btnTraCuu);

        UIStyle.taoBang(tblDuLieu2, 2, 3, 4);

        // Khong xuat truc tiep tblDuLieu2 chua luu: chung tu phai lay tu SQL theo ma phieu.
        // lblHuongDanPdf: Nhãn hiển thị: Xuất PDF: Tra cứu nhập kho → Xem chi tiết.
        JLabel lblHuongDanPdf = new JLabel("Xuất PDF: Tra cứu nhập kho → Xem chi tiết");
        lblHuongDanPdf.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHuongDanPdf.setForeground(new Color(85, 101, 120));
        // pnlThanhCongCu: Khung chứa thành phần giao diện thanh công cụ.
        JPanel pnlThanhCongCu = new JPanel(new BorderLayout());
        pnlThanhCongCu.setOpaque(false);
        pnlThanhCongCu.add(pnlNhomTrai, BorderLayout.WEST);
        pnlThanhCongCu.add(lblHuongDanPdf, BorderLayout.EAST);

        pnlThe.add(pnlThanhCongCu, BorderLayout.NORTH);
        pnlThe.add(UIStyle.cuon(tblDuLieu2), BorderLayout.CENTER);

        return pnlThe;
    }

    // Tạo phần cuối.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPhanCuoi() {

        lblTong.setFont(UIStyle.font(Font.BOLD, 22));

        // btnLuuNhap: Nút thao tác lưu nhập.
        JButton btnLuuNhap = UIStyle.nutPhu("Lưu nháp");
        // btnHoanTat: Nút thao tác có hoàn tất phiếu hay chỉ lưu nháp.
        JButton btnHoanTat = UIStyle.nutXanh("Hoàn tất nhập kho");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLuuNhap.addActionListener(e -> luu(false));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnHoanTat.addActionListener(e -> luu(true));

        // pnlNut2: Khung chứa thành phần giao diện nút.
        JPanel pnlNut2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlNut2.setOpaque(false);
        pnlNut2.add(btnLuuNhap);
        pnlNut2.add(btnHoanTat);

        // pnlKhung2: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung2 = new JPanel(new BorderLayout());
        pnlKhung2.setOpaque(false);
        pnlKhung2.add(lblTong, BorderLayout.WEST);
        pnlKhung2.add(pnlNut2, BorderLayout.EAST);

        return pnlKhung2;
    }

    // =========================================================
    // NAP DU LIEU
    // =========================================================

    // Nạp lại dữ liệu và cập nhật nội dung màn hình.
    public void napLai() {

        napNhaCungCap(null);

        // Chi doi ma phieu khi phieu dang trong
        if (dong.isEmpty()) {
            phieuMoi();
            if (!daNapDongMau) {
                napDongMau();
                daNapDongMau = cmbNhaCungCap.getSelectedItem() != null;
            }
        }
    }

    /** Điền sẵn vài mặt hàng thật vào phiếu nháp để màn hình có dữ liệu minh họa. */
    private void napDongMau() {
        NhaCungCap ncc = (NhaCungCap) cmbNhaCungCap.getSelectedItem();
        if (ncc == null) return;

        try {
            // sanPham: Lưu sản phẩm; phần tử có kiểu SanPham.
            ArrayList<SanPham> sanPham = dao.timSanPhamTheoNhaCungCap(ncc.getMaNhaCungCap());
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
            veLaiBang();
        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // Nạp nhà cung cấp.
    // Tham số maCanChon: mã cần chọn.
    private void napNhaCungCap(String maCanChon) {

        try {
            String maDangChon = maCanChon != null ? maCanChon
                    : (nccHienTai == null ? null : nccHienTai.getMaNhaCungCap());

            dangNapCombo = true;
            cmbNhaCungCap.removeAllItems();

            NhaCungCap chon = null;

            for (NhaCungCap n : dao.layDanhSachNhaCungCap()) {
                cmbNhaCungCap.addItem(n);
                if (n.getMaNhaCungCap().equals(maDangChon)) {
                    chon = n;
                }
            }

            if (chon != null) {
                cmbNhaCungCap.setSelectedItem(chon);
            }

            nccHienTai = (NhaCungCap) cmbNhaCungCap.getSelectedItem();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        } finally {
            dangNapCombo = false;
        }
    }

    // Khởi tạo thông tin phiếu mới và làm mới dữ liệu hiển thị.
    private void phieuMoi() {

        try {
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtMaPhieu.setText(dao.taoMaMoi());
        } catch (SQLException ex) {
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtMaPhieu.setText("");
            UIStyle.loi(this, ex);
        }

        ngayNhap = LocalDateTime.now();
        // Định dạng dữ liệu thành chuỗi rồi hiển thị trong control bằng setText().
        txtNgayNhap.setText(ngayNhap.format(DINH_DANG_NGAY));
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtNhanVien.setText(PhienLamViec.hoTen);

        dong.clear();
        veLaiBang();
    }

    // Đồng bộ các dòng dữ liệu hiện tại lên mô hình bảng.
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

        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblTong.setText("Tổng thanh toán: " + UIStyle.tien(tong));
    }

    // =========================================================
    // NHA CUNG CAP
    // =========================================================

    // Doi nha cung cap: san pham trong phieu phai cung 1 nha cung cap
    // Đổi nhà cung cấp.
    private void doiNhaCungCap() {

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
            veLaiBang();
        }

        nccHienTai = moi;
    }

    // Thêm nhà cung cấp.
    private void themNhaCungCap() {

        try {
            NhaCungCapDialog winDlg = new NhaCungCapDialog(
                    SwingUtilities.getWindowAncestor(this),
                    dao.taoMaNhaCungCap());
            winDlg.setVisible(true);

            NhaCungCap moi = winDlg.getKetQua();

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

    // Thêm mặt hàng.
    private void themMatHang() {

        NhaCungCap ncc = (NhaCungCap) cmbNhaCungCap.getSelectedItem();

        if (ncc == null) {
            UIStyle.canhBao(this, "Vui lòng chọn nhà cung cấp trước.");
            return;
        }

        try {
            // dsSanPham: Lưu danh sách sản phẩm; phần tử có kiểu SanPham.
            ArrayList<SanPham> dsSanPham = dao.timSanPhamTheoNhaCungCap(
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

    // Xóa dòng dữ liệu.
    private void xoaDong() {

        // row: Lưu chỉ số dòng.
        int row = tblDuLieu2.getSelectedRow();

        if (row < 0) {
            UIStyle.canhBao(this, "Vui lòng chọn dòng cần xóa.");
            return;
        }

        dong.remove(row);
        veLaiBang();
    }

    // Xem / hoan tat / xoa cac phieu da "Luu nhap"
    // Mở phiếu nhập.
    private void moPhieuNhap() {

        PhieuNhapNhapDialog winDlg = new PhieuNhapNhapDialog(
                SwingUtilities.getWindowAncestor(this));
        winDlg.setVisible(true);

        PhieuNhapKho canSua = winDlg.getPhieuSua();
        if (canSua != null) {
            if (!dong.isEmpty() && !UIStyle.xacNhan(this, "Nạp phiếu nháp sẽ thay nội dung đang nhập. Tiếp tục?"))
                return;
            try {
                napNhaCungCap(canSua.getMaNhaCungCap());
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtMaPhieu.setText(canSua.getMaPhieuNhap());
                ngayNhap = canSua.getNgayNhap() == null ? LocalDateTime.now() : canSua.getNgayNhap();
                // Định dạng dữ liệu thành chuỗi rồi hiển thị trong control bằng setText().
                txtNgayNhap.setText(ngayNhap.format(DINH_DANG_NGAY));
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtNhanVien.setText(canSua.getMaNhanVien());
                dong.clear();
                dong.addAll(dao.layChiTiet(canSua.getMaPhieuNhap()));
                veLaiBang();
                return;
            } catch (SQLException ex) {
                UIStyle.loi(this, ex);
            }
        }

        // Ma phieu moi co pnlThe bi doi (phieu nhap bi xoa / hoan tat)
        if (winDlg.isCoThayDoi() && dong.isEmpty()) {
            phieuMoi();
        }
    }

    // =========================================================
    // LUU PHIEU
    // =========================================================

    // hoanTat = true: trangThai 1 (tinh vao ton kho); false: luu nhap (trangThai 0)
    // Kiểm tra dữ liệu nhập và lưu thông tin của biểu mẫu.
    // Tham số hoanTat: có hoàn tất phiếu hay chỉ lưu nháp.
    private void luu(boolean hoanTat) {

        NhaCungCap ncc = (NhaCungCap) cmbNhaCungCap.getSelectedItem();

        if (ncc == null) {
            UIStyle.canhBao(this, "Vui lòng chọn nhà cung cấp.");
            return;
        }

        // maPhieu: Đọc nội dung chữ từ ô nhập để xử lý mã phiếu.
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

        // dao: Đối tượng truy cập dữ liệu nhập kho.
        private final NhapKhoDAO dao = new NhapKhoDAO();

        // txtMa: Ô nhập/hiển thị văn bản mã.
        private final JTextField txtMa = UIStyle.o(22);
        // txtTen: Ô nhập/hiển thị văn bản tên.
        private final JTextField txtTen = UIStyle.o(22);
        // txtSdt: Ô nhập/hiển thị văn bản số điện thoại.
        private final JTextField txtSdt = UIStyle.o(22);
        // txtEmail: Ô nhập/hiển thị văn bản email.
        private final JTextField txtEmail = UIStyle.o(22);
        // txtDiaChi: Ô nhập/hiển thị văn bản địa chỉ.
        private final JTextField txtDiaChi = UIStyle.o(22);

        // ketQua: Đối tượng kết quả có kiểu NhaCungCap.
        private NhaCungCap ketQua = null;

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        // Tham số winCha: thành phần winCha.
        // Tham số maMoi: mã mới.
        public NhaCungCapDialog(Window winCha, String maMoi) {

            super(winCha, "Thêm nhà cung cấp", ModalityType.APPLICATION_MODAL);

            // pnlForm2: Khung chứa thành phần giao diện biểu mẫu.
            JPanel pnlForm2 = new JPanel(new GridBagLayout());
            pnlForm2.setBorder(new EmptyBorder(20, 25, 10, 25));

            UIStyle.hang(pnlForm2, 0, "Mã NCC", txtMa, false);
            UIStyle.hang(pnlForm2, 1, "Tên NCC", txtTen, false);
            UIStyle.hang(pnlForm2, 2, "Số điện thoại", txtSdt, false);
            UIStyle.hang(pnlForm2, 3, "Email", txtEmail, false);
            UIStyle.hang(pnlForm2, 4, "Địa chỉ", txtDiaChi, false);

            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtMa.setText(maMoi);

            // btnHuy: Nút thao tác hủy.
            JButton btnHuy = UIStyle.nutPhu("Hủy");
            // btnLuu: Nút thao tác lưu.
            JButton btnLuu = UIStyle.nutXanh("Lưu");
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnHuy.addActionListener(e -> dispose());
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnLuu.addActionListener(e -> luu());

            // pnlNut2: Khung chứa thành phần giao diện nút.
            JPanel pnlNut2 = UIStyle.hangNut(btnHuy, btnLuu);
            pnlNut2.setBorder(new EmptyBorder(5, 25, 20, 25));

            setLayout(new BorderLayout());
            add(pnlForm2, BorderLayout.CENTER);
            add(pnlNut2, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(btnLuu);

            pack();
            setResizable(false);
            setLocationRelativeTo(winCha);
        }

        // Chuyển chuỗi trống thành null để lưu dữ liệu không bắt buộc.
        // Tham số s: chuỗi đầu vào.
        // Kết quả: chuỗi kết quả.
        private String rongThanhNull(String s) {
            s = s.trim();
            return s.isEmpty() ? null : s;
        }

        // Kiểm tra dữ liệu nhập và lưu thông tin của biểu mẫu.
        private void luu() {

            // ma: Đọc nội dung chữ từ ô nhập để xử lý mã.
            String ma = txtMa.getText().trim();
            // ten: Đọc nội dung chữ từ ô nhập để xử lý tên.
            String ten = txtTen.getText().trim();
            // sdt: Đọc nội dung chữ từ ô nhập để xử lý số điện thoại.
            String sdt = txtSdt.getText().trim();
            // email: Đọc nội dung chữ từ ô nhập để xử lý email.
            String email = txtEmail.getText().trim();
            // diaChi: Đọc nội dung chữ từ ô nhập để xử lý địa chỉ.
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

        // cmbSanPham: Hộp lựa chọn sản phẩm.
        private final JComboBox<SanPham> cmbSanPham = new JComboBox<>();
        // spnSoLuong: Ô chọn giá trị bằng nút tăng/giảm số lượng.
        private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
        // txtDonGia: Ô nhập/hiển thị văn bản đơn giá.
        private final JTextField txtDonGia = UIStyle.o(22);
        // txtHanSuDung: Ô nhập/hiển thị văn bản hạn sử dụng.
        private final JTextField txtHanSuDung = UIStyle.o(12);

        // ketQua: Đối tượng kết quả có kiểu ChiTietPhieuNhap.
        private ChiTietPhieuNhap ketQua = null;

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        // Tham số winCha: thành phần winCha.
        // Tham số dsSanPham: danh sách sản phẩm.
        public ChiTietNhapDialog(Window winCha, List<SanPham> dsSanPham) {

            super(winCha, "Thêm mặt hàng vào phiếu nhập",
                    ModalityType.APPLICATION_MODAL);

            for (SanPham sp : dsSanPham) {
                cmbSanPham.addItem(sp);
            }
            cmbSanPham.setFont(UIStyle.font(Font.PLAIN, 13));
            spnSoLuong.setFont(UIStyle.font(Font.PLAIN, 13));

            // Chon san pham -> tu dien gia nhap dang co trong tblDuLieu2 SanPham
            cmbSanPham.addActionListener(e -> {
                SanPham sp = (SanPham) cmbSanPham.getSelectedItem();
                if (sp != null) {
                    // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                    txtDonGia.setText(String.valueOf((long) sp.getGiaNhap()));
                }
            });

            // pnlForm2: Khung chứa thành phần giao diện biểu mẫu.
            JPanel pnlForm2 = new JPanel(new GridBagLayout());
            pnlForm2.setBorder(new EmptyBorder(20, 25, 10, 25));

            UIStyle.hang(pnlForm2, 0, "Sản phẩm", cmbSanPham, false);
            UIStyle.hang(pnlForm2, 1, "Số lượng nhập", spnSoLuong, false);
            UIStyle.hang(pnlForm2, 2, "Đơn giá nhập", txtDonGia, false);
            UIStyle.hang(pnlForm2, 3, "Hạn sử dụng (dd/MM/yyyy, tùy chọn)", txtHanSuDung, false);

            if (cmbSanPham.getItemCount() > 0) {
                cmbSanPham.setSelectedIndex(0);
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtDonGia.setText(String.valueOf(
                        (long) dsSanPham.get(0).getGiaNhap()));
            }

            // btnHuy: Nút thao tác hủy.
            JButton btnHuy = UIStyle.nutPhu("Hủy");
            // btnThem: Nút thao tác thêm.
            JButton btnThem = UIStyle.nutXanh("Thêm vào phiếu");
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnHuy.addActionListener(e -> dispose());
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnThem.addActionListener(e -> them());

            // pnlNut2: Khung chứa thành phần giao diện nút.
            JPanel pnlNut2 = UIStyle.hangNut(btnHuy, btnThem);
            pnlNut2.setBorder(new EmptyBorder(5, 25, 20, 25));

            setLayout(new BorderLayout());
            add(pnlForm2, BorderLayout.CENTER);
            add(pnlNut2, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(btnThem);

            pack();
            setMinimumSize(new Dimension(420, getHeight()));
            setResizable(false);
            setLocationRelativeTo(winCha);
        }

        // Xử lý yêu cầu thêm dữ liệu từ giao diện.
        private void them() {

            SanPham sp = (SanPham) cmbSanPham.getSelectedItem();

            if (sp == null) {
                UIStyle.canhBao(this, "Vui lòng chọn sản phẩm.");
                return;
            }

            // donGia: Lưu đơn giá.
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
            // han: Lưu hạn dưới dạng dữ liệu thời gian.
            java.time.LocalDate han = null;
            try {
                if (!txtHanSuDung.getText().trim().isEmpty())
                    // Phân tích chuỗi ngày thành LocalDate theo bộ định dạng được chỉ định.
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

        // NGAY: Bộ định dạng ngày/giờ theo mẫu dd/MM/yyyy HH:mm để chuyển giữa dữ liệu thời gian và chuỗi.
        private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // dao: Đối tượng truy cập dữ liệu nhập kho.
        private final NhapKhoDAO dao = new NhapKhoDAO();

        // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
        private final DefaultTableModel model = new DefaultTableModel(
                new String[] { "Mã phiếu", "Ngày lập", "Nhà cung cấp",
                        "Số mặt hàng", "Tổng tiền" },
                0) {
            // Không cho phép sửa trực tiếp các ô trong bảng.
            // Tham số row: chỉ số dòng.
            // Tham số col: chỉ số cột.
            // Kết quả: false, bảng chỉ dùng để xem.
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        // tblDuLieu2: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
        private final JTable tblDuLieu2 = new JTable(model);
        // ds: Danh sách chứa các phần tử kiểu PhieuNhapKho.
        private ArrayList<PhieuNhapKho> ds = new ArrayList<>();

        // coThayDoi: Cờ cho biết dữ liệu đã thay đổi.
        private boolean coThayDoi = false;
        // phieuSua: Đối tượng phiếu đang sửa có kiểu PhieuNhapKho.
        private PhieuNhapKho phieuSua;

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        // Tham số winCha: thành phần winCha.
        public PhieuNhapNhapDialog(Window winCha) {

            super(winCha, "Phiếu nhập đang lưu nháp", ModalityType.APPLICATION_MODAL);

            UIStyle.taoBang(tblDuLieu2, 3, 4);

            // scrCuon: Khung cuộn cuộn.
            JScrollPane scrCuon = UIStyle.cuon(tblDuLieu2);
            scrCuon.setPreferredSize(new Dimension(700, 280));

            // btnHoanTat: Nút thao tác có hoàn tất phiếu hay chỉ lưu nháp.
            JButton btnHoanTat = UIStyle.nutXanh("Hoàn tất nhập kho");
            // btnSua: Nút thao tác sửa.
            JButton btnSua = UIStyle.nutChinh("Sửa / tiếp tục");
            // btnXoa: Nút thao tác xóa.
            JButton btnXoa = UIStyle.nutXoa("Xóa phiếu nháp");
            // btnDong: Nút thao tác dòng dữ liệu.
            JButton btnDong = UIStyle.nutPhu("Đóng");

            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnHoanTat.addActionListener(e -> hoanTat());
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnSua.addActionListener(e -> sua());
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnXoa.addActionListener(e -> xoa());
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnDong.addActionListener(e -> dispose());

            // pnlNut2: Khung chứa thành phần giao diện nút.
            JPanel pnlNut2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnlNut2.setOpaque(false);
            pnlNut2.add(btnHoanTat);
            pnlNut2.add(btnSua);
            pnlNut2.add(btnXoa);
            pnlNut2.add(btnDong);

            // pnlNoiDung2: Khung chứa thành phần giao diện nội dung.
            JPanel pnlNoiDung2 = new JPanel(new BorderLayout(0, 12));
            pnlNoiDung2.setBorder(new EmptyBorder(18, 20, 18, 20));
            pnlNoiDung2.add(scrCuon, BorderLayout.CENTER);
            pnlNoiDung2.add(pnlNut2, BorderLayout.SOUTH);

            setContentPane(pnlNoiDung2);
            pack();
            setLocationRelativeTo(winCha);

            nap();
        }

        // Nạp danh sách dữ liệu để hiển thị.
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

        // Lấy đối tượng tương ứng với dòng đang được chọn.
        // Kết quả: đối tượng/giá trị kiểu PhieuNhapKho.
        private PhieuNhapKho dangChon() {

            // row: Lưu chỉ số dòng.
            int row = tblDuLieu2.getSelectedRow();

            if (row < 0) {
                UIStyle.canhBao(this, "Vui lòng chọn một phiếu nháp trong bảng.");
                return null;
            }

            return ds.get(row);
        }

        // Hoàn tất dữ liệu của màn hình.
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

        // Xử lý yêu cầu sửa bản ghi đang chọn.
        private void sua() {
            PhieuNhapKho p = dangChon();
            if (p == null)
                return;
            phieuSua = p;
            dispose();
        }

        // Xử lý yêu cầu xóa bản ghi đang chọn.
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

        // Kiểm tra dữ liệu đã thay đổi.
        // Kết quả: true/false theo điều kiện xử lý.
        public boolean isCoThayDoi() {
            return coThayDoi;
        }

        // Trả về phiếu đang sửa của đối tượng.
        public PhieuNhapKho getPhieuSua() {
            return phieuSua;
        }
    }
}
