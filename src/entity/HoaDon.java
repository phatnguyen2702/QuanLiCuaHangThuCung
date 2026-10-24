package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HoaDon {

    private final String maHoaDon;
    private final LocalDateTime ngayLap;
    private final String maKhachHang;
    private final String tenKhachHang;
    private final String soDienThoai;
    private final String tenPhuongThuc;
    private final BigDecimal tongTien;
    private final List<ChiTiet> chiTiet;

    public HoaDon(
            String maHoaDon,
            LocalDateTime ngayLap,
            String maKhachHang,
            String tenKhachHang,
            String soDienThoai,
            String tenPhuongThuc,
            BigDecimal tongTien,
            List<ChiTiet> chiTiet) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoai = soDienThoai;
        this.tenPhuongThuc = tenPhuongThuc;
        this.tongTien = tongTien == null ? BigDecimal.ZERO : tongTien;
        this.chiTiet = chiTiet == null
                ? Collections.<ChiTiet>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(chiTiet));
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getTenPhuongThuc() {
        return tenPhuongThuc;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public List<ChiTiet> getChiTiet() {
        return chiTiet;
    }

    public static class ChiTiet {
        private final String maHang;
        private final String tenHang;
        private final String loaiHang;
        private final String tenThuCung;
        private final int soLuong;
        private final BigDecimal donGia;
        private final BigDecimal phanTramGiam;

        public ChiTiet(
                String maHang,
                String tenHang,
                String loaiHang,
                int soLuong,
                BigDecimal donGia,
                BigDecimal phanTramGiam) {
            this(maHang, tenHang, loaiHang, soLuong, donGia, phanTramGiam, null);
        }

        public ChiTiet(
                String maHang,
                String tenHang,
                String loaiHang,
                int soLuong,
                BigDecimal donGia,
                BigDecimal phanTramGiam,
                String tenThuCung) {
            this.maHang = maHang;
            this.tenHang = tenHang;
            this.loaiHang = loaiHang;
            this.tenThuCung = tenThuCung;
            this.soLuong = soLuong;
            this.donGia = donGia == null ? BigDecimal.ZERO : donGia;
            this.phanTramGiam = phanTramGiam == null ? BigDecimal.ZERO : phanTramGiam;
        }

        public String getMaHang() {
            return maHang;
        }

        public String getTenHang() {
            return tenHang;
        }

        public String getLoaiHang() {
            return loaiHang;
        }

        public String getTenThuCung() {
            return tenThuCung;
        }

        public int getSoLuong() {
            return soLuong;
        }

        public BigDecimal getDonGia() {
            return donGia;
        }

        public BigDecimal getPhanTramGiam() {
            return phanTramGiam;
        }

        public BigDecimal getThanhTien() {
            return donGia.multiply(BigDecimal.valueOf(soLuong))
                    .multiply(BigDecimal.ONE.subtract(
                            phanTramGiam.divide(BigDecimal.valueOf(100))));
        }
    }
}
