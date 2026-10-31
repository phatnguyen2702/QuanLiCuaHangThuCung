package UI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import dao.HoaDonDAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;

// Giao diện/chức năng tra cứu lịch sử hóa đơn của ứng dụng.
public class TraCuuLichSuHoaDon extends JPanel {

    // NAVY: Màu dùng cho giao diện: xanh đậm.
    private static final Color NAVY = new Color(18, 48, 82);
    // LIGHT_BLUE: Màu dùng cho giao diện: xanh dương.
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);
    // GREEN: Màu dùng cho giao diện: xanh lá.
    private static final Color GREEN = new Color(25, 157, 107);
    // BORDER: Màu dùng cho giao diện: viền.
    private static final Color BORDER = new Color(210, 220, 230);
    // DATE_TIME: Bộ định dạng ngày/giờ theo mẫu dd/MM/yyyy HH:mm để chuyển giữa dữ liệu thời gian và chuỗi.
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    // MONEY: Bộ định dạng số/tiền dùng để hiển thị dữ liệu trên giao diện.
    private static final NumberFormat MONEY =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    // dao: Đối tượng truy cập dữ liệu hóa đơn.
    private final HoaDonDAO dao = new HoaDonDAO();
    // quayLai: Hành động được gọi khi cần quay lại màn hình trước.
    private final Runnable quayLai;
    // cardLayout: Bộ bố trí cho phép chuyển giữa các màn hình trong cùng một vùng chứa.
    private final CardLayout cardLayout = new CardLayout();
    // pnlScreens: Khung chứa thành phần giao diện các màn hình.
    private final JPanel pnlScreens = new JPanel(cardLayout);
    // invoiceTableModel: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private final InvoiceTableModel invoiceTableModel = new InvoiceTableModel();
    // detailTableModel: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private final DetailTableModel detailTableModel = new DetailTableModel();
    // cmbMaHoaDon: Hộp lựa chọn mã hóa đơn.
    private final JComboBox<String> cmbMaHoaDon = new JComboBox<>();
    // danhSachMaHoaDon: Lưu danh sách mã hóa đơn; phần tử có kiểu String.
    private final List<String> danhSachMaHoaDon = new ArrayList<>();
    // tblHoaDon: Bảng hiển thị dữ liệu hóa đơn.
    private final JTable tblHoaDon = new JTable(invoiceTableModel);
    // tblChiTiet: Bảng hiển thị dữ liệu chi tiết.
    private final JTable tblChiTiet = new JTable(detailTableModel);
    // lblHoaDonHomNay: Nhãn hiển thị: 0.
    private final JLabel lblHoaDonHomNay = new JLabel("0");
    // lblTongHoaDon: Nhãn hiển thị: 0.
    private final JLabel lblTongHoaDon = new JLabel("0");
    // lblKhachHangHomNay: Nhãn hiển thị: 0.
    private final JLabel lblKhachHangHomNay = new JLabel("0");
    // lblDoanhThuHomNay: Nhãn hiển thị: 0 ₫.
    private final JLabel lblDoanhThuHomNay = new JLabel("0 ₫");
    // lblMaHoaDon: Nhãn hiển thị mã hóa đơn.
    private final JLabel lblMaHoaDon = new JLabel();
    // lblNgayLap: Nhãn hiển thị ngày giờ lập hóa đơn.
    private final JLabel lblNgayLap = new JLabel();
    // lblKhachHang: Nhãn hiển thị khách hàng.
    private final JLabel lblKhachHang = new JLabel();
    // lblTrangThai: Nhãn hiển thị: Đã thanh toán.
    private final JLabel lblTrangThai = new JLabel("Đã thanh toán");
    // lblMaKhachHang: Nhãn hiển thị mã khách hàng.
    private final JLabel lblMaKhachHang = new JLabel();
    // lblTenKhachHang: Nhãn hiển thị tên khách hàng.
    private final JLabel lblTenKhachHang = new JLabel();
    // lblSoDienThoai: Nhãn hiển thị số điện thoại.
    private final JLabel lblSoDienThoai = new JLabel();
    // lblThuCung: Nhãn hiển thị: -.
    private final JLabel lblThuCung = new JLabel("-");
    // lblTongTien: Nhãn hiển thị tổng tiền.
    private final JLabel lblTongTien = new JLabel();
    // lblPhuongThuc: Nhãn hiển thị phương thức thanh toán.
    private final JLabel lblPhuongThuc = new JLabel();
    // hoaDonDangXem: Đối tượng hóa đơn đang xem có kiểu HoaDon.
    private HoaDon hoaDonDangXem;
    // dangCapNhatGoiYMaHoaDon: Cờ cho biết đang cập nhật gợi ý mã hóa đơn.
    private boolean dangCapNhatGoiYMaHoaDon;

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    // Tham số quayLai: quay lại màn hình trước.
    public TraCuuLichSuHoaDon(Runnable quayLai) {
        this.quayLai = quayLai;
        setLayout(new BorderLayout());
        setBackground(LIGHT_BLUE);
        setBorder(BorderFactory.createEmptyBorder(14, 18, 18, 18));
        pnlScreens.setOpaque(false);
        pnlScreens.add(createListScreen(), "LIST");
        pnlScreens.add(createDetailScreen(), "DETAIL");
        add(pnlScreens, BorderLayout.CENTER);
    }

    // Hiển thị danh sách.
    public void hienDanhSach() {
        cardLayout.show(pnlScreens, "LIST");
        napDanhSach();
    }

    // Tạo màn hình danh sách.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createListScreen() {
        // pnlScreen: Khung chứa thành phần giao diện màn hình.
        JPanel pnlScreen = new JPanel(new BorderLayout(0, 14));
        pnlScreen.setOpaque(false);
        pnlScreen.add(createTitle("Tra cứu lịch sử hóa đơn"), BorderLayout.NORTH);

        // pnlContent: Khung chứa thành phần giao diện nội dung.
        JPanel pnlContent = new JPanel(new BorderLayout(0, 12));
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        pnlContent.add(createStatistics(), BorderLayout.NORTH);

        // pnlDanhSach: Khung chứa thành phần giao diện danh sách.
        JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 10));
        pnlDanhSach.setOpaque(false);
        pnlDanhSach.add(createSearchBar(), BorderLayout.NORTH);
        configureInvoiceTable();
        pnlDanhSach.add(new JScrollPane(tblHoaDon), BorderLayout.CENTER);
        // pnlActions: Khung chứa thành phần giao diện thao tác.
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        pnlActions.setOpaque(false);
        // btnQuayLai: Nút thao tác: Quay lại.
        JButton btnQuayLai = new JButton("Quay lại");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnQuayLai.addActionListener(e -> quayLai.run());
        pnlActions.add(btnQuayLai);
        pnlDanhSach.add(pnlActions, BorderLayout.SOUTH);
        pnlContent.add(pnlDanhSach, BorderLayout.CENTER);
        pnlScreen.add(pnlContent, BorderLayout.CENTER);
        return pnlScreen;
    }

    // Tạo tiêu đề.
    // Tham số text: nội dung chữ.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createTitle(String text) {
        // pnlTitlePanel: Khung chứa thành phần giao diện tiêu đề khung.
        JPanel pnlTitlePanel = new JPanel(new BorderLayout());
        pnlTitlePanel.setOpaque(false);
        // title: Nhãn hiển thị tiêu đề.
        JLabel lblTitle2 = new JLabel(text);
        lblTitle2.setForeground(NAVY);
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 25));
        pnlTitlePanel.add(lblTitle2, BorderLayout.WEST);
        return pnlTitlePanel;
    }

    // Tạo các chỉ số thống kê.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createStatistics() {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlKhung.setOpaque(false);
        pnlKhung.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        pnlKhung.add(createStatCard("Hóa đơn hôm nay", lblHoaDonHomNay));
        pnlKhung.add(createStatCard("Tổng hóa đơn", lblTongHoaDon));
        pnlKhung.add(createStatCard("Khách hàng hôm nay", lblKhachHangHomNay));
        pnlKhung.add(createStatCard("Doanh thu trong ngày", lblDoanhThuHomNay));
        return pnlKhung;
    }

    // Tạo thẻ thống kê.
    // Tham số title: tiêu đề.
    // Tham số lblValue: giá trị.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createStatCard(String title, JLabel lblValue) {
        // pnlCard: Khung chứa thành phần giao diện thẻ.
        JPanel pnlCard = new JPanel(new BorderLayout(4, 3));
        pnlCard.setBackground(new Color(245, 247, 250));
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        // lblTitleLabel: Nhãn hiển thị tiêu đề nhãn.
        JLabel lblTitleLabel = new JLabel(title);
        lblTitleLabel.setForeground(new Color(80, 80, 80));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValue.setForeground(NAVY);
        pnlCard.add(lblTitleLabel, BorderLayout.NORTH);
        pnlCard.add(lblValue, BorderLayout.CENTER);
        pnlCard.setPreferredSize(new Dimension(170, 60));
        return pnlCard;
    }

    // Tạo thanh tìm kiếm.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createSearchBar() {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new BorderLayout(8, 0));
        pnlKhung.setOpaque(false);
        // pnlSearch: Khung chứa thành phần giao diện tìm kiếm.
        JPanel pnlSearch = new JPanel(new BorderLayout(6, 0));
        pnlSearch.setOpaque(false);
        cmbMaHoaDon.setEditable(true);
        cmbMaHoaDon.setToolTipText("Nhập mã hóa đơn hoặc tên khách hàng");
        Component editor = cmbMaHoaDon.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            // txtMaHoaDon: Ô nhập/hiển thị văn bản mã hóa đơn.
            JTextField txtMaHoaDon = (JTextField) editor;
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            txtMaHoaDon.addActionListener(e -> timKiem());
            txtMaHoaDon.getDocument().addDocumentListener(new DocumentListener() {
                // Cập nhật trạng thái liên quan khi người dùng thêm ký tự vào ô văn bản.
                // Tham số e: thông tin sự kiện.
                @Override
                public void insertUpdate(DocumentEvent e) {
                    capNhatGoiYMaHoaDon();
                }

                // Cập nhật trạng thái liên quan khi người dùng xóa ký tự trong ô văn bản.
                // Tham số e: thông tin sự kiện.
                @Override
                public void removeUpdate(DocumentEvent e) {
                    capNhatGoiYMaHoaDon();
                }

                // Nhận thông báo khi thuộc tính của tài liệu văn bản thay đổi.
                // Tham số e: thông tin sự kiện.
                @Override
                public void changedUpdate(DocumentEvent e) {
                    capNhatGoiYMaHoaDon();
                }
            });
        }
        pnlSearch.add(new JLabel("Tìm hóa đơn:"), BorderLayout.WEST);
        pnlSearch.add(cmbMaHoaDon, BorderLayout.CENTER);
        // btnTimKiem: Nút thao tác: Xác nhận.
        JButton btnTimKiem = new JButton("Xác nhận");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTimKiem.addActionListener(e -> timKiem());
        pnlSearch.add(btnTimKiem, BorderLayout.EAST);
        pnlKhung.add(pnlSearch, BorderLayout.CENTER);

        // btnLamMoi: Nút thao tác: Làm mới.
        JButton btnLamMoi = new JButton("Làm mới");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLamMoi.addActionListener(e -> {
            cmbMaHoaDon.getEditor().setItem("");
            napDanhSach();
        });
        pnlKhung.add(btnLamMoi, BorderLayout.EAST);
        return pnlKhung;
    }

    // Lấy từ khóa tìm kiếm.
    // Kết quả: chuỗi kết quả.
    private String layTuKhoa() {
        Object item = cmbMaHoaDon.getEditor().getItem();
        return item == null ? "" : item.toString().trim();
    }

    // Cấu hình hóa đơn bảng.
    private void configureInvoiceTable() {
        tblHoaDon.setRowHeight(30);
        tblHoaDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHoaDon.setFillsViewportHeight(true);
        tblHoaDon.setGridColor(new Color(232, 236, 240));
        tblHoaDon.getTableHeader().setBackground(NAVY);
        tblHoaDon.getTableHeader().setForeground(Color.WHITE);
        tblHoaDon.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblHoaDon.getColumnModel().getColumn(0).setPreferredWidth(95);
        tblHoaDon.getColumnModel().getColumn(1).setPreferredWidth(155);
        tblHoaDon.getColumnModel().getColumn(2).setPreferredWidth(200);
        tblHoaDon.getColumnModel().getColumn(3).setPreferredWidth(120);
        tblHoaDon.getColumnModel().getColumn(4).setPreferredWidth(140);
        tblHoaDon.getColumnModel().getColumn(5).setPreferredWidth(135);
        tblHoaDon.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        tblHoaDon.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(
                tblHoaDon, row -> moChiTiet(invoiceTableModel.getInvoice(row))));
        tblHoaDon.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
    }

    // Tạo màn hình chi tiết.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createDetailScreen() {
        // pnlScreen: Khung chứa thành phần giao diện màn hình.
        JPanel pnlScreen = new JPanel(new BorderLayout(0, 12));
        pnlScreen.setOpaque(false);
        // title: Khung chứa thành phần giao diện tiêu đề.
        JPanel pnlTitle = createTitle("Chi tiết hóa đơn");

        // pnlContent: Khung chứa thành phần giao diện nội dung.
        JPanel pnlContent = new JPanel(new BorderLayout(0, 10));
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        pnlContent.add(createInvoiceInformation(), BorderLayout.NORTH);

        configureDetailTable();
        pnlContent.add(new JScrollPane(tblChiTiet), BorderLayout.CENTER);
        pnlContent.add(createDetailFooter(), BorderLayout.SOUTH);
        pnlScreen.add(pnlTitle, BorderLayout.NORTH);
        pnlScreen.add(pnlContent, BorderLayout.CENTER);
        return pnlScreen;
    }

    // Tạo thông tin hóa đơn.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createInvoiceInformation() {
        // pnlInformation: Khung chứa thành phần giao diện thông tin.
        JPanel pnlInformation = new JPanel(new GridLayout(1, 2, 26, 0));
        pnlInformation.setOpaque(false);
        // pnlInvoiceInfo: Khung chứa thành phần giao diện hóa đơn thông tin.
        JPanel pnlInvoiceInfo = new JPanel(new GridLayout(0, 2, 8, 6));
        pnlInvoiceInfo.setOpaque(false);
        pnlInvoiceInfo.add(sectionLabel("Thông tin hóa đơn"));
        pnlInvoiceInfo.add(new JLabel());
        addInfo(pnlInvoiceInfo, "Mã hóa đơn:", lblMaHoaDon);
        addInfo(pnlInvoiceInfo, "Ngày:", lblNgayLap);
        addInfo(pnlInvoiceInfo, "Khách hàng:", lblKhachHang);
        addInfo(pnlInvoiceInfo, "Trạng thái:", lblTrangThai);
        lblTrangThai.setForeground(GREEN);
        lblTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // pnlCustomerInfo: Khung chứa thành phần giao diện khách hàng thông tin.
        JPanel pnlCustomerInfo = new JPanel(new GridLayout(0, 2, 8, 6));
        pnlCustomerInfo.setOpaque(false);
        pnlCustomerInfo.add(sectionLabel("Thông tin khách hàng"));
        pnlCustomerInfo.add(new JLabel());
        addInfo(pnlCustomerInfo, "Mã khách hàng:", lblMaKhachHang);
        addInfo(pnlCustomerInfo, "Tên khách hàng:", lblTenKhachHang);
        addInfo(pnlCustomerInfo, "Số điện thoại:", lblSoDienThoai);
        addInfo(pnlCustomerInfo, "Thú cưng:", lblThuCung);
        addInfo(pnlCustomerInfo, "Thanh toán:", lblPhuongThuc);
        pnlInformation.add(pnlInvoiceInfo);
        pnlInformation.add(pnlCustomerInfo);
        return pnlInformation;
    }

    // Tạo nhãn tiêu đề của một phần thông tin.
    // Tham số text: nội dung chữ.
    // Kết quả: đối tượng/giá trị kiểu JLabel.
    private JLabel sectionLabel(String text) {
        // lblNhan2: Nhãn hiển thị nhãn.
        JLabel lblNhan2 = new JLabel(text);
        lblNhan2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNhan2.setForeground(NAVY);
        return lblNhan2;
    }

    // Thêm thông tin.
    // Tham số pnlKhung: khung.
    // Tham số title: tiêu đề.
    // Tham số lblValue: giá trị.
    private void addInfo(JPanel pnlKhung, String title, JLabel lblValue) {
        pnlKhung.add(new JLabel(title));
        pnlKhung.add(lblValue);
    }

    // Cấu hình chi tiết bảng.
    private void configureDetailTable() {
        tblChiTiet.setRowHeight(28);
        tblChiTiet.setFillsViewportHeight(true);
        tblChiTiet.setGridColor(new Color(190, 190, 190));
        tblChiTiet.setShowGrid(true);
        tblChiTiet.getTableHeader().setBackground(NAVY);
        tblChiTiet.getTableHeader().setForeground(Color.WHITE);
        tblChiTiet.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblChiTiet.getColumnModel().getColumn(0).setPreferredWidth(48);
        tblChiTiet.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblChiTiet.getColumnModel().getColumn(2).setPreferredWidth(115);
        tblChiTiet.getColumnModel().getColumn(3).setPreferredWidth(240);
        tblChiTiet.getColumnModel().getColumn(4).setPreferredWidth(140);
        tblChiTiet.getColumnModel().getColumn(5).setPreferredWidth(70);
        tblChiTiet.getColumnModel().getColumn(6).setPreferredWidth(120);
        tblChiTiet.getColumnModel().getColumn(7).setPreferredWidth(135);
    }

    // Tạo phần cuối của khu vực chi tiết.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createDetailFooter() {
        // pnlFooter2: Khung chứa thành phần giao diện cuối trang.
        JPanel pnlFooter2 = new JPanel(new BorderLayout());
        pnlFooter2.setOpaque(false);
        // pnlTotalPanel: Khung chứa thành phần giao diện tổng khung.
        JPanel pnlTotalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        pnlTotalPanel.setOpaque(false);
        // lblTotalTitle: Nhãn hiển thị: Tổng cộng:.
        JLabel lblTotalTitle = new JLabel("Tổng cộng:");
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 15));
        pnlTotalPanel.add(lblTotalTitle);
        pnlTotalPanel.add(lblTongTien);
        pnlFooter2.add(pnlTotalPanel, BorderLayout.NORTH);

        // pnlActions: Khung chứa thành phần giao diện thao tác.
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        pnlActions.setOpaque(false);
        // btnXuatHoaDon: Nút thao tác: Xuất hóa đơn.
        JButton btnXuatHoaDon = new JButton("Xuất hóa đơn");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnXuatHoaDon.addActionListener(e -> xuatHoaDon());
        // btnQuayLai: Nút thao tác: Quay lại.
        JButton btnQuayLai = new JButton("Quay lại");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnQuayLai.addActionListener(e -> cardLayout.show(pnlScreens, "LIST"));
        pnlActions.add(btnXuatHoaDon);
        pnlActions.add(btnQuayLai);
        pnlFooter2.add(pnlActions, BorderLayout.SOUTH);
        return pnlFooter2;
    }

    // Tìm kiểm.
    private void timKiem() {
        napDanhSach();
    }

    // Cập nhật gợi y mã hóa đơn.
    private void capNhatGoiYMaHoaDon() {
        if (dangCapNhatGoiYMaHoaDon) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (dangCapNhatGoiYMaHoaDon) {
                return;
            }

            Component editorComponent = cmbMaHoaDon.getEditor().getEditorComponent();
            if (!(editorComponent instanceof JTextField)) {
                return;
            }
            // txtEditor: Ô nhập/hiển thị văn bản bộ chỉnh sửa.
            JTextField txtEditor = (JTextField) editorComponent;
            // tuKhoa: Đọc nội dung chữ từ ô nhập để xử lý từ khóa tìm kiếm.
            String tuKhoa = txtEditor.getText();
            // goiY: Lưu gợi y; phần tử có kiểu String.
            List<String> goiY = new ArrayList<>();
            String tuKhoaChuanHoa = tuKhoa.toLowerCase(Locale.ROOT);
            for (String ma : danhSachMaHoaDon) {
                if (maCoChuaTatCaKyTu(ma, tuKhoaChuanHoa)) {
                    goiY.add(ma);
                }
            }

            int viTriConTro = txtEditor.getCaretPosition();
            dangCapNhatGoiYMaHoaDon = true;
            try {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                for (String ma : goiY) {
                    model.addElement(ma);
                }
                cmbMaHoaDon.setModel(model);
                cmbMaHoaDon.setSelectedItem(tuKhoa);
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtEditor.setText(tuKhoa);
                txtEditor.setCaretPosition(Math.min(viTriConTro, tuKhoa.length()));
            } finally {
                dangCapNhatGoiYMaHoaDon = false;
            }

            if (!tuKhoa.isEmpty() && !goiY.isEmpty()
                    && !(goiY.size() == 1 && goiY.get(0).equalsIgnoreCase(tuKhoa))) {
                cmbMaHoaDon.showPopup();
            } else if (cmbMaHoaDon.isPopupVisible()) {
                cmbMaHoaDon.hidePopup();
            }
        });
    }

    // Kiểm tra mã có chứa đủ các ký tự của từ khóa đã chuẩn hóa hay không.
    // Tham số ma: mã.
    // Tham số tuKhoaChuanHoa: từ khóa tìm kiếm chuẩn hóa.
    // Kết quả: true/false theo điều kiện xử lý.
    private boolean maCoChuaTatCaKyTu(String ma, String tuKhoaChuanHoa) {
        String maChuanHoa = ma.toLowerCase(Locale.ROOT);
        for (int i = 0; i < tuKhoaChuanHoa.length(); i++) {
            if (maChuanHoa.indexOf(tuKhoaChuanHoa.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }

    // Nạp danh sách.
    private void napDanhSach() {
        try {
            HoaDonDAO.ThongKe thongKe = dao.layThongKe();
            // tuKhoa: Lưu từ khóa tìm kiếm.
            String tuKhoa = layTuKhoa();
            // hoaDons: Lưu các hóa đơn; phần tử có kiểu HoaDon.
            List<HoaDon> hoaDons = dao.timHoaDon(tuKhoa);
            if (tuKhoa.isEmpty()) {
                danhSachMaHoaDon.clear();
                for (HoaDon hoaDon : hoaDons) {
                    danhSachMaHoaDon.add(hoaDon.getMaHoaDon());
                }
            }
            invoiceTableModel.setInvoices(hoaDons);
            capNhatThongKe(thongKe);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải danh sách hóa đơn.\n" + e.getMessage(),
                    "Lỗi tra cứu hóa đơn",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Cập nhật thống kê.
    // Tham số thongKe: thống kê.
    private void capNhatThongKe(HoaDonDAO.ThongKe thongKe) {
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblHoaDonHomNay.setText(Integer.toString(thongKe.getHoaDonHomNay()));
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblTongHoaDon.setText(Integer.toString(thongKe.getTongHoaDon()));
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblKhachHangHomNay.setText(Integer.toString(thongKe.getKhachHangHomNay()));
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblDoanhThuHomNay.setText(formatMoney(thongKe.getDoanhThuHomNay()) + " ₫");
    }

    // Mở chi tiết.
    // Tham số selected: trạng thái được chọn.
    private void moChiTiet(HoaDon selected) {
        if (selected == null) {
            return;
        }
        try {
            HoaDon fullInvoice = dao.layChiTietHoaDon(selected.getMaHoaDon());
            if (fullInvoice == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Hóa đơn không còn tồn tại trong hệ thống.",
                        "Không tìm thấy hóa đơn",
                        JOptionPane.WARNING_MESSAGE);
                napDanhSach();
                return;
            }
            hoaDonDangXem = fullInvoice;
            hienThongTinChiTiet(fullInvoice);
            cardLayout.show(pnlScreens, "DETAIL");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải chi tiết hóa đơn.\n" + e.getMessage(),
                    "Lỗi tra cứu hóa đơn",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Hiển thị thông tin chi tiết.
    // Tham số invoice: hóa đơn.
    private void hienThongTinChiTiet(HoaDon invoice) {
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblMaHoaDon.setText(invoice.getMaHoaDon());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblNgayLap.setText(formatDate(invoice.getNgayLap()));
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblKhachHang.setText(invoice.getTenKhachHang());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblMaKhachHang.setText(invoice.getMaKhachHang());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblTenKhachHang.setText(invoice.getTenKhachHang());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblSoDienThoai.setText(invoice.getSoDienThoai());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblThuCung.setText(layTenThuCung(invoice));
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblPhuongThuc.setText(invoice.getTenPhuongThuc());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblTongTien.setText(formatMoney(invoice.getTongTien()) + " ₫");
        detailTableModel.setDetails(invoice.getChiTiet());
    }

    // Lấy tên thú cưng.
    // Tham số invoice: hóa đơn.
    // Kết quả: chuỗi kết quả.
    private String layTenThuCung(HoaDon invoice) {
        // names: Lưu các tên; phần tử có kiểu String.
        List<String> names = new ArrayList<>();
        for (ChiTietHoaDon detail : invoice.getChiTiet()) {
            if ("Thú cưng".equals(detail.getLoaiHang())) {
                names.add(detail.getTenHang());
            } else if (detail.getTenThuCung() != null
                    && !names.contains(detail.getTenThuCung())) {
                names.add(detail.getTenThuCung());
            }
        }
        return names.isEmpty() ? "-" : String.join(", ", names);
    }

    // Xuất hóa đơn.
    private void xuatHoaDon() {
        if (hoaDonDangXem == null) {
            return;
        }
        // fchChonTep: Hộp chọn tệp bộ chọn.
        JFileChooser fchChonTep = new JFileChooser();
        fchChonTep.setDialogTitle("Lưu hóa đơn TXT");
        fchChonTep.setSelectedFile(new java.io.File(hoaDonDangXem.getMaHoaDon() + ".txt"));
        fchChonTep.setFileFilter(new FileNameExtensionFilter("Tệp văn bản (*.txt)", "txt"));
        if (fchChonTep.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // file: Đường dẫn tệp dùng cho tệp.
        Path file = fchChonTep.getSelectedFile().toPath();
        if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".txt")) {
            file = file.resolveSibling(file.getFileName() + ".txt");
        }
        if (Files.exists(file)
                && JOptionPane.showConfirmDialog(
                        this,
                        "Tệp đã tồn tại. Bạn có muốn ghi đè không?",
                        "Xác nhận ghi đè",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Files.write(
                    file,
                    taoNoiDungHoaDon(hoaDonDangXem).getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(
                    this,
                    "Đã lưu hóa đơn TXT:\n" + file.toAbsolutePath(),
                    "Xuất hóa đơn thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể lưu hóa đơn.\n" + e.getMessage(),
                    "Lỗi xuất hóa đơn",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Tạo nội dung hóa đơn.
    // Tham số invoice: hóa đơn.
    // Kết quả: chuỗi kết quả.
    private String taoNoiDungHoaDon(HoaDon invoice) {
        // text: Bộ đệm ghép các đoạn chuỗi để tạo nội dung chữ.
        StringBuilder text = new StringBuilder();
        text.append("PET SHOP PRO\n");
        text.append("HÓA ĐƠN THANH TOÁN\n");
        text.append("Mã hóa đơn: ").append(safe(invoice.getMaHoaDon())).append('\n');
        text.append("Ngày lập: ").append(formatDate(invoice.getNgayLap())).append('\n');
        text.append("Khách hàng: ").append(safe(invoice.getTenKhachHang())).append('\n');
        text.append("Mã khách hàng: ").append(safe(invoice.getMaKhachHang())).append('\n');
        text.append("Số điện thoại: ").append(safe(invoice.getSoDienThoai())).append('\n');
        text.append("Thú cưng: ").append(layTenThuCung(invoice)).append('\n');
        text.append("Thanh toán: ").append(safe(invoice.getTenPhuongThuc())).append('\n');
        text.append("------------------------------------------------------------\n");
        text.append("STT | Mã hàng | Loại | Tên hàng | Thú cưng | SL | Đơn giá | Thành tiền\n");
        text.append("------------------------------------------------------------\n");
        // details: Lưu các dòng chi tiết hóa đơn.
        List<ChiTietHoaDon> details = invoice.getChiTiet();
        for (int index = 0; index < details.size(); index++) {
            ChiTietHoaDon item = details.get(index);
            text.append(index + 1).append(" | ")
                    .append(safe(item.getMaHang())).append(" | ")
                    .append(safe(item.getLoaiHang())).append(" | ")
                    .append(safe(item.getTenHang())).append(" | ")
                    .append(safe(item.getTenThuCung())).append(" | ")
                    .append(item.getSoLuong()).append(" | ")
                    .append(formatMoney(item.getDonGia())).append(" ₫ | ")
                    .append(formatMoney(item.getThanhTien())).append(" ₫\n");
        }
        text.append("------------------------------------------------------------\n");
        text.append("TỔNG CỘNG: ")
                .append(formatMoney(invoice.getTongTien())).append(" ₫\n\n");
        text.append("Cảm ơn quý khách đã mua hàng!");
        return text.toString();
    }

    // Thay null bằng chuỗi rỗng trước khi hiển thị.
    // Tham số lblValue: giá trị.
    // Kết quả: chuỗi kết quả.
    private String safe(String value) {
        return value == null ? "" : value;
    }

    // Định dạng ngày giờ của hóa đơn thành chuỗi hiển thị.
    // Tham số dateTime: ngày giờ.
    // Kết quả: chuỗi kết quả.
    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME);
    }

    // Mô hình dữ liệu cho bảng hóa đơn.
    private static class InvoiceTableModel extends AbstractTableModel {
        // COLUMNS: Mảng lưu các cột.
        private static final String[] COLUMNS = {
                "MÃ", "THỜI GIAN", "KHÁCH HÀNG", "TỔNG TIỀN",
                "THANH TOÁN", "TRẠNG THÁI", ""
        };
        // invoices: Lưu danh sách hóa đơn; phần tử có kiểu HoaDon.
        private List<HoaDon> invoices = new ArrayList<>();

        // Trả về số dòng dữ liệu của mô hình bảng.
        @Override
        public int getRowCount() {
            return invoices.size();
        }

        // Trả về số cột của mô hình bảng.
        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        // Quy định ô của bảng có được chỉnh sửa trực tiếp hay không.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: true/false theo điều kiện xử lý.
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 6;
        }

        // Trả về tiêu đề của cột theo chỉ số.
        // Tham số column: chỉ số cột.
        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        // Lấy giá trị hiển thị tại một dòng và cột của bảng.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: đối tượng/giá trị kiểu Object.
        @Override
        public Object getValueAt(int row, int column) {
            HoaDon invoice = invoices.get(row);
            switch (column) {
                case 0:
                    return invoice.getMaHoaDon();
                case 1:
                    return invoice.getNgayLap() == null ? "" : invoice.getNgayLap().format(DATE_TIME);
                case 2:
                    return invoice.getTenKhachHang();
                case 3:
                    return formatMoney(invoice.getTongTien()) + " ₫";
                case 4:
                    return invoice.getTenPhuongThuc();
                case 5:
                    return "Đã thanh toán";
                case 6:
                    return "Xem thêm";
                default:
                    return "";
            }
        }

        // Lấy hóa đơn.
        // Tham số row: chỉ số dòng.
        // Kết quả: đối tượng/giá trị kiểu HoaDon.
        private HoaDon getInvoice(int row) {
            return row < 0 || row >= invoices.size() ? null : invoices.get(row);
        }

        // Cập nhật danh sách hóa đơn.
        // Tham số invoices: danh sách hóa đơn.
        private void setInvoices(List<HoaDon> invoices) {
            this.invoices = new ArrayList<>(invoices);
            fireTableDataChanged();
        }
    }

    // Mô hình dữ liệu cho bảng chi tiết.
    private static class DetailTableModel extends AbstractTableModel {
        // COLUMNS: Mảng lưu các cột.
        private static final String[] COLUMNS = {
                "STT", "Mã sản phẩm / Dịch vụ", "Loại", "Sản phẩm / Dịch vụ", "Thú cưng",
                "Số lượng", "Đơn giá", "Thành tiền"
        };
        // details: Lưu các dòng chi tiết hóa đơn.
        private List<ChiTietHoaDon> details = new ArrayList<>();

        // Trả về số dòng dữ liệu của mô hình bảng.
        @Override
        public int getRowCount() {
            return details.size();
        }

        // Trả về số cột của mô hình bảng.
        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        // Trả về tiêu đề của cột theo chỉ số.
        // Tham số column: chỉ số cột.
        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        // Lấy giá trị hiển thị tại một dòng và cột của bảng.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: đối tượng/giá trị kiểu Object.
        @Override
        public Object getValueAt(int row, int column) {
            ChiTietHoaDon detail = details.get(row);
            switch (column) {
                case 0:
                    return row + 1;
                case 1:
                    return detail.getMaHang();
                case 2:
                    return detail.getLoaiHang();
                case 3:
                    return detail.getTenHang();
                case 4:
                    return detail.getTenThuCung() == null ? "" : detail.getTenThuCung();
                case 5:
                    return detail.getSoLuong();
                case 6:
                    return formatMoney(detail.getDonGia()) + " ₫";
                case 7:
                    return formatMoney(detail.getThanhTien()) + " ₫";
                default:
                    return "";
            }
        }

        // Cập nhật các dòng chi tiết.
        // Tham số details: các dòng chi tiết.
        private void setDetails(List<ChiTietHoaDon> details) {
            this.details = new ArrayList<>(details);
            fireTableDataChanged();
        }
    }

    // Định dạng số tiền thành chuỗi hiển thị.
    // Tham số amount: số tiền.
    // Kết quả: chuỗi kết quả.
    private static String formatMoney(BigDecimal amount) {
        return MONEY.format(amount == null ? BigDecimal.ZERO : amount);
    }

    private static String formatMoney(double amount) {
        return MONEY.format(BigDecimal.valueOf(amount));
    }

    // Quy định cách hiển thị ô hoặc thành phần trạng thái.
    private static class StatusRenderer extends DefaultTableCellRenderer {
        // Thiết lập thành phần dùng để vẽ một ô của bảng.
        // Tham số tblDuLieu: bảng dữ liệu.
        // Tham số lblValue: giá trị.
        // Tham số selected: trạng thái được chọn.
        // Tham số focused: trạng thái có tiêu điểm.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: đối tượng/giá trị kiểu Component.
        @Override
        public Component getTableCellRendererComponent(
                JTable tblDuLieu, Object value, boolean selected, boolean focused, int row, int column) {
            Component component = super.getTableCellRendererComponent(
                    tblDuLieu, value, selected, focused, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(selected ? tblDuLieu.getSelectionForeground() : GREEN);
            setFont(tblDuLieu.getFont().deriveFont(Font.BOLD));
            return component;
        }
    }

    // Quy định cách hiển thị ô hoặc thành phần nút thao tác.
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        // Khởi tạo đối tượng với trạng thái ban đầu.
        private ButtonRenderer() {
            setText("Xem thêm");
            setFocusable(false);
        }

        // Thiết lập thành phần dùng để vẽ một ô của bảng.
        // Tham số tblDuLieu: bảng dữ liệu.
        // Tham số lblValue: giá trị.
        // Tham số selected: trạng thái được chọn.
        // Tham số focused: trạng thái có tiêu điểm.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: đối tượng/giá trị kiểu Component.
        @Override
        public Component getTableCellRendererComponent(
                JTable tblDuLieu, Object value, boolean selected, boolean focused, int row, int column) {
            return this;
        }
    }

    // Nhóm dữ liệu hoặc chức năng hành động trên dòng dùng trong TraCuuLichSuHoaDon.
    private interface RowAction {
        // Thực hiện hành động ứng với dòng được chọn.
        // Tham số row: chỉ số dòng.
        void perform(int row);
    }

    // Bộ chỉnh sửa và xử lý tương tác cho ô của bảng.
    private static class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        // btnThaoTac: Nút thao tác: Xem thêm.
        private final JButton btnThaoTac = new JButton("Xem thêm");
        // tblDuLieu: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
        private final JTable tblDuLieu;
        // rowAction: Đối tượng hành động trên dòng có kiểu RowAction.
        private final RowAction rowAction;
        // row: Lưu chỉ số dòng.
        private int row;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số tblDuLieu: bảng dữ liệu.
        // Tham số rowAction: hành động trên dòng.
        private ButtonEditor(JTable tblDuLieu, RowAction rowAction) {
            this.tblDuLieu = tblDuLieu;
            this.rowAction = rowAction;
            btnThaoTac.setFocusable(false);
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnThaoTac.addActionListener(new ActionListener() {
                // Xử lý hành động được phát ra từ nút, menu hoặc phím tắt.
                // Tham số event: thông tin sự kiện.
                @Override
                public void actionPerformed(ActionEvent event) {
                    fireEditingStopped();
                    ButtonEditor.this.rowAction.perform(
                            ButtonEditor.this.tblDuLieu.convertRowIndexToModel(ButtonEditor.this.row));
                }
            });
        }

        // Chuẩn bị thành phần chỉnh sửa hoặc nút thao tác trong ô của bảng.
        // Tham số tblDuLieu: bảng dữ liệu.
        // Tham số lblValue: giá trị.
        // Tham số selected: trạng thái được chọn.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: đối tượng/giá trị kiểu Component.
        @Override
        public Component getTableCellEditorComponent(
                JTable tblDuLieu, Object value, boolean selected, int row, int column) {
            this.row = row;
            return btnThaoTac;
        }

        // Trả về giá trị của bộ chỉnh sửa ô hiện tại.
        @Override
        public Object getCellEditorValue() {
            return "Xem thêm";
        }
    }
}
