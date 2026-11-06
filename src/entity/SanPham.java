package entity;
public class SanPham {
    private String maSanPham;
    private String tenSanPham;
    private double giaNhap;
    private double giaBan;
    private String maDanhMuc;
    private String maNhaCungCap;
    private String tenDanhMuc;
    private String tenNhaCungCap;
    private int tonKho;
    private String maPhieuNhap;
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
        this.maDanhMuc = maDanhMuc;
        this.maNhaCungCap = maNhaCungCap;
    }
    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public double getGiaNhap() { return giaNhap; }
    public void setGiaNhap(double giaNhap) { this.giaNhap = giaNhap; }
    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }
    public String getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(String maDanhMuc) { this.maDanhMuc = maDanhMuc; }
    public String getMaNhaCungCap() { return maNhaCungCap; }
    public void setMaNhaCungCap(String maNhaCungCap) { this.maNhaCungCap = maNhaCungCap; }
    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }
    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }
    public int getTonKho() { return tonKho; }
    public void setTonKho(int tonKho) { this.tonKho = tonKho; }
    public String getMaPhieuNhap() { return maPhieuNhap; }
    public void setMaPhieuNhap(String maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }
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
