package control;

import dao.DangNhapDAO;
import entity.NhanVien;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Locale;

public final class DangNhapControl {
    private final DangNhapDAO dao = new DangNhapDAO();

    public NhanVien dangNhap(String taiKhoanNhap, char[] matKhau) throws SQLException {
        String ten = taiKhoanNhap == null ? "" : taiKhoanNhap.trim().toLowerCase(java.util.Locale.ROOT);
        if (ten.isEmpty())
            throw new IllegalArgumentException("Vui lòng nhập tài khoản!");
        if (matKhau == null || matKhau.length == 0)
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu!");
        return dao.xacThuc(ten, new String(matKhau));
    }

    public String chuanHoaTaiKhoan(String text) {
        return (text == null ? "" : text).toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    public static String taoTaiKhoan(String hoTen, String maNhanVien) {
        if (hoTen == null || maNhanVien == null || maNhanVien.length() < 3)
            return "";

        String ten = Normalizer.normalize(hoTen, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "");
        return ten + maNhanVien.substring(maNhanVien.length() - 3);
    }

}
