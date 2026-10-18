package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ConnectDB.connectDB;
import entity.ThanhToan;

public class ThanhToanDao {

    public List<ThanhToan> timMatHang(String tuKhoa) throws SQLException {
        return truyTimMatHang(tuKhoa, true);
    }

    public List<ThanhToan> goiYMatHangTheoMa(String ma) throws SQLException {
        return truyTimMatHang(ma, false);
    }

    private List<ThanhToan> truyTimMatHang(String tuKhoa, boolean timTheoTen)
            throws SQLException {
        String dieuKienSanPham = timTheoTen
                ? "maSanPham = ? OR tenSanPham LIKE ?"
                : "maSanPham LIKE ?";
        String dieuKienDichVu = timTheoTen
                ? "maDichVu = ? OR tenDichVu LIKE ?"
                : "maDichVu LIKE ?";
        String sql = "SELECT TOP 30 maSanPham AS maHang, tenSanPham AS tenHang, "
                + "giaBan AS donGia, CAST(0 AS BIT) AS laDichVu "
                + "FROM SanPham WHERE " + dieuKienSanPham
                + " UNION ALL "
                + "SELECT TOP 30 maDichVu, tenDichVu, giaDichVu, CAST(1 AS BIT) "
                + "FROM DichVu WHERE " + dieuKienDichVu;
        List<ThanhToan> matHangs = new ArrayList<>();

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            String keyword = tuKhoa.trim();
            if (timTheoTen) {
                ps.setString(1, keyword);
                ps.setString(2, "%" + keyword + "%");
                ps.setString(3, keyword);
                ps.setString(4, "%" + keyword + "%");
            } else {
                ps.setString(1, keyword + "%");
                ps.setString(2, keyword + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal donGia = rs.getBigDecimal("donGia");
                    if (donGia == null || donGia.signum() < 0) {
                        throw new SQLException("Mặt hàng " + rs.getString("maHang")
                                + " có đơn giá không hợp lệ.");
                    }
                    String maHang = rs.getString("maHang");
                    String tenHang = rs.getString("tenHang");
                    if (rs.getBoolean("laDichVu")) {
                        matHangs.add(ThanhToan.dichVu(maHang, tenHang, donGia));
                    } else {
                        matHangs.add(ThanhToan.sanPham(maHang, tenHang, donGia));
                    }
                }
            }
        }
        return matHangs;
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

    public List<String> goiYKhachHangTheoMa(String ma) throws SQLException {
        String sql = "SELECT TOP 30 maKhachHang FROM KhachHang "
                + "WHERE maKhachHang LIKE ? ORDER BY maKhachHang";
        List<String> maKhachHangs = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ma.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    maKhachHangs.add(rs.getString("maKhachHang"));
                }
            }
        }
        return maKhachHangs;
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
            List<ThanhToan> chiTiet) throws SQLException {
        if (chiTiet == null || chiTiet.isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn chưa có mặt hàng.");
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

        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
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
