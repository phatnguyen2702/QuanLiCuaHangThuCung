package dao;

import ConnectDB.connectDB;
import entity.DanhMuc;
import entity.SanPham;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cua man hinh "Danh muc": danh muc + cac thao tac ma form Them/Sua danh muc
 * can (liet ke san pham trong danh muc, ghi them so luong nhap kho).
 */
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
        return taoMa("DanhMuc", "maDanhMuc", "DM", 3);
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

    /** Chỉ xóa danh mục rỗng để không làm mất sản phẩm và lịch sử giao dịch. */
    public boolean xoa(String maDanhMuc) throws SQLException {
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement check = con.prepareStatement(
                        "SELECT COUNT(*) FROM SanPham WHERE maDanhMuc=?")) {
                    check.setString(1, maDanhMuc);
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("Không thể xóa danh mục đang có sản phẩm. Hãy chuyển hoặc xóa sản phẩm trước.");
                        }
                    }
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

    // =========================================================
    // SAN PHAM CUA DANH MUC + NHAP BO SUNG (dung trong form Sua danh muc)
    // =========================================================

    /** San pham thuoc 1 danh muc, kem ton kho hien tai. */
    public ArrayList<SanPham> timSanPhamTheoDanhMuc(String maDanhMuc) throws SQLException {

        ArrayList<SanPham> list = new ArrayList<>();

        String sql = """
                SELECT sp.maSanPham,
                       sp.tenSanPham,
                       sp.giaNhap,
                       sp.giaBan,
                       sp.maDanhMuc,
                       sp.maNhaCungCap,
                """ + SanPhamDAO.BIEU_THUC_TON + """
                       AS tonKho
                FROM SanPham sp
                WHERE sp.maDanhMuc = ?
                ORDER BY sp.maSanPham
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, maDanhMuc);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SanPham sp = new SanPham();
                    sp.setMaSanPham(rs.getString("maSanPham"));
                    sp.setTenSanPham(rs.getString("tenSanPham"));
                    sp.setGiaNhap(rs.getDouble("giaNhap"));
                    sp.setGiaBan(rs.getDouble("giaBan"));
                    sp.setMaDanhMuc(rs.getString("maDanhMuc"));
                    sp.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                    sp.setTonKho(rs.getInt("tonKho"));
                    list.add(sp);
                }
            }
        }

        return list;
    }

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
        int so = Integer.parseInt(
                taoMa("PhieuNhapKho", "maPhieuNhap", "PN", 3).substring(2));

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
