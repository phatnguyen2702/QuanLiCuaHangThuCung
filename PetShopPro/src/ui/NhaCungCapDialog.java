package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import data.DuLieu;
import model.NhaCungCap;

public class NhaCungCapDialog extends JDialog {
    private final JTextField txtMa = new JTextField(22), txtTen = new JTextField(22), txtSdt = new JTextField(22),
            txtEmail = new JTextField(22), txtDiaChi = new JTextField(22);
    private boolean saved = false;

    public NhaCungCapDialog(Window owner) {
        super(owner, "Thêm nhà cung cấp", ModalityType.APPLICATION_MODAL);
        JPanel f = new JPanel(new GridBagLayout());
        f.setBorder(new EmptyBorder(12, 12, 4, 12));
        txtMa.setText(String.format("NCC%03d", DuLieu.dsNhaCungCap.size() + 1));
        Theme.row(f, 0, "Mã NCC", txtMa); Theme.row(f, 1, "Tên NCC", txtTen);
        Theme.row(f, 2, "Số điện thoại", txtSdt); Theme.row(f, 3, "Email", txtEmail);
        Theme.row(f, 4, "Địa chỉ", txtDiaChi);
        add(f, BorderLayout.CENTER);
        add(Theme.footer(this::luu, this::dispose, "Lưu"), BorderLayout.SOUTH);
        pack(); setLocationRelativeTo(owner);
    }

    private void luu() {
        String ma = txtMa.getText().trim(), ten = txtTen.getText().trim(), sdt = txtSdt.getText().trim();
        if (ma.isEmpty() || ten.isEmpty()) { Theme.loi(this, "Vui lòng nhập mã và tên nhà cung cấp."); return; }
        if (!sdt.isEmpty() && !sdt.matches("\\d{9,11}")) { Theme.loi(this, "Số điện thoại phải gồm 9–11 chữ số."); return; }
        for (NhaCungCap n : DuLieu.dsNhaCungCap)
            if (n.getMaNhaCungCap().equals(ma)) { Theme.loi(this, "Mã nhà cung cấp đã tồn tại."); return; }
        DuLieu.dsNhaCungCap.add(new NhaCungCap(ma, ten, sdt, txtEmail.getText().trim(), txtDiaChi.getText().trim()));
        saved = true; dispose();
    }
    public boolean isSaved() { return saved; }
}
