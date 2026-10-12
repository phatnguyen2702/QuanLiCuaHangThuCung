
package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TrangChu extends JFrame {

        // ==================== MAU GIAO DIEN ====================

        private final Color NAVY = new Color(18, 48, 82);
        private final Color LIGHT_BLUE = new Color(229, 241, 252);
        private final Color MENU_BLUE = new Color(207, 222, 235);
        private final Color BG = new Color(245, 247, 250);
        private final Color RED = new Color(210, 70, 70);

        // ==================== THONG TIN NHAN VIEN ====================

        private final String maNV;
        private final String hoTen;
        private final String vaiTro;

        // ==================== QUAN LY MAN HINH ====================

        private CardLayout cardLayout;
        private JPanel contentPanel;

        private QuanLyTaiKhoan quanLyTaiKhoan;
        private QuanLyCaLam quanLyCaLam;
        private QuanLyPhanCa quanLyPhanCa;
        private QuanLyLichHen quanLyLichHen;
        private QuanLyThuCung quanLyThuCung;
        private QuanLyPhieuDichVu quanLyPhieuDichVu;

        private JPanel danhMucPanel;
        private JPanel sanPhamPanel;
        private JPanel nhapKhoPanel;
        private JPanel kiemKePanel;

        private JPanel traCuuHoaDonPanel;
        private JPanel thanhToanPanel;

        // ==================== KHOI TAO FRAME ====================

        public TrangChu(String maNV, String hoTen, String vaiTro) {
                this.maNV = maNV;
                this.hoTen = hoTen;
                this.vaiTro = vaiTro;

                setTitle("Pet Shop Pro - Trang chủ");
                setSize(1200, 720);
                setMinimumSize(new Dimension(1000, 620));
                setLocationRelativeTo(null);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setLayout(new BorderLayout());

                add(createHeader(), BorderLayout.NORTH);

                cardLayout = new CardLayout();
                contentPanel = new JPanel(cardLayout);
                contentPanel.setBackground(BG);

                // Dang ky cac man hinh vao CardLayout
                contentPanel.add(createMainContent(), "TRANG_CHU");

                quanLyTaiKhoan = new QuanLyTaiKhoan();
                contentPanel.add(quanLyTaiKhoan, "TAI_KHOAN");

                quanLyCaLam = new QuanLyCaLam();
                contentPanel.add(quanLyCaLam, "CA_LAM");

                quanLyPhanCa = new QuanLyPhanCa();
                contentPanel.add(quanLyPhanCa, "PHAN_CA");

                quanLyLichHen = new QuanLyLichHen();
                contentPanel.add(quanLyLichHen, "LICH_HEN");

                quanLyThuCung = new QuanLyThuCung();
                contentPanel.add(quanLyThuCung, "THU_CUNG");

                quanLyPhieuDichVu = new QuanLyPhieuDichVu();
                contentPanel.add(quanLyPhieuDichVu, "PHIEU_DICH_VU");

                danhMucPanel = new DanhMucPanel();
                contentPanel.add(danhMucPanel, "DANH_MUC");

                sanPhamPanel = new SanPhamPanel();
                contentPanel.add(sanPhamPanel, "SAN_PHAM");

                nhapKhoPanel = new NhapKhoPanel();
                contentPanel.add(nhapKhoPanel, "NHAP_KHO");

                kiemKePanel = new KiemKePanel();
                contentPanel.add(kiemKePanel, "KIEM_KE");

                add(contentPanel, BorderLayout.CENTER);
                hienTrangChu();
        }

        // ==================== HEADER MENU ====================

        private JPanel createHeader() {
                JPanel header = new JPanel(new BorderLayout());
                header.setBackground(NAVY);
                header.setBorder(new EmptyBorder(5, 18, 5, 18));

                JPanel menu = new JPanel(
                                new FlowLayout(FlowLayout.LEFT, 5, 8));
                menu.setOpaque(false);

                JLabel logo = new JLabel("PET SHOP PRO");
                logo.setForeground(Color.WHITE);
                logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
                logo.setBorder(new EmptyBorder(0, 5, 0, 10));
                menu.add(logo);

                menu.add(createMenuButton(
                                "Trang chủ", e -> hienTrangChu()));

                menu.add(createMenuButton(
                                "Kho & Danh mục",
                                e -> hienPopupKho((JButton) e.getSource())));

                menu.add(createMenuButton(
                                "Spa & Lịch hẹn",
                                e -> hienPopupSpa((JButton) e.getSource())));

                menu.add(createMenuButton(
                                "Hóa đơn",
                                e -> hienPopupHoaDon((JButton) e.getSource())));

                menu.add(createMenuButton(
                                "Báo cáo",
                                e -> hienPopupBaoCao((JButton) e.getSource())));

                menu.add(createMenuButton(
                                "Hệ thống",
                                e -> hienPopupHeThong((JButton) e.getSource())));

                menu.add(createMenuButton(
                                "Cài đặt",
                                e -> hienThongBao("Cài đặt")));

                header.add(menu, BorderLayout.WEST);

                JButton btnNhanVien = createMenuButton(
                                hoTen + "  ●",
                                e -> hienThongTinNhanVien(
                                                (JButton) e.getSource()));

                header.add(btnNhanVien, BorderLayout.EAST);
                return header;
        }

        private JButton createMenuButton(
                        String text,
                        java.awt.event.ActionListener action) {

                JButton button = new JButton(text);
                button.setForeground(Color.WHITE);
                button.setBackground(NAVY);
                button.setFont(new Font("Segoe UI", Font.BOLD, 12));
                button.setFocusPainted(false);
                button.setBorderPainted(false);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                button.addActionListener(action);

                return button;
        }

        // ==================== TRANG CHU ====================

        private void hienTrangChu() {
                cardLayout.show(contentPanel, "TRANG_CHU");
        }

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

        private JPanel createTitleBar() {
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(new Color(247, 249, 251));
                panel.setBorder(new EmptyBorder(10, 25, 10, 25));

                JLabel date = new JLabel(
                                "▣  " + LocalDateTime.now().format(
                                                DateTimeFormatter.ofPattern(
                                                                "HH:mm:ss | dd/MM/yyyy")));

                date.setFont(new Font("Segoe UI", Font.BOLD, 12));

                JLabel title = new JLabel(
                                "TRANG CHỦ", SwingConstants.CENTER);
                title.setFont(new Font("Segoe UI", Font.BOLD, 25));
                title.setForeground(NAVY);

                panel.add(date, BorderLayout.WEST);
                panel.add(title, BorderLayout.CENTER);

                return panel;
        }

        // ==================== ANH GIOI THIEU ====================

        private JPanel createIntroduction() {
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(Color.WHITE);

                java.net.URL imageUrl = getClass().getResource("/img/giothieutrangchu.png");

                if (imageUrl == null) {
                        JLabel error = new JLabel(
                                        "Không tìm thấy ảnh giới thiệu",
                                        SwingConstants.CENTER);
                        error.setForeground(RED);
                        error.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        panel.add(error, BorderLayout.CENTER);
                        return panel;
                }

                Image originalImage = new ImageIcon(imageUrl).getImage();
                JLabel imageLabel = new JLabel();
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                imageLabel.setVerticalAlignment(SwingConstants.CENTER);

                Runnable scaleImage = () -> {
                        int availableWidth = Math.max(1, panel.getWidth() - 16);
                        int availableHeight = Math.max(1, panel.getHeight() - 16);

                        int originalWidth = Math.max(
                                        1, originalImage.getWidth(panel));
                        int originalHeight = Math.max(
                                        1, originalImage.getHeight(panel));

                        double scale = Math.min(
                                        (double) availableWidth / originalWidth,
                                        (double) availableHeight / originalHeight);

                        int width = Math.max(
                                        1, (int) (originalWidth * scale));
                        int height = Math.max(
                                        1, (int) (originalHeight * scale));

                        imageLabel.setIcon(new ImageIcon(
                                        originalImage.getScaledInstance(
                                                        width, height, Image.SCALE_SMOOTH)));
                };

                panel.add(imageLabel, BorderLayout.CENTER);

                panel.addComponentListener(
                                new java.awt.event.ComponentAdapter() {
                                        @Override
                                        public void componentResized(
                                                        java.awt.event.ComponentEvent e) {
                                                scaleImage.run();
                                        }
                                });

                SwingUtilities.invokeLater(scaleImage);
                return panel;
        }

        // ==================== TRUY CAP NHANH ====================

        private JPanel createQuickAccess() {
                JPanel panel = new JPanel();
                panel.setBackground(Color.WHITE);
                panel.setBorder(new EmptyBorder(15, 15, 15, 15));
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                JLabel title = new JLabel("TRUY CẬP NHANH");
                title.setFont(new Font("Segoe UI", Font.BOLD, 18));
                title.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(title);
                panel.add(Box.createVerticalStrut(15));

                addQuickButton(panel, "Thanh toán", "F4");
                addQuickButton(panel, "Quản lý lịch hẹn Spa", "F5");
                addQuickButton(panel, "Quản lý sản phẩm", "F6");
                addQuickButton(panel, "Quản lý khách hàng", "F7");
                addQuickButton(panel, "Báo cáo doanh thu", "F8");

                return panel;
        }

        private void addQuickButton(
                        JPanel parent, String text, String shortcut) {

                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(MENU_BLUE);
                row.setMaximumSize(
                                new Dimension(Integer.MAX_VALUE, 45));

                JButton button = new JButton(text);
                button.setHorizontalAlignment(SwingConstants.LEFT);
                button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                button.setBackground(MENU_BLUE);
                button.setFocusPainted(false);
                button.setBorderPainted(false);

                button.addActionListener(e -> {
                        switch (text) {
                                case "Thanh toán":
                                        hienQuanLyThanhToan();
                                        break;
                                case "Quản lý lịch hẹn Spa":
                                        hienQuanLyLichHen();
                                        break;
                                case "Quản lý sản phẩm":
                                        hienSanPham();
                                        break;
                                case "Quản lý khách hàng":
                                        hienThongBao("Quản lý khách hàng");
                                        break;
                                case "Báo cáo doanh thu":
                                        hienThongBao("Báo cáo doanh thu");
                                        break;
                                default:
                                        hienThongBao(text);
                        }
                });

                JLabel key = new JLabel(shortcut);
                key.setFont(new Font("Segoe UI", Font.BOLD, 14));
                key.setBorder(new EmptyBorder(0, 0, 0, 10));

                row.add(button, BorderLayout.CENTER);
                row.add(key, BorderLayout.EAST);

                parent.add(row);
                parent.add(Box.createVerticalStrut(5));
        }

        // ==================== CAC MAN HINH KHO ====================

        private void hienDanhMuc() {
                cardLayout.show(contentPanel, "DANH_MUC");
        }

        private void hienSanPham() {
                cardLayout.show(contentPanel, "SAN_PHAM");
        }

        private void hienNhapKho() {
                cardLayout.show(contentPanel, "NHAP_KHO");
        }

        private void hienKiemKe() {
                cardLayout.show(contentPanel, "KIEM_KE");
        }

        private void hienPopupKho(JButton source) {
                JPopupMenu popup = new JPopupMenu();

                JMenuItem danhMuc = new JMenuItem("Quản lý danh mục");
                danhMuc.addActionListener(e -> hienDanhMuc());
                popup.add(danhMuc);

                JMenuItem sanPham = new JMenuItem("Quản lý sản phẩm");
                sanPham.addActionListener(e -> hienSanPham());
                popup.add(sanPham);

                JMenuItem nhapKho = new JMenuItem("Nhập kho");
                nhapKho.addActionListener(e -> hienNhapKho());
                popup.add(nhapKho);

                JMenuItem kiemKe = new JMenuItem("Kiểm kê kho");
                kiemKe.addActionListener(e -> hienKiemKe());
                popup.add(kiemKe);

                popup.show(source, 0, source.getHeight());
        }

        // ==================== CAC MAN HINH SPA ====================

        private void hienQuanLyLichHen() {
                cardLayout.show(contentPanel, "LICH_HEN");
        }

        private void hienQuanLyThuCung() {
                cardLayout.show(contentPanel, "THU_CUNG");
        }

        private void hienQuanLyPhieuDichVu() {
                cardLayout.show(contentPanel, "PHIEU_DICH_VU");
        }

        private void hienPopupSpa(JButton source) {
                JPopupMenu popup = new JPopupMenu();

                JMenuItem lichHen = new JMenuItem("Quản lý lịch hẹn");
                lichHen.addActionListener(e -> hienQuanLyLichHen());
                popup.add(lichHen);

                JMenuItem thuCung = new JMenuItem("Quản lý thú cưng");
                thuCung.addActionListener(e -> hienQuanLyThuCung());
                popup.add(thuCung);

                JMenuItem khachHang = new JMenuItem("Quản lý khách hàng");
                khachHang.addActionListener(
                                e -> hienThongBao("Quản lý khách hàng"));
                popup.add(khachHang);

                JMenuItem phieuDichVu = new JMenuItem("Quản lý phiếu dịch vụ");
                phieuDichVu.addActionListener(
                                e -> hienQuanLyPhieuDichVu());
                popup.add(phieuDichVu);

                popup.show(source, 0, source.getHeight());
        }

        // ==================== HOA DON VA THANH TOAN ====================

        private void hienPopupHoaDon(JButton source) {
                JPopupMenu popup = new JPopupMenu();

                JMenuItem traCuu = new JMenuItem("Tra cứu hóa đơn");
                traCuu.addActionListener(e -> hienTraCuuHoaDon());
                popup.add(traCuu);

                JMenuItem thanhToan = new JMenuItem("Thanh toán");
                thanhToan.addActionListener(e -> hienQuanLyThanhToan());
                popup.add(thanhToan);

                popup.show(source, 0, source.getHeight());
        }

        private void hienTraCuuHoaDon() {
                if (traCuuHoaDonPanel == null) {
                        traCuuHoaDonPanel = new TraCuuHoaDon();
                        contentPanel.add(
                                        traCuuHoaDonPanel, "TRA_CUU_HOA_DON");
                }

                cardLayout.show(contentPanel, "TRA_CUU_HOA_DON");
        }

        private void hienQuanLyThanhToan() {
                if (thanhToanPanel == null) {
                        thanhToanPanel = new QuanLyThanhToan(maNV);
                        contentPanel.add(thanhToanPanel, "THANH_TOAN");
                }

                cardLayout.show(contentPanel, "THANH_TOAN");
        }

        // ==================== BAO CAO ====================

        private void hienPopupBaoCao(JButton source) {
                JPopupMenu popup = new JPopupMenu();

                themMenuItem(popup, "Báo cáo theo ngày");
                themMenuItem(popup, "Báo cáo theo tháng");
                themMenuItem(popup, "Báo cáo theo năm");

                popup.show(source, 0, source.getHeight());
        }

        // ==================== HE THONG ====================

        private void hienQuanLyTaiKhoan() {
                cardLayout.show(contentPanel, "TAI_KHOAN");
        }

        private void hienQuanLyCaLam() {
                cardLayout.show(contentPanel, "CA_LAM");
        }

        private void hienQuanLyPhanCa() {
                cardLayout.show(contentPanel, "PHAN_CA");
        }

        private void hienPopupHeThong(JButton source) {
                JPopupMenu popup = new JPopupMenu();

                JMenuItem taiKhoan = new JMenuItem("Quản lý tài khoản");
                taiKhoan.addActionListener(e -> hienQuanLyTaiKhoan());
                popup.add(taiKhoan);

                JMenuItem phanCa = new JMenuItem("Quản lý phân ca");
                phanCa.addActionListener(e -> hienQuanLyPhanCa());
                popup.add(phanCa);

                popup.addSeparator();

                JMenuItem caLam = new JMenuItem("Quản lý ca làm");
                caLam.addActionListener(e -> hienQuanLyCaLam());
                popup.add(caLam);

                popup.show(source, 0, source.getHeight());
        }

        // ==================== MENU ITEM CHUNG ====================

        private void themMenuItem(JPopupMenu popup, String text) {
                JMenuItem item = new JMenuItem(text);
                item.addActionListener(e -> hienThongBao(text));
                popup.add(item);
        }

        // ==================== THONG TIN NHAN VIEN ====================

        private void hienThongTinNhanVien(JButton source) {
                JPopupMenu popup = new JPopupMenu();

                JPanel info = new JPanel(new GridLayout(0, 2, 15, 8));
                info.setBorder(new EmptyBorder(12, 12, 12, 12));

                themThongTin(info, "Mã nhân viên", maNV);
                themThongTin(info, "Họ tên", hoTen);
                themThongTin(info, "Vai trò", vaiTro);

                popup.add(info);
                popup.addSeparator();

                JMenuItem dangXuat = new JMenuItem("Đăng xuất");
                dangXuat.addActionListener(e -> {
                        dispose();
                        new DangNhap().setVisible(true);
                });

                popup.add(dangXuat);

                popup.show(
                                source,
                                -popup.getPreferredSize().width + source.getWidth(),
                                source.getHeight());
        }

        private void themThongTin(
                        JPanel panel, String nhan, String giaTri) {

                JLabel lb = new JLabel(nhan);
                lb.setFont(new Font("Segoe UI", Font.BOLD, 12));

                JLabel val = new JLabel(
                                giaTri == null ? "" : giaTri);

                panel.add(lb);
                panel.add(val);
        }

        // ==================== THONG BAO ====================

        private void hienThongBao(String tenChucNang) {
                JOptionPane.showMessageDialog(
                                this,
                                "Bạn đang mở chức năng: " + tenChucNang,
                                "Pet Shop Pro",
                                JOptionPane.INFORMATION_MESSAGE);
        }

        // ==================== CHAY UNG DUNG ====================

        public static void main(String[] args) {
                SwingUtilities.invokeLater(() -> {
                        TrangChu frame = new TrangChu(
                                        "NV001", "Nhân viên demo", "Quản lý");
                        frame.setVisible(true);
                });
        }
}