package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Lớp dữ liệu biểu diễn các thuộc tính của hóa đơn, bao gồm cả thông tin hiển thị khi tra cứu.
public class HoaDon {

    private final String maHoaDon;
    private final LocalDateTime ngayLap;
    private final String maPhuongThuc;
    private final String maKhachHang;
    private final String tenKhachHang;
    private final String soDienThoai;
    private final String tenPhuongThuc;
    private final BigDecimal tongTien;
    private final List<ChiTietHoaDon> chiTiet;

    public HoaDon(
            String maHoaDon,
            LocalDateTime ngayLap,
            String maPhuongThuc,
            String maKhachHang) {
        this(maHoaDon, ngayLap, maPhuongThuc, maKhachHang, null, null, null, BigDecimal.ZERO, null);
    }

    public HoaDon(
            String maHoaDon,
            LocalDateTime ngayLap,
            String maPhuongThuc,
            String maKhachHang,
            String tenKhachHang,
            String soDienThoai,
            String tenPhuongThuc,
            BigDecimal tongTien,
            List<ChiTietHoaDon> chiTiet) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.maPhuongThuc = maPhuongThuc;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoai = soDienThoai;
        this.tenPhuongThuc = tenPhuongThuc;
        this.tongTien = tongTien == null ? BigDecimal.ZERO : tongTien;
        this.chiTiet = chiTiet == null
                ? Collections.<ChiTietHoaDon>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(chiTiet));
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

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public List<ChiTietHoaDon> getChiTiet() {
        return chiTiet;
    }
}
