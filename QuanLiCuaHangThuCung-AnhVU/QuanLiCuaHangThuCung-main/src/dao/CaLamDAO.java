package dao;

import entity.CaLam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import ConnectDB.connectDB;

public class CaLamDAO {

    // ==============================
    // LAY TAT CA CA LAM
    // ==============================

    public ArrayList<CaLam> getAll() {

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

    // ==============================
    // THEM
    // ==============================

    public boolean them(CaLam ca) {

        String sql = "INSERT INTO CaLam " +
                "(maCa, tenCa, gioBatDau, gioKetThuc) " +
                "VALUES (?, ?, ?, ?)";

        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ca.getMaCa());
            ps.setString(2, ca.getTenCa());
            ps.setString(3, ca.getGioBatDau());
            ps.setString(4, ca.getGioKetThuc());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==============================
    // SUA
    // ==============================

    public boolean sua(CaLam ca) {

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

    // ==============================
    // XOA
    // ==============================

    public boolean xoa(String maCa) {

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