package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Lớp dữ liệu biểu diễn lịch hẹn, gồm thuộc tính và các phương thức truy cập.
public class LichHen {
    // maLichHen: Lưu mã lịch hẹn.
    private String maLichHen;
    // ngayGioHen: Lưu ngày giờ hẹn dưới dạng dữ liệu thời gian.
    private LocalDateTime ngayGioHen;
    // ghiChu: Lưu ghi chú.
    private String ghiChu;
    // maKhachHang: Lưu mã khách hàng.
    private String maKhachHang;
    // maNhanVien: Lưu mã nhân viên.
    private String maNhanVien;
    // maThuCung: Lưu mã thú cưng.
    private String maThuCung;

    // Các trường dữ liệu bổ sung khi JOIN bảng hiển thị lên UI
    // tenKhachHang: Lưu tên khách hàng.
    private String tenKhachHang;
    // tenThuCung: Lưu tên thú cưng.
    private String tenThuCung;
    // tenNhanVien: Lưu tên nhân viên.
    private String tenNhanVien;
    // trangThai: Lưu trạng thái.
    private String trangThai; // Bổ sung cho UI

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public LichHen() {
    }

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maLichHen: mã lịch hẹn.
    // Tham số ngayGioHen: ngày giờ hẹn.
    // Tham số ghiChu: ghi chú.
    // Tham số maKhachHang: mã khách hàng.
    // Tham số maNhanVien: mã nhân viên.
    // Tham số maThuCung: mã thú cưng.
    public LichHen(String maLichHen, LocalDateTime ngayGioHen, String ghiChu, String maKhachHang, String maNhanVien,
            String maThuCung) {
        this.maLichHen = maLichHen;
        this.ngayGioHen = ngayGioHen;
        this.ghiChu = ghiChu;
        this.maKhachHang = maKhachHang;
        this.maNhanVien = maNhanVien;
        this.maThuCung = maThuCung;
    }

    // Trả về mã lịch hẹn của đối tượng.
    public String getMaLichHen() {
        return maLichHen;
    }

    // Gán mã lịch hẹn từ giá trị được truyền vào.
    // Tham số maLichHen: mã lịch hẹn.
    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    // Trả về ngày giờ hẹn của đối tượng.
    public LocalDateTime getNgayGioHen() {
        return ngayGioHen;
    }

    // Gán ngày giờ hẹn từ giá trị được truyền vào.
    // Tham số ngayGioHen: ngày giờ hẹn.
    public void setNgayGioHen(LocalDateTime ngayGioHen) {
        this.ngayGioHen = ngayGioHen;
    }

    // Trả về ghi chú của đối tượng.
    public String getGhiChu() {
        return ghiChu;
    }

    // Gán ghi chú từ giá trị được truyền vào.
    // Tham số ghiChu: ghi chú.
    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    // Trả về mã khách hàng của đối tượng.
    public String getMaKhachHang() {
        return maKhachHang;
    }

    // Gán mã khách hàng từ giá trị được truyền vào.
    // Tham số maKhachHang: mã khách hàng.
    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    // Trả về mã nhân viên của đối tượng.
    public String getMaNhanVien() {
        return maNhanVien;
    }

    // Gán mã nhân viên từ giá trị được truyền vào.
    // Tham số maNhanVien: mã nhân viên.
    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    // Trả về mã thú cưng của đối tượng.
    public String getMaThuCung() {
        return maThuCung;
    }

    // Gán mã thú cưng từ giá trị được truyền vào.
    // Tham số maThuCung: mã thú cưng.
    public void setMaThuCung(String maThuCung) {
        this.maThuCung = maThuCung;
    }

    // Trả về tên khách hàng của đối tượng.
    public String getTenKhachHang() {
        return tenKhachHang;
    }

    // Gán tên khách hàng từ giá trị được truyền vào.
    // Tham số tenKhachHang: tên khách hàng.
    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    // Trả về tên thú cưng của đối tượng.
    public String getTenThuCung() {
        return tenThuCung;
    }

    // Gán tên thú cưng từ giá trị được truyền vào.
    // Tham số tenThuCung: tên thú cưng.
    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
    }

    // Trả về tên nhân viên của đối tượng.
    public String getTenNhanVien() {
        return tenNhanVien;
    }

    // Gán tên nhân viên từ giá trị được truyền vào.
    // Tham số tenNhanVien: tên nhân viên.
    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    // Trả về trạng thái của đối tượng.
    public String getTrangThai() {
        return trangThai;
    }

    // Gán trạng thái từ giá trị được truyền vào.
    // Tham số trangThai: trạng thái.
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    // Lấy ngày giờ hẹn đã định dạng.
    // Kết quả: chuỗi kết quả.
    public String getNgayGioHenFormat() {
        if (ngayGioHen != null) {
            // formatter: Bộ định dạng ngày/giờ theo mẫu dd/MM/yyyy h:mm a để chuyển giữa dữ liệu thời gian và chuỗi.
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a");
            return ngayGioHen.format(formatter);
        }
        return "";
    }
}