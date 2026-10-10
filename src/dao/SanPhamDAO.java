package dao;

import ConnectDB.connectDB;
import entity.SanPham;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SanPhamDAO {

    /*
     * Bang SanPham khong co cot ton kho, nen ton kho duoc TINH tu du lieu:
     *   ton = tong nhap (phieu nhap da hoan tat)
     *       - tong ban (ChiTietHoaDon)
     *       + chenh lech cac phieu kiem ke da chot (thuc te - he thong)
     */
    /** Bieu thuc SQL tinh ton kho cua san pham co bi danh "sp" (dung chung cho DAO khac). */
    static final String BIEU_THUC_TON = """
                   ISNULL((SELECT SUM(ct.soLuongNhap)
                           FROM ChiTietPhieuNhap ct
                           JOIN PhieuNhapKho pn
                             ON pn.maPhieuNhap = ct.maPhieuNhap
                           WHERE ct.maSanPham = sp.maSanPham
                             AND pn.trangThai = 1
                             AND ISNULL(ct.trangThai, 1) = 1), 0)
                 - ISNULL((SELECT SUM(hd.soLuong)
                           FROM ChiTietHoaDon hd
                           WHERE hd.maSanPham = sp.maSanPham), 0)
                 + ISNULL((SELECT SUM(kk.soLuongThucTe - kk.soLuongHeThong)
                           FROM ChiTietPhieuKiemKe kk
                           JOIN PhieuKiemKe pk
                             ON pk.maPhieuKiemKe = kk.maPhieuKiemKe
                           WHERE kk.maSanPham = sp.maSanPham
                             AND pk.trangThai = 1
                             AND kk.soLuongThucTe IS NOT NULL), 0)
            """;

    private static final String SELECT_CO_BAN = """
            SELECT sp.maSanPham,
                   sp.tenSanPham,
                   sp.giaNhap,
                   sp.giaBan,
                   sp.maDanhMuc,
                   dm.tenDanhMuc,
                   sp.maNhaCungCap,
                   ncc.tenNhaCungCap,
            """ + BIEU_THUC_TON + """
                   AS tonKho
            FROM SanPham sp
            JOIN DanhMuc dm ON dm.maDanhMuc = sp.maDanhMuc
            JOIN Nhacungcap ncc ON ncc.maNhaCungCap = sp.maNhaCungCap
            """;

    public ArrayList<SanPham> timKiem(String tuKhoa,
                                      String maDanhMuc,
                                      String maNhaCungCap) throws SQLException {

        ArrayList<SanPham> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(SELECT_CO_BAN);
        List<String> thamSo = new ArrayList<>();

        String kw = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";

        sql.append(" WHERE (sp.maSanPham LIKE ? OR sp.tenSanPham LIKE ?)");
        thamSo.add(kw);
        thamSo.add(kw);

        if (maDanhMuc != null) {
            sql.append(" AND sp.maDanhMuc = ?");
            thamSo.add(maDanhMuc);
        }

        if (maNhaCungCap != null) {
            sql.append(" AND sp.maNhaCungCap = ?");
            thamSo.add(maNhaCungCap);
        }

        sql.append(" ORDER BY sp.maSanPham");

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < thamSo.size(); i++) {
                ps.setString(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SanPham sp = new SanPham();
                    sp.setMaSanPham(rs.getString("maSanPham"));
                    sp.setTenSanPham(rs.getString("tenSanPham"));
                    sp.setGiaNhap(rs.getDouble("giaNhap"));
                    sp.setGiaBan(rs.getDouble("giaBan"));
                    sp.setMaDanhMuc(rs.getString("maDanhMuc"));
                    sp.setTenDanhMuc(rs.getString("tenDanhMuc"));
                    sp.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                    sp.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                    sp.setTonKho(rs.getInt("tonKho"));
                    list.add(sp);
                }
            }
        }

        return list;
    }

    public ArrayList<SanPham> getAll() throws SQLException {
        return timKiem("", null, null);
    }

    public String taoMaMoi() throws SQLException {
        return MaTuDong.taoMa("SanPham", "maSanPham", "SP", 3);
    }

    public boolean them(SanPham sp) throws SQLException {

        String sql = """
                INSERT INTO SanPham
                (maSanPham, tenSanPham, giaNhap, giaBan,
                 maDanhMuc, maNhaCungCap)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, sp.getMaSanPham());
            ps.setString(2, sp.getTenSanPham());
            ps.setDouble(3, sp.getGiaNhap());
            ps.setDouble(4, sp.getGiaBan());
            ps.setString(5, sp.getMaDanhMuc());
            ps.setString(6, sp.getMaNhaCungCap());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean sua(SanPham sp) throws SQLException {

        String sql = """
                UPDATE SanPham
                SET tenSanPham = ?,
                    giaNhap = ?,
                    giaBan = ?,
                    maDanhMuc = ?,
                    maNhaCungCap = ?
                WHERE maSanPham = ?
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, sp.getTenSanPham());
            ps.setDouble(2, sp.getGiaNhap());
            ps.setDouble(3, sp.getGiaBan());
            ps.setString(4, sp.getMaDanhMuc());
            ps.setString(5, sp.getMaNhaCungCap());
            ps.setString(6, sp.getMaSanPham());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean xoa(String maSanPham) throws SQLException {

        String sql = "DELETE FROM SanPham WHERE maSanPham = ?";

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, maSanPham);

            return ps.executeUpdate() > 0;
        }
    }

    /** So dong nhap / ban / kiem ke dang tham chieu toi san pham nay. */
    public int demLichSu(String maSanPham) throws SQLException {

        String sql = """
                SELECT (SELECT COUNT(*) FROM ChiTietPhieuNhap   WHERE maSanPham = ?)
                     + (SELECT COUNT(*) FROM ChiTietHoaDon      WHERE maSanPham = ?)
                     + (SELECT COUNT(*) FROM ChiTietPhieuKiemKe WHERE maSanPham = ?)
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, maSanPham);
            ps.setString(2, maSanPham);
            ps.setString(3, maSanPham);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
