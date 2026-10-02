package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PhieuKiemKe {
    private String maPhieuKiemKe;
    private NhanVien maNhanVien;
    private LocalDateTime ngayKiemKe;
    private boolean trangThai;
    private final List<ChiTietPhieuKiemKe> chiTiet = new ArrayList<>();

    public PhieuKiemKe(String ma, NhanVien nv, LocalDateTime ngay, boolean trangThai) {
        this.maPhieuKiemKe = ma; this.maNhanVien = nv; this.ngayKiemKe = ngay; this.trangThai = trangThai;
    }

    public int tinhChenhLech() {
        int t = 0;
        for (ChiTietPhieuKiemKe c : chiTiet) t += c.getChenhLech();
        return t;
    }
    public String getMaPhieuKiemKe() { return maPhieuKiemKe; }
    public NhanVien getMaNhanVien() { return maNhanVien; }
    public LocalDateTime getNgayKiemKe() { return ngayKiemKe; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean v) { trangThai = v; }
    public List<ChiTietPhieuKiemKe> getChiTiet() { return chiTiet; }
}
