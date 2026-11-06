package control;

import dao.LichHenDAO;
import dao.PhieuDichVuDAO;
import entity.LichHen;
import entity.PhieuDichVu;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PhieuDichVuControl {
    private final PhieuDichVuDAO phieuDichVuDAO = new PhieuDichVuDAO();
    private final LichHenDAO lichHenDAO = new LichHenDAO();

    public ArrayList<PhieuDichVu> layDanhSach() {
        return phieuDichVuDAO.getAll();
    }

    public ArrayList<String[]> layDanhSachDichVu() {
        return phieuDichVuDAO.getDanhSachDichVu();
    }

    public ArrayList<String> layDanhSachLichHen(String maPhieuDangSua) {
        return phieuDichVuDAO.getDanhSachLichHen(maPhieuDangSua);
    }

    public ArrayList<String[]> layDanhSachKhachHang() {
        return lichHenDAO.getDanhSachKhachHang();
    }

    public ArrayList<String[]> layDanhSachNhanVienSpa() {
        return lichHenDAO.getDanhSachNhanVienSpa();
    }

    public ArrayList<String[]> layDanhSachThuCungTheoKhachHang(String maKhachHang) {
        return lichHenDAO.getDanhSachThuCungTheoKhachHang(maKhachHang);
    }

    public ArrayList<String> layMaDichVuCuaPhieu(String maPhieu) {
        return phieuDichVuDAO.getDichVuCuaPhieu(maPhieu);
    }

    public String taoMaTuDong() {
        return phieuDichVuDAO.taoMaTuDong();
    }

    public boolean coTheSua(PhieuDichVu phieu) {
        return phieu != null && !"Đã thanh toán".equals(phieu.getTrangThai());
    }

    public PhieuDichVu timTheoMa(String maPhieu) {
        if (maPhieu == null)
            return null;
        for (PhieuDichVu phieu : layDanhSach()) {
            if (maPhieu.equals(phieu.getMaPhieuDichVu())) {
                return phieu;
            }
        }
        return null;
    }

    public ArrayList<PhieuDichVu> locDanhSach(List<PhieuDichVu> danhSach,
            String nhanVien, String trangThai, String dichVu, String tuKhoa) {
        String tuKhoaChuanHoa = chuoiAnToan(tuKhoa).trim().toLowerCase(java.util.Locale.ROOT);
        ArrayList<PhieuDichVu> ketQua = new ArrayList<>();
        for (PhieuDichVu phieu : danhSach) {
            if (nhanVien != null && !"Tất cả".equals(nhanVien)
                    && !nhanVien.equals(phieu.getTenNhanVien())) {
                continue;
            }
            if (trangThai != null && !"Tất cả".equals(trangThai)
                    && !trangThai.equals(phieu.getTrangThai())) {
                continue;
            }
            String cacDichVu = chuoiAnToan(phieu.getCacDichVu());
            if (dichVu != null && !"Tất cả".equals(dichVu)
                    && !coDichVu(cacDichVu, dichVu)) {
                continue;
            }
            if (!tuKhoaChuanHoa.isEmpty()
                    && !chuoiAnToan(phieu.getMaPhieuDichVu()).toLowerCase(java.util.Locale.ROOT)
                            .contains(tuKhoaChuanHoa)
                    && !chuoiAnToan(phieu.getMaLichHen()).toLowerCase(java.util.Locale.ROOT)
                            .contains(tuKhoaChuanHoa)
                    && !chuoiAnToan(phieu.getTenThuCung()).toLowerCase(java.util.Locale.ROOT)
                            .contains(tuKhoaChuanHoa)
                    && !cacDichVu.toLowerCase(java.util.Locale.ROOT).contains(tuKhoaChuanHoa)) {
                continue;
            }
            ketQua.add(phieu);
        }
        return ketQua;
    }

    private boolean coDichVu(String danhSach, String tenDichVu) {
        for (String dichVu : danhSach.split(",\\s*")) {
            if (tenDichVu.equals(dichVu))
                return true;
        }
        return false;
    }

    public LichHen kiemTraLichHen(String maLichHen, String maLichHenCu) {
        if (maLichHen == null || maLichHen.isBlank() || "Không có".equals(maLichHen)) {
            return null;
        }
        LichHen lichHen = lichHenDAO.timTheoMa(maLichHen);
        boolean lichCu = maLichHen.equals(maLichHenCu);
        if (lichHen == null || "Đã hủy".equalsIgnoreCase(chuoiAnToan(lichHen.getTrangThai()))
                || lichHen.getNgayGioHen() == null
                || (!lichCu && lichHen.getNgayGioHen().isBefore(LocalDateTime.now()))) {
            throw new IllegalArgumentException(
                    "Lịch hẹn đã qua giờ, đã bị hủy hoặc không còn hợp lệ. Hãy chọn lịch hẹn khác hoặc 'Không có'.");
        }
        return lichHen;
    }

    public boolean luu(PhieuDichVu phieu, List<String> maDichVuDuocChon,
            String maLichHenCu, boolean dangSua) {
        if (phieu == null) {
            throw new IllegalArgumentException("Thông tin phiếu dịch vụ không hợp lệ.");
        }
        if (chuoiAnToan(phieu.getMaKhachHang()).isBlank()
                || chuoiAnToan(phieu.getMaThuCung()).isBlank()
                || chuoiAnToan(phieu.getMaNhanVien()).isBlank()) {
            throw new IllegalArgumentException("Hãy chọn đầy đủ khách hàng, thú cưng và thợ Spa.");
        }
        if (maDichVuDuocChon == null || maDichVuDuocChon.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 dịch vụ!");
        }

        ArrayList<String> maDaChon = new ArrayList<>();
        for (String maDichVu : maDichVuDuocChon) {
            if (!maDaChon.contains(maDichVu))
                maDaChon.add(maDichVu);
        }

        ArrayList<String[]> dichVuDuocChon = new ArrayList<>();
        for (String[] dichVu : layDanhSachDichVu()) {
            if (maDaChon.contains(dichVu[0])) {
                dichVuDuocChon.add(dichVu);
                maDaChon.remove(dichVu[0]);
            }
        }
        if (!maDaChon.isEmpty()) {
            throw new IllegalArgumentException("Một hoặc nhiều dịch vụ đã chọn không còn hợp lệ.");
        }

        String maLichHen = phieu.getMaLichHen();
        if (maLichHen != null && !"Không có".equals(maLichHen)) {
            kiemTraLichHen(maLichHen, dangSua ? maLichHenCu : null);
        }
        return dangSua
                ? phieuDichVuDAO.sua(phieu, dichVuDuocChon)
                : phieuDichVuDAO.them(phieu, dichVuDuocChon);
    }

    public boolean xoa(String maPhieu) {
        if (chuoiAnToan(maPhieu).isBlank()) {
            throw new IllegalArgumentException("Mã phiếu dịch vụ không hợp lệ.");
        }
        return phieuDichVuDAO.xoa(maPhieu);
    }

    private static String chuoiAnToan(String value) {
        return value == null ? "" : value;
    }
}
