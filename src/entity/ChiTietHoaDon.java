package entity;

// Lớp dữ liệu biểu diễn một dòng chi tiết hóa đơn.
public class ChiTietHoaDon {
    private final String maHang;
    private final String tenHang;
    private final String loaiHang;
    private final String tenThuCung;
    private final int soLuong;
    private final double donGia;
    private final double phanTramGiam;

    public ChiTietHoaDon(
            String maHang,
            String tenHang,
            String loaiHang,
            int soLuong,
            double donGia,
            double phanTramGiam) {
        this(maHang, tenHang, loaiHang, soLuong, donGia, phanTramGiam, null);
    }

    public ChiTietHoaDon(
            String maHang,
            String tenHang,
            String loaiHang,
            int soLuong,
            double donGia,
            double phanTramGiam,
            String tenThuCung) {
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.loaiHang = loaiHang;
        this.tenThuCung = tenThuCung;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.phanTramGiam = phanTramGiam;
    }

    public String getMaHang() {
        return maHang;
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
        return soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public double getPhanTramGiam() {
        return phanTramGiam;
    }

    public double getThanhTien() {
        return donGia * soLuong * (1 - phanTramGiam / 100);
    }
}
