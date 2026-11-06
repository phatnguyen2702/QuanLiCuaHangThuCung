package UI;

import control.CaLamControl;

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

public class QuanLyCaLamUI extends JPanel implements ActionListener {

        private static final Color NAVY = new Color(18, 48, 82);
        private static final Color BG = new Color(245, 247, 250);
        private static final Color GREEN = new Color(72, 196, 62);
        private static final Color RED = new Color(210, 70, 70);
        private static final Color ORANGE = new Color(230, 150, 50);
        private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);

        private JTextField txtMaCa, txtTenCa, txtTimKiem;

        private JFormattedTextField txtGioBatDau, txtGioKetThuc;

        private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnTim, btnTatCa;

        private JTable tblDuLieu;
        private DefaultTableModel model;

        private final CaLamControl control = new CaLamControl();

        public QuanLyCaLamUI() {

                setLayout(new BorderLayout());
                setBackground(BG);
                add(createTitle(), BorderLayout.NORTH);
                add(createCenter(), BorderLayout.CENTER);
                loadData();
                lamMoi();
        }

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

        private JPanel createCenter() {
                JPanel content = new JPanel(new BorderLayout(15, 15));
                content.setBackground(BG);
                content.setBorder(new EmptyBorder(20, 25, 20, 25));
                content.add(createForm(), BorderLayout.NORTH);
                content.add(createTablePanel(), BorderLayout.CENTER);
                return content;
        }

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

        private JTextField createTextField() {
                JTextField field = new JTextField();
                styleTextField(field);
                return field;
        }

        private void styleTextField(JTextField field) {
                field.setPreferredSize(new Dimension(220, 34));
                field.setFont(FONT);
        }

        private JFormattedTextField taoOChonGio() {
                try {
                        MaskFormatter mask = new MaskFormatter("##:##");
                        mask.setPlaceholderCharacter('_');
                        JFormattedTextField field = new JFormattedTextField(mask);
                        field.setFocusLostBehavior(JFormattedTextField.PERSIST);
                        field.setToolTipText("Giờ theo HH:mm, trong khung 06:00 - 22:00. Ví dụ: 06:00");
                        styleTextField(field);
                        return field;
                } catch (ParseException ex) {
                        throw new IllegalStateException("Không tạo được ô nhập giờ", ex);
                }
        }

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

        private JPanel createWhitePanel(LayoutManager layout, int padding) {
                JPanel panel = new JPanel(layout);
                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                                new EmptyBorder(padding, padding, padding, padding)));
                return panel;
        }

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

                tblDuLieu.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                                hienThiDuLieu();
                        }
                });

                tblDuLieu.getSelectionModel().addListSelectionListener(e -> {
                        if (!e.getValueIsAdjusting())
                                hienThiDuLieu();
                });
                panel.add(new JScrollPane(tblDuLieu), BorderLayout.CENTER);
                return panel;
        }

        private void loadData() {
                model.setRowCount(0);
                for (CaLam ca : control.layDanhSach())
                        addRow(ca);
        }

        private void addRow(CaLam ca) {
                model.addRow(new Object[] {
                                ca.getMaCa(), ca.getTenCa(),
                                dinhDangGio(ca.getGioBatDau()), dinhDangGio(ca.getGioKetThuc())
                });
        }

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

        private void them() {
                if (tblDuLieu.getSelectedRow() >= 0) {
                        thongBao("Đang chọn ca cũ. Nhấn Làm mới để thêm ca mới.");
                        return;
                }
                if (!kiemTraDuLieu())
                        return;
                CaLam ca = getCaLamFromForm();
                try {
                        if (control.them(ca)) {
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

        private void sua() {
                if (!daChonCa("Vui lòng chọn ca cần sửa!"))
                        return;
                if (!kiemTraDuLieu())
                        return;
                if (control.sua(getCaLamFromForm())) {
                        JOptionPane.showMessageDialog(this, "Cập nhật ca làm thành công!",
                                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        capNhatSauLuu();
                } else {
                        JOptionPane.showMessageDialog(this, "Cập nhật ca làm thất bại!",
                                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
        }

        private void xoa() {
                if (!daChonCa("Vui lòng chọn ca cần xóa!"))
                        return;
                String maCa = txtMaCa.getText().trim();
                if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa ca " + maCa + "?",
                                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;
                if (control.xoa(maCa)) {
                        JOptionPane.showMessageDialog(this, "Xóa ca làm thành công!",
                                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        capNhatSauLuu();
                } else {
                        JOptionPane.showMessageDialog(this, "Xóa ca làm thất bại!",
                                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
        }

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

        private void timKiem() {
                model.setRowCount(0);
                for (CaLam ca : control.timKiem(txtTimKiem.getText()))
                        addRow(ca);
        }

        private void lamMoi() {
                txtMaCa.setText(control.goiYMa());
                txtTenCa.setText("");
                txtGioBatDau.setValue(null);
                txtGioKetThuc.setValue(null);
                txtTimKiem.setText("");
                txtMaCa.setEditable(false);
                tblDuLieu.clearSelection();
        }

        private boolean kiemTraDuLieu() {
                try {
                        control.taoCaLam(txtMaCa.getText(), txtTenCa.getText(),
                                        txtGioBatDau.getText(), txtGioKetThuc.getText());
                        return true;
                } catch (IllegalArgumentException ex) {
                        String msg = ex.getMessage();
                        JComponent focus = msg.startsWith("Tên") ? txtTenCa
                                        : msg.startsWith("Giờ bắt đầu") ? txtGioBatDau : txtGioKetThuc;
                        return baoLoiTruong(focus, msg);
                }
        }

        private boolean baoLoiTruong(JComponent field, String message) {
                JOptionPane.showMessageDialog(this, message,
                                "Dữ liệu ca làm không hợp lệ", JOptionPane.WARNING_MESSAGE);
                field.requestFocusInWindow();
                return false;
        }

        private CaLam getCaLamFromForm() {
                return control.taoCaLam(txtMaCa.getText(), txtTenCa.getText(),
                                txtGioBatDau.getText(), txtGioKetThuc.getText());
        }

        private String dinhDangGio(LocalTime gio) {
                return CaLamControl.dinhDangGio(gio);
        }

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
