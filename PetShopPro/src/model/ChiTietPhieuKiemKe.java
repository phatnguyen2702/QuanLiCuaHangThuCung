package model;

public class ChiTietPhieuKiemKe {
    private PhieuKiemKe maPhieuKiemKe;
    private SanPham maSanPham;
    private int soLuongHeThong, soLuongThucTe; 
    private String ghiChu;

    public ChiTietPhieuKiemKe(PhieuKiemKe phieu, SanPham sp, int heThong, int thucTe, String ghiChu) {
        this.maPhieuKiemKe = phieu; this.maSanPham = sp; this.soLuongHeThong = heThong;
        this.soLuongThucTe = thucTe; this.ghiChu = ghiChu;
    }
    public boolean daKiem() { return soLuongThucTe >= 0; }
    public int getChenhLech() { return daKiem() ? soLuongThucTe - soLuongHeThong : 0; }
    public PhieuKiemKe getMaPhieuKiemKe() { return maPhieuKiemKe; }
    public SanPham getMaSanPham() { return maSanPham; }
    public int getSoLuongHeThong() { return soLuongHeThong; }
    public void setSoLuongHeThong(int v) { soLuongHeThong = v; }
    public int getSoLuongThucTe() { return soLuongThucTe; }
    public void setSoLuongThucTe(int v) { soLuongThucTe = v; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String v) { ghiChu = v; }
    @Override public String toString() { return maSanPham.getTenSanPham() + ": " + soLuongHeThong + "/" + soLuongThucTe; }
}
