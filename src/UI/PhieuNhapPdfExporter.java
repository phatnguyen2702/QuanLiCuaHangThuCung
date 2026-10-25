package UI;

import dao.NhapKhoDAO;
import entity.ChiTietPhieuNhap;
import entity.PhieuNhapKho;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.DeflaterOutputStream;

/**
 * Xuat chung tu nhap kho A4 nhieu trang, co dau/chu ky de trong.
 * Dung java.awt ve tieng Viet roi nhung anh vao PDF. Khong can them JAR.
 * Du lieu phai duoc tai tu SQL Server qua NhapKhoDAO.layBanInPhieuNhap().
 */
public final class PhieuNhapPdfExporter {
    private PhieuNhapPdfExporter() {
    }

    private static final int W = 595, H = 842, SCALE = 2;
    private static final int LEFT = 37, RIGHT = 558;
    private static final int[] COLUMNS = { 29, 53, 158, 43, 74, 94, 70 }; // 521 pt
    private static final int LINE_H = 31;
    private static final Color NAVY = new Color(18, 48, 82);
    private static final Color BORDER = new Color(204, 215, 225);
    private static final Color MUTED = new Color(97, 108, 122);
    private static final Color PALE = new Color(244, 248, 252);
    private static final Color GREEN = new Color(22, 124, 73);
    private static final Color ORANGE = new Color(159, 84, 23);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Hien hop thoai Save As, chi ghi de tep da ton tai neu nguoi dung dong y. */
    public static void xuat(Component cha, NhapKhoDAO.BanInPhieuNhap phieu) {
        if (phieu == null || phieu.getPhieu() == null) {
            JOptionPane.showMessageDialog(cha, "Không tìm thấy dữ liệu phiếu nhập đã lưu.");
            return;
        }
        String ma = phieu.getPhieu().getMaPhieuNhap();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất phiếu nhập " + ma + " ra PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp PDF (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File("Phieu_nhap_" + ma.replaceAll("[^a-zA-Z0-9_-]", "_") + ".pdf"));
        if (chooser.showSaveDialog(cha) != JFileChooser.APPROVE_OPTION)
            return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf"))
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        if (file.exists() && JOptionPane.showConfirmDialog(cha,
                "Tệp này đã tồn tại. Bạn muốn ghi đè?", "Xác nhận ghi đè",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
            return;
        try {
            Files.write(file.toPath(), taoPdf(phieu));
            JOptionPane.showMessageDialog(cha, "Đã xuất phiếu " + ma + ":\n" + file.getAbsolutePath(),
                    "Xuất PDF thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(cha, "Không thể tạo PDF:\n" + ex.getMessage(),
                    "Lỗi xuất PDF", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Exposed for headless format tests. */
    public static byte[] taoPdf(NhapKhoDAO.BanInPhieuNhap data) throws IOException {
        if (data == null || data.getPhieu() == null)
            throw new IllegalArgumentException("Thiếu dữ liệu phiếu nhập.");
        List<ChiTietPhieuNhap> items = data.getChiTiet();
        List<int[]> slices = new ArrayList<>();
        int offset = 0;
        // Page 1: header is taller; reserve space on the final page for signatures.
        while (offset < items.size() || slices.isEmpty()) {
            boolean first = slices.isEmpty();
            int remaining = items.size() - offset;
            int normalCap = first ? 13 : 18;
            int lastCap = first ? 8 : 13;
            int count = remaining <= lastCap ? Math.max(remaining, 0)
                    : remaining <= normalCap ? lastCap : normalCap;
            slices.add(new int[] { offset, offset + count });
            offset += count;
            if (slices.size() > 10000)
                throw new IOException("Phiếu nhập quá dài.");
        }
        List<BufferedImage> pages = new ArrayList<>();
        for (int i = 0; i < slices.size(); i++) {
            int[] seg = slices.get(i);
            pages.add(veTrang(data, seg[0], seg[1], i, slices.size()));
        }
        return dongGoiPdf(pages);
    }

    private static BufferedImage veTrang(NhapKhoDAO.BanInPhieuNhap data,
            int from, int to, int pageNo, int totalPage) {
        BufferedImage img = new BufferedImage(W * SCALE, H * SCALE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, W * SCALE, H * SCALE);
            g.scale(SCALE, SCALE);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            PhieuNhapKho p = data.getPhieu();
            boolean first = pageNo == 0;
            chu(g, "PET SHOP PRO", LEFT, 42, 16, true, NAVY);
            canLePhai(g, "CHỨNG TỪ NHẬP KHO", RIGHT, 41, 9, true, MUTED);
            gach(g, LEFT, 52, RIGHT, 52, NAVY);
            if (first) {
                chu(g, "PHIẾU NHẬP KHO", LEFT, 88, 21, true, NAVY);
                boolean finished = p.isTrangThai();
                chu(g, finished ? "ĐÃ HOÀN TẤT" : "BẢN NHÁP – CHƯA HOÀN TẤT",
                        LEFT, 113, 10, true, finished ? GREEN : ORANGE);
                dongThongTin(g, "Mã phiếu:", giaTri(p.getMaPhieuNhap()), 140,
                        "Ngày nhập:", p.getNgayNhap() == null ? "—" : p.getNgayNhap().format(DATE_TIME));
                dongThongTin(g, "Nhân viên lập:", giaTri(data.getTenNhanVien()), 166,
                        "Mã nhân viên:", giaTri(p.getMaNhanVien()));
                chu(g, "Nhà cung cấp:", LEFT, 192, 9, true, MUTED);
                veFit(g, giaTri(p.getTenNhaCungCap()), LEFT + 95, 192, 424, 9, false, NAVY);
                dongThongTin(g, "Mã NCC:", giaTri(p.getMaNhaCungCap()), 218,
                        "SĐT NCC:", giaTri(data.getSoDienThoaiNhaCungCap()));
                chu(g, "Địa chỉ NCC: ", LEFT, 242, 9, true, MUTED);
                veFit(g, giaTri(data.getDiaChiNhaCungCap()), LEFT + 75, 242, 445, 9, false, NAVY);
            } else {
                chu(g, "PHIẾU NHẬP KHO (tiếp theo)", LEFT, 78, 15, true, NAVY);
                chu(g, "Mã phiếu: " + giaTri(p.getMaPhieuNhap()), LEFT, 102, 10, false, MUTED);
            }
            int tableY = first ? 278 : 129;
            veBang(g, data.getChiTiet(), from, to, tableY);
            int tableEnd = tableY + 29 + (to - from) * LINE_H;
            if (pageNo == totalPage - 1)
                veTongVaChuKy(g, data, tableEnd + 23);
            gach(g, LEFT, 808, RIGHT, 808, BORDER);
            chu(g, "PET SHOP PRO  •  Phiếu nhập " + giaTri(p.getMaPhieuNhap()),
                    LEFT, 823, 8, false, MUTED);
            canLePhai(g, "Trang " + (pageNo + 1) + " / " + totalPage, RIGHT, 823, 8, false, MUTED);
        } finally {
            g.dispose();
        }
        return img;
    }

    private static void dongThongTin(Graphics2D g, String l1, String v1, int y, String l2, String v2) {
        chu(g, l1, LEFT, y, 9, true, MUTED);
        veFit(g, v1, LEFT + 95, y, 160, 9, false, NAVY);
        chu(g, l2, 325, y, 9, true, MUTED);
        veFit(g, v2, 408, y, 148, 9, false, NAVY);
    }

    private static void veBang(Graphics2D g, List<ChiTietPhieuNhap> items, int from, int to, int y) {
        int hHead = 29, row = LINE_H;
        String[] labels = { "STT", "Mã SP", "Tên sản phẩm", "SL", "Đơn giá", "Thành tiền", "Hạn SD" };
        g.setColor(NAVY);
        g.fillRect(LEFT, y, RIGHT - LEFT, hHead);
        int xx = LEFT;
        for (int i = 0; i < COLUMNS.length; i++) {
            g.setColor(Color.WHITE);
            canGiua(g, labels[i], xx, y + 19, COLUMNS[i], 9, true, Color.WHITE);
            xx += COLUMNS[i];
        }
        for (int idx = from; idx < to; idx++) {
            ChiTietPhieuNhap ct = items.get(idx);
            int yy = y + hHead + (idx - from) * row;
            if ((idx % 2) == 1) {
                g.setColor(PALE);
                g.fillRect(LEFT, yy, RIGHT - LEFT, row);
            }
            g.setColor(BORDER);
            g.setStroke(new BasicStroke(.55f));
            g.drawLine(LEFT, yy + row, RIGHT, yy + row);
            int x = LEFT;
            canGiua(g, String.valueOf(idx + 1), x, yy + 19, COLUMNS[0], 9, false, NAVY);
            x += COLUMNS[0];
            veFit(g, giaTri(ct.getMaSanPham()), x + 4, yy + 19, COLUMNS[1] - 8, 8.5f, false, NAVY);
            x += COLUMNS[1];
            veTenSanPham(g, giaTri(ct.getTenSanPham()), x + 5, yy + 12, COLUMNS[2] - 10);
            x += COLUMNS[2];
            canGiua(g, String.valueOf(ct.getSoLuongNhap()), x, yy + 19, COLUMNS[3], 9, false, NAVY);
            x += COLUMNS[3];
            canLePhai(g, tien(ct.getDonGiaNhap()), x + COLUMNS[4] - 4, yy + 19, 8.5f, false, NAVY);
            x += COLUMNS[4];
            canLePhai(g, tien(ct.getThanhTien()), x + COLUMNS[5] - 4, yy + 19, 8.5f, true, NAVY);
            x += COLUMNS[5];
            canGiua(g, ct.getHanSuDung() == null ? "—" : DATE.format(ct.getHanSuDung()),
                    x, yy + 19, COLUMNS[6], 8, false, NAVY);
        }
        g.setColor(BORDER);
        g.setStroke(new BasicStroke(.7f));
        g.drawRect(LEFT, y, RIGHT - LEFT, hHead + (to - from) * row);
    }

    private static void veTenSanPham(Graphics2D g, String s, int x, int y, int maxW) {
        Font f = font(8.5f, false);
        g.setFont(f);
        FontMetrics metrics = g.getFontMetrics();
        if (metrics.stringWidth(s) <= maxW) {
            chu(g, s, x, y + 7, 8.5f, false, NAVY);
            return;
        }
        int n = s.length();
        for (int j = n - 1; j > 0; j--) {
            if (s.charAt(j) == ' ' && metrics.stringWidth(s.substring(0, j)) <= maxW) {
                chu(g, s.substring(0, j), x, y, 8.5f, false, NAVY);
                veFit(g, s.substring(j + 1), x, y + 12, maxW, 8.5f, false, NAVY);
                return;
            }
        }
        veFit(g, s, x, y + 7, maxW, 8.5f, false, NAVY);
    }

    private static void veTongVaChuKy(Graphics2D g, NhapKhoDAO.BanInPhieuNhap data, int y) {
        gach(g, LEFT, y - 13, RIGHT, y - 13, NAVY);
        chu(g, "Tổng số mặt hàng: " + data.getChiTiet().size(), LEFT, y + 3, 10, true, NAVY);
        chu(g, "Tổng số lượng: " + data.getTongSoLuong(), LEFT + 210, y + 3, 10, true, NAVY);
        canLePhai(g, "TỔNG TIỀN: " + tien(data.getTongTien()) + " đồng", RIGHT, y + 26, 12, true, NAVY);
        if (!data.getPhieu().isTrangThai())
            chu(g, "LƯU Ý: Phiếu nháp, chưa được xác nhận nhập kho.", LEFT, y + 45, 9, true, ORANGE);
        else
            chu(g, "Chứng từ lấy từ dữ liệu phiếu nhập đã hoàn tất trên hệ thống.", LEFT, y + 45, 9, false, MUTED);
        int sigY = y + 68;
        canGiua(g, "NGƯỜI GIAO HÀNG", LEFT + 12, sigY, 236, 11, true, NAVY);
        canGiua(g, "NGƯỜI LẬP PHIẾU", LEFT + 275, sigY, 236, 11, true, NAVY);
        canGiua(g, "(Ký và ghi rõ họ tên)", LEFT + 12, sigY + 17, 236, 9, false, MUTED);
        canGiua(g, "(Ký và ghi rõ họ tên)", LEFT + 275, sigY + 17, 236, 9, false, MUTED);
        veFit(g, giaTri(data.getTenNhanVien()), LEFT + 318, sigY + 71, 150, 9, false, NAVY);
    }

    private static String giaTri(String s) {
        return s == null || s.isBlank() ? "—" : s.trim();
    }

    private static Font font(float size, boolean bold) {
        return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, Math.max(1, Math.round(size)));
    }

    private static void chu(Graphics2D g, String s, int x, int y, float sz, boolean bold, Color c) {
        g.setFont(font(sz, bold));
        g.setColor(c);
        g.drawString(s, x, y);
    }

    private static void veFit(Graphics2D g, String s, int x, int y, int maxW, float sz, boolean bold, Color c) {
        g.setFont(font(sz, bold));
        FontMetrics m = g.getFontMetrics();
        if (m.stringWidth(s) > maxW) {
            String suffix = "…";
            while (!s.isEmpty() && m.stringWidth(s + suffix) > maxW)
                s = s.substring(0, s.length() - 1);
            s = s + suffix;
        }
        chu(g, s, x, y, sz, bold, c);
    }

    private static void canGiua(Graphics2D g, String s, int x, int y, int w, float sz,
            boolean bold, Color c) {
        g.setFont(font(sz, bold));
        int tw = g.getFontMetrics().stringWidth(s);
        if (tw > w - 4) {
            veFit(g, s, x + 2, y, w - 4, sz, bold, c);
            return;
        }
        chu(g, s, x + (w - tw) / 2, y, sz, bold, c);
    }

    private static void canLePhai(Graphics2D g, String s, int xRight, int y, float sz,
            boolean bold, Color c) {
        g.setFont(font(sz, bold));
        chu(g, s, xRight - g.getFontMetrics().stringWidth(s), y, sz, bold, c);
    }

    private static void gach(Graphics2D g, int x1, int y1, int x2, int y2, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x1, y1, x2, y2);
    }

    private static String tien(double value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("vi-VN"));
        return new DecimalFormat("#,##0", symbols).format(value);
    }

    /** PDF 1.4 with one lossless deflate RGB image per A4 page. */
    private static byte[] dongGoiPdf(List<BufferedImage> pages) throws IOException {
        List<byte[]> objects = new ArrayList<>();
        objects.add(bytes("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"));
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pages.size(); i++)
            kids.append(3 + 3 * i).append(" 0 R ");
        objects.add(bytes("2 0 obj\n<< /Type /Pages /Kids [ " + kids + "] /Count "
                + pages.size() + " >>\nendobj\n"));
        for (int i = 0; i < pages.size(); i++) {
            int pageObj = 3 + 3 * i, contentsObj = pageObj + 1, imageObj = pageObj + 2;
            BufferedImage img = pages.get(i);
            objects.add(bytes(pageObj + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 "
                    + W + " " + H + "] /Resources << /XObject << /Im0 " + imageObj
                    + " 0 R >> >> /Contents " + contentsObj + " 0 R >>\nendobj\n"));
            String content = "q\n" + W + " 0 0 " + H + " 0 0 cm\n/Im0 Do\nQ\n";
            objects.add(stream(contentsObj, "", bytes(content)));
            byte[] pixelBytes = nenRGB(img);
            objects.add(stream(imageObj, "/Type /XObject /Subtype /Image /Width " + img.getWidth()
                    + " /Height " + img.getHeight()
                    + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /FlateDecode", pixelBytes));
        }
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        pdf.write(bytes("%PDF-1.4\n"));
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (byte[] obj : objects) {
            offsets.add(pdf.size());
            pdf.write(obj);
        }
        int xref = pdf.size();
        pdf.write(bytes("xref\n0 " + offsets.size() + "\n0000000000 65535 f \n"));
        for (int i = 1; i < offsets.size(); i++)
            pdf.write(bytes(String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(i))));
        pdf.write(bytes("trailer\n<< /Size " + offsets.size()
                + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n"));
        return pdf.toByteArray();
    }

    private static byte[] nenRGB(BufferedImage img) throws IOException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        try (DeflaterOutputStream out = new DeflaterOutputStream(sink)) {
            int w = img.getWidth();
            int[] px = new int[w];
            byte[] row = new byte[w * 3];
            for (int y = 0; y < img.getHeight(); y++) {
                img.getRGB(0, y, w, 1, px, 0, w);
                for (int x = 0; x < w; x++) {
                    row[x * 3] = (byte) (px[x] >> 16);
                    row[x * 3 + 1] = (byte) (px[x] >> 8);
                    row[x * 3 + 2] = (byte) px[x];
                }
                out.write(row);
            }
        }
        return sink.toByteArray();
    }

    private static byte[] stream(int index, String opts, byte[] content) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write(bytes(index + " 0 obj\n<< " + opts + " /Length " + content.length + " >>\nstream\n"));
        b.write(content);
        b.write(bytes("\nendstream\nendobj\n"));
        return b.toByteArray();
    }

    private static byte[] bytes(String text) {
        return text.getBytes(StandardCharsets.ISO_8859_1);
    }
}
