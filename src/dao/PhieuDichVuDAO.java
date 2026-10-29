package dao;

import UI.PhanQuyen;

import entity.PhieuDichVu;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import ConnectDB.connectDB;

// Lớp truy cập cơ sở dữ liệu cho phiếu dịch vụ.
public class PhieuDichVuDAO {

    // Lấy toàn bộ danh sách bản ghi từ cơ sở dữ liệu.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<PhieuDichVu>.
    public ArrayList<PhieuDichVu> getAll() {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.PHIEU_DICH_VU);
        // list: Danh sách chứa các phần tử kiểu PhieuDichVu.
        ArrayList<PhieuDichVu> list = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT p.*, tc.tenThuCung, tc.giong, nv.hoTen AS tenNhanVien, kh.hoTen AS tenKhachHang, " +
                "ISNULL((SELECT STRING_AGG(CONVERT(NVARCHAR(MAX), dv.tenDichVu), N', ') FROM ChiTietDichVu ctdv JOIN DichVu dv ON ctdv.maDichVu = dv.maDichVu WHERE ctdv.maPhieuDichVu = p.maPhieuDichVu), N'') AS cacDichVu "
                +
                "FROM PhieuDichVu p " +
                "INNER JOIN ThuCung tc ON p.maThuCung = tc.maThuCung " +
                "INNER JOIN NhanVien nv ON p.maNhanVien = nv.maNhanVien " +
                "INNER JOIN KhachHang kh ON p.maKhachHang = kh.maKhachHang " +
                "ORDER BY p.maPhieuDichVu DESC";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                PhieuDichVu pdv = new PhieuDichVu();
                pdv.setMaPhieuDichVu(rs.getString("maPhieuDichVu"));
                pdv.setTrangThai(rs.getBoolean("trangThai") ? "Đã thanh toán" : "Chưa thanh toán");
                pdv.setMaKhachHang(rs.getString("maKhachHang"));
                pdv.setMaThuCung(rs.getString("maThuCung"));
                pdv.setMaNhanVien(rs.getString("maNhanVien"));
                pdv.setMaLichHen(rs.getString("maLichHen") != null ? rs.getString("maLichHen") : "Không có");
                pdv.setTenThuCung(rs.getString("tenThuCung") + " (" + rs.getString("giong") + ")");
                pdv.setTenNhanVien(rs.getString("tenNhanVien"));
                pdv.setTenKhachHang(rs.getString("tenKhachHang"));
                pdv.setCacDichVu(rs.getString("cacDichVu")); // Thêm lấy tên dịch vụ
                list.add(pdv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy toàn bộ dịch vụ để hiển thị lên Form Checkbox
    // Lấy danh sách dịch vụ.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<String[]>.
    public ArrayList<String[]> getDanhSachDichVu() {
        // list: Danh sách chứa các phần tử kiểu String[].
        ArrayList<String[]> list = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT maDichVu, tenDichVu, giaDichVu FROM DichVu ORDER BY tenDichVu";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                list.add(new String[] { rs.getString(1), rs.getString(2), rs.getString(3) });
            }
        } catch (SQLException e) {
        }
        return list;
    }

    // Lấy các dịch vụ đã chọn của 1 phiếu cụ thể
    // Lấy dịch vụ của phiếu.
    // Tham số maPhieu: mã phiếu.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<String>.
    public ArrayList<String> getDichVuCuaPhieu(String maPhieu) {
        // list: Danh sách chứa các phần tử kiểu String.
        ArrayList<String> list = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT maDichVu FROM ChiTietDichVu WHERE maPhieuDichVu = ?";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            // rs: Tập kết quả truy vấn; gọi next() để chuyển đến từng dòng dữ liệu.
            ResultSet rs = ps.executeQuery();
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next())
                list.add(rs.getString(1));
        } catch (SQLException e) {
        }
        return list;
    }

    /** Kiểm tra thú cưng thuộc khách và phiếu liên kết đúng lịch đã chọn. */
    private void kiemTraLienKet(Connection con, PhieuDichVu p) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM ThuCung WHERE maThuCung = ? AND maKhachHang = ?")) {
            ps.setString(1, p.getMaThuCung());
            ps.setString(2, p.getMaKhachHang());
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    throw new SQLException("Thú cưng không thuộc khách hàng đã chọn.");
            }
        }
        if (p.getMaLichHen() == null || "Không có".equals(p.getMaLichHen()))
            return;
        // Khong duoc lien ket MOI voi lich da qua gio. Ngoai le: khi sua phieu
        // cu thi van duoc GIU NGUYEN lich dang gan voi chinh phieu do.
        // Kiem tra ngay tai SQL Server (GETDATE) de tranh thoi diem may tram lech.
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT 1 FROM LichHen WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE maLichHen = ? AND maKhachHang = ? AND maThuCung = ? "
                + "AND maNhanVien = ? AND ISNULL(trangThai, N'Xác nhận') <> N'Đã hủy' "
                + "AND (ngayGioHen >= GETDATE() OR EXISTS ("
                + "SELECT 1 FROM PhieuDichVu pdv "
                + "WHERE pdv.maLichHen = LichHen.maLichHen "
                + "AND pdv.maPhieuDichVu = ?))";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getMaLichHen());
            ps.setString(2, p.getMaKhachHang());
            ps.setString(3, p.getMaThuCung());
            ps.setString(4, p.getMaNhanVien());
            ps.setString(5, p.getMaPhieuDichVu());
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    throw new SQLException("Lịch hẹn đã qua giờ, đã hủy hoặc không khớp thông tin phiếu.");
            }
        }
        // trung: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String trung = "SELECT 1 FROM PhieuDichVu WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE maLichHen = ? AND maPhieuDichVu <> ?";
        try (PreparedStatement ps = con.prepareStatement(trung)) {
            ps.setString(1, p.getMaLichHen());
            ps.setString(2, p.getMaPhieuDichVu());
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    throw new SQLException("Lịch hẹn này đã có phiếu dịch vụ khác.");
            }
        }
    }

    /** Lưu chi tiết trên cùng transaction với phiếu. */
    private void chenDichVu(Connection con, String maPhieu, List<String[]> ds) throws SQLException {
        if (ds == null || ds.isEmpty())
            throw new SQLException("Phiếu phải có ít nhất một dịch vụ.");
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "INSERT INTO ChiTietDichVu (maPhieuDichVu, maDichVu, soLuong, donGia, ghiChu) "
                + "VALUES (?, ?, 1, ?, N'')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String[] dv : ds) {
                ps.setString(1, maPhieu);
                ps.setString(2, dv[0]);
                ps.setBigDecimal(3, new java.math.BigDecimal(dv[2]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // Hoàn tác giao dịch và giữ lại thông tin lỗi ban đầu.
    // Tham số con: kết nối cơ sở dữ liệu.
    // Tham số ex: ngoại lệ cần xử lý.
    private static void rollback(Connection con, Exception ex) {
        try {
            // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
            con.rollback();
        } catch (SQLException rb) {
            ex.addSuppressed(rb);
        }
    }

    /**
     * Trạng thái = 0 (chưa thanh toán) khi tạo; thu ngân sẽ chuyển sang 1 sau thanh
     * toán.
     */
    public boolean them(PhieuDichVu p, List<String[]> dichVuDuocChon) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.PHIEU_DICH_VU);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "INSERT INTO PhieuDichVu "
                + "(maPhieuDichVu, trangThai, maKhachHang, maThuCung, maNhanVien, maLichHen) "
                + "VALUES (?, 0, ?, ?, ?, ?)";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection()) {
            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
            con.setAutoCommit(false);
            try {
                kiemTraLienKet(con, p);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, p.getMaPhieuDichVu());
                    ps.setString(2, p.getMaKhachHang());
                    ps.setString(3, p.getMaThuCung());
                    ps.setString(4, p.getMaNhanVien());
                    if (p.getMaLichHen() == null || "Không có".equals(p.getMaLichHen()))
                        ps.setNull(5, Types.VARCHAR);
                    else
                        ps.setString(5, p.getMaLichHen());
                    ps.executeUpdate();
                }
                chenDichVu(con, p.getMaPhieuDichVu(), dichVuDuocChon);
                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                con.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Chỉ sửa phiếu chưa thanh toán; không đổi trạng thái do ứng dụng Spa. */
    public boolean sua(PhieuDichVu p, List<String[]> dichVuDuocChon) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.PHIEU_DICH_VU);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "UPDATE PhieuDichVu SET maKhachHang=?, maThuCung=?, maNhanVien=?, maLichHen=? "
                + "WHERE maPhieuDichVu=? AND trangThai=0";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection()) {
            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
            con.setAutoCommit(false);
            try {
                kiemTraLienKet(con, p);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, p.getMaKhachHang());
                    ps.setString(2, p.getMaThuCung());
                    ps.setString(3, p.getMaNhanVien());
                    if (p.getMaLichHen() == null || "Không có".equals(p.getMaLichHen()))
                        ps.setNull(4, Types.VARCHAR);
                    else
                        ps.setString(4, p.getMaLichHen());
                    ps.setString(5, p.getMaPhieuDichVu());
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Phiếu không tồn tại hoặc đã thanh toán.");
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM ChiTietDichVu WHERE maPhieuDichVu=?")) {
                    ps.setString(1, p.getMaPhieuDichVu());
                    ps.executeUpdate();
                }
                chenDichVu(con, p.getMaPhieuDichVu(), dichVuDuocChon);
                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                con.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Xóa phiếu chưa thanh toán và chi tiết một cách nguyên tử. */
    public boolean xoa(String maPhieu) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.PHIEU_DICH_VU);
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection()) {
            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT trangThai FROM PhieuDichVu WITH (UPDLOCK, HOLDLOCK) WHERE maPhieuDichVu=?")) {
                    ps.setString(1, maPhieu);
                    // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getBoolean(1)) {
                            // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
                            con.rollback();
                            return false;
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM ChiTietDichVu WHERE maPhieuDichVu=?")) {
                    ps.setString(1, maPhieu);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM PhieuDichVu WHERE maPhieuDichVu=? AND trangThai=0")) {
                    ps.setString(1, maPhieu);
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Không xóa được phiếu.");
                }
                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                con.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Sinh mã tự động cho bản ghi mới.
    // Kết quả: chuỗi kết quả.
    public String taoMaTuDong() {
        int max = 0;
        for (PhieuDichVu p : getAll()) {
            if (p.getMaPhieuDichVu().startsWith("PDV")) {
                try {
                    int num = Integer.parseInt(p.getMaPhieuDichVu().substring(3));
                    if (num > max)
                        max = num;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("PDV%03d", max + 1);
    }

    /**
     * Chi liet ke lich hen chua qua gio, chua huy va chua gan phieu khac.
     * Khi SUA phieu, van hien lich da gan voi chinh phieu do du da qua gio.
     */
    public ArrayList<String> getDanhSachLichHen(String maPhieuDangSua) {
        // list: Danh sách chứa các phần tử kiểu String.
        ArrayList<String> list = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT lh.maLichHen FROM LichHen lh "
                + "WHERE ISNULL(lh.trangThai, N'Xác nhận') <> N'Đã hủy' "
                + "AND (lh.ngayGioHen >= GETDATE() OR EXISTS ("
                + "SELECT 1 FROM PhieuDichVu px "
                + "WHERE px.maLichHen = lh.maLichHen AND px.maPhieuDichVu = ?)) "
                + "AND NOT EXISTS (SELECT 1 FROM PhieuDichVu p "
                + "WHERE p.maLichHen = lh.maLichHen "
                + "AND (? IS NULL OR p.maPhieuDichVu <> ?)) "
                + "ORDER BY lh.ngayGioHen ASC";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            if (maPhieuDangSua == null) {
                ps.setNull(1, Types.VARCHAR);
                ps.setNull(2, Types.VARCHAR);
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(1, maPhieuDangSua);
                ps.setString(2, maPhieuDangSua);
                ps.setString(3, maPhieuDangSua);
            }
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
                while (rs.next())
                    list.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Lấy danh sách lịch hẹn.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<String>.
    public ArrayList<String> getDanhSachLichHen() {
        return getDanhSachLichHen(null);
    }
}
