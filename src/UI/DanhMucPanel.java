package UI;

import UI.TrangChu.PhienLamViec;
import UI.TrangChu.UIStyle;
import dao.DanhMucDAO;
import entity.DanhMuc;
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
 * Man hinh "Danh muc" - Quan ly danh muc (nam trong khung TrangChu).
 * Form Them / Sua danh muc la lop long DanhMucDialog o cuoi file.
 */
public class DanhMucPanel extends JPanel {

    private final DanhMucDAO dao = new DanhMucDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã", "Tên danh mục", "Mô tả", "Số sản phẩm", "Tổng tồn kho"}, 0) {
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

        UIStyle.taoBang(bang, 3, 4);
        // Cho chon nhieu dong (Ctrl / Shift) de xoa cung luc
        bang.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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

    public void napLai() {

        try {
            dsHienTai = dao.timKiem(txtTim.getText());

            model.setRowCount(0);
            for (DanhMuc dm : dsHienTai) {
                model.addRow(new Object[]{
                        dm.getMaDanhMuc(),
                        dm.getTenDanhMuc(),
                        dm.getMoTa(),
                        dm.getSoSanPham(),
                        dm.getTongTon()
                });
            }

            lbTong.setText(String.valueOf(dao.demTatCa()));

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    private DanhMuc dangChon() {

        int[] rows = bang.getSelectedRows();

        if (rows.length == 0) {
            UIStyle.canhBao(this, "Vui lòng chọn một danh mục trong bảng.");
            return null;
        }

        if (rows.length > 1) {
            UIStyle.canhBao(this, "Chỉ sửa được từng danh mục một, vui lòng chỉ chọn 1 dòng.");
            return null;
        }

        return dsHienTai.get(rows[0]);
    }

    // =========================================================
    // CHUC NANG
    // =========================================================

    private void them() {

        // Ma danh muc de trong: nguoi dung tu nhap
        DanhMucDialog dlg = new DanhMucDialog(
                SwingUtilities.getWindowAncestor(this),
                null, "");
        dlg.setVisible(true);

        if (dlg.isDaLuu()) {
            napLai();
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

    // Xoa BAT KY dong nao dang chon (chon nhieu dong cung duoc)
    private void xoa() {

        int[] rows = bang.getSelectedRows();

        if (rows.length == 0) {
            UIStyle.canhBao(this, "Vui lòng chọn dòng (danh mục) cần xóa.");
            return;
        }

        ArrayList<DanhMuc> dsXoa = new ArrayList<>();
        for (int r : rows) {
            dsXoa.add(dsHienTai.get(r));
        }

        try {
            // Danh muc co san pham khong the xoa de bao toan lich su kho/ban hang.
            int tongSp = 0;
            StringBuilder ten = new StringBuilder();

            for (DanhMuc dm : dsXoa) {
                int[] c = dao.demLienQuan(dm.getMaDanhMuc());
                tongSp += c[0];
                ten.append("\n  - ").append(dm.getMaDanhMuc())
                   .append(" ").append(dm.getTenDanhMuc());
            }

            StringBuilder hoi = new StringBuilder();
            hoi.append("Xóa ").append(dsXoa.size()).append(" danh mục:")
               .append(ten);

            if (tongSp > 0) {
                UIStyle.canhBao(this, "Không thể xóa danh mục đang có " + tongSp
                        + " sản phẩm. Hãy chuyển sản phẩm sang danh mục khác trước.");
                return;
            }

            hoi.append("\n\nBạn chắc chắn muốn xóa danh mục rỗng?");

            if (!UIStyle.xacNhan(this, hoi.toString())) {
                return;
            }

            for (DanhMuc dm : dsXoa) {
                dao.xoa(dm.getMaDanhMuc());
            }

            napLai();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
            napLai();
        }
    }

    // =========================================================
    // FORM THEM / SUA DANH MUC
    // =========================================================

    /**
     * Form Them / Sua danh muc: ma, ten, mo ta (3 cot cua bang DanhMuc).
     * Khi SUA con co bang san pham thuoc danh muc de ghi THEM so luong
     * (luu thanh phieu nhap kho hoan tat nen ton kho van khop voi database).
     */
    private static class DanhMucDialog extends JDialog {

        private final DanhMucDAO dao = new DanhMucDAO();
        private final boolean dangSua;

        private final JTextField txtMa = UIStyle.o(22);
        private final JTextField txtTen = UIStyle.o(22);
        private final JTextArea txtMoTa = new JTextArea(4, 22);

        private final DefaultTableModel modelSp = new DefaultTableModel(
                new String[]{"Mã SP", "Tên sản phẩm", "Tồn hiện tại", "Thêm số lượng"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 3;
            }
        };
        private final JTable bangSp = new JTable(modelSp);
        private final List<SanPham> dsSanPham = new ArrayList<>();

        private boolean daLuu = false;

        /** dm = null: them moi. maMoi: ma goi y khi them. */
        public DanhMucDialog(Window cha, DanhMuc dm, String maMoi) {

            super(cha, dm == null ? "Thêm danh mục" : "Sửa danh mục",
                    ModalityType.APPLICATION_MODAL);

            dangSua = dm != null;

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(new EmptyBorder(20, 25, 10, 25));

            txtMoTa.setFont(UIStyle.font(Font.PLAIN, 13));
            txtMoTa.setLineWrap(true);
            txtMoTa.setWrapStyleWord(true);
            JScrollPane cuonMoTa = new JScrollPane(txtMoTa);

            UIStyle.hang(form, 0, "Mã danh mục", txtMa, false);
            UIStyle.hang(form, 1, "Tên danh mục", txtTen, false);
            UIStyle.hang(form, 2, "Mô tả", cuonMoTa, true);

            JComponent phanSoLuong = dangSua ? taoBangSoLuong(dm) : null;

            if (dangSua) {
                txtMa.setText(dm.getMaDanhMuc());
                txtMa.setEditable(false);
                txtMa.setBackground(UIStyle.READONLY);
                txtTen.setText(dm.getTenDanhMuc());
                txtMoTa.setText(dm.getMoTa());
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
            add(form, BorderLayout.NORTH);
            if (phanSoLuong != null) {
                add(phanSoLuong, BorderLayout.CENTER);
            }
            add(nut, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(btnLuu);

            pack();
            setResizable(false);
            setLocationRelativeTo(cha);
        }

        // Bang san pham cua danh muc: chi cot "Them so luong" duoc go
        private JComponent taoBangSoLuong(DanhMuc dm) {

            try {
                dsSanPham.addAll(dao.timSanPhamTheoDanhMuc(dm.getMaDanhMuc()));
            } catch (SQLException ex) {
                UIStyle.loi(this, ex);
            }

            for (SanPham sp : dsSanPham) {
                modelSp.addRow(new Object[]{
                        sp.getMaSanPham(), sp.getTenSanPham(), sp.getTonKho(), 0});
            }

            UIStyle.taoBang(bangSp, 2, 3);
            bangSp.setRowHeight(28);

            JLabel tieuDe = UIStyle.nhan("Sản phẩm trong danh mục — nhập số lượng cần thêm vào cột bên phải");

            JScrollPane cuon = UIStyle.cuon(bangSp);
            cuon.setPreferredSize(new Dimension(520, 170));

            JPanel p = new JPanel(new BorderLayout(0, 6));
            p.setBorder(new EmptyBorder(0, 25, 5, 25));
            p.add(tieuDe, BorderLayout.NORTH);

            if (dsSanPham.isEmpty()) {
                JLabel trong = new JLabel("Danh mục này chưa có sản phẩm nào.");
                trong.setBorder(new EmptyBorder(8, 0, 8, 0));
                p.add(trong, BorderLayout.CENTER);
            } else {
                p.add(cuon, BorderLayout.CENTER);
            }

            return p;
        }

        // Doc cot "Them so luong"; tra ve null neu co o nhap sai
        private List<Integer> docSoLuongThem() {

            if (bangSp.isEditing()) {
                bangSp.getCellEditor().stopCellEditing();
            }

            List<Integer> kq = new ArrayList<>();

            for (int i = 0; i < modelSp.getRowCount(); i++) {
                String chu = String.valueOf(modelSp.getValueAt(i, 3)).trim();

                if (chu.isEmpty()) {
                    chu = "0";
                }

                if (!chu.matches("\\d{1,7}")) {
                    UIStyle.canhBao(this, "Số lượng thêm của \""
                            + modelSp.getValueAt(i, 1)
                            + "\" phải là số nguyên không âm.\n"
                            + "(Muốn giảm tồn kho hãy dùng Kiểm kê kho.)");
                    return null;
                }

                kq.add(Integer.parseInt(chu));
            }

            return kq;
        }

        private void luu() {

            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String moTa = txtMoTa.getText().trim();

            if (ma.isEmpty() || ten.isEmpty()) {
                UIStyle.canhBao(this, "Vui lòng nhập mã và tên danh mục.");
                return;
            }

            if (ma.length() > 10) {
                UIStyle.canhBao(this, "Mã danh mục tối đa 10 ký tự.");
                return;
            }

            if (ten.length() > 50) {
                UIStyle.canhBao(this, "Tên danh mục tối đa 50 ký tự.");
                return;
            }

            if (moTa.length() > 255) {
                UIStyle.canhBao(this, "Mô tả tối đa 255 ký tự.");
                return;
            }

            DanhMuc dm = new DanhMuc(ma, ten, moTa.isEmpty() ? null : moTa);

            List<Integer> soLuongThem = dangSua ? docSoLuongThem() : null;
            if (dangSua && soLuongThem == null) {
                return;
            }

            try {
                if (dangSua) {
                    dao.sua(dm);
                    dao.nhapBoSung(
                            dsSanPham, soLuongThem, PhienLamViec.maNV);
                } else {
                    dao.them(dm);
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
