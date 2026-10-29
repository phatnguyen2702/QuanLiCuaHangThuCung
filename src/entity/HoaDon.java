package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Lớp dữ liệu biểu diễn hóa đơn, gồm thuộc tính và các phương thức truy cập.
public class HoaDon {

    // maHoaDon: Lưu mã hóa đơn.
    private final String maHoaDon;
    // ngayLap: Lưu ngày giờ lập hóa đơn dưới dạng dữ liệu thời gian.
    private final LocalDateTime ngayLap;
    // maKhachHang: Lưu mã khách hàng.
    private final String maKhachHang;
    // tenKhachHang: Lưu tên khách hàng.
    private final String tenKhachHang;
    // soDienThoai: Lưu số điện thoại.
    private final String soDienThoai;
    // tenPhuongThuc: Lưu tên phương thức thanh toán.
    private final String tenPhuongThuc;
    // tongTien: Giá trị tổng tiền dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
    private final BigDecimal tongTien;
    // chiTiet: Lưu chi tiết; phần tử có kiểu ChiTiet.
    private final List<ChiTiet> chiTiet;

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maHoaDon: mã hóa đơn.
    // Tham số ngayLap: ngày giờ lập hóa đơn.
    // Tham số maKhachHang: mã khách hàng.
    // Tham số tenKhachHang: tên khách hàng.
    // Tham số soDienThoai: số điện thoại.
    // Tham số tenPhuongThuc: tên phương thức thanh toán.
    // Tham số tongTien: tổng tiền.
    // Tham số chiTiet: chi tiết.
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

    // Trả về mã hóa đơn của đối tượng.
    public String getMaHoaDon() {
        return maHoaDon;
    }

    // Trả về ngày giờ lập hóa đơn của đối tượng.
    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    // Trả về mã khách hàng của đối tượng.
    public String getMaKhachHang() {
        return maKhachHang;
    }

    // Trả về tên khách hàng của đối tượng.
    public String getTenKhachHang() {
        return tenKhachHang;
    }

    // Trả về số điện thoại của đối tượng.
    public String getSoDienThoai() {
        return soDienThoai;
    }

    // Trả về tên phương thức thanh toán của đối tượng.
    public String getTenPhuongThuc() {
        return tenPhuongThuc;
    }

    // Trả về tổng tiền của đối tượng.
    public BigDecimal getTongTien() {
        return tongTien;
    }

    // Trả về chi tiết của đối tượng.
    public List<ChiTiet> getChiTiet() {
        return chiTiet;
    }

    // Nhóm dữ liệu hoặc chức năng chi tiết dùng trong HoaDon.
    public static class ChiTiet {
        // maHang: Lưu mã mặt hàng.
        private final String maHang;
        // tenHang: Lưu tên hàng.
        private final String tenHang;
        // loaiHang: Lưu loại mặt hàng.
        private final String loaiHang;
        // tenThuCung: Lưu tên thú cưng.
        private final String tenThuCung;
        // soLuong: Lưu số lượng.
        private final int soLuong;
        // donGia: Giá trị đơn giá dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        private final BigDecimal donGia;
        // phanTramGiam: Giá trị phần trăm giảm giá dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        private final BigDecimal phanTramGiam;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số maHang: mã mặt hàng.
        // Tham số tenHang: tên hàng.
        // Tham số loaiHang: loại mặt hàng.
        // Tham số soLuong: số lượng.
        // Tham số donGia: đơn giá.
        // Tham số phanTramGiam: phần trăm giảm giá.
        public ChiTiet(
                String maHang,
                String tenHang,
                String loaiHang,
                int soLuong,
                BigDecimal donGia,
                BigDecimal phanTramGiam) {
            this(maHang, tenHang, loaiHang, soLuong, donGia, phanTramGiam, null);
        }

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số maHang: mã mặt hàng.
        // Tham số tenHang: tên hàng.
        // Tham số loaiHang: loại mặt hàng.
        // Tham số soLuong: số lượng.
        // Tham số donGia: đơn giá.
        // Tham số phanTramGiam: phần trăm giảm giá.
        // Tham số tenThuCung: tên thú cưng.
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

        // Trả về mã mặt hàng của đối tượng.
        public String getMaHang() {
            return maHang;
        }

        // Trả về tên hàng của đối tượng.
        public String getTenHang() {
            return tenHang;
        }

        // Trả về loại mặt hàng của đối tượng.
        public String getLoaiHang() {
            return loaiHang;
        }

        // Trả về tên thú cưng của đối tượng.
        public String getTenThuCung() {
            return tenThuCung;
        }

        // Trả về số lượng của đối tượng.
        public int getSoLuong() {
            return soLuong;
        }

        // Trả về đơn giá của đối tượng.
        public BigDecimal getDonGia() {
            return donGia;
        }

        // Trả về phần trăm giảm giá của đối tượng.
        public BigDecimal getPhanTramGiam() {
            return phanTramGiam;
        }

        // Tính thành tiền của dòng dữ liệu.
        // Kết quả: đối tượng/giá trị kiểu BigDecimal.
        public BigDecimal getThanhTien() {
            return donGia.multiply(BigDecimal.valueOf(soLuong))
                    .multiply(BigDecimal.ONE.subtract(
                            phanTramGiam.divide(BigDecimal.valueOf(100))));
        }
    }
}
