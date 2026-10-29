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
    // NAVY: Màu dùng cho giao diện: xanh đậm.
    private static final Color NAVY = new Color(18, 48, 82);
    // BLUE: Màu dùng cho giao diện: xanh dương.
    private static final Color BLUE = new Color(229, 241, 252);

    // Ẩn hàm khởi tạo vì lớp chỉ cung cấp các hàm tiện ích dùng chung.
    private LichChonNgay() {
    }

    // Chọn ngày.
    // Tham số cha: thành phần cha.
    // Tham số ngayBanDau: ngày chọn ban đầu.
    // Tham số gioiHanQuaKhu: có ngăn chọn ngày trong quá khứ.
    // Kết quả: đối tượng/giá trị kiểu LocalDate.
    public static LocalDate chonNgay(Component cha, LocalDate ngayBanDau, boolean gioiHanQuaKhu) {
        Window winOwner = SwingUtilities.getWindowAncestor(cha);
        // winDlg: Hộp thoại hộp thoại.
        JDialog winDlg = new JDialog(winOwner, "Chọn ngày làm việc", Dialog.ModalityType.APPLICATION_MODAL);
        winDlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        winDlg.setLayout(new BorderLayout(0, 10));
        // pnlPnl: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlPnl = new JPanel(new BorderLayout(0, 10));
        pnlPnl.setBackground(Color.WHITE);
        pnlPnl.setBorder(new EmptyBorder(15, 15, 15, 15));
        winDlg.add(pnlPnl, BorderLayout.CENTER);

        // today: Lưu hôm nay dưới dạng dữ liệu thời gian.
        LocalDate today = LocalDate.now();
        // selected: Lưu trạng thái được chọn dưới dạng dữ liệu thời gian.
        LocalDate selected = ngayBanDau != null ? ngayBanDau : today;
        if (gioiHanQuaKhu && selected.isBefore(today))
            selected = today;
        // ketQua: Mảng lưu kết quả.
        final LocalDate[] ketQua = { null };
        // thang: Mảng lưu tháng đang hiển thị.
        final YearMonth[] thang = { YearMonth.from(selected) };

        // pnlHeader: Khung chứa thành phần giao diện đầu trang.
        JPanel pnlHeader = new JPanel(new BorderLayout(8, 0));
        pnlHeader.setBackground(Color.WHITE);
        // btnTruoc: Nút thao tác: ◀.
        JButton btnTruoc = new JButton("◀");
        // btnSau: Nút thao tác: ▶.
        JButton btnSau = new JButton("▶");
        // lblThang: Nhãn hiển thị tháng đang hiển thị.
        JLabel lblThang = new JLabel("", SwingConstants.CENTER);
        lblThang.setForeground(NAVY);
        lblThang.setFont(new Font("Segoe UI", Font.BOLD, 17));
        pnlHeader.add(btnTruoc, BorderLayout.WEST);
        pnlHeader.add(lblThang, BorderLayout.CENTER);
        pnlHeader.add(btnSau, BorderLayout.EAST);
        pnlPnl.add(pnlHeader, BorderLayout.NORTH);

        // pnlNgay: Khung chứa thành phần giao diện ngày.
        JPanel pnlNgay = new JPanel(new GridLayout(7, 7, 4, 4));
        pnlNgay.setBackground(Color.WHITE);
        pnlPnl.add(pnlNgay, BorderLayout.CENTER);

        // veLich: Mảng lưu về lịch.
        Runnable[] veLich = new Runnable[1];
        veLich[0] = () -> {
            pnlNgay.removeAll();
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblThang.setText("Tháng " + thang[0].getMonthValue() + " / " + thang[0].getYear());
            // thu: Mảng lưu thú.
            String[] thu = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
            for (String t : thu) {
                // lblLbl: Nhãn hiển thị nhãn.
                JLabel lblLbl = new JLabel(t, SwingConstants.CENTER);
                lblLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblLbl.setForeground(NAVY);
                pnlNgay.add(lblLbl);
            }
            int batDau = thang[0].atDay(1).getDayOfWeek().getValue() - 1;
            for (int i = 0; i < 42; i++) {
                int ngay = i - batDau + 1;
                if (ngay < 1 || ngay > thang[0].lengthOfMonth()) {
                    pnlNgay.add(new JLabel(""));
                    continue;
                }
                // date: Lưu ngày dưới dạng dữ liệu thời gian.
                LocalDate date = thang[0].atDay(ngay);
                // btnNgay: Nút thao tác ngày.
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
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnNgay.addActionListener(e -> {
                    ketQua[0] = date;
                    winDlg.dispose();
                });
                pnlNgay.add(btnNgay);
            }
            btnTruoc.setEnabled(!gioiHanQuaKhu || thang[0].isAfter(YearMonth.from(today)));
            pnlNgay.revalidate();
            pnlNgay.repaint();
        };
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTruoc.addActionListener(e -> {
            thang[0] = thang[0].minusMonths(1);
            veLich[0].run();
        });
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnSau.addActionListener(e -> {
            thang[0] = thang[0].plusMonths(1);
            veLich[0].run();
        });
        veLich[0].run();

        // pnlCuoi: Khung chứa thành phần giao diện cuối.
        JPanel pnlCuoi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlCuoi.setBackground(Color.WHITE);
        // btnHomNay: Nút thao tác: Hôm nay.
        JButton btnHomNay = new JButton("Hôm nay");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnHomNay.addActionListener(e -> {
            ketQua[0] = today;
            winDlg.dispose();
        });
        // btnHuy: Nút thao tác: Hủy.
        JButton btnHuy = new JButton("Hủy");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnHuy.addActionListener(e -> winDlg.dispose());
        pnlCuoi.add(btnHomNay);
        pnlCuoi.add(btnHuy);
        pnlPnl.add(pnlCuoi, BorderLayout.SOUTH);

        winDlg.pack();
        winDlg.setMinimumSize(new Dimension(340, 370));
        winDlg.setLocationRelativeTo(cha);
        winDlg.setVisible(true);
        return ketQua[0];
    }
}
