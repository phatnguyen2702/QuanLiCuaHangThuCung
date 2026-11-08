package control;

import dao.HoaDonDAO;
import dao.LichHenDAO;
import dao.NhanVienDAO;
import dao.TrangChuDAO;
import entity.ChiTietHoaDon;
import entity.LichHen;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;




public final class TrangChuControl {
    private final TrangChuDAO dao = new TrangChuDAO();
    private static final List<ChiTietHoaDon> GIO_HANG = new ArrayList<>();

    public static String maNV;
    public static String hoTen;
    public static String vaiTro;

    public static void dat(String ma, String ten, String role) {
        GIO_HANG.clear();
        maNV = ma;
        hoTen = ten;
        vaiTro = role;
    }

    public static List<ChiTietHoaDon> gioHang() {
        return GIO_HANG;
    }

    public void kiemTraPhien(String maNV, String vaiTro) {
        if (maNV == null || maNV.isBlank()
                || PhanQuyenControl.vaiTro(vaiTro) == PhanQuyenControl.VaiTro.KHONG_HOP_LE)
            throw new SecurityException("Chưa đăng nhập hoặc vai trò không hợp lệ.");
    }

    public static final class ThongKeHoaDon {
        private final double doanhThuHomNay;
        private final int hoaDonHomNay;

        public ThongKeHoaDon(double doanhThuHomNay, int hoaDonHomNay) {
            this.doanhThuHomNay = doanhThuHomNay;
            this.hoaDonHomNay = hoaDonHomNay;
        }

        public double getDoanhThuHomNay() {
            return doanhThuHomNay;
        }

        public int getHoaDonHomNay() {
            return hoaDonHomNay;
        }
    }

    public ThongKeHoaDon thongKeHoaDon() throws SQLException {
        HoaDonDAO.ThongKe kq = new HoaDonControl().layThongKe();
        return new ThongKeHoaDon(kq.getDoanhThuHomNay(), kq.getHoaDonHomNay());
    }

    public int demLichSpaHomNay() throws SQLException {
        return dao.demLichSpaHomNay();
    }

    public int demPhieuNhapHomNay() throws SQLException {
        return dao.demPhieuNhapHomNay();
    }

    public List<LichHen> lichSpaHomNay(LocalDateTime now) throws SQLException {
        return new LichHenDAO().getAll().stream()
                .filter(lh -> lh.getNgayGioHen() != null
                        && lh.getNgayGioHen().toLocalDate().equals(now.toLocalDate())
                        && !"Đã hủy".equalsIgnoreCase(lh.getTrangThai() == null ? "" : lh.getTrangThai().trim()))
                .sorted(Comparator.comparing(LichHen::getNgayGioHen))
                .toList();
    }

    public List<LichHen> lichSpaConLai(List<LichHen> lichHomNay, LocalDateTime now) {
        return lichHomNay.stream().filter(lh -> !lh.getNgayGioHen().isBefore(now)).toList();
    }

    public void kiemTraDoiMatKhau(char[] cu, char[] moi, char[] xacNhan) {
        if (cu == null || moi == null || xacNhan == null || cu.length == 0 || moi.length == 0 || xacNhan.length == 0)
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ ba ô mật khẩu.");
        if (moi.length < 6)
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        if (Arrays.equals(cu, moi))
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        if (!Arrays.equals(moi, xacNhan))
            throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp.");
    }

    public boolean doiMatKhau(String maNV, String matKhauCu, String matKhauMoi) throws SQLException {
        return new NhanVienDAO().doiMatKhau(maNV, matKhauCu, matKhauMoi);
    }
}
