package entity;

import java.math.BigDecimal;

// Lớp dữ liệu biểu diễn thanh toán, gồm thuộc tính và các phương thức truy cập.
public class ThanhToan {

    // maSanPham: Lưu mã sản phẩm.
    private final String maSanPham;
    // maDichVu: Lưu mã dịch vụ.
    private final String maDichVu;
    // maThuCung: Lưu mã thú cưng.
    private final String maThuCung;
    // tenMatHang: Lưu tên mặt hàng.
    private final String tenMatHang;
    // donGia: Giá trị đơn giá dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
    private final BigDecimal donGia;
    // soLuong: Lưu số lượng.
    private int soLuong;

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maSanPham: mã sản phẩm.
    // Tham số maDichVu: mã dịch vụ.
    // Tham số maThuCung: mã thú cưng.
    // Tham số tenMatHang: tên mặt hàng.
    // Tham số donGia: đơn giá.
    // Tham số soLuong: số lượng.
    private ThanhToan(
            String maSanPham,
            String maDichVu,
            String maThuCung,
            String tenMatHang,
            BigDecimal donGia,
            int soLuong) {
        if ((maSanPham == null) == (maDichVu == null)) {
            throw new IllegalArgumentException("Chi tiet phai la san pham hoac dich vu.");
        }
        if (tenMatHang == null || donGia == null || donGia.signum() < 0 || soLuong < 1) {
            throw new IllegalArgumentException("Thong tin thanh toan khong hop le.");
        }
        this.maSanPham = maSanPham;
        this.maDichVu = maDichVu;
        this.maThuCung = maThuCung;
        this.tenMatHang = tenMatHang;
        this.donGia = donGia;
        this.soLuong = soLuong;
    }

    // Tạo dòng thanh toán dành cho sản phẩm.
    // Tham số ma: mã.
    // Tham số ten: tên.
    // Tham số donGia: đơn giá.
    // Kết quả: đối tượng/giá trị kiểu ThanhToan.
    public static ThanhToan sanPham(String ma, String ten, BigDecimal donGia) {
        return new ThanhToan(ma, null, null, ten, donGia, 1);
    }

    // Tạo dòng thanh toán dành cho dịch vụ.
    // Tham số ma: mã.
    // Tham số ten: tên.
    // Tham số donGia: đơn giá.
    // Kết quả: đối tượng/giá trị kiểu ThanhToan.
    public static ThanhToan dichVu(String ma, String ten, BigDecimal donGia) {
        return dichVu(ma, ten, donGia, null);
    }

    // Tạo dòng thanh toán dành cho dịch vụ.
    // Tham số ma: mã.
    // Tham số ten: tên.
    // Tham số donGia: đơn giá.
    // Tham số maThuCung: mã thú cưng.
    // Kết quả: đối tượng/giá trị kiểu ThanhToan.
    public static ThanhToan dichVu(
            String ma, String ten, BigDecimal donGia, String maThuCung) {
        return new ThanhToan(null, ma, maThuCung, ten, donGia, 1);
    }

    // Trả về mã sản phẩm của đối tượng.
    public String getMaSanPham() {
        return maSanPham;
    }

    // Trả về mã dịch vụ của đối tượng.
    public String getMaDichVu() {
        return maDichVu;
    }

    // Trả về mã thú cưng của đối tượng.
    public String getMaThuCung() {
        return maThuCung;
    }

    // Lấy mã mặt hàng.
    // Kết quả: chuỗi kết quả.
    public String getMaHang() {
        return maSanPham == null ? maDichVu : maSanPham;
    }

    // Trả về tên mặt hàng của đối tượng.
    public String getTenMatHang() {
        return tenMatHang;
    }

    // Trả về đơn giá của đối tượng.
    public BigDecimal getDonGia() {
        return donGia;
    }

    // Trả về số lượng của đối tượng.
    public int getSoLuong() {
        return soLuong;
    }

    // Gán số lượng từ giá trị được truyền vào.
    // Tham số soLuong: số lượng.
    public void setSoLuong(int soLuong) {
        if (soLuong < 1) {
            throw new IllegalArgumentException("So luong phai lon hon 0.");
        }
        this.soLuong = soLuong;
    }

    // Tính thành tiền của dòng dữ liệu.
    // Kết quả: đối tượng/giá trị kiểu BigDecimal.
    public BigDecimal getThanhTien() {
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    // Nhóm dữ liệu hoặc chức năng khách hàng dùng trong ThanhToan.
    public static class KhachHang {
        // maKhachHang: Lưu mã khách hàng.
        private final String maKhachHang;
        // hoTen: Lưu họ tên.
        private final String hoTen;
        // soDienThoai: Lưu số điện thoại.
        private final String soDienThoai;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số maKhachHang: mã khách hàng.
        // Tham số hoTen: họ tên.
        // Tham số soDienThoai: số điện thoại.
        public KhachHang(String maKhachHang, String hoTen, String soDienThoai) {
            this.maKhachHang = maKhachHang;
            this.hoTen = hoTen;
            this.soDienThoai = soDienThoai;
        }

        // Trả về mã khách hàng của đối tượng.
        public String getMaKhachHang() {
            return maKhachHang;
        }

        // Trả về họ tên của đối tượng.
        public String getHoTen() {
            return hoTen;
        }

        // Trả về số điện thoại của đối tượng.
        public String getSoDienThoai() {
            return soDienThoai;
        }
    }

    // Nhóm dữ liệu hoặc chức năng voucher dùng trong ThanhToan.
    public static class Voucher {
        // maVoucher: Lưu mã voucher.
        private final String maVoucher;
        // tenVoucher: Lưu tên voucher.
        private final String tenVoucher;
        // phanTramGiam: Giá trị phần trăm giảm giá dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        private final BigDecimal phanTramGiam;
        // dieuKien: Lưu điều kiện áp dụng voucher.
        private final String dieuKien;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số maVoucher: mã voucher.
        // Tham số tenVoucher: tên voucher.
        // Tham số phanTramGiam: phần trăm giảm giá.
        // Tham số dieuKien: điều kiện áp dụng voucher.
        public Voucher(
                String maVoucher,
                String tenVoucher,
                BigDecimal phanTramGiam,
                String dieuKien) {
            this.maVoucher = maVoucher;
            this.tenVoucher = tenVoucher;
            this.phanTramGiam = phanTramGiam;
            this.dieuKien = dieuKien;
        }

        // Trả về mã voucher của đối tượng.
        public String getMaVoucher() {
            return maVoucher;
        }

        // Trả về tên voucher của đối tượng.
        public String getTenVoucher() {
            return tenVoucher;
        }

        // Trả về phần trăm giảm giá của đối tượng.
        public BigDecimal getPhanTramGiam() {
            return phanTramGiam;
        }

        // Trả về điều kiện áp dụng voucher của đối tượng.
        public String getDieuKien() {
            return dieuKien;
        }
    }

    // Nhóm dữ liệu hoặc chức năng phương thức thanh toán dùng trong ThanhToan.
    public static class PhuongThuc {
        // maPhuongThuc: Lưu mã phương thức thanh toán.
        private final String maPhuongThuc;
        // tenPhuongThuc: Lưu tên phương thức thanh toán.
        private final String tenPhuongThuc;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số maPhuongThuc: mã phương thức thanh toán.
        // Tham số tenPhuongThuc: tên phương thức thanh toán.
        public PhuongThuc(String maPhuongThuc, String tenPhuongThuc) {
            this.maPhuongThuc = maPhuongThuc;
            this.tenPhuongThuc = tenPhuongThuc;
        }

        // Trả về mã phương thức thanh toán của đối tượng.
        public String getMaPhuongThuc() {
            return maPhuongThuc;
        }

        // Trả về tên phương thức thanh toán của đối tượng.
        public String getTenPhuongThuc() {
            return tenPhuongThuc;
        }

        // Trả về chuỗi đại diện của đối tượng để hiển thị.
        @Override
        public String toString() {
            return tenPhuongThuc;
        }
    }
}
