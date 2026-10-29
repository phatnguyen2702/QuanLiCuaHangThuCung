package entity;

// Lớp dữ liệu biểu diễn sản phẩm, gồm thuộc tính và các phương thức truy cập.
public class SanPham {

    // maSanPham: Lưu mã sản phẩm.
    private String maSanPham;
    // tenSanPham: Lưu tên sản phẩm.
    private String tenSanPham;
    // giaNhap: Lưu giá nhập.
    private double giaNhap;
    // giaBan: Lưu giá bán.
    private double giaBan;
    // maDanhMuc: Lưu mã danh mục.
    private String maDanhMuc;
    // maNhaCungCap: Lưu mã nhà cung cấp.
    private String maNhaCungCap;

    // Chi dung de hien thi (lay tu JOIN / tinh toan, khong phai cot cua bang SanPham)
    // tenDanhMuc: Lưu tên danh mục.
    private String tenDanhMuc;
    // tenNhaCungCap: Lưu tên nhà cung cấp.
    private String tenNhaCungCap;
    // tonKho: Lưu số lượng tồn kho.
    private int tonKho;

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public SanPham() {
    }

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maSanPham: mã sản phẩm.
    // Tham số tenSanPham: tên sản phẩm.
    // Tham số giaNhap: giá nhập.
    // Tham số giaBan: giá bán.
    // Tham số maDanhMuc: mã danh mục.
    // Tham số maNhaCungCap: mã nhà cung cấp.
    public SanPham(String maSanPham, String tenSanPham, double giaNhap,
                   double giaBan, String maDanhMuc, String maNhaCungCap) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.giaNhap = giaNhap;
        this.giaBan = giaBan;
        this.maDanhMuc = maDanhMuc;
        this.maNhaCungCap = maNhaCungCap;
    }

    // Trả về mã sản phẩm của đối tượng.
    public String getMaSanPham() { return maSanPham; }
    // Gán mã sản phẩm từ giá trị được truyền vào.
    // Tham số maSanPham: mã sản phẩm.
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }

    // Trả về tên sản phẩm của đối tượng.
    public String getTenSanPham() { return tenSanPham; }
    // Gán tên sản phẩm từ giá trị được truyền vào.
    // Tham số tenSanPham: tên sản phẩm.
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    // Trả về giá nhập của đối tượng.
    public double getGiaNhap() { return giaNhap; }
    // Gán giá nhập từ giá trị được truyền vào.
    // Tham số giaNhap: giá nhập.
    public void setGiaNhap(double giaNhap) { this.giaNhap = giaNhap; }

    // Trả về giá bán của đối tượng.
    public double getGiaBan() { return giaBan; }
    // Gán giá bán từ giá trị được truyền vào.
    // Tham số giaBan: giá bán.
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    // Trả về mã danh mục của đối tượng.
    public String getMaDanhMuc() { return maDanhMuc; }
    // Gán mã danh mục từ giá trị được truyền vào.
    // Tham số maDanhMuc: mã danh mục.
    public void setMaDanhMuc(String maDanhMuc) { this.maDanhMuc = maDanhMuc; }

    // Trả về mã nhà cung cấp của đối tượng.
    public String getMaNhaCungCap() { return maNhaCungCap; }
    // Gán mã nhà cung cấp từ giá trị được truyền vào.
    // Tham số maNhaCungCap: mã nhà cung cấp.
    public void setMaNhaCungCap(String maNhaCungCap) { this.maNhaCungCap = maNhaCungCap; }

    // Trả về tên danh mục của đối tượng.
    public String getTenDanhMuc() { return tenDanhMuc; }
    // Gán tên danh mục từ giá trị được truyền vào.
    // Tham số tenDanhMuc: tên danh mục.
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }

    // Trả về tên nhà cung cấp của đối tượng.
    public String getTenNhaCungCap() { return tenNhaCungCap; }
    // Gán tên nhà cung cấp từ giá trị được truyền vào.
    // Tham số tenNhaCungCap: tên nhà cung cấp.
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

    // Trả về số lượng tồn kho của đối tượng.
    public int getTonKho() { return tonKho; }
    // Gán số lượng tồn kho từ giá trị được truyền vào.
    // Tham số tonKho: số lượng tồn kho.
    public void setTonKho(int tonKho) { this.tonKho = tonKho; }

    // Trả về chuỗi đại diện của đối tượng để hiển thị.
    @Override
    public String toString() {
        return maSanPham + " - " + tenSanPham;
    }
}
