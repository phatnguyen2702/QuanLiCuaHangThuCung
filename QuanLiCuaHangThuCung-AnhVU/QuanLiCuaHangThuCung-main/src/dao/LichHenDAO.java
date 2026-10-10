package dao;

import entity.LichHen;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class LichHenDAO {

        // =========================================================
        // DANH SACH NHAN VIEN (de loc): moi phan tu {ma, ten}
        // =========================================================

        public ArrayList<String[]> getNhanVien() {

                ArrayList<String[]> list = new ArrayList<>();

                String sql = "SELECT maNhanVien, hoTen FROM NhanVien ORDER BY maNhanVien";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {
                                list.add(new String[] { rs.getString(1), rs.getString(2) });
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list;
        }

        // =========================================================
        // LAY LICH HEN (tham so null / rong = khong loc)
        // =========================================================

        public ArrayList<LichHen> getAll(
                        String sdt,
                        LocalDate tu,
                        LocalDate den,
                        String maNhanVien,
                        String trangThai) {

                ArrayList<LichHen> list = new ArrayList<>();

                StringBuilder sql = new StringBuilder(
                                "SELECT l.maLichHen, l.ngayGioHen, l.ghiChu, "
                                                + "l.maKhachHang, k.hoTen AS tenKH, k.soDienThoai, "
                                                + "l.maThuCung, t.tenThuCung, t.giong, "
                                                + "l.maNhanVien, nv.hoTen AS tenNV, l.trangThai "
                                                + "FROM LichHen l "
                                                + "JOIN KhachHang k ON k.maKhachHang = l.maKhachHang "
                                                + "JOIN ThuCung t ON t.maThuCung = l.maThuCung "
                                                + "JOIN NhanVien nv ON nv.maNhanVien = l.maNhanVien "
                                                + "WHERE 1 = 1 ");

                ArrayList<Object> params = new ArrayList<>();

                if (sdt != null && !sdt.isEmpty()) {
                        sql.append("AND k.soDienThoai = ? ");
                        params.add(sdt);
                }

                if (tu != null) {
                        sql.append("AND l.ngayGioHen >= ? ");
                        params.add(Timestamp.valueOf(tu.atStartOfDay()));
                }

                if (den != null) {
                        sql.append("AND l.ngayGioHen < ? ");
                        params.add(Timestamp.valueOf(den.plusDays(1).atStartOfDay()));
                }

                if (maNhanVien != null && !maNhanVien.isEmpty()) {
                        sql.append("AND l.maNhanVien = ? ");
                        params.add(maNhanVien);
                }

                if (trangThai != null && !trangThai.equals("Tat ca")) {
                        sql.append("AND l.trangThai = ? ");
                        params.add(trangThai);
                }

                sql.append("ORDER BY l.ngayGioHen DESC");

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql.toString())) {

                        for (int i = 0; i < params.size(); i++) {
                                ps.setObject(i + 1, params.get(i));
                        }

                        try (ResultSet rs = ps.executeQuery()) {

                                while (rs.next()) {

                                        String giong = rs.getString("giong");

                                        String thuCung = rs.getString("tenThuCung")
                                                        + (giong == null ? "" : " (" + giong + ")");

                                        list.add(new LichHen(
                                                        rs.getString("maLichHen"),
                                                        rs.getTimestamp("ngayGioHen").toLocalDateTime(),
                                                        rs.getString("ghiChu"),
                                                        rs.getString("maKhachHang"),
                                                        rs.getString("tenKH"),
                                                        rs.getString("soDienThoai"),
                                                        rs.getString("maThuCung"),
                                                        thuCung,
                                                        rs.getString("maNhanVien"),
                                                        rs.getString("tenNV"),
                                                        rs.getString("trangThai")));
                                }
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list;
        }

        // =========================================================
        // DOI TRANG THAI
        // =========================================================

        public boolean capNhatTrangThai(String maLichHen, String trangThai) {

                String sql = "UPDATE LichHen SET trangThai = ? WHERE maLichHen = ?";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, trangThai);
                        ps.setString(2, maLichHen);

                        return ps.executeUpdate() > 0;

                } catch (SQLException e) {

                        e.printStackTrace();

                        return false;
                }
        }

        // =========================================================
        // XOA LICH HEN (xoa chi tiet dich vu truoc, roi xoa lich)
        // =========================================================

        public boolean xoa(String maLichHen) {

                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        con.setAutoCommit(false);

                        try {

                                try (PreparedStatement ps = con.prepareStatement(
                                                "DELETE FROM ChiTietLichHen WHERE maLichHen = ?")) {

                                        ps.setString(1, maLichHen);
                                        ps.executeUpdate();
                                }

                                int n;

                                try (PreparedStatement ps = con.prepareStatement(
                                                "DELETE FROM LichHen WHERE maLichHen = ?")) {

                                        ps.setString(1, maLichHen);
                                        n = ps.executeUpdate();
                                }

                                con.commit();

                                return n > 0;

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
        // THONG KE 1 KHACH: {lich sap toi, da den, tong chi spa}
        // tong chi spa = tong tien cac phieu dich vu cua khach
        // =========================================================

        public long[] thongKeKhach(String maKhachHang) {

                long[] kq = new long[3];

                String sql = "SELECT "
                                + "(SELECT COUNT(*) FROM LichHen WHERE maKhachHang = ? "
                                + "   AND ngayGioHen >= GETDATE() "
                                + "   AND trangThai IN ('Cho xac nhan', 'Da xac nhan')), "
                                + "(SELECT COUNT(*) FROM LichHen WHERE maKhachHang = ? "
                                + "   AND trangThai = 'Hoan thanh'), "
                                + "(SELECT ISNULL(SUM(ct.soLuong * ct.donGia), 0) "
                                + "   FROM PhieuDichVu p JOIN ChiTietDichVu ct ON ct.maPhieuDichVu = p.maPhieuDichVu "
                                + "   WHERE p.maKhachHang = ?)";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, maKhachHang);
                        ps.setString(2, maKhachHang);
                        ps.setString(3, maKhachHang);

                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        for (int i = 0; i < 3; i++) {
                                                kq[i] = rs.getLong(i + 1);
                                        }
                                }
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return kq;
        }

        // =========================================================
        // DAT LICH MOI
        // =========================================================

        // Nhan vien Spa (vaiTro co chu "Spa"); neu khong co thi lay tat ca
        public ArrayList<String[]> getNhanVienSpa() {

                ArrayList<String[]> list = new ArrayList<>();

                String sql = "SELECT maNhanVien, hoTen FROM NhanVien "
                                + "WHERE vaiTro LIKE '%Spa%' ORDER BY maNhanVien";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {
                                list.add(new String[] { rs.getString(1), rs.getString(2) });
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list.isEmpty() ? getNhanVien() : list;
        }

        // Thu cung cua 1 khach: moi phan tu {ma, "ten (giong)"}
        public ArrayList<String[]> getThuCungCuaKhach(String maKhachHang) {

                ArrayList<String[]> list = new ArrayList<>();

                String sql = "SELECT maThuCung, tenThuCung, giong FROM ThuCung "
                                + "WHERE maKhachHang = ? ORDER BY maThuCung";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, maKhachHang);

                        try (ResultSet rs = ps.executeQuery()) {

                                while (rs.next()) {

                                        String giong = rs.getString(3);

                                        list.add(new String[] {
                                                        rs.getString(1),
                                                        rs.getString(2) + (giong == null ? "" : " (" + giong + ")") });
                                }
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list;
        }

        // Dich vu: moi phan tu {ma, ten, gia}
        public ArrayList<String[]> getDichVu() {

                ArrayList<String[]> list = new ArrayList<>();

                String sql = "SELECT maDichVu, tenDichVu, CAST(ISNULL(giaDichVu, 0) AS BIGINT) "
                                + "FROM DichVu ORDER BY maDichVu";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {
                                list.add(new String[] { rs.getString(1), rs.getString(2), rs.getString(3) });
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list;
        }

        // Nhan vien da co lich (khong huy) trong vong 60 phut quanh gio hen?
        public boolean trungLich(String maNhanVien, LocalDateTime tg) {

                String sql = "SELECT COUNT(*) FROM LichHen "
                                + "WHERE maNhanVien = ? AND trangThai <> 'Da huy' "
                                + "AND ABS(DATEDIFF(MINUTE, ngayGioHen, ?)) < 60";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, maNhanVien);
                        ps.setTimestamp(2, Timestamp.valueOf(tg));

                        try (ResultSet rs = ps.executeQuery()) {
                                return rs.next() && rs.getInt(1) > 0;
                        }

                } catch (SQLException e) {

                        e.printStackTrace();

                        return false;
                }
        }

        // Them lich hen + chi tiet dich vu (1 giao dich). Tra ve ma lich moi, null neu loi.
        public String them(
                        String maKhachHang,
                        String maThuCung,
                        String maNhanVien,
                        LocalDateTime tg,
                        String ghiChu,
                        ArrayList<String> dsMaDichVu) {

                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        con.setAutoCommit(false);

                        try {

                                String ma = nextCode(con, "LichHen", "maLichHen", "LH", 3);

                                try (PreparedStatement ps = con.prepareStatement(
                                                "INSERT INTO LichHen "
                                                                + "(maLichHen, ngayGioHen, ghiChu, maKhachHang, "
                                                                + "maNhanVien, maThuCung, trangThai) "
                                                                + "VALUES (?, ?, ?, ?, ?, ?, 'Cho xac nhan')")) {

                                        ps.setString(1, ma);
                                        ps.setTimestamp(2, Timestamp.valueOf(tg));
                                        ps.setString(3, (ghiChu == null || ghiChu.isEmpty()) ? null : ghiChu);
                                        ps.setString(4, maKhachHang);
                                        ps.setString(5, maNhanVien);
                                        ps.setString(6, maThuCung);
                                        ps.executeUpdate();
                                }

                                for (String dv : dsMaDichVu) {

                                        try (PreparedStatement ps = con.prepareStatement(
                                                        "INSERT INTO ChiTietLichHen "
                                                                        + "(maLichHen, maDichVu, maNhanVien) "
                                                                        + "VALUES (?, ?, ?)")) {

                                                ps.setString(1, ma);
                                                ps.setString(2, dv);
                                                ps.setString(3, maNhanVien);
                                                ps.executeUpdate();
                                        }
                                }

                                con.commit();

                                return ma;

                        } catch (SQLException e) {

                                con.rollback();

                                throw e;
                        }

                } catch (SQLException e) {

                        e.printStackTrace();

                        return null;
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
