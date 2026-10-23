package dao;

import ConnectDB.connectDB;
import entity.ThanhToan;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ThanhToanDao {

    public List<ThanhToan> timMatHangTheoMa(String ma) throws SQLException {
        List<ThanhToan> ketQua = new ArrayList<>();
        for (ThanhToan item : timMatHang(ma)) {
            if (item.getMaHang().equalsIgnoreCase(ma.trim())) ketQua.add(item);
        }
        return ketQua;
    }

    public List<String> layDanhSachMaSanPham() throws SQLException {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT maSanPham FROM SanPham ORDER BY maSanPham";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(rs.getString(1));
        }
        return ds;
    }

    public Map<String, String> layDanhSachSanPham() throws SQLException {
        Map<String, String> sanPhams = new LinkedHashMap<>();
        String sql = "SELECT maSanPham, tenSanPham FROM SanPham ORDER BY maSanPham";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                sanPhams.put(rs.getString("maSanPham"), rs.getString("tenSanPham"));
            }
        }
        return sanPhams;
    }

    public List<String> layDanhSachMaKhachHang() throws SQLException {
        List<String> ds = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT maKhachHang FROM KhachHang ORDER BY maKhachHang");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(rs.getString(1));
        }
        return ds;
    }

    public List<ThanhToan> timMatHang(String tuKhoa) throws SQLException {
        String sql = "SELECT TOP 30 maSanPham AS maHang, tenSanPham AS tenHang, "
                + "giaBan AS donGia FROM SanPham "
                + "WHERE maSanPham = ? OR tenSanPham LIKE ?";
        List<ThanhToan> matHangs = new ArrayList<>();

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            String keyword = tuKhoa.trim();
            ps.setString(1, keyword);
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal donGia = rs.getBigDecimal("donGia");
                    if (donGia == null || donGia.signum() < 0) {
                        throw new SQLException("Mặt hàng " + rs.getString("maHang")
                                + " có đơn giá không hợp lệ.");
                    }
                    matHangs.add(ThanhToan.sanPham(
                            rs.getString("maHang"), rs.getString("tenHang"), donGia));
                }
            }
        }
        return matHangs;
    }

    public Map<String, List<ThanhToan>> layPhieuDichVuDangXuLy(String maKhachHang)
            throws SQLException {
        String sql = "SELECT p.maPhieuDichVu, dv.maDichVu, dv.tenDichVu, "
                + "ctdv.soLuong, ctdv.donGia "
                + "FROM PhieuDichVu p "
                + "JOIN ChiTietDichVu ctdv ON ctdv.maPhieuDichVu = p.maPhieuDichVu "
                + "JOIN DichVu dv ON dv.maDichVu = ctdv.maDichVu "
                + "WHERE p.maKhachHang = ? AND p.trangThai = 0 "
                + "ORDER BY p.maPhieuDichVu, dv.maDichVu";
        Map<String, List<ThanhToan>> phieuDichVus = new LinkedHashMap<>();
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maPhieuDichVu = rs.getString("maPhieuDichVu");
                    BigDecimal donGia = rs.getBigDecimal("donGia");
                    if (donGia == null || donGia.signum() < 0) {
                        throw new SQLException("Dịch vụ trong phiếu " + maPhieuDichVu
                                + " có đơn giá không hợp lệ.");
                    }
                    ThanhToan dichVu = ThanhToan.dichVu(
                            rs.getString("maDichVu"), rs.getString("tenDichVu"), donGia);
                    dichVu.setSoLuong(rs.getInt("soLuong"));
                    phieuDichVus.computeIfAbsent(maPhieuDichVu, key -> new ArrayList<>())
                            .add(dichVu);
                }
            }
        }
        return phieuDichVus;
    }

    public ThanhToan.KhachHang timKhachHang(String maHoacSoDienThoai)
            throws SQLException {
        String sql = "SELECT TOP 1 maKhachHang, hoTen, soDienThoai "
                + "FROM KhachHang WHERE maKhachHang = ? OR soDienThoai = ?";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            String keyword = maHoacSoDienThoai.trim();
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ThanhToan.KhachHang(
                        rs.getString("maKhachHang"),
                        rs.getString("hoTen"),
                        rs.getString("soDienThoai"));
            }
        }
    }

    public ThanhToan.Voucher timVoucher(String maHoacTen) throws SQLException {
        String sql = "SELECT maVoucher, tenVoucher, phanTramGiam, dieuKien "
                + "FROM Vocher "
                + "WHERE trangThai = 1 AND (UPPER(maVoucher) = UPPER(?) "
                + "OR UPPER(tenVoucher) = UPPER(?))";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            String keyword = maHoacTen.trim();
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                BigDecimal phanTramGiam = rs.getBigDecimal("phanTramGiam");
                if (phanTramGiam == null
                        || phanTramGiam.signum() < 0
                        || phanTramGiam.compareTo(BigDecimal.valueOf(100)) > 0) {
                    throw new SQLException("Phần trăm giảm của voucher không hợp lệ.");
                }
                return new ThanhToan.Voucher(
                        rs.getString("maVoucher"),
                        rs.getString("tenVoucher"),
                        phanTramGiam,
                        rs.getString("dieuKien"));
            }
        }
    }

    public boolean laKhachHangThanhVien(String maKhachHang) throws SQLException {
        String sql = "SELECT 1 FROM TheThanhVien WHERE maKhachHang = ?";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<ThanhToan.PhuongThuc> layPhuongThucThanhToan()
            throws SQLException {
        String sql = "SELECT maPhuongThuc, tenPhuongThuc "
                + "FROM PhuongThucThanhToan "
                + "WHERE trangThai = 1 AND maPhuongThuc <> 'PT003' "
                + "ORDER BY maPhuongThuc";
        List<ThanhToan.PhuongThuc> phuongThucs = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                phuongThucs.add(new ThanhToan.PhuongThuc(
                        rs.getString("maPhuongThuc"),
                        rs.getString("tenPhuongThuc")));
            }
        }
        return phuongThucs;
    }

    public String taoMaHoaDon(String maKhachHang, LocalDateTime ngayLap)
            throws SQLException {
        if (maKhachHang == null || ngayLap == null) {
            throw new IllegalArgumentException("Thiếu mã khách hàng hoặc ngày lập hóa đơn.");
        }

        String chuSoMaKhachHang = maKhachHang.replaceAll("\\D", "");
        if (chuSoMaKhachHang.length() < 3) {
            throw new IllegalArgumentException(
                    "Mã khách hàng phải có ít nhất 3 chữ số để tạo mã hóa đơn.");
        }

        String sql = "UPDATE BoDemHoaDon "
                + "SET giaTri = CASE WHEN giaTri >= 9999 THEN 1 ELSE giaTri + 1 END "
                + "OUTPUT INSERTED.giaTri "
                + "WHERE id = 1";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException(
                        "Không cấp được số thứ tự hóa đơn. Hãy kiểm tra bộ đếm hóa đơn.");
            }

            int thuTuMua = rs.getInt(1);
            return "HD" + chuSoMaKhachHang.substring(0, 3)
                    + ngayLap.format(DateTimeFormatter.ofPattern("ddMMyy", Locale.ROOT))
                    + String.format(Locale.ROOT, "%04d", thuTuMua);
        }
    }

    public String taoHoaDon(
            String maHoaDon,
            String maNhanVien,
            String maKhachHang,
            String maVoucher,
            String maPhuongThuc,
            LocalDateTime ngayLap,
            BigDecimal phanTramGiam,
            List<ThanhToan> chiTiet,
            List<String> maPhieuDichVus) throws SQLException {
        if (chiTiet == null || chiTiet.isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn chưa có mặt hàng.");
        }
        if (maPhieuDichVus == null) {
            throw new IllegalArgumentException("Danh sách phiếu dịch vụ không hợp lệ.");
        }

        if (maHoaDon == null || !maHoaDon.matches("HD\\d{13}")) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        }
        String insertHoaDon = "INSERT INTO HoaDon "
                + "(maHoaDon, ngayLap, maPhuongThuc, maKhachHang, maNhanVien, maVoucher) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String insertChiTiet = "INSERT INTO ChiTietHoaDon "
                + "(maHoaDon, maSanPham, maDichVu, soLuong, donGia, phanTramGiam) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String completeServiceTicket = "UPDATE PhieuDichVu SET trangThai = 1 "
                + "WHERE maPhieuDichVu = ? AND maKhachHang = ? AND trangThai = 0";

        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Kiem tra ton ngay trong giao dich va khoa dong san pham den khi hoa don duoc ghi.
                java.util.Map<String, Integer> soLuongCan = new java.util.LinkedHashMap<>();
                for (ThanhToan item : chiTiet) {
                    if (item.getMaSanPham() != null) {
                        soLuongCan.merge(item.getMaSanPham(), item.getSoLuong(), Integer::sum);
                    }
                }
                String sqlTon = "SELECT " + SanPhamDAO.BIEU_THUC_TON + " AS tonKho FROM SanPham sp WITH (UPDLOCK, HOLDLOCK) WHERE sp.maSanPham=?";
                try (PreparedStatement psTon = con.prepareStatement(sqlTon)) {
                    for (var entry : soLuongCan.entrySet()) {
                        psTon.setString(1, entry.getKey());
                        try (ResultSet rs = psTon.executeQuery()) {
                            if (!rs.next() || rs.getInt("tonKho") < entry.getValue()) {
                                throw new SQLException("Sản phẩm " + entry.getKey()
                                        + " không đủ tồn kho. Vui lòng tải lại giỏ hàng.");
                            }
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(insertHoaDon)) {
                    ps.setString(1, maHoaDon);
                    ps.setTimestamp(2, Timestamp.valueOf(ngayLap));
                    ps.setString(3, maPhuongThuc);
                    ps.setString(4, maKhachHang);
                    ps.setString(5, maNhanVien);
                    if (maVoucher == null) {
                        ps.setNull(6, Types.VARCHAR);
                    } else {
                        ps.setString(6, maVoucher);
                    }
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(insertChiTiet)) {
                    for (ThanhToan item : chiTiet) {
                        ps.setString(1, maHoaDon);
                        ps.setString(2, item.getMaSanPham());
                        ps.setString(3, item.getMaDichVu());
                        ps.setInt(4, item.getSoLuong());
                        ps.setBigDecimal(5, item.getDonGia());
                        ps.setBigDecimal(6, phanTramGiam);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                try (PreparedStatement ps = con.prepareStatement(completeServiceTicket)) {
                    for (String maPhieuDichVu : maPhieuDichVus) {
                        ps.setString(1, maPhieuDichVu);
                        ps.setString(2, maKhachHang);
                        if (ps.executeUpdate() != 1) {
                            throw new SQLException("Phiếu dịch vụ " + maPhieuDichVu
                                    + " không còn ở trạng thái đang xử lý cho khách hàng này.");
                        }
                    }
                }
                con.commit();
                return maHoaDon;
            } catch (SQLException | RuntimeException e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }
}
