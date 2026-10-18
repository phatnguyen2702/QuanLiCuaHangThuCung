package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;

import java.awt.*;
import java.text.Normalizer;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Arrays;

public class DangNhap extends JFrame {

        private JTextField txtTaiKhoan;
        private JPasswordField txtMatKhau;

        private final Color NAVY = new Color(18, 48, 82);
        private final Color LIGHT_BLUE = new Color(229, 241, 252);
        private final Color GREEN = new Color(72, 196, 62);

        private TaiKhoanDAO taiKhoanDAO;
        private int intSoLanXacMinhSai = 0;

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
                                new Dimension(360, 395));

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

                // Quen mat khau: nam GIUA o mat khau va nut dang nhap.
                card.add(Box.createVerticalStrut(9));
                JPanel pnlQuenMatKhau = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                pnlQuenMatKhau.setOpaque(false);
                pnlQuenMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlQuenMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
                JButton btnQuenMatKhau = new JButton("Quên mật khẩu?");
                btnQuenMatKhau.setForeground(NAVY);
                btnQuenMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btnQuenMatKhau.setContentAreaFilled(false);
                btnQuenMatKhau.setBorderPainted(false);
                btnQuenMatKhau.setFocusPainted(false);
                btnQuenMatKhau.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btnQuenMatKhau.addActionListener(e -> moDlgQuenMatKhau());
                pnlQuenMatKhau.add(btnQuenMatKhau);
                card.add(pnlQuenMatKhau);
                card.add(Box.createVerticalStrut(14));

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
        // QUEN MAT KHAU: NHAP MA NV -> DANG NHAP QUAN LY -> CAP MK MOI
        // =========================================================
        private void moDlgQuenMatKhau() {
                if (intSoLanXacMinhSai >= 5) {
                        JOptionPane.showMessageDialog(this,
                                        "Đã quá 5 lần xác minh sai trong phiên này.\n"
                                                        + "Vui lòng liên hệ trực tiếp quản lý.",
                                        "Tạm dừng xác minh", JOptionPane.WARNING_MESSAGE);
                        return;
                }

                JDialog dlgQuenMatKhau = new JDialog(this, "Pet Shop Pro - Quên mật khẩu", true);
                dlgQuenMatKhau.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dlgQuenMatKhau.setSize(480, 295);
                dlgQuenMatKhau.setMinimumSize(new Dimension(450, 280));
                dlgQuenMatKhau.setLocationRelativeTo(this);
                dlgQuenMatKhau.setLayout(new BorderLayout());

                JPanel pnlTieuDe = new JPanel(new BorderLayout());
                pnlTieuDe.setBackground(NAVY);
                pnlTieuDe.setBorder(new EmptyBorder(15, 22, 15, 22));
                JLabel lblTieuDe = new JLabel("YÊU CẦU KHÔI PHỤC MẬT KHẨU");
                lblTieuDe.setForeground(Color.WHITE);
                lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 17));
                pnlTieuDe.add(lblTieuDe, BorderLayout.CENTER);
                dlgQuenMatKhau.add(pnlTieuDe, BorderLayout.NORTH);

                JPanel pnlNoiDung = new JPanel(new GridBagLayout());
                pnlNoiDung.setBackground(Color.WHITE);
                pnlNoiDung.setBorder(new EmptyBorder(16, 24, 12, 24));
                JTextField txtMaNhanVien = new JTextField();
                themHangXacMinh(pnlNoiDung, 0, "Mã nhân viên", txtMaNhanVien);

                JLabel lblHuongDan = new JLabel("<html><body style='width:340px'>"
                                + "Chỉ cần nhập mã nhân viên cần khôi phục. "
                                + "Quản lý sẽ đăng nhập để xác minh và cấp mật khẩu tạm thời mới. "
                                + "Hệ thống không hiển thị mật khẩu cũ."
                                + "</body></html>");
                lblHuongDan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblHuongDan.setForeground(new Color(95, 105, 115));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.gridwidth = 2;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(14, 4, 3, 4);
                pnlNoiDung.add(lblHuongDan, gbc);
                dlgQuenMatKhau.add(pnlNoiDung, BorderLayout.CENTER);

                JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
                pnlNut.setBackground(Color.WHITE);
                JButton btnHuy = taoBtnXacMinh("Hủy", new Color(105, 115, 125));
                JButton btnTiepTuc = taoBtnXacMinh("Tiếp tục", GREEN);
                btnHuy.addActionListener(e -> dlgQuenMatKhau.dispose());
                btnTiepTuc.addActionListener(e -> {
                        String strMaNhanVien = txtMaNhanVien.getText().trim();
                        if (strMaNhanVien.isEmpty()) {
                                JOptionPane.showMessageDialog(dlgQuenMatKhau,
                                                "Vui lòng nhập mã nhân viên cần khôi phục.",
                                                "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                                txtMaNhanVien.requestFocusInWindow();
                                return;
                        }
                        if (strMaNhanVien.length() > 20) {
                                JOptionPane.showMessageDialog(dlgQuenMatKhau,
                                                "Mã nhân viên quá dài.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                                return;
                        }
                        // Khong tiet lo tai khoan co ton tai hay khong truoc khi quan ly xac thuc.
                        if (pheDuyetCapLaiMatKhau(dlgQuenMatKhau, strMaNhanVien)) {
                                dlgQuenMatKhau.dispose();
                        } else if (intSoLanXacMinhSai >= 5) {
                                dlgQuenMatKhau.dispose();
                        }
                });
                pnlNut.add(btnHuy);
                pnlNut.add(btnTiepTuc);
                dlgQuenMatKhau.add(pnlNut, BorderLayout.SOUTH);
                dlgQuenMatKhau.getRootPane().setDefaultButton(btnTiepTuc);
                dlgQuenMatKhau.setVisible(true);
        }

        private void themHangXacMinh(JPanel pnl, int dong, String strNhan, JComponent cmpNhap) {
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridy = dong;
                gbc.insets = new Insets(7, 5, 7, 8);
                gbc.anchor = GridBagConstraints.WEST;
                gbc.gridx = 0;
                gbc.weightx = 0;
                JLabel lblNhan = new JLabel(strNhan + ":");
                lblNhan.setFont(new Font("Segoe UI", Font.BOLD, 12));
                pnl.add(lblNhan, gbc);

                gbc.gridx = 1;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                cmpNhap.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                cmpNhap.setPreferredSize(new Dimension(270, 34));
                pnl.add(cmpNhap, gbc);
        }

        private JButton taoBtnXacMinh(String strText, Color clrNen) {
                JButton btn = new JButton(strText);
                btn.setBackground(clrNen);
                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btn.setPreferredSize(new Dimension(125, 37));
                return btn;
        }

        private boolean pheDuyetCapLaiMatKhau(JDialog dlgCha, String strMaNhanVien) {
                if (intSoLanXacMinhSai >= 5)
                        return false;

                JPanel pnlXacNhan = new JPanel(new GridLayout(0, 1, 6, 6));
                pnlXacNhan.setBorder(new EmptyBorder(10, 12, 10, 12));
                JLabel lblHuongDan = new JLabel("<html><b>Quản lý xác nhận yêu cầu cho "
                                + strMaNhanVien.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                                + "</b></html>");
                pnlXacNhan.add(lblHuongDan);

                JTextField txtTaiKhoanQuanLy = new JTextField();
                JPasswordField pwdQuanLy = new JPasswordField();
                pnlXacNhan.add(new JLabel("Tài khoản quản lý (hoặc mã NV):"));
                pnlXacNhan.add(txtTaiKhoanQuanLy);
                pnlXacNhan.add(new JLabel("Mật khẩu quản lý:"));
                pnlXacNhan.add(pwdQuanLy);

                int intChon = JOptionPane.showConfirmDialog(dlgCha, pnlXacNhan,
                                "Đăng nhập tài khoản quản lý", JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                if (intChon != JOptionPane.OK_OPTION)
                        return false;

                char[] arrMatKhauQuanLy = pwdQuanLy.getPassword();
                try {
                        String strTaiKhoanQuanLy = txtTaiKhoanQuanLy.getText().trim();
                        if (strTaiKhoanQuanLy.isEmpty() || arrMatKhauQuanLy.length == 0) {
                                JOptionPane.showMessageDialog(dlgCha,
                                                "Hãy nhập tài khoản và mật khẩu của quản lý.",
                                                "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                                return false;
                        }

                        String strMatKhauMoi = taoMatKhauTamThoi();
                        boolean blnThanhCong = taiKhoanDAO.capLaiMatKhauQuaTaiKhoanQuanLy(
                                        strMaNhanVien, strTaiKhoanQuanLy,
                                        new String(arrMatKhauQuanLy), strMatKhauMoi);
                        if (!blnThanhCong) {
                                intSoLanXacMinhSai++;
                                JOptionPane.showMessageDialog(dlgCha,
                                                "Không thể cấp lại mật khẩu. Kiểm tra mã nhân viên hoặc tài khoản quản lý."
                                                                + "\nSố lần xác minh còn lại: "
                                                                + Math.max(0, 5 - intSoLanXacMinhSai),
                                                "Xác minh thất bại", JOptionPane.WARNING_MESSAGE);
                                return false;
                        }

                        JTextField txtMatKhauTamThoi = new JTextField(strMatKhauMoi, 22);
                        txtMatKhauTamThoi.setEditable(false);
                        txtMatKhauTamThoi.setFont(new Font("Consolas", Font.BOLD, 16));
                        JPanel pnlThongBao = new JPanel(new BorderLayout(0, 10));
                        pnlThongBao.add(new JLabel("Mật khẩu tạm thời mới cho " + strMaNhanVien + ":"),
                                        BorderLayout.NORTH);
                        pnlThongBao.add(txtMatKhauTamThoi, BorderLayout.CENTER);
                        pnlThongBao.add(new JLabel(
                                        "Giao riêng cho nhân viên và yêu cầu đổi mật khẩu sau khi đăng nhập."),
                                        BorderLayout.SOUTH);
                        JOptionPane.showMessageDialog(dlgCha, pnlThongBao,
                                        "Quản lý đã cấp lại mật khẩu", JOptionPane.INFORMATION_MESSAGE);
                        txtMatKhau.setText("");
                        intSoLanXacMinhSai = 0;
                        return true;
                } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dlgCha,
                                        "Không kết nối/cập nhật được cơ sở dữ liệu. Vui lòng kiểm tra Console.",
                                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                        return false;
                } finally {
                        Arrays.fill(arrMatKhauQuanLy, '\0');
                        pwdQuanLy.setText("");
                }
        }

        private String taoMatKhauTamThoi() {
                String strKyTu = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#";
                SecureRandom random = new SecureRandom();
                StringBuilder strbMatKhau = new StringBuilder(12);
                for (int i = 0; i < 12; i++) {
                        strbMatKhau.append(strKyTu.charAt(random.nextInt(strKyTu.length())));
                }
                return strbMatKhau.toString();
        }

        // =========================================================
        // XU LY DANG NHAP
        // =========================================================
        private void xuLyDangNhap() {

                String taiKhoan = txtTaiKhoan.getText()
                                .trim()
                                .toLowerCase();

                String matKhau = new String(
                                txtMatKhau.getPassword());

                // =========================
                // KIEM TRA TAI KHOAN
                // =========================

                if (taiKhoan.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui lòng nhập tài khoản!",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);

                        txtTaiKhoan.requestFocus();

                        return;
                }

                // =========================
                // KIEM TRA MAT KHAU
                // =========================

                if (matKhau.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui lòng nhập mật khẩu!",
                                        "Thong bao",
                                        JOptionPane.WARNING_MESSAGE);

                        txtMatKhau.requestFocus();

                        return;
                }

                // =========================
                // KIEM TRA DATABASE
                // =========================

                TaiKhoan tk = null;

                for (TaiKhoan temp : taiKhoanDAO.getAll()) {

                        String taiKhoanDB = taoTaiKhoanKhongDau(
                                        temp.getHoTen(),
                                        temp.getMaNhanVien());

                        if (taiKhoanDB.equals(taiKhoan)
                                        && temp.getMatKhau().equals(matKhau)) {

                                tk = temp;

                                break;
                        }
                }

                // =========================
                // DANG NHAP THANH CONG
                // =========================

                if (tk != null) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Đăng nhập thành công!",
                                        "Thông báo",
                                        JOptionPane.INFORMATION_MESSAGE);

                        dispose();

                        new TrangChu(
                                        tk.getMaNhanVien(),
                                        tk.getHoTen(),
                                        tk.getVaiTro()).setVisible(true);

                } else {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Tài khoản hoặc mật khẩu không chính xác!",
                                        "Đăng nhập thất bại",
                                        JOptionPane.ERROR_MESSAGE);

                        txtMatKhau.selectAll();

                        txtMatKhau.requestFocus();
                }
        }
}