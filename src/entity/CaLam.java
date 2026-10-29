package entity;

// Lớp dữ liệu biểu diễn ca làm, gồm thuộc tính và các phương thức truy cập.
public class CaLam {

    // maCa: Lưu mã ca.
    private String maCa;
    // tenCa: Lưu tên ca.
    private String tenCa;
    // gioBatDau: Lưu giờ bắt đầu.
    private String gioBatDau;
    // gioKetThuc: Lưu giờ kết thúc.
    private String gioKetThuc;

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public CaLam() {
    }

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maCa: mã ca.
    // Tham số tenCa: tên ca.
    // Tham số gioBatDau: giờ bắt đầu.
    // Tham số gioKetThuc: giờ kết thúc.
    public CaLam(String maCa, String tenCa,
            String gioBatDau, String gioKetThuc) {
        this.maCa = maCa;
        this.tenCa = tenCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
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

    // Trả về chuỗi đại diện của đối tượng để hiển thị.
    @Override
    public String toString() {
        return maCa + " - " + tenCa;
    }
}