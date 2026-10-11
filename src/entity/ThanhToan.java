package entity;

import java.math.BigDecimal;

public class ThanhToan {

    private final String maSanPham;
    private final String maDichVu;
    private final String tenMatHang;
    private final BigDecimal donGia;
    private int soLuong;

    private ThanhToan(
            String maSanPham,
            String maDichVu,
            String tenMatHang,
            BigDecimal donGia,
            int soLuong) {
        if ((maSanPham == null) == (maDichVu == null)) {
            throw new IllegalArgumentException("Chi tiet phai la san pham hoac dich vu.");
        }
        if (tenMatHang == null || donGia == null || donGia.signum() < 0 || soLuong < 1) {
            throw new IllegalArgumentException("Thong tin thanh toan khong hop le.");
        }
        this.maSanPham = maSanPham;
        this.maDichVu = maDichVu;
        this.tenMatHang = tenMatHang;
        this.donGia = donGia;
        this.soLuong = soLuong;
    }

    public static ThanhToan sanPham(String ma, String ten, BigDecimal donGia) {
        return new ThanhToan(ma, null, ten, donGia, 1);
    }

    public static ThanhToan dichVu(String ma, String ten, BigDecimal donGia) {
        return new ThanhToan(null, ma, ten, donGia, 1);
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public String getMaHang() {
        return maSanPham == null ? maDichVu : maSanPham;
    }

    public String getTenMatHang() {
        return tenMatHang;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong < 1) {
            throw new IllegalArgumentException("So luong phai lon hon 0.");
        }
        this.soLuong = soLuong;
    }

    public BigDecimal getThanhTien() {
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    public static class KhachHang {
        private final String maKhachHang;
        private final String hoTen;
        private final String soDienThoai;

        public KhachHang(String maKhachHang, String hoTen, String soDienThoai) {
            this.maKhachHang = maKhachHang;
            this.hoTen = hoTen;
            this.soDienThoai = soDienThoai;
        }

        public String getMaKhachHang() {
            return maKhachHang;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }
    }

    public static class Voucher {
        private final String maVoucher;
        private final String tenVoucher;
        private final BigDecimal phanTramGiam;
        private final String dieuKien;

        public Voucher(
                String maVoucher,
                String tenVoucher,
                BigDecimal phanTramGiam,
                String dieuKien) {
            this.maVoucher = maVoucher;
            this.tenVoucher = tenVoucher;
            this.phanTramGiam = phanTramGiam;
            this.dieuKien = dieuKien;
        }

        public String getMaVoucher() {
            return maVoucher;
        }

        public String getTenVoucher() {
            return tenVoucher;
        }

        public BigDecimal getPhanTramGiam() {
            return phanTramGiam;
        }

        public String getDieuKien() {
            return dieuKien;
        }
    }

    public static class PhuongThuc {
        private final String maPhuongThuc;
        private final String tenPhuongThuc;

        public PhuongThuc(String maPhuongThuc, String tenPhuongThuc) {
            this.maPhuongThuc = maPhuongThuc;
            this.tenPhuongThuc = tenPhuongThuc;
        }

        public String getMaPhuongThuc() {
            return maPhuongThuc;
        }

        public String getTenPhuongThuc() {
            return tenPhuongThuc;
        }

        @Override
        public String toString() {
            return tenPhuongThuc;
        }
    }
}
