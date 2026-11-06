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
        try { dao.luuPhieuKiemKe(phieu, chiTiet); return true; }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static boolean luuPhieuKiemKeNhap(PhieuKiemKe phieu, List<ChiTietPhieuKiemKe> chiTiet) {
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
}
