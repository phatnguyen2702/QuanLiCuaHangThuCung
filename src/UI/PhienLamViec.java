package UI;

import entity.ThanhToan;
import java.util.ArrayList;
import java.util.List;

/** Luu nhan vien dang dang nhap de cac man hinh dung chung. */
public final class PhienLamViec {

    // GIO_HANG: Lưu hàng; phần tử có kiểu ThanhToan.
    private static final List<ThanhToan> GIO_HANG = new ArrayList<>();

    // maNV: Lưu mã nhân viên.
    public static String maNV = null;
    // hoTen: Lưu họ tên.
    public static String hoTen = null;
    // vaiTro: Lưu vai trò.
    public static String vaiTro = null;

    // Ẩn hàm khởi tạo vì lớp chỉ cung cấp các hàm tiện ích dùng chung.
    private PhienLamViec() {
    }

    // Ghi thông tin nhân viên vào phiên làm việc dùng chung.
    // Tham số ma: mã.
    // Tham số ten: tên.
    // Tham số role: vai trò.
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
