package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TrangChu extends JFrame {

    // =========================
    // MAU SAC
    // =========================
    private final Color NAVY = new Color(18, 48, 82);
    private final Color LIGHT_BLUE = new Color(229, 241, 252);
    private final Color MENU_BLUE = new Color(207, 222, 235);

    // =========================
    // THONG TIN NHAN VIEN
    // =========================
    private String maNV;
    private String hoTen;
    private String vaiTro;

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

        // HEADER (thanh menu dung chung cho cac man hinh)
        add(new NavHeader(this, "trangchu"), BorderLayout.NORTH);

        // NOI DUNG CHINH
        add(createMainContent(), BorderLayout.CENTER);
    }

    // =========================================================
    // NOI DUNG CHINH
    // =========================================================

    private JPanel createMainContent() {

        JPanel main = new JPanel(
                new BorderLayout()
        );

        main.setBackground(
                LIGHT_BLUE
        );

        main.add(
                createTitleBar(),
                BorderLayout.NORTH
        );

        JPanel center = new JPanel(
                new GridLayout(
                        1,
                        2,
                        20,
                        0
                )
        );

        center.setBackground(
                LIGHT_BLUE
        );

        center.setBorder(
                new EmptyBorder(
                        20,
                        30,
                        30,
                        30
                )
        );

        center.add(
                createIntroduction()
        );

        center.add(
                createQuickAccess()
        );

        main.add(
                center,
                BorderLayout.CENTER
        );

        return main;
    }

    // =========================================================
    // THANH TIEU DE
    // =========================================================

    private JPanel createTitleBar() {

        JPanel panel = new JPanel(
                new BorderLayout()
        );

        panel.setBackground(
                new Color(
                        247,
                        249,
                        251
                )
        );

        panel.setBorder(
                new EmptyBorder(
                        10,
                        25,
                        10,
                        25
                )
        );

        JLabel date = new JLabel(
                "▣  "
                + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "HH:mm:ss | dd/MM/yyyy"
                        )
                )
        );

        date.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        JLabel title = new JLabel(
                "TRANG CHỦ",
                SwingConstants.CENTER
        );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        25
                )
        );

        panel.add(
                date,
                BorderLayout.WEST
        );

        panel.add(
                title,
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // ANH GIOI THIEU
    // =========================================================

    private JPanel createIntroduction() {

        JPanel panel = new JPanel(
                new BorderLayout()
        );

        panel.setBackground(
                Color.WHITE
        );

        panel.setBorder(null);

        ImageIcon icon = new ImageIcon(
                getClass().getResource(
                        "/img/giothieutrangchu.png"
                )
        );

        Image image = icon.getImage();

        int panelWidth = 550;

        int panelHeight = 510;

        Image scaledImage = image.getScaledInstance(
                panelWidth,
                panelHeight,
                Image.SCALE_SMOOTH
        );

        JLabel imageLabel = new JLabel(
                new ImageIcon(
                        scaledImage
                )
        );

        imageLabel.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        imageLabel.setVerticalAlignment(
                SwingConstants.CENTER
        );

        panel.add(
                imageLabel,
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // TRUY CAP NHANH
    // =========================================================

    private JPanel createQuickAccess() {

        JPanel panel = new JPanel();

        panel.setBackground(
                Color.WHITE
        );

        panel.setBorder(
                new EmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel title = new JLabel(
                "TRUY CẬP NHANH"
        );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        18
                )
        );

        title.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        panel.add(title);

        panel.add(
                Box.createVerticalStrut(15)
        );

        addQuickButton(
                panel,
                "Thanh toán",
                "F4"
        );

        addQuickButton(
                panel,
                "Quản lý lịch hẹn Spa",
                "F5"
        );

        addQuickButton(
                panel,
                "Quản lý sản phẩm",
                "F6"
        );

        addQuickButton(
                panel,
                "Quản lý khách hàng",
                "F7"
        );

        addQuickButton(
                panel,
                "Báo cáo doanh thu",
                "F8"
        );

        return panel;
    }

    // =========================================================
    // BUTTON TRUY CAP NHANH
    // =========================================================

    private void addQuickButton(
            JPanel parent,
            String text,
            String shortcut
    ) {

        JPanel row = new JPanel(
                new BorderLayout()
        );

        row.setBackground(
                MENU_BLUE
        );

        row.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        45
                )
        );

        JButton button = new JButton(text);

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        button.setBackground(
                MENU_BLUE
        );

        button.setFocusPainted(false);

        button.setBorderPainted(false);

        button.addActionListener(
                e -> {
                    if (text.equals("Quản lý sản phẩm")) {
                        DieuHuong.moKho(this, 1);
                    } else {
                        hienThongBao(text);
                    }
                }
        );

        JLabel key = new JLabel(
                shortcut
        );

        key.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        key.setBorder(
                new EmptyBorder(
                        0,
                        0,
                        0,
                        10
                )
        );

        row.add(
                button,
                BorderLayout.CENTER
        );

        row.add(
                key,
                BorderLayout.EAST
        );

        parent.add(row);

        parent.add(
                Box.createVerticalStrut(5)
        );
    }

    // =========================================================
    // THONG BAO
    // =========================================================

    private void hienThongBao(
            String tenChucNang
    ) {

        JOptionPane.showMessageDialog(
                this,
                "Bạn đang mở chức năng: "
                        + tenChucNang,
                "Pet Shop Pro",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}