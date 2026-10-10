package entity;

public class SanPham {

    private String maSanPham;
    private String tenSanPham;
    private double giaNhap;
    private double giaBan;
    private String maDanhMuc;
    private String maNhaCungCap;

    // Chi dung de hien thi (lay tu JOIN / tinh toan, khong phai cot cua bang SanPham)
    private String tenDanhMuc;
    private String tenNhaCungCap;
    private int tonKho;

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

    @Override
    public String toString() {
        return maSanPham + " - " + tenSanPham;
    }
}
