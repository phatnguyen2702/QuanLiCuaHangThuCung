package UI;

import dao.ThuCungDAO;
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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Quản lý hồ sơ thú cưng: tên thú cưng không bắt buộc, liên kết chủ nuôi đúng
 * mã,
 * và hiển thị đầy đủ chi tiết khi người dùng chọn một dòng trong bảng.
 */
public class QuanLyThuCung extends JPanel {
    private static final Color NAVY = new Color(18, 48, 82);
    private static final Color GREEN = new Color(72, 196, 62);
    private static final Color BLUE = new Color(91, 180, 235);
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);

    private final ThuCungDAO dao = new ThuCungDAO();
    private final ArrayList<HoSoThuCung> dsTatCa = new ArrayList<>();
    private final ArrayList<HoSoThuCung> dsHienThi = new ArrayList<>();

    private JTextField txtTimKiem, txtMaTC, txtTenTC, txtCanNang, txtGiong;
    private JTextField txtTenKhachHang, txtMaKH;
    private JComboBox<String> cboLocGioiTinh, cboLocLoai;
    private JComboBox<String> cboLoai, cboGioiTinh;
    private JTextField txtTinhTrangLich;
    private JLabel lblFormTitle, lblChiTietChuNuoi;
    private JButton btnLuu, btnXoaHoSo, btnBoChon;
    private JDialog dlgHoSo;
    private HoSoThuCung hoSoDangSua;
    private JTable table;
    private DefaultTableModel tableModel;
    private ChuNuoi chuDaChon;
    private boolean dangNapChu = false;
    private boolean isEditMode = false;

    public QuanLyThuCung() {
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
        add(createLeftPanel(), BorderLayout.CENTER);
        lamMoiDanhSach();
        // Khi quay lại từ Lịch hẹn Spa, tự tải lại trạng thái Có lịch hẹn/Chưa có.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) {
                lamMoiDanhSach();
            }
        });
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        pnlTim.setOpaque(false);
        txtTimKiem = new JTextField(13);
        txtTimKiem.setPreferredSize(new Dimension(170, 34));
        txtTimKiem.setToolTipText("Tìm mã/tên/giống thú cưng hoặc tên/SĐT chủ nuôi, không phân biệt dấu");
        cboLocGioiTinh = new JComboBox<>(new String[] { "Giới tính", "Đực", "Cái" });
        cboLocLoai = new JComboBox<>(new String[] { "Loại", "Chó", "Mèo", "Chim", "Khác" });
        cboLocGioiTinh.setPreferredSize(new Dimension(92, 34));
        cboLocLoai.setPreferredSize(new Dimension(90, 34));
        JButton btnTim = nut("Tìm kiếm", GREEN);
        btnTim.addActionListener(e -> locDuLieu());
        txtTimKiem.addActionListener(e -> locDuLieu());
        cboLocGioiTinh.addActionListener(e -> locDuLieu());
        cboLocLoai.addActionListener(e -> locDuLieu());
        pnlTim.add(txtTimKiem);
        pnlTim.add(cboLocGioiTinh);
        pnlTim.add(cboLocLoai);
        pnlTim.add(btnTim);
        panel.add(pnlTim, BorderLayout.NORTH);

        String[] cot = { "Mã TC", "Tên thú cưng", "Loài", "Giống",
                "Giới tính", "Cân nặng", "Chủ nuôi", "Số điện thoại", "Lịch hẹn" };
        tableModel = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(39);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(100, 38));
        int[] rongCot = { 80, 145, 75, 140, 85, 96, 180, 140, 140 };
        for (int i = 0; i < rongCot.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(rongCot[i]);
        }
        DefaultTableCellRenderer canGiua = new DefaultTableCellRenderer();
        canGiua.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[] { 0, 2, 4, 5, 8 }) {
            table.getColumnModel().getColumn(i).setCellRenderer(canGiua);
        }
        // Không còn cột Thao tác: toàn bộ nút Lưu / Xóa / Đóng nằm trong modal.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setToolTipText("Nhấp một lần vào dòng để xem chi tiết, sửa hoặc xóa hồ sơ.");
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e))
                    return;
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0)
                    return;
                int i = table.convertRowIndexToModel(row);
                if (i >= 0 && i < dsHienThi.size()) {
                    moDialogHoSo(dsHienThi.get(i));
                }
            }
        });
        JScrollPane scrBang = new JScrollPane(table);
        scrBang.getViewport().setBackground(Color.WHITE);
        panel.add(scrBang, BorderLayout.CENTER);

        JPanel pnlDuoi = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlDuoi.setOpaque(false);
        JButton btnLamMoi = nut("↻ Làm mới", BLUE);
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            cboLocGioiTinh.setSelectedIndex(0);
            cboLocLoai.setSelectedIndex(0);
            lamMoiDanhSach();
        });
        pnlDuoi.add(btnLamMoi);
        panel.add(pnlDuoi, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(670, 610));
        panel.setBorder(new EmptyBorder(13, 14, 12, 14));

        lblFormTitle = new JLabel("Thêm hồ sơ thú cưng", SwingConstants.CENTER);
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblFormTitle.setBorder(new EmptyBorder(3, 0, 6, 0));
        panel.add(lblFormTitle, BorderLayout.NORTH);

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

        cboLoai = combo(new String[] { "Chó", "Mèo", "Chim", "Khác" });
        themTruong(pnlForm, gbc, "Loại", cboLoai, row++);
        txtGiong = textField();
        themTruong(pnlForm, gbc, "Giống", txtGiong, row++);
        cboGioiTinh = combo(new String[] { "Đực", "Cái" });
        themTruong(pnlForm, gbc, "Giới tính", cboGioiTinh, row++);
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

        // Nhiều trường: cho phép cuộn để không bị cắt khi cửa sổ nhỏ.
        JScrollPane scrForm = new JScrollPane(pnlForm,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrForm.setBorder(null);
        scrForm.getVerticalScrollBar().setUnitIncrement(14);
        panel.add(scrForm, BorderLayout.CENTER);

        btnLuu = nut("Thêm hồ sơ", GREEN);
        btnLuu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLuu.addActionListener(e -> xuLyLuuDuLieu());
        btnXoaHoSo = nut("Xóa hồ sơ", new Color(210, 60, 76));
        btnXoaHoSo.setPreferredSize(new Dimension(130, 38));
        btnXoaHoSo.addActionListener(e -> {
            if (hoSoDangSua != null) {
                xoaThuCung(hoSoDangSua.thuCung.getMaThuCung());
            }
        });
        btnBoChon = nut("Đóng", new Color(105, 117, 133));
        btnBoChon.addActionListener(e -> {
            if (dlgHoSo != null)
                dlgHoSo.dispose();
        });
        btnLuu.setPreferredSize(new Dimension(150, 38));
        btnBoChon.setPreferredSize(new Dimension(110, 38));
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        pnlNut.setOpaque(false);
        pnlNut.setBorder(new EmptyBorder(6, 2, 0, 2));
        pnlNut.add(btnLuu);
        pnlNut.add(btnXoaHoSo);
        pnlNut.add(btnBoChon);
        panel.add(pnlNut, BorderLayout.SOUTH);
        return panel;
    }

    private static JTextField textField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(160, 34));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(145, 162, 180)),
                new EmptyBorder(0, 8, 0, 8)));
        return f;
    }

    private static JComboBox<String> combo(String[] options) {
        JComboBox<String> c = new JComboBox<>(options);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setPreferredSize(new Dimension(160, 34));
        c.setBackground(Color.WHITE);
        return c;
    }

    private static JButton nut(String ten, Color nen) {
        JButton b = new JButton(ten);
        b.setBackground(nen);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(100, 34));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void themTruong(JPanel panel, GridBagConstraints gbc, String label, JComponent comp, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.31;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.69;
        panel.add(comp, gbc);
    }

    private void lamMoiDanhSach() {
        dsTatCa.clear();
        dsTatCa.addAll(dao.getAllChiTiet());
        locDuLieu();
    }

    private static String boDauVaThuong(String s) {
        if (s == null)
            return "";
        String normal = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
        return normal.replace('đ', 'd').trim().replaceAll("\\s+", " ");
    }

    private void locDuLieu() {
        if (tableModel == null)
            return;
        String q = boDauVaThuong(txtTimKiem.getText());
        String gt = String.valueOf(cboLocGioiTinh.getSelectedItem());
        String loai = String.valueOf(cboLocLoai.getSelectedItem());
        dsHienThi.clear();
        tableModel.setRowCount(0);
        for (HoSoThuCung hs : dsTatCa) {
            ThuCung tc = hs.thuCung;
            String haystack = boDauVaThuong(tc.getMaThuCung() + " " + tc.getTenThuCung()
                    + " " + tc.getLoai() + " " + tc.getGiong() + " "
                    + hs.tenKhachHang + " " + hs.soDienThoai);
            if (!haystack.contains(q))
                continue;
            if (!"Giới tính".equals(gt) && !gt.equals(tc.getGioiTinh()))
                continue;
            if (!"Loại".equals(loai) && !loai.equals(tc.getLoai()))
                continue;
            dsHienThi.add(hs);
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

    /** Chọn chính xác mã khách hàng khi nhiều người trùng tên. */
    private ChuNuoi chonTrongDanhSach(List<ChuNuoi> ds, String title) {
        DefaultListModel<ChuNuoi> model = new DefaultListModel<>();
        for (ChuNuoi c : ds)
            model.addElement(c);
        JList<ChuNuoi> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (!ds.isEmpty())
            list.setSelectedIndex(0);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(485, 230));
        int ret = JOptionPane.showConfirmDialog(this, scroll, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        return ret == JOptionPane.OK_OPTION ? list.getSelectedValue() : null;
    }

    private void chonKhachHang() {
        String tuKhoa = boDauVaThuong(txtTenKhachHang.getText());
        ArrayList<ChuNuoi> khop = new ArrayList<>();
        for (ChuNuoi c : dao.getDanhSachChuNuoi()) {
            if (tuKhoa.isEmpty() || boDauVaThuong(c.hoTen).contains(tuKhoa)
                    || boDauVaThuong(c.maKhachHang).contains(tuKhoa)
                    || boDauVaThuong(c.soDienThoai).contains(tuKhoa)) {
                khop.add(c);
            }
        }
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
        String tenNhap = boDauVaThuong(txtTenKhachHang.getText());
        if (tenNhap.isBlank()) {
            canhBao("Vui lòng nhập hoặc chọn tên khách hàng.");
            return null;
        }
        ArrayList<ChuNuoi> khop = new ArrayList<>();
        for (ChuNuoi c : dao.getDanhSachChuNuoi()) {
            if (boDauVaThuong(c.hoTen).equals(tenNhap))
                khop.add(c);
        }
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
        isEditMode = false;
        hoSoDangSua = null;
        txtMaTC.setText(dao.taoMaThuCungTuDong());
        txtTenTC.setText("");
        txtGiong.setText("");
        txtCanNang.setText("");
        cboLoai.setSelectedIndex(0);
        cboGioiTinh.setSelectedIndex(0);
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
        isEditMode = true;
        txtMaTC.setText(tc.getMaThuCung());
        txtTenTC.setText(tc.getTenThuCung() == null ? "" : tc.getTenThuCung());
        cboLoai.setSelectedItem(tc.getLoai());
        txtGiong.setText(tc.getGiong() == null ? "" : tc.getGiong());
        cboGioiTinh.setSelectedItem(tc.getGioiTinh());
        txtCanNang.setText(tc.getCanNang() <= 0 ? "" : String.valueOf(tc.getCanNang()));
        hoSoDangSua = hs;
        txtTinhTrangLich.setText(hs.coLichHen ? "Có lịch hẹn" : "Chưa có");
        ganChuNuoi(new ChuNuoi(tc.getMaKhachHang(), hs.tenKhachHang, hs.soDienThoai));
        lblFormTitle.setText("Thông tin chi tiết thú cưng");
        btnLuu.setText("Lưu thay đổi");
        btnLuu.setBackground(NAVY);
        btnXoaHoSo.setVisible(true);
        btnBoChon.setEnabled(true);
    }

    /**
     * Form thêm/sửa được hiển thị trong modal, bảng danh sách chiếm hết màn hình.
     */
    private void moDialogHoSo(HoSoThuCung hoSo) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        dlgHoSo = new JDialog(owner,
                hoSo == null ? "Thêm hồ sơ thú cưng"
                        : "Chi tiết / Sửa thú cưng - "
                                + hoSo.thuCung.getMaThuCung(),
                Dialog.ModalityType.APPLICATION_MODAL);
        dlgHoSo.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlgHoSo.setContentPane(createRightPanel());
        if (hoSo == null)
            resetForm();
        else
            hienThongTinChiTiet(hoSo);
        dlgHoSo.setSize(740, 690);
        dlgHoSo.setMinimumSize(new Dimension(610, 560));
        dlgHoSo.setLocationRelativeTo(this);
        dlgHoSo.getRootPane().setDefaultButton(btnLuu);
        dlgHoSo.setVisible(true);
        dlgHoSo = null;
    }

    private void canhBao(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Dữ liệu chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    private void xuLyLuuDuLieu() {
        String loai = String.valueOf(cboLoai.getSelectedItem());
        String giong = txtGiong.getText().trim();
        String ten = txtTenTC.getText().trim();
        if (ten.isEmpty()) {
            ten = loai + " - " + (giong.isEmpty() ? "Chưa rõ giống" : giong);
        }
        if (ten.length() > 50) {
            canhBao("Tên thú cưng không được quá 50 ký tự.");
            return;
        }
        if (giong.length() > 25) {
            canhBao("Giống thú cưng không được quá 25 ký tự.");
            return;
        }
        String chuoiCanNang = txtCanNang.getText().trim().replace(',', '.');
        double canNang;
        try {
            canNang = Double.parseDouble(chuoiCanNang);
        } catch (NumberFormatException ex) {
            canhBao("Vui lòng nhập cân nặng là số, ví dụ 3.5 (kg).");
            return;
        }
        if (!Double.isFinite(canNang) || canNang <= 0 || canNang > 999.99) {
            canhBao("Cân nặng phải lớn hơn 0 và không quá 999,99 kg.");
            return;
        }
        ChuNuoi c = xacDinhChuNuoi();
        if (c == null)
            return;

        // Không chuyển chủ nuôi khi đã có lịch sử nghiệp vụ gắn với thú cưng.
        if (isEditMode) {
            for (HoSoThuCung cu : dsTatCa) {
                if (cu.thuCung.getMaThuCung().equals(txtMaTC.getText())
                        && !cu.thuCung.getMaKhachHang().equals(c.maKhachHang)) {
                    if (dao.daCoLichSu(txtMaTC.getText())) {
                        canhBao("Thú cưng đã có lịch hẹn, phiếu dịch vụ hoặc hóa đơn. "
                                + "Không thể đổi chủ trực tiếp vì sẽ sai lịch sử. "
                                + "Vui lòng giữ chủ cũ và lập hồ sơ chuyển chủ riêng nếu cần.");
                        return;
                    }
                    int xacNhan = JOptionPane.showConfirmDialog(this,
                            "Bạn đang đổi chủ nuôi của thú cưng chưa có lịch sử. Tiếp tục?",
                            "Xác nhận đổi chủ", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (xacNhan != JOptionPane.YES_OPTION)
                        return;
                }
            }
        }
        // Giữ nguyên trạng thái hồ sơ cũ; "Có lịch hẹn" được tính từ LichHen,
        // không ghi đè vào ThuCung.trangThai.
        String trangThaiHoSo = isEditMode && hoSoDangSua != null
                ? hoSoDangSua.thuCung.getTrangThai()
                : "Đang theo dõi";
        ThuCung tc = new ThuCung(txtMaTC.getText(), ten, loai, giong,
                String.valueOf(cboGioiTinh.getSelectedItem()), canNang,
                c.maKhachHang, trangThaiHoSo);
        boolean thanhCong = isEditMode ? dao.sua(tc) : dao.them(tc);
        if (!thanhCong) {
            JOptionPane.showMessageDialog(this,
                    "Không lưu được hồ sơ. Hãy kiểm tra mã khách hàng, kết nối SQL Server và thông báo Console.",
                    "Lưu thất bại", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
                isEditMode ? "Đã cập nhật hồ sơ thú cưng." : "Đã thêm hồ sơ thú cưng.");
        lamMoiDanhSach();
        if (dlgHoSo != null)
            dlgHoSo.dispose();
    }

    private void xoaThuCung(String maTC) {
        int ret = JOptionPane.showConfirmDialog(this,
                "Xóa thú cưng " + maTC + "?\nHồ sơ đã có lịch hẹn, phiếu dịch vụ "
                        + "hoặc hóa đơn sẽ không thể xóa để giữ lịch sử.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ret != JOptionPane.YES_OPTION)
            return;
        if (dao.xoa(maTC)) {
            JOptionPane.showMessageDialog(this, "Đã xóa hồ sơ thú cưng.");
            lamMoiDanhSach();
            if (dlgHoSo != null)
                dlgHoSo.dispose();
        } else {
            canhBao("Không thể xóa hồ sơ có lịch hẹn, phiếu dịch vụ hoặc hóa đơn. "
                    + "Hãy giữ hồ sơ để bảo toàn lịch sử nghiệp vụ.");
        }
    }

}
