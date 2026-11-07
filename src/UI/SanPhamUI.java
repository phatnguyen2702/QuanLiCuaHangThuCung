package UI;
import control.PhienLamViecControl;
import UI.TrangChuUI.UIStyle;
import control.SanPhamControl;
import entity.DanhMuc;
import entity.NhaCungCap;
import entity.SanPham;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
public class SanPhamUI extends JFrame {
private static final String TONG_CAC_DOT = "Tổng các đợt nhập";
    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DefaultTableModel model = taoModel(5, "Ảnh", "Mã SP", "Tên sản phẩm", "Danh mục",
            "Nhà cung cấp", "Đợt nhập", "Ngày nhập", "Hạn sử dụng", "Trạng thái", "Giá nhập", "Giá bán", "Tồn");
    private final JTable tblSanPham = new JTable(model);
    private final JTextField txtTim = UIStyle.o(16);
    private final JComboBox<Muc> cmbDanhMuc = new JComboBox<>();
    private final Map<String, String> maDotTheoTen = new LinkedHashMap<>();
    private final JLabel lblSoLuong = new JLabel("0 sản phẩm");
    private ArrayList<SanPham> dsHienTai = new ArrayList<>();
    private ArrayList<SanPham> dsTongHop = new ArrayList<>();
    private boolean dangNapCombo = false;
    private static DefaultTableModel taoModel(int cotSua, String... tenCot) {
        return new DefaultTableModel(tenCot, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == cotSua; }
        };
    }
    private static String ngay(LocalDate value) { return value == null ? "" : value.format(DD_MM_YYYY); }
    private static String trangThaiLo(SanPham sp) {
        if (sp.getHanSuDung() != null && sp.getHanSuDung().isBefore(LocalDate.now())) return "Hết hạn";
        return sp.isNgungKinhDoanh() ? "Ngừng kinh doanh" : "Đang kinh doanh";
    }
    private static void trongSuot(JComponent... thanhPhan) {
        for (JComponent item : thanhPhan) item.setOpaque(false);
    }
    private static void themHang(JPanel form, String nhan, JComponent giaTri) {
        form.add(new JLabel(nhan));
        form.add(giaTri);
    }
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
        if (code == null || !code.matches("[A-Za-z0-9_-]+"))
            return null;
        for (String ext : new String[] { ".png", ".jpg", ".jpeg" }) {
            File file = new File("src/img/products", code + ext);
            if (!file.isFile())
                file = new File("img/products", code + ext);
            if (!file.isFile())
                continue;
            ImageIcon source = new ImageIcon(file.getAbsolutePath());
            if (source.getIconWidth() <= 0 || source.getIconHeight() <= 0)
                continue;
            double scale = Math.min((double) maxWidth / source.getIconWidth(),
                    (double) maxHeight / source.getIconHeight());
            int w = Math.max(1, (int) (source.getIconWidth() * scale));
            int h = Math.max(1, (int) (source.getIconHeight() * scale));
            Image scaled = source.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        return null;
    }
    public SanPhamUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.LIGHT_BLUE);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(20, 25, 20, 25));
        add(taoPhanDau(), BorderLayout.NORTH);
        add(taoPhanBang(), BorderLayout.CENTER);
        napLai();
    }
    private JPanel taoPhanDau() {
        JLabel lblTieuDe = new JLabel("Quản lý sản phẩm");
        lblTieuDe.setFont(UIStyle.font(Font.BOLD, 26));
        JButton btnThem = UIStyle.nutXanh("+ Thêm sản phẩm");
        btnThem.addActionListener(e -> moFormSanPham(null));
        JPanel pnlHang = new JPanel(new BorderLayout());
        trongSuot(pnlHang);
        pnlHang.setBorder(new EmptyBorder(0, 0, 15, 0));
        pnlHang.add(lblTieuDe, BorderLayout.WEST);
        pnlHang.add(btnThem, BorderLayout.EAST);
        return pnlHang;
    }
    private JPanel taoPhanBang() {
        JPanel pnlThe = UIStyle.the();
        pnlThe.setLayout(new BorderLayout(0, 10));
        cmbDanhMuc.setFont(UIStyle.font(Font.PLAIN, 13));
        cmbDanhMuc.addActionListener(e -> {
            if (!dangNapCombo) {
                napBangSanPham();
            }
        });
        JButton btnTim = UIStyle.nutChinh("Tìm kiếm");
        JButton btnSua = UIStyle.nutPhu("Sửa");
        JButton btnXoa = UIStyle.nutPhu("Xóa");
        btnXoa.setForeground(new Color(220, 53, 69));
        JButton btnXuLyHang = UIStyle.nutPhu("Xử lý hàng lỗi / hết hạn");
        JButton btnLichSuXuLy = UIStyle.nutPhu("Lịch sử xử lý hàng lỗi");
        btnTim.addActionListener(e -> napBangSanPham());
        txtTim.addActionListener(e -> napBangSanPham());
        btnSua.addActionListener(e -> suaSanPham());
        btnXoa.addActionListener(e -> xoaSanPham());
        btnXuLyHang.addActionListener(e -> xuLyLoSanPham());
        btnLichSuXuLy.addActionListener(e -> hienLichSuXuLySanPham());
        JPanel pnlThanhCongCu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        trongSuot(pnlThanhCongCu);
        pnlThanhCongCu.add(UIStyle.nhan("Mã / tên:"));
        pnlThanhCongCu.add(txtTim);
        pnlThanhCongCu.add(cmbDanhMuc);
        pnlThanhCongCu.add(btnTim);
        pnlThanhCongCu.add(btnSua);
        pnlThanhCongCu.add(btnXoa);
        pnlThanhCongCu.add(btnXuLyHang);
        pnlThanhCongCu.add(btnLichSuXuLy);
        lblSoLuong.setFont(UIStyle.font(Font.BOLD, 13));
        lblSoLuong.setBorder(new EmptyBorder(0, 2, 0, 0));
        JPanel pnlPhanTren = new JPanel();
        trongSuot(pnlPhanTren);
        pnlPhanTren.setLayout(new BoxLayout(pnlPhanTren, BoxLayout.Y_AXIS));
        pnlThanhCongCu.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSoLuong.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlPhanTren.add(pnlThanhCongCu);
        pnlPhanTren.add(Box.createVerticalStrut(8));
        pnlPhanTren.add(lblSoLuong);
        UIStyle.taoBang(tblSanPham, 9, 10, 11);
        tblSanPham.setRowHeight(84);
        tblSanPham.getColumnModel().getColumn(0).setPreferredWidth(96);
        tblSanPham.getColumnModel().getColumn(0).setMaxWidth(110);
        tblSanPham.getColumnModel().getColumn(0).setCellRenderer(new ImageRenderer());
        tblSanPham.getColumnModel().getColumn(5).setCellEditor(taoBoChonDotNhap());
        tblSanPham.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblSanPham.getSelectedRow() >= 0
                        && tblSanPham.columnAtPoint(e.getPoint()) != 5) {
                    suaSanPham();
                }
            }
        });
        pnlThe.add(pnlPhanTren, BorderLayout.NORTH);
        pnlThe.add(UIStyle.cuon(tblSanPham), BorderLayout.CENTER);
        return pnlThe;
    }
    public void napLai() {
        napDuLieuComboSanPham();
        napBangSanPham();
    }
    private void napDuLieuComboSanPham() {
        try {
            Muc dangChon = (Muc) cmbDanhMuc.getSelectedItem();
            String maDangChon = dangChon == null ? null : dangChon.ma;
            dangNapCombo = true;
            cmbDanhMuc.removeAllItems();
            cmbDanhMuc.addItem(new Muc(null, "Tất cả danh mục"));
            for (DanhMuc dm : SanPhamControl.layDanhSachDanhMucSanPham()) {
                Muc m = new Muc(dm.getMaDanhMuc(),
                        dm.getTenDanhMuc());
                cmbDanhMuc.addItem(m);
                if (m.ma.equals(maDangChon)) {
                    cmbDanhMuc.setSelectedItem(m);
                }
            }
            maDotTheoTen.clear();
            for (dao.SanPhamDAO.DotNhap dot : SanPhamControl.layDanhSachDotNhapSanPham()) {
                maDotTheoTen.put(dot.toString(), dot.getMaPhieuNhap());
            }
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        } finally {
            dangNapCombo = false;
        }
    }
    private javax.swing.table.TableCellEditor taoBoChonDotNhap() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(UIStyle.font(Font.PLAIN, 12));
        return new javax.swing.DefaultCellEditor(combo) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean selected,
                    int row, int column) {
                int modelRow = table.convertRowIndexToModel(row);
                combo.removeAllItems();
                combo.addItem(TONG_CAC_DOT);
                try {
                    for (dao.SanPhamDAO.DotNhap dot : SanPhamControl.layDanhSachDotNhapSanPham(dsTongHop.get(modelRow).getMaSanPham()))
                        combo.addItem(dot.toString());
                } catch (Exception ex) {
                    UIStyle.loi(SanPhamUI.this, ex);
                }
                combo.setSelectedItem(value == null ? TONG_CAC_DOT : value.toString());
                return super.getTableCellEditorComponent(table, value, selected, row, column);
            }
            @Override
            public boolean stopCellEditing() {
                int row = tblSanPham.getEditingRow();
                int modelRow = row < 0 ? -1 : tblSanPham.convertRowIndexToModel(row);
                String tenDot = (String) combo.getSelectedItem();
                if (modelRow >= 0 && !hienThiDotNhap(modelRow, tenDot))
                    return false;
                return super.stopCellEditing();
            }
        };
    }
    private boolean hienThiDotNhap(int row, String tenDot) {
        String maSanPham = dsTongHop.get(row).getMaSanPham();
        SanPham sp;
        if (tenDot == null || tenDot.equals(TONG_CAC_DOT)) {
            sp = dsTongHop.get(row);
        } else {
            String maDot = maDotTheoTen.get(tenDot);
            if (maDot == null)
                return false;
            try {
                sp = SanPhamControl.timSanPhamTheoDotNhap("", null, maDot, maSanPham).stream().findFirst().orElse(null);
            } catch (Exception ex) {
                UIStyle.loi(this, ex);
                return false;
            }
            if (sp == null) {
                UIStyle.canhBao(this, "Lô này đã hết tồn hoặc không còn đủ điều kiện kinh doanh.");
                return false;
            }
        }
        dsHienTai.set(row, sp);
        boolean tongHop = sp.getMaPhieuNhap() == null;
        model.setValueAt(tenDot == null ? TONG_CAC_DOT : tenDot, row, 5);
        model.setValueAt(tongHop ? "" : ngay(sp.getNgayNhap()), row, 6);
        model.setValueAt(tongHop ? "" : ngay(sp.getHanSuDung()), row, 7);
        String trangThai = tongHop ? "" : trangThaiLo(sp);
        model.setValueAt(trangThai, row, 8);
        model.setValueAt(tongHop ? "" : UIStyle.tien(sp.getGiaNhap()), row, 9);
        model.setValueAt(tongHop ? "" : UIStyle.tien(sp.getGiaBan()), row, 10);
        model.setValueAt(sp.getTonKho(), row, 11);
        return true;
    }
    private void napBangSanPham() {
        try {
            Muc muc = (Muc) cmbDanhMuc.getSelectedItem();
            String maDanhMuc = muc == null ? null : muc.ma;
            dsHienTai = SanPhamControl.timKiemSanPham(txtTim.getText(), maDanhMuc, null);
            dsTongHop = new ArrayList<>(dsHienTai);
            model.setRowCount(0);
            for (SanPham sp : dsHienTai) {
                model.addRow(new Object[] {
                        sp.getMaSanPham(),
                        sp.getMaSanPham(),
                        sp.getTenSanPham(),
                        sp.getTenDanhMuc(),
                        sp.getTenNhaCungCap(),
                        TONG_CAC_DOT, "", "", "",
                        "", "",
                        sp.getTonKho()
                });
            }
            lblSoLuong.setText(dsHienTai.size() + " sản phẩm");
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private SanPham laySanPhamDangChon() {
        int row = tblSanPham.getSelectedRow();
        if (row < 0) {
            UIStyle.canhBao(this, "Vui lòng chọn một sản phẩm trong bảng.");
            return null;
        }
        return dsHienTai.get(tblSanPham.convertRowIndexToModel(row));
    }
    private void suaSanPham() {
        SanPham sp = laySanPhamDangChon();
        if (sp != null) {
            if (sp.getMaPhieuNhap() != null) {
                try {
                    SanPhamControl.timKiemSanPham(sp.getMaSanPham(), null, null).stream()
                            .filter(hoSo -> hoSo.getMaSanPham().equals(sp.getMaSanPham()))
                            .findFirst().ifPresent(hoSo -> {
                                sp.setGiaNhap(hoSo.getGiaNhap());
                                sp.setTonKho(hoSo.getTonKho());
                            });
                } catch (Exception ex) {
                    UIStyle.loi(this, ex);
                    return;
                }
            }
            moFormSanPham(sp);
        }
    }
    private void moFormSanPham(SanPham sp) {
        try {
            ArrayList<DanhMuc> dsDanhMuc = SanPhamControl.layDanhSachDanhMucSanPham();
            ArrayList<NhaCungCap> dsNcc = SanPhamControl.layDanhSachNhaCungCapSanPham();
            if (dsDanhMuc.isEmpty() || dsNcc.isEmpty()) {
                UIStyle.canhBao(this,
                        "Cần có ít nhất 1 danh mục và 1 nhà cung cấp trước khi thêm sản phẩm.");
                return;
            }
            SanPhamDialog winDlg = new SanPhamDialog(
                    SwingUtilities.getWindowAncestor(this),
                    sp,
                    sp == null ? SanPhamControl.taoMaSanPhamMoi() : null,
                    dsDanhMuc, dsNcc);
            winDlg.setVisible(true);
            if (winDlg.isDaLuu()) {
                napBangSanPham();
            }
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private void xoaSanPham() {
        SanPham sp = laySanPhamDangChon();
        if (sp == null) {
            return;
        }
        try {
            if (SanPhamControl.demLichSuSanPham(sp.getMaSanPham()) > 0) {
                UIStyle.canhBao(this,
                        "Không thể xóa sản phẩm đã phát sinh lịch sử nhập kho, hóa đơn hoặc kiểm kê.");
                return;
            }
            if (!UIStyle.xacNhan(this,
                    "Xóa sản phẩm \"" + sp.getTenSanPham()
                            + "\"? Chỉ sản phẩm chưa có giao dịch nào mới được xóa.\n"
                            + "Thao tác này không thể hoàn tác.")) {
                return;
            }
            if (!SanPhamControl.xoaSanPham(sp.getMaSanPham())) throw new IllegalStateException("Không xóa được sản phẩm.");
            napBangSanPham();
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private void xuLyLoSanPham() {
        SanPham sp = laySanPhamDangChon();
        if (sp == null)
            return;
        if (sp.getMaPhieuNhap() == null) {
            UIStyle.canhBao(this, "Chọn một đợt nhập cụ thể trong cột Đợt nhập trước.");
            return;
        }
        if (sp.getTonKho() <= 0) {
            UIStyle.canhBao(this, "Đợt nhập này hiện không còn tồn để xử lý.");
            return;
        }
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        JSpinner soLuong = new JSpinner(new SpinnerNumberModel(1, 1, sp.getTonKho(), 1));
        JComboBox<String> lyDo = new JComboBox<>(new String[] {
                "Hết hạn", "Hư hỏng", "Lỗi sản phẩm", "Trả nhà cung cấp", "Khác"
        });
        JTextField ghiChu = new JTextField(22);
        themHang(form, "Sản phẩm", new JLabel(sp.getMaSanPham() + " - " + sp.getTenSanPham()));
        themHang(form, "Đợt nhập", new JLabel(sp.getMaPhieuNhap()));
        themHang(form, "Tồn hiện tại", new JLabel(String.valueOf(sp.getTonKho())));
        themHang(form, "Số lượng xử lý", soLuong);
        themHang(form, "Lý do", lyDo);
        themHang(form, "Ghi chú", ghiChu);
        if (JOptionPane.showConfirmDialog(this, form, "Xử lý hàng lỗi / hết hạn",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
            return;
        int soLuongXuLy = (Integer) soLuong.getValue();
        String thongTinXacNhan = "Xác nhận hủy " + soLuongXuLy + " sản phẩm "
                + sp.getMaSanPham() + " - " + sp.getTenSanPham()
                + " thuộc đợt nhập " + sp.getMaPhieuNhap() + "?\n"
                + "Lý do: " + lyDo.getSelectedItem() + "\n\n"
                + "Số lượng này sẽ bị trừ khỏi tồn kho và không thể hoàn tác trực tiếp.";
        if (JOptionPane.showConfirmDialog(this, thongTinXacNhan,
                "Xác nhận hủy hàng lỗi", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
            return;
        try {
            if (!SanPhamControl.ghiNhanXuLySanPhamTheoDotNhap(sp.getMaSanPham(), sp.getMaPhieuNhap(),
                    soLuongXuLy, (String) lyDo.getSelectedItem(), ghiChu.getText(),
                    PhienLamViecControl.maNV)) throw new IllegalStateException("Không ghi nhận được xử lý lô hàng.");
            UIStyle.thongBao(this, "Đã xác nhận hủy hàng lỗi. Số lượng đã được trừ khỏi tồn kho và lưu lịch sử xử lý.");
            napLai();
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private void hienLichSuXuLySanPham() {
        String[] cot = { "Mã xử lý", "Ngày xử lý", "Mã SP", "Tên sản phẩm",
                "Đợt nhập", "Số lượng", "Lý do", "Ghi chú", "Người xử lý" };
        DefaultTableModel lichSuModel = taoModel(-1, cot);
        try {
            for (Object[] dong : SanPhamControl.layLichSuXuLySanPham()) {
                lichSuModel.addRow(dong);
            }
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
            return;
        }
        JTable bangLichSu = new JTable(lichSuModel);
        UIStyle.taoBang(bangLichSu);
        bangLichSu.setAutoCreateRowSorter(true);
        bangLichSu.setFillsViewportHeight(true);
        bangLichSu.getColumnModel().getColumn(0).setPreferredWidth(85);
        bangLichSu.getColumnModel().getColumn(1).setPreferredWidth(135);
        bangLichSu.getColumnModel().getColumn(3).setPreferredWidth(190);
        bangLichSu.getColumnModel().getColumn(7).setPreferredWidth(220);
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Lịch sử xử lý hàng lỗi / hết hạn", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));
        JPanel noiDung = new JPanel(new BorderLayout(0, 10));
        noiDung.setBorder(new EmptyBorder(16, 16, 12, 16));
        noiDung.setBackground(UIStyle.LIGHT_BLUE);
        JLabel tieuDe = new JLabel("Lịch sử xử lý hàng lỗi / hết hạn / trả nhà cung cấp  ·  "
                + lichSuModel.getRowCount() + " lượt");
        tieuDe.setFont(UIStyle.font(Font.BOLD, 16));
        noiDung.add(tieuDe, BorderLayout.NORTH);
        noiDung.add(UIStyle.cuon(bangLichSu), BorderLayout.CENTER);
        JButton dong = UIStyle.nutPhu("Đóng");
        dong.addActionListener(e -> dialog.dispose());
        JPanel chan = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        trongSuot(chan);
        chan.add(dong);
        noiDung.add(chan, BorderLayout.SOUTH);
        dialog.add(noiDung, BorderLayout.CENTER);
        dialog.setSize(1120, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    private static class Muc {
        final String ma;
        final String ten;
        Muc(String ma, String ten) {
            this.ma = ma;
            this.ten = ten;
        }
        @Override
        public String toString() {
            return ten;
        }
    }
    private static class SanPhamDialog extends JDialog {
        private final boolean dangSua;
        private final JTextField txtMa = UIStyle.o(22);
        private final JTextField txtTen = UIStyle.o(22);
        private final JTextField txtGiaNhap = UIStyle.o(22);
        private final JTextField txtGiaBan = UIStyle.o(22);
        private final JLabel lblAnh = new JLabel("Chưa có ảnh", SwingConstants.CENTER);
        private File anhDaChon;
        private final JTextField txtHanSuDung = UIStyle.oChiDoc("Chưa có dữ liệu");
        private final JComboBox<DanhMuc> cmbDanhMuc = new JComboBox<>();
        private final JComboBox<NhaCungCap> cmbNhaCungCap = new JComboBox<>();
        private final JComboBox<String> cmbNhomTuoi = new JComboBox<>(new String[] {
                "Mọi độ tuổi", "Con non", "Trưởng thành", "Cao tuổi", "Chưa xác định" });
        private final JComboBox<String> cmbKichThuoc = new JComboBox<>(new String[] {
                "Mọi kích thước", "Nhỏ", "Trung bình", "Lớn", "Chưa xác định" });
        private boolean daLuu = false;
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
            for (JComboBox<?> combo : List.of(cmbDanhMuc, cmbNhaCungCap, cmbNhomTuoi, cmbKichThuoc))
                combo.setFont(UIStyle.font(Font.PLAIN, 13));
            JPanel pnlForm = new JPanel(new GridBagLayout());
            pnlForm.setBorder(new EmptyBorder(20, 25, 10, 25));
            UIStyle.hang(pnlForm, 0, "Mã sản phẩm", txtMa, false);
            UIStyle.hang(pnlForm, 1, "Tên sản phẩm", txtTen, false);
            UIStyle.hang(pnlForm, 2, "Giá nhập", txtGiaNhap, false);
            UIStyle.hang(pnlForm, 3, "Giá bán", txtGiaBan, false);
            UIStyle.hang(pnlForm, 4, "Danh mục", cmbDanhMuc, false);
            UIStyle.hang(pnlForm, 5, "Nhà cung cấp", cmbNhaCungCap, false);
            UIStyle.hang(pnlForm, 6, "Độ tuổi phù hợp", cmbNhomTuoi, false);
            UIStyle.hang(pnlForm, 7, "Kích thước phù hợp", cmbKichThuoc, false);
            JButton btnChonAnh = UIStyle.nutPhu("Chọn ảnh...");
            JPanel pnlAnh = new JPanel(new BorderLayout(8, 0));
            trongSuot(pnlAnh);
            lblAnh.setPreferredSize(new Dimension(120, 82));
            lblAnh.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 230)));
            pnlAnh.add(lblAnh, BorderLayout.WEST);
            pnlAnh.add(btnChonAnh, BorderLayout.CENTER);
            UIStyle.hang(pnlForm, 8, "Ảnh sản phẩm", pnlAnh, false);
            btnChonAnh.addActionListener(e -> chonAnhSanPham());
            if (dangSua) {
                txtMa.setText(sp.getMaSanPham());
                txtTen.setText(sp.getTenSanPham());
                txtGiaNhap.setText(String.valueOf((long) sp.getGiaNhap()));
                txtGiaBan.setText(String.valueOf((long) sp.getGiaBan()));
                chon(cmbDanhMuc, sp.getMaDanhMuc(), DanhMuc::getMaDanhMuc);
                chon(cmbNhaCungCap, sp.getMaNhaCungCap(), NhaCungCap::getMaNhaCungCap);
                txtHanSuDung.setToolTipText(
                        "Hạn sử dụng sớm nhất trong các phiếu nhập đã hoàn tất; "
                                + "sản phẩm có thể có nhiều lô/HSD khác nhau. "
                                + "Chỉnh hạn dùng tại nghiệp vụ nhập kho, không phải ở đây.");
                UIStyle.hang(pnlForm, 9, "Hạn sử dụng (sớm nhất)", txtHanSuDung, false);
                UIStyle.hang(pnlForm, 10, "Tồn hiện tại",
                        UIStyle.oChiDoc(String.valueOf(sp.getTonKho())), false);
                if (sp.getTonKho() > 0) {
                    txtGiaNhap.setEditable(false);
                    txtGiaNhap.setBackground(UIStyle.READONLY);
                    txtGiaNhap.setToolTipText("Chỉ được đổi giá nhập khi tồn kho bằng 0.");
                }
                napDuLieuSua(sp);
            } else {
                txtMa.setText(maMoi);
            }
            txtMa.setEditable(false);
            txtMa.setBackground(UIStyle.READONLY);
            txtMa.setToolTipText("Mã sản phẩm được hệ thống tự tạo, không thể chỉnh sửa.");
            JButton btnHuy = UIStyle.nutPhu("Hủy");
            JButton btnLuu = UIStyle.nutXanh("Lưu");
            btnHuy.addActionListener(e -> dispose());
            btnLuu.addActionListener(e -> luuSanPham());
            JPanel pnlNut = UIStyle.hangNut(btnHuy, btnLuu);
            pnlNut.setBorder(new EmptyBorder(5, 25, 20, 25));
            setLayout(new BorderLayout());
            add(pnlForm, BorderLayout.CENTER);
            add(pnlNut, BorderLayout.SOUTH);
            getRootPane().setDefaultButton(btnLuu);
            pack();
            setResizable(false);
            setLocationRelativeTo(winCha);
        }
        private void napDuLieuSua(SanPham sp) {
            hienThiAnhSanPham(sp.getMaSanPham());
            try {
                LocalDate han = SanPhamControl.layHanSuDungSomNhatSanPham(sp.getMaSanPham());
                txtHanSuDung.setText(han == null ? "Chưa có dữ liệu" : ngay(han));
            } catch (Exception ex) {
                txtHanSuDung.setText("Không tải được dữ liệu");
                UIStyle.loi(this, ex);
            }
            try {
                String[] thongTin = SanPhamControl.layThongTinGoiYThucAn(sp.getMaSanPham());
                if (thongTin != null) {
                    chon(cmbNhomTuoi, thongTin[1], Function.identity());
                    chon(cmbKichThuoc, thongTin[2], Function.identity());
                }
            } catch (Exception ex) {
                UIStyle.loi(this, ex);
            }
        }
        private void chonAnhSanPham() {
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
        private void hienThiAnhSanPham(String code) {
            Icon icon = loadProductImage(code, 110, 72);
            if (icon != null) {
                lblAnh.setIcon(icon);
                lblAnh.setText("");
            }
        }
        private void luuAnhSanPham(String code) throws IOException {
            if (anhDaChon == null)
                return;
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
                if (!other.equals(ext))
                    Files.deleteIfExists(new File(folder, code + other).toPath());
            }
        }
        private <T> void chon(JComboBox<T> combo, String giaTri, Function<T, String> layGiaTri) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (giaTri.equals(layGiaTri.apply(combo.getItemAt(i)))) {
                    combo.setSelectedIndex(i);
                    return;
                }
            }
        }
        private long docGia(JTextField truong, String tenGia) {
            try {
                return UIStyle.docTien(truong.getText());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(tenGia + " phải là số không âm (ví dụ 180000).");
            }
        }
        private void luuSanPham() {
            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            if (ten.isEmpty()) {
                UIStyle.canhBao(this, "Vui lòng nhập tên sản phẩm.");
                return;
            }
            if (ten.length() > 50) {
                UIStyle.canhBao(this, "Tên sản phẩm tối đa 50 ký tự.");
                return;
            }
            long giaNhap;
            long giaBan;
            try {
                giaNhap = docGia(txtGiaNhap, "Giá nhập");
                giaBan = docGia(txtGiaBan, "Giá bán");
            } catch (IllegalArgumentException e) {
                UIStyle.canhBao(this, e.getMessage());
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
                if (!SanPhamControl.luuThongTinSanPham(sp, dangSua, dm.getTenDanhMuc(),
                        (String) cmbNhomTuoi.getSelectedItem(),
                        (String) cmbKichThuoc.getSelectedItem(), PhienLamViecControl.maNV)) throw new IllegalStateException("Không lưu được sản phẩm.");
                String loiAnh = null;
                try {
                    luuAnhSanPham(ma);
                } catch (IOException ex) {
                    loiAnh = ex.getMessage();
                }
                daLuu = true;
                dispose();
                if (loiAnh != null) {
                    JOptionPane.showMessageDialog(getOwner(),
                            "Thông tin sản phẩm đã được lưu, nhưng ảnh chưa lưu được: " + loiAnh,
                            "Lưu ảnh thất bại", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                UIStyle.loi(this, ex);
            }
        }
        public boolean isDaLuu() {
            return daLuu;
        }
    }
}
