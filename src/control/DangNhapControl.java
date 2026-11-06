package control;

import dao.NhanVienDAO;
import entity.NhanVien;
import java.sql.SQLException;

public final class DangNhapControl {
    private final NhanVienDAO dao = new NhanVienDAO();

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

}
