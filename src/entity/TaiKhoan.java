package entity;

// Lớp dữ liệu biểu diễn tài khoản, gồm thuộc tính và các phương thức truy cập.
public class TaiKhoan {

        // maNhanVien: Lưu mã nhân viên.
        private String maNhanVien;
        // hoTen: Lưu họ tên.
        private String hoTen;
        // soDienThoai: Lưu số điện thoại.
        private String soDienThoai;
        // email: Lưu email.
        private String email;
        // matKhau: Lưu mật khẩu.
        private String matKhau;
        // gioiTinh: Lưu giới tính.
        private String gioiTinh;
        // vaiTro: Lưu vai trò.
        private String vaiTro;

        // Khởi tạo đối tượng với trạng thái ban đầu.
        public TaiKhoan() {
        }

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số maNhanVien: mã nhân viên.
        // Tham số hoTen: họ tên.
        // Tham số soDienThoai: số điện thoại.
        // Tham số email: email.
        // Tham số matKhau: mật khẩu.
        // Tham số gioiTinh: giới tính.
        // Tham số vaiTro: vai trò.
        public TaiKhoan(
                        String maNhanVien,
                        String hoTen,
                        String soDienThoai,
                        String email,
                        String matKhau,
                        String gioiTinh,
                        String vaiTro) {

                this.maNhanVien = maNhanVien;
                this.hoTen = hoTen;
                this.soDienThoai = soDienThoai;
                this.email = email;
                this.matKhau = matKhau;
                this.gioiTinh = gioiTinh;
                this.vaiTro = vaiTro;
        }

        // Trả về mã nhân viên của đối tượng.
        public String getMaNhanVien() {
                return maNhanVien;
        }

        // Gán mã nhân viên từ giá trị được truyền vào.
        // Tham số maNhanVien: mã nhân viên.
        public void setMaNhanVien(
                        String maNhanVien) {
                this.maNhanVien = maNhanVien;
        }

        // Trả về họ tên của đối tượng.
        public String getHoTen() {
                return hoTen;
        }

        // Gán họ tên từ giá trị được truyền vào.
        // Tham số hoTen: họ tên.
        public void setHoTen(
                        String hoTen) {
                this.hoTen = hoTen;
        }

        // Trả về số điện thoại của đối tượng.
        public String getSoDienThoai() {
                return soDienThoai;
        }

        // Gán số điện thoại từ giá trị được truyền vào.
        // Tham số soDienThoai: số điện thoại.
        public void setSoDienThoai(
                        String soDienThoai) {
                this.soDienThoai = soDienThoai;
        }

        // Trả về email của đối tượng.
        public String getEmail() {
                return email;
        }

        // Gán email từ giá trị được truyền vào.
        // Tham số email: email.
        public void setEmail(
                        String email) {
                this.email = email;
        }

        // Trả về mật khẩu của đối tượng.
        public String getMatKhau() {
                return matKhau;
        }

        // Gán mật khẩu từ giá trị được truyền vào.
        // Tham số matKhau: mật khẩu.
        public void setMatKhau(
                        String matKhau) {
                this.matKhau = matKhau;
        }

        // Trả về giới tính của đối tượng.
        public String getGioiTinh() {
                return gioiTinh;
        }

        // Gán giới tính từ giá trị được truyền vào.
        // Tham số gioiTinh: giới tính.
        public void setGioiTinh(
                        String gioiTinh) {
                this.gioiTinh = gioiTinh;
        }

        // Trả về vai trò của đối tượng.
        public String getVaiTro() {
                return vaiTro;
        }

        // Gán vai trò từ giá trị được truyền vào.
        // Tham số vaiTro: vai trò.
        public void setVaiTro(
                        String vaiTro) {
                this.vaiTro = vaiTro;
        }
}