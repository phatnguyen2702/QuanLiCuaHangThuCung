package UI;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import control.ThanhToanControl;
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

public class QuanLyThanhToanUI extends JFrame implements ActionListener, MouseListener {

    private static final Color NAVY = new Color(18, 48, 82);
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);
    private static final Color GREEN = new Color(25, 157, 107);
    private static final Color BORDER = new Color(210, 220, 230);
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(
            Locale.forLanguageTag("vi-VN"));

    private final ThanhToanControl thanhToanControl = new ThanhToanControl();
    private final String maNhanVien;
    private final String tenNhanVien;
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout(10, 12));
    private final Runnable moLichSuHoaDon;
    private final Runnable capNhatPhieuDichVu;
    private final List<entity.ChiTietHoaDon> chiTiet = control.TrangChuControl.gioHang();
    private final List<entity.KhachHang> danhSachKhachHang = new ArrayList<>();
    private final List<String> danhSachMaSanPham = new ArrayList<>();
    private final Map<String, String> tenSanPhamTheoMa = new LinkedHashMap<>();
    private final List<entity.Voucher> danhSachVoucher = new ArrayList<>();
    private final List<String> maPhieuDichVuTrongDonHang = new ArrayList<>();
    private final List<entity.ChiTietHoaDon> dichVuTuPhieuDaThem = new ArrayList<>();
    private final DonHangModel tableModel = new DonHangModel();

    private final JComboBox<String> cmbMaKhachHang = new JComboBox<>();
    private final JTextField txtTenKhachHang = new JTextField();
    private final JTextField txtSoDienThoai = new JTextField(12);
    private final JTextField txtMaPhieuDichVu = new JTextField(18);
    private final JTextField txtKhachHangTomTat = new JTextField();
    private final JComboBox<String> cmbMaSanPham = new JComboBox<>();
    private final JComboBox<String> cmbVoucher = new JComboBox<>();
    private final JTextField txtTienKhachDua = new JTextField();
    private final JTextField txtNgayLap = new JTextField();
    private final JLabel lblTongHang = new JLabel();
    private final JLabel lblGiamGia = new JLabel();
    private final JLabel lblTongThanhToan = new JLabel();
    private final JLabel lblTienThua = new JLabel();
    private final JTable tblDonHang = new JTable(tableModel);
    private final JPanel pnlPaymentMethodsPanel = new JPanel(new GridLayout(1, 0, 8, 0));
    private final Map<JToggleButton, entity.PhuongThucThanhToan> phuongThucTheoNut = new LinkedHashMap<>();
    private final JButton btnApDungVoucher = new JButton("Áp dụng");

    private String maKhachHang;
    private String maKhachHangDaXacNhan;
    private boolean dangCapNhatGoiYMaKhachHang;
    private boolean dangCapNhatGoiYMaSanPham;
    private boolean dangCapNhatGoiYVoucher;
    private entity.PhuongThucThanhToan phuongThucHienTai;
    private entity.Voucher voucher;
    private LocalDateTime ngayLap = LocalDateTime.now();

    public QuanLyThanhToanUI(String maNhanVien, Runnable moLichSuHoaDon) {
        this(maNhanVien, moLichSuHoaDon, () -> {
        });
    }

    public QuanLyThanhToanUI(
            String maNhanVien, Runnable moLichSuHoaDon, Runnable capNhatPhieuDichVu) {
        this(maNhanVien, null, moLichSuHoaDon, capNhatPhieuDichVu);
    }

    public QuanLyThanhToanUI(
            String maNhanVien, String tenNhanVien,
            Runnable moLichSuHoaDon, Runnable capNhatPhieuDichVu) {
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.moLichSuHoaDon = moLichSuHoaDon;
        this.capNhatPhieuDichVu = capNhatPhieuDichVu;
        setTitle("Thanh toán hóa đơn");
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
        entity.PhuongThucThanhToan selectedMethod = phuongThucTheoNut.get(selectedButton);
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

    public void napGioHang() {
        tableModel.fireTableDataChanged();
        capNhatTien();
    }

    private JPanel createTitle() {
        JPanel pnlKhung = new JPanel(new BorderLayout());
        pnlKhung.setOpaque(false);
        JLabel lblTitle2 = new JLabel("Thanh toán hoá đơn");
        lblTitle2.setForeground(NAVY);
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 25));
        pnlKhung.add(lblTitle2, BorderLayout.WEST);
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);
        JButton btnLichSu = new JButton("Lịch sử hóa đơn");
        btnLichSu.setForeground(NAVY);
        btnLichSu.setFocusPainted(false);
        btnLichSu.setActionCommand("OPEN_INVOICE_HISTORY");
        btnLichSu.addActionListener(this);
        pnlActions.add(btnLichSu);

        JButton btnHuy = new JButton("Hủy hóa đơn  ×");
        btnHuy.setForeground(NAVY);
        btnHuy.setFocusPainted(false);
        btnHuy.setActionCommand("CANCEL_INVOICE");
        btnHuy.addActionListener(this);
        pnlActions.add(btnHuy);
        pnlKhung.add(pnlActions, BorderLayout.EAST);
        return pnlKhung;
    }

    private JPanel createBody() {
        JPanel pnlBody = new JPanel(new GridBagLayout());
        pnlBody.setOpaque(false);
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

    private JPanel createOrderPanel() {
        JPanel pnlKhung = new JPanel(new BorderLayout(0, 8));
        pnlKhung.setOpaque(false);
        pnlKhung.add(createCustomerPanel(), BorderLayout.NORTH);

        JPanel pnlOrder = new JPanel(new BorderLayout(6, 8));
        pnlOrder.setBackground(Color.WHITE);
        pnlOrder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("ĐƠN HÀNG"),
                BorderFactory.createEmptyBorder(5, 8, 8, 8)));
        JPanel pnlSearch = new JPanel(new BorderLayout(12, 0));
        pnlSearch.setOpaque(false);
        cmbMaSanPham.setEditable(true);
        cmbMaSanPham.setToolTipText("Nhập mã sản phẩm");
        Component editor = cmbMaSanPham.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            JTextField txtMaSanPham = (JTextField) editor;
            txtMaSanPham.setActionCommand("CHOOSE_PRODUCT");
            txtMaSanPham.addActionListener(this);
            txtMaSanPham.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    capNhatGoiYMaSanPham();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    capNhatGoiYMaSanPham();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    capNhatGoiYMaSanPham();
                }
            });
        }
        JPanel pnlSearchField = new JPanel(new BorderLayout(6, 0));
        pnlSearchField.setOpaque(false);
        pnlSearchField.add(new JLabel("Tìm sản phẩm"), BorderLayout.WEST);
        cmbMaSanPham.setToolTipText("Nhập mã hoặc tên sản phẩm");
        pnlSearchField.add(cmbMaSanPham, BorderLayout.CENTER);
        JButton btnTim = new JButton("Xác nhận");
        btnTim.setActionCommand("CHOOSE_PRODUCT");
        btnTim.addActionListener(this);
        JPanel pnlDatePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlDatePanel.setOpaque(false);
        pnlDatePanel.add(new JLabel("Ngày lập"));
        txtNgayLap.setEditable(false);
        txtNgayLap.setHorizontalAlignment(SwingConstants.CENTER);
        txtNgayLap.setPreferredSize(new Dimension(150, txtNgayLap.getPreferredSize().height));
        capNhatNgayLap();
        pnlDatePanel.add(txtNgayLap);
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
        JScrollPane scrCuon2 = new JScrollPane(tblDonHang);
        scrCuon2.setBorder(BorderFactory.createLineBorder(BORDER));
        pnlOrder.add(scrCuon2, BorderLayout.CENTER);

        pnlOrder.add(createOrderFooter(), BorderLayout.SOUTH);
        pnlKhung.add(pnlOrder, BorderLayout.CENTER);
        return pnlKhung;
    }

    private JPanel createCustomerPanel() {
        JPanel pnlKhung = new JPanel(new GridBagLayout());
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("KHÁCH HÀNG"),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        pnlKhung.add(new JLabel("Mã khách hàng"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.45;
        cmbMaKhachHang.setEditable(true);
        cmbMaKhachHang.setToolTipText("Nhập mã khách hàng");
        Component editor = cmbMaKhachHang.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            JTextField txtMaKhachHang = (JTextField) editor;
            txtMaKhachHang.setActionCommand("CONFIRM_CUSTOMER");
            txtMaKhachHang.addActionListener(this);
            txtMaKhachHang.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    capNhatGoiYMaKhachHang();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    capNhatGoiYMaKhachHang();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    capNhatGoiYMaKhachHang();
                }
            });
        }
        pnlKhung.add(cmbMaKhachHang, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton btnXacNhan = new JButton("Xác nhận");
        btnXacNhan.setMargin(new Insets(3, 8, 3, 8));
        btnXacNhan.setActionCommand("CONFIRM_CUSTOMER");
        btnXacNhan.addActionListener(this);
        pnlKhung.add(btnXacNhan, gbc);
        gbc.gridx = 3;
        gbc.weightx = 0;
        pnlKhung.add(new JLabel("Số điện thoại"), gbc);
        gbc.gridx = 4;
        gbc.weightx = 0.55;
        txtSoDienThoai.setEditable(false);
        pnlKhung.add(txtSoDienThoai, gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        pnlKhung.add(new JLabel("Tên khách hàng"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.45;
        txtTenKhachHang.setColumns(18);
        txtTenKhachHang.setEditable(false);
        pnlKhung.add(txtTenKhachHang, gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        pnlKhung.add(new JLabel("Mã phiếu dịch vụ"), gbc);
        gbc.gridx = 4;
        gbc.weightx = 0.55;
        txtMaPhieuDichVu.setEditable(false);
        txtMaPhieuDichVu.setToolTipText("Các phiếu dịch vụ đang xử lý của khách hàng");
        pnlKhung.add(txtMaPhieuDichVu, gbc);
        txtKhachHangTomTat.setEditable(false);
        return pnlKhung;
    }

    private JPanel createOrderFooter() {
        JPanel pnlFooter2 = new JPanel(new GridBagLayout());
        pnlFooter2.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 0, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.7;
        pnlFooter2.add(new JLabel("Khách hàng"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.3;
        pnlFooter2.add(new JLabel("Voucher"), gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.7;
        gbc.gridwidth = 1;
        txtKhachHangTomTat.setToolTipText("Khách hàng đã xác nhận");
        txtKhachHangTomTat.setEditable(false);
        pnlFooter2.add(txtKhachHangTomTat, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.3;
        JPanel pnlVoucherRow = new JPanel(new BorderLayout(5, 0));
        pnlVoucherRow.setOpaque(false);
        cmbVoucher.setEditable(true);
        cmbVoucher.setToolTipText("Nhập mã hoặc tên voucher");
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
        btnApDungVoucher.setActionCommand("APPLY_VOUCHER");
        btnApDungVoucher.addActionListener(this);
        JPanel pnlVoucherActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlVoucherActions.setOpaque(false);
        JButton btnXoa = new JButton("Xóa sản phẩm");
        btnXoa.setActionCommand("REMOVE_PRODUCT");
        btnXoa.addActionListener(this);
        pnlVoucherActions.add(btnApDungVoucher);
        pnlVoucherActions.add(btnXoa);
        pnlVoucherRow.add(pnlVoucherActions, BorderLayout.EAST);
        pnlFooter2.add(pnlVoucherRow, gbc);
        return pnlFooter2;
    }

    private JPanel createPaymentPanel() {
        JPanel pnlKhung = new JPanel(new BorderLayout(8, 10));
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlOverview = new JPanel(new GridBagLayout());
        pnlOverview.setBackground(Color.WHITE);
        pnlOverview.setBorder(BorderFactory.createTitledBorder("Chi tiết thanh toán"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.gridy = 0;
        pnlOverview.add(new JLabel("Tiền hàng"), gbc);
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
        pnlOverview.add(new JLabel("Tổng thanh toán"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        lblTongThanhToan.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTongThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTongThanhToan.setForeground(NAVY);
        pnlOverview.add(lblTongThanhToan, gbc);
        pnlKhung.add(pnlOverview, BorderLayout.NORTH);

        JPanel pnlPayment = new JPanel(new GridBagLayout());
        pnlPayment.setBackground(Color.WHITE);
        pnlPayment.setBorder(BorderFactory.createTitledBorder("Hình thức thanh toán"));
        GridBagConstraints pg = new GridBagConstraints();
        pg.gridx = 0;
        pg.gridy = 0;
        pg.weightx = 1;
        pg.fill = GridBagConstraints.HORIZONTAL;
        pg.insets = new Insets(5, 6, 8, 6);
        pnlPaymentMethodsPanel.setOpaque(false);
        pnlPayment.add(pnlPaymentMethodsPanel, pg);
        pg.gridy++;
        pnlPayment.add(new JLabel("Tiền khách đưa"), pg);
        pg.gridy++;
        txtTienKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                capNhatTien();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                capNhatTien();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                capNhatTien();
            }
        });
        pnlPayment.add(createMoneyField(txtTienKhachDua), pg);
        pg.gridy++;
        JPanel pnlChangePanel = new JPanel(new BorderLayout());
        pnlChangePanel.setBackground(LIGHT_BLUE);
        pnlChangePanel.setBorder(BorderFactory.createEmptyBorder(7, 9, 7, 9));
        pnlChangePanel.add(new JLabel("Số tiền thừa"), BorderLayout.WEST);
        lblTienThua.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlChangePanel.add(lblTienThua, BorderLayout.EAST);
        pnlPayment.add(pnlChangePanel, pg);

        JPanel pnlMiddle = new JPanel(new BorderLayout(0, 10));
        pnlMiddle.setOpaque(false);
        pnlMiddle.add(pnlPayment, BorderLayout.CENTER);
        pnlKhung.add(pnlMiddle, BorderLayout.CENTER);

        JButton btnThanhToan = new JButton("THANH TOÁN & IN HÓA ĐƠN");
        btnThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setBackground(GREEN);
        btnThanhToan.setFocusPainted(false);
        btnThanhToan.setPreferredSize(new Dimension(100, 48));
        btnThanhToan.setActionCommand("COMPLETE_PAYMENT");
        btnThanhToan.addActionListener(this);
        pnlKhung.add(btnThanhToan, BorderLayout.SOUTH);
        return pnlKhung;
    }

    private JPanel createMoneyField(JTextField txtField) {
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setBorder(BorderFactory.createLineBorder(BORDER));
        pnlWrapper.add(txtField, BorderLayout.CENTER);
        JLabel lblCurrency = new JLabel("VND");
        lblCurrency.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 7));
        pnlWrapper.add(lblCurrency, BorderLayout.EAST);
        return pnlWrapper;
    }

    private void napPhuongThucThanhToan() {
        try {
            List<entity.PhuongThucThanhToan> phuongThucs = thanhToanControl.layPhuongThucThanhToan();
            ButtonGroup group = new ButtonGroup();
            pnlPaymentMethodsPanel.removeAll();
            phuongThucTheoNut.clear();
            phuongThucHienTai = null;
            for (entity.PhuongThucThanhToan phuongThuc : phuongThucs) {
                JToggleButton tglThaoTac = new JToggleButton(
                        "<html><center>" + phuongThuc.getTenPhuongThuc() + "</center></html>");
                tglThaoTac.setFocusPainted(false);
                tglThaoTac.setBackground(Color.WHITE);
                tglThaoTac.setOpaque(true);
                tglThaoTac.setBorder(BorderFactory.createLineBorder(BORDER));
                tglThaoTac.setPreferredSize(new Dimension(95, 48));
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
            hienLoi("Không tải được phương thức thanh toán: " + e.getMessage());
        }
    }

    private void napDanhSachMaSanPham() {
        Object giaTriDangNhap = cmbMaSanPham.getEditor().getItem();
        try {
            Map<String, String> sanPhamMoi = thanhToanControl.layDanhSachSanPham();
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
            hienLoi("Không tải được danh sách mã sản phẩm: " + e.getMessage());
        }
    }

    public void napDanhSachMaKhachHang() {
        Object giaTriDangNhap = cmbMaKhachHang.getEditor().getItem();
        try {
            List<entity.KhachHang> danhSachMoi = thanhToanControl.layDanhSachKhachHang();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            String maDaiNhat = "KH000000";
            for (entity.KhachHang khachHang : danhSachMoi) {
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
            hienLoi("Không tải được danh sách mã khách hàng: " + e.getMessage());
        }
    }

    private void napDanhSachVoucher() {
        Object giaTriDangNhap = cmbVoucher.getEditor().getItem();
        try {
            List<entity.Voucher> voucherMoi = thanhToanControl.layDanhSachVoucher();
            String maDaiNhat = "VOUCHER000";
            for (entity.Voucher item : voucherMoi) {
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
            hienLoi("Không tải được danh sách voucher: " + e.getMessage());
        }
    }

    public void napDanhSachGoiYSanPhamVaVoucher() {
        napDanhSachMaSanPham();
        napDanhSachVoucher();
    }

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
            for (entity.KhachHang khachHang : danhSachKhachHang) {
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
                        entity.KhachHang khachHang = thanhToanControl.timKhachHang(maTheoTen);
                        if (khachHang != null) {
                            goiY.add(taoGoiYKhachHang(khachHang));
                        }
                    } catch (SQLException ex) {
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

    private String taoGoiYKhachHang(entity.KhachHang khachHang) {
        String ten = khachHang.getHoTen();
        return ten == null || ten.isBlank()
                ? khachHang.getMaKhachHang()
                : khachHang.getMaKhachHang() + " - " + ten;
    }

    private String layMaKhachHangTuGoiY(String tuKhoa) {
        if (tuKhoa == null) {
            return "";
        }
        String input = tuKhoa.trim();
        int separator = input.indexOf(" - ");
        if (separator > 0) {
            String ma = input.substring(0, separator);
            for (entity.KhachHang khachHang : danhSachKhachHang) {
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
        for (entity.KhachHang khachHang : danhSachKhachHang) {
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
            JTextField txtEditor = (JTextField) editorComponent;
            String tuKhoa = txtEditor.getText();
            String maTimKiem = layMaSanPhamTuGoiY(tuKhoa);
            String tenTimKiem = maTimKiem.toLowerCase(Locale.ROOT);
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
            List<entity.Voucher> goiY = new ArrayList<>();
            try {
                for (entity.Voucher item : danhSachVoucher) {
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
                hienLoi("Không thể kiểm tra điều kiện voucher: " + e.getMessage());
                return;
            }
            for (entity.Voucher item : goiY) {
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
                for (entity.Voucher item : goiY) {
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

    private String taoGoiYVoucher(entity.Voucher item) {
        String ten = item.getTenVoucher();
        return ten == null || ten.isBlank()
                ? item.getMaVoucher()
                : item.getMaVoucher() + " - " + ten;
    }

    private String layMaVoucherTuGoiY(String tuKhoa) {
        int separator = tuKhoa.indexOf(" - ");
        if (separator > 0) {
            String ma = tuKhoa.substring(0, separator);
            for (entity.Voucher item : danhSachVoucher) {
                if (item.getMaVoucher().equalsIgnoreCase(ma)
                        && tuKhoa.equals(taoGoiYVoucher(item))) {
                    return ma;
                }
            }
        }
        return tuKhoa.trim();
    }

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
                            "Voucher " + tenVoucher + " đã được gỡ vì " + loiDieuKien,
                            "Voucher không còn đủ điều kiện",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException e) {
                hienLoi("Không thể kiểm tra điều kiện voucher: " + e.getMessage());
                return;
            }
        }
        capNhatGoiYVoucher();
    }

    private String taoGoiYSanPham(String ma) {
        String ten = tenSanPhamTheoMa.get(ma);
        return ten == null || ten.isBlank() ? ma : ma + " - " + ten;
    }

    private String layMaSanPhamTuGoiY(String tuKhoa) {
        int separator = tuKhoa.indexOf(" - ");
        if (separator > 0) {
            String ma = tuKhoa.substring(0, separator);
            if (tenSanPhamTheoMa.containsKey(ma)) {
                return ma;
            }
        }
        return tuKhoa.trim();
    }

    private boolean maCoChuaTatCaKyTu(String ma, String tuKhoaDaChuanHoa) {
        String maDaChuanHoa = ma.toLowerCase(Locale.ROOT);
        for (int i = 0; i < tuKhoaDaChuanHoa.length(); i++) {
            if (maDaChuanHoa.indexOf(tuKhoaDaChuanHoa.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }

    private void xacNhanKhachHang() {
        String tuKhoa = layMaKhachHangNhap();
        if (tuKhoa.isEmpty()) {
            hienLoi("Nhập mã khách hàng.");
            return;
        }
        try {
            entity.KhachHang khachHang = thanhToanControl.timKhachHang(tuKhoa);
            if (khachHang == null) {
                xoaKhachHangDaChon();
                hienLoi("Không tìm thấy khách hàng.");
                return;
            }
            Map<String, List<entity.ChiTietHoaDon>> phieuDichVus = thanhToanControl.layPhieuDichVuDangXuLy(khachHang.getMaKhachHang());
            xoaDichVuTuPhieuDaThem();
            maKhachHang = khachHang.getMaKhachHang();
            maKhachHangDaXacNhan = tuKhoa;
            txtTenKhachHang.setText(khachHang.getHoTen());
            txtSoDienThoai.setText(khachHang.getSoDienThoai());
            txtKhachHangTomTat.setText(
                    maKhachHang + " - " + khachHang.getHoTen()
                            + " - " + khachHang.getSoDienThoai());
            maPhieuDichVuTrongDonHang.clear();
            for (Map.Entry<String, List<entity.ChiTietHoaDon>> phieu : phieuDichVus.entrySet()) {
                maPhieuDichVuTrongDonHang.add(phieu.getKey());
                for (entity.ChiTietHoaDon dichVu : phieu.getValue()) {
                    chiTiet.add(dichVu);
                    dichVuTuPhieuDaThem.add(dichVu);
                }
            }
            txtMaPhieuDichVu.setText(maPhieuDichVuTrongDonHang.isEmpty()
                    ? "Không có phiếu cần thanh toán"
                    : String.join(", ", maPhieuDichVuTrongDonHang));
            tableModel.fireTableDataChanged();
            capNhatTien();
            capNhatVoucherSauThayDoiDonHang();
        } catch (Exception e) {
            hienLoi("Không tra cứu được khách hàng: " + e.getMessage());
        }
    }

    private void xoaKhachHangDaChon() {
        xoaDichVuTuPhieuDaThem();
        maKhachHang = null;
        maKhachHangDaXacNhan = null;
        txtTenKhachHang.setText("");
        txtSoDienThoai.setText("");
        txtMaPhieuDichVu.setText("");
        txtKhachHangTomTat.setText("");
        capNhatVoucherSauThayDoiDonHang();
    }

    private void xoaDichVuTuPhieuDaThem() {
        if (!dichVuTuPhieuDaThem.isEmpty()) {
            chiTiet.removeAll(dichVuTuPhieuDaThem);
            dichVuTuPhieuDaThem.clear();
            tableModel.fireTableDataChanged();
            capNhatTien();
        }
        maPhieuDichVuTrongDonHang.clear();
    }

    private String layMaKhachHangNhap() {
        Object item = cmbMaKhachHang.getEditor().getItem();
        return item == null ? "" : layMaKhachHangTuGoiY(item.toString());
    }

    private void moChonSanPham() {
        Object item = cmbMaSanPham.getEditor().getItem();
        moChonSanPham(item == null ? "" : layMaSanPhamTuGoiY(item.toString()).trim());
    }

    private void moChonSanPham(String tuKhoa) {
        tuKhoa = layMaSanPhamTuGoiY(tuKhoa).trim();
        if (tuKhoa.isEmpty()) {
            hienLoi("Nhập mã sản phẩm.");
            return;
        }
        try {
            List<entity.ChiTietHoaDon> ketQua = thanhToanControl.timMatHangTheoMa(tuKhoa);
            if (ketQua.isEmpty()) {
                hienLoi("Không tìm thấy sản phẩm.");
                return;
            }
            entity.ChiTietHoaDon sanPham = ketQua.get(0);
            JSpinner spnQuantity = new JSpinner(new javax.swing.SpinnerNumberModel(1, 1, 999, 1));
            JLabel lblItem = new JLabel(sanPham.getMaHang() + " - " + sanPham.getTenMatHang());
            JLabel lblPrice = new JLabel(dinhDangTien(sanPham.getDonGia()));
            JPanel pnlAddDialog = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(7, 8, 7, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            pnlAddDialog.add(new JLabel("Mã / tên:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.gridwidth = 2;
            pnlAddDialog.add(lblItem, gbc);
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.weightx = 0;
            pnlAddDialog.add(new JLabel("Số lượng:"), gbc);
            gbc.gridx = 1;
            pnlAddDialog.add(spnQuantity, gbc);
            gbc.gridy++;
            gbc.gridx = 0;
            pnlAddDialog.add(new JLabel("Đơn giá:"), gbc);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            pnlAddDialog.add(lblPrice, gbc);
            int choice = JOptionPane.showConfirmDialog(
                    pnlNoiDung, pnlAddDialog, "Thêm sản phẩm",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice == JOptionPane.OK_OPTION) {
                themSanPham(sanPham, (Integer) spnQuantity.getValue());
                cmbMaSanPham.getEditor().setItem("");
            }
        } catch (Exception e) {
            hienLoi("Không tìm được sản phẩm: " + e.getMessage());
        }
    }

    private void themSanPham(entity.ChiTietHoaDon sanPham, int soLuong) {
        for (entity.ChiTietHoaDon item : chiTiet) {
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

    private void xoaSanPham() {
        int row = tblDonHang.getSelectedRow();
        if (row < 0) {
            hienLoi("Chọn sản phẩm cần xóa.");
            return;
        }
        int modelRow = tblDonHang.convertRowIndexToModel(row);
        if (dichVuTuPhieuDaThem.contains(chiTiet.get(modelRow))) {
            hienLoi("Không thể xóa dịch vụ thuộc phiếu dịch vụ đang chờ thanh toán.");
            return;
        }
        chiTiet.remove(modelRow);
        tableModel.fireTableDataChanged();
        capNhatTien();
        capNhatVoucherSauThayDoiDonHang();
    }

    private void huyHoaDon() {
        if (chiTiet.isEmpty() && maKhachHang == null && voucher == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                pnlNoiDung, "Bạn muốn hủy hóa đơn hiện tại?",
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            chiTiet.clear();
            tableModel.fireTableDataChanged();
            maKhachHang = null;
            maKhachHangDaXacNhan = null;
            cmbMaKhachHang.getEditor().setItem("");
            txtTenKhachHang.setText("");
            txtSoDienThoai.setText("");
            txtMaPhieuDichVu.setText("");
            maPhieuDichVuTrongDonHang.clear();
            dichVuTuPhieuDaThem.clear();
            voucher = null;
            cmbVoucher.getEditor().setItem("");
            txtTienKhachDua.setText("");
            capNhatTien();
        }
    }

    private void apDungVoucher() {
        String ma = layMaVoucherNhap();
        if (ma.isEmpty()) {
            voucher = null;
            capNhatTien();
            return;
        }
        try {
            entity.Voucher timDuoc = thanhToanControl.timVoucher(ma);
            if (timDuoc == null) {
                hienLoi("Voucher không tồn tại, chưa đến ngày bắt đầu, đã hết hạn "
                        + "hoặc đã hết lượt sử dụng.");
                return;
            }
            String loiDieuKien = kiemTraDieuKienVoucher(timDuoc);
            if (loiDieuKien != null) {
                hienLoi(loiDieuKien);
                return;
            }
            voucher = timDuoc;
            capNhatTien();
            JOptionPane.showMessageDialog(pnlNoiDung, "Đã áp dụng " + voucher.getTenVoucher()
                    + " (" + dinhDangPhanTram(voucher.getPhanTramGiam())
                    + "%).");
        } catch (SQLException e) {
            hienLoi("Không áp dụng được voucher: " + e.getMessage());
        }
    }

    private void thanhToan() {
        if (maKhachHang == null
                || !layMaKhachHangNhap().equals(maKhachHangDaXacNhan)) {
            xoaKhachHangDaChon();
            hienLoi("Hãy xác nhận khách hàng trước khi thanh toán.");
            return;
        }
        if (chiTiet.isEmpty()) {
            hienLoi("Hóa đơn chưa có sản phẩm.");
            return;
        }
        entity.PhuongThucThanhToan phuongThuc = phuongThucHienTai;
        if (phuongThuc == null) {
            hienLoi("Chưa có phương thức thanh toán đang hoạt động.");
            return;
        }
        if (voucher != null) {
            try {
                entity.Voucher voucherMoi = thanhToanControl.timVoucher(voucher.getMaVoucher());
                if (voucherMoi == null) {
                    voucher = null;
                    capNhatTien();
                    hienLoi("Voucher không còn hiệu lực, chưa đến ngày bắt đầu "
                            + "hoặc đã hết lượt sử dụng.");
                    return;
                }
                voucher = voucherMoi;
                String loiDieuKien = kiemTraDieuKienVoucher(voucher);
                if (loiDieuKien != null) {
                    hienLoi(loiDieuKien);
                    return;
                }
            } catch (SQLException e) {
                hienLoi("Không kiểm tra được điều kiện voucher: " + e.getMessage());
                return;
            }
        }

        double tongThanhToan = tinhTongThanhToan();
        boolean tienMat = phuongThuc.getTenPhuongThuc()
                .toLowerCase(Locale.forLanguageTag("vi-VN"))
                .contains("tiền mặt");
        boolean chuyenKhoan = phuongThuc.getTenPhuongThuc()
                .toLowerCase(Locale.forLanguageTag("vi-VN"))
                .contains("chuyển khoản");
        if (tienMat) {
            double tienKhachDua;
            try {
                tienKhachDua = docTienKhachDua();
            } catch (NumberFormatException e) {
                hienLoi("Nhập số tiền khách đưa hợp lệ.");
                return;
            }
            if (tienKhachDua < tongThanhToan) {
                hienLoi("Tiền khách đưa chưa đủ thanh toán.");
                return;
            }
        }

        if (!chuyenKhoan) {
            int confirm = JOptionPane.showConfirmDialog(
                    pnlNoiDung, "Xác nhận thanh toán " + dinhDangTien(tongThanhToan) + "?",
                    "Thanh toán", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        ngayLap = LocalDateTime.now();
        capNhatNgayLap();
        String maHoaDon;
        try {
            maHoaDon = thanhToanControl.taoMaHoaDon(maKhachHang, ngayLap);
        } catch (SQLException | IllegalArgumentException e) {
            hienLoi("Không tạo được mã hóa đơn: " + e.getMessage());
            return;
        }
        if (chuyenKhoan && !hienMaQrDemo(maHoaDon, tongThanhToan)) {
            return;
        }

        try {
            String maHoaDonDaLuu = thanhToanControl.taoHoaDon(
                    maHoaDon,
                    maNhanVien,
                    maKhachHang,
                    voucher == null ? null : voucher.getMaVoucher(),
                    phuongThuc.getMaPhuongThuc(),
                    layNgayLap(),
                    voucher == null ? 0 : voucher.getPhanTramGiam(),
                    chiTiet,
                    maPhieuDichVuTrongDonHang);
            if (!maPhieuDichVuTrongDonHang.isEmpty()) {
                capNhatPhieuDichVu.run();
            }
            String noiDungHoaDon = taoNoiDungHoaDon(
                    phuongThuc.getTenPhuongThuc(), tongThanhToan);
            noiDungHoaDon = "PET SHOP PRO\n"
                    + "Địa chỉ: 12 Nguyễn Văn Bảo, Hạnh Thông, Hồ Chí Minh\n"
                    + "Nhân viên lập: " + (tenNhanVien == null ? "" : tenNhanVien) + "\n"
                    + "HÓA ĐƠN: " + maHoaDonDaLuu + "\n"
                    + noiDungHoaDon;
            hienXacNhanHoaDon(noiDungHoaDon, maHoaDonDaLuu);
            chiTiet.clear();
            tableModel.fireTableDataChanged();
            maKhachHang = null;
            maKhachHangDaXacNhan = null;
            maPhieuDichVuTrongDonHang.clear();
            dichVuTuPhieuDaThem.clear();
            cmbMaKhachHang.getEditor().setItem("");
            txtTenKhachHang.setText("");
            txtSoDienThoai.setText("");
            txtMaPhieuDichVu.setText("");
            txtKhachHangTomTat.setText("");
            voucher = null;
            cmbVoucher.getEditor().setItem("");
            txtTienKhachDua.setText("");
            ngayLap = LocalDateTime.now();
            capNhatNgayLap();
            capNhatTien();
        } catch (Exception e) {
            hienLoi("Thanh toán thất bại; hóa đơn chưa được lưu: " + e.getMessage());
        }
    }

    private boolean hienMaQrDemo(String maHoaDon, double tongThanhToan) {
        String duLieuQr = "PETSHOP-DEMO|" + maHoaDon + "|" + Double.toString(tongThanhToan);
        BufferedImage qrImage;
        try {
            qrImage = taoQrImage(duLieuQr);
        } catch (WriterException e) {
            hienLoi("Không tạo được mã QR: " + e.getMessage());
            return false;
        }

        JPanel pnlContent = new JPanel(new BorderLayout(8, 8));
        pnlContent.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlContent.add(new QrCodePanel(qrImage), BorderLayout.CENTER);
        JPanel pnlDetails = new JPanel(new GridLayout(0, 1, 4, 4));
        pnlDetails.add(new JLabel("Mã đơn: " + maHoaDon));
        pnlDetails.add(new JLabel("Số tiền: " + dinhDangTien(tongThanhToan)));
        JLabel lblNote = new JLabel(
                "<html><b>QR minh họa</b> — Chưa kết nối cổng thanh toán ngân hàng.</html>");
        lblNote.setForeground(new Color(180, 75, 25));
        pnlDetails.add(lblNote);
        pnlContent.add(pnlDetails, BorderLayout.SOUTH);

        int choice = JOptionPane.showOptionDialog(
                pnlNoiDung,
                pnlContent,
                "Mã QR minh họa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[] { "Xác nhận", "Hủy" },
                "Hủy");
        return choice == 0;
    }

    private static BufferedImage taoQrImage(String duLieuQr) throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        BitMatrix matrix = new MultiFormatWriter().encode(
                duLieuQr, BarcodeFormat.QR_CODE, 280, 280, hints);
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

    private static class QrCodePanel extends JComponent {
        private static final int SIZE = 280;
        private final BufferedImage image;

        private QrCodePanel(BufferedImage image) {
            this.image = image;
            setPreferredSize(new Dimension(SIZE, SIZE));
            setBackground(Color.WHITE);
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
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

    private String taoNoiDungHoaDon(String tenPhuongThuc, double tong) {
        StringBuilder text = new StringBuilder();
        text.append("Ngày lập: ").append(layNgayLap().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append('\n');
        text.append("Khách hàng: ").append(txtTenKhachHang.getText())
                .append(" - ").append(maKhachHang).append('\n');
        text.append("----------------------------------------\n");
        for (entity.ChiTietHoaDon item : chiTiet) {
            text.append(item.getTenMatHang()).append(" x")
                    .append(item.getSoLuong()).append("  ")
                    .append(dinhDangTien(item.getThanhTien())).append('\n');
        }
        text.append("----------------------------------------\n");
        text.append("Tổng hàng: ").append(dinhDangTien(tinhTongHang())).append('\n');
        if (voucher != null) {
            text.append("Voucher ").append(voucher.getTenVoucher()).append(": -")
                    .append(dinhDangTien(tinhTienGiam())).append('\n');
        }
        text.append("Tổng thanh toán: ").append(dinhDangTien(tong)).append('\n');
        text.append("Phương thức: ").append(tenPhuongThuc).append('\n');
        if (tenPhuongThuc.toLowerCase(Locale.forLanguageTag("vi-VN")).contains("tiền mặt")) {
            double tienDua = docTienKhachDua();
            text.append("Khách đưa: ").append(dinhDangTien(tienDua)).append('\n');
            text.append("Tiền thừa: ").append(dinhDangTien(tienDua - tong)).append('\n');
        }
        text.append("\nCảm ơn quý khách!");
        return text.toString();
    }

    private void xuatHoaDonTxt(String noiDung, String maHoaDon) {
        JFileChooser fchChonTep = new JFileChooser();
        fchChonTep.setDialogTitle("Lưu hóa đơn dạng TXT");
        fchChonTep.setSelectedFile(new java.io.File("HoaDon-" + maHoaDon + ".txt"));
        fchChonTep.setFileFilter(new FileNameExtensionFilter("Tệp văn bản (*.txt)", "txt"));
        if (fchChonTep.showSaveDialog(pnlNoiDung) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path file = fchChonTep.getSelectedFile().toPath();
        if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".txt")) {
            file = file.resolveSibling(file.getFileName() + ".txt");
        }
        if (Files.exists(file)
                && JOptionPane.showConfirmDialog(
                        pnlNoiDung,
                        "Tệp đã tồn tại. Bạn có muốn ghi đè không?",
                        "Xác nhận ghi đè",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Files.write(file, noiDung.getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(
                    pnlNoiDung,
                    "Đã lưu hóa đơn:\n" + file.toAbsolutePath(),
                    "Xuất hóa đơn thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            hienLoi("Không thể xuất hóa đơn: " + e.getMessage());
        }
    }

    private void hienXacNhanHoaDon(String noiDung, String maHoaDon) {
        JTextArea txaPreview = new JTextArea(noiDung, 18, 38);
        txaPreview.setEditable(false);
        txaPreview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txaPreview.setCaretPosition(0);

        int action = JOptionPane.showOptionDialog(
                pnlNoiDung,
                new JScrollPane(txaPreview),
                "Thanh toán thành công",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[] { "Xuất hóa đơn", "Đóng" },
                "Xuất hóa đơn");
        if (action == 0) {
            xuatHoaDonTxt(noiDung, maHoaDon);
        }
    }

    private double tinhTongHang() {
        double tong = 0;
        for (entity.ChiTietHoaDon item : chiTiet) {
            tong += item.getThanhTien();
        }
        return tong;
    }

    private double docTienKhachDua() {
        String raw = txtTienKhachDua.getText().trim().replaceAll("[.,\\s]", "");
        if (!raw.matches("\\d+")) {
            throw new NumberFormatException("Chua nhap tien khach dua.");
        }
        double tienKhachDua = Double.parseDouble(raw);
        if (!Double.isFinite(tienKhachDua)) {
            throw new NumberFormatException("Số tiền khách đưa vượt giới hạn.");
        }
        return tienKhachDua;
    }

    private LocalDateTime layNgayLap() {
        return ngayLap;
    }

    private void capNhatNgayLap() {
        txtNgayLap.setText(ngayLap.format(
                DateTimeFormatter.ofPattern("d/M/yyyy")));
    }

    private String kiemTraDieuKienVoucher(entity.Voucher candidate)
            throws SQLException {
        String dieuKien = candidate.getDieuKien() == null ? "" : candidate.getDieuKien();
        java.util.regex.Matcher threshold = java.util.regex.Pattern
                .compile("(\\d{4,})").matcher(dieuKien);
        if (threshold.find()) {
            double mucToiThieu = Double.parseDouble(threshold.group(1));
            if (tinhTongHang() < mucToiThieu) {
                return "Đơn chưa đạt điều kiện tối thiểu "
                        + dinhDangTien(mucToiThieu) + ".";
            }
        }
        String lower = dieuKien.toLowerCase(Locale.forLanguageTag("vi-VN"));
        if ((lower.contains("thành viên") || lower.contains("thanh vien"))
                && (maKhachHang == null || !thanhToanControl.laKhachHangThanhVien(maKhachHang))) {
            return "Voucher này chỉ áp dụng cho khách hàng thành viên.";
        }
        return null;
    }

    private double tinhTienGiam() {
        if (voucher == null) {
            return 0;
        }
        return Math.round(tinhTongHang() * voucher.getPhanTramGiam() / 100);
    }

    private double tinhTongThanhToan() {
        return tinhTongHang() - tinhTienGiam();
    }

    private void capNhatTien() {
        double tongHang = tinhTongHang();
        double giam = tinhTienGiam();
        double tong = tongHang - giam;
        lblTongHang.setText(dinhDangTien(tongHang));
        lblGiamGia.setText(voucher == null ? "0 ₫"
                : "-" + dinhDangTien(giam));
        lblTongThanhToan.setText(dinhDangTien(tong));
        entity.PhuongThucThanhToan phuongThuc = phuongThucHienTai;
        boolean tienMat = phuongThuc != null
                && phuongThuc.getTenPhuongThuc().toLowerCase(Locale.forLanguageTag("vi-VN"))
                        .contains("tiền mặt");
        txtTienKhachDua.setEnabled(tienMat);
        if (!tienMat) {
            lblTienThua.setText("—");
        } else {
            try {
                double tienDua = docTienKhachDua();
                lblTienThua.setText(tienDua >= tong
                        ? dinhDangTien(tienDua - tong)
                        : "Chưa đủ tiền");
            } catch (NumberFormatException e) {
                lblTienThua.setText("—");
            }
        }
    }

    private static String dinhDangTien(double value) {
        synchronized (MONEY) {
            return MONEY.format(value) + " ₫";
        }
    }

    private static String dinhDangPhanTram(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
        format.setMaximumFractionDigits(2);
        return format.format(value);
    }

    private void hienLoi(String message) {
        JOptionPane.showMessageDialog(pnlNoiDung, message, "Thanh toán",
                JOptionPane.ERROR_MESSAGE);
    }

    private class DonHangModel extends AbstractTableModel {
        private final String[] columns = {
                "Mã", "Sản phẩm/Dịch vụ", "Số lượng", "Đơn giá", "Thành tiền"
        };

        @Override
        public int getRowCount() {
            return chiTiet.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            entity.ChiTietHoaDon item = chiTiet.get(row);
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

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2 && !dichVuTuPhieuDaThem.contains(chiTiet.get(row));
        }

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
                hienLoi("Số lượng phải là số nguyên lớn hơn 0.");
                fireTableRowsUpdated(row, row);
            }
        }
    }

    private static class MoneyRenderer extends AlternatingRowRenderer {
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
            if (value instanceof Number) {
                setText(dinhDangTien(((Number) value).doubleValue()));
            }
            return component;
        }

    }

    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
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
