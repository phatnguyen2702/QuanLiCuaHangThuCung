package dao;

import UI.PhanQuyen;

import ConnectDB.connectDB;
import entity.ThanhToan;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Lớp truy cập cơ sở dữ liệu cho thanh toán.
public class ThanhToanDao {

    // Tìm mặt hàng theo mã.
    // Tham số ma: mã.
    // Kết quả: đối tượng/giá trị kiểu List<ThanhToan>.
    public List<ThanhToan> timMatHangTheoMa(String ma) throws SQLException {
        // ketQua: Lưu kết quả; phần tử có kiểu ThanhToan.
        List<ThanhToan> ketQua = new ArrayList<>();
        for (ThanhToan item : timMatHang(ma)) {
            if (item.getMaHang().equalsIgnoreCase(ma.trim())) ketQua.add(item);
        }
        return ketQua;
    }

    // Lấy danh sách mã sản phẩm.
    // Kết quả: đối tượng/giá trị kiểu List<String>.
    public List<String> layDanhSachMaSanPham() throws SQLException {
        // ds: Danh sách chứa các phần tử kiểu String.
        List<String> ds = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT maSanPham FROM SanPham ORDER BY maSanPham";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) ds.add(rs.getString(1));
        }
        return ds;
    }

    // Lấy danh sách sản phẩm.
    // Kết quả: đối tượng/giá trị kiểu Map<String, String>.
    public Map<String, String> layDanhSachSanPham() throws SQLException {
        // sanPhams: Bảng ánh xạ khóa đến giá trị dùng cho các sản phẩm.
        Map<String, String> sanPhams = new LinkedHashMap<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT maSanPham, tenSanPham FROM SanPham ORDER BY maSanPham";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                sanPhams.put(rs.getString("maSanPham"), rs.getString("tenSanPham"));
            }
        }
        return sanPhams;
    }

    // Lấy danh sách mã khách hàng.
    // Kết quả: đối tượng/giá trị kiểu List<String>.
    public List<String> layDanhSachMaKhachHang() throws SQLException {
        // ds: Danh sách chứa các phần tử kiểu String.
        List<String> ds = new ArrayList<>();
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT maKhachHang FROM KhachHang ORDER BY maKhachHang");
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) ds.add(rs.getString(1));
        }
        return ds;
    }

    // Lấy danh sách khách hàng để hiển thị gợi ý mã và họ tên.
    public List<ThanhToan.KhachHang> layDanhSachKhachHang() throws SQLException {
        List<ThanhToan.KhachHang> ds = new ArrayList<>();
        String sql = "SELECT maKhachHang, hoTen, soDienThoai "
                + "FROM KhachHang ORDER BY maKhachHang";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new ThanhToan.KhachHang(
                        rs.getString("maKhachHang"),
                        rs.getString("hoTen"),
                        rs.getString("soDienThoai")));
            }
        }
        return ds;
    }

    // Tìm mặt hàng.
    // Tham số tuKhoa: từ khóa tìm kiếm.
    // Kết quả: đối tượng/giá trị kiểu List<ThanhToan>.
    public List<ThanhToan> timMatHang(String tuKhoa) throws SQLException {
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT TOP 30 maSanPham AS maHang, tenSanPham AS tenHang, "
                + "giaBan AS donGia FROM SanPham "
                + "WHERE maSanPham = ? OR tenSanPham LIKE ?";
        // matHangs: Lưu các mặt hàng; phần tử có kiểu ThanhToan.
        List<ThanhToan> matHangs = new ArrayList<>();

        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            // keyword: Lưu từ khóa tìm kiếm.
            String keyword = tuKhoa.trim();
            ps.setString(1, keyword);
            ps.setString(2, "%" + keyword + "%");
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
                while (rs.next()) {
                    // donGia: Giá trị đơn giá dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
                    BigDecimal donGia = rs.getBigDecimal("donGia");
                    if (donGia == null || donGia.signum() < 0) {
                        throw new SQLException("Mặt hàng " + rs.getString("maHang")
                                + " có đơn giá không hợp lệ.");
                    }
                    matHangs.add(ThanhToan.sanPham(
                            rs.getString("maHang"), rs.getString("tenHang"), donGia));
                }
            }
        }
        return matHangs;
    }

    // Lấy phiếu dịch vụ đang xử lý.
    // Tham số maKhachHang: mã khách hàng.
    // Kết quả: đối tượng/giá trị kiểu Map<String, List<ThanhToan>>.
    public Map<String, List<ThanhToan>> layPhieuDichVuDangXuLy(String maKhachHang)
            throws SQLException {
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT p.maPhieuDichVu, p.maThuCung, dv.maDichVu, dv.tenDichVu, "
                + "ctdv.soLuong, ctdv.donGia "
                + "FROM PhieuDichVu p "
                + "JOIN ChiTietDichVu ctdv ON ctdv.maPhieuDichVu = p.maPhieuDichVu "
                + "JOIN DichVu dv ON dv.maDichVu = ctdv.maDichVu "
                + "WHERE p.maKhachHang = ? AND p.trangThai = 0 "
                + "ORDER BY p.maPhieuDichVu, dv.maDichVu";
        // phieuDichVus: Bảng ánh xạ khóa đến giá trị dùng cho các phiếu dịch vụ.
        Map<String, List<ThanhToan>> phieuDichVus = new LinkedHashMap<>();
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
                while (rs.next()) {
                    // maPhieuDichVu: Lưu mã phiếu dịch vụ.
                    String maPhieuDichVu = rs.getString("maPhieuDichVu");
                    // donGia: Giá trị đơn giá dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
                    BigDecimal donGia = rs.getBigDecimal("donGia");
                    if (donGia == null || donGia.signum() < 0) {
                        throw new SQLException("Dịch vụ trong phiếu " + maPhieuDichVu
                                + " có đơn giá không hợp lệ.");
                    }
                    ThanhToan dichVu = ThanhToan.dichVu(
                            rs.getString("maDichVu"), rs.getString("tenDichVu"), donGia,
                            rs.getString("maThuCung"));
                    dichVu.setSoLuong(rs.getInt("soLuong"));
                    phieuDichVus.computeIfAbsent(maPhieuDichVu, key -> new ArrayList<>())
                            .add(dichVu);
                }
            }
        }
        return phieuDichVus;
    }

    // Tìm khách hàng.
    // Tham số maHoacSoDienThoai: mã hoặc số điện thoại.
    // Kết quả: đối tượng/giá trị kiểu ThanhToan.KhachHang.
    public ThanhToan.KhachHang timKhachHang(String maHoacSoDienThoai)
            throws SQLException {
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT TOP 1 maKhachHang, hoTen, soDienThoai "
                + "FROM KhachHang WHERE maKhachHang = ? OR soDienThoai = ?";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            // keyword: Lưu từ khóa tìm kiếm.
            String keyword = maHoacSoDienThoai.trim();
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ThanhToan.KhachHang(
                        rs.getString("maKhachHang"),
                        rs.getString("hoTen"),
                        rs.getString("soDienThoai"));
            }
        }
    }

    // Tìm voucher.
    // Tham số maHoacTen: mã hoặc tên.
    // Kết quả: đối tượng/giá trị kiểu ThanhToan.Voucher.
    public ThanhToan.Voucher timVoucher(String maHoacTen) throws SQLException {
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT maVoucher, tenVoucher, phanTramGiam, dieuKien "
                + "FROM Vocher v "
                + "WHERE " + dieuKienVoucherApDung("v")
                + " AND (UPPER(v.maVoucher) = UPPER(?) "
                + "OR UPPER(v.tenVoucher) = UPPER(?))";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            // keyword: Lưu từ khóa tìm kiếm.
            String keyword = maHoacTen.trim();
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                // phanTramGiam: Giá trị phần trăm giảm giá dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
                BigDecimal phanTramGiam = rs.getBigDecimal("phanTramGiam");
                if (phanTramGiam == null
                        || phanTramGiam.signum() < 0
                        || phanTramGiam.compareTo(BigDecimal.valueOf(100)) > 0) {
                    throw new SQLException("Phần trăm giảm của voucher không hợp lệ.");
                }
                return new ThanhToan.Voucher(
                        rs.getString("maVoucher"),
                        rs.getString("tenVoucher"),
                        phanTramGiam,
                        rs.getString("dieuKien"));
            }
        }
    }

    // Lấy danh sách voucher đang hoạt động để hiển thị gợi ý.
    public List<ThanhToan.Voucher> layDanhSachVoucher() throws SQLException {
        List<ThanhToan.Voucher> ds = new ArrayList<>();
        String sql = "SELECT maVoucher, tenVoucher, phanTramGiam, dieuKien "
                + "FROM Vocher v WHERE " + dieuKienVoucherApDung("v")
                + " ORDER BY v.maVoucher";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BigDecimal phanTramGiam = rs.getBigDecimal("phanTramGiam");
                if (phanTramGiam == null
                        || phanTramGiam.signum() < 0
                        || phanTramGiam.compareTo(BigDecimal.valueOf(100)) > 0) {
                    throw new SQLException("Phần trăm giảm của voucher "
                            + rs.getString("maVoucher") + " không hợp lệ.");
                }
                ds.add(new ThanhToan.Voucher(
                        rs.getString("maVoucher"),
                        rs.getString("tenVoucher"),
                        phanTramGiam,
                        rs.getString("dieuKien")));
            }
        }
        return ds;
    }

    private static String dieuKienVoucherApDung(String alias) {
        return alias + ".trangThai = 1 "
                + "AND (" + alias + ".ngayBatDau IS NULL "
                + "OR " + alias + ".ngayBatDau <= CAST(GETDATE() AS DATE)) "
                + "AND (" + alias + ".ngayKetThuc IS NULL "
                + "OR " + alias + ".ngayKetThuc >= CAST(GETDATE() AS DATE)) "
                + "AND (" + alias + ".soLuongToiDa IS NULL "
                + "OR (SELECT COUNT(*) FROM HoaDon h "
                + "WHERE h.maVoucher = " + alias + ".maVoucher) < "
                + alias + ".soLuongToiDa)";
    }

    // Kiểm tra khách hàng đã có thẻ thành viên hay chưa.
    // Tham số maKhachHang: mã khách hàng.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean laKhachHangThanhVien(String maKhachHang) throws SQLException {
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT 1 FROM TheThanhVien WHERE maKhachHang = ?";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Lấy phương thức thanh toán thanh toán.
    // Kết quả: đối tượng/giá trị kiểu List<ThanhToan.PhuongThuc>.
    public List<ThanhToan.PhuongThuc> layPhuongThucThanhToan()
            throws SQLException {
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT maPhuongThuc, tenPhuongThuc "
                + "FROM PhuongThucThanhToan "
                + "WHERE trangThai = 1 AND maPhuongThuc <> 'PT003' "
                + "ORDER BY maPhuongThuc";
        // phuongThucs: Lưu các phương thức thanh toán; phần tử có kiểu ThanhToan.PhuongThuc.
        List<ThanhToan.PhuongThuc> phuongThucs = new ArrayList<>();
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                phuongThucs.add(new ThanhToan.PhuongThuc(
                        rs.getString("maPhuongThuc"),
                        rs.getString("tenPhuongThuc")));
            }
        }
        return phuongThucs;
    }

    // Tạo mã hóa đơn.
    // Tham số maKhachHang: mã khách hàng.
    // Tham số ngayLap: ngày giờ lập hóa đơn.
    // Kết quả: chuỗi kết quả.
    public String taoMaHoaDon(String maKhachHang, LocalDateTime ngayLap)
            throws SQLException {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.THANH_TOAN);
        if (maKhachHang == null || ngayLap == null) {
            throw new IllegalArgumentException("Thiếu mã khách hàng hoặc ngày lập hóa đơn.");
        }

        String chuSoMaKhachHang = maKhachHang.replaceAll("\\D", "");
        if (chuSoMaKhachHang.length() < 3) {
            throw new IllegalArgumentException(
                    "Mã khách hàng phải có ít nhất 3 chữ số để tạo mã hóa đơn.");
        }

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "UPDATE BoDemHoaDon "
                + "SET giaTri = CASE WHEN giaTri >= 9999 THEN 1 ELSE giaTri + 1 END "
                + "OUTPUT INSERTED.giaTri "
                + "WHERE id = 1";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException(
                        "Không cấp được số thứ tự hóa đơn. Hãy kiểm tra bộ đếm hóa đơn.");
            }

            // thuTuMua: Lưu thứ tự mua hàng.
            int thuTuMua = rs.getInt(1);
            return "HD" + chuSoMaKhachHang.substring(0, 3)
                    + ngayLap.format(DateTimeFormatter.ofPattern("ddMMyy", Locale.ROOT))
                    + String.format(Locale.ROOT, "%04d", thuTuMua);
        }
    }

    // Tạo hóa đơn.
    // Tham số maHoaDon: mã hóa đơn.
    // Tham số maNhanVien: mã nhân viên.
    // Tham số maKhachHang: mã khách hàng.
    // Tham số maVoucher: mã voucher.
    // Tham số maPhuongThuc: mã phương thức thanh toán.
    // Tham số ngayLap: ngày giờ lập hóa đơn.
    // Tham số phanTramGiam: phần trăm giảm giá.
    // Tham số chiTiet: chi tiết.
    // Tham số maPhieuDichVus: mã phiếu dịch dịch vụ.
    // Kết quả: chuỗi kết quả.
    public String taoHoaDon(
            String maHoaDon,
            String maNhanVien,
            String maKhachHang,
            String maVoucher,
            String maPhuongThuc,
            LocalDateTime ngayLap,
            BigDecimal phanTramGiam,
            List<ThanhToan> chiTiet,
            List<String> maPhieuDichVus) throws SQLException {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.THANH_TOAN);
        if (chiTiet == null || chiTiet.isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn chưa có mặt hàng.");
        }
        if (maPhieuDichVus == null) {
            throw new IllegalArgumentException("Danh sách phiếu dịch vụ không hợp lệ.");
        }

        if (maHoaDon == null || !maHoaDon.matches("HD\\d{13}")) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        }
        // insertHoaDon: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String insertHoaDon = "INSERT INTO HoaDon "
                + "(maHoaDon, ngayLap, maPhuongThuc, maKhachHang, maNhanVien, maVoucher) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        // insertChiTiet: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String insertChiTiet = "INSERT INTO ChiTietHoaDon "
                + "(maHoaDon, maThuCung, maSanPham, maDichVu, soLuong, donGia, phanTramGiam) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        // completeServiceTicket: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String completeServiceTicket = "UPDATE PhieuDichVu SET trangThai = 1 "
                + "WHERE maPhieuDichVu = ? AND maKhachHang = ? AND trangThai = 0";

        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection()) {
            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
            con.setAutoCommit(false);
            try {
                if (maVoucher != null) {
                    String sqlVoucher = "SELECT v.maVoucher FROM Vocher v WITH (UPDLOCK, HOLDLOCK) "
                            + "WHERE v.maVoucher = ? AND " + dieuKienVoucherApDung("v");
                    try (PreparedStatement psVoucher = con.prepareStatement(sqlVoucher)) {
                        psVoucher.setString(1, maVoucher);
                        try (ResultSet rs = psVoucher.executeQuery()) {
                            if (!rs.next()) {
                                throw new SQLException(
                                        "Voucher không còn hiệu lực, chưa đến ngày bắt đầu "
                                                + "hoặc đã hết lượt sử dụng.");
                            }
                        }
                    }
                }

                // Kiem tra ton ngay trong giao dich va khoa dong san pham den khi hoa don duoc ghi.
                // soLuongCan: Bảng ánh xạ khóa đến giá trị dùng cho số lượng cần.
                java.util.Map<String, Integer> soLuongCan = new java.util.LinkedHashMap<>();
                for (ThanhToan item : chiTiet) {
                    if (item.getMaSanPham() != null) {
                        soLuongCan.merge(item.getMaSanPham(), item.getSoLuong(), Integer::sum);
                    }
                }
                // sqlTon: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
                String sqlTon = "SELECT " + SanPhamDAO.BIEU_THUC_TON + " AS tonKho FROM SanPham sp WITH (UPDLOCK, HOLDLOCK) WHERE sp.maSanPham=?";
                try (PreparedStatement psTon = con.prepareStatement(sqlTon)) {
                    for (var entry : soLuongCan.entrySet()) {
                        psTon.setString(1, entry.getKey());
                        // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
                        try (ResultSet rs = psTon.executeQuery()) {
                            if (!rs.next() || rs.getInt("tonKho") < entry.getValue()) {
                                throw new SQLException("Sản phẩm " + entry.getKey()
                                        + " không đủ tồn kho. Vui lòng tải lại giỏ hàng.");
                            }
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(insertHoaDon)) {
                    ps.setString(1, maHoaDon);
                    ps.setTimestamp(2, Timestamp.valueOf(ngayLap));
                    ps.setString(3, maPhuongThuc);
                    ps.setString(4, maKhachHang);
                    ps.setString(5, maNhanVien);
                    if (maVoucher == null) {
                        ps.setNull(6, Types.VARCHAR);
                    } else {
                        ps.setString(6, maVoucher);
                    }
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(insertChiTiet)) {
                    for (ThanhToan item : chiTiet) {
                        ps.setString(1, maHoaDon);
                        ps.setString(2, item.getMaThuCung());
                        ps.setString(3, item.getMaSanPham());
                        ps.setString(4, item.getMaDichVu());
                        ps.setInt(5, item.getSoLuong());
                        ps.setBigDecimal(6, item.getDonGia());
                        ps.setBigDecimal(7, phanTramGiam);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                try (PreparedStatement ps = con.prepareStatement(completeServiceTicket)) {
                    for (String maPhieuDichVu : maPhieuDichVus) {
                        ps.setString(1, maPhieuDichVu);
                        ps.setString(2, maKhachHang);
                        if (ps.executeUpdate() != 1) {
                            throw new SQLException("Phiếu dịch vụ " + maPhieuDichVu
                                    + " không còn ở trạng thái đang xử lý cho khách hàng này.");
                        }
                    }
                }
                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                con.commit();
                return maHoaDon;
            } catch (SQLException | RuntimeException e) {
                try {
                    // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }
}
