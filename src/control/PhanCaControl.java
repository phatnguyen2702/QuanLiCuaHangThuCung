package control;

import dao.LichPhanCaDAO;
import entity.LichPhanCa;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import util.PhanCaExporter;

public final class PhanCaControl {
    private final LichPhanCaDAO dao = new LichPhanCaDAO();
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");

    private String dinhDangGio(LocalDateTime gio) {
        return gio == null ? "" : gio.format(GIO);
    }

    public ArrayList<LichPhanCa> layDanhSach() {
        return dao.getAll();
    }

    public ArrayList<String[]> layNhanVien() {
        return dao.getDanhSachNhanVien();
    }

    public ArrayList<String[]> layCaLam() {
        return dao.getDanhSachCa();
    }

    public String taoMaPhanCa() {
        return dao.taoMaPhanCa();
    }

    public ArrayList<LichPhanCa> layPhanCaTrongTuan(LocalDate ngay) throws SQLException {
        return dao.layPhanCaTrongTuan(ngay);
    }

    public static String maDaChon(String text) {
        if (text == null)
            return "";
        int pos = text.indexOf(" - ");
        return pos >= 0 ? text.substring(0, pos) : text;
    }

    public static LocalDate docNgay(String text) {
        if (text == null || text.trim().isEmpty())
            return null;
        try {
            return LocalDate.parse(text.trim(), NGAY);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    public LichPhanCa taoPhanCa(String maDangSua, boolean sua, String maNV, String maCa, String ngayText) {
        if (maNV == null || !maNV.matches("NV\\d{3}") || maCa == null || !maCa.matches("CA\\d{3}"))
            throw new IllegalArgumentException("Hãy chọn nhân viên và ca làm hợp lệ từ danh sách.");
        if (sua && (maDangSua == null || maDangSua.isBlank()))
            throw new IllegalArgumentException("Vui lòng chọn phân ca cần sửa.");
        LocalDate ngay = docNgay(ngayText);
        if (ngay == null)
            throw new IllegalArgumentException("Vui lòng chọn ngày làm việc từ lịch.");
        if (ngay.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Không được thêm hoặc sửa phân ca cho ngày đã qua.");
        return new LichPhanCa(sua ? maDangSua : dao.taoMaPhanCa(), ngay, maNV, maCa);
    }

    public boolean them(LichPhanCa pc) throws SQLException {
        kiemTraPhanCa(pc);
        if (dao.trungGio(pc.getMaNhanVien(), pc.getMaCa(), pc.getNgayLamViec(), null))
            throw new IllegalArgumentException("Nhân viên đã có ca trùng hoặc chồng giờ trong ngày này.");
        return dao.them(pc);
    }

    public boolean sua(LichPhanCa pc) throws SQLException {
        kiemTraPhanCa(pc);
        if (dao.trungGio(pc.getMaNhanVien(), pc.getMaCa(), pc.getNgayLamViec(), pc.getMaPhanCa()))
            throw new IllegalArgumentException("Nhân viên đã có ca trùng hoặc chồng giờ với phân ca khác.");
        return dao.sua(pc);
    }

    private void kiemTraPhanCa(LichPhanCa pc) {
        if (pc == null || pc.getNgayLamViec() == null || pc.getNgayLamViec().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Không được phân ca cho ngày đã qua.");
    }

    public boolean xoa(String maPhanCa) {
        return dao.xoa(maPhanCa);
    }

    public List<LichPhanCa> loc(LocalDate tu, LocalDate den, String maNV, String maCa) {
        List<LichPhanCa> ketQua = new ArrayList<>();
        for (LichPhanCa pc : dao.getAll()) {
            LocalDate ngay = pc.getNgayLamViec();
            if (tu != null && (ngay == null || ngay.isBefore(tu)))
                continue;
            if (den != null && (ngay == null || ngay.isAfter(den)))
                continue;
            if (maNV != null && !maNV.isBlank() && !maNV.equals("Tất cả") && !maNV.equalsIgnoreCase(pc.getMaNhanVien()))
                continue;
            if (maCa != null && !maCa.isBlank() && !maCa.equals("Tất cả") && !maCa.equalsIgnoreCase(pc.getMaCa()))
                continue;
            ketQua.add(pc);
        }
        return ketQua;
    }

    public record ThongKe(int soNhanVien, int soCa) {
    }

    public ThongKe thongKeNgay(LocalDate ngay) throws SQLException {
        List<LichPhanCa> lich = dao.layPhanCaTheoNgay(ngay);
        Set<String> nhanVien = new HashSet<>();
        for (LichPhanCa pc : lich)
            nhanVien.add(pc.getMaNhanVien());
        return new ThongKe(nhanVien.size(), lich.size());
    }

    public static LocalDate dauTuan(LocalDate ngay) {
        return ngay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static String khoangTuan(LocalDate thuHai) {
        return thuHai.format(NGAY) + " – " + thuHai.plusDays(6).format(NGAY);
    }

    public List<String[]> duLieuExcel(LocalDate tu, LocalDate den, String maNV, String maCa) {
        List<LichPhanCa> danhSach = loc(tu, den, maNV, maCa);
        danhSach.sort(Comparator.comparing(LichPhanCa::getNgayLamViec)
                .thenComparing(LichPhanCa::getMaNhanVien));
        List<String[]> rows = new ArrayList<>();
        for (LichPhanCa pc : danhSach) {
            rows.add(new String[] { pc.getMaPhanCa(), pc.getNgayLamViec().format(NGAY),
                    pc.getMaNhanVien(), pc.getHoTen(), pc.getMaCa(), pc.getThongTinCa() });
        }
        return rows;
    }

    public void xuatExcel(Path path, List<String[]> rows) throws IOException {
        String[] tenCot = { "Mã phân ca", "Ngày làm việc", "Mã nhân viên", "Họ tên", "Mã ca", "Ca làm" };
        PhanCaExporter.xuat(path, tenCot, rows);
    }

    public record KetQuaSaoChep(int tongNguon, int daThem, int boQuaTrungGio, int boQuaQuaKhu) {
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

    public KetQuaSaoChep saoChepTuan(LocalDate nguon, LocalDate dich) throws Exception {
        if (nguon.equals(dich))
            throw new IllegalArgumentException("Tuần nguồn và tuần đích phải khác nhau.");
        if (dich.plusDays(6).isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Tuần đích đã qua, vui lòng chọn tuần khác.");
        LichPhanCaDAO.KetQuaSaoChep kq = dao.saoChepLichTheoTuan(nguon, dich);
        return new KetQuaSaoChep(kq.getTongNguon(), kq.getDaThem(),
                kq.getBoQuaTrungGio(), kq.getBoQuaQuaKhu());
    }

    public List<Object[]> bangLichTuan(List<LichPhanCa> phanCaTuan) {
        java.util.Map<String, StringBuilder[]> bangTheoNguoi = new java.util.LinkedHashMap<>();
        for (LichPhanCa pc : phanCaTuan) {
            String nhanVien = pc.getMaNhanVien() + " - " + pc.getHoTen();
            if (!bangTheoNguoi.containsKey(nhanVien)) {
                StringBuilder[] ngay = new StringBuilder[7];
                for (int i = 0; i < 7; i++) {
                    ngay[i] = new StringBuilder();
                }
                bangTheoNguoi.put(nhanVien, ngay);
            }
            int thuIndex = pc.getNgayLamViec().getDayOfWeek().getValue() - 1;
            StringBuilder o = bangTheoNguoi.get(nhanVien)[thuIndex];
            if (o.length() > 0) {
                o.append("\n");
            }
            o.append(pc.getMaCa())
                    .append(" - ")
                    .append(pc.getTenCa())
                    .append(" (")
                    .append(dinhDangGio(pc.getGioBatDau()))
                    .append("–")
                    .append(dinhDangGio(pc.getGioKetThuc()))
                    .append(")");
        }
        List<Object[]> rows = new ArrayList<>();
        for (java.util.Map.Entry<String, StringBuilder[]> hang : bangTheoNguoi.entrySet()) {
            Object[] duLieu = new Object[8];
            duLieu[0] = hang.getKey();
            for (int i = 0; i < 7; i++) {
                duLieu[i + 1] = hang.getValue()[i].toString();
            }
            rows.add(duLieu);
        }
        return rows;
    }

}
