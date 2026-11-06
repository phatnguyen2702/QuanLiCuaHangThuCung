package UI;

import control.PhieuDichVuControl;
import entity.PhieuDichVu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuanLyPhieuDichVuUI extends JPanel {

    private static final Color NAVY = new Color(18, 48, 82);
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);
    private static final Color DARK_BTN = new Color(11, 43, 64);
    private static final Color GRAY_BTN = new Color(136, 142, 148);
    private static final Color STATUS_GREEN = new Color(115, 214, 137);
    private static final Color STATUS_GRAY = new Color(200, 200, 200);

    private JTextField txtTimKiem;
    private JComboBox<String> cmbNhanVien, cmbTrangThai, cmbDichVuLoc;
    private JTable tblDuLieu;
    private DefaultTableModel tableModel;

    private final PhieuDichVuControl control = new PhieuDichVuControl();
    private final ArrayList<PhieuDichVu> danhSachPhieu = new ArrayList<>();

    public QuanLyPhieuDichVuUI() {
        setLayout(new BorderLayout());
        setBackground(LIGHT_BLUE);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadComboBox();
        loadData();
    }

    private JPanel createTopPanel() {
        JPanel pnlKhung = new JPanel(new BorderLayout(0, 15));
        pnlKhung.setBackground(LIGHT_BLUE);
        pnlKhung.setBorder(new EmptyBorder(20, 25, 10, 25));

        JPanel pnlTitleRow = new JPanel(new BorderLayout());
        pnlTitleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ PHIẾU DỊCH VỤ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        pnlTitleRow.add(lblTitle, BorderLayout.WEST);

        JButton btnTaoPhieu = new JButton("+ Tạo phiếu mới");
        btnTaoPhieu.setBackground(DARK_BTN);
        btnTaoPhieu.setForeground(Color.WHITE);
        btnTaoPhieu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTaoPhieu.setFocusPainted(false);
        btnTaoPhieu.setPreferredSize(new Dimension(160, 38));
        btnTaoPhieu.addActionListener(e -> hienFormPhieuDichVu(null));
        pnlTitleRow.add(btnTaoPhieu, BorderLayout.EAST);

        pnlKhung.add(pnlTitleRow, BorderLayout.NORTH);

        JPanel pnlFilterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlFilterRow.setBackground(Color.WHITE);
        pnlFilterRow.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));

        pnlFilterRow.add(createFilterLabel("Nhân viên"));
        cmbNhanVien = new JComboBox<>();
        cmbNhanVien.setPreferredSize(new Dimension(150, 30));
        pnlFilterRow.add(cmbNhanVien);

        pnlFilterRow.add(createFilterLabel("Trạng thái"));
        cmbTrangThai = new JComboBox<>(new String[] { "Tất cả", "Chưa thanh toán", "Đã thanh toán" });
        cmbTrangThai.setPreferredSize(new Dimension(120, 30));
        pnlFilterRow.add(cmbTrangThai);

        pnlFilterRow.add(createFilterLabel("Dịch vụ"));
        cmbDichVuLoc = new JComboBox<>();
        cmbDichVuLoc.setPreferredSize(new Dimension(165, 30));
        pnlFilterRow.add(cmbDichVuLoc);

        pnlFilterRow.add(createFilterLabel("Tìm kiếm"));
        txtTimKiem = new JTextField(15);
        txtTimKiem.setToolTipText("Mã, Khách, Thú cưng, Dịch vụ...");
        pnlFilterRow.add(txtTimKiem);

        JButton btnTimKiem = new JButton("Lọc");
        btnTimKiem.setBackground(NAVY);
        btnTimKiem.setForeground(Color.WHITE);

        btnTimKiem.addActionListener(e -> locDuLieu());
        txtTimKiem.addActionListener(e -> locDuLieu());
        cmbNhanVien.addActionListener(e -> locDuLieu());
        cmbTrangThai.addActionListener(e -> locDuLieu());
        cmbDichVuLoc.addActionListener(e -> locDuLieu());

        pnlFilterRow.add(btnTimKiem);
        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setBackground(new Color(226, 233, 241));
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.addActionListener(e -> lamMoiBoLoc());
        pnlFilterRow.add(btnLamMoi);

        pnlKhung.add(pnlFilterRow, BorderLayout.CENTER);
        return pnlKhung;
    }

    private JLabel createFilterLabel(String text) {
        JLabel lblLbl = new JLabel(text);
        lblLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return lblLbl;
    }

    private JPanel createTablePanel() {
        JPanel pnlKhung = new JPanel(new BorderLayout());
        pnlKhung.setBackground(LIGHT_BLUE);
        pnlKhung.setBorder(new EmptyBorder(0, 25, 20, 25));

        String[] columns = { "Mã Phiếu", "Mã Lịch Hẹn", "Khách Hàng", "Thú Cưng",
                "Nhân Viên", "Dịch Vụ", "Thanh Toán", "Thao Tác" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        tblDuLieu = new JTable(tableModel);
        tblDuLieu.setRowHeight(54);
        tblDuLieu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblDuLieu.getTableHeader().setBackground(Color.WHITE);
        tblDuLieu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDuLieu.getTableHeader().setPreferredSize(new Dimension(100, 40));

        int[] rongCot = { 85, 100, 160, 165, 145, 355, 135, 145 };
        for (int i = 0; i < rongCot.length; i++)
            tblDuLieu.getColumnModel().getColumn(i).setPreferredWidth(rongCot[i]);

        tblDuLieu.setBorder(BorderFactory.createLineBorder(new Color(51, 153, 255), 1));

        DefaultTableCellRenderer lblCenter = new DefaultTableCellRenderer();
        lblCenter.setHorizontalAlignment(SwingConstants.CENTER);
        tblDuLieu.getColumnModel().getColumn(0).setCellRenderer(lblCenter);
        tblDuLieu.getColumnModel().getColumn(1).setCellRenderer(lblCenter);
        tblDuLieu.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        tblDuLieu.getColumnModel().getColumn(5).setCellRenderer(new DichVuNhieuDongRenderer());
        tblDuLieu.getColumnModel().getColumn(7).setCellRenderer(new ActionButtonRenderer());
        tblDuLieu.getColumnModel().getColumn(7).setCellEditor(new ActionButtonEditor());
        tblDuLieu.setToolTipText("Nhấp đúp một dòng để xem hoặc sửa phiếu chưa thanh toán.");
        tblDuLieu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tblDuLieu.getSelectedRow() >= 0
                        && tblDuLieu.columnAtPoint(e.getPoint()) != 7) {
                    String ma = String.valueOf(tblDuLieu.getValueAt(tblDuLieu.getSelectedRow(), 0));
                    PhieuDichVu phieu = control.timTheoMa(ma);
                    if (phieu != null)
                        hienFormPhieuDichVu(phieu);
                }
            }
        });

        tblDuLieu.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrCuon2 = new JScrollPane(tblDuLieu);
        scrCuon2.getViewport().setBackground(Color.WHITE);
        scrCuon2.setBorder(BorderFactory.createEmptyBorder());
        pnlKhung.add(scrCuon2, BorderLayout.CENTER);
        return pnlKhung;
    }

    private void loadComboBox() {
        cmbNhanVien.removeAllItems();
        cmbNhanVien.addItem("Tất cả");
        for (String[] nv : control.layDanhSachNhanVienSpa()) {
            cmbNhanVien.addItem(nv[1]);
        }
        cmbDichVuLoc.removeAllItems();
        cmbDichVuLoc.addItem("Tất cả");
        for (String[] dv : control.layDanhSachDichVu()) {
            cmbDichVuLoc.addItem(dv[1]);
        }
    }

    private void lamMoiBoLoc() {
        txtTimKiem.setText("");
        cmbNhanVien.setSelectedIndex(0);
        cmbTrangThai.setSelectedIndex(0);
        cmbDichVuLoc.setSelectedIndex(0);
        loadComboBox();
        locDuLieu();
    }

    public void loadData() {
        danhSachPhieu.clear();
        danhSachPhieu.addAll(control.layDanhSach());
        locDuLieu();
    }

    private void locDuLieu() {
        if (tableModel == null || cmbNhanVien.getSelectedItem() == null
                || cmbTrangThai.getSelectedItem() == null
                || cmbDichVuLoc.getSelectedItem() == null)
            return;

        tableModel.setRowCount(0);
        String nhanVien = cmbNhanVien.getSelectedItem().toString();
        String trangThai = cmbTrangThai.getSelectedItem().toString();
        String dichVu = cmbDichVuLoc.getSelectedItem().toString();
        ArrayList<PhieuDichVu> ketQua = control.locDanhSach(
                danhSachPhieu, nhanVien, trangThai, dichVu, txtTimKiem.getText());
        for (PhieuDichVu phieu : ketQua) {
            tableModel.addRow(new Object[] {
                    phieu.getMaPhieuDichVu(), phieu.getMaLichHen(), phieu.getTenKhachHang(),
                    phieu.getTenThuCung(), phieu.getTenNhanVien(),
                    phieu.getCacDichVu(), phieu.getTrangThai(), ""
            });
        }

        int rongCot = tblDuLieu.getColumnModel().getColumn(5).getPreferredWidth();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            JTextArea noiDung = new JTextArea(
                    String.valueOf(tableModel.getValueAt(row, 5)).replace(", ", "\n"));
            noiDung.setFont(tblDuLieu.getFont());
            noiDung.setLineWrap(true);
            noiDung.setWrapStyleWord(true);
            noiDung.setSize(Math.max(120, rongCot - 18), Short.MAX_VALUE);
            tblDuLieu.setRowHeight(row, Math.max(54, noiDung.getPreferredSize().height + 18));
        }
    }

    private void hienFormPhieuDichVu(PhieuDichVu pdvEdit) {
        if (pdvEdit != null && !control.coTheSua(pdvEdit)) {
            JOptionPane.showMessageDialog(this,
                    "Phiếu đã thanh toán không thể sửa. Hãy xem lại hóa đơn nếu cần đối chiếu.",
                    "Không thể sửa", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFrame winParentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        PhieuDichVuDialog winDialog = new PhieuDichVuDialog(winParentFrame, this, pdvEdit);
        winDialog.setVisible(true);
    }

    private class DichVuNhieuDongRenderer extends JTextArea implements TableCellRenderer {
        DichVuNhieuDongRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setEditable(false);
            setBorder(new EmptyBorder(5, 8, 5, 6));
        }

        @Override
        public Component getTableCellRendererComponent(JTable tblTbl, Object value,
                boolean selected, boolean focused, int row, int column) {
            String noiDung = value == null ? "" : value.toString();
            setText(noiDung.replace(", ", "\n"));
            setFont(tblTbl.getFont());
            setBackground(selected ? tblTbl.getSelectionBackground() : Color.WHITE);
            setForeground(selected ? tblTbl.getSelectionForeground() : tblTbl.getForeground());
            setToolTipText("<html>" + noiDung.replace("&", "&amp;")
                    .replace("<", "&lt;").replace(">", "&gt;")
                    .replace(", ", "<br>") + "</html>");
            return this;
        }
    }

    private class StatusCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable tblDuLieu, Object value, boolean isSel, boolean hasFocus,
                int r, int c) {
            Component comp = super.getTableCellRendererComponent(tblDuLieu, value, isSel, hasFocus, r, c);
            setHorizontalAlignment(SwingConstants.CENTER);
            String st = value != null ? value.toString() : "";
            if (st.equals("Đã thanh toán"))
                comp.setBackground(STATUS_GREEN);
            else if (st.equals("Chưa thanh toán"))
                comp.setBackground(STATUS_GRAY);
            else
                comp.setBackground(Color.WHITE);
            return comp;
        }
    }

    private class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setOpaque(true);
            JButton btnEdit = new JButton("Sửa");
            btnEdit.setBackground(NAVY);
            btnEdit.setForeground(Color.WHITE);
            JButton btnDelete = new JButton("Xóa");
            btnDelete.setBackground(new Color(220, 53, 69));
            btnDelete.setForeground(Color.WHITE);
            add(btnEdit);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tblDuLieu, Object value, boolean isSelected,
                boolean hasFocus,
                int row, int column) {
            setBackground(isSelected ? tblDuLieu.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel pnlKhung;
        private String currentMaPhieu;

        public ActionButtonEditor() {
            pnlKhung = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            pnlKhung.setBackground(Color.WHITE);
            JButton btnEdit = new JButton("Sửa");
            btnEdit.setBackground(NAVY);
            btnEdit.setForeground(Color.WHITE);
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                PhieuDichVu phieu = control.timTheoMa(currentMaPhieu);
                if (phieu != null)
                    hienFormPhieuDichVu(phieu);
            });

            JButton btnDelete = new JButton("Xóa");
            btnDelete.setBackground(new Color(220, 53, 69));
            btnDelete.setForeground(Color.WHITE);
            btnDelete.addActionListener(e -> {
                fireEditingStopped();
                if (JOptionPane.showConfirmDialog(pnlKhung, "Xóa phiếu " + currentMaPhieu + "?", "Xóa",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (control.xoa(currentMaPhieu)) {
                        JOptionPane.showMessageDialog(pnlKhung, "Đã xóa!");
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(pnlKhung, "Không thể xóa phiếu dịch vụ.",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            pnlKhung.add(btnEdit);
            pnlKhung.add(btnDelete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable tblDuLieu, Object value, boolean isSelected, int row,
                int column) {
            currentMaPhieu = tblDuLieu.getValueAt(row, 0).toString();
            pnlKhung.setBackground(tblDuLieu.getSelectionBackground());
            return pnlKhung;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    class PhieuDichVuDialog extends JDialog {
        private JTextField txtMaPhieu;
        private JComboBox<String> cmbLichHen, cmbKhachHang, cmbThuCung, cmbNhanVien;
        private JTextField txtFormTrangThai;

        private final ArrayList<JCheckBox> chkDichVuList = new ArrayList<>();

        private final QuanLyPhieuDichVuUI pnlParentPanel;
        private final PhieuDichVu pEdit;

        public PhieuDichVuDialog(JFrame winParent, QuanLyPhieuDichVuUI pnlKhung, PhieuDichVu pEdit) {
            super(winParent, pEdit == null
                    ? "Tạo Phiếu Dịch Vụ Mới"
                    : "Sửa Phiếu " + pEdit.getMaPhieuDichVu(), true);
            this.pnlParentPanel = pnlKhung;
            this.pEdit = pEdit;

            setSize(760, 640);
            setMinimumSize(new Dimension(650, 560));
            setLocationRelativeTo(winParent);
            setLayout(new BorderLayout());

            JPanel pnlTop = new JPanel(new GridLayout(6, 2, 10, 10));
            pnlTop.setBorder(new EmptyBorder(15, 30, 10, 30));
            pnlTop.setBackground(Color.WHITE);

            pnlTop.add(new JLabel("Mã Phiếu:"));
            txtMaPhieu = new JTextField(pEdit == null ? control.taoMaTuDong() : pEdit.getMaPhieuDichVu());
            txtMaPhieu.setEditable(false);
            pnlTop.add(txtMaPhieu);

            pnlTop.add(new JLabel("Liên kết Lịch Hẹn:"));
            cmbLichHen = new JComboBox<>();
            cmbLichHen.addItem("Không có");
            for (String lh : control.layDanhSachLichHen(pEdit == null ? null : pEdit.getMaPhieuDichVu()))
                cmbLichHen.addItem(lh);
            pnlTop.add(cmbLichHen);

            pnlTop.add(new JLabel("Khách Hàng:"));
            cmbKhachHang = new JComboBox<>();
            for (String[] kh : control.layDanhSachKhachHang())
                cmbKhachHang.addItem(kh[0] + " - " + kh[1]);
            pnlTop.add(cmbKhachHang);

            pnlTop.add(new JLabel("Thú Cưng:"));
            cmbThuCung = new JComboBox<>();
            pnlTop.add(cmbThuCung);

            pnlTop.add(new JLabel("Thợ Spa:"));
            cmbNhanVien = new JComboBox<>();
            for (String[] nv : control.layDanhSachNhanVienSpa())
                cmbNhanVien.addItem(nv[0] + " - " + nv[1]);
            pnlTop.add(cmbNhanVien);

            pnlTop.add(new JLabel("Trạng Thái:"));
            String trangThaiHienTai = (pEdit == null) ? "Chưa thanh toán" : pEdit.getTrangThai();
            txtFormTrangThai = new JTextField(trangThaiHienTai);
            txtFormTrangThai.setEditable(false);
            txtFormTrangThai.setBackground(new Color(240, 240, 240));
            txtFormTrangThai
                    .setToolTipText("Tự chuyển sang 'Đã thanh toán' khi thanh toán hóa đơn thành công.");
            pnlTop.add(txtFormTrangThai);

            JPanel pnlCenter = new JPanel(new BorderLayout(0, 5));
            pnlCenter.setBackground(Color.WHITE);
            pnlCenter.setBorder(new EmptyBorder(0, 30, 10, 30));

            JLabel lblDichVuTitle = new JLabel("Chọn Dịch Vụ:");
            lblDichVuTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            pnlCenter.add(lblDichVuTitle, BorderLayout.NORTH);

            JPanel pnlDichVu = new JPanel(new GridLayout(0, 2, 5, 5));
            pnlDichVu.setBackground(Color.WHITE);

            ArrayList<String[]> danhSachDichVu = control.layDanhSachDichVu();
            ArrayList<String> selectedMaDichVu = (pEdit == null) ? new ArrayList<>()
                    : control.layMaDichVuCuaPhieu(pEdit.getMaPhieuDichVu());

            for (String[] dv : danhSachDichVu) {
                JCheckBox checkbox = new JCheckBox(dv[1]);
                checkbox.setBackground(Color.WHITE);
                checkbox.setActionCommand(dv[0]);
                checkbox.setSelected(selectedMaDichVu.contains(dv[0]));
                chkDichVuList.add(checkbox);
                pnlDichVu.add(checkbox);
            }

            JScrollPane scrScrollDichVu = new JScrollPane(pnlDichVu);
            scrScrollDichVu.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            pnlCenter.add(scrScrollDichVu, BorderLayout.CENTER);

            JPanel pnlMain = new JPanel(new BorderLayout());
            pnlMain.setBackground(Color.WHITE);
            pnlMain.add(pnlTop, BorderLayout.NORTH);
            pnlMain.add(pnlCenter, BorderLayout.CENTER);

            add(pnlMain, BorderLayout.CENTER);

            JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            pnlBtn.setBackground(Color.WHITE);
            pnlBtn.setBorder(new EmptyBorder(0, 0, 10, 15));

            JButton btnHuy = new JButton("Hủy");
            btnHuy.setBackground(GRAY_BTN);
            btnHuy.setForeground(Color.WHITE);
            btnHuy.addActionListener(e -> dispose());

            JButton btnLuu = new JButton("Lưu Phiếu");
            btnLuu.setBackground(DARK_BTN);
            btnLuu.setForeground(Color.WHITE);
            btnLuu.addActionListener(e -> xuLyLuu());

            pnlBtn.add(btnHuy);
            pnlBtn.add(btnLuu);
            add(pnlBtn, BorderLayout.SOUTH);

            cmbKhachHang.addActionListener(e -> {
                if (cmbKhachHang.isEnabled())
                    napThuCungTheoKhachHang();
            });
            cmbLichHen.addActionListener(e -> capNhatTheoLichHen());

            if (pEdit != null && pEdit.getMaLichHen() != null
                    && !"Không có".equals(pEdit.getMaLichHen())) {
                cmbLichHen.setSelectedItem(pEdit.getMaLichHen());
            } else {
                cmbLichHen.setSelectedItem("Không có");
                if (pEdit != null) {
                    setCombo(cmbKhachHang, pEdit.getMaKhachHang());
                    napThuCungTheoKhachHang();
                    setCombo(cmbThuCung, pEdit.getMaThuCung());
                    setCombo(cmbNhanVien, pEdit.getMaNhanVien());
                }
            }
            capNhatTheoLichHen();
        }

        private void napThuCungTheoKhachHang() {
            String maKH = layMa(cmbKhachHang);
            cmbThuCung.removeAllItems();
            for (String[] tc : control.layDanhSachThuCungTheoKhachHang(maKH)) {
                cmbThuCung.addItem(tc[0] + " - " + tc[1]);
            }
        }

        private void capNhatTheoLichHen() {
            String maLich = (String) cmbLichHen.getSelectedItem();
            boolean coLich = maLich != null && !"Không có".equals(maLich);
            if (coLich) {
                try {
                    entity.LichHen lh = control.kiemTraLichHen(maLich,
                            pEdit == null ? null : pEdit.getMaLichHen());
                    setCombo(cmbKhachHang, lh.getMaKhachHang());
                    napThuCungTheoKhachHang();
                    setCombo(cmbThuCung, lh.getMaThuCung());
                    setCombo(cmbNhanVien, lh.getMaNhanVien());
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this,
                            ex.getMessage(),
                            "Không thể chọn lịch hẹn", JOptionPane.WARNING_MESSAGE);
                    cmbLichHen.setSelectedItem("Không có");
                    return;
                }
            } else {
                napThuCungTheoKhachHang();
            }
            cmbKhachHang.setEnabled(!coLich);
            cmbThuCung.setEnabled(!coLich);
            cmbNhanVien.setEnabled(!coLich);
            String goiY = coLich
                    ? "Thông tin tự động lấy theo lịch hẹn, không thể chỉnh sửa ở phiếu."
                    : "Phiếu khách đến trực tiếp: chọn khách hàng, thú cưng và thợ Spa.";
            cmbKhachHang.setToolTipText(goiY);
            cmbThuCung.setToolTipText(goiY);
            cmbNhanVien.setToolTipText(goiY);
        }

        private String layMa(JComboBox<String> cmbCbo) {
            Object item = cmbCbo.getSelectedItem();
            if (item == null)
                return null;
            String text = item.toString();
            int pos = text.indexOf(" - ");
            return pos >= 0 ? text.substring(0, pos) : text;
        }

        private void setCombo(JComboBox<String> cmbCbo, String id) {
            for (int i = 0; i < cmbCbo.getItemCount(); i++) {
                if (cmbCbo.getItemAt(i).startsWith(id + " - ")) {
                    cmbCbo.setSelectedIndex(i);
                    break;
                }
            }
        }

        private void xuLyLuu() {
            List<String> maDichVuDuocChon = new ArrayList<>();
            for (JCheckBox checkbox : chkDichVuList) {
                if (checkbox.isSelected())
                    maDichVuDuocChon.add(checkbox.getActionCommand());
            }

            String maLich = (String) cmbLichHen.getSelectedItem();
            if (maLich == null)
                maLich = "Không có";

            PhieuDichVu phieu = new PhieuDichVu(
                    txtMaPhieu.getText(),
                    pEdit == null ? "Chưa thanh toán" : pEdit.getTrangThai(),
                    layMa(cmbKhachHang),
                    layMa(cmbThuCung),
                    layMa(cmbNhanVien),
                    maLich);
            try {
                if (control.luu(phieu, maDichVuDuocChon,
                        pEdit == null ? null : pEdit.getMaLichHen(), pEdit != null)) {
                    JOptionPane.showMessageDialog(this, "Thành công!");
                    pnlParentPanel.loadData();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu chưa hợp lệ",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
