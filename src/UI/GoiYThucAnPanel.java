package UI;

import UI.TrangChu.UIStyle;
import dao.SanPhamDAO;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/** Gợi ý thức ăn đang còn hàng theo loài thú cưng và danh mục sản phẩm. */
public class GoiYThucAnPanel extends JPanel {
    private final JComboBox<String> pet = new JComboBox<>(new String[] { "Chó", "Mèo" });
    private final JComboBox<String> age = new JComboBox<>(
            new String[] { "Tất cả độ tuổi", "Con non", "Trưởng thành", "Cao tuổi" });
    private final JComboBox<String> size = new JComboBox<>(
            new String[] { "Tất cả kích thước", "Nhỏ", "Trung bình", "Lớn" });
    private final JTextField search = new JTextField(15);
    private final JPanel cards = new JPanel(new GridLayout(0, 3, 12, 12));
    private final JLabel note = new JLabel("Chọn loại thú cưng để xem thức ăn còn hàng.");
    private final JButton selectedButton = UIStyle.nutPhu("Giỏ hàng (0)");
    private final SanPhamDAO dao = new SanPhamDAO();
    private List<SanPham> products = List.of();
    private final Map<String, Icon> anhSanPhamCache = new HashMap<>();

    public GoiYThucAnPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UIStyle.LIGHT_BLUE);
        setBorder(new EmptyBorder(22, 25, 22, 25));

        JLabel title = new JLabel("Gợi ý thức ăn theo loại thú cưng");
        title.setFont(UIStyle.font(Font.BOLD, 24));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(selectedButton, BorderLayout.EAST);
        selectedButton.addActionListener(e -> moDanhSachDaChon());

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filters.setBackground(Color.WHITE);
        filters.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(206, 222, 237)), new EmptyBorder(8, 10, 8, 10)));
        filters.add(new JLabel("Loại thú cưng:"));
        filters.add(pet);
        filters.add(new JLabel("Độ tuổi:"));
        filters.add(age);
        filters.add(new JLabel("Kích thước:"));
        filters.add(size);
        filters.add(new JLabel("Tên sản phẩm:"));
        filters.add(search);
        JButton find = UIStyle.nutChinh("Tìm kiếm");
        filters.add(find);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(header);
        top.add(Box.createVerticalStrut(12));
        top.add(filters);
        top.add(Box.createVerticalStrut(6));
        top.add(note);
        add(top, BorderLayout.NORTH);

        cards.setBackground(UIStyle.LIGHT_BLUE);
        JScrollPane scroll = new JScrollPane(cards);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        scroll.getViewport().setBackground(UIStyle.LIGHT_BLUE);
        add(scroll, BorderLayout.CENTER);

        find.addActionListener(e -> napLai());
        search.addActionListener(e -> napLai());
        pet.addActionListener(e -> napLai());
        age.addActionListener(e -> napLai());
        size.addActionListener(e -> napLai());
        napLai();
    }

    public void napLai() {
        cards.removeAll();
        try {
            String kind = (String) pet.getSelectedItem();
            products = dao.goiYThucAn(kind, (String) age.getSelectedItem(), (String) size.getSelectedItem());
            String keyword = search.getText().trim().toLowerCase(Locale.ROOT);
            int count = 0;
            for (SanPham product : products) {
                if (product.getTonKho() <= 0)
                    continue;
                String name = product.getTenSanPham() == null ? "" : product.getTenSanPham().toLowerCase(Locale.ROOT);
                if (!keyword.isEmpty() && !name.contains(keyword))
                    continue;
                cards.add(taoTheSanPham(product));
                count++;
            }
            note.setText(count == 0
                    ? "Không có sản phẩm thức ăn còn hàng phù hợp với loài thú cưng đã chọn."
                    : count + " sản phẩm thức ăn còn hàng. Độ tuổi/kích thước là tiêu chí tham khảo; hãy kiểm tra nhãn bao bì.");
        } catch (Exception ex) {
            note.setText("Không tải được sản phẩm: " + ex.getMessage());
        }
        selectedButton.setText("Giỏ hàng (" + tongSoLuongTrongGio() + ")");
        cards.revalidate();
        cards.repaint();
    }

    private JPanel taoTheSanPham(SanPham product) {
        JPanel card = new JPanel(new BorderLayout(7, 7));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(205, 218, 232)), new EmptyBorder(10, 10, 10, 10)));

        JLabel image = new JLabel(anhSanPham(product), SwingConstants.CENTER);
        image.setPreferredSize(new Dimension(150, 115));
        image.setOpaque(true);
        image.setBackground(new Color(246, 249, 252));
        card.add(image, BorderLayout.NORTH);

        String details = "<html><b>" + escape(product.getTenSanPham()) + "</b><br>"
                + escape(product.getTenDanhMuc()) + "<br><font color='#087b43'>"
                + String.format("%,.0f đ", product.getGiaBan()) + " · Tồn "
                + product.getTonKho() + "</font></html>";
        JLabel description = new JLabel(details);
        description.setFont(UIStyle.font(Font.PLAIN, 13));
        card.add(description, BorderLayout.CENTER);

        entity.ThanhToan daThem = timTrongGio(product.getMaSanPham());
        JButton add = UIStyle.nutXanh(daThem == null ? "Thêm vào giỏ hàng"
                : "Đã thêm · SL " + daThem.getSoLuong() + " · thêm tiếp");
        add.addActionListener(e -> themVaoGio(product));
        card.add(add, BorderLayout.SOUTH);
        return card;
    }

    private entity.ThanhToan timTrongGio(String maSanPham) {
        for (entity.ThanhToan item : UI.PhienLamViec.gioHang()) {
            if (maSanPham.equals(item.getMaSanPham()))
                return item;
        }
        return null;
    }

    private int tongSoLuongTrongGio() {
        return UI.PhienLamViec.gioHang().stream().mapToInt(entity.ThanhToan::getSoLuong).sum();
    }

    private void themVaoGio(SanPham product) {
        entity.ThanhToan item = timTrongGio(product.getMaSanPham());
        if (item != null) {
            if (item.getSoLuong() >= product.getTonKho()) {
                JOptionPane.showMessageDialog(this, "Số lượng trong giỏ đã bằng tồn kho hiện có.",
                        "Không đủ tồn kho", JOptionPane.WARNING_MESSAGE);
                return;
            }
            item.setSoLuong(item.getSoLuong() + 1);
        } else {
            if (product.getTonKho() < 1)
                return;
            UI.PhienLamViec.gioHang().add(entity.ThanhToan.sanPham(
                    product.getMaSanPham(), product.getTenSanPham(), BigDecimal.valueOf(product.getGiaBan())));
        }
        napLai();
    }

    /**
     * Anh duoc gan THEO MA SAN PHAM, vi du SP017 -> /img/products/SP017.png.
     * Thu doc resource tren classpath (Eclipse, JAR), sau do moi doc file tu
     * project.
     * Cache de khong phai mo va scale anh moi lan tim kiem / thay doi bo loc.
     */
    private Icon anhSanPham(SanPham product) {
        String maSanPham = product == null ? null : product.getMaSanPham();
        if (maSanPham == null || !maSanPham.matches("[A-Za-z0-9_-]+")) {
            return new TextIcon("Chưa có ảnh sản phẩm");
        }
        return anhSanPhamCache.computeIfAbsent(maSanPham, this::taiAnhTheoMa);
    }

    private Icon taiAnhTheoMa(String maSanPham) {
        for (String extension : new String[] { ".png", ".jpg", ".jpeg" }) {
            String tenFile = maSanPham + extension;
            ImageIcon nguon = null;

            URL resource = GoiYThucAnPanel.class.getResource("/img/products/" + tenFile);
            if (resource != null) {
                nguon = new ImageIcon(resource);
            }

            // Khi chay Java truc tiep tu thu muc project (khong dong goi JAR).
            if (nguon == null || nguon.getIconWidth() <= 0) {
                File file = new File("src/img/products/", tenFile);
                if (!file.isFile()) {
                    file = new File("img/products/", tenFile);
                }
                if (file.isFile()) {
                    nguon = new ImageIcon(file.getAbsolutePath());
                }
            }

            if (nguon != null && nguon.getIconWidth() > 0 && nguon.getIconHeight() > 0) {
                // Giu nguyen ty le: anh vuong khong bi keo det trong the ngang.
                final int rongKhung = 145;
                final int caoKhung = 105;
                double tyLe = Math.min((double) rongKhung / nguon.getIconWidth(),
                        (double) caoKhung / nguon.getIconHeight());
                int rongAnh = Math.max(1, (int) Math.round(nguon.getIconWidth() * tyLe));
                int caoAnh = Math.max(1, (int) Math.round(nguon.getIconHeight() * tyLe));

                BufferedImage ve = new BufferedImage(rongKhung, caoKhung, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = ve.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(nguon.getImage(), (rongKhung - rongAnh) / 2,
                        (caoKhung - caoAnh) / 2, rongAnh, caoAnh, null);
                g.dispose();
                return new ImageIcon(ve);
            }
        }
        return new TextIcon("Chưa có ảnh sản phẩm");
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void moDanhSachDaChon() {
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "Mã SP", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }

            @Override
            public void setValueAt(Object value, int row, int column) {
                if (column != 2) {
                    super.setValueAt(value, row, column);
                    return;
                }
                final int quantity;
                try {
                    quantity = Integer.parseInt(String.valueOf(value).trim());
                    if (quantity < 1)
                        throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(GoiYThucAnPanel.this,
                            "Số lượng phải là số nguyên lớn hơn 0.", "Số lượng không hợp lệ",
                            JOptionPane.WARNING_MESSAGE);
                    fireTableCellUpdated(row, column);
                    return;
                }

                String code = String.valueOf(getValueAt(row, 0));
                for (entity.ThanhToan item : UI.PhienLamViec.gioHang()) {
                    if (code.equals(item.getMaSanPham())) {
                        item.setSoLuong(quantity);
                        super.setValueAt(quantity, row, column);
                        super.setValueAt(String.format("%,.0f đ", item.getThanhTien()), row, 4);
                        selectedButton.setText("Giỏ hàng (" + tongSoLuongTrongGio() + ")");
                        napLai();
                        return;
                    }
                }
                fireTableCellUpdated(row, column);
            }
        };
        for (entity.ThanhToan item : UI.PhienLamViec.gioHang()) {
            model.addRow(new Object[] { item.getMaHang(), item.getTenMatHang(), item.getSoLuong(),
                    String.format("%,.0f đ", item.getDonGia()), String.format("%,.0f đ", item.getThanhTien()) });
        }
        JTable table = new JTable(model);
        table.setToolTipText("Bấm đúp vào ô trong cột Số lượng để chỉnh sửa.");
        UIStyle.taoBang(table, 1, 2, 3);
        JButton remove = UIStyle.nutXoa("Xóa sản phẩm khỏi giỏ");
        JButton checkout = UIStyle.nutXanh("Chuyển sang thanh toán");
        checkout.setEnabled(!UI.PhienLamViec.gioHang().isEmpty());
        JButton close = UIStyle.nutPhu("Đóng");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(remove);
        actions.add(checkout);
        actions.add(close);
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setPreferredSize(new Dimension(760, 340));
        content.add(new JLabel("Bấm đúp vào cột Số lượng để nhập số lượng mới."), BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Giỏ hàng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(content);
        remove.addActionListener(e -> {
            if (table.isEditing() && !table.getCellEditor().stopCellEditing())
                return;
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            String maSanPham = String.valueOf(model.getValueAt(table.convertRowIndexToModel(row), 0));
            UI.PhienLamViec.gioHang().removeIf(item -> maSanPham.equals(item.getMaSanPham()));
            model.removeRow(table.convertRowIndexToModel(row));
            checkout.setEnabled(!UI.PhienLamViec.gioHang().isEmpty());
            napLai();
        });
        checkout.addActionListener(e -> {
            if (table.isEditing() && !table.getCellEditor().stopCellEditing())
                return;
            dialog.dispose();
            if (owner instanceof TrangChu) {
                ((TrangChu) owner).moThanhToanTuGioHang();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy màn hình thanh toán.",
                        "Giỏ hàng", JOptionPane.ERROR_MESSAGE);
            }
        });
        close.addActionListener(e -> dialog.dispose());
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static final class TextIcon implements Icon {
        private final String text;

        private TextIcon(String text) {
            this.text = text;
        }

        @Override
        public int getIconWidth() {
            return 150;
        }

        @Override
        public int getIconHeight() {
            return 110;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setColor(new Color(246, 249, 252));
            g.fillRect(x, y, getIconWidth(), getIconHeight());
            g.setColor(new Color(110, 125, 140));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(text, x + (getIconWidth() - metrics.stringWidth(text)) / 2, y + 58);
            g.dispose();
        }
    }
}
