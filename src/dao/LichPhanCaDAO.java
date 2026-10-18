package dao;

import entity.LichPhanCa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import ConnectDB.connectDB;

public class LichPhanCaDAO {
    // LAY TAT CA LICH PHAN CA
    public ArrayList<LichPhanCa> getAll() {
        ArrayList<LichPhanCa> list = new ArrayList<>();
        String sql = "SELECT pc.maPhanCa, pc.ngayLamViec, " +
                "pc.maNhanVien, nv.hoTen, " +
                "pc.maCa, c.tenCa, c.gioBatDau, c.gioKetThuc " +
                "FROM LichPhanCa pc " +
                "INNER JOIN NhanVien nv " +
                "ON pc.maNhanVien = nv.maNhanVien " +
                "INNER JOIN CaLam c " +
                "ON pc.maCa = c.maCa " +
                "ORDER BY pc.ngayLamViec DESC, pc.maPhanCa";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LichPhanCa pc = new LichPhanCa();
                pc.setMaPhanCa(
                        rs.getString("maPhanCa"));
                if (rs.getDate("ngayLamViec") != null) {
                    pc.setNgayLamViec(
                            rs.getDate("ngayLamViec").toLocalDate());
                }
                pc.setMaNhanVien(rs.getString("maNhanVien"));
                pc.setHoTen(rs.getString("hoTen"));
                pc.setMaCa(rs.getString("maCa"));
                pc.setTenCa(rs.getString("tenCa"));
                pc.setGioBatDau(formatGio(rs.getString("gioBatDau")));
                pc.setGioKetThuc(formatGio(rs.getString("gioKetThuc")));
                list.add(pc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // THEM PHAN CA
    public boolean them(LichPhanCa pc) {
        String sql = "INSERT INTO LichPhanCa " +
                "(maPhanCa, ngayLamViec, maNhanVien, maCa) " +
                "VALUES (?, ?, ?, ?)";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pc.getMaPhanCa());
            if (pc.getNgayLamViec() != null) {
                ps.setDate(2, java.sql.Date.valueOf(pc.getNgayLamViec()));

            } else {

                ps.setNull(2, java.sql.Types.DATE);
            }
            ps.setString(3, pc.getMaNhanVien());
            ps.setString(4, pc.getMaCa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sửa một phân ca trong SQL Server. Không cho thay đổi phân ca cũ
     * đã qua ngày, cũng không chấp nhận một ngày mới trong quá khứ.
     */
    public boolean sua(LichPhanCa pc) throws SQLException {
        String sql = "UPDATE LichPhanCa " +
                "SET ngayLamViec = ?, maNhanVien = ?, maCa = ? " +
                "WHERE maPhanCa = ? AND ngayLamViec >= CONVERT(date, GETDATE()) " +
                "AND ? >= CONVERT(date, GETDATE())";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(pc.getNgayLamViec()));
            ps.setString(2, pc.getMaNhanVien());
            ps.setString(3, pc.getMaCa());
            ps.setString(4, pc.getMaPhanCa());
            ps.setDate(5, java.sql.Date.valueOf(pc.getNgayLamViec()));
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Kiểm tra mọi ca trùng giờ, kể cả hai mã ca khác nhau.
     * Xử lý ca qua đêm và ca của ngày liền kề.
     * Khi sửa, boQuaMaPhanCa là mã của chính bản ghi đang sửa.
     */
    public boolean trungGio(String maNhanVien, String maCa, LocalDate ngay,
            String boQuaMaPhanCa) throws SQLException {
        String sqlCa = "SELECT gioBatDau, gioKetThuc FROM CaLam WHERE maCa = ?";
        String sqlTrung = "SELECT pc.maPhanCa, pc.ngayLamViec, c.gioBatDau, c.gioKetThuc " +
                "FROM LichPhanCa pc JOIN CaLam c ON pc.maCa = c.maCa " +
                "WHERE pc.maNhanVien = ? " +
                "AND pc.ngayLamViec BETWEEN DATEADD(day, -1, ?) AND DATEADD(day, 1, ?)";
        try (Connection con = connectDB.getConnection()) {
            LocalTime gioBatDau, gioKetThuc;
            try (PreparedStatement ps = con.prepareStatement(sqlCa)) {
                ps.setString(1, maCa);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next())
                        throw new SQLException("Không tìm thấy ca làm: " + maCa);
                    gioBatDau = gioSQL(rs.getString("gioBatDau"));
                    gioKetThuc = gioSQL(rs.getString("gioKetThuc"));
                }
            }
            LocalDateTime batDau = LocalDateTime.of(ngay, gioBatDau);
            LocalDateTime ketThuc = LocalDateTime.of(ngay, gioKetThuc);
            if (!ketThuc.isAfter(batDau))
                ketThuc = ketThuc.plusDays(1);

            try (PreparedStatement ps = con.prepareStatement(sqlTrung)) {
                ps.setString(1, maNhanVien);
                ps.setDate(2, java.sql.Date.valueOf(ngay));
                ps.setDate(3, java.sql.Date.valueOf(ngay));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (boQuaMaPhanCa != null && boQuaMaPhanCa.equals(rs.getString("maPhanCa")))
                            continue;
                        LocalDate ngayCu = rs.getDate("ngayLamViec").toLocalDate();
                        LocalDateTime batDauCu = LocalDateTime.of(ngayCu, gioSQL(rs.getString("gioBatDau")));
                        LocalDateTime ketThucCu = LocalDateTime.of(ngayCu, gioSQL(rs.getString("gioKetThuc")));
                        if (!ketThucCu.isAfter(batDauCu))
                            ketThucCu = ketThucCu.plusDays(1);
                        if (batDau.isBefore(ketThucCu) && batDauCu.isBefore(ketThuc))
                            return true;
                    }
                }
            }
            return false;
        }
    }

    private LocalTime gioSQL(String giaTri) throws SQLException {
        try {
            return LocalTime.parse(giaTri.trim());
        } catch (Exception ex) {
            throw new SQLException("Giờ ca làm không hợp lệ: " + giaTri, ex);
        }
    }

    // XOA PHAN CA
    public boolean xoa(String maPhanCa) {
        String sql = "DELETE FROM LichPhanCa " +
                "WHERE maPhanCa = ?";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhanCa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // KIEM TRA NHAN VIEN DA DUOC PHAN CA CHUA
    public boolean daPhanCa(
            String maNhanVien,
            String maCa,
            java.sql.Date ngayLamViec) {
        String sql = "SELECT COUNT(*) " +
                "FROM LichPhanCa " +
                "WHERE maNhanVien = ? " +
                "AND maCa = ? " +
                "AND ngayLamViec = ?";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            ps.setString(2, maCa);
            ps.setDate(3, ngayLamViec);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // TAO MA PHAN CA TU DONG
    public String taoMaPhanCa() {

        String sql = "SELECT MAX(maPhanCa) " +
                "FROM LichPhanCa " +
                "WHERE maPhanCa LIKE 'PC%'";

        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maCu = rs.getString(1);
                if (maCu != null && maCu.length() >= 3) {
                    try {
                        int so = Integer.parseInt(
                                maCu.substring(2));
                        return String.format(
                                "PC%03d",
                                so + 1);
                    } catch (NumberFormatException e) {
                        return "PC001";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "PC001";
    }

    // DANH SACH NHAN VIEN
    public ArrayList<String[]> getDanhSachNhanVien() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT maNhanVien, hoTen " +
                "FROM NhanVien " +
                "ORDER BY maNhanVien";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String[] nv = new String[2];
                nv[0] = rs.getString("maNhanVien");
                nv[1] = rs.getString("hoTen");
                list.add(nv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // DANH SACH CA LAM
    public ArrayList<String[]> getDanhSachCa() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT maCa, tenCa, gioBatDau, gioKetThuc " +
                "FROM CaLam " +
                "ORDER BY maCa";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String[] ca = new String[4];
                ca[0] = rs.getString("maCa");
                ca[1] = rs.getString("tenCa");
                ca[2] = formatGio(rs.getString("gioBatDau"));
                ca[3] = formatGio(rs.getString("gioKetThuc"));
                list.add(ca);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private String formatGio(String gio) {
        if (gio == null || gio.trim().isEmpty()) {
            return "";
        }
        gio = gio.trim();
        // Vi du:
        // 06:00:00.0000000
        // 14:00:00.0000000
        int index = gio.indexOf(":");
        if (index < 0) {
            return gio;
        }
        // Lay HH:mm
        if (gio.length() >= 5) {
            return gio.substring(0, 5);
        }
        return gio;
    }
}