package UI;
import UI.TrangChuUI.UIStyle;
import control.DanhMucControl;
import entity.DanhMuc;
import entity.SanPham;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
public class DanhMucUI extends JFrame {
private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã", "Tên danh mục", "Mô tả", "Số sản phẩm", "Tổng tồn kho" }, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable tblDuLieu2 = new JTable(model);
    private final JTextField txtTim = UIStyle.o(18);
    private final JLabel lblTong = new JLabel("0");
    private ArrayList<DanhMuc> dsHienTai = new ArrayList<>();
    public DanhMucUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.LIGHT_BLUE);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(20, 25, 20, 25));
        add(taoPhanDau(), BorderLayout.NORTH);
        add(taoPhanBang(), BorderLayout.CENTER);
        napLai();
    }
    private JPanel taoPhanDau() {
        JPanel pnlDau2 = new JPanel();
        pnlDau2.setOpaque(false);
        pnlDau2.setLayout(new BoxLayout(pnlDau2, BoxLayout.Y_AXIS));
        JLabel lblTieuDe2 = new JLabel("Quản lý danh mục");
        lblTieuDe2.setFont(UIStyle.font(Font.BOLD, 26));
        JButton btnThem = UIStyle.nutXanh("+ Thêm danh mục");
        btnThem.addActionListener(e -> themDanhMuc());
        JPanel pnlHang = new JPanel(new BorderLayout());
        pnlHang.setOpaque(false);
        pnlHang.add(lblTieuDe2, BorderLayout.WEST);
        pnlHang.add(btnThem, BorderLayout.EAST);
        JPanel pnlThe = UIStyle.theThongKe("Tổng danh mục", lblTong);
        pnlThe.setPreferredSize(new Dimension(250, 88));
        JPanel pnlHangThe = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlHangThe.setOpaque(false);
        pnlHangThe.add(pnlThe);
        pnlDau2.add(pnlHang);
        pnlDau2.add(Box.createVerticalStrut(15));
        pnlDau2.add(pnlHangThe);
        pnlDau2.add(Box.createVerticalStrut(15));
        return pnlDau2;
    }
    private JPanel taoPhanBang() {
        JPanel pnlThe = UIStyle.the();
        pnlThe.setLayout(new BorderLayout(0, 12));
        JPanel pnlThanhCongCu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlThanhCongCu.setOpaque(false);
        JButton btnLoc = UIStyle.nutChinh("Lọc");
        JButton btnSua = UIStyle.nutPhu("Sửa");
        JButton btnXoa = UIStyle.nutXoa("Xóa");
        btnLoc.addActionListener(e -> napLai());
        txtTim.addActionListener(e -> napLai());
        btnSua.addActionListener(e -> suaDanhMuc());
        btnXoa.addActionListener(e -> xoaDanhMuc());
        pnlThanhCongCu.add(UIStyle.nhan("Tìm danh mục (mã/tên):"));
        pnlThanhCongCu.add(txtTim);
        pnlThanhCongCu.add(btnLoc);
        pnlThanhCongCu.add(btnSua);
        pnlThanhCongCu.add(btnXoa);
        UIStyle.taoBang(tblDuLieu2, 3, 4);
        tblDuLieu2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tblDuLieu2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblDuLieu2.getSelectedRow() >= 0) {
                    suaDanhMuc();
                }
            }
        });
        pnlThe.add(pnlThanhCongCu, BorderLayout.NORTH);
        pnlThe.add(UIStyle.cuon(tblDuLieu2), BorderLayout.CENTER);
        return pnlThe;
    }
    public void napLai() {
        try {
            dsHienTai = DanhMucControl.timKiemDanhMuc(txtTim.getText());
            model.setRowCount(0);
            for (DanhMuc dm : dsHienTai) {
                model.addRow(new Object[] {
                        dm.getMaDanhMuc(),
                        dm.getTenDanhMuc(),
                        dm.getMoTa(),
                        dm.getSoSanPham(),
                        dm.getTongTon()
                });
            }
            lblTong.setText(String.valueOf(DanhMucControl.demTatCaDanhMuc()));
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private DanhMuc dangChon() {
        int[] rows = tblDuLieu2.getSelectedRows();
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
    private void themDanhMuc() {
        try {
            String maMoi = DanhMucControl.taoMaDanhMucMoi();
            DanhMucDialog winDlg = new DanhMucDialog(
                    SwingUtilities.getWindowAncestor(this), null, maMoi);
            winDlg.setVisible(true);
            if (winDlg.isDaLuu())
                napLai();
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private void suaDanhMuc() {
        DanhMuc dm = dangChon();
        if (dm == null) {
            return;
        }
        DanhMucDialog winDlg = new DanhMucDialog(
                SwingUtilities.getWindowAncestor(this), dm, null);
        winDlg.setVisible(true);
        if (winDlg.isDaLuu()) {
            napLai();
        }
    }
    private void xoaDanhMuc() {
        int[] rows = tblDuLieu2.getSelectedRows();
        if (rows.length == 0) {
            UIStyle.canhBao(this, "Vui lòng chọn dòng (danh mục) cần xóa.");
            return;
        }
        ArrayList<DanhMuc> dsXoa = new ArrayList<>();
        for (int r : rows) {
            dsXoa.add(dsHienTai.get(r));
        }
        try {
            int tongSp = 0;
            StringBuilder ten = new StringBuilder();
            for (DanhMuc dm : dsXoa) {
                int[] c = DanhMucControl.demSanPhamTrongDanhMuc(dm.getMaDanhMuc());
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
                if (!DanhMucControl.xoaDanhMuc(dm.getMaDanhMuc())) {
                    throw new IllegalStateException("Không xóa được danh mục " + dm.getMaDanhMuc() + ".");
                }
            }
            napLai();
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
            napLai();
        }
    }
    private static class DanhMucDialog extends JDialog {
        private final boolean dangSua;
        private final JTextField txtMa = UIStyle.o(22);
        private final JTextField txtTen = UIStyle.o(22);
        private final JTextArea txaMoTa = new JTextArea(4, 22);
        private final DefaultTableModel modelSp = new DefaultTableModel(
                new String[] { "Mã SP", "Tên sản phẩm", "Tồn hiện tại" }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        private final JTable tblBangSp = new JTable(modelSp);
        private final List<SanPham> dsSanPham = new ArrayList<>();
        private boolean daLuu = false;
        public DanhMucDialog(Window winCha, DanhMuc dm, String maMoi) {
            super(winCha, dm == null ? "Thêm danh mục" : "Sửa danh mục",
                    ModalityType.APPLICATION_MODAL);
            dangSua = dm != null;
            JPanel pnlForm2 = new JPanel(new GridBagLayout());
            pnlForm2.setBorder(new EmptyBorder(20, 25, 10, 25));
            txaMoTa.setFont(UIStyle.font(Font.PLAIN, 13));
            txaMoTa.setLineWrap(true);
            txaMoTa.setWrapStyleWord(true);
            JScrollPane scrCuonMoTa = new JScrollPane(txaMoTa);
            UIStyle.hang(pnlForm2, 0, "Mã danh mục", txtMa, false);
            UIStyle.hang(pnlForm2, 1, "Tên danh mục", txtTen, false);
            UIStyle.hang(pnlForm2, 2, "Mô tả", scrCuonMoTa, true);
            JComponent phanSanPham = dangSua ? taoBangSanPham(dm) : null;
            if (dangSua) {
                txtMa.setText(dm.getMaDanhMuc());
                txtMa.setEditable(false);
                txtMa.setBackground(UIStyle.READONLY);
                txtTen.setText(dm.getTenDanhMuc());
                txaMoTa.setText(dm.getMoTa());
            } else {
                txtMa.setText(maMoi);
                txtMa.setEditable(false);
                txtMa.setBackground(UIStyle.READONLY);
            }
            JButton btnHuy = UIStyle.nutPhu("Hủy");
            JButton btnLuu = UIStyle.nutXanh("Lưu");
            btnHuy.addActionListener(e -> dispose());
            btnLuu.addActionListener(e -> luuDanhMuc());
            JPanel pnlNut2 = UIStyle.hangNut(btnHuy, btnLuu);
            pnlNut2.setBorder(new EmptyBorder(5, 25, 20, 25));
            setLayout(new BorderLayout());
            add(pnlForm2, BorderLayout.NORTH);
            if (phanSanPham != null) {
                add(phanSanPham, BorderLayout.CENTER);
            }
            add(pnlNut2, BorderLayout.SOUTH);
            getRootPane().setDefaultButton(btnLuu);
            pack();
            setResizable(false);
            setLocationRelativeTo(winCha);
        }
        private JComponent taoBangSanPham(DanhMuc dm) {
            try {
                dsSanPham.addAll(DanhMucControl.laySanPhamTheoDanhMuc(dm.getMaDanhMuc()));
            } catch (Exception ex) {
                UIStyle.loi(this, ex);
            }
            for (SanPham sp : dsSanPham) {
                modelSp.addRow(new Object[] { sp.getMaSanPham(), sp.getTenSanPham(), sp.getTonKho() });
            }
            UIStyle.taoBang(tblBangSp, 2);
            tblBangSp.setRowHeight(28);
            JLabel lblTieuDe2 = UIStyle.nhan("Sản phẩm trong danh mục");
            JScrollPane scrCuon = UIStyle.cuon(tblBangSp);
            scrCuon.setPreferredSize(new Dimension(520, 170));
            JPanel pnlKhung2 = new JPanel(new BorderLayout(0, 6));
            pnlKhung2.setBorder(new EmptyBorder(0, 25, 5, 25));
            pnlKhung2.add(lblTieuDe2, BorderLayout.NORTH);
            if (dsSanPham.isEmpty()) {
                JLabel lblTrong = new JLabel("Danh mục này chưa có sản phẩm nào.");
                lblTrong.setBorder(new EmptyBorder(8, 0, 8, 0));
                pnlKhung2.add(lblTrong, BorderLayout.CENTER);
            } else {
                pnlKhung2.add(scrCuon, BorderLayout.CENTER);
            }
            return pnlKhung2;
        }
        private void luuDanhMuc() {
            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String moTa = txaMoTa.getText().trim();
            if (ten.isEmpty()) {
                UIStyle.canhBao(this, "Vui lòng nhập tên danh mục.");
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
            try {
                if (dangSua) {
                    if (!DanhMucControl.capNhatDanhMuc(dm)) throw new IllegalStateException("Không cập nhật được danh mục.");
                } else {
                    if (!DanhMucControl.themDanhMuc(dm)) throw new IllegalStateException("Không thêm được danh mục.");
                }
                daLuu = true;
                dispose();
            } catch (Exception ex) {
                UIStyle.loi(this, ex);
            }
        }
        public boolean isDaLuu() {
            return daLuu;
        }
    }
}
