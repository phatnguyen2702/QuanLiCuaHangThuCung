package control;

import dao.DichVuDAO;
import entity.DichVu;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

/** Xác thực dữ liệu và điều phối CRUD dịch vụ. Không phụ thuộc Swing. */
public final class DichVuControl {
    private final DichVuDAO dao = new DichVuDAO();
    public ArrayList<DichVu> timKiem(String keyword) throws SQLException { return dao.timKiem(keyword); }
    public String maDuKien() throws SQLException { return dao.maDuKien(); }
    public String them(DichVu dv) throws SQLException { kiemTra(dv); return dao.them(dv); }
    public boolean sua(DichVu dv) throws SQLException { kiemTra(dv); return dao.sua(dv); }
    public int[] demLienQuan(String ma) throws SQLException { return dao.demLienQuan(ma); }
    /** Trả thông báo nếu dịch vụ đã được sử dụng; null nếu có thể xóa. */
    public String lyDoKhongTheXoa(String ma) throws SQLException {
        int[] lienQuan = dao.demLienQuan(ma);
        if (lienQuan[0] + lienQuan[1] + lienQuan[2] == 0) return null;
        return "Không thể xóa " + ma + " vì đã được sử dụng trong:\n"
                + "• Lịch hẹn Spa: " + lienQuan[0] + "\n"
                + "• Phiếu dịch vụ: " + lienQuan[1] + "\n"
                + "• Hóa đơn: " + lienQuan[2] + "\n"
                + "Cần giữ dịch vụ để bảo toàn lịch sử. Bạn có thể sửa tên/giá/mô tả.";
    }
    public boolean xoa(String ma) throws SQLException {
        int[] lienQuan = dao.demLienQuan(ma);
        if (lienQuan[0] + lienQuan[1] + lienQuan[2] > 0)
            throw new IllegalStateException("Không thể xóa dịch vụ vì đã có dữ liệu liên quan.");
        return dao.xoa(ma);
    }
    public DichVu taoDichVu(String ma, String tenNhap, String giaText, String moTaNhap) {
        String ten = tenNhap == null ? "" : tenNhap.trim();
        String moTa = moTaNhap == null ? "" : moTaNhap.trim();
        if (ten.length() < 2 || ten.length() > 50)
            throw new IllegalArgumentException("Tên dịch vụ phải từ 2 đến 50 ký tự.");
        if (moTa.length() > 255)
            throw new IllegalArgumentException("Mô tả không được quá 255 ký tự.");
        String giaNhap = giaText == null ? "" : giaText.trim().replace(".", "").replace(" ", "");
        if (!giaNhap.matches("[0-9]{1,16}"))
            throw new IllegalArgumentException("Giá dịch vụ phải là số nguyên dương (VNĐ), ví dụ 150000.");
        BigDecimal gia = new BigDecimal(giaNhap);
        if (gia.signum() <= 0 || gia.precision() > 16)
            throw new IllegalArgumentException("Giá dịch vụ phải lớn hơn 0 và vừa kiểu DECIMAL(18,2).");
        return new DichVu(ma, ten, gia, moTa.isEmpty() ? null : moTa);
    }
    public void kiemTra(DichVu dv) {
        // Giữ chung một quy tắc cho form và cả lời gọi từ nơi khác.
        taoDichVu(dv.getMaDichVu(), dv.getTenDichVu(),
                dv.getGiaDichVu() == null ? "" : dv.getGiaDichVu().toPlainString(), dv.getMoTa());
    }
}
