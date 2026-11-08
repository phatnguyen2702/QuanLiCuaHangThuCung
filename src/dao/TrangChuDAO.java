package dao;

import ConnectDB.connectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

public class TrangChuDAO {
    public int demLichSpaHomNay() throws SQLException {
        String sql = "SELECT COUNT(*) FROM LichHen WHERE ngayGioHen >= ? AND ngayGioHen < ? "
                + "AND ISNULL(trangThai, N'Xác nhận') <> N'Đã hủy'";
        LocalDate homNay = LocalDate.now();

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(homNay.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(homNay.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int demPhieuNhapHomNay() throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhieuNhapKho "
                + "WHERE CAST(ngayNhap AS DATE) = CAST(GETDATE() AS DATE)";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
