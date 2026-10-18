package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
//QR
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultCellEditor;
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
import javax.swing.ListSelectionModel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

//QR
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import dao.ThanhToanDao;

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
    private final DonHangModel tableModel = new DonHangModel();

    private final JComboBox<String> cboMaKhachHang = new JComboBox<>();
    private final JTextField txtTenKhachHang = new JTextField();
    private final JTextField txtSoDienThoai = new JTextField();
    private final JTextField txtKhachHangTomTat = new JTextField();
    private final JComboBox<String> cboTimSanPham = new JComboBox<>();
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
    private final javax.swing.Timer timerGoiYSanPham =
            new javax.swing.Timer(300, e -> capNhatGoiYSanPham());
    private final javax.swing.Timer timerGoiYKhachHang =
            new javax.swing.Timer(300, e -> capNhatGoiYKhachHang());

    private String maKhachHang;
    private String maKhachHangDaXacNhan;
    private boolean dangCapNhatGoiYSanPham;
    private int lanTimGoiYSanPham;
    private boolean dangCapNhatGoiYKhachHang;
    private int lanTimGoiYKhachHang;
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
        napPhuongThucThanhToan();
        capNhatTien();
    }

    private JPanel createTitle() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel title = new JLabel("POS bán hàng tại quầy");
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
        cboTimSanPham.setEditable(true);
        cboTimSanPham.setToolTipText("Nhập mã sản phẩm/dịch vụ");
        JTextField editorTimSanPham = (JTextField) cboTimSanPham.getEditor().getEditorComponent();
        editorTimSanPham.addActionListener(e -> moChonSanPham());
        editorTimSanPham.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                datLaiTimerGoiYSanPham();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                datLaiTimerGoiYSanPham();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                datLaiTimerGoiYSanPham();
            }
        });
        JPanel searchField = new JPanel(new BorderLayout(6, 0));
        searchField.setOpaque(false);
        searchField.add(new JLabel("Tìm mã sản phẩm"), BorderLayout.WEST);
        searchField.add(cboTimSanPham, BorderLayout.CENTER);
        JButton btnTim = new JButton("Tìm kiếm");
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
        JTextField editorMaKhachHang =
                (JTextField) cboMaKhachHang.getEditor().getEditorComponent();
        editorMaKhachHang.addActionListener(e -> xacNhanKhachHang());
        editorMaKhachHang.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                datLaiTimerGoiYKhachHang();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                datLaiTimerGoiYKhachHang();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                datLaiTimerGoiYKhachHang();
            }
        });
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
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        txtTenKhachHang.setEditable(false);
        panel.add(txtTenKhachHang, gbc);
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
            maKhachHang = khachHang.getMaKhachHang();
            maKhachHangDaXacNhan = tuKhoa;
            txtTenKhachHang.setText(khachHang.getHoTen());
            txtSoDienThoai.setText(khachHang.getSoDienThoai());
            txtKhachHangTomTat.setText(
                    maKhachHang + " - " + khachHang.getHoTen()
                            + " - " + khachHang.getSoDienThoai());
        } catch (Exception e) {
            hienLoi("Không tra cứu được khách hàng: " + e.getMessage());
        }
    }

    private void xoaKhachHangDaChon() {
        maKhachHang = null;
        maKhachHangDaXacNhan = null;
        txtTenKhachHang.setText("");
        txtSoDienThoai.setText("");
        txtKhachHangTomTat.setText("");
    }

    private void datLaiTimerGoiYKhachHang() {
        if (dangCapNhatGoiYKhachHang) {
            return;
        }
        lanTimGoiYKhachHang++;
        timerGoiYKhachHang.stop();
        if (layMaKhachHangNhap().isEmpty()) {
            dangCapNhatGoiYKhachHang = true;
            cboMaKhachHang.setModel(new DefaultComboBoxModel<>());
            cboMaKhachHang.setEditable(true);
            dangCapNhatGoiYKhachHang = false;
        } else {
            timerGoiYKhachHang.restart();
        }
    }

    private void capNhatGoiYKhachHang() {
        String ma = layMaKhachHangNhap();
        if (ma.isEmpty()) {
            return;
        }

        int lanTimHienTai = ++lanTimGoiYKhachHang;
        new javax.swing.SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return dao.goiYKhachHangTheoMa(ma);
            }

            @Override
            protected void done() {
                if (lanTimHienTai != lanTimGoiYKhachHang) {
                    return;
                }
                try {
                    List<String> ketQua = get();
                    JTextField editor = (JTextField) cboMaKhachHang
                            .getEditor().getEditorComponent();
                    String noiDung = editor.getText();
                    int viTriConTro = editor.getCaretPosition();
                    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                    for (String maKhachHangGoiY : ketQua) {
                        model.addElement(maKhachHangGoiY);
                    }

                    dangCapNhatGoiYKhachHang = true;
                    cboMaKhachHang.setModel(model);
                    cboMaKhachHang.setEditable(true);
                    cboMaKhachHang.getEditor().setItem(noiDung);
                    editor = (JTextField) cboMaKhachHang.getEditor().getEditorComponent();
                    editor.setCaretPosition(Math.min(viTriConTro, editor.getText().length()));
                    dangCapNhatGoiYKhachHang = false;

                    if (!ketQua.isEmpty() && editor.hasFocus() && cboMaKhachHang.isShowing()) {
                        cboMaKhachHang.showPopup();
                    }
                } catch (Exception e) {
                    dangCapNhatGoiYKhachHang = false;
                    hienLoi("Không tải được gợi ý mã khách hàng: " + e.getMessage());
                }
            }
        }.execute();
    }

    private String layMaKhachHangNhap() {
        Object giaTri = cboMaKhachHang.getEditor().getItem();
        return giaTri == null ? "" : giaTri.toString().trim();
    }

    private void moChonSanPham() {
        String tuKhoa = layTuKhoaSanPham();
        if (tuKhoa.isEmpty()) {
            hienLoi("Nhập mã hoặc tên sản phẩm/dịch vụ cần tìm.");
            return;
        }
        try {
            List<entity.ThanhToan> ketQua = dao.timMatHang(tuKhoa);
            if (ketQua.isEmpty()) {
                hienLoi("Không tìm thấy sản phẩm.");
                return;
            }
            JComboBox<entity.ThanhToan> cbo = new JComboBox<>(
                    ketQua.toArray(new entity.ThanhToan[0]));
            cbo.setRenderer((list, value, index, selected, focus) -> {
                JLabel label = new JLabel(value == null ? "" :
                        value.getMaHang() + " - " + value.getTenMatHang());
                if (selected) {
                    label.setOpaque(true);
                    label.setBackground(list.getSelectionBackground());
                    label.setForeground(list.getSelectionForeground());
                }
                return label;
            });
            JSpinner quantity = new JSpinner(new javax.swing.SpinnerNumberModel(1, 1, 999, 1));
            JLabel price = new JLabel(dinhDangTien(
                    ((entity.ThanhToan) cbo.getSelectedItem()).getDonGia()));
            cbo.addActionListener(e -> {
                entity.ThanhToan selected = (entity.ThanhToan) cbo.getSelectedItem();
                if (selected != null) {
                    price.setText(dinhDangTien(selected.getDonGia()));
                }
            });
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
            addDialog.add(cbo, gbc);
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
                    this, addDialog, "Thêm sản phẩm / dịch vụ",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice == JOptionPane.OK_OPTION) {
                themSanPham((entity.ThanhToan) cbo.getSelectedItem(),
                        (Integer) quantity.getValue());
                cboTimSanPham.getEditor().setItem("");
            }
        } catch (Exception e) {
            hienLoi("Không tìm được sản phẩm: " + e.getMessage());
        }
    }

    private void datLaiTimerGoiYSanPham() {
        if (dangCapNhatGoiYSanPham) {
            return;
        }
        lanTimGoiYSanPham++;
        timerGoiYSanPham.stop();
        if (((JTextField) cboTimSanPham.getEditor().getEditorComponent())
                .getText().trim().isEmpty()) {
            dangCapNhatGoiYSanPham = true;
            cboTimSanPham.setModel(new DefaultComboBoxModel<>());
            cboTimSanPham.setEditable(true);
            dangCapNhatGoiYSanPham = false;
        } else {
            timerGoiYSanPham.restart();
        }
    }

    private void capNhatGoiYSanPham() {
        String tuKhoa = layTuKhoaSanPham();
        if (tuKhoa.isEmpty()) {
            return;
        }

        int lanTimHienTai = ++lanTimGoiYSanPham;
        new javax.swing.SwingWorker<List<entity.ThanhToan>, Void>() {
            @Override
            protected List<entity.ThanhToan> doInBackground() throws Exception {
                return dao.goiYMatHangTheoMa(tuKhoa);
            }

            @Override
            protected void done() {
                if (lanTimHienTai != lanTimGoiYSanPham) {
                    return;
                }
                try {
                    List<entity.ThanhToan> ketQua = get();
                    JTextField editor = (JTextField) cboTimSanPham
                            .getEditor().getEditorComponent();
                    String noiDung = editor.getText();
                    int viTriConTro = editor.getCaretPosition();
                    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                    for (entity.ThanhToan matHang : ketQua) {
                        model.addElement(matHang.getMaHang() + " - "
                                + matHang.getTenMatHang());
                    }

                    dangCapNhatGoiYSanPham = true;
                    cboTimSanPham.setModel(model);
                    cboTimSanPham.setEditable(true);
                    cboTimSanPham.getEditor().setItem(noiDung);
                    editor = (JTextField) cboTimSanPham.getEditor().getEditorComponent();
                    editor.setCaretPosition(Math.min(viTriConTro, editor.getText().length()));
                    dangCapNhatGoiYSanPham = false;

                    if (!ketQua.isEmpty() && editor.hasFocus() && cboTimSanPham.isShowing()) {
                        cboTimSanPham.showPopup();
                    }
                } catch (Exception e) {
                    dangCapNhatGoiYSanPham = false;
                    hienLoi("Không tải được gợi ý sản phẩm: " + e.getMessage());
                }
            }
        }.execute();
    }

    private String layTuKhoaSanPham() {
        Object giaTri = cboTimSanPham.getEditor().getItem();
        String noiDung = giaTri == null ? "" : giaTri.toString().trim();
        for (int i = 0; i < cboTimSanPham.getItemCount(); i++) {
            String luaChon = cboTimSanPham.getItemAt(i);
            if (luaChon.equals(noiDung)) {
                int dauPhanCach = luaChon.indexOf(" - ");
                return dauPhanCach >= 0
                        ? luaChon.substring(0, dauPhanCach).trim()
                        : luaChon;
            }
        }
        return noiDung;
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
        chiTiet.remove(tblDonHang.convertRowIndexToModel(row));
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
                    chiTiet);
            String noiDungHoaDon = taoNoiDungHoaDon(
                    phuongThuc.getTenPhuongThuc(), tongThanhToan);
            noiDungHoaDon = "PET SHOP PRO\nHÓA ĐƠN: " + maHoaDonDaLuu + "\n"
                    + noiDungHoaDon;
            hienXacNhanHoaDon(noiDungHoaDon, maHoaDonDaLuu);
            chiTiet.clear();
            tableModel.fireTableDataChanged();
            maKhachHang = null;
            maKhachHangDaXacNhan = null;
            cboMaKhachHang.getEditor().setItem("");
            txtTenKhachHang.setText("");
            txtSoDienThoai.setText("");
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
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);
            BitMatrix matrix = new QRCodeWriter().encode(
                    duLieuQr, BarcodeFormat.QR_CODE, 280, 280, hints);

            JPanel content = new JPanel(new BorderLayout(8, 8));
            content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            content.add(new QrCodePanel(matrix), BorderLayout.CENTER);
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
        } catch (WriterException e) {
            hienLoi("Không tạo được mã QR: " + e.getMessage());
            return false;
        }
    }

    private static class QrCodePanel extends JComponent {
        private static final int SIZE = 280;
        private final BitMatrix matrix;

        private QrCodePanel(BitMatrix matrix) {
            this.matrix = matrix;
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
                    Math.min(getWidth(), getHeight()) / matrix.getWidth());
            int qrSize = cellSize * matrix.getWidth();
            int left = (getWidth() - qrSize) / 2;
            int top = (getHeight() - qrSize) / 2;
            g2.setColor(Color.WHITE);
            g2.fillRect(left, top, qrSize, qrSize);
            g2.setColor(Color.BLACK);
            for (int x = 0; x < matrix.getWidth(); x++) {
                for (int y = 0; y < matrix.getHeight(); y++) {
                    if (matrix.get(x, y)) {
                        g2.fillRect(left + x * cellSize, top + y * cellSize,
                                cellSize, cellSize);
                    }
                }
            }
            g2.dispose();
        }
    }

    private String taoNoiDungHoaDon(String tenPhuongThuc, BigDecimal tong) {
        StringBuilder text = new StringBuilder();
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

    private void xuatHoaDonPdf(String noiDung, String maHoaDon) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hóa đơn PDF");
        chooser.setSelectedFile(new java.io.File("HoaDon-" + maHoaDon + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp PDF (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path file = chooser.getSelectedFile().toPath();
        if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            file = file.resolveSibling(file.getFileName() + ".pdf");
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
            Files.write(file, taoPdf(noiDung));
            JOptionPane.showMessageDialog(
                    this,
                    "Đã lưu hóa đơn PDF:\n" + file.toAbsolutePath(),
                    "Xuất hóa đơn thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            hienLoi("Hóa đơn đã lưu nhưng không thể xuất PDF: " + e.getMessage());
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
                new Object[] { "Xuất hóa đơn PDF", "Đóng" },
                "Xuất hóa đơn PDF");
        if (action == 0) {
            xuatHoaDonPdf(noiDung, maHoaDon);
        }
    }

    private byte[] taoPdf(String noiDung) throws IOException {
        final int pageWidth = 595;
        final int pageHeight = 842;
        final int scale = 2;
        final int lineHeight = 25;
        final int firstLineY = 72;
        List<String> lines = wrapNoiDung(noiDung, 470);
        int linesPerPage = (pageHeight - firstLineY - 50) / lineHeight;
        int pageCount = Math.max(1, (lines.size() + linesPerPage - 1) / linesPerPage);
        List<byte[]> objects = new ArrayList<>();
        objects.add(null);
        objects.add(null);
        StringBuilder pageReferences = new StringBuilder();

        for (int page = 0; page < pageCount; page++) {
            int pageObject = 3 + page * 3;
            int contentObject = pageObject + 1;
            int imageObject = pageObject + 2;
            pageReferences.append(pageObject).append(" 0 R ");
            BufferedImage image = new BufferedImage(
                    pageWidth * scale, pageHeight * scale, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.scale(scale, scale);
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, pageWidth, pageHeight);
                graphics.setColor(NAVY);
                graphics.setFont(new Font("Segoe UI", Font.BOLD, 18));
                graphics.drawString("PET SHOP PRO", 55, 42);
                graphics.setColor(Color.DARK_GRAY);
                graphics.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                int firstLine = page * linesPerPage;
                int lastLine = Math.min(firstLine + linesPerPage, lines.size());
                for (int line = firstLine; line < lastLine; line++) {
                    graphics.drawString(
                            lines.get(line),
                            55,
                            firstLineY + (line - firstLine) * lineHeight);
                }
            } finally {
                graphics.dispose();
            }

            byte[] compressedImage = nenAnhRgb(image);
            byte[] content = ascii("q\n"
                    + pageWidth + " 0 0 " + pageHeight + " 0 0 cm\n"
                    + "/Im0 Do\nQ\n");
            objects.add(ascii(pageObject + " 0 obj\n"
                    + "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 "
                    + pageWidth + " " + pageHeight + "] "
                    + "/Resources << /XObject << /Im0 " + imageObject + " 0 R >> >> "
                    + "/Contents " + contentObject + " 0 R >>\nendobj\n"));
            objects.add(streamObject(contentObject, "<<", content));
            objects.add(streamObject(
                    imageObject,
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

    private List<String> wrapNoiDung(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        Font font = new Font("Segoe UI", Font.PLAIN, 11);
        BufferedImage measureImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = measureImage.createGraphics();
        try {
            graphics.setFont(font);
            java.awt.FontMetrics metrics = graphics.getFontMetrics();
            for (String sourceLine : text.split("\\R", -1)) {
                String remaining = sourceLine;
                while (!remaining.isEmpty() && metrics.stringWidth(remaining) > maxWidth) {
                    int breakAt = remaining.length();
                    while (breakAt > 1
                            && metrics.stringWidth(remaining.substring(0, breakAt)) > maxWidth) {
                        breakAt--;
                    }
                    int space = remaining.lastIndexOf(' ', breakAt);
                    if (space > 0) {
                        breakAt = space;
                    }
                    lines.add(remaining.substring(0, breakAt));
                    remaining = remaining.substring(breakAt).trim();
                }
                lines.add(remaining);
            }
        } finally {
            graphics.dispose();
        }
        return lines;
    }

    private byte[] nenAnhRgb(BufferedImage image) throws IOException {
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

    private byte[] streamObject(int objectNumber, String dictionary, byte[] data)
            throws IOException {
        ByteArrayOutputStream object = new ByteArrayOutputStream();
        object.write(ascii(objectNumber + " 0 obj\n" + dictionary
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
        pdf.write(ascii("xref\n0 " + offsets.size() + "\n"));
        pdf.write(ascii("0000000000 65535 f \n"));
        for (int index = 1; index < offsets.size(); index++) {
            pdf.write(ascii(String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(index))));
        }
        pdf.write(ascii("trailer\n<< /Size " + offsets.size()
                + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n"));
        return pdf.toByteArray();
    }

    private byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.ISO_8859_1);
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
                "Mã", "Sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"
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
            return column == 2;
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
