package UI;

import dao.CaLamDAO;
import dao.MaTuDongDAO;
import java.sql.SQLException;
import entity.CaLam;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
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
        private static final DateTimeFormatter FORMAT_GIO = DateTimeFormatter.ofPattern("HH:mm")
                        .withResolverStyle(ResolverStyle.STRICT);

        // ==============================
        // FORM
        // ==============================

        private JTextField txtMaCa;
        private JTextField txtTenCa;
        private JFormattedTextField txtGioBatDau;
        private JFormattedTextField txtGioKetThuc;

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
                lamMoi();
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
                                "QUẢN LÝ CA LÀM");

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                24));

                title.setForeground(NAVY);

                JLabel sub = new JLabel(
                                "Quản lý thông tin ca làm việc");

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
                                createLabel("Mã ca:"),
                                gbc);

                txtMaCa = new JTextField();

                styleTextField(txtMaCa);
                txtMaCa.setEditable(false);
                txtMaCa.setToolTipText("Mã CA được hệ thống tự sinh khi lưu");

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
                                createLabel("Tên ca:"),
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
                                createLabel("Giờ bắt đầu:"),
                                gbc);

                txtGioBatDau = taoOChonGio();

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
                                createLabel("Giờ kết thúc:"),
                                gbc);

                txtGioKetThuc = taoOChonGio();

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

        /** Ô nhập tự chèn dấu ':' theo mặt nạ HH:mm (ví dụ 06:00). */
        private JFormattedTextField taoOChonGio() {
                try {
                        MaskFormatter mask = new MaskFormatter("##:##");
                        mask.setPlaceholderCharacter('_');
                        JFormattedTextField txtGio = new JFormattedTextField(mask);
                        txtGio.setFocusLostBehavior(JFormattedTextField.PERSIST);
                        txtGio.setToolTipText("Giờ theo HH:mm (00:00 - 23:59). Ví dụ: 06:00");
                        styleTextField(txtGio);
                        return txtGio;
                } catch (ParseException ex) {
                        throw new IllegalStateException("Không tạo được ô nhập giờ", ex);
                }
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
                                "DANH SÁCH CA LÀM");

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

                JLabel lbSearch = new JLabel("Tìm kiếm:");

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

                // ==============================
                // MODEL
                // ==============================

                model = new DefaultTableModel(
                                new String[] {
                                                "Mã ca",
                                                "Tên ca",
                                                "Giờ bắt đầu",
                                                "Giờ kết thúc"
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
                                                        dinhDangGio(ca.getGioBatDau()),
                                                        dinhDangGio(ca.getGioKetThuc())
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

                txtMaCa.setEditable(false);
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
                if (table.getSelectedRow() >= 0) {
                        JOptionPane.showMessageDialog(this,
                                        "Đang chọn ca cũ. Nhấn Làm mới để thêm ca mới.");
                        return;
                }
                if (!kiemTraDuLieu())
                        return;
                CaLam ca = getCaLamFromForm();
                try {
                        if (caLamDAO.them(ca)) {
                                JOptionPane.showMessageDialog(this,
                                                "Thêm ca làm thành công! Mã ca: " + ca.getMaCa());
                                loadData();
                                lamMoi();
                        } else {
                                JOptionPane.showMessageDialog(this, "Không thể thêm ca làm.");
                        }
                } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Lỗi thêm ca làm: " + ex.getMessage(),
                                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                }
        }

        // ==============================
        // SUA
        // ==============================

        private void sua() {

                if (table.getSelectedRow() < 0) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui lòng chọn ca cần sửa!",
                                        "Thông báo",
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
                                        "Cập nhật ca làm thành công!",
                                        "Thông báo",
                                        JOptionPane.INFORMATION_MESSAGE);

                        loadData();

                        lamMoi();

                } else {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Cập nhật ca làm thất bại!",
                                        "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                }
        }

        // ==============================
        // XOA
        // ==============================

        private void xoa() {

                if (table.getSelectedRow() < 0) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui lòng chọn ca cần xóa!",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);

                        return;
                }

                String maCa = txtMaCa.getText().trim();

                int result = JOptionPane.showConfirmDialog(
                                this,
                                "Bạn có chắc muốn xóa ca "
                                                + maCa
                                                + "?",
                                "Xác nhận",
                                JOptionPane.YES_NO_OPTION);

                if (result != JOptionPane.YES_OPTION) {
                        return;
                }

                if (caLamDAO.xoa(maCa)) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Xóa ca làm thành công!",
                                        "Thông báo",
                                        JOptionPane.INFORMATION_MESSAGE);

                        loadData();

                        lamMoi();

                } else {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Xóa ca làm thất bại!",
                                        "Lỗi",
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
                                                                dinhDangGio(ca.getGioBatDau()),
                                                                dinhDangGio(ca.getGioKetThuc())
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

                txtMaCa.setText(MaTuDongDAO.goiY(MaTuDongDAO.Loai.CA_LAM));
                txtTenCa.setText("");
                txtGioBatDau.setValue(null);
                txtGioKetThuc.setValue(null);

                txtTimKiem.setText("");

                txtMaCa.setEditable(false);

                table.clearSelection();
        }

        // ==============================
        // KIEM TRA
        // ==============================

        private boolean kiemTraDuLieu() {
                String tenCa = txtTenCa.getText().trim();
                String gioBatDau = txtGioBatDau.getText().trim();
                String gioKetThuc = txtGioKetThuc.getText().trim();

                if (tenCa.length() < 3 || tenCa.length() > 50
                                || !tenCa.matches("[\\p{L}0-9 ]+")) {
                        return baoLoiTruong(txtTenCa,
                                        "Tên ca từ 3–50 ký tự, chỉ gồm chữ, số và khoảng trắng.");
                }
                if (!kiemTraGio(gioBatDau, txtGioBatDau, "Giờ bắt đầu")
                                || !kiemTraGio(gioKetThuc, txtGioKetThuc, "Giờ kết thúc")) {
                        return false;
                }
                LocalTime batDau = LocalTime.parse(gioBatDau, FORMAT_GIO);
                LocalTime ketThuc = LocalTime.parse(gioKetThuc, FORMAT_GIO);
                if (batDau.equals(ketThuc)) {
                        return baoLoiTruong(txtGioKetThuc,
                                        "Giờ bắt đầu và kết thúc không được trùng nhau.");
                }
                // Ca qua dem (VD 22:00–06:00) duoc chap nhan.
                return true;
        }

        private boolean baoLoiTruong(JComponent truong, String thongBao) {
                JOptionPane.showMessageDialog(this, thongBao,
                                "Dữ liệu ca làm không hợp lệ", JOptionPane.WARNING_MESSAGE);
                truong.requestFocusInWindow();
                return false;
        }

        /** Kiểm tra giờ nhập đủ 5 ký tự HH:mm, giờ 00-23 và phút 00-59. */
        private boolean kiemTraGio(String giaTri, JFormattedTextField txtGio, String tenTruong) {
                if (!giaTri.matches("\\d{2}:\\d{2}")) {
                        JOptionPane.showMessageDialog(this,
                                        tenTruong + " phải có định dạng HH:mm, ví dụ 06:00.",
                                        "Giờ không hợp lệ", JOptionPane.WARNING_MESSAGE);
                        txtGio.requestFocusInWindow();
                        return false;
                }
                try {
                        LocalTime.parse(giaTri, FORMAT_GIO);
                        return true;
                } catch (DateTimeParseException ex) {
                        JOptionPane.showMessageDialog(this,
                                        tenTruong + " không hợp lệ. Giờ phải từ 00 đến 23, phút từ 00 đến 59.",
                                        "Giờ không hợp lệ", JOptionPane.WARNING_MESSAGE);
                        txtGio.requestFocusInWindow();
                        return false;
                }
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

        /** Chỉ hiển thị HH:mm, giữ nguyên dữ liệu thời gian trong CSDL. */
        private String dinhDangGio(String gio) {
                if (gio == null)
                        return "";
                String s = gio.trim();
                return s.length() >= 5 ? s.substring(0, 5) : s;
        }

}