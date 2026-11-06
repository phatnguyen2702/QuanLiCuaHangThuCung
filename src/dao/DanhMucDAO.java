package dao;
import ConnectDB.connectDB;
import control.PhanQuyenControl;
import entity.DanhMuc;
import entity.SanPham;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
public class DanhMucDAO {
    public ArrayList<DanhMuc> timKiemDanhMuc(String tuKhoa) throws SQLException {
        String sql = "SELECT maDanhMuc,tenDanhMuc,moTa FROM DanhMuc "
                + "WHERE maDanhMuc LIKE ? OR tenDanhMuc LIKE ? ORDER BY maDanhMuc";
        String keyword = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
        ArrayList<DanhMuc> danhMucs = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DanhMuc danhMuc = docDanhMuc(rs);
                    danhMucs.add(danhMuc);
                }
            }
        }
        Map<String, Integer> soSanPhamTheoDanhMuc = laySoSanPhamTheoDanhMuc();
        Map<String, Integer> tonTheoDanhMuc = new SanPhamDAO().layTongTonKhoTheoDanhMuc();
        for (DanhMuc danhMuc : danhMucs) {
            danhMuc.setSoSanPham(soSanPhamTheoDanhMuc.getOrDefault(danhMuc.getMaDanhMuc(), 0));
            danhMuc.setTongTon(tonTheoDanhMuc.getOrDefault(danhMuc.getMaDanhMuc(), 0));
        }
        return danhMucs;
    }
    public ArrayList<DanhMuc> layTatCaDanhMuc() throws SQLException {
        return timKiemDanhMuc("");
    }
    public int demTatCaDanhMuc() throws SQLException {
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM DanhMuc");
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    public String taoMaDanhMucMoi() throws SQLException {
        return taoMaMoi("DanhMuc", "maDanhMuc", "DM");
    }
    public boolean themDanhMuc(DanhMuc danhMuc) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.DANH_MUC_SAN_PHAM);
        String sql = "INSERT INTO DanhMuc(maDanhMuc,tenDanhMuc,moTa) VALUES(?,?,?)";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, danhMuc.getMaDanhMuc());
            ps.setString(2, danhMuc.getTenDanhMuc());
            ps.setString(3, danhMuc.getMoTa());
            return ps.executeUpdate() == 1;
        }
    }
    public boolean capNhatDanhMuc(DanhMuc danhMuc) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.DANH_MUC_SAN_PHAM);
        String sql = "UPDATE DanhMuc SET tenDanhMuc=?,moTa=? WHERE maDanhMuc=?";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, danhMuc.getTenDanhMuc());
            ps.setString(2, danhMuc.getMoTa());
            ps.setString(3, danhMuc.getMaDanhMuc());
            return ps.executeUpdate() == 1;
        }
    }
    public int[] demSanPhamTrongDanhMuc(String maDanhMuc) throws SQLException {
        return new int[] {
                demSoDongDanhMuc("SELECT COUNT(*) FROM SanPham WHERE maDanhMuc=?", maDanhMuc),
                demSoDongDanhMuc("SELECT COUNT(*) FROM ChiTietHoaDon h JOIN SanPham s ON s.maSanPham=h.maSanPham WHERE s.maDanhMuc=?", maDanhMuc),
                demSoDongDanhMuc("SELECT COUNT(*) FROM ChiTietPhieuNhap n JOIN SanPham s ON s.maSanPham=n.maSanPham WHERE s.maDanhMuc=?", maDanhMuc),
                demSoDongDanhMuc("SELECT COUNT(*) FROM ChiTietPhieuKiemKe k JOIN SanPham s ON s.maSanPham=k.maSanPham WHERE s.maDanhMuc=?", maDanhMuc)
        };
    }
    public boolean xoaDanhMuc(String maDanhMuc) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.DANH_MUC_SAN_PHAM);
        String sql = "DELETE FROM DanhMuc WHERE maDanhMuc=? AND NOT EXISTS "
                + "(SELECT 1 FROM SanPham WHERE maDanhMuc=?)";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDanhMuc);
            ps.setString(2, maDanhMuc);
            return ps.executeUpdate() == 1;
        }
    }
    public ArrayList<SanPham> laySanPhamTheoDanhMuc(String maDanhMuc) throws SQLException {
        String sql = "SELECT sp.maSanPham,sp.tenSanPham,sp.giaNhap,sp.giaBan,sp.maDanhMuc,sp.maNhaCungCap "
                + "FROM SanPham sp WHERE sp.maDanhMuc=? ORDER BY sp.maSanPham";
        ArrayList<SanPham> sanPhams = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
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
                    sanPhams.add(sp);
                }
            }
        }
        var tonKho = new SanPhamDAO().layTonKhoTatCaSanPham();
        for (SanPham sp : sanPhams) sp.setTonKho(tonKho.getOrDefault(sp.getMaSanPham(), 0));
        return sanPhams;
    }
    private DanhMuc docDanhMuc(ResultSet rs) throws SQLException {
        DanhMuc danhMuc = new DanhMuc();
        danhMuc.setMaDanhMuc(rs.getString("maDanhMuc"));
        danhMuc.setTenDanhMuc(rs.getString("tenDanhMuc"));
        danhMuc.setMoTa(rs.getString("moTa"));
        return danhMuc;
    }
    private Map<String, Integer> laySoSanPhamTheoDanhMuc() throws SQLException {
        String sql = "SELECT maDanhMuc,COUNT(*) AS soSanPham FROM SanPham GROUP BY maDanhMuc";
        Map<String, Integer> soSanPham = new java.util.HashMap<>();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) soSanPham.put(rs.getString("maDanhMuc"), rs.getInt("soSanPham"));
        }
        return soSanPham;
    }
    private int demSoDongDanhMuc(String sql, String maDanhMuc) throws SQLException {
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDanhMuc);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
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
}
