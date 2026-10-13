package UI;

import dao.KhachHangDAO;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class QuanLyKhachHang extends JPanel {

        private static final String[] COLS = { "Ma KH", "Khach hang", "So dien thoai", "Thu cung",
                        "Hang", "Diem", "Tong chi", "Gan nhat" };

        private final KhachHangDAO khachHangDAO = new KhachHangDAO();

        private final JTextField txtTim = new JTextField(30);

        private final JComboBox<String> cboHang = new JComboBox<>(
                        new String[] { "Tat ca hang", "Khong co the", "Silver", "Gold", "Platinum" });

        private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                        return false;
                }
        };

        private final JTable table = new JTable(model);

        private final JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));

        private final JPanel danhSach = new JPanel(new BorderLayout());

        // form them khach hang hien thanh cua so popup (JDialog) giong "Dat lich moi"
        private ThemKhachHang formThem;
        private JDialog dlgThem;

        // maNhanVien: nhan vien dang dang nhap (ghi vao the thanh vien khi them khach)
        public QuanLyKhachHang(String maNhanVien) {

                setLayout(new BorderLayout());

                danhSach.setBackground(UiKit.LIGHT_BLUE);

                setBackground(UiKit.LIGHT_BLUE);

                JButton btnThem = UiKit.button("Them khach hang", UiKit.GREEN, Color.WHITE);

                btnThem.addActionListener(e -> moFormThem());

                danhSach.add(UiKit.titleBar("QUAN LY KHACH HANG", btnThem), BorderLayout.NORTH);

                stats.setOpaque(false);

                // ---------------- the tim kiem ----------------
                JPanel search = UiKit.card(new FlowLayout(FlowLayout.LEFT, 12, 0));

                UiKit.field(txtTim);

                cboHang.setFont(UiKit.F);

                JButton btnTim = UiKit.button("Tim kiem", UiKit.NAVY, Color.WHITE);
                JButton btnXuat = UiKit.button("Xuat danh sach", Color.WHITE, UiKit.NAVY);

                btnTim.addActionListener(e -> taiLai());
                txtTim.addActionListener(e -> taiLai());
                btnXuat.addActionListener(e -> xuatCsv());

                JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

                nut.setOpaque(false);
                nut.setBorder(new EmptyBorder(16, 0, 0, 0));
                nut.add(btnTim);
                nut.add(btnXuat);

                search.add(UiKit.captioned("Tim khach hang (ten, SDT hoac ma KH)", txtTim));
                search.add(UiKit.captioned("Hang thanh vien", cboHang));
                search.add(nut);

                // ---------------- bang ----------------
                UiKit.styleTable(table);

                DefaultTableCellRenderer phai = new DefaultTableCellRenderer();

                phai.setHorizontalAlignment(SwingConstants.RIGHT);

                table.getColumnModel().getColumn(5).setCellRenderer(phai);
                table.getColumnModel().getColumn(6).setCellRenderer(phai);

                int[] w = { 70, 160, 110, 220, 80, 90, 120, 100 };

                for (int i = 0; i < w.length; i++) {
                        table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
                }

                JScrollPane sp = new JScrollPane(table);

                sp.setBorder(BorderFactory.createLineBorder(UiKit.BORDER));

                sp.getViewport().setBackground(Color.WHITE);

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

                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

                actions.setOpaque(false);

                JButton btnXoa = UiKit.button("Xoa khach hang", UiKit.RED, Color.WHITE);

                btnXoa.addActionListener(e -> xoaKhach());

                actions.add(btnXoa);

                center.add(actions, BorderLayout.SOUTH);

                formThem = new ThemKhachHang(
                                maNhanVien,
                                () -> {
                                        dongPopup();
                                        taiLai();
                                });

                danhSach.add(center, BorderLayout.CENTER);

                add(danhSach, BorderLayout.CENTER);

                taiLai();
        }

        private JPanel theThongKe(String title, String value) {

                JPanel p = UiKit.card(new GridLayout(2, 1));

                p.add(UiKit.label(title, UiKit.F, UiKit.MUTED));
                p.add(UiKit.label(value, UiKit.BIG, UiKit.NAVY));

                return p;
        }

        // Goi khi mo man hinh: dong popup them va nap lai du lieu
        public void lamMoi() {

                dongPopup();

                taiLai();
        }

        private void dongPopup() {

                if (dlgThem != null) {

                        dlgThem.dispose();

                        dlgThem = null;
                }
        }

        // Hien popup "Them khach hang" noi len tren man hinh nay
        private void moFormThem() {

                formThem.lamMoi();

                Window owner = SwingUtilities.getWindowAncestor(this);

                dlgThem = new JDialog(owner, "Th\u00eam Kh\u00e1ch H\u00e0ng",
                                Dialog.ModalityType.APPLICATION_MODAL);

                dlgThem.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

                JPanel content = new JPanel(new BorderLayout());

                content.setBackground(UiKit.LIGHT_BLUE);

                content.setBorder(new EmptyBorder(14, 14, 14, 14));

                formThem.setPreferredSize(new Dimension(580, 640));

                content.add(formThem, BorderLayout.CENTER);

                dlgThem.setContentPane(content);

                // nhan ESC de dong popup
                dlgThem.getRootPane().registerKeyboardAction(
                                e -> dongPopup(),
                                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                                JComponent.WHEN_IN_FOCUSED_WINDOW);

                dlgThem.pack();

                dlgThem.setLocationRelativeTo(owner);

                dlgThem.setVisible(true);
        }

        // Nap lai thong ke + bang (goi lai sau khi them khach)
        public void taiLai() {

                long[] tk = khachHangDAO.thongKe();

                stats.removeAll();

                stats.add(theThongKe("Tong khach hang", UiKit.tien(tk[0])));
                stats.add(theThongKe("Khach co the thanh vien", UiKit.tien(tk[1])));
                stats.add(theThongKe("Tong diem tich luy", UiKit.tien(tk[2])));
                stats.add(theThongKe("Khach mua thang nay", UiKit.tien(tk[3])));

                stats.revalidate();
                stats.repaint();

                ArrayList<KhachHang> ds = khachHangDAO.timKiem(
                                txtTim.getText(), (String) cboHang.getSelectedItem());

                model.setRowCount(0);

                for (KhachHang k : ds) {

                        model.addRow(new Object[] {
                                        k.getMaKhachHang(),
                                        k.getHoTen(),
                                        k.getSoDienThoai(),
                                        k.getThuCung() == null ? "" : k.getThuCung(),
                                        k.getHangThe() == null ? "-" : k.getHangThe(),
                                        UiKit.tien(k.getDiemTichLuy()),
                                        UiKit.tien(k.getTongChi()) + " d",
                                        k.getGanNhat() == null ? "" : k.getGanNhat() });
                }
        }

        private void xoaKhach() {

                int row = table.getSelectedRow();

                if (row < 0) {

                        JOptionPane.showMessageDialog(this, "Hay chon 1 khach hang trong bang.");

                        return;
                }

                String ma = String.valueOf(model.getValueAt(row, 0));
                String ten = String.valueOf(model.getValueAt(row, 1));

                int ok = JOptionPane.showConfirmDialog(this,
                                "Xoa khach hang " + ma + " - " + ten + "?\n"
                                                + "(Cung xoa the thanh vien va thu cung cua khach)",
                                "Xac nhan xoa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (ok != JOptionPane.YES_OPTION) {
                        return;
                }

                String loi = khachHangDAO.xoa(ma);

                if (loi != null) {

                        JOptionPane.showMessageDialog(this, loi, "Khong xoa duoc",
                                        JOptionPane.WARNING_MESSAGE);

                        return;
                }

                taiLai();
        }

        // Xuat file CSV (mo duoc bang Excel)
        private void xuatCsv() {

                JFileChooser fc = new JFileChooser();

                fc.setSelectedFile(new java.io.File("danh_sach_khach_hang.csv"));

                if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                        return;
                }

                try (Writer w = new OutputStreamWriter(
                                new FileOutputStream(fc.getSelectedFile()), StandardCharsets.UTF_8)) {

                        w.write('\uFEFF');

                        for (int c = 0; c < model.getColumnCount(); c++) {
                                w.write((c > 0 ? "," : "") + model.getColumnName(c));
                        }

                        w.write("\r\n");

                        for (int r = 0; r < model.getRowCount(); r++) {

                                for (int c = 0; c < model.getColumnCount(); c++) {

                                        String v = String.valueOf(model.getValueAt(r, c)).replace("\"", "\"\"");

                                        w.write((c > 0 ? "," : "") + "\"" + v + "\"");
                                }

                                w.write("\r\n");
                        }

                        JOptionPane.showMessageDialog(this, "Da xuat danh sach.");

                } catch (Exception ex) {

                        JOptionPane.showMessageDialog(this, "Xuat that bai: " + ex.getMessage(),
                                        "Loi", JOptionPane.ERROR_MESSAGE);
                }
        }

        // =========================================================
        // FORM THEM KHACH HANG (class long, hien trong popup)
        // =========================================================

        // Form them khach hang: hien trong cua so popup tren man Quan ly khach hang
        public static class ThemKhachHang extends JPanel {

                // Gia tri luu vao CSDL (dung ky tu Unicode escape de khong phu thuoc ma hoa
                // file)
                private static final String[] LOAI = { "Ch\u00f3", "M\u00e8o", "Kh\u00e1c" };
                private static final String[] GIOI_TINH = { "\u0110\u1ef1c", "C\u00e1i" };
                private static final String[] HANG = { "Khong co the", "Silver", "Gold", "Platinum" };

                private final KhachHangDAO khachHangDAO = new KhachHangDAO();

                private final String maNhanVien;
                private final Runnable dong;

                private final JTextField txtTen = new JTextField();
                private final JTextField txtSdt = new JTextField();
                private final JTextField txtEmail = new JTextField();
                private final JTextField txtDiaChi = new JTextField();
                private final JComboBox<String> cboHang = new JComboBox<>(HANG);
                private final JSpinner spnDiem = new JSpinner(new SpinnerNumberModel(0, 0, 1000000, 10));

                private final JPanel petsBox = new JPanel();
                private final ArrayList<PetRow> rows = new ArrayList<>();

                // dong: chay khi bam Dong / Huy / sau khi luu thanh cong (dong popup, nap lai
                // danh sach)
                public ThemKhachHang(String maNhanVien, Runnable dong) {

                        this.maNhanVien = maNhanVien;
                        this.dong = dong;

                        setLayout(new BorderLayout(0, 8));

                        setBackground(Color.WHITE);

                        setBorder(new javax.swing.border.CompoundBorder(
                                        new javax.swing.border.LineBorder(UiKit.BORDER, 1, true),
                                        new EmptyBorder(12, 14, 12, 14)));

                        setPreferredSize(new Dimension(390, 100));

                        // ---------------- tieu de ----------------
                        JPanel head = new JPanel(new BorderLayout());

                        head.setOpaque(false);

                        head.add(UiKit.label("Them khach hang", new Font("Segoe UI", Font.BOLD, 16), UiKit.NAVY),
                                        BorderLayout.WEST);

                        JButton btnDong = UiKit.button("Dong", Color.WHITE, UiKit.NAVY);

                        btnDong.addActionListener(e -> dong.run());

                        head.add(btnDong, BorderLayout.EAST);

                        // ---------------- noi dung ----------------
                        UiKit.field(txtTen);
                        UiKit.field(txtSdt);
                        UiKit.field(txtEmail);
                        UiKit.field(txtDiaChi);

                        cboHang.setFont(UiKit.F);

                        ((JSpinner.DefaultEditor) spnDiem.getEditor()).getTextField().setFont(UiKit.F);

                        petsBox.setLayout(new BoxLayout(petsBox, BoxLayout.Y_AXIS));

                        petsBox.setOpaque(false);

                        JButton btnThemTc = UiKit.button("+ Them thu cung", Color.WHITE, UiKit.NAVY);

                        btnThemTc.addActionListener(e -> themDongThuCung());

                        JPanel body = new JPanel();

                        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

                        body.setOpaque(false);

                        them(body, UiKit.captioned("Ho va ten *", txtTen));
                        them(body, Box.createVerticalStrut(8));
                        them(body, UiKit.captioned("So dien thoai *", txtSdt));
                        them(body, Box.createVerticalStrut(8));
                        them(body, UiKit.captioned("Email", txtEmail));
                        them(body, Box.createVerticalStrut(8));
                        them(body, UiKit.captioned("Dia chi", txtDiaChi));
                        them(body, Box.createVerticalStrut(8));

                        JPanel hang = new JPanel(new GridLayout(1, 2, 10, 0));

                        hang.setOpaque(false);

                        hang.add(UiKit.captioned("Hang thanh vien", cboHang));
                        hang.add(UiKit.captioned("Diem khoi tao", spnDiem));

                        them(body, hang);
                        them(body, Box.createVerticalStrut(12));
                        them(body, UiKit.label("Thu cung", UiKit.FB, UiKit.NAVY));
                        them(body, Box.createVerticalStrut(4));
                        them(body, petsBox);
                        them(body, btnThemTc);

                        // body nam o phia tren de cac o khong bi gian chieu cao
                        JPanel wrap = new JPanel(new BorderLayout());

                        wrap.setOpaque(false);

                        wrap.add(body, BorderLayout.NORTH);

                        JScrollPane sp = new JScrollPane(wrap);

                        sp.setBorder(null);

                        sp.getViewport().setBackground(Color.WHITE);

                        sp.getVerticalScrollBar().setUnitIncrement(16);

                        // ---------------- nut ----------------
                        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

                        nut.setOpaque(false);

                        JButton btnHuy = UiKit.button("Huy", Color.WHITE, UiKit.NAVY);
                        JButton btnLuu = UiKit.button("Luu khach hang", UiKit.GREEN, Color.WHITE);

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

                // =========================================================
                // DONG THU CUNG (2 hang cho vua khung hep)
                // =========================================================

                private class PetRow extends JPanel {

                        final JTextField ten = new JTextField();
                        final JTextField giong = new JTextField();
                        final JTextField canNang = new JTextField();
                        final JComboBox<String> loai = new JComboBox<>(LOAI);
                        final JComboBox<String> gioiTinh = new JComboBox<>(GIOI_TINH);

                        PetRow() {

                                super(new GridLayout(2, 3, 8, 6));

                                setOpaque(false);

                                setAlignmentX(Component.LEFT_ALIGNMENT);

                                setBorder(new javax.swing.border.CompoundBorder(
                                                BorderFactory.createMatteBorder(0, 0, 1, 0, UiKit.BORDER),
                                                new EmptyBorder(4, 0, 8, 0)));

                                UiKit.field(ten);
                                UiKit.field(giong);
                                UiKit.field(canNang);

                                loai.setFont(UiKit.F);
                                gioiTinh.setFont(UiKit.F);

                                JButton xoa = UiKit.button("Xoa", Color.WHITE, UiKit.RED);

                                xoa.addActionListener(e -> {
                                        petsBox.remove(this);
                                        rows.remove(this);
                                        petsBox.revalidate();
                                        petsBox.repaint();
                                });

                                JPanel xp = new JPanel(new BorderLayout(0, 3));

                                xp.setOpaque(false);

                                xp.add(UiKit.label(" ", UiKit.FB, UiKit.NAVY), BorderLayout.NORTH);
                                xp.add(xoa, BorderLayout.CENTER);

                                add(UiKit.captioned("Ten *", ten));
                                add(UiKit.captioned("Loai", loai));
                                add(UiKit.captioned("Giong", giong));
                                add(UiKit.captioned("Gioi tinh", gioiTinh));
                                add(UiKit.captioned("Can nang (kg)", canNang));
                                add(xp);
                        }
                }

                private void themDongThuCung() {

                        PetRow r = new PetRow();

                        rows.add(r);

                        petsBox.add(r);

                        petsBox.revalidate();
                        petsBox.repaint();
                }

                // =========================================================
                // LUU
                // =========================================================

                private void loi(String msg) {

                        JOptionPane.showMessageDialog(this, msg, "Du lieu chua hop le",
                                        JOptionPane.WARNING_MESSAGE);
                }

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
                        ArrayList<KhachHangDAO.ThuCungMoi> pets = new ArrayList<>();

                        for (PetRow r : rows) {

                                String tenTC = r.ten.getText().trim();
                                String giong = r.giong.getText().trim();

                                if (tenTC.isEmpty() || tenTC.length() > 50) {
                                        loi("Moi thu cung can co ten (bam Xoa neu khong dung).");
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

                                pets.add(new KhachHangDAO.ThuCungMoi(tenTC, (String) r.loai.getSelectedItem(),
                                                giong, (String) r.gioiTinh.getSelectedItem(), kg));
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

                        dong.run();
                }

                // Xoa trang form (goi khi mo khung)
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
