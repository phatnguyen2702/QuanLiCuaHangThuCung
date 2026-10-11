package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Thanh menu tren cung (giong header cua TrangChu), dung chung cho moi man hinh. */
public class NavHeader extends JPanel {

    private final JFrame cha;

    /** mucDangChon: "trangchu" hoac "kho" - nut nao duoc to sang. */
    public NavHeader(JFrame cha, String mucDangChon) {

        this.cha = cha;

        setLayout(new BorderLayout());
        setBackground(UIStyle.NAVY);
        setBorder(new EmptyBorder(5, 18, 5, 18));

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        menu.setOpaque(false);

        JLabel logo = new JLabel("PET SHOP PRO");
        logo.setForeground(Color.WHITE);
        logo.setFont(UIStyle.font(Font.BOLD, 16));
        menu.add(logo);

        menu.add(nutMenu("Trang chủ", "trangchu".equals(mucDangChon),
                e -> DieuHuong.moTrangChu(cha)));

        menu.add(nutMenu("Kho & Danh mục", "kho".equals(mucDangChon),
                e -> popupKho((JButton) e.getSource())));

        menu.add(nutMenu("Spa & Lịch hẹn", false,
                e -> popupThongBao((JButton) e.getSource(),
                        "Lịch hẹn", "Thú cưng", "Khách hàng", "Phiếu dịch vụ")));

        menu.add(nutMenu("Hóa Đơn", false,
                e -> popupThongBao((JButton) e.getSource(),
                        "Hóa đơn", "Thanh toán")));

        menu.add(nutMenu("Báo cáo", false,
                e -> popupThongBao((JButton) e.getSource(),
                        "Theo ngày", "Theo tháng", "Theo năm")));

        menu.add(nutMenu("Hệ thống", false,
                e -> popupHeThong((JButton) e.getSource())));

        menu.add(nutMenu("Cài đặt", false,
                e -> thongBao("Cài đặt")));

        add(menu, BorderLayout.WEST);

        JButton btnNhanVien = nutMenu(PhienLamViec.hoTen + "  ●", false,
                e -> hienThongTinNhanVien((JButton) e.getSource()));

        add(btnNhanVien, BorderLayout.EAST);
    }

    // =========================================================
    // NUT MENU
    // =========================================================

    private JButton nutMenu(String text, boolean dangChon,
                            java.awt.event.ActionListener hanhDong) {

        JButton b = new JButton(text);

        b.setForeground(Color.WHITE);
        b.setBackground(dangChon ? UIStyle.ACTIVE_BLUE : UIStyle.NAVY);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFont(UIStyle.font(Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(hanhDong);

        return b;
    }

    // =========================================================
    // POPUP
    // =========================================================

    private void popupKho(JButton nguon) {

        String[] ten = {"Danh mục", "Sản phẩm", "Nhập kho", "Kiểm kê kho"};

        JPopupMenu popup = new JPopupMenu();

        for (int i = 0; i < ten.length; i++) {
            final int tab = i;
            JMenuItem item = new JMenuItem(ten[i]);
            item.addActionListener(e -> DieuHuong.moKho(cha, tab));
            popup.add(item);
        }

        popup.show(nguon, 0, nguon.getHeight());
    }

    // Cac muc chua lam: giu nguyen kieu thong bao cua TrangChu
    private void popupThongBao(JButton nguon, String... muc) {

        JPopupMenu popup = new JPopupMenu();

        for (String m : muc) {
            JMenuItem item = new JMenuItem(m);
            item.addActionListener(e -> thongBao(m));
            popup.add(item);
        }

        popup.show(nguon, 0, nguon.getHeight());
    }

    private void popupHeThong(JButton nguon) {

        JPopupMenu popup = new JPopupMenu();

        JMenuItem taiKhoan = new JMenuItem("Quản lý tài khoản");
        taiKhoan.addActionListener(e -> moQuanLyTaiKhoan());
        popup.add(taiKhoan);

        popup.show(nguon, 0, nguon.getHeight());
    }

    private void moQuanLyTaiKhoan() {

        JFrame frame = new JFrame("Quản lý tài khoản");
        frame.setSize(1100, 650);
        frame.setLocationRelativeTo(cha);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new QuanLyTaiKhoan());
        frame.setVisible(true);
    }

    private void thongBao(String tenChucNang) {
        JOptionPane.showMessageDialog(cha,
                "Bạn đang mở chức năng: " + tenChucNang,
                "Pet Shop Pro",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================
    // THONG TIN NHAN VIEN
    // =========================================================

    private void hienThongTinNhanVien(JButton nguon) {

        JPopupMenu popup = new JPopupMenu();

        JPanel info = new JPanel(new GridLayout(0, 2, 15, 8));
        info.setBorder(new EmptyBorder(12, 12, 12, 12));

        themThongTin(info, "Mã NV", PhienLamViec.maNV);
        themThongTin(info, "Họ tên", PhienLamViec.hoTen);
        themThongTin(info, "Vai trò", PhienLamViec.vaiTro);
        themThongTin(info, "Trạng thái", "Đang hoạt động");

        popup.add(info);
        popup.addSeparator();

        JMenuItem dangXuat = new JMenuItem("Đăng xuất");
        dangXuat.addActionListener(e -> DieuHuong.dangXuat(cha));
        popup.add(dangXuat);

        popup.show(nguon,
                -popup.getPreferredSize().width + nguon.getWidth(),
                nguon.getHeight());
    }

    private void themThongTin(JPanel panel, String nhan, String giaTri) {
        JLabel lb = new JLabel(nhan);
        lb.setFont(UIStyle.font(Font.BOLD, 12));
        panel.add(lb);
        panel.add(new JLabel(giaTri));
    }
}
