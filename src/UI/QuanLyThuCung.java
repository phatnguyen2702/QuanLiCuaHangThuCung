package UI;

import dao.ThuCungDAO;
import dao.ThuCungDAO.ChuNuoi;
import dao.ThuCungDAO.HoSoThuCung;
import entity.ThuCung;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Quản lý hồ sơ thú cưng: tên thú cưng không bắt buộc, liên kết chủ nuôi đúng
 * mã,
 * và hiển thị đầy đủ chi tiết khi người dùng chọn một dòng trong bảng.
 */
public class QuanLyThuCung extends JPanel {
    // NAVY: Màu dùng cho giao diện: xanh đậm.
    private static final Color NAVY = new Color(18, 48, 82);
    // GREEN: Màu dùng cho giao diện: xanh lá.
    private static final Color GREEN = new Color(72, 196, 62);
    // BLUE: Màu dùng cho giao diện: xanh dương.
    private static final Color BLUE = new Color(91, 180, 235);
    // LIGHT_BLUE: Màu dùng cho giao diện: xanh dương.
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);

    // dao: Đối tượng truy cập dữ liệu thú cưng.
    private final ThuCungDAO dao = new ThuCungDAO();
    // dsTatCa: Lưu toàn bộ danh sách; phần tử có kiểu HoSoThuCung.
    private final ArrayList<HoSoThuCung> dsTatCa = new ArrayList<>();
    // dsHienThi: Lưu danh sách đang hiển thị; phần tử có kiểu HoSoThuCung.
    private final ArrayList<HoSoThuCung> dsHienThi = new ArrayList<>();

    // txtTimKiem: Ô nhập/hiển thị văn bản tìm kiếm.
    // txtMaTC: Ô nhập/hiển thị văn bản mã thú cưng.
    // txtTenTC: Ô nhập/hiển thị văn bản tên TC.
    // txtCanNang: Ô nhập/hiển thị văn bản cân nặng.
    // txtGiong: Ô nhập/hiển thị văn bản giống thú cưng.
    private JTextField txtTimKiem, txtMaTC, txtTenTC, txtCanNang, txtGiong;
    // txtTenKhachHang: Ô nhập/hiển thị văn bản tên khách hàng.
    // txtMaKH: Ô nhập/hiển thị văn bản mã khách hàng.
    private JTextField txtTenKhachHang, txtMaKH;
    // cmbLocGioiTinh: Hộp lựa chọn lọc giới tính.
    // cmbLocLoai: Hộp lựa chọn lọc loài.
    private JComboBox<String> cmbLocGioiTinh, cmbLocLoai;
    // cmbLoai2: Hộp lựa chọn loài/loại.
    // cmbGioiTinh2: Hộp lựa chọn giới tính.
    private JComboBox<String> cmbLoai2, cmbGioiTinh2;
    // txtTinhTrangLich: Ô nhập/hiển thị văn bản tình trạng lịch hẹn.
    private JTextField txtTinhTrangLich;
    // lblFormTitle: Nhãn hiển thị tiêu đề biểu mẫu.
    // lblChiTietChuNuoi: Nhãn hiển thị chi tiết chủ nuôi.
    private JLabel lblFormTitle, lblChiTietChuNuoi;
    // btnLuu: Nút thao tác lưu.
    // btnXoaHoSo: Nút thao tác xóa hồ sơ.
    // btnBoChon: Nút thao tác bỏ chọn.
    private JButton btnLuu, btnXoaHoSo, btnBoChon;
    // winHoSo: Hộp thoại hồ sơ.
    private JDialog winHoSo;
    // hoSoDangSua: Đối tượng hồ sơ thú cưng đang sửa có kiểu HoSoThuCung.
    private HoSoThuCung hoSoDangSua;
    // tblDuLieu: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
    private JTable tblDuLieu;
    // tableModel: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private DefaultTableModel tableModel;
    // chuDaChon: Đối tượng chủ nuôi đã chọn có kiểu ChuNuoi.
    private ChuNuoi chuDaChon;
    // dangNapChu: Cờ cho biết đang nạp danh sách chủ nuôi.
    private boolean dangNapChu = false;
    // isEditMode: Cờ cho biết đang ở chế độ chỉnh sửa.
    private boolean isEditMode = false;

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    public QuanLyThuCung() {
        setLayout(new BorderLayout(18, 15));
        setBackground(LIGHT_BLUE);
        setBorder(new EmptyBorder(18, 24, 18, 24));

        // pnlDau: Khung chứa thành phần giao diện đầu.
        JPanel pnlDau = new JPanel(new BorderLayout(12, 0));
        pnlDau.setOpaque(false);
        // lblTitle: Nhãn hiển thị: QUẢN LÝ HỒ SƠ THÚ CƯNG.
        JLabel lblTitle = new JLabel("QUẢN LÝ HỒ SƠ THÚ CƯNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        // btnThem: Nút thao tác thêm.
        JButton btnThem = nut("+ Thêm thú cưng", GREEN);
        btnThem.setPreferredSize(new Dimension(175, 39));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnThem.addActionListener(e -> moDialogHoSo(null));
        pnlDau.add(lblTitle, BorderLayout.WEST);
        pnlDau.add(btnThem, BorderLayout.EAST);
        add(pnlDau, BorderLayout.NORTH);
        add(createLeftPanel(), BorderLayout.CENTER);
        lamMoiDanhSach();
        // Khi quay lại từ Lịch hẹn Spa, tự tải lại trạng thái Có lịch hẹn/Chưa có.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) {
                lamMoiDanhSach();
            }
        });
    }

    // Tạo khung bên trái.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createLeftPanel() {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new BorderLayout(0, 10));
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setBorder(new EmptyBorder(15, 15, 15, 15));

        // pnlTim: Khung chứa thành phần giao diện tìm.
        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        pnlTim.setOpaque(false);
        txtTimKiem = new JTextField(13);
        txtTimKiem.setPreferredSize(new Dimension(170, 34));
        txtTimKiem.setToolTipText("Tìm mã/tên/giống thú cưng hoặc tên/SĐT chủ nuôi, không phân biệt dấu");
        cmbLocGioiTinh = new JComboBox<>(new String[] { "Giới tính", "Đực", "Cái" });
        cmbLocLoai = new JComboBox<>(new String[] { "Loại", "Chó", "Mèo", "Chim", "Khác" });
        cmbLocGioiTinh.setPreferredSize(new Dimension(92, 34));
        cmbLocLoai.setPreferredSize(new Dimension(90, 34));
        // btnTim: Nút thao tác tìm.
        JButton btnTim = nut("Tìm kiếm", GREEN);
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTim.addActionListener(e -> locDuLieu());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        txtTimKiem.addActionListener(e -> locDuLieu());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbLocGioiTinh.addActionListener(e -> locDuLieu());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbLocLoai.addActionListener(e -> locDuLieu());
        pnlTim.add(txtTimKiem);
        pnlTim.add(cmbLocGioiTinh);
        pnlTim.add(cmbLocLoai);
        pnlTim.add(btnTim);
        pnlKhung.add(pnlTim, BorderLayout.NORTH);

        // cot: Mảng lưu cột.
        String[] cot = { "Mã TC", "Tên thú cưng", "Loài", "Giống",
                "Giới tính", "Cân nặng", "Chủ nuôi", "Số điện thoại", "Lịch hẹn" };
        tableModel = new DefaultTableModel(cot, 0) {
            // Không cho phép sửa trực tiếp các ô trong bảng.
            // Tham số r: chỉ số dòng.
            // Tham số cmbC: chỉ số cột.
            // Kết quả: false, bảng chỉ dùng để xem.
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblDuLieu = new JTable(tableModel);
        tblDuLieu.setRowHeight(39);
        tblDuLieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblDuLieu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDuLieu.getTableHeader().setBackground(NAVY);
        tblDuLieu.getTableHeader().setForeground(Color.WHITE);
        tblDuLieu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDuLieu.getTableHeader().setPreferredSize(new Dimension(100, 38));
        // rongCot: Mảng lưu rỗng cột.
        int[] rongCot = { 80, 145, 75, 140, 85, 96, 180, 140, 140 };
        for (int i = 0; i < rongCot.length; i++) {
            tblDuLieu.getColumnModel().getColumn(i).setPreferredWidth(rongCot[i]);
        }
        DefaultTableCellRenderer lblCanGiua = new DefaultTableCellRenderer();
        lblCanGiua.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[] { 0, 2, 4, 5, 8 }) {
            tblDuLieu.getColumnModel().getColumn(i).setCellRenderer(lblCanGiua);
        }
        // Không còn cột Thao tác: toàn bộ nút Lưu / Xóa / Đóng nằm trong modal.
        tblDuLieu.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblDuLieu.setToolTipText("Nhấp một lần vào dòng để xem chi tiết, sửa hoặc xóa hồ sơ.");
        tblDuLieu.addMouseListener(new java.awt.event.MouseAdapter() {
            // Xử lý thao tác nhấn chuột trên thành phần đã gắn listener.
            // Tham số e: thông tin sự kiện.
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e))
                    return;
                // row: Lưu chỉ số dòng.
                int row = tblDuLieu.rowAtPoint(e.getPoint());
                if (row < 0)
                    return;
                int i = tblDuLieu.convertRowIndexToModel(row);
                if (i >= 0 && i < dsHienThi.size()) {
                    moDialogHoSo(dsHienThi.get(i));
                }
            }
        });
        // scrBang: Khung cuộn bảng dữ liệu.
        JScrollPane scrBang = new JScrollPane(tblDuLieu);
        scrBang.getViewport().setBackground(Color.WHITE);
        pnlKhung.add(scrBang, BorderLayout.CENTER);

        // pnlDuoi: Khung chứa thành phần giao diện dưới.
        JPanel pnlDuoi = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlDuoi.setOpaque(false);
        // btnLamMoi: Nút thao tác làm mới.
        JButton btnLamMoi = nut("↻ Làm mới", BLUE);
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLamMoi.addActionListener(e -> {
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtTimKiem.setText("");
            cmbLocGioiTinh.setSelectedIndex(0);
            cmbLocLoai.setSelectedIndex(0);
            lamMoiDanhSach();
        });
        pnlDuoi.add(btnLamMoi);
        pnlKhung.add(pnlDuoi, BorderLayout.SOUTH);
        return pnlKhung;
    }

    // Tạo khung bên phải.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel createRightPanel() {
        // pnlKhung: Khung dùng để nhóm và bố trí các thành phần giao diện.
        JPanel pnlKhung = new JPanel(new BorderLayout(0, 8));
        pnlKhung.setBackground(Color.WHITE);
        pnlKhung.setPreferredSize(new Dimension(670, 610));
        pnlKhung.setBorder(new EmptyBorder(13, 14, 12, 14));

        lblFormTitle = new JLabel("Thêm hồ sơ thú cưng", SwingConstants.CENTER);
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblFormTitle.setBorder(new EmptyBorder(3, 0, 6, 0));
        pnlKhung.add(lblFormTitle, BorderLayout.NORTH);

        // pnlForm: Khung chứa thành phần giao diện biểu mẫu.
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(4, 1, 6, 3));
        // gbc: Ràng buộc vị trí, kích thước và khoảng cách khi bố trí thành phần bằng GridBagLayout.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 3, 5, 3);
        gbc.weightx = 1.0;
        // row: Lưu chỉ số dòng.
        int row = 0;

        txtMaTC = textField();
        txtMaTC.setEditable(false);
        txtMaTC.setBackground(new Color(245, 247, 250));
        themTruong(pnlForm, gbc, "Mã thú cưng", txtMaTC, row++);

        txtTenTC = textField();
        txtTenTC.setToolTipText("Có thể để trống: hệ thống tự tạo từ loài + giống");
        themTruong(pnlForm, gbc, "Tên thú cưng", txtTenTC, row++);

        cmbLoai2 = combo(new String[] { "Chó", "Mèo", "Chim", "Khác" });
        themTruong(pnlForm, gbc, "Loại", cmbLoai2, row++);
        txtGiong = textField();
        themTruong(pnlForm, gbc, "Giống", txtGiong, row++);
        cmbGioiTinh2 = combo(new String[] { "Đực", "Cái" });
        themTruong(pnlForm, gbc, "Giới tính", cmbGioiTinh2, row++);
        txtCanNang = textField();
        themTruong(pnlForm, gbc, "Cân nặng (kg)", txtCanNang, row++);

        txtTenKhachHang = textField();
        // btnChonKH: Nút thao tác chọn KH.
        JButton btnChonKH = nut("Chọn", NAVY);
        btnChonKH.setPreferredSize(new Dimension(67, 34));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnChonKH.addActionListener(e -> chonKhachHang());
        // pnlTenKH: Khung chứa thành phần giao diện tên KH.
        JPanel pnlTenKH = new JPanel(new BorderLayout(5, 0));
        pnlTenKH.setOpaque(false);
        pnlTenKH.add(txtTenKhachHang, BorderLayout.CENTER);
        pnlTenKH.add(btnChonKH, BorderLayout.EAST);
        themTruong(pnlForm, gbc, "Tên khách hàng", pnlTenKH, row++);
        txtTenKhachHang.setToolTipText("Nhập tên không cần đúng dấu/chữ hoa, hoặc nhấn Chọn");
        txtTenKhachHang.getDocument().addDocumentListener(new DocumentListener() {
            // Xử lý thay đổi dữ liệu của màn hình.
            private void thayDoi() {
                if (!dangNapChu) {
                    chuDaChon = null;
                    // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                    txtMaKH.setText("");
                    // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                    lblChiTietChuNuoi.setText("Chưa xác định chủ nuôi");
                }
            }

            // Cập nhật trạng thái liên quan khi người dùng thêm ký tự vào ô văn bản.
            // Tham số e: thông tin sự kiện.
            @Override
            public void insertUpdate(DocumentEvent e) {
                thayDoi();
            }

            // Cập nhật trạng thái liên quan khi người dùng xóa ký tự trong ô văn bản.
            // Tham số e: thông tin sự kiện.
            @Override
            public void removeUpdate(DocumentEvent e) {
                thayDoi();
            }

            // Nhận thông báo khi thuộc tính của tài liệu văn bản thay đổi.
            // Tham số e: thông tin sự kiện.
            @Override
            public void changedUpdate(DocumentEvent e) {
                thayDoi();
            }
        });

        txtMaKH = textField();
        txtMaKH.setEditable(false);
        txtMaKH.setBackground(new Color(245, 247, 250));
        themTruong(pnlForm, gbc, "Mã khách hàng", txtMaKH, row++);

        txtTinhTrangLich = textField();
        txtTinhTrangLich.setEditable(false);
        txtTinhTrangLich.setBackground(new Color(245, 247, 250));
        txtTinhTrangLich.setToolTipText("Tự xác định theo lịch hẹn sắp tới chưa hủy trong SQL Server.");
        themTruong(pnlForm, gbc, "Lịch hẹn Spa", txtTinhTrangLich, row++);

        // pnlThongTinChu: Khung chứa thành phần giao diện thông tin chữ.
        JPanel pnlThongTinChu = new JPanel(new BorderLayout(0, 3));
        pnlThongTinChu.setOpaque(false);
        pnlThongTinChu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 223, 233)),
                new EmptyBorder(7, 9, 7, 9)));
        // lblNhom: Nhãn hiển thị: THÔNG TIN CHỦ NUÔI.
        JLabel lblNhom = new JLabel("THÔNG TIN CHỦ NUÔI");
        lblNhom.setForeground(NAVY);
        lblNhom.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChiTietChuNuoi = new JLabel("Chưa xác định chủ nuôi");
        lblChiTietChuNuoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlThongTinChu.add(lblNhom, BorderLayout.NORTH);
        pnlThongTinChu.add(lblChiTietChuNuoi, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        pnlForm.add(pnlThongTinChu, gbc);
        gbc.gridwidth = 1;

        // Nhiều trường: cho phép cuộn để không bị cắt khi cửa sổ nhỏ.
        // scrForm: Khung cuộn biểu mẫu.
        JScrollPane scrForm = new JScrollPane(pnlForm,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrForm.setBorder(null);
        scrForm.getVerticalScrollBar().setUnitIncrement(14);
        pnlKhung.add(scrForm, BorderLayout.CENTER);

        btnLuu = nut("Thêm hồ sơ", GREEN);
        btnLuu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLuu.addActionListener(e -> xuLyLuuDuLieu());
        btnXoaHoSo = nut("Xóa hồ sơ", new Color(210, 60, 76));
        btnXoaHoSo.setPreferredSize(new Dimension(130, 38));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnXoaHoSo.addActionListener(e -> {
            if (hoSoDangSua != null) {
                xoaThuCung(hoSoDangSua.thuCung.getMaThuCung());
            }
        });
        btnBoChon = nut("Đóng", new Color(105, 117, 133));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnBoChon.addActionListener(e -> {
            if (winHoSo != null)
                winHoSo.dispose();
        });
        btnLuu.setPreferredSize(new Dimension(150, 38));
        btnBoChon.setPreferredSize(new Dimension(110, 38));
        // pnlNut: Khung chứa thành phần giao diện nút.
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        pnlNut.setOpaque(false);
        pnlNut.setBorder(new EmptyBorder(6, 2, 0, 2));
        pnlNut.add(btnLuu);
        pnlNut.add(btnXoaHoSo);
        pnlNut.add(btnBoChon);
        pnlKhung.add(pnlNut, BorderLayout.SOUTH);
        return pnlKhung;
    }

    // Tạo ô nhập văn bản theo kiểu giao diện của màn hình.
    // Kết quả: đối tượng/giá trị kiểu JTextField.
    private static JTextField textField() {
        // txtVanBan2: Ô nhập/hiển thị văn bản txtVanBan2.
        JTextField txtVanBan2 = new JTextField();
        txtVanBan2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtVanBan2.setPreferredSize(new Dimension(160, 34));
        txtVanBan2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(145, 162, 180)),
                new EmptyBorder(0, 8, 0, 8)));
        return txtVanBan2;
    }

    // Tạo hộp lựa chọn từ danh sách giá trị.
    // Tham số options: các lựa chọn.
    // Kết quả: đối tượng/giá trị kiểu JComboBox<String>.
    private static JComboBox<String> combo(String[] options) {
        // cmbC: Hộp lựa chọn cột/thành phần được xử lý.
        JComboBox<String> cmbC = new JComboBox<>(options);
        cmbC.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbC.setPreferredSize(new Dimension(160, 34));
        cmbC.setBackground(Color.WHITE);
        return cmbC;
    }

    // Tạo nút thao tác với nội dung và màu sắc được chỉ định.
    // Tham số ten: tên.
    // Tham số nen: màu nền.
    // Kết quả: đối tượng/giá trị kiểu JButton.
    private static JButton nut(String ten, Color nen) {
        // btnThaoTac2: Nút nhận thao tác nhấn của người dùng.
        JButton btnThaoTac2 = new JButton(ten);
        btnThaoTac2.setBackground(nen);
        btnThaoTac2.setForeground(Color.WHITE);
        btnThaoTac2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnThaoTac2.setFocusPainted(false);
        btnThaoTac2.setPreferredSize(new Dimension(100, 34));
        btnThaoTac2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btnThaoTac2;
    }

    // Thêm trường nhập liệu.
    // Tham số pnlKhung: khung.
    // Tham số gbc: ràng buộc bố trí.
    // Tham số label: nhãn.
    // Tham số comp: thành phần.
    // Tham số row: chỉ số dòng.
    private static void themTruong(JPanel pnlKhung, GridBagConstraints gbc, String label, JComponent comp, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.31;
        // lblLbl: Nhãn hiển thị nhãn.
        JLabel lblLbl = new JLabel(label);
        lblLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlKhung.add(lblLbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.69;
        pnlKhung.add(comp, gbc);
    }

    // Làm mới danh sách.
    private void lamMoiDanhSach() {
        dsTatCa.clear();
        dsTatCa.addAll(dao.getAllChiTiet());
        locDuLieu();
    }

    // Bỏ dấu tiếng Việt và chuyển thành chữ thường để so sánh hoặc lọc.
    // Tham số s: chuỗi đầu vào.
    // Kết quả: chuỗi kết quả.
    private static String boDauVaThuong(String s) {
        if (s == null)
            return "";
        String normal = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
        return normal.replace('đ', 'd').trim().replaceAll("\\s+", " ");
    }

    // Lọc dữ liệu.
    private void locDuLieu() {
        if (tableModel == null)
            return;
        // q: Đọc nội dung chữ từ ô nhập để xử lý q.
        String q = boDauVaThuong(txtTimKiem.getText());
        String gt = String.valueOf(cmbLocGioiTinh.getSelectedItem());
        // loai: Lưu loài/loại.
        String loai = String.valueOf(cmbLocLoai.getSelectedItem());
        dsHienThi.clear();
        tableModel.setRowCount(0);
        for (HoSoThuCung hs : dsTatCa) {
            ThuCung tc = hs.thuCung;
            String haystack = boDauVaThuong(tc.getMaThuCung() + " " + tc.getTenThuCung()
                    + " " + tc.getLoai() + " " + tc.getGiong() + " "
                    + hs.tenKhachHang + " " + hs.soDienThoai);
            if (!haystack.contains(q))
                continue;
            if (!"Giới tính".equals(gt) && !gt.equals(tc.getGioiTinh()))
                continue;
            if (!"Loại".equals(loai) && !loai.equals(tc.getLoai()))
                continue;
            dsHienThi.add(hs);
            tableModel.addRow(new Object[] {
                    tc.getMaThuCung(), tc.getTenThuCung(), tc.getLoai(),
                    tc.getGiong(), tc.getGioiTinh(), String.format(Locale.US, "%.2f kg", tc.getCanNang()),
                    hs.tenKhachHang == null ? "" : hs.tenKhachHang,
                    hs.soDienThoai == null ? "" : hs.soDienThoai,
                    hs.coLichHen ? "Có lịch hẹn" : "Chưa có"
            });
        }
    }

    // Gán chủ nuôi.
    // Tham số kh: chủ nuôi cần xử lý.
    private void ganChuNuoi(ChuNuoi kh) {
        dangNapChu = true;
        try {
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtTenKhachHang.setText(kh == null ? "" : kh.hoTen);
        } finally {
            dangNapChu = false;
        }
        chuDaChon = kh;
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtMaKH.setText(kh == null ? "" : kh.maKhachHang);
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblChiTietChuNuoi.setText(kh == null ? "Chưa xác định chủ nuôi"
                : "<html>" + html(kh.hoTen) + "<br>Mã: " + html(kh.maKhachHang)
                        + " &nbsp;|&nbsp; SĐT: " + html(kh.soDienThoai) + "</html>");
    }

    // Mã hóa các ký tự đặc biệt trước khi chèn chuỗi vào HTML.
    // Tham số s: chuỗi đầu vào.
    // Kết quả: chuỗi kết quả.
    private static String html(String s) {
        return s == null ? ""
                : s.replace("&", "&amp;")
                        .replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** Chọn chính xác mã khách hàng khi nhiều người trùng tên. */
    private ChuNuoi chonTrongDanhSach(List<ChuNuoi> ds, String title) {
        DefaultListModel<ChuNuoi> model = new DefaultListModel<>();
        for (ChuNuoi c : ds)
            model.addElement(c);
        // lstDanhSach: Danh sách lựa chọn danh sách.
        JList<ChuNuoi> lstDanhSach = new JList<>(model);
        lstDanhSach.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstDanhSach.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (!ds.isEmpty())
            lstDanhSach.setSelectedIndex(0);
        // scrCuon2: Khung cuộn khung cuộn.
        JScrollPane scrCuon2 = new JScrollPane(lstDanhSach);
        scrCuon2.setPreferredSize(new Dimension(485, 230));
        int ret = JOptionPane.showConfirmDialog(this, scrCuon2, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        return ret == JOptionPane.OK_OPTION ? lstDanhSach.getSelectedValue() : null;
    }

    // Chọn khách hàng.
    private void chonKhachHang() {
        // tuKhoa: Đọc nội dung chữ từ ô nhập để xử lý từ khóa tìm kiếm.
        String tuKhoa = boDauVaThuong(txtTenKhachHang.getText());
        // khop: Lưu khớp; phần tử có kiểu ChuNuoi.
        ArrayList<ChuNuoi> khop = new ArrayList<>();
        for (ChuNuoi c : dao.getDanhSachChuNuoi()) {
            if (tuKhoa.isEmpty() || boDauVaThuong(c.hoTen).contains(tuKhoa)
                    || boDauVaThuong(c.maKhachHang).contains(tuKhoa)
                    || boDauVaThuong(c.soDienThoai).contains(tuKhoa)) {
                khop.add(c);
            }
        }
        if (khop.isEmpty()) {
            canhBao("Không tìm thấy khách hàng. Vui lòng thêm khách hàng trước khi đăng ký thú cưng.");
            return;
        }
        ChuNuoi c = chonTrongDanhSach(khop, "Chọn đúng khách hàng (mã / họ tên / số điện thoại)");
        if (c != null)
            ganChuNuoi(c);
    }

    // Xác định chủ nuôi.
    // Kết quả: đối tượng/giá trị kiểu ChuNuoi.
    private ChuNuoi xacDinhChuNuoi() {
        if (chuDaChon != null && chuDaChon.maKhachHang.equals(txtMaKH.getText())) {
            return chuDaChon;
        }
        // tenNhap: Đọc nội dung chữ từ ô nhập để xử lý tên nhập.
        String tenNhap = boDauVaThuong(txtTenKhachHang.getText());
        if (tenNhap.isBlank()) {
            canhBao("Vui lòng nhập hoặc chọn tên khách hàng.");
            return null;
        }
        // khop: Lưu khớp; phần tử có kiểu ChuNuoi.
        ArrayList<ChuNuoi> khop = new ArrayList<>();
        for (ChuNuoi c : dao.getDanhSachChuNuoi()) {
            if (boDauVaThuong(c.hoTen).equals(tenNhap))
                khop.add(c);
        }
        if (khop.size() == 1) {
            ganChuNuoi(khop.get(0));
            return khop.get(0);
        }
        if (khop.size() > 1) {
            ChuNuoi c = chonTrongDanhSach(khop,
                    "Có nhiều khách trùng tên, vui lòng chọn theo mã / số điện thoại");
            if (c != null)
                ganChuNuoi(c);
            return c;
        }
        canhBao("Chưa tìm được khách hàng có tên này. Bạn có thể nhập không dấu/không đúng hoa thường, "
                + "nhưng khách hàng phải tồn tại trong hệ thống. Nhấn Chọn để tìm theo tên gần đúng.");
        return null;
    }

    // Xóa thông tin đang nhập và đặt lại trạng thái biểu mẫu.
    private void resetForm() {
        isEditMode = false;
        hoSoDangSua = null;
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtMaTC.setText(dao.taoMaThuCungTuDong());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtTenTC.setText("");
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtGiong.setText("");
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtCanNang.setText("");
        cmbLoai2.setSelectedIndex(0);
        cmbGioiTinh2.setSelectedIndex(0);
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtTinhTrangLich.setText("Chưa có");
        ganChuNuoi(null);
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblFormTitle.setText("Thêm hồ sơ thú cưng");
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        btnLuu.setText("Thêm hồ sơ");
        btnLuu.setBackground(GREEN);
        btnXoaHoSo.setVisible(false);
        btnBoChon.setEnabled(true);
    }

    // Hiển thị thông tin chi tiết.
    // Tham số hs: hồ sơ.
    private void hienThongTinChiTiet(HoSoThuCung hs) {
        ThuCung tc = hs.thuCung;
        isEditMode = true;
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtMaTC.setText(tc.getMaThuCung());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtTenTC.setText(tc.getTenThuCung() == null ? "" : tc.getTenThuCung());
        cmbLoai2.setSelectedItem(tc.getLoai());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtGiong.setText(tc.getGiong() == null ? "" : tc.getGiong());
        cmbGioiTinh2.setSelectedItem(tc.getGioiTinh());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtCanNang.setText(tc.getCanNang() <= 0 ? "" : String.valueOf(tc.getCanNang()));
        hoSoDangSua = hs;
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtTinhTrangLich.setText(hs.coLichHen ? "Có lịch hẹn" : "Chưa có");
        ganChuNuoi(new ChuNuoi(tc.getMaKhachHang(), hs.tenKhachHang, hs.soDienThoai));
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        lblFormTitle.setText("Thông tin chi tiết thú cưng");
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        btnLuu.setText("Lưu thay đổi");
        btnLuu.setBackground(NAVY);
        btnXoaHoSo.setVisible(true);
        btnBoChon.setEnabled(true);
    }

    /**
     * Form thêm/sửa được hiển thị trong modal, bảng danh sách chiếm hết màn hình.
     */
    private void moDialogHoSo(HoSoThuCung hoSo) {
        Window winOwner = SwingUtilities.getWindowAncestor(this);
        winHoSo = new JDialog(winOwner,
                hoSo == null ? "Thêm hồ sơ thú cưng"
                        : "Chi tiết / Sửa thú cưng - "
                                + hoSo.thuCung.getMaThuCung(),
                Dialog.ModalityType.APPLICATION_MODAL);
        winHoSo.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        winHoSo.setContentPane(createRightPanel());
        if (hoSo == null)
            resetForm();
        else
            hienThongTinChiTiet(hoSo);
        winHoSo.setSize(740, 690);
        winHoSo.setMinimumSize(new Dimension(610, 560));
        winHoSo.setLocationRelativeTo(this);
        winHoSo.getRootPane().setDefaultButton(btnLuu);
        winHoSo.setVisible(true);
        winHoSo = null;
    }

    // Cảnh báo dữ liệu của màn hình.
    // Tham số msg: thông báo.
    private void canhBao(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Dữ liệu chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    // Xử lý lưu dữ liệu.
    private void xuLyLuuDuLieu() {
        // loai: Lưu loài/loại.
        String loai = String.valueOf(cmbLoai2.getSelectedItem());
        // giong: Đọc nội dung chữ từ ô nhập để xử lý giống thú cưng.
        String giong = txtGiong.getText().trim();
        // ten: Đọc nội dung chữ từ ô nhập để xử lý tên.
        String ten = txtTenTC.getText().trim();
        if (ten.isEmpty()) {
            ten = loai + " - " + (giong.isEmpty() ? "Chưa rõ giống" : giong);
        }
        if (ten.length() > 50) {
            canhBao("Tên thú cưng không được quá 50 ký tự.");
            return;
        }
        if (giong.length() > 25) {
            canhBao("Giống thú cưng không được quá 25 ký tự.");
            return;
        }
        // chuoiCanNang: Đọc nội dung chữ từ ô nhập để xử lý chuỗi cân nặng.
        String chuoiCanNang = txtCanNang.getText().trim().replace(',', '.');
        // canNang: Lưu cân nặng.
        double canNang;
        try {
            canNang = Double.parseDouble(chuoiCanNang);
        } catch (NumberFormatException ex) {
            canhBao("Vui lòng nhập cân nặng là số, ví dụ 3.5 (kg).");
            return;
        }
        if (!Double.isFinite(canNang) || canNang <= 0 || canNang > 999.99) {
            canhBao("Cân nặng phải lớn hơn 0 và không quá 999,99 kg.");
            return;
        }
        ChuNuoi c = xacDinhChuNuoi();
        if (c == null)
            return;

        // Không chuyển chủ nuôi khi đã có lịch sử nghiệp vụ gắn với thú cưng.
        if (isEditMode) {
            for (HoSoThuCung cu : dsTatCa) {
                if (cu.thuCung.getMaThuCung().equals(txtMaTC.getText())
                        && !cu.thuCung.getMaKhachHang().equals(c.maKhachHang)) {
                    if (dao.daCoLichSu(txtMaTC.getText())) {
                        canhBao("Thú cưng đã có lịch hẹn, phiếu dịch vụ hoặc hóa đơn. "
                                + "Không thể đổi chủ trực tiếp vì sẽ sai lịch sử. "
                                + "Vui lòng giữ chủ cũ và lập hồ sơ chuyển chủ riêng nếu cần.");
                        return;
                    }
                    int xacNhan = JOptionPane.showConfirmDialog(this,
                            "Bạn đang đổi chủ nuôi của thú cưng chưa có lịch sử. Tiếp tục?",
                            "Xác nhận đổi chủ", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (xacNhan != JOptionPane.YES_OPTION)
                        return;
                }
            }
        }
        // Giữ nguyên trạng thái hồ sơ cũ; "Có lịch hẹn" được tính từ LichHen,
        // không ghi đè vào ThuCung.trangThai.
        String trangThaiHoSo = isEditMode && hoSoDangSua != null
                ? hoSoDangSua.thuCung.getTrangThai()
                : "Đang theo dõi";
        ThuCung tc = new ThuCung(txtMaTC.getText(), ten, loai, giong,
                String.valueOf(cmbGioiTinh2.getSelectedItem()), canNang,
                c.maKhachHang, trangThaiHoSo);
        boolean thanhCong = isEditMode ? dao.sua(tc) : dao.them(tc);
        if (!thanhCong) {
            JOptionPane.showMessageDialog(this,
                    "Không lưu được hồ sơ. Hãy kiểm tra mã khách hàng, kết nối SQL Server và thông báo Console.",
                    "Lưu thất bại", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
                isEditMode ? "Đã cập nhật hồ sơ thú cưng." : "Đã thêm hồ sơ thú cưng.");
        lamMoiDanhSach();
        if (winHoSo != null)
            winHoSo.dispose();
    }

    // Xóa thú cưng.
    // Tham số maTC: mã thú cưng.
    private void xoaThuCung(String maTC) {
        int ret = JOptionPane.showConfirmDialog(this,
                "Xóa thú cưng " + maTC + "?\nHồ sơ đã có lịch hẹn, phiếu dịch vụ "
                        + "hoặc hóa đơn sẽ không thể xóa để giữ lịch sử.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ret != JOptionPane.YES_OPTION)
            return;
        if (dao.xoa(maTC)) {
            JOptionPane.showMessageDialog(this, "Đã xóa hồ sơ thú cưng.");
            lamMoiDanhSach();
            if (winHoSo != null)
                winHoSo.dispose();
        } else {
            canhBao("Không thể xóa hồ sơ có lịch hẹn, phiếu dịch vụ hoặc hóa đơn. "
                    + "Hãy giữ hồ sơ để bảo toàn lịch sử nghiệp vụ.");
        }
    }

}
