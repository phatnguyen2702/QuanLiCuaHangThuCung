package UI;

import UI.TrangChuUI.UIStyle;
import dao.SanPhamDAO;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/** Gá»£i Ã½ thá»©c Äƒn Ä‘ang cÃ²n hÃ ng theo loÃ i thÃº cÆ°ng vÃ  danh má»¥c sáº£n pháº©m. */
public class GoiYThucAnUI extends JFrame implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
    }

    // cmbPet: Há»™p lá»±a chá»n thÃº cÆ°ng.
    private final JComboBox<String> cmbPet = new JComboBox<>(new String[] { "ChÃ³", "MÃ¨o" });
    // cmbAge: Há»™p lá»±a chá»n tuá»•i.
    private final JComboBox<String> cmbAge = new JComboBox<>(
            new String[] { "Táº¥t cáº£ Ä‘á»™ tuá»•i", "Con non", "TrÆ°á»Ÿng thÃ nh", "Cao tuá»•i" });
    // cmbSize: Há»™p lá»±a chá»n kÃ­ch thÆ°á»›c.
    private final JComboBox<String> cmbSize = new JComboBox<>(
            new String[] { "Táº¥t cáº£ kÃ­ch thÆ°á»›c", "Nhá»", "Trung bÃ¬nh", "Lá»›n" });
    // txtSearch: Ã” nháº­p/hiá»ƒn thá»‹ vÄƒn báº£n tÃ¬m kiáº¿m.
    private final JTextField txtSearch = new JTextField(15);
    // pnlCards: Khung chá»©a thÃ nh pháº§n giao diá»‡n cÃ¡c trang ná»™i dung.
    private final JPanel pnlCards = new JPanel(new GridLayout(0, 3, 12, 12));
    // lblNote: NhÃ£n hiá»ƒn thá»‹: Chá»n loáº¡i thÃº cÆ°ng Ä‘á»ƒ xem thá»©c Äƒn cÃ²n hÃ ng..
    private final JLabel lblNote = new JLabel("Chá»n loáº¡i thÃº cÆ°ng Ä‘á»ƒ xem thá»©c Äƒn cÃ²n hÃ ng.");
    // btnSelectedButton: NÃºt thao tÃ¡c nÃºt Ä‘ang Ä‘Æ°á»£c chá»n.
    private final JButton btnSelectedButton = UIStyle.nutPhu("Giá» hÃ ng (0)");
    // dao: Äá»‘i tÆ°á»£ng truy cáº­p dá»¯ liá»‡u sáº£n pháº©m.
    private final SanPhamDAO dao = new SanPhamDAO();
    private final Runnable moThanhToan;
    // products: LÆ°u danh sÃ¡ch sáº£n pháº©m; pháº§n tá»­ cÃ³ kiá»ƒu SanPham.
    private List<SanPham> products = List.of();
    // anhSanPhamCache: Báº£ng Ã¡nh xáº¡ khÃ³a Ä‘áº¿n giÃ¡ trá»‹ dÃ¹ng cho bá»™ nhá»› Ä‘á»‡m áº£nh sáº£n
    // pháº©m.
    private final Map<String, Icon> anhSanPhamCache = new HashMap<>();

    // Khá»Ÿi táº¡o giao diá»‡n vÃ  thiáº¿t láº­p cÃ¡c thÃ nh pháº§n, sá»± kiá»‡n ban Ä‘áº§u.
    public GoiYThucAnUI() {
        this(null);
    }

    public GoiYThucAnUI(Runnable moThanhToan) {
        this.moThanhToan = moThanhToan;
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(UIStyle.LIGHT_BLUE);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(22, 25, 22, 25));

        // lblTitle2: NhÃ£n hiá»ƒn thá»‹: Gá»£i Ã½ thá»©c Äƒn theo loáº¡i thÃº cÆ°ng.
        JLabel lblTitle2 = new JLabel("Gá»£i Ã½ thá»©c Äƒn theo loáº¡i thÃº cÆ°ng");
        lblTitle2.setFont(UIStyle.font(Font.BOLD, 24));
        // pnlHeader2: Khung chá»©a thÃ nh pháº§n giao diá»‡n Ä‘áº§u trang.
        JPanel pnlHeader2 = new JPanel(new BorderLayout());
        pnlHeader2.setOpaque(false);
        pnlHeader2.add(lblTitle2, BorderLayout.WEST);
        pnlHeader2.add(btnSelectedButton, BorderLayout.EAST);
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        btnSelectedButton.addActionListener(e -> moDanhSachDaChon());

        // pnlFilters: Khung chá»©a thÃ nh pháº§n giao diá»‡n cÃ¡c bá»™ lá»c.
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnlFilters.setBackground(Color.WHITE);
        pnlFilters.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(206, 222, 237)), new EmptyBorder(8, 10, 8, 10)));
        pnlFilters.add(new JLabel("Loáº¡i thÃº cÆ°ng:"));
        pnlFilters.add(cmbPet);
        pnlFilters.add(new JLabel("Äá»™ tuá»•i:"));
        pnlFilters.add(cmbAge);
        pnlFilters.add(new JLabel("KÃ­ch thÆ°á»›c:"));
        pnlFilters.add(cmbSize);
        pnlFilters.add(new JLabel("TÃªn sáº£n pháº©m:"));
        pnlFilters.add(txtSearch);
        // btnFind: NÃºt thao tÃ¡c tÃ¬m.
        JButton btnFind = UIStyle.nutChinh("TÃ¬m kiáº¿m");
        pnlFilters.add(btnFind);

        // pnlTop2: Khung chá»©a thÃ nh pháº§n giao diá»‡n trÃªn.
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
        // scrCuon2: Khung cuá»™n khung cuá»™n.
        JScrollPane scrCuon2 = new JScrollPane(pnlCards);
        scrCuon2.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrCuon2.getViewport().setBackground(UIStyle.LIGHT_BLUE);
        add(scrCuon2, BorderLayout.CENTER);

        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        btnFind.addActionListener(e -> napLai());
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        txtSearch.addActionListener(e -> napLai());
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        cmbPet.addActionListener(e -> napLai());
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        cmbAge.addActionListener(e -> napLai());
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        cmbSize.addActionListener(e -> napLai());
        napLai();
    }

    // Náº¡p láº¡i dá»¯ liá»‡u vÃ  cáº­p nháº­t ná»™i dung mÃ n hÃ¬nh.
    public void napLai() {
        pnlCards.removeAll();
        try {
            String kind = (String) cmbPet.getSelectedItem();
            products = dao.goiYThucAn(kind, (String) cmbAge.getSelectedItem(), (String) cmbSize.getSelectedItem());
            // keyword: Äá»c ná»™i dung chá»¯ tá»« Ã´ nháº­p Ä‘á»ƒ xá»­ lÃ½ tá»« khÃ³a tÃ¬m kiáº¿m.
            String keyword = txtSearch.getText().trim().toLowerCase(Locale.ROOT);
            // count: LÆ°u sá»‘ pháº§n tá»­.
            int count = 0;
            for (SanPham product : products) {
                if (product.getTonKho() <= 0)
                    continue;
                // name: LÆ°u tÃªn.
                String name = product.getTenSanPham() == null ? "" : product.getTenSanPham().toLowerCase(Locale.ROOT);
                if (!keyword.isEmpty() && !name.contains(keyword))
                    continue;
                pnlCards.add(taoTheSanPham(product));
                count++;
            }
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            lblNote.setText(count == 0
                    ? "KhÃ´ng cÃ³ sáº£n pháº©m thá»©c Äƒn cÃ²n hÃ ng phÃ¹ há»£p vá»›i loÃ i thÃº cÆ°ng Ä‘Ã£ chá»n."
                    : count + " sáº£n pháº©m thá»©c Äƒn cÃ²n hÃ ng. Äá»™ tuá»•i/kÃ­ch thÆ°á»›c lÃ  tiÃªu chÃ­ tham kháº£o; hÃ£y kiá»ƒm tra nhÃ£n bao bÃ¬.");
        } catch (SQLException ex) {
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            lblNote.setText("KhÃ´ng táº£i Ä‘Æ°á»£c sáº£n pháº©m: " + ex.getMessage());
        }
        // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
        btnSelectedButton.setText("Giá» hÃ ng (" + tongSoLuongTrongGio() + ")");
        pnlCards.revalidate();
        pnlCards.repaint();
    }

    // Táº¡o tháº» sáº£n pháº©m.
    // Tham sá»‘ product: sáº£n pháº©m.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu JPanel.
    private JPanel taoTheSanPham(SanPham product) {
        // pnlCard: Khung chá»©a thÃ nh pháº§n giao diá»‡n tháº».
        JPanel pnlCard = new JPanel(new BorderLayout(7, 7));
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(205, 218, 232)), new EmptyBorder(10, 10, 10, 10)));

        // lblImage: NhÃ£n hiá»ƒn thá»‹ hÃ¬nh áº£nh.
        JLabel lblImage = new JLabel(anhSanPham(product), SwingConstants.CENTER);
        lblImage.setPreferredSize(new Dimension(150, 115));
        lblImage.setOpaque(true);
        lblImage.setBackground(new Color(246, 249, 252));
        pnlCard.add(lblImage, BorderLayout.NORTH);

        // details: LÆ°u cÃ¡c dÃ²ng chi tiáº¿t.
        String details = "<html><b>" + escape(product.getTenSanPham()) + "</b><br>"
                + escape(product.getTenDanhMuc()) + "<br><font color='#087b43'>"
                + String.format("%,.0f Ä‘", product.getGiaBan()) + " Â· Tá»“n "
                + product.getTonKho() + "</font></html>";
        // lblDescription: NhÃ£n hiá»ƒn thá»‹ mÃ´ táº£.
        JLabel lblDescription = new JLabel(details);
        lblDescription.setFont(UIStyle.font(Font.PLAIN, 13));
        pnlCard.add(lblDescription, BorderLayout.CENTER);

        entity.ChiTietHoaDon daThem = timTrongGio(product.getMaSanPham());
        // btnAdd: NÃºt thao tÃ¡c thÃªm.
        JButton btnAdd = UIStyle.nutXanh(daThem == null ? "ThÃªm vÃ o giá» hÃ ng"
                : "ÄÃ£ thÃªm Â· SL " + daThem.getSoLuong() + " Â· thÃªm tiáº¿p");
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        btnAdd.addActionListener(e -> themVaoGio(product));
        pnlCard.add(btnAdd, BorderLayout.SOUTH);
        return pnlCard;
    }

    // TÃ¬m trong giá».
    // Tham sá»‘ maSanPham: mÃ£ sáº£n pháº©m.
    // Kết quả: mặt hàng trong giỏ có mã sản phẩm khớp.
    private entity.ChiTietHoaDon timTrongGio(String maSanPham) {
        for (entity.ChiTietHoaDon item : control.PhienLamViecControl.gioHang()) {
            if (maSanPham.equals(item.getMaSanPham()))
                return item;
        }
        return null;
    }

    // Cá»™ng sá»‘ lÆ°á»£ng cÃ¡c máº·t hÃ ng trong giá».
    // Káº¿t quáº£: giÃ¡ trá»‹ sá»‘ nguyÃªn.
    private int tongSoLuongTrongGio() {
        return control.PhienLamViecControl.gioHang().stream().mapToInt(entity.ChiTietHoaDon::getSoLuong).sum();
    }

    // ThÃªm vÃ o giá».
    // Tham sá»‘ product: sáº£n pháº©m.
    private void themVaoGio(SanPham product) {
        entity.ChiTietHoaDon item = timTrongGio(product.getMaSanPham());
        if (item != null) {
            if (item.getSoLuong() >= product.getTonKho()) {
                JOptionPane.showMessageDialog(this, "Sá»‘ lÆ°á»£ng trong giá» Ä‘Ã£ báº±ng tá»“n kho hiá»‡n cÃ³.",
                        "KhÃ´ng Ä‘á»§ tá»“n kho", JOptionPane.WARNING_MESSAGE);
                return;
            }
            item.setSoLuong(item.getSoLuong() + 1);
        } else {
            if (product.getTonKho() < 1)
                return;
            control.PhienLamViecControl.gioHang().add(entity.ChiTietHoaDon.sanPham(
                    product.getMaSanPham(), product.getTenSanPham(), product.getGiaBan()));
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
        // maSanPham: LÆ°u mÃ£ sáº£n pháº©m.
        String maSanPham = product == null ? null : product.getMaSanPham();
        if (maSanPham == null || !maSanPham.matches("[A-Za-z0-9_-]+")) {
            return new TextIcon("ChÆ°a cÃ³ áº£nh sáº£n pháº©m");
        }
        return anhSanPhamCache.computeIfAbsent(maSanPham, this::taiAnhTheoMa);
    }

    // Táº£i áº£nh theo mÃ£.
    // Tham sá»‘ maSanPham: mÃ£ sáº£n pháº©m.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu Icon.
    private Icon taiAnhTheoMa(String maSanPham) {
        for (String extension : new String[] { ".png", ".jpg", ".jpeg" }) {
            String tenFile = maSanPham + extension;
            ImageIcon nguon = null;

            URL resource = GoiYThucAnUI.class.getResource("/img/products/" + tenFile);
            if (resource != null) {
                nguon = new ImageIcon(resource);
            }

            // Khi chay Java truc tiep tu thu muc project (khong dong goi JAR).
            if (nguon == null || nguon.getIconWidth() <= 0) {
                // file: ÄÆ°á»ng dáº«n tá»‡p dÃ¹ng cho tá»‡p.
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

                // ve: áº¢nh trong bá»™ nhá»› dÃ¹ng cho vá».
                BufferedImage ve = new BufferedImage(rongKhung, caoKhung, BufferedImage.TYPE_INT_ARGB);
                // g: Ngá»¯ cáº£nh váº½ dÃ¹ng Ä‘á»ƒ thiáº¿t láº­p mÃ u, phÃ´ng vÃ  váº½ ná»™i dung.
                Graphics2D g = ve.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(nguon.getImage(), (rongKhung - rongAnh) / 2,
                        (caoKhung - caoAnh) / 2, rongAnh, caoAnh, null);
                g.dispose();
                return new ImageIcon(ve);
            }
        }
        return new TextIcon("ChÆ°a cÃ³ áº£nh sáº£n pháº©m");
    }

    // MÃ£ hÃ³a cÃ¡c kÃ½ tá»± Ä‘áº·c biá»‡t trÆ°á»›c khi hiá»ƒn thá»‹ chuá»—i trong HTML.
    // Tham sá»‘ value: giÃ¡ trá»‹.
    // Káº¿t quáº£: chuá»—i káº¿t quáº£.
    private String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // Má»Ÿ danh sÃ¡ch Ä‘Ã£ chá»n.
    private void moDanhSachDaChon() {
        // model: MÃ´ hÃ¬nh lÆ°u dá»¯ liá»‡u vÃ  cung cáº¥p cÃ¡c Ã´ cho báº£ng giao diá»‡n.
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "MÃ£ SP", "TÃªn sáº£n pháº©m", "Sá»‘ lÆ°á»£ng", "ÄÆ¡n giÃ¡", "ThÃ nh tiá»n" }, 0) {
            // Quy Ä‘á»‹nh Ã´ cá»§a báº£ng cÃ³ Ä‘Æ°á»£c chá»‰nh sá»­a trá»±c tiáº¿p hay khÃ´ng.
            // Tham sá»‘ row: chá»‰ sá»‘ dÃ²ng.
            // Tham sá»‘ column: chá»‰ sá»‘ cá»™t.
            // Káº¿t quáº£: true/false theo Ä‘iá»u kiá»‡n xá»­ lÃ½.
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }

            // Cáº­p nháº­t giÃ¡ trá»‹ Ä‘Æ°á»£c nháº­p vÃ o Ã´ cá»§a báº£ng.
            // Tham sá»‘ value: giÃ¡ trá»‹ táº¡i má»›i.
            // Tham sá»‘ row: chá»‰ sá»‘ dÃ²ng.
            // Tham sá»‘ column: chá»‰ sá»‘ cá»™t.
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
                            "Sá»‘ lÆ°á»£ng pháº£i lÃ  sá»‘ nguyÃªn lá»›n hÆ¡n 0.", "Sá»‘ lÆ°á»£ng khÃ´ng há»£p lá»‡",
                            JOptionPane.WARNING_MESSAGE);
                    fireTableCellUpdated(row, column);
                    return;
                }

                String code = String.valueOf(getValueAt(row, 0));
                for (entity.ChiTietHoaDon item : control.PhienLamViecControl.gioHang()) {
                    if (code.equals(item.getMaSanPham())) {
                        final int tonKho;
                        try {
                            tonKho = dao.layTonKho(code);
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(GoiYThucAnUI.this,
                                    "KhÃ´ng kiá»ƒm tra Ä‘Æ°á»£c tá»“n kho; sá»‘ lÆ°á»£ng chÆ°a thay Ä‘á»•i.\n" + ex.getMessage(),
                                    "KhÃ´ng kiá»ƒm tra Ä‘Æ°á»£c tá»“n kho", JOptionPane.ERROR_MESSAGE);
                            fireTableCellUpdated(row, column);
                            return;
                        }
                        if (quantity > tonKho) {
                            JOptionPane.showMessageDialog(GoiYThucAnUI.this,
                                    "Sá»‘ lÆ°á»£ng yÃªu cáº§u (" + quantity + ") vÆ°á»£t tá»“n hiá»‡n táº¡i (" + tonKho
                                            + "). Sá»‘ lÆ°á»£ng trong giá» Ä‘Æ°á»£c giá»¯ nguyÃªn.",
                                    "KhÃ´ng Ä‘á»§ tá»“n kho", JOptionPane.WARNING_MESSAGE);
                            fireTableCellUpdated(row, column);
                            return;
                        }
                        item.setSoLuong(quantity);
                        super.setValueAt(quantity, row, column);
                        super.setValueAt(String.format("%,.0f Ä‘", item.getThanhTien()), row, 4);
                        // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
                        btnSelectedButton.setText("Giá» hÃ ng (" + tongSoLuongTrongGio() + ")");
                        napLai();
                        return;
                    }
                }
                fireTableCellUpdated(row, column);
            }
        };
        for (entity.ChiTietHoaDon item : control.PhienLamViecControl.gioHang()) {
            model.addRow(new Object[] { item.getMaHang(), item.getTenMatHang(), item.getSoLuong(),
                    String.format("%,.0f Ä‘", item.getDonGia()),
                    String.format("%,.0f Ä‘", item.getThanhTien()) });
        }
        // tblDuLieu: Báº£ng hiá»ƒn thá»‹ cÃ¡c dÃ²ng do mÃ´ hÃ¬nh dá»¯ liá»‡u cung cáº¥p.
        JTable tblDuLieu = new JTable(model);
        tblDuLieu.setToolTipText("Báº¥m Ä‘Ãºp vÃ o Ã´ trong cá»™t Sá»‘ lÆ°á»£ng Ä‘á»ƒ chá»‰nh sá»­a.");
        UIStyle.taoBang(tblDuLieu, 1, 2, 3);
        // btnRemove: NÃºt thao tÃ¡c xÃ³a.
        JButton btnRemove = UIStyle.nutXoa("XÃ³a sáº£n pháº©m khá»i giá»");
        // btnCheckout: NÃºt thao tÃ¡c thanh toÃ¡n.
        JButton btnCheckout = UIStyle.nutXanh("Chuyá»ƒn sang thanh toÃ¡n");
        btnCheckout.setEnabled(!control.PhienLamViecControl.gioHang().isEmpty());
        // btnClose: NÃºt thao tÃ¡c Ä‘Ã³ng.
        JButton btnClose = UIStyle.nutPhu("ÄÃ³ng");
        // pnlActions: Khung chá»©a thÃ nh pháº§n giao diá»‡n thao tÃ¡c.
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.add(btnRemove);
        pnlActions.add(btnCheckout);
        pnlActions.add(btnClose);
        // pnlContent: Khung chá»©a thÃ nh pháº§n giao diá»‡n ná»™i dung.
        JPanel pnlContent = new JPanel(new BorderLayout(8, 8));
        pnlContent.setPreferredSize(new Dimension(760, 340));
        pnlContent.add(new JLabel("Báº¥m Ä‘Ãºp vÃ o cá»™t Sá»‘ lÆ°á»£ng Ä‘á»ƒ nháº­p sá»‘ lÆ°á»£ng má»›i."), BorderLayout.NORTH);
        pnlContent.add(new JScrollPane(tblDuLieu), BorderLayout.CENTER);
        pnlContent.add(pnlActions, BorderLayout.SOUTH);
        Window winOwner = SwingUtilities.getWindowAncestor(this);
        // winDialog: Há»™p thoáº¡i há»™p thoáº¡i.
        JDialog winDialog = new JDialog(winOwner, "Giá» hÃ ng", Dialog.ModalityType.APPLICATION_MODAL);
        winDialog.setContentPane(pnlContent);
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        btnRemove.addActionListener(e -> {
            if (tblDuLieu.isEditing() && !tblDuLieu.getCellEditor().stopCellEditing())
                return;
            // row: LÆ°u chá»‰ sá»‘ dÃ²ng.
            int row = tblDuLieu.getSelectedRow();
            if (row < 0)
                return;
            // maSanPham: LÆ°u mÃ£ sáº£n pháº©m.
            String maSanPham = String.valueOf(model.getValueAt(tblDuLieu.convertRowIndexToModel(row), 0));
            control.PhienLamViecControl.gioHang().removeIf(item -> maSanPham.equals(item.getMaSanPham()));
            model.removeRow(tblDuLieu.convertRowIndexToModel(row));
            btnCheckout.setEnabled(!control.PhienLamViecControl.gioHang().isEmpty());
            napLai();
        });
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        btnCheckout.addActionListener(e -> {
            if (tblDuLieu.isEditing() && !tblDuLieu.getCellEditor().stopCellEditing())
                return;
            winDialog.dispose();
            if (moThanhToan != null) {
                moThanhToan.run();
            } else if (winOwner instanceof TrangChuUI) {
                ((TrangChuUI) winOwner).moThanhToanTuGioHang();
            } else {
                JOptionPane.showMessageDialog(this, "KhÃ´ng tÃ¬m tháº¥y mÃ n hÃ¬nh thanh toÃ¡n.",
                        "Giá» hÃ ng", JOptionPane.ERROR_MESSAGE);
            }
        });
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        btnClose.addActionListener(e -> winDialog.dispose());
        winDialog.pack();
        winDialog.setLocationRelativeTo(this);
        winDialog.setVisible(true);
    }

    // NhÃ³m dá»¯ liá»‡u hoáº·c chá»©c nÄƒng chá»¯ biá»ƒu tÆ°á»£ng dÃ¹ng trong GoiYThucAnPanel.
    private static final class TextIcon implements Icon {
        // text: LÆ°u ná»™i dung chá»¯.
        private final String text;

        // Khá»Ÿi táº¡o Ä‘á»‘i tÆ°á»£ng tá»« cÃ¡c giÃ¡ trá»‹ Ä‘Æ°á»£c truyá»n vÃ o.
        // Tham sá»‘ text: ná»™i dung chá»¯.
        private TextIcon(String text) {
            this.text = text;
        }

        // Tráº£ vá» chiá»u rá»™ng cá»§a biá»ƒu tÆ°á»£ng.
        @Override
        public int getIconWidth() {
            return 150;
        }

        // Tráº£ vá» chiá»u cao cá»§a biá»ƒu tÆ°á»£ng.
        @Override
        public int getIconHeight() {
            return 110;
        }

        // Váº½ biá»ƒu tÆ°á»£ng táº¡i vá»‹ trÃ­ Ä‘Æ°á»£c yÃªu cáº§u.
        // Tham sá»‘ component: thÃ nh pháº§n giao diá»‡n.
        // Tham sá»‘ graphics: ngá»¯ cáº£nh váº½.
        // Tham sá»‘ x: tá»a Ä‘á»™ ngang.
        // Tham sá»‘ y: tá»a Ä‘á»™ dá»c.
        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            // g: Ngá»¯ cáº£nh váº½ dÃ¹ng Ä‘á»ƒ thiáº¿t láº­p mÃ u, phÃ´ng vÃ  váº½ ná»™i dung.
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
