package entity;

public class TaiKhoan {

        private String maNhanVien;
        private String hoTen;
        private String soDienThoai;
        private String email;
        private String matKhau;
        private String gioiTinh;
        private String vaiTro;

        public TaiKhoan() {
        }

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

        public String getMaNhanVien() {
                return maNhanVien;
        }

        public void setMaNhanVien(
                        String maNhanVien) {
                this.maNhanVien = maNhanVien;
        }

        public String getHoTen() {
                return hoTen;
        }

        public void setHoTen(
                        String hoTen) {
                this.hoTen = hoTen;
        }

        public String getSoDienThoai() {
                return soDienThoai;
        }

        public void setSoDienThoai(
                        String soDienThoai) {
                this.soDienThoai = soDienThoai;
        }

        public String getEmail() {
                return email;
        }

        public void setEmail(
                        String email) {
                this.email = email;
        }

        public String getMatKhau() {
                return matKhau;
        }

        public void setMatKhau(
                        String matKhau) {
                this.matKhau = matKhau;
        }

        public String getGioiTinh() {
                return gioiTinh;
        }

        public void setGioiTinh(
                        String gioiTinh) {
                this.gioiTinh = gioiTinh;
        }

        public String getVaiTro() {
                return vaiTro;
        }

        public void setVaiTro(
                        String vaiTro) {
                this.vaiTro = vaiTro;
        }
}