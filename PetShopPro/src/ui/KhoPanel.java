package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class KhoPanel extends JPanel {
    public KhoPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        CardLayout cl = new CardLayout();
        JPanel body = new JPanel(cl);
        String[] names = {"Danh mục", "Sản phẩm", "Nhập kho", "Kiểm kê kho"};
        JPanel[] screens = {new DanhMucPanel(), new SanPhamPanel(), new PhieuNhapPanel(), new KiemKePanel()};

        JPanel sub = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        sub.setBackground(Color.WHITE);
        sub.setBorder(new EmptyBorder(0, 12, 0, 12));
        ButtonGroup g = new ButtonGroup();
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            body.add(screens[i], names[i]);
            JToggleButton b = new JToggleButton(names[i], i == 0);
            b.setFont(Theme.B); b.setFocusPainted(false); b.setBackground(Color.WHITE);
            b.addChangeListener(e -> { b.setBackground(b.isSelected() ? Theme.NAVY : Color.WHITE); b.setForeground(b.isSelected() ? Color.WHITE : Theme.NAVY); });
            b.addActionListener(e -> {
                cl.show(body, names[idx]);
                if (screens[idx] instanceof Refreshable) ((Refreshable) screens[idx]).refresh();
            });
            b.setForeground(i == 0 ? Color.WHITE : Theme.NAVY); b.setBackground(i == 0 ? Theme.NAVY : Color.WHITE);
            g.add(b); sub.add(b);
        }
        add(sub, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        ((Refreshable) screens[0]).refresh();
    }
}
