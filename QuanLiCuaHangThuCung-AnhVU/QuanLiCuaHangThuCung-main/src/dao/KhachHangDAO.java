package dao;

import entity.KhachHang;
import entity.ThuCung;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class KhachHangDAO {

        // =========================================================
        // CAU SELECT CHUNG (kem thu cung, hang the, tong chi, gan nhat)
        // Can SQL Server 2017 tro len (STRING_AGG)
        // =========================================================

        private static final String BASE_SELECT = "SELECT k.maKhachHang, k.hoTen, k.soDienThoai, k.email, k.diaChi, "
                        + "ISNULL(k.diemTichLuy, 0) AS diem, "
                        + "(SELECT STRING_AGG(t.tenThuCung + ' - ' + ISNULL(t.giong, ''), ', ') "
                        + "   FROM ThuCung t WHERE t.maKhachHang = k.maKhachHang) AS thuCung, "
                        + "(SELECT TOP 1 tv.hangThe FROM TheThanhVien tv "
                        + "   WHERE tv.maKhachHang = k.maKhachHang ORDER BY tv.ngayCap DESC) AS hang, "
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
                                rs.getString("email"),
                                rs.getString("diaChi"),
                                rs.getInt("diem"));

                kh.setThuCung(rs.getString("thuCung"));
                kh.setHangThe(rs.getString("hang"));
                kh.setTongChi(rs.getLong("tongChi"));

                Timestamp ts = rs.getTimestamp("ganNhat");

                if (ts != null) {
                        kh.setGanNhat(new SimpleDateFormat("dd/MM/yyyy").format(ts));
                }

                return kh;
        }

        // =========================================================
        // TIM KIEM (ten / SDT / ma) + LOC THEO HANG
        // hang: "Tat ca hang" | "Khong co the" | Silver | Gold | Platinum
        // =========================================================

        public ArrayList<KhachHang> timKiem(String tuKhoa, String hang) {

                ArrayList<KhachHang> list = new ArrayList<>();

                StringBuilder sql = new StringBuilder(BASE_SELECT);

                sql.append("WHERE (k.hoTen LIKE ? OR k.soDienThoai LIKE ? OR k.maKhachHang LIKE ?) ");

                boolean theoHang = hang != null
                                && !hang.equals("Tat ca hang")
                                && !hang.equals("Khong co the");

                if (hang != null && hang.equals("Khong co the")) {
                        sql.append("AND NOT EXISTS (SELECT 1 FROM TheThanhVien tv "
                                        + "WHERE tv.maKhachHang = k.maKhachHang) ");
                } else if (theoHang) {
                        sql.append("AND EXISTS (SELECT 1 FROM TheThanhVien tv "
                                        + "WHERE tv.maKhachHang = k.maKhachHang AND tv.hangThe = ?) ");
                }

                sql.append("ORDER BY k.maKhachHang");

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql.toString())) {

                        String kw = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";

                        ps.setString(1, kw);
                        ps.setString(2, kw);
                        ps.setString(3, kw);

                        if (theoHang) {
                                ps.setString(4, hang);
                        }

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
        // THONG KE: {tong khach, khach co the, tong diem, khach mua thang nay}
        // =========================================================

        public long[] thongKe() {

                long[] kq = new long[4];

                String sql = "SELECT "
                                + "(SELECT COUNT(*) FROM KhachHang), "
                                + "(SELECT COUNT(DISTINCT maKhachHang) FROM TheThanhVien), "
                                + "(SELECT ISNULL(SUM(diemTichLuy), 0) FROM KhachHang), "
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
        // THEM KHACH HANG + THE THANH VIEN + THU CUNG (1 giao dich)
        // hangThe = null neu khong cap the. Tra ve ma khach moi, null neu loi.
        // =========================================================

        public String them(
                        KhachHang kh,
                        ArrayList<ThuCung> thuCungs,
                        String hangThe,
                        String maNhanVien) {

                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        con.setAutoCommit(false);

                        try {

                                String maKH = nextCode(con, "KhachHang", "maKhachHang", "KH", 3);

                                // ---- khach hang ----
                                try (PreparedStatement ps = con.prepareStatement(
                                                "INSERT INTO KhachHang "
                                                                + "(maKhachHang, hoTen, soDienThoai, email, diaChi, diemTichLuy) "
                                                                + "VALUES (?, ?, ?, ?, ?, ?)")) {

                                        ps.setString(1, maKH);
                                        ps.setString(2, kh.getHoTen());
                                        ps.setString(3, kh.getSoDienThoai());
                                        ps.setString(4, kh.getEmail());
                                        ps.setString(5, kh.getDiaChi());
                                        ps.setInt(6, kh.getDiemTichLuy());
                                        ps.executeUpdate();
                                }

                                // ---- the thanh vien ----
                                if (hangThe != null) {

                                        String maThe = nextCode(con, "TheThanhVien", "maThe", "THE", 3);

                                        try (PreparedStatement ps = con.prepareStatement(
                                                        "INSERT INTO TheThanhVien "
                                                                        + "(maThe, hangThe, chietKhauPhanTram, ngayCap, maKhachHang, maNhanVien) "
                                                                        + "VALUES (?, ?, ?, CAST(GETDATE() AS DATE), ?, ?)")) {

                                                ps.setString(1, maThe);
                                                ps.setString(2, hangThe);
                                                ps.setDouble(3, chietKhau(hangThe));
                                                ps.setString(4, maKH);
                                                ps.setString(5, maNhanVien);
                                                ps.executeUpdate();
                                        }
                                }

                                // ---- thu cung ----
                                for (ThuCung tc : thuCungs) {

                                        String maTC = nextCode(con, "ThuCung", "maThuCung", "TC", 3);

                                        try (PreparedStatement ps = con.prepareStatement(
                                                        "INSERT INTO ThuCung "
                                                                        + "(maThuCung, tenThuCung, loai, giong, gioiTinh, canNang, maKhachHang) "
                                                                        + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {

                                                ps.setString(1, maTC);
                                                ps.setString(2, tc.getTenThuCung());
                                                ps.setString(3, tc.getLoai());
                                                ps.setString(4, tc.getGiong());
                                                ps.setString(5, tc.getGioiTinh());

                                                if (tc.getCanNang() == null) {
                                                        ps.setNull(6, Types.DECIMAL);
                                                } else {
                                                        ps.setDouble(6, tc.getCanNang());
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

                                chay(con, "DELETE FROM TheThanhVien WHERE maKhachHang = ?", maKhachHang);
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
