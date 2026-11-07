package control;
import dao.NhapKhoDAO;
import entity.ChiTietPhieuNhap;
import entity.NhaCungCap;
import entity.PhieuNhapKho;
import entity.SanPham;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public final class NhapKhoControl {
    private static final NhapKhoDAO dao = new NhapKhoDAO();
    private NhapKhoControl() { }
    public static String taoMaPhieuNhapMoi() {
        try { return dao.taoMaPhieuNhapMoi(); }
        catch (SQLException ex) { ex.printStackTrace(); return ""; }
    }
    public static boolean luuPhieuNhap(PhieuNhapKho phieu, List<ChiTietPhieuNhap> chiTiet) {
        kiemTraPhieuNhap(phieu, chiTiet);
        try { dao.luuPhieuNhap(phieu, chiTiet); return true; }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static ArrayList<ChiTietPhieuNhap> layChiTietPhieuNhap(String maPhieu) {
        try { return dao.layChiTietPhieuNhap(maPhieu); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static NhapKhoDAO.BanInPhieuNhap layThongTinInPhieuNhap(String maPhieu) {
        try { return dao.layThongTinInPhieuNhap(maPhieu); }
        catch (SQLException ex) { ex.printStackTrace(); return null; }
    }
    public static ArrayList<PhieuNhapKho> layDanhSachPhieuNhap() {
        try { return dao.layDanhSachPhieuNhap(); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static boolean hoanTatPhieuNhap(String maPhieu) {
        kiemTraMa(maPhieu, "Mã phiếu nhập");
        try { dao.hoanTatPhieuNhap(maPhieu); return true; }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static boolean xoaPhieuNhap(String maPhieu) {
        kiemTraMa(maPhieu, "Mã phiếu nhập");
        try { dao.xoaPhieuNhap(maPhieu); return true; }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static ArrayList<NhaCungCap> layDanhSachNhaCungCap() {
        try { return dao.layDanhSachNhaCungCap(); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static String taoMaNhaCungCapMoi() {
        try { return dao.taoMaNhaCungCapMoi(); }
        catch (SQLException ex) { ex.printStackTrace(); return ""; }
    }
    public static boolean themNhaCungCap(NhaCungCap nhaCungCap) {
        kiemTraNhaCungCap(nhaCungCap);
        try { return dao.themNhaCungCap(nhaCungCap); }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static ArrayList<SanPham> timSanPhamTheoNhaCungCap(String maNhaCungCap) {
        kiemTraMa(maNhaCungCap, "Mã nhà cung cấp");
        try { return dao.timSanPhamTheoNhaCungCap(maNhaCungCap); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static void kiemTraPhieuNhap(PhieuNhapKho phieu, List<ChiTietPhieuNhap> chiTiet) {
        if (phieu == null) throw new IllegalArgumentException("Thông tin phiếu nhập không hợp lệ.");
        kiemTraMa(phieu.getMaPhieuNhap(), "Mã phiếu nhập");
        kiemTraMa(phieu.getMaNhanVien(), "Mã nhân viên");
        kiemTraMa(phieu.getMaNhaCungCap(), "Mã nhà cung cấp");
        if (phieu.getNgayNhap() == null) throw new IllegalArgumentException("Thời điểm nhập kho chưa hợp lệ.");
        if (chiTiet == null || chiTiet.isEmpty()) throw new IllegalArgumentException("Phiếu nhập phải có ít nhất một sản phẩm.");
        java.util.HashSet<String> maDaNhap = new java.util.HashSet<>();
        for (ChiTietPhieuNhap dong : chiTiet) {
            if (dong == null) throw new IllegalArgumentException("Chi tiết phiếu nhập không hợp lệ.");
            kiemTraMa(dong.getMaSanPham(), "Mã sản phẩm");
            if (!maDaNhap.add(dong.getMaSanPham()))
                throw new IllegalArgumentException("Mỗi sản phẩm chỉ được xuất hiện một lần trong phiếu nhập.");
            if (dong.getSoLuongNhap() <= 0) throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");
            if (!Double.isFinite(dong.getDonGiaNhap()) || dong.getDonGiaNhap() < 0)
                throw new IllegalArgumentException("Đơn giá nhập phải là số không âm.");
            if (dong.getHanSuDung() != null && dong.getHanSuDung().isBefore(LocalDate.now()))
                throw new IllegalArgumentException("Hạn sử dụng không được trước ngày hiện tại.");
        }
    }
    public static void kiemTraNhaCungCap(NhaCungCap nhaCungCap) {
        if (nhaCungCap == null) throw new IllegalArgumentException("Thông tin nhà cung cấp không hợp lệ.");
        kiemTraMa(nhaCungCap.getMaNhaCungCap(), "Mã nhà cung cấp");
        if (nhaCungCap.getTenNhaCungCap() == null || nhaCungCap.getTenNhaCungCap().isBlank()
                || nhaCungCap.getTenNhaCungCap().trim().length() > 50)
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống và tối đa 50 ký tự.");
    }
    private static void kiemTraMa(String ma, String tenTruong) {
        if (ma == null || ma.isBlank() || ma.trim().length() > 10)
            throw new IllegalArgumentException(tenTruong + " không được để trống và tối đa 10 ký tự.");
    }
}
