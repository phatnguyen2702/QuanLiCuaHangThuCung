package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Hop thoai lich thuan Swing, khong phu thuoc JCalendar.
 * 
 * @param gioiHanQuaKhu true: chi cho chon hom nay hoac tuong lai.
 */
public final class LichChonNgay {
    private static final Color NAVY = new Color(18, 48, 82);
    private static final Color BLUE = new Color(229, 241, 252);

    private LichChonNgay() {
    }

    public static LocalDate chonNgay(Component cha, LocalDate ngayBanDau, boolean gioiHanQuaKhu) {
        Window owner = SwingUtilities.getWindowAncestor(cha);
        JDialog dlg = new JDialog(owner, "Chọn ngày làm việc", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setLayout(new BorderLayout(0, 10));
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(15, 15, 15, 15));
        dlg.add(pnl, BorderLayout.CENTER);

        LocalDate today = LocalDate.now();
        LocalDate selected = ngayBanDau != null ? ngayBanDau : today;
        if (gioiHanQuaKhu && selected.isBefore(today))
            selected = today;
        final LocalDate[] ketQua = { null };
        final YearMonth[] thang = { YearMonth.from(selected) };

        JPanel pnlHeader = new JPanel(new BorderLayout(8, 0));
        pnlHeader.setBackground(Color.WHITE);
        JButton btnTruoc = new JButton("◀");
        JButton btnSau = new JButton("▶");
        JLabel lblThang = new JLabel("", SwingConstants.CENTER);
        lblThang.setForeground(NAVY);
        lblThang.setFont(new Font("Segoe UI", Font.BOLD, 17));
        pnlHeader.add(btnTruoc, BorderLayout.WEST);
        pnlHeader.add(lblThang, BorderLayout.CENTER);
        pnlHeader.add(btnSau, BorderLayout.EAST);
        pnl.add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlNgay = new JPanel(new GridLayout(7, 7, 4, 4));
        pnlNgay.setBackground(Color.WHITE);
        pnl.add(pnlNgay, BorderLayout.CENTER);

        Runnable[] veLich = new Runnable[1];
        veLich[0] = () -> {
            pnlNgay.removeAll();
            lblThang.setText("Tháng " + thang[0].getMonthValue() + " / " + thang[0].getYear());
            String[] thu = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
            for (String t : thu) {
                JLabel lbl = new JLabel(t, SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(NAVY);
                pnlNgay.add(lbl);
            }
            int batDau = thang[0].atDay(1).getDayOfWeek().getValue() - 1;
            for (int i = 0; i < 42; i++) {
                int ngay = i - batDau + 1;
                if (ngay < 1 || ngay > thang[0].lengthOfMonth()) {
                    pnlNgay.add(new JLabel(""));
                    continue;
                }
                LocalDate date = thang[0].atDay(ngay);
                JButton btnNgay = new JButton(String.valueOf(ngay));
                btnNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btnNgay.setFocusPainted(false);
                btnNgay.setMargin(new Insets(2, 2, 2, 2));
                btnNgay.setPreferredSize(new Dimension(38, 34));
                boolean quaKhu = gioiHanQuaKhu && date.isBefore(today);
                btnNgay.setEnabled(!quaKhu);
                if (date.equals(today)) {
                    btnNgay.setBackground(BLUE);
                    btnNgay.setOpaque(true);
                }
                if (quaKhu)
                    btnNgay.setToolTipText("Không thể chọn ngày đã qua");
                btnNgay.addActionListener(e -> {
                    ketQua[0] = date;
                    dlg.dispose();
                });
                pnlNgay.add(btnNgay);
            }
            btnTruoc.setEnabled(!gioiHanQuaKhu || thang[0].isAfter(YearMonth.from(today)));
            pnlNgay.revalidate();
            pnlNgay.repaint();
        };
        btnTruoc.addActionListener(e -> {
            thang[0] = thang[0].minusMonths(1);
            veLich[0].run();
        });
        btnSau.addActionListener(e -> {
            thang[0] = thang[0].plusMonths(1);
            veLich[0].run();
        });
        veLich[0].run();

        JPanel pnlCuoi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlCuoi.setBackground(Color.WHITE);
        JButton btnHomNay = new JButton("Hôm nay");
        btnHomNay.addActionListener(e -> {
            ketQua[0] = today;
            dlg.dispose();
        });
        JButton btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> dlg.dispose());
        pnlCuoi.add(btnHomNay);
        pnlCuoi.add(btnHuy);
        pnl.add(pnlCuoi, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setMinimumSize(new Dimension(340, 370));
        dlg.setLocationRelativeTo(cha);
        dlg.setVisible(true);
        return ketQua[0];
    }
}
