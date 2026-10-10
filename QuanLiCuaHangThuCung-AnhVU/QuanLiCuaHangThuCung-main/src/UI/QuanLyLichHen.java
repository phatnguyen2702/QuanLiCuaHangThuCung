package UI;

import dao.KhachHangDAO;
import dao.LichHenDAO;
import entity.KhachHang;
import entity.LichHen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class QuanLyLichHen extends JPanel {

        private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        private static final String[] COLS = { "Ma lich", "Ngay & gio", "Ten khach hang", "SDT",
                        "Ten thu cung", "Nhan vien Spa", "Trang thai", "Ghi chu" };

        private final LichHenDAO lichHenDAO = new LichHenDAO();
        private final KhachHangDAO khachHangDAO = new KhachHangDAO();

        private final JTextField txtSdt = new JTextField(22);
        private final JTextField txtTu = new JTextField(9);
        private final JTextField txtDen = new JTextField(9);
        private final JComboBox<String> cboNv = new JComboBox<>();
        private final JComboBox<String> cboTt = new JComboBox<>(
                        new String[] { "Tat ca", "Cho xac nhan", "Da xac nhan", "Hoan thanh", "Da huy" });

        private final ArrayList<String> dsMaNv = new ArrayList<>();

        private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                        return false;
                }
        };

        private final JTable table = new JTable(model);
        private final JPanel infoPanel = new JPanel(new GridLayout(1, 4, 12, 0));

        private KhachHang khachHienTai = null;

        private final CardLayout cards = new CardLayout();
        private final JPanel danhSach = new JPanel(new BorderLayout());
        private JPanel datLichPanel = null;

        public QuanLyLichHen() {

                setLayout(cards);

                setBackground(UiKit.LIGHT_BLUE);

                danhSach.setBackground(UiKit.LIGHT_BLUE);

                JButton btnMoi = UiKit.button("Dat lich moi", UiKit.GREEN, Color.WHITE);

                btnMoi.addActionListener(e -> datLichMoi());

                danhSach.add(UiKit.titleBar("QUAN LY LICH HEN SPA", btnMoi), BorderLayout.NORTH);

                // ---------------- phan tren: tra cuu + thong tin khach + bo loc ----------------
                JPanel top = new JPanel();

                top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

                top.setOpaque(false);

                top.add(taoTheTraCuu());
                top.add(Box.createVerticalStrut(8));

                infoPanel.setOpaque(false);
                infoPanel.setVisible(false);
                top.add(infoPanel);
                top.add(Box.createVerticalStrut(8));

                top.add(taoTheBoLoc());

                // ---------------- bang ----------------
                UiKit.styleTable(table);

                table.getColumnModel().getColumn(6).setCellRenderer(new UiKit.TrangThaiRenderer());

                int[] w = { 70, 120, 150, 100, 150, 140, 100, 180 };

                for (int i = 0; i < w.length; i++) {
                        table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
                }

                JScrollPane sp = new JScrollPane(table);

                sp.setBorder(BorderFactory.createLineBorder(UiKit.BORDER));

                sp.getViewport().setBackground(Color.WHITE);

                // ---------------- thao tac tren dong dang chon ----------------
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

                actions.setOpaque(false);

                JButton btnXacNhan = UiKit.button("Xac nhan", UiKit.NAVY, Color.WHITE);
                JButton btnHoanThanh = UiKit.button("Hoan thanh", UiKit.GREEN, Color.WHITE);
                JButton btnHuy = UiKit.button("Huy lich", UiKit.RED, Color.WHITE);

                btnXacNhan.addActionListener(e -> doiTrangThai("Da xac nhan"));
                btnHoanThanh.addActionListener(e -> doiTrangThai("Hoan thanh"));
                btnHuy.addActionListener(e -> doiTrangThai("Da huy"));

                actions.add(btnXacNhan);
                actions.add(btnHoanThanh);
                actions.add(btnHuy);

                JButton btnXoa = UiKit.button("Xoa lich", Color.WHITE, UiKit.RED);

                btnXoa.addActionListener(e -> xoaLich());

                actions.add(btnXoa);

                JPanel center = new JPanel(new BorderLayout(0, 8));

                center.setOpaque(false);

                center.setBorder(new EmptyBorder(12, 25, 15, 25));

                center.add(top, BorderLayout.NORTH);
                center.add(sp, BorderLayout.CENTER);
                center.add(actions, BorderLayout.SOUTH);

                danhSach.add(center, BorderLayout.CENTER);

                add(danhSach, "DS");

                lamMoi();
        }

        // Goi khi mo man hinh de nap lai du lieu
        public void lamMoi() {

                taiNhanVien();

                taiDuLieu();
        }

        // =========================================================
        // XAY DUNG GIAO DIEN
        // =========================================================

        private JPanel taoTheTraCuu() {

                JPanel card = UiKit.card(new BorderLayout(0, 6));

                JPanel title = new JPanel(new GridLayout(2, 1));

                title.setOpaque(false);

                title.add(UiKit.label("Tra cuu theo so dien thoai", UiKit.FB, UiKit.NAVY));
                title.add(UiKit.label("Nhap SDT khach hang de xem toan bo lich hen va chi tieu Spa.",
                                UiKit.F, UiKit.MUTED));

                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

                row.setOpaque(false);

                UiKit.field(txtSdt);

                JButton btnTra = UiKit.button("Tra cuu", UiKit.NAVY, Color.WHITE);
                JButton btnXoa = UiKit.button("Xoa bo loc", Color.WHITE, UiKit.NAVY);

                btnTra.addActionListener(e -> traCuu());
                txtSdt.addActionListener(e -> traCuu());
                btnXoa.addActionListener(e -> xoaLoc());

                row.add(txtSdt);
                row.add(btnTra);
                row.add(btnXoa);

                card.add(title, BorderLayout.NORTH);
                card.add(row, BorderLayout.CENTER);

                return card;
        }

        private JPanel taoTheBoLoc() {

                JPanel card = UiKit.card(new FlowLayout(FlowLayout.LEFT, 14, 0));

                UiKit.field(txtTu);
                UiKit.field(txtDen);

                cboNv.setFont(UiKit.F);
                cboTt.setFont(UiKit.F);

                card.add(UiKit.captioned("Tu (dd/MM/yyyy)", txtTu));
                card.add(UiKit.captioned("Den (dd/MM/yyyy)", txtDen));
                card.add(UiKit.captioned("Nhan vien", cboNv));
                card.add(UiKit.captioned("Trang thai", cboTt));

                JButton btnLoc = UiKit.button("Ap dung", UiKit.NAVY, Color.WHITE);

                btnLoc.addActionListener(e -> taiDuLieu());

                JPanel wrap = new JPanel(new BorderLayout());

                wrap.setOpaque(false);

                wrap.setBorder(new EmptyBorder(16, 0, 0, 0));

                wrap.add(btnLoc, BorderLayout.CENTER);

                card.add(wrap);

                return card;
        }

        private JPanel miniCard(String title, String value) {

                JPanel p = UiKit.card(new GridLayout(2, 1));

                p.add(UiKit.label(title, UiKit.F, UiKit.MUTED));
                p.add(UiKit.label(value, UiKit.BIG, UiKit.NAVY));

                return p;
        }

        private void hienThongTinKhach(KhachHang kh) {

                long[] tk = lichHenDAO.thongKeKhach(kh.getMaKhachHang());

                infoPanel.removeAll();

                // the ten khach
                JPanel who = UiKit.card(new BorderLayout(10, 0));

                String[] w = kh.getHoTen().trim().split("\\s+");

                String chuDau = ("" + w[0].charAt(0)
                                + (w.length > 1 ? w[w.length - 1].charAt(0) : "")).toUpperCase();

                JLabel avatar = new JLabel(chuDau, SwingConstants.CENTER) {
                        @Override
                        protected void paintComponent(Graphics g0) {

                                Graphics2D g2 = (Graphics2D) g0.create();

                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                                RenderingHints.VALUE_ANTIALIAS_ON);

                                g2.setColor(new Color(248, 215, 230));

                                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);

                                g2.dispose();

                                super.paintComponent(g0);
                        }
                };

                avatar.setFont(UiKit.FB);
                avatar.setPreferredSize(new Dimension(38, 38));

                JPanel txt = new JPanel(new GridLayout(3, 1));

                txt.setOpaque(false);

                txt.add(UiKit.label(kh.getHoTen(), UiKit.FB, UiKit.NAVY));
                txt.add(UiKit.label(kh.getSoDienThoai(), UiKit.F, UiKit.MUTED));
                txt.add(UiKit.label("Thu cung: "
                                + (kh.getThuCung() == null ? "-" : kh.getThuCung()), UiKit.F, UiKit.MUTED));

                who.add(avatar, BorderLayout.WEST);
                who.add(txt, BorderLayout.CENTER);

                infoPanel.add(who);
                infoPanel.add(miniCard("Lich sap toi", String.valueOf(tk[0])));
                infoPanel.add(miniCard("Da den", String.valueOf(tk[1])));
                infoPanel.add(miniCard("Tong chi Spa", UiKit.tien(tk[2])));

                infoPanel.setVisible(true);
                infoPanel.revalidate();
                infoPanel.repaint();
        }

        // =========================================================
        // XU LY
        // =========================================================

        private void taiNhanVien() {

                cboNv.removeAllItems();

                dsMaNv.clear();

                cboNv.addItem("Tat ca");

                dsMaNv.add("");

                for (String[] nv : lichHenDAO.getNhanVien()) {

                        cboNv.addItem(nv[0] + " - " + nv[1]);

                        dsMaNv.add(nv[0]);
                }
        }

        private void traCuu() {

                String sdt = txtSdt.getText().replaceAll("\\s", "");

                if (sdt.isEmpty()) {

                        xoaLoc();

                        return;
                }

                KhachHang kh = khachHangDAO.getBySdt(sdt);

                if (kh == null) {

                        khachHienTai = null;

                        infoPanel.setVisible(false);

                        model.setRowCount(0);

                        JOptionPane.showMessageDialog(this, "Khong tim thay khach hang co SDT " + sdt);

                        return;
                }

                khachHienTai = kh;

                hienThongTinKhach(kh);

                taiDuLieu();
        }

        private void xoaLoc() {

                txtSdt.setText("");
                txtTu.setText("");
                txtDen.setText("");

                if (cboNv.getItemCount() > 0) {
                        cboNv.setSelectedIndex(0);
                }

                cboTt.setSelectedIndex(0);

                khachHienTai = null;

                infoPanel.setVisible(false);

                taiDuLieu();
        }

        private LocalDate doc(JTextField f) {

                String s = f.getText().trim();

                return s.isEmpty() ? null : LocalDate.parse(s, D);
        }

        private void taiDuLieu() {

                try {

                        LocalDate tu = doc(txtTu);
                        LocalDate den = doc(txtDen);

                        int i = cboNv.getSelectedIndex();

                        String maNv = (i <= 0 || i >= dsMaNv.size()) ? "" : dsMaNv.get(i);

                        String sdt = txtSdt.getText().replaceAll("\\s", "");

                        ArrayList<LichHen> ds = lichHenDAO.getAll(
                                        sdt, tu, den, maNv, (String) cboTt.getSelectedItem());

                        model.setRowCount(0);

                        for (LichHen l : ds) {

                                model.addRow(new Object[] {
                                                l.getMaLichHen(),
                                                l.getNgayGioHen().format(DT),
                                                l.getTenKhachHang(),
                                                l.getSoDienThoai(),
                                                l.getTenThuCung(),
                                                l.getTenNhanVien(),
                                                l.getTrangThai(),
                                                l.getGhiChu() });
                        }

                } catch (DateTimeParseException ex) {

                        JOptionPane.showMessageDialog(this,
                                        "Ngay phai co dang dd/MM/yyyy, vi du 07/10/2026");
                }
        }

        private void datLichMoi() {

                if (datLichPanel != null) {
                        remove(datLichPanel);
                }

                datLichPanel = new DatLichMoi(
                                txtSdt.getText().replaceAll("\\s", ""),
                                () -> cards.show(this, "DS"),
                                () -> {

                                        taiDuLieu();

                                        if (khachHienTai != null) {
                                                hienThongTinKhach(khachHienTai);
                                        }

                                        cards.show(this, "DS");
                                });

                add(datLichPanel, "DAT");

                cards.show(this, "DAT");
        }

        private void xoaLich() {

                int row = table.getSelectedRow();

                if (row < 0) {

                        JOptionPane.showMessageDialog(this, "Hay chon 1 lich hen trong bang.");

                        return;
                }

                String ma = String.valueOf(model.getValueAt(row, 0));
                String tt = String.valueOf(model.getValueAt(row, 6));

                if (tt.equals("Hoan thanh")) {

                        JOptionPane.showMessageDialog(this,
                                        "Lich da hoan thanh duoc giu lai de luu lich su, khong the xoa.",
                                        "Khong xoa duoc", JOptionPane.WARNING_MESSAGE);

                        return;
                }

                int ok = JOptionPane.showConfirmDialog(this,
                                "Xoa han lich " + ma + "?\n(Cung xoa cac dich vu da chon cho lich nay)",
                                "Xac nhan xoa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (ok != JOptionPane.YES_OPTION) {
                        return;
                }

                if (!lichHenDAO.xoa(ma)) {

                        JOptionPane.showMessageDialog(this, "Xoa that bai.", "Loi",
                                        JOptionPane.ERROR_MESSAGE);

                        return;
                }

                taiDuLieu();

                if (khachHienTai != null) {
                        hienThongTinKhach(khachHienTai);
                }
        }

        private void doiTrangThai(String trangThaiMoi) {

                int row = table.getSelectedRow();

                if (row < 0) {

                        JOptionPane.showMessageDialog(this, "Hay chon 1 lich hen trong bang.");

                        return;
                }

                String ma = String.valueOf(model.getValueAt(row, 0));

                int ok = JOptionPane.showConfirmDialog(this,
                                "Chuyen lich " + ma + " sang \"" + trangThaiMoi + "\"?",
                                "Xac nhan", JOptionPane.YES_NO_OPTION);

                if (ok != JOptionPane.YES_OPTION) {
                        return;
                }

                if (!lichHenDAO.capNhatTrangThai(ma, trangThaiMoi)) {

                        JOptionPane.showMessageDialog(this, "Cap nhat that bai.", "Loi",
                                        JOptionPane.ERROR_MESSAGE);

                        return;
                }

                taiDuLieu();

                if (khachHienTai != null) {
                        hienThongTinKhach(khachHienTai);
                }
        }

        // =========================================================
        // MAN HINH DAT LICH MOI (class long trong QuanLyLichHen)
        // =========================================================

        // Man hinh "Dat lich moi" (nam trong QuanLyLichHen, hien thay cho danh sach)
        public static class DatLichMoi extends JPanel {

                private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("HH:mm");

                private final LichHenDAO lichHenDAO = new LichHenDAO();
                private final KhachHangDAO khachHangDAO = new KhachHangDAO();

                private final Runnable quayLai;
                private final Runnable sauKhiLuu;

                private final JTextField txtSdt = new JTextField(14);
                private final JTextField txtNgay = new JTextField(9);
                private final JTextField txtGio = new JTextField(6);
                private final JLabel lblKhach = UiKit.label("Chua chon khach hang", UiKit.F, UiKit.MUTED);
                private final JComboBox<String> cboThuCung = new JComboBox<>();
                private final JComboBox<String> cboNv = new JComboBox<>();
                private final JTextArea txtGhiChu = new JTextArea(3, 30);

                private final JLabel sKhach = UiKit.label("-", UiKit.FB, UiKit.NAVY);
                private final JLabel sPet = UiKit.label("-", UiKit.FB, UiKit.NAVY);
                private final JLabel sGio = UiKit.label("-", UiKit.FB, UiKit.NAVY);
                private final JLabel sNv = UiKit.label("-", UiKit.FB, UiKit.NAVY);
                private final JLabel sDv = UiKit.label("0", UiKit.FB, UiKit.NAVY);
                private final JLabel sTong = UiKit.label("0 d", UiKit.FB, UiKit.NAVY);

                private final ArrayList<String> dsMaTc = new ArrayList<>();
                private final ArrayList<String> dsMaNv = new ArrayList<>();
                private final ArrayList<String> dsMaDv = new ArrayList<>();
                private final ArrayList<Long> dsGiaDv = new ArrayList<>();
                private final ArrayList<JCheckBox> dsCheck = new ArrayList<>();

                private KhachHang khach = null;

                // quayLai: chay khi bam "< Danh sach" / Huy
                // sauKhiLuu: chay sau khi dat lich thanh cong
                public DatLichMoi(String sdtBanDau, Runnable quayLai, Runnable sauKhiLuu) {

                        this.quayLai = quayLai;
                        this.sauKhiLuu = sauKhiLuu;

                        setLayout(new BorderLayout());

                        setBackground(UiKit.LIGHT_BLUE);

                        JButton btnVe = UiKit.button("< Danh sach", Color.WHITE, UiKit.NAVY);

                        btnVe.addActionListener(e -> quayLai.run());

                        add(UiKit.titleBar("DAT LICH MOI", btnVe), BorderLayout.NORTH);

                        // ---------------- cot trai ----------------
                        JPanel left = new JPanel();

                        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

                        left.setOpaque(false);

                        left.add(taoTheKhach());
                        left.add(Box.createVerticalStrut(10));
                        left.add(taoTheThoiGian());
                        left.add(Box.createVerticalStrut(10));
                        left.add(taoTheDichVu());
                        left.add(Box.createVerticalStrut(10));
                        left.add(taoTheGhiChu());

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

                        sp.getVerticalScrollBar().setUnitIncrement(16);

                        add(sp, BorderLayout.CENTER);

                        // ---- gia tri mac dinh ----
                        LocalDateTime gio = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0);

                        txtNgay.setText(gio.format(D));
                        txtGio.setText(gio.format(T));

                        for (String[] nv : lichHenDAO.getNhanVienSpa()) {

                                cboNv.addItem(nv[0] + " - " + nv[1]);

                                dsMaNv.add(nv[0]);
                        }

                        if (sdtBanDau != null && !sdtBanDau.isEmpty()) {

                                txtSdt.setText(sdtBanDau);

                                timKhach();
                        }

                        // cap nhat tom tat khi nguoi dung thay doi
                        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
                                public void insertUpdate(javax.swing.event.DocumentEvent e) { capNhatTomTat(); }
                                public void removeUpdate(javax.swing.event.DocumentEvent e) { capNhatTomTat(); }
                                public void changedUpdate(javax.swing.event.DocumentEvent e) { capNhatTomTat(); }
                        };

                        txtNgay.getDocument().addDocumentListener(dl);
                        txtGio.getDocument().addDocumentListener(dl);

                        cboThuCung.addActionListener(e -> capNhatTomTat());
                        cboNv.addActionListener(e -> capNhatTomTat());

                        capNhatTomTat();
                }

                // =========================================================
                // XAY DUNG GIAO DIEN
                // =========================================================

                private JPanel taoTheKhach() {

                        JPanel card = UiKit.card(new BorderLayout(0, 6));

                        card.add(UiKit.label("Khach hang va thu cung", UiKit.FB, UiKit.NAVY), BorderLayout.NORTH);

                        UiKit.field(txtSdt);

                        cboThuCung.setFont(UiKit.F);

                        cboThuCung.setPrototypeDisplayValue("Ten thu cung (Giong thu cung dai)");

                        JButton btnTim = UiKit.button("Tim", UiKit.NAVY, Color.WHITE);

                        btnTim.addActionListener(e -> timKhach());
                        txtSdt.addActionListener(e -> timKhach());

                        JPanel wrap = new JPanel(new BorderLayout());

                        wrap.setOpaque(false);

                        wrap.setBorder(new EmptyBorder(16, 0, 0, 0));

                        wrap.add(btnTim, BorderLayout.CENTER);

                        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

                        row.setOpaque(false);

                        row.add(UiKit.captioned("SDT khach hang *", txtSdt));
                        row.add(wrap);
                        row.add(UiKit.captioned("Thu cung *", cboThuCung));

                        card.add(row, BorderLayout.CENTER);
                        card.add(lblKhach, BorderLayout.SOUTH);

                        return card;
                }

                private JPanel taoTheThoiGian() {

                        JPanel card = UiKit.card(new BorderLayout(0, 6));

                        card.add(UiKit.label("Thoi gian va nhan vien", UiKit.FB, UiKit.NAVY), BorderLayout.NORTH);

                        UiKit.field(txtNgay);
                        UiKit.field(txtGio);

                        cboNv.setFont(UiKit.F);

                        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));

                        row.setOpaque(false);

                        row.add(UiKit.captioned("Ngay (dd/MM/yyyy) *", txtNgay));
                        row.add(UiKit.captioned("Gio (HH:mm) *", txtGio));
                        row.add(UiKit.captioned("Nhan vien Spa *", cboNv));

                        card.add(row, BorderLayout.CENTER);

                        return card;
                }

                private JPanel taoTheDichVu() {

                        JPanel card = UiKit.card(new BorderLayout(0, 6));

                        card.add(UiKit.label("Dich vu (chon it nhat 1)", UiKit.FB, UiKit.NAVY), BorderLayout.NORTH);

                        JPanel list = new JPanel(new GridLayout(0, 2, 10, 4));

                        list.setOpaque(false);

                        for (String[] dv : lichHenDAO.getDichVu()) {

                                long gia = Long.parseLong(dv[2]);

                                JCheckBox cb = new JCheckBox(dv[1] + " - " + UiKit.tien(gia) + " d");

                                cb.setOpaque(false);
                                cb.setFont(UiKit.F);
                                cb.addItemListener(e -> capNhatTomTat());

                                dsMaDv.add(dv[0]);
                                dsGiaDv.add(gia);
                                dsCheck.add(cb);

                                list.add(cb);
                        }

                        card.add(list, BorderLayout.CENTER);

                        return card;
                }

                private JPanel taoTheGhiChu() {

                        JPanel card = UiKit.card(new BorderLayout(0, 6));

                        card.add(UiKit.label("Ghi chu", UiKit.FB, UiKit.NAVY), BorderLayout.NORTH);

                        txtGhiChu.setFont(UiKit.F);
                        txtGhiChu.setLineWrap(true);
                        txtGhiChu.setWrapStyleWord(true);

                        JScrollPane sp = new JScrollPane(txtGhiChu);

                        sp.setBorder(BorderFactory.createLineBorder(UiKit.BORDER));

                        card.add(sp, BorderLayout.CENTER);

                        return card;
                }

                private JPanel taoTheTomTat() {

                        JPanel card = UiKit.card(new BorderLayout(0, 8));

                        card.add(UiKit.label("Tom tat", UiKit.FB, UiKit.NAVY), BorderLayout.NORTH);

                        JPanel g = new JPanel(new GridLayout(6, 2, 8, 8));

                        g.setOpaque(false);

                        g.add(UiKit.label("Khach hang", UiKit.F, UiKit.MUTED));
                        g.add(sKhach);
                        g.add(UiKit.label("Thu cung", UiKit.F, UiKit.MUTED));
                        g.add(sPet);
                        g.add(UiKit.label("Gio hen", UiKit.F, UiKit.MUTED));
                        g.add(sGio);
                        g.add(UiKit.label("Nhan vien", UiKit.F, UiKit.MUTED));
                        g.add(sNv);
                        g.add(UiKit.label("So dich vu", UiKit.F, UiKit.MUTED));
                        g.add(sDv);
                        g.add(UiKit.label("Tam tinh", UiKit.F, UiKit.MUTED));
                        g.add(sTong);

                        card.add(g, BorderLayout.CENTER);

                        return card;
                }

                private JPanel taoNut() {

                        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

                        p.setOpaque(false);

                        JButton btnHuy = UiKit.button("Huy", Color.WHITE, UiKit.NAVY);
                        JButton btnLuu = UiKit.button("Dat lich", UiKit.GREEN, Color.WHITE);

                        btnHuy.addActionListener(e -> quayLai.run());
                        btnLuu.addActionListener(e -> luu());

                        p.add(btnHuy);
                        p.add(btnLuu);

                        return p;
                }

                // =========================================================
                // XU LY
                // =========================================================

                private void capNhatTomTat() {

                        sKhach.setText(khach == null ? "-" : khach.getHoTen());

                        sPet.setText(cboThuCung.getSelectedIndex() < 0 ? "-" : (String) cboThuCung.getSelectedItem());

                        sGio.setText(txtGio.getText().trim() + " " + txtNgay.getText().trim());

                        int i = cboNv.getSelectedIndex();

                        sNv.setText(i < 0 || i >= dsMaNv.size() ? "-" : dsMaNv.get(i));

                        long tong = 0;
                        int n = 0;

                        for (int k = 0; k < dsCheck.size(); k++) {

                                if (dsCheck.get(k).isSelected()) {

                                        n++;

                                        tong += dsGiaDv.get(k);
                                }
                        }

                        sDv.setText(String.valueOf(n));

                        sTong.setText(UiKit.tien(tong) + " d");
                }

                private void timKhach() {

                        String sdt = txtSdt.getText().replaceAll("\\s", "");

                        khach = null;

                        cboThuCung.removeAllItems();

                        dsMaTc.clear();

                        if (sdt.isEmpty()) {

                                lblKhach.setText("Hay nhap SDT khach hang.");

                                capNhatTomTat();

                                return;
                        }

                        KhachHang kh = khachHangDAO.getBySdt(sdt);

                        if (kh == null) {

                                lblKhach.setText("Khong tim thay khach hang co SDT " + sdt
                                                + ". Hay them khach hang truoc.");

                                capNhatTomTat();

                                return;
                        }

                        khach = kh;

                        for (String[] tc : lichHenDAO.getThuCungCuaKhach(kh.getMaKhachHang())) {

                                cboThuCung.addItem(tc[1]);

                                dsMaTc.add(tc[0]);
                        }

                        lblKhach.setText("Khach hang: " + kh.getHoTen() + " (" + kh.getMaKhachHang() + ")"
                                        + (dsMaTc.isEmpty() ? " - khach chua co thu cung nao." : ""));

                        capNhatTomTat();
                }

                private void canhBao(String msg) {

                        JOptionPane.showMessageDialog(this, msg, "Du lieu chua hop le",
                                        JOptionPane.WARNING_MESSAGE);
                }

                private void luu() {

                        if (khach == null) {

                                canhBao("Hay nhap SDT va bam Tim de chon khach hang.");

                                return;
                        }

                        if (cboThuCung.getSelectedIndex() < 0) {

                                canhBao("Khach hang chua co thu cung. Hay them thu cung truoc khi dat lich.");

                                return;
                        }

                        LocalDateTime tg;

                        try {

                                tg = LocalDateTime.of(
                                                LocalDate.parse(txtNgay.getText().trim(), D),
                                                LocalTime.parse(txtGio.getText().trim(), T));

                        } catch (DateTimeParseException ex) {

                                canhBao("Ngay phai dang dd/MM/yyyy (vi du 15/10/2026), gio dang HH:mm (vi du 09:30).");

                                return;
                        }

                        if (tg.isBefore(LocalDateTime.now())) {

                                canhBao("Thoi gian hen phai o tuong lai.");

                                return;
                        }

                        if (cboNv.getSelectedIndex() < 0) {

                                canhBao("Chua co nhan vien Spa de chon.");

                                return;
                        }

                        ArrayList<String> dv = new ArrayList<>();

                        for (int i = 0; i < dsCheck.size(); i++) {

                                if (dsCheck.get(i).isSelected()) {
                                        dv.add(dsMaDv.get(i));
                                }
                        }

                        if (dv.isEmpty()) {

                                canhBao("Hay chon it nhat 1 dich vu.");

                                return;
                        }

                        String ghiChu = txtGhiChu.getText().trim();

                        if (ghiChu.length() > 255) {

                                canhBao("Ghi chu toi da 255 ky tu.");

                                return;
                        }

                        String maNv = dsMaNv.get(cboNv.getSelectedIndex());

                        if (lichHenDAO.trungLich(maNv, tg)) {

                                canhBao("Nhan vien nay da co lich trong vong 60 phut quanh gio da chon.\n"
                                                + "Hay chon gio hoac nhan vien khac.");

                                return;
                        }

                        String ma = lichHenDAO.them(
                                        khach.getMaKhachHang(),
                                        dsMaTc.get(cboThuCung.getSelectedIndex()),
                                        maNv, tg, ghiChu, dv);

                        if (ma == null) {

                                JOptionPane.showMessageDialog(this,
                                                "Dat lich that bai. Xem loi chi tiet trong Console.",
                                                "Loi", JOptionPane.ERROR_MESSAGE);

                                return;
                        }

                        JOptionPane.showMessageDialog(this, "Da dat lich " + ma + " (trang thai: Cho xac nhan).");

                        sauKhiLuu.run();
                }
        }
}
