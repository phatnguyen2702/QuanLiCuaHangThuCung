package UI;

import dao.VoucherDAO;
import entity.Voucher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Locale;

public class QuanLyVoucher extends JPanel {

        private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        private static final String[] COLS = { "Ma", "Ten chuong trinh", "Dieu kien",
                        "Muc giam", "Da dung", "Hieu luc", "Trang thai" };

        private final VoucherDAO voucherDAO = new VoucherDAO();

        private final JTextField txtTim = new JTextField(28);

        private final JComboBox<String> cboTt = new JComboBox<>(
                        new String[] { "Tat ca", "Dang chay", "Sap chay", "Het han", "Het luot", "Da huy" });

        private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                        return false;
                }
        };

        private final JTable table = new JTable(model);
        private final JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));

        private final CardLayout cards = new CardLayout();
        private final JPanel danhSach = new JPanel(new BorderLayout());
        private JPanel formPanel = null;

        // dong i cua bang <-> dsHienThi.get(i)
        private ArrayList<Voucher> dsHienThi = new ArrayList<>();

        public QuanLyVoucher() {

                setLayout(cards);

                setBackground(UiKit.LIGHT_BLUE);

                danhSach.setBackground(UiKit.LIGHT_BLUE);

                JButton btnTao = UiKit.button("Tao voucher", UiKit.GREEN, Color.WHITE);

                btnTao.addActionListener(e -> moForm(null));

                danhSach.add(UiKit.titleBar("MA GIAM GIA VA VOUCHER", btnTao), BorderLayout.NORTH);

                stats.setOpaque(false);

                // ---------------- the tra cuu ----------------
                JPanel search = UiKit.card(new FlowLayout(FlowLayout.LEFT, 12, 0));

                UiKit.field(txtTim);

                cboTt.setFont(UiKit.F);

                JButton btnTra = UiKit.button("Tra cuu", UiKit.NAVY, Color.WHITE);
                JButton btnXoaLoc = UiKit.button("Xoa bo loc", Color.WHITE, UiKit.NAVY);

                btnTra.addActionListener(e -> taiLai());
                txtTim.addActionListener(e -> taiLai());

                btnXoaLoc.addActionListener(e -> {
                        txtTim.setText("");
                        cboTt.setSelectedIndex(0);
                        taiLai();
                });

                JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

                nut.setOpaque(false);
                nut.setBorder(new EmptyBorder(16, 0, 0, 0));
                nut.add(btnTra);
                nut.add(btnXoaLoc);

                search.add(UiKit.captioned("Tim ma voucher (ma hoac ten chuong trinh)", txtTim));
                search.add(UiKit.captioned("Trang thai", cboTt));
                search.add(nut);

                // ---------------- bang ----------------
                UiKit.styleTable(table);

                table.getColumnModel().getColumn(6).setCellRenderer(new UiKit.TrangThaiRenderer());

                int[] w = { 60, 200, 230, 80, 100, 190, 100 };

                for (int i = 0; i < w.length; i++) {
                        table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
                }

                JScrollPane sp = new JScrollPane(table);

                sp.setBorder(BorderFactory.createLineBorder(UiKit.BORDER));

                sp.getViewport().setBackground(Color.WHITE);

                // ---------------- thao tac ----------------
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

                actions.setOpaque(false);

                JButton btnSua = UiKit.button("Sua", UiKit.NAVY, Color.WHITE);
                JButton btnHuy = UiKit.button("Huy / Kich hoat", UiKit.GREEN, Color.WHITE);
                JButton btnXoa = UiKit.button("Xoa", Color.WHITE, UiKit.RED);

                btnSua.addActionListener(e -> sua());
                btnHuy.addActionListener(e -> huyKichHoat());
                btnXoa.addActionListener(e -> xoa());

                actions.add(btnSua);
                actions.add(btnHuy);
                actions.add(btnXoa);

                JPanel top = new JPanel();

                top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

                top.setOpaque(false);

                top.add(stats);
                top.add(Box.createVerticalStrut(8));
                top.add(search);

                JPanel center = new JPanel(new BorderLayout(0, 8));

                center.setOpaque(false);

                center.setBorder(new EmptyBorder(12, 25, 15, 25));

                center.add(top, BorderLayout.NORTH);
                center.add(sp, BorderLayout.CENTER);
                center.add(actions, BorderLayout.SOUTH);

                danhSach.add(center, BorderLayout.CENTER);

                add(danhSach, "DS");

                taiLai();
        }

        // Goi khi mo man hinh
        public void lamMoi() {

                cards.show(this, "DS");

                taiLai();
        }

        // =========================================================
        // DANH SACH
        // =========================================================

        private JPanel theThongKe(String title, String value) {

                JPanel p = UiKit.card(new GridLayout(2, 1));

                p.add(UiKit.label(title, UiKit.F, UiKit.MUTED));
                p.add(UiKit.label(value, UiKit.BIG, UiKit.NAVY));

                return p;
        }

        private static String hieuLuc(Voucher v) {

                LocalDate bd = v.getNgayBatDau();
                LocalDate kt = v.getNgayKetThuc();

                if (bd == null && kt == null) {
                        return "Khong gioi han";
                }

                if (bd == null) {
                        return "Den " + kt.format(D);
                }

                if (kt == null) {
                        return "Tu " + bd.format(D);
                }

                return bd.format(D) + " - " + kt.format(D);
        }

        private static String phanTram(double v) {

                return (v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.US, "%.2f", v)) + "%";
        }

        public void taiLai() {

                // ---- thong ke (tren toan bo voucher) ----
                int dangChay = 0;

                for (Voucher v : voucherDAO.getAll("")) {

                        if (v.getTrangThaiText().equals("Dang chay")) {
                                dangChay++;
                        }
                }

                double[] hd = voucherDAO.thongKeHoaDon();

                stats.removeAll();

                stats.add(theThongKe("Dang hoat dong", String.valueOf(dangChay)));
                stats.add(theThongKe("Luot su dung thang", UiKit.tien((long) hd[0])));
                stats.add(theThongKe("Tong gia tri uu dai", UiKit.tien((long) hd[1]) + " d"));
                stats.add(theThongKe("Hoa don dung voucher", String.format(Locale.US, "%.1f%%", hd[2])));

                stats.revalidate();
                stats.repaint();

                // ---- bang ----
                String loc = (String) cboTt.getSelectedItem();

                model.setRowCount(0);

                dsHienThi = new ArrayList<>();

                for (Voucher v : voucherDAO.getAll(txtTim.getText())) {

                        String tt = v.getTrangThaiText();

                        if (!loc.equals("Tat ca") && !loc.equals(tt)) {
                                continue;
                        }

                        dsHienThi.add(v);

                        String daDung = v.getSoLuongToiDa() == null
                                        ? String.valueOf(v.getDaDung())
                                        : v.getDaDung() + "/" + v.getSoLuongToiDa();

                        model.addRow(new Object[] {
                                        v.getMaVoucher(),
                                        v.getTenVoucher(),
                                        v.getDieuKien() == null ? "" : v.getDieuKien(),
                                        phanTram(v.getPhanTramGiam()),
                                        daDung,
                                        hieuLuc(v),
                                        tt });
                }
        }

        private Voucher dongDangChon() {

                int row = table.getSelectedRow();

                if (row < 0 || row >= dsHienThi.size()) {

                        JOptionPane.showMessageDialog(this, "Hay chon 1 voucher trong bang.");

                        return null;
                }

                return dsHienThi.get(row);
        }

        private void sua() {

                Voucher v = dongDangChon();

                if (v != null) {
                        moForm(v);
                }
        }

        private void huyKichHoat() {

                Voucher v = dongDangChon();

                if (v == null) {
                        return;
                }

                boolean hoatDong = v.isTrangThai();

                String hoi = hoatDong
                                ? "Huy voucher " + v.getMaVoucher() + "? (Van giu trong danh sach)"
                                : "Kich hoat lai voucher " + v.getMaVoucher() + "?";

                if (JOptionPane.showConfirmDialog(this, hoi, "Xac nhan",
                                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        return;
                }

                if (!voucherDAO.doiTrangThai(v.getMaVoucher(), !hoatDong)) {

                        JOptionPane.showMessageDialog(this, "Cap nhat that bai.", "Loi",
                                        JOptionPane.ERROR_MESSAGE);

                        return;
                }

                taiLai();
        }

        private void xoa() {

                Voucher v = dongDangChon();

                if (v == null) {
                        return;
                }

                if (JOptionPane.showConfirmDialog(this,
                                "Xoa han voucher " + v.getMaVoucher() + " - " + v.getTenVoucher() + "?",
                                "Xac nhan xoa", JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                        return;
                }

                String loi = voucherDAO.xoa(v.getMaVoucher());

                if (loi != null) {

                        JOptionPane.showMessageDialog(this, loi, "Khong xoa duoc",
                                        JOptionPane.WARNING_MESSAGE);

                        return;
                }

                taiLai();
        }

        // =========================================================
        // MO MAN HINH TAO / SUA
        // =========================================================

        private void moForm(Voucher goc) {

                if (formPanel != null) {
                        remove(formPanel);
                }

                formPanel = new VoucherForm(
                                goc,
                                () -> cards.show(this, "DS"),
                                () -> {
                                        taiLai();
                                        cards.show(this, "DS");
                                });

                add(formPanel, "FORM");

                cards.show(this, "FORM");
        }

        // =========================================================
        // MAN HINH TAO / SUA VOUCHER (class long)
        // goc = null -> tao moi; goc != null -> sua
        // =========================================================

        public static class VoucherForm extends JPanel {

                private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                private final VoucherDAO voucherDAO = new VoucherDAO();

                private final Voucher goc;
                private final Runnable quayLai;
                private final Runnable sauKhiLuu;

                private final JTextField txtTen = new JTextField();
                private final JTextField txtPt = new JTextField();
                private final JTextField txtBd = new JTextField();
                private final JTextField txtKt = new JTextField();
                private final JTextField txtSl = new JTextField();
                private final JTextArea txtDk = new JTextArea(3, 30);
                private final JCheckBox chkHoatDong = new JCheckBox("Dang kich hoat", true);

                private final JLabel sMa = UiKit.label("-", UiKit.FB, UiKit.NAVY);
                private final JLabel sTen = UiKit.label("-", UiKit.FB, UiKit.NAVY);
                private final JLabel sGiam = UiKit.label("-", UiKit.FB, UiKit.NAVY);
                private final JLabel sHieuLuc = UiKit.label("-", UiKit.FB, UiKit.NAVY);
                private final JLabel sLuot = UiKit.label("-", UiKit.FB, UiKit.NAVY);

                public VoucherForm(Voucher goc, Runnable quayLai, Runnable sauKhiLuu) {

                        this.goc = goc;
                        this.quayLai = quayLai;
                        this.sauKhiLuu = sauKhiLuu;

                        setLayout(new BorderLayout());

                        setBackground(UiKit.LIGHT_BLUE);

                        JButton btnVe = UiKit.button("< Danh sach", Color.WHITE, UiKit.NAVY);

                        btnVe.addActionListener(e -> quayLai.run());

                        add(UiKit.titleBar(goc == null ? "TAO VOUCHER" : "SUA VOUCHER", btnVe),
                                        BorderLayout.NORTH);

                        // ---------------- cot trai ----------------
                        JPanel left = new JPanel();

                        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

                        left.setOpaque(false);

                        left.add(taoTheThongTin());
                        left.add(Box.createVerticalStrut(10));
                        left.add(taoTheHieuLuc());

                        // ---------------- cot phai ----------------
                        JPanel right = new JPanel();

                        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

                        right.setOpaque(false);

                        right.setPreferredSize(new Dimension(280, 100));

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

                        add(sp, BorderLayout.CENTER);

                        // ---- do du lieu khi sua ----
                        if (goc != null) {

                                txtTen.setText(goc.getTenVoucher());

                                txtPt.setText(phanTram(goc.getPhanTramGiam()).replace("%", ""));

                                txtDk.setText(goc.getDieuKien() == null ? "" : goc.getDieuKien());

                                txtBd.setText(goc.getNgayBatDau() == null ? "" : goc.getNgayBatDau().format(D));

                                txtKt.setText(goc.getNgayKetThuc() == null ? "" : goc.getNgayKetThuc().format(D));

                                txtSl.setText(goc.getSoLuongToiDa() == null ? "" : String.valueOf(goc.getSoLuongToiDa()));

                                chkHoatDong.setSelected(goc.isTrangThai());
                        }

                        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
                                public void insertUpdate(javax.swing.event.DocumentEvent e) { capNhatTomTat(); }
                                public void removeUpdate(javax.swing.event.DocumentEvent e) { capNhatTomTat(); }
                                public void changedUpdate(javax.swing.event.DocumentEvent e) { capNhatTomTat(); }
                        };

                        txtTen.getDocument().addDocumentListener(dl);
                        txtPt.getDocument().addDocumentListener(dl);
                        txtBd.getDocument().addDocumentListener(dl);
                        txtKt.getDocument().addDocumentListener(dl);
                        txtSl.getDocument().addDocumentListener(dl);

                        capNhatTomTat();
                }

                // =========================================================
                // XAY DUNG GIAO DIEN
                // =========================================================

                private JPanel taoTheThongTin() {

                        JPanel card = UiKit.card(new BorderLayout(0, 8));

                        card.add(UiKit.label("Thong tin voucher", UiKit.FB, UiKit.NAVY), BorderLayout.NORTH);

                        UiKit.field(txtTen);
                        UiKit.field(txtPt);

                        txtDk.setFont(UiKit.F);
                        txtDk.setLineWrap(true);
                        txtDk.setWrapStyleWord(true);

                        JScrollPane dk = new JScrollPane(txtDk);

                        dk.setBorder(BorderFactory.createLineBorder(UiKit.BORDER));

                        JPanel top = new JPanel(new GridLayout(1, 2, 12, 0));

                        top.setOpaque(false);

                        top.add(UiKit.captioned("Ten chuong trinh *", txtTen));
                        top.add(UiKit.captioned("Muc giam (%) *", txtPt));

                        JPanel form = new JPanel(new BorderLayout(0, 10));

                        form.setOpaque(false);

                        form.add(top, BorderLayout.NORTH);
                        form.add(UiKit.captioned("Dieu kien ap dung", dk), BorderLayout.CENTER);

                        card.add(form, BorderLayout.CENTER);

                        return card;
                }

                private JPanel taoTheHieuLuc() {

                        JPanel card = UiKit.card(new BorderLayout(0, 8));

                        card.add(UiKit.label("Hieu luc va gioi han", UiKit.FB, UiKit.NAVY), BorderLayout.NORTH);

                        UiKit.field(txtBd);
                        UiKit.field(txtKt);
                        UiKit.field(txtSl);

                        JPanel grid = new JPanel(new GridLayout(1, 3, 12, 0));

                        grid.setOpaque(false);

                        grid.add(UiKit.captioned("Tu ngay (dd/MM/yyyy)", txtBd));
                        grid.add(UiKit.captioned("Den ngay (dd/MM/yyyy)", txtKt));
                        grid.add(UiKit.captioned("So luot toi da", txtSl));

                        JPanel form = new JPanel(new BorderLayout(0, 6));

                        form.setOpaque(false);

                        chkHoatDong.setOpaque(false);
                        chkHoatDong.setFont(UiKit.F);

                        form.add(grid, BorderLayout.NORTH);
                        form.add(UiKit.label("De trong = khong gioi han ngay / so luot.", UiKit.F, UiKit.MUTED),
                                        BorderLayout.CENTER);
                        form.add(chkHoatDong, BorderLayout.SOUTH);

                        card.add(form, BorderLayout.CENTER);

                        return card;
                }

                private JPanel taoTheTomTat() {

                        JPanel card = UiKit.card(new BorderLayout(0, 8));

                        card.add(UiKit.label("Tom tat", UiKit.FB, UiKit.NAVY), BorderLayout.NORTH);

                        JPanel g = new JPanel(new GridLayout(5, 2, 8, 8));

                        g.setOpaque(false);

                        g.add(UiKit.label("Ma", UiKit.F, UiKit.MUTED));
                        g.add(sMa);
                        g.add(UiKit.label("Chuong trinh", UiKit.F, UiKit.MUTED));
                        g.add(sTen);
                        g.add(UiKit.label("Muc giam", UiKit.F, UiKit.MUTED));
                        g.add(sGiam);
                        g.add(UiKit.label("Hieu luc", UiKit.F, UiKit.MUTED));
                        g.add(sHieuLuc);
                        g.add(UiKit.label("So luot", UiKit.F, UiKit.MUTED));
                        g.add(sLuot);

                        card.add(g, BorderLayout.CENTER);

                        return card;
                }

                private JPanel taoNut() {

                        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

                        p.setOpaque(false);

                        JButton btnHuy = UiKit.button("Huy", Color.WHITE, UiKit.NAVY);
                        JButton btnLuu = UiKit.button("Luu voucher", UiKit.GREEN, Color.WHITE);

                        btnHuy.addActionListener(e -> quayLai.run());
                        btnLuu.addActionListener(e -> luu());

                        p.add(btnHuy);
                        p.add(btnLuu);

                        return p;
                }

                private void capNhatTomTat() {

                        sMa.setText(goc == null ? "Tu dong" : goc.getMaVoucher());

                        sTen.setText(txtTen.getText().trim().isEmpty() ? "-" : txtTen.getText().trim());

                        sGiam.setText(txtPt.getText().trim().isEmpty() ? "-" : txtPt.getText().trim() + "%");

                        String bd = txtBd.getText().trim();
                        String kt = txtKt.getText().trim();

                        sHieuLuc.setText(bd.isEmpty() && kt.isEmpty() ? "Khong gioi han"
                                        : (bd.isEmpty() ? "..." : bd) + " - " + (kt.isEmpty() ? "..." : kt));

                        sLuot.setText(txtSl.getText().trim().isEmpty() ? "Khong gioi han" : txtSl.getText().trim());
                }

                // =========================================================
                // LUU
                // =========================================================

                private void canhBao(String msg) {

                        JOptionPane.showMessageDialog(this, msg, "Du lieu chua hop le",
                                        JOptionPane.WARNING_MESSAGE);
                }

                private void luu() {

                        String ten = txtTen.getText().trim();

                        if (ten.isEmpty() || ten.length() > 50) {
                                canhBao("Ten chuong trinh bat buoc nhap (toi da 50 ky tu).");
                                return;
                        }

                        double pt;

                        try {
                                pt = Double.parseDouble(txtPt.getText().trim().replace(',', '.'));
                        } catch (NumberFormatException ex) {
                                canhBao("Muc giam phai la so, vi du 10 hoac 12.5");
                                return;
                        }

                        if (pt <= 0 || pt > 100) {
                                canhBao("Muc giam phai lon hon 0 va khong qua 100 (%).");
                                return;
                        }

                        pt = Math.round(pt * 100) / 100.0;

                        String dk = txtDk.getText().trim();

                        if (dk.length() > 255) {
                                canhBao("Dieu kien toi da 255 ky tu.");
                                return;
                        }

                        LocalDate bd;
                        LocalDate kt;

                        try {

                                bd = txtBd.getText().trim().isEmpty() ? null
                                                : LocalDate.parse(txtBd.getText().trim(), D);

                                kt = txtKt.getText().trim().isEmpty() ? null
                                                : LocalDate.parse(txtKt.getText().trim(), D);

                        } catch (DateTimeParseException ex) {

                                canhBao("Ngay phai dang dd/MM/yyyy, vi du 01/11/2026.");

                                return;
                        }

                        if (bd != null && kt != null && kt.isBefore(bd)) {
                                canhBao("Ngay ket thuc phai sau ngay bat dau.");
                                return;
                        }

                        Integer sl = null;

                        String s = txtSl.getText().trim();

                        if (!s.isEmpty()) {

                                try {
                                        sl = Integer.parseInt(s);
                                } catch (NumberFormatException ex) {
                                        canhBao("So luot toi da phai la so nguyen.");
                                        return;
                                }

                                if (sl <= 0) {
                                        canhBao("So luot toi da phai lon hon 0.");
                                        return;
                                }

                                if (goc != null && sl < goc.getDaDung()) {
                                        canhBao("Voucher da dung " + goc.getDaDung()
                                                        + " luot, so luot toi da khong the nho hon.");
                                        return;
                                }
                        }

                        Voucher v = new Voucher(
                                        goc == null ? null : goc.getMaVoucher(),
                                        ten, pt, dk.isEmpty() ? null : dk,
                                        chkHoatDong.isSelected(), bd, kt, sl);

                        if (goc == null) {

                                String ma = voucherDAO.them(v);

                                if (ma == null) {

                                        JOptionPane.showMessageDialog(this,
                                                        "Luu that bai. Xem loi chi tiet trong Console.",
                                                        "Loi", JOptionPane.ERROR_MESSAGE);

                                        return;
                                }

                                JOptionPane.showMessageDialog(this, "Da tao voucher " + ma + ".");

                        } else {

                                if (!voucherDAO.sua(v)) {

                                        JOptionPane.showMessageDialog(this,
                                                        "Luu that bai. Xem loi chi tiet trong Console.",
                                                        "Loi", JOptionPane.ERROR_MESSAGE);

                                        return;
                                }

                                JOptionPane.showMessageDialog(this, "Da cap nhat voucher " + goc.getMaVoucher() + ".");
                        }

                        sauKhiLuu.run();
                }
        }
}
