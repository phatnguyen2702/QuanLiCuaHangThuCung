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
        // SUA KHACH HANG (+ doi hang the neu hangThe != null)
        // - Khach da co the: cap nhat hang cua the moi nhat.
        // - Khach chua co the: tao the moi (ghi nhan vien dang dang nhap).
        // =========================================================

        public boolean sua(KhachHang kh, String hangThe, String maNhanVien) {

                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        con.setAutoCommit(false);

                        try {

                                try (PreparedStatement ps = con.prepareStatement(
                                                "UPDATE KhachHang SET hoTen = ?, soDienThoai = ?, email = ?, "
                                                                + "diaChi = ?, diemTichLuy = ? WHERE maKhachHang = ?")) {

                                        ps.setString(1, kh.getHoTen());
                                        ps.setString(2, kh.getSoDienThoai());
                                        ps.setString(3, kh.getEmail());
                                        ps.setString(4, kh.getDiaChi());
                                        ps.setInt(5, kh.getDiemTichLuy());
                                        ps.setString(6, kh.getMaKhachHang());

                                        if (ps.executeUpdate() == 0) {

                                                con.rollback();

                                                return false;
                                        }
                                }

                                if (hangThe != null) {

                                        String maThe = null;

                                        try (PreparedStatement ps = con.prepareStatement(
                                                        "SELECT TOP 1 maThe FROM TheThanhVien WHERE maKhachHang = ? "
                                                                        + "ORDER BY ngayCap DESC, maThe DESC")) {

                                                ps.setString(1, kh.getMaKhachHang());

                                                try (ResultSet rs = ps.executeQuery()) {
                                                        if (rs.next()) {
                                                                maThe = rs.getString(1);
                                                        }
                                                }
                                        }

                                        if (maThe != null) {

                                                try (PreparedStatement ps = con.prepareStatement(
                                                                "UPDATE TheThanhVien SET hangThe = ?, "
                                                                                + "chietKhauPhanTram = ? WHERE maThe = ?")) {

                                                        ps.setString(1, hangThe);
                                                        ps.setDouble(2, chietKhau(hangThe));
                                                        ps.setString(3, maThe);
                                                        ps.executeUpdate();
                                                }

                                        } else {

                                                try (PreparedStatement ps = con.prepareStatement(
                                                                "INSERT INTO TheThanhVien "
                                                                                + "(maThe, hangThe, chietKhauPhanTram, ngayCap, maKhachHang, maNhanVien) "
                                                                                + "VALUES (?, ?, ?, CAST(GETDATE() AS DATE), ?, ?)")) {

                                                        ps.setString(1, maTheMoi(con));
                                                        ps.setString(2, hangThe);
                                                        ps.setDouble(3, chietKhau(hangThe));
                                                        ps.setString(4, kh.getMaKhachHang());
                                                        ps.setString(5, maNhanVien);
                                                        ps.executeUpdate();
                                                }
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

        // =========================================================
        // HAM PHU
        // =========================================================

        private static double chietKhau(String hangThe) {

                switch (hangThe) {
                        case "Platinum":
                                return 15;
                        case "Gold":
                                return 10;
                        default:
                                return 5;
                }
        }

        private static String maTheMoi(Connection con) throws SQLException {

                try (PreparedStatement ps = con.prepareStatement(
                                "SELECT MAX(maThe) FROM TheThanhVien WHERE maThe LIKE 'THE%'");
                                ResultSet rs = ps.executeQuery()) {

                        int n = 0;

                        if (rs.next() && rs.getString(1) != null) {

                                try {
                                        n = Integer.parseInt(rs.getString(1).substring(3));
                                } catch (NumberFormatException e) {
                                        n = 0;
                                }
                        }

                        return "THE" + String.format("%03d", n + 1);
                }
        }
}