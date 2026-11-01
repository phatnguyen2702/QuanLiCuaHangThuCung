package dao;

import UI.PhanQuyen;

import ConnectDB.connectDB;
import entity.DanhMuc;
import entity.NhaCungCap;
import entity.SanPham;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cua man hinh "San pham": san pham + danh sach danh muc / nha cung cap
 * cho o chon trong form + ghi them so luong nhap kho.
 */
public class SanPhamDAO {

    /*
     * Bang SanPham khong co cot ton kho, nen ton kho duoc TINH tu du lieu:
     * ton = tong nhap (phieu nhap da hoan tat)
     * - tong ban (ChiTietHoaDon)
     * + chenh lech cac phieu kiem ke da chot (thuc te - he thong)
     */
    /**
     * Bieu thuc SQL tinh ton kho cua san pham co bi danh "sp" (cac DAO khac dung
     * lai de ton kho luon khop).
     */
    static final String BIEU_THUC_TON = """
                   ISNULL((SELECT SUM(ct.soLuongNhap)
                           FROM ChiTietPhieuNhap ct
                           JOIN PhieuNhapKho pn
                             ON pn.maPhieuNhap = ct.maPhieuNhap
                           WHERE ct.maSanPham = sp.maSanPham
                             AND pn.trangThai = 1
                             AND ISNULL(ct.trangThai, 1) = 1), 0)
                 - ISNULL((SELECT SUM(hd.soLuong)
                           FROM ChiTietHoaDon hd
                           WHERE hd.maSanPham = sp.maSanPham), 0)
                 + ISNULL((SELECT SUM(kk.soLuongThucTe - kk.soLuongHeThong)
                           FROM ChiTietPhieuKiemKe kk
                           JOIN PhieuKiemKe pk
                             ON pk.maPhieuKiemKe = kk.maPhieuKiemKe
                           WHERE kk.maSanPham = sp.maSanPham
                             AND pk.trangThai = 1
                             AND kk.soLuongThucTe IS NOT NULL), 0)
            """;

    // SELECT_CO_BAN: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
    private static final String SELECT_CO_BAN = """
            SELECT sp.maSanPham,
                   sp.tenSanPham,
                   sp.giaNhap,
                   sp.giaBan,
                   sp.maDanhMuc,
                   dm.tenDanhMuc,
                   sp.maNhaCungCap,
                   ncc.tenNhaCungCap,
            """ + BIEU_THUC_TON + """
                   AS tonKho
            FROM SanPham sp
            JOIN DanhMuc dm ON dm.maDanhMuc = sp.maDanhMuc
            JOIN Nhacungcap ncc ON ncc.maNhaCungCap = sp.maNhaCungCap
            """;

    // Tìm kiểm.
    // Tham số tuKhoa: từ khóa tìm kiếm.
    // Tham số maDanhMuc: mã danh mục.
    // Tham số maNhaCungCap: mã nhà cung cấp.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<SanPham>.
    public ArrayList<SanPham> timKiem(String tuKhoa,
            String maDanhMuc,
            String maNhaCungCap) throws SQLException {

        // list: Danh sách chứa các phần tử kiểu SanPham.
        ArrayList<SanPham> list = new ArrayList<>();

        // sql: Bộ đệm ghép các đoạn chuỗi để tạo câu lệnh SQL.
        StringBuilder sql = new StringBuilder(SELECT_CO_BAN);
        // thamSo: Lưu tham số; phần tử có kiểu String.
        List<String> thamSo = new ArrayList<>();

        String kw = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";

        sql.append(" WHERE (sp.maSanPham LIKE ? OR sp.tenSanPham LIKE ?)");
        thamSo.add(kw);
        thamSo.add(kw);

        if (maDanhMuc != null) {
            sql.append(" AND sp.maDanhMuc = ?");
            thamSo.add(maDanhMuc);
        }

        if (maNhaCungCap != null) {
            sql.append(" AND sp.maNhaCungCap = ?");
            thamSo.add(maNhaCungCap);
        }

        sql.append(" ORDER BY sp.maSanPham");

        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < thamSo.size(); i++) {
                ps.setString(i + 1, thamSo.get(i));
            }

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
                    sp.setTenDanhMuc(rs.getString("tenDanhMuc"));
                    sp.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                    sp.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                    sp.setTonKho(rs.getInt("tonKho"));
                    list.add(sp);
                }
            }
        }

        return list;
    }

    /** Lọc thức ăn theo các trường hợp phù hợp đã lưu trong GoiYThucAn. */
    public ArrayList<SanPham> goiYThucAn(String loaiThuCung, String nhomTuoi, String kichThuoc) throws SQLException {
        damBaoBangGoiYThucAn();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = SELECT_CO_BAN + " WHERE (" + BIEU_THUC_TON + ") > 0 " +
                "AND EXISTS (SELECT 1 FROM GoiYThucAn gy WHERE gy.maSanPham = sp.maSanPham " +
                "AND gy.loaiThuCung = ? " +
                "AND (? = N'Tất cả độ tuổi' OR gy.nhomTuoi = ? OR gy.nhomTuoi = N'Mọi độ tuổi') " +
                "AND (? = N'Tất cả kích thước' OR gy.kichThuoc = ? OR gy.kichThuoc = N'Mọi kích thước')) " +
                "ORDER BY sp.tenSanPham";
        // list: Danh sách chứa các phần tử kiểu SanPham.
        ArrayList<SanPham> list = new ArrayList<>();
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loaiThuCung);
            ps.setString(2, nhomTuoi);
            ps.setString(3, nhomTuoi);
            ps.setString(4, kichThuoc);
            ps.setString(5, kichThuoc);
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
                    sp.setTenDanhMuc(rs.getString("tenDanhMuc"));
                    sp.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                    sp.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                    sp.setTonKho(rs.getInt("tonKho"));
                    list.add(sp);
                }
            }
        }
        return list;
    }

    /**
     * Tạo bảng nếu thiếu, rồi gắn metadata dựa trên tên/danh mục dữ liệu hiện có.
     */
    private void damBaoBangGoiYThucAn() throws SQLException {
        String taoBang = """
                IF OBJECT_ID('dbo.GoiYThucAn', 'U') IS NULL
                CREATE TABLE dbo.GoiYThucAn (
                    maSanPham VARCHAR(10) NOT NULL,
                    loaiThuCung NVARCHAR(10) NOT NULL,
                    nhomTuoi NVARCHAR(20) NOT NULL,
                    kichThuoc NVARCHAR(20) NOT NULL,
                    ghiChu NVARCHAR(250) NULL,
                    CONSTRAINT FK_GoiYThucAn_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham),
                    CONSTRAINT PK_GoiYThucAn PRIMARY KEY (maSanPham, loaiThuCung, nhomTuoi, kichThuoc),
                    CONSTRAINT CK_GoiYThucAn_Loai CHECK (loaiThuCung IN (N'Chó', N'Mèo')),
                    CONSTRAINT CK_GoiYThucAn_Tuoi CHECK (nhomTuoi IN (N'Con non', N'Trưởng thành', N'Cao tuổi', N'Mọi độ tuổi', N'Chưa xác định')),
                    CONSTRAINT CK_GoiYThucAn_Size CHECK (kichThuoc IN (N'Nhỏ', N'Trung bình', N'Lớn', N'Mọi kích thước', N'Chưa xác định'))
                )
                """;
        // seed: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String seed = """
                INSERT INTO dbo.GoiYThucAn(maSanPham, loaiThuCung, nhomTuoi, kichThuoc, ghiChu)
                SELECT sp.maSanPham,
                       CASE WHEN dm.tenDanhMuc LIKE N'%chó%' OR sp.tenSanPham LIKE N'%chó%' OR sp.tenSanPham LIKE N'%dog%' THEN N'Chó'
                            WHEN dm.tenDanhMuc LIKE N'%mèo%' OR sp.tenSanPham LIKE N'%mèo%' OR sp.tenSanPham LIKE N'%cat%' THEN N'Mèo' END,
                       CASE WHEN sp.tenSanPham LIKE N'%puppy%' OR sp.tenSanPham LIKE N'%kitten%' OR sp.tenSanPham LIKE N'%con non%' THEN N'Con non'
                            WHEN sp.tenSanPham LIKE N'%senior%' OR sp.tenSanPham LIKE N'%ageing%' OR sp.tenSanPham LIKE N'%aging%' OR sp.tenSanPham LIKE N'%cao tuổi%' THEN N'Cao tuổi'
                            WHEN sp.tenSanPham LIKE N'%adult%' OR sp.tenSanPham LIKE N'%trưởng thành%' THEN N'Trưởng thành'
                            ELSE N'Chưa xác định' END,
                       CASE WHEN sp.tenSanPham LIKE N'%mini%' OR sp.tenSanPham LIKE N'%small%' OR sp.tenSanPham LIKE N'%nhỏ%' THEN N'Nhỏ'
                            WHEN sp.tenSanPham LIKE N'%medium%' OR sp.tenSanPham LIKE N'%trung bình%' THEN N'Trung bình'
                            WHEN sp.tenSanPham LIKE N'%maxi%' OR sp.tenSanPham LIKE N'%large%' OR sp.tenSanPham LIKE N'%giant%' OR sp.tenSanPham LIKE N'%lớn%' THEN N'Lớn'
                            ELSE N'Chưa xác định' END,
                       N'Phân nhóm theo tên/danh mục; đối chiếu nhãn bao bì để xác nhận.'
                FROM SanPham sp JOIN DanhMuc dm ON dm.maDanhMuc = sp.maDanhMuc
                WHERE (dm.tenDanhMuc LIKE N'%thức ăn%' OR dm.tenDanhMuc LIKE N'%thuc an%')
                  AND (dm.tenDanhMuc LIKE N'%chó%' OR dm.tenDanhMuc LIKE N'%mèo%' OR sp.tenSanPham LIKE N'%dog%' OR sp.tenSanPham LIKE N'%cat%'
                       OR sp.tenSanPham LIKE N'%chó%' OR sp.tenSanPham LIKE N'%mèo%')
                  AND NOT EXISTS (SELECT 1 FROM dbo.GoiYThucAn gy WHERE gy.maSanPham = sp.maSanPham)
                """;
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection(); Statement st = con.createStatement()) {
            st.execute(taoBang);
            st.executeUpdate(seed);
        }
    }

    /**
     * Hạn sử dụng sớm nhất đã ghi nhận từ các PHIẾU NHẬP HOÀN TẤT.
     * Không phải cột của SanPham: mỗi lần nhập một mặt hàng có thể có HSD khác
     * nhau.
     * Đây là thông tin từ lịch sử nhập, không cam kết lô đó còn tồn thực tế.
     */
    public java.time.LocalDate layHanSuDungSomNhat(String maSanPham) throws SQLException {
        if (maSanPham == null || maSanPham.isBlank()) {
            return null;
        }
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = """
                SELECT MIN(ct.hanSuDung) AS hanSomNhat
                FROM dbo.ChiTietPhieuNhap ct
                JOIN dbo.PhieuNhapKho pn ON pn.maPhieuNhap = ct.maPhieuNhap
                WHERE ct.maSanPham = ?
                  AND pn.trangThai = 1
                  AND ISNULL(ct.trangThai, 1) = 1
                  AND ct.hanSuDung IS NOT NULL
                """;
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // han: Lưu hạn dưới dạng dữ liệu thời gian.
                    java.sql.Date han = rs.getDate("hanSomNhat");
                    return han == null ? null : han.toLocalDate();
                }
            }
        }
        return null;
    }

    // Lấy toàn bộ danh sách bản ghi từ cơ sở dữ liệu.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<SanPham>.
    public ArrayList<SanPham> getAll() throws SQLException {
        return timKiem("", null, null);
    }

    /** Tồn mới nhất, dùng khi chỉnh số lượng trong giỏ thức ăn. */
    public int layTonKho(String maSanPham) throws SQLException {
        String sql = "SELECT " + BIEU_THUC_TON + " AS tonKho FROM SanPham sp WHERE sp.maSanPham=?";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("tonKho") : 0;
            }
        }
    }

    /** Lưu sản phẩm, metadata gợi ý và lượng nhập ban đầu trong một giao dịch. */
    public void luuSanPhamDayDu(SanPham sp, boolean dangSua, String tenDanhMuc,
            String nhomTuoi, String kichThuoc, int soLuongThem, String maNhanVien) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.SAN_PHAM);
        if (soLuongThem > 0) PhanQuyen.yeuCau(PhanQuyen.Quyen.NHAP_KHO);
        damBaoBangGoiYThucAn();

        String nguon = ((tenDanhMuc == null ? "" : tenDanhMuc) + " " +
                (sp.getTenSanPham() == null ? "" : sp.getTenSanPham())).toLowerCase(java.util.Locale.ROOT);
        boolean laThucAn = nguon.contains("thức ăn") || nguon.contains("thuc an")
                || nguon.contains("pate") || nguon.contains("hạt chó") || nguon.contains("hạt mèo");
        String loai = (nguon.contains("chó") || nguon.contains("cho") || nguon.contains("dog")) ? "Chó"
                : (nguon.contains("mèo") || nguon.contains("meo") || nguon.contains("cat")) ? "Mèo" : null;
        if (laThucAn && loai == null) {
            throw new SQLException("Không xác định được thức ăn dành cho chó hay mèo từ danh mục/tên sản phẩm.");
        }

        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String sql = dangSua
                        ? "UPDATE SanPham SET tenSanPham=?,giaNhap=?,giaBan=?,maDanhMuc=?,maNhaCungCap=? WHERE maSanPham=?"
                        : "INSERT INTO SanPham(maSanPham,tenSanPham,giaNhap,giaBan,maDanhMuc,maNhaCungCap) VALUES(?,?,?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    if (dangSua) {
                        ps.setString(1, sp.getTenSanPham()); ps.setDouble(2, sp.getGiaNhap());
                        ps.setDouble(3, sp.getGiaBan()); ps.setString(4, sp.getMaDanhMuc());
                        ps.setString(5, sp.getMaNhaCungCap()); ps.setString(6, sp.getMaSanPham());
                    } else {
                        ps.setString(1, sp.getMaSanPham()); ps.setString(2, sp.getTenSanPham());
                        ps.setDouble(3, sp.getGiaNhap()); ps.setDouble(4, sp.getGiaBan());
                        ps.setString(5, sp.getMaDanhMuc()); ps.setString(6, sp.getMaNhaCungCap());
                    }
                    if (ps.executeUpdate() != 1) throw new SQLException("Không lưu được hồ sơ sản phẩm.");
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM dbo.GoiYThucAn WHERE maSanPham=?")) {
                    ps.setString(1, sp.getMaSanPham()); ps.executeUpdate();
                }
                if (laThucAn) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO dbo.GoiYThucAn(maSanPham,loaiThuCung,nhomTuoi,kichThuoc,ghiChu) VALUES(?,?,?,?,N'Nhập từ form sản phẩm')")) {
                        ps.setString(1, sp.getMaSanPham()); ps.setString(2, loai);
                        ps.setString(3, nhomTuoi); ps.setString(4, kichThuoc); ps.executeUpdate();
                    }
                }

                if (soLuongThem > 0) {
                    int max = 0;
                    try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(
                            "SELECT maPhieuNhap FROM dbo.PhieuNhapKho WITH (UPDLOCK,HOLDLOCK)")) {
                        while (rs.next()) {
                            String ma = rs.getString(1);
                            if (ma != null && ma.matches("PN\\d{1,9}"))
                                max = Math.max(max, Integer.parseInt(ma.substring(2)));
                        }
                    }
                    String maPhieu = "PN" + String.format("%03d", max + 1);
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO PhieuNhapKho(maPhieuNhap,ngayNhap,trangThai,maNhanVien,maNhaCungCap) VALUES(?,CURRENT_TIMESTAMP,1,?,?)")) {
                        ps.setString(1, maPhieu); ps.setString(2, maNhanVien); ps.setString(3, sp.getMaNhaCungCap());
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO ChiTietPhieuNhap(maPhieuNhap,maSanPham,soLuongNhap,donGiaNhap,trangThai) VALUES(?,?,?,?,1)")) {
                        ps.setString(1, maPhieu); ps.setString(2, sp.getMaSanPham());
                        ps.setInt(3, soLuongThem); ps.setDouble(4, sp.getGiaNhap()); ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                try { con.rollback(); } catch (SQLException rollbackError) { ex.addSuppressed(rollbackError); }
                throw ex;
            }
        }
    }

    // Lấy mã mới để hiển thị khi lập bản ghi.
    // Kết quả: chuỗi kết quả.
    public String taoMaMoi() throws SQLException {
        return taoMa("SanPham", "maSanPham", "SP", 3);
    }

    // Thêm dữ liệu sản phẩm trong cơ sở dữ liệu.
    // Tham số sp: sản phẩm cần xử lý.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean them(SanPham sp) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.SAN_PHAM);

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = """
                INSERT INTO SanPham
                (maSanPham, tenSanPham, giaNhap, giaBan,
                 maDanhMuc, maNhaCungCap)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sp.getMaSanPham());
            ps.setString(2, sp.getTenSanPham());
            ps.setDouble(3, sp.getGiaNhap());
            ps.setDouble(4, sp.getGiaBan());
            ps.setString(5, sp.getMaDanhMuc());
            ps.setString(6, sp.getMaNhaCungCap());

            return ps.executeUpdate() > 0;
        }
    }

    // Cập nhật dữ liệu sản phẩm trong cơ sở dữ liệu.
    // Tham số sp: sản phẩm cần xử lý.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean sua(SanPham sp) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.SAN_PHAM);

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = """
                UPDATE SanPham
                SET tenSanPham = ?,
                    giaNhap = ?,
                    giaBan = ?,
                    maDanhMuc = ?,
                    maNhaCungCap = ?
                WHERE maSanPham = ?
                """;

        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sp.getTenSanPham());
            ps.setDouble(2, sp.getGiaNhap());
            ps.setDouble(3, sp.getGiaBan());
            ps.setString(4, sp.getMaDanhMuc());
            ps.setString(5, sp.getMaNhaCungCap());
            ps.setString(6, sp.getMaSanPham());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Luu metadata goi y cho san pham thuc an. Loai thu cung duoc suy ra tu danh
     * muc/ten.
     */
    public void luuThongTinGoiY(String maSanPham, String tenDanhMuc, String tenSanPham,
            String nhomTuoi, String kichThuoc) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.SAN_PHAM);
        String nguon = ((tenDanhMuc == null ? "" : tenDanhMuc) + " " +
                (tenSanPham == null ? "" : tenSanPham)).toLowerCase(java.util.Locale.ROOT);
        boolean laThucAn = nguon.contains("thức ăn") || nguon.contains("thuc an")
                || nguon.contains("pate") || nguon.contains("hạt chó") || nguon.contains("hạt mèo");
        if (!laThucAn) {
            damBaoBangGoiYThucAn();
            // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
            try (Connection con = connectDB.getConnection();
                    PreparedStatement ps = con.prepareStatement("DELETE FROM dbo.GoiYThucAn WHERE maSanPham=?")) {
                ps.setString(1, maSanPham);
                ps.executeUpdate();
            }
            return;
        }
        // loai: Lưu loài/loại.
        String loai = (nguon.contains("chó") || nguon.contains("cho") || nguon.contains("dog")) ? "Chó"
                : (nguon.contains("mèo") || nguon.contains("meo") || nguon.contains("cat")) ? "Mèo" : null;
        if (loai == null) {
            throw new SQLException("Không xác định được thức ăn dành cho chó hay mèo từ danh mục/tên sản phẩm.");
        }
        damBaoBangGoiYThucAn();
        // xoa: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String xoa = "DELETE FROM dbo.GoiYThucAn WHERE maSanPham = ?";
        // them: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String them = "INSERT INTO dbo.GoiYThucAn(maSanPham, loaiThuCung, nhomTuoi, kichThuoc, ghiChu) VALUES (?, ?, ?, ?, N'Nhập từ form sản phẩm')";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection()) {
            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
            con.setAutoCommit(false);
            try (PreparedStatement psXoa = con.prepareStatement(xoa);
                    PreparedStatement psThem = con.prepareStatement(them)) {
                psXoa.setString(1, maSanPham);
                psXoa.executeUpdate();
                psThem.setString(1, maSanPham);
                psThem.setString(2, loai);
                psThem.setString(3, nhomTuoi);
                psThem.setString(4, kichThuoc);
                psThem.executeUpdate();
                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                con.commit();
            } catch (SQLException ex) {
                // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /** Tra lai loai, tuoi, co da luu cho san pham; null neu chua co. */
    public String[] layThongTinGoiY(String maSanPham) throws SQLException {
        damBaoBangGoiYThucAn();
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT TOP 1 loaiThuCung, nhomTuoi, kichThuoc FROM dbo.GoiYThucAn WHERE maSanPham = ?")) {
            ps.setString(1, maSanPham);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new String[] { rs.getString(1), rs.getString(2), rs.getString(3) } : null;
            }
        }
    }

    // Xử lý xóa dữ liệu sản phẩm trong cơ sở dữ liệu.
    // Tham số maSanPham: mã sản phẩm.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean xoa(String maSanPham) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.SAN_PHAM);
        damBaoBangGoiYThucAn();
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection()) {
            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT maSanPham FROM SanPham WITH (UPDLOCK,HOLDLOCK) WHERE maSanPham=?")) {
                    ps.setString(1, maSanPham);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.commit();
                            return false;
                        }
                    }
                }

                String sqlDem = "SELECT (SELECT COUNT(*) FROM ChiTietPhieuNhap WHERE maSanPham=?)"
                        + "+(SELECT COUNT(*) FROM ChiTietHoaDon WHERE maSanPham=?)"
                        + "+(SELECT COUNT(*) FROM ChiTietPhieuKiemKe WHERE maSanPham=?)";
                try (PreparedStatement ps = con.prepareStatement(sqlDem)) {
                    ps.setString(1, maSanPham); ps.setString(2, maSanPham); ps.setString(3, maSanPham);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("Không thể xóa sản phẩm đã phát sinh lịch sử nhập kho, hóa đơn hoặc kiểm kê.");
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM dbo.GoiYThucAn WHERE maSanPham=?")) {
                    ps.setString(1, maSanPham); ps.executeUpdate();
                }
                boolean deleted;
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM SanPham WHERE maSanPham=?")) {
                    ps.setString(1, maSanPham);
                    deleted = ps.executeUpdate() > 0;
                }
                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                con.commit();
                return deleted;
            } catch (SQLException e) {
                // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
                con.rollback();
                throw e;
            }
        }
    }

    /** So dong nhap / ban / kiem ke dang tham chieu toi san pham nay. */
    public int demLichSu(String maSanPham) throws SQLException {

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = """
                SELECT (SELECT COUNT(*) FROM ChiTietPhieuNhap   WHERE maSanPham = ?)
                     + (SELECT COUNT(*) FROM ChiTietHoaDon      WHERE maSanPham = ?)
                     + (SELECT COUNT(*) FROM ChiTietPhieuKiemKe WHERE maSanPham = ?)
                """;

        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            ps.setString(2, maSanPham);
            ps.setString(3, maSanPham);

            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // =========================================================
    // DANH SACH CHO O CHON (combobox) TRONG MAN HINH SAN PHAM
    // =========================================================

    // Lấy danh sách danh mục.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<DanhMuc>.
    public ArrayList<DanhMuc> layDanhSachDanhMuc() throws SQLException {

        // list: Danh sách chứa các phần tử kiểu DanhMuc.
        ArrayList<DanhMuc> list = new ArrayList<>();

        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT maDanhMuc, tenDanhMuc, moTa FROM DanhMuc ORDER BY maDanhMuc");
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                list.add(new DanhMuc(rs.getString("maDanhMuc"),
                        rs.getString("tenDanhMuc"), rs.getString("moTa")));
            }
        }

        return list;
    }

    // Lấy danh sách nhà cung cấp.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<NhaCungCap>.
    public ArrayList<NhaCungCap> layDanhSachNhaCungCap() throws SQLException {

        // list: Danh sách chứa các phần tử kiểu NhaCungCap.
        ArrayList<NhaCungCap> list = new ArrayList<>();

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = """
                SELECT maNhaCungCap, tenNhaCungCap,
                       soDienThoai, email, diaChi
                FROM Nhacungcap
                ORDER BY maNhaCungCap
                """;

        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                list.add(new NhaCungCap(rs.getString("maNhaCungCap"),
                        rs.getString("tenNhaCungCap"), rs.getString("soDienThoai"),
                        rs.getString("email"), rs.getString("diaChi")));
            }
        }

        return list;
    }

    // =========================================================
    // NHAP BO SUNG SO LUONG (o "Them so luong" trong form san pham)
    // =========================================================

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

    // Sinh ma tu dong dang TIENTO + so (DM005, SP009...): lay so lon nhat dang co
    // roi cong 1
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
                ResultSet rs = ps.executeQuery()) {
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
