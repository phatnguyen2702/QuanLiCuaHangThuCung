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
    private final String tenHang;
    private final String loaiHang;
    private final String tenThuCung;

    public ChiTietHoaDon(
            String maHoaDon,
            String maThuCung,
            String maDichVu,
            String maSanPham,
            int soLuong,
            double donGia,
            double phanTramGiam) {
        this(maHoaDon, maThuCung, maDichVu, maSanPham, soLuong, donGia, phanTramGiam, null, null, null);
    }

    public ChiTietHoaDon(
            String maHoaDon,
            String maThuCung,
            String maDichVu,
            String maSanPham,
            int soLuong,
            double donGia,
            double phanTramGiam,
            String tenHang,
            String loaiHang,
            String tenThuCung) {
        this.maHoaDon = maHoaDon;
        this.maThuCung = maThuCung;
        this.maDichVu = maDichVu;
        this.maSanPham = maSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.phanTramGiam = phanTramGiam;
        this.tenHang = tenHang;
        this.loaiHang = loaiHang;
        this.tenThuCung = tenThuCung;
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

    public String getMaHang() {
        if (maSanPham != null) {
            return maSanPham;
        }
        if (maDichVu != null) {
            return maDichVu;
        }
        return maThuCung;
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

    public String getTenHang() {
        return tenHang;
    }

    public String getLoaiHang() {
        return loaiHang;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public double getThanhTien() {
        return donGia * soLuong * (1 - phanTramGiam / 100);
    }
}
