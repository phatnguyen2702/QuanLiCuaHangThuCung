package dao;

import entity.LichHen;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import ConnectDB.connectDB;

public class LichHenDAO {

    public ArrayList<String> getDanhSachDichVu() {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT tenDichVu FROM DichVu ORDER BY tenDichVu";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================
    // LẤY TẤT CẢ LỊCH HẸN (Kèm thông tin JOIN)
    // =========================================================
    public ArrayList<LichHen> getAll() {
        ArrayList<LichHen> list = new ArrayList<>();

        String sql = "SELECT lh.maLichHen, lh.ngayGioHen, lh.ghiChu, " +
                "lh.maKhachHang, kh.hoTen as tenKhachHang, " +
                "lh.maThuCung, tc.tenThuCung, tc.giong, " +
                "lh.maNhanVien, nv.hoTen as tenNhanVien " +
                "FROM LichHen lh " +
                "INNER JOIN KhachHang kh ON lh.maKhachHang = kh.maKhachHang " +
                "INNER JOIN ThuCung tc ON lh.maThuCung = tc.maThuCung " +
                "INNER JOIN NhanVien nv ON lh.maNhanVien = nv.maNhanVien " +
                "ORDER BY lh.ngayGioHen DESC";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LichHen lh = new LichHen();
                lh.setMaLichHen(rs.getString("maLichHen"));

                Timestamp ts = rs.getTimestamp("ngayGioHen");
                if (ts != null) {
                    lh.setNgayGioHen(ts.toLocalDateTime());
                }

                lh.setGhiChu(rs.getString("ghiChu"));
                lh.setMaKhachHang(rs.getString("maKhachHang"));
                lh.setTenKhachHang(rs.getString("tenKhachHang"));
                lh.setMaThuCung(rs.getString("maThuCung"));

                String tenThuCungDayDu = rs.getString("tenThuCung") + " (" + rs.getString("giong") + ")";
                lh.setTenThuCung(tenThuCungDayDu);

                lh.setMaNhanVien(rs.getString("maNhanVien"));
                lh.setTenNhanVien(rs.getString("tenNhanVien"));

                // Mặc định do DB không có cột trangThai
                lh.setTrangThai("Xác nhận");

                list.add(lh);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================
    // XÓA LỊCH HẸN
    // =========================================================
    public boolean xoa(String maLichHen) {
        String sql = "DELETE FROM LichHen WHERE maLichHen = ?";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLichHen);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // THÊM LỊCH HẸN
    // =========================================================
    public boolean them(LichHen lh) {
        String sql = "INSERT INTO LichHen (maLichHen, ngayGioHen, ghiChu, maKhachHang, maNhanVien, maThuCung) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lh.getMaLichHen());
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(lh.getNgayGioHen()));
            ps.setString(3, lh.getGhiChu());
            ps.setString(4, lh.getMaKhachHang());
            ps.setString(5, lh.getMaNhanVien());
            ps.setString(6, lh.getMaThuCung());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // SỬA LỊCH HẸN
    // =========================================================
    public boolean sua(LichHen lh) {
        String sql = "UPDATE LichHen SET ngayGioHen = ?, ghiChu = ?, maKhachHang = ?, maNhanVien = ?, maThuCung = ? WHERE maLichHen = ?";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(lh.getNgayGioHen()));
            ps.setString(2, lh.getGhiChu());
            ps.setString(3, lh.getMaKhachHang());
            ps.setString(4, lh.getMaNhanVien());
            ps.setString(5, lh.getMaThuCung());
            ps.setString(6, lh.getMaLichHen());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Các hàm lấy danh sách cho ComboBox
    public ArrayList<String[]> getDanhSachKhachHang() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT maKhachHang, hoTen FROM KhachHang";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[] { rs.getString(1), rs.getString(2) });
            }
        } catch (SQLException e) {
        }
        return list;
    }

    public ArrayList<String[]> getDanhSachNhanVienSpa() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT maNhanVien, hoTen FROM NhanVien WHERE vaiTro = N'Nhan vien Spa'";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[] { rs.getString(1), rs.getString(2) });
            }
        } catch (SQLException e) {
        }
        return list;
    }

    public ArrayList<String[]> getDanhSachThuCung() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT maThuCung, tenThuCung FROM ThuCung";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[] { rs.getString(1), rs.getString(2) });
            }
        } catch (SQLException e) {
        }
        return list;
    }
}
