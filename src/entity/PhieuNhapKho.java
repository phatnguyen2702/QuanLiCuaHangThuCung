package entity;
import java.time.LocalDateTime;
public class PhieuNhapKho {
    private String maPhieuNhap;
    private LocalDateTime ngayNhap;
    private boolean trangThai;
    private NhanVien maNhanVien;
    private NhaCungCap maNhaCungCap;
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
        setMaNhanVien(maNhanVien);
        setMaNhaCungCap(maNhaCungCap);
    }
    public String getMaPhieuNhap() { return maPhieuNhap; }
    public void setMaPhieuNhap(String maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }
    public LocalDateTime getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(LocalDateTime ngayNhap) { this.ngayNhap = ngayNhap; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    public String getMaNhanVien() { return maNhanVien == null ? null : maNhanVien.getMaNhanVien(); }
    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien == null ? null : new NhanVien();
        if (this.maNhanVien != null) this.maNhanVien.setMaNhanVien(maNhanVien);
    }
    public NhanVien getNhanVien() { return maNhanVien; }
    public void setNhanVien(NhanVien nhanVien) { maNhanVien = nhanVien; }
    public String getMaNhaCungCap() { return maNhaCungCap == null ? null : maNhaCungCap.getMaNhaCungCap(); }
    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap == null ? null : new NhaCungCap();
        if (this.maNhaCungCap != null) this.maNhaCungCap.setMaNhaCungCap(maNhaCungCap);
    }
    public NhaCungCap getNhaCungCap() { return maNhaCungCap; }
    public void setNhaCungCap(NhaCungCap nhaCungCap) {
        maNhaCungCap = nhaCungCap;
        if (nhaCungCap != null) tenNhaCungCap = nhaCungCap.getTenNhaCungCap();
    }
    public String getTenNhaCungCap() {
        return tenNhaCungCap != null ? tenNhaCungCap
                : maNhaCungCap == null ? null : maNhaCungCap.getTenNhaCungCap();
    }
    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
        if (maNhaCungCap != null) maNhaCungCap.setTenNhaCungCap(tenNhaCungCap);
    }
    public int getSoMatHang() { return soMatHang; }
    public void setSoMatHang(int soMatHang) { this.soMatHang = soMatHang; }
    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
}
