package UI;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class QuanLyTaiKhoan extends JPanel {

    private JTextField txtMaNV;
    private JTextField txtHoTen;
    private JPasswordField txtMatKhau;
    private JComboBox<String> cboVaiTro;
    private JCheckBox chkTrangThai;

    private JTextField txtTimKiem;

    private JTable table;
    private DefaultTableModel model;

    private TaiKhoanDAO taiKhoanDAO;

    public QuanLyTaiKhoan() {

        taiKhoanDAO = new TaiKhoanDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(240, 244, 248));

        add(createTitle(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);

        loadData();
    }

    // ==============================
    // TIÊU ĐỀ
    // ==============================

    private JPanel createTitle() {

        JPanel panel = new JPanel(
                new BorderLayout()
        );

        panel.setBackground(Color.WHITE);
        panel.setBorder(
                new EmptyBorder(
                        18, 25, 18, 25
                )
        );

        JLabel title = new JLabel(
                "QUẢN LÝ TÀI KHOẢN"
        );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        24
                )
        );

        title.setForeground(
                new Color(18, 48, 82)
        );

        JLabel sub = new JLabel(
                "Quản lý tài khoản nhân viên và phân quyền hệ thống"
        );

        sub.setForeground(Color.GRAY);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(
                new BoxLayout(
                        text,
                        BoxLayout.Y_AXIS
                )
        );

        text.add(title);
        text.add(Box.createVerticalStrut(5));
        text.add(sub);

        panel.add(
                text,
                BorderLayout.WEST
        );

        return panel;
    }

    // ==============================
    // CENTER
    // ==============================

    private JPanel createCenter() {

        JPanel panel = new JPanel(
                new BorderLayout(15, 15)
        );

        panel.setBackground(
                new Color(240, 244, 248)
        );

        panel.setBorder(
                new EmptyBorder(
                        20, 25, 20, 25
                )
        );

        panel.add(
                createForm(),
                BorderLayout.NORTH
        );

        panel.add(
                createTablePanel(),
                BorderLayout.CENTER
        );

        return panel;
    }

    // ==============================
    // FORM
    // ==============================

    private JPanel createForm() {

        JPanel panel =
                new JPanel(
                        new GridBagLayout()
                );

        panel.setBackground(
                Color.WHITE
        );

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Thong tin tai khoan"
                )
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(
                        8,
                        10,
                        8,
                        10
                );

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        // =====================================
        // MA NHAN VIEN
        // =====================================

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        panel.add(
                new JLabel("Ma nhan vien:"),
                gbc
        );

        txtMaNV =
                new JTextField();

        txtMaNV.setPreferredSize(
                new Dimension(
                        220,
                        32
                )
        );

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(
                txtMaNV,
                gbc
        );

        // =====================================
        // HO TEN
        // =====================================

        gbc.gridx = 2;
        gbc.weightx = 0;

        panel.add(
                new JLabel("Ho ten:"),
                gbc
        );

        txtHoTen =
                new JTextField();

        gbc.gridx = 3;
        gbc.weightx = 1;

        panel.add(
                txtHoTen,
                gbc
        );

        // =====================================
        // MAT KHAU
        // =====================================

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;

        panel.add(
                new JLabel("Mat khau:"),
                gbc
        );

        txtMatKhau =
                new JPasswordField();

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(
                txtMatKhau,
                gbc
        );

        // =====================================
        // VAI TRO
        // =====================================

        gbc.gridx = 2;
        gbc.weightx = 0;

        panel.add(
                new JLabel("Vai tro:"),
                gbc
        );

        cboVaiTro =
                new JComboBox<>(
                        new String[]{
                                "Quan ly",
                                "Thu ngan",
                                "Nhan vien kho",
                                "Nhan vien Spa"
                        }
                );

        gbc.gridx = 3;
        gbc.weightx = 1;

        panel.add(
                cboVaiTro,
                gbc
        );

        // =====================================
        // TRANG THAI
        // =====================================

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;

        panel.add(
                new JLabel("Trang thai:"),
                gbc
        );

        chkTrangThai =
                new JCheckBox(
                        "Dang hoat dong"
                );

        chkTrangThai.setSelected(
                true
        );

        chkTrangThai.setBackground(
                Color.WHITE
        );

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(
                chkTrangThai,
                gbc
        );

        // =====================================
        // BUTTON
        // =====================================

        JPanel buttons =
                createButtonPanel();

        gbc.gridx = 2;
        gbc.gridy = 2;

        gbc.gridwidth = 2;

        gbc.weightx = 1;

        panel.add(
                buttons,
                gbc
        );

        return panel;
    }

    // ==============================
    // BUTTON
    // ==============================

    private JPanel createButtonPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT
                )
        );

        panel.setBackground(Color.WHITE);

        JButton btnThem =
                new JButton("Thêm");

        JButton btnSua =
                new JButton("Sửa");

        JButton btnXoa =
                new JButton("Xóa");

        JButton btnLamMoi =
                new JButton("Làm mới");

        btnThem.addActionListener(
                e -> them()
        );

        btnSua.addActionListener(
                e -> sua()
        );

        btnXoa.addActionListener(
                e -> xoa()
        );

        btnLamMoi.addActionListener(
                e -> lamMoi()
        );

        panel.add(btnThem);
        panel.add(btnSua);
        panel.add(btnXoa);
        panel.add(btnLamMoi);

        return panel;
    }

    // ==============================
    // TABLE
    // ==============================

    private JPanel createTablePanel() {

        JPanel panel = new JPanel(
                new BorderLayout(10, 10)
        );

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Danh sách tài khoản"
                )
        );

        JPanel search =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        search.setBackground(Color.WHITE);

        search.add(
                new JLabel("Tìm kiếm:")
        );

        txtTimKiem =
                new JTextField(25);

        search.add(txtTimKiem);

        JButton btnTim =
                new JButton("Tìm");

        btnTim.addActionListener(
                e -> timKiem()
        );

        search.add(btnTim);

        JButton btnTatCa =
                new JButton("Tất cả");

        btnTatCa.addActionListener(
                e -> loadData()
        );

        search.add(btnTatCa);

        panel.add(
                search,
                BorderLayout.NORTH
        );

        model =
                new DefaultTableModel(
                        new String[]{
                                "Mã NV",
                                "Họ tên",
                                "Mật khẩu",
                                "Vai trò",
                                "Trạng thái"
                        },
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        table =
                new JTable(model);

        table.setRowHeight(30);
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.getSelectionModel()
                .addListSelectionListener(
                        e -> hienThiDuLieu()
                );

        JScrollPane scroll =
                new JScrollPane(table);

        panel.add(
                scroll,
                BorderLayout.CENTER
        );

        return panel;
    }

    // ==============================
    // LOAD DATA
    // ==============================

    private void loadData() {

        model.setRowCount(0);

        ArrayList<TaiKhoan> list =
                taiKhoanDAO.getAll();

        for (TaiKhoan tk : list) {

            model.addRow(
                    new Object[]{
                            tk.getMaNhanVien(),
                            tk.getHoTen(),
                            tk.getMatKhau(),
                            tk.getVaiTro(),
                            tk.isTrangThai()
                                    ? "Đang hoạt động"
                                    : "Khóa"
                    }
            );
        }
    }

    // ==============================
    // HIỂN THỊ DỮ LIỆU
    // ==============================

    private void hienThiDuLieu() {

        int row =
                table.getSelectedRow();

        if (row < 0) {
            return;
        }

        txtMaNV.setText(
                model.getValueAt(
                        row, 0
                ).toString()
        );

        txtHoTen.setText(
                model.getValueAt(
                        row, 1
                ).toString()
        );

        txtMatKhau.setText(
                model.getValueAt(
                        row, 2
                ).toString()
        );

        cboVaiTro.setSelectedItem(
                model.getValueAt(
                        row, 3
                ).toString()
        );

        String trangThai =
                model.getValueAt(
                        row, 4
                ).toString();

        chkTrangThai.setSelected(
                trangThai.equals(
                        "Đang hoạt động"
                )
        );

        txtMaNV.setEnabled(false);
    }

    // ==============================
    // THÊM
    // ==============================

    private void them() {

        if (!kiemTraDuLieu()) {
            return;
        }

        TaiKhoan tk =
                getTaiKhoanFromForm();

        if (taiKhoanDAO.them(tk)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Thêm tài khoản thành công!"
            );

            loadData();
            lamMoi();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Thêm tài khoản thất bại!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ==============================
    // SỬA
    // ==============================

    private void sua() {

        if (txtMaNV.getText()
                .trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn tài khoản cần sửa!"
            );

            return;
        }

        if (!kiemTraDuLieu()) {
            return;
        }

        TaiKhoan tk =
                getTaiKhoanFromForm();

        if (taiKhoanDAO.sua(tk)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Cập nhật thành công!"
            );

            loadData();
            lamMoi();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Cập nhật thất bại!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ==============================
    // XÓA
    // ==============================

    private void xoa() {

        String ma =
                txtMaNV.getText().trim();

        if (ma.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn tài khoản cần xóa!"
            );

            return;
        }

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        "Bạn có chắc muốn xóa tài khoản "
                                + ma + "?",
                        "Xác nhận",
                        JOptionPane.YES_NO_OPTION
                );

        if (result ==
                JOptionPane.YES_OPTION) {

            if (taiKhoanDAO.xoa(ma)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Xóa thành công!"
                );

                loadData();
                lamMoi();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Không thể xóa tài khoản!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // ==============================
    // TÌM KIẾM
    // ==============================

    private void timKiem() {

        String keyword =
                txtTimKiem.getText()
                        .trim()
                        .toLowerCase();

        model.setRowCount(0);

        ArrayList<TaiKhoan> list =
                taiKhoanDAO.getAll();

        for (TaiKhoan tk : list) {

            if (
                    tk.getMaNhanVien()
                            .toLowerCase()
                            .contains(keyword)
                    ||
                    tk.getHoTen()
                            .toLowerCase()
                            .contains(keyword)
                    ||
                    tk.getVaiTro()
                            .toLowerCase()
                            .contains(keyword)
            ) {

                model.addRow(
                        new Object[]{
                                tk.getMaNhanVien(),
                                tk.getHoTen(),
                                tk.getMatKhau(),
                                tk.getVaiTro(),
                                tk.isTrangThai()
                                        ? "Đang hoạt động"
                                        : "Khóa"
                        }
                );
            }
        }
    }

    // ==============================
    // LÀM MỚI
    // ==============================

    private void lamMoi() {

        txtMaNV.setText("");
        txtHoTen.setText("");
        txtMatKhau.setText("");

        cboVaiTro.setSelectedIndex(0);

        chkTrangThai.setSelected(true);

        txtMaNV.setEnabled(true);

        table.clearSelection();

        txtTimKiem.setText("");
    }

    // ==============================
    // KIỂM TRA
    // ==============================

    private boolean kiemTraDuLieu() {

        if (txtMaNV.getText()
                .trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập mã nhân viên!"
            );

            return false;
        }

        if (txtHoTen.getText()
                .trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập họ tên!"
            );

            return false;
        }

        if (txtMatKhau.getPassword()
                .length == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập mật khẩu!"
            );

            return false;
        }

        return true;
    }

    // ==============================
    // LẤY DATA TỪ FORM
    // ==============================

    private TaiKhoan getTaiKhoanFromForm() {

        return new TaiKhoan(
                txtMaNV.getText().trim(),
                txtHoTen.getText().trim(),
                new String(
                        txtMatKhau.getPassword()
                ),
                cboVaiTro.getSelectedItem()
                        .toString(),
                chkTrangThai.isSelected()
        );
    }
}