package dao;

import UI.PhanQuyen;

import entity.KhachHang;

import java.sql.*;
import java.util.ArrayList;

// Cac ham bo sung cho man Quan ly khach hang: xem chi tiet + sua khach hang
// Lớp truy cập cơ sở dữ liệu cho khách hàng chi tiết.
public class KhachHangChiTietDAO {

        // =========================================================
        // THU CUNG CUA 1 KHACH: moi phan tu {ten, loai, giong, gioiTinh, canNang}
        // =========================================================

        // Lấy thú cưng.
        // Tham số maKhachHang: mã khách hàng.
        // Kết quả: đối tượng/giá trị kiểu ArrayList<String[]>.
        public ArrayList<String[]> getThuCung(String maKhachHang) {

                // list: Danh sách chứa các phần tử kiểu String[].
                ArrayList<String[]> list = new ArrayList<>();

                // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
                String sql = "SELECT tenThuCung, loai, giong, gioiTinh, canNang "
                                + "FROM ThuCung WHERE maKhachHang = ? ORDER BY maThuCung";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, maKhachHang);

                        // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
                        try (ResultSet rs = ps.executeQuery()) {

                                // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
                                while (rs.next()) {

                                        // kg: Giá trị kg dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
                                        java.math.BigDecimal kg = rs.getBigDecimal("canNang");

                                        list.add(new String[] {
                                                        rs.getString("tenThuCung"),
                                                        rs.getString("loai"),
                                                        rs.getString("giong"),
                                                        rs.getString("gioiTinh"),
                                                        kg == null ? "" : kg.stripTrailingZeros().toPlainString() });
                                }
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list;
        }

        // =========================================================
        // SDT da thuoc ve khach KHAC chua? (dung khi sua)
        // =========================================================

        // Kiểm tra số điện thoại có thuộc khách hàng khác hay không.
        // Tham số sdt: số điện thoại.
        // Tham số maKhachHang: mã khách hàng.
        // Kết quả: true/false theo điều kiện xử lý.
        public boolean tonTaiSdtKhac(String sdt, String maKhachHang) {

                // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
                String sql = "SELECT 1 FROM KhachHang WHERE soDienThoai = ? AND maKhachHang <> ?";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, sdt);
                        ps.setString(2, maKhachHang);

                        // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
                        try (ResultSet rs = ps.executeQuery()) {
                                return rs.next();
                        }

                } catch (SQLException e) {

                        e.printStackTrace();

                        return false;
                }
        }

        // =========================================================
        // SUA THONG TIN KHACH HANG
        // =========================================================

        // Cập nhật dữ liệu khách hàng chi tiết trong cơ sở dữ liệu.
        // Tham số kh: khách hàng cần xử lý.
        // Kết quả: true/false theo điều kiện xử lý.
        public boolean sua(KhachHang kh) {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.KHACH_HANG);

                // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
                        con.setAutoCommit(false);

                        try {

                                try (PreparedStatement ps = con.prepareStatement(
                                                "UPDATE KhachHang SET hoTen = ?, soDienThoai = ? "
                                                                + "WHERE maKhachHang = ?")) {

                                        ps.setString(1, kh.getHoTen());
                                        ps.setString(2, kh.getSoDienThoai());
                                        ps.setString(3, kh.getMaKhachHang());

                                        if (ps.executeUpdate() == 0) {

                                                // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
                                                con.rollback();

                                                return false;
                                        }
                                }

                                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                                con.commit();

                                return true;

                        } catch (SQLException e) {

                                // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
                                con.rollback();

                                throw e;
                        }

                } catch (SQLException e) {

                        e.printStackTrace();

                        return false;
                }
        }

}
