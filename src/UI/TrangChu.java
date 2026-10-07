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

        setTitle("Pet Shop Pro - Trang chu");
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // HEADER
        add(createHeader(), BorderLayout.NORTH);

        // NOI DUNG CHINH
        add(createMainContent(), BorderLayout.CENTER);
    }

    // =========================================================
    // HEADER
    // =========================================================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(NAVY);
        header.setBorder(
                new EmptyBorder(5, 18, 5, 18)
        );

        // MENU BEN TRAI
        JPanel menu = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        8,
                        8
                )
        );

        menu.setOpaque(false);

        // LOGO
        JLabel logo = new JLabel("PET SHOP PRO");

        logo.setForeground(Color.WHITE);
        logo.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        16
                )
        );

        menu.add(logo);

        // =========================
        // TRANG CHU
        // =========================

        menu.add(
                createMenuButton(
                        "Trang chu",
                        e -> hienThongBao("Trang chu")
                )
        );

        // =========================
        // KHO & DANH MUC
        // =========================

        menu.add(
                createMenuButton(
                        "Kho & Danh muc",
                        e -> hienPopupKho(
                                (JButton) e.getSource()
                        )
                )
        );

        // =========================
        // SPA & LICH HEN
        // =========================

        menu.add(
                createMenuButton(
                        "Spa & Lich hen",
                        e -> hienPopupSpa(
                                (JButton) e.getSource()
                        )
                )
        );

        // =========================
        // HOA DON
        // =========================

        menu.add(
                createMenuButton(
                        "Hoa don",
                        e -> hienPopupHoaDon(
                                (JButton) e.getSource()
                        )
                )
        );

        // =========================
        // BAO CAO
        // =========================

        menu.add(
                createMenuButton(
                        "Bao cao",
                        e -> hienPopupBaoCao(
                                (JButton) e.getSource()
                        )
                )
        );

        // =========================
        // HE THONG
        // =========================

        menu.add(
                createMenuButton(
                        "He thong",
                        e -> hienPopupHeThong(
                                (JButton) e.getSource()
                        )
                )
        );

        // =========================
        // CAI DAT
        // =========================

        menu.add(
                createMenuButton(
                        "Cai dat",
                        e -> hienThongBao("Cai dat")
                )
        );

        header.add(
                menu,
                BorderLayout.WEST
        );

        // =========================
        // THONG TIN NHAN VIEN
        // =========================

        JButton btnNhanVien = createMenuButton(
                hoTen + "  ●",
                e -> hienThongTinNhanVien(
                        (JButton) e.getSource()
                )
        );

        header.add(
                btnNhanVien,
                BorderLayout.EAST
        );

        return header;
    }

    // =========================================================
    // TAO MENU BUTTON
    // =========================================================

    private JButton createMenuButton(
            String text,
            java.awt.event.ActionListener action
    ) {

        JButton button = new JButton(text);

        button.setForeground(Color.WHITE);

        button.setBackground(NAVY);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        button.setFocusPainted(false);

        button.setBorderPainted(false);

        button.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.addActionListener(action);

        return button;
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
                "TRANG CHU",
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
                "TRUY CAP NHANH"
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
                "Thanh toan",
                "F4"
        );

        addQuickButton(
                panel,
                "Quan ly lich hen Spa",
                "F5"
        );

        addQuickButton(
                panel,
                "Quan ly san pham",
                "F6"
        );

        addQuickButton(
                panel,
                "Quan ly khach hang",
                "F7"
        );

        addQuickButton(
                panel,
                "Bao cao doanh thu",
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
                e -> hienThongBao(text)
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
    // POPUP KHO
    // =========================================================

    private void hienPopupKho(
            JButton source
    ) {

        JPopupMenu popup = new JPopupMenu();

        themMenuItem(
                popup,
                "Danh muc"
        );

        themMenuItem(
                popup,
                "San pham"
        );

        themMenuItem(
                popup,
                "Nhap kho"
        );

        themMenuItem(
                popup,
                "Kiem ke"
        );

        popup.show(
                source,
                0,
                source.getHeight()
        );
    }

    // =========================================================
    // POPUP SPA
    // =========================================================

    private void hienPopupSpa(
            JButton source
    ) {

        JPopupMenu popup = new JPopupMenu();

        themMenuItem(
                popup,
                "Lich hen"
        );

        themMenuItem(
                popup,
                "Thu cung"
        );

        themMenuItem(
                popup,
                "Khach hang"
        );

        themMenuItem(
                popup,
                "Phieu dich vu"
        );

        popup.show(
                source,
                0,
                source.getHeight()
        );
    }

    // =========================================================
    // POPUP HOA DON
    // =========================================================

    private void hienPopupHoaDon(
            JButton source
    ) {

        JPopupMenu popup = new JPopupMenu();

        themMenuItem(
                popup,
                "Hoa don"
        );

        themMenuItem(
                popup,
                "Thanh toan"
        );

        popup.show(
                source,
                0,
                source.getHeight()
        );
    }

    // =========================================================
    // POPUP BAO CAO
    // =========================================================

    private void hienPopupBaoCao(
            JButton source
    ) {

        JPopupMenu popup = new JPopupMenu();

        themMenuItem(
                popup,
                "Theo ngay"
        );

        themMenuItem(
                popup,
                "Theo thang"
        );

        themMenuItem(
                popup,
                "Theo nam"
        );

        popup.show(
                source,
                0,
                source.getHeight()
        );
    }

    // =========================================================
    // POPUP HE THONG
    // =========================================================

    private void hienPopupHeThong(
            JButton source
    ) {

        JPopupMenu popup = new JPopupMenu();

        // QUAN LY TAI KHOAN
        JMenuItem taiKhoan =
                new JMenuItem(
                        "Quan ly tai khoan"
                );

        taiKhoan.addActionListener(
                e -> moQuanLyTaiKhoan()
        );

        popup.add(taiKhoan);

 
        popup.show(
                source,
                0,
                source.getHeight()
        );
    }          

    // =========================================================
    // MO QUAN LY TAI KHOAN
    // =========================================================

    private void moQuanLyTaiKhoan() {

        JFrame frame =
                new JFrame(
                        "Quan ly tai khoan"
                );

        frame.setSize(
                1100,
                650
        );

        frame.setLocationRelativeTo(
                this
        );

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        frame.add(
                new QuanLyTaiKhoan()
        );

        frame.setVisible(true);
    }

    // =========================================================
    // MO QUAN LY PHAN QUYEN
    // =========================================================

 /*   private void moQuanLyPhanQuyen() {

        JFrame frame =
                new JFrame(
                        "Quan ly phan quyen"
                );

        frame.setSize(
                1100,
                650
        );

        frame.setLocationRelativeTo(
                this
        );

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        frame.add(
                new QuanLyPhanQuyen()
        );

        frame.setVisible(true);
    }
*/
    // =========================================================
    // MO QUAN LY CA LAM
    // =========================================================

/*    private void moQuanLyCaLam() {

        JFrame frame =
                new JFrame(
                        "Quan ly ca lam"
                );

        frame.setSize(
                1100,
                650
        );

        frame.setLocationRelativeTo(
                this
        );

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        frame.add(
                new QuanLyCaLam()
        );

        frame.setVisible(true);
    }
*/
    // =========================================================
    // MENU ITEM CHUNG
    // =========================================================

    private void themMenuItem(
            JPopupMenu popup,
            String text
    ) {

        JMenuItem item =
                new JMenuItem(text);

        item.addActionListener(
                e -> hienThongBao(text)
        );

        popup.add(item);
    }

    // =========================================================
    // THONG TIN NHAN VIEN
    // =========================================================

    private void hienThongTinNhanVien(
            JButton source
    ) {

        JPopupMenu popup =
                new JPopupMenu();

        JPanel info =
                new JPanel(
                        new GridLayout(
                                0,
                                2,
                                15,
                                8
                        )
                );

        info.setBorder(
                new EmptyBorder(
                        12,
                        12,
                        12,
                        12
                )
        );

        themThongTin(
                info,
                "Ma NV",
                maNV
        );

        themThongTin(
                info,
                "Ho ten",
                hoTen
        );

        themThongTin(
                info,
                "So dien thoai",
                "01239415224"
        );

        themThongTin(
                info,
                "Email",
                "dangdinhan01@gmail.com"
        );

        themThongTin(
                info,
                "Gioi tinh",
                "Nam"
        );

        themThongTin(
                info,
                "Vai tro",
                vaiTro
        );

        themThongTin(
                info,
                "Trang thai",
                "Dang hoat dong"
        );

        popup.add(info);

        popup.addSeparator();

        JMenuItem dangXuat =
                new JMenuItem(
                        "Dang xuat"
                );

        dangXuat.addActionListener(
                e -> {

                    dispose();

                    new DangNhap()
                            .setVisible(true);
                }
        );

        popup.add(dangXuat);

        popup.show(
                source,
                -popup.getPreferredSize().width
                        + source.getWidth(),
                source.getHeight()
        );
    }

    // =========================================================
    // HIEN THONG TIN
    // =========================================================

    private void themThongTin(
            JPanel panel,
            String label,
            String value
    ) {

        JLabel lb =
                new JLabel(label);

        lb.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        JLabel val =
                new JLabel(value);

        panel.add(lb);

        panel.add(val);
    }

    // =========================================================
    // THONG BAO
    // =========================================================

    private void hienThongBao(
            String tenChucNang
    ) {

        JOptionPane.showMessageDialog(
                this,
                "Ban dang mo chuc nang: "
                        + tenChucNang,
                "Pet Shop Pro",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}