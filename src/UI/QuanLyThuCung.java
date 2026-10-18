package UI;

import dao.ThuCungDAO;
import entity.ThuCung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class QuanLyThuCung extends JPanel {

    private final Color NAVY = new Color(18, 48, 82);
    private final Color GREEN_BTN = new Color(72, 196, 62);
    private final Color BLUE_BTN = new Color(91, 180, 235);
    private final Color LIGHT_BLUE = new Color(229, 241, 252);

    private JTextField txtTimKiem, txtMaTC, txtHoTen, txtCanNang, txtMaKH, txtGiong;
    private JComboBox<String> cboLocGioiTinh, cboLocLoai;
    private JComboBox<String> cboLoai, cboGioiTinh, cboTrangThai;

    // Đưa Tiêu đề và Nút Lưu lên làm biến toàn cục để dễ cập nhật Text
    private JLabel lblFormTitle;
    private JButton btnLuu;

    private JTable table;
    private DefaultTableModel tableModel;
    private ThuCungDAO dao;

    private boolean isEditMode = false;

    public QuanLyThuCung() {
        dao = new ThuCungDAO();
        setLayout(new BorderLayout(20, 20));
        setBackground(LIGHT_BLUE);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // Tiêu đề chính
        JLabel lblTitle = new JLabel("Quản lý hồ sơ thú cưng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // Chia màn hình làm 2 phần: Trái (Bảng) - Phải (Form)
        JPanel centerPanel = new JPanel(new BorderLayout(20, 0));
        centerPanel.setOpaque(false);

        centerPanel.add(createLeftPanel(), BorderLayout.CENTER);
        centerPanel.add(createRightPanel(), BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);
        loadData();
    }

    // ================== PANEL TRÁI (BẢNG & LỌC) ==================
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. Thanh tìm kiếm & lọc
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(Color.WHITE);

        txtTimKiem = new JTextField(15);
        txtTimKiem.setPreferredSize(new Dimension(200, 35));
        txtTimKiem.setToolTipText("Mã Thú cưng...");

        cboLocGioiTinh = new JComboBox<>(new String[] { "Giới tính", "Đực", "Cái" });
        cboLocGioiTinh.setPreferredSize(new Dimension(100, 35));

        cboLocLoai = new JComboBox<>(new String[] { "Loại", "Chó", "Mèo", "Chim", "Khác" });
        cboLocLoai.setPreferredSize(new Dimension(100, 35));

        JButton btnTimKiem = new JButton("Tìm kiếm");
        btnTimKiem.setBackground(GREEN_BTN);
        btnTimKiem.setForeground(Color.WHITE);
        btnTimKiem.setPreferredSize(new Dimension(100, 35));

        // --- GÁN SỰ KIỆN TÌM KIẾM ---
        btnTimKiem.addActionListener(e -> timKiem());
        txtTimKiem.addActionListener(e -> timKiem()); // Cho phép ấn Enter ở ô Text
        cboLocGioiTinh.addActionListener(e -> timKiem()); // Lọc ngay khi đổi tùy chọn
        cboLocLoai.addActionListener(e -> timKiem());

        filterPanel.add(txtTimKiem);
        filterPanel.add(cboLocGioiTinh);
        filterPanel.add(cboLocLoai);
        filterPanel.add(btnTimKiem);

        panel.add(filterPanel, BorderLayout.NORTH);

        // 2. Bảng dữ liệu
        String[] columns = { "Mã TC", "Họ tên TC", "Loại TC", "Trạng thái", "Thao Tác" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(100, 40));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);

        table.getColumnModel().getColumn(4).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ActionButtonEditor());

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // 3. Nút làm mới dưới cùng
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        JButton btnLamMoi = new JButton("↻ Làm mới");
        btnLamMoi.setBackground(BLUE_BTN);
        btnLamMoi.setForeground(Color.WHITE);
        btnLamMoi.addActionListener(e -> {
            // Khi bấm Làm Mới, xóa cả bộ lọc và tải lại danh sách gốc
            txtTimKiem.setText("");
            cboLocGioiTinh.setSelectedIndex(0);
            cboLocLoai.setSelectedIndex(0);
            loadData();
            resetForm();
        });
        bottomPanel.add(btnLamMoi);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ================== PANEL PHẢI (FORM THÊM/SỬA) ==================
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(400, 0));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Khởi tạo Tiêu đề
        lblFormTitle = new JLabel("Thêm hồ sơ thú cưng", SwingConstants.CENTER);
        lblFormTitle.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        panel.add(lblFormTitle, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.weightx = 1.0;

        int row = 0;
        txtMaTC = createTextField();
        txtMaTC.setEditable(false);
        addFieldRow(pnlForm, gbc, "Mã thú cưng", txtMaTC, row++);

        txtHoTen = createTextField();
        addFieldRow(pnlForm, gbc, "Họ tên", txtHoTen, row++);

        cboLoai = new JComboBox<>(new String[] { "Chó", "Mèo", "Chim", "Khác" });
        cboLoai.setPreferredSize(new Dimension(200, 35));
        cboLoai.setBackground(Color.WHITE);
        addFieldRow(pnlForm, gbc, "Loại", cboLoai, row++);

        txtGiong = createTextField();
        addFieldRow(pnlForm, gbc, "Giống", txtGiong, row++);

        cboGioiTinh = new JComboBox<>(new String[] { "Đực", "Cái" });
        cboGioiTinh.setPreferredSize(new Dimension(200, 35));
        cboGioiTinh.setBackground(Color.WHITE);
        addFieldRow(pnlForm, gbc, "Giới tính", cboGioiTinh, row++);

        txtCanNang = createTextField();
        addFieldRow(pnlForm, gbc, "Cân nặng (kg)", txtCanNang, row++);

        txtMaKH = createTextField();
        addFieldRow(pnlForm, gbc, "Mã khách hàng", txtMaKH, row++);

        cboTrangThai = new JComboBox<>(new String[] { "Đang hoạt động", "Không hoạt động" });
        cboTrangThai.setPreferredSize(new Dimension(200, 35));
        cboTrangThai.setBackground(Color.WHITE);
        addFieldRow(pnlForm, gbc, "Trạng thái", cboTrangThai, row++);

        panel.add(pnlForm, BorderLayout.CENTER);

        // Khởi tạo Nút Lưu
        btnLuu = new JButton("Thêm Hồ Sơ");
        btnLuu.setBackground(GREEN_BTN);
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLuu.setPreferredSize(new Dimension(200, 40));
        btnLuu.addActionListener(e -> xuLyLuuDuLieu());

        JPanel pnlBtn = new JPanel();
        pnlBtn.setBackground(Color.WHITE);
        pnlBtn.add(btnLuu);
        panel.add(pnlBtn, BorderLayout.SOUTH);

        resetForm(); // Gọi để sinh mã tự động ban đầu
        return panel;
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(200, 35));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK), new EmptyBorder(0, 10, 0, 10)));
        return txt;
    }

    private void addFieldRow(JPanel panel, GridBagConstraints gbc, String label, JComponent comp, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    // =========================================================
    // XỬ LÝ DỮ LIỆU
    // =========================================================

    // Hàm thực hiện việc tìm kiếm và lọc
    private void timKiem() {
        String tuKhoa = txtTimKiem.getText().trim().toLowerCase();
        String gioiTinhLoc = cboLocGioiTinh.getSelectedItem().toString();
        String loaiLoc = cboLocLoai.getSelectedItem().toString();

        tableModel.setRowCount(0); // Làm sạch bảng

        for (ThuCung tc : dao.getAll()) {
            // Lọc mã Thú Cưng (chứa từ khóa tìm kiếm)
            boolean khopMa = tuKhoa.isEmpty() || tc.getMaThuCung().toLowerCase().contains(tuKhoa);

            // Lọc Giới tính
            boolean khopGioiTinh = gioiTinhLoc.equals("Giới tính") ||
                    (tc.getGioiTinh() != null && tc.getGioiTinh().equals(gioiTinhLoc));

            // Lọc Loại thú cưng
            boolean khopLoai = loaiLoc.equals("Loại") ||
                    (tc.getLoai() != null && tc.getLoai().equals(loaiLoc));

            // Thỏa mãn toàn bộ điều kiện thì đưa vào bảng
            if (khopMa && khopGioiTinh && khopLoai) {
                tableModel.addRow(new Object[] {
                        tc.getMaThuCung(), tc.getTenThuCung(), tc.getLoai(), tc.getTrangThai(), ""
                });
            }
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        for (ThuCung tc : dao.getAll()) {
            tableModel.addRow(new Object[] {
                    tc.getMaThuCung(), tc.getTenThuCung(), tc.getLoai(), tc.getTrangThai(), ""
            });
        }
    }

    private void resetForm() {
        isEditMode = false;
        txtMaTC.setText(dao.taoMaThuCungTuDong());
        txtHoTen.setText("");
        txtGiong.setText("");
        txtCanNang.setText("");
        txtMaKH.setText("");
        cboLoai.setSelectedIndex(0);
        cboGioiTinh.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);

        // Đổi lại Text tiêu đề và Nút bấm trực tiếp qua biến toàn cục
        if (lblFormTitle != null)
            lblFormTitle.setText("Thêm hồ sơ thú cưng");
        if (btnLuu != null)
            btnLuu.setText("Thêm Hồ Sơ");
    }

    private void hienThiThongTinSua(String maTC) {
        for (ThuCung tc : dao.getAll()) {
            if (tc.getMaThuCung().equals(maTC)) {
                isEditMode = true;
                txtMaTC.setText(tc.getMaThuCung());
                txtHoTen.setText(tc.getTenThuCung());
                txtGiong.setText(tc.getGiong() != null ? tc.getGiong() : "");
                cboLoai.setSelectedItem(tc.getLoai());
                cboGioiTinh.setSelectedItem(tc.getGioiTinh());
                txtCanNang.setText(String.valueOf(tc.getCanNang()));
                txtMaKH.setText(tc.getMaKhachHang() != null ? tc.getMaKhachHang() : "");
                cboTrangThai.setSelectedItem(tc.getTrangThai());

                // Đổi Text tiêu đề và Nút bấm sang chế độ Cập nhật
                if (lblFormTitle != null)
                    lblFormTitle.setText("Cập nhật hồ sơ");
                if (btnLuu != null)
                    btnLuu.setText("Cập nhật");
                break;
            }
        }
    }

    private void xuLyLuuDuLieu() {
        try {
            if (txtHoTen.getText().trim().isEmpty() || txtCanNang.getText().trim().isEmpty()
                    || txtMaKH.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            double canNang = Double.parseDouble(txtCanNang.getText().replace("kg", "").trim());

            ThuCung tc = new ThuCung(
                    txtMaTC.getText(), txtHoTen.getText().trim(), cboLoai.getSelectedItem().toString(),
                    txtGiong.getText().trim(), cboGioiTinh.getSelectedItem().toString(),
                    canNang, txtMaKH.getText().trim(), cboTrangThai.getSelectedItem().toString());

            boolean success = isEditMode ? dao.sua(tc) : dao.them(tc);

            if (success) {
                JOptionPane.showMessageDialog(this, isEditMode ? "Cập nhật thành công!" : "Thêm mới thành công!");
                timKiem(); // Làm mới lại danh sách nhưng vẫn giữ các filter đang chọn
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thao tác thất bại. Hãy kiểm tra Mã Khách Hàng có tồn tại không!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cân nặng phải là một con số!");
        }
    }

    private void xoaThuCung(String maTC) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa thú cưng " + maTC
                        + "?\nKhuyến nghị: Chuyển Trạng thái sang 'Không hoạt động' thay vì xóa vĩnh viễn.",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.xoa(maTC)) {
                JOptionPane.showMessageDialog(this, "Đã xóa thú cưng!");
                timKiem(); // Gọi tìm kiếm để giữ nguyên trạng thái filter đang áp dụng
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Không thể xóa! Thú cưng này đang có dữ liệu trong Lịch Hẹn hoặc Dịch Vụ.", "Lỗi Ràng Buộc",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================
    // LỚP VẼ VÀ BẮT SỰ KIỆN NÚT SỬA/XÓA TRONG BẢNG
    // =========================================================
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
        private String currentMaTC;

        public ActionButtonEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(Color.WHITE);

            JButton btnEdit = new JButton("Sửa");
            btnEdit.setBackground(NAVY);
            btnEdit.setForeground(Color.WHITE);
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                hienThiThongTinSua(currentMaTC);
            });

            JButton btnDelete = new JButton("Xóa");
            btnDelete.setBackground(new Color(220, 53, 69));
            btnDelete.setForeground(Color.WHITE);
            btnDelete.addActionListener(e -> {
                fireEditingStopped();
                xoaThuCung(currentMaTC);
            });

            panel.add(btnEdit);
            panel.add(btnDelete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            currentMaTC = table.getValueAt(row, 0).toString();
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}