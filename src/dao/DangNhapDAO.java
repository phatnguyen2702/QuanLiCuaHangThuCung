package dao;

import ConnectDB.connectDB;
import entity.NhanVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Locale;

public class DangNhapDAO {

    public NhanVien xacThuc(String tenDangNhap, String matKhau) throws SQLException {
        String sql = "SELECT maNhanVien, hoTen, soDienThoai, email, matKhau, gioiTinh, vaiTro FROM NhanVien";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maNhanVien = rs.getString("maNhanVien");
                String hoTen = rs.getString("hoTen");
                if (maNhanVien == null || hoTen == null || maNhanVien.length() < 3)
                    continue;

                boolean dungTaiKhoan = taoTaiKhoanKhongDau(hoTen, maNhanVien)
                        .equalsIgnoreCase(tenDangNhap)
                        || maNhanVien.equalsIgnoreCase(tenDangNhap);
                if (dungTaiKhoan && matKhau != null && matKhau.equals(rs.getString("matKhau"))) {
                    return new NhanVien(
                            maNhanVien,
                            hoTen,
                            rs.getString("soDienThoai"),
                            rs.getString("email"),
                            rs.getString("matKhau"),
                            rs.getString("gioiTinh"),
                            rs.getString("vaiTro"));
                }
            }
        }
        return null;
    }

    private static String taoTaiKhoanKhongDau(String hoTen, String maNhanVien) {
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
