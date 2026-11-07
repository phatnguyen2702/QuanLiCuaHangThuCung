package entity;

import java.math.BigDecimal;
import java.time.LocalDate;


public class Voucher {


        private String maVoucher;

        private String tenVoucher;

        private double phanTramGiam;

        private String dieuKien;

        private boolean trangThai;

        private LocalDate ngayBatDau;

        private LocalDate ngayKetThuc;

        private Integer soLuongToiDa;



        private int daDung;


        public Voucher() {
        }










        public Voucher(String maVoucher, String tenVoucher, double phanTramGiam,
                        String dieuKien, boolean trangThai,
                        LocalDate ngayBatDau, LocalDate ngayKetThuc, Integer soLuongToiDa) {
                this.maVoucher = maVoucher;
                this.tenVoucher = tenVoucher;
                this.phanTramGiam = phanTramGiam;
                this.dieuKien = dieuKien;
                this.trangThai = trangThai;
                this.ngayBatDau = ngayBatDau;
                this.ngayKetThuc = ngayKetThuc;
                this.soLuongToiDa = soLuongToiDa;
        }

        public Voucher(
                        String maVoucher,
                        String tenVoucher,
                        BigDecimal phanTramGiam,
                        String dieuKien) {
                this.maVoucher = maVoucher;
                this.tenVoucher = tenVoucher;
                this.phanTramGiam = phanTramGiam == null ? 0 : phanTramGiam.doubleValue();
                this.dieuKien = dieuKien;
        }




        public String getTrangThaiText() {

                if (!trangThai) {
                        return "Da huy";
                }


                LocalDate homNay = LocalDate.now();

                if (ngayBatDau != null && homNay.isBefore(ngayBatDau)) {
                        return "Sap chay";
                }

                if (ngayKetThuc != null && homNay.isAfter(ngayKetThuc)) {
                        return "Het han";
                }

                if (soLuongToiDa != null && daDung >= soLuongToiDa) {
                        return "Het luot";
                }

                return "Dang chay";
        }


        public String getMaVoucher() {
                return maVoucher;
        }



        public void setMaVoucher(String v) {
                this.maVoucher = v;
        }


        public String getTenVoucher() {
                return tenVoucher;
        }



        public void setTenVoucher(String v) {
                this.tenVoucher = v;
        }


        public double getPhanTramGiam() {
                return phanTramGiam;
        }



        public void setPhanTramGiam(double v) {
                this.phanTramGiam = v;
        }


        public String getDieuKien() {
                return dieuKien;
        }



        public void setDieuKien(String v) {
                this.dieuKien = v;
        }


        public boolean isTrangThai() {
                return trangThai;
        }



        public void setTrangThai(boolean v) {
                this.trangThai = v;
        }


        public LocalDate getNgayBatDau() {
                return ngayBatDau;
        }



        public void setNgayBatDau(LocalDate v) {
                this.ngayBatDau = v;
        }


        public LocalDate getNgayKetThuc() {
                return ngayKetThuc;
        }



        public void setNgayKetThuc(LocalDate v) {
                this.ngayKetThuc = v;
        }


        public Integer getSoLuongToiDa() {
                return soLuongToiDa;
        }



        public void setSoLuongToiDa(Integer v) {
                this.soLuongToiDa = v;
        }


        public int getDaDung() {
                return daDung;
        }



        public void setDaDung(int v) {
                this.daDung = v;
        }
}
