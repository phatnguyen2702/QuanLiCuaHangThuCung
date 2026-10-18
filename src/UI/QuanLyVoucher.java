package UI;

import UI.TrangChu.UIStyle;
import dao.VoucherDAO;
import entity.Voucher;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Locale;

// Man Ma giam gia va Voucher: tim kiem, tao / sua / xoa, kich hoat / huy, lam moi,
// chon 1 dong de xem thong tin voucher
public class QuanLyVoucher extends JPanel {

        private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        private static final String[] COLS = { "Mã", "Tên chương trình", "Điều kiện",
                        "Mức giảm", "Đã dùng", "Hiệu lực", "Trạng thái" };

        private static final String[] LOC_HIEN_THI = { "Tất cả trạng thái", "Đang chạy", "Sắp chạy",
                        "Hết hạn", "Hết lượt", "Đã hủy" };

        // mã trạng thái do Voucher.getTrangThaiText() trả về
        private static final String[] LOC_MA = { "Tat ca", "Dang chay", "Sap chay",
                        "Het han", "Het luot", "Da huy" };

        private final VoucherDAO voucherDAO = new VoucherDAO();

        private final JTextField txtTim = UIStyle.o(16);
        private final JComboBox<String> cboTt = new JComboBox<>(LOC_HIEN_THI);
        private final JLabel lblDem = UIStyle.nhan("0 voucher");

        private final JButton btnKichHoat = UIStyle.nutPhu("Kích hoạt / Hủy");

        private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                        return false;
                }
        };

        private final JTable table = new JTable(model);

        // dòng i của bảng <-> dsHienThi.get(i)
        private ArrayList<Voucher> dsHienThi = new ArrayList<>();

        // ----- khung thông tin chi tiết (hiện khi chọn 1 dòng) -----
        private final CardLayout cardCT = new CardLayout();
        private final JPanel khungCT = new JPanel(cardCT);

        private final JLabel vMa = giaTri();
        private final JLabel vTen = giaTri();
        private final JLabel vGiam = giaTri();
        private final JLabel vTrangThai = giaTri();
        private final JLabel vTu = giaTri();
        private final JLabel vDen = giaTri();
        private final JLabel vDaDung = giaTri();
        private final JLabel vConLai = giaTri();
        private final JLabel vDieuKien = new JLabel();

        public QuanLyVoucher() {

                setLayout(new BorderLayout(0, 14));
                setBackground(UIStyle.LIGHT_BLUE);
                setBorder(new EmptyBorder(18, 24, 18, 24));

                // ---------------- tiêu đề + nút Tạo ----------------
                JLabel tieuDe = new JLabel("Mã giảm giá và Voucher");

                tieuDe.setFont(UIStyle.font(Font.BOLD, 26));
                tieuDe.setForeground(new Color(40, 40, 40));

                JButton btnTao = UIStyle.nutXanh("+  Tạo voucher");

                btnTao.addActionListener(e -> moDialog(null));

                JPanel dau = new JPanel(new BorderLayout());

                dau.setOpaque(false);
                dau.add(tieuDe, BorderLayout.WEST);
                dau.add(btnTao, BorderLayout.EAST);

                // ---------------- hàng tìm kiếm + nút ----------------
                cboTt.setFont(UIStyle.font(Font.PLAIN, 13));

                JButton btnTim = UIStyle.nutChinh("Tìm kiếm");
                JButton btnSua = UIStyle.nutPhu("Sửa");
                JButton btnXoa = UIStyle.nutXoa("Xóa");
                JButton btnLamMoi = UIStyle.nutPhu("Làm mới");

                btnTim.addActionListener(e -> taiLai(maDangChon()));
                txtTim.addActionListener(e -> taiLai(maDangChon()));
                cboTt.addActionListener(e -> taiLai(maDangChon()));
                btnSua.addActionListener(e -> sua());
                btnXoa.addActionListener(e -> xoa());
                btnKichHoat.addActionListener(e -> kichHoatHuy());
                btnLamMoi.addActionListener(e -> lamMoiTatCa());

                JPanel loc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

                loc.setOpaque(false);
                loc.add(UIStyle.nhan("Mã / tên:"));
                loc.add(txtTim);
                loc.add(cboTt);
                loc.add(btnTim);
                loc.add(btnSua);
                loc.add(btnXoa);
                loc.add(btnKichHoat);
                loc.add(btnLamMoi);

                JPanel hangTren = new JPanel(new BorderLayout(0, 8));

                hangTren.setOpaque(false);
                hangTren.add(loc, BorderLayout.NORTH);
                hangTren.add(lblDem, BorderLayout.SOUTH);

                // ---------------- bảng ----------------
                UIStyle.taoBang(table, 3, 4);

                table.getColumnModel().getColumn(6).setCellRenderer(new TrangThaiRenderer());

                int[] w = { 60, 170, 230, 80, 80, 190, 110 };

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

                JLabel goiY = new JLabel("Chọn một voucher trong bảng để xem thông tin chi tiết.");

                goiY.setFont(UIStyle.font(Font.PLAIN, 13));
                goiY.setForeground(Color.GRAY);

                JPanel trong = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));

                trong.setOpaque(false);
                trong.add(goiY);

                khungCT.add(trong, "TRONG");
                khungCT.add(taoNoiDungChiTiet(), "CO");
                khungCT.setPreferredSize(new Dimension(100, 130));

                JPanel chiTiet = new JPanel(new BorderLayout(0, 6));

                chiTiet.setOpaque(false);
                chiTiet.setBorder(new javax.swing.border.MatteBorder(1, 0, 0, 0, UIStyle.LINE));

                JLabel tdCT = UIStyle.nhan("Thông tin voucher");

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

                luoi.add(cap("Mã voucher", vMa));
                luoi.add(cap("Tên chương trình", vTen));
                luoi.add(cap("Mức giảm", vGiam));
                luoi.add(cap("Trạng thái", vTrangThai));
                luoi.add(cap("Từ ngày", vTu));
                luoi.add(cap("Đến ngày", vDen));
                luoi.add(cap("Đã dùng", vDaDung));
                luoi.add(cap("Còn lại", vConLai));

                vDieuKien.setFont(UIStyle.font(Font.PLAIN, 13));

                JPanel dk = new JPanel(new BorderLayout(0, 2));

                dk.setOpaque(false);

                JLabel l = new JLabel("Điều kiện áp dụng");

                l.setFont(UIStyle.font(Font.PLAIN, 12));
                l.setForeground(Color.GRAY);

                dk.add(l, BorderLayout.NORTH);
                dk.add(vDieuKien, BorderLayout.CENTER);

                JPanel p = new JPanel(new BorderLayout(0, 8));

                p.setOpaque(false);
                p.add(luoi, BorderLayout.NORTH);
                p.add(dk, BorderLayout.CENTER);

                return p;
        }

        // Tô màu ô trạng thái
        private static class TrangThaiRenderer extends DefaultTableCellRenderer {

                @Override
                public Component getTableCellRendererComponent(
                                JTable t, Object v, boolean sel, boolean foc, int r, int c) {

                        JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, r, c);

                        Color nen;
                        Color chu;

                        switch (String.valueOf(v)) {
                                case "Đang chạy":
                                        nen = new Color(217, 245, 229);
                                        chu = new Color(12, 107, 64);
                                        break;
                                case "Sắp chạy":
                                        nen = new Color(230, 232, 236);
                                        chu = new Color(51, 51, 51);
                                        break;
                                case "Hết hạn":
                                case "Hết lượt":
                                        nen = new Color(255, 240, 205);
                                        chu = new Color(146, 90, 0);
                                        break;
                                case "Đã hủy":
                                        nen = new Color(255, 217, 217);
                                        chu = new Color(180, 35, 24);
                                        break;
                                default:
                                        nen = Color.WHITE;
                                        chu = Color.BLACK;
                        }

                        l.setOpaque(true);
                        l.setBackground(nen);
                        l.setForeground(chu);
                        l.setHorizontalAlignment(SwingConstants.CENTER);

                        return l;
                }
        }

        // =========================================================
        // DỮ LIỆU
        // =========================================================

        private static String phanTram(double v) {

                return (v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.US, "%.2f", v)) + "%";
        }

        // đổi mã trạng thái (không dấu) sang chữ hiển thị
        private static String tenTrangThai(String ma) {

                switch (ma) {
                        case "Dang chay":
                                return "Đang chạy";
                        case "Sap chay":
                                return "Sắp chạy";
                        case "Het han":
                                return "Hết hạn";
                        case "Het luot":
                                return "Hết lượt";
                        default:
                                return "Đã hủy";
                }
        }

        private static String hieuLuc(Voucher v) {

                LocalDate bd = v.getNgayBatDau();
                LocalDate kt = v.getNgayKetThuc();

                if (bd == null && kt == null) {
                        return "Không giới hạn";
                }

                if (bd == null) {
                        return "Đến " + kt.format(D);
                }

                if (kt == null) {
                        return "Từ " + bd.format(D);
                }

                return bd.format(D) + " - " + kt.format(D);
        }

        private Voucher dongDangChon() {

                int row = table.getSelectedRow();

                if (row < 0 || row >= dsHienThi.size()) {
                        return null;
                }

                return dsHienThi.get(row);
        }

        private String maDangChon() {

                Voucher v = dongDangChon();

                return v == null ? null : v.getMaVoucher();
        }

        // Gọi khi mở màn hình (TrangChu): nạp lại dữ liệu, giữ dòng đang chọn
        public void lamMoi() {

                taiLai(maDangChon());
        }

        // Nút "Làm mới": xóa bộ lọc, bỏ chọn, nạp lại từ CSDL
        private void lamMoiTatCa() {

                txtTim.setText("");

                cboTt.setSelectedIndex(0);

                table.clearSelection();

                taiLai(null);
        }

        // Nạp bảng theo bộ lọc; chonMa != null thì chọn lại dòng đó nếu còn
        public void taiLai(String chonMa) {

                String loc = LOC_MA[cboTt.getSelectedIndex()];

                ArrayList<Voucher> tatCa = voucherDAO.getAll(txtTim.getText());

                dsHienThi = new ArrayList<>();

                table.clearSelection();

                model.setRowCount(0);

                for (Voucher v : tatCa) {

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
                                        tenTrangThai(tt) });
                }

                lblDem.setText(dsHienThi.size() + " voucher");

                if (chonMa != null) {

                        for (int i = 0; i < dsHienThi.size(); i++) {

                                if (dsHienThi.get(i).getMaVoucher().equals(chonMa)) {

                                        table.setRowSelectionInterval(i, i);

                                        table.scrollRectToVisible(table.getCellRect(i, 0, true));

                                        break;
                                }
                        }
                }

                hienThongTin();
        }

        // Hiện thông tin của dòng đang chọn ở khung bên dưới bảng + đổi chữ nút Kích hoạt / Hủy
        private void hienThongTin() {

                Voucher v = dongDangChon();

                if (v == null) {

                        btnKichHoat.setText("Kích hoạt / Hủy");

                        cardCT.show(khungCT, "TRONG");

                        return;
                }

                btnKichHoat.setText(v.isTrangThai() ? "Hủy voucher" : "Kích hoạt");

                vMa.setText(v.getMaVoucher());
                vTen.setText(v.getTenVoucher());
                vGiam.setText(phanTram(v.getPhanTramGiam()));
                vTrangThai.setText(tenTrangThai(v.getTrangThaiText()));
                vTu.setText(v.getNgayBatDau() == null ? "Không giới hạn" : v.getNgayBatDau().format(D));
                vDen.setText(v.getNgayKetThuc() == null ? "Không giới hạn" : v.getNgayKetThuc().format(D));
                vDaDung.setText(v.getDaDung() + " lượt");

                vConLai.setText(v.getSoLuongToiDa() == null
                                ? "Không giới hạn"
                                : Math.max(0, v.getSoLuongToiDa() - v.getDaDung()) + " lượt");

                String dk = v.getDieuKien() == null || v.getDieuKien().trim().isEmpty()
                                ? "Không có điều kiện."
                                : v.getDieuKien();

                vDieuKien.setText("<html><body style='width:760px'>" + dk
                                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                                + "</body></html>");

                cardCT.show(khungCT, "CO");
        }

        // =========================================================
        // TẠO / SỬA / XÓA / KÍCH HOẠT
        // =========================================================

        private void sua() {

                Voucher v = dongDangChon();

                if (v == null) {

                        UIStyle.canhBao(this, "Hãy chọn một voucher trong bảng để sửa.");

                        return;
                }

                moDialog(v);
        }

        private void xoa() {

                Voucher v = dongDangChon();

                if (v == null) {

                        UIStyle.canhBao(this, "Hãy chọn một voucher trong bảng để xóa.");

                        return;
                }

                if (!UIStyle.xacNhan(this, "Xóa hẳn voucher " + v.getMaVoucher() + " - " + v.getTenVoucher() + "?")) {
                        return;
                }

                String loi = voucherDAO.xoa(v.getMaVoucher());

                if (loi != null) {

                        UIStyle.canhBao(this, loi);

                        return;
                }

                taiLai(null);
        }

        // Voucher đang hoạt động -> Hủy; đã hủy -> Kích hoạt (chuyển sang "Đang chạy")
        private void kichHoatHuy() {

                Voucher v = dongDangChon();

                if (v == null) {

                        UIStyle.canhBao(this, "Hãy chọn một voucher trong bảng.");

                        return;
                }

                boolean hoatDong = v.isTrangThai();

                // ---------- HỦY ----------
                if (hoatDong) {

                        if (!UIStyle.xacNhan(this, "Hủy voucher " + v.getMaVoucher()
                                        + "? (Vẫn giữ trong danh sách, có thể kích hoạt lại)")) {
                                return;
                        }

                        if (!voucherDAO.doiTrangThai(v.getMaVoucher(), false)) {

                                UIStyle.canhBao(this, "Cập nhật thất bại. Xem lỗi chi tiết trong Console.");

                                return;
                        }

                        taiLai(v.getMaVoucher());

                        return;
                }

                // ---------- KÍCH HOẠT ----------
                LocalDate homNay = LocalDate.now();

                // hết hạn hoặc hết lượt thì kích hoạt cũng không chạy được -> mời người dùng sửa
                boolean hetHan = v.getNgayKetThuc() != null && v.getNgayKetThuc().isBefore(homNay);
                boolean hetLuot = v.getSoLuongToiDa() != null && v.getDaDung() >= v.getSoLuongToiDa();

                if (hetHan || hetLuot) {

                        String lyDo = hetHan
                                        ? "đã quá ngày kết thúc (" + v.getNgayKetThuc().format(D) + ")"
                                        : "đã dùng hết lượt (" + v.getDaDung() + "/" + v.getSoLuongToiDa() + ")";

                        if (UIStyle.xacNhan(this, "Voucher " + v.getMaVoucher() + " " + lyDo
                                        + " nên chưa thể chạy.\nBạn có muốn mở form sửa để gia hạn / tăng lượt không?")) {
                                moDialog(v);
                        }

                        return;
                }

                boolean chuaToiNgay = v.getNgayBatDau() != null && v.getNgayBatDau().isAfter(homNay);

                String hoi = "Kích hoạt voucher " + v.getMaVoucher() + "?";

                if (chuaToiNgay) {
                        hoi += "\nVoucher bắt đầu từ " + v.getNgayBatDau().format(D)
                                        + ", ngày bắt đầu sẽ được đổi thành hôm nay (" + homNay.format(D)
                                        + ") để chạy ngay.";
                }

                if (!UIStyle.xacNhan(this, hoi)) {
                        return;
                }

                boolean ok;

                if (chuaToiNgay) {

                        // dời ngày bắt đầu về hôm nay + bật trạng thái trong cùng một lần cập nhật
                        Voucher moi = new Voucher(
                                        v.getMaVoucher(), v.getTenVoucher(), v.getPhanTramGiam(),
                                        v.getDieuKien(), true, homNay, v.getNgayKetThuc(), v.getSoLuongToiDa());

                        ok = voucherDAO.sua(moi);

                } else {

                        ok = voucherDAO.doiTrangThai(v.getMaVoucher(), true);
                }

                if (!ok) {

                        UIStyle.canhBao(this, "Cập nhật thất bại. Xem lỗi chi tiết trong Console.");

                        return;
                }

                taiLai(v.getMaVoucher());
        }

        // goc = null -> tạo mới; goc != null -> sửa voucher đó
        private void moDialog(Voucher goc) {

                VoucherDialog dlg = new VoucherDialog(SwingUtilities.getWindowAncestor(this), goc, voucherDAO);

                dlg.setVisible(true);

                if (dlg.isDaLuu()) {
                        taiLai(dlg.getMaDaLuu());
                }
        }

        // =========================================================
        // HỘP THOẠI TẠO / SỬA VOUCHER (class lồng)
        // =========================================================

        public static class VoucherDialog extends JDialog {

                private static final Color VIEN = new Color(110, 130, 160);
                private static final Color XAM = new Color(110, 120, 135);
                private static final Font F14 = new Font("Segoe UI", Font.PLAIN, 14);
                private static final Font NHAN = new Font("Segoe UI", Font.BOLD, 12);

                private final VoucherDAO voucherDAO;
                private final Voucher goc;

                private boolean daLuu = false;
                private String maDaLuu = null;

                private final JTextField txtTen = new JTextField();
                private final JTextField txtPt = new JTextField();
                private final JTextField txtBd = new JTextField();
                private final JTextField txtKt = new JTextField();
                private final JTextField txtSl = new JTextField();
                private final JComboBox<String> cboTt = new JComboBox<>(new String[] { "Đang kích hoạt", "Đã hủy" });
                private final JTextArea txtDk = new JTextArea(3, 30);

                public VoucherDialog(Window owner, Voucher goc, VoucherDAO voucherDAO) {

                        super(owner, goc == null ? "Tạo Voucher" : "Sửa Voucher",
                                        Dialog.ModalityType.APPLICATION_MODAL);

                        this.goc = goc;
                        this.voucherDAO = voucherDAO;

                        setSize(760, 600);
                        setResizable(false);
                        setLocationRelativeTo(owner);

                        kieu(txtTen);
                        kieu(txtPt);
                        kieu(txtBd);
                        kieu(txtKt);
                        kieu(txtSl);

                        cboTt.setFont(F14);
                        cboTt.setBackground(Color.WHITE);
                        cboTt.setPreferredSize(new Dimension(100, 36));

                        txtDk.setFont(F14);
                        txtDk.setLineWrap(true);
                        txtDk.setWrapStyleWord(true);
                        txtDk.setBorder(new EmptyBorder(6, 10, 6, 10));

                        JScrollPane dk = new JScrollPane(txtDk);

                        dk.setBorder(new LineBorder(VIEN, 1));
                        dk.setPreferredSize(new Dimension(100, 80));

                        JLabel ghiChu = new JLabel("Để trống ngày / số lượt = không giới hạn.");

                        ghiChu.setFont(NHAN);
                        ghiChu.setForeground(XAM);

                        JPanel body = new JPanel();

                        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
                        body.setOpaque(false);
                        body.setBorder(new EmptyBorder(24, 40, 10, 40));

                        them(body, muc(goc == null ? "Thông tin Voucher"
                                        : "Thông tin Voucher (" + goc.getMaVoucher() + ")"));
                        them(body, Box.createVerticalStrut(8));
                        them(body, hai(cot("Tên chương trình: *", txtTen), cot("Mức giảm (%): *", txtPt)));
                        them(body, Box.createVerticalStrut(8));
                        them(body, cot("Điều kiện áp dụng:", dk));
                        them(body, Box.createVerticalStrut(14));
                        them(body, muc("Hiệu lực & Giới hạn"));
                        them(body, Box.createVerticalStrut(8));
                        them(body, hai(cot("Từ ngày: (dd/MM/yyyy)", txtBd), cot("Đến ngày: (dd/MM/yyyy)", txtKt)));
                        them(body, Box.createVerticalStrut(8));
                        them(body, hai(cot("Số lượt tối đa:", txtSl), cot("Trạng thái:", cboTt)));
                        them(body, Box.createVerticalStrut(6));
                        them(body, ghiChu);

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

                                txtTen.setText(goc.getTenVoucher());

                                txtPt.setText(phanTram(goc.getPhanTramGiam()).replace("%", ""));

                                txtDk.setText(goc.getDieuKien() == null ? "" : goc.getDieuKien());

                                txtBd.setText(goc.getNgayBatDau() == null ? "" : goc.getNgayBatDau().format(D));

                                txtKt.setText(goc.getNgayKetThuc() == null ? "" : goc.getNgayKetThuc().format(D));

                                txtSl.setText(goc.getSoLuongToiDa() == null ? "" : String.valueOf(goc.getSoLuongToiDa()));

                                cboTt.setSelectedIndex(goc.isTrangThai() ? 0 : 1);
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

                // ---------------- lưu ----------------

                private void canhBao(String msg) {

                        JOptionPane.showMessageDialog(this, msg, "Dữ liệu chưa hợp lệ",
                                        JOptionPane.WARNING_MESSAGE);
                }

                private void luu() {

                        String ten = txtTen.getText().trim();

                        if (ten.isEmpty() || ten.length() > 50) {
                                canhBao("Tên chương trình bắt buộc nhập (tối đa 50 ký tự).");
                                return;
                        }

                        double pt;

                        try {
                                pt = Double.parseDouble(txtPt.getText().trim().replace(',', '.'));
                        } catch (NumberFormatException ex) {
                                canhBao("Mức giảm phải là số, ví dụ 10 hoặc 12.5");
                                return;
                        }

                        if (pt <= 0 || pt > 100) {
                                canhBao("Mức giảm phải lớn hơn 0 và không quá 100 (%).");
                                return;
                        }

                        pt = Math.round(pt * 100) / 100.0;

                        String dk = txtDk.getText().trim();

                        if (dk.length() > 255) {
                                canhBao("Điều kiện tối đa 255 ký tự.");
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

                                canhBao("Ngày phải có dạng dd/MM/yyyy, ví dụ 01/11/2026.");

                                return;
                        }

                        if (bd != null && kt != null && kt.isBefore(bd)) {
                                canhBao("Ngày kết thúc phải sau ngày bắt đầu.");
                                return;
                        }

                        Integer sl = null;

                        String s = txtSl.getText().trim();

                        if (!s.isEmpty()) {

                                try {
                                        sl = Integer.parseInt(s);
                                } catch (NumberFormatException ex) {
                                        canhBao("Số lượt tối đa phải là số nguyên.");
                                        return;
                                }

                                if (sl <= 0) {
                                        canhBao("Số lượt tối đa phải lớn hơn 0.");
                                        return;
                                }

                                if (goc != null && sl < goc.getDaDung()) {
                                        canhBao("Voucher đã dùng " + goc.getDaDung()
                                                        + " lượt, số lượt tối đa không thể nhỏ hơn.");
                                        return;
                                }
                        }

                        Voucher v = new Voucher(
                                        goc == null ? null : goc.getMaVoucher(),
                                        ten, pt, dk.isEmpty() ? null : dk,
                                        cboTt.getSelectedIndex() == 0, bd, kt, sl);

                        if (goc == null) {

                                String ma = voucherDAO.them(v);

                                if (ma == null) {

                                        JOptionPane.showMessageDialog(this,
                                                        "Lưu thất bại. Xem lỗi chi tiết trong Console.",
                                                        "Lỗi", JOptionPane.ERROR_MESSAGE);

                                        return;
                                }

                                JOptionPane.showMessageDialog(this, "Đã tạo voucher " + ma + ".");

                                maDaLuu = ma;

                        } else {

                                if (!voucherDAO.sua(v)) {

                                        JOptionPane.showMessageDialog(this,
                                                        "Lưu thất bại. Xem lỗi chi tiết trong Console.",
                                                        "Lỗi", JOptionPane.ERROR_MESSAGE);

                                        return;
                                }

                                JOptionPane.showMessageDialog(this, "Đã cập nhật voucher " + goc.getMaVoucher() + ".");

                                maDaLuu = goc.getMaVoucher();
                        }

                        daLuu = true;

                        dispose();
                }
        }
}
