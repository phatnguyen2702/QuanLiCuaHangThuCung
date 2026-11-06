package dao;
import ConnectDB.connectDB;
import control.PhanQuyenControl;
import entity.ChiTietPhieuNhap;
import entity.NhaCungCap;
import entity.PhieuNhapKho;
import entity.SanPham;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class NhapKhoDAO {
    private record TongKetNhap(int soMatHang, double tongTien) { }
    public String taoMaPhieuNhapMoi() throws SQLException {
        return taoMaMoi("PhieuNhapKho", "maPhieuNhap", "PN");
    }
    public void luuPhieuNhap(PhieuNhapKho phieu, List<ChiTietPhieuNhap> chiTiet) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.NHAP_KHO);
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (!capNhatPhieuNhapNhap(con, phieu)) {
                    try (PreparedStatement ps = con.prepareStatement("""
                            INSERT INTO PhieuNhapKho(maPhieuNhap,ngayNhap,trangThai,maNhanVien,maNhaCungCap)
                            VALUES(?,?,?,?,?)
                            """)) {
                        ps.setString(1, phieu.getMaPhieuNhap());
                        ps.setTimestamp(2, Timestamp.valueOf(phieu.getNgayNhap()));
                        ps.setBoolean(3, phieu.isTrangThai());
                        ps.setString(4, phieu.getMaNhanVien());
                        ps.setString(5, phieu.getMaNhaCungCap());
                        ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietPhieuNhap WHERE maPhieuNhap=?")) {
                    ps.setString(1, phieu.getMaPhieuNhap());
                    ps.executeUpdate();
                }
                themChiTietPhieuNhap(con, phieu, chiTiet);
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                throw ex;
            }
        }
    }
    private boolean capNhatPhieuNhapNhap(Connection con, PhieuNhapKho phieu) throws SQLException {
        String sql = "UPDATE PhieuNhapKho SET ngayNhap=?,trangThai=?,maNhanVien=?,maNhaCungCap=? "
                + "WHERE maPhieuNhap=? AND ISNULL(trangThai,1)=0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(phieu.getNgayNhap()));
            ps.setBoolean(2, phieu.isTrangThai());
            ps.setString(3, phieu.getMaNhanVien());
            ps.setString(4, phieu.getMaNhaCungCap());
            ps.setString(5, phieu.getMaPhieuNhap());
            return ps.executeUpdate() == 1;
        }
    }
    private void themChiTietPhieuNhap(Connection con, PhieuNhapKho phieu, List<ChiTietPhieuNhap> chiTiet) throws SQLException {
        String sql = "INSERT INTO ChiTietPhieuNhap(maPhieuNhap,maSanPham,soLuongNhap,donGiaNhap,trangThai,hanSuDung) "
                + "VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (ChiTietPhieuNhap dong : chiTiet) {
                ps.setString(1, phieu.getMaPhieuNhap());
                ps.setString(2, dong.getMaSanPham());
                ps.setInt(3, dong.getSoLuongNhap());
                ps.setDouble(4, dong.getDonGiaNhap());
                ps.setBoolean(5, phieu.isTrangThai());
                if (dong.getHanSuDung() == null) ps.setNull(6, Types.DATE);
                else ps.setDate(6, Date.valueOf(dong.getHanSuDung()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
    public ArrayList<ChiTietPhieuNhap> layChiTietPhieuNhap(String maPhieu) throws SQLException {
        String sql = "SELECT ct.maSanPham,sp.tenSanPham,ct.soLuongNhap,ct.donGiaNhap,ct.hanSuDung "
                + "FROM ChiTietPhieuNhap ct JOIN SanPham sp ON sp.maSanPham=ct.maSanPham "
                + "WHERE ct.maPhieuNhap=? ORDER BY ct.maSanPham";
        ArrayList<ChiTietPhieuNhap> rows = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(docChiTietPhieuNhap(rs, maPhieu));
            }
        }
        return rows;
    }
    public static final class BanInPhieuNhap {
        private final PhieuNhapKho phieu;
        private final String tenNhanVien;
        private final String soDienThoaiNhaCungCap;
        private final String diaChiNhaCungCap;
        private final List<ChiTietPhieuNhap> chiTiet;
        public BanInPhieuNhap(PhieuNhapKho phieu, String tenNhanVien, String soDienThoaiNhaCungCap,
                String diaChiNhaCungCap, List<ChiTietPhieuNhap> chiTiet) {
            this.phieu = phieu;
            this.tenNhanVien = tenNhanVien;
            this.soDienThoaiNhaCungCap = soDienThoaiNhaCungCap;
            this.diaChiNhaCungCap = diaChiNhaCungCap;
            this.chiTiet = List.copyOf(chiTiet);
        }
        public PhieuNhapKho getPhieu() { return phieu; }
        public String getTenNhanVien() { return tenNhanVien; }
        public String getSoDienThoaiNhaCungCap() { return soDienThoaiNhaCungCap; }
        public String getDiaChiNhaCungCap() { return diaChiNhaCungCap; }
        public List<ChiTietPhieuNhap> getChiTiet() { return Collections.unmodifiableList(chiTiet); }
        public double getTongTien() { return chiTiet.stream().mapToDouble(ChiTietPhieuNhap::getThanhTien).sum(); }
        public int getTongSoLuong() { return chiTiet.stream().mapToInt(ChiTietPhieuNhap::getSoLuongNhap).sum(); }
    }
    public BanInPhieuNhap layThongTinInPhieuNhap(String maPhieu) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.TRA_CUU_NHAP_KHO);
        String sql = """
                SELECT pn.maPhieuNhap,pn.ngayNhap,pn.trangThai,pn.maNhanVien,
                       nv.hoTen AS tenNhanVien,pn.maNhaCungCap,n.tenNhaCungCap,
                       n.soDienThoai AS sdtNhaCungCap,n.diaChi AS diaChiNhaCungCap
                FROM PhieuNhapKho pn
                LEFT JOIN NhanVien nv ON nv.maNhanVien=pn.maNhanVien
                LEFT JOIN Nhacungcap n ON n.maNhaCungCap=pn.maNhaCungCap
                WHERE pn.maPhieuNhap=?
                """;
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy phiếu nhập " + maPhieu + ".");
                PhieuNhapKho phieu = docPhieuNhap(rs);
                return new BanInPhieuNhap(phieu, rs.getString("tenNhanVien"),
                        rs.getString("sdtNhaCungCap"), rs.getString("diaChiNhaCungCap"),
                        layChiTietPhieuNhap(maPhieu));
            }
        }
    }
    public ArrayList<PhieuNhapKho> layDanhSachPhieuNhap() throws SQLException {
        String sql = "SELECT pn.maPhieuNhap,pn.ngayNhap,pn.trangThai,pn.maNhanVien,"
                + "pn.maNhaCungCap,n.tenNhaCungCap FROM PhieuNhapKho pn "
                + "JOIN NhaCungCap n ON n.maNhaCungCap=pn.maNhaCungCap "
                + "WHERE ISNULL(pn.trangThai,1)=0 ORDER BY pn.maPhieuNhap";
        Map<String, TongKetNhap> tongKet = layTongKetPhieuNhap();
        ArrayList<PhieuNhapKho> rows = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                PhieuNhapKho phieu = docPhieuNhap(rs);
                phieu.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                TongKetNhap summary = tongKet.getOrDefault(phieu.getMaPhieuNhap(), new TongKetNhap(0, 0));
                phieu.setSoMatHang(summary.soMatHang());
                phieu.setTongTien(summary.tongTien());
                rows.add(phieu);
            }
        }
        return rows;
    }
    private Map<String, TongKetNhap> layTongKetPhieuNhap() throws SQLException {
        String sql = "SELECT maPhieuNhap,COUNT(*) AS soMatHang,"
                + "SUM(soLuongNhap*donGiaNhap) AS tongTien FROM ChiTietPhieuNhap GROUP BY maPhieuNhap";
        Map<String, TongKetNhap> tongKet = new HashMap<>();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) tongKet.put(rs.getString("maPhieuNhap"),
                    new TongKetNhap(rs.getInt("soMatHang"), rs.getDouble("tongTien")));
        }
        return tongKet;
    }
    public void hoanTatPhieuNhap(String maPhieu) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.NHAP_KHO);
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String sql = "UPDATE PhieuNhapKho SET trangThai=1,ngayNhap=? WHERE maPhieuNhap=? AND ISNULL(trangThai,1)=0";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setString(2, maPhieu);
                    if (ps.executeUpdate() != 1) throw new SQLException("Phiếu nhập không còn ở trạng thái nháp.");
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE ChiTietPhieuNhap SET trangThai=1 WHERE maPhieuNhap=?")) {
                    ps.setString(1, maPhieu);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                throw ex;
            }
        }
    }
    public void xoaPhieuNhap(String maPhieu) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.NHAP_KHO);
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietPhieuNhap WHERE maPhieuNhap=? AND EXISTS "
                        + "(SELECT 1 FROM PhieuNhapKho WHERE maPhieuNhap=? AND ISNULL(trangThai,1)=0)")) {
                    ps.setString(1, maPhieu);
                    ps.setString(2, maPhieu);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhieuNhapKho WHERE maPhieuNhap=? AND ISNULL(trangThai,1)=0")) {
                    ps.setString(1, maPhieu);
                    if (ps.executeUpdate() != 1) throw new SQLException("Chỉ có thể xóa phiếu nhập nháp.");
                }
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                throw ex;
            }
        }
    }
    public ArrayList<NhaCungCap> layDanhSachNhaCungCap() throws SQLException {
        String sql = "SELECT maNhaCungCap,tenNhaCungCap,soDienThoai,email,diaChi FROM Nhacungcap ORDER BY maNhaCungCap";
        ArrayList<NhaCungCap> rows = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                NhaCungCap nhaCungCap = new NhaCungCap();
                nhaCungCap.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                nhaCungCap.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                nhaCungCap.setSoDienThoai(rs.getString("soDienThoai"));
                nhaCungCap.setEmail(rs.getString("email"));
                nhaCungCap.setDiaChi(rs.getString("diaChi"));
                rows.add(nhaCungCap);
            }
        }
        return rows;
    }
    public String taoMaNhaCungCapMoi() throws SQLException {
        return taoMaMoi("Nhacungcap", "maNhaCungCap", "NCC");
    }
    public boolean themNhaCungCap(NhaCungCap nhaCungCap) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.NHAP_KHO);
        String sql = "INSERT INTO Nhacungcap(maNhaCungCap,tenNhaCungCap,soDienThoai,email,diaChi) VALUES(?,?,?,?,?)";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nhaCungCap.getMaNhaCungCap());
            ps.setString(2, nhaCungCap.getTenNhaCungCap());
            ps.setString(3, nhaCungCap.getSoDienThoai());
            ps.setString(4, nhaCungCap.getEmail());
            ps.setString(5, nhaCungCap.getDiaChi());
            return ps.executeUpdate() == 1;
        }
    }
    public ArrayList<SanPham> timSanPhamTheoNhaCungCap(String maNhaCungCap) throws SQLException {
        String sql = "SELECT maSanPham,tenSanPham,giaNhap,giaBan,maDanhMuc,maNhaCungCap FROM SanPham "
                + "WHERE maNhaCungCap=? ORDER BY maSanPham";
        ArrayList<SanPham> rows = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNhaCungCap);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(new SanPham(rs.getString("maSanPham"), rs.getString("tenSanPham"),
                        rs.getDouble("giaNhap"), rs.getDouble("giaBan"), rs.getString("maDanhMuc"), rs.getString("maNhaCungCap")));
            }
        }
        return rows;
    }
    private ChiTietPhieuNhap docChiTietPhieuNhap(ResultSet rs, String maPhieu) throws SQLException {
        ChiTietPhieuNhap dong = new ChiTietPhieuNhap();
        dong.setMaPhieuNhap(maPhieu);
        dong.setMaSanPham(rs.getString("maSanPham"));
        dong.setTenSanPham(rs.getString("tenSanPham"));
        dong.setSoLuongNhap(rs.getInt("soLuongNhap"));
        dong.setDonGiaNhap(rs.getDouble("donGiaNhap"));
        Date han = rs.getDate("hanSuDung");
        dong.setHanSuDung(han == null ? null : han.toLocalDate());
        return dong;
    }
    private PhieuNhapKho docPhieuNhap(ResultSet rs) throws SQLException {
        PhieuNhapKho phieu = new PhieuNhapKho();
        phieu.setMaPhieuNhap(rs.getString("maPhieuNhap"));
        Timestamp ngay = rs.getTimestamp("ngayNhap");
        phieu.setNgayNhap(ngay == null ? null : ngay.toLocalDateTime());
        phieu.setTrangThai(rs.getBoolean("trangThai"));
        phieu.setMaNhanVien(rs.getString("maNhanVien"));
        phieu.setMaNhaCungCap(rs.getString("maNhaCungCap"));
        return phieu;
    }
    private String taoMaMoi(String bang, String cot, String tienTo) throws SQLException {
        String sql = "SELECT " + cot + " FROM " + bang;
        int max = 0;
        try (Connection con = connectDB.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
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
