package dao;

import ConnectDB.connectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Sinh mã theo dữ liệu thật trong SQL Server.
 * Mã chính thức được lấy và INSERT trong cùng giao dịch, dùng TABLOCKX/HOLDLOCK
 * để hai máy đang thêm đồng thời không lấy cùng một mã.
 * Mã hiển thị trên form chỉ là dự kiến; mã sau khi lưu mới là mã chính thức.
 */
public final class MaTuDongDAO {
    private MaTuDongDAO() {
    }

    public enum Loai {
        CA_LAM("CaLam", "maCa", "CA", 3, 999),
        NHAN_VIEN("NhanVien", "maNhanVien", "NV", 3, 999),
        PHAN_CA("LichPhanCa", "maPhanCa", "PC", 3, 99999999);

        final String bang;
        final String cot;
        final String tienTo;
        final int soChuSo;
        final int gioiHan;

        Loai(String bang, String cot, String tienTo, int soChuSo, int gioiHan) {
            this.bang = bang;
            this.cot = cot;
            this.tienTo = tienTo;
            this.soChuSo = soChuSo;
            this.gioiHan = gioiHan;
        }
    }

    /** Gợi ý mã để hiển thị; có thể thay đổi nếu máy khác thêm trước. */
    public static String goiY(Loai loai) {
        try (Connection con = connectDB.getConnection()) {
            return maTiepTheo(con, loai, false);
        } catch (SQLException ex) {
            return "Tự động khi lưu";
        }
    }

    /** Phải gọi sau setAutoCommit(false), trước INSERT và COMMIT. */
    public static String taoMaTrongGiaoDich(Connection con, Loai loai) throws SQLException {
        if (con.getAutoCommit()) {
            throw new SQLException("Phải bắt đầu transaction trước khi sinh mã " + loai.tienTo);
        }
        return maTiepTheo(con, loai, true);
    }

    private static String maTiepTheo(Connection con, Loai loai, boolean khoaBang) throws SQLException {
        // Các tên bảng/cột là hằng số enum, không lấy từ dữ liệu người dùng.
        String kieuSo = loai == Loai.PHAN_CA
                ? loai.cot + " LIKE 'PC[0-9]%' AND SUBSTRING(" + loai.cot
                        + ", 3, 8) NOT LIKE '%[^0-9]%'"
                : loai.cot + " LIKE '" + loai.tienTo + "[0-9][0-9][0-9]'";
        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(" + loai.cot
                + ", 3, 8))), 0) AS soMax FROM dbo." + loai.bang
                + (khoaBang ? " WITH (TABLOCKX, HOLDLOCK)" : "")
                + " WHERE " + kieuSo;
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (!rs.next())
                throw new SQLException("Không đọc được mã mới cho " + loai.bang);
            int tiepTheo = rs.getInt(1) + 1;
            if (tiepTheo > loai.gioiHan) {
                throw new SQLException("Đã hết mã " + loai.tienTo
                        + " trong giới hạn hiện tại. Hãy điều chỉnh quy ước mã trước khi thêm.");
            }
            return loai.tienTo + String.format(java.util.Locale.ROOT,
                    "%0" + loai.soChuSo + "d", tiepTheo);
        }
    }
}
