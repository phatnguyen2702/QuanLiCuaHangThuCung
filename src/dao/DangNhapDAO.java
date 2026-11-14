package dao;

import ConnectDB.connectDB;
import control.DangNhapControl;
import entity.NhanVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

                boolean dungTaiKhoan = DangNhapControl.taoTaiKhoan(hoTen, maNhanVien)
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
}
