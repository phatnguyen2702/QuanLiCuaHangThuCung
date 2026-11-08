package dao;

import control.PhanQuyenControl;
import entity.Voucher;

import java.sql.*;
import java.util.ArrayList;








public class VoucherDAO {


    private static final int MAX_TEN = 50;
    private static final int MAX_DIEU_KIEN = 255;






    public ArrayList<Voucher> getAll(String tuKhoa) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.VOUCHER);

        ArrayList<Voucher> list = new ArrayList<>();

        String sql = "SELECT v.maVoucher, v.tenVoucher, v.phanTramGiam, v.dieuKien, v.trangThai, "
                + "v.ngayBatDau, v.ngayKetThuc, v.soLuongToiDa, "
                + "(SELECT COUNT(*) FROM HoaDon h WHERE h.maVoucher = v.maVoucher) AS daDung "
                + "FROM Vocher v "
                + "WHERE (v.maVoucher LIKE ? OR v.tenVoucher LIKE ?) "
                + "ORDER BY v.maVoucher";

        try (Connection con = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            String kw = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date bd = rs.getDate("ngayBatDau");
                    Date kt = rs.getDate("ngayKetThuc");

                    int sl = rs.getInt("soLuongToiDa");
                    Integer soLuong = rs.wasNull() ? null : sl;

                    Voucher v = new Voucher(
                            rs.getString("maVoucher"),
                            rs.getString("tenVoucher"),
                            rs.getDouble("phanTramGiam"),
                            rs.getString("dieuKien"),
                            rs.getBoolean("trangThai"),
                            bd == null ? null : bd.toLocalDate(),
                            kt == null ? null : kt.toLocalDate(),
                            soLuong);

                    v.setDaDung(rs.getInt("daDung"));
                    list.add(v);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public double[] thongKeHoaDon() {
        double[] kq = new double[3];

        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM HoaDon WHERE maVoucher IS NOT NULL "
                + "   AND YEAR(ngayLap) = YEAR(GETDATE()) AND MONTH(ngayLap) = MONTH(GETDATE())), "
                + "(SELECT ISNULL(SUM(ct.soLuong * ct.donGia * ISNULL(ct.phanTramGiam, 0) / 100.0), 0) "
                + "   FROM HoaDon h JOIN ChiTietHoaDon ct ON ct.maHoaDon = h.maHoaDon "
                + "   WHERE h.maVoucher IS NOT NULL), "
                + "(SELECT CASE WHEN COUNT(*) = 0 THEN 0 "
                + "   ELSE 100.0 * SUM(CASE WHEN maVoucher IS NOT NULL THEN 1 ELSE 0 END) / COUNT(*) END "
                + "   FROM HoaDon)";

        try (Connection con = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                for (int i = 0; i < 3; i++) {
                    kq[i] = rs.getDouble(i + 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kq;
    }






    public String them(Voucher v) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.VOUCHER);

        String loi = kiemTraDoDai(v);
        if (loi != null) {
            return loi;
        }

        String sql = "INSERT INTO Vocher "
                + "(maVoucher, tenVoucher, phanTramGiam, dieuKien, trangThai, "
                + "ngayBatDau, ngayKetThuc, soLuongToiDa) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConnectDB.connectDB.getConnection()) {

            String ma = taoMa(con);

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, ma);
                ganGiaTri(ps, v, 2);
                ps.executeUpdate();
            }

            v.setMaVoucher(ma);
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Lưu thất bại: " + e.getMessage();
        }
    }


    public String sua(Voucher v) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.VOUCHER);

        String loi = kiemTraDoDai(v);
        if (loi != null) {
            return loi;
        }

        String sql = "UPDATE Vocher SET tenVoucher = ?, phanTramGiam = ?, dieuKien = ?, "
                + "trangThai = ?, ngayBatDau = ?, ngayKetThuc = ?, soLuongToiDa = ? "
                + "WHERE maVoucher = ?";

        try (Connection con = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ganGiaTri(ps, v, 1);
            ps.setString(8, v.getMaVoucher());

            if (ps.executeUpdate() == 0) {
                return "Không tìm thấy voucher " + v.getMaVoucher() + ".";
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Lưu thất bại: " + e.getMessage();
        }
    }


    public String doiTrangThai(String maVoucher, boolean hoatDong) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.VOUCHER);

        String sql = "UPDATE Vocher SET trangThai = ? WHERE maVoucher = ?";

        try (Connection con = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, hoatDong);
            ps.setString(2, maVoucher);

            if (ps.executeUpdate() == 0) {
                return "Không tìm thấy voucher " + maVoucher + ".";
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Cập nhật thất bại: " + e.getMessage();
        }
    }


    public String xoa(String maVoucher) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.VOUCHER);

        try (Connection con = ConnectDB.connectDB.getConnection()) {


            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM HoaDon WHERE maVoucher = ?")) {
                ps.setString(1, maVoucher);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return "Voucher đã được dùng trong hóa đơn nên không thể xóa.\n"
                                + "Hãy dùng nút Hủy voucher.";
                    }
                }
            }


            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM Vocher WHERE maVoucher = ?")) {
                ps.setString(1, maVoucher);
                ps.executeUpdate();
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Xóa thất bại: " + e.getMessage();
        }
    }





    private String kiemTraDoDai(Voucher v) {
        if (v.getTenVoucher() != null && v.getTenVoucher().length() > MAX_TEN) {
            return "Tên chương trình tối đa " + MAX_TEN + " ký tự.";
        }
        if (v.getDieuKien() != null && v.getDieuKien().length() > MAX_DIEU_KIEN) {
            return "Điều kiện tối đa " + MAX_DIEU_KIEN + " ký tự.";
        }
        return null;
    }





    private void ganGiaTri(PreparedStatement ps, Voucher v, int batDau) throws SQLException {
        int i = batDau;

        ps.setString(i++, v.getTenVoucher());
        ps.setDouble(i++, v.getPhanTramGiam());
        ps.setString(i++, v.getDieuKien());
        ps.setBoolean(i++, v.isTrangThai());

        if (v.getNgayBatDau() == null) {
            ps.setNull(i++, Types.DATE);
        } else {
            ps.setDate(i++, Date.valueOf(v.getNgayBatDau()));
        }

        if (v.getNgayKetThuc() == null) {
            ps.setNull(i++, Types.DATE);
        } else {
            ps.setDate(i++, Date.valueOf(v.getNgayKetThuc()));
        }

        if (v.getSoLuongToiDa() == null) {
            ps.setNull(i++, Types.INTEGER);
        } else {
            ps.setInt(i++, v.getSoLuongToiDa());
        }
    }


    private String taoMa(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT MAX(maVoucher) FROM Vocher WHERE maVoucher LIKE 'VC%'");
                ResultSet rs = ps.executeQuery()) {

            int n = 0;
            if (rs.next() && rs.getString(1) != null) {
                try {
                    n = Integer.parseInt(rs.getString(1).substring(2));
                } catch (NumberFormatException e) {
                    n = 0;
                }
            }
            return "VC" + String.format("%03d", n + 1);
        }
    }
}
