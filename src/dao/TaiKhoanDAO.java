package dao;

import entity.TaiKhoan;

import java.sql.*;
import java.util.ArrayList;

public class TaiKhoanDAO {

        // =========================================================
        // LAY TAT CA
        // =========================================================

        public ArrayList<TaiKhoan> getAll() {

                ArrayList<TaiKhoan> list = new ArrayList<>();

                String sql = "SELECT maNhanVien, hoTen, "
                                + "soDienThoai, email, matKhau, "
                                + "gioiTinh, vaiTro "
                                + "FROM NhanVien "
                                + "ORDER BY maNhanVien";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();

                                PreparedStatement ps = con.prepareStatement(sql);

                                ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {

                                TaiKhoan tk = new TaiKhoan(
                                                rs.getString("maNhanVien"),
                                                rs.getString("hoTen"),
                                                rs.getString("soDienThoai"),
                                                rs.getString("email"),
                                                rs.getString("matKhau"),
                                                rs.getString("gioiTinh"),
                                                rs.getString("vaiTro"));

                                list.add(tk);
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list;
        }

        // =========================================================
        // THEM
        // =========================================================

        // Mã nhân viên được cấp khi INSERT trong cùng transaction.
        public boolean them(TaiKhoan tk) throws SQLException {
                String sql = "INSERT INTO NhanVien "
                                + "(maNhanVien, hoTen, soDienThoai, email, matKhau, gioiTinh, vaiTro) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (Connection con = ConnectDB.connectDB.getConnection()) {
                        con.setAutoCommit(false);
                        try {
                                String maMoi = MaTuDongDAO.taoMaTrongGiaoDich(
                                                con, MaTuDongDAO.Loai.NHAN_VIEN);
                                try (PreparedStatement ps = con.prepareStatement(sql)) {
                                        ps.setString(1, maMoi);
                                        ps.setString(2, tk.getHoTen());
                                        ps.setString(3, tk.getSoDienThoai());
                                        ps.setString(4, tk.getEmail());
                                        ps.setString(5, tk.getMatKhau());
                                        ps.setString(6, tk.getGioiTinh());
                                        ps.setString(7, tk.getVaiTro());
                                        if (ps.executeUpdate() != 1) {
                                                throw new SQLException("Không thể thêm nhân viên.");
                                        }
                                }
                                con.commit();
                                tk.setMaNhanVien(maMoi);
                                return true;
                        } catch (SQLException ex) {
                                con.rollback();
                                throw ex;
                        }
                }
        }

        // SUA
        public boolean sua(
                        TaiKhoan tk) {

                String sql = "UPDATE NhanVien SET "
                                + "hoTen = ?, "
                                + "soDienThoai = ?, "
                                + "email = ?, "
                                + "matKhau = ?, "
                                + "gioiTinh = ?, "
                                + "vaiTro = ? "
                                + "WHERE maNhanVien = ?";
                try (
                                Connection con = ConnectDB.connectDB.getConnection();

                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, tk.getHoTen());

                        ps.setString(2, tk.getSoDienThoai());

                        ps.setString(3, tk.getEmail());

                        ps.setString(4, tk.getMatKhau());

                        ps.setString(5, tk.getGioiTinh());

                        ps.setString(6, tk.getVaiTro());

                        ps.setString(7, tk.getMaNhanVien());
                        return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                }
        }

        // XOA
        public boolean xoa(
                        String maNhanVien) {
                String sql = "DELETE FROM NhanVien "
                                + "WHERE maNhanVien = ?";
                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, maNhanVien);
                        return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                }
        }

        /**
         * Giu tuong thich voi man hinh QuanLyTaiKhoan co nut "Cap lai MK".
         * Chi goi tu luong quan tri da duoc phan quyen; nen nang cap
         * kiem tra quyen o may chu trong phien ban thuc te.
         */
        public boolean capLaiMatKhau(String maNhanVien, String matKhauMoi) throws SQLException {
                if (maNhanVien == null || maNhanVien.trim().isEmpty()
                                || matKhauMoi == null || matKhauMoi.length() < 6) {
                        throw new IllegalArgumentException("Ma NV hoac mat khau moi khong hop le.");
                }
                String sql = "UPDATE NhanVien SET matKhau = ? WHERE maNhanVien = ?";
                try (Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, matKhauMoi);
                        ps.setString(2, maNhanVien.trim());
                        return ps.executeUpdate() == 1;
                }
        }

        /**
         * Doi mat khau cua tai khoan dang dang nhap.
         * So sanh mat khau cu theo BIN2 de khong nham chu HOA/thuong
         * du SQL Server mac dinh co the dung collation khong phan biet hoa/thuong.
         * Khong tra lai / in ra mat khau cu.
         *
         * Luu y: CSDL cua do an dang luu mat khau dang chuoi thuan.
         * Khi nang cap len password hash can doi logic xac thuc va cap nhat.
         */
        public boolean doiMatKhau(String maNhanVien, String matKhauCu,
                        String matKhauMoi) throws SQLException {
                if (maNhanVien == null || maNhanVien.isBlank()
                                || matKhauCu == null || matKhauCu.isEmpty()
                                || matKhauMoi == null || matKhauMoi.length() < 6
                                || matKhauCu.equals(matKhauMoi)) {
                        return false;
                }

                String sql = "UPDATE NhanVien SET matKhau = ? "
                                + "WHERE maNhanVien = ? "
                                + "AND matKhau COLLATE Latin1_General_100_BIN2 = ?";

                try (Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, matKhauMoi);
                        ps.setString(2, maNhanVien.trim());
                        ps.setString(3, matKhauCu);
                        return ps.executeUpdate() == 1;
                }
        }

}
