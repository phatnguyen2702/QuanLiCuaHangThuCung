package entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Voucher / mã giảm giá. */
public class Voucher {

    private String maVoucher;
    private String tenVoucher;
    private double phanTramGiam;
    private String dieuKien;
    private boolean trangThai;        // false = đã hủy
    private LocalDate ngayBatDau;     // null = không giới hạn
    private LocalDate ngayKetThuc;    // null = không giới hạn
    private Integer soLuongToiDa;     // null = không giới hạn

    private int daDung;               // số hóa đơn đã dùng voucher (DAO tính sẵn)

    public Voucher() {
    }

    public Voucher(String maVoucher, String tenVoucher, double phanTramGiam,
            String dieuKien, boolean trangThai,
            LocalDate ngayBatDau, LocalDate ngayKetThuc, Integer soLuongToiDa) {
        this.maVoucher = maVoucher;
        this.tenVoucher = tenVoucher;
        this.phanTramGiam = phanTramGiam;
        this.dieuKien = dieuKien;
        this.trangThai = trangThai;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.soLuongToiDa = soLuongToiDa;
    }

    public Voucher(String maVoucher, String tenVoucher, BigDecimal phanTramGiam, String dieuKien) {
        this.maVoucher = maVoucher;
        this.tenVoucher = tenVoucher;
        this.phanTramGiam = phanTramGiam == null ? 0 : phanTramGiam.doubleValue();
        this.dieuKien = dieuKien;
    }

    /** Trạng thái hiện tại: "Da huy", "Sap chay", "Het han", "Het luot" hoặc "Dang chay". */
    public String getTrangThaiText() {
        if (!trangThai) {
            return "Da huy";
        }

        LocalDate homNay = LocalDate.now();

        if (ngayBatDau != null && homNay.isBefore(ngayBatDau)) {
            return "Sap chay";
        }
        if (ngayKetThuc != null && homNay.isAfter(ngayKetThuc)) {
            return "Het han";
        }
        if (soLuongToiDa != null && daDung >= soLuongToiDa) {
            return "Het luot";
        }
        return "Dang chay";
    }

    public String getMaVoucher() {
        return maVoucher;
    }

    public void setMaVoucher(String maVoucher) {
        this.maVoucher = maVoucher;
    }

    public String getTenVoucher() {
        return tenVoucher;
    }

    public void setTenVoucher(String tenVoucher) {
        this.tenVoucher = tenVoucher;
    }

    public double getPhanTramGiam() {
        return phanTramGiam;
    }

    public void setPhanTramGiam(double phanTramGiam) {
        this.phanTramGiam = phanTramGiam;
    }

    public String getDieuKien() {
        return dieuKien;
    }

    public void setDieuKien(String dieuKien) {
        this.dieuKien = dieuKien;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public Integer getSoLuongToiDa() {
        return soLuongToiDa;
    }

    public void setSoLuongToiDa(Integer soLuongToiDa) {
        this.soLuongToiDa = soLuongToiDa;
    }

    public int getDaDung() {
        return daDung;
    }

    public void setDaDung(int daDung) {
        this.daDung = daDung;
    }
}
