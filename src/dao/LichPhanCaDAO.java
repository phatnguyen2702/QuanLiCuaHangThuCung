package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ConnectDB.connectDB;
import control.PhanQuyenControl;
import entity.CaLam;
import entity.LichPhanCa;
import entity.NhanVien;

public class LichPhanCaDAO {
    private static final String TRUY_VAN_LICH = "SELECT pc.maPhanCa, pc.ngayLamViec, pc.maNhanVien, nv.hoTen, "
            + "pc.maCa, c.tenCa, c.gioBatDau, c.gioKetThuc "
            + "FROM LichPhanCa pc "
            + "JOIN NhanVien nv ON nv.maNhanVien = pc.maNhanVien "
            + "JOIN CaLam c ON c.maCa = pc.maCa ";

    private static LichPhanCa docPhanCa(ResultSet rs) throws SQLException {
        java.sql.Date ngay = rs.getDate("ngayLamViec");
        LocalDate ngayLamViec = ngay == null ? null : ngay.toLocalDate();
        NhanVien nhanVien = new NhanVien();
        nhanVien.setMaNhanVien(rs.getString("maNhanVien"));
        nhanVien.setHoTen(rs.getString("hoTen"));

        CaLam caLam = new CaLam();
        caLam.setMaCa(rs.getString("maCa"));
        caLam.setTenCa(rs.getString("tenCa"));
        caLam.setGioBatDau(layGio(rs, "gioBatDau"));
        caLam.setGioKetThuc(layGio(rs, "gioKetThuc"));

        LichPhanCa pc = new LichPhanCa(
                rs.getString("maPhanCa"), ngayLamViec,
                nhanVien, caLam);
        pc.setHoTen(rs.getString("hoTen"));
        pc.setTenCa(rs.getString("tenCa"));
        pc.setGioBatDau(ghepNgayGio(ngayLamViec, layGio(rs, "gioBatDau")));
        pc.setGioKetThuc(ghepNgayGio(ngayLamViec, layGio(rs, "gioKetThuc")));
        return pc;
    }

    private static LocalDateTime ghepNgayGio(LocalDate ngayLamViec, LocalTime gio) {
        return ngayLamViec == null || gio == null ? null : LocalDateTime.of(ngayLamViec, gio);
    }

    private static String dinhDangGio(ResultSet rs, String cot) throws SQLException {
        LocalTime gio = layGio(rs, cot);
        return String.format("%02d:%02d", gio.getHour(), gio.getMinute());
    }

    private static LocalTime layGio(ResultSet rs, String cot) throws SQLException {
        Time gio = rs.getTime(cot);
        if (gio == null)
            throw new SQLException("Giờ ca không được để trống" + cot);
        return gio.toLocalTime();
    }

    public ArrayList<LichPhanCa> getAll() {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.PHAN_CA);
        ArrayList<LichPhanCa> list = new ArrayList<>();
        String sql = TRUY_VAN_LICH
                + "ORDER BY pc.ngayLamViec DESC, pc.maPhanCa";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(docPhanCa(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean them(LichPhanCa pc) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.PHAN_CA);
        String sql = "INSERT INTO LichPhanCa(maPhanCa, ngayLamViec,maNhanVien,maCa)VALUES (?,?,?,?)";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pc.getMaPhanCa());
            ps.setDate(2, java.sql.Date.valueOf(pc.getNgayLamViec()));
            ps.setString(3, pc.getMaNhanVien());
            ps.setString(4, pc.getMaCa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sua(LichPhanCa pc) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.PHAN_CA);
        String sql = "UPDATE LichPhanCa SET ngayLamViec=?, maNhanVien=?,maCa=?" +
                "WHERE maPhanCa =? AND ngayLamViec >= CONVERT(date, GETDATE())" +
                "AND ? >= CONVERT(date, GETDATE())";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            java.sql.Date ngay = java.sql.Date.valueOf(pc.getNgayLamViec());
            ps.setDate(1, ngay);
            ps.setString(2, pc.getMaNhanVien());
            ps.setString(3, pc.getMaCa());
            ps.setString(4, pc.getMaPhanCa());
            ps.setDate(5, ngay);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean xoa(String maPhanCa) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.PHAN_CA);
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM LichPhanCa WHERE maPhanCa=?")) {
            ps.setString(1, maPhanCa);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean daPhanCa(String maNhanVien, String maCa, java.sql.Date ngayLamViec) {
        String sql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM LichPhanCa WHERE maNhanVien=? AND maCa=? AND ngayLamViec=?) "
                + "THEN 1 ELSE 0 END";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            ps.setString(2, maCa);
            ps.setDate(3, ngayLamViec);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String taoMaPhanCa() {
        String sql = "SELECT MAX(maPhanCa) FROM LichPhanCa WHERE maPhanCa LIKE 'PC%'";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String ma = rs.getString(1);
                if (ma != null && ma.length() >= 3)
                    return String.format("PC%03d", Integer.parseInt(ma.substring(2)) + 1);
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
        return "PC001";
    }

    public ArrayList<String[]> getDanhSachNhanVien() {
        ArrayList<String[]> ds = new ArrayList<>();
        String sql = "SELECT maNhanVien, hoTen FROM NhanVien ORDER BY maNhanVien";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                ds.add(new String[] { rs.getString(1), rs.getString(2) });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public ArrayList<String[]> getDanhSachCa() {
        ArrayList<String[]> ds = new ArrayList<>();
        String sql = "SELECT maCa, tenCa, gioBatDau, gioKetThuc FROM CaLam ORDER BY maCa";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                ds.add(new String[] {
                        rs.getString("maCa"), rs.getString("tenCa"),
                        dinhDangGio(rs, "gioBatDau"), dinhDangGio(rs, "gioKetThuc") });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public ArrayList<LichPhanCa> layPhanCaTheoNgay(LocalDate ngay) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.PHAN_CA);
        if (ngay == null)
            throw new IllegalArgumentException("Ngày không được rỗng");
        ArrayList<LichPhanCa> list = new ArrayList<>();
        String sql = TRUY_VAN_LICH +
                "WHERE pc.ngayLamViec = ? " +
                "ORDER BY c.gioBatDau, nv.hoTen, pc.maCa";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(docPhanCa(rs));
                }
            }
        }
        return list;
    }

    public ArrayList<LichPhanCa> layPhanCaTrongTuan(LocalDate ngayTrongTuan) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.PHAN_CA);
        if (ngayTrongTuan == null)
            throw new IllegalArgumentException("Ngày không được rỗng");
        LocalDate dauTuan = ngayTrongTuan.with(DayOfWeek.MONDAY);
        ArrayList<LichPhanCa> list = new ArrayList<>();
        String sql = TRUY_VAN_LICH +
                "WHERE pc.ngayLamViec BETWEEN ? AND ? " +
                "ORDER BY nv.hoTen, pc.ngayLamViec, c.gioBatDau";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(dauTuan));
            ps.setDate(2, java.sql.Date.valueOf(dauTuan.plusDays(6)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(docPhanCa(rs));
                }
            }
        }
        return list;
    }

    private static final class KhungCa {
        final String maNhanVien;
        final LocalDateTime batDau, ketThuc;

        KhungCa(LocalDate ngay, String maNV, LocalTime gioDau, LocalTime gioCuoi) {
            maNhanVien = maNV;
            batDau = ngay.atTime(gioDau);
            ketThuc = ngay.atTime(gioCuoi);
        }

        boolean trungGio(KhungCa ca) {
            return maNhanVien.equalsIgnoreCase(ca.maNhanVien) &&
                    batDau.isBefore(ca.ketThuc) && ca.batDau.isBefore(ketThuc);
        }
    }

    public boolean trungGio(String maNhanVien, String maCa, LocalDate ngay, String boQuaMaPhanCa)
            throws SQLException {
        String sql = "SELECT pc.maPhanCa, pc.ngayLamViec, c.gioBatDau, c.gioKetThuc " +
                "FROM LichPhanCa pc JOIN CaLam c ON c.maCa=pc.maCa " +
                "WHERE pc.maNhanVien=? AND pc.ngayLamViec=?";
        try (Connection con = connectDB.getConnection();
                PreparedStatement psCa = con.prepareStatement(
                        "SELECT gioBatDau, gioKetThuc FROM CaLam WHERE maCa=?")) {
            psCa.setString(1, maCa);
            KhungCa canKiemTra;
            try (ResultSet rsCa = psCa.executeQuery()) {
                if (!rsCa.next())
                    throw new SQLException("Không tìm thấy ca làm: " + maCa);
                canKiemTra = new KhungCa(ngay, maNhanVien,
                        layGio(rsCa, "gioBatDau"), layGio(rsCa, "gioKetThuc"));
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maNhanVien);
                ps.setDate(2, java.sql.Date.valueOf(ngay));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (boQuaMaPhanCa != null && boQuaMaPhanCa.equals(rs.getString("maPhanCa")))
                            continue;
                        KhungCa daCo = new KhungCa(rs.getDate("ngayLamViec").toLocalDate(), maNhanVien,
                                layGio(rs, "gioBatDau"), layGio(rs, "gioKetThuc"));
                        if (canKiemTra.trungGio(daCo))
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public static final class KetQuaSaoChep {
        private final int tongNguon, daThem, boQuaTrungGio, boQuaQuaKhu;

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

    private List<LichPhanCa> layLichNguon(Connection con, LocalDate dauTuan) throws SQLException {
        String sql = "SELECT pc.ngayLamViec, pc.maNhanVien, pc.maCa, c.gioBatDau, c.gioKetThuc " +
                "FROM LichPhanCa pc JOIN CaLam c ON c.maCa = pc.maCa " +
                "WHERE pc.ngayLamViec BETWEEN ? AND ? " +
                "ORDER BY pc.ngayLamViec, pc.maNhanVien, c.gioBatDau";
        List<LichPhanCa> ds = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(dauTuan));
            ps.setDate(2, java.sql.Date.valueOf(dauTuan.plusDays(6)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maCa = rs.getString("maCa");
                    LichPhanCa pc = new LichPhanCa(null,
                            rs.getDate("ngayLamViec").toLocalDate(),
                            rs.getString("maNhanVien"), maCa);
                    CaLam ca = new CaLam();
                    ca.setMaCa(maCa);
                    ca.setGioBatDau(layGio(rs, "gioBatDau"));
                    ca.setGioKetThuc(layGio(rs, "gioKetThuc"));
                    pc.setCaLam(ca);
                    ds.add(pc);
                }
            }
        }
        return ds;
    }

    private List<KhungCa> layLichDaCo(Connection con, LocalDate dauTuan) throws SQLException {
        String sql = "SELECT pc.ngayLamViec, pc.maNhanVien, c.gioBatDau, c.gioKetThuc " +
                "FROM LichPhanCa pc " +
                "JOIN CaLam c ON c.maCa = pc.maCa " +
                "WHERE pc.ngayLamViec BETWEEN ? AND ?";
        List<KhungCa> ds = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(dauTuan));
            ps.setDate(2, java.sql.Date.valueOf(dauTuan.plusDays(6)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ds.add(new KhungCa(rs.getDate("ngayLamViec").toLocalDate(),
                            rs.getString("maNhanVien"), layGio(rs, "gioBatDau"), layGio(rs, "gioKetThuc")));
            }
        }
        return ds;
    }

    private int soPhanCaLonNhat(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maPhanCa, 3, 10))), 0) " +
                        "FROM LichPhanCa " +
                        "WHERE maPhanCa LIKE 'PC[0-9]%'");
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public KetQuaSaoChep saoChepLichTheoTuan(LocalDate ngayNguon, LocalDate ngayDich)
            throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.PHAN_CA);
        if (ngayNguon == null || ngayDich == null)
            throw new IllegalArgumentException("Vui lòng chọn tuần nguồn và tuần đích.");
        LocalDate dauNguon = ngayNguon.with(DayOfWeek.MONDAY);
        LocalDate dauDich = ngayDich.with(DayOfWeek.MONDAY);
        if (dauNguon.equals(dauDich))
            throw new IllegalArgumentException("Tuần nguồn và tuần đích phải khác nhau.");
        if (dauDich.plusDays(6).isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Không thể sao chép lịch vào tuần đã qua.");

        String sqlThem = "INSERT INTO LichPhanCa (maPhanCa, ngayLamViec, maNhanVien, maCa) VALUES (?, ?, ?, ?)";
        try (Connection con = connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                List<LichPhanCa> lichNguon = layLichNguon(con, dauNguon);
                List<KhungCa> daCo = layLichDaCo(con, dauDich);
                int soLonNhat = soPhanCaLonNhat(con);
                int them = 0, trung = 0, quaKhu = 0;
                LocalDate homNay = LocalDate.now();

                try (PreparedStatement ps = con.prepareStatement(sqlThem)) {
                    for (LichPhanCa pc : lichNguon) {
                        LocalDate ngayMoi = dauDich.plusDays(
                                ChronoUnit.DAYS.between(dauNguon, pc.getNgayLamViec()));
                        if (ngayMoi.isBefore(homNay)) {
                            quaKhu++;
                            continue;
                        }

                        CaLam ca = pc.getCaLam();
                        KhungCa caMoi = new KhungCa(ngayMoi, pc.getMaNhanVien(),
                                ca.getGioBatDau(), ca.getGioKetThuc());
                        if (daCo.stream().anyMatch(caMoi::trungGio)) {
                            trung++;
                            continue;
                        }

                        String maMoi = String.format(Locale.ROOT, "PC%03d", ++soLonNhat);
                        ps.setString(1, maMoi);
                        ps.setDate(2, java.sql.Date.valueOf(ngayMoi));
                        ps.setString(3, pc.getMaNhanVien());
                        ps.setString(4, pc.getMaCa());
                        if (ps.executeUpdate() != 1)
                            throw new SQLException("Không lưu được lịch phân ca " + maMoi);
                        daCo.add(caMoi);
                        them++;
                    }
                }
                con.commit();
                return new KetQuaSaoChep(lichNguon.size(), them, trung, quaKhu);
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}