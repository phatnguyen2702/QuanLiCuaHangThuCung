package UI;

import dao.LichHenDAO;
import dao.PhieuDichVuDAO;
import entity.PhieuDichVu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class QuanLyPhieuDichVu extends JPanel {

    private final Color NAVY = new Color(18, 48, 82);
    private final Color LIGHT_BLUE = new Color(229, 241, 252);
    private final Color DARK_BTN = new Color(11, 43, 64); // Nút + Tạo phiếu mới
    private final Color GRAY_BTN = new Color(136, 142, 148);
    private final Color STATUS_GREEN = new Color(115, 214, 137);
    private final Color STATUS_GRAY = new Color(200, 200, 200);

    private JTextField txtTuNgay, txtDenNgay, txtTimKiem;
    private JComboBox<String> cboNhanVien, cboTrangThai;
    private JTable table;
    private DefaultTableModel tableModel;

    private PhieuDichVuDAO dao;
    private LichHenDAO lichHenDAO; // Dùng để load cb Khách hàng, Thú cưng

    public QuanLyPhieuDichVu() {
        dao = new PhieuDichVuDAO();
        lichHenDAO = new LichHenDAO();
        setLayout(new BorderLayout());
        setBackground(LIGHT_BLUE);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadComboBox();
        loadData();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(LIGHT_BLUE);
        panel.setBorder(new EmptyBorder(20, 25, 10, 25));

        // 1. Tiêu đề & Nút Tạo phiếu
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ PHIẾU DỊCH VỤ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleRow.add(lblTitle, BorderLayout.WEST);

        JButton btnTaoPhieu = new JButton("+ Tạo phiếu mới");
        btnTaoPhieu.setBackground(DARK_BTN);
        btnTaoPhieu.setForeground(Color.WHITE);
        btnTaoPhieu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTaoPhieu.setFocusPainted(false);
        btnTaoPhieu.setPreferredSize(new Dimension(160, 38));
        btnTaoPhieu.addActionListener(e -> hienFormPhieuDichVu(null));
        titleRow.add(btnTaoPhieu, BorderLayout.EAST);

        panel.add(titleRow, BorderLayout.NORTH);

        // 2. Bộ lọc
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterRow.setBackground(Color.WHITE);
        filterRow.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));

        filterRow.add(createFilterLabel("Từ"));
        txtTuNgay = new JTextField(8);
        filterRow.add(txtTuNgay);

        filterRow.add(createFilterLabel("Đến"));
        txtDenNgay = new JTextField(8);
        filterRow.add(txtDenNgay);

        filterRow.add(createFilterLabel("Nhân viên"));
        cboNhanVien = new JComboBox<>();
        cboNhanVien.setPreferredSize(new Dimension(150, 30));
        filterRow.add(cboNhanVien);

        filterRow.add(createFilterLabel("Trạng thái"));
        cboTrangThai = new JComboBox<>(new String[] { "Tất cả", "Hoàn thành", "Đang xử lý" });
        cboTrangThai.setPreferredSize(new Dimension(120, 30));
        filterRow.add(cboTrangThai);

        filterRow.add(createFilterLabel("Tìm kiếm"));
        txtTimKiem = new JTextField(15);
        filterRow.add(txtTimKiem);

        JButton btnTimKiem = new JButton("Lọc");
        btnTimKiem.setBackground(NAVY);
        btnTimKiem.setForeground(Color.WHITE);
        filterRow.add(btnTimKiem);

        panel.add(filterRow, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return lbl;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_BLUE);
        panel.setBorder(new EmptyBorder(0, 25, 20, 25));

        String[] columns = { "Mã Phiếu", "Mã Lịch Hẹn", "Thú Cưng", "Nhân Viên", "Trạng Thái", "Thao Tác" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            } // Chỉ cột Thao tác
        };

        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(100, 40));

        // Custom viền xanh theo thiết kế
        table.setBorder(BorderFactory.createLineBorder(new Color(51, 153, 255), 1));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());

        table.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionButtonEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void loadComboBox() {
        cboNhanVien.removeAllItems();
        cboNhanVien.addItem("Tất cả");
        for (String[] nv : lichHenDAO.getDanhSachNhanVienSpa()) {
            cboNhanVien.addItem(nv[1]);
        }
    }

    public void loadData() {
        tableModel.setRowCount(0);
        for (PhieuDichVu p : dao.getAll()) {
            tableModel.addRow(new Object[] {
                    p.getMaPhieuDichVu(), p.getMaLichHen(), p.getTenThuCung(), p.getTenNhanVien(), p.getTrangThai(), ""
            });
        }
    }

    private void hienFormPhieuDichVu(PhieuDichVu pdvEdit) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        PhieuDichVuDialog dialog = new PhieuDichVuDialog(parentFrame, this, dao, lichHenDAO, pdvEdit);
        dialog.setVisible(true);
    }

    // ================== RENDERER MÀU TRẠNG THÁI ==================
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus,
                int r, int c) {
            Component comp = super.getTableCellRendererComponent(table, value, isSel, hasFocus, r, c);
            setHorizontalAlignment(SwingConstants.CENTER);
            String st = value != null ? value.toString() : "";
            if (st.equals("Hoàn thành"))
                comp.setBackground(STATUS_GREEN);
            else if (st.equals("Đang xử lý"))
                comp.setBackground(STATUS_GRAY);
            else
                comp.setBackground(Color.WHITE);
            return comp;
        }
    }

    // ================== RENDERER & EDITOR NÚT BẤM ==================
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private String currentMaPhieu;

        public ActionButtonEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(Color.WHITE);
            JButton btnEdit = new JButton("Sửa");
            btnEdit.setBackground(NAVY);
            btnEdit.setForeground(Color.WHITE);
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                for (PhieuDichVu p : dao.getAll()) {
                    if (p.getMaPhieuDichVu().equals(currentMaPhieu)) {
                        hienFormPhieuDichVu(p);
                        break;
                    }
                }
            });

            JButton btnDelete = new JButton("Xóa");
            btnDelete.setBackground(new Color(220, 53, 69));
            btnDelete.setForeground(Color.WHITE);
            btnDelete.addActionListener(e -> {
                fireEditingStopped();
                if (JOptionPane.showConfirmDialog(panel, "Xóa phiếu " + currentMaPhieu + "?", "Xóa",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (dao.xoa(currentMaPhieu)) {
                        JOptionPane.showMessageDialog(panel, "Đã xóa!");
                        loadData();
                    }
                }
            });
            panel.add(btnEdit);
            panel.add(btnDelete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            currentMaPhieu = table.getValueAt(row, 0).toString();
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    // ================== DIALOG THÊM / SỬA (INNER CLASS) ==================
    class PhieuDichVuDialog extends JDialog {
        private JTextField txtMaPhieu;
        private JComboBox<String> cboLichHen, cboKhachHang, cboThuCung, cboNhanVien, cboTrangThai;
        private PhieuDichVuDAO pDao;
        private LichHenDAO lhDao;
        private QuanLyPhieuDichVu parentPanel;
        private PhieuDichVu pEdit;

        public PhieuDichVuDialog(JFrame parent, QuanLyPhieuDichVu panel, PhieuDichVuDAO pDao, LichHenDAO lhDao,
                PhieuDichVu pEdit) {
            super(parent, pEdit == null ? "Tạo Phiếu Dịch Vụ Mới" : "Sửa Phiếu " + pEdit.getMaPhieuDichVu(), true);
            this.parentPanel = panel;
            this.pDao = pDao;
            this.lhDao = lhDao;
            this.pEdit = pEdit;
            setSize(500, 450);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());

            JPanel pnlForm = new JPanel(new GridLayout(6, 2, 10, 15));
            pnlForm.setBorder(new EmptyBorder(20, 30, 20, 30));
            pnlForm.setBackground(Color.WHITE);

            pnlForm.add(new JLabel("Mã Phiếu:"));
            txtMaPhieu = new JTextField(pEdit == null ? pDao.taoMaTuDong() : pEdit.getMaPhieuDichVu());
            txtMaPhieu.setEditable(false);
            pnlForm.add(txtMaPhieu);

            pnlForm.add(new JLabel("Liên kết Lịch Hẹn:"));
            cboLichHen = new JComboBox<>();
            cboLichHen.addItem("Không có");
            for (String lh : pDao.getDanhSachLichHen())
                cboLichHen.addItem(lh);
            pnlForm.add(cboLichHen);

            pnlForm.add(new JLabel("Khách Hàng:"));
            cboKhachHang = new JComboBox<>();
            for (String[] kh : lhDao.getDanhSachKhachHang())
                cboKhachHang.addItem(kh[0] + " - " + kh[1]);
            pnlForm.add(cboKhachHang);

            pnlForm.add(new JLabel("Thú Cưng:"));
            cboThuCung = new JComboBox<>();
            for (String[] tc : lhDao.getDanhSachThuCung())
                cboThuCung.addItem(tc[0] + " - " + tc[1]);
            pnlForm.add(cboThuCung);

            pnlForm.add(new JLabel("Thợ Spa:"));
            cboNhanVien = new JComboBox<>();
            for (String[] nv : lhDao.getDanhSachNhanVienSpa())
                cboNhanVien.addItem(nv[0] + " - " + nv[1]);
            pnlForm.add(cboNhanVien);

            pnlForm.add(new JLabel("Trạng Thái:"));
            cboTrangThai = new JComboBox<>(new String[] { "Đang xử lý", "Hoàn thành" });
            pnlForm.add(cboTrangThai);

            add(pnlForm, BorderLayout.CENTER);

            JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            pnlBtn.setBackground(Color.WHITE);
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

            if (pEdit != null) {
                cboLichHen.setSelectedItem(pEdit.getMaLichHen());
                cboTrangThai.setSelectedItem(pEdit.getTrangThai());
                setCombo(cboKhachHang, pEdit.getMaKhachHang());
                setCombo(cboThuCung, pEdit.getMaThuCung());
                setCombo(cboNhanVien, pEdit.getMaNhanVien());
            }
        }

        private void setCombo(JComboBox<String> cbo, String id) {
            for (int i = 0; i < cbo.getItemCount(); i++) {
                if (cbo.getItemAt(i).startsWith(id + " - ")) {
                    cbo.setSelectedIndex(i);
                    break;
                }
            }
        }

        private void xuLyLuu() {
            String kh = cboKhachHang.getSelectedItem().toString().split(" - ")[0];
            String tc = cboThuCung.getSelectedItem().toString().split(" - ")[0];
            String nv = cboNhanVien.getSelectedItem().toString().split(" - ")[0];

            PhieuDichVu p = new PhieuDichVu(txtMaPhieu.getText(), cboTrangThai.getSelectedItem().toString(), kh, tc, nv,
                    cboLichHen.getSelectedItem().toString());
            boolean success = (pEdit == null) ? pDao.them(p) : pDao.sua(p);

            if (success) {
                JOptionPane.showMessageDialog(this, "Thành công!");
                parentPanel.loadData();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}