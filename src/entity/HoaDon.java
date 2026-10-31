package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Lớp dữ liệu biểu diễn hóa đơn, gồm thuộc tính và các phương thức truy cập.
public class HoaDon {

    // maHoaDon: Lưu mã hóa đơn.
    private final String maHoaDon;
    // ngayLap: Lưu ngày giờ lập hóa đơn dưới dạng dữ liệu thời gian.
    private final LocalDateTime ngayLap;
    // maKhachHang: Lưu mã khách hàng.
    private final String maKhachHang;
    // tenKhachHang: Lưu tên khách hàng.
    private final String tenKhachHang;
    // soDienThoai: Lưu số điện thoại.
    private final String soDienThoai;
    // tenPhuongThuc: Lưu tên phương thức thanh toán.
    private final String tenPhuongThuc;
    // tongTien: Giá trị tổng tiền dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
    private final BigDecimal tongTien;
    // chiTiet: Lưu các dòng chi tiết hóa đơn.
    private final List<ChiTietHoaDon> chiTiet;

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maHoaDon: mã hóa đơn.
    // Tham số ngayLap: ngày giờ lập hóa đơn.
    // Tham số maKhachHang: mã khách hàng.
    // Tham số tenKhachHang: tên khách hàng.
    // Tham số soDienThoai: số điện thoại.
    // Tham số tenPhuongThuc: tên phương thức thanh toán.
    // Tham số tongTien: tổng tiền.
    // Tham số chiTiet: chi tiết.
    public HoaDon(
            String maHoaDon,
            LocalDateTime ngayLap,
            String maKhachHang,
            String tenKhachHang,
            String soDienThoai,
            String tenPhuongThuc,
            BigDecimal tongTien,
            List<ChiTietHoaDon> chiTiet) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoai = soDienThoai;
        this.tenPhuongThuc = tenPhuongThuc;
        this.tongTien = tongTien == null ? BigDecimal.ZERO : tongTien;
        this.chiTiet = chiTiet == null
                ? Collections.<ChiTietHoaDon>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(chiTiet));
    }

    // Trả về mã hóa đơn của đối tượng.
    public String getMaHoaDon() {
        return maHoaDon;
    }

    // Trả về ngày giờ lập hóa đơn của đối tượng.
    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    // Trả về mã khách hàng của đối tượng.
    public String getMaKhachHang() {
        return maKhachHang;
    }

    // Trả về tên khách hàng của đối tượng.
    public String getTenKhachHang() {
        return tenKhachHang;
    }

    // Trả về số điện thoại của đối tượng.
    public String getSoDienThoai() {
        return soDienThoai;
    }

    // Trả về tên phương thức thanh toán của đối tượng.
    public String getTenPhuongThuc() {
        return tenPhuongThuc;
    }

    // Trả về tổng tiền của đối tượng.
    public BigDecimal getTongTien() {
        return tongTien;
    }

    // Trả về chi tiết của đối tượng.
    public List<ChiTietHoaDon> getChiTiet() {
        return chiTiet;
    }

}
