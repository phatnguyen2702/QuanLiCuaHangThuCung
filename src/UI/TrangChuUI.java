package UI;

import control.TrangChuControl;
import control.PhanQuyenControl;

import entity.LichHen;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.zip.DeflaterOutputStream;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class TrangChuUI extends JFrame implements ActionListener {

    private final Color LIGHT_BLUE = new Color(229, 241, 252);
    private final Color MENU_BLUE = new Color(207, 222, 235);

    private final String maNV;
    private final String hoTen;
    private final String vaiTro;
    private final TrangChuControl control = new TrangChuControl();

    private static final String[] TEN_TAB = {
            "Danh mục", "Sản phẩm", "Nhập kho", "Kiểm kê kho"
    };

    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm:ss | dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO_HEN = DateTimeFormatter.ofPattern("HH:mm");

    private final CardLayout cardLayout = new CardLayout();

    private final JPanel pnlNoiDung = new JPanel(cardLayout);
    private JLabel lblNgayTrangChu;
    private JButton btnTrangChu;
    private JButton btnHeThong;
    private JButton btnDanhMuc;
    private JButton btnXuLy;
    private JButton btnTimKiem;
    private JButton btnThongKe;
    private JButton btnNhanVien, btnLamMoi, btnHoTro;

    private final ArrayList<JButton> dsBtnMucChinh = new ArrayList<>();
    private JButton btnNhomDaChon;

    private final JPanel[] arrPnlManHinhKho = new JPanel[TEN_TAB.length];

    private final Runnable[] napLai = new Runnable[TEN_TAB.length];

    private final Map<String, Component> mapManHinhKhac = new HashMap<>();
    private TraCuuLichSuHoaDonUI pnlTraCuuHoaDon;
    private TraCuuKhoUI pnlTraCuuNhapKho;
    private TraCuuKhoUI pnlTraCuuKiemKe;
    private ThongKeDoanhThuUI pnlThongKeDoanhThu;
    private QuanLyThanhToanUI pnlThanhToan;

    private final javax.swing.Timer tmrDongHo;
    private CanhBaoHanSuDungUI pnlCanhBaoHanSuDung;
    private GoiYThucAnUI pnlGoiYThucAn;

    private JPanel pnlLichSpaNoiDung;
    private JLabel lblPhuDeLichSpa;
    private JLabel lblDoanhThuHomNay;
    private JLabel lblLuotMuaHomNay;
    private JLabel lblLichSpaHomNay;
    private JLabel lblPhieuNhapHomNay;

    public TrangChuUI(String maNV, String hoTen, String vaiTro) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;

        control.kiemTraPhien(maNV, vaiTro);

        TrangChuControl.dat(maNV, hoTen, vaiTro);

        setTitle("Pet Shop Pro - Trang chủ");

        setSize(1200, 720);
        setMinimumSize(new Dimension(980, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        add(taoPnlHeader(), BorderLayout.NORTH);

        pnlNoiDung.setBackground(LIGHT_BLUE);

        pnlNoiDung.add(taoPnlTrangChu(), "trangchu");
        add(pnlNoiDung, BorderLayout.CENTER);

        tmrDongHo = new javax.swing.Timer(1000, e -> capNhatGio());
        tmrDongHo.start();
        capNhatGio();
        capNhatQuyenMenu();
        hienTrangChu();
    }

    @Override
    public void dispose() {
        tmrDongHo.stop();
        super.dispose();
    }

    private void hienTrangChu() {
        btnNhomDaChon = null;
        chuyenManHinh("trangchu", "Trang chủ", btnTrangChu);
        napLichSpaSapToi();
    }

    private void chuyenManHinh(String maManHinh, String tieuDe, JButton nutNhom) {

        cardLayout.show(pnlNoiDung, maManHinh);
        datMucDangChon(nutNhom);
        setTitle("Pet Shop Pro - " + tieuDe);
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
    }

    public void chuyenKho(int tab) {
        if (tab < 0 || tab >= TEN_TAB.length)
            tab = 0;

        try {

            boolean laLanDau = arrPnlManHinhKho[tab] == null;
            if (laLanDau) {
                taoManHinh(tab);
                pnlNoiDung.add(arrPnlManHinhKho[tab], "tab" + tab);
            }
            chuyenManHinh("tab" + tab, TEN_TAB[tab],
                    btnNhomDaChon != null ? btnNhomDaChon : btnDanhMuc);

            if (!laLanDau && napLai[tab] != null)
                napLai[tab].run();
        } catch (RuntimeException ex) {
            baoLoiMoManHinh(TEN_TAB[tab], ex);
        }
    }

    private void moThongKeDoanhThu() {
        boolean daTaoManHinh = pnlThongKeDoanhThu != null;
        hienManHinh("thongkedoanhthu", "Thống kê doanh thu", btnThongKe,
                () -> {
                    pnlThongKeDoanhThu = new ThongKeDoanhThuUI();
                    return pnlThongKeDoanhThu.getContentPanel();
                });
        if (daTaoManHinh && pnlThongKeDoanhThu != null) {
            pnlThongKeDoanhThu.lamMoi();
        }
    }

    private void taoManHinh(int tab) {
        switch (tab) {
            case 0 -> {
                DanhMucUI pnlKhung2 = new DanhMucUI();
                arrPnlManHinhKho[tab] = (JPanel) pnlKhung2.getContentPane();
                napLai[tab] = pnlKhung2::napLai;
            }
            case 1 -> {
                SanPhamUI pnlKhung2 = new SanPhamUI();
                arrPnlManHinhKho[tab] = (JPanel) pnlKhung2.getContentPane();
                napLai[tab] = pnlKhung2::napLai;
            }
            case 2 -> {
                NhapKhoUI pnlKhung2 = new NhapKhoUI(this::moTraCuuNhapKho);
                arrPnlManHinhKho[tab] = (JPanel) pnlKhung2.getContentPane();
                napLai[tab] = pnlKhung2::napLai;
            }
            default -> {
                KiemKeUI pnlKhung2 = new KiemKeUI(this::moTraCuuKiemKe);
                arrPnlManHinhKho[tab] = (JPanel) pnlKhung2.getContentPane();
                napLai[tab] = pnlKhung2::napLai;
            }
        }
    }

    private void hienManHinh(String khoa, String tieuDe, JButton btnNhomMacDinh,
            Supplier<? extends Component> tao) {

        try {

            Component c = mapManHinhKhac.get(khoa);
            if (c == null) {

                c = tao.get();

                mapManHinhKhac.put(khoa, c);

                pnlNoiDung.add(c, khoa);
            }
            chuyenManHinh(khoa, tieuDe,
                    btnNhomDaChon != null ? btnNhomDaChon : btnNhomMacDinh);
        } catch (RuntimeException ex) {
            baoLoiMoManHinh(tieuDe, ex);
        }
    }

    private void baoLoiMoManHinh(String ten, RuntimeException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Không thể mở màn hình " + ten + ".\n"
                        + "Vui lòng kiểm tra kết nối CSDL hoặc Console.\n"
                        + ex.getMessage(),
                "Pet Shop Pro - Lỗi mở màn hình", JOptionPane.ERROR_MESSAGE);
    }

    private void moLichHen() {
        hienManHinh("lichhen", "Lịch hẹn Spa", btnXuLy, QuanLyLichHenUI::new);
    }

    private void moThuCung() {
        hienManHinh("thucung", "Quản lý thú cưng", btnDanhMuc, QuanLyThuCungUI::new);
    }

    private void moKhachHang() {
        hienManHinh("khachhang", "Quản lý khách hàng", btnDanhMuc,
                () -> new QuanLyKhachHangUI(maNV));
    }

    private void moVoucher() {
        hienManHinh("voucher", "Quản lý voucher", btnDanhMuc, QuanLyVoucherUI::new);
    }

    private void moPhieuDichVu() {
        hienManHinh("phieudv", "Quản lý phiếu dịch vụ", btnXuLy,
                QuanLyPhieuDichVuUI::new);
    }

    private void moTraCuuHoaDon() {
        hienManHinh("hoadon", "Tra cứu lịch sử hóa đơn", btnTimKiem,
                () -> {
                    pnlTraCuuHoaDon = new TraCuuLichSuHoaDonUI(
                            () -> chuyenTuNhom(btnXuLy, this::moThanhToan));
                    return pnlTraCuuHoaDon.getContentPanel();
                });
        if (pnlTraCuuHoaDon != null) {
            try {
                pnlTraCuuHoaDon.hienDanhSach();
            } catch (RuntimeException ex) {
                baoLoiMoManHinh("Tra cứu lịch sử hóa đơn", ex);
            }
        }
    }

    private void moTraCuuNhapKho() {
        hienManHinh("tracuukho", "Tra cứu nhập kho", btnTimKiem,
                () -> {
                    pnlTraCuuNhapKho = new TraCuuKhoUI(false,
                            () -> chuyenTuNhom(btnXuLy, () -> chuyenKho(2)));
                    return pnlTraCuuNhapKho.getContentPane();
                });
        if (pnlTraCuuNhapKho != null)
            pnlTraCuuNhapKho.lamMoi();
    }

    private void moTraCuuKiemKe() {
        hienManHinh("tracuukkkho", "Tra cứu kiểm kê kho", btnTimKiem,
                () -> {
                    pnlTraCuuKiemKe = new TraCuuKiemKeKhoUI(
                            () -> chuyenTuNhom(btnXuLy, () -> chuyenKho(3)));
                    return pnlTraCuuKiemKe.getContentPane();
                });
        if (pnlTraCuuKiemKe != null)
            pnlTraCuuKiemKe.lamMoi();
    }

    private void moThanhToan() {
        hienManHinh("thanhtoan", "Thanh toán", btnXuLy,
                () -> {
                    pnlThanhToan = new QuanLyThanhToanUI(maNV, hoTen,
                            () -> chuyenTuNhom(btnTimKiem, this::moTraCuuHoaDon),
                            this::capNhatDanhSachPhieuDichVu);
                    return pnlThanhToan.getContentPanel();
                });
        if (pnlThanhToan != null) {
            pnlThanhToan.napDanhSachMaKhachHang();
            pnlThanhToan.napDanhSachGoiYSanPhamVaVoucher();
            pnlThanhToan.napGioHang();
        }
    }

    private void capNhatDanhSachPhieuDichVu() {
        Component manHinh = mapManHinhKhac.get("phieudv");
        if (manHinh instanceof QuanLyPhieuDichVuUI) {
            ((QuanLyPhieuDichVuUI) manHinh).loadData();
        }
    }

    public void moThanhToanTuGioHang() {
        if (TrangChuControl.gioHang().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống.", "Giỏ hàng",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        chuyenTuNhom(btnXuLy, this::moThanhToan);
    }

    private void moMaTranPhanQuyen() {
        hienManHinh("phanquyen", "Ma trận phân quyền", btnHeThong,
                () -> new MaTranPhanQuyenUI(this::moQuanLyTaiKhoan));
    }

    private void moQuanLyTaiKhoan() {
        hienManHinh("taikhoan", "Quản lý nhân viên", btnHeThong,
                QuanLyNhanVienUI::new);
    }

    private void moQuanLyPhanCa() {
        hienManHinh("phanca", "Phân ca nhân viên", btnXuLy,
                () -> {
                    QuanLyPhanCaUI pnl = new QuanLyPhanCaUI();
                    return pnl.getContentPane();
                });
    }

    private void moQuanLyCaLam() {
        hienManHinh("calam", "Quản lý ca làm", btnHeThong,
                QuanLyCaLamUI::new);
    }

    private void moDichVu() {
        hienManHinh("dichvu", "Quản lý dịch vụ", btnDanhMuc, QuanLyDichVuUI::new);
        Component manHinh = mapManHinhKhac.get("dichvu");
        if (manHinh instanceof QuanLyDichVuUI) {
            ((QuanLyDichVuUI) manHinh).lamMoi();
        }
    }

    private void chuyenTuNhom(JButton btnNhom, Runnable hanhDong) {
        btnNhomDaChon = btnNhom;

        hanhDong.run();
    }

    private boolean coMucMenu(JPopupMenu pmnMenu) {
        for (Component c : pmnMenu.getComponents())
            if (c instanceof JMenuItem item && item.isEnabled())
                return true;
        return false;
    }

    private void capNhatQuyenMenu() {
        btnHeThong.setEnabled(coMucMenu(taoMenuHeThong()));
        btnDanhMuc.setEnabled(coMucMenu(taoMenuDanhMuc()));
        btnXuLy.setEnabled(coMucMenu(taoMenuNghiepVu()));
        btnTimKiem.setEnabled(coMucMenu(taoMenuTimKiem()));
        btnThongKe.setEnabled(coMucMenu(taoMenuThongKe()));
        for (JButton btnBtn : dsBtnMucChinh) {
            if (!btnBtn.isEnabled())
                btnBtn.setToolTipText("Bạn không có quyền truy cập nhóm chức năng này");
        }
    }

    private void datMucDangChon(JButton btnDangChon) {
        for (JButton btnTam : dsBtnMucChinh) {
            boolean active = btnTam == btnDangChon;
            btnTam.setBackground(active ? new Color(48, 91, 138) : UIStyle.NAVY);
            btnTam.setForeground(Color.WHITE);
            btnTam.setFont(UIStyle.font(active ? Font.BOLD : Font.PLAIN, 12));
        }
        if (btnTrangChu != null) {
            boolean active = btnTrangChu == btnDangChon;
            btnTrangChu.setBackground(active ? new Color(48, 91, 138) : UIStyle.NAVY);
            btnTrangChu.setForeground(Color.WHITE);
            btnTrangChu.setFont(UIStyle.font(active ? Font.BOLD : Font.PLAIN, 12));
        }
    }

    private void capNhatGio() {
        if (lblNgayTrangChu != null) {
            lblNgayTrangChu.setText(LocalDateTime.now().format(DINH_DANG_GIO));
        }
    }

    private JPanel taoPnlHeader() {

        JPanel pnlHeader2 = new JPanel(new BorderLayout(8, 0));
        pnlHeader2.setBackground(UIStyle.NAVY);
        pnlHeader2.setBorder(new EmptyBorder(7, 12, 7, 12));

        JPanel pnlThuongHieu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlThuongHieu.setOpaque(false);

        JLabel lblLogo = new JLabel("PET SHOP PRO", taiIcon("/img/logo.png", 26, 26), SwingConstants.LEFT);
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(UIStyle.font(Font.BOLD, 16));
        lblLogo.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblLogo.setIconTextGap(8);
        pnlThuongHieu.add(lblLogo);
        pnlThuongHieu.setPreferredSize(new Dimension(160, 38));
        pnlHeader2.add(pnlThuongHieu, BorderLayout.WEST);

        JPanel pnlMenu = new JPanel(new GridLayout(1, 6, 2, 0));
        pnlMenu.setOpaque(false);

        btnTrangChu = taoBtnNhom("TRANG CHỦ", "/img/trangchulogo.png");

        dangKyHanhDong(btnTrangChu, this::hienTrangChu);

        btnHeThong = taoBtnNhom("HỆ THỐNG", "/img/caidatlogo.png");
        btnDanhMuc = taoBtnNhom("DANH MỤC", "/img/danhmuclogo.png");
        btnXuLy = taoBtnNhom("XỬ LÝ", "/img/nghiepvulogo.png");
        btnTimKiem = taoBtnNhom("TÌM KIẾM", "/img/timkiemlogo.png");
        btnThongKe = taoBtnNhom("THỐNG KÊ", "/img/thongkelogo.png");

        pnlMenu.add(btnTrangChu);
        pnlMenu.add(btnHeThong);
        pnlMenu.add(btnDanhMuc);
        pnlMenu.add(btnXuLy);
        pnlMenu.add(btnTimKiem);
        pnlMenu.add(btnThongKe);

        dsBtnMucChinh.add(btnHeThong);
        dsBtnMucChinh.add(btnDanhMuc);
        dsBtnMucChinh.add(btnXuLy);
        dsBtnMucChinh.add(btnTimKiem);
        dsBtnMucChinh.add(btnThongKe);

        dangKyHanhDong(btnHeThong, () -> hienPopup(btnHeThong, taoMenuHeThong()));
        dangKyHanhDong(btnDanhMuc, () -> hienPopup(btnDanhMuc, taoMenuDanhMuc()));
        dangKyHanhDong(btnXuLy, () -> hienPopup(btnXuLy, taoMenuNghiepVu()));
        dangKyHanhDong(btnTimKiem, () -> hienPopup(btnTimKiem, taoMenuTimKiem()));
        dangKyHanhDong(btnThongKe, () -> hienPopup(btnThongKe, taoMenuThongKe()));

        pnlHeader2.add(pnlMenu, BorderLayout.CENTER);

        btnNhanVien = new JButton(hoTen == null ? "Tài khoản" : hoTen);
        btnNhanVien.setFont(UIStyle.font(Font.BOLD, 12));
        btnNhanVien.setForeground(Color.WHITE);
        btnNhanVien.setBackground(UIStyle.NAVY);
        btnNhanVien.setFocusPainted(false);
        btnNhanVien.setOpaque(true);
        btnNhanVien.setBorder(new EmptyBorder(8, 6, 8, 6));
        btnNhanVien.setPreferredSize(new Dimension(170, 38));
        btnNhanVien.setToolTipText("Thông tin nhân viên / Đổi mật khẩu / Đăng xuất");
        btnNhanVien.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dangKyHanhDong(btnNhanVien, () -> hienThongTinNhanVien(btnNhanVien));
        pnlHeader2.add(btnNhanVien, BorderLayout.EAST);

        return pnlHeader2;
    }

    private JButton taoBtnNhom(String title, String iconPath) {
        JButton btnTam = new JButton(title, taiIcon(iconPath, 18, 18));
        btnTam.setFont(UIStyle.font(Font.PLAIN, 12));
        btnTam.setBackground(UIStyle.NAVY);
        btnTam.setForeground(Color.WHITE);
        btnTam.setOpaque(true);
        btnTam.setFocusPainted(false);
        btnTam.setBorder(new EmptyBorder(8, 3, 8, 3));
        btnTam.setToolTipText(title.equals("Trang chủ") ? "Về trang chủ" : "Mở menu " + title);
        btnTam.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTam.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnTam.setIconTextGap(8);
        return btnTam;
    }

    private ImageIcon taiIcon(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url == null)
            return null;

        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private void hienPopup(JButton btnNguon, JPopupMenu pmnPopMenu) {
        pmnPopMenu.show(btnNguon, 0, btnNguon.getHeight());
    }

    private void themMuc(JPopupMenu pmnPopMenu, String ten, JButton btnNhom, Runnable hanhDong) {
        PhanQuyenControl.Quyen q = PhanQuyenControl.tuTenMenu(ten);

        JMenuItem mniChucNang = new JMenuItem(ten);
        mniChucNang.setFont(UIStyle.font(Font.PLAIN, 13));
        mniChucNang.setBorder(new EmptyBorder(9, 14, 9, 14));

        mniChucNang.setEnabled(PhanQuyenControl.coQuyen(q));
        dangKyHanhDong(mniChucNang, () -> chuyenTuNhom(btnNhom, hanhDong));
        pmnPopMenu.add(mniChucNang);
    }

    private JPopupMenu taoMenuHeThong() {
        JPopupMenu pmnP = new JPopupMenu();
        themMuc(pmnP, "Quản lý nhân viên", btnHeThong, this::moQuanLyTaiKhoan);
        themMuc(pmnP, "Bảng phân quyền", btnHeThong, this::moMaTranPhanQuyen);
        themMuc(pmnP, "Quản lý ca làm", btnHeThong, this::moQuanLyCaLam);
        return pmnP;
    }

    private JPopupMenu taoMenuDanhMuc() {
        JPopupMenu pmnP = new JPopupMenu();
        themMuc(pmnP, "Danh mục sản phẩm", btnDanhMuc, () -> chuyenKho(0));
        themMuc(pmnP, "Sản phẩm", btnDanhMuc, () -> chuyenKho(1));
        themMuc(pmnP, "Khách hàng", btnDanhMuc, this::moKhachHang);
        themMuc(pmnP, "Thú cưng", btnDanhMuc, this::moThuCung);
        themMuc(pmnP, "Dịch vụ", btnDanhMuc, this::moDichVu);
        themMuc(pmnP, "Mã giảm giá", btnDanhMuc, this::moVoucher);
        return pmnP;
    }

    private JPopupMenu taoMenuNghiepVu() {
        JPopupMenu pmnP = new JPopupMenu();
        themMuc(pmnP, "Nhập kho", btnXuLy, () -> chuyenKho(2));
        themMuc(pmnP, "Kiểm kê kho", btnXuLy, () -> chuyenKho(3));
        pmnP.addSeparator();
        themMuc(pmnP, "Lịch hẹn Spa", btnXuLy, this::moLichHen);
        themMuc(pmnP, "Phiếu dịch vụ", btnXuLy, this::moPhieuDichVu);
        pmnP.addSeparator();
        themMuc(pmnP, "Gợi ý thức ăn theo loại thú cưng", btnXuLy, this::moGoiYThucAn);
        pmnP.addSeparator();
        themMuc(pmnP, "Thanh toán hóa đơn", btnXuLy, this::moThanhToan);

        pmnP.addSeparator();
        themMuc(pmnP, "Phân ca nhân viên", btnXuLy, this::moQuanLyPhanCa);

        return pmnP;
    }

    private JPopupMenu taoMenuTimKiem() {
        JPopupMenu pmnP = new JPopupMenu();
        themMuc(pmnP, "Tra cứu lịch sử hóa đơn", btnTimKiem, this::moTraCuuHoaDon);
        themMuc(pmnP, "Tra cứu nhập kho", btnTimKiem, this::moTraCuuNhapKho);
        themMuc(pmnP, "Tra cứu kiểm kê kho", btnTimKiem, this::moTraCuuKiemKe);
        themMuc(pmnP, "Tra cứu hạn sử dụng", btnTimKiem, this::moCanhBaoHanSuDung);
        return pmnP;
    }

    private JPopupMenu taoMenuThongKe() {
        JPopupMenu pmnP = new JPopupMenu();
        themMuc(pmnP, "Thống kê doanh thu", btnThongKe, this::moThongKeDoanhThu);
        return pmnP;
    }

    private void hienThongTinNhanVien(JButton btnNguon) {
        JPopupMenu pmnPopMenu = new JPopupMenu();
        JPanel pnlThongTin = new JPanel(new GridLayout(0, 2, 15, 8));
        pnlThongTin.setBorder(new EmptyBorder(12, 12, 12, 12));
        themThongTin(pnlThongTin, "Mã NV", maNV);
        themThongTin(pnlThongTin, "Họ tên", hoTen);
        themThongTin(pnlThongTin, "Vai trò", vaiTro);
        themThongTin(pnlThongTin, "Trạng thái", "Đang hoạt động");
        pmnPopMenu.add(pnlThongTin);
        pmnPopMenu.addSeparator();
        JMenuItem mniDoiMatKhau = new JMenuItem("Đổi mật khẩu");
        mniDoiMatKhau.setFont(UIStyle.font(Font.PLAIN, 13));
        mniDoiMatKhau.setBorder(new EmptyBorder(9, 14, 9, 14));
        dangKyHanhDong(mniDoiMatKhau, () -> moDlgDoiMatKhau());
        pmnPopMenu.add(mniDoiMatKhau);
        pmnPopMenu.addSeparator();
        JMenuItem mniDangXuat = new JMenuItem("Đăng xuất");
        mniDangXuat.setFont(UIStyle.font(Font.PLAIN, 13));
        mniDangXuat.setBorder(new EmptyBorder(9, 14, 9, 14));
        dangKyHanhDong(mniDangXuat, () -> dangXuat());
        pmnPopMenu.add(mniDangXuat);
        pmnPopMenu.show(btnNguon,
                -pmnPopMenu.getPreferredSize().width + btnNguon.getWidth(), btnNguon.getHeight());
    }

    private void moDlgDoiMatKhau() {

        if (maNV == null || maNV.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không xác định được phiên đăng nhập hiện tại.",
                    "Đổi mật khẩu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog winDoiMatKhau = new JDialog(this, "Pet Shop Pro - Đổi mật khẩu", true);
        winDoiMatKhau.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        winDoiMatKhau.setSize(460, 405);
        winDoiMatKhau.setMinimumSize(new Dimension(420, 385));
        winDoiMatKhau.setLocationRelativeTo(this);

        winDoiMatKhau.setLayout(new BorderLayout());

        JPanel pnlTieuDe = new JPanel(new BorderLayout(0, 5));
        pnlTieuDe.setBackground(UIStyle.NAVY);
        pnlTieuDe.setBorder(new EmptyBorder(16, 22, 16, 22));
        JLabel lblTieuDe = new JLabel("ĐỔI MẬT KHẨU");
        lblTieuDe.setForeground(Color.WHITE);
        lblTieuDe.setFont(UIStyle.font(Font.BOLD, 19));
        JLabel lblTaiKhoan = new JLabel("Tài khoản: " + maNV + " - " + (hoTen == null ? "" : hoTen));
        lblTaiKhoan.setForeground(new Color(215, 230, 245));
        lblTaiKhoan.setFont(UIStyle.font(Font.PLAIN, 12));
        pnlTieuDe.add(lblTieuDe, BorderLayout.NORTH);
        pnlTieuDe.add(lblTaiKhoan, BorderLayout.SOUTH);
        winDoiMatKhau.add(pnlTieuDe, BorderLayout.NORTH);

        JPasswordField txtMatKhauCu = new JPasswordField(21);
        JPasswordField txtMatKhauMoi = new JPasswordField(21);
        JPasswordField txtXacNhanMatKhau = new JPasswordField(21);
        JPasswordField[] arrMatKhau = { txtMatKhauCu, txtMatKhauMoi, txtXacNhanMatKhau };
        for (JPasswordField txtMatKhau : arrMatKhau) {
            txtMatKhau.setFont(UIStyle.font(Font.PLAIN, 14));
            txtMatKhau.setPreferredSize(new Dimension(280, 35));
        }

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(16, 22, 8, 22));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(3, 0, 5, 0);

        themTruongMatKhau(pnlForm, gbc, 0, "Mật khẩu hiện tại", txtMatKhauCu);
        themTruongMatKhau(pnlForm, gbc, 2, "Mật khẩu mới", txtMatKhauMoi);
        themTruongMatKhau(pnlForm, gbc, 4, "Xác nhận mật khẩu mới", txtXacNhanMatKhau);

        JCheckBox chkHienMatKhau = new JCheckBox("Hiện mật khẩu");
        chkHienMatKhau.setOpaque(false);
        chkHienMatKhau.setFont(UIStyle.font(Font.PLAIN, 12));
        char charEcho = txtMatKhauCu.getEchoChar();
        dangKyHanhDong(chkHienMatKhau, () -> {
            for (JPasswordField txtMatKhau : arrMatKhau) {
                txtMatKhau.setEchoChar(chkHienMatKhau.isSelected() ? (char) 0 : charEcho);
            }
        });
        gbc.gridy = 6;
        gbc.insets = new Insets(1, 0, 0, 0);
        pnlForm.add(chkHienMatKhau, gbc);

        JLabel lblTrangThai = new JLabel("Mật khẩu mới phải có ít nhất 6 ký tự.");
        lblTrangThai.setForeground(new Color(100, 108, 120));
        lblTrangThai.setFont(UIStyle.font(Font.PLAIN, 12));
        gbc.gridy = 7;
        gbc.insets = new Insets(5, 0, 0, 0);
        pnlForm.add(lblTrangThai, gbc);

        winDoiMatKhau.add(pnlForm, BorderLayout.CENTER);

        JButton btnHuy = UIStyle.nutPhu("Hủy");
        JButton btnLuuMatKhau = UIStyle.nutChinh("Lưu mật khẩu");
        dangKyHanhDong(btnHuy, () -> winDoiMatKhau.dispose());
        dangKyHanhDong(btnLuuMatKhau, () -> {
            char[] arrMatKhauCu = txtMatKhauCu.getPassword();
            char[] arrMatKhauMoi = txtMatKhauMoi.getPassword();
            char[] arrXacNhan = txtXacNhanMatKhau.getPassword();
            try {
                try {
                    control.kiemTraDoiMatKhau(arrMatKhauCu, arrMatKhauMoi, arrXacNhan);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(winDoiMatKhau, ex.getMessage(),
                            "Mật khẩu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                    if (ex.getMessage().startsWith("Xác nhận"))
                        txtXacNhanMatKhau.setText("");
                    return;
                }

                String strMatKhauCu = new String(arrMatKhauCu);
                String strMatKhauMoi = new String(arrMatKhauMoi);
                btnLuuMatKhau.setEnabled(false);
                btnHuy.setEnabled(false);
                lblTrangThai.setForeground(UIStyle.NAVY);
                lblTrangThai.setText("Đang cập nhật mật khẩu...");

                new SwingWorker<Boolean, Void>() {
                    @Override

                    protected Boolean doInBackground() throws Exception {
                        return control.doiMatKhau(maNV, strMatKhauCu, strMatKhauMoi);
                    }

                    @Override

                    protected void done() {
                        btnLuuMatKhau.setEnabled(true);
                        btnHuy.setEnabled(true);
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(winDoiMatKhau,
                                        "Đổi mật khẩu thành công!\nHãy sử dụng mật khẩu mới trong lần đăng nhập tiếp theo.",
                                        "Pet Shop Pro", JOptionPane.INFORMATION_MESSAGE);
                                for (JPasswordField txtMatKhau : arrMatKhau)
                                    txtMatKhau.setText("");
                                winDoiMatKhau.dispose();
                            } else {
                                lblTrangThai.setForeground(new Color(192, 55, 55));
                                lblTrangThai.setText("Mật khẩu hiện tại không đúng. Vui lòng thử lại.");
                                txtMatKhauCu.setText("");
                                txtMatKhauCu.requestFocusInWindow();
                            }
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            lblTrangThai.setForeground(new Color(192, 55, 55));
                            lblTrangThai.setText("Thao tác bị gián đoạn. Vui lòng thử lại.");
                        } catch (ExecutionException ex) {
                            ex.printStackTrace();
                            lblTrangThai.setForeground(new Color(192, 55, 55));
                            lblTrangThai.setText("Không cập nhật được. Hãy kiểm tra kết nối SQL Server.");
                        }
                    }
                }.execute();
            } finally {
                Arrays.fill(arrMatKhauCu, '\0');
                Arrays.fill(arrMatKhauMoi, '\0');
                Arrays.fill(arrXacNhan, '\0');
            }
        });

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        pnlNut.setBackground(Color.WHITE);
        pnlNut.setBorder(new EmptyBorder(0, 12, 2, 12));
        pnlNut.add(btnHuy);
        pnlNut.add(btnLuuMatKhau);
        winDoiMatKhau.add(pnlNut, BorderLayout.SOUTH);
        winDoiMatKhau.getRootPane().setDefaultButton(btnLuuMatKhau);
        SwingUtilities.invokeLater(txtMatKhauCu::requestFocusInWindow);
        winDoiMatKhau.setVisible(true);
    }

    private void themTruongMatKhau(JPanel pnlForm, GridBagConstraints gbc,
            int intDong, String strNhan, JPasswordField txtMatKhau) {
        JLabel lblNhan = new JLabel(strNhan);
        lblNhan.setFont(UIStyle.font(Font.BOLD, 12));
        gbc.gridy = intDong;
        gbc.insets = new Insets(3, 0, 4, 0);
        pnlForm.add(lblNhan, gbc);
        gbc.gridy = intDong + 1;
        gbc.insets = new Insets(0, 0, 8, 0);
        pnlForm.add(txtMatKhau, gbc);
    }

    private void themThongTin(JPanel pnlPanel, String lblNhan, String lblGiaTri) {
        JLabel lblThongTin = new JLabel(lblNhan);
        lblThongTin.setFont(UIStyle.font(Font.BOLD, 12));
        pnlPanel.add(lblThongTin);
        pnlPanel.add(new JLabel(lblGiaTri != null ? lblGiaTri : ""));
    }

    private void dangXuat() {
        TrangChuControl.dat(null, null, null);
        dispose();
        new DangNhapUI().setVisible(true);
    }

    private void lamMoiTrangChu() {
        capNhatGio();
        napLichSpaSapToi();

        napTongQuanHomNay();
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
    }

    private JPanel taoPnlTrangChu() {

        JPanel pnlChinh = new JPanel(new BorderLayout());
        pnlChinh.setBackground(LIGHT_BLUE);
        pnlChinh.add(taoPnlThanhTieuDe(), BorderLayout.NORTH);

        JPanel pnlThan = new JPanel(new BorderLayout(0, 14));
        pnlThan.setBackground(LIGHT_BLUE);
        pnlThan.setBorder(new EmptyBorder(16, 28, 16, 28));

        JPanel pnlTren = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlTren.setOpaque(false);
        pnlTren.add(taoPnlGioiThieu());
        pnlTren.add(taoPnlTruyCapNhanh());

        JPanel pnlDuoi = taoPnlTienIchDuoi();
        pnlDuoi.setPreferredSize(new Dimension(0, 200));

        pnlThan.add(pnlTren, BorderLayout.CENTER);
        pnlThan.add(pnlDuoi, BorderLayout.SOUTH);
        pnlChinh.add(pnlThan, BorderLayout.CENTER);
        return pnlChinh;
    }

    private JPanel taoPnlThanhTieuDe() {
        JPanel pnlPanel = new JPanel(new BorderLayout());
        pnlPanel.setBackground(UIStyle.BAR);
        pnlPanel.setBorder(new EmptyBorder(10, 25, 10, 25));

        JLabel lblNgayHienTai = new JLabel(LocalDateTime.now().format(DINH_DANG_GIO));
        lblNgayTrangChu = lblNgayHienTai;
        lblNgayHienTai.setFont(UIStyle.font(Font.BOLD, 12));

        JLabel lblTieuDeTrangChu = new JLabel("TRANG CHỦ", SwingConstants.CENTER);
        lblTieuDeTrangChu.setFont(UIStyle.font(Font.BOLD, 25));

        JPanel pnlThaoTac = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlThaoTac.setOpaque(false);

        btnLamMoi = UIStyle.nutPhu(" Làm mới");
        btnLamMoi.setPreferredSize(new Dimension(135, 38));
        dangKyHanhDong(btnLamMoi, this::lamMoiTrangChu);

        btnHoTro = UIStyle.nutPhu("? Hỗ trợ");
        btnHoTro.setPreferredSize(new Dimension(125, 38));
        dangKyHanhDong(btnHoTro, this::moModalHoTro);

        pnlThaoTac.add(btnLamMoi);
        pnlThaoTac.add(btnHoTro);

        pnlPanel.add(lblNgayHienTai, BorderLayout.WEST);
        pnlPanel.add(lblTieuDeTrangChu, BorderLayout.CENTER);
        pnlPanel.add(pnlThaoTac, BorderLayout.EAST);
        return pnlPanel;
    }

    private JPanel taoPnlGioiThieu() {
        JPanel pnlPanel = new JPanel(new BorderLayout());
        pnlPanel.setBackground(Color.WHITE);

        ImageIcon anhGioiThieu = taiAnh("/img/giothieutrangchu.png");
        if (anhGioiThieu != null) {
            JLabel lblAnhGioiThieu = new JLabel("", SwingConstants.CENTER);
            lblAnhGioiThieu.setOpaque(true);
            lblAnhGioiThieu.setBackground(Color.WHITE);
            pnlPanel.add(lblAnhGioiThieu, BorderLayout.CENTER);
            pnlPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    Dimension kichThuoc = pnlPanel.getSize();
                    Insets vien = pnlPanel.getInsets();
                    int rong = Math.max(1, kichThuoc.width - vien.left - vien.right);
                    int cao = Math.max(1, kichThuoc.height - vien.top - vien.bottom);
                    lblAnhGioiThieu.setIcon(phongToDayAnh(anhGioiThieu, rong, cao));
                }
            });
            return pnlPanel;
        }

        JLabel lblChaoMung = new JLabel("PET SHOP PRO", SwingConstants.CENTER);
        lblChaoMung.setFont(UIStyle.font(Font.BOLD, 30));
        lblChaoMung.setForeground(UIStyle.NAVY);
        pnlPanel.add(lblChaoMung, BorderLayout.CENTER);
        return pnlPanel;
    }

    private ImageIcon taiAnh(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url != null) {
            return new ImageIcon(url);
        }

        java.io.File tep = new java.io.File("src", path.replace('/', java.io.File.separatorChar).substring(1));
        if (tep.isFile()) {
            return new ImageIcon(tep.getAbsolutePath());
        }
        return null;
    }

    private ImageIcon phongToDayAnh(ImageIcon icon, int rong, int cao) {
        int rongGoc = icon.getIconWidth();
        int caoGoc = icon.getIconHeight();
        if (rongGoc <= 0 || caoGoc <= 0) {
            return icon;
        }

        double tyLe = Math.max((double) rong / rongGoc, (double) cao / caoGoc);
        int rongMoi = Math.max(1, (int) Math.round(rongGoc * tyLe));
        int caoMoi = Math.max(1, (int) Math.round(caoGoc * tyLe));
        Image anh = icon.getImage().getScaledInstance(rongMoi, caoMoi, Image.SCALE_SMOOTH);
        return new ImageIcon(anh);
    }

    private JPanel taoPnlTruyCapNhanh() {
        JPanel pnlPanel = new JPanel();
        pnlPanel.setBackground(Color.WHITE);
        pnlPanel.setBorder(new CompoundBorder(new LineBorder(UIStyle.LINE), new EmptyBorder(15, 15, 15, 15)));

        pnlPanel.setLayout(new BoxLayout(pnlPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle2 = new JLabel("TRUY CẬP NHANH");
        lblTitle2.setFont(UIStyle.font(Font.BOLD, 18));
        lblTitle2.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlPanel.add(lblTitle2);
        pnlPanel.add(Box.createVerticalStrut(15));

        themBtnTruyCapNhanh(pnlPanel, "Quản lý khách hàng", "F4",
                PhanQuyenControl.Quyen.KHACH_HANG,
                () -> chuyenTuNhom(btnDanhMuc, this::moKhachHang));
        themBtnTruyCapNhanh(pnlPanel, "Quản lý lịch hẹn", "F5",
                PhanQuyenControl.Quyen.LICH_HEN_SPA,
                () -> chuyenTuNhom(btnXuLy, this::moLichHen));
        themBtnTruyCapNhanh(pnlPanel, "Phiếu dịch vụ", "F6",
                PhanQuyenControl.Quyen.PHIEU_DICH_VU,
                () -> chuyenTuNhom(btnXuLy, this::moPhieuDichVu));
        themBtnTruyCapNhanh(pnlPanel, "Thanh toán", "F7",
                PhanQuyenControl.Quyen.THANH_TOAN,
                () -> chuyenTuNhom(btnXuLy, this::moThanhToan));
        themBtnTruyCapNhanh(pnlPanel, "Tra cứu hóa đơn", "F8",
                PhanQuyenControl.Quyen.TRA_CUU_HOA_DON,
                () -> chuyenTuNhom(btnTimKiem, this::moTraCuuHoaDon));

        return pnlPanel;
    }

    private JPanel taoPnlTienIchDuoi() {
        JPanel pnlPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlPanel.setOpaque(false);
        pnlPanel.add(taoPnlLichSpaSapToi());
        pnlPanel.add(taoPnlTongQuan());
        return pnlPanel;
    }

    private JPanel taoPnlTongQuan() {
        JPanel pnlTongQuan = new JPanel(new BorderLayout(0, 14));
        pnlTongQuan.setBackground(Color.WHITE);
        pnlTongQuan.setBorder(new CompoundBorder(new LineBorder(UIStyle.LINE), new EmptyBorder(14, 16, 14, 16)));

        JLabel lblTieuDeTongQuan = new JLabel("Tổng quan hôm nay", taiIcon("/img/thongkelogo.png", 18, 18),
                SwingConstants.LEFT);
        lblTieuDeTongQuan.setFont(UIStyle.font(Font.BOLD, 17));
        lblTieuDeTongQuan.setForeground(new Color(40, 55, 77));
        lblTieuDeTongQuan.setIconTextGap(8);
        pnlTongQuan.add(lblTieuDeTongQuan, BorderLayout.NORTH);

        JPanel pnlLuoi = new JPanel(new GridLayout(2, 2, 12, 12));
        pnlLuoi.setOpaque(false);

        lblDoanhThuHomNay = taoGiaTriTongQuan("—");
        lblLuotMuaHomNay = taoGiaTriTongQuan("—");
        lblLichSpaHomNay = taoGiaTriTongQuan("—");
        lblPhieuNhapHomNay = taoGiaTriTongQuan("—");

        pnlLuoi.add(taoTheTongQuan("Doanh thu", lblDoanhThuHomNay));
        pnlLuoi.add(taoTheTongQuan("Lượt mua", lblLuotMuaHomNay));
        pnlLuoi.add(taoTheTongQuan("Lịch Spa", lblLichSpaHomNay));
        pnlLuoi.add(taoTheTongQuan("Phiếu nhập", lblPhieuNhapHomNay));

        pnlTongQuan.add(pnlLuoi, BorderLayout.CENTER);

        napTongQuanHomNay();
        return pnlTongQuan;
    }

    private JPanel taoTheTongQuan(String tieuDe, JLabel lblGiaTri2) {
        JPanel pnlKhung = new JPanel(new BorderLayout(0, 6));

        pnlKhung.setBackground(new Color(245, 248, 252));
        pnlKhung.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(UIStyle.font(Font.PLAIN, 12));

        lblGiaTri2.setFont(UIStyle.font(Font.BOLD, 20));
        lblGiaTri2.setForeground(new Color(18, 48, 82));

        pnlKhung.add(lblTieuDe, BorderLayout.NORTH);
        pnlKhung.add(lblGiaTri2, BorderLayout.CENTER);

        return pnlKhung;
    }

    private JLabel taoGiaTriTongQuan(String text) {
        JLabel lblTam = new JLabel(text);
        lblTam.setFont(UIStyle.font(Font.BOLD, 23));
        lblTam.setForeground(new Color(38, 45, 57));
        return lblTam;
    }

    private void moCanhBaoHanSuDung() {
        hienManHinh("canhbaohansudung", "Tra cứu hạn sử dụng sản phẩm", btnTimKiem,
                () -> {
                    pnlCanhBaoHanSuDung = new CanhBaoHanSuDungUI();
                    return pnlCanhBaoHanSuDung.getContentPane();
                });
        if (pnlCanhBaoHanSuDung != null) {
            pnlCanhBaoHanSuDung.napLai();
        }
    }

    private void moGoiYThucAn() {
        hienManHinh("goiythucan", "Gợi ý thức ăn theo loại thú cưng", btnXuLy,
                () -> {
                    pnlGoiYThucAn = new GoiYThucAnUI(this::moThanhToanTuGioHang);
                    return pnlGoiYThucAn.getContentPane();
                });
        if (pnlGoiYThucAn != null) {
            pnlGoiYThucAn.napLai();
        }
    }

    private void napTongQuanHomNay() {
        if (lblDoanhThuHomNay == null)
            return;

        lblDoanhThuHomNay.setText("Không có quyền");
        lblLuotMuaHomNay.setText("—");
        lblLichSpaHomNay.setText("—");
        lblPhieuNhapHomNay.setText("—");

        if (PhanQuyenControl.coQuyen(PhanQuyenControl.Quyen.THONG_KE_DOANH_THU)) {
            try {
                TrangChuControl.ThongKeHoaDon tk = control.thongKeHoaDon();
                lblDoanhThuHomNay.setText(UIStyle.tien(tk.getDoanhThuHomNay()));
                lblLuotMuaHomNay.setText(String.valueOf(tk.getHoaDonHomNay()));
            } catch (Exception ex) {
                lblDoanhThuHomNay.setText("—");
            }
        }
        if (PhanQuyenControl.coQuyen(PhanQuyenControl.Quyen.LICH_HEN_SPA)) {
            try {
                lblLichSpaHomNay.setText(String.valueOf(control.demLichSpaHomNay()));
            } catch (SQLException ex) {
                lblLichSpaHomNay.setText("—");
            }
        }
        if (PhanQuyenControl.coQuyen(PhanQuyenControl.Quyen.NHAP_KHO)) {
            try {
                lblPhieuNhapHomNay.setText(String.valueOf(control.demPhieuNhapHomNay()));
            } catch (SQLException ex) {
                lblPhieuNhapHomNay.setText("—");
            }
        }
    }

    private JPanel taoPnlLichSpaSapToi() {
        JPanel pnlCard = new JPanel(new BorderLayout(0, 5));
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setBorder(new CompoundBorder(new LineBorder(UIStyle.LINE), new EmptyBorder(10, 14, 10, 14)));

        JPanel pnlTren = new JPanel(new BorderLayout());
        pnlTren.setOpaque(false);

        JPanel pnlBocTieuDe = new JPanel();
        pnlBocTieuDe.setOpaque(false);
        pnlBocTieuDe.setLayout(new BoxLayout(pnlBocTieuDe, BoxLayout.Y_AXIS));

        JLabel lblTitle2 = new JLabel("Lịch Spa sắp tới", taiIcon("/img/lichlogo.png", 18, 18), SwingConstants.LEFT);
        lblTitle2.setFont(UIStyle.font(Font.BOLD, 17));
        lblTitle2.setForeground(new Color(40, 55, 77));
        lblTitle2.setIconTextGap(8);

        lblPhuDeLichSpa = new JLabel("Đang tải dữ liệu lịch hẹn...");
        lblPhuDeLichSpa.setFont(UIStyle.font(Font.PLAIN, 12));
        lblPhuDeLichSpa.setForeground(Color.GRAY);

        pnlBocTieuDe.add(lblTitle2);
        pnlBocTieuDe.add(Box.createVerticalStrut(2));
        pnlBocTieuDe.add(lblPhuDeLichSpa);
        pnlTren.add(pnlBocTieuDe, BorderLayout.WEST);

        pnlCard.add(pnlTren, BorderLayout.NORTH);

        pnlLichSpaNoiDung = new JPanel();
        pnlLichSpaNoiDung.setOpaque(false);
        pnlLichSpaNoiDung.setLayout(new BoxLayout(pnlLichSpaNoiDung, BoxLayout.Y_AXIS));
        pnlCard.add(pnlLichSpaNoiDung, BorderLayout.CENTER);

        napLichSpaSapToi();
        return pnlCard;
    }

    private void napLichSpaSapToi() {
        if (pnlLichSpaNoiDung == null || lblPhuDeLichSpa == null)
            return;
        if (!PhanQuyenControl.coQuyen(PhanQuyenControl.Quyen.LICH_HEN_SPA)) {
            pnlLichSpaNoiDung.removeAll();
            lblPhuDeLichSpa.setText("Không có quyền xem lịch Spa");

            pnlLichSpaNoiDung.revalidate();

            pnlLichSpaNoiDung.repaint();
            return;
        }

        pnlLichSpaNoiDung.removeAll();
        LocalDateTime now = LocalDateTime.now();
        try {

            List<LichHen> lichHomNay = control.lichSpaHomNay(now);
            List<LichHen> lichConLai = control.lichSpaConLai(lichHomNay, now);
            long tongLichHomNay = lichHomNay.size();

            if (!lichConLai.isEmpty()) {
                lblPhuDeLichSpa.setText("Các lịch còn lại trong hôm nay");

                for (LichHen lh : lichConLai.stream().limit(3).toList()) {
                    pnlLichSpaNoiDung.add(taoDongLichSpa(lh));
                    pnlLichSpaNoiDung.add(Box.createVerticalStrut(3));
                }
            } else {
                lblPhuDeLichSpa.setText(tongLichHomNay == 0
                        ? "Hôm nay không có lịch Spa"
                        : "Không còn lịch Spa nào sắp tới hôm nay");
                JLabel lblRong = new JLabel(tongLichHomNay == 0
                        ? "Chưa có lịch Spa được đặt trong ngày hôm nay."
                        : "Các lịch Spa của hôm nay đều đã qua giờ hẹn.");
                lblRong.setFont(UIStyle.font(Font.PLAIN, 13));
                lblRong.setForeground(Color.GRAY);
                pnlLichSpaNoiDung.add(lblRong);
            }
            pnlLichSpaNoiDung.add(Box.createVerticalGlue());
            pnlLichSpaNoiDung.add(taoChipTongKet(
                    "Hôm nay: " + tongLichHomNay + " lịch Spa • Còn: " + lichConLai.size() + " lịch"));
        } catch (Exception ex) {
            lblPhuDeLichSpa.setText("Không đọc được dữ liệu lịch hẹn");
            JLabel lblLoi = new JLabel("Vui lòng kiểm tra kết nối CSDL hoặc dữ liệu Lịch hẹn Spa.");
            lblLoi.setFont(UIStyle.font(Font.PLAIN, 13));
            lblLoi.setForeground(new Color(170, 60, 60));
            pnlLichSpaNoiDung.add(lblLoi);
        }

        pnlLichSpaNoiDung.revalidate();

        pnlLichSpaNoiDung.repaint();
    }

    private JComponent taoDongLichSpa(LichHen lh) {
        String gio = lh.getNgayGioHen() == null ? "--:--" : lh.getNgayGioHen().format(DINH_DANG_GIO_HEN);
        String thuCung = khongRong(lh.getTenThuCung(), "Thú cưng");
        String ghiChu = khongRong(lh.getGhiChu(), "Chăm sóc Spa");
        String strNoiDung = gio + "  ·  " + thuCung + "  ·  " + ghiChu;
        if (strNoiDung.length() > 62)
            strNoiDung = strNoiDung.substring(0, 59) + "...";
        JLabel lblThongTin = new JLabel(strNoiDung);
        lblThongTin.setToolTipText(gio + " - " + thuCung + " - " + ghiChu);
        lblThongTin.setFont(UIStyle.font(Font.PLAIN, 12));
        lblThongTin.setForeground(new Color(45, 56, 70));
        lblThongTin.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblThongTin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return lblThongTin;
    }

    private String khongRong(String s, String macDinh) {
        return (s == null || s.trim().isEmpty()) ? macDinh : s.trim();
    }

    private JComponent taoChipTongKet(String text) {
        JLabel lblTam = new JLabel(text);
        lblTam.setOpaque(true);
        lblTam.setBackground(new Color(223, 245, 232));
        lblTam.setForeground(new Color(28, 123, 81));
        lblTam.setFont(UIStyle.font(Font.BOLD, 11));
        lblTam.setBorder(new CompoundBorder(new LineBorder(new Color(200, 233, 216)), new EmptyBorder(4, 10, 4, 10)));
        JPanel pnlBoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBoc.setOpaque(false);
        pnlBoc.add(lblTam);
        pnlBoc.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pnlBoc;
    }

    private void moModalHoTro() {
        JDialog winHoTro = new JDialog(this, "Hỗ trợ sử dụng Pet Shop Pro", true);
        winHoTro.setSize(760, 560);
        winHoTro.setLocationRelativeTo(this);
        winHoTro.setLayout(new BorderLayout(0, 10));
        JPanel pnlTieuDeHoTro = new JPanel(new BorderLayout());
        pnlTieuDeHoTro.setBackground(UIStyle.NAVY);
        pnlTieuDeHoTro.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel lblTitle2 = new JLabel("HỖ TRỢ - TỔNG QUAN CHỨC NĂNG", taiIcon("/img/logo.png", 24, 24),
                SwingConstants.LEFT);
        lblTitle2.setForeground(Color.WHITE);
        lblTitle2.setFont(UIStyle.font(Font.BOLD, 18));
        lblTitle2.setIconTextGap(10);
        pnlTieuDeHoTro.add(lblTitle2, BorderLayout.WEST);
        winHoTro.add(pnlTieuDeHoTro, BorderLayout.NORTH);

        JEditorPane edtEdpHuongDan = new JEditorPane();
        edtEdpHuongDan.setContentType("text/html");
        edtEdpHuongDan.setEditable(false);
        edtEdpHuongDan.setText(
                """
                        <html>
                        <body style='font-family:Segoe UI; font-size:12px; padding:8px 10px;'>
                        <h2 style='color:#123052; margin-top:0;'>Pet Shop Pro</h2>
                        <p>Màn hình trung tâm để thao tác các chức năng của cửa hàng.</p>
                        <ul>
                          <li><b>Trang chủ:</b> màn hình tổng quan, truy cập nhanh, lịch Spa sắp tới và hỗ trợ sử dụng.</li>
                          <li><b>Hệ thống:</b> tài khoản, ca làm và phân quyền.</li>
                          <li><b>Danh mục:</b> danh mục, sản phẩm, khách hàng, thú cưng và dịch vụ.</li>
                          <li><b>Nghiệp vụ:</b> nhập kho, kiểm kê, lịch hẹn, phiếu dịch vụ và thanh toán.</li>
                          <li><b>Tìm kiếm:</b> tra cứu hóa đơn, nhập kho và kiểm kê.</li>
                          <li><b>Thống kê:</b> doanh thu và lượt mua.</li>
                        </ul>
                        <h3 style='color:#123052;'>Gợi ý sử dụng</h3>
                        <ol>
                          <li>Chọn menu chức năng trên thanh điều hướng để mở màn hình tương ứng.</li>
                          <li>Dùng truy cập nhanh khi cần mở nhanh thao tác thường dùng.</li>
                          <li>Nhấn tên nhân viên ở góc trên bên phải để xem thông tin đăng nhập hoặc đăng xuất.</li>
                        </ol>
                        </body>
                        </html>
                        """);
        edtEdpHuongDan.setCaretPosition(0);

        JScrollPane scrHuongDan = new JScrollPane(edtEdpHuongDan);
        scrHuongDan.setBorder(new EmptyBorder(0, 12, 0, 12));
        winHoTro.add(scrHuongDan, BorderLayout.CENTER);

        JButton btnDongHoTro = UIStyle.nutChinh("Đóng");
        dangKyHanhDong(btnDongHoTro, () -> winHoTro.dispose());
        JPanel pnlDuoi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        pnlDuoi.setBackground(Color.WHITE);
        pnlDuoi.add(btnDongHoTro);
        winHoTro.add(pnlDuoi, BorderLayout.SOUTH);

        winHoTro.setVisible(true);
    }

    private void themBtnTruyCapNhanh(JPanel pnlParent, String text, String shortcut,
            PhanQuyenControl.Quyen quyen, Runnable hanhDong) {
        JPanel pnlDong = new JPanel(new BorderLayout());
        pnlDong.setBackground(MENU_BLUE);
        pnlDong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JButton btnTruyCap = new JButton(text);
        btnTruyCap.setHorizontalAlignment(SwingConstants.LEFT);
        btnTruyCap.setFont(UIStyle.font(Font.PLAIN, 14));
        btnTruyCap.setBackground(MENU_BLUE);
        btnTruyCap.setFocusPainted(false);
        btnTruyCap.setBorderPainted(false);
        btnTruyCap.setEnabled(PhanQuyenControl.coQuyen(quyen));
        if (!btnTruyCap.isEnabled())
            btnTruyCap.setToolTipText("Vai trò hiện tại không có quyền");
        dangKyHanhDong(btnTruyCap, hanhDong);

        String khoa = "truycapnhanh-" + shortcut;

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(shortcut), khoa);

        getRootPane().getActionMap().put(khoa, new AbstractAction() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {

                if (btnTruyCap.isEnabled())
                    hanhDong.run();
            }
        });

        JLabel lblPhimTat = new JLabel(shortcut);
        lblPhimTat.setFont(UIStyle.font(Font.BOLD, 14));
        lblPhimTat.setBorder(new EmptyBorder(0, 0, 0, 10));

        pnlDong.add(btnTruyCap, BorderLayout.CENTER);
        pnlDong.add(lblPhimTat, BorderLayout.EAST);

        pnlParent.add(pnlDong);
        pnlParent.add(Box.createVerticalStrut(5));
    }

    public static final class UIStyle {
        public static final Color NAVY = new Color(18, 48, 82);
        public static final Color LIGHT_BLUE = new Color(229, 241, 252);
        public static final Color MENU_BLUE = new Color(207, 222, 235);
        public static final Color ACTIVE_BLUE = new Color(169, 194, 218);
        public static final Color GREEN = new Color(72, 196, 62);
        public static final Color RED = new Color(200, 40, 40);
        public static final Color LINE = new Color(210, 218, 226);
        public static final Color BAR = new Color(247, 249, 251);
        public static final Color READONLY = new Color(235, 235, 235);

        private UIStyle() {
        }

        public static Font font(int style, int size) {
            return new Font("Segoe UI", style, size);
        }

        private static final DecimalFormat DINH_DANG_TIEN;

        static {
            DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
            s.setGroupingSeparator('.');
            DINH_DANG_TIEN = new DecimalFormat("#,##0", s);
        }

        public static String tien(double v) {
            return DINH_DANG_TIEN.format(v) + " ₫";
        }

        public static long docTien(String s) {
            String sach = s == null ? "" : s.replaceAll("[\\s.,₫dđ]", "");
            if (!sach.matches("\\d{1,15}")) {
                throw new NumberFormatException(s);
            }
            return Long.parseLong(sach);
        }

        private static JButton nut(String text, Color nen, Color chu, Color vien) {
            JButton btnTam = new JButton(text);
            btnTam.setFont(font(Font.BOLD, 13));
            btnTam.setBackground(nen);
            btnTam.setForeground(chu);
            btnTam.setOpaque(true);
            btnTam.setContentAreaFilled(true);
            btnTam.setFocusPainted(false);
            btnTam.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (vien != null) {
                btnTam.setBorder(new CompoundBorder(new LineBorder(vien), new EmptyBorder(8, 18, 8, 18)));
            } else {
                btnTam.setBorder(new EmptyBorder(9, 19, 9, 19));
            }
            return btnTam;
        }

        public static JButton nutChinh(String text) {
            return nut(text, NAVY, Color.WHITE, null);
        }

        public static JButton nutXanh(String text) {
            return nut(text, GREEN, Color.WHITE, null);
        }

        public static JButton nutPhu(String text) {
            return nut(text, Color.WHITE, NAVY, NAVY);
        }

        public static JButton nutXoa(String text) {
            return nut(text, Color.WHITE, RED, LINE);
        }

        public static JButton nutTab(String text, boolean dangChon) {
            return nut(text, dangChon ? ACTIVE_BLUE : Color.WHITE,
                    dangChon ? Color.WHITE : NAVY, NAVY);
        }

        public static JLabel nhan(String text) {
            JLabel lblNhan3 = new JLabel(text);
            lblNhan3.setFont(font(Font.BOLD, 13));
            return lblNhan3;
        }

        public static JTextField o(int cot) {
            JTextField txtVanBan = new JTextField(cot);
            txtVanBan.setFont(font(Font.PLAIN, 13));
            return txtVanBan;
        }

        public static JTextField oChiDoc(String lblGiaTri) {
            JTextField txtVanBan = o(10);
            txtVanBan.setText(lblGiaTri);
            txtVanBan.setEditable(false);
            txtVanBan.setBackground(READONLY);
            return txtVanBan;
        }

        public static void hang(JPanel pnlForm2, int dong, String ten, JComponent thanhPhan, boolean cao) {
            GridBagConstraints g = new GridBagConstraints();
            g.gridy = dong;
            g.insets = new Insets(6, 0, 6, 15);
            g.anchor = cao ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;

            g.gridx = 0;
            g.weightx = 0;
            pnlForm2.add(nhan(ten), g);

            g.gridx = 1;
            g.weightx = 1;
            g.insets = new Insets(6, 0, 6, 0);
            g.fill = GridBagConstraints.HORIZONTAL;
            pnlForm2.add(thanhPhan, g);
        }

        public static JPanel the() {
            JPanel pnlKetQua = new JPanel();
            pnlKetQua.setBackground(Color.WHITE);
            pnlKetQua.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(15, 15, 15, 15)));
            return pnlKetQua;
        }

        public static JPanel theThongKe(String tieuDe, JLabel lblGiaTri) {
            JPanel pnlKetQua = new JPanel();
            pnlKetQua.setLayout(new BoxLayout(pnlKetQua, BoxLayout.Y_AXIS));
            pnlKetQua.setBackground(Color.WHITE);
            pnlKetQua.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(14, 18, 14, 18)));

            JLabel lblTam = new JLabel(tieuDe);
            lblTam.setForeground(Color.GRAY);
            lblTam.setFont(font(Font.PLAIN, 13));
            lblTam.setAlignmentX(Component.LEFT_ALIGNMENT);

            lblGiaTri.setFont(font(Font.BOLD, 26));
            lblGiaTri.setAlignmentX(Component.LEFT_ALIGNMENT);

            pnlKetQua.add(lblTam);
            pnlKetQua.add(Box.createVerticalStrut(6));
            pnlKetQua.add(lblGiaTri);
            return pnlKetQua;
        }

        public static void taoBang(JTable tblDuLieu2, int... cotCanPhai) {
            tblDuLieu2.setRowHeight(32);
            tblDuLieu2.setFont(font(Font.PLAIN, 13));
            tblDuLieu2.setShowGrid(false);
            tblDuLieu2.setShowHorizontalLines(true);
            tblDuLieu2.setGridColor(new Color(225, 230, 236));
            tblDuLieu2.setSelectionBackground(MENU_BLUE);
            tblDuLieu2.setSelectionForeground(Color.BLACK);
            tblDuLieu2.setFillsViewportHeight(true);
            tblDuLieu2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JTableHeader tbhHeader = tblDuLieu2.getTableHeader();
            tbhHeader.setReorderingAllowed(false);
            tbhHeader.setPreferredSize(new Dimension(0, 40));
            tbhHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tblDuLieu3, Object v, boolean sel, boolean foc,
                        int r,
                        int c) {
                    super.getTableCellRendererComponent(tblDuLieu3, v, false, false, r, c);
                    setOpaque(true);
                    setBackground(NAVY);
                    setForeground(Color.WHITE);
                    setFont(font(Font.BOLD, 13));
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setBorder(new javax.swing.border.MatteBorder(0, 0, 0, 1, Color.WHITE));
                    return this;
                }
            });

            tblDuLieu2.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tblDuLieu3, Object v, boolean sel, boolean foc,
                        int r,
                        int c) {
                    super.getTableCellRendererComponent(tblDuLieu3, v, sel, false, r, c);
                    setBorder(new EmptyBorder(0, 10, 0, 10));
                    boolean phai = false;
                    for (int x : cotCanPhai) {
                        if (x == c) {
                            phai = true;
                            break;
                        }
                    }
                    setHorizontalAlignment(phai ? SwingConstants.RIGHT : SwingConstants.LEFT);
                    return this;
                }
            });
        }

        public static JScrollPane cuon(JTable tblDuLieu2) {
            JScrollPane scrHuongDan = new JScrollPane(tblDuLieu2);
            scrHuongDan.setBorder(new LineBorder(LINE));
            scrHuongDan.getViewport().setBackground(Color.WHITE);
            return scrHuongDan;
        }

        public static void thongBao(Component cha, String strNoiDung) {
            JOptionPane.showMessageDialog(cha, strNoiDung, "Pet Shop Pro", JOptionPane.INFORMATION_MESSAGE);
        }

        public static void xuatPdf(Component cha, String tieuDe, JTable tblDuLieu2) {

            JFileChooser fchChonTep = new JFileChooser();
            fchChonTep.setDialogTitle("Lưu " + tieuDe + " PDF");
            fchChonTep.setSelectedFile(new java.io.File(
                    tieuDe.trim().replaceAll("[^\\p{L}\\p{N}]+", "-") + ".pdf"));
            fchChonTep.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Tệp PDF (*.pdf)", "pdf"));
            if (fchChonTep.showSaveDialog(cha) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            java.io.File file = fchChonTep.getSelectedFile();
            if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                file = new java.io.File(file.getParentFile(), file.getName() + ".pdf");
            }
            try {

                Files.write(file.toPath(), taoPdfBang(tieuDe, tblDuLieu2));
                JOptionPane.showMessageDialog(cha, "Đã xuất PDF:\n" + file.getAbsolutePath(),
                        "Xuất PDF", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(cha, "Không thể xuất PDF.\n" + ex.getMessage(),
                        "Lỗi xuất PDF", JOptionPane.ERROR_MESSAGE);
            }
        }

        private static byte[] taoPdfBang(String tieuDe, JTable tblDuLieu2) throws IOException {
            int tableWidth = Math.max(1, tblDuLieu2.getPreferredSize().width);
            int tableHeight = Math.max(1, tblDuLieu2.getTableHeader().getPreferredSize().height
                    + tblDuLieu2.getRowCount() * tblDuLieu2.getRowHeight());
            int imageWidth = 1400;
            int imageHeight = Math.max(120, (int) Math.ceil(
                    (double) tableHeight * imageWidth / tableWidth) + 90);

            BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            Dimension oldTableSize = tblDuLieu2.getSize();
            Dimension oldHeaderSize = tblDuLieu2.getTableHeader().getSize();
            try {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, imageWidth, imageHeight);
                g.scale((double) imageWidth / tableWidth, (double) imageWidth / tableWidth);
                g.setColor(NAVY);
                g.setFont(new Font("Segoe UI", Font.BOLD, 20));
                g.drawString(tieuDe, 0, 25);
                int headerHeight = tblDuLieu2.getTableHeader().getPreferredSize().height;
                tblDuLieu2.getTableHeader().setSize(tableWidth, headerHeight);
                g.translate(0, 40);
                tblDuLieu2.getTableHeader().paint(g);
                g.translate(0, headerHeight);
                tblDuLieu2.setSize(tableWidth, tableHeight - headerHeight);
                tblDuLieu2.doLayout();
                tblDuLieu2.paint(g);
            } finally {
                g.dispose();
                tblDuLieu2.setSize(oldTableSize);
                tblDuLieu2.getTableHeader().setSize(oldHeaderSize);
            }

            ByteArrayOutputStream compressed = new ByteArrayOutputStream();

            try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressed)) {
                int[] pixels = new int[imageWidth];
                byte[] row = new byte[imageWidth * 3];
                for (int y = 0; y < imageHeight; y++) {
                    image.getRGB(0, y, imageWidth, 1, pixels, 0, imageWidth);
                    for (int x = 0; x < imageWidth; x++) {
                        int rgb = pixels[x];
                        row[x * 3] = (byte) (rgb >> 16);
                        row[x * 3 + 1] = (byte) (rgb >> 8);
                        row[x * 3 + 2] = (byte) rgb;
                    }
                    deflater.write(row);
                }
            }

            byte[] imageData = compressed.toByteArray();
            int pageHeight = Math.max(1, (int) Math.ceil(842.0 * imageHeight / imageWidth));
            java.util.List<byte[]> objects = new ArrayList<>();
            objects.add(pdfBytes("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"));
            objects.add(pdfBytes("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n"));
            objects.add(pdfBytes("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 842 "
                    + pageHeight + "] /Resources << /XObject << /Im0 5 0 R >> >> /Contents 4 0 R >>\nendobj\n"));
            byte[] content = pdfBytes("q\n842 0 0 " + pageHeight + " 0 0 cm\n/Im0 Do\nQ\n");
            objects.add(pdfStream(4, "", content));
            objects.add(pdfStream(5, "/Type /XObject /Subtype /Image /Width " + imageWidth
                    + " /Height " + imageHeight + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /FlateDecode",
                    imageData));

            ByteArrayOutputStream pdf = new ByteArrayOutputStream();
            pdf.write(pdfBytes("%PDF-1.4\n"));
            java.util.List<Integer> offsets = new ArrayList<>();
            offsets.add(0);
            for (byte[] object : objects) {
                offsets.add(pdf.size());
                pdf.write(object);
            }
            int xref = pdf.size();
            pdf.write(pdfBytes("xref\n0 " + offsets.size() + "\n0000000000 65535 f \n"));
            for (int i = 1; i < offsets.size(); i++) {
                pdf.write(pdfBytes(String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(i))));
            }
            pdf.write(pdfBytes("trailer\n<< /Size " + offsets.size()
                    + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n"));
            return pdf.toByteArray();
        }

        private static byte[] pdfStream(int number, String dictionary, byte[] data) throws IOException {
            ByteArrayOutputStream object = new ByteArrayOutputStream();
            object.write(pdfBytes(number + " 0 obj\n<< " + dictionary + " /Length " + data.length
                    + " >>\nstream\n"));
            object.write(data);
            object.write(pdfBytes("\nendstream\nendobj\n"));
            return object.toByteArray();
        }

        private static byte[] pdfBytes(String value) {
            return value.getBytes(StandardCharsets.ISO_8859_1);
        }

        public static void canhBao(Component cha, String strNoiDung) {
            JOptionPane.showMessageDialog(cha, strNoiDung, "Pet Shop Pro", JOptionPane.WARNING_MESSAGE);
        }

        public static boolean xacNhan(Component cha, String strNoiDung) {
            return JOptionPane.showConfirmDialog(cha, strNoiDung, "Pet Shop Pro",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
        }

        public static void loi(Component cha, Exception ex) {
            String nd;

            if (ex instanceof SQLException sql) {
                if (sql.getErrorCode() == 547) {
                    nd = "Không thực hiện được vì dữ liệu này đang được dùng ở nơi khác\n"
                            + "(ví dụ: danh mục còn sản phẩm, sản phẩm đã có phiếu nhập/hóa đơn).";
                } else if (sql.getErrorCode() == 2627 || sql.getErrorCode() == 2601) {
                    nd = "Mã này đã tồn tại, vui lòng nhập mã khác.";
                } else {
                    nd = "Lỗi cơ sở dữ liệu:\n" + sql.getMessage();
                }
            } else {
                nd = "Có lỗi xảy ra:\n" + ex.getMessage();
            }

            ex.printStackTrace();
            JOptionPane.showMessageDialog(cha, nd, "Pet Shop Pro", JOptionPane.ERROR_MESSAGE);
        }

        public static JPanel hangNut(JButton btnHuy2, JButton btnLuu2) {
            JPanel pnlKetQua = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnlKetQua.setOpaque(false);
            pnlKetQua.add(btnHuy2);
            pnlKetQua.add(btnLuu2);
            return pnlKetQua;
        }
    }

    private void dangKyHanhDong(AbstractButton nut, Runnable hanhDong) {

        nut.putClientProperty("petshop.hanhDong", hanhDong);

        nut.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComponent control) {

            Object handler = control.getClientProperty("petshop.hanhDong");
            if (handler instanceof Runnable hanhDong) {

                hanhDong.run();
            }
        }
    }

}
