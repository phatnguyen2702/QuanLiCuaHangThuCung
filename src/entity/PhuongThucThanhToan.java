package entity;

public class PhuongThucThanhToan {
    private String maPhuongThuc;
    private String tenPhuongThuc;
    private boolean trangThai;
    private String ghiChu;

    public PhuongThucThanhToan(
            String maPhuongThuc,
            String tenPhuongThuc,
            boolean trangThai,
            String ghiChu) {
        this.maPhuongThuc = maPhuongThuc;
        this.tenPhuongThuc = tenPhuongThuc;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public String getMaPhuongThuc() {
        return maPhuongThuc;
    }

    public String getTenPhuongThuc() {
        return tenPhuongThuc;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    @Override
    public String toString() {
        return tenPhuongThuc;
    }
}
