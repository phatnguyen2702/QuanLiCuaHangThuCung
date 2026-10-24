package UI;

import entity.ThanhToan;
import java.util.ArrayList;
import java.util.List;

/** Luu nhan vien dang dang nhap de cac man hinh dung chung. */
public final class PhienLamViec {

    private static final List<ThanhToan> GIO_HANG = new ArrayList<>();

    public static String maNV = null;
    public static String hoTen = null;
    public static String vaiTro = null;

    private PhienLamViec() {
    }

    public static void dat(String ma, String ten, String role) {
        // Mỗi lần mở phiên đăng nhập mới thì bắt đầu giỏ hàng mới.
        GIO_HANG.clear();
        maNV = ma;
        hoTen = ten;
        vaiTro = role;
    }

    /** Giỏ hàng dùng chung giữa màn hình gợi ý và màn hình thanh toán. */
    public static List<ThanhToan> gioHang() {
        return GIO_HANG;
    }
}
