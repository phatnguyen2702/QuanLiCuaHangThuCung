package dao;

import ConnectDB.connectDB;
import entity.ThuCung;
import java.sql.*;
import java.util.ArrayList;

public class ThuCungDAO {

    public static class HoSoThuCung {
        public final ThuCung thuCung;
        public final String tenKhachHang;
        public final String soDienThoai;
        /** Đúng khi thú cưng có lịch hẹn sắp tới chưa hủy. */
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

    public static String tenTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()
                || "Đang hoạt động".equalsIgnoreCase(trangThai)) {
            return "Đang theo dõi";
        }
        if ("Không hoạt động".equalsIgnoreCase(trangThai)) {
            return "Ngừng theo dõi";
        }
        return trangThai;
    }

    private ThuCung taoThuCung(ResultSet rs) throws SQLException {
        return new ThuCung(rs.getString("maThuCung"), rs.getString("tenThuCung"),
                rs.getString("loai"), rs.getString("giong"), rs.getString("gioiTinh"),
                rs.getDouble("canNang"), rs.getString("maKhachHang"),
                tenTrangThai(rs.getString("trangThai")));
    }

    public ArrayList<HoSoThuCung> getAllChiTiet() {
        ArrayList<HoSoThuCung> list = new ArrayList<>();
        String sql = "SELECT t.maThuCung,t.tenThuCung,t.loai,t.giong,t.gioiTinh,"
                + "t.canNang,t.maKhachHang,t.trangThai,k.hoTen AS tenKhachHang,"
                + "k.soDienThoai, CASE WHEN EXISTS ("
                + "SELECT 1 FROM LichHen lh WHERE lh.maThuCung = t.maThuCung "
                + "AND lh.ngayGioHen >= GETDATE() "
                + "AND ISNULL(lh.trangThai, N'Xác nhận') NOT IN (N'Đã hủy', N'Đã huỷ')"
                + ") THEN 1 ELSE 0 END AS coLichHen FROM ThuCung t "
                + "LEFT JOIN KhachHang k ON k.maKhachHang=t.maKhachHang "
                + "ORDER BY t.maThuCung DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new HoSoThuCung(taoThuCung(rs),
                        rs.getString("tenKhachHang"), rs.getString("soDienThoai"),
                        rs.getInt("coLichHen") == 1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<ThuCung> getAll() {
        ArrayList<ThuCung> list = new ArrayList<>();
        for (HoSoThuCung hs : getAllChiTiet()) {
            list.add(hs.thuCung);
        }
        return list;
    }

    public ArrayList<ChuNuoi> getDanhSachChuNuoi() {
        ArrayList<ChuNuoi> ds = new ArrayList<>();
        String sql = "SELECT maKhachHang, hoTen, soDienThoai FROM KhachHang "
                + "ORDER BY hoTen, maKhachHang";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new ChuNuoi(rs.getString("maKhachHang"), rs.getString("hoTen"),
                        rs.getString("soDienThoai")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean them(ThuCung tc) {
        String sql = "INSERT INTO ThuCung "
                + "(maThuCung,tenThuCung,loai,giong,gioiTinh,canNang,maKhachHang,trangThai) "
                + "VALUES(?,?,?,?,?,?,?,?)";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tc.getMaThuCung());
            ps.setString(2, tc.getTenThuCung());
            ps.setString(3, tc.getLoai());
            ps.setString(4, tc.getGiong());
            ps.setString(5, tc.getGioiTinh());
            ps.setDouble(6, tc.getCanNang());
            ps.setString(7, tc.getMaKhachHang());
            ps.setString(8, tenTrangThai(tc.getTrangThai()));
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Khong cho chuyen chu nuoi neu thu cung da co lich hen, phieu dich vu
     * hoac chi tiet hoa don, tranh khong khop du lieu lich su.
     */
    public boolean daCoLichSu(String maThuCung) {
        String sql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM LichHen WHERE maThuCung=?) "
                + "OR EXISTS (SELECT 1 FROM PhieuDichVu WHERE maThuCung=?) "
                + "OR EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maThuCung=?) "
                + "THEN 1 ELSE 0 END";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++)
                ps.setString(i, maThuCung);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Khong xac nhan duoc tinh trang lich su thi mac dinh khoa.
            return true;
        }
    }

    public boolean sua(ThuCung tc) {
        String sql = "UPDATE ThuCung SET tenThuCung=?, loai=?, giong=?, "
                + "gioiTinh=?, canNang=?, maKhachHang=?, trangThai=? WHERE maThuCung=? "
                + "AND (maKhachHang=? OR ("
                + "NOT EXISTS (SELECT 1 FROM LichHen WHERE maThuCung=?) "
                + "AND NOT EXISTS (SELECT 1 FROM PhieuDichVu WHERE maThuCung=?) "
                + "AND NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maThuCung=?)))";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tc.getTenThuCung());
            ps.setString(2, tc.getLoai());
            ps.setString(3, tc.getGiong());
            ps.setString(4, tc.getGioiTinh());
            ps.setDouble(5, tc.getCanNang());
            ps.setString(6, tc.getMaKhachHang());
            ps.setString(7, tenTrangThai(tc.getTrangThai()));
            ps.setString(8, tc.getMaThuCung());
            ps.setString(9, tc.getMaKhachHang());
            ps.setString(10, tc.getMaThuCung());
            ps.setString(11, tc.getMaThuCung());
            ps.setString(12, tc.getMaThuCung());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String maThuCung) {
        String sql = "DELETE FROM ThuCung WHERE maThuCung=?";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maThuCung);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            // FK to LichHen, PhieuDichVu, ChiTietHoaDon: do not delete history.
            return false;
        }
    }

    public String taoMaThuCungTuDong() {
        int max = 0;
        for (ThuCung tc : getAll()) {
            String ma = tc.getMaThuCung();
            if (ma != null && ma.matches("TC\\d{4,}")) {
                try {
                    max = Math.max(max, Integer.parseInt(ma.substring(2)));
                } catch (NumberFormatException ignore) {
                    // Ignore corrupted identifiers.
                }
            }
        }
        return String.format("TC%04d", max + 1);
    }
}
