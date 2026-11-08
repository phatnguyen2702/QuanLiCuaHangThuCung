package entity;

import control.HoaDonControl;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class HoaDon {
    private String maHoaDon;
    private LocalDateTime ngayLap;
    private PhuongThucThanhToan maPhuongThuc;
    private KhachHang maKhachHang;
    private NhanVien maNhanVien;
    private Voucher maVoucher;

    public HoaDon() {
    }

    public HoaDon(
            String maHoaDon,
            LocalDateTime ngayLap,
            String maPhuongThuc,
            String maKhachHang) {
        this(maHoaDon, ngayLap, maPhuongThuc, maKhachHang, null, null, null);
    }

    public HoaDon(
            String maHoaDon,
            LocalDateTime ngayLap,
            String maPhuongThuc,
            String maKhachHang,
            String tenKhachHang,
            String soDienThoai,
            String tenPhuongThuc) {
        this(maHoaDon, ngayLap, maPhuongThuc, maKhachHang,
                tenKhachHang, soDienThoai, tenPhuongThuc, null, null);
    }

    public HoaDon(
            String maHoaDon,
            LocalDateTime ngayLap,
            String maPhuongThuc,
            String maKhachHang,
            String tenKhachHang,
            String soDienThoai,
            String tenPhuongThuc,
            String maNhanVien,
            String tenNhanVien) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        if (maPhuongThuc != null) {
            this.maPhuongThuc = new PhuongThucThanhToan(
                    maPhuongThuc, tenPhuongThuc, true, null);
        }
        if (maKhachHang != null) {
            this.maKhachHang = new KhachHang(
                    maKhachHang, tenKhachHang, soDienThoai, null, null, 0);
        }
        if (maNhanVien != null) {
            this.maNhanVien = new NhanVien(
                    maNhanVien, tenNhanVien, null, null, null, null, null);
        }
    }

    public HoaDon(
            String maHoaDon,
            LocalDateTime ngayLap,
            PhuongThucThanhToan maPhuongThuc,
            KhachHang maKhachHang,
            NhanVien maNhanVien,
            Voucher maVoucher) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.maPhuongThuc = maPhuongThuc;
        this.maKhachHang = maKhachHang;
        this.maNhanVien = maNhanVien;
        this.maVoucher = maVoucher;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public String getMaPhuongThuc() {
        return maPhuongThuc == null ? null : maPhuongThuc.getMaPhuongThuc();
    }

    public PhuongThucThanhToan getPhuongThucThanhToan() {
        return maPhuongThuc;
    }

    public String getMaKhachHang() {
        return maKhachHang == null ? null : maKhachHang.getMaKhachHang();
    }

    public KhachHang getKhachHang() {
        return maKhachHang;
    }

    public String getMaNhanVien() {
        return maNhanVien == null ? null : maNhanVien.getMaNhanVien();
    }

    public NhanVien getNhanVien() {
        return maNhanVien;
    }

    public String getTenNhanVien() {
        return maNhanVien == null ? null : maNhanVien.getHoTen();
    }

    public String getMaVoucher() {
        return maVoucher == null ? null : maVoucher.getMaVoucher();
    }

    public Voucher getVoucher() {
        return maVoucher;
    }

    public String getTenKhachHang() {
        return maKhachHang == null ? null : maKhachHang.getHoTen();
    }

    public String getSoDienThoai() {
        return maKhachHang == null ? null : maKhachHang.getSoDienThoai();
    }

    public String getTenPhuongThuc() {
        return maPhuongThuc == null ? null : maPhuongThuc.getTenPhuongThuc();
    }

    public double getTongTien() throws SQLException {
        return tinhTongTien();
    }

    public List<ChiTietHoaDon> getChiTiet() throws SQLException {
        return chiTiet();
    }

    public double tinhTongTien() throws SQLException {
        return new HoaDonControl().layTongTienHoaDon(maHoaDon);
    }

    public List<ChiTietHoaDon> chiTiet() throws SQLException {
        return new HoaDonControl().layDanhSachChiTietHoaDon(maHoaDon);
    }
}
