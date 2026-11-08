package control;
import dao.KiemKeDAO;
import entity.ChiTietPhieuKiemKe;
import entity.PhieuKiemKe;
import entity.SanPham;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
public final class KiemKeControl {
    private static final KiemKeDAO dao = new KiemKeDAO();
    private KiemKeControl() { }
    public static String taoMaPhieuKiemKeMoi() {
        try { return dao.taoMaPhieuKiemKeMoi(); }
        catch (SQLException ex) { ex.printStackTrace(); return ""; }
    }
    public static boolean luuPhieuKiemKe(PhieuKiemKe phieu, List<ChiTietPhieuKiemKe> chiTiet) {
        kiemTraPhieuKiemKe(phieu, chiTiet, true);
        try { dao.luuPhieuKiemKe(phieu, chiTiet); return true; }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static boolean luuPhieuKiemKeNhap(PhieuKiemKe phieu, List<ChiTietPhieuKiemKe> chiTiet) {
        kiemTraPhieuKiemKe(phieu, chiTiet, false);
        try { dao.luuPhieuKiemKeNhap(phieu, chiTiet); return true; }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static ArrayList<PhieuKiemKe> layDanhSachPhieuKiemKeNhap() {
        try { return dao.layDanhSachPhieuKiemKeNhap(); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static ArrayList<ChiTietPhieuKiemKe> layChiTietPhieuKiemKe(String maPhieu) {
        try { return dao.layChiTietPhieuKiemKe(maPhieu); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static ArrayList<SanPham> layDanhSachSanPhamKemTonKho() {
        try { return dao.layDanhSachSanPhamKemTonKho(); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static void kiemTraPhieuKiemKe(PhieuKiemKe phieu, List<ChiTietPhieuKiemKe> chiTiet, boolean chotPhieu) {
        if (phieu == null) throw new IllegalArgumentException("Thông tin phiếu kiểm kê không hợp lệ.");
        kiemTraMa(phieu.getMaPhieuKiemKe(), "Mã phiếu kiểm kê");
        kiemTraMa(phieu.getMaNhanVien(), "Mã nhân viên");
        if (phieu.getNgayBatDau() == null) throw new IllegalArgumentException("Thiếu thời điểm bắt đầu kiểm kê.");
        if (chotPhieu && (phieu.getNgayChot() == null || phieu.getNgayKiemKe() == null))
            throw new IllegalArgumentException("Thiếu thời điểm chốt kiểm kê.");
        if (chiTiet == null || chiTiet.isEmpty()) throw new IllegalArgumentException("Phiếu kiểm kê phải có ít nhất một lô hàng.");
        java.util.HashSet<String> khoaLo = new java.util.HashSet<>();
        for (ChiTietPhieuKiemKe dong : chiTiet) {
            if (dong == null) throw new IllegalArgumentException("Dòng kiểm kê không hợp lệ.");
            kiemTraMa(dong.getMaSanPham(), "Mã sản phẩm");
            kiemTraMa(dong.getMaPhieuNhap(), "Mã phiếu nhập");
            if (!khoaLo.add(dong.getMaSanPham() + "|" + dong.getMaPhieuNhap()))
                throw new IllegalArgumentException("Lô sản phẩm bị lặp trong phiếu kiểm kê.");
            if (dong.getSoLuongHeThong() < 0)
                throw new IllegalArgumentException("Tồn hệ thống không được âm.");
            if (chotPhieu && dong.getSoLuongThucTe() == null)
                throw new IllegalArgumentException("Chỉ chốt các lô đã được nhập số lượng thực tế.");
            if (dong.getSoLuongThucTe() != null && dong.getSoLuongThucTe() < 0)
                throw new IllegalArgumentException("Số lượng thực tế không được âm.");
        }
    }
    private static void kiemTraMa(String ma, String tenTruong) {
        if (ma == null || ma.isBlank() || ma.trim().length() > 10)
            throw new IllegalArgumentException(tenTruong + " không được để trống và tối đa 10 ký tự.");
    }
}
