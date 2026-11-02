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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import dao.HoaDonDAO;
import entity.HoaDon;

// Giao diện/chức năng tra cứu lịch sử hóa đơn của ứng dụng.
public class TraCuuLichSuHoaDonUI extends JFrame implements ActionListener, MouseListener {

    // NAVY: Màu dùng cho giao diện: xanh đậm.
    private static final Color NAVY = new Color(18, 48, 82);
    // LIGHT_BLUE: Màu dùng cho giao diện: xanh dương.
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);
    // GREEN: Màu dùng cho giao diện: xanh lá.
    private static final Color GREEN = new Color(25, 157, 107);
    // BORDER: Màu dùng cho giao diện: viền.
    private static final Color BORDER = new Color(210, 220, 230);
    // DATE_TIME: Bộ định dạng ngày/giờ theo mẫu dd/MM/yyyy HH:mm để chuyển giữa dữ
    // liệu thời gian và chuỗi.
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    // MONEY: Bộ định dạng số/tiền dùng để hiển thị dữ liệu trên giao diện.
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    // dao: Đối tượng truy cập dữ liệu hóa đơn.
    private final HoaDonDAO dao = new HoaDonDAO();
    // quayLai: Hành động được gọi khi cần quay lại màn hình trước.
    private final Runnable quayLai;
    // pnlNoiDung: Phần giao diện được TrangChu nhúng vào CardLayout.
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout());
    // cardLayout: Bộ bố trí cho phép chuyển giữa các màn hình trong cùng một vùng
    // chứa.
    private final CardLayout cardLayout = new CardLayout();
    // pnlScreens: Khung chứa thành phần giao diện các màn hình.
    private final JPanel pnlScreens = new JPanel(cardLayout);
    // chiTietHoaDonUI: Màn hình hiển thị chi tiết hóa đơn.
    private final ChiTietHoaDonUI chiTietHoaDonUI = new ChiTietHoaDonUI(
            () -> cardLayout.show(pnlScreens, "LIST"));
    // invoiceTableModel: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private final InvoiceTableModel invoiceTableModel = new InvoiceTableModel();
    // cmbMaHoaDon: Hộp lựa chọn mã hóa đơn.
    private final JComboBox<String> cmbMaHoaDon = new JComboBox<>();
    // danhSachMaHoaDon: Lưu danh sách mã hóa đơn; phần tử có kiểu String.
    private final List<String> danhSachMaHoaDon = new ArrayList<>();
    // tblHoaDon: Bảng hiển thị dữ liệu hóa đơn.
    private final JTable tblHoaDon = new JTable(invoiceTableModel);
    // lblHoaDonHomNay: Nhãn hiển thị: 0.
    private final JLabel lblHoaDonHomNay = new JLabel("0");
    // lblTongHoaDon: Nhãn hiển thị: 0.
    private final JLabel lblTongHoaDon = new JLabel("0");
    // lblKhachHangHomNay: Nhãn hiển thị: 0.
    private final JLabel lblKhachHangHomNay = new JLabel("0");
    // lblDoanhThuHomNay: Nhãn hiển thị: 0 ₫.
    private final JLabel lblDoanhThuHomNay = new JLabel("0 ₫");
    // dangCapNhatGoiYMaHoaDon: Cờ cho biết đang cập nhật gợi ý mã hóa đơn.
    private boolean dangCapNhatGoiYMaHoaDon;

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    // Tham số quayLai: quay lại màn hình trước.
    public TraCuuLichSuHoaDonUI(Runnable quayLai) {
        this.quayLai = quayLai;
        setTitle("Tra cứu lịch sử hóa đơn");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pnlNoiDung.setBackground(LIGHT_BLUE);
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(14, 18, 18, 18));
        pnlScreens.setOpaque(false);
        pnlScreens.add(createListScreen(), "LIST");
        pnlScreens.add(chiTietHoaDonUI, "DETAIL");
        pnlNoiDung.add(pnlScreens, BorderLayout.CENTER);
        setContentPane(pnlNoiDung);
    }

    /** Trả về panel nội dung để TrangChu tiếp tục hiển thị bằng CardLayout. */
    public JPanel getContentPanel() {
        return pnlNoiDung;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        switch (event.getActionCommand()) {
            case "RETURN_TO_PAYMENT":
                quayLai.run();
                break;
            case "SEARCH_INVOICES":
                timKiem();
                break;
            case "REFRESH_INVOICES":
                cmbMaHoaDon.getEditor().setItem("");
                napDanhSach();
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() != 2) {
            return;
        }
        int row = tblHoaDon.rowAtPoint(event.getPoint());
        int column = tblHoaDon.columnAtPoint(event.getPoint());
        if (row >= 0 && column >= 0 && column != 6) {
            moChiTiet(invoiceTableModel.getInvoice(tblHoaDon.convertRowIndexToModel(row)));
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
        btnQuayLai.setActionCommand("RETURN_TO_PAYMENT");
        btnQuayLai.addActionListener(this);
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
            txtMaHoaDon.setActionCommand("SEARCH_INVOICES");
            txtMaHoaDon.addActionListener(this);
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
        pnlSearch.add(new JLabel("Tìm mã hóa đơn / Khách hàng:"), BorderLayout.WEST);
        pnlSearch.add(cmbMaHoaDon, BorderLayout.CENTER);
        // btnTimKiem: Nút thao tác: Xác nhận.
        JButton btnTimKiem = new JButton("Xác nhận");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTimKiem.setActionCommand("SEARCH_INVOICES");
        btnTimKiem.addActionListener(this);
        pnlSearch.add(btnTimKiem, BorderLayout.EAST);
        pnlKhung.add(pnlSearch, BorderLayout.CENTER);

        // btnLamMoi: Nút thao tác: Làm mới.
        JButton btnLamMoi = new JButton("Làm mới");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLamMoi.setActionCommand("REFRESH_INVOICES");
        btnLamMoi.addActionListener(this);
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
        tblHoaDon.addMouseListener(this);
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
                if (ma.toLowerCase(Locale.ROOT).contains(tuKhoaChuanHoa)) {
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

    // Nạp danh sách.
    private void napDanhSach() {
        try {
            HoaDonDAO.ThongKe thongKe = dao.layThongKe();
            // tuKhoa: Lưu từ khóa tìm kiếm.
            String tuKhoa = layTuKhoa();
            // hoaDons: Dữ liệu hóa đơn tra cứu.
            List<HoaDon> hoaDons = dao.timHoaDon(tuKhoa);
            List<HoaDon> hoaDonGoiY = tuKhoa.isEmpty()
                    ? hoaDons
                    : dao.timHoaDon("");
            danhSachMaHoaDon.clear();
            for (HoaDon hoaDon : hoaDonGoiY) {
                danhSachMaHoaDon.add(hoaDon.getMaHoaDon());
            }
            invoiceTableModel.setInvoices(hoaDons, dao.layTongTienHoaDon(hoaDons));
            capNhatThongKe(thongKe);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    pnlNoiDung,
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
                        pnlNoiDung,
                        "Hóa đơn không còn tồn tại trong hệ thống.",
                        "Không tìm thấy hóa đơn",
                        JOptionPane.WARNING_MESSAGE);
                napDanhSach();
                return;
            }
            chiTietHoaDonUI.hienThi(
                    fullInvoice,
                    fullInvoice.chiTiet(),
                    fullInvoice.tinhTongTien());
            cardLayout.show(pnlScreens, "DETAIL");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    pnlNoiDung,
                    "Không thể tải chi tiết hóa đơn.\n" + e.getMessage(),
                    "Lỗi tra cứu hóa đơn",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Mô hình dữ liệu cho bảng hóa đơn.
    private static class InvoiceTableModel extends AbstractTableModel {
        // COLUMNS: Mảng lưu các cột.
        private static final String[] COLUMNS = {
                "MÃ", "THỜI GIAN", "KHÁCH HÀNG", "TỔNG TIỀN",
                "THANH TOÁN", "TRẠNG THÁI", ""
        };
        // invoices: Dữ liệu hóa đơn hiển thị trên bảng.
        private List<HoaDon> invoices = new ArrayList<>();
        private Map<String, BigDecimal> tongTienTheoMa = Map.of();

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
                    return formatMoney(tongTienTheoMa.get(invoice.getMaHoaDon())) + " ₫";
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
        // Kết quả: dữ liệu hóa đơn tại dòng được chọn.
        private HoaDon getInvoice(int row) {
            return row < 0 || row >= invoices.size() ? null : invoices.get(row);
        }

        // Cập nhật danh sách hóa đơn.
        // Tham số invoices: danh sách hóa đơn.
        private void setInvoices(List<HoaDon> invoices, Map<String, BigDecimal> tongTienTheoMa) {
            this.invoices = new ArrayList<>(invoices);
            this.tongTienTheoMa = Map.copyOf(tongTienTheoMa);
            fireTableDataChanged();
        }
    }

    // Định dạng số tiền thành chuỗi hiển thị.
    // Tham số amount: số tiền.
    // Kết quả: chuỗi kết quả.
    private static String formatMoney(BigDecimal amount) {
        return MONEY.format(amount == null ? BigDecimal.ZERO : amount);
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

    // Nhóm dữ liệu hoặc chức năng hành động trên dòng dùng trong
    // TraCuuLichSuHoaDon.
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
