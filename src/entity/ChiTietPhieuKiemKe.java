package entity;

public class ChiTietPhieuKiemKe {

    private String maPhieuKiemKe;
    private String maSanPham;
    private int soLuongHeThong;
    private Integer soLuongThucTe;  // null = chua kiem
    private boolean trangThai;      // true = khop, false = chenh lech

    // Chi dung de hien thi
    private String tenSanPham;

    public ChiTietPhieuKiemKe() {
    }

    public ChiTietPhieuKiemKe(String maPhieuKiemKe, String maSanPham,
                              int soLuongHeThong, Integer soLuongThucTe,
                              boolean trangThai) {
        this.maPhieuKiemKe = maPhieuKiemKe;
        this.maSanPham = maSanPham;
        this.soLuongHeThong = soLuongHeThong;
        this.soLuongThucTe = soLuongThucTe;
        this.trangThai = trangThai;
    }

    public String getMaPhieuKiemKe() { return maPhieuKiemKe; }
    public void setMaPhieuKiemKe(String maPhieuKiemKe) { this.maPhieuKiemKe = maPhieuKiemKe; }

    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }

    public int getSoLuongHeThong() { return soLuongHeThong; }
    public void setSoLuongHeThong(int soLuongHeThong) { this.soLuongHeThong = soLuongHeThong; }

    public Integer getSoLuongThucTe() { return soLuongThucTe; }
    public void setSoLuongThucTe(Integer soLuongThucTe) { this.soLuongThucTe = soLuongThucTe; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public boolean daKiem() {
        return soLuongThucTe != null;
    }

    public int getChenhLech() {
        return soLuongThucTe == null ? 0 : soLuongThucTe - soLuongHeThong;
    }
}
