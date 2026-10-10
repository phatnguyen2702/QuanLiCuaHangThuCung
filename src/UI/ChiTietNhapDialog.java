package UI;

import entity.ChiTietPhieuNhap;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/** Form "Them mat hang vao phieu nhap": san pham, so luong nhap, don gia nhap. */
public class ChiTietNhapDialog extends JDialog {

    private final JComboBox<SanPham> cboSanPham = new JComboBox<>();
    private final JSpinner spnSoLuong =
            new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
    private final JTextField txtDonGia = UIStyle.o(22);

    private ChiTietPhieuNhap ketQua = null;

    public ChiTietNhapDialog(Window cha, List<SanPham> dsSanPham) {

        super(cha, "Thêm mặt hàng vào phiếu nhập",
                ModalityType.APPLICATION_MODAL);

        for (SanPham sp : dsSanPham) {
            cboSanPham.addItem(sp);
        }
        cboSanPham.setFont(UIStyle.font(Font.PLAIN, 13));
        spnSoLuong.setFont(UIStyle.font(Font.PLAIN, 13));

        // Chon san pham -> tu dien gia nhap dang co trong bang SanPham
        cboSanPham.addActionListener(e -> {
            SanPham sp = (SanPham) cboSanPham.getSelectedItem();
            if (sp != null) {
                txtDonGia.setText(String.valueOf((long) sp.getGiaNhap()));
            }
        });

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 25, 10, 25));

        UIStyle.hang(form, 0, "Sản phẩm", cboSanPham, false);
        UIStyle.hang(form, 1, "Số lượng nhập", spnSoLuong, false);
        UIStyle.hang(form, 2, "Đơn giá nhập", txtDonGia, false);

        if (cboSanPham.getItemCount() > 0) {
            cboSanPham.setSelectedIndex(0);
            txtDonGia.setText(String.valueOf(
                    (long) dsSanPham.get(0).getGiaNhap()));
        }

        JButton btnHuy = UIStyle.nutPhu("Hủy");
        JButton btnThem = UIStyle.nutXanh("Thêm vào phiếu");
        btnHuy.addActionListener(e -> dispose());
        btnThem.addActionListener(e -> them());

        JPanel nut = UIStyle.hangNut(btnHuy, btnThem);
        nut.setBorder(new EmptyBorder(5, 25, 20, 25));

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(nut, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnThem);

        pack();
        setMinimumSize(new Dimension(420, getHeight()));
        setResizable(false);
        setLocationRelativeTo(cha);
    }

    private void them() {

        SanPham sp = (SanPham) cboSanPham.getSelectedItem();

        if (sp == null) {
            UIStyle.canhBao(this, "Vui lòng chọn sản phẩm.");
            return;
        }

        long donGia;

        try {
            donGia = UIStyle.docTien(txtDonGia.getText());
        } catch (NumberFormatException e) {
            UIStyle.canhBao(this, "Đơn giá nhập phải là số không âm.");
            return;
        }

        if (donGia <= 0) {
            UIStyle.canhBao(this, "Đơn giá nhập phải lớn hơn 0.");
            return;
        }

        ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
        ct.setMaSanPham(sp.getMaSanPham());
        ct.setTenSanPham(sp.getTenSanPham());
        ct.setSoLuongNhap((Integer) spnSoLuong.getValue());
        ct.setDonGiaNhap(donGia);

        ketQua = ct;
        dispose();
    }

    /** Dong chi tiet vua nhap, hoac null neu bam Huy. */
    public ChiTietPhieuNhap getKetQua() {
        return ketQua;
    }
}
