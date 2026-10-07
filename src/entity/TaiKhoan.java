package entity;

public class TaiKhoan {

    private String maNhanVien;
    private String hoTen;
    private String matKhau;
    private String vaiTro;
    private boolean trangThai;

    public TaiKhoan() {
    }

    public TaiKhoan(String maNhanVien, String hoTen,
                    String matKhau, String vaiTro,
                    boolean trangThai) {

        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }
}