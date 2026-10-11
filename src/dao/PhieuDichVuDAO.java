package dao;

import entity.PhieuDichVu;
import java.sql.*;
import java.util.ArrayList;
import ConnectDB.connectDB;

public class PhieuDichVuDAO {

    public ArrayList<PhieuDichVu> getAll() {
        ArrayList<PhieuDichVu> list = new ArrayList<>();
        String sql = "SELECT p.*, tc.tenThuCung, tc.giong, nv.hoTen AS tenNhanVien, kh.hoTen AS tenKhachHang " +
                "FROM PhieuDichVu p " +
                "INNER JOIN ThuCung tc ON p.maThuCung = tc.maThuCung " +
                "INNER JOIN NhanVien nv ON p.maNhanVien = nv.maNhanVien " +
                "INNER JOIN KhachHang kh ON p.maKhachHang = kh.maKhachHang " +
                "ORDER BY p.maPhieuDichVu DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PhieuDichVu pdv = new PhieuDichVu();
                pdv.setMaPhieuDichVu(rs.getString("maPhieuDichVu"));
                // Ánh xạ BIT trong SQL (1/0) thành Chuỗi trên UI
                pdv.setTrangThai(rs.getBoolean("trangThai") ? "Hoàn thành" : "Đang xử lý");
                pdv.setMaKhachHang(rs.getString("maKhachHang"));
                pdv.setMaThuCung(rs.getString("maThuCung"));
                pdv.setMaNhanVien(rs.getString("maNhanVien"));
                pdv.setMaLichHen(rs.getString("maLichHen") != null ? rs.getString("maLichHen") : "Không có");

                pdv.setTenThuCung(rs.getString("tenThuCung") + " (" + rs.getString("giong") + ")");
                pdv.setTenNhanVien(rs.getString("tenNhanVien"));
                pdv.setTenKhachHang(rs.getString("tenKhachHang"));
                list.add(pdv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean them(PhieuDichVu p) {
        String sql = "INSERT INTO PhieuDichVu (maPhieuDichVu, trangThai, maKhachHang, maThuCung, maNhanVien, maLichHen) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getMaPhieuDichVu());
            ps.setBoolean(2, p.getTrangThai().equals("Hoàn thành")); // Ánh xạ ngược lại BIT
            ps.setString(3, p.getMaKhachHang());
            ps.setString(4, p.getMaThuCung());
            ps.setString(5, p.getMaNhanVien());
            if (p.getMaLichHen().equals("Không có"))
                ps.setNull(6, Types.VARCHAR);
            else
                ps.setString(6, p.getMaLichHen());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sua(PhieuDichVu p) {
        String sql = "UPDATE PhieuDichVu SET trangThai=?, maKhachHang=?, maThuCung=?, maNhanVien=?, maLichHen=? WHERE maPhieuDichVu=?";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, p.getTrangThai().equals("Hoàn thành"));
            ps.setString(2, p.getMaKhachHang());
            ps.setString(3, p.getMaThuCung());
            ps.setString(4, p.getMaNhanVien());
            if (p.getMaLichHen().equals("Không có"))
                ps.setNull(5, Types.VARCHAR);
            else
                ps.setString(5, p.getMaLichHen());
            ps.setString(6, p.getMaPhieuDichVu());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String maPhieu) {
        // Xóa bảng con (ChiTietDichVu) trước để tránh lỗi khóa ngoại
        String sqlChiTiet = "DELETE FROM ChiTietDichVu WHERE maPhieuDichVu = ?";
        String sqlPhieu = "DELETE FROM PhieuDichVu WHERE maPhieuDichVu = ?";
        try (Connection con = connectDB.getConnection()) {
            try (PreparedStatement ps1 = con.prepareStatement(sqlChiTiet)) {
                ps1.setString(1, maPhieu);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = con.prepareStatement(sqlPhieu)) {
                ps2.setString(1, maPhieu);
                return ps2.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                } catch (Exception e) {
                }
            }
        }
        return String.format("PDV%03d", max + 1);
    }

    // Các hàm phụ trợ lấy danh sách cho ComboBox
    public ArrayList<String> getDanhSachLichHen() {
        ArrayList<String> list = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT maLichHen FROM LichHen");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(rs.getString(1));
        } catch (SQLException e) {
        }
        return list;
    }
}