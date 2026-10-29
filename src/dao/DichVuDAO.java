package dao;

import UI.PhanQuyen;

import ConnectDB.connectDB;
import entity.DichVu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** CRUD dich vu. Khong xoa du lieu giao dich lien quan. */
public class DichVuDAO {

    // Tìm kiểm.
    // Tham số tuKhoa: từ khóa tìm kiếm.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<DichVu>.
    public ArrayList<DichVu> timKiem(String tuKhoa) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.DICH_VU);
        // ketQua: Lưu kết quả; phần tử có kiểu DichVu.
        ArrayList<DichVu> ketQua = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "SELECT maDichVu, tenDichVu, giaDichVu, moTa FROM DichVu "
                + "WHERE maDichVu LIKE ? OR tenDichVu LIKE ? OR moTa LIKE ? "
                + "ORDER BY maDichVu";
        String kw = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++)
                ps.setString(i, kw);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
                while (rs.next()) {
                    ketQua.add(new DichVu(
                            rs.getString("maDichVu"),
                            rs.getString("tenDichVu"),
                            rs.getBigDecimal("giaDichVu"),
                            rs.getString("moTa")));
                }
            }
        }
        return ketQua;
    }

    /** Xem ma tiep theo (chi de hien thi; ma chinh thuc tao khi INSERT). */
    public String maDuKien() throws SQLException {
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection con = connectDB.getConnection()) {
            return timMaTiepTheo(con, false);
        }
    }

    // Tìm mã tiếp theo.
    // Tham số con: kết nối cơ sở dữ liệu.
    // Tham số khoaBang: có khóa bảng trong lúc cấp mã.
    // Kết quả: chuỗi kết quả.
    private String timMaTiepTheo(Connection con, boolean khoaBang) throws SQLException {
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "SELECT maDichVu FROM DichVu" + (khoaBang ? " WITH (TABLOCKX, HOLDLOCK)" : "");
        int max = 0;
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                // ma: Lưu mã.
                String ma = rs.getString(1);
                if (ma != null && ma.matches("DV\\d+")) {
                    try {
                        max = Math.max(max, Integer.parseInt(ma.substring(2)));
                    } catch (NumberFormatException ignored) {
                        /* Bo qua ma cu khong chuan */ }
                }
            }
        }
        if (max >= 999) {
            throw new SQLException("Đã dùng hết dải mã DV001 đến DV999. Cần mở rộng quy tắc mã dịch vụ.");
        }
        return String.format("DV%03d", max + 1);
    }

    /** Sinh ma va luu tren cung mot transaction, khoa bang khi cap ma. */
    public String them(DichVu dv) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.DICH_VU);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, moTa) VALUES (?, ?, ?, ?)";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection con = connectDB.getConnection()) {
            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng
            // nhau.
            con.setAutoCommit(false);
            try {
                // ma: Lưu mã.
                String ma = timMaTiepTheo(con, true);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, ma);
                    ps.setString(2, dv.getTenDichVu());
                    ps.setBigDecimal(3, dv.getGiaDichVu());
                    ps.setString(4, dv.getMoTa());
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Không thể thêm dịch vụ.");
                }
                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                con.commit();
                return ma;
            } catch (SQLException | RuntimeException ex) {
                try {
                    // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
                    con.rollback();
                } catch (SQLException loiRollback) {
                    ex.addSuppressed(loiRollback);
                }
                throw ex;
            }
        }
    }

    // Cập nhật dữ liệu dịch vụ trong cơ sở dữ liệu.
    // Tham số dv: dịch vụ cần xử lý.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean sua(DichVu dv) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.DICH_VU);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "UPDATE DichVu SET tenDichVu = ?, giaDichVu = ?, moTa = ? WHERE maDichVu = ?";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dv.getTenDichVu());
            ps.setBigDecimal(2, dv.getGiaDichVu());
            ps.setString(3, dv.getMoTa());
            ps.setString(4, dv.getMaDichVu());
            return ps.executeUpdate() == 1;
        }
    }

    /** Mang [so chi tiet lich hen, so chi tiet phieu Spa, so dong hoa don]. */
    public int[] demLienQuan(String ma) throws SQLException {
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "SELECT (SELECT COUNT(*) FROM ChiTietLichHen WHERE maDichVu = ?), "
                + "(SELECT COUNT(*) FROM ChiTietDichVu WHERE maDichVu = ?), "
                + "(SELECT COUNT(*) FROM ChiTietHoaDon WHERE maDichVu = ?)";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++)
                ps.setString(i, ma);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new int[] { rs.getInt(1), rs.getInt(2), rs.getInt(3) };
            }
        }
    }

    // Xử lý xóa dữ liệu dịch vụ trong cơ sở dữ liệu.
    // Tham số ma: mã.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean xoa(String ma) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.DICH_VU);
        // FK trong SQL Server tiep tuc bao ve neu co giao dich phat sinh dong thoi.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM DichVu WHERE maDichVu = ?")) {
            ps.setString(1, ma);
            return ps.executeUpdate() == 1;
        }
    }
}
