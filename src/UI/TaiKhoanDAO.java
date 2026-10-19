package dao;

import entity.TaiKhoan;

import java.sql.*;
import java.util.ArrayList;

public class TaiKhoanDAO {

        /** Xác thực bằng tài khoản suy ra từ họ tên + 3 số cuối mã nhân viên. */
        public TaiKhoan xacThuc(String tenDangNhap, String matKhau) throws SQLException {
                String sql = "SELECT maNhanVien, hoTen, soDienThoai, email, matKhau, gioiTinh, vaiTro FROM NhanVien";
                try (Connection con = ConnectDB.connectDB.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                                String ma = rs.getString("maNhanVien");
                                String ten = rs.getString("hoTen");
                                if (ma == null || ten == null || ma.length() < 3) continue;
                                String normalized = java.text.Normalizer.normalize(ten, java.text.Normalizer.Form.NFD)
                                        .replaceAll("\\p{M}", "").replace("đ", "d").replace("Đ", "D")
                                        .toLowerCase(java.util.Locale.ROOT).replaceAll("\\s+", "")
                                        + ma.substring(ma.length() - 3);
                                if (normalized.equals(tenDangNhap) && rs.getString("matKhau") != null
                                        && rs.getString("matKhau").equals(matKhau)) {
                                        return new TaiKhoan(ma, ten, rs.getString("soDienThoai"), rs.getString("email"),
                                                rs.getString("matKhau"), rs.getString("gioiTinh"), rs.getString("vaiTro"));
                                }
                        }
                }
                return null;
        }

        // =========================================================
        // LAY TAT CA
        // =========================================================

        public ArrayList<TaiKhoan> getAll() {

                ArrayList<TaiKhoan> list = new ArrayList<>();

                String sql = "SELECT maNhanVien, hoTen, "
                                + "soDienThoai, email, matKhau, "
                                + "gioiTinh, vaiTro "
                                + "FROM NhanVien "
                                + "ORDER BY maNhanVien";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();

                                PreparedStatement ps = con.prepareStatement(sql);

                                ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {

                                TaiKhoan tk = new TaiKhoan(
                                                rs.getString("maNhanVien"),
                                                rs.getString("hoTen"),
                                                rs.getString("soDienThoai"),
                                                rs.getString("email"),
                                                rs.getString("matKhau"),
                                                rs.getString("gioiTinh"),
                                                rs.getString("vaiTro"));

                                list.add(tk);
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list;
        }

        // =========================================================
        // THEM
        // =========================================================

        public boolean them(
                        TaiKhoan tk) {

                String sql = "INSERT INTO NhanVien "
                                + "(maNhanVien, hoTen, soDienThoai, "
                                + "email, matKhau, gioiTinh, vaiTro) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

                try (Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, tk.getMaNhanVien());
                        ps.setString(2, tk.getHoTen());
                        ps.setString(3, tk.getSoDienThoai());
                        ps.setString(4, tk.getEmail());
                        ps.setString(5, tk.getMatKhau());
                        ps.setString(6, tk.getGioiTinh());
                        ps.setString(7, tk.getVaiTro());
                        return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                }
        }

        // SUA
        public boolean sua(
                        TaiKhoan tk) {

                String sql = "UPDATE NhanVien SET "
                                + "hoTen = ?, "
                                + "soDienThoai = ?, "
                                + "email = ?, "
                                + "matKhau = ?, "
                                + "gioiTinh = ?, "
                                + "vaiTro = ? "
                                + "WHERE maNhanVien = ?";
                try (
                                Connection con = ConnectDB.connectDB.getConnection();

                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, tk.getHoTen());

                        ps.setString(2, tk.getSoDienThoai());

                        ps.setString(3, tk.getEmail());

                        ps.setString(4, tk.getMatKhau());

                        ps.setString(5, tk.getGioiTinh());

                        ps.setString(6, tk.getVaiTro());

                        ps.setString(7, tk.getMaNhanVien());
                        return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                }
        }

        // XOA
        public boolean xoa(
                        String maNhanVien) {
                String sql = "DELETE FROM NhanVien "
                                + "WHERE maNhanVien = ?";
                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, maNhanVien);
                        return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                }
        }
}