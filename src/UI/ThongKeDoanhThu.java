package UI;

import dao.ThongKeDoanhThuDAO;
import dao.ThongKeDoanhThuDAO.BaoCao;
import dao.ThongKeDoanhThuDAO.CheDo;
import dao.ThongKeDoanhThuDAO.DongBieuDo;
import dao.ThongKeDoanhThuDAO.DongMatHang;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.DeflaterOutputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

// Giao diện/chức năng thống kê doanh thu của ứng dụng.
public class ThongKeDoanhThu extends JFrame implements ActionListener, MouseListener {

    // NAVY: Màu dùng cho giao diện: xanh đậm.
    private static final Color NAVY = new Color(18, 48, 82);
    // BLUE: Màu dùng cho giao diện: xanh dương.
    private static final Color BLUE = new Color(45, 125, 205);
    // BORDER: Màu dùng cho giao diện: viền.
    private static final Color BORDER = new Color(210, 220, 230);
    // MUTED: Màu dùng cho giao diện: màu chữ phụ.
    private static final Color MUTED = new Color(105, 116, 130);
    // MONEY: Bộ định dạng số/tiền dùng để hiển thị dữ liệu trên giao diện.
    private static final NumberFormat MONEY =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    // DAY_FORMAT: Bộ định dạng ngày/giờ theo mẫu dd/MM/yyyy để chuyển giữa dữ liệu thời gian và chuỗi.
    private static final DateTimeFormatter DAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // MONTH_FORMAT: Bộ định dạng ngày/giờ theo mẫu MM/yyyy để chuyển giữa dữ liệu thời gian và chuỗi.
    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MM/yyyy");

    // dao: Đối tượng truy cập dữ liệu thống kê doanh thu.
    private final ThongKeDoanhThuDAO dao = new ThongKeDoanhThuDAO();
    // pnlNoiDung: Phần giao diện được TrangChu nhúng vào CardLayout.
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
    // cmbCheDo: Hộp lựa chọn chế độ thống kê.
    private final JComboBox<CheDo> cmbCheDo = new JComboBox<>(CheDo.values());
    // lblKy: Nhãn hiển thị kỳ.
    private final JLabel lblKy = new JLabel();
    // lblTongDoanhThu: Nhãn hiển thị: —.
    private final JLabel lblTongDoanhThu = new JLabel("—", SwingConstants.CENTER);
    // lblTongHoaDon: Nhãn hiển thị: —.
    private final JLabel lblTongHoaDon = new JLabel("—", SwingConstants.CENTER);
    // lblTrungBinh: Nhãn hiển thị: —.
    private final JLabel lblTrungBinh = new JLabel("—", SwingConstants.CENTER);
    // lblTrangThai: Nhãn hiển thị:  .
    private final JLabel lblTrangThai = new JLabel(" ");
    // btnTruoc: Nút thao tác: ‹.
    private final JButton btnTruoc = new JButton("‹");
    // btnSau: Nút thao tác: ›.
    private final JButton btnSau = new JButton("›");
    // btnPdf: Nút thao tác xuất file.
    private final JButton btnPdf = new JButton("Xuất file");
    // bangModel: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private final BangMatHangModel bangModel = new BangMatHangModel();
    // tblMatHang: Bảng hiển thị dữ liệu mặt hàng.
    private final JTable tblMatHang = new JTable(bangModel);
    // pnlBieuDo: Đối tượng biểu đồ có kiểu BieuDoPanel.
    private final BieuDoPanel pnlBieuDo = new BieuDoPanel();
    // ngayThamChieu: Lưu ngày tham chiếu dưới dạng dữ liệu thời gian.
    private LocalDate ngayThamChieu = LocalDate.now();
    // baoCao: Đối tượng báo cáo có kiểu BaoCao.
    private BaoCao baoCao;

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    public ThongKeDoanhThu() {
        setTitle("Thống kê doanh thu");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pnlNoiDung.setBackground(new Color(229, 241, 252));
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(14, 18, 16, 18));
        setContentPane(pnlNoiDung);
        pnlNoiDung.add(taoTieuDe(), BorderLayout.NORTH);
        pnlNoiDung.add(taoNoiDung(), BorderLayout.CENTER);

        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbCheDo.addActionListener(this);
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTruoc.addActionListener(this);
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnSau.addActionListener(this);
        tblMatHang.addMouseListener(this);
        capNhatBaoCao();
    }

    /** Trả về panel nội dung để TrangChu tiếp tục hiển thị màn hình bằng CardLayout. */
    public JPanel getContentPanel() {
        return pnlNoiDung;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == cmbCheDo) {
            capNhatBaoCao();
        } else if (source == btnTruoc) {
            dichKy(-1);
        } else if (source == btnSau) {
            dichKy(1);
        } else if (source == btnPdf) {
            xuatPdf();
        }
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        int row = tblMatHang.rowAtPoint(event.getPoint());
        if (row >= 0) {
            tblMatHang.setRowSelectionInterval(row, row);
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

    // Làm mới dữ liệu hoặc trạng thái của màn hình.
    public void lamMoi() {
        capNhatBaoCao();
    }

    // Tạo tiêu đề.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoTieuDe() {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new BorderLayout());
        pnlKhung.setOpaque(false);
        // lblTitle2: Nhãn hiển thị: Thống kê doanh thu.
        JLabel lblTitle2 = new JLabel("Thống kê doanh thu");
        lblTitle2.setForeground(NAVY);
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 25));
        pnlKhung.add(lblTitle2, BorderLayout.WEST);
        return pnlKhung;
    }

    // Tạo nội dung.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoNoiDung() {
        // pnlContent: Khung chứa thành phần giao diện nội dung.
        JPanel pnlContent = new JPanel(new BorderLayout(0, 12));
        pnlContent.setBackground(new Color(229, 241, 252));

        // pnlToolbar: Khung chứa thành phần giao diện thanh công cụ.
        JPanel pnlToolbar = new JPanel(new BorderLayout(10, 0));
        pnlToolbar.setOpaque(false);
        // pnlKyChon: Khung chứa thành phần giao diện kỳ chọn.
        JPanel pnlKyChon = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlKyChon.setOpaque(false);
        pnlKyChon.add(new JLabel("Xem theo:"));
        cmbCheDo.setFocusable(false);
        cmbCheDo.setPreferredSize(new Dimension(100, 28));
        pnlKyChon.add(cmbCheDo);
        btnTruoc.setToolTipText("Kỳ trước");
        btnSau.setToolTipText("Kỳ tiếp theo");
        pnlKyChon.add(btnTruoc);
        pnlKyChon.add(lblKy);
        pnlKyChon.add(btnSau);
        pnlToolbar.add(pnlKyChon, BorderLayout.WEST);

        // btnPdf: Nút thao tác: Xuất file.
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnPdf.addActionListener(this);
        pnlToolbar.add(btnPdf, BorderLayout.EAST);

        // pnlStats: Khung chứa thành phần giao diện thống kê.
        JPanel pnlStats = new JPanel(new java.awt.GridLayout(1, 3, 12, 0));
        pnlStats.setOpaque(false);
        pnlStats.add(taoTheChiSo("Tổng doanh thu", lblTongDoanhThu));
        pnlStats.add(taoTheChiSo("Tổng số hóa đơn", lblTongHoaDon));
        pnlStats.add(taoTheChiSo("Doanh thu trung bình/HD", lblTrungBinh));
        // pnlHeaderContent: Khung chứa thành phần giao diện đầu trang nội dung.
        JPanel pnlHeaderContent = new JPanel(new BorderLayout(0, 12));
        pnlHeaderContent.setOpaque(false);
        pnlHeaderContent.add(pnlToolbar, BorderLayout.NORTH);
        pnlHeaderContent.add(pnlStats, BorderLayout.CENTER);

        // pnlChartCard: Khung chứa thành phần giao diện biểu đồ thẻ.
        JPanel pnlChartCard = taoTheNoiDung("Biểu đồ doanh thu");
        pnlChartCard.add(pnlBieuDo, BorderLayout.CENTER);
        // pnlTableCard: Khung chứa thành phần giao diện bảng thẻ.
        JPanel pnlTableCard = taoTheNoiDung("Doanh thu theo mặt hàng");
        cauHinhBang();
        // scrCuon2: Khung cuộn khung cuộn.
        JScrollPane scrCuon2 = new JScrollPane(tblMatHang);
        scrCuon2.setBorder(BorderFactory.createLineBorder(BORDER));
        pnlTableCard.add(scrCuon2, BorderLayout.CENTER);

        // pnlResults: Khung chứa thành phần giao diện kết quả.
        JPanel pnlResults = new JPanel(new java.awt.GridLayout(1, 2, 14, 0));
        pnlResults.setOpaque(false);
        pnlResults.add(pnlChartCard);
        pnlResults.add(pnlTableCard);

        // pnlCenter2: Khung chứa thành phần giao diện trung tâm.
        JPanel pnlCenter2 = new JPanel(new BorderLayout(0, 10));
        pnlCenter2.setOpaque(false);
        pnlCenter2.add(pnlHeaderContent, BorderLayout.NORTH);
        pnlCenter2.add(pnlResults, BorderLayout.CENTER);

        lblTrangThai.setForeground(MUTED);
        pnlContent.add(pnlCenter2, BorderLayout.CENTER);
        pnlContent.add(lblTrangThai, BorderLayout.SOUTH);
        return pnlContent;
    }

    // Tạo thẻ chi số.
    // Tham số tieuDe: tiêu đề.
    // Tham số lblGiaTri2: giá trị.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoTheChiSo(String tieuDe, JLabel lblGiaTri2) {
        // pnlCard: Khung chứa thành phần giao diện thẻ.
        JPanel pnlCard = new JPanel(new BorderLayout(4, 4));
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        // lblNhan2: Nhãn hiển thị nhãn.
        JLabel lblNhan2 = new JLabel(tieuDe, SwingConstants.CENTER);
        lblNhan2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGiaTri2.setForeground(Color.BLACK);
        lblGiaTri2.setFont(new Font("Segoe UI", Font.BOLD, 21));
        pnlCard.add(lblNhan2, BorderLayout.NORTH);
        pnlCard.add(lblGiaTri2, BorderLayout.CENTER);
        pnlCard.setPreferredSize(new Dimension(180, 62));
        return pnlCard;
    }

    // Tạo thẻ nội dung.
    // Tham số tieuDe: tiêu đề.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoTheNoiDung(String tieuDe) {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new BorderLayout(0, 8));
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(9, 10, 10, 10)));
        // lblNhan2: Nhãn hiển thị nhãn.
        JLabel lblNhan2 = new JLabel(tieuDe);
        lblNhan2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlKhung.add(lblNhan2, BorderLayout.NORTH);
        return pnlKhung;
    }

    // Thiết lập cột, kích thước và cách hiển thị dữ liệu của bảng.
    private void cauHinhBang() {
        tblMatHang.setRowHeight(25);
        tblMatHang.setFillsViewportHeight(true);
        tblMatHang.setAutoCreateRowSorter(true);
        tblMatHang.getTableHeader().setReorderingAllowed(false);
        tblMatHang.getTableHeader().setBackground(new Color(245, 247, 250));
        tblMatHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tblMatHang.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            // Thiết lập thành phần dùng để vẽ một ô của bảng.
            // Tham số tblDuLieu: bảng dữ liệu.
            // Tham số value: giá trị.
            // Tham số selected: trạng thái được chọn.
            // Tham số focus: trạng thái có tiêu điểm.
            // Tham số row: chỉ số dòng.
            // Tham số col: chỉ số cột.
            // Kết quả: đối tượng/giá trị kiểu java.awt.Component.
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable tblDuLieu, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(tblDuLieu, value, selected, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                setHorizontalAlignment(col == 1 || col == 4
                        ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return this;
            }
        });
    }

    // Cập nhật báo cáo.
    private void capNhatBaoCao() {
        CheDo cheDo = (CheDo) cmbCheDo.getSelectedItem();
        if (cheDo == null) {
            return;
        }
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblKy.setText(nhanKy(cheDo, ngayThamChieu));
        btnSau.setEnabled(laKyTruocHienTai(cheDo, ngayThamChieu));
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblTrangThai.setText("Đang tải số liệu...");
        try {
            baoCao = dao.layBaoCao(cheDo, ngayThamChieu);
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblTongDoanhThu.setText(dinhDangTien(baoCao.getTongDoanhThu()) + " ₫");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblTongHoaDon.setText(String.valueOf(baoCao.getTongHoaDon()));
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblTrungBinh.setText(dinhDangTien(baoCao.getDoanhThuTrungBinh()) + " ₫");
            bangModel.setBaoCao(baoCao);
            pnlBieuDo.setBaoCao(baoCao, cheDo);
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblTrangThai.setText(" ");
        } catch (SQLException ex) {
            baoCao = null;
            bangModel.setBaoCao(null);
            pnlBieuDo.setBaoCao(null, cheDo);
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblTongDoanhThu.setText("—");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblTongHoaDon.setText("—");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblTrungBinh.setText("—");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblTrangThai.setText("Không tải được thống kê doanh thu.");
            JOptionPane.showMessageDialog(pnlNoiDung,
                    "Không tải được thống kê doanh thu.\n" + ex.getMessage(),
                    "Lỗi thống kê", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Dịch chuyển kỳ.
    // Tham số soKy: số kỳ cần dịch chuyển.
    private void dichKy(int soKy) {
        CheDo cheDo = (CheDo) cmbCheDo.getSelectedItem();
        if (cheDo == null) {
            return;
        }
        switch (cheDo) {
            case NGAY:
                ngayThamChieu = ngayThamChieu.plusDays(soKy);
                break;
            case THANG:
                ngayThamChieu = ngayThamChieu.plusMonths(soKy);
                break;
            case NAM:
                ngayThamChieu = ngayThamChieu.plusYears(soKy);
                break;
            default:
                throw new IllegalStateException("Kiểu thống kê không được hỗ trợ.");
        }
        capNhatBaoCao();
    }

    // Kiểm tra kỳ thống kê có nằm trước kỳ hiện tại hay không.
    // Tham số cheDo: chế độ thống kê.
    // Tham số ngay: ngày.
    // Kết quả: true/false theo điều kiện xử lý.
    private boolean laKyTruocHienTai(CheDo cheDo, LocalDate ngay) {
        switch (cheDo) {
            case NGAY:
                return ngay.isBefore(LocalDate.now());
            case THANG:
                return YearMonth.from(ngay).isBefore(YearMonth.now());
            case NAM:
                return ngay.getYear() < LocalDate.now().getYear();
            default:
                return false;
        }
    }

    // Tạo nhãn cho kỳ thống kê được chọn.
    // Tham số cheDo: chế độ thống kê.
    // Tham số ngay: ngày.
    // Kết quả: chuỗi kết quả.
    private String nhanKy(CheDo cheDo, LocalDate ngay) {
        switch (cheDo) {
            case NGAY:
                return ngay.format(DAY_FORMAT);
            case THANG:
                return ngay.format(MONTH_FORMAT);
            case NAM:
                return Integer.toString(ngay.getYear());
            default:
                throw new IllegalStateException("Kiểu thống kê không được hỗ trợ.");
        }
    }

    // Định dạng số tiền thành chuỗi hiển thị.
    // Tham số amount: số tiền.
    // Kết quả: chuỗi kết quả.
    private String dinhDangTien(BigDecimal amount) {
        return MONEY.format(amount);
    }

    // Tính tỷ trong.
    // Tham số doanhThu: doanh thu.
    // Kết quả: đối tượng/giá trị kiểu BigDecimal.
    private BigDecimal tinhTyTrong(BigDecimal doanhThu) {
        if (baoCao == null || baoCao.getTongDoanhThu().signum() == 0) {
            return BigDecimal.ZERO;
        }
        return doanhThu.multiply(BigDecimal.valueOf(100))
                .divide(baoCao.getTongDoanhThu(), 1, RoundingMode.HALF_UP);
    }

    // Xuất PDF.
    private void xuatPdf() {
        if (baoCao == null) {
            JOptionPane.showMessageDialog(pnlNoiDung, "Chưa có số liệu để xuất.",
                    "Xuất thống kê", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CheDo cheDo = (CheDo) cmbCheDo.getSelectedItem();
        // fchChonTep: Hộp chọn tệp bộ chọn.
        JFileChooser fchChonTep = new JFileChooser();
        fchChonTep.setDialogTitle("Lưu thống kê doanh thu PDF");
        fchChonTep.setSelectedFile(new java.io.File("ThongKeDoanhThu-" + nhanKy(cheDo, ngayThamChieu)
                .replace('/', '-') + ".pdf"));
        fchChonTep.setFileFilter(new FileNameExtensionFilter("Tệp PDF (*.pdf)", "pdf"));
        if (fchChonTep.showSaveDialog(pnlNoiDung) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        // file: Đường dẫn tệp dùng cho tệp.
        Path file = fchChonTep.getSelectedFile().toPath();
        if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            file = file.resolveSibling(file.getFileName() + ".pdf");
        }
        try {
            Files.write(file, taoPdf());
            JOptionPane.showMessageDialog(pnlNoiDung,
                    "Đã xuất thống kê:\n" + file.toAbsolutePath(),
                    "Xuất thống kê", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(pnlNoiDung,
                    "Không thể xuất file.\n" + ex.getMessage(),
                    "Lỗi xuất", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Tạo PDF.
    // Kết quả: đối tượng/giá trị kiểu byte[].
    private byte[] taoPdf() throws IOException {
        // width: Lưu chiều rộng.
        final int width = 842;
        // height: Lưu chiều cao.
        final int height = 595;
        final int firstPageRows = 11;
        final int followingPageRows = 17;
        int extraRows = Math.max(0, baoCao.getMatHangs().size() - firstPageRows);
        int pageCount = 1 + (extraRows + followingPageRows - 1) / followingPageRows;
        // objects: Lưu các đối tượng; phần tử có kiểu byte[].
        List<byte[]> objects = new ArrayList<>();
        objects.add(null);
        objects.add(null);
        // pageReferences: Bộ đệm ghép các đoạn chuỗi để tạo trang các tham chiếu.
        StringBuilder pageReferences = new StringBuilder();
        CheDo cheDo = (CheDo) cmbCheDo.getSelectedItem();

        for (int page = 0; page < pageCount; page++) {
            int pageObject = 3 + page * 3;
            int contentObject = pageObject + 1;
            int imageObject = pageObject + 2;
            pageReferences.append(pageObject).append(" 0 R ");
            // image: Ảnh trong bộ nhớ dùng cho hình ảnh.
            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                    width * 2, height * 2, java.awt.image.BufferedImage.TYPE_INT_RGB);
            // graphics: Ngữ cảnh vẽ dùng để thiết lập màu, phông và vẽ nội dung.
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.scale(2, 2);
                vePdfTrang(graphics, page, cheDo, firstPageRows, followingPageRows);
            } finally {
                graphics.dispose();
            }
            // compressedImage: Mảng lưu đã nén ảnh.
            byte[] compressedImage = nenAnhRgb(image);
            // pnlContent: Mảng lưu nội dung.
            byte[] content = ascii("q\n" + width + " 0 0 " + height + " 0 0 cm\n/Im0 Do\nQ\n");
            objects.add(ascii(pageObject + " 0 obj\n"
                    + "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 "
                    + width + " " + height + "] "
                    + "/Resources << /XObject << /Im0 " + imageObject + " 0 R >> >> "
                    + "/Contents " + contentObject + " 0 R >>\nendobj\n"));
            objects.add(streamObject(contentObject, "<<", content));
            objects.add(streamObject(imageObject,
                    "<< /Type /XObject /Subtype /Image /Width " + image.getWidth()
                            + " /Height " + image.getHeight()
                            + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /FlateDecode",
                    compressedImage));
        }

        objects.set(0, ascii("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"));
        objects.set(1, ascii("2 0 obj\n<< /Type /Pages /Kids ["
                + pageReferences + "] /Count " + pageCount + " >>\nendobj\n"));
        return assemblePdf(objects);
    }

    // Vẽ PDF trạng.
    // Tham số g: ngữ cảnh vẽ.
    // Tham số page: trang.
    // Tham số cheDo: chế độ thống kê.
    // Tham số firstPageRows: đầu tiên trang các dòng.
    // Tham số followingPageRows: tiếp theo trang các dòng.
    private void vePdfTrang(
            Graphics2D g, int page, CheDo cheDo, int firstPageRows, int followingPageRows) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 842, 595);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(NAVY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 23));
        g.drawString("PET SHOP PRO - THỐNG KÊ DOANH THU", 36, 38);
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.drawString("Kỳ báo cáo: " + nhanKy(cheDo, ngayThamChieu), 36, 61);
        g.drawString("Tổng doanh thu: " + dinhDangTien(baoCao.getTongDoanhThu()) + " đ", 36, 88);
        g.drawString("Tổng số hóa đơn: " + baoCao.getTongHoaDon(), 310, 88);
        g.drawString("Doanh thu trung bình/HD: "
                + dinhDangTien(baoCao.getDoanhThuTrungBinh()) + " đ", 540, 88);

        int first;
        // count: Lưu số phần tử.
        int count;
        // y: Lưu tọa độ dọc.
        int y;
        if (page == 0) {
            veBieuDoPdf(g, 36, 111, 770, 140, cheDo);
            first = 0;
            count = firstPageRows;
            y = 270;
        } else {
            first = firstPageRows + (page - 1) * followingPageRows;
            count = followingPageRows;
            y = 126;
        }
        g.setColor(new Color(230, 237, 245));
        g.fillRect(35, y, 772, 26);
        g.setColor(NAVY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g.drawString("STT", 44, y + 17);
        g.drawString("Dịch vụ/Sản phẩm", 90, y + 17);
        g.drawString("Số lượng", 480, y + 17);
        g.drawString("Doanh thu", 585, y + 17);
        g.drawString("Tỷ trọng (%)", 705, y + 17);
        int last = Math.min(first + count, baoCao.getMatHangs().size());
        for (int i = first; i < last; i++) {
            int rowY = y + 26 + (i - first) * 24;
            DongMatHang dong = baoCao.getMatHangs().get(i);
            g.setColor(i % 2 == 0 ? Color.WHITE : new Color(247, 249, 251));
            g.fillRect(35, rowY, 772, 24);
            g.setColor(BORDER);
            g.drawLine(35, rowY + 23, 807, rowY + 23);
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString(Integer.toString(i + 1), 46, rowY + 16);
            g.drawString(catChuoi(dong.getTenMatHang(), 55), 90, rowY + 16);
            g.drawString(Integer.toString(dong.getSoLuong()), 492, rowY + 16);
            g.drawString(dinhDangTien(dong.getDoanhThu()), 585, rowY + 16);
            g.drawString(tinhTyTrong(dong.getDoanhThu()).toPlainString(), 720, rowY + 16);
        }
        g.setColor(MUTED);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g.drawString("Trang " + (page + 1), 770, 578);
    }

    // Vẽ biểu đồ PDF.
    // Tham số g: ngữ cảnh vẽ.
    // Tham số x: tọa độ ngang.
    // Tham số y: tọa độ dọc.
    // Tham số width: chiều rộng.
    // Tham số height: chiều cao.
    // Tham số cheDo: chế độ thống kê.
    private void veBieuDoPdf(Graphics2D g, int x, int y, int width, int height, CheDo cheDo) {
        g.setColor(new Color(247, 249, 251));
        g.fillRoundRect(x, y, width, height, 8, 8);
        g.setColor(BORDER);
        g.drawRoundRect(x, y, width, height, 8, 8);
        g.setColor(NAVY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g.drawString("Biểu đồ doanh thu", x + 12, y + 19);
        if (baoCao.getBieuDo().isEmpty()) {
            return;
        }
        // max: Giá trị tối đa dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        BigDecimal max = BigDecimal.ZERO;
        for (DongBieuDo row : baoCao.getBieuDo()) {
            max = max.max(row.getDoanhThu());
        }
        if (max.signum() == 0) {
            max = BigDecimal.ONE;
        }
        int plotX = x + 46;
        int plotY = y + 30;
        int plotWidth = width - 62;
        int plotHeight = height - 54;
        for (int line = 0; line <= 3; line++) {
            int gridY = plotY + plotHeight - plotHeight * line / 3;
            g.setColor(new Color(218, 224, 230));
            g.drawLine(plotX, gridY, plotX + plotWidth, gridY);
        }
        // points: Lưu các điểm; phần tử có kiểu DongBieuDo.
        List<DongBieuDo> points = baoCao.getBieuDo();
        int gap = points.size() >= 20 ? 2 : 5;
        int barWidth = Math.max(2, (plotWidth - gap * (points.size() - 1)) / points.size());
        for (int i = 0; i < points.size(); i++) {
            DongBieuDo point = points.get(i);
            int barHeight = point.getDoanhThu().multiply(BigDecimal.valueOf(plotHeight))
                    .divide(max, 0, RoundingMode.HALF_UP).intValue();
            int barX = plotX + i * (barWidth + gap);
            g.setColor(BLUE);
            g.fillRect(barX, plotY + plotHeight - barHeight, barWidth, barHeight);
            if (points.size() <= 12 || i % 4 == 0) {
                g.setColor(MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                g.drawString(pnlBieuDo.nhanMoc(cheDo, point.getThuTu()),
                        barX, plotY + plotHeight + 14);
            }
        }
    }

    // Cắt chuỗi.
    // Tham số text: nội dung chữ.
    // Tham số maxLength: tối đa độ dài.
    // Kết quả: chuỗi kết quả.
    private String catChuoi(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }

    // Nén dữ liệu màu RGB của ảnh để nhúng vào PDF.
    // Tham số image: hình ảnh.
    // Kết quả: đối tượng/giá trị kiểu byte[].
    private byte[] nenAnhRgb(java.awt.image.BufferedImage image) throws IOException {
        // compressed: Luồng ghi dữ liệu dùng khi tạo hoặc đóng gói tệp xuất.
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressed)) {
            // pixels: Mảng lưu các điểm ảnh.
            int[] pixels = new int[image.getWidth()];
            // row: Mảng lưu chỉ số dòng.
            byte[] row = new byte[image.getWidth() * 3];
            for (int y = 0; y < image.getHeight(); y++) {
                image.getRGB(0, y, image.getWidth(), 1, pixels, 0, image.getWidth());
                for (int x = 0; x < pixels.length; x++) {
                    int rgb = pixels[x];
                    int offset = x * 3;
                    row[offset] = (byte) (rgb >> 16);
                    row[offset + 1] = (byte) (rgb >> 8);
                    row[offset + 2] = (byte) rgb;
                }
                deflater.write(row);
            }
        }
        return compressed.toByteArray();
    }

    // Tạo đối tượng stream trong cấu trúc tệp PDF.
    // Tham số number: số.
    // Tham số dictionary: từ điển.
    // Tham số data: dữ liệu.
    // Kết quả: đối tượng/giá trị kiểu byte[].
    private byte[] streamObject(int number, String dictionary, byte[] data) throws IOException {
        // object: Luồng ghi dữ liệu dùng khi tạo hoặc đóng gói tệp xuất.
        ByteArrayOutputStream object = new ByteArrayOutputStream();
        object.write(ascii(number + " 0 obj\n" + dictionary
                + " /Length " + data.length + " >>\nstream\n"));
        object.write(data);
        object.write(ascii("\nendstream\nendobj\n"));
        return object.toByteArray();
    }

    // Ghép các đối tượng và bảng tham chiếu thành tệp PDF hoàn chỉnh.
    // Tham số objects: các đối tượng.
    // Kết quả: đối tượng/giá trị kiểu byte[].
    private byte[] assemblePdf(List<byte[]> objects) throws IOException {
        // pdf: Luồng ghi dữ liệu dùng khi tạo hoặc đóng gói tệp xuất.
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        pdf.write(ascii("%PDF-1.4\n"));
        // offsets: Lưu các vị trí byte; phần tử có kiểu Integer.
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (byte[] object : objects) {
            offsets.add(pdf.size());
            pdf.write(object);
        }
        int xrefOffset = pdf.size();
        pdf.write(ascii("xref\n0 " + offsets.size() + "\n0000000000 65535 f \n"));
        for (int i = 1; i < offsets.size(); i++) {
            pdf.write(ascii(String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(i))));
        }
        pdf.write(ascii("trailer\n<< /Size " + offsets.size()
                + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n"));
        return pdf.toByteArray();
    }

    // Chuyển chuỗi nội dung PDF thành mảng byte ASCII.
    // Tham số text: nội dung chữ.
    // Kết quả: đối tượng/giá trị kiểu byte[].
    private byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.ISO_8859_1);
    }

    // Khung giao diện dành cho biểu đồ khung.
    private final class BieuDoPanel extends JPanel {
        // data: Đối tượng dữ liệu có kiểu BaoCao.
        private BaoCao data;
        // cheDo: Đối tượng chế độ thống kê có kiểu CheDo.
        private CheDo cheDo = CheDo.NGAY;

        // Khởi tạo đối tượng với trạng thái ban đầu.
        private BieuDoPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(380, 270));
        }

        // Cập nhật báo cáo.
        // Tham số data: dữ liệu.
        // Tham số cheDo: chế độ thống kê.
        private void setBaoCao(BaoCao data, CheDo cheDo) {
            this.data = data;
            this.cheDo = cheDo;
            repaint();
        }

        // Vẽ nội dung tùy chỉnh lên thành phần giao diện.
        // Tham số graphics: ngữ cảnh vẽ.
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (data == null || data.getBieuDo().isEmpty()) {
                return;
            }
            // g: Ngữ cảnh vẽ dùng để thiết lập màu, phông và vẽ nội dung.
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int left = 43;
                int right = 12;
                int top = 15;
                int bottom = 42;
                int chartWidth = Math.max(1, getWidth() - left - right);
                int chartHeight = Math.max(1, getHeight() - top - bottom);
                // max: Giá trị tối đa dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
                BigDecimal max = BigDecimal.ZERO;
                for (DongBieuDo row : data.getBieuDo()) {
                    max = max.max(row.getDoanhThu());
                }
                if (max.signum() == 0) {
                    max = BigDecimal.ONE;
                }

                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                for (int line = 0; line <= 4; line++) {
                    // y: Lưu tọa độ dọc.
                    int y = top + chartHeight - chartHeight * line / 4;
                    // value: Giá trị giá trị dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
                    BigDecimal value = max.multiply(BigDecimal.valueOf(line))
                            .divide(BigDecimal.valueOf(4), 0, RoundingMode.HALF_UP);
                    g.setColor(new Color(226, 230, 235));
                    g.drawLine(left, y, left + chartWidth, y);
                    g.setColor(MUTED);
                    g.drawString(catChuoi(dinhDangTien(value), 9), 1, y + 4);
                }
                // count: Lưu số phần tử.
                int count = data.getBieuDo().size();
                int gap = count >= 20 ? 2 : 5;
                int barWidth = Math.max(2, (chartWidth - gap * (count - 1)) / count);
                for (int i = 0; i < count; i++) {
                    DongBieuDo row = data.getBieuDo().get(i);
                    int barHeight = row.getDoanhThu().multiply(BigDecimal.valueOf(chartHeight))
                            .divide(max, 0, RoundingMode.HALF_UP).intValue();
                    // x: Lưu tọa độ ngang.
                    int x = left + i * (barWidth + gap);
                    // y: Lưu tọa độ dọc.
                    int y = top + chartHeight - barHeight;
                    g.setColor(BLUE);
                    g.fillRect(x, y, barWidth, barHeight);
                    if (hienNhanMoc(i, count)) {
                        g.setColor(MUTED);
                        g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                        // lblNhan2: Lưu nhãn.
                        String label = nhanMoc(cheDo, row.getThuTu());
                        int textX = x + (barWidth - g.getFontMetrics().stringWidth(label)) / 2;
                        g.drawString(label, textX, top + chartHeight + 17);
                    }
                }
                g.setColor(new Color(110, 120, 130));
                g.setStroke(new BasicStroke(1f));
                g.drawLine(left, top, left, top + chartHeight);
                g.drawLine(left, top + chartHeight, left + chartWidth, top + chartHeight);
            } finally {
                g.dispose();
            }
        }

        // Hiển thị nhân mốc.
        // Tham số index: chỉ số phần tử.
        // Tham số count: số phần tử.
        // Kết quả: true/false theo điều kiện xử lý.
        private boolean hienNhanMoc(int index, int count) {
            if (count <= 12) {
                return true;
            }
            if (cheDo == CheDo.NGAY) {
                return index % 3 == 0;
            }
            return index % 5 == 0 || index == count - 1;
        }

        // Tạo nhãn của mốc trên biểu đồ.
        // Tham số cheDo: chế độ thống kê.
        // Tham số moc: mốc.
        // Kết quả: chuỗi kết quả.
        private String nhanMoc(CheDo cheDo, int moc) {
            if (cheDo == CheDo.NGAY) {
                return String.format(Locale.ROOT, "%02dh", moc);
            }
            if (cheDo == CheDo.THANG) {
                return Integer.toString(moc);
            }
            return new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" }[moc - 1];
        }
    }

    // Mô hình dữ liệu cho bảng bảng mặt hàng.
    private final class BangMatHangModel extends AbstractTableModel {
        // cot: Mảng lưu cột.
        private final String[] cot = { "STT", "Danh mục / Sản phẩm", "Số lượng",
                "Doanh thu", "Tỷ trọng (%)" };
        // rows: Danh sách chứa các phần tử kiểu DongMatHang.
        private List<DongMatHang> rows = new ArrayList<>();

        // Cập nhật báo cáo.
        // Tham số baoCao: báo cáo.
        private void setBaoCao(BaoCao baoCao) {
            rows = baoCao == null ? new ArrayList<>() : baoCao.getMatHangs();
            fireTableDataChanged();
        }

        // Trả về số dòng dữ liệu của mô hình bảng.
        @Override
        public int getRowCount() {
            return rows.size();
        }

        // Trả về số cột của mô hình bảng.
        @Override
        public int getColumnCount() {
            return cot.length;
        }

        // Trả về tiêu đề của cột theo chỉ số.
        // Tham số column: chỉ số cột.
        @Override
        public String getColumnName(int column) {
            return cot[column];
        }

        // Lấy giá trị hiển thị tại một dòng và cột của bảng.
        // Tham số rowIndex: chỉ số dòng.
        // Tham số columnIndex: chỉ số cột.
        // Kết quả: đối tượng/giá trị kiểu Object.
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DongMatHang row = rows.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return rowIndex + 1;
                case 1:
                    return row.getTenMatHang();
                case 2:
                    return row.getSoLuong();
                case 3:
                    return dinhDangTien(row.getDoanhThu()) + " ₫";
                case 4:
                    return tinhTyTrong(row.getDoanhThu()).toPlainString();
                default:
                    throw new IndexOutOfBoundsException("Cột không hợp lệ: " + columnIndex);
            }
        }
    }
}
