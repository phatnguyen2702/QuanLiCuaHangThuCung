package entity;

public class ChiTietPhieuDichVu {
    private PhieuDichVu maPhieuDichVu;
    private DichVu maDichVu;
    private int soLuong;
    private double donGia;
    private String ghiChu;

    public ChiTietPhieuDichVu() {
    }

    public ChiTietPhieuDichVu(PhieuDichVu maPhieuDichVu, DichVu maDichVu, int soLuong, double donGia, String ghiChu) {
        this.maPhieuDichVu = maPhieuDichVu;
        this.maDichVu = maDichVu;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.ghiChu = ghiChu;
    }

    public ChiTietPhieuDichVu(String maPhieuDichVu, String maDichVu, int soLuong, double donGia, String ghiChu) {
        this(taoPhieuDichVuThamChieu(maPhieuDichVu), taoDichVuThamChieu(maDichVu), soLuong, donGia, ghiChu);
    }

    private static PhieuDichVu taoPhieuDichVuThamChieu(String maPhieuDichVu) {
        if (maPhieuDichVu == null) {
            return null;
        }
        PhieuDichVu phieuDichVu = new PhieuDichVu();
        phieuDichVu.setMaPhieuDichVu(maPhieuDichVu);
        return phieuDichVu;
    }

    private static DichVu taoDichVuThamChieu(String maDichVu) {
        if (maDichVu == null) {
            return null;
        }
        DichVu dichVu = new DichVu();
        dichVu.setMaDichVu(maDichVu);
        return dichVu;
    }

    public String getMaPhieuDichVu() {
        return maPhieuDichVu == null ? null : maPhieuDichVu.getMaPhieuDichVu();
    }

    public PhieuDichVu getPhieuDichVu() {
        return maPhieuDichVu;
    }

    public void setMaPhieuDichVu(String maPhieuDichVu) {
        this.maPhieuDichVu = taoPhieuDichVuThamChieu(maPhieuDichVu);
    }

    public void setPhieuDichVu(PhieuDichVu phieuDichVu) {
        this.maPhieuDichVu = phieuDichVu;
    }

    public String getMaDichVu() {
        return maDichVu == null ? null : maDichVu.getMaDichVu();
    }

    public DichVu getDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = taoDichVuThamChieu(maDichVu);
    }

    public void setDichVu(DichVu dichVu) {
        this.maDichVu = dichVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public double getThanhTien() {
        return soLuong * donGia;
    }
}
