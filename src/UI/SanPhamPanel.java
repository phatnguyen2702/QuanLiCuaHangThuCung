package UI;

import dao.DanhMucDAO;
import dao.NhaCungCapDAO;
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

/** Tab "San pham" - Quan ly san pham. */
public class SanPhamPanel extends JPanel implements ManHinhKho {

    private final SanPhamDAO dao = new SanPhamDAO();
    private final DanhMucDAO danhMucDAO = new DanhMucDAO();
    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAO();

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
    private final JComboBox<UIStyle.Muc> cboDanhMuc = new JComboBox<>();
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

    @Override
    public void napLai() {
        napCombo();
        napBang();
    }

    private void napCombo() {

        try {
            UIStyle.Muc dangChon = (UIStyle.Muc) cboDanhMuc.getSelectedItem();
            String maDangChon = dangChon == null ? null : dangChon.ma;

            dangNapCombo = true;
            cboDanhMuc.removeAllItems();
            cboDanhMuc.addItem(new UIStyle.Muc(null, "Tất cả danh mục"));

            for (DanhMuc dm : danhMucDAO.getAll()) {
                UIStyle.Muc m = new UIStyle.Muc(dm.getMaDanhMuc(),
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
            UIStyle.Muc muc = (UIStyle.Muc) cboDanhMuc.getSelectedItem();
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
            ArrayList<DanhMuc> dsDanhMuc = danhMucDAO.getAll();
            ArrayList<NhaCungCap> dsNcc = nhaCungCapDAO.getAll();

            if (dsDanhMuc.isEmpty() || dsNcc.isEmpty()) {
                UIStyle.canhBao(this,
                        "Cần có ít nhất 1 danh mục và 1 nhà cung cấp trước khi thêm sản phẩm.");
                return;
            }

            SanPhamDialog dlg = new SanPhamDialog(
                    SwingUtilities.getWindowAncestor(this),
                    sp,
                    sp == null ? dao.taoMaMoi() : null,
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

        if (!UIStyle.xacNhan(this,
                "Xóa sản phẩm \"" + sp.getTenSanPham() + "\"?")) {
            return;
        }

        try {
            dao.xoa(sp.getMaSanPham());
            napBang();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }
}
