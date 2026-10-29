package dao;

import UI.PhanQuyen;

import entity.KhachHang;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

// Lớp truy cập cơ sở dữ liệu cho khách hàng.
public class KhachHangDAO {

        // =========================================================
        // THU CUNG DANG NHAP O FORM THEM KHACH (khong dung entity.ThuCung
        // de khong dung do voi file ThuCung.java co san cua nhom)
        // =========================================================

        // Nhóm dữ liệu hoặc chức năng thú cưng mới dùng trong KhachHangDAO.
        public static class ThuCungMoi {

                // ten: Lưu tên.
                public final String ten;
                // loai: Lưu loài/loại.
                public final String loai;
                // giong: Lưu giống thú cưng.
                public final String giong;
                // gioiTinh: Lưu giới tính.
                public final String gioiTinh;
                // canNang: Lưu cân nặng.
                public final Double canNang; // null neu khong nhap

                // Khởi tạo đối tượng từ các giá trị được truyền vào.
                // Tham số ten: tên.
                // Tham số loai: loài/loại.
                // Tham số giong: giống thú cưng.
                // Tham số gioiTinh: giới tính.
                // Tham số canNang: cân nặng.
                public ThuCungMoi(String ten, String loai, String giong, String gioiTinh, Double canNang) {
                        this.ten = ten;
                        this.loai = loai;
                        this.giong = giong;
                        this.gioiTinh = gioiTinh;
                        this.canNang = canNang;
                }
        }

        // =========================================================
        // CAU SELECT CHUNG (kem thu cung, tong chi, gan nhat)
        // Can SQL Server 2017 tro len (STRING_AGG)
        // =========================================================

        // BASE_SELECT: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        private static final String BASE_SELECT = "SELECT k.maKhachHang, k.hoTen, k.soDienThoai, "
                        + "(SELECT COUNT(*) FROM ThuCung t WHERE t.maKhachHang = k.maKhachHang) AS thuCung, "
                        + "(SELECT ISNULL(SUM(ct.soLuong * ct.donGia * (1 - ISNULL(ct.phanTramGiam, 0) / 100.0)), 0) "
                        + "   FROM HoaDon h JOIN ChiTietHoaDon ct ON ct.maHoaDon = h.maHoaDon "
                        + "   WHERE h.maKhachHang = k.maKhachHang) AS tongChi, "
                        + "(SELECT MAX(h2.ngayLap) FROM HoaDon h2 WHERE h2.maKhachHang = k.maKhachHang) AS ganNhat "
                        + "FROM KhachHang k ";

        // Chuyển dòng dữ liệu hiện tại trong ResultSet thành đối tượng khách hàng.
        // Tham số rs: tập kết quả truy vấn.
        // Kết quả: đối tượng/giá trị kiểu KhachHang.
        private KhachHang map(ResultSet rs) throws SQLException {

                KhachHang kh = new KhachHang(
                                rs.getString("maKhachHang"),
                                rs.getString("hoTen"),
                                rs.getString("soDienThoai"),
                                null, // email: CSDL khong co cot nay
                                null, // diaChi: CSDL khong co cot nay
                                0); // diemTichLuy: CSDL khong co cot nay

                kh.setThuCung(rs.getString("thuCung"));
                kh.setTongChi(rs.getLong("tongChi"));

                // ts: Lưu thời điểm dưới dạng dữ liệu thời gian.
                Timestamp ts = rs.getTimestamp("ganNhat");

                if (ts != null) {
                        kh.setGanNhat(new SimpleDateFormat("dd/MM/yyyy").format(ts));
                }

                return kh;
        }

        // =========================================================
        // TIM KIEM (ten / SDT / ma)
        // =========================================================

        // Tìm kiểm.
        // Tham số tuKhoa: từ khóa tìm kiếm.
        // Kết quả: đối tượng/giá trị kiểu ArrayList<KhachHang>.
        public ArrayList<KhachHang> timKiem(String tuKhoa) {

                // list: Danh sách chứa các phần tử kiểu KhachHang.
                ArrayList<KhachHang> list = new ArrayList<>();

                // sql: Bộ đệm ghép các đoạn chuỗi để tạo câu lệnh SQL.
                StringBuilder sql = new StringBuilder(BASE_SELECT);

                sql.append("WHERE (k.hoTen LIKE ? OR k.soDienThoai LIKE ? OR k.maKhachHang LIKE ?) ");

                sql.append("ORDER BY k.maKhachHang");

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql.toString())) {

                        String kw = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";

                        ps.setString(1, kw);
                        ps.setString(2, kw);
                        ps.setString(3, kw);

                        // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
                        try (ResultSet rs = ps.executeQuery()) {
                                // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
                                while (rs.next()) {
                                        list.add(map(rs));
                                }
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list;
        }

        // =========================================================
        // LAY THEO SDT (null neu khong co)
        // =========================================================

        // Tìm khách hàng theo số điện thoại.
        // Tham số sdt: số điện thoại.
        // Kết quả: đối tượng/giá trị kiểu KhachHang.
        public KhachHang getBySdt(String sdt) {

                // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
                String sql = BASE_SELECT + "WHERE k.soDienThoai = ?";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, sdt);

                        // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        return map(rs);
                                }
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return null;
        }

        // =========================================================
        // KIEM TRA TRUNG SDT
        // =========================================================

        // Kiểm tra số điện thoại đã được sử dụng hay chưa.
        // Tham số sdt: số điện thoại.
        // Kết quả: true/false theo điều kiện xử lý.
        public boolean tonTaiSdt(String sdt) {

                // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
                String sql = "SELECT 1 FROM KhachHang WHERE soDienThoai = ?";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, sdt);

                        // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
                        try (ResultSet rs = ps.executeQuery()) {
                                return rs.next();
                        }

                } catch (SQLException e) {

                        e.printStackTrace();

                        return false;
                }
        }

        // =========================================================
        // THONG KE: {tong khach, so khach co the (0), tong diem (0), khach mua thang nay}
        // =========================================================

        // Tổng hợp các chỉ số thống kê từ cơ sở dữ liệu.
        // Kết quả: đối tượng/giá trị kiểu long[].
        public long[] thongKe() {

                // kq: Mảng lưu kết quả.
                long[] kq = new long[4];

                // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
                String sql = "SELECT "
                                + "(SELECT COUNT(*) FROM KhachHang), "
                                + "(SELECT 0), "
                                + "(SELECT 0), "
                                + "(SELECT COUNT(DISTINCT maKhachHang) FROM HoaDon "
                                + "   WHERE YEAR(ngayLap) = YEAR(GETDATE()) AND MONTH(ngayLap) = MONTH(GETDATE()))";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();
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

        // =========================================================
        // THEM KHACH HANG + THU CUNG (1 giao dich)
        // Tra ve ma khach moi, null neu loi.
        // =========================================================

        // Thêm dữ liệu khách hàng trong cơ sở dữ liệu.
        // Tham số kh: khách hàng cần xử lý.
        // Tham số thuCungs: các thú cưng.
        // Kết quả: chuỗi kết quả.
        public String them(
                        KhachHang kh,
                        ArrayList<ThuCungMoi> thuCungs) {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.KHACH_HANG);

                // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
                        con.setAutoCommit(false);

                        try {

                                // maKH: Lưu mã khách hàng.
                                String maKH = nextCode(con, "KhachHang", "maKhachHang", "KH", 3);

                                // ---- khach hang ----
                                try (PreparedStatement ps = con.prepareStatement(
                                                "INSERT INTO KhachHang "
                                                                + "(maKhachHang, hoTen, soDienThoai) "
                                                                + "VALUES (?, ?, ?)")) {

                                        ps.setString(1, maKH);
                                        ps.setString(2, kh.getHoTen());
                                        ps.setString(3, kh.getSoDienThoai());
                                        ps.executeUpdate();
                                }

                                // ---- thu cung ----
                                for (ThuCungMoi tc : thuCungs) {

                                        // maTC: Lưu mã thú cưng.
                                        String maTC = nextCode(con, "ThuCung", "maThuCung", "TC", 3);

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

                                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                                con.commit();

                                return maKH;

                        } catch (SQLException e) {

                                // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
                                con.rollback();

                                throw e;
                        }

                } catch (SQLException e) {

                        e.printStackTrace();

                        return null;
                }
        }

        // =========================================================
        // XOA KHACH HANG
        // Tra ve null neu xoa duoc, nguoc lai tra ve noi dung loi.
        // Khach da co lich hen / hoa don / phieu dich vu thi khong xoa.
        // =========================================================

        // Xử lý xóa dữ liệu khách hàng trong cơ sở dữ liệu.
        // Tham số maKhachHang: mã khách hàng.
        // Kết quả: chuỗi kết quả.
        public String xoa(String maKhachHang) {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.KHACH_HANG);

                // rangBuoc: Mảng lưu ràng buộc.
                String[][] rangBuoc = {
                                { "LichHen", "lich hen" },
                                { "HoaDon", "hoa don" },
                                { "PhieuDichVu", "phieu dich vu" } };

                // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        for (String[] r : rangBuoc) {

                                try (PreparedStatement ps = con.prepareStatement(
                                                "SELECT COUNT(*) FROM " + r[0] + " WHERE maKhachHang = ?")) {

                                        ps.setString(1, maKhachHang);

                                        // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
                                        try (ResultSet rs = ps.executeQuery()) {

                                                if (rs.next() && rs.getInt(1) > 0) {

                                                        return "Khach hang da co " + r[1]
                                                                        + " nen khong the xoa.";
                                                }
                                        }
                                }
                        }

                        // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
                        con.setAutoCommit(false);

                        try {

                                chay(con, "DELETE FROM ThuCung WHERE maKhachHang = ?", maKhachHang);
                                chay(con, "DELETE FROM KhachHang WHERE maKhachHang = ?", maKhachHang);

                                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                                con.commit();

                                return null;

                        } catch (SQLException e) {

                                // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
                                con.rollback();

                                throw e;
                        }

                } catch (SQLException e) {

                        e.printStackTrace();

                        return "Xoa that bai: " + e.getMessage();
                }
        }

        // Gán mã vào câu lệnh SQL và thực thi cập nhật qua kết nối được truyền vào.
        // Tham số con: kết nối cơ sở dữ liệu.
        // Tham số sql: câu lệnh SQL.
        // Tham số ma: mã.
        private static void chay(Connection con, String sql, String ma) throws SQLException {

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, ma);

                        ps.executeUpdate();
                }
        }

        // =========================================================
        // HAM PHU
        // =========================================================

        // Tạo mã tiếp theo dựa trên mã hiện có trong cơ sở dữ liệu.
        // Tham số con: kết nối cơ sở dữ liệu.
        // Tham số table: bảng dữ liệu.
        // Tham số col: chỉ số cột.
        // Tham số prefix: tiền tố mã.
        // Tham số width: chiều rộng.
        // Kết quả: chuỗi kết quả.
        private static String nextCode(
                        Connection con,
                        String table,
                        String col,
                        String prefix,
                        int width) throws SQLException {

                // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
                String sql = "SELECT MAX(" + col + ") FROM " + table
                                + " WHERE " + col + " LIKE ?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, prefix + "%");

                        // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
                        try (ResultSet rs = ps.executeQuery()) {

                                int n = 0;

                                if (rs.next() && rs.getString(1) != null) {

                                        try {
                                                n = Integer.parseInt(
                                                                rs.getString(1).substring(prefix.length()));
                                        } catch (NumberFormatException e) {
                                                n = 0;
                                        }
                                }

                                return prefix + String.format("%0" + width + "d", n + 1);
                        }
                }
        }
}
