package dao;

import ConnectDB.connectDB;
import control.PhanQuyenControl;
import entity.LichHen;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class LichHenDAO {

    public ArrayList<String> getDanhSachDichVu() {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT tenDichVu FROM DichVu ORDER BY tenDichVu";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(rs.getString(1));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public ArrayList<LichHen> getAll() {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.LICH_HEN_SPA);
        ArrayList<LichHen> list = new ArrayList<>();
        String sql = "SELECT lh.*, kh.hoTen AS tenKhachHang, tc.tenThuCung, tc.giong, "
                + "nv.hoTen AS tenNhanVien "
                + "FROM LichHen lh "
                + "JOIN KhachHang kh ON lh.maKhachHang = kh.maKhachHang "
                + "JOIN ThuCung tc ON lh.maThuCung = tc.maThuCung "
                + "JOIN NhanVien nv ON lh.maNhanVien = nv.maNhanVien "
                + "ORDER BY lh.ngayGioHen DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LichHen lichHen = new LichHen();
                lichHen.setMaLichHen(rs.getString("maLichHen"));
                Timestamp ngayGioHen = rs.getTimestamp("ngayGioHen");
                if (ngayGioHen != null)
                    lichHen.setNgayGioHen(ngayGioHen.toLocalDateTime());
                lichHen.setGhiChu(rs.getString("ghiChu"));
                lichHen.setMaKhachHang(rs.getString("maKhachHang"));
                lichHen.setTenKhachHang(rs.getString("tenKhachHang"));
                lichHen.setMaThuCung(rs.getString("maThuCung"));
                lichHen.setTenThuCung(rs.getString("tenThuCung") + " (" + rs.getString("giong") + ")");
                lichHen.setMaNhanVien(rs.getString("maNhanVien"));
                lichHen.setTenNhanVien(rs.getString("tenNhanVien"));
                String trangThai = rs.getString("trangThai");
                lichHen.setTrangThai(trangThai == null ? "Xác nhận" : trangThai);
                list.add(lichHen);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public LichHen timTheoMa(String ma) {
        if (ma == null || ma.isBlank())
            return null;
        for (LichHen lichHen : getAll()) {
            if (ma.equals(lichHen.getMaLichHen()))
                return lichHen;
        }
        return null;
    }

    public boolean huy(String maLichHen) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.LICH_HEN_SPA);
        String sql = "UPDATE LichHen SET trangThai = N'Đã hủy' "
                + "WHERE maLichHen = ? "
                + "AND ISNULL(trangThai, N'Xác nhận') <> N'Đã hủy' "
                + "AND NOT EXISTS (SELECT 1 FROM PhieuDichVu "
                + "WHERE maLichHen = LichHen.maLichHen)";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLichHen);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String maLichHen) {
        return huy(maLichHen);
    }

    public boolean them(LichHen lichHen) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.LICH_HEN_SPA);
        String sql = "INSERT INTO LichHen "
                + "(maLichHen, ngayGioHen, ghiChu, maKhachHang, maNhanVien, maThuCung, trangThai) "
                + "SELECT ?, ?, ?, ?, ?, ?, ? "
                + "WHERE ? > SYSDATETIME() "
                + "AND EXISTS (SELECT 1 FROM ThuCung "
                + "WHERE maThuCung = ? AND maKhachHang = ?) "
                + "AND NOT EXISTS (SELECT 1 FROM LichHen WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE maNhanVien = ? AND ISNULL(trangThai, N'Xác nhận') <> N'Đã hủy' "
                + "AND maLichHen <> ? "
                + "AND ngayGioHen > DATEADD(MINUTE, -60, ?) "
                + "AND ngayGioHen < DATEADD(MINUTE, 60, ?))";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lichHen.getMaLichHen());
            ps.setTimestamp(2, Timestamp.valueOf(lichHen.getNgayGioHen()));
            ps.setString(3, lichHen.getGhiChu());
            ps.setString(4, lichHen.getMaKhachHang());
            ps.setString(5, lichHen.getMaNhanVien());
            ps.setString(6, lichHen.getMaThuCung());
            ps.setString(7, lichHen.getTrangThai());
            Timestamp ngayGioHen = Timestamp.valueOf(lichHen.getNgayGioHen());
            ps.setTimestamp(8, ngayGioHen);
            ps.setString(9, lichHen.getMaThuCung());
            ps.setString(10, lichHen.getMaKhachHang());
            ps.setString(11, lichHen.getMaNhanVien());
            ps.setString(12, lichHen.getMaLichHen());
            ps.setTimestamp(13, ngayGioHen);
            ps.setTimestamp(14, ngayGioHen);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean sua(LichHen lichHen) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.LICH_HEN_SPA);
        String sql = "UPDATE LichHen SET ngayGioHen = ?, ghiChu = ?, maKhachHang = ?, "
                + "maNhanVien = ?, maThuCung = ? "
                + "WHERE maLichHen = ? "
                + "AND ISNULL(trangThai, N'Xác nhận') <> N'Đã hủy' "
                + "AND ? > SYSDATETIME() "
                + "AND EXISTS (SELECT 1 FROM ThuCung WHERE maThuCung = ? AND maKhachHang = ?) "
                + "AND NOT EXISTS (SELECT 1 FROM LichHen WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE maNhanVien = ? AND ISNULL(trangThai, N'Xác nhận') <> N'Đã hủy' "
                + "AND maLichHen <> ? "
                + "AND ngayGioHen > DATEADD(MINUTE, -60, ?) "
                + "AND ngayGioHen < DATEADD(MINUTE, 60, ?))";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            Timestamp ngayGioHen = Timestamp.valueOf(lichHen.getNgayGioHen());
            ps.setTimestamp(1, ngayGioHen);
            ps.setString(2, lichHen.getGhiChu());
            ps.setString(3, lichHen.getMaKhachHang());
            ps.setString(4, lichHen.getMaNhanVien());
            ps.setString(5, lichHen.getMaThuCung());
            ps.setString(6, lichHen.getMaLichHen());
            ps.setTimestamp(7, ngayGioHen);
            ps.setString(8, lichHen.getMaThuCung());
            ps.setString(9, lichHen.getMaKhachHang());
            ps.setString(10, lichHen.getMaNhanVien());
            ps.setString(11, lichHen.getMaLichHen());
            ps.setTimestamp(12, ngayGioHen);
            ps.setTimestamp(13, ngayGioHen);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public ArrayList<String[]> getDanhSachKhachHang() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT maKhachHang, hoTen FROM KhachHang ORDER BY hoTen";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new String[] { rs.getString(1), rs.getString(2) });
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public ArrayList<String[]> getDanhSachNhanVienSpa() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT maNhanVien, hoTen FROM NhanVien "
                + "WHERE vaiTro = N'Nhan vien Spa' ORDER BY hoTen";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new String[] { rs.getString(1), rs.getString(2) });
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public ArrayList<String[]> getDanhSachThuCungTheoKhachHang(String maKhachHang) {
        ArrayList<String[]> list = new ArrayList<>();
        if (maKhachHang == null || maKhachHang.isBlank())
            return list;

        String sql = "SELECT maThuCung, tenThuCung FROM ThuCung "
                + "WHERE maKhachHang = ? ORDER BY tenThuCung";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new String[] { rs.getString(1), rs.getString(2) });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public ArrayList<String[]> getDanhSachThuCung() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT maThuCung, tenThuCung FROM ThuCung ORDER BY tenThuCung";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new String[] { rs.getString(1), rs.getString(2) });
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public String taoMaTuDong() {
        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maLichHen, 3, 8))), 0) "
                + "FROM LichHen WHERE maLichHen LIKE 'LH[0-9][0-9][0-9]%'";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format("LH%03d", rs.getInt(1) + 1);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tạo mã lịch hẹn.", ex);
        }
    }
}
