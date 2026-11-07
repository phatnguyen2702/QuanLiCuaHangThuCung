package control;

import dao.CaLamDAO;
import entity.CaLam;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Locale;


public final class CaLamControl {
    private final CaLamDAO dao = new CaLamDAO();
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime GIO_MO_CUA = LocalTime.of(6, 0);
    private static final LocalTime GIO_DONG_CUA = LocalTime.of(22, 0);

    public ArrayList<CaLam> layDanhSach() {
        return dao.getAll();
    }

    public String goiYMa() {
        return dao.goiYMa();
    }

    public boolean them(CaLam ca) throws SQLException {
        kiemTra(ca);
        return dao.them(ca);
    }

    public boolean sua(CaLam ca) {
        kiemTra(ca);
        return dao.sua(ca);
    }

    public boolean xoa(String maCa) {
        return dao.xoa(maCa);
    }

    public CaLam taoCaLam(String ma, String ten, String batDau, String ketThuc) {
        CaLam ca = new CaLam(safe(ma), safe(ten),
                docGio(safe(batDau), "Giờ bắt đầu"), docGio(safe(ketThuc), "Giờ kết thúc"));
        kiemTra(ca);
        return ca;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    public void kiemTra(CaLam ca) {
        String ten = safe(ca.getTenCa());
        if (ten.length() < 3 || ten.length() > 50 || !ten.matches("[\\p{L}0-9 ]+"))
            throw new IllegalArgumentException("Tên ca từ 3-50 ký tự, chỉ gồm chữ, số và khoảng trắng.");
        LocalTime start = ca.getGioBatDau();
        LocalTime end = ca.getGioKetThuc();
        if (start == null || end == null)
            throw new IllegalArgumentException("Vui lòng chọn giờ bắt đầu và giờ kết thúc.");
        if (start.isBefore(GIO_MO_CUA) || end.isAfter(GIO_DONG_CUA))
            throw new IllegalArgumentException("Ca làm chỉ được trong khung giờ 06:00 đến 22:00.");
        if (start.equals(end))
            throw new IllegalArgumentException("Giờ bắt đầu và kết thúc không được trùng nhau.");
        if (!end.isAfter(start))
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu; ca làm không được qua đêm.");
    }

    private static LocalTime docGio(String s, String nhan) {
        if (s == null || !s.matches("\\d{2}:\\d{2}"))
            throw new IllegalArgumentException(nhan + " phải có định dạng HH:mm, ví dụ 06:00.");
        try {
            return LocalTime.parse(s, GIO);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(nhan + " không hợp lệ. Giờ phải từ 06 đến 22, phút từ 00 đến 59.");
        }
    }

    public ArrayList<CaLam> timKiem(String tim) {
        String q = safe(tim).toLowerCase(Locale.ROOT);
        ArrayList<CaLam> ds = new ArrayList<>();

        for (CaLam ca : dao.getAll()) {
            String thongTin = lower(ca.getMaCa()) + " "
                    + lower(ca.getTenCa()) + " "
                    + dinhDangGio(ca.getGioBatDau()) + " "
                    + dinhDangGio(ca.getGioKetThuc());

            if (q.isEmpty() || thongTin.contains(q))
                ds.add(ca);
        }
        return ds;
    }

    private static String lower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }


    public static String dinhDangGio(LocalTime gio) {
        return gio == null ? "" : gio.format(GIO);
    }
}
