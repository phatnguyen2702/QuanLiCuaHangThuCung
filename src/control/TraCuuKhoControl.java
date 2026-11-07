package control;
import dao.TraCuuKhoDAO;
import java.sql.SQLException;
import java.util.List;
public final class TraCuuKhoControl {
    private static final TraCuuKhoDAO dao = new TraCuuKhoDAO();
    private TraCuuKhoControl() { }
    public static List<TraCuuKhoDAO.NhaCungCapTraCuu> layDanhSachNhaCungCap() {
        try { return dao.layDanhSachNhaCungCap(); }
        catch (SQLException ex) { ex.printStackTrace(); return List.of(); }
    }
    public static List<Object[]> timKiemPhieuNhap(String tuKhoa) {
        try { return dao.timKiemPhieuNhap(chuanHoaTuKhoa(tuKhoa)); }
        catch (SQLException ex) { ex.printStackTrace(); return List.of(); }
    }
    public static List<Object[]> timKiemPhieuKiemKe(String tuKhoa) {
        try { return dao.timKiemPhieuKiemKe(chuanHoaTuKhoa(tuKhoa)); }
        catch (SQLException ex) { ex.printStackTrace(); return List.of(); }
    }
    public static List<Object[]> layChiTietPhieuKiemKe(String maPhieu) {
        if (maPhieu == null || maPhieu.isBlank() || maPhieu.trim().length() > 10)
            throw new IllegalArgumentException("Mã phiếu kiểm kê không hợp lệ.");
        try { return dao.layChiTietPhieuKiemKe(maPhieu); }
        catch (SQLException ex) { ex.printStackTrace(); return List.of(); }
    }
    private static String chuanHoaTuKhoa(String tuKhoa) {
        String tuKhoaChuanHoa = tuKhoa == null ? "" : tuKhoa.trim();
        if (tuKhoaChuanHoa.length() > 100)
            throw new IllegalArgumentException("Từ khóa tra cứu tối đa 100 ký tự.");
        return tuKhoaChuanHoa;
    }
}
