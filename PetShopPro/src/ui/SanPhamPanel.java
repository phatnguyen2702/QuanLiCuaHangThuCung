package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import data.DuLieu;
import model.*;

public class SanPhamPanel extends JPanel implements Refreshable {
    private static final String TAT_CA = "Tất cả danh mục";
    private final DefaultTableModel model = Theme.model("Mã SP", "Tên sản phẩm", "Danh mục", "Nhà cung cấp", "Giá nhập", "Giá bán", "Tồn");
    private final JTable table = new JTable(model);
    private final JTextField txtTim = new JTextField(18);
    private final JComboBox<Object> cboDm = new JComboBox<>();
    private final JLabel lblTong = new JLabel();

    public SanPhamPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Quản lý sản phẩm"); title.setFont(Theme.H1);
        JButton btnThem = Theme.btn("+ Thêm sản phẩm", Theme.GREEN, Color.WHITE);
        btnThem.addActionListener(e -> moForm(null));
        JPanel north = new JPanel(new BorderLayout()); north.setOpaque(false);
        north.add(title, BorderLayout.WEST); north.add(btnThem, BorderLayout.EAST);
        add(north, BorderLayout.NORTH);

        JButton btnTim = Theme.btn("Tìm kiếm", Theme.NAVY, Color.WHITE), btnSua = Theme.btn("Sửa", Color.WHITE, Theme.NAVY),
                btnXoa = Theme.btn("Xóa", Color.WHITE, Theme.RED);
        btnTim.addActionListener(e -> refresh());
        txtTim.addActionListener(e -> refresh());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)); tool.setBackground(Color.WHITE);
        tool.add(new JLabel("Mã / tên:")); tool.add(txtTim); tool.add(cboDm);
        tool.add(btnTim); tool.add(btnSua); tool.add(btnXoa);

        lblTong.setFont(Theme.B);
        JPanel card = Theme.card(new BorderLayout(0, 8));
        JPanel head = new JPanel(new BorderLayout()); head.setBackground(Color.WHITE);
        head.add(tool, BorderLayout.NORTH); head.add(lblTong, BorderLayout.SOUTH);
        card.add(head, BorderLayout.NORTH);
        card.add(Theme.table(table), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) sua(); }
        });
        refresh();
    }

    @Override public void refresh() {
        Object dangChon = cboDm.getSelectedItem();
        cboDm.removeAllItems();
        cboDm.addItem(TAT_CA);
        for (DanhMuc d : DuLieu.dsDanhMuc) cboDm.addItem(d);
        cboDm.setSelectedItem(dangChon != null && (dangChon instanceof DanhMuc ? DuLieu.dsDanhMuc.contains(dangChon) : true) ? dangChon : TAT_CA);

        model.setRowCount(0);
        String k = txtTim.getText().trim().toLowerCase();
        Object dm = cboDm.getSelectedItem();
        for (SanPham s : DuLieu.dsSanPham) {
            if (!k.isEmpty() && !s.getMaSanPham().toLowerCase().contains(k) && !s.getTenSanPham().toLowerCase().contains(k)) continue;
            if (dm instanceof DanhMuc && s.getMaDanhMuc() != dm) continue;
            model.addRow(new Object[]{s.getMaSanPham(), s.getTenSanPham(), s.getMaDanhMuc().getTenDanhMuc(),
                    s.getMaNhaCungCap().getTenNhaCungCap(), Theme.tien(s.getGiaNhap()), Theme.tien(s.getGiaBan()), s.getSoLuongTon()});
        }
        lblTong.setText(DuLieu.dsSanPham.size() + " sản phẩm đang kinh doanh");
    }

    private SanPham chon() {
        int r = table.getSelectedRow();
        if (r < 0) { Theme.loi(this, "Hãy chọn một sản phẩm trong bảng."); return null; }
        return DuLieu.timSanPham((String) model.getValueAt(r, 0));
    }

    private void moForm(SanPham sp) {
        SanPhamDialog d = new SanPhamDialog(SwingUtilities.getWindowAncestor(this), sp);
        d.setVisible(true);
        if (d.isSaved()) refresh();
    }

    private void sua() { SanPham s = chon(); if (s != null) moForm(s); }

    private void xoa() {
        SanPham s = chon();
        if (s != null && Theme.xacNhan(this, "Xóa sản phẩm " + s.getTenSanPham() + "?")) { DuLieu.dsSanPham.remove(s); refresh(); }
    }
}
