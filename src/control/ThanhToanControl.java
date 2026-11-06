package control;

import dao.ThanhToanDao;
import entity.ChiTietHoaDon;
import entity.KhachHang;
import entity.PhuongThucThanhToan;
import entity.Voucher;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ThanhToanControl {
    private final ThanhToanDao dao = new ThanhToanDao();

    public List<ChiTietHoaDon> timMatHangTheoMa(String ma) throws SQLException {
        return dao.timMatHangTheoMa(ma);
    }

    public List<String> layDanhSachMaSanPham() throws SQLException {
        return dao.layDanhSachMaSanPham();
    }

    public Map<String, String> layDanhSachSanPham() throws SQLException {
        return dao.layDanhSachSanPham();
    }

    public List<String> layDanhSachMaKhachHang() throws SQLException {
        return dao.layDanhSachMaKhachHang();
    }

    public List<KhachHang> layDanhSachKhachHang() throws SQLException {
        return dao.layDanhSachKhachHang();
    }

    public List<ChiTietHoaDon> timMatHang(String tuKhoa) throws SQLException {
        return dao.timMatHang(tuKhoa);
    }

    public Map<String, List<ChiTietHoaDon>> layPhieuDichVuDangXuLy(String maKhachHang)
            throws SQLException {
        return dao.layPhieuDichVuDangXuLy(maKhachHang);
    }

    public KhachHang timKhachHang(String maHoacSoDienThoai) throws SQLException {
        return dao.timKhachHang(maHoacSoDienThoai);
    }

    public Voucher timVoucher(String maHoacTen) throws SQLException {
        return dao.timVoucher(maHoacTen);
    }

    public List<Voucher> layDanhSachVoucher() throws SQLException {
        return dao.layDanhSachVoucher();
    }

    public boolean laKhachHangThanhVien(String maKhachHang) throws SQLException {
        return dao.laKhachHangThanhVien(maKhachHang);
    }

    public List<PhuongThucThanhToan> layPhuongThucThanhToan() throws SQLException {
        return dao.layPhuongThucThanhToan();
    }

    public String taoMaHoaDon(String maKhachHang, LocalDateTime ngayLap) throws SQLException {
        return dao.taoMaHoaDon(maKhachHang, ngayLap);
    }

    public String taoHoaDon(
            String maHoaDon,
            String maNhanVien,
            String maKhachHang,
            String maVoucher,
            String maPhuongThuc,
            LocalDateTime ngayLap,
            double phanTramGiam,
            List<ChiTietHoaDon> chiTiet,
            List<String> maPhieuDichVus) throws SQLException {
        return dao.taoHoaDon(
                maHoaDon, maNhanVien, maKhachHang, maVoucher, maPhuongThuc,
                ngayLap, phanTramGiam, chiTiet, maPhieuDichVus);
    }
}
