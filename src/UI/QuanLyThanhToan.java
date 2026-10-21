package UI;

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

public class QuanLyThanhToan extends JPanel {

    private static final Color NAVY = new Color(18, 48, 82);
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);
    private static final Color GREEN = new Color(25, 157, 107);
    private static final Color BORDER = new Color(210, 220, 230);
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(
            Locale.forLanguageTag("vi-VN"));

    private final ThanhToanDao dao = new ThanhToanDao();
    private final String maNhanVien;
    private final Runnable moLichSuHoaDon;
    private final List<entity.ThanhToan> chiTiet = new ArrayList<>();
    private final List<String> danhSachMaKhachHang = new ArrayList<>();
    private final List<String> danhSachMaSanPham = new ArrayList<>();
    private final List<String> maPhieuDichVuTrongDonHang = new ArrayList<>();
    private final List<entity.ThanhToan> dichVuTuPhieuDaThem = new ArrayList<>();
    private final DonHangModel tableModel = new DonHangModel();

    private final JComboBox<String> cboMaKhachHang = new JComboBox<>();
    private final JTextField txtTenKhachHang = new JTextField();
    private final JTextField txtSoDienThoai = new JTextField(12);
    private final JTextField txtMaPhieuDichVu = new JTextField();
    private final JTextField txtKhachHangTomTat = new JTextField();
    private final JComboBox<String> cboMaSanPham = new JComboBox<>();
    private final JTextField txtVoucher = new JTextField();
    private final JTextField txtTienKhachDua = new JTextField();
    private final JTextField txtNgayLap = new JTextField();
    private final JLabel lblTongHang = new JLabel();
    private final JLabel lblGiamGia = new JLabel();
    private final JLabel lblTongThanhToan = new JLabel();
    private final JLabel lblTienThua = new JLabel();
    private final JTable tblDonHang = new JTable(tableModel);
    private final JPanel paymentMethodsPanel = new JPanel(new GridLayout(1, 0, 8, 0));
    private final JButton btnApDungVoucher = new JButton("Áp dụng");

    private String maKhachHang;
    private String maKhachHangDaXacNhan;
    private boolean dangCapNhatGoiYMaKhachHang;
    private boolean dangCapNhatGoiYMaSanPham;
    private entity.ThanhToan.PhuongThuc phuongThucHienTai;
    private entity.ThanhToan.Voucher voucher;
    private LocalDateTime ngayLap = LocalDateTime.now();

    public QuanLyThanhToan(String maNhanVien, Runnable moLichSuHoaDon) {
        this.maNhanVien = maNhanVien;
        this.moLichSuHoaDon = moLichSuHoaDon;
        setLayout(new BorderLayout(10, 12));
        setBackground(LIGHT_BLUE);
        setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 18));
        add(createTitle(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        napDanhSachMaKhachHang();
        napDanhSachMaSanPham();
        napPhuongThucThanhToan();
        capNhatTien();
    }

    private JPanel createTitle() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel title = new JLabel("Thanh toán hoá đơn");
        title.setForeground(NAVY);
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        panel.add(title, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnLichSu = new JButton("Lịch sử hóa đơn");
        btnLichSu.setForeground(NAVY);
        btnLichSu.setFocusPainted(false);
        btnLichSu.addActionListener(e -> moLichSuHoaDon.run());
        actions.add(btnLichSu);

        JButton btnHuy = new JButton("Hủy hóa đơn  ×");
        btnHuy.setForeground(NAVY);
        btnHuy.setFocusPainted(false);
        btnHuy.addActionListener(e -> huyHoaDon());
        actions.add(btnHuy);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 12);
        gbc.gridx = 0;
        gbc.weightx = 0.65;
        body.add(createOrderPanel(), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 0);
        body.add(createPaymentPanel(), gbc);
        return body;
    }

    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(createCustomerPanel(), BorderLayout.NORTH);

        JPanel order = new JPanel(new BorderLayout(6, 8));
        order.setBackground(Color.WHITE);
        order.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("ĐƠN HÀNG"),
                BorderFactory.createEmptyBorder(5, 8, 8, 8)));
        JPanel search = new JPanel(new BorderLayout(12, 0));
        search.setOpaque(false);
        cboMaSanPham.setEditable(true);
        cboMaSanPham.setToolTipText("Nhập mã sản phẩm");
        Component editor = cboMaSanPham.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            JTextField txtMaSanPham = (JTextField) editor;
            txtMaSanPham.addActionListener(e -> moChonSanPham());
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
        JPanel searchField = new JPanel(new BorderLayout(6, 0));
        searchField.setOpaque(false);
        searchField.add(new JLabel("Tìm sản phẩm"), BorderLayout.WEST);
        searchField.add(cboMaSanPham, BorderLayout.CENTER);
        JButton btnTim = new JButton("Xác nhận");
        btnTim.addActionListener(e -> moChonSanPham());
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        datePanel.setOpaque(false);
        datePanel.add(new JLabel("Ngày lập"));
        txtNgayLap.setEditable(false);
        txtNgayLap.setHorizontalAlignment(SwingConstants.CENTER);
        txtNgayLap.setPreferredSize(new Dimension(150, txtNgayLap.getPreferredSize().height));
        capNhatNgayLap();
        datePanel.add(txtNgayLap);
        JPanel searchLeft = new JPanel(new BorderLayout(6, 0));
        searchLeft.setOpaque(false);
        searchLeft.add(searchField, BorderLayout.CENTER);
        searchLeft.add(btnTim, BorderLayout.EAST);
        search.add(searchLeft, BorderLayout.CENTER);
        search.add(datePanel, BorderLayout.EAST);
        order.add(search, BorderLayout.NORTH);

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
        JScrollPane scroll = new JScrollPane(tblDonHang);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        order.add(scroll, BorderLayout.CENTER);

        order.add(createOrderFooter(), BorderLayout.SOUTH);
        panel.add(order, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("KHÁCH HÀNG"),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Mã khách hàng"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.45;
        cboMaKhachHang.setEditable(true);
        cboMaKhachHang.setToolTipText("Nhập mã khách hàng");
        Component editor = cboMaKhachHang.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            JTextField txtMaKhachHang = (JTextField) editor;
            txtMaKhachHang.addActionListener(e -> xacNhanKhachHang());
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
        panel.add(cboMaKhachHang, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton btnXacNhan = new JButton("Xác nhận");
        btnXacNhan.setMargin(new Insets(3, 8, 3, 8));
        btnXacNhan.addActionListener(e -> xacNhanKhachHang());
        panel.add(btnXacNhan, gbc);
        gbc.gridx = 3;
        gbc.weightx = 0;
        panel.add(new JLabel("Số điện thoại"), gbc);
        gbc.gridx = 4;
        gbc.weightx = 0.55;
        txtSoDienThoai.setEditable(false);
        panel.add(txtSoDienThoai, gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Tên khách hàng"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.45;
        txtTenKhachHang.setEditable(false);
        panel.add(txtTenKhachHang, gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Mã phiếu dịch vụ"), gbc);
        gbc.gridx = 4;
        gbc.weightx = 0.55;
        txtMaPhieuDichVu.setEditable(false);
        txtMaPhieuDichVu.setToolTipText("Các phiếu dịch vụ đang xử lý của khách hàng");
        panel.add(txtMaPhieuDichVu, gbc);
        txtKhachHangTomTat.setEditable(false);
        return panel;
    }

    private JPanel createOrderFooter() {
        JPanel footer = new JPanel(new GridBagLayout());
        footer.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 0, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.7;
        footer.add(new JLabel("Khách hàng"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.3;
        footer.add(new JLabel("Voucher"), gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.7;
        gbc.gridwidth = 1;
        txtKhachHangTomTat.setToolTipText("Khách hàng đã xác nhận");
        txtKhachHangTomTat.setEditable(false);
        footer.add(txtKhachHangTomTat, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.3;
        JPanel voucherRow = new JPanel(new BorderLayout(5, 0));
        voucherRow.setOpaque(false);
        voucherRow.add(txtVoucher, BorderLayout.CENTER);
        btnApDungVoucher.addActionListener(e -> apDungVoucher());
        JPanel voucherActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        voucherActions.setOpaque(false);
        JButton btnXoa = new JButton("Xóa sản phẩm");
        btnXoa.addActionListener(e -> xoaSanPham());
        voucherActions.add(btnApDungVoucher);
        voucherActions.add(btnXoa);
        voucherRow.add(voucherActions, BorderLayout.EAST);
        footer.add(voucherRow, gbc);
        return footer;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel overview = new JPanel(new GridBagLayout());
        overview.setBackground(Color.WHITE);
        overview.setBorder(BorderFactory.createTitledBorder("Chi tiết thanh toán"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.gridy = 0;
        overview.add(new JLabel("Tiền hàng"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        lblTongHang.setHorizontalAlignment(SwingConstants.RIGHT);
        overview.add(lblTongHang, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        overview.add(new JLabel("Voucher"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        lblGiamGia.setHorizontalAlignment(SwingConstants.RIGHT);
        overview.add(lblGiamGia, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        overview.add(new JLabel("Tổng thanh toán"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        lblTongThanhToan.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTongThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTongThanhToan.setForeground(NAVY);
        overview.add(lblTongThanhToan, gbc);
        panel.add(overview, BorderLayout.NORTH);

        JPanel payment = new JPanel(new GridBagLayout());
        payment.setBackground(Color.WHITE);
        payment.setBorder(BorderFactory.createTitledBorder("Hình thức thanh toán"));
        GridBagConstraints pg = new GridBagConstraints();
        pg.gridx = 0;
        pg.gridy = 0;
        pg.weightx = 1;
        pg.fill = GridBagConstraints.HORIZONTAL;
        pg.insets = new Insets(5, 6, 8, 6);
        paymentMethodsPanel.setOpaque(false);
        payment.add(paymentMethodsPanel, pg);
        pg.gridy++;
        payment.add(new JLabel("Tiền khách đưa"), pg);
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
        payment.add(createMoneyField(txtTienKhachDua), pg);
        pg.gridy++;
        JPanel changePanel = new JPanel(new BorderLayout());
        changePanel.setBackground(LIGHT_BLUE);
        changePanel.setBorder(BorderFactory.createEmptyBorder(7, 9, 7, 9));
        changePanel.add(new JLabel("Số tiền thừa"), BorderLayout.WEST);
        lblTienThua.setHorizontalAlignment(SwingConstants.RIGHT);
        changePanel.add(lblTienThua, BorderLayout.EAST);
        payment.add(changePanel, pg);

        JPanel middle = new JPanel(new BorderLayout(0, 10));
        middle.setOpaque(false);
        middle.add(payment, BorderLayout.CENTER);
        panel.add(middle, BorderLayout.CENTER);

        JButton btnThanhToan = new JButton("THANH TOÁN & IN HÓA ĐƠN");
        btnThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setBackground(GREEN);
        btnThanhToan.setFocusPainted(false);
        btnThanhToan.setPreferredSize(new Dimension(100, 48));
        btnThanhToan.addActionListener(e -> thanhToan());
        panel.add(btnThanhToan, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createMoneyField(JTextField field) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createLineBorder(BORDER));
        wrapper.add(field, BorderLayout.CENTER);
        JLabel currency = new JLabel("VND");
        currency.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 7));
        wrapper.add(currency, BorderLayout.EAST);
        return wrapper;
    }

    private void napPhuongThucThanhToan() {
        try {
            List<entity.ThanhToan.PhuongThuc> phuongThucs =
                    dao.layPhuongThucThanhToan();
            ButtonGroup group = new ButtonGroup();
            paymentMethodsPanel.removeAll();
            phuongThucHienTai = null;
            for (entity.ThanhToan.PhuongThuc phuongThuc : phuongThucs) {
                JToggleButton button = new JToggleButton(
                        "<html><center>" + phuongThuc.getTenPhuongThuc() + "</center></html>");
                button.setFocusPainted(false);
                button.setBackground(Color.WHITE);
                button.setOpaque(true);
                button.setBorder(BorderFactory.createLineBorder(BORDER));
                button.setPreferredSize(new Dimension(95, 48));
                button.addActionListener(e -> {
                    phuongThucHienTai = phuongThuc;
                    for (Component component : paymentMethodsPanel.getComponents()) {
                        if (component instanceof JToggleButton) {
                            JToggleButton methodButton = (JToggleButton) component;
                            methodButton.setBackground(methodButton.isSelected()
                                    ? new Color(215, 231, 244) : Color.WHITE);
                        }
                    }
                    capNhatTien();
                });
                group.add(button);
                paymentMethodsPanel.add(button);
                if (phuongThucHienTai == null) {
                    button.setSelected(true);
                    button.setBackground(new Color(215, 231, 244));
                    phuongThucHienTai = phuongThuc;
                }
            }
            paymentMethodsPanel.revalidate();
            paymentMethodsPanel.repaint();
        } catch (Exception e) {
            hienLoi("Không tải được phương thức thanh toán: " + e.getMessage());
        }
    }

    private void napDanhSachMaSanPham() {
        try {
            danhSachMaSanPham.clear();
            danhSachMaSanPham.addAll(dao.layDanhSachMaSanPham());
            for (String ma : danhSachMaSanPham) {
                cboMaSanPham.addItem(ma);
            }
            cboMaSanPham.setSelectedItem("");
        } catch (Exception e) {
            hienLoi("Không tải được danh sách mã sản phẩm: " + e.getMessage());
        }
    }

    private void napDanhSachMaKhachHang() {
        try {
            danhSachMaKhachHang.clear();
            danhSachMaKhachHang.addAll(dao.layDanhSachMaKhachHang());
            for (String ma : danhSachMaKhachHang) {
                cboMaKhachHang.addItem(ma);
            }
            cboMaKhachHang.setSelectedItem("");
        } catch (Exception e) {
            hienLoi("Không tải được danh sách mã khách hàng: " + e.getMessage());
        }
    }

    private void capNhatGoiYMaKhachHang() {
        if (dangCapNhatGoiYMaKhachHang) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (dangCapNhatGoiYMaKhachHang) {
                return;
            }

            Component editorComponent = cboMaKhachHang.getEditor().getEditorComponent();
            if (!(editorComponent instanceof JTextField)) {
                return;
            }
            JTextField editor = (JTextField) editorComponent;
            String tuKhoa = editor.getText();
            List<String> goiY = new ArrayList<>();
            for (String ma : danhSachMaKhachHang) {
                if (maCoChuaTatCaKyTu(ma, tuKhoa.toLowerCase(Locale.ROOT))) {
                    goiY.add(ma);
                }
            }

            int viTriConTro = editor.getCaretPosition();
            dangCapNhatGoiYMaKhachHang = true;
            try {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                for (String ma : goiY) {
                    model.addElement(ma);
                }
                cboMaKhachHang.setModel(model);
                cboMaKhachHang.setSelectedItem(tuKhoa);
                editor.setText(tuKhoa);
                editor.setCaretPosition(Math.min(viTriConTro, tuKhoa.length()));
            } finally {
                dangCapNhatGoiYMaKhachHang = false;
            }

            if (!tuKhoa.isEmpty() && !goiY.isEmpty()
                    && !(goiY.size() == 1 && goiY.get(0).equalsIgnoreCase(tuKhoa))) {
                cboMaKhachHang.showPopup();
            } else if (cboMaKhachHang.isPopupVisible()) {
                cboMaKhachHang.hidePopup();
            }
        });
    }

    private void capNhatGoiYMaSanPham() {
        if (dangCapNhatGoiYMaSanPham) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (dangCapNhatGoiYMaSanPham) {
                return;
            }

            Component editorComponent = cboMaSanPham.getEditor().getEditorComponent();
            if (!(editorComponent instanceof JTextField)) {
                return;
            }
            JTextField editor = (JTextField) editorComponent;
            String tuKhoa = editor.getText();
            List<String> goiY = new ArrayList<>();
            String tuKhoaKhongPhanBietHoaThuong = tuKhoa.toLowerCase(Locale.ROOT);
            for (String ma : danhSachMaSanPham) {
                if (maCoChuaTatCaKyTu(ma, tuKhoaKhongPhanBietHoaThuong)) {
                    goiY.add(ma);
                }
            }

            int viTriConTro = editor.getCaretPosition();
            dangCapNhatGoiYMaSanPham = true;
            try {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                for (String ma : goiY) {
                    model.addElement(ma);
                }
                cboMaSanPham.setModel(model);
                cboMaSanPham.setSelectedItem(tuKhoa);
                editor.setText(tuKhoa);
                editor.setCaretPosition(Math.min(viTriConTro, tuKhoa.length()));
            } finally {
                dangCapNhatGoiYMaSanPham = false;
            }

            if (!tuKhoa.isEmpty() && !goiY.isEmpty()
                    && !(goiY.size() == 1 && goiY.get(0).equalsIgnoreCase(tuKhoa))) {
                cboMaSanPham.showPopup();
            } else if (cboMaSanPham.isPopupVisible()) {
                cboMaSanPham.hidePopup();
            }
        });
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
            entity.ThanhToan.KhachHang khachHang = dao.timKhachHang(tuKhoa);
            if (khachHang == null) {
                xoaKhachHangDaChon();
                hienLoi("Không tìm thấy khách hàng.");
                return;
            }
            Map<String, List<entity.ThanhToan>> phieuDichVus =
                    dao.layPhieuDichVuDangXuLy(khachHang.getMaKhachHang());
            xoaDichVuTuPhieuDaThem();
            maKhachHang = khachHang.getMaKhachHang();
            maKhachHangDaXacNhan = tuKhoa;
            txtTenKhachHang.setText(khachHang.getHoTen());
            txtSoDienThoai.setText(khachHang.getSoDienThoai());
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
            txtMaPhieuDichVu.setText(maPhieuDichVuTrongDonHang.isEmpty()
                    ? "Không có phiếu cần thanh toán"
                    : String.join(", ", maPhieuDichVuTrongDonHang));
            tableModel.fireTableDataChanged();
            capNhatTien();
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
        Object item = cboMaKhachHang.getEditor().getItem();
        return item == null ? "" : item.toString().trim();
    }

    private void moChonSanPham() {
        Object item = cboMaSanPham.getEditor().getItem();
        moChonSanPham(item == null ? "" : item.toString().trim());
    }

    private void moChonSanPham(String tuKhoa) {
        tuKhoa = tuKhoa.trim();
        if (tuKhoa.isEmpty()) {
            hienLoi("Nhập mã sản phẩm.");
            return;
        }
        try {
            List<entity.ThanhToan> ketQua = dao.timSanPhamTheoMa(tuKhoa);
            if (ketQua.isEmpty()) {
                hienLoi("Không tìm thấy sản phẩm.");
                return;
            }
            entity.ThanhToan sanPham = ketQua.get(0);
            JSpinner quantity = new JSpinner(new javax.swing.SpinnerNumberModel(1, 1, 999, 1));
            JLabel item = new JLabel(sanPham.getMaHang() + " - " + sanPham.getTenMatHang());
            JLabel price = new JLabel(dinhDangTien(sanPham.getDonGia()));
            JPanel addDialog = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(7, 8, 7, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            addDialog.add(new JLabel("Mã / tên:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.gridwidth = 2;
            addDialog.add(item, gbc);
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.weightx = 0;
            addDialog.add(new JLabel("Số lượng:"), gbc);
            gbc.gridx = 1;
            addDialog.add(quantity, gbc);
            gbc.gridy++;
            gbc.gridx = 0;
            addDialog.add(new JLabel("Đơn giá:"), gbc);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            addDialog.add(price, gbc);
            int choice = JOptionPane.showConfirmDialog(
                    this, addDialog, "Thêm sản phẩm",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice == JOptionPane.OK_OPTION) {
                themSanPham(sanPham, (Integer) quantity.getValue());
                cboMaSanPham.getEditor().setItem("");
            }
        } catch (Exception e) {
            hienLoi("Không tìm được sản phẩm: " + e.getMessage());
        }
    }

    private void themSanPham(entity.ThanhToan sanPham, int soLuong) {
        for (entity.ThanhToan item : chiTiet) {
            if (item.getMaHang().equals(sanPham.getMaHang())) {
                item.setSoLuong(item.getSoLuong() + soLuong);
                tableModel.fireTableDataChanged();
                capNhatTien();
                return;
            }
        }
        sanPham.setSoLuong(soLuong);
        chiTiet.add(sanPham);
        tableModel.fireTableDataChanged();
        capNhatTien();
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
    }

    private void huyHoaDon() {
        if (chiTiet.isEmpty() && maKhachHang == null && voucher == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this, "Bạn muốn hủy hóa đơn hiện tại?",
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            chiTiet.clear();
            tableModel.fireTableDataChanged();
            maKhachHang = null;
            maKhachHangDaXacNhan = null;
            cboMaKhachHang.getEditor().setItem("");
            txtTenKhachHang.setText("");
            txtSoDienThoai.setText("");
            txtMaPhieuDichVu.setText("");
            maPhieuDichVuTrongDonHang.clear();
            dichVuTuPhieuDaThem.clear();
            voucher = null;
            txtVoucher.setText("");
            txtTienKhachDua.setText("");
            capNhatTien();
        }
    }

    private void apDungVoucher() {
        String ma = txtVoucher.getText().trim();
        if (ma.isEmpty()) {
            voucher = null;
            capNhatTien();
            return;
        }
        try {
            entity.ThanhToan.Voucher timDuoc = dao.timVoucher(ma);
            if (timDuoc == null) {
                hienLoi("Mã voucher không tồn tại hoặc đã hết hiệu lực.");
                return;
            }
            String loiDieuKien = kiemTraDieuKienVoucher(timDuoc);
            if (loiDieuKien != null) {
                hienLoi(loiDieuKien);
                return;
            }
            voucher = timDuoc;
            capNhatTien();
            JOptionPane.showMessageDialog(this, "Đã áp dụng " + voucher.getTenVoucher()
                    + " (" + voucher.getPhanTramGiam().stripTrailingZeros().toPlainString()
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
        entity.ThanhToan.PhuongThuc phuongThuc = phuongThucHienTai;
        if (phuongThuc == null) {
            hienLoi("Chưa có phương thức thanh toán đang hoạt động.");
            return;
        }
        if (voucher != null) {
            try {
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

        BigDecimal tongThanhToan = tinhTongThanhToan();
        boolean tienMat = phuongThuc.getTenPhuongThuc()
                .toLowerCase(Locale.forLanguageTag("vi-VN"))
                .contains("tiền mặt");
        boolean chuyenKhoan = phuongThuc.getTenPhuongThuc()
                .toLowerCase(Locale.forLanguageTag("vi-VN"))
                .contains("chuyển khoản");
        if (tienMat) {
            BigDecimal tienKhachDua;
            try {
                tienKhachDua = docTienKhachDua();
            } catch (NumberFormatException e) {
                hienLoi("Nhập số tiền khách đưa hợp lệ.");
                return;
            }
            if (tienKhachDua.compareTo(tongThanhToan) < 0) {
                hienLoi("Tiền khách đưa chưa đủ thanh toán.");
                return;
            }
        }

        if (!chuyenKhoan) {
            int confirm = JOptionPane.showConfirmDialog(
                    this, "Xác nhận thanh toán " + dinhDangTien(tongThanhToan) + "?",
                    "Thanh toán", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        ngayLap = LocalDateTime.now();
        capNhatNgayLap();
        String maHoaDon;
        try {
            maHoaDon = dao.taoMaHoaDon(maKhachHang, ngayLap);
        } catch (SQLException | IllegalArgumentException e) {
            hienLoi("Không tạo được mã hóa đơn: " + e.getMessage());
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
            String noiDungHoaDon = taoNoiDungHoaDon(
                    phuongThuc.getTenPhuongThuc(), tongThanhToan, maHoaDonDaLuu);
            hienXacNhanHoaDon(noiDungHoaDon, maHoaDonDaLuu);
            chiTiet.clear();
            tableModel.fireTableDataChanged();
            maKhachHang = null;
            maKhachHangDaXacNhan = null;
            maPhieuDichVuTrongDonHang.clear();
            dichVuTuPhieuDaThem.clear();
            cboMaKhachHang.getEditor().setItem("");
            txtTenKhachHang.setText("");
            txtSoDienThoai.setText("");
            txtMaPhieuDichVu.setText("");
            txtKhachHangTomTat.setText("");
            voucher = null;
            txtVoucher.setText("");
            txtTienKhachDua.setText("");
            ngayLap = LocalDateTime.now();
            capNhatNgayLap();
            capNhatTien();
        } catch (Exception e) {
            hienLoi("Thanh toán thất bại; hóa đơn chưa được lưu: " + e.getMessage());
        }
    }

    private boolean hienMaQrDemo(String maHoaDon, BigDecimal tongThanhToan) {
        String duLieuQr = "PETSHOP-DEMO|" + maHoaDon + "|" + tongThanhToan.toPlainString();
        BufferedImage qrImage = taoQrImage(duLieuQr);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(new QrCodePanel(qrImage), BorderLayout.CENTER);
        JPanel details = new JPanel(new GridLayout(0, 1, 4, 4));
        details.add(new JLabel("Mã đơn: " + maHoaDon));
        details.add(new JLabel("Số tiền: " + dinhDangTien(tongThanhToan)));
        JLabel note = new JLabel(
                "<html><b>QR Thanh Toán</b> — Quét mã để chuyển khoản.</html>");
        note.setForeground(new Color(180, 75, 25));
        details.add(note);
        content.add(details, BorderLayout.SOUTH);

        int choice = JOptionPane.showOptionDialog(
                this,
                content,
                "Mã QR chuyển khoản",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[] { "Xác nhận", "Hủy" },
                "Hủy");
        return choice == 0;
    }

    private static BufferedImage taoQrImage(String duLieuQr) {
        final int dimension = 21;
        boolean[][] data = new boolean[dimension][dimension];

        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                boolean isFinder = row == 0 || row == 6 || col == 0 || col == 6
                        || (row >= 2 && row <= 4 && col >= 2 && col <= 4);
                data[row][col] = isFinder;
                data[row][dimension - 7 + col] = isFinder;
                data[dimension - 7 + row][col] = isFinder;
            }
        }

        for (int index = 0; index < dimension; index++) {
            data[index][6] = index % 2 == 0;
            data[6][index] = index % 2 == 0;
        }

        int seed = 0;
        for (int row = 0; row < dimension; row++) {
            for (int col = 0; col < dimension; col++) {
                if (row < 7 && col < 7) {
                    continue;
                }
                if (row == 6 || col == 6) {
                    continue;
                }
                if (row >= dimension - 7 && col < 7) {
                    continue;
                }
                if (col >= dimension - 7 && row < 7) {
                    continue;
                }
                char ch = duLieuQr.charAt(seed % duLieuQr.length());
                boolean bit = ((ch + row * 7 + col * 11 + seed) % 2 == 0);
                data[row][col] = bit;
                seed++;
            }
        }

        BufferedImage image = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < dimension; y++) {
            for (int x = 0; x < dimension; x++) {
                image.setRGB(x, y, data[y][x] ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
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

    private String taoNoiDungHoaDon(
            String tenPhuongThuc, BigDecimal tong, String maHoaDon) {
        StringBuilder text = new StringBuilder();
        text.append("PET SHOP PRO\n");
        text.append("HÓA ĐƠN THANH TOÁN\n");
        text.append("Mã hóa đơn: ").append(maHoaDon).append('\n');
        text.append("Ngày lập: ").append(layNgayLap().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append('\n');
        text.append("Khách hàng: ").append(txtTenKhachHang.getText())
                .append(" - ").append(maKhachHang).append('\n');
        text.append("----------------------------------------\n");
        for (entity.ThanhToan item : chiTiet) {
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
            BigDecimal tienDua = docTienKhachDua();
            text.append("Khách đưa: ").append(dinhDangTien(tienDua)).append('\n');
            text.append("Tiền thừa: ").append(dinhDangTien(tienDua.subtract(tong))).append('\n');
        }
        text.append("\nCảm ơn quý khách!");
        return text.toString();
    }

    private void xuatHoaDonTxt(String noiDung, String maHoaDon) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hóa đơn TXT");
        chooser.setSelectedFile(new java.io.File("HoaDon-" + maHoaDon + ".txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp văn bản (*.txt)", "txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path file = chooser.getSelectedFile().toPath();
        if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".txt")) {
            file = file.resolveSibling(file.getFileName() + ".txt");
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
            Files.write(file, noiDung.getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(
                    this,
                    "Đã lưu hóa đơn:\n" + file.toAbsolutePath(),
                    "Xuất hóa đơn thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            hienLoi("Không thể lưu hóa đơn: " + e.getMessage());
        }
    }

    private void hienXacNhanHoaDon(String noiDung, String maHoaDon) {
        JTextArea preview = new JTextArea(noiDung, 18, 38);
        preview.setEditable(false);
        preview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        preview.setCaretPosition(0);

        int action = JOptionPane.showOptionDialog(
                this,
                new JScrollPane(preview),
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

    private BigDecimal tinhTongHang() {
        BigDecimal tong = BigDecimal.ZERO;
        for (entity.ThanhToan item : chiTiet) {
            tong = tong.add(item.getThanhTien());
        }
        return tong;
    }

    private BigDecimal docTienKhachDua() {
        String raw = txtTienKhachDua.getText().trim().replaceAll("[.,\\s]", "");
        if (!raw.matches("\\d+")) {
            throw new NumberFormatException("Chua nhap tien khach dua.");
        }
        return new BigDecimal(raw);
    }

    private LocalDateTime layNgayLap() {
        return ngayLap;
    }

    private void capNhatNgayLap() {
        txtNgayLap.setText(ngayLap.format(
                DateTimeFormatter.ofPattern("d/M/yyyy")));
    }

    private String kiemTraDieuKienVoucher(entity.ThanhToan.Voucher candidate)
            throws SQLException {
        String dieuKien = candidate.getDieuKien() == null ? "" : candidate.getDieuKien();
        java.util.regex.Matcher threshold = java.util.regex.Pattern
                .compile("(\\d{4,})").matcher(dieuKien);
        if (threshold.find()) {
            BigDecimal mucToiThieu = new BigDecimal(threshold.group(1));
            if (tinhTongHang().compareTo(mucToiThieu) < 0) {
                return "Đơn chưa đạt điều kiện tối thiểu "
                        + dinhDangTien(mucToiThieu) + ".";
            }
        }
        String lower = dieuKien.toLowerCase(Locale.forLanguageTag("vi-VN"));
        if ((lower.contains("thành viên") || lower.contains("thanh vien"))
                && (maKhachHang == null || !dao.laKhachHangThanhVien(maKhachHang))) {
            return "Voucher này chỉ áp dụng cho khách hàng thành viên.";
        }
        return null;
    }

    private BigDecimal tinhTienGiam() {
        if (voucher == null) {
            return BigDecimal.ZERO;
        }
        return tinhTongHang()
                .multiply(voucher.getPhanTramGiam())
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    private BigDecimal tinhTongThanhToan() {
        return tinhTongHang().subtract(tinhTienGiam());
    }

    private void capNhatTien() {
        BigDecimal tongHang = tinhTongHang();
        BigDecimal giam = tinhTienGiam();
        BigDecimal tong = tongHang.subtract(giam);
        lblTongHang.setText(dinhDangTien(tongHang));
        lblGiamGia.setText(voucher == null ? "0 ₫"
                : "-" + dinhDangTien(giam));
        lblTongThanhToan.setText(dinhDangTien(tong));
        entity.ThanhToan.PhuongThuc phuongThuc = phuongThucHienTai;
        boolean tienMat = phuongThuc != null
                && phuongThuc.getTenPhuongThuc().toLowerCase(Locale.forLanguageTag("vi-VN"))
                        .contains("tiền mặt");
        txtTienKhachDua.setEnabled(tienMat);
        if (!tienMat) {
            lblTienThua.setText("—");
        } else {
            try {
                BigDecimal tienDua = docTienKhachDua();
                lblTienThua.setText(tienDua.compareTo(tong) >= 0
                        ? dinhDangTien(tienDua.subtract(tong)) : "Chưa đủ tiền");
            } catch (NumberFormatException e) {
                lblTienThua.setText("—");
            }
        }
    }

    private static String dinhDangTien(BigDecimal value) {
        synchronized (MONEY) {
            return MONEY.format(value) + " ₫";
        }
    }

    private void hienLoi(String message) {
        JOptionPane.showMessageDialog(this, message, "Thanh toán",
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
            } catch (IllegalArgumentException e) {
                hienLoi("Số lượng phải là số nguyên lớn hơn 0.");
                fireTableRowsUpdated(row, row);
            }
        }
    }

    private static class MoneyRenderer extends AlternatingRowRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean selected,
                boolean focused,
                int row,
                int column) {
            Component component = super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            if (value instanceof BigDecimal) {
                setText(dinhDangTien((BigDecimal) value));
            }
            return component;
        }

    }

    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean selected,
                boolean focused,
                int row,
                int column) {
            Component component = super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column);
            if (!selected) {
                component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(239, 243, 247));
                component.setForeground(Color.DARK_GRAY);
            }
            return component;
        }
    }
}
