package UI;

import dao.DanhMucDAO;
import entity.DanhMuc;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/** Form Them / Sua danh muc: ma, ten, mo ta (dung 3 cot cua bang DanhMuc). */
public class DanhMucDialog extends JDialog {

    private final DanhMucDAO dao = new DanhMucDAO();
    private final boolean dangSua;

    private final JTextField txtMa = UIStyle.o(22);
    private final JTextField txtTen = UIStyle.o(22);
    private final JTextArea txtMoTa = new JTextArea(4, 22);

    private boolean daLuu = false;

    /** dm = null: them moi. maMoi: ma goi y khi them. */
    public DanhMucDialog(Window cha, DanhMuc dm, String maMoi) {

        super(cha, dm == null ? "Thêm danh mục" : "Sửa danh mục",
                ModalityType.APPLICATION_MODAL);

        dangSua = dm != null;

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 25, 10, 25));

        txtMoTa.setFont(UIStyle.font(Font.PLAIN, 13));
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        JScrollPane cuonMoTa = new JScrollPane(txtMoTa);

        UIStyle.hang(form, 0, "Mã danh mục", txtMa, false);
        UIStyle.hang(form, 1, "Tên danh mục", txtTen, false);
        UIStyle.hang(form, 2, "Mô tả", cuonMoTa, true);

        if (dangSua) {
            txtMa.setText(dm.getMaDanhMuc());
            txtMa.setEditable(false);
            txtMa.setBackground(UIStyle.READONLY);
            txtTen.setText(dm.getTenDanhMuc());
            txtMoTa.setText(dm.getMoTa());
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

    private void luu() {

        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        String moTa = txtMoTa.getText().trim();

        if (ma.isEmpty() || ten.isEmpty()) {
            UIStyle.canhBao(this, "Vui lòng nhập mã và tên danh mục.");
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
                dao.sua(dm);
            } else {
                dao.them(dm);
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
