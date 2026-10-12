package UI;

import javax.swing.JFrame;

public final class DieuHuong {

    private DieuHuong() {
    }

    public static void moTrangChu(JFrame cha) {
        if (cha instanceof TrangChu trangChu) {
            trangChu.setVisible(true);
            trangChu.toFront();
            return;
        }

        if (cha != null) {
            cha.dispose();
        }

        TrangChu frame = new TrangChu("NV001", "Nhân viên demo", "Quản lý");
        frame.setVisible(true);
    }

    public static void moKho(JFrame cha, int tab) {
        if (cha instanceof TrangChu trangChu) {
            trangChu.chuyenKho(tab);
            trangChu.setVisible(true);
            trangChu.toFront();
            return;
        }

        if (cha != null) {
            cha.dispose();
        }

        TrangChu frame = new TrangChu("NV001", "Nhân viên demo", "Quản lý");
        frame.chuyenKho(tab);
        frame.setVisible(true);
    }

    public static void dangXuat(JFrame cha) {
        if (cha != null) {
            cha.dispose();
        }
        new DangNhap().setVisible(true);
    }
}
