package UI;

import dao.TaiKhoanDAO;
import dao.MaTuDongDAO;
import java.sql.SQLException;
import entity.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

// Giao diện/chức năng quản lý tài khoản của ứng dụng.
public class QuanLyTaiKhoan extends JPanel implements ActionListener, MouseListener {

        // =========================================================
        // MAU
        // =========================================================

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

        // =========================================================
        // FORM
        // =========================================================

        // txtMaNV: Ô nhập/hiển thị văn bản mã nhân viên.
        private JTextField txtMaNV;
        // txtHoTen: Ô nhập/hiển thị văn bản họ tên.
        private JTextField txtHoTen;
        // txtSoDienThoai: Ô nhập/hiển thị văn bản số điện thoại.
        private JTextField txtSoDienThoai;
        // txtEmail: Ô nhập/hiển thị văn bản email.
        private JTextField txtEmail;
        // txtMatKhau: Ô nhập mật khẩu có che ký tự mật khẩu.
        private JPasswordField txtMatKhau;

        // cmbGioiTinh2: Hộp lựa chọn giới tính.
        private JComboBox<String> cmbGioiTinh2;
        // cmbVaiTro: Hộp lựa chọn vai trò.
        private JComboBox<String> cmbVaiTro;

        // txtTimKiem: Ô nhập/hiển thị văn bản tìm kiếm.
        private JTextField txtTimKiem;

        // Các nút xử lý chung tại actionPerformed().
        private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnTim, btnTatCa;

        // =========================================================
        // TABLE
        // =========================================================

        // tblDuLieu: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
        private JTable tblDuLieu;
        // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
        private DefaultTableModel model;

        // =========================================================
        // DAO
        // =========================================================

        // taiKhoanDAO: Đối tượng truy cập dữ liệu tài khoản.
        private TaiKhoanDAO taiKhoanDAO;

        // =========================================================
        // CONSTRUCTOR
        // =========================================================

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
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

        // Tạo tiêu đề.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel createTitle() {

                // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung = new JPanel(
                                new BorderLayout());

                pnlKhung.setBackground(
                                Color.WHITE);

                pnlKhung.setBorder(
                                new EmptyBorder(
                                                18,
                                                25,
                                                18,
                                                25));

                // lblTitle2: Nhãn hiển thị: QUẢN LÝ TÀI KHOẢN.
                JLabel lblTitle2 = new JLabel(
                                "QUẢN LÝ TÀI KHOẢN");

                lblTitle2.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                24));

                lblTitle2.setForeground(
                                NAVY);

                // lblSub: Nhãn hiển thị: Quản lý thông tin tài khoản nhân viên.
                JLabel lblSub = new JLabel(
                                "Quản lý thông tin tài khoản nhân viên");

                lblSub.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                13));

                lblSub.setForeground(
                                Color.GRAY);

                // pnlText: Khung chứa thành phần giao diện nội dung chữ.
                JPanel pnlText = new JPanel();

                pnlText.setOpaque(false);

                pnlText.setLayout(
                                new BoxLayout(
                                                pnlText,
                                                BoxLayout.Y_AXIS));

                pnlText.add(lblTitle2);

                pnlText.add(
                                Box.createVerticalStrut(5));

                pnlText.add(lblSub);

                pnlKhung.add(
                                pnlText,
                                BorderLayout.WEST);

                return pnlKhung;
        }

        // =========================================================
        // CENTER
        // =========================================================

        // Tạo trung tâm.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel createCenter() {

                // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung = new JPanel(
                                new BorderLayout(
                                                15,
                                                15));

                pnlKhung.setBackground(
                                BG);

                pnlKhung.setBorder(
                                new EmptyBorder(
                                                20,
                                                25,
                                                20,
                                                25));

                pnlKhung.add(
                                createForm(),
                                BorderLayout.NORTH);

                pnlKhung.add(
                                createTablePanel(),
                                BorderLayout.CENTER);

                return pnlKhung;
        }

        // =========================================================
        // FORM
        // =========================================================

        // Tạo biểu mẫu.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel createForm() {

                // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung = new JPanel(
                                new GridBagLayout());

                pnlKhung.setBackground(
                                Color.WHITE);

                pnlKhung.setBorder(
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

                // gbc: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng
                // GridBagLayout.
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

                pnlKhung.add(
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

                pnlKhung.add(
                                txtMaNV,
                                gbc);

                // -----------------------------------------------------
                // HO TEN
                // -----------------------------------------------------

                gbc.gridx = 2;
                gbc.weightx = 0;

                pnlKhung.add(
                                createLabel(
                                                "Họ tên:"),
                                gbc);

                txtHoTen = new JTextField();

                styleTextField(
                                txtHoTen);

                gbc.gridx = 3;
                gbc.weightx = 1;

                pnlKhung.add(
                                txtHoTen,
                                gbc);

                // -----------------------------------------------------
                // SO DIEN THOAI
                // -----------------------------------------------------

                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.weightx = 0;

                pnlKhung.add(
                                createLabel(
                                                "Số điện thoại:"),
                                gbc);

                txtSoDienThoai = new JTextField();

                styleTextField(
                                txtSoDienThoai);

                gbc.gridx = 1;
                gbc.weightx = 1;

                pnlKhung.add(
                                txtSoDienThoai,
                                gbc);

                // -----------------------------------------------------
                // EMAIL
                // -----------------------------------------------------

                gbc.gridx = 2;
                gbc.weightx = 0;

                pnlKhung.add(
                                createLabel(
                                                "Email:"),
                                gbc);

                txtEmail = new JTextField();

                styleTextField(
                                txtEmail);

                gbc.gridx = 3;
                gbc.weightx = 1;

                pnlKhung.add(
                                txtEmail,
                                gbc);

                // -----------------------------------------------------
                // MAT KHAU
                // -----------------------------------------------------

                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.weightx = 0;

                pnlKhung.add(
                                createLabel(
                                                "Mật khẩu:"),
                                gbc);

                txtMatKhau = new JPasswordField();

                styleTextField(
                                txtMatKhau);

                gbc.gridx = 1;
                gbc.weightx = 1;

                pnlKhung.add(
                                txtMatKhau,
                                gbc);

                // -----------------------------------------------------
                // GIOI TINH
                // -----------------------------------------------------

                gbc.gridx = 2;
                gbc.weightx = 0;

                pnlKhung.add(
                                createLabel(
                                                "Giới tính:"),
                                gbc);

                cmbGioiTinh2 = new JComboBox<>(
                                new String[] {
                                                "Nam",
                                                "Nu"
                                });

                styleComboBox(
                                cmbGioiTinh2);
                cmbGioiTinh2.setRenderer(new DefaultListCellRenderer() {
                        // Thiết lập thành phần dùng để hiển thị một mục trong danh sách.
                        // Tham số lstDanhSach: danh sách.
                        // Tham số value: giá trị.
                        // Tham số index: chỉ số phần tử.
                        // Tham số selected: trạng thái được chọn.
                        // Tham số focused: trạng thái có tiêu điểm.
                        // Kết quả: đối tượng/giá trị kiểu Component.
                        @Override
                        public Component getListCellRendererComponent(JList<?> lstDanhSach, Object value,
                                        int index, boolean selected, boolean focused) {
                                super.getListCellRendererComponent(lstDanhSach, value, index, selected, focused);
                                setText(hienThiGioiTinh(value == null ? "" : value.toString()));
                                return this;
                        }
                });

                gbc.gridx = 3;
                gbc.weightx = 1;

                pnlKhung.add(
                                cmbGioiTinh2,
                                gbc);

                // -----------------------------------------------------
                // VAI TRO
                // -----------------------------------------------------

                gbc.gridx = 0;
                gbc.gridy = 3;
                gbc.weightx = 0;

                pnlKhung.add(
                                createLabel(
                                                "Vai trò:"),
                                gbc);

                cmbVaiTro = new JComboBox<>(
                                new String[] {
                                                "Quan ly",
                                                "Thu ngan",
                                                "Nhan vien kho",
                                                "Nhan vien Spa"
                                });

                styleComboBox(
                                cmbVaiTro);
                cmbVaiTro.setRenderer(new DefaultListCellRenderer() {
                        // Thiết lập thành phần dùng để hiển thị một mục trong danh sách.
                        // Tham số lstDanhSach: danh sách.
                        // Tham số value: giá trị.
                        // Tham số index: chỉ số phần tử.
                        // Tham số selected: trạng thái được chọn.
                        // Tham số focused: trạng thái có tiêu điểm.
                        // Kết quả: đối tượng/giá trị kiểu Component.
                        @Override
                        public Component getListCellRendererComponent(JList<?> lstDanhSach, Object value,
                                        int index, boolean selected, boolean focused) {
                                super.getListCellRendererComponent(lstDanhSach, value, index, selected, focused);
                                setText(hienThiVaiTro(value == null ? "" : value.toString()));
                                return this;
                        }
                });

                gbc.gridx = 1;
                gbc.weightx = 1;

                pnlKhung.add(
                                cmbVaiTro,
                                gbc);

                // -----------------------------------------------------
                // BUTTON
                // -----------------------------------------------------

                // pnlButtons: Khung chứa thành phần giao diện các nút.
                JPanel pnlButtons = createButtonPanel();

                gbc.gridx = 2;
                gbc.gridy = 3;
                gbc.gridwidth = 2;
                gbc.weightx = 1;

                pnlKhung.add(
                                pnlButtons,
                                gbc);

                return pnlKhung;
        }

        // =========================================================
        // LABEL
        // =========================================================

        // Tạo nhãn.
        // Tham số pnlText: nội dung chữ.
        // Kết quả: đối tượng/giá trị kiểu JLabel.
        private JLabel createLabel(
                        String text) {

                // lblNhan2: Nhãn hiển thị nhãn.
                JLabel lblNhan2 = new JLabel(text);

                lblNhan2.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                12));

                lblNhan2.setForeground(
                                new Color(
                                                60,
                                                60,
                                                60));

                return lblNhan2;
        }

        // =========================================================
        // TEXT FIELD STYLE
        // =========================================================

        // Áp dụng kiểu hiển thị cho trường nhập liệu.
        // Tham số component: thành phần giao diện.
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

        // Áp dụng kiểu hiển thị cho hộp lựa chọn.
        // Tham số cmbLuaChon: hộp lựa chọn.
        private void styleComboBox(
                        JComboBox<String> cmbLuaChon) {

                cmbLuaChon.setPreferredSize(
                                new Dimension(
                                                220,
                                                34));

                cmbLuaChon.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                13));
        }

        // =========================================================
        // BUTTON PANEL
        // =========================================================

        // Tạo khung chứa các nút.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel createButtonPanel() {

                // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung = new JPanel(
                                new FlowLayout(
                                                FlowLayout.RIGHT,
                                                8,
                                                0));

                pnlKhung.setBackground(
                                Color.WHITE);

                // btnThem: Nút thao tác thêm.
                btnThem = createActionButton(
                                "Thêm",
                                GREEN);

                // btnSua: Nút thao tác sửa.
                btnSua = createActionButton(
                                "Sửa",
                                NAVY);

                // btnXoa: Nút thao tác xóa.
                btnXoa = createActionButton(
                                "Xóa",
                                RED);

                // btnLamMoi: Nút thao tác làm mới.
                btnLamMoi = createActionButton(
                                "Làm mới",
                                ORANGE);

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnThem.addActionListener(this);

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnSua.addActionListener(this);

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnXoa.addActionListener(this);

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnLamMoi.addActionListener(this);

                pnlKhung.add(btnThem);
                pnlKhung.add(btnSua);
                pnlKhung.add(btnXoa);
                pnlKhung.add(btnLamMoi);

                return pnlKhung;
        }

        // =========================================================
        // ACTION BUTTON
        // =========================================================

        // Tạo nút thao tác.
        // Tham số pnlText: nội dung chữ.
        // Tham số color: màu sắc.
        // Kết quả: đối tượng/giá trị kiểu JButton.
        private JButton createActionButton(
                        String text,
                        Color color) {

                // btnThaoTac: Nút thao tác nút thao tác.
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

        // =========================================================
        // TABLE PANEL
        // =========================================================

        // Tạo khung chứa bảng.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel createTablePanel() {

                // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung = new JPanel(
                                new BorderLayout(
                                                10,
                                                10));

                pnlKhung.setBackground(
                                Color.WHITE);

                pnlKhung.setBorder(
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

                // pnlTop2: Khung chứa thành phần giao diện trên.
                JPanel pnlTop2 = new JPanel(
                                new BorderLayout());

                pnlTop2.setBackground(
                                Color.WHITE);

                // lblTitle2: Nhãn hiển thị: DANH SÁCH TÀI KHOẢN.
                JLabel lblTitle2 = new JLabel(
                                "DANH SÁCH TÀI KHOẢN");

                lblTitle2.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                16));

                lblTitle2.setForeground(
                                NAVY);

                pnlTop2.add(
                                lblTitle2,
                                BorderLayout.WEST);

                // pnlSearch: Khung chứa thành phần giao diện tìm kiếm.
                JPanel pnlSearch = new JPanel(
                                new FlowLayout(
                                                FlowLayout.RIGHT,
                                                5,
                                                0));

                pnlSearch.setBackground(
                                Color.WHITE);

                // lblSearch: Nhãn hiển thị: Tìm kiếm:.
                JLabel lblSearch = new JLabel(
                                "Tìm kiếm:");

                txtTimKiem = new JTextField(18);

                txtTimKiem.setPreferredSize(
                                new Dimension(
                                                180,
                                                32));

                // btnTim: Nút thao tác tìm.
                btnTim = createSmallButton(
                                "Tìm",
                                NAVY);

                // btnTatCa: Nút thao tác toàn bộ dữ liệu.
                btnTatCa = createSmallButton(
                                "Tất cả",
                                new Color(
                                                100,
                                                110,
                                                120));

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnTim.addActionListener(this);

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnTatCa.addActionListener(this);

                pnlSearch.add(lblSearch);
                pnlSearch.add(txtTimKiem);
                pnlSearch.add(btnTim);
                pnlSearch.add(btnTatCa);

                pnlTop2.add(
                                pnlSearch,
                                BorderLayout.EAST);

                pnlKhung.add(
                                pnlTop2,
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

                        // Không cho phép sửa trực tiếp các ô trong bảng.
                        // Tham số row: chỉ số dòng.
                        // Tham số column: chỉ số cột.
                        // Kết quả: false, bảng chỉ dùng để xem.
                        @Override
                        public boolean isCellEditable(
                                        int row,
                                        int column) {

                                return false;
                        }
                };

                tblDuLieu = new JTable(model);

                tblDuLieu.setRowHeight(30);

                tblDuLieu.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                12));

                tblDuLieu.getTableHeader()
                                .setFont(
                                                new Font(
                                                                "Segoe UI",
                                                                Font.BOLD,
                                                                12));

                tblDuLieu.getTableHeader()
                                .setBackground(
                                                NAVY);

                tblDuLieu.getTableHeader()
                                .setForeground(
                                                Color.WHITE);

                tblDuLieu.setSelectionMode(
                                ListSelectionModel.SINGLE_SELECTION);

                tblDuLieu.addMouseListener(this); // Không thay đổi listener chọn dòng.

                tblDuLieu.setAutoCreateRowSorter(
                                true);

                tblDuLieu.getSelectionModel()
                                .addListSelectionListener(
                                                e -> {

                                                        if (!e.getValueIsAdjusting()) {
                                                                hienThiDuLieu();
                                                        }
                                                });

                // scrCuon2: Khung cuộn khung cuộn.
                JScrollPane scrCuon2 = new JScrollPane(tblDuLieu);

                pnlKhung.add(
                                scrCuon2,
                                BorderLayout.CENTER);

                return pnlKhung;
        }

        // =========================================================
        // SMALL BUTTON
        // =========================================================

        // Tạo nút nhỏ.
        // Tham số pnlText: nội dung chữ.
        // Tham số color: màu sắc.
        // Kết quả: đối tượng/giá trị kiểu JButton.
        private JButton createSmallButton(
                        String text,
                        Color color) {

                // btnThaoTac: Nút thao tác nút thao tác.
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

        // =========================================================
        // LOAD DATA
        // =========================================================

        // Đọc dữ liệu qua DAO và cập nhật bảng trên giao diện.
        private void loadData() {

                model.setRowCount(0);

                // lstDanhSach: Danh sách chứa các phần tử kiểu TaiKhoan.
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

        // Hiển thị thị dữ liệu.
        private void hienThiDuLieu() {

                int selectedRow = tblDuLieu.getSelectedRow();

                if (selectedRow < 0) {
                        return;
                }

                // row: Lưu chỉ số dòng.
                int row = tblDuLieu.convertRowIndexToModel(
                                selectedRow);

                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtMaNV.setText(
                                valueAt(row, 0));

                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtHoTen.setText(
                                valueAt(row, 1));

                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtSoDienThoai.setText(
                                valueAt(row, 2));

                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtEmail.setText(
                                valueAt(row, 3));

                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtMatKhau.setText(
                                valueAt(row, 4));

                cmbGioiTinh2.setSelectedItem(
                                maGioiTinh(valueAt(row, 5)));

                cmbVaiTro.setSelectedItem(
                                maVaiTro(valueAt(row, 6)));

                // Khong cho sua ma nhan vien
                txtMaNV.setEditable(false);
        }

        // =========================================================
        // VALUE TABLE
        // =========================================================

        // Đọc nội dung tại một ô của bảng và chuyển thành chuỗi.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: chuỗi kết quả.
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

        // Xử lý yêu cầu thêm dữ liệu từ giao diện.
        private void them() {
                if (tblDuLieu.getSelectedRow() >= 0) {
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

        // =========================================================
        // TIM KIEM
        // =========================================================

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

                // lstDanhSach: Danh sách chứa các phần tử kiểu TaiKhoan.
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

        // Thay null bằng chuỗi rỗng và chuyển chuỗi sang chữ thường để tìm kiếm.
        // Tham số value: giá trị.
        // Kết quả: chuỗi kết quả.
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

        // Làm mới dữ liệu hoặc trạng thái của màn hình.
        private void lamMoi() {

                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtMaNV.setText(MaTuDongDAO.goiY(MaTuDongDAO.Loai.NHAN_VIEN));
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtHoTen.setText("");
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtSoDienThoai.setText("");
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtEmail.setText("");
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtMatKhau.setText("");

                cmbGioiTinh2.setSelectedIndex(0);
                cmbVaiTro.setSelectedIndex(0);

                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtTimKiem.setText("");

                txtMaNV.setEditable(false);

                tblDuLieu.clearSelection();
        }

        // =========================================================
        // KIEM TRA DU LIEU
        // =========================================================

        // Kiểm tra các trường nhập trước khi thêm hoặc cập nhật dữ liệu.
        // Kết quả: true/false theo điều kiện xử lý.
        private boolean kiemTraDuLieu() {
                // hoTen: Đọc nội dung chữ từ ô nhập để xử lý họ tên.
                String hoTen = txtHoTen.getText().trim();
                // sdt: Đọc nội dung chữ từ ô nhập để xử lý số điện thoại.
                String sdt = txtSoDienThoai.getText().trim();
                // email: Đọc nội dung chữ từ ô nhập để xử lý email.
                String email = txtEmail.getText().trim();
                // matKhau: Lưu mật khẩu.
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
        // Tham số truong: trường nhập liệu.
        // Tham số thongBao: thống báo.
        // Kết quả: true/false theo điều kiện xử lý.
        private boolean baoLoiTruong(JComponent truong, String thongBao) {
                JOptionPane.showMessageDialog(this, thongBao,
                                "Dữ liệu nhân viên không hợp lệ", JOptionPane.WARNING_MESSAGE);
                truong.requestFocusInWindow();
                return false;
        }

        // =========================================================
        // FORM -> ENTITY
        // =========================================================

        // Tạo đối tượng tài khoản từ dữ liệu trên biểu mẫu.
        // Kết quả: đối tượng/giá trị kiểu TaiKhoan.
        private TaiKhoan getTaiKhoanFromForm() {

                return new TaiKhoan(
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
        // Hiển thị thị giới tính.
        // Tham số s: chuỗi đầu vào.
        // Kết quả: chuỗi kết quả.
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
        // Tham số s: chuỗi đầu vào.
        // Kết quả: chuỗi kết quả.
        private String maGioiTinh(String s) {
                return switch (hienThiGioiTinh(s)) {
                        case "Nữ" -> "Nu";
                        default -> "Nam";
                };
        }

        // Hiển thị thị vai trò.
        // Tham số s: chuỗi đầu vào.
        // Kết quả: chuỗi kết quả.
        private String hienThiVaiTro(String s) {
                if (s == null)
                        return "";
                // vaiTro: Lưu vai trò.
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
        // Tham số s: chuỗi đầu vào.
        // Kết quả: chuỗi kết quả.
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

        // MouseListener: để sẵn các điểm xử lý chuột, không thay thế
        // ListSelectionListener
        // (chọn dòng JTable bằng bàn phím vẫn hoạt động như project gốc).
        @Override
        public void mouseClicked(MouseEvent e) {
                // Không gọi lại hàm chọn dòng để tránh xử lý trùng sự kiện.
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

}
