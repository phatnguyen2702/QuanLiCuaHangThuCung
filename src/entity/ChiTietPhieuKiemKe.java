package entity;

// Lớp dữ liệu biểu diễn chi tiết phiếu kiểm kê, gồm thuộc tính và các phương thức truy cập.
public class ChiTietPhieuKiemKe {

    // maPhieuKiemKe: Lưu mã phiếu kiểm kê.
    private String maPhieuKiemKe;
    // maSanPham: Lưu mã sản phẩm.
    private String maSanPham;
    // soLuongHeThong: Lưu số lượng theo hệ thống.
    private int soLuongHeThong;
    // soLuongThucTe: Lưu số lượng kiểm đếm thực tế.
    private Integer soLuongThucTe;  // null = chua kiem
    // trangThai: Cờ cho biết trạng thái.
    private boolean trangThai;      // true = khop, false = chenh lech

    // Chi dung de hien thi
    // tenSanPham: Lưu tên sản phẩm.
    private String tenSanPham;

    // Khởi tạo đối tượng với trạng thái ban đầu.
    public ChiTietPhieuKiemKe() {
    }

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maPhieuKiemKe: mã phiếu kiểm kê.
    // Tham số maSanPham: mã sản phẩm.
    // Tham số soLuongHeThong: số lượng theo hệ thống.
    // Tham số soLuongThucTe: số lượng kiểm đếm thực tế.
    // Tham số trangThai: trạng thái.
    public ChiTietPhieuKiemKe(String maPhieuKiemKe, String maSanPham,
                              int soLuongHeThong, Integer soLuongThucTe,
                              boolean trangThai) {
        this.maPhieuKiemKe = maPhieuKiemKe;
        this.maSanPham = maSanPham;
        this.soLuongHeThong = soLuongHeThong;
        this.soLuongThucTe = soLuongThucTe;
        this.trangThai = trangThai;
    }

    // Trả về mã phiếu kiểm kê của đối tượng.
    public String getMaPhieuKiemKe() { return maPhieuKiemKe; }
    // Gán mã phiếu kiểm kê từ giá trị được truyền vào.
    // Tham số maPhieuKiemKe: mã phiếu kiểm kê.
    public void setMaPhieuKiemKe(String maPhieuKiemKe) { this.maPhieuKiemKe = maPhieuKiemKe; }

    // Trả về mã sản phẩm của đối tượng.
    public String getMaSanPham() { return maSanPham; }
    // Gán mã sản phẩm từ giá trị được truyền vào.
    // Tham số maSanPham: mã sản phẩm.
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }

    // Trả về số lượng theo hệ thống của đối tượng.
    public int getSoLuongHeThong() { return soLuongHeThong; }
    // Gán số lượng theo hệ thống từ giá trị được truyền vào.
    // Tham số soLuongHeThong: số lượng theo hệ thống.
    public void setSoLuongHeThong(int soLuongHeThong) { this.soLuongHeThong = soLuongHeThong; }

    // Trả về số lượng kiểm đếm thực tế của đối tượng.
    public Integer getSoLuongThucTe() { return soLuongThucTe; }
    // Gán số lượng kiểm đếm thực tế từ giá trị được truyền vào.
    // Tham số soLuongThucTe: số lượng kiểm đếm thực tế.
    public void setSoLuongThucTe(Integer soLuongThucTe) { this.soLuongThucTe = soLuongThucTe; }

    // Trả về cờ trạng thái hiện tại của đối tượng.
    public boolean isTrangThai() { return trangThai; }
    // Gán trạng thái từ giá trị được truyền vào.
    // Tham số trangThai: trạng thái.
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    // Trả về tên sản phẩm của đối tượng.
    public String getTenSanPham() { return tenSanPham; }
    // Gán tên sản phẩm từ giá trị được truyền vào.
    // Tham số tenSanPham: tên sản phẩm.
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    // Kiểm tra dòng đã có số lượng kiểm đếm thực tế hay chưa.
    // Kết quả: true/false theo điều kiện xử lý.
    public boolean daKiem() {
        return soLuongThucTe != null;
    }

    // Tính chênh lệch giữa số lượng thực tế và số lượng hệ thống.
    // Kết quả: giá trị số nguyên.
    public int getChenhLech() {
        return soLuongThucTe == null ? 0 : soLuongThucTe - soLuongHeThong;
    }
}
