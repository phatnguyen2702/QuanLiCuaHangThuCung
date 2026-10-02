package model;

public class NhaCungCap {
    private String maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi;

    public NhaCungCap(String ma, String ten, String sdt, String email, String diaChi) {
        this.maNhaCungCap = ma; this.tenNhaCungCap = ten; this.soDienThoai = sdt;
        this.email = email; this.diaChi = diaChi;
    }
    public String getMaNhaCungCap() { return maNhaCungCap; }
    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getEmail() { return email; }
    public String getDiaChi() { return diaChi; }
    @Override public String toString() { return tenNhaCungCap; }
}
