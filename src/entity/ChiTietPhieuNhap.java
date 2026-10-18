package entity;

public class ChiTietPhieuNhap {

    private String maPhieuNhap;
    private String maSanPham;
    private int soLuongNhap;
    private double donGiaNhap;
    private boolean trangThai;

    // Chi dung de hien thi
    private String tenSanPham;

    public ChiTietPhieuNhap() {
    }

    public ChiTietPhieuNhap(String maPhieuNhap, String maSanPham,
                            int soLuongNhap, double donGiaNhap,
                            boolean trangThai) {
        this.maPhieuNhap = maPhieuNhap;
        this.maSanPham = maSanPham;
        this.soLuongNhap = soLuongNhap;
        this.donGiaNhap = donGiaNhap;
        this.trangThai = trangThai;
    }

    public String getMaPhieuNhap() { return maPhieuNhap; }
    public void setMaPhieuNhap(String maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }

    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }

    public int getSoLuongNhap() { return soLuongNhap; }
    public void setSoLuongNhap(int soLuongNhap) { this.soLuongNhap = soLuongNhap; }

    public double getDonGiaNhap() { return donGiaNhap; }
    public void setDonGiaNhap(double donGiaNhap) { this.donGiaNhap = donGiaNhap; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public double getThanhTien() {
        return soLuongNhap * donGiaNhap;
    }
}
