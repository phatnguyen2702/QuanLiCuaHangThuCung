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
        try { return dao.capNhatDanhMuc(danhMuc); }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static boolean themDanhMuc(DanhMuc danhMuc) {
        try { return dao.themDanhMuc(danhMuc); }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static ArrayList<SanPham> laySanPhamTheoDanhMuc(String maDanhMuc) {
        try { return dao.laySanPhamTheoDanhMuc(maDanhMuc); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
}
