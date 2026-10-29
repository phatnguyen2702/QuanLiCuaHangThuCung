package UI;

import UI.TrangChu.PhienLamViec;
import UI.TrangChu.UIStyle;
import dao.KiemKeDAO;
import entity.ChiTietPhieuKiemKe;
import entity.PhieuKiemKe;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Man hinh "Kiem ke kho" - Kiem ke ton kho (nam trong khung TrangChu). */
public class KiemKePanel extends JPanel {

    // dao: Đối tượng truy cập dữ liệu kiểm kê.
    private final KiemKeDAO dao = new KiemKeDAO();
    // moTraCuu: Hành động được gọi khi cần mở màn hình tra cứu.
    private final Runnable moTraCuu;

    // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã SP", "Tên sản phẩm", "Tồn hệ thống",
                    "Tồn thực tế", "Chênh lệch", "Trạng thái" },
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
    // txtTim: Ô nhập/hiển thị văn bản tìm.
    private final JTextField txtTim = UIStyle.o(16);

    // lblTieuDe3: Nhãn hiển thị: Kiểm kê kho.
    private final JLabel lblTieuDe3 = new JLabel("Kiểm kê kho");
    // lblDaKiem: Nhãn hiển thị: 0 / 0.
    private final JLabel lblDaKiem = new JLabel("0 / 0");
    // lblChuaKiem: Nhãn hiển thị: 0.
    private final JLabel lblChuaKiem = new JLabel("0");
    // lblChenhLech: Nhãn hiển thị: 0 mặt hàng.
    private final JLabel lblChenhLech = new JLabel("0 mặt hàng");
    // lblBatDau: Nhãn hiển thị: Thời điểm kiểm kê: —.
    private final JLabel lblBatDau = new JLabel("Thời điểm kiểm kê: —");
    // ngayBatDau: Lưu thời điểm bắt đầu dưới dạng dữ liệu thời gian.
    private LocalDateTime ngayBatDau = LocalDateTime.now();

    // Toan bo san pham cua phieu dang kiem
    // tatCa: Lưu toàn bộ dữ liệu; phần tử có kiểu ChiTietPhieuKiemKe.
    private final List<ChiTietPhieuKiemKe> tatCa = new ArrayList<>();
    // Cac dong dang hien tren tblDuLieu2 (sau khi tim)
    // dangHien: Lưu dữ liệu đang hiển thị; phần tử có kiểu ChiTietPhieuKiemKe.
    private final List<ChiTietPhieuKiemKe> dangHien = new ArrayList<>();

    // maPhieu: Lưu mã phiếu.
    private String maPhieu = "";

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public KiemKePanel() {
        this(() -> { });
    }

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    // Tham số moTraCuu: mở màn hình tra cứu.
    public KiemKePanel(Runnable moTraCuu) {
        this.moTraCuu = moTraCuu;

        setLayout(new BorderLayout(0, 12));
        setBackground(UIStyle.LIGHT_BLUE);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // pnlPhanDau: Khung chứa thành phần giao diện phần đầu.
        JPanel pnlPhanDau = new JPanel();
        pnlPhanDau.setOpaque(false);
        pnlPhanDau.setLayout(new BoxLayout(pnlPhanDau, BoxLayout.Y_AXIS));
        pnlPhanDau.add(taoHangTieuDe());
        // pnlMocThoiGian: Khung chứa thành phần giao diện mốc thời gian.
        JPanel pnlMocThoiGian = new JPanel(new GridLayout(1, 2, 18, 0));
        pnlMocThoiGian.setOpaque(false);
        pnlMocThoiGian.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlMocThoiGian.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        lblBatDau.setFont(UIStyle.font(Font.PLAIN, 13));
        lblBatDau.setBorder(new EmptyBorder(4, 0, 4, 0));
        pnlMocThoiGian.add(lblBatDau);
        pnlPhanDau.add(pnlMocThoiGian);
        pnlPhanDau.add(Box.createVerticalStrut(12));
        pnlPhanDau.add(taoCacTheThongKe());

        add(pnlPhanDau, BorderLayout.NORTH);
        add(taoPhanBang(), BorderLayout.CENTER);

        phieuMoi();
    }

    // =========================================================
    // GIAO DIEN
    // =========================================================

    // Tạo hàng tiêu đề.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoHangTieuDe() {

        lblTieuDe3.setFont(UIStyle.font(Font.BOLD, 26));

        // btnPhieuMoi: Nút thao tác phiếu mới.
        JButton btnPhieuMoi = UIStyle.nutPhu("Phiếu mới");
        // btnLichSu: Nút thao tác lịch sử.
        JButton btnLichSu = UIStyle.nutPhu("Phiếu nháp");
        // btnLuuNhap: Nút thao tác lưu nhập.
        JButton btnLuuNhap = UIStyle.nutPhu("Lưu nháp");
        // btnPdf: Nút thao tác PDF.
        JButton btnPdf = UIStyle.nutPhu("Xuất phiếu kiểm kê");
        // btnTraCuu: Nút thao tác tra cứu.
        JButton btnTraCuu = UIStyle.nutPhu("Tra cứu kiểm kê");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnPdf.addActionListener(e -> UIStyle.xuatPdf(this, "Phiếu kiểm kê kho", tblDuLieu2));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTraCuu.addActionListener(e -> moTraCuu.run());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnPhieuMoi.addActionListener(e -> hoiPhieuMoi());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLichSu.addActionListener(e -> xemLichSu());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLuuNhap.addActionListener(e -> luuNhap());

        // pnlNut2: Khung chứa thành phần giao diện nút.
        JPanel pnlNut2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlNut2.setOpaque(false);
        pnlNut2.add(btnPhieuMoi);
        pnlNut2.add(btnLichSu);
        pnlNut2.add(btnLuuNhap);
        pnlNut2.add(btnPdf);
        pnlNut2.add(btnTraCuu);

        // pnlHang: Khung chứa thành phần giao diện hàng.
        JPanel pnlHang = new JPanel(new BorderLayout());
        pnlHang.setOpaque(false);
        pnlHang.add(lblTieuDe3, BorderLayout.WEST);
        pnlHang.add(pnlNut2, BorderLayout.EAST);
        pnlHang.setAlignmentX(Component.LEFT_ALIGNMENT);

        return pnlHang;
    }

    // Tạo các thẻ thống kê.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoCacTheThongKe() {

        // pnlHang: Khung chứa thành phần giao diện hàng.
        JPanel pnlHang = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlHang.setOpaque(false);
        pnlHang.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlHang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        pnlHang.add(UIStyle.theThongKe("Đã kiểm", lblDaKiem));
        pnlHang.add(UIStyle.theThongKe("Chưa kiểm", lblChuaKiem));
        pnlHang.add(UIStyle.theThongKe("Mặt hàng chênh lệch", lblChenhLech));

        return pnlHang;
    }

    // Tạo phần bảng dữ liệu.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPhanBang() {

        // pnlThe: Khung chứa thành phần giao diện thẻ.
        JPanel pnlThe = UIStyle.the();
        pnlThe.setLayout(new BorderLayout(0, 12));

        // btnTim: Nút thao tác tìm.
        JButton btnTim = UIStyle.nutChinh("Tìm");
        // btnNhap: Nút thao tác nhập.
        JButton btnNhap = UIStyle.nutChinh("Nhập số liệu kiểm");
        // btnChot: Nút thao tác chốt.
        JButton btnChot = UIStyle.nutXanh("Chốt kiểm kê");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTim.addActionListener(e -> veLaiBang());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        txtTim.addActionListener(e -> veLaiBang());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnNhap.addActionListener(e -> nhapSoLieu());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnChot.addActionListener(e -> chot());

        // pnlThanhCongCu: Khung chứa thành phần giao diện thanh công cụ.
        JPanel pnlThanhCongCu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlThanhCongCu.setOpaque(false);
        pnlThanhCongCu.add(UIStyle.nhan("Mã / tên SP:"));
        pnlThanhCongCu.add(txtTim);
        pnlThanhCongCu.add(btnTim);
        pnlThanhCongCu.add(btnNhap);

        UIStyle.taoBang(tblDuLieu2, 2, 3, 4);

        // To mau cot trang thai
        tblDuLieu2.getColumnModel().getColumn(5).setCellRenderer(
                new DefaultTableCellRenderer() {
                    // Thiết lập thành phần dùng để vẽ một ô của bảng.
                    // Tham số tblDuLieu3: bảng đang hiển thị.
                    // Tham số v: giá trị của ô.
                    // Tham số sel: trạng thái được chọn.
                    // Tham số foc: trạng thái có tiêu điểm.
                    // Tham số r: chỉ số dòng.
                    // Tham số c: chỉ số cột.
                    // Kết quả: đối tượng/giá trị kiểu Component.
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable tblDuLieu3, Object v, boolean sel, boolean foc,
                            int r, int c) {
                        super.getTableCellRendererComponent(
                                tblDuLieu3, v, sel, false, r, c);
                        setBorder(new EmptyBorder(0, 10, 0, 10));
                        setHorizontalAlignment(SwingConstants.LEFT);

                        // s: Lưu chuỗi đầu vào.
                        String s = String.valueOf(v);
                        if ("Chênh lệch".equals(s)) {
                            setForeground(UIStyle.RED);
                        } else if ("Khớp".equals(s)) {
                            setForeground(new Color(30, 140, 50));
                        } else {
                            setForeground(Color.DARK_GRAY);
                        }
                        return this;
                    }
                });

        tblDuLieu2.addMouseListener(new MouseAdapter() {
            // Xử lý thao tác nhấn chuột trên thành phần đã gắn listener.
            // Tham số e: thông tin sự kiện.
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblDuLieu2.getSelectedRow() >= 0) {
                    nhapSoLieu();
                }
            }
        });

        pnlThe.add(pnlThanhCongCu, BorderLayout.NORTH);
        pnlThe.add(UIStyle.cuon(tblDuLieu2), BorderLayout.CENTER);
        // pnlHanhDong2: Khung chứa thành phần giao diện hành động.
        JPanel pnlHanhDong2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        pnlHanhDong2.setOpaque(false);
        pnlHanhDong2.add(btnChot);
        pnlThe.add(pnlHanhDong2, BorderLayout.SOUTH);

        return pnlThe;
    }

    // =========================================================
    // DU LIEU
    // =========================================================

    // Nạp lại dữ liệu và cập nhật nội dung màn hình.
    public void napLai() {
        // Dang kiem do thi giu nguyen, chua kiem gi thi lay ton moi nhat
        if (!coDongDaKiem()) {
            phieuMoi();
        }
    }

    // Kiểm tra có dòng nào đã nhập số lượng kiểm đếm hay chưa.
    // Kết quả: true/false theo điều kiện xử lý.
    private boolean coDongDaKiem() {
        for (ChiTietPhieuKiemKe ct : tatCa) {
            if (ct.daKiem()) {
                return true;
            }
        }
        return false;
    }

    // Khởi tạo thông tin phiếu mới và làm mới dữ liệu hiển thị.
    private void phieuMoi() {

        try {
            maPhieu = dao.taoMaMoi();
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblTieuDe3.setText("Kiểm kê kho — " + maPhieu);
            ngayBatDau = LocalDateTime.now();
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblBatDau.setText("Thời điểm kiểm kê: "
                    + ngayBatDau.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            tatCa.clear();

            // Ton he thong = ton kho dang tinh tu du lieu nhap / ban / kiem ke
            for (SanPham sp : dao.laySanPhamKemTon()) {
                ChiTietPhieuKiemKe ct = new ChiTietPhieuKiemKe();
                ct.setMaSanPham(sp.getMaSanPham());
                ct.setTenSanPham(sp.getTenSanPham());
                ct.setSoLuongHeThong(sp.getTonKho());
                ct.setSoLuongThucTe(null);
                tatCa.add(ct);
            }

            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtTim.setText("");
            veLaiBang();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // Hỏi xác nhận phiếu mới.
    private void hoiPhieuMoi() {

        if (coDongDaKiem() && !UIStyle.xacNhan(this,
                "Phiếu đang kiểm chưa chốt sẽ bị bỏ. Tạo phiếu mới?")) {
            return;
        }

        phieuMoi();
    }

    // Đồng bộ các dòng dữ liệu hiện tại lên mô hình bảng.
    private void veLaiBang() {

        // kw: Đọc nội dung chữ từ ô nhập để xử lý từ khóa.
        String kw = txtTim.getText().trim().toLowerCase();

        dangHien.clear();
        model.setRowCount(0);

        for (ChiTietPhieuKiemKe ct : tatCa) {
            if (!kw.isEmpty()
                    && !ct.getMaSanPham().toLowerCase().contains(kw)
                    && !ct.getTenSanPham().toLowerCase().contains(kw)) {
                continue;
            }

            dangHien.add(ct);

            String thucTe = "-";
            String chenh = "-";
            // trangThai: Lưu trạng thái.
            String trangThai = "Chưa kiểm";

            if (ct.daKiem()) {
                int lech = ct.getChenhLech();
                thucTe = String.valueOf(ct.getSoLuongThucTe());
                chenh = lech > 0 ? "+" + lech : String.valueOf(lech);
                trangThai = lech == 0 ? "Khớp" : "Chênh lệch";
            }

            model.addRow(new Object[] {
                    ct.getMaSanPham(),
                    ct.getTenSanPham(),
                    ct.getSoLuongHeThong(),
                    thucTe,
                    chenh,
                    trangThai
            });
        }

        capNhatThongKe();
    }

    // Cập nhật thống kê.
    private void capNhatThongKe() {

        int daKiem = 0;
        int lech = 0;

        for (ChiTietPhieuKiemKe ct : tatCa) {
            if (ct.daKiem()) {
                daKiem++;
                if (ct.getChenhLech() != 0) {
                    lech++;
                }
            }
        }

        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblDaKiem.setText(daKiem + " / " + tatCa.size());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblChuaKiem.setText(String.valueOf(tatCa.size() - daKiem));
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblChenhLech.setText(lech + " mặt hàng");
    }

    // =========================================================
    // NHAP SO LIEU KIEM
    // =========================================================

    // Nhập số liệu.
    private void nhapSoLieu() {

        // row: Lưu chỉ số dòng.
        int row = tblDuLieu2.getSelectedRow();

        if (row < 0) {
            UIStyle.canhBao(this, "Vui lòng chọn sản phẩm cần nhập số liệu kiểm.");
            return;
        }

        ChiTietPhieuKiemKe ct = dangHien.get(row);

        int giaTriBanDau = ct.daKiem() ? ct.getSoLuongThucTe()
                : ct.getSoLuongHeThong();

        // spnSpn: Ô chọn giá trị bằng nút tăng/giảm ô tăng giảm.
        JSpinner spnSpn = new JSpinner(
                new SpinnerNumberModel(Math.max(giaTriBanDau, 0), 0, 1_000_000, 1));
        spnSpn.setFont(UIStyle.font(Font.PLAIN, 14));

        // pnlKhung2: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung2 = new JPanel(new GridLayout(0, 1, 0, 8));
        pnlKhung2.add(new JLabel(ct.getMaSanPham() + " - " + ct.getTenSanPham()));
        pnlKhung2.add(new JLabel("Tồn hệ thống: " + ct.getSoLuongHeThong()));
        pnlKhung2.add(new JLabel("Số lượng thực tế đếm được:"));
        pnlKhung2.add(spnSpn);

        int kq = JOptionPane.showConfirmDialog(this, pnlKhung2,
                "Nhập số liệu kiểm", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (kq != JOptionPane.OK_OPTION) {
            return;
        }

        ct.setSoLuongThucTe((Integer) spnSpn.getValue());
        veLaiBang();

        // Chuyen sang dong ke tiep de nhap lien tuc
        int tiep = Math.min(row + 1, model.getRowCount() - 1);
        if (tiep >= 0) {
            tblDuLieu2.setRowSelectionInterval(tiep, tiep);
        }
    }

    // =========================================================
    // CHOT KIEM KE
    // =========================================================

    // Chốt dữ liệu của màn hình.
    private void chot() {

        try {
            // tonMoi: Bảng ánh xạ khóa đến giá trị dùng cho tồn mới.
            java.util.Map<String, Integer> tonMoi = new java.util.HashMap<>();
            for (SanPham sp : dao.laySanPhamKemTon())
                tonMoi.put(sp.getMaSanPham(), sp.getTonKho());
            for (ChiTietPhieuKiemKe ct : tatCa) {
                if (ct.daKiem() && tonMoi.containsKey(ct.getMaSanPham()))
                    ct.setSoLuongHeThong(tonMoi.get(ct.getMaSanPham()));
            }
            veLaiBang();
        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
            return;
        }

        // daKiem: Lưu đã kiểm; phần tử có kiểu ChiTietPhieuKiemKe.
        List<ChiTietPhieuKiemKe> daKiem = new ArrayList<>();

        for (ChiTietPhieuKiemKe ct : tatCa) {
            if (ct.daKiem()) {
                ct.setTrangThai(ct.getChenhLech() == 0);
                daKiem.add(ct);
            }
        }

        if (daKiem.isEmpty()) {
            UIStyle.canhBao(this, "Chưa có sản phẩm nào được nhập số liệu kiểm.");
            return;
        }

        int chua = tatCa.size() - daKiem.size();

        String hoi = "Chốt phiếu " + maPhieu + " với " + daKiem.size()
                + " mặt hàng đã kiểm?"
                + (chua > 0 ? "\n" + chua
                        + " mặt hàng chưa kiểm sẽ không nằm trong phiếu." : "")
                + "\nTồn kho sẽ được điều chỉnh theo số lượng thực tế.";

        if (!UIStyle.xacNhan(this, hoi)) {
            return;
        }

        // gioChot: Lưu giờ chốt dưới dạng dữ liệu thời gian.
        LocalDateTime gioChot = LocalDateTime.now();
        PhieuKiemKe phieu = new PhieuKiemKe(
                maPhieu,
                gioChot,
                true,
                PhienLamViec.maNV);
        phieu.setNgayBatDau(ngayBatDau);
        phieu.setNgayChot(gioChot);

        try {
            dao.luu(phieu, daKiem);

            UIStyle.thongBao(this, "Đã chốt kiểm kê " + maPhieu + ".");

            phieuMoi();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // Xem lịch sử.
    private void xemLichSu() {
        try {
            // ds: Danh sách chứa các phần tử kiểu PhieuKiemKe.
            ArrayList<PhieuKiemKe> ds = dao.dsPhieuNhap();
            // colsPhieu: Mảng lưu các cột phiếu.
            String[] colsPhieu = { "Mã phiếu", "Trạng thái", "Thời điểm kiểm kê", "Mã NV lập" };
            // modelPhieu: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
            DefaultTableModel modelPhieu = new DefaultTableModel(colsPhieu, 0) {
                // Không cho phép sửa trực tiếp các ô trong bảng.
                // Tham số row: chỉ số dòng.
                // Tham số col: chỉ số cột.
                // Kết quả: false, bảng chỉ dùng để xem.
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            for (PhieuKiemKe p : ds)
                modelPhieu.addRow(new Object[] {
                        p.getMaPhieuKiemKe(), p.isTrangThai() ? "Đã chốt" : "Nháp",
                        dinhDangGio(p.getNgayBatDau()), p.getMaNhanVien()
                });
            // tblBangPhieu: Bảng hiển thị dữ liệu bảng phiếu.
            JTable tblBangPhieu = new JTable(modelPhieu);
            UIStyle.taoBang(tblBangPhieu, 0);
            tblBangPhieu.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            // rongCot: Mảng lưu rỗng cột.
            int[] rongCot = { 95, 80, 170, 105 };
            for (int i = 0; i < rongCot.length; i++)
                tblBangPhieu.getColumnModel().getColumn(i).setPreferredWidth(rongCot[i]);

            // modelChiTiet: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
            DefaultTableModel modelChiTiet = new DefaultTableModel(
                    new String[] { "Mã SP", "Tên sản phẩm", "Tồn hệ thống", "Thực tế", "Chênh lệch", "Kết quả" }, 0) {
                // Không cho phép sửa trực tiếp các ô trong bảng.
                // Tham số row: chỉ số dòng.
                // Tham số col: chỉ số cột.
                // Kết quả: false, bảng chỉ dùng để xem.
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            // tblBangChiTiet: Bảng hiển thị dữ liệu bảng chi tiết.
            JTable tblBangChiTiet = new JTable(modelChiTiet);
            UIStyle.taoBang(tblBangChiTiet, 2, 3, 4);
            // lblThongTin2: Nhãn hiển thị: Chọn một phiếu để xem chi tiết..
            JLabel lblThongTin2 = new JLabel("Chọn một phiếu để xem chi tiết.");
            // btnTiepTuc: Nút thao tác tiếp tục.
            JButton btnTiepTuc = UIStyle.nutChinh("Tiếp tục phiếu nháp");
            btnTiepTuc.setEnabled(false);

            // pnlChiTiet: Khung chứa thành phần giao diện chi tiết.
            JPanel pnlChiTiet = new JPanel(new BorderLayout(0, 8));
            pnlChiTiet.setBorder(new EmptyBorder(8, 8, 8, 8));
            pnlChiTiet.add(lblThongTin2, BorderLayout.NORTH);
            pnlChiTiet.add(UIStyle.cuon(tblBangChiTiet), BorderLayout.CENTER);

            JSplitPane noiDung = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    UIStyle.cuon(tblBangPhieu), pnlChiTiet);
            noiDung.setResizeWeight(0.57);
            // pnlControls: Khung chứa thành phần giao diện các thành phần giao diện.
            JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            pnlControls.add(btnTiepTuc);
            // pnlBody: Khung chứa thành phần giao diện nội dung.
            JPanel pnlBody = new JPanel(new BorderLayout(0, 8));
            pnlBody.setBorder(new EmptyBorder(12, 12, 12, 12));
            pnlBody.add(new JLabel("Phiếu nháp kiểm kê — chọn phiếu để xem hoặc tiếp tục kiểm kê."),
                    BorderLayout.NORTH);
            pnlBody.add(noiDung, BorderLayout.CENTER);
            pnlBody.add(pnlControls, BorderLayout.SOUTH);
            // winDlg: Hộp thoại hộp thoại.
            JDialog winDlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Phiếu nháp kiểm kê",
                    Dialog.ModalityType.APPLICATION_MODAL);
            winDlg.setContentPane(pnlBody);
            winDlg.setSize(1180, 620);
            winDlg.setLocationRelativeTo(this);
            tblBangPhieu.getSelectionModel().addListSelectionListener(e -> {
                if (e.getValueIsAdjusting())
                    return;
                // row: Lưu chỉ số dòng.
                int row = tblBangPhieu.getSelectedRow();
                if (row < 0)
                    return;
                PhieuKiemKe p = ds.get(tblBangPhieu.convertRowIndexToModel(row));
                btnTiepTuc.setEnabled(!p.isTrangThai());
                try {
                    // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                    lblThongTin2.setText("Phiếu " + p.getMaPhieuKiemKe() + " | " + (p.isTrangThai() ? "Đã chốt" : "Nháp")
                            + " | Thời điểm kiểm kê: " + dinhDangGio(p.getNgayBatDau())
                            + " | Mã NV lập: " + p.getMaNhanVien());
                    modelChiTiet.setRowCount(0);
                    for (ChiTietPhieuKiemKe ct : dao.layChiTiet(p.getMaPhieuKiemKe())) {
                        Integer tt = ct.getSoLuongThucTe();
                        modelChiTiet
                                .addRow(new Object[] { ct.getMaSanPham(), ct.getTenSanPham(), ct.getSoLuongHeThong(),
                                        tt == null ? "Chưa kiểm" : tt,
                                        tt == null ? "—" : (ct.getChenhLech() > 0 ? "+" : "") + ct.getChenhLech(),
                                        tt == null ? "Chưa kiểm" : ct.getChenhLech() == 0 ? "Khớp" : "Chênh lệch" });
                    }
                } catch (SQLException ex) {
                    UIStyle.loi(this, ex);
                }
            });
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnTiepTuc.addActionListener(e -> {
                // row: Lưu chỉ số dòng.
                int row = tblBangPhieu.getSelectedRow();
                if (row < 0)
                    return;
                PhieuKiemKe p = ds.get(tblBangPhieu.convertRowIndexToModel(row));
                if (p.isTrangThai())
                    return;
                if (coDongDaKiem() && !UIStyle.xacNhan(this, "Tiếp tục phiếu nháp sẽ thay phiếu đang kiểm. Tiếp tục?"))
                    return;
                try {
                    maPhieu = p.getMaPhieuKiemKe();
                    // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                    lblTieuDe3.setText("Kiểm kê kho — " + maPhieu);
                    // bd: Lưu bắt đầu dưới dạng dữ liệu thời gian.
                    LocalDateTime bd = p.getNgayBatDau() == null ? LocalDateTime.now() : p.getNgayBatDau();
                    ngayBatDau = bd;
                    // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                    lblBatDau.setText("Thời điểm kiểm kê: "
                            + bd.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                    tatCa.clear();
                    tatCa.addAll(dao.layChiTiet(maPhieu));
                    veLaiBang();
                    winDlg.dispose();
                } catch (SQLException ex) {
                    UIStyle.loi(this, ex);
                }
            });
            winDlg.setVisible(true);
        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // Định dạng giá trị giờ để hiển thị.
    // Tham số thoiDiem: thời điểm.
    // Kết quả: chuỗi kết quả.
    private String dinhDangGio(LocalDateTime thoiDiem) {
        return thoiDiem == null ? "—"
                : thoiDiem.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // Lưu nhập.
    private void luuNhap() {
        try {
            PhieuKiemKe p = new PhieuKiemKe(maPhieu, ngayBatDau, false, PhienLamViec.maNV);
            p.setNgayBatDau(ngayBatDau);
            dao.luuNhap(p, tatCa);
            UIStyle.thongBao(this, "Đã lưu nháp phiếu " + maPhieu + ".");
        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }
}
