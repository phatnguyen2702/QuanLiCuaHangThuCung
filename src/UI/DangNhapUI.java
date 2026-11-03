package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import control.DangNhapControl;
import entity.NhanVien;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DangNhapUI extends JFrame implements ActionListener {

        private JTextField txtTaiKhoan;
        private JPasswordField txtMatKhau;
        private JButton btnDangNhap;

        private final Color NAVY = new Color(18, 48, 82);

        private final Color LIGHT_BLUE = new Color(229, 241, 252);

        private final Color GREEN = new Color(72, 196, 62);

        private final DangNhapControl control = new DangNhapControl();

        public DangNhapUI() {

                setTitle("Pet Shop Pro - Đăng Nhập");
                setSize(1000, 570);
                setLocationRelativeTo(null);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setResizable(false);

                JPanel pnlMainPanel = new JPanel(new GridLayout(1, 2));

                pnlMainPanel.add(createLeftPanel());
                pnlMainPanel.add(createRightPanel());

                add(pnlMainPanel);
        }

        private JPanel createLeftPanel() {

                JPanel pnlKhung = new JPanel(new BorderLayout());
                pnlKhung.setBackground(NAVY);

                pnlKhung.setBorder(
                                new EmptyBorder(
                                                35, 45, 30, 45));

                JPanel pnlContent = new JPanel(new BorderLayout(0, 20));
                pnlContent.setOpaque(false);
                JPanel pnlBranding = new JPanel();
                pnlBranding.setOpaque(false);

                pnlBranding.setLayout(new BoxLayout(pnlBranding, BoxLayout.Y_AXIS));

                JLabel lblTen = new JLabel("PET SHOP PRO");
                lblTen.setForeground(Color.WHITE);
                lblTen.setFont(new Font("Segoe UI", Font.BOLD, 26));
                lblTen.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel lblTieuDe2 = new JLabel("HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG");
                lblTieuDe2.setForeground(new Color(255, 105, 90));
                lblTieuDe2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lblTieuDe2.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel lblSlogan = new JLabel(
                                "<html>" + "<div style='text-align:center;'>"
                                                + "Vận hành gọn gàng<br>"
                                                + "Chăm thú cưng tận tâm"
                                                + "</div>"
                                                + "</html>");
                lblSlogan.setForeground(Color.WHITE);
                lblSlogan.setFont(new Font("Segoe UI", Font.ITALIC, 18));
                lblSlogan.setAlignmentX(Component.CENTER_ALIGNMENT);
                lblSlogan.setHorizontalAlignment(SwingConstants.CENTER);
                pnlBranding.add(Box.createVerticalStrut(2));
                pnlBranding.add(lblTen);
                pnlBranding.add(Box.createVerticalStrut(12));
                pnlBranding.add(lblTieuDe2);
                pnlBranding.add(Box.createVerticalStrut(12));
                pnlBranding.add(lblSlogan);
                pnlContent.add(pnlBranding, BorderLayout.NORTH);
                JPanel pnlTinhNang = new JPanel(new GridBagLayout());
                pnlTinhNang.setBackground(new Color(169, 194, 218));
                pnlTinhNang.setBorder(new EmptyBorder(15, 18, 15, 18));

                JLabel lblTinhNang = new JLabel(
                                "<html>"
                                                + "<div>"
                                                + "• Thanh toán tại quầy - Thanh toán nhanh<br><br>"
                                                + "• Lịch Spa - Điều phối phòng và thợ<br><br>"
                                                + "• Kho hàng - Theo dõi tồn và nhập hàng<br><br>"
                                                + "• Báo cáo - Đối soát theo ca"
                                                + "</div>"
                                                + "</html>");

                lblTinhNang.setForeground(Color.WHITE);
                lblTinhNang.setFont(new Font("Segoe UI", Font.BOLD, 18));
                pnlTinhNang.add(lblTinhNang);

                pnlContent.add(pnlTinhNang, BorderLayout.CENTER);
                JPanel pnlStatus = new JPanel(
                                new FlowLayout(
                                                FlowLayout.LEFT,
                                                5,
                                                0));

                pnlStatus.setOpaque(false);

                JLabel lblCham = new JLabel("●");
                lblCham.setForeground(new Color(70, 220, 80));
                lblCham.setFont(new Font("Arial", Font.BOLD, 18));
                JLabel lblTrangThai2 = new JLabel("Máy chủ Pet Shop Pro đang hoạt động ổn định");
                lblTrangThai2.setForeground(Color.WHITE);
                lblTrangThai2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                pnlStatus.add(lblCham);
                pnlStatus.add(lblTrangThai2);
                pnlContent.add(pnlStatus, BorderLayout.SOUTH);

                pnlKhung.add(pnlContent, BorderLayout.CENTER);
                return pnlKhung;
        }

        private JPanel createRightPanel() {
                JPanel pnlBackground = new JPanel(new GridBagLayout());
                pnlBackground.setBackground(LIGHT_BLUE);
                JPanel pnlCard = new JPanel();
                pnlCard.setBackground(Color.WHITE);
                pnlCard.setPreferredSize(new Dimension(360, 395));
                pnlCard.setBorder(new EmptyBorder(35, 35, 35, 35));
                pnlCard.setLayout(new BoxLayout(pnlCard, BoxLayout.Y_AXIS));
                JLabel lblTitle2 = new JLabel("Đăng Nhập");
                lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 27));

                lblTitle2.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlCard.add(lblTitle2);
                pnlCard.add(Box.createVerticalStrut(28));
                JLabel lblTaiKhoan2 = new JLabel("TÀI KHOẢN");
                lblTaiKhoan2.setForeground(Color.GRAY);
                lblTaiKhoan2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblTaiKhoan2.setAlignmentX(Component.LEFT_ALIGNMENT);

                pnlCard.add(lblTaiKhoan2);
                pnlCard.add(Box.createVerticalStrut(7));
                txtTaiKhoan = new JTextField();

                txtTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                txtTaiKhoan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

                txtTaiKhoan.setAlignmentX(Component.LEFT_ALIGNMENT);

                txtTaiKhoan.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                        private void locTaiKhoan() {
                                SwingUtilities.invokeLater(() -> {
                                        String hienTai = txtTaiKhoan.getText();
                                        String hopLe = control.chuanHoaTaiKhoan(hienTai);
                                        if (!hienTai.equals(hopLe))
                                                txtTaiKhoan.setText(hopLe);
                                });
                        }

                        @Override
                        public void insertUpdate(javax.swing.event.DocumentEvent e) {
                                locTaiKhoan();
                        }

                        @Override
                        public void removeUpdate(javax.swing.event.DocumentEvent e) {
                                locTaiKhoan();
                        }

                        @Override
                        public void changedUpdate(javax.swing.event.DocumentEvent e) {
                                locTaiKhoan();
                        }
                });
                pnlCard.add(txtTaiKhoan);
                pnlCard.add(Box.createVerticalStrut(18));

                JLabel lblMatKhau = new JLabel("MẬT KHẨU");
                lblMatKhau.setForeground(Color.GRAY);
                lblMatKhau.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlCard.add(lblMatKhau);
                pnlCard.add(Box.createVerticalStrut(7));

                txtMatKhau = new JPasswordField();
                txtMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                txtMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
                txtMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlCard.add(txtMatKhau);

                pnlCard.add(Box.createVerticalStrut(20));
                btnDangNhap = new JButton("ĐĂNG NHẬP");
                btnDangNhap.setBackground(GREEN);
                btnDangNhap.setForeground(Color.WHITE);
                btnDangNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
                btnDangNhap.setFocusPainted(false);
                btnDangNhap.setBorderPainted(false);
                btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                btnDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);

                btnDangNhap.addActionListener(this);

                txtMatKhau.addActionListener(this);

                pnlCard.add(btnDangNhap);

                pnlBackground.add(pnlCard);

                return pnlBackground;
        }

        private void xuLyDangNhap() {
        char[] matKhau = txtMatKhau.getPassword();
        NhanVien tk;
        try {
            tk = control.dangNhap(txtTaiKhoan.getText(), matKhau);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
            if (txtTaiKhoan.getText().isBlank()) txtTaiKhoan.requestFocus();
            else txtMatKhau.requestFocus();
            return;
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể kết nối hoặc truy vấn SQL Server!\nChi tiết: " + ex.getMessage(),
                    "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
            return;
        } finally {
            java.util.Arrays.fill(matKhau, '\0');
        }
        if (tk == null) {
            JOptionPane.showMessageDialog(this, "Tài khoản hoặc mật khẩu không chính xác!",
                    "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
            txtMatKhau.selectAll();
            txtMatKhau.requestFocus();
            return;
        }
        try {
            TrangChuUI trangChu = new TrangChuUI(tk.getMaNhanVien(), tk.getHoTen(), tk.getVaiTro());
            trangChu.setVisible(true);
            dispose();
            JOptionPane.showMessageDialog(trangChu, "Đăng nhập thành công!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Đã xác thực nhưng không mở được Trang chủ!\n" + ex.getMessage(),
                    "Lỗi mở giao diện", JOptionPane.ERROR_MESSAGE);
        }
    }

        @Override
        public void actionPerformed(ActionEvent e) {

                Object nguon = e.getSource();
                if (nguon == btnDangNhap || nguon == txtMatKhau) {
                        xuLyDangNhap();
                }
        }

}
