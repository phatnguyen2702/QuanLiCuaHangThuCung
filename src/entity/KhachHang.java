package entity;

/** Khách hàng của cửa hàng. */
public class KhachHang {

    private String maKhachHang;
    private String hoTen;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private int diemTichLuy;

    // Các trường chỉ dùng để hiển thị trên bảng (DAO tính sẵn khi truy vấn)
    private String thuCung;   // số thú cưng
    private String hangThe;
    private long tongChi;     // tổng tiền đã chi
    private String ganNhat;   // ngày mua gần nhất (dd/MM/yyyy)

    public KhachHang() {
    }

    public KhachHang(String maKhachHang, String hoTen, String soDienThoai,
            String email, String diaChi, int diemTichLuy) {
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
        this.diemTichLuy = diemTichLuy;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    public String getThuCung() {
        return thuCung;
    }

    public void setThuCung(String thuCung) {
        this.thuCung = thuCung;
    }

    public String getHangThe() {
        return hangThe;
    }

    public void setHangThe(String hangThe) {
        this.hangThe = hangThe;
    }

    public long getTongChi() {
        return tongChi;
    }

    public void setTongChi(long tongChi) {
        this.tongChi = tongChi;
    }

    public String getGanNhat() {
        return ganNhat;
    }

    public void setGanNhat(String ganNhat) {
        this.ganNhat = ganNhat;
    }
}
