package dao;

import ConnectDB.connectDB;
import entity.ChiTietPhieuNhap;
import entity.PhieuNhapKho;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import entity.SanPham;

public class PhieuNhapKhoDAO {

    public String taoMaMoi() throws SQLException {
        return MaTuDong.taoMa("PhieuNhapKho", "maPhieuNhap", "PN", 3);
    }

    // Luu phieu + chi tiet trong 1 giao dich: loi o dau thi huy het
    public void luu(PhieuNhapKho phieu,
                    List<ChiTietPhieuNhap> chiTiet) throws SQLException {

        String sqlPhieu = """
                INSERT INTO PhieuNhapKho
                (maPhieuNhap, ngayNhap, trangThai, maNhanVien, maNhaCungCap)
                VALUES (?, ?, ?, ?, ?)
                """;

        String sqlChiTiet = """
                INSERT INTO ChiTietPhieuNhap
                (maPhieuNhap, maSanPham, soLuongNhap, donGiaNhap, trangThai)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = connectDB.getConnection()) {

            con.setAutoCommit(false);

            try {
                try (PreparedStatement ps = con.prepareStatement(sqlPhieu)) {
                    ps.setString(1, phieu.getMaPhieuNhap());
                    ps.setTimestamp(2, Timestamp.valueOf(phieu.getNgayNhap()));
                    ps.setBoolean(3, phieu.isTrangThai());
                    ps.setString(4, phieu.getMaNhanVien());
                    ps.setString(5, phieu.getMaNhaCungCap());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {
                    for (ChiTietPhieuNhap ct : chiTiet) {
                        ps.setString(1, phieu.getMaPhieuNhap());
                        ps.setString(2, ct.getMaSanPham());
                        ps.setInt(3, ct.getSoLuongNhap());
                        ps.setDouble(4, ct.getDonGiaNhap());
                        ps.setBoolean(5, phieu.isTrangThai());
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

    // =========================================================
    // NHAP BO SUNG SO LUONG (tu form Danh muc / San pham)
    // =========================================================

    /**
     * Ghi them so luong cho cac san pham: tao phieu nhap kho DA HOAN TAT,
     * moi nha cung cap 1 phieu (vi 1 phieu chi co 1 nha cung cap),
     * don gia nhap = gia nhap hien tai cua san pham. 1 giao dich.
     * soLuong.get(i) la so luong them cho sanPham.get(i); dong <= 0 bi bo qua.
     */
    public void nhapBoSung(List<SanPham> sanPham, List<Integer> soLuong,
                           String maNhanVien) throws SQLException {

        // Gom theo nha cung cap
        Map<String, List<Integer>> theoNcc = new LinkedHashMap<>();

        for (int i = 0; i < sanPham.size(); i++) {
            if (soLuong.get(i) != null && soLuong.get(i) > 0) {
                theoNcc.computeIfAbsent(sanPham.get(i).getMaNhaCungCap(),
                        k -> new ArrayList<>()).add(i);
            }
        }

        if (theoNcc.isEmpty()) {
            return;
        }

        String sqlPhieu = """
                INSERT INTO PhieuNhapKho
                (maPhieuNhap, ngayNhap, trangThai, maNhanVien, maNhaCungCap)
                VALUES (?, ?, 1, ?, ?)
                """;

        String sqlChiTiet = """
                INSERT INTO ChiTietPhieuNhap
                (maPhieuNhap, maSanPham, soLuongNhap, donGiaNhap, trangThai)
                VALUES (?, ?, ?, ?, 1)
                """;

        // Ma phieu ke tiep; moi phieu trong lan nay +1
        String ma0 = taoMaMoi();
        int so = Integer.parseInt(ma0.substring(2));

        try (Connection con = connectDB.getConnection()) {

            con.setAutoCommit(false);

            try {
                for (Map.Entry<String, List<Integer>> e : theoNcc.entrySet()) {

                    String maPhieu = String.format("PN%03d", so++);

                    try (PreparedStatement ps = con.prepareStatement(sqlPhieu)) {
                        ps.setString(1, maPhieu);
                        ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                        ps.setString(3, maNhanVien);
                        ps.setString(4, e.getKey());
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {
                        for (int i : e.getValue()) {
                            ps.setString(1, maPhieu);
                            ps.setString(2, sanPham.get(i).getMaSanPham());
                            ps.setInt(3, soLuong.get(i));
                            ps.setDouble(4, sanPham.get(i).getGiaNhap());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                con.commit();

            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
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
}
