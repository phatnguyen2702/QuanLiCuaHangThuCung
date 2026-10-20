package UI;

import dao.TaiKhoanDAO;
import dao.MaTuDongDAO;
import java.sql.SQLException;
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
                lamMoi();
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
                                "QUẢN LÝ TÀI KHOẢN");

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                24));

                title.setForeground(
                                NAVY);

                JLabel sub = new JLabel(
                                "Quản lý thông tin tài khoản nhân viên");

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
                                                "Mã nhân viên:"),
                                gbc);

                txtMaNV = new JTextField();

                styleTextField(
                                txtMaNV);
                txtMaNV.setEditable(false);
                txtMaNV.setToolTipText("Mã NV được hệ thống tự sinh khi lưu");

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
                                                "Họ tên:"),
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
                                                "Số điện thoại:"),
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
                                                "Mật khẩu:"),
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
                                                "Giới tính:"),
                                gbc);

                cboGioiTinh = new JComboBox<>(
                                new String[] {
                                                "Nam",
                                                "Nu"
                                });

                styleComboBox(
                                cboGioiTinh);
                cboGioiTinh.setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(JList<?> list, Object value,
                                        int index, boolean selected, boolean focused) {
                                super.getListCellRendererComponent(list, value, index, selected, focused);
                                setText(hienThiGioiTinh(value == null ? "" : value.toString()));
                                return this;
                        }
                });

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
                                                "Vai trò:"),
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
                cboVaiTro.setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(JList<?> list, Object value,
                                        int index, boolean selected, boolean focused) {
                                super.getListCellRendererComponent(list, value, index, selected, focused);
                                setText(hienThiVaiTro(value == null ? "" : value.toString()));
                                return this;
                        }
                });

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
                                "Thêm",
                                GREEN);

                JButton btnSua = createActionButton(
                                "Sửa",
                                NAVY);

                JButton btnXoa = createActionButton(
                                "Xóa",
                                RED);

                JButton btnLamMoi = createActionButton(
                                "Làm mới",
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
                                "DANH SÁCH TÀI KHOẢN");

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
                                "Tìm kiếm:");

                txtTimKiem = new JTextField(18);

                txtTimKiem.setPreferredSize(
                                new Dimension(
                                                180,
                                                32));

                JButton btnTim = createSmallButton(
                                "Tìm",
                                NAVY);

                JButton btnTatCa = createSmallButton(
                                "Tất cả",
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
                                                "Mã NV",
                                                "Họ tên",
                                                "Số điện thoại",
                                                "Email",
                                                "Mật khẩu",
                                                "Giới tính",
                                                "Vai trò"
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
                                                        hienThiGioiTinh(tk.getGioiTinh()),
                                                        hienThiVaiTro(tk.getVaiTro())
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
                                maGioiTinh(valueAt(row, 5)));

                cboVaiTro.setSelectedItem(
                                maVaiTro(valueAt(row, 6)));

                // Khong cho sua ma nhan vien
                txtMaNV.setEditable(false);
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
                if (table.getSelectedRow() >= 0) {
                        JOptionPane.showMessageDialog(this,
                                        "Đang chọn nhân viên cũ. Nhấn Làm mới để thêm nhân viên mới.");
                        return;
                }
                if (!kiemTraDuLieu())
                        return;
                TaiKhoan tk = getTaiKhoanFromForm();
                try {
                        if (taiKhoanDAO.them(tk)) {
                                JOptionPane.showMessageDialog(this,
                                                "Thêm nhân viên thành công! Mã nhân viên: " + tk.getMaNhanVien());
                                loadData();
                                lamMoi();
                        } else {
                                JOptionPane.showMessageDialog(this, "Không thể thêm nhân viên.");
                        }
                } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this,
                                        "Lỗi thêm nhân viên: " + ex.getMessage(),
                                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                }
        }

        // =========================================================
        // SUA
        // =========================================================

        private void sua() {

                if (!kiemTraDuLieu()) {
                        return;
                }

                if (table.getSelectedRow() < 0) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui lòng chọn tài khoản cần sửa!",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);

                        return;
                }

                TaiKhoan tk = getTaiKhoanFromForm();

                if (taiKhoanDAO.sua(tk)) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Cập nhật tài khoản thành công!",
                                        "Thông báo",
                                        JOptionPane.INFORMATION_MESSAGE);

                        loadData();

                        lamMoi();

                } else {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Cập nhật tài khoản thất bại!",
                                        "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                }
        }

        // =========================================================
        // XOA
        // =========================================================

        private void xoa() {

                if (table.getSelectedRow() < 0) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui lòng chọn tài khoản cần xóa!",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);

                        return;
                }

                String ma = txtMaNV.getText()
                                .trim();

                int result = JOptionPane.showConfirmDialog(
                                this,
                                "Bạn có chắc muốn xóa tài khoản "
                                                + ma
                                                + "?",
                                "Xác nhận",
                                JOptionPane.YES_NO_OPTION);

                if (result != JOptionPane.YES_OPTION) {
                        return;
                }

                if (taiKhoanDAO.xoa(ma)) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Xóa tài khoản thành công!",
                                        "Thông báo",
                                        JOptionPane.INFORMATION_MESSAGE);

                        loadData();

                        lamMoi();

                } else {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Xóa tài khoản thất bại!",
                                        "Lỗi",
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

                                        || safe(hienThiVaiTro(tk.getVaiTro()))
                                                        .contains(keyword);

                        if (match) {

                                model.addRow(
                                                new Object[] {
                                                                tk.getMaNhanVien(),
                                                                tk.getHoTen(),
                                                                tk.getSoDienThoai(),
                                                                tk.getEmail(),
                                                                tk.getMatKhau(),
                                                                hienThiGioiTinh(tk.getGioiTinh()),
                                                                hienThiVaiTro(tk.getVaiTro())
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

                txtMaNV.setText(MaTuDongDAO.goiY(MaTuDongDAO.Loai.NHAN_VIEN));
                txtHoTen.setText("");
                txtSoDienThoai.setText("");
                txtEmail.setText("");
                txtMatKhau.setText("");

                cboGioiTinh.setSelectedIndex(0);
                cboVaiTro.setSelectedIndex(0);

                txtTimKiem.setText("");

                txtMaNV.setEditable(false);

                table.clearSelection();
        }

        // =========================================================
        // KIEM TRA DU LIEU
        // =========================================================

        private boolean kiemTraDuLieu() {
                String hoTen = txtHoTen.getText().trim();
                String sdt = txtSoDienThoai.getText().trim();
                String email = txtEmail.getText().trim();
                String matKhau = new String(txtMatKhau.getPassword());

                if (hoTen.length() < 2 || hoTen.length() > 100
                                || !hoTen.matches("[\\p{L} ]+")) {
                        return baoLoiTruong(txtHoTen,
                                        "Họ tên từ 2–100 ký tự, chỉ chứa chữ cái (có dấu) và dấu cách.");
                }
                if (!sdt.matches("0\\d{9}")) {
                        return baoLoiTruong(txtSoDienThoai,
                                        "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.");
                }
                if (email.length() > 254 || !email.matches(
                                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                        return baoLoiTruong(txtEmail, "Email không hợp lệ. Ví dụ: ten@gmail.com.");
                }
                if (matKhau.length() < 6) {
                        return baoLoiTruong(txtMatKhau,
                                        "Mật khẩu phải có ít nhất 6 ký tự (không giới hạn loại ký tự).");
                }
                if (cboGioiTinh.getSelectedItem() == null
                                || !("Nam".equals(cboGioiTinh.getSelectedItem())
                                                || "Nu".equals(cboGioiTinh.getSelectedItem()))
                                || cboVaiTro.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(this, "Giới tính chỉ được chọn Nam/Nữ và phải chọn vai trò.",
                                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                        return false;
                }
                return true;
        }

        private boolean baoLoiTruong(JComponent truong, String thongBao) {
                JOptionPane.showMessageDialog(this, thongBao,
                                "Dữ liệu nhân viên không hợp lệ", JOptionPane.WARNING_MESSAGE);
                truong.requestFocusInWindow();
                return false;
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

        // Chỉ đổi tên hiển thị, KHÔNG thay giá trị dùng để lưu trong CSDL.
        private String hienThiGioiTinh(String s) {
                if (s == null)
                        return "";
                return switch (s.trim().toLowerCase(java.util.Locale.ROOT)) {
                        case "nu", "nữ" -> "Nữ";
                        case "nam" -> "Nam";
                        default -> s;
                };
        }

        private String maGioiTinh(String s) {
                return switch (hienThiGioiTinh(s)) {
                        case "Nữ" -> "Nu";
                        default -> "Nam";
                };
        }

        private String hienThiVaiTro(String s) {
                if (s == null)
                        return "";
                String vaiTro = s.trim().toLowerCase(java.util.Locale.ROOT);
                return switch (vaiTro) {
                        case "quan ly", "quản lý" -> "Quản lý";
                        case "thu ngan", "thu ngân" -> "Thu ngân";
                        case "nhan vien kho", "nhân viên kho" -> "Nhân viên kho";
                        case "nhan vien spa", "nhân viên spa" -> "Nhân viên Spa";
                        default -> s;
                };
        }

        private String maVaiTro(String s) {
                return switch (hienThiVaiTro(s)) {
                        case "Quản lý" -> "Quan ly";
                        case "Thu ngân" -> "Thu ngan";
                        case "Nhân viên kho" -> "Nhan vien kho";
                        case "Nhân viên Spa" -> "Nhan vien Spa";
                        default -> s;
                };
        }

}