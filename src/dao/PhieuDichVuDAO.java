package dao;

import ConnectDB.connectDB;
import control.PhanQuyenControl;
import entity.PhieuDichVu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PhieuDichVuDAO {

    public ArrayList<PhieuDichVu> getAll() {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.PHIEU_DICH_VU);
        ArrayList<PhieuDichVu> list = new ArrayList<>();
        String sql = "SELECT p.*, tc.tenThuCung, tc.giong, " +
                "nv.hoTen AS tenNhanVien, kh.hoTen AS tenKhachHang, " +
                "ISNULL((SELECT STRING_AGG(CONVERT(NVARCHAR(MAX), dv.tenDichVu), N', ') " +
                "FROM ChiTietDichVu ctdv JOIN DichVu dv " +
                "ON ctdv.maDichVu = dv.maDichVu " +
                "WHERE ctdv.maPhieuDichVu = p.maPhieuDichVu), N'') AS cacDichVu " +
                "FROM PhieuDichVu p " +
                "JOIN ThuCung tc ON p.maThuCung = tc.maThuCung " +
                "JOIN NhanVien nv ON p.maNhanVien = nv.maNhanVien " +
                "JOIN KhachHang kh ON p.maKhachHang = kh.maKhachHang " +
                "ORDER BY p.maPhieuDichVu DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PhieuDichVu pdv = new PhieuDichVu();
                pdv.setMaPhieuDichVu(rs.getString("maPhieuDichVu"));
                pdv.setTrangThai(rs.getBoolean("trangThai") ? "Đã thanh toán" : "Chưa thanh toán");
                pdv.setMaKhachHang(rs.getString("maKhachHang"));
                pdv.setMaThuCung(rs.getString("maThuCung"));
                pdv.setMaNhanVien(rs.getString("maNhanVien"));
                pdv.setMaLichHen(rs.getString("maLichHen") != null ? rs.getString("maLichHen") : "Không có");
                pdv.setTenThuCung(rs.getString("tenThuCung") + " (" + rs.getString("giong") + ")");
                pdv.setTenNhanVien(rs.getString("tenNhanVien"));
                pdv.setTenKhachHang(rs.getString("tenKhachHang"));
                pdv.setCacDichVu(rs.getString("cacDichVu"));
                list.add(pdv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<String[]> getDanhSachDichVu() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT maDichVu, tenDichVu, giaDichVu FROM DichVu ORDER BY tenDichVu";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[] { rs.getString(1), rs.getString(2), rs.getString(3) });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<String> getDichVuCuaPhieu(String maPhieu) {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT maDichVu FROM ChiTietDichVu WHERE maPhieuDichVu = ?";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    private void kiemTraLienKet(Connection con, PhieuDichVu p) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM ThuCung WHERE maThuCung = ? AND maKhachHang = ?")) {
            ps.setString(1, p.getMaThuCung());
            ps.setString(2, p.getMaKhachHang());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    throw new SQLException("Thú cưng không thuộc khách hàng đã chọn.");
            }
        }
        if (p.getMaLichHen() == null || "Không có".equals(p.getMaLichHen()))
            return;
        String sql = "SELECT 1 FROM LichHen WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE maLichHen = ? AND maKhachHang = ? AND maThuCung = ? "
                + "AND maNhanVien = ? AND ISNULL(trangThai, N'Xác nhận') <> N'Đã hủy' "
                + "AND (ngayGioHen >= GETDATE() OR EXISTS ("
                + "SELECT 1 FROM PhieuDichVu pdv "
                + "WHERE pdv.maLichHen = LichHen.maLichHen "
                + "AND pdv.maPhieuDichVu = ?))";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getMaLichHen());
            ps.setString(2, p.getMaKhachHang());
            ps.setString(3, p.getMaThuCung());
            ps.setString(4, p.getMaNhanVien());
            ps.setString(5, p.getMaPhieuDichVu());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    throw new SQLException("Lịch hẹn đã qua giờ, đã hủy hoặc không khớp thông tin phiếu.");
            }
        }
        String sqlTrung = "SELECT 1 FROM PhieuDichVu WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE maLichHen = ? AND maPhieuDichVu <> ?";
        try (PreparedStatement ps = con.prepareStatement(sqlTrung)) {
            ps.setString(1, p.getMaLichHen());
            ps.setString(2, p.getMaPhieuDichVu());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    throw new SQLException("Lịch hẹn này đã có phiếu dịch vụ khác.");
            }
        }
    }


    private void chenDichVu(Connection con, String maPhieu, List<String[]> ds) throws SQLException {
        if (ds == null || ds.isEmpty())
            throw new SQLException("Phiếu phải có ít nhất một dịch vụ.");
        String sql = "INSERT INTO ChiTietDichVu (maPhieuDichVu, maDichVu, soLuong, donGia, ghiChu) "
                + "VALUES (?, ?, 1, ?, N'')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String[] dv : ds) {
                ps.setString(1, maPhieu);
                ps.setString(2, dv[0]);
                ps.setBigDecimal(3, new java.math.BigDecimal(dv[2]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void rollback(Connection con, Exception ex) {
        try {
            con.rollback();
        } catch (SQLException rb) {
            ex.addSuppressed(rb);
        }
    }

    public boolean them(PhieuDichVu p, List<String[]> dichVuDuocChon) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.PHIEU_DICH_VU);
        String sql = "INSERT INTO PhieuDichVu "
                + "(maPhieuDichVu, trangThai, maKhachHang, maThuCung, maNhanVien, maLichHen) "
                + "VALUES (?, 0, ?, ?, ?, ?)";
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                kiemTraLienKet(con, p);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, p.getMaPhieuDichVu());
                    ps.setString(2, p.getMaKhachHang());
                    ps.setString(3, p.getMaThuCung());
                    ps.setString(4, p.getMaNhanVien());
                    if (p.getMaLichHen() == null || "Không có".equals(p.getMaLichHen()))
                        ps.setNull(5, Types.VARCHAR);
                    else
                        ps.setString(5, p.getMaLichHen());
                    ps.executeUpdate();
                }
                chenDichVu(con, p.getMaPhieuDichVu(), dichVuDuocChon);
                con.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean sua(PhieuDichVu p, List<String[]> dichVuDuocChon) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.PHIEU_DICH_VU);
        String sql = "UPDATE PhieuDichVu SET maKhachHang=?, maThuCung=?, maNhanVien=?, maLichHen=? "
                + "WHERE maPhieuDichVu=? AND trangThai=0";
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                kiemTraLienKet(con, p);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, p.getMaKhachHang());
                    ps.setString(2, p.getMaThuCung());
                    ps.setString(3, p.getMaNhanVien());
                    if (p.getMaLichHen() == null || "Không có".equals(p.getMaLichHen()))
                        ps.setNull(4, Types.VARCHAR);
                    else
                        ps.setString(4, p.getMaLichHen());
                    ps.setString(5, p.getMaPhieuDichVu());
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Phiếu không tồn tại hoặc đã thanh toán.");
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM ChiTietDichVu WHERE maPhieuDichVu=?")) {
                    ps.setString(1, p.getMaPhieuDichVu());
                    ps.executeUpdate();
                }
                chenDichVu(con, p.getMaPhieuDichVu(), dichVuDuocChon);
                con.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String maPhieu) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.PHIEU_DICH_VU);
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT trangThai FROM PhieuDichVu WITH (UPDLOCK, HOLDLOCK) WHERE maPhieuDichVu=?")) {
                    ps.setString(1, maPhieu);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getBoolean(1)) {
                            con.rollback();
                            return false;
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM ChiTietDichVu WHERE maPhieuDichVu=?")) {
                    ps.setString(1, maPhieu);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM PhieuDichVu WHERE maPhieuDichVu=? AND trangThai=0")) {
                    ps.setString(1, maPhieu);
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Không xóa được phiếu.");
                }
                con.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String taoMaTuDong() {
        int max = 0;
        for (PhieuDichVu p : getAll()) {
            if (p.getMaPhieuDichVu().startsWith("PDV")) {
                try {
                    int num = Integer.parseInt(p.getMaPhieuDichVu().substring(3));
                    if (num > max)
                        max = num;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("PDV%03d", max + 1);
    }

    public ArrayList<String> getDanhSachLichHen(String maPhieuDangSua) {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT lh.maLichHen FROM LichHen lh "
                + "WHERE ISNULL(lh.trangThai, N'Xác nhận') <> N'Đã hủy' "
                + "AND (lh.ngayGioHen >= GETDATE() OR EXISTS ("
                + "SELECT 1 FROM PhieuDichVu px "
                + "WHERE px.maLichHen = lh.maLichHen AND px.maPhieuDichVu = ?)) "
                + "AND NOT EXISTS (SELECT 1 FROM PhieuDichVu p "
                + "WHERE p.maLichHen = lh.maLichHen "
                + "AND (? IS NULL OR p.maPhieuDichVu <> ?)) "
                + "ORDER BY lh.ngayGioHen ASC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            if (maPhieuDangSua == null) {
                ps.setNull(1, Types.VARCHAR);
                ps.setNull(2, Types.VARCHAR);
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(1, maPhieuDangSua);
                ps.setString(2, maPhieuDangSua);
                ps.setString(3, maPhieuDangSua);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public ArrayList<String> getDanhSachLichHen() {
        return getDanhSachLichHen(null);
    }
}
