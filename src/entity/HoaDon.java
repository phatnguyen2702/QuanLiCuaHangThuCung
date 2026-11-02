package entity;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import dao.HoaDonDAO;

// Lớp dữ liệu biểu diễn các thuộc tính của hóa đơn, bao gồm cả thông tin hiển thị khi tra cứu.
public class HoaDon {

    private final String maHoaDon;
    private final LocalDateTime ngayLap;
    private final String maPhuongThuc;
    private final String maKhachHang;
    private final String tenKhachHang;
    private final String soDienThoai;
    private final String tenPhuongThuc;
    public HoaDon(
            String maHoaDon,
            LocalDateTime ngayLap,
            String maPhuongThuc,
            String maKhachHang) {
        this(maHoaDon, ngayLap, maPhuongThuc, maKhachHang, null, null, null);
    }

    public HoaDon(
            String maHoaDon,
            LocalDateTime ngayLap,
            String maPhuongThuc,
            String maKhachHang,
            String tenKhachHang,
            String soDienThoai,
            String tenPhuongThuc) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.maPhuongThuc = maPhuongThuc;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoai = soDienThoai;
        this.tenPhuongThuc = tenPhuongThuc;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public String getMaPhuongThuc() {
        return maPhuongThuc;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getTenPhuongThuc() {
        return tenPhuongThuc;
    }

    public BigDecimal getTongTien() throws SQLException {
        return tinhTongTien();
    }

    public List<ChiTietHoaDon> getChiTiet() throws SQLException {
        return chiTiet();
    }

    public BigDecimal tinhTongTien() throws SQLException {
        return new HoaDonDAO().layTongTienHoaDon(maHoaDon);
    }

    public List<ChiTietHoaDon> chiTiet() throws SQLException {
        return new HoaDonDAO().layDanhSachChiTietHoaDon(maHoaDon);
    }
}
