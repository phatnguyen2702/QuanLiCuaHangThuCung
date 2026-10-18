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
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Gợi ý thức ăn đang còn hàng theo loài thú cưng và danh mục sản phẩm. */
public class GoiYThucAnPanel extends JPanel {
    private final JComboBox<String> pet = new JComboBox<>(new String[]{"Chó", "Mèo", "Khác"});
    private final JComboBox<String> age = new JComboBox<>(new String[]{"Mọi độ tuổi", "Con non", "Trưởng thành", "Cao tuổi"});
    private final JComboBox<String> size = new JComboBox<>(new String[]{"Mọi kích thước", "Nhỏ", "Trung bình", "Lớn"});
    private final JTextField search = new JTextField(15);
    private final JPanel cards = new JPanel(new GridLayout(0, 3, 12, 12));
    private final JLabel note = new JLabel("Chọn loại thú cưng để xem thức ăn còn hàng.");
    private final JButton selectedButton = UIStyle.nutPhu("Danh sách đã chọn (0)");
    private final SanPhamDAO dao = new SanPhamDAO();
    private final Map<String, SanPham> selected = new LinkedHashMap<>();
    private List<SanPham> products = List.of();

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
            products = dao.getAll();
            String kind = (String) pet.getSelectedItem();
            String keyword = search.getText().trim().toLowerCase(Locale.ROOT);
            int count = 0;
            for (SanPham product : products) {
                if (product.getTonKho() <= 0 || !isFood(product) || !matchesPet(product, kind)) continue;
                String name = product.getTenSanPham() == null ? "" : product.getTenSanPham().toLowerCase(Locale.ROOT);
                if (!keyword.isEmpty() && !name.contains(keyword)) continue;
                cards.add(taoTheSanPham(product));
                count++;
            }
            note.setText(count == 0
                    ? "Không có sản phẩm thức ăn còn hàng phù hợp với loài thú cưng đã chọn."
                    : count + " sản phẩm thức ăn còn hàng. Độ tuổi/kích thước là tiêu chí tham khảo; hãy kiểm tra nhãn bao bì.");
        } catch (Exception ex) {
            note.setText("Không tải được sản phẩm: " + ex.getMessage());
        }
        selectedButton.setText("Danh sách đã chọn (" + selected.size() + ")");
        cards.revalidate();
        cards.repaint();
    }

    private boolean isFood(SanPham product) {
        String category = normalize(product.getTenDanhMuc());
        String name = normalize(product.getTenSanPham());
        return category.contains("thuc an") || category.contains("thuc-an")
                || name.contains("thuc an") || name.contains("hat ")
                || name.contains("pate") || name.contains("snack");
    }

    private boolean matchesPet(SanPham product, String selectedPet) {
        if ("Khác".equals(selectedPet)) return true;
        String category = normalize(product.getTenDanhMuc());
        String name = normalize(product.getTenSanPham());
        String text = category + " " + name;
        boolean dog = text.contains("cho cho") || text.contains("dog") || text.contains("puppy")
                || name.contains("cho ") || name.endsWith("cho");
        boolean cat = text.contains("cho meo") || text.contains("cat") || text.contains("kitten")
                || name.contains("meo");
        if (dog && cat) return true;
        return "Chó".equals(selectedPet) ? dog : cat;
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
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

        JButton add = UIStyle.nutXanh(selected.containsKey(product.getMaSanPham()) ? "Đã chọn" : "Thêm vào danh sách");
        add.setEnabled(!selected.containsKey(product.getMaSanPham()));
        add.addActionListener(e -> {
            selected.put(product.getMaSanPham(), product);
            napLai();
        });
        card.add(add, BorderLayout.SOUTH);
        return card;
    }

    private Icon anhSanPham(SanPham product) {
        String root = "src/img/products/" + product.getMaSanPham();
        for (String extension : new String[]{".jpg", ".jpeg", ".png"}) {
            File imageFile = new File(root + extension);
            if (imageFile.isFile()) {
                ImageIcon source = new ImageIcon(imageFile.getPath());
                Image scaled = source.getImage().getScaledInstance(145, 105, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        }
        return new TextIcon("Chưa có ảnh sản phẩm");
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void moDanhSachDaChon() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Mã SP", "Tên sản phẩm", "Danh mục", "Giá bán", "Tồn kho"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (SanPham product : selected.values()) {
            model.addRow(new Object[]{product.getMaSanPham(), product.getTenSanPham(), product.getTenDanhMuc(),
                    String.format("%,.0f đ", product.getGiaBan()), product.getTonKho()});
        }
        JTable table = new JTable(model);
        UIStyle.taoBang(table, 1, 2, 3);
        JButton remove = UIStyle.nutXoa("Xóa sản phẩm đã chọn");
        JButton close = UIStyle.nutPhu("Đóng");
        JPanel actions = UIStyle.hangNut(remove, close);
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setPreferredSize(new Dimension(700, 340));
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Danh sách sản phẩm đã chọn", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(content);
        remove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            selected.remove(String.valueOf(model.getValueAt(row, 0)));
            dialog.dispose();
            napLai();
            moDanhSachDaChon();
        });
        close.addActionListener(e -> dialog.dispose());
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static final class TextIcon implements Icon {
        private final String text;
        private TextIcon(String text) { this.text = text; }
        @Override public int getIconWidth() { return 150; }
        @Override public int getIconHeight() { return 110; }
        @Override public void paintIcon(Component component, Graphics graphics, int x, int y) {
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
