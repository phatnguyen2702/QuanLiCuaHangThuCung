package dao;

import control.PhanQuyenControl;

import ConnectDB.connectDB;
import entity.DanhMuc;
import entity.NhaCungCap;
import entity.SanPham;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cua man hinh "San pham": san pham + danh sach danh muc / nha cung cap
 * cho o chon trong form va thông tin gợi ý thức ăn.
 */
public class SanPhamDAO {

    /** Bổ sung cờ dừng bán theo lô cho các cơ sở dữ liệu đã tạo trước đây. */
    public static void damBaoCotKinhDoanh() throws SQLException {
        try (Connection con = connectDB.getConnection(); Statement st = con.createStatement()) {
            st.execute("IF COL_LENGTH('dbo.ChiTietPhieuNhap','ngungKinhDoanh') IS NULL "
                    + "ALTER TABLE dbo.ChiTietPhieuNhap ADD ngungKinhDoanh BIT NOT NULL "
                    + "CONSTRAINT DF_CTPN_ngungKinhDoanh DEFAULT 0 WITH VALUES");
            st.execute("IF COL_LENGTH('dbo.ChiTietPhieuNhap','soLuongDaXuLy') IS NULL "
                    + "ALTER TABLE dbo.ChiTietPhieuNhap ADD soLuongDaXuLy INT NOT NULL "
                    + "CONSTRAINT DF_CTPN_soLuongDaXuLy DEFAULT 0 WITH VALUES");
            st.execute("IF OBJECT_ID('dbo.ChiTietPhieuKiemKeLo','U') IS NULL "
                    + "CREATE TABLE dbo.ChiTietPhieuKiemKeLo ("
                    + "maPhieuKiemKe VARCHAR(10) NOT NULL, maPhieuNhap VARCHAR(10) NOT NULL, "
                    + "maSanPham VARCHAR(10) NOT NULL, soLuongHeThong INT NOT NULL, "
                    + "soLuongThucTe INT NULL, trangThai BIT NULL, "
                    + "CONSTRAINT PK_ChiTietPhieuKiemKeLo PRIMARY KEY(maPhieuKiemKe,maPhieuNhap,maSanPham), "
                    + "CONSTRAINT FK_CTPKKL_Phieu FOREIGN KEY(maPhieuKiemKe) REFERENCES PhieuKiemKe(maPhieuKiemKe), "
                    + "CONSTRAINT FK_CTPKKL_Lo FOREIGN KEY(maPhieuNhap,maSanPham) REFERENCES ChiTietPhieuNhap(maPhieuNhap,maSanPham))");
            st.execute("IF OBJECT_ID('dbo.ChiTietXuLyHang','U') IS NULL "
                    + "CREATE TABLE dbo.ChiTietXuLyHang ("
                    + "maXuLy BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, maPhieuNhap VARCHAR(10) NOT NULL, "
                    + "maSanPham VARCHAR(10) NOT NULL, soLuong INT NOT NULL CHECK(soLuong>0), "
                    + "lyDo NVARCHAR(50) NOT NULL, ghiChu NVARCHAR(250) NULL, ngayXuLy DATETIME2 NOT NULL DEFAULT GETDATE(), "
                    + "maNhanVien VARCHAR(10) NULL, "
                    + "CONSTRAINT FK_XuLyHang_DotNhap FOREIGN KEY(maPhieuNhap,maSanPham) "
                    + "REFERENCES ChiTietPhieuNhap(maPhieuNhap,maSanPham))");
            st.executeUpdate("INSERT INTO dbo.ChiTietPhieuKiemKeLo "
                    + "(maPhieuKiemKe,maPhieuNhap,maSanPham,soLuongHeThong,soLuongThucTe,trangThai) "
                    + "SELECT k.maPhieuKiemKe,l.maPhieuNhap,k.maSanPham,k.soLuongHeThong,k.soLuongThucTe,k.trangThai "
                    + "FROM dbo.ChiTietPhieuKiemKe k JOIN dbo.PhieuKiemKe p ON p.maPhieuKiemKe=k.maPhieuKiemKe "
                    + "CROSS APPLY (SELECT TOP 1 ct.maPhieuNhap FROM dbo.ChiTietPhieuNhap ct "
                    + "JOIN dbo.PhieuNhapKho pn ON pn.maPhieuNhap=ct.maPhieuNhap "
                    + "WHERE ct.maSanPham=k.maSanPham AND pn.trangThai=1 AND ISNULL(ct.trangThai,1)=1 "
                    + "ORDER BY pn.ngayNhap,ct.maPhieuNhap) l "
                    + "WHERE ISNULL(p.trangThai,0)=0 AND NOT EXISTS (SELECT 1 FROM dbo.ChiTietPhieuKiemKeLo x "
                    + "WHERE x.maPhieuKiemKe=k.maPhieuKiemKe AND x.maSanPham=k.maSanPham)");
        }
    }

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
                 + ISNULL((SELECT SUM(kl.soLuongThucTe - kl.soLuongHeThong)
                           FROM ChiTietPhieuKiemKeLo kl
                           JOIN PhieuKiemKe pk ON pk.maPhieuKiemKe = kl.maPhieuKiemKe
                           WHERE kl.maSanPham = sp.maSanPham
                             AND pk.trangThai = 1
                             AND kl.soLuongThucTe IS NOT NULL), 0)
                 - ISNULL((SELECT SUM(ct.soLuongDaXuLy)
                           FROM ChiTietPhieuNhap ct JOIN PhieuNhapKho pn ON pn.maPhieuNhap=ct.maPhieuNhap
                           WHERE ct.maSanPham=sp.maSanPham AND pn.trangThai=1
                             AND ISNULL(ct.trangThai,1)=1),0)
            """;

    // SELECT_CO_BAN: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị
    // trí nhận tham số nếu có.
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

        damBaoCotKinhDoanh();

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
        damBaoCotKinhDoanh();
        damBaoBangGoiYThucAn();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = SELECT_CO_BAN + " WHERE (" + BIEU_THUC_TON + ") > 0 " +
                "AND EXISTS (SELECT 1 FROM ChiTietPhieuNhap ct JOIN PhieuNhapKho pn ON pn.maPhieuNhap=ct.maPhieuNhap " +
                "WHERE ct.maSanPham=sp.maSanPham AND pn.trangThai=1 AND ISNULL(ct.trangThai,1)=1 " +
                "AND ISNULL(ct.ngungKinhDoanh,0)=0 AND (ct.hanSuDung IS NULL OR ct.hanSuDung>=CAST(GETDATE() AS date))) "
                +
                "AND EXISTS (SELECT 1 FROM GoiYThucAn gy WHERE gy.maSanPham = sp.maSanPham " +
                "AND gy.loaiThuCung = ? " +
                "AND (? = N'Tất cả độ tuổi' OR gy.nhomTuoi = ? OR gy.nhomTuoi = N'Mọi độ tuổi') " +
                "AND (? = N'Tất cả kích thước' OR gy.kichThuoc = ? OR gy.kichThuoc = N'Mọi kích thước')) " +
                "ORDER BY sp.tenSanPham";
        // list: Danh sách chứa các phần tử kiểu SanPham.
        ArrayList<SanPham> list = new ArrayList<>();
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
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
        // seed: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
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
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
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
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = """
                SELECT MIN(ct.hanSuDung) AS hanSomNhat
                FROM dbo.ChiTietPhieuNhap ct
                JOIN dbo.PhieuNhapKho pn ON pn.maPhieuNhap = ct.maPhieuNhap
                WHERE ct.maSanPham = ?
                  AND pn.trangThai = 1
                  AND ISNULL(ct.trangThai, 1) = 1
                  AND ct.hanSuDung IS NOT NULL
                """;
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
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
        damBaoCotKinhDoanh();
        String sql = "SELECT " + BIEU_THUC_TON + " AS tonKho FROM SanPham sp WHERE sp.maSanPham=?";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("tonKho") : 0;
            }
        }
    }

    /** Các đợt nhập dùng làm bộ lọc lô ở màn hình sản phẩm. */
    public ArrayList<DotNhap> layDanhSachDotNhap() throws SQLException {
        damBaoCotKinhDoanh();
        ArrayList<DotNhap> ds = new ArrayList<>();
        String sql = "SELECT p.maPhieuNhap,p.ngayNhap FROM PhieuNhapKho p WHERE p.trangThai=1 "
                + "ORDER BY p.ngayNhap DESC,p.maPhieuNhap DESC";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("ngayNhap");
                ds.add(new DotNhap(rs.getString("maPhieuNhap"),
                        timestamp == null ? null : timestamp.toLocalDateTime()));
            }
        }
        return ds;
    }

    public ArrayList<DotNhap> layDanhSachDotNhap(String maSanPham) throws SQLException {
        damBaoCotKinhDoanh();
        ArrayList<DotNhap> ds = new ArrayList<>();
        String sql = "SELECT p.maPhieuNhap,p.ngayNhap FROM PhieuNhapKho p "
                + "JOIN ChiTietPhieuNhap ct ON ct.maPhieuNhap=p.maPhieuNhap "
                + "WHERE p.trangThai=1 AND ct.maSanPham=? ORDER BY p.ngayNhap DESC,p.maPhieuNhap DESC";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("ngayNhap");
                    ds.add(new DotNhap(rs.getString("maPhieuNhap"),
                            timestamp == null ? null : timestamp.toLocalDateTime()));
                }
            }
        }
        return ds;
    }

    /**
     * Trả về tồn và giá theo một đợt nhập; null nghĩa là tổng hợp tất cả các đợt.
     */
    public ArrayList<SanPham> timKiemTheoDotNhap(String tuKhoa, String maDanhMuc, String maPhieuNhap)
            throws SQLException {
        return timKiemTheoDotNhap(tuKhoa, maDanhMuc, maPhieuNhap, null);
    }

    /** Lấy tồn của một sản phẩm trong một đợt nhập cụ thể nếu biết mã sản phẩm. */
    public ArrayList<SanPham> timKiemTheoDotNhap(String tuKhoa, String maDanhMuc, String maPhieuNhap,
            String maSanPham) throws SQLException {
        if (maPhieuNhap == null)
            return timKiem(tuKhoa, maDanhMuc, null);
        damBaoCotKinhDoanh();
        ArrayList<SanPham> list = new ArrayList<>();
        String sql = """
                WITH NguonLo AS (
                  SELECT ct.maSanPham,ct.maPhieuNhap,pn.ngayNhap,ct.hanSuDung,ct.donGiaNhap,
                         CASE WHEN ISNULL(ct.soLuongNhap,0)-ISNULL(ct.soLuongDaXuLy,0)<0 THEN 0
                              ELSE ISNULL(ct.soLuongNhap,0)-ISNULL(ct.soLuongDaXuLy,0) END AS soLuongNhap,
                         ISNULL(ct.ngungKinhDoanh,0) AS ngungKinhDoanh,
                         ISNULL((SELECT SUM(h.soLuong) FROM ChiTietHoaDon h WHERE h.maSanPham=ct.maSanPham),0) AS daBan,
                         ISNULL((SELECT SUM(k.soLuongThucTe-k.soLuongHeThong) FROM ChiTietPhieuKiemKe k JOIN PhieuKiemKe p ON p.maPhieuKiemKe=k.maPhieuKiemKe WHERE k.maSanPham=ct.maSanPham AND p.trangThai=1 AND k.soLuongThucTe IS NOT NULL),0) AS dieuChinhCu,
                         ISNULL((SELECT SUM(k.soLuongThucTe-k.soLuongHeThong) FROM ChiTietPhieuKiemKeLo k JOIN PhieuKiemKe p ON p.maPhieuKiemKe=k.maPhieuKiemKe WHERE k.maPhieuNhap=ct.maPhieuNhap AND k.maSanPham=ct.maSanPham AND p.trangThai=1 AND k.soLuongThucTe IS NOT NULL),0) AS dieuChinhLo
                  FROM ChiTietPhieuNhap ct JOIN PhieuNhapKho pn ON pn.maPhieuNhap=ct.maPhieuNhap
                  WHERE pn.trangThai=1 AND ISNULL(ct.trangThai,1)=1
                ), Lo AS (
                  SELECT *,SUM(soLuongNhap) OVER(PARTITION BY maSanPham ORDER BY ngayNhap,maPhieuNhap ROWS UNBOUNDED PRECEDING) AS tonLuyKe
                  FROM NguonLo
                ), ConLai AS (
                  SELECT *,CASE WHEN daBan>=tonLuyKe THEN 0 WHEN daBan<=tonLuyKe-soLuongNhap THEN soLuongNhap ELSE tonLuyKe-daBan END
                       + CASE WHEN tonLuyKe=soLuongNhap THEN dieuChinhCu ELSE 0 END + dieuChinhLo AS tonLo
                  FROM Lo
                )
                SELECT sp.maSanPham,sp.tenSanPham,sp.giaBan,sp.maDanhMuc,dm.tenDanhMuc,
                       sp.maNhaCungCap,n.tenNhaCungCap,c.maPhieuNhap,c.ngayNhap,c.hanSuDung,c.donGiaNhap,c.tonLo,c.ngungKinhDoanh
                FROM ConLai c JOIN SanPham sp ON sp.maSanPham=c.maSanPham
                JOIN DanhMuc dm ON dm.maDanhMuc=sp.maDanhMuc JOIN Nhacungcap n ON n.maNhaCungCap=sp.maNhaCungCap
                WHERE c.maPhieuNhap=? AND (sp.maSanPham LIKE ? OR sp.tenSanPham LIKE ?)
                  AND c.tonLo>=0 AND (? IS NULL OR sp.maDanhMuc=?)
                  AND (? IS NULL OR c.maSanPham=?)
                ORDER BY sp.maSanPham
                """;
        String like = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieuNhap);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, maDanhMuc);
            ps.setString(5, maDanhMuc);
            ps.setString(6, maSanPham);
            ps.setString(7, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SanPham sp = new SanPham();
                    sp.setMaSanPham(rs.getString("maSanPham"));
                    sp.setTenSanPham(rs.getString("tenSanPham"));
                    sp.setGiaNhap(rs.getDouble("donGiaNhap"));
                    sp.setGiaBan(rs.getDouble("giaBan"));
                    sp.setMaDanhMuc(rs.getString("maDanhMuc"));
                    sp.setTenDanhMuc(rs.getString("tenDanhMuc"));
                    sp.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                    sp.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                    sp.setMaPhieuNhap(rs.getString("maPhieuNhap"));
                    Timestamp date = rs.getTimestamp("ngayNhap");
                    sp.setNgayNhap(date == null ? null : date.toLocalDateTime().toLocalDate());
                    Date expiry = rs.getDate("hanSuDung");
                    sp.setHanSuDung(expiry == null ? null : expiry.toLocalDate());
                    sp.setTonKho(rs.getInt("tonLo"));
                    sp.setNgungKinhDoanh(rs.getBoolean("ngungKinhDoanh"));
                    list.add(sp);
                }
            }
        }
        return list;
    }

    /** Ghi nhận hàng hủy/trả theo một đợt nhập, không xóa dữ liệu phiếu nhập. */
    public void xuLyHangTheoDot(String maSanPham, String maPhieuNhap, int soLuong,
            String lyDo, String ghiChu, String maNhanVien) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);
        if (soLuong <= 0)
            throw new SQLException("Số lượng xử lý phải lớn hơn 0.");
        damBaoCotKinhDoanh();

        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Khóa hồ sơ sản phẩm để tránh hai thao tác cùng sử dụng một lượng tồn.
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT maSanPham FROM SanPham WITH (UPDLOCK,HOLDLOCK) WHERE maSanPham=?")) {
                    ps.setString(1, maSanPham);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next())
                            throw new SQLException("Không tìm thấy sản phẩm.");
                    }
                }

                String sqlTon = """
                        WITH NguonLo AS (
                         SELECT ct.maSanPham,ct.maPhieuNhap,pn.ngayNhap,
                                 CASE WHEN ISNULL(ct.soLuongNhap,0)-ISNULL(ct.soLuongDaXuLy,0)<0 THEN 0
                                      ELSE ISNULL(ct.soLuongNhap,0)-ISNULL(ct.soLuongDaXuLy,0) END AS soLuongNhap,
                                 ISNULL((SELECT SUM(h.soLuong) FROM ChiTietHoaDon h WHERE h.maSanPham=ct.maSanPham),0) AS daBan,
                                 ISNULL((SELECT SUM(k.soLuongThucTe-k.soLuongHeThong) FROM ChiTietPhieuKiemKe k JOIN PhieuKiemKe p ON p.maPhieuKiemKe=k.maPhieuKiemKe WHERE k.maSanPham=ct.maSanPham AND p.trangThai=1 AND k.soLuongThucTe IS NOT NULL),0) AS dieuChinhCu,
                                 ISNULL((SELECT SUM(k.soLuongThucTe-k.soLuongHeThong) FROM ChiTietPhieuKiemKeLo k JOIN PhieuKiemKe p ON p.maPhieuKiemKe=k.maPhieuKiemKe WHERE k.maPhieuNhap=ct.maPhieuNhap AND k.maSanPham=ct.maSanPham AND p.trangThai=1 AND k.soLuongThucTe IS NOT NULL),0) AS dieuChinhLo
                          FROM ChiTietPhieuNhap ct JOIN PhieuNhapKho pn ON pn.maPhieuNhap=ct.maPhieuNhap
                          WHERE pn.trangThai=1 AND ISNULL(ct.trangThai,1)=1 AND ct.maSanPham=?
                        ), Lo AS (
                          SELECT *,SUM(soLuongNhap) OVER(PARTITION BY maSanPham ORDER BY ngayNhap,maPhieuNhap ROWS UNBOUNDED PRECEDING) AS tonLuyKe
                          FROM NguonLo
                        ), ConLai AS (
                          SELECT *,CASE WHEN daBan>=tonLuyKe THEN 0 WHEN daBan<=tonLuyKe-soLuongNhap THEN soLuongNhap ELSE tonLuyKe-daBan END
                                 + CASE WHEN tonLuyKe=soLuongNhap THEN dieuChinhCu ELSE 0 END + dieuChinhLo AS tonLo
                          FROM Lo
                        )
                        SELECT tonLo FROM ConLai WHERE maSanPham=? AND maPhieuNhap=?
                        """;
                int tonLo;
                try (PreparedStatement ps = con.prepareStatement(sqlTon)) {
                    ps.setString(1, maSanPham);
                    ps.setString(2, maSanPham);
                    ps.setString(3, maPhieuNhap);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next())
                            throw new SQLException("Đợt nhập không còn tồn hoặc không tồn tại.");
                        tonLo = rs.getInt("tonLo");
                    }
                }
                if (soLuong > tonLo)
                    throw new SQLException("Số lượng xử lý vượt quá tồn của đợt nhập (" + tonLo + ").");

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE ChiTietPhieuNhap SET soLuongDaXuLy=ISNULL(soLuongDaXuLy,0)+? "
                                + "WHERE maPhieuNhap=? AND maSanPham=? "
                                + "AND ISNULL(soLuongDaXuLy,0)+?<=ISNULL(soLuongNhap,0)")) {
                    ps.setInt(1, soLuong);
                    ps.setString(2, maPhieuNhap);
                    ps.setString(3, maSanPham);
                    ps.setInt(4, soLuong);
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Số lượng xử lý vượt quá số lượng đã nhập của đợt này.");
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO ChiTietXuLyHang(maPhieuNhap,maSanPham,soLuong,lyDo,ghiChu,maNhanVien) VALUES(?,?,?,?,?,?)")) {
                    ps.setString(1, maPhieuNhap);
                    ps.setString(2, maSanPham);
                    ps.setInt(3, soLuong);
                    ps.setString(4, lyDo);
                    if (ghiChu == null || ghiChu.isBlank())
                        ps.setNull(5, Types.NVARCHAR);
                    else
                        ps.setString(5, ghiChu.trim());
                    if (maNhanVien == null || maNhanVien.isBlank())
                        ps.setNull(6, Types.VARCHAR);
                    else
                        ps.setString(6, maNhanVien);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Lấy toàn bộ lịch sử xử lý hàng lỗi, hết hạn hoặc trả nhà cung cấp. */
    public List<Object[]> layLichSuXuLyHang() throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);
        damBaoCotKinhDoanh();
        String sql = "SELECT x.maXuLy, x.ngayXuLy, x.maSanPham, sp.tenSanPham, "
                + "x.maPhieuNhap, x.soLuong, x.lyDo, x.ghiChu, "
                + "COALESCE(nv.hoTen, x.maNhanVien, N'—') AS nguoiXuLy "
                + "FROM dbo.ChiTietXuLyHang x "
                + "JOIN dbo.SanPham sp ON sp.maSanPham=x.maSanPham "
                + "LEFT JOIN dbo.NhanVien nv ON nv.maNhanVien=x.maNhanVien "
                + "ORDER BY x.ngayXuLy DESC, x.maXuLy DESC";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp ngay = rs.getTimestamp("ngayXuLy");
                rows.add(new Object[] {
                        rs.getLong("maXuLy"),
                        ngay == null ? ""
                                : ngay.toLocalDateTime()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        rs.getString("maSanPham"), rs.getString("tenSanPham"),
                        rs.getString("maPhieuNhap"), rs.getInt("soLuong"),
                        rs.getString("lyDo"), rs.getString("ghiChu"), rs.getString("nguoiXuLy")
                });
            }
        }
        return rows;
    }

    public static final class DotNhap {
        private final String maPhieuNhap;
        private final LocalDateTime ngayNhap;

        public DotNhap(String maPhieuNhap, LocalDateTime ngayNhap) {
            this.maPhieuNhap = maPhieuNhap;
            this.ngayNhap = ngayNhap;
        }

        public String getMaPhieuNhap() {
            return maPhieuNhap;
        }

        @Override
        public String toString() {
            return maPhieuNhap + (ngayNhap == null ? ""
                    : " · " + ngayNhap.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
    }

    /** Lưu thông tin sản phẩm và metadata gợi ý trong một giao dịch. */
    public void luuSanPhamDayDu(SanPham sp, boolean dangSua, String tenDanhMuc,
            String nhomTuoi, String kichThuoc, String maNhanVien) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);
        damBaoCotKinhDoanh();
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
                if (dangSua) {
                    String sqlKiemTra = "SELECT " + BIEU_THUC_TON
                            + " AS tonKho, giaNhap FROM SanPham sp WITH (UPDLOCK, HOLDLOCK) WHERE maSanPham=?";
                    try (PreparedStatement ps = con.prepareStatement(sqlKiemTra)) {
                        ps.setString(1, sp.getMaSanPham());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next())
                                throw new SQLException("Không tìm thấy sản phẩm cần sửa.");
                            double giaNhapCu = rs.getDouble("giaNhap");
                            if (rs.getInt("tonKho") > 0 && Double.compare(giaNhapCu, sp.getGiaNhap()) != 0)
                                throw new SQLException("Chỉ được đổi giá nhập khi tồn kho bằng 0.");
                        }
                    }
                }
                String sql = dangSua
                        ? "UPDATE SanPham SET tenSanPham=?,giaNhap=?,giaBan=?,maDanhMuc=?,maNhaCungCap=? WHERE maSanPham=?"
                        : "INSERT INTO SanPham(maSanPham,tenSanPham,giaNhap,giaBan,maDanhMuc,maNhaCungCap) VALUES(?,?,?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    if (dangSua) {
                        ps.setString(1, sp.getTenSanPham());
                        ps.setDouble(2, sp.getGiaNhap());
                        ps.setDouble(3, sp.getGiaBan());
                        ps.setString(4, sp.getMaDanhMuc());
                        ps.setString(5, sp.getMaNhaCungCap());
                        ps.setString(6, sp.getMaSanPham());
                    } else {
                        ps.setString(1, sp.getMaSanPham());
                        ps.setString(2, sp.getTenSanPham());
                        ps.setDouble(3, sp.getGiaNhap());
                        ps.setDouble(4, sp.getGiaBan());
                        ps.setString(5, sp.getMaDanhMuc());
                        ps.setString(6, sp.getMaNhaCungCap());
                    }
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Không lưu được hồ sơ sản phẩm.");
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM dbo.GoiYThucAn WHERE maSanPham=?")) {
                    ps.setString(1, sp.getMaSanPham());
                    ps.executeUpdate();
                }
                if (laThucAn) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO dbo.GoiYThucAn(maSanPham,loaiThuCung,nhomTuoi,kichThuoc,ghiChu) VALUES(?,?,?,?,N'Nhập từ form sản phẩm')")) {
                        ps.setString(1, sp.getMaSanPham());
                        ps.setString(2, loai);
                        ps.setString(3, nhomTuoi);
                        ps.setString(4, kichThuoc);
                        ps.executeUpdate();
                    }
                }

                con.commit();
            } catch (SQLException | RuntimeException ex) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
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
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
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
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
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
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);
        String nguon = ((tenDanhMuc == null ? "" : tenDanhMuc) + " " +
                (tenSanPham == null ? "" : tenSanPham)).toLowerCase(java.util.Locale.ROOT);
        boolean laThucAn = nguon.contains("thức ăn") || nguon.contains("thuc an")
                || nguon.contains("pate") || nguon.contains("hạt chó") || nguon.contains("hạt mèo");
        if (!laThucAn) {
            damBaoBangGoiYThucAn();
            // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
            // lệnh.
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
        // xoa: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String xoa = "DELETE FROM dbo.GoiYThucAn WHERE maSanPham = ?";
        // them: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String them = "INSERT INTO dbo.GoiYThucAn(maSanPham, loaiThuCung, nhomTuoi, kichThuoc, ghiChu) VALUES (?, ?, ?, ?, N'Nhập từ form sản phẩm')";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection con = connectDB.getConnection()) {
            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng
            // nhau.
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
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
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
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.SAN_PHAM);
        damBaoBangGoiYThucAn();
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection con = connectDB.getConnection()) {
            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng
            // nhau.
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
                    ps.setString(1, maSanPham);
                    ps.setString(2, maSanPham);
                    ps.setString(3, maSanPham);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException(
                                    "Không thể xóa sản phẩm đã phát sinh lịch sử nhập kho, hóa đơn hoặc kiểm kê.");
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM dbo.GoiYThucAn WHERE maSanPham=?")) {
                    ps.setString(1, maSanPham);
                    ps.executeUpdate();
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

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
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

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
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
