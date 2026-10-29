package entity;

import java.time.LocalDateTime;

// Lớp dữ liệu biểu diễn phiếu kiểm kê, gồm thuộc tính và các phương thức truy cập.
public class PhieuKiemKe {

    // maPhieuKiemKe: Lưu mã phiếu kiểm kê.
    private String maPhieuKiemKe;
    // ngayKiemKe: Lưu ngày giờ kiểm kê dưới dạng dữ liệu thời gian.
    private LocalDateTime ngayKiemKe;
    // trangThai: Cờ cho biết trạng thái.
    private boolean trangThai;      // true = da chot
    // maNhanVien: Lưu mã nhân viên.
    private String maNhanVien;
    // ngayBatDau: Lưu thời điểm bắt đầu dưới dạng dữ liệu thời gian.
    private LocalDateTime ngayBatDau;
    // ngayChot: Lưu thời điểm chốt dưới dạng dữ liệu thời gian.
    private LocalDateTime ngayChot;

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public PhieuKiemKe() {
    }

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maPhieuKiemKe: mã phiếu kiểm kê.
    // Tham số ngayKiemKe: ngày giờ kiểm kê.
    // Tham số trangThai: trạng thái.
    // Tham số maNhanVien: mã nhân viên.
    public PhieuKiemKe(String maPhieuKiemKe, LocalDateTime ngayKiemKe,
                       boolean trangThai, String maNhanVien) {
        this.maPhieuKiemKe = maPhieuKiemKe;
        this.ngayKiemKe = ngayKiemKe;
        this.trangThai = trangThai;
        this.maNhanVien = maNhanVien;
    }

    // Trả về mã phiếu kiểm kê của đối tượng.
    public String getMaPhieuKiemKe() { return maPhieuKiemKe; }
    // Gán mã phiếu kiểm kê từ giá trị được truyền vào.
    // Tham số maPhieuKiemKe: mã phiếu kiểm kê.
    public void setMaPhieuKiemKe(String maPhieuKiemKe) { this.maPhieuKiemKe = maPhieuKiemKe; }

    // Trả về ngày giờ kiểm kê của đối tượng.
    public LocalDateTime getNgayKiemKe() { return ngayKiemKe; }
    // Gán ngày giờ kiểm kê từ giá trị được truyền vào.
    // Tham số ngayKiemKe: ngày giờ kiểm kê.
    public void setNgayKiemKe(LocalDateTime ngayKiemKe) { this.ngayKiemKe = ngayKiemKe; }

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
    // Trả về thời điểm bắt đầu của đối tượng.
    public LocalDateTime getNgayBatDau() { return ngayBatDau; }
    // Gán thời điểm bắt đầu từ giá trị được truyền vào.
    // Tham số ngayBatDau: thời điểm bắt đầu.
    public void setNgayBatDau(LocalDateTime ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    // Trả về thời điểm chốt của đối tượng.
    public LocalDateTime getNgayChot() { return ngayChot; }
    // Gán thời điểm chốt từ giá trị được truyền vào.
    // Tham số ngayChot: thời điểm chốt.
    public void setNgayChot(LocalDateTime ngayChot) { this.ngayChot = ngayChot; }
}
