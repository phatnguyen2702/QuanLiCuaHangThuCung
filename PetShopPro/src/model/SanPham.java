package model;

public class SanPham {
    private String maSanPham, tenSanPham;
    private double giaNhap, giaBan;
    private DanhMuc maDanhMuc;
    private NhaCungCap maNhaCungCap;
    private int soLuongTon; 

    public SanPham(String ma, String ten, double giaNhap, double giaBan, DanhMuc dm, NhaCungCap ncc, int ton) {
        this.maSanPham = ma; this.tenSanPham = ten; this.giaNhap = giaNhap; this.giaBan = giaBan;
        this.maDanhMuc = dm; this.maNhaCungCap = ncc; this.soLuongTon = ton;
    }
    public String getMaSanPham() { return maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String v) { tenSanPham = v; }
    public double getGiaNhap() { return giaNhap; }
    public void setGiaNhap(double v) { giaNhap = v; }
    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double v) { giaBan = v; }
    public DanhMuc getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(DanhMuc v) { maDanhMuc = v; }
    public NhaCungCap getMaNhaCungCap() { return maNhaCungCap; }
    public void setMaNhaCungCap(NhaCungCap v) { maNhaCungCap = v; }
    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int v) { soLuongTon = v; }
    @Override public String toString() { return maSanPham + " - " + tenSanPham; }
}
