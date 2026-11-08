package dao;

import control.PhanQuyenControl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ConnectDB.connectDB;

public class ThongKeDoanhThuDAO {

    private record MatHangKey(
            String tenDanhMuc,
            String maDichVu,
            String maThuCung,
            String tenSanPham,
            String tenDichVu,
            String tenThuCung) {
    }

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
        private final double doanhThu;

        private DongBieuDo(int thuTu, double doanhThu) {
            this.thuTu = thuTu;
            this.doanhThu = doanhThu;
        }

        public int getThuTu() {
            return thuTu;
        }

        public double getDoanhThu() {
            return doanhThu;
        }
    }

    public static final class DongMatHang {
        private final String tenMatHang;
        private final int soLuong;
        private final double doanhThu;

        private DongMatHang(String tenMatHang, int soLuong, double doanhThu) {
            this.tenMatHang = tenMatHang;
            this.soLuong = soLuong;
            this.doanhThu = doanhThu;
        }

        public String getTenMatHang() {
            return tenMatHang;
        }

        public int getSoLuong() {
            return soLuong;
        }

        public double getDoanhThu() {
            return doanhThu;
        }
    }

    public static final class BaoCao {
        private final double tongDoanhThu;
        private final int tongHoaDon;
        private final double doanhThuTrungBinh;
        private final List<DongBieuDo> bieuDo;
        private final List<DongMatHang> matHangs;

        private BaoCao(
                double tongDoanhThu,
                int tongHoaDon,
                double doanhThuTrungBinh,
                List<DongBieuDo> bieuDo,
                List<DongMatHang> matHangs) {
            this.tongDoanhThu = tongDoanhThu;
            this.tongHoaDon = tongHoaDon;
            this.doanhThuTrungBinh = doanhThuTrungBinh;
            this.bieuDo = Collections.unmodifiableList(new ArrayList<>(bieuDo));
            this.matHangs = Collections.unmodifiableList(new ArrayList<>(matHangs));
        }

        public double getTongDoanhThu() {
            return tongDoanhThu;
        }

        public int getTongHoaDon() {
            return tongHoaDon;
        }

        public double getDoanhThuTrungBinh() {
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
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.THONG_KE_DOANH_THU);
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

        double tongDoanhThu = 0;
        int tongHoaDon;
        List<DongBieuDo> bieuDo = new ArrayList<>();
        List<Double> doanhThuTheoMoc = new ArrayList<>();
        for (int i = 1; i <= soMoc; i++) {
            bieuDo.add(new DongBieuDo(
                    cheDo == CheDo.NGAY ? i - 1 : i,
                    0));
            doanhThuTheoMoc.add(0.0);
        }
        List<DongMatHang> matHangs = new ArrayList<>();

        String tongHoaDonSql = "SELECT COUNT(*) FROM HoaDon WHERE ngayLap >= ? AND ngayLap < ?";
        String bieuDoSql = """
                SELECT %1$s AS moc, ct.soLuong, ct.donGia, ct.phanTramGiam
                FROM HoaDon hd JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon
                WHERE hd.ngayLap >= ? AND hd.ngayLap < ?
                """.formatted(cotNhom);
        String matHangSql = """
                SELECT dm.tenDanhMuc, sp.maSanPham, sp.tenSanPham,
                       dv.maDichVu, dv.tenDichVu, tc.maThuCung, tc.tenThuCung,
                       ct.soLuong, ct.donGia, ct.phanTramGiam
                FROM HoaDon hd
                JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon
                LEFT JOIN SanPham sp ON sp.maSanPham = ct.maSanPham
                LEFT JOIN DanhMuc dm ON dm.maDanhMuc = sp.maDanhMuc
                LEFT JOIN DichVu dv ON dv.maDichVu = ct.maDichVu
                LEFT JOIN ThuCung tc ON tc.maThuCung = ct.maThuCung
                WHERE hd.ngayLap >= ? AND hd.ngayLap < ?
                """;

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
                                int soLuong = rs.getInt("soLuong");
                                double donGia = rs.getDouble("donGia");
                                boolean donGiaNull = rs.wasNull();
                                double phanTramGiam = rs.getDouble("phanTramGiam");
                                if (rs.wasNull()) {
                                    phanTramGiam = 0;
                                }
                                if (!donGiaNull) {
                                    double thanhTien = soLuong * donGia
                                            * (1 - phanTramGiam / 100.0);
                                    doanhThuTheoMoc.set(
                                            viTri,
                                            doanhThuTheoMoc.get(viTri) + thanhTien);
                                }
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < bieuDo.size(); i++) {
                bieuDo.set(i, new DongBieuDo(
                        bieuDo.get(i).getThuTu(),
                        doanhThuTheoMoc.get(i)));
            }

            Map<MatHangKey, DongMatHang> matHangTheoMa = new LinkedHashMap<>();
            try (PreparedStatement ps = con.prepareStatement(matHangSql)) {
                ps.setTimestamp(1, java.sql.Timestamp.valueOf(batDau));
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(ketThuc));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String maDichVu = rs.getString("maDichVu");
                        String maThuCung = rs.getString("maThuCung");
                        String tenDanhMuc = rs.getString("tenDanhMuc");
                        String tenSanPham = rs.getString("tenSanPham");
                        String tenDichVu = rs.getString("tenDichVu");
                        String tenThuCung = rs.getString("tenThuCung");
                        String tenHang = rs.getString("tenSanPham");
                        if (tenHang == null) {
                            tenHang = tenDichVu;
                        }
                        if (tenHang == null) {
                            tenHang = tenThuCung;
                        }
                        if (tenHang == null) {
                            tenHang = "Không rõ";
                        }

                        String nhomHang = tenDanhMuc != null ? tenDanhMuc
                                : maDichVu != null ? "Dịch vụ"
                                : maThuCung != null ? "Thú cưng"
                                : "Khác";
                        String tenMatHang = nhomHang + " / " + tenHang;
                        double phanTramGiam = rs.getDouble("phanTramGiam");
                        if (rs.wasNull()) {
                            phanTramGiam = 0;
                        }
                        int soLuong = rs.getInt("soLuong");
                        double doanhThu = soLuong * rs.getDouble("donGia")
                                * (1 - phanTramGiam / 100.0);

                        MatHangKey khoa = new MatHangKey(
                                tenDanhMuc,
                                maDichVu,
                                maThuCung,
                                tenSanPham,
                                tenDichVu,
                                tenThuCung);
                        DongMatHang dongCu = matHangTheoMa.get(khoa);
                        int tongSoLuong = soLuong + (dongCu == null ? 0 : dongCu.getSoLuong());
                        double tongTien = doanhThu + (dongCu == null ? 0 : dongCu.getDoanhThu());
                        matHangTheoMa.put(
                                khoa,
                                new DongMatHang(tenMatHang, tongSoLuong, tongTien));
                    }
                }
            }
            matHangs.addAll(matHangTheoMa.values());
            matHangs.sort(Comparator.comparingDouble(DongMatHang::getDoanhThu).reversed()
                    .thenComparing(DongMatHang::getTenMatHang));
            for (DongMatHang dong : matHangs) {
                tongDoanhThu += dong.getDoanhThu();
            }
        }

        double trungBinh = tongHoaDon == 0 ? 0
                : Math.round(tongDoanhThu / tongHoaDon);
        return new BaoCao(tongDoanhThu, tongHoaDon, trungBinh, bieuDo, matHangs);
    }
}
