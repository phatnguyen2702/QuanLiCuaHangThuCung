package control;

import entity.ThanhToan;
import java.util.ArrayList;
import java.util.List;

public final class PhienLamViecControl {
    private static final List<ThanhToan> GIO_HANG = new ArrayList<>();
    public static String maNV;
    public static String hoTen;
    public static String vaiTro;

    private PhienLamViecControl() {
    }

    public static void dat(String ma, String ten, String role) {
        GIO_HANG.clear();
        maNV = ma;
        hoTen = ten;
        vaiTro = role;
    }

    public static List<ThanhToan> gioHang() {
        return GIO_HANG;
    }
}
