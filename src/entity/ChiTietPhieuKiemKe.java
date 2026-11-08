package entity;
public class ChiTietPhieuKiemKe {
    private PhieuKiemKe maPhieuKiemKe;
    private SanPham maSanPham;
    private PhieuNhapKho maPhieuNhap;
    private java.time.LocalDate ngayNhap;
    private int soLuongHeThong;
    private Integer soLuongThucTe;
    private boolean trangThai;
    private String tenSanPham;
    public ChiTietPhieuKiemKe() {
    }
    public ChiTietPhieuKiemKe(String maPhieuKiemKe, String maSanPham,
                              int soLuongHeThong, Integer soLuongThucTe,
                              boolean trangThai) {
        setMaPhieuKiemKe(maPhieuKiemKe);
        setMaSanPham(maSanPham);
        this.soLuongHeThong = soLuongHeThong;
        this.soLuongThucTe = soLuongThucTe;
        this.trangThai = trangThai;
    }
    public String getMaPhieuKiemKe() { return maPhieuKiemKe == null ? null : maPhieuKiemKe.getMaPhieuKiemKe(); }
    public void setMaPhieuKiemKe(String maPhieuKiemKe) {
        this.maPhieuKiemKe = maPhieuKiemKe == null ? null : new PhieuKiemKe();
        if (this.maPhieuKiemKe != null) this.maPhieuKiemKe.setMaPhieuKiemKe(maPhieuKiemKe);
    }
    public PhieuKiemKe getPhieuKiemKe() { return maPhieuKiemKe; }
    public void setPhieuKiemKe(PhieuKiemKe phieuKiemKe) { maPhieuKiemKe = phieuKiemKe; }
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
    public String getMaPhieuNhap() { return maPhieuNhap == null ? null : maPhieuNhap.getMaPhieuNhap(); }
    public void setMaPhieuNhap(String maPhieuNhap) {
        this.maPhieuNhap = maPhieuNhap == null ? null : new PhieuNhapKho();
        if (this.maPhieuNhap != null) this.maPhieuNhap.setMaPhieuNhap(maPhieuNhap);
    }
    public PhieuNhapKho getPhieuNhapKho() { return maPhieuNhap; }
    public void setPhieuNhapKho(PhieuNhapKho phieuNhapKho) { maPhieuNhap = phieuNhapKho; }
    public java.time.LocalDate getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(java.time.LocalDate ngayNhap) { this.ngayNhap = ngayNhap; }
    public int getSoLuongHeThong() { return soLuongHeThong; }
    public void setSoLuongHeThong(int soLuongHeThong) { this.soLuongHeThong = soLuongHeThong; }
    public Integer getSoLuongThucTe() { return soLuongThucTe; }
    public void setSoLuongThucTe(Integer soLuongThucTe) { this.soLuongThucTe = soLuongThucTe; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    public String getTenSanPham() {
        return tenSanPham != null ? tenSanPham : maSanPham == null ? null : maSanPham.getTenSanPham();
    }
    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
        if (maSanPham != null) maSanPham.setTenSanPham(tenSanPham);
    }
    public boolean daKiem() {
        return soLuongThucTe != null;
    }
    public int getChenhLech() {
        return soLuongThucTe == null ? 0 : soLuongThucTe - soLuongHeThong;
    }
}
