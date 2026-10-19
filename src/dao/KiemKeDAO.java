package dao;

import ConnectDB.connectDB;
import entity.ChiTietPhieuKiemKe;
import entity.PhieuKiemKe;
import entity.SanPham;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cua man hinh "Kiem ke kho": phieu kiem ke + danh sach san pham
 * kem ton he thong de kiem.
 */
public class KiemKeDAO {

    public String taoMaMoi() throws SQLException {
        return taoMa("PhieuKiemKe", "maPhieuKiemKe", "KK", 3);
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

    /** Toan bo san pham kem ton kho he thong (cong thuc ton lay tu SanPhamDAO de luon khop). */
    public ArrayList<SanPham> laySanPhamKemTon() throws SQLException {

        ArrayList<SanPham> list = new ArrayList<>();

        String sql = """
                SELECT sp.maSanPham,
                       sp.tenSanPham,
                """ + SanPhamDAO.BIEU_THUC_TON + """
                       AS tonKho
                FROM SanPham sp
                ORDER BY sp.maSanPham
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                SanPham sp = new SanPham();
                sp.setMaSanPham(rs.getString("maSanPham"));
                sp.setTenSanPham(rs.getString("tenSanPham"));
                sp.setTonKho(rs.getInt("tonKho"));
                list.add(sp);
            }
        }

        return list;
    }

    // Sinh ma tu dong dang TIENTO + so (DM005, SP009...): lay so lon nhat dang co roi cong 1
    private static String taoMa(String bang, String cot,
                                String tienTo, int soChuSo) throws SQLException {

        int max = 0;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT " + cot + " FROM " + bang);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                String ma = rs.getString(1);

                if (ma != null && ma.startsWith(tienTo)) {
                    String so = ma.substring(tienTo.length());

                    if (so.matches("\\d{1,9}")) {
                        max = Math.max(max, Integer.parseInt(so));
                    }
                }
            }
        }

        return tienTo + String.format("%0" + soChuSo + "d", max + 1);
    }
}
