package control;

import dao.NhanVienDAO;
import entity.NhanVien;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

public class NhanVienControl {
    private final NhanVienDAO dao = new NhanVienDAO();

    public record KetQuaKiemTra(boolean hopLe, String thongBao, String truong) {
        public static KetQuaKiemTra thanhCong() {
            return new KetQuaKiemTra(true, null, null);
        }
    }

    public record KetQuaThaoTac(boolean thanhCong, String thongBao) {
    }

    public ArrayList<NhanVien> layDanhSach() {
        return dao.getAll();
    }

    public boolean them(NhanVien nhanVien) throws SQLException {
        return dao.them(nhanVien);
    }

    public boolean sua(NhanVien nhanVien) {
        return dao.sua(nhanVien);
    }

    public boolean xoa(String maNhanVien) {
        return dao.xoa(maNhanVien);
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
        return dao.xoa(maNhanVien)
                ? new KetQuaThaoTac(true, "Xóa tài khoản thành công!")
                : new KetQuaThaoTac(false, "Xóa tài khoản thất bại!");
    }

    public record DuLieuLamMoi(String maNhanVien) {
    }

    public DuLieuLamMoi duLieuLamMoi() {
        return new DuLieuLamMoi(goiYMaNhanVien());
    }

    public String goiYMaNhanVien() {
        return dao.goiYMa();
    }

    public NhanVien taoNhanVien(String ma, String hoTen, String soDienThoai,
            String email, String matKhau, String gioiTinh, String vaiTro) {
        return new NhanVien(ma, hoTen, chuyenSoDienThoai(soDienThoai), email, matKhau,
                gioiTinh, vaiTro);
    }

    public int chuyenSoDienThoai(String soDienThoai) {
        String giaTri = chuoiAnToan(soDienThoai).trim();
        if (!giaTri.matches("0\\d{9}")) {
            return 0;
        }
        try {
            return Integer.parseInt(giaTri.substring(1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public String dinhDangSoDienThoai(int soDienThoai) {
        if (soDienThoai < 0 || soDienThoai > 999_999_999) {
            return "";
        }
        return "0" + String.format(Locale.ROOT, "%09d", soDienThoai);
    }

    public KetQuaKiemTra kiemTra(NhanVien nhanVien) {
        if (nhanVien == null) {
            return new KetQuaKiemTra(false, "Dữ liệu nhân viên không hợp lệ.", null);
        }

        String hoTen = nhanVien.getHoTen() == null ? "" : nhanVien.getHoTen().trim();
        if (hoTen.length() < 2 || hoTen.length() > 50 || !hoTen.matches("[\\p{L} ]+")) {
            return new KetQuaKiemTra(false,
                    "Họ tên từ 2–50 ký tự, chỉ chứa chữ cái (có dấu) và dấu cách.", "hoTen");
        }

        String soDienThoai = dinhDangSoDienThoai(nhanVien.getSoDienThoai());
        if (!soDienThoai.matches("0\\d{9}")) {
            return new KetQuaKiemTra(false,
                    "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.", "soDienThoai");
        }

        String email = nhanVien.getEmail() == null ? "" : nhanVien.getEmail().trim();
        if (email.length() > 30 || !email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return new KetQuaKiemTra(false,
                    "Email không hợp lệ. Ví dụ: ten@example.com.", "email");
        }

        String matKhau = nhanVien.getMatKhau() == null ? "" : nhanVien.getMatKhau();
        if (matKhau.length() < 6 || matKhau.length() > 255) {
            return new KetQuaKiemTra(false, "Mật khẩu phải có từ 6–255 ký tự.", "matKhau");
        }

        if (!"Nam".equals(nhanVien.getGioiTinh())
                && !"Nữ".equals(nhanVien.getGioiTinh())) {
            return new KetQuaKiemTra(false, "Giới tính chỉ được chọn Nam/Nữ.", "gioiTinh");
        }
        if (nhanVien.getVaiTro() == null || nhanVien.getVaiTro().isBlank()) {
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
            String[] duLieuTimKiem = {
                    nhanVien.getMaNhanVien(),
                    nhanVien.getHoTen(),
                    dinhDangSoDienThoai(nhanVien.getSoDienThoai()),
                    nhanVien.getEmail(),
                    nhanVien.getGioiTinh(),
                    hienThiVaiTro(nhanVien.getVaiTro())
            };

            if (java.util.Arrays.stream(duLieuTimKiem)
                    .map(this::chuoiAnToan)
                    .map(giaTri -> giaTri.toLowerCase(Locale.ROOT))
                    .anyMatch(giaTri -> giaTri.contains(keyword))) {
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
