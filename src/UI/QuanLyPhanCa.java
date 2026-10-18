package UI;

import dao.LichPhanCaDAO;
import entity.LichPhanCa;
import util.XlsxExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/** Phân ca nhân viên: thêm, sửa, xóa, lọc, lịch chọn ngày và xuất Excel. */
public class QuanLyPhanCa extends JPanel {
        private static final Color NAVY = new Color(18, 48, 82);
        private static final Color LIGHT_BLUE = new Color(229, 241, 252);
        private static final Color BG = new Color(245, 247, 250);
        private static final Color GREEN = new Color(72, 196, 62);
        private static final Color RED = new Color(210, 70, 70);
        private static final Color BLUE = new Color(91, 180, 235);
        private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                        .withResolverStyle(ResolverStyle.STRICT);

        private final LichPhanCaDAO dao = new LichPhanCaDAO();
        private final Font fntNoiDung = new Font("Segoe UI", Font.PLAIN, 12);
        private JTextField txtMaPhanCa, txtNgayLamViec, txtHoTen, txtThongTinCa, txtLocNgay;
        private JComboBox<String> cboNhanVien, cboCa, cboLocNhanVien, cboLocCa;
        private JTable tblPhanCa;
        private DefaultTableModel mdlPhanCa;
        private JLabel lblTrangThai;
        private JButton btnSua;
        private String maDangSua;

        public QuanLyPhanCa() {
                setLayout(new BorderLayout());
                setBackground(BG);
                add(taoPnlTieuDe(), BorderLayout.NORTH);
                add(taoPnlNoiDung(), BorderLayout.CENTER);
                taiDanhMuc();
                taiDuLieu();
                lamMoiForm();
        }

        private JPanel taoPnlTieuDe() {
                JPanel pnl = new JPanel(new BorderLayout());
                pnl.setBackground(BG);
                pnl.setBorder(new EmptyBorder(14, 24, 12, 24));
                JLabel lblTitle = new JLabel("PHÂN CA NHÂN VIÊN");
                lblTitle.setForeground(NAVY);
                lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
                pnl.add(lblTitle, BorderLayout.WEST);
                return pnl;
        }

        private JPanel taoPnlNoiDung() {
                JPanel pnl = new JPanel(new BorderLayout(15, 0));
                pnl.setBackground(LIGHT_BLUE);
                pnl.setBorder(new EmptyBorder(12, 18, 15, 18));
                pnl.add(taoPnlDanhSach(), BorderLayout.CENTER);
                pnl.add(taoPnlBenPhai(), BorderLayout.EAST);
                return pnl;
        }

        private JPanel taoPnlDanhSach() {
                JPanel pnl = new JPanel(new BorderLayout(0, 9));
                pnl.setBackground(Color.WHITE);
                pnl.setBorder(new EmptyBorder(10, 10, 10, 10));
                pnl.add(taoPnlLoc(), BorderLayout.NORTH);

                mdlPhanCa = new DefaultTableModel(new String[] {
                                "Mã PC", "Ngày làm việc", "Mã NV", "Họ tên", "Mã ca", "Ca làm"
                }, 0) {
                        @Override
                        public boolean isCellEditable(int row, int col) {
                                return false;
                        }
                };
                tblPhanCa = new JTable(mdlPhanCa);
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
                DefaultTableCellRenderer center = new DefaultTableCellRenderer();
                center.setHorizontalAlignment(SwingConstants.CENTER);
                for (int i : new int[] { 0, 1, 2, 4 })
                        tblPhanCa.getColumnModel().getColumn(i).setCellRenderer(center);
                int[] doRong = { 65, 105, 75, 140, 65, 170 };
                for (int i = 0; i < doRong.length; i++)
                        tblPhanCa.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
                tblPhanCa.getSelectionModel().addListSelectionListener(e -> {
                        if (!e.getValueIsAdjusting() && tblPhanCa.getSelectedRow() >= 0)
                                hienThiForm();
                });

                JScrollPane scr = new JScrollPane(tblPhanCa);
                scr.setBorder(new LineBorder(new Color(220, 225, 230)));
                pnl.add(scr, BorderLayout.CENTER);
                pnl.add(taoPnlNutDuoi(), BorderLayout.SOUTH);
                return pnl;
        }

        private JPanel taoPnlLoc() {
                JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 4));
                pnl.setBackground(Color.WHITE);
                txtLocNgay = new JTextField(9);
                txtLocNgay.setEditable(false);
                txtLocNgay.setFont(fntNoiDung);
                txtLocNgay.setToolTipText("Có thể lọc cả các ngày trong quá khứ");
                JButton btnChonNgay = taoNut("Lịch", NAVY);
                btnChonNgay.addActionListener(e -> {
                        LocalDate banDau = docNgay(txtLocNgay.getText());
                        LocalDate chon = LichChonNgay.chonNgay(this, banDau, false);
                        if (chon != null)
                                txtLocNgay.setText(chon.format(DINH_DANG_NGAY));
                });
                JButton btnXoaLocNgay = taoNut("×", new Color(108, 117, 125));
                btnXoaLocNgay.setToolTipText("Bỏ lọc theo ngày");
                btnXoaLocNgay.addActionListener(e -> txtLocNgay.setText(""));

                cboLocNhanVien = new JComboBox<>();
                cboLocNhanVien.setPreferredSize(new Dimension(145, 30));
                cboLocCa = new JComboBox<>();
                cboLocCa.setPreferredSize(new Dimension(110, 30));
                JButton btnLoc = taoNut("Lọc", GREEN);
                btnLoc.addActionListener(e -> locDuLieu());

                pnl.add(new JLabel("Ngày làm việc"));
                pnl.add(txtLocNgay);
                pnl.add(btnChonNgay);
                pnl.add(btnXoaLocNgay);
                pnl.add(new JLabel("Nhân viên"));
                pnl.add(cboLocNhanVien);
                pnl.add(new JLabel("Ca"));
                pnl.add(cboLocCa);
                pnl.add(btnLoc);
                return pnl;
        }

        private JPanel taoPnlNutDuoi() {
                JPanel pnl = new JPanel(new BorderLayout());
                pnl.setBackground(Color.WHITE);
                JPanel pnlTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                pnlTrai.setOpaque(false);
                JButton btnXoa = taoNut("Xóa phân ca", RED);
                btnXoa.addActionListener(e -> xoa());
                JButton btnExcel = taoNut("Xuất Excel", new Color(29, 133, 82));
                btnExcel.addActionListener(e -> xuatExcel());
                pnlTrai.add(btnXoa);
                pnlTrai.add(btnExcel);
                pnl.add(pnlTrai, BorderLayout.WEST);
                JButton btnLamMoi = taoNut("↻ Làm mới", BLUE);
                btnLamMoi.addActionListener(e -> {
                        taiDanhMuc();
                        taiDuLieu();
                        lamMoiForm();
                });
                pnl.add(btnLamMoi, BorderLayout.EAST);
                return pnl;
        }

        private JPanel taoPnlBenPhai() {
                JPanel pnl = new JPanel(new BorderLayout(0, 12));
                pnl.setBackground(LIGHT_BLUE);
                pnl.setPreferredSize(new Dimension(310, 0));
                pnl.add(taoPnlForm(), BorderLayout.NORTH);
                pnl.add(taoPnlThongTinCa(), BorderLayout.CENTER);
                return pnl;
        }

        private JPanel taoPnlForm() {
                JPanel pnl = new JPanel();
                pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
                pnl.setBackground(Color.WHITE);
                pnl.setBorder(new EmptyBorder(10, 14, 12, 14));
                JLabel lblTitle = new JLabel("TẠO / SỬA PHÂN CA");
                lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblTitle.setForeground(NAVY);
                pnl.add(lblTitle);
                pnl.add(Box.createVerticalStrut(8));

                txtMaPhanCa = new JTextField();
                txtMaPhanCa.setEditable(false);
                themTruong(pnl, "Mã phân ca", txtMaPhanCa);

                txtNgayLamViec = new JTextField();
                txtNgayLamViec.setEditable(false);
                txtNgayLamViec.setBackground(Color.WHITE);
                JPanel pnlChonNgay = new JPanel(new BorderLayout(4, 0));
                pnlChonNgay.setBackground(Color.WHITE);
                pnlChonNgay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));
                JButton btnLich = taoNut("Chọn lịch", NAVY);
                btnLich.setMargin(new Insets(2, 6, 2, 6));
                btnLich.addActionListener(e -> {
                        LocalDate chon = LichChonNgay.chonNgay(this, docNgay(txtNgayLamViec.getText()), true);
                        if (chon != null)
                                txtNgayLamViec.setText(chon.format(DINH_DANG_NGAY));
                });
                pnlChonNgay.add(txtNgayLamViec, BorderLayout.CENTER);
                pnlChonNgay.add(btnLich, BorderLayout.EAST);
                themTruong(pnl, "Ngày làm việc (từ hôm nay)", pnlChonNgay);

                cboNhanVien = new JComboBox<>();
                cboNhanVien.addActionListener(e -> capNhatHoTen());
                themTruong(pnl, "Nhân viên", cboNhanVien);
                txtHoTen = new JTextField();
                txtHoTen.setEditable(false);
                themTruong(pnl, "Họ tên", txtHoTen);
                cboCa = new JComboBox<>();
                cboCa.addActionListener(e -> capNhatThongTinCa());
                themTruong(pnl, "Ca làm việc", cboCa);
                txtThongTinCa = new JTextField();
                txtThongTinCa.setEditable(false);
                themTruong(pnl, "Thông tin ca", txtThongTinCa);

                JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 0));
                pnlNut.setOpaque(false);
                JButton btnThem = taoNut("Thêm +", GREEN);
                btnThem.addActionListener(e -> them());
                btnSua = taoNut("Sửa", NAVY);
                btnSua.setEnabled(false);
                btnSua.addActionListener(e -> sua());
                JButton btnLamMoi = taoNut("Bỏ chọn", new Color(108, 117, 125));
                btnLamMoi.addActionListener(e -> lamMoiForm());
                pnlNut.add(btnThem);
                pnlNut.add(btnSua);
                pnlNut.add(btnLamMoi);
                pnlNut.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnl.add(Box.createVerticalStrut(8));
                pnl.add(pnlNut);
                pnl.add(Box.createVerticalStrut(6));
                return pnl;
        }

        private void themTruong(JPanel pnl, String nhan, JComponent control) {
                JLabel lbl = new JLabel(nhan);
                lbl.setFont(fntNoiDung);
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnl.add(lbl);
                pnl.add(Box.createVerticalStrut(3));
                control.setFont(fntNoiDung);
                control.setAlignmentX(Component.LEFT_ALIGNMENT);
                control.setMaximumSize(new Dimension(10000, 29));
                control.setPreferredSize(new Dimension(225, 29));
                pnl.add(control);
                pnl.add(Box.createVerticalStrut(6));
        }

        private JPanel taoPnlThongTinCa() {
                JPanel pnl = new JPanel(new BorderLayout(0, 8));
                pnl.setBackground(Color.WHITE);
                pnl.setBorder(new EmptyBorder(14, 15, 14, 15));
                JLabel lbl = new JLabel("Danh sách ca");
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setForeground(NAVY);
                pnl.add(lbl, BorderLayout.NORTH);
                JTextArea txtDanhSach = new JTextArea();
                txtDanhSach.setName("txtDanhSachCa");
                txtDanhSach.setEditable(false);
                txtDanhSach.setLineWrap(true);
                txtDanhSach.setWrapStyleWord(true);
                txtDanhSach.setFont(fntNoiDung);
                txtDanhSach.setBackground(Color.WHITE);
                pnl.add(new JScrollPane(txtDanhSach), BorderLayout.CENTER);
                return pnl;
        }

        private void taiDanhMuc() {
                String maNV = maDaChon(cboNhanVien);
                String maCa = maDaChon(cboCa);
                if (cboNhanVien != null) {
                        cboNhanVien.removeAllItems();
                        cboLocNhanVien.removeAllItems();
                        cboLocNhanVien.addItem("Tất cả");
                        for (String[] nv : dao.getDanhSachNhanVien()) {
                                String item = nv[0] + " - " + nv[1];
                                cboNhanVien.addItem(item);
                                cboLocNhanVien.addItem(item);
                        }
                }
                if (cboCa != null) {
                        cboCa.removeAllItems();
                        cboLocCa.removeAllItems();
                        cboLocCa.addItem("Tất cả");
                        JTextArea txtCa = timTxtDanhSachCa();
                        StringBuilder sb = new StringBuilder();
                        for (String[] ca : dao.getDanhSachCa()) {
                                cboCa.addItem(ca[0] + " - " + ca[1] + ": " + ca[2] + "–" + ca[3]);
                                cboLocCa.addItem(ca[0] + " - " + ca[1]);
                                sb.append(ca[0]).append(" - ").append(ca[1]).append("\n")
                                                .append(ca[2]).append(" – ").append(ca[3]).append("\n\n");
                        }
                        if (txtCa != null) {
                                txtCa.setText(sb.toString());
                                txtCa.setCaretPosition(0);
                        }
                }
                chonTheoMa(cboNhanVien, maNV);
                chonTheoMa(cboCa, maCa);
                capNhatHoTen();
                capNhatThongTinCa();
        }

        private JTextArea timTxtDanhSachCa() {
                return timTxt(this);
        }

        private JTextArea timTxt(Container container) {
                for (Component child : container.getComponents()) {
                        if (child instanceof JTextArea && "txtDanhSachCa".equals(child.getName()))
                                return (JTextArea) child;
                        if (child instanceof Container) {
                                JTextArea kq = timTxt((Container) child);
                                if (kq != null)
                                        return kq;
                        }
                }
                return null;
        }

        private void taiDuLieu() {
                hienThiDuLieu(dao.getAll());
        }

        private void hienThiDuLieu(List<LichPhanCa> ds) {
                mdlPhanCa.setRowCount(0);
                for (LichPhanCa pc : ds) {
                        String strNgay = pc.getNgayLamViec() == null ? "" : pc.getNgayLamViec().format(DINH_DANG_NGAY);
                        String thongTinCa = pc.getThongTinCa();
                        mdlPhanCa.addRow(new Object[] { pc.getMaPhanCa(), strNgay,
                                        pc.getMaNhanVien(), pc.getHoTen(), pc.getMaCa(), thongTinCa });
                }
                if (lblTrangThai != null)
                        lblTrangThai.setText("Tổng: " + ds.size() + " phân ca");
        }

        private void hienThiForm() {
                int row = tblPhanCa.getSelectedRow();
                if (row < 0)
                        return;
                int modelRow = tblPhanCa.convertRowIndexToModel(row);
                maDangSua = String.valueOf(mdlPhanCa.getValueAt(modelRow, 0));
                txtMaPhanCa.setText(maDangSua);
                txtNgayLamViec.setText(String.valueOf(mdlPhanCa.getValueAt(modelRow, 1)));
                chonTheoMa(cboNhanVien, String.valueOf(mdlPhanCa.getValueAt(modelRow, 2)));
                chonTheoMa(cboCa, String.valueOf(mdlPhanCa.getValueAt(modelRow, 4)));
                btnSua.setEnabled(true);
        }

        private void chonTheoMa(JComboBox<String> cbo, String ma) {
                if (cbo == null || ma == null)
                        return;
                for (int i = 0; i < cbo.getItemCount(); i++) {
                        if (cbo.getItemAt(i).startsWith(ma + " - ")) {
                                cbo.setSelectedIndex(i);
                                return;
                        }
                }
        }

        private String maDaChon(JComboBox<String> cbo) {
                if (cbo == null || cbo.getSelectedItem() == null)
                        return "";
                String selected = cbo.getSelectedItem().toString();
                int pos = selected.indexOf(" - ");
                return pos >= 0 ? selected.substring(0, pos) : selected;
        }

        private void capNhatHoTen() {
                if (txtHoTen == null)
                        return;
                String selected = cboNhanVien.getSelectedItem() == null ? "" : cboNhanVien.getSelectedItem().toString();
                int pos = selected.indexOf(" - ");
                txtHoTen.setText(pos >= 0 ? selected.substring(pos + 3) : "");
        }

        private void capNhatThongTinCa() {
                if (txtThongTinCa != null)
                        txtThongTinCa.setText(
                                        cboCa.getSelectedItem() == null ? "" : cboCa.getSelectedItem().toString());
        }

        private LichPhanCa layDuLieuForm(boolean sua) {
                if (cboNhanVien.getSelectedItem() == null || cboCa.getSelectedItem() == null) {
                        canhBao("Vui lòng chọn nhân viên và ca làm việc.");
                        return null;
                }
                String maNhanVien = maDaChon(cboNhanVien);
                String maCa = maDaChon(cboCa);
                if (!maNhanVien.matches("NV\\d{3}") || !maCa.matches("CA\\d{3}")) {
                        canhBao("Hãy chọn nhân viên và ca làm hợp lệ từ danh sách.");
                        return null;
                }
                if (sua && (maDangSua == null || maDangSua.isBlank())) {
                        canhBao("Vui lòng chọn phân ca cần sửa.");
                        return null;
                }
                LocalDate ngay = docNgay(txtNgayLamViec.getText());
                if (ngay == null) {
                        canhBao("Vui lòng chọn ngày làm việc từ lịch.");
                        return null;
                }
                if (ngay.isBefore(LocalDate.now())) {
                        canhBao("Không được thêm hoặc sửa phân ca cho ngày đã qua.");
                        return null;
                }
                return new LichPhanCa(sua ? maDangSua : dao.taoMaPhanCa(),
                                ngay, maNhanVien, maCa);
        }

        private void them() {
                LichPhanCa pc = layDuLieuForm(false);
                if (pc == null)
                        return;
                try {
                        if (dao.trungGio(pc.getMaNhanVien(), pc.getMaCa(), pc.getNgayLamViec(), null)) {
                                canhBao("Nhân viên đã có ca trùng hoặc chồng giờ trong ngày này.");
                                return;
                        }
                        if (dao.them(pc)) {
                                thongBao("Thêm phân ca thành công.");
                                taiDuLieu();
                                lamMoiForm();
                        } else
                                canhBao("Không thể thêm phân ca. Kiểm tra kết nối hoặc mã đã tồn tại.");
                } catch (Exception ex) {
                        baoLoi(ex);
                }
        }

        private void sua() {
                if (maDangSua == null) {
                        canhBao("Vui lòng chọn dòng phân ca cần sửa.");
                        return;
                }
                LichPhanCa pc = layDuLieuForm(true);
                if (pc == null)
                        return;
                try {
                        if (dao.trungGio(pc.getMaNhanVien(), pc.getMaCa(), pc.getNgayLamViec(), pc.getMaPhanCa())) {
                                canhBao("Nhân viên đã có ca trùng hoặc chồng giờ với phân ca khác.");
                                return;
                        }
                        if (dao.sua(pc)) {
                                thongBao("Cập nhật phân ca " + pc.getMaPhanCa() + " thành công.");
                                taiDuLieu();
                                lamMoiForm();
                        } else
                                canhBao("Không thể sửa. Không cho sửa phân ca của ngày đã qua hoặc mã không tồn tại.");
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
                if (dao.xoa(ma)) {
                        thongBao("Đã xóa phân ca " + ma + ".");
                        taiDuLieu();
                        lamMoiForm();
                } else
                        canhBao("Xóa phân ca thất bại.");
        }

        private void locDuLieu() {
                String maNV = maDaChon(cboLocNhanVien);
                String maCa = maDaChon(cboLocCa);
                String ngayNhap = txtLocNgay.getText().trim();
                LocalDate ngay = docNgay(ngayNhap);
                if (!ngayNhap.isEmpty() && ngay == null) {
                        canhBao("Ngày lọc không hợp lệ. Hãy chọn lại từ lịch (dd/MM/yyyy).");
                        txtLocNgay.requestFocusInWindow();
                        return;
                }
                List<LichPhanCa> ketQua = new ArrayList<>();
                for (LichPhanCa pc : dao.getAll()) {
                        if (ngay != null && !ngay.equals(pc.getNgayLamViec()))
                                continue;
                        if (!maNV.isEmpty() && !"Tất cả".equals(maNV) && !maNV.equalsIgnoreCase(pc.getMaNhanVien()))
                                continue;
                        if (!maCa.isEmpty() && !"Tất cả".equals(maCa) && !maCa.equalsIgnoreCase(pc.getMaCa()))
                                continue;
                        ketQua.add(pc);
                }
                hienThiDuLieu(ketQua);
                lamMoiFormKhongXoaBoLoc();
        }

        private LocalDate docNgay(String text) {
                if (text == null || text.trim().isEmpty())
                        return null;
                try {
                        return LocalDate.parse(text.trim(), DINH_DANG_NGAY);
                } catch (Exception ex) {
                        return null;
                }
        }

        private void lamMoiFormKhongXoaBoLoc() {
                maDangSua = null;
                txtMaPhanCa.setText(dao.taoMaPhanCa());
                txtNgayLamViec.setText(LocalDate.now().format(DINH_DANG_NGAY));
                tblPhanCa.clearSelection();
                if (btnSua != null)
                        btnSua.setEnabled(false);
        }

        private void lamMoiForm() {
                lamMoiFormKhongXoaBoLoc();
                if (cboNhanVien.getItemCount() > 0)
                        cboNhanVien.setSelectedIndex(0);
                if (cboCa.getItemCount() > 0)
                        cboCa.setSelectedIndex(0);
                if (txtLocNgay != null)
                        txtLocNgay.setText("");
                if (cboLocNhanVien.getItemCount() > 0)
                        cboLocNhanVien.setSelectedIndex(0);
                if (cboLocCa.getItemCount() > 0)
                        cboLocCa.setSelectedIndex(0);
        }

        private void xuatExcel() {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Lưu lịch phân ca thành Excel");
                chooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
                chooser.setSelectedFile(new File("LichPhanCa_" + LocalDate.now() + ".xlsx"));
                if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
                        return;
                File f = chooser.getSelectedFile();
                if (!f.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".xlsx"))
                        f = new File(f.getParentFile(), f.getName() + ".xlsx");
                if (f.exists() && JOptionPane.showConfirmDialog(this, "Tệp đã tồn tại. Ghi đè?",
                                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;
                List<String[]> rows = new ArrayList<>();
                for (int r = 0; r < tblPhanCa.getRowCount(); r++) {
                        String[] row = new String[tblPhanCa.getColumnCount()];
                        for (int c = 0; c < row.length; c++) {
                                Object value = tblPhanCa.getValueAt(r, c);
                                row[c] = value == null ? "" : value.toString();
                        }
                        rows.add(row);
                }
                String[] cols = { "Mã phân ca", "Ngày làm việc", "Mã nhân viên", "Họ tên", "Mã ca", "Ca làm" };
                try {
                        XlsxExporter.xuat(f.toPath(), cols, rows);
                        thongBao("Đã xuất " + rows.size() + " phân ca sang Excel:\n" + f.getAbsolutePath());
                } catch (IOException ex) {
                        baoLoi(ex);
                }
        }

        private JButton taoNut(String text, Color nen) {
                JButton btn = new JButton(text);
                btn.setBackground(nen);
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btn.setFocusPainted(false);
                btn.setBorder(new EmptyBorder(8, 10, 8, 10));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return btn;
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
}
