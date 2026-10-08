package entity;

import java.time.LocalDate;

public class LichPhanCa {

    private String maPhanCa;
    private LocalDate ngayLamViec;
    private String maNhanVien;
    private String maCa;

    // Du lieu hien thi them tu NhanVien va CaLam
    private String hoTen;
    private String tenCa;
    private String gioBatDau;
    private String gioKetThuc;

    public LichPhanCa() {
    }

    public LichPhanCa(String maPhanCa, LocalDate ngayLamViec,
            String maNhanVien, String maCa) {
        this.maPhanCa = maPhanCa;
        this.ngayLamViec = ngayLamViec;
        this.maNhanVien = maNhanVien;
        this.maCa = maCa;
    }

    public String getMaPhanCa() {
        return maPhanCa;
    }

    public void setMaPhanCa(String maPhanCa) {
        this.maPhanCa = maPhanCa;
    }

    public LocalDate getNgayLamViec() {
        return ngayLamViec;
    }

    public void setNgayLamViec(LocalDate ngayLamViec) {
        this.ngayLamViec = ngayLamViec;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getMaCa() {
        return maCa;
    }

    public void setMaCa(String maCa) {
        this.maCa = maCa;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getTenCa() {
        return tenCa;
    }

    public void setTenCa(String tenCa) {
        this.tenCa = tenCa;
    }

    public String getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(String gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public String getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(String gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    public String getThongTinCa() {

        if (tenCa == null) {
            return maCa;
        }

        if (gioBatDau != null && gioKetThuc != null) {
            return maCa + " - " + tenCa
                    + ": " + gioBatDau
                    + "-" + gioKetThuc;
        }

        return maCa + " - " + tenCa;
    }

    @Override
    public String toString() {
        return maPhanCa;
    }
}