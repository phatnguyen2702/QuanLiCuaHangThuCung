package entity;

import java.time.LocalDateTime;

// Lớp dữ liệu biểu diễn các thuộc tính chính của hóa đơn.
public class HoaDon {

    private final String maHoaDon;
    private final LocalDateTime ngayLap;
    private final String maPhuongThuc;
    private final String maKhachHang;

    public HoaDon(
            String maHoaDon,
            LocalDateTime ngayLap,
            String maPhuongThuc,
            String maKhachHang) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.maPhuongThuc = maPhuongThuc;
        this.maKhachHang = maKhachHang;
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
}
