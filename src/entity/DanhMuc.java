package entity;

// Lớp dữ liệu biểu diễn danh mục, gồm thuộc tính và các phương thức truy cập.
public class DanhMuc {

    // maDanhMuc: Lưu mã danh mục.
    private String maDanhMuc;
    // tenDanhMuc: Lưu tên danh mục.
    private String tenDanhMuc;
    // moTa: Lưu mô tả.
    private String moTa;

    // Chi dung de hien thi (khong co cot trong bang DanhMuc)
    // soSanPham: Lưu số sản phẩm.
    private int soSanPham;
    // tongTon: Lưu tổng tồn.
    private int tongTon;

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public DanhMuc() {
    }

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maDanhMuc: mã danh mục.
    // Tham số tenDanhMuc: tên danh mục.
    // Tham số moTa: mô tả.
    public DanhMuc(String maDanhMuc, String tenDanhMuc, String moTa) {
        this.maDanhMuc = maDanhMuc;
        this.tenDanhMuc = tenDanhMuc;
        this.moTa = moTa;
    }

    // Trả về mã danh mục của đối tượng.
    public String getMaDanhMuc() { return maDanhMuc; }
    // Gán mã danh mục từ giá trị được truyền vào.
    // Tham số maDanhMuc: mã danh mục.
    public void setMaDanhMuc(String maDanhMuc) { this.maDanhMuc = maDanhMuc; }

    // Trả về tên danh mục của đối tượng.
    public String getTenDanhMuc() { return tenDanhMuc; }
    // Gán tên danh mục từ giá trị được truyền vào.
    // Tham số tenDanhMuc: tên danh mục.
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }

    // Trả về mô tả của đối tượng.
    public String getMoTa() { return moTa; }
    // Gán mô tả từ giá trị được truyền vào.
    // Tham số moTa: mô tả.
    public void setMoTa(String moTa) { this.moTa = moTa; }

    // Trả về tổng tồn của đối tượng.
    public int getTongTon() { return tongTon; }
    // Gán tổng tồn từ giá trị được truyền vào.
    // Tham số tongTon: tổng tồn.
    public void setTongTon(int tongTon) { this.tongTon = tongTon; }

    // Trả về số sản phẩm của đối tượng.
    public int getSoSanPham() { return soSanPham; }
    // Gán số sản phẩm từ giá trị được truyền vào.
    // Tham số soSanPham: số sản phẩm.
    public void setSoSanPham(int soSanPham) { this.soSanPham = soSanPham; }

    // Trả về chuỗi đại diện của đối tượng để hiển thị.
    @Override
    public String toString() {
        return tenDanhMuc;
    }
}
