package dao;

import UI.PhanQuyen;

import entity.CaLam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import ConnectDB.connectDB;

// Lớp truy cập cơ sở dữ liệu cho ca làm.
public class CaLamDAO {

    // LAY TAT CA CA LAM
    // Lấy toàn bộ danh sách bản ghi từ cơ sở dữ liệu.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<CaLam>.
    public ArrayList<CaLam> getAll() {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.CA_LAM);
        // list: Danh sách chứa các phần tử kiểu CaLam.
        ArrayList<CaLam> list = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT maCa, tenCa, gioBatDau, gioKetThuc " +
                "FROM CaLam " +
                "ORDER BY maCa";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
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
    // Thêm dữ liệu ca làm trong cơ sở dữ liệu.
    // Tham số ca: ca làm cần xử lý.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean them(CaLam ca) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.CA_LAM);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "INSERT INTO CaLam (maCa, tenCa, gioBatDau, gioKetThuc) "
                + "VALUES (?, ?, ?, ?)";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection()) {
            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
            con.setAutoCommit(false);
            try {
                // maMoi: Lưu mã mới.
                String maMoi = MaTuDongDAO.taoMaTrongGiaoDich(con, MaTuDongDAO.Loai.CA_LAM);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, maMoi);
                    ps.setString(2, ca.getTenCa());
                    ps.setString(3, ca.getGioBatDau());
                    ps.setString(4, ca.getGioKetThuc());
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Không thể thêm ca làm.");
                }
                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                con.commit();
                ca.setMaCa(maMoi);
                return true;
            } catch (SQLException ex) {
                // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
                con.rollback();
                throw ex;
            }
        }
    }

    // SUA
    // Cập nhật dữ liệu ca làm trong cơ sở dữ liệu.
    // Tham số ca: ca làm cần xử lý.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean sua(CaLam ca) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.CA_LAM);

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
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
    // Xử lý xóa dữ liệu ca làm trong cơ sở dữ liệu.
    // Tham số maCa: mã ca.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean xoa(String maCa) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.CA_LAM);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
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