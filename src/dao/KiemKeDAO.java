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

    private void damBaoCotKiemKe() throws SQLException {
        try (Connection con = ConnectDB.connectDB.getConnection(); Statement st = con.createStatement()) {
            st.execute("IF COL_LENGTH('dbo.PhieuKiemKe','ngayBatDau') IS NULL ALTER TABLE dbo.PhieuKiemKe ADD ngayBatDau DATETIME2 NULL");
            st.execute("IF COL_LENGTH('dbo.PhieuKiemKe','ngayChot') IS NULL ALTER TABLE dbo.PhieuKiemKe ADD ngayChot DATETIME2 NULL");
            st.execute("IF COL_LENGTH('dbo.PhieuKiemKe','lyDoChenhLech') IS NULL ALTER TABLE dbo.PhieuKiemKe ADD lyDoChenhLech NVARCHAR(500) NULL");
            st.execute("IF COL_LENGTH('dbo.PhieuKiemKe','maNhanVienXacNhan') IS NULL ALTER TABLE dbo.PhieuKiemKe ADD maNhanVienXacNhan VARCHAR(10) NULL");
        }
    }

    public String taoMaMoi() throws SQLException {
        return taoMa("PhieuKiemKe", "maPhieuKiemKe", "KK", 3);
    }

    // Luu phieu + chi tiet trong 1 giao dich
    public void luu(PhieuKiemKe phieu,
                    List<ChiTietPhieuKiemKe> chiTiet) throws SQLException {

        damBaoCotKiemKe();

        String sqlPhieu = """
                INSERT INTO PhieuKiemKe
                (maPhieuKiemKe, ngayKiemKe, trangThai, maNhanVien, ngayBatDau, ngayChot, lyDoChenhLech, maNhanVienXacNhan)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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
                int daCapNhat;
                try (PreparedStatement ps = con.prepareStatement("UPDATE PhieuKiemKe SET ngayKiemKe=?, trangThai=1, maNhanVien=?, ngayBatDau=?, ngayChot=?, lyDoChenhLech=?, maNhanVienXacNhan=? WHERE maPhieuKiemKe=? AND ISNULL(trangThai,0)=0")) {
                    ps.setTimestamp(1, Timestamp.valueOf(phieu.getNgayKiemKe())); ps.setString(2, phieu.getMaNhanVien());
                    ps.setTimestamp(3, Timestamp.valueOf(phieu.getNgayBatDau())); ps.setTimestamp(4, Timestamp.valueOf(phieu.getNgayChot()));
                    ps.setString(5, phieu.getLyDoChenhLech()); ps.setString(6, phieu.getMaNhanVienXacNhan());
                    ps.setString(7, phieu.getMaPhieuKiemKe()); daCapNhat = ps.executeUpdate();
                }
                if (daCapNhat > 0) {
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietPhieuKiemKe WHERE maPhieuKiemKe=?")) {
                        ps.setString(1, phieu.getMaPhieuKiemKe()); ps.executeUpdate();
                    }
                } else try (PreparedStatement ps = con.prepareStatement(sqlPhieu)) {
                    ps.setString(1, phieu.getMaPhieuKiemKe());
                    ps.setTimestamp(2, Timestamp.valueOf(phieu.getNgayKiemKe()));
                    ps.setBoolean(3, phieu.isTrangThai());
                    ps.setString(4, phieu.getMaNhanVien());
                    ps.setTimestamp(5, Timestamp.valueOf(phieu.getNgayBatDau()));
                    ps.setTimestamp(6, Timestamp.valueOf(phieu.getNgayChot()));
                    ps.setString(7, phieu.getLyDoChenhLech());
                    ps.setString(8, phieu.getMaNhanVienXacNhan());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {
                    for (ChiTietPhieuKiemKe ct : chiTiet) {
                        ps.setString(1, phieu.getMaPhieuKiemKe());
                        ps.setString(2, ct.getMaSanPham());
                        ps.setInt(3, ct.getSoLuongHeThong());
                        if (ct.getSoLuongThucTe() == null) ps.setNull(4, Types.INTEGER);
                        else ps.setInt(4, ct.getSoLuongThucTe());
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

    public void luuNhap(PhieuKiemKe phieu, List<ChiTietPhieuKiemKe> chiTiet) throws SQLException {
        damBaoCotKiemKe();
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                int capNhat;
                try (PreparedStatement ps = con.prepareStatement("UPDATE PhieuKiemKe SET ngayKiemKe=?,trangThai=0,maNhanVien=?,ngayBatDau=?,ngayChot=NULL,lyDoChenhLech=NULL,maNhanVienXacNhan=NULL WHERE maPhieuKiemKe=? AND ISNULL(trangThai,0)=0")) {
                    ps.setTimestamp(1, Timestamp.valueOf(phieu.getNgayBatDau())); ps.setString(2, phieu.getMaNhanVien());
                    ps.setTimestamp(3, Timestamp.valueOf(phieu.getNgayBatDau())); ps.setString(4, phieu.getMaPhieuKiemKe()); capNhat = ps.executeUpdate();
                }
                if (capNhat == 0) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO PhieuKiemKe(maPhieuKiemKe,ngayKiemKe,trangThai,maNhanVien,ngayBatDau) VALUES(?,?,0,?,?)")) {
                        ps.setString(1, phieu.getMaPhieuKiemKe()); ps.setTimestamp(2, Timestamp.valueOf(phieu.getNgayBatDau()));
                        ps.setString(3, phieu.getMaNhanVien()); ps.setTimestamp(4, Timestamp.valueOf(phieu.getNgayBatDau())); ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietPhieuKiemKe WHERE maPhieuKiemKe=?")) {
                    ps.setString(1, phieu.getMaPhieuKiemKe()); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO ChiTietPhieuKiemKe(maPhieuKiemKe,maSanPham,soLuongHeThong,soLuongThucTe,trangThai) VALUES(?,?,?,?,?)")) {
                    for (ChiTietPhieuKiemKe ct : chiTiet) {
                        ps.setString(1, phieu.getMaPhieuKiemKe()); ps.setString(2, ct.getMaSanPham()); ps.setInt(3, ct.getSoLuongHeThong());
                        if (ct.getSoLuongThucTe() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, ct.getSoLuongThucTe());
                        ps.setBoolean(5, ct.isTrangThai()); ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
            } catch (SQLException e) { con.rollback(); throw e; }
        }
    }

    public ArrayList<PhieuKiemKe> dsPhieu() throws SQLException {
        damBaoCotKiemKe();
        ArrayList<PhieuKiemKe> ds = new ArrayList<>();
        String sql = "SELECT maPhieuKiemKe, ngayKiemKe, trangThai, maNhanVien, ngayBatDau, ngayChot, lyDoChenhLech, maNhanVienXacNhan FROM PhieuKiemKe ORDER BY ngayKiemKe DESC";
        try (Connection c = ConnectDB.connectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                PhieuKiemKe x = new PhieuKiemKe();
                x.setMaPhieuKiemKe(r.getString(1));
                Timestamp t = r.getTimestamp(2); x.setNgayKiemKe(t == null ? null : t.toLocalDateTime());
                x.setTrangThai(r.getBoolean(3)); x.setMaNhanVien(r.getString(4));
                t = r.getTimestamp(5); x.setNgayBatDau(t == null ? null : t.toLocalDateTime());
                t = r.getTimestamp(6); x.setNgayChot(t == null ? null : t.toLocalDateTime());
                x.setLyDoChenhLech(r.getString(7)); x.setMaNhanVienXacNhan(r.getString(8)); ds.add(x);
            }
        }
        return ds;
    }

    public ArrayList<ChiTietPhieuKiemKe> layChiTiet(String maPhieu) throws SQLException {
        ArrayList<ChiTietPhieuKiemKe> ds = new ArrayList<>();
        String sql = "SELECT k.maSanPham,s.tenSanPham,k.soLuongHeThong,k.soLuongThucTe,k.trangThai FROM ChiTietPhieuKiemKe k JOIN SanPham s ON s.maSanPham=k.maSanPham WHERE k.maPhieuKiemKe=? ORDER BY k.maSanPham";
        try (Connection c=connectDB.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,maPhieu); try(ResultSet r=p.executeQuery()) { while(r.next()) {
                ChiTietPhieuKiemKe x=new ChiTietPhieuKiemKe(); x.setMaPhieuKiemKe(maPhieu); x.setMaSanPham(r.getString(1));
                x.setTenSanPham(r.getString(2)); x.setSoLuongHeThong(r.getInt(3)); int thuc=r.getInt(4);
                x.setSoLuongThucTe(r.wasNull()?null:thuc); x.setTrangThai(r.getBoolean(5)); ds.add(x);
            }}
        }
        return ds;
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
