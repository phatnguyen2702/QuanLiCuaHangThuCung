package entity;

// Lớp dữ liệu biểu diễn nhà cung cấp, gồm thuộc tính và các phương thức truy cập.
public class NhaCungCap {

    // maNhaCungCap: Lưu mã nhà cung cấp.
    private String maNhaCungCap;
    // tenNhaCungCap: Lưu tên nhà cung cấp.
    private String tenNhaCungCap;
    // soDienThoai: Lưu số điện thoại.
    private String soDienThoai;
    // email: Lưu email.
    private String email;
    // diaChi: Lưu địa chỉ.
    private String diaChi;

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public NhaCungCap() {
    }

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maNhaCungCap: mã nhà cung cấp.
    // Tham số tenNhaCungCap: tên nhà cung cấp.
    // Tham số soDienThoai: số điện thoại.
    // Tham số email: email.
    // Tham số diaChi: địa chỉ.
    public NhaCungCap(String maNhaCungCap, String tenNhaCungCap,
                      String soDienThoai, String email, String diaChi) {
        this.maNhaCungCap = maNhaCungCap;
        this.tenNhaCungCap = tenNhaCungCap;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
    }

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

    // Trả về số điện thoại của đối tượng.
    public String getSoDienThoai() { return soDienThoai; }
    // Gán số điện thoại từ giá trị được truyền vào.
    // Tham số soDienThoai: số điện thoại.
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    // Trả về email của đối tượng.
    public String getEmail() { return email; }
    // Gán email từ giá trị được truyền vào.
    // Tham số email: email.
    public void setEmail(String email) { this.email = email; }

    // Trả về địa chỉ của đối tượng.
    public String getDiaChi() { return diaChi; }
    // Gán địa chỉ từ giá trị được truyền vào.
    // Tham số diaChi: địa chỉ.
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    // Trả về chuỗi đại diện của đối tượng để hiển thị.
    @Override
    public String toString() {
        return tenNhaCungCap;
    }
}
