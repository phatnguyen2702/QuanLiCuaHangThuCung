package UI;

import dao.CaLamDAO;
import dao.MaTuDongDAO;
import entity.CaLam;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Giao diện quản lý ca làm; giữ luồng Form -> Entity -> DAO -> SQL Server. */
public class QuanLyCaLam extends JPanel implements ActionListener {

        // Màu sắc và định dạng dùng chung trong giao diện.
        private static final Color NAVY = new Color(18, 48, 82);
        private static final Color BG = new Color(245, 247, 250);
        private static final Color GREEN = new Color(72, 196, 62);
        private static final Color RED = new Color(210, 70, 70);
        private static final Color ORANGE = new Color(230, 150, 50);
        private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);
        private static final DateTimeFormatter FORMAT_GIO = DateTimeFormatter
                        .ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

        // JTextField: ô nhập liệu; mã ca chỉ đọc vì hệ thống tự sinh mã.
        private JTextField txtMaCa, txtTenCa, txtTimKiem;
        // JFormattedTextField: nhập giờ có mặt nạ HH:mm.
        private JFormattedTextField txtGioBatDau, txtGioKetThuc;
        // JButton: các thao tác của màn hình.
        private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnTim, btnTatCa;
        // JTable / DefaultTableModel: hiển thị danh sách ca làm, không chỉnh sửa trực
        // tiếp.
        private JTable tblDuLieu;
        private DefaultTableModel model;
        // DAO: lớp truy vấn và cập nhật ca làm trong SQL Server.
        private final CaLamDAO caLamDAO = new CaLamDAO();

        public QuanLyCaLam() {
                // BorderLayout: tiêu đề phía trên, form và bảng ở giữa.
                setLayout(new BorderLayout());
                setBackground(BG);
                add(createTitle(), BorderLayout.NORTH);
                add(createCenter(), BorderLayout.CENTER);
                loadData();
                lamMoi();
        }

        // JPanel: khung tiêu đề; BoxLayout: xếp tiêu đề và mô tả theo chiều dọc.
        private JPanel createTitle() {
                JPanel header = new JPanel(new BorderLayout());
                header.setBackground(Color.WHITE);
                header.setBorder(new EmptyBorder(18, 25, 18, 25));

                JLabel title = createLabel("QUẢN LÝ CA LÀM", 24, NAVY);
                JLabel subtitle = createLabel("Quản lý thông tin ca làm việc", 13, Color.GRAY);
                subtitle.setFont(FONT);

                JPanel text = new JPanel();
                text.setOpaque(false);
                text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
                text.add(title);
                text.add(Box.createVerticalStrut(5));
                text.add(subtitle);
                header.add(text, BorderLayout.WEST);
                return header;
        }

        // JPanel: khung nội dung; BorderLayout: form phía trên, bảng phía dưới.
        private JPanel createCenter() {
                JPanel content = new JPanel(new BorderLayout(15, 15));
                content.setBackground(BG);
                content.setBorder(new EmptyBorder(20, 25, 20, 25));
                content.add(createForm(), BorderLayout.NORTH);
                content.add(createTablePanel(), BorderLayout.CENTER);
                return content;
        }

        // GridBagLayout: bố trí 4 trường nhập trên 2 hàng, dưới cùng là hàng nút.
        private JPanel createForm() {
                JPanel form = createWhitePanel(new GridBagLayout(), 15);
                txtMaCa = createTextField();
                txtMaCa.setEditable(false);
                txtMaCa.setToolTipText("Mã CA được hệ thống tự sinh khi lưu");
                txtTenCa = createTextField();
                txtGioBatDau = taoOChonGio();
                txtGioKetThuc = taoOChonGio();

                addField(form, "Mã ca:", txtMaCa, 0, 0);
                addField(form, "Tên ca:", txtTenCa, 2, 0);
                addField(form, "Giờ bắt đầu:", txtGioBatDau, 0, 1);
                addField(form, "Giờ kết thúc:", txtGioKetThuc, 2, 1);

                // GridBagConstraints: hàng cuối chiếm đủ 4 cột của form.
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.gridwidth = 4;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(6, 8, 6, 8);
                form.add(createButtonPanel(), gbc);
                return form;
        }

        // Hàm dùng chung để tránh lặp code đặt JLabel và trường nhập ở mỗi hàng.
        private void addField(JPanel panel, String name, JComponent field, int x, int y) {
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridy = y;
                gbc.insets = new Insets(6, 8, 6, 8);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.gridx = x;
                gbc.weightx = 0;
                panel.add(createLabel(name, 12, new Color(60, 60, 60)), gbc);
                gbc.gridx = x + 1;
                gbc.weightx = 1;
                panel.add(field, gbc);
        }

        private JLabel createLabel(String text, int size, Color color) {
                JLabel label = new JLabel(text);
                label.setFont(new Font("Segoe UI", Font.BOLD, size));
                label.setForeground(color);
                return label;
        }

        // Tạo ô nhập theo cùng font và kích thước.
        private JTextField createTextField() {
                JTextField field = new JTextField();
                styleTextField(field);
                return field;
        }

        private void styleTextField(JTextField field) {
                field.setPreferredSize(new Dimension(220, 34));
                field.setFont(FONT);
        }

        // MaskFormatter: tự đặt dấu ':' và yêu cầu đủ 4 chữ số (HH:mm).
        private JFormattedTextField taoOChonGio() {
                try {
                        MaskFormatter mask = new MaskFormatter("##:##");
                        mask.setPlaceholderCharacter('_');
                        JFormattedTextField field = new JFormattedTextField(mask);
                        field.setFocusLostBehavior(JFormattedTextField.PERSIST);
                        field.setToolTipText("Giờ theo HH:mm (00:00 - 23:59). Ví dụ: 06:00");
                        styleTextField(field);
                        return field;
                } catch (ParseException ex) {
                        throw new IllegalStateException("Không tạo được ô nhập giờ", ex);
                }
        }

        // FlowLayout.RIGHT: nút Thêm, Sửa, Xóa và Làm mới nằm bên phải biểu mẫu.
        private JPanel createButtonPanel() {
                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                buttons.setBackground(Color.WHITE);
                btnThem = createButton("Thêm", GREEN, 85, 34, 12);
                btnSua = createButton("Sửa", NAVY, 85, 34, 12);
                btnXoa = createButton("Xóa", RED, 85, 34, 12);
                btnLamMoi = createButton("Làm mới", ORANGE, 85, 34, 12);
                for (JButton button : new JButton[] { btnThem, btnSua, btnXoa, btnLamMoi }) {
                        buttons.add(button);
                }
                return buttons;
        }

        // Dùng chung cách tạo nút và đăng ký ActionListener cho mọi thao tác.
        private JButton createButton(String text, Color color, int width, int height, int fontSize) {
                JButton button = new JButton(text);
                button.setBackground(color);
                button.setForeground(Color.WHITE);
                button.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
                button.setFocusPainted(false);
                button.setBorderPainted(false);
                button.setPreferredSize(new Dimension(width, height));
                button.addActionListener(this);
                return button;
        }

        // Khung nền trắng có viền giống form và bảng của phiên bản gốc.
        private JPanel createWhitePanel(LayoutManager layout, int padding) {
                JPanel panel = new JPanel(layout);
                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                                new EmptyBorder(padding, padding, padding, padding)));
                return panel;
        }

        // JPanel + JTable: khu vực tìm kiếm và danh sách ca làm.
        private JPanel createTablePanel() {
                JPanel panel = createWhitePanel(new BorderLayout(10, 10), 10);
                JPanel top = new JPanel(new BorderLayout());
                top.setBackground(Color.WHITE);
                top.add(createLabel("DANH SÁCH CA LÀM", 16, NAVY), BorderLayout.WEST);

                JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                search.setBackground(Color.WHITE);
                txtTimKiem = new JTextField(18);
                txtTimKiem.setPreferredSize(new Dimension(180, 32));
                btnTim = createButton("Tìm", NAVY, 65, 32, 11);
                btnTatCa = createButton("Tất cả", new Color(100, 110, 120), 65, 32, 11);
                search.add(new JLabel("Tìm kiếm:"));
                search.add(txtTimKiem);
                search.add(btnTim);
                search.add(btnTatCa);
                top.add(search, BorderLayout.EAST);
                panel.add(top, BorderLayout.NORTH);

                // DefaultTableModel: khóa chỉnh sửa trực tiếp từng ô trong JTable.
                model = new DefaultTableModel(new String[] {
                                "Mã ca", "Tên ca", "Giờ bắt đầu", "Giờ kết thúc"
                }, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                                return false;
                        }
                };
                tblDuLieu = new JTable(model);
                tblDuLieu.setRowHeight(30);
                tblDuLieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                tblDuLieu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
                tblDuLieu.getTableHeader().setBackground(NAVY);
                tblDuLieu.getTableHeader().setForeground(Color.WHITE);
                tblDuLieu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                tblDuLieu.setAutoCreateRowSorter(true);

                // ListSelectionListener: chọn dòng bằng chuột hoặc bàn phím đều đổ dữ liệu lên
                // form.
                // MouseListener: lấy dữ liệu khi nhấp chuột vào một ca trên JTable.
                tblDuLieu.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                                hienThiDuLieu();
                        }
                });
                // ListSelectionListener: vẫn cho phép chọn dòng bằng bàn phím.
                tblDuLieu.getSelectionModel().addListSelectionListener(e -> {
                        if (!e.getValueIsAdjusting())
                                hienThiDuLieu();
                });
                panel.add(new JScrollPane(tblDuLieu), BorderLayout.CENTER);
                return panel;
        }

        // Nạp danh sách ca làm từ DAO lên bảng.
        private void loadData() {
                model.setRowCount(0);
                for (CaLam ca : caLamDAO.getAll())
                        addRow(ca);
        }

        // Dùng lại một hàm thêm dòng cho cả nạp danh sách và tìm kiếm.
        private void addRow(CaLam ca) {
                model.addRow(new Object[] {
                                ca.getMaCa(), ca.getTenCa(),
                                dinhDangGio(ca.getGioBatDau()), dinhDangGio(ca.getGioKetThuc())
                });
        }

        // Khi chọn JTable, convertRowIndexToModel để lấy đúng dòng dù đã sắp xếp.
        private void hienThiDuLieu() {
                int selected = tblDuLieu.getSelectedRow();
                if (selected < 0)
                        return;
                int row = tblDuLieu.convertRowIndexToModel(selected);
                txtMaCa.setText(valueAt(row, 0));
                txtTenCa.setText(valueAt(row, 1));
                txtGioBatDau.setText(valueAt(row, 2));
                txtGioKetThuc.setText(valueAt(row, 3));
                txtMaCa.setEditable(false);
        }

        private String valueAt(int row, int col) {
                Object value = model.getValueAt(row, col);
                return value == null ? "" : value.toString();
        }

        // Thêm: không cho thêm khi đang chọn ca cũ; mã ca do DAO tự sinh.
        private void them() {
                if (tblDuLieu.getSelectedRow() >= 0) {
                        thongBao("Đang chọn ca cũ. Nhấn Làm mới để thêm ca mới.");
                        return;
                }
                if (!kiemTraDuLieu())
                        return;
                CaLam ca = getCaLamFromForm();
                try {
                        if (caLamDAO.them(ca)) {
                                thongBao("Thêm ca làm thành công! Mã ca: " + ca.getMaCa());
                                capNhatSauLuu();
                        } else {
                                thongBao("Không thể thêm ca làm.");
                        }
                } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Lỗi thêm ca làm: " + ex.getMessage(),
                                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                }
        }

        // Sửa: yêu cầu chọn dòng, kiểm tra form rồi gọi DAO cập nhật.
        private void sua() {
                if (!daChonCa("Vui lòng chọn ca cần sửa!"))
                        return;
                if (!kiemTraDuLieu())
                        return;
                if (caLamDAO.sua(getCaLamFromForm())) {
                        JOptionPane.showMessageDialog(this, "Cập nhật ca làm thành công!",
                                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        capNhatSauLuu();
                } else {
                        JOptionPane.showMessageDialog(this, "Cập nhật ca làm thất bại!",
                                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
        }

        // Xóa: người dùng phải chọn ca và xác nhận trước khi xóa.
        private void xoa() {
                if (!daChonCa("Vui lòng chọn ca cần xóa!"))
                        return;
                String maCa = txtMaCa.getText().trim();
                if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa ca " + maCa + "?",
                                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;
                if (caLamDAO.xoa(maCa)) {
                        JOptionPane.showMessageDialog(this, "Xóa ca làm thành công!",
                                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        capNhatSauLuu();
                } else {
                        JOptionPane.showMessageDialog(this, "Xóa ca làm thất bại!",
                                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
        }

        // Hai hàm dùng chung để giảm các đoạn kiểm tra/chạy lại bảng bị lặp.
        private boolean daChonCa(String message) {
                if (tblDuLieu.getSelectedRow() >= 0)
                        return true;
                JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
                return false;
        }

        private void capNhatSauLuu() {
                loadData();
                lamMoi();
        }

        private void thongBao(String message) {
                JOptionPane.showMessageDialog(this, message);
        }

        // Tìm kiếm theo mã, tên hoặc giờ; giữ nguyên tìm kiếm không phân biệt
        // hoa/thường.
        private void timKiem() {
                String keyword = txtTimKiem.getText().trim().toLowerCase();
                if (keyword.isEmpty()) {
                        loadData();
                        return;
                }
                model.setRowCount(0);
                for (CaLam ca : caLamDAO.getAll()) {
                        if (safe(ca.getMaCa()).contains(keyword)
                                        || safe(ca.getTenCa()).contains(keyword)
                                        || safe(ca.getGioBatDau()).contains(keyword)
                                        || safe(ca.getGioKetThuc()).contains(keyword)) {
                                addRow(ca);
                        }
                }
        }

        private String safe(String value) {
                return value == null ? "" : value.toLowerCase();
        }

        // Làm mới: xóa form, bỏ chọn dòng, gợi ý mã ca tự động.
        private void lamMoi() {
                txtMaCa.setText(MaTuDongDAO.goiY(MaTuDongDAO.Loai.CA_LAM));
                txtTenCa.setText("");
                txtGioBatDau.setValue(null);
                txtGioKetThuc.setValue(null);
                txtTimKiem.setText("");
                txtMaCa.setEditable(false);
                tblDuLieu.clearSelection();
        }

        // Validation: tên ca 3–50 ký tự; thời gian HH:mm hợp lệ và không trùng nhau.
        private boolean kiemTraDuLieu() {
                String ten = txtTenCa.getText().trim();
                String batDau = txtGioBatDau.getText().trim();
                String ketThuc = txtGioKetThuc.getText().trim();
                if (ten.length() < 3 || ten.length() > 50 || !ten.matches("[\\p{L}0-9 ]+")) {
                        return baoLoiTruong(txtTenCa, "Tên ca từ 3–50 ký tự, chỉ gồm chữ, số và khoảng trắng.");
                }
                if (!kiemTraGio(batDau, txtGioBatDau, "Giờ bắt đầu")
                                || !kiemTraGio(ketThuc, txtGioKetThuc, "Giờ kết thúc"))
                        return false;

                // Ca qua đêm (22:00–06:00) hợp lệ; chỉ cấm hai giờ bằng nhau.
                if (LocalTime.parse(batDau, FORMAT_GIO).equals(LocalTime.parse(ketThuc, FORMAT_GIO))) {
                        return baoLoiTruong(txtGioKetThuc,
                                        "Giờ bắt đầu và kết thúc không được trùng nhau.");
                }
                return true;
        }

        private boolean baoLoiTruong(JComponent field, String message) {
                JOptionPane.showMessageDialog(this, message,
                                "Dữ liệu ca làm không hợp lệ", JOptionPane.WARNING_MESSAGE);
                field.requestFocusInWindow();
                return false;
        }

        private boolean kiemTraGio(String value, JFormattedTextField field, String label) {
                if (!value.matches("\\d{2}:\\d{2}")) {
                        JOptionPane.showMessageDialog(this, label + " phải có định dạng HH:mm, ví dụ 06:00.",
                                        "Giờ không hợp lệ", JOptionPane.WARNING_MESSAGE);
                        field.requestFocusInWindow();
                        return false;
                }
                try {
                        LocalTime.parse(value, FORMAT_GIO);
                        return true;
                } catch (DateTimeParseException ex) {
                        JOptionPane.showMessageDialog(this,
                                        label + " không hợp lệ. Giờ phải từ 00 đến 23, phút từ 00 đến 59.",
                                        "Giờ không hợp lệ", JOptionPane.WARNING_MESSAGE);
                        field.requestFocusInWindow();
                        return false;
                }
        }

        // Entity: chuyển dữ liệu trên form thành đối tượng CaLam để gọi DAO.
        private CaLam getCaLamFromForm() {
                return new CaLam(txtMaCa.getText().trim(), txtTenCa.getText().trim(),
                                txtGioBatDau.getText().trim(), txtGioKetThuc.getText().trim());
        }

        // Chỉ hiển thị HH:mm, không làm thay đổi giá trị giờ lưu trong SQL Server.
        private String dinhDangGio(String gio) {
                if (gio == null)
                        return "";
                String text = gio.trim();
                return text.length() >= 5 ? text.substring(0, 5) : text;
        }

        // Một ActionListener chung để giữ luồng thao tác của sáu nút.
        @Override
        public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source == btnThem)
                        them();
                else if (source == btnSua)
                        sua();
                else if (source == btnXoa)
                        xoa();
                else if (source == btnLamMoi)
                        lamMoi();
                else if (source == btnTim)
                        timKiem();
                else if (source == btnTatCa)
                        loadData();
        }
}
