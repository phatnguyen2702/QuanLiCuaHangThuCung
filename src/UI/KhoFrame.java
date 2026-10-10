package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Khung "Kho & Danh muc": giu nguyen header cua Pet Shop Pro,
 * ben duoi la 4 tab Danh muc / San pham / Nhap kho / Kiem ke kho.
 * Doi tab chi doi noi dung ben trong khung, khong mo cua so moi.
 */
public class KhoFrame extends JFrame {

    private static final String[] TEN_TAB =
            {"Danh mục", "Sản phẩm", "Nhập kho", "Kiểm kê kho"};

    private final CardLayout card = new CardLayout();
    private final JPanel noiDung = new JPanel(card);

    private final JButton[] nutTab = new JButton[TEN_TAB.length];
    private final JPanel thanhTab = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 8, 8));

    private final ManHinhKho[] manHinh = new ManHinhKho[TEN_TAB.length];

    private final JLabel lbGio = new JLabel();
    private final javax.swing.Timer dongHo;

    public KhoFrame(int tabBanDau) {

        setTitle("Pet Shop Pro - Kho & Danh mục");
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // ---- phan tren: header + thanh gio + thanh tab ----
        JPanel phanTren = new JPanel(new BorderLayout());
        phanTren.add(new NavHeader(this, "kho"), BorderLayout.NORTH);

        JPanel duoi = new JPanel(new BorderLayout());
        duoi.add(taoThanhGio(), BorderLayout.NORTH);
        duoi.add(taoThanhTab(), BorderLayout.CENTER);
        phanTren.add(duoi, BorderLayout.CENTER);

        add(phanTren, BorderLayout.NORTH);

        // ---- noi dung ----
        noiDung.setBackground(UIStyle.LIGHT_BLUE);
        add(noiDung, BorderLayout.CENTER);

        dongHo = new javax.swing.Timer(1000, e -> capNhatGio());
        dongHo.start();
        capNhatGio();

        chuyenTab(tabBanDau);
    }

    // =========================================================
    // THANH GIO + NHAN VIEN
    // =========================================================

    private JPanel taoThanhGio() {

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIStyle.BAR);
        p.setBorder(new EmptyBorder(10, 25, 10, 25));

        lbGio.setFont(UIStyle.font(Font.BOLD, 12));

        JLabel nv = new JLabel(
                PhienLamViec.hoTen + " · " + PhienLamViec.vaiTro);
        nv.setFont(UIStyle.font(Font.BOLD, 12));

        p.add(lbGio, BorderLayout.WEST);
        p.add(nv, BorderLayout.EAST);

        return p;
    }

    private void capNhatGio() {
        lbGio.setText(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss | dd/MM/yyyy")));
    }

    // =========================================================
    // THANH TAB
    // =========================================================

    private JPanel taoThanhTab() {

        thanhTab.setBackground(Color.WHITE);
        thanhTab.setBorder(new EmptyBorder(0, 17, 0, 17));

        for (int i = 0; i < TEN_TAB.length; i++) {
            final int tab = i;
            nutTab[i] = UIStyle.nutTab(TEN_TAB[i], false);
            nutTab[i].addActionListener(e -> chuyenTab(tab));
            thanhTab.add(nutTab[i]);
        }

        return thanhTab;
    }

    // =========================================================
    // CHUYEN TAB
    // =========================================================

    public void chuyenTab(int tab) {

        if (tab < 0 || tab >= TEN_TAB.length) {
            tab = 0;
        }

        boolean laLanDau = manHinh[tab] == null;

        if (laLanDau) {
            manHinh[tab] = taoManHinh(tab);
            noiDung.add((JPanel) manHinh[tab], "tab" + tab);
        }

        card.show(noiDung, "tab" + tab);

        // Lan dau da tu nap du lieu trong constructor
        if (!laLanDau) {
            manHinh[tab].napLai();
        }

        for (int i = 0; i < nutTab.length; i++) {
            boolean chon = i == tab;
            nutTab[i].setBackground(chon ? UIStyle.ACTIVE_BLUE : Color.WHITE);
            nutTab[i].setForeground(chon ? Color.WHITE : UIStyle.NAVY);
        }

        noiDung.revalidate();
        noiDung.repaint();
    }

    private ManHinhKho taoManHinh(int tab) {
        switch (tab) {
            case 0:
                return new DanhMucPanel();
            case 1:
                return new SanPhamPanel();
            case 2:
                return new NhapKhoPanel();
            default:
                return new KiemKePanel();
        }
    }

    @Override
    public void dispose() {
        dongHo.stop();
        super.dispose();
    }
}
