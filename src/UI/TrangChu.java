
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
 * KHUNG CHINH cua Pet Shop Pro.
 *
 * - Thanh menu "PET SHOP PRO" nam co dinh o tren cung, KHONG bi mo lai.
 * - Vung noi dung ben duoi doi bang CardLayout (Trang chu, Kho, Spa, Hoa don,
 * He thong...) nen bam menu chi doi phan man hinh ben trong, khong nhay
 * cua so moi. Muc dang chon duoc to sang tren thanh menu.
 * - Moi man hinh duoc tao lan dau khi mo, lan sau chi hien lai.
 *
 * Hai lop tien ich dung chung cho cac man hinh (UIStyle, PhienLamViec)
 * duoc dat ngay trong file nay (cac panel import UI.TrangChu.UIStyle).
 */
public class TrangChu extends JFrame {

    // ==================== MAU GIAO DIEN ====================

    private final Color LIGHT_BLUE = new Color(229, 241, 252);
    private final Color MENU_BLUE = new Color(207, 222, 235);

    // ==================== THONG TIN NHAN VIEN ====================

    private final String maNV;
    private final String hoTen;
    private final String vaiTro;

    // ==================== QUAN LY MAN HINH ====================
    private static final String[] TEN_TAB = { "Danh mục", "Sản phẩm", "Nhập kho", "Kiểm kê kho" };

    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm:ss | dd/MM/yyyy");

    // =========================
    // KHUNG + NOI DUNG DOI THEO CARD
    // =========================
    private final CardLayout card = new CardLayout();
    private final JPanel noiDung = new JPanel(card);

    private JLabel lbNgayTrangChu; // dong ho tren man hinh Trang chu

    // Cac muc co man hinh rieng tren thanh menu (de to sang muc dang chon)
    private JButton btnTrangChu;
    private JButton btnKho;
    private JButton btnSpa;
    private JButton btnHoaDon;
    private JButton btnHeThong;
    private final ArrayList<JButton> nutMucChinh = new ArrayList<>();

    // 4 man hinh Kho: tao lan dau khi mo, lan sau chi nap lai du lieu
    private final JPanel[] manHinh = new JPanel[TEN_TAB.length];
    private final Runnable[] napLai = new Runnable[TEN_TAB.length];

    // Cac man hinh khac (lich hen, thu cung, hoa don, ca lam...): tao lan dau khi
    // mo
    private final Map<String, Component> manHinhKhac = new HashMap<>();
    private TraCuuHoaDon traCuuHoaDon;

    private final javax.swing.Timer dongHo;

    // =========================
    // CONSTRUCTOR
    // =========================
    public TrangChu(String maNV, String hoTen, String vaiTro) {

        this.maNV = maNV;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;

        PhienLamViec.dat(maNV, hoTen, vaiTro);

        setTitle("Pet Shop Pro - Trang chủ");
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // HEADER: dung yen, khong bao gio bi tao lai
        add(taoHeader(), BorderLayout.NORTH);

        // VUNG DUOI HEADER: noi dung doi theo card (sat ngay duoi thanh menu)
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

    // =========================================================
    // CHUYEN MAN HINH (chi doi noi dung ben trong)
    // =========================================================

    private void hienTrangChu() {

        card.show(noiDung, "trangchu");
        datMucDangChon(btnTrangChu);
        setTitle("Pet Shop Pro - Trang chủ");
    }

    /** tab: 0 Danh muc, 1 San pham, 2 Nhap kho, 3 Kiem ke kho. */
    public void chuyenKho(int tab) {

        if (tab < 0 || tab >= TEN_TAB.length) {
            tab = 0;
        }

        boolean laLanDau = manHinh[tab] == null;

        if (laLanDau) {
            taoManHinh(tab);
            noiDung.add(manHinh[tab], "tab" + tab);
        }

        card.show(noiDung, "tab" + tab);
        datMucDangChon(btnKho);
        setTitle("Pet Shop Pro - Kho & Danh mục - " + TEN_TAB[tab]);

        // Lan dau da tu nap du lieu trong constructor cua panel
        if (!laLanDau) {
            napLai[tab].run();
        }

        noiDung.revalidate();
        noiDung.repaint();
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

    /**
     * Hien mot man hinh khac (tao lan dau khi mo) va to sang muc menu tuong ung.
     */
    private void hienManHinh(String khoa, String tieuDe, JButton muc,
            Supplier<? extends Component> tao) {

        Component c = manHinhKhac.get(khoa);

        if (c == null) {
            c = tao.get();
            manHinhKhac.put(khoa, c);
            noiDung.add(c, khoa);
        }

        card.show(noiDung, khoa);
        datMucDangChon(muc);
        setTitle("Pet Shop Pro - " + tieuDe);

        noiDung.revalidate();
        noiDung.repaint();
    }

    private void moLichHen() {
        hienManHinh("lichhen", "Lịch hẹn Spa", btnSpa, QuanLyLichHen::new);
    }

    private void moThuCung() {
        hienManHinh("thucung", "Thú cưng", btnSpa, QuanLyThuCung::new);
    }

    private void moPhieuDichVu() {
        hienManHinh("phieudv", "Phiếu dịch vụ", btnSpa, QuanLyPhieuDichVu::new);
    }

    private void moTraCuuHoaDon() {
        hienManHinh("hoadon", "Hóa đơn", btnHoaDon,
                () -> traCuuHoaDon = new TraCuuHoaDon());
        traCuuHoaDon.hienDanhSach();
    }

    private void moThanhToan() {
        hienManHinh("thanhtoan", "Thanh toán", btnHoaDon,
                () -> new QuanLyThanhToan(maNV));
    }

    private void moQuanLyTaiKhoan() {
        hienManHinh("taikhoan", "Quản lý tài khoản", btnHeThong,
                QuanLyTaiKhoan::new);
    }

    private void moQuanLyPhanCa() {
        hienManHinh("phanca", "Quản lý phân ca", btnHeThong, QuanLyPhanCa::new);
    }

    private void moQuanLyCaLam() {
        hienManHinh("calam", "Quản lý ca làm", btnHeThong, QuanLyCaLam::new);
    }

    private void datMucDangChon(JButton dangChon) {
        for (JButton b : nutMucChinh) {
            b.setBackground(b == dangChon ? UIStyle.ACTIVE_BLUE : UIStyle.NAVY);
        }
    }

    // =========================================================
    // DONG HO (chi hien tren man hinh Trang chu)
    // =========================================================

    private void capNhatGio() {

        String gio = LocalDateTime.now().format(DINH_DANG_GIO);

        if (lbNgayTrangChu != null) {
            lbNgayTrangChu.setText("▣  " + gio);
        }
    }

    // =========================================================
    // HEADER "PET SHOP PRO" (thanh menu tren cung)
    // =========================================================

    private JPanel taoHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIStyle.NAVY);
        header.setBorder(new EmptyBorder(5, 18, 5, 18));

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        menu.setOpaque(false);

        JLabel logo = new JLabel("PET SHOP PRO");
        logo.setForeground(Color.WHITE);
        logo.setFont(UIStyle.font(Font.BOLD, 16));
        menu.add(logo);

        btnTrangChu = nutMenu("Trang chủ", e -> hienTrangChu());
        menu.add(btnTrangChu);

        btnKho = nutMenu("Kho & Danh mục",
                e -> popupKho((JButton) e.getSource()));
        menu.add(btnKho);

        btnSpa = nutMenu("Spa & Lịch hẹn",
                e -> popupSpa((JButton) e.getSource()));
        menu.add(btnSpa);

        btnHoaDon = nutMenu("Hóa Đơn",
                e -> popupHoaDon((JButton) e.getSource()));
        menu.add(btnHoaDon);

        menu.add(nutMenu("Báo cáo",
                e -> popupChuaCo((JButton) e.getSource(),
                        "Theo ngày", "Theo tháng", "Theo năm")));

        btnHeThong = nutMenu("Hệ thống",
                e -> popupHeThong((JButton) e.getSource()));
        menu.add(btnHeThong);

        menu.add(nutMenu("Cài đặt",
                e -> hienThongBao("Cài đặt")));

        nutMucChinh.add(btnTrangChu);
        nutMucChinh.add(btnKho);
        nutMucChinh.add(btnSpa);
        nutMucChinh.add(btnHoaDon);
        nutMucChinh.add(btnHeThong);

        header.add(menu, BorderLayout.WEST);

        JButton btnNhanVien = nutMenu(PhienLamViec.hoTen + "  ●",
                e -> hienThongTinNhanVien((JButton) e.getSource()));

        header.add(btnNhanVien, BorderLayout.EAST);

        return header;
    }

    private JButton nutMenu(String text, ActionListener hanhDong) {

        JButton b = new JButton(text);

        b.setForeground(Color.WHITE);
        b.setBackground(UIStyle.NAVY);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFont(UIStyle.font(Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(hanhDong);

        return b;
    }

    private void themMuc(JPopupMenu popup, String ten, ActionListener hanhDong) {
        JMenuItem item = new JMenuItem(ten);
        item.addActionListener(hanhDong);
        popup.add(item);
    }

    private void popupKho(JButton nguon) {

        JPopupMenu popup = new JPopupMenu();

        for (int i = 0; i < TEN_TAB.length; i++) {
            final int tab = i;
            themMuc(popup, TEN_TAB[i], e -> chuyenKho(tab));
        }

        popup.show(nguon, 0, nguon.getHeight());
    }

    private void popupSpa(JButton nguon) {

        JPopupMenu popup = new JPopupMenu();

        themMuc(popup, "Lịch hẹn", e -> moLichHen());
        themMuc(popup, "Thú cưng", e -> moThuCung());
        themMuc(popup, "Khách hàng", e -> hienChuaCo("Khách hàng"));
        themMuc(popup, "Phiếu dịch vụ", e -> moPhieuDichVu());

        popup.show(nguon, 0, nguon.getHeight());
    }

    private void popupHoaDon(JButton nguon) {

        JPopupMenu popup = new JPopupMenu();

        themMuc(popup, "Hóa đơn", e -> moTraCuuHoaDon());
        themMuc(popup, "Thanh toán", e -> moThanhToan());

        popup.show(nguon, 0, nguon.getHeight());
    }

    // Cac muc chua co giao dien
    private void popupChuaCo(JButton nguon, String... muc) {

        JPopupMenu popup = new JPopupMenu();

        for (String m : muc) {
            themMuc(popup, m, e -> hienChuaCo(m));
        }

        popup.show(nguon, 0, nguon.getHeight());
    }

    private void popupHeThong(JButton nguon) {

        JPopupMenu popup = new JPopupMenu();

        themMuc(popup, "Quản lý tài khoản", e -> moQuanLyTaiKhoan());
        themMuc(popup, "Quản lý phân ca", e -> moQuanLyPhanCa());
        popup.addSeparator();
        themMuc(popup, "Quản lý ca làm", e -> moQuanLyCaLam());

        popup.show(nguon, 0, nguon.getHeight());
    }

    private void hienThongTinNhanVien(JButton nguon) {

        JPopupMenu popup = new JPopupMenu();

        JPanel info = new JPanel(new GridLayout(0, 2, 15, 8));
        info.setBorder(new EmptyBorder(12, 12, 12, 12));

        themThongTin(info, "Mã NV", PhienLamViec.maNV);
        themThongTin(info, "Họ tên", PhienLamViec.hoTen);
        themThongTin(info, "Vai trò", PhienLamViec.vaiTro);
        themThongTin(info, "Trạng thái", "Đang hoạt động");

        popup.add(info);
        popup.addSeparator();

        JMenuItem dangXuat = new JMenuItem("Đăng xuất");
        dangXuat.addActionListener(e -> dangXuat());
        popup.add(dangXuat);

        popup.show(nguon,
                -popup.getPreferredSize().width + nguon.getWidth(),
                nguon.getHeight());
    }

    private void themThongTin(JPanel panel, String nhan, String giaTri) {
        JLabel lb = new JLabel(nhan);
        lb.setFont(UIStyle.font(Font.BOLD, 12));
        panel.add(lb);
        panel.add(new JLabel(giaTri));
    }

    private void dangXuat() {
        dispose();
        new DangNhap().setVisible(true);
    }

    // =========================================================
    // NOI DUNG TRANG CHU
    // =========================================================

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
        panel.setBorder(null);

        try {
            ImageIcon icon = new ImageIcon(
                    getClass().getResource("/img/giothieutrangchu.png"));

            Image anh = icon.getImage().getScaledInstance(
                    550, 510, Image.SCALE_SMOOTH);

            JLabel imageLabel = new JLabel(new ImageIcon(anh));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setVerticalAlignment(SwingConstants.CENTER);

            panel.add(imageLabel, BorderLayout.CENTER);

        } catch (Exception e) {
            JLabel loi = new JLabel("Không tìm thấy ảnh giới thiệu",
                    SwingConstants.CENTER);
            loi.setForeground(UIStyle.RED);
            loi.setFont(UIStyle.font(Font.BOLD, 14));
            panel.add(loi, BorderLayout.CENTER);
        }

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

        addQuickButton(panel, "Thanh toán", "F4", this::moThanhToan);
        addQuickButton(panel, "Quản lý lịch hẹn Spa", "F5", this::moLichHen);
        addQuickButton(panel, "Quản lý sản phẩm", "F6", () -> chuyenKho(1));
        addQuickButton(panel, "Quản lý khách hàng", "F7",
                () -> hienChuaCo("Quản lý khách hàng"));
        addQuickButton(panel, "Báo cáo doanh thu", "F8",
                () -> hienChuaCo("Báo cáo doanh thu"));

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