package dao;

import ConnectDB.connectDB;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HoaDonDAO {

    public static class ThongKe {
        private final int tongHoaDon;
        private final int hoaDonHomNay;
        private final int khachHangHomNay;
        private final double doanhThuHomNay;

        private ThongKe(
                int tongHoaDon,
                int hoaDonHomNay,
                int khachHangHomNay,
                double doanhThuHomNay) {
            this.tongHoaDon = tongHoaDon;
            this.hoaDonHomNay = hoaDonHomNay;
            this.khachHangHomNay = khachHangHomNay;
            this.doanhThuHomNay = doanhThuHomNay;
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

        public double getDoanhThuHomNay() {
            return doanhThuHomNay;
        }
    }

    public ThongKe layThongKe() throws SQLException {
        control.PhanQuyenControl.yeuCau(control.PhanQuyenControl.Quyen.THONG_KE_DOANH_THU);
        String sql = """
                SELECT (SELECT COUNT(*) FROM HoaDon) AS tongHoaDon,
                       (SELECT COUNT(*) FROM HoaDon WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)) AS hoaDonHomNay,
                       (SELECT COUNT(DISTINCT maKhachHang) FROM HoaDon WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)) AS khachHangHomNay,
                       (SELECT COALESCE(SUM(ct.soLuong * ct.donGia * (1 - COALESCE(ct.phanTramGiam, 0) / 100.0)), 0)
                        FROM HoaDon hd JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon
                        WHERE CAST(hd.ngayLap AS DATE) = CAST(GETDATE() AS DATE)) AS doanhThuHomNay
                """;
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
                    rs.getDouble("doanhThuHomNay"));
        }
    }

    public List<HoaDon> timHoaDon(String tuKhoa) throws SQLException {
        control.PhanQuyenControl.yeuCau(control.PhanQuyenControl.Quyen.TRA_CUU_HOA_DON);
        String sql = """
                SELECT hd.maHoaDon, hd.ngayLap, hd.maPhuongThuc, kh.maKhachHang, kh.hoTen, kh.soDienThoai, pt.tenPhuongThuc
                FROM HoaDon hd JOIN KhachHang kh ON kh.maKhachHang = hd.maKhachHang
                JOIN PhuongThucThanhToan pt ON pt.maPhuongThuc = hd.maPhuongThuc
                WHERE ? = '' OR hd.maHoaDon LIKE ? OR kh.hoTen LIKE ? OR kh.soDienThoai LIKE ?
                ORDER BY hd.ngayLap DESC, hd.maHoaDon DESC
                """;
        String keyword = tuKhoa == null ? "" : tuKhoa.trim();
        List<HoaDon> hoaDons = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, "%" + keyword + "%");
            ps.setString(4, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ngayLap = rs.getTimestamp("ngayLap");
                    String maHoaDon = rs.getString("maHoaDon");
                    String maKhachHang = rs.getString("maKhachHang");
                    hoaDons.add(new HoaDon(
                            maHoaDon,
                            ngayLap == null ? null : ngayLap.toLocalDateTime(),
                            rs.getString("maPhuongThuc"),
                            maKhachHang,
                            rs.getString("hoTen"),
                            rs.getString("soDienThoai"),
                            rs.getString("tenPhuongThuc")));
                }
            }
        }
        return hoaDons;
    }

    public double layTongTienHoaDon(String maHoaDon) throws SQLException {
        control.PhanQuyenControl.yeuCau(control.PhanQuyenControl.Quyen.TRA_CUU_HOA_DON);
        String sql = "SELECT COALESCE(SUM(ct.soLuong * ct.donGia * (1 - COALESCE(ct.phanTramGiam, 0) / 100.0)), 0) AS tongTien FROM ChiTietHoaDon ct WHERE ct.maHoaDon = ?";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không đọc được tổng tiền hóa đơn.");
                }
                return rs.getDouble("tongTien");
            }
        }
    }

    public Map<String, Double> layTongTienHoaDon(List<HoaDon> hoaDons) throws SQLException {
        control.PhanQuyenControl.yeuCau(control.PhanQuyenControl.Quyen.TRA_CUU_HOA_DON);
        Map<String, Double> tongTienTheoMa = new LinkedHashMap<>();
        for (HoaDon hoaDon : hoaDons) {
            tongTienTheoMa.put(hoaDon.getMaHoaDon(), 0.0);
        }
        if (tongTienTheoMa.isEmpty()) {
            return tongTienTheoMa;
        }

        List<String> maHoaDons = new ArrayList<>(tongTienTheoMa.keySet());
        try (Connection con = connectDB.getConnection()) {
            for (int batDau = 0; batDau < maHoaDons.size(); batDau += 1000) {
                List<String> nhomMa = maHoaDons.subList(batDau, Math.min(batDau + 1000, maHoaDons.size()));
                String placeholders = String.join(", ", Collections.nCopies(nhomMa.size(), "?"));
                String sql = """
                        SELECT ct.maHoaDon, COALESCE(SUM(ct.soLuong * ct.donGia * (1 - COALESCE(ct.phanTramGiam, 0) / 100.0)), 0) AS tongTien
                        FROM ChiTietHoaDon ct WHERE ct.maHoaDon IN (%s)
                        GROUP BY ct.maHoaDon
                        """.formatted(placeholders);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    for (int i = 0; i < nhomMa.size(); i++) {
                        ps.setString(i + 1, nhomMa.get(i));
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            tongTienTheoMa.put(
                                    rs.getString("maHoaDon"),
                                    rs.getDouble("tongTien"));
                        }
                    }
                }
            }
        }
        return tongTienTheoMa;
    }

    public List<ChiTietHoaDon> layDanhSachChiTietHoaDon(String maHoaDon) throws SQLException {
        control.PhanQuyenControl.yeuCau(control.PhanQuyenControl.Quyen.TRA_CUU_HOA_DON);
        String sql = """
                SELECT ct.maHoaDon, ct.maThuCung, ct.maDichVu, ct.maSanPham,
                       COALESCE(sp.tenSanPham, dv.tenDichVu, tc.tenThuCung, N'Không xác định') AS tenHang,
                       CASE WHEN sp.maSanPham IS NOT NULL THEN N'Sản phẩm'
                            WHEN dv.maDichVu IS NOT NULL THEN N'Dịch vụ'
                            ELSE N'Thú cưng' END AS loaiHang,
                       CASE WHEN dv.maDichVu IS NOT NULL THEN tc.tenThuCung END AS tenThuCungDichVu,
                       ct.soLuong, ct.donGia, ct.phanTramGiam
                FROM ChiTietHoaDon ct
                LEFT JOIN SanPham sp ON sp.maSanPham = ct.maSanPham
                LEFT JOIN DichVu dv ON dv.maDichVu = ct.maDichVu
                LEFT JOIN ThuCung tc ON tc.maThuCung = ct.maThuCung
                WHERE ct.maHoaDon = ?
                ORDER BY COALESCE(ct.maSanPham, ct.maDichVu, ct.maThuCung)
                """;
        List<ChiTietHoaDon> chiTiet = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chiTiet.add(new ChiTietHoaDon(
                            rs.getString("maHoaDon"),
                            rs.getString("maThuCung"),
                            rs.getString("maDichVu"),
                            rs.getString("maSanPham"),
                            rs.getInt("soLuong"),
                            rs.getDouble("donGia"),
                            rs.getDouble("phanTramGiam"),
                            rs.getString("tenHang"),
                            rs.getString("loaiHang"),
                            rs.getString("tenThuCungDichVu")));
                }
            }
        }
        return chiTiet;
    }

    public HoaDon layChiTietHoaDon(String maHoaDon) throws SQLException {
        control.PhanQuyenControl.yeuCau(control.PhanQuyenControl.Quyen.TRA_CUU_HOA_DON);
        String sql = """
                SELECT hd.maHoaDon, hd.ngayLap, hd.maPhuongThuc, kh.maKhachHang, kh.hoTen, kh.soDienThoai, pt.tenPhuongThuc
                FROM HoaDon hd JOIN KhachHang kh ON kh.maKhachHang = hd.maKhachHang
                JOIN PhuongThucThanhToan pt ON pt.maPhuongThuc = hd.maPhuongThuc
                WHERE hd.maHoaDon = ?
                """;
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Timestamp ngayLap = rs.getTimestamp("ngayLap");
                return new HoaDon(
                        rs.getString("maHoaDon"),
                        ngayLap == null ? null : ngayLap.toLocalDateTime(),
                        rs.getString("maPhuongThuc"),
                        rs.getString("maKhachHang"),
                        rs.getString("hoTen"),
                        rs.getString("soDienThoai"),
                        rs.getString("tenPhuongThuc"));
            }
        }
    }
}
