package dao;
import ConnectDB.connectDB;
import java.sql.*;
import java.util.*;
public class TraCuuKhoDAO {
    public record NhaCungCapTraCuu(String maNhaCungCap, String tenNhaCungCap) {
        @Override public String toString() { return maNhaCungCap + " - " + tenNhaCungCap; }
    }
    private static final class KetQuaKiemKe {
        int soDong, soLech;
        boolean conDongChuaKiem;
    }
    private record ThongKePhieuNhap(int soDong, double tongTien) { }
    public List<NhaCungCapTraCuu> layDanhSachNhaCungCap() throws SQLException {
        String sql = "SELECT maNhaCungCap,tenNhaCungCap FROM NhaCungCap ORDER BY tenNhaCungCap";
        List<NhaCungCapTraCuu> rows = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rows.add(new NhaCungCapTraCuu(rs.getString(1), rs.getString(2)));
        }
        return rows;
    }
    public List<Object[]> timKiemPhieuNhap(String tuKhoa) throws SQLException {
        String sql = "SELECT p.maPhieuNhap,p.ngayNhap,p.maNhanVien,nv.hoTen,p.maNhaCungCap,"
                + "n.tenNhaCungCap,p.trangThai FROM PhieuNhapKho p "
                + "LEFT JOIN NhanVien nv ON nv.maNhanVien=p.maNhanVien "
                + "LEFT JOIN NhaCungCap n ON n.maNhaCungCap=p.maNhaCungCap ORDER BY p.ngayNhap DESC";
        String keyword = chuanHoa(tuKhoa);
        Set<String> phieuCoSanPham = timMaPhieuCoSanPham("ChiTietPhieuNhap", "maPhieuNhap", keyword);
        Map<String, ThongKePhieuNhap> thongKe = layThongKePhieuNhap();
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maPhieu = rs.getString("maPhieuNhap");
                if (!keyword.isEmpty() && !phieuCoSanPham.contains(maPhieu)
                        && !coTuKhoa(keyword, maPhieu, rs.getString("maNhanVien"), rs.getString("hoTen"),
                                rs.getString("maNhaCungCap"), rs.getString("tenNhaCungCap"))) continue;
                ThongKePhieuNhap tongKet = thongKe.getOrDefault(maPhieu, new ThongKePhieuNhap(0, 0));
                rows.add(new Object[] { maPhieu, rs.getTimestamp("ngayNhap"), rs.getString("maNhanVien"),
                        rs.getString("maNhaCungCap"), rs.getString("tenNhaCungCap"), tongKet.soDong(),
                        tongKet.tongTien(), rs.getBoolean("trangThai") });
            }
        }
        return rows;
    }
    private Map<String, ThongKePhieuNhap> layThongKePhieuNhap() throws SQLException {
        String sql = "SELECT maPhieuNhap,COUNT(*) AS soDong,"
                + "SUM(soLuongNhap*donGiaNhap) AS tongTien FROM ChiTietPhieuNhap GROUP BY maPhieuNhap";
        Map<String, ThongKePhieuNhap> tongKet = new HashMap<>();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) tongKet.put(rs.getString("maPhieuNhap"), new ThongKePhieuNhap(
                    rs.getInt("soDong"), rs.getDouble("tongTien")));
        }
        return tongKet;
    }
    public List<Object[]> timKiemPhieuKiemKe(String tuKhoa) throws SQLException {
        String keyword = chuanHoa(tuKhoa);
        Set<String> phieuCoSanPham = new HashSet<>();
        phieuCoSanPham.addAll(timMaPhieuCoSanPham("ChiTietPhieuKiemKeLo", "maPhieuKiemKe", keyword));
        phieuCoSanPham.addAll(timMaPhieuCoSanPham("ChiTietPhieuKiemKe", "maPhieuKiemKe", keyword));
        Map<String, KetQuaKiemKe> ketQua = new HashMap<>();
        docTongKetKiemKe("ChiTietPhieuKiemKeLo", ketQua);
        docTongKetKiemKe("ChiTietPhieuKiemKe", ketQua);
        String sql = "SELECT p.maPhieuKiemKe,COALESCE(p.ngayChot,p.ngayKiemKe) AS ngayChot,"
                + "p.maNhanVien,nv.hoTen,p.trangThai FROM PhieuKiemKe p "
                + "LEFT JOIN NhanVien nv ON nv.maNhanVien=p.maNhanVien "
                + "WHERE ISNULL(p.trangThai,0)=1 ORDER BY COALESCE(p.ngayChot,p.ngayKiemKe) DESC";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maPhieu = rs.getString("maPhieuKiemKe");
                if (!keyword.isEmpty() && !phieuCoSanPham.contains(maPhieu)
                        && !coTuKhoa(keyword, maPhieu, rs.getString("maNhanVien"), rs.getString("hoTen"))) continue;
                KetQuaKiemKe summary = ketQua.getOrDefault(maPhieu, new KetQuaKiemKe());
                rows.add(new Object[] { maPhieu, rs.getTimestamp("ngayChot"), rs.getString("maNhanVien"),
                        rs.getBoolean("trangThai"), summary.soDong, summary.soLech, summary.conDongChuaKiem ? 1 : 0 });
            }
        }
        return rows;
    }
    public List<Object[]> layChiTietPhieuKiemKe(String maPhieu) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        docChiTietKiemKeLo(maPhieu, rows);
        docChiTietKiemKeCu(maPhieu, rows);
        rows.sort(Comparator.comparing(row -> String.valueOf(row[0]) + String.valueOf(row[2])));
        return rows;
    }
    private void docChiTietKiemKeLo(String maPhieu, List<Object[]> rows) throws SQLException {
        String sql = "SELECT d.maSanPham,s.tenSanPham,d.maPhieuNhap,pn.ngayNhap,d.soLuongHeThong,d.soLuongThucTe "
                + "FROM ChiTietPhieuKiemKeLo d JOIN SanPham s ON s.maSanPham=d.maSanPham "
                + "JOIN PhieuNhapKho pn ON pn.maPhieuNhap=d.maPhieuNhap WHERE d.maPhieuKiemKe=?";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(new Object[] { rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getTimestamp(4), rs.getInt(5), rs.getObject(6) });
            }
        }
    }
    private void docChiTietKiemKeCu(String maPhieu, List<Object[]> rows) throws SQLException {
        String sql = "SELECT d.maSanPham,s.tenSanPham,d.soLuongHeThong,d.soLuongThucTe "
                + "FROM ChiTietPhieuKiemKe d JOIN SanPham s ON s.maSanPham=d.maSanPham WHERE d.maPhieuKiemKe=?";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(new Object[] { rs.getString(1), rs.getString(2), null, null,
                        rs.getInt(3), rs.getObject(4) });
            }
        }
    }
    private void docTongKetKiemKe(String bangChiTiet, Map<String, KetQuaKiemKe> tongKet) throws SQLException {
        String sql = "SELECT maPhieuKiemKe,COUNT(*) AS soDong,"
                + "SUM(CASE WHEN soLuongThucTe IS NOT NULL AND soLuongThucTe<>soLuongHeThong THEN 1 ELSE 0 END) AS soLech,"
                + "MAX(CASE WHEN soLuongThucTe IS NULL THEN 1 ELSE 0 END) AS conThieu FROM "
                + bangChiTiet + " GROUP BY maPhieuKiemKe";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                KetQuaKiemKe row = tongKet.computeIfAbsent(rs.getString("maPhieuKiemKe"), key -> new KetQuaKiemKe());
                row.soDong += rs.getInt("soDong");
                row.soLech += rs.getInt("soLech");
                row.conDongChuaKiem |= rs.getInt("conThieu") > 0;
            }
        }
    }
    private Set<String> timMaPhieuCoSanPham(String bangChiTiet, String cotMaPhieu, String tuKhoa) throws SQLException {
        Set<String> maPhieu = new HashSet<>();
        if (tuKhoa.isEmpty()) return maPhieu;
        String sql = "SELECT DISTINCT d." + cotMaPhieu + " FROM " + bangChiTiet + " d "
                + "JOIN SanPham s ON s.maSanPham=d.maSanPham WHERE LOWER(d.maSanPham) LIKE ? OR LOWER(s.tenSanPham) LIKE ?";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + tuKhoa + "%";
            ps.setString(1, like); ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) maPhieu.add(rs.getString(1)); }
        }
        return maPhieu;
    }
    private boolean coTuKhoa(String tuKhoa, String... giaTri) {
        for (String value : giaTri)
            if (value != null && value.toLowerCase(Locale.ROOT).contains(tuKhoa)) return true;
        return false;
    }
    private String chuanHoa(String tuKhoa) { return tuKhoa == null ? "" : tuKhoa.trim().toLowerCase(Locale.ROOT); }
}
