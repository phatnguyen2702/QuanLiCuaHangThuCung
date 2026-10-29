package UI;

import dao.LichHenDAO;
import dao.PhieuDichVuDAO;
import entity.PhieuDichVu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Giao diện/chức năng quản lý phiếu dịch vụ của ứng dụng.
public class QuanLyPhieuDichVu extends JPanel {

    // NAVY: Màu dùng cho giao diện: xanh đậm.
    private final Color NAVY = new Color(18, 48, 82);
    // LIGHT_BLUE: Màu dùng cho giao diện: xanh dương.
    private final Color LIGHT_BLUE = new Color(229, 241, 252);
    // DARK_BTN: Màu dùng cho giao diện: nút.
    private final Color DARK_BTN = new Color(11, 43, 64);
    // GRAY_BTN: Màu dùng cho giao diện: nút.
    private final Color GRAY_BTN = new Color(136, 142, 148);
    // STATUS_GREEN: Màu dùng cho giao diện: xanh lá.
    private final Color STATUS_GREEN = new Color(115, 214, 137);
    // STATUS_GRAY: Màu dùng cho giao diện: xám.
    private final Color STATUS_GRAY = new Color(200, 200, 200);

    // txtTimKiem: Ô nhập/hiển thị văn bản tìm kiếm.
    private JTextField txtTimKiem;
    // cmbNhanVien: Hộp lựa chọn nhân viên.
    // cmbTrangThai: Hộp lựa chọn trạng thái.
    // cmbDichVuLoc: Hộp lựa chọn dịch vụ lọc.
    private JComboBox<String> cmbNhanVien, cmbTrangThai, cmbDichVuLoc;
    // tblDuLieu: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
    private JTable tblDuLieu;
    // tableModel: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private DefaultTableModel tableModel;

    // dao: Đối tượng truy cập dữ liệu phiếu dịch vụ.
    private PhieuDichVuDAO dao;
    // lichHenDAO: Đối tượng truy cập dữ liệu lịch hẹn.
    private LichHenDAO lichHenDAO;

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    public QuanLyPhieuDichVu() {
        dao = new PhieuDichVuDAO();
        lichHenDAO = new LichHenDAO();
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

        // 1. Tiêu đề & Nút Tạo phiếu
        // pnlTitleRow: Khung chứa thành phần giao diện tiêu đề dòng.
        JPanel pnlTitleRow = new JPanel(new BorderLayout());
        pnlTitleRow.setOpaque(false);

        // lblTitle: Nhãn hiển thị: QUẢN LÝ PHIẾU DỊCH VỤ.
        JLabel lblTitle = new JLabel("QUẢN LÝ PHIẾU DỊCH VỤ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        pnlTitleRow.add(lblTitle, BorderLayout.WEST);

        // btnTaoPhieu: Nút thao tác: + Tạo phiếu mới.
        JButton btnTaoPhieu = new JButton("+ Tạo phiếu mới");
        btnTaoPhieu.setBackground(DARK_BTN);
        btnTaoPhieu.setForeground(Color.WHITE);
        btnTaoPhieu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTaoPhieu.setFocusPainted(false);
        btnTaoPhieu.setPreferredSize(new Dimension(160, 38));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTaoPhieu.addActionListener(e -> hienFormPhieuDichVu(null));
        pnlTitleRow.add(btnTaoPhieu, BorderLayout.EAST);

        pnlKhung.add(pnlTitleRow, BorderLayout.NORTH);

        // 2. Bộ lọc
        // pnlFilterRow: Khung chứa thành phần giao diện bộ lọc dòng.
        JPanel pnlFilterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlFilterRow.setBackground(Color.WHITE);
        pnlFilterRow.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));

        pnlFilterRow.add(createFilterLabel("Nhân viên"));
        cmbNhanVien = new JComboBox<>();
        cmbNhanVien.setPreferredSize(new Dimension(150, 30));
        pnlFilterRow.add(cmbNhanVien);

        pnlFilterRow.add(createFilterLabel("Trạng thái"));
        cmbTrangThai = new JComboBox<>(new String[] { "Tất cả", "Chưa thanh toán", "Đã thanh toán" });
        cmbTrangThai.setPreferredSize(new Dimension(120, 30));
        pnlFilterRow.add(cmbTrangThai);

        pnlFilterRow.add(createFilterLabel("Dịch vụ"));
        cmbDichVuLoc = new JComboBox<>();
        cmbDichVuLoc.setPreferredSize(new Dimension(165, 30));
        pnlFilterRow.add(cmbDichVuLoc);

        pnlFilterRow.add(createFilterLabel("Tìm kiếm"));
        txtTimKiem = new JTextField(15);
        txtTimKiem.setToolTipText("Mã, Khách, Thú cưng, Dịch vụ...");
        pnlFilterRow.add(txtTimKiem);

        // btnTimKiem: Nút thao tác: Lọc.
        JButton btnTimKiem = new JButton("Lọc");
        btnTimKiem.setBackground(NAVY);
        btnTimKiem.setForeground(Color.WHITE);

        // Gắn sự kiện lọc
        btnTimKiem.addActionListener(e -> locDuLieu());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        txtTimKiem.addActionListener(e -> locDuLieu());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbNhanVien.addActionListener(e -> locDuLieu());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbTrangThai.addActionListener(e -> locDuLieu());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbDichVuLoc.addActionListener(e -> locDuLieu());

        pnlFilterRow.add(btnTimKiem);
        // btnLamMoi: Nút thao tác: Làm mới.
        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setBackground(new Color(226, 233, 241));
        btnLamMoi.setFocusPainted(false);
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLamMoi.addActionListener(e -> lamMoiBoLoc());
        pnlFilterRow.add(btnLamMoi);

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

        // Thêm cột Dịch Vụ
        // columns: Mảng lưu các cột.
        String[] columns = { "Mã Phiếu", "Mã Lịch Hẹn", "Khách Hàng", "Thú Cưng",
                "Nhân Viên", "Dịch Vụ", "Thanh Toán", "Thao Tác" };
        tableModel = new DefaultTableModel(columns, 0) {
            // Quy định ô của bảng có được chỉnh sửa trực tiếp hay không.
            // Tham số row: chỉ số dòng.
            // Tham số column: chỉ số cột.
            // Kết quả: true/false theo điều kiện xử lý.
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Chỉ cột Thao tác được bấm
            }
        };

        tblDuLieu = new JTable(tableModel);
        tblDuLieu.setRowHeight(54);
        tblDuLieu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblDuLieu.getTableHeader().setBackground(Color.WHITE);
        tblDuLieu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDuLieu.getTableHeader().setPreferredSize(new Dimension(100, 40));

        // Tinh chỉnh độ rộng các cột
        // rongCot: Mảng lưu rỗng cột.
        int[] rongCot = { 85, 100, 160, 165, 145, 355, 135, 145 };
        for (int i = 0; i < rongCot.length; i++)
            tblDuLieu.getColumnModel().getColumn(i).setPreferredWidth(rongCot[i]);

        tblDuLieu.setBorder(BorderFactory.createLineBorder(new Color(51, 153, 255), 1));

        DefaultTableCellRenderer lblCenter = new DefaultTableCellRenderer();
        lblCenter.setHorizontalAlignment(SwingConstants.CENTER);
        tblDuLieu.getColumnModel().getColumn(0).setCellRenderer(lblCenter);
        tblDuLieu.getColumnModel().getColumn(1).setCellRenderer(lblCenter);
        tblDuLieu.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        tblDuLieu.getColumnModel().getColumn(5).setCellRenderer(new DichVuNhieuDongRenderer());
        tblDuLieu.getColumnModel().getColumn(7).setCellRenderer(new ActionButtonRenderer());
        tblDuLieu.getColumnModel().getColumn(7).setCellEditor(new ActionButtonEditor());
        tblDuLieu.setToolTipText("Nhấp đúp một dòng để xem hoặc sửa phiếu chưa thanh toán.");
        tblDuLieu.addMouseListener(new java.awt.event.MouseAdapter() {
            // Xử lý thao tác nhấn chuột trên thành phần đã gắn listener.
            // Tham số e: thông tin sự kiện.
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tblDuLieu.getSelectedRow() >= 0
                        && tblDuLieu.columnAtPoint(e.getPoint()) != 7) {
                    // ma: Lưu mã.
                    String ma = String.valueOf(tblDuLieu.getValueAt(tblDuLieu.getSelectedRow(), 0));
                    for (PhieuDichVu pdv : dao.getAll()) {
                        if (ma.equals(pdv.getMaPhieuDichVu())) {
                            hienFormPhieuDichVu(pdv);
                            break;
                        }
                    }
                }
            }
        });

        tblDuLieu.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // scrCuon2: Khung cuộn khung cuộn.
        JScrollPane scrCuon2 = new JScrollPane(tblDuLieu);
        scrCuon2.getViewport().setBackground(Color.WHITE);
        scrCuon2.setBorder(BorderFactory.createEmptyBorder());
        pnlKhung.add(scrCuon2, BorderLayout.CENTER);
        return pnlKhung;
    }

    // Nạp hộp lựa chọn khung.
    private void loadComboBox() {
        cmbNhanVien.removeAllItems();
        cmbNhanVien.addItem("Tất cả");
        for (String[] nv : lichHenDAO.getDanhSachNhanVienSpa()) {
            cmbNhanVien.addItem(nv[1]);
        }
        cmbDichVuLoc.removeAllItems();
        cmbDichVuLoc.addItem("Tất cả");
        for (String[] dv : dao.getDanhSachDichVu()) {
            cmbDichVuLoc.addItem(dv[1]);
        }
    }

    // Làm mới bỏ lọc.
    private void lamMoiBoLoc() {
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtTimKiem.setText("");
        cmbNhanVien.setSelectedIndex(0);
        cmbTrangThai.setSelectedIndex(0);
        cmbDichVuLoc.setSelectedIndex(0);
        loadComboBox();
        locDuLieu();
    }

    // Đọc dữ liệu qua DAO và cập nhật bảng trên giao diện.
    public void loadData() {
        locDuLieu(); // Dùng hàm lọc để load dữ liệu
    }

    // =========================================================
    // HÀM LỌC DỮ LIỆU
    // =========================================================
    // Lọc dữ liệu.
    private void locDuLieu() {
        if (cmbNhanVien == null || cmbTrangThai == null || cmbDichVuLoc == null ||
                cmbNhanVien.getSelectedItem() == null || cmbTrangThai.getSelectedItem() == null ||
                cmbDichVuLoc.getSelectedItem() == null)
            return;

        String nhanVienLoc = cmbNhanVien.getSelectedItem().toString();
        String trangThaiLoc = cmbTrangThai.getSelectedItem().toString();
        String dichVuLoc = cmbDichVuLoc.getSelectedItem().toString();
        // tuKhoa: Đọc nội dung chữ từ ô nhập để xử lý từ khóa tìm kiếm.
        String tuKhoa = txtTimKiem.getText().trim().toLowerCase();

        tableModel.setRowCount(0);

        for (PhieuDichVu pdv : dao.getAll()) {
            boolean isMatch = true;

            // 1. Lọc theo Nhân Viên
            if (!nhanVienLoc.equals("Tất cả")) {
                if (pdv.getTenNhanVien() == null || !pdv.getTenNhanVien().equals(nhanVienLoc)) {
                    isMatch = false;
                }
            }

            // 2. Lọc theo Trạng Thái
            if (!trangThaiLoc.equals("Tất cả")) {
                if (pdv.getTrangThai() == null || !pdv.getTrangThai().equals(trangThaiLoc)) {
                    isMatch = false;
                }
            }

            if (!"Tất cả".equals(dichVuLoc)) {
                // cacDichVu: Lưu các dịch vụ.
                String cacDichVu = pdv.getCacDichVu() == null ? "" : pdv.getCacDichVu();
                if (!java.util.Arrays.asList(cacDichVu.split(",\\s*")).contains(dichVuLoc)) {
                    isMatch = false;
                }
            }

            // 3. Lọc theo Từ Khóa (Mã phiếu, Mã Lịch Hẹn, Tên thú cưng, Tên Dịch Vụ)
            if (!tuKhoa.isEmpty()) {
                // maPhieu: Lưu mã phiếu.
                String maPhieu = pdv.getMaPhieuDichVu() != null ? pdv.getMaPhieuDichVu().toLowerCase() : "";
                String maLH = pdv.getMaLichHen() != null ? pdv.getMaLichHen().toLowerCase() : "";
                String tenTC = pdv.getTenThuCung() != null ? pdv.getTenThuCung().toLowerCase() : "";
                // dichVu: Lưu dịch vụ.
                String dichVu = pdv.getCacDichVu() != null ? pdv.getCacDichVu().toLowerCase() : "";

                if (!maPhieu.contains(tuKhoa) && !maLH.contains(tuKhoa) && !tenTC.contains(tuKhoa)
                        && !dichVu.contains(tuKhoa)) {
                    isMatch = false;
                }
            }

            if (isMatch) {
                tableModel.addRow(new Object[] {
                        pdv.getMaPhieuDichVu(), pdv.getMaLichHen(), pdv.getTenKhachHang(),
                        pdv.getTenThuCung(), pdv.getTenNhanVien(),
                        pdv.getCacDichVu(), pdv.getTrangThai(), ""
                });
            }
        }
        // Đặt chiều cao dòng theo số tên dịch vụ để không bị cắt nội dung.
        int rongCot = tblDuLieu.getColumnModel().getColumn(5).getPreferredWidth();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            // ds: Lưu danh sách.
            String ds = String.valueOf(tableModel.getValueAt(row, 5));
            // txaDoCao: Vùng văn bản nhiều dòng độ cao.
            JTextArea txaDoCao = new JTextArea(ds.replace(", ", "\n"));
            txaDoCao.setFont(tblDuLieu.getFont());
            txaDoCao.setLineWrap(true);
            txaDoCao.setWrapStyleWord(true);
            txaDoCao.setSize(Math.max(120, rongCot - 18), Short.MAX_VALUE);
            tblDuLieu.setRowHeight(row, Math.max(54, txaDoCao.getPreferredSize().height + 18));
        }
    }

    // Hiển thị biểu mẫu phiếu dịch vụ.
    // Tham số pdvEdit: phiếu dịch vụ cần chỉnh sửa.
    private void hienFormPhieuDichVu(PhieuDichVu pdvEdit) {
        if (pdvEdit != null && "Đã thanh toán".equals(pdvEdit.getTrangThai())) {
            JOptionPane.showMessageDialog(this,
                    "Phiếu đã thanh toán không thể sửa. Hãy xem lại hóa đơn nếu cần đối chiếu.",
                    "Không thể sửa", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // winParentFrame: Cửa sổ cha cửa sổ.
        JFrame winParentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        PhieuDichVuDialog winDialog = new PhieuDichVuDialog(winParentFrame, this, dao, lichHenDAO, pdvEdit);
        winDialog.setVisible(true);
    }

    /** Hiển thị đầy đủ tên dịch vụ trên nhiều dòng thay vì dấu "...". */
    private class DichVuNhieuDongRenderer extends JTextArea implements TableCellRenderer {
        // Khởi tạo đối tượng với trạng thái ban đầu.
        DichVuNhieuDongRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setEditable(false);
            setBorder(new EmptyBorder(5, 8, 5, 6));
        }

        // Thiết lập thành phần dùng để vẽ một ô của bảng.
        // Tham số tblTbl: tblTbl.
        // Tham số value: giá trị.
        // Tham số selected: trạng thái được chọn.
        // Tham số focused: trạng thái có tiêu điểm.
        // Tham số row: chỉ số dòng.
        // Tham số column: chỉ số cột.
        // Kết quả: đối tượng/giá trị kiểu Component.
        @Override
        public Component getTableCellRendererComponent(JTable tblTbl, Object value,
                boolean selected, boolean focused, int row, int column) {
            // noiDung: Lưu nội dung.
            String noiDung = value == null ? "" : value.toString();
            setText(noiDung.replace(", ", "\n"));
            setFont(tblTbl.getFont());
            setBackground(selected ? tblTbl.getSelectionBackground() : Color.WHITE);
            setForeground(selected ? tblTbl.getSelectionForeground() : tblTbl.getForeground());
            setToolTipText("<html>" + noiDung.replace("&", "&amp;")
                    .replace("<", "&lt;").replace(">", "&gt;")
                    .replace(", ", "<br>") + "</html>");
            return this;
        }
    }

    // ================== RENDERER MÀU TRẠNG THÁI ==================
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
            if (st.equals("Đã thanh toán"))
                comp.setBackground(STATUS_GREEN);
            else if (st.equals("Chưa thanh toán"))
                comp.setBackground(STATUS_GRAY);
            else
                comp.setBackground(Color.WHITE);
            return comp;
        }
    }

    // ================== RENDERER & EDITOR NÚT BẤM ==================
    // Quy định cách hiển thị ô hoặc thành phần nút thao tác.
    private class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setOpaque(true);
            // btnEdit: Nút thao tác: Sửa.
            JButton btnEdit = new JButton("Sửa");
            btnEdit.setBackground(NAVY);
            btnEdit.setForeground(Color.WHITE);
            // btnDelete: Nút thao tác: Xóa.
            JButton btnDelete = new JButton("Xóa");
            btnDelete.setBackground(new Color(220, 53, 69));
            btnDelete.setForeground(Color.WHITE);
            add(btnEdit);
            add(btnDelete);
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
        // currentMaPhieu: Lưu mã phiếu hiện tại.
        private String currentMaPhieu;

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        public ActionButtonEditor() {
            pnlKhung = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            pnlKhung.setBackground(Color.WHITE);
            // btnEdit: Nút thao tác: Sửa.
            JButton btnEdit = new JButton("Sửa");
            btnEdit.setBackground(NAVY);
            btnEdit.setForeground(Color.WHITE);
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                for (PhieuDichVu p : dao.getAll()) {
                    if (p.getMaPhieuDichVu().equals(currentMaPhieu)) {
                        hienFormPhieuDichVu(p);
                        break;
                    }
                }
            });

            // btnDelete: Nút thao tác: Xóa.
            JButton btnDelete = new JButton("Xóa");
            btnDelete.setBackground(new Color(220, 53, 69));
            btnDelete.setForeground(Color.WHITE);
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnDelete.addActionListener(e -> {
                fireEditingStopped();
                if (JOptionPane.showConfirmDialog(pnlKhung, "Xóa phiếu " + currentMaPhieu + "?", "Xóa",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (dao.xoa(currentMaPhieu)) {
                        JOptionPane.showMessageDialog(pnlKhung, "Đã xóa!");
                        loadData();
                    }
                }
            });
            pnlKhung.add(btnEdit);
            pnlKhung.add(btnDelete);
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
            currentMaPhieu = tblDuLieu.getValueAt(row, 0).toString();
            pnlKhung.setBackground(tblDuLieu.getSelectionBackground());
            return pnlKhung;
        }

        // Trả về giá trị của bộ chỉnh sửa ô hiện tại.
        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    // ================== DIALOG THÊM / SỬA (INNER CLASS) ==================
    // Hộp thoại nhập hoặc chỉnh sửa phiếu dịch vụ.
    class PhieuDichVuDialog extends JDialog {
        // txtMaPhieu: Ô nhập/hiển thị văn bản mã phiếu.
        private JTextField txtMaPhieu;
        // cmbLichHen: Hộp lựa chọn lịch hẹn.
        // cmbKhachHang: Hộp lựa chọn khách hàng.
        // cmbThuCung: Hộp lựa chọn thú cưng.
        // cmbNhanVien: Hộp lựa chọn nhân viên.
        private JComboBox<String> cmbLichHen, cmbKhachHang, cmbThuCung, cmbNhanVien;
        // txtFormTrangThai: Ô nhập/hiển thị văn bản trạng thái trên biểu mẫu.
        private JTextField txtFormTrangThai;

        // CÁC THÀNH PHẦN CHO CHECKBOX DỊCH VỤ
        // chkDichVuList: Lưu ô chọn dịch vụ danh sách; phần tử có kiểu JCheckBox.
        private ArrayList<JCheckBox> chkDichVuList = new ArrayList<>();
        // danhSachDichVu: Lưu danh sách dịch vụ; phần tử có kiểu String[].
        private ArrayList<String[]> danhSachDichVu;

        // pDao: Đối tượng truy cập dữ liệu phiếu dịch vụ.
        private PhieuDichVuDAO pDao;
        // lhDao: Đối tượng truy cập dữ liệu lịch hẹn.
        private LichHenDAO lhDao;
        // pnlParentPanel: Đối tượng màn hình cha có kiểu QuanLyPhieuDichVu.
        private QuanLyPhieuDichVu pnlParentPanel;
        // pEdit: Đối tượng phiếu dịch vụ cần chỉnh sửa có kiểu PhieuDichVu.
        private PhieuDichVu pEdit;

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        // Tham số winParent: thành phần cha.
        // Tham số pnlKhung: khung.
        // Tham số pDao: đối tượng truy cập dữ liệu phiếu dịch vụ.
        // Tham số lhDao: đối tượng truy cập dữ liệu lịch hẹn.
        // Tham số pEdit: phiếu dịch vụ cần chỉnh sửa.
        public PhieuDichVuDialog(JFrame winParent, QuanLyPhieuDichVu pnlKhung, PhieuDichVuDAO pDao, LichHenDAO lhDao,
                PhieuDichVu pEdit) {
            super(winParent, pEdit == null ? "Tạo Phiếu Dịch Vụ Mới" : "Sửa Phiếu " + pEdit.getMaPhieuDichVu(), true);
            this.pnlParentPanel = pnlKhung;
            this.pDao = pDao;
            this.lhDao = lhDao;
            this.pEdit = pEdit;

            setSize(760, 640); // Modal rộng hơn, không chiếm diện tích bảng danh sách.
            setMinimumSize(new Dimension(650, 560));
            setLocationRelativeTo(winParent);
            setLayout(new BorderLayout());

            // --- PHẦN THÔNG TIN CƠ BẢN ---
            // pnlTop: Khung chứa thành phần giao diện trên.
            JPanel pnlTop = new JPanel(new GridLayout(6, 2, 10, 10));
            pnlTop.setBorder(new EmptyBorder(15, 30, 10, 30));
            pnlTop.setBackground(Color.WHITE);

            pnlTop.add(new JLabel("Mã Phiếu:"));
            txtMaPhieu = new JTextField(pEdit == null ? pDao.taoMaTuDong() : pEdit.getMaPhieuDichVu());
            txtMaPhieu.setEditable(false);
            pnlTop.add(txtMaPhieu);

            pnlTop.add(new JLabel("Liên kết Lịch Hẹn:"));
            cmbLichHen = new JComboBox<>();
            cmbLichHen.addItem("Không có");
            for (String lh : pDao.getDanhSachLichHen(pEdit == null ? null : pEdit.getMaPhieuDichVu()))
                cmbLichHen.addItem(lh);
            pnlTop.add(cmbLichHen);

            pnlTop.add(new JLabel("Khách Hàng:"));
            cmbKhachHang = new JComboBox<>();
            for (String[] kh : lhDao.getDanhSachKhachHang())
                cmbKhachHang.addItem(kh[0] + " - " + kh[1]);
            pnlTop.add(cmbKhachHang);

            pnlTop.add(new JLabel("Thú Cưng:"));
            cmbThuCung = new JComboBox<>();
            pnlTop.add(cmbThuCung);

            pnlTop.add(new JLabel("Thợ Spa:"));
            cmbNhanVien = new JComboBox<>();
            for (String[] nv : lhDao.getDanhSachNhanVienSpa())
                cmbNhanVien.addItem(nv[0] + " - " + nv[1]);
            pnlTop.add(cmbNhanVien);

            pnlTop.add(new JLabel("Trạng Thái:"));
            String trangThaiHienTai = (pEdit == null) ? "Chưa thanh toán" : pEdit.getTrangThai();
            txtFormTrangThai = new JTextField(trangThaiHienTai);
            txtFormTrangThai.setEditable(false);
            txtFormTrangThai.setBackground(new Color(240, 240, 240));
            txtFormTrangThai
                    .setToolTipText("Tự chuyển sang 'Đã thanh toán' khi thanh toán hóa đơn thành công.");
            pnlTop.add(txtFormTrangThai);

            // --- PHẦN DANH SÁCH DỊCH VỤ (CHECKBOX) ---
            // pnlCenter: Khung chứa thành phần giao diện trung tâm.
            JPanel pnlCenter = new JPanel(new BorderLayout(0, 5));
            pnlCenter.setBackground(Color.WHITE);
            pnlCenter.setBorder(new EmptyBorder(0, 30, 10, 30));

            // lblDichVuTitle: Nhãn hiển thị: Chọn Dịch Vụ:.
            JLabel lblDichVuTitle = new JLabel("Chọn Dịch Vụ:");
            lblDichVuTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            pnlCenter.add(lblDichVuTitle, BorderLayout.NORTH);

            // pnlDichVu: Khung chứa thành phần giao diện dịch vụ.
            JPanel pnlDichVu = new JPanel(new GridLayout(0, 2, 5, 5)); // 2 cột Checkbox
            pnlDichVu.setBackground(Color.WHITE);

            // Lấy danh sách dịch vụ từ DAO
            danhSachDichVu = pDao.getDanhSachDichVu();
            // selectedMaDichVu: Lưu đã chọn mã dịch vụ; phần tử có kiểu String.
            ArrayList<String> selectedMaDichVu = (pEdit == null) ? new ArrayList<>()
                    : pDao.getDichVuCuaPhieu(pEdit.getMaPhieuDichVu());

            for (String[] dv : danhSachDichVu) {
                // chkChk: Ô chọn đánh dấu ô chọn.
                JCheckBox chkChk = new JCheckBox(dv[1]);
                chkChk.setBackground(Color.WHITE);
                chkChk.setActionCommand(dv[0]); // Lưu mã Dịch vụ

                // Nếu đang Sửa phiếu và dịch vụ này đã được chọn trước đó
                if (selectedMaDichVu.contains(dv[0])) {
                    chkChk.setSelected(true);
                }

                chkDichVuList.add(chkChk);
                pnlDichVu.add(chkChk);
            }

            // scrScrollDichVu: Khung cuộn khung cuộn dịch vụ.
            JScrollPane scrScrollDichVu = new JScrollPane(pnlDichVu);
            scrScrollDichVu.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            pnlCenter.add(scrScrollDichVu, BorderLayout.CENTER);

            // Gom 2 phần Top & Center lại
            // pnlMain: Khung chứa thành phần giao diện chính.
            JPanel pnlMain = new JPanel(new BorderLayout());
            pnlMain.setBackground(Color.WHITE);
            pnlMain.add(pnlTop, BorderLayout.NORTH);
            pnlMain.add(pnlCenter, BorderLayout.CENTER);

            add(pnlMain, BorderLayout.CENTER);

            // --- NÚT BẤM (BOTTOM) ---
            // pnlBtn: Khung chứa thành phần giao diện nút.
            JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            pnlBtn.setBackground(Color.WHITE);
            pnlBtn.setBorder(new EmptyBorder(0, 0, 10, 15));

            // btnHuy: Nút thao tác: Hủy.
            JButton btnHuy = new JButton("Hủy");
            btnHuy.setBackground(GRAY_BTN);
            btnHuy.setForeground(Color.WHITE);
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnHuy.addActionListener(e -> dispose());

            // btnLuu: Nút thao tác: Lưu Phiếu.
            JButton btnLuu = new JButton("Lưu Phiếu");
            btnLuu.setBackground(DARK_BTN);
            btnLuu.setForeground(Color.WHITE);
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnLuu.addActionListener(e -> xuLyLuu());

            pnlBtn.add(btnHuy);
            pnlBtn.add(btnLuu);
            add(pnlBtn, BorderLayout.SOUTH);

            // Khi liên kết lịch hẹn, khóa ba trường để tránh chọn sai người hoặc thú cưng.
            cmbKhachHang.addActionListener(e -> {
                if (cmbKhachHang.isEnabled())
                    napThuCungTheoKhachHang();
            });
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            cmbLichHen.addActionListener(e -> capNhatTheoLichHen());

            if (pEdit != null && pEdit.getMaLichHen() != null
                    && !"Không có".equals(pEdit.getMaLichHen())) {
                cmbLichHen.setSelectedItem(pEdit.getMaLichHen());
            } else {
                cmbLichHen.setSelectedItem("Không có");
                if (pEdit != null) {
                    setCombo(cmbKhachHang, pEdit.getMaKhachHang());
                    napThuCungTheoKhachHang();
                    setCombo(cmbThuCung, pEdit.getMaThuCung());
                    setCombo(cmbNhanVien, pEdit.getMaNhanVien());
                }
            }
            capNhatTheoLichHen();
        }

        // Nạp thú cưng theo khách hàng.
        private void napThuCungTheoKhachHang() {
            // maKH: Lưu mã khách hàng.
            String maKH = layMa(cmbKhachHang);
            cmbThuCung.removeAllItems();
            for (String[] tc : lhDao.getDanhSachThuCungTheoKhachHang(maKH)) {
                cmbThuCung.addItem(tc[0] + " - " + tc[1]);
            }
        }

        // Cập nhật theo lịch hẹn.
        private void capNhatTheoLichHen() {
            String maLich = (String) cmbLichHen.getSelectedItem();
            boolean coLich = maLich != null && !"Không có".equals(maLich);
            if (coLich) {
                entity.LichHen lh = lhDao.timTheoMa(maLich);
                boolean duocChon = lh != null
                        && !"Đã hủy".equals(lh.getTrangThai())
                        && lh.getNgayGioHen() != null
                        && (!lh.getNgayGioHen().isBefore(java.time.LocalDateTime.now())
                                || (pEdit != null && maLich.equals(pEdit.getMaLichHen())));
                if (duocChon) {
                    setCombo(cmbKhachHang, lh.getMaKhachHang());
                    napThuCungTheoKhachHang();
                    setCombo(cmbThuCung, lh.getMaThuCung());
                    setCombo(cmbNhanVien, lh.getMaNhanVien());
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Lịch hẹn đã qua giờ, đã bị hủy hoặc không còn hợp lệ.",
                            "Không thể chọn lịch hẹn", JOptionPane.WARNING_MESSAGE);
                    cmbLichHen.setSelectedItem("Không có");
                    return;
                }
            } else {
                napThuCungTheoKhachHang();
            }
            cmbKhachHang.setEnabled(!coLich);
            cmbThuCung.setEnabled(!coLich);
            cmbNhanVien.setEnabled(!coLich);
            String goiY = coLich
                    ? "Thông tin tự động lấy theo lịch hẹn, không thể chỉnh sửa ở phiếu."
                    : "Phiếu khách đến trực tiếp: chọn khách hàng, thú cưng và thợ Spa.";
            cmbKhachHang.setToolTipText(goiY);
            cmbThuCung.setToolTipText(goiY);
            cmbNhanVien.setToolTipText(goiY);
        }

        // Lấy mã của mục hiện được chọn trong hộp lựa chọn.
        // Tham số cmbCbo: hộp lựa chọn.
        // Kết quả: chuỗi kết quả.
        private String layMa(JComboBox<String> cmbCbo) {
            Object item = cmbCbo.getSelectedItem();
            if (item == null)
                return null;
            // text: Lưu nội dung chữ.
            String text = item.toString();
            int pos = text.indexOf(" - ");
            return pos >= 0 ? text.substring(0, pos) : text;
        }

        // Chọn mục trong hộp lựa chọn theo mã.
        // Tham số cmbCbo: hộp lựa chọn.
        // Tham số id: mã.
        private void setCombo(JComboBox<String> cmbCbo, String id) {
            for (int i = 0; i < cmbCbo.getItemCount(); i++) {
                if (cmbCbo.getItemAt(i).startsWith(id + " - ")) {
                    cmbCbo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Xử lý lưu.
        private void xuLyLuu() {
            // Lấy danh sách dịch vụ đã tích chọn
            // dichVuDuocChon: Lưu dịch vụ được chọn; phần tử có kiểu String[].
            List<String[]> dichVuDuocChon = new ArrayList<>();
            for (int i = 0; i < chkDichVuList.size(); i++) {
                if (chkDichVuList.get(i).isSelected()) {
                    dichVuDuocChon.add(danhSachDichVu.get(i));
                }
            }

            // Validate: Phải chọn ít nhất 1 dịch vụ
            if (dichVuDuocChon.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 dịch vụ!", "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String kh = layMa(cmbKhachHang);
            String tc = layMa(cmbThuCung);
            String nv = layMa(cmbNhanVien);
            if (kh == null || tc == null || nv == null) {
                JOptionPane.showMessageDialog(this,
                        "Hãy chọn đầy đủ khách hàng, thú cưng và thợ Spa.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String maLich = (String) cmbLichHen.getSelectedItem();
            if (maLich == null)
                maLich = "Không có";

            // Neu form mo qua lau, phai kiem tra lai truoc khi luu.
            // Phieu dang sua duoc GIU NGUYEN lich da gan, ke ca khi lich qua gio.
            if (!"Không có".equals(maLich)) {
                entity.LichHen lich = lhDao.timTheoMa(maLich);
                boolean giuLichCu = pEdit != null && maLich.equals(pEdit.getMaLichHen());
                if (lich == null || "Đã hủy".equals(lich.getTrangThai())
                        || lich.getNgayGioHen() == null
                        || (!giuLichCu && lich.getNgayGioHen().isBefore(
                                java.time.LocalDateTime.now()))) {
                    JOptionPane.showMessageDialog(this,
                            "Lịch hẹn đã qua giờ, bị hủy hoặc không còn tồn tại. "
                                    + "Hãy chọn lịch hẹn khác hoặc 'Không có'.",
                            "Không thể lưu phiếu", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            PhieuDichVu p = new PhieuDichVu(txtMaPhieu.getText(),
                    pEdit == null ? "Chưa thanh toán" : pEdit.getTrangThai(), kh, tc, nv,
                    maLich);

            boolean success = (pEdit == null) ? pDao.them(p, dichVuDuocChon) : pDao.sua(p, dichVuDuocChon);

            if (success) {
                JOptionPane.showMessageDialog(this, "Thành công!");
                pnlParentPanel.loadData();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}