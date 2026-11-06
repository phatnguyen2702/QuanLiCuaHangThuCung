package control;

import dao.ThuCungDAO;
import dao.ThuCungDAO.ChuNuoi;
import dao.ThuCungDAO.HoSoThuCung;
import entity.ThuCung;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ThuCungControl {
    private final ThuCungDAO dao = new ThuCungDAO();

    public ArrayList<HoSoThuCung> layDanhSach() {
        return dao.getAllChiTiet();
    }

    public ArrayList<ChuNuoi> layChuNuoi() {
        return dao.getDanhSachChuNuoi();
    }

    public String goiYMa() {
        return dao.taoMaThuCungTuDong();
    }

    public boolean them(ThuCung thuCung) {
        kiemTra(thuCung);
        return dao.them(thuCung);
    }

    public boolean sua(ThuCung thuCung) {
        kiemTra(thuCung);
        return dao.sua(thuCung);
    }

    public boolean xoa(String ma) {
        String maChuanHoa = chuanHoa(ma);
        if (maChuanHoa.isEmpty())
            throw new IllegalArgumentException("Mã thú cưng không được để trống.");
        return dao.xoa(maChuanHoa);
    }

    public boolean khongTheDoiChuNuoi(String maThuCung, String maChuCu, String maChuMoi) {
        return !chuanHoa(maChuCu).equals(chuanHoa(maChuMoi))
                && dao.daCoLichSu(chuanHoa(maThuCung));
    }

    public boolean doiChuNuoi(String maChuCu, String maChuMoi) {
        return !chuanHoa(maChuCu).equals(chuanHoa(maChuMoi));
    }

    public ThuCung taoThuCung(String ma, String ten, String loai, String giong,
            String gioiTinh, String canNang, String maKhachHang, String trangThai) {
        String tenChuanHoa = chuanHoa(ten);
        String loaiChuanHoa = chuanHoa(loai);
        String giongChuanHoa = chuanHoa(giong);
        if (tenChuanHoa.isEmpty()) {
            tenChuanHoa = loaiChuanHoa + " - "
                    + (giongChuanHoa.isEmpty() ? "Chưa rõ giống" : giongChuanHoa);
        }

        double kg;
        try {
            kg = Double.parseDouble(chuanHoa(canNang).replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Vui lòng nhập cân nặng là số, ví dụ 3.5 (kg).");
        }

        ThuCung thuCung = new ThuCung(chuanHoa(ma), tenChuanHoa, loaiChuanHoa,
                giongChuanHoa, chuanHoa(gioiTinh), kg,
                chuanHoa(maKhachHang), chuanHoa(trangThai));
        kiemTra(thuCung);
        return thuCung;
    }

    public void kiemTra(ThuCung thuCung) {
        if (thuCung == null)
            throw new IllegalArgumentException("Thông tin thú cưng không hợp lệ.");
        if (thuCung.getMaThuCung() == null || thuCung.getMaThuCung().isBlank()
                || thuCung.getMaThuCung().trim().length() > 10)
            throw new IllegalArgumentException("Mã thú cưng không được để trống và tối đa 10 ký tự.");
        if (thuCung.getTenThuCung() == null || thuCung.getTenThuCung().isBlank()
                || thuCung.getTenThuCung().trim().length() > 50)
            throw new IllegalArgumentException("Tên thú cưng không được để trống và tối đa 50 ký tự.");
        if (thuCung.getLoai() == null
                || !List.of("Chó", "Mèo", "Chim", "Khác").contains(thuCung.getLoai()))
            throw new IllegalArgumentException("Loại thú cưng phải là Chó, Mèo, Chim hoặc Khác.");
        if (thuCung.getGiong() != null && thuCung.getGiong().trim().length() > 25)
            throw new IllegalArgumentException("Giống thú cưng không được quá 25 ký tự.");
        if (thuCung.getGioiTinh() == null
                || !List.of("Đực", "Cái").contains(thuCung.getGioiTinh()))
            throw new IllegalArgumentException("Giới tính chỉ được chọn Đực hoặc Cái.");
        if (!Double.isFinite(thuCung.getCanNang())
                || thuCung.getCanNang() <= 0 || thuCung.getCanNang() > 999.99)
            throw new IllegalArgumentException("Cân nặng phải lớn hơn 0 và không quá 999,99 kg.");
        if (thuCung.getMaKhachHang() == null || thuCung.getMaKhachHang().isBlank()
                || thuCung.getMaKhachHang().trim().length() > 10)
            throw new IllegalArgumentException("Vui lòng chọn chủ nuôi hợp lệ.");
    }

    public ArrayList<HoSoThuCung> loc(List<HoSoThuCung> danhSach, String tuKhoa,
            String gioiTinh, String loai) {
        String tuKhoaChuanHoa = khongDau(tuKhoa);
        ArrayList<HoSoThuCung> ketQua = new ArrayList<>();
        for (HoSoThuCung hoSo : danhSach) {
            ThuCung thuCung = hoSo.thuCung;
            String thongTin = String.join(" ", chuanHoa(thuCung.getMaThuCung()),
                    chuanHoa(thuCung.getTenThuCung()), chuanHoa(thuCung.getLoai()),
                    chuanHoa(thuCung.getGiong()), chuanHoa(hoSo.tenKhachHang),
                    chuanHoa(hoSo.soDienThoai));
            if (!khongDau(thongTin).contains(tuKhoaChuanHoa))
                continue;
            if (gioiTinh != null && !"Giới tính".equals(gioiTinh)
                    && !gioiTinh.equals(thuCung.getGioiTinh()))
                continue;
            if (loai != null && !"Loại".equals(loai) && !loai.equals(thuCung.getLoai()))
                continue;
            ketQua.add(hoSo);
        }
        return ketQua;
    }

    public ArrayList<ChuNuoi> timChuNuoi(String tuKhoa) {
        String q = khongDau(tuKhoa);
        ArrayList<ChuNuoi> ketQua = new ArrayList<>();
        for (ChuNuoi chuNuoi : layChuNuoi()) {
            String thongTin = String.join(" ", chuanHoa(chuNuoi.maKhachHang),
                    chuanHoa(chuNuoi.hoTen), chuanHoa(chuNuoi.soDienThoai));
            if (q.isEmpty() || khongDau(thongTin).contains(q))
                ketQua.add(chuNuoi);
        }
        return ketQua;
    }

    public ArrayList<ChuNuoi> timChuNuoiTheoTen(String ten) {
        String q = khongDau(ten);
        ArrayList<ChuNuoi> ketQua = new ArrayList<>();
        for (ChuNuoi chuNuoi : layChuNuoi()) {
            if (khongDau(chuNuoi.hoTen).equals(q))
                ketQua.add(chuNuoi);
        }
        return ketQua;
    }

    private static String chuanHoa(String value) {
        return value == null ? "" : value.trim();
    }

    private static String khongDau(String value) {
        return Normalizer.normalize(chuanHoa(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }
}
