package dao;

import entity.KhachHang;

import java.sql.*;
import java.util.ArrayList;

// Cac ham bo sung cho man Quan ly khach hang: xem chi tiet + sua khach hang
public class KhachHangChiTietDAO {

        // =========================================================
        // THU CUNG CUA 1 KHACH: moi phan tu {ten, loai, giong, gioiTinh, canNang}
        // =========================================================

        public ArrayList<String[]> getThuCung(String maKhachHang) {

                ArrayList<String[]> list = new ArrayList<>();

                String sql = "SELECT tenThuCung, loai, giong, gioiTinh, canNang "
                                + "FROM ThuCung WHERE maKhachHang = ? ORDER BY maThuCung";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, maKhachHang);

                        try (ResultSet rs = ps.executeQuery()) {

                                while (rs.next()) {

                                        java.math.BigDecimal kg = rs.getBigDecimal("canNang");

                                        list.add(new String[] {
                                                        rs.getString("tenThuCung"),
                                                        rs.getString("loai"),
                                                        rs.getString("giong"),
                                                        rs.getString("gioiTinh"),
                                                        kg == null ? "" : kg.stripTrailingZeros().toPlainString() });
                                }
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list;
        }

        // =========================================================
        // SDT da thuoc ve khach KHAC chua? (dung khi sua)
        // =========================================================

        public boolean tonTaiSdtKhac(String sdt, String maKhachHang) {

                String sql = "SELECT 1 FROM KhachHang WHERE soDienThoai = ? AND maKhachHang <> ?";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, sdt);
                        ps.setString(2, maKhachHang);

                        try (ResultSet rs = ps.executeQuery()) {
                                return rs.next();
                        }

                } catch (SQLException e) {

                        e.printStackTrace();

                        return false;
                }
        }

        // =========================================================
        // SUA THONG TIN KHACH HANG
        // =========================================================

        public boolean sua(KhachHang kh) {

                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        con.setAutoCommit(false);

                        try {

                                try (PreparedStatement ps = con.prepareStatement(
                                                "UPDATE KhachHang SET hoTen = ?, soDienThoai = ? "
                                                                + "WHERE maKhachHang = ?")) {

                                        ps.setString(1, kh.getHoTen());
                                        ps.setString(2, kh.getSoDienThoai());
                                        ps.setString(3, kh.getMaKhachHang());

                                        if (ps.executeUpdate() == 0) {

                                                con.rollback();

                                                return false;
                                        }
                                }

                                con.commit();

                                return true;

                        } catch (SQLException e) {

                                con.rollback();

                                throw e;
                        }

                } catch (SQLException e) {

                        e.printStackTrace();

                        return false;
                }
        }

}
