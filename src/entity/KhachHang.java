package entity;

// Lớp dữ liệu biểu diễn khách hàng, gồm thuộc tính và các phương thức truy cập.
public class KhachHang {

        // maKhachHang: Lưu mã khách hàng.
        private String maKhachHang;
        // hoTen: Lưu họ tên.
        private String hoTen;
        // soDienThoai: Lưu số điện thoại.
        private String soDienThoai;
        // email: Lưu email.
        private String email;
        // diaChi: Lưu địa chỉ.
        private String diaChi;
        // diemTichLuy: Lưu điểm tích lũy.
        private int diemTichLuy;

        // thong tin phuc vu hien thi danh sach
        // thuCung: Lưu thú cưng.
        private String thuCung;
        // hangThe: Lưu hạng thẻ thành viên.
        private String hangThe;
        // tongChi: Lưu tổng chi tiêu.
        private long tongChi;
        // ganNhat: Lưu thông tin giao dịch gần nhất.
        private String ganNhat;

        // Khởi tạo đối tượng với trạng thái ban đầu.
        public KhachHang() {
        }

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số maKhachHang: mã khách hàng.
        // Tham số hoTen: họ tên.
        // Tham số soDienThoai: số điện thoại.
        // Tham số email: email.
        // Tham số diaChi: địa chỉ.
        // Tham số diemTichLuy: điểm tích lũy.
        public KhachHang(String maKhachHang, String hoTen, String soDienThoai,
                        String email, String diaChi, int diemTichLuy) {
                this.maKhachHang = maKhachHang;
                this.hoTen = hoTen;
                this.soDienThoai = soDienThoai;
                this.email = email;
                this.diaChi = diaChi;
                this.diemTichLuy = diemTichLuy;
        }

        // Trả về mã khách hàng của đối tượng.
        public String getMaKhachHang() {
                return maKhachHang;
        }

        // Gán mã khách hàng từ giá trị được truyền vào.
        // Tham số v: mã khách hàng mới.
        public void setMaKhachHang(String v) {
                this.maKhachHang = v;
        }

        // Trả về họ tên của đối tượng.
        public String getHoTen() {
                return hoTen;
        }

        // Gán họ tên từ giá trị được truyền vào.
        // Tham số v: họ tên mới.
        public void setHoTen(String v) {
                this.hoTen = v;
        }

        // Trả về số điện thoại của đối tượng.
        public String getSoDienThoai() {
                return soDienThoai;
        }

        // Gán số điện thoại từ giá trị được truyền vào.
        // Tham số v: số điện thoại mới.
        public void setSoDienThoai(String v) {
                this.soDienThoai = v;
        }

        // Trả về email của đối tượng.
        public String getEmail() {
                return email;
        }

        // Gán email từ giá trị được truyền vào.
        // Tham số v: email mới.
        public void setEmail(String v) {
                this.email = v;
        }

        // Trả về địa chỉ của đối tượng.
        public String getDiaChi() {
                return diaChi;
        }

        // Gán địa chỉ từ giá trị được truyền vào.
        // Tham số v: địa chỉ mới.
        public void setDiaChi(String v) {
                this.diaChi = v;
        }

        // Trả về điểm tích lũy của đối tượng.
        public int getDiemTichLuy() {
                return diemTichLuy;
        }

        // Gán điểm tích lũy từ giá trị được truyền vào.
        // Tham số v: điểm tích lũy mới.
        public void setDiemTichLuy(int v) {
                this.diemTichLuy = v;
        }

        // Trả về thú cưng của đối tượng.
        public String getThuCung() {
                return thuCung;
        }

        // Gán thú cưng từ giá trị được truyền vào.
        // Tham số v: thú cưng mới.
        public void setThuCung(String v) {
                this.thuCung = v;
        }

        // Trả về hạng thẻ thành viên của đối tượng.
        public String getHangThe() {
                return hangThe;
        }

        // Gán hạng thẻ thành viên từ giá trị được truyền vào.
        // Tham số v: hạng thẻ thành viên mới.
        public void setHangThe(String v) {
                this.hangThe = v;
        }

        // Trả về tổng chi tiêu của đối tượng.
        public long getTongChi() {
                return tongChi;
        }

        // Gán tổng chi tiêu từ giá trị được truyền vào.
        // Tham số v: tổng chi tiêu mới.
        public void setTongChi(long v) {
                this.tongChi = v;
        }

        // Trả về thông tin giao dịch gần nhất của đối tượng.
        public String getGanNhat() {
                return ganNhat;
        }

        // Gán thông tin giao dịch gần nhất từ giá trị được truyền vào.
        // Tham số v: thông tin giao dịch gần nhất mới.
        public void setGanNhat(String v) {
                this.ganNhat = v;
        }
}
