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
        try { return dao.timKiemPhieuNhap(tuKhoa); }
        catch (SQLException ex) { ex.printStackTrace(); return List.of(); }
    }
    public static List<Object[]> timKiemPhieuKiemKe(String tuKhoa) {
        try { return dao.timKiemPhieuKiemKe(tuKhoa); }
        catch (SQLException ex) { ex.printStackTrace(); return List.of(); }
    }
    public static List<Object[]> layChiTietPhieuKiemKe(String maPhieu) {
        try { return dao.layChiTietPhieuKiemKe(maPhieu); }
        catch (SQLException ex) { ex.printStackTrace(); return List.of(); }
    }
}
