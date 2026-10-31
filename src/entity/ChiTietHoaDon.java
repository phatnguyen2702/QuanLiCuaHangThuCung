package entity;

// Lớp dữ liệu biểu diễn các thuộc tính lưu trữ của một dòng chi tiết hóa đơn.
public class ChiTietHoaDon {
    private final String maHoaDon;
    private final String maThuCung;
    private final String maDichVu;
    private final String maSanPham;
    private final int soLuong;
    private final double donGia;
    private final double phanTramGiam;

    public ChiTietHoaDon(
            String maHoaDon,
            String maThuCung,
            String maDichVu,
            String maSanPham,
            int soLuong,
            double donGia,
            double phanTramGiam) {
        this.maHoaDon = maHoaDon;
        this.maThuCung = maThuCung;
        this.maDichVu = maDichVu;
        this.maSanPham = maSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.phanTramGiam = phanTramGiam;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public String getMaThuCung() {
        return maThuCung;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public String getMaSanPham() {
        return maSanPham;
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
