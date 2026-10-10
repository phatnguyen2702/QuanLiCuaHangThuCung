package UI;

import dao.NhaCungCapDAO;
import entity.NhaCungCap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/** Form Them nha cung cap: ma, ten, so dien thoai, email, dia chi. */
public class NhaCungCapDialog extends JDialog {

    private final NhaCungCapDAO dao = new NhaCungCapDAO();

    private final JTextField txtMa = UIStyle.o(22);
    private final JTextField txtTen = UIStyle.o(22);
    private final JTextField txtSdt = UIStyle.o(22);
    private final JTextField txtEmail = UIStyle.o(22);
    private final JTextField txtDiaChi = UIStyle.o(22);

    private NhaCungCap ketQua = null;

    public NhaCungCapDialog(Window cha, String maMoi) {

        super(cha, "Thêm nhà cung cấp", ModalityType.APPLICATION_MODAL);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 25, 10, 25));

        UIStyle.hang(form, 0, "Mã NCC", txtMa, false);
        UIStyle.hang(form, 1, "Tên NCC", txtTen, false);
        UIStyle.hang(form, 2, "Số điện thoại", txtSdt, false);
        UIStyle.hang(form, 3, "Email", txtEmail, false);
        UIStyle.hang(form, 4, "Địa chỉ", txtDiaChi, false);

        txtMa.setText(maMoi);

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

    private String rongThanhNull(String s) {
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private void luu() {

        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        String sdt = txtSdt.getText().trim();
        String email = txtEmail.getText().trim();
        String diaChi = txtDiaChi.getText().trim();

        if (ma.isEmpty() || ten.isEmpty()) {
            UIStyle.canhBao(this, "Vui lòng nhập mã và tên nhà cung cấp.");
            return;
        }

        if (ma.length() > 10 || ten.length() > 50
                || sdt.length() > 15 || email.length() > 50
                || diaChi.length() > 50) {
            UIStyle.canhBao(this,
                    "Độ dài tối đa: mã 10, tên 50, SĐT 15, email 50, địa chỉ 50 ký tự.");
            return;
        }

        if (!sdt.isEmpty() && !sdt.matches("[0-9+ .-]+")) {
            UIStyle.canhBao(this, "Số điện thoại chỉ gồm chữ số.");
            return;
        }

        if (!email.isEmpty() && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            UIStyle.canhBao(this, "Email chưa đúng định dạng.");
            return;
        }

        NhaCungCap n = new NhaCungCap(ma, ten,
                rongThanhNull(sdt), rongThanhNull(email), rongThanhNull(diaChi));

        try {
            dao.them(n);
            ketQua = n;
            dispose();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    /** Nha cung cap vua them, hoac null neu bam Huy. */
    public NhaCungCap getKetQua() {
        return ketQua;
    }
}
