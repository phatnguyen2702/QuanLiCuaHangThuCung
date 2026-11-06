package entity;

public class ChiTietHoaDon {
    private HoaDon maHoaDon;
    private ThuCung maThuCung;
    private DichVu maDichVu;
    private SanPham maSanPham;
    private int soLuong;
    private double donGia;
    private double phanTramGiam;

    public ChiTietHoaDon(
            HoaDon maHoaDon,
            ThuCung maThuCung,
            DichVu maDichVu,
            SanPham maSanPham,
            int soLuong,
            double donGia,
            double phanTramGiam) {
        this.maHoaDon = maHoaDon;
        this.maThuCung = maThuCung;
        this.maDichVu = maDichVu;
        this.maSanPham = maSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.phanTramGiam = phanTramGiam;
    }

    public ChiTietHoaDon(
            String maHoaDon,
            String maThuCung,
            String maDichVu,
            String maSanPham,
            int soLuong,
            double donGia,
            double phanTramGiam) {
        this(maHoaDon, maThuCung, maDichVu, maSanPham, soLuong, donGia, phanTramGiam,
                null, null, null);
    }

    public ChiTietHoaDon(
            String maHoaDon,
            String maThuCung,
            String maDichVu,
            String maSanPham,
            int soLuong,
            double donGia,
            double phanTramGiam,
            String tenHang,
            String loaiHang,
            String tenThuCung) {
        this(
                taoHoaDonThamChieu(maHoaDon),
                taoThuCungThamChieu(maThuCung, tenThuCung),
                taoDichVuThamChieu(maDichVu, tenHang, loaiHang),
                taoSanPhamThamChieu(maSanPham, tenHang),
                soLuong,
                donGia,
                phanTramGiam);
    }

    public static ChiTietHoaDon sanPham(String ma, String ten, double donGia) {
        SanPham sanPham = taoSanPhamThamChieu(ma, ten);
        sanPham.setGiaBan(donGia);
        return new ChiTietHoaDon(null, null, null, sanPham, 1, donGia, 0);
    }

    public static ChiTietHoaDon dichVu(
            String ma, String ten, double donGia, String maThuCung) {
        DichVu dichVu = taoDichVuThamChieu(ma, ten, "Dịch vụ");
        return new ChiTietHoaDon(
                null, taoThuCungThamChieu(maThuCung, null), dichVu, null, 1,
                donGia, 0);
    }

    private static HoaDon taoHoaDonThamChieu(String ma) {
        if (ma == null) {
            return null;
        }
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(ma);
        return hoaDon;
    }

    private static ThuCung taoThuCungThamChieu(String ma, String ten) {
        if (ma == null) {
            return null;
        }
        ThuCung thuCung = new ThuCung();
        thuCung.setMaThuCung(ma);
        thuCung.setTenThuCung(ten);
        return thuCung;
    }

    private static DichVu taoDichVuThamChieu(String ma, String ten, String loaiHang) {
        if (ma == null) {
            return null;
        }
        DichVu dichVu = new DichVu();
        dichVu.setMaDichVu(ma);
        dichVu.setTenDichVu(ten);
        return dichVu;
    }

    private static SanPham taoSanPhamThamChieu(String ma, String ten) {
        if (ma == null) {
            return null;
        }
        SanPham sanPham = new SanPham();
        sanPham.setMaSanPham(ma);
        sanPham.setTenSanPham(ten);
        return sanPham;
    }

    public String getMaHoaDon() {
        return maHoaDon == null ? null : maHoaDon.getMaHoaDon();
    }

    public HoaDon getHoaDon() {
        return maHoaDon;
    }

    public String getMaThuCung() {
        return maThuCung == null ? null : maThuCung.getMaThuCung();
    }

    public ThuCung getThuCung() {
        return maThuCung;
    }

    public String getMaDichVu() {
        return maDichVu == null ? null : maDichVu.getMaDichVu();
    }

    public DichVu getDichVu() {
        return maDichVu;
    }

    public String getMaSanPham() {
        return maSanPham == null ? null : maSanPham.getMaSanPham();
    }

    public SanPham getSanPham() {
        return maSanPham;
    }

    public String getMaHang() {
        if (maSanPham != null) {
            return maSanPham.getMaSanPham();
        }
        if (maDichVu != null) {
            return maDichVu.getMaDichVu();
        }
        return getMaThuCung();
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong < 1) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        }
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public double getPhanTramGiam() {
        return phanTramGiam;
    }

    public String getTenHang() {
        if (maSanPham != null) {
            return maSanPham.getTenSanPham();
        }
        if (maDichVu != null) {
            return maDichVu.getTenDichVu();
        }
        return maThuCung == null ? null : maThuCung.getTenThuCung();
    }

    public String getTenMatHang() {
        return getTenHang();
    }

    public String getLoaiHang() {
        if (maSanPham != null) {
            return "Sản phẩm";
        }
        if (maDichVu != null) {
            return "Dịch vụ";
        }
        return "Thú cưng";
    }

    public String getTenThuCung() {
        return maThuCung == null ? null : maThuCung.getTenThuCung();
    }

    public double getThanhTien() {
        return donGia * soLuong * (1 - phanTramGiam / 100);
    }
}
