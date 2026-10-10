package UI;

import dao.KhachHangDAO;
import entity.KhachHang;
import entity.ThuCung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

public class ThemKhachHang extends JPanel {

        // Gia tri luu vao CSDL (dung ky tu Unicode escape de khong phu thuoc ma hoa file)
        private static final String[] LOAI = { "Ch\u00f3", "M\u00e8o", "Kh\u00e1c" };
        private static final String[] GIOI_TINH = { "\u0110\u1ef1c", "C\u00e1i" };
        private static final String[] HANG = { "Khong co the", "Silver", "Gold", "Platinum" };

        private final KhachHangDAO khachHangDAO = new KhachHangDAO();

        private final String maNhanVien;
        private final Runnable sauKhiLuu;

        private final JTextField txtTen = new JTextField();
        private final JTextField txtSdt = new JTextField();
        private final JTextField txtEmail = new JTextField();
        private final JTextField txtDiaChi = new JTextField();
        private final JComboBox<String> cboHang = new JComboBox<>(HANG);
        private final JSpinner spnDiem = new JSpinner(new SpinnerNumberModel(0, 0, 1000000, 10));

        private final JPanel petsBox = new JPanel();
        private final ArrayList<PetRow> rows = new ArrayList<>();

        private final JLabel sTen = UiKit.label("-", UiKit.FB, UiKit.NAVY);
        private final JLabel sSdt = UiKit.label("-", UiKit.FB, UiKit.NAVY);
        private final JLabel sPet = UiKit.label("0", UiKit.FB, UiKit.NAVY);
        private final JLabel sHang = UiKit.label("Khong co the", UiKit.FB, UiKit.NAVY);

        // maNhanVien: nhan vien dang dang nhap (luu vao the thanh vien)
        // quayLai: chay khi bam Huy / sau khi luu thanh cong (TrangChu quay ve danh sach)
        public ThemKhachHang(String maNhanVien, Runnable quayLai) {

                this.maNhanVien = maNhanVien;
                this.sauKhiLuu = quayLai;

                setLayout(new BorderLayout());

                setBackground(UiKit.LIGHT_BLUE);

                JButton btnVe = UiKit.button("< Danh sach", Color.WHITE, UiKit.NAVY);

                btnVe.addActionListener(e -> quayLai.run());

                add(UiKit.titleBar("THEM KHACH HANG", btnVe), BorderLayout.NORTH);

                // ---------------- cot trai ----------------
                JPanel left = new JPanel();

                left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

                left.setOpaque(false);

                left.add(taoTheThongTin());
                left.add(Box.createVerticalStrut(10));
                left.add(taoTheThuCung());

                // ---------------- cot phai ----------------
                JPanel right = new JPanel();

                right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

                right.setOpaque(false);

                right.setPreferredSize(new Dimension(280, 100));

                right.add(taoTheHang());
                right.add(Box.createVerticalStrut(10));
                right.add(taoTheTomTat());
                right.add(Box.createVerticalStrut(10));
                right.add(taoNut());

                JPanel body = new JPanel(new BorderLayout(14, 0));

                body.setOpaque(false);

                body.setBorder(new EmptyBorder(12, 25, 15, 25));

                body.add(left, BorderLayout.CENTER);
                body.add(right, BorderLayout.EAST);

                JScrollPane sp = new JScrollPane(body);

                sp.setBorder(null);

                sp.getViewport().setBackground(UiKit.LIGHT_BLUE);

                sp.getVerticalScrollBar().setUnitIncrement(16);

                add(sp, BorderLayout.CENTER);

                // cap nhat tom tat khi go chu
                javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
                        public void insertUpdate(javax.swing.event.DocumentEvent e) { capNhatTomTat(); }
                        public void removeUpdate(javax.swing.event.DocumentEvent e) { capNhatTomTat(); }
                        public void changedUpdate(javax.swing.event.DocumentEvent e) { capNhatTomTat(); }
                };

                txtTen.getDocument().addDocumentListener(dl);
                txtSdt.getDocument().addDocumentListener(dl);

                cboHang.addActionListener(e -> capNhatTomTat());
        }

        // =========================================================
        // XAY DUNG GIAO DIEN
        // =========================================================

        private JPanel taoTheThongTin() {

                JPanel card = UiKit.card(new BorderLayout(0, 8));

                card.add(UiKit.label("Thong tin khach hang", UiKit.FB, UiKit.NAVY), BorderLayout.NORTH);

                UiKit.field(txtTen);
                UiKit.field(txtSdt);
                UiKit.field(txtEmail);
                UiKit.field(txtDiaChi);

                JPanel grid = new JPanel(new GridLayout(2, 2, 12, 10));

                grid.setOpaque(false);

                grid.add(UiKit.captioned("Ho va ten *", txtTen));
                grid.add(UiKit.captioned("So dien thoai *", txtSdt));
                grid.add(UiKit.captioned("Email", txtEmail));
                grid.add(UiKit.captioned("Dia chi", txtDiaChi));

                card.add(grid, BorderLayout.CENTER);

                return card;
        }

        private JPanel taoTheThuCung() {

                JPanel card = UiKit.card(new BorderLayout(0, 8));

                card.add(UiKit.label("Thu cung", UiKit.FB, UiKit.NAVY), BorderLayout.NORTH);

                petsBox.setLayout(new BoxLayout(petsBox, BoxLayout.Y_AXIS));

                petsBox.setOpaque(false);

                JButton btnThem = UiKit.button("+ Them thu cung", Color.WHITE, UiKit.NAVY);

                btnThem.addActionListener(e -> themDongThuCung());

                JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

                south.setOpaque(false);

                south.add(btnThem);

                card.add(petsBox, BorderLayout.CENTER);
                card.add(south, BorderLayout.SOUTH);

                return card;
        }

        private JPanel taoTheHang() {

                JPanel card = UiKit.card(new BorderLayout(0, 8));

                card.add(UiKit.label("Hang thanh vien", UiKit.FB, UiKit.NAVY), BorderLayout.NORTH);

                cboHang.setFont(UiKit.F);

                ((JSpinner.DefaultEditor) spnDiem.getEditor()).getTextField().setFont(UiKit.F);

                JPanel form = new JPanel(new GridLayout(2, 1, 0, 10));

                form.setOpaque(false);

                form.add(UiKit.captioned("Cap the", cboHang));
                form.add(UiKit.captioned("Diem khoi tao", spnDiem));

                card.add(form, BorderLayout.CENTER);

                return card;
        }

        private JPanel taoTheTomTat() {

                JPanel card = UiKit.card(new BorderLayout(0, 8));

                card.add(UiKit.label("Tom tat", UiKit.FB, UiKit.NAVY), BorderLayout.NORTH);

                JPanel g = new JPanel(new GridLayout(4, 2, 8, 6));

                g.setOpaque(false);

                g.add(UiKit.label("Khach hang", UiKit.F, UiKit.MUTED));
                g.add(sTen);
                g.add(UiKit.label("SDT", UiKit.F, UiKit.MUTED));
                g.add(sSdt);
                g.add(UiKit.label("Thu cung", UiKit.F, UiKit.MUTED));
                g.add(sPet);
                g.add(UiKit.label("Hang", UiKit.F, UiKit.MUTED));
                g.add(sHang);

                card.add(g, BorderLayout.CENTER);

                return card;
        }

        private JPanel taoNut() {

                JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

                p.setOpaque(false);

                JButton btnHuy = UiKit.button("Huy", Color.WHITE, UiKit.NAVY);
                JButton btnLuu = UiKit.button("Luu khach hang", UiKit.GREEN, Color.WHITE);

                btnHuy.addActionListener(e -> sauKhiLuu.run());
                btnLuu.addActionListener(e -> luu());

                p.add(btnHuy);
                p.add(btnLuu);

                return p;
        }

        private void capNhatTomTat() {

                sTen.setText(txtTen.getText().trim().isEmpty() ? "-" : txtTen.getText().trim());
                sSdt.setText(txtSdt.getText().trim().isEmpty() ? "-" : txtSdt.getText().trim());
                sPet.setText(String.valueOf(rows.size()));
                sHang.setText((String) cboHang.getSelectedItem());
        }

        // =========================================================
        // DONG THU CUNG
        // =========================================================

        private class PetRow extends JPanel {

                final JTextField ten = new JTextField(9);
                final JTextField giong = new JTextField(9);
                final JTextField canNang = new JTextField(5);
                final JComboBox<String> loai = new JComboBox<>(LOAI);
                final JComboBox<String> gioiTinh = new JComboBox<>(GIOI_TINH);

                PetRow() {

                        super(new FlowLayout(FlowLayout.LEFT, 8, 4));

                        setOpaque(false);

                        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiKit.BORDER));

                        UiKit.field(ten);
                        UiKit.field(giong);
                        UiKit.field(canNang);

                        loai.setFont(UiKit.F);
                        gioiTinh.setFont(UiKit.F);

                        JButton xoa = UiKit.button("X", Color.WHITE, UiKit.RED);

                        xoa.addActionListener(e -> {
                                petsBox.remove(this);
                                rows.remove(this);
                                petsBox.revalidate();
                                petsBox.repaint();
                                capNhatTomTat();
                        });

                        JPanel wrap = new JPanel(new BorderLayout());

                        wrap.setOpaque(false);

                        wrap.setBorder(new EmptyBorder(16, 0, 0, 0));

                        wrap.add(xoa, BorderLayout.CENTER);

                        add(UiKit.captioned("Ten *", ten));
                        add(UiKit.captioned("Loai", loai));
                        add(UiKit.captioned("Giong", giong));
                        add(UiKit.captioned("Gioi tinh", gioiTinh));
                        add(UiKit.captioned("Can nang (kg)", canNang));
                        add(wrap);
                }
        }

        private void themDongThuCung() {

                PetRow r = new PetRow();

                rows.add(r);

                petsBox.add(r);

                petsBox.revalidate();
                petsBox.repaint();

                capNhatTomTat();
        }

        // =========================================================
        // LUU
        // =========================================================

        private void luu() {

                String ten = txtTen.getText().trim();
                String sdt = txtSdt.getText().replaceAll("\\s", "");
                String email = txtEmail.getText().trim();
                String diaChi = txtDiaChi.getText().trim();

                // ---- kiem tra khach hang (do dai theo cot trong CSDL) ----
                if (ten.isEmpty() || ten.length() > 50) {
                        loi("Ho ten bat buoc nhap (toi da 50 ky tu).");
                        return;
                }

                if (!sdt.matches("\\d{10,11}")) {
                        loi("So dien thoai phai gom 10 hoac 11 chu so.");
                        return;
                }

                if (!email.isEmpty() && (!email.contains("@") || email.length() > 50)) {
                        loi("Email khong hop le (toi da 50 ky tu).");
                        return;
                }

                if (diaChi.length() > 50) {
                        loi("Dia chi toi da 50 ky tu.");
                        return;
                }

                if (khachHangDAO.tonTaiSdt(sdt)) {
                        loi("So dien thoai " + sdt + " da ton tai.");
                        return;
                }

                // ---- kiem tra thu cung ----
                ArrayList<ThuCung> pets = new ArrayList<>();

                for (PetRow r : rows) {

                        String tenTC = r.ten.getText().trim();
                        String giong = r.giong.getText().trim();

                        if (tenTC.isEmpty() || tenTC.length() > 50) {
                                loi("Moi thu cung can co ten (xoa dong neu khong dung).");
                                return;
                        }

                        if (giong.length() > 25) {
                                loi("Giong thu cung toi da 25 ky tu.");
                                return;
                        }

                        Double kg = null;

                        String s = r.canNang.getText().trim().replace(',', '.');

                        if (!s.isEmpty()) {

                                try {
                                        kg = Double.parseDouble(s);
                                } catch (NumberFormatException ex) {
                                        loi("Can nang phai la so, vi du 4.5");
                                        return;
                                }

                                if (kg < 0 || kg >= 1000) {
                                        loi("Can nang phai nho hon 1000 kg.");
                                        return;
                                }
                        }

                        pets.add(new ThuCung(null, tenTC, (String) r.loai.getSelectedItem(),
                                        giong, (String) r.gioiTinh.getSelectedItem(), kg, null));
                }

                KhachHang kh = new KhachHang(null, ten, sdt,
                                email.isEmpty() ? null : email,
                                diaChi.isEmpty() ? null : diaChi,
                                (Integer) spnDiem.getValue());

                String hang = (String) cboHang.getSelectedItem();

                String ma = khachHangDAO.them(kh, pets,
                                hang.equals("Khong co the") ? null : hang, maNhanVien);

                if (ma == null) {
                        loi("Luu that bai. Xem loi chi tiet trong Console.");
                        return;
                }

                JOptionPane.showMessageDialog(this, "Da them khach hang " + ma + ".");

                lamMoi();

                sauKhiLuu.run();
        }

        private void loi(String msg) {

                JOptionPane.showMessageDialog(this, msg, "Du lieu chua hop le",
                                JOptionPane.WARNING_MESSAGE);
        }

        // Xoa trang form (goi khi mo man hinh)
        public void lamMoi() {

                txtTen.setText("");
                txtSdt.setText("");
                txtEmail.setText("");
                txtDiaChi.setText("");

                cboHang.setSelectedIndex(0);

                spnDiem.setValue(0);

                rows.clear();

                petsBox.removeAll();

                petsBox.revalidate();
                petsBox.repaint();

                capNhatTomTat();
        }
}
