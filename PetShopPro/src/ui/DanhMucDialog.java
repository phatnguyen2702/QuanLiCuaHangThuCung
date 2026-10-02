package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import data.DuLieu;
import model.DanhMuc;

public class DanhMucDialog extends JDialog {
    private final JTextField txtMa = new JTextField(22), txtTen = new JTextField(22);
    private final JTextArea txtMoTa = new JTextArea(3, 22);
    private final DanhMuc dm;
    private boolean saved = false;

    public DanhMucDialog(Window owner, DanhMuc dm) {
        super(owner, dm == null ? "Thêm danh mục" : "Sửa danh mục", ModalityType.APPLICATION_MODAL);
        this.dm = dm;
        JPanel f = new JPanel(new GridBagLayout());
        f.setBorder(new EmptyBorder(12, 12, 4, 12));
        Theme.row(f, 0, "Mã danh mục", txtMa);
        Theme.row(f, 1, "Tên danh mục", txtTen);
        Theme.row(f, 2, "Mô tả", new JScrollPane(txtMoTa));
        if (dm != null) {
            txtMa.setText(dm.getMaDanhMuc()); txtMa.setEnabled(false);
            txtTen.setText(dm.getTenDanhMuc()); txtMoTa.setText(dm.getMoTa());
        } else {
            txtMa.setText(String.format("DM%03d", DuLieu.dsDanhMuc.size() + 1));
        }
        add(f, BorderLayout.CENTER);
        add(Theme.footer(this::luu, this::dispose, "Lưu"), BorderLayout.SOUTH);
        pack(); setLocationRelativeTo(owner);
    }

    private void luu() {
        String ma = txtMa.getText().trim(), ten = txtTen.getText().trim();
        if (ma.isEmpty() || ten.isEmpty()) { Theme.loi(this, "Vui lòng nhập mã và tên danh mục."); return; }
        if (dm == null) {
            if (DuLieu.timDanhMuc(ma) != null) { Theme.loi(this, "Mã danh mục đã tồn tại."); return; }
            DuLieu.dsDanhMuc.add(new DanhMuc(ma, ten, txtMoTa.getText().trim()));
        } else {
            dm.setTenDanhMuc(ten); dm.setMoTa(txtMoTa.getText().trim());
        }
        saved = true; dispose();
    }
    public boolean isSaved() { return saved; }
}
