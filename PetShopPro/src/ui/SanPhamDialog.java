package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import data.DuLieu;
import model.*;

public class SanPhamDialog extends JDialog {
    private final JTextField txtMa = new JTextField(22), txtTen = new JTextField(22),
            txtGiaNhap = new JTextField(22), txtGiaBan = new JTextField(22);
    private final JComboBox<DanhMuc> cboDm = new JComboBox<>(DuLieu.dsDanhMuc.toArray(new DanhMuc[0]));
    private final JComboBox<NhaCungCap> cboNcc = new JComboBox<>(DuLieu.dsNhaCungCap.toArray(new NhaCungCap[0]));
    private final JSpinner spTon = new JSpinner(new SpinnerNumberModel(0, 0, 1000000, 1));
    private final SanPham sp;
    private boolean saved = false;

    public SanPhamDialog(Window owner, SanPham sp) {
        super(owner, sp == null ? "Thêm sản phẩm" : "Sửa sản phẩm", ModalityType.APPLICATION_MODAL);
        this.sp = sp;
        JPanel f = new JPanel(new GridBagLayout());
        f.setBorder(new EmptyBorder(12, 12, 4, 12));
        Theme.row(f, 0, "Mã sản phẩm", txtMa); Theme.row(f, 1, "Tên sản phẩm", txtTen);
        Theme.row(f, 2, "Giá nhập", txtGiaNhap); Theme.row(f, 3, "Giá bán", txtGiaBan);
        Theme.row(f, 4, "Danh mục", cboDm); Theme.row(f, 5, "Nhà cung cấp", cboNcc);
        Theme.row(f, 6, "Tồn kho", spTon);
        if (sp != null) {
            txtMa.setText(sp.getMaSanPham()); txtMa.setEnabled(false);
            txtTen.setText(sp.getTenSanPham());
            txtGiaNhap.setText(String.valueOf((long) sp.getGiaNhap()));
            txtGiaBan.setText(String.valueOf((long) sp.getGiaBan()));
            cboDm.setSelectedItem(sp.getMaDanhMuc()); cboNcc.setSelectedItem(sp.getMaNhaCungCap());
            spTon.setValue(sp.getSoLuongTon());
        } else {
            txtMa.setText(String.format("SP%05d", DuLieu.dsSanPham.size() + 1));
        }
        add(f, BorderLayout.CENTER);
        add(Theme.footer(this::luu, this::dispose, "Lưu"), BorderLayout.SOUTH);
        pack(); setLocationRelativeTo(owner);
    }

    private void luu() {
        String ma = txtMa.getText().trim(), ten = txtTen.getText().trim();
        if (ma.isEmpty() || ten.isEmpty()) { Theme.loi(this, "Vui lòng nhập mã và tên sản phẩm."); return; }
        if (cboDm.getSelectedItem() == null || cboNcc.getSelectedItem() == null) {
            Theme.loi(this, "Cần có ít nhất 1 danh mục và 1 nhà cung cấp."); return;
        }
        double gn, gb;
        try { gn = Double.parseDouble(txtGiaNhap.getText().trim()); gb = Double.parseDouble(txtGiaBan.getText().trim()); }
        catch (NumberFormatException e) { Theme.loi(this, "Giá nhập / giá bán phải là số."); return; }
        if (gn < 0 || gb < 0) { Theme.loi(this, "Giá không được âm."); return; }
        DanhMuc dm = (DanhMuc) cboDm.getSelectedItem();
        NhaCungCap ncc = (NhaCungCap) cboNcc.getSelectedItem();
        int ton = (Integer) spTon.getValue();
        if (sp == null) {
            if (DuLieu.timSanPham(ma) != null) { Theme.loi(this, "Mã sản phẩm đã tồn tại."); return; }
            DuLieu.dsSanPham.add(new SanPham(ma, ten, gn, gb, dm, ncc, ton));
        } else {
            sp.setTenSanPham(ten); sp.setGiaNhap(gn); sp.setGiaBan(gb);
            sp.setMaDanhMuc(dm); sp.setMaNhaCungCap(ncc); sp.setSoLuongTon(ton);
        }
        saved = true; dispose();
    }
    public boolean isSaved() { return saved; }
}
