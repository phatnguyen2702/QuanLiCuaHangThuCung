package entity;

import java.time.LocalDateTime;

public class PhieuKiemKe {

    private String maPhieuKiemKe;
    private LocalDateTime ngayKiemKe;
    private boolean trangThai;      // true = da chot
    private String maNhanVien;

    public PhieuKiemKe() {
    }

    public PhieuKiemKe(String maPhieuKiemKe, LocalDateTime ngayKiemKe,
                       boolean trangThai, String maNhanVien) {
        this.maPhieuKiemKe = maPhieuKiemKe;
        this.ngayKiemKe = ngayKiemKe;
        this.trangThai = trangThai;
        this.maNhanVien = maNhanVien;
    }

    public String getMaPhieuKiemKe() { return maPhieuKiemKe; }
    public void setMaPhieuKiemKe(String maPhieuKiemKe) { this.maPhieuKiemKe = maPhieuKiemKe; }

    public LocalDateTime getNgayKiemKe() { return ngayKiemKe; }
    public void setNgayKiemKe(LocalDateTime ngayKiemKe) { this.ngayKiemKe = ngayKiemKe; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }
}
