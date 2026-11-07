package UI;

import UI.TrangChuUI.UIStyle;
import dao.KhachHangChiTietDAO;
import dao.KhachHangDAO;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;



public class QuanLyKhachHangUI extends JPanel {


        private static final String[] COLS = { "Mã KH", "Họ tên", "Số điện thoại", "Số thú cưng", "Tổng chi" };


        private final KhachHangDAO khachHangDAO = new KhachHangDAO();

        private final KhachHangChiTietDAO chiTietDAO = new KhachHangChiTietDAO();


        private final JTextField txtTim = UIStyle.o(16);

        private final JLabel lblDem = UIStyle.nhan("0 khách hàng");


        private final javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(COLS, 0) {




                @Override
                public boolean isCellEditable(int r, int c) {
                        return false;
                }
        };


        private final JTable tblDuLieu = new JTable(model);



        private ArrayList<KhachHang> dsHienThi = new ArrayList<>();



        private final CardLayout cardCT = new CardLayout();

        private final JPanel pnlKhungCT = new JPanel(cardCT);


        private final JLabel lblVMa = giaTri();

        private final JLabel lblVTen = giaTri();

        private final JLabel lblVSdt = giaTri();

        private final JLabel lblVTong = giaTri();

        private final JLabel lblVThuCung = new JLabel();


        public QuanLyKhachHangUI() {
                this(null);
        }



        public QuanLyKhachHangUI(String maNV) {
                setLayout(new BorderLayout(0, 14));
                setBackground(UIStyle.LIGHT_BLUE);
                setBorder(new EmptyBorder(18, 24, 18, 24));



                JLabel lblTieuDe2 = new JLabel("Quản lý khách hàng");

                lblTieuDe2.setFont(UIStyle.font(Font.BOLD, 26));
                lblTieuDe2.setForeground(new Color(40, 40, 40));


                JButton btnThem = UIStyle.nutXanh("+  Thêm khách hàng");


                btnThem.addActionListener(e -> moDialog(null));


                JPanel pnlDau2 = new JPanel(new BorderLayout());

                pnlDau2.setOpaque(false);
                pnlDau2.add(lblTieuDe2, BorderLayout.WEST);
                pnlDau2.add(btnThem, BorderLayout.EAST);



                JButton btnTim = UIStyle.nutChinh("Tìm kiếm");

                JButton btnSua = UIStyle.nutPhu("Sửa");

                JButton btnXoa = UIStyle.nutXoa("Xóa");

                JButton btnLamMoi = UIStyle.nutPhu("Làm mới");


                btnTim.addActionListener(e -> taiLai(maDangChon()));

                txtTim.addActionListener(e -> taiLai(maDangChon()));

                btnSua.addActionListener(e -> sua());

                btnXoa.addActionListener(e -> xoa());

                btnLamMoi.addActionListener(e -> lamMoiTatCa());


                JPanel pnlLoc2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

                pnlLoc2.setOpaque(false);
                pnlLoc2.add(UIStyle.nhan("Mã / tên / SĐT:"));
                pnlLoc2.add(txtTim);
                pnlLoc2.add(btnTim);
                pnlLoc2.add(btnSua);
                pnlLoc2.add(btnXoa);
                pnlLoc2.add(btnLamMoi);


                JPanel pnlHangTren = new JPanel(new BorderLayout(0, 8));

                pnlHangTren.setOpaque(false);
                pnlHangTren.add(pnlLoc2, BorderLayout.NORTH);
                pnlHangTren.add(lblDem, BorderLayout.SOUTH);


                UIStyle.taoBang(tblDuLieu, 6, 7);


                int[] w = { 70, 160, 110, 230, 120 };

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



                JLabel lblGoiY = new JLabel("Chọn một khách hàng trong bảng để xem thông tin chi tiết.");

                lblGoiY.setFont(UIStyle.font(Font.PLAIN, 13));
                lblGoiY.setForeground(Color.GRAY);


                JPanel pnlTrong = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));

                pnlTrong.setOpaque(false);
                pnlTrong.add(lblGoiY);

                pnlKhungCT.add(pnlTrong, "TRONG");
                pnlKhungCT.add(taoNoiDungChiTiet(), "CO");
                pnlKhungCT.setPreferredSize(new Dimension(100, 150));


                JPanel pnlChiTiet = new JPanel(new BorderLayout(0, 6));

                pnlChiTiet.setOpaque(false);
                pnlChiTiet.setBorder(new javax.swing.border.MatteBorder(1, 0, 0, 0, UIStyle.LINE));


                JLabel lblTdCT = UIStyle.nhan("Thông tin khách hàng");

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


                JPanel pnlLuoi2 = new JPanel(new GridLayout(2, 3, 18, 8));

                pnlLuoi2.setOpaque(false);

                pnlLuoi2.add(cap("Mã khách hàng", lblVMa));
                pnlLuoi2.add(cap("Họ tên", lblVTen));
                pnlLuoi2.add(cap("Số điện thoại", lblVSdt));
                pnlLuoi2.add(cap("Tổng chi / mua gần nhất", lblVTong));

                lblVThuCung.setFont(UIStyle.font(Font.PLAIN, 13));


                JPanel pnlPets = new JPanel(new BorderLayout(0, 2));

                pnlPets.setOpaque(false);


                JLabel lblNhan3 = new JLabel("Thú cưng");

                lblNhan3.setFont(UIStyle.font(Font.PLAIN, 12));
                lblNhan3.setForeground(Color.GRAY);

                pnlPets.add(lblNhan3, BorderLayout.NORTH);
                pnlPets.add(lblVThuCung, BorderLayout.CENTER);


                JPanel pnlKhung2 = new JPanel(new BorderLayout(0, 8));

                pnlKhung2.setOpaque(false);
                pnlKhung2.add(pnlLuoi2, BorderLayout.NORTH);
                pnlKhung2.add(pnlPets, BorderLayout.CENTER);

                return pnlKhung2;
        }







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



        public void lamMoi() {

                taiLai(maDangChon());
        }



        private void lamMoiTatCa() {


                txtTim.setText("");

                tblDuLieu.clearSelection();

                taiLai(null);
        }




        public void taiLai(String chonMa) {

                dsHienThi = khachHangDAO.timKiem(txtTim.getText());

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



        private void hienThongTin() {

                KhachHang k = dongDangChon();

                if (k == null) {

                        cardCT.show(pnlKhungCT, "TRONG");

                        return;
                }


                lblVMa.setText(k.getMaKhachHang());

                lblVTen.setText(k.getHoTen());

                lblVSdt.setText(k.getSoDienThoai());

                lblVTong.setText(UIStyle.tien(k.getTongChi())
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


                lblVThuCung.setText(sb.append("</body></html>").toString());

                cardCT.show(pnlKhungCT, "CO");
        }






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
                                + "(Cũng xóa thú cưng của khách này)")) {
                        return;
                }

                String loi = khachHangDAO.xoa(k.getMaKhachHang());

                if (loi != null) {

                        UIStyle.canhBao(this, loi);

                        return;
                }

                taiLai(null);
        }




        private void moDialog(KhachHang goc) {

                KhachHangDialog winDlg = new KhachHangDialog(
                                SwingUtilities.getWindowAncestor(this),
                                goc, khachHangDAO, chiTietDAO);

                winDlg.setVisible(true);

                if (winDlg.isDaLuu()) {
                        taiLai(winDlg.getMaDaLuu());
                }
        }






        public static class KhachHangDialog extends JDialog {



                private static final String[] LOAI = { "Ch\u00f3", "M\u00e8o", "Kh\u00e1c" };

                private static final String[] GIOI_TINH = { "\u0110\u1ef1c", "C\u00e1i" };


                private static final Color VIEN = new Color(110, 130, 160);

                private static final Color XAM = new Color(110, 120, 135);

                private static final Font F14 = new Font("Segoe UI", Font.PLAIN, 14);

                private static final Font NHAN = new Font("Segoe UI", Font.BOLD, 12);


                private final KhachHangDAO khachHangDAO;

                private final KhachHangChiTietDAO chiTietDAO;


                private final KhachHang goc;


                private boolean daLuu = false;

                private String maDaLuu = null;


                private final JTextField txtTen = new JTextField();

                private final JTextField txtSdt = new JTextField();

                private final JPanel pnlPetsBox = new JPanel();

                private final ArrayList<PetRow> rows = new ArrayList<>();






                public KhachHangDialog(Window winOwner, KhachHang goc,
                                KhachHangDAO khachHangDAO, KhachHangChiTietDAO chiTietDAO) {

                        super(winOwner, goc == null ? "Thêm Khách Hàng" : "Sửa Khách Hàng",
                                        Dialog.ModalityType.APPLICATION_MODAL);

                        this.goc = goc;
                        this.khachHangDAO = khachHangDAO;
                        this.chiTietDAO = chiTietDAO;

                        setSize(760, goc == null ? 490 : 330);
                        setResizable(false);
                        setLocationRelativeTo(winOwner);

                        kieu(txtTen);
                        kieu(txtSdt);


                        JPanel pnlBody = new JPanel();

                        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
                        pnlBody.setOpaque(false);
                        pnlBody.setBorder(new EmptyBorder(24, 40, 10, 40));

                        them(pnlBody, muc(goc == null ? "Thông tin Khách hàng"
                                        : "Thông tin Khách hàng (" + goc.getMaKhachHang() + ")"));
                        them(pnlBody, Box.createVerticalStrut(8));
                        them(pnlBody, hai(cot("Họ và tên: *", txtTen), cot("Số điện thoại: *", txtSdt)));

                        if (goc == null) {

                                pnlPetsBox.setLayout(new BoxLayout(pnlPetsBox, BoxLayout.Y_AXIS));
                                pnlPetsBox.setOpaque(false);


                                JPanel pnlPetsWrap = new JPanel(new BorderLayout());

                                pnlPetsWrap.setBackground(Color.WHITE);
                                pnlPetsWrap.add(pnlPetsBox, BorderLayout.NORTH);


                                JScrollPane scrSp = new JScrollPane(pnlPetsWrap);

                                scrSp.setBorder(new LineBorder(new Color(220, 226, 235)));
                                scrSp.setPreferredSize(new Dimension(100, 130));
                                scrSp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
                                scrSp.getVerticalScrollBar().setUnitIncrement(16);


                                JButton btnThemTc = nut("+ Thêm thú cưng", new Color(15, 42, 68));


                                btnThemTc.addActionListener(e -> themDongThuCung());

                                them(pnlBody, Box.createVerticalStrut(14));
                                them(pnlBody, muc("Thú cưng"));
                                them(pnlBody, Box.createVerticalStrut(6));
                                them(pnlBody, scrSp);
                                them(pnlBody, Box.createVerticalStrut(6));
                                them(pnlBody, btnThemTc);
                        }


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



                private static void kieuCbo(JComboBox<?> cmbC) {

                        cmbC.setFont(F14);
                        cmbC.setBackground(Color.WHITE);
                        cmbC.setPreferredSize(new Dimension(100, 36));
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




                private class PetRow extends JPanel {


                        final JTextField txtTen2 = new JTextField();

                        final JTextField txtGiong2 = new JTextField();

                        final JTextField txtCanNang2 = new JTextField();

                        final JComboBox<String> cmbLoai = new JComboBox<>(LOAI);

                        final JComboBox<String> cmbGioiTinh = new JComboBox<>(GIOI_TINH);


                        PetRow() {

                                super(new GridLayout(1, 6, 8, 0));

                                setOpaque(false);

                                setAlignmentX(Component.LEFT_ALIGNMENT);

                                setBorder(new EmptyBorder(4, 6, 4, 6));

                                kieu(txtTen2);
                                kieu(txtGiong2);
                                kieu(txtCanNang2);

                                kieuCbo(cmbLoai);
                                kieuCbo(cmbGioiTinh);


                                JButton btnXoa2 = nut("Xóa", new Color(210, 70, 70));

                                btnXoa2.setPreferredSize(new Dimension(60, 36));


                                btnXoa2.addActionListener(e -> {
                                        pnlPetsBox.remove(this);
                                        rows.remove(this);
                                        pnlPetsBox.revalidate();
                                        pnlPetsBox.repaint();
                                });


                                JPanel pnlXp = new JPanel(new BorderLayout(0, 4));

                                pnlXp.setOpaque(false);

                                pnlXp.add(new JLabel(" "), BorderLayout.NORTH);
                                pnlXp.add(btnXoa2, BorderLayout.CENTER);

                                add(cot("Tên: *", txtTen2));
                                add(cot("Loại:", cmbLoai));
                                add(cot("Giống:", txtGiong2));
                                add(cot("Giới tính:", cmbGioiTinh));
                                add(cot("Cân nặng (kg):", txtCanNang2));
                                add(pnlXp);
                        }
                }


                private void themDongThuCung() {

                        PetRow pnlKhung3 = new PetRow();

                        rows.add(pnlKhung3);

                        pnlPetsBox.add(pnlKhung3);

                        pnlPetsBox.revalidate();
                        pnlPetsBox.repaint();
                }





                private void loi(String msg) {

                        JOptionPane.showMessageDialog(this, msg, "Dữ liệu chưa hợp lệ",
                                        JOptionPane.WARNING_MESSAGE);
                }


                private void luu() {


                        String ten = txtTen.getText().trim();

                        String sdt = txtSdt.getText().replaceAll("\\s", "");

                        if (ten.isEmpty() || ten.length() > 50) {
                                loi("Họ tên bắt buộc nhập (tối đa 50 ký tự).");
                                return;
                        }

                        if (!sdt.matches("\\d{10,11}")) {
                                loi("Số điện thoại phải gồm 10 hoặc 11 chữ số.");
                                return;
                        }

                        boolean trung = goc == null
                                        ? khachHangDAO.tonTaiSdt(sdt)
                                        : chiTietDAO.tonTaiSdtKhac(sdt, goc.getMaKhachHang());

                        if (trung) {
                                loi("Số điện thoại " + sdt + " đã thuộc về khách hàng khác.");
                                return;
                        }

                        KhachHang kh = new KhachHang(goc == null ? null : goc.getMaKhachHang(), ten, sdt,
                                        null, null, 0);


                        if (goc != null) {

                                if (!chiTietDAO.sua(kh)) {
                                        loi("Lưu thất bại. Xem lỗi chi tiết trong Console.");
                                        return;
                                }

                                JOptionPane.showMessageDialog(this,
                                                "Đã cập nhật khách hàng " + goc.getMaKhachHang() + ".");

                                daLuu = true;
                                maDaLuu = goc.getMaKhachHang();

                                dispose();

                                return;
                        }



                        ArrayList<KhachHangDAO.ThuCungMoi> pets = new ArrayList<>();

                        for (PetRow pnlKhung3 : rows) {


                                String tenTC = pnlKhung3.txtTen2.getText().trim();

                                String giong = pnlKhung3.txtGiong2.getText().trim();

                                if (tenTC.isEmpty() || tenTC.length() > 50) {
                                        loi("Mỗi thú cưng cần có tên (bấm Xóa nếu không dùng).");
                                        return;
                                }

                                if (giong.length() > 25) {
                                        loi("Giống thú cưng tối đa 25 ký tự.");
                                        return;
                                }

                                Double kg = null;


                                String s = pnlKhung3.txtCanNang2.getText().trim().replace(',', '.');

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

                                pets.add(new KhachHangDAO.ThuCungMoi(tenTC,
                                                (String) pnlKhung3.cmbLoai.getSelectedItem(),
                                                giong, (String) pnlKhung3.cmbGioiTinh.getSelectedItem(), kg));
                        }


                        String ma = khachHangDAO.them(kh, pets);

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
