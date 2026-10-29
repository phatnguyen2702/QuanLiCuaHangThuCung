package entity;

import java.time.LocalDate;

// Lớp dữ liệu biểu diễn lịch phân ca, gồm thuộc tính và các phương thức truy cập.
public class LichPhanCa {

    // maPhanCa: Lưu mã phân ca.
    private String maPhanCa;
    // ngayLamViec: Lưu ngày làm việc dưới dạng dữ liệu thời gian.
    private LocalDate ngayLamViec;
    // maNhanVien: Lưu mã nhân viên.
    private String maNhanVien;
    // maCa: Lưu mã ca.
    private String maCa;

    // Du lieu hien thi them tu NhanVien va CaLam
    // hoTen: Lưu họ tên.
    private String hoTen;
    // tenCa: Lưu tên ca.
    private String tenCa;
    // gioBatDau: Lưu giờ bắt đầu.
    private String gioBatDau;
    // gioKetThuc: Lưu giờ kết thúc.
    private String gioKetThuc;

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public LichPhanCa() {
    }

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maPhanCa: mã phân ca.
    // Tham số ngayLamViec: ngày làm việc.
    // Tham số maNhanVien: mã nhân viên.
    // Tham số maCa: mã ca.
    public LichPhanCa(String maPhanCa, LocalDate ngayLamViec,
            String maNhanVien, String maCa) {
        this.maPhanCa = maPhanCa;
        this.ngayLamViec = ngayLamViec;
        this.maNhanVien = maNhanVien;
        this.maCa = maCa;
    }

    // Trả về mã phân ca của đối tượng.
    public String getMaPhanCa() {
        return maPhanCa;
    }

    // Gán mã phân ca từ giá trị được truyền vào.
    // Tham số maPhanCa: mã phân ca.
    public void setMaPhanCa(String maPhanCa) {
        this.maPhanCa = maPhanCa;
    }

    // Trả về ngày làm việc của đối tượng.
    public LocalDate getNgayLamViec() {
        return ngayLamViec;
    }

    // Gán ngày làm việc từ giá trị được truyền vào.
    // Tham số ngayLamViec: ngày làm việc.
    public void setNgayLamViec(LocalDate ngayLamViec) {
        this.ngayLamViec = ngayLamViec;
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

    // Trả về mã ca của đối tượng.
    public String getMaCa() {
        return maCa;
    }

    // Gán mã ca từ giá trị được truyền vào.
    // Tham số maCa: mã ca.
    public void setMaCa(String maCa) {
        this.maCa = maCa;
    }

    // Trả về họ tên của đối tượng.
    public String getHoTen() {
        return hoTen;
    }

    // Gán họ tên từ giá trị được truyền vào.
    // Tham số hoTen: họ tên.
    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    // Trả về tên ca của đối tượng.
    public String getTenCa() {
        return tenCa;
    }

    // Gán tên ca từ giá trị được truyền vào.
    // Tham số tenCa: tên ca.
    public void setTenCa(String tenCa) {
        this.tenCa = tenCa;
    }

    // Trả về giờ bắt đầu của đối tượng.
    public String getGioBatDau() {
        return gioBatDau;
    }

    // Gán giờ bắt đầu từ giá trị được truyền vào.
    // Tham số gioBatDau: giờ bắt đầu.
    public void setGioBatDau(String gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    // Trả về giờ kết thúc của đối tượng.
    public String getGioKetThuc() {
        return gioKetThuc;
    }

    // Gán giờ kết thúc từ giá trị được truyền vào.
    // Tham số gioKetThuc: giờ kết thúc.
    public void setGioKetThuc(String gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    // Trả về thông tin ca làm của đối tượng.
    public String getThongTinCa() {

        if (tenCa == null) {
            return maCa;
        }

        if (gioBatDau != null && gioKetThuc != null) {
            return maCa + " - " + tenCa
                    + ": " + gioBatDau
                    + "-" + gioKetThuc;
        }

        return maCa + " - " + tenCa;
    }

    // Trả về chuỗi đại diện của đối tượng để hiển thị.
    @Override
    public String toString() {
        return maPhanCa;
    }
}