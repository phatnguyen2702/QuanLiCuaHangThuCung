package dao;

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

    // Giữ các chữ ký cũ để DAO và giao diện khác không phải sửa đồng loạt.
    public static VaiTro vaiTro(String ten) {
        return control.PhanQuyenControl.vaiTro(ten);
    }

    public static boolean coQuyen(Quyen q) {
        return control.PhanQuyenControl.coQuyen(q);
    }

    public static boolean duocPhep(VaiTro r, Quyen q) {
        return control.PhanQuyenControl.duocPhep(r, q);
    }

    public static void yeuCau(Quyen q) {
        control.PhanQuyenControl.yeuCau(q);
    }

    public static boolean kiemTra(Quyen q, java.awt.Component parent) {
        return control.PhanQuyenControl.kiemTra(q, parent);
    }

    public static String tenQuyen(Quyen q) {
        return control.PhanQuyenControl.tenQuyen(q);
    }

    public static Quyen tuMaManHinh(String ma) {
        return control.PhanQuyenControl.tuMaManHinh(ma);
    }

    public static Quyen tuTenMenu(String ten) {
        return control.PhanQuyenControl.tuTenMenu(ten);
    }
}
