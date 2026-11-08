package UI;
import UI.TrangChuUI.UIStyle;
import control.SanPhamControl;
import entity.SanPham;
import entity.ChiTietHoaDon;
import control.TrangChuControl;
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
import java.util.List;
import java.util.Locale;
public class GoiYThucAnUI extends JFrame {
private final JComboBox<String> cmbPet = new JComboBox<>(new String[] { "Chó", "Mèo" });
    private final JComboBox<String> cmbAge = new JComboBox<>(
            new String[] { "Tất cả độ tuổi", "Con non", "Trưởng thành", "Cao tuổi" });
    private final JComboBox<String> cmbSize = new JComboBox<>(
            new String[] { "Tất cả kích thước", "Nhỏ", "Trung bình", "Lớn" });
    private final JTextField txtSearch = new JTextField(15);
    private final JPanel pnlCards = new JPanel(new GridLayout(0, 3, 12, 12));
    private final JLabel lblNote = new JLabel("Chọn loại thú cưng để xem thức ăn còn hàng.");
    private final JButton btnSelectedButton = UIStyle.nutPhu("Giỏ hàng (0)");
    private final Runnable moThanhToan;
    private final Map<String, Icon> anhSanPhamCache = new HashMap<>();
    public GoiYThucAnUI() {
        this(null);
    }
    public GoiYThucAnUI(Runnable moThanhToan) {
        this.moThanhToan = moThanhToan;
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(UIStyle.LIGHT_BLUE);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(22, 25, 22, 25));
        JLabel lblTitle2 = new JLabel("Gợi ý thức ăn theo loại thú cưng");
        lblTitle2.setFont(UIStyle.font(Font.BOLD, 24));
        JPanel pnlHeader2 = new JPanel(new BorderLayout());
        pnlHeader2.setOpaque(false);
        pnlHeader2.add(lblTitle2, BorderLayout.WEST);
        pnlHeader2.add(btnSelectedButton, BorderLayout.EAST);
        btnSelectedButton.addActionListener(e -> moDanhSachSanPhamDaChon());
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnlFilters.setBackground(Color.WHITE);
        pnlFilters.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(206, 222, 237)), new EmptyBorder(8, 10, 8, 10)));
        pnlFilters.add(new JLabel("Loại thú cưng:"));
        pnlFilters.add(cmbPet);
        pnlFilters.add(new JLabel("Độ tuổi:"));
        pnlFilters.add(cmbAge);
        pnlFilters.add(new JLabel("Kích thước:"));
        pnlFilters.add(cmbSize);
        pnlFilters.add(new JLabel("Tên sản phẩm:"));
        pnlFilters.add(txtSearch);
        JButton btnFind = UIStyle.nutChinh("Tìm kiếm");
        pnlFilters.add(btnFind);
        JPanel pnlTop2 = new JPanel();
        pnlTop2.setOpaque(false);
        pnlTop2.setLayout(new BoxLayout(pnlTop2, BoxLayout.Y_AXIS));
        pnlTop2.add(pnlHeader2);
        pnlTop2.add(Box.createVerticalStrut(12));
        pnlTop2.add(pnlFilters);
        pnlTop2.add(Box.createVerticalStrut(6));
        pnlTop2.add(lblNote);
        add(pnlTop2, BorderLayout.NORTH);
        pnlCards.setBackground(UIStyle.LIGHT_BLUE);
        JScrollPane scrCuon2 = new JScrollPane(pnlCards);
        scrCuon2.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrCuon2.getViewport().setBackground(UIStyle.LIGHT_BLUE);
        add(scrCuon2, BorderLayout.CENTER);
        btnFind.addActionListener(e -> napLai());
        txtSearch.addActionListener(e -> napLai());
        cmbPet.addActionListener(e -> napLai());
        cmbAge.addActionListener(e -> napLai());
        cmbSize.addActionListener(e -> napLai());
        napLai();
    }
    public void napLai() {
        pnlCards.removeAll();
        try {
            String kind = (String) cmbPet.getSelectedItem();
            List<SanPham> products = SanPhamControl.timSanPhamGoiYThucAn(kind, (String) cmbAge.getSelectedItem(), (String) cmbSize.getSelectedItem());
            String keyword = txtSearch.getText().trim().toLowerCase(Locale.ROOT);
            int count = 0;
            for (SanPham product : products) {
                if (product.getTonKho() <= 0)
                    continue;
                String name = product.getTenSanPham() == null ? "" : product.getTenSanPham().toLowerCase(Locale.ROOT);
                if (!keyword.isEmpty() && !name.contains(keyword))
                    continue;
                pnlCards.add(taoTheGoiYThucAn(product));
                count++;
            }
            lblNote.setText(count == 0
                    ? "Không có sản phẩm thức ăn còn hàng phù hợp với loài thú cưng đã chọn."
                    : count + " sản phẩm thức ăn còn hàng. Độ tuổi/kích thước là tiêu chí tham khảo; hãy kiểm tra nhãn bao bì.");
        } catch (Exception ex) {
            lblNote.setText("Không tải được sản phẩm: " + ex.getMessage());
        }
        btnSelectedButton.setText("Giỏ hàng (" + tinhTongSoLuongTrongGio() + ")");
        pnlCards.revalidate();
        pnlCards.repaint();
    }
    private JPanel taoTheGoiYThucAn(SanPham product) {
        JPanel pnlCard = new JPanel(new BorderLayout(7, 7));
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(205, 218, 232)), new EmptyBorder(10, 10, 10, 10)));
        JLabel lblImage = new JLabel(layAnhGoiYThucAn(product), SwingConstants.CENTER);
        lblImage.setPreferredSize(new Dimension(150, 115));
        lblImage.setOpaque(true);
        lblImage.setBackground(new Color(246, 249, 252));
        pnlCard.add(lblImage, BorderLayout.NORTH);
        String details = "<html><b>" + escape(product.getTenSanPham()) + "</b><br>"
                + escape(product.getTenDanhMuc()) + "<br><font color='#087b43'>"
                + UIStyle.tien(product.getGiaBan()) + " · Tồn "
                + product.getTonKho() + "</font></html>";
        JLabel lblDescription = new JLabel(details);
        lblDescription.setFont(UIStyle.font(Font.PLAIN, 13));
        pnlCard.add(lblDescription, BorderLayout.CENTER);
        ChiTietHoaDon daThem = timSanPhamTrongGioGoiY(product.getMaSanPham());
        JButton btnAdd = UIStyle.nutXanh(daThem == null ? "Thêm vào giỏ hàng"
                : "Đã thêm · SL " + daThem.getSoLuong() + " · thêm tiếp");
        btnAdd.addActionListener(e -> themSanPhamGoiYVaoGio(product));
        pnlCard.add(btnAdd, BorderLayout.SOUTH);
        return pnlCard;
    }
    private ChiTietHoaDon timSanPhamTrongGioGoiY(String maSanPham) {
        for (ChiTietHoaDon item : TrangChuControl.gioHang()) {
            if (maSanPham.equals(item.getMaSanPham()))
                return item;
        }
        return null;
    }
    private int tinhTongSoLuongTrongGio() {
        return TrangChuControl.gioHang().stream().mapToInt(ChiTietHoaDon::getSoLuong).sum();
    }
    private void themSanPhamGoiYVaoGio(SanPham product) {
        ChiTietHoaDon item = timSanPhamTrongGioGoiY(product.getMaSanPham());
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
            TrangChuControl.gioHang().add(ChiTietHoaDon.sanPham(
                    product.getMaSanPham(), product.getTenSanPham(), product.getGiaBan()));
        }
        napLai();
    }
    private Icon layAnhGoiYThucAn(SanPham product) {
        String maSanPham = product == null ? null : product.getMaSanPham();
        if (maSanPham == null || !maSanPham.matches("[A-Za-z0-9_-]+")) {
            return new TextIcon("Chưa có ảnh sản phẩm");
        }
        Icon icon = anhSanPhamCache.get(maSanPham);
        if (icon != null) return icon;
        icon = taiAnhSanPhamTheoMa(maSanPham);
        if (icon == null) return new TextIcon("Chưa có ảnh sản phẩm");
        anhSanPhamCache.put(maSanPham, icon);
        return icon;
    }
    private Icon taiAnhSanPhamTheoMa(String maSanPham) {
        for (String extension : new String[] { ".png", ".jpg", ".jpeg" }) {
            String tenFile = maSanPham + extension;
            ImageIcon nguon = null;
            URL resource = GoiYThucAnUI.class.getResource("/img/products/" + tenFile);
            if (resource != null) {
                nguon = new ImageIcon(resource);
            }
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
        return null;
    }
    private String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    private void moDanhSachSanPhamDaChon() {
        List<ChiTietHoaDon> hangTrongBang = new java.util.ArrayList<>(TrangChuControl.gioHang());
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
                    JOptionPane.showMessageDialog(GoiYThucAnUI.this,
                            "Số lượng phải là số nguyên lớn hơn 0.", "Số lượng không hợp lệ",
                            JOptionPane.WARNING_MESSAGE);
                    fireTableCellUpdated(row, column);
                    return;
                }
                ChiTietHoaDon item = hangTrongBang.get(row);
                if (item.getMaSanPham() != null) {
                    int tonBanDuoc = SanPhamControl.laySoLuongBanDuocSanPham(item.getMaSanPham());
                    if (tonBanDuoc < 0) {
                        JOptionPane.showMessageDialog(GoiYThucAnUI.this,
                                "Không kiểm tra được tồn bán được; số lượng chưa thay đổi.",
                                "Không kiểm tra được tồn kho", JOptionPane.ERROR_MESSAGE);
                        fireTableCellUpdated(row, column);
                        return;
                    }
                    if (quantity > tonBanDuoc) {
                        JOptionPane.showMessageDialog(GoiYThucAnUI.this,
                                "Số lượng yêu cầu (" + quantity + ") vượt tồn bán được (" + tonBanDuoc
                                        + "). Số lượng trong giỏ được giữ nguyên.",
                                "Không đủ tồn kho", JOptionPane.WARNING_MESSAGE);
                        fireTableCellUpdated(row, column);
                        return;
                    }
                }
                item.setSoLuong(quantity);
                super.setValueAt(quantity, row, column);
                super.setValueAt(UIStyle.tien(item.getThanhTien()), row, 4);
                napLai();
            }
        };
        for (ChiTietHoaDon item : hangTrongBang) {
            model.addRow(new Object[] { item.getMaHang(), item.getTenMatHang(), item.getSoLuong(),
                    UIStyle.tien(item.getDonGia()), UIStyle.tien(item.getThanhTien()) });
        }
        JTable tblDuLieu = new JTable(model);
        tblDuLieu.setToolTipText("Bấm đúp vào ô trong cột Số lượng để chỉnh sửa.");
        UIStyle.taoBang(tblDuLieu, 2, 3, 4);
        JButton btnRemove = UIStyle.nutXoa("Xóa sản phẩm khỏi giỏ");
        JButton btnCheckout = UIStyle.nutXanh("Chuyển sang thanh toán");
        btnCheckout.setEnabled(!TrangChuControl.gioHang().isEmpty());
        JButton btnClose = UIStyle.nutPhu("Đóng");
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.add(btnRemove);
        pnlActions.add(btnCheckout);
        pnlActions.add(btnClose);
        JPanel pnlContent = new JPanel(new BorderLayout(8, 8));
        pnlContent.setPreferredSize(new Dimension(760, 340));
        pnlContent.add(new JLabel("Bấm đúp vào cột Số lượng để nhập số lượng mới."), BorderLayout.NORTH);
        pnlContent.add(new JScrollPane(tblDuLieu), BorderLayout.CENTER);
        pnlContent.add(pnlActions, BorderLayout.SOUTH);
        Window winOwner = SwingUtilities.getWindowAncestor(this);
        JDialog winDialog = new JDialog(winOwner, "Giỏ hàng", Dialog.ModalityType.APPLICATION_MODAL);
        winDialog.setContentPane(pnlContent);
        btnRemove.addActionListener(e -> {
            if (tblDuLieu.isEditing() && !tblDuLieu.getCellEditor().stopCellEditing())
                return;
            int row = tblDuLieu.getSelectedRow();
            if (row < 0)
                return;
            int rowModel = tblDuLieu.convertRowIndexToModel(row);
            ChiTietHoaDon item = hangTrongBang.remove(rowModel);
            TrangChuControl.gioHang().remove(item);
            model.removeRow(rowModel);
            btnCheckout.setEnabled(!TrangChuControl.gioHang().isEmpty());
            napLai();
        });
        btnCheckout.addActionListener(e -> {
            if (tblDuLieu.isEditing() && !tblDuLieu.getCellEditor().stopCellEditing())
                return;
            winDialog.dispose();
            if (moThanhToan != null) {
                moThanhToan.run();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy màn hình thanh toán.",
                        "Giỏ hàng", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnClose.addActionListener(e -> {
            if (!tblDuLieu.isEditing() || tblDuLieu.getCellEditor().stopCellEditing()) winDialog.dispose();
        });
        winDialog.pack();
        winDialog.setLocationRelativeTo(this);
        winDialog.setVisible(true);
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
