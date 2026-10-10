package UI;

import dao.DanhMucDAO;
import entity.DanhMuc;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;

/** Tab "Danh muc" - Quan ly danh muc. */
public class DanhMucPanel extends JPanel implements ManHinhKho {

    private final DanhMucDAO dao = new DanhMucDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã", "Tên danh mục", "Mô tả", "Số sản phẩm"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable bang = new JTable(model);
    private final JTextField txtTim = UIStyle.o(18);
    private final JLabel lbTong = new JLabel("0");

    private ArrayList<DanhMuc> dsHienTai = new ArrayList<>();

    public DanhMucPanel() {

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

        JPanel dau = new JPanel();
        dau.setOpaque(false);
        dau.setLayout(new BoxLayout(dau, BoxLayout.Y_AXIS));

        JLabel tieuDe = new JLabel("Quản lý danh mục");
        tieuDe.setFont(UIStyle.font(Font.BOLD, 26));

        JButton btnThem = UIStyle.nutXanh("+ Thêm danh mục");
        btnThem.addActionListener(e -> them());

        JPanel hang = new JPanel(new BorderLayout());
        hang.setOpaque(false);
        hang.add(tieuDe, BorderLayout.WEST);
        hang.add(btnThem, BorderLayout.EAST);

        JPanel the = UIStyle.theThongKe("Tổng danh mục", lbTong);
        the.setPreferredSize(new Dimension(250, 88));

        JPanel hangThe = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        hangThe.setOpaque(false);
        hangThe.add(the);

        dau.add(hang);
        dau.add(Box.createVerticalStrut(15));
        dau.add(hangThe);
        dau.add(Box.createVerticalStrut(15));

        return dau;
    }

    private JPanel taoPhanBang() {

        JPanel the = UIStyle.the();
        the.setLayout(new BorderLayout(0, 12));

        JPanel thanhCongCu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        thanhCongCu.setOpaque(false);

        JButton btnLoc = UIStyle.nutChinh("Lọc");
        JButton btnSua = UIStyle.nutPhu("Sửa");
        JButton btnXoa = UIStyle.nutXoa("Xóa");

        btnLoc.addActionListener(e -> napLai());
        txtTim.addActionListener(e -> napLai());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());

        thanhCongCu.add(UIStyle.nhan("Tìm danh mục:"));
        thanhCongCu.add(txtTim);
        thanhCongCu.add(btnLoc);
        thanhCongCu.add(btnSua);
        thanhCongCu.add(btnXoa);

        UIStyle.taoBang(bang, 3);
        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bang.getSelectedRow() >= 0) {
                    sua();
                }
            }
        });

        the.add(thanhCongCu, BorderLayout.NORTH);
        the.add(UIStyle.cuon(bang), BorderLayout.CENTER);

        return the;
    }

    // =========================================================
    // DU LIEU
    // =========================================================

    @Override
    public void napLai() {

        try {
            dsHienTai = dao.timKiem(txtTim.getText());

            model.setRowCount(0);
            for (DanhMuc dm : dsHienTai) {
                model.addRow(new Object[]{
                        dm.getMaDanhMuc(),
                        dm.getTenDanhMuc(),
                        dm.getMoTa(),
                        dm.getSoSanPham()
                });
            }

            lbTong.setText(String.valueOf(dao.demTatCa()));

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    private DanhMuc dangChon() {

        int row = bang.getSelectedRow();

        if (row < 0) {
            UIStyle.canhBao(this, "Vui lòng chọn một danh mục trong bảng.");
            return null;
        }

        return dsHienTai.get(row);
    }

    // =========================================================
    // CHUC NANG
    // =========================================================

    private void them() {

        try {
            DanhMucDialog dlg = new DanhMucDialog(
                    SwingUtilities.getWindowAncestor(this),
                    null, dao.taoMaMoi());
            dlg.setVisible(true);

            if (dlg.isDaLuu()) {
                napLai();
            }

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    private void sua() {

        DanhMuc dm = dangChon();
        if (dm == null) {
            return;
        }

        DanhMucDialog dlg = new DanhMucDialog(
                SwingUtilities.getWindowAncestor(this), dm, null);
        dlg.setVisible(true);

        if (dlg.isDaLuu()) {
            napLai();
        }
    }

    private void xoa() {

        DanhMuc dm = dangChon();
        if (dm == null) {
            return;
        }

        if (dm.getSoSanPham() > 0) {
            UIStyle.canhBao(this, "Danh mục \"" + dm.getTenDanhMuc()
                    + "\" đang có " + dm.getSoSanPham()
                    + " sản phẩm nên không thể xóa.");
            return;
        }

        if (!UIStyle.xacNhan(this,
                "Xóa danh mục \"" + dm.getTenDanhMuc() + "\"?")) {
            return;
        }

        try {
            dao.xoa(dm.getMaDanhMuc());
            napLai();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }
}
