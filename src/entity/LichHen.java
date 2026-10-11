package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LichHen {
    private String maLichHen;
    private LocalDateTime ngayGioHen;
    private String ghiChu;
    private String maKhachHang;
    private String maNhanVien;
    private String maThuCung;

    // Các trường dữ liệu bổ sung khi JOIN bảng hiển thị lên UI
    private String tenKhachHang;
    private String tenThuCung;
    private String tenNhanVien;
    private String trangThai; // Bổ sung cho UI

    public LichHen() {
    }

    public LichHen(String maLichHen, LocalDateTime ngayGioHen, String ghiChu, String maKhachHang, String maNhanVien,
            String maThuCung) {
        this.maLichHen = maLichHen;
        this.ngayGioHen = ngayGioHen;
        this.ghiChu = ghiChu;
        this.maKhachHang = maKhachHang;
        this.maNhanVien = maNhanVien;
        this.maThuCung = maThuCung;
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    public LocalDateTime getNgayGioHen() {
        return ngayGioHen;
    }

    public void setNgayGioHen(LocalDateTime ngayGioHen) {
        this.ngayGioHen = ngayGioHen;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getMaThuCung() {
        return maThuCung;
    }

    public void setMaThuCung(String maThuCung) {
        this.maThuCung = maThuCung;
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNgayGioHenFormat() {
        if (ngayGioHen != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a");
            return ngayGioHen.format(formatter);
        }
        return "";
    }
}