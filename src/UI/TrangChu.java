package UI;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class TrangChu extends JFrame {

        private final Color NAVY = new Color(18, 48, 82);
        private final Color LIGHT_BLUE = new Color(229, 241, 252);
        private final Color MENU_BLUE = new Color(207, 222, 235);
        private final Color BG = new Color(245, 247, 250);
        private final Color RED = new Color(210, 70, 70);

        private String maNV;
        private String hoTen;
        private String vaiTro;

        private CardLayout cardLayout;
        private JPanel contentPanel;

        // Các màn hình chức năng
        private QuanLyTaiKhoan quanLyTaiKhoan;
        private QuanLyCaLam quanLyCaLam;
        private QuanLyPhanCa quanLyPhanCa;
        private final QuanLyThanhToan thanhToan;
        private final TraCuuHoaDon traCuuHoaDon;

        // Các màn hình Kho & Danh mục mới kết nối
        private DanhMucPanel danhMucPanel;
        private SanPhamPanel sanPhamPanel;
        private NhapKhoPanel nhapKhoPanel;
        private KiemKePanel kiemKePanel;

        public TrangChu(String maNV, String hoTen, String vaiTro) {
                this.maNV = maNV;
                this.hoTen = hoTen;
                this.vaiTro = vaiTro;

                setTitle("Pet Shop Pro - Trang chủ");
                setSize(1200, 720);
                setLocationRelativeTo(null);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setResizable(false);
                setLayout(new BorderLayout());

                add(createHeader(), BorderLayout.NORTH);

                cardLayout = new CardLayout();
                contentPanel = new JPanel(cardLayout);
                contentPanel.setBackground(BG);

                // Thêm các màn hình vào CardLayout
                contentPanel.add(createMainContent(), "TRANG_CHU");

                quanLyTaiKhoan = new QuanLyTaiKhoan();
                contentPanel.add(quanLyTaiKhoan, "TAI_KHOAN");

                quanLyCaLam = new QuanLyCaLam();
                contentPanel.add(quanLyCaLam, "CA_LAM");

                quanLyPhanCa = new QuanLyPhanCa();
                contentPanel.add(quanLyPhanCa, "PHAN_CA");

                thanhToan = new QuanLyThanhToan(maNV);
                contentPanel.add(thanhToan, "THANH_TOAN");

                traCuuHoaDon = new TraCuuHoaDon();
                contentPanel.add(traCuuHoaDon, "TRA_CUU_HOA_DON");

                // Khởi tạo và thêm các màn hình Kho vào CardLayout
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

        private JPanel createHeader() {
                JPanel header = new JPanel(new BorderLayout());
                header.setBackground(NAVY);
                header.setBorder(new EmptyBorder(5, 18, 5, 18));

                JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 8));
                menu.setOpaque(false);

                JLabel logo = new JLabel("PET SHOP PRO");
                logo.setForeground(Color.WHITE);
                logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
                logo.setBorder(new EmptyBorder(0, 5, 0, 10));
                menu.add(logo);

                menu.add(createMenuButton("Trang chủ", e -> hienTrangChu()));
                menu.add(createMenuButton("Kho & Danh mục", e -> hienPopupKho((JButton) e.getSource())));
                menu.add(createMenuButton("Spa & Lịch hẹn", e -> hienPopupSpa((JButton) e.getSource())));
                menu.add(createMenuButton("Hóa đơn", e -> hienPopupHoaDon((JButton) e.getSource())));
                menu.add(createMenuButton("Báo cáo", e -> hienPopupBaoCao((JButton) e.getSource())));
                menu.add(createMenuButton("Hệ thống", e -> hienPopupHeThong((JButton) e.getSource())));
                menu.add(createMenuButton("Cài đặt", e -> hienThongBao("Cài đặt")));

                header.add(menu, BorderLayout.WEST);

                JButton btnNhanVien = createMenuButton(hoTen + "  ●",
                                e -> hienThongTinNhanVien((JButton) e.getSource()));
                header.add(btnNhanVien, BorderLayout.EAST);

                return header;
        }

        private JButton createMenuButton(String text, java.awt.event.ActionListener action) {
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

        private void hienTrangChu() {
                cardLayout.show(contentPanel, "TRANG_CHU");
        }

        private void hienQuanLyTaiKhoan() {
                cardLayout.show(contentPanel, "TAI_KHOAN");
        }

        private void hienQuanLyCaLam() {
                cardLayout.show(contentPanel, "CA_LAM");
        }

        private void hienQuanLyPhanCa() {
                cardLayout.show(contentPanel, "PHAN_CA");
        }

        private void hienThanhToan() {
                cardLayout.show(contentPanel, "THANH_TOAN");
        }

        private void hienTraCuuHoaDon() {
                traCuuHoaDon.hienDanhSach();
                cardLayout.show(contentPanel, "TRA_CUU_HOA_DON");
        }

        // Các hàm chuyển đổi màn hình Kho
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

                JLabel date = new JLabel("▣  "
                                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss | dd/MM/yyyy")));
                date.setFont(new Font("Segoe UI", Font.BOLD, 12));

                JLabel title = new JLabel("TRANG CHỦ", SwingConstants.CENTER);
                title.setFont(new Font("Segoe UI", Font.BOLD, 25));
                title.setForeground(NAVY);

                panel.add(date, BorderLayout.WEST);
                panel.add(title, BorderLayout.CENTER);
                return panel;
        }

        private JPanel createIntroduction() {
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(Color.WHITE);

                try {
                        ImageIcon icon = new ImageIcon(getClass().getResource("/img/giothieutrangchu.png"));
                        Image image = icon.getImage();
                        Image scaledImage = image.getScaledInstance(550, 510, Image.SCALE_SMOOTH);

                        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
                        panel.add(imageLabel, BorderLayout.CENTER);
                } catch (Exception e) {
                        JLabel error = new JLabel("Không tìm thấy ảnh giới thiệu", SwingConstants.CENTER);
                        error.setForeground(RED);
                        error.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        panel.add(error, BorderLayout.CENTER);
                }

                return panel;
        }

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

        private void addQuickButton(JPanel parent, String text, String shortcut) {
                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(MENU_BLUE);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

                JButton button = new JButton(text);
                button.setHorizontalAlignment(SwingConstants.LEFT);
                button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                button.setBackground(MENU_BLUE);
                button.setFocusPainted(false);
                button.setBorderPainted(false);

                button.addActionListener(e -> {
                        if ("Thanh toán".equals(text) || "Thanh toan".equals(text)) {
                                hienThanhToan();
                        } else if ("Quản lý sản phẩm".equals(text) || "Quan ly san pham".equals(text)) {
                                hienSanPham();
                        } else {
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

        private void hienPopupKho(JButton source) {
                JPopupMenu popup = new JPopupMenu();

                JMenuItem itemDanhMuc = new JMenuItem("Danh mục");
                itemDanhMuc.addActionListener(e -> hienDanhMuc());
                popup.add(itemDanhMuc);

                JMenuItem itemSanPham = new JMenuItem("Sản phẩm");
                itemSanPham.addActionListener(e -> hienSanPham());
                popup.add(itemSanPham);

                JMenuItem itemNhapKho = new JMenuItem("Nhập kho");
                itemNhapKho.addActionListener(e -> hienNhapKho());
                popup.add(itemNhapKho);

                JMenuItem itemKiemKe = new JMenuItem("Kiểm kê");
                itemKiemKe.addActionListener(e -> hienKiemKe());
                popup.add(itemKiemKe);

                popup.show(source, 0, source.getHeight());
        }

        private void hienPopupSpa(JButton source) {
                JPopupMenu popup = new JPopupMenu();
                themMenuItem(popup, "Lịch hẹn");
                themMenuItem(popup, "Thú cưng");
                themMenuItem(popup, "Khách hàng");
                themMenuItem(popup, "Phiếu dịch vụ");
                popup.show(source, 0, source.getHeight());
        }

        private void hienPopupHoaDon(JButton source) {
                JPopupMenu popup = new JPopupMenu();
                JMenuItem traCuuItem = new JMenuItem("Tra cứu hóa đơn");
                traCuuItem.addActionListener(e -> hienTraCuuHoaDon());
                popup.add(traCuuItem);

                JMenuItem thanhToanItem = new JMenuItem("Thanh toán");
                thanhToanItem.addActionListener(e -> hienThanhToan());
                popup.add(thanhToanItem);
                popup.show(source, 0, source.getHeight());
        }

        private void hienPopupBaoCao(JButton source) {
                JPopupMenu popup = new JPopupMenu();
                themMenuItem(popup, "Theo ngày");
                themMenuItem(popup, "Theo tháng");
                themMenuItem(popup, "Theo năm");
                popup.show(source, 0, source.getHeight());
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

        private void themMenuItem(JPopupMenu popup, String text) {
                JMenuItem item = new JMenuItem(text);
                item.addActionListener(e -> hienThongBao(text));
                popup.add(item);
        }

        private void hienThongTinNhanVien(JButton source) {
                JPopupMenu popup = new JPopupMenu();
                JPanel info = new JPanel(new GridLayout(0, 2, 15, 8));
                info.setBorder(new EmptyBorder(12, 12, 12, 12));

                themThongTin(info, "Mã NV", maNV);
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

                popup.show(source, -popup.getPreferredSize().width + source.getWidth(), source.getHeight());
        }

        private void themThongTin(JPanel panel, String label, String value) {
                JLabel lb = new JLabel(label);
                lb.setFont(new Font("Segoe UI", Font.BOLD, 12));
                JLabel val = new JLabel(value);
                panel.add(lb);
                panel.add(val);
        }

        private void hienThongBao(String tenChucNang) {
                JOptionPane.showMessageDialog(this, "Bạn đang mở chức năng: " + tenChucNang, "Pet Shop Pro",
                                JOptionPane.INFORMATION_MESSAGE);
        }
}