package dao;

import UI.PhanQuyen;

import ConnectDB.connectDB;
import entity.ThuCung;
import java.sql.*;
import java.util.ArrayList;

// Lớp truy cập cơ sở dữ liệu cho thú cưng.
public class ThuCungDAO {

    // Nhóm dữ liệu hoặc chức năng hồ sơ thú cưng dùng trong ThuCungDAO.
    public static class HoSoThuCung {
        // thuCung: Đối tượng thú cưng có kiểu ThuCung.
        public final ThuCung thuCung;
        // tenKhachHang: Lưu tên khách hàng.
        public final String tenKhachHang;
        // soDienThoai: Lưu số điện thoại.
        public final String soDienThoai;
        /** Đúng khi thú cưng có lịch hẹn sắp tới chưa hủy. */
        public final boolean coLichHen;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số thuCung: thú cưng.
        // Tham số tenKhachHang: tên khách hàng.
        // Tham số soDienThoai: số điện thoại.
        // Tham số coLichHen: thú cưng có lịch hẹn.
        public HoSoThuCung(ThuCung thuCung, String tenKhachHang,
                String soDienThoai, boolean coLichHen) {
            this.thuCung = thuCung;
            this.tenKhachHang = tenKhachHang;
            this.soDienThoai = soDienThoai;
            this.coLichHen = coLichHen;
        }
    }

    // Nhóm dữ liệu hoặc chức năng chủ nuôi dùng trong ThuCungDAO.
    public static class ChuNuoi {
        // maKhachHang: Lưu mã khách hàng.
        public final String maKhachHang;
        // hoTen: Lưu họ tên.
        public final String hoTen;
        // soDienThoai: Lưu số điện thoại.
        public final String soDienThoai;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số maKhachHang: mã khách hàng.
        // Tham số hoTen: họ tên.
        // Tham số soDienThoai: số điện thoại.
        public ChuNuoi(String maKhachHang, String hoTen, String soDienThoai) {
            this.maKhachHang = maKhachHang;
            this.hoTen = hoTen;
            this.soDienThoai = soDienThoai;
        }

        // Trả về chuỗi đại diện của đối tượng để hiển thị.
        @Override
        public String toString() {
            return maKhachHang + " - " + hoTen + " - " + soDienThoai;
        }
    }

    // Chuyển mã trạng thái thành tên dễ đọc trên giao diện.
    // Tham số trangThai: trạng thái.
    // Kết quả: chuỗi kết quả.
    public static String tenTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()
                || "Đang hoạt động".equalsIgnoreCase(trangThai)) {
            return "Đang theo dõi";
        }
        if ("Không hoạt động".equalsIgnoreCase(trangThai)) {
            return "Ngừng theo dõi";
        }
        return trangThai;
    }

    // Tạo thú cưng.
    // Tham số rs: tập kết quả truy vấn.
    // Kết quả: đối tượng/giá trị kiểu ThuCung.
    private ThuCung taoThuCung(ResultSet rs) throws SQLException {
        return new ThuCung(rs.getString("maThuCung"), rs.getString("tenThuCung"),
                rs.getString("loai"), rs.getString("giong"), rs.getString("gioiTinh"),
                rs.getDouble("canNang"), rs.getString("maKhachHang"),
                tenTrangThai(rs.getString("trangThai")));
    }

    // Lấy danh sách hồ sơ thú cưng kèm thông tin chủ nuôi và lịch hẹn.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<HoSoThuCung>.
    public ArrayList<HoSoThuCung> getAllChiTiet() {
        // list: Danh sách chứa các phần tử kiểu HoSoThuCung.
        ArrayList<HoSoThuCung> list = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT t.maThuCung,t.tenThuCung,t.loai,t.giong,t.gioiTinh,"
                + "t.canNang,t.maKhachHang,t.trangThai,k.hoTen AS tenKhachHang,"
                + "k.soDienThoai, CASE WHEN EXISTS ("
                + "SELECT 1 FROM LichHen lh WHERE lh.maThuCung = t.maThuCung "
                + "AND lh.ngayGioHen >= GETDATE() "
                + "AND ISNULL(lh.trangThai, N'Xác nhận') NOT IN (N'Đã hủy', N'Đã huỷ')"
                + ") THEN 1 ELSE 0 END AS coLichHen FROM ThuCung t "
                + "LEFT JOIN KhachHang k ON k.maKhachHang=t.maKhachHang "
                + "ORDER BY t.maThuCung DESC";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                list.add(new HoSoThuCung(taoThuCung(rs),
                        rs.getString("tenKhachHang"), rs.getString("soDienThoai"),
                        rs.getInt("coLichHen") == 1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy toàn bộ danh sách bản ghi từ cơ sở dữ liệu.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<ThuCung>.
    public ArrayList<ThuCung> getAll() {
        // list: Danh sách chứa các phần tử kiểu ThuCung.
        ArrayList<ThuCung> list = new ArrayList<>();
        for (HoSoThuCung hs : getAllChiTiet()) {
            list.add(hs.thuCung);
        }
        return list;
    }

    // Lấy danh sách chủ nuôi.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<ChuNuoi>.
    public ArrayList<ChuNuoi> getDanhSachChuNuoi() {
        // ds: Danh sách chứa các phần tử kiểu ChuNuoi.
        ArrayList<ChuNuoi> ds = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT maKhachHang, hoTen, soDienThoai FROM KhachHang "
                + "ORDER BY hoTen, maKhachHang";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                ds.add(new ChuNuoi(rs.getString("maKhachHang"), rs.getString("hoTen"),
                        rs.getString("soDienThoai")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Thêm dữ liệu thú cưng trong cơ sở dữ liệu.
    // Tham số tc: thú cưng cần xử lý.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean them(ThuCung tc) {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.THU_CUNG);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "INSERT INTO ThuCung "
                + "(maThuCung,tenThuCung,loai,giong,gioiTinh,canNang,maKhachHang,trangThai) "
                + "VALUES(?,?,?,?,?,?,?,?)";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tc.getMaThuCung());
            ps.setString(2, tc.getTenThuCung());
            ps.setString(3, tc.getLoai());
            ps.setString(4, tc.getGiong());
            ps.setString(5, tc.getGioiTinh());
            ps.setDouble(6, tc.getCanNang());
            ps.setString(7, tc.getMaKhachHang());
            ps.setString(8, tenTrangThai(tc.getTrangThai()));
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Khong cho chuyen chu nuoi neu thu cung da co lich hen, phieu dich vu
     * hoac chi tiet hoa don, tranh khong khop du lieu lich su.
     */
    public boolean daCoLichSu(String maThuCung) {
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM LichHen WHERE maThuCung=?) "
                + "OR EXISTS (SELECT 1 FROM PhieuDichVu WHERE maThuCung=?) "
                + "OR EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maThuCung=?) "
                + "THEN 1 ELSE 0 END";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++)
                ps.setString(i, maThuCung);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Khong xac nhan duoc tinh trang lich su thi mac dinh khoa.
            return true;
        }
    }

    // Cập nhật dữ liệu thú cưng trong cơ sở dữ liệu.
    // Tham số tc: thú cưng cần xử lý.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean sua(ThuCung tc) {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.THU_CUNG);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "UPDATE ThuCung SET tenThuCung=?, loai=?, giong=?, "
                + "gioiTinh=?, canNang=?, maKhachHang=?, trangThai=? WHERE maThuCung=? "
                + "AND (maKhachHang=? OR ("
                + "NOT EXISTS (SELECT 1 FROM LichHen WHERE maThuCung=?) "
                + "AND NOT EXISTS (SELECT 1 FROM PhieuDichVu WHERE maThuCung=?) "
                + "AND NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maThuCung=?)))";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tc.getTenThuCung());
            ps.setString(2, tc.getLoai());
            ps.setString(3, tc.getGiong());
            ps.setString(4, tc.getGioiTinh());
            ps.setDouble(5, tc.getCanNang());
            ps.setString(6, tc.getMaKhachHang());
            ps.setString(7, tenTrangThai(tc.getTrangThai()));
            ps.setString(8, tc.getMaThuCung());
            ps.setString(9, tc.getMaKhachHang());
            ps.setString(10, tc.getMaThuCung());
            ps.setString(11, tc.getMaThuCung());
            ps.setString(12, tc.getMaThuCung());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xử lý xóa dữ liệu thú cưng trong cơ sở dữ liệu.
    // Tham số maThuCung: mã thú cưng.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean xoa(String maThuCung) {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.THU_CUNG);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "DELETE FROM ThuCung WHERE maThuCung=?";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maThuCung);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            // FK to LichHen, PhieuDichVu, ChiTietHoaDon: do not delete history.
            return false;
        }
    }

    /**
     * Sinh mã thú cưng theo dạng TC001, TC002,..., TC999.
     * Mã cũ TC0001 (nếu có) vẫn được tính khi tìm số tiếp theo;
     * không thay đổi mã đã lưu để tránh ảnh hưởng các khóa ngoại.
     */
    public String taoMaThuCungTuDong() {
        int max = 0;
        for (ThuCung tc : getAll()) {
            // ma: Lưu mã.
            String ma = tc.getMaThuCung();
            if (ma != null && ma.matches("TC\\d{3,}")) {
                try {
                    max = Math.max(max, Integer.parseInt(ma.substring(2)));
                } catch (NumberFormatException ignore) {
                    // Bỏ qua mã không hợp lệ hoặc quá lớn.
                }
            }
        }
        if (max >= 999) {
            throw new IllegalStateException(
                    "Đã sử dụng hết mã thú cưng TC001–TC999. "
                            + "Cần mở rộng quy tắc mã trước khi thêm hồ sơ mới.");
        }
        return String.format("TC%03d", max + 1);
    }
}
