package UI;

import UI.TrangChu.UIStyle;
import dao.SanPhamDAO;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/** Gợi ý thức ăn đang còn hàng theo loài thú cưng và danh mục sản phẩm. */
public class GoiYThucAnPanel extends JFrame implements ActionListener, MouseListener {
    @Override public void actionPerformed(ActionEvent e) { }
    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mousePressed(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
    // cmbPet: Hộp lựa chọn thú cưng.
    private final JComboBox<String> cmbPet = new JComboBox<>(new String[] { "Chó", "Mèo" });
    // cmbAge: Hộp lựa chọn tuổi.
    private final JComboBox<String> cmbAge = new JComboBox<>(
            new String[] { "Tất cả độ tuổi", "Con non", "Trưởng thành", "Cao tuổi" });
    // cmbSize: Hộp lựa chọn kích thước.
    private final JComboBox<String> cmbSize = new JComboBox<>(
            new String[] { "Tất cả kích thước", "Nhỏ", "Trung bình", "Lớn" });
    // txtSearch: Ô nhập/hiển thị văn bản tìm kiếm.
    private final JTextField txtSearch = new JTextField(15);
    // pnlCards: Khung chứa thành phần giao diện các trang nội dung.
    private final JPanel pnlCards = new JPanel(new GridLayout(0, 3, 12, 12));
    // lblNote: Nhãn hiển thị: Chọn loại thú cưng để xem thức ăn còn hàng..
    private final JLabel lblNote = new JLabel("Chọn loại thú cưng để xem thức ăn còn hàng.");
    // btnSelectedButton: Nút thao tác nút đang được chọn.
    private final JButton btnSelectedButton = UIStyle.nutPhu("Giỏ hàng (0)");
    // dao: Đối tượng truy cập dữ liệu sản phẩm.
    private final SanPhamDAO dao = new SanPhamDAO();
    // products: Lưu danh sách sản phẩm; phần tử có kiểu SanPham.
    private List<SanPham> products = List.of();
    // anhSanPhamCache: Bảng ánh xạ khóa đến giá trị dùng cho bộ nhớ đệm ảnh sản phẩm.
    private final Map<String, Icon> anhSanPhamCache = new HashMap<>();

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    public GoiYThucAnPanel() {
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(UIStyle.LIGHT_BLUE);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(22, 25, 22, 25));

        // lblTitle2: Nhãn hiển thị: Gợi ý thức ăn theo loại thú cưng.
        JLabel lblTitle2 = new JLabel("Gợi ý thức ăn theo loại thú cưng");
        lblTitle2.setFont(UIStyle.font(Font.BOLD, 24));
        // pnlHeader2: Khung chứa thành phần giao diện đầu trang.
        JPanel pnlHeader2 = new JPanel(new BorderLayout());
        pnlHeader2.setOpaque(false);
        pnlHeader2.add(lblTitle2, BorderLayout.WEST);
        pnlHeader2.add(btnSelectedButton, BorderLayout.EAST);
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnSelectedButton.addActionListener(e -> moDanhSachDaChon());

        // pnlFilters: Khung chứa thành phần giao diện các bộ lọc.
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
        // btnFind: Nút thao tác tìm.
        JButton btnFind = UIStyle.nutChinh("Tìm kiếm");
        pnlFilters.add(btnFind);

        // pnlTop2: Khung chứa thành phần giao diện trên.
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
        // scrCuon2: Khung cuộn khung cuộn.
        JScrollPane scrCuon2 = new JScrollPane(pnlCards);
        scrCuon2.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrCuon2.getViewport().setBackground(UIStyle.LIGHT_BLUE);
        add(scrCuon2, BorderLayout.CENTER);

        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnFind.addActionListener(e -> napLai());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        txtSearch.addActionListener(e -> napLai());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbPet.addActionListener(e -> napLai());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbAge.addActionListener(e -> napLai());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbSize.addActionListener(e -> napLai());
        napLai();
    }

    // Nạp lại dữ liệu và cập nhật nội dung màn hình.
    public void napLai() {
        pnlCards.removeAll();
        try {
            String kind = (String) cmbPet.getSelectedItem();
            products = dao.goiYThucAn(kind, (String) cmbAge.getSelectedItem(), (String) cmbSize.getSelectedItem());
            // keyword: Đọc nội dung chữ từ ô nhập để xử lý từ khóa tìm kiếm.
            String keyword = txtSearch.getText().trim().toLowerCase(Locale.ROOT);
            // count: Lưu số phần tử.
            int count = 0;
            for (SanPham product : products) {
                if (product.getTonKho() <= 0)
                    continue;
                // name: Lưu tên.
                String name = product.getTenSanPham() == null ? "" : product.getTenSanPham().toLowerCase(Locale.ROOT);
                if (!keyword.isEmpty() && !name.contains(keyword))
                    continue;
                pnlCards.add(taoTheSanPham(product));
                count++;
            }
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblNote.setText(count == 0
                    ? "Không có sản phẩm thức ăn còn hàng phù hợp với loài thú cưng đã chọn."
                    : count + " sản phẩm thức ăn còn hàng. Độ tuổi/kích thước là tiêu chí tham khảo; hãy kiểm tra nhãn bao bì.");
        } catch (Exception ex) {
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblNote.setText("Không tải được sản phẩm: " + ex.getMessage());
        }
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        btnSelectedButton.setText("Giỏ hàng (" + tongSoLuongTrongGio() + ")");
        pnlCards.revalidate();
        pnlCards.repaint();
    }

    // Tạo thẻ sản phẩm.
    // Tham số product: sản phẩm.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoTheSanPham(SanPham product) {
        // pnlCard: Khung chứa thành phần giao diện thẻ.
        JPanel pnlCard = new JPanel(new BorderLayout(7, 7));
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(205, 218, 232)), new EmptyBorder(10, 10, 10, 10)));

        // lblImage: Nhãn hiển thị hình ảnh.
        JLabel lblImage = new JLabel(anhSanPham(product), SwingConstants.CENTER);
        lblImage.setPreferredSize(new Dimension(150, 115));
        lblImage.setOpaque(true);
        lblImage.setBackground(new Color(246, 249, 252));
        pnlCard.add(lblImage, BorderLayout.NORTH);

        // details: Lưu các dòng chi tiết.
        String details = "<html><b>" + escape(product.getTenSanPham()) + "</b><br>"
                + escape(product.getTenDanhMuc()) + "<br><font color='#087b43'>"
                + String.format("%,.0f đ", product.getGiaBan()) + " · Tồn "
                + product.getTonKho() + "</font></html>";
        // lblDescription: Nhãn hiển thị mô tả.
        JLabel lblDescription = new JLabel(details);
        lblDescription.setFont(UIStyle.font(Font.PLAIN, 13));
        pnlCard.add(lblDescription, BorderLayout.CENTER);

        entity.ThanhToan daThem = timTrongGio(product.getMaSanPham());
        // btnAdd: Nút thao tác thêm.
        JButton btnAdd = UIStyle.nutXanh(daThem == null ? "Thêm vào giỏ hàng"
                : "Đã thêm · SL " + daThem.getSoLuong() + " · thêm tiếp");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnAdd.addActionListener(e -> themVaoGio(product));
        pnlCard.add(btnAdd, BorderLayout.SOUTH);
        return pnlCard;
    }

    // Tìm trong giờ.
    // Tham số maSanPham: mã sản phẩm.
    // Kết quả: đối tượng/giá trị kiểu entity.ThanhToan.
    private entity.ThanhToan timTrongGio(String maSanPham) {
        for (entity.ThanhToan item : UI.PhienLamViec.gioHang()) {
            if (maSanPham.equals(item.getMaSanPham()))
                return item;
        }
        return null;
    }

    // Cộng số lượng các mặt hàng trong giỏ.
    // Kết quả: giá trị số nguyên.
    private int tongSoLuongTrongGio() {
        return UI.PhienLamViec.gioHang().stream().mapToInt(entity.ThanhToan::getSoLuong).sum();
    }

    // Thêm vào giờ.
    // Tham số product: sản phẩm.
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
        // maSanPham: Lưu mã sản phẩm.
        String maSanPham = product == null ? null : product.getMaSanPham();
        if (maSanPham == null || !maSanPham.matches("[A-Za-z0-9_-]+")) {
            return new TextIcon("Chưa có ảnh sản phẩm");
        }
        return anhSanPhamCache.computeIfAbsent(maSanPham, this::taiAnhTheoMa);
    }

    // Tải ảnh theo mã.
    // Tham số maSanPham: mã sản phẩm.
    // Kết quả: đối tượng/giá trị kiểu Icon.
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
                // file: Đường dẫn tệp dùng cho tệp.
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

                // ve: Ảnh trong bộ nhớ dùng cho về.
                BufferedImage ve = new BufferedImage(rongKhung, caoKhung, BufferedImage.TYPE_INT_ARGB);
                // g: Ngữ cảnh vẽ dùng để thiết lập màu, phông và vẽ nội dung.
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

    // Mã hóa các ký tự đặc biệt trước khi hiển thị chuỗi trong HTML.
    // Tham số value: giá trị.
    // Kết quả: chuỗi kết quả.
    private String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // Mở danh sách đã chọn.
    private void moDanhSachDaChon() {
        // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "Mã SP", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền" }, 0) {
            // Quy định ô của bảng có được chỉnh sửa trực tiếp hay không.
            // Tham số row: chỉ số dòng.
            // Tham số column: chỉ số cột.
            // Kết quả: true/false theo điều kiện xử lý.
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }

            // Cập nhật giá trị được nhập vào ô của bảng.
            // Tham số value: giá trị tại mới.
            // Tham số row: chỉ số dòng.
            // Tham số column: chỉ số cột.
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
                        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                        btnSelectedButton.setText("Giỏ hàng (" + tongSoLuongTrongGio() + ")");
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
        // tblDuLieu: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
        JTable tblDuLieu = new JTable(model);
        tblDuLieu.setToolTipText("Bấm đúp vào ô trong cột Số lượng để chỉnh sửa.");
        UIStyle.taoBang(tblDuLieu, 1, 2, 3);
        // btnRemove: Nút thao tác xóa.
        JButton btnRemove = UIStyle.nutXoa("Xóa sản phẩm khỏi giỏ");
        // btnCheckout: Nút thao tác thanh toán.
        JButton btnCheckout = UIStyle.nutXanh("Chuyển sang thanh toán");
        btnCheckout.setEnabled(!UI.PhienLamViec.gioHang().isEmpty());
        // btnClose: Nút thao tác đóng.
        JButton btnClose = UIStyle.nutPhu("Đóng");
        // pnlActions: Khung chứa thành phần giao diện thao tác.
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.add(btnRemove);
        pnlActions.add(btnCheckout);
        pnlActions.add(btnClose);
        // pnlContent: Khung chứa thành phần giao diện nội dung.
        JPanel pnlContent = new JPanel(new BorderLayout(8, 8));
        pnlContent.setPreferredSize(new Dimension(760, 340));
        pnlContent.add(new JLabel("Bấm đúp vào cột Số lượng để nhập số lượng mới."), BorderLayout.NORTH);
        pnlContent.add(new JScrollPane(tblDuLieu), BorderLayout.CENTER);
        pnlContent.add(pnlActions, BorderLayout.SOUTH);
        Window winOwner = SwingUtilities.getWindowAncestor(this);
        // winDialog: Hộp thoại hộp thoại.
        JDialog winDialog = new JDialog(winOwner, "Giỏ hàng", Dialog.ModalityType.APPLICATION_MODAL);
        winDialog.setContentPane(pnlContent);
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnRemove.addActionListener(e -> {
            if (tblDuLieu.isEditing() && !tblDuLieu.getCellEditor().stopCellEditing())
                return;
            // row: Lưu chỉ số dòng.
            int row = tblDuLieu.getSelectedRow();
            if (row < 0)
                return;
            // maSanPham: Lưu mã sản phẩm.
            String maSanPham = String.valueOf(model.getValueAt(tblDuLieu.convertRowIndexToModel(row), 0));
            UI.PhienLamViec.gioHang().removeIf(item -> maSanPham.equals(item.getMaSanPham()));
            model.removeRow(tblDuLieu.convertRowIndexToModel(row));
            btnCheckout.setEnabled(!UI.PhienLamViec.gioHang().isEmpty());
            napLai();
        });
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnCheckout.addActionListener(e -> {
            if (tblDuLieu.isEditing() && !tblDuLieu.getCellEditor().stopCellEditing())
                return;
            winDialog.dispose();
            if (winOwner instanceof TrangChu) {
                ((TrangChu) winOwner).moThanhToanTuGioHang();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy màn hình thanh toán.",
                        "Giỏ hàng", JOptionPane.ERROR_MESSAGE);
            }
        });
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnClose.addActionListener(e -> winDialog.dispose());
        winDialog.pack();
        winDialog.setLocationRelativeTo(this);
        winDialog.setVisible(true);
    }

    // Nhóm dữ liệu hoặc chức năng chữ biểu tượng dùng trong GoiYThucAnPanel.
    private static final class TextIcon implements Icon {
        // text: Lưu nội dung chữ.
        private final String text;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số text: nội dung chữ.
        private TextIcon(String text) {
            this.text = text;
        }

        // Trả về chiều rộng của biểu tượng.
        @Override
        public int getIconWidth() {
            return 150;
        }

        // Trả về chiều cao của biểu tượng.
        @Override
        public int getIconHeight() {
            return 110;
        }

        // Vẽ biểu tượng tại vị trí được yêu cầu.
        // Tham số component: thành phần giao diện.
        // Tham số graphics: ngữ cảnh vẽ.
        // Tham số x: tọa độ ngang.
        // Tham số y: tọa độ dọc.
        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            // g: Ngữ cảnh vẽ dùng để thiết lập màu, phông và vẽ nội dung.
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
