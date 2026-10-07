package dao;

import entity.TaiKhoan;

import java.sql.*;
import java.util.ArrayList;

import ConnectDB.connectDB;

public class TaiKhoanDAO {

    public ArrayList<TaiKhoan> getAll() {

        ArrayList<TaiKhoan> list =
                new ArrayList<>();

        String sql = """
                SELECT maNhanVien,
                       hoTen,
                       matKhau,
                       vaiTro,
                       trangThai
                FROM NhanVien
                ORDER BY maNhanVien
                """;

        try (
            Connection con =connectDB.getConnection();

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ResultSet rs =
                    ps.executeQuery()
        ) {

            while (rs.next()) {

                TaiKhoan tk = new TaiKhoan();

                tk.setMaNhanVien(
                        rs.getString("maNhanVien")
                );

                tk.setHoTen(
                        rs.getString("hoTen")
                );

                tk.setMatKhau(
                        rs.getString("matKhau")
                );

                tk.setVaiTro(
                        rs.getString("vaiTro")
                );

                tk.setTrangThai(
                        rs.getBoolean("trangThai")
                );

                list.add(tk);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean them(TaiKhoan tk) {

        String sql = """
                INSERT INTO NhanVien
                (
                    maNhanVien,
                    hoTen,
                    matKhau,
                    vaiTro,
                    trangThai
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
            Connection con =
            		connectDB.getConnection();

            PreparedStatement ps =
                    con.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    tk.getMaNhanVien()
            );

            ps.setString(
                    2,
                    tk.getHoTen()
            );

            ps.setString(
                    3,
                    tk.getMatKhau()
            );

            ps.setString(
                    4,
                    tk.getVaiTro()
            );

            ps.setBoolean(
                    5,
                    tk.isTrangThai()
            );

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sua(TaiKhoan tk) {

        String sql = """
                UPDATE NhanVien
                SET hoTen = ?,
                    matKhau = ?,
                    vaiTro = ?,
                    trangThai = ?
                WHERE maNhanVien = ?
                """;

        try (
            Connection con =
            		connectDB.getConnection();

            PreparedStatement ps =
                    con.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    tk.getHoTen()
            );

            ps.setString(
                    2,
                    tk.getMatKhau()
            );

            ps.setString(
                    3,
                    tk.getVaiTro()
            );

            ps.setBoolean(
                    4,
                    tk.isTrangThai()
            );

            ps.setString(
                    5,
                    tk.getMaNhanVien()
            );

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String maNhanVien) {

        String sql = """
                DELETE FROM NhanVien
                WHERE maNhanVien = ?
                """;

        try (
            Connection con =
            		connectDB.getConnection();

            PreparedStatement ps =
                    con.prepareStatement(sql)
        ) {

            ps.setString(1, maNhanVien);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}