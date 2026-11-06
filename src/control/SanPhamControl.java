package control;
import dao.SanPhamDAO;
import entity.DanhMuc;
import entity.NhaCungCap;
import entity.SanPham;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public final class SanPhamControl {
    private static final SanPhamDAO dao = new SanPhamDAO();
    private SanPhamControl() { }
    public static ArrayList<SanPham> timKiemSanPham(String tuKhoa, String maDanhMuc, String maNhaCungCap) {
        try { return dao.timKiemSanPham(tuKhoa, maDanhMuc, maNhaCungCap); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static ArrayList<SanPham> timSanPhamGoiYThucAn(String loaiThuCung, String nhomTuoi, String kichThuoc) {
        try { return dao.timSanPhamGoiYThucAn(loaiThuCung, nhomTuoi, kichThuoc); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static int laySoLuongTonSanPham(String maSanPham) {
        try { return dao.laySoLuongTonSanPham(maSanPham); }
        catch (SQLException ex) { ex.printStackTrace(); return 0; }
    }
    public static ArrayList<SanPhamDAO.DotNhap> layDanhSachDotNhapSanPham() {
        try { return dao.layDanhSachDotNhapSanPham(); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static ArrayList<SanPhamDAO.DotNhap> layDanhSachDotNhapSanPham(String maSanPham) {
        try { return dao.layDanhSachDotNhapSanPham(maSanPham); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static ArrayList<SanPham> timSanPhamTheoDotNhap(String tuKhoa, String maDanhMuc, String maPhieuNhap, String maSanPham) {
        try { return dao.timSanPhamTheoDotNhap(tuKhoa, maDanhMuc, maPhieuNhap, maSanPham); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static boolean ghiNhanXuLySanPhamTheoDotNhap(String maSanPham, String maPhieuNhap, int soLuong, String lyDo, String ghiChu, String maNhanVien) {
        try { dao.ghiNhanXuLySanPhamTheoDotNhap(maSanPham, maPhieuNhap, soLuong, lyDo, ghiChu, maNhanVien); return true; }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static List<Object[]> layLichSuXuLySanPham() {
        try { return dao.layLichSuXuLySanPham(); }
        catch (SQLException ex) { ex.printStackTrace(); return List.of(); }
    }
    public static boolean luuThongTinSanPham(SanPham sanPham, boolean dangSua, String tenDanhMuc, String nhomTuoi, String kichThuoc, String maNhanVien) {
        try { dao.luuThongTinSanPham(sanPham, dangSua, tenDanhMuc, nhomTuoi, kichThuoc, maNhanVien); return true; }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static String taoMaSanPhamMoi() {
        try { return dao.taoMaSanPhamMoi(); }
        catch (SQLException ex) { ex.printStackTrace(); return ""; }
    }
    public static boolean xoaSanPham(String maSanPham) {
        try { return dao.xoaSanPham(maSanPham); }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static int demLichSuSanPham(String maSanPham) {
        try { return dao.demLichSuSanPham(maSanPham); }
        catch (SQLException ex) { ex.printStackTrace(); return 0; }
    }
    public static ArrayList<DanhMuc> layDanhSachDanhMucSanPham() {
        try { return dao.layDanhSachDanhMucSanPham(); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static ArrayList<NhaCungCap> layDanhSachNhaCungCapSanPham() {
        try { return dao.layDanhSachNhaCungCapSanPham(); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static LocalDate layHanSuDungSomNhatSanPham(String maSanPham) {
        try { return dao.layHanSuDungSomNhatSanPham(maSanPham); }
        catch (SQLException ex) { ex.printStackTrace(); return null; }
    }
    public static String[] layThongTinGoiYThucAn(String maSanPham) {
        try { return dao.layThongTinGoiYThucAn(maSanPham); }
        catch (SQLException ex) { ex.printStackTrace(); return null; }
    }
    public static boolean luuThongTinGoiYThucAn(String maSanPham, String tenDanhMuc, String tenSanPham, String nhomTuoi, String kichThuoc) {
        try { dao.luuThongTinGoiYThucAn(maSanPham, tenDanhMuc, tenSanPham, nhomTuoi, kichThuoc); return true; }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static ArrayList<SanPhamDAO.LoCanhBaoHanSuDung> layDanhSachLoCanhBaoHanSuDung() {
        try { return dao.layDanhSachLoCanhBaoHanSuDung(); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static boolean ngungKinhDoanhLo(String maPhieuNhap, String maSanPham) {
        try { return dao.ngungKinhDoanhLo(maPhieuNhap, maSanPham); }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
}
