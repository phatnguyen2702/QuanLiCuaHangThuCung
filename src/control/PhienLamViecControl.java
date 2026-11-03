package control;

/** Dữ liệu phiên đăng nhập cho nghiệp vụ kiểm tra quyền; không phụ thuộc UI. */
public final class PhienLamViecControl {
    private static volatile String maNV;
    private static volatile String vaiTro;
    private PhienLamViecControl() {}
    public static void dat(String ma, String role) {
        maNV = ma;
        vaiTro = role;
    }
    public static String maNV() { return maNV; }
    public static String vaiTro() { return vaiTro; }
}
