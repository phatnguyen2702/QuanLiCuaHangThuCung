package dao;

import control.PhanQuyenControl;
import entity.Voucher;

import java.sql.*;
import java.util.ArrayList;

/**
 * DAO voucher: chỉ chạy SQL, kiểm tra phân quyền và độ dài chuỗi.
 * Kiểm tra rỗng / sai định dạng / logic ngày tháng là việc của VoucherControl.
 *
 * Quy ước: các hàm ghi (them / sua / doiTrangThai / xoa) trả về null nếu thành công,
 * ngược lại trả về câu thông báo lỗi.
 */
public class VoucherDAO {

    // Độ dài tối đa của cột trong CSDL (sửa lại nếu CSDL của bạn khác)
    private static final int MAX_TEN = 50;
    private static final int MAX_DIEU_KIEN = 255;

    // =====================================================================
    // TRUY VẤN (đọc dữ liệu)
    // =====================================================================

    /** Lấy voucher theo mã hoặc tên (để trống = lấy tất cả). */
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
                    Integer soLuong = rs.wasNull() ? null : sl; // cột NULL = không giới hạn

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

    /** Thống kê: [0] hóa đơn dùng voucher tháng này, [1] tổng tiền đã giảm, [2] % hóa đơn có voucher. */
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

    // =====================================================================
    // GHI DỮ LIỆU - trả về null nếu thành công
    // =====================================================================

    /** Thêm voucher. Thành công: mã mới được gán vào v (v.getMaVoucher()). */
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
                ganGiaTri(ps, v, 2); // các cột còn lại bắt đầu từ dấu ? thứ 2
                ps.executeUpdate();
            }

            v.setMaVoucher(ma);
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Lưu thất bại: " + e.getMessage();
        }
    }

    /** Sửa toàn bộ thông tin voucher (theo mã v.getMaVoucher()). */
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

            ganGiaTri(ps, v, 1); // 7 dấu ? đầu
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

    /** Kích hoạt (true) hoặc hủy (false) voucher. */
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

    /** Xóa hẳn voucher. Không xóa được nếu đã có hóa đơn dùng. */
    public String xoa(String maVoucher) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.VOUCHER);

        try (Connection con = ConnectDB.connectDB.getConnection()) {

            // 1. Đã có hóa đơn dùng voucher này chưa?
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

            // 2. Chưa ai dùng -> xóa
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

    // =====================================================================
    // HÀM PHỤ (private)
    // =====================================================================

    private String kiemTraDoDai(Voucher v) {
        if (v.getTenVoucher() != null && v.getTenVoucher().length() > MAX_TEN) {
            return "Tên chương trình tối đa " + MAX_TEN + " ký tự.";
        }
        if (v.getDieuKien() != null && v.getDieuKien().length() > MAX_DIEU_KIEN) {
            return "Điều kiện tối đa " + MAX_DIEU_KIEN + " ký tự.";
        }
        return null;
    }

    /**
     * Gán 7 giá trị (tên, %, điều kiện, trạng thái, ngày bắt đầu, ngày kết thúc, số lượt)
     * vào PreparedStatement, bắt đầu từ dấu ? thứ batDau. Dùng chung cho them() và sua().
     */
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

    /** Sinh mã kế tiếp, ví dụ VC001 -> VC002. */
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
