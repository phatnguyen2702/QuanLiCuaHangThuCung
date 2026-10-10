package UI;

import dao.SanPhamDAO;
import entity.DanhMuc;
import entity.NhaCungCap;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Form Them / Sua san pham - dung cac cot cua bang SanPham:
 * ma, ten, giaNhap, giaBan, maDanhMuc, maNhaCungCap.
 * (Ton kho khong phai cot cua SanPham nen khong nhap o day.)
 */
public class SanPhamDialog extends JDialog {

    private final SanPhamDAO dao = new SanPhamDAO();
    private final boolean dangSua;

    private final JTextField txtMa = UIStyle.o(22);
    private final JTextField txtTen = UIStyle.o(22);
    private final JTextField txtGiaNhap = UIStyle.o(22);
    private final JTextField txtGiaBan = UIStyle.o(22);
    private final JComboBox<DanhMuc> cboDanhMuc = new JComboBox<>();
    private final JComboBox<NhaCungCap> cboNhaCungCap = new JComboBox<>();

    private boolean daLuu = false;

    /** sp = null: them moi. maMoi: ma goi y khi them. */
    public SanPhamDialog(Window cha, SanPham sp, String maMoi,
                         List<DanhMuc> dsDanhMuc,
                         List<NhaCungCap> dsNhaCungCap) {

        super(cha, sp == null ? "Thêm sản phẩm" : "Sửa sản phẩm",
                ModalityType.APPLICATION_MODAL);

        dangSua = sp != null;

        for (DanhMuc dm : dsDanhMuc) {
            cboDanhMuc.addItem(dm);
        }
        for (NhaCungCap n : dsNhaCungCap) {
            cboNhaCungCap.addItem(n);
        }

        cboDanhMuc.setFont(UIStyle.font(Font.PLAIN, 13));
        cboNhaCungCap.setFont(UIStyle.font(Font.PLAIN, 13));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 25, 10, 25));

        UIStyle.hang(form, 0, "Mã sản phẩm", txtMa, false);
        UIStyle.hang(form, 1, "Tên sản phẩm", txtTen, false);
        UIStyle.hang(form, 2, "Giá nhập", txtGiaNhap, false);
        UIStyle.hang(form, 3, "Giá bán", txtGiaBan, false);
        UIStyle.hang(form, 4, "Danh mục", cboDanhMuc, false);
        UIStyle.hang(form, 5, "Nhà cung cấp", cboNhaCungCap, false);

        if (dangSua) {
            txtMa.setText(sp.getMaSanPham());
            txtMa.setEditable(false);
            txtMa.setBackground(UIStyle.READONLY);
            txtTen.setText(sp.getTenSanPham());
            txtGiaNhap.setText(String.valueOf((long) sp.getGiaNhap()));
            txtGiaBan.setText(String.valueOf((long) sp.getGiaBan()));
            chonDanhMuc(sp.getMaDanhMuc());
            chonNhaCungCap(sp.getMaNhaCungCap());
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
        add(form, BorderLayout.CENTER);
        add(nut, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnLuu);

        pack();
        setResizable(false);
        setLocationRelativeTo(cha);
    }

    private void chonDanhMuc(String ma) {
        for (int i = 0; i < cboDanhMuc.getItemCount(); i++) {
            if (cboDanhMuc.getItemAt(i).getMaDanhMuc().equals(ma)) {
                cboDanhMuc.setSelectedIndex(i);
                return;
            }
        }
    }

    private void chonNhaCungCap(String ma) {
        for (int i = 0; i < cboNhaCungCap.getItemCount(); i++) {
            if (cboNhaCungCap.getItemAt(i).getMaNhaCungCap().equals(ma)) {
                cboNhaCungCap.setSelectedIndex(i);
                return;
            }
        }
    }

    private void luu() {

        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();

        if (ma.isEmpty() || ten.isEmpty()) {
            UIStyle.canhBao(this, "Vui lòng nhập mã và tên sản phẩm.");
            return;
        }

        if (ma.length() > 10) {
            UIStyle.canhBao(this, "Mã sản phẩm tối đa 10 ký tự.");
            return;
        }

        if (ten.length() > 50) {
            UIStyle.canhBao(this, "Tên sản phẩm tối đa 50 ký tự.");
            return;
        }

        long giaNhap;
        long giaBan;

        try {
            giaNhap = UIStyle.docTien(txtGiaNhap.getText());
        } catch (NumberFormatException e) {
            UIStyle.canhBao(this, "Giá nhập phải là số không âm (ví dụ 180000).");
            return;
        }

        try {
            giaBan = UIStyle.docTien(txtGiaBan.getText());
        } catch (NumberFormatException e) {
            UIStyle.canhBao(this, "Giá bán phải là số không âm (ví dụ 250000).");
            return;
        }

        DanhMuc dm = (DanhMuc) cboDanhMuc.getSelectedItem();
        NhaCungCap ncc = (NhaCungCap) cboNhaCungCap.getSelectedItem();

        if (dm == null || ncc == null) {
            UIStyle.canhBao(this,
                    "Cần có ít nhất 1 danh mục và 1 nhà cung cấp.");
            return;
        }

        if (giaBan < giaNhap && !UIStyle.xacNhan(this,
                "Giá bán đang thấp hơn giá nhập. Vẫn lưu?")) {
            return;
        }

        SanPham sp = new SanPham(ma, ten, giaNhap, giaBan,
                dm.getMaDanhMuc(), ncc.getMaNhaCungCap());

        try {
            if (dangSua) {
                dao.sua(sp);
            } else {
                dao.them(sp);
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
