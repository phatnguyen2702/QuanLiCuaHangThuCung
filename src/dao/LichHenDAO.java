package dao;

import UI.PhanQuyen;

import entity.LichHen;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import ConnectDB.connectDB;

public class LichHenDAO {

    /** Mỗi lịch Spa giữ chỗ cho nhân viên 60 phút kể từ giờ bắt đầu. */
    private static final int THOI_LUONG_LICH_PHUT = 60;
    private static final DateTimeFormatter GIO_HEN_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ArrayList<String> getDanhSachDichVu() {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT tenDichVu FROM DichVu ORDER BY tenDichVu";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(rs.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================
    // LẤY TẤT CẢ LỊCH HẸN (Kèm thông tin JOIN)
    // =========================================================
    public ArrayList<LichHen> getAll() {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.LICH_HEN_SPA);
        ArrayList<LichHen> list = new ArrayList<>();

        String sql = "SELECT lh.maLichHen, lh.ngayGioHen, lh.ghiChu, lh.trangThai, " +
                "lh.maKhachHang, kh.hoTen as tenKhachHang, " +
                "lh.maThuCung, tc.tenThuCung, tc.giong, " +
                "lh.maNhanVien, nv.hoTen as tenNhanVien " +
                "FROM LichHen lh " +
                "INNER JOIN KhachHang kh ON lh.maKhachHang = kh.maKhachHang " +
                "INNER JOIN ThuCung tc ON lh.maThuCung = tc.maThuCung " +
                "INNER JOIN NhanVien nv ON lh.maNhanVien = nv.maNhanVien " +
                "ORDER BY lh.ngayGioHen DESC";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LichHen lh = new LichHen();
                lh.setMaLichHen(rs.getString("maLichHen"));

                Timestamp ts = rs.getTimestamp("ngayGioHen");
                if (ts != null) {
                    lh.setNgayGioHen(ts.toLocalDateTime());
                }

                lh.setGhiChu(rs.getString("ghiChu"));
                lh.setMaKhachHang(rs.getString("maKhachHang"));
                lh.setTenKhachHang(rs.getString("tenKhachHang"));
                lh.setMaThuCung(rs.getString("maThuCung"));

                String tenThuCungDayDu = rs.getString("tenThuCung") + " (" + rs.getString("giong") + ")";
                lh.setTenThuCung(tenThuCungDayDu);

                lh.setMaNhanVien(rs.getString("maNhanVien"));
                lh.setTenNhanVien(rs.getString("tenNhanVien"));

                lh.setTrangThai(rs.getString("trangThai") == null
                        ? "Xác nhận"
                        : rs.getString("trangThai"));

                list.add(lh);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Dùng khi chọn lịch hẹn trong phiếu dịch vụ. */
    public LichHen timTheoMa(String ma) {
        if (ma == null || ma.isBlank())
            return null;
        for (LichHen lh : getAll()) {
            if (ma.equals(lh.getMaLichHen()))
                return lh;
        }
        return null;
    }

    /** Hủy mềm để bảo toàn lịch sử, không hủy lịch đã có phiếu dịch vụ. */
    public boolean huy(String maLichHen) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.LICH_HEN_SPA);
        String sql = "UPDATE LichHen SET trangThai = N'Đã hủy' "
                + "WHERE maLichHen = ? AND ISNULL(trangThai, N'Xác nhận') <> N'Đã hủy' "
                + "AND NOT EXISTS (SELECT 1 FROM PhieuDichVu p WHERE p.maLichHen = LichHen.maLichHen)";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLichHen);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // XÓA LỊCH HẸN
    // =========================================================
    /** Tên phương thức tương thích code cũ; luôn hủy mềm, không DELETE. */
    public boolean xoa(String maLichHen) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.LICH_HEN_SPA);
        return huy(maLichHen);
    }

    // =========================================================
    // KIỂM TRA GIỜ HẸN VÀ TRÙNG LỊCH NHÂN VIÊN SPA
    // =========================================================

    /**
     * Hàm này được gọi khi THÊM hoặc SỬA, ngay trong transaction lưu.
     * - Cấm giờ hiện tại / giờ trong quá khứ.
     * - Một thợ Spa chỉ có một lịch trong mỗi khoảng 60 phút.
     * - Bỏ qua lịch đã hủy và chính lịch đang sửa.
     * Khóa UPDLOCK + HOLDLOCK dưới SERIALIZABLE tránh hai nhân viên cùng
     * đặt trùng trong lúc thao tác đồng thời ở hai máy.
     */
    private void kiemTraLichHopLe(Connection con, LichHen lh) throws SQLException {
        if (lh == null || lh.getNgayGioHen() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày và giờ hẹn hợp lệ.");
        }
        if (lh.getMaNhanVien() == null || lh.getMaNhanVien().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn nhân viên Spa.");
        }

        LocalDateTime ngayGio = lh.getNgayGioHen();
        if (!ngayGio.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Không được đặt hoặc chuyển lịch sang thời gian đã qua.");
        }

        // Kiểm tra theo đồng hồ SQL Server lần nữa, ngay trước khi lưu.
        String sqlQuaGio = "SELECT CASE WHEN ? <= SYSDATETIME() THEN 1 ELSE 0 END";
        try (PreparedStatement ps = con.prepareStatement(sqlQuaGio)) {
            ps.setTimestamp(1, Timestamp.valueOf(ngayGio));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 1) {
                    throw new IllegalArgumentException("Giờ hẹn đã qua. Vui lòng chọn thời gian mới.");
                }
            }
        }

        // Hai lịch cách nhau dưới 60 phút sẽ chồng nhau (ví dụ 09:00 và 09:30).
        String sqlTrung = "SELECT TOP (1) maLichHen, ngayGioHen "
                + "FROM LichHen WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE maNhanVien = ? "
                + "AND ISNULL(trangThai, N'Xác nhận') <> N'Đã hủy' "
                + "AND maLichHen <> ? "
                + "AND ngayGioHen > DATEADD(MINUTE, ?, ?) "
                + "AND ngayGioHen < DATEADD(MINUTE, ?, ?) "
                + "ORDER BY ngayGioHen";
        try (PreparedStatement ps = con.prepareStatement(sqlTrung)) {
            ps.setString(1, lh.getMaNhanVien());
            ps.setString(2, lh.getMaLichHen() == null ? "" : lh.getMaLichHen());
            ps.setInt(3, -THOI_LUONG_LICH_PHUT);
            ps.setTimestamp(4, Timestamp.valueOf(ngayGio));
            ps.setInt(5, THOI_LUONG_LICH_PHUT);
            ps.setTimestamp(6, Timestamp.valueOf(ngayGio));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp gioDaCo = rs.getTimestamp("ngayGioHen");
                    String gioTrung = gioDaCo == null ? "" : gioDaCo.toLocalDateTime().format(GIO_HEN_FORMAT);
                    throw new IllegalStateException("Nhân viên Spa này đã có lịch "
                            + rs.getString("maLichHen") + " vào " + gioTrung + ". "
                            + "Mỗi lịch chiếm 60 phút. Hãy chọn giờ cách lịch hiện có ít nhất 60 phút "
                            + "hoặc chọn nhân viên Spa khác.");
                }
            }
        }
    }

    private void rollbackAnToan(Connection con, Exception loiBanDau) {
        try {
            con.rollback();
        } catch (SQLException ex) {
            loiBanDau.addSuppressed(ex);
        }
    }

    // =========================================================
    // THÊM LỊCH HẸN
    // =========================================================
    public boolean them(LichHen lh) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.LICH_HEN_SPA);
        if (lh == null) {
            throw new IllegalArgumentException("Thông tin lịch hẹn không hợp lệ.");
        }
        if (!thuCungThuocKhach(lh.getMaThuCung(), lh.getMaKhachHang()))
            return false;
        String sql = "INSERT INTO LichHen (maLichHen, ngayGioHen, ghiChu, maKhachHang, maNhanVien, maThuCung, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                kiemTraLichHopLe(con, lh);
                int soDong;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, lh.getMaLichHen());
                    ps.setTimestamp(2, Timestamp.valueOf(lh.getNgayGioHen()));
                    ps.setString(3, lh.getGhiChu());
                    ps.setString(4, lh.getMaKhachHang());
                    ps.setString(5, lh.getMaNhanVien());
                    ps.setString(6, lh.getMaThuCung());
                    ps.setString(7, lh.getTrangThai() == null ? "Xác nhận" : lh.getTrangThai());
                    soDong = ps.executeUpdate();
                }
                con.commit();
                return soDong == 1;
            } catch (SQLException | RuntimeException ex) {
                rollbackAnToan(con, ex);
                throw ex;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // SỬA LỊCH HẸN
    // =========================================================
    public boolean sua(LichHen lh) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.LICH_HEN_SPA);
        if (lh == null) {
            throw new IllegalArgumentException("Thông tin lịch hẹn không hợp lệ.");
        }
        if (!thuCungThuocKhach(lh.getMaThuCung(), lh.getMaKhachHang()))
            return false;
        String sql = "UPDATE LichHen SET ngayGioHen = ?, ghiChu = ?, maKhachHang = ?, maNhanVien = ?, maThuCung = ? "
                + "WHERE maLichHen = ? AND ISNULL(trangThai, N'Xác nhận') <> N'Đã hủy'";
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                kiemTraLichHopLe(con, lh);
                int soDong;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(lh.getNgayGioHen()));
                    ps.setString(2, lh.getGhiChu());
                    ps.setString(3, lh.getMaKhachHang());
                    ps.setString(4, lh.getMaNhanVien());
                    ps.setString(5, lh.getMaThuCung());
                    ps.setString(6, lh.getMaLichHen());
                    soDong = ps.executeUpdate();
                }
                con.commit();
                return soDong == 1;
            } catch (SQLException | RuntimeException ex) {
                rollbackAnToan(con, ex);
                throw ex;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean thuCungThuocKhach(String maTC, String maKH) {
        String sql = "SELECT 1 FROM ThuCung WHERE maThuCung = ? AND maKhachHang = ?";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTC);
            ps.setString(2, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Các hàm lấy danh sách cho ComboBox
    public ArrayList<String[]> getDanhSachKhachHang() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT maKhachHang, hoTen FROM KhachHang";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[] { rs.getString(1), rs.getString(2) });
            }
        } catch (SQLException e) {
        }
        return list;
    }

    public ArrayList<String[]> getDanhSachNhanVienSpa() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT maNhanVien, hoTen FROM NhanVien WHERE vaiTro = N'Nhan vien Spa'";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[] { rs.getString(1), rs.getString(2) });
            }
        } catch (SQLException e) {
        }
        return list;
    }

    /** Chỉ hiển thị thú cưng thuộc khách hàng đang chọn. */
    public ArrayList<String[]> getDanhSachThuCungTheoKhachHang(String maKhachHang) {
        ArrayList<String[]> list = new ArrayList<>();
        if (maKhachHang == null || maKhachHang.isBlank())
            return list;
        String sql = "SELECT maThuCung, tenThuCung FROM ThuCung WHERE maKhachHang = ? ORDER BY tenThuCung";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new String[] { rs.getString(1), rs.getString(2) });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<String[]> getDanhSachThuCung() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT maThuCung, tenThuCung FROM ThuCung";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[] { rs.getString(1), rs.getString(2) });
            }
        } catch (SQLException e) {
        }
        return list;
    }
}
