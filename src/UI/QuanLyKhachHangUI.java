package UI;

import UI.TrangChuUI.UIStyle;
import control.KhachHangControl;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Màn hình quản lý khách hàng.
 * UI chỉ: vẽ giao diện, lấy dữ liệu từ ô nhập, gọi KhachHangControl,
 * rồi hiện JOptionPane theo KhachHangControl.KetQua.
 */
public class QuanLyKhachHangUI extends JPanel {

    private static final String[] COLS = { "Mã KH", "Họ tên", "Số điện thoại", "Số thú cưng", "Tổng chi" };

    private final KhachHangControl control = new KhachHangControl();

    // --- Thanh tìm kiếm + bảng ---
    private final JTextField txtTim = UIStyle.o(16);
    private final JLabel lblDem = UIStyle.nhan("0 khách hàng");

    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tblDuLieu = new JTable(model);

    /** Danh sách đang hiển thị (dòng i của bảng ứng với phần tử i). */
    private ArrayList<KhachHang> dsHienThi = new ArrayList<>();

    // --- Khung thông tin chi tiết bên dưới bảng ---
    private final CardLayout cardChiTiet = new CardLayout();
    private final JPanel pnlKhungChiTiet = new JPanel(cardChiTiet);
    private final JLabel lblMa = taoNhanGiaTri();
    private final JLabel lblTen = taoNhanGiaTri();
    private final JLabel lblSdt = taoNhanGiaTri();
    private final JLabel lblTongChi = taoNhanGiaTri();
    private final JLabel lblThuCung = new JLabel();

    public QuanLyKhachHangUI() {
        this(null);
    }

    public QuanLyKhachHangUI(String maNV) {
        setLayout(new BorderLayout(0, 14));
        setBackground(UIStyle.LIGHT_BLUE);
        setBorder(new EmptyBorder(18, 24, 18, 24));

        caiDatBang();

        JPanel pnlThe = UIStyle.the();
        pnlThe.setLayout(new BorderLayout(0, 10));
        pnlThe.add(taoThanhTimKiem(), BorderLayout.NORTH);
        pnlThe.add(UIStyle.cuon(tblDuLieu), BorderLayout.CENTER);
        pnlThe.add(taoPhanChiTiet(), BorderLayout.SOUTH);

        add(taoTieuDe(), BorderLayout.NORTH);
        add(pnlThe, BorderLayout.CENTER);

        taiLai(null);
    }

    // =====================================================================
    // VẼ GIAO DIỆN
    // =====================================================================

    private JPanel taoTieuDe() {
        JLabel lblTieuDe = new JLabel("Quản lý khách hàng");
        lblTieuDe.setFont(UIStyle.font(Font.BOLD, 26));
        lblTieuDe.setForeground(new Color(40, 40, 40));

        JButton btnThem = UIStyle.nutXanh("+  Thêm khách hàng");
        btnThem.addActionListener(e -> moDialog(null));

        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.add(lblTieuDe, BorderLayout.WEST);
        pnl.add(btnThem, BorderLayout.EAST);
        return pnl;
    }

    private JPanel taoThanhTimKiem() {
        JButton btnTim = UIStyle.nutChinh("Tìm kiếm");
        JButton btnSua = UIStyle.nutPhu("Sửa");
        JButton btnXoa = UIStyle.nutXoa("Xóa");
        JButton btnLamMoi = UIStyle.nutPhu("Làm mới");

        btnTim.addActionListener(e -> taiLai(maDangChon()));
        txtTim.addActionListener(e -> taiLai(maDangChon()));
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnLamMoi.addActionListener(e -> lamMoiTatCa());

        JPanel pnlLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(UIStyle.nhan("Mã / tên / SĐT:"));
        pnlLoc.add(txtTim);
        pnlLoc.add(btnTim);
        pnlLoc.add(btnSua);
        pnlLoc.add(btnXoa);
        pnlLoc.add(btnLamMoi);

        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setOpaque(false);
        pnl.add(pnlLoc, BorderLayout.NORTH);
        pnl.add(lblDem, BorderLayout.SOUTH);
        return pnl;
    }

    private void caiDatBang() {
        UIStyle.taoBang(tblDuLieu, 6, 7);

        int[] doRong = { 70, 160, 110, 230, 120 };
        for (int i = 0; i < doRong.length; i++) {
            tblDuLieu.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }

        // Chọn dòng -> hiện chi tiết
        tblDuLieu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienThongTin();
            }
        });

        // Nhấp đúp -> sửa
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
        // Trang "chưa chọn ai"
        JLabel lblGoiY = new JLabel("Chọn một khách hàng trong bảng để xem thông tin chi tiết.");
        lblGoiY.setFont(UIStyle.font(Font.PLAIN, 13));
        lblGoiY.setForeground(Color.GRAY);

        JPanel pnlTrong = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        pnlTrong.setOpaque(false);
        pnlTrong.add(lblGoiY);

        // Trang "đã chọn": 4 ô thông tin + danh sách thú cưng
        JPanel pnlLuoi = new JPanel(new GridLayout(2, 3, 18, 8));
        pnlLuoi.setOpaque(false);
        pnlLuoi.add(taoCapThongTin("Mã khách hàng", lblMa));
        pnlLuoi.add(taoCapThongTin("Họ tên", lblTen));
        pnlLuoi.add(taoCapThongTin("Số điện thoại", lblSdt));
        pnlLuoi.add(taoCapThongTin("Tổng chi / mua gần nhất", lblTongChi));

        lblThuCung.setFont(UIStyle.font(Font.PLAIN, 13));

        JLabel lblNhanThuCung = new JLabel("Thú cưng");
        lblNhanThuCung.setFont(UIStyle.font(Font.PLAIN, 12));
        lblNhanThuCung.setForeground(Color.GRAY);

        JPanel pnlThuCung = new JPanel(new BorderLayout(0, 2));
        pnlThuCung.setOpaque(false);
        pnlThuCung.add(lblNhanThuCung, BorderLayout.NORTH);
        pnlThuCung.add(lblThuCung, BorderLayout.CENTER);

        JPanel pnlCo = new JPanel(new BorderLayout(0, 8));
        pnlCo.setOpaque(false);
        pnlCo.add(pnlLuoi, BorderLayout.NORTH);
        pnlCo.add(pnlThuCung, BorderLayout.CENTER);

        pnlKhungChiTiet.setOpaque(false);
        pnlKhungChiTiet.add(pnlTrong, "TRONG");
        pnlKhungChiTiet.add(pnlCo, "CO");
        pnlKhungChiTiet.setPreferredSize(new Dimension(100, 150));

        JLabel lblTieuDe = UIStyle.nhan("Thông tin khách hàng");
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

    /** Một ô gồm tiêu đề nhỏ màu xám và giá trị bên dưới. */
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

    // =====================================================================
    // HIỂN THỊ DỮ LIỆU
    // =====================================================================

    private KhachHang dongDangChon() {
        int row = tblDuLieu.getSelectedRow();
        if (row < 0 || row >= dsHienThi.size()) {
            return null;
        }
        return dsHienThi.get(row);
    }

    private String maDangChon() {
        KhachHang k = dongDangChon();
        return k == null ? null : k.getMaKhachHang();
    }

    /** Tải lại bảng theo từ khóa đang nhập; chọn lại dòng có mã chonMa (nếu có). */
    public void taiLai(String chonMa) {
        dsHienThi = control.timKiem(txtTim.getText());

        tblDuLieu.clearSelection();
        model.setRowCount(0);

        for (KhachHang k : dsHienThi) {
            model.addRow(new Object[] {
                    k.getMaKhachHang(),
                    k.getHoTen(),
                    k.getSoDienThoai(),
                    k.getThuCung() == null ? "0" : k.getThuCung(),
                    UIStyle.tien(k.getTongChi()) });
        }
        lblDem.setText(dsHienThi.size() + " khách hàng");

        if (chonMa != null) {
            for (int i = 0; i < dsHienThi.size(); i++) {
                if (dsHienThi.get(i).getMaKhachHang().equals(chonMa)) {
                    tblDuLieu.setRowSelectionInterval(i, i);
                    tblDuLieu.scrollRectToVisible(tblDuLieu.getCellRect(i, 0, true));
                    break;
                }
            }
        }
        hienThongTin();
    }

    public void lamMoi() {
        taiLai(maDangChon());
    }

    private void lamMoiTatCa() {
        txtTim.setText("");
        taiLai(null);
    }

    private void hienThongTin() {
        KhachHang k = dongDangChon();
        if (k == null) {
            cardChiTiet.show(pnlKhungChiTiet, "TRONG");
            return;
        }

        lblMa.setText(k.getMaKhachHang());
        lblTen.setText(k.getHoTen());
        lblSdt.setText(k.getSoDienThoai());
        lblTongChi.setText(UIStyle.tien(k.getTongChi())
                + (k.getGanNhat() == null ? "" : "  /  " + k.getGanNhat()));
        lblThuCung.setText(moTaThuCung(control.getThuCung(k.getMaKhachHang())));

        cardChiTiet.show(pnlKhungChiTiet, "CO");
    }

    /** Ghép danh sách thú cưng { tên, loại, giống, giới tính, cân nặng } thành 1 đoạn HTML ngắn. */
    private static String moTaThuCung(ArrayList<String[]> ds) {
        if (ds.isEmpty()) {
            return "Chưa có thú cưng nào.";
        }

        StringBuilder sb = new StringBuilder("<html><body style='width:760px'>");
        for (int i = 0; i < ds.size(); i++) {
            String[] p = ds.get(i);

            if (i > 0) {
                sb.append("; ");
            }
            sb.append("<b>").append(html(p[0])).append("</b> (").append(html(p[1]));

            if (p[2] != null && !p[2].isEmpty()) {
                sb.append(", ").append(html(p[2]));
            }
            if (p[3] != null && !p[3].isEmpty()) {
                sb.append(", ").append(html(p[3]));
            }
            if (!p[4].isEmpty()) {
                sb.append(", ").append(p[4]).append(" kg");
            }
            sb.append(")");
        }
        return sb.append("</body></html>").toString();
    }

    /** Tránh tên thú cưng có ký tự < > & làm hỏng HTML. */
    private static String html(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // =====================================================================
    // HÀNH ĐỘNG (sửa / xóa / mở hộp thoại) - dùng JOptionPane
    // =====================================================================

    private void sua() {
        KhachHang k = dongDangChon();
        if (k == null) {
            canhBao(this, "Hãy chọn một khách hàng trong bảng để sửa.");
            return;
        }
        moDialog(k);
    }

    private void xoa() {
        KhachHang k = dongDangChon();
        if (k == null) {
            canhBao(this, "Hãy chọn một khách hàng trong bảng để xóa.");
            return;
        }

        int chon = JOptionPane.showConfirmDialog(this,
                "Xóa khách hàng " + k.getMaKhachHang() + " - " + k.getHoTen() + "?\n"
                        + "(Cũng xóa thú cưng của khách này)",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }

        KhachHangControl.KetQua kq = control.xoa(k.getMaKhachHang());
        hienKetQua(this, kq);

        if (kq.isThanhCong()) {
            taiLai(null);
        }
    }

    /** goc = null: thêm mới; goc != null: sửa khách hàng đó. */
    private void moDialog(KhachHang goc) {
        KhachHangDialog dlg = new KhachHangDialog(SwingUtilities.getWindowAncestor(this), goc, control);
        dlg.setVisible(true);

        if (dlg.isDaLuu()) {
            taiLai(dlg.getMaDaLuu());
        }
    }

    private static void canhBao(Component cha, String noiDung) {
        JOptionPane.showMessageDialog(cha, noiDung, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    /** Hiện JOptionPane theo kết quả Control trả về: thành công -> thông tin, thất bại -> cảnh báo. */
    private static void hienKetQua(Component cha, KhachHangControl.KetQua kq) {
        if (kq.isThanhCong()) {
            JOptionPane.showMessageDialog(cha, kq.getThongBao(), "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(cha, kq.getThongBao(), "Dữ liệu chưa hợp lệ",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // =====================================================================
    // HỘP THOẠI THÊM / SỬA KHÁCH HÀNG
    // =====================================================================

    public static class KhachHangDialog extends JDialog {

        private static final String[] LOAI = { "Chó", "Mèo", "Khác" };
        private static final String[] GIOI_TINH = { "Đực", "Cái" };

        private static final Color VIEN = new Color(110, 130, 160);
        private static final Color XAM = new Color(110, 120, 135);
        private static final Color NAVY = new Color(15, 42, 68);
        private static final Font F14 = new Font("Segoe UI", Font.PLAIN, 14);
        private static final Font F_NHAN = new Font("Segoe UI", Font.BOLD, 12);

        private final KhachHangControl control;
        private final KhachHang goc; // null = đang thêm mới

        private boolean daLuu = false;
        private String maDaLuu = null;

        private final JTextField txtTen = new JTextField();
        private final JTextField txtSdt = new JTextField();

        private final JPanel pnlThuCung = new JPanel();          // chứa các dòng thú cưng
        private final ArrayList<PetRow> cacDong = new ArrayList<>();

        public KhachHangDialog(Window cha, KhachHang goc, KhachHangControl control) {
            super(cha, goc == null ? "Thêm Khách Hàng" : "Sửa Khách Hàng",
                    Dialog.ModalityType.APPLICATION_MODAL);
            this.goc = goc;
            this.control = control;

            setSize(760, goc == null ? 490 : 330);
            setResizable(false);
            setLocationRelativeTo(cha);

            kieuO(txtTen);
            kieuO(txtSdt);

            // --- Phần thân ---
            JPanel pnlThan = new JPanel();
            pnlThan.setLayout(new BoxLayout(pnlThan, BoxLayout.Y_AXIS));
            pnlThan.setOpaque(false);
            pnlThan.setBorder(new EmptyBorder(24, 40, 10, 40));

            themVao(pnlThan, taoMuc(goc == null ? "Thông tin Khách hàng"
                    : "Thông tin Khách hàng (" + goc.getMaKhachHang() + ")"));
            themVao(pnlThan, Box.createVerticalStrut(8));
            themVao(pnlThan, taoHaiCot(taoCot("Họ và tên: *", txtTen), taoCot("Số điện thoại: *", txtSdt)));

            if (goc == null) {
                taoPhanThuCung(pnlThan); // chỉ khi thêm mới mới nhập thú cưng
            }

            JPanel pnlTren = new JPanel(new BorderLayout());
            pnlTren.setOpaque(false);
            pnlTren.add(pnlThan, BorderLayout.NORTH);

            // --- Hàng nút ---
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
                txtTen.setText(goc.getHoTen());
                txtSdt.setText(goc.getSoDienThoai());
            }
        }

        public boolean isDaLuu() {
            return daLuu;
        }

        public String getMaDaLuu() {
            return maDaLuu;
        }

        // ---------------------------------------------------------------
        // Lưu: lấy dữ liệu từ ô nhập -> gọi Control -> hiện JOptionPane
        // ---------------------------------------------------------------
        private void luu() {
            KhachHangControl.KetQua kq;

            if (goc == null) {
                ArrayList<String[]> dsThuCung = new ArrayList<>();
                for (PetRow dong : cacDong) {
                    dsThuCung.add(dong.layDuLieu());
                }
                kq = control.them(txtTen.getText(), txtSdt.getText(), dsThuCung);
            } else {
                kq = control.sua(goc.getMaKhachHang(), txtTen.getText(), txtSdt.getText());
            }

            hienKetQua(this, kq);

            if (kq.isThanhCong()) {
                daLuu = true;
                maDaLuu = kq.getMaKhachHang();
                dispose();
            }
        }

        // ---------------------------------------------------------------
        // Phần nhập thú cưng (chỉ hiện khi thêm mới)
        // ---------------------------------------------------------------
        private void taoPhanThuCung(JPanel pnlThan) {
            pnlThuCung.setLayout(new BoxLayout(pnlThuCung, BoxLayout.Y_AXIS));
            pnlThuCung.setOpaque(false);

            JPanel pnlBoc = new JPanel(new BorderLayout());
            pnlBoc.setBackground(Color.WHITE);
            pnlBoc.add(pnlThuCung, BorderLayout.NORTH);

            JScrollPane scr = new JScrollPane(pnlBoc);
            scr.setBorder(new LineBorder(new Color(220, 226, 235)));
            scr.setPreferredSize(new Dimension(100, 130));
            scr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
            scr.getVerticalScrollBar().setUnitIncrement(16);

            JButton btnThemThuCung = taoNut("+ Thêm thú cưng", NAVY);
            btnThemThuCung.addActionListener(e -> themDongThuCung());

            themVao(pnlThan, Box.createVerticalStrut(14));
            themVao(pnlThan, taoMuc("Thú cưng"));
            themVao(pnlThan, Box.createVerticalStrut(6));
            themVao(pnlThan, scr);
            themVao(pnlThan, Box.createVerticalStrut(6));
            themVao(pnlThan, btnThemThuCung);
        }

        private void themDongThuCung() {
            PetRow dong = new PetRow();
            cacDong.add(dong);
            pnlThuCung.add(dong);
            pnlThuCung.revalidate();
            pnlThuCung.repaint();
        }

        /** Một dòng nhập thú cưng. */
        private class PetRow extends JPanel {

            private final JTextField txtTenTC = new JTextField();
            private final JTextField txtGiong = new JTextField();
            private final JTextField txtCanNang = new JTextField();
            private final JComboBox<String> cmbLoai = new JComboBox<>(LOAI);
            private final JComboBox<String> cmbGioiTinh = new JComboBox<>(GIOI_TINH);

            PetRow() {
                super(new GridLayout(1, 6, 8, 0));
                setOpaque(false);
                setAlignmentX(Component.LEFT_ALIGNMENT);
                setBorder(new EmptyBorder(4, 6, 4, 6));

                kieuO(txtTenTC);
                kieuO(txtGiong);
                kieuO(txtCanNang);
                kieuCombo(cmbLoai);
                kieuCombo(cmbGioiTinh);

                JButton btnXoa = taoNut("Xóa", new Color(210, 70, 70));
                btnXoa.setPreferredSize(new Dimension(60, 36));
                btnXoa.addActionListener(e -> {
                    pnlThuCung.remove(this);
                    cacDong.remove(this);
                    pnlThuCung.revalidate();
                    pnlThuCung.repaint();
                });

                JPanel pnlXoa = new JPanel(new BorderLayout(0, 4));
                pnlXoa.setOpaque(false);
                pnlXoa.add(new JLabel(" "), BorderLayout.NORTH);
                pnlXoa.add(btnXoa, BorderLayout.CENTER);

                add(taoCot("Tên: *", txtTenTC));
                add(taoCot("Loại:", cmbLoai));
                add(taoCot("Giống:", txtGiong));
                add(taoCot("Giới tính:", cmbGioiTinh));
                add(taoCot("Cân nặng (kg):", txtCanNang));
                add(pnlXoa);
            }

            /** Dữ liệu thô của dòng: { tên, loại, giống, giới tính, cân nặng } (đúng thứ tự Control cần). */
            String[] layDuLieu() {
                return new String[] {
                        txtTenTC.getText(),
                        (String) cmbLoai.getSelectedItem(),
                        txtGiong.getText(),
                        (String) cmbGioiTinh.getSelectedItem(),
                        txtCanNang.getText() };
            }
        }

        // ---------------------------------------------------------------
        // Hàm dựng giao diện nhỏ
        // ---------------------------------------------------------------
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

        private static void kieuCombo(JComboBox<?> cmb) {
            cmb.setFont(F14);
            cmb.setBackground(Color.WHITE);
            cmb.setPreferredSize(new Dimension(100, 36));
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
