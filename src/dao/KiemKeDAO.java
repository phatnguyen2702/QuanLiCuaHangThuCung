package dao;
import ConnectDB.connectDB;
import control.PhanQuyenControl;
import entity.ChiTietPhieuKiemKe;
import entity.PhieuKiemKe;
import entity.PhieuNhapKho;
import entity.SanPham;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class KiemKeDAO {
    public String taoMaPhieuKiemKeMoi() throws SQLException {
        return taoMaMoi("PhieuKiemKe", "maPhieuKiemKe", "KK");
    }
    public void luuPhieuKiemKe(PhieuKiemKe phieu, List<ChiTietPhieuKiemKe> chiTiet) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.KIEM_KE);
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (!capNhatPhieuKiemKeNhap(con, phieu)) themPhieuKiemKeDaChot(con, phieu);
                xoaChiTietPhieuKiemKe(con, phieu.getMaPhieuKiemKe());
                themChiTietPhieuKiemKe(con, phieu.getMaPhieuKiemKe(), chiTiet);
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                throw ex;
            }
        }
    }
    private boolean capNhatPhieuKiemKeNhap(Connection con, PhieuKiemKe phieu) throws SQLException {
        String sql = "UPDATE PhieuKiemKe SET ngayKiemKe=?,trangThai=1,maNhanVien=?,ngayBatDau=?,ngayChot=? "
                + "WHERE maPhieuKiemKe=? AND ISNULL(trangThai,0)=0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(phieu.getNgayKiemKe()));
            ps.setString(2, phieu.getMaNhanVien());
            ps.setTimestamp(3, Timestamp.valueOf(phieu.getNgayBatDau()));
            ps.setTimestamp(4, Timestamp.valueOf(phieu.getNgayChot()));
            ps.setString(5, phieu.getMaPhieuKiemKe());
            return ps.executeUpdate() == 1;
        }
    }
    private void themPhieuKiemKeDaChot(Connection con, PhieuKiemKe phieu) throws SQLException {
        String sql = "INSERT INTO PhieuKiemKe(maPhieuKiemKe,ngayKiemKe,trangThai,maNhanVien,ngayBatDau,ngayChot) "
                + "VALUES(?,?,1,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phieu.getMaPhieuKiemKe());
            ps.setTimestamp(2, Timestamp.valueOf(phieu.getNgayKiemKe()));
            ps.setString(3, phieu.getMaNhanVien());
            ps.setTimestamp(4, Timestamp.valueOf(phieu.getNgayBatDau()));
            ps.setTimestamp(5, Timestamp.valueOf(phieu.getNgayChot()));
            ps.executeUpdate();
        }
    }
    public void luuPhieuKiemKeNhap(PhieuKiemKe phieu, List<ChiTietPhieuKiemKe> chiTiet) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.KIEM_KE);
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (!capNhatPhieuNhap(con, phieu)) themPhieuNhap(con, phieu);
                xoaChiTietPhieuKiemKe(con, phieu.getMaPhieuKiemKe());
                themChiTietPhieuKiemKe(con, phieu.getMaPhieuKiemKe(), chiTiet);
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                throw ex;
            }
        }
    }
    private boolean capNhatPhieuNhap(Connection con, PhieuKiemKe phieu) throws SQLException {
        String sql = "UPDATE PhieuKiemKe SET ngayKiemKe=?,trangThai=0,maNhanVien=?,ngayBatDau=?,ngayChot=NULL "
                + "WHERE maPhieuKiemKe=? AND ISNULL(trangThai,0)=0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(phieu.getNgayBatDau()));
            ps.setString(2, phieu.getMaNhanVien());
            ps.setTimestamp(3, Timestamp.valueOf(phieu.getNgayBatDau()));
            ps.setString(4, phieu.getMaPhieuKiemKe());
            return ps.executeUpdate() == 1;
        }
    }
    private void themPhieuNhap(Connection con, PhieuKiemKe phieu) throws SQLException {
        String sql = "INSERT INTO PhieuKiemKe(maPhieuKiemKe,ngayKiemKe,trangThai,maNhanVien,ngayBatDau) VALUES(?,?,0,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phieu.getMaPhieuKiemKe());
            ps.setTimestamp(2, Timestamp.valueOf(phieu.getNgayBatDau()));
            ps.setString(3, phieu.getMaNhanVien());
            ps.setTimestamp(4, Timestamp.valueOf(phieu.getNgayBatDau()));
            ps.executeUpdate();
        }
    }
    private void xoaChiTietPhieuKiemKe(Connection con, String maPhieu) throws SQLException {
        for (String table : List.of("ChiTietPhieuKiemKeLo", "ChiTietPhieuKiemKe")) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM " + table + " WHERE maPhieuKiemKe=?")) {
                ps.setString(1, maPhieu);
                ps.executeUpdate();
            }
        }
    }
    private void themChiTietPhieuKiemKe(Connection con, String maPhieu, List<ChiTietPhieuKiemKe> chiTiet) throws SQLException {
        String sql = "INSERT INTO ChiTietPhieuKiemKeLo(maPhieuKiemKe,maPhieuNhap,maSanPham,soLuongHeThong,soLuongThucTe,trangThai) "
                + "VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (ChiTietPhieuKiemKe row : chiTiet) {
                ps.setString(1, maPhieu);
                ps.setString(2, row.getMaPhieuNhap());
                ps.setString(3, row.getMaSanPham());
                ps.setInt(4, row.getSoLuongHeThong());
                if (row.getSoLuongThucTe() == null) ps.setNull(5, Types.INTEGER);
                else ps.setInt(5, row.getSoLuongThucTe());
                ps.setBoolean(6, row.isTrangThai());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
    public ArrayList<PhieuKiemKe> layDanhSachPhieuKiemKeNhap() throws SQLException {
        String sql = "SELECT maPhieuKiemKe,ngayKiemKe,trangThai,maNhanVien,ngayBatDau,ngayChot "
                + "FROM PhieuKiemKe WHERE ISNULL(trangThai,0)=0 ORDER BY ngayKiemKe DESC";
        ArrayList<PhieuKiemKe> rows = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) rows.add(docPhieuKiemKe(rs));
        }
        return rows;
    }
    public ArrayList<ChiTietPhieuKiemKe> layChiTietPhieuKiemKe(String maPhieu) throws SQLException {
        ArrayList<ChiTietPhieuKiemKe> rows = new ArrayList<>();
        String sqlLo = "SELECT k.maSanPham,s.tenSanPham,k.maPhieuNhap,pn.ngayNhap,k.soLuongHeThong,k.soLuongThucTe,k.trangThai "
                + "FROM ChiTietPhieuKiemKeLo k JOIN SanPham s ON s.maSanPham=k.maSanPham "
                + "JOIN PhieuNhapKho pn ON pn.maPhieuNhap=k.maPhieuNhap WHERE k.maPhieuKiemKe=?";
        String sqlCu = "SELECT k.maSanPham,s.tenSanPham,CAST(NULL AS VARCHAR(10)) AS maPhieuNhap,"
                + "CAST(NULL AS DATETIME2) AS ngayNhap,k.soLuongHeThong,k.soLuongThucTe,k.trangThai "
                + "FROM ChiTietPhieuKiemKe k JOIN SanPham s ON s.maSanPham=k.maSanPham WHERE k.maPhieuKiemKe=?";
        try (Connection con = connectDB.getConnection()) {
            docChiTiet(sqlLo, maPhieu, con, rows);
            docChiTiet(sqlCu, maPhieu, con, rows);
        }
        rows.sort(java.util.Comparator.comparing(ChiTietPhieuKiemKe::getMaSanPham)
                .thenComparing(ChiTietPhieuKiemKe::getMaPhieuNhap,
                        java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())));
        return rows;
    }
    private void docChiTiet(String sql, String maPhieu, Connection con,
            List<ChiTietPhieuKiemKe> rows) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(docChiTietPhieuKiemKe(rs, maPhieu));
            }
        }
    }
    public ArrayList<SanPham> layDanhSachSanPhamKemTonKho() throws SQLException {
        ArrayList<SanPham> rows = new SanPhamDAO().layDanhSachSanPhamTheoLoTon();
        rows.sort(java.util.Comparator.comparing(SanPham::getMaSanPham)
                .thenComparing(SanPham::getNgayNhap,
                        java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder()))
                .thenComparing(SanPham::getMaPhieuNhap));
        return rows;
    }
    private PhieuKiemKe docPhieuKiemKe(ResultSet rs) throws SQLException {
        PhieuKiemKe phieu = new PhieuKiemKe();
        phieu.setMaPhieuKiemKe(rs.getString("maPhieuKiemKe"));
        phieu.setTrangThai(rs.getBoolean("trangThai"));
        phieu.setMaNhanVien(rs.getString("maNhanVien"));
        Timestamp time = rs.getTimestamp("ngayKiemKe");
        phieu.setNgayKiemKe(time == null ? null : time.toLocalDateTime());
        time = rs.getTimestamp("ngayBatDau");
        phieu.setNgayBatDau(time == null ? null : time.toLocalDateTime());
        time = rs.getTimestamp("ngayChot");
        phieu.setNgayChot(time == null ? null : time.toLocalDateTime());
        return phieu;
    }
    private ChiTietPhieuKiemKe docChiTietPhieuKiemKe(ResultSet rs, String maPhieu) throws SQLException {
        ChiTietPhieuKiemKe row = new ChiTietPhieuKiemKe();
        row.setMaPhieuKiemKe(maPhieu);
        SanPham sanPham = new SanPham();
        sanPham.setMaSanPham(rs.getString("maSanPham"));
        sanPham.setTenSanPham(rs.getString("tenSanPham"));
        row.setSanPham(sanPham);
        row.setTenSanPham(sanPham.getTenSanPham());
        String maPhieuNhap = rs.getString("maPhieuNhap");
        if (maPhieuNhap != null) {
            PhieuNhapKho phieuNhap = new PhieuNhapKho();
            phieuNhap.setMaPhieuNhap(maPhieuNhap);
            Timestamp ngayNhap = rs.getTimestamp("ngayNhap");
            phieuNhap.setNgayNhap(ngayNhap == null ? null : ngayNhap.toLocalDateTime());
            row.setPhieuNhapKho(phieuNhap);
        }
        Timestamp ngayNhap = rs.getTimestamp("ngayNhap");
        row.setNgayNhap(ngayNhap == null ? null : ngayNhap.toLocalDateTime().toLocalDate());
        row.setSoLuongHeThong(rs.getInt("soLuongHeThong"));
        int soLuongThucTe = rs.getInt("soLuongThucTe");
        row.setSoLuongThucTe(rs.wasNull() ? null : soLuongThucTe);
        row.setTrangThai(rs.getBoolean("trangThai"));
        return row;
    }
    private String taoMaMoi(String bang, String cot, String tienTo) throws SQLException {
        int max = 0;
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT " + cot + " FROM " + bang);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString(1);
                if (ma != null && ma.matches(tienTo + "\\d+"))
                    max = Math.max(max, Integer.parseInt(ma.substring(tienTo.length())));
            }
        }
        return tienTo + String.format("%03d", max + 1);
    }
    private void rollback(Connection con, Exception ex) {
        try { con.rollback(); }
        catch (SQLException rollbackError) { ex.addSuppressed(rollbackError); }
    }
}
