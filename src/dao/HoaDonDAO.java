package dao;

import UI.PhanQuyen;

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

// Lớp truy cập cơ sở dữ liệu cho hóa đơn.
public class HoaDonDAO {

    // Nhóm dữ liệu hoặc chức năng thống kê dùng trong HoaDonDAO.
    public static class ThongKe {
        // tongHoaDon: Lưu tổng số hóa đơn.
        private final int tongHoaDon;
        // hoaDonHomNay: Lưu số hóa đơn hôm nay.
        private final int hoaDonHomNay;
        // khachHangHomNay: Lưu số khách hàng hôm nay.
        private final int khachHangHomNay;
        // doanhThuHomNay: Giá trị doanh thu hôm nay dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        private final BigDecimal doanhThuHomNay;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số tongHoaDon: tổng số hóa đơn.
        // Tham số hoaDonHomNay: số hóa đơn hôm nay.
        // Tham số khachHangHomNay: số khách hàng hôm nay.
        // Tham số doanhThuHomNay: doanh thu hôm nay.
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

        // Trả về tổng số hóa đơn của đối tượng.
        public int getTongHoaDon() {
            return tongHoaDon;
        }

        // Trả về số hóa đơn hôm nay của đối tượng.
        public int getHoaDonHomNay() {
            return hoaDonHomNay;
        }

        // Trả về số khách hàng hôm nay của đối tượng.
        public int getKhachHangHomNay() {
            return khachHangHomNay;
        }

        // Trả về doanh thu hôm nay của đối tượng.
        public BigDecimal getDoanhThuHomNay() {
            return doanhThuHomNay;
        }
    }

    // Lấy thống kê.
    // Kết quả: đối tượng/giá trị kiểu ThongKe.
    public ThongKe layThongKe() throws SQLException {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.THONG_KE_DOANH_THU);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
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
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
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

    // Tìm hóa đơn.
    // Tham số tuKhoa: từ khóa tìm kiếm.
    // Kết quả: đối tượng/giá trị kiểu List<HoaDon>.
    public List<HoaDon> timHoaDon(String tuKhoa) throws SQLException {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.TRA_CUU_HOA_DON);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT hd.maHoaDon, hd.ngayLap, kh.maKhachHang, kh.hoTen, "
                + "kh.soDienThoai, pt.tenPhuongThuc, "
                + "COALESCE(SUM(ct.soLuong * ct.donGia * "
                + " (1 - COALESCE(ct.phanTramGiam, 0) / 100.0)), 0) AS tongTien "
                + "FROM HoaDon hd "
                + "JOIN KhachHang kh ON kh.maKhachHang = hd.maKhachHang "
                + "JOIN PhuongThucThanhToan pt ON pt.maPhuongThuc = hd.maPhuongThuc "
                + "LEFT JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon "
                + "WHERE (? = '' OR hd.maHoaDon LIKE ? OR kh.hoTen LIKE ? "
                + "OR kh.soDienThoai LIKE ?) "
                + "GROUP BY hd.maHoaDon, hd.ngayLap, kh.maKhachHang, kh.hoTen, "
                + "kh.soDienThoai, pt.tenPhuongThuc "
                + "ORDER BY hd.ngayLap DESC, hd.maHoaDon DESC";
        // keyword: Lưu từ khóa tìm kiếm.
        String keyword = tuKhoa == null ? "" : tuKhoa.trim();
        // hoaDons: Lưu các hóa đơn; phần tử có kiểu HoaDon.
        List<HoaDon> hoaDons = new ArrayList<>();
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, "%" + keyword + "%");
            ps.setString(4, "%" + keyword + "%");
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
                while (rs.next()) {
                    // ngayLap: Lưu ngày giờ lập hóa đơn dưới dạng dữ liệu thời gian.
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

    // Lấy chi tiết hóa đơn.
    // Tham số maHoaDon: mã hóa đơn.
    // Kết quả: đối tượng/giá trị kiểu HoaDon.
    public HoaDon layChiTietHoaDon(String maHoaDon) throws SQLException {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.TRA_CUU_HOA_DON);
        // headerSql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
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
        // detailSql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String detailSql = "SELECT COALESCE(sp.maSanPham, dv.maDichVu, tc.maThuCung) AS maHang, "
                + "COALESCE(sp.tenSanPham, dv.tenDichVu, tc.tenThuCung, N'Không xác định') AS tenHang, "
                + "CASE WHEN sp.maSanPham IS NOT NULL THEN N'Sản phẩm' "
                + "WHEN dv.maDichVu IS NOT NULL THEN N'Dịch vụ' ELSE N'Thú cưng' END AS loaiHang, "
                + "CASE WHEN dv.maDichVu IS NOT NULL THEN tc.tenThuCung END "
                + "AS tenThuCungDichVu, "
                + "ct.soLuong, ct.donGia, ct.phanTramGiam "
                + "FROM ChiTietHoaDon ct "
                + "LEFT JOIN SanPham sp ON sp.maSanPham = ct.maSanPham "
                + "LEFT JOIN DichVu dv ON dv.maDichVu = ct.maDichVu "
                + "LEFT JOIN ThuCung tc ON tc.maThuCung = ct.maThuCung "
                + "WHERE ct.maHoaDon = ? ORDER BY maHang";

        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection()) {
            // maKhachHang: Lưu mã khách hàng.
            String maKhachHang;
            String tenKhachHang;
            // soDienThoai: Lưu số điện thoại.
            String soDienThoai;
            // tenPhuongThuc: Lưu tên phương thức thanh toán.
            String tenPhuongThuc;
            // ngayLap: Lưu ngày giờ lập hóa đơn dưới dạng dữ liệu thời gian.
            Timestamp ngayLap;
            // tongTien: Giá trị tổng tiền dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
            BigDecimal tongTien;
            try (PreparedStatement ps = con.prepareStatement(headerSql)) {
                ps.setString(1, maHoaDon);
                // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
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

            // chiTiet: Lưu chi tiết; phần tử có kiểu HoaDon.ChiTiet.
            List<HoaDon.ChiTiet> chiTiet = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(detailSql)) {
                ps.setString(1, maHoaDon);
                // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
                try (ResultSet rs = ps.executeQuery()) {
                    // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
                    while (rs.next()) {
                        chiTiet.add(new HoaDon.ChiTiet(
                                rs.getString("maHang"),
                                rs.getString("tenHang"),
                                rs.getString("loaiHang"),
                                rs.getInt("soLuong"),
                                rs.getBigDecimal("donGia"),
                                rs.getBigDecimal("phanTramGiam"),
                                rs.getString("tenThuCungDichVu")));
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
