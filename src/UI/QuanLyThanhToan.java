package UI;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import dao.ThanhToanDao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

// Giao diện/chức năng quản lý thanh toán của ứng dụng.
public class QuanLyThanhToan extends JFrame implements ActionListener, MouseListener {

    // NAVY: Màu dùng cho giao diện: xanh đậm.
    private static final Color NAVY = new Color(18, 48, 82);
    // LIGHT_BLUE: Màu dùng cho giao diện: xanh dương.
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);
    // GREEN: Màu dùng cho giao diện: xanh lá.
    private static final Color GREEN = new Color(25, 157, 107);
    // BORDER: Màu dùng cho giao diện: viền.
    private static final Color BORDER = new Color(210, 220, 230);
    // MONEY: Bộ định dạng số/tiền dùng để hiển thị dữ liệu trên giao diện.
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(
            Locale.forLanguageTag("vi-VN"));

    // dao: Đối tượng truy cập dữ liệu thanh toán.
    private final ThanhToanDao dao = new ThanhToanDao();
    // maNhanVien: Lưu mã nhân viên.
    private final String maNhanVien;
    // pnlNoiDung: Phần giao diện được TrangChu nhúng vào CardLayout.
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout(10, 12));
    // moLichSuHoaDon: Hành động được gọi khi cần mở lịch sử hóa đơn.
    private final Runnable moLichSuHoaDon;
    // capNhatPhieuDichVu: Hành động được gọi khi cần cập nhật danh sách phiếu dịch vụ.
    private final Runnable capNhatPhieuDichVu;
    // chiTiet: Lưu chi tiết; phần tử có kiểu entity.ThanhToan.
    private final List<entity.ThanhToan> chiTiet = UI.PhienLamViec.gioHang();
    // danhSachKhachHang: Lưu danh sách khách hàng dùng để gợi ý.
    private final List<entity.ThanhToan.KhachHang> danhSachKhachHang = new ArrayList<>();
    // danhSachMaSanPham: Lưu danh sách mã sản phẩm; phần tử có kiểu String.
    private final List<String> danhSachMaSanPham = new ArrayList<>();
    // tenSanPhamTheoMa: Bảng ánh xạ khóa đến giá trị dùng cho ánh xạ mã sản phẩm đến tên sản phẩm.
    private final Map<String, String> tenSanPhamTheoMa = new LinkedHashMap<>();
    // danhSachVoucher: Danh sách voucher đang hoạt động.
    private final List<entity.ThanhToan.Voucher> danhSachVoucher = new ArrayList<>();
    // maPhieuDichVuTrongDonHang: Lưu các mã phiếu dịch vụ trong đơn hàng; phần tử có kiểu String.
    private final List<String> maPhieuDichVuTrongDonHang = new ArrayList<>();
    // dichVuTuPhieuDaThem: Lưu các dòng dịch vụ được thêm từ phiếu; phần tử có kiểu entity.ThanhToan.
    private final List<entity.ThanhToan> dichVuTuPhieuDaThem = new ArrayList<>();
    // tableModel: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private final DonHangModel tableModel = new DonHangModel();

    // cmbMaKhachHang: Hộp lựa chọn mã khách hàng.
    private final JComboBox<String> cmbMaKhachHang = new JComboBox<>();
    // txtTenKhachHang: Ô nhập/hiển thị văn bản tên khách hàng.
    private final JTextField txtTenKhachHang = new JTextField();
    // txtSoDienThoai: Ô nhập/hiển thị văn bản số điện thoại.
    private final JTextField txtSoDienThoai = new JTextField(12);
    // txtMaPhieuDichVu: Ô nhập/hiển thị văn bản mã phiếu dịch vụ.
    private final JTextField txtMaPhieuDichVu = new JTextField(18);
    // txtKhachHangTomTat: Ô nhập/hiển thị văn bản thông tin tóm tắt khách hàng.
    private final JTextField txtKhachHangTomTat = new JTextField();
    // cmbMaSanPham: Hộp lựa chọn mã sản phẩm.
    private final JComboBox<String> cmbMaSanPham = new JComboBox<>();
    // cmbVoucher: Hộp gợi ý mã voucher.
    private final JComboBox<String> cmbVoucher = new JComboBox<>();
    // txtTienKhachDua: Ô nhập/hiển thị văn bản số tiền khách đưa.
    private final JTextField txtTienKhachDua = new JTextField();
    // txtNgayLap: Ô nhập/hiển thị văn bản ngày giờ lập hóa đơn.
    private final JTextField txtNgayLap = new JTextField();
    // lblTongHang: Nhãn hiển thị tổng tiền hàng.
    private final JLabel lblTongHang = new JLabel();
    // lblGiamGia: Nhãn hiển thị tiền giảm giá.
    private final JLabel lblGiamGia = new JLabel();
    // lblTongThanhToan: Nhãn hiển thị tổng tiền thanh toán.
    private final JLabel lblTongThanhToan = new JLabel();
    // lblTienThua: Nhãn hiển thị tiền thừa.
    private final JLabel lblTienThua = new JLabel();
    // tblDonHang: Bảng hiển thị dữ liệu đơn hàng.
    private final JTable tblDonHang = new JTable(tableModel);
    // pnlPaymentMethodsPanel: Khung chứa thành phần giao diện khung phương thức thanh toán.
    private final JPanel pnlPaymentMethodsPanel = new JPanel(new GridLayout(1, 0, 8, 0));
    // phuongThucTheoNut: Phương thức thanh toán tương ứng với từng nút chọn.
    private final Map<JToggleButton, entity.ThanhToan.PhuongThuc> phuongThucTheoNut =
            new LinkedHashMap<>();
    // btnApDungVoucher: Nút thao tác: Áp dụng.
    private final JButton btnApDungVoucher = new JButton("Áp dụng");

    // maKhachHang: Lưu mã khách hàng.
    private String maKhachHang;
    // maKhachHangDaXacNhan: Lưu mã khách hàng đã xác nhận.
    private String maKhachHangDaXacNhan;
    // dangCapNhatGoiYMaKhachHang: Cờ cho biết đang cập nhật gợi ý mã khách hàng.
    private boolean dangCapNhatGoiYMaKhachHang;
    // dangCapNhatGoiYMaSanPham: Cờ cho biết đang cập nhật gợi ý mã sản phẩm.
    private boolean dangCapNhatGoiYMaSanPham;
    // dangCapNhatGoiYVoucher: Cờ cho biết đang cập nhật gợi ý voucher.
    private boolean dangCapNhatGoiYVoucher;
    // phuongThucHienTai: Đối tượng phương thức thanh toán hiện tại có kiểu entity.ThanhToan.PhuongThuc.
    private entity.ThanhToan.PhuongThuc phuongThucHienTai;
    // voucher: Đối tượng voucher có kiểu entity.ThanhToan.Voucher.
    private entity.ThanhToan.Voucher voucher;
    // ngayLap: Lưu ngày giờ lập hóa đơn dưới dạng dữ liệu thời gian.
    private LocalDateTime ngayLap = LocalDateTime.now();

    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số maNhanVien: mã nhân viên.
    // Tham số moLichSuHoaDon: mở lịch sử hóa đơn.
    public QuanLyThanhToan(String maNhanVien, Runnable moLichSuHoaDon) {
        this(maNhanVien, moLichSuHoaDon, () -> { });
    }

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    // Tham số maNhanVien: mã nhân viên.
    // Tham số moLichSuHoaDon: mở lịch sử hóa đơn.
    // Tham số capNhatPhieuDichVu: cập nhật danh sách phiếu dịch vụ.
    public QuanLyThanhToan(
            String maNhanVien, Runnable moLichSuHoaDon, Runnable capNhatPhieuDichVu) {
        this.maNhanVien = maNhanVien;
        this.moLichSuHoaDon = moLichSuHoaDon;
        this.capNhatPhieuDichVu = capNhatPhieuDichVu;
        setTitle("Thanh toán hóa đơn");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pnlNoiDung.setBackground(LIGHT_BLUE);
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));
        pnlNoiDung.add(createTitle(), BorderLayout.NORTH);
        pnlNoiDung.add(createBody(), BorderLayout.CENTER);
        setContentPane(pnlNoiDung);
        tblDonHang.addMouseListener(this);
        napDanhSachMaKhachHang();
        napDanhSachMaSanPham();
        napDanhSachVoucher();
        napPhuongThucThanhToan();
        capNhatTien();
    }

    /** Trả về panel nội dung để TrangChu tiếp tục hiển thị bằng CardLayout. */
    public JPanel getContentPanel() {
        return pnlNoiDung;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        switch (event.getActionCommand()) {
            case "OPEN_INVOICE_HISTORY":
                moLichSuHoaDon.run();
                break;
            case "CANCEL_INVOICE":
                huyHoaDon();
                break;
            case "CHOOSE_PRODUCT":
                moChonSanPham();
                break;
            case "CONFIRM_CUSTOMER":
                xacNhanKhachHang();
                break;
            case "APPLY_VOUCHER":
                apDungVoucher();
                break;
            case "REMOVE_PRODUCT":
                xoaSanPham();
                break;
            case "COMPLETE_PAYMENT":
                thanhToan();
                break;
            case "SELECT_PAYMENT_METHOD":
                capNhatPhuongThucThanhToan(event.getSource());
                break;
            default:
                break;
        }
    }

    private void capNhatPhuongThucThanhToan(Object source) {
        if (!(source instanceof JToggleButton)) {
            return;
        }
        JToggleButton selectedButton = (JToggleButton) source;
        entity.ThanhToan.PhuongThuc selectedMethod = phuongThucTheoNut.get(selectedButton);
        if (selectedMethod == null) {
            return;
        }
        phuongThucHienTai = selectedMethod;
        for (Component component : pnlPaymentMethodsPanel.getComponents()) {
            if (component instanceof JToggleButton) {
                JToggleButton methodButton = (JToggleButton) component;
                methodButton.setBackground(methodButton.isSelected()
                        ? new Color(215, 231, 244) : Color.WHITE);
            }
        }
        capNhatTien();
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        int row = tblDonHang.rowAtPoint(event.getPoint());
        if (row >= 0) {
            tblDonHang.setRowSelectionInterval(row, row);
        }
    }

    @Override
    public void mousePressed(MouseEvent event) {
    }

    @Override
    public void mouseReleased(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }

    /** Đồng bộ bảng và tổng tiền khi quay lại thanh toán sau khi sửa giỏ hàng. */
    public void napGioHang() {
        tableModel.fireTableDataChanged();
        capNhatTien();
    }

    // Tạo tiêu đề.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createTitle() {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new BorderLayout());
        pnlKhung.setOpaque(false);
        // lblTitle2: Nhãn hiển thị: Thanh toán hoá đơn.
        JLabel lblTitle2 = new JLabel("Thanh toán hoá đơn");
        lblTitle2.setForeground(NAVY);
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 25));
        pnlKhung.add(lblTitle2, BorderLayout.WEST);
        // pnlActions: Khung chứa thành phần giao diện thao tác.
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);
        // btnLichSu: Nút thao tác: Lịch sử hóa đơn.
        JButton btnLichSu = new JButton("Lịch sử hóa đơn");
        btnLichSu.setForeground(NAVY);
        btnLichSu.setFocusPainted(false);
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLichSu.setActionCommand("OPEN_INVOICE_HISTORY");
        btnLichSu.addActionListener(this);
        pnlActions.add(btnLichSu);

        // btnHuy: Nút thao tác: Hủy hóa đơn  ×.
        JButton btnHuy = new JButton("Hủy hóa đơn  ×");
        btnHuy.setForeground(NAVY);
        btnHuy.setFocusPainted(false);
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnHuy.setActionCommand("CANCEL_INVOICE");
        btnHuy.addActionListener(this);
        pnlActions.add(btnHuy);
        pnlKhung.add(pnlActions, BorderLayout.EAST);
        return pnlKhung;
    }

    // Tạo nội dung.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createBody() {
        // pnlBody: Khung chứa thành phần giao diện nội dung.
        JPanel pnlBody = new JPanel(new GridBagLayout());
        pnlBody.setOpaque(false);
        // gbc: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng GridBagLayout.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 12);
        gbc.gridx = 0;
        gbc.weightx = 0.65;
        pnlBody.add(createOrderPanel(), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 0);
        pnlBody.add(createPaymentPanel(), gbc);
        return pnlBody;
    }

    // Tạo khung đơn hàng.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createOrderPanel() {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new BorderLayout(0, 8));
        pnlKhung.setOpaque(false);
        pnlKhung.add(createCustomerPanel(), BorderLayout.NORTH);

        // pnlOrder: Khung chứa thành phần giao diện đơn hàng.
        JPanel pnlOrder = new JPanel(new BorderLayout(6, 8));
        pnlOrder.setBackground(Color.WHITE);
        pnlOrder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("ĐƠN HÀNG"),
                BorderFactory.createEmptyBorder(5, 8, 8, 8)));
        // pnlSearch: Khung chứa thành phần giao diện tìm kiếm.
        JPanel pnlSearch = new JPanel(new BorderLayout(12, 0));
        pnlSearch.setOpaque(false);
        cmbMaSanPham.setEditable(true);
        cmbMaSanPham.setToolTipText("Nhập mã sản phẩm");
        Component editor = cmbMaSanPham.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            // txtMaSanPham: Ô nhập/hiển thị văn bản mã sản phẩm.
            JTextField txtMaSanPham = (JTextField) editor;
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            txtMaSanPham.setActionCommand("CHOOSE_PRODUCT");
            txtMaSanPham.addActionListener(this);
            txtMaSanPham.getDocument().addDocumentListener(new DocumentListener() {
                // Cập nhật trạng thái liên quan khi người dùng thêm ký tự vào ô văn bản.
                // Tham số e: thông tin sự kiện.
                @Override
                public void insertUpdate(DocumentEvent e) {
                    capNhatGoiYMaSanPham();
                }

                // Cập nhật trạng thái liên quan khi người dùng xóa ký tự trong ô văn bản.
                // Tham số e: thông tin sự kiện.
                @Override
                public void removeUpdate(DocumentEvent e) {
                    capNhatGoiYMaSanPham();
                }

                // Nhận thông báo khi thuộc tính của tài liệu văn bản thay đổi.
                // Tham số e: thông tin sự kiện.
                @Override
                public void changedUpdate(DocumentEvent e) {
                    capNhatGoiYMaSanPham();
                }
            });
        }
        // pnlSearchField: Khung chứa thành phần giao diện tìm kiếm trường.
        JPanel pnlSearchField = new JPanel(new BorderLayout(6, 0));
        pnlSearchField.setOpaque(false);
        pnlSearchField.add(new JLabel("Tìm sản phẩm"), BorderLayout.WEST);
        cmbMaSanPham.setToolTipText("Nhập mã hoặc tên sản phẩm");
        pnlSearchField.add(cmbMaSanPham, BorderLayout.CENTER);
        // btnTim: Nút thao tác: Xác nhận.
        JButton btnTim = new JButton("Xác nhận");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTim.setActionCommand("CHOOSE_PRODUCT");
        btnTim.addActionListener(this);
        // pnlDatePanel: Khung chứa thành phần giao diện ngày khung.
        JPanel pnlDatePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlDatePanel.setOpaque(false);
        pnlDatePanel.add(new JLabel("Ngày lập"));
        txtNgayLap.setEditable(false);
        txtNgayLap.setHorizontalAlignment(SwingConstants.CENTER);
        txtNgayLap.setPreferredSize(new Dimension(150, txtNgayLap.getPreferredSize().height));
        capNhatNgayLap();
        pnlDatePanel.add(txtNgayLap);
        // pnlSearchLeft: Khung chứa thành phần giao diện tìm kiếm trái.
        JPanel pnlSearchLeft = new JPanel(new BorderLayout(6, 0));
        pnlSearchLeft.setOpaque(false);
        pnlSearchLeft.add(pnlSearchField, BorderLayout.CENTER);
        pnlSearchLeft.add(btnTim, BorderLayout.EAST);
        pnlSearch.add(pnlSearchLeft, BorderLayout.CENTER);
        pnlSearch.add(pnlDatePanel, BorderLayout.EAST);
        pnlOrder.add(pnlSearch, BorderLayout.NORTH);

        tblDonHang.setRowHeight(25);
        tblDonHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDonHang.getTableHeader().setBackground(NAVY);
        tblDonHang.getTableHeader().setForeground(Color.WHITE);
        tblDonHang.setFillsViewportHeight(true);
        tblDonHang.setShowHorizontalLines(true);
        tblDonHang.setGridColor(new Color(232, 236, 240));
        tblDonHang.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblDonHang.getColumnModel().getColumn(1).setPreferredWidth(240);
        tblDonHang.getColumnModel().getColumn(2).setPreferredWidth(55);
        tblDonHang.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblDonHang.getColumnModel().getColumn(4).setPreferredWidth(110);
        tblDonHang.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        tblDonHang.getColumnModel().getColumn(3).setCellRenderer(new MoneyRenderer());
        tblDonHang.getColumnModel().getColumn(4).setCellRenderer(new MoneyRenderer());
        tblDonHang.getColumnModel().getColumn(2).setCellEditor(
                new DefaultCellEditor(new JTextField()));
        // scrCuon2: Khung cuộn khung cuộn.
        JScrollPane scrCuon2 = new JScrollPane(tblDonHang);
        scrCuon2.setBorder(BorderFactory.createLineBorder(BORDER));
        pnlOrder.add(scrCuon2, BorderLayout.CENTER);

        pnlOrder.add(createOrderFooter(), BorderLayout.SOUTH);
        pnlKhung.add(pnlOrder, BorderLayout.CENTER);
        return pnlKhung;
    }

    // Tạo khung thông tin khách hàng.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createCustomerPanel() {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new GridBagLayout());
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("KHÁCH HÀNG"),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        // gbc: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng GridBagLayout.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        pnlKhung.add(new JLabel("Mã khách hàng"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.45;
        cmbMaKhachHang.setEditable(true);
        cmbMaKhachHang.setToolTipText("Nhập mã khách hàng");
        Component editor = cmbMaKhachHang.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            // txtMaKhachHang: Ô nhập/hiển thị văn bản mã khách hàng.
            JTextField txtMaKhachHang = (JTextField) editor;
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            txtMaKhachHang.setActionCommand("CONFIRM_CUSTOMER");
            txtMaKhachHang.addActionListener(this);
            txtMaKhachHang.getDocument().addDocumentListener(new DocumentListener() {
                // Cập nhật trạng thái liên quan khi người dùng thêm ký tự vào ô văn bản.
                // Tham số e: thông tin sự kiện.
                @Override
                public void insertUpdate(DocumentEvent e) {
                    capNhatGoiYMaKhachHang();
                }

                // Cập nhật trạng thái liên quan khi người dùng xóa ký tự trong ô văn bản.
                // Tham số e: thông tin sự kiện.
                @Override
                public void removeUpdate(DocumentEvent e) {
                    capNhatGoiYMaKhachHang();
                }

                // Nhận thông báo khi thuộc tính của tài liệu văn bản thay đổi.
                // Tham số e: thông tin sự kiện.
                @Override
                public void changedUpdate(DocumentEvent e) {
                    capNhatGoiYMaKhachHang();
                }
            });
        }
        pnlKhung.add(cmbMaKhachHang, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        // btnXacNhan: Nút thao tác: Xác nhận.
        JButton btnXacNhan = new JButton("Xác nhận");
        btnXacNhan.setMargin(new Insets(3, 8, 3, 8));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnXacNhan.setActionCommand("CONFIRM_CUSTOMER");
        btnXacNhan.addActionListener(this);
        pnlKhung.add(btnXacNhan, gbc);
        gbc.gridx = 3;
        gbc.weightx = 0;
        pnlKhung.add(new JLabel("Số điện thoại"), gbc);
        gbc.gridx = 4;
        gbc.weightx = 0.55;
        txtSoDienThoai.setEditable(false);
        pnlKhung.add(txtSoDienThoai, gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        pnlKhung.add(new JLabel("Tên khách hàng"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.45;
        txtTenKhachHang.setColumns(18);
        txtTenKhachHang.setEditable(false);
        pnlKhung.add(txtTenKhachHang, gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        pnlKhung.add(new JLabel("Mã phiếu dịch vụ"), gbc);
        gbc.gridx = 4;
        gbc.weightx = 0.55;
        txtMaPhieuDichVu.setEditable(false);
        txtMaPhieuDichVu.setToolTipText("Các phiếu dịch vụ đang xử lý của khách hàng");
        pnlKhung.add(txtMaPhieuDichVu, gbc);
        txtKhachHangTomTat.setEditable(false);
        return pnlKhung;
    }

    // Tạo phần tổng kết đơn hàng.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createOrderFooter() {
        // pnlFooter2: Khung chứa thành phần giao diện cuối trang.
        JPanel pnlFooter2 = new JPanel(new GridBagLayout());
        pnlFooter2.setOpaque(false);
        // gbc: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng GridBagLayout.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 0, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.7;
        pnlFooter2.add(new JLabel("Khách hàng"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.3;
        pnlFooter2.add(new JLabel("Voucher"), gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.7;
        gbc.gridwidth = 1;
        txtKhachHangTomTat.setToolTipText("Khách hàng đã xác nhận");
        txtKhachHangTomTat.setEditable(false);
        pnlFooter2.add(txtKhachHangTomTat, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.3;
        // pnlVoucherRow: Khung chứa thành phần giao diện voucher dòng.
        JPanel pnlVoucherRow = new JPanel(new BorderLayout(5, 0));
        pnlVoucherRow.setOpaque(false);
        cmbVoucher.setEditable(true);
        cmbVoucher.setToolTipText("Nhập mã hoặc tên voucher");
        Component voucherEditor = cmbVoucher.getEditor().getEditorComponent();
        if (voucherEditor instanceof JTextField) {
            JTextField txtVoucher = (JTextField) voucherEditor;
            txtVoucher.setActionCommand("APPLY_VOUCHER");
            txtVoucher.addActionListener(this);
            txtVoucher.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    capNhatGoiYVoucher();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    capNhatGoiYVoucher();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    capNhatGoiYVoucher();
                }
            });
        }
        pnlVoucherRow.add(cmbVoucher, BorderLayout.CENTER);
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnApDungVoucher.setActionCommand("APPLY_VOUCHER");
        btnApDungVoucher.addActionListener(this);
        // pnlVoucherActions: Khung chứa thành phần giao diện voucher thao tác.
        JPanel pnlVoucherActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlVoucherActions.setOpaque(false);
        // btnXoa: Nút thao tác: Xóa sản phẩm.
        JButton btnXoa = new JButton("Xóa sản phẩm");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnXoa.setActionCommand("REMOVE_PRODUCT");
        btnXoa.addActionListener(this);
        pnlVoucherActions.add(btnApDungVoucher);
        pnlVoucherActions.add(btnXoa);
        pnlVoucherRow.add(pnlVoucherActions, BorderLayout.EAST);
        pnlFooter2.add(pnlVoucherRow, gbc);
        return pnlFooter2;
    }

    // Tạo khung thanh toán.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createPaymentPanel() {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new BorderLayout(8, 10));
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // pnlOverview: Khung chứa thành phần giao diện tổng quan.
        JPanel pnlOverview = new JPanel(new GridBagLayout());
        pnlOverview.setBackground(Color.WHITE);
        pnlOverview.setBorder(BorderFactory.createTitledBorder("Chi tiết thanh toán"));
        // gbc: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng GridBagLayout.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.gridy = 0;
        pnlOverview.add(new JLabel("Tiền hàng"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        lblTongHang.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlOverview.add(lblTongHang, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        pnlOverview.add(new JLabel("Voucher"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        lblGiamGia.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlOverview.add(lblGiamGia, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        pnlOverview.add(new JLabel("Tổng thanh toán"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        lblTongThanhToan.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTongThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTongThanhToan.setForeground(NAVY);
        pnlOverview.add(lblTongThanhToan, gbc);
        pnlKhung.add(pnlOverview, BorderLayout.NORTH);

        // pnlPayment: Khung chứa thành phần giao diện thanh toán.
        JPanel pnlPayment = new JPanel(new GridBagLayout());
        pnlPayment.setBackground(Color.WHITE);
        pnlPayment.setBorder(BorderFactory.createTitledBorder("Hình thức thanh toán"));
        // pg: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng GridBagLayout.
        GridBagConstraints pg = new GridBagConstraints();
        pg.gridx = 0;
        pg.gridy = 0;
        pg.weightx = 1;
        pg.fill = GridBagConstraints.HORIZONTAL;
        pg.insets = new Insets(5, 6, 8, 6);
        pnlPaymentMethodsPanel.setOpaque(false);
        pnlPayment.add(pnlPaymentMethodsPanel, pg);
        pg.gridy++;
        pnlPayment.add(new JLabel("Tiền khách đưa"), pg);
        pg.gridy++;
        txtTienKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            // Cập nhật trạng thái liên quan khi người dùng thêm ký tự vào ô văn bản.
            // Tham số e: thông tin sự kiện.
            @Override
            public void insertUpdate(DocumentEvent e) {
                capNhatTien();
            }

            // Cập nhật trạng thái liên quan khi người dùng xóa ký tự trong ô văn bản.
            // Tham số e: thông tin sự kiện.
            @Override
            public void removeUpdate(DocumentEvent e) {
                capNhatTien();
            }

            // Nhận thông báo khi thuộc tính của tài liệu văn bản thay đổi.
            // Tham số e: thông tin sự kiện.
            @Override
            public void changedUpdate(DocumentEvent e) {
                capNhatTien();
            }
        });
        pnlPayment.add(createMoneyField(txtTienKhachDua), pg);
        pg.gridy++;
        // pnlChangePanel: Khung chứa thành phần giao diện thay đổi khung.
        JPanel pnlChangePanel = new JPanel(new BorderLayout());
        pnlChangePanel.setBackground(LIGHT_BLUE);
        pnlChangePanel.setBorder(BorderFactory.createEmptyBorder(7, 9, 7, 9));
        pnlChangePanel.add(new JLabel("Số tiền thừa"), BorderLayout.WEST);
        lblTienThua.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlChangePanel.add(lblTienThua, BorderLayout.EAST);
        pnlPayment.add(pnlChangePanel, pg);

        // pnlMiddle: Khung chứa thành phần giao diện giữa.
        JPanel pnlMiddle = new JPanel(new BorderLayout(0, 10));
        pnlMiddle.setOpaque(false);
        pnlMiddle.add(pnlPayment, BorderLayout.CENTER);
        pnlKhung.add(pnlMiddle, BorderLayout.CENTER);

        // btnThanhToan: Nút thao tác: THANH TOÁN & IN HÓA ĐƠN.
        JButton btnThanhToan = new JButton("THANH TOÁN & IN HÓA ĐƠN");
        btnThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setBackground(GREEN);
        btnThanhToan.setFocusPainted(false);
        btnThanhToan.setPreferredSize(new Dimension(100, 48));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnThanhToan.setActionCommand("COMPLETE_PAYMENT");
        btnThanhToan.addActionListener(this);
        pnlKhung.add(btnThanhToan, BorderLayout.SOUTH);
        return pnlKhung;
    }

    // Tạo vùng nhập tiền.
    // Tham số txtField: trường.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createMoneyField(JTextField txtField) {
        // pnlWrapper: Khung chứa thành phần giao diện khung bọc.
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setBorder(BorderFactory.createLineBorder(BORDER));
        pnlWrapper.add(txtField, BorderLayout.CENTER);
        // lblCurrency: Nhãn hiển thị: VND.
        JLabel lblCurrency = new JLabel("VND");
        lblCurrency.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 7));
        pnlWrapper.add(lblCurrency, BorderLayout.EAST);
        return pnlWrapper;
    }

    // Nạp phương thức thanh toán thanh toán.
    private void napPhuongThucThanhToan() {
        try {
            // phuongThucs: Lưu các phương thức thanh toán; phần tử có kiểu entity.ThanhToan.PhuongThuc.
            List<entity.ThanhToan.PhuongThuc> phuongThucs =
                    dao.layPhuongThucThanhToan();
            ButtonGroup group = new ButtonGroup();
            pnlPaymentMethodsPanel.removeAll();
            phuongThucTheoNut.clear();
            phuongThucHienTai = null;
            for (entity.ThanhToan.PhuongThuc phuongThuc : phuongThucs) {
                // tglThaoTac: Nút bật/tắt nút thao tác.
                JToggleButton tglThaoTac = new JToggleButton(
                        "<html><center>" + phuongThuc.getTenPhuongThuc() + "</center></html>");
                tglThaoTac.setFocusPainted(false);
                tglThaoTac.setBackground(Color.WHITE);
                tglThaoTac.setOpaque(true);
                tglThaoTac.setBorder(BorderFactory.createLineBorder(BORDER));
                tglThaoTac.setPreferredSize(new Dimension(95, 48));
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                tglThaoTac.setActionCommand("SELECT_PAYMENT_METHOD");
                tglThaoTac.addActionListener(this);
                phuongThucTheoNut.put(tglThaoTac, phuongThuc);
                group.add(tglThaoTac);
                pnlPaymentMethodsPanel.add(tglThaoTac);
                if (phuongThucHienTai == null) {
                    tglThaoTac.setSelected(true);
                    tglThaoTac.setBackground(new Color(215, 231, 244));
                    phuongThucHienTai = phuongThuc;
                }
            }
            pnlPaymentMethodsPanel.revalidate();
            pnlPaymentMethodsPanel.repaint();
        } catch (Exception e) {
            hienLoi("Không tải được phương thức thanh toán: " + e.getMessage());
        }
    }

    // Nạp danh sách mã sản phẩm.
    private void napDanhSachMaSanPham() {
        Object giaTriDangNhap = cmbMaSanPham.getEditor().getItem();
        try {
            Map<String, String> sanPhamMoi = dao.layDanhSachSanPham();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            String mauHienThi = "";
            for (Map.Entry<String, String> sanPham : sanPhamMoi.entrySet()) {
                String goiY = sanPham.getKey();
                if (sanPham.getValue() != null && !sanPham.getValue().isBlank()) {
                    goiY += " - " + sanPham.getValue();
                }
                model.addElement(goiY);
                if (goiY.length() > mauHienThi.length()) {
                    mauHienThi = goiY;
                }
            }
            tenSanPhamTheoMa.clear();
            tenSanPhamTheoMa.putAll(sanPhamMoi);
            danhSachMaSanPham.clear();
            danhSachMaSanPham.addAll(sanPhamMoi.keySet());
            dangCapNhatGoiYMaSanPham = true;
            try {
                cmbMaSanPham.setModel(model);
                cmbMaSanPham.setSelectedItem(giaTriDangNhap == null ? "" : giaTriDangNhap);
                cmbMaSanPham.getEditor().setItem(giaTriDangNhap == null ? "" : giaTriDangNhap);
            } finally {
                dangCapNhatGoiYMaSanPham = false;
            }
            cmbMaSanPham.setPrototypeDisplayValue(mauHienThi);
            cmbMaSanPham.setPreferredSize(cmbMaSanPham.getPreferredSize());
            if (giaTriDangNhap != null && !giaTriDangNhap.toString().isBlank()) {
                capNhatGoiYMaSanPham();
            }
        } catch (Exception e) {
            hienLoi("Không tải được danh sách mã sản phẩm: " + e.getMessage());
        }
    }

    // Nạp danh sách mã khách hàng.
    public void napDanhSachMaKhachHang() {
        Object giaTriDangNhap = cmbMaKhachHang.getEditor().getItem();
        try {
            List<entity.ThanhToan.KhachHang> danhSachMoi = dao.layDanhSachKhachHang();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            String maDaiNhat = "KH000000";
            for (entity.ThanhToan.KhachHang khachHang : danhSachMoi) {
                model.addElement(taoGoiYKhachHang(khachHang));
                if (khachHang.getMaKhachHang().length() > maDaiNhat.length()) {
                    maDaiNhat = khachHang.getMaKhachHang();
                }
            }
            danhSachKhachHang.clear();
            danhSachKhachHang.addAll(danhSachMoi);
            dangCapNhatGoiYMaKhachHang = true;
            try {
                cmbMaKhachHang.setModel(model);
                cmbMaKhachHang.setSelectedItem(giaTriDangNhap == null ? "" : giaTriDangNhap);
                cmbMaKhachHang.getEditor().setItem(giaTriDangNhap == null ? "" : giaTriDangNhap);
            } finally {
                dangCapNhatGoiYMaKhachHang = false;
            }
            cmbMaKhachHang.setPrototypeDisplayValue(maDaiNhat);
            cmbMaKhachHang.setPreferredSize(cmbMaKhachHang.getPreferredSize());
        } catch (SQLException e) {
            hienLoi("Không tải được danh sách mã khách hàng: " + e.getMessage());
        }
    }

    // Nạp danh sách voucher đang hoạt động.
    private void napDanhSachVoucher() {
        Object giaTriDangNhap = cmbVoucher.getEditor().getItem();
        try {
            List<entity.ThanhToan.Voucher> voucherMoi = dao.layDanhSachVoucher();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            String maDaiNhat = "VOUCHER000";
            for (entity.ThanhToan.Voucher item : voucherMoi) {
                String goiY = taoGoiYVoucher(item);
                model.addElement(goiY);
                if (item.getMaVoucher().length() > maDaiNhat.length()) {
                    maDaiNhat = item.getMaVoucher();
                }
            }
            danhSachVoucher.clear();
            danhSachVoucher.addAll(voucherMoi);
            dangCapNhatGoiYVoucher = true;
            try {
                cmbVoucher.setModel(model);
                cmbVoucher.setSelectedItem(giaTriDangNhap == null ? "" : giaTriDangNhap);
                cmbVoucher.getEditor().setItem(giaTriDangNhap == null ? "" : giaTriDangNhap);
            } finally {
                dangCapNhatGoiYVoucher = false;
            }
            cmbVoucher.setPrototypeDisplayValue(maDaiNhat);
            cmbVoucher.setPreferredSize(cmbVoucher.getPreferredSize());
            if (giaTriDangNhap != null && !giaTriDangNhap.toString().isBlank()) {
                capNhatGoiYVoucher();
            }
        } catch (SQLException e) {
            hienLoi("Không tải được danh sách voucher: " + e.getMessage());
        }
    }

    /** Làm mới dữ liệu gợi ý khi mở lại màn hình thanh toán. */
    public void napDanhSachGoiYSanPhamVaVoucher() {
        napDanhSachMaSanPham();
        napDanhSachVoucher();
    }

    // Cập nhật gợi y mã khách hàng.
    private void capNhatGoiYMaKhachHang() {
        if (dangCapNhatGoiYMaKhachHang) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (dangCapNhatGoiYMaKhachHang) {
                return;
            }

            Component editorComponent = cmbMaKhachHang.getEditor().getEditorComponent();
            if (!(editorComponent instanceof JTextField)) {
                return;
            }
            // txtEditor: Ô nhập/hiển thị văn bản bộ chỉnh sửa.
            JTextField txtEditor = (JTextField) editorComponent;
            // tuKhoa: Đọc nội dung chữ từ ô nhập để xử lý từ khóa tìm kiếm.
            String tuKhoa = txtEditor.getText();
            String maTimKiem = layMaKhachHangTuGoiY(tuKhoa);
            // goiY: Lưu gợi y; phần tử có kiểu String.
            List<String> goiY = new ArrayList<>();
            String tuKhoaDaChuanHoa = maTimKiem.toLowerCase(Locale.ROOT);
            for (entity.ThanhToan.KhachHang khachHang : danhSachKhachHang) {
                if (maCoChuaTatCaKyTu(
                        khachHang.getMaKhachHang(), tuKhoaDaChuanHoa)) {
                    goiY.add(taoGoiYKhachHang(khachHang));
                }
            }
            for (entity.ThanhToan.KhachHang khachHang : danhSachKhachHang) {
                if (khachHang.getMaKhachHang().equalsIgnoreCase(maTimKiem)) {
                    goiY.clear();
                    goiY.add(taoGoiYKhachHang(khachHang));
                    break;
                }
            }

            int viTriConTro = txtEditor.getCaretPosition();
            dangCapNhatGoiYMaKhachHang = true;
            try {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                for (String ma : goiY) {
                    model.addElement(ma);
                }
                cmbMaKhachHang.setModel(model);
                cmbMaKhachHang.setSelectedItem(tuKhoa);
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtEditor.setText(tuKhoa);
                txtEditor.setCaretPosition(Math.min(viTriConTro, tuKhoa.length()));
            } finally {
                dangCapNhatGoiYMaKhachHang = false;
            }

            boolean daChonGoiY = danhSachKhachHang.stream().anyMatch(khachHang ->
                    khachHang.getMaKhachHang().equalsIgnoreCase(maTimKiem)
                    && tuKhoa.equals(taoGoiYKhachHang(khachHang)));
            if (!maTimKiem.isEmpty() && !goiY.isEmpty()
                    && !daChonGoiY) {
                cmbMaKhachHang.showPopup();
            } else if (cmbMaKhachHang.isPopupVisible()) {
                cmbMaKhachHang.hidePopup();
            }
        });
    }

    // Tạo gợi ý khách hàng gồm mã và họ tên.
    private String taoGoiYKhachHang(entity.ThanhToan.KhachHang khachHang) {
        String ten = khachHang.getHoTen();
        return ten == null || ten.isBlank()
                ? khachHang.getMaKhachHang()
                : khachHang.getMaKhachHang() + " - " + ten;
    }

    // Lấy mã khách hàng từ nội dung ô nhập hoặc gợi ý đã chọn.
    private String layMaKhachHangTuGoiY(String tuKhoa) {
        int separator = tuKhoa.indexOf(" - ");
        if (separator > 0) {
            String ma = tuKhoa.substring(0, separator);
            for (entity.ThanhToan.KhachHang khachHang : danhSachKhachHang) {
                if (khachHang.getMaKhachHang().equalsIgnoreCase(ma)
                        && tuKhoa.equals(taoGoiYKhachHang(khachHang))) {
                    return ma;
                }
            }
        }
        return tuKhoa.trim();
    }

    // Cập nhật gợi y mã sản phẩm.
    private void capNhatGoiYMaSanPham() {
        if (dangCapNhatGoiYMaSanPham) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (dangCapNhatGoiYMaSanPham) {
                return;
            }

            Component editorComponent = cmbMaSanPham.getEditor().getEditorComponent();
            if (!(editorComponent instanceof JTextField)) {
                return;
            }
            // txtEditor: Ô nhập/hiển thị văn bản bộ chỉnh sửa.
            JTextField txtEditor = (JTextField) editorComponent;
            // tuKhoa: Đọc nội dung chữ từ ô nhập để xử lý từ khóa tìm kiếm.
            String tuKhoa = txtEditor.getText();
            String maTimKiem = layMaSanPhamTuGoiY(tuKhoa);
            String tenTimKiem = maTimKiem.toLowerCase(Locale.ROOT);
            // goiY: Lưu gợi y; phần tử có kiểu String.
            List<String> goiY = new ArrayList<>();
            String tuKhoaKhongPhanBietHoaThuong = maTimKiem.toLowerCase(Locale.ROOT);
            for (String ma : danhSachMaSanPham) {
                String ten = tenSanPhamTheoMa.get(ma);
                if (maCoChuaTatCaKyTu(ma, tuKhoaKhongPhanBietHoaThuong)
                        || (ten != null && ten.toLowerCase(Locale.ROOT).contains(tenTimKiem))) {
                    goiY.add(ma);
                }
            }

            int viTriConTro = txtEditor.getCaretPosition();
            dangCapNhatGoiYMaSanPham = true;
            try {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                for (String ma : goiY) {
                    model.addElement(taoGoiYSanPham(ma));
                }
                cmbMaSanPham.setModel(model);
                cmbMaSanPham.setSelectedItem(tuKhoa);
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtEditor.setText(tuKhoa);
                txtEditor.setCaretPosition(Math.min(viTriConTro, tuKhoa.length()));
            } finally {
                dangCapNhatGoiYMaSanPham = false;
            }

            boolean daChonGoiY = tenSanPhamTheoMa.containsKey(maTimKiem)
                    && tuKhoa.equals(taoGoiYSanPham(maTimKiem));
            if (!maTimKiem.isEmpty() && !goiY.isEmpty() && !daChonGoiY
                    && !(goiY.size() == 1 && goiY.get(0).equalsIgnoreCase(maTimKiem))) {
                cmbMaSanPham.showPopup();
            } else if (cmbMaSanPham.isPopupVisible()) {
                cmbMaSanPham.hidePopup();
            }
        });
    }

    // Cập nhật gợi ý voucher.
    private void capNhatGoiYVoucher() {
        if (dangCapNhatGoiYVoucher) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (dangCapNhatGoiYVoucher) {
                return;
            }

            Component editorComponent = cmbVoucher.getEditor().getEditorComponent();
            if (!(editorComponent instanceof JTextField)) {
                return;
            }
            JTextField txtEditor = (JTextField) editorComponent;
            String tuKhoa = txtEditor.getText();
            String maTimKiem = layMaVoucherTuGoiY(tuKhoa);
            String tuKhoaDaChuanHoa = maTimKiem.toLowerCase(Locale.ROOT);
            List<entity.ThanhToan.Voucher> goiY = new ArrayList<>();
            for (entity.ThanhToan.Voucher item : danhSachVoucher) {
                String tenVoucher = item.getTenVoucher();
                if (maCoChuaTatCaKyTu(item.getMaVoucher(), tuKhoaDaChuanHoa)
                        || (tenVoucher != null
                                && tenVoucher.toLowerCase(Locale.ROOT)
                                        .contains(tuKhoaDaChuanHoa))) {
                    goiY.add(item);
                }
            }
            for (entity.ThanhToan.Voucher item : danhSachVoucher) {
                if (item.getMaVoucher().equalsIgnoreCase(maTimKiem)) {
                    goiY.clear();
                    goiY.add(item);
                    break;
                }
            }

            int viTriConTro = txtEditor.getCaretPosition();
            dangCapNhatGoiYVoucher = true;
            try {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                for (entity.ThanhToan.Voucher item : goiY) {
                    model.addElement(taoGoiYVoucher(item));
                }
                cmbVoucher.setModel(model);
                cmbVoucher.setSelectedItem(tuKhoa);
                txtEditor.setText(tuKhoa);
                txtEditor.setCaretPosition(Math.min(viTriConTro, tuKhoa.length()));
            } finally {
                dangCapNhatGoiYVoucher = false;
            }

            boolean daChonGoiY = danhSachVoucher.stream().anyMatch(item ->
                    item.getMaVoucher().equalsIgnoreCase(maTimKiem)
                    && tuKhoa.equals(taoGoiYVoucher(item)));
            if (!maTimKiem.isEmpty() && !goiY.isEmpty() && !daChonGoiY) {
                cmbVoucher.showPopup();
            } else if (cmbVoucher.isPopupVisible()) {
                cmbVoucher.hidePopup();
            }
        });
    }

    // Tạo gợi ý voucher gồm mã và tên.
    private String taoGoiYVoucher(entity.ThanhToan.Voucher item) {
        String ten = item.getTenVoucher();
        return ten == null || ten.isBlank()
                ? item.getMaVoucher() : item.getMaVoucher() + " - " + ten;
    }

    // Lấy mã voucher từ nội dung ô nhập hoặc gợi ý đã chọn.
    private String layMaVoucherTuGoiY(String tuKhoa) {
        int separator = tuKhoa.indexOf(" - ");
        if (separator > 0) {
            String ma = tuKhoa.substring(0, separator);
            for (entity.ThanhToan.Voucher item : danhSachVoucher) {
                if (item.getMaVoucher().equalsIgnoreCase(ma)
                        && tuKhoa.equals(taoGoiYVoucher(item))) {
                    return ma;
                }
            }
        }
        return tuKhoa.trim();
    }

    // Lấy mã voucher đã nhập hoặc đã chọn.
    private String layMaVoucherNhap() {
        Object item = cmbVoucher.getEditor().getItem();
        return item == null ? "" : layMaVoucherTuGoiY(item.toString());
    }

    // Tạo gợi y sản phẩm.
    // Tham số ma: mã.
    // Kết quả: chuỗi kết quả.
    private String taoGoiYSanPham(String ma) {
        // ten: Lưu tên.
        String ten = tenSanPhamTheoMa.get(ma);
        return ten == null || ten.isBlank() ? ma : ma + " - " + ten;
    }

    // Lấy mã sản phẩm từ gợi y.
    // Tham số tuKhoa: từ khóa tìm kiếm.
    // Kết quả: chuỗi kết quả.
    private String layMaSanPhamTuGoiY(String tuKhoa) {
        int separator = tuKhoa.indexOf(" - ");
        if (separator > 0) {
            // ma: Lưu mã.
            String ma = tuKhoa.substring(0, separator);
            if (tenSanPhamTheoMa.containsKey(ma)) {
                return ma;
            }
        }
        return tuKhoa.trim();
    }

    // Kiểm tra mã có chứa đủ các ký tự của từ khóa đã chuẩn hóa hay không.
    // Tham số ma: mã.
    // Tham số tuKhoaDaChuanHoa: từ khóa tìm kiếm đã chuẩn hóa.
    // Kết quả: true/false theo điều kiện xử lý.
    private boolean maCoChuaTatCaKyTu(String ma, String tuKhoaDaChuanHoa) {
        String maDaChuanHoa = ma.toLowerCase(Locale.ROOT);
        for (int i = 0; i < tuKhoaDaChuanHoa.length(); i++) {
            if (maDaChuanHoa.indexOf(tuKhoaDaChuanHoa.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }

    // Xác nhận khách hàng.
    private void xacNhanKhachHang() {
        // tuKhoa: Lưu từ khóa tìm kiếm.
        String tuKhoa = layMaKhachHangNhap();
        if (tuKhoa.isEmpty()) {
            hienLoi("Nhập mã khách hàng.");
            return;
        }
        try {
            entity.ThanhToan.KhachHang khachHang = dao.timKhachHang(tuKhoa);
            if (khachHang == null) {
                xoaKhachHangDaChon();
                hienLoi("Không tìm thấy khách hàng.");
                return;
            }
            // phieuDichVus: Bảng ánh xạ khóa đến giá trị dùng cho các phiếu dịch vụ.
            Map<String, List<entity.ThanhToan>> phieuDichVus =
                    dao.layPhieuDichVuDangXuLy(khachHang.getMaKhachHang());
            xoaDichVuTuPhieuDaThem();
            maKhachHang = khachHang.getMaKhachHang();
            maKhachHangDaXacNhan = tuKhoa;
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtTenKhachHang.setText(khachHang.getHoTen());
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtSoDienThoai.setText(khachHang.getSoDienThoai());
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtKhachHangTomTat.setText(
                    maKhachHang + " - " + khachHang.getHoTen()
                            + " - " + khachHang.getSoDienThoai());
            maPhieuDichVuTrongDonHang.clear();
            for (Map.Entry<String, List<entity.ThanhToan>> phieu : phieuDichVus.entrySet()) {
                maPhieuDichVuTrongDonHang.add(phieu.getKey());
                for (entity.ThanhToan dichVu : phieu.getValue()) {
                    chiTiet.add(dichVu);
                    dichVuTuPhieuDaThem.add(dichVu);
                }
            }
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtMaPhieuDichVu.setText(maPhieuDichVuTrongDonHang.isEmpty()
                    ? "Không có phiếu cần thanh toán"
                    : String.join(", ", maPhieuDichVuTrongDonHang));
            tableModel.fireTableDataChanged();
            capNhatTien();
        } catch (Exception e) {
            hienLoi("Không tra cứu được khách hàng: " + e.getMessage());
        }
    }

    // Xóa khách hàng đã chọn.
    private void xoaKhachHangDaChon() {
        xoaDichVuTuPhieuDaThem();
        maKhachHang = null;
        maKhachHangDaXacNhan = null;
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtTenKhachHang.setText("");
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtSoDienThoai.setText("");
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtMaPhieuDichVu.setText("");
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtKhachHangTomTat.setText("");
    }

    // Xóa các dòng dịch vụ được thêm từ phiếu.
    private void xoaDichVuTuPhieuDaThem() {
        if (!dichVuTuPhieuDaThem.isEmpty()) {
            chiTiet.removeAll(dichVuTuPhieuDaThem);
            dichVuTuPhieuDaThem.clear();
            tableModel.fireTableDataChanged();
            capNhatTien();
        }
        maPhieuDichVuTrongDonHang.clear();
    }

    // Lấy mã khách hàng nhập.
    // Kết quả: chuỗi kết quả.
    private String layMaKhachHangNhap() {
        Object item = cmbMaKhachHang.getEditor().getItem();
        return item == null ? "" : layMaKhachHangTuGoiY(item.toString());
    }

    // Mở chọn sản phẩm.
    private void moChonSanPham() {
        Object item = cmbMaSanPham.getEditor().getItem();
        moChonSanPham(item == null ? "" : layMaSanPhamTuGoiY(item.toString()).trim());
    }

    // Mở chọn sản phẩm.
    // Tham số tuKhoa: từ khóa tìm kiếm.
    private void moChonSanPham(String tuKhoa) {
        tuKhoa = layMaSanPhamTuGoiY(tuKhoa).trim();
        if (tuKhoa.isEmpty()) {
            hienLoi("Nhập mã sản phẩm.");
            return;
        }
        try {
            // ketQua: Lưu kết quả; phần tử có kiểu entity.ThanhToan.
            List<entity.ThanhToan> ketQua = dao.timMatHangTheoMa(tuKhoa);
            if (ketQua.isEmpty()) {
                hienLoi("Không tìm thấy sản phẩm.");
                return;
            }
            entity.ThanhToan sanPham = ketQua.get(0);
            // spnQuantity: Ô chọn giá trị bằng nút tăng/giảm số lượng.
            JSpinner spnQuantity = new JSpinner(new javax.swing.SpinnerNumberModel(1, 1, 999, 1));
            // lblItem: Nhãn hiển thị mục.
            JLabel lblItem = new JLabel(sanPham.getMaHang() + " - " + sanPham.getTenMatHang());
            // lblPrice: Nhãn hiển thị giá.
            JLabel lblPrice = new JLabel(dinhDangTien(sanPham.getDonGia()));
            // pnlAddDialog: Khung chứa thành phần giao diện thêm hộp thoại.
            JPanel pnlAddDialog = new JPanel(new GridBagLayout());
            // gbc: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng GridBagLayout.
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(7, 8, 7, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            pnlAddDialog.add(new JLabel("Mã / tên:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.gridwidth = 2;
            pnlAddDialog.add(lblItem, gbc);
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.weightx = 0;
            pnlAddDialog.add(new JLabel("Số lượng:"), gbc);
            gbc.gridx = 1;
            pnlAddDialog.add(spnQuantity, gbc);
            gbc.gridy++;
            gbc.gridx = 0;
            pnlAddDialog.add(new JLabel("Đơn giá:"), gbc);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            pnlAddDialog.add(lblPrice, gbc);
            int choice = JOptionPane.showConfirmDialog(
                    pnlNoiDung, pnlAddDialog, "Thêm sản phẩm",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice == JOptionPane.OK_OPTION) {
                themSanPham(sanPham, (Integer) spnQuantity.getValue());
                cmbMaSanPham.getEditor().setItem("");
            }
        } catch (Exception e) {
            hienLoi("Không tìm được sản phẩm: " + e.getMessage());
        }
    }

    // Thêm sản phẩm.
    // Tham số sanPham: sản phẩm.
    // Tham số soLuong: số lượng.
    private void themSanPham(entity.ThanhToan sanPham, int soLuong) {
        for (entity.ThanhToan item : chiTiet) {
            if (item.getMaHang().equals(sanPham.getMaHang())
                    && java.util.Objects.equals(item.getMaThuCung(), sanPham.getMaThuCung())) {
                item.setSoLuong(item.getSoLuong() + soLuong);
                tableModel.fireTableDataChanged();
                capNhatTien();
                return;
            }
        }
        sanPham.setSoLuong(soLuong);
        chiTiet.add(sanPham);
        tableModel.fireTableDataChanged();
        capNhatTien();
    }

    // Xóa sản phẩm.
    private void xoaSanPham() {
        // row: Lưu chỉ số dòng.
        int row = tblDonHang.getSelectedRow();
        if (row < 0) {
            hienLoi("Chọn sản phẩm cần xóa.");
            return;
        }
        int modelRow = tblDonHang.convertRowIndexToModel(row);
        if (dichVuTuPhieuDaThem.contains(chiTiet.get(modelRow))) {
            hienLoi("Không thể xóa dịch vụ thuộc phiếu dịch vụ đang chờ thanh toán.");
            return;
        }
        chiTiet.remove(modelRow);
        tableModel.fireTableDataChanged();
        capNhatTien();
    }

    // Hủy hóa đơn.
    private void huyHoaDon() {
        if (chiTiet.isEmpty() && maKhachHang == null && voucher == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                pnlNoiDung, "Bạn muốn hủy hóa đơn hiện tại?",
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            chiTiet.clear();
            tableModel.fireTableDataChanged();
            maKhachHang = null;
            maKhachHangDaXacNhan = null;
            cmbMaKhachHang.getEditor().setItem("");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtTenKhachHang.setText("");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtSoDienThoai.setText("");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtMaPhieuDichVu.setText("");
            maPhieuDichVuTrongDonHang.clear();
            dichVuTuPhieuDaThem.clear();
            voucher = null;
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            cmbVoucher.getEditor().setItem("");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtTienKhachDua.setText("");
            capNhatTien();
        }
    }

    // Áp dụng voucher.
    private void apDungVoucher() {
        // ma: Đọc nội dung chữ từ ô nhập để xử lý mã.
        String ma = layMaVoucherNhap();
        if (ma.isEmpty()) {
            voucher = null;
            capNhatTien();
            return;
        }
        try {
            entity.ThanhToan.Voucher timDuoc = dao.timVoucher(ma);
            if (timDuoc == null) {
                hienLoi("Voucher không tồn tại, chưa đến ngày bắt đầu, đã hết hạn "
                        + "hoặc đã hết lượt sử dụng.");
                return;
            }
            String loiDieuKien = kiemTraDieuKienVoucher(timDuoc);
            if (loiDieuKien != null) {
                hienLoi(loiDieuKien);
                return;
            }
            voucher = timDuoc;
            capNhatTien();
            JOptionPane.showMessageDialog(pnlNoiDung, "Đã áp dụng " + voucher.getTenVoucher()
                    + " (" + voucher.getPhanTramGiam().stripTrailingZeros().toPlainString()
                    + "%).");
        } catch (SQLException e) {
            hienLoi("Không áp dụng được voucher: " + e.getMessage());
        }
    }

    // Kiểm tra đơn hàng, lưu hóa đơn và xử lý bước xác nhận thanh toán.
    private void thanhToan() {
        if (maKhachHang == null
                || !layMaKhachHangNhap().equals(maKhachHangDaXacNhan)) {
            xoaKhachHangDaChon();
            hienLoi("Hãy xác nhận khách hàng trước khi thanh toán.");
            return;
        }
        if (chiTiet.isEmpty()) {
            hienLoi("Hóa đơn chưa có sản phẩm.");
            return;
        }
        entity.ThanhToan.PhuongThuc phuongThuc = phuongThucHienTai;
        if (phuongThuc == null) {
            hienLoi("Chưa có phương thức thanh toán đang hoạt động.");
            return;
        }
        if (voucher != null) {
            try {
                entity.ThanhToan.Voucher voucherMoi = dao.timVoucher(voucher.getMaVoucher());
                if (voucherMoi == null) {
                    voucher = null;
                    capNhatTien();
                    hienLoi("Voucher không còn hiệu lực, chưa đến ngày bắt đầu "
                            + "hoặc đã hết lượt sử dụng.");
                    return;
                }
                voucher = voucherMoi;
                String loiDieuKien = kiemTraDieuKienVoucher(voucher);
                if (loiDieuKien != null) {
                    hienLoi(loiDieuKien);
                    return;
                }
            } catch (SQLException e) {
                hienLoi("Không kiểm tra được điều kiện voucher: " + e.getMessage());
                return;
            }
        }

        // tongThanhToan: Giá trị tổng tiền thanh toán dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        BigDecimal tongThanhToan = tinhTongThanhToan();
        boolean tienMat = phuongThuc.getTenPhuongThuc()
                .toLowerCase(Locale.forLanguageTag("vi-VN"))
                .contains("tiền mặt");
        boolean chuyenKhoan = phuongThuc.getTenPhuongThuc()
                .toLowerCase(Locale.forLanguageTag("vi-VN"))
                .contains("chuyển khoản");
        if (tienMat) {
            // tienKhachDua: Giá trị số tiền khách đưa dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
            BigDecimal tienKhachDua;
            try {
                tienKhachDua = docTienKhachDua();
            } catch (NumberFormatException e) {
                hienLoi("Nhập số tiền khách đưa hợp lệ.");
                return;
            }
            if (tienKhachDua.compareTo(tongThanhToan) < 0) {
                hienLoi("Tiền khách đưa chưa đủ thanh toán.");
                return;
            }
        }

        if (!chuyenKhoan) {
            int confirm = JOptionPane.showConfirmDialog(
                    pnlNoiDung, "Xác nhận thanh toán " + dinhDangTien(tongThanhToan) + "?",
                    "Thanh toán", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        ngayLap = LocalDateTime.now();
        capNhatNgayLap();
        // maHoaDon: Lưu mã hóa đơn.
        String maHoaDon;
        try {
            maHoaDon = dao.taoMaHoaDon(maKhachHang, ngayLap);
        } catch (SQLException | IllegalArgumentException e) {
            hienLoi("Không tạo được mã hóa đơn: " + e.getMessage());
            return;
        }
        if (chuyenKhoan && !hienMaQrDemo(maHoaDon, tongThanhToan)) {
            return;
        }

        try {
            String maHoaDonDaLuu = dao.taoHoaDon(
                    maHoaDon,
                    maNhanVien,
                    maKhachHang,
                    voucher == null ? null : voucher.getMaVoucher(),
                    phuongThuc.getMaPhuongThuc(),
                    layNgayLap(),
                    voucher == null ? BigDecimal.ZERO : voucher.getPhanTramGiam(),
                    chiTiet,
                    maPhieuDichVuTrongDonHang);
            if (!maPhieuDichVuTrongDonHang.isEmpty()) {
                capNhatPhieuDichVu.run();
            }
            String noiDungHoaDon = taoNoiDungHoaDon(
                    phuongThuc.getTenPhuongThuc(), tongThanhToan);
            noiDungHoaDon = "PET SHOP PRO\nHÓA ĐƠN: " + maHoaDonDaLuu + "\n"
                    + noiDungHoaDon;
            hienXacNhanHoaDon(noiDungHoaDon, maHoaDonDaLuu);
            chiTiet.clear();
            tableModel.fireTableDataChanged();
            maKhachHang = null;
            maKhachHangDaXacNhan = null;
            maPhieuDichVuTrongDonHang.clear();
            dichVuTuPhieuDaThem.clear();
            cmbMaKhachHang.getEditor().setItem("");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtTenKhachHang.setText("");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtSoDienThoai.setText("");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtMaPhieuDichVu.setText("");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtKhachHangTomTat.setText("");
            voucher = null;
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            cmbVoucher.getEditor().setItem("");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtTienKhachDua.setText("");
            ngayLap = LocalDateTime.now();
            capNhatNgayLap();
            capNhatTien();
        } catch (Exception e) {
            hienLoi("Thanh toán thất bại; hóa đơn chưa được lưu: " + e.getMessage());
        }
    }

    // Hiển thị mã QR minh họa cho thông tin hóa đơn và số tiền.
    // Tham số maHoaDon: mã hóa đơn.
    // Tham số tongThanhToan: tổng tiền thanh toán.
    // Kết quả: true/false theo điều kiện xử lý.
    private boolean hienMaQrDemo(String maHoaDon, BigDecimal tongThanhToan) {
        // duLieuQr: Lưu nội dung được mã hóa trong QR.
        String duLieuQr = "PETSHOP-DEMO|" + maHoaDon + "|" + tongThanhToan.toPlainString();
        // qrImage: Ảnh trong bộ nhớ dùng cho QR ảnh.
        BufferedImage qrImage;
        try {
            qrImage = taoQrImage(duLieuQr);
        } catch (WriterException e) {
            hienLoi("Không tạo được mã QR: " + e.getMessage());
            return false;
        }

        // pnlContent: Khung chứa thành phần giao diện nội dung.
        JPanel pnlContent = new JPanel(new BorderLayout(8, 8));
        pnlContent.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlContent.add(new QrCodePanel(qrImage), BorderLayout.CENTER);
        // pnlDetails: Khung chứa thành phần giao diện các dòng chi tiết.
        JPanel pnlDetails = new JPanel(new GridLayout(0, 1, 4, 4));
        pnlDetails.add(new JLabel("Mã đơn: " + maHoaDon));
        pnlDetails.add(new JLabel("Số tiền: " + dinhDangTien(tongThanhToan)));
        // lblNote: Nhãn hiển thị ghi chú hướng dẫn.
        JLabel lblNote = new JLabel(
                "<html><b>QR minh họa</b> — Chưa kết nối cổng thanh toán ngân hàng.</html>");
        lblNote.setForeground(new Color(180, 75, 25));
        pnlDetails.add(lblNote);
        pnlContent.add(pnlDetails, BorderLayout.SOUTH);

        int choice = JOptionPane.showOptionDialog(
                pnlNoiDung,
                pnlContent,
                "Mã QR minh họa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[] { "Xác nhận", "Hủy" },
                "Hủy");
        return choice == 0;
    }

    // Mã hóa nội dung thành ảnh QR bằng thư viện ZXing.
    // Tham số duLieuQr: nội dung được mã hóa trong QR.
    // Kết quả: đối tượng/giá trị kiểu BufferedImage.
    private static BufferedImage taoQrImage(String duLieuQr) throws WriterException {
        // hints: Bảng ánh xạ khóa đến giá trị dùng cho tùy chọn mã hóa.
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        BitMatrix matrix = new MultiFormatWriter().encode(
                duLieuQr, BarcodeFormat.QR_CODE, 280, 280, hints);
        // image: Ảnh trong bộ nhớ dùng cho hình ảnh.
        BufferedImage image = new BufferedImage(
                matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                image.setRGB(x, y, matrix.get(x, y)
                        ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        return image;
    }

    // Nhóm dữ liệu hoặc chức năng QR mã khung dùng trong QuanLyThanhToan.
    private static class QrCodePanel extends JComponent {
        // SIZE: Lưu kích thước.
        private static final int SIZE = 280;
        // image: Ảnh trong bộ nhớ dùng cho hình ảnh.
        private final BufferedImage image;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số image: hình ảnh.
        private QrCodePanel(BufferedImage image) {
            this.image = image;
            setPreferredSize(new Dimension(SIZE, SIZE));
            setBackground(Color.WHITE);
            setOpaque(true);
        }

        // Vẽ nội dung tùy chỉnh lên thành phần giao diện.
        // Tham số graphics: ngữ cảnh vẽ.
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            // g2: Ngữ cảnh vẽ dùng để thiết lập màu, phông và vẽ nội dung.
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
            int cellSize = Math.max(1,
                    Math.min(getWidth(), getHeight()) / image.getWidth());
            int qrSize = cellSize * image.getWidth();
            int left = (getWidth() - qrSize) / 2;
            int top = (getHeight() - qrSize) / 2;
            g2.setColor(Color.WHITE);
            g2.fillRect(left, top, qrSize, qrSize);
            g2.drawImage(image, left, top, qrSize, qrSize, null);
            g2.dispose();
        }
    }

    // Tạo nội dung hóa đơn.
    // Tham số tenPhuongThuc: tên phương thức thanh toán.
    // Tham số tong: tổng.
    // Kết quả: chuỗi kết quả.
    private String taoNoiDungHoaDon(String tenPhuongThuc, BigDecimal tong) {
        // text: Bộ đệm ghép các đoạn chuỗi để tạo nội dung chữ.
        StringBuilder text = new StringBuilder();
        text.append("Ngày lập: ").append(layNgayLap().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append('\n');
        text.append("Khách hàng: ").append(txtTenKhachHang.getText())
                .append(" - ").append(maKhachHang).append('\n');
        text.append("----------------------------------------\n");
        for (entity.ThanhToan item : chiTiet) {
            text.append(item.getTenMatHang()).append(" x")
                    .append(item.getSoLuong()).append("  ")
                    .append(dinhDangTien(item.getThanhTien())).append('\n');
        }
        text.append("----------------------------------------\n");
        text.append("Tổng hàng: ").append(dinhDangTien(tinhTongHang())).append('\n');
        if (voucher != null) {
            text.append("Voucher ").append(voucher.getTenVoucher()).append(": -")
                    .append(dinhDangTien(tinhTienGiam())).append('\n');
        }
        text.append("Tổng thanh toán: ").append(dinhDangTien(tong)).append('\n');
        text.append("Phương thức: ").append(tenPhuongThuc).append('\n');
        if (tenPhuongThuc.toLowerCase(Locale.forLanguageTag("vi-VN")).contains("tiền mặt")) {
            // tienDua: Giá trị tiền đưa dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
            BigDecimal tienDua = docTienKhachDua();
            text.append("Khách đưa: ").append(dinhDangTien(tienDua)).append('\n');
            text.append("Tiền thừa: ").append(dinhDangTien(tienDua.subtract(tong))).append('\n');
        }
        text.append("\nCảm ơn quý khách!");
        return text.toString();
    }

    // Xuất hóa đơn ô văn bản.
    // Tham số noiDung: nội dung.
    // Tham số maHoaDon: mã hóa đơn.
    private void xuatHoaDonTxt(String noiDung, String maHoaDon) {
        // fchChonTep: Hộp chọn tệp bộ chọn.
        JFileChooser fchChonTep = new JFileChooser();
        fchChonTep.setDialogTitle("Lưu hóa đơn dạng TXT");
        fchChonTep.setSelectedFile(new java.io.File("HoaDon-" + maHoaDon + ".txt"));
        fchChonTep.setFileFilter(new FileNameExtensionFilter("Tệp văn bản (*.txt)", "txt"));
        if (fchChonTep.showSaveDialog(pnlNoiDung) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // file: Đường dẫn tệp dùng cho tệp.
        Path file = fchChonTep.getSelectedFile().toPath();
        if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".txt")) {
            file = file.resolveSibling(file.getFileName() + ".txt");
        }
        if (Files.exists(file)
                && JOptionPane.showConfirmDialog(
                        pnlNoiDung,
                        "Tệp đã tồn tại. Bạn có muốn ghi đè không?",
                        "Xác nhận ghi đè",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Files.write(file, noiDung.getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(
                    pnlNoiDung,
                    "Đã lưu hóa đơn:\n" + file.toAbsolutePath(),
                    "Xuất hóa đơn thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            hienLoi("Không thể xuất hóa đơn: " + e.getMessage());
        }
    }

    // Hiển thị xác nhân hóa đơn.
    // Tham số noiDung: nội dung.
    // Tham số maHoaDon: mã hóa đơn.
    private void hienXacNhanHoaDon(String noiDung, String maHoaDon) {
        // txaPreview: Vùng văn bản nhiều dòng xem trước.
        JTextArea txaPreview = new JTextArea(noiDung, 18, 38);
        txaPreview.setEditable(false);
        txaPreview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txaPreview.setCaretPosition(0);

        int action = JOptionPane.showOptionDialog(
                pnlNoiDung,
                new JScrollPane(txaPreview),
                "Thanh toán thành công",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[] { "Xuất hóa đơn", "Đóng" },
                "Xuất hóa đơn");
        if (action == 0) {
            xuatHoaDonTxt(noiDung, maHoaDon);
        }
    }

    // Tính tổng tiền hàng.
    // Kết quả: đối tượng/giá trị kiểu BigDecimal.
    private BigDecimal tinhTongHang() {
        // tong: Giá trị tổng dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        BigDecimal tong = BigDecimal.ZERO;
        for (entity.ThanhToan item : chiTiet) {
            tong = tong.add(item.getThanhTien());
        }
        return tong;
    }

    // Đọc số tiền khách đưa.
    // Kết quả: đối tượng/giá trị kiểu BigDecimal.
    private BigDecimal docTienKhachDua() {
        // raw: Đọc nội dung chữ từ ô nhập để xử lý dữ liệu thô.
        String raw = txtTienKhachDua.getText().trim().replaceAll("[.,\\s]", "");
        if (!raw.matches("\\d+")) {
            throw new NumberFormatException("Chua nhap tien khach dua.");
        }
        return new BigDecimal(raw);
    }

    // Lấy ngày giờ lập hóa đơn.
    // Kết quả: đối tượng/giá trị kiểu LocalDateTime.
    private LocalDateTime layNgayLap() {
        return ngayLap;
    }

    // Cập nhật ngày giờ lập hóa đơn.
    private void capNhatNgayLap() {
        // Định dạng dữ liệu thành chuỗi rồi hiển thị trong control bằng setText().
        txtNgayLap.setText(ngayLap.format(
                DateTimeFormatter.ofPattern("d/M/yyyy")));
    }

    // Kiểm tra điều kiện áp dụng voucher voucher.
    // Tham số candidate: voucher cần kiểm tra.
    // Kết quả: chuỗi kết quả.
    private String kiemTraDieuKienVoucher(entity.ThanhToan.Voucher candidate)
            throws SQLException {
        // dieuKien: Lưu điều kiện áp dụng voucher.
        String dieuKien = candidate.getDieuKien() == null ? "" : candidate.getDieuKien();
        java.util.regex.Matcher threshold = java.util.regex.Pattern
                .compile("(\\d{4,})").matcher(dieuKien);
        if (threshold.find()) {
            // mucToiThieu: Giá trị mục tới thiệu dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
            BigDecimal mucToiThieu = new BigDecimal(threshold.group(1));
            if (tinhTongHang().compareTo(mucToiThieu) < 0) {
                return "Đơn chưa đạt điều kiện tối thiểu "
                        + dinhDangTien(mucToiThieu) + ".";
            }
        }
        String lower = dieuKien.toLowerCase(Locale.forLanguageTag("vi-VN"));
        if ((lower.contains("thành viên") || lower.contains("thanh vien"))
                && (maKhachHang == null || !dao.laKhachHangThanhVien(maKhachHang))) {
            return "Voucher này chỉ áp dụng cho khách hàng thành viên.";
        }
        return null;
    }

    // Tính tiền giảm.
    // Kết quả: đối tượng/giá trị kiểu BigDecimal.
    private BigDecimal tinhTienGiam() {
        if (voucher == null) {
            return BigDecimal.ZERO;
        }
        return tinhTongHang()
                .multiply(voucher.getPhanTramGiam())
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    // Tính tổng tiền thanh toán.
    // Kết quả: đối tượng/giá trị kiểu BigDecimal.
    private BigDecimal tinhTongThanhToan() {
        return tinhTongHang().subtract(tinhTienGiam());
    }

    // Cập nhật tiền.
    private void capNhatTien() {
        // tongHang: Giá trị tổng tiền hàng dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        BigDecimal tongHang = tinhTongHang();
        // giam: Giá trị giảm dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        BigDecimal giam = tinhTienGiam();
        // tong: Giá trị tổng dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        BigDecimal tong = tongHang.subtract(giam);
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblTongHang.setText(dinhDangTien(tongHang));
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblGiamGia.setText(voucher == null ? "0 ₫"
                : "-" + dinhDangTien(giam));
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblTongThanhToan.setText(dinhDangTien(tong));
        entity.ThanhToan.PhuongThuc phuongThuc = phuongThucHienTai;
        boolean tienMat = phuongThuc != null
                && phuongThuc.getTenPhuongThuc().toLowerCase(Locale.forLanguageTag("vi-VN"))
                        .contains("tiền mặt");
        txtTienKhachDua.setEnabled(tienMat);
        if (!tienMat) {
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblTienThua.setText("—");
        } else {
            try {
                // tienDua: Giá trị tiền đưa dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
                BigDecimal tienDua = docTienKhachDua();
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                lblTienThua.setText(tienDua.compareTo(tong) >= 0
                        ? dinhDangTien(tienDua.subtract(tong)) : "Chưa đủ tiền");
            } catch (NumberFormatException e) {
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                lblTienThua.setText("—");
            }
        }
    }

    // Định dạng số tiền thành chuỗi hiển thị.
    // Tham số value: giá trị.
    // Kết quả: chuỗi kết quả.
    private static String dinhDangTien(BigDecimal value) {
        synchronized (MONEY) {
            return MONEY.format(value) + " ₫";
        }
    }

    // Hiển thị lỗi.
    // Tham số message: thông báo.
    private void hienLoi(String message) {
        JOptionPane.showMessageDialog(pnlNoiDung, message, "Thanh toán",
                JOptionPane.ERROR_MESSAGE);
    }

    // Mô hình dữ liệu cho bảng đơn hàng.
    private class DonHangModel extends AbstractTableModel {
        // columns: Mảng lưu các cột.
        private final String[] columns = {
                "Mã", "Sản phẩm/Dịch vụ", "Số lượng", "Đơn giá", "Thành tiền"
        };

        // Trả về số dòng dữ liệu của mô hình bảng.
        @Override
        public int getRowCount() {
            return chiTiet.size();
        }

        // Trả về số cột của mô hình bảng.
        @Override
        public int getColumnCount() {
            return columns.length;
        }

        // Trả về tiêu đề của cột theo chỉ số.
        // Tham số column: chỉ số cột.
        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        // Lấy giá trị hiển thị tại một dòng và cột của bảng.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: đối tượng/giá trị kiểu Object.
        @Override
        public Object getValueAt(int row, int column) {
            entity.ThanhToan item = chiTiet.get(row);
            switch (column) {
                case 0:
                    return item.getMaHang();
                case 1:
                    return item.getTenMatHang();
                case 2:
                    return item.getSoLuong();
                case 3:
                    return item.getDonGia();
                case 4:
                    return item.getThanhTien();
                default:
                    throw new IllegalArgumentException("Cot khong hop le.");
            }
        }

        // Quy định ô của bảng có được chỉnh sửa trực tiếp hay không.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: true/false theo điều kiện xử lý.
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2 && !dichVuTuPhieuDaThem.contains(chiTiet.get(row));
        }

        // Cập nhật giá trị được nhập vào ô của bảng.
        // Tham số value: giá trị tại mới.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        @Override
        public void setValueAt(Object value, int row, int column) {
            if (column != 2) {
                return;
            }
            try {
                int quantity = Integer.parseInt(value.toString().trim());
                chiTiet.get(row).setSoLuong(quantity);
                fireTableRowsUpdated(row, row);
                capNhatTien();
            } catch (IllegalArgumentException e) {
                hienLoi("Số lượng phải là số nguyên lớn hơn 0.");
                fireTableRowsUpdated(row, row);
            }
        }
    }

    // Quy định cách hiển thị ô hoặc thành phần tiền.
    private static class MoneyRenderer extends AlternatingRowRenderer {
        // Thiết lập thành phần dùng để vẽ một ô của bảng.
        // Tham số tblDuLieu: bảng dữ liệu.
        // Tham số value: giá trị.
        // Tham số selected: trạng thái được chọn.
        // Tham số focused: trạng thái có tiêu điểm.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: đối tượng/giá trị kiểu Component.
        @Override
        public Component getTableCellRendererComponent(
                JTable tblDuLieu,
                Object value,
                boolean selected,
                boolean focused,
                int row,
                int column) {
            Component component = super.getTableCellRendererComponent(
                    tblDuLieu, value, selected, focused, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            if (value instanceof BigDecimal) {
                setText(dinhDangTien((BigDecimal) value));
            }
            return component;
        }

    }

    // Quy định cách hiển thị ô hoặc thành phần xen kẽ dòng.
    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        // Thiết lập thành phần dùng để vẽ một ô của bảng.
        // Tham số tblDuLieu: bảng dữ liệu.
        // Tham số value: giá trị.
        // Tham số selected: trạng thái được chọn.
        // Tham số focused: trạng thái có tiêu điểm.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: đối tượng/giá trị kiểu Component.
        @Override
        public Component getTableCellRendererComponent(
                JTable tblDuLieu,
                Object value,
                boolean selected,
                boolean focused,
                int row,
                int column) {
            Component component = super.getTableCellRendererComponent(
                    tblDuLieu, value, selected, focused, row, column);
            if (!selected) {
                component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(239, 243, 247));
                component.setForeground(Color.DARK_GRAY);
            }
            return component;
        }
    }
}
