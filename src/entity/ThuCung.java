package entity;

public class ThuCung {
    private String maThuCung;
    private String tenThuCung;
    private String loai;
    private String giong;
    private String gioiTinh;
    private double canNang;
    private String maKhachHang;
    private String trangThai;

    public ThuCung() {
    }

    public ThuCung(String maThuCung, String tenThuCung, String loai, String giong, String gioiTinh, double canNang,
            String maKhachHang, String trangThai) {
        this.maThuCung = maThuCung;
        this.tenThuCung = tenThuCung;
        this.loai = loai;
        this.giong = giong;
        this.gioiTinh = gioiTinh;
        this.canNang = canNang;
        this.maKhachHang = maKhachHang;
        this.trangThai = trangThai;
    }

    // Getters and Setters
    public String getMaThuCung() {
        return maThuCung;
    }

    public void setMaThuCung(String maThuCung) {
        this.maThuCung = maThuCung;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public String getGiong() {
        return giong;
    }

    public void setGiong(String giong) {
        this.giong = giong;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public double getCanNang() {
        return canNang;
    }

    public void setCanNang(double canNang) {
        this.canNang = canNang;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}