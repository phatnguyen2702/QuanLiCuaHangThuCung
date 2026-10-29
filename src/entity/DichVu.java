package entity;

import java.math.BigDecimal;

/** Dich vu Spa duoc luu trong bang DichVu. */
public class DichVu {
    // maDichVu: Lưu mã dịch vụ.
    private String maDichVu;
    // tenDichVu: Lưu tên dịch vụ.
    private String tenDichVu;
    // giaDichVu: Giá trị giá dịch vụ dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
    private BigDecimal giaDichVu;
    // moTa: Lưu mô tả.
    private String moTa;

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public DichVu() {}

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maDichVu: mã dịch vụ.
    // Tham số tenDichVu: tên dịch vụ.
    // Tham số giaDichVu: giá dịch vụ.
    // Tham số moTa: mô tả.
    public DichVu(String maDichVu, String tenDichVu, BigDecimal giaDichVu, String moTa) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.giaDichVu = giaDichVu;
        this.moTa = moTa;
    }

    // Trả về mã dịch vụ của đối tượng.
    public String getMaDichVu() { return maDichVu; }
    // Gán mã dịch vụ từ giá trị được truyền vào.
    // Tham số maDichVu: mã dịch vụ.
    public void setMaDichVu(String maDichVu) { this.maDichVu = maDichVu; }
    // Trả về tên dịch vụ của đối tượng.
    public String getTenDichVu() { return tenDichVu; }
    // Gán tên dịch vụ từ giá trị được truyền vào.
    // Tham số tenDichVu: tên dịch vụ.
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }
    // Trả về giá dịch vụ của đối tượng.
    public BigDecimal getGiaDichVu() { return giaDichVu; }
    // Gán giá dịch vụ từ giá trị được truyền vào.
    // Tham số giaDichVu: giá dịch vụ.
    public void setGiaDichVu(BigDecimal giaDichVu) { this.giaDichVu = giaDichVu; }
    // Trả về mô tả của đối tượng.
    public String getMoTa() { return moTa; }
    // Gán mô tả từ giá trị được truyền vào.
    // Tham số moTa: mô tả.
    public void setMoTa(String moTa) { this.moTa = moTa; }
}
