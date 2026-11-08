package UI;

import UI.TrangChuUI.UIStyle;
import control.VoucherControl;
import control.VoucherControl.KetQua;
import entity.Voucher;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    private final VoucherControl control = new VoucherControl();


    private final JTextField txtTim = UIStyle.o(16);
    private final JComboBox<String> cmbLoc = new JComboBox<>(LOC_HIEN_THI);
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


    private final CardLayout cardChiTiet = new CardLayout();
    private final JPanel pnlKhungChiTiet = new JPanel(cardChiTiet);
    private final JLabel lblMa = taoNhanGiaTri();
    private final JLabel lblTen = taoNhanGiaTri();
    private final JLabel lblGiam = taoNhanGiaTri();
    private final JLabel lblTrangThai = taoNhanGiaTri();
    private final JLabel lblTu = taoNhanGiaTri();
    private final JLabel lblDen = taoNhanGiaTri();
    private final JLabel lblDaDung = taoNhanGiaTri();
    private final JLabel lblConLai = taoNhanGiaTri();
    private final JLabel lblDieuKien = new JLabel();

    public QuanLyVoucherUI() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UIStyle.LIGHT_BLUE);
        setBorder(new EmptyBorder(18, 24, 18, 24));

        caiDatBang();

        JPanel pnlThe = UIStyle.the();
        pnlThe.setLayout(new BorderLayout(0, 10));
        pnlThe.add(taoThanhCongCu(), BorderLayout.NORTH);
        pnlThe.add(UIStyle.cuon(tblDuLieu), BorderLayout.CENTER);
        pnlThe.add(taoPhanChiTiet(), BorderLayout.SOUTH);

        add(taoTieuDe(), BorderLayout.NORTH);
        add(pnlThe, BorderLayout.CENTER);

        taiLai(null);
    }





    private JPanel taoTieuDe() {
        JLabel lblTieuDe = new JLabel("Mã giảm giá và Voucher");
        lblTieuDe.setFont(UIStyle.font(Font.BOLD, 26));
        lblTieuDe.setForeground(new Color(40, 40, 40));

        JButton btnTao = UIStyle.nutXanh("+  Tạo voucher");
        btnTao.addActionListener(e -> moDialog(null));

        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.add(lblTieuDe, BorderLayout.WEST);
        pnl.add(btnTao, BorderLayout.EAST);
        return pnl;
    }

    private JPanel taoThanhCongCu() {
        cmbLoc.setFont(UIStyle.font(Font.PLAIN, 13));

        JButton btnTim = UIStyle.nutChinh("Tìm kiếm");
        JButton btnSua = UIStyle.nutPhu("Sửa");
        JButton btnXoa = UIStyle.nutXoa("Xóa");
        JButton btnLamMoi = UIStyle.nutPhu("Làm mới");

        btnTim.addActionListener(e -> taiLai(maDangChon()));
        txtTim.addActionListener(e -> taiLai(maDangChon()));
        cmbLoc.addActionListener(e -> taiLai(maDangChon()));
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnKichHoat.addActionListener(e -> kichHoat());
        btnHuyVc.addActionListener(e -> huyVoucher());
        btnDung.addActionListener(e -> dungVoucher());
        btnLamMoi.addActionListener(e -> lamMoiTatCa());

        JPanel pnlLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(UIStyle.nhan("Mã / tên:"));
        pnlLoc.add(txtTim);
        pnlLoc.add(cmbLoc);
        pnlLoc.add(btnTim);
        pnlLoc.add(btnSua);
        pnlLoc.add(btnXoa);
        pnlLoc.add(btnKichHoat);
        pnlLoc.add(btnHuyVc);
        pnlLoc.add(btnDung);
        pnlLoc.add(btnLamMoi);

        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setOpaque(false);
        pnl.add(pnlLoc, BorderLayout.NORTH);
        pnl.add(lblDem, BorderLayout.SOUTH);
        return pnl;
    }

    private void caiDatBang() {
        UIStyle.taoBang(tblDuLieu, 3, 4);
        tblDuLieu.getColumnModel().getColumn(6).setCellRenderer(new TrangThaiRenderer());

        int[] doRong = { 60, 170, 230, 80, 80, 190, 110 };
        for (int i = 0; i < doRong.length; i++) {
            tblDuLieu.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }


        tblDuLieu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienThongTin();
            }
        });


        tblDuLieu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && dongDangChon() != null) {
                    sua();
                }
            }
        });
    }

    private JPanel taoPhanChiTiet() {

        JLabel lblGoiY = new JLabel("Chọn một voucher trong bảng để xem thông tin chi tiết.");
        lblGoiY.setFont(UIStyle.font(Font.PLAIN, 13));
        lblGoiY.setForeground(Color.GRAY);

        JPanel pnlTrong = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        pnlTrong.setOpaque(false);
        pnlTrong.add(lblGoiY);


        JPanel pnlLuoi = new JPanel(new GridLayout(2, 4, 18, 8));
        pnlLuoi.setOpaque(false);
        pnlLuoi.add(taoCapThongTin("Mã voucher", lblMa));
        pnlLuoi.add(taoCapThongTin("Tên chương trình", lblTen));
        pnlLuoi.add(taoCapThongTin("Mức giảm", lblGiam));
        pnlLuoi.add(taoCapThongTin("Trạng thái", lblTrangThai));
        pnlLuoi.add(taoCapThongTin("Từ ngày", lblTu));
        pnlLuoi.add(taoCapThongTin("Đến ngày", lblDen));
        pnlLuoi.add(taoCapThongTin("Đã dùng", lblDaDung));
        pnlLuoi.add(taoCapThongTin("Còn lại", lblConLai));

        lblDieuKien.setFont(UIStyle.font(Font.PLAIN, 13));

        JLabel lblNhanDk = new JLabel("Điều kiện áp dụng");
        lblNhanDk.setFont(UIStyle.font(Font.PLAIN, 12));
        lblNhanDk.setForeground(Color.GRAY);

        JPanel pnlDk = new JPanel(new BorderLayout(0, 2));
        pnlDk.setOpaque(false);
        pnlDk.add(lblNhanDk, BorderLayout.NORTH);
        pnlDk.add(lblDieuKien, BorderLayout.CENTER);

        JPanel pnlCo = new JPanel(new BorderLayout(0, 8));
        pnlCo.setOpaque(false);
        pnlCo.add(pnlLuoi, BorderLayout.NORTH);
        pnlCo.add(pnlDk, BorderLayout.CENTER);

        pnlKhungChiTiet.setOpaque(false);
        pnlKhungChiTiet.add(pnlTrong, "TRONG");
        pnlKhungChiTiet.add(pnlCo, "CO");
        pnlKhungChiTiet.setPreferredSize(new Dimension(100, 130));

        JLabel lblTieuDe = UIStyle.nhan("Thông tin voucher");
        lblTieuDe.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel pnl = new JPanel(new BorderLayout(0, 6));
        pnl.setOpaque(false);
        pnl.setBorder(new MatteBorder(1, 0, 0, 0, UIStyle.LINE));
        pnl.add(lblTieuDe, BorderLayout.NORTH);
        pnl.add(pnlKhungChiTiet, BorderLayout.CENTER);
        return pnl;
    }

    private static JLabel taoNhanGiaTri() {
        JLabel lbl = new JLabel("-");
        lbl.setFont(UIStyle.font(Font.BOLD, 13));
        return lbl;
    }


    private static JPanel taoCapThongTin(String tieuDe, JLabel lblGiaTri) {
        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(UIStyle.font(Font.PLAIN, 12));
        lblTieuDe.setForeground(Color.GRAY);
        lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblGiaTri.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);
        pnl.add(lblTieuDe);
        pnl.add(Box.createVerticalStrut(2));
        pnl.add(lblGiaTri);
        return pnl;
    }


    private static class TrangThaiRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean sel, boolean foc, int row, int col) {

            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, false, false, row, col);

            Color nen;
            Color chu;
            switch (String.valueOf(value)) {
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

            lbl.setOpaque(true);
            lbl.setBackground(nen);
            lbl.setForeground(chu);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            return lbl;
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
        cmbLoc.setSelectedIndex(0);
        taiLai(null);
    }


    public void taiLai(String chonMa) {
        dsHienThi = control.timKiem(txtTim.getText(), LOC_MA[cmbLoc.getSelectedIndex()]);

        tblDuLieu.clearSelection();
        model.setRowCount(0);

        for (Voucher v : dsHienThi) {
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
                    tenTrangThai(v.getTrangThaiText()) });
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
            cardChiTiet.show(pnlKhungChiTiet, "TRONG");
            return;
        }


        String tt = v.getTrangThaiText();
        btnKichHoat.setEnabled(!tt.equals("Dang chay") && !tt.equals("Sap chay"));
        btnHuyVc.setEnabled(v.isTrangThai());
        btnDung.setEnabled(tt.equals("Dang chay")
                && (v.getNgayBatDau() == null || v.getNgayBatDau().isBefore(LocalDate.now())));

        lblMa.setText(v.getMaVoucher());
        lblTen.setText(v.getTenVoucher());
        lblGiam.setText(phanTram(v.getPhanTramGiam()));
        lblTrangThai.setText(tenTrangThai(tt));
        lblTu.setText(v.getNgayBatDau() == null ? "Không giới hạn" : v.getNgayBatDau().format(D));
        lblDen.setText(v.getNgayKetThuc() == null ? "Không giới hạn" : v.getNgayKetThuc().format(D));
        lblDaDung.setText(v.getDaDung() + " lượt");
        lblConLai.setText(v.getSoLuongToiDa() == null
                ? "Không giới hạn"
                : Math.max(0, v.getSoLuongToiDa() - v.getDaDung()) + " lượt");

        String dk = v.getDieuKien() == null || v.getDieuKien().trim().isEmpty()
                ? "Không có điều kiện."
                : v.getDieuKien();


        lblDieuKien.setText("<html><body style='width:760px'>"
                + dk.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                + "</body></html>");

        cardChiTiet.show(pnlKhungChiTiet, "CO");
    }






    private Voucher chonVoucher(String thongBaoNeuChuaChon) {
        Voucher v = dongDangChon();
        if (v == null) {
            JOptionPane.showMessageDialog(this, thongBaoNeuChuaChon, "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
        }
        return v;
    }

    private boolean hoiXacNhan(String cauHoi) {
        return JOptionPane.showConfirmDialog(this, cauHoi, "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }


    private void xuLyKetQua(KetQua kq, String chonMa) {
        hienKetQua(this, kq);
        if (kq.isThanhCong()) {
            taiLai(chonMa);
        }
    }

    private static void hienKetQua(Component cha, KetQua kq) {
        if (kq.isThanhCong()) {
            JOptionPane.showMessageDialog(cha, kq.getThongBao(), "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(cha, kq.getThongBao(), "Dữ liệu chưa hợp lệ",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void sua() {
        Voucher v = chonVoucher("Hãy chọn một voucher trong bảng để sửa.");
        if (v != null) {
            moDialog(v);
        }
    }

    private void xoa() {
        Voucher v = chonVoucher("Hãy chọn một voucher trong bảng để xóa.");
        if (v == null || !hoiXacNhan("Xóa hẳn voucher " + v.getMaVoucher() + " - " + v.getTenVoucher() + "?")) {
            return;
        }
        xuLyKetQua(control.xoa(v.getMaVoucher()), null);
    }

    private void kichHoat() {
        Voucher v = chonVoucher("Hãy chọn một voucher trong bảng.");
        if (v == null || !hoiXacNhan("Kích hoạt voucher " + v.getMaVoucher() + "?")) {
            return;
        }
        xuLyKetQua(control.kichHoat(v), v.getMaVoucher());
    }

    private void huyVoucher() {
        Voucher v = chonVoucher("Hãy chọn một voucher trong bảng.");
        if (v == null || !hoiXacNhan("Hủy voucher " + v.getMaVoucher()
                + "? (Vẫn giữ trong danh sách, có thể kích hoạt lại)")) {
            return;
        }
        xuLyKetQua(control.huy(v), v.getMaVoucher());
    }

    private void dungVoucher() {
        Voucher v = chonVoucher("Hãy chọn một voucher trong bảng.");
        if (v == null || !hoiXacNhan("Dừng voucher " + v.getMaVoucher() + " - " + v.getTenVoucher()
                + "?\nVoucher sẽ kết thúc ngay (chuyển sang \"Hết hạn\"). "
                + "Muốn chạy lại, hãy sửa ngày kết thúc rồi kích hoạt.")) {
            return;
        }
        xuLyKetQua(control.dung(v), v.getMaVoucher());
    }


    private void moDialog(Voucher goc) {
        VoucherDialog dlg = new VoucherDialog(SwingUtilities.getWindowAncestor(this), goc, control);
        dlg.setVisible(true);

        if (dlg.isDaLuu()) {
            taiLai(dlg.getMaDaLuu());
        }
    }





    public static class VoucherDialog extends JDialog {

        private static final Color VIEN = new Color(110, 130, 160);
        private static final Color XAM = new Color(110, 120, 135);
        private static final Color NAVY = new Color(15, 42, 68);
        private static final Font F14 = new Font("Segoe UI", Font.PLAIN, 14);
        private static final Font F_NHAN = new Font("Segoe UI", Font.BOLD, 12);

        private final VoucherControl control;
        private final Voucher goc;

        private boolean daLuu = false;
        private String maDaLuu = null;

        private final JTextField txtTen = new JTextField();
        private final JTextField txtPt = new JTextField();
        private final JTextField txtBd = new JTextField();
        private final JTextField txtKt = new JTextField();
        private final JTextField txtSl = new JTextField();
        private final JComboBox<String> cmbTrangThai = new JComboBox<>(
                new String[] { "Đang kích hoạt", "Sắp chạy", "Đã hủy" });
        private final JTextArea txaDk = new JTextArea(3, 30);

        public VoucherDialog(Window cha, Voucher goc, VoucherControl control) {
            super(cha, goc == null ? "Tạo Voucher" : "Sửa Voucher", Dialog.ModalityType.APPLICATION_MODAL);
            this.goc = goc;
            this.control = control;

            setSize(760, 600);
            setResizable(false);
            setLocationRelativeTo(cha);

            kieuO(txtTen);
            kieuO(txtPt);
            kieuO(txtBd);
            kieuO(txtKt);
            kieuO(txtSl);

            cmbTrangThai.setFont(F14);
            cmbTrangThai.setBackground(Color.WHITE);
            cmbTrangThai.setPreferredSize(new Dimension(100, 36));

            txaDk.setFont(F14);
            txaDk.setLineWrap(true);
            txaDk.setWrapStyleWord(true);
            txaDk.setBorder(new EmptyBorder(6, 10, 6, 10));

            JScrollPane scrDk = new JScrollPane(txaDk);
            scrDk.setBorder(new LineBorder(VIEN, 1));
            scrDk.setPreferredSize(new Dimension(100, 80));

            JLabel lblGhiChu = new JLabel("Để trống ngày / số lượt = không giới hạn.");
            lblGhiChu.setFont(F_NHAN);
            lblGhiChu.setForeground(XAM);


            JPanel pnlThan = new JPanel();
            pnlThan.setLayout(new BoxLayout(pnlThan, BoxLayout.Y_AXIS));
            pnlThan.setOpaque(false);
            pnlThan.setBorder(new EmptyBorder(24, 40, 10, 40));

            themVao(pnlThan, taoMuc(goc == null ? "Thông tin Voucher"
                    : "Thông tin Voucher (" + goc.getMaVoucher() + ")"));
            themVao(pnlThan, Box.createVerticalStrut(8));
            themVao(pnlThan, taoHaiCot(taoCot("Tên chương trình: *", txtTen), taoCot("Mức giảm (%): *", txtPt)));
            themVao(pnlThan, Box.createVerticalStrut(8));
            themVao(pnlThan, taoCot("Điều kiện áp dụng:", scrDk));
            themVao(pnlThan, Box.createVerticalStrut(14));
            themVao(pnlThan, taoMuc("Hiệu lực & Giới hạn"));
            themVao(pnlThan, Box.createVerticalStrut(8));
            themVao(pnlThan, taoHaiCot(taoCot("Từ ngày: (dd/MM/yyyy)", oChonNgay(txtBd)),
                    taoCot("Đến ngày: (dd/MM/yyyy)", oChonNgay(txtKt))));
            themVao(pnlThan, Box.createVerticalStrut(8));
            themVao(pnlThan, taoHaiCot(taoCot("Số lượt tối đa:", txtSl), taoCot("Trạng thái:", cmbTrangThai)));
            themVao(pnlThan, Box.createVerticalStrut(6));
            themVao(pnlThan, lblGhiChu);

            JPanel pnlTren = new JPanel(new BorderLayout());
            pnlTren.setOpaque(false);
            pnlTren.add(pnlThan, BorderLayout.NORTH);


            JButton btnHuy = taoNut("Hủy", new Color(138, 144, 153));
            JButton btnLuu = taoNut(goc == null ? "Thêm mới" : "Lưu thay đổi", NAVY);
            btnHuy.addActionListener(e -> dispose());
            btnLuu.addActionListener(e -> luu());

            JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnlNut.setOpaque(false);
            pnlNut.setBorder(new EmptyBorder(6, 40, 24, 40));
            pnlNut.add(btnHuy);
            pnlNut.add(btnLuu);

            JPanel pnlGoc = new JPanel(new BorderLayout());
            pnlGoc.setBackground(Color.WHITE);
            pnlGoc.add(pnlTren, BorderLayout.CENTER);
            pnlGoc.add(pnlNut, BorderLayout.SOUTH);
            setContentPane(pnlGoc);


            if (goc != null) {
                txtTen.setText(goc.getTenVoucher());
                txtPt.setText(phanTram(goc.getPhanTramGiam()).replace("%", ""));
                txaDk.setText(goc.getDieuKien() == null ? "" : goc.getDieuKien());
                txtBd.setText(goc.getNgayBatDau() == null ? "" : goc.getNgayBatDau().format(D));
                txtKt.setText(goc.getNgayKetThuc() == null ? "" : goc.getNgayKetThuc().format(D));
                txtSl.setText(goc.getSoLuongToiDa() == null ? "" : String.valueOf(goc.getSoLuongToiDa()));

                cmbTrangThai.setSelectedIndex(!goc.isTrangThai() ? VoucherControl.TT_DA_HUY
                        : goc.getTrangThaiText().equals("Sap chay") ? VoucherControl.TT_SAP_CHAY
                                : VoucherControl.TT_KICH_HOAT);
            }
        }

        public boolean isDaLuu() {
            return daLuu;
        }

        public String getMaDaLuu() {
            return maDaLuu;
        }




        private void luu() {
            String ten = txtTen.getText();
            String pt = txtPt.getText();
            String dk = txaDk.getText();
            String bd = txtBd.getText();
            String kt = txtKt.getText();
            String sl = txtSl.getText();
            int tt = cmbTrangThai.getSelectedIndex();

            KetQua kq;
            if (goc == null) {
                kq = control.them(ten, pt, dk, bd, kt, sl, tt);
            } else {
                kq = control.sua(goc, ten, pt, dk, bd, kt, sl, tt);
            }

            hienKetQua(this, kq);

            if (kq.isThanhCong()) {
                daLuu = true;
                maDaLuu = kq.getMaVoucher();
                dispose();
            }
        }




        private JPanel oChonNgay(JTextField txt) {
            txt.setEditable(false);
            txt.setFocusable(false);
            txt.setBackground(new Color(245, 247, 250));
            txt.setToolTipText("Nhấn nút Lịch để chọn ngày (dd/MM/yyyy)");

            JButton btnLich = taoNut("Lịch", NAVY);
            btnLich.setPreferredSize(new Dimension(70, 36));

            btnLich.addActionListener(e -> {
                LocalDate hienTai = null;
                try {
                    if (!txt.getText().trim().isEmpty()) {
                        hienTai = LocalDate.parse(txt.getText().trim(), D);
                    }
                } catch (DateTimeParseException ex) {
                    hienTai = null;
                }

                LichDialog lich = new LichDialog(this, hienTai);
                lich.setVisible(true);

                if (lich.getKetQua() != null) {
                    txt.setText(lich.getKetQua().format(D));
                }
            });

            JPanel pnl = new JPanel(new BorderLayout(6, 0));
            pnl.setOpaque(false);
            pnl.add(txt, BorderLayout.CENTER);
            pnl.add(btnLich, BorderLayout.EAST);
            return pnl;
        }


        private static class LichDialog extends JDialog {

            private static final Color XANH_NHAT = new Color(222, 236, 250);
            private static final Color VIEN_O = new Color(200, 205, 212);
            private static final String[] THU = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };

            private final LocalDate dangChon;
            private YearMonth thang;
            private LocalDate ketQua = null;

            private final JLabel lblThang = new JLabel("", SwingConstants.CENTER);
            private final JPanel pnlLuoi = new JPanel(new GridLayout(7, 7, 6, 6));

            LichDialog(Component cha, LocalDate batDau) {
                super(SwingUtilities.getWindowAncestor(cha), "Chọn ngày", Dialog.ModalityType.APPLICATION_MODAL);

                this.dangChon = batDau;
                this.thang = YearMonth.from(batDau != null ? batDau : LocalDate.now());

                setResizable(false);

                lblThang.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lblThang.setForeground(NAVY);

                JButton btnTruoc = nutNhat("◄");
                JButton btnSau = nutNhat("►");
                btnTruoc.addActionListener(e -> {
                    thang = thang.minusMonths(1);
                    veLich();
                });
                btnSau.addActionListener(e -> {
                    thang = thang.plusMonths(1);
                    veLich();
                });

                JPanel pnlDau = new JPanel(new BorderLayout());
                pnlDau.setOpaque(false);
                pnlDau.add(btnTruoc, BorderLayout.WEST);
                pnlDau.add(lblThang, BorderLayout.CENTER);
                pnlDau.add(btnSau, BorderLayout.EAST);

                pnlLuoi.setOpaque(false);
                pnlLuoi.setBorder(new EmptyBorder(14, 0, 10, 0));

                JButton btnHomNay = nutNhat("Hôm nay");
                JButton btnHuy = nutNhat("Hủy");
                btnHomNay.addActionListener(e -> {
                    ketQua = LocalDate.now();
                    dispose();
                });
                btnHuy.addActionListener(e -> dispose());

                JPanel pnlChan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                pnlChan.setOpaque(false);
                pnlChan.add(btnHomNay);
                pnlChan.add(btnHuy);

                JPanel pnlGoc = new JPanel(new BorderLayout());
                pnlGoc.setBackground(Color.WHITE);
                pnlGoc.setBorder(new EmptyBorder(14, 16, 14, 16));
                pnlGoc.add(pnlDau, BorderLayout.NORTH);
                pnlGoc.add(pnlLuoi, BorderLayout.CENTER);
                pnlGoc.add(pnlChan, BorderLayout.SOUTH);
                setContentPane(pnlGoc);

                veLich();
                setSize(360, 400);
                setLocationRelativeTo(cha);
            }

            LocalDate getKetQua() {
                return ketQua;
            }

            private static JButton nutNhat(String text) {
                JButton btn = new JButton(text);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
                btn.setForeground(NAVY);
                btn.setBackground(XANH_NHAT);
                btn.setOpaque(true);
                btn.setFocusPainted(false);
                btn.setBorder(new LineBorder(new Color(160, 190, 225), 1));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btn.setPreferredSize(new Dimension(Math.max(70, btn.getPreferredSize().width + 16), 32));
                return btn;
            }


            private void veLich() {
                lblThang.setText("Tháng " + thang.getMonthValue() + " / " + thang.getYear());
                pnlLuoi.removeAll();

                for (String t : THU) {
                    JLabel lbl = new JLabel(t, SwingConstants.CENTER);
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lbl.setForeground(NAVY);
                    pnlLuoi.add(lbl);
                }

                int lech = thang.atDay(1).getDayOfWeek().getValue() - 1;
                int soNgay = thang.lengthOfMonth();
                LocalDate homNay = LocalDate.now();

                for (int i = 0; i < 42; i++) {
                    int ngay = i - lech + 1;

                    if (ngay < 1 || ngay > soNgay) {
                        pnlLuoi.add(new JLabel());
                        continue;
                    }

                    LocalDate d = thang.atDay(ngay);

                    JButton btn = new JButton(String.valueOf(ngay));
                    btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    btn.setForeground(new Color(40, 40, 40));
                    btn.setBackground(Color.WHITE);
                    btn.setOpaque(true);
                    btn.setFocusPainted(false);
                    btn.setBorder(new LineBorder(VIEN_O, 1));
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    if (d.equals(homNay)) {
                        btn.setBackground(XANH_NHAT);
                        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    }
                    if (d.equals(dangChon)) {
                        btn.setBackground(NAVY);
                        btn.setForeground(Color.WHITE);
                        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    }

                    btn.addActionListener(e -> {
                        ketQua = d;
                        dispose();
                    });
                    pnlLuoi.add(btn);
                }

                pnlLuoi.revalidate();
                pnlLuoi.repaint();
            }
        }




        private static JPanel taoCot(String nhan, JComponent c) {
            JLabel lbl = new JLabel(nhan);
            lbl.setFont(F_NHAN);
            lbl.setForeground(XAM);

            JPanel pnl = new JPanel(new BorderLayout(0, 4));
            pnl.setOpaque(false);
            pnl.add(lbl, BorderLayout.NORTH);
            pnl.add(c, BorderLayout.CENTER);
            return pnl;
        }

        private static JPanel taoHaiCot(JComponent a, JComponent b) {
            JPanel pnl = new JPanel(new GridLayout(1, 2, 20, 0));
            pnl.setOpaque(false);
            pnl.add(a);
            pnl.add(b);
            return pnl;
        }

        private static JLabel taoMuc(String text) {
            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lbl.setForeground(new Color(20, 30, 45));
            return lbl;
        }

        private static void kieuO(JTextField txt) {
            txt.setFont(F14);
            txt.setBorder(new CompoundBorder(new LineBorder(VIEN, 1), new EmptyBorder(6, 10, 6, 10)));
            txt.setPreferredSize(new Dimension(100, 36));
        }

        private static JButton taoNut(String text, Color nen) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setForeground(Color.WHITE);
            btn.setBackground(nen);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(Math.max(110, btn.getPreferredSize().width + 10), 38));
            return btn;
        }

        private static void themVao(JPanel pnl, Component c) {
            if (c instanceof JComponent) {
                ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            pnl.add(c);
        }
    }
}
