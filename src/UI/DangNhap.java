package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;

import java.awt.*;
import java.text.Normalizer;

public class DangNhap extends JFrame {

        private JTextField txtTaiKhoan;
        private JPasswordField txtMatKhau;

        private final Color NAVY = new Color(18, 48, 82);
        private final Color LIGHT_BLUE = new Color(229, 241, 252);
        private final Color GREEN = new Color(72, 196, 62);

        private TaiKhoanDAO taiKhoanDAO;

        public DangNhap() {

                setTitle("Pet Shop Pro - Đăng Nhập");
                setSize(1000, 570);
                setLocationRelativeTo(null);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setResizable(false);

                taiKhoanDAO = new TaiKhoanDAO();

                JPanel mainPanel = new JPanel(new GridLayout(1, 2));

                mainPanel.add(createLeftPanel());
                mainPanel.add(createRightPanel());

                add(mainPanel);
        }

        // =========================================================
        // BO DAU + VIET THUONG + BO KHOANG TRANG
        // =========================================================
        private String taoTaiKhoanKhongDau(String hoTen, String maNhanVien) {

                String tenKhongDau = Normalizer.normalize(
                                hoTen,
                                Normalizer.Form.NFD);

                tenKhongDau = tenKhongDau
                                .replaceAll("\\p{M}", "")
                                .replace("đ", "d")
                                .replace("Đ", "D")
                                .toLowerCase()
                                .replaceAll("\\s+", "");

                // Lay 3 so cuoi cua ma nhan vien
                String baSoCuoi = maNhanVien.substring(
                                maNhanVien.length() - 3);

                return tenKhongDau + baSoCuoi;
        }

        // =========================================================
        // LEFT PANEL
        // =========================================================
        private JPanel createLeftPanel() {

                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(NAVY);

                panel.setBorder(
                                new EmptyBorder(
                                                35, 45, 30, 45));

                // =====================================================
                // KHU VUC NOI DUNG CHINH
                // =====================================================
                JPanel content = new JPanel(new BorderLayout(0, 20));
                content.setOpaque(false);

                // =====================================================
                // PHAN LOGO + TIEU DE
                // =====================================================
                JPanel branding = new JPanel();
                branding.setOpaque(false);

                branding.setLayout(
                                new BoxLayout(
                                                branding,
                                                BoxLayout.Y_AXIS));

                // Logo
                JLabel logo = new JLabel("🐾");
                logo.setFont(
                                new Font(
                                                "Segoe UI Emoji",
                                                Font.PLAIN,
                                                48));
                logo.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                // Ten he thong
                JLabel ten = new JLabel("PET SHOP PRO");
                ten.setForeground(Color.WHITE);
                ten.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                26));
                ten.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                // Tieu de
                JLabel tieuDe = new JLabel(
                                "HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG");
                tieuDe.setForeground(
                                new Color(255, 105, 90));
                tieuDe.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                16));
                tieuDe.setAlignmentX(
                                Component.CENTER_ALIGNMENT);
                // slogan
                JLabel slogan = new JLabel(
                                "<html>"
                                                + "<div style='text-align:center;'>"
                                                + "Vận hành gọn gàng<br>"
                                                + "Chăm thú cưng tận tâm"
                                                + "</div>"
                                                + "</html>");

                slogan.setForeground(Color.WHITE);
                slogan.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.ITALIC,
                                                18));

                slogan.setAlignmentX(Component.CENTER_ALIGNMENT);
                slogan.setHorizontalAlignment(SwingConstants.CENTER);

                branding.add(logo);
                branding.add(
                                Box.createVerticalStrut(2));

                branding.add(ten);

                branding.add(
                                Box.createVerticalStrut(12));

                branding.add(tieuDe);

                branding.add(
                                Box.createVerticalStrut(12));

                branding.add(slogan);

                content.add(
                                branding,
                                BorderLayout.NORTH);

                // =====================================================
                // KHUNG TINH NANG
                // =====================================================
                JPanel tinhNang = new JPanel(
                                new GridBagLayout());

                tinhNang.setBackground(
                                new Color(169, 194, 218));

                tinhNang.setBorder(
                                new EmptyBorder(
                                                15, 18, 15, 18));

                String[] dsTinhNang = {
                                "Thanh toán tại quầy - Thanh toán nhanh",
                                "Lịch Spa - Điều phối phòng và thợ",
                                "Kho hàng - Theo dõi tồn và nhập hàng",
                                "Báo cáo - Đối soát theo ca"
                };

                GridBagConstraints gbc = new GridBagConstraints();

                gbc.gridx = 0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1.0;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(
                                5, 0, 5, 0);

                for (int i = 0; i < dsTinhNang.length; i++) {

                        JLabel label = new JLabel(
                                        "<html>"
                                                        + "<b>•</b>&nbsp;&nbsp;"
                                                        + dsTinhNang[i]
                                                        + "</html>");

                        label.setForeground(Color.WHITE);
                        label.setFont(
                                        new Font(
                                                        "Segoe UI",
                                                        Font.BOLD,
                                                        18));

                        gbc.gridy = i;

                        tinhNang.add(
                                        label,
                                        gbc);
                }

                content.add(
                                tinhNang,
                                BorderLayout.CENTER);

                // =====================================================
                // TRANG THAI MAY CHU
                // =====================================================
                JPanel status = new JPanel(
                                new FlowLayout(
                                                FlowLayout.LEFT,
                                                5,
                                                0));

                status.setOpaque(false);

                JLabel cham = new JLabel("●");
                cham.setForeground(
                                new Color(70, 220, 80));
                cham.setFont(
                                new Font(
                                                "Arial",
                                                Font.BOLD,
                                                18));

                JLabel trangThai = new JLabel(
                                "Máy chủ Pet Shop Pro đang hoạt động ổn định");

                trangThai.setForeground(Color.WHITE);
                trangThai.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.ITALIC,
                                                11));

                status.add(cham);
                status.add(trangThai);

                content.add(
                                status,
                                BorderLayout.SOUTH);

                // =====================================================
                // THEM CONTENT VAO PANEL
                // =====================================================
                panel.add(
                                content,
                                BorderLayout.CENTER);

                return panel;
        }

        // =========================================================
        // RIGHT PANEL
        // =========================================================
        private JPanel createRightPanel() {

                JPanel background = new JPanel(
                                new GridBagLayout());

                background.setBackground(LIGHT_BLUE);

                JPanel card = new JPanel();

                card.setBackground(Color.WHITE);

                card.setPreferredSize(
                                new Dimension(350, 355));

                card.setBorder(
                                new EmptyBorder(
                                                35,
                                                35,
                                                35,
                                                35));

                card.setLayout(
                                new BoxLayout(
                                                card,
                                                BoxLayout.Y_AXIS));

                JLabel title = new JLabel("Đăng Nhập");

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                27));

                title.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                card.add(title);

                card.add(
                                Box.createVerticalStrut(28));

                // =========================
                // TAI KHOAN
                // =========================

                JLabel lbTaiKhoan = new JLabel(
                                "TÀI KHOẢN");

                lbTaiKhoan.setForeground(Color.GRAY);

                lbTaiKhoan.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                11));

                lbTaiKhoan.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                card.add(lbTaiKhoan);

                card.add(
                                Box.createVerticalStrut(7));

                txtTaiKhoan = new JTextField();

                txtTaiKhoan.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                14));

                txtTaiKhoan.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                38));

                txtTaiKhoan.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                // Khi nguoi dung nhap chu:
                // tu dong chuyen thanh chu thuong
                txtTaiKhoan.getDocument()
                                .addDocumentListener(
                                                new javax.swing.event.DocumentListener() {

                                                        private void xuLy() {

                                                                SwingUtilities.invokeLater(() -> {

                                                                        String text = txtTaiKhoan.getText();

                                                                        String moi = text.toLowerCase()
                                                                                        .replaceAll(
                                                                                                        "[^a-z0-9]",
                                                                                                        "");

                                                                        if (!text.equals(moi)) {

                                                                                txtTaiKhoan.setText(moi);

                                                                        }
                                                                });
                                                        }

                                                        @Override
                                                        public void insertUpdate(
                                                                        javax.swing.event.DocumentEvent e) {
                                                                xuLy();
                                                        }

                                                        @Override
                                                        public void removeUpdate(
                                                                        javax.swing.event.DocumentEvent e) {
                                                                xuLy();
                                                        }

                                                        @Override
                                                        public void changedUpdate(
                                                                        javax.swing.event.DocumentEvent e) {
                                                                xuLy();
                                                        }
                                                });

                card.add(txtTaiKhoan);

                card.add(
                                Box.createVerticalStrut(18));

                // =========================
                // MAT KHAU
                // =========================

                JLabel lbMatKhau = new JLabel(
                                "MẬT KHẨU");

                lbMatKhau.setForeground(Color.GRAY);

                lbMatKhau.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                11));

                lbMatKhau.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                card.add(lbMatKhau);

                card.add(
                                Box.createVerticalStrut(7));

                txtMatKhau = new JPasswordField();

                txtMatKhau.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                14));

                txtMatKhau.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                38));

                txtMatKhau.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                card.add(txtMatKhau);

                card.add(
                                Box.createVerticalStrut(30));

                // =========================
                // BUTTON
                // =========================

                JButton btnDangNhap = new JButton("ĐĂNG NHẬP");

                btnDangNhap.setBackground(GREEN);

                btnDangNhap.setForeground(Color.WHITE);

                btnDangNhap.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                14));

                btnDangNhap.setFocusPainted(false);

                btnDangNhap.setBorderPainted(false);

                btnDangNhap.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                40));

                btnDangNhap.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                btnDangNhap.addActionListener(
                                e -> xuLyDangNhap());

                txtMatKhau.addActionListener(
                                e -> xuLyDangNhap());

                card.add(btnDangNhap);

                background.add(card);

                return background;
        }

        // =========================================================
        // XU LY DANG NHAP
        // =========================================================
       private void xuLyDangNhap() {
    this.dispose();
    new TrangChu("NV001", "Dang Dinh An", "Quan ly").setVisible(true);
}
}