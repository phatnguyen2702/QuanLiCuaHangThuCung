package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TrangChu extends JFrame {

        // =========================================================
        // MAU GIAO DIEN
        // =========================================================

        private final Color NAVY = new Color(18, 48, 82);
        private final Color LIGHT_BLUE = new Color(229, 241, 252);
        private final Color MENU_BLUE = new Color(207, 222, 235);
        private final Color BG = new Color(245, 247, 250);
        private final Color GREEN = new Color(72, 196, 62);
        private final Color RED = new Color(210, 70, 70);

        // =========================================================
        // THONG TIN NHAN VIEN DANG DANG NHAP
        // =========================================================

        private String maNV;
        private String hoTen;
        private String vaiTro;

        // =========================================================
        // CARD LAYOUT
        // =========================================================

        private CardLayout cardLayout;
        private JPanel contentPanel;

        // =========================================================
        // CAC MAN HINH
        // =========================================================

        private QuanLyTaiKhoan quanLyTaiKhoan;
        private QuanLyCaLam quanLyCaLam;
        private QuanLyPhanCa quanLyPhanCa;
        private QuanLyLichHen quanLyLichHen;
        private QuanLyThuCung quanLyThuCung;
        private QuanLyPhieuDichVu quanLyPhieuDichVu;
        private TraCuuHoaDon traCuuHoaDon;
        private QuanLyThanhToan quanLyThanhToan;
        private final ManHinhKho[] manHinhKho = new ManHinhKho[4];
        private final JButton[] nutTabKho = new JButton[4];
        private CardLayout khoCardLayout;
        private JPanel khoContentPanel;
        // =========================================================
        // CONSTRUCTOR
        // =========================================================

        public TrangChu(
                        String maNV,
                        String hoTen,
                        String vaiTro) {

                this.maNV = maNV;
                this.hoTen = hoTen;
                this.vaiTro = vaiTro;

                // =====================================================
                // CAU HINH FRAME
                // =====================================================

                setTitle("Pet Shop Pro - Trang chu");

                setSize(1200, 720);

                setLocationRelativeTo(null);

                setDefaultCloseOperation(
                                JFrame.EXIT_ON_CLOSE);

                setResizable(false);

                setLayout(new BorderLayout());

                // =====================================================
                // HEADER
                // =====================================================

                add(
                                createHeader(),
                                BorderLayout.NORTH);

                // =====================================================
                // CARD LAYOUT
                // =====================================================

                cardLayout = new CardLayout();

                contentPanel = new JPanel(
                                cardLayout);

                contentPanel.setBackground(BG);

                // =====================================================
                // TRANG CHU
                // =====================================================

                contentPanel.add(
                                createMainContent(),
                                "TRANG_CHU");

                quanLyTaiKhoan = new QuanLyTaiKhoan();

                contentPanel.add(
                                quanLyTaiKhoan,
                                "TAI_KHOAN");

                quanLyCaLam = new QuanLyCaLam();

                contentPanel.add(
                                quanLyCaLam,
                                "CA_LAM");

                quanLyPhanCa = new QuanLyPhanCa();

                contentPanel.add(
                                quanLyPhanCa,
                                "PHAN_CA");
                quanLyLichHen = new QuanLyLichHen();
                contentPanel.add(quanLyLichHen, "LICH_HEN");

                quanLyThuCung = new QuanLyThuCung();
                contentPanel.add(quanLyThuCung, "THU_CUNG");

                quanLyPhieuDichVu = new QuanLyPhieuDichVu();
                contentPanel.add(quanLyPhieuDichVu, "PHIEU_DICH_VU");
                contentPanel.add(taoManHinhKho(), "KHO");

                // =====================================================
                // ADD CONTENT
                // =====================================================

                add(
                                contentPanel,
                                BorderLayout.CENTER);

                // =====================================================
                // MAC DINH HIEN TRANG CHU
                // =====================================================

                hienTrangChu();
        }

        // =========================================================
        // HEADER - MENU CHUNG
        // =========================================================

        private JPanel createHeader() {

                JPanel header = new JPanel(
                                new BorderLayout());

                header.setBackground(NAVY);

                header.setBorder(
                                new EmptyBorder(
                                                5,
                                                18,
                                                5,
                                                18));

                // =====================================================
                // MENU BEN TRAI
                // =====================================================

                JPanel menu = new JPanel(
                                new FlowLayout(
                                                FlowLayout.LEFT,
                                                5,
                                                8));

                menu.setOpaque(false);

                // =====================================================
                // LOGO
                // =====================================================

                JLabel logo = new JLabel(
                                "PET SHOP PRO");

                logo.setForeground(
                                Color.WHITE);

                logo.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                16));

                logo.setBorder(
                                new EmptyBorder(
                                                0,
                                                5,
                                                0,
                                                10));

                menu.add(logo);

                // =====================================================
                // TRANG CHU
                // =====================================================

                menu.add(
                                createMenuButton(
                                                "Trang chu",
                                                e -> hienTrangChu()));

                // =====================================================
                // KHO
                // =====================================================

                menu.add(
                                createMenuButton(
                                                "Kho & Danh muc",
                                                e -> hienPopupKho(
                                                                (JButton) e.getSource())));

                // =====================================================
                // SPA
                // =====================================================

                menu.add(
                                createMenuButton(
                                                "Spa & Lich hen",
                                                e -> hienPopupSpa(
                                                                (JButton) e.getSource())));

                // =====================================================
                // HOA DON
                // =====================================================

                menu.add(
                                createMenuButton(
                                                "Hoa don",
                                                e -> hienPopupHoaDon(
                                                                (JButton) e.getSource())));

                // =====================================================
                // BAO CAO
                // =====================================================

                menu.add(
                                createMenuButton(
                                                "Bao cao",
                                                e -> hienPopupBaoCao(
                                                                (JButton) e.getSource())));

                // =====================================================
                // HE THONG
                // =====================================================

                menu.add(
                                createMenuButton(
                                                "He thong",
                                                e -> hienPopupHeThong(
                                                                (JButton) e.getSource())));

                // =====================================================
                // CAI DAT
                // =====================================================

                menu.add(
                                createMenuButton(
                                                "Cai dat",
                                                e -> hienThongBao(
                                                                "Cai dat")));

                header.add(
                                menu,
                                BorderLayout.WEST);

                // =====================================================
                // USER BEN PHAI
                // =====================================================

                JButton btnNhanVien = createMenuButton(
                                hoTen + "  ●",
                                e -> hienThongTinNhanVien(
                                                (JButton) e.getSource()));

                header.add(
                                btnNhanVien,
                                BorderLayout.EAST);

                return header;
        }

        // =========================================================
        // TAO BUTTON MENU
        // =========================================================

        private JButton createMenuButton(
                        String text,
                        java.awt.event.ActionListener action) {

                JButton button = new JButton(text);

                button.setForeground(
                                Color.WHITE);

                button.setBackground(
                                NAVY);

                button.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                12));

                button.setFocusPainted(false);

                button.setBorderPainted(false);

                button.setCursor(
                                new Cursor(
                                                Cursor.HAND_CURSOR));

                button.addActionListener(
                                action);

                return button;
        }

        // =========================================================
        // HIEN TRANG CHU
        // =========================================================

        private void hienTrangChu() {

                cardLayout.show(
                                contentPanel,
                                "TRANG_CHU");
        }

        // =========================================================
        // HIEN QUAN LY TAI KHOAN
        // =========================================================

        private void hienQuanLyTaiKhoan() {

                cardLayout.show(
                                contentPanel,
                                "TAI_KHOAN");
        }

        // =========================================================
        // HIEN QUAN LY CA LAM
        // =========================================================

        private void hienQuanLyCaLam() {

                cardLayout.show(
                                contentPanel,
                                "CA_LAM");
        }

        // =========================================================
        // HIEN QUAN LY PHAN CA
        // =========================================================
        private void hienQuanLyPhanCa() {

                cardLayout.show(
                                contentPanel,
                                "PHAN_CA");
        }
        // =========================================================
        // NOI DUNG TRANG CHU
        // =========================================================

        private JPanel createMainContent() {

                JPanel main = new JPanel(
                                new BorderLayout());

                main.setBackground(
                                LIGHT_BLUE);

                main.add(
                                createTitleBar(),
                                BorderLayout.NORTH);

                // =====================================================
                // CENTER
                // =====================================================

                JPanel center = new JPanel(
                                new GridLayout(
                                                1,
                                                2,
                                                20,
                                                0));

                center.setBackground(
                                LIGHT_BLUE);

                center.setBorder(
                                new EmptyBorder(
                                                20,
                                                30,
                                                30,
                                                30));

                center.add(
                                createIntroduction());

                center.add(
                                createQuickAccess());

                main.add(
                                center,
                                BorderLayout.CENTER);

                return main;
        }

        // =========================================================
        // TITLE BAR
        // =========================================================

        private JPanel createTitleBar() {

                JPanel panel = new JPanel(
                                new BorderLayout());

                panel.setBackground(
                                new Color(
                                                247,
                                                249,
                                                251));

                panel.setBorder(
                                new EmptyBorder(
                                                10,
                                                25,
                                                10,
                                                25));

                // =====================================================
                // DATE
                // =====================================================

                JLabel date = new JLabel(
                                "▣  "
                                                + LocalDateTime.now()
                                                                .format(
                                                                                DateTimeFormatter.ofPattern(
                                                                                                "HH:mm:ss | dd/MM/yyyy")));

                date.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                12));

                // =====================================================
                // TITLE
                // =====================================================

                JLabel title = new JLabel(
                                "TRANG CHU",
                                SwingConstants.CENTER);

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                25));

                title.setForeground(
                                NAVY);

                panel.add(
                                date,
                                BorderLayout.WEST);

                panel.add(
                                title,
                                BorderLayout.CENTER);

                return panel;
        }

        // =========================================================
        // ANH GIOI THIEU
        // =========================================================

        private JPanel createIntroduction() {

                JPanel panel = new JPanel(
                                new BorderLayout());

                panel.setBackground(
                                Color.WHITE);

                try {

                        ImageIcon icon = new ImageIcon(
                                        getClass().getResource(
                                                        "/img/giothieutrangchu.png"));

                        Image image = icon.getImage();

                        Image scaledImage = image.getScaledInstance(
                                        550,
                                        510,
                                        Image.SCALE_SMOOTH);

                        JLabel imageLabel = new JLabel(
                                        new ImageIcon(
                                                        scaledImage));

                        imageLabel.setHorizontalAlignment(
                                        SwingConstants.CENTER);

                        imageLabel.setVerticalAlignment(
                                        SwingConstants.CENTER);

                        panel.add(
                                        imageLabel,
                                        BorderLayout.CENTER);

                } catch (Exception e) {

                        JLabel error = new JLabel(
                                        "Khong tim thay anh gioi thieu",
                                        SwingConstants.CENTER);

                        error.setForeground(
                                        RED);

                        error.setFont(
                                        new Font(
                                                        "Segoe UI",
                                                        Font.BOLD,
                                                        14));

                        panel.add(
                                        error,
                                        BorderLayout.CENTER);
                }

                return panel;
        }

        // =========================================================
        // TRUY CAP NHANH
        // =========================================================

        private JPanel createQuickAccess() {

                JPanel panel = new JPanel();

                panel.setBackground(
                                Color.WHITE);

                panel.setBorder(
                                new EmptyBorder(
                                                15,
                                                15,
                                                15,
                                                15));

                panel.setLayout(
                                new BoxLayout(
                                                panel,
                                                BoxLayout.Y_AXIS));

                // =====================================================
                // TITLE
                // =====================================================

                JLabel title = new JLabel(
                                "TRUY CAP NHANH");

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                18));

                title.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                panel.add(title);

                panel.add(
                                Box.createVerticalStrut(15));

                // =====================================================
                // BUTTON
                // =====================================================

                addQuickButton(
                                panel,
                                "Thanh toan",
                                "F4",
                                this::hienQuanLyThanhToan);

                addQuickButton(
                                panel,
                                "Quan ly lich hen Spa",
                                "F5",
                                this::hienQuanLyLichHen);

                addQuickButton(
                                panel,
                                "Quan ly san pham",
                                "F6",
                                () -> hienKho(1));

                addQuickButton(
                                panel,
                                "Quan ly khach hang",
                                "F7",
                                () -> hienChucNangChuaCoGiaoDien("Quan ly khach hang"));

                addQuickButton(
                                panel,
                                "Bao cao doanh thu",
                                "F8",
                                () -> hienChucNangChuaCoGiaoDien("Bao cao doanh thu"));

                return panel;
        }

        // =========================================================
        // QUICK BUTTON
        // =========================================================

        private void addQuickButton(
                        JPanel parent,
                        String text,
                        String shortcut,
                        Runnable action) {

                JPanel row = new JPanel(
                                new BorderLayout());

                row.setBackground(
                                MENU_BLUE);

                row.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                45));

                JButton button = new JButton(text);

                button.setHorizontalAlignment(
                                SwingConstants.LEFT);

                button.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                14));

                button.setBackground(
                                MENU_BLUE);

                button.setFocusPainted(false);

                button.setBorderPainted(false);

                button.addActionListener(
                                e -> action.run());

                String actionKey = "quick-access-" + shortcut;
                getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                                .put(KeyStroke.getKeyStroke(shortcut), actionKey);
                getRootPane().getActionMap().put(actionKey, new AbstractAction() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                                action.run();
                        }
                });

                JLabel key = new JLabel(shortcut);

                key.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                14));

                key.setBorder(
                                new EmptyBorder(
                                                0,
                                                0,
                                                0,
                                                10));

                row.add(
                                button,
                                BorderLayout.CENTER);

                row.add(
                                key,
                                BorderLayout.EAST);

                parent.add(row);

                parent.add(
                                Box.createVerticalStrut(5));
        }

        // =========================================================
        // POPUP KHO
        // =========================================================

        private void hienPopupKho(
                        JButton source) {

                JPopupMenu popup = new JPopupMenu();

                themMenuKho(popup, "Danh muc", 0);
                themMenuKho(popup, "San pham", 1);
                themMenuKho(popup, "Nhap kho", 2);
                themMenuKho(popup, "Kiem ke", 3);

                popup.show(source, 0, source.getHeight());
        }

        // =========================================================
        // POPUP SPA
        // =========================================================

        private void hienPopupSpa(JButton source) {
                JPopupMenu popup = new JPopupMenu();

                // 1. Nút Lịch hẹn
                JMenuItem mnuLichHen = new JMenuItem("Lich hen");
                mnuLichHen.addActionListener(e -> hienQuanLyLichHen());
                popup.add(mnuLichHen);

                // 2. Nút Thú cưng
                JMenuItem mnuThuCung = new JMenuItem("Thu cung");
                mnuThuCung.addActionListener(e -> hienQuanLyThuCung());
                popup.add(mnuThuCung);

                // 3. Khach hang chua co giao dien rieng
                themMenuItem(popup, "Khach hang");

                // 4. Nút Phiếu dịch vụ (ĐÃ KẾT NỐI)
                JMenuItem mnuPhieuDV = new JMenuItem("Phieu dich vu");
                mnuPhieuDV.addActionListener(e -> hienQuanLyPhieuDichVu());
                popup.add(mnuPhieuDV);

                popup.show(source, 0, source.getHeight());
        }
        // =========================================================
        // POPUP HOA DON
        // =========================================================

        private void hienPopupHoaDon(
                        JButton source) {

                JPopupMenu popup = new JPopupMenu();

                JMenuItem hoaDon = new JMenuItem("Hoa don");
                hoaDon.addActionListener(e -> hienTraCuuHoaDon());
                popup.add(hoaDon);

                JMenuItem thanhToan = new JMenuItem("Thanh toan");
                thanhToan.addActionListener(e -> hienQuanLyThanhToan());
                popup.add(thanhToan);

                popup.show(
                                source,
                                0,
                                source.getHeight());
        }

        // =========================================================
        // POPUP BAO CAO
        // =========================================================

        private void hienPopupBaoCao(
                        JButton source) {

                JPopupMenu popup = new JPopupMenu();

                themMenuItem(
                                popup,
                                "Theo ngay");

                themMenuItem(
                                popup,
                                "Theo thang");

                themMenuItem(
                                popup,
                                "Theo nam");

                popup.show(
                                source,
                                0,
                                source.getHeight());
        }

        // =========================================================
        // POPUP HE THONG
        // =========================================================

        private void hienPopupHeThong(
                        JButton source) {

                JPopupMenu popup = new JPopupMenu();

                // =====================================================
                // QUAN LY TAI KHOAN
                // =====================================================

                JMenuItem taiKhoan = new JMenuItem(
                                "Quan ly tai khoan");

                taiKhoan.addActionListener(
                                e -> hienQuanLyTaiKhoan());

                popup.add(taiKhoan);

                // =====================================================
                // QUAN LY PHAN CA
                // =====================================================
                JMenuItem phanCa = new JMenuItem("Quan ly phan ca");

                phanCa.addActionListener(
                                e -> hienQuanLyPhanCa());

                popup.add(phanCa);
                // =====================================================
                // SEPARATOR
                // =====================================================

                popup.addSeparator();

                // =====================================================
                // QUAN LY CA LAM
                // =====================================================

                JMenuItem caLam = new JMenuItem(
                                "Quan ly ca lam");

                caLam.addActionListener(
                                e -> hienQuanLyCaLam());

                popup.add(caLam);

                // =====================================================
                // HIEN POPUP
                // =====================================================

                popup.show(
                                source,
                                0,
                                source.getHeight());
        }

        // =========================================================
        // MENU ITEM CHUNG
        // =========================================================

        private void themMenuItem(
                        JPopupMenu popup,
                        String text) {

                JMenuItem item = new JMenuItem(text);

                item.addActionListener(
                                e -> hienChucNangChuaCoGiaoDien(text));

                popup.add(item);
        }

        private void themMenuKho(JPopupMenu popup, String text, int tab) {
                JMenuItem item = new JMenuItem(text);
                item.addActionListener(e -> hienKho(tab));
                popup.add(item);
        }

        private JPanel taoManHinhKho() {
                JPanel panel = new JPanel(new BorderLayout());

                JPanel thanhTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
                thanhTab.setBackground(Color.WHITE);

                String[] tenTab = {"Danh muc", "San pham", "Nhap kho", "Kiem ke"};
                for (int i = 0; i < tenTab.length; i++) {
                        final int tab = i;
                        nutTabKho[i] = UIStyle.nutTab(tenTab[i], false);
                        nutTabKho[i].addActionListener(e -> hienKho(tab));
                        thanhTab.add(nutTabKho[i]);
                }

                khoCardLayout = new CardLayout();
                khoContentPanel = new JPanel(khoCardLayout);
                khoContentPanel.setBackground(LIGHT_BLUE);

                panel.add(thanhTab, BorderLayout.NORTH);
                panel.add(khoContentPanel, BorderLayout.CENTER);
                return panel;
        }

        private void hienKho(int tab) {
                if (tab < 0 || tab >= manHinhKho.length) {
                        tab = 0;
                }

                boolean lanDau = manHinhKho[tab] == null;
                if (lanDau) {
                        manHinhKho[tab] = taoManHinhKho(tab);
                        khoContentPanel.add((JPanel) manHinhKho[tab], "KHO_" + tab);
                }

                khoCardLayout.show(khoContentPanel, "KHO_" + tab);
                if (!lanDau) {
                        manHinhKho[tab].napLai();
                }

                for (int i = 0; i < nutTabKho.length; i++) {
                        boolean dangChon = i == tab;
                        nutTabKho[i].setBackground(
                                        dangChon ? UIStyle.ACTIVE_BLUE : Color.WHITE);
                        nutTabKho[i].setForeground(
                                        dangChon ? Color.WHITE : UIStyle.NAVY);
                }

                cardLayout.show(contentPanel, "KHO");
                khoContentPanel.revalidate();
                khoContentPanel.repaint();
        }

        private ManHinhKho taoManHinhKho(int tab) {
                switch (tab) {
                        case 0:
                                return new DanhMucPanel();
                        case 1:
                                return new SanPhamPanel();
                        case 2:
                                return new NhapKhoPanel();
                        default:
                                return new KiemKePanel();
                }
        }

        // =========================================================
        // THONG TIN NHAN VIEN
        // =========================================================

        private void hienThongTinNhanVien(
                        JButton source) {

                JPopupMenu popup = new JPopupMenu();

                JPanel info = new JPanel(
                                new GridLayout(
                                                0,
                                                2,
                                                15,
                                                8));

                info.setBorder(
                                new EmptyBorder(
                                                12,
                                                12,
                                                12,
                                                12));

                themThongTin(
                                info,
                                "Ma NV",
                                maNV);

                themThongTin(
                                info,
                                "Ho ten",
                                hoTen);

                themThongTin(
                                info,
                                "Vai tro",
                                vaiTro);

                popup.add(info);

                popup.addSeparator();

                // =====================================================
                // DANG XUAT
                // =====================================================

                JMenuItem dangXuat = new JMenuItem(
                                "Dang xuat");

                dangXuat.addActionListener(
                                e -> {

                                        dispose();

                                        new DangNhap()
                                                        .setVisible(true);
                                });

                popup.add(
                                dangXuat);

                popup.show(
                                source,
                                -popup.getPreferredSize().width
                                                + source.getWidth(),
                                source.getHeight());
        }

        // =========================================================
        // THEM THONG TIN
        // =========================================================

        private void themThongTin(
                        JPanel panel,
                        String label,
                        String value) {

                JLabel lb = new JLabel(label);

                lb.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                12));

                JLabel val = new JLabel(value);

                panel.add(lb);
                panel.add(val);
        }

        // =========================================================
        // THONG BAO
        // =========================================================

        private void hienThongBao(
                        String tenChucNang) {

                JOptionPane.showMessageDialog(
                                this,
                                "Ban dang mo chuc nang: "
                                                + tenChucNang,
                                "Pet Shop Pro",
                                JOptionPane.INFORMATION_MESSAGE);
        }

        private void hienChucNangChuaCoGiaoDien(String tenChucNang) {
                JOptionPane.showMessageDialog(
                                this,
                                "Chuc nang \"" + tenChucNang + "\" chua co giao dien.",
                                "Pet Shop Pro",
                                JOptionPane.INFORMATION_MESSAGE);
        }

        private void hienQuanLyLichHen() {
                cardLayout.show(contentPanel, "LICH_HEN");
        }

        private void hienQuanLyThuCung() {
                cardLayout.show(contentPanel, "THU_CUNG");
        }

        private void hienQuanLyPhieuDichVu() {
                cardLayout.show(contentPanel, "PHIEU_DICH_VU");
        }

        private void hienTraCuuHoaDon() {
                if (traCuuHoaDon == null) {
                        traCuuHoaDon = new TraCuuHoaDon();
                        contentPanel.add(traCuuHoaDon, "TRA_CUU_HOA_DON");
                }
                cardLayout.show(contentPanel, "TRA_CUU_HOA_DON");
                traCuuHoaDon.hienDanhSach();
        }

        private void hienQuanLyThanhToan() {
                if (quanLyThanhToan == null) {
                        quanLyThanhToan = new QuanLyThanhToan(maNV);
                        contentPanel.add(quanLyThanhToan, "THANH_TOAN");
                }
                cardLayout.show(contentPanel, "THANH_TOAN");
        }
}
