package dao;

import ConnectDB.connectDB;
import control.PhanQuyenControl;
import entity.ThuCung;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ThuCungDAO {
    public static class HoSoThuCung {
        public final ThuCung thuCung;
        public final String tenKhachHang;
        public final String soDienThoai;
        public final boolean coLichHen;

        public HoSoThuCung(ThuCung thuCung, String tenKhachHang,
                String soDienThoai, boolean coLichHen) {
            this.thuCung = thuCung;
            this.tenKhachHang = tenKhachHang;
            this.soDienThoai = soDienThoai;
            this.coLichHen = coLichHen;
        }
    }

    public static class ChuNuoi {
        public final String maKhachHang;
        public final String hoTen;
        public final String soDienThoai;

        public ChuNuoi(String maKhachHang, String hoTen, String soDienThoai) {
            this.maKhachHang = maKhachHang;
            this.hoTen = hoTen;
            this.soDienThoai = soDienThoai;
        }

        @Override
        public String toString() {
            return maKhachHang + " - " + hoTen + " - " + soDienThoai;
        }
    }

    public ArrayList<HoSoThuCung> getAllChiTiet() {
        ArrayList<HoSoThuCung> danhSach = new ArrayList<>();
        String sql = "SELECT t.*, k.hoTen AS tenKhachHang, k.soDienThoai, "
                + "CASE WHEN EXISTS (SELECT 1 FROM LichHen lh "
                + "WHERE lh.maThuCung = t.maThuCung "
                + "AND lh.ngayGioHen >= GETDATE() "
                + "AND ISNULL(lh.trangThai, N'Xác nhận') "
                + "NOT IN (N'Đã hủy', N'Đã huỷ')) THEN 1 ELSE 0 END AS coLichHen "
                + "FROM ThuCung t "
                + "LEFT JOIN KhachHang k ON k.maKhachHang = t.maKhachHang "
                + "ORDER BY t.maThuCung DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ThuCung thuCung = new ThuCung(
                        rs.getString("maThuCung"),
                        rs.getString("tenThuCung"),
                        rs.getString("loai"),
                        rs.getString("giong"),
                        rs.getString("gioiTinh"),
                        rs.getDouble("canNang"),
                        rs.getString("maKhachHang"),
                        tenTrangThai(rs.getString("trangThai")));
                danhSach.add(new HoSoThuCung(thuCung, rs.getString("tenKhachHang"),
                        rs.getString("soDienThoai"), rs.getBoolean("coLichHen")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return danhSach;
    }

    public ArrayList<ThuCung> getAll() {
        ArrayList<ThuCung> danhSach = new ArrayList<>();
        for (HoSoThuCung hoSo : getAllChiTiet())
            danhSach.add(hoSo.thuCung);
        return danhSach;
    }

    public ArrayList<ChuNuoi> getDanhSachChuNuoi() {
        ArrayList<ChuNuoi> danhSach = new ArrayList<>();
        String sql = "SELECT maKhachHang, hoTen, soDienThoai FROM KhachHang "
                + "ORDER BY hoTen, maKhachHang";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(new ChuNuoi(rs.getString("maKhachHang"),
                        rs.getString("hoTen"), rs.getString("soDienThoai")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return danhSach;
    }

    public boolean them(ThuCung thuCung) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.THU_CUNG);
        String sql = "INSERT INTO ThuCung (maThuCung, tenThuCung, loai, giong, "
                + "gioiTinh, canNang, maKhachHang, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, thuCung.getMaThuCung());
            ps.setString(2, thuCung.getTenThuCung());
            ps.setString(3, thuCung.getLoai());
            ps.setString(4, thuCung.getGiong());
            ps.setString(5, thuCung.getGioiTinh());
            ps.setDouble(6, thuCung.getCanNang());
            ps.setString(7, thuCung.getMaKhachHang());
            ps.setString(8, tenTrangThai(thuCung.getTrangThai()));
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean sua(ThuCung thuCung) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.THU_CUNG);
        String sql = "UPDATE ThuCung SET tenThuCung = ?, loai = ?, giong = ?, gioiTinh = ?, "
                + "canNang = ?, maKhachHang = ?, trangThai = ? "
                + "WHERE maThuCung = ? AND (maKhachHang = ? OR ("
                + "NOT EXISTS (SELECT 1 FROM LichHen WHERE maThuCung = ?) AND "
                + "NOT EXISTS (SELECT 1 FROM PhieuDichVu WHERE maThuCung = ?) AND "
                + "NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maThuCung = ?)))";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, thuCung.getTenThuCung());
            ps.setString(2, thuCung.getLoai());
            ps.setString(3, thuCung.getGiong());
            ps.setString(4, thuCung.getGioiTinh());
            ps.setDouble(5, thuCung.getCanNang());
            ps.setString(6, thuCung.getMaKhachHang());
            ps.setString(7, tenTrangThai(thuCung.getTrangThai()));
            ps.setString(8, thuCung.getMaThuCung());
            ps.setString(9, thuCung.getMaKhachHang());
            ps.setString(10, thuCung.getMaThuCung());
            ps.setString(11, thuCung.getMaThuCung());
            ps.setString(12, thuCung.getMaThuCung());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean daCoLichSu(String maThuCung) {
        String sql = "SELECT CASE WHEN EXISTS "
                + "(SELECT 1 FROM LichHen WHERE maThuCung = ?) OR EXISTS "
                + "(SELECT 1 FROM PhieuDichVu WHERE maThuCung = ?) OR EXISTS "
                + "(SELECT 1 FROM ChiTietHoaDon WHERE maThuCung = ?) "
                + "THEN 1 ELSE 0 END";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maThuCung);
            ps.setString(2, maThuCung);
            ps.setString(3, maThuCung);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return true;
        }
    }

    public boolean xoa(String maThuCung) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.THU_CUNG);
        String sql = "DELETE FROM ThuCung WHERE maThuCung = ?";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maThuCung);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String taoMaThuCungTuDong() {
        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maThuCung, 3, 8))), 0) "
                + "FROM ThuCung WHERE maThuCung LIKE 'TC[0-9][0-9][0-9]%'";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            int maTiepTheo = rs.getInt(1) + 1;
            if (maTiepTheo > 999)
                throw new IllegalStateException("Đã sử dụng hết mã thú cưng TC001–TC999.");
            return String.format("TC%03d", maTiepTheo);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tạo mã thú cưng.", ex);
        }
    }

    private static String tenTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()
                || "Đang hoạt động".equalsIgnoreCase(trangThai))
            return "Đang theo dõi";
        if ("Không hoạt động".equalsIgnoreCase(trangThai))
            return "Ngừng theo dõi";
        return trangThai;
    }
}
