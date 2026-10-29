package dao;

import UI.PhanQuyen;

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

// Lớp truy cập cơ sở dữ liệu cho thống kê doanh thu.
public class ThongKeDoanhThuDAO {

    // Tập giá trị cố định cho chế độ thống kê.
    public enum CheDo {
        NGAY("Ngày"),
        THANG("Tháng"),
        NAM("Năm");

        private final String nhan;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số nhan: nhãn.
        CheDo(String nhan) {
            this.nhan = nhan;
        }

        // Trả về chuỗi đại diện của đối tượng để hiển thị.
        @Override
        public String toString() {
            return nhan;
        }
    }

    // Nhóm dữ liệu hoặc chức năng dòng biểu đồ dùng trong ThongKeDoanhThuDAO.
    public static final class DongBieuDo {
        // thuTu: Lưu thứ tự.
        private final int thuTu;
        // doanhThu: Giá trị doanh thu dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        private final BigDecimal doanhThu;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số thuTu: thứ tự.
        // Tham số doanhThu: doanh thu.
        private DongBieuDo(int thuTu, BigDecimal doanhThu) {
            this.thuTu = thuTu;
            this.doanhThu = doanhThu == null ? BigDecimal.ZERO : doanhThu;
        }

        // Trả về thứ tự của đối tượng.
        public int getThuTu() {
            return thuTu;
        }

        // Trả về doanh thu của đối tượng.
        public BigDecimal getDoanhThu() {
            return doanhThu;
        }
    }

    // Nhóm dữ liệu hoặc chức năng dòng mặt hàng dùng trong ThongKeDoanhThuDAO.
    public static final class DongMatHang {
        // tenMatHang: Lưu tên mặt hàng.
        private final String tenMatHang;
        // soLuong: Lưu số lượng.
        private final int soLuong;
        // doanhThu: Giá trị doanh thu dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        private final BigDecimal doanhThu;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số tenMatHang: tên mặt hàng.
        // Tham số soLuong: số lượng.
        // Tham số doanhThu: doanh thu.
        private DongMatHang(String tenMatHang, int soLuong, BigDecimal doanhThu) {
            this.tenMatHang = tenMatHang;
            this.soLuong = soLuong;
            this.doanhThu = doanhThu == null ? BigDecimal.ZERO : doanhThu;
        }

        // Trả về tên mặt hàng của đối tượng.
        public String getTenMatHang() {
            return tenMatHang;
        }

        // Trả về số lượng của đối tượng.
        public int getSoLuong() {
            return soLuong;
        }

        // Trả về doanh thu của đối tượng.
        public BigDecimal getDoanhThu() {
            return doanhThu;
        }
    }

    // Nhóm dữ liệu hoặc chức năng báo cáo dùng trong ThongKeDoanhThuDAO.
    public static final class BaoCao {
        // tongDoanhThu: Giá trị tổng doanh thu dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        private final BigDecimal tongDoanhThu;
        // tongHoaDon: Lưu tổng số hóa đơn.
        private final int tongHoaDon;
        // doanhThuTrungBinh: Giá trị doanh thu trung bình dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        private final BigDecimal doanhThuTrungBinh;
        // bieuDo: Lưu biểu đồ; phần tử có kiểu DongBieuDo.
        private final List<DongBieuDo> bieuDo;
        // matHangs: Lưu các mặt hàng; phần tử có kiểu DongMatHang.
        private final List<DongMatHang> matHangs;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số tongDoanhThu: tổng doanh thu.
        // Tham số tongHoaDon: tổng số hóa đơn.
        // Tham số doanhThuTrungBinh: doanh thu trung bình.
        // Tham số bieuDo: biểu đồ.
        // Tham số matHangs: các mặt hàng.
        private BaoCao(
                BigDecimal tongDoanhThu,
                int tongHoaDon,
                BigDecimal doanhThuTrungBinh,
                List<DongBieuDo> bieuDo,
                List<DongMatHang> matHangs) {
            this.tongDoanhThu = tongDoanhThu == null ? BigDecimal.ZERO : tongDoanhThu;
            this.tongHoaDon = tongHoaDon;
            this.doanhThuTrungBinh = doanhThuTrungBinh == null
                    ? BigDecimal.ZERO : doanhThuTrungBinh;
            this.bieuDo = Collections.unmodifiableList(new ArrayList<>(bieuDo));
            this.matHangs = Collections.unmodifiableList(new ArrayList<>(matHangs));
        }

        // Trả về tổng doanh thu của đối tượng.
        public BigDecimal getTongDoanhThu() {
            return tongDoanhThu;
        }

        // Trả về tổng số hóa đơn của đối tượng.
        public int getTongHoaDon() {
            return tongHoaDon;
        }

        // Trả về doanh thu trung bình của đối tượng.
        public BigDecimal getDoanhThuTrungBinh() {
            return doanhThuTrungBinh;
        }

        // Trả về biểu đồ của đối tượng.
        public List<DongBieuDo> getBieuDo() {
            return bieuDo;
        }

        // Trả về các mặt hàng của đối tượng.
        public List<DongMatHang> getMatHangs() {
            return matHangs;
        }
    }

    // Lấy báo cáo.
    // Tham số cheDo: chế độ thống kê.
    // Tham số ngayThamChieu: ngày tham chiếu.
    // Kết quả: đối tượng/giá trị kiểu BaoCao.
    public BaoCao layBaoCao(CheDo cheDo, LocalDate ngayThamChieu) throws SQLException {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.THONG_KE_DOANH_THU);
        if (cheDo == null || ngayThamChieu == null) {
            throw new IllegalArgumentException("Thiếu kỳ thống kê doanh thu.");
        }

        // batDau: Lưu bắt đầu dưới dạng dữ liệu thời gian.
        LocalDateTime batDau;
        // ketThuc: Lưu kết thực dưới dạng dữ liệu thời gian.
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

        // tongDoanhThu: Giá trị tổng doanh thu dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        BigDecimal tongDoanhThu = BigDecimal.ZERO;
        // tongHoaDon: Lưu tổng số hóa đơn.
        int tongHoaDon;
        // bieuDo: Lưu biểu đồ; phần tử có kiểu DongBieuDo.
        List<DongBieuDo> bieuDo = new ArrayList<>();
        for (int i = 1; i <= soMoc; i++) {
            bieuDo.add(new DongBieuDo(
                    cheDo == CheDo.NGAY ? i - 1 : i,
                    BigDecimal.ZERO));
        }
        // matHangs: Lưu các mặt hàng; phần tử có kiểu DongMatHang.
        List<DongMatHang> matHangs = new ArrayList<>();

        // tongHoaDonSql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String tongHoaDonSql = "SELECT COUNT(*) FROM HoaDon "
                + "WHERE ngayLap >= ? AND ngayLap < ?";
        // bieuDoSql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String bieuDoSql = "SELECT " + cotNhom + " AS moc, "
                + "COALESCE(SUM(ct.soLuong * ct.donGia "
                + "* (1 - COALESCE(ct.phanTramGiam, 0) / 100.0)), 0) AS doanhThu "
                + "FROM HoaDon hd JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon "
                + "WHERE hd.ngayLap >= ? AND hd.ngayLap < ? "
                + "GROUP BY " + cotNhom + " ORDER BY moc";
        // matHangSql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
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

        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(tongHoaDonSql)) {
                ps.setTimestamp(1, java.sql.Timestamp.valueOf(batDau));
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(ketThuc));
                // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
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
                // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
                try (ResultSet rs = ps.executeQuery()) {
                    // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
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
                // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
                try (ResultSet rs = ps.executeQuery()) {
                    // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
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

        // trungBinh: Giá trị trung bình dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        BigDecimal trungBinh = tongHoaDon == 0 ? BigDecimal.ZERO
                : tongDoanhThu.divide(BigDecimal.valueOf(tongHoaDon), 0,
                        java.math.RoundingMode.HALF_UP);
        return new BaoCao(tongDoanhThu, tongHoaDon, trungBinh, bieuDo, matHangs);
    }
}
