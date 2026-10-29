package entity;

import java.time.LocalDateTime;

// Lớp dữ liệu biểu diễn phiếu nhập kho, gồm thuộc tính và các phương thức truy cập.
public class PhieuNhapKho {

    // maPhieuNhap: Lưu mã phiếu nhập.
    private String maPhieuNhap;
    // ngayNhap: Lưu ngày giờ nhập kho dưới dạng dữ liệu thời gian.
    private LocalDateTime ngayNhap;
    // trangThai: Cờ cho biết trạng thái.
    private boolean trangThai;      // true = da hoan tat, false = luu nhap
    // maNhanVien: Lưu mã nhân viên.
    private String maNhanVien;
    // maNhaCungCap: Lưu mã nhà cung cấp.
    private String maNhaCungCap;

    // Chi dung de hien thi (JOIN / tinh toan, khong phai cot cua bang)
    // tenNhaCungCap: Lưu tên nhà cung cấp.
    private String tenNhaCungCap;
    // soMatHang: Lưu số mặt hàng.
    private int soMatHang;
    // tongTien: Lưu tổng tiền.
    private double tongTien;

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public PhieuNhapKho() {
    }

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maPhieuNhap: mã phiếu nhập.
    // Tham số ngayNhap: ngày giờ nhập kho.
    // Tham số trangThai: trạng thái.
    // Tham số maNhanVien: mã nhân viên.
    // Tham số maNhaCungCap: mã nhà cung cấp.
    public PhieuNhapKho(String maPhieuNhap, LocalDateTime ngayNhap,
                        boolean trangThai, String maNhanVien,
                        String maNhaCungCap) {
        this.maPhieuNhap = maPhieuNhap;
        this.ngayNhap = ngayNhap;
        this.trangThai = trangThai;
        this.maNhanVien = maNhanVien;
        this.maNhaCungCap = maNhaCungCap;
    }

    // Trả về mã phiếu nhập của đối tượng.
    public String getMaPhieuNhap() { return maPhieuNhap; }
    // Gán mã phiếu nhập từ giá trị được truyền vào.
    // Tham số maPhieuNhap: mã phiếu nhập.
    public void setMaPhieuNhap(String maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }

    // Trả về ngày giờ nhập kho của đối tượng.
    public LocalDateTime getNgayNhap() { return ngayNhap; }
    // Gán ngày giờ nhập kho từ giá trị được truyền vào.
    // Tham số ngayNhap: ngày giờ nhập kho.
    public void setNgayNhap(LocalDateTime ngayNhap) { this.ngayNhap = ngayNhap; }

    // Trả về cờ trạng thái hiện tại của đối tượng.
    public boolean isTrangThai() { return trangThai; }
    // Gán trạng thái từ giá trị được truyền vào.
    // Tham số trangThai: trạng thái.
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    // Trả về mã nhân viên của đối tượng.
    public String getMaNhanVien() { return maNhanVien; }
    // Gán mã nhân viên từ giá trị được truyền vào.
    // Tham số maNhanVien: mã nhân viên.
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    // Trả về mã nhà cung cấp của đối tượng.
    public String getMaNhaCungCap() { return maNhaCungCap; }
    // Gán mã nhà cung cấp từ giá trị được truyền vào.
    // Tham số maNhaCungCap: mã nhà cung cấp.
    public void setMaNhaCungCap(String maNhaCungCap) { this.maNhaCungCap = maNhaCungCap; }

    // Trả về tên nhà cung cấp của đối tượng.
    public String getTenNhaCungCap() { return tenNhaCungCap; }
    // Gán tên nhà cung cấp từ giá trị được truyền vào.
    // Tham số tenNhaCungCap: tên nhà cung cấp.
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

    // Trả về số mặt hàng của đối tượng.
    public int getSoMatHang() { return soMatHang; }
    // Gán số mặt hàng từ giá trị được truyền vào.
    // Tham số soMatHang: số mặt hàng.
    public void setSoMatHang(int soMatHang) { this.soMatHang = soMatHang; }

    // Trả về tổng tiền của đối tượng.
    public double getTongTien() { return tongTien; }
    // Gán tổng tiền từ giá trị được truyền vào.
    // Tham số tongTien: tổng tiền.
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
}
