package dao;

import entity.CaLam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalTime;
import java.util.ArrayList;

import ConnectDB.connectDB;

public class CaLamDAO {

    public ArrayList<CaLam> getAll() {
        control.PhanQuyenControl.yeuCau(control.PhanQuyenControl.Quyen.CA_LAM);
        ArrayList<CaLam> list = new ArrayList<>();
        String sql = "SELECT maCa, tenCa, gioBatDau, gioKetThuc " +
                "FROM CaLam " +
                "ORDER BY maCa";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CaLam ca = new CaLam();
                ca.setMaCa(rs.getString("maCa"));
                ca.setTenCa(rs.getString("tenCa"));
                ca.setGioBatDau(docGio(rs, "gioBatDau"));
                ca.setGioKetThuc(docGio(rs, "gioKetThuc"));
                list.add(ca);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean them(CaLam ca) throws SQLException {
        control.PhanQuyenControl.yeuCau(control.PhanQuyenControl.Quyen.CA_LAM);
        kiemTraGioLam(ca);
        String sql = "INSERT INTO CaLam (maCa, tenCa, gioBatDau, gioKetThuc) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String maMoi = taoMaMoi(con);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, maMoi);
                    ps.setString(2, ca.getTenCa());
                    ghiGio(ps, 3, ca.getGioBatDau());
                    ghiGio(ps, 4, ca.getGioKetThuc());
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Không thể thêm ca làm.");
                }
                con.commit();
                ca.setMaCa(maMoi);
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public String goiYMa() {
        try (Connection con = connectDB.getConnection()) {
            String sql = "SELECT COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maCa, 3, 3))), 0) "
                    + "FROM dbo.CaLam WHERE maCa LIKE 'CA[0-9][0-9][0-9]'";
            try (PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return "Tự động khi lưu";
                return String.format(java.util.Locale.ROOT, "CA%03d", rs.getInt(1) + 1);
            }
        } catch (SQLException ex) {
            return "Tự động khi lưu";
        }
    }

    private static String taoMaMoi(Connection con) throws SQLException {

        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maCa, 3, 3))), 0) "
                + "FROM dbo.CaLam"
                + " WHERE maCa LIKE 'CA[0-9][0-9][0-9]'";
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (!rs.next())
                throw new SQLException("Không đọc được mã ca làm mới.");
            int tiepTheo = rs.getInt(1) + 1;

            return String.format(java.util.Locale.ROOT, "CA%03d", tiepTheo);
        }
    }

    public boolean sua(CaLam ca) {
        control.PhanQuyenControl.yeuCau(control.PhanQuyenControl.Quyen.CA_LAM);
        kiemTraGioLam(ca);
        String sql = "UPDATE CaLam SET " +
                "tenCa = ?, " +
                "gioBatDau = ?, " +
                "gioKetThuc = ? " +
                "WHERE maCa = ?";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ca.getTenCa());
            ghiGio(ps, 2, ca.getGioBatDau());
            ghiGio(ps, 3, ca.getGioKetThuc());
            ps.setString(4, ca.getMaCa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String maCa) {
        control.PhanQuyenControl.yeuCau(control.PhanQuyenControl.Quyen.CA_LAM);
        String sql = "DELETE FROM CaLam WHERE maCa = ?";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static LocalTime docGio(ResultSet rs, String tenCot) throws SQLException {
        Time sqlTime = rs.getTime(tenCot);
        return sqlTime == null ? null : sqlTime.toLocalTime();
    }

    private static void ghiGio(PreparedStatement ps, int viTri, LocalTime gio) throws SQLException {
        if (gio == null)
            ps.setNull(viTri, Types.TIME);
        else
            ps.setTime(viTri, Time.valueOf(gio));
    }

    private static void kiemTraGioLam(CaLam ca) {
        LocalTime batDau = ca == null ? null : ca.getGioBatDau();
        LocalTime ketThuc = ca == null ? null : ca.getGioKetThuc();
        LocalTime moCua = LocalTime.of(6, 0);
        LocalTime dongCua = LocalTime.of(22, 0);

        if (batDau == null || ketThuc == null
                || batDau.isBefore(moCua) || ketThuc.isAfter(dongCua)
                || !ketThuc.isAfter(batDau)) {
            throw new IllegalArgumentException(
                    "Ca làm không hợp lệ: phải nằm trong 06:00-22:00 và kết thúc sau giờ bắt đầu.");
        }
    }
}
