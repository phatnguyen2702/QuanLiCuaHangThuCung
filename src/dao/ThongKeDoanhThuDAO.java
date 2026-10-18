package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ConnectDB.connectDB;

public class ThongKeDoanhThuDAO {

    public enum CheDo {
        NGAY("Ngày"),
        THANG("Tháng"),
        NAM("Năm");

        private final String nhan;

        CheDo(String nhan) {
            this.nhan = nhan;
        }

        @Override
        public String toString() {
            return nhan;
        }
    }

    public static final class DongBieuDo {
        private final int thuTu;
        private final BigDecimal doanhThu;

        private DongBieuDo(int thuTu, BigDecimal doanhThu) {
            this.thuTu = thuTu;
            this.doanhThu = doanhThu == null ? BigDecimal.ZERO : doanhThu;
        }

        public int getThuTu() {
            return thuTu;
        }

        public BigDecimal getDoanhThu() {
            return doanhThu;
        }
    }

    public static final class DongMatHang {
        private final String tenMatHang;
        private final int soLuong;
        private final BigDecimal doanhThu;

        private DongMatHang(String tenMatHang, int soLuong, BigDecimal doanhThu) {
            this.tenMatHang = tenMatHang;
            this.soLuong = soLuong;
            this.doanhThu = doanhThu == null ? BigDecimal.ZERO : doanhThu;
        }

        public String getTenMatHang() {
            return tenMatHang;
        }

        public int getSoLuong() {
            return soLuong;
        }

        public BigDecimal getDoanhThu() {
            return doanhThu;
        }
    }

    public static final class BaoCao {
        private final BigDecimal tongDoanhThu;
        private final int tongHoaDon;
        private final BigDecimal doanhThuTrungBinh;
        private final List<DongBieuDo> bieuDo;
        private final List<DongMatHang> matHangs;

        private BaoCao(
                BigDecimal tongDoanhThu,
                int tongHoaDon,
                BigDecimal doanhThuTrungBinh,
                List<DongBieuDo> bieuDo,
                List<DongMatHang> matHangs) {
            this.tongDoanhThu = tongDoanhThu == null ? BigDecimal.ZERO : tongDoanhThu;
            this.tongHoaDon = tongHoaDon;
            this.doanhThuTrungBinh = doanhThuTrungBinh == null
                    ? BigDecimal.ZERO
                    : doanhThuTrungBinh;
            this.bieuDo = Collections.unmodifiableList(new ArrayList<>(bieuDo));
            this.matHangs = Collections.unmodifiableList(new ArrayList<>(matHangs));
        }

        public BigDecimal getTongDoanhThu() {
            return tongDoanhThu;
        }

        public int getTongHoaDon() {
            return tongHoaDon;
        }

        public BigDecimal getDoanhThuTrungBinh() {
            return doanhThuTrungBinh;
        }

        public List<DongBieuDo> getBieuDo() {
            return bieuDo;
        }

        public List<DongMatHang> getMatHangs() {
            return matHangs;
        }
    }

    public BaoCao layBaoCao(CheDo cheDo, LocalDate ngayThamChieu) throws SQLException {
        if (cheDo == null || ngayThamChieu == null) {
            throw new IllegalArgumentException("Thiếu kỳ thống kê doanh thu.");
        }

        LocalDateTime batDau;
        LocalDateTime ketThuc;
        int soMoc;
        String cotNhom;
        switch (cheDo) {
            case NGAY:
                batDau = ngayThamChieu.atStartOfDay();
                ketThuc = batDau.plusDays(1);
                soMoc = 24;
                cotNhom = "DATEPART(HOUR, hd.ngayLap)";
                break;
            case THANG:
                batDau = YearMonth.from(ngayThamChieu).atDay(1).atStartOfDay();
                ketThuc = batDau.plusMonths(1);
                soMoc = YearMonth.from(ngayThamChieu).lengthOfMonth();
                cotNhom = "DATEPART(DAY, hd.ngayLap)";
                break;
            case NAM:
                batDau = ngayThamChieu.withDayOfYear(1).atStartOfDay();
                ketThuc = batDau.plusYears(1);
                soMoc = 12;
                cotNhom = "DATEPART(MONTH, hd.ngayLap)";
                break;
            default:
                throw new IllegalArgumentException("Kiểu thống kê không được hỗ trợ.");
        }

        BigDecimal tongDoanhThu = BigDecimal.ZERO;
        int tongHoaDon;
        List<DongBieuDo> bieuDo = new ArrayList<>();
        for (int i = 1; i <= soMoc; i++) {
            bieuDo.add(new DongBieuDo(
                    cheDo == CheDo.NGAY ? i - 1 : i,
                    BigDecimal.ZERO));
        }
        List<DongMatHang> matHangs = new ArrayList<>();

        String tongHoaDonSql = "SELECT COUNT(*) FROM HoaDon "
                + "WHERE ngayLap >= ? AND ngayLap < ?";
        String bieuDoSql = "SELECT " + cotNhom + " AS moc, "
                + "COALESCE(SUM(ct.soLuong * ct.donGia "
                + "* (1 - COALESCE(ct.phanTramGiam, 0) / 100.0)), 0) AS doanhThu "
                + "FROM HoaDon hd JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon "
                + "WHERE hd.ngayLap >= ? AND hd.ngayLap < ? "
                + "GROUP BY " + cotNhom + " ORDER BY moc";
        String matHangSql = "SELECT "
                + "COALESCE(dm.tenDanhMuc, "
                + "CASE WHEN dv.maDichVu IS NOT NULL THEN N'Dịch vụ' "
                + "WHEN tc.maThuCung IS NOT NULL THEN N'Thú cưng' ELSE N'Khác' END) "
                + "+ N' / ' + COALESCE(sp.tenSanPham, dv.tenDichVu, tc.tenThuCung, N'Không rõ') "
                + "AS tenMatHang, SUM(ct.soLuong) AS soLuong, "
                + "SUM(ct.soLuong * ct.donGia "
                + "* (1 - COALESCE(ct.phanTramGiam, 0) / 100.0)) AS doanhThu "
                + "FROM HoaDon hd JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon "
                + "LEFT JOIN SanPham sp ON sp.maSanPham = ct.maSanPham "
                + "LEFT JOIN DanhMuc dm ON dm.maDanhMuc = sp.maDanhMuc "
                + "LEFT JOIN DichVu dv ON dv.maDichVu = ct.maDichVu "
                + "LEFT JOIN ThuCung tc ON tc.maThuCung = ct.maThuCung "
                + "WHERE hd.ngayLap >= ? AND hd.ngayLap < ? "
                + "GROUP BY dm.tenDanhMuc, dv.maDichVu, tc.maThuCung, "
                + "sp.tenSanPham, dv.tenDichVu, tc.tenThuCung "
                + "ORDER BY doanhThu DESC, tenMatHang";

        try (Connection con = connectDB.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(tongHoaDonSql)) {
                ps.setTimestamp(1, java.sql.Timestamp.valueOf(batDau));
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(ketThuc));
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Không đọc được số lượng hóa đơn.");
                    }
                    tongHoaDon = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(bieuDoSql)) {
                ps.setTimestamp(1, java.sql.Timestamp.valueOf(batDau));
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(ketThuc));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int moc = rs.getInt("moc");
                        if (moc >= 0 && moc < bieuDo.size()) {
                            int viTri = cheDo == CheDo.NGAY ? moc : moc - 1;
                            if (viTri >= 0 && viTri < bieuDo.size()) {
                                bieuDo.set(viTri, new DongBieuDo(
                                        bieuDo.get(viTri).getThuTu(),
                                        rs.getBigDecimal("doanhThu")));
                            }
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(matHangSql)) {
                ps.setTimestamp(1, java.sql.Timestamp.valueOf(batDau));
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(ketThuc));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DongMatHang dong = new DongMatHang(
                                rs.getString("tenMatHang"),
                                rs.getInt("soLuong"),
                                rs.getBigDecimal("doanhThu"));
                        matHangs.add(dong);
                        tongDoanhThu = tongDoanhThu.add(dong.getDoanhThu());
                    }
                }
            }
        }

        BigDecimal trungBinh = tongHoaDon == 0 ? BigDecimal.ZERO
                : tongDoanhThu.divide(BigDecimal.valueOf(tongHoaDon), 0,
                        java.math.RoundingMode.HALF_UP);
        return new BaoCao(tongDoanhThu, tongHoaDon, trungBinh, bieuDo, matHangs);
    }
}
