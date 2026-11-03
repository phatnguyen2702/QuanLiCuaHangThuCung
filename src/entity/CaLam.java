package entity;

import java.time.LocalTime;

/**
 * Entity tương ứng bảng CaLam trong SQL Server.
 * SQL TIME được ánh xạ thành java.time.LocalTime, không dùng String.
 */
public class CaLam {
    private String maCa;
    private String tenCa;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;

    public CaLam() {
    }

    public CaLam(String maCa, String tenCa, LocalTime gioBatDau, LocalTime gioKetThuc) {
        this.maCa = maCa;
        this.tenCa = tenCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
    }

    public String getMaCa() {
        return maCa;
    }

    public void setMaCa(String maCa) {
        this.maCa = maCa;
    }

    public String getTenCa() {
        return tenCa;
    }

    public void setTenCa(String tenCa) {
        this.tenCa = tenCa;
    }

    public LocalTime getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(LocalTime gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public LocalTime getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(LocalTime gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    @Override
    public String toString() {
        return maCa + " - " + tenCa;
    }
}
