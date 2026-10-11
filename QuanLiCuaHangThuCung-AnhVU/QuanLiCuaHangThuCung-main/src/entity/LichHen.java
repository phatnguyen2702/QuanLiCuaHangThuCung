package entity;

import java.time.LocalDateTime;

public class LichHen {

        private String maLichHen;
        private LocalDateTime ngayGioHen;
        private String ghiChu;
        private String maKhachHang;
        private String tenKhachHang;
        private String soDienThoai;
        private String maThuCung;
        private String tenThuCung;
        private String maNhanVien;
        private String tenNhanVien;
        private String trangThai;

        public LichHen() {
        }

        public LichHen(String maLichHen, LocalDateTime ngayGioHen, String ghiChu,
                        String maKhachHang, String tenKhachHang, String soDienThoai,
                        String maThuCung, String tenThuCung,
                        String maNhanVien, String tenNhanVien, String trangThai) {
                this.maLichHen = maLichHen;
                this.ngayGioHen = ngayGioHen;
                this.ghiChu = ghiChu;
                this.maKhachHang = maKhachHang;
                this.tenKhachHang = tenKhachHang;
                this.soDienThoai = soDienThoai;
                this.maThuCung = maThuCung;
                this.tenThuCung = tenThuCung;
                this.maNhanVien = maNhanVien;
                this.tenNhanVien = tenNhanVien;
                this.trangThai = trangThai;
        }

        public String getMaLichHen() { return maLichHen; }
        public LocalDateTime getNgayGioHen() { return ngayGioHen; }
        public String getGhiChu() { return ghiChu; }
        public String getMaKhachHang() { return maKhachHang; }
        public String getTenKhachHang() { return tenKhachHang; }
        public String getSoDienThoai() { return soDienThoai; }
        public String getMaThuCung() { return maThuCung; }
        public String getTenThuCung() { return tenThuCung; }
        public String getMaNhanVien() { return maNhanVien; }
        public String getTenNhanVien() { return tenNhanVien; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String v) { this.trangThai = v; }
}
