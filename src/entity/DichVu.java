package entity;

import java.math.BigDecimal;

/** Dich vu Spa duoc luu trong bang DichVu. */
public class DichVu {
    private String maDichVu;
    private String tenDichVu;
    private BigDecimal giaDichVu;
    private String moTa;

    public DichVu() {}

    public DichVu(String maDichVu, String tenDichVu, BigDecimal giaDichVu, String moTa) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.giaDichVu = giaDichVu;
        this.moTa = moTa;
    }

    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String maDichVu) { this.maDichVu = maDichVu; }
    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }
    public BigDecimal getGiaDichVu() { return giaDichVu; }
    public void setGiaDichVu(BigDecimal giaDichVu) { this.giaDichVu = giaDichVu; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
}
