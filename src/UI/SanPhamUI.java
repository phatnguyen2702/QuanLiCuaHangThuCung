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
import java.util.ArrayList;
import java.util.List;
public class SanPhamUI extends JFrame {
private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Ảnh", "Mã SP", "Tên sản phẩm", "Danh mục",
                    "Nhà cung cấp", "Đợt nhập", "Ngày nhập", "Hạn sử dụng", "Trạng thái",
                    "Giá nhập", "Giá bán", "Tồn" },
            0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 5;
        }
    };
    private final JTable tblDuLieu2 = new JTable(model);
    private final JTextField txtTim = UIStyle.o(16);
    private final JComboBox<Muc> cmbDanhMuc = new JComboBox<>();
    private final java.util.Map<String, String> maDotTheoTen = new java.util.LinkedHashMap<>();
    private final JLabel lblSoLuong2 = new JLabel("0 sản phẩm");
    private ArrayList<SanPham> dsHienTai = new ArrayList<>();
    private ArrayList<SanPham> dsTongHop = new ArrayList<>();
    private boolean dangNapCombo = false;
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
        JLabel lblTieuDe2 = new JLabel("Quản lý sản phẩm");
        lblTieuDe2.setFont(UIStyle.font(Font.BOLD, 26));
        JButton btnThem = UIStyle.nutXanh("+ Thêm sản phẩm");
        btnThem.addActionListener(e -> themSanPham());
        JPanel pnlHang = new JPanel(new BorderLayout());
        pnlHang.setOpaque(false);
        pnlHang.setBorder(new EmptyBorder(0, 0, 15, 0));
        pnlHang.add(lblTieuDe2, BorderLayout.WEST);
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
        pnlThanhCongCu.setOpaque(false);
        pnlThanhCongCu.add(UIStyle.nhan("Mã / tên:"));
        pnlThanhCongCu.add(txtTim);
        pnlThanhCongCu.add(cmbDanhMuc);
        pnlThanhCongCu.add(btnTim);
        pnlThanhCongCu.add(btnSua);
        pnlThanhCongCu.add(btnXoa);
        pnlThanhCongCu.add(btnXuLyHang);
        pnlThanhCongCu.add(btnLichSuXuLy);
        lblSoLuong2.setFont(UIStyle.font(Font.BOLD, 13));
        lblSoLuong2.setBorder(new EmptyBorder(0, 2, 0, 0));
        JPanel pnlPhanTren = new JPanel();
        pnlPhanTren.setOpaque(false);
        pnlPhanTren.setLayout(new BoxLayout(pnlPhanTren, BoxLayout.Y_AXIS));
        pnlThanhCongCu.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSoLuong2.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlPhanTren.add(pnlThanhCongCu);
        pnlPhanTren.add(Box.createVerticalStrut(8));
        pnlPhanTren.add(lblSoLuong2);
        UIStyle.taoBang(tblDuLieu2, 9, 10, 11);
        tblDuLieu2.setRowHeight(84);
        tblDuLieu2.getColumnModel().getColumn(0).setPreferredWidth(96);
        tblDuLieu2.getColumnModel().getColumn(0).setMaxWidth(110);
        tblDuLieu2.getColumnModel().getColumn(0).setCellRenderer(new ImageRenderer());
        tblDuLieu2.getColumnModel().getColumn(5).setCellEditor(taoBoChonDotNhap());
        tblDuLieu2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblDuLieu2.getSelectedRow() >= 0
                        && tblDuLieu2.columnAtPoint(e.getPoint()) != 5) {
                    suaSanPham();
                }
            }
        });
        pnlThe.add(pnlPhanTren, BorderLayout.NORTH);
        pnlThe.add(UIStyle.cuon(tblDuLieu2), BorderLayout.CENTER);
        return pnlThe;
    }
    public void napLai() {
        napDuLieuComboSanPham();
        napBangSanPham();
    }
    private void napDuLieuComboSanPham() {
        try {
            Muc laySanPhamDangChon = (Muc) cmbDanhMuc.getSelectedItem();
            String maDangChon = laySanPhamDangChon == null ? null : laySanPhamDangChon.ma;
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
                String ten = dot.toString();
                maDotTheoTen.put(ten, dot.getMaPhieuNhap());
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
                combo.addItem("Tổng các đợt nhập");
                try {
                    for (dao.SanPhamDAO.DotNhap dot : SanPhamControl.layDanhSachDotNhapSanPham(dsTongHop.get(modelRow).getMaSanPham()))
                        combo.addItem(dot.toString());
                } catch (Exception ex) {
                    UIStyle.loi(SanPhamUI.this, ex);
                }
                combo.setSelectedItem(value == null ? "Tổng các đợt nhập" : value.toString());
                return super.getTableCellEditorComponent(table, value, selected, row, column);
            }
            @Override
            public boolean stopCellEditing() {
                int row = tblDuLieu2.getEditingRow();
                int modelRow = row < 0 ? -1 : tblDuLieu2.convertRowIndexToModel(row);
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
        if (tenDot == null || tenDot.equals("Tổng các đợt nhập")) {
            sp = dsTongHop.get(row);
            dsHienTai.set(row, sp);
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
            dsHienTai.set(row, sp);
        }
        boolean tongHop = sp.getMaPhieuNhap() == null;
        model.setValueAt(tenDot == null ? "Tổng các đợt nhập" : tenDot, row, 5);
        model.setValueAt(tongHop || sp.getNgayNhap() == null ? "" : sp.getNgayNhap().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), row, 6);
        model.setValueAt(tongHop || sp.getHanSuDung() == null ? "" : sp.getHanSuDung().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), row, 7);
        String trangThai = tongHop ? "" : (sp.getHanSuDung() != null
                && sp.getHanSuDung().isBefore(java.time.LocalDate.now()) ? "Hết hạn"
                        : sp.isNgungKinhDoanh() ? "Ngừng kinh doanh" : "Đang kinh doanh");
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
                        "Tổng các đợt nhập", "", "", "",
                        "", "",
                        sp.getTonKho()
                });
            }
            lblSoLuong2.setText(dsHienTai.size() + " sản phẩm");
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private SanPham laySanPhamDangChon() {
        int row = tblDuLieu2.getSelectedRow();
        if (row < 0) {
            UIStyle.canhBao(this, "Vui lòng chọn một sản phẩm trong bảng.");
            return null;
        }
        return dsHienTai.get(tblDuLieu2.convertRowIndexToModel(row));
    }
    private void themSanPham() {
        moFormSanPham(null);
    }
    private void suaSanPham() {
        SanPham sp = laySanPhamDangChon();
        if (sp != null) {
            if (sp.getMaPhieuNhap() != null) {
                try {
                    SanPham hoSo = SanPhamControl.timKiemSanPham(sp.getMaSanPham(), null, null).stream()
                            .filter(x -> x.getMaSanPham().equals(sp.getMaSanPham())).findFirst().orElse(null);
                    if (hoSo != null) {
                        sp.setGiaNhap(hoSo.getGiaNhap());
                        sp.setTonKho(hoSo.getTonKho());
                    }
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
        form.add(new JLabel("Sản phẩm"));
        form.add(new JLabel(sp.getMaSanPham() + " - " + sp.getTenSanPham()));
        form.add(new JLabel("Đợt nhập"));
        form.add(new JLabel(sp.getMaPhieuNhap()));
        form.add(new JLabel("Tồn hiện tại"));
        form.add(new JLabel(String.valueOf(sp.getTonKho())));
        form.add(new JLabel("Số lượng xử lý"));
        form.add(soLuong);
        form.add(new JLabel("Lý do"));
        form.add(lyDo);
        form.add(new JLabel("Ghi chú"));
        form.add(ghiChu);
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
        DefaultTableModel lichSuModel = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
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
        chan.setOpaque(false);
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
            cmbDanhMuc.setFont(UIStyle.font(Font.PLAIN, 13));
            cmbNhaCungCap.setFont(UIStyle.font(Font.PLAIN, 13));
            cmbNhomTuoi.setFont(UIStyle.font(Font.PLAIN, 13));
            cmbKichThuoc.setFont(UIStyle.font(Font.PLAIN, 13));
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
            btnChonAnh.addActionListener(e -> chonAnhSanPham());
            int dongTiep = 9;
            if (sp != null) {
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
                if (sp.getTonKho() > 0) {
                    txtGiaNhap.setEditable(false);
                    txtGiaNhap.setBackground(UIStyle.READONLY);
                    txtGiaNhap.setToolTipText("Chỉ được đổi giá nhập khi tồn kho bằng 0.");
                }
            }
            if (dangSua) {
                txtMa.setText(sp.getMaSanPham());
                txtMa.setEditable(false);
                txtMa.setBackground(UIStyle.READONLY);
                txtTen.setText(sp.getTenSanPham());
                hienThiAnhSanPham(sp.getMaSanPham());
                txtGiaNhap.setText(String.valueOf((long) sp.getGiaNhap()));
                txtGiaBan.setText(String.valueOf((long) sp.getGiaBan()));
                chonDanhMuc(sp.getMaDanhMuc());
                chonNhaCungCap(sp.getMaNhaCungCap());
                try {
                    java.time.LocalDate han = SanPhamControl.layHanSuDungSomNhatSanPham(sp.getMaSanPham());
                    if (han == null) {
                        txtHanSuDung.setText("Chưa có dữ liệu");
                    } else {
                        txtHanSuDung.setText(han.format(
                                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }
                } catch (Exception ex) {
                    txtHanSuDung.setText("Không tải được dữ liệu");
                    UIStyle.loi(this, ex);
                }
                try {
                    String[] thongTin = SanPhamControl.layThongTinGoiYThucAn(sp.getMaSanPham());
                    if (thongTin != null) {
                        chonGiaTri(cmbNhomTuoi, thongTin[1]);
                        chonGiaTri(cmbKichThuoc, thongTin[2]);
                    }
                } catch (Exception ex) {
                    UIStyle.loi(this, ex);
                }
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
        private void chonDanhMuc(String ma) {
            for (int i = 0; i < cmbDanhMuc.getItemCount(); i++) {
                if (cmbDanhMuc.getItemAt(i).getMaDanhMuc().equals(ma)) {
                    cmbDanhMuc.setSelectedIndex(i);
                    return;
                }
            }
        }
        private void chonNhaCungCap(String ma) {
            for (int i = 0; i < cmbNhaCungCap.getItemCount(); i++) {
                if (cmbNhaCungCap.getItemAt(i).getMaNhaCungCap().equals(ma)) {
                    cmbNhaCungCap.setSelectedIndex(i);
                    return;
                }
            }
        }
        private void chonGiaTri(JComboBox<String> cmbLuaChon, String giaTri) {
            for (int i = 0; i < cmbLuaChon.getItemCount(); i++) {
                if (cmbLuaChon.getItemAt(i).equals(giaTri)) {
                    cmbLuaChon.setSelectedIndex(i);
                    return;
                }
            }
        }
        private void luuSanPham() {
            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            if (ten.isEmpty()) {
                UIStyle.canhBao(this, "Vui lòng nhập tên sản phẩm.");
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
            long giaNhap;
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
