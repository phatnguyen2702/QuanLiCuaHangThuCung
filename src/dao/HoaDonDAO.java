package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ConnectDB.connectDB;
import entity.HoaDon;

public class HoaDonDAO {

    public static class ThongKe {
        private final int tongHoaDon;
        private final int hoaDonHomNay;
        private final int khachHangHomNay;
        private final BigDecimal doanhThuHomNay;

        private ThongKe(
                int tongHoaDon,
                int hoaDonHomNay,
                int khachHangHomNay,
                BigDecimal doanhThuHomNay) {
            this.tongHoaDon = tongHoaDon;
            this.hoaDonHomNay = hoaDonHomNay;
            this.khachHangHomNay = khachHangHomNay;
            this.doanhThuHomNay = doanhThuHomNay == null ? BigDecimal.ZERO : doanhThuHomNay;
        }

        public int getTongHoaDon() {
            return tongHoaDon;
        }

        public int getHoaDonHomNay() {
            return hoaDonHomNay;
        }

        public int getKhachHangHomNay() {
            return khachHangHomNay;
        }

        public BigDecimal getDoanhThuHomNay() {
            return doanhThuHomNay;
        }
    }

    public ThongKe layThongKe() throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM HoaDon) AS tongHoaDon, "
                + "(SELECT COUNT(*) FROM HoaDon "
                + " WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)) AS hoaDonHomNay, "
                + "(SELECT COUNT(DISTINCT maKhachHang) FROM HoaDon "
                + " WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)) AS khachHangHomNay, "
                + "(SELECT COALESCE(SUM(ct.soLuong * ct.donGia * "
                + " (1 - COALESCE(ct.phanTramGiam, 0) / 100.0)), 0) "
                + " FROM HoaDon hd JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon "
                + " WHERE CAST(hd.ngayLap AS DATE) = CAST(GETDATE() AS DATE)) AS doanhThuHomNay";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("Không đọc được số liệu hóa đơn.");
            }
            return new ThongKe(
                    rs.getInt("tongHoaDon"),
                    rs.getInt("hoaDonHomNay"),
                    rs.getInt("khachHangHomNay"),
                    rs.getBigDecimal("doanhThuHomNay"));
        }
    }

    public List<HoaDon> timHoaDon(String tuKhoa) throws SQLException {
        String sql = "SELECT hd.maHoaDon, hd.ngayLap, kh.maKhachHang, kh.hoTen, "
                + "kh.soDienThoai, pt.tenPhuongThuc, "
                + "COALESCE(SUM(ct.soLuong * ct.donGia * "
                + " (1 - COALESCE(ct.phanTramGiam, 0) / 100.0)), 0) AS tongTien "
                + "FROM HoaDon hd "
                + "JOIN KhachHang kh ON kh.maKhachHang = hd.maKhachHang "
                + "JOIN PhuongThucThanhToan pt ON pt.maPhuongThuc = hd.maPhuongThuc "
                + "LEFT JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon "
                + "WHERE (? = '' OR hd.maHoaDon LIKE ? OR kh.hoTen LIKE ?) "
                + "GROUP BY hd.maHoaDon, hd.ngayLap, kh.maKhachHang, kh.hoTen, "
                + "kh.soDienThoai, pt.tenPhuongThuc "
                + "ORDER BY hd.ngayLap DESC, hd.maHoaDon DESC";
        String keyword = tuKhoa == null ? "" : tuKhoa.trim();
        List<HoaDon> hoaDons = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ngayLap = rs.getTimestamp("ngayLap");
                    hoaDons.add(new HoaDon(
                            rs.getString("maHoaDon"),
                            ngayLap == null ? null : ngayLap.toLocalDateTime(),
                            rs.getString("maKhachHang"),
                            rs.getString("hoTen"),
                            rs.getString("soDienThoai"),
                            rs.getString("tenPhuongThuc"),
                            rs.getBigDecimal("tongTien"),
                            Collections.<HoaDon.ChiTiet>emptyList()));
                }
            }
        }
        return hoaDons;
    }

    public List<String> goiYTimHoaDon(String tuKhoa) throws SQLException {
        String sql = "SELECT TOP 30 giaTri FROM ("
                + "SELECT hd.maHoaDon AS giaTri FROM HoaDon hd "
                + "WHERE hd.maHoaDon LIKE ? "
                + "UNION "
                + "SELECT kh.hoTen AS giaTri FROM KhachHang kh "
                + "WHERE kh.hoTen LIKE ? AND EXISTS ("
                + "SELECT 1 FROM HoaDon hd WHERE hd.maKhachHang = kh.maKhachHang) "
                + ") AS goiY ORDER BY giaTri";
        String keyword = "%" + tuKhoa.trim() + "%";
        List<String> goiY = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    goiY.add(rs.getString("giaTri"));
                }
            }
        }
        return goiY;
    }

    public HoaDon layChiTietHoaDon(String maHoaDon) throws SQLException {
        String headerSql = "SELECT hd.maHoaDon, hd.ngayLap, kh.maKhachHang, kh.hoTen, "
                + "kh.soDienThoai, pt.tenPhuongThuc, "
                + "COALESCE(SUM(ct.soLuong * ct.donGia * "
                + " (1 - COALESCE(ct.phanTramGiam, 0) / 100.0)), 0) AS tongTien "
                + "FROM HoaDon hd "
                + "JOIN KhachHang kh ON kh.maKhachHang = hd.maKhachHang "
                + "JOIN PhuongThucThanhToan pt ON pt.maPhuongThuc = hd.maPhuongThuc "
                + "LEFT JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon "
                + "WHERE hd.maHoaDon = ? "
                + "GROUP BY hd.maHoaDon, hd.ngayLap, kh.maKhachHang, kh.hoTen, "
                + "kh.soDienThoai, pt.tenPhuongThuc";
        String detailSql = "SELECT COALESCE(sp.maSanPham, dv.maDichVu, tc.maThuCung) AS maHang, "
                + "COALESCE(sp.tenSanPham, dv.tenDichVu, tc.tenThuCung, N'Không xác định') AS tenHang, "
                + "CASE WHEN sp.maSanPham IS NOT NULL THEN N'Sản phẩm' "
                + "WHEN dv.maDichVu IS NOT NULL THEN N'Dịch vụ' ELSE N'Thú cưng' END AS loaiHang, "
                + "ct.soLuong, ct.donGia, ct.phanTramGiam "
                + "FROM ChiTietHoaDon ct "
                + "LEFT JOIN SanPham sp ON sp.maSanPham = ct.maSanPham "
                + "LEFT JOIN DichVu dv ON dv.maDichVu = ct.maDichVu "
                + "LEFT JOIN ThuCung tc ON tc.maThuCung = ct.maThuCung "
                + "WHERE ct.maHoaDon = ? ORDER BY maHang";

        try (Connection con = connectDB.getConnection()) {
            String maKhachHang;
            String tenKhachHang;
            String soDienThoai;
            String tenPhuongThuc;
            Timestamp ngayLap;
            BigDecimal tongTien;
            try (PreparedStatement ps = con.prepareStatement(headerSql)) {
                ps.setString(1, maHoaDon);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    maKhachHang = rs.getString("maKhachHang");
                    tenKhachHang = rs.getString("hoTen");
                    soDienThoai = rs.getString("soDienThoai");
                    tenPhuongThuc = rs.getString("tenPhuongThuc");
                    ngayLap = rs.getTimestamp("ngayLap");
                    tongTien = rs.getBigDecimal("tongTien");
                }
            }

            List<HoaDon.ChiTiet> chiTiet = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(detailSql)) {
                ps.setString(1, maHoaDon);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        chiTiet.add(new HoaDon.ChiTiet(
                                rs.getString("maHang"),
                                rs.getString("tenHang"),
                                rs.getString("loaiHang"),
                                rs.getInt("soLuong"),
                                rs.getBigDecimal("donGia"),
                                rs.getBigDecimal("phanTramGiam")));
                    }
                }
            }
            return new HoaDon(
                    maHoaDon,
                    ngayLap == null ? null : ngayLap.toLocalDateTime(),
                    maKhachHang,
                    tenKhachHang,
                    soDienThoai,
                    tenPhuongThuc,
                    tongTien,
                    chiTiet);
        }
    }
}
