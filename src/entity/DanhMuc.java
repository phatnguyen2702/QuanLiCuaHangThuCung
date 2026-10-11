package entity;

public class DanhMuc {

    private String maDanhMuc;
    private String tenDanhMuc;
    private String moTa;

    // Chi dung de hien thi (khong co cot trong bang DanhMuc)
    private int soSanPham;
    private int tongTon;

    public DanhMuc() {
    }

    public DanhMuc(String maDanhMuc, String tenDanhMuc, String moTa) {
        this.maDanhMuc = maDanhMuc;
        this.tenDanhMuc = tenDanhMuc;
        this.moTa = moTa;
    }

    public String getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(String maDanhMuc) { this.maDanhMuc = maDanhMuc; }

    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public int getTongTon() { return tongTon; }
    public void setTongTon(int tongTon) { this.tongTon = tongTon; }

    public int getSoSanPham() { return soSanPham; }
    public void setSoSanPham(int soSanPham) { this.soSanPham = soSanPham; }

    @Override
    public String toString() {
        return tenDanhMuc;
    }
}
