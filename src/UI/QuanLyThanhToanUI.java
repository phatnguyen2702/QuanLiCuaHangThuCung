package UI;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import dao.ThanhToanDao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

// Giao diá»‡n/chá»©c nÄƒng quáº£n lÃ½ thanh toÃ¡n cá»§a á»©ng dá»¥ng.
public class QuanLyThanhToanUI extends JFrame implements ActionListener, MouseListener {

    // NAVY: MÃ u dÃ¹ng cho giao diá»‡n: xanh Ä‘áº­m.
    private static final Color NAVY = new Color(18, 48, 82);
    // LIGHT_BLUE: MÃ u dÃ¹ng cho giao diá»‡n: xanh dÆ°Æ¡ng.
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);
    // GREEN: MÃ u dÃ¹ng cho giao diá»‡n: xanh lÃ¡.
    private static final Color GREEN = new Color(25, 157, 107);
    // BORDER: MÃ u dÃ¹ng cho giao diá»‡n: viá»n.
    private static final Color BORDER = new Color(210, 220, 230);
    // MONEY: Bá»™ Ä‘á»‹nh dáº¡ng sá»‘/tiá»n dÃ¹ng Ä‘á»ƒ hiá»ƒn thá»‹ dá»¯ liá»‡u trÃªn giao diá»‡n.
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(
            Locale.forLanguageTag("vi-VN"));

    // dao: Äá»‘i tÆ°á»£ng truy cáº­p dá»¯ liá»‡u thanh toÃ¡n.
    private final ThanhToanDao dao = new ThanhToanDao();
    // maNhanVien: LÆ°u mÃ£ nhÃ¢n viÃªn.
    private final String maNhanVien;
    // pnlNoiDung: Pháº§n giao diá»‡n Ä‘Æ°á»£c TrangChu nhÃºng vÃ o CardLayout.
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout(10, 12));
    // moLichSuHoaDon: HÃ nh Ä‘á»™ng Ä‘Æ°á»£c gá»i khi cáº§n má»Ÿ lá»‹ch sá»­ hÃ³a Ä‘Æ¡n.
    private final Runnable moLichSuHoaDon;
    // capNhatPhieuDichVu: HÃ nh Ä‘á»™ng Ä‘Æ°á»£c gá»i khi cáº§n cáº­p nháº­t danh sÃ¡ch phiáº¿u dá»‹ch
    // vá»¥.
    private final Runnable capNhatPhieuDichVu;
    // chiTiet: LÆ°u chi tiáº¿t; pháº§n tá»­ cÃ³ kiá»ƒu entity.ThanhToan.
    private final List<entity.ThanhToan> chiTiet = control.PhienLamViecControl.gioHang();
    // danhSachKhachHang: LÆ°u danh sÃ¡ch khÃ¡ch hÃ ng dÃ¹ng Ä‘á»ƒ gá»£i Ã½.
    private final List<entity.ThanhToan.KhachHang> danhSachKhachHang = new ArrayList<>();
    // danhSachMaSanPham: LÆ°u danh sÃ¡ch mÃ£ sáº£n pháº©m; pháº§n tá»­ cÃ³ kiá»ƒu String.
    private final List<String> danhSachMaSanPham = new ArrayList<>();
    // tenSanPhamTheoMa: Báº£ng Ã¡nh xáº¡ khÃ³a Ä‘áº¿n giÃ¡ trá»‹ dÃ¹ng cho Ã¡nh xáº¡ mÃ£ sáº£n pháº©m
    // Ä‘áº¿n tÃªn sáº£n pháº©m.
    private final Map<String, String> tenSanPhamTheoMa = new LinkedHashMap<>();
    // danhSachVoucher: Danh sÃ¡ch voucher Ä‘ang hoáº¡t Ä‘á»™ng.
    private final List<entity.ThanhToan.Voucher> danhSachVoucher = new ArrayList<>();
    // maPhieuDichVuTrongDonHang: LÆ°u cÃ¡c mÃ£ phiáº¿u dá»‹ch vá»¥ trong Ä‘Æ¡n hÃ ng; pháº§n tá»­
    // cÃ³ kiá»ƒu String.
    private final List<String> maPhieuDichVuTrongDonHang = new ArrayList<>();
    // dichVuTuPhieuDaThem: LÆ°u cÃ¡c dÃ²ng dá»‹ch vá»¥ Ä‘Æ°á»£c thÃªm tá»« phiáº¿u; pháº§n tá»­ cÃ³ kiá»ƒu
    // entity.ThanhToan.
    private final List<entity.ThanhToan> dichVuTuPhieuDaThem = new ArrayList<>();
    // tableModel: MÃ´ hÃ¬nh lÆ°u dá»¯ liá»‡u vÃ  cung cáº¥p cÃ¡c Ã´ cho báº£ng giao diá»‡n.
    private final DonHangModel tableModel = new DonHangModel();

    // cmbMaKhachHang: Há»™p lá»±a chá»n mÃ£ khÃ¡ch hÃ ng.
    private final JComboBox<String> cmbMaKhachHang = new JComboBox<>();
    // txtTenKhachHang: Ã” nháº­p/hiá»ƒn thá»‹ vÄƒn báº£n tÃªn khÃ¡ch hÃ ng.
    private final JTextField txtTenKhachHang = new JTextField();
    // txtSoDienThoai: Ã” nháº­p/hiá»ƒn thá»‹ vÄƒn báº£n sá»‘ Ä‘iá»‡n thoáº¡i.
    private final JTextField txtSoDienThoai = new JTextField(12);
    // txtMaPhieuDichVu: Ã” nháº­p/hiá»ƒn thá»‹ vÄƒn báº£n mÃ£ phiáº¿u dá»‹ch vá»¥.
    private final JTextField txtMaPhieuDichVu = new JTextField(18);
    // txtKhachHangTomTat: Ã” nháº­p/hiá»ƒn thá»‹ vÄƒn báº£n thÃ´ng tin tÃ³m táº¯t khÃ¡ch hÃ ng.
    private final JTextField txtKhachHangTomTat = new JTextField();
    // cmbMaSanPham: Há»™p lá»±a chá»n mÃ£ sáº£n pháº©m.
    private final JComboBox<String> cmbMaSanPham = new JComboBox<>();
    // cmbVoucher: Há»™p gá»£i Ã½ mÃ£ voucher.
    private final JComboBox<String> cmbVoucher = new JComboBox<>();
    // txtTienKhachDua: Ã” nháº­p/hiá»ƒn thá»‹ vÄƒn báº£n sá»‘ tiá»n khÃ¡ch Ä‘Æ°a.
    private final JTextField txtTienKhachDua = new JTextField();
    // txtNgayLap: Ã” nháº­p/hiá»ƒn thá»‹ vÄƒn báº£n ngÃ y giá» láº­p hÃ³a Ä‘Æ¡n.
    private final JTextField txtNgayLap = new JTextField();
    // lblTongHang: NhÃ£n hiá»ƒn thá»‹ tá»•ng tiá»n hÃ ng.
    private final JLabel lblTongHang = new JLabel();
    // lblGiamGia: NhÃ£n hiá»ƒn thá»‹ tiá»n giáº£m giÃ¡.
    private final JLabel lblGiamGia = new JLabel();
    // lblTongThanhToan: NhÃ£n hiá»ƒn thá»‹ tá»•ng tiá»n thanh toÃ¡n.
    private final JLabel lblTongThanhToan = new JLabel();
    // lblTienThua: NhÃ£n hiá»ƒn thá»‹ tiá»n thá»«a.
    private final JLabel lblTienThua = new JLabel();
    // tblDonHang: Báº£ng hiá»ƒn thá»‹ dá»¯ liá»‡u Ä‘Æ¡n hÃ ng.
    private final JTable tblDonHang = new JTable(tableModel);
    // pnlPaymentMethodsPanel: Khung chá»©a thÃ nh pháº§n giao diá»‡n khung phÆ°Æ¡ng thá»©c
    // thanh toÃ¡n.
    private final JPanel pnlPaymentMethodsPanel = new JPanel(new GridLayout(1, 0, 8, 0));
    // phuongThucTheoNut: PhÆ°Æ¡ng thá»©c thanh toÃ¡n tÆ°Æ¡ng á»©ng vá»›i tá»«ng nÃºt chá»n.
    private final Map<JToggleButton, entity.ThanhToan.PhuongThuc> phuongThucTheoNut = new LinkedHashMap<>();
    // btnApDungVoucher: NÃºt thao tÃ¡c: Ãp dá»¥ng.
    private final JButton btnApDungVoucher = new JButton("Ãp dá»¥ng");

    // maKhachHang: LÆ°u mÃ£ khÃ¡ch hÃ ng.
    private String maKhachHang;
    // maKhachHangDaXacNhan: LÆ°u mÃ£ khÃ¡ch hÃ ng Ä‘Ã£ xÃ¡c nháº­n.
    private String maKhachHangDaXacNhan;
    // dangCapNhatGoiYMaKhachHang: Cá» cho biáº¿t Ä‘ang cáº­p nháº­t gá»£i Ã½ mÃ£ khÃ¡ch hÃ ng.
    private boolean dangCapNhatGoiYMaKhachHang;
    // dangCapNhatGoiYMaSanPham: Cá» cho biáº¿t Ä‘ang cáº­p nháº­t gá»£i Ã½ mÃ£ sáº£n pháº©m.
    private boolean dangCapNhatGoiYMaSanPham;
    // dangCapNhatGoiYVoucher: Cá» cho biáº¿t Ä‘ang cáº­p nháº­t gá»£i Ã½ voucher.
    private boolean dangCapNhatGoiYVoucher;
    // phuongThucHienTai: Äá»‘i tÆ°á»£ng phÆ°Æ¡ng thá»©c thanh toÃ¡n hiá»‡n táº¡i cÃ³ kiá»ƒu
    // entity.ThanhToan.PhuongThuc.
    private entity.ThanhToan.PhuongThuc phuongThucHienTai;
    // voucher: Äá»‘i tÆ°á»£ng voucher cÃ³ kiá»ƒu entity.ThanhToan.Voucher.
    private entity.ThanhToan.Voucher voucher;
    // ngayLap: LÆ°u ngÃ y giá» láº­p hÃ³a Ä‘Æ¡n dÆ°á»›i dáº¡ng dá»¯ liá»‡u thá»i gian.
    private LocalDateTime ngayLap = LocalDateTime.now();

    // Khá»Ÿi táº¡o Ä‘á»‘i tÆ°á»£ng tá»« cÃ¡c giÃ¡ trá»‹ Ä‘Æ°á»£c truyá»n vÃ o.
    // Tham sá»‘ maNhanVien: mÃ£ nhÃ¢n viÃªn.
    // Tham sá»‘ moLichSuHoaDon: má»Ÿ lá»‹ch sá»­ hÃ³a Ä‘Æ¡n.
    public QuanLyThanhToanUI(String maNhanVien, Runnable moLichSuHoaDon) {
        this(maNhanVien, moLichSuHoaDon, () -> {
        });
    }

    // Khá»Ÿi táº¡o giao diá»‡n vÃ  thiáº¿t láº­p cÃ¡c thÃ nh pháº§n, sá»± kiá»‡n ban Ä‘áº§u.
    // Tham sá»‘ maNhanVien: mÃ£ nhÃ¢n viÃªn.
    // Tham sá»‘ moLichSuHoaDon: má»Ÿ lá»‹ch sá»­ hÃ³a Ä‘Æ¡n.
    // Tham sá»‘ capNhatPhieuDichVu: cáº­p nháº­t danh sÃ¡ch phiáº¿u dá»‹ch vá»¥.
    public QuanLyThanhToanUI(
            String maNhanVien, Runnable moLichSuHoaDon, Runnable capNhatPhieuDichVu) {
        this.maNhanVien = maNhanVien;
        this.moLichSuHoaDon = moLichSuHoaDon;
        this.capNhatPhieuDichVu = capNhatPhieuDichVu;
        setTitle("Thanh toÃ¡n hÃ³a Ä‘Æ¡n");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pnlNoiDung.setBackground(LIGHT_BLUE);
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));
        pnlNoiDung.add(createTitle(), BorderLayout.NORTH);
        pnlNoiDung.add(createBody(), BorderLayout.CENTER);
        setContentPane(pnlNoiDung);
        tblDonHang.addMouseListener(this);
        napDanhSachMaKhachHang();
        napDanhSachMaSanPham();
        napDanhSachVoucher();
        napPhuongThucThanhToan();
        capNhatTien();
    }

    /** Tráº£ vá» panel ná»™i dung Ä‘á»ƒ TrangChu tiáº¿p tá»¥c hiá»ƒn thá»‹ báº±ng CardLayout. */
    public JPanel getContentPanel() {
        return pnlNoiDung;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        switch (event.getActionCommand()) {
            case "OPEN_INVOICE_HISTORY":
                moLichSuHoaDon.run();
                break;
            case "CANCEL_INVOICE":
                huyHoaDon();
                break;
            case "CHOOSE_PRODUCT":
                moChonSanPham();
                break;
            case "CONFIRM_CUSTOMER":
                xacNhanKhachHang();
                break;
            case "APPLY_VOUCHER":
                apDungVoucher();
                break;
            case "REMOVE_PRODUCT":
                xoaSanPham();
                break;
            case "COMPLETE_PAYMENT":
                thanhToan();
                break;
            case "SELECT_PAYMENT_METHOD":
                capNhatPhuongThucThanhToan(event.getSource());
                break;
            default:
                break;
        }
    }

    private void capNhatPhuongThucThanhToan(Object source) {
        if (!(source instanceof JToggleButton)) {
            return;
        }
        JToggleButton selectedButton = (JToggleButton) source;
        entity.ThanhToan.PhuongThuc selectedMethod = phuongThucTheoNut.get(selectedButton);
        if (selectedMethod == null) {
            return;
        }
        phuongThucHienTai = selectedMethod;
        for (Component component : pnlPaymentMethodsPanel.getComponents()) {
            if (component instanceof JToggleButton) {
                JToggleButton methodButton = (JToggleButton) component;
                methodButton.setBackground(methodButton.isSelected()
                        ? new Color(215, 231, 244)
                        : Color.WHITE);
            }
        }
        capNhatTien();
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        int row = tblDonHang.rowAtPoint(event.getPoint());
        if (row >= 0) {
            tblDonHang.setRowSelectionInterval(row, row);
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

    /** Äá»“ng bá»™ báº£ng vÃ  tá»•ng tiá»n khi quay láº¡i thanh toÃ¡n sau khi sá»­a giá» hÃ ng. */
    public void napGioHang() {
        tableModel.fireTableDataChanged();
        capNhatTien();
    }

    // Táº¡o tiÃªu Ä‘á».
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu JPanel.
    private JPanel createTitle() {
        // pnlKhung: Khung dÃ¹ng Ä‘á»ƒ nhÃ³m vÃ  bá»‘ trÃ­ cÃ¡c thÃ nh pháº§n giao diá»‡n.
        JPanel pnlKhung = new JPanel(new BorderLayout());
        pnlKhung.setOpaque(false);
        // lblTitle2: NhÃ£n hiá»ƒn thá»‹: Thanh toÃ¡n hoÃ¡ Ä‘Æ¡n.
        JLabel lblTitle2 = new JLabel("Thanh toÃ¡n hoÃ¡ Ä‘Æ¡n");
        lblTitle2.setForeground(NAVY);
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 25));
        pnlKhung.add(lblTitle2, BorderLayout.WEST);
        // pnlActions: Khung chá»©a thÃ nh pháº§n giao diá»‡n thao tÃ¡c.
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);
        // btnLichSu: NÃºt thao tÃ¡c: Lá»‹ch sá»­ hÃ³a Ä‘Æ¡n.
        JButton btnLichSu = new JButton("Lá»‹ch sá»­ hÃ³a Ä‘Æ¡n");
        btnLichSu.setForeground(NAVY);
        btnLichSu.setFocusPainted(false);
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        btnLichSu.setActionCommand("OPEN_INVOICE_HISTORY");
        btnLichSu.addActionListener(this);
        pnlActions.add(btnLichSu);

        // btnHuy: NÃºt thao tÃ¡c: Há»§y hÃ³a Ä‘Æ¡n Ã—.
        JButton btnHuy = new JButton("Há»§y hÃ³a Ä‘Æ¡n  Ã—");
        btnHuy.setForeground(NAVY);
        btnHuy.setFocusPainted(false);
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        btnHuy.setActionCommand("CANCEL_INVOICE");
        btnHuy.addActionListener(this);
        pnlActions.add(btnHuy);
        pnlKhung.add(pnlActions, BorderLayout.EAST);
        return pnlKhung;
    }

    // Táº¡o ná»™i dung.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu JPanel.
    private JPanel createBody() {
        // pnlBody: Khung chá»©a thÃ nh pháº§n giao diá»‡n ná»™i dung.
        JPanel pnlBody = new JPanel(new GridBagLayout());
        pnlBody.setOpaque(false);
        // gbc: RÃ ng buá»™c vá»‹ trÃ­, kÃ­ch thÆ°á»›c vÃ  khoáº£ng cÃ¡ch khi bá»‘ trÃ­ thÃ nh pháº§n báº±ng
        // GridBagLayout.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 12);
        gbc.gridx = 0;
        gbc.weightx = 0.65;
        pnlBody.add(createOrderPanel(), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 0);
        pnlBody.add(createPaymentPanel(), gbc);
        return pnlBody;
    }

    // Táº¡o khung Ä‘Æ¡n hÃ ng.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu JPanel.
    private JPanel createOrderPanel() {
        // pnlKhung: Khung dÃ¹ng Ä‘á»ƒ nhÃ³m vÃ  bá»‘ trÃ­ cÃ¡c thÃ nh pháº§n giao diá»‡n.
        JPanel pnlKhung = new JPanel(new BorderLayout(0, 8));
        pnlKhung.setOpaque(false);
        pnlKhung.add(createCustomerPanel(), BorderLayout.NORTH);

        // pnlOrder: Khung chá»©a thÃ nh pháº§n giao diá»‡n Ä‘Æ¡n hÃ ng.
        JPanel pnlOrder = new JPanel(new BorderLayout(6, 8));
        pnlOrder.setBackground(Color.WHITE);
        pnlOrder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("ÄÆ N HÃ€NG"),
                BorderFactory.createEmptyBorder(5, 8, 8, 8)));
        // pnlSearch: Khung chá»©a thÃ nh pháº§n giao diá»‡n tÃ¬m kiáº¿m.
        JPanel pnlSearch = new JPanel(new BorderLayout(12, 0));
        pnlSearch.setOpaque(false);
        cmbMaSanPham.setEditable(true);
        cmbMaSanPham.setToolTipText("Nháº­p mÃ£ sáº£n pháº©m");
        Component editor = cmbMaSanPham.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            // txtMaSanPham: Ã” nháº­p/hiá»ƒn thá»‹ vÄƒn báº£n mÃ£ sáº£n pháº©m.
            JTextField txtMaSanPham = (JTextField) editor;
            // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
            txtMaSanPham.setActionCommand("CHOOSE_PRODUCT");
            txtMaSanPham.addActionListener(this);
            txtMaSanPham.getDocument().addDocumentListener(new DocumentListener() {
                // Cáº­p nháº­t tráº¡ng thÃ¡i liÃªn quan khi ngÆ°á»i dÃ¹ng thÃªm kÃ½ tá»± vÃ o Ã´ vÄƒn báº£n.
                // Tham sá»‘ e: thÃ´ng tin sá»± kiá»‡n.
                @Override
                public void insertUpdate(DocumentEvent e) {
                    capNhatGoiYMaSanPham();
                }

                // Cáº­p nháº­t tráº¡ng thÃ¡i liÃªn quan khi ngÆ°á»i dÃ¹ng xÃ³a kÃ½ tá»± trong Ã´ vÄƒn báº£n.
                // Tham sá»‘ e: thÃ´ng tin sá»± kiá»‡n.
                @Override
                public void removeUpdate(DocumentEvent e) {
                    capNhatGoiYMaSanPham();
                }

                // Nháº­n thÃ´ng bÃ¡o khi thuá»™c tÃ­nh cá»§a tÃ i liá»‡u vÄƒn báº£n thay Ä‘á»•i.
                // Tham sá»‘ e: thÃ´ng tin sá»± kiá»‡n.
                @Override
                public void changedUpdate(DocumentEvent e) {
                    capNhatGoiYMaSanPham();
                }
            });
        }
        // pnlSearchField: Khung chá»©a thÃ nh pháº§n giao diá»‡n tÃ¬m kiáº¿m trÆ°á»ng.
        JPanel pnlSearchField = new JPanel(new BorderLayout(6, 0));
        pnlSearchField.setOpaque(false);
        pnlSearchField.add(new JLabel("TÃ¬m sáº£n pháº©m"), BorderLayout.WEST);
        cmbMaSanPham.setToolTipText("Nháº­p mÃ£ hoáº·c tÃªn sáº£n pháº©m");
        pnlSearchField.add(cmbMaSanPham, BorderLayout.CENTER);
        // btnTim: NÃºt thao tÃ¡c: XÃ¡c nháº­n.
        JButton btnTim = new JButton("XÃ¡c nháº­n");
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        btnTim.setActionCommand("CHOOSE_PRODUCT");
        btnTim.addActionListener(this);
        // pnlDatePanel: Khung chá»©a thÃ nh pháº§n giao diá»‡n ngÃ y khung.
        JPanel pnlDatePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlDatePanel.setOpaque(false);
        pnlDatePanel.add(new JLabel("NgÃ y láº­p"));
        txtNgayLap.setEditable(false);
        txtNgayLap.setHorizontalAlignment(SwingConstants.CENTER);
        txtNgayLap.setPreferredSize(new Dimension(150, txtNgayLap.getPreferredSize().height));
        capNhatNgayLap();
        pnlDatePanel.add(txtNgayLap);
        // pnlSearchLeft: Khung chá»©a thÃ nh pháº§n giao diá»‡n tÃ¬m kiáº¿m trÃ¡i.
        JPanel pnlSearchLeft = new JPanel(new BorderLayout(6, 0));
        pnlSearchLeft.setOpaque(false);
        pnlSearchLeft.add(pnlSearchField, BorderLayout.CENTER);
        pnlSearchLeft.add(btnTim, BorderLayout.EAST);
        pnlSearch.add(pnlSearchLeft, BorderLayout.CENTER);
        pnlSearch.add(pnlDatePanel, BorderLayout.EAST);
        pnlOrder.add(pnlSearch, BorderLayout.NORTH);

        tblDonHang.setRowHeight(25);
        tblDonHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDonHang.getTableHeader().setBackground(NAVY);
        tblDonHang.getTableHeader().setForeground(Color.WHITE);
        tblDonHang.setFillsViewportHeight(true);
        tblDonHang.setShowHorizontalLines(true);
        tblDonHang.setGridColor(new Color(232, 236, 240));
        tblDonHang.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblDonHang.getColumnModel().getColumn(1).setPreferredWidth(240);
        tblDonHang.getColumnModel().getColumn(2).setPreferredWidth(55);
        tblDonHang.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblDonHang.getColumnModel().getColumn(4).setPreferredWidth(110);
        tblDonHang.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        tblDonHang.getColumnModel().getColumn(3).setCellRenderer(new MoneyRenderer());
        tblDonHang.getColumnModel().getColumn(4).setCellRenderer(new MoneyRenderer());
        tblDonHang.getColumnModel().getColumn(2).setCellEditor(
                new DefaultCellEditor(new JTextField()));
        // scrCuon2: Khung cuá»™n khung cuá»™n.
        JScrollPane scrCuon2 = new JScrollPane(tblDonHang);
        scrCuon2.setBorder(BorderFactory.createLineBorder(BORDER));
        pnlOrder.add(scrCuon2, BorderLayout.CENTER);

        pnlOrder.add(createOrderFooter(), BorderLayout.SOUTH);
        pnlKhung.add(pnlOrder, BorderLayout.CENTER);
        return pnlKhung;
    }

    // Táº¡o khung thÃ´ng tin khÃ¡ch hÃ ng.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu JPanel.
    private JPanel createCustomerPanel() {
        // pnlKhung: Khung dÃ¹ng Ä‘á»ƒ nhÃ³m vÃ  bá»‘ trÃ­ cÃ¡c thÃ nh pháº§n giao diá»‡n.
        JPanel pnlKhung = new JPanel(new GridBagLayout());
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("KHÃCH HÃ€NG"),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        // gbc: RÃ ng buá»™c vá»‹ trÃ­, kÃ­ch thÆ°á»›c vÃ  khoáº£ng cÃ¡ch khi bá»‘ trÃ­ thÃ nh pháº§n báº±ng
        // GridBagLayout.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        pnlKhung.add(new JLabel("MÃ£ khÃ¡ch hÃ ng"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.45;
        cmbMaKhachHang.setEditable(true);
        cmbMaKhachHang.setToolTipText("Nháº­p mÃ£ khÃ¡ch hÃ ng");
        Component editor = cmbMaKhachHang.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            // txtMaKhachHang: Ã” nháº­p/hiá»ƒn thá»‹ vÄƒn báº£n mÃ£ khÃ¡ch hÃ ng.
            JTextField txtMaKhachHang = (JTextField) editor;
            // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
            txtMaKhachHang.setActionCommand("CONFIRM_CUSTOMER");
            txtMaKhachHang.addActionListener(this);
            txtMaKhachHang.getDocument().addDocumentListener(new DocumentListener() {
                // Cáº­p nháº­t tráº¡ng thÃ¡i liÃªn quan khi ngÆ°á»i dÃ¹ng thÃªm kÃ½ tá»± vÃ o Ã´ vÄƒn báº£n.
                // Tham sá»‘ e: thÃ´ng tin sá»± kiá»‡n.
                @Override
                public void insertUpdate(DocumentEvent e) {
                    capNhatGoiYMaKhachHang();
                }

                // Cáº­p nháº­t tráº¡ng thÃ¡i liÃªn quan khi ngÆ°á»i dÃ¹ng xÃ³a kÃ½ tá»± trong Ã´ vÄƒn báº£n.
                // Tham sá»‘ e: thÃ´ng tin sá»± kiá»‡n.
                @Override
                public void removeUpdate(DocumentEvent e) {
                    capNhatGoiYMaKhachHang();
                }

                // Nháº­n thÃ´ng bÃ¡o khi thuá»™c tÃ­nh cá»§a tÃ i liá»‡u vÄƒn báº£n thay Ä‘á»•i.
                // Tham sá»‘ e: thÃ´ng tin sá»± kiá»‡n.
                @Override
                public void changedUpdate(DocumentEvent e) {
                    capNhatGoiYMaKhachHang();
                }
            });
        }
        pnlKhung.add(cmbMaKhachHang, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        // btnXacNhan: NÃºt thao tÃ¡c: XÃ¡c nháº­n.
        JButton btnXacNhan = new JButton("XÃ¡c nháº­n");
        btnXacNhan.setMargin(new Insets(3, 8, 3, 8));
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        btnXacNhan.setActionCommand("CONFIRM_CUSTOMER");
        btnXacNhan.addActionListener(this);
        pnlKhung.add(btnXacNhan, gbc);
        gbc.gridx = 3;
        gbc.weightx = 0;
        pnlKhung.add(new JLabel("Sá»‘ Ä‘iá»‡n thoáº¡i"), gbc);
        gbc.gridx = 4;
        gbc.weightx = 0.55;
        txtSoDienThoai.setEditable(false);
        pnlKhung.add(txtSoDienThoai, gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        pnlKhung.add(new JLabel("TÃªn khÃ¡ch hÃ ng"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.45;
        txtTenKhachHang.setColumns(18);
        txtTenKhachHang.setEditable(false);
        pnlKhung.add(txtTenKhachHang, gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        pnlKhung.add(new JLabel("MÃ£ phiáº¿u dá»‹ch vá»¥"), gbc);
        gbc.gridx = 4;
        gbc.weightx = 0.55;
        txtMaPhieuDichVu.setEditable(false);
        txtMaPhieuDichVu.setToolTipText("CÃ¡c phiáº¿u dá»‹ch vá»¥ Ä‘ang xá»­ lÃ½ cá»§a khÃ¡ch hÃ ng");
        pnlKhung.add(txtMaPhieuDichVu, gbc);
        txtKhachHangTomTat.setEditable(false);
        return pnlKhung;
    }

    // Táº¡o pháº§n tá»•ng káº¿t Ä‘Æ¡n hÃ ng.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu JPanel.
    private JPanel createOrderFooter() {
        // pnlFooter2: Khung chá»©a thÃ nh pháº§n giao diá»‡n cuá»‘i trang.
        JPanel pnlFooter2 = new JPanel(new GridBagLayout());
        pnlFooter2.setOpaque(false);
        // gbc: RÃ ng buá»™c vá»‹ trÃ­, kÃ­ch thÆ°á»›c vÃ  khoáº£ng cÃ¡ch khi bá»‘ trÃ­ thÃ nh pháº§n báº±ng
        // GridBagLayout.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 0, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.7;
        pnlFooter2.add(new JLabel("KhÃ¡ch hÃ ng"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.3;
        pnlFooter2.add(new JLabel("Voucher"), gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.7;
        gbc.gridwidth = 1;
        txtKhachHangTomTat.setToolTipText("KhÃ¡ch hÃ ng Ä‘Ã£ xÃ¡c nháº­n");
        txtKhachHangTomTat.setEditable(false);
        pnlFooter2.add(txtKhachHangTomTat, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.3;
        // pnlVoucherRow: Khung chá»©a thÃ nh pháº§n giao diá»‡n voucher dÃ²ng.
        JPanel pnlVoucherRow = new JPanel(new BorderLayout(5, 0));
        pnlVoucherRow.setOpaque(false);
        cmbVoucher.setEditable(true);
        cmbVoucher.setToolTipText("Nháº­p mÃ£ hoáº·c tÃªn voucher");
        Component voucherEditor = cmbVoucher.getEditor().getEditorComponent();
        if (voucherEditor instanceof JTextField) {
            JTextField txtVoucher = (JTextField) voucherEditor;
            txtVoucher.setActionCommand("APPLY_VOUCHER");
            txtVoucher.addActionListener(this);
            txtVoucher.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    capNhatGoiYVoucher();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    capNhatGoiYVoucher();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    capNhatGoiYVoucher();
                }
            });
        }
        pnlVoucherRow.add(cmbVoucher, BorderLayout.CENTER);
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        btnApDungVoucher.setActionCommand("APPLY_VOUCHER");
        btnApDungVoucher.addActionListener(this);
        // pnlVoucherActions: Khung chá»©a thÃ nh pháº§n giao diá»‡n voucher thao tÃ¡c.
        JPanel pnlVoucherActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlVoucherActions.setOpaque(false);
        // btnXoa: NÃºt thao tÃ¡c: XÃ³a sáº£n pháº©m.
        JButton btnXoa = new JButton("XÃ³a sáº£n pháº©m");
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        btnXoa.setActionCommand("REMOVE_PRODUCT");
        btnXoa.addActionListener(this);
        pnlVoucherActions.add(btnApDungVoucher);
        pnlVoucherActions.add(btnXoa);
        pnlVoucherRow.add(pnlVoucherActions, BorderLayout.EAST);
        pnlFooter2.add(pnlVoucherRow, gbc);
        return pnlFooter2;
    }

    // Táº¡o khung thanh toÃ¡n.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu JPanel.
    private JPanel createPaymentPanel() {
        // pnlKhung: Khung dÃ¹ng Ä‘á»ƒ nhÃ³m vÃ  bá»‘ trÃ­ cÃ¡c thÃ nh pháº§n giao diá»‡n.
        JPanel pnlKhung = new JPanel(new BorderLayout(8, 10));
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // pnlOverview: Khung chá»©a thÃ nh pháº§n giao diá»‡n tá»•ng quan.
        JPanel pnlOverview = new JPanel(new GridBagLayout());
        pnlOverview.setBackground(Color.WHITE);
        pnlOverview.setBorder(BorderFactory.createTitledBorder("Chi tiáº¿t thanh toÃ¡n"));
        // gbc: RÃ ng buá»™c vá»‹ trÃ­, kÃ­ch thÆ°á»›c vÃ  khoáº£ng cÃ¡ch khi bá»‘ trÃ­ thÃ nh pháº§n báº±ng
        // GridBagLayout.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.gridy = 0;
        pnlOverview.add(new JLabel("Tiá»n hÃ ng"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        lblTongHang.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlOverview.add(lblTongHang, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        pnlOverview.add(new JLabel("Voucher"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        lblGiamGia.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlOverview.add(lblGiamGia, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        pnlOverview.add(new JLabel("Tá»•ng thanh toÃ¡n"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        lblTongThanhToan.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTongThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTongThanhToan.setForeground(NAVY);
        pnlOverview.add(lblTongThanhToan, gbc);
        pnlKhung.add(pnlOverview, BorderLayout.NORTH);

        // pnlPayment: Khung chá»©a thÃ nh pháº§n giao diá»‡n thanh toÃ¡n.
        JPanel pnlPayment = new JPanel(new GridBagLayout());
        pnlPayment.setBackground(Color.WHITE);
        pnlPayment.setBorder(BorderFactory.createTitledBorder("HÃ¬nh thá»©c thanh toÃ¡n"));
        // pg: RÃ ng buá»™c vá»‹ trÃ­, kÃ­ch thÆ°á»›c vÃ  khoáº£ng cÃ¡ch khi bá»‘ trÃ­ thÃ nh pháº§n báº±ng
        // GridBagLayout.
        GridBagConstraints pg = new GridBagConstraints();
        pg.gridx = 0;
        pg.gridy = 0;
        pg.weightx = 1;
        pg.fill = GridBagConstraints.HORIZONTAL;
        pg.insets = new Insets(5, 6, 8, 6);
        pnlPaymentMethodsPanel.setOpaque(false);
        pnlPayment.add(pnlPaymentMethodsPanel, pg);
        pg.gridy++;
        pnlPayment.add(new JLabel("Tiá»n khÃ¡ch Ä‘Æ°a"), pg);
        pg.gridy++;
        txtTienKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            // Cáº­p nháº­t tráº¡ng thÃ¡i liÃªn quan khi ngÆ°á»i dÃ¹ng thÃªm kÃ½ tá»± vÃ o Ã´ vÄƒn báº£n.
            // Tham sá»‘ e: thÃ´ng tin sá»± kiá»‡n.
            @Override
            public void insertUpdate(DocumentEvent e) {
                capNhatTien();
            }

            // Cáº­p nháº­t tráº¡ng thÃ¡i liÃªn quan khi ngÆ°á»i dÃ¹ng xÃ³a kÃ½ tá»± trong Ã´ vÄƒn báº£n.
            // Tham sá»‘ e: thÃ´ng tin sá»± kiá»‡n.
            @Override
            public void removeUpdate(DocumentEvent e) {
                capNhatTien();
            }

            // Nháº­n thÃ´ng bÃ¡o khi thuá»™c tÃ­nh cá»§a tÃ i liá»‡u vÄƒn báº£n thay Ä‘á»•i.
            // Tham sá»‘ e: thÃ´ng tin sá»± kiá»‡n.
            @Override
            public void changedUpdate(DocumentEvent e) {
                capNhatTien();
            }
        });
        pnlPayment.add(createMoneyField(txtTienKhachDua), pg);
        pg.gridy++;
        // pnlChangePanel: Khung chá»©a thÃ nh pháº§n giao diá»‡n thay Ä‘á»•i khung.
        JPanel pnlChangePanel = new JPanel(new BorderLayout());
        pnlChangePanel.setBackground(LIGHT_BLUE);
        pnlChangePanel.setBorder(BorderFactory.createEmptyBorder(7, 9, 7, 9));
        pnlChangePanel.add(new JLabel("Sá»‘ tiá»n thá»«a"), BorderLayout.WEST);
        lblTienThua.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlChangePanel.add(lblTienThua, BorderLayout.EAST);
        pnlPayment.add(pnlChangePanel, pg);

        // pnlMiddle: Khung chá»©a thÃ nh pháº§n giao diá»‡n giá»¯a.
        JPanel pnlMiddle = new JPanel(new BorderLayout(0, 10));
        pnlMiddle.setOpaque(false);
        pnlMiddle.add(pnlPayment, BorderLayout.CENTER);
        pnlKhung.add(pnlMiddle, BorderLayout.CENTER);

        // btnThanhToan: NÃºt thao tÃ¡c: THANH TOÃN & IN HÃ“A ÄÆ N.
        JButton btnThanhToan = new JButton("THANH TOÃN & IN HÃ“A ÄÆ N");
        btnThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setBackground(GREEN);
        btnThanhToan.setFocusPainted(false);
        btnThanhToan.setPreferredSize(new Dimension(100, 48));
        // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
        btnThanhToan.setActionCommand("COMPLETE_PAYMENT");
        btnThanhToan.addActionListener(this);
        pnlKhung.add(btnThanhToan, BorderLayout.SOUTH);
        return pnlKhung;
    }

    // Táº¡o vÃ¹ng nháº­p tiá»n.
    // Tham sá»‘ txtField: trÆ°á»ng.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu JPanel.
    private JPanel createMoneyField(JTextField txtField) {
        // pnlWrapper: Khung chá»©a thÃ nh pháº§n giao diá»‡n khung bá»c.
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setBorder(BorderFactory.createLineBorder(BORDER));
        pnlWrapper.add(txtField, BorderLayout.CENTER);
        // lblCurrency: NhÃ£n hiá»ƒn thá»‹: VND.
        JLabel lblCurrency = new JLabel("VND");
        lblCurrency.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 7));
        pnlWrapper.add(lblCurrency, BorderLayout.EAST);
        return pnlWrapper;
    }

    // Náº¡p phÆ°Æ¡ng thá»©c thanh toÃ¡n thanh toÃ¡n.
    private void napPhuongThucThanhToan() {
        try {
            // phuongThucs: LÆ°u cÃ¡c phÆ°Æ¡ng thá»©c thanh toÃ¡n; pháº§n tá»­ cÃ³ kiá»ƒu
            // entity.ThanhToan.PhuongThuc.
            List<entity.ThanhToan.PhuongThuc> phuongThucs = dao.layPhuongThucThanhToan();
            ButtonGroup group = new ButtonGroup();
            pnlPaymentMethodsPanel.removeAll();
            phuongThucTheoNut.clear();
            phuongThucHienTai = null;
            for (entity.ThanhToan.PhuongThuc phuongThuc : phuongThucs) {
                // tglThaoTac: NÃºt báº­t/táº¯t nÃºt thao tÃ¡c.
                JToggleButton tglThaoTac = new JToggleButton(
                        "<html><center>" + phuongThuc.getTenPhuongThuc() + "</center></html>");
                tglThaoTac.setFocusPainted(false);
                tglThaoTac.setBackground(Color.WHITE);
                tglThaoTac.setOpaque(true);
                tglThaoTac.setBorder(BorderFactory.createLineBorder(BORDER));
                tglThaoTac.setPreferredSize(new Dimension(95, 48));
                // ÄÄƒng kÃ½ hÃ nh Ä‘á»™ng xá»­ lÃ½ khi ngÆ°á»i dÃ¹ng kÃ­ch hoáº¡t control nÃ y.
                tglThaoTac.setActionCommand("SELECT_PAYMENT_METHOD");
                tglThaoTac.addActionListener(this);
                phuongThucTheoNut.put(tglThaoTac, phuongThuc);
                group.add(tglThaoTac);
                pnlPaymentMethodsPanel.add(tglThaoTac);
                if (phuongThucHienTai == null) {
                    tglThaoTac.setSelected(true);
                    tglThaoTac.setBackground(new Color(215, 231, 244));
                    phuongThucHienTai = phuongThuc;
                }
            }
            pnlPaymentMethodsPanel.revalidate();
            pnlPaymentMethodsPanel.repaint();
        } catch (Exception e) {
            hienLoi("KhÃ´ng táº£i Ä‘Æ°á»£c phÆ°Æ¡ng thá»©c thanh toÃ¡n: " + e.getMessage());
        }
    }

    // Náº¡p danh sÃ¡ch mÃ£ sáº£n pháº©m.
    private void napDanhSachMaSanPham() {
        Object giaTriDangNhap = cmbMaSanPham.getEditor().getItem();
        try {
            Map<String, String> sanPhamMoi = dao.layDanhSachSanPham();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            String mauHienThi = "";
            for (Map.Entry<String, String> sanPham : sanPhamMoi.entrySet()) {
                String goiY = sanPham.getKey();
                if (sanPham.getValue() != null && !sanPham.getValue().isBlank()) {
                    goiY += " - " + sanPham.getValue();
                }
                model.addElement(goiY);
                if (goiY.length() > mauHienThi.length()) {
                    mauHienThi = goiY;
                }
            }
            tenSanPhamTheoMa.clear();
            tenSanPhamTheoMa.putAll(sanPhamMoi);
            danhSachMaSanPham.clear();
            danhSachMaSanPham.addAll(sanPhamMoi.keySet());
            dangCapNhatGoiYMaSanPham = true;
            try {
                cmbMaSanPham.setModel(model);
                cmbMaSanPham.setSelectedItem(giaTriDangNhap == null ? "" : giaTriDangNhap);
                cmbMaSanPham.getEditor().setItem(giaTriDangNhap == null ? "" : giaTriDangNhap);
            } finally {
                dangCapNhatGoiYMaSanPham = false;
            }
            cmbMaSanPham.setPrototypeDisplayValue(mauHienThi);
            cmbMaSanPham.setPreferredSize(cmbMaSanPham.getPreferredSize());
            if (giaTriDangNhap != null && !giaTriDangNhap.toString().isBlank()) {
                capNhatGoiYMaSanPham();
            }
        } catch (Exception e) {
            hienLoi("KhÃ´ng táº£i Ä‘Æ°á»£c danh sÃ¡ch mÃ£ sáº£n pháº©m: " + e.getMessage());
        }
    }

    // Náº¡p danh sÃ¡ch mÃ£ khÃ¡ch hÃ ng.
    public void napDanhSachMaKhachHang() {
        Object giaTriDangNhap = cmbMaKhachHang.getEditor().getItem();
        try {
            List<entity.ThanhToan.KhachHang> danhSachMoi = dao.layDanhSachKhachHang();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            String maDaiNhat = "KH000000";
            for (entity.ThanhToan.KhachHang khachHang : danhSachMoi) {
                model.addElement(taoGoiYKhachHang(khachHang));
                if (khachHang.getMaKhachHang().length() > maDaiNhat.length()) {
                    maDaiNhat = khachHang.getMaKhachHang();
                }
            }
            danhSachKhachHang.clear();
            danhSachKhachHang.addAll(danhSachMoi);
            dangCapNhatGoiYMaKhachHang = true;
            try {
                cmbMaKhachHang.setModel(model);
                cmbMaKhachHang.setSelectedItem(giaTriDangNhap == null ? "" : giaTriDangNhap);
                cmbMaKhachHang.getEditor().setItem(giaTriDangNhap == null ? "" : giaTriDangNhap);
            } finally {
                dangCapNhatGoiYMaKhachHang = false;
            }
            cmbMaKhachHang.setPrototypeDisplayValue(maDaiNhat);
            cmbMaKhachHang.setPreferredSize(cmbMaKhachHang.getPreferredSize());
        } catch (SQLException e) {
            hienLoi("KhÃ´ng táº£i Ä‘Æ°á»£c danh sÃ¡ch mÃ£ khÃ¡ch hÃ ng: " + e.getMessage());
        }
    }

    // Náº¡p danh sÃ¡ch voucher Ä‘ang hoáº¡t Ä‘á»™ng.
    private void napDanhSachVoucher() {
        Object giaTriDangNhap = cmbVoucher.getEditor().getItem();
        try {
            List<entity.ThanhToan.Voucher> voucherMoi = dao.layDanhSachVoucher();
            String maDaiNhat = "VOUCHER000";
            for (entity.ThanhToan.Voucher item : voucherMoi) {
                if (item.getMaVoucher().length() > maDaiNhat.length()) {
                    maDaiNhat = item.getMaVoucher();
                }
            }
            danhSachVoucher.clear();
            danhSachVoucher.addAll(voucherMoi);
            cmbVoucher.setPrototypeDisplayValue(maDaiNhat);
            cmbVoucher.setPreferredSize(cmbVoucher.getPreferredSize());
            dangCapNhatGoiYVoucher = true;
            try {
                cmbVoucher.getEditor().setItem(giaTriDangNhap == null ? "" : giaTriDangNhap);
            } finally {
                dangCapNhatGoiYVoucher = false;
            }
            capNhatGoiYVoucher();
        } catch (SQLException e) {
            hienLoi("KhÃ´ng táº£i Ä‘Æ°á»£c danh sÃ¡ch voucher: " + e.getMessage());
        }
    }

    /** LÃ m má»›i dá»¯ liá»‡u gá»£i Ã½ khi má»Ÿ láº¡i mÃ n hÃ¬nh thanh toÃ¡n. */
    public void napDanhSachGoiYSanPhamVaVoucher() {
        napDanhSachMaSanPham();
        napDanhSachVoucher();
    }

    // Cáº­p nháº­t gá»£i y mÃ£ khÃ¡ch hÃ ng.
    private void capNhatGoiYMaKhachHang() {
        if (dangCapNhatGoiYMaKhachHang) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (dangCapNhatGoiYMaKhachHang) {
                return;
            }

            Component editorComponent = cmbMaKhachHang.getEditor().getEditorComponent();
            if (!(editorComponent instanceof JTextField)) {
                return;
            }
            JTextField txtEditor = (JTextField) editorComponent;
            String tuKhoa = txtEditor.getText();
            String maTimKiem = layMaKhachHangTuGoiY(tuKhoa);
            String tuKhoaDaChuanHoa = maTimKiem.toLowerCase(Locale.ROOT);
            List<String> goiY = new ArrayList<>();
            for (entity.ThanhToan.KhachHang khachHang : danhSachKhachHang) {
                String ma = khachHang.getMaKhachHang();
                String ten = khachHang.getHoTen() == null ? "" : khachHang.getHoTen();
                String soDienThoai = khachHang.getSoDienThoai() == null ? "" : khachHang.getSoDienThoai();
                boolean khopMa = maCoChuaTatCaKyTu(ma, tuKhoaDaChuanHoa);
                boolean khopTen = ten.toLowerCase(Locale.ROOT).contains(tuKhoaDaChuanHoa);
                boolean khopSdt = soDienThoai.toLowerCase(Locale.ROOT).contains(tuKhoaDaChuanHoa);
                if (khopMa || khopTen || khopSdt) {
                    goiY.add(taoGoiYKhachHang(khachHang));
                }
            }
            if (goiY.isEmpty()) {
                String maTheoTen = timMaKhachHangTheoTen(tuKhoa);
                if (!maTheoTen.isEmpty()) {
                    try {
                        entity.ThanhToan.KhachHang khachHang = dao.timKhachHang(maTheoTen);
                        if (khachHang != null) {
                            goiY.add(taoGoiYKhachHang(khachHang));
                        }
                    } catch (SQLException ex) {
                        // Bá» qua lá»—i lookup phá»¥ vÃ  giá»¯ gá»£i Ã½ cá»¥c bá»™.
                    }
                }
            }

            int viTriConTro = txtEditor.getCaretPosition();
            dangCapNhatGoiYMaKhachHang = true;
            try {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                for (String ma : goiY) {
                    model.addElement(ma);
                }
                cmbMaKhachHang.setModel(model);
                cmbMaKhachHang.setSelectedItem(tuKhoa);
                txtEditor.setText(tuKhoa);
                txtEditor.setCaretPosition(Math.min(viTriConTro, tuKhoa.length()));
            } finally {
                dangCapNhatGoiYMaKhachHang = false;
            }

            boolean daChonGoiY = danhSachKhachHang.stream()
                    .anyMatch(khachHang -> khachHang.getMaKhachHang().equalsIgnoreCase(maTimKiem)
                            && tuKhoa.equals(taoGoiYKhachHang(khachHang)))
                    || danhSachKhachHang.stream()
                            .anyMatch(khachHang -> tuKhoa.equalsIgnoreCase(khachHang.getHoTen())
                                    || tuKhoa.equalsIgnoreCase(khachHang.getSoDienThoai()));
            if (!maTimKiem.isEmpty() && !goiY.isEmpty() && !daChonGoiY) {
                cmbMaKhachHang.showPopup();
            } else if (cmbMaKhachHang.isPopupVisible()) {
                cmbMaKhachHang.hidePopup();
            }
        });
    }

    // Táº¡o gá»£i Ã½ khÃ¡ch hÃ ng gá»“m mÃ£ vÃ  há» tÃªn.
    private String taoGoiYKhachHang(entity.ThanhToan.KhachHang khachHang) {
        String ten = khachHang.getHoTen();
        return ten == null || ten.isBlank()
                ? khachHang.getMaKhachHang()
                : khachHang.getMaKhachHang() + " - " + ten;
    }

    // Láº¥y mÃ£ khÃ¡ch hÃ ng tá»« ná»™i dung Ã´ nháº­p hoáº·c gá»£i Ã½ Ä‘Ã£ chá»n.
    private String layMaKhachHangTuGoiY(String tuKhoa) {
        if (tuKhoa == null) {
            return "";
        }
        String input = tuKhoa.trim();
        int separator = input.indexOf(" - ");
        if (separator > 0) {
            String ma = input.substring(0, separator);
            for (entity.ThanhToan.KhachHang khachHang : danhSachKhachHang) {
                if (khachHang.getMaKhachHang().equalsIgnoreCase(ma)
                        && input.equals(taoGoiYKhachHang(khachHang))) {
                    return ma;
                }
            }
        }
        String maTheoTen = timMaKhachHangTheoTen(input);
        return maTheoTen.isEmpty() ? input : maTheoTen;
    }

    private String timMaKhachHangTheoTen(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) {
            return "";
        }
        String keyword = tuKhoa.trim();
        for (entity.ThanhToan.KhachHang khachHang : danhSachKhachHang) {
            String hoTen = khachHang.getHoTen() == null ? "" : khachHang.getHoTen();
            String soDienThoai = khachHang.getSoDienThoai() == null ? "" : khachHang.getSoDienThoai();
            if (hoTen.equalsIgnoreCase(keyword)
                    || hoTen.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT))
                    || soDienThoai.equalsIgnoreCase(keyword)) {
                return khachHang.getMaKhachHang();
            }
        }
        return "";
    }

    // Cáº­p nháº­t gá»£i y mÃ£ sáº£n pháº©m.
    private void capNhatGoiYMaSanPham() {
        if (dangCapNhatGoiYMaSanPham) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (dangCapNhatGoiYMaSanPham) {
                return;
            }

            Component editorComponent = cmbMaSanPham.getEditor().getEditorComponent();
            if (!(editorComponent instanceof JTextField)) {
                return;
            }
            // txtEditor: Ã” nháº­p/hiá»ƒn thá»‹ vÄƒn báº£n bá»™ chá»‰nh sá»­a.
            JTextField txtEditor = (JTextField) editorComponent;
            // tuKhoa: Äá»c ná»™i dung chá»¯ tá»« Ã´ nháº­p Ä‘á»ƒ xá»­ lÃ½ tá»« khÃ³a tÃ¬m kiáº¿m.
            String tuKhoa = txtEditor.getText();
            String maTimKiem = layMaSanPhamTuGoiY(tuKhoa);
            String tenTimKiem = maTimKiem.toLowerCase(Locale.ROOT);
            // goiY: LÆ°u gá»£i y; pháº§n tá»­ cÃ³ kiá»ƒu String.
            List<String> goiY = new ArrayList<>();
            String tuKhoaKhongPhanBietHoaThuong = maTimKiem.toLowerCase(Locale.ROOT);
            for (String ma : danhSachMaSanPham) {
                String ten = tenSanPhamTheoMa.get(ma);
                if (maCoChuaTatCaKyTu(ma, tuKhoaKhongPhanBietHoaThuong)
                        || (ten != null && ten.toLowerCase(Locale.ROOT).contains(tenTimKiem))) {
                    goiY.add(ma);
                }
            }

            int viTriConTro = txtEditor.getCaretPosition();
            dangCapNhatGoiYMaSanPham = true;
            try {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                for (String ma : goiY) {
                    model.addElement(taoGoiYSanPham(ma));
                }
                cmbMaSanPham.setModel(model);
                cmbMaSanPham.setSelectedItem(tuKhoa);
                // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
                txtEditor.setText(tuKhoa);
                txtEditor.setCaretPosition(Math.min(viTriConTro, tuKhoa.length()));
            } finally {
                dangCapNhatGoiYMaSanPham = false;
            }

            boolean daChonGoiY = tenSanPhamTheoMa.containsKey(maTimKiem)
                    && tuKhoa.equals(taoGoiYSanPham(maTimKiem));
            if (!maTimKiem.isEmpty() && !goiY.isEmpty() && !daChonGoiY
                    && !(goiY.size() == 1 && goiY.get(0).equalsIgnoreCase(maTimKiem))) {
                cmbMaSanPham.showPopup();
            } else if (cmbMaSanPham.isPopupVisible()) {
                cmbMaSanPham.hidePopup();
            }
        });
    }

    // Cáº­p nháº­t gá»£i Ã½ voucher.
    private void capNhatGoiYVoucher() {
        if (dangCapNhatGoiYVoucher) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (dangCapNhatGoiYVoucher) {
                return;
            }

            Component editorComponent = cmbVoucher.getEditor().getEditorComponent();
            if (!(editorComponent instanceof JTextField)) {
                return;
            }
            JTextField txtEditor = (JTextField) editorComponent;
            String tuKhoa = txtEditor.getText();
            String maTimKiem = layMaVoucherTuGoiY(tuKhoa);
            String tuKhoaDaChuanHoa = maTimKiem.toLowerCase(Locale.ROOT);
            List<entity.ThanhToan.Voucher> goiY = new ArrayList<>();
            try {
                for (entity.ThanhToan.Voucher item : danhSachVoucher) {
                    if (kiemTraDieuKienVoucher(item) != null) {
                        continue;
                    }
                    String tenVoucher = item.getTenVoucher();
                    if (maCoChuaTatCaKyTu(item.getMaVoucher(), tuKhoaDaChuanHoa)
                            || (tenVoucher != null
                                    && tenVoucher.toLowerCase(Locale.ROOT)
                                            .contains(tuKhoaDaChuanHoa))) {
                        goiY.add(item);
                    }
                }
            } catch (SQLException e) {
                hienLoi("KhÃ´ng thá»ƒ kiá»ƒm tra Ä‘iá»u kiá»‡n voucher: " + e.getMessage());
                return;
            }
            for (entity.ThanhToan.Voucher item : goiY) {
                if (item.getMaVoucher().equalsIgnoreCase(maTimKiem)) {
                    if (goiY.size() > 1) {
                        goiY.clear();
                        goiY.add(item);
                    }
                    break;
                }
            }

            int viTriConTro = txtEditor.getCaretPosition();
            dangCapNhatGoiYVoucher = true;
            try {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                for (entity.ThanhToan.Voucher item : goiY) {
                    model.addElement(taoGoiYVoucher(item));
                }
                cmbVoucher.setModel(model);
                cmbVoucher.setSelectedItem(tuKhoa);
                txtEditor.setText(tuKhoa);
                txtEditor.setCaretPosition(Math.min(viTriConTro, tuKhoa.length()));
            } finally {
                dangCapNhatGoiYVoucher = false;
            }

            boolean daChonGoiY = danhSachVoucher.stream()
                    .anyMatch(item -> item.getMaVoucher().equalsIgnoreCase(maTimKiem)
                            && tuKhoa.equals(taoGoiYVoucher(item)));
            if (!maTimKiem.isEmpty() && !goiY.isEmpty() && !daChonGoiY) {
                cmbVoucher.showPopup();
            } else if (cmbVoucher.isPopupVisible()) {
                cmbVoucher.hidePopup();
            }
        });
    }

    // Táº¡o gá»£i Ã½ voucher gá»“m mÃ£ vÃ  tÃªn.
    private String taoGoiYVoucher(entity.ThanhToan.Voucher item) {
        String ten = item.getTenVoucher();
        return ten == null || ten.isBlank()
                ? item.getMaVoucher()
                : item.getMaVoucher() + " - " + ten;
    }

    // Láº¥y mÃ£ voucher tá»« ná»™i dung Ã´ nháº­p hoáº·c gá»£i Ã½ Ä‘Ã£ chá»n.
    private String layMaVoucherTuGoiY(String tuKhoa) {
        int separator = tuKhoa.indexOf(" - ");
        if (separator > 0) {
            String ma = tuKhoa.substring(0, separator);
            for (entity.ThanhToan.Voucher item : danhSachVoucher) {
                if (item.getMaVoucher().equalsIgnoreCase(ma)
                        && tuKhoa.equals(taoGoiYVoucher(item))) {
                    return ma;
                }
            }
        }
        return tuKhoa.trim();
    }

    // Láº¥y mÃ£ voucher Ä‘Ã£ nháº­p hoáº·c Ä‘Ã£ chá»n.
    private String layMaVoucherNhap() {
        Object item = cmbVoucher.getEditor().getItem();
        return item == null ? "" : layMaVoucherTuGoiY(item.toString());
    }

    private void capNhatVoucherSauThayDoiDonHang() {
        if (voucher != null) {
            try {
                String loiDieuKien = kiemTraDieuKienVoucher(voucher);
                if (loiDieuKien != null) {
                    String tenVoucher = voucher.getTenVoucher();
                    voucher = null;
                    cmbVoucher.getEditor().setItem("");
                    capNhatTien();
                    JOptionPane.showMessageDialog(
                            pnlNoiDung,
                            "Voucher " + tenVoucher + " Ä‘Ã£ Ä‘Æ°á»£c gá»¡ vÃ¬ " + loiDieuKien,
                            "Voucher khÃ´ng cÃ²n Ä‘á»§ Ä‘iá»u kiá»‡n",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException e) {
                hienLoi("KhÃ´ng thá»ƒ kiá»ƒm tra Ä‘iá»u kiá»‡n voucher: " + e.getMessage());
                return;
            }
        }
        capNhatGoiYVoucher();
    }

    // Táº¡o gá»£i y sáº£n pháº©m.
    // Tham sá»‘ ma: mÃ£.
    // Káº¿t quáº£: chuá»—i káº¿t quáº£.
    private String taoGoiYSanPham(String ma) {
        // ten: LÆ°u tÃªn.
        String ten = tenSanPhamTheoMa.get(ma);
        return ten == null || ten.isBlank() ? ma : ma + " - " + ten;
    }

    // Láº¥y mÃ£ sáº£n pháº©m tá»« gá»£i y.
    // Tham sá»‘ tuKhoa: tá»« khÃ³a tÃ¬m kiáº¿m.
    // Káº¿t quáº£: chuá»—i káº¿t quáº£.
    private String layMaSanPhamTuGoiY(String tuKhoa) {
        int separator = tuKhoa.indexOf(" - ");
        if (separator > 0) {
            // ma: LÆ°u mÃ£.
            String ma = tuKhoa.substring(0, separator);
            if (tenSanPhamTheoMa.containsKey(ma)) {
                return ma;
            }
        }
        return tuKhoa.trim();
    }

    // Kiá»ƒm tra mÃ£ cÃ³ chá»©a Ä‘á»§ cÃ¡c kÃ½ tá»± cá»§a tá»« khÃ³a Ä‘Ã£ chuáº©n hÃ³a hay khÃ´ng.
    // Tham sá»‘ ma: mÃ£.
    // Tham sá»‘ tuKhoaDaChuanHoa: tá»« khÃ³a tÃ¬m kiáº¿m Ä‘Ã£ chuáº©n hÃ³a.
    // Káº¿t quáº£: true/false theo Ä‘iá»u kiá»‡n xá»­ lÃ½.
    private boolean maCoChuaTatCaKyTu(String ma, String tuKhoaDaChuanHoa) {
        String maDaChuanHoa = ma.toLowerCase(Locale.ROOT);
        for (int i = 0; i < tuKhoaDaChuanHoa.length(); i++) {
            if (maDaChuanHoa.indexOf(tuKhoaDaChuanHoa.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }

    // XÃ¡c nháº­n khÃ¡ch hÃ ng.
    private void xacNhanKhachHang() {
        // tuKhoa: LÆ°u tá»« khÃ³a tÃ¬m kiáº¿m.
        String tuKhoa = layMaKhachHangNhap();
        if (tuKhoa.isEmpty()) {
            hienLoi("Nháº­p mÃ£ khÃ¡ch hÃ ng.");
            return;
        }
        try {
            entity.ThanhToan.KhachHang khachHang = dao.timKhachHang(tuKhoa);
            if (khachHang == null) {
                xoaKhachHangDaChon();
                hienLoi("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng.");
                return;
            }
            // phieuDichVus: Báº£ng Ã¡nh xáº¡ khÃ³a Ä‘áº¿n giÃ¡ trá»‹ dÃ¹ng cho cÃ¡c phiáº¿u dá»‹ch vá»¥.
            Map<String, List<entity.ThanhToan>> phieuDichVus = dao.layPhieuDichVuDangXuLy(khachHang.getMaKhachHang());
            xoaDichVuTuPhieuDaThem();
            maKhachHang = khachHang.getMaKhachHang();
            maKhachHangDaXacNhan = tuKhoa;
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            txtTenKhachHang.setText(khachHang.getHoTen());
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            txtSoDienThoai.setText(khachHang.getSoDienThoai());
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            txtKhachHangTomTat.setText(
                    maKhachHang + " - " + khachHang.getHoTen()
                            + " - " + khachHang.getSoDienThoai());
            maPhieuDichVuTrongDonHang.clear();
            for (Map.Entry<String, List<entity.ThanhToan>> phieu : phieuDichVus.entrySet()) {
                maPhieuDichVuTrongDonHang.add(phieu.getKey());
                for (entity.ThanhToan dichVu : phieu.getValue()) {
                    chiTiet.add(dichVu);
                    dichVuTuPhieuDaThem.add(dichVu);
                }
            }
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            txtMaPhieuDichVu.setText(maPhieuDichVuTrongDonHang.isEmpty()
                    ? "KhÃ´ng cÃ³ phiáº¿u cáº§n thanh toÃ¡n"
                    : String.join(", ", maPhieuDichVuTrongDonHang));
            tableModel.fireTableDataChanged();
            capNhatTien();
            capNhatVoucherSauThayDoiDonHang();
        } catch (Exception e) {
            hienLoi("KhÃ´ng tra cá»©u Ä‘Æ°á»£c khÃ¡ch hÃ ng: " + e.getMessage());
        }
    }

    // XÃ³a khÃ¡ch hÃ ng Ä‘Ã£ chá»n.
    private void xoaKhachHangDaChon() {
        xoaDichVuTuPhieuDaThem();
        maKhachHang = null;
        maKhachHangDaXacNhan = null;
        // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
        txtTenKhachHang.setText("");
        // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
        txtSoDienThoai.setText("");
        // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
        txtMaPhieuDichVu.setText("");
        // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
        txtKhachHangTomTat.setText("");
        capNhatVoucherSauThayDoiDonHang();
    }

    // XÃ³a cÃ¡c dÃ²ng dá»‹ch vá»¥ Ä‘Æ°á»£c thÃªm tá»« phiáº¿u.
    private void xoaDichVuTuPhieuDaThem() {
        if (!dichVuTuPhieuDaThem.isEmpty()) {
            chiTiet.removeAll(dichVuTuPhieuDaThem);
            dichVuTuPhieuDaThem.clear();
            tableModel.fireTableDataChanged();
            capNhatTien();
        }
        maPhieuDichVuTrongDonHang.clear();
    }

    // Láº¥y mÃ£ khÃ¡ch hÃ ng nháº­p.
    // Káº¿t quáº£: chuá»—i káº¿t quáº£.
    private String layMaKhachHangNhap() {
        Object item = cmbMaKhachHang.getEditor().getItem();
        return item == null ? "" : layMaKhachHangTuGoiY(item.toString());
    }

    // Má»Ÿ chá»n sáº£n pháº©m.
    private void moChonSanPham() {
        Object item = cmbMaSanPham.getEditor().getItem();
        moChonSanPham(item == null ? "" : layMaSanPhamTuGoiY(item.toString()).trim());
    }

    // Má»Ÿ chá»n sáº£n pháº©m.
    // Tham sá»‘ tuKhoa: tá»« khÃ³a tÃ¬m kiáº¿m.
    private void moChonSanPham(String tuKhoa) {
        tuKhoa = layMaSanPhamTuGoiY(tuKhoa).trim();
        if (tuKhoa.isEmpty()) {
            hienLoi("Nháº­p mÃ£ sáº£n pháº©m.");
            return;
        }
        try {
            // ketQua: LÆ°u káº¿t quáº£; pháº§n tá»­ cÃ³ kiá»ƒu entity.ThanhToan.
            List<entity.ThanhToan> ketQua = dao.timMatHangTheoMa(tuKhoa);
            if (ketQua.isEmpty()) {
                hienLoi("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m.");
                return;
            }
            entity.ThanhToan sanPham = ketQua.get(0);
            // spnQuantity: Ã” chá»n giÃ¡ trá»‹ báº±ng nÃºt tÄƒng/giáº£m sá»‘ lÆ°á»£ng.
            JSpinner spnQuantity = new JSpinner(new javax.swing.SpinnerNumberModel(1, 1, 999, 1));
            // lblItem: NhÃ£n hiá»ƒn thá»‹ má»¥c.
            JLabel lblItem = new JLabel(sanPham.getMaHang() + " - " + sanPham.getTenMatHang());
            // lblPrice: NhÃ£n hiá»ƒn thá»‹ giÃ¡.
            JLabel lblPrice = new JLabel(dinhDangTien(sanPham.getDonGia()));
            // pnlAddDialog: Khung chá»©a thÃ nh pháº§n giao diá»‡n thÃªm há»™p thoáº¡i.
            JPanel pnlAddDialog = new JPanel(new GridBagLayout());
            // gbc: RÃ ng buá»™c vá»‹ trÃ­, kÃ­ch thÆ°á»›c vÃ  khoáº£ng cÃ¡ch khi bá»‘ trÃ­ thÃ nh pháº§n báº±ng
            // GridBagLayout.
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(7, 8, 7, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            pnlAddDialog.add(new JLabel("MÃ£ / tÃªn:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.gridwidth = 2;
            pnlAddDialog.add(lblItem, gbc);
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.weightx = 0;
            pnlAddDialog.add(new JLabel("Sá»‘ lÆ°á»£ng:"), gbc);
            gbc.gridx = 1;
            pnlAddDialog.add(spnQuantity, gbc);
            gbc.gridy++;
            gbc.gridx = 0;
            pnlAddDialog.add(new JLabel("ÄÆ¡n giÃ¡:"), gbc);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            pnlAddDialog.add(lblPrice, gbc);
            int choice = JOptionPane.showConfirmDialog(
                    pnlNoiDung, pnlAddDialog, "ThÃªm sáº£n pháº©m",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice == JOptionPane.OK_OPTION) {
                themSanPham(sanPham, (Integer) spnQuantity.getValue());
                cmbMaSanPham.getEditor().setItem("");
            }
        } catch (Exception e) {
            hienLoi("KhÃ´ng tÃ¬m Ä‘Æ°á»£c sáº£n pháº©m: " + e.getMessage());
        }
    }

    // ThÃªm sáº£n pháº©m.
    // Tham sá»‘ sanPham: sáº£n pháº©m.
    // Tham sá»‘ soLuong: sá»‘ lÆ°á»£ng.
    private void themSanPham(entity.ThanhToan sanPham, int soLuong) {
        for (entity.ThanhToan item : chiTiet) {
            if (item.getMaHang().equals(sanPham.getMaHang())
                    && java.util.Objects.equals(item.getMaThuCung(), sanPham.getMaThuCung())) {
                item.setSoLuong(item.getSoLuong() + soLuong);
                tableModel.fireTableDataChanged();
                capNhatTien();
                capNhatVoucherSauThayDoiDonHang();
                return;
            }
        }
        sanPham.setSoLuong(soLuong);
        chiTiet.add(sanPham);
        tableModel.fireTableDataChanged();
        capNhatTien();
        capNhatVoucherSauThayDoiDonHang();
    }

    // XÃ³a sáº£n pháº©m.
    private void xoaSanPham() {
        // row: LÆ°u chá»‰ sá»‘ dÃ²ng.
        int row = tblDonHang.getSelectedRow();
        if (row < 0) {
            hienLoi("Chá»n sáº£n pháº©m cáº§n xÃ³a.");
            return;
        }
        int modelRow = tblDonHang.convertRowIndexToModel(row);
        if (dichVuTuPhieuDaThem.contains(chiTiet.get(modelRow))) {
            hienLoi("KhÃ´ng thá»ƒ xÃ³a dá»‹ch vá»¥ thuá»™c phiáº¿u dá»‹ch vá»¥ Ä‘ang chá» thanh toÃ¡n.");
            return;
        }
        chiTiet.remove(modelRow);
        tableModel.fireTableDataChanged();
        capNhatTien();
        capNhatVoucherSauThayDoiDonHang();
    }

    // Há»§y hÃ³a Ä‘Æ¡n.
    private void huyHoaDon() {
        if (chiTiet.isEmpty() && maKhachHang == null && voucher == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                pnlNoiDung, "Báº¡n muá»‘n há»§y hÃ³a Ä‘Æ¡n hiá»‡n táº¡i?",
                "XÃ¡c nháº­n há»§y", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            chiTiet.clear();
            tableModel.fireTableDataChanged();
            maKhachHang = null;
            maKhachHangDaXacNhan = null;
            cmbMaKhachHang.getEditor().setItem("");
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            txtTenKhachHang.setText("");
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            txtSoDienThoai.setText("");
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            txtMaPhieuDichVu.setText("");
            maPhieuDichVuTrongDonHang.clear();
            dichVuTuPhieuDaThem.clear();
            voucher = null;
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            cmbVoucher.getEditor().setItem("");
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            txtTienKhachDua.setText("");
            capNhatTien();
        }
    }

    // Ãp dá»¥ng voucher.
    private void apDungVoucher() {
        // ma: Äá»c ná»™i dung chá»¯ tá»« Ã´ nháº­p Ä‘á»ƒ xá»­ lÃ½ mÃ£.
        String ma = layMaVoucherNhap();
        if (ma.isEmpty()) {
            voucher = null;
            capNhatTien();
            return;
        }
        try {
            entity.ThanhToan.Voucher timDuoc = dao.timVoucher(ma);
            if (timDuoc == null) {
                hienLoi("Voucher khÃ´ng tá»“n táº¡i, chÆ°a Ä‘áº¿n ngÃ y báº¯t Ä‘áº§u, Ä‘Ã£ háº¿t háº¡n "
                        + "hoáº·c Ä‘Ã£ háº¿t lÆ°á»£t sá»­ dá»¥ng.");
                return;
            }
            String loiDieuKien = kiemTraDieuKienVoucher(timDuoc);
            if (loiDieuKien != null) {
                hienLoi(loiDieuKien);
                return;
            }
            voucher = timDuoc;
            capNhatTien();
            JOptionPane.showMessageDialog(pnlNoiDung, "ÄÃ£ Ã¡p dá»¥ng " + voucher.getTenVoucher()
                    + " (" + voucher.getPhanTramGiam().stripTrailingZeros().toPlainString()
                    + "%).");
        } catch (SQLException e) {
            hienLoi("KhÃ´ng Ã¡p dá»¥ng Ä‘Æ°á»£c voucher: " + e.getMessage());
        }
    }

    // Kiá»ƒm tra Ä‘Æ¡n hÃ ng, lÆ°u hÃ³a Ä‘Æ¡n vÃ  xá»­ lÃ½ bÆ°á»›c xÃ¡c nháº­n thanh toÃ¡n.
    private void thanhToan() {
        if (maKhachHang == null
                || !layMaKhachHangNhap().equals(maKhachHangDaXacNhan)) {
            xoaKhachHangDaChon();
            hienLoi("HÃ£y xÃ¡c nháº­n khÃ¡ch hÃ ng trÆ°á»›c khi thanh toÃ¡n.");
            return;
        }
        if (chiTiet.isEmpty()) {
            hienLoi("HÃ³a Ä‘Æ¡n chÆ°a cÃ³ sáº£n pháº©m.");
            return;
        }
        entity.ThanhToan.PhuongThuc phuongThuc = phuongThucHienTai;
        if (phuongThuc == null) {
            hienLoi("ChÆ°a cÃ³ phÆ°Æ¡ng thá»©c thanh toÃ¡n Ä‘ang hoáº¡t Ä‘á»™ng.");
            return;
        }
        if (voucher != null) {
            try {
                entity.ThanhToan.Voucher voucherMoi = dao.timVoucher(voucher.getMaVoucher());
                if (voucherMoi == null) {
                    voucher = null;
                    capNhatTien();
                    hienLoi("Voucher khÃ´ng cÃ²n hiá»‡u lá»±c, chÆ°a Ä‘áº¿n ngÃ y báº¯t Ä‘áº§u "
                            + "hoáº·c Ä‘Ã£ háº¿t lÆ°á»£t sá»­ dá»¥ng.");
                    return;
                }
                voucher = voucherMoi;
                String loiDieuKien = kiemTraDieuKienVoucher(voucher);
                if (loiDieuKien != null) {
                    hienLoi(loiDieuKien);
                    return;
                }
            } catch (SQLException e) {
                hienLoi("KhÃ´ng kiá»ƒm tra Ä‘Æ°á»£c Ä‘iá»u kiá»‡n voucher: " + e.getMessage());
                return;
            }
        }

        // tongThanhToan: GiÃ¡ trá»‹ tá»•ng tiá»n thanh toÃ¡n dÃ¹ng sá»‘ tháº­p phÃ¢n chÃ­nh xÃ¡c Ä‘á»ƒ
        // tÃ­nh toÃ¡n tiá»n/tá»· lá»‡.
        BigDecimal tongThanhToan = tinhTongThanhToan();
        boolean tienMat = phuongThuc.getTenPhuongThuc()
                .toLowerCase(Locale.forLanguageTag("vi-VN"))
                .contains("tiá»n máº·t");
        boolean chuyenKhoan = phuongThuc.getTenPhuongThuc()
                .toLowerCase(Locale.forLanguageTag("vi-VN"))
                .contains("chuyá»ƒn khoáº£n");
        if (tienMat) {
            // tienKhachDua: GiÃ¡ trá»‹ sá»‘ tiá»n khÃ¡ch Ä‘Æ°a dÃ¹ng sá»‘ tháº­p phÃ¢n chÃ­nh xÃ¡c Ä‘á»ƒ tÃ­nh
            // toÃ¡n tiá»n/tá»· lá»‡.
            BigDecimal tienKhachDua;
            try {
                tienKhachDua = docTienKhachDua();
            } catch (NumberFormatException e) {
                hienLoi("Nháº­p sá»‘ tiá»n khÃ¡ch Ä‘Æ°a há»£p lá»‡.");
                return;
            }
            if (tienKhachDua.compareTo(tongThanhToan) < 0) {
                hienLoi("Tiá»n khÃ¡ch Ä‘Æ°a chÆ°a Ä‘á»§ thanh toÃ¡n.");
                return;
            }
        }

        if (!chuyenKhoan) {
            int confirm = JOptionPane.showConfirmDialog(
                    pnlNoiDung, "XÃ¡c nháº­n thanh toÃ¡n " + dinhDangTien(tongThanhToan) + "?",
                    "Thanh toÃ¡n", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        ngayLap = LocalDateTime.now();
        capNhatNgayLap();
        // maHoaDon: LÆ°u mÃ£ hÃ³a Ä‘Æ¡n.
        String maHoaDon;
        try {
            maHoaDon = dao.taoMaHoaDon(maKhachHang, ngayLap);
        } catch (SQLException | IllegalArgumentException e) {
            hienLoi("KhÃ´ng táº¡o Ä‘Æ°á»£c mÃ£ hÃ³a Ä‘Æ¡n: " + e.getMessage());
            return;
        }
        if (chuyenKhoan && !hienMaQrDemo(maHoaDon, tongThanhToan)) {
            return;
        }

        try {
            String maHoaDonDaLuu = dao.taoHoaDon(
                    maHoaDon,
                    maNhanVien,
                    maKhachHang,
                    voucher == null ? null : voucher.getMaVoucher(),
                    phuongThuc.getMaPhuongThuc(),
                    layNgayLap(),
                    voucher == null ? BigDecimal.ZERO : voucher.getPhanTramGiam(),
                    chiTiet,
                    maPhieuDichVuTrongDonHang);
            if (!maPhieuDichVuTrongDonHang.isEmpty()) {
                capNhatPhieuDichVu.run();
            }
            String noiDungHoaDon = taoNoiDungHoaDon(
                    phuongThuc.getTenPhuongThuc(), tongThanhToan);
            noiDungHoaDon = "PET SHOP PRO\nHÃ“A ÄÆ N: " + maHoaDonDaLuu + "\n"
                    + noiDungHoaDon;
            hienXacNhanHoaDon(noiDungHoaDon, maHoaDonDaLuu);
            chiTiet.clear();
            tableModel.fireTableDataChanged();
            maKhachHang = null;
            maKhachHangDaXacNhan = null;
            maPhieuDichVuTrongDonHang.clear();
            dichVuTuPhieuDaThem.clear();
            cmbMaKhachHang.getEditor().setItem("");
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            txtTenKhachHang.setText("");
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            txtSoDienThoai.setText("");
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            txtMaPhieuDichVu.setText("");
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            txtKhachHangTomTat.setText("");
            voucher = null;
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            cmbVoucher.getEditor().setItem("");
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            txtTienKhachDua.setText("");
            ngayLap = LocalDateTime.now();
            capNhatNgayLap();
            capNhatTien();
        } catch (Exception e) {
            hienLoi("Thanh toÃ¡n tháº¥t báº¡i; hÃ³a Ä‘Æ¡n chÆ°a Ä‘Æ°á»£c lÆ°u: " + e.getMessage());
        }
    }

    // Hiá»ƒn thá»‹ mÃ£ QR minh há»a cho thÃ´ng tin hÃ³a Ä‘Æ¡n vÃ  sá»‘ tiá»n.
    // Tham sá»‘ maHoaDon: mÃ£ hÃ³a Ä‘Æ¡n.
    // Tham sá»‘ tongThanhToan: tá»•ng tiá»n thanh toÃ¡n.
    // Káº¿t quáº£: true/false theo Ä‘iá»u kiá»‡n xá»­ lÃ½.
    private boolean hienMaQrDemo(String maHoaDon, BigDecimal tongThanhToan) {
        // duLieuQr: LÆ°u ná»™i dung Ä‘Æ°á»£c mÃ£ hÃ³a trong QR.
        String duLieuQr = "PETSHOP-DEMO|" + maHoaDon + "|" + tongThanhToan.toPlainString();
        // qrImage: áº¢nh trong bá»™ nhá»› dÃ¹ng cho QR áº£nh.
        BufferedImage qrImage;
        try {
            qrImage = taoQrImage(duLieuQr);
        } catch (WriterException e) {
            hienLoi("KhÃ´ng táº¡o Ä‘Æ°á»£c mÃ£ QR: " + e.getMessage());
            return false;
        }

        // pnlContent: Khung chá»©a thÃ nh pháº§n giao diá»‡n ná»™i dung.
        JPanel pnlContent = new JPanel(new BorderLayout(8, 8));
        pnlContent.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlContent.add(new QrCodePanel(qrImage), BorderLayout.CENTER);
        // pnlDetails: Khung chá»©a thÃ nh pháº§n giao diá»‡n cÃ¡c dÃ²ng chi tiáº¿t.
        JPanel pnlDetails = new JPanel(new GridLayout(0, 1, 4, 4));
        pnlDetails.add(new JLabel("MÃ£ Ä‘Æ¡n: " + maHoaDon));
        pnlDetails.add(new JLabel("Sá»‘ tiá»n: " + dinhDangTien(tongThanhToan)));
        // lblNote: NhÃ£n hiá»ƒn thá»‹ ghi chÃº hÆ°á»›ng dáº«n.
        JLabel lblNote = new JLabel(
                "<html><b>QR minh há»a</b> â€” ChÆ°a káº¿t ná»‘i cá»•ng thanh toÃ¡n ngÃ¢n hÃ ng.</html>");
        lblNote.setForeground(new Color(180, 75, 25));
        pnlDetails.add(lblNote);
        pnlContent.add(pnlDetails, BorderLayout.SOUTH);

        int choice = JOptionPane.showOptionDialog(
                pnlNoiDung,
                pnlContent,
                "MÃ£ QR minh há»a",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[] { "XÃ¡c nháº­n", "Há»§y" },
                "Há»§y");
        return choice == 0;
    }

    // MÃ£ hÃ³a ná»™i dung thÃ nh áº£nh QR báº±ng thÆ° viá»‡n ZXing.
    // Tham sá»‘ duLieuQr: ná»™i dung Ä‘Æ°á»£c mÃ£ hÃ³a trong QR.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu BufferedImage.
    private static BufferedImage taoQrImage(String duLieuQr) throws WriterException {
        // hints: Báº£ng Ã¡nh xáº¡ khÃ³a Ä‘áº¿n giÃ¡ trá»‹ dÃ¹ng cho tÃ¹y chá»n mÃ£ hÃ³a.
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        BitMatrix matrix = new MultiFormatWriter().encode(
                duLieuQr, BarcodeFormat.QR_CODE, 280, 280, hints);
        // image: áº¢nh trong bá»™ nhá»› dÃ¹ng cho hÃ¬nh áº£nh.
        BufferedImage image = new BufferedImage(
                matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                image.setRGB(x, y, matrix.get(x, y)
                        ? Color.BLACK.getRGB()
                        : Color.WHITE.getRGB());
            }
        }
        return image;
    }

    // NhÃ³m dá»¯ liá»‡u hoáº·c chá»©c nÄƒng QR mÃ£ khung dÃ¹ng trong QuanLyThanhToan.
    private static class QrCodePanel extends JComponent {
        // SIZE: LÆ°u kÃ­ch thÆ°á»›c.
        private static final int SIZE = 280;
        // image: áº¢nh trong bá»™ nhá»› dÃ¹ng cho hÃ¬nh áº£nh.
        private final BufferedImage image;

        // Khá»Ÿi táº¡o Ä‘á»‘i tÆ°á»£ng tá»« cÃ¡c giÃ¡ trá»‹ Ä‘Æ°á»£c truyá»n vÃ o.
        // Tham sá»‘ image: hÃ¬nh áº£nh.
        private QrCodePanel(BufferedImage image) {
            this.image = image;
            setPreferredSize(new Dimension(SIZE, SIZE));
            setBackground(Color.WHITE);
            setOpaque(true);
        }

        // Váº½ ná»™i dung tÃ¹y chá»‰nh lÃªn thÃ nh pháº§n giao diá»‡n.
        // Tham sá»‘ graphics: ngá»¯ cáº£nh váº½.
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            // g2: Ngá»¯ cáº£nh váº½ dÃ¹ng Ä‘á»ƒ thiáº¿t láº­p mÃ u, phÃ´ng vÃ  váº½ ná»™i dung.
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
            int cellSize = Math.max(1,
                    Math.min(getWidth(), getHeight()) / image.getWidth());
            int qrSize = cellSize * image.getWidth();
            int left = (getWidth() - qrSize) / 2;
            int top = (getHeight() - qrSize) / 2;
            g2.setColor(Color.WHITE);
            g2.fillRect(left, top, qrSize, qrSize);
            g2.drawImage(image, left, top, qrSize, qrSize, null);
            g2.dispose();
        }
    }

    // Táº¡o ná»™i dung hÃ³a Ä‘Æ¡n.
    // Tham sá»‘ tenPhuongThuc: tÃªn phÆ°Æ¡ng thá»©c thanh toÃ¡n.
    // Tham sá»‘ tong: tá»•ng.
    // Káº¿t quáº£: chuá»—i káº¿t quáº£.
    private String taoNoiDungHoaDon(String tenPhuongThuc, BigDecimal tong) {
        // text: Bá»™ Ä‘á»‡m ghÃ©p cÃ¡c Ä‘oáº¡n chuá»—i Ä‘á»ƒ táº¡o ná»™i dung chá»¯.
        StringBuilder text = new StringBuilder();
        text.append("NgÃ y láº­p: ").append(layNgayLap().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append('\n');
        text.append("KhÃ¡ch hÃ ng: ").append(txtTenKhachHang.getText())
                .append(" - ").append(maKhachHang).append('\n');
        text.append("----------------------------------------\n");
        for (entity.ThanhToan item : chiTiet) {
            text.append(item.getTenMatHang()).append(" x")
                    .append(item.getSoLuong()).append("  ")
                    .append(dinhDangTien(item.getThanhTien())).append('\n');
        }
        text.append("----------------------------------------\n");
        text.append("Tá»•ng hÃ ng: ").append(dinhDangTien(tinhTongHang())).append('\n');
        if (voucher != null) {
            text.append("Voucher ").append(voucher.getTenVoucher()).append(": -")
                    .append(dinhDangTien(tinhTienGiam())).append('\n');
        }
        text.append("Tá»•ng thanh toÃ¡n: ").append(dinhDangTien(tong)).append('\n');
        text.append("PhÆ°Æ¡ng thá»©c: ").append(tenPhuongThuc).append('\n');
        if (tenPhuongThuc.toLowerCase(Locale.forLanguageTag("vi-VN")).contains("tiá»n máº·t")) {
            // tienDua: GiÃ¡ trá»‹ tiá»n Ä‘Æ°a dÃ¹ng sá»‘ tháº­p phÃ¢n chÃ­nh xÃ¡c Ä‘á»ƒ tÃ­nh toÃ¡n tiá»n/tá»·
            // lá»‡.
            BigDecimal tienDua = docTienKhachDua();
            text.append("KhÃ¡ch Ä‘Æ°a: ").append(dinhDangTien(tienDua)).append('\n');
            text.append("Tiá»n thá»«a: ").append(dinhDangTien(tienDua.subtract(tong))).append('\n');
        }
        text.append("\nCáº£m Æ¡n quÃ½ khÃ¡ch!");
        return text.toString();
    }

    // Xuáº¥t hÃ³a Ä‘Æ¡n Ã´ vÄƒn báº£n.
    // Tham sá»‘ noiDung: ná»™i dung.
    // Tham sá»‘ maHoaDon: mÃ£ hÃ³a Ä‘Æ¡n.
    private void xuatHoaDonTxt(String noiDung, String maHoaDon) {
        // fchChonTep: Há»™p chá»n tá»‡p bá»™ chá»n.
        JFileChooser fchChonTep = new JFileChooser();
        fchChonTep.setDialogTitle("LÆ°u hÃ³a Ä‘Æ¡n dáº¡ng TXT");
        fchChonTep.setSelectedFile(new java.io.File("HoaDon-" + maHoaDon + ".txt"));
        fchChonTep.setFileFilter(new FileNameExtensionFilter("Tá»‡p vÄƒn báº£n (*.txt)", "txt"));
        if (fchChonTep.showSaveDialog(pnlNoiDung) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // file: ÄÆ°á»ng dáº«n tá»‡p dÃ¹ng cho tá»‡p.
        Path file = fchChonTep.getSelectedFile().toPath();
        if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".txt")) {
            file = file.resolveSibling(file.getFileName() + ".txt");
        }
        if (Files.exists(file)
                && JOptionPane.showConfirmDialog(
                        pnlNoiDung,
                        "Tá»‡p Ä‘Ã£ tá»“n táº¡i. Báº¡n cÃ³ muá»‘n ghi Ä‘Ã¨ khÃ´ng?",
                        "XÃ¡c nháº­n ghi Ä‘Ã¨",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Files.write(file, noiDung.getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(
                    pnlNoiDung,
                    "ÄÃ£ lÆ°u hÃ³a Ä‘Æ¡n:\n" + file.toAbsolutePath(),
                    "Xuáº¥t hÃ³a Ä‘Æ¡n thÃ nh cÃ´ng",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            hienLoi("KhÃ´ng thá»ƒ xuáº¥t hÃ³a Ä‘Æ¡n: " + e.getMessage());
        }
    }

    // Hiá»ƒn thá»‹ xÃ¡c nhÃ¢n hÃ³a Ä‘Æ¡n.
    // Tham sá»‘ noiDung: ná»™i dung.
    // Tham sá»‘ maHoaDon: mÃ£ hÃ³a Ä‘Æ¡n.
    private void hienXacNhanHoaDon(String noiDung, String maHoaDon) {
        // txaPreview: VÃ¹ng vÄƒn báº£n nhiá»u dÃ²ng xem trÆ°á»›c.
        JTextArea txaPreview = new JTextArea(noiDung, 18, 38);
        txaPreview.setEditable(false);
        txaPreview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txaPreview.setCaretPosition(0);

        int action = JOptionPane.showOptionDialog(
                pnlNoiDung,
                new JScrollPane(txaPreview),
                "Thanh toÃ¡n thÃ nh cÃ´ng",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[] { "Xuáº¥t hÃ³a Ä‘Æ¡n", "ÄÃ³ng" },
                "Xuáº¥t hÃ³a Ä‘Æ¡n");
        if (action == 0) {
            xuatHoaDonTxt(noiDung, maHoaDon);
        }
    }

    // TÃ­nh tá»•ng tiá»n hÃ ng.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu BigDecimal.
    private BigDecimal tinhTongHang() {
        // tong: GiÃ¡ trá»‹ tá»•ng dÃ¹ng sá»‘ tháº­p phÃ¢n chÃ­nh xÃ¡c Ä‘á»ƒ tÃ­nh toÃ¡n tiá»n/tá»· lá»‡.
        BigDecimal tong = BigDecimal.ZERO;
        for (entity.ThanhToan item : chiTiet) {
            tong = tong.add(item.getThanhTien());
        }
        return tong;
    }

    // Äá»c sá»‘ tiá»n khÃ¡ch Ä‘Æ°a.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu BigDecimal.
    private BigDecimal docTienKhachDua() {
        // raw: Äá»c ná»™i dung chá»¯ tá»« Ã´ nháº­p Ä‘á»ƒ xá»­ lÃ½ dá»¯ liá»‡u thÃ´.
        String raw = txtTienKhachDua.getText().trim().replaceAll("[.,\\s]", "");
        if (!raw.matches("\\d+")) {
            throw new NumberFormatException("Chua nhap tien khach dua.");
        }
        return new BigDecimal(raw);
    }

    // Láº¥y ngÃ y giá» láº­p hÃ³a Ä‘Æ¡n.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu LocalDateTime.
    private LocalDateTime layNgayLap() {
        return ngayLap;
    }

    // Cáº­p nháº­t ngÃ y giá» láº­p hÃ³a Ä‘Æ¡n.
    private void capNhatNgayLap() {
        // Äá»‹nh dáº¡ng dá»¯ liá»‡u thÃ nh chuá»—i rá»“i hiá»ƒn thá»‹ trong control báº±ng setText().
        txtNgayLap.setText(ngayLap.format(
                DateTimeFormatter.ofPattern("d/M/yyyy")));
    }

    // Kiá»ƒm tra Ä‘iá»u kiá»‡n Ã¡p dá»¥ng voucher voucher.
    // Tham sá»‘ candidate: voucher cáº§n kiá»ƒm tra.
    // Káº¿t quáº£: chuá»—i káº¿t quáº£.
    private String kiemTraDieuKienVoucher(entity.ThanhToan.Voucher candidate)
            throws SQLException {
        // dieuKien: LÆ°u Ä‘iá»u kiá»‡n Ã¡p dá»¥ng voucher.
        String dieuKien = candidate.getDieuKien() == null ? "" : candidate.getDieuKien();
        java.util.regex.Matcher threshold = java.util.regex.Pattern
                .compile("(\\d{4,})").matcher(dieuKien);
        if (threshold.find()) {
            // mucToiThieu: GiÃ¡ trá»‹ má»¥c tá»›i thiá»‡u dÃ¹ng sá»‘ tháº­p phÃ¢n chÃ­nh xÃ¡c Ä‘á»ƒ tÃ­nh toÃ¡n
            // tiá»n/tá»· lá»‡.
            BigDecimal mucToiThieu = new BigDecimal(threshold.group(1));
            if (tinhTongHang().compareTo(mucToiThieu) < 0) {
                return "ÄÆ¡n chÆ°a Ä‘áº¡t Ä‘iá»u kiá»‡n tá»‘i thiá»ƒu "
                        + dinhDangTien(mucToiThieu) + ".";
            }
        }
        String lower = dieuKien.toLowerCase(Locale.forLanguageTag("vi-VN"));
        if ((lower.contains("thÃ nh viÃªn") || lower.contains("thanh vien"))
                && (maKhachHang == null || !dao.laKhachHangThanhVien(maKhachHang))) {
            return "Voucher nÃ y chá»‰ Ã¡p dá»¥ng cho khÃ¡ch hÃ ng thÃ nh viÃªn.";
        }
        return null;
    }

    // TÃ­nh tiá»n giáº£m.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu BigDecimal.
    private BigDecimal tinhTienGiam() {
        if (voucher == null) {
            return BigDecimal.ZERO;
        }
        return tinhTongHang()
                .multiply(voucher.getPhanTramGiam())
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    // TÃ­nh tá»•ng tiá»n thanh toÃ¡n.
    // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu BigDecimal.
    private BigDecimal tinhTongThanhToan() {
        return tinhTongHang().subtract(tinhTienGiam());
    }

    // Cáº­p nháº­t tiá»n.
    private void capNhatTien() {
        // tongHang: GiÃ¡ trá»‹ tá»•ng tiá»n hÃ ng dÃ¹ng sá»‘ tháº­p phÃ¢n chÃ­nh xÃ¡c Ä‘á»ƒ tÃ­nh toÃ¡n
        // tiá»n/tá»· lá»‡.
        BigDecimal tongHang = tinhTongHang();
        // giam: GiÃ¡ trá»‹ giáº£m dÃ¹ng sá»‘ tháº­p phÃ¢n chÃ­nh xÃ¡c Ä‘á»ƒ tÃ­nh toÃ¡n tiá»n/tá»· lá»‡.
        BigDecimal giam = tinhTienGiam();
        // tong: GiÃ¡ trá»‹ tá»•ng dÃ¹ng sá»‘ tháº­p phÃ¢n chÃ­nh xÃ¡c Ä‘á»ƒ tÃ­nh toÃ¡n tiá»n/tá»· lá»‡.
        BigDecimal tong = tongHang.subtract(giam);
        // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
        lblTongHang.setText(dinhDangTien(tongHang));
        // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
        lblGiamGia.setText(voucher == null ? "0 â‚«"
                : "-" + dinhDangTien(giam));
        // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
        lblTongThanhToan.setText(dinhDangTien(tong));
        entity.ThanhToan.PhuongThuc phuongThuc = phuongThucHienTai;
        boolean tienMat = phuongThuc != null
                && phuongThuc.getTenPhuongThuc().toLowerCase(Locale.forLanguageTag("vi-VN"))
                        .contains("tiá»n máº·t");
        txtTienKhachDua.setEnabled(tienMat);
        if (!tienMat) {
            // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
            lblTienThua.setText("â€”");
        } else {
            try {
                // tienDua: GiÃ¡ trá»‹ tiá»n Ä‘Æ°a dÃ¹ng sá»‘ tháº­p phÃ¢n chÃ­nh xÃ¡c Ä‘á»ƒ tÃ­nh toÃ¡n tiá»n/tá»·
                // lá»‡.
                BigDecimal tienDua = docTienKhachDua();
                // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
                lblTienThua.setText(tienDua.compareTo(tong) >= 0
                        ? dinhDangTien(tienDua.subtract(tong))
                        : "ChÆ°a Ä‘á»§ tiá»n");
            } catch (NumberFormatException e) {
                // Cáº­p nháº­t ná»™i dung chá»¯ hiá»ƒn thá»‹ trong control báº±ng setText().
                lblTienThua.setText("â€”");
            }
        }
    }

    // Äá»‹nh dáº¡ng sá»‘ tiá»n thÃ nh chuá»—i hiá»ƒn thá»‹.
    // Tham sá»‘ value: giÃ¡ trá»‹.
    // Káº¿t quáº£: chuá»—i káº¿t quáº£.
    private static String dinhDangTien(BigDecimal value) {
        synchronized (MONEY) {
            return MONEY.format(value) + " â‚«";
        }
    }

    // Hiá»ƒn thá»‹ lá»—i.
    // Tham sá»‘ message: thÃ´ng bÃ¡o.
    private void hienLoi(String message) {
        JOptionPane.showMessageDialog(pnlNoiDung, message, "Thanh toÃ¡n",
                JOptionPane.ERROR_MESSAGE);
    }

    // MÃ´ hÃ¬nh dá»¯ liá»‡u cho báº£ng Ä‘Æ¡n hÃ ng.
    private class DonHangModel extends AbstractTableModel {
        // columns: Máº£ng lÆ°u cÃ¡c cá»™t.
        private final String[] columns = {
                "MÃ£", "Sáº£n pháº©m/Dá»‹ch vá»¥", "Sá»‘ lÆ°á»£ng", "ÄÆ¡n giÃ¡", "ThÃ nh tiá»n"
        };

        // Tráº£ vá» sá»‘ dÃ²ng dá»¯ liá»‡u cá»§a mÃ´ hÃ¬nh báº£ng.
        @Override
        public int getRowCount() {
            return chiTiet.size();
        }

        // Tráº£ vá» sá»‘ cá»™t cá»§a mÃ´ hÃ¬nh báº£ng.
        @Override
        public int getColumnCount() {
            return columns.length;
        }

        // Tráº£ vá» tiÃªu Ä‘á» cá»§a cá»™t theo chá»‰ sá»‘.
        // Tham sá»‘ column: chá»‰ sá»‘ cá»™t.
        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        // Láº¥y giÃ¡ trá»‹ hiá»ƒn thá»‹ táº¡i má»™t dÃ²ng vÃ  cá»™t cá»§a báº£ng.
        // Tham sá»‘ row: chá»‰ sá»‘ dÃ²ng.
        // Tham sá»‘ column: chá»‰ sá»‘ cá»™t.
        // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu Object.
        @Override
        public Object getValueAt(int row, int column) {
            entity.ThanhToan item = chiTiet.get(row);
            switch (column) {
                case 0:
                    return item.getMaHang();
                case 1:
                    return item.getTenMatHang();
                case 2:
                    return item.getSoLuong();
                case 3:
                    return item.getDonGia();
                case 4:
                    return item.getThanhTien();
                default:
                    throw new IllegalArgumentException("Cot khong hop le.");
            }
        }

        // Quy Ä‘á»‹nh Ã´ cá»§a báº£ng cÃ³ Ä‘Æ°á»£c chá»‰nh sá»­a trá»±c tiáº¿p hay khÃ´ng.
        // Tham sá»‘ row: chá»‰ sá»‘ dÃ²ng.
        // Tham sá»‘ column: chá»‰ sá»‘ cá»™t.
        // Káº¿t quáº£: true/false theo Ä‘iá»u kiá»‡n xá»­ lÃ½.
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2 && !dichVuTuPhieuDaThem.contains(chiTiet.get(row));
        }

        // Cáº­p nháº­t giÃ¡ trá»‹ Ä‘Æ°á»£c nháº­p vÃ o Ã´ cá»§a báº£ng.
        // Tham sá»‘ value: giÃ¡ trá»‹ táº¡i má»›i.
        // Tham sá»‘ row: chá»‰ sá»‘ dÃ²ng.
        // Tham sá»‘ column: chá»‰ sá»‘ cá»™t.
        @Override
        public void setValueAt(Object value, int row, int column) {
            if (column != 2) {
                return;
            }
            try {
                int quantity = Integer.parseInt(value.toString().trim());
                chiTiet.get(row).setSoLuong(quantity);
                fireTableRowsUpdated(row, row);
                capNhatTien();
                capNhatVoucherSauThayDoiDonHang();
            } catch (IllegalArgumentException e) {
                hienLoi("Sá»‘ lÆ°á»£ng pháº£i lÃ  sá»‘ nguyÃªn lá»›n hÆ¡n 0.");
                fireTableRowsUpdated(row, row);
            }
        }
    }

    // Quy Ä‘á»‹nh cÃ¡ch hiá»ƒn thá»‹ Ã´ hoáº·c thÃ nh pháº§n tiá»n.
    private static class MoneyRenderer extends AlternatingRowRenderer {
        // Thiáº¿t láº­p thÃ nh pháº§n dÃ¹ng Ä‘á»ƒ váº½ má»™t Ã´ cá»§a báº£ng.
        // Tham sá»‘ tblDuLieu: báº£ng dá»¯ liá»‡u.
        // Tham sá»‘ value: giÃ¡ trá»‹.
        // Tham sá»‘ selected: tráº¡ng thÃ¡i Ä‘Æ°á»£c chá»n.
        // Tham sá»‘ focused: tráº¡ng thÃ¡i cÃ³ tiÃªu Ä‘iá»ƒm.
        // Tham sá»‘ row: chá»‰ sá»‘ dÃ²ng.
        // Tham sá»‘ column: chá»‰ sá»‘ cá»™t.
        // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu Component.
        @Override
        public Component getTableCellRendererComponent(
                JTable tblDuLieu,
                Object value,
                boolean selected,
                boolean focused,
                int row,
                int column) {
            Component component = super.getTableCellRendererComponent(
                    tblDuLieu, value, selected, focused, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            if (value instanceof BigDecimal) {
                setText(dinhDangTien((BigDecimal) value));
            }
            return component;
        }

    }

    // Quy Ä‘á»‹nh cÃ¡ch hiá»ƒn thá»‹ Ã´ hoáº·c thÃ nh pháº§n xen káº½ dÃ²ng.
    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        // Thiáº¿t láº­p thÃ nh pháº§n dÃ¹ng Ä‘á»ƒ váº½ má»™t Ã´ cá»§a báº£ng.
        // Tham sá»‘ tblDuLieu: báº£ng dá»¯ liá»‡u.
        // Tham sá»‘ value: giÃ¡ trá»‹.
        // Tham sá»‘ selected: tráº¡ng thÃ¡i Ä‘Æ°á»£c chá»n.
        // Tham sá»‘ focused: tráº¡ng thÃ¡i cÃ³ tiÃªu Ä‘iá»ƒm.
        // Tham sá»‘ row: chá»‰ sá»‘ dÃ²ng.
        // Tham sá»‘ column: chá»‰ sá»‘ cá»™t.
        // Káº¿t quáº£: Ä‘á»‘i tÆ°á»£ng/giÃ¡ trá»‹ kiá»ƒu Component.
        @Override
        public Component getTableCellRendererComponent(
                JTable tblDuLieu,
                Object value,
                boolean selected,
                boolean focused,
                int row,
                int column) {
            Component component = super.getTableCellRendererComponent(
                    tblDuLieu, value, selected, focused, row, column);
            if (!selected) {
                component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(239, 243, 247));
                component.setForeground(Color.DARK_GRAY);
            }
            return component;
        }
    }
}
