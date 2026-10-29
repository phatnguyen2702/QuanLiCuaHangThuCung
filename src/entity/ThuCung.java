package entity;

// Lớp dữ liệu biểu diễn thú cưng, gồm thuộc tính và các phương thức truy cập.
public class ThuCung {
    // maThuCung: Lưu mã thú cưng.
    private String maThuCung;
    // tenThuCung: Lưu tên thú cưng.
    private String tenThuCung;
    // loai: Lưu loài/loại.
    private String loai;
    // giong: Lưu giống thú cưng.
    private String giong;
    // gioiTinh: Lưu giới tính.
    private String gioiTinh;
    // canNang: Lưu cân nặng.
    private double canNang;
    // maKhachHang: Lưu mã khách hàng.
    private String maKhachHang;
    // trangThai: Lưu trạng thái.
    private String trangThai;

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public ThuCung() {
    }

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maThuCung: mã thú cưng.
    // Tham số tenThuCung: tên thú cưng.
    // Tham số loai: loài/loại.
    // Tham số giong: giống thú cưng.
    // Tham số gioiTinh: giới tính.
    // Tham số canNang: cân nặng.
    // Tham số maKhachHang: mã khách hàng.
    // Tham số trangThai: trạng thái.
    public ThuCung(String maThuCung, String tenThuCung, String loai, String giong, String gioiTinh, double canNang,
            String maKhachHang, String trangThai) {
        this.maThuCung = maThuCung;
        this.tenThuCung = tenThuCung;
        this.loai = loai;
        this.giong = giong;
        this.gioiTinh = gioiTinh;
        this.canNang = canNang;
        this.maKhachHang = maKhachHang;
        this.trangThai = trangThai;
    }

    // Getters and Setters
    // Trả về mã thú cưng của đối tượng.
    public String getMaThuCung() {
        return maThuCung;
    }

    // Gán mã thú cưng từ giá trị được truyền vào.
    // Tham số maThuCung: mã thú cưng.
    public void setMaThuCung(String maThuCung) {
        this.maThuCung = maThuCung;
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

    // Trả về loài/loại của đối tượng.
    public String getLoai() {
        return loai;
    }

    // Gán loài/loại từ giá trị được truyền vào.
    // Tham số loai: loài/loại.
    public void setLoai(String loai) {
        this.loai = loai;
    }

    // Trả về giống thú cưng của đối tượng.
    public String getGiong() {
        return giong;
    }

    // Gán giống thú cưng từ giá trị được truyền vào.
    // Tham số giong: giống thú cưng.
    public void setGiong(String giong) {
        this.giong = giong;
    }

    // Trả về giới tính của đối tượng.
    public String getGioiTinh() {
        return gioiTinh;
    }

    // Gán giới tính từ giá trị được truyền vào.
    // Tham số gioiTinh: giới tính.
    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    // Trả về cân nặng của đối tượng.
    public double getCanNang() {
        return canNang;
    }

    // Gán cân nặng từ giá trị được truyền vào.
    // Tham số canNang: cân nặng.
    public void setCanNang(double canNang) {
        this.canNang = canNang;
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

    // Trả về trạng thái của đối tượng.
    public String getTrangThai() {
        return trangThai;
    }

    // Gán trạng thái từ giá trị được truyền vào.
    // Tham số trangThai: trạng thái.
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}