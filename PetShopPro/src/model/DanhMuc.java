package model;

public class DanhMuc {
    private String maDanhMuc, tenDanhMuc, moTa;

    public DanhMuc(String maDanhMuc, String tenDanhMuc, String moTa) {
        this.maDanhMuc = maDanhMuc; this.tenDanhMuc = tenDanhMuc; this.moTa = moTa;
    }
    public String getMaDanhMuc() { return maDanhMuc; }
    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String v) { tenDanhMuc = v; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String v) { moTa = v; }
    @Override public String toString() { return tenDanhMuc; }
}
