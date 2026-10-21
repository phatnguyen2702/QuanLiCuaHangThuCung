package dao;

import ConnectDB.connectDB;
import entity.DichVu;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** CRUD dich vu. Khong xoa du lieu giao dich lien quan. */
public class DichVuDAO {

    public ArrayList<DichVu> timKiem(String tuKhoa) throws SQLException {
        ArrayList<DichVu> ketQua = new ArrayList<>();
        String sql = "SELECT maDichVu, tenDichVu, giaDichVu, moTa FROM DichVu "
                + "WHERE maDichVu LIKE ? OR tenDichVu LIKE ? OR moTa LIKE ? "
                + "ORDER BY maDichVu";
        String kw = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++)
                ps.setString(i, kw);
            try (ResultSet rs = ps.executeQuery()) {
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
        try (Connection con = connectDB.getConnection()) {
            return timMaTiepTheo(con, false);
        }
    }

    private String timMaTiepTheo(Connection con, boolean khoaBang) throws SQLException {
        String sql = "SELECT maDichVu FROM DichVu" + (khoaBang ? " WITH (TABLOCKX, HOLDLOCK)" : "");
        int max = 0;
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
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
        String sql = "INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, moTa) VALUES (?, ?, ?, ?)";
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String ma = timMaTiepTheo(con, true);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, ma);
                    ps.setString(2, dv.getTenDichVu());
                    ps.setBigDecimal(3, dv.getGiaDichVu());
                    ps.setString(4, dv.getMoTa());
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Không thể thêm dịch vụ.");
                }
                con.commit();
                return ma;
            } catch (SQLException | RuntimeException ex) {
                try {
                    con.rollback();
                } catch (SQLException loiRollback) {
                    ex.addSuppressed(loiRollback);
                }
                throw ex;
            }
        }
    }

    public boolean sua(DichVu dv) throws SQLException {
        String sql = "UPDATE DichVu SET tenDichVu = ?, giaDichVu = ?, moTa = ? WHERE maDichVu = ?";
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
        String sql = "SELECT (SELECT COUNT(*) FROM ChiTietLichHen WHERE maDichVu = ?), "
                + "(SELECT COUNT(*) FROM ChiTietDichVu WHERE maDichVu = ?), "
                + "(SELECT COUNT(*) FROM ChiTietHoaDon WHERE maDichVu = ?)";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++)
                ps.setString(i, ma);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new int[] { rs.getInt(1), rs.getInt(2), rs.getInt(3) };
            }
        }
    }

    public boolean xoa(String ma) throws SQLException {
        // FK trong SQL Server tiep tuc bao ve neu co giao dich phat sinh dong thoi.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM DichVu WHERE maDichVu = ?")) {
            ps.setString(1, ma);
            return ps.executeUpdate() == 1;
        }
    }
}
