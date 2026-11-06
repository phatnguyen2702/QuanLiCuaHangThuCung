package control;
import dao.NhapKhoDAO;
import entity.ChiTietPhieuNhap;
import entity.NhaCungCap;
import entity.PhieuNhapKho;
import entity.SanPham;
import java.sql.SQLException;
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
        try { dao.hoanTatPhieuNhap(maPhieu); return true; }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static boolean xoaPhieuNhap(String maPhieu) {
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
        try { return dao.themNhaCungCap(nhaCungCap); }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static ArrayList<SanPham> timSanPhamTheoNhaCungCap(String maNhaCungCap) {
        try { return dao.timSanPhamTheoNhaCungCap(maNhaCungCap); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
}
