package UI;

/** Luu nhan vien dang dang nhap de cac man hinh dung chung. */
public final class PhienLamViec {

    public static String maNV = "NV001";
    public static String hoTen = "Đặng Đình An";
    public static String vaiTro = "Quản lý";

    private PhienLamViec() {
    }

    public static void dat(String ma, String ten, String role) {
        maNV = ma;
        hoTen = ten;
        vaiTro = role;
    }
}
