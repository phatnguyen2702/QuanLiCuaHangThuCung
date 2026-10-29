package UI;

import java.text.Normalizer;
import java.util.EnumSet;
import java.util.Locale;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Phân quyền theo vai trò hiện có ở NhanVien.vaiTro.
 * Khai báo tập trung; mọi vai trò không nhận diện được sẽ bị từ chối.
 * Lưu ý: lớp này bảo vệ thao tác trong ứng dụng Java, KHÔNG thay thế quyền SQL
 * Server.
 */
public final class PhanQuyen {
    // Tập giá trị cố định cho vai trò.
    public enum VaiTro {
        QUAN_LY, THU_NGAN, NHAN_VIEN_KHO, NHAN_VIEN_SPA, KHONG_HOP_LE
    }

    // Tập giá trị cố định cho quyền.
    public enum Quyen {
        TAI_KHOAN, PHAN_QUYEN, CA_LAM, PHAN_CA,
        DANH_MUC_SAN_PHAM, SAN_PHAM, KHACH_HANG, THU_CUNG, DICH_VU, VOUCHER,
        NHAP_KHO, KIEM_KE, TRA_CUU_NHAP_KHO, TRA_CUU_KIEM_KE, CANH_BAO_HAN_SU_DUNG,
        LICH_HEN_SPA, PHIEU_DICH_VU, THANH_TOAN, TRA_CUU_HOA_DON,
        GOI_Y_THUC_AN, THONG_KE_DOANH_THU
    }

    // Ẩn hàm khởi tạo vì lớp chỉ cung cấp các hàm tiện ích dùng chung.
    private PhanQuyen() {
    }

    // Chuẩn hóa tên vai trò hoặc quyền để so sánh.
    // Tham số s: chuỗi đầu vào.
    // Kết quả: chuỗi kết quả.
    private static String chuan(String s) {
        if (s == null)
            return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    // Chuyển tên vai trò thành giá trị enum VaiTro.
    // Tham số ten: tên.
    // Kết quả: đối tượng/giá trị kiểu VaiTro.
    public static VaiTro vaiTro(String ten) {
        return switch (chuan(ten)) {
            case "quan ly" -> VaiTro.QUAN_LY;
            case "thu ngan" -> VaiTro.THU_NGAN;
            case "nhan vien kho" -> VaiTro.NHAN_VIEN_KHO;
            case "nhan vien spa", "tho spa" -> VaiTro.NHAN_VIEN_SPA;
            default -> VaiTro.KHONG_HOP_LE;
        };
    }

    // Kiểm tra phiên làm việc hiện tại có quyền được yêu cầu hay không.
    // Tham số q: q.
    // Kết quả: true/false theo điều kiện xử lý.
    public static boolean coQuyen(Quyen q) {
        if (q == null || UI.PhienLamViec.maNV == null || UI.PhienLamViec.maNV.isBlank())
            return false;
        return duocPhep(vaiTro(UI.PhienLamViec.vaiTro), q);
    }

    /** Dùng để trình bày ma trận quyền mà không phải thay đổi phiên đăng nhập. */
    public static boolean duocPhep(VaiTro r, Quyen q) {
        if (r == null || q == null)
            return false;
        if (r == VaiTro.QUAN_LY)
            return true;
        return switch (r) {
            case THU_NGAN -> EnumSet.of(
                    Quyen.KHACH_HANG, Quyen.THU_CUNG, Quyen.LICH_HEN_SPA,
                    Quyen.PHIEU_DICH_VU, Quyen.THANH_TOAN, Quyen.TRA_CUU_HOA_DON,
                    Quyen.GOI_Y_THUC_AN).contains(q);
            case NHAN_VIEN_SPA -> EnumSet.of(
                    Quyen.KHACH_HANG, Quyen.THU_CUNG, Quyen.LICH_HEN_SPA,
                    Quyen.PHIEU_DICH_VU, Quyen.GOI_Y_THUC_AN).contains(q);
            case NHAN_VIEN_KHO -> EnumSet.of(
                    Quyen.DANH_MUC_SAN_PHAM, Quyen.SAN_PHAM,
                    Quyen.NHAP_KHO, Quyen.KIEM_KE,
                    Quyen.TRA_CUU_NHAP_KHO, Quyen.TRA_CUU_KIEM_KE,
                    Quyen.CANH_BAO_HAN_SU_DUNG, Quyen.GOI_Y_THUC_AN).contains(q);
            default -> false;
        };
    }

    /** Chặn ở tầng xử lý dữ liệu (DAO); không chỉ ẩn nút trên giao diện. */
    public static void yeuCau(Quyen q) {
        if (!coQuyen(q))
            throw new SecurityException(
                    "Tài khoản không có quyền thực hiện: " + q + ". Vui lòng liên hệ quản lý.");
        // Xác minh lại quyền trong SQL mỗi khi ghi dữ liệu: tránh phiên cũ vẫn có quyền
        // sau khi quản lý đổi vai trò hoặc xóa nhân viên.
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT vaiTro FROM NhanVien WHERE maNhanVien = ?";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection conn = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UI.PhienLamViec.maNV);
            // Thực thi truy vấn và tự đóng tập kết quả sau khi đọc xong.
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || vaiTro(rs.getString(1)) != vaiTro(UI.PhienLamViec.vaiTro)) {
                    throw new SecurityException("Vai trò đã thay đổi hoặc tài khoản không tồn tại. "
                            + "Vui lòng đăng xuất và đăng nhập lại.");
                }
            }
        } catch (SQLException ex) {
            throw new SecurityException("Không thể xác minh quyền trên SQL Server; thao tác bị từ chối.", ex);
        }
    }

    // Kiểm tra quyền và thông báo trên giao diện khi không được phép.
    // Tham số q: q.
    // Tham số parent: thành phần cha.
    // Kết quả: true/false theo điều kiện xử lý.
    public static boolean kiemTra(Quyen q, Component parent) {
        if (coQuyen(q))
            return true;
        JOptionPane.showMessageDialog(parent,
                "Vai trò hiện tại không được phép sử dụng chức năng này.",
                "Không có quyền truy cập", JOptionPane.WARNING_MESSAGE);
        return false;
    }

    // Lấy tên mô tả của quyền để trình bày trên giao diện.
    // Tham số q: q.
    // Kết quả: chuỗi kết quả.
    public static String tenQuyen(Quyen q) {
        return switch (q) {
            case TAI_KHOAN -> "Quản lý tài khoản nhân viên";
            case PHAN_QUYEN -> "Xem ma trận phân quyền";
            case CA_LAM -> "Quản lý ca làm";
            case PHAN_CA -> "Phân ca nhân viên";
            case DANH_MUC_SAN_PHAM -> "Danh mục sản phẩm";
            case SAN_PHAM -> "Quản lý sản phẩm";
            case KHACH_HANG -> "Quản lý khách hàng";
            case THU_CUNG -> "Hồ sơ thú cưng";
            case DICH_VU -> "Quản lý dịch vụ Spa";
            case VOUCHER -> "Quản lý Voucher";
            case NHAP_KHO -> "Nhập kho";
            case KIEM_KE -> "Kiểm kê kho";
            case TRA_CUU_NHAP_KHO -> "Tra cứu nhập kho";
            case TRA_CUU_KIEM_KE -> "Tra cứu kiểm kê kho";
            case CANH_BAO_HAN_SU_DUNG -> "Cảnh báo hàng sắp hết hạn";
            case LICH_HEN_SPA -> "Quản lý lịch hẹn Spa";
            case PHIEU_DICH_VU -> "Quản lý phiếu dịch vụ";
            case THANH_TOAN -> "Thanh toán tại quầy";
            case TRA_CUU_HOA_DON -> "Tra cứu hóa đơn";
            case GOI_Y_THUC_AN -> "Gợi ý thức ăn";
            case THONG_KE_DOANH_THU -> "Thống kê doanh thu";
        };
    }

    // Ánh xạ mã màn hình sang quyền tương ứng.
    // Tham số ma: mã.
    // Kết quả: đối tượng/giá trị kiểu Quyen.
    public static Quyen tuMaManHinh(String ma) {
        if (ma == null)
            return null;
        return switch (ma) {
            case "taikhoan" -> Quyen.TAI_KHOAN;
            case "phanquyen" -> Quyen.PHAN_QUYEN;
            case "calam" -> Quyen.CA_LAM;
            case "phanca" -> Quyen.PHAN_CA;
            case "danhmuc" -> Quyen.DANH_MUC_SAN_PHAM;
            case "sanpham" -> Quyen.SAN_PHAM;
            case "khachhang" -> Quyen.KHACH_HANG;
            case "thucung" -> Quyen.THU_CUNG;
            case "dichvu" -> Quyen.DICH_VU;
            case "voucher" -> Quyen.VOUCHER;
            case "nhapkho" -> Quyen.NHAP_KHO;
            case "kiemke" -> Quyen.KIEM_KE;
            case "tracuukho" -> Quyen.TRA_CUU_NHAP_KHO;
            case "tracuukkkho" -> Quyen.TRA_CUU_KIEM_KE;
            case "canhbaohansudung" -> Quyen.CANH_BAO_HAN_SU_DUNG;
            case "lichhen" -> Quyen.LICH_HEN_SPA;
            case "phieudv" -> Quyen.PHIEU_DICH_VU;
            case "thanhtoan" -> Quyen.THANH_TOAN;
            case "hoadon" -> Quyen.TRA_CUU_HOA_DON;
            case "goiythucan" -> Quyen.GOI_Y_THUC_AN;
            case "thongkedoanhthu" -> Quyen.THONG_KE_DOANH_THU;
            default -> null;
        };
    }

    // Ánh xạ tên menu sang quyền tương ứng.
    // Tham số ten: tên.
    // Kết quả: đối tượng/giá trị kiểu Quyen.
    public static Quyen tuTenMenu(String ten) {
        if (ten == null)
            return null;
        return switch (ten) {
            case "Quản lý tài khoản" -> Quyen.TAI_KHOAN;
            case "Ma trận phân quyền" -> Quyen.PHAN_QUYEN;
            case "Quản lý ca làm" -> Quyen.CA_LAM;
            case "Phân ca nhân viên" -> Quyen.PHAN_CA;
            case "Danh mục sản phẩm" -> Quyen.DANH_MUC_SAN_PHAM;
            case "Sản phẩm" -> Quyen.SAN_PHAM;
            case "Khách hàng" -> Quyen.KHACH_HANG;
            case "Thú cưng" -> Quyen.THU_CUNG;
            case "Dịch vụ" -> Quyen.DICH_VU;
            case "Mã giảm giá" -> Quyen.VOUCHER;
            case "Nhập kho" -> Quyen.NHAP_KHO;
            case "Kiểm kê kho" -> Quyen.KIEM_KE;
            case "Tra cứu nhập kho" -> Quyen.TRA_CUU_NHAP_KHO;
            case "Tra cứu kiểm kê kho" -> Quyen.TRA_CUU_KIEM_KE;
            case "Cảnh báo sản phẩm sắp hết hạn" -> Quyen.CANH_BAO_HAN_SU_DUNG;
            case "Lịch hẹn Spa" -> Quyen.LICH_HEN_SPA;
            case "Phiếu dịch vụ" -> Quyen.PHIEU_DICH_VU;
            case "Thanh toán hóa đơn" -> Quyen.THANH_TOAN;
            case "Tra cứu lịch sử hóa đơn" -> Quyen.TRA_CUU_HOA_DON;
            case "Gợi ý thức ăn theo loại thú cưng" -> Quyen.GOI_Y_THUC_AN;
            case "Thống kê doanh thu" -> Quyen.THONG_KE_DOANH_THU;
            default -> null;
        };
    }
}
