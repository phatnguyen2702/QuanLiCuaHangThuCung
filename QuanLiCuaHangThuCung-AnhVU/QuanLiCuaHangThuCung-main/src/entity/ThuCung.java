package entity;

public class ThuCung {

        private String maThuCung;
        private String tenThuCung;
        private String loai;
        private String giong;
        private String gioiTinh;
        private Double canNang;
        private String maKhachHang;

        public ThuCung() {
        }

        public ThuCung(String maThuCung, String tenThuCung, String loai, String giong,
                        String gioiTinh, Double canNang, String maKhachHang) {
                this.maThuCung = maThuCung;
                this.tenThuCung = tenThuCung;
                this.loai = loai;
                this.giong = giong;
                this.gioiTinh = gioiTinh;
                this.canNang = canNang;
                this.maKhachHang = maKhachHang;
        }

        public String getMaThuCung() { return maThuCung; }
        public void setMaThuCung(String v) { this.maThuCung = v; }
        public String getTenThuCung() { return tenThuCung; }
        public void setTenThuCung(String v) { this.tenThuCung = v; }
        public String getLoai() { return loai; }
        public void setLoai(String v) { this.loai = v; }
        public String getGiong() { return giong; }
        public void setGiong(String v) { this.giong = v; }
        public String getGioiTinh() { return gioiTinh; }
        public void setGioiTinh(String v) { this.gioiTinh = v; }
        public Double getCanNang() { return canNang; }
        public void setCanNang(Double v) { this.canNang = v; }
        public String getMaKhachHang() { return maKhachHang; }
        public void setMaKhachHang(String v) { this.maKhachHang = v; }
}
