package entity;
public class SanPham {
    private String maSanPham;
    private String tenSanPham;
    private double giaNhap;
    private double giaBan;
    private DanhMuc maDanhMuc;
    private NhaCungCap maNhaCungCap;
    private String tenDanhMuc;
    private String tenNhaCungCap;
    private int tonKho;
    private PhieuNhapKho maPhieuNhap;
    private java.time.LocalDate ngayNhap;
    private java.time.LocalDate hanSuDung;
    private boolean ngungKinhDoanh;
    public SanPham() {
    }
    public SanPham(String maSanPham, String tenSanPham, double giaNhap,
                   double giaBan, String maDanhMuc, String maNhaCungCap) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.giaNhap = giaNhap;
        this.giaBan = giaBan;
        setMaDanhMuc(maDanhMuc);
        setMaNhaCungCap(maNhaCungCap);
    }
    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public double getGiaNhap() { return giaNhap; }
    public void setGiaNhap(double giaNhap) { this.giaNhap = giaNhap; }
    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }
    public String getMaDanhMuc() { return maDanhMuc == null ? null : maDanhMuc.getMaDanhMuc(); }
    public void setMaDanhMuc(String maDanhMuc) {
        this.maDanhMuc = maDanhMuc == null ? null : new DanhMuc(maDanhMuc, null, null);
    }
    public DanhMuc getDanhMuc() { return maDanhMuc; }
    public void setDanhMuc(DanhMuc danhMuc) {
        maDanhMuc = danhMuc;
        if (danhMuc != null) tenDanhMuc = danhMuc.getTenDanhMuc();
    }
    public String getMaNhaCungCap() { return maNhaCungCap == null ? null : maNhaCungCap.getMaNhaCungCap(); }
    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap == null ? null : new NhaCungCap(maNhaCungCap, null, null, null, null);
    }
    public NhaCungCap getNhaCungCap() { return maNhaCungCap; }
    public void setNhaCungCap(NhaCungCap nhaCungCap) {
        maNhaCungCap = nhaCungCap;
        if (nhaCungCap != null) tenNhaCungCap = nhaCungCap.getTenNhaCungCap();
    }
    public String getTenDanhMuc() {
        return tenDanhMuc != null ? tenDanhMuc : maDanhMuc == null ? null : maDanhMuc.getTenDanhMuc();
    }
    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
        if (maDanhMuc != null) maDanhMuc.setTenDanhMuc(tenDanhMuc);
    }
    public String getTenNhaCungCap() {
        return tenNhaCungCap != null ? tenNhaCungCap
                : maNhaCungCap == null ? null : maNhaCungCap.getTenNhaCungCap();
    }
    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
        if (maNhaCungCap != null) maNhaCungCap.setTenNhaCungCap(tenNhaCungCap);
    }
    public int getTonKho() { return tonKho; }
    public void setTonKho(int tonKho) { this.tonKho = tonKho; }
    public String getMaPhieuNhap() { return maPhieuNhap == null ? null : maPhieuNhap.getMaPhieuNhap(); }
    public void setMaPhieuNhap(String maPhieuNhap) {
        this.maPhieuNhap = maPhieuNhap == null ? null : new PhieuNhapKho();
        if (this.maPhieuNhap != null) this.maPhieuNhap.setMaPhieuNhap(maPhieuNhap);
    }
    public PhieuNhapKho getPhieuNhapKho() { return maPhieuNhap; }
    public void setPhieuNhapKho(PhieuNhapKho phieuNhapKho) { maPhieuNhap = phieuNhapKho; }
    public java.time.LocalDate getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(java.time.LocalDate ngayNhap) { this.ngayNhap = ngayNhap; }
    public java.time.LocalDate getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(java.time.LocalDate hanSuDung) { this.hanSuDung = hanSuDung; }
    public boolean isNgungKinhDoanh() { return ngungKinhDoanh; }
    public void setNgungKinhDoanh(boolean ngungKinhDoanh) { this.ngungKinhDoanh = ngungKinhDoanh; }
    @Override
    public String toString() {
        return maSanPham + " - " + tenSanPham;
    }
}
