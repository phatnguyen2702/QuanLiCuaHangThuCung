package UI;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class QuanLyTaiKhoan extends JPanel {

        // =========================================================
        // MAU
        // =========================================================

        private final Color NAVY = new Color(18, 48, 82);
        private final Color LIGHT_BLUE = new Color(229, 241, 252);
        private final Color BG = new Color(245, 247, 250);
        private final Color GREEN = new Color(72, 196, 62);
        private final Color RED = new Color(210, 70, 70);
        private final Color ORANGE = new Color(230, 150, 50);

        // =========================================================
        // FORM
        // =========================================================

        private JTextField txtMaNV;
        private JTextField txtHoTen;
        private JTextField txtSoDienThoai;
        private JTextField txtEmail;
        private JPasswordField txtMatKhau;

        private JComboBox<String> cboGioiTinh;
        private JComboBox<String> cboVaiTro;

        private JTextField txtTimKiem;

        // =========================================================
        // TABLE
        // =========================================================

        private JTable table;
        private DefaultTableModel model;

        // =========================================================
        // DAO
        // =========================================================

        private TaiKhoanDAO taiKhoanDAO;

        // =========================================================
        // CONSTRUCTOR
        // =========================================================

        public QuanLyTaiKhoan() {

                taiKhoanDAO = new TaiKhoanDAO();

                setLayout(
                                new BorderLayout());

                setBackground(BG);

                add(
                                createTitle(),
                                BorderLayout.NORTH);

                add(
                                createCenter(),
                                BorderLayout.CENTER);

                loadData();
        }

        // =========================================================
        // TITLE
        // =========================================================

        private JPanel createTitle() {

                JPanel panel = new JPanel(
                                new BorderLayout());

                panel.setBackground(
                                Color.WHITE);

                panel.setBorder(
                                new EmptyBorder(
                                                18,
                                                25,
                                                18,
                                                25));

                JLabel title = new JLabel(
                                "QUAN LY TAI KHOAN");

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                24));

                title.setForeground(
                                NAVY);

                JLabel sub = new JLabel(
                                "Quan ly thong tin tai khoan nhan vien");

                sub.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                13));

                sub.setForeground(
                                Color.GRAY);

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

        // =========================================================
        // CENTER
        // =========================================================

        private JPanel createCenter() {

                JPanel panel = new JPanel(
                                new BorderLayout(
                                                15,
                                                15));

                panel.setBackground(
                                BG);

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

        // =========================================================
        // FORM
        // =========================================================

        private JPanel createForm() {

                JPanel panel = new JPanel(
                                new GridBagLayout());

                panel.setBackground(
                                Color.WHITE);

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

                // -----------------------------------------------------
                // MA NHAN VIEN
                // -----------------------------------------------------

                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.weightx = 0;

                panel.add(
                                createLabel(
                                                "Ma nhan vien:"),
                                gbc);

                txtMaNV = new JTextField();

                styleTextField(
                                txtMaNV);

                gbc.gridx = 1;
                gbc.weightx = 1;

                panel.add(
                                txtMaNV,
                                gbc);

                // -----------------------------------------------------
                // HO TEN
                // -----------------------------------------------------

                gbc.gridx = 2;
                gbc.weightx = 0;

                panel.add(
                                createLabel(
                                                "Ho ten:"),
                                gbc);

                txtHoTen = new JTextField();

                styleTextField(
                                txtHoTen);

                gbc.gridx = 3;
                gbc.weightx = 1;

                panel.add(
                                txtHoTen,
                                gbc);

                // -----------------------------------------------------
                // SO DIEN THOAI
                // -----------------------------------------------------

                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.weightx = 0;

                panel.add(
                                createLabel(
                                                "So dien thoai:"),
                                gbc);

                txtSoDienThoai = new JTextField();

                styleTextField(
                                txtSoDienThoai);

                gbc.gridx = 1;
                gbc.weightx = 1;

                panel.add(
                                txtSoDienThoai,
                                gbc);

                // -----------------------------------------------------
                // EMAIL
                // -----------------------------------------------------

                gbc.gridx = 2;
                gbc.weightx = 0;

                panel.add(
                                createLabel(
                                                "Email:"),
                                gbc);

                txtEmail = new JTextField();

                styleTextField(
                                txtEmail);

                gbc.gridx = 3;
                gbc.weightx = 1;

                panel.add(
                                txtEmail,
                                gbc);

                // -----------------------------------------------------
                // MAT KHAU
                // -----------------------------------------------------

                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.weightx = 0;

                panel.add(
                                createLabel(
                                                "Mat khau:"),
                                gbc);

                txtMatKhau = new JPasswordField();

                styleTextField(
                                txtMatKhau);

                gbc.gridx = 1;
                gbc.weightx = 1;

                panel.add(
                                txtMatKhau,
                                gbc);

                // -----------------------------------------------------
                // GIOI TINH
                // -----------------------------------------------------

                gbc.gridx = 2;
                gbc.weightx = 0;

                panel.add(
                                createLabel(
                                                "Gioi tinh:"),
                                gbc);

                cboGioiTinh = new JComboBox<>(
                                new String[] {
                                                "Nam",
                                                "Nu",
                                                "Khac"
                                });

                styleComboBox(
                                cboGioiTinh);

                gbc.gridx = 3;
                gbc.weightx = 1;

                panel.add(
                                cboGioiTinh,
                                gbc);

                // -----------------------------------------------------
                // VAI TRO
                // -----------------------------------------------------

                gbc.gridx = 0;
                gbc.gridy = 3;
                gbc.weightx = 0;

                panel.add(
                                createLabel(
                                                "Vai tro:"),
                                gbc);

                cboVaiTro = new JComboBox<>(
                                new String[] {
                                                "Quan ly",
                                                "Thu ngan",
                                                "Nhan vien kho",
                                                "Nhan vien Spa"
                                });

                styleComboBox(
                                cboVaiTro);

                gbc.gridx = 1;
                gbc.weightx = 1;

                panel.add(
                                cboVaiTro,
                                gbc);

                // -----------------------------------------------------
                // BUTTON
                // -----------------------------------------------------

                JPanel buttons = createButtonPanel();

                gbc.gridx = 2;
                gbc.gridy = 3;
                gbc.gridwidth = 2;
                gbc.weightx = 1;

                panel.add(
                                buttons,
                                gbc);

                return panel;
        }

        // =========================================================
        // LABEL
        // =========================================================

        private JLabel createLabel(
                        String text) {

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

        // =========================================================
        // TEXT FIELD STYLE
        // =========================================================

        private void styleTextField(
                        JComponent component) {

                component.setPreferredSize(
                                new Dimension(
                                                220,
                                                34));

                component.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                13));
        }

        // =========================================================
        // COMBO BOX STYLE
        // =========================================================

        private void styleComboBox(
                        JComboBox<String> combo) {

                combo.setPreferredSize(
                                new Dimension(
                                                220,
                                                34));

                combo.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                13));
        }

        // =========================================================
        // BUTTON PANEL
        // =========================================================

        private JPanel createButtonPanel() {

                JPanel panel = new JPanel(
                                new FlowLayout(
                                                FlowLayout.RIGHT,
                                                8,
                                                0));

                panel.setBackground(
                                Color.WHITE);

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

        // =========================================================
        // ACTION BUTTON
        // =========================================================

        private JButton createActionButton(
                        String text,
                        Color color) {

                JButton button = new JButton(text);

                button.setBackground(
                                color);

                button.setForeground(
                                Color.WHITE);

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

        // =========================================================
        // TABLE PANEL
        // =========================================================

        private JPanel createTablePanel() {

                JPanel panel = new JPanel(
                                new BorderLayout(
                                                10,
                                                10));

                panel.setBackground(
                                Color.WHITE);

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

                // -----------------------------------------------------
                // TITLE + SEARCH
                // -----------------------------------------------------

                JPanel top = new JPanel(
                                new BorderLayout());

                top.setBackground(
                                Color.WHITE);

                JLabel title = new JLabel(
                                "DANH SACH TAI KHOAN");

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                16));

                title.setForeground(
                                NAVY);

                top.add(
                                title,
                                BorderLayout.WEST);

                JPanel search = new JPanel(
                                new FlowLayout(
                                                FlowLayout.RIGHT,
                                                5,
                                                0));

                search.setBackground(
                                Color.WHITE);

                JLabel lbSearch = new JLabel(
                                "Tim kiem:");

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

                // -----------------------------------------------------
                // TABLE MODEL
                // -----------------------------------------------------

                model = new DefaultTableModel(
                                new String[] {
                                                "Ma NV",
                                                "Ho ten",
                                                "So dien thoai",
                                                "Email",
                                                "Mat khau",
                                                "Gioi tinh",
                                                "Vai tro"
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
                                .setBackground(
                                                NAVY);

                table.getTableHeader()
                                .setForeground(
                                                Color.WHITE);

                table.setSelectionMode(
                                ListSelectionModel.SINGLE_SELECTION);

                table.setAutoCreateRowSorter(
                                true);

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

        // =========================================================
        // SMALL BUTTON
        // =========================================================

        private JButton createSmallButton(
                        String text,
                        Color color) {

                JButton button = new JButton(text);

                button.setBackground(
                                color);

                button.setForeground(
                                Color.WHITE);

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

        // =========================================================
        // LOAD DATA
        // =========================================================

        private void loadData() {

                model.setRowCount(0);

                ArrayList<TaiKhoan> list = taiKhoanDAO.getAll();

                for (TaiKhoan tk : list) {

                        model.addRow(
                                        new Object[] {
                                                        tk.getMaNhanVien(),
                                                        tk.getHoTen(),
                                                        tk.getSoDienThoai(),
                                                        tk.getEmail(),
                                                        tk.getMatKhau(),
                                                        tk.getGioiTinh(),
                                                        tk.getVaiTro()
                                        });
                }
        }

        // =========================================================
        // HIEN THI DU LIEU
        // =========================================================

        private void hienThiDuLieu() {

                int selectedRow = table.getSelectedRow();

                if (selectedRow < 0) {
                        return;
                }

                int row = table.convertRowIndexToModel(
                                selectedRow);

                txtMaNV.setText(
                                valueAt(row, 0));

                txtHoTen.setText(
                                valueAt(row, 1));

                txtSoDienThoai.setText(
                                valueAt(row, 2));

                txtEmail.setText(
                                valueAt(row, 3));

                txtMatKhau.setText(
                                valueAt(row, 4));

                cboGioiTinh.setSelectedItem(
                                valueAt(row, 5));

                cboVaiTro.setSelectedItem(
                                valueAt(row, 6));

                // Khong cho sua ma nhan vien
                txtMaNV.setEnabled(false);
        }

        // =========================================================
        // VALUE TABLE
        // =========================================================

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

        // =========================================================
        // THEM
        // =========================================================

        private void them() {

                if (!kiemTraDuLieu()) {
                        return;
                }

                TaiKhoan tk = getTaiKhoanFromForm();

                if (taiKhoanDAO.them(tk)) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Them tai khoan thanh cong!",
                                        "Thong bao",
                                        JOptionPane.INFORMATION_MESSAGE);

                        loadData();

                        lamMoi();

                } else {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Them tai khoan that bai!",
                                        "Loi",
                                        JOptionPane.ERROR_MESSAGE);
                }
        }

        // =========================================================
        // SUA
        // =========================================================

        private void sua() {

                if (!kiemTraDuLieu()) {
                        return;
                }

                if (txtMaNV.isEnabled()) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui long chon tai khoan can sua!",
                                        "Thong bao",
                                        JOptionPane.WARNING_MESSAGE);

                        return;
                }

                TaiKhoan tk = getTaiKhoanFromForm();

                if (taiKhoanDAO.sua(tk)) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Cap nhat tai khoan thanh cong!",
                                        "Thong bao",
                                        JOptionPane.INFORMATION_MESSAGE);

                        loadData();

                        lamMoi();

                } else {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Cap nhat tai khoan that bai!",
                                        "Loi",
                                        JOptionPane.ERROR_MESSAGE);
                }
        }

        // =========================================================
        // XOA
        // =========================================================

        private void xoa() {

                if (txtMaNV.isEnabled()
                                || txtMaNV.getText()
                                                .trim()
                                                .isEmpty()) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui long chon tai khoan can xoa!",
                                        "Thong bao",
                                        JOptionPane.WARNING_MESSAGE);

                        return;
                }

                String ma = txtMaNV.getText()
                                .trim();

                int result = JOptionPane.showConfirmDialog(
                                this,
                                "Ban co chac muon xoa tai khoan "
                                                + ma
                                                + "?",
                                "Xac nhan",
                                JOptionPane.YES_NO_OPTION);

                if (result != JOptionPane.YES_OPTION) {
                        return;
                }

                if (taiKhoanDAO.xoa(ma)) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Xoa tai khoan thanh cong!",
                                        "Thong bao",
                                        JOptionPane.INFORMATION_MESSAGE);

                        loadData();

                        lamMoi();

                } else {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Xoa tai khoan that bai!",
                                        "Loi",
                                        JOptionPane.ERROR_MESSAGE);
                }
        }

        // =========================================================
        // TIM KIEM
        // =========================================================

        private void timKiem() {

                String keyword = txtTimKiem.getText()
                                .trim()
                                .toLowerCase();

                if (keyword.isEmpty()) {

                        loadData();

                        return;
                }

                model.setRowCount(0);

                ArrayList<TaiKhoan> list = taiKhoanDAO.getAll();

                for (TaiKhoan tk : list) {

                        boolean match = safe(tk.getMaNhanVien())
                                        .contains(keyword)

                                        || safe(tk.getHoTen())
                                                        .contains(keyword)

                                        || safe(tk.getSoDienThoai())
                                                        .contains(keyword)

                                        || safe(tk.getEmail())
                                                        .contains(keyword)

                                        || safe(tk.getGioiTinh())
                                                        .contains(keyword)

                                        || safe(tk.getVaiTro())
                                                        .contains(keyword);

                        if (match) {

                                model.addRow(
                                                new Object[] {
                                                                tk.getMaNhanVien(),
                                                                tk.getHoTen(),
                                                                tk.getSoDienThoai(),
                                                                tk.getEmail(),
                                                                tk.getMatKhau(),
                                                                tk.getGioiTinh(),
                                                                tk.getVaiTro()
                                                });
                        }
                }
        }

        // =========================================================
        // SAFE STRING
        // =========================================================

        private String safe(
                        String value) {

                if (value == null) {
                        return "";
                }

                return value.toLowerCase();
        }

        // =========================================================
        // LAM MOI
        // =========================================================

        private void lamMoi() {

                txtMaNV.setText("");
                txtHoTen.setText("");
                txtSoDienThoai.setText("");
                txtEmail.setText("");
                txtMatKhau.setText("");

                cboGioiTinh.setSelectedIndex(0);
                cboVaiTro.setSelectedIndex(0);

                txtTimKiem.setText("");

                txtMaNV.setEnabled(true);

                table.clearSelection();
        }

        // =========================================================
        // KIEM TRA DU LIEU
        // =========================================================

        private boolean kiemTraDuLieu() {

                String ma = txtMaNV.getText()
                                .trim();

                String hoTen = txtHoTen.getText()
                                .trim();

                String sdt = txtSoDienThoai.getText()
                                .trim();

                String email = txtEmail.getText()
                                .trim();

                String matKhau = new String(
                                txtMatKhau.getPassword());

                // Ma NV
                if (ma.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui long nhap ma nhan vien!");

                        txtMaNV.requestFocus();

                        return false;
                }

                if (ma.length() > 10) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Ma nhan vien toi da 10 ky tu!");

                        txtMaNV.requestFocus();

                        return false;
                }

                // Ho ten
                if (hoTen.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui long nhap ho ten!");

                        txtHoTen.requestFocus();

                        return false;
                }

                // SDT
                if (!sdt.isEmpty()
                                && !sdt.matches(
                                                "\\d{10,15}")) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "So dien thoai phai gom 10-15 chu so!");

                        txtSoDienThoai.requestFocus();

                        return false;
                }

                // Email
                if (!email.isEmpty()
                                && !email.matches(
                                                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Email khong hop le!");

                        txtEmail.requestFocus();

                        return false;
                }

                // Mat khau
                if (matKhau.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui long nhap mat khau!");

                        txtMatKhau.requestFocus();

                        return false;
                }

                if (matKhau.length() > 25) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Mat khau toi da 25 ky tu!");

                        txtMatKhau.requestFocus();

                        return false;
                }

                return true;
        }

        // =========================================================
        // FORM -> ENTITY
        // =========================================================

        private TaiKhoan getTaiKhoanFromForm() {

                return new TaiKhoan(
                                txtMaNV.getText().trim(),
                                txtHoTen.getText().trim(),
                                txtSoDienThoai.getText().trim(),
                                txtEmail.getText().trim(),
                                new String(
                                                txtMatKhau.getPassword()),
                                cboGioiTinh.getSelectedItem()
                                                .toString(),
                                cboVaiTro.getSelectedItem()
                                                .toString());
        }
}