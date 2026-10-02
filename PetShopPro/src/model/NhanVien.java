package model;

public class NhanVien {
    private String maNhanVien, hoTen, soDienThoai, email, matKhau, gioiTinh, vaiTro;

    public NhanVien(String ma, String hoTen, String sdt, String email, String matKhau, String gioiTinh, String vaiTro) {
        this.maNhanVien = ma; this.hoTen = hoTen; this.soDienThoai = sdt; this.email = email;
        this.matKhau = matKhau; this.gioiTinh = gioiTinh; this.vaiTro = vaiTro;
    }
    public boolean dangNhap(String matKhau) { return this.matKhau != null && this.matKhau.equals(matKhau); }
    public String getMaNhanVien() { return maNhanVien; }
    public String getHoTen() { return hoTen; }
    public String getVaiTro() { return vaiTro; }
    @Override public String toString() { return hoTen; }
}
