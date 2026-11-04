package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LichPhanCa {
    private String maPhanCa;
    private LocalDate ngayLamViec;
    private NhanVien nhanVien;
    private CaLam caLam;
    private String hoTen;
    private String tenCa;
    private LocalDateTime gioBatDau;
    private LocalDateTime gioKetThuc;

    public LichPhanCa() {
    }

    public LichPhanCa(String maPhanCa, LocalDate ngayLamViec,
            String maNhanVien, String maCa) {
        this.maPhanCa = maPhanCa;
        this.ngayLamViec = ngayLamViec;
        setMaNhanVien(maNhanVien);
        setMaCa(maCa);
    }

    public LichPhanCa(String maPhanCa, LocalDate ngayLamViec,
            NhanVien nhanVien, CaLam caLam) {
        this.maPhanCa = maPhanCa;
        this.ngayLamViec = ngayLamViec;
        this.nhanVien = nhanVien;
        this.caLam = caLam;
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
        return nhanVien == null ? null : nhanVien.getMaNhanVien();
    }

    public void setMaNhanVien(String maNhanVien) {
        this.nhanVien = maNhanVien == null ? null : new NhanVien();
        if (this.nhanVien != null) {
            this.nhanVien.setMaNhanVien(maNhanVien);
        }
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getMaCa() {
        return caLam == null ? null : caLam.getMaCa();
    }

    public void setMaCa(String maCa) {
        this.caLam = maCa == null ? null : new CaLam();
        if (this.caLam != null) {
            this.caLam.setMaCa(maCa);
        }
    }

    public CaLam getCaLam() {
        return caLam;
    }

    public void setCaLam(CaLam caLam) {
        this.caLam = caLam;
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

    public LocalDateTime getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(LocalDateTime gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public LocalDateTime getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(LocalDateTime gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    public String getThongTinCa() {
        if (tenCa == null) {
            return getMaCa();
        }

        if (gioBatDau != null && gioKetThuc != null) {
            DateTimeFormatter gio = DateTimeFormatter.ofPattern("HH:mm");
            return getMaCa() + " - " + tenCa
                    + ": " + gioBatDau.format(gio)
                    + "-" + gioKetThuc.format(gio);
        }

        return getMaCa() + " - " + tenCa;
    }

    @Override
    public String toString() {
        return maPhanCa;
    }
}
