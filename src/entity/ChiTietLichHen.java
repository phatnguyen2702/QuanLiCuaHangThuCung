package entity;

public class ChiTietLichHen {
    private LichHen maLichHen;
    private DichVu maDichVu;
    private NhanVien maNhanVien;
    private String ghiChu;

    public ChiTietLichHen() {
    }

    public ChiTietLichHen(LichHen maLichHen, DichVu maDichVu, NhanVien maNhanVien, String ghiChu) {
        this.maLichHen = maLichHen;
        this.maDichVu = maDichVu;
        this.maNhanVien = maNhanVien;
        this.ghiChu = ghiChu;
    }

    public ChiTietLichHen(String maLichHen, String maDichVu, String maNhanVien, String ghiChu) {
        this(taoLichHenThamChieu(maLichHen), taoDichVuThamChieu(maDichVu), taoNhanVienThamChieu(maNhanVien), ghiChu);
    }

    private static LichHen taoLichHenThamChieu(String maLichHen) {
        if (maLichHen == null) {
            return null;
        }
        LichHen lichHen = new LichHen();
        lichHen.setMaLichHen(maLichHen);
        return lichHen;
    }

    private static DichVu taoDichVuThamChieu(String maDichVu) {
        if (maDichVu == null) {
            return null;
        }
        DichVu dichVu = new DichVu();
        dichVu.setMaDichVu(maDichVu);
        return dichVu;
    }

    private static NhanVien taoNhanVienThamChieu(String maNhanVien) {
        if (maNhanVien == null) {
            return null;
        }
        NhanVien nhanVien = new NhanVien();
        nhanVien.setMaNhanVien(maNhanVien);
        return nhanVien;
    }

    public String getMaLichHen() {
        return maLichHen == null ? null : maLichHen.getMaLichHen();
    }

    public LichHen getLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = taoLichHenThamChieu(maLichHen);
    }

    public void setLichHen(LichHen lichHen) {
        this.maLichHen = lichHen;
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

    public String getMaNhanVien() {
        return maNhanVien == null ? null : maNhanVien.getMaNhanVien();
    }

    public NhanVien getNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = taoNhanVienThamChieu(maNhanVien);
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.maNhanVien = nhanVien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
