package entity;

// Dữ liệu chi tiết hóa đơn đã bổ sung thông tin để hiển thị trên màn tra cứu.
public class ChiTietHoaDonTraCuu {
    private final ChiTietHoaDon chiTiet;
    private final String tenHang;
    private final String loaiHang;
    private final String tenThuCung;

    public ChiTietHoaDonTraCuu(
            ChiTietHoaDon chiTiet,
            String tenHang,
            String loaiHang,
            String tenThuCung) {
        this.chiTiet = chiTiet;
        this.tenHang = tenHang;
        this.loaiHang = loaiHang;
        this.tenThuCung = tenThuCung;
    }

    public String getMaHang() {
        if (chiTiet.getMaSanPham() != null) {
            return chiTiet.getMaSanPham();
        }
        if (chiTiet.getMaDichVu() != null) {
            return chiTiet.getMaDichVu();
        }
        return chiTiet.getMaThuCung();
    }

    public String getTenHang() {
        return tenHang;
    }

    public String getLoaiHang() {
        return loaiHang;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public int getSoLuong() {
        return chiTiet.getSoLuong();
    }

    public double getDonGia() {
        return chiTiet.getDonGia();
    }

    public double getThanhTien() {
        return chiTiet.getThanhTien();
    }
}
