package model;

public class ChiTietPhieuNhap {
    private PhieuNhapKho maPhieuNhap;
    private SanPham maSanPham;
    private int soLuongNhap;
    private double donGiaNhap;
    private boolean trangThai;

    public ChiTietPhieuNhap(PhieuNhapKho phieu, SanPham sp, int soLuong, double donGia) {
        this.maPhieuNhap = phieu; this.maSanPham = sp; this.soLuongNhap = soLuong; this.donGiaNhap = donGia;
    }
    public double thanhTien() { return soLuongNhap * donGiaNhap; }
    public PhieuNhapKho getMaPhieuNhap() { return maPhieuNhap; }
    public SanPham getMaSanPham() { return maSanPham; }
    public int getSoLuongNhap() { return soLuongNhap; }
    public double getDonGiaNhap() { return donGiaNhap; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean v) { trangThai = v; }
    @Override public String toString() { return maSanPham.getTenSanPham() + " x" + soLuongNhap; }
}
