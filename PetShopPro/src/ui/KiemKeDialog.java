package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.ChiTietPhieuKiemKe;

public class KiemKeDialog extends JDialog {
    private final JSpinner spThucTe;
    private final JTextField txtGhiChu = new JTextField(22);
    private final ChiTietPhieuKiemKe ct;
    private boolean saved = false;

    public KiemKeDialog(Window owner, ChiTietPhieuKiemKe ct) {
        super(owner, "Nhập số liệu kiểm kê", ModalityType.APPLICATION_MODAL);
        this.ct = ct;
        spThucTe = new JSpinner(new SpinnerNumberModel(ct.daKiem() ? ct.getSoLuongThucTe() : ct.getSoLuongHeThong(), 0, 1000000, 1));
        JPanel f = new JPanel(new GridBagLayout());
        f.setBorder(new EmptyBorder(12, 12, 4, 12));
        Theme.row(f, 0, "Sản phẩm", new JLabel(ct.getMaSanPham().toString()));
        Theme.row(f, 1, "Tồn hệ thống", new JLabel(String.valueOf(ct.getSoLuongHeThong())));
        Theme.row(f, 2, "Tồn thực tế", spThucTe);
        Theme.row(f, 3, "Ghi chú", txtGhiChu);
        txtGhiChu.setText(ct.getGhiChu());
        add(f, BorderLayout.CENTER);
        add(Theme.footer(this::luu, this::dispose, "Lưu"), BorderLayout.SOUTH);
        pack(); setLocationRelativeTo(owner);
    }

    private void luu() {
        ct.setSoLuongThucTe((Integer) spThucTe.getValue());
        ct.setGhiChu(txtGhiChu.getText().trim());
        saved = true; dispose();
    }
    public boolean isSaved() { return saved; }
}
