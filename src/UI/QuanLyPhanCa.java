package UI;

import dao.LichPhanCaDAO;
import entity.LichPhanCa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class QuanLyPhanCa extends JPanel {

    // =========================================================
    // MAU
    // =========================================================

    private final Color NAVY = new Color(18, 48, 82);
    private final Color LIGHT_BLUE = new Color(229, 241, 252);
    private final Color BG = new Color(245, 247, 250);
    private final Color GREEN = new Color(72, 196, 62);
    private final Color RED = new Color(210, 70, 70);
    private final Color BLUE = new Color(91, 180, 235);
    // =========================================================
    // DAO
    // =========================================================

    private LichPhanCaDAO dao;

    // =========================================================
    // FORM
    // =========================================================

    private JTextField txtMaPhanCa;
    private JTextField txtNgayLamViec;

    private JComboBox<String> cboNhanVien;
    private JComboBox<String> cboCa;

    private JTextField txtHoTen;
    private JTextField txtThongTinCa;

    // =========================================================
    // LOC
    // =========================================================

    private JTextField txtLocNgay;
    private JComboBox<String> cboLocNhanVien;
    private JComboBox<String> cboLocCa;

    // =========================================================
    // TABLE
    // =========================================================

    private JTable table;
    private DefaultTableModel tableModel;

    // =========================================================
    // FORMAT DATE
    // =========================================================

    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public QuanLyPhanCa() {

        dao = new LichPhanCaDAO();

        setLayout(new BorderLayout());

        setBackground(BG);

        add(createTitle(), BorderLayout.NORTH);

        add(createCenter(), BorderLayout.CENTER);

        loadComboBox();

        loadData();

        lamMoiForm();
    }

    // =========================================================
    // TITLE
    // =========================================================

    private JPanel createTitle() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(BG);

        panel.setBorder(
                new EmptyBorder(
                        15, 25, 10, 25));

        JLabel lblTitle = new JLabel("Quản lý phân ca");

        lblTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        24));

        lblTitle.setForeground(Color.BLACK);

        panel.add(
                lblTitle,
                BorderLayout.WEST);

        return panel;
    }

    // =========================================================
    // CENTER
    // =========================================================

    private JPanel createCenter() {

        JPanel main = new JPanel(new BorderLayout(15, 15));

        main.setBackground(LIGHT_BLUE);

        main.setBorder(
                new EmptyBorder(
                        10, 20, 15, 20));

        main.add(
                createLeftPanel(),
                BorderLayout.CENTER);

        main.add(
                createRightPanel(),
                BorderLayout.EAST);

        return main;
    }

    // =========================================================
    // LEFT
    // =========================================================

    private JPanel createLeftPanel() {

        JPanel panel = new JPanel(new BorderLayout(8, 8));

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                new EmptyBorder(
                        10, 10, 10, 10));

        panel.add(
                createFilterPanel(),
                BorderLayout.NORTH);

        panel.add(
                createTablePanel(),
                BorderLayout.CENTER);

        panel.add(
                createBottomPanel(),
                BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================
    // FILTER
    // =========================================================

    private JPanel createFilterPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        8,
                        5));

        panel.setBackground(Color.WHITE);

        txtLocNgay = new JTextField(9);

        txtLocNgay.setToolTipText(
                "dd/MM/yyyy");

        cboLocNhanVien = new JComboBox<>();

        cboLocNhanVien.setPreferredSize(
                new Dimension(120, 30));

        cboLocCa = new JComboBox<>();

        cboLocCa.setPreferredSize(
                new Dimension(110, 30));

        JButton btnLoc = new JButton("Lọc");

        btnLoc.setBackground(GREEN);

        btnLoc.setForeground(Color.WHITE);

        btnLoc.setFocusPainted(false);

        btnLoc.addActionListener(
                e -> locDuLieu());

        panel.add(
                taoLabel("Ngày làm việc"));

        panel.add(txtLocNgay);

        panel.add(
                taoLabel("Nhân viên"));

        panel.add(cboLocNhanVien);

        panel.add(
                taoLabel("Ca"));

        panel.add(cboLocCa);

        panel.add(btnLoc);

        return panel;
    }

    // =========================================================
    // TABLE
    // =========================================================

    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(Color.WHITE);

        String[] columns = {
                "Mã PC",
                "Ngày làm việc",
                "Mã NV",
                "Họ tên",
                "Mã ca",
                "Ca làm"
        };

        tableModel = new DefaultTableModel(
                columns,
                0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        table = new JTable(tableModel);

        table.setRowHeight(32);

        table.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12));

        table.getTableHeader().setBackground(
                NAVY);

        table.getTableHeader().setForeground(
                Color.WHITE);

        table.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12));

        // Can giua
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();

        center.setHorizontalAlignment(
                SwingConstants.CENTER);

        table.getColumnModel()
                .getColumn(0)
                .setCellRenderer(center);

        table.getColumnModel()
                .getColumn(1)
                .setCellRenderer(center);

        table.getColumnModel()
                .getColumn(2)
                .setCellRenderer(center);

        table.getColumnModel()
                .getColumn(4)
                .setCellRenderer(center);

        // Do rong cot
        table.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(65);

        table.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(100);

        table.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(70);

        table.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(130);

        table.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(65);

        table.getColumnModel()
                .getColumn(5)
                .setPreferredWidth(150);

        // Click dong
        table.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent e) {

                        int row = table.getSelectedRow();

                        if (row >= 0) {
                            hienThiForm(row);
                        }
                    }
                });

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createLineBorder(
                        new Color(220, 220, 220)));

        panel.add(
                scroll,
                BorderLayout.CENTER);

        return panel;
    }

    // =========================================================
    // BOTTOM
    // =========================================================

    private JPanel createBottomPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(Color.WHITE);

        JButton btnXoa = new JButton("Xóa phân ca");

        btnXoa.setBackground(RED);

        btnXoa.setForeground(Color.WHITE);

        btnXoa.setFocusPainted(false);

        btnXoa.addActionListener(
                e -> xoa());

        JButton btnLamMoi = new JButton("↻  Làm mới");

        btnLamMoi.setBackground(BLUE);

        btnLamMoi.setForeground(Color.WHITE);

        btnLamMoi.setFocusPainted(false);

        btnLamMoi.addActionListener(
                e -> {
                    loadComboBox();
                    loadData();
                    lamMoiForm();
                });

        panel.add(
                btnXoa,
                BorderLayout.WEST);

        panel.add(
                btnLamMoi,
                BorderLayout.EAST);

        return panel;
    }

    // =========================================================
    // RIGHT
    // =========================================================

    private JPanel createRightPanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(LIGHT_BLUE);

        // Chiều rộng cố định cho khu vực bên phải
        panel.setPreferredSize(new Dimension(300, 0));

        // Khung tạo lịch làm
        panel.add(
                createFormPanel(),
                BorderLayout.NORTH);

        // Khung danh sách ca
        panel.add(
                createInfoPanel(),
                BorderLayout.CENTER);

        return panel;
    }
    // =========================================================
    // FORM
    // =========================================================

    private JPanel createFormPanel() {

        JPanel panel = new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS));

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                new EmptyBorder(
                        12, 15, 15, 15));

        JLabel title = new JLabel("Tạo lịch làm");

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        20));

        title.setAlignmentX(
                Component.CENTER_ALIGNMENT);

        panel.add(title);

        panel.add(
                Box.createVerticalStrut(15));

        // Ma phan ca
        panel.add(
                taoLabel("Mã phân ca"));

        txtMaPhanCa = new JTextField();

        txtMaPhanCa.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        32));

        txtMaPhanCa.setEditable(false);

        panel.add(txtMaPhanCa);

        panel.add(
                Box.createVerticalStrut(8));

        // Ngay
        panel.add(
                taoLabel("Ngày làm việc"));

        txtNgayLamViec = new JTextField();

        txtNgayLamViec.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        32));

        txtNgayLamViec.setToolTipText(
                "dd/MM/yyyy");

        panel.add(txtNgayLamViec);

        panel.add(
                Box.createVerticalStrut(8));

        // Nhan vien
        panel.add(
                taoLabel("Mã nhân viên"));

        cboNhanVien = new JComboBox<>();

        cboNhanVien.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        32));

        cboNhanVien.addActionListener(
                e -> capNhatHoTen());

        panel.add(cboNhanVien);

        panel.add(
                Box.createVerticalStrut(8));

        // Ho ten
        panel.add(
                taoLabel("Họ tên"));

        txtHoTen = new JTextField();

        txtHoTen.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        32));

        txtHoTen.setEditable(false);

        panel.add(txtHoTen);

        panel.add(
                Box.createVerticalStrut(8));

        // Ma ca
        panel.add(
                taoLabel("Mã ca"));

        cboCa = new JComboBox<>();

        cboCa.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        32));

        cboCa.addActionListener(
                e -> capNhatThongTinCa());

        panel.add(cboCa);

        panel.add(
                Box.createVerticalStrut(8));

        // Thong tin ca
        panel.add(
                taoLabel("Thông tin ca"));

        txtThongTinCa = new JTextField();

        txtThongTinCa.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        32));

        txtThongTinCa.setEditable(false);

        panel.add(txtThongTinCa);

        panel.add(
                Box.createVerticalStrut(12));

        JButton btnThem = new JButton("Thêm +");

        btnThem.setBackground(GREEN);

        btnThem.setForeground(Color.WHITE);

        btnThem.setFocusPainted(false);

        btnThem.setAlignmentX(
                Component.CENTER_ALIGNMENT);

        btnThem.addActionListener(
                e -> them());

        panel.add(btnThem);

        return panel;
    }

    // =========================================================
    // INFO
    // =========================================================

    // =========================================================
    // INFO - DANH SACH CA
    // =========================================================
    private JPanel createInfoPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        panel.setBorder(
                new EmptyBorder(
                        15, 18, 15, 18));

        // Tieu de
        JLabel title = new JLabel("Danh sách ca");
        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        15));

        panel.add(
                title,
                BorderLayout.NORTH);

        // Noi dung danh sach
        JPanel danhSach = new JPanel();
        danhSach.setLayout(
                new BoxLayout(
                        danhSach,
                        BoxLayout.Y_AXIS));

        danhSach.setBackground(Color.WHITE);

        ArrayList<String[]> dsCa = dao.getDanhSachCa();

        for (String[] ca : dsCa) {

            JLabel lbl = taoLabelCa(
                    ca[0],
                    ca[1],
                    ca[2],
                    ca[3]);

            danhSach.add(lbl);

            danhSach.add(
                    Box.createVerticalStrut(12));
        }

        panel.add(
                danhSach,
                BorderLayout.CENTER);

        return panel;
    }

    // =========================================================
    // LABEL THONG TIN CA
    // =========================================================
    private JLabel taoLabelCa(
            String maCa,
            String tenCa,
            String gioBatDau,
            String gioKetThuc) {

        String html = "<html>"
                + "<div style='width:235px;'>"
                + "<b>" + maCa + "</b>"
                + " - " + tenCa
                + "<br>"
                + "<span style='padding-left:15px;'>"
                + gioBatDau
                + " - "
                + gioKetThuc
                + "</span>"
                + "</div>"
                + "</html>";

        JLabel label = new JLabel(html);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12));

        label.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        return label;
    }
    // =========================================================
    // LOAD COMBOBOX
    // =========================================================

    private void loadComboBox() {

        if (cboNhanVien == null ||
                cboCa == null) {
            return;
        }

        // Nhan vien
        cboNhanVien.removeAllItems();

        ArrayList<String[]> dsNV = dao.getDanhSachNhanVien();

        for (String[] nv : dsNV) {

            cboNhanVien.addItem(
                    nv[0] + " - " + nv[1]);
        }

        // Loc nhan vien
        if (cboLocNhanVien != null) {

            cboLocNhanVien.removeAllItems();

            cboLocNhanVien.addItem(
                    "Tất cả");

            for (String[] nv : dsNV) {

                cboLocNhanVien.addItem(
                        nv[0] + " - " + nv[1]);
            }
        }

        // Ca
        cboCa.removeAllItems();

        ArrayList<String[]> dsCa = dao.getDanhSachCa();

        for (String[] ca : dsCa) {

            String text = ca[0] + " - "
                    + ca[1] + ": "
                    + ca[2] + "-"
                    + ca[3];

            cboCa.addItem(text);
        }

        // Loc ca
        if (cboLocCa != null) {

            cboLocCa.removeAllItems();

            cboLocCa.addItem(
                    "Tất cả");

            for (String[] ca : dsCa) {

                cboLocCa.addItem(
                        ca[0] + " - "
                                + ca[1]);
            }
        }
    }

    // =========================================================
    // LOAD DATA
    // =========================================================

    private void loadData() {

        ArrayList<LichPhanCa> list = dao.getAll();

        hienThiDuLieu(list);
    }

    // =========================================================
    // HIEN THI
    // =========================================================

    private void hienThiDuLieu(
            ArrayList<LichPhanCa> list) {

        tableModel.setRowCount(0);

        for (LichPhanCa pc : list) {

            String ngay = "";

            if (pc.getNgayLamViec() != null) {

                ngay = pc.getNgayLamViec()
                        .format(DATE_FORMAT);
            }

            String ca = pc.getMaCa();

            if (pc.getTenCa() != null) {

                ca += " - "
                        + pc.getTenCa()
                        + " "
                        + pc.getGioBatDau()
                        + "-"
                        + pc.getGioKetThuc();
            }

            tableModel.addRow(
                    new Object[] {
                            pc.getMaPhanCa(),
                            ngay,
                            pc.getMaNhanVien(),
                            pc.getHoTen(),
                            pc.getMaCa(),
                            ca
                    });
        }
    }

    // =========================================================
    // THEM
    // =========================================================

    private void them() {

        try {

            if (cboNhanVien.getSelectedItem() == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Vui lòng chọn nhân viên!");

                return;
            }

            if (cboCa.getSelectedItem() == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Vui lòng chọn ca!");

                return;
            }

            LocalDate ngay = LocalDate.parse(
                    txtNgayLamViec
                            .getText()
                            .trim(),
                    DATE_FORMAT);

            String maNV = layMaNhanVien();

            String maCa = layMaCa();

            if (dao.daPhanCa(
                    maNV,
                    maCa,
                    java.sql.Date.valueOf(ngay))) {

                JOptionPane.showMessageDialog(
                        this,
                        "Nhân viên đã được phân ca này trong ngày!");

                return;
            }

            String maPhanCa = dao.taoMaPhanCa();

            LichPhanCa pc = new LichPhanCa(
                    maPhanCa,
                    ngay,
                    maNV,
                    maCa);

            boolean result = dao.them(pc);

            if (result) {

                JOptionPane.showMessageDialog(
                        this,
                        "Thêm lịch làm thành công!");

                loadData();

                lamMoiForm();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Thêm lịch làm thất bại!",
                        "Thông báo",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ngày phải có dạng dd/MM/yyyy!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Có lỗi: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================
    // XOA
    // =========================================================

    private void xoa() {

        int row = table.getSelectedRow();

        if (row < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn phân ca cần xóa!");

            return;
        }

        String maPhanCa = tableModel
                .getValueAt(row, 0)
                .toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa "
                        + maPhanCa
                        + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (dao.xoa(maPhanCa)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Xóa phân ca thành công!");

            loadData();

            lamMoiForm();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Xóa phân ca thất bại!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================
    // LOC
    // =========================================================

    private void locDuLieu() {

        ArrayList<LichPhanCa> list = dao.getAll();

        String ngay = txtLocNgay
                .getText()
                .trim();

        String nv = "";

        if (cboLocNhanVien.getSelectedItem() != null) {

            String value = cboLocNhanVien
                    .getSelectedItem()
                    .toString();

            if (!value.equals("Tất cả")) {

                nv = value.substring(
                        0,
                        value.indexOf(" - "));
            }
        }

        String ca = "";

        if (cboLocCa.getSelectedItem() != null) {

            String value = cboLocCa
                    .getSelectedItem()
                    .toString();

            if (!value.equals("Tất cả")) {

                ca = value.substring(
                        0,
                        value.indexOf(" - "));
            }
        }

        LocalDate ngayLoc = null;

        if (!ngay.isEmpty()) {

            try {

                ngayLoc = LocalDate.parse(
                        ngay,
                        DATE_FORMAT);

            } catch (DateTimeParseException e) {

                JOptionPane.showMessageDialog(
                        this,
                        "Ngày lọc phải có dạng dd/MM/yyyy!");

                return;
            }
        }

        ArrayList<LichPhanCa> ketQua = new ArrayList<>();

        for (LichPhanCa pc : list) {

            boolean dung = true;

            if (ngayLoc != null) {

                if (pc.getNgayLamViec() == null
                        || !pc.getNgayLamViec()
                                .equals(ngayLoc)) {

                    dung = false;
                }
            }

            if (!nv.isEmpty()) {

                if (!pc.getMaNhanVien()
                        .equalsIgnoreCase(nv)) {

                    dung = false;
                }
            }

            if (!ca.isEmpty()) {

                if (!pc.getMaCa()
                        .equalsIgnoreCase(ca)) {

                    dung = false;
                }
            }

            if (dung) {
                ketQua.add(pc);
            }
        }

        hienThiDuLieu(ketQua);
    }

    // =========================================================
    // CLICK TABLE -> FORM
    // =========================================================

    private void hienThiForm(int row) {

        txtMaPhanCa.setText(
                tableModel
                        .getValueAt(row, 0)
                        .toString());

        txtNgayLamViec.setText(
                tableModel
                        .getValueAt(row, 1)
                        .toString());

        String maNV = tableModel
                .getValueAt(row, 2)
                .toString();

        String maCa = tableModel
                .getValueAt(row, 4)
                .toString();

        chonNhanVien(maNV);

        chonCa(maCa);
    }

    // =========================================================
    // CHON NHAN VIEN
    // =========================================================

    private void chonNhanVien(
            String maNV) {

        for (int i = 0; i < cboNhanVien.getItemCount(); i++) {

            String value = cboNhanVien
                    .getItemAt(i);

            if (value.startsWith(
                    maNV + " - ")) {

                cboNhanVien
                        .setSelectedIndex(i);

                break;
            }
        }
    }

    // =========================================================
    // CHON CA
    // =========================================================

    private void chonCa(
            String maCa) {

        for (int i = 0; i < cboCa.getItemCount(); i++) {

            String value = cboCa
                    .getItemAt(i);

            if (value.startsWith(
                    maCa + " - ")) {

                cboCa
                        .setSelectedIndex(i);

                break;
            }
        }
    }

    // =========================================================
    // CAP NHAT HO TEN
    // =========================================================

    private void capNhatHoTen() {

        if (cboNhanVien.getSelectedItem() == null) {
            return;
        }

        String value = cboNhanVien
                .getSelectedItem()
                .toString();

        int index = value.indexOf(" - ");

        if (index >= 0) {

            String hoTen = value.substring(
                    index + 3);

            txtHoTen.setText(hoTen);
        }
    }

    // =========================================================
    // CAP NHAT THONG TIN CA
    // =========================================================

    private void capNhatThongTinCa() {

        if (cboCa.getSelectedItem() == null) {
            return;
        }

        txtThongTinCa.setText(
                cboCa
                        .getSelectedItem()
                        .toString());
    }

    // =========================================================
    // LAY MA NHAN VIEN
    // =========================================================

    private String layMaNhanVien() {

        String value = cboNhanVien
                .getSelectedItem()
                .toString();

        int index = value.indexOf(" - ");

        if (index >= 0) {

            return value.substring(
                    0,
                    index);
        }

        return value;
    }

    // =========================================================
    // LAY MA CA
    // =========================================================

    private String layMaCa() {

        String value = cboCa
                .getSelectedItem()
                .toString();

        int index = value.indexOf(" - ");

        if (index >= 0) {

            return value.substring(
                    0,
                    index);
        }

        return value;
    }

    // =========================================================
    // LAM MOI FORM
    // =========================================================

    private void lamMoiForm() {

        txtMaPhanCa.setText(
                dao.taoMaPhanCa());

        txtNgayLamViec.setText(
                LocalDate.now()
                        .format(DATE_FORMAT));

        if (cboNhanVien.getItemCount() > 0) {

            cboNhanVien.setSelectedIndex(0);
        }

        if (cboCa.getItemCount() > 0) {

            cboCa.setSelectedIndex(0);
        }

        if (txtLocNgay != null) {
            txtLocNgay.setText("");
        }

        if (cboLocNhanVien != null
                && cboLocNhanVien.getItemCount() > 0) {

            cboLocNhanVien.setSelectedIndex(0);
        }

        if (cboLocCa != null
                && cboLocCa.getItemCount() > 0) {

            cboLocCa.setSelectedIndex(0);
        }
    }

    // =========================================================
    // LABEL
    // =========================================================

    private JLabel taoLabel(String text) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12));

        label.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        return label;
    }
}