package dao;

import ConnectDB.connectDB;
import entity.DanhMuc;

import java.sql.*;
import java.util.ArrayList;

public class DanhMucDAO {

    // Lay danh muc kem so san pham, loc theo ma hoac ten
    public ArrayList<DanhMuc> timKiem(String tuKhoa) throws SQLException {

        ArrayList<DanhMuc> list = new ArrayList<>();

        String sql = """
                SELECT dm.maDanhMuc,
                       dm.tenDanhMuc,
                       dm.moTa,
                       COUNT(t.maSanPham) AS soSanPham,
                       ISNULL(SUM(t.ton), 0) AS tongTon
                FROM DanhMuc dm
                LEFT JOIN (
                    SELECT sp.maSanPham, sp.maDanhMuc,
                """ + SanPhamDAO.BIEU_THUC_TON + """
                           AS ton
                    FROM SanPham sp
                ) t ON t.maDanhMuc = dm.maDanhMuc
                WHERE dm.maDanhMuc LIKE ?
                   OR dm.tenDanhMuc LIKE ?
                GROUP BY dm.maDanhMuc, dm.tenDanhMuc, dm.moTa
                ORDER BY dm.maDanhMuc
                """;

        String kw = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, kw);
            ps.setString(2, kw);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DanhMuc dm = new DanhMuc();
                    dm.setMaDanhMuc(rs.getString("maDanhMuc"));
                    dm.setTenDanhMuc(rs.getString("tenDanhMuc"));
                    dm.setMoTa(rs.getString("moTa"));
                    dm.setSoSanPham(rs.getInt("soSanPham"));
                    dm.setTongTon(rs.getInt("tongTon"));
                    list.add(dm);
                }
            }
        }

        return list;
    }

    public ArrayList<DanhMuc> getAll() throws SQLException {
        return timKiem("");
    }

    public int demTatCa() throws SQLException {

        String sql = "SELECT COUNT(*) FROM DanhMuc";

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public String taoMaMoi() throws SQLException {
        return MaTuDong.taoMa("DanhMuc", "maDanhMuc", "DM", 3);
    }

    public boolean them(DanhMuc dm) throws SQLException {

        String sql = """
                INSERT INTO DanhMuc (maDanhMuc, tenDanhMuc, moTa)
                VALUES (?, ?, ?)
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, dm.getMaDanhMuc());
            ps.setString(2, dm.getTenDanhMuc());
            ps.setString(3, dm.getMoTa());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean sua(DanhMuc dm) throws SQLException {

        String sql = """
                UPDATE DanhMuc
                SET tenDanhMuc = ?,
                    moTa = ?
                WHERE maDanhMuc = ?
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, dm.getTenDanhMuc());
            ps.setString(2, dm.getMoTa());
            ps.setString(3, dm.getMaDanhMuc());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Du lieu se bi xoa kem khi xoa 1 danh muc:
     * {so san pham, dong hoa don, dong phieu nhap, dong kiem ke}.
     */
    public int[] demLienQuan(String maDanhMuc) throws SQLException {

        String sql = """
                SELECT (SELECT COUNT(*) FROM SanPham
                         WHERE maDanhMuc = ?),
                       (SELECT COUNT(*) FROM ChiTietHoaDon x
                         JOIN SanPham sp ON sp.maSanPham = x.maSanPham
                         WHERE sp.maDanhMuc = ?),
                       (SELECT COUNT(*) FROM ChiTietPhieuNhap x
                         JOIN SanPham sp ON sp.maSanPham = x.maSanPham
                         WHERE sp.maDanhMuc = ?),
                       (SELECT COUNT(*) FROM ChiTietPhieuKiemKe x
                         JOIN SanPham sp ON sp.maSanPham = x.maSanPham
                         WHERE sp.maDanhMuc = ?)
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, maDanhMuc);
            }

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new int[]{rs.getInt(1), rs.getInt(2),
                        rs.getInt(3), rs.getInt(4)};
            }
        }
    }

    /**
     * Xoa danh muc BAT KY (ke ca khi con san pham): xoa luon cac dong
     * chi tiet hoa don / nhap kho / kiem ke cua san pham thuoc danh muc,
     * roi xoa san pham, roi xoa danh muc - tat ca trong 1 giao dich.
     * Phieu nhap / phieu kiem ke bi trong sau khi xoa cung duoc don di.
     */
    public boolean xoa(String maDanhMuc) throws SQLException {

        String[] cacLenh = {
                """
                DELETE FROM ChiTietHoaDon WHERE maSanPham IN
                  (SELECT maSanPham FROM SanPham WHERE maDanhMuc = ?)
                """,
                """
                DELETE FROM ChiTietPhieuNhap WHERE maSanPham IN
                  (SELECT maSanPham FROM SanPham WHERE maDanhMuc = ?)
                """,
                """
                DELETE FROM ChiTietPhieuKiemKe WHERE maSanPham IN
                  (SELECT maSanPham FROM SanPham WHERE maDanhMuc = ?)
                """,
                "DELETE FROM SanPham WHERE maDanhMuc = ?"
        };

        try (Connection con = connectDB.getConnection()) {

            con.setAutoCommit(false);

            try {
                for (String lenh : cacLenh) {
                    try (PreparedStatement ps = con.prepareStatement(lenh)) {
                        ps.setString(1, maDanhMuc);
                        ps.executeUpdate();
                    }
                }

                // Don phieu nhap / kiem ke khong con dong chi tiet nao
                try (Statement st = con.createStatement()) {
                    st.executeUpdate("""
                            DELETE FROM PhieuNhapKho WHERE maPhieuNhap NOT IN
                              (SELECT maPhieuNhap FROM ChiTietPhieuNhap)
                            """);
                    st.executeUpdate("""
                            DELETE FROM PhieuKiemKe WHERE maPhieuKiemKe NOT IN
                              (SELECT maPhieuKiemKe FROM ChiTietPhieuKiemKe)
                            """);
                }

                boolean ok;
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM DanhMuc WHERE maDanhMuc = ?")) {
                    ps.setString(1, maDanhMuc);
                    ok = ps.executeUpdate() > 0;
                }

                con.commit();
                return ok;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
