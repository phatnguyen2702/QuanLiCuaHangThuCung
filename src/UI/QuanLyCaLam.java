package UI;

import dao.CaLamDAO;
import entity.CaLam;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class QuanLyCaLam extends JPanel {

    // ==============================
    // MAU
    // ==============================

    private final Color NAVY = new Color(18, 48, 82);
    private final Color BG = new Color(245, 247, 250);
    private final Color GREEN = new Color(72, 196, 62);
    private final Color RED = new Color(210, 70, 70);
    private final Color ORANGE = new Color(230, 150, 50);

    // ==============================
    // FORM
    // ==============================

    private JTextField txtMaCa;
    private JTextField txtTenCa;
    private JTextField txtGioBatDau;
    private JTextField txtGioKetThuc;

    private JTextField txtTimKiem;

    // ==============================
    // TABLE
    // ==============================

    private JTable table;
    private DefaultTableModel model;

    // ==============================
    // DAO
    // ==============================

    private CaLamDAO caLamDAO;

    // ==============================
    // CONSTRUCTOR
    // ==============================

    public QuanLyCaLam() {

        caLamDAO = new CaLamDAO();

        setLayout(new BorderLayout());

        setBackground(BG);

        add(
                createTitle(),
                BorderLayout.NORTH);

        add(
                createCenter(),
                BorderLayout.CENTER);

        loadData();
    }

    // ==============================
    // TITLE
    // ==============================

    private JPanel createTitle() {

        JPanel panel = new JPanel(
                new BorderLayout());

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                new EmptyBorder(
                        18,
                        25,
                        18,
                        25));

        JLabel title = new JLabel(
                "QUAN LY CA LAM");

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        24));

        title.setForeground(NAVY);

        JLabel sub = new JLabel(
                "Quan ly thong tin ca lam viec");

        sub.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13));

        sub.setForeground(Color.GRAY);

        JPanel text = new JPanel();

        text.setOpaque(false);

        text.setLayout(
                new BoxLayout(
                        text,
                        BoxLayout.Y_AXIS));

        text.add(title);

        text.add(
                Box.createVerticalStrut(5));

        text.add(sub);

        panel.add(
                text,
                BorderLayout.WEST);

        return panel;
    }

    // ==============================
    // CENTER
    // ==============================

    private JPanel createCenter() {

        JPanel panel = new JPanel(
                new BorderLayout(
                        15,
                        15));

        panel.setBackground(BG);

        panel.setBorder(
                new EmptyBorder(
                        20,
                        25,
                        20,
                        25));

        panel.add(
                createForm(),
                BorderLayout.NORTH);

        panel.add(
                createTablePanel(),
                BorderLayout.CENTER);

        return panel;
    }

    // ==============================
    // FORM
    // ==============================

    private JPanel createForm() {

        JPanel panel = new JPanel(
                new GridBagLayout());

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(
                                        220,
                                        225,
                                        230)),
                        new EmptyBorder(
                                15,
                                15,
                                15,
                                15)));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(
                6,
                8,
                6,
                8);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ==============================
        // MA CA
        // ==============================

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        panel.add(
                createLabel("Ma ca:"),
                gbc);

        txtMaCa = new JTextField();

        styleTextField(txtMaCa);

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(
                txtMaCa,
                gbc);

        // ==============================
        // TEN CA
        // ==============================

        gbc.gridx = 2;
        gbc.weightx = 0;

        panel.add(
                createLabel("Ten ca:"),
                gbc);

        txtTenCa = new JTextField();

        styleTextField(txtTenCa);

        gbc.gridx = 3;
        gbc.weightx = 1;

        panel.add(
                txtTenCa,
                gbc);

        // ==============================
        // GIO BAT DAU
        // ==============================

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;

        panel.add(
                createLabel("Gio bat dau:"),
                gbc);

        txtGioBatDau = new JTextField();

        styleTextField(txtGioBatDau);

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(
                txtGioBatDau,
                gbc);

        // ==============================
        // GIO KET THUC
        // ==============================

        gbc.gridx = 2;
        gbc.weightx = 0;

        panel.add(
                createLabel("Gio ket thuc:"),
                gbc);

        txtGioKetThuc = new JTextField();

        styleTextField(txtGioKetThuc);

        gbc.gridx = 3;
        gbc.weightx = 1;

        panel.add(
                txtGioKetThuc,
                gbc);

        // ==============================
        // BUTTON
        // ==============================

        JPanel buttons = createButtonPanel();

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.weightx = 1;

        panel.add(
                buttons,
                gbc);

        return panel;
    }

    // ==============================
    // LABEL
    // ==============================

    private JLabel createLabel(String text) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12));

        label.setForeground(
                new Color(
                        60,
                        60,
                        60));

        return label;
    }

    // ==============================
    // TEXT FIELD
    // ==============================

    private void styleTextField(
            JTextField textField) {

        textField.setPreferredSize(
                new Dimension(
                        220,
                        34));

        textField.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13));
    }

    // ==============================
    // BUTTON PANEL
    // ==============================

    private JPanel createButtonPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        8,
                        0));

        panel.setBackground(Color.WHITE);

        JButton btnThem = createActionButton(
                "Them",
                GREEN);

        JButton btnSua = createActionButton(
                "Sua",
                NAVY);

        JButton btnXoa = createActionButton(
                "Xoa",
                RED);

        JButton btnLamMoi = createActionButton(
                "Lam moi",
                ORANGE);

        btnThem.addActionListener(
                e -> them());

        btnSua.addActionListener(
                e -> sua());

        btnXoa.addActionListener(
                e -> xoa());

        btnLamMoi.addActionListener(
                e -> lamMoi());

        panel.add(btnThem);
        panel.add(btnSua);
        panel.add(btnXoa);
        panel.add(btnLamMoi);

        return panel;
    }

    // ==============================
    // ACTION BUTTON
    // ==============================

    private JButton createActionButton(
            String text,
            Color color) {

        JButton button = new JButton(text);

        button.setBackground(color);

        button.setForeground(Color.WHITE);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12));

        button.setFocusPainted(false);

        button.setBorderPainted(false);

        button.setPreferredSize(
                new Dimension(
                        85,
                        34));

        return button;
    }

    // ==============================
    // TABLE
    // ==============================

    private JPanel createTablePanel() {

        JPanel panel = new JPanel(
                new BorderLayout(
                        10,
                        10));

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(
                                        220,
                                        225,
                                        230)),
                        new EmptyBorder(
                                10,
                                10,
                                10,
                                10)));

        // ==============================
        // TOP
        // ==============================

        JPanel top = new JPanel(
                new BorderLayout());

        top.setBackground(Color.WHITE);

        JLabel title = new JLabel(
                "DANH SACH CA LAM");

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        16));

        title.setForeground(NAVY);

        top.add(
                title,
                BorderLayout.WEST);

        // ==============================
        // SEARCH
        // ==============================

        JPanel search = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        5,
                        0));

        search.setBackground(Color.WHITE);

        JLabel lbSearch = new JLabel("Tim kiem:");

        txtTimKiem = new JTextField(18);

        txtTimKiem.setPreferredSize(
                new Dimension(
                        180,
                        32));

        JButton btnTim = createSmallButton(
                "Tim",
                NAVY);

        JButton btnTatCa = createSmallButton(
                "Tat ca",
                new Color(
                        100,
                        110,
                        120));

        btnTim.addActionListener(
                e -> timKiem());

        btnTatCa.addActionListener(
                e -> loadData());

        search.add(lbSearch);
        search.add(txtTimKiem);
        search.add(btnTim);
        search.add(btnTatCa);

        top.add(
                search,
                BorderLayout.EAST);

        panel.add(
                top,
                BorderLayout.NORTH);

        // ==============================
        // MODEL
        // ==============================

        model = new DefaultTableModel(
                new String[] {
                        "Ma ca",
                        "Ten ca",
                        "Gio bat dau",
                        "Gio ket thuc"
                },
                0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {
                return false;
            }
        };

        table = new JTable(model);

        table.setRowHeight(30);

        table.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12));

        table.getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                12));

        table.getTableHeader()
                .setBackground(NAVY);

        table.getTableHeader()
                .setForeground(Color.WHITE);

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        table.setAutoCreateRowSorter(true);

        table.getSelectionModel()
                .addListSelectionListener(
                        e -> {

                            if (!e.getValueIsAdjusting()) {
                                hienThiDuLieu();
                            }

                        });

        JScrollPane scroll = new JScrollPane(table);

        panel.add(
                scroll,
                BorderLayout.CENTER);

        return panel;
    }

    // ==============================
    // SMALL BUTTON
    // ==============================

    private JButton createSmallButton(
            String text,
            Color color) {

        JButton button = new JButton(text);

        button.setBackground(color);

        button.setForeground(Color.WHITE);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11));

        button.setFocusPainted(false);

        button.setBorderPainted(false);

        button.setPreferredSize(
                new Dimension(
                        65,
                        32));

        return button;
    }

    // ==============================
    // LOAD DATA
    // ==============================

    private void loadData() {

        model.setRowCount(0);

        ArrayList<CaLam> list = caLamDAO.getAll();

        for (CaLam ca : list) {

            model.addRow(
                    new Object[] {
                            ca.getMaCa(),
                            ca.getTenCa(),
                            ca.getGioBatDau(),
                            ca.getGioKetThuc()
                    });
        }
    }

    // ==============================
    // HIEN THI
    // ==============================

    private void hienThiDuLieu() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            return;
        }

        int row = table.convertRowIndexToModel(
                selectedRow);

        txtMaCa.setText(
                valueAt(row, 0));

        txtTenCa.setText(
                valueAt(row, 1));

        txtGioBatDau.setText(
                valueAt(row, 2));

        txtGioKetThuc.setText(
                valueAt(row, 3));

        txtMaCa.setEnabled(false);
    }

    private String valueAt(
            int row,
            int column) {

        Object value = model.getValueAt(
                row,
                column);

        return value == null
                ? ""
                : value.toString();
    }

    // ==============================
    // THEM
    // ==============================

    private void them() {

        if (!kiemTraDuLieu()) {
            return;
        }

        CaLam ca = getCaLamFromForm();

        if (caLamDAO.them(ca)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Them ca lam thanh cong!",
                    "Thong bao",
                    JOptionPane.INFORMATION_MESSAGE);

            loadData();

            lamMoi();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Them ca lam that bai!",
                    "Loi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==============================
    // SUA
    // ==============================

    private void sua() {

        if (txtMaCa.isEnabled()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui long chon ca can sua!",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);

            return;
        }

        if (!kiemTraDuLieu()) {
            return;
        }

        CaLam ca = getCaLamFromForm();

        if (caLamDAO.sua(ca)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Cap nhat ca lam thanh cong!",
                    "Thong bao",
                    JOptionPane.INFORMATION_MESSAGE);

            loadData();

            lamMoi();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Cap nhat ca lam that bai!",
                    "Loi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==============================
    // XOA
    // ==============================

    private void xoa() {

        if (txtMaCa.isEnabled()
                || txtMaCa.getText()
                        .trim()
                        .isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui long chon ca can xoa!",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);

            return;
        }

        String maCa = txtMaCa.getText().trim();

        int result = JOptionPane.showConfirmDialog(
                this,
                "Ban co chac muon xoa ca "
                        + maCa
                        + "?",
                "Xac nhan",
                JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        if (caLamDAO.xoa(maCa)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Xoa ca lam thanh cong!",
                    "Thong bao",
                    JOptionPane.INFORMATION_MESSAGE);

            loadData();

            lamMoi();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Xoa ca lam that bai!",
                    "Loi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==============================
    // TIM KIEM
    // ==============================

    private void timKiem() {

        String keyword = txtTimKiem.getText()
                .trim()
                .toLowerCase();

        if (keyword.isEmpty()) {

            loadData();

            return;
        }

        model.setRowCount(0);

        ArrayList<CaLam> list = caLamDAO.getAll();

        for (CaLam ca : list) {

            boolean match = safe(ca.getMaCa())
                    .contains(keyword)

                    || safe(ca.getTenCa())
                            .contains(keyword)

                    || safe(ca.getGioBatDau())
                            .contains(keyword)

                    || safe(ca.getGioKetThuc())
                            .contains(keyword);

            if (match) {

                model.addRow(
                        new Object[] {
                                ca.getMaCa(),
                                ca.getTenCa(),
                                ca.getGioBatDau(),
                                ca.getGioKetThuc()
                        });
            }
        }
    }

    private String safe(String value) {

        if (value == null) {
            return "";
        }

        return value.toLowerCase();
    }

    // ==============================
    // LAM MOI
    // ==============================

    private void lamMoi() {

        txtMaCa.setText("");
        txtTenCa.setText("");
        txtGioBatDau.setText("");
        txtGioKetThuc.setText("");

        txtTimKiem.setText("");

        txtMaCa.setEnabled(true);

        table.clearSelection();
    }

    // ==============================
    // KIEM TRA
    // ==============================

    private boolean kiemTraDuLieu() {

        String maCa = txtMaCa.getText().trim();

        String tenCa = txtTenCa.getText().trim();

        String gioBatDau = txtGioBatDau.getText().trim();

        String gioKetThuc = txtGioKetThuc.getText().trim();

        if (maCa.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui long nhap ma ca!");

            txtMaCa.requestFocus();

            return false;
        }

        if (tenCa.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui long nhap ten ca!");

            txtTenCa.requestFocus();

            return false;
        }

        if (gioBatDau.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui long nhap gio bat dau!");

            txtGioBatDau.requestFocus();

            return false;
        }

        if (gioKetThuc.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui long nhap gio ket thuc!");

            txtGioKetThuc.requestFocus();

            return false;
        }

        return true;
    }

    // ==============================
    // FORM -> ENTITY
    // ==============================

    private CaLam getCaLamFromForm() {

        return new CaLam(
                txtMaCa.getText().trim(),
                txtTenCa.getText().trim(),
                txtGioBatDau.getText().trim(),
                txtGioKetThuc.getText().trim());
    }
}