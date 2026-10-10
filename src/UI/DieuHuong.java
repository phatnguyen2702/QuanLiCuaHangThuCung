package UI;

import javax.swing.*;

/**
 * Chuyen man hinh giong Dang nhap -> Trang chu:
 * mo khung moi o dung vi tri/kich thuoc cu roi dong khung cu,
 * khong de cua so moi len tren cua so cu.
 */
public final class DieuHuong {

    private DieuHuong() {
    }

    public static void moTrangChu(JFrame tu) {
        chuyen(tu, new TrangChu(
                PhienLamViec.maNV,
                PhienLamViec.hoTen,
                PhienLamViec.vaiTro));
    }

    /** tab: 0 Danh muc, 1 San pham, 2 Nhap kho, 3 Kiem ke kho. */
    public static void moKho(JFrame tu, int tab) {

        // Dang o trong Kho roi thi chi doi noi dung ben trong
        if (tu instanceof KhoFrame kho) {
            kho.chuyenTab(tab);
            return;
        }

        chuyen(tu, new KhoFrame(tab));
    }

    public static void dangXuat(JFrame tu) {
        tu.dispose();
        new DangNhap().setVisible(true);
    }

    private static void chuyen(JFrame tu, JFrame den) {
        den.setBounds(tu.getBounds());
        den.setExtendedState(tu.getExtendedState());
        den.setVisible(true);
        tu.dispose();
    }
}
