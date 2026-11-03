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
        String sql = "INSERT INTO CaLam (maCa, tenCa, gioBatDau, gioKetThuc) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String maMoi = MaTuDongDAO.taoMaTrongGiaoDich(con, MaTuDongDAO.Loai.CA_LAM);
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
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public boolean sua(CaLam ca) {
        control.PhanQuyenControl.yeuCau(control.PhanQuyenControl.Quyen.CA_LAM);
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
}
