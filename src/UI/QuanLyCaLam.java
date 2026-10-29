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

// Giao diện/chức năng quản lý ca làm của ứng dụng.
public class QuanLyCaLam extends JPanel {

        // ==============================
        // MAU
        // ==============================

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
        // FORMAT_GIO: Bộ định dạng ngày/giờ theo mẫu HH:mm để chuyển giữa dữ liệu thời gian và chuỗi.
        private static final DateTimeFormatter FORMAT_GIO = DateTimeFormatter.ofPattern("HH:mm")
                        .withResolverStyle(ResolverStyle.STRICT);

        // ==============================
        // FORM
        // ==============================

        // txtMaCa: Ô nhập/hiển thị văn bản mã ca.
        private JTextField txtMaCa;
        // txtTenCa: Ô nhập/hiển thị văn bản tên ca.
        private JTextField txtTenCa;
        // txtGioBatDau: Ô nhập theo định dạng giờ bắt đầu.
        private JFormattedTextField txtGioBatDau;
        // txtGioKetThuc: Ô nhập theo định dạng giờ kết thúc.
        private JFormattedTextField txtGioKetThuc;

        // txtTimKiem: Ô nhập/hiển thị văn bản tìm kiếm.
        private JTextField txtTimKiem;

        // ==============================
        // TABLE
        // ==============================

        // tblDuLieu: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
        private JTable tblDuLieu;
        // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
        private DefaultTableModel model;

        // ==============================
        // DAO
        // ==============================

        // caLamDAO: Đối tượng truy cập dữ liệu ca làm.
        private CaLamDAO caLamDAO;

        // ==============================
        // CONSTRUCTOR
        // ==============================

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
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

        // Tạo tiêu đề.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel createTitle() {

                // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung = new JPanel(
                                new BorderLayout());

                pnlKhung.setBackground(Color.WHITE);

                pnlKhung.setBorder(
                                new EmptyBorder(
                                                18,
                                                25,
                                                18,
                                                25));

                // lblTitle2: Nhãn hiển thị: QUẢN LÝ CA LÀM.
                JLabel lblTitle2 = new JLabel(
                                "QUẢN LÝ CA LÀM");

                lblTitle2.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                24));

                lblTitle2.setForeground(NAVY);

                // lblSub: Nhãn hiển thị: Quản lý thông tin ca làm việc.
                JLabel lblSub = new JLabel(
                                "Quản lý thông tin ca làm việc");

                lblSub.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                13));

                lblSub.setForeground(Color.GRAY);

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

        // ==============================
        // CENTER
        // ==============================

        // Tạo trung tâm.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel createCenter() {

                // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung = new JPanel(
                                new BorderLayout(
                                                15,
                                                15));

                pnlKhung.setBackground(BG);

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

        // ==============================
        // FORM
        // ==============================

        // Tạo biểu mẫu.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel createForm() {

                // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung = new JPanel(
                                new GridBagLayout());

                pnlKhung.setBackground(Color.WHITE);

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

                // gbc: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng GridBagLayout.
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

                pnlKhung.add(
                                createLabel("Mã ca:"),
                                gbc);

                txtMaCa = new JTextField();

                styleTextField(txtMaCa);
                txtMaCa.setEditable(false);
                txtMaCa.setToolTipText("Mã CA được hệ thống tự sinh khi lưu");

                gbc.gridx = 1;
                gbc.weightx = 1;

                pnlKhung.add(
                                txtMaCa,
                                gbc);

                // ==============================
                // TEN CA
                // ==============================

                gbc.gridx = 2;
                gbc.weightx = 0;

                pnlKhung.add(
                                createLabel("Tên ca:"),
                                gbc);

                txtTenCa = new JTextField();

                styleTextField(txtTenCa);

                gbc.gridx = 3;
                gbc.weightx = 1;

                pnlKhung.add(
                                txtTenCa,
                                gbc);

                // ==============================
                // GIO BAT DAU
                // ==============================

                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.weightx = 0;

                pnlKhung.add(
                                createLabel("Giờ bắt đầu:"),
                                gbc);

                txtGioBatDau = taoOChonGio();

                gbc.gridx = 1;
                gbc.weightx = 1;

                pnlKhung.add(
                                txtGioBatDau,
                                gbc);

                // ==============================
                // GIO KET THUC
                // ==============================

                gbc.gridx = 2;
                gbc.weightx = 0;

                pnlKhung.add(
                                createLabel("Giờ kết thúc:"),
                                gbc);

                txtGioKetThuc = taoOChonGio();

                gbc.gridx = 3;
                gbc.weightx = 1;

                pnlKhung.add(
                                txtGioKetThuc,
                                gbc);

                // ==============================
                // BUTTON
                // ==============================

                // pnlButtons: Khung chứa thành phần giao diện các nút.
                JPanel pnlButtons = createButtonPanel();

                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.gridwidth = 4;
                gbc.weightx = 1;

                pnlKhung.add(
                                pnlButtons,
                                gbc);

                return pnlKhung;
        }

        // ==============================
        // LABEL
        // ==============================

        // Tạo nhãn.
        // Tham số pnlText: nội dung chữ.
        // Kết quả: đối tượng/giá trị kiểu JLabel.
        private JLabel createLabel(String text) {

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

        // ==============================
        // TEXT FIELD
        // ==============================

        // Áp dụng kiểu hiển thị cho trường nhập liệu.
        // Tham số txtTextField: ô nhập văn bản.
        private void styleTextField(
                        JTextField txtTextField) {

                txtTextField.setPreferredSize(
                                new Dimension(
                                                220,
                                                34));

                txtTextField.setFont(
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
                        // txtGio: Ô nhập theo định dạng giờ.
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

        // Tạo khung chứa các nút.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel createButtonPanel() {

                // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung = new JPanel(
                                new FlowLayout(
                                                FlowLayout.RIGHT,
                                                8,
                                                0));

                pnlKhung.setBackground(Color.WHITE);

                // btnThem: Nút thao tác thêm.
                JButton btnThem = createActionButton(
                                "Thêm",
                                GREEN);

                // btnSua: Nút thao tác sửa.
                JButton btnSua = createActionButton(
                                "Sửa",
                                NAVY);

                // btnXoa: Nút thao tác xóa.
                JButton btnXoa = createActionButton(
                                "Xóa",
                                RED);

                // btnLamMoi: Nút thao tác làm mới.
                JButton btnLamMoi = createActionButton(
                                "Làm mới",
                                ORANGE);

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnThem.addActionListener(
                                e -> them());

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnSua.addActionListener(
                                e -> sua());

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnXoa.addActionListener(
                                e -> xoa());

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnLamMoi.addActionListener(
                                e -> lamMoi());

                pnlKhung.add(btnThem);
                pnlKhung.add(btnSua);
                pnlKhung.add(btnXoa);
                pnlKhung.add(btnLamMoi);

                return pnlKhung;
        }

        // ==============================
        // ACTION BUTTON
        // ==============================

        // Tạo nút thao tác.
        // Tham số pnlText: nội dung chữ.
        // Tham số color: màu sắc.
        // Kết quả: đối tượng/giá trị kiểu JButton.
        private JButton createActionButton(
                        String text,
                        Color color) {

                // btnThaoTac: Nút thao tác nút thao tác.
                JButton btnThaoTac = new JButton(text);

                btnThaoTac.setBackground(color);

                btnThaoTac.setForeground(Color.WHITE);

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

        // ==============================
        // TABLE
        // ==============================

        // Tạo khung chứa bảng.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel createTablePanel() {

                // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung = new JPanel(
                                new BorderLayout(
                                                10,
                                                10));

                pnlKhung.setBackground(Color.WHITE);

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

                // ==============================
                // TOP
                // ==============================

                // pnlTop2: Khung chứa thành phần giao diện trên.
                JPanel pnlTop2 = new JPanel(
                                new BorderLayout());

                pnlTop2.setBackground(Color.WHITE);

                // lblTitle2: Nhãn hiển thị: DANH SÁCH CA LÀM.
                JLabel lblTitle2 = new JLabel(
                                "DANH SÁCH CA LÀM");

                lblTitle2.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                16));

                lblTitle2.setForeground(NAVY);

                pnlTop2.add(
                                lblTitle2,
                                BorderLayout.WEST);

                // ==============================
                // SEARCH
                // ==============================

                // pnlSearch: Khung chứa thành phần giao diện tìm kiếm.
                JPanel pnlSearch = new JPanel(
                                new FlowLayout(
                                                FlowLayout.RIGHT,
                                                5,
                                                0));

                pnlSearch.setBackground(Color.WHITE);

                // lblSearch: Nhãn hiển thị: Tìm kiếm:.
                JLabel lblSearch = new JLabel("Tìm kiếm:");

                txtTimKiem = new JTextField(18);

                txtTimKiem.setPreferredSize(
                                new Dimension(
                                                180,
                                                32));

                // btnTim: Nút thao tác tìm.
                JButton btnTim = createSmallButton(
                                "Tìm",
                                NAVY);

                // btnTatCa: Nút thao tác toàn bộ dữ liệu.
                JButton btnTatCa = createSmallButton(
                                "Tất cả",
                                new Color(
                                                100,
                                                110,
                                                120));

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnTim.addActionListener(
                                e -> timKiem());

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnTatCa.addActionListener(
                                e -> loadData());

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
                                .setBackground(NAVY);

                tblDuLieu.getTableHeader()
                                .setForeground(Color.WHITE);

                tblDuLieu.setSelectionMode(
                                ListSelectionModel.SINGLE_SELECTION);

                tblDuLieu.setAutoCreateRowSorter(true);

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

        // ==============================
        // SMALL BUTTON
        // ==============================

        // Tạo nút nhỏ.
        // Tham số pnlText: nội dung chữ.
        // Tham số color: màu sắc.
        // Kết quả: đối tượng/giá trị kiểu JButton.
        private JButton createSmallButton(
                        String text,
                        Color color) {

                // btnThaoTac: Nút thao tác nút thao tác.
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

                btnThaoTac.setPreferredSize(
                                new Dimension(
                                                65,
                                                32));

                return btnThaoTac;
        }

        // ==============================
        // LOAD DATA
        // ==============================

        // Đọc dữ liệu qua DAO và cập nhật bảng trên giao diện.
        private void loadData() {

                model.setRowCount(0);

                // list: Danh sách chứa các phần tử kiểu CaLam.
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
                txtMaCa.setText(
                                valueAt(row, 0));

                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtTenCa.setText(
                                valueAt(row, 1));

                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtGioBatDau.setText(
                                valueAt(row, 2));

                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtGioKetThuc.setText(
                                valueAt(row, 3));

                txtMaCa.setEditable(false);
        }

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

        // ==============================
        // THEM
        // ==============================

        // Xử lý yêu cầu thêm dữ liệu từ giao diện.
        private void them() {
                if (tblDuLieu.getSelectedRow() >= 0) {
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

        // Xử lý yêu cầu sửa bản ghi đang chọn.
        private void sua() {

                if (tblDuLieu.getSelectedRow() < 0) {

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

        // Xử lý yêu cầu xóa bản ghi đang chọn.
        private void xoa() {

                if (tblDuLieu.getSelectedRow() < 0) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Vui lòng chọn ca cần xóa!",
                                        "Thông báo",
                                        JOptionPane.WARNING_MESSAGE);

                        return;
                }

                // maCa: Đọc nội dung chữ từ ô nhập để xử lý mã ca.
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

                // list: Danh sách chứa các phần tử kiểu CaLam.
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

        // Thay null bằng chuỗi rỗng và chuyển chuỗi sang chữ thường để tìm kiếm.
        // Tham số value: giá trị.
        // Kết quả: chuỗi kết quả.
        private String safe(String value) {

                if (value == null) {
                        return "";
                }

                return value.toLowerCase();
        }

        // ==============================
        // LAM MOI
        // ==============================

        // Làm mới dữ liệu hoặc trạng thái của màn hình.
        private void lamMoi() {

                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtMaCa.setText(MaTuDongDAO.goiY(MaTuDongDAO.Loai.CA_LAM));
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtTenCa.setText("");
                txtGioBatDau.setValue(null);
                txtGioKetThuc.setValue(null);

                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtTimKiem.setText("");

                txtMaCa.setEditable(false);

                tblDuLieu.clearSelection();
        }

        // ==============================
        // KIEM TRA
        // ==============================

        // Kiểm tra các trường nhập trước khi thêm hoặc cập nhật dữ liệu.
        // Kết quả: true/false theo điều kiện xử lý.
        private boolean kiemTraDuLieu() {
                // tenCa: Đọc nội dung chữ từ ô nhập để xử lý tên ca.
                String tenCa = txtTenCa.getText().trim();
                // gioBatDau: Đọc nội dung chữ từ ô nhập để xử lý giờ bắt đầu.
                String gioBatDau = txtGioBatDau.getText().trim();
                // gioKetThuc: Đọc nội dung chữ từ ô nhập để xử lý giờ kết thúc.
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

        // Thông báo lỗi và đưa tiêu điểm về trường nhập cần sửa.
        // Tham số truong: trường nhập liệu.
        // Tham số thongBao: thống báo.
        // Kết quả: true/false theo điều kiện xử lý.
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

        // Tạo đối tượng ca làm từ dữ liệu trên biểu mẫu.
        // Kết quả: đối tượng/giá trị kiểu CaLam.
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
                // s: Lưu chuỗi đầu vào.
                String s = gio.trim();
                return s.length() >= 5 ? s.substring(0, 5) : s;
        }

}