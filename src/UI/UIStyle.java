package UI;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Mau sac, font, thanh phan dung chung (lay tu giao dien Pet Shop Pro). */
public final class UIStyle {

    public static final Color NAVY = new Color(18, 48, 82);
    public static final Color LIGHT_BLUE = new Color(229, 241, 252);
    public static final Color MENU_BLUE = new Color(207, 222, 235);
    public static final Color ACTIVE_BLUE = new Color(169, 194, 218);
    public static final Color GREEN = new Color(72, 196, 62);
    public static final Color RED = new Color(200, 40, 40);
    public static final Color LINE = new Color(210, 218, 226);
    public static final Color BAR = new Color(247, 249, 251);
    public static final Color READONLY = new Color(235, 235, 235);

    private UIStyle() {
    }

    /** Muc cua combobox: hien ten, giu ma (ma = null nghia la "tat ca"). */
    public static class Muc {
        public final String ma;
        public final String ten;

        public Muc(String ma, String ten) {
            this.ma = ma;
            this.ten = ten;
        }

        @Override
        public String toString() {
            return ten;
        }
    }

    // =========================================================
    // FONT + DINH DANG
    // =========================================================

    public static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    private static final DecimalFormat DINH_DANG_TIEN;

    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
        s.setGroupingSeparator('.');
        DINH_DANG_TIEN = new DecimalFormat("#,##0", s);
    }

    public static String tien(double v) {
        return DINH_DANG_TIEN.format(v) + " ₫";
    }

    /** Doc so tien VND nguoi dung go ("335000", "335.000", "335,000 d"). */
    public static long docTien(String s) {
        String sach = s == null ? "" : s.replaceAll("[\\s.,₫dđ]", "");
        if (!sach.matches("\\d{1,15}")) {
            throw new NumberFormatException(s);
        }
        return Long.parseLong(sach);
    }

    // =========================================================
    // NUT BAM
    // =========================================================

    private static JButton nut(String text, Color nen, Color chu, Color vien) {

        JButton b = new JButton(text);

        b.setFont(font(Font.BOLD, 13));
        b.setBackground(nen);
        b.setForeground(chu);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (vien != null) {
            b.setBorder(new CompoundBorder(
                    new LineBorder(vien),
                    new EmptyBorder(8, 18, 8, 18)));
        } else {
            b.setBorder(new EmptyBorder(9, 19, 9, 19));
        }

        return b;
    }

    public static JButton nutChinh(String text) {
        return nut(text, NAVY, Color.WHITE, null);
    }

    public static JButton nutXanh(String text) {
        return nut(text, GREEN, Color.WHITE, null);
    }

    public static JButton nutPhu(String text) {
        return nut(text, Color.WHITE, NAVY, NAVY);
    }

    public static JButton nutXoa(String text) {
        return nut(text, Color.WHITE, RED, LINE);
    }

    public static JButton nutTab(String text, boolean dangChon) {
        JButton b = nut(text, dangChon ? ACTIVE_BLUE : Color.WHITE,
                dangChon ? Color.WHITE : NAVY, NAVY);
        return b;
    }

    // =========================================================
    // O NHAP / NHAN
    // =========================================================

    public static JLabel nhan(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.BOLD, 13));
        return l;
    }

    public static JTextField o(int cot) {
        JTextField t = new JTextField(cot);
        t.setFont(font(Font.PLAIN, 13));
        return t;
    }

    public static JTextField oChiDoc(String giaTri) {
        JTextField t = o(10);
        t.setText(giaTri);
        t.setEditable(false);
        t.setBackground(READONLY);
        return t;
    }

    /** Them 1 dong "nhan + o nhap" vao form dung GridBagLayout. */
    public static void hang(JPanel form, int dong, String ten,
                            JComponent thanhPhan, boolean cao) {

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = dong;
        g.insets = new Insets(6, 0, 6, 15);
        g.anchor = cao ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;

        g.gridx = 0;
        g.weightx = 0;
        form.add(nhan(ten), g);

        g.gridx = 1;
        g.weightx = 1;
        g.insets = new Insets(6, 0, 6, 0);
        g.fill = GridBagConstraints.HORIZONTAL;
        form.add(thanhPhan, g);
    }

    // =========================================================
    // THE / THONG KE
    // =========================================================

    public static JPanel the() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
                new LineBorder(LINE),
                new EmptyBorder(15, 15, 15, 15)));
        return p;
    }

    public static JPanel theThongKe(String tieuDe, JLabel giaTri) {

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
                new LineBorder(LINE),
                new EmptyBorder(14, 18, 14, 18)));

        JLabel lb = new JLabel(tieuDe);
        lb.setForeground(Color.GRAY);
        lb.setFont(font(Font.PLAIN, 13));
        lb.setAlignmentX(Component.LEFT_ALIGNMENT);

        giaTri.setFont(font(Font.BOLD, 26));
        giaTri.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lb);
        p.add(Box.createVerticalStrut(6));
        p.add(giaTri);

        return p;
    }

    // =========================================================
    // BANG
    // =========================================================

    /** Dinh dang bang theo kieu Pet Shop Pro (header navy, hang cao 32). */
    public static void taoBang(JTable bang, int... cotCanPhai) {

        bang.setRowHeight(32);
        bang.setFont(font(Font.PLAIN, 13));
        bang.setShowGrid(false);
        bang.setShowHorizontalLines(true);
        bang.setGridColor(new Color(225, 230, 236));
        bang.setSelectionBackground(MENU_BLUE);
        bang.setSelectionForeground(Color.BLACK);
        bang.setFillsViewportHeight(true);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = bang.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc,
                    int r, int c) {
                super.getTableCellRendererComponent(t, v, false, false, r, c);
                setOpaque(true);
                setBackground(NAVY);
                setForeground(Color.WHITE);
                setFont(font(Font.BOLD, 13));
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new javax.swing.border.MatteBorder(
                        0, 0, 0, 1, Color.WHITE));
                return this;
            }
        });

        bang.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc,
                    int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, false, r, c);
                setBorder(new EmptyBorder(0, 10, 0, 10));

                boolean phai = false;
                for (int x : cotCanPhai) {
                    if (x == c) {
                        phai = true;
                        break;
                    }
                }
                setHorizontalAlignment(phai ? SwingConstants.RIGHT
                        : SwingConstants.LEFT);
                return this;
            }
        });
    }

    public static JScrollPane cuon(JTable bang) {
        JScrollPane sp = new JScrollPane(bang);
        sp.setBorder(new LineBorder(LINE));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // =========================================================
    // THONG BAO
    // =========================================================

    public static void thongBao(Component cha, String noiDung) {
        JOptionPane.showMessageDialog(cha, noiDung, "Pet Shop Pro",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void canhBao(Component cha, String noiDung) {
        JOptionPane.showMessageDialog(cha, noiDung, "Pet Shop Pro",
                JOptionPane.WARNING_MESSAGE);
    }

    public static boolean xacNhan(Component cha, String noiDung) {
        return JOptionPane.showConfirmDialog(cha, noiDung, "Pet Shop Pro",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void loi(Component cha, Exception ex) {

        String nd;

        if (ex instanceof SQLException sql) {
            if (sql.getErrorCode() == 547) {
                nd = "Không thực hiện được vì dữ liệu này đang được dùng ở nơi khác\n"
                        + "(ví dụ: danh mục còn sản phẩm, sản phẩm đã có phiếu nhập/hóa đơn).";
            } else if (sql.getErrorCode() == 2627 || sql.getErrorCode() == 2601) {
                nd = "Mã này đã tồn tại, vui lòng nhập mã khác.";
            } else {
                nd = "Lỗi cơ sở dữ liệu:\n" + sql.getMessage();
            }
        } else {
            nd = "Có lỗi xảy ra:\n" + ex.getMessage();
        }

        ex.printStackTrace();

        JOptionPane.showMessageDialog(cha, nd, "Pet Shop Pro",
                JOptionPane.ERROR_MESSAGE);
    }

    /** Hang nut Huy / Luu o cuoi hop thoai. */
    public static JPanel hangNut(JButton huy, JButton luu) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        p.add(huy);
        p.add(luu);
        return p;
    }
}
