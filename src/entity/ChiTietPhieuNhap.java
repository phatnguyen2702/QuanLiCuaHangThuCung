package entity;
import java.time.LocalDate;
public class ChiTietPhieuNhap {
    private PhieuNhapKho maPhieuNhap;
    private SanPham maSanPham;
    private int soLuongNhap;
    private double donGiaNhap;
    private boolean trangThai;
    private LocalDate hanSuDung;
    private String tenSanPham;
    public ChiTietPhieuNhap() {
    }
    public ChiTietPhieuNhap(String maPhieuNhap, String maSanPham,
                            int soLuongNhap, double donGiaNhap,
                            boolean trangThai) {
        setMaPhieuNhap(maPhieuNhap);
        setMaSanPham(maSanPham);
        this.soLuongNhap = soLuongNhap;
        this.donGiaNhap = donGiaNhap;
        this.trangThai = trangThai;
    }
    public String getMaPhieuNhap() { return maPhieuNhap == null ? null : maPhieuNhap.getMaPhieuNhap(); }
    public void setMaPhieuNhap(String maPhieuNhap) {
        this.maPhieuNhap = maPhieuNhap == null ? null : new PhieuNhapKho();
        if (this.maPhieuNhap != null) this.maPhieuNhap.setMaPhieuNhap(maPhieuNhap);
    }
    public PhieuNhapKho getPhieuNhapKho() { return maPhieuNhap; }
    public void setPhieuNhapKho(PhieuNhapKho phieuNhapKho) { maPhieuNhap = phieuNhapKho; }
    public String getMaSanPham() { return maSanPham == null ? null : maSanPham.getMaSanPham(); }
    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham == null ? null : new SanPham();
        if (this.maSanPham != null) this.maSanPham.setMaSanPham(maSanPham);
    }
    public SanPham getSanPham() { return maSanPham; }
    public void setSanPham(SanPham sanPham) {
        maSanPham = sanPham;
        if (sanPham != null) tenSanPham = sanPham.getTenSanPham();
    }
    public int getSoLuongNhap() { return soLuongNhap; }
    public void setSoLuongNhap(int soLuongNhap) { this.soLuongNhap = soLuongNhap; }
    public double getDonGiaNhap() { return donGiaNhap; }
    public void setDonGiaNhap(double donGiaNhap) { this.donGiaNhap = donGiaNhap; }
    public LocalDate getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(LocalDate hanSuDung) { this.hanSuDung = hanSuDung; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    public String getTenSanPham() {
        return tenSanPham != null ? tenSanPham : maSanPham == null ? null : maSanPham.getTenSanPham();
    }
    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
        if (maSanPham != null) maSanPham.setTenSanPham(tenSanPham);
    }
    public double getThanhTien() {
        return soLuongNhap * donGiaNhap;
    }
}
