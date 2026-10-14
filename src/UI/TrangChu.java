package UI;

import ConnectDB.connectDB;
import dao.HoaDonDAO;
import dao.LichHenDAO;
import entity.LichHen;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Cua so chinh Pet Shop Pro - menu theo HUONG CHUC NANG.
 * 5 nhom: He thong, Danh muc, Nghiep vu, Tim kiem, Thong ke.
 * Moi chuc nang mo JPanel trong CardLayout, khong mo JFrame moi.
 */
public class TrangChu extends JFrame {

    private final Color LIGHT_BLUE = new Color(229, 241, 252);
    private final Color MENU_BLUE = new Color(207, 222, 235);
    private final String maNV;
    private final String hoTen;
    private final String vaiTro;

    private static final String[] TEN_TAB = {
            "Danh mục", "Sản phẩm", "Nhập kho", "Kiểm kê kho"
    };
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm:ss | dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO_HEN = DateTimeFormatter.ofPattern("HH:mm");

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pnlNoiDung = new JPanel(cardLayout);
    private JLabel lblNgayTrangChu;
    private JButton btnTrangChu;
    private JButton btnHeThong;
    private JButton btnDanhMuc;
    private JButton btnNghiepVu;
    private JButton btnTimKiem;
    private JButton btnThongKe;
    private final ArrayList<JButton> dsBtnMucChinh = new ArrayList<>();
    private JButton btnNhomDaChon;

    // Man hinh kho; nap lai du lieu khi quay lai.
    private final JPanel[] arrPnlManHinhKho = new JPanel[TEN_TAB.length];
    private final Runnable[] napLai = new Runnable[TEN_TAB.length];
    // Man hinh khac khoi tao mot lan, tai su dung trong CardLayout.
    private final Map<String, Component> mapManHinhKhac = new HashMap<>();
    private TraCuuHoaDon pnlTraCuuHoaDon;
    private final javax.swing.Timer tmrDongHo;

    // Thanh phan dong tren trang chu.
    private JPanel pnlLichSpaNoiDung;
    private JLabel lblPhuDeLichSpa;
    private JLabel lblDoanhThuHomNay;
    private JLabel lblLuotMuaHomNay;
    private JLabel lblLichSpaHomNay;
    private JLabel lblPhieuNhapHomNay;

    public TrangChu(String maNV, String hoTen, String vaiTro) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;

        PhienLamViec.dat(maNV, hoTen, vaiTro);
        UI.PhienLamViec.dat(maNV, hoTen, vaiTro);

        setTitle("Pet Shop Pro - Trang chủ");
        setSize(1200, 720);
        setMinimumSize(new Dimension(980, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(taoPnlHeader(), BorderLayout.NORTH);

        pnlNoiDung.setBackground(LIGHT_BLUE);
        pnlNoiDung.add(taoPnlTrangChu(), "trangchu");
        add(pnlNoiDung, BorderLayout.CENTER);

        tmrDongHo = new javax.swing.Timer(1000, e -> capNhatGio());
        tmrDongHo.start();
        capNhatGio();
        hienTrangChu();
    }

    @Override
    public void dispose() {
        tmrDongHo.stop();
        super.dispose();
    }

    private void hienTrangChu() {
        btnNhomDaChon = null;
        cardLayout.show(pnlNoiDung, "trangchu");
        datMucDangChon(btnTrangChu);
        setTitle("Pet Shop Pro - Trang chủ");
        napLichSpaSapToi();
    }

    /** API cu cho DieuHuong.moKho(cha, tab) va nhung man hinh khac. */
    public void chuyenKho(int tab) {
        if (tab < 0 || tab >= TEN_TAB.length)
            tab = 0;
        try {
            boolean laLanDau = arrPnlManHinhKho[tab] == null;
            if (laLanDau) {
                taoManHinh(tab);
                pnlNoiDung.add(arrPnlManHinhKho[tab], "tab" + tab);
            }
            cardLayout.show(pnlNoiDung, "tab" + tab);
            datMucDangChon(btnNhomDaChon != null ? btnNhomDaChon : btnDanhMuc);
            setTitle("Pet Shop Pro - " + TEN_TAB[tab]);
            if (!laLanDau && napLai[tab] != null)
                napLai[tab].run();
            pnlNoiDung.revalidate();
            pnlNoiDung.repaint();
        } catch (RuntimeException ex) {
            baoLoiMoManHinh(TEN_TAB[tab], ex);
        }
    }

    private void taoManHinh(int tab) {
        switch (tab) {
            case 0 -> {
                DanhMucPanel p = new DanhMucPanel();
                arrPnlManHinhKho[tab] = p;
                napLai[tab] = p::napLai;
            }
            case 1 -> {
                SanPhamPanel p = new SanPhamPanel();
                arrPnlManHinhKho[tab] = p;
                napLai[tab] = p::napLai;
            }
            case 2 -> {
                NhapKhoPanel p = new NhapKhoPanel();
                arrPnlManHinhKho[tab] = p;
                napLai[tab] = p::napLai;
            }
            default -> {
                KiemKePanel p = new KiemKePanel();
                arrPnlManHinhKho[tab] = p;
                napLai[tab] = p::napLai;
            }
        }
    }

    private void hienManHinh(String khoa, String tieuDe, JButton nhomMacDinh,
            Supplier<? extends Component> tao) {
        try {
            Component c = mapManHinhKhac.get(khoa);
            if (c == null) {
                c = tao.get();
                mapManHinhKhac.put(khoa, c);
                pnlNoiDung.add(c, khoa);
            }
            cardLayout.show(pnlNoiDung, khoa);
            datMucDangChon(btnNhomDaChon != null ? btnNhomDaChon : nhomMacDinh);
            setTitle("Pet Shop Pro - " + tieuDe);
            pnlNoiDung.revalidate();
            pnlNoiDung.repaint();
        } catch (RuntimeException ex) {
            baoLoiMoManHinh(tieuDe, ex);
        }
    }

    private void baoLoiMoManHinh(String ten, RuntimeException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Không thể mở màn hình " + ten + ".\n"
                        + "Vui lòng kiểm tra kết nối CSDL hoặc Console.\n"
                        + ex.getMessage(),
                "Pet Shop Pro - Lỗi mở màn hình", JOptionPane.ERROR_MESSAGE);
    }

    private void moLichHen() {
        hienManHinh("lichhen", "Lịch hẹn Spa", btnNghiepVu, QuanLyLichHen::new);
    }

    private void moThuCung() {
        hienManHinh("thucung", "Quản lý thú cưng", btnDanhMuc, QuanLyThuCung::new);
    }

    private void moKhachHang() {
        hienManHinh("khachhang", "Quản lý khách hàng", btnDanhMuc,
                () -> new QuanLyKhachHang(maNV));
    }

    private void moVoucher() {
        hienManHinh("voucher", "Quản lý voucher", btnNghiepVu, QuanLyVoucher::new);
    }

    private void moPhieuDichVu() {
        hienManHinh("phieudv", "Quản lý phiếu dịch vụ", btnNghiepVu,
                QuanLyPhieuDichVu::new);
    }

    private void moTraCuuHoaDon() {
        hienManHinh("hoadon", "Tra cứu hóa đơn", btnTimKiem,
                () -> pnlTraCuuHoaDon = new TraCuuHoaDon());
        if (pnlTraCuuHoaDon != null) {
            try {
                pnlTraCuuHoaDon.hienDanhSach();
            } catch (RuntimeException ex) {
                baoLoiMoManHinh("Tra cứu hóa đơn", ex);
            }
        }
    }

    private void moThanhToan() {
        hienManHinh("thanhtoan", "Thanh toán", btnNghiepVu,
                () -> new QuanLyThanhToan(maNV));
    }

    private void moQuanLyTaiKhoan() {
        hienManHinh("taikhoan", "Quản lý tài khoản", btnHeThong,
                QuanLyTaiKhoan::new);
    }

    private void moQuanLyPhanCa() {
        hienManHinh("phanca", "Phân ca nhân viên", btnNghiepVu,
                QuanLyPhanCa::new);
    }

    private void moQuanLyCaLam() {
        hienManHinh("calam", "Quản lý ca làm", btnHeThong,
                QuanLyCaLam::new);
    }

    private void chuyenTuNhom(JButton nhom, Runnable hanhDong) {
        btnNhomDaChon = nhom;
        hanhDong.run();
    }

    private void datMucDangChon(JButton dangChon) {
        for (JButton btnTam : dsBtnMucChinh) {
            boolean active = btnTam == dangChon;
            btnTam.setBackground(active ? new Color(48, 91, 138) : UIStyle.NAVY);
            btnTam.setForeground(Color.WHITE);
            btnTam.setFont(UIStyle.font(active ? Font.BOLD : Font.PLAIN, 12));
        }
        if (btnTrangChu != null) {
            boolean active = btnTrangChu == dangChon;
            btnTrangChu.setBackground(active ? new Color(48, 91, 138) : UIStyle.NAVY);
            btnTrangChu.setForeground(Color.WHITE);
            btnTrangChu.setFont(UIStyle.font(active ? Font.BOLD : Font.PLAIN, 12));
        }
    }

    private void capNhatGio() {
        if (lblNgayTrangChu != null) {
            lblNgayTrangChu.setText(LocalDateTime.now().format(DINH_DANG_GIO));
        }
    }

    // ==================== MENU THEO HUONG CHUC NANG ====================
    private JPanel taoPnlHeader() {
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(UIStyle.NAVY);
        header.setBorder(new EmptyBorder(7, 12, 7, 12));

        JPanel pnlThuongHieu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlThuongHieu.setOpaque(false);
        JLabel lblLogo = new JLabel("PET SHOP PRO", taiIcon("/img/logo.png", 26, 26), SwingConstants.LEFT);
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(UIStyle.font(Font.BOLD, 16));
        lblLogo.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblLogo.setIconTextGap(8);
        pnlThuongHieu.add(lblLogo);
        pnlThuongHieu.setPreferredSize(new Dimension(160, 38));
        header.add(pnlThuongHieu, BorderLayout.WEST);

        JPanel pnlMenu = new JPanel(new GridLayout(1, 6, 2, 0));
        pnlMenu.setOpaque(false);

        btnTrangChu = taoBtnNhom("Trang chủ", "/img/trangchulogo.png");
        btnTrangChu.addActionListener(e -> hienTrangChu());

        btnHeThong = taoBtnNhom("HỆ THỐNG", "/img/caidatlogo.png");
        btnDanhMuc = taoBtnNhom("DANH MỤC", "/img/danhmuclogo.png");
        btnNghiepVu = taoBtnNhom("NGHIỆP VỤ", "/img/nghiepvulogo.png");
        btnTimKiem = taoBtnNhom("TÌM KIẾM", "/img/timkiemlogo.png");
        btnThongKe = taoBtnNhom("THỐNG KÊ", "/img/thongkelogo.png");

        pnlMenu.add(btnTrangChu);
        pnlMenu.add(btnHeThong);
        pnlMenu.add(btnDanhMuc);
        pnlMenu.add(btnNghiepVu);
        pnlMenu.add(btnTimKiem);
        pnlMenu.add(btnThongKe);

        dsBtnMucChinh.add(btnHeThong);
        dsBtnMucChinh.add(btnDanhMuc);
        dsBtnMucChinh.add(btnNghiepVu);
        dsBtnMucChinh.add(btnTimKiem);
        dsBtnMucChinh.add(btnThongKe);

        btnHeThong.addActionListener(e -> hienPopup(btnHeThong, taoMenuHeThong()));
        btnDanhMuc.addActionListener(e -> hienPopup(btnDanhMuc, taoMenuDanhMuc()));
        btnNghiepVu.addActionListener(e -> hienPopup(btnNghiepVu, taoMenuNghiepVu()));
        btnTimKiem.addActionListener(e -> hienPopup(btnTimKiem, taoMenuTimKiem()));
        btnThongKe.addActionListener(e -> hienPopup(btnThongKe, taoMenuThongKe()));

        header.add(pnlMenu, BorderLayout.CENTER);

        JButton btnNhanVien = new JButton(hoTen == null ? "Tài khoản" : hoTen);
        btnNhanVien.setFont(UIStyle.font(Font.BOLD, 12));
        btnNhanVien.setForeground(Color.WHITE);
        btnNhanVien.setBackground(UIStyle.NAVY);
        btnNhanVien.setFocusPainted(false);
        btnNhanVien.setOpaque(true);
        btnNhanVien.setBorder(new EmptyBorder(8, 6, 8, 6));
        btnNhanVien.setPreferredSize(new Dimension(170, 38));
        btnNhanVien.setToolTipText("Thông tin nhân viên / Đăng xuất");
        btnNhanVien.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNhanVien.addActionListener(e -> hienThongTinNhanVien(btnNhanVien));
        header.add(btnNhanVien, BorderLayout.EAST);

        return header;
    }

    private JButton taoBtnNhom(String title, String iconPath) {
        JButton btnTam = new JButton(title, taiIcon(iconPath, 18, 18));
        btnTam.setFont(UIStyle.font(Font.PLAIN, 12));
        btnTam.setBackground(UIStyle.NAVY);
        btnTam.setForeground(Color.WHITE);
        btnTam.setOpaque(true);
        btnTam.setFocusPainted(false);
        btnTam.setBorder(new EmptyBorder(8, 3, 8, 3));
        btnTam.setToolTipText(title.equals("Trang chủ") ? "Về trang chủ" : "Mở menu " + title);
        btnTam.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTam.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnTam.setIconTextGap(8);
        return btnTam;
    }

    private ImageIcon taiIcon(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url == null)
            return null;
        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private void hienPopup(JButton nguon, JPopupMenu popMenu) {
        popMenu.show(nguon, 0, nguon.getHeight());
    }

    private void themMuc(JPopupMenu popMenu, String ten, JButton nhom, Runnable hanhDong) {
        JMenuItem mniChucNang = new JMenuItem(ten);
        mniChucNang.setFont(UIStyle.font(Font.PLAIN, 13));
        mniChucNang.setBorder(new EmptyBorder(9, 14, 9, 14));
        mniChucNang.addActionListener(e -> chuyenTuNhom(nhom, hanhDong));
        popMenu.add(mniChucNang);
    }

    private JPopupMenu taoMenuHeThong() {
        JPopupMenu p = new JPopupMenu();
        themMuc(p, "Quản lý tài khoản", btnHeThong, this::moQuanLyTaiKhoan);
        themMuc(p, "Quản lý ca làm", btnHeThong, this::moQuanLyCaLam);
        return p;
    }

    private JPopupMenu taoMenuDanhMuc() {
        JPopupMenu p = new JPopupMenu();
        themMuc(p, "Danh mục sản phẩm", btnDanhMuc, () -> chuyenKho(0));
        themMuc(p, "Sản phẩm", btnDanhMuc, () -> chuyenKho(1));
        themMuc(p, "Khách hàng", btnDanhMuc, this::moKhachHang);
        themMuc(p, "Thú cưng", btnDanhMuc, this::moThuCung);
        return p;
    }

    private JPopupMenu taoMenuNghiepVu() {
        JPopupMenu p = new JPopupMenu();
        themMuc(p, "Nhập kho", btnNghiepVu, () -> chuyenKho(2));
        themMuc(p, "Kiểm kê kho", btnNghiepVu, () -> chuyenKho(3));
        p.addSeparator();
        themMuc(p, "Lịch hẹn Spa", btnNghiepVu, this::moLichHen);
        themMuc(p, "Phiếu dịch vụ", btnNghiepVu, this::moPhieuDichVu);
        p.addSeparator();
        themMuc(p, "Thanh toán hóa đơn", btnNghiepVu, this::moThanhToan);
        themMuc(p, "Voucher / Mã giảm giá", btnNghiepVu, this::moVoucher);
        p.addSeparator();
        themMuc(p, "Phân ca nhân viên", btnNghiepVu, this::moQuanLyPhanCa);
        return p;
    }

    private JPopupMenu taoMenuTimKiem() {
        JPopupMenu p = new JPopupMenu();
        themMuc(p, "Tra cứu hóa đơn", btnTimKiem, this::moTraCuuHoaDon);
        themMuc(p, "Tra cứu khách hàng", btnTimKiem, this::moKhachHang);
        themMuc(p, "Tra cứu sản phẩm", btnTimKiem, () -> chuyenKho(1));
        themMuc(p, "Tra cứu thú cưng", btnTimKiem, this::moThuCung);
        themMuc(p, "Tra cứu lịch hẹn", btnTimKiem, this::moLichHen);
        return p;
    }

    private JPopupMenu taoMenuThongKe() {
        JPopupMenu p = new JPopupMenu();
        return p;
    }

    private void hienThongTinNhanVien(JButton nguon) {
        JPopupMenu popMenu = new JPopupMenu();
        JPanel pnlThongTin = new JPanel(new GridLayout(0, 2, 15, 8));
        pnlThongTin.setBorder(new EmptyBorder(12, 12, 12, 12));
        themThongTin(pnlThongTin, "Mã NV", maNV);
        themThongTin(pnlThongTin, "Họ tên", hoTen);
        themThongTin(pnlThongTin, "Vai trò", vaiTro);
        themThongTin(pnlThongTin, "Trạng thái", "Đang hoạt động");
        popMenu.add(pnlThongTin);
        popMenu.addSeparator();
        JMenuItem dangXuat = new JMenuItem("Đăng xuất");
        dangXuat.addActionListener(e -> dangXuat());
        popMenu.add(dangXuat);
        popMenu.show(nguon,
                -popMenu.getPreferredSize().width + nguon.getWidth(), nguon.getHeight());
    }

    private void themThongTin(JPanel pnlPanel, String lblNhan, String lblGiaTri) {
        JLabel lblThongTin = new JLabel(lblNhan);
        lblThongTin.setFont(UIStyle.font(Font.BOLD, 12));
        pnlPanel.add(lblThongTin);
        pnlPanel.add(new JLabel(lblGiaTri != null ? lblGiaTri : ""));
    }

    private void dangXuat() {
        dispose();
        new DangNhap().setVisible(true);
    }

    private void lamMoiTrangChu() {
        capNhatGio();
        napLichSpaSapToi();
        napTongQuanHomNay();
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
    }

    // ==================== NOI DUNG TRANG CHU ====================
    private JPanel taoPnlTrangChu() {
        JPanel pnlChinh = new JPanel(new BorderLayout());
        pnlChinh.setBackground(LIGHT_BLUE);
        pnlChinh.add(taoPnlThanhTieuDe(), BorderLayout.NORTH);

        JPanel pnlThan = new JPanel(new BorderLayout(0, 14));
        pnlThan.setBackground(LIGHT_BLUE);
        pnlThan.setBorder(new EmptyBorder(16, 28, 16, 28));

        // Hai cot ben tren va duoi can nhau theo cung mot luoi.
        JPanel pnlTren = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlTren.setOpaque(false);
        pnlTren.add(taoPnlGioiThieu());
        pnlTren.add(taoPnlTruyCapNhanh());

        JPanel pnlDuoi = taoPnlTienIchDuoi();
        pnlDuoi.setPreferredSize(new Dimension(0, 200));

        pnlThan.add(pnlTren, BorderLayout.CENTER);
        pnlThan.add(pnlDuoi, BorderLayout.SOUTH);
        pnlChinh.add(pnlThan, BorderLayout.CENTER);
        return pnlChinh;
    }

    private JPanel taoPnlThanhTieuDe() {
        JPanel pnlPanel = new JPanel(new BorderLayout());
        pnlPanel.setBackground(UIStyle.BAR);
        pnlPanel.setBorder(new EmptyBorder(10, 25, 10, 25));

        JLabel lblNgayHienTai = new JLabel(LocalDateTime.now().format(DINH_DANG_GIO));
        lblNgayTrangChu = lblNgayHienTai;
        lblNgayHienTai.setFont(UIStyle.font(Font.BOLD, 12));

        JLabel lblTieuDeTrangChu = new JLabel("TRANG CHỦ", SwingConstants.CENTER);
        lblTieuDeTrangChu.setFont(UIStyle.font(Font.BOLD, 25));

        JPanel pnlThaoTac = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlThaoTac.setOpaque(false);

        JButton btnLamMoi = UIStyle.nutPhu(" Làm mới");
        btnLamMoi.setPreferredSize(new Dimension(135, 38));
        btnLamMoi.addActionListener(e -> lamMoiTrangChu());

        JButton btnHoTro = UIStyle.nutPhu("? Hỗ trợ");
        btnHoTro.setPreferredSize(new Dimension(125, 38));
        btnHoTro.addActionListener(e -> moModalHoTro());

        pnlThaoTac.add(btnLamMoi);
        pnlThaoTac.add(btnHoTro);

        pnlPanel.add(lblNgayHienTai, BorderLayout.WEST);
        pnlPanel.add(lblTieuDeTrangChu, BorderLayout.CENTER);
        pnlPanel.add(pnlThaoTac, BorderLayout.EAST);
        return pnlPanel;
    }

    private JPanel taoPnlGioiThieu() {
        JPanel pnlPanel = new JPanel(new BorderLayout());
        pnlPanel.setBackground(Color.WHITE);
        pnlPanel.setBorder(new LineBorder(UIStyle.LINE));

        java.net.URL url = getClass().getResource("/img/giothieutrangchu.png");
        if (url == null) {
            pnlPanel.add(new JLabel("Không tìm thấy ảnh giới thiệu", SwingConstants.CENTER));
            return pnlPanel;
        }
        Image anh = new ImageIcon(url).getImage();
        JPanel pnlAnhCoGian = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int w = getWidth(), h = getHeight();
                int iw = anh.getWidth(this), ih = anh.getHeight(this);
                if (iw <= 0 || ih <= 0 || w <= 0 || h <= 0)
                    return;
                // Anh luon phu kin khung, khong con khoang trang lon.
                // Giu ti le goc, cat bot rat it o canh neu can thiet.
                double tile = Math.max((double) w / iw, (double) h / ih);
                int newW = (int) Math.ceil(iw * tile);
                int newH = (int) Math.ceil(ih * tile);
                int x = (w - newW) / 2;
                int y = 0; // Can tren bang khung Truy cap nhanh.
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(anh, x, y, newW, newH, this);
                g2.dispose();
            }
        };
        pnlAnhCoGian.setBackground(Color.WHITE);
        pnlPanel.add(pnlAnhCoGian, BorderLayout.CENTER);
        return pnlPanel;
    }

    private JPanel taoPnlTruyCapNhanh() {
        JPanel pnlPanel = new JPanel();
        pnlPanel.setBackground(Color.WHITE);
        pnlPanel.setBorder(new CompoundBorder(new LineBorder(UIStyle.LINE), new EmptyBorder(15, 15, 15, 15)));
        pnlPanel.setLayout(new BoxLayout(pnlPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("TRUY CẬP NHANH");
        title.setFont(UIStyle.font(Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlPanel.add(title);
        pnlPanel.add(Box.createVerticalStrut(15));

        themBtnTruyCapNhanh(pnlPanel, "Thanh toán", "F4",
                () -> chuyenTuNhom(btnNghiepVu, this::moThanhToan));
        themBtnTruyCapNhanh(pnlPanel, "Quản lý lịch hẹn Spa", "F5",
                () -> chuyenTuNhom(btnNghiepVu, this::moLichHen));
        themBtnTruyCapNhanh(pnlPanel, "Quản lý sản phẩm", "F6",
                () -> chuyenTuNhom(btnDanhMuc, () -> chuyenKho(1)));
        themBtnTruyCapNhanh(pnlPanel, "Quản lý khách hàng", "F7",
                () -> chuyenTuNhom(btnDanhMuc, this::moKhachHang));
        themBtnTruyCapNhanh(pnlPanel, "Tra cứu hóa đơn", "F8",
                () -> chuyenTuNhom(btnTimKiem, this::moTraCuuHoaDon));

        return pnlPanel;
    }

    private JPanel taoPnlTienIchDuoi() {
        JPanel pnlPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlPanel.setOpaque(false);
        pnlPanel.add(taoPnlLichSpaSapToi());
        pnlPanel.add(taoPnlTongQuan());
        return pnlPanel;
    }

    private JPanel taoPnlTongQuan() {
        JPanel pnlTongQuan = new JPanel(new BorderLayout(0, 14));
        pnlTongQuan.setBackground(Color.WHITE);
        pnlTongQuan.setBorder(new CompoundBorder(new LineBorder(UIStyle.LINE), new EmptyBorder(14, 16, 14, 16)));

        JLabel lblTieuDeTongQuan = new JLabel("Tổng quan hôm nay", taiIcon("/img/thongkelogo.png", 18, 18),
                SwingConstants.LEFT);
        lblTieuDeTongQuan.setFont(UIStyle.font(Font.BOLD, 17));
        lblTieuDeTongQuan.setForeground(new Color(40, 55, 77));
        lblTieuDeTongQuan.setIconTextGap(8);
        pnlTongQuan.add(lblTieuDeTongQuan, BorderLayout.NORTH);

        JPanel pnlLuoi = new JPanel(new GridLayout(2, 2, 12, 12));
        pnlLuoi.setOpaque(false);

        lblDoanhThuHomNay = taoGiaTriTongQuan("—");
        lblLuotMuaHomNay = taoGiaTriTongQuan("—");
        lblLichSpaHomNay = taoGiaTriTongQuan("—");
        lblPhieuNhapHomNay = taoGiaTriTongQuan("—");

        pnlLuoi.add(taoTheTongQuan("Doanh thu", lblDoanhThuHomNay));
        pnlLuoi.add(taoTheTongQuan("Lượt mua", lblLuotMuaHomNay));
        pnlLuoi.add(taoTheTongQuan("Lịch Spa", lblLichSpaHomNay));
        pnlLuoi.add(taoTheTongQuan("Phiếu nhập", lblPhieuNhapHomNay));

        pnlTongQuan.add(pnlLuoi, BorderLayout.CENTER);
        napTongQuanHomNay();
        return pnlTongQuan;
    }

    private JPanel taoTheTongQuan(String tieuDe, JLabel giaTri) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));

        panel.setBackground(new Color(245, 248, 252));
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(UIStyle.font(Font.PLAIN, 12));

        giaTri.setFont(UIStyle.font(Font.BOLD, 20));
        giaTri.setForeground(new Color(18, 48, 82));

        panel.add(lblTieuDe, BorderLayout.NORTH);
        panel.add(giaTri, BorderLayout.CENTER);

        return panel;
    }

    private JLabel taoGiaTriTongQuan(String text) {
        JLabel lblTam = new JLabel(text);
        lblTam.setFont(UIStyle.font(Font.BOLD, 23));
        lblTam.setForeground(new Color(38, 45, 57));
        return lblTam;
    }

    private void napTongQuanHomNay() {
        if (lblDoanhThuHomNay == null)
            return;
        try {
            HoaDonDAO.ThongKe tk = new HoaDonDAO().layThongKe();
            int lichSpaHomNay = demLichSpaHomNay();
            int phieuNhapHomNay = demPhieuNhapHomNay();

            lblDoanhThuHomNay.setText(formatVnd(tk.getDoanhThuHomNay()));
            lblLuotMuaHomNay.setText(String.valueOf(tk.getHoaDonHomNay()));
            lblLichSpaHomNay.setText(String.valueOf(lichSpaHomNay));
            lblPhieuNhapHomNay.setText(String.valueOf(phieuNhapHomNay));
        } catch (Exception ex) {
            lblDoanhThuHomNay.setText("—");
            lblLuotMuaHomNay.setText("—");
            lblLichSpaHomNay.setText("—");
            lblPhieuNhapHomNay.setText("—");
        }
    }

    private int demLichSpaHomNay() throws SQLException {
        String sql = "SELECT COUNT(*) FROM LichHen WHERE CAST(ngayGioHen AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int demPhieuNhapHomNay() throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhieuNhapKho WHERE CAST(ngayNhap AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private String formatVnd(BigDecimal soTien) {
        return UIStyle.tien(soTien == null ? 0 : soTien.doubleValue());
    }

    private JPanel taoPnlLichSpaSapToi() {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(UIStyle.LINE), new EmptyBorder(10, 14, 10, 14)));

        JPanel pnlTren = new JPanel(new BorderLayout());
        pnlTren.setOpaque(false);

        JPanel pnlBocTieuDe = new JPanel();
        pnlBocTieuDe.setOpaque(false);
        pnlBocTieuDe.setLayout(new BoxLayout(pnlBocTieuDe, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Lịch Spa sắp tới", taiIcon("/img/lichlogo.png", 18, 18), SwingConstants.LEFT);
        title.setFont(UIStyle.font(Font.BOLD, 17));
        title.setForeground(new Color(40, 55, 77));
        title.setIconTextGap(8);

        lblPhuDeLichSpa = new JLabel("Đang tải dữ liệu lịch hẹn...");
        lblPhuDeLichSpa.setFont(UIStyle.font(Font.PLAIN, 12));
        lblPhuDeLichSpa.setForeground(Color.GRAY);

        pnlBocTieuDe.add(title);
        pnlBocTieuDe.add(Box.createVerticalStrut(2));
        pnlBocTieuDe.add(lblPhuDeLichSpa);
        pnlTren.add(pnlBocTieuDe, BorderLayout.WEST);

        card.add(pnlTren, BorderLayout.NORTH);

        pnlLichSpaNoiDung = new JPanel();
        pnlLichSpaNoiDung.setOpaque(false);
        pnlLichSpaNoiDung.setLayout(new BoxLayout(pnlLichSpaNoiDung, BoxLayout.Y_AXIS));
        card.add(pnlLichSpaNoiDung, BorderLayout.CENTER);

        napLichSpaSapToi();
        return card;
    }

    private void napLichSpaSapToi() {
        if (pnlLichSpaNoiDung == null || lblPhuDeLichSpa == null)
            return;

        pnlLichSpaNoiDung.removeAll();
        LocalDateTime now = LocalDateTime.now();

        try {
            List<LichHen> tatCa = new LichHenDAO().getAll();
            List<LichHen> sapToi = tatCa.stream()
                    .filter(lh -> lh.getNgayGioHen() != null && !lh.getNgayGioHen().isBefore(now))
                    .sorted(Comparator.comparing(LichHen::getNgayGioHen))
                    .toList();

            List<LichHen> trong90Phut = sapToi.stream()
                    .filter(lh -> Duration.between(now, lh.getNgayGioHen()).toMinutes() <= 90)
                    .limit(3)
                    .toList();

            long soLichHomNay = sapToi.stream()
                    .filter(lh -> lh.getNgayGioHen().toLocalDate().equals(now.toLocalDate()))
                    .count();

            if (!trong90Phut.isEmpty()) {
                lblPhuDeLichSpa.setText("Trong 90 phút tiếp theo");
                for (LichHen lh : trong90Phut) {
                    pnlLichSpaNoiDung.add(taoDongLichSpa(lh));
                    pnlLichSpaNoiDung.add(Box.createVerticalStrut(3));
                }
                pnlLichSpaNoiDung.add(Box.createVerticalGlue());
                pnlLichSpaNoiDung.add(taoChipTongKet(
                        "Sắp tới: " + trong90Phut.size() + " lịch • Hôm nay: " + soLichHomNay + " lịch"));
            } else if (!sapToi.isEmpty()) {
                lblPhuDeLichSpa.setText("Hiển thị những lịch hẹn tiếp theo");
                int dem = 0;
                for (LichHen lh : sapToi) {
                    pnlLichSpaNoiDung.add(taoDongLichSpa(lh));
                    pnlLichSpaNoiDung.add(Box.createVerticalStrut(3));
                    dem++;
                    if (dem == 3)
                        break;
                }
                pnlLichSpaNoiDung.add(Box.createVerticalGlue());
                pnlLichSpaNoiDung.add(taoChipTongKet("Hôm nay: " + soLichHomNay + " lịch Spa"));
            } else {
                lblPhuDeLichSpa.setText("Chưa có lịch hẹn Spa sắp tới");
                JLabel lblRong = new JLabel("Không có lịch hẹn nào trong dữ liệu hiện tại.");
                lblRong.setFont(UIStyle.font(Font.PLAIN, 13));
                lblRong.setForeground(Color.GRAY);
                pnlLichSpaNoiDung.add(lblRong);
            }
        } catch (Exception ex) {
            lblPhuDeLichSpa.setText("Không đọc được dữ liệu lịch hẹn");
            JLabel lblLoi = new JLabel("Vui lòng kiểm tra kết nối CSDL hoặc dữ liệu Lịch hẹn Spa.");
            lblLoi.setFont(UIStyle.font(Font.PLAIN, 13));
            lblLoi.setForeground(new Color(170, 60, 60));
            pnlLichSpaNoiDung.add(lblLoi);
        }

        pnlLichSpaNoiDung.revalidate();
        pnlLichSpaNoiDung.repaint();
    }

    private JComponent taoDongLichSpa(LichHen lh) {
        String gio = lh.getNgayGioHen() == null ? "--:--" : lh.getNgayGioHen().format(DINH_DANG_GIO_HEN);
        String thuCung = khongRong(lh.getTenThuCung(), "Thú cưng");
        String ghiChu = khongRong(lh.getGhiChu(), "Chăm sóc Spa");
        // Khong de HTML tu xuong dong lam panel bi keo rong/le cot.
        String strNoiDung = gio + "  ·  " + thuCung + "  ·  " + ghiChu;
        if (strNoiDung.length() > 62)
            strNoiDung = strNoiDung.substring(0, 59) + "...";
        JLabel lblThongTin = new JLabel(strNoiDung);
        lblThongTin.setToolTipText(gio + " - " + thuCung + " - " + ghiChu);
        lblThongTin.setFont(UIStyle.font(Font.PLAIN, 12));
        lblThongTin.setForeground(new Color(45, 56, 70));
        lblThongTin.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblThongTin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return lblThongTin;
    }

    private String khongRong(String s, String macDinh) {
        return (s == null || s.trim().isEmpty()) ? macDinh : s.trim();
    }

    private JComponent taoChipTongKet(String text) {
        JLabel lblTam = new JLabel(text);
        lblTam.setOpaque(true);
        lblTam.setBackground(new Color(223, 245, 232));
        lblTam.setForeground(new Color(28, 123, 81));
        lblTam.setFont(UIStyle.font(Font.BOLD, 11));
        lblTam.setBorder(new CompoundBorder(new LineBorder(new Color(200, 233, 216)), new EmptyBorder(4, 10, 4, 10)));
        JPanel pnlBoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBoc.setOpaque(false);
        pnlBoc.add(lblTam);
        pnlBoc.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pnlBoc;
    }

    private void moModalHoTro() {
        JDialog dlgHoTro = new JDialog(this, "Hỗ trợ sử dụng Pet Shop Pro", true);
        dlgHoTro.setSize(760, 560);
        dlgHoTro.setLocationRelativeTo(this);
        dlgHoTro.setLayout(new BorderLayout(0, 10));

        JPanel pnlTieuDeHoTro = new JPanel(new BorderLayout());
        pnlTieuDeHoTro.setBackground(UIStyle.NAVY);
        pnlTieuDeHoTro.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel title = new JLabel("HỖ TRỢ - TỔNG QUAN CHỨC NĂNG", taiIcon("/img/logo.png", 24, 24),
                SwingConstants.LEFT);
        title.setForeground(Color.WHITE);
        title.setFont(UIStyle.font(Font.BOLD, 18));
        title.setIconTextGap(10);
        pnlTieuDeHoTro.add(title, BorderLayout.WEST);
        dlgHoTro.add(pnlTieuDeHoTro, BorderLayout.NORTH);

        JEditorPane edpHuongDan = new JEditorPane();
        edpHuongDan.setContentType("text/html");
        edpHuongDan.setEditable(false);
        edpHuongDan.setText(
                """
                        <html>
                        <body style='font-family:Segoe UI; font-size:12px; padding:8px 10px;'>
                        <h2 style='color:#123052; margin-top:0;'>Pet Shop Pro</h2>
                        <p>Đây là màn hình trung tâm giúp nhân viên thao tác nhanh với các chức năng chính của cửa hàng thú cưng.</p>
                        <h3 style='color:#123052;'>Mô tả menu trên thanh điều hướng</h3>
                        <ul>
                          <li><b>Trang chủ:</b> màn hình tổng quan, truy cập nhanh, lịch Spa sắp tới và hỗ trợ sử dụng.</li>
                          <li><b>Hệ thống:</b> quản lý tài khoản nhân viên, quản lý ca làm, đăng xuất và quay về trang chủ.</li>
                          <li><b>Danh mục:</b> quản lý danh mục sản phẩm, sản phẩm, khách hàng và thú cưng.</li>
                          <li><b>Nghiệp vụ:</b> các thao tác vận hành cửa hàng như nhập kho, kiểm kê, lịch hẹn Spa, phiếu dịch vụ, thanh toán, voucher và phân ca nhân viên.</li>
                          <li><b>Tìm kiếm:</b> tra cứu hóa đơn, khách hàng, sản phẩm, thú cưng và lịch hẹn.</li>
                          <li><b>Thống kê:</b> thống kê doanh thu và thống kê lượt mua từ dữ liệu hóa đơn.</li>
                        </ul>
                        <h3 style='color:#123052;'>Khu vực trong trang chủ</h3>
                        <ul>
                          <li><b>Ảnh giới thiệu:</b> hình ảnh nhận diện thương hiệu Pet Shop Pro.</li>
                          <li><b>Truy cập nhanh:</b> mở nhanh các chức năng thường dùng bằng chuột hoặc phím tắt F4-F8.</li>
                          <li><b>Lịch Spa sắp tới:</b> hiển thị lịch hẹn gần nhất lấy từ dữ liệu lịch hẹn Spa.</li>
                          <li><b>Hỗ trợ:</b> mở cửa sổ hướng dẫn tổng quan cách sử dụng giao diện.</li>
                        </ul>
                        <h3 style='color:#123052;'>Gợi ý sử dụng</h3>
                        <ol>
                          <li>Chọn menu chức năng trên thanh điều hướng để mở màn hình tương ứng.</li>
                          <li>Dùng truy cập nhanh khi cần mở nhanh thao tác thường dùng.</li>
                          <li>Kiểm tra lịch Spa sắp tới ở góc dưới bên trái để nắm các lịch hẹn gần nhất.</li>
                          <li>Nhấn tên nhân viên ở góc trên bên phải để xem thông tin đăng nhập hoặc đăng xuất.</li>
                        </ol>
                        <p style='color:#666;'>Lưu ý: một số chức năng phụ thuộc dữ liệu SQL Server. Nếu không hiển thị, hãy kiểm tra kết nối CSDL.</p>
                        </body>
                        </html>
                        """);
        edpHuongDan.setCaretPosition(0);
        JScrollPane scrHuongDan = new JScrollPane(edpHuongDan);
        scrHuongDan.setBorder(new EmptyBorder(0, 12, 0, 12));
        dlgHoTro.add(scrHuongDan, BorderLayout.CENTER);

        JButton btnDongHoTro = UIStyle.nutChinh("Đóng");
        btnDongHoTro.addActionListener(e -> dlgHoTro.dispose());
        JPanel pnlDuoi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        pnlDuoi.setBackground(Color.WHITE);
        pnlDuoi.add(btnDongHoTro);
        dlgHoTro.add(pnlDuoi, BorderLayout.SOUTH);

        dlgHoTro.setVisible(true);
    }

    private void themBtnTruyCapNhanh(JPanel parent, String text, String shortcut, Runnable hanhDong) {
        JPanel pnlDong = new JPanel(new BorderLayout());
        pnlDong.setBackground(MENU_BLUE);
        pnlDong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JButton btnTruyCap = new JButton(text);
        btnTruyCap.setHorizontalAlignment(SwingConstants.LEFT);
        btnTruyCap.setFont(UIStyle.font(Font.PLAIN, 14));
        btnTruyCap.setBackground(MENU_BLUE);
        btnTruyCap.setFocusPainted(false);
        btnTruyCap.setBorderPainted(false);
        btnTruyCap.addActionListener(e -> hanhDong.run());

        String khoa = "truycapnhanh-" + shortcut;
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(shortcut), khoa);
        getRootPane().getActionMap().put(khoa, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                hanhDong.run();
            }
        });

        JLabel lblPhimTat = new JLabel(shortcut);
        lblPhimTat.setFont(UIStyle.font(Font.BOLD, 14));
        lblPhimTat.setBorder(new EmptyBorder(0, 0, 0, 10));

        pnlDong.add(btnTruyCap, BorderLayout.CENTER);
        pnlDong.add(lblPhimTat, BorderLayout.EAST);

        parent.add(pnlDong);
        parent.add(Box.createVerticalStrut(5));
    }

    // =========================================================
    // PHIEN LAM VIEC (nhan vien dang dang nhap, dung chung)
    // =========================================================

    public static final class PhienLamViec {
        public static String maNV = "NV001";
        public static String hoTen = "Đặng Đình An";
        public static String vaiTro = "Quản lý";

        private PhienLamViec() {
        }

        public static void dat(String ma, String ten, String role) {
            maNV = ma;
            hoTen = ten;
            vaiTro = role;
        }
    }

    // =========================================================
    // MAU SAC, FONT, THANH PHAN DUNG CHUNG
    // =========================================================

    public static final class UIStyle {
        public static final Color NAVY = new Color(18, 48, 82);
        public static final Color LIGHT_BLUE = new Color(229, 241, 252);
        public static final Color MENU_BLUE = new Color(207, 222, 235);
        public static final Color ACTIVE_BLUE = new Color(169, 194, 218);
        public static final Color GREEN = new Color(72, 196, 62);
        public static final Color RED = new Color(200, 40, 40);
        public static final Color LINE = new Color(210, 218, 226);
        public static final Color BAR = new Color(247, 249, 251);
        public static final Color READONLY = new Color(235, 235, 235);

        private UIStyle() {
        }

        public static Font font(int style, int size) {
            return new Font("Segoe UI", style, size);
        }

        private static final DecimalFormat DINH_DANG_TIEN;

        static {
            DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
            s.setGroupingSeparator('.');
            DINH_DANG_TIEN = new DecimalFormat("#,##0", s);
        }

        public static String tien(double v) {
            return DINH_DANG_TIEN.format(v) + " ₫";
        }

        public static long docTien(String s) {
            String sach = s == null ? "" : s.replaceAll("[\\s.,₫dđ]", "");
            if (!sach.matches("\\d{1,15}")) {
                throw new NumberFormatException(s);
            }
            return Long.parseLong(sach);
        }

        private static JButton nut(String text, Color nen, Color chu, Color vien) {
            JButton btnTam = new JButton(text);
            btnTam.setFont(font(Font.BOLD, 13));
            btnTam.setBackground(nen);
            btnTam.setForeground(chu);
            btnTam.setOpaque(true);
            btnTam.setContentAreaFilled(true);
            btnTam.setFocusPainted(false);
            btnTam.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (vien != null) {
                btnTam.setBorder(new CompoundBorder(new LineBorder(vien), new EmptyBorder(8, 18, 8, 18)));
            } else {
                btnTam.setBorder(new EmptyBorder(9, 19, 9, 19));
            }
            return btnTam;
        }

        public static JButton nutChinh(String text) {
            return nut(text, NAVY, Color.WHITE, null);
        }

        public static JButton nutXanh(String text) {
            return nut(text, GREEN, Color.WHITE, null);
        }

        public static JButton nutPhu(String text) {
            return nut(text, Color.WHITE, NAVY, NAVY);
        }

        public static JButton nutXoa(String text) {
            return nut(text, Color.WHITE, RED, LINE);
        }

        public static JButton nutTab(String text, boolean dangChon) {
            return nut(text, dangChon ? ACTIVE_BLUE : Color.WHITE,
                    dangChon ? Color.WHITE : NAVY, NAVY);
        }

        public static JLabel nhan(String text) {
            JLabel l = new JLabel(text);
            l.setFont(font(Font.BOLD, 13));
            return l;
        }

        public static JTextField o(int cot) {
            JTextField t = new JTextField(cot);
            t.setFont(font(Font.PLAIN, 13));
            return t;
        }

        public static JTextField oChiDoc(String lblGiaTri) {
            JTextField t = o(10);
            t.setText(lblGiaTri);
            t.setEditable(false);
            t.setBackground(READONLY);
            return t;
        }

        public static void hang(JPanel form, int dong, String ten, JComponent thanhPhan, boolean cao) {
            GridBagConstraints g = new GridBagConstraints();
            g.gridy = dong;
            g.insets = new Insets(6, 0, 6, 15);
            g.anchor = cao ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;

            g.gridx = 0;
            g.weightx = 0;
            form.add(nhan(ten), g);

            g.gridx = 1;
            g.weightx = 1;
            g.insets = new Insets(6, 0, 6, 0);
            g.fill = GridBagConstraints.HORIZONTAL;
            form.add(thanhPhan, g);
        }

        public static JPanel the() {
            JPanel pnlKetQua = new JPanel();
            pnlKetQua.setBackground(Color.WHITE);
            pnlKetQua.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(15, 15, 15, 15)));
            return pnlKetQua;
        }

        public static JPanel theThongKe(String tieuDe, JLabel lblGiaTri) {
            JPanel pnlKetQua = new JPanel();
            pnlKetQua.setLayout(new BoxLayout(pnlKetQua, BoxLayout.Y_AXIS));
            pnlKetQua.setBackground(Color.WHITE);
            pnlKetQua.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(14, 18, 14, 18)));

            JLabel lblTam = new JLabel(tieuDe);
            lblTam.setForeground(Color.GRAY);
            lblTam.setFont(font(Font.PLAIN, 13));
            lblTam.setAlignmentX(Component.LEFT_ALIGNMENT);

            lblGiaTri.setFont(font(Font.BOLD, 26));
            lblGiaTri.setAlignmentX(Component.LEFT_ALIGNMENT);

            pnlKetQua.add(lblTam);
            pnlKetQua.add(Box.createVerticalStrut(6));
            pnlKetQua.add(lblGiaTri);
            return pnlKetQua;
        }

        public static void taoBang(JTable bang, int... cotCanPhai) {
            bang.setRowHeight(32);
            bang.setFont(font(Font.PLAIN, 13));
            bang.setShowGrid(false);
            bang.setShowHorizontalLines(true);
            bang.setGridColor(new Color(225, 230, 236));
            bang.setSelectionBackground(MENU_BLUE);
            bang.setSelectionForeground(Color.BLACK);
            bang.setFillsViewportHeight(true);
            bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JTableHeader header = bang.getTableHeader();
            header.setReorderingAllowed(false);
            header.setPreferredSize(new Dimension(0, 40));
            header.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r,
                        int c) {
                    super.getTableCellRendererComponent(t, v, false, false, r, c);
                    setOpaque(true);
                    setBackground(NAVY);
                    setForeground(Color.WHITE);
                    setFont(font(Font.BOLD, 13));
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setBorder(new javax.swing.border.MatteBorder(0, 0, 0, 1, Color.WHITE));
                    return this;
                }
            });

            bang.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r,
                        int c) {
                    super.getTableCellRendererComponent(t, v, sel, false, r, c);
                    setBorder(new EmptyBorder(0, 10, 0, 10));
                    boolean phai = false;
                    for (int x : cotCanPhai) {
                        if (x == c) {
                            phai = true;
                            break;
                        }
                    }
                    setHorizontalAlignment(phai ? SwingConstants.RIGHT : SwingConstants.LEFT);
                    return this;
                }
            });
        }

        public static JScrollPane cuon(JTable bang) {
            JScrollPane scrHuongDan = new JScrollPane(bang);
            scrHuongDan.setBorder(new LineBorder(LINE));
            scrHuongDan.getViewport().setBackground(Color.WHITE);
            return scrHuongDan;
        }

        public static void thongBao(Component cha, String strNoiDung) {
            JOptionPane.showMessageDialog(cha, strNoiDung, "Pet Shop Pro", JOptionPane.INFORMATION_MESSAGE);
        }

        public static void canhBao(Component cha, String strNoiDung) {
            JOptionPane.showMessageDialog(cha, strNoiDung, "Pet Shop Pro", JOptionPane.WARNING_MESSAGE);
        }

        public static boolean xacNhan(Component cha, String strNoiDung) {
            return JOptionPane.showConfirmDialog(cha, strNoiDung, "Pet Shop Pro",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
        }

        public static void loi(Component cha, Exception ex) {
            String nd;
            if (ex instanceof SQLException sql) {
                if (sql.getErrorCode() == 547) {
                    nd = "Không thực hiện được vì dữ liệu này đang được dùng ở nơi khác\n"
                            + "(ví dụ: danh mục còn sản phẩm, sản phẩm đã có phiếu nhập/hóa đơn).";
                } else if (sql.getErrorCode() == 2627 || sql.getErrorCode() == 2601) {
                    nd = "Mã này đã tồn tại, vui lòng nhập mã khác.";
                } else {
                    nd = "Lỗi cơ sở dữ liệu:\n" + sql.getMessage();
                }
            } else {
                nd = "Có lỗi xảy ra:\n" + ex.getMessage();
            }

            ex.printStackTrace();
            JOptionPane.showMessageDialog(cha, nd, "Pet Shop Pro", JOptionPane.ERROR_MESSAGE);
        }

        public static JPanel hangNut(JButton huy, JButton luu) {
            JPanel pnlKetQua = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnlKetQua.setOpaque(false);
            pnlKetQua.add(huy);
            pnlKetQua.add(luu);
            return pnlKetQua;
        }
    }
}
