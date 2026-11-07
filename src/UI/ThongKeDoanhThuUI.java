package UI;

import control.ThongKeDoanhThuControl;
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


public class ThongKeDoanhThuUI extends JFrame implements ActionListener, MouseListener {


    private static final Color NAVY = new Color(18, 48, 82);

    private static final Color BLUE = new Color(45, 125, 205);

    private static final Color BORDER = new Color(210, 220, 230);

    private static final Color MUTED = new Color(105, 116, 130);

    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));


    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MM/yyyy");


    private final ThongKeDoanhThuControl thongKeControl = new ThongKeDoanhThuControl();

    private final JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));

    private final JComboBox<CheDo> cmbCheDo = new JComboBox<>(CheDo.values());

    private final JLabel lblKy = new JLabel();

    private final JLabel lblTongDoanhThu = new JLabel("—", SwingConstants.CENTER);

    private final JLabel lblTongHoaDon = new JLabel("—", SwingConstants.CENTER);

    private final JLabel lblTrungBinh = new JLabel("—", SwingConstants.CENTER);

    private final JLabel lblTrangThai = new JLabel(" ");

    private final JButton btnTruoc = new JButton("‹");

    private final JButton btnSau = new JButton("›");

    private final JButton btnPdf = new JButton("Xuất file");

    private final BangMatHangModel bangModel = new BangMatHangModel();

    private final JTable tblMatHang = new JTable(bangModel);

    private final BieuDoPanel pnlBieuDo = new BieuDoPanel();

    private LocalDate ngayThamChieu = LocalDate.now();

    private BaoCao baoCao;


    public ThongKeDoanhThuUI() {
        setTitle("Thống kê doanh thu");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pnlNoiDung.setBackground(new Color(229, 241, 252));
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(14, 18, 16, 18));
        setContentPane(pnlNoiDung);
        pnlNoiDung.add(taoTieuDe(), BorderLayout.NORTH);
        pnlNoiDung.add(taoNoiDung(), BorderLayout.CENTER);


        cmbCheDo.addActionListener(this);

        btnTruoc.addActionListener(this);

        btnSau.addActionListener(this);
        tblMatHang.addMouseListener(this);
        capNhatBaoCao();
    }




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


    public void lamMoi() {
        capNhatBaoCao();
    }



    private JPanel taoTieuDe() {

        JPanel pnlKhung = new JPanel(new BorderLayout());
        pnlKhung.setOpaque(false);

        JLabel lblTitle2 = new JLabel("Thống kê doanh thu");
        lblTitle2.setForeground(NAVY);
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 25));
        pnlKhung.add(lblTitle2, BorderLayout.WEST);
        return pnlKhung;
    }



    private JPanel taoNoiDung() {

        JPanel pnlContent = new JPanel(new BorderLayout(0, 12));
        pnlContent.setBackground(new Color(229, 241, 252));


        JPanel pnlToolbar = new JPanel(new BorderLayout(10, 0));
        pnlToolbar.setOpaque(false);

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



        btnPdf.addActionListener(this);
        pnlToolbar.add(btnPdf, BorderLayout.EAST);


        JPanel pnlStats = new JPanel(new java.awt.GridLayout(1, 3, 12, 0));
        pnlStats.setOpaque(false);
        pnlStats.add(taoTheChiSo("Tổng doanh thu", lblTongDoanhThu));
        pnlStats.add(taoTheChiSo("Tổng số hóa đơn", lblTongHoaDon));
        pnlStats.add(taoTheChiSo("Doanh thu trung bình/HD", lblTrungBinh));

        JPanel pnlHeaderContent = new JPanel(new BorderLayout(0, 12));
        pnlHeaderContent.setOpaque(false);
        pnlHeaderContent.add(pnlToolbar, BorderLayout.NORTH);
        pnlHeaderContent.add(pnlStats, BorderLayout.CENTER);


        JPanel pnlChartCard = taoTheNoiDung("Biểu đồ doanh thu");
        pnlChartCard.add(pnlBieuDo, BorderLayout.CENTER);

        JPanel pnlTableCard = taoTheNoiDung("Doanh thu theo mặt hàng");
        cauHinhBang();

        JScrollPane scrCuon2 = new JScrollPane(tblMatHang);
        scrCuon2.setBorder(BorderFactory.createLineBorder(BORDER));
        pnlTableCard.add(scrCuon2, BorderLayout.CENTER);


        JPanel pnlResults = new JPanel(new java.awt.GridLayout(1, 2, 14, 0));
        pnlResults.setOpaque(false);
        pnlResults.add(pnlChartCard);
        pnlResults.add(pnlTableCard);


        JPanel pnlCenter2 = new JPanel(new BorderLayout(0, 10));
        pnlCenter2.setOpaque(false);
        pnlCenter2.add(pnlHeaderContent, BorderLayout.NORTH);
        pnlCenter2.add(pnlResults, BorderLayout.CENTER);

        lblTrangThai.setForeground(MUTED);
        pnlContent.add(pnlCenter2, BorderLayout.CENTER);
        pnlContent.add(lblTrangThai, BorderLayout.SOUTH);
        return pnlContent;
    }





    private JPanel taoTheChiSo(String tieuDe, JLabel lblGiaTri2) {

        JPanel pnlCard = new JPanel(new BorderLayout(4, 4));
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JLabel lblNhan2 = new JLabel(tieuDe, SwingConstants.CENTER);
        lblNhan2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGiaTri2.setForeground(Color.BLACK);
        lblGiaTri2.setFont(new Font("Segoe UI", Font.BOLD, 21));
        pnlCard.add(lblNhan2, BorderLayout.NORTH);
        pnlCard.add(lblGiaTri2, BorderLayout.CENTER);
        pnlCard.setPreferredSize(new Dimension(180, 62));
        return pnlCard;
    }




    private JPanel taoTheNoiDung(String tieuDe) {

        JPanel pnlKhung = new JPanel(new BorderLayout(0, 8));
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(9, 10, 10, 10)));

        JLabel lblNhan2 = new JLabel(tieuDe);
        lblNhan2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlKhung.add(lblNhan2, BorderLayout.NORTH);
        return pnlKhung;
    }


    private void cauHinhBang() {
        tblMatHang.setRowHeight(25);
        tblMatHang.setFillsViewportHeight(true);
        tblMatHang.setAutoCreateRowSorter(true);
        tblMatHang.getTableHeader().setReorderingAllowed(false);
        tblMatHang.getTableHeader().setBackground(new Color(245, 247, 250));
        tblMatHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tblMatHang.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {








            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable tblDuLieu, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(tblDuLieu, value, selected, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                setHorizontalAlignment(col == 1 || col == 4
                        ? SwingConstants.RIGHT
                        : SwingConstants.LEFT);
                return this;
            }
        });
    }


    private void capNhatBaoCao() {
        CheDo cheDo = (CheDo) cmbCheDo.getSelectedItem();
        if (cheDo == null) {
            return;
        }

        lblKy.setText(nhanKy(cheDo, ngayThamChieu));
        btnSau.setEnabled(laKyTruocHienTai(cheDo, ngayThamChieu));

        lblTrangThai.setText("Đang tải số liệu...");
        try {
            baoCao = thongKeControl.layBaoCao(cheDo, ngayThamChieu);

            lblTongDoanhThu.setText(dinhDangTien(baoCao.getTongDoanhThu()) + " ₫");

            lblTongHoaDon.setText(String.valueOf(baoCao.getTongHoaDon()));

            lblTrungBinh.setText(dinhDangTien(baoCao.getDoanhThuTrungBinh()) + " ₫");
            bangModel.setBaoCao(baoCao);
            pnlBieuDo.setBaoCao(baoCao, cheDo);

            lblTrangThai.setText(" ");
        } catch (SQLException ex) {
            baoCao = null;
            bangModel.setBaoCao(null);
            pnlBieuDo.setBaoCao(null, cheDo);

            lblTongDoanhThu.setText("—");

            lblTongHoaDon.setText("—");

            lblTrungBinh.setText("—");

            lblTrangThai.setText("Không tải được thống kê doanh thu.");
            JOptionPane.showMessageDialog(pnlNoiDung,
                    "Không tải được thống kê doanh thu.\n" + ex.getMessage(),
                    "Lỗi thống kê", JOptionPane.ERROR_MESSAGE);
        }
    }



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




    private String dinhDangTien(double amount) {
        return MONEY.format(amount);
    }




    private double tinhTyTrong(double doanhThu) {
        if (baoCao == null || baoCao.getTongDoanhThu() == 0) {
            return 0;
        }
        return Math.round(doanhThu * 1000 / baoCao.getTongDoanhThu()) / 10.0;
    }


    private void xuatPdf() {
        if (baoCao == null) {
            JOptionPane.showMessageDialog(pnlNoiDung, "Chưa có số liệu để xuất.",
                    "Xuất thống kê", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CheDo cheDo = (CheDo) cmbCheDo.getSelectedItem();

        JFileChooser fchChonTep = new JFileChooser();
        fchChonTep.setDialogTitle("Lưu thống kê doanh thu PDF");
        fchChonTep.setSelectedFile(new java.io.File("ThongKeDoanhThu-" + nhanKy(cheDo, ngayThamChieu)
                .replace('/', '-') + ".pdf"));
        fchChonTep.setFileFilter(new FileNameExtensionFilter("Tệp PDF (*.pdf)", "pdf"));
        if (fchChonTep.showSaveDialog(pnlNoiDung) != JFileChooser.APPROVE_OPTION) {
            return;
        }

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



    private byte[] taoPdf() throws IOException {

        final int width = 842;

        final int height = 595;
        final int firstPageRows = 11;
        final int followingPageRows = 17;
        int extraRows = Math.max(0, baoCao.getMatHangs().size() - firstPageRows);
        int pageCount = 1 + (extraRows + followingPageRows - 1) / followingPageRows;

        List<byte[]> objects = new ArrayList<>();
        objects.add(null);
        objects.add(null);

        StringBuilder pageReferences = new StringBuilder();
        CheDo cheDo = (CheDo) cmbCheDo.getSelectedItem();

        for (int page = 0; page < pageCount; page++) {
            int pageObject = 3 + page * 3;
            int contentObject = pageObject + 1;
            int imageObject = pageObject + 2;
            pageReferences.append(pageObject).append(" 0 R ");

            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                    width * 2, height * 2, java.awt.image.BufferedImage.TYPE_INT_RGB);

            Graphics2D graphics = image.createGraphics();
            try {
                graphics.scale(2, 2);
                vePdfTrang(graphics, page, cheDo, firstPageRows, followingPageRows);
            } finally {
                graphics.dispose();
            }

            byte[] compressedImage = nenAnhRgb(image);

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

        int count;

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
            g.drawString(String.format(Locale.ROOT, "%.1f", tinhTyTrong(dong.getDoanhThu())),
                    720, rowY + 16);
        }
        g.setColor(MUTED);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g.drawString("Trang " + (page + 1), 770, 578);
    }








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

        double max = 0;
        for (DongBieuDo row : baoCao.getBieuDo()) {
            max = Math.max(max, row.getDoanhThu());
        }
        if (max == 0) {
            max = 1;
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

        List<DongBieuDo> points = baoCao.getBieuDo();
        int gap = points.size() >= 20 ? 2 : 5;
        int barWidth = Math.max(2, (plotWidth - gap * (points.size() - 1)) / points.size());
        for (int i = 0; i < points.size(); i++) {
            DongBieuDo point = points.get(i);
            int barHeight = (int) Math.round(point.getDoanhThu() / max * plotHeight);
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





    private String catChuoi(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }




    private byte[] nenAnhRgb(java.awt.image.BufferedImage image) throws IOException {

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






    private byte[] streamObject(int number, String dictionary, byte[] data) throws IOException {

        ByteArrayOutputStream object = new ByteArrayOutputStream();
        object.write(ascii(number + " 0 obj\n" + dictionary
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
        pdf.write(ascii("xref\n0 " + offsets.size() + "\n0000000000 65535 f \n"));
        for (int i = 1; i < offsets.size(); i++) {
            pdf.write(ascii(String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(i))));
        }
        pdf.write(ascii("trailer\n<< /Size " + offsets.size()
                + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n"));
        return pdf.toByteArray();
    }




    private byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.ISO_8859_1);
    }


    private final class BieuDoPanel extends JPanel {

        private BaoCao data;

        private CheDo cheDo = CheDo.NGAY;


        private BieuDoPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(380, 270));
        }




        private void setBaoCao(BaoCao data, CheDo cheDo) {
            this.data = data;
            this.cheDo = cheDo;
            repaint();
        }



        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (data == null || data.getBieuDo().isEmpty()) {
                return;
            }

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

                double max = 0;
                for (DongBieuDo row : data.getBieuDo()) {
                    max = Math.max(max, row.getDoanhThu());
                }
                if (max == 0) {
                    max = 1;
                }

                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                for (int line = 0; line <= 4; line++) {

                    int y = top + chartHeight - chartHeight * line / 4;

                    double value = Math.round(max * line / 4);
                    g.setColor(new Color(226, 230, 235));
                    g.drawLine(left, y, left + chartWidth, y);
                    g.setColor(MUTED);
                    g.drawString(catChuoi(dinhDangTien(value), 9), 1, y + 4);
                }

                int count = data.getBieuDo().size();
                int gap = count >= 20 ? 2 : 5;
                int barWidth = Math.max(2, (chartWidth - gap * (count - 1)) / count);
                for (int i = 0; i < count; i++) {
                    DongBieuDo row = data.getBieuDo().get(i);
                    int barHeight = (int) Math.round(row.getDoanhThu() / max * chartHeight);

                    int x = left + i * (barWidth + gap);

                    int y = top + chartHeight - barHeight;
                    g.setColor(BLUE);
                    g.fillRect(x, y, barWidth, barHeight);
                    if (hienNhanMoc(i, count)) {
                        g.setColor(MUTED);
                        g.setFont(new Font("Segoe UI", Font.PLAIN, 9));

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





        private boolean hienNhanMoc(int index, int count) {
            if (count <= 12) {
                return true;
            }
            if (cheDo == CheDo.NGAY) {
                return index % 3 == 0;
            }
            return index % 5 == 0 || index == count - 1;
        }





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


    private final class BangMatHangModel extends AbstractTableModel {

        private final String[] cot = { "STT", "Danh mục / Sản phẩm", "Số lượng",
                "Doanh thu", "Tỷ trọng (%)" };

        private List<DongMatHang> rows = new ArrayList<>();



        private void setBaoCao(BaoCao baoCao) {
            rows = baoCao == null ? new ArrayList<>() : baoCao.getMatHangs();
            fireTableDataChanged();
        }


        @Override
        public int getRowCount() {
            return rows.size();
        }


        @Override
        public int getColumnCount() {
            return cot.length;
        }



        @Override
        public String getColumnName(int column) {
            return cot[column];
        }





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
                    return String.format(Locale.ROOT, "%.1f", tinhTyTrong(row.getDoanhThu()));
                default:
                    throw new IndexOutOfBoundsException("Cột không hợp lệ: " + columnIndex);
            }
        }
    }
}
