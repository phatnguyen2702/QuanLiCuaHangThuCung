package dao;

import entity.ThuCung;
import java.sql.*;
import java.util.ArrayList;
import ConnectDB.connectDB;

public class ThuCungDAO {

    public ArrayList<ThuCung> getAll() {
        ArrayList<ThuCung> list = new ArrayList<>();
        String sql = "SELECT * FROM ThuCung ORDER BY maThuCung DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ThuCung(
                        rs.getString("maThuCung"), rs.getString("tenThuCung"),
                        rs.getString("loai"), rs.getString("giong"),
                        rs.getString("gioiTinh"), rs.getDouble("canNang"),
                        rs.getString("maKhachHang"),
                        rs.getString("trangThai") != null ? rs.getString("trangThai") : "Đang hoạt động"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean them(ThuCung tc) {
        String sql = "INSERT INTO ThuCung (maThuCung, tenThuCung, loai, giong, gioiTinh, canNang, maKhachHang, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tc.getMaThuCung());
            ps.setString(2, tc.getTenThuCung());
            ps.setString(3, tc.getLoai());
            ps.setString(4, tc.getGiong());
            ps.setString(5, tc.getGioiTinh());
            ps.setDouble(6, tc.getCanNang());
            ps.setString(7, tc.getMaKhachHang());
            ps.setString(8, tc.getTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sua(ThuCung tc) {
        String sql = "UPDATE ThuCung SET tenThuCung=?, loai=?, giong=?, gioiTinh=?, canNang=?, maKhachHang=?, trangThai=? WHERE maThuCung=?";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tc.getTenThuCung());
            ps.setString(2, tc.getLoai());
            ps.setString(3, tc.getGiong());
            ps.setString(4, tc.getGioiTinh());
            ps.setDouble(5, tc.getCanNang());
            ps.setString(6, tc.getMaKhachHang());
            ps.setString(7, tc.getTrangThai());
            ps.setString(8, tc.getMaThuCung());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String maThuCung) {
        String sql = "DELETE FROM ThuCung WHERE maThuCung = ?";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maThuCung);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false; // Lỗi khóa ngoại (đang có lịch hẹn/hóa đơn)
        }
    }

    public String taoMaThuCungTuDong() {
        int max = 0;
        for (ThuCung tc : getAll()) {
            if (tc.getMaThuCung().startsWith("TC")) {
                try {
                    int num = Integer.parseInt(tc.getMaThuCung().substring(2));
                    if (num > max)
                        max = num;
                } catch (Exception e) {
                }
            }
        }
        return String.format("TC%04d", max + 1); // Sinh mã dạng TC0001, TC0002...
    }
}