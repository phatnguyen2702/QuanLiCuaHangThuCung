package UI;

import UI.TrangChu.UIStyle;
import dao.KhachHangChiTietDAO;
import dao.KhachHangDAO;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;

// Man Quan ly khach hang: tim kiem, them / sua / xoa, lam moi, chon 1 dong de xem thong tin
public class QuanLyKhachHang extends JPanel {

        private static final String[] COLS = { "Mã KH", "Họ tên", "Số điện thoại", "Email",
                        "Thú cưng", "Hạng", "Điểm", "Tổng chi", "Gần nhất" };

        private static final String[] LOC_HIEN_THI = { "Tất cả hạng", "Không có thẻ", "Silver", "Gold", "Platinum" };

        // giá trị tương ứng mà KhachHangDAO.timKiem hiểu
        private static final String[] LOC_DAO = { "Tat ca hang", "Khong co the", "Silver", "Gold", "Platinum" };

        private final KhachHangDAO khachHangDAO = new KhachHangDAO();
        private final KhachHangChiTietDAO chiTietDAO = new KhachHangChiTietDAO();

        private final String maNhanVien;

        private final JTextField txtTim = UIStyle.o(16);
        private final JComboBox<String> cboHang = new JComboBox<>(LOC_HIEN_THI);
        private final JLabel lblDem = UIStyle.nhan("0 khách hàng");

        private final javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(COLS, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                        return false;
                }
        };

        private final JTable table = new JTable(model);

        // dòng i của bảng <-> dsHienThi.get(i)
        private ArrayList<KhachHang> dsHienThi = new ArrayList<>();

        // ----- khung thông tin chi tiết (hiện khi chọn 1 dòng) -----
        private final CardLayout cardCT = new CardLayout();
        private final JPanel khungCT = new JPanel(cardCT);

        private final JLabel vMa = giaTri();
        private final JLabel vTen = giaTri();
        private final JLabel vSdt = giaTri();
        private final JLabel vEmail = giaTri();
        private final JLabel vDiaChi = giaTri();
        private final JLabel vHang = giaTri();
        private final JLabel vDiem = giaTri();
        private final JLabel vTong = giaTri();
        private final JLabel vThuCung = new JLabel();

        // maNhanVien: nhân viên đang đăng nhập (ghi vào thẻ thành viên khi tạo thẻ)
        public QuanLyKhachHang(String maNhanVien) {

                this.maNhanVien = maNhanVien;

                setLayout(new BorderLayout(0, 14));
                setBackground(UIStyle.LIGHT_BLUE);
                setBorder(new EmptyBorder(18, 24, 18, 24));

                // ---------------- tiêu đề + nút Thêm ----------------
                JLabel tieuDe = new JLabel("Quản lý khách hàng");

                tieuDe.setFont(UIStyle.font(Font.BOLD, 26));
                tieuDe.setForeground(new Color(40, 40, 40));

                JButton btnThem = UIStyle.nutXanh("+  Thêm khách hàng");

                btnThem.addActionListener(e -> moDialog(null));

                JPanel dau = new JPanel(new BorderLayout());

                dau.setOpaque(false);
                dau.add(tieuDe, BorderLayout.WEST);
                dau.add(btnThem, BorderLayout.EAST);

                // ---------------- hàng tìm kiếm + nút ----------------
                cboHang.setFont(UIStyle.font(Font.PLAIN, 13));

                JButton btnTim = UIStyle.nutChinh("Tìm kiếm");
                JButton btnSua = UIStyle.nutPhu("Sửa");
                JButton btnXoa = UIStyle.nutXoa("Xóa");
                JButton btnLamMoi = UIStyle.nutPhu("Làm mới");

                btnTim.addActionListener(e -> taiLai(maDangChon()));
                txtTim.addActionListener(e -> taiLai(maDangChon()));
                cboHang.addActionListener(e -> taiLai(maDangChon()));
                btnSua.addActionListener(e -> sua());
                btnXoa.addActionListener(e -> xoa());
                btnLamMoi.addActionListener(e -> lamMoiTatCa());

                JPanel loc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

                loc.setOpaque(false);
                loc.add(UIStyle.nhan("Mã / tên / SĐT:"));
                loc.add(txtTim);
                loc.add(cboHang);
                loc.add(btnTim);
                loc.add(btnSua);
                loc.add(btnXoa);
                loc.add(btnLamMoi);

                JPanel hangTren = new JPanel(new BorderLayout(0, 8));

                hangTren.setOpaque(false);
                hangTren.add(loc, BorderLayout.NORTH);
                hangTren.add(lblDem, BorderLayout.SOUTH);

                // ---------------- bảng ----------------
                UIStyle.taoBang(table, 6, 7);

                int[] w = { 70, 160, 110, 170, 230, 80, 80, 120, 100 };

                for (int i = 0; i < w.length; i++) {
                        table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
                }

                table.getSelectionModel().addListSelectionListener(e -> {
                        if (!e.getValueIsAdjusting()) {
                                hienThongTin();
                        }
                });

                table.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                                if (e.getClickCount() == 2 && dongDangChon() != null) {
                                        sua();
                                }
                        }
                });

                // ---------------- khung thông tin chi tiết ----------------
                khungCT.setOpaque(false);

                JLabel goiY = new JLabel("Chọn một khách hàng trong bảng để xem thông tin chi tiết.");

                goiY.setFont(UIStyle.font(Font.PLAIN, 13));
                goiY.setForeground(Color.GRAY);

                JPanel trong = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));

                trong.setOpaque(false);
                trong.add(goiY);

                khungCT.add(trong, "TRONG");
                khungCT.add(taoNoiDungChiTiet(), "CO");
                khungCT.setPreferredSize(new Dimension(100, 150));

                JPanel chiTiet = new JPanel(new BorderLayout(0, 6));

                chiTiet.setOpaque(false);
                chiTiet.setBorder(new javax.swing.border.MatteBorder(1, 0, 0, 0, UIStyle.LINE));

                JLabel tdCT = UIStyle.nhan("Thông tin khách hàng");

                tdCT.setBorder(new EmptyBorder(8, 0, 0, 0));

                chiTiet.add(tdCT, BorderLayout.NORTH);
                chiTiet.add(khungCT, BorderLayout.CENTER);

                // ---------------- thẻ trắng chứa tất cả ----------------
                JPanel the = UIStyle.the();

                the.setLayout(new BorderLayout(0, 10));

                the.add(hangTren, BorderLayout.NORTH);
                the.add(UIStyle.cuon(table), BorderLayout.CENTER);
                the.add(chiTiet, BorderLayout.SOUTH);

                add(dau, BorderLayout.NORTH);
                add(the, BorderLayout.CENTER);

                taiLai(null);
        }

        // =========================================================
        // GIAO DIỆN PHỤ
        // =========================================================

        private static JLabel giaTri() {

                JLabel l = new JLabel("-");

                l.setFont(UIStyle.font(Font.BOLD, 13));

                return l;
        }

        private static JPanel cap(String tieuDe, JLabel gt) {

                JPanel p = new JPanel();

                p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                p.setOpaque(false);

                JLabel l = new JLabel(tieuDe);

                l.setFont(UIStyle.font(Font.PLAIN, 12));
                l.setForeground(Color.GRAY);
                l.setAlignmentX(Component.LEFT_ALIGNMENT);

                gt.setAlignmentX(Component.LEFT_ALIGNMENT);

                p.add(l);
                p.add(Box.createVerticalStrut(2));
                p.add(gt);

                return p;
        }

        private JPanel taoNoiDungChiTiet() {

                JPanel luoi = new JPanel(new GridLayout(2, 4, 18, 8));

                luoi.setOpaque(false);

                luoi.add(cap("Mã khách hàng", vMa));
                luoi.add(cap("Họ tên", vTen));
                luoi.add(cap("Số điện thoại", vSdt));
                luoi.add(cap("Email", vEmail));
                luoi.add(cap("Địa chỉ", vDiaChi));
                luoi.add(cap("Hạng thành viên", vHang));
                luoi.add(cap("Điểm tích lũy", vDiem));
                luoi.add(cap("Tổng chi / mua gần nhất", vTong));

                vThuCung.setFont(UIStyle.font(Font.PLAIN, 13));

                JPanel pets = new JPanel(new BorderLayout(0, 2));

                pets.setOpaque(false);

                JLabel l = new JLabel("Thú cưng");

                l.setFont(UIStyle.font(Font.PLAIN, 12));
                l.setForeground(Color.GRAY);

                pets.add(l, BorderLayout.NORTH);
                pets.add(vThuCung, BorderLayout.CENTER);

                JPanel p = new JPanel(new BorderLayout(0, 8));

                p.setOpaque(false);
                p.add(luoi, BorderLayout.NORTH);
                p.add(pets, BorderLayout.CENTER);

                return p;
        }

        // =========================================================
        // DỮ LIỆU
        // =========================================================

        private static String so(long v) {

                return String.format(Locale.US, "%,d", v).replace(',', '.');
        }

        private KhachHang dongDangChon() {

                int row = table.getSelectedRow();

                if (row < 0 || row >= dsHienThi.size()) {
                        return null;
                }

                return dsHienThi.get(row);
        }

        private String maDangChon() {

                KhachHang k = dongDangChon();

                return k == null ? null : k.getMaKhachHang();
        }

        // Gọi khi mở màn hình (TrangChu): nạp lại dữ liệu, giữ dòng đang chọn
        public void lamMoi() {

                taiLai(maDangChon());
        }

        // Nút "Làm mới": xóa bộ lọc, bỏ chọn, nạp lại từ CSDL
        private void lamMoiTatCa() {

                txtTim.setText("");

                cboHang.setSelectedIndex(0);

                table.clearSelection();

                taiLai(null);
        }

        // Nạp bảng theo bộ lọc; chonMa != null thì chọn lại dòng đó nếu còn
        public void taiLai(String chonMa) {

                dsHienThi = khachHangDAO.timKiem(txtTim.getText(), LOC_DAO[cboHang.getSelectedIndex()]);

                table.clearSelection();

                model.setRowCount(0);

                for (KhachHang k : dsHienThi) {

                        model.addRow(new Object[] {
                                        k.getMaKhachHang(),
                                        k.getHoTen(),
                                        k.getSoDienThoai(),
                                        k.getEmail() == null ? "" : k.getEmail(),
                                        k.getThuCung() == null ? "" : k.getThuCung(),
                                        k.getHangThe() == null ? "-" : k.getHangThe(),
                                        so(k.getDiemTichLuy()),
                                        UIStyle.tien(k.getTongChi()),
                                        k.getGanNhat() == null ? "" : k.getGanNhat() });
                }

                lblDem.setText(dsHienThi.size() + " khách hàng");

                if (chonMa != null) {

                        for (int i = 0; i < dsHienThi.size(); i++) {

                                if (dsHienThi.get(i).getMaKhachHang().equals(chonMa)) {

                                        table.setRowSelectionInterval(i, i);

                                        table.scrollRectToVisible(table.getCellRect(i, 0, true));

                                        break;
                                }
                        }
                }

                hienThongTin();
        }

        // Hiện thông tin của dòng đang chọn ở khung bên dưới bảng
        private void hienThongTin() {

                KhachHang k = dongDangChon();

                if (k == null) {

                        cardCT.show(khungCT, "TRONG");

                        return;
                }

                vMa.setText(k.getMaKhachHang());
                vTen.setText(k.getHoTen());
                vSdt.setText(k.getSoDienThoai());
                vEmail.setText(k.getEmail() == null || k.getEmail().isEmpty() ? "-" : k.getEmail());
                vDiaChi.setText(k.getDiaChi() == null || k.getDiaChi().isEmpty() ? "-" : k.getDiaChi());
                vHang.setText(k.getHangThe() == null ? "Không có thẻ" : k.getHangThe());
                vDiem.setText(so(k.getDiemTichLuy()));
                vTong.setText(UIStyle.tien(k.getTongChi())
                                + (k.getGanNhat() == null ? "" : "  /  " + k.getGanNhat()));

                StringBuilder sb = new StringBuilder("<html><body style='width:760px'>");

                ArrayList<String[]> pets = chiTietDAO.getThuCung(k.getMaKhachHang());

                if (pets.isEmpty()) {

                        sb.append("Chưa có thú cưng nào.");

                } else {

                        for (int i = 0; i < pets.size(); i++) {

                                String[] p = pets.get(i);

                                if (i > 0) {
                                        sb.append("; ");
                                }

                                sb.append("<b>").append(p[0]).append("</b> (").append(p[1] == null ? "" : p[1]);

                                if (p[2] != null && !p[2].isEmpty()) {
                                        sb.append(", ").append(p[2]);
                                }

                                if (p[3] != null && !p[3].isEmpty()) {
                                        sb.append(", ").append(p[3]);
                                }

                                if (!p[4].isEmpty()) {
                                        sb.append(", ").append(p[4]).append(" kg");
                                }

                                sb.append(")");
                        }
                }

                vThuCung.setText(sb.append("</body></html>").toString());

                cardCT.show(khungCT, "CO");
        }

        // =========================================================
        // THÊM / SỬA / XÓA
        // =========================================================

        private void sua() {

                KhachHang k = dongDangChon();

                if (k == null) {

                        UIStyle.canhBao(this, "Hãy chọn một khách hàng trong bảng để sửa.");

                        return;
                }

                moDialog(k);
        }

        private void xoa() {

                KhachHang k = dongDangChon();

                if (k == null) {

                        UIStyle.canhBao(this, "Hãy chọn một khách hàng trong bảng để xóa.");

                        return;
                }

                if (!UIStyle.xacNhan(this, "Xóa khách hàng " + k.getMaKhachHang() + " - " + k.getHoTen() + "?\n"
                                + "(Cũng xóa thẻ thành viên và thú cưng của khách này)")) {
                        return;
                }

                String loi = khachHangDAO.xoa(k.getMaKhachHang());

                if (loi != null) {

                        UIStyle.canhBao(this, loi);

                        return;
                }

                taiLai(null);
        }

        // goc = null -> thêm mới; goc != null -> sửa khách đó
        private void moDialog(KhachHang goc) {

                KhachHangDialog dlg = new KhachHangDialog(
                                SwingUtilities.getWindowAncestor(this),
                                maNhanVien, goc, khachHangDAO, chiTietDAO);

                dlg.setVisible(true);

                if (dlg.isDaLuu()) {
                        taiLai(dlg.getMaDaLuu());
                }
        }

        // =========================================================
        // HỘP THOẠI THÊM / SỬA KHÁCH HÀNG (class lồng)
        // =========================================================

        public static class KhachHangDialog extends JDialog {

                // Giá trị lưu vào CSDL (dùng Unicode escape cho chắc chắn)
                private static final String[] LOAI = { "Ch\u00f3", "M\u00e8o", "Kh\u00e1c" };
                private static final String[] GIOI_TINH = { "\u0110\u1ef1c", "C\u00e1i" };

                private static final Color VIEN = new Color(110, 130, 160);
                private static final Color XAM = new Color(110, 120, 135);
                private static final Font F14 = new Font("Segoe UI", Font.PLAIN, 14);
                private static final Font NHAN = new Font("Segoe UI", Font.BOLD, 12);

                private final KhachHangDAO khachHangDAO;
                private final KhachHangChiTietDAO chiTietDAO;

                private final String maNhanVien;
                private final KhachHang goc;

                private boolean daLuu = false;
                private String maDaLuu = null;

                private final JTextField txtTen = new JTextField();
                private final JTextField txtSdt = new JTextField();
                private final JTextField txtEmail = new JTextField();
                private final JTextField txtDiaChi = new JTextField();
                private final JComboBox<String> cboHangThe;
                private final JSpinner spnDiem = new JSpinner(new SpinnerNumberModel(0, 0, 1000000, 10));

                private final JPanel petsBox = new JPanel();
                private final ArrayList<PetRow> rows = new ArrayList<>();

                public KhachHangDialog(Window owner, String maNhanVien, KhachHang goc,
                                KhachHangDAO khachHangDAO, KhachHangChiTietDAO chiTietDAO) {

                        super(owner, goc == null ? "Thêm Khách Hàng" : "Sửa Khách Hàng",
                                        Dialog.ModalityType.APPLICATION_MODAL);

                        this.maNhanVien = maNhanVien;
                        this.goc = goc;
                        this.khachHangDAO = khachHangDAO;
                        this.chiTietDAO = chiTietDAO;

                        // Khách đã có thẻ: chỉ đổi giữa các hạng; chưa có thẻ: thêm lựa chọn "Không có thẻ"
                        if (goc != null && goc.getHangThe() != null) {
                                cboHangThe = new JComboBox<>(new String[] { "Silver", "Gold", "Platinum" });
                                cboHangThe.setSelectedItem(goc.getHangThe());
                        } else {
                                cboHangThe = new JComboBox<>(new String[] { "Không có thẻ", "Silver", "Gold", "Platinum" });
                        }

                        setSize(760, goc == null ? 640 : 440);
                        setResizable(false);
                        setLocationRelativeTo(owner);

                        kieu(txtTen);
                        kieu(txtSdt);
                        kieu(txtEmail);
                        kieu(txtDiaChi);

                        kieuCbo(cboHangThe);

                        spnDiem.setPreferredSize(new Dimension(100, 36));
                        ((JSpinner.DefaultEditor) spnDiem.getEditor()).getTextField().setFont(F14);

                        JPanel body = new JPanel();

                        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
                        body.setOpaque(false);
                        body.setBorder(new EmptyBorder(24, 40, 10, 40));

                        them(body, muc(goc == null ? "Thông tin Khách hàng"
                                        : "Thông tin Khách hàng (" + goc.getMaKhachHang() + ")"));
                        them(body, Box.createVerticalStrut(8));
                        them(body, hai(cot("Họ và tên: *", txtTen), cot("Số điện thoại: *", txtSdt)));
                        them(body, Box.createVerticalStrut(8));
                        them(body, hai(cot("Email:", txtEmail), cot("Địa chỉ:", txtDiaChi)));
                        them(body, Box.createVerticalStrut(14));
                        them(body, muc("Hạng thành viên"));
                        them(body, Box.createVerticalStrut(8));
                        them(body, hai(cot("Cấp thẻ:", cboHangThe),
                                        cot(goc == null ? "Điểm khởi tạo:" : "Điểm tích lũy:", spnDiem)));

                        // Thú cưng: chỉ nhập khi thêm mới
                        if (goc == null) {

                                petsBox.setLayout(new BoxLayout(petsBox, BoxLayout.Y_AXIS));
                                petsBox.setOpaque(false);

                                JPanel petsWrap = new JPanel(new BorderLayout());

                                petsWrap.setBackground(Color.WHITE);
                                petsWrap.add(petsBox, BorderLayout.NORTH);

                                JScrollPane sp = new JScrollPane(petsWrap);

                                sp.setBorder(new LineBorder(new Color(220, 226, 235)));
                                sp.setPreferredSize(new Dimension(100, 130));
                                sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
                                sp.getVerticalScrollBar().setUnitIncrement(16);

                                JButton btnThemTc = nut("+ Thêm thú cưng", new Color(15, 42, 68));

                                btnThemTc.addActionListener(e -> themDongThuCung());

                                them(body, Box.createVerticalStrut(14));
                                them(body, muc("Thú cưng"));
                                them(body, Box.createVerticalStrut(6));
                                them(body, sp);
                                them(body, Box.createVerticalStrut(6));
                                them(body, btnThemTc);
                        }

                        JPanel top = new JPanel(new BorderLayout());

                        top.setOpaque(false);
                        top.add(body, BorderLayout.NORTH);

                        // ---------------- nút ----------------
                        JButton btnHuy = nut("Hủy", new Color(138, 144, 153));
                        JButton btnLuu = nut(goc == null ? "Thêm mới" : "Lưu thay đổi", new Color(15, 42, 68));

                        btnHuy.addActionListener(e -> dispose());
                        btnLuu.addActionListener(e -> luu());

                        JPanel hangNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

                        hangNut.setOpaque(false);
                        hangNut.setBorder(new EmptyBorder(6, 40, 24, 40));
                        hangNut.add(btnHuy);
                        hangNut.add(btnLuu);

                        JPanel root = new JPanel(new BorderLayout());

                        root.setBackground(Color.WHITE);
                        root.add(top, BorderLayout.CENTER);
                        root.add(hangNut, BorderLayout.SOUTH);

                        setContentPane(root);

                        // ---- đổ dữ liệu khi sửa ----
                        if (goc != null) {

                                txtTen.setText(goc.getHoTen());
                                txtSdt.setText(goc.getSoDienThoai());
                                txtEmail.setText(goc.getEmail() == null ? "" : goc.getEmail());
                                txtDiaChi.setText(goc.getDiaChi() == null ? "" : goc.getDiaChi());
                                spnDiem.setValue(goc.getDiemTichLuy());
                        }
                }

                public boolean isDaLuu() {

                        return daLuu;
                }

                public String getMaDaLuu() {

                        return maDaLuu;
                }

                // ---------------- thành phần giao diện ----------------

                private static JPanel cot(String nhan, JComponent c) {

                        JPanel p = new JPanel(new BorderLayout(0, 4));

                        p.setOpaque(false);

                        JLabel l = new JLabel(nhan);

                        l.setFont(NHAN);
                        l.setForeground(XAM);

                        p.add(l, BorderLayout.NORTH);
                        p.add(c, BorderLayout.CENTER);

                        return p;
                }

                private static JPanel hai(JComponent a, JComponent b) {

                        JPanel p = new JPanel(new GridLayout(1, 2, 20, 0));

                        p.setOpaque(false);

                        p.add(a);
                        p.add(b);

                        return p;
                }

                private static JLabel muc(String text) {

                        JLabel l = new JLabel(text);

                        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
                        l.setForeground(new Color(20, 30, 45));

                        return l;
                }

                private static void kieu(JTextField t) {

                        t.setFont(F14);

                        t.setBorder(new CompoundBorder(
                                        new LineBorder(VIEN, 1),
                                        new EmptyBorder(6, 10, 6, 10)));

                        t.setPreferredSize(new Dimension(100, 36));
                }

                private static void kieuCbo(JComboBox<?> c) {

                        c.setFont(F14);
                        c.setBackground(Color.WHITE);
                        c.setPreferredSize(new Dimension(100, 36));
                }

                private static JButton nut(String text, Color nen) {

                        JButton b = new JButton(text);

                        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        b.setForeground(Color.WHITE);
                        b.setBackground(nen);
                        b.setOpaque(true);
                        b.setBorderPainted(false);
                        b.setFocusPainted(false);
                        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        b.setPreferredSize(new Dimension(Math.max(110, b.getPreferredSize().width + 10), 38));

                        return b;
                }

                private static void them(JPanel p, Component c) {

                        if (c instanceof JComponent) {
                                ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
                        }

                        p.add(c);
                }

                // ---------------- dòng thú cưng (chỉ khi thêm mới) ----------------

                private class PetRow extends JPanel {

                        final JTextField ten = new JTextField();
                        final JTextField giong = new JTextField();
                        final JTextField canNang = new JTextField();
                        final JComboBox<String> loai = new JComboBox<>(LOAI);
                        final JComboBox<String> gioiTinh = new JComboBox<>(GIOI_TINH);

                        PetRow() {

                                super(new GridLayout(1, 6, 8, 0));

                                setOpaque(false);

                                setAlignmentX(Component.LEFT_ALIGNMENT);

                                setBorder(new EmptyBorder(4, 6, 4, 6));

                                kieu(ten);
                                kieu(giong);
                                kieu(canNang);

                                kieuCbo(loai);
                                kieuCbo(gioiTinh);

                                JButton xoa = nut("Xóa", new Color(210, 70, 70));

                                xoa.setPreferredSize(new Dimension(60, 36));

                                xoa.addActionListener(e -> {
                                        petsBox.remove(this);
                                        rows.remove(this);
                                        petsBox.revalidate();
                                        petsBox.repaint();
                                });

                                JPanel xp = new JPanel(new BorderLayout(0, 4));

                                xp.setOpaque(false);

                                xp.add(new JLabel(" "), BorderLayout.NORTH);
                                xp.add(xoa, BorderLayout.CENTER);

                                add(cot("Tên: *", ten));
                                add(cot("Loại:", loai));
                                add(cot("Giống:", giong));
                                add(cot("Giới tính:", gioiTinh));
                                add(cot("Cân nặng (kg):", canNang));
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

                // ---------------- lưu ----------------

                private void loi(String msg) {

                        JOptionPane.showMessageDialog(this, msg, "Dữ liệu chưa hợp lệ",
                                        JOptionPane.WARNING_MESSAGE);
                }

                private void luu() {

                        String ten = txtTen.getText().trim();
                        String sdt = txtSdt.getText().replaceAll("\\s", "");
                        String email = txtEmail.getText().trim();
                        String diaChi = txtDiaChi.getText().trim();

                        if (ten.isEmpty() || ten.length() > 50) {
                                loi("Họ tên bắt buộc nhập (tối đa 50 ký tự).");
                                return;
                        }

                        if (!sdt.matches("\\d{10,11}")) {
                                loi("Số điện thoại phải gồm 10 hoặc 11 chữ số.");
                                return;
                        }

                        if (!email.isEmpty() && (!email.contains("@") || email.length() > 50)) {
                                loi("Email không hợp lệ (tối đa 50 ký tự).");
                                return;
                        }

                        if (diaChi.length() > 50) {
                                loi("Địa chỉ tối đa 50 ký tự.");
                                return;
                        }

                        boolean trung = goc == null
                                        ? khachHangDAO.tonTaiSdt(sdt)
                                        : chiTietDAO.tonTaiSdtKhac(sdt, goc.getMaKhachHang());

                        if (trung) {
                                loi("Số điện thoại " + sdt + " đã thuộc về khách hàng khác.");
                                return;
                        }

                        String hangChon = (String) cboHangThe.getSelectedItem();

                        String hang = hangChon.equals("Không có thẻ") ? null : hangChon;

                        KhachHang kh = new KhachHang(goc == null ? null : goc.getMaKhachHang(), ten, sdt,
                                        email.isEmpty() ? null : email,
                                        diaChi.isEmpty() ? null : diaChi,
                                        (Integer) spnDiem.getValue());

                        // ---------- SỬA ----------
                        if (goc != null) {

                                // chỉ đổi hạng khi người dùng chọn hạng khác hạng hiện tại
                                String hangMoi = (hang != null && !hang.equals(goc.getHangThe())) ? hang : null;

                                if (!chiTietDAO.sua(kh, hangMoi, maNhanVien)) {
                                        loi("Lưu thất bại. Xem lỗi chi tiết trong Console.");
                                        return;
                                }

                                JOptionPane.showMessageDialog(this, "Đã cập nhật khách hàng " + goc.getMaKhachHang() + ".");

                                daLuu = true;
                                maDaLuu = goc.getMaKhachHang();

                                dispose();

                                return;
                        }

                        // ---------- THÊM ----------
                        ArrayList<KhachHangDAO.ThuCungMoi> pets = new ArrayList<>();

                        for (PetRow r : rows) {

                                String tenTC = r.ten.getText().trim();
                                String giong = r.giong.getText().trim();

                                if (tenTC.isEmpty() || tenTC.length() > 50) {
                                        loi("Mỗi thú cưng cần có tên (bấm Xóa nếu không dùng).");
                                        return;
                                }

                                if (giong.length() > 25) {
                                        loi("Giống thú cưng tối đa 25 ký tự.");
                                        return;
                                }

                                Double kg = null;

                                String s = r.canNang.getText().trim().replace(',', '.');

                                if (!s.isEmpty()) {

                                        try {
                                                kg = Double.parseDouble(s);
                                        } catch (NumberFormatException ex) {
                                                loi("Cân nặng phải là số, ví dụ 4.5");
                                                return;
                                        }

                                        if (kg < 0 || kg >= 1000) {
                                                loi("Cân nặng phải nhỏ hơn 1000 kg.");
                                                return;
                                        }
                                }

                                pets.add(new KhachHangDAO.ThuCungMoi(tenTC, (String) r.loai.getSelectedItem(),
                                                giong, (String) r.gioiTinh.getSelectedItem(), kg));
                        }

                        String ma = khachHangDAO.them(kh, pets, hang, maNhanVien);

                        if (ma == null) {
                                loi("Lưu thất bại. Xem lỗi chi tiết trong Console.");
                                return;
                        }

                        JOptionPane.showMessageDialog(this, "Đã thêm khách hàng " + ma + ".");

                        daLuu = true;
                        maDaLuu = ma;

                        dispose();
                }
        }
}
