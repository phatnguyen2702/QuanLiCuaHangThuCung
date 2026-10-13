package UI;

import dao.VoucherDAO;
import entity.Voucher;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.time.LocalDateTime;
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

        private final JPanel danhSach = new JPanel(new BorderLayout());

        // form tao / sua voucher hien thanh cua so popup (JDialog) giong "Dat lich moi"
        private VoucherForm form;
        private JDialog dlgForm;

        // dong i cua bang <-> dsHienThi.get(i)
        private ArrayList<Voucher> dsHienThi = new ArrayList<>();

        public QuanLyVoucher() {

                setLayout(new BorderLayout());

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

                form = new VoucherForm(
                                () -> {
                                        dongPopup();
                                        taiLai();
                                });

                danhSach.add(center, BorderLayout.CENTER);

                add(danhSach, BorderLayout.CENTER);

                taiLai();
        }

        // Goi khi mo man hinh
        public void lamMoi() {

                dongPopup();

                taiLai();
        }

        private void dongPopup() {

                if (dlgForm != null) {

                        dlgForm.dispose();

                        dlgForm = null;
                }
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

                form.mo(goc);

                Window owner = SwingUtilities.getWindowAncestor(this);

                String tieuDe = goc == null ? "T\u1ea1o Voucher" : "S\u1eeda Voucher";

                dlgForm = new JDialog(owner, tieuDe, Dialog.ModalityType.APPLICATION_MODAL);

                dlgForm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

                JPanel content = new JPanel(new BorderLayout());

                content.setBackground(UiKit.LIGHT_BLUE);

                content.setBorder(new EmptyBorder(14, 14, 14, 14));

                form.setPreferredSize(new Dimension(540, 560));

                content.add(form, BorderLayout.CENTER);

                dlgForm.setContentPane(content);

                // nhan ESC de dong popup
                dlgForm.getRootPane().registerKeyboardAction(
                                e -> dongPopup(),
                                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                                JComponent.WHEN_IN_FOCUSED_WINDOW);

                dlgForm.pack();

                dlgForm.setLocationRelativeTo(owner);

                dlgForm.setVisible(true);
        }

        // =========================================================
        // FORM TAO / SUA VOUCHER (class long, hien trong popup)
        // =========================================================

        // Form tao / sua voucher: hien trong cua so popup tren man Quan ly voucher
        public static class VoucherForm extends JPanel {

                private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                private final VoucherDAO voucherDAO = new VoucherDAO();

                private final Runnable dong;

                // null = dang tao moi, khac null = dang sua voucher nay
                private Voucher goc = null;

                private final JLabel lblTieuDe = UiKit.label("Tao voucher", new Font("Segoe UI", Font.BOLD, 16),
                                UiKit.NAVY);
                private final JLabel lblMa = UiKit.label(" ", UiKit.F, UiKit.MUTED);

                private final JTextField txtTen = new JTextField();
                private final JTextField txtPt = new JTextField();
                private final JTextField txtBd = new JTextField();
                private final JTextField txtKt = new JTextField();
                private final JTextField txtSl = new JTextField();
                private final JTextArea txtDk = new JTextArea(3, 20);
                private final JCheckBox chkHoatDong = new JCheckBox("Dang kich hoat", true);

                // dong: chay khi bam Dong / Huy / sau khi luu thanh cong (dong popup, nap lai
                // bang)
                public VoucherForm(Runnable dong) {

                        this.dong = dong;

                        setLayout(new BorderLayout(0, 8));

                        setBackground(Color.WHITE);

                        setBorder(new javax.swing.border.CompoundBorder(
                                        new javax.swing.border.LineBorder(UiKit.BORDER, 1, true),
                                        new EmptyBorder(12, 14, 12, 14)));

                        setPreferredSize(new Dimension(380, 100));

                        // ---------------- tieu de ----------------
                        JPanel tieuDe = new JPanel(new GridLayout(2, 1));

                        tieuDe.setOpaque(false);

                        tieuDe.add(lblTieuDe);
                        tieuDe.add(lblMa);

                        JButton btnDong = UiKit.button("Dong", Color.WHITE, UiKit.NAVY);

                        btnDong.addActionListener(e -> dong.run());

                        JPanel head = new JPanel(new BorderLayout());

                        head.setOpaque(false);

                        head.add(tieuDe, BorderLayout.WEST);
                        head.add(btnDong, BorderLayout.EAST);

                        // ---------------- noi dung ----------------
                        UiKit.field(txtTen);
                        UiKit.field(txtPt);
                        UiKit.field(txtBd);
                        UiKit.field(txtKt);
                        UiKit.field(txtSl);

                        txtDk.setFont(UiKit.F);
                        txtDk.setLineWrap(true);
                        txtDk.setWrapStyleWord(true);

                        JScrollPane dk = new JScrollPane(txtDk);

                        dk.setBorder(BorderFactory.createLineBorder(UiKit.BORDER));

                        chkHoatDong.setOpaque(false);
                        chkHoatDong.setFont(UiKit.F);

                        JPanel ngay = new JPanel(new GridLayout(1, 2, 10, 0));

                        ngay.setOpaque(false);

                        ngay.add(UiKit.captioned("Tu ngay (dd/MM/yyyy)", txtBd));
                        ngay.add(UiKit.captioned("Den ngay (dd/MM/yyyy)", txtKt));

                        JPanel giam = new JPanel(new GridLayout(1, 2, 10, 0));

                        giam.setOpaque(false);

                        giam.add(UiKit.captioned("Muc giam (%) *", txtPt));
                        giam.add(UiKit.captioned("So luot toi da", txtSl));

                        JPanel body = new JPanel();

                        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

                        body.setOpaque(false);

                        them(body, UiKit.captioned("Ten chuong trinh *", txtTen));
                        them(body, Box.createVerticalStrut(8));
                        them(body, giam);
                        them(body, Box.createVerticalStrut(8));
                        them(body, UiKit.captioned("Dieu kien ap dung", dk));
                        them(body, Box.createVerticalStrut(8));
                        them(body, ngay);
                        them(body, Box.createVerticalStrut(4));
                        them(body, UiKit.label("De trong ngay / so luot = khong gioi han.", UiKit.F, UiKit.MUTED));
                        them(body, Box.createVerticalStrut(8));
                        them(body, chkHoatDong);

                        // body nam o phia tren de cac o khong bi gian chieu cao
                        JPanel wrap = new JPanel(new BorderLayout());

                        wrap.setOpaque(false);

                        wrap.add(body, BorderLayout.NORTH);

                        JScrollPane sp = new JScrollPane(wrap);

                        sp.setBorder(null);

                        sp.getViewport().setBackground(Color.WHITE);

                        // ---------------- nut ----------------
                        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

                        nut.setOpaque(false);

                        JButton btnHuy = UiKit.button("Huy", Color.WHITE, UiKit.NAVY);
                        JButton btnLuu = UiKit.button("Luu voucher", UiKit.GREEN, Color.WHITE);

                        btnHuy.addActionListener(e -> dong.run());
                        btnLuu.addActionListener(e -> luu());

                        nut.add(btnHuy);
                        nut.add(btnLuu);

                        add(head, BorderLayout.NORTH);
                        add(sp, BorderLayout.CENTER);
                        add(nut, BorderLayout.SOUTH);
                }

                private static void them(JPanel p, Component c) {

                        if (c instanceof JComponent) {
                                ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
                        }

                        p.add(c);
                }

                // Mo form: goc = null -> tao moi (de trong); goc != null -> do du lieu de sua
                public void mo(Voucher goc) {

                        this.goc = goc;

                        lblTieuDe.setText(goc == null ? "Tao voucher" : "Sua voucher");

                        lblMa.setText(goc == null ? "Ma se tu dong sinh (VC001, VC002...)"
                                        : "Ma: " + goc.getMaVoucher());

                        if (goc == null) {

                                txtTen.setText("");
                                txtPt.setText("");
                                txtDk.setText("");
                                txtBd.setText("");
                                txtKt.setText("");
                                txtSl.setText("");

                                chkHoatDong.setSelected(true);

                                return;
                        }

                        txtTen.setText(goc.getTenVoucher());

                        txtPt.setText(phanTram(goc.getPhanTramGiam()).replace("%", ""));

                        txtDk.setText(goc.getDieuKien() == null ? "" : goc.getDieuKien());

                        txtBd.setText(goc.getNgayBatDau() == null ? "" : goc.getNgayBatDau().format(D));

                        txtKt.setText(goc.getNgayKetThuc() == null ? "" : goc.getNgayKetThuc().format(D));

                        txtSl.setText(goc.getSoLuongToiDa() == null ? "" : String.valueOf(goc.getSoLuongToiDa()));

                        chkHoatDong.setSelected(goc.isTrangThai());
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

                        dong.run();
                }
        }

        // =========================================================
        // UiKit: mau, nut, bang dung chung (gop ngay trong file nay)
        // =========================================================

        static class UiKit {

                public static final Color NAVY = new Color(18, 48, 82);
                public static final Color LIGHT_BLUE = new Color(229, 241, 252);
                public static final Color GREEN = new Color(72, 196, 62);
                public static final Color RED = new Color(210, 70, 70);
                public static final Color BORDER = new Color(219, 228, 239);
                public static final Color MUTED = new Color(107, 124, 147);

                public static final Font F = new Font("Segoe UI", Font.PLAIN, 12);
                public static final Font FB = new Font("Segoe UI", Font.BOLD, 12);
                public static final Font BIG = new Font("Segoe UI", Font.BOLD, 20);

                public static JPanel card(LayoutManager lm) {

                        JPanel p = new JPanel(lm);

                        p.setBackground(Color.WHITE);

                        p.setBorder(new CompoundBorder(
                                        new LineBorder(BORDER, 1, true),
                                        new EmptyBorder(10, 14, 10, 14)));

                        return p;
                }

                public static JLabel label(String text, Font font, Color color) {

                        JLabel l = new JLabel(text);

                        l.setFont(font);

                        l.setForeground(color);

                        return l;
                }

                public static JButton button(String text, Color bg, Color fg) {

                        JButton b = new JButton(text);

                        b.setFont(FB);
                        b.setBackground(bg);
                        b.setForeground(fg);
                        b.setOpaque(true);
                        b.setFocusPainted(false);
                        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

                        b.setBorder(new CompoundBorder(
                                        new LineBorder(bg.equals(Color.WHITE) ? BORDER : bg, 1, true),
                                        new EmptyBorder(7, 16, 7, 16)));

                        return b;
                }

                public static void field(JTextField t) {

                        t.setFont(F);

                        t.setBorder(new CompoundBorder(
                                        new LineBorder(BORDER, 1, true),
                                        new EmptyBorder(6, 8, 6, 8)));
                }

                // nhan nho phia tren 1 o nhap
                public static JPanel captioned(String caption, JComponent c) {

                        JPanel p = new JPanel(new BorderLayout(0, 3));

                        p.setOpaque(false);

                        p.add(label(caption, FB, NAVY), BorderLayout.NORTH);

                        p.add(c, BorderLayout.CENTER);

                        return p;
                }

                // 12345 -> 12.345
                public static String tien(long v) {

                        return String.format(Locale.US, "%,d", v).replace(',', '.');
                }

                // thanh tieu de: ngay gio (trai), ten man hinh (giua), nut (phai)
                public static JPanel titleBar(String title, JComponent east) {

                        JPanel panel = new JPanel(new BorderLayout());

                        panel.setBackground(new Color(247, 249, 251));

                        panel.setBorder(new EmptyBorder(10, 25, 10, 25));

                        JLabel date = new JLabel();

                        date.setFont(FB);

                        Timer timer = new Timer(1000, e -> date.setText(
                                        LocalDateTime.now().format(
                                                        DateTimeFormatter.ofPattern("HH:mm:ss | dd/MM/yyyy"))));

                        timer.setInitialDelay(0);

                        timer.start();

                        JLabel lb = new JLabel(title, SwingConstants.CENTER);

                        lb.setFont(new Font("Segoe UI", Font.BOLD, 25));

                        lb.setForeground(NAVY);

                        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

                        west.setOpaque(false);

                        west.setPreferredSize(new Dimension(220, 30));

                        west.add(date);

                        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));

                        right.setOpaque(false);

                        right.setPreferredSize(new Dimension(220, 30));

                        if (east != null) {
                                right.add(east);
                        }

                        panel.add(west, BorderLayout.WEST);
                        panel.add(lb, BorderLayout.CENTER);
                        panel.add(right, BorderLayout.EAST);

                        return panel;
                }

                public static void styleTable(JTable t) {

                        t.setFont(F);
                        t.setRowHeight(32);
                        t.setShowVerticalLines(false);
                        t.setGridColor(BORDER);
                        t.setSelectionBackground(new Color(227, 238, 251));
                        t.setSelectionForeground(NAVY);
                        t.setFillsViewportHeight(true);

                        JTableHeader h = t.getTableHeader();

                        h.setFont(FB);
                        h.setBackground(NAVY);
                        h.setForeground(Color.WHITE);
                        h.setReorderingAllowed(false);
                }

                // To mau o trang thai lich hen
                public static class TrangThaiRenderer extends DefaultTableCellRenderer {

                        @Override
                        public Component getTableCellRendererComponent(
                                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {

                                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, r, c);

                                Color bg;
                                Color fg;

                                switch (String.valueOf(v)) {
                                        case "Da xac nhan":
                                                bg = new Color(217, 245, 229);
                                                fg = new Color(12, 107, 64);
                                                break;
                                        case "Cho xac nhan":
                                                bg = new Color(230, 232, 236);
                                                fg = new Color(51, 51, 51);
                                                break;
                                        case "Hoan thanh":
                                                bg = new Color(220, 235, 255);
                                                fg = new Color(31, 95, 191);
                                                break;
                                        case "Da huy":
                                                bg = new Color(255, 217, 217);
                                                fg = new Color(180, 35, 24);
                                                break;
                                        case "Dang chay":
                                                bg = new Color(217, 245, 229);
                                                fg = new Color(12, 107, 64);
                                                break;
                                        case "Sap chay":
                                                bg = new Color(230, 232, 236);
                                                fg = new Color(51, 51, 51);
                                                break;
                                        case "Het han":
                                        case "Het luot":
                                                bg = new Color(255, 240, 205);
                                                fg = new Color(146, 90, 0);
                                                break;
                                        default:
                                                bg = Color.WHITE;
                                                fg = NAVY;
                                }

                                l.setOpaque(true);
                                l.setBackground(bg);
                                l.setForeground(fg);
                                l.setHorizontalAlignment(SwingConstants.CENTER);

                                return l;
                        }
                }
        }
}
