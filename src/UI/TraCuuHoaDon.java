package UI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
import java.util.zip.DeflaterOutputStream;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import dao.HoaDonDAO;
import entity.HoaDon;

public class TraCuuHoaDon extends JPanel {

    private static final Color NAVY = new Color(18, 48, 82);
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);
    private static final Color GREEN = new Color(25, 157, 107);
    private static final Color BORDER = new Color(210, 220, 230);
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat MONEY =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private static final int PDF_PAGE_WIDTH = 595;
    private static final int PDF_PAGE_HEIGHT = 842;
    private static final int PDF_IMAGE_SCALE = 2;
    private static final int ITEMS_PER_PAGE = 18;

    private final HoaDonDAO dao = new HoaDonDAO();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel screens = new JPanel(cardLayout);
    private final InvoiceTableModel invoiceTableModel = new InvoiceTableModel();
    private final DetailTableModel detailTableModel = new DetailTableModel();
    private final JTextField txtTimKiem = new JTextField();
    private final JTable tblHoaDon = new JTable(invoiceTableModel);
    private final JTable tblChiTiet = new JTable(detailTableModel);
    private final JLabel lblHoaDonHomNay = new JLabel("0");
    private final JLabel lblTongHoaDon = new JLabel("0");
    private final JLabel lblKhachHangHomNay = new JLabel("0");
    private final JLabel lblDoanhThuHomNay = new JLabel("0 ₫");
    private final JLabel lblMaHoaDon = new JLabel();
    private final JLabel lblNgayLap = new JLabel();
    private final JLabel lblKhachHang = new JLabel();
    private final JLabel lblTrangThai = new JLabel("Đã thanh toán");
    private final JLabel lblMaKhachHang = new JLabel();
    private final JLabel lblTenKhachHang = new JLabel();
    private final JLabel lblSoDienThoai = new JLabel();
    private final JLabel lblThuCung = new JLabel("-");
    private final JLabel lblTongTien = new JLabel();
    private final JLabel lblPhuongThuc = new JLabel();
    private HoaDon hoaDonDangXem;

    public TraCuuHoaDon() {
        setLayout(new BorderLayout());
        setBackground(LIGHT_BLUE);
        setBorder(BorderFactory.createEmptyBorder(14, 18, 18, 18));
        screens.add(createListScreen(), "LIST");
        screens.add(createDetailScreen(), "DETAIL");
        add(screens, BorderLayout.CENTER);
    }

    public void hienDanhSach() {
        cardLayout.show(screens, "LIST");
        napDanhSach();
    }

    private JPanel createListScreen() {
        JPanel screen = new JPanel(new BorderLayout(0, 14));
        screen.setOpaque(false);
        screen.add(createTitle("Tra cứu hóa đơn"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        content.add(createStatistics(), BorderLayout.NORTH);

        JPanel list = new JPanel(new BorderLayout(0, 10));
        list.setOpaque(false);
        list.add(createSearchBar(), BorderLayout.NORTH);
        configureInvoiceTable();
        list.add(new JScrollPane(tblHoaDon), BorderLayout.CENTER);
        content.add(list, BorderLayout.CENTER);
        screen.add(content, BorderLayout.CENTER);
        return screen;
    }

    private JPanel createTitle(String text) {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel title = new JLabel(text);
        title.setForeground(NAVY);
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        titlePanel.add(title, BorderLayout.WEST);
        return titlePanel;
    }

    private JPanel createStatistics() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        panel.add(createStatCard("Hóa đơn hôm nay", lblHoaDonHomNay));
        panel.add(createStatCard("Tổng hóa đơn", lblTongHoaDon));
        panel.add(createStatCard("Khách hàng hôm nay", lblKhachHangHomNay));
        panel.add(createStatCard("Doanh thu trong ngày", lblDoanhThuHomNay));
        return panel;
    }

    private JPanel createStatCard(String title, JLabel value) {
        JPanel card = new JPanel(new BorderLayout(4, 3));
        card.setBackground(new Color(245, 247, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(80, 80, 80));
        value.setFont(new Font("Segoe UI", Font.BOLD, 16));
        value.setForeground(NAVY);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(170, 60));
        return card;
    }

    private JPanel createSearchBar() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        JPanel search = new JPanel(new BorderLayout(6, 0));
        search.setOpaque(false);
        JLabel hint = new JLabel("Tìm hóa đơn / Khách hàng:");
        txtTimKiem.setToolTipText("Nhập mã hóa đơn, tên khách hàng hoặc số điện thoại");
        txtTimKiem.addActionListener(e -> timKiem());
        search.add(hint, BorderLayout.WEST);
        search.add(txtTimKiem, BorderLayout.CENTER);
        JButton btnTimKiem = new JButton("Tra cứu");
        btnTimKiem.addActionListener(e -> timKiem());
        search.add(btnTimKiem, BorderLayout.EAST);
        panel.add(search, BorderLayout.CENTER);

        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            napDanhSach();
        });
        panel.add(btnLamMoi, BorderLayout.EAST);
        return panel;
    }

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

    private JPanel createDetailScreen() {
        JPanel screen = new JPanel(new BorderLayout(0, 12));
        screen.setOpaque(false);
        JPanel title = createTitle("Chi tiết hóa đơn");

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        content.add(createInvoiceInformation(), BorderLayout.NORTH);

        configureDetailTable();
        content.add(new JScrollPane(tblChiTiet), BorderLayout.CENTER);
        content.add(createDetailFooter(), BorderLayout.SOUTH);
        screen.add(title, BorderLayout.NORTH);
        screen.add(content, BorderLayout.CENTER);
        return screen;
    }

    private JPanel createInvoiceInformation() {
        JPanel information = new JPanel(new GridLayout(1, 2, 26, 0));
        information.setOpaque(false);
        JPanel invoiceInfo = new JPanel(new GridLayout(0, 2, 8, 6));
        invoiceInfo.setOpaque(false);
        invoiceInfo.add(sectionLabel("Thông tin hóa đơn"));
        invoiceInfo.add(new JLabel());
        addInfo(invoiceInfo, "Mã hóa đơn:", lblMaHoaDon);
        addInfo(invoiceInfo, "Ngày:", lblNgayLap);
        addInfo(invoiceInfo, "Khách hàng:", lblKhachHang);
        addInfo(invoiceInfo, "Trạng thái:", lblTrangThai);
        lblTrangThai.setForeground(GREEN);
        lblTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel customerInfo = new JPanel(new GridLayout(0, 2, 8, 6));
        customerInfo.setOpaque(false);
        customerInfo.add(sectionLabel("Thông tin khách hàng"));
        customerInfo.add(new JLabel());
        addInfo(customerInfo, "Mã khách hàng:", lblMaKhachHang);
        addInfo(customerInfo, "Tên khách hàng:", lblTenKhachHang);
        addInfo(customerInfo, "Số điện thoại:", lblSoDienThoai);
        addInfo(customerInfo, "Thú cưng:", lblThuCung);
        addInfo(customerInfo, "Thanh toán:", lblPhuongThuc);
        information.add(invoiceInfo);
        information.add(customerInfo);
        return information;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(NAVY);
        return label;
    }

    private void addInfo(JPanel panel, String title, JLabel value) {
        panel.add(new JLabel(title));
        panel.add(value);
    }

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
        tblChiTiet.getColumnModel().getColumn(4).setPreferredWidth(70);
        tblChiTiet.getColumnModel().getColumn(5).setPreferredWidth(120);
        tblChiTiet.getColumnModel().getColumn(6).setPreferredWidth(135);
    }

    private JPanel createDetailFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        totalPanel.setOpaque(false);
        JLabel totalTitle = new JLabel("Tổng cộng:");
        totalTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalPanel.add(totalTitle);
        totalPanel.add(lblTongTien);
        footer.add(totalPanel, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.setOpaque(false);
        JButton btnXuatHoaDon = new JButton("Xuất hóa đơn");
        btnXuatHoaDon.addActionListener(e -> xuatHoaDon());
        JButton btnQuayLai = new JButton("Quay lại");
        btnQuayLai.addActionListener(e -> cardLayout.show(screens, "LIST"));
        actions.add(btnXuatHoaDon);
        actions.add(btnQuayLai);
        footer.add(actions, BorderLayout.SOUTH);
        return footer;
    }

    private void timKiem() {
        napDanhSach();
    }

    private void napDanhSach() {
        try {
            HoaDonDAO.ThongKe thongKe = dao.layThongKe();
            List<HoaDon> hoaDons = dao.timHoaDon(txtTimKiem.getText());
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

    private void capNhatThongKe(HoaDonDAO.ThongKe thongKe) {
        lblHoaDonHomNay.setText(Integer.toString(thongKe.getHoaDonHomNay()));
        lblTongHoaDon.setText(Integer.toString(thongKe.getTongHoaDon()));
        lblKhachHangHomNay.setText(Integer.toString(thongKe.getKhachHangHomNay()));
        lblDoanhThuHomNay.setText(formatMoney(thongKe.getDoanhThuHomNay()) + " ₫");
    }

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
            cardLayout.show(screens, "DETAIL");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải chi tiết hóa đơn.\n" + e.getMessage(),
                    "Lỗi tra cứu hóa đơn",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hienThongTinChiTiet(HoaDon invoice) {
        lblMaHoaDon.setText(invoice.getMaHoaDon());
        lblNgayLap.setText(formatDate(invoice.getNgayLap()));
        lblKhachHang.setText(invoice.getTenKhachHang());
        lblMaKhachHang.setText(invoice.getMaKhachHang());
        lblTenKhachHang.setText(invoice.getTenKhachHang());
        lblSoDienThoai.setText(invoice.getSoDienThoai());
        lblThuCung.setText(layTenThuCung(invoice));
        lblPhuongThuc.setText(invoice.getTenPhuongThuc());
        lblTongTien.setText(formatMoney(invoice.getTongTien()) + " ₫");
        detailTableModel.setDetails(invoice.getChiTiet());
    }

    private String layTenThuCung(HoaDon invoice) {
        List<String> names = new ArrayList<>();
        for (HoaDon.ChiTiet detail : invoice.getChiTiet()) {
            if ("Thú cưng".equals(detail.getLoaiHang())) {
                names.add(detail.getTenHang());
            }
        }
        return names.isEmpty() ? "-" : String.join(", ", names);
    }

    private void xuatHoaDon() {
        if (hoaDonDangXem == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hóa đơn PDF");
        chooser.setSelectedFile(new java.io.File(hoaDonDangXem.getMaHoaDon() + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp PDF (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path file = chooser.getSelectedFile().toPath();
        if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            file = file.resolveSibling(file.getFileName() + ".pdf");
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
            Files.write(file, taoPdfHoaDon(hoaDonDangXem));
            JOptionPane.showMessageDialog(
                    this,
                    "Đã lưu hóa đơn PDF:\n" + file.toAbsolutePath(),
                    "Xuất hóa đơn thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể lưu hóa đơn PDF.\n" + e.getMessage(),
                    "Lỗi xuất PDF",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private byte[] taoPdfHoaDon(HoaDon invoice) throws IOException {
        int pageCount = Math.max(
                1,
                (invoice.getChiTiet().size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        List<byte[]> objects = new ArrayList<>();
        objects.add(null);
        objects.add(null);

        StringBuilder pageReferences = new StringBuilder();
        for (int page = 0; page < pageCount; page++) {
            int pageObject = 3 + page * 3;
            int contentObject = pageObject + 1;
            int imageObject = pageObject + 2;
            pageReferences.append(pageObject).append(" 0 R ");

            byte[] content = ascii("q\n"
                    + PDF_PAGE_WIDTH + " 0 0 " + PDF_PAGE_HEIGHT + " 0 0 cm\n"
                    + "/Im0 Do\nQ\n");
            BufferedImage image = taoTrangHoaDon(invoice, page);
            byte[] compressedImage = nenAnhRgb(image);

            objects.add(ascii(pageObject + " 0 obj\n"
                    + "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 "
                    + PDF_PAGE_WIDTH + " " + PDF_PAGE_HEIGHT + "] "
                    + "/Resources << /XObject << /Im0 " + imageObject + " 0 R >> >> "
                    + "/Contents " + contentObject + " 0 R >>\nendobj\n"));
            objects.add(streamObject(contentObject, "<<", content));
            objects.add(streamObject(
                    imageObject,
                    "<< /Type /XObject /Subtype /Image /Width "
                            + image.getWidth() + " /Height " + image.getHeight()
                            + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /FlateDecode",
                    compressedImage));
        }

        objects.set(0, ascii("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"));
        objects.set(1, ascii("2 0 obj\n<< /Type /Pages /Kids ["
                + pageReferences + "] /Count " + pageCount + " >>\nendobj\n"));
        return assemblePdf(objects);
    }

    private BufferedImage taoTrangHoaDon(HoaDon invoice, int page) {
        int width = PDF_PAGE_WIDTH * PDF_IMAGE_SCALE;
        int height = PDF_PAGE_HEIGHT * PDF_IMAGE_SCALE;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.scale(PDF_IMAGE_SCALE, PDF_IMAGE_SCALE);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT);
            graphics.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int left = 44;
            graphics.setColor(NAVY);
            graphics.setFont(new Font("Segoe UI", Font.BOLD, 23));
            graphics.drawString("PET SHOP PRO", left, 54);
            graphics.setFont(new Font("Segoe UI", Font.BOLD, 15));
            graphics.drawString("HÓA ĐƠN THANH TOÁN", left, 80);
            graphics.setColor(Color.DARK_GRAY);
            graphics.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            graphics.drawString("Mã hóa đơn: " + safe(invoice.getMaHoaDon()), left, 115);
            graphics.drawString("Ngày lập: " + formatDate(invoice.getNgayLap()), left, 137);
            graphics.drawString("Khách hàng: " + safe(invoice.getTenKhachHang()), left, 159);
            graphics.drawString("Mã khách hàng: " + safe(invoice.getMaKhachHang()), left, 181);
            graphics.drawString("Số điện thoại: " + safe(invoice.getSoDienThoai()), left, 203);
            graphics.drawString("Thú cưng: " + layTenThuCung(invoice), left, 225);
            graphics.drawString("Thanh toán: " + safe(invoice.getTenPhuongThuc()), left, 247);

            int tableTop = 278;
            int rowHeight = 24;
            int tableBottom = tableTop + rowHeight * (ITEMS_PER_PAGE + 1);
            graphics.setColor(NAVY);
            graphics.fillRect(left, tableTop, PDF_PAGE_WIDTH - left * 2, rowHeight);
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("Segoe UI", Font.BOLD, 9));
            graphics.drawString("STT", left + 7, tableTop + 16);
            graphics.drawString("MÃ", left + 43, tableTop + 16);
            graphics.drawString("SẢN PHẨM / DỊCH VỤ", left + 105, tableTop + 16);
            graphics.drawString("SL", left + 340, tableTop + 16);
            graphics.drawString("ĐƠN GIÁ", left + 378, tableTop + 16);
            graphics.drawString("THÀNH TIỀN", left + 464, tableTop + 16);

            List<HoaDon.ChiTiet> details = invoice.getChiTiet();
            int firstItem = page * ITEMS_PER_PAGE;
            int lastItem = Math.min(firstItem + ITEMS_PER_PAGE, details.size());
            for (int index = firstItem; index < lastItem; index++) {
                int row = index - firstItem;
                int y = tableTop + rowHeight * (row + 1);
                graphics.setColor(row % 2 == 0 ? new Color(246, 249, 252) : Color.WHITE);
                graphics.fillRect(left, y, PDF_PAGE_WIDTH - left * 2, rowHeight);
                graphics.setColor(BORDER);
                graphics.drawLine(left, y + rowHeight, PDF_PAGE_WIDTH - left, y + rowHeight);
                HoaDon.ChiTiet item = details.get(index);
                graphics.setColor(Color.DARK_GRAY);
                graphics.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                graphics.drawString(Integer.toString(index + 1), left + 9, y + 16);
                graphics.drawString(safe(item.getMaHang()), left + 43, y + 16);
                drawClippedText(graphics, safe(item.getTenHang()), left + 105, y + 16, 225);
                graphics.drawString(Integer.toString(item.getSoLuong()), left + 343, y + 16);
                graphics.drawString(formatMoney(item.getDonGia()), left + 378, y + 16);
                graphics.drawString(formatMoney(item.getThanhTien()), left + 464, y + 16);
            }

            graphics.setColor(BORDER);
            graphics.drawRect(left, tableTop, PDF_PAGE_WIDTH - left * 2, tableBottom - tableTop);
            if (page == pageCount(invoice) - 1) {
                int totalY = tableBottom + 38;
                graphics.setColor(NAVY);
                graphics.setFont(new Font("Segoe UI", Font.BOLD, 14));
                graphics.drawString("TỔNG CỘNG:", left + 315, totalY);
                graphics.drawString(formatMoney(invoice.getTongTien()) + " ₫", left + 420, totalY);
                graphics.setColor(Color.GRAY);
                graphics.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                graphics.drawString("Cảm ơn quý khách đã mua hàng!", left + 145, totalY + 42);
            }
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private int pageCount(HoaDon invoice) {
        return Math.max(1, (invoice.getChiTiet().size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
    }

    private void drawClippedText(Graphics2D graphics, String text, int x, int y, int maxWidth) {
        java.awt.Shape originalClip = graphics.getClip();
        graphics.clipRect(x, y - 13, maxWidth, 18);
        graphics.drawString(text, x, y);
        graphics.setClip(originalClip);
    }

    private byte[] nenAnhRgb(BufferedImage image) throws IOException {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressed)) {
            int[] pixels = new int[image.getWidth()];
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

    private byte[] streamObject(int objectNumber, String dictionary, byte[] data)
            throws IOException {
        ByteArrayOutputStream object = new ByteArrayOutputStream();
        object.write(ascii(objectNumber + " 0 obj\n" + dictionary
                + " /Length " + data.length + " >>\nstream\n"));
        object.write(data);
        object.write(ascii("\nendstream\nendobj\n"));
        return object.toByteArray();
    }

    private byte[] assemblePdf(List<byte[]> objects) throws IOException {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        pdf.write(ascii("%PDF-1.4\n"));
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (byte[] object : objects) {
            offsets.add(pdf.size());
            pdf.write(object);
        }
        int xrefOffset = pdf.size();
        pdf.write(ascii("xref\n0 " + offsets.size() + "\n"));
        pdf.write(ascii("0000000000 65535 f \n"));
        for (int index = 1; index < offsets.size(); index++) {
            pdf.write(ascii(String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(index))));
        }
        pdf.write(ascii("trailer\n<< /Size " + offsets.size()
                + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n"));
        return pdf.toByteArray();
    }

    private byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.ISO_8859_1);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME);
    }

    private static class InvoiceTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
                "MÃ", "THỜI GIAN", "KHÁCH HÀNG", "TỔNG TIỀN",
                "THANH TOÁN", "TRẠNG THÁI", ""
        };
        private List<HoaDon> invoices = new ArrayList<>();

        @Override
        public int getRowCount() {
            return invoices.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 6;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

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

        private HoaDon getInvoice(int row) {
            return row < 0 || row >= invoices.size() ? null : invoices.get(row);
        }

        private void setInvoices(List<HoaDon> invoices) {
            this.invoices = new ArrayList<>(invoices);
            fireTableDataChanged();
        }
    }

    private static class DetailTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
                "STT", "Mã sản phẩm / Dịch vụ", "Loại", "Sản phẩm / Dịch vụ",
                "Số lượng", "Đơn giá", "Thành tiền"
        };
        private List<HoaDon.ChiTiet> details = new ArrayList<>();

        @Override
        public int getRowCount() {
            return details.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            HoaDon.ChiTiet detail = details.get(row);
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
                    return detail.getSoLuong();
                case 5:
                    return formatMoney(detail.getDonGia()) + " ₫";
                case 6:
                    return formatMoney(detail.getThanhTien()) + " ₫";
                default:
                    return "";
            }
        }

        private void setDetails(List<HoaDon.ChiTiet> details) {
            this.details = new ArrayList<>(details);
            fireTableDataChanged();
        }
    }

    private static String formatMoney(BigDecimal amount) {
        return MONEY.format(amount == null ? BigDecimal.ZERO : amount);
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected, boolean focused, int row, int column) {
            Component component = super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(selected ? table.getSelectionForeground() : GREEN);
            setFont(table.getFont().deriveFont(Font.BOLD));
            return component;
        }
    }

    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        private ButtonRenderer() {
            setText("Xem thêm");
            setFocusable(false);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected, boolean focused, int row, int column) {
            return this;
        }
    }

    private interface RowAction {
        void perform(int row);
    }

    private static class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton("Xem thêm");
        private final JTable table;
        private final RowAction rowAction;
        private int row;

        private ButtonEditor(JTable table, RowAction rowAction) {
            this.table = table;
            this.rowAction = rowAction;
            button.setFocusable(false);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    fireEditingStopped();
                    ButtonEditor.this.rowAction.perform(
                            ButtonEditor.this.table.convertRowIndexToModel(ButtonEditor.this.row));
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean selected, int row, int column) {
            this.row = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Xem thêm";
        }
    }
}
