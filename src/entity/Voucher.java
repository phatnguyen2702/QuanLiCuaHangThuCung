package entity;

import java.time.LocalDate;

// Lớp dữ liệu biểu diễn voucher, gồm thuộc tính và các phương thức truy cập.
public class Voucher {

        // maVoucher: Lưu mã voucher.
        private String maVoucher;
        // tenVoucher: Lưu tên voucher.
        private String tenVoucher;
        // phanTramGiam: Lưu phần trăm giảm giá.
        private double phanTramGiam;
        // dieuKien: Lưu điều kiện áp dụng voucher.
        private String dieuKien;
        // trangThai: Cờ cho biết trạng thái.
        private boolean trangThai;
        // ngayBatDau: Lưu thời điểm bắt đầu dưới dạng dữ liệu thời gian.
        private LocalDate ngayBatDau;
        // ngayKetThuc: Lưu ngày kết thúc dưới dạng dữ liệu thời gian.
        private LocalDate ngayKetThuc;
        // soLuongToiDa: Lưu số lượt sử dụng tối đa.
        private Integer soLuongToiDa;

        // so luot da dung (dem tu bang HoaDon)
        // daDung: Lưu số lượt đã dùng.
        private int daDung;

        // Khởi tạo đối tượng với trạng thái ban đầu.
        public Voucher() {
        }

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số maVoucher: mã voucher.
        // Tham số tenVoucher: tên voucher.
        // Tham số phanTramGiam: phần trăm giảm giá.
        // Tham số dieuKien: điều kiện áp dụng voucher.
        // Tham số trangThai: trạng thái.
        // Tham số ngayBatDau: thời điểm bắt đầu.
        // Tham số ngayKetThuc: ngày kết thúc.
        // Tham số soLuongToiDa: số lượt sử dụng tối đa.
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

        // Trang thai hien thi: Dang chay / Sap chay / Het han / Het luot / Da huy
        // Lấy trạng thái dạng chữ.
        // Kết quả: chuỗi kết quả.
        public String getTrangThaiText() {

                if (!trangThai) {
                        return "Da huy";
                }

                // homNay: Lưu hôm nay dưới dạng dữ liệu thời gian.
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

        // Trả về mã voucher của đối tượng.
        public String getMaVoucher() {
                return maVoucher;
        }

        // Gán mã voucher từ giá trị được truyền vào.
        // Tham số v: mã voucher mới.
        public void setMaVoucher(String v) {
                this.maVoucher = v;
        }

        // Trả về tên voucher của đối tượng.
        public String getTenVoucher() {
                return tenVoucher;
        }

        // Gán tên voucher từ giá trị được truyền vào.
        // Tham số v: tên voucher mới.
        public void setTenVoucher(String v) {
                this.tenVoucher = v;
        }

        // Trả về phần trăm giảm giá của đối tượng.
        public double getPhanTramGiam() {
                return phanTramGiam;
        }

        // Gán phần trăm giảm giá từ giá trị được truyền vào.
        // Tham số v: phần trăm giảm giá mới.
        public void setPhanTramGiam(double v) {
                this.phanTramGiam = v;
        }

        // Trả về điều kiện áp dụng voucher của đối tượng.
        public String getDieuKien() {
                return dieuKien;
        }

        // Gán điều kiện áp dụng voucher từ giá trị được truyền vào.
        // Tham số v: điều kiện áp dụng voucher mới.
        public void setDieuKien(String v) {
                this.dieuKien = v;
        }

        // Trả về cờ trạng thái hiện tại của đối tượng.
        public boolean isTrangThai() {
                return trangThai;
        }

        // Gán trạng thái từ giá trị được truyền vào.
        // Tham số v: trạng thái mới.
        public void setTrangThai(boolean v) {
                this.trangThai = v;
        }

        // Trả về thời điểm bắt đầu của đối tượng.
        public LocalDate getNgayBatDau() {
                return ngayBatDau;
        }

        // Gán thời điểm bắt đầu từ giá trị được truyền vào.
        // Tham số v: thời điểm bắt đầu mới.
        public void setNgayBatDau(LocalDate v) {
                this.ngayBatDau = v;
        }

        // Trả về ngày kết thúc của đối tượng.
        public LocalDate getNgayKetThuc() {
                return ngayKetThuc;
        }

        // Gán ngày kết thúc từ giá trị được truyền vào.
        // Tham số v: ngày kết thúc mới.
        public void setNgayKetThuc(LocalDate v) {
                this.ngayKetThuc = v;
        }

        // Trả về số lượt sử dụng tối đa của đối tượng.
        public Integer getSoLuongToiDa() {
                return soLuongToiDa;
        }

        // Gán số lượt sử dụng tối đa từ giá trị được truyền vào.
        // Tham số v: số lượt sử dụng tối đa mới.
        public void setSoLuongToiDa(Integer v) {
                this.soLuongToiDa = v;
        }

        // Trả về số lượt đã dùng của đối tượng.
        public int getDaDung() {
                return daDung;
        }

        // Gán số lượt đã dùng từ giá trị được truyền vào.
        // Tham số v: số lượt đã dùng mới.
        public void setDaDung(int v) {
                this.daDung = v;
        }
}
