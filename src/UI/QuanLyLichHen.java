package UI;

import dao.LichHenDAO;
import entity.LichHen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.ResolverStyle;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

// Giao diện/chức năng quản lý lịch hẹn của ứng dụng.
public class QuanLyLichHen extends JPanel {

    // NAVY: Màu dùng cho giao diện: xanh đậm.
    private final Color NAVY = new Color(18, 48, 82);
    // LIGHT_BLUE: Màu dùng cho giao diện: xanh dương.
    private final Color LIGHT_BLUE = new Color(229, 241, 252);
    // BLUE: Màu dùng cho giao diện: xanh dương.
    private final Color BLUE = new Color(24, 144, 255);
    // DARK_BLUE: Màu dùng cho giao diện: xanh dương.
    private final Color DARK_BLUE = new Color(11, 43, 64);
    // GRAY_BTN: Màu dùng cho giao diện: nút.
    private final Color GRAY_BTN = new Color(136, 142, 148);
    // STATUS_GREEN: Màu dùng cho giao diện: xanh lá.
    private final Color STATUS_GREEN = new Color(115, 214, 137);
    // STATUS_GRAY: Màu dùng cho giao diện: xám.
    private final Color STATUS_GRAY = new Color(200, 200, 200);
    // STATUS_RED: Màu dùng cho giao diện: đỏ.
    private final Color STATUS_RED = new Color(235, 138, 138);

    // txtNgay: Ô nhập/hiển thị văn bản ngày.
    // txtTimKiem: Ô nhập/hiển thị văn bản tìm kiếm.
    private JTextField txtNgay, txtTimKiem;
    // cmbNhanVien: Hộp lựa chọn nhân viên.
    // cmbTrangThai: Hộp lựa chọn trạng thái.
    private JComboBox<String> cmbNhanVien, cmbTrangThai;
    // tblDuLieu: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
    private JTable tblDuLieu;
    // tableModel: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private DefaultTableModel tableModel;
    // dao: Đối tượng truy cập dữ liệu lịch hẹn.
    private LichHenDAO dao;

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    public QuanLyLichHen() {
        dao = new LichHenDAO();
        setLayout(new BorderLayout());
        setBackground(LIGHT_BLUE);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadComboBox();
        loadData();
    }

    // Tạo khung phía trên.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createTopPanel() {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new BorderLayout(0, 15));
        pnlKhung.setBackground(LIGHT_BLUE);
        pnlKhung.setBorder(new EmptyBorder(20, 25, 10, 25));

        // pnlTitleRow: Khung chứa thành phần giao diện tiêu đề dòng.
        JPanel pnlTitleRow = new JPanel(new BorderLayout());
        pnlTitleRow.setOpaque(false);

        // lblTitle: Nhãn hiển thị: QUẢN LÝ LỊCH HẸN SPA.
        JLabel lblTitle = new JLabel("QUẢN LÝ LỊCH HẸN SPA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(NAVY);
        pnlTitleRow.add(lblTitle, BorderLayout.WEST);

        // btnDatLich: Nút thao tác: Đặt Lịch Mới.
        JButton btnDatLich = new JButton("Đặt Lịch Mới");
        btnDatLich.setBackground(BLUE);
        btnDatLich.setForeground(Color.WHITE);
        btnDatLich.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDatLich.setFocusPainted(false);
        btnDatLich.setBorderPainted(false);
        btnDatLich.setPreferredSize(new Dimension(140, 38));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnDatLich.addActionListener(e -> hienFormDatLich(null));
        pnlTitleRow.add(btnDatLich, BorderLayout.EAST);

        pnlKhung.add(pnlTitleRow, BorderLayout.NORTH);

        // pnlFilterRow: Khung chứa thành phần giao diện bộ lọc dòng.
        JPanel pnlFilterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlFilterRow.setBackground(Color.WHITE);
        pnlFilterRow.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));

        pnlFilterRow.add(createFilterLabel("Ngày"));
        txtNgay = new JTextField(8);
        txtNgay.setEditable(false);
        txtNgay.setFocusable(false);
        txtNgay.setToolTipText("Chỉ chọn ngày bằng nút Lịch; nút x để bỏ lọc ngày");
        pnlFilterRow.add(txtNgay);

        // btnLich: Nút thao tác: Lịch.
        JButton btnLich = new JButton("Lịch");
        btnLich.setBackground(NAVY);
        btnLich.setForeground(Color.WHITE);
        btnLich.setFocusPainted(false);
        btnLich.setPreferredSize(new Dimension(60, 25));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLich.addActionListener(e -> {
            DatePickerDialog winPicker = new DatePickerDialog((JFrame) SwingUtilities.getWindowAncestor(this));
            winPicker.setLocationRelativeTo(btnLich);
            winPicker.setVisible(true);
            if (winPicker.getSelectedDate() != null) {
                // Định dạng dữ liệu thành chuỗi rồi hiển thị trong control bằng setText().
                txtNgay.setText(winPicker.getSelectedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                locDuLieu();
            }
        });
        pnlFilterRow.add(btnLich);
        // btnXoaNgay: Nút thao tác: Xóa ngày.
        JButton btnXoaNgay = new JButton("Xóa ngày");
        btnXoaNgay.setBackground(new Color(108, 117, 125));
        btnXoaNgay.setForeground(Color.WHITE);
        btnXoaNgay.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnXoaNgay.setFocusPainted(false);
        btnXoaNgay.setPreferredSize(new Dimension(85, 30));
        btnXoaNgay.setToolTipText("Bỏ lọc ngày và hiển thị lịch của tất cả các ngày");

        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnXoaNgay.addActionListener(e -> {
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtNgay.setText("");
            locDuLieu();
        });

        pnlFilterRow.add(btnXoaNgay);

        pnlFilterRow.add(Box.createHorizontalStrut(5));

        pnlFilterRow.add(createFilterLabel("Nhân viên"));
        cmbNhanVien = new JComboBox<>();
        cmbNhanVien.setPreferredSize(new Dimension(140, 30));
        pnlFilterRow.add(cmbNhanVien);

        pnlFilterRow.add(createFilterLabel("Trạng thái"));
        cmbTrangThai = new JComboBox<>(new String[] { "Tất cả", "Xác nhận", "Đã hủy" });
        cmbTrangThai.setPreferredSize(new Dimension(110, 30));
        pnlFilterRow.add(cmbTrangThai);

        pnlFilterRow.add(createFilterLabel("Tìm kiếm"));
        txtTimKiem = new JTextField(12);
        txtTimKiem.setToolTipText("Mã lịch, tên KH, thú cưng...");
        pnlFilterRow.add(txtTimKiem);

        // btnTimKiem: Nút thao tác: Lọc.
        JButton btnTimKiem = new JButton("Lọc");
        btnTimKiem.setBackground(STATUS_GREEN);
        btnTimKiem.setForeground(Color.WHITE);
        btnTimKiem.setFocusPainted(false);
        pnlFilterRow.add(btnTimKiem);

        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTimKiem.addActionListener(e -> locDuLieu());
        // btnLamMoi: Nút thao tác: Làm mới.
        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setBackground(new Color(226, 233, 241));
        btnLamMoi.setFocusPainted(false);
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLamMoi.addActionListener(e -> {
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtNgay.setText("");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtTimKiem.setText("");
            cmbNhanVien.setSelectedIndex(0);
            cmbTrangThai.setSelectedIndex(0);
            loadComboBox();
            locDuLieu();
        });
        pnlFilterRow.add(btnLamMoi);
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        txtTimKiem.addActionListener(e -> locDuLieu());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        txtNgay.addActionListener(e -> locDuLieu());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbNhanVien.addActionListener(e -> locDuLieu());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbTrangThai.addActionListener(e -> locDuLieu());

        pnlKhung.add(pnlFilterRow, BorderLayout.CENTER);
        return pnlKhung;
    }

    // Tạo nhãn bộ lọc.
    // Tham số text: nội dung chữ.
    // Kết quả: đối tượng/giá trị kiểu JLabel.
    private JLabel createFilterLabel(String text) {
        // lblLbl: Nhãn hiển thị nhãn.
        JLabel lblLbl = new JLabel(text);
        lblLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return lblLbl;
    }

    // Tạo khung chứa bảng.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createTablePanel() {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new BorderLayout());
        pnlKhung.setBackground(LIGHT_BLUE);
        pnlKhung.setBorder(new EmptyBorder(0, 25, 20, 25));

        // Đã xóa hoàn toàn cột Ghi Chú
        // columns: Mảng lưu các cột.
        String[] columns = { "Mã Lịch", "Ngày & Giờ", "Tên Khách Hàng", "Tên Thú Cưng", "Nhân Viên Spa", "Trạng Thái",
                "Thao Tác" };

        tableModel = new DefaultTableModel(columns, 0) {
            // Quy định ô của bảng có được chỉnh sửa trực tiếp hay không.
            // Tham số row: chỉ số dòng.
            // Tham số column: chỉ số cột.
            // Kết quả: true/false theo điều kiện xử lý.
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Cột Thao tác lùi về index 6
            }
        };

        tblDuLieu = new JTable(tableModel);
        tblDuLieu.setRowHeight(45);
        tblDuLieu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblDuLieu.getTableHeader().setBackground(Color.WHITE);
        tblDuLieu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDuLieu.getTableHeader().setPreferredSize(new Dimension(100, 40));

        // Canh chỉnh lại độ rộng cột cho đẹp
        tblDuLieu.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblDuLieu.getColumnModel().getColumn(1).setPreferredWidth(140);
        tblDuLieu.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblDuLieu.getColumnModel().getColumn(3).setPreferredWidth(150);
        tblDuLieu.getColumnModel().getColumn(4).setPreferredWidth(130);
        tblDuLieu.getColumnModel().getColumn(5).setPreferredWidth(100);
        tblDuLieu.getColumnModel().getColumn(6).setPreferredWidth(130);

        DefaultTableCellRenderer lblCenter = new DefaultTableCellRenderer();
        lblCenter.setHorizontalAlignment(SwingConstants.CENTER);
        tblDuLieu.getColumnModel().getColumn(0).setCellRenderer(lblCenter);
        tblDuLieu.getColumnModel().getColumn(1).setCellRenderer(lblCenter);
        tblDuLieu.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        tblDuLieu.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());
        tblDuLieu.getColumnModel().getColumn(6).setCellEditor(new ActionButtonEditor());

        // scrCuon2: Khung cuộn khung cuộn.
        JScrollPane scrCuon2 = new JScrollPane(tblDuLieu);
        scrCuon2.getViewport().setBackground(Color.WHITE);
        pnlKhung.add(scrCuon2, BorderLayout.CENTER);
        return pnlKhung;
    }

    // Nạp hộp lựa chọn khung.
    private void loadComboBox() {
        cmbNhanVien.removeAllItems();
        cmbNhanVien.addItem("Tất cả");
        for (String[] nv : dao.getDanhSachNhanVienSpa()) {
            cmbNhanVien.addItem(nv[1]);
        }
        cmbNhanVien.setSelectedItem("Tất cả");
    }

    // Đọc dữ liệu qua DAO và cập nhật bảng trên giao diện.
    public void loadData() {
        locDuLieu();
    }

    // Lọc dữ liệu.
    private void locDuLieu() {
        // Khi JComboBox đang tải lại, lựa chọn có thể tạm thời là null.
        if (cmbNhanVien == null || cmbTrangThai == null || tableModel == null)
            return;

        // ngayStr: Đọc nội dung chữ từ ô nhập để xử lý ngày chuỗi.
        String ngayStr = txtNgay.getText().trim();
        Object nvDaChon = cmbNhanVien.getSelectedItem();
        Object trangThaiDaChon = cmbTrangThai.getSelectedItem();
        String nhanVienLoc = nvDaChon == null ? "Tất cả" : nvDaChon.toString();
        String trangThaiLoc = trangThaiDaChon == null ? "Tất cả" : trangThaiDaChon.toString();
        // tuKhoa: Đọc nội dung chữ từ ô nhập để xử lý từ khóa tìm kiếm.
        String tuKhoa = txtTimKiem.getText().trim().toLowerCase();

        // formatter: Bộ định dạng ngày/giờ theo mẫu dd/MM/yyyy để chuyển giữa dữ liệu thời gian và chuỗi.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        // ngayLoc: Lưu ngày lọc dưới dạng dữ liệu thời gian.
        LocalDate ngayLoc = null;

        try {
            if (!ngayStr.isEmpty())
                // Phân tích chuỗi ngày thành LocalDate theo bộ định dạng được chỉ định.
                ngayLoc = LocalDate.parse(ngayStr, formatter);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày lọc phải có định dạng dd/MM/yyyy", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);

        for (LichHen lh : dao.getAll()) {
            boolean isMatch = true;

            if (ngayLoc != null) {
                if (lh.getNgayGioHen() == null || !lh.getNgayGioHen().toLocalDate().equals(ngayLoc)) {
                    isMatch = false;
                }
            }

            if (!nhanVienLoc.equals("Tất cả")) {
                if (lh.getTenNhanVien() == null || !lh.getTenNhanVien().equals(nhanVienLoc)) {
                    isMatch = false;
                }
            }

            if (!trangThaiLoc.equals("Tất cả")) {
                if (lh.getTrangThai() == null || !lh.getTrangThai().equals(trangThaiLoc)) {
                    isMatch = false;
                }
            }

            if (!tuKhoa.isEmpty()) {
                String maLH = lh.getMaLichHen() != null ? lh.getMaLichHen().toLowerCase() : "";
                String tenKH = lh.getTenKhachHang() != null ? lh.getTenKhachHang().toLowerCase() : "";
                String tenTC = lh.getTenThuCung() != null ? lh.getTenThuCung().toLowerCase() : "";

                if (!maLH.contains(tuKhoa) && !tenKH.contains(tuKhoa) && !tenTC.contains(tuKhoa)) {
                    isMatch = false;
                }
            }

            if (isMatch) {
                tableModel.addRow(new Object[] {
                        lh.getMaLichHen(), lh.getNgayGioHenFormat(), lh.getTenKhachHang(),
                        lh.getTenThuCung(), lh.getTenNhanVien(), lh.getTrangThai(), ""
                });
            }
        }
    }

    // Trả về lịch hẹn theo mã của đối tượng.
    // Tham số maLichHen: mã lịch hẹn.
    private LichHen getLichHenByMa(String maLichHen) {
        for (LichHen lh : dao.getAll()) {
            if (lh.getMaLichHen().equals(maLichHen)) {
                return lh;
            }
        }
        return null;
    }

    // Xóa lịch hẹn.
    // Tham số maLichHen: mã lịch hẹn.
    private void xoaLichHen(String maLichHen) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hủy lịch hẹn " + maLichHen + "?\nLịch sẽ được giữ trong danh sách với trạng thái Đã hủy.", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.huy(maLichHen)) {
                JOptionPane.showMessageDialog(this, "Đã hủy lịch hẹn. Lịch sử vẫn được giữ lại.");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Không thể hủy lịch đã hủy hoặc đã có phiếu dịch vụ.",
                        "Không thể hủy", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // Hiển thị biểu mẫu đặt lịch.
    // Tham số lhEdit: lịch hẹn cần chỉnh sửa.
    private void hienFormDatLich(LichHen lhEdit) {
        if (lhEdit != null && "Đã hủy".equals(lhEdit.getTrangThai())) {
            JOptionPane.showMessageDialog(this,
                    "Lịch hẹn đã hủy không thể sửa. Hãy tạo lịch mới.",
                    "Lịch đã hủy", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // winParentFrame: Cửa sổ cha cửa sổ.
        JFrame winParentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        DatLichDialog winDialog = new DatLichDialog(winParentFrame, this, dao, lhEdit);
        winDialog.setVisible(true);
    }

    // Quy định cách hiển thị ô hoặc thành phần trạng thái ô.
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        // Thiết lập thành phần dùng để vẽ một ô của bảng.
        // Tham số tblDuLieu: bảng dữ liệu.
        // Tham số value: giá trị.
        // Tham số isSel: trạng thái được chọn.
        // Tham số hasFocus: trạng thái có tiêu điểm.
        // Tham số r: chỉ số dòng.
        // Tham số c: chỉ số cột.
        // Kết quả: đối tượng/giá trị kiểu Component.
        @Override
        public Component getTableCellRendererComponent(JTable tblDuLieu, Object value, boolean isSel, boolean hasFocus,
                int r, int c) {
            Component comp = super.getTableCellRendererComponent(tblDuLieu, value, isSel, hasFocus, r, c);
            setHorizontalAlignment(SwingConstants.CENTER);
            String st = value != null ? value.toString() : "";
            if (st.equals("Xác nhận"))
                comp.setBackground(STATUS_GREEN);
            else if (st.equals("Chờ xác nhận"))
                comp.setBackground(STATUS_GRAY);
            else if (st.equals("Đã hủy"))
                comp.setBackground(STATUS_RED);
            else
                comp.setBackground(Color.WHITE);
            return comp;
        }
    }

    // Quy định cách hiển thị ô hoặc thành phần nút thao tác.
    private class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        // btnSua: Nút thao tác sửa.
        // btnXoa: Nút thao tác xóa.
        private JButton btnSua, btnXoa;

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setOpaque(true);
            btnSua = new JButton("Sửa");
            btnSua.setBackground(NAVY);
            btnSua.setForeground(Color.WHITE);
            btnSua.setPreferredSize(new Dimension(60, 30));

            btnXoa = new JButton("Hủy");
            btnXoa.setBackground(new Color(220, 53, 69));
            btnXoa.setForeground(Color.WHITE);
            btnXoa.setPreferredSize(new Dimension(60, 30));

            add(btnSua);
            add(btnXoa);
        }

        // Thiết lập thành phần dùng để vẽ một ô của bảng.
        // Tham số tblDuLieu: bảng dữ liệu.
        // Tham số value: giá trị.
        // Tham số isSelected: trạng thái được chọn.
        // Tham số hasFocus: trạng thái có tiêu điểm.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: đối tượng/giá trị kiểu Component.
        @Override
        public Component getTableCellRendererComponent(JTable tblDuLieu, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            setBackground(isSelected ? tblDuLieu.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    // Bộ chỉnh sửa và xử lý tương tác cho ô của bảng.
    private class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        private JPanel pnlKhung;
        // currentMaLich: Lưu mã lịch hẹn hiện tại.
        private String currentMaLich;

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        public ActionButtonEditor() {
            pnlKhung = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            pnlKhung.setBackground(Color.WHITE);

            // btnSua: Nút thao tác: Sửa.
            JButton btnSua = new JButton("Sửa");
            btnSua.setBackground(NAVY);
            btnSua.setForeground(Color.WHITE);
            btnSua.setPreferredSize(new Dimension(60, 30));
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnSua.addActionListener(e -> {
                fireEditingStopped();
                LichHen lhEdit = getLichHenByMa(currentMaLich);
                if (lhEdit != null) {
                    hienFormDatLich(lhEdit);
                }
            });

            // btnXoa: Nút thao tác: Hủy.
            JButton btnXoa = new JButton("Hủy");
            btnXoa.setBackground(new Color(220, 53, 69));
            btnXoa.setForeground(Color.WHITE);
            btnXoa.setPreferredSize(new Dimension(60, 30));
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnXoa.addActionListener(e -> {
                fireEditingStopped();
                xoaLichHen(currentMaLich);
            });

            pnlKhung.add(btnSua);
            pnlKhung.add(btnXoa);
        }

        // Chuẩn bị thành phần chỉnh sửa hoặc nút thao tác trong ô của bảng.
        // Tham số tblDuLieu: bảng dữ liệu.
        // Tham số value: giá trị.
        // Tham số isSelected: trạng thái được chọn.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: đối tượng/giá trị kiểu Component.
        @Override
        public Component getTableCellEditorComponent(JTable tblDuLieu, Object value, boolean isSelected, int row,
                int column) {
            currentMaLich = tblDuLieu.getValueAt(row, 0).toString();
            pnlKhung.setBackground(tblDuLieu.getSelectionBackground());
            return pnlKhung;
        }

        // Trả về giá trị của bộ chỉnh sửa ô hiện tại.
        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    // Hộp thoại nhập hoặc chỉnh sửa ngày bộ chọn.
    public static class DatePickerDialog extends JDialog {
        // selectedDate: Lưu ngày đã chọn dưới dạng dữ liệu thời gian.
        private LocalDate selectedDate = null;
        // currentMonth: Lưu tháng đang hiển thị dưới dạng dữ liệu thời gian.
        private YearMonth currentMonth = YearMonth.now();
        // chiTuHomNay: Cờ cho biết chỉ cho chọn từ hôm nay.
        private final boolean chiTuHomNay;
        // btnPrev: Nút thao tác trước.
        private JButton btnPrev;
        // pnlDays: Khung chứa thành phần giao diện ngày.
        private JPanel pnlDays;
        // lblMonthYear: Nhãn hiển thị tháng và năm.
        private JLabel lblMonthYear;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số winParent: thành phần cha.
        public DatePickerDialog(Frame winParent) {
            this(winParent, null, false);
        }

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        // Tham số winParent: thành phần cha.
        // Tham số ngayHienTai: ngày hiện tại.
        // Tham số chiTuHomNay: chỉ cho chọn từ hôm nay.
        public DatePickerDialog(Frame winParent, LocalDate ngayHienTai, boolean chiTuHomNay) {
            super(winParent, "Chọn ngày", true);
            this.chiTuHomNay = chiTuHomNay;
            this.currentMonth = YearMonth.from(ngayHienTai != null ? ngayHienTai : LocalDate.now());
            setSize(320, 280);
            setLayout(new BorderLayout());
            setResizable(false);
            getContentPane().setBackground(Color.WHITE);

            // pnlHeader: Khung chứa thành phần giao diện đầu trang.
            JPanel pnlHeader = new JPanel(new BorderLayout());
            pnlHeader.setBackground(Color.WHITE);
            pnlHeader.setBorder(new EmptyBorder(10, 10, 10, 10));

            btnPrev = new JButton("◄");
            btnPrev.setFocusPainted(false);
            btnPrev.setBackground(new Color(229, 241, 252));
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnPrev.addActionListener(e -> {
                currentMonth = currentMonth.minusMonths(1);
                updateCalendar();
            });

            lblMonthYear = new JLabel("", SwingConstants.CENTER);
            lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblMonthYear.setForeground(new Color(18, 48, 82));

            // btnNext: Nút thao tác: ►.
            JButton btnNext = new JButton("►");
            btnNext.setFocusPainted(false);
            btnNext.setBackground(new Color(229, 241, 252));
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnNext.addActionListener(e -> {
                currentMonth = currentMonth.plusMonths(1);
                updateCalendar();
            });

            pnlHeader.add(btnPrev, BorderLayout.WEST);
            pnlHeader.add(lblMonthYear, BorderLayout.CENTER);
            pnlHeader.add(btnNext, BorderLayout.EAST);

            add(pnlHeader, BorderLayout.NORTH);

            pnlDays = new JPanel(new GridLayout(0, 7, 2, 2));
            pnlDays.setBackground(Color.WHITE);
            pnlDays.setBorder(new EmptyBorder(0, 10, 10, 10));
            add(pnlDays, BorderLayout.CENTER);

            // pnlFooter: Khung chứa thành phần giao diện cuối trang.
            JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            pnlFooter.setBackground(Color.WHITE);

            // btnToday: Nút thao tác: Hôm nay.
            JButton btnToday = new JButton("Hôm nay");
            btnToday.setBackground(new Color(229, 241, 252));
            btnToday.setFocusPainted(false);
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnToday.addActionListener(e -> {
                selectedDate = LocalDate.now();
                dispose();
            });

            // btnCancel: Nút thao tác: Hủy.
            JButton btnCancel = new JButton("Hủy");
            btnCancel.setBackground(new Color(229, 241, 252));
            btnCancel.setFocusPainted(false);
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnCancel.addActionListener(e -> dispose());

            pnlFooter.add(btnToday);
            pnlFooter.add(btnCancel);
            add(pnlFooter, BorderLayout.SOUTH);

            updateCalendar();
        }

        // Vẽ lại lịch theo tháng hiện tại và ngày đang chọn.
        private void updateCalendar() {
            pnlDays.removeAll();
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblMonthYear.setText("Tháng " + currentMonth.getMonthValue() + " / " + currentMonth.getYear());

            // daysOfWeek: Mảng lưu ngày của tuần.
            String[] daysOfWeek = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
            for (String day : daysOfWeek) {
                // lblLbl: Nhãn hiển thị nhãn.
                JLabel lblLbl = new JLabel(day, SwingConstants.CENTER);
                lblLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblLbl.setForeground(new Color(18, 48, 82));
                pnlDays.add(lblLbl);
            }

            // firstDayOfMonth: Lưu đầu tiên ngày của tháng dưới dạng dữ liệu thời gian.
            LocalDate firstDayOfMonth = currentMonth.atDay(1);
            int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue();
            int daysInMonth = currentMonth.lengthOfMonth();

            for (int i = 1; i < dayOfWeekValue; i++) {
                pnlDays.add(new JLabel(""));
            }

            btnPrev.setEnabled(!chiTuHomNay || currentMonth.isAfter(YearMonth.now()));
            for (int i = 1; i <= daysInMonth; i++) {
                // btnDay: Nút thao tác ngày.
                JButton btnDay = new JButton(String.valueOf(i));
                btnDay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btnDay.setFocusPainted(false);
                btnDay.setBackground(Color.WHITE);
                btnDay.setBorder(new LineBorder(new Color(200, 200, 200)));

                // dateOfButton: Lưu ngày của nút dưới dạng dữ liệu thời gian.
                LocalDate dateOfButton = currentMonth.atDay(i);
                if (dateOfButton.equals(LocalDate.now())) {
                    btnDay.setBackground(new Color(229, 241, 252));
                    btnDay.setFont(new Font("Segoe UI", Font.BOLD, 12));
                }

                if (chiTuHomNay && dateOfButton.isBefore(LocalDate.now())) {
                    btnDay.setEnabled(false);
                    btnDay.setForeground(Color.GRAY);
                }
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnDay.addActionListener(e -> {
                    selectedDate = dateOfButton;
                    dispose();
                });
                pnlDays.add(btnDay);
            }

            pnlDays.revalidate();
            pnlDays.repaint();
        }

        // Trả về ngày đã chọn của đối tượng.
        public LocalDate getSelectedDate() {
            return selectedDate;
        }
    }

    // Hộp thoại nhập hoặc chỉnh sửa đặt lịch.
    class DatLichDialog extends JDialog {
        // txtNgayDat: Ô nhập/hiển thị văn bản ngày đặt.
        // txtGioHen: Ô nhập/hiển thị văn bản giờ hẹn.
        private JTextField txtNgayDat, txtGioHen;
        // cmbKhachHang: Hộp lựa chọn khách hàng.
        // cmbThuCung: Hộp lựa chọn thú cưng.
        // cmbNhanVienSpa: Hộp lựa chọn nhân viên Spa.
        private JComboBox<String> cmbKhachHang, cmbThuCung, cmbNhanVienSpa;
        // txtFormTrangThai: Ô nhập/hiển thị văn bản trạng thái trên biểu mẫu.
        private JTextField txtFormTrangThai;

        // lichHenDAO: Đối tượng truy cập dữ liệu lịch hẹn.
        private LichHenDAO lichHenDAO;
        // pnlParentPanel: Đối tượng màn hình cha có kiểu QuanLyLichHen.
        private QuanLyLichHen pnlParentPanel;
        // lhEdit: Đối tượng lịch hẹn cần chỉnh sửa có kiểu LichHen.
        private LichHen lhEdit;

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        // Tham số winParent: thành phần cha.
        // Tham số pnlKhung: khung.
        // Tham số dao: đối tượng truy cập dữ liệu lịch hẹn.
        // Tham số lhEdit: lịch hẹn cần chỉnh sửa.
        public DatLichDialog(JFrame winParent, QuanLyLichHen pnlKhung, LichHenDAO dao, LichHen lhEdit) {
            super(winParent, lhEdit == null ? "Đặt Lịch Mới" : "Sửa Lịch Hẹn - " + lhEdit.getMaLichHen(), true);
            this.lichHenDAO = dao;
            this.pnlParentPanel = pnlKhung;
            this.lhEdit = lhEdit;

            setSize(800, 500); // Đã thu nhỏ form cho vừa vặn khi không còn trường Ghi chú
            setLocationRelativeTo(winParent);
            setResizable(false);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);

            // pnlForm: Khung chứa thành phần giao diện biểu mẫu.
            JPanel pnlForm = new JPanel(new GridBagLayout());
            pnlForm.setBackground(Color.WHITE);
            pnlForm.setBorder(new EmptyBorder(20, 30, 20, 30));
            // gbc: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng GridBagLayout.
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(8, 10, 8, 10);
            gbc.weightx = 0.5;

            // 1. Ngày & Giờ
            gbc.gridx = 0;
            gbc.gridy = 0;
            pnlForm.add(taoLabel("Ngày đặt: (dd/MM/yyyy)"), gbc);
            gbc.gridx = 1;
            pnlForm.add(taoLabel("Giờ hẹn: (HH:mm)"), gbc);

            // pnlNgay: Khung chứa thành phần giao diện ngày.
            JPanel pnlNgay = new JPanel(new BorderLayout());
            pnlNgay.setOpaque(false);
            // Ngày giờ mặc định phải ở tương lai, kể cả khi đang gần nửa đêm.
            // macDinh: Lưu mặc định dưới dạng dữ liệu thời gian.
            LocalDateTime macDinh = LocalDateTime.now().plusMinutes(15).withSecond(0).withNano(0);
            txtNgayDat = taoTextField(macDinh.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            txtNgayDat.setEditable(false);
            txtNgayDat.setFocusable(false);
            txtNgayDat.setBackground(new Color(244, 247, 250));
            txtNgayDat.setToolTipText("Chỉ thay đổi ngày hẹn bằng nút Lịch");
            pnlNgay.add(txtNgayDat, BorderLayout.CENTER);

            // btnLichForm: Nút thao tác: Lịch.
            JButton btnLichForm = new JButton("Lịch");
            btnLichForm.setBackground(NAVY);
            btnLichForm.setForeground(Color.WHITE);
            btnLichForm.setFocusPainted(false);
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnLichForm.addActionListener(e -> {
                // ngayCu: Lưu ngày cụ dưới dạng dữ liệu thời gian.
                LocalDate ngayCu = LocalDate.now();
                try {
                    // Phân tích chuỗi ngày thành LocalDate theo bộ định dạng được chỉ định.
                    ngayCu = LocalDate.parse(txtNgayDat.getText().trim(),
                            DateTimeFormatter.ofPattern("dd/MM/uuuu"));
                } catch (DateTimeParseException ex) {
                    // Sử dụng ngày hiện tại khi không có ngày hợp lệ.
                }
                DatePickerDialog winPicker = new DatePickerDialog(
                        (JFrame) SwingUtilities.getWindowAncestor(this), ngayCu, true);
                winPicker.setLocationRelativeTo(btnLichForm);
                winPicker.setVisible(true);
                if (winPicker.getSelectedDate() != null) {
                    // Định dạng dữ liệu thành chuỗi rồi hiển thị trong control bằng setText().
                    txtNgayDat.setText(winPicker.getSelectedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            });
            pnlNgay.add(btnLichForm, BorderLayout.EAST);

            txtGioHen = taoOChonGio(macDinh.format(DateTimeFormatter.ofPattern("HH:mm")));
            gbc.gridy = 1;
            gbc.gridx = 0;
            pnlForm.add(pnlNgay, gbc);
            gbc.gridx = 1;
            pnlForm.add(txtGioHen, gbc);

            // Tiêu đề Phân cách 1
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            pnlForm.add(taoTitleLabel("Thông tin Khách hàng & Thú cưng"), gbc);
            gbc.gridwidth = 1;

            // 2. Khách hàng & Thú cưng
            gbc.gridx = 0;
            gbc.gridy = 3;
            pnlForm.add(taoLabel("Khách hàng:"), gbc);
            gbc.gridx = 1;
            pnlForm.add(taoLabel("Thú cưng:"), gbc);

            cmbKhachHang = new JComboBox<>();
            cmbThuCung = new JComboBox<>();
            styleCombo(cmbKhachHang);
            styleCombo(cmbThuCung);
            gbc.gridy = 4;
            gbc.gridx = 0;
            pnlForm.add(cmbKhachHang, gbc);
            gbc.gridx = 1;
            pnlForm.add(cmbThuCung, gbc);

            // Tiêu đề Phân cách 2
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 2;
            pnlForm.add(taoTitleLabel("Phân công & Trạng thái"), gbc);
            gbc.gridwidth = 1;

            // 3. Nhân viên & Trạng thái
            gbc.gridx = 0;
            gbc.gridy = 6;
            pnlForm.add(taoLabel("Nhân viên Spa:"), gbc);
            gbc.gridx = 1;
            pnlForm.add(taoLabel("Trạng thái:"), gbc);

            cmbNhanVienSpa = new JComboBox<>();
            txtFormTrangThai = taoTextField(
                    lhEdit == null ? "Xác nhận" : lhEdit.getTrangThai() == null ? "Xác nhận" : lhEdit.getTrangThai());
            txtFormTrangThai.setEditable(false);
            txtFormTrangThai.setFocusable(false);
            txtFormTrangThai.setBackground(new Color(244, 247, 250));
            txtFormTrangThai.setToolTipText("Tự xác nhận khi tạo mới; nút Hủy trong bảng dùng để hủy lịch");
            styleCombo(cmbNhanVienSpa);
            gbc.gridy = 7;
            gbc.gridx = 0;
            pnlForm.add(cmbNhanVienSpa, gbc);
            gbc.gridx = 1;
            pnlForm.add(txtFormTrangThai, gbc);

            // Đã xóa hoàn toàn Phần 4 (Ghi chú / Dịch vụ)

            add(pnlForm, BorderLayout.CENTER);

            // --- BOTTOM BUTTONS ---
            // pnlBtn: Khung chứa thành phần giao diện nút.
            JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
            pnlBtn.setBackground(Color.WHITE);

            // btnHuy: Nút thao tác: Hủy.
            JButton btnHuy = new JButton("Hủy");
            btnHuy.setBackground(GRAY_BTN);
            btnHuy.setForeground(Color.WHITE);
            btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnHuy.setPreferredSize(new Dimension(100, 35));
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnHuy.addActionListener(e -> dispose());

            // btnLuu: Nút thao tác lưu.
            JButton btnLuu = new JButton(lhEdit == null ? "Thêm mới" : "Cập nhật");
            btnLuu.setBackground(DARK_BLUE);
            btnLuu.setForeground(Color.WHITE);
            btnLuu.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnLuu.setPreferredSize(new Dimension(120, 35));
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnLuu.addActionListener(e -> xuLyLuuDuLieu());

            pnlBtn.add(btnHuy);
            pnlBtn.add(btnLuu);
            add(pnlBtn, BorderLayout.SOUTH);

            loadComboData();
            dienDuLieuSua();
        }

        // Nạp hộp lựa chọn dữ liệu.
        private void loadComboData() {
            // Nạp khách hàng trước, rồi mới nạp thú cưng tương ứng.
            // Nếu SQL không trả về khách hàng, JComboBox sẽ không có lựa chọn.
            cmbKhachHang.removeAllItems();
            for (String[] kh : lichHenDAO.getDanhSachKhachHang()) {
                cmbKhachHang.addItem(kh[0] + " - " + kh[1]);
            }
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            cmbKhachHang.addActionListener(e -> napThuCungTheoKhachHang());
            if (cmbKhachHang.getItemCount() > 0)
                cmbKhachHang.setSelectedIndex(0);
            napThuCungTheoKhachHang();

            cmbNhanVienSpa.removeAllItems();
            for (String[] nv : lichHenDAO.getDanhSachNhanVienSpa()) {
                cmbNhanVienSpa.addItem(nv[0] + " - " + nv[1]);
            }
            if (cmbNhanVienSpa.getItemCount() > 0)
                cmbNhanVienSpa.setSelectedIndex(0);
        }

        // Đưa thông tin bản ghi đang sửa lên các trường của biểu mẫu.
        private void dienDuLieuSua() {
            if (lhEdit == null)
                return;

            if (lhEdit.getNgayGioHen() != null) {
                // Định dạng dữ liệu thành chuỗi rồi hiển thị trong control bằng setText().
                txtNgayDat.setText(lhEdit.getNgayGioHen().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                // Định dạng dữ liệu thành chuỗi rồi hiển thị trong control bằng setText().
                txtGioHen.setText(lhEdit.getNgayGioHen().format(DateTimeFormatter.ofPattern("HH:mm")));
            }

            setComboSelectedById(cmbKhachHang, lhEdit.getMaKhachHang());
            napThuCungTheoKhachHang();
            setComboSelectedById(cmbThuCung, lhEdit.getMaThuCung());
            setComboSelectedById(cmbNhanVienSpa, lhEdit.getMaNhanVien());

            if (lhEdit.getTrangThai() != null) {
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtFormTrangThai.setText(lhEdit.getTrangThai());
            }
        }

        // Nạp thú cưng theo khách hàng.
        private void napThuCungTheoKhachHang() {
            if (cmbThuCung == null || cmbKhachHang == null)
                return;
            Object khDaChon = cmbKhachHang.getSelectedItem();
            cmbThuCung.removeAllItems();
            if (khDaChon == null) {
                cmbThuCung.setToolTipText("Chọn khách hàng trước khi chọn thú cưng.");
                return;
            }
            String maKh = extractId(khDaChon.toString());
            for (String[] tc : lichHenDAO.getDanhSachThuCungTheoKhachHang(maKh)) {
                cmbThuCung.addItem(tc[0] + " - " + tc[1]);
            }
            if (cmbThuCung.getItemCount() > 0) {
                cmbThuCung.setSelectedIndex(0);
                cmbThuCung.setToolTipText(null);
            } else {
                cmbThuCung.setToolTipText("Khách hàng chưa có thú cưng. Đăng ký trong Danh mục > Thú cưng.");
            }
        }

        // Tạo ô chọn giờ.
        // Tham số gioMacDinh: giờ mặc định.
        // Kết quả: đối tượng/giá trị kiểu JTextField.
        private JTextField taoOChonGio(String gioMacDinh) {
            try {
                MaskFormatter dinhDang = new MaskFormatter("##:##");
                dinhDang.setPlaceholderCharacter('_');
                // txtTxt: Ô nhập theo định dạng ô văn bản.
                JFormattedTextField txtTxt = new JFormattedTextField(dinhDang);
                txtTxt.setFocusLostBehavior(JFormattedTextField.PERSIST);
                txtTxt.setValue(gioMacDinh);
                txtTxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                txtTxt.setPreferredSize(new Dimension(200, 35));
                txtTxt.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        new EmptyBorder(5, 10, 5, 10)));
                txtTxt.setToolTipText("Giờ theo HH:mm, ví dụ 09:30 (00:00-23:59)");
                return txtTxt;
            } catch (ParseException e) {
                throw new IllegalStateException("Không khởi tạo được định dạng giờ HH:mm", e);
            }
        }

        // Chọn mục trong hộp lựa chọn theo mã.
        // Tham số cmbCbo: hộp lựa chọn.
        // Tham số id: mã.
        private void setComboSelectedById(JComboBox<String> cmbCbo, String id) {
            if (id == null)
                return;
            for (int i = 0; i < cmbCbo.getItemCount(); i++) {
                if (cmbCbo.getItemAt(i).startsWith(id + " - ")) {
                    cmbCbo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Xử lý lưu dữ liệu.
        private void xuLyLuuDuLieu() {
            try {
                // ngayStr: Đọc nội dung chữ từ ô nhập để xử lý ngày chuỗi.
                String ngayStr = txtNgayDat.getText().trim();
                // gioStr: Đọc nội dung chữ từ ô nhập để xử lý giờ chuỗi.
                String gioStr = txtGioHen.getText().trim();
                // ngayHen: Lưu ngày hẹn dưới dạng dữ liệu thời gian.
                LocalDate ngayHen = LocalDate.parse(ngayStr,
                        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
                LocalTime gioHen = LocalTime.parse(gioStr,
                        DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT));
                // ngayGioHen: Lưu ngày giờ hẹn dưới dạng dữ liệu thời gian.
                LocalDateTime ngayGioHen = LocalDateTime.of(ngayHen, gioHen);
                if (!ngayGioHen.isAfter(LocalDateTime.now())) {
                    JOptionPane.showMessageDialog(this,
                            "Giờ hẹn phải lớn hơn thời gian hiện tại. Vui lòng chọn giờ trong tương lai.",
                            "Ngày giờ không hợp lệ", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // Chỉ đọc lựa chọn một lần; không gọi .toString() khi chưa có dữ liệu.
                Object khDaChon = cmbKhachHang.getSelectedItem();
                Object tcDaChon = cmbThuCung.getSelectedItem();
                Object nvDaChon = cmbNhanVienSpa.getSelectedItem();
                if (khDaChon == null) {
                    JOptionPane.showMessageDialog(this,
                            "Chưa có khách hàng để đặt lịch. Hãy kiểm tra dữ liệu KhachHang hoặc kết nối SQL Server.",
                            "Chưa chọn khách hàng", JOptionPane.WARNING_MESSAGE);
                    cmbKhachHang.requestFocusInWindow();
                    return;
                }
                if (tcDaChon == null) {
                    JOptionPane.showMessageDialog(this,
                            "Khách hàng đã chọn chưa có thú cưng. Hãy thêm hồ sơ tại Danh mục > Thú cưng trước.",
                            "Chưa chọn thú cưng", JOptionPane.WARNING_MESSAGE);
                    cmbThuCung.requestFocusInWindow();
                    return;
                }
                if (nvDaChon == null) {
                    JOptionPane.showMessageDialog(this,
                            "Chưa có thợ Spa để phân công. Hãy kiểm tra vai trò nhân viên và dữ liệu NhanVien.",
                            "Chưa chọn nhân viên Spa", JOptionPane.WARNING_MESSAGE);
                    cmbNhanVienSpa.requestFocusInWindow();
                    return;
                }
                if ("Đã hủy".equals(txtFormTrangThai.getText())) {
                    JOptionPane.showMessageDialog(this, "Lịch đã hủy không thể sửa. Hãy tạo lịch mới.",
                            "Lịch đã hủy", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // maKH: Lưu mã khách hàng.
                String maKH = extractId(khDaChon.toString());
                // maTC: Lưu mã thú cưng.
                String maTC = extractId(tcDaChon.toString());
                // maNV: Lưu mã nhân viên.
                String maNV = extractId(nvDaChon.toString());
                // trangThai: Đọc nội dung chữ từ ô nhập để xử lý trạng thái.
                String trangThai = txtFormTrangThai.getText();

                String maLH = (lhEdit == null) ? taoMaLichHenTuDong() : lhEdit.getMaLichHen();

                // Trực tiếp truyền chuỗi rỗng "" vào vị trí của biến ghi chú
                LichHen lh = new LichHen(maLH, ngayGioHen, "", maKH, maNV, maTC);
                lh.setTrangThai(trangThai);

                boolean thanhCong;
                if (lhEdit == null) {
                    thanhCong = lichHenDAO.them(lh);
                } else {
                    thanhCong = lichHenDAO.sua(lh);
                }

                if (thanhCong) {
                    JOptionPane.showMessageDialog(this,
                            lhEdit == null ? "Đã thêm lịch hẹn mới!" : "Đã cập nhật lịch hẹn!");
                    pnlParentPanel.loadData();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Thao tác thất bại. Vui lòng kiểm tra lại!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Ngày phải chọn từ lịch và giờ phải đúng HH:mm (00:00–23:59).",
                        "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                // Hiển thị lý do do DAO trả về (quá giờ hoặc thợ Spa trùng lịch).
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Không thể lưu lịch hẹn", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Không thể lưu lịch hẹn: " + ex.getMessage() + "\nKiểm tra Console nếu lỗi tiếp tục xảy ra.",
                        "Lỗi đặt lịch", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Tách phần mã khỏi nội dung mục đang hiển thị.
        // Tham số item: mục.
        // Kết quả: chuỗi kết quả.
        private String extractId(String item) {
            if (item == null)
                return "";
            int idx = item.indexOf(" - ");
            return idx > 0 ? item.substring(0, idx) : item;
        }

        // Tạo mã lịch hẹn từ dòng.
        // Kết quả: chuỗi kết quả.
        private String taoMaLichHenTuDong() {
            int max = 0;
            for (LichHen lh : lichHenDAO.getAll()) {
                if (lh.getMaLichHen().startsWith("LH")) {
                    try {
                        int num = Integer.parseInt(lh.getMaLichHen().substring(2));
                        if (num > max)
                            max = num;
                    } catch (Exception e) {
                    }
                }
            }
            return String.format("LH%03d", max + 1);
        }

        // Tạo nhãn.
        // Tham số text: nội dung chữ.
        // Kết quả: đối tượng/giá trị kiểu JLabel.
        private JLabel taoLabel(String text) {
            // lblLbl: Nhãn hiển thị nhãn.
            JLabel lblLbl = new JLabel(text);
            lblLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblLbl.setForeground(Color.GRAY);
            return lblLbl;
        }

        // Tạo tiêu đề nhãn.
        // Tham số text: nội dung chữ.
        // Kết quả: đối tượng/giá trị kiểu JLabel.
        private JLabel taoTitleLabel(String text) {
            // lblLbl: Nhãn hiển thị nhãn.
            JLabel lblLbl = new JLabel(text);
            lblLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblLbl.setForeground(Color.BLACK);
            return lblLbl;
        }

        // Tạo ô nhập văn bản.
        // Tham số defaultText: nội dung mặc định.
        // Kết quả: đối tượng/giá trị kiểu JTextField.
        private JTextField taoTextField(String defaultText) {
            // txtTxt: Ô nhập/hiển thị văn bản ô văn bản.
            JTextField txtTxt = new JTextField(defaultText);
            txtTxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtTxt.setPreferredSize(new Dimension(200, 35));
            txtTxt.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(5, 10, 5, 10)));
            return txtTxt;
        }

        // Áp dụng kiểu hiển thị cho hộp lựa chọn.
        // Tham số cmbCbo: hộp lựa chọn.
        private void styleCombo(JComboBox<String> cmbCbo) {
            cmbCbo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cmbCbo.setPreferredSize(new Dimension(200, 35));
            cmbCbo.setBackground(Color.WHITE);
        }
    }
}