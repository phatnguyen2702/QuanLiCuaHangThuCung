package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

// Giao diện/chức năng đăng nhập của ứng dụng.
public class DangNhap extends JFrame implements ActionListener, MouseListener {

        // txtTaiKhoan: Ô nhập/hiển thị văn bản tài khoản.
        private JTextField txtTaiKhoan;
        // txtMatKhau: Ô nhập mật khẩu có che ký tự mật khẩu.
        private JPasswordField txtMatKhau;
        private JButton btnDangNhap;

        // NAVY: Màu dùng cho giao diện: xanh đậm.
        private final Color NAVY = new Color(18, 48, 82);
        // LIGHT_BLUE: Màu dùng cho giao diện: xanh dương.
        private final Color LIGHT_BLUE = new Color(229, 241, 252);
        // GREEN: Màu dùng cho giao diện: xanh lá.
        private final Color GREEN = new Color(72, 196, 62);

        // taiKhoanDAO: Đối tượng truy cập dữ liệu tài khoản.
        private TaiKhoanDAO taiKhoanDAO;

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        public DangNhap() {

                setTitle("Pet Shop Pro - Đăng Nhập");
                setSize(1000, 570);
                setLocationRelativeTo(null);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setResizable(false);

                taiKhoanDAO = new TaiKhoanDAO();

                // pnlMainPanel: Khung chứa thành phần giao diện chính khung.
                JPanel pnlMainPanel = new JPanel(new GridLayout(1, 2));

                pnlMainPanel.add(createLeftPanel());
                pnlMainPanel.add(createRightPanel());

                add(pnlMainPanel);
        }

        // Tạo khung bên trái.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel createLeftPanel() {

                // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung = new JPanel(new BorderLayout());
                pnlKhung.setBackground(NAVY);

                pnlKhung.setBorder(
                                new EmptyBorder(
                                                35, 45, 30, 45));

                // pnlContent: Khung chứa thành phần giao diện nội dung.
                JPanel pnlContent = new JPanel(new BorderLayout(0, 20));
                pnlContent.setOpaque(false);
                // pnlBranding: Khung chứa thành phần giao diện thương hiệu.
                JPanel pnlBranding = new JPanel();
                pnlBranding.setOpaque(false);

                pnlBranding.setLayout(new BoxLayout(pnlBranding, BoxLayout.Y_AXIS));
                // Ten he thong
                // lblTen: Nhãn hiển thị: PET SHOP PRO.
                JLabel lblTen = new JLabel("PET SHOP PRO");
                lblTen.setForeground(Color.WHITE);
                lblTen.setFont(new Font("Segoe UI", Font.BOLD, 26));
                lblTen.setAlignmentX(Component.CENTER_ALIGNMENT);
                // Tieu de
                // lblTieuDe2: Nhãn hiển thị: HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG.
                JLabel lblTieuDe2 = new JLabel("HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG");
                lblTieuDe2.setForeground(new Color(255, 105, 90));
                lblTieuDe2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lblTieuDe2.setAlignmentX(Component.CENTER_ALIGNMENT);
                // lblSlogan
                // lblSlogan: Nhãn hiển thị khẩu hiệu.
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
                // pnlTinhNang: Khung chứa thành phần giao diện tính nặng.
                JPanel pnlTinhNang = new JPanel(new GridBagLayout());
                pnlTinhNang.setBackground(new Color(169, 194, 218));
                pnlTinhNang.setBorder(new EmptyBorder(15, 18, 15, 18));
                // dsTinhNang: Mảng lưu danh sách tính năng.
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
                // pnlStatus: Khung chứa thành phần giao diện trạng thái.
                JPanel pnlStatus = new JPanel(
                                new FlowLayout(
                                                FlowLayout.LEFT,
                                                5,
                                                0));

                pnlStatus.setOpaque(false);

                // lblCham: Nhãn hiển thị: ●.
                JLabel lblCham = new JLabel("●");
                lblCham.setForeground(new Color(70, 220, 80));
                lblCham.setFont(new Font("Arial", Font.BOLD, 18));
                // lblTrangThai2: Nhãn hiển thị: Máy chủ Pet Shop Pro đang hoạt động ổn định.
                JLabel lblTrangThai2 = new JLabel("Máy chủ Pet Shop Pro đang hoạt động ổn định");
                lblTrangThai2.setForeground(Color.WHITE);
                lblTrangThai2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                pnlStatus.add(lblCham);
                pnlStatus.add(lblTrangThai2);
                pnlContent.add(pnlStatus, BorderLayout.SOUTH);
                // THEM CONTENT VAO PANEL
                pnlKhung.add(pnlContent, BorderLayout.CENTER);
                return pnlKhung;
        }

        // RIGHT PANEL
        // Tạo khung bên phải.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel createRightPanel() {
                // pnlBackground: Khung chứa thành phần giao diện màu nền.
                JPanel pnlBackground = new JPanel(new GridBagLayout());
                pnlBackground.setBackground(LIGHT_BLUE);
                // pnlCard: Khung chứa thành phần giao diện thẻ.
                JPanel pnlCard = new JPanel();
                pnlCard.setBackground(Color.WHITE);
                pnlCard.setPreferredSize(new Dimension(360, 395));
                pnlCard.setBorder(new EmptyBorder(35, 35, 35, 35));
                pnlCard.setLayout(new BoxLayout(pnlCard, BoxLayout.Y_AXIS));
                // lblTitle2: Nhãn hiển thị: Đăng Nhập.
                JLabel lblTitle2 = new JLabel("Đăng Nhập");
                lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 27));

                lblTitle2.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlCard.add(lblTitle2);
                pnlCard.add(Box.createVerticalStrut(28));
                // lblTaiKhoan2: Nhãn hiển thị: TÀI KHOẢN.
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
                // Khi nguoi dung nhap chu:
                // tu dong chuyen thanh chu thuong
                txtTaiKhoan.getDocument().addDocumentListener(
                                new javax.swing.event.DocumentListener() {
                                        // Cập nhật trạng thái liên quan đến nội dung đang nhập.
                                        private void xuLy() {
                                                SwingUtilities.invokeLater(() -> {
                                                        // text: Đọc nội dung chữ từ ô nhập để xử lý nội dung chữ.
                                                        String text = txtTaiKhoan.getText();
                                                        String moi = text.toLowerCase().replaceAll("[^a-z0-9]", "");
                                                        if (!text.equals(moi)) {
                                                                // Cập nhật nội dung chữ hiển thị trong
                                                                // control bằng setText().
                                                                txtTaiKhoan.setText(moi);
                                                        }
                                                });
                                        }

                                        // Cập nhật trạng thái liên quan khi người dùng thêm ký tự vào ô văn bản.
                                        // Tham số e: thông tin sự kiện.
                                        @Override
                                        public void insertUpdate(
                                                        javax.swing.event.DocumentEvent e) {
                                                xuLy();
                                        }

                                        // Cập nhật trạng thái liên quan khi người dùng xóa ký tự trong ô văn bản.
                                        @Override
                                        public void removeUpdate(
                                                        javax.swing.event.DocumentEvent e) {
                                                xuLy();
                                        }

                                        // Nhận thông báo khi thuộc tính của tài liệu văn bản thay đổi.
                                        @Override
                                        public void changedUpdate(
                                                        javax.swing.event.DocumentEvent e) {
                                                xuLy();
                                        }
                                });
                pnlCard.add(txtTaiKhoan);
                pnlCard.add(Box.createVerticalStrut(18));

                // lblMatKhau: Nhãn hiển thị: MẬT KHẨU.
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
                // Khoang cach giua truong mat khau va nut dang nhap.
                pnlCard.add(Box.createVerticalStrut(20));
                // btnDangNhap: Nút thao tác: ĐĂNG NHẬP.
                btnDangNhap = new JButton("ĐĂNG NHẬP");
                btnDangNhap.setBackground(GREEN);
                btnDangNhap.setForeground(Color.WHITE);
                btnDangNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
                btnDangNhap.setFocusPainted(false);
                btnDangNhap.setBorderPainted(false);
                btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                btnDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnDangNhap.addActionListener(this);

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                txtMatKhau.addActionListener(this);

                pnlCard.add(btnDangNhap);

                pnlBackground.add(pnlCard);

                return pnlBackground;
        }

        // Kiểm tra thông tin đăng nhập và mở màn hình tương ứng khi xác thực thành
        // công.
        private void xuLyDangNhap() {

                // taiKhoan: Đọc nội dung chữ từ ô nhập để xử lý tài khoản.
                String taiKhoan = txtTaiKhoan.getText().trim().toLowerCase();

                // matKhau: Lưu mật khẩu.
                String matKhau = new String(txtMatKhau.getPassword());

                if (taiKhoan.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Vui lòng nhập tài khoản!", "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);
                        txtTaiKhoan.requestFocus();
                        return;
                }

                // kiểm tra mật khẩu
                if (matKhau.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu!", "Thong bao",
                                        JOptionPane.WARNING_MESSAGE);
                        txtMatKhau.requestFocus();
                        return;
                }

                // Xac thuc khong yeu cau phien dang nhap.
                // getAll() chi dung cho man hinh QUAN LY TAI KHOAN,
                // vi ham do yeu cau quyen TAI_KHOAN.
                TaiKhoan tk;
                try {
                        tk = taiKhoanDAO.xacThuc(taiKhoan, matKhau);
                } catch (java.sql.SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(
                                        this,
                                        "Không thể kết nối hoặc truy vấn SQL Server!\nChi tiết: " + ex.getMessage(),
                                        "Lỗi cơ sở dữ liệu",
                                        JOptionPane.ERROR_MESSAGE);
                        return;
                }
                if (tk == null) {
                        JOptionPane.showMessageDialog(
                                        this,
                                        "Tài khoản hoặc mật khẩu không chính xác!",
                                        "Đăng nhập thất bại",
                                        JOptionPane.ERROR_MESSAGE);
                        txtMatKhau.selectAll();
                        txtMatKhau.requestFocus();
                        return;
                }
                // Khoi tao trang chu truoc khi dong cua so dang nhap:
                // neu trang chu gap loi thi nguoi dung van nhin thay thong bao.
                try {
                        TrangChu trangChu = new TrangChu(tk.getMaNhanVien(), tk.getHoTen(), tk.getVaiTro());
                        trangChu.setVisible(true);
                        dispose();
                        JOptionPane.showMessageDialog(trangChu, "Đăng nhập thành công!", "Thông báo",
                                        JOptionPane.INFORMATION_MESSAGE);
                } catch (RuntimeException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this,
                                        "Đã xác thực tài khoản nhưng không thể mở Trang chủ!\nChi tiết: "
                                                        + ex.getMessage(),
                                        "Lỗi mở giao diện", JOptionPane.ERROR_MESSAGE);
                }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
                // Cả nhấn nút và Enter trong ô mật khẩu đều đăng nhập như trước.
                Object nguon = e.getSource();
                if (nguon == btnDangNhap || nguon == txtMatKhau) {
                        xuLyDangNhap();
                }
        }

        // Giữ sẵn giao diện MouseListener cho các thao tác chuột khi cần bổ sung.
        // Những màn hình này chưa yêu cầu xử lý chuột riêng, nên không đăng ký sự kiện
        // thừa.
        @Override
        public void mouseClicked(MouseEvent e) {
                // Không gọi lại hàm chọn dòng để tránh xử lý trùng sự kiện.
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

}
