package dao;

import entity.PhieuDichVu;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import ConnectDB.connectDB;

public class PhieuDichVuDAO {

    public ArrayList<PhieuDichVu> getAll() {
        ArrayList<PhieuDichVu> list = new ArrayList<>();
        String sql = "SELECT p.*, tc.tenThuCung, tc.giong, nv.hoTen AS tenNhanVien, kh.hoTen AS tenKhachHang, " +
                "ISNULL((SELECT STRING_AGG(dv.tenDichVu, ', ') FROM ChiTietDichVu ctdv JOIN DichVu dv ON ctdv.maDichVu = dv.maDichVu WHERE ctdv.maPhieuDichVu = p.maPhieuDichVu), N'') AS cacDichVu "
                +
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
                pdv.setTrangThai(rs.getBoolean("trangThai") ? "Hoàn thành" : "Đang xử lý");
                pdv.setMaKhachHang(rs.getString("maKhachHang"));
                pdv.setMaThuCung(rs.getString("maThuCung"));
                pdv.setMaNhanVien(rs.getString("maNhanVien"));
                pdv.setMaLichHen(rs.getString("maLichHen") != null ? rs.getString("maLichHen") : "Không có");
                pdv.setTenThuCung(rs.getString("tenThuCung") + " (" + rs.getString("giong") + ")");
                pdv.setTenNhanVien(rs.getString("tenNhanVien"));
                pdv.setTenKhachHang(rs.getString("tenKhachHang"));
                pdv.setCacDichVu(rs.getString("cacDichVu")); // Thêm lấy tên dịch vụ
                list.add(pdv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy toàn bộ dịch vụ để hiển thị lên Form Checkbox
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
        }
        return list;
    }

    // Lấy các dịch vụ đã chọn của 1 phiếu cụ thể
    public ArrayList<String> getDichVuCuaPhieu(String maPhieu) {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT maDichVu FROM ChiTietDichVu WHERE maPhieuDichVu = ?";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(rs.getString(1));
        } catch (SQLException e) {
        }
        return list;
    }

    // Lưu phiếu và các dịch vụ đã tick
    public boolean them(PhieuDichVu p, List<String[]> dichVuDuocChon) {
        String sql1 = "INSERT INTO PhieuDichVu (maPhieuDichVu, trangThai, maKhachHang, maThuCung, maNhanVien, maLichHen) VALUES (?, ?, ?, ?, ?, ?)";
        String sql2 = "INSERT INTO ChiTietDichVu (maPhieuDichVu, maDichVu, soLuong, donGia, ghiChu) VALUES (?, ?, 1, ?, N'')";
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql1)) {
                ps.setString(1, p.getMaPhieuDichVu());
                ps.setBoolean(2, p.getTrangThai().equals("Hoàn thành"));
                ps.setString(3, p.getMaKhachHang());
                ps.setString(4, p.getMaThuCung());
                ps.setString(5, p.getMaNhanVien());
                if (p.getMaLichHen() == null || p.getMaLichHen().equals("Không có"))
                    ps.setNull(6, Types.VARCHAR);
                else
                    ps.setString(6, p.getMaLichHen());
                ps.executeUpdate();
            }
            if (dichVuDuocChon != null && !dichVuDuocChon.isEmpty()) {
                try (PreparedStatement ps2 = con.prepareStatement(sql2)) {
                    for (String[] dv : dichVuDuocChon) {
                        ps2.setString(1, p.getMaPhieuDichVu());
                        ps2.setString(2, dv[0]);
                        ps2.setDouble(3, Double.parseDouble(dv[2]));
                        ps2.addBatch();
                    }
                    ps2.executeBatch();
                }
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật phiếu và cập nhật lại các dịch vụ đã tick
    public boolean sua(PhieuDichVu p, List<String[]> dichVuDuocChon) {
        String sql1 = "UPDATE PhieuDichVu SET trangThai=?, maKhachHang=?, maThuCung=?, maNhanVien=?, maLichHen=? WHERE maPhieuDichVu=?";
        String sqlDel = "DELETE FROM ChiTietDichVu WHERE maPhieuDichVu=?";
        String sql2 = "INSERT INTO ChiTietDichVu (maPhieuDichVu, maDichVu, soLuong, donGia, ghiChu) VALUES (?, ?, 1, ?, N'')";
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql1)) {
                ps.setBoolean(1, p.getTrangThai().equals("Hoàn thành"));
                ps.setString(2, p.getMaKhachHang());
                ps.setString(3, p.getMaThuCung());
                ps.setString(4, p.getMaNhanVien());
                if (p.getMaLichHen() == null || p.getMaLichHen().equals("Không có"))
                    ps.setNull(5, Types.VARCHAR);
                else
                    ps.setString(5, p.getMaLichHen());
                ps.setString(6, p.getMaPhieuDichVu());
                ps.executeUpdate();
            }
            try (PreparedStatement psDel = con.prepareStatement(sqlDel)) {
                psDel.setString(1, p.getMaPhieuDichVu());
                psDel.executeUpdate();
            }
            if (dichVuDuocChon != null && !dichVuDuocChon.isEmpty()) {
                try (PreparedStatement ps2 = con.prepareStatement(sql2)) {
                    for (String[] dv : dichVuDuocChon) {
                        ps2.setString(1, p.getMaPhieuDichVu());
                        ps2.setString(2, dv[0]);
                        ps2.setDouble(3, Double.parseDouble(dv[2]));
                        ps2.addBatch();
                    }
                    ps2.executeBatch();
                }
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String maPhieu) {
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