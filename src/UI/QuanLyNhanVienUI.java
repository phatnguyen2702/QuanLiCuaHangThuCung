package UI;

import control.NhanVienControl;

import entity.NhanVien;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;

public class QuanLyNhanVienUI extends JPanel implements ActionListener {

        private final Color NAVY = new Color(18, 48, 82);
        private final Color BG = new Color(245, 247, 250);
        private final Color GREEN = new Color(72, 196, 62);
        private final Color RED = new Color(210, 70, 70);
        private final Color ORANGE = new Color(230, 150, 50);
        private JTextField txtMaNV;
        private JTextField txtHoTen;
        private JTextField txtSoDienThoai;
        private JTextField txtEmail;
        private JPasswordField txtMatKhau;

        private JComboBox<String> cmbGioiTinh2;
        private JComboBox<String> cmbVaiTro;
        private JTextField txtTimKiem;

        private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnTim, btnTatCa;

        private JTable tblDuLieu;
        private DefaultTableModel model;

        private final NhanVienControl nhanVienControl = new NhanVienControl();

        public QuanLyNhanVienUI() {
                setLayout(new BorderLayout());
                setBackground(BG);
                add(createTitle(), BorderLayout.NORTH);
                add(createCenter(), BorderLayout.CENTER);
                loadData();
                lamMoi();
        }

        private JPanel createTitle() {
                JPanel title = new JPanel(new BorderLayout());
                title.setBackground(Color.WHITE);
                title.setBorder(new EmptyBorder(18, 25, 18, 25));
                JPanel lines = new JPanel();
                lines.setOpaque(false);
                lines.setLayout(new BoxLayout(lines, BoxLayout.Y_AXIS));
                JLabel heading = new JLabel("QUẢN LÝ TÀI KHOẢN");
                heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
                heading.setForeground(NAVY);
                JLabel sub = new JLabel("Quản lý thông tin tài khoản nhân viên");
                sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                sub.setForeground(Color.GRAY);
                lines.add(heading);
                lines.add(Box.createVerticalStrut(5));
                lines.add(sub);
                title.add(lines, BorderLayout.WEST);
                return title;
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
                JPanel form = new JPanel(new GridBagLayout());
                form.setBackground(Color.WHITE);
                form.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                                new EmptyBorder(15, 15, 15, 15)));
                txtMaNV = new JTextField();
                txtMaNV.setEditable(false);
                txtMaNV.setToolTipText("Mã NV được hệ thống tự sinh khi lưu");
                txtHoTen = new JTextField();
                txtSoDienThoai = new JTextField();
                txtEmail = new JTextField();
                txtMatKhau = new JPasswordField();

                cmbGioiTinh2 = new JComboBox<>(new String[] { "Nam", "Nữ" });
                cmbVaiTro = new JComboBox<>(new String[] {
                                "Quan ly", "Thu ngan", "Nhan vien kho", "Nhan vien Spa" });
                cmbVaiTro.setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(
                                        JList<?> list, Object value, int index, boolean selected, boolean focused) {
                                super.getListCellRendererComponent(list, value, index, selected, focused);
                                setText(nhanVienControl.hienThiVaiTro(value == null ? "" : value.toString()));
                                return this;
                        }
                });
                themTruong(form, "Mã nhân viên:", txtMaNV, 0, 0);
                themTruong(form, "Họ tên:", txtHoTen, 0, 2);
                themTruong(form, "Số điện thoại:", txtSoDienThoai, 1, 0);
                themTruong(form, "Email:", txtEmail, 1, 2);
                themTruong(form, "Mật khẩu:", txtMatKhau, 2, 0);
                themTruong(form, "Giới tính:", cmbGioiTinh2, 2, 2);
                themTruong(form, "Vai trò:", cmbVaiTro, 3, 0);
                GridBagConstraints g = new GridBagConstraints();
                g.gridx = 2;
                g.gridy = 3;
                g.gridwidth = 2;
                g.weightx = 1;
                g.fill = GridBagConstraints.HORIZONTAL;
                g.insets = new Insets(6, 8, 6, 8);
                form.add(createButtonPanel(), g);
                return form;
        }

        private void themTruong(JPanel form, String ten, JComponent input, int hang, int cot) {
                input.setPreferredSize(new Dimension(220, 34));
                input.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                GridBagConstraints g = new GridBagConstraints();
                g.insets = new Insets(6, 8, 6, 8);
                g.fill = GridBagConstraints.HORIZONTAL;
                g.gridx = cot;
                g.gridy = hang;
                g.weightx = 0;
                form.add(createLabel(ten), g);
                g = new GridBagConstraints();
                g.insets = new Insets(6, 8, 6, 8);
                g.fill = GridBagConstraints.HORIZONTAL;
                g.gridx = cot + 1;
                g.gridy = hang;
                g.weightx = 1;
                form.add(input, g);
        }

        private JLabel createLabel(String text) {
                JLabel label = new JLabel(text);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setForeground(new Color(60, 60, 60));
                return label;
        }

        private JPanel createButtonPanel() {
                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                buttons.setBackground(Color.WHITE);
                btnThem = createActionButton("Thêm", GREEN);
                btnSua = createActionButton("Sửa", NAVY);
                btnXoa = createActionButton("Xóa", RED);
                btnLamMoi = createActionButton("Làm mới", ORANGE);
                for (JButton button : new JButton[] { btnThem, btnSua, btnXoa, btnLamMoi }) {
                        button.addActionListener(this);
                        buttons.add(button);
                }
                return buttons;
        }

        private JButton createActionButton(String text, Color color) {
                JButton btnThaoTac = new JButton(text);
                btnThaoTac.setBackground(color);
                btnThaoTac.setForeground(Color.WHITE);
                btnThaoTac.setFont(new Font(
                                "Segoe UI",
                                Font.BOLD,
                                12));
                btnThaoTac.setFocusPainted(false);
                btnThaoTac.setBorderPainted(false);
                btnThaoTac.setPreferredSize(
                                new Dimension(
                                                85,
                                                34));

                return btnThaoTac;
        }

        private JPanel createTablePanel() {
                JPanel panel = new JPanel(new BorderLayout(10, 10));
                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                                new EmptyBorder(10, 10, 10, 10)));
                JPanel top = new JPanel(new BorderLayout());
                top.setBackground(Color.WHITE);
                JLabel title = new JLabel("DANH SÁCH TÀI KHOẢN");
                title.setFont(new Font("Segoe UI", Font.BOLD, 16));
                title.setForeground(NAVY);
                top.add(title, BorderLayout.WEST);

                JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                search.setBackground(Color.WHITE);
                txtTimKiem = new JTextField(18);
                txtTimKiem.setPreferredSize(new Dimension(180, 32));
                btnTim = createSmallButton("Tìm", NAVY);
                btnTatCa = createSmallButton("Tất cả", new Color(100, 110, 120));
                for (JButton b : new JButton[] { btnTim, btnTatCa })
                        b.addActionListener(this);
                search.add(new JLabel("Tìm kiếm:"));
                search.add(txtTimKiem);
                search.add(btnTim);
                search.add(btnTatCa);
                top.add(search, BorderLayout.EAST);
                panel.add(top, BorderLayout.NORTH);

                model = new DefaultTableModel(new String[] {
                                "Mã NV", "Họ tên", "Số điện thoại", "Email", "Mật khẩu", "Giới tính", "Vai trò"
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

        private JButton createSmallButton(String text, Color color) {
                JButton btnThaoTac = new JButton(text);
                btnThaoTac.setBackground(color);
                btnThaoTac.setForeground(Color.WHITE);
                btnThaoTac.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                11));

                btnThaoTac.setFocusPainted(false);
                btnThaoTac.setBorderPainted(false);
                btnThaoTac.setPreferredSize(new Dimension(
                                65,
                                32));
                return btnThaoTac;
        }

        private void loadData() {
                model.setRowCount(0);
                hienThiNhanVien(nhanVienControl.layDanhSach());
        }

        private void hienThiDuLieu() {
                int selectedRow = tblDuLieu.getSelectedRow();
                if (selectedRow < 0) {
                        return;
                }
                int row = tblDuLieu.convertRowIndexToModel(selectedRow);
                txtMaNV.setText(valueAt(row, 0));
                txtHoTen.setText(valueAt(row, 1));
                txtSoDienThoai.setText(valueAt(row, 2));
                txtEmail.setText(valueAt(row, 3));
                txtMatKhau.setText(valueAt(row, 4));
                cmbGioiTinh2.setSelectedItem(valueAt(row, 5));
                cmbVaiTro.setSelectedItem(nhanVienControl.maVaiTro(valueAt(row, 6)));
                txtMaNV.setEditable(false);
        }

        private String valueAt(int row, int column) {
                Object value = model.getValueAt(row, column);
                return value == null ? "" : value.toString();
        }

        private void them() {
                if (tblDuLieu.getSelectedRow() >= 0) {
                        JOptionPane.showMessageDialog(this,
                                        "Đang chọn nhân viên cũ. Nhấn Làm mới để thêm nhân viên mới.");
                        return;
                }
                NhanVien tk = getTaiKhoanFromForm();
                NhanVienControl.KetQuaThaoTac ketQua = nhanVienControl.themNhanVien(tk);
                if (!ketQua.thanhCong() && !nhanVienControl.kiemTra(tk).hopLe()) {
                        hienThiLoiDuLieu();
                        return;
                }
                JOptionPane.showMessageDialog(this, ketQua.thongBao());
                if (ketQua.thanhCong()) {
                        loadData();
                        lamMoi();
                }
        }

        private void sua() {
                if (tblDuLieu.getSelectedRow() < 0) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui lòng chọn tài khoản cần sửa!",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);
                        return;
                }
                NhanVien tk = getTaiKhoanFromForm();
                NhanVienControl.KetQuaThaoTac ketQua = nhanVienControl.suaNhanVien(tk);
                if (!ketQua.thanhCong() && !nhanVienControl.kiemTra(tk).hopLe()) {
                        hienThiLoiDuLieu();
                        return;
                }
                JOptionPane.showMessageDialog(this, ketQua.thongBao());
                if (ketQua.thanhCong()) {
                        loadData();
                        lamMoi();
                }
        }

        private void xoa() {
                if (tblDuLieu.getSelectedRow() < 0) {
                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui lòng chọn tài khoản cần xóa!",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);

                        return;
                }
                String ma = txtMaNV.getText().trim();
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
                NhanVienControl.KetQuaThaoTac ketQua = nhanVienControl.xoaNhanVien(ma);
                JOptionPane.showMessageDialog(this, ketQua.thongBao());
                if (ketQua.thanhCong()) {
                        loadData();
                        lamMoi();
                }
        }

        private void timKiem() {
                String keyword = txtTimKiem.getText().trim().toLowerCase();
                if (keyword.isEmpty()) {
                        loadData();
                        return;
                }
                model.setRowCount(0);
                hienThiNhanVien(nhanVienControl.timKiem(keyword));
        }

        private void hienThiNhanVien(ArrayList<NhanVien> danhSach) {
                for (NhanVien nv : danhSach) {
                        model.addRow(new Object[] {
                                        nv.getMaNhanVien(),
                                        nv.getHoTen(),
                                        nhanVienControl.dinhDangSoDienThoai(nv.getSoDienThoai()),
                                        nv.getEmail(),
                                        nv.getMatKhau(),
                                        nv.getGioiTinh(),
                                        nhanVienControl.hienThiVaiTro(nv.getVaiTro())
                        });
                }
        }

        private void lamMoi() {
                txtMaNV.setText(nhanVienControl.duLieuLamMoi().maNhanVien());
                txtHoTen.setText("");
                txtSoDienThoai.setText("");
                txtEmail.setText("");
                txtMatKhau.setText("");
                cmbGioiTinh2.setSelectedIndex(0);
                cmbVaiTro.setSelectedIndex(0);
                txtTimKiem.setText("");
                txtMaNV.setEditable(false);
                tblDuLieu.clearSelection();
        }

        private void hienThiLoiDuLieu() {
                NhanVienControl.KetQuaKiemTra ketQua = nhanVienControl.kiemTra(getTaiKhoanFromForm());
                JOptionPane.showMessageDialog(this, ketQua.thongBao(),
                                "Dữ liệu nhân viên không hợp lệ", JOptionPane.WARNING_MESSAGE);
                layTruong(ketQua.truong()).requestFocusInWindow();
        }

        private JComponent layTruong(String tenTruong) {
                if ("hoTen".equals(tenTruong))
                        return txtHoTen;
                if ("soDienThoai".equals(tenTruong))
                        return txtSoDienThoai;
                if ("email".equals(tenTruong))
                        return txtEmail;
                if ("matKhau".equals(tenTruong))
                        return txtMatKhau;
                if ("gioiTinh".equals(tenTruong))
                        return cmbGioiTinh2;
                return cmbVaiTro;
        }

        private NhanVien getTaiKhoanFromForm() {

                return nhanVienControl.taoNhanVien(
                                txtMaNV.getText().trim(),
                                txtHoTen.getText().trim(),
                                txtSoDienThoai.getText().trim(),
                                txtEmail.getText().trim(),
                                new String(
                                                txtMatKhau.getPassword()),
                                cmbGioiTinh2.getSelectedItem() == null
                                                ? ""
                                                : cmbGioiTinh2.getSelectedItem().toString(),
                                cmbVaiTro.getSelectedItem() == null
                                                ? ""
                                                : cmbVaiTro.getSelectedItem().toString());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
                Object nguon = e.getSource();
                if (nguon == btnThem) {
                        them();
                } else if (nguon == btnSua) {
                        sua();
                } else if (nguon == btnXoa) {
                        xoa();
                } else if (nguon == btnLamMoi) {
                        lamMoi();
                } else if (nguon == btnTim) {
                        timKiem();
                } else if (nguon == btnTatCa) {
                        loadData();
                }
        }

}
