package entity;

import java.time.LocalDate;

// Lớp dữ liệu biểu diễn chi tiết phiếu nhập, gồm thuộc tính và các phương thức truy cập.
public class ChiTietPhieuNhap {

    // maPhieuNhap: Lưu mã phiếu nhập.
    private String maPhieuNhap;
    // maSanPham: Lưu mã sản phẩm.
    private String maSanPham;
    // soLuongNhap: Lưu số lượng nhập.
    private int soLuongNhap;
    // donGiaNhap: Lưu đơn giá nhập.
    private double donGiaNhap;
    // trangThai: Cờ cho biết trạng thái.
    private boolean trangThai;
    // hanSuDung: Lưu hạn sử dụng dưới dạng dữ liệu thời gian.
    private LocalDate hanSuDung;

    // Chi dung de hien thi
    // tenSanPham: Lưu tên sản phẩm.
    private String tenSanPham;

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public ChiTietPhieuNhap() {
    }

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maPhieuNhap: mã phiếu nhập.
    // Tham số maSanPham: mã sản phẩm.
    // Tham số soLuongNhap: số lượng nhập.
    // Tham số donGiaNhap: đơn giá nhập.
    // Tham số trangThai: trạng thái.
    public ChiTietPhieuNhap(String maPhieuNhap, String maSanPham,
                            int soLuongNhap, double donGiaNhap,
                            boolean trangThai) {
        this.maPhieuNhap = maPhieuNhap;
        this.maSanPham = maSanPham;
        this.soLuongNhap = soLuongNhap;
        this.donGiaNhap = donGiaNhap;
        this.trangThai = trangThai;
    }

    // Trả về mã phiếu nhập của đối tượng.
    public String getMaPhieuNhap() { return maPhieuNhap; }
    // Gán mã phiếu nhập từ giá trị được truyền vào.
    // Tham số maPhieuNhap: mã phiếu nhập.
    public void setMaPhieuNhap(String maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }

    // Trả về mã sản phẩm của đối tượng.
    public String getMaSanPham() { return maSanPham; }
    // Gán mã sản phẩm từ giá trị được truyền vào.
    // Tham số maSanPham: mã sản phẩm.
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }

    // Trả về số lượng nhập của đối tượng.
    public int getSoLuongNhap() { return soLuongNhap; }
    // Gán số lượng nhập từ giá trị được truyền vào.
    // Tham số soLuongNhap: số lượng nhập.
    public void setSoLuongNhap(int soLuongNhap) { this.soLuongNhap = soLuongNhap; }

    // Trả về đơn giá nhập của đối tượng.
    public double getDonGiaNhap() { return donGiaNhap; }
    // Gán đơn giá nhập từ giá trị được truyền vào.
    // Tham số donGiaNhap: đơn giá nhập.
    public void setDonGiaNhap(double donGiaNhap) { this.donGiaNhap = donGiaNhap; }

    // Trả về hạn sử dụng của đối tượng.
    public LocalDate getHanSuDung() { return hanSuDung; }
    // Gán hạn sử dụng từ giá trị được truyền vào.
    // Tham số hanSuDung: hạn sử dụng.
    public void setHanSuDung(LocalDate hanSuDung) { this.hanSuDung = hanSuDung; }

    // Trả về cờ trạng thái hiện tại của đối tượng.
    public boolean isTrangThai() { return trangThai; }
    // Gán trạng thái từ giá trị được truyền vào.
    // Tham số trangThai: trạng thái.
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    // Trả về tên sản phẩm của đối tượng.
    public String getTenSanPham() { return tenSanPham; }
    // Gán tên sản phẩm từ giá trị được truyền vào.
    // Tham số tenSanPham: tên sản phẩm.
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    // Tính thành tiền của dòng dữ liệu.
    // Kết quả: giá trị số thực.
    public double getThanhTien() {
        return soLuongNhap * donGiaNhap;
    }
}
