package data;

import java.util.ArrayList;
import java.util.List;
import model.*;

public class DuLieu {
    public static final List<DanhMuc> dsDanhMuc = new ArrayList<>();
    public static final List<NhaCungCap> dsNhaCungCap = new ArrayList<>();
    public static final List<SanPham> dsSanPham = new ArrayList<>();
    public static final List<PhieuNhapKho> dsPhieuNhap = new ArrayList<>();
    public static final List<PhieuKiemKe> dsKiemKe = new ArrayList<>();
    public static final NhanVien nguoiDung =
            new NhanVien("NV001", "Đặng Đình An", "0901234567", "an@petshop.vn", "123456", "Nam", "Thu ngân");

    static {
        DanhMuc thucAnCho = new DanhMuc("DM001", "Thức ăn cho chó", "Hạt, pate cho chó");
        DanhMuc thucAnMeo = new DanhMuc("DM002", "Thức ăn cho mèo", "Hạt, pate cho mèo");
        DanhMuc phuKien = new DanhMuc("DM003", "Phụ kiện", "Dây dắt, vòng cổ, nhà cây");
        DanhMuc veSinh = new DanhMuc("DM004", "Vệ sinh", "Sữa tắm, khay vệ sinh");
        dsDanhMuc.add(thucAnCho); dsDanhMuc.add(thucAnMeo); dsDanhMuc.add(phuKien); dsDanhMuc.add(veSinh);

        NhaCungCap n1 = new NhaCungCap("NCC001", "Công ty TNHH Pet Care Việt", "0281234567", "petcare@mail.vn", "Q.1, TP.HCM");
        NhaCungCap n2 = new NhaCungCap("NCC002", "Pet World Supply", "0287654321", "sales@petworld.vn", "Q.7, TP.HCM");
        dsNhaCungCap.add(n1); dsNhaCungCap.add(n2);

        dsSanPham.add(new SanPham("SP00128", "Royal Canin Mini Adult 2kg", 335000, 420000, thucAnCho, n1, 18));
        dsSanPham.add(new SanPham("SP00131", "Hạt Reflex Plus Kitten 1.5kg", 221000, 285000, thucAnMeo, n1, 12));
        dsSanPham.add(new SanPham("SP00046", "Pate CIAO cá ngừ 80g", 21500, 29000, thucAnMeo, n2, 64));
        dsSanPham.add(new SanPham("SP00205", "Dây dắt phản quang size M", 92000, 165000, phuKien, n2, 9));
        dsSanPham.add(new SanPham("SP00176", "Sữa tắm Joyce & Dolls 500ml", 168000, 230000, veSinh, n1, 4));
        dsSanPham.add(new SanPham("SP00224", "Nhà cây mèo 3 tầng", 870000, 1250000, phuKien, n2, 3));
        dsSanPham.add(new SanPham("SP00235", "Khay vệ sinh mèo size L", 120000, 175000, veSinh, n2, 4));
        dsSanPham.add(new SanPham("SP00091", "Snack xương gặm vị bò 100g", 44000, 68000, thucAnCho, n1, 26));
    }

    public static SanPham timSanPham(String ma) {
        for (SanPham s : dsSanPham) if (s.getMaSanPham().equals(ma)) return s;
        return null;
    }
    public static DanhMuc timDanhMuc(String ma) {
        for (DanhMuc d : dsDanhMuc) if (d.getMaDanhMuc().equals(ma)) return d;
        return null;
    }
}
