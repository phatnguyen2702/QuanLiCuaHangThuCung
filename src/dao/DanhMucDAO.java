package dao;

import UI.PhanQuyen;

import ConnectDB.connectDB;
import entity.DanhMuc;
import entity.SanPham;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cua man hinh "Danh muc": danh muc + cac thao tac ma form Them/Sua danh muc
 * can (liet ke san pham trong danh muc, ghi them so luong nhap kho).
 */
public class DanhMucDAO {

    // Lay danh muc kem so san pham, loc theo ma hoac ten
    // Tìm kiểm.
    // Tham số tuKhoa: từ khóa tìm kiếm.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<DanhMuc>.
    public ArrayList<DanhMuc> timKiem(String tuKhoa) throws SQLException {

        // list: Danh sách chứa các phần tử kiểu DanhMuc.
        ArrayList<DanhMuc> list = new ArrayList<>();

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = """
                SELECT dm.maDanhMuc,
                       dm.tenDanhMuc,
                       dm.moTa,
                       COUNT(t.maSanPham) AS soSanPham,
                       ISNULL(SUM(t.ton), 0) AS tongTon
                FROM DanhMuc dm
                LEFT JOIN (
                    SELECT sp.maSanPham, sp.maDanhMuc,
                """ + SanPhamDAO.BIEU_THUC_TON + """
                           AS ton
                    FROM SanPham sp
                ) t ON t.maDanhMuc = dm.maDanhMuc
                WHERE dm.maDanhMuc LIKE ?
                   OR dm.tenDanhMuc LIKE ?
                GROUP BY dm.maDanhMuc, dm.tenDanhMuc, dm.moTa
                ORDER BY dm.maDanhMuc
                """;

        String kw = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, kw);
            ps.setString(2, kw);

            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
                while (rs.next()) {
                    DanhMuc dm = new DanhMuc();
                    dm.setMaDanhMuc(rs.getString("maDanhMuc"));
                    dm.setTenDanhMuc(rs.getString("tenDanhMuc"));
                    dm.setMoTa(rs.getString("moTa"));
                    dm.setSoSanPham(rs.getInt("soSanPham"));
                    dm.setTongTon(rs.getInt("tongTon"));
                    list.add(dm);
                }
            }
        }

        return list;
    }

    // Lấy toàn bộ danh sách bản ghi từ cơ sở dữ liệu.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<DanhMuc>.
    public ArrayList<DanhMuc> getAll() throws SQLException {
        return timKiem("");
    }

    // Đếm toàn bộ dữ liệu.
    // Kết quả: giá trị số nguyên.
    public int demTatCa() throws SQLException {

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT COUNT(*) FROM DanhMuc";

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Lấy mã mới để hiển thị khi lập bản ghi.
    // Kết quả: chuỗi kết quả.
    public String taoMaMoi() throws SQLException {
        return taoMa("DanhMuc", "maDanhMuc", "DM", 3);
    }

    // Thêm dữ liệu danh mục trong cơ sở dữ liệu.
    // Tham số dm: danh mục cần xử lý.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean them(DanhMuc dm) throws SQLException {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.DANH_MUC_SAN_PHAM);

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = """
                INSERT INTO DanhMuc (maDanhMuc, tenDanhMuc, moTa)
                VALUES (?, ?, ?)
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, dm.getMaDanhMuc());
            ps.setString(2, dm.getTenDanhMuc());
            ps.setString(3, dm.getMoTa());

            return ps.executeUpdate() > 0;
        }
    }

    // Cập nhật dữ liệu danh mục trong cơ sở dữ liệu.
    // Tham số dm: danh mục cần xử lý.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean sua(DanhMuc dm) throws SQLException {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.DANH_MUC_SAN_PHAM);

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = """
                UPDATE DanhMuc
                SET tenDanhMuc = ?,
                    moTa = ?
                WHERE maDanhMuc = ?
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, dm.getTenDanhMuc());
            ps.setString(2, dm.getMoTa());
            ps.setString(3, dm.getMaDanhMuc());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Du lieu se bi xoa kem khi xoa 1 danh muc:
     * {so san pham, dong hoa don, dong phieu nhap, dong kiem ke}.
     */
    public int[] demLienQuan(String maDanhMuc) throws SQLException {

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = """
                SELECT (SELECT COUNT(*) FROM SanPham
                         WHERE maDanhMuc = ?),
                       (SELECT COUNT(*) FROM ChiTietHoaDon x
                         JOIN SanPham sp ON sp.maSanPham = x.maSanPham
                         WHERE sp.maDanhMuc = ?),
                       (SELECT COUNT(*) FROM ChiTietPhieuNhap x
                         JOIN SanPham sp ON sp.maSanPham = x.maSanPham
                         WHERE sp.maDanhMuc = ?),
                       (SELECT COUNT(*) FROM ChiTietPhieuKiemKe x
                         JOIN SanPham sp ON sp.maSanPham = x.maSanPham
                         WHERE sp.maDanhMuc = ?)
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, maDanhMuc);
            }

            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new int[]{rs.getInt(1), rs.getInt(2),
                        rs.getInt(3), rs.getInt(4)};
            }
        }
    }

    /** Chỉ xóa danh mục rỗng để không làm mất sản phẩm và lịch sử giao dịch. */
    public boolean xoa(String maDanhMuc) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.DANH_MUC_SAN_PHAM);
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection()) {
            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
            con.setAutoCommit(false);
            try {
                try (PreparedStatement check = con.prepareStatement(
                        "SELECT COUNT(*) FROM SanPham WHERE maDanhMuc=?")) {
                    check.setString(1, maDanhMuc);
                    // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("Không thể xóa danh mục đang có sản phẩm. Hãy chuyển hoặc xóa sản phẩm trước.");
                        }
                    }
                }
                boolean ok;
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM DanhMuc WHERE maDanhMuc = ?")) {
                    ps.setString(1, maDanhMuc);
                    ok = ps.executeUpdate() > 0;
                }

                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                con.commit();
                return ok;

            } catch (SQLException e) {
                // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
                con.rollback();
                throw e;
            }
        }
    }

    // =========================================================
    // SAN PHAM CUA DANH MUC + NHAP BO SUNG (dung trong form Sua danh muc)
    // =========================================================

    /** San pham thuoc 1 danh muc, kem ton kho hien tai. */
    public ArrayList<SanPham> timSanPhamTheoDanhMuc(String maDanhMuc) throws SQLException {

        // list: Danh sách chứa các phần tử kiểu SanPham.
        ArrayList<SanPham> list = new ArrayList<>();

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = """
                SELECT sp.maSanPham,
                       sp.tenSanPham,
                       sp.giaNhap,
                       sp.giaBan,
                       sp.maDanhMuc,
                       sp.maNhaCungCap,
                """ + SanPhamDAO.BIEU_THUC_TON + """
                       AS tonKho
                FROM SanPham sp
                WHERE sp.maDanhMuc = ?
                ORDER BY sp.maSanPham
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, maDanhMuc);

            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
                while (rs.next()) {
                    SanPham sp = new SanPham();
                    sp.setMaSanPham(rs.getString("maSanPham"));
                    sp.setTenSanPham(rs.getString("tenSanPham"));
                    sp.setGiaNhap(rs.getDouble("giaNhap"));
                    sp.setGiaBan(rs.getDouble("giaBan"));
                    sp.setMaDanhMuc(rs.getString("maDanhMuc"));
                    sp.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                    sp.setTonKho(rs.getInt("tonKho"));
                    list.add(sp);
                }
            }
        }

        return list;
    }

    /**
     * Ghi them so luong cho cac san pham: tao phieu nhap kho DA HOAN TAT,
     * moi nha cung cap 1 phieu (vi 1 phieu chi co 1 nha cung cap),
     * don gia nhap = gia nhap hien tai cua san pham. 1 giao dich.
     * soLuong.get(i) la so luong them cho sanPham.get(i); dong <= 0 bi bo qua.
     */
    public void nhapBoSung(List<SanPham> sanPham, List<Integer> soLuong,
                           String maNhanVien) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.NHAP_KHO);

        // Gom theo nha cung cap
        // theoNcc: Bảng ánh xạ khóa đến giá trị dùng cho theo NCC.
        Map<String, List<Integer>> theoNcc = new LinkedHashMap<>();

        for (int i = 0; i < sanPham.size(); i++) {
            if (soLuong.get(i) != null && soLuong.get(i) > 0) {
                theoNcc.computeIfAbsent(sanPham.get(i).getMaNhaCungCap(),
                        k -> new ArrayList<>()).add(i);
            }
        }

        if (theoNcc.isEmpty()) {
            return;
        }

        // sqlPhieu: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sqlPhieu = """
                INSERT INTO PhieuNhapKho
                (maPhieuNhap, ngayNhap, trangThai, maNhanVien, maNhaCungCap)
                VALUES (?, ?, 1, ?, ?)
                """;

        // sqlChiTiet: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sqlChiTiet = """
                INSERT INTO ChiTietPhieuNhap
                (maPhieuNhap, maSanPham, soLuongNhap, donGiaNhap, trangThai)
                VALUES (?, ?, ?, ?, 1)
                """;

        // Ma phieu ke tiep; moi phieu trong lan nay +1
        int so = Integer.parseInt(
                taoMa("PhieuNhapKho", "maPhieuNhap", "PN", 3).substring(2));

        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection()) {

            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
            con.setAutoCommit(false);

            try {
                for (Map.Entry<String, List<Integer>> e : theoNcc.entrySet()) {

                    // maPhieu: Lưu mã phiếu.
                    String maPhieu = String.format("PN%03d", so++);

                    try (PreparedStatement ps = con.prepareStatement(sqlPhieu)) {
                        ps.setString(1, maPhieu);
                        ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                        ps.setString(3, maNhanVien);
                        ps.setString(4, e.getKey());
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {
                        for (int i : e.getValue()) {
                            ps.setString(1, maPhieu);
                            ps.setString(2, sanPham.get(i).getMaSanPham());
                            ps.setInt(3, soLuong.get(i));
                            ps.setDouble(4, sanPham.get(i).getGiaNhap());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                con.commit();

            } catch (SQLException ex) {
                // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
                con.rollback();
                throw ex;
            }
        }
    }

    // Sinh ma tu dong dang TIENTO + so (DM005, SP009...): lay so lon nhat dang co roi cong 1
    // Tạo mã mới từ bảng, cột, tiền tố và số chữ số được yêu cầu.
    // Tham số bang: bảng dữ liệu.
    // Tham số cot: cột.
    // Tham số tienTo: tiền tố mã.
    // Tham số soChuSo: số chữ số của mã.
    // Kết quả: chuỗi kết quả.
    private static String taoMa(String bang, String cot,
                                String tienTo, int soChuSo) throws SQLException {

        int max = 0;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT " + cot + " FROM " + bang);
            ResultSet rs = ps.executeQuery()
        ) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                // ma: Lưu mã.
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
