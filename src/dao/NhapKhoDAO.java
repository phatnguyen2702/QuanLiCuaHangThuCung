package dao;

import ConnectDB.connectDB;
import entity.ChiTietPhieuNhap;
import entity.NhaCungCap;
import entity.PhieuNhapKho;
import entity.SanPham;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cua man hinh "Nhap kho": phieu nhap (luu, nhap, hoan tat, xoa)
 * + nha cung cap + san pham theo nha cung cap.
 */
public class NhapKhoDAO {

    private void damBaoCotHanSuDung() throws SQLException {
        try (Connection con = connectDB.getConnection(); Statement st = con.createStatement()) {
            st.execute("IF COL_LENGTH('dbo.ChiTietPhieuNhap', 'hanSuDung') IS NULL " +
                    "ALTER TABLE dbo.ChiTietPhieuNhap ADD hanSuDung DATE NULL");
            st.execute("IF COL_LENGTH('dbo.ChiTietPhieuNhap', 'maLoHang') IS NULL ALTER TABLE dbo.ChiTietPhieuNhap ADD maLoHang VARCHAR(30) NULL");
            st.execute("IF COL_LENGTH('dbo.ChiTietPhieuNhap', 'maLo') IS NULL BEGIN " +
                    "ALTER TABLE dbo.ChiTietPhieuNhap ADD maLo INT IDENTITY(1,1) NOT NULL; " +
                    "IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name='PK_ChiTietPhieuNhap') " +
                    "ALTER TABLE dbo.ChiTietPhieuNhap DROP CONSTRAINT PK_ChiTietPhieuNhap; " +
                    "ALTER TABLE dbo.ChiTietPhieuNhap ADD CONSTRAINT PK_ChiTietPhieuNhap PRIMARY KEY (maLo); END");
        }
    }

    public String taoMaMoi() throws SQLException {
        return taoMa("PhieuNhapKho", "maPhieuNhap", "PN", 3);
    }

    // Luu phieu + chi tiet trong 1 giao dich: loi o dau thi huy het
    public void luu(PhieuNhapKho phieu,
                    List<ChiTietPhieuNhap> chiTiet) throws SQLException {

        damBaoCotHanSuDung();

        String sqlPhieu = """
                INSERT INTO PhieuNhapKho
                (maPhieuNhap, ngayNhap, trangThai, maNhanVien, maNhaCungCap)
                VALUES (?, ?, ?, ?, ?)
                """;

        String sqlChiTiet = """
                INSERT INTO ChiTietPhieuNhap
                (maPhieuNhap, maSanPham, soLuongNhap, donGiaNhap, trangThai, hanSuDung, maLoHang)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = connectDB.getConnection()) {

            con.setAutoCommit(false);

            try {
                int daCapNhat;
                try (PreparedStatement ps = con.prepareStatement("UPDATE PhieuNhapKho SET ngayNhap=?, trangThai=?, maNhanVien=?, maNhaCungCap=? WHERE maPhieuNhap=? AND ISNULL(trangThai,1)=0")) {
                    ps.setTimestamp(1, Timestamp.valueOf(phieu.getNgayNhap()));
                    ps.setBoolean(2, phieu.isTrangThai());
                    ps.setString(3, phieu.getMaNhanVien());
                    ps.setString(4, phieu.getMaNhaCungCap());
                    ps.setString(5, phieu.getMaPhieuNhap());
                    daCapNhat = ps.executeUpdate();
                }
                if (daCapNhat > 0) {
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietPhieuNhap WHERE maPhieuNhap=?")) {
                        ps.setString(1, phieu.getMaPhieuNhap()); ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement(sqlPhieu)) {
                        ps.setString(1, phieu.getMaPhieuNhap());
                        ps.setTimestamp(2, Timestamp.valueOf(phieu.getNgayNhap()));
                        ps.setBoolean(3, phieu.isTrangThai());
                        ps.setString(4, phieu.getMaNhanVien());
                        ps.setString(5, phieu.getMaNhaCungCap());
                        ps.executeUpdate();
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {
                    for (ChiTietPhieuNhap ct : chiTiet) {
                        ps.setString(1, phieu.getMaPhieuNhap());
                        ps.setString(2, ct.getMaSanPham());
                        ps.setInt(3, ct.getSoLuongNhap());
                        ps.setDouble(4, ct.getDonGiaNhap());
                        ps.setBoolean(5, phieu.isTrangThai());
                        if (ct.getHanSuDung() == null) ps.setNull(6, Types.DATE);
                        else ps.setDate(6, Date.valueOf(ct.getHanSuDung()));
                        ps.setString(7, ct.getMaLoHang());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public ArrayList<ChiTietPhieuNhap> layChiTiet(String maPhieu) throws SQLException {
        damBaoCotHanSuDung();
        ArrayList<ChiTietPhieuNhap> ds = new ArrayList<>();
        String sql = "SELECT ct.maSanPham,sp.tenSanPham,ct.soLuongNhap,ct.donGiaNhap,ct.hanSuDung,ct.maLoHang FROM ChiTietPhieuNhap ct JOIN SanPham sp ON sp.maSanPham=ct.maSanPham WHERE ct.maPhieuNhap=? ORDER BY ct.maLo";
        try (Connection c = connectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maPhieu);
            try (ResultSet r = p.executeQuery()) { while (r.next()) {
                ChiTietPhieuNhap ct = new ChiTietPhieuNhap(); ct.setMaPhieuNhap(maPhieu);
                ct.setMaSanPham(r.getString(1)); ct.setTenSanPham(r.getString(2));
                ct.setSoLuongNhap(r.getInt(3)); ct.setDonGiaNhap(r.getDouble(4));
                Date d = r.getDate(5); ct.setHanSuDung(d == null ? null : d.toLocalDate()); ct.setMaLoHang(r.getString(6)); ds.add(ct);
            }}
        }
        return ds;
    }

    // =========================================================
    // PHIEU NHAP NHAP (trangThai = 0)
    // =========================================================

    public ArrayList<PhieuNhapKho> dsPhieuNhap() throws SQLException {

        ArrayList<PhieuNhapKho> list = new ArrayList<>();

        String sql = """
                SELECT pn.maPhieuNhap, pn.ngayNhap, pn.maNhanVien,
                       pn.maNhaCungCap, ncc.tenNhaCungCap,
                       COUNT(ct.maSanPham) AS soMatHang,
                       ISNULL(SUM(ct.soLuongNhap * ct.donGiaNhap), 0) AS tongTien
                FROM PhieuNhapKho pn
                JOIN Nhacungcap ncc ON ncc.maNhaCungCap = pn.maNhaCungCap
                LEFT JOIN ChiTietPhieuNhap ct ON ct.maPhieuNhap = pn.maPhieuNhap
                WHERE ISNULL(pn.trangThai, 1) = 0
                GROUP BY pn.maPhieuNhap, pn.ngayNhap, pn.maNhanVien,
                         pn.maNhaCungCap, ncc.tenNhaCungCap
                ORDER BY pn.maPhieuNhap
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                PhieuNhapKho p = new PhieuNhapKho();
                p.setMaPhieuNhap(rs.getString("maPhieuNhap"));
                Timestamp t = rs.getTimestamp("ngayNhap");
                p.setNgayNhap(t == null ? null : t.toLocalDateTime());
                p.setMaNhanVien(rs.getString("maNhanVien"));
                p.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                p.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                p.setSoMatHang(rs.getInt("soMatHang"));
                p.setTongTien(rs.getDouble("tongTien"));
                list.add(p);
            }
        }

        return list;
    }

    /** Hoan tat phieu nhap: phieu + moi dong chi tiet chuyen sang trangThai 1. */
    public void hoanTatPhieuNhap(String maPhieu) throws SQLException {

        try (Connection con = connectDB.getConnection()) {

            con.setAutoCommit(false);

            try {
                try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE PhieuNhapKho SET trangThai = 1, ngayNhap = ?
                        WHERE maPhieuNhap = ? AND ISNULL(trangThai, 1) = 0
                        """)) {
                    ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setString(2, maPhieu);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE ChiTietPhieuNhap SET trangThai = 1 WHERE maPhieuNhap = ?")) {
                    ps.setString(1, maPhieu);
                    ps.executeUpdate();
                }

                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Xoa phieu nhap (chi phieu con la nhap, khong dung toi ton kho). */
    public void xoaPhieuNhap(String maPhieu) throws SQLException {

        try (Connection con = connectDB.getConnection()) {

            con.setAutoCommit(false);

            try {
                try (PreparedStatement ps = con.prepareStatement("""
                        DELETE FROM ChiTietPhieuNhap WHERE maPhieuNhap = ?
                          AND maPhieuNhap IN (SELECT maPhieuNhap FROM PhieuNhapKho
                                              WHERE ISNULL(trangThai, 1) = 0)
                        """)) {
                    ps.setString(1, maPhieu);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM PhieuNhapKho WHERE maPhieuNhap = ? AND ISNULL(trangThai, 1) = 0")) {
                    ps.setString(1, maPhieu);
                    ps.executeUpdate();
                }

                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // =========================================================
    // NHA CUNG CAP
    // =========================================================

    public ArrayList<NhaCungCap> layDanhSachNhaCungCap() throws SQLException {

        ArrayList<NhaCungCap> list = new ArrayList<>();

        String sql = """
                SELECT maNhaCungCap, tenNhaCungCap,
                       soDienThoai, email, diaChi
                FROM Nhacungcap
                ORDER BY maNhaCungCap
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                NhaCungCap n = new NhaCungCap();
                n.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                n.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                n.setSoDienThoai(rs.getString("soDienThoai"));
                n.setEmail(rs.getString("email"));
                n.setDiaChi(rs.getString("diaChi"));
                list.add(n);
            }
        }

        return list;
    }

    public String taoMaNhaCungCap() throws SQLException {
        return taoMa("Nhacungcap", "maNhaCungCap", "NCC", 3);
    }

    public boolean themNhaCungCap(NhaCungCap n) throws SQLException {

        String sql = """
                INSERT INTO Nhacungcap
                (maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, n.getMaNhaCungCap());
            ps.setString(2, n.getTenNhaCungCap());
            ps.setString(3, n.getSoDienThoai());
            ps.setString(4, n.getEmail());
            ps.setString(5, n.getDiaChi());

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // SAN PHAM THEO NHA CUNG CAP (chon mat hang cho phieu nhap)
    // =========================================================

    public ArrayList<SanPham> timSanPhamTheoNhaCungCap(String maNhaCungCap) throws SQLException {

        ArrayList<SanPham> list = new ArrayList<>();

        String sql = """
                SELECT maSanPham, tenSanPham, giaNhap, giaBan,
                       maDanhMuc, maNhaCungCap
                FROM SanPham
                WHERE maNhaCungCap = ?
                ORDER BY maSanPham
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, maNhaCungCap);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new SanPham(rs.getString("maSanPham"),
                            rs.getString("tenSanPham"),
                            rs.getDouble("giaNhap"), rs.getDouble("giaBan"),
                            rs.getString("maDanhMuc"),
                            rs.getString("maNhaCungCap")));
                }
            }
        }

        return list;
    }

    // Sinh ma tu dong dang TIENTO + so (DM005, SP009...): lay so lon nhat dang co roi cong 1
    private static String taoMa(String bang, String cot,
                                String tienTo, int soChuSo) throws SQLException {

        int max = 0;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT " + cot + " FROM " + bang);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                String ma = rs.getString(1);

                if (ma != null && ma.startsWith(tienTo)) {
                    String so = ma.substring(tienTo.length());

                    if (so.matches("\\d{1,9}")) {
                        max = Math.max(max, Integer.parseInt(so));
                    }
                }
            }
        }

        return tienTo + String.format("%0" + soChuSo + "d", max + 1);
    }
}
