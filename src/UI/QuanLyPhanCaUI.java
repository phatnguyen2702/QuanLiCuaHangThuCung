package UI;

import control.PhanCaControl;
import entity.LichPhanCa;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import javax.swing.table.TableCellRenderer;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;

public class QuanLyPhanCaUI extends JFrame implements ActionListener, MouseListener {

        private static final Color NAVY = new Color(18, 48, 82);

        private static final Color LIGHT_BLUE = new Color(229, 241, 252);

        private static final Color BG = new Color(245, 247, 250);

        private static final Color GREEN = new Color(72, 196, 62);

        private static final Color RED = new Color(210, 70, 70);

        private static final Color BLUE = new Color(91, 180, 235);

        private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                        .withResolverStyle(ResolverStyle.STRICT);

        private final PhanCaControl control = new PhanCaControl();

        private final Font fntNoiDung = new Font("Segoe UI", Font.PLAIN, 12);

        private JTextField txtMaPhanCa, txtNgayLamViec, txtHoTen, txtLocNgay;

        private JComboBox<String> cmbNhanVien, cmbCa, cmbLocNhanVien, cmbLocCa;

        private JTable tblPhanCa;

        private DefaultTableModel mdlPhanCa;

        private JButton btnSua;

        private JButton btnChonNgay, btnXoaLocNgay, btnLoc;
        private JButton btnXoa, btnExcel, btnLamMoiDanhSach;
        private JButton btnXemTuan, btnSaoChepTuan;
        private JButton btnLich, btnThem, btnBoChon;

        private JLabel lblNgayThongKe, lblSoNhanVienHomNay, lblSoCaHomNay;
        private JButton btnLamMoiThongKe;

        private String maDangSua;

        public QuanLyPhanCaUI() {
                setLayout(new BorderLayout());
                setBackground(BG);
                add(taoPnlTieuDe(), BorderLayout.NORTH);
                add(taoPnlNoiDung(), BorderLayout.CENTER);
                taiDanhMuc();
                taiDuLieu();
                lamMoiForm();
        }

        private JPanel taoPnlTieuDe() {

                JPanel pnlPnl = new JPanel(new BorderLayout());
                pnlPnl.setBackground(BG);
                pnlPnl.setBorder(new EmptyBorder(14, 24, 12, 24));

                JLabel lblTitle = new JLabel("PHÂN CA NHÂN VIÊN");
                lblTitle.setForeground(NAVY);
                lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
                pnlPnl.add(lblTitle, BorderLayout.WEST);
                return pnlPnl;
        }

        private JPanel taoPnlNoiDung() {

                JPanel pnlPnl = new JPanel(new BorderLayout(15, 0));
                pnlPnl.setBackground(LIGHT_BLUE);
                pnlPnl.setBorder(new EmptyBorder(12, 18, 15, 18));
                pnlPnl.add(taoPnlDanhSach(), BorderLayout.CENTER);
                pnlPnl.add(taoPnlBenPhai(), BorderLayout.EAST);
                return pnlPnl;
        }

        private JPanel taoPnlDanhSach() {

                JPanel pnlPnl = new JPanel(new BorderLayout(0, 9));
                pnlPnl.setBackground(Color.WHITE);
                pnlPnl.setBorder(new EmptyBorder(10, 10, 10, 10));
                pnlPnl.add(taoPnlLoc(), BorderLayout.NORTH);

                mdlPhanCa = new DefaultTableModel(new String[] {
                                "Mã PC", "Ngày làm việc", "Mã NV", "Họ tên", "Mã ca", "Ca làm"
                }, 0) {

                        @Override
                        public boolean isCellEditable(int row, int col) {
                                return false;
                        }
                };
                tblPhanCa = new JTable(mdlPhanCa);
                tblPhanCa.addMouseListener(this);
                tblPhanCa.setRowHeight(30);
                tblPhanCa.setFont(fntNoiDung);
                tblPhanCa.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                tblPhanCa.setAutoCreateRowSorter(true);
                tblPhanCa.setSelectionBackground(new Color(207, 222, 235));
                tblPhanCa.setFillsViewportHeight(true);
                tblPhanCa.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
                tblPhanCa.getTableHeader().setBackground(NAVY);
                tblPhanCa.getTableHeader().setForeground(Color.WHITE);
                tblPhanCa.getTableHeader().setOpaque(true);
                tblPhanCa.getTableHeader().setReorderingAllowed(false);
                DefaultTableCellRenderer lblCenter = new DefaultTableCellRenderer();
                lblCenter.setHorizontalAlignment(SwingConstants.CENTER);
                for (int i : new int[] { 0, 1, 2, 4 })
                        tblPhanCa.getColumnModel().getColumn(i).setCellRenderer(lblCenter);

                int[] doRong = { 65, 105, 75, 140, 65, 170 };
                for (int i = 0; i < doRong.length; i++)
                        tblPhanCa.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
                tblPhanCa.getSelectionModel().addListSelectionListener(e -> {
                        if (!e.getValueIsAdjusting() && tblPhanCa.getSelectedRow() >= 0)
                                hienThiForm();
                });

                JScrollPane scrScr = new JScrollPane(tblPhanCa);
                scrScr.setBorder(new LineBorder(new Color(220, 225, 230)));
                pnlPnl.add(scrScr, BorderLayout.CENTER);
                pnlPnl.add(taoPnlNutDuoi(), BorderLayout.SOUTH);
                return pnlPnl;
        }

        private JPanel taoPnlLoc() {

                JPanel pnlPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 4));
                pnlPnl.setBackground(Color.WHITE);
                txtLocNgay = new JTextField(9);
                txtLocNgay.setEditable(false);
                txtLocNgay.setFont(fntNoiDung);
                txtLocNgay.setToolTipText("Có thể lọc cả các ngày trong quá khứ");

                btnChonNgay = taoNut("Lịch", NAVY);

                ganHanhDong(btnChonNgay, () -> {
                        LocalDate ngay = LichChonNgay.chonNgay(this,
                                        PhanCaControl.docNgay(txtLocNgay.getText()), false);
                        if (ngay != null)
                                txtLocNgay.setText(ngay.format(DINH_DANG_NGAY));
                });

                btnXoaLocNgay = taoNut("×", new Color(108, 117, 125));
                btnXoaLocNgay.setToolTipText("Bỏ lọc theo ngày");

                ganHanhDong(btnXoaLocNgay, () -> txtLocNgay.setText(""));

                cmbLocNhanVien = new JComboBox<>();
                cmbLocNhanVien.setPreferredSize(new Dimension(145, 30));
                cmbLocCa = new JComboBox<>();
                cmbLocCa.setPreferredSize(new Dimension(110, 30));

                btnLoc = taoNut("Lọc", GREEN);

                ganHanhDong(btnLoc, this::locDuLieu);

                pnlPnl.add(new JLabel("Ngày làm việc"));
                pnlPnl.add(txtLocNgay);
                pnlPnl.add(btnChonNgay);
                pnlPnl.add(btnXoaLocNgay);
                pnlPnl.add(new JLabel("Nhân viên"));
                pnlPnl.add(cmbLocNhanVien);
                pnlPnl.add(new JLabel("Ca"));
                pnlPnl.add(cmbLocCa);
                pnlPnl.add(btnLoc);
                return pnlPnl;
        }

        private JPanel taoPnlNutDuoi() {

                JPanel pnlPnl = new JPanel(new BorderLayout());
                pnlPnl.setBackground(Color.WHITE);

                JPanel pnlTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                pnlTrai.setOpaque(false);

                btnXoa = taoNut("Xóa phân ca", RED);

                ganHanhDong(btnXoa, this::xoa);

                btnExcel = taoNut("Xuất lịch phân ca", new Color(29, 133, 82));

                ganHanhDong(btnExcel, this::xuatExcel);
                btnXemTuan = taoNut("Xem lịch tuần", NAVY);
                ganHanhDong(btnXemTuan, this::xemLichTheoTuan);
                btnXemTuan.setToolTipText("Xem toàn bộ lịch nhân viên theo thứ Hai – Chủ nhật");
                btnSaoChepTuan = taoNut("Sao chép tuần", new Color(80, 112, 156));
                ganHanhDong(btnSaoChepTuan, this::hienThiSaoChepTuan);
                btnSaoChepTuan.setToolTipText("Sao chép lịch từ một tuần sang tuần khác, không ghi đè");
                pnlTrai.add(btnXoa);
                pnlTrai.add(btnExcel);
                pnlTrai.add(btnXemTuan);
                pnlTrai.add(btnSaoChepTuan);
                pnlPnl.add(pnlTrai, BorderLayout.WEST);

                btnLamMoiDanhSach = taoNut("Làm mới", BLUE);

                ganHanhDong(btnLamMoiDanhSach, () -> {
                        taiDanhMuc();
                        taiDuLieu();
                        lamMoiForm();
                });
                pnlPnl.add(btnLamMoiDanhSach, BorderLayout.EAST);
                return pnlPnl;
        }

        private JPanel taoPnlBenPhai() {
                JPanel pnlPnl = new JPanel(new BorderLayout());
                pnlPnl.setBackground(Color.WHITE);
                pnlPnl.setPreferredSize(new Dimension(310, 0));

                JPanel phanTren = new JPanel(new BorderLayout(0, 12));
                phanTren.setOpaque(false);
                phanTren.add(taoPnlForm(), BorderLayout.NORTH);
                phanTren.add(taoPnlThongKeHomNay(), BorderLayout.CENTER);
                pnlPnl.add(phanTren, BorderLayout.NORTH);
                return pnlPnl;
        }

        private JPanel taoPnlForm() {

                JPanel pnlPnl = new JPanel();
                pnlPnl.setLayout(new BoxLayout(pnlPnl, BoxLayout.Y_AXIS));
                pnlPnl.setBackground(Color.WHITE);
                pnlPnl.setBorder(new EmptyBorder(10, 14, 12, 14));

                JLabel lblTitle = new JLabel("TẠO / SỬA PHÂN CA");
                lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblTitle.setForeground(NAVY);
                pnlPnl.add(lblTitle);
                pnlPnl.add(Box.createVerticalStrut(8));

                txtMaPhanCa = new JTextField();
                txtMaPhanCa.setEditable(false);
                themTruong(pnlPnl, "Mã phân ca", txtMaPhanCa);

                txtNgayLamViec = new JTextField();
                txtNgayLamViec.setEditable(false);
                txtNgayLamViec.setBackground(Color.WHITE);

                JPanel pnlChonNgay = new JPanel(new BorderLayout(4, 0));
                pnlChonNgay.setBackground(Color.WHITE);
                pnlChonNgay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));

                btnLich = taoNut("Chọn lịch", NAVY);
                btnLich.setMargin(new Insets(2, 6, 2, 6));

                ganHanhDong(btnLich, () -> {
                        LocalDate ngay = LichChonNgay.chonNgay(this,
                                        PhanCaControl.docNgay(txtNgayLamViec.getText()), true);
                        if (ngay != null)
                                txtNgayLamViec.setText(ngay.format(DINH_DANG_NGAY));
                });
                pnlChonNgay.add(txtNgayLamViec, BorderLayout.CENTER);
                pnlChonNgay.add(btnLich, BorderLayout.EAST);
                themTruong(pnlPnl, "Ngày làm việc (từ hôm nay)", pnlChonNgay);

                cmbNhanVien = new JComboBox<>();

                cmbNhanVien.addActionListener(e -> capNhatHoTen());
                themTruong(pnlPnl, "Nhân viên", cmbNhanVien);
                txtHoTen = new JTextField();
                txtHoTen.setEditable(false);
                themTruong(pnlPnl, "Họ tên", txtHoTen);
                cmbCa = new JComboBox<>();
                themTruong(pnlPnl, "Ca làm việc", cmbCa);

                JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 0));
                pnlNut.setOpaque(false);

                btnThem = taoNut("Thêm +", GREEN);

                ganHanhDong(btnThem, this::them);
                btnSua = taoNut("Sửa", NAVY);
                btnSua.setEnabled(false);

                ganHanhDong(btnSua, this::sua);

                btnBoChon = taoNut("Bỏ chọn", new Color(108, 117, 125));

                ganHanhDong(btnBoChon, this::lamMoiForm);
                pnlNut.add(btnThem);
                pnlNut.add(btnSua);
                pnlNut.add(btnBoChon);
                pnlNut.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlPnl.add(Box.createVerticalStrut(8));
                pnlPnl.add(pnlNut);
                pnlPnl.add(Box.createVerticalStrut(6));
                return pnlPnl;
        }

        private JPanel taoPnlThongKeHomNay() {
                JPanel khung = new JPanel(new BorderLayout(0, 12));
                khung.setBackground(Color.WHITE);
                khung.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(215, 226, 237)),
                                new EmptyBorder(12, 14, 14, 14)));

                JPanel hangTieuDe = new JPanel(new BorderLayout(6, 0));
                hangTieuDe.setOpaque(false);
                JLabel tieuDe = new JLabel("Tổng quan phân ca hôm nay");
                tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 14));
                tieuDe.setForeground(NAVY);
                hangTieuDe.add(tieuDe, BorderLayout.CENTER);

                btnLamMoiThongKe = new JButton("↻");
                btnLamMoiThongKe.setFocusable(false);
                btnLamMoiThongKe.setToolTipText("Cập nhật thống kê phân ca hôm nay từ SQL Server");
                btnLamMoiThongKe.setMargin(new Insets(1, 5, 1, 5));
                ganHanhDong(btnLamMoiThongKe, this::taiThongKeHomNay);
                hangTieuDe.add(btnLamMoiThongKe, BorderLayout.EAST);

                lblNgayThongKe = new JLabel();
                lblNgayThongKe.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblNgayThongKe.setForeground(new Color(91, 105, 120));

                JPanel phanDau = new JPanel(new BorderLayout(0, 7));
                phanDau.setOpaque(false);
                phanDau.add(hangTieuDe, BorderLayout.NORTH);
                phanDau.add(lblNgayThongKe, BorderLayout.SOUTH);
                khung.add(phanDau, BorderLayout.NORTH);

                lblSoNhanVienHomNay = new JLabel("0");
                lblSoCaHomNay = new JLabel("0");
                JPanel luoiChiSo = new JPanel(new GridLayout(1, 2, 10, 0));
                luoiChiSo.setOpaque(false);
                luoiChiSo.add(taoTheChiSoPhanCa("Nhân viên", lblSoNhanVienHomNay));
                luoiChiSo.add(taoTheChiSoPhanCa("Ca làm", lblSoCaHomNay));
                khung.add(luoiChiSo, BorderLayout.CENTER);
                return khung;
        }

        private JPanel taoTheChiSoPhanCa(String nhan, JLabel giaTri) {
                JPanel the = new JPanel(new BorderLayout(0, 6));
                the.setBackground(new Color(245, 248, 252));
                the.setBorder(new EmptyBorder(10, 12, 10, 12));
                JLabel tieuDe = new JLabel(nhan);
                tieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                tieuDe.setForeground(new Color(52, 65, 81));
                giaTri.setFont(new Font("Segoe UI", Font.BOLD, 23));
                giaTri.setForeground(NAVY);
                the.add(tieuDe, BorderLayout.NORTH);
                the.add(giaTri, BorderLayout.CENTER);
                return the;
        }

        private void taiThongKeHomNay() {
                LocalDate homNay = LocalDate.now();
                lblNgayThongKe.setText("Ngày " + homNay.format(DINH_DANG_NGAY));
                try {
                        PhanCaControl.ThongKe tk = control.thongKeNgay(homNay);
                        lblSoNhanVienHomNay.setText(String.valueOf(tk.soNhanVien()));
                        lblSoCaHomNay.setText(String.valueOf(tk.soCa()));
                } catch (Exception ex) {
                        lblSoNhanVienHomNay.setText("—");
                        lblSoCaHomNay.setText("—");
                        ex.printStackTrace();
                }
        }

        private void themTruong(JPanel pnlPnl, String nhan, JComponent control) {

                JLabel lblLbl = new JLabel(nhan);
                lblLbl.setFont(fntNoiDung);
                lblLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlPnl.add(lblLbl);
                pnlPnl.add(Box.createVerticalStrut(3));
                control.setFont(fntNoiDung);
                control.setAlignmentX(Component.LEFT_ALIGNMENT);
                control.setMaximumSize(new Dimension(10000, 29));
                control.setPreferredSize(new Dimension(225, 29));
                pnlPnl.add(control);
                pnlPnl.add(Box.createVerticalStrut(6));
        }

        private void taiDanhMuc() {

                String maNV = maDaChon(cmbNhanVien);

                String maCa = maDaChon(cmbCa);
                if (cmbNhanVien != null) {
                        cmbNhanVien.removeAllItems();
                        cmbLocNhanVien.removeAllItems();
                        cmbLocNhanVien.addItem("Tất cả");
                        for (String[] nv : control.layNhanVien()) {
                                String item = nv[0] + " - " + nv[1];
                                cmbNhanVien.addItem(item);
                                cmbLocNhanVien.addItem(item);
                        }
                }
                if (cmbCa != null) {
                        cmbCa.removeAllItems();
                        cmbLocCa.removeAllItems();
                        cmbLocCa.addItem("Tất cả");
                        for (String[] ca : control.layCaLam()) {
                                cmbCa.addItem(ca[0] + " - " + ca[1] + ": " + ca[2] + "–" + ca[3]);
                                cmbLocCa.addItem(ca[0] + " - " + ca[1]);
                        }
                }
                chonTheoMa(cmbNhanVien, maNV);
                chonTheoMa(cmbCa, maCa);
                capNhatHoTen();
        }

        private void taiDuLieu() {
                hienThiDuLieu(control.layDanhSach());
                taiThongKeHomNay();
        }

        private void hienThiDuLieu(List<LichPhanCa> ds) {
                mdlPhanCa.setRowCount(0);
                for (LichPhanCa pc : ds) {
                        String strNgay = pc.getNgayLamViec() == null ? "" : pc.getNgayLamViec().format(DINH_DANG_NGAY);

                        String thongTinCa = pc.getThongTinCa();
                        mdlPhanCa.addRow(new Object[] { pc.getMaPhanCa(), strNgay,
                                        pc.getMaNhanVien(), pc.getHoTen(), pc.getMaCa(), thongTinCa });
                }
        }

        private void hienThiForm() {

                int row = tblPhanCa.getSelectedRow();
                if (row < 0)
                        return;
                int modelRow = tblPhanCa.convertRowIndexToModel(row);
                maDangSua = String.valueOf(mdlPhanCa.getValueAt(modelRow, 0));

                txtMaPhanCa.setText(maDangSua);

                txtNgayLamViec.setText(String.valueOf(mdlPhanCa.getValueAt(modelRow, 1)));
                chonTheoMa(cmbNhanVien, String.valueOf(mdlPhanCa.getValueAt(modelRow, 2)));
                chonTheoMa(cmbCa, String.valueOf(mdlPhanCa.getValueAt(modelRow, 4)));
                btnSua.setEnabled(true);
        }

        private void chonTheoMa(JComboBox<String> cmbCbo, String ma) {
                if (cmbCbo == null || ma == null)
                        return;
                for (int i = 0; i < cmbCbo.getItemCount(); i++) {
                        if (cmbCbo.getItemAt(i).startsWith(ma + " - ")) {
                                cmbCbo.setSelectedIndex(i);
                                return;
                        }
                }
        }

        private String maDaChon(JComboBox<String> cmbCbo) {
                if (cmbCbo == null || cmbCbo.getSelectedItem() == null)
                        return "";
                return PhanCaControl.maDaChon(cmbCbo.getSelectedItem().toString());
        }

        private void capNhatHoTen() {
                if (txtHoTen == null)
                        return;

                String selected = cmbNhanVien.getSelectedItem() == null ? "" : cmbNhanVien.getSelectedItem().toString();
                int pos = selected.indexOf(" - ");

                txtHoTen.setText(pos >= 0 ? selected.substring(pos + 3) : "");
        }

        private LichPhanCa layDuLieuForm(boolean sua) {
                try {
                        return control.taoPhanCa(maDangSua, sua,
                                        maDaChon(cmbNhanVien), maDaChon(cmbCa), txtNgayLamViec.getText());
                } catch (IllegalArgumentException ex) {
                        canhBao(ex.getMessage());
                        return null;
                }
        }

        private void them() {
                LichPhanCa pc = layDuLieuForm(false);
                if (pc == null)
                        return;
                try {
                        if (control.them(pc)) {
                                thongBao("Thêm phân ca thành công.");
                                taiDuLieu();
                                lamMoiForm();
                        } else
                                canhBao("Không thể thêm phân ca. Kiểm tra kết nối hoặc mã đã tồn tại.");
                } catch (IllegalArgumentException ex) {
                        canhBao(ex.getMessage());
                } catch (Exception ex) {
                        baoLoi(ex);
                }
        }

        private void sua() {
                LichPhanCa pc = layDuLieuForm(true);
                if (pc == null)
                        return;
                try {
                        if (control.sua(pc)) {
                                thongBao("Cập nhật phân ca " + pc.getMaPhanCa() + " thành công.");
                                taiDuLieu();
                                lamMoiForm();
                        } else
                                canhBao("Không thể sửa. Không cho sửa phân ca của ngày đã qua hoặc mã không tồn tại.");
                } catch (IllegalArgumentException ex) {
                        canhBao(ex.getMessage());
                } catch (Exception ex) {
                        baoLoi(ex);
                }
        }

        private void xoa() {

                int row = tblPhanCa.getSelectedRow();
                if (row < 0) {
                        canhBao("Vui lòng chọn phân ca cần xóa.");
                        return;
                }
                int modelRow = tblPhanCa.convertRowIndexToModel(row);

                String ma = String.valueOf(mdlPhanCa.getValueAt(modelRow, 0));
                if (JOptionPane.showConfirmDialog(this, "Xóa phân ca " + ma + "?",
                                "Xác nhận xóa", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;
                if (control.xoa(ma)) {
                        thongBao("Đã xóa phân ca " + ma + ".");
                        taiDuLieu();
                        lamMoiForm();
                } else
                        canhBao("Xóa phân ca thất bại.");
        }

        private void locDuLieu() {
                String maNV = maDaChon(cmbLocNhanVien);
                String maCa = maDaChon(cmbLocCa);
                String ngayNhap = txtLocNgay.getText().trim();
                LocalDate ngay = PhanCaControl.docNgay(ngayNhap);
                if (!ngayNhap.isEmpty() && ngay == null) {
                        canhBao("Ngày lọc không hợp lệ. Hãy chọn lại từ lịch (dd/MM/yyyy).");
                        txtLocNgay.requestFocusInWindow();
                        return;
                }
                hienThiDuLieu(control.loc(ngay, ngay, maNV, maCa));
                lamMoiFormKhongXoaBoLoc();
        }

        private void lamMoiFormKhongXoaBoLoc() {
                maDangSua = null;

                txtMaPhanCa.setText(control.taoMaPhanCa());

                txtNgayLamViec.setText(LocalDate.now().format(DINH_DANG_NGAY));
                tblPhanCa.clearSelection();
                if (btnSua != null)
                        btnSua.setEnabled(false);
        }

        private void lamMoiForm() {
                lamMoiFormKhongXoaBoLoc();
                if (cmbNhanVien.getItemCount() > 0)
                        cmbNhanVien.setSelectedIndex(0);
                if (cmbCa.getItemCount() > 0)
                        cmbCa.setSelectedIndex(0);
                if (txtLocNgay != null)

                        txtLocNgay.setText("");
                if (cmbLocNhanVien.getItemCount() > 0)
                        cmbLocNhanVien.setSelectedIndex(0);
                if (cmbLocCa.getItemCount() > 0)
                        cmbLocCa.setSelectedIndex(0);
        }

        private void xuatExcel() {
                String[] tuyChon = { "Theo ngày", "Theo tuần", "Hủy" };
                int kieu = JOptionPane.showOptionDialog(this,
                                "Chọn phạm vi xuất lịch phân ca:",
                                "Xuất lịch phân ca Excel",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                                null, tuyChon, tuyChon[0]);
                if (kieu != 0 && kieu != 1)
                        return;

                boolean theoTuan = kieu == 1;
                LocalDate ngayDangLoc = PhanCaControl.docNgay(txtLocNgay.getText());
                LocalDate ngayChon;

                if (!theoTuan && ngayDangLoc != null) {

                        ngayChon = ngayDangLoc;
                } else {

                        ngayChon = LichChonNgay.chonNgay(this,
                                        ngayDangLoc != null ? ngayDangLoc : LocalDate.now(), false);
                        if (ngayChon == null)
                                return;
                        if (!theoTuan)
                                txtLocNgay.setText(ngayChon.format(DINH_DANG_NGAY));
                }

                LocalDate tuNgay = theoTuan ? PhanCaControl.dauTuan(ngayChon) : ngayChon;
                LocalDate denNgay = theoTuan ? tuNgay.plusDays(6) : tuNgay;
                String phamVi = theoTuan
                                ? "tuần " + PhanCaControl.khoangTuan(tuNgay)
                                : "ngày " + tuNgay.format(DINH_DANG_NGAY);

                String maNV = maDaChon(cmbLocNhanVien);
                String maCa = maDaChon(cmbLocCa);
                List<String[]> rows;
                try {
                        rows = control.duLieuExcel(tuNgay, denNgay, maNV, maCa);
                } catch (RuntimeException ex) {
                        baoLoi(ex);
                        return;
                }

                if (rows.isEmpty()) {
                        canhBao("Không có phân ca để xuất trong " + phamVi + ".");
                        return;
                }

                String tenTep = theoTuan
                                ? "LichPhanCa_Tuan_" + tuNgay + "_den_" + denNgay + ".xlsx"
                                : "LichPhanCa_Ngay_" + tuNgay + ".xlsx";
                JFileChooser chonTep = new JFileChooser();
                chonTep.setDialogTitle("Xuất lịch phân ca " + phamVi);
                chonTep.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));
                chonTep.setSelectedFile(new File(tenTep));
                if (chonTep.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
                        return;

                File tep = chonTep.getSelectedFile();
                if (!tep.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".xlsx"))
                        tep = new File(tep.getParentFile(), tep.getName() + ".xlsx");
                if (tep.exists() && JOptionPane.showConfirmDialog(this,
                                "Tệp đã tồn tại. Bạn có muốn ghi đè?", "Xác nhận",
                                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;

                try {
                        control.xuatExcel(tep.toPath(), rows);
                        thongBao("Đã xuất " + rows.size() + " phân ca trong " + phamVi
                                        + " sang Excel:\n" + tep.getAbsolutePath());
                } catch (IOException ex) {
                        baoLoi(ex);
                }
        }

        private void xemLichTheoTuan() {
                Window cha = SwingUtilities.getWindowAncestor(this);
                JDialog dialog = new JDialog(cha, "Lịch phân ca theo tuần", Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setLayout(new BorderLayout(10, 10));
                ((JPanel) dialog.getContentPane()).setBorder(new EmptyBorder(14, 14, 12, 14));

                LocalDate daLoc = PhanCaControl.docNgay(txtLocNgay.getText());
                LocalDate[] tuan = { PhanCaControl.dauTuan(daLoc == null ? LocalDate.now() : daLoc) };
                JLabel lblTuan = new JLabel();
                lblTuan.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lblTuan.setForeground(NAVY);
                JPanel thanhTren = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
                JButton truoc = taoNut("< Tuần trước", NAVY);
                JButton chon = taoNut("Chọn tuần", NAVY);
                JButton hienTai = taoNut("Tuần này", new Color(29, 133, 82));
                JButton sau = taoNut("Tuần sau >", NAVY);
                thanhTren.add(truoc);
                thanhTren.add(chon);
                thanhTren.add(hienTai);
                thanhTren.add(sau);
                thanhTren.add(lblTuan);
                dialog.add(thanhTren, BorderLayout.NORTH);

                DefaultTableModel modelTuan = new DefaultTableModel() {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                                return false;
                        }
                };
                JTable bangTuan = new JTable(modelTuan);
                bangTuan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                bangTuan.setRowHeight(67);
                bangTuan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                bangTuan.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
                bangTuan.getTableHeader().setBackground(NAVY);
                bangTuan.getTableHeader().setForeground(Color.WHITE);
                bangTuan.getTableHeader().setReorderingAllowed(false);
                bangTuan.setSelectionBackground(new Color(207, 222, 235));
                bangTuan.setFillsViewportHeight(true);

                bangTuan.setDefaultRenderer(Object.class, new TableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value,
                                        boolean isSelected, boolean hasFocus, int row, int column) {
                                JTextArea area = new JTextArea(value == null ? "" : value.toString());
                                area.setLineWrap(true);
                                area.setWrapStyleWord(true);
                                area.setEditable(false);
                                area.setFont(table.getFont());
                                area.setBorder(new EmptyBorder(6, 6, 4, 4));
                                if (isSelected)
                                        area.setBackground(table.getSelectionBackground());
                                else
                                        area.setBackground(column == 0 ? new Color(232, 240, 249)
                                                        : (column >= 6 ? new Color(247, 250, 253) : Color.WHITE));
                                area.setForeground(NAVY);
                                return area;
                        }
                });
                JScrollPane cuon = new JScrollPane(bangTuan);
                cuon.setBorder(new LineBorder(new Color(210, 220, 230)));
                dialog.add(cuon, BorderLayout.CENTER);

                JLabel tongKet = new JLabel();
                tongKet.setFont(fntNoiDung);
                JButton dong = taoNut("Đóng", new Color(108, 117, 125));
                dong.addActionListener(e -> dialog.dispose());
                JPanel duoi = new JPanel(new BorderLayout());
                duoi.add(tongKet, BorderLayout.WEST);
                duoi.add(dong, BorderLayout.EAST);
                dialog.add(duoi, BorderLayout.SOUTH);

                Runnable taiTuan = () -> {
                        try {
                                LocalDate thuHai = tuan[0];
                                lblTuan.setText("Tuần: " + PhanCaControl.khoangTuan(thuHai));
                                String[] cot = new String[8];
                                cot[0] = "Nhân viên";
                                String[] thu = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
                                for (int i = 0; i < 7; i++)
                                        cot[i + 1] = thu[i] + " " + thuHai.plusDays(i)
                                                        .format(DateTimeFormatter.ofPattern("dd/MM"));
                                modelTuan.setColumnIdentifiers(cot);
                                bangTuan.getColumnModel().getColumn(0).setPreferredWidth(205);
                                for (int c = 1; c < 8; c++)
                                        bangTuan.getColumnModel().getColumn(c).setPreferredWidth(163);
                                modelTuan.setRowCount(0);
                                List<LichPhanCa> phanCaTuan = control.layPhanCaTrongTuan(thuHai);

                                List<Object[]> hangTuan = control.bangLichTuan(phanCaTuan);
                                for (Object[] hang : hangTuan)
                                        modelTuan.addRow(hang);
                                tongKet.setText(phanCaTuan.isEmpty()
                                                ? "Không có nhân viên được phân ca trong tuần này."
                                                : "Tổng: " + phanCaTuan.size() + " phân ca / "
                                                                + hangTuan.size() + " nhân viên có ca");
                        } catch (Exception ex) {
                                baoLoi(ex);
                        }
                };
                truoc.addActionListener(e -> {
                        tuan[0] = tuan[0].minusWeeks(1);
                        taiTuan.run();
                });
                sau.addActionListener(e -> {
                        tuan[0] = tuan[0].plusWeeks(1);
                        taiTuan.run();
                });
                hienTai.addActionListener(e -> {
                        tuan[0] = PhanCaControl.dauTuan(LocalDate.now());
                        taiTuan.run();
                });
                chon.addActionListener(e -> {
                        LocalDate ngay = LichChonNgay.chonNgay(dialog, tuan[0], false);
                        if (ngay != null) {
                                tuan[0] = PhanCaControl.dauTuan(ngay);
                                taiTuan.run();
                        }
                });

                dialog.setSize(1350, 655);
                dialog.setMinimumSize(new Dimension(980, 450));
                dialog.setLocationRelativeTo(cha);
                taiTuan.run();
                dialog.setVisible(true);
        }

        private void hienThiSaoChepTuan() {
                Window cha = SwingUtilities.getWindowAncestor(this);
                JDialog dialog = new JDialog(cha, "Sao chép lịch phân ca", Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setLayout(new BorderLayout(10, 10));
                ((JPanel) dialog.getContentPane()).setBorder(new EmptyBorder(18, 18, 14, 18));
                LocalDate[] nguon = { PhanCaControl.dauTuan(LocalDate.now()) };
                LocalDate[] dich = { nguon[0].plusWeeks(1) };
                JTextField txtNguon = new JTextField(PhanCaControl.khoangTuan(nguon[0]), 22);
                JTextField txtDich = new JTextField(PhanCaControl.khoangTuan(dich[0]), 22);
                for (JTextField txt : new JTextField[] { txtNguon, txtDich }) {
                        txt.setEditable(false);
                        txt.setBackground(new Color(245, 247, 250));
                        txt.setFont(fntNoiDung);
                }
                JButton chonNguon = taoNut("Chọn tuần nguồn", NAVY);
                JButton chonDich = taoNut("Chọn tuần đích", NAVY);
                chonNguon.addActionListener(e -> {
                        LocalDate ngay = LichChonNgay.chonNgay(dialog, nguon[0], false);
                        if (ngay != null) {
                                nguon[0] = PhanCaControl.dauTuan(ngay);
                                txtNguon.setText(PhanCaControl.khoangTuan(nguon[0]));
                        }
                });
                chonDich.addActionListener(e -> {
                        LocalDate ngay = LichChonNgay.chonNgay(dialog, dich[0], false);
                        if (ngay != null) {
                                dich[0] = PhanCaControl.dauTuan(ngay);
                                txtDich.setText(PhanCaControl.khoangTuan(dich[0]));
                        }
                });
                JPanel noidung = new JPanel(new GridBagLayout());
                GridBagConstraints g = new GridBagConstraints();
                g.insets = new Insets(6, 4, 6, 4);
                g.fill = GridBagConstraints.HORIZONTAL;
                g.weightx = 0;
                g.gridx = 0;
                g.gridy = 0;
                noidung.add(new JLabel("Tuần nguồn:"), g);
                g.gridx = 1;
                g.weightx = 1;
                noidung.add(txtNguon, g);
                g.gridx = 2;
                g.weightx = 0;
                noidung.add(chonNguon, g);
                g.gridx = 0;
                g.gridy = 1;
                noidung.add(new JLabel("Tuần đích:"), g);
                g.gridx = 1;
                g.weightx = 1;
                noidung.add(txtDich, g);
                g.gridx = 2;
                g.weightx = 0;
                noidung.add(chonDich, g);
                dialog.add(noidung, BorderLayout.CENTER);

                JLabel huongDan = new JLabel("<html>Giữ nguyên nhân viên và ca làm; chuyển đúng thứ trong tuần.<br>"
                                + "Tự bỏ qua ca trùng giờ hoặc ngày quá khứ; không ghi đè lịch đích.</html>");
                huongDan.setForeground(NAVY);
                dialog.add(huongDan, BorderLayout.NORTH);
                JPanel nutDuoi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                JButton huy = taoNut("Hủy", new Color(108, 117, 125));
                JButton saoChep = taoNut("Sao chép lịch", GREEN);
                huy.addActionListener(e -> dialog.dispose());
                nutDuoi.add(huy);
                nutDuoi.add(saoChep);
                dialog.add(nutDuoi, BorderLayout.SOUTH);
                saoChep.addActionListener(e -> {
                        if (nguon[0].equals(dich[0])) {
                                JOptionPane.showMessageDialog(dialog, "Tuần nguồn và tuần đích phải khác nhau.",
                                                "Ngày không hợp lệ", JOptionPane.WARNING_MESSAGE);
                                return;
                        }
                        if (dich[0].plusDays(6).isBefore(LocalDate.now())) {
                                JOptionPane.showMessageDialog(dialog, "Tuần đích đã qua, vui lòng chọn tuần khác.",
                                                "Ngày không hợp lệ", JOptionPane.WARNING_MESSAGE);
                                return;
                        }
                        int xacNhan = JOptionPane.showConfirmDialog(dialog,
                                        "Sao chép từ tuần " + PhanCaControl.khoangTuan(nguon[0]) + "\n"
                                                        + "sang tuần " + PhanCaControl.khoangTuan(dich[0]) + "?\n\n"
                                                        + "Các ca đã tồn tại hoặc trùng giờ sẽ được giữ nguyên và bỏ qua.\n"
                                                        + "Không tạo phân ca cho các ngày đã qua.",
                                        "Xác nhận sao chép", JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE);
                        if (xacNhan != JOptionPane.YES_OPTION)
                                return;
                        saoChep.setEnabled(false);
                        chonNguon.setEnabled(false);
                        chonDich.setEnabled(false);
                        new SwingWorker<PhanCaControl.KetQuaSaoChep, Void>() {
                                @Override
                                protected PhanCaControl.KetQuaSaoChep doInBackground() throws Exception {
                                        return control.saoChepTuan(nguon[0], dich[0]);
                                }

                                @Override
                                protected void done() {
                                        saoChep.setEnabled(true);
                                        chonNguon.setEnabled(true);
                                        chonDich.setEnabled(true);
                                        try {
                                                PhanCaControl.KetQuaSaoChep kq = get();
                                                JOptionPane.showMessageDialog(dialog,
                                                                "Tổng lịch tuần nguồn: " + kq.getTongNguon() + "\n"
                                                                                + "Đã sao chép: " + kq.getDaThem()
                                                                                + "\n"
                                                                                + "Bỏ qua do trùng giờ: "
                                                                                + kq.getBoQuaTrungGio() + "\n"
                                                                                + "Bỏ qua do ngày đã qua: "
                                                                                + kq.getBoQuaQuaKhu(),
                                                                "Kết quả sao chép", JOptionPane.INFORMATION_MESSAGE);
                                                if (kq.getDaThem() > 0) {
                                                        taiDuLieu();
                                                        lamMoiForm();
                                                        dialog.dispose();
                                                }
                                        } catch (InterruptedException ex) {
                                                Thread.currentThread().interrupt();
                                                baoLoi(ex);
                                        } catch (ExecutionException ex) {
                                                Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                                                JOptionPane.showMessageDialog(dialog,
                                                                "Không thể sao chép lịch: " + cause.getMessage(),
                                                                "Lỗi sao chép", JOptionPane.ERROR_MESSAGE);
                                        }
                                }
                        }.execute();
                });

                dialog.setSize(690, 260);
                dialog.setMinimumSize(new Dimension(630, 260));
                dialog.setLocationRelativeTo(cha);
                dialog.setVisible(true);
        }

        private JButton taoNut(String text, Color nen) {

                JButton btnBtn = new JButton(text);
                btnBtn.setBackground(nen);
                btnBtn.setForeground(Color.WHITE);
                btnBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnBtn.setFocusPainted(false);
                btnBtn.setBorder(new EmptyBorder(8, 10, 8, 10));
                btnBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return btnBtn;
        }

        private void ganHanhDong(AbstractButton nut, Runnable hanhDong) {
                nut.addActionListener(e -> hanhDong.run());
        }

        private void thongBao(String text) {
                JOptionPane.showMessageDialog(this, text, "Pet Shop Pro", JOptionPane.INFORMATION_MESSAGE);
        }

        private void canhBao(String text) {
                JOptionPane.showMessageDialog(this, text, "Pet Shop Pro", JOptionPane.WARNING_MESSAGE);
        }

        private void baoLoi(Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Có lỗi khi xử lý phân ca:\n" + ex.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
                hienThiForm();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

}
