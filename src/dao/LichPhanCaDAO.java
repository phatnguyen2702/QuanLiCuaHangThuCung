package dao;

import UI.PhanQuyen;

import entity.LichPhanCa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import ConnectDB.connectDB;

// Lớp truy cập cơ sở dữ liệu cho lịch phân ca.
public class LichPhanCaDAO {

    // Cập nhật dữ liệu lịch phân ca trong cơ sở dữ liệu.
    // Tham số pc: lịch phân ca cần xử lý.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean sua(LichPhanCa pc) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.PHAN_CA);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "UPDATE LichPhanCa SET ngayLamViec=?, maNhanVien=?, maCa=? "
                + "WHERE maPhanCa=? AND ngayLamViec >= CONVERT(date, GETDATE()) "
                + "AND ? >= CONVERT(date, GETDATE())";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(pc.getNgayLamViec()));
            ps.setString(2, pc.getMaNhanVien());
            ps.setString(3, pc.getMaCa());
            ps.setString(4, pc.getMaPhanCa());
            ps.setDate(5, java.sql.Date.valueOf(pc.getNgayLamViec()));
            return ps.executeUpdate() == 1;
        }
    }

    // Kiểm tra ca làm có trùng thời gian với lịch phân ca đã có hay không.
    // Tham số maNhanVien: mã nhân viên.
    // Tham số maCa: mã ca.
    // Tham số ngay: ngày.
    // Tham số boQuaMaPhanCa: mã phân ca bỏ qua khi kiểm tra trùng.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean trungGio(String maNhanVien, String maCa, LocalDate ngay, String boQuaMaPhanCa)
            throws SQLException {
        // gioMoi: Mảng lưu giờ mới.
        LocalTime[] gioMoi = layGioCa(maCa);
        // batDau: Lưu bắt đầu dưới dạng dữ liệu thời gian.
        LocalDateTime batDau = LocalDateTime.of(ngay, gioMoi[0]);
        // ketThuc: Lưu kết thực dưới dạng dữ liệu thời gian.
        LocalDateTime ketThuc = LocalDateTime.of(ngay, gioMoi[1]);
        if (!ketThuc.isAfter(batDau))
            ketThuc = ketThuc.plusDays(1);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "SELECT pc.maPhanCa, pc.ngayLamViec, c.gioBatDau, c.gioKetThuc "
                + "FROM LichPhanCa pc JOIN CaLam c ON c.maCa=pc.maCa "
                + "WHERE pc.maNhanVien=? AND pc.ngayLamViec BETWEEN DATEADD(day,-1,?) AND DATEADD(day,1,?)";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection con = connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            ps.setDate(2, java.sql.Date.valueOf(ngay));
            ps.setDate(3, java.sql.Date.valueOf(ngay));
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
                while (rs.next()) {
                    if (boQuaMaPhanCa != null && boQuaMaPhanCa.equals(rs.getString("maPhanCa")))
                        continue;
                    // ngayCu: Lưu ngày cụ dưới dạng dữ liệu thời gian.
                    LocalDate ngayCu = rs.getDate("ngayLamViec").toLocalDate();
                    // dauCu: Lưu đầu cụ dưới dạng dữ liệu thời gian.
                    LocalDateTime dauCu = LocalDateTime.of(ngayCu, LocalTime.parse(rs.getString("gioBatDau").trim()));
                    // cuoiCu: Lưu cuối cụ dưới dạng dữ liệu thời gian.
                    LocalDateTime cuoiCu = LocalDateTime.of(ngayCu, LocalTime.parse(rs.getString("gioKetThuc").trim()));
                    if (!cuoiCu.isAfter(dauCu))
                        cuoiCu = cuoiCu.plusDays(1);
                    if (batDau.isBefore(cuoiCu) && dauCu.isBefore(ketThuc))
                        return true;
                }
            }
        }
        return false;
    }

    // Lấy giờ ca.
    // Tham số maCa: mã ca.
    // Kết quả: đối tượng/giá trị kiểu LocalTime[].
    private LocalTime[] layGioCa(String maCa) throws SQLException {
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT gioBatDau, gioKetThuc FROM CaLam WHERE maCa=?")) {
            ps.setString(1, maCa);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    throw new SQLException("Không tìm thấy ca làm: " + maCa);
                return new LocalTime[] { LocalTime.parse(rs.getString(1).trim()),
                        LocalTime.parse(rs.getString(2).trim()) };
            }
        }
    }

    // LAY TAT CA LICH PHAN CA
    // Lấy toàn bộ danh sách bản ghi từ cơ sở dữ liệu.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<LichPhanCa>.
    public ArrayList<LichPhanCa> getAll() {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.PHAN_CA);
        // list: Danh sách chứa các phần tử kiểu LichPhanCa.
        ArrayList<LichPhanCa> list = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "SELECT pc.maPhanCa, pc.ngayLamViec, " +
                "pc.maNhanVien, nv.hoTen, " +
                "pc.maCa, c.tenCa, c.gioBatDau, c.gioKetThuc " +
                "FROM LichPhanCa pc " +
                "INNER JOIN NhanVien nv " +
                "ON pc.maNhanVien = nv.maNhanVien " +
                "INNER JOIN CaLam c " +
                "ON pc.maCa = c.maCa " +
                "ORDER BY pc.ngayLamViec DESC, pc.maPhanCa";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                LichPhanCa pc = new LichPhanCa();
                pc.setMaPhanCa(
                        rs.getString("maPhanCa"));
                if (rs.getDate("ngayLamViec") != null) {
                    pc.setNgayLamViec(
                            rs.getDate("ngayLamViec").toLocalDate());
                }
                pc.setMaNhanVien(rs.getString("maNhanVien"));
                pc.setHoTen(rs.getString("hoTen"));
                pc.setMaCa(rs.getString("maCa"));
                pc.setTenCa(rs.getString("tenCa"));
                pc.setGioBatDau(formatGio(rs.getString("gioBatDau")));
                pc.setGioKetThuc(formatGio(rs.getString("gioKetThuc")));
                list.add(pc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // THEM PHAN CA
    // Thêm dữ liệu lịch phân ca trong cơ sở dữ liệu.
    // Tham số pc: lịch phân ca cần xử lý.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean them(LichPhanCa pc) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.PHAN_CA);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "INSERT INTO LichPhanCa " +
                "(maPhanCa, ngayLamViec, maNhanVien, maCa) " +
                "VALUES (?, ?, ?, ?)";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pc.getMaPhanCa());
            if (pc.getNgayLamViec() != null) {
                ps.setDate(2, java.sql.Date.valueOf(pc.getNgayLamViec()));

            } else {

                ps.setNull(2, java.sql.Types.DATE);
            }
            ps.setString(3, pc.getMaNhanVien());
            ps.setString(4, pc.getMaCa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // XOA PHAN CA
    // Xử lý xóa dữ liệu lịch phân ca trong cơ sở dữ liệu.
    // Tham số maPhanCa: mã phân ca.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean xoa(String maPhanCa) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.PHAN_CA);
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "DELETE FROM LichPhanCa " +
                "WHERE maPhanCa = ?";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhanCa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // KIEM TRA NHAN VIEN DA DUOC PHAN CA CHUA
    // Kiểm tra nhân viên đã được phân vào ca trong ngày hay chưa.
    // Tham số maNhanVien: mã nhân viên.
    // Tham số maCa: mã ca.
    // Tham số ngayLamViec: ngày làm việc.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean daPhanCa(
            String maNhanVien,
            String maCa,
            java.sql.Date ngayLamViec) {
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "SELECT COUNT(*) " +
                "FROM LichPhanCa " +
                "WHERE maNhanVien = ? " +
                "AND maCa = ? " +
                "AND ngayLamViec = ?";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            ps.setString(2, maCa);
            ps.setDate(3, ngayLamViec);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // TAO MA PHAN CA TU DONG
    // Tạo mã phân ca.
    // Kết quả: chuỗi kết quả.
    public String taoMaPhanCa() {

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "SELECT MAX(maPhanCa) " +
                "FROM LichPhanCa " +
                "WHERE maPhanCa LIKE 'PC%'";

        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maCu = rs.getString(1);
                if (maCu != null && maCu.length() >= 3) {
                    try {
                        int so = Integer.parseInt(
                                maCu.substring(2));
                        return String.format(
                                "PC%03d",
                                so + 1);
                    } catch (NumberFormatException e) {
                        return "PC001";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "PC001";
    }

    // DANH SACH NHAN VIEN
    // Lấy danh sách nhân viên.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<String[]>.
    public ArrayList<String[]> getDanhSachNhanVien() {
        // list: Danh sách chứa các phần tử kiểu String[].
        ArrayList<String[]> list = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "SELECT maNhanVien, hoTen " +
                "FROM NhanVien " +
                "ORDER BY maNhanVien";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                // nv: Mảng lưu NV.
                String[] nv = new String[2];
                nv[0] = rs.getString("maNhanVien");
                nv[1] = rs.getString("hoTen");
                list.add(nv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // DANH SACH CA LAM
    // Lấy danh sách ca.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<String[]>.
    public ArrayList<String[]> getDanhSachCa() {
        // list: Danh sách chứa các phần tử kiểu String[].
        ArrayList<String[]> list = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "SELECT maCa, tenCa, gioBatDau, gioKetThuc " +
                "FROM CaLam " +
                "ORDER BY maCa";
        try (
                Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                // ca: Mảng lưu ca.
                String[] ca = new String[4];
                ca[0] = rs.getString("maCa");
                ca[1] = rs.getString("tenCa");
                ca[2] = formatGio(rs.getString("gioBatDau"));
                ca[3] = formatGio(rs.getString("gioKetThuc"));
                list.add(ca);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Chuẩn hóa chuỗi giờ để hiển thị.
    // Tham số gio: giờ.
    // Kết quả: chuỗi kết quả.
    private String formatGio(String gio) {
        if (gio == null || gio.trim().isEmpty()) {
            return "";
        }
        gio = gio.trim();
        // Vi du:
        // 06:00:00.0000000
        // 14:00:00.0000000
        // index: Lưu chỉ số phần tử.
        int index = gio.indexOf(":");
        if (index < 0) {
            return gio;
        }
        // Lay HH:mm
        if (gio.length() >= 5) {
            return gio.substring(0, 5);
        }
        return gio;
    }

    /** Kết quả sao chép: không ghi đè các phân ca đã tồn tại. */
    public static final class KetQuaSaoChep {
        private final int tongNguon;
        private final int daThem;
        private final int boQuaTrungGio;
        private final int boQuaQuaKhu;

        public KetQuaSaoChep(int tongNguon, int daThem, int boQuaTrungGio, int boQuaQuaKhu) {
            this.tongNguon = tongNguon;
            this.daThem = daThem;
            this.boQuaTrungGio = boQuaTrungGio;
            this.boQuaQuaKhu = boQuaQuaKhu;
        }

        public int getTongNguon() {
            return tongNguon;
        }

        public int getDaThem() {
            return daThem;
        }

        public int getBoQuaTrungGio() {
            return boQuaTrungGio;
        }

        public int getBoQuaQuaKhu() {
            return boQuaQuaKhu;
        }
    }

    /**
     * Các ca được PHÂN CÔNG cho một ngày chính xác; không lệ thuộc bộ lọc giao
     * diện.
     * Ném SQLException khi lỗi kết nối, tránh hiển thị số 0 sai.
     */
    public ArrayList<LichPhanCa> layPhanCaTheoNgay(LocalDate ngay) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.PHAN_CA);
        if (ngay == null)
            throw new IllegalArgumentException("Ngày thống kê không được rỗng");
        String sql = "SELECT pc.maPhanCa, pc.ngayLamViec, pc.maNhanVien, nv.hoTen, "
                + "pc.maCa, c.tenCa, c.gioBatDau, c.gioKetThuc "
                + "FROM LichPhanCa pc "
                + "JOIN NhanVien nv ON nv.maNhanVien = pc.maNhanVien "
                + "JOIN CaLam c ON c.maCa = pc.maCa "
                + "WHERE pc.ngayLamViec = ? "
                + "ORDER BY c.gioBatDau, nv.hoTen, pc.maCa";
        ArrayList<LichPhanCa> ds = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LichPhanCa pc = new LichPhanCa(
                            rs.getString("maPhanCa"),
                            rs.getDate("ngayLamViec").toLocalDate(),
                            rs.getString("maNhanVien"), rs.getString("maCa"));
                    pc.setHoTen(rs.getString("hoTen"));
                    pc.setTenCa(rs.getString("tenCa"));
                    pc.setGioBatDau(formatGio(rs.getString("gioBatDau")));
                    pc.setGioKetThuc(formatGio(rs.getString("gioKetThuc")));
                    ds.add(pc);
                }
            }
        }
        return ds;
    }

    /**
     * Lấy phân ca trong một tuần (thứ Hai đến Chủ nhật). Khác getAll(),
     * lỗi truy vấn được ném lên giao diện thay vì bị nuốt và hiện bảng trống.
     */
    public ArrayList<LichPhanCa> layPhanCaTrongTuan(LocalDate ngayTrongTuan) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.PHAN_CA);
        LocalDate dauTuan = ngayTrongTuan.with(java.time.DayOfWeek.MONDAY);
        String sql = "SELECT pc.maPhanCa, pc.ngayLamViec, pc.maNhanVien, nv.hoTen, "
                + "pc.maCa, c.tenCa, c.gioBatDau, c.gioKetThuc "
                + "FROM LichPhanCa pc JOIN NhanVien nv ON nv.maNhanVien=pc.maNhanVien "
                + "JOIN CaLam c ON c.maCa=pc.maCa "
                + "WHERE pc.ngayLamViec BETWEEN ? AND ? "
                + "ORDER BY nv.hoTen, pc.ngayLamViec, c.gioBatDau";
        ArrayList<LichPhanCa> danhSach = new ArrayList<>();
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(dauTuan));
            ps.setDate(2, java.sql.Date.valueOf(dauTuan.plusDays(6)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LichPhanCa pc = new LichPhanCa(
                            rs.getString("maPhanCa"),
                            rs.getDate("ngayLamViec").toLocalDate(),
                            rs.getString("maNhanVien"), rs.getString("maCa"));
                    pc.setHoTen(rs.getString("hoTen"));
                    pc.setTenCa(rs.getString("tenCa"));
                    pc.setGioBatDau(formatGio(rs.getString("gioBatDau")));
                    pc.setGioKetThuc(formatGio(rs.getString("gioKetThuc")));
                    danhSach.add(pc);
                }
            }
        }
        return danhSach;
    }

    /** Dùng ngày giờ thực tế, kể cả ca qua đêm, để so sánh trùng. */
    private static final class KhungCa {
        final LocalDateTime batDau;
        final LocalDateTime ketThuc;
        final String maNhanVien;

        KhungCa(LocalDate ngay, String maNhanVien, LocalTime gioDau, LocalTime gioCuoi) {
            this.maNhanVien = maNhanVien;
            this.batDau = ngay.atTime(gioDau);
            LocalDateTime ketThucTam = ngay.atTime(gioCuoi);
            this.ketThuc = ketThucTam.isAfter(batDau) ? ketThucTam : ketThucTam.plusDays(1);
        }

        boolean trungGio(KhungCa khac) {
            return maNhanVien.equalsIgnoreCase(khac.maNhanVien)
                    && batDau.isBefore(khac.ketThuc) && khac.batDau.isBefore(ketThuc);
        }
    }

    /**
     * Sao chép từ tuần nguồn sang tuần đích bằng một giao dịch SQL Server.
     * Không xóa hay ghi đè phân ca đích. Bỏ qua ngày đã qua và ca bị chồng giờ
     * (bao gồm ca kéo qua nửa đêm). Tạo mã PC mới ngay trong transaction.
     */
    public KetQuaSaoChep saoChepLichTheoTuan(LocalDate ngayNguon, LocalDate ngayDich)
            throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.PHAN_CA);
        if (ngayNguon == null || ngayDich == null)
            throw new IllegalArgumentException("Vui lòng chọn tuần nguồn và tuần đích.");
        LocalDate dauNguon = ngayNguon.with(java.time.DayOfWeek.MONDAY);
        LocalDate dauDich = ngayDich.with(java.time.DayOfWeek.MONDAY);
        if (dauNguon.equals(dauDich))
            throw new IllegalArgumentException("Tuần nguồn và tuần đích phải khác nhau.");
        if (dauDich.plusDays(6).isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Không thể sao chép lịch vào tuần đã qua.");

        String sqlNguon = "SELECT pc.ngayLamViec, pc.maNhanVien, pc.maCa, "
                + "c.gioBatDau, c.gioKetThuc FROM LichPhanCa pc WITH (HOLDLOCK) "
                + "JOIN CaLam c ON c.maCa=pc.maCa "
                + "WHERE pc.ngayLamViec BETWEEN ? AND ? "
                + "ORDER BY pc.ngayLamViec, pc.maNhanVien, c.gioBatDau";
        String sqlLanCan = "SELECT pc.ngayLamViec, pc.maNhanVien, "
                + "c.gioBatDau, c.gioKetThuc FROM LichPhanCa pc WITH (UPDLOCK, HOLDLOCK) "
                + "JOIN CaLam c ON c.maCa=pc.maCa "
                + "WHERE pc.ngayLamViec BETWEEN ? AND ?";
        String sqlMa = "SELECT maPhanCa FROM LichPhanCa WITH (UPDLOCK, HOLDLOCK)";
        String sqlThem = "INSERT INTO LichPhanCa (maPhanCa, ngayLamViec, maNhanVien, maCa) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = connectDB.getConnection()) {
            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            con.setAutoCommit(false);
            try {
                java.util.List<LichPhanCa> lichNguon = new ArrayList<>();
                java.util.Map<String, LocalTime[]> gioTheoCa = new java.util.HashMap<>();
                try (PreparedStatement ps = con.prepareStatement(sqlNguon)) {
                    ps.setDate(1, java.sql.Date.valueOf(dauNguon));
                    ps.setDate(2, java.sql.Date.valueOf(dauNguon.plusDays(6)));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            LocalDate ngay = rs.getDate("ngayLamViec").toLocalDate();
                            String maNV = rs.getString("maNhanVien");
                            String maCa = rs.getString("maCa");
                            lichNguon.add(new LichPhanCa(null, ngay, maNV, maCa));
                            gioTheoCa.put(maCa, new LocalTime[] {
                                    LocalTime.parse(rs.getString("gioBatDau").trim()),
                                    LocalTime.parse(rs.getString("gioKetThuc").trim()) });
                        }
                    }
                }

                java.util.List<KhungCa> daCo = new ArrayList<>();
                // Lấy thêm ngày trước và ngày sau để phát hiện ca qua đêm.
                try (PreparedStatement ps = con.prepareStatement(sqlLanCan)) {
                    ps.setDate(1, java.sql.Date.valueOf(dauDich.minusDays(1)));
                    ps.setDate(2, java.sql.Date.valueOf(dauDich.plusDays(7)));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            daCo.add(new KhungCa(
                                    rs.getDate("ngayLamViec").toLocalDate(),
                                    rs.getString("maNhanVien"),
                                    LocalTime.parse(rs.getString("gioBatDau").trim()),
                                    LocalTime.parse(rs.getString("gioKetThuc").trim())));
                        }
                    }
                }

                int soLonNhat = 0;
                // Khóa dãy mã PC trong transaction để hạn chế trùng mã khi chép đồng thời.
                try (PreparedStatement ps = con.prepareStatement(sqlMa);
                        ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String ma = rs.getString(1);
                        if (ma != null && ma.matches("PC\\d+")) {
                            try {
                                soLonNhat = Math.max(soLonNhat, Integer.parseInt(ma.substring(2)));
                            } catch (NumberFormatException boQua) {
                                // Bản ghi có mã ngoại lệ; không sử dụng để tính mã mới.
                            }
                        }
                    }
                }

                int them = 0, trung = 0, quaKhu = 0;
                LocalDate homNay = LocalDate.now();
                try (PreparedStatement ps = con.prepareStatement(sqlThem)) {
                    for (LichPhanCa pc : lichNguon) {
                        LocalDate ngayMoi = dauDich.plusDays(
                                java.time.temporal.ChronoUnit.DAYS.between(dauNguon, pc.getNgayLamViec()));
                        if (ngayMoi.isBefore(homNay)) {
                            quaKhu++;
                            continue;
                        }
                        LocalTime[] gio = gioTheoCa.get(pc.getMaCa());
                        KhungCa canThem = new KhungCa(ngayMoi, pc.getMaNhanVien(), gio[0], gio[1]);
                        boolean trungGio = false;
                        for (KhungCa caCu : daCo) {
                            if (canThem.trungGio(caCu)) {
                                trungGio = true;
                                break;
                            }
                        }
                        if (trungGio) {
                            trung++;
                            continue;
                        }
                        String maMoi = String.format(java.util.Locale.ROOT, "PC%03d", ++soLonNhat);
                        if (maMoi.length() > 10)
                            throw new SQLException("Đã vượt giới hạn mã phân ca (10 ký tự).");
                        ps.setString(1, maMoi);
                        ps.setDate(2, java.sql.Date.valueOf(ngayMoi));
                        ps.setString(3, pc.getMaNhanVien());
                        ps.setString(4, pc.getMaCa());
                        if (ps.executeUpdate() != 1)
                            throw new SQLException("Không lưu được lịch phân ca " + maMoi);
                        daCo.add(canThem); // Ngăn trùng giữa chính các phân ca đang sao chép.
                        them++;
                    }
                }
                con.commit();
                return new KetQuaSaoChep(lichNguon.size(), them, trung, quaKhu);
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

}
