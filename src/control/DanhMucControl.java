package control;
import dao.DanhMucDAO;
import entity.DanhMuc;
import entity.SanPham;
import java.sql.SQLException;
import java.util.ArrayList;
public final class DanhMucControl {
    private static final DanhMucDAO dao = new DanhMucDAO();
    private DanhMucControl() { }
    public static ArrayList<DanhMuc> timKiemDanhMuc(String tuKhoa) {
        try { return dao.timKiemDanhMuc(tuKhoa); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static int demTatCaDanhMuc() {
        try { return dao.demTatCaDanhMuc(); }
        catch (SQLException ex) { ex.printStackTrace(); return 0; }
    }
    public static String taoMaDanhMucMoi() {
        try { return dao.taoMaDanhMucMoi(); }
        catch (SQLException ex) { ex.printStackTrace(); return ""; }
    }
    public static int[] demSanPhamTrongDanhMuc(String maDanhMuc) {
        try { return dao.demSanPhamTrongDanhMuc(maDanhMuc); }
        catch (SQLException ex) { ex.printStackTrace(); return new int[4]; }
    }
    public static boolean xoaDanhMuc(String maDanhMuc) {
        try { return dao.xoaDanhMuc(maDanhMuc); }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static boolean capNhatDanhMuc(DanhMuc danhMuc) {
        kiemTraDanhMuc(danhMuc);
        try { return dao.capNhatDanhMuc(danhMuc); }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static boolean themDanhMuc(DanhMuc danhMuc) {
        kiemTraDanhMuc(danhMuc);
        try { return dao.themDanhMuc(danhMuc); }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static ArrayList<SanPham> laySanPhamTheoDanhMuc(String maDanhMuc) {
        kiemTraMaDanhMuc(maDanhMuc);
        try { return dao.laySanPhamTheoDanhMuc(maDanhMuc); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static void kiemTraDanhMuc(DanhMuc danhMuc) {
        if (danhMuc == null) throw new IllegalArgumentException("Thông tin danh mục không hợp lệ.");
        kiemTraMaDanhMuc(danhMuc.getMaDanhMuc());
        String ten = danhMuc.getTenDanhMuc() == null ? "" : danhMuc.getTenDanhMuc().trim();
        String moTa = danhMuc.getMoTa() == null ? "" : danhMuc.getMoTa().trim();
        if (ten.isEmpty() || ten.length() > 50)
            throw new IllegalArgumentException("Tên danh mục không được để trống và tối đa 50 ký tự.");
        if (moTa.length() > 255)
            throw new IllegalArgumentException("Mô tả danh mục tối đa 255 ký tự.");
        danhMuc.setTenDanhMuc(ten);
        danhMuc.setMoTa(moTa.isEmpty() ? null : moTa);
    }
    private static void kiemTraMaDanhMuc(String maDanhMuc) {
        if (maDanhMuc == null || maDanhMuc.isBlank() || maDanhMuc.trim().length() > 10)
            throw new IllegalArgumentException("Mã danh mục không được để trống và tối đa 10 ký tự.");
    }
}
