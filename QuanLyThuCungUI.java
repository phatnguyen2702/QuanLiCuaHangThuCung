package UI;

import control.ThuCungControl;
import dao.ThuCungDAO.ChuNuoi;
import dao.ThuCungDAO.HoSoThuCung;
import entity.ThuCung;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuanLyThuCungUI extends JPanel {
    private static final Color NAVY = new Color(18, 48, 82);
    private static final Color GREEN = new Color(72, 196, 62);
    private static final Color BLUE = new Color(91, 180, 235);
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);

    private final ThuCungControl control = new ThuCungControl();
    private final ArrayList<HoSoThuCung> dsTatCa = new ArrayList<>();
    private final ArrayList<HoSoThuCung> dsHienThi = new ArrayList<>();
    private JTextField txtTimKiem, txtMaTC, txtTenTC, txtCanNang, txtGiong;
    private JTextField txtTenKhachHang, txtMaKH;
    private JComboBox<String> cmbLocGioiTinh, cmbLocLoai;
    private JComboBox<String> cmbLoai2, cmbGioiTinh2;
    private JTextField txtTinhTrangLich;
    private JLabel lblFormTitle, lblChiTietChuNuoi;
    private JButton btnLuu, btnXoaHoSo, btnBoChon;
    private JDialog winHoSo;
    private HoSoThuCung hoSoDangSua;
    private JTable tblDuLieu;
    private DefaultTableModel tableModel;
    private ChuNuoi chuDaChon;
    private boolean dangNapChu = false;

    public QuanLyThuCungUI() {
        setLayout(new BorderLayout(18, 15));
        setBackground(LIGHT_BLUE);
        setBorder(new EmptyBorder(18, 24, 18, 24));
        JPanel pnlDau = new JPanel(new BorderLayout(12, 0));
        pnlDau.setOpaque(false);
        JLabel lblTitle = new JLabel("QUẢN LÝ HỒ SƠ THÚ CƯNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        JButton btnThem = nut("+ Thêm thú cưng", GREEN);
        btnThem.setPreferredSize(new Dimension(175, 39));
        btnThem.addActionListener(e -> moDialogHoSo(null));
        pnlDau.add(lblTitle, BorderLayout.WEST);
        pnlDau.add(btnThem, BorderLayout.EAST);
        add(pnlDau, BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        lamMoiDanhSach();
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) {
                lamMoiDanhSach();
            }
        });
    }

    private JPanel createTablePanel() {
        JPanel pnlKhung = new JPanel(new BorderLayout(0, 10));
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setBorder(new EmptyBorder(15, 15, 15, 15));
        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        pnlTim.setOpaque(false);
        txtTimKiem = new JTextField(13);
        txtTimKiem.setPreferredSize(new Dimension(170, 34));
        txtTimKiem.setToolTipText("Tìm mã/tên/giống thú cưng hoặc tên/SĐT chủ nuôi, không phân biệt dấu");
        cmbLocGioiTinh = new JComboBox<>(new String[] { "Giới tính", "Đực", "Cái" });
        cmbLocLoai = new JComboBox<>(new String[] { "Loại", "Chó", "Mèo", "Chim", "Khác" });
        cmbLocGioiTinh.setPreferredSize(new Dimension(92, 34));
        cmbLocLoai.setPreferredSize(new Dimension(90, 34));
        JButton btnTim = nut("Tìm kiếm", GREEN);
        btnTim.addActionListener(e -> locDuLieu());
        txtTimKiem.addActionListener(e -> locDuLieu());
        cmbLocGioiTinh.addActionListener(e -> locDuLieu());
        cmbLocLoai.addActionListener(e -> locDuLieu());
        pnlTim.add(txtTimKiem);
        pnlTim.add(cmbLocGioiTinh);
        pnlTim.add(cmbLocLoai);
        pnlTim.add(btnTim);
        pnlKhung.add(pnlTim, BorderLayout.NORTH);
        String[] cot = { "Mã TC", "Tên thú cưng", "Loài", "Giống",
                "Giới tính", "Cân nặng", "Chủ nuôi", "Số điện thoại", "Lịch hẹn" };
        tableModel = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblDuLieu = new JTable(tableModel);
        tblDuLieu.setRowHeight(39);
        tblDuLieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblDuLieu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDuLieu.getTableHeader().setBackground(NAVY);
        tblDuLieu.getTableHeader().setForeground(Color.WHITE);
        tblDuLieu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDuLieu.getTableHeader().setPreferredSize(new Dimension(100, 38));
        int[] rongCot = { 80, 145, 75, 140, 85, 96, 180, 140, 140 };
        for (int i = 0; i < rongCot.length; i++) {
            tblDuLieu.getColumnModel().getColumn(i).setPreferredWidth(rongCot[i]);
        }
        DefaultTableCellRenderer lblCanGiua = new DefaultTableCellRenderer();
        lblCanGiua.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[] { 0, 2, 4, 5, 8 }) {
            tblDuLieu.getColumnModel().getColumn(i).setCellRenderer(lblCanGiua);
        }
        tblDuLieu.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblDuLieu.setToolTipText("Nhấp một lần vào dòng để xem chi tiết, sửa hoặc xóa hồ sơ.");
        tblDuLieu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e))
                    return;
                int row = tblDuLieu.rowAtPoint(e.getPoint());
                if (row < 0)
                    return;
                int i = tblDuLieu.convertRowIndexToModel(row);
                if (i >= 0 && i < dsHienThi.size()) {
                    moDialogHoSo(dsHienThi.get(i));
                }
            }
        });
        JScrollPane scrBang = new JScrollPane(tblDuLieu);
        scrBang.getViewport().setBackground(Color.WHITE);
        pnlKhung.add(scrBang, BorderLayout.CENTER);
        JPanel pnlDuoi = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlDuoi.setOpaque(false);
        JButton btnLamMoi = nut("↻ Làm mới", BLUE);
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            cmbLocGioiTinh.setSelectedIndex(0);
            cmbLocLoai.setSelectedIndex(0);
            lamMoiDanhSach();
        });
        pnlDuoi.add(btnLamMoi);
        pnlKhung.add(pnlDuoi, BorderLayout.SOUTH);
        return pnlKhung;
    }

    private JPanel createFormPanel() {
        JPanel pnlKhung = new JPanel(new BorderLayout(0, 8));
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setPreferredSize(new Dimension(670, 610));
        pnlKhung.setBorder(new EmptyBorder(13, 14, 12, 14));

        lblFormTitle = new JLabel("Thêm hồ sơ thú cưng", SwingConstants.CENTER);
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblFormTitle.setBorder(new EmptyBorder(3, 0, 6, 0));
        pnlKhung.add(lblFormTitle, BorderLayout.NORTH);
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(4, 1, 6, 3));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 3, 5, 3);
        gbc.weightx = 1.0;
        int row = 0;

        txtMaTC = textField();
        txtMaTC.setEditable(false);
        txtMaTC.setBackground(new Color(245, 247, 250));
        themTruong(pnlForm, gbc, "Mã thú cưng", txtMaTC, row++);

        txtTenTC = textField();
        txtTenTC.setToolTipText("Có thể để trống: hệ thống tự tạo từ loài + giống");
        themTruong(pnlForm, gbc, "Tên thú cưng", txtTenTC, row++);

        cmbLoai2 = combo(new String[] { "Chó", "Mèo", "Chim", "Khác" });
        themTruong(pnlForm, gbc, "Loại", cmbLoai2, row++);
        txtGiong = textField();
        themTruong(pnlForm, gbc, "Giống", txtGiong, row++);
        cmbGioiTinh2 = combo(new String[] { "Đực", "Cái" });
        themTruong(pnlForm, gbc, "Giới tính", cmbGioiTinh2, row++);
        txtCanNang = textField();
        themTruong(pnlForm, gbc, "Cân nặng (kg)", txtCanNang, row++);

        txtTenKhachHang = textField();
        JButton btnChonKH = nut("Chọn", NAVY);
        btnChonKH.setPreferredSize(new Dimension(67, 34));
        btnChonKH.addActionListener(e -> chonKhachHang());
        JPanel pnlTenKH = new JPanel(new BorderLayout(5, 0));
        pnlTenKH.setOpaque(false);
        pnlTenKH.add(txtTenKhachHang, BorderLayout.CENTER);
        pnlTenKH.add(btnChonKH, BorderLayout.EAST);
        themTruong(pnlForm, gbc, "Tên khách hàng", pnlTenKH, row++);
        txtTenKhachHang.setToolTipText("Nhập tên không cần đúng dấu/chữ hoa, hoặc nhấn Chọn");
        txtTenKhachHang.getDocument().addDocumentListener(new DocumentListener() {
            private void thayDoi() {
                if (!dangNapChu) {
                    chuDaChon = null;
                    txtMaKH.setText("");
                    lblChiTietChuNuoi.setText("Chưa xác định chủ nuôi");
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                thayDoi();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                thayDoi();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                thayDoi();
            }
        });

        txtMaKH = textField();
        txtMaKH.setEditable(false);
        txtMaKH.setBackground(new Color(245, 247, 250));
        themTruong(pnlForm, gbc, "Mã khách hàng", txtMaKH, row++);

        txtTinhTrangLich = textField();
        txtTinhTrangLich.setEditable(false);
        txtTinhTrangLich.setBackground(new Color(245, 247, 250));
        txtTinhTrangLich.setToolTipText("Tự xác định theo lịch hẹn sắp tới chưa hủy trong SQL Server.");
        themTruong(pnlForm, gbc, "Lịch hẹn Spa", txtTinhTrangLich, row++);
        JPanel pnlThongTinChu = new JPanel(new BorderLayout(0, 3));
        pnlThongTinChu.setOpaque(false);
        pnlThongTinChu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 223, 233)),
                new EmptyBorder(7, 9, 7, 9)));
        JLabel lblNhom = new JLabel("THÔNG TIN CHỦ NUÔI");
        lblNhom.setForeground(NAVY);
        lblNhom.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChiTietChuNuoi = new JLabel("Chưa xác định chủ nuôi");
        lblChiTietChuNuoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlThongTinChu.add(lblNhom, BorderLayout.NORTH);
        pnlThongTinChu.add(lblChiTietChuNuoi, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        pnlForm.add(pnlThongTinChu, gbc);
        gbc.gridwidth = 1;
        JScrollPane scrForm = new JScrollPane(pnlForm,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrForm.setBorder(null);
        scrForm.getVerticalScrollBar().setUnitIncrement(14);
        pnlKhung.add(scrForm, BorderLayout.CENTER);

        btnLuu = nut("Thêm hồ sơ", GREEN);
        btnLuu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLuu.addActionListener(e -> {
            if (hoSoDangSua == null)
                them();
            else
                sua();
        });
        btnXoaHoSo = nut("Xóa hồ sơ", new Color(210, 60, 76));
        btnXoaHoSo.setPreferredSize(new Dimension(130, 38));
        btnXoaHoSo.addActionListener(e -> xoa());
        btnBoChon = nut("Đóng", new Color(105, 117, 133));
        btnBoChon.addActionListener(e -> {
            if (winHoSo != null)
                winHoSo.dispose();
        });
        btnLuu.setPreferredSize(new Dimension(150, 38));
        btnBoChon.setPreferredSize(new Dimension(110, 38));
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        pnlNut.setOpaque(false);
        pnlNut.setBorder(new EmptyBorder(6, 2, 0, 2));
        pnlNut.add(btnLuu);
        pnlNut.add(btnXoaHoSo);
        pnlNut.add(btnBoChon);
        pnlKhung.add(pnlNut, BorderLayout.SOUTH);
        return pnlKhung;
    }

    private static JTextField textField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(160, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(145, 162, 180)),
                new EmptyBorder(0, 8, 0, 8)));
        return field;
    }

    private static JComboBox<String> combo(String[] options) {
        JComboBox<String> field = new JComboBox<>(options);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(160, 34));
        field.setBackground(Color.WHITE);
        return field;
    }

    private static JButton nut(String ten, Color nen) {
        JButton button = new JButton(ten);
        button.setBackground(nen);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 34));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static void themTruong(JPanel panel, GridBagConstraints gbc,
            String label, JComponent component, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.31;
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(fieldLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.69;
        panel.add(component, gbc);
    }

    private void lamMoiDanhSach() {
        dsTatCa.clear();
        dsTatCa.addAll(control.layDanhSach());
        locDuLieu();
    }

    private void locDuLieu() {
        if (tableModel == null)
            return;
        String gt = String.valueOf(cmbLocGioiTinh.getSelectedItem());
        String loai = String.valueOf(cmbLocLoai.getSelectedItem());
        dsHienThi.clear();
        tableModel.setRowCount(0);
        dsHienThi.addAll(control.loc(dsTatCa, txtTimKiem.getText(), gt, loai));
        for (HoSoThuCung hs : dsHienThi) {
            ThuCung tc = hs.thuCung;
            tableModel.addRow(new Object[] {
                    tc.getMaThuCung(), tc.getTenThuCung(), tc.getLoai(),
                    tc.getGiong(), tc.getGioiTinh(), String.format(Locale.US, "%.2f kg", tc.getCanNang()),
                    hs.tenKhachHang == null ? "" : hs.tenKhachHang,
                    hs.soDienThoai == null ? "" : hs.soDienThoai,
                    hs.coLichHen ? "Có lịch hẹn" : "Chưa có"
            });
        }
    }

    private void ganChuNuoi(ChuNuoi kh) {
        dangNapChu = true;
        try {
            txtTenKhachHang.setText(kh == null ? "" : kh.hoTen);
        } finally {
            dangNapChu = false;
        }
        chuDaChon = kh;
        txtMaKH.setText(kh == null ? "" : kh.maKhachHang);
        lblChiTietChuNuoi.setText(kh == null ? "Chưa xác định chủ nuôi"
                : "<html>" + html(kh.hoTen) + "<br>Mã: " + html(kh.maKhachHang)
                        + " &nbsp;|&nbsp; SĐT: " + html(kh.soDienThoai) + "</html>");
    }

    private static String html(String s) {
        return s == null ? ""
                : s.replace("&", "&amp;")
                        .replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private ChuNuoi chonTrongDanhSach(List<ChuNuoi> ds, String title) {
        DefaultListModel<ChuNuoi> model = new DefaultListModel<>();
        for (ChuNuoi c : ds)
            model.addElement(c);
        JList<ChuNuoi> lstDanhSach = new JList<>(model);
        lstDanhSach.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstDanhSach.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (!ds.isEmpty())
            lstDanhSach.setSelectedIndex(0);
        JScrollPane scrCuon2 = new JScrollPane(lstDanhSach);
        scrCuon2.setPreferredSize(new Dimension(485, 230));
        int ret = JOptionPane.showConfirmDialog(this, scrCuon2, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        return ret == JOptionPane.OK_OPTION ? lstDanhSach.getSelectedValue() : null;
    }

    private void chonKhachHang() {
        ArrayList<ChuNuoi> khop = control.timChuNuoi(txtTenKhachHang.getText());
        if (khop.isEmpty()) {
            canhBao("Không tìm thấy khách hàng. Vui lòng thêm khách hàng trước khi đăng ký thú cưng.");
            return;
        }
        ChuNuoi c = chonTrongDanhSach(khop, "Chọn đúng khách hàng (mã / họ tên / số điện thoại)");
        if (c != null)
            ganChuNuoi(c);
    }

    private ChuNuoi xacDinhChuNuoi() {
        if (chuDaChon != null && chuDaChon.maKhachHang.equals(txtMaKH.getText())) {
            return chuDaChon;
        }
        if (txtTenKhachHang.getText().isBlank()) {
            canhBao("Vui lòng nhập hoặc chọn tên khách hàng.");
            return null;
        }
        ArrayList<ChuNuoi> khop = control.timChuNuoiTheoTen(txtTenKhachHang.getText());
        if (khop.size() == 1) {
            ganChuNuoi(khop.get(0));
            return khop.get(0);
        }
        if (khop.size() > 1) {
            ChuNuoi c = chonTrongDanhSach(khop,
                    "Có nhiều khách trùng tên, vui lòng chọn theo mã / số điện thoại");
            if (c != null)
                ganChuNuoi(c);
            return c;
        }
        canhBao("Chưa tìm được khách hàng có tên này. Bạn có thể nhập không dấu/không đúng hoa thường, "
                + "nhưng khách hàng phải tồn tại trong hệ thống. Nhấn Chọn để tìm theo tên gần đúng.");
        return null;
    }

    private void resetForm() {
        hoSoDangSua = null;
        txtMaTC.setText(control.goiYMa());
        txtTenTC.setText("");
        txtGiong.setText("");
        txtCanNang.setText("");
        cmbLoai2.setSelectedIndex(0);
        cmbGioiTinh2.setSelectedIndex(0);
        txtTinhTrangLich.setText("Chưa có");
        ganChuNuoi(null);
        lblFormTitle.setText("Thêm hồ sơ thú cưng");
        btnLuu.setText("Thêm hồ sơ");
        btnLuu.setBackground(GREEN);
        btnXoaHoSo.setVisible(false);
        btnBoChon.setEnabled(true);
    }

    private void hienThongTinChiTiet(HoSoThuCung hs) {
        ThuCung tc = hs.thuCung;
        hoSoDangSua = hs;
        txtMaTC.setText(tc.getMaThuCung());
        txtTenTC.setText(tc.getTenThuCung() == null ? "" : tc.getTenThuCung());
        cmbLoai2.setSelectedItem(tc.getLoai());
        txtGiong.setText(tc.getGiong() == null ? "" : tc.getGiong());
        cmbGioiTinh2.setSelectedItem(tc.getGioiTinh());
        txtCanNang.setText(tc.getCanNang() <= 0 ? "" : String.valueOf(tc.getCanNang()));
        txtTinhTrangLich.setText(hs.coLichHen ? "Có lịch hẹn" : "Chưa có");
        ganChuNuoi(new ChuNuoi(tc.getMaKhachHang(), hs.tenKhachHang, hs.soDienThoai));
        lblFormTitle.setText("Thông tin chi tiết thú cưng");
        btnLuu.setText("Lưu thay đổi");
        btnLuu.setBackground(NAVY);
        btnXoaHoSo.setVisible(true);
        btnBoChon.setEnabled(true);
    }

    private void moDialogHoSo(HoSoThuCung hoSo) {
        Window winOwner = SwingUtilities.getWindowAncestor(this);
        winHoSo = new JDialog(winOwner,
                hoSo == null ? "Thêm hồ sơ thú cưng"
                        : "Chi tiết / Sửa thú cưng - "
                                + hoSo.thuCung.getMaThuCung(),
                Dialog.ModalityType.APPLICATION_MODAL);
        winHoSo.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        winHoSo.setContentPane(createFormPanel());
        if (hoSo == null)
            resetForm();
        else
            hienThongTinChiTiet(hoSo);
        winHoSo.setSize(740, 690);
        winHoSo.setMinimumSize(new Dimension(610, 560));
        winHoSo.setLocationRelativeTo(this);
        winHoSo.getRootPane().setDefaultButton(btnLuu);
        winHoSo.setVisible(true);
        winHoSo = null;
    }

    private void canhBao(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Dữ liệu chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    private void them() {
        ThuCung thuCung = layThuCungTuForm();
        if (thuCung == null)
            return;
        if (control.them(thuCung)) {
            thongBao("Thêm hồ sơ thú cưng thành công.");
            capNhatSauLuu();
        } else {
            thongBaoLoi("Không thể thêm hồ sơ thú cưng.");
        }
    }

    private void sua() {
        if (hoSoDangSua == null) {
            canhBao("Vui lòng chọn thú cưng cần sửa.");
            return;
        }

        ThuCung thuCung = layThuCungTuForm();
        if (thuCung == null)
            return;
        if (control.sua(thuCung)) {
            thongBao("Cập nhật hồ sơ thú cưng thành công.");
            capNhatSauLuu();
        } else {
            thongBaoLoi("Không thể cập nhật hồ sơ thú cưng.");
        }
    }

    private boolean xacNhanDoiChuNuoi(ChuNuoi chuNuoiMoi) {
        if (hoSoDangSua == null
                || !control.doiChuNuoi(hoSoDangSua.thuCung.getMaKhachHang(), chuNuoiMoi.maKhachHang))
            return true;

        if (control.khongTheDoiChuNuoi(txtMaTC.getText(),
                hoSoDangSua.thuCung.getMaKhachHang(), chuNuoiMoi.maKhachHang)) {
            canhBao("Thú cưng đã có lịch hẹn, phiếu dịch vụ hoặc hóa đơn. "
                    + "Không thể đổi chủ trực tiếp để bảo toàn lịch sử.");
            return false;
        }

        return JOptionPane.showConfirmDialog(this,
                "Thú cưng chưa có lịch sử. Bạn có muốn đổi chủ nuôi?",
                "Xác nhận đổi chủ", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private ThuCung layThuCungTuForm() {
        ChuNuoi chuNuoi = xacDinhChuNuoi();
        if (chuNuoi == null || !xacNhanDoiChuNuoi(chuNuoi))
            return null;
        try {
            return taoThuCung(chuNuoi);
        } catch (IllegalArgumentException ex) {
            canhBao(ex.getMessage());
            return null;
        }
    }

    private ThuCung taoThuCung(ChuNuoi chuNuoi) {
        String trangThai = hoSoDangSua == null
                ? "Đang theo dõi"
                : hoSoDangSua.thuCung.getTrangThai();
        return control.taoThuCung(txtMaTC.getText(), txtTenTC.getText(),
                String.valueOf(cmbLoai2.getSelectedItem()), txtGiong.getText(),
                String.valueOf(cmbGioiTinh2.getSelectedItem()), txtCanNang.getText(),
                chuNuoi.maKhachHang, trangThai);
    }

    private void capNhatSauLuu() {
        lamMoiDanhSach();
        if (winHoSo != null)
            winHoSo.dispose();
    }

    private void thongBao(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void thongBaoLoi(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void xoa() {
        if (hoSoDangSua == null) {
            canhBao("Vui lòng chọn thú cưng cần xóa.");
            return;
        }

        String maThuCung = hoSoDangSua.thuCung.getMaThuCung();
        int ret = JOptionPane.showConfirmDialog(this,
                "Xóa thú cưng " + maThuCung + "?\nHồ sơ đã có lịch hẹn, phiếu dịch vụ "
                        + "hoặc hóa đơn sẽ không thể xóa để giữ lịch sử.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ret != JOptionPane.YES_OPTION)
            return;
        if (control.xoa(maThuCung)) {
            JOptionPane.showMessageDialog(this, "Đã xóa hồ sơ thú cưng.");
            capNhatSauLuu();
        } else {
            canhBao("Không thể xóa hồ sơ có lịch hẹn, phiếu dịch vụ hoặc hóa đơn. "
                    + "Hãy giữ hồ sơ để bảo toàn lịch sử nghiệp vụ.");
        }
    }

}
