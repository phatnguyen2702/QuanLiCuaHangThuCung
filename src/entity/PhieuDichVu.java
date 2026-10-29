package entity;

// Lớp dữ liệu biểu diễn phiếu dịch vụ, gồm thuộc tính và các phương thức truy cập.
public class PhieuDichVu {
    // maPhieuDichVu: Lưu mã phiếu dịch vụ.
    private String maPhieuDichVu;
    // trangThai: Lưu trạng thái.
    private String trangThai;
    // maKhachHang: Lưu mã khách hàng.
    private String maKhachHang;
    // maThuCung: Lưu mã thú cưng.
    private String maThuCung;
    // maNhanVien: Lưu mã nhân viên.
    private String maNhanVien;
    // maLichHen: Lưu mã lịch hẹn.
    private String maLichHen;

    // Các trường JOIN để hiển thị lên bảng
    // tenKhachHang: Lưu tên khách hàng.
    private String tenKhachHang;
    // tenThuCung: Lưu tên thú cưng.
    private String tenThuCung;
    // tenNhanVien: Lưu tên nhân viên.
    private String tenNhanVien;
    // cacDichVu: Lưu các dịch vụ.
    private String cacDichVu;

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public PhieuDichVu() {
    }

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maPhieuDichVu: mã phiếu dịch vụ.
    // Tham số trangThai: trạng thái.
    // Tham số maKhachHang: mã khách hàng.
    // Tham số maThuCung: mã thú cưng.
    // Tham số maNhanVien: mã nhân viên.
    // Tham số maLichHen: mã lịch hẹn.
    public PhieuDichVu(String maPhieuDichVu, String trangThai, String maKhachHang, String maThuCung, String maNhanVien,
            String maLichHen) {
        this.maPhieuDichVu = maPhieuDichVu;
        this.trangThai = trangThai;
        this.maKhachHang = maKhachHang;
        this.maThuCung = maThuCung;
        this.maNhanVien = maNhanVien;
        this.maLichHen = maLichHen;
    }

    // Getters and Setters
    // Trả về mã phiếu dịch vụ của đối tượng.
    public String getMaPhieuDichVu() {
        return maPhieuDichVu;
    }

    // Gán mã phiếu dịch vụ từ giá trị được truyền vào.
    // Tham số maPhieuDichVu: mã phiếu dịch vụ.
    public void setMaPhieuDichVu(String maPhieuDichVu) {
        this.maPhieuDichVu = maPhieuDichVu;
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

    // Trả về mã khách hàng của đối tượng.
    public String getMaKhachHang() {
        return maKhachHang;
    }

    // Gán mã khách hàng từ giá trị được truyền vào.
    // Tham số maKhachHang: mã khách hàng.
    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
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

    // Trả về mã nhân viên của đối tượng.
    public String getMaNhanVien() {
        return maNhanVien;
    }

    // Gán mã nhân viên từ giá trị được truyền vào.
    // Tham số maNhanVien: mã nhân viên.
    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
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

    // Trả về các dịch vụ của đối tượng.
    public String getCacDichVu() {
        return cacDichVu;
    }

    // Gán các dịch vụ từ giá trị được truyền vào.
    // Tham số cacDichVu: các dịch vụ.
    public void setCacDichVu(String cacDichVu) {
        this.cacDichVu = cacDichVu;
    }
}