package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DangNhap extends JFrame {

    private JTextField txtTaiKhoan;
    private JPasswordField txtMatKhau;

    private final Color NAVY = new Color(18, 48, 82);
    private final Color LIGHT_BLUE = new Color(229, 241, 252);
    private final Color GREEN = new Color(72, 196, 62);

    public DangNhap() {
        setTitle("Pet Shop Pro - Dang nhap");
        setSize(1000, 570);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(createLeftPanel());
        mainPanel.add(createRightPanel());

        add(mainPanel);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(NAVY);
        panel.setBorder(new EmptyBorder(55, 50, 40, 50));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("🐾");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel ten = new JLabel("Pet Shop Pro");
        ten.setForeground(Color.WHITE);
        ten.setFont(new Font("Segoe UI", Font.BOLD, 27));
        ten.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tieuDe = new JLabel("HE THONG QUAN LY CUA HANG THU CUNG");
        tieuDe.setForeground(new Color(255, 105, 90));
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tieuDe.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel slogan = new JLabel(
                "<html><center>Van hanh gon gang.<br>"
                        + "Cham thu cung tan tam.</center></html>"
        );
        slogan.setForeground(Color.WHITE);
        slogan.setFont(new Font("Segoe UI", Font.BOLD, 16));
        slogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(logo);
        content.add(Box.createVerticalStrut(3));
        content.add(ten);
        content.add(Box.createVerticalStrut(15));
        content.add(tieuDe);
        content.add(Box.createVerticalStrut(14));
        content.add(slogan);
        content.add(Box.createVerticalStrut(35));

        JPanel tinhNang = new JPanel();
        tinhNang.setBackground(new Color(169, 194, 218));
        tinhNang.setBorder(new EmptyBorder(14, 15, 14, 15));
        tinhNang.setLayout(new BoxLayout(tinhNang, BoxLayout.Y_AXIS));

        String[] dsTinhNang = {
                "Thanh toan tai quay - Thanh toan nhanh",
                "Lich Spa - Dieu phoi phong va tho",
                "Kho hang - Theo doi ton va nhap hang",
                "Bao cao - Doi soat theo ca"
        };

        for (String s : dsTinhNang) {
            JLabel label = new JLabel(s);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            tinhNang.add(label);
            tinhNang.add(Box.createVerticalStrut(9));
        }

        content.add(tinhNang);
        content.add(Box.createVerticalGlue());

        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        status.setOpaque(false);

        JLabel cham = new JLabel("●");
        cham.setForeground(Color.GREEN);
        cham.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel trangThai = new JLabel(
                "May chu Pet Shop Pro dang hoat dong on dinh"
        );
        trangThai.setForeground(Color.WHITE);
        trangThai.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        status.add(cham);
        status.add(trangThai);

        content.add(status);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(LIGHT_BLUE);

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(350, 355));
        card.setBorder(new EmptyBorder(35, 35, 35, 35));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Dang Nhap");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(28));

        JLabel lbTaiKhoan = new JLabel("TAI KHOAN");
        lbTaiKhoan.setForeground(Color.GRAY);
        lbTaiKhoan.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbTaiKhoan.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbTaiKhoan);

        card.add(Box.createVerticalStrut(7));

        txtTaiKhoan = new JTextField();
        txtTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTaiKhoan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtTaiKhoan.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(txtTaiKhoan);

        card.add(Box.createVerticalStrut(18));

        JLabel lbMatKhau = new JLabel("MAT KHAU");
        lbMatKhau.setForeground(Color.GRAY);
        lbMatKhau.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbMatKhau);

        card.add(Box.createVerticalStrut(7));

        txtMatKhau = new JPasswordField();
        txtMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(txtMatKhau);

        card.add(Box.createVerticalStrut(30));

        JButton btnDangNhap = new JButton("DANG NHAP");
        btnDangNhap.setBackground(GREEN);
        btnDangNhap.setForeground(Color.WHITE);
        btnDangNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDangNhap.setFocusPainted(false);
        btnDangNhap.setBorderPainted(false);
        btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnDangNhap.addActionListener(e -> xuLyDangNhap());
        txtMatKhau.addActionListener(e -> xuLyDangNhap());

        card.add(btnDangNhap);

        background.add(card);
        return background;
    }

    private void xuLyDangNhap() {
        String taiKhoan = txtTaiKhoan.getText().trim();
        String matKhau = new String(txtMatKhau.getPassword());

        if (taiKhoan.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui long nhap tai khoan!",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE
            );
            txtTaiKhoan.requestFocus();
            return;
        }

        if (matKhau.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui long nhap mat khau!",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE
            );
            txtMatKhau.requestFocus();
            return;
        }

        // Tai khoan demo de test giao dien
        if (taiKhoan.equals("1")
                && matKhau.equals("2")) {

            dispose();

            new TrangChu(
                    "NV001",
                    "Dang Dinh An",
                    "Quan ly"
            ).setVisible(true);

        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Tai khoan hoac mat khau khong chinh xac!",
                    "Dang nhap that bai",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
