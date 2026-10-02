package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import data.DuLieu;
import model.*;

public class ChiTietNhapDialog extends JDialog {
    private final JComboBox<SanPham> cboSp = new JComboBox<>(DuLieu.dsSanPham.toArray(new SanPham[0]));
    private final JSpinner spSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
    private final JTextField txtDonGia = new JTextField(20);
    private final PhieuNhapKho phieu;
    private ChiTietPhieuNhap ketQua;

    public ChiTietNhapDialog(Window owner, PhieuNhapKho phieu) {
        super(owner, "Thêm mặt hàng vào phiếu nhập", ModalityType.APPLICATION_MODAL);
        this.phieu = phieu;
        JPanel f = new JPanel(new GridBagLayout());
        f.setBorder(new EmptyBorder(12, 12, 4, 12));
        Theme.row(f, 0, "Sản phẩm", cboSp); Theme.row(f, 1, "Số lượng nhập", spSoLuong);
        Theme.row(f, 2, "Đơn giá nhập", txtDonGia);
        cboSp.addActionListener(e -> capNhatGia());
        capNhatGia();
        add(f, BorderLayout.CENTER);
        add(Theme.footer(this::luu, this::dispose, "Thêm vào phiếu"), BorderLayout.SOUTH);
        pack(); setLocationRelativeTo(owner);
    }

    private void capNhatGia() {
        SanPham s = (SanPham) cboSp.getSelectedItem();
        if (s != null) txtDonGia.setText(String.valueOf((long) s.getGiaNhap()));
    }

    private void luu() {
        SanPham s = (SanPham) cboSp.getSelectedItem();
        if (s == null) { Theme.loi(this, "Chưa có sản phẩm để chọn."); return; }
        double gia;
        try { gia = Double.parseDouble(txtDonGia.getText().trim()); }
        catch (NumberFormatException e) { Theme.loi(this, "Đơn giá phải là số."); return; }
        if (gia <= 0) { Theme.loi(this, "Đơn giá phải lớn hơn 0."); return; }
        ketQua = new ChiTietPhieuNhap(phieu, s, (Integer) spSoLuong.getValue(), gia);
        dispose();
    }
    /** null nếu người dùng bấm Hủy. */
    public ChiTietPhieuNhap getKetQua() { return ketQua; }
}
