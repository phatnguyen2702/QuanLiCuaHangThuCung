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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Man hinh "San pham" - Quan ly san pham (nam trong khung TrangChu).
 * Form Them / Sua san pham la lop long SanPhamDialog o cuoi file.
 */
public class SanPhamPanel extends JPanel {

    private final SanPhamDAO dao = new SanPhamDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã SP", "Tên sản phẩm", "Danh mục",
                    "Nhà cung cấp", "Giá nhập", "Giá bán", "Tồn"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable bang = new JTable(model);
    private final JTextField txtTim = UIStyle.o(16);
    private final JComboBox<Muc> cboDanhMuc = new JComboBox<>();
    private final JLabel lbSoLuong = new JLabel("0 sản phẩm");

    private ArrayList<SanPham> dsHienTai = new ArrayList<>();
    private boolean dangNapCombo = false;

    public SanPhamPanel() {

        setLayout(new BorderLayout());
        setBackground(UIStyle.LIGHT_BLUE);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        add(taoPhanDau(), BorderLayout.NORTH);
        add(taoPhanBang(), BorderLayout.CENTER);

        napLai();
    }

    // =========================================================
    // GIAO DIEN
    // =========================================================

    private JPanel taoPhanDau() {

        JLabel tieuDe = new JLabel("Quản lý sản phẩm");
        tieuDe.setFont(UIStyle.font(Font.BOLD, 26));

        JButton btnThem = UIStyle.nutXanh("+ Thêm sản phẩm");
        btnThem.addActionListener(e -> them());

        JPanel hang = new JPanel(new BorderLayout());
        hang.setOpaque(false);
        hang.setBorder(new EmptyBorder(0, 0, 15, 0));
        hang.add(tieuDe, BorderLayout.WEST);
        hang.add(btnThem, BorderLayout.EAST);

        return hang;
    }

    private JPanel taoPhanBang() {

        JPanel the = UIStyle.the();
        the.setLayout(new BorderLayout(0, 10));

        cboDanhMuc.setFont(UIStyle.font(Font.PLAIN, 13));
        cboDanhMuc.addActionListener(e -> {
            if (!dangNapCombo) {
                napBang();
            }
        });

        JButton btnTim = UIStyle.nutChinh("Tìm kiếm");
        JButton btnSua = UIStyle.nutPhu("Sửa");
        JButton btnXoa = UIStyle.nutXoa("Xóa");

        btnTim.addActionListener(e -> napBang());
        txtTim.addActionListener(e -> napBang());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());

        JPanel thanhCongCu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        thanhCongCu.setOpaque(false);
        thanhCongCu.add(UIStyle.nhan("Mã / tên:"));
        thanhCongCu.add(txtTim);
        thanhCongCu.add(cboDanhMuc);
        thanhCongCu.add(btnTim);
        thanhCongCu.add(btnSua);
        thanhCongCu.add(btnXoa);

        lbSoLuong.setFont(UIStyle.font(Font.BOLD, 13));
        lbSoLuong.setBorder(new EmptyBorder(0, 2, 0, 0));

        JPanel phanTren = new JPanel();
        phanTren.setOpaque(false);
        phanTren.setLayout(new BoxLayout(phanTren, BoxLayout.Y_AXIS));
        thanhCongCu.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbSoLuong.setAlignmentX(Component.LEFT_ALIGNMENT);
        phanTren.add(thanhCongCu);
        phanTren.add(Box.createVerticalStrut(8));
        phanTren.add(lbSoLuong);

        UIStyle.taoBang(bang, 4, 5, 6);
        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bang.getSelectedRow() >= 0) {
                    sua();
                }
            }
        });

        the.add(phanTren, BorderLayout.NORTH);
        the.add(UIStyle.cuon(bang), BorderLayout.CENTER);

        return the;
    }

    // =========================================================
    // DU LIEU
    // =========================================================

    public void napLai() {
        napCombo();
        napBang();
    }

    private void napCombo() {

        try {
            Muc dangChon = (Muc) cboDanhMuc.getSelectedItem();
            String maDangChon = dangChon == null ? null : dangChon.ma;

            dangNapCombo = true;
            cboDanhMuc.removeAllItems();
            cboDanhMuc.addItem(new Muc(null, "Tất cả danh mục"));

            for (DanhMuc dm : dao.layDanhSachDanhMuc()) {
                Muc m = new Muc(dm.getMaDanhMuc(),
                        dm.getTenDanhMuc());
                cboDanhMuc.addItem(m);
                if (m.ma.equals(maDangChon)) {
                    cboDanhMuc.setSelectedItem(m);
                }
            }

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        } finally {
            dangNapCombo = false;
        }
    }

    private void napBang() {

        try {
            Muc muc = (Muc) cboDanhMuc.getSelectedItem();
            String maDanhMuc = muc == null ? null : muc.ma;

            dsHienTai = dao.timKiem(txtTim.getText(), maDanhMuc, null);

            model.setRowCount(0);
            for (SanPham sp : dsHienTai) {
                model.addRow(new Object[]{
                        sp.getMaSanPham(),
                        sp.getTenSanPham(),
                        sp.getTenDanhMuc(),
                        sp.getTenNhaCungCap(),
                        UIStyle.tien(sp.getGiaNhap()),
                        UIStyle.tien(sp.getGiaBan()),
                        sp.getTonKho()
                });
            }

            lbSoLuong.setText(dsHienTai.size() + " sản phẩm");

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    private SanPham dangChon() {

        int row = bang.getSelectedRow();

        if (row < 0) {
            UIStyle.canhBao(this, "Vui lòng chọn một sản phẩm trong bảng.");
            return null;
        }

        return dsHienTai.get(row);
    }

    // =========================================================
    // CHUC NANG
    // =========================================================

    private void them() {
        moForm(null);
    }

    private void sua() {
        SanPham sp = dangChon();
        if (sp != null) {
            moForm(sp);
        }
    }

    private void moForm(SanPham sp) {

        try {
            ArrayList<DanhMuc> dsDanhMuc = dao.layDanhSachDanhMuc();
            ArrayList<NhaCungCap> dsNcc = dao.layDanhSachNhaCungCap();

            if (dsDanhMuc.isEmpty() || dsNcc.isEmpty()) {
                UIStyle.canhBao(this,
                        "Cần có ít nhất 1 danh mục và 1 nhà cung cấp trước khi thêm sản phẩm.");
                return;
            }

            SanPhamDialog dlg = new SanPhamDialog(
                    SwingUtilities.getWindowAncestor(this),
                    sp,
                    sp == null ? "" : null,   // them moi: ma de trong, nguoi dung tu nhap
                    dsDanhMuc, dsNcc);
            dlg.setVisible(true);

            if (dlg.isDaLuu()) {
                napBang();
            }

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    private void xoa() {

        SanPham sp = dangChon();
        if (sp == null) {
            return;
        }

        try {
            // Da co phieu nhap / hoa don / kiem ke thi khong xoa duoc (khoa ngoai)
            int lichSu = dao.demLichSu(sp.getMaSanPham());
            if (lichSu > 0) {
                UIStyle.canhBao(this, "Sản phẩm \"" + sp.getTenSanPham()
                        + "\" đã phát sinh " + lichSu
                        + " dòng nhập kho / hóa đơn / kiểm kê nên không thể xóa.\n"
                        + "Muốn xóa cả sản phẩm lẫn lịch sử, hãy xóa ở màn hình Danh mục.");
                return;
            }

            if (!UIStyle.xacNhan(this,
                    "Xóa sản phẩm \"" + sp.getTenSanPham() + "\"?")) {
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

    // =========================================================
    // FORM THEM / SUA SAN PHAM
    // =========================================================

    /**
     * Form Them / Sua san pham - dung cac cot cua bang SanPham:
     * ma, ten, giaNhap, giaBan, maDanhMuc, maNhaCungCap.
     * Ton kho khong phai cot cua SanPham, nen o "Them so luong" duoc luu thanh
     * 1 phieu nhap kho hoan tat (ton = tong nhap - ban +/- kiem ke van khop).
     */
    private static class SanPhamDialog extends JDialog {

        private final SanPhamDAO dao = new SanPhamDAO();
        private final boolean dangSua;

        private final JTextField txtMa = UIStyle.o(22);
        private final JTextField txtTen = UIStyle.o(22);
        private final JTextField txtGiaNhap = UIStyle.o(22);
        private final JTextField txtGiaBan = UIStyle.o(22);
        private final JSpinner spnThem =
                new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
        private final JComboBox<DanhMuc> cboDanhMuc = new JComboBox<>();
        private final JComboBox<NhaCungCap> cboNhaCungCap = new JComboBox<>();
        private final JComboBox<String> cboNhomTuoi = new JComboBox<>(new String[]{
                "Mọi độ tuổi", "Con non", "Trưởng thành", "Cao tuổi", "Chưa xác định"});
        private final JComboBox<String> cboKichThuoc = new JComboBox<>(new String[]{
                "Mọi kích thước", "Nhỏ", "Trung bình", "Lớn", "Chưa xác định"});

        private boolean daLuu = false;

        /** sp = null: them moi. maMoi: ma goi y khi them. */
        public SanPhamDialog(Window cha, SanPham sp, String maMoi,
                             List<DanhMuc> dsDanhMuc,
                             List<NhaCungCap> dsNhaCungCap) {

            super(cha, sp == null ? "Thêm sản phẩm" : "Sửa sản phẩm",
                    ModalityType.APPLICATION_MODAL);

            dangSua = sp != null;

            for (DanhMuc dm : dsDanhMuc) {
                cboDanhMuc.addItem(dm);
            }
            for (NhaCungCap n : dsNhaCungCap) {
                cboNhaCungCap.addItem(n);
            }

            cboDanhMuc.setFont(UIStyle.font(Font.PLAIN, 13));
            cboNhaCungCap.setFont(UIStyle.font(Font.PLAIN, 13));
            cboNhomTuoi.setFont(UIStyle.font(Font.PLAIN, 13));
            cboKichThuoc.setFont(UIStyle.font(Font.PLAIN, 13));

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(new EmptyBorder(20, 25, 10, 25));

            UIStyle.hang(form, 0, "Mã sản phẩm", txtMa, false);
            UIStyle.hang(form, 1, "Tên sản phẩm", txtTen, false);
            UIStyle.hang(form, 2, "Giá nhập", txtGiaNhap, false);
            UIStyle.hang(form, 3, "Giá bán", txtGiaBan, false);
            UIStyle.hang(form, 4, "Danh mục", cboDanhMuc, false);
            UIStyle.hang(form, 5, "Nhà cung cấp", cboNhaCungCap, false);
            UIStyle.hang(form, 6, "Độ tuổi phù hợp", cboNhomTuoi, false);
            UIStyle.hang(form, 7, "Kích thước phù hợp", cboKichThuoc, false);

            spnThem.setFont(UIStyle.font(Font.PLAIN, 13));
            int dongTiep = 8;
            if (sp != null) {
                UIStyle.hang(form, dongTiep++, "Tồn hiện tại",
                        UIStyle.oChiDoc(String.valueOf(sp.getTonKho())), false);
            }
            UIStyle.hang(form, dongTiep,
                    sp == null ? "Số lượng ban đầu" : "Thêm số lượng", spnThem, false);

            if (dangSua) {
                txtMa.setText(sp.getMaSanPham());
                txtMa.setEditable(false);
                txtMa.setBackground(UIStyle.READONLY);
                txtTen.setText(sp.getTenSanPham());
                txtGiaNhap.setText(String.valueOf((long) sp.getGiaNhap()));
                txtGiaBan.setText(String.valueOf((long) sp.getGiaBan()));
                chonDanhMuc(sp.getMaDanhMuc());
                chonNhaCungCap(sp.getMaNhaCungCap());
                try {
                    String[] thongTin = dao.layThongTinGoiY(sp.getMaSanPham());
                    if (thongTin != null) {
                        chonGiaTri(cboNhomTuoi, thongTin[1]);
                        chonGiaTri(cboKichThuoc, thongTin[2]);
                    }
                } catch (SQLException ex) {
                    UIStyle.loi(this, ex);
                }
            } else {
                txtMa.setText(maMoi);
            }

            JButton btnHuy = UIStyle.nutPhu("Hủy");
            JButton btnLuu = UIStyle.nutXanh("Lưu");
            btnHuy.addActionListener(e -> dispose());
            btnLuu.addActionListener(e -> luu());

            JPanel nut = UIStyle.hangNut(btnHuy, btnLuu);
            nut.setBorder(new EmptyBorder(5, 25, 20, 25));

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(nut, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(btnLuu);

            pack();
            setResizable(false);
            setLocationRelativeTo(cha);
        }

        private void chonDanhMuc(String ma) {
            for (int i = 0; i < cboDanhMuc.getItemCount(); i++) {
                if (cboDanhMuc.getItemAt(i).getMaDanhMuc().equals(ma)) {
                    cboDanhMuc.setSelectedIndex(i);
                    return;
                }
            }
        }

        private void chonNhaCungCap(String ma) {
            for (int i = 0; i < cboNhaCungCap.getItemCount(); i++) {
                if (cboNhaCungCap.getItemAt(i).getMaNhaCungCap().equals(ma)) {
                    cboNhaCungCap.setSelectedIndex(i);
                    return;
                }
            }
        }

        private void chonGiaTri(JComboBox<String> combo, String giaTri) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (combo.getItemAt(i).equals(giaTri)) {
                    combo.setSelectedIndex(i);
                    return;
                }
            }
        }

        private void luu() {

            String ma = txtMa.getText().trim();
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

            DanhMuc dm = (DanhMuc) cboDanhMuc.getSelectedItem();
            NhaCungCap ncc = (NhaCungCap) cboNhaCungCap.getSelectedItem();

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
                dao.luuThongTinGoiY(ma, dm.getTenDanhMuc(), ten,
                        (String) cboNhomTuoi.getSelectedItem(),
                        (String) cboKichThuoc.getSelectedItem());

                int them = (Integer) spnThem.getValue();
                if (them > 0) {
                    dao.nhapBoSung(
                            List.of(sp), List.of(them), PhienLamViec.maNV);
                }

                daLuu = true;
                dispose();

            } catch (SQLException ex) {
                UIStyle.loi(this, ex);
            }
        }

        public boolean isDaLuu() {
            return daLuu;
        }
    }
}
