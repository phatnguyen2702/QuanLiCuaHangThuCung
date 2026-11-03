package UI;

import dao.NhanVienDAO;
import dao.MaTuDongDAO;
import java.sql.SQLException;
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

        // NAVY: Màu dùng cho giao diện: xanh đậm.
        private final Color NAVY = new Color(18, 48, 82);
        // BG: Màu dùng cho giao diện: nền.
        private final Color BG = new Color(245, 247, 250);
        // GREEN: Màu dùng cho giao diện: xanh lá.
        private final Color GREEN = new Color(72, 196, 62);
        // RED: Màu dùng cho giao diện: đỏ.
        private final Color RED = new Color(210, 70, 70);
        // ORANGE: Màu dùng cho giao diện: cam.
        private final Color ORANGE = new Color(230, 150, 50);

        private JTextField txtMaNV;
        private JTextField txtHoTen;
        private JTextField txtSoDienThoai;
        private JTextField txtEmail;
        private JPasswordField txtMatKhau;

        // cmbGioiTinh2: Hộp lựa chọn giới tính.
        private JComboBox<String> cmbGioiTinh2;
        // cmbVaiTro: Hộp lựa chọn vai trò.
        private JComboBox<String> cmbVaiTro;

        private JTextField txtTimKiem;

        // Các nút xử lý chung tại actionPerformed().
        private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnTim, btnTatCa;

        // tblDuLieu: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
        private JTable tblDuLieu;
        private DefaultTableModel model;

        private NhanVienDAO taiKhoanDAO;

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        public QuanLyNhanVienUI() {

                taiKhoanDAO = new NhanVienDAO();

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

                // JComboBox: lưu các giá trị CSDL không dấu, renderer chỉ đổi nội dung hiển
                // thị.
                cmbGioiTinh2 = new JComboBox<>(new String[] { "Nam", "Nu" });
                cmbVaiTro = new JComboBox<>(new String[] {
                                "Quan ly", "Thu ngan", "Nhan vien kho", "Nhan vien Spa" });
                cmbGioiTinh2.setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(
                                        JList<?> list, Object value, int index, boolean selected, boolean focused) {
                                super.getListCellRendererComponent(list, value, index, selected, focused);
                                setText(hienThiGioiTinh(value == null ? "" : value.toString()));
                                return this;
                        }
                });
                cmbVaiTro.setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(
                                        JList<?> list, Object value, int index, boolean selected, boolean focused) {
                                super.getListCellRendererComponent(list, value, index, selected, focused);
                                setText(hienThiVaiTro(value == null ? "" : value.toString()));
                                return this;
                        }
                });
                // Mỗi hàng có hai cặp: nhãn + ô nhập; giữ đúng bố cục 4 cột của bản gốc.
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

        // GridBagLayout: đặt một nhãn ở cột 0/2 và ô nhập ngay bên phải (cột 1/3).
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

        private JButton createActionButton(
                        String text,
                        Color color) {

                JButton btnThaoTac = new JButton(text);

                btnThaoTac.setBackground(
                                color);

                btnThaoTac.setForeground(
                                Color.WHITE);

                btnThaoTac.setFont(
                                new Font(
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
        // TABLE PANEL

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
                // MouseListener: chọn nhân viên bằng chuột; chọn bằng bàn phím vẫn được hỗ trợ.
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

        private JButton createSmallButton(
                        String text,
                        Color color) {

                JButton btnThaoTac = new JButton(text);

                btnThaoTac.setBackground(
                                color);

                btnThaoTac.setForeground(
                                Color.WHITE);

                btnThaoTac.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                11));

                btnThaoTac.setFocusPainted(false);
                btnThaoTac.setBorderPainted(false);

                btnThaoTac.setPreferredSize(
                                new Dimension(
                                                65,
                                                32));

                return btnThaoTac;
        }

        // Đọc dữ liệu qua DAO và cập nhật bảng trên giao diện.
        private void loadData() {

                model.setRowCount(0);

                // lstDanhSach: Danh sách chứa các phần tử kiểu NhanVien.
                ArrayList<NhanVien> list = taiKhoanDAO.getAll();

                for (NhanVien tk : list) {

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
        // HIEN THI DU LIEU

        private void hienThiDuLieu() {

                int selectedRow = tblDuLieu.getSelectedRow();

                if (selectedRow < 0) {
                        return;
                }

                int row = tblDuLieu.convertRowIndexToModel(
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

                cmbGioiTinh2.setSelectedItem(
                                maGioiTinh(valueAt(row, 5)));

                cmbVaiTro.setSelectedItem(
                                maVaiTro(valueAt(row, 6)));

                // Khong cho sua ma nhan vien
                txtMaNV.setEditable(false);
        }
        // VALUE TABLE

        // Đọc nội dung tại một ô của bảng và chuyển thành chuỗi.
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

        // Xử lý yêu cầu thêm dữ liệu từ giao diện.
        private void them() {
                if (tblDuLieu.getSelectedRow() >= 0) {
                        JOptionPane.showMessageDialog(this,
                                        "Đang chọn nhân viên cũ. Nhấn Làm mới để thêm nhân viên mới.");
                        return;
                }
                if (!kiemTraDuLieu())
                        return;
                NhanVien tk = getTaiKhoanFromForm();
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

        // Xử lý yêu cầu sửa bản ghi đang chọn.
        private void sua() {

                if (!kiemTraDuLieu()) {
                        return;
                }

                if (tblDuLieu.getSelectedRow() < 0) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui lòng chọn tài khoản cần sửa!",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);

                        return;
                }

                NhanVien tk = getTaiKhoanFromForm();

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

        // Xử lý yêu cầu xóa bản ghi đang chọn.
        private void xoa() {

                if (tblDuLieu.getSelectedRow() < 0) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui lòng chọn tài khoản cần xóa!",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);

                        return;
                }

                // ma: Đọc nội dung chữ từ ô nhập để xử lý mã.
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

        // Tìm kiểm.
        private void timKiem() {

                // keyword: Đọc nội dung chữ từ ô nhập để xử lý từ khóa tìm kiếm.
                String keyword = txtTimKiem.getText()
                                .trim()
                                .toLowerCase();

                if (keyword.isEmpty()) {

                        loadData();

                        return;
                }

                model.setRowCount(0);

                // lstDanhSach: Danh sách chứa các phần tử kiểu NhanVien.
                ArrayList<NhanVien> list = taiKhoanDAO.getAll();

                for (NhanVien tk : list) {

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

        // Thay null bằng chuỗi rỗng và chuyển chuỗi sang chữ thường để tìm kiếm.
        private String safe(
                        String value) {

                if (value == null) {
                        return "";
                }

                return value.toLowerCase();
        }

        // Làm mới dữ liệu hoặc trạng thái của màn hình.
        private void lamMoi() {

                txtMaNV.setText(MaTuDongDAO.goiY(MaTuDongDAO.Loai.NHAN_VIEN));
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
        // KIEM TRA DU LIEU

        // Kiểm tra các trường nhập trước khi thêm hoặc cập nhật dữ liệu.
        private boolean kiemTraDuLieu() {
                // hoTen: Đọc nội dung chữ từ ô nhập để xử lý họ tên.
                String hoTen = txtHoTen.getText().trim();
                // sdt: Đọc nội dung chữ từ ô nhập để xử lý số điện thoại.
                String sdt = txtSoDienThoai.getText().trim();
                // email: Đọc nội dung chữ từ ô nhập để xử lý email.
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
                if (cmbGioiTinh2.getSelectedItem() == null
                                || !("Nam".equals(cmbGioiTinh2.getSelectedItem())
                                                || "Nu".equals(cmbGioiTinh2.getSelectedItem()))
                                || cmbVaiTro.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(this, "Giới tính chỉ được chọn Nam/Nữ và phải chọn vai trò.",
                                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                        return false;
                }
                return true;
        }

        // Thông báo lỗi và đưa tiêu điểm về trường nhập cần sửa.
        private boolean baoLoiTruong(JComponent truong, String thongBao) {
                JOptionPane.showMessageDialog(this, thongBao,
                                "Dữ liệu nhân viên không hợp lệ", JOptionPane.WARNING_MESSAGE);
                truong.requestFocusInWindow();
                return false;
        }

        private NhanVien getTaiKhoanFromForm() {

                return new NhanVien(
                                txtMaNV.getText().trim(),
                                txtHoTen.getText().trim(),
                                txtSoDienThoai.getText().trim(),
                                txtEmail.getText().trim(),
                                new String(
                                                txtMatKhau.getPassword()),
                                cmbGioiTinh2.getSelectedItem()
                                                .toString(),
                                cmbVaiTro.getSelectedItem()
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

        // Chuyển nội dung giới tính trên giao diện sang mã lưu trữ.
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

        // Chuyển nội dung vai trò trên giao diện sang mã lưu trữ.
        private String maVaiTro(String s) {
                return switch (hienThiVaiTro(s)) {
                        case "Quản lý" -> "Quan ly";
                        case "Thu ngân" -> "Thu ngan";
                        case "Nhân viên kho" -> "Nhan vien kho";
                        case "Nhân viên Spa" -> "Nhan vien Spa";
                        default -> s;
                };
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
