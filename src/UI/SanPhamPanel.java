package UI;

import UI.TrangChu.PhienLamViec;
import UI.TrangChu.UIStyle;
import dao.SanPhamDAO;
import entity.DanhMuc;
import entity.NhaCungCap;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Man hinh "San pham" - Quan ly san pham (nam trong khung TrangChu).
 * Form Them / Sua san pham la lop long SanPhamDialog o cuoi file.
 */
public class SanPhamPanel extends JFrame implements ActionListener, MouseListener {
    @Override public void actionPerformed(ActionEvent e) { }
    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mousePressed(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }

    // dao: Đối tượng truy cập dữ liệu sản phẩm.
    private final SanPhamDAO dao = new SanPhamDAO();

    // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Ảnh", "Mã SP", "Tên sản phẩm", "Danh mục",
                    "Nhà cung cấp", "Giá nhập", "Giá bán", "Tồn" },
            0) {
        // Không cho phép sửa trực tiếp các ô trong bảng.
        // Tham số row: chỉ số dòng.
        // Tham số col: chỉ số cột.
        // Kết quả: false, bảng chỉ dùng để xem.
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    // tblDuLieu2: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
    private final JTable tblDuLieu2 = new JTable(model);
    // txtTim: Ô nhập/hiển thị văn bản tìm.
    private final JTextField txtTim = UIStyle.o(16);
    // cmbDanhMuc: Hộp lựa chọn danh mục.
    private final JComboBox<Muc> cmbDanhMuc = new JComboBox<>();
    // lblSoLuong2: Nhãn hiển thị: 0 sản phẩm.
    private final JLabel lblSoLuong2 = new JLabel("0 sản phẩm");

    // dsHienTai: Lưu danh sách hiện tại; phần tử có kiểu SanPham.
    private ArrayList<SanPham> dsHienTai = new ArrayList<>();
    // dangNapCombo: Cờ cho biết đang nạp danh sách lựa chọn.
    private boolean dangNapCombo = false;

    /** Renderer ảnh lấy cùng quy ước mã sản phẩm như màn gợi ý thức ăn. */
    private static class ImageRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        ImageRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int column) {
            setBackground(selected ? table.getSelectionBackground() : table.getBackground());
            String code = value == null ? "" : value.toString();
            Icon icon = loadProductImage(code, 76, 64);
            setIcon(icon);
            setText(icon == null ? "Chưa có ảnh" : "");
            setFont(table.getFont().deriveFont(10f));
            return this;
        }
    }

    private static Icon loadProductImage(String code, int maxWidth, int maxHeight) {
        if (code == null || !code.matches("[A-Za-z0-9_-]+")) return null;
        for (String ext : new String[] { ".png", ".jpg", ".jpeg" }) {
            File file = new File("src/img/products", code + ext);
            if (!file.isFile()) file = new File("img/products", code + ext);
            if (!file.isFile()) continue;
            ImageIcon source = new ImageIcon(file.getAbsolutePath());
            if (source.getIconWidth() <= 0 || source.getIconHeight() <= 0) continue;
            double scale = Math.min((double) maxWidth / source.getIconWidth(),
                    (double) maxHeight / source.getIconHeight());
            int w = Math.max(1, (int) (source.getIconWidth() * scale));
            int h = Math.max(1, (int) (source.getIconHeight() * scale));
            Image scaled = source.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        return null;
    }

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    public SanPhamPanel() {

        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.LIGHT_BLUE);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(20, 25, 20, 25));

        add(taoPhanDau(), BorderLayout.NORTH);
        add(taoPhanBang(), BorderLayout.CENTER);

        napLai();
    }

    // =========================================================
    // GIAO DIEN
    // =========================================================

    // Tạo phần đầu.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPhanDau() {

        // lblTieuDe2: Nhãn hiển thị: Quản lý sản phẩm.
        JLabel lblTieuDe2 = new JLabel("Quản lý sản phẩm");
        lblTieuDe2.setFont(UIStyle.font(Font.BOLD, 26));

        // btnThem: Nút thao tác thêm.
        JButton btnThem = UIStyle.nutXanh("+ Thêm sản phẩm");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnThem.addActionListener(e -> them());

        // pnlHang: Khung chứa thành phần giao diện hàng.
        JPanel pnlHang = new JPanel(new BorderLayout());
        pnlHang.setOpaque(false);
        pnlHang.setBorder(new EmptyBorder(0, 0, 15, 0));
        pnlHang.add(lblTieuDe2, BorderLayout.WEST);
        pnlHang.add(btnThem, BorderLayout.EAST);

        return pnlHang;
    }

    // Tạo phần bảng dữ liệu.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPhanBang() {

        // pnlThe: Khung chứa thành phần giao diện thẻ.
        JPanel pnlThe = UIStyle.the();
        pnlThe.setLayout(new BorderLayout(0, 10));

        cmbDanhMuc.setFont(UIStyle.font(Font.PLAIN, 13));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        cmbDanhMuc.addActionListener(e -> {
            if (!dangNapCombo) {
                napBang();
            }
        });

        // btnTim: Nút thao tác tìm.
        JButton btnTim = UIStyle.nutChinh("Tìm kiếm");
        // btnSua: Nút thao tác sửa.
        JButton btnSua = UIStyle.nutPhu("Sửa");
        // btnXoa: Nút thao tác xóa.
        JButton btnXoa = UIStyle.nutXoa("Xóa");

        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTim.addActionListener(e -> napBang());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        txtTim.addActionListener(e -> napBang());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnSua.addActionListener(e -> sua());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnXoa.addActionListener(e -> xoa());

        // pnlThanhCongCu: Khung chứa thành phần giao diện thanh công cụ.
        JPanel pnlThanhCongCu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlThanhCongCu.setOpaque(false);
        pnlThanhCongCu.add(UIStyle.nhan("Mã / tên:"));
        pnlThanhCongCu.add(txtTim);
        pnlThanhCongCu.add(cmbDanhMuc);
        pnlThanhCongCu.add(btnTim);
        pnlThanhCongCu.add(btnSua);
        pnlThanhCongCu.add(btnXoa);

        lblSoLuong2.setFont(UIStyle.font(Font.BOLD, 13));
        lblSoLuong2.setBorder(new EmptyBorder(0, 2, 0, 0));

        // pnlPhanTren: Khung chứa thành phần giao diện phần trên.
        JPanel pnlPhanTren = new JPanel();
        pnlPhanTren.setOpaque(false);
        pnlPhanTren.setLayout(new BoxLayout(pnlPhanTren, BoxLayout.Y_AXIS));
        pnlThanhCongCu.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSoLuong2.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlPhanTren.add(pnlThanhCongCu);
        pnlPhanTren.add(Box.createVerticalStrut(8));
        pnlPhanTren.add(lblSoLuong2);

        UIStyle.taoBang(tblDuLieu2, 5, 6, 7);
        tblDuLieu2.setRowHeight(84);
        tblDuLieu2.getColumnModel().getColumn(0).setPreferredWidth(96);
        tblDuLieu2.getColumnModel().getColumn(0).setMaxWidth(110);
        tblDuLieu2.getColumnModel().getColumn(0).setCellRenderer(new ImageRenderer());
        tblDuLieu2.addMouseListener(new MouseAdapter() {
            // Xử lý thao tác nhấn chuột trên thành phần đã gắn listener.
            // Tham số e: thông tin sự kiện.
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblDuLieu2.getSelectedRow() >= 0) {
                    sua();
                }
            }
        });

        pnlThe.add(pnlPhanTren, BorderLayout.NORTH);
        pnlThe.add(UIStyle.cuon(tblDuLieu2), BorderLayout.CENTER);

        return pnlThe;
    }

    // =========================================================
    // DU LIEU
    // =========================================================

    // Nạp lại dữ liệu và cập nhật nội dung màn hình.
    public void napLai() {
        napCombo();
        napBang();
    }

    // Nạp hộp lựa chọn.
    private void napCombo() {

        try {
            Muc dangChon = (Muc) cmbDanhMuc.getSelectedItem();
            String maDangChon = dangChon == null ? null : dangChon.ma;

            dangNapCombo = true;
            cmbDanhMuc.removeAllItems();
            cmbDanhMuc.addItem(new Muc(null, "Tất cả danh mục"));

            for (DanhMuc dm : dao.layDanhSachDanhMuc()) {
                Muc m = new Muc(dm.getMaDanhMuc(),
                        dm.getTenDanhMuc());
                cmbDanhMuc.addItem(m);
                if (m.ma.equals(maDangChon)) {
                    cmbDanhMuc.setSelectedItem(m);
                }
            }

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        } finally {
            dangNapCombo = false;
        }
    }

    // Nạp bảng dữ liệu.
    private void napBang() {

        try {
            Muc muc = (Muc) cmbDanhMuc.getSelectedItem();
            // maDanhMuc: Lưu mã danh mục.
            String maDanhMuc = muc == null ? null : muc.ma;

            dsHienTai = dao.timKiem(txtTim.getText(), maDanhMuc, null);

            model.setRowCount(0);
            for (SanPham sp : dsHienTai) {
                model.addRow(new Object[] {
                        sp.getMaSanPham(),
                        sp.getMaSanPham(),
                        sp.getTenSanPham(),
                        sp.getTenDanhMuc(),
                        sp.getTenNhaCungCap(),
                        UIStyle.tien(sp.getGiaNhap()),
                        UIStyle.tien(sp.getGiaBan()),
                        sp.getTonKho()
                });
            }

            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblSoLuong2.setText(dsHienTai.size() + " sản phẩm");

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // Lấy đối tượng tương ứng với dòng đang được chọn.
    // Kết quả: đối tượng/giá trị kiểu SanPham.
    private SanPham dangChon() {

        // row: Lưu chỉ số dòng.
        int row = tblDuLieu2.getSelectedRow();

        if (row < 0) {
            UIStyle.canhBao(this, "Vui lòng chọn một sản phẩm trong bảng.");
            return null;
        }

        return dsHienTai.get(tblDuLieu2.convertRowIndexToModel(row));
    }

    // =========================================================
    // CHUC NANG
    // =========================================================

    // Xử lý yêu cầu thêm dữ liệu từ giao diện.
    private void them() {
        moForm(null);
    }

    // Xử lý yêu cầu sửa bản ghi đang chọn.
    private void sua() {
        SanPham sp = dangChon();
        if (sp != null) {
            moForm(sp);
        }
    }

    // Mở biểu mẫu.
    // Tham số sp: sản phẩm cần xử lý.
    private void moForm(SanPham sp) {

        try {
            // dsDanhMuc: Lưu danh sách danh mục; phần tử có kiểu DanhMuc.
            ArrayList<DanhMuc> dsDanhMuc = dao.layDanhSachDanhMuc();
            // dsNcc: Lưu danh sách NCC; phần tử có kiểu NhaCungCap.
            ArrayList<NhaCungCap> dsNcc = dao.layDanhSachNhaCungCap();

            if (dsDanhMuc.isEmpty() || dsNcc.isEmpty()) {
                UIStyle.canhBao(this,
                        "Cần có ít nhất 1 danh mục và 1 nhà cung cấp trước khi thêm sản phẩm.");
                return;
            }

            SanPhamDialog winDlg = new SanPhamDialog(
                    SwingUtilities.getWindowAncestor(this),
                    sp,
                    sp == null ? dao.taoMaMoi() : null,
                    dsDanhMuc, dsNcc);
            winDlg.setVisible(true);

            if (winDlg.isDaLuu()) {
                napBang();
            }

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // Xử lý yêu cầu xóa bản ghi đang chọn.
    private void xoa() {

        SanPham sp = dangChon();
        if (sp == null) {
            return;
        }

        try {
            if (!UIStyle.xacNhan(this,
                    "Xóa sản phẩm \"" + sp.getTenSanPham()
                            + "\" cùng các dòng nhập kho, hóa đơn, kiểm kê và gợi ý thức ăn liên quan?\n"
                            + "Thao tác này không thể hoàn tác.")) {
                return;
            }

            dao.xoa(sp.getMaSanPham());
            napBang();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // =========================================================
    // MUC CUA COMBOBOX
    // =========================================================

    /** Muc cua combobox: hien ten, giu ma (ma = null nghia la "tat ca"). */
    private static class Muc {
        // ma: Lưu mã.
        final String ma;
        // ten: Lưu tên.
        final String ten;

        // Khởi tạo đối tượng từ các giá trị được truyền vào.
        // Tham số ma: mã.
        // Tham số ten: tên.
        Muc(String ma, String ten) {
            this.ma = ma;
            this.ten = ten;
        }

        // Trả về chuỗi đại diện của đối tượng để hiển thị.
        @Override
        public String toString() {
            return ten;
        }
    }

    // =========================================================
    // FORM THEM / SUA SAN PHAM
    // =========================================================

    /**
     * Form Them / Sua san pham - dung cac cot cua tblDuLieu2 SanPham:
     * ma, ten, giaNhap, giaBan, maDanhMuc, maNhaCungCap.
     * Ton kho khong phai cot cua SanPham, nen o "Them so luong" duoc luu thanh
     * 1 phieu nhap kho hoan tat (ton = tong nhap - ban +/- kiem ke van khop).
     */
    private static class SanPhamDialog extends JDialog {

        // dao: Đối tượng truy cập dữ liệu sản phẩm.
        private final SanPhamDAO dao = new SanPhamDAO();
        // dangSua: Cờ cho biết đang ở chế độ sửa.
        private final boolean dangSua;

        // txtMa: Ô nhập/hiển thị văn bản mã.
        private final JTextField txtMa = UIStyle.o(22);
        // txtTen: Ô nhập/hiển thị văn bản tên.
        private final JTextField txtTen = UIStyle.o(22);
        // txtGiaNhap: Ô nhập/hiển thị văn bản giá nhập.
        private final JTextField txtGiaNhap = UIStyle.o(22);
        // txtGiaBan: Ô nhập/hiển thị văn bản giá bán.
        private final JTextField txtGiaBan = UIStyle.o(22);
        private final JLabel lblAnh = new JLabel("Chưa có ảnh", SwingConstants.CENTER);
        private File anhDaChon;
        // Thông tin HSD theo lịch sử nhập kho, chỉ được xem khi mở chi tiết/sửa.
        // txtHanSuDung: Ô nhập/hiển thị văn bản hạn sử dụng.
        private final JTextField txtHanSuDung = UIStyle.oChiDoc("Chưa có dữ liệu");
        // spnThem: Ô chọn giá trị bằng nút tăng/giảm thêm.
        private final JSpinner spnThem = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
        // cmbDanhMuc: Hộp lựa chọn danh mục.
        private final JComboBox<DanhMuc> cmbDanhMuc = new JComboBox<>();
        // cmbNhaCungCap: Hộp lựa chọn nhà cung cấp.
        private final JComboBox<NhaCungCap> cmbNhaCungCap = new JComboBox<>();
        // cmbNhomTuoi: Hộp lựa chọn nhóm tuổi.
        private final JComboBox<String> cmbNhomTuoi = new JComboBox<>(new String[] {
                "Mọi độ tuổi", "Con non", "Trưởng thành", "Cao tuổi", "Chưa xác định" });
        // cmbKichThuoc: Hộp lựa chọn kích thước.
        private final JComboBox<String> cmbKichThuoc = new JComboBox<>(new String[] {
                "Mọi kích thước", "Nhỏ", "Trung bình", "Lớn", "Chưa xác định" });

        // daLuu: Cờ cho biết dữ liệu đã được lưu.
        private boolean daLuu = false;

        /** sp = null: them moi. maMoi: ma goi y khi them. */
        public SanPhamDialog(Window winCha, SanPham sp, String maMoi,
                List<DanhMuc> dsDanhMuc,
                List<NhaCungCap> dsNhaCungCap) {

            super(winCha, sp == null ? "Thêm sản phẩm" : "Sửa sản phẩm",
                    ModalityType.APPLICATION_MODAL);

            dangSua = sp != null;

            for (DanhMuc dm : dsDanhMuc) {
                cmbDanhMuc.addItem(dm);
            }
            for (NhaCungCap n : dsNhaCungCap) {
                cmbNhaCungCap.addItem(n);
            }

            cmbDanhMuc.setFont(UIStyle.font(Font.PLAIN, 13));
            cmbNhaCungCap.setFont(UIStyle.font(Font.PLAIN, 13));
            cmbNhomTuoi.setFont(UIStyle.font(Font.PLAIN, 13));
            cmbKichThuoc.setFont(UIStyle.font(Font.PLAIN, 13));

            // pnlForm2: Khung chứa thành phần giao diện biểu mẫu.
            JPanel pnlForm2 = new JPanel(new GridBagLayout());
            pnlForm2.setBorder(new EmptyBorder(20, 25, 10, 25));

            UIStyle.hang(pnlForm2, 0, "Mã sản phẩm", txtMa, false);
            UIStyle.hang(pnlForm2, 1, "Tên sản phẩm", txtTen, false);
            UIStyle.hang(pnlForm2, 2, "Giá nhập", txtGiaNhap, false);
            UIStyle.hang(pnlForm2, 3, "Giá bán", txtGiaBan, false);
            UIStyle.hang(pnlForm2, 4, "Danh mục", cmbDanhMuc, false);
            UIStyle.hang(pnlForm2, 5, "Nhà cung cấp", cmbNhaCungCap, false);
            UIStyle.hang(pnlForm2, 6, "Độ tuổi phù hợp", cmbNhomTuoi, false);
            UIStyle.hang(pnlForm2, 7, "Kích thước phù hợp", cmbKichThuoc, false);

            JButton btnChonAnh = UIStyle.nutPhu("Chọn ảnh...");
            JPanel pnlAnh = new JPanel(new BorderLayout(8, 0));
            pnlAnh.setOpaque(false);
            lblAnh.setPreferredSize(new Dimension(120, 82));
            lblAnh.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 230)));
            pnlAnh.add(lblAnh, BorderLayout.WEST);
            pnlAnh.add(btnChonAnh, BorderLayout.CENTER);
            UIStyle.hang(pnlForm2, 8, "Ảnh sản phẩm", pnlAnh, false);
            btnChonAnh.addActionListener(e -> chonAnh());

            spnThem.setFont(UIStyle.font(Font.PLAIN, 13));
            int dongTiep = 9;
            if (sp != null) {
                // Hạn sử dụng thuộc CHI TIẾT PHIẾU NHẬP; không chỉnh ở hồ sơ sản phẩm.
                txtHanSuDung.setEditable(false);
                txtHanSuDung.setFocusable(false);
                txtHanSuDung.setBackground(UIStyle.READONLY);
                txtHanSuDung.setToolTipText(
                        "Hạn sử dụng sớm nhất trong các phiếu nhập đã hoàn tất; "
                                + "sản phẩm có thể có nhiều lô/HSD khác nhau. "
                                + "Chỉnh hạn dùng tại nghiệp vụ nhập kho, không phải ở đây.");
                UIStyle.hang(pnlForm2, dongTiep++, "Hạn sử dụng (sớm nhất)", txtHanSuDung, false);
                UIStyle.hang(pnlForm2, dongTiep++, "Tồn hiện tại",
                        UIStyle.oChiDoc(String.valueOf(sp.getTonKho())), false);
            }
            UIStyle.hang(pnlForm2, dongTiep,
                    sp == null ? "Số lượng ban đầu" : "Thêm số lượng", spnThem, false);

            if (dangSua) {
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtMa.setText(sp.getMaSanPham());
                txtMa.setEditable(false);
                txtMa.setBackground(UIStyle.READONLY);
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtTen.setText(sp.getTenSanPham());
                hienThiAnh(sp.getMaSanPham());
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtGiaNhap.setText(String.valueOf((long) sp.getGiaNhap()));
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtGiaBan.setText(String.valueOf((long) sp.getGiaBan()));
                chonDanhMuc(sp.getMaDanhMuc());
                chonNhaCungCap(sp.getMaNhaCungCap());
                try {
                    // han: Lưu hạn dưới dạng dữ liệu thời gian.
                    java.time.LocalDate han = dao.layHanSuDungSomNhat(sp.getMaSanPham());
                    if (han == null) {
                        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                        txtHanSuDung.setText("Chưa có dữ liệu");
                    } else {
                        // Định dạng dữ liệu thành chuỗi rồi hiển thị trong control bằng setText().
                        txtHanSuDung.setText(han.format(
                                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }
                } catch (SQLException ex) {
                    // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                    txtHanSuDung.setText("Không tải được dữ liệu");
                    UIStyle.loi(this, ex);
                }
                try {
                    // thongTin: Mảng lưu thông tin.
                    String[] thongTin = dao.layThongTinGoiY(sp.getMaSanPham());
                    if (thongTin != null) {
                        chonGiaTri(cmbNhomTuoi, thongTin[1]);
                        chonGiaTri(cmbKichThuoc, thongTin[2]);
                    }
                } catch (SQLException ex) {
                    UIStyle.loi(this, ex);
                }
            } else {
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtMa.setText(maMoi);
                txtMa.setEditable(false);
                txtMa.setBackground(UIStyle.READONLY);
            }

            // btnHuy: Nút thao tác hủy.
            JButton btnHuy = UIStyle.nutPhu("Hủy");
            // btnLuu: Nút thao tác lưu.
            JButton btnLuu = UIStyle.nutXanh("Lưu");
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnHuy.addActionListener(e -> dispose());
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnLuu.addActionListener(e -> luu());

            // pnlNut2: Khung chứa thành phần giao diện nút.
            JPanel pnlNut2 = UIStyle.hangNut(btnHuy, btnLuu);
            pnlNut2.setBorder(new EmptyBorder(5, 25, 20, 25));

            setLayout(new BorderLayout());
            add(pnlForm2, BorderLayout.CENTER);
            add(pnlNut2, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(btnLuu);

            pack();
            setResizable(false);
            setLocationRelativeTo(winCha);
        }

        private void chonAnh() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn ảnh sản phẩm");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Ảnh (PNG, JPG, JPEG)", "png", "jpg", "jpeg"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                anhDaChon = chooser.getSelectedFile();
                ImageIcon original = new ImageIcon(anhDaChon.getAbsolutePath());
                Image scaled = original.getImage().getScaledInstance(110, 72, Image.SCALE_SMOOTH);
                lblAnh.setIcon(new ImageIcon(scaled));
                lblAnh.setText("");
            }
        }

        private void hienThiAnh(String code) {
            Icon icon = loadProductImage(code, 110, 72);
            if (icon != null) {
                lblAnh.setIcon(icon);
                lblAnh.setText("");
            }
        }

        private void luuAnh(String code) throws IOException {
            if (anhDaChon == null) return;
            File folder = new File("src/img/products");
            if (!folder.exists() && !folder.mkdirs()) {
                throw new IOException("Không tạo được thư mục lưu ảnh sản phẩm.");
            }
            String name = anhDaChon.getName().toLowerCase(java.util.Locale.ROOT);
            String ext = name.endsWith(".png") ? ".png"
                    : (name.endsWith(".jpeg") ? ".jpeg" : ".jpg");
            Files.copy(anhDaChon.toPath(), new File(folder, code + ext).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            for (String other : new String[] { ".png", ".jpg", ".jpeg" }) {
                if (!other.equals(ext)) Files.deleteIfExists(new File(folder, code + other).toPath());
            }
        }

        // Chọn danh mục.
        // Tham số ma: mã.
        private void chonDanhMuc(String ma) {
            for (int i = 0; i < cmbDanhMuc.getItemCount(); i++) {
                if (cmbDanhMuc.getItemAt(i).getMaDanhMuc().equals(ma)) {
                    cmbDanhMuc.setSelectedIndex(i);
                    return;
                }
            }
        }

        // Chọn nhà cung cấp.
        // Tham số ma: mã.
        private void chonNhaCungCap(String ma) {
            for (int i = 0; i < cmbNhaCungCap.getItemCount(); i++) {
                if (cmbNhaCungCap.getItemAt(i).getMaNhaCungCap().equals(ma)) {
                    cmbNhaCungCap.setSelectedIndex(i);
                    return;
                }
            }
        }

        // Chọn giá trị.
        // Tham số cmbLuaChon: hộp lựa chọn.
        // Tham số giaTri: giá trị.
        private void chonGiaTri(JComboBox<String> cmbLuaChon, String giaTri) {
            for (int i = 0; i < cmbLuaChon.getItemCount(); i++) {
                if (cmbLuaChon.getItemAt(i).equals(giaTri)) {
                    cmbLuaChon.setSelectedIndex(i);
                    return;
                }
            }
        }

        // Kiểm tra dữ liệu nhập và lưu thông tin của biểu mẫu.
        private void luu() {

            // ma: Đọc nội dung chữ từ ô nhập để xử lý mã.
            String ma = txtMa.getText().trim();
            // ten: Đọc nội dung chữ từ ô nhập để xử lý tên.
            String ten = txtTen.getText().trim();

            if (ma.isEmpty() || ten.isEmpty()) {
                UIStyle.canhBao(this, "Vui lòng nhập mã và tên sản phẩm.");
                return;
            }

            if (ma.length() > 10) {
                UIStyle.canhBao(this, "Mã sản phẩm tối đa 10 ký tự.");
                return;
            }

            if (ten.length() > 50) {
                UIStyle.canhBao(this, "Tên sản phẩm tối đa 50 ký tự.");
                return;
            }

            // giaNhap: Lưu giá nhập.
            long giaNhap;
            // giaBan: Lưu giá bán.
            long giaBan;

            try {
                giaNhap = UIStyle.docTien(txtGiaNhap.getText());
            } catch (NumberFormatException e) {
                UIStyle.canhBao(this, "Giá nhập phải là số không âm (ví dụ 180000).");
                return;
            }

            try {
                giaBan = UIStyle.docTien(txtGiaBan.getText());
            } catch (NumberFormatException e) {
                UIStyle.canhBao(this, "Giá bán phải là số không âm (ví dụ 250000).");
                return;
            }

            DanhMuc dm = (DanhMuc) cmbDanhMuc.getSelectedItem();
            NhaCungCap ncc = (NhaCungCap) cmbNhaCungCap.getSelectedItem();

            if (dm == null || ncc == null) {
                UIStyle.canhBao(this,
                        "Cần có ít nhất 1 danh mục và 1 nhà cung cấp.");
                return;
            }

            if (giaBan < giaNhap && !UIStyle.xacNhan(this,
                    "Giá bán đang thấp hơn giá nhập. Vẫn lưu?")) {
                return;
            }

            SanPham sp = new SanPham(ma, ten, giaNhap, giaBan,
                    dm.getMaDanhMuc(), ncc.getMaNhaCungCap());

            try {
                if (dangSua) {
                    dao.sua(sp);
                } else {
                    dao.them(sp);
                }
                luuAnh(ma);
                dao.luuThongTinGoiY(ma, dm.getTenDanhMuc(), ten,
                        (String) cmbNhomTuoi.getSelectedItem(),
                        (String) cmbKichThuoc.getSelectedItem());

                int them = (Integer) spnThem.getValue();
                if (them > 0) {
                    dao.nhapBoSung(
                            List.of(sp), List.of(them), PhienLamViec.maNV);
                }

                daLuu = true;
                dispose();

            } catch (SQLException ex) {
                UIStyle.loi(this, ex);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Không lưu được ảnh: " + ex.getMessage(),
                        "Lỗi ảnh sản phẩm", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Kiểm tra dữ liệu đã được lưu.
        // Kết quả: true/false theo điều kiện xử lý.
        public boolean isDaLuu() {
            return daLuu;
        }
    }
}
