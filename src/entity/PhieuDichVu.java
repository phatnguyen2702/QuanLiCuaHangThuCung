package entity;

public class PhieuDichVu {
    private String maPhieuDichVu;
    private String trangThai;
    private String maKhachHang;
    private String maThuCung;
    private String maNhanVien;
    private String maLichHen;

    // Các trường JOIN để hiển thị lên bảng
    private String tenKhachHang;
    private String tenThuCung;
    private String tenNhanVien;

    public PhieuDichVu() {
    }

    public PhieuDichVu(String maPhieuDichVu, String trangThai, String maKhachHang, String maThuCung, String maNhanVien,
            String maLichHen) {
        this.maPhieuDichVu = maPhieuDichVu;
        this.trangThai = trangThai;
        this.maKhachHang = maKhachHang;
        this.maThuCung = maThuCung;
        this.maNhanVien = maNhanVien;
        this.maLichHen = maLichHen;
    }

    // Getters and Setters
    public String getMaPhieuDichVu() {
        return maPhieuDichVu;
    }

    public void setMaPhieuDichVu(String maPhieuDichVu) {
        this.maPhieuDichVu = maPhieuDichVu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getMaThuCung() {
        return maThuCung;
    }

    public void setMaThuCung(String maThuCung) {
        this.maThuCung = maThuCung;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }
}