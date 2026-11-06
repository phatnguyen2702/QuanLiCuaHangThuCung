package dao;
import ConnectDB.connectDB;
import control.PhanQuyenControl;
import entity.DanhMuc;
import entity.NhaCungCap;
import entity.SanPham;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class SanPhamDAO {
    private record NhomGoiY(String nhomTuoi, String kichThuoc) { }
    private static final String SELECT_SAN_PHAM = """
            SELECT sp.maSanPham,sp.tenSanPham,sp.giaNhap,sp.giaBan,sp.maDanhMuc,dm.tenDanhMuc,
                   sp.maNhaCungCap,ncc.tenNhaCungCap
            FROM SanPham sp
            JOIN DanhMuc dm ON dm.maDanhMuc=sp.maDanhMuc
            JOIN NhaCungCap ncc ON ncc.maNhaCungCap=sp.maNhaCungCap
            """;
    public static final class DotNhap {
        private final String maPhieuNhap;
        private final LocalDateTime ngayNhap;
        public DotNhap(String maPhieuNhap, LocalDateTime ngayNhap) {
            this.maPhieuNhap = maPhieuNhap;
            this.ngayNhap = ngayNhap;
        }
        public String getMaPhieuNhap() { return maPhieuNhap; }
        @Override public String toString() {
            return maPhieuNhap + (ngayNhap == null ? "" : " · " + ngayNhap.toLocalDate());
        }
    }
    public static final class LoCanhBaoHanSuDung {
        private final String maSanPham, tenSanPham, tenDanhMuc, maPhieuNhap;
        private final Date ngayNhap, hanSuDung;
        private final int soLuongCon, soNgayCon;
        private final boolean ngungKinhDoanh;
        private LoCanhBaoHanSuDung(SanPham sp, int soNgayCon) {
            maSanPham = sp.getMaSanPham();
            tenSanPham = sp.getTenSanPham();
            tenDanhMuc = sp.getTenDanhMuc();
            maPhieuNhap = sp.getMaPhieuNhap();
            ngayNhap = sp.getNgayNhap() == null ? null : Date.valueOf(sp.getNgayNhap());
            hanSuDung = sp.getHanSuDung() == null ? null : Date.valueOf(sp.getHanSuDung());
            soLuongCon = sp.getTonKho();
            this.soNgayCon = soNgayCon;
            ngungKinhDoanh = sp.isNgungKinhDoanh();
        }
        public String getMaSanPham() { return maSanPham; }
        public String getTenSanPham() { return tenSanPham; }
        public String getTenDanhMuc() { return tenDanhMuc; }
        public String getMaPhieuNhap() { return maPhieuNhap; }
        public Date getNgayNhap() { return ngayNhap; }
        public Date getHanSuDung() { return hanSuDung; }
        public int getSoLuongCon() { return soLuongCon; }
        public int getSoNgayCon() { return soNgayCon; }
        public boolean isNgungKinhDoanh() { return ngungKinhDoanh; }
    }
    private static final class LoTon {
        final SanPham sanPham;
        final int soLuongNhap;
        final int soLuongDaXuLy;
        LoTon(SanPham sanPham, int soLuongNhap, int soLuongDaXuLy) {
            this.sanPham = sanPham;
            this.soLuongNhap = soLuongNhap;
            this.soLuongDaXuLy = soLuongDaXuLy;
        }
    }
    public ArrayList<SanPham> timKiemSanPham(String tuKhoa, String maDanhMuc, String maNhaCungCap)
            throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_SAN_PHAM).append(" WHERE (sp.maSanPham LIKE ? OR sp.tenSanPham LIKE ?)");
        List<String> thamSo = new ArrayList<>();
        String tuKhoaLike = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
        thamSo.add(tuKhoaLike);
        thamSo.add(tuKhoaLike);
        if (maDanhMuc != null && !maDanhMuc.isBlank()) {
            sql.append(" AND sp.maDanhMuc=?");
            thamSo.add(maDanhMuc);
        }
        if (maNhaCungCap != null && !maNhaCungCap.isBlank()) {
            sql.append(" AND sp.maNhaCungCap=?");
            thamSo.add(maNhaCungCap);
        }
        sql.append(" ORDER BY sp.maSanPham");
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < thamSo.size(); i++) ps.setString(i + 1, thamSo.get(i));
            try (ResultSet rs = ps.executeQuery()) { return docDanhSachSanPham(rs); }
        }
    }
    public ArrayList<SanPham> layTatCaSanPham() throws SQLException {
        return timKiemSanPham("", null, null);
    }
    public ArrayList<SanPham> timSanPhamGoiYThucAn(String loaiThuCung, String nhomTuoi, String kichThuoc)
            throws SQLException {
        Map<String, List<NhomGoiY>> goiYTheoSanPham = layNhomGoiYTheoSanPham(loaiThuCung);
        Map<String, SanPham> sanPhamCoTheBan = new HashMap<>();
        LocalDate homNay = LocalDate.now();
        for (SanPham lo : layDanhSachSanPhamTheoLoTon()) {
            if (lo.isNgungKinhDoanh() || lo.getTonKho() <= 0) continue;
            if (lo.getHanSuDung() != null && lo.getHanSuDung().isBefore(homNay)) continue;
            List<NhomGoiY> nhomGoiY = goiYTheoSanPham.get(lo.getMaSanPham());
            if (nhomGoiY == null || !coNhomGoiYPhuHop(nhomGoiY, nhomTuoi, kichThuoc)) continue;
            SanPham sanPham = sanPhamCoTheBan.get(lo.getMaSanPham());
            if (sanPham == null) {
                sanPham = lo;
                sanPham.setTonKho(0);
                sanPhamCoTheBan.put(sanPham.getMaSanPham(), sanPham);
            }
            sanPham.setTonKho(sanPham.getTonKho() + lo.getTonKho());
        }
        ArrayList<SanPham> ketQua = new ArrayList<>(sanPhamCoTheBan.values());
        ketQua.sort(java.util.Comparator.comparing(SanPham::getTenSanPham, String.CASE_INSENSITIVE_ORDER));
        return ketQua;
    }
    private Map<String, List<NhomGoiY>> layNhomGoiYTheoSanPham(String loaiThuCung) throws SQLException {
        String sql = "SELECT maSanPham,nhomTuoi,kichThuoc FROM GoiYThucAn WHERE loaiThuCung=?";
        Map<String, List<NhomGoiY>> nhomTheoSanPham = new HashMap<>();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loaiThuCung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    nhomTheoSanPham.computeIfAbsent(rs.getString("maSanPham"), key -> new ArrayList<>())
                            .add(new NhomGoiY(rs.getString("nhomTuoi"), rs.getString("kichThuoc")));
                }
            }
        }
        return nhomTheoSanPham;
    }
    private boolean coNhomGoiYPhuHop(List<NhomGoiY> nhomGoiY, String nhomTuoi, String kichThuoc) {
        for (NhomGoiY nhom : nhomGoiY) {
            boolean dungTuoi = "Tất cả độ tuổi".equals(nhomTuoi)
                    || nhomTuoi.equals(nhom.nhomTuoi()) || "Mọi độ tuổi".equals(nhom.nhomTuoi());
            boolean dungKichThuoc = "Tất cả kích thước".equals(kichThuoc)
                    || kichThuoc.equals(nhom.kichThuoc()) || "Mọi kích thước".equals(nhom.kichThuoc());
            if (dungTuoi && dungKichThuoc) return true;
        }
        return false;
    }
    public int laySoLuongTonSanPham(String maSanPham) throws SQLException {
        return layTonKhoTatCaSanPham().getOrDefault(maSanPham, 0);
    }
    public Map<String, Integer> layTonKhoTatCaSanPham() throws SQLException {
        try (Connection con = connectDB.getConnection()) {
            return layTonKhoTatCaSanPham(con);
        }
    }
    public Map<String, Integer> layTonKhoTatCaSanPham(Connection con) throws SQLException {
        Map<String, Integer> tonKho = new HashMap<>();
        congDon(tonKho, layTongTheoSanPham(con, "SELECT ct.maSanPham,SUM(CASE "
                + "WHEN ct.soLuongNhap>ISNULL(ct.soLuongDaXuLy,0) THEN ct.soLuongNhap-ISNULL(ct.soLuongDaXuLy,0) "
                + "ELSE 0 END) AS soLuong FROM ChiTietPhieuNhap ct JOIN PhieuNhapKho pn "
                + "ON pn.maPhieuNhap=ct.maPhieuNhap WHERE pn.trangThai=1 AND ISNULL(ct.trangThai,1)=1 GROUP BY ct.maSanPham"), 1);
        congDon(tonKho, layTongTheoSanPham(con, "SELECT maSanPham,SUM(soLuong) AS soLuong FROM ChiTietHoaDon GROUP BY maSanPham"), -1);
        congDon(tonKho, layTongTheoSanPham(con, "SELECT k.maSanPham,SUM(k.soLuongThucTe-k.soLuongHeThong) AS soLuong "
                + "FROM ChiTietPhieuKiemKe k JOIN PhieuKiemKe p ON p.maPhieuKiemKe=k.maPhieuKiemKe "
                + "WHERE p.trangThai=1 AND k.soLuongThucTe IS NOT NULL GROUP BY k.maSanPham"), 1);
        congDon(tonKho, layTongTheoLo(con), 1);
        return tonKho;
    }
    public Map<String, Integer> layTonBanDuocTatCaSanPham() throws SQLException {
        try (Connection con = connectDB.getConnection()) {
            return layTonBanDuocTatCaSanPham(con);
        }
    }
    public Map<String, Integer> layTonBanDuocTatCaSanPham(Connection con) throws SQLException {
        Map<String, Integer> tonBanDuoc = new HashMap<>();
        LocalDate homNay = LocalDate.now();
        for (SanPham sp : layDanhSachLoTon(con)) {
            if (!sp.isNgungKinhDoanh() && (sp.getHanSuDung() == null || !sp.getHanSuDung().isBefore(homNay)))
                tonBanDuoc.merge(sp.getMaSanPham(), sp.getTonKho(), Integer::sum);
        }
        return tonBanDuoc;
    }
    public ArrayList<SanPham> layDanhSachSanPhamTheoLoTon() throws SQLException {
        ArrayList<SanPham> rows = new ArrayList<>();
        for (SanPham sp : layDanhSachLoTon()) if (sp.getTonKho() > 0) rows.add(sp);
        return rows;
    }
    public Map<String, Integer> layTongTonKhoTheoDanhMuc() throws SQLException {
        Map<String, Integer> tonSanPham = layTonKhoTatCaSanPham();
        Map<String, Integer> tongTheoDanhMuc = new HashMap<>();
        String sql = "SELECT maDanhMuc,maSanPham FROM SanPham";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maSanPham = rs.getString("maSanPham");
                tongTheoDanhMuc.merge(rs.getString("maDanhMuc"), tonSanPham.getOrDefault(maSanPham, 0), Integer::sum);
            }
        }
        return tongTheoDanhMuc;
    }
    private int layTonKhoSanPham(Connection con, String maSanPham) throws SQLException {
        return layDanhSachLoTon(con).stream().filter(sp -> maSanPham.equals(sp.getMaSanPham()))
                .mapToInt(SanPham::getTonKho).sum();
    }
    private Map<String, Integer> layTongTheoSanPham(Connection con, String sql) throws SQLException {
        Map<String, Integer> totals = new HashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) totals.put(rs.getString(1), rs.getInt("soLuong"));
        }
        return totals;
    }
    private void congDon(Map<String, Integer> ketQua, Map<String, Integer> giaTri, int heSo) {
        giaTri.forEach((ma, soLuong) -> ketQua.merge(ma, soLuong * heSo, Integer::sum));
    }
    private Map<String, Integer> layTongTheoLo(Connection con) throws SQLException {
        String sql = "SELECT k.maSanPham,k.maPhieuNhap,SUM(k.soLuongThucTe-k.soLuongHeThong) AS soLuong "
                + "FROM ChiTietPhieuKiemKeLo k JOIN PhieuKiemKe p ON p.maPhieuKiemKe=k.maPhieuKiemKe "
                + "WHERE p.trangThai=1 AND k.soLuongThucTe IS NOT NULL GROUP BY k.maSanPham,k.maPhieuNhap";
        Map<String, Integer> totals = new HashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) totals.put(rs.getString("maSanPham") + "|" + rs.getString("maPhieuNhap"), rs.getInt("soLuong"));
        }
        return totals;
    }
    public LocalDate layHanSuDungSomNhatSanPham(String maSanPham) throws SQLException {
        String sql = "SELECT MIN(ct.hanSuDung) FROM ChiTietPhieuNhap ct JOIN PhieuNhapKho pn "
                + "ON pn.maPhieuNhap=ct.maPhieuNhap WHERE ct.maSanPham=? AND pn.trangThai=1 "
                + "AND ISNULL(ct.trangThai,1)=1 AND ct.hanSuDung IS NOT NULL";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Date date = rs.getDate(1);
                return date == null ? null : date.toLocalDate();
            }
        }
    }
    public ArrayList<DotNhap> layDanhSachDotNhapSanPham() throws SQLException {
        return layDanhSachDotNhapSanPham(null);
    }
    public ArrayList<DotNhap> layDanhSachDotNhapSanPham(String maSanPham) throws SQLException {
        String sql = "SELECT DISTINCT p.maPhieuNhap,p.ngayNhap FROM PhieuNhapKho p "
                + "JOIN ChiTietPhieuNhap ct ON ct.maPhieuNhap=p.maPhieuNhap "
                + "WHERE p.trangThai=1 AND (? IS NULL OR ct.maSanPham=?) ORDER BY p.ngayNhap DESC,p.maPhieuNhap DESC";
        ArrayList<DotNhap> rows = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSanPham); ps.setString(2, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ngayNhap = rs.getTimestamp("ngayNhap");
                    rows.add(new DotNhap(rs.getString("maPhieuNhap"), ngayNhap == null ? null : ngayNhap.toLocalDateTime()));
                }
            }
        }
        return rows;
    }
    public ArrayList<SanPham> timSanPhamTheoDotNhap(String tuKhoa, String maDanhMuc, String maPhieuNhap)
            throws SQLException {
        return timSanPhamTheoDotNhap(tuKhoa, maDanhMuc, maPhieuNhap, null);
    }
    public ArrayList<SanPham> timSanPhamTheoDotNhap(String tuKhoa, String maDanhMuc, String maPhieuNhap,
            String maSanPham) throws SQLException {
        if (maPhieuNhap == null) return timKiemSanPham(tuKhoa, maDanhMuc, null);
        ArrayList<SanPham> rows = new ArrayList<>();
        String keyword = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase(java.util.Locale.ROOT);
        for (SanPham sp : layDanhSachLoTon()) {
            if (!maPhieuNhap.equals(sp.getMaPhieuNhap()) || sp.getTonKho() < 0) continue;
            if (maDanhMuc != null && !maDanhMuc.equals(sp.getMaDanhMuc())) continue;
            if (maSanPham != null && !maSanPham.equals(sp.getMaSanPham())) continue;
            if (!sp.getMaSanPham().toLowerCase(java.util.Locale.ROOT).contains(keyword)
                    && !sp.getTenSanPham().toLowerCase(java.util.Locale.ROOT).contains(keyword)) continue;
            rows.add(sp);
        }
        return rows;
    }
    private ArrayList<SanPham> layDanhSachLoTon() throws SQLException {
        try (Connection con = connectDB.getConnection()) {
            return layDanhSachLoTon(con);
        }
    }
    private ArrayList<SanPham> layDanhSachLoTon(Connection con) throws SQLException {
        String sql = """
                SELECT ct.maSanPham, sp.tenSanPham, sp.giaBan,
                       sp.maDanhMuc, dm.tenDanhMuc,
                       sp.maNhaCungCap, ncc.tenNhaCungCap,
                       ct.maPhieuNhap, pn.ngayNhap, ct.hanSuDung,
                       ct.donGiaNhap, ct.soLuongNhap, ct.soLuongDaXuLy, ct.ngungKinhDoanh
                FROM ChiTietPhieuNhap ct
                JOIN PhieuNhapKho pn ON pn.maPhieuNhap = ct.maPhieuNhap
                JOIN SanPham sp ON sp.maSanPham = ct.maSanPham
                JOIN DanhMuc dm ON dm.maDanhMuc = sp.maDanhMuc
                JOIN NhaCungCap ncc ON ncc.maNhaCungCap = sp.maNhaCungCap
                WHERE pn.trangThai = 1 AND ISNULL(ct.trangThai, 1) = 1
                ORDER BY ct.maSanPham, pn.ngayNhap, ct.maPhieuNhap
                """;
        ArrayList<LoTon> lots = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SanPham sp = new SanPham();
                sp.setMaSanPham(rs.getString("maSanPham"));
                sp.setTenSanPham(rs.getString("tenSanPham"));
                sp.setGiaNhap(rs.getDouble("donGiaNhap"));
                sp.setGiaBan(rs.getDouble("giaBan"));
                sp.setMaDanhMuc(rs.getString("maDanhMuc"));
                sp.setTenDanhMuc(rs.getString("tenDanhMuc"));
                sp.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                sp.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                sp.setMaPhieuNhap(rs.getString("maPhieuNhap"));
                sp.setNgayNhap(toLocalDate(rs.getTimestamp("ngayNhap")));
                sp.setHanSuDung(toLocalDate(rs.getDate("hanSuDung")));
                sp.setNgungKinhDoanh(rs.getBoolean("ngungKinhDoanh"));
                lots.add(new LoTon(sp, rs.getInt("soLuongNhap"), rs.getInt("soLuongDaXuLy")));
            }
        }
        Map<String, Integer> daBan = layTongTheoSanPham(con, "SELECT maSanPham,SUM(soLuong) AS soLuong FROM ChiTietHoaDon GROUP BY maSanPham");
        Map<String, Integer> dieuChinhCu = layTongTheoSanPham(con, "SELECT k.maSanPham,SUM(k.soLuongThucTe-k.soLuongHeThong) AS soLuong "
                + "FROM ChiTietPhieuKiemKe k JOIN PhieuKiemKe p ON p.maPhieuKiemKe=k.maPhieuKiemKe "
                + "WHERE p.trangThai=1 AND k.soLuongThucTe IS NOT NULL GROUP BY k.maSanPham");
        Map<String, Integer> dieuChinhLo = layTongTheoLo(con);
        ArrayList<SanPham> rows = new ArrayList<>();
        String maTruoc = null;
        int banConLai = 0;
        boolean loDau = true;
        for (LoTon lot : lots) {
            SanPham sp = lot.sanPham;
            if (!sp.getMaSanPham().equals(maTruoc)) {
                maTruoc = sp.getMaSanPham();
                banConLai = daBan.getOrDefault(maTruoc, 0);
                loDau = true;
            }
            int daNhap = Math.max(0, lot.soLuongNhap - lot.soLuongDaXuLy);
            int truTuLo = Math.min(daNhap, Math.max(0, banConLai));
            banConLai -= truTuLo;
            int tonLo = daNhap - truTuLo;
            if (loDau) tonLo += dieuChinhCu.getOrDefault(maTruoc, 0);
            tonLo += dieuChinhLo.getOrDefault(maTruoc + "|" + sp.getMaPhieuNhap(), 0);
            sp.setTonKho(tonLo);
            rows.add(sp);
            loDau = false;
        }
        return rows;
    }
    public void ghiNhanXuLySanPhamTheoDotNhap(String maSanPham, String maPhieuNhap, int soLuong,
            String lyDo, String ghiChu, String maNhanVien) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);
        if (soLuong <= 0) throw new SQLException("Số lượng xử lý phải lớn hơn 0.");
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String sqlKhoaLo = "SELECT maPhieuNhap FROM ChiTietPhieuNhap WITH (UPDLOCK,HOLDLOCK) "
                        + "WHERE maPhieuNhap=? AND maSanPham=?";
                try (PreparedStatement ps = con.prepareStatement(sqlKhoaLo)) {
                    ps.setString(1, maPhieuNhap); ps.setString(2, maSanPham);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy lô nhập đã chọn.");
                    }
                }
                int tonLo = layDanhSachLoTon(con).stream()
                        .filter(sp -> maSanPham.equals(sp.getMaSanPham()) && maPhieuNhap.equals(sp.getMaPhieuNhap()))
                        .mapToInt(SanPham::getTonKho).findFirst()
                        .orElseThrow(() -> new SQLException("Không tìm thấy lô nhập còn tồn."));
                if (soLuong > tonLo) throw new SQLException("Số lượng xử lý vượt tồn lô (" + tonLo + ").");
                try (PreparedStatement ps = con.prepareStatement("UPDATE ChiTietPhieuNhap "
                        + "SET soLuongDaXuLy=ISNULL(soLuongDaXuLy,0)+? WHERE maPhieuNhap=? AND maSanPham=? "
                        + "AND ISNULL(soLuongDaXuLy,0)+?<=ISNULL(soLuongNhap,0)")) {
                    ps.setInt(1, soLuong); ps.setString(2, maPhieuNhap); ps.setString(3, maSanPham); ps.setInt(4, soLuong);
                    if (ps.executeUpdate() != 1) throw new SQLException("Không cập nhật được số lượng xử lý.");
                }
                String sql = "INSERT INTO ChiTietXuLyHang(maPhieuNhap,maSanPham,soLuong,lyDo,ghiChu,maNhanVien) VALUES(?,?,?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, maPhieuNhap); ps.setString(2, maSanPham); ps.setInt(3, soLuong);
                    ps.setString(4, lyDo); ps.setString(5, ghiChu);
                    if (maNhanVien == null || maNhanVien.isBlank()) ps.setNull(6, Types.VARCHAR);
                    else ps.setString(6, maNhanVien);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }
    public List<Object[]> layLichSuXuLySanPham() throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);
        String sql = "SELECT x.maXuLy,x.ngayXuLy,x.maSanPham,sp.tenSanPham,x.maPhieuNhap,x.soLuong,"
                + "x.lyDo,x.ghiChu,COALESCE(nv.hoTen,x.maNhanVien,N'—') AS nguoiXuLy "
                + "FROM ChiTietXuLyHang x JOIN SanPham sp ON sp.maSanPham=x.maSanPham "
                + "LEFT JOIN NhanVien nv ON nv.maNhanVien=x.maNhanVien ORDER BY x.ngayXuLy DESC,x.maXuLy DESC";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp ngay = rs.getTimestamp("ngayXuLy");
                String ngayText = ngay == null ? "" : ngay.toLocalDateTime()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                rows.add(new Object[] { rs.getLong("maXuLy"), ngayText, rs.getString("maSanPham"),
                        rs.getString("tenSanPham"), rs.getString("maPhieuNhap"), rs.getInt("soLuong"),
                        rs.getString("lyDo"), rs.getString("ghiChu"), rs.getString("nguoiXuLy") });
            }
        }
        return rows;
    }
    public void luuThongTinSanPham(SanPham sp, boolean dangSua, String tenDanhMuc,
            String nhomTuoi, String kichThuoc, String maNhanVien) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);
        String tenVaDanhMuc = chuanHoa(tenDanhMuc + " " + sp.getTenSanPham());
        String loaiThuCung = timLoaiThuCung(tenVaDanhMuc);
        boolean laThucAn = tenVaDanhMuc.contains("thuc an") || tenVaDanhMuc.contains("pate")
                || tenVaDanhMuc.contains("hat cho") || tenVaDanhMuc.contains("hat meo");
        if (laThucAn && loaiThuCung == null) throw new SQLException("Không xác định được thức ăn cho chó hay mèo.");
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (dangSua) kiemTraGiaNhapCoTon(con, sp);
                String sql = dangSua
                        ? "UPDATE SanPham SET tenSanPham=?,giaNhap=?,giaBan=?,maDanhMuc=?,maNhaCungCap=? WHERE maSanPham=?"
                        : "INSERT INTO SanPham(maSanPham,tenSanPham,giaNhap,giaBan,maDanhMuc,maNhaCungCap) VALUES(?,?,?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    if (dangSua) {
                        ps.setString(1, sp.getTenSanPham()); ps.setDouble(2, sp.getGiaNhap()); ps.setDouble(3, sp.getGiaBan());
                        ps.setString(4, sp.getMaDanhMuc()); ps.setString(5, sp.getMaNhaCungCap()); ps.setString(6, sp.getMaSanPham());
                    } else {
                        ps.setString(1, sp.getMaSanPham()); ps.setString(2, sp.getTenSanPham());
                        ps.setDouble(3, sp.getGiaNhap()); ps.setDouble(4, sp.getGiaBan());
                        ps.setString(5, sp.getMaDanhMuc()); ps.setString(6, sp.getMaNhaCungCap());
                    }
                    if (ps.executeUpdate() != 1) throw new SQLException("Không lưu được sản phẩm.");
                }
                luuGoiYThucAn(con, sp.getMaSanPham(), laThucAn ? loaiThuCung : null, nhomTuoi, kichThuoc);
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }
    public String taoMaSanPhamMoi() throws SQLException { return taoMaMoi("SanPham", "maSanPham", "SP"); }
    public boolean themSanPham(SanPham sp) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);
        String sql = "INSERT INTO SanPham(maSanPham,tenSanPham,giaNhap,giaBan,maDanhMuc,maNhaCungCap) VALUES(?,?,?,?,?,?)";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sp.getMaSanPham()); ps.setString(2, sp.getTenSanPham());
            ps.setDouble(3, sp.getGiaNhap()); ps.setDouble(4, sp.getGiaBan());
            ps.setString(5, sp.getMaDanhMuc()); ps.setString(6, sp.getMaNhaCungCap());
            return ps.executeUpdate() == 1;
        }
    }
    public boolean capNhatSanPham(SanPham sp) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);
        String sql = "UPDATE SanPham SET tenSanPham=?,giaNhap=?,giaBan=?,maDanhMuc=?,maNhaCungCap=? WHERE maSanPham=?";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sp.getTenSanPham()); ps.setDouble(2, sp.getGiaNhap()); ps.setDouble(3, sp.getGiaBan());
            ps.setString(4, sp.getMaDanhMuc()); ps.setString(5, sp.getMaNhaCungCap()); ps.setString(6, sp.getMaSanPham());
            return ps.executeUpdate() == 1;
        }
    }
    public void luuThongTinGoiYThucAn(String maSanPham, String tenDanhMuc, String tenSanPham,
            String nhomTuoi, String kichThuoc) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);
        String tenVaDanhMuc = chuanHoa(tenDanhMuc + " " + tenSanPham);
        String loai = timLoaiThuCung(tenVaDanhMuc);
        boolean laThucAn = tenVaDanhMuc.contains("thuc an") || tenVaDanhMuc.contains("pate")
                || tenVaDanhMuc.contains("hat cho") || tenVaDanhMuc.contains("hat meo");
        if (laThucAn && loai == null) throw new SQLException("Không xác định được thức ăn cho chó hay mèo.");
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                luuGoiYThucAn(con, maSanPham, laThucAn ? loai : null, nhomTuoi, kichThuoc);
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }
    public String[] layThongTinGoiYThucAn(String maSanPham) throws SQLException {
        String sql = "SELECT TOP 1 loaiThuCung,nhomTuoi,kichThuoc FROM GoiYThucAn WHERE maSanPham=?";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new String[] { rs.getString(1), rs.getString(2), rs.getString(3) } : null;
            }
        }
    }
    public boolean xoaSanPham(String maSanPham) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String sqlLichSu = "SELECT (SELECT COUNT(*) FROM ChiTietPhieuNhap WHERE maSanPham=?)"
                        + "+(SELECT COUNT(*) FROM ChiTietHoaDon WHERE maSanPham=?)"
                        + "+(SELECT COUNT(*) FROM ChiTietPhieuKiemKe WHERE maSanPham=?)"
                        + "+(SELECT COUNT(*) FROM ChiTietPhieuKiemKeLo WHERE maSanPham=?)";
                try (PreparedStatement ps = con.prepareStatement(sqlLichSu)) {
                    for (int i = 1; i <= 4; i++) ps.setString(i, maSanPham);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) throw new SQLException("Không thể xóa sản phẩm đã phát sinh giao dịch.");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM GoiYThucAn WHERE maSanPham=?")) {
                    ps.setString(1, maSanPham); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM SanPham WHERE maSanPham=?")) {
                    ps.setString(1, maSanPham);
                    boolean daXoa = ps.executeUpdate() == 1;
                    con.commit();
                    return daXoa;
                }
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }
    public int demLichSuSanPham(String maSanPham) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM ChiTietPhieuNhap WHERE maSanPham=?)"
                + "+(SELECT COUNT(*) FROM ChiTietHoaDon WHERE maSanPham=?)"
                + "+(SELECT COUNT(*) FROM ChiTietPhieuKiemKe WHERE maSanPham=?)"
                + "+(SELECT COUNT(*) FROM ChiTietPhieuKiemKeLo WHERE maSanPham=?)";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, maSanPham);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }
    public ArrayList<DanhMuc> layDanhSachDanhMucSanPham() throws SQLException {
        ArrayList<DanhMuc> rows = new ArrayList<>();
        String sql = "SELECT maDanhMuc,tenDanhMuc,moTa FROM DanhMuc ORDER BY maDanhMuc";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rows.add(new DanhMuc(rs.getString(1), rs.getString(2), rs.getString(3)));
        }
        return rows;
    }
    public ArrayList<NhaCungCap> layDanhSachNhaCungCapSanPham() throws SQLException {
        ArrayList<NhaCungCap> rows = new ArrayList<>();
        String sql = "SELECT maNhaCungCap,tenNhaCungCap,soDienThoai,email,diaChi FROM NhaCungCap ORDER BY maNhaCungCap";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rows.add(new NhaCungCap(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)));
        }
        return rows;
    }
    public ArrayList<LoCanhBaoHanSuDung> layDanhSachLoCanhBaoHanSuDung() throws SQLException {
        ArrayList<LoCanhBaoHanSuDung> rows = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (SanPham sp : layDanhSachLoTon()) {
            LocalDate expiry = sp.getHanSuDung();
            if (sp.getTonKho() > 0 && expiry != null && !expiry.isAfter(today.plusDays(30)))
                rows.add(new LoCanhBaoHanSuDung(sp, (int) java.time.temporal.ChronoUnit.DAYS.between(today, expiry)));
        }
        rows.sort(java.util.Comparator.comparing(LoCanhBaoHanSuDung::getHanSuDung)
                .thenComparing(LoCanhBaoHanSuDung::getTenSanPham));
        return rows;
    }
    public boolean ngungKinhDoanhLo(String maPhieuNhap, String maSanPham) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.CANH_BAO_HAN_SU_DUNG);
        String sql = "UPDATE ChiTietPhieuNhap SET ngungKinhDoanh=1 WHERE maPhieuNhap=? AND maSanPham=?";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieuNhap); ps.setString(2, maSanPham);
            return ps.executeUpdate() == 1;
        }
    }
    private void kiemTraGiaNhapCoTon(Connection con, SanPham sp) throws SQLException {
        String sql = "SELECT giaNhap FROM SanPham WITH (UPDLOCK,HOLDLOCK) WHERE maSanPham=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sp.getMaSanPham());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy sản phẩm cần sửa.");
                if (layTonKhoSanPham(con, sp.getMaSanPham()) > 0 && Double.compare(rs.getDouble("giaNhap"), sp.getGiaNhap()) != 0)
                    throw new SQLException("Chỉ được đổi giá nhập khi tồn kho bằng 0.");
            }
        }
    }
    private void luuGoiYThucAn(Connection con, String maSanPham, String loai, String nhomTuoi, String kichThuoc)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM GoiYThucAn WHERE maSanPham=?")) {
            ps.setString(1, maSanPham); ps.executeUpdate();
        }
        if (loai == null) return;
        String sql = "INSERT INTO GoiYThucAn(maSanPham,loaiThuCung,nhomTuoi,kichThuoc,ghiChu) VALUES(?,?,?,?,N'Nhập từ form sản phẩm')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSanPham); ps.setString(2, loai);
            ps.setString(3, giaTriMacDinh(nhomTuoi, "Chưa xác định"));
            ps.setString(4, giaTriMacDinh(kichThuoc, "Chưa xác định"));
            ps.executeUpdate();
        }
    }
    private ArrayList<SanPham> docDanhSachSanPham(ResultSet rs) throws SQLException {
        ArrayList<SanPham> rows = new ArrayList<>();
        while (rs.next()) rows.add(docSanPham(rs));
        Map<String, Integer> tonKho = layTonKhoTatCaSanPham();
        for (SanPham sp : rows) sp.setTonKho(tonKho.getOrDefault(sp.getMaSanPham(), 0));
        return rows;
    }
    private SanPham docSanPham(ResultSet rs) throws SQLException {
        SanPham sp = new SanPham();
        sp.setMaSanPham(rs.getString("maSanPham"));
        sp.setTenSanPham(rs.getString("tenSanPham"));
        sp.setGiaNhap(rs.getDouble("giaNhap"));
        sp.setGiaBan(rs.getDouble("giaBan"));
        sp.setMaDanhMuc(rs.getString("maDanhMuc"));
        sp.setTenDanhMuc(rs.getString("tenDanhMuc"));
        sp.setMaNhaCungCap(rs.getString("maNhaCungCap"));
        sp.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
        return sp;
    }
    private String taoMaMoi(String bang, String cot, String tienTo) throws SQLException {
        String sql = "SELECT " + cot + " FROM " + bang;
        int max = 0;
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString(1);
                if (ma != null && ma.matches(tienTo + "\\d+"))
                    max = Math.max(max, Integer.parseInt(ma.substring(tienTo.length())));
            }
        }
        return tienTo + String.format("%03d", max + 1);
    }
    private String timLoaiThuCung(String tenVaDanhMuc) {
        if (tenVaDanhMuc.contains("cho") || tenVaDanhMuc.contains("dog")) return "Chó";
        if (tenVaDanhMuc.contains("meo") || tenVaDanhMuc.contains("cat")) return "Mèo";
        return null;
    }
    private String chuanHoa(String value) {
        return java.text.Normalizer.normalize(value == null ? "" : value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(java.util.Locale.ROOT);
    }
    private String giaTriMacDinh(String value, String macDinh) {
        return value == null || value.isBlank() ? macDinh : value;
    }
    private LocalDate toLocalDate(Timestamp dateTime) {
        return dateTime == null ? null : dateTime.toLocalDateTime().toLocalDate();
    }
    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
