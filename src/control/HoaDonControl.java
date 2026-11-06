package control;

import dao.HoaDonDAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public final class HoaDonControl {
    private final HoaDonDAO dao = new HoaDonDAO();

    public HoaDonDAO.ThongKe layThongKe() throws SQLException {
        return dao.layThongKe();
    }

    public List<HoaDon> timHoaDon(String tuKhoa) throws SQLException {
        return dao.timHoaDon(tuKhoa);
    }

    public double layTongTienHoaDon(String maHoaDon) throws SQLException {
        return dao.layTongTienHoaDon(maHoaDon);
    }

    public Map<String, Double> layTongTienHoaDon(List<HoaDon> hoaDons) throws SQLException {
        return dao.layTongTienHoaDon(hoaDons);
    }

    public List<ChiTietHoaDon> layDanhSachChiTietHoaDon(String maHoaDon) throws SQLException {
        return dao.layDanhSachChiTietHoaDon(maHoaDon);
    }

    public HoaDon layChiTietHoaDon(String maHoaDon) throws SQLException {
        return dao.layChiTietHoaDon(maHoaDon);
    }
}
