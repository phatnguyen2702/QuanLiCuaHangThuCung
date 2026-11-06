package control;

import dao.LichHenDAO;
import entity.LichHen;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LichHenControl {
    private final LichHenDAO dao = new LichHenDAO();
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    public ArrayList<LichHen> layDanhSach() {
        return dao.getAll();
    }

    public ArrayList<String[]> layDanhSachKhachHang() {
        return dao.getDanhSachKhachHang();
    }

    public ArrayList<String[]> layDanhSachNhanVienSpa() {
        return dao.getDanhSachNhanVienSpa();
    }

    public ArrayList<String[]> layDanhSachThuCung(String maKhachHang) {
        return dao.getDanhSachThuCungTheoKhachHang(maKhachHang);
    }

    public String taoMaTuDong() {
        return dao.taoMaTuDong();
    }

    public LichHen timTheoMa(String maLichHen) {
        return dao.timTheoMa(maLichHen);
    }

    public boolean coTheSua(LichHen lichHen) {
        return lichHen != null && !"Đã hủy".equals(lichHen.getTrangThai());
    }

    public ArrayList<LichHen> locDanhSach(List<LichHen> danhSach, String ngay,
            String nhanVien, String trangThai, String tuKhoa) {
        LocalDate ngayLoc = null;
        if (ngay != null && !ngay.isBlank()) {
            try {
                ngayLoc = LocalDate.parse(ngay.trim(), NGAY);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Ngày lọc phải có định dạng dd/MM/yyyy.");
            }
        }

        String tuKhoaLoc = safe(tuKhoa).toLowerCase(Locale.ROOT);
        ArrayList<LichHen> ketQua = new ArrayList<>();
        for (LichHen lichHen : danhSach) {
            if (ngayLoc != null && (lichHen.getNgayGioHen() == null
                    || !lichHen.getNgayGioHen().toLocalDate().equals(ngayLoc)))
                continue;
            if (nhanVien != null && !"Tất cả".equals(nhanVien)
                    && !nhanVien.equals(lichHen.getTenNhanVien()))
                continue;
            if (trangThai != null && !"Tất cả".equals(trangThai)
                    && !trangThai.equals(lichHen.getTrangThai()))
                continue;

            String thongTin = safe(lichHen.getMaLichHen()) + " "
                    + safe(lichHen.getTenKhachHang()) + " "
                    + safe(lichHen.getTenThuCung());
            if (!thongTin.toLowerCase(Locale.ROOT).contains(tuKhoaLoc))
                continue;
            ketQua.add(lichHen);
        }
        return ketQua;
    }

    public LichHen taoLichHen(String ma, String ngay, String gio,
            String khachHang, String thuCung, String nhanVien, String trangThai) {
        if (safe(khachHang).isBlank())
            throw new IllegalArgumentException("Vui lòng chọn khách hàng.");
        if (safe(thuCung).isBlank())
            throw new IllegalArgumentException("Vui lòng chọn thú cưng.");
        if (safe(nhanVien).isBlank())
            throw new IllegalArgumentException("Vui lòng chọn nhân viên Spa.");

        LocalDateTime ngayGioHen;
        try {
            ngayGioHen = LocalDateTime.of(
                    LocalDate.parse(safe(ngay), NGAY),
                    LocalTime.parse(safe(gio), GIO));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ngày phải theo dd/MM/yyyy và giờ theo HH:mm.");
        }
        if (!ngayGioHen.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Giờ hẹn phải lớn hơn thời gian hiện tại.");

        LichHen lichHen = new LichHen(safe(ma), ngayGioHen, "",
                safe(khachHang), safe(nhanVien), safe(thuCung));
        lichHen.setTrangThai(safe(trangThai).isEmpty() ? "Xác nhận" : trangThai);
        return lichHen;
    }

    public boolean them(LichHen lichHen) {
        kiemTra(lichHen);
        return dao.them(lichHen);
    }

    public boolean sua(LichHen lichHen) {
        kiemTra(lichHen);
        return dao.sua(lichHen);
    }

    public boolean huy(String maLichHen) {
        if (safe(maLichHen).isBlank())
            throw new IllegalArgumentException("Mã lịch hẹn không hợp lệ.");
        return dao.huy(maLichHen);
    }

    public String layMa(String mucChon) {
        if (mucChon == null)
            return "";
        int viTri = mucChon.indexOf(" - ");
        return viTri < 0 ? mucChon.trim() : mucChon.substring(0, viTri).trim();
    }

    private void kiemTra(LichHen lichHen) {
        if (lichHen == null)
            throw new IllegalArgumentException("Thông tin lịch hẹn không hợp lệ.");
        if (safe(lichHen.getMaLichHen()).isBlank())
            throw new IllegalArgumentException("Mã lịch hẹn không hợp lệ.");
        if (lichHen.getNgayGioHen() == null
                || !lichHen.getNgayGioHen().isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Giờ hẹn phải lớn hơn thời gian hiện tại.");
        if (!thuCungCuaKhach(lichHen.getMaThuCung(), lichHen.getMaKhachHang()))
            throw new IllegalArgumentException("Thú cưng không thuộc khách hàng đã chọn.");
        if (biTrungLich(lichHen))
            throw new IllegalArgumentException("Nhân viên Spa đã có lịch trong vòng 60 phút.");
    }

    private boolean thuCungCuaKhach(String maThuCung, String maKhachHang) {
        for (String[] thuCung : layDanhSachThuCung(maKhachHang)) {
            if (maThuCung.equals(thuCung[0]))
                return true;
        }
        return false;
    }

    private boolean biTrungLich(LichHen lichMoi) {
        for (LichHen lichCu : layDanhSach()) {
            if (lichMoi.getMaLichHen().equals(lichCu.getMaLichHen())
                    || "Đã hủy".equals(lichCu.getTrangThai())
                    || !lichMoi.getMaNhanVien().equals(lichCu.getMaNhanVien())
                    || lichCu.getNgayGioHen() == null)
                continue;
            long phut = Math.abs(Duration.between(
                    lichMoi.getNgayGioHen(), lichCu.getNgayGioHen()).toMinutes());
            if (phut < 60)
                return true;
        }
        return false;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
