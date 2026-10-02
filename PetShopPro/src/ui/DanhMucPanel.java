package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import data.DuLieu;
import model.DanhMuc;

public class DanhMucPanel extends JPanel implements Refreshable {
    private final DefaultTableModel model = Theme.model("Mã", "Tên danh mục", "Mô tả", "Số sản phẩm");
    private final JTable table = new JTable(model);
    private final JTextField txtTim = new JTextField(20);
    private final JLabel lblTong = new JLabel();

    public DanhMucPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Quản lý danh mục"); title.setFont(Theme.H1);
        JButton btnThem = Theme.btn("+ Thêm danh mục", Theme.GREEN, Color.WHITE);
        btnThem.addActionListener(e -> moForm(null));
        JPanel north = new JPanel(new BorderLayout()); north.setOpaque(false);
        north.add(title, BorderLayout.WEST); north.add(btnThem, BorderLayout.EAST);

        JPanel stat = Theme.statCard("Tổng danh mục", lblTong);
        stat.setPreferredSize(new Dimension(200, 70));
        JPanel top = new JPanel(new BorderLayout(0, 10)); top.setOpaque(false);
        top.add(north, BorderLayout.NORTH);
        JPanel statRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); statRow.setOpaque(false); statRow.add(stat);
        top.add(statRow, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JButton btnLoc = Theme.btn("Lọc", Theme.NAVY, Color.WHITE), btnSua = Theme.btn("Sửa", Color.WHITE, Theme.NAVY),
                btnXoa = Theme.btn("Xóa", Color.WHITE, Theme.RED);
        btnLoc.addActionListener(e -> refresh());
        txtTim.addActionListener(e -> refresh());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)); tool.setBackground(Color.WHITE);
        tool.add(new JLabel("Tìm danh mục:")); tool.add(txtTim); tool.add(btnLoc); tool.add(btnSua); tool.add(btnXoa);

        JPanel card = Theme.card(new BorderLayout(0, 8));
        card.add(tool, BorderLayout.NORTH);
        card.add(Theme.table(table), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) sua(); }
        });
        refresh();
    }

    @Override public void refresh() {
        model.setRowCount(0);
        String k = txtTim.getText().trim().toLowerCase();
        for (DanhMuc d : DuLieu.dsDanhMuc) {
            if (!k.isEmpty() && !d.getMaDanhMuc().toLowerCase().contains(k) && !d.getTenDanhMuc().toLowerCase().contains(k)) continue;
            long soSp = DuLieu.dsSanPham.stream().filter(s -> s.getMaDanhMuc() == d).count();
            model.addRow(new Object[]{d.getMaDanhMuc(), d.getTenDanhMuc(), d.getMoTa(), soSp});
        }
        lblTong.setText(String.valueOf(DuLieu.dsDanhMuc.size()));
    }

    private DanhMuc chon() {
        int r = table.getSelectedRow();
        if (r < 0) { Theme.loi(this, "Hãy chọn một danh mục trong bảng."); return null; }
        return DuLieu.timDanhMuc((String) model.getValueAt(r, 0));
    }

    private void moForm(DanhMuc dm) {
        DanhMucDialog d = new DanhMucDialog(SwingUtilities.getWindowAncestor(this), dm);
        d.setVisible(true);
        if (d.isSaved()) refresh();
    }

    private void sua() { DanhMuc d = chon(); if (d != null) moForm(d); }

    private void xoa() {
        DanhMuc d = chon();
        if (d == null) return;
        if (DuLieu.dsSanPham.stream().anyMatch(s -> s.getMaDanhMuc() == d)) {
            Theme.loi(this, "Danh mục đang có sản phẩm, không thể xóa."); return;
        }
        if (Theme.xacNhan(this, "Xóa danh mục " + d.getTenDanhMuc() + "?")) { DuLieu.dsDanhMuc.remove(d); refresh(); }
    }
}
