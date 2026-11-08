package entity;
import java.time.LocalDateTime;
public class PhieuKiemKe {
    private String maPhieuKiemKe;
    private LocalDateTime ngayKiemKe;
    private boolean trangThai;
    private NhanVien maNhanVien;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayChot;
    public PhieuKiemKe() {
    }
    public PhieuKiemKe(String maPhieuKiemKe, LocalDateTime ngayKiemKe,
                       boolean trangThai, String maNhanVien) {
        this.maPhieuKiemKe = maPhieuKiemKe;
        this.ngayKiemKe = ngayKiemKe;
        this.trangThai = trangThai;
        setMaNhanVien(maNhanVien);
    }
    public String getMaPhieuKiemKe() { return maPhieuKiemKe; }
    public void setMaPhieuKiemKe(String maPhieuKiemKe) { this.maPhieuKiemKe = maPhieuKiemKe; }
    public LocalDateTime getNgayKiemKe() { return ngayKiemKe; }
    public void setNgayKiemKe(LocalDateTime ngayKiemKe) { this.ngayKiemKe = ngayKiemKe; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    public String getMaNhanVien() { return maNhanVien == null ? null : maNhanVien.getMaNhanVien(); }
    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien == null ? null : new NhanVien();
        if (this.maNhanVien != null) this.maNhanVien.setMaNhanVien(maNhanVien);
    }
    public NhanVien getNhanVien() { return maNhanVien; }
    public void setNhanVien(NhanVien nhanVien) { maNhanVien = nhanVien; }
    public LocalDateTime getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDateTime ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    public LocalDateTime getNgayChot() { return ngayChot; }
    public void setNgayChot(LocalDateTime ngayChot) { this.ngayChot = ngayChot; }
}
