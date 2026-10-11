package entity;

import java.time.LocalDateTime;

public class PhieuNhapKho {

    private String maPhieuNhap;
    private LocalDateTime ngayNhap;
    private boolean trangThai;      // true = da hoan tat, false = luu nhap
    private String maNhanVien;
    private String maNhaCungCap;

    // Chi dung de hien thi (JOIN / tinh toan, khong phai cot cua bang)
    private String tenNhaCungCap;
    private int soMatHang;
    private double tongTien;

    public PhieuNhapKho() {
    }

    public PhieuNhapKho(String maPhieuNhap, LocalDateTime ngayNhap,
                        boolean trangThai, String maNhanVien,
                        String maNhaCungCap) {
        this.maPhieuNhap = maPhieuNhap;
        this.ngayNhap = ngayNhap;
        this.trangThai = trangThai;
        this.maNhanVien = maNhanVien;
        this.maNhaCungCap = maNhaCungCap;
    }

    public String getMaPhieuNhap() { return maPhieuNhap; }
    public void setMaPhieuNhap(String maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }

    public LocalDateTime getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(LocalDateTime ngayNhap) { this.ngayNhap = ngayNhap; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getMaNhaCungCap() { return maNhaCungCap; }
    public void setMaNhaCungCap(String maNhaCungCap) { this.maNhaCungCap = maNhaCungCap; }

    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

    public int getSoMatHang() { return soMatHang; }
    public void setSoMatHang(int soMatHang) { this.soMatHang = soMatHang; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
}
