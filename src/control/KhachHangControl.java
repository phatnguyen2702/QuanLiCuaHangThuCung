package control;

import dao.KhachHangDAO;
import dao.KhachHangDAO.ThuCungMoi;
import entity.KhachHang;

import java.util.ArrayList;

/**
 * Control khách hàng: kiểm tra dữ liệu rỗng / sai định dạng, gọi DAO,
 * rồi trả về KetQua cho UI.
 *
 * Thú cưng từ UI truyền xuống là mảng chuỗi thô, thứ tự:
 * { tên, loại, giống, giới tính, cân nặng }
 */
public class KhachHangControl {

    private final KhachHangDAO dao = new KhachHangDAO();

    /**
     * Kết quả Control trả về cho UI sau khi thêm / sửa / xóa.
     * UI chỉ cần isThanhCong() và getThongBao() để hiện JOptionPane.
     */
    public static class KetQua {
        private final boolean thanhCong;
        private final String thongBao;
        private final String maKhachHang; // mã khách hàng vừa thêm / sửa (có thể null)

        private KetQua(boolean thanhCong, String thongBao, String maKhachHang) {
            this.thanhCong = thanhCong;
            this.thongBao = thongBao;
            this.maKhachHang = maKhachHang;
        }

        public static KetQua ok(String thongBao, String maKhachHang) {
            return new KetQua(true, thongBao, maKhachHang);
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

        public String getMaKhachHang() {
            return maKhachHang;
        }
    }

    // ---------------------------------------------------------------------
    // Đọc dữ liệu (không cần kiểm tra gì thêm)
    // ---------------------------------------------------------------------

    public ArrayList<KhachHang> timKiem(String tuKhoa) {
        return dao.timKiem(tuKhoa);
    }

    public ArrayList<String[]> getThuCung(String maKhachHang) {
        return dao.getThuCung(maKhachHang);
    }

    // ---------------------------------------------------------------------
    // Thêm / sửa / xóa
    // ---------------------------------------------------------------------

    public KetQua them(String hoTen, String sdt, ArrayList<String[]> dsThuCungTho) {
        hoTen = hoTen.trim();
        sdt = sdt.replaceAll("\\s", "");

        String loi = kiemTraKhachHang(hoTen, sdt);
        if (loi != null) {
            return KetQua.loi(loi);
        }
        if (dao.tonTaiSdt(sdt)) {
            return KetQua.loi("Số điện thoại " + sdt + " đã thuộc về khách hàng khác.");
        }

        // Đổi từng dòng thú cưng thô thành ThuCungMoi, kiểm tra luôn
        ArrayList<ThuCungMoi> dsThuCung = new ArrayList<>();
        for (String[] r : dsThuCungTho) {
            String ten = r[0].trim();
            String giong = r[2].trim();
            String kg = r[4].trim().replace(',', '.');

            if (ten.isEmpty()) {
                return KetQua.loi("Mỗi thú cưng cần có tên (bấm Xóa nếu không dùng).");
            }

            Double canNang = null;
            if (!kg.isEmpty()) {
                if (!kg.matches("\\d+(\\.\\d+)?")) {
                    return KetQua.loi("Cân nặng phải là số, ví dụ 4.5");
                }
                canNang = Double.parseDouble(kg);
                if (canNang >= 1000) {
                    return KetQua.loi("Cân nặng phải nhỏ hơn 1000 kg.");
                }
            }

            dsThuCung.add(new ThuCungMoi(ten, r[1], giong, r[3], canNang));
        }

        KhachHang kh = new KhachHang(null, hoTen, sdt, null, null, 0);

        String loiDao = dao.them(kh, dsThuCung); // thành công: kh có mã mới
        if (loiDao != null) {
            return KetQua.loi(loiDao);
        }
        return KetQua.ok("Đã thêm khách hàng " + kh.getMaKhachHang() + ".", kh.getMaKhachHang());
    }

    public KetQua sua(String maKhachHang, String hoTen, String sdt) {
        hoTen = hoTen.trim();
        sdt = sdt.replaceAll("\\s", "");

        String loi = kiemTraKhachHang(hoTen, sdt);
        if (loi != null) {
            return KetQua.loi(loi);
        }
        if (dao.tonTaiSdtKhac(sdt, maKhachHang)) {
            return KetQua.loi("Số điện thoại " + sdt + " đã thuộc về khách hàng khác.");
        }

        KhachHang kh = new KhachHang(maKhachHang, hoTen, sdt, null, null, 0);

        String loiDao = dao.sua(kh);
        if (loiDao != null) {
            return KetQua.loi(loiDao);
        }
        return KetQua.ok("Đã cập nhật khách hàng " + maKhachHang + ".", maKhachHang);
    }

    public KetQua xoa(String maKhachHang) {
        String loiDao = dao.xoa(maKhachHang);
        if (loiDao != null) {
            return KetQua.loi(loiDao);
        }
        return KetQua.ok("Đã xóa khách hàng " + maKhachHang + ".", maKhachHang);
    }

    // ---------------------------------------------------------------------
    // Hàm phụ
    // ---------------------------------------------------------------------

    /** Kiểm tra họ tên + số điện thoại (dùng chung cho thêm và sửa). Hợp lệ -> null. */
    private String kiemTraKhachHang(String hoTen, String sdt) {
        if (hoTen.isEmpty()) {
            return "Họ tên không được để trống.";
        }
        if (!sdt.matches("\\d{10,11}")) {
            return "Số điện thoại phải gồm 10 hoặc 11 chữ số.";
        }
        return null;
    }
}
