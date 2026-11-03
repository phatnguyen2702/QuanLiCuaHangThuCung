package control;

import ConnectDB.connectDB;
import dao.HoaDonDAO;
import dao.LichHenDAO;

import dao.NhanVienDAO;
import entity.LichHen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Tầng xử lý dữ liệu và quy tắc của Trang chủ; không tạo hay điều khiển JPanel.
 */
public final class TrangChuControl {
    public void kiemTraPhien(String maNV, String vaiTro) {
        if (maNV == null || maNV.isBlank()
                || PhanQuyenControl.vaiTro(vaiTro) == PhanQuyenControl.VaiTro.KHONG_HOP_LE)
            throw new SecurityException("Chưa đăng nhập hoặc vai trò không hợp lệ.");
    }

    public record ThongKeHoaDon(java.math.BigDecimal doanhThuHomNay, int hoaDonHomNay) {
        public java.math.BigDecimal getDoanhThuHomNay() {
            return doanhThuHomNay;
        }

        public int getHoaDonHomNay() {
            return hoaDonHomNay;
        }
    }

    public ThongKeHoaDon thongKeHoaDon() throws SQLException {
        HoaDonDAO.ThongKe kq = new HoaDonDAO().layThongKe();
        return new ThongKeHoaDon(kq.getDoanhThuHomNay(), kq.getHoaDonHomNay());
    }

    public int demLichSpaHomNay() throws SQLException {
        String sql = "SELECT COUNT(*) FROM LichHen WHERE ngayGioHen >= ? AND ngayGioHen < ? "
                + "AND ISNULL(trangThai, N'Xác nhận') <> N'Đã hủy'";
        LocalDate homNay = LocalDate.now();
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(homNay.atStartOfDay()));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(homNay.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int demPhieuNhapHomNay() throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhieuNhapKho WHERE CAST(ngayNhap AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
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
