package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Dữ liệu hóa đơn đã tổng hợp để hiển thị trên màn tra cứu lịch sử.
public class HoaDonTraCuu {

    private final HoaDon hoaDon;
    private final String tenKhachHang;
    private final String soDienThoai;
    private final String tenPhuongThuc;
    private final BigDecimal tongTien;
    private final List<ChiTietHoaDonTraCuu> chiTiet;

    public HoaDonTraCuu(
            HoaDon hoaDon,
            String tenKhachHang,
            String soDienThoai,
            String tenPhuongThuc,
            BigDecimal tongTien,
            List<ChiTietHoaDonTraCuu> chiTiet) {
        this.hoaDon = hoaDon;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoai = soDienThoai;
        this.tenPhuongThuc = tenPhuongThuc;
        this.tongTien = tongTien == null ? BigDecimal.ZERO : tongTien;
        this.chiTiet = chiTiet == null
                ? Collections.<ChiTietHoaDonTraCuu>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(chiTiet));
    }

    public String getMaHoaDon() {
        return hoaDon.getMaHoaDon();
    }

    public LocalDateTime getNgayLap() {
        return hoaDon.getNgayLap();
    }

    public String getMaKhachHang() {
        return hoaDon.getMaKhachHang();
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

    public List<ChiTietHoaDonTraCuu> getChiTiet() {
        return chiTiet;
    }
}
