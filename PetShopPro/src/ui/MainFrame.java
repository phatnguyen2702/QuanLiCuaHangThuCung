package ui;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import data.DuLieu;

public class MainFrame extends JFrame {
    private static final String[] MENU = {"Trang chủ", "Kho & Danh mục", "Spa & Lịch hẹn", "Hóa Đơn", "Báo cáo", "Cài đặt"};
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel lblClock = new JLabel();

    public MainFrame() {
        super("Pet Shop Pro");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 780);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new BorderLayout());
        top.add(navBar(), BorderLayout.NORTH);
        top.add(infoBar(), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        content.setBackground(Theme.BG);
        for (String m : MENU) content.add(m.equals("Kho & Danh mục") ? new KhoPanel() : placeholder(m), m);
        add(content, BorderLayout.CENTER);
        cards.show(content, "Kho & Danh mục");
    }

    private JPanel navBar() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        nav.setBackground(Theme.NAVY);
        JLabel logo = new JLabel("  PET SHOP PRO   ");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17)); logo.setForeground(Color.WHITE);
        logo.setIcon(new Icon() { // logo tròn màu cam
            public void paintIcon(Component c, Graphics g, int x, int y) { g.setColor(Theme.ORANGE); g.fillOval(x, y, 30, 30); }
            public int getIconWidth() { return 30; }
            public int getIconHeight() { return 30; }
        });
        nav.add(logo);
        ButtonGroup group = new ButtonGroup();
        for (String m : MENU) {
            JToggleButton b = new JToggleButton(m, m.equals("Kho & Danh mục"));
            b.setFont(Theme.B); b.setForeground(Color.WHITE); b.setBackground(Theme.NAVY);
            b.setFocusPainted(false); b.setBorder(new EmptyBorder(8, 14, 8, 14));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addChangeListener(e -> b.setBackground(b.isSelected() ? new Color(0x2A, 0x4A, 0x7A) : Theme.NAVY));
            b.addActionListener(e -> cards.show(content, m));
            group.add(b); nav.add(b);
        }
        return nav;
    }

    private JPanel infoBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0xF5, 0xF7, 0xFA));
        bar.setBorder(new EmptyBorder(8, 20, 8, 20));
        lblClock.setFont(Theme.B);
        JLabel user = new JLabel(DuLieu.nguoiDung.getHoTen() + " · " + DuLieu.nguoiDung.getVaiTro());
        user.setFont(Theme.B);
        bar.add(lblClock, BorderLayout.WEST);
        bar.add(user, BorderLayout.EAST);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss | d/M/yyyy");
        Timer t = new Timer(1000, e -> lblClock.setText(LocalDateTime.now().format(f)));
        t.setInitialDelay(0); t.start();
        return bar;
    }

    private JPanel placeholder(String name) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG);
        JLabel l = new JLabel(name + " — đang phát triển");
        l.setFont(Theme.H1); l.setForeground(Color.GRAY);
        p.add(l);
        return p;
    }
}
