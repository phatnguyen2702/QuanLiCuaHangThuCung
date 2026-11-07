package control;

import dao.VoucherDAO;
import entity.Voucher;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * Control voucher: kiểm tra dữ liệu rỗng / sai định dạng / sai logic,
 * gọi DAO, rồi trả về KetQua cho UI.
 *
 * Dữ liệu từ form UI truyền xuống toàn là chuỗi thô (đúng như người dùng gõ).
 */
public class VoucherControl {

    // Lựa chọn "Trạng thái" trong form (thứ tự giống ComboBox bên UI)
    public static final int TT_KICH_HOAT = 0;
    public static final int TT_SAP_CHAY = 1;
    public static final int TT_DA_HUY = 2;

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VoucherDAO dao = new VoucherDAO();

    /**
     * Kết quả Control trả về cho UI sau khi thêm / sửa / xóa / đổi trạng thái.
     * UI chỉ cần isThanhCong() và getThongBao() để hiện JOptionPane.
     */
    public static class KetQua {
        private final boolean thanhCong;
        private final String thongBao;
        private final String maVoucher; // mã voucher vừa thao tác (có thể null)

        private KetQua(boolean thanhCong, String thongBao, String maVoucher) {
            this.thanhCong = thanhCong;
            this.thongBao = thongBao;
            this.maVoucher = maVoucher;
        }

        public static KetQua ok(String thongBao, String maVoucher) {
            return new KetQua(true, thongBao, maVoucher);
        }

        public static KetQua loi(String thongBao) {
            return new KetQua(false, thongBao, null);
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public String getThongBao() {
            return thongBao;
        }

        public String getMaVoucher() {
            return maVoucher;
        }
    }

    // ---------------------------------------------------------------------
    // Đọc dữ liệu
    // ---------------------------------------------------------------------

    /**
     * Lấy voucher theo từ khóa (mã / tên) và lọc theo trạng thái.
     * locTrangThai: "Tat ca", "Dang chay", "Sap chay", "Het han", "Het luot" hoặc "Da huy".
     */
    public ArrayList<Voucher> timKiem(String tuKhoa, String locTrangThai) {
        ArrayList<Voucher> ketQua = new ArrayList<>();

        for (Voucher v : dao.getAll(tuKhoa)) {
            if (locTrangThai.equals("Tat ca") || locTrangThai.equals(v.getTrangThaiText())) {
                ketQua.add(v);
            }
        }
        return ketQua;
    }

    // ---------------------------------------------------------------------
    // Thêm / sửa / xóa
    // ---------------------------------------------------------------------

    public KetQua them(String ten, String phanTram, String dieuKien,
            String ngayBd, String ngayKt, String soLuong, int luaChonTrangThai) {
        try {
            Voucher v = docForm(null, 0, ten, phanTram, dieuKien, ngayBd, ngayKt, soLuong, luaChonTrangThai);

            String loiDao = dao.them(v); // thành công: v có mã mới
            if (loiDao != null) {
                return KetQua.loi(loiDao);
            }
            return KetQua.ok("Đã tạo voucher " + v.getMaVoucher() + ".", v.getMaVoucher());

        } catch (IllegalArgumentException e) {
            return KetQua.loi(e.getMessage()); // dữ liệu nhập chưa hợp lệ
        }
    }

    /** goc = voucher đang sửa (lấy mã và số lượt đã dùng từ đây). */
    public KetQua sua(Voucher goc, String ten, String phanTram, String dieuKien,
            String ngayBd, String ngayKt, String soLuong, int luaChonTrangThai) {
        try {
            Voucher v = docForm(goc.getMaVoucher(), goc.getDaDung(),
                    ten, phanTram, dieuKien, ngayBd, ngayKt, soLuong, luaChonTrangThai);

            String loiDao = dao.sua(v);
            if (loiDao != null) {
                return KetQua.loi(loiDao);
            }
            return KetQua.ok("Đã cập nhật voucher " + goc.getMaVoucher() + ".", goc.getMaVoucher());

        } catch (IllegalArgumentException e) {
            return KetQua.loi(e.getMessage());
        }
    }

    public KetQua xoa(String maVoucher) {
        String loiDao = dao.xoa(maVoucher);
        if (loiDao != null) {
            return KetQua.loi(loiDao);
        }
        return KetQua.ok("Đã xóa voucher " + maVoucher + ".", maVoucher);
    }

    // ---------------------------------------------------------------------
    // Kích hoạt / hủy / dừng
    // ---------------------------------------------------------------------

    public KetQua kichHoat(Voucher v) {
        String tt = v.getTrangThaiText();
        if (tt.equals("Dang chay") || tt.equals("Sap chay")) {
            return KetQua.loi("Voucher này đang được kích hoạt rồi.");
        }

        LocalDate homNay = LocalDate.now();

        if (v.getNgayKetThuc() != null && v.getNgayKetThuc().isBefore(homNay)) {
            return KetQua.loi("Voucher đã quá ngày kết thúc (" + v.getNgayKetThuc().format(D)
                    + ") nên chưa thể chạy.\nHãy bấm Sửa để gia hạn.");
        }
        if (v.getSoLuongToiDa() != null && v.getDaDung() >= v.getSoLuongToiDa()) {
            return KetQua.loi("Voucher đã dùng hết lượt (" + v.getDaDung() + "/" + v.getSoLuongToiDa()
                    + ") nên chưa thể chạy.\nHãy bấm Sửa để tăng lượt.");
        }

        String loiDao = dao.doiTrangThai(v.getMaVoucher(), true);
        if (loiDao != null) {
            return KetQua.loi(loiDao);
        }
        return KetQua.ok("Đã kích hoạt voucher " + v.getMaVoucher() + ".", v.getMaVoucher());
    }

    public KetQua huy(Voucher v) {
        if (!v.isTrangThai()) {
            return KetQua.loi("Voucher này đã hủy rồi.");
        }

        String loiDao = dao.doiTrangThai(v.getMaVoucher(), false);
        if (loiDao != null) {
            return KetQua.loi(loiDao);
        }
        return KetQua.ok("Đã hủy voucher " + v.getMaVoucher() + ".", v.getMaVoucher());
    }

    /** Dừng voucher đang chạy: đặt ngày kết thúc = hôm qua (voucher chuyển sang "Hết hạn"). */
    public KetQua dung(Voucher v) {
        String tt = v.getTrangThaiText();

        if (tt.equals("Sap chay")) {
            return KetQua.loi("Voucher chưa chạy nên không cần dừng. Hãy dùng \"Hủy voucher\".");
        }
        if (!tt.equals("Dang chay")) {
            return KetQua.loi("Chỉ có thể dừng voucher đang chạy.");
        }

        LocalDate ketThuc = LocalDate.now().minusDays(1);

        if (v.getNgayBatDau() != null && ketThuc.isBefore(v.getNgayBatDau())) {
            return KetQua.loi("Voucher mới bắt đầu từ hôm nay nên không thể dừng theo ngày.\n"
                    + "Hãy dùng \"Hủy voucher\".");
        }

        Voucher moi = new Voucher(v.getMaVoucher(), v.getTenVoucher(), v.getPhanTramGiam(),
                v.getDieuKien(), v.isTrangThai(), v.getNgayBatDau(), ketThuc, v.getSoLuongToiDa());

        String loiDao = dao.sua(moi);
        if (loiDao != null) {
            return KetQua.loi(loiDao);
        }
        return KetQua.ok("Đã dừng voucher " + v.getMaVoucher() + ".", v.getMaVoucher());
    }

    // ---------------------------------------------------------------------
    // Hàm phụ: đọc + kiểm tra dữ liệu form (dùng chung cho them và sua)
    // ---------------------------------------------------------------------

    /**
     * Đổi các chuỗi nhập thành Voucher. Nếu dữ liệu sai thì ném IllegalArgumentException
     * kèm câu thông báo; them() / sua() sẽ bắt lại và trả về KetQua.loi(...).
     */
    private Voucher docForm(String ma, int daDung, String ten, String phanTram, String dieuKien,
            String ngayBd, String ngayKt, String soLuong, int luaChonTrangThai) {

        // Tên
        ten = ten.trim();
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Tên chương trình bắt buộc nhập.");
        }

        // Mức giảm
        String pt = phanTram.trim().replace(',', '.');
        if (!pt.matches("\\d+(\\.\\d+)?")) {
            throw new IllegalArgumentException("Mức giảm phải là số, ví dụ 10 hoặc 12.5");
        }
        double giam = Double.parseDouble(pt);
        if (giam <= 0 || giam > 100) {
            throw new IllegalArgumentException("Mức giảm phải lớn hơn 0 và không quá 100 (%).");
        }
        giam = Math.round(giam * 100) / 100.0; // làm tròn 2 chữ số

        // Điều kiện (có thể để trống)
        dieuKien = dieuKien.trim();

        // Ngày (có thể để trống = không giới hạn)
        LocalDate bd = docNgay(ngayBd);
        LocalDate kt = docNgay(ngayKt);
        if (bd != null && kt != null && kt.isBefore(bd)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
        }

        // Số lượt tối đa (có thể để trống = không giới hạn)
        Integer sl = null;
        if (!soLuong.trim().isEmpty()) {
            try {
                sl = Integer.parseInt(soLuong.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Số lượt tối đa phải là số nguyên.");
            }
            if (sl <= 0) {
                throw new IllegalArgumentException("Số lượt tối đa phải lớn hơn 0.");
            }
            if (sl < daDung) {
                throw new IllegalArgumentException("Voucher đã dùng " + daDung
                        + " lượt, số lượt tối đa không thể nhỏ hơn.");
            }
        }

        // Trạng thái phải khớp với ngày bắt đầu
        boolean batDauTuongLai = bd != null && bd.isAfter(LocalDate.now());

        if (luaChonTrangThai == TT_KICH_HOAT && batDauTuongLai) {
            throw new IllegalArgumentException("Từ ngày còn ở tương lai, hãy chọn trạng thái \"Sắp chạy\".");
        }
        if (luaChonTrangThai == TT_SAP_CHAY && !batDauTuongLai) {
            throw new IllegalArgumentException("Trạng thái \"Sắp chạy\" cần có Từ ngày sau hôm nay.");
        }

        Voucher v = new Voucher(ma, ten, giam, dieuKien.isEmpty() ? null : dieuKien,
                luaChonTrangThai != TT_DA_HUY, bd, kt, sl);
        v.setDaDung(daDung);
        return v;
    }

    /** Chuỗi dd/MM/yyyy -> LocalDate; chuỗi rỗng -> null. */
    private LocalDate docNgay(String s) {
        s = s.trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(s, D);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày phải có dạng dd/MM/yyyy, ví dụ 01/11/2026.");
        }
    }
}
