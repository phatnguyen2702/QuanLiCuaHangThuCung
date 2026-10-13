package UI;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Cua so chinh Pet Shop Pro - menu theo HUONG CHUC NANG.
 * 6 nhom: He thong, Danh muc, Cap nhat, Xu ly, Tim kiem, Bao bieu.
 * Moi chuc nang mo JPanel trong CardLayout, khong mo JFrame moi.
 * Giu lai UIStyle va PhienLamViec long nhau cho cac lop UI hien tai.
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

    private final CardLayout card = new CardLayout();
    private final JPanel noiDung = new JPanel(card);
    private JLabel lbNgayTrangChu;
    private JButton btnTrangChu;
    private JButton btnHeThong;
    private JButton btnDanhMuc;
    private JButton btnCapNhat;
    private JButton btnXuLy;
    private JButton btnTimKiem;
    private JButton btnBaoBieu;
    private final ArrayList<JButton> nutMucChinh = new ArrayList<>();
    private JButton nhomDaChon;

    // Man hinh kho; nap lai du lieu khi quay lai.
    private final JPanel[] manHinh = new JPanel[TEN_TAB.length];
    private final Runnable[] napLai = new Runnable[TEN_TAB.length];
    // Man hinh khac khoi tao mot lan, tai su dung trong CardLayout.
    private final Map<String, Component> manHinhKhac = new HashMap<>();
    private TraCuuHoaDon traCuuHoaDon;
    private final javax.swing.Timer dongHo;

    public TrangChu(String maNV, String hoTen, String vaiTro) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;

        // Hai lop PhienLamViec dang ton tai trong project: dong bo ca hai.
        PhienLamViec.dat(maNV, hoTen, vaiTro);
        UI.PhienLamViec.dat(maNV, hoTen, vaiTro);

        setTitle("Pet Shop Pro - Trang chủ");
        setSize(1200, 720);
        setMinimumSize(new Dimension(960, 610));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(taoHeader(), BorderLayout.NORTH);

        noiDung.setBackground(LIGHT_BLUE);
        noiDung.add(createMainContent(), "trangchu");
        add(noiDung, BorderLayout.CENTER);

        dongHo = new javax.swing.Timer(1000, e -> capNhatGio());
        dongHo.start();
        capNhatGio();
        hienTrangChu();
    }

    @Override
    public void dispose() {
        dongHo.stop();
        super.dispose();
    }

    private void hienTrangChu() {
        nhomDaChon = null;
        card.show(noiDung, "trangchu");
        datMucDangChon(btnTrangChu);
        setTitle("Pet Shop Pro - Trang chủ");
    }

    /** API cu cho DieuHuong.moKho(cha, tab) va nhung man hinh khac. */
    public void chuyenKho(int tab) {
        if (tab < 0 || tab >= TEN_TAB.length)
            tab = 0;
        try {
            boolean laLanDau = manHinh[tab] == null;
            if (laLanDau) {
                taoManHinh(tab);
                noiDung.add(manHinh[tab], "tab" + tab);
            }
            card.show(noiDung, "tab" + tab);
            datMucDangChon(nhomDaChon != null ? nhomDaChon : btnDanhMuc);
            setTitle("Pet Shop Pro - " + TEN_TAB[tab]);
            if (!laLanDau && napLai[tab] != null)
                napLai[tab].run();
            noiDung.revalidate();
            noiDung.repaint();
        } catch (RuntimeException ex) {
            baoLoiMoManHinh(TEN_TAB[tab], ex);
        }
    }

    private void taoManHinh(int tab) {
        switch (tab) {
            case 0 -> {
                DanhMucPanel p = new DanhMucPanel();
                manHinh[tab] = p;
                napLai[tab] = p::napLai;
            }
            case 1 -> {
                SanPhamPanel p = new SanPhamPanel();
                manHinh[tab] = p;
                napLai[tab] = p::napLai;
            }
            case 2 -> {
                NhapKhoPanel p = new NhapKhoPanel();
                manHinh[tab] = p;
                napLai[tab] = p::napLai;
            }
            default -> {
                KiemKePanel p = new KiemKePanel();
                manHinh[tab] = p;
                napLai[tab] = p::napLai;
            }
        }
    }

    private void hienManHinh(String khoa, String tieuDe, JButton nhomMacDinh,
            Supplier<? extends Component> tao) {
        try {
            Component c = manHinhKhac.get(khoa);
            if (c == null) {
                c = tao.get();
                manHinhKhac.put(khoa, c);
                noiDung.add(c, khoa);
            }
            card.show(noiDung, khoa);
            datMucDangChon(nhomDaChon != null ? nhomDaChon : nhomMacDinh);
            setTitle("Pet Shop Pro - " + tieuDe);
            noiDung.revalidate();
            noiDung.repaint();
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
        hienManHinh("lichhen", "Lịch hẹn Spa", btnCapNhat, QuanLyLichHen::new);
    }

    private void moThuCung() {
        hienManHinh("thucung", "Quản lý thú cưng", btnDanhMuc, QuanLyThuCung::new);
    }

    private void moKhachHang() {
        // Constructor THUC TE cua file QuanLyKhachHang.java can ma nhan vien.
        hienManHinh("khachhang", "Quản lý khách hàng", btnDanhMuc,
                () -> new QuanLyKhachHang(maNV));
    }

    private void moVoucher() {
        hienManHinh("voucher", "Quản lý voucher", btnCapNhat, QuanLyVoucher::new);
    }

    private void moPhieuDichVu() {
        hienManHinh("phieudv", "Quản lý phiếu dịch vụ", btnCapNhat,
                QuanLyPhieuDichVu::new);
    }

    private void moTraCuuHoaDon() {
        hienManHinh("hoadon", "Tra cứu hóa đơn", btnTimKiem,
                () -> traCuuHoaDon = new TraCuuHoaDon());
        if (traCuuHoaDon != null) {
            try {
                traCuuHoaDon.hienDanhSach();
            } catch (RuntimeException ex) {
                baoLoiMoManHinh("Tra cứu hóa đơn", ex);
            }
        }
    }

    private void moThanhToan() {
        hienManHinh("thanhtoan", "Thanh toán", btnXuLy,
                () -> new QuanLyThanhToan(maNV));
    }

    private void moQuanLyTaiKhoan() {
        hienManHinh("taikhoan", "Quản lý tài khoản", btnHeThong,
                QuanLyTaiKhoan::new);
    }

    private void moQuanLyPhanCa() {
        hienManHinh("phanca", "Quản lý phân ca", btnHeThong,
                QuanLyPhanCa::new);
    }

    private void moQuanLyCaLam() {
        hienManHinh("calam", "Quản lý ca làm", btnHeThong,
                QuanLyCaLam::new);
    }

    private void chuyenTuNhom(JButton nhom, Runnable hanhDong) {
        nhomDaChon = nhom;
        hanhDong.run();
    }

    // To sang muc dang mo ngay tren thanh menu cung hang.
    private void datMucDangChon(JButton dangChon) {
        for (JButton b : nutMucChinh) {
            boolean active = b == dangChon;
            b.setBackground(active ? new Color(48, 91, 138) : UIStyle.NAVY);
            b.setForeground(Color.WHITE);
            b.setFont(UIStyle.font(active ? Font.BOLD : Font.PLAIN, 12));
        }
        if (btnTrangChu != null) {
            boolean active = btnTrangChu == dangChon;
            btnTrangChu.setBackground(active ? new Color(48, 91, 138) : UIStyle.NAVY);
            btnTrangChu.setForeground(Color.WHITE);
            btnTrangChu.setFont(UIStyle.font(active ? Font.BOLD : Font.PLAIN, 12));
        }
    }

    private void capNhatGio() {
        if (lbNgayTrangChu != null) {
            lbNgayTrangChu.setText("▣  " + LocalDateTime.now().format(DINH_DANG_GIO));
        }
    }

    // ==================== MENU THEO HUONG CHUC NANG ====================
    private JPanel taoHeader() {
        // Chi mot hang: thuong hieu | Trang chu + 6 nhom chuc nang | nhan vien.
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(UIStyle.NAVY);
        header.setBorder(new EmptyBorder(7, 12, 7, 12));

        JLabel logo = new JLabel("PET SHOP PRO");
        logo.setForeground(Color.WHITE);
        logo.setFont(UIStyle.font(Font.BOLD, 16));
        logo.setHorizontalAlignment(SwingConstants.LEFT);
        logo.setPreferredSize(new Dimension(148, 38));
        header.add(logo, BorderLayout.WEST);

        JPanel menu = new JPanel(new GridLayout(1, 7, 2, 0));
        menu.setOpaque(false);

        btnTrangChu = nutNhom("Trang chủ");
        btnTrangChu.addActionListener(e -> hienTrangChu());

        btnHeThong = nutNhom("HỆ THỐNG");
        btnDanhMuc = nutNhom("DANH MỤC");
        btnCapNhat = nutNhom("CẬP NHẬT");
        btnXuLy = nutNhom("XỬ LÝ");
        btnTimKiem = nutNhom("TÌM KIẾM");
        btnBaoBieu = nutNhom("BÁO BIỂU");

        menu.add(btnTrangChu);
        menu.add(btnHeThong);
        menu.add(btnDanhMuc);
        menu.add(btnCapNhat);
        menu.add(btnXuLy);
        menu.add(btnTimKiem);
        menu.add(btnBaoBieu);

        nutMucChinh.add(btnHeThong);
        nutMucChinh.add(btnDanhMuc);
        nutMucChinh.add(btnCapNhat);
        nutMucChinh.add(btnXuLy);
        nutMucChinh.add(btnTimKiem);
        nutMucChinh.add(btnBaoBieu);

        btnHeThong.addActionListener(e -> hienPopup(btnHeThong, taoMenuHeThong()));
        btnDanhMuc.addActionListener(e -> hienPopup(btnDanhMuc, taoMenuDanhMuc()));
        btnCapNhat.addActionListener(e -> hienPopup(btnCapNhat, taoMenuCapNhat()));
        btnXuLy.addActionListener(e -> hienPopup(btnXuLy, taoMenuXuLy()));
        btnTimKiem.addActionListener(e -> hienPopup(btnTimKiem, taoMenuTimKiem()));
        btnBaoBieu.addActionListener(e -> hienPopup(btnBaoBieu, taoMenuBaoBieu()));

        header.add(menu, BorderLayout.CENTER);

        JButton btnNhanVien = new JButton(hoTen == null ? "Tài khoản" : hoTen);
        btnNhanVien.setFont(UIStyle.font(Font.BOLD, 12));
        btnNhanVien.setForeground(Color.WHITE);
        btnNhanVien.setBackground(UIStyle.NAVY);
        btnNhanVien.setFocusPainted(false);
        btnNhanVien.setOpaque(true);
        btnNhanVien.setBorder(new EmptyBorder(8, 6, 8, 6));
        btnNhanVien.setPreferredSize(new Dimension(165, 38));
        btnNhanVien.setToolTipText("Thông tin nhân viên / Đăng xuất");
        btnNhanVien.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNhanVien.addActionListener(e -> hienThongTinNhanVien(btnNhanVien));
        header.add(btnNhanVien, BorderLayout.EAST);

        return header;
    }

    private JButton nutNhom(String title) {
        JButton b = new JButton(title);
        b.setFont(UIStyle.font(Font.PLAIN, 12));
        b.setBackground(UIStyle.NAVY);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 3, 8, 3));
        b.setToolTipText(title.equals("Trang chủ") ? "Về trang chủ" : "Mở menu " + title);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void hienPopup(JButton nguon, JPopupMenu popup) {
        popup.show(nguon, 0, nguon.getHeight());
    }

    private void themMuc(JPopupMenu popup, String ten, JButton nhom, Runnable hanhDong) {
        JMenuItem item = new JMenuItem(ten);
        item.setFont(UIStyle.font(Font.PLAIN, 13));
        item.setBorder(new EmptyBorder(9, 14, 9, 14));
        item.addActionListener(e -> chuyenTuNhom(nhom, hanhDong));
        popup.add(item);
    }

    private JPopupMenu taoMenuHeThong() {
        JPopupMenu p = new JPopupMenu();
        themMuc(p, "Quản lý tài khoản", btnHeThong, this::moQuanLyTaiKhoan);
        themMuc(p, "Quản lý phân ca", btnHeThong, this::moQuanLyPhanCa);
        themMuc(p, "Quản lý ca làm", btnHeThong, this::moQuanLyCaLam);
        p.addSeparator();
        themMuc(p, "Trang chủ", btnHeThong, this::hienTrangChu);
        themMuc(p, "Đăng xuất", btnHeThong, this::dangXuat);
        return p;
    }

    private JPopupMenu taoMenuDanhMuc() {
        JPopupMenu p = new JPopupMenu();
        themMuc(p, "Danh mục sản phẩm", btnDanhMuc, () -> chuyenKho(0));
        themMuc(p, "Sản phẩm", btnDanhMuc, () -> chuyenKho(1));
        themMuc(p, "Khách hàng", btnDanhMuc, this::moKhachHang);
        themMuc(p, "Thú cưng", btnDanhMuc, this::moThuCung);
        themMuc(p, "Voucher / Mã giảm giá", btnDanhMuc, this::moVoucher);
        return p;
    }

    private JPopupMenu taoMenuCapNhat() {
        JPopupMenu p = new JPopupMenu();
        themMuc(p, "Nhập kho", btnCapNhat, () -> chuyenKho(2));
        themMuc(p, "Kiểm kê kho", btnCapNhat, () -> chuyenKho(3));
        themMuc(p, "Lịch hẹn Spa", btnCapNhat, this::moLichHen);
        themMuc(p, "Phiếu dịch vụ", btnCapNhat, this::moPhieuDichVu);
        themMuc(p, "Voucher / Mã giảm giá", btnCapNhat, this::moVoucher);
        return p;
    }

    private JPopupMenu taoMenuXuLy() {
        JPopupMenu p = new JPopupMenu();
        themMuc(p, "Thanh toán hóa đơn", btnXuLy, this::moThanhToan);
        themMuc(p, "Xử lý lịch hẹn Spa", btnXuLy, this::moLichHen);
        themMuc(p, "Phân ca nhân viên", btnXuLy, this::moQuanLyPhanCa);
        themMuc(p, "Quản lý ca làm", btnXuLy, this::moQuanLyCaLam);
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

    private JPopupMenu taoMenuBaoBieu() {
        JPopupMenu p = new JPopupMenu();
        // Project chua co panel BaoCao rieng: mo cac man hinh danh sach san co.
        themMuc(p, "Danh sách hóa đơn", btnBaoBieu, this::moTraCuuHoaDon);
        themMuc(p, "Danh sách khách hàng", btnBaoBieu, this::moKhachHang);
        themMuc(p, "Danh sách sản phẩm", btnBaoBieu, () -> chuyenKho(1));
        themMuc(p, "Danh sách voucher", btnBaoBieu, this::moVoucher);
        return p;
    }

    private void hienThongTinNhanVien(JButton nguon) {
        JPopupMenu popup = new JPopupMenu();
        JPanel info = new JPanel(new GridLayout(0, 2, 15, 8));
        info.setBorder(new EmptyBorder(12, 12, 12, 12));
        themThongTin(info, "Mã NV", maNV);
        themThongTin(info, "Họ tên", hoTen);
        themThongTin(info, "Vai trò", vaiTro);
        themThongTin(info, "Trạng thái", "Đang hoạt động");
        popup.add(info);
        popup.addSeparator();
        JMenuItem dangXuat = new JMenuItem("Đăng xuất");
        dangXuat.addActionListener(e -> dangXuat());
        popup.add(dangXuat);
        popup.show(nguon,
                -popup.getPreferredSize().width + nguon.getWidth(), nguon.getHeight());
    }

    private void themThongTin(JPanel panel, String nhan, String giaTri) {
        JLabel label = new JLabel(nhan);
        label.setFont(UIStyle.font(Font.BOLD, 12));
        panel.add(label);
        panel.add(new JLabel(giaTri != null ? giaTri : ""));
    }

    private void dangXuat() {
        dispose();
        new DangNhap().setVisible(true);
    }

    // ==================== NOI DUNG TRANG CHU ====================
    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(LIGHT_BLUE);

        main.add(createTitleBar(), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 20, 0));
        center.setBackground(LIGHT_BLUE);
        center.setBorder(new EmptyBorder(20, 30, 30, 30));

        center.add(createIntroduction());
        center.add(createQuickAccess());

        main.add(center, BorderLayout.CENTER);

        return main;
    }

    // =========================================================
    // THANH TIEU DE
    // =========================================================

    private JPanel createTitleBar() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIStyle.BAR);
        panel.setBorder(new EmptyBorder(10, 25, 10, 25));

        JLabel date = new JLabel("▣  " + LocalDateTime.now().format(DINH_DANG_GIO));
        lbNgayTrangChu = date;
        date.setFont(UIStyle.font(Font.BOLD, 12));

        JLabel title = new JLabel("TRANG CHỦ", SwingConstants.CENTER);
        title.setFont(UIStyle.font(Font.BOLD, 25));

        panel.add(date, BorderLayout.WEST);
        panel.add(title, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================
    // ANH GIOI THIEU
    // =========================================================

    private JPanel createIntroduction() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        java.net.URL url = getClass().getResource("/img/giothieutrangchu.png");
        if (url == null) {
            panel.add(new JLabel("Không tìm thấy ảnh giới thiệu", SwingConstants.CENTER));
            return panel;
        }
        Image anh = new ImageIcon(url).getImage();
        JPanel anhCoGian = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int w = getWidth(), h = getHeight();
                int iw = anh.getWidth(this), ih = anh.getHeight(this);
                if (iw <= 0 || ih <= 0 || w <= 0 || h <= 0)
                    return;
                double tile = Math.min((double) w / iw, (double) h / ih);
                int newW = (int) (iw * tile), newH = (int) (ih * tile);
                g.drawImage(anh, (w - newW) / 2, (h - newH) / 2,
                        newW, newH, this);
            }
        };
        anhCoGian.setBackground(Color.WHITE);
        panel.add(anhCoGian, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================
    // TRUY CAP NHANH
    // =========================================================

    private JPanel createQuickAccess() {

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("TRUY CẬP NHANH");
        title.setFont(UIStyle.font(Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        addQuickButton(panel, "Thanh toán", "F4",
                () -> chuyenTuNhom(btnXuLy, this::moThanhToan));
        addQuickButton(panel, "Quản lý lịch hẹn Spa", "F5",
                () -> chuyenTuNhom(btnCapNhat, this::moLichHen));
        addQuickButton(panel, "Quản lý sản phẩm", "F6",
                () -> chuyenTuNhom(btnDanhMuc, () -> chuyenKho(1)));
        addQuickButton(panel, "Quản lý khách hàng", "F7",
                () -> chuyenTuNhom(btnDanhMuc, this::moKhachHang));
        addQuickButton(panel, "Tra cứu hóa đơn", "F8",
                () -> chuyenTuNhom(btnTimKiem, this::moTraCuuHoaDon));

        return panel;
    }

    // =========================================================
    // BUTTON TRUY CAP NHANH (bam chuot hoac phim tat)
    // =========================================================

    private void addQuickButton(
            JPanel parent,
            String text,
            String shortcut,
            Runnable hanhDong) {

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(MENU_BLUE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(UIStyle.font(Font.PLAIN, 14));
        button.setBackground(MENU_BLUE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.addActionListener(e -> hanhDong.run());

        // Phim tat (F4...F8) hoat dong o moi man hinh
        String khoa = "truycapnhanh-" + shortcut;
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(shortcut), khoa);
        getRootPane().getActionMap().put(khoa, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                hanhDong.run();
            }
        });

        JLabel key = new JLabel(shortcut);
        key.setFont(UIStyle.font(Font.BOLD, 14));
        key.setBorder(new EmptyBorder(0, 0, 0, 10));

        row.add(button, BorderLayout.CENTER);
        row.add(key, BorderLayout.EAST);

        parent.add(row);
        parent.add(Box.createVerticalStrut(5));
    }

    // =========================================================
    // THONG BAO
    // =========================================================

    private void hienThongBao(String tenChucNang) {

        JOptionPane.showMessageDialog(
                this,
                "Bạn đang mở chức năng: " + tenChucNang,
                "Pet Shop Pro",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void hienChuaCo(String tenChucNang) {

        JOptionPane.showMessageDialog(
                this,
                "Chức năng \"" + tenChucNang + "\" chưa có giao diện.",
                "Pet Shop Pro",
                JOptionPane.INFORMATION_MESSAGE);
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
    // MAU SAC, FONT, THANH PHAN DUNG CHUNG (lay tu giao dien Pet Shop Pro)
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

        // =========================================================
        // FONT + DINH DANG
        // =========================================================

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

        /** Doc so tien VND nguoi dung go ("335000", "335.000", "335,000 d"). */
        public static long docTien(String s) {
            String sach = s == null ? "" : s.replaceAll("[\\s.,₫dđ]", "");
            if (!sach.matches("\\d{1,15}")) {
                throw new NumberFormatException(s);
            }
            return Long.parseLong(sach);
        }

        // =========================================================
        // NUT BAM
        // =========================================================

        private static JButton nut(String text, Color nen, Color chu, Color vien) {

            JButton b = new JButton(text);

            b.setFont(font(Font.BOLD, 13));
            b.setBackground(nen);
            b.setForeground(chu);
            b.setOpaque(true);
            b.setContentAreaFilled(true);
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (vien != null) {
                b.setBorder(new CompoundBorder(
                        new LineBorder(vien),
                        new EmptyBorder(8, 18, 8, 18)));
            } else {
                b.setBorder(new EmptyBorder(9, 19, 9, 19));
            }

            return b;
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
            JButton b = nut(text, dangChon ? ACTIVE_BLUE : Color.WHITE,
                    dangChon ? Color.WHITE : NAVY, NAVY);
            return b;
        }

        // =========================================================
        // O NHAP / NHAN
        // =========================================================

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

        public static JTextField oChiDoc(String giaTri) {
            JTextField t = o(10);
            t.setText(giaTri);
            t.setEditable(false);
            t.setBackground(READONLY);
            return t;
        }

        /** Them 1 dong "nhan + o nhap" vao form dung GridBagLayout. */
        public static void hang(JPanel form, int dong, String ten,
                JComponent thanhPhan, boolean cao) {

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

        // =========================================================
        // THE / THONG KE
        // =========================================================

        public static JPanel the() {
            JPanel p = new JPanel();
            p.setBackground(Color.WHITE);
            p.setBorder(new CompoundBorder(
                    new LineBorder(LINE),
                    new EmptyBorder(15, 15, 15, 15)));
            return p;
        }

        public static JPanel theThongKe(String tieuDe, JLabel giaTri) {

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(Color.WHITE);
            p.setBorder(new CompoundBorder(
                    new LineBorder(LINE),
                    new EmptyBorder(14, 18, 14, 18)));

            JLabel lb = new JLabel(tieuDe);
            lb.setForeground(Color.GRAY);
            lb.setFont(font(Font.PLAIN, 13));
            lb.setAlignmentX(Component.LEFT_ALIGNMENT);

            giaTri.setFont(font(Font.BOLD, 26));
            giaTri.setAlignmentX(Component.LEFT_ALIGNMENT);

            p.add(lb);
            p.add(Box.createVerticalStrut(6));
            p.add(giaTri);

            return p;
        }

        // =========================================================
        // BANG
        // =========================================================

        /** Dinh dang bang theo kieu Pet Shop Pro (header navy, hang cao 32). */
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
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc,
                        int r, int c) {
                    super.getTableCellRendererComponent(t, v, false, false, r, c);
                    setOpaque(true);
                    setBackground(NAVY);
                    setForeground(Color.WHITE);
                    setFont(font(Font.BOLD, 13));
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setBorder(new javax.swing.border.MatteBorder(
                            0, 0, 0, 1, Color.WHITE));
                    return this;
                }
            });

            bang.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc,
                        int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, false, r, c);
                    setBorder(new EmptyBorder(0, 10, 0, 10));

                    boolean phai = false;
                    for (int x : cotCanPhai) {
                        if (x == c) {
                            phai = true;
                            break;
                        }
                    }
                    setHorizontalAlignment(phai ? SwingConstants.RIGHT
                            : SwingConstants.LEFT);
                    return this;
                }
            });
        }

        public static JScrollPane cuon(JTable bang) {
            JScrollPane sp = new JScrollPane(bang);
            sp.setBorder(new LineBorder(LINE));
            sp.getViewport().setBackground(Color.WHITE);
            return sp;
        }

        public static void thongBao(Component cha, String noiDung) {
            JOptionPane.showMessageDialog(cha, noiDung, "Pet Shop Pro",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        public static void canhBao(Component cha, String noiDung) {
            JOptionPane.showMessageDialog(cha, noiDung, "Pet Shop Pro",
                    JOptionPane.WARNING_MESSAGE);
        }

        public static boolean xacNhan(Component cha, String noiDung) {
            return JOptionPane.showConfirmDialog(cha, noiDung, "Pet Shop Pro",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
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

            JOptionPane.showMessageDialog(cha, nd, "Pet Shop Pro",
                    JOptionPane.ERROR_MESSAGE);
        }

        /** Hang nut Huy / Luu o cuoi hop thoai. */
        public static JPanel hangNut(JButton huy, JButton luu) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            p.setOpaque(false);
            p.add(huy);
            p.add(luu);
            return p;
        }
    }
}