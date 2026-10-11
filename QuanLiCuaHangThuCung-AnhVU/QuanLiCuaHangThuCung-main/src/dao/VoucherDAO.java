package dao;

import entity.Voucher;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class VoucherDAO {

        // =========================================================
        // LAY DANH SACH (loc theo ma / ten), kem so luot da dung
        // =========================================================

        public ArrayList<Voucher> getAll(String tuKhoa) {

                ArrayList<Voucher> list = new ArrayList<>();

                String sql = "SELECT v.maVoucher, v.tenVoucher, v.phanTramGiam, v.dieuKien, v.trangThai, "
                                + "v.ngayBatDau, v.ngayKetThuc, v.soLuongToiDa, "
                                + "(SELECT COUNT(*) FROM HoaDon h WHERE h.maVoucher = v.maVoucher) AS daDung "
                                + "FROM Vocher v "
                                + "WHERE (v.maVoucher LIKE ? OR v.tenVoucher LIKE ?) "
                                + "ORDER BY v.maVoucher";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        String kw = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";

                        ps.setString(1, kw);
                        ps.setString(2, kw);

                        try (ResultSet rs = ps.executeQuery()) {

                                while (rs.next()) {

                                        Date bd = rs.getDate("ngayBatDau");
                                        Date kt = rs.getDate("ngayKetThuc");

                                        int sl = rs.getInt("soLuongToiDa");

                                        Integer soLuong = rs.wasNull() ? null : sl;

                                        Voucher v = new Voucher(
                                                        rs.getString("maVoucher"),
                                                        rs.getString("tenVoucher"),
                                                        rs.getDouble("phanTramGiam"),
                                                        rs.getString("dieuKien"),
                                                        rs.getBoolean("trangThai"),
                                                        bd == null ? null : bd.toLocalDate(),
                                                        kt == null ? null : kt.toLocalDate(),
                                                        soLuong);

                                        v.setDaDung(rs.getInt("daDung"));

                                        list.add(v);
                                }
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list;
        }

        // =========================================================
        // THONG KE HOA DON CO VOUCHER:
        // {luot dung trong thang, tong gia tri uu dai, % hoa don dung voucher}
        // =========================================================

        public double[] thongKeHoaDon() {

                double[] kq = new double[3];

                String sql = "SELECT "
                                + "(SELECT COUNT(*) FROM HoaDon WHERE maVoucher IS NOT NULL "
                                + "   AND YEAR(ngayLap) = YEAR(GETDATE()) AND MONTH(ngayLap) = MONTH(GETDATE())), "
                                + "(SELECT ISNULL(SUM(ct.soLuong * ct.donGia * ISNULL(ct.phanTramGiam, 0) / 100.0), 0) "
                                + "   FROM HoaDon h JOIN ChiTietHoaDon ct ON ct.maHoaDon = h.maHoaDon "
                                + "   WHERE h.maVoucher IS NOT NULL), "
                                + "(SELECT CASE WHEN COUNT(*) = 0 THEN 0 "
                                + "   ELSE 100.0 * SUM(CASE WHEN maVoucher IS NOT NULL THEN 1 ELSE 0 END) / COUNT(*) END "
                                + "   FROM HoaDon)";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {

                                for (int i = 0; i < 3; i++) {
                                        kq[i] = rs.getDouble(i + 1);
                                }
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return kq;
        }

        // =========================================================
        // THEM (ma tu sinh VC001, VC002...). Tra ve ma moi, null neu loi.
        // =========================================================

        public String them(Voucher v) {

                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        String ma = nextCode(con);

                        String sql = "INSERT INTO Vocher "
                                        + "(maVoucher, tenVoucher, phanTramGiam, dieuKien, trangThai, "
                                        + "ngayBatDau, ngayKetThuc, soLuongToiDa) "
                                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                        try (PreparedStatement ps = con.prepareStatement(sql)) {

                                ps.setString(1, ma);

                                gan(ps, v, 2);

                                ps.executeUpdate();
                        }

                        return ma;

                } catch (SQLException e) {

                        e.printStackTrace();

                        return null;
                }
        }

        // =========================================================
        // SUA
        // =========================================================

        public boolean sua(Voucher v) {

                String sql = "UPDATE Vocher SET tenVoucher = ?, phanTramGiam = ?, dieuKien = ?, "
                                + "trangThai = ?, ngayBatDau = ?, ngayKetThuc = ?, soLuongToiDa = ? "
                                + "WHERE maVoucher = ?";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        gan(ps, v, 1);

                        ps.setString(8, v.getMaVoucher());

                        return ps.executeUpdate() > 0;

                } catch (SQLException e) {

                        e.printStackTrace();

                        return false;
                }
        }

        // =========================================================
        // HUY / KICH HOAT LAI
        // =========================================================

        public boolean doiTrangThai(String maVoucher, boolean hoatDong) {

                String sql = "UPDATE Vocher SET trangThai = ? WHERE maVoucher = ?";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setBoolean(1, hoatDong);
                        ps.setString(2, maVoucher);

                        return ps.executeUpdate() > 0;

                } catch (SQLException e) {

                        e.printStackTrace();

                        return false;
                }
        }

        // =========================================================
        // XOA: null neu xoa duoc, nguoc lai tra ve noi dung loi.
        // Voucher da dung trong hoa don thi khong xoa (nen Huy).
        // =========================================================

        public String xoa(String maVoucher) {

                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        try (PreparedStatement ps = con.prepareStatement(
                                        "SELECT COUNT(*) FROM HoaDon WHERE maVoucher = ?")) {

                                ps.setString(1, maVoucher);

                                try (ResultSet rs = ps.executeQuery()) {

                                        if (rs.next() && rs.getInt(1) > 0) {

                                                return "Voucher da duoc dung trong hoa don nen khong the xoa.\n"
                                                                + "Hay dung nut Huy voucher.";
                                        }
                                }
                        }

                        try (PreparedStatement ps = con.prepareStatement(
                                        "DELETE FROM Vocher WHERE maVoucher = ?")) {

                                ps.setString(1, maVoucher);

                                ps.executeUpdate();
                        }

                        return null;

                } catch (SQLException e) {

                        e.printStackTrace();

                        return "Xoa that bai: " + e.getMessage();
                }
        }

        // =========================================================
        // HAM PHU
        // =========================================================

        // gan 7 tham so: ten, %, dieu kien, trang thai, ngay bd, ngay kt, so luong
        private static void gan(PreparedStatement ps, Voucher v, int batDau) throws SQLException {

                int i = batDau;

                ps.setString(i++, v.getTenVoucher());
                ps.setDouble(i++, v.getPhanTramGiam());
                ps.setString(i++, v.getDieuKien());
                ps.setBoolean(i++, v.isTrangThai());

                if (v.getNgayBatDau() == null) {
                        ps.setNull(i++, Types.DATE);
                } else {
                        ps.setDate(i++, Date.valueOf(v.getNgayBatDau()));
                }

                if (v.getNgayKetThuc() == null) {
                        ps.setNull(i++, Types.DATE);
                } else {
                        ps.setDate(i++, Date.valueOf(v.getNgayKetThuc()));
                }

                if (v.getSoLuongToiDa() == null) {
                        ps.setNull(i++, Types.INTEGER);
                } else {
                        ps.setInt(i++, v.getSoLuongToiDa());
                }
        }

        private static String nextCode(Connection con) throws SQLException {

                try (PreparedStatement ps = con.prepareStatement(
                                "SELECT MAX(maVoucher) FROM Vocher WHERE maVoucher LIKE 'VC%'");
                                ResultSet rs = ps.executeQuery()) {

                        int n = 0;

                        if (rs.next() && rs.getString(1) != null) {

                                try {
                                        n = Integer.parseInt(rs.getString(1).substring(2));
                                } catch (NumberFormatException e) {
                                        n = 0;
                                }
                        }

                        return "VC" + String.format("%03d", n + 1);
                }
        }
}
