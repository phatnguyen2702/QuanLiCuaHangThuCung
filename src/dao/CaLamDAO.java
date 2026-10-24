package dao;

import UI.PhanQuyen;

import entity.CaLam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import ConnectDB.connectDB;

public class CaLamDAO {

    // LAY TAT CA CA LAM
    public ArrayList<CaLam> getAll() {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.CA_LAM);
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
                ca.setGioBatDau(rs.getString("gioBatDau"));
                ca.setGioKetThuc(rs.getString("gioKetThuc"));
                list.add(ca);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Mã ca được sinh tự động cùng giao dịch INSERT, không dùng mã người dùng nhập.
    public boolean them(CaLam ca) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.CA_LAM);
        String sql = "INSERT INTO CaLam (maCa, tenCa, gioBatDau, gioKetThuc) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String maMoi = MaTuDongDAO.taoMaTrongGiaoDich(con, MaTuDongDAO.Loai.CA_LAM);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, maMoi);
                    ps.setString(2, ca.getTenCa());
                    ps.setString(3, ca.getGioBatDau());
                    ps.setString(4, ca.getGioKetThuc());
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

    // SUA
    public boolean sua(CaLam ca) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.CA_LAM);

        String sql = "UPDATE CaLam SET " +
                "tenCa = ?, " +
                "gioBatDau = ?, " +
                "gioKetThuc = ? " +
                "WHERE maCa = ?";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ca.getTenCa());
            ps.setString(2, ca.getGioBatDau());
            ps.setString(3, ca.getGioKetThuc());
            ps.setString(4, ca.getMaCa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // XOA
    public boolean xoa(String maCa) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.CA_LAM);
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
}