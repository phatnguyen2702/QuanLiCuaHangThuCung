package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PhieuNhapKho {
    private String maPhieuNhap;
    private LocalDateTime ngayNhap;
    private NhaCungCap maNhaCungCap;
    private NhanVien maNhanVien;
    private boolean trangThai; 
    private final List<ChiTietPhieuNhap> chiTiet = new ArrayList<>();

    public PhieuNhapKho(String ma, LocalDateTime ngay, NhaCungCap ncc, NhanVien nv, boolean trangThai) {
        this.maPhieuNhap = ma; this.ngayNhap = ngay; this.maNhaCungCap = ncc;
        this.maNhanVien = nv; this.trangThai = trangThai;
    }
    public double tinhTongTienNhap() {
        double t = 0;
        for (ChiTietPhieuNhap c : chiTiet) t += c.thanhTien();
        return t;
    }
    public String getMaPhieuNhap() { return maPhieuNhap; }
    public LocalDateTime getNgayNhap() { return ngayNhap; }
    public NhaCungCap getMaNhaCungCap() { return maNhaCungCap; }
    public void setMaNhaCungCap(NhaCungCap v) { maNhaCungCap = v; }
    public NhanVien getMaNhanVien() { return maNhanVien; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean v) { trangThai = v; }
    public List<ChiTietPhieuNhap> getChiTiet() { return chiTiet; }
}
