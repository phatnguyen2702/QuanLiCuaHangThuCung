package control;

import dao.NhanVienDAO;
import entity.NhanVien;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

public class NhanVienControl {
    private final NhanVienDAO dao = new NhanVienDAO();

    public static class KetQuaKiemTra {
        private final boolean hopLe;
        private final String thongBao;
        private final String truong;

        public KetQuaKiemTra(boolean hopLe, String thongBao, String truong) {
            this.hopLe = hopLe;
            this.thongBao = thongBao;
            this.truong = truong;
        }

        public static KetQuaKiemTra thanhCong() {
            return new KetQuaKiemTra(true, null, null);
        }

        public boolean hopLe() {
            return hopLe;
        }

        public String thongBao() {
            return thongBao;
        }

        public String truong() {
            return truong;
        }
    }

    public static class KetQuaThaoTac {
        private final boolean thanhCong;
        private final String thongBao;

        public KetQuaThaoTac(boolean thanhCong, String thongBao) {
            this.thanhCong = thanhCong;
            this.thongBao = thongBao;
        }

        public boolean thanhCong() {
            return thanhCong;
        }

        public String thongBao() {
            return thongBao;
        }
    }

    public ArrayList<NhanVien> layDanhSach() {
        return dao.getAll();
    }

    public String goiYMa() {
        return dao.goiYMa();
    }

    public KetQuaThaoTac themNhanVien(NhanVien nhanVien) {
        KetQuaKiemTra ketQua = kiemTra(nhanVien);
        if (!ketQua.hopLe()) {
            return new KetQuaThaoTac(false, ketQua.thongBao());
        }
        try {
            boolean thanhCong = dao.them(nhanVien);
            return thanhCong
                    ? new KetQuaThaoTac(true, "Thêm nhân viên thành công! Mã nhân viên: " + nhanVien.getMaNhanVien())
                    : new KetQuaThaoTac(false, "Không thể thêm nhân viên.");
        } catch (SQLException ex) {
            return new KetQuaThaoTac(false, "Lỗi thêm nhân viên: " + ex.getMessage());
        }
    }

    public KetQuaThaoTac suaNhanVien(NhanVien nhanVien) {
        KetQuaKiemTra ketQua = kiemTra(nhanVien);
        if (!ketQua.hopLe()) {
            return new KetQuaThaoTac(false, ketQua.thongBao());
        }
        return dao.sua(nhanVien)
                ? new KetQuaThaoTac(true, "Cập nhật tài khoản thành công!")
                : new KetQuaThaoTac(false, "Cập nhật tài khoản thất bại!");
    }

    public KetQuaThaoTac xoaNhanVien(String maNhanVien) {
        if (maNhanVien == null || maNhanVien.isBlank()) {
            return new KetQuaThaoTac(false, "Vui lòng chọn nhân viên cần xóa.");
        }
        if (maNhanVien.equals(TrangChuControl.maNV)) {
            return new KetQuaThaoTac(false, "Không thể xóa tài khoản đang đăng nhập.");
        }
        try {
            return dao.xoa(maNhanVien)
                    ? new KetQuaThaoTac(true, "Xóa tài khoản thành công!")
                    : new KetQuaThaoTac(false, "Không tìm thấy nhân viên cần xóa.");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 547 || ex.getMessage().contains("REFERENCE constraint")) {
                return new KetQuaThaoTac(false,
                        "Không thể xóa nhân viên vì đang có dữ liệu liên quan (lịch làm, lịch hẹn, phiếu dịch vụ, hóa đơn hoặc phiếu kho). "
                                + "Hãy chuyển/xóa các dữ liệu liên quan trước.");
            }
            return new KetQuaThaoTac(false, "Lỗi xóa nhân viên: " + ex.getMessage());
        } catch (SecurityException ex) {
            return new KetQuaThaoTac(false, ex.getMessage());
        }
    }

    public NhanVien taoNhanVien(String ma, String hoTen, String soDienThoai,
            String email, String matKhau, String gioiTinh, String vaiTro) {
        return new NhanVien(ma, chuoiAnToan(hoTen).trim(),
                chuoiAnToan(soDienThoai).trim(), chuoiAnToan(email).trim(), matKhau,
                gioiTinh, vaiTro);
    }

    public KetQuaKiemTra kiemTra(NhanVien nhanVien) {
        if (nhanVien == null) {
            return new KetQuaKiemTra(false, "Dữ liệu nhân viên không hợp lệ.", null);
        }

        String hoTen = chuoiAnToan(nhanVien.getHoTen()).trim();
        if (hoTen.length() < 2 || hoTen.length() > 50 || !hoTen.matches("[\\p{L} ]+")) {
            return new KetQuaKiemTra(false,
                    "Họ tên từ 2–50 ký tự, chỉ chứa chữ cái (có dấu) và dấu cách.", "hoTen");
        }

        String soDienThoai = chuoiAnToan(nhanVien.getSoDienThoai()).trim();
        if (!soDienThoai.matches("0\\d{9}")) {
            return new KetQuaKiemTra(false,
                    "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.", "soDienThoai");
        }

        String email = chuoiAnToan(nhanVien.getEmail()).trim();
        if (email.length() > 30 || !email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return new KetQuaKiemTra(false,
                    "Email không hợp lệ. Ví dụ: ten@example.com.", "email");
        }

        String matKhau = chuoiAnToan(nhanVien.getMatKhau());
        if (matKhau.length() < 6 || matKhau.length() > 255) {
            return new KetQuaKiemTra(false, "Mật khẩu phải có từ 6–255 ký tự.", "matKhau");
        }

        if (!"Nam".equals(nhanVien.getGioiTinh()) && !"Nữ".equals(nhanVien.getGioiTinh())) {
            return new KetQuaKiemTra(false, "Giới tính chỉ được chọn Nam/Nữ.", "gioiTinh");
        }
        if (chuoiAnToan(nhanVien.getVaiTro()).isBlank()) {
            return new KetQuaKiemTra(false, "Vui lòng chọn vai trò.", "vaiTro");
        }
        return KetQuaKiemTra.thanhCong();
    }

    public ArrayList<NhanVien> timKiem(String tuKhoa) {
        String keyword = chuoiAnToan(tuKhoa).trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            return layDanhSach();
        }
        ArrayList<NhanVien> ketQua = new ArrayList<>();
        for (NhanVien nhanVien : layDanhSach()) {
            String duLieuTimKiem = String.join(" ",
                    chuoiAnToan(nhanVien.getMaNhanVien()),
                    chuoiAnToan(nhanVien.getHoTen()),
                    chuoiAnToan(nhanVien.getSoDienThoai()),
                    chuoiAnToan(nhanVien.getEmail()),
                    chuoiAnToan(nhanVien.getGioiTinh()),
                    chuoiAnToan(hienThiVaiTro(nhanVien.getVaiTro())));

            if (chuoiAnToan(duLieuTimKiem).toLowerCase(Locale.ROOT).contains(keyword)) {
                ketQua.add(nhanVien);
            }
        }
        return ketQua;
    }

    public String chuoiAnToan(String value) {
        return value == null ? "" : value;
    }

    public String hienThiVaiTro(String value) {
        if (value == null)
            return "";
        String vaiTro = value.trim().toLowerCase(Locale.ROOT);
        return switch (vaiTro) {
            case "quan ly", "quản lý" -> "Quản lý";
            case "thu ngan", "thu ngân" -> "Thu ngân";
            case "nhan vien kho", "nhân viên kho" -> "Nhân viên kho";
            case "nhan vien spa", "nhân viên spa" -> "Nhân viên Spa";
            default -> value;
        };
    }

    public String maVaiTro(String value) {
        return switch (hienThiVaiTro(value)) {
            case "Quản lý" -> "Quan ly";
            case "Thu ngân" -> "Thu ngan";
            case "Nhân viên kho" -> "Nhan vien kho";
            case "Nhân viên Spa" -> "Nhan vien Spa";
            default -> value == null ? "" : value;
        };
    }
}
