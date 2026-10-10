package entity;

public class KhachHang {

        private String maKhachHang;
        private String hoTen;
        private String soDienThoai;
        private String email;
        private String diaChi;
        private int diemTichLuy;

        // thong tin phuc vu hien thi danh sach
        private String thuCung;
        private String hangThe;
        private long tongChi;
        private String ganNhat;

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

        public String getMaKhachHang() { return maKhachHang; }
        public void setMaKhachHang(String v) { this.maKhachHang = v; }
        public String getHoTen() { return hoTen; }
        public void setHoTen(String v) { this.hoTen = v; }
        public String getSoDienThoai() { return soDienThoai; }
        public void setSoDienThoai(String v) { this.soDienThoai = v; }
        public String getEmail() { return email; }
        public void setEmail(String v) { this.email = v; }
        public String getDiaChi() { return diaChi; }
        public void setDiaChi(String v) { this.diaChi = v; }
        public int getDiemTichLuy() { return diemTichLuy; }
        public void setDiemTichLuy(int v) { this.diemTichLuy = v; }
        public String getThuCung() { return thuCung; }
        public void setThuCung(String v) { this.thuCung = v; }
        public String getHangThe() { return hangThe; }
        public void setHangThe(String v) { this.hangThe = v; }
        public long getTongChi() { return tongChi; }
        public void setTongChi(long v) { this.tongChi = v; }
        public String getGanNhat() { return ganNhat; }
        public void setGanNhat(String v) { this.ganNhat = v; }
}
