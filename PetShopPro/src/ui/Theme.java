package ui;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class Theme {
    public static final Color NAVY = new Color(0x12, 0x2B, 0x4F), BG = new Color(0xE6, 0xF1, 0xFC),
            GREEN = new Color(0x1F, 0xA3, 0x5C), ORANGE = new Color(0xF0, 0x8A, 0x6B),
            LINE = new Color(0xD9, 0xE2, 0xEC), RED = new Color(0xD9, 0x3B, 0x3B);
    public static final Font F = new Font("Segoe UI", Font.PLAIN, 13), B = new Font("Segoe UI", Font.BOLD, 13),
            H1 = new Font("Segoe UI", Font.BOLD, 22);
    private static final NumberFormat NF = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    public static String tien(double d) { return NF.format(d) + " ₫"; }

    public static JButton btn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(B); b.setBackground(bg); b.setForeground(fg);
        b.setOpaque(true); b.setContentAreaFilled(true); b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(new LineBorder(bg.equals(Color.WHITE) ? LINE : bg), new EmptyBorder(7, 16, 7, 16)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JPanel card(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(12, 14, 12, 14)));
        return p;
    }

    public static JPanel statCard(String title, JLabel value) {
        JPanel p = card(new GridLayout(2, 1, 0, 2));
        JLabel t = new JLabel(title); t.setFont(F); t.setForeground(Color.GRAY);
        value.setFont(new Font("Segoe UI", Font.BOLD, 20));
        p.add(t); p.add(value);
        return p;
    }

    public static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static JScrollPane table(JTable t) {
        t.setRowHeight(30); t.setFont(F); t.setShowVerticalLines(false);
        t.setGridColor(LINE); t.setSelectionBackground(new Color(0xCF, 0xE3, 0xF8)); t.setSelectionForeground(Color.BLACK);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader h = t.getTableHeader();
        h.setBackground(NAVY); h.setForeground(Color.WHITE); h.setFont(B);
        h.setPreferredSize(new Dimension(0, 34)); h.setReorderingAllowed(false);
        JScrollPane sp = new JScrollPane(t);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(new LineBorder(LINE));
        return sp;
    }

    public static void row(JPanel p, int y, String label, JComponent c) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = y; g.insets = new Insets(6, 6, 6, 6); g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; JLabel l = new JLabel(label); l.setFont(B); p.add(l, g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; p.add(c, g);
    }

    public static void loi(Component c, String msg) {
        JOptionPane.showMessageDialog(c, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean xacNhan(Component c, String msg) {
        return JOptionPane.showConfirmDialog(c, msg, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static JPanel footer(Runnable onSave, Runnable onCancel, String saveText) {
        JButton ok = btn(saveText, GREEN, Color.WHITE), cancel = btn("Hủy", Color.WHITE, NAVY);
        ok.addActionListener(e -> onSave.run());
        cancel.addActionListener(e -> onCancel.run());
        JPanel s = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        s.add(cancel); s.add(ok);
        return s;
    }
}
