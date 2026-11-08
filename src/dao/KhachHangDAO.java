package dao;

import control.PhanQuyenControl;
import entity.KhachHang;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * DAO khách hàng (đã gộp KhachHangDAO + KhachHangChiTietDAO).
 * Chỉ làm 3 việc: chạy SQL, kiểm tra phân quyền, kiểm tra độ dài chuỗi.
 * Kiểm tra rỗng / sai định dạng là việc của KhachHangControl.
 *
 * Quy ước: các hàm ghi (them / sua / xoa) trả về null nếu thành công,
 * ngược lại trả về câu thông báo lỗi.
 */
public class KhachHangDAO {

    /** Dữ liệu 1 thú cưng mới khi thêm khách hàng. */
    public static class ThuCungMoi {
        public final String ten;
        public final String loai;
        public final String giong;
        public final String gioiTinh;
        public final Double canNang; // null = chưa nhập

        public ThuCungMoi(String ten, String loai, String giong, String gioiTinh, Double canNang) {
            this.ten = ten;
            this.loai = loai;
            this.giong = giong;
            this.gioiTinh = gioiTinh;
            this.canNang = canNang;
        }
    }

    // Độ dài tối đa của cột trong CSDL (sửa lại nếu CSDL của bạn khác)
    private static final int MAX_TEN_KH = 50;
    private static final int MAX_SDT = 11;
    private static final int MAX_TEN_THU_CUNG = 50;
    private static final int MAX_GIONG = 25;

    private static final String BASE_SELECT = "SELECT k.maKhachHang, k.hoTen, k.soDienThoai, "
            + "(SELECT COUNT(*) FROM ThuCung t WHERE t.maKhachHang = k.maKhachHang) AS thuCung, "
            + "(SELECT ISNULL(SUM(ct.soLuong * ct.donGia * (1 - ISNULL(ct.phanTramGiam, 0) / 100.0)), 0) "
            + "   FROM HoaDon h JOIN ChiTietHoaDon ct ON ct.maHoaDon = h.maHoaDon "
            + "   WHERE h.maKhachHang = k.maKhachHang) AS tongChi, "
            + "(SELECT MAX(h2.ngayLap) FROM HoaDon h2 WHERE h2.maKhachHang = k.maKhachHang) AS ganNhat "
            + "FROM KhachHang k ";

    // =====================================================================
    // TRUY VẤN (đọc dữ liệu)
    // =====================================================================

    /** Tìm theo mã / tên / số điện thoại (để trống = lấy tất cả). */
    public ArrayList<KhachHang> timKiem(String tuKhoa) {
        ArrayList<KhachHang> list = new ArrayList<>();

        String sql = BASE_SELECT
                + "WHERE (k.hoTen LIKE ? OR k.soDienThoai LIKE ? OR k.maKhachHang LIKE ?) "
                + "ORDER BY k.maKhachHang";

        try (Connection con = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            String kw = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(docKhachHang(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lấy 1 khách hàng theo số điện thoại (không có thì trả về null). */
    public KhachHang getBySdt(String sdt) {
        String sql = BASE_SELECT + "WHERE k.soDienThoai = ?";

        try (Connection con = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return docKhachHang(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Danh sách thú cưng của 1 khách hàng, mỗi phần tử: { tên, loại, giống, giới tính, cân nặng }. */
    public ArrayList<String[]> getThuCung(String maKhachHang) {
        ArrayList<String[]> list = new ArrayList<>();

        String sql = "SELECT tenThuCung, loai, giong, gioiTinh, canNang "
                + "FROM ThuCung WHERE maKhachHang = ? ORDER BY maThuCung";

        try (Connection con = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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

    /** Số điện thoại này đã có khách hàng nào dùng chưa? (dùng khi thêm) */
    public boolean tonTaiSdt(String sdt) {
        return tonTai("SELECT 1 FROM KhachHang WHERE soDienThoai = ?", sdt);
    }

    /** Số điện thoại này đã có khách hàng KHÁC dùng chưa? (dùng khi sửa) */
    public boolean tonTaiSdtKhac(String sdt, String maKhachHang) {
        return tonTai("SELECT 1 FROM KhachHang WHERE soDienThoai = ? AND maKhachHang <> ?",
                sdt, maKhachHang);
    }

    /** Thống kê: [0] tổng khách, [1] và [2] chưa dùng, [3] khách có mua trong tháng này. */
    public long[] thongKe() {
        long[] kq = new long[4];

        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM KhachHang), "
                + "(SELECT 0), "
                + "(SELECT 0), "
                + "(SELECT COUNT(DISTINCT maKhachHang) FROM HoaDon "
                + "   WHERE YEAR(ngayLap) = YEAR(GETDATE()) AND MONTH(ngayLap) = MONTH(GETDATE()))";

        try (Connection con = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                for (int i = 0; i < 4; i++) {
                    kq[i] = rs.getLong(i + 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kq;
    }

    // =====================================================================
    // GHI DỮ LIỆU (thêm / sửa / xóa) - trả về null nếu thành công
    // =====================================================================

    /**
     * Thêm khách hàng kèm danh sách thú cưng (trong 1 transaction).
     * Thành công: mã khách hàng mới được gán vào kh (kh.getMaKhachHang()).
     */
    public String them(KhachHang kh, ArrayList<ThuCungMoi> dsThuCung) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.KHACH_HANG);

        String loi = kiemTraDoDai(kh);
        for (ThuCungMoi tc : dsThuCung) {
            if (loi != null) {
                break;
            }
            loi = kiemTraDoDai(tc);
        }
        if (loi != null) {
            return loi;
        }

        try (Connection con = ConnectDB.connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String maKH = taoMa(con, "KhachHang", "maKhachHang", "KH", 3);

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai) VALUES (?, ?, ?)")) {
                    ps.setString(1, maKH);
                    ps.setString(2, kh.getHoTen());
                    ps.setString(3, kh.getSoDienThoai());
                    ps.executeUpdate();
                }

                for (ThuCungMoi tc : dsThuCung) {
                    String maTC = taoMa(con, "ThuCung", "maThuCung", "TC", 3);

                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO ThuCung "
                                    + "(maThuCung, tenThuCung, loai, giong, gioiTinh, canNang, maKhachHang) "
                                    + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setString(1, maTC);
                        ps.setString(2, tc.ten);
                        ps.setString(3, tc.loai);
                        ps.setString(4, tc.giong);
                        ps.setString(5, tc.gioiTinh);
                        if (tc.canNang == null) {
                            ps.setNull(6, Types.DECIMAL);
                        } else {
                            ps.setDouble(6, tc.canNang);
                        }
                        ps.setString(7, maKH);
                        ps.executeUpdate();
                    }
                }

                con.commit();
                kh.setMaKhachHang(maKH);
                return null;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Lưu thất bại: " + e.getMessage();
        }
    }

    /** Sửa họ tên + số điện thoại của khách hàng. */
    public String sua(KhachHang kh) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.KHACH_HANG);

        String loi = kiemTraDoDai(kh);
        if (loi != null) {
            return loi;
        }

        String sql = "UPDATE KhachHang SET hoTen = ?, soDienThoai = ? WHERE maKhachHang = ?";

        try (Connection con = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, kh.getHoTen());
            ps.setString(2, kh.getSoDienThoai());
            ps.setString(3, kh.getMaKhachHang());

            if (ps.executeUpdate() == 0) {
                return "Không tìm thấy khách hàng " + kh.getMaKhachHang() + ".";
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Lưu thất bại: " + e.getMessage();
        }
    }

    /** Xóa khách hàng (cùng thú cưng). Không xóa được nếu đã có lịch hẹn / hóa đơn / phiếu dịch vụ. */
    public String xoa(String maKhachHang) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.KHACH_HANG);

        // { tên bảng, tên hiển thị trong thông báo }
        String[][] rangBuoc = {
                { "LichHen", "lịch hẹn" },
                { "HoaDon", "hóa đơn" },
                { "PhieuDichVu", "phiếu dịch vụ" } };

        try (Connection con = ConnectDB.connectDB.getConnection()) {

            for (String[] r : rangBuoc) {
                if (tonTai("SELECT 1 FROM " + r[0] + " WHERE maKhachHang = ?", maKhachHang)) {
                    return "Khách hàng đã có " + r[1] + " nên không thể xóa.";
                }
            }

            con.setAutoCommit(false);
            try {
                chay(con, "DELETE FROM ThuCung WHERE maKhachHang = ?", maKhachHang);
                chay(con, "DELETE FROM KhachHang WHERE maKhachHang = ?", maKhachHang);
                con.commit();
                return null;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Xóa thất bại: " + e.getMessage();
        }
    }

    // =====================================================================
    // HÀM PHỤ (private)
    // =====================================================================

    private String kiemTraDoDai(KhachHang kh) {
        if (kh.getHoTen() != null && kh.getHoTen().length() > MAX_TEN_KH) {
            return "Họ tên tối đa " + MAX_TEN_KH + " ký tự.";
        }
        if (kh.getSoDienThoai() != null && kh.getSoDienThoai().length() > MAX_SDT) {
            return "Số điện thoại tối đa " + MAX_SDT + " ký tự.";
        }
        return null;
    }

    private String kiemTraDoDai(ThuCungMoi tc) {
        if (tc.ten != null && tc.ten.length() > MAX_TEN_THU_CUNG) {
            return "Tên thú cưng tối đa " + MAX_TEN_THU_CUNG + " ký tự.";
        }
        if (tc.giong != null && tc.giong.length() > MAX_GIONG) {
            return "Giống thú cưng tối đa " + MAX_GIONG + " ký tự.";
        }
        return null;
    }

    /** Đọc 1 dòng kết quả của BASE_SELECT thành KhachHang. */
    private KhachHang docKhachHang(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang(
                rs.getString("maKhachHang"),
                rs.getString("hoTen"),
                rs.getString("soDienThoai"),
                null, null, 0);

        kh.setThuCung(rs.getString("thuCung"));
        kh.setTongChi(rs.getLong("tongChi"));

        Timestamp ts = rs.getTimestamp("ganNhat");
        if (ts != null) {
            kh.setGanNhat(new SimpleDateFormat("dd/MM/yyyy").format(ts));
        }
        return kh;
    }

    /** Chạy câu SELECT 1 ... WHERE ...: có dòng nào khớp không? (các ? là chuỗi) */
    private boolean tonTai(String sql, String... thamSo) {
        try (Connection con = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < thamSo.length; i++) {
                ps.setString(i + 1, thamSo[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Chạy câu DELETE/UPDATE có 1 tham số mã. */
    private static void chay(Connection con, String sql, String ma) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ma);
            ps.executeUpdate();
        }
    }

    /** Sinh mã kế tiếp, ví dụ KH001 -> KH002. */
    private static String taoMa(Connection con, String bang, String cot, String tienTo, int doRong)
            throws SQLException {

        String sql = "SELECT MAX(" + cot + ") FROM " + bang + " WHERE " + cot + " LIKE ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tienTo + "%");

            try (ResultSet rs = ps.executeQuery()) {
                int n = 0;
                if (rs.next() && rs.getString(1) != null) {
                    try {
                        n = Integer.parseInt(rs.getString(1).substring(tienTo.length()));
                    } catch (NumberFormatException e) {
                        n = 0;
                    }
                }
                return tienTo + String.format("%0" + doRong + "d", n + 1);
            }
        }
    }
}
