package dao;

import ConnectDB.connectDB;
import entity.DanhMuc;
import entity.NhaCungCap;
import entity.SanPham;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cua man hinh "San pham": san pham + danh sach danh muc / nha cung cap
 * cho o chon trong form + ghi them so luong nhap kho.
 */
public class SanPhamDAO {

    /*
     * Bang SanPham khong co cot ton kho, nen ton kho duoc TINH tu du lieu:
     *   ton = tong nhap (phieu nhap da hoan tat)
     *       - tong ban (ChiTietHoaDon)
     *       + chenh lech cac phieu kiem ke da chot (thuc te - he thong)
     */
    /** Bieu thuc SQL tinh ton kho cua san pham co bi danh "sp" (cac DAO khac dung lai de ton kho luon khop). */
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
        return taoMa("SanPham", "maSanPham", "SP", 3);
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

    // =========================================================
    // DANH SACH CHO O CHON (combobox) TRONG MAN HINH SAN PHAM
    // =========================================================

    public ArrayList<DanhMuc> layDanhSachDanhMuc() throws SQLException {

        ArrayList<DanhMuc> list = new ArrayList<>();

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT maDanhMuc, tenDanhMuc, moTa FROM DanhMuc ORDER BY maDanhMuc");
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                list.add(new DanhMuc(rs.getString("maDanhMuc"),
                        rs.getString("tenDanhMuc"), rs.getString("moTa")));
            }
        }

        return list;
    }

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
                list.add(new NhaCungCap(rs.getString("maNhaCungCap"),
                        rs.getString("tenNhaCungCap"), rs.getString("soDienThoai"),
                        rs.getString("email"), rs.getString("diaChi")));
            }
        }

        return list;
    }

    // =========================================================
    // NHAP BO SUNG SO LUONG (o "Them so luong" trong form san pham)
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
