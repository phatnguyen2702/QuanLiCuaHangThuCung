package UI;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Cac ham dung chung cho cac man hinh Khach hang / Lich hen
public class UiKit {

        public static final Color NAVY = new Color(18, 48, 82);
        public static final Color LIGHT_BLUE = new Color(229, 241, 252);
        public static final Color GREEN = new Color(72, 196, 62);
        public static final Color RED = new Color(210, 70, 70);
        public static final Color BORDER = new Color(219, 228, 239);
        public static final Color MUTED = new Color(107, 124, 147);

        public static final Font F = new Font("Segoe UI", Font.PLAIN, 12);
        public static final Font FB = new Font("Segoe UI", Font.BOLD, 12);
        public static final Font BIG = new Font("Segoe UI", Font.BOLD, 20);

        public static JPanel card(LayoutManager lm) {

                JPanel p = new JPanel(lm);

                p.setBackground(Color.WHITE);

                p.setBorder(new CompoundBorder(
                                new LineBorder(BORDER, 1, true),
                                new EmptyBorder(10, 14, 10, 14)));

                return p;
        }

        public static JLabel label(String text, Font font, Color color) {

                JLabel l = new JLabel(text);

                l.setFont(font);

                l.setForeground(color);

                return l;
        }

        public static JButton button(String text, Color bg, Color fg) {

                JButton b = new JButton(text);

                b.setFont(FB);
                b.setBackground(bg);
                b.setForeground(fg);
                b.setOpaque(true);
                b.setFocusPainted(false);
                b.setCursor(new Cursor(Cursor.HAND_CURSOR));

                b.setBorder(new CompoundBorder(
                                new LineBorder(bg.equals(Color.WHITE) ? BORDER : bg, 1, true),
                                new EmptyBorder(7, 16, 7, 16)));

                return b;
        }

        public static void field(JTextField t) {

                t.setFont(F);

                t.setBorder(new CompoundBorder(
                                new LineBorder(BORDER, 1, true),
                                new EmptyBorder(6, 8, 6, 8)));
        }

        // nhan nho phia tren 1 o nhap
        public static JPanel captioned(String caption, JComponent c) {

                JPanel p = new JPanel(new BorderLayout(0, 3));

                p.setOpaque(false);

                p.add(label(caption, FB, NAVY), BorderLayout.NORTH);

                p.add(c, BorderLayout.CENTER);

                return p;
        }

        // 12345 -> 12.345
        public static String tien(long v) {

                return String.format(Locale.US, "%,d", v).replace(',', '.');
        }

        // thanh tieu de: ngay gio (trai), ten man hinh (giua), nut (phai)
        public static JPanel titleBar(String title, JComponent east) {

                JPanel panel = new JPanel(new BorderLayout());

                panel.setBackground(new Color(247, 249, 251));

                panel.setBorder(new EmptyBorder(10, 25, 10, 25));

                JLabel date = new JLabel();

                date.setFont(FB);

                Timer timer = new Timer(1000, e -> date.setText(
                                LocalDateTime.now().format(
                                                DateTimeFormatter.ofPattern("HH:mm:ss | dd/MM/yyyy"))));

                timer.setInitialDelay(0);

                timer.start();

                JLabel lb = new JLabel(title, SwingConstants.CENTER);

                lb.setFont(new Font("Segoe UI", Font.BOLD, 25));

                lb.setForeground(NAVY);

                JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

                west.setOpaque(false);

                west.setPreferredSize(new Dimension(220, 30));

                west.add(date);

                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));

                right.setOpaque(false);

                right.setPreferredSize(new Dimension(220, 30));

                if (east != null) {
                        right.add(east);
                }

                panel.add(west, BorderLayout.WEST);
                panel.add(lb, BorderLayout.CENTER);
                panel.add(right, BorderLayout.EAST);

                return panel;
        }

        public static void styleTable(JTable t) {

                t.setFont(F);
                t.setRowHeight(32);
                t.setShowVerticalLines(false);
                t.setGridColor(BORDER);
                t.setSelectionBackground(new Color(227, 238, 251));
                t.setSelectionForeground(NAVY);
                t.setFillsViewportHeight(true);

                JTableHeader h = t.getTableHeader();

                h.setFont(FB);
                h.setBackground(NAVY);
                h.setForeground(Color.WHITE);
                h.setReorderingAllowed(false);
        }

        // To mau o trang thai lich hen
        public static class TrangThaiRenderer extends DefaultTableCellRenderer {

                @Override
                public Component getTableCellRendererComponent(
                                JTable t, Object v, boolean sel, boolean foc, int r, int c) {

                        JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, r, c);

                        Color bg;
                        Color fg;

                        switch (String.valueOf(v)) {
                                case "Da xac nhan":
                                        bg = new Color(217, 245, 229);
                                        fg = new Color(12, 107, 64);
                                        break;
                                case "Cho xac nhan":
                                        bg = new Color(230, 232, 236);
                                        fg = new Color(51, 51, 51);
                                        break;
                                case "Hoan thanh":
                                        bg = new Color(220, 235, 255);
                                        fg = new Color(31, 95, 191);
                                        break;
                                case "Da huy":
                                        bg = new Color(255, 217, 217);
                                        fg = new Color(180, 35, 24);
                                        break;
                                default:
                                        bg = Color.WHITE;
                                        fg = NAVY;
                        }

                        l.setOpaque(true);
                        l.setBackground(bg);
                        l.setForeground(fg);
                        l.setHorizontalAlignment(SwingConstants.CENTER);

                        return l;
                }
        }
}
