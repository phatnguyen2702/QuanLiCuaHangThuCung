package control;
import dao.SanPhamDAO;
import entity.DanhMuc;
import entity.NhaCungCap;
import entity.SanPham;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Set;
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
        kiemTraTieuChiGoiYThucAn(loaiThuCung, nhomTuoi, kichThuoc);
        try { return dao.timSanPhamGoiYThucAn(loaiThuCung, nhomTuoi, kichThuoc); }
        catch (SQLException ex) { ex.printStackTrace(); return new ArrayList<>(); }
    }
    public static int laySoLuongTonSanPham(String maSanPham) {
        try { return dao.laySoLuongTonSanPham(maSanPham); }
        catch (SQLException ex) { ex.printStackTrace(); return 0; }
    }
    public static int laySoLuongBanDuocSanPham(String maSanPham) {
        try { return dao.layTonBanDuocTatCaSanPham().getOrDefault(maSanPham, 0); }
        catch (SQLException ex) { ex.printStackTrace(); return -1; }
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
        kiemTraMa(maSanPham, "Mã sản phẩm");
        kiemTraMa(maPhieuNhap, "Mã phiếu nhập");
        kiemTraMa(maNhanVien, "Mã nhân viên");
        if (soLuong <= 0) throw new IllegalArgumentException("Số lượng xử lý phải lớn hơn 0.");
        if (lyDo == null || lyDo.isBlank() || lyDo.trim().length() > 50)
            throw new IllegalArgumentException("Lý do xử lý không được để trống và tối đa 50 ký tự.");
        if (ghiChu != null && ghiChu.trim().length() > 250)
            throw new IllegalArgumentException("Ghi chú xử lý tối đa 250 ký tự.");
        try { dao.ghiNhanXuLySanPhamTheoDotNhap(maSanPham, maPhieuNhap, soLuong, lyDo, ghiChu, maNhanVien); return true; }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static List<Object[]> layLichSuXuLySanPham() {
        try { return dao.layLichSuXuLySanPham(); }
        catch (SQLException ex) { ex.printStackTrace(); return List.of(); }
    }
    public static boolean luuThongTinSanPham(SanPham sanPham, boolean dangSua, String tenDanhMuc, String nhomTuoi, String kichThuoc, String maNhanVien) {
        kiemTraSanPham(sanPham, tenDanhMuc, maNhanVien);
        kiemTraTieuChiNhomTuoiKichThuoc(nhomTuoi, kichThuoc);
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
        kiemTraMa(maPhieuNhap, "Mã phiếu nhập");
        kiemTraMa(maSanPham, "Mã sản phẩm");
        try { return dao.ngungKinhDoanhLo(maPhieuNhap, maSanPham); }
        catch (SQLException ex) { ex.printStackTrace(); return false; }
    }
    public static void kiemTraSanPham(SanPham sanPham, String tenDanhMuc, String maNhanVien) {
        if (sanPham == null) throw new IllegalArgumentException("Thông tin sản phẩm không hợp lệ.");
        kiemTraMa(sanPham.getMaSanPham(), "Mã sản phẩm");
        kiemTraMa(sanPham.getMaDanhMuc(), "Mã danh mục");
        kiemTraMa(sanPham.getMaNhaCungCap(), "Mã nhà cung cấp");
        kiemTraMa(maNhanVien, "Mã nhân viên");
        if (sanPham.getTenSanPham() == null || sanPham.getTenSanPham().isBlank()
                || sanPham.getTenSanPham().trim().length() > 50)
            throw new IllegalArgumentException("Tên sản phẩm không được để trống và tối đa 50 ký tự.");
        if (!Double.isFinite(sanPham.getGiaNhap()) || sanPham.getGiaNhap() < 0
                || !Double.isFinite(sanPham.getGiaBan()) || sanPham.getGiaBan() < 0)
            throw new IllegalArgumentException("Giá nhập và giá bán phải là số không âm hợp lệ.");
        if (tenDanhMuc == null || tenDanhMuc.isBlank())
            throw new IllegalArgumentException("Vui lòng chọn danh mục sản phẩm.");
        sanPham.setTenSanPham(sanPham.getTenSanPham().trim());
    }
    public static void kiemTraTieuChiGoiYThucAn(String loaiThuCung, String nhomTuoi, String kichThuoc) {
        if (loaiThuCung == null || !Set.of("Chó", "Mèo").contains(loaiThuCung))
            throw new IllegalArgumentException("Loài thú cưng chỉ được chọn Chó hoặc Mèo.");
        if (nhomTuoi == null || !Set.of("Tất cả độ tuổi", "Con non", "Trưởng thành", "Cao tuổi").contains(nhomTuoi))
            throw new IllegalArgumentException("Nhóm tuổi thức ăn không hợp lệ.");
        if (kichThuoc == null || !Set.of("Tất cả kích thước", "Nhỏ", "Trung bình", "Lớn").contains(kichThuoc))
            throw new IllegalArgumentException("Kích thước thức ăn không hợp lệ.");
    }
    private static void kiemTraTieuChiNhomTuoiKichThuoc(String nhomTuoi, String kichThuoc) {
        if (nhomTuoi == null || !Set.of("Con non", "Trưởng thành", "Cao tuổi", "Mọi độ tuổi", "Chưa xác định").contains(nhomTuoi))
            throw new IllegalArgumentException("Nhóm tuổi thức ăn không hợp lệ.");
        if (kichThuoc == null || !Set.of("Nhỏ", "Trung bình", "Lớn", "Mọi kích thước", "Chưa xác định").contains(kichThuoc))
            throw new IllegalArgumentException("Kích thước thức ăn không hợp lệ.");
    }
    private static void kiemTraMa(String ma, String tenTruong) {
        if (ma == null || ma.isBlank() || ma.trim().length() > 10)
            throw new IllegalArgumentException(tenTruong + " không được để trống và tối đa 10 ký tự.");
    }
}
