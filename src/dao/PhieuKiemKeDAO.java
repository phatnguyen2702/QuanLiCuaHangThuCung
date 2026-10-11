package dao;

import ConnectDB.connectDB;
import entity.ChiTietPhieuKiemKe;
import entity.PhieuKiemKe;

import java.sql.*;
import java.util.List;

public class PhieuKiemKeDAO {

    public String taoMaMoi() throws SQLException {
        return MaTuDong.taoMa("PhieuKiemKe", "maPhieuKiemKe", "KK", 3);
    }

    // Luu phieu + chi tiet trong 1 giao dich
    public void luu(PhieuKiemKe phieu,
                    List<ChiTietPhieuKiemKe> chiTiet) throws SQLException {

        String sqlPhieu = """
                INSERT INTO PhieuKiemKe
                (maPhieuKiemKe, ngayKiemKe, trangThai, maNhanVien)
                VALUES (?, ?, ?, ?)
                """;

        String sqlChiTiet = """
                INSERT INTO ChiTietPhieuKiemKe
                (maPhieuKiemKe, maSanPham, soLuongHeThong,
                 soLuongThucTe, trangThai)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = connectDB.getConnection()) {

            con.setAutoCommit(false);

            try {
                try (PreparedStatement ps = con.prepareStatement(sqlPhieu)) {
                    ps.setString(1, phieu.getMaPhieuKiemKe());
                    ps.setTimestamp(2, Timestamp.valueOf(phieu.getNgayKiemKe()));
                    ps.setBoolean(3, phieu.isTrangThai());
                    ps.setString(4, phieu.getMaNhanVien());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {
                    for (ChiTietPhieuKiemKe ct : chiTiet) {
                        ps.setString(1, phieu.getMaPhieuKiemKe());
                        ps.setString(2, ct.getMaSanPham());
                        ps.setInt(3, ct.getSoLuongHeThong());
                        ps.setInt(4, ct.getSoLuongThucTe());
                        ps.setBoolean(5, ct.isTrangThai());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
