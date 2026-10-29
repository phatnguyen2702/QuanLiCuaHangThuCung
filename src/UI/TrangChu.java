package UI;

import ConnectDB.connectDB;
import dao.HoaDonDAO;
import dao.LichHenDAO;
import dao.TaiKhoanDAO;
import entity.LichHen;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

/**
 * Cua so chinh Pet Shop Pro - pmnMenu theo HUONG CHUC NANG.
 * 5 btnNhom: He thong, Danh muc, Nghiep vu, Tim kiem, Thong ke.
 * Moi chuc nang mo JPanel trong CardLayout, khong mo JFrame moi.
 */
public class TrangChu extends JFrame {

    // LIGHT_BLUE: Màu dùng cho giao diện: xanh dương.
    private final Color LIGHT_BLUE = new Color(229, 241, 252);
    // MENU_BLUE: Màu dùng cho giao diện: xanh dương.
    private final Color MENU_BLUE = new Color(207, 222, 235);
    // maNV: Lưu mã nhân viên.
    private final String maNV;
    // hoTen: Lưu họ tên.
    private final String hoTen;
    // vaiTro: Lưu vai trò.
    private final String vaiTro;

    // TEN_TAB: Mảng lưu tab.
    private static final String[] TEN_TAB = {
            "Danh mục", "Sản phẩm", "Nhập kho", "Kiểm kê kho"
    };
    // DINH_DANG_GIO: Bộ định dạng ngày/giờ theo mẫu HH:mm:ss | dd/MM/yyyy để chuyển
    // giữa dữ liệu thời gian và chuỗi.
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm:ss | dd/MM/yyyy");
    // DINH_DANG_GIO_HEN: Bộ định dạng ngày/giờ theo mẫu HH:mm để chuyển giữa dữ
    // liệu thời gian và chuỗi.
    private static final DateTimeFormatter DINH_DANG_GIO_HEN = DateTimeFormatter.ofPattern("HH:mm");

    // cardLayout: Bộ bố trí cho phép chuyển giữa các màn hình trong cùng một vùng
    // chứa.
    private final CardLayout cardLayout = new CardLayout();
    // pnlNoiDung: Khung chứa thành phần giao diện nội dung.
    private final JPanel pnlNoiDung = new JPanel(cardLayout);
    // lblNgayTrangChu: Nhãn hiển thị ngày hiển thị trên trang chủ.
    private JLabel lblNgayTrangChu;
    // btnTrangChu: Nút thao tác trang chủ.
    private JButton btnTrangChu;
    // btnHeThong: Nút thao tác hệ thống.
    private JButton btnHeThong;
    // btnDanhMuc: Nút thao tác danh mục.
    private JButton btnDanhMuc;
    // btnXuLy: Nút thao tác xử lý.
    private JButton btnXuLy;
    // btnTimKiem: Nút thao tác tìm kiếm.
    private JButton btnTimKiem;
    // btnThongKe: Nút thao tác thống kê.
    private JButton btnThongKe;
    // dsBtnMucChinh: Lưu danh sách nút mục chính; phần tử có kiểu JButton.
    private final ArrayList<JButton> dsBtnMucChinh = new ArrayList<>();
    // btnNhomDaChon: Nút thao tác nhóm đã chọn.
    private JButton btnNhomDaChon;

    // Man hinh kho; nap lai du lieu khi quay lai.
    // arrPnlManHinhKho: Mảng lưu mảng khung màn hình kho.
    private final JPanel[] arrPnlManHinhKho = new JPanel[TEN_TAB.length];
    // napLai: Mảng lưu nạp lại.
    private final Runnable[] napLai = new Runnable[TEN_TAB.length];
    // Man hinh khac khoi tao mot lan, tai su dung trong CardLayout.
    // mapManHinhKhac: Bảng ánh xạ khóa đến giá trị dùng cho ánh xạ mã màn hình đến
    // thành phần giao diện.
    private final Map<String, Component> mapManHinhKhac = new HashMap<>();
    // pnlTraCuuHoaDon: Đối tượng khung tra cứu hóa đơn có kiểu TraCuuLichSuHoaDon.
    private TraCuuLichSuHoaDon pnlTraCuuHoaDon;
    // pnlTraCuuNhapKho: Đối tượng khung tra cứu nhập kho có kiểu TraCuuKhoPanel.
    private TraCuuKhoPanel pnlTraCuuNhapKho;
    // pnlTraCuuKiemKe: Đối tượng khung tra cứu kiểm kê có kiểu TraCuuKhoPanel.
    private TraCuuKhoPanel pnlTraCuuKiemKe;
    // pnlThongKeDoanhThu: Đối tượng khung thống kê doanh thu có kiểu
    // ThongKeDoanhThu.
    private ThongKeDoanhThu pnlThongKeDoanhThu;
    // pnlThanhToan: Đối tượng khung thanh toán có kiểu QuanLyThanhToan.
    private QuanLyThanhToan pnlThanhToan;
    // tmrDongHo: Bộ hẹn giờ dùng để cập nhật giao diện định kỳ.
    private final javax.swing.Timer tmrDongHo;
    // pnlCanhBaoHanSuDung: Đối tượng khung cảnh báo hạn sử dụng có kiểu
    // CanhBaoHanSuDungPanel.
    private CanhBaoHanSuDungPanel pnlCanhBaoHanSuDung;
    // pnlGoiYThucAn: Đối tượng khung gợi ý thức ăn có kiểu GoiYThucAnPanel.
    private GoiYThucAnPanel pnlGoiYThucAn;

    // Thanh phan dong tren trang chu.
    // pnlLichSpaNoiDung: Khung chứa thành phần giao diện lịch Spa nội dung.
    private JPanel pnlLichSpaNoiDung;
    // lblPhuDeLichSpa: Nhãn hiển thị phụ đề lịch Spa.
    private JLabel lblPhuDeLichSpa;
    // lblDoanhThuHomNay: Nhãn hiển thị doanh thu hôm nay.
    private JLabel lblDoanhThuHomNay;
    // lblLuotMuaHomNay: Nhãn hiển thị lượt mua hôm nay.
    private JLabel lblLuotMuaHomNay;
    // lblLichSpaHomNay: Nhãn hiển thị lịch Spa hôm nay.
    private JLabel lblLichSpaHomNay;
    // lblPhieuNhapHomNay: Nhãn hiển thị phiếu nhập hôm nay.
    private JLabel lblPhieuNhapHomNay;

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    // Tham số maNV: mã nhân viên.
    // Tham số hoTen: họ tên.
    // Tham số vaiTro: vai trò.
    public TrangChu(String maNV, String hoTen, String vaiTro) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;

        if (maNV == null || maNV.isBlank()
                || PhanQuyen.vaiTro(vaiTro) == PhanQuyen.VaiTro.KHONG_HOP_LE) {
            throw new SecurityException("Chưa đăng nhập hoặc vai trò không hợp lệ.");
        }

        PhienLamViec.dat(maNV, hoTen, vaiTro);
        UI.PhienLamViec.dat(maNV, hoTen, vaiTro);

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

    // Giải phóng tài nguyên liên quan khi đóng cửa sổ.
    @Override
    public void dispose() {
        tmrDongHo.stop();
        super.dispose();
    }

    // Hiển thị trang chủ.
    private void hienTrangChu() {
        btnNhomDaChon = null;
        cardLayout.show(pnlNoiDung, "trangchu");
        datMucDangChon(btnTrangChu);
        setTitle("Pet Shop Pro - Trang chủ");
        napLichSpaSapToi();
    }

    /** API cu cho DieuHuong.moKho(cha, tab) va nhung man hinh khac. */
    public void chuyenKho(int tab) {
        if (tab < 0 || tab >= TEN_TAB.length)
            tab = 0;
        PhanQuyen.Quyen q = switch (tab) {
            case 0 -> PhanQuyen.Quyen.DANH_MUC_SAN_PHAM;
            case 1 -> PhanQuyen.Quyen.SAN_PHAM;
            case 2 -> PhanQuyen.Quyen.NHAP_KHO;
            default -> PhanQuyen.Quyen.KIEM_KE;
        };
        if (!PhanQuyen.kiemTra(q, this))
            return;
        try {
            boolean laLanDau = arrPnlManHinhKho[tab] == null;
            if (laLanDau) {
                taoManHinh(tab);
                pnlNoiDung.add(arrPnlManHinhKho[tab], "tab" + tab);
            }
            cardLayout.show(pnlNoiDung, "tab" + tab);
            datMucDangChon(btnNhomDaChon != null ? btnNhomDaChon : btnDanhMuc);
            setTitle("Pet Shop Pro - " + TEN_TAB[tab]);
            if (!laLanDau && napLai[tab] != null)
                napLai[tab].run();
            pnlNoiDung.revalidate();
            pnlNoiDung.repaint();
        } catch (RuntimeException ex) {
            baoLoiMoManHinh(TEN_TAB[tab], ex);
        }
    }

    // Mở thống kê doanh thu.
    private void moThongKeDoanhThu() {
        boolean daTaoManHinh = pnlThongKeDoanhThu != null;
        hienManHinh("thongkedoanhthu", "Thống kê doanh thu", btnThongKe,
                () -> pnlThongKeDoanhThu = new ThongKeDoanhThu());
        if (daTaoManHinh && pnlThongKeDoanhThu != null) {
            pnlThongKeDoanhThu.lamMoi();
        }
    }

    // Tạo màn hình.
    // Tham số tab: tab.
    private void taoManHinh(int tab) {
        switch (tab) {
            case 0 -> {
                DanhMucPanel pnlKhung2 = new DanhMucPanel();
                arrPnlManHinhKho[tab] = pnlKhung2;
                napLai[tab] = pnlKhung2::napLai;
            }
            case 1 -> {
                SanPhamPanel pnlKhung2 = new SanPhamPanel();
                arrPnlManHinhKho[tab] = pnlKhung2;
                napLai[tab] = pnlKhung2::napLai;
            }
            case 2 -> {
                NhapKhoPanel pnlKhung2 = new NhapKhoPanel(this::moTraCuuNhapKho);
                arrPnlManHinhKho[tab] = pnlKhung2;
                napLai[tab] = pnlKhung2::napLai;
            }
            default -> {
                KiemKePanel pnlKhung2 = new KiemKePanel(this::moTraCuuKiemKe);
                arrPnlManHinhKho[tab] = pnlKhung2;
                napLai[tab] = pnlKhung2::napLai;
            }
        }
    }

    // Hiển thị màn hình.
    // Tham số khoa: khóa.
    // Tham số tieuDe: tiêu đề.
    // Tham số btnNhomMacDinh: nhóm mặc định.
    // Tham số tao: tạo.
    private void hienManHinh(String khoa, String tieuDe, JButton btnNhomMacDinh,
            Supplier<? extends Component> tao) {
        if (!PhanQuyen.kiemTra(PhanQuyen.tuMaManHinh(khoa), this))
            return;
        try {
            Component c = mapManHinhKhac.get(khoa);
            if (c == null) {
                c = tao.get();
                mapManHinhKhac.put(khoa, c);
                pnlNoiDung.add(c, khoa);
            }
            cardLayout.show(pnlNoiDung, khoa);
            datMucDangChon(btnNhomDaChon != null ? btnNhomDaChon : btnNhomMacDinh);
            setTitle("Pet Shop Pro - " + tieuDe);
            pnlNoiDung.revalidate();
            pnlNoiDung.repaint();
        } catch (RuntimeException ex) {
            baoLoiMoManHinh(tieuDe, ex);
        }
    }

    // Báo lỗi mô màn hình.
    // Tham số ten: tên.
    // Tham số ex: ngoại lệ cần xử lý.
    private void baoLoiMoManHinh(String ten, RuntimeException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Không thể mở màn hình " + ten + ".\n"
                        + "Vui lòng kiểm tra kết nối CSDL hoặc Console.\n"
                        + ex.getMessage(),
                "Pet Shop Pro - Lỗi mở màn hình", JOptionPane.ERROR_MESSAGE);
    }

    // Mở lịch hẹn.
    private void moLichHen() {
        hienManHinh("lichhen", "Lịch hẹn Spa", btnXuLy, QuanLyLichHen::new);
    }

    // Mở thú cưng.
    private void moThuCung() {
        hienManHinh("thucung", "Quản lý thú cưng", btnDanhMuc, QuanLyThuCung::new);
    }

    // Mở khách hàng.
    private void moKhachHang() {
        hienManHinh("khachhang", "Quản lý khách hàng", btnDanhMuc,
                () -> new QuanLyKhachHang(maNV));
    }

    // Mở voucher.
    private void moVoucher() {
        hienManHinh("voucher", "Quản lý voucher", btnDanhMuc, QuanLyVoucher::new);
    }

    // Mở phiếu dịch vụ.
    private void moPhieuDichVu() {
        hienManHinh("phieudv", "Quản lý phiếu dịch vụ", btnXuLy,
                QuanLyPhieuDichVu::new);
    }

    // Mở tra cứu hóa đơn.
    private void moTraCuuHoaDon() {
        hienManHinh("hoadon", "Tra cứu lịch sử hóa đơn", btnTimKiem,
                () -> pnlTraCuuHoaDon = new TraCuuLichSuHoaDon(
                        () -> chuyenTuNhom(btnXuLy, this::moThanhToan)));
        if (pnlTraCuuHoaDon != null) {
            try {
                pnlTraCuuHoaDon.hienDanhSach();
            } catch (RuntimeException ex) {
                baoLoiMoManHinh("Tra cứu lịch sử hóa đơn", ex);
            }
        }
    }

    // Mở tra cứu nhập kho.
    private void moTraCuuNhapKho() {
        hienManHinh("tracuukho", "Tra cứu nhập kho", btnTimKiem,
                () -> pnlTraCuuNhapKho = new TraCuuKho(
                        () -> chuyenTuNhom(btnXuLy, () -> chuyenKho(2))));
        if (pnlTraCuuNhapKho != null)
            pnlTraCuuNhapKho.lamMoi();
    }

    // Mở tra cứu kiểm kê.
    private void moTraCuuKiemKe() {
        hienManHinh("tracuukkkho", "Tra cứu kiểm kê kho", btnTimKiem,
                () -> pnlTraCuuKiemKe = new TraCuuKKKho(
                        () -> chuyenTuNhom(btnXuLy, () -> chuyenKho(3))));
        if (pnlTraCuuKiemKe != null)
            pnlTraCuuKiemKe.lamMoi();
    }

    // Mở thanh toán.
    private void moThanhToan() {
        hienManHinh("thanhtoan", "Thanh toán", btnXuLy,
                () -> pnlThanhToan = new QuanLyThanhToan(maNV,
                        () -> chuyenTuNhom(btnTimKiem, this::moTraCuuHoaDon),
                        this::capNhatDanhSachPhieuDichVu));
        if (pnlThanhToan != null) {
            pnlThanhToan.napGioHang();
        }
    }

    // Cập nhật danh sách phiếu dịch vụ.
    private void capNhatDanhSachPhieuDichVu() {
        Component manHinh = mapManHinhKhac.get("phieudv");
        if (manHinh instanceof QuanLyPhieuDichVu) {
            ((QuanLyPhieuDichVu) manHinh).loadData();
        }
    }

    /** Mở màn hình thanh toán với các mặt hàng đang có trong giỏ gợi ý. */
    public void moThanhToanTuGioHang() {
        if (!PhanQuyen.kiemTra(PhanQuyen.Quyen.THANH_TOAN, this))
            return;
        if (UI.PhienLamViec.gioHang().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống.", "Giỏ hàng",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        chuyenTuNhom(btnXuLy, this::moThanhToan);
    }

    // Mở ma trận phân quyền.
    private void moMaTranPhanQuyen() {
        hienManHinh("phanquyen", "Ma trận phân quyền", btnHeThong,
                () -> new MaTranPhanQuyenPanel(this::moQuanLyTaiKhoan));
    }

    // Mở quản lý tài khoản.
    private void moQuanLyTaiKhoan() {
        hienManHinh("taikhoan", "Quản lý tài khoản", btnHeThong,
                QuanLyTaiKhoan::new);
    }

    // Mở quản lý phân ca.
    private void moQuanLyPhanCa() {
        hienManHinh("phanca", "Phân ca nhân viên", btnXuLy,
                QuanLyPhanCa::new);
    }

    // Mở quản lý ca làm.
    private void moQuanLyCaLam() {
        hienManHinh("calam", "Quản lý ca làm", btnHeThong,
                QuanLyCaLam::new);
    }

    // Mở dịch vụ.
    private void moDichVu() {
        hienManHinh("dichvu", "Quản lý dịch vụ", btnDanhMuc, QuanLyDichVu::new);
        Component manHinh = mapManHinhKhac.get("dichvu");
        if (manHinh instanceof QuanLyDichVu) {
            ((QuanLyDichVu) manHinh).lamMoi();
        }
    }

    // Chuyển từ nhóm.
    // Tham số btnNhom: nhóm.
    // Tham số hanhDong: hành động.
    private void chuyenTuNhom(JButton btnNhom, Runnable hanhDong) {
        btnNhomDaChon = btnNhom;
        hanhDong.run();
    }

    // Kiểm tra pmnMenu có mục nào có thể hiển thị hay không.
    // Tham số pmnMenu: pmnMenu.
    // Kết quả: true/false theo điều kiện xử lý.
    private boolean coMucMenu(JPopupMenu pmnMenu) {
        for (Component c : pmnMenu.getComponents())
            if (c instanceof JMenuItem)
                return true;
        return false;
    }

    // Cập nhật quyền truy cập pmnMenu.
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

    // Cập nhật kiểu hiển thị để đánh dấu mục pmnMenu đang được chọn.
    // Tham số btnDangChon: mục đang chọn.
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

    // Cập nhật giờ.
    private void capNhatGio() {
        if (lblNgayTrangChu != null) {
            // Định dạng dữ liệu thành chuỗi rồi hiển thị trong control bằng setText().
            lblNgayTrangChu.setText(LocalDateTime.now().format(DINH_DANG_GIO));
        }
    }

    // ==================== MENU THEO HUONG CHUC NANG ====================
    // Tạo khung đầu trang.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPnlHeader() {
        // header: Khung chứa thành phần giao diện đầu trang.
        JPanel pnlHeader2 = new JPanel(new BorderLayout(8, 0));
        pnlHeader2.setBackground(UIStyle.NAVY);
        pnlHeader2.setBorder(new EmptyBorder(7, 12, 7, 12));

        // pnlThuongHieu: Khung chứa thành phần giao diện thương hiệu.
        JPanel pnlThuongHieu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlThuongHieu.setOpaque(false);
        // lblLogo: Nhãn hiển thị: PET SHOP PRO.
        JLabel lblLogo = new JLabel("PET SHOP PRO", taiIcon("/img/logo.png", 26, 26), SwingConstants.LEFT);
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(UIStyle.font(Font.BOLD, 16));
        lblLogo.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblLogo.setIconTextGap(8);
        pnlThuongHieu.add(lblLogo);
        pnlThuongHieu.setPreferredSize(new Dimension(160, 38));
        pnlHeader2.add(pnlThuongHieu, BorderLayout.WEST);

        // pnlMenu: Khung chứa thành phần giao diện pmnMenu.
        JPanel pnlMenu = new JPanel(new GridLayout(1, 6, 2, 0));
        pnlMenu.setOpaque(false);

        btnTrangChu = taoBtnNhom("TRANG CHỦ", "/img/trangchulogo.png");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTrangChu.addActionListener(e -> hienTrangChu());

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

        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnHeThong.addActionListener(e -> hienPopup(btnHeThong, taoMenuHeThong()));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnDanhMuc.addActionListener(e -> hienPopup(btnDanhMuc, taoMenuDanhMuc()));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnXuLy.addActionListener(e -> hienPopup(btnXuLy, taoMenuNghiepVu()));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTimKiem.addActionListener(e -> hienPopup(btnTimKiem, taoMenuTimKiem()));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnThongKe.addActionListener(e -> hienPopup(btnThongKe, taoMenuThongKe()));

        pnlHeader2.add(pnlMenu, BorderLayout.CENTER);

        // btnNhanVien: Nút thao tác nhân viên.
        JButton btnNhanVien = new JButton(hoTen == null ? "Tài khoản" : hoTen);
        btnNhanVien.setFont(UIStyle.font(Font.BOLD, 12));
        btnNhanVien.setForeground(Color.WHITE);
        btnNhanVien.setBackground(UIStyle.NAVY);
        btnNhanVien.setFocusPainted(false);
        btnNhanVien.setOpaque(true);
        btnNhanVien.setBorder(new EmptyBorder(8, 6, 8, 6));
        btnNhanVien.setPreferredSize(new Dimension(170, 38));
        btnNhanVien.setToolTipText("Thông tin nhân viên / Đổi mật khẩu / Đăng xuất");
        btnNhanVien.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnNhanVien.addActionListener(e -> hienThongTinNhanVien(btnNhanVien));
        pnlHeader2.add(btnNhanVien, BorderLayout.EAST);

        return pnlHeader2;
    }

    // Tạo nút nhóm chức năng.
    // Tham số lblTitle2: tiêu đề.
    // Tham số iconPath: biểu tượng đường dẫn.
    // Kết quả: đối tượng/giá trị kiểu JButton.
    private JButton taoBtnNhom(String title, String iconPath) {
        // btnTam: Nút thao tác tạm.
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

    // Tải biểu tượng.
    // Tham số path: đường dẫn.
    // Tham số w: chiều rộng.
    // Tham số h: chiều cao.
    // Kết quả: đối tượng/giá trị kiểu ImageIcon.
    private ImageIcon taiIcon(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url == null)
            return null;
        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    // Hiển thị pmnMenu bật lên.
    // Tham số btnNguon: nguồn.
    // Tham số pmnPopMenu: bật lên pmnMenu.
    private void hienPopup(JButton btnNguon, JPopupMenu pmnPopMenu) {
        pmnPopMenu.show(btnNguon, 0, btnNguon.getHeight());
    }

    // Thêm mục.
    // Tham số pmnPopMenu: bật lên pmnMenu.
    // Tham số ten: tên.
    // Tham số btnNhom: nhóm.
    // Tham số hanhDong: hành động.
    private void themMuc(JPopupMenu pmnPopMenu, String ten, JButton btnNhom, Runnable hanhDong) {
        PhanQuyen.Quyen q = PhanQuyen.tuTenMenu(ten);
        if (!PhanQuyen.coQuyen(q))
            return;
        // mniChucNang: Mục lệnh trong pmnMenu chức nặng.
        JMenuItem mniChucNang = new JMenuItem(ten);
        mniChucNang.setFont(UIStyle.font(Font.PLAIN, 13));
        mniChucNang.setBorder(new EmptyBorder(9, 14, 9, 14));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        mniChucNang.addActionListener(e -> {
            if (PhanQuyen.kiemTra(q, this))
                chuyenTuNhom(btnNhom, hanhDong);
        });
        pmnPopMenu.add(mniChucNang);
    }

    // Tạo pmnMenu hệ thống.
    // Kết quả: đối tượng/giá trị kiểu JPopupMenu.
    private JPopupMenu taoMenuHeThong() {
        // p: Menu bật lên p.
        JPopupMenu pmnP = new JPopupMenu();
        themMuc(pmnP, "Quản lý tài khoản", btnHeThong, this::moQuanLyTaiKhoan);
        themMuc(pmnP, "Ma trận phân quyền", btnHeThong, this::moMaTranPhanQuyen);
        themMuc(pmnP, "Quản lý ca làm", btnHeThong, this::moQuanLyCaLam);
        return pmnP;
    }

    // Tạo pmnMenu danh mục.
    // Kết quả: đối tượng/giá trị kiểu JPopupMenu.
    private JPopupMenu taoMenuDanhMuc() {
        // p: Menu bật lên p.
        JPopupMenu pmnP = new JPopupMenu();
        themMuc(pmnP, "Danh mục sản phẩm", btnDanhMuc, () -> chuyenKho(0));
        themMuc(pmnP, "Sản phẩm", btnDanhMuc, () -> chuyenKho(1));
        themMuc(pmnP, "Khách hàng", btnDanhMuc, this::moKhachHang);
        themMuc(pmnP, "Thú cưng", btnDanhMuc, this::moThuCung);
        themMuc(pmnP, "Dịch vụ", btnDanhMuc, this::moDichVu);
        themMuc(pmnP, "Mã giảm giá", btnDanhMuc, this::moVoucher);
        return pmnP;
    }

    // Tạo pmnMenu nghiệp vụ.
    // Kết quả: đối tượng/giá trị kiểu JPopupMenu.
    private JPopupMenu taoMenuNghiepVu() {
        // p: Menu bật lên p.
        JPopupMenu pmnP = new JPopupMenu();
        themMuc(pmnP, "Nhập kho", btnXuLy, () -> chuyenKho(2));
        themMuc(pmnP, "Kiểm kê kho", btnXuLy, () -> chuyenKho(3));
        pmnP.addSeparator();
        themMuc(pmnP, "Lịch hẹn Spa", btnXuLy, this::moLichHen);
        themMuc(pmnP, "Phiếu dịch vụ", btnXuLy, this::moPhieuDichVu);
        pmnP.addSeparator();
        themMuc(pmnP, "Cảnh báo sản phẩm sắp hết hạn", btnXuLy, this::moCanhBaoHanSuDung);
        themMuc(pmnP, "Gợi ý thức ăn theo loại thú cưng", btnXuLy, this::moGoiYThucAn);
        pmnP.addSeparator();
        themMuc(pmnP, "Thanh toán hóa đơn", btnXuLy, this::moThanhToan);

        pmnP.addSeparator();
        themMuc(pmnP, "Phân ca nhân viên", btnXuLy, this::moQuanLyPhanCa);

        return pmnP;
    }

    // Tạo pmnMenu tìm kiếm.
    // Kết quả: đối tượng/giá trị kiểu JPopupMenu.
    private JPopupMenu taoMenuTimKiem() {
        // p: Menu bật lên p.
        JPopupMenu pmnP = new JPopupMenu();
        themMuc(pmnP, "Tra cứu lịch sử hóa đơn", btnTimKiem, this::moTraCuuHoaDon);
        themMuc(pmnP, "Tra cứu nhập kho", btnTimKiem, this::moTraCuuNhapKho);
        themMuc(pmnP, "Tra cứu kiểm kê kho", btnTimKiem, this::moTraCuuKiemKe);
        return pmnP;
    }

    // Tạo pmnMenu thống kê.
    // Kết quả: đối tượng/giá trị kiểu JPopupMenu.
    private JPopupMenu taoMenuThongKe() {
        // p: Menu bật lên p.
        JPopupMenu pmnP = new JPopupMenu();
        themMuc(pmnP, "Thống kê doanh thu", btnThongKe, this::moThongKeDoanhThu);
        return pmnP;
    }

    // Hiển thị thông tin nhân viên.
    // Tham số btnNguon: nguồn.
    private void hienThongTinNhanVien(JButton btnNguon) {
        // pmnPopMenu: Menu bật lên bật lên pmnMenu.
        JPopupMenu pmnPopMenu = new JPopupMenu();
        // pnlThongTin: Khung chứa thành phần giao diện thông tin.
        JPanel pnlThongTin = new JPanel(new GridLayout(0, 2, 15, 8));
        pnlThongTin.setBorder(new EmptyBorder(12, 12, 12, 12));
        themThongTin(pnlThongTin, "Mã NV", maNV);
        themThongTin(pnlThongTin, "Họ tên", hoTen);
        themThongTin(pnlThongTin, "Vai trò", vaiTro);
        themThongTin(pnlThongTin, "Trạng thái", "Đang hoạt động");
        pmnPopMenu.add(pnlThongTin);
        pmnPopMenu.addSeparator();
        // mniDoiMatKhau: Mục lệnh trong pmnMenu: Đổi mật khẩu.
        JMenuItem mniDoiMatKhau = new JMenuItem("Đổi mật khẩu");
        mniDoiMatKhau.setFont(UIStyle.font(Font.PLAIN, 13));
        mniDoiMatKhau.setBorder(new EmptyBorder(9, 14, 9, 14));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        mniDoiMatKhau.addActionListener(e -> moDlgDoiMatKhau());
        pmnPopMenu.add(mniDoiMatKhau);
        pmnPopMenu.addSeparator();
        // mniDangXuat: Mục lệnh trong pmnMenu: Đăng xuất.
        JMenuItem mniDangXuat = new JMenuItem("Đăng xuất");
        mniDangXuat.setFont(UIStyle.font(Font.PLAIN, 13));
        mniDangXuat.setBorder(new EmptyBorder(9, 14, 9, 14));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        mniDangXuat.addActionListener(e -> dangXuat());
        pmnPopMenu.add(mniDangXuat);
        pmnPopMenu.show(btnNguon,
                -pmnPopMenu.getPreferredSize().width + btnNguon.getWidth(), btnNguon.getHeight());
    }

    /**
     * Cho phep moi nhan vien da dang nhap doi mat khau cua CHINH MINH.
     * Khong hien mat khau cu, khong yeu cau quyen quan ly.
     */
    private void moDlgDoiMatKhau() {
        if (maNV == null || maNV.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không xác định được phiên đăng nhập hiện tại.",
                    "Đổi mật khẩu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // winDoiMatKhau: Hộp thoại đổi mật khẩu.
        JDialog winDoiMatKhau = new JDialog(this, "Pet Shop Pro - Đổi mật khẩu", true);
        winDoiMatKhau.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        winDoiMatKhau.setSize(460, 405);
        winDoiMatKhau.setMinimumSize(new Dimension(420, 385));
        winDoiMatKhau.setLocationRelativeTo(this);
        winDoiMatKhau.setLayout(new BorderLayout());

        // pnlTieuDe: Khung chứa thành phần giao diện tiêu đề.
        JPanel pnlTieuDe = new JPanel(new BorderLayout(0, 5));
        pnlTieuDe.setBackground(UIStyle.NAVY);
        pnlTieuDe.setBorder(new EmptyBorder(16, 22, 16, 22));
        // lblTieuDe: Nhãn hiển thị: ĐỔI MẬT KHẨU.
        JLabel lblTieuDe = new JLabel("ĐỔI MẬT KHẨU");
        lblTieuDe.setForeground(Color.WHITE);
        lblTieuDe.setFont(UIStyle.font(Font.BOLD, 19));
        // lblTaiKhoan: Nhãn hiển thị: Tài khoản: .
        JLabel lblTaiKhoan = new JLabel("Tài khoản: " + maNV + " - " + (hoTen == null ? "" : hoTen));
        lblTaiKhoan.setForeground(new Color(215, 230, 245));
        lblTaiKhoan.setFont(UIStyle.font(Font.PLAIN, 12));
        pnlTieuDe.add(lblTieuDe, BorderLayout.NORTH);
        pnlTieuDe.add(lblTaiKhoan, BorderLayout.SOUTH);
        winDoiMatKhau.add(pnlTieuDe, BorderLayout.NORTH);

        // txtMatKhauCu: Ô nhập mật khẩu có che ký tự mật khẩu cũ.
        JPasswordField txtMatKhauCu = new JPasswordField(21);
        // txtMatKhauMoi: Ô nhập mật khẩu có che ký tự mật khẩu mới.
        JPasswordField txtMatKhauMoi = new JPasswordField(21);
        // txtXacNhanMatKhau: Ô nhập mật khẩu có che ký tự xác nhân mật khẩu.
        JPasswordField txtXacNhanMatKhau = new JPasswordField(21);
        // arrMatKhau: Mảng lưu mảng mật khẩu.
        JPasswordField[] arrMatKhau = { txtMatKhauCu, txtMatKhauMoi, txtXacNhanMatKhau };
        for (JPasswordField txtMatKhau : arrMatKhau) {
            txtMatKhau.setFont(UIStyle.font(Font.PLAIN, 14));
            txtMatKhau.setPreferredSize(new Dimension(280, 35));
        }

        // pnlForm: Khung chứa thành phần giao diện biểu mẫu.
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(16, 22, 8, 22));
        // gbc: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng
        // GridBagLayout.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(3, 0, 5, 0);

        themTruongMatKhau(pnlForm, gbc, 0, "Mật khẩu hiện tại", txtMatKhauCu);
        themTruongMatKhau(pnlForm, gbc, 2, "Mật khẩu mới", txtMatKhauMoi);
        themTruongMatKhau(pnlForm, gbc, 4, "Xác nhận mật khẩu mới", txtXacNhanMatKhau);

        // chkHienMatKhau: Ô chọn đánh dấu: Hiện mật khẩu.
        JCheckBox chkHienMatKhau = new JCheckBox("Hiện mật khẩu");
        chkHienMatKhau.setOpaque(false);
        chkHienMatKhau.setFont(UIStyle.font(Font.PLAIN, 12));
        char charEcho = txtMatKhauCu.getEchoChar();
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        chkHienMatKhau.addActionListener(e -> {
            for (JPasswordField txtMatKhau : arrMatKhau) {
                txtMatKhau.setEchoChar(chkHienMatKhau.isSelected() ? (char) 0 : charEcho);
            }
        });
        gbc.gridy = 6;
        gbc.insets = new Insets(1, 0, 0, 0);
        pnlForm.add(chkHienMatKhau, gbc);

        // lblTrangThai: Nhãn hiển thị: Mật khẩu mới phải có ít nhất 6 ký tự..
        JLabel lblTrangThai = new JLabel("Mật khẩu mới phải có ít nhất 6 ký tự.");
        lblTrangThai.setForeground(new Color(100, 108, 120));
        lblTrangThai.setFont(UIStyle.font(Font.PLAIN, 12));
        gbc.gridy = 7;
        gbc.insets = new Insets(5, 0, 0, 0);
        pnlForm.add(lblTrangThai, gbc);

        winDoiMatKhau.add(pnlForm, BorderLayout.CENTER);

        // btnHuy: Nút thao tác hủy.
        JButton btnHuy = UIStyle.nutPhu("Hủy");
        // btnLuuMatKhau: Nút thao tác lưu mật khẩu.
        JButton btnLuuMatKhau = UIStyle.nutChinh("Lưu mật khẩu");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnHuy.addActionListener(e -> winDoiMatKhau.dispose());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLuuMatKhau.addActionListener(e -> {
            // arrMatKhauCu: Mảng lưu mảng mật khẩu cũ.
            char[] arrMatKhauCu = txtMatKhauCu.getPassword();
            // arrMatKhauMoi: Mảng lưu mảng mật khẩu mới.
            char[] arrMatKhauMoi = txtMatKhauMoi.getPassword();
            // arrXacNhan: Mảng lưu mảng xác nhân.
            char[] arrXacNhan = txtXacNhanMatKhau.getPassword();
            try {
                if (arrMatKhauCu.length == 0 || arrMatKhauMoi.length == 0 || arrXacNhan.length == 0) {
                    JOptionPane.showMessageDialog(winDoiMatKhau,
                            "Vui lòng nhập đầy đủ ba ô mật khẩu.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (arrMatKhauMoi.length < 6) {
                    JOptionPane.showMessageDialog(winDoiMatKhau,
                            "Mật khẩu mới phải có ít nhất 6 ký tự.", "Mật khẩu không hợp lệ",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (Arrays.equals(arrMatKhauCu, arrMatKhauMoi)) {
                    JOptionPane.showMessageDialog(winDoiMatKhau,
                            "Mật khẩu mới phải khác mật khẩu hiện tại.", "Mật khẩu không hợp lệ",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!Arrays.equals(arrMatKhauMoi, arrXacNhan)) {
                    JOptionPane.showMessageDialog(winDoiMatKhau,
                            "Xác nhận mật khẩu mới không khớp.", "Mật khẩu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                    // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                    txtXacNhanMatKhau.setText("");
                    return;
                }

                // SQL Server duoc truy van o worker, khong khoa giao dien Swing.
                String strMatKhauCu = new String(arrMatKhauCu);
                String strMatKhauMoi = new String(arrMatKhauMoi);
                btnLuuMatKhau.setEnabled(false);
                btnHuy.setEnabled(false);
                lblTrangThai.setForeground(UIStyle.NAVY);
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                lblTrangThai.setText("Đang cập nhật mật khẩu...");

                new SwingWorker<Boolean, Void>() {
                    // Chạy công việc nền để tránh làm treo luồng giao diện Swing.
                    // Kết quả: đối tượng/giá trị kiểu Boolean.
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return new TaiKhoanDAO().doiMatKhau(maNV, strMatKhauCu, strMatKhauMoi);
                    }

                    // Xử lý kết quả công việc nền trên luồng giao diện Swing.
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
                                    // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                                    txtMatKhau.setText("");
                                winDoiMatKhau.dispose();
                            } else {
                                lblTrangThai.setForeground(new Color(192, 55, 55));
                                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                                lblTrangThai.setText("Mật khẩu hiện tại không đúng. Vui lòng thử lại.");
                                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                                txtMatKhauCu.setText("");
                                txtMatKhauCu.requestFocusInWindow();
                            }
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            lblTrangThai.setForeground(new Color(192, 55, 55));
                            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                            lblTrangThai.setText("Thao tác bị gián đoạn. Vui lòng thử lại.");
                        } catch (ExecutionException ex) {
                            ex.printStackTrace();
                            lblTrangThai.setForeground(new Color(192, 55, 55));
                            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
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

        // pnlNut: Khung chứa thành phần giao diện nút.
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

    // Thêm trường mật khẩu.
    // Tham số pnlForm: khung biểu mẫu.
    // Tham số gbc: ràng buộc bố trí.
    // Tham số intDong: số nguyên dòng.
    // Tham số strNhan: chuỗi nhân.
    // Tham số txtMatKhau: ô văn bản mật khẩu.
    private void themTruongMatKhau(JPanel pnlForm, GridBagConstraints gbc,
            int intDong, String strNhan, JPasswordField txtMatKhau) {
        // lblNhan: Nhãn hiển thị nhãn.
        JLabel lblNhan = new JLabel(strNhan);
        lblNhan.setFont(UIStyle.font(Font.BOLD, 12));
        gbc.gridy = intDong;
        gbc.insets = new Insets(3, 0, 4, 0);
        pnlForm.add(lblNhan, gbc);
        gbc.gridy = intDong + 1;
        gbc.insets = new Insets(0, 0, 8, 0);
        pnlForm.add(txtMatKhau, gbc);
    }

    // Thêm thông tin.
    // Tham số pnlPanel: khung khung.
    // Tham số lblNhan: nhãn nhân.
    // Tham số lblGiaTri: nhãn giá trị.
    private void themThongTin(JPanel pnlPanel, String lblNhan, String lblGiaTri) {
        // lblThongTin: Nhãn hiển thị thông tin.
        JLabel lblThongTin = new JLabel(lblNhan);
        lblThongTin.setFont(UIStyle.font(Font.BOLD, 12));
        pnlPanel.add(lblThongTin);
        pnlPanel.add(new JLabel(lblGiaTri != null ? lblGiaTri : ""));
    }

    // Kết thúc phiên hiện tại và quay lại màn hình đăng nhập.
    private void dangXuat() {
        // Xóa phiên dùng chung trước khi về màn hình đăng nhập.
        UI.PhienLamViec.dat(null, null, null);
        PhienLamViec.dat(null, null, null);
        dispose();
        new DangNhap().setVisible(true);
    }

    // Làm mới trang chủ.
    private void lamMoiTrangChu() {
        capNhatGio();
        napLichSpaSapToi();
        napTongQuanHomNay();
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
    }

    // ==================== NOI DUNG TRANG CHU ====================
    // Tạo khung trang chủ.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPnlTrangChu() {
        // pnlChinh: Khung chứa thành phần giao diện chính.
        JPanel pnlChinh = new JPanel(new BorderLayout());
        pnlChinh.setBackground(LIGHT_BLUE);
        pnlChinh.add(taoPnlThanhTieuDe(), BorderLayout.NORTH);

        // pnlThan: Khung chứa thành phần giao diện thân.
        JPanel pnlThan = new JPanel(new BorderLayout(0, 14));
        pnlThan.setBackground(LIGHT_BLUE);
        pnlThan.setBorder(new EmptyBorder(16, 28, 16, 28));

        // Hai cot ben tren va duoi can nhau theo cung mot luoi.
        // pnlTren: Khung chứa thành phần giao diện trên.
        JPanel pnlTren = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlTren.setOpaque(false);
        pnlTren.add(taoPnlGioiThieu());
        pnlTren.add(taoPnlTruyCapNhanh());

        // pnlDuoi: Khung chứa thành phần giao diện dưới.
        JPanel pnlDuoi = taoPnlTienIchDuoi();
        pnlDuoi.setPreferredSize(new Dimension(0, 200));

        pnlThan.add(pnlTren, BorderLayout.CENTER);
        pnlThan.add(pnlDuoi, BorderLayout.SOUTH);
        pnlChinh.add(pnlThan, BorderLayout.CENTER);
        return pnlChinh;
    }

    // Tạo thanh tiêu đề.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPnlThanhTieuDe() {
        // pnlPanel: Khung chứa thành phần giao diện khung.
        JPanel pnlPanel = new JPanel(new BorderLayout());
        pnlPanel.setBackground(UIStyle.BAR);
        pnlPanel.setBorder(new EmptyBorder(10, 25, 10, 25));

        // lblNgayHienTai: Nhãn hiển thị ngày hiện tại.
        JLabel lblNgayHienTai = new JLabel(LocalDateTime.now().format(DINH_DANG_GIO));
        lblNgayTrangChu = lblNgayHienTai;
        lblNgayHienTai.setFont(UIStyle.font(Font.BOLD, 12));

        // lblTieuDeTrangChu: Nhãn hiển thị: TRANG CHỦ.
        JLabel lblTieuDeTrangChu = new JLabel("TRANG CHỦ", SwingConstants.CENTER);
        lblTieuDeTrangChu.setFont(UIStyle.font(Font.BOLD, 25));

        // pnlThaoTac: Khung chứa thành phần giao diện thao tác.
        JPanel pnlThaoTac = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlThaoTac.setOpaque(false);

        // btnLamMoi: Nút thao tác làm mới.
        JButton btnLamMoi = UIStyle.nutPhu(" Làm mới");
        btnLamMoi.setPreferredSize(new Dimension(135, 38));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLamMoi.addActionListener(e -> lamMoiTrangChu());

        // btnHoTro: Nút thao tác hỗ trợ.
        JButton btnHoTro = UIStyle.nutPhu("? Hỗ trợ");
        btnHoTro.setPreferredSize(new Dimension(125, 38));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnHoTro.addActionListener(e -> moModalHoTro());

        pnlThaoTac.add(btnLamMoi);
        pnlThaoTac.add(btnHoTro);

        pnlPanel.add(lblNgayHienTai, BorderLayout.WEST);
        pnlPanel.add(lblTieuDeTrangChu, BorderLayout.CENTER);
        pnlPanel.add(pnlThaoTac, BorderLayout.EAST);
        return pnlPanel;
    }

    // Tạo khung giới thiệu.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPnlGioiThieu() {
        // pnlPanel: Khung chứa thành phần giao diện khung.
        JPanel pnlPanel = new JPanel(new BorderLayout());
        pnlPanel.setBackground(Color.WHITE);
        pnlPanel.setBorder(new LineBorder(UIStyle.LINE));

        java.net.URL url = getClass().getResource("/img/giothieutrangchu.png");
        if (url == null) {
            pnlPanel.add(new JLabel("Không tìm thấy ảnh giới thiệu", SwingConstants.CENTER));
            return pnlPanel;
        }
        Image anh = new ImageIcon(url).getImage();
        // pnlAnhCoGian: Khung chứa thành phần giao diện ảnh có gian.
        JPanel pnlAnhCoGian = new JPanel() {
            // Vẽ nội dung tùy chỉnh lên thành phần giao diện.
            // Tham số g: ngữ cảnh vẽ.
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // w: Lưu chiều rộng.
                // h: Lưu chiều cao.
                int w = getWidth(), h = getHeight();
                int iw = anh.getWidth(this), ih = anh.getHeight(this);
                if (iw <= 0 || ih <= 0 || w <= 0 || h <= 0)
                    return;
                // Anh luon phu kin khung, khong con khoang trang lon.
                // Giu ti le goc, cat bot rat it o canh neu can thiet.
                double tile = Math.max((double) w / iw, (double) h / ih);
                int newW = (int) Math.ceil(iw * tile);
                int newH = (int) Math.ceil(ih * tile);
                // x: Lưu tọa độ ngang.
                int x = (w - newW) / 2;
                // y: Lưu tọa độ dọc.
                int y = 0; // Can tren tblDuLieu2 khung Truy cap nhanh.
                // g2: Ngữ cảnh vẽ dùng để thiết lập màu, phông và vẽ nội dung.
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(anh, x, y, newW, newH, this);
                g2.dispose();
            }
        };
        pnlAnhCoGian.setBackground(Color.WHITE);
        pnlPanel.add(pnlAnhCoGian, BorderLayout.CENTER);
        return pnlPanel;
    }

    // Tạo khung truy cập nhanh.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPnlTruyCapNhanh() {
        // pnlPanel: Khung chứa thành phần giao diện khung.
        JPanel pnlPanel = new JPanel();
        pnlPanel.setBackground(Color.WHITE);
        pnlPanel.setBorder(new CompoundBorder(new LineBorder(UIStyle.LINE), new EmptyBorder(15, 15, 15, 15)));
        pnlPanel.setLayout(new BoxLayout(pnlPanel, BoxLayout.Y_AXIS));

        // lblTitle2: Nhãn hiển thị: TRUY CẬP NHANH.
        JLabel lblTitle2 = new JLabel("TRUY CẬP NHANH");
        lblTitle2.setFont(UIStyle.font(Font.BOLD, 18));
        lblTitle2.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlPanel.add(lblTitle2);
        pnlPanel.add(Box.createVerticalStrut(15));

        themBtnTruyCapNhanh(pnlPanel, "Quản lý khách hàng", "F4",
                PhanQuyen.Quyen.KHACH_HANG,
                () -> chuyenTuNhom(btnDanhMuc, this::moKhachHang));
        themBtnTruyCapNhanh(pnlPanel, "Quản lý lịch hẹn", "F5",
                PhanQuyen.Quyen.LICH_HEN_SPA,
                () -> chuyenTuNhom(btnXuLy, this::moLichHen));
        themBtnTruyCapNhanh(pnlPanel, "Phiếu dịch vụ", "F6",
                PhanQuyen.Quyen.PHIEU_DICH_VU,
                () -> chuyenTuNhom(btnXuLy, this::moPhieuDichVu));
        themBtnTruyCapNhanh(pnlPanel, "Thanh toán", "F7",
                PhanQuyen.Quyen.THANH_TOAN,
                () -> chuyenTuNhom(btnXuLy, this::moThanhToan));
        themBtnTruyCapNhanh(pnlPanel, "Tra cứu hóa đơn", "F8",
                PhanQuyen.Quyen.TRA_CUU_HOA_DON,
                () -> chuyenTuNhom(btnTimKiem, this::moTraCuuHoaDon));

        return pnlPanel;
    }

    // Tạo khung tiện ích phía dưới.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPnlTienIchDuoi() {
        // pnlPanel: Khung chứa thành phần giao diện khung.
        JPanel pnlPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlPanel.setOpaque(false);
        pnlPanel.add(taoPnlLichSpaSapToi());
        pnlPanel.add(taoPnlTongQuan());
        return pnlPanel;
    }

    // Tạo khung tổng quan.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPnlTongQuan() {
        // pnlTongQuan: Khung chứa thành phần giao diện tổng quan.
        JPanel pnlTongQuan = new JPanel(new BorderLayout(0, 14));
        pnlTongQuan.setBackground(Color.WHITE);
        pnlTongQuan.setBorder(new CompoundBorder(new LineBorder(UIStyle.LINE), new EmptyBorder(14, 16, 14, 16)));

        // lblTieuDeTongQuan: Nhãn hiển thị: Tổng quan hôm nay.
        JLabel lblTieuDeTongQuan = new JLabel("Tổng quan hôm nay", taiIcon("/img/thongkelogo.png", 18, 18),
                SwingConstants.LEFT);
        lblTieuDeTongQuan.setFont(UIStyle.font(Font.BOLD, 17));
        lblTieuDeTongQuan.setForeground(new Color(40, 55, 77));
        lblTieuDeTongQuan.setIconTextGap(8);
        pnlTongQuan.add(lblTieuDeTongQuan, BorderLayout.NORTH);

        // pnlLuoi: Khung chứa thành phần giao diện lưới ngày.
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

    // Tạo thẻ tổng quan.
    // Tham số tieuDe: tiêu đề.
    // Tham số lblGiaTri2: giá trị.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoTheTongQuan(String tieuDe, JLabel lblGiaTri2) {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new BorderLayout(0, 6));

        pnlKhung.setBackground(new Color(245, 248, 252));
        pnlKhung.setBorder(new EmptyBorder(8, 12, 8, 12));

        // lblTieuDe: Nhãn hiển thị tiêu đề.
        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(UIStyle.font(Font.PLAIN, 12));

        lblGiaTri2.setFont(UIStyle.font(Font.BOLD, 20));
        lblGiaTri2.setForeground(new Color(18, 48, 82));

        pnlKhung.add(lblTieuDe, BorderLayout.NORTH);
        pnlKhung.add(lblGiaTri2, BorderLayout.CENTER);

        return pnlKhung;
    }

    // Tạo giá trị tổng quan.
    // Tham số text: nội dung chữ.
    // Kết quả: đối tượng/giá trị kiểu JLabel.
    private JLabel taoGiaTriTongQuan(String text) {
        // lblTam: Nhãn hiển thị tạm.
        JLabel lblTam = new JLabel(text);
        lblTam.setFont(UIStyle.font(Font.BOLD, 23));
        lblTam.setForeground(new Color(38, 45, 57));
        return lblTam;
    }

    // Mở cảnh báo hạn sử dụng.
    private void moCanhBaoHanSuDung() {
        hienManHinh("canhbaohansudung", "Cảnh báo sản phẩm sắp hết hạn", btnXuLy,
                () -> pnlCanhBaoHanSuDung = new CanhBaoHanSuDungPanel());
        if (pnlCanhBaoHanSuDung != null) {
            pnlCanhBaoHanSuDung.napLai();
        }
    }

    // Mở gợi ý thức ăn.
    private void moGoiYThucAn() {
        hienManHinh("goiythucan", "Gợi ý thức ăn theo loại thú cưng", btnXuLy,
                () -> pnlGoiYThucAn = new GoiYThucAnPanel());
        if (pnlGoiYThucAn != null) {
            pnlGoiYThucAn.napLai();
        }
    }

    // Nạp tổng quan hôm nay.
    private void napTongQuanHomNay() {
        if (lblDoanhThuHomNay == null)
            return;
        boolean xemThongKe = PhanQuyen.coQuyen(PhanQuyen.Quyen.THONG_KE_DOANH_THU);
        boolean xemSpa = PhanQuyen.coQuyen(PhanQuyen.Quyen.LICH_HEN_SPA);
        boolean xemKho = PhanQuyen.coQuyen(PhanQuyen.Quyen.NHAP_KHO);
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblDoanhThuHomNay.setText("Không có quyền");
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblLuotMuaHomNay.setText("—");
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblLichSpaHomNay.setText("—");
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblPhieuNhapHomNay.setText("—");
        if (xemThongKe) {
            try {
                HoaDonDAO.ThongKe tk = new HoaDonDAO().layThongKe();
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                lblDoanhThuHomNay.setText(formatVnd(tk.getDoanhThuHomNay()));
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                lblLuotMuaHomNay.setText(String.valueOf(tk.getHoaDonHomNay()));
            } catch (Exception ex) {
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                lblDoanhThuHomNay.setText("—");
            }
        }
        if (xemSpa) {
            try {
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                lblLichSpaHomNay.setText(String.valueOf(demLichSpaHomNay()));
            } catch (SQLException ex) {
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                lblLichSpaHomNay.setText("—");
            }
        }
        if (xemKho) {
            try {
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                lblPhieuNhapHomNay.setText(String.valueOf(demPhieuNhapHomNay()));
            } catch (SQLException ex) {
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                lblPhieuNhapHomNay.setText("—");
            }
        }
    }

    // Đếm lịch Spa hôm nay.
    // Kết quả: giá trị số nguyên.
    private int demLichSpaHomNay() throws SQLException {
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "SELECT COUNT(*) FROM LichHen WHERE CAST(ngayGioHen AS DATE) = CAST(GETDATE() AS DATE)";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Đếm phiếu nhập hôm nay.
    // Kết quả: giá trị số nguyên.
    private int demPhieuNhapHomNay() throws SQLException {
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận
        // tham số nếu có.
        String sql = "SELECT COUNT(*) FROM PhieuNhapKho WHERE CAST(ngayNhap AS DATE) = CAST(GETDATE() AS DATE)";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối
        // lệnh.
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Định dạng số tiền theo cách hiển thị tiền Việt Nam.
    // Tham số soTien: số tiền.
    // Kết quả: chuỗi kết quả.
    private String formatVnd(BigDecimal soTien) {
        return UIStyle.tien(soTien == null ? 0 : soTien.doubleValue());
    }

    // Tạo khung lịch Spa sắp tới.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPnlLichSpaSapToi() {
        // pnlCard: Khung chứa thành phần giao diện thẻ.
        JPanel pnlCard = new JPanel(new BorderLayout(0, 5));
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setBorder(new CompoundBorder(new LineBorder(UIStyle.LINE), new EmptyBorder(10, 14, 10, 14)));

        // pnlTren: Khung chứa thành phần giao diện trên.
        JPanel pnlTren = new JPanel(new BorderLayout());
        pnlTren.setOpaque(false);

        // pnlBocTieuDe: Khung chứa thành phần giao diện bọc tiêu đề.
        JPanel pnlBocTieuDe = new JPanel();
        pnlBocTieuDe.setOpaque(false);
        pnlBocTieuDe.setLayout(new BoxLayout(pnlBocTieuDe, BoxLayout.Y_AXIS));

        // lblTitle2: Nhãn hiển thị: Lịch Spa sắp tới.
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

    // Nạp lịch Spa sắp tới.
    private void napLichSpaSapToi() {
        if (pnlLichSpaNoiDung == null || lblPhuDeLichSpa == null)
            return;
        if (!PhanQuyen.coQuyen(PhanQuyen.Quyen.LICH_HEN_SPA)) {
            pnlLichSpaNoiDung.removeAll();
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblPhuDeLichSpa.setText("Không có quyền xem lịch Spa");
            pnlLichSpaNoiDung.revalidate();
            pnlLichSpaNoiDung.repaint();
            return;
        }

        pnlLichSpaNoiDung.removeAll();
        // now: Lưu hiện tại dưới dạng dữ liệu thời gian.
        LocalDateTime now = LocalDateTime.now();

        try {
            // tatCa: Lưu toàn bộ dữ liệu; phần tử có kiểu LichHen.
            List<LichHen> tatCa = new LichHenDAO().getAll();
            // sapToi: Lưu sắp tới; phần tử có kiểu LichHen.
            List<LichHen> sapToi = tatCa.stream()
                    .filter(lh -> lh.getNgayGioHen() != null && !lh.getNgayGioHen().isBefore(now))
                    .sorted(Comparator.comparing(LichHen::getNgayGioHen))
                    .toList();

            // trong90Phut: Lưu trong 90 phút; phần tử có kiểu LichHen.
            List<LichHen> trong90Phut = sapToi.stream()
                    .filter(lh -> Duration.between(now, lh.getNgayGioHen()).toMinutes() <= 90)
                    .limit(3)
                    .toList();

            long soLichHomNay = sapToi.stream()
                    .filter(lh -> lh.getNgayGioHen().toLocalDate().equals(now.toLocalDate()))
                    .count();

            if (!trong90Phut.isEmpty()) {
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                lblPhuDeLichSpa.setText("Trong 90 phút tiếp theo");
                for (LichHen lh : trong90Phut) {
                    pnlLichSpaNoiDung.add(taoDongLichSpa(lh));
                    pnlLichSpaNoiDung.add(Box.createVerticalStrut(3));
                }
                pnlLichSpaNoiDung.add(Box.createVerticalGlue());
                pnlLichSpaNoiDung.add(taoChipTongKet(
                        "Sắp tới: " + trong90Phut.size() + " lịch • Hôm nay: " + soLichHomNay + " lịch"));
            } else if (!sapToi.isEmpty()) {
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                lblPhuDeLichSpa.setText("Hiển thị những lịch hẹn tiếp theo");
                int dem = 0;
                for (LichHen lh : sapToi) {
                    pnlLichSpaNoiDung.add(taoDongLichSpa(lh));
                    pnlLichSpaNoiDung.add(Box.createVerticalStrut(3));
                    dem++;
                    if (dem == 3)
                        break;
                }
                pnlLichSpaNoiDung.add(Box.createVerticalGlue());
                pnlLichSpaNoiDung.add(taoChipTongKet("Hôm nay: " + soLichHomNay + " lịch Spa"));
            } else {
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                lblPhuDeLichSpa.setText("Chưa có lịch hẹn Spa sắp tới");
                // lblRong: Nhãn hiển thị: Không có lịch hẹn nào trong dữ liệu hiện tại..
                JLabel lblRong = new JLabel("Không có lịch hẹn nào trong dữ liệu hiện tại.");
                lblRong.setFont(UIStyle.font(Font.PLAIN, 13));
                lblRong.setForeground(Color.GRAY);
                pnlLichSpaNoiDung.add(lblRong);
            }
        } catch (Exception ex) {
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblPhuDeLichSpa.setText("Không đọc được dữ liệu lịch hẹn");
            // lblLoi: Nhãn hiển thị: Vui lòng kiểm tra kết nối CSDL hoặc dữ liệu Lịch hẹn
            // Spa..
            JLabel lblLoi = new JLabel("Vui lòng kiểm tra kết nối CSDL hoặc dữ liệu Lịch hẹn Spa.");
            lblLoi.setFont(UIStyle.font(Font.PLAIN, 13));
            lblLoi.setForeground(new Color(170, 60, 60));
            pnlLichSpaNoiDung.add(lblLoi);
        }

        pnlLichSpaNoiDung.revalidate();
        pnlLichSpaNoiDung.repaint();
    }

    // Tạo dòng lịch Spa.
    // Tham số lh: lịch hẹn cần xử lý.
    // Kết quả: đối tượng/giá trị kiểu JComponent.
    private JComponent taoDongLichSpa(LichHen lh) {
        String gio = lh.getNgayGioHen() == null ? "--:--" : lh.getNgayGioHen().format(DINH_DANG_GIO_HEN);
        // thuCung: Lưu thú cưng.
        String thuCung = khongRong(lh.getTenThuCung(), "Thú cưng");
        // ghiChu: Lưu ghi chú.
        String ghiChu = khongRong(lh.getGhiChu(), "Chăm sóc Spa");
        // Khong de HTML tu xuong dong lam pnlKhung bi keo rong/le cot.
        String strNoiDung = gio + "  ·  " + thuCung + "  ·  " + ghiChu;
        if (strNoiDung.length() > 62)
            strNoiDung = strNoiDung.substring(0, 59) + "...";
        // lblThongTin: Nhãn hiển thị thông tin.
        JLabel lblThongTin = new JLabel(strNoiDung);
        lblThongTin.setToolTipText(gio + " - " + thuCung + " - " + ghiChu);
        lblThongTin.setFont(UIStyle.font(Font.PLAIN, 12));
        lblThongTin.setForeground(new Color(45, 56, 70));
        lblThongTin.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblThongTin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return lblThongTin;
    }

    // Trả về chuỗi mặc định nếu nội dung đầu vào trống hoặc null.
    // Tham số s: chuỗi đầu vào.
    // Tham số macDinh: mặc định.
    private String khongRong(String s, String macDinh) {
        return (s == null || s.trim().isEmpty()) ? macDinh : s.trim();
    }

    // Tạo nhãn tổng kết.
    // Tham số text: nội dung chữ.
    // Kết quả: đối tượng/giá trị kiểu JComponent.
    private JComponent taoChipTongKet(String text) {
        // lblTam: Nhãn hiển thị tạm.
        JLabel lblTam = new JLabel(text);
        lblTam.setOpaque(true);
        lblTam.setBackground(new Color(223, 245, 232));
        lblTam.setForeground(new Color(28, 123, 81));
        lblTam.setFont(UIStyle.font(Font.BOLD, 11));
        lblTam.setBorder(new CompoundBorder(new LineBorder(new Color(200, 233, 216)), new EmptyBorder(4, 10, 4, 10)));
        // pnlBoc: Khung chứa thành phần giao diện bọc.
        JPanel pnlBoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBoc.setOpaque(false);
        pnlBoc.add(lblTam);
        pnlBoc.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pnlBoc;
    }

    // Mở hộp thoại hỗ trợ.
    private void moModalHoTro() {
        // winHoTro: Hộp thoại hỗ trợ.
        JDialog winHoTro = new JDialog(this, "Hỗ trợ sử dụng Pet Shop Pro", true);
        winHoTro.setSize(760, 560);
        winHoTro.setLocationRelativeTo(this);
        winHoTro.setLayout(new BorderLayout(0, 10));

        // pnlTieuDeHoTro: Khung chứa thành phần giao diện tiêu đề hỗ trợ.
        JPanel pnlTieuDeHoTro = new JPanel(new BorderLayout());
        pnlTieuDeHoTro.setBackground(UIStyle.NAVY);
        pnlTieuDeHoTro.setBorder(new EmptyBorder(14, 18, 14, 18));
        // lblTitle2: Nhãn hiển thị: HỖ TRỢ - TỔNG QUAN CHỨC NĂNG.
        JLabel lblTitle2 = new JLabel("HỖ TRỢ - TỔNG QUAN CHỨC NĂNG", taiIcon("/img/logo.png", 24, 24),
                SwingConstants.LEFT);
        lblTitle2.setForeground(Color.WHITE);
        lblTitle2.setFont(UIStyle.font(Font.BOLD, 18));
        lblTitle2.setIconTextGap(10);
        pnlTieuDeHoTro.add(lblTitle2, BorderLayout.WEST);
        winHoTro.add(pnlTieuDeHoTro, BorderLayout.NORTH);

        // edtEdpHuongDan: Vùng hiển thị văn bản có định dạng vùng văn bản hướng dẫn.
        JEditorPane edtEdpHuongDan = new JEditorPane();
        edtEdpHuongDan.setContentType("text/html");
        edtEdpHuongDan.setEditable(false);
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        edtEdpHuongDan.setText(
                """
                        <html>
                        <body style='font-family:Segoe UI; font-size:12px; padding:8px 10px;'>
                        <h2 style='color:#123052; margin-top:0;'>Pet Shop Pro</h2>
                        <p>Đây là màn hình trung tâm giúp nhân viên thao tác nhanh với các chức năng chính của cửa hàng thú cưng.</p>
                        <h3 style='color:#123052;'>Mô tả menu trên thanh điều hướng</h3>
                        <ul>
                          <li><b>Trang chủ:</b> màn hình tổng quan, truy cập nhanh, lịch Spa sắp tới và hỗ trợ sử dụng.</li>
                          <li><b>Hệ thống:</b> quản lý tài khoản nhân viên, quản lý ca làm, đăng xuất và quay về trang chủ.</li>
                          <li><b>Danh mục:</b> quản lý danh mục sản phẩm, sản phẩm, khách hàng và thú cưng.</li>
                          <li><b>Nghiệp vụ:</b> các thao tác vận hành cửa hàng như nhập kho, kiểm kê, lịch hẹn Spa, phiếu dịch vụ, thanh toán, voucher và phân ca nhân viên.</li>
                          <li><b>Tìm kiếm:</b> tra cứu lịch sử hóa đơn, phiếu nhập kho và phiếu kiểm kê kho; xem chi tiết và lọc theo thông tin liên quan.</li>
                          <li><b>Thống kê:</b> thống kê doanh thu và thống kê lượt mua từ dữ liệu hóa đơn.</li>
                        </ul>
                        <h3 style='color:#123052;'>Khu vực trong trang chủ</h3>
                        <ul>
                          <li><b>Ảnh giới thiệu:</b> hình ảnh nhận diện thương hiệu Pet Shop Pro.</li>
                          <li><b>Truy cập nhanh:</b> mở nhanh các chức năng thường dùng bằng chuột hoặc phím tắt F4-F8.</li>
                          <li><b>Lịch Spa sắp tới:</b> hiển thị lịch hẹn gần nhất lấy từ dữ liệu lịch hẹn Spa.</li>
                          <li><b>Hỗ trợ:</b> mở cửa sổ hướng dẫn tổng quan cách sử dụng giao diện.</li>
                        </ul>
                        <h3 style='color:#123052;'>Gợi ý sử dụng</h3>
                        <ol>
                          <li>Chọn menu chức năng trên thanh điều hướng để mở màn hình tương ứng.</li>
                          <li>Dùng truy cập nhanh khi cần mở nhanh thao tác thường dùng.</li>
                          <li>Kiểm tra lịch Spa sắp tới ở góc dưới bên trái để nắm các lịch hẹn gần nhất.</li>
                          <li>Nhấn tên nhân viên ở góc trên bên phải để xem thông tin đăng nhập hoặc đăng xuất.</li>
                        </ol>
                        <p style='color:#666;'>Lưu ý: một số chức năng phụ thuộc dữ liệu SQL Server. Nếu không hiển thị, hãy kiểm tra kết nối CSDL.</p>
                        </body>
                        </html>
                        """);
        edtEdpHuongDan.setCaretPosition(0);
        // scrHuongDan: Khung cuộn hướng dẫn.
        JScrollPane scrHuongDan = new JScrollPane(edtEdpHuongDan);
        scrHuongDan.setBorder(new EmptyBorder(0, 12, 0, 12));
        winHoTro.add(scrHuongDan, BorderLayout.CENTER);

        // btnDongHoTro: Nút thao tác dòng hỗ trợ.
        JButton btnDongHoTro = UIStyle.nutChinh("Đóng");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnDongHoTro.addActionListener(e -> winHoTro.dispose());
        // pnlDuoi: Khung chứa thành phần giao diện dưới.
        JPanel pnlDuoi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        pnlDuoi.setBackground(Color.WHITE);
        pnlDuoi.add(btnDongHoTro);
        winHoTro.add(pnlDuoi, BorderLayout.SOUTH);

        winHoTro.setVisible(true);
    }

    // Thêm nút truy cấp nhanh.
    // Tham số pnlParent: thành phần cha.
    // Tham số text: nội dung chữ.
    // Tham số shortcut: phím tắt.
    // Tham số quyen: quyền.
    // Tham số hanhDong: hành động.
    private void themBtnTruyCapNhanh(JPanel pnlParent, String text, String shortcut,
            PhanQuyen.Quyen quyen, Runnable hanhDong) {
        // pnlDong: Khung chứa thành phần giao diện dòng dữ liệu.
        JPanel pnlDong = new JPanel(new BorderLayout());
        pnlDong.setBackground(MENU_BLUE);
        pnlDong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // btnTruyCap: Nút thao tác truy cấp.
        JButton btnTruyCap = new JButton(text);
        btnTruyCap.setHorizontalAlignment(SwingConstants.LEFT);
        btnTruyCap.setFont(UIStyle.font(Font.PLAIN, 14));
        btnTruyCap.setBackground(MENU_BLUE);
        btnTruyCap.setFocusPainted(false);
        btnTruyCap.setBorderPainted(false);
        btnTruyCap.setEnabled(PhanQuyen.coQuyen(quyen));
        if (!btnTruyCap.isEnabled())
            btnTruyCap.setToolTipText("Vai trò hiện tại không có quyền");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTruyCap.addActionListener(e -> {
            if (PhanQuyen.kiemTra(quyen, this))
                hanhDong.run();
        });

        String khoa = "truycapnhanh-" + shortcut;
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(shortcut), khoa);
        getRootPane().getActionMap().put(khoa, new AbstractAction() {
            // Xử lý hành động được phát ra từ nút, pmnMenu hoặc phím tắt.
            // Tham số e: thông tin sự kiện.
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (PhanQuyen.kiemTra(quyen, TrangChu.this))
                    hanhDong.run();
            }
        });

        // lblPhimTat: Nhãn hiển thị phím tất.
        JLabel lblPhimTat = new JLabel(shortcut);
        lblPhimTat.setFont(UIStyle.font(Font.BOLD, 14));
        lblPhimTat.setBorder(new EmptyBorder(0, 0, 0, 10));

        pnlDong.add(btnTruyCap, BorderLayout.CENTER);
        pnlDong.add(lblPhimTat, BorderLayout.EAST);

        pnlParent.add(pnlDong);
        pnlParent.add(Box.createVerticalStrut(5));
    }

    // =========================================================
    // PHIEN LAM VIEC (nhan vien dang dang nhap, dung chung)
    // =========================================================

    // Nhóm dữ liệu hoặc chức năng phiên làm việc dùng trong TrangChu.
    public static final class PhienLamViec {
        // maNV: Lưu mã nhân viên.
        public static String maNV;
        // hoTen: Lưu họ tên.
        public static String hoTen;
        // vaiTro: Lưu vai trò.
        public static String vaiTro;

        // Ẩn hàm khởi tạo vì lớp chỉ cung cấp các hàm tiện ích dùng chung.
        private PhienLamViec() {
        }

        // Ghi thông tin nhân viên vào phiên làm việc dùng chung.
        // Tham số ma: mã.
        // Tham số ten: tên.
        // Tham số role: vai trò.
        public static void dat(String ma, String ten, String role) {
            maNV = ma;
            hoTen = ten;
            vaiTro = role;
        }
    }

    // =========================================================
    // MAU SAC, FONT, THANH PHAN DUNG CHUNG
    // =========================================================

    // Nhóm dữ liệu hoặc chức năng u i kiểu hiển thị dùng trong TrangChu.
    public static final class UIStyle {
        // NAVY: Màu dùng cho giao diện: xanh đậm.
        public static final Color NAVY = new Color(18, 48, 82);
        // LIGHT_BLUE: Màu dùng cho giao diện: xanh dương.
        public static final Color LIGHT_BLUE = new Color(229, 241, 252);
        // MENU_BLUE: Màu dùng cho giao diện: xanh dương.
        public static final Color MENU_BLUE = new Color(207, 222, 235);
        // ACTIVE_BLUE: Màu dùng cho giao diện: xanh dương.
        public static final Color ACTIVE_BLUE = new Color(169, 194, 218);
        // GREEN: Màu dùng cho giao diện: xanh lá.
        public static final Color GREEN = new Color(72, 196, 62);
        // RED: Màu dùng cho giao diện: đỏ.
        public static final Color RED = new Color(200, 40, 40);
        // LINE: Màu dùng cho giao diện: đường.
        public static final Color LINE = new Color(210, 218, 226);
        // BAR: Màu dùng cho giao diện: thanh.
        public static final Color BAR = new Color(247, 249, 251);
        // READONLY: Màu dùng cho giao diện: chỉ đọc.
        public static final Color READONLY = new Color(235, 235, 235);

        // Ẩn hàm khởi tạo vì lớp chỉ cung cấp các hàm tiện ích dùng chung.
        private UIStyle() {
        }

        // Tạo phông chữ với kiểu và kích thước được chỉ định.
        // Tham số style: kiểu hiển thị.
        // Tham số size: kích thước.
        // Kết quả: đối tượng/giá trị kiểu Font.
        public static Font font(int style, int size) {
            return new Font("Segoe UI", style, size);
        }

        // DINH_DANG_TIEN: Bộ định dạng số/tiền dùng để hiển thị dữ liệu trên giao diện.
        private static final DecimalFormat DINH_DANG_TIEN;

        static {
            DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
            s.setGroupingSeparator('.');
            DINH_DANG_TIEN = new DecimalFormat("#,##0", s);
        }

        // Định dạng số tiền thành chuỗi để hiển thị.
        // Tham số v: giá trị đầu vào.
        // Kết quả: chuỗi kết quả.
        public static String tien(double v) {
            return DINH_DANG_TIEN.format(v) + " ₫";
        }

        // Đọc chuỗi tiền trên giao diện thành giá trị số.
        // Tham số s: chuỗi đầu vào.
        // Kết quả: đối tượng/giá trị kiểu long.
        public static long docTien(String s) {
            String sach = s == null ? "" : s.replaceAll("[\\s.,₫dđ]", "");
            if (!sach.matches("\\d{1,15}")) {
                throw new NumberFormatException(s);
            }
            return Long.parseLong(sach);
        }

        // Tạo nút thao tác với nội dung và màu sắc được chỉ định.
        // Tham số text: nội dung chữ.
        // Tham số nen: màu nền.
        // Tham số chu: màu chữ.
        // Tham số vien: màu viền.
        // Kết quả: đối tượng/giá trị kiểu JButton.
        private static JButton nut(String text, Color nen, Color chu, Color vien) {
            // btnTam: Nút thao tác tạm.
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

        // Tạo nút cho thao tác chính theo kiểu giao diện chung.
        // Tham số text: nội dung chữ.
        // Kết quả: đối tượng/giá trị kiểu JButton.
        public static JButton nutChinh(String text) {
            return nut(text, NAVY, Color.WHITE, null);
        }

        // Tạo nút màu xanh dùng cho thao tác xác nhận.
        // Tham số text: nội dung chữ.
        // Kết quả: đối tượng/giá trị kiểu JButton.
        public static JButton nutXanh(String text) {
            return nut(text, GREEN, Color.WHITE, null);
        }

        // Tạo nút cho thao tác phụ theo kiểu giao diện chung.
        // Tham số text: nội dung chữ.
        // Kết quả: đối tượng/giá trị kiểu JButton.
        public static JButton nutPhu(String text) {
            return nut(text, Color.WHITE, NAVY, NAVY);
        }

        // Tạo nút cho thao tác xóa theo kiểu giao diện chung.
        // Tham số text: nội dung chữ.
        // Kết quả: đối tượng/giá trị kiểu JButton.
        public static JButton nutXoa(String text) {
            return nut(text, Color.WHITE, RED, LINE);
        }

        // Tạo nút chuyển tab và đánh dấu tab đang chọn.
        // Tham số text: nội dung chữ.
        // Tham số btnDangChon: mục đang chọn.
        // Kết quả: đối tượng/giá trị kiểu JButton.
        public static JButton nutTab(String text, boolean dangChon) {
            return nut(text, dangChon ? ACTIVE_BLUE : Color.WHITE,
                    dangChon ? Color.WHITE : NAVY, NAVY);
        }

        // Tạo nhãn chữ theo kiểu giao diện dùng chung.
        // Tham số text: nội dung chữ.
        // Kết quả: đối tượng/giá trị kiểu JLabel.
        public static JLabel nhan(String text) {
            // lblNhan3: Nhãn dùng để hiển thị chữ hoặc giá trị trên giao diện.
            JLabel lblNhan3 = new JLabel(text);
            lblNhan3.setFont(font(Font.BOLD, 13));
            return lblNhan3;
        }

        // Tạo ô nhập văn bản với độ rộng theo số cột.
        // Tham số cot: cột.
        // Kết quả: đối tượng/giá trị kiểu JTextField.
        public static JTextField o(int cot) {
            // t: Ô nhập/hiển thị văn bản bảng/thành phần được xử lý.
            JTextField txtVanBan = new JTextField(cot);
            txtVanBan.setFont(font(Font.PLAIN, 13));
            return txtVanBan;
        }

        // Tạo ô văn bản chỉ đọc để hiển thị dữ liệu.
        // Tham số lblGiaTri: nhãn giá trị.
        // Kết quả: đối tượng/giá trị kiểu JTextField.
        public static JTextField oChiDoc(String lblGiaTri) {
            // t: Ô nhập/hiển thị văn bản bảng/thành phần được xử lý.
            JTextField txtVanBan = o(10);
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtVanBan.setText(lblGiaTri);
            txtVanBan.setEditable(false);
            txtVanBan.setBackground(READONLY);
            return txtVanBan;
        }

        // Bố trí nhãn và thành phần nhập liệu vào một hàng của biểu mẫu.
        // Tham số pnlForm2: biểu mẫu.
        // Tham số dong: dòng dữ liệu.
        // Tham số ten: tên.
        // Tham số thanhPhan: thanh phần.
        // Tham số cao: cao.
        public static void hang(JPanel pnlForm2, int dong, String ten, JComponent thanhPhan, boolean cao) {
            // g: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng
            // GridBagLayout.
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

        // Tạo khung nội dung theo kiểu giao diện dùng chung.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        public static JPanel the() {
            // pnlKetQua: Khung chứa thành phần giao diện kết quả.
            JPanel pnlKetQua = new JPanel();
            pnlKetQua.setBackground(Color.WHITE);
            pnlKetQua.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(15, 15, 15, 15)));
            return pnlKetQua;
        }

        // Tạo thẻ hiển thị tiêu đề và giá trị thống kê.
        // Tham số tieuDe: tiêu đề.
        // Tham số lblGiaTri: nhãn giá trị.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        public static JPanel theThongKe(String tieuDe, JLabel lblGiaTri) {
            // pnlKetQua: Khung chứa thành phần giao diện kết quả.
            JPanel pnlKetQua = new JPanel();
            pnlKetQua.setLayout(new BoxLayout(pnlKetQua, BoxLayout.Y_AXIS));
            pnlKetQua.setBackground(Color.WHITE);
            pnlKetQua.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(14, 18, 14, 18)));

            // lblTam: Nhãn hiển thị tạm.
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

        // Tạo bảng dữ liệu.
        // Tham số tblDuLieu2: bảng dữ liệu.
        // Tham số cotCanPhai: cột cần phải.
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

            // header: Phần tiêu đề cột của bảng đầu trang.
            JTableHeader tbhHeader = tblDuLieu2.getTableHeader();
            tbhHeader.setReorderingAllowed(false);
            tbhHeader.setPreferredSize(new Dimension(0, 40));
            tbhHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
                // Thiết lập thành phần dùng để vẽ một ô của bảng.
                // Tham số t: bảng đang hiển thị.
                // Tham số v: giá trị của ô.
                // Tham số sel: trạng thái được chọn.
                // Tham số foc: trạng thái có tiêu điểm.
                // Tham số r: chỉ số dòng.
                // Tham số c: chỉ số cột.
                // Kết quả: đối tượng/giá trị kiểu Component.
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
                // Thiết lập thành phần dùng để vẽ một ô của bảng.
                // Tham số t: bảng đang hiển thị.
                // Tham số v: giá trị của ô.
                // Tham số sel: trạng thái được chọn.
                // Tham số foc: trạng thái có tiêu điểm.
                // Tham số r: chỉ số dòng.
                // Tham số c: chỉ số cột.
                // Kết quả: đối tượng/giá trị kiểu Component.
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

        // Bọc bảng trong khung cuộn.
        // Tham số tblDuLieu2: bảng dữ liệu.
        // Kết quả: đối tượng/giá trị kiểu JScrollPane.
        public static JScrollPane cuon(JTable tblDuLieu2) {
            // scrHuongDan: Khung cuộn hướng dẫn.
            JScrollPane scrHuongDan = new JScrollPane(tblDuLieu2);
            scrHuongDan.setBorder(new LineBorder(LINE));
            scrHuongDan.getViewport().setBackground(Color.WHITE);
            return scrHuongDan;
        }

        // Thông báo dữ liệu của màn hình.
        // Tham số cha: thành phần cha.
        // Tham số strNoiDung: chuỗi nội dung.
        public static void thongBao(Component cha, String strNoiDung) {
            JOptionPane.showMessageDialog(cha, strNoiDung, "Pet Shop Pro", JOptionPane.INFORMATION_MESSAGE);
        }

        // Xuất PDF.
        // Tham số cha: thành phần cha.
        // Tham số tieuDe: tiêu đề.
        // Tham số tblDuLieu2: bảng dữ liệu.
        public static void xuatPdf(Component cha, String tieuDe, JTable tblDuLieu2) {
            // fchChonTep: Hộp chọn tệp bộ chọn.
            JFileChooser fchChonTep = new JFileChooser();
            fchChonTep.setDialogTitle("Lưu " + tieuDe + " PDF");
            fchChonTep.setSelectedFile(new java.io.File(
                    tieuDe.trim().replaceAll("[^\\p{L}\\p{N}]+", "-") + ".pdf"));
            fchChonTep.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Tệp PDF (*.pdf)", "pdf"));
            if (fchChonTep.showSaveDialog(cha) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            // file: Đường dẫn tệp dùng cho tệp.
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

        // Tạo PDF bảng.
        // Tham số tieuDe: tiêu đề.
        // Tham số tblDuLieu2: bảng dữ liệu.
        // Kết quả: đối tượng/giá trị kiểu byte[].
        private static byte[] taoPdfBang(String tieuDe, JTable tblDuLieu2) throws IOException {
            int tableWidth = Math.max(1, tblDuLieu2.getPreferredSize().width);
            int tableHeight = Math.max(1, tblDuLieu2.getTableHeader().getPreferredSize().height
                    + tblDuLieu2.getRowCount() * tblDuLieu2.getRowHeight());
            int imageWidth = 1400;
            int imageHeight = Math.max(120, (int) Math.ceil(
                    (double) tableHeight * imageWidth / tableWidth) + 90);
            // image: Ảnh trong bộ nhớ dùng cho hình ảnh.
            BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
            // g: Ngữ cảnh vẽ dùng để thiết lập màu, phông và vẽ nội dung.
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

            // compressed: Luồng ghi dữ liệu dùng khi tạo hoặc đóng gói tệp xuất.
            ByteArrayOutputStream compressed = new ByteArrayOutputStream();
            try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressed)) {
                // pixels: Mảng lưu các điểm ảnh.
                int[] pixels = new int[imageWidth];
                // row: Mảng lưu chỉ số dòng.
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

            // imageData: Mảng lưu ảnh dữ liệu.
            byte[] imageData = compressed.toByteArray();
            int pageHeight = Math.max(1, (int) Math.ceil(842.0 * imageHeight / imageWidth));
            // objects: Lưu các đối tượng; phần tử có kiểu byte[].
            java.util.List<byte[]> objects = new ArrayList<>();
            objects.add(pdfBytes("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"));
            objects.add(pdfBytes("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n"));
            objects.add(pdfBytes("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 842 "
                    + pageHeight + "] /Resources << /XObject << /Im0 5 0 R >> >> /Contents 4 0 R >>\nendobj\n"));
            // content: Mảng lưu nội dung.
            byte[] content = pdfBytes("q\n842 0 0 " + pageHeight + " 0 0 cm\n/Im0 Do\nQ\n");
            objects.add(pdfStream(4, "", content));
            objects.add(pdfStream(5, "/Type /XObject /Subtype /Image /Width " + imageWidth
                    + " /Height " + imageHeight + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /FlateDecode",
                    imageData));

            // pdf: Luồng ghi dữ liệu dùng khi tạo hoặc đóng gói tệp xuất.
            ByteArrayOutputStream pdf = new ByteArrayOutputStream();
            pdf.write(pdfBytes("%PDF-1.4\n"));
            // offsets: Lưu các vị trí byte; phần tử có kiểu Integer.
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

        // Tạo đối tượng stream trong cấu trúc tệp PDF.
        // Tham số number: số.
        // Tham số dictionary: từ điển.
        // Tham số data: dữ liệu.
        // Kết quả: đối tượng/giá trị kiểu byte[].
        private static byte[] pdfStream(int number, String dictionary, byte[] data) throws IOException {
            // object: Luồng ghi dữ liệu dùng khi tạo hoặc đóng gói tệp xuất.
            ByteArrayOutputStream object = new ByteArrayOutputStream();
            object.write(pdfBytes(number + " 0 obj\n<< " + dictionary + " /Length " + data.length
                    + " >>\nstream\n"));
            object.write(data);
            object.write(pdfBytes("\nendstream\nendobj\n"));
            return object.toByteArray();
        }

        // Chuyển chuỗi nội dung PDF thành mảng byte.
        // Tham số value: giá trị.
        // Kết quả: đối tượng/giá trị kiểu byte[].
        private static byte[] pdfBytes(String value) {
            return value.getBytes(StandardCharsets.ISO_8859_1);
        }

        // Cảnh báo dữ liệu của màn hình.
        // Tham số cha: thành phần cha.
        // Tham số strNoiDung: chuỗi nội dung.
        public static void canhBao(Component cha, String strNoiDung) {
            JOptionPane.showMessageDialog(cha, strNoiDung, "Pet Shop Pro", JOptionPane.WARNING_MESSAGE);
        }

        // Xác nhận dữ liệu của màn hình.
        // Tham số cha: thành phần cha.
        // Tham số strNoiDung: chuỗi nội dung.
        // Kết quả: true/false theo điều kiện xử lý.
        public static boolean xacNhan(Component cha, String strNoiDung) {
            return JOptionPane.showConfirmDialog(cha, strNoiDung, "Pet Shop Pro",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
        }

        // Hiển thị thông báo lỗi từ ngoại lệ nhận được.
        // Tham số cha: thành phần cha.
        // Tham số ex: ngoại lệ cần xử lý.
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

        // Sắp xếp các nút hủy và lưu trong một hàng.
        // Tham số btnHuy2: hủy.
        // Tham số btnLuu2: lưu.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        public static JPanel hangNut(JButton btnHuy2, JButton btnLuu2) {
            // pnlKetQua: Khung chứa thành phần giao diện kết quả.
            JPanel pnlKetQua = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnlKetQua.setOpaque(false);
            pnlKetQua.add(btnHuy2);
            pnlKetQua.add(btnLuu2);
            return pnlKetQua;
        }
    }
}
