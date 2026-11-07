package UI;

import UI.TrangChuUI.UIStyle;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Locale;




public class QuanLyVoucherUI extends JPanel {



        private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");


        private static final String[] COLS = { "Mã", "Tên chương trình", "Điều kiện",
                        "Mức giảm", "Đã dùng", "Hiệu lực", "Trạng thái" };


        private static final String[] LOC_HIEN_THI = { "Tất cả trạng thái", "Đang chạy", "Sắp chạy",
                        "Hết hạn", "Hết lượt", "Đã hủy" };



        private static final String[] LOC_MA = { "Tat ca", "Dang chay", "Sap chay",
                        "Het han", "Het luot", "Da huy" };


        private final VoucherDAO voucherDAO = new VoucherDAO();


        private final JTextField txtTim = UIStyle.o(16);

        private final JComboBox<String> cmbTt = new JComboBox<>(LOC_HIEN_THI);

        private final JLabel lblDem = UIStyle.nhan("0 voucher");


        private final JButton btnKichHoat = UIStyle.nutPhu("Kích hoạt voucher");

        private final JButton btnHuyVc = UIStyle.nutPhu("Hủy voucher");

        private final JButton btnDung = UIStyle.nutPhu("Dừng voucher");


        private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {




                @Override
                public boolean isCellEditable(int r, int c) {
                        return false;
                }
        };


        private final JTable tblDuLieu = new JTable(model);



        private ArrayList<Voucher> dsHienThi = new ArrayList<>();




        private final CardLayout cardCT = new CardLayout();

        private final JPanel pnlKhungCT = new JPanel(cardCT);


        private final JLabel lblVMa = giaTri();

        private final JLabel lblVTen = giaTri();

        private final JLabel lblVGiam = giaTri();

        private final JLabel lblVTrangThai = giaTri();

        private final JLabel lblVTu = giaTri();

        private final JLabel lblVDen = giaTri();

        private final JLabel lblVDaDung = giaTri();

        private final JLabel lblVConLai = giaTri();

        private final JLabel lblVDieuKien = new JLabel();


        public QuanLyVoucherUI() {

                setLayout(new BorderLayout(0, 14));
                setBackground(UIStyle.LIGHT_BLUE);
                setBorder(new EmptyBorder(18, 24, 18, 24));



                JLabel lblTieuDe2 = new JLabel("Mã giảm giá và Voucher");

                lblTieuDe2.setFont(UIStyle.font(Font.BOLD, 26));
                lblTieuDe2.setForeground(new Color(40, 40, 40));


                JButton btnTao = UIStyle.nutXanh("+  Tạo voucher");


                btnTao.addActionListener(e -> moDialog(null));


                JPanel pnlDau2 = new JPanel(new BorderLayout());

                pnlDau2.setOpaque(false);
                pnlDau2.add(lblTieuDe2, BorderLayout.WEST);
                pnlDau2.add(btnTao, BorderLayout.EAST);


                cmbTt.setFont(UIStyle.font(Font.PLAIN, 13));


                JButton btnTim = UIStyle.nutChinh("Tìm kiếm");

                JButton btnSua = UIStyle.nutPhu("Sửa");

                JButton btnXoa = UIStyle.nutXoa("Xóa");

                JButton btnLamMoi = UIStyle.nutPhu("Làm mới");


                btnTim.addActionListener(e -> taiLai(maDangChon()));

                txtTim.addActionListener(e -> taiLai(maDangChon()));

                cmbTt.addActionListener(e -> taiLai(maDangChon()));

                btnSua.addActionListener(e -> sua());

                btnXoa.addActionListener(e -> xoa());

                btnKichHoat.addActionListener(e -> kichHoat());

                btnHuyVc.addActionListener(e -> huyVoucher());

                btnDung.addActionListener(e -> dungVoucher());

                btnLamMoi.addActionListener(e -> lamMoiTatCa());


                JPanel pnlLoc2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

                pnlLoc2.setOpaque(false);
                pnlLoc2.add(UIStyle.nhan("Mã / tên:"));
                pnlLoc2.add(txtTim);
                pnlLoc2.add(cmbTt);
                pnlLoc2.add(btnTim);
                pnlLoc2.add(btnSua);
                pnlLoc2.add(btnXoa);
                pnlLoc2.add(btnKichHoat);
                pnlLoc2.add(btnHuyVc);
                pnlLoc2.add(btnDung);
                pnlLoc2.add(btnLamMoi);


                JPanel pnlHangTren = new JPanel(new BorderLayout(0, 8));

                pnlHangTren.setOpaque(false);
                pnlHangTren.add(pnlLoc2, BorderLayout.NORTH);
                pnlHangTren.add(lblDem, BorderLayout.SOUTH);


                UIStyle.taoBang(tblDuLieu, 3, 4);

                tblDuLieu.getColumnModel().getColumn(6).setCellRenderer(new TrangThaiRenderer());


                int[] w = { 60, 170, 230, 80, 80, 190, 110 };

                for (int i = 0; i < w.length; i++) {
                        tblDuLieu.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
                }

                tblDuLieu.getSelectionModel().addListSelectionListener(e -> {
                        if (!e.getValueIsAdjusting()) {
                                hienThongTin();
                        }
                });

                tblDuLieu.addMouseListener(new java.awt.event.MouseAdapter() {


                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                                if (e.getClickCount() == 2 && dongDangChon() != null) {
                                        sua();
                                }
                        }
                });


                pnlKhungCT.setOpaque(false);



                JLabel lblGoiY = new JLabel("Chọn một voucher trong bảng để xem thông tin chi tiết.");

                lblGoiY.setFont(UIStyle.font(Font.PLAIN, 13));
                lblGoiY.setForeground(Color.GRAY);


                JPanel pnlTrong = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));

                pnlTrong.setOpaque(false);
                pnlTrong.add(lblGoiY);

                pnlKhungCT.add(pnlTrong, "TRONG");
                pnlKhungCT.add(taoNoiDungChiTiet(), "CO");
                pnlKhungCT.setPreferredSize(new Dimension(100, 130));


                JPanel pnlChiTiet = new JPanel(new BorderLayout(0, 6));

                pnlChiTiet.setOpaque(false);
                pnlChiTiet.setBorder(new javax.swing.border.MatteBorder(1, 0, 0, 0, UIStyle.LINE));


                JLabel lblTdCT = UIStyle.nhan("Thông tin voucher");

                lblTdCT.setBorder(new EmptyBorder(8, 0, 0, 0));

                pnlChiTiet.add(lblTdCT, BorderLayout.NORTH);
                pnlChiTiet.add(pnlKhungCT, BorderLayout.CENTER);



                JPanel pnlThe = UIStyle.the();

                pnlThe.setLayout(new BorderLayout(0, 10));

                pnlThe.add(pnlHangTren, BorderLayout.NORTH);
                pnlThe.add(UIStyle.cuon(tblDuLieu), BorderLayout.CENTER);
                pnlThe.add(pnlChiTiet, BorderLayout.SOUTH);

                add(pnlDau2, BorderLayout.NORTH);
                add(pnlThe, BorderLayout.CENTER);

                taiLai(null);
        }







        private static JLabel giaTri() {


                JLabel lblNhan3 = new JLabel("-");

                lblNhan3.setFont(UIStyle.font(Font.BOLD, 13));

                return lblNhan3;
        }





        private static JPanel cap(String tieuDe, JLabel lblGt) {


                JPanel pnlKhung2 = new JPanel();

                pnlKhung2.setLayout(new BoxLayout(pnlKhung2, BoxLayout.Y_AXIS));
                pnlKhung2.setOpaque(false);


                JLabel lblNhan3 = new JLabel(tieuDe);

                lblNhan3.setFont(UIStyle.font(Font.PLAIN, 12));
                lblNhan3.setForeground(Color.GRAY);
                lblNhan3.setAlignmentX(Component.LEFT_ALIGNMENT);

                lblGt.setAlignmentX(Component.LEFT_ALIGNMENT);

                pnlKhung2.add(lblNhan3);
                pnlKhung2.add(Box.createVerticalStrut(2));
                pnlKhung2.add(lblGt);

                return pnlKhung2;
        }



        private JPanel taoNoiDungChiTiet() {


                JPanel pnlLuoi2 = new JPanel(new GridLayout(2, 4, 18, 8));

                pnlLuoi2.setOpaque(false);

                pnlLuoi2.add(cap("Mã voucher", lblVMa));
                pnlLuoi2.add(cap("Tên chương trình", lblVTen));
                pnlLuoi2.add(cap("Mức giảm", lblVGiam));
                pnlLuoi2.add(cap("Trạng thái", lblVTrangThai));
                pnlLuoi2.add(cap("Từ ngày", lblVTu));
                pnlLuoi2.add(cap("Đến ngày", lblVDen));
                pnlLuoi2.add(cap("Đã dùng", lblVDaDung));
                pnlLuoi2.add(cap("Còn lại", lblVConLai));

                lblVDieuKien.setFont(UIStyle.font(Font.PLAIN, 13));


                JPanel pnlDk = new JPanel(new BorderLayout(0, 2));

                pnlDk.setOpaque(false);


                JLabel lblNhan3 = new JLabel("Điều kiện áp dụng");

                lblNhan3.setFont(UIStyle.font(Font.PLAIN, 12));
                lblNhan3.setForeground(Color.GRAY);

                pnlDk.add(lblNhan3, BorderLayout.NORTH);
                pnlDk.add(lblVDieuKien, BorderLayout.CENTER);


                JPanel pnlKhung2 = new JPanel(new BorderLayout(0, 8));

                pnlKhung2.setOpaque(false);
                pnlKhung2.add(pnlLuoi2, BorderLayout.NORTH);
                pnlKhung2.add(pnlDk, BorderLayout.CENTER);

                return pnlKhung2;
        }



        private static class TrangThaiRenderer extends DefaultTableCellRenderer {









                @Override
                public Component getTableCellRendererComponent(
                                JTable tblDuLieu3, Object v, boolean sel, boolean foc, int r, int c) {


                        JLabel lblNhan3 = (JLabel) super.getTableCellRendererComponent(tblDuLieu3, v, false, false, r,
                                        c);


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

                        lblNhan3.setOpaque(true);
                        lblNhan3.setBackground(nen);
                        lblNhan3.setForeground(chu);
                        lblNhan3.setHorizontalAlignment(SwingConstants.CENTER);

                        return lblNhan3;
                }
        }








        private static String phanTram(double v) {

                return (v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.US, "%.2f", v)) + "%";
        }





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


                int row = tblDuLieu.getSelectedRow();

                if (row < 0 || row >= dsHienThi.size()) {
                        return null;
                }

                return dsHienThi.get(row);
        }



        private String maDangChon() {

                Voucher v = dongDangChon();

                return v == null ? null : v.getMaVoucher();
        }



        public void lamMoi() {

                taiLai(maDangChon());
        }



        private void lamMoiTatCa() {


                txtTim.setText("");

                cmbTt.setSelectedIndex(0);

                tblDuLieu.clearSelection();

                taiLai(null);
        }




        public void taiLai(String chonMa) {

                String loc = LOC_MA[cmbTt.getSelectedIndex()];


                ArrayList<Voucher> tatCa = voucherDAO.getAll(txtTim.getText());

                dsHienThi = new ArrayList<>();

                tblDuLieu.clearSelection();

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

                                        tblDuLieu.setRowSelectionInterval(i, i);

                                        tblDuLieu.scrollRectToVisible(tblDuLieu.getCellRect(i, 0, true));

                                        break;
                                }
                        }
                }

                hienThongTin();
        }




        private void hienThongTin() {

                Voucher v = dongDangChon();

                if (v == null) {

                        btnKichHoat.setEnabled(false);
                        btnHuyVc.setEnabled(false);
                        btnDung.setEnabled(false);

                        cardCT.show(pnlKhungCT, "TRONG");

                        return;
                }

                String ttMa = v.getTrangThaiText();

                btnKichHoat.setEnabled(!ttMa.equals("Dang chay") && !ttMa.equals("Sap chay"));
                btnHuyVc.setEnabled(v.isTrangThai());
                btnDung.setEnabled(ttMa.equals("Dang chay")
                                && (v.getNgayBatDau() == null || v.getNgayBatDau().isBefore(LocalDate.now())));


                lblVMa.setText(v.getMaVoucher());

                lblVTen.setText(v.getTenVoucher());

                lblVGiam.setText(phanTram(v.getPhanTramGiam()));

                lblVTrangThai.setText(tenTrangThai(v.getTrangThaiText()));

                lblVTu.setText(v.getNgayBatDau() == null ? "Không giới hạn" : v.getNgayBatDau().format(D));

                lblVDen.setText(v.getNgayKetThuc() == null ? "Không giới hạn" : v.getNgayKetThuc().format(D));

                lblVDaDung.setText(v.getDaDung() + " lượt");


                lblVConLai.setText(v.getSoLuongToiDa() == null
                                ? "Không giới hạn"
                                : Math.max(0, v.getSoLuongToiDa() - v.getDaDung()) + " lượt");

                String dk = v.getDieuKien() == null || v.getDieuKien().trim().isEmpty()
                                ? "Không có điều kiện."
                                : v.getDieuKien();


                lblVDieuKien.setText("<html><body style='width:760px'>" + dk
                                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                                + "</body></html>");

                cardCT.show(pnlKhungCT, "CO");
        }






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




        private void kichHoat() {

                Voucher v = dongDangChon();

                if (v == null) {

                        UIStyle.canhBao(this, "Hãy chọn một voucher trong bảng.");

                        return;
                }

                String tt = v.getTrangThaiText();

                if (tt.equals("Dang chay") || tt.equals("Sap chay")) {

                        UIStyle.canhBao(this, "Voucher này đang được kích hoạt rồi.");

                        return;
                }


                LocalDate homNay = LocalDate.now();



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

                if (!UIStyle.xacNhan(this, "Kích hoạt voucher " + v.getMaVoucher() + "?")) {
                        return;
                }

                if (!voucherDAO.doiTrangThai(v.getMaVoucher(), true)) {

                        UIStyle.canhBao(this, "Cập nhật thất bại. Xem lỗi chi tiết trong Console.");

                        return;
                }

                taiLai(v.getMaVoucher());
        }



        private void huyVoucher() {

                Voucher v = dongDangChon();

                if (v == null) {

                        UIStyle.canhBao(this, "Hãy chọn một voucher trong bảng.");

                        return;
                }

                if (!v.isTrangThai()) {

                        UIStyle.canhBao(this, "Voucher này đã hủy rồi.");

                        return;
                }

                if (!UIStyle.xacNhan(this, "Hủy voucher " + v.getMaVoucher()
                                + "? (Vẫn giữ trong danh sách, có thể kích hoạt lại)")) {
                        return;
                }

                if (!voucherDAO.doiTrangThai(v.getMaVoucher(), false)) {

                        UIStyle.canhBao(this, "Cập nhật thất bại. Xem lỗi chi tiết trong Console.");

                        return;
                }

                taiLai(v.getMaVoucher());
        }



        private void dungVoucher() {

                Voucher v = dongDangChon();

                if (v == null) {

                        UIStyle.canhBao(this, "Hãy chọn một voucher trong bảng.");

                        return;
                }

                String tt = v.getTrangThaiText();

                if (!tt.equals("Dang chay")) {

                        UIStyle.canhBao(this, tt.equals("Sap chay")
                                        ? "Voucher chưa chạy nên không cần dừng. Hãy dùng \"Hủy voucher\"."
                                        : "Chỉ có thể dừng voucher đang chạy.");

                        return;
                }


                LocalDate ketThuc = LocalDate.now().minusDays(1);

                if (v.getNgayBatDau() != null && ketThuc.isBefore(v.getNgayBatDau())) {

                        UIStyle.canhBao(this, "Voucher mới bắt đầu từ hôm nay nên không thể dừng theo ngày.\n"
                                        + "Hãy dùng \"Hủy voucher\".");

                        return;
                }

                if (!UIStyle.xacNhan(this, "Dừng voucher " + v.getMaVoucher() + " - " + v.getTenVoucher()
                                + "?\nVoucher sẽ kết thúc ngay (chuyển sang \"Hết hạn\"). "
                                + "Muốn chạy lại, hãy sửa ngày kết thúc rồi kích hoạt.")) {
                        return;
                }

                Voucher moi = new Voucher(
                                v.getMaVoucher(), v.getTenVoucher(), v.getPhanTramGiam(),
                                v.getDieuKien(), v.isTrangThai(), v.getNgayBatDau(), ketThuc, v.getSoLuongToiDa());

                if (!voucherDAO.sua(moi)) {

                        UIStyle.canhBao(this, "Cập nhật thất bại. Xem lỗi chi tiết trong Console.");

                        return;
                }

                taiLai(v.getMaVoucher());
        }




        private void moDialog(Voucher goc) {

                VoucherDialog winDlg = new VoucherDialog(SwingUtilities.getWindowAncestor(this), goc, voucherDAO);

                winDlg.setVisible(true);

                if (winDlg.isDaLuu()) {
                        taiLai(winDlg.getMaDaLuu());
                }
        }






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

                private final JComboBox<String> cmbTt = new JComboBox<>(
                                new String[] { "Đang kích hoạt", "Sắp chạy", "Đã hủy" });

                private final JTextArea txaDk = new JTextArea(3, 30);





                public VoucherDialog(Window winOwner, Voucher goc, VoucherDAO voucherDAO) {

                        super(winOwner, goc == null ? "Tạo Voucher" : "Sửa Voucher",
                                        Dialog.ModalityType.APPLICATION_MODAL);

                        this.goc = goc;
                        this.voucherDAO = voucherDAO;

                        setSize(760, 600);
                        setResizable(false);
                        setLocationRelativeTo(winOwner);

                        kieu(txtTen);
                        kieu(txtPt);
                        kieu(txtBd);
                        kieu(txtKt);
                        kieu(txtSl);

                        cmbTt.setFont(F14);
                        cmbTt.setBackground(Color.WHITE);
                        cmbTt.setPreferredSize(new Dimension(100, 36));

                        txaDk.setFont(F14);
                        txaDk.setLineWrap(true);
                        txaDk.setWrapStyleWord(true);
                        txaDk.setBorder(new EmptyBorder(6, 10, 6, 10));


                        JScrollPane scrDk = new JScrollPane(txaDk);

                        scrDk.setBorder(new LineBorder(VIEN, 1));
                        scrDk.setPreferredSize(new Dimension(100, 80));


                        JLabel lblGhiChu = new JLabel("Để trống ngày / số lượt = không giới hạn.");

                        lblGhiChu.setFont(NHAN);
                        lblGhiChu.setForeground(XAM);


                        JPanel pnlBody = new JPanel();

                        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
                        pnlBody.setOpaque(false);
                        pnlBody.setBorder(new EmptyBorder(24, 40, 10, 40));

                        them(pnlBody, muc(goc == null ? "Thông tin Voucher"
                                        : "Thông tin Voucher (" + goc.getMaVoucher() + ")"));
                        them(pnlBody, Box.createVerticalStrut(8));
                        them(pnlBody, hai(cot("Tên chương trình: *", txtTen), cot("Mức giảm (%): *", txtPt)));
                        them(pnlBody, Box.createVerticalStrut(8));
                        them(pnlBody, cot("Điều kiện áp dụng:", scrDk));
                        them(pnlBody, Box.createVerticalStrut(14));
                        them(pnlBody, muc("Hiệu lực & Giới hạn"));
                        them(pnlBody, Box.createVerticalStrut(8));
                        them(pnlBody, hai(cot("Từ ngày: (dd/MM/yyyy)", oChonNgay(txtBd)),
                                        cot("Đến ngày: (dd/MM/yyyy)", oChonNgay(txtKt))));
                        them(pnlBody, Box.createVerticalStrut(8));
                        them(pnlBody, hai(cot("Số lượt tối đa:", txtSl), cot("Trạng thái:", cmbTt)));
                        them(pnlBody, Box.createVerticalStrut(6));
                        them(pnlBody, lblGhiChu);


                        JPanel pnlTop2 = new JPanel(new BorderLayout());

                        pnlTop2.setOpaque(false);
                        pnlTop2.add(pnlBody, BorderLayout.NORTH);



                        JButton btnHuy = nut("Hủy", new Color(138, 144, 153));

                        JButton btnLuu = nut(goc == null ? "Thêm mới" : "Lưu thay đổi", new Color(15, 42, 68));


                        btnHuy.addActionListener(e -> dispose());

                        btnLuu.addActionListener(e -> luu());


                        JPanel pnlHangNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

                        pnlHangNut.setOpaque(false);
                        pnlHangNut.setBorder(new EmptyBorder(6, 40, 24, 40));
                        pnlHangNut.add(btnHuy);
                        pnlHangNut.add(btnLuu);


                        JPanel pnlRoot = new JPanel(new BorderLayout());

                        pnlRoot.setBackground(Color.WHITE);
                        pnlRoot.add(pnlTop2, BorderLayout.CENTER);
                        pnlRoot.add(pnlHangNut, BorderLayout.SOUTH);

                        setContentPane(pnlRoot);


                        if (goc != null) {


                                txtTen.setText(goc.getTenVoucher());


                                txtPt.setText(phanTram(goc.getPhanTramGiam()).replace("%", ""));


                                txaDk.setText(goc.getDieuKien() == null ? "" : goc.getDieuKien());


                                txtBd.setText(goc.getNgayBatDau() == null ? "" : goc.getNgayBatDau().format(D));


                                txtKt.setText(goc.getNgayKetThuc() == null ? "" : goc.getNgayKetThuc().format(D));


                                txtSl.setText(goc.getSoLuongToiDa() == null ? ""
                                                : String.valueOf(goc.getSoLuongToiDa()));

                                cmbTt.setSelectedIndex(!goc.isTrangThai() ? 2
                                                : goc.getTrangThaiText().equals("Sap chay") ? 1 : 0);
                        }
                }



                public boolean isDaLuu() {

                        return daLuu;
                }


                public String getMaDaLuu() {

                        return maDaLuu;
                }







                private static JPanel cot(String nhan, JComponent c) {


                        JPanel pnlKhung2 = new JPanel(new BorderLayout(0, 4));

                        pnlKhung2.setOpaque(false);


                        JLabel lblNhan3 = new JLabel(nhan);

                        lblNhan3.setFont(NHAN);
                        lblNhan3.setForeground(XAM);

                        pnlKhung2.add(lblNhan3, BorderLayout.NORTH);
                        pnlKhung2.add(c, BorderLayout.CENTER);

                        return pnlKhung2;
                }





                private static JPanel hai(JComponent a, JComponent b) {


                        JPanel pnlKhung2 = new JPanel(new GridLayout(1, 2, 20, 0));

                        pnlKhung2.setOpaque(false);

                        pnlKhung2.add(a);
                        pnlKhung2.add(b);

                        return pnlKhung2;
                }




                private static JLabel muc(String text) {


                        JLabel lblNhan3 = new JLabel(text);

                        lblNhan3.setFont(new Font("Segoe UI", Font.BOLD, 15));
                        lblNhan3.setForeground(new Color(20, 30, 45));

                        return lblNhan3;
                }



                private static void kieu(JTextField txtVanBan) {

                        txtVanBan.setFont(F14);

                        txtVanBan.setBorder(new CompoundBorder(
                                        new LineBorder(VIEN, 1),
                                        new EmptyBorder(6, 10, 6, 10)));

                        txtVanBan.setPreferredSize(new Dimension(100, 36));
                }





                private static JButton nut(String text, Color nen) {


                        JButton btnThaoTac2 = new JButton(text);

                        btnThaoTac2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        btnThaoTac2.setForeground(Color.WHITE);
                        btnThaoTac2.setBackground(nen);
                        btnThaoTac2.setOpaque(true);
                        btnThaoTac2.setBorderPainted(false);
                        btnThaoTac2.setFocusPainted(false);
                        btnThaoTac2.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        btnThaoTac2.setPreferredSize(
                                        new Dimension(Math.max(110, btnThaoTac2.getPreferredSize().width + 10), 38));

                        return btnThaoTac2;
                }




                private static void them(JPanel pnlKhung2, Component c) {

                        if (c instanceof JComponent) {
                                ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
                        }

                        pnlKhung2.add(c);
                }







                private JPanel oChonNgay(JTextField txtTxt) {



                        txtTxt.setEditable(false);
                        txtTxt.setFocusable(false);
                        txtTxt.setBackground(new Color(245, 247, 250));
                        txtTxt.setToolTipText("Nhấn nút Lịch để chọn ngày (dd/MM/yyyy)");


                        JButton btnLich = nut("Lịch", new Color(15, 42, 68));

                        btnLich.setPreferredSize(new Dimension(70, 36));


                        btnLich.addActionListener(e -> {


                                LocalDate hienTai = null;

                                try {

                                        String t = txtTxt.getText().trim();

                                        if (!t.isEmpty()) {

                                                hienTai = LocalDate.parse(t, D);
                                        }
                                } catch (DateTimeParseException ex) {
                                        hienTai = null;
                                }

                                LichDialog winLich = new LichDialog(this, hienTai);


                                Point p = btnLich.getLocationOnScreen();
                                Rectangle mh = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                .getMaximumWindowBounds();


                                int x = p.x + btnLich.getWidth() + 10;

                                int y = p.y - 10;

                                if (x + winLich.getWidth() > mh.x + mh.width) {
                                        x = p.x - winLich.getWidth() - 10;
                                }

                                if (y + winLich.getHeight() > mh.y + mh.height) {
                                        y = mh.y + mh.height - winLich.getHeight();
                                }

                                winLich.setLocation(Math.max(mh.x, x), Math.max(mh.y, y));
                                winLich.setVisible(true);

                                if (winLich.getKetQua() != null) {

                                        txtTxt.setText(winLich.getKetQua().format(D));
                                }
                        });


                        JPanel pnlKhung2 = new JPanel(new BorderLayout(6, 0));

                        pnlKhung2.setOpaque(false);
                        pnlKhung2.add(txtTxt, BorderLayout.CENTER);
                        pnlKhung2.add(btnLich, BorderLayout.EAST);

                        return pnlKhung2;
                }




                private static class LichDialog extends JDialog {


                        private static final Color NAVY = new Color(15, 42, 68);

                        private static final Color XANH_NHAT = new Color(222, 236, 250);

                        private static final Color VIEN_O = new Color(200, 205, 212);

                        private static final String[] THU = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };


                        private final LocalDate dangChon;

                        private YearMonth thang;

                        private LocalDate ketQua = null;


                        private final JLabel lblThang = new JLabel("", SwingConstants.CENTER);

                        private final JPanel pnlLuoi2 = new JPanel(new GridLayout(7, 7, 6, 6));




                        LichDialog(Component cha, LocalDate batDau) {

                                super(SwingUtilities.getWindowAncestor(cha), "Chọn ngày",
                                                Dialog.ModalityType.APPLICATION_MODAL);

                                this.dangChon = batDau;
                                this.thang = YearMonth.from(batDau != null ? batDau : LocalDate.now());

                                setResizable(false);

                                lblThang.setFont(new Font("Segoe UI", Font.BOLD, 16));
                                lblThang.setForeground(NAVY);


                                JButton btnTruoc2 = nutNhat("◄");

                                JButton btnSau2 = nutNhat("►");

                                btnTruoc2.setPreferredSize(new Dimension(70, 34));
                                btnSau2.setPreferredSize(new Dimension(70, 34));


                                btnTruoc2.addActionListener(e -> {
                                        thang = thang.minusMonths(1);
                                        veLich();
                                });


                                btnSau2.addActionListener(e -> {
                                        thang = thang.plusMonths(1);
                                        veLich();
                                });


                                JPanel pnlDau2 = new JPanel(new BorderLayout());

                                pnlDau2.setOpaque(false);
                                pnlDau2.add(btnTruoc2, BorderLayout.WEST);
                                pnlDau2.add(lblThang, BorderLayout.CENTER);
                                pnlDau2.add(btnSau2, BorderLayout.EAST);

                                pnlLuoi2.setOpaque(false);
                                pnlLuoi2.setBorder(new EmptyBorder(14, 0, 10, 0));


                                JButton btnHomNay2 = nutNhat("Hôm nay");

                                JButton btnHuy2 = nutNhat("Hủy");


                                btnHomNay2.addActionListener(e -> {
                                        ketQua = LocalDate.now();
                                        dispose();
                                });


                                btnHuy2.addActionListener(e -> dispose());


                                JPanel pnlChan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

                                pnlChan.setOpaque(false);
                                pnlChan.add(btnHomNay2);
                                pnlChan.add(btnHuy2);


                                JPanel pnlRoot = new JPanel(new BorderLayout());

                                pnlRoot.setBackground(Color.WHITE);
                                pnlRoot.setBorder(new EmptyBorder(14, 16, 14, 16));
                                pnlRoot.add(pnlDau2, BorderLayout.NORTH);
                                pnlRoot.add(pnlLuoi2, BorderLayout.CENTER);
                                pnlRoot.add(pnlChan, BorderLayout.SOUTH);

                                setContentPane(pnlRoot);

                                veLich();

                                setSize(360, 400);
                        }


                        LocalDate getKetQua() {

                                return ketQua;
                        }




                        private static JButton nutNhat(String text) {


                                JButton btnThaoTac2 = new JButton(text);

                                btnThaoTac2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                                btnThaoTac2.setForeground(NAVY);
                                btnThaoTac2.setBackground(XANH_NHAT);
                                btnThaoTac2.setOpaque(true);
                                btnThaoTac2.setFocusPainted(false);
                                btnThaoTac2.setContentAreaFilled(true);
                                btnThaoTac2.setBorder(new LineBorder(new Color(160, 190, 225), 1));
                                btnThaoTac2.setCursor(new Cursor(Cursor.HAND_CURSOR));
                                btnThaoTac2.setPreferredSize(new Dimension(
                                                Math.max(80, btnThaoTac2.getPreferredSize().width + 16), 32));

                                return btnThaoTac2;
                        }


                        private void veLich() {


                                lblThang.setText("Tháng " + thang.getMonthValue() + " / " + thang.getYear());

                                pnlLuoi2.removeAll();


                                for (String t : THU) {


                                        JLabel lblNhan3 = new JLabel(t, SwingConstants.CENTER);

                                        lblNhan3.setFont(new Font("Segoe UI", Font.BOLD, 12));
                                        lblNhan3.setForeground(NAVY);

                                        pnlLuoi2.add(lblNhan3);
                                }


                                int lech = thang.atDay(1).getDayOfWeek().getValue() - 1;
                                int soNgay = thang.lengthOfMonth();

                                LocalDate homNay = LocalDate.now();

                                for (int i = 0; i < 42; i++) {

                                        int ngay = i - lech + 1;

                                        if (ngay < 1 || ngay > soNgay) {
                                                pnlLuoi2.add(new JLabel());
                                                continue;
                                        }


                                        LocalDate d = thang.atDay(ngay);


                                        JButton btnThaoTac2 = new JButton(String.valueOf(ngay));

                                        btnThaoTac2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                                        btnThaoTac2.setForeground(new Color(40, 40, 40));
                                        btnThaoTac2.setBackground(Color.WHITE);
                                        btnThaoTac2.setOpaque(true);
                                        btnThaoTac2.setFocusPainted(false);
                                        btnThaoTac2.setContentAreaFilled(true);
                                        btnThaoTac2.setBorder(new LineBorder(VIEN_O, 1));
                                        btnThaoTac2.setCursor(new Cursor(Cursor.HAND_CURSOR));

                                        if (d.equals(homNay)) {
                                                btnThaoTac2.setBackground(XANH_NHAT);
                                                btnThaoTac2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                                        }

                                        if (d.equals(dangChon)) {
                                                btnThaoTac2.setBackground(NAVY);
                                                btnThaoTac2.setForeground(Color.WHITE);
                                                btnThaoTac2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                                        }


                                        btnThaoTac2.addActionListener(e -> {
                                                ketQua = d;
                                                dispose();
                                        });

                                        pnlLuoi2.add(btnThaoTac2);
                                }

                                pnlLuoi2.revalidate();
                                pnlLuoi2.repaint();
                        }
                }





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


                        String dk = txaDk.getText().trim();

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


                        if (cmbTt.getSelectedIndex() == 0 && bd != null && bd.isAfter(LocalDate.now())) {
                                canhBao("Từ ngày còn ở tương lai, hãy chọn trạng thái \"Sắp chạy\".");
                                return;
                        }


                        if (cmbTt.getSelectedIndex() == 1 && (bd == null || !bd.isAfter(LocalDate.now()))) {
                                canhBao("Trạng thái \"Sắp chạy\" cần có Từ ngày sau hôm nay.");
                                return;
                        }

                        Voucher v = new Voucher(
                                        goc == null ? null : goc.getMaVoucher(),
                                        ten, pt, dk.isEmpty() ? null : dk,
                                        cmbTt.getSelectedIndex() != 2, bd, kt, sl);

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
