package UI;

import control.TrangChuControl;
import dao.PhanQuyen;
import control.PhanQuyenControl;

import entity.LichHen;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
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

/**
 * JFrame: cửa sổ chính của Pet Shop Pro.
 * ActionListener: nhận sự kiện từ JButton/JMenuItem; MouseListener: các
 * callback chuột.
 * JPanel + CardLayout: chứa và chuyển giữa các chức năng ngay trong một cửa sổ.
 */
public class TrangChuUI extends JFrame implements ActionListener, MouseListener {

    // Color: quy định màu nền của Trang chủ; final: không gán lại biến sau khi khởi
    // tạo.
    private final Color LIGHT_BLUE = new Color(229, 241, 252);
    private final Color MENU_BLUE = new Color(207, 222, 235);
    // Ba thuộc tính private: lưu thông tin đăng nhập; final: chỉ gán một lần trong
    // constructor.
    private final String maNV;
    private final String hoTen;
    private final String vaiTro;
    private final TrangChuControl control = new TrangChuControl();

    // Mảng hằng tên bốn màn hình kho, dùng chung khi chọn tab.
    private static final String[] TEN_TAB = {
            "Danh mục", "Sản phẩm", "Nhập kho", "Kiểm kê kho"
    };
    // DateTimeFormatter: quy định cách hiển thị đồng hồ trên giao diện.
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm:ss | dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO_HEN = DateTimeFormatter.ofPattern("HH:mm");

    // CardLayout: giữ nhiều màn hình trong một khung và chỉ hiện màn hình được
    // chọn.
    private final CardLayout cardLayout = new CardLayout();
    // JPanel: khung chứa màn hình; đặt CardLayout để điều hướng không cần mở JFrame
    // mới.
    private final JPanel pnlNoiDung = new JPanel(cardLayout);
    private JLabel lblNgayTrangChu;
    private JButton btnTrangChu;
    private JButton btnHeThong;
    private JButton btnDanhMuc;
    private JButton btnXuLy;
    private JButton btnTimKiem;
    private JButton btnThongKe;
    private JButton btnNhanVien, btnLamMoi, btnHoTro;

    // ArrayList<JButton>: lưu các nút menu để đổi màu trạng thái đồng loạt.
    private final ArrayList<JButton> dsBtnMucChinh = new ArrayList<>();
    private JButton btnNhomDaChon;

    // Mảng JPanel: lưu bốn panel Kho để tránh khởi tạo lặp.
    private final JPanel[] arrPnlManHinhKho = new JPanel[TEN_TAB.length];
    // Runnable: giữ lời gọi nạp lại dữ liệu cho từng màn hình Kho.
    private final Runnable[] napLai = new Runnable[TEN_TAB.length];
    // HashMap: ánh xạ mã chức năng -> giao diện Swing đã tạo.
    private final Map<String, Component> mapManHinhKhac = new HashMap<>();
    private TraCuuLichSuHoaDonUI pnlTraCuuHoaDon;
    private TraCuuKhoUI pnlTraCuuNhapKho;
    private TraCuuKhoUI pnlTraCuuKiemKe;
    private ThongKeDoanhThuUI pnlThongKeDoanhThu;
    private QuanLyThanhToanUI pnlThanhToan;
    // Swing Timer: phát sự kiện định kỳ để cập nhật đồng hồ, không truy vấn thống
    // kê mỗi giây.
    private final javax.swing.Timer tmrDongHo;
    private CanhBaoHanSuDungUI pnlCanhBaoHanSuDung;
    private GoiYThucAnUI pnlGoiYThucAn;

    private JPanel pnlLichSpaNoiDung;
    private JLabel lblPhuDeLichSpa;
    private JLabel lblDoanhThuHomNay;
    private JLabel lblLuotMuaHomNay;
    private JLabel lblLichSpaHomNay;
    private JLabel lblPhieuNhapHomNay;

    /**
     * Constructor: nhận thông tin nhân viên từ màn hình đăng nhập.
     * Kiểm tra vai trò, lưu phiên, dựng JFrame và bắt đầu đồng hồ.
     */
    public TrangChuUI(String maNV, String hoTen, String vaiTro) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;

        // Validation: từ chối mở cửa sổ khi mã nhân viên rỗng hoặc vai trò không hợp
        // lệ.
        control.kiemTraPhien(maNV, vaiTro);

        // Lưu phiên đăng nhập dùng trong TrangChu và các chức năng liên quan.
        PhienLamViec.dat(maNV, hoTen, vaiTro);
        // Đồng bộ phiên làm việc với lớp UI.PhienLamViec của các màn hình khác.
        UI.PhienLamViecUI.dat(maNV, hoTen, vaiTro);

        // JFrame.setTitle(): đặt tiêu đề cửa sổ ứng dụng.
        setTitle("Pet Shop Pro - Trang chủ");
        // setSize(): đặt kích thước khởi đầu của JFrame (rộng x cao).
        setSize(1200, 720);
        setMinimumSize(new Dimension(980, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // BorderLayout: chia cửa sổ thành các vùng Bắc, Nam, Đông, Tây và Giữa.
        setLayout(new BorderLayout());
        // Đưa thanh menu vào vùng NORTH của cửa sổ.
        add(taoPnlHeader(), BorderLayout.NORTH);

        pnlNoiDung.setBackground(LIGHT_BLUE);
        // Đăng ký thẻ Trang chủ với khóa "trangchu" trong CardLayout.
        pnlNoiDung.add(taoPnlTrangChu(), "trangchu");
        add(pnlNoiDung, BorderLayout.CENTER);

        // Timer 1000 ms: mỗi khoảng 1 giây thực hiện lambda cập nhật đồng hồ.
        tmrDongHo = new javax.swing.Timer(1000, e -> capNhatGio());
        tmrDongHo.start();
        capNhatGio();
        capNhatQuyenMenu();
        hienTrangChu();
    }

    /**
     * Đóng cửa sổ TrangChu.
     * Dừng Swing Timer trước khi gọi dispose() của JFrame để không phát sinh sự
     * kiện thừa.
     */
    @Override
    public void dispose() {
        tmrDongHo.stop();
        super.dispose();
    }

    /**
     * Quay về card "trangchu" trong CardLayout.
     * Bỏ đánh dấu nhóm menu và nạp lại các lịch Spa sắp tới.
     */
    private void hienTrangChu() {
        btnNhomDaChon = null;
        chuyenManHinh("trangchu", "Trang chủ", btnTrangChu);
        napLichSpaSapToi();
    }

    /**
     * Hàm dùng chung để hiển thị một card trong JPanel nội dung.
     * CardLayout.show() chọn màn hình; các câu lệnh còn lại cập nhật menu, tiêu đề
     * và bố cục.
     */
    private void chuyenManHinh(String maManHinh, String tieuDe, JButton nutNhom) {
        // CardLayout.show(): chuyển sang card được định danh bởi maManHinh.
        cardLayout.show(pnlNoiDung, maManHinh);
        datMucDangChon(nutNhom);
        setTitle("Pet Shop Pro - " + tieuDe);
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
    }

    /**
     * Chuyển đến tab Danh mục, Sản phẩm, Nhập kho hoặc Kiểm kê.
     * switch expression xác định quyền; màn hình chỉ khởi tạo lần đầu và được nạp
     * lại khi mở tiếp.
     */
    public void chuyenKho(int tab) {
        if (tab < 0 || tab >= TEN_TAB.length)
            tab = 0;
        // Quyền được áp dụng khi bật/tắt JMenuItem trong menu.
        try {
            // Lazy Initialization: chỉ tạo giao diện lần đầu tiên, tránh tạo thừa đối
            // tượng.
            boolean laLanDau = arrPnlManHinhKho[tab] == null;
            if (laLanDau) {
                taoManHinh(tab);
                pnlNoiDung.add(arrPnlManHinhKho[tab], "tab" + tab);
            }
            chuyenManHinh("tab" + tab, TEN_TAB[tab],
                    btnNhomDaChon != null ? btnNhomDaChon : btnDanhMuc);
            // Khi mở lại tab cũ, chạy Runnable để tải dữ liệu mới.
            if (!laLanDau && napLai[tab] != null)
                napLai[tab].run();
        } catch (RuntimeException ex) {
            baoLoiMoManHinh(TEN_TAB[tab], ex);
        }
    }

    /**
     * Mở màn hình thống kê doanh thu.
     * Tái sử dụng màn hình đã tạo và gọi lamMoi() khi mở lại.
     */
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

    /**
     * Tạo một trong bốn màn hình kho dựa vào chỉ số tab.
     * Lấy content pane của màn hình con để nhúng vào CardLayout; lưu Runnable dùng
     * để nạp lại.
     */
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

    /**
     * Hàm điều hướng chung cho các chức năng không thuộc nhóm tab kho.
     * PhanQuyen chặn truy cập trái quyền; HashMap lưu Component; Supplier.get() chỉ
     * tạo màn hình khi cần (lazy initialization).
     */
    private void hienManHinh(String khoa, String tieuDe, JButton btnNhomMacDinh,
            Supplier<? extends Component> tao) {
        // Màn hình được mở từ các mục menu đã bật theo vai trò.
        try {
            // Lấy màn hình theo khóa trong HashMap; trả về null nếu chưa được tạo.
            Component c = mapManHinhKhac.get(khoa);
            if (c == null) {
                // Supplier.get(): gọi constructor được truyền vào để tạo giao diện khi cần.
                c = tao.get();
                // Lưu giao diện vào HashMap để những lần sau không tạo mới.
                mapManHinhKhac.put(khoa, c);
                // Đăng ký Component mới vào CardLayout với khóa duy nhất.
                pnlNoiDung.add(c, khoa);
            }
            chuyenManHinh(khoa, tieuDe,
                    btnNhomDaChon != null ? btnNhomDaChon : btnNhomMacDinh);
        } catch (RuntimeException ex) {
            baoLoiMoManHinh(tieuDe, ex);
        }
    }

    /**
     * Thông báo khi không tạo hoặc hiển thị được màn hình.
     * Ghi stack trace vào Console và hiện JOptionPane để người dùng biết lỗi.
     */
    private void baoLoiMoManHinh(String ten, RuntimeException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Không thể mở màn hình " + ten + ".\n"
                        + "Vui lòng kiểm tra kết nối CSDL hoặc Console.\n"
                        + ex.getMessage(),
                "Pet Shop Pro - Lỗi mở màn hình", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Mở Quản lý lịch hẹn Spa.
     * Truyền constructor reference QuanLyLichHen::new vào hàm điều hướng.
     */
    private void moLichHen() {
        hienManHinh("lichhen", "Lịch hẹn Spa", btnXuLy, QuanLyLichHenUI::new);
    }

    /**
     * Mở Quản lý thú cưng.
     * Màn hình được quản lý bởi hienManHinh().
     */
    private void moThuCung() {
        hienManHinh("thucung", "Quản lý thú cưng", btnDanhMuc, QuanLyThuCungUI::new);
    }

    /**
     * Mở Quản lý khách hàng.
     * Lambda truyền mã nhân viên đăng nhập vào constructor của màn hình con.
     */
    private void moKhachHang() {
        hienManHinh("khachhang", "Quản lý khách hàng", btnDanhMuc,
                () -> new QuanLyKhachHangUI(maNV));
    }

    /**
     * Mở Quản lý mã giảm giá.
     * Tái sử dụng phương thức điều hướng chung.
     */
    private void moVoucher() {
        hienManHinh("voucher", "Quản lý voucher", btnDanhMuc, QuanLyVoucherUI::new);
    }

    /**
     * Mở Quản lý phiếu dịch vụ.
     * Kiểm tra quyền trước khi tạo hoặc hiển thị JPanel nghiệp vụ.
     */
    private void moPhieuDichVu() {
        hienManHinh("phieudv", "Quản lý phiếu dịch vụ", btnXuLy,
                QuanLyPhieuDichVuUI::new);
    }

    /**
     * Mở trang tra cứu lịch sử hóa đơn.
     * Dùng đối tượng đã tạo khi chuyển qua lại giữa các chức năng.
     */
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

    /**
     * Mở tra cứu phiếu nhập kho.
     * Sử dụng giao diện tra cứu kho và cập nhật dữ liệu theo trạng thái màn hình.
     */
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

    /**
     * Mở tra cứu phiếu kiểm kê.
     * Sử dụng giao diện tra cứu kho dành cho dữ liệu kiểm kê.
     */
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

    /**
     * Mở giao diện thanh toán.
     * Giữ và tái sử dụng màn hình thanh toán trong vùng nội dung.
     */
    private void moThanhToan() {
        hienManHinh("thanhtoan", "Thanh toán", btnXuLy,
                () -> {
                    pnlThanhToan = new QuanLyThanhToanUI(maNV,
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

    /**
     * Làm mới dữ liệu phiếu dịch vụ khi cần.
     * Gọi chức năng cập nhật của màn hình đã mở, không khởi tạo màn hình mới.
     */
    private void capNhatDanhSachPhieuDichVu() {
        Component manHinh = mapManHinhKhac.get("phieudv");
        if (manHinh instanceof QuanLyPhieuDichVuUI) {
            ((QuanLyPhieuDichVuUI) manHinh).loadData();
        }
    }

    /**
     * Mở thanh toán với các mặt hàng được chọn từ phần gợi ý thức ăn.
     * Liên kết luồng gợi ý sản phẩm với luồng thanh toán.
     */
    public void moThanhToanTuGioHang() {
        if (UI.PhienLamViecUI.gioHang().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống.", "Giỏ hàng",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        chuyenTuNhom(btnXuLy, this::moThanhToan);
    }

    /**
     * Mở giao diện ma trận phân quyền.
     * Dành cho vai trò được cấp quyền cấu hình hệ thống.
     */
    private void moMaTranPhanQuyen() {
        hienManHinh("phanquyen", "Ma trận phân quyền", btnHeThong,
                () -> new MaTranPhanQuyenUI(this::moQuanLyTaiKhoan));
    }

    /**
     * Mở giao diện quản lý tài khoản.
     * Gọi hienManHinh() để tạo (nếu cần) và hiển thị.
     */
    private void moQuanLyTaiKhoan() {
        hienManHinh("taikhoan", "Quản lý tài khoản", btnHeThong,
                QuanLyNhanVienUI::new);
    }

    /**
     * Mở giao diện phân ca nhân viên.
     * Tạo JPanel chỉ khi lần đầu mở chức năng.
     */
    private void moQuanLyPhanCa() {
        hienManHinh("phanca", "Phân ca nhân viên", btnXuLy,
                QuanLyPhanCaUI::new);
    }

    /**
     * Mở giao diện quản lý ca làm.
     * Tạo hoặc tái sử dụng màn hình theo mã định danh.
     */
    private void moQuanLyCaLam() {
        hienManHinh("calam", "Quản lý ca làm", btnHeThong,
                QuanLyCaLamUI::new);
    }

    /**
     * Mở giao diện quản lý dịch vụ.
     * Có thể gọi lại hàm làm mới dữ liệu của màn hình đã mở.
     */
    private void moDichVu() {
        hienManHinh("dichvu", "Quản lý dịch vụ", btnDanhMuc, QuanLyDichVuUI::new);
        Component manHinh = mapManHinhKhac.get("dichvu");
        if (manHinh instanceof QuanLyDichVuUI) {
            ((QuanLyDichVuUI) manHinh).lamMoi();
        }
    }

    /**
     * Ghi nhận nhóm menu hiện tại rồi thực hiện hành động được truyền vào.
     * Runnable.run() là gọi phương thức, KHÔNG tự tạo luồng mới.
     */
    private void chuyenTuNhom(JButton btnNhom, Runnable hanhDong) {
        btnNhomDaChon = btnNhom;
        // Runnable.run(): gọi hành động ngay trên luồng hiện tại, không tạo Thread mới.
        hanhDong.run();
    }

    /**
     * Kiểm tra menu thả xuống có ít nhất một mục đang được bật hay không.
     * Hỗ trợ khóa các nhóm menu không có chức năng phù hợp với quyền nhân viên.
     */
    private boolean coMucMenu(JPopupMenu pmnMenu) {
        for (Component c : pmnMenu.getComponents())
            if (c instanceof JMenuItem item && item.isEnabled())
                return true;
        return false;
    }

    /**
     * Cập nhật trạng thái bật/tắt của các nút menu chính.
     * Khóa nhóm menu nếu tất cả mục bên trong đều bị vô hiệu hóa theo vai trò.
     */
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

    /**
     * Đổi màu và trạng thái để nhận biết nhóm menu đang được chọn.
     * Duyệt danh sách JButton và cập nhật thuộc tính màu nền.
     */
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

    /**
     * Hiển thị thời gian hiện tại ở đầu Trang chủ.
     * LocalDateTime.now() lấy thời gian, DateTimeFormatter chuyển sang chuỗi
     * HH:mm:ss | dd/MM/yyyy.
     */
    private void capNhatGio() {
        if (lblNgayTrangChu != null) {
            lblNgayTrangChu.setText(LocalDateTime.now().format(DINH_DANG_GIO));
        }
    }

    /**
     * Tạo thanh điều hướng nằm phía trên cửa sổ.
     * JPanel = khung chứa; JLabel = chữ/logo; JButton = nút; BorderLayout chia
     * thương hiệu, menu và tài khoản.
     */
    private JPanel taoPnlHeader() {
        // JPanel: tạo khung thanh menu; BorderLayout chia làm ba vùng chính.
        JPanel pnlHeader2 = new JPanel(new BorderLayout(8, 0));
        pnlHeader2.setBackground(UIStyle.NAVY);
        pnlHeader2.setBorder(new EmptyBorder(7, 12, 7, 12));

        // FlowLayout: đặt logo và tên thương hiệu theo hàng ngang.
        JPanel pnlThuongHieu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlThuongHieu.setOpaque(false);
        // JLabel: tạo nhãn hiển thị logo kèm chữ PET SHOP PRO.
        JLabel lblLogo = new JLabel("PET SHOP PRO", taiIcon("/img/logo.png", 26, 26), SwingConstants.LEFT);
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(UIStyle.font(Font.BOLD, 16));
        lblLogo.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblLogo.setIconTextGap(8);
        pnlThuongHieu.add(lblLogo);
        pnlThuongHieu.setPreferredSize(new Dimension(160, 38));
        pnlHeader2.add(pnlThuongHieu, BorderLayout.WEST);

        // GridLayout(1,6): sắp xếp sáu nút menu trên cùng một hàng.
        JPanel pnlMenu = new JPanel(new GridLayout(1, 6, 2, 0));
        pnlMenu.setOpaque(false);

        // JButton: tạo nút Trang chủ và đăng ký hành động quay về trang chính.
        btnTrangChu = taoBtnNhom("TRANG CHỦ", "/img/trangchulogo.png");
        // Method Reference this::hienTrangChu: truyền hành động, chưa chạy ngay.
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

        // Thêm vùng menu vào giữa thanh điều hướng.
        pnlHeader2.add(pnlMenu, BorderLayout.CENTER);

        // JButton: nút tên nhân viên; nhấn để mở thông tin và các thao tác tài khoản.
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

    /**
     * Tạo một JButton dùng chung cho các nhóm menu.
     * Thiết lập biểu tượng, phông chữ, màu, vùng viền, con trỏ và tooltip.
     */
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

    /**
     * Đọc ảnh từ thư mục tài nguyên /img trong project.
     * getResource() lấy URL; ImageIcon chứa ảnh; getScaledInstance() co giãn ảnh
     * theo kích thước yêu cầu.
     */
    private ImageIcon taiIcon(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url == null)
            return null;
        // ImageIcon chứa ảnh; getScaledInstance() đổi kích thước và làm mượt ảnh.
        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    /**
     * Hiển thị JPopupMenu ngay bên dưới nút menu được nhấn.
     * JPopupMenu là bảng lựa chọn chức năng bật ra tại vị trí của JButton.
     */
    private void hienPopup(JButton btnNguon, JPopupMenu pmnPopMenu) {
        pmnPopMenu.show(btnNguon, 0, btnNguon.getHeight());
    }

    /**
     * Thêm một JMenuItem vào menu thả xuống.
     * Khóa mục menu không có quyền bằng setEnabled(false); mục được bật mới chạy
     * Runnable.
     */
    private void themMuc(JPopupMenu pmnPopMenu, String ten, JButton btnNhom, Runnable hanhDong) {
        PhanQuyen.Quyen q = PhanQuyenControl.tuTenMenu(ten);
        // JMenuItem: một lựa chọn trong menu thả xuống (JPopupMenu).
        JMenuItem mniChucNang = new JMenuItem(ten);
        mniChucNang.setFont(UIStyle.font(Font.PLAIN, 13));
        mniChucNang.setBorder(new EmptyBorder(9, 14, 9, 14));
        // Giữ mọi mục trong menu; mục không được cấp quyền sẽ có màu xám và không thể
        // nhấn.
        mniChucNang.setEnabled(PhanQuyenControl.coQuyen(q));
        dangKyHanhDong(mniChucNang, () -> chuyenTuNhom(btnNhom, hanhDong));
        pmnPopMenu.add(mniChucNang);
    }

    /**
     * Tạo menu Hệ thống: tài khoản, phân quyền và ca làm.
     * Các mục không có quyền vẫn hiển thị nhưng bị vô hiệu hóa trong themMuc().
     */
    private JPopupMenu taoMenuHeThong() {
        JPopupMenu pmnP = new JPopupMenu();
        themMuc(pmnP, "Quản lý tài khoản", btnHeThong, this::moQuanLyTaiKhoan);
        themMuc(pmnP, "Ma trận phân quyền", btnHeThong, this::moMaTranPhanQuyen);
        themMuc(pmnP, "Quản lý ca làm", btnHeThong, this::moQuanLyCaLam);
        return pmnP;
    }

    /**
     * Tạo menu Danh mục: sản phẩm, khách hàng, thú cưng, dịch vụ, voucher.
     * Hành động cho từng mục được truyền bằng lambda hoặc method reference.
     */
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

    /**
     * Tạo menu Xử lý: nhập kho, kiểm kê, Spa, thanh toán, phân ca.
     * addSeparator() tạo đường ngăn cách các nhóm thao tác.
     */
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

    /**
     * Tạo menu Tìm kiếm: các chức năng tra cứu.
     * Mỗi mục được kiểm tra quyền trước khi hiển thị.
     */
    private JPopupMenu taoMenuTimKiem() {
        JPopupMenu pmnP = new JPopupMenu();
        themMuc(pmnP, "Tra cứu lịch sử hóa đơn", btnTimKiem, this::moTraCuuHoaDon);
        themMuc(pmnP, "Tra cứu nhập kho", btnTimKiem, this::moTraCuuNhapKho);
        themMuc(pmnP, "Tra cứu kiểm kê kho", btnTimKiem, this::moTraCuuKiemKe);
        themMuc(pmnP, "Tra cứu hạn sử dụng", btnTimKiem, this::moCanhBaoHanSuDung);
        return pmnP;
    }

    /**
     * Tạo menu Thống kê.
     * Đưa chức năng thống kê doanh thu vào menu nếu nhân viên có quyền.
     */
    private JPopupMenu taoMenuThongKe() {
        JPopupMenu pmnP = new JPopupMenu();
        themMuc(pmnP, "Thống kê doanh thu", btnThongKe, this::moThongKeDoanhThu);
        return pmnP;
    }

    /**
     * Hiển thị JPopupMenu khi nhấn tên nhân viên.
     * Bên trong có mã nhân viên, tên, vai trò cùng mục đổi mật khẩu và đăng xuất.
     */
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

    /**
     * Tạo JDialog đổi mật khẩu cho chính nhân viên đang đăng nhập.
     * JPasswordField che mật khẩu; kiểm tra đầu vào; SwingWorker gọi TaiKhoanDAO
     * ngoài luồng giao diện.
     */
    private void moDlgDoiMatKhau() {
        // Validation: từ chối mở cửa sổ khi mã nhân viên rỗng hoặc vai trò không hợp
        // lệ.
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
        // BorderLayout: chia cửa sổ thành các vùng Bắc, Nam, Đông, Tây và Giữa.
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

                // SwingWorker: chạy truy vấn đổi mật khẩu ở luồng nền, tránh chặn giao diện.
                new SwingWorker<Boolean, Void>() {
                    @Override
                    // doInBackground(): xử lý công việc nền, không cập nhật trực tiếp thành phần
                    // Swing.
                    protected Boolean doInBackground() throws Exception {
                        return control.doiMatKhau(maNV, strMatKhauCu, strMatKhauMoi);
                    }

                    @Override
                    // done(): nhận kết quả tác vụ trên luồng sự kiện Swing để cập nhật thông báo.
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

    /**
     * Tạo một dòng gồm JLabel và JPasswordField trong biểu mẫu đổi mật khẩu.
     * GridBagConstraints xác định vị trí và độ rộng các thành phần.
     */
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

    /**
     * Thêm một cặp nhãn và giá trị vào bảng thông tin tài khoản.
     * JLabel dùng để hiển thị chữ không cho chỉnh sửa.
     */
    private void themThongTin(JPanel pnlPanel, String lblNhan, String lblGiaTri) {
        JLabel lblThongTin = new JLabel(lblNhan);
        lblThongTin.setFont(UIStyle.font(Font.BOLD, 12));
        pnlPanel.add(lblThongTin);
        pnlPanel.add(new JLabel(lblGiaTri != null ? lblGiaTri : ""));
    }

    /**
     * Kết thúc phiên làm việc của nhân viên.
     * Xóa dữ liệu phiên, đóng TrangChu và mở lại màn hình DangNhap.
     */
    private void dangXuat() {
        UI.PhienLamViecUI.dat(null, null, null);
        PhienLamViec.dat(null, null, null);
        dispose();
        new DangNhapUI().setVisible(true);
    }

    /**
     * Tải lại những nội dung có thể thay đổi trên Trang chủ.
     * Gọi lại đồng hồ, lịch Spa và thống kê; revalidate() tính lại bố cục,
     * repaint() vẽ lại.
     */
    private void lamMoiTrangChu() {
        capNhatGio();
        napLichSpaSapToi();
        // Lấy số liệu từ DAO/JDBC và đưa vào bốn nhãn thống kê.
        napTongQuanHomNay();
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
    }

    /**
     * Tạo bố cục trang chủ gồm tiêu đề, ảnh, truy cập nhanh và tiện ích.
     * BorderLayout chia vùng chính; GridLayout chia hai cột ảnh và truy cập nhanh.
     */
    private JPanel taoPnlTrangChu() {
        // JPanel: khung chính của trang; BorderLayout chia phần tiêu đề và nội dung.
        JPanel pnlChinh = new JPanel(new BorderLayout());
        pnlChinh.setBackground(LIGHT_BLUE);
        pnlChinh.add(taoPnlThanhTieuDe(), BorderLayout.NORTH);

        JPanel pnlThan = new JPanel(new BorderLayout(0, 14));
        pnlThan.setBackground(LIGHT_BLUE);
        pnlThan.setBorder(new EmptyBorder(16, 28, 16, 28));

        // GridLayout(1,2): chia hai cột bằng nhau cho ảnh giới thiệu và truy cập nhanh.
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

    /**
     * Tạo thanh tiêu đề Trang chủ.
     * JLabel hiển thị ngày giờ/tiêu đề; JButton Làm mới và Hỗ trợ gắn Runnable để
     * thực thi.
     */
    private JPanel taoPnlThanhTieuDe() {
        JPanel pnlPanel = new JPanel(new BorderLayout());
        pnlPanel.setBackground(UIStyle.BAR);
        pnlPanel.setBorder(new EmptyBorder(10, 25, 10, 25));

        // JLabel: tạo nhãn ngày giờ ở góc trái thanh tiêu đề.
        JLabel lblNgayHienTai = new JLabel(LocalDateTime.now().format(DINH_DANG_GIO));
        lblNgayTrangChu = lblNgayHienTai;
        lblNgayHienTai.setFont(UIStyle.font(Font.BOLD, 12));

        JLabel lblTieuDeTrangChu = new JLabel("TRANG CHỦ", SwingConstants.CENTER);
        lblTieuDeTrangChu.setFont(UIStyle.font(Font.BOLD, 25));

        JPanel pnlThaoTac = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlThaoTac.setOpaque(false);

        // JButton Làm mới: nút được định dạng chung qua UIStyle.
        btnLamMoi = UIStyle.nutPhu(" Làm mới");
        btnLamMoi.setPreferredSize(new Dimension(135, 38));
        dangKyHanhDong(btnLamMoi, this::lamMoiTrangChu);

        // JButton Hỗ trợ: mở JDialog hướng dẫn sử dụng.
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

    /**
     * Tạo khung ảnh giới thiệu Pet Shop Pro.
     * Ghi đè paintComponent() và dùng Graphics2D để vẽ ảnh theo kích thước hiện tại
     * của khung.
     */
    private JPanel taoPnlGioiThieu() {
        JPanel pnlPanel = new JPanel(new BorderLayout());
        pnlPanel.setBackground(Color.WHITE);
        pnlPanel.setBorder(new LineBorder(UIStyle.LINE));

        // getResource(): tìm ảnh trong tài nguyên classpath của project.
        java.net.URL url = getClass().getResource("/img/giothieutrangchu.png");
        if (url == null) {
            pnlPanel.add(new JLabel("Không tìm thấy ảnh giới thiệu", SwingConstants.CENTER));
            return pnlPanel;
        }
        Image anh = new ImageIcon(url).getImage();
        JPanel pnlAnhCoGian = new JPanel() {
            @Override
            // Ghi đè paintComponent(): vẽ lại ảnh khi kích thước khung thay đổi.
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int w = getWidth(), h = getHeight();
                int iw = anh.getWidth(this), ih = anh.getHeight(this);
                if (iw <= 0 || ih <= 0 || w <= 0 || h <= 0)
                    return;
                // Chọn tỷ lệ phủ kín khung; ảnh có thể bị cắt bớt để không tạo khoảng trống.
                double tile = Math.max((double) w / iw, (double) h / ih);
                int newW = (int) Math.ceil(iw * tile);
                int newH = (int) Math.ceil(ih * tile);
                int x = (w - newW) / 2;
                int y = 0;
                // Graphics2D: cung cấp thao tác vẽ ảnh với thuật toán nội suy làm mượt.
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

    /**
     * Tạo khu vực truy cập nhanh F4–F8.
     * BoxLayout.Y_AXIS xếp các lựa chọn theo chiều dọc; mỗi lựa chọn kiểm tra
     * quyền.
     */
    private JPanel taoPnlTruyCapNhanh() {
        JPanel pnlPanel = new JPanel();
        pnlPanel.setBackground(Color.WHITE);
        pnlPanel.setBorder(new CompoundBorder(new LineBorder(UIStyle.LINE), new EmptyBorder(15, 15, 15, 15)));
        // BoxLayout.Y_AXIS: xếp các dòng truy cập nhanh theo chiều dọc.
        pnlPanel.setLayout(new BoxLayout(pnlPanel, BoxLayout.Y_AXIS));

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

    /**
     * Tạo hai khung ở phía dưới Trang chủ.
     * GridLayout(1,2) chia Lịch Spa sắp tới và Tổng quan hôm nay thành hai cột.
     */
    private JPanel taoPnlTienIchDuoi() {
        JPanel pnlPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlPanel.setOpaque(false);
        pnlPanel.add(taoPnlLichSpaSapToi());
        pnlPanel.add(taoPnlTongQuan());
        return pnlPanel;
    }

    /**
     * Tạo khung "Tổng quan hôm nay".
     * GridLayout(2,2) chứa bốn thẻ Doanh thu, Lượt mua, Lịch Spa và Phiếu nhập.
     */
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

        // GridLayout(2,2): bốn ô thống kê xếp thành hai hàng, hai cột.
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
        // Lấy số liệu từ DAO/JDBC và đưa vào bốn nhãn thống kê.
        napTongQuanHomNay();
        return pnlTongQuan;
    }

    /**
     * Tạo một thẻ thống kê có tiêu đề và số liệu.
     * JPanel chứa JLabel tiêu đề ở trên và JLabel giá trị ở giữa.
     */
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

    /**
     * Khởi tạo JLabel để hiển thị giá trị của một chỉ số.
     * Thiết lập phông chữ đậm, màu và nội dung ban đầu.
     */
    private JLabel taoGiaTriTongQuan(String text) {
        JLabel lblTam = new JLabel(text);
        lblTam.setFont(UIStyle.font(Font.BOLD, 23));
        lblTam.setForeground(new Color(38, 45, 57));
        return lblTam;
    }

    /**
     * Mở màn hình tra cứu hạn sử dụng sản phẩm.
     * Đưa content pane của màn hình vào vùng CardLayout và nạp dữ liệu.
     */
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

    /**
     * Mở màn hình gợi ý thức ăn theo loại thú cưng.
     * Truyền callback mở thanh toán khi người dùng muốn mua sản phẩm được gợi ý.
     */
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

    /**
     * Tải bốn chỉ số thống kê trong ngày.
     * Kiểm tra quyền từng nhóm; gọi HoaDonDAO và các truy vấn đếm lịch Spa/phiếu
     * nhập; setText() cập nhật JLabel.
     */
    private void napTongQuanHomNay() {
        if (lblDoanhThuHomNay == null)
            return;
        // Kiểm tra quyền riêng cho nhóm số liệu doanh thu.
        boolean xemThongKe = PhanQuyenControl.coQuyen(PhanQuyen.Quyen.THONG_KE_DOANH_THU);
        boolean xemSpa = PhanQuyenControl.coQuyen(PhanQuyen.Quyen.LICH_HEN_SPA);
        boolean xemKho = PhanQuyenControl.coQuyen(PhanQuyen.Quyen.NHAP_KHO);
        lblDoanhThuHomNay.setText("Không có quyền");
        lblLuotMuaHomNay.setText("—");
        lblLichSpaHomNay.setText("—");
        lblPhieuNhapHomNay.setText("—");
        if (xemThongKe) {
            try {
                // DAO Pattern: tách phần lấy dữ liệu hóa đơn khỏi bố cục Swing.
                TrangChuControl.ThongKeHoaDon tk = control.thongKeHoaDon();
                lblDoanhThuHomNay.setText(formatVnd(tk.getDoanhThuHomNay()));
                lblLuotMuaHomNay.setText(String.valueOf(tk.getHoaDonHomNay()));
            } catch (Exception ex) {
                lblDoanhThuHomNay.setText("—");
            }
        }
        if (xemSpa) {
            try {
                lblLichSpaHomNay.setText(String.valueOf(demLichSpaHomNay()));
            } catch (SQLException ex) {
                lblLichSpaHomNay.setText("—");
            }
        }
        if (xemKho) {
            try {
                lblPhieuNhapHomNay.setText(String.valueOf(demPhieuNhapHomNay()));
            } catch (SQLException ex) {
                lblPhieuNhapHomNay.setText("—");
            }
        }
    }

    /**
     * Đếm lịch hẹn Spa trong ngày chưa bị hủy bằng SQL COUNT(*).
     * PreparedStatement gán mốc 00:00 hôm nay và 00:00 ngày mai; ResultSet lấy kết
     * quả.
     */
    private int demLichSpaHomNay() throws SQLException {
        return control.demLichSpaHomNay();
    }

    /**
     * Đếm phiếu nhập có ngày nhập là ngày hiện tại.
     * JDBC kết hợp SQL COUNT(*) và GETDATE() của SQL Server.
     */
    private int demPhieuNhapHomNay() throws SQLException {
        return control.demPhieuNhapHomNay();
    }

    /**
     * Định dạng BigDecimal thành chuỗi tiền Việt Nam.
     * Chuyển sang định dạng chung của UIStyle; trường hợp null được xem là 0.
     */
    private String formatVnd(BigDecimal soTien) {
        return UIStyle.tien(soTien == null ? 0 : soTien.doubleValue());
    }

    /**
     * Tạo thẻ Lịch Spa sắp tới.
     * JPanel chứa tiêu đề/phụ đề và danh sách dòng lịch; BoxLayout xếp dòng theo
     * chiều dọc.
     */
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

    /**
     * Nạp và hiển thị lịch Spa sắp diễn ra trong hôm nay.
     * LichHenDAO.getAll() -> Stream lọc lịch hôm nay/chưa hủy -> lọc lịch chưa đến
     * giờ -> giới hạn 3 dòng.
     */
    private void napLichSpaSapToi() {
        if (pnlLichSpaNoiDung == null || lblPhuDeLichSpa == null)
            return;
        if (!PhanQuyenControl.coQuyen(PhanQuyen.Quyen.LICH_HEN_SPA)) {
            pnlLichSpaNoiDung.removeAll();
            lblPhuDeLichSpa.setText("Không có quyền xem lịch Spa");
            // revalidate(): yêu cầu Swing cập nhật lại cách sắp xếp danh sách lịch.
            pnlLichSpaNoiDung.revalidate();
            // repaint(): yêu cầu Swing vẽ lại danh sách sau khi thay đổi dữ liệu.
            pnlLichSpaNoiDung.repaint();
            return;
        }

        pnlLichSpaNoiDung.removeAll();
        LocalDateTime now = LocalDateTime.now();
        try {
            // LichHenDAO: lấy danh sách lịch Spa từ SQL Server.
            // Việc truy vấn, lọc trạng thái và sắp xếp được xử lý ở TrangChuControl.
            // Stream.filter(): chỉ lấy lịch trong hôm nay và chưa bị hủy.
            List<LichHen> lichHomNay = control.lichSpaHomNay(now);
            List<LichHen> lichConLai = control.lichSpaConLai(lichHomNay, now);
            long tongLichHomNay = lichHomNay.size();

            if (!lichConLai.isEmpty()) {
                lblPhuDeLichSpa.setText("Các lịch còn lại trong hôm nay");
                // Stream.limit(3): trang chủ hiển thị tối đa 3 lịch sắp tới.
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

        // revalidate(): yêu cầu Swing cập nhật lại cách sắp xếp danh sách lịch.
        pnlLichSpaNoiDung.revalidate();
        // repaint(): yêu cầu Swing vẽ lại danh sách sau khi thay đổi dữ liệu.
        pnlLichSpaNoiDung.repaint();
    }

    /**
     * Tạo một dòng JLabel thể hiện giờ hẹn, thú cưng và ghi chú.
     * Giới hạn độ dài hiển thị và giữ thông tin đầy đủ trong tooltip.
     */
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

    /**
     * Chuẩn hóa chuỗi tùy chọn.
     * Trả về giá trị mặc định nếu chuỗi null hoặc chỉ gồm khoảng trắng.
     */
    private String khongRong(String s, String macDinh) {
        return (s == null || s.trim().isEmpty()) ? macDinh : s.trim();
    }

    /**
     * Tạo nhãn tổng kết số lịch Spa dạng thẻ nhỏ.
     * JLabel với nền xanh nhạt đặt trong JPanel.
     */
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

    /**
     * Mở hộp thoại hỗ trợ sử dụng phần mềm.
     * JDialog hiển thị nội dung HTML trong JEditorPane, bọc JScrollPane để cuộn.
     */
    private void moModalHoTro() {
        // JDialog: tạo cửa sổ Hỗ trợ nằm trên Trang chủ.
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

        // JEditorPane: hiển thị hướng dẫn có định dạng HTML.
        JEditorPane edtEdpHuongDan = new JEditorPane();
        edtEdpHuongDan.setContentType("text/html");
        edtEdpHuongDan.setEditable(false);
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
        // JScrollPane: thêm thanh cuộn nếu nội dung hướng dẫn dài.
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

    /**
     * Tạo một dòng truy cập nhanh và đăng ký phím tắt.
     * InputMap nhận F4–F8; ActionMap chỉ chạy khi nút nhanh được bật theo vai trò.
     */
    private void themBtnTruyCapNhanh(JPanel pnlParent, String text, String shortcut,
            PhanQuyen.Quyen quyen, Runnable hanhDong) {
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

        // Tạo mã nhận diện riêng cho mỗi phím F4–F8.
        String khoa = "truycapnhanh-" + shortcut;
        // InputMap: bắt phím tắt khi cửa sổ đang được focus.
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(shortcut), khoa);
        // ActionMap: ánh xạ mã phím sang hành động mở màn hình được phép.
        getRootPane().getActionMap().put(khoa, new AbstractAction() {
            /**
             * Xử lý tập trung sự kiện nhấn nút hoặc chọn menu.
             * Lấy Runnable đã lưu bằng getClientProperty() và gọi run() trên luồng xử lý sự
             * kiện.
             */
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Phím tắt chỉ chạy khi nút truy cập nhanh tương ứng đang bật.
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

    /** Lớp lồng lưu mã, tên và vai trò nhân viên của phiên hiện tại. */
    public static final class PhienLamViec {
        public static String maNV;
        public static String hoTen;
        public static String vaiTro;

        private PhienLamViec() {
        }

        /**
         * Lưu thông tin của phiên làm việc.
         * Các biến static cho phép truy cập dữ liệu phiên thông qua lớp lồng
         * PhienLamViec.
         */
        public static void dat(String ma, String ten, String role) {
            maNV = ma;
            hoTen = ten;
            vaiTro = role;
        }
    }

    /** Các hàm tạo thành phần Swing và quy tắc màu/định dạng dùng chung. */
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

        /**
         * Tạo Font Segoe UI với kiểu và kích thước mong muốn.
         * Dùng chung để đồng nhất chữ trên nhiều màn hình.
         */
        public static Font font(int style, int size) {
            return new Font("Segoe UI", style, size);
        }

        // DecimalFormat: định dạng số tiền với dấu phân nhóm hàng nghìn.
        private static final DecimalFormat DINH_DANG_TIEN;

        static {
            DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
            s.setGroupingSeparator('.');
            DINH_DANG_TIEN = new DecimalFormat("#,##0", s);
        }

        /**
         * Chuyển số tiền thành chuỗi có dấu phân cách hàng nghìn và ký hiệu ₫.
         * Sử dụng DecimalFormat.
         */
        public static String tien(double v) {
            return DINH_DANG_TIEN.format(v) + " ₫";
        }

        /**
         * Đọc chuỗi tiền người dùng nhập thành số nguyên long.
         * Loại bỏ dấu cách/dấu phân cách rồi kiểm tra bằng biểu thức chính quy.
         */
        public static long docTien(String s) {
            String sach = s == null ? "" : s.replaceAll("[\\s.,₫dđ]", "");
            if (!sach.matches("\\d{1,15}")) {
                throw new NumberFormatException(s);
            }
            return Long.parseLong(sach);
        }

        /**
         * Tạo nút JButton theo bộ màu chung.
         * Thiết lập nền, màu chữ, Border, Font và hình dạng con trỏ.
         */
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

        /**
         * Tạo nút thao tác chính màu xanh đậm.
         * Gọi lại hàm nut() để tránh viết lặp giao diện.
         */
        public static JButton nutChinh(String text) {
            return nut(text, NAVY, Color.WHITE, null);
        }

        /**
         * Tạo nút thao tác tích cực màu xanh lá.
         * Gọi lại hàm nut() với bộ màu tương ứng.
         */
        public static JButton nutXanh(String text) {
            return nut(text, GREEN, Color.WHITE, null);
        }

        /**
         * Tạo nút phụ nền trắng, viền xanh.
         * Gọi lại hàm nut() với màu định sẵn.
         */
        public static JButton nutPhu(String text) {
            return nut(text, Color.WHITE, NAVY, NAVY);
        }

        /**
         * Tạo nút xóa/hủy có chữ đỏ.
         * Gọi lại hàm nut() để thống nhất định dạng.
         */
        public static JButton nutXoa(String text) {
            return nut(text, Color.WHITE, RED, LINE);
        }

        /**
         * Tạo nút tab; đổi màu theo trạng thái đang chọn.
         * Toán tử điều kiện ?: chọn bộ màu.
         */
        public static JButton nutTab(String text, boolean dangChon) {
            return nut(text, dangChon ? ACTIVE_BLUE : Color.WHITE,
                    dangChon ? Color.WHITE : NAVY, NAVY);
        }

        /**
         * Tạo JLabel tiêu chuẩn dùng chung.
         * JLabel là nhãn hiển thị chữ hoặc biểu tượng.
         */
        public static JLabel nhan(String text) {
            JLabel lblNhan3 = new JLabel(text);
            lblNhan3.setFont(font(Font.BOLD, 13));
            return lblNhan3;
        }

        /**
         * Tạo JTextField nhập dữ liệu.
         * JTextField là ô nhập một dòng; thiết lập kiểu chữ/border thống nhất.
         */
        public static JTextField o(int cot) {
            JTextField txtVanBan = new JTextField(cot);
            txtVanBan.setFont(font(Font.PLAIN, 13));
            return txtVanBan;
        }

        /**
         * Tạo JTextField chỉ đọc.
         * setEditable(false) ngăn nhập tay nhưng vẫn có thể xem nội dung.
         */
        public static JTextField oChiDoc(String lblGiaTri) {
            JTextField txtVanBan = o(10);
            txtVanBan.setText(lblGiaTri);
            txtVanBan.setEditable(false);
            txtVanBan.setBackground(READONLY);
            return txtVanBan;
        }

        /**
         * Bố trí một nhãn và thành phần nhập liệu thành một dòng form.
         * GridBagLayout/GridBagConstraints hỗ trợ sắp xếp trường nhập.
         */
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

        /**
         * Tạo thẻ JPanel nền trắng dùng cho giao diện.
         * Thiết lập Border và khoảng đệm.
         */
        public static JPanel the() {
            JPanel pnlKetQua = new JPanel();
            pnlKetQua.setBackground(Color.WHITE);
            pnlKetQua.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(15, 15, 15, 15)));
            return pnlKetQua;
        }

        /**
         * Tạo thẻ hiển thị một chỉ số thống kê.
         * Đặt JLabel tiêu đề và JLabel giá trị vào JPanel.
         */
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

        /**
         * Thiết lập kiểu hiển thị cho JTable.
         * Chỉnh JTableHeader và cell renderer; căn phải một số cột nếu được chỉ định.
         */
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

        /**
         * Bọc JTable trong JScrollPane để tạo thanh cuộn.
         * Đặt Border và màu nền của viewport.
         */
        public static JScrollPane cuon(JTable tblDuLieu2) {
            JScrollPane scrHuongDan = new JScrollPane(tblDuLieu2);
            scrHuongDan.setBorder(new LineBorder(LINE));
            scrHuongDan.getViewport().setBackground(Color.WHITE);
            return scrHuongDan;
        }

        /**
         * Hiển thị thông báo thông thường.
         * JOptionPane.INFORMATION_MESSAGE là hộp thoại thông tin.
         */
        public static void thongBao(Component cha, String strNoiDung) {
            JOptionPane.showMessageDialog(cha, strNoiDung, "Pet Shop Pro", JOptionPane.INFORMATION_MESSAGE);
        }

        /**
         * Xuất dữ liệu của JTable ra tệp PDF.
         * JFileChooser cho chọn vị trí lưu, Files.write() ghi kết quả PDF.
         */
        public static void xuatPdf(Component cha, String tieuDe, JTable tblDuLieu2) {
            // JFileChooser: hộp thoại cho người dùng chọn đường dẫn lưu tệp.
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
                // Ghi nội dung PDF thành tệp; taoPdfBang() tạo dữ liệu nhị phân.
                Files.write(file.toPath(), taoPdfBang(tieuDe, tblDuLieu2));
                JOptionPane.showMessageDialog(cha, "Đã xuất PDF:\n" + file.getAbsolutePath(),
                        "Xuất PDF", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(cha, "Không thể xuất PDF.\n" + ex.getMessage(),
                        "Lỗi xuất PDF", JOptionPane.ERROR_MESSAGE);
            }
        }

        /**
         * Tạo PDF một trang từ ảnh chụp JTable và tiêu đề.
         * Graphics2D vẽ bảng lên BufferedImage; DeflaterOutputStream nén ảnh; tự tạo
         * cấu trúc PDF 1.4.
         */
        private static byte[] taoPdfBang(String tieuDe, JTable tblDuLieu2) throws IOException {
            int tableWidth = Math.max(1, tblDuLieu2.getPreferredSize().width);
            int tableHeight = Math.max(1, tblDuLieu2.getTableHeader().getPreferredSize().height
                    + tblDuLieu2.getRowCount() * tblDuLieu2.getRowHeight());
            int imageWidth = 1400;
            int imageHeight = Math.max(120, (int) Math.ceil(
                    (double) tableHeight * imageWidth / tableWidth) + 90);
            // BufferedImage: vùng ảnh trong bộ nhớ để render JTable trước khi xuất PDF.
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
            // DeflaterOutputStream: nén dữ liệu ảnh để đưa vào cấu trúc PDF.
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

        /**
         * Đóng gói byte dữ liệu vào một đối tượng stream của PDF.
         * Khai báo độ dài và ranh giới stream.
         */
        private static byte[] pdfStream(int number, String dictionary, byte[] data) throws IOException {
            ByteArrayOutputStream object = new ByteArrayOutputStream();
            object.write(pdfBytes(number + " 0 obj\n<< " + dictionary + " /Length " + data.length
                    + " >>\nstream\n"));
            object.write(data);
            object.write(pdfBytes("\nendstream\nendobj\n"));
            return object.toByteArray();
        }

        /**
         * Mã hóa chuỗi cấu trúc PDF thành mảng byte.
         * Dùng charset ISO-8859-1 cho các token cấu trúc PDF.
         */
        private static byte[] pdfBytes(String value) {
            return value.getBytes(StandardCharsets.ISO_8859_1);
        }

        /**
         * Hiển thị hộp thoại cảnh báo.
         * JOptionPane.WARNING_MESSAGE là kiểu cảnh báo.
         */
        public static void canhBao(Component cha, String strNoiDung) {
            JOptionPane.showMessageDialog(cha, strNoiDung, "Pet Shop Pro", JOptionPane.WARNING_MESSAGE);
        }

        /**
         * Hiển thị hộp thoại yêu cầu Yes/No.
         * Trả về true nếu người dùng chọn Yes.
         */
        public static boolean xacNhan(Component cha, String strNoiDung) {
            return JOptionPane.showConfirmDialog(cha, strNoiDung, "Pet Shop Pro",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
        }

        /**
         * Hiển thị lỗi; diễn giải một số mã lỗi SQL Server.
         * Phân biệt trùng khóa và ràng buộc khóa ngoại để tạo thông báo dễ hiểu.
         */
        public static void loi(Component cha, Exception ex) {
            String nd;
            // Pattern matching instanceof: kiểm tra và lấy lỗi SQL trong cùng một biểu
            // thức.
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

        /**
         * Tạo JPanel chứa các nút Hủy/Lưu.
         * FlowLayout.RIGHT căn các nút về bên phải.
         */
        public static JPanel hangNut(JButton btnHuy2, JButton btnLuu2) {
            JPanel pnlKetQua = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnlKetQua.setOpaque(false);
            pnlKetQua.add(btnHuy2);
            pnlKetQua.add(btnLuu2);
            return pnlKetQua;
        }
    }

    /**
     * Gắn một Runnable vào JButton/JMenuItem.
     * putClientProperty() lưu hành động; addActionListener(this) dùng chung bộ nhận
     * sự kiện.
     */
    private void dangKyHanhDong(AbstractButton nut, Runnable hanhDong) {
        // ClientProperty lưu Runnable ngay trên nút; không cần viết nhiều if-else để xử
        // lý.
        nut.putClientProperty("petshop.hanhDong", hanhDong);
        // ActionListener: đăng ký TrangChu tiếp nhận sự kiện nhấn nút/menu.
        nut.addActionListener(this);
    }

    /**
     * Xử lý tập trung sự kiện nhấn nút hoặc chọn menu.
     * Lấy Runnable đã lưu bằng getClientProperty() và gọi run() trên luồng xử lý sự
     * kiện.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComponent control) {
            // Lấy Runnable đã lưu trên thành phần phát sự kiện.
            Object handler = control.getClientProperty("petshop.hanhDong");
            if (handler instanceof Runnable hanhDong) {
                // Runnable.run(): gọi hành động ngay trên luồng hiện tại, không tạo Thread mới.
                hanhDong.run();
            }
        }
    }

    /**
     * Phương thức của giao diện MouseListener.
     * Hiện không xử lý thao tác nhấp chuột riêng.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * Phương thức MouseListener khi nhấn nút chuột.
     * Để trống vì không có logic sử dụng ở Trang chủ.
     */
    @Override
    public void mousePressed(MouseEvent e) {
    }

    /**
     * Phương thức MouseListener khi thả nút chuột.
     * Để trống vì không có logic sử dụng ở Trang chủ.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Phương thức MouseListener khi con trỏ đi vào thành phần.
     * Để trống vì không có logic sử dụng ở Trang chủ.
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Phương thức MouseListener khi con trỏ rời thành phần.
     * Để trống vì không có logic sử dụng ở Trang chủ.
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

}
