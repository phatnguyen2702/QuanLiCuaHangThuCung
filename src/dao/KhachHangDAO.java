package dao;

import entity.KhachHang;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class KhachHangDAO {

        // =========================================================
        // THU CUNG DANG NHAP O FORM THEM KHACH (khong dung entity.ThuCung
        // de khong dung do voi file ThuCung.java co san cua nhom)
        // =========================================================

        public static class ThuCungMoi {

                public final String ten;
                public final String loai;
                public final String giong;
                public final String gioiTinh;
                public final Double canNang; // null neu khong nhap

                public ThuCungMoi(String ten, String loai, String giong, String gioiTinh, Double canNang) {
                        this.ten = ten;
                        this.loai = loai;
                        this.giong = giong;
                        this.gioiTinh = gioiTinh;
                        this.canNang = canNang;
                }
        }

        // =========================================================
        // CAU SELECT CHUNG (kem thu cung, tong chi, gan nhat)
        // Can SQL Server 2017 tro len (STRING_AGG)
        // =========================================================

        private static final String BASE_SELECT = "SELECT k.maKhachHang, k.hoTen, k.soDienThoai, "
                        + "(SELECT COUNT(*) FROM ThuCung t WHERE t.maKhachHang = k.maKhachHang) AS thuCung, "
                        + "(SELECT ISNULL(SUM(ct.soLuong * ct.donGia * (1 - ISNULL(ct.phanTramGiam, 0) / 100.0)), 0) "
                        + "   FROM HoaDon h JOIN ChiTietHoaDon ct ON ct.maHoaDon = h.maHoaDon "
                        + "   WHERE h.maKhachHang = k.maKhachHang) AS tongChi, "
                        + "(SELECT MAX(h2.ngayLap) FROM HoaDon h2 WHERE h2.maKhachHang = k.maKhachHang) AS ganNhat "
                        + "FROM KhachHang k ";

        private KhachHang map(ResultSet rs) throws SQLException {

                KhachHang kh = new KhachHang(
                                rs.getString("maKhachHang"),
                                rs.getString("hoTen"),
                                rs.getString("soDienThoai"),
                                null, // email: CSDL khong co cot nay
                                null, // diaChi: CSDL khong co cot nay
                                0); // diemTichLuy: CSDL khong co cot nay

                kh.setThuCung(rs.getString("thuCung"));
                kh.setTongChi(rs.getLong("tongChi"));

                Timestamp ts = rs.getTimestamp("ganNhat");

                if (ts != null) {
                        kh.setGanNhat(new SimpleDateFormat("dd/MM/yyyy").format(ts));
                }

                return kh;
        }

        // =========================================================
        // TIM KIEM (ten / SDT / ma)
        // =========================================================

        public ArrayList<KhachHang> timKiem(String tuKhoa) {

                ArrayList<KhachHang> list = new ArrayList<>();

                StringBuilder sql = new StringBuilder(BASE_SELECT);

                sql.append("WHERE (k.hoTen LIKE ? OR k.soDienThoai LIKE ? OR k.maKhachHang LIKE ?) ");

                sql.append("ORDER BY k.maKhachHang");

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql.toString())) {

                        String kw = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";

                        ps.setString(1, kw);
                        ps.setString(2, kw);
                        ps.setString(3, kw);

                        try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                        list.add(map(rs));
                                }
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list;
        }

        // =========================================================
        // LAY THEO SDT (null neu khong co)
        // =========================================================

        public KhachHang getBySdt(String sdt) {

                String sql = BASE_SELECT + "WHERE k.soDienThoai = ?";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, sdt);

                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        return map(rs);
                                }
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return null;
        }

        // =========================================================
        // KIEM TRA TRUNG SDT
        // =========================================================

        public boolean tonTaiSdt(String sdt) {

                String sql = "SELECT 1 FROM KhachHang WHERE soDienThoai = ?";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, sdt);

                        try (ResultSet rs = ps.executeQuery()) {
                                return rs.next();
                        }

                } catch (SQLException e) {

                        e.printStackTrace();

                        return false;
                }
        }

        // =========================================================
        // THONG KE: {tong khach, so khach co the (0), tong diem (0), khach mua thang nay}
        // =========================================================

        public long[] thongKe() {

                long[] kq = new long[4];

                String sql = "SELECT "
                                + "(SELECT COUNT(*) FROM KhachHang), "
                                + "(SELECT 0), "
                                + "(SELECT 0), "
                                + "(SELECT COUNT(DISTINCT maKhachHang) FROM HoaDon "
                                + "   WHERE YEAR(ngayLap) = YEAR(GETDATE()) AND MONTH(ngayLap) = MONTH(GETDATE()))";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {
                                for (int i = 0; i < 4; i++) {
                                        kq[i] = rs.getLong(i + 1);
                                }
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return kq;
        }

        // =========================================================
        // THEM KHACH HANG + THU CUNG (1 giao dich)
        // Tra ve ma khach moi, null neu loi.
        // =========================================================

        public String them(
                        KhachHang kh,
                        ArrayList<ThuCungMoi> thuCungs) {

                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        con.setAutoCommit(false);

                        try {

                                String maKH = nextCode(con, "KhachHang", "maKhachHang", "KH", 3);

                                // ---- khach hang ----
                                try (PreparedStatement ps = con.prepareStatement(
                                                "INSERT INTO KhachHang "
                                                                + "(maKhachHang, hoTen, soDienThoai) "
                                                                + "VALUES (?, ?, ?)")) {

                                        ps.setString(1, maKH);
                                        ps.setString(2, kh.getHoTen());
                                        ps.setString(3, kh.getSoDienThoai());
                                        ps.executeUpdate();
                                }

                                // ---- thu cung ----
                                for (ThuCungMoi tc : thuCungs) {

                                        String maTC = nextCode(con, "ThuCung", "maThuCung", "TC", 3);

                                        try (PreparedStatement ps = con.prepareStatement(
                                                        "INSERT INTO ThuCung "
                                                                        + "(maThuCung, tenThuCung, loai, giong, gioiTinh, canNang, maKhachHang) "
                                                                        + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {

                                                ps.setString(1, maTC);
                                                ps.setString(2, tc.ten);
                                                ps.setString(3, tc.loai);
                                                ps.setString(4, tc.giong);
                                                ps.setString(5, tc.gioiTinh);

                                                if (tc.canNang == null) {
                                                        ps.setNull(6, Types.DECIMAL);
                                                } else {
                                                        ps.setDouble(6, tc.canNang);
                                                }

                                                ps.setString(7, maKH);
                                                ps.executeUpdate();
                                        }
                                }

                                con.commit();

                                return maKH;

                        } catch (SQLException e) {

                                con.rollback();

                                throw e;
                        }

                } catch (SQLException e) {

                        e.printStackTrace();

                        return null;
                }
        }

        // =========================================================
        // XOA KHACH HANG
        // Tra ve null neu xoa duoc, nguoc lai tra ve noi dung loi.
        // Khach da co lich hen / hoa don / phieu dich vu thi khong xoa.
        // =========================================================

        public String xoa(String maKhachHang) {

                String[][] rangBuoc = {
                                { "LichHen", "lich hen" },
                                { "HoaDon", "hoa don" },
                                { "PhieuDichVu", "phieu dich vu" } };

                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        for (String[] r : rangBuoc) {

                                try (PreparedStatement ps = con.prepareStatement(
                                                "SELECT COUNT(*) FROM " + r[0] + " WHERE maKhachHang = ?")) {

                                        ps.setString(1, maKhachHang);

                                        try (ResultSet rs = ps.executeQuery()) {

                                                if (rs.next() && rs.getInt(1) > 0) {

                                                        return "Khach hang da co " + r[1]
                                                                        + " nen khong the xoa.";
                                                }
                                        }
                                }
                        }

                        con.setAutoCommit(false);

                        try {

                                chay(con, "DELETE FROM ThuCung WHERE maKhachHang = ?", maKhachHang);
                                chay(con, "DELETE FROM KhachHang WHERE maKhachHang = ?", maKhachHang);

                                con.commit();

                                return null;

                        } catch (SQLException e) {

                                con.rollback();

                                throw e;
                        }

                } catch (SQLException e) {

                        e.printStackTrace();

                        return "Xoa that bai: " + e.getMessage();
                }
        }

        private static void chay(Connection con, String sql, String ma) throws SQLException {

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, ma);

                        ps.executeUpdate();
                }
        }

        // =========================================================
        // HAM PHU
        // =========================================================

        private static String nextCode(
                        Connection con,
                        String table,
                        String col,
                        String prefix,
                        int width) throws SQLException {

                String sql = "SELECT MAX(" + col + ") FROM " + table
                                + " WHERE " + col + " LIKE ?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, prefix + "%");

                        try (ResultSet rs = ps.executeQuery()) {

                                int n = 0;

                                if (rs.next() && rs.getString(1) != null) {

                                        try {
                                                n = Integer.parseInt(
                                                                rs.getString(1).substring(prefix.length()));
                                        } catch (NumberFormatException e) {
                                                n = 0;
                                        }
                                }

                                return prefix + String.format("%0" + width + "d", n + 1);
                        }
                }
        }
}
