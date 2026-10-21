package UI;

import dao.LichHenDAO;
import entity.LichHen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class QuanLyLichHen extends JPanel {

    private final Color NAVY = new Color(18, 48, 82);
    private final Color LIGHT_BLUE = new Color(229, 241, 252);
    private final Color BLUE = new Color(24, 144, 255);
    private final Color DARK_BLUE = new Color(11, 43, 64);
    private final Color GRAY_BTN = new Color(136, 142, 148);
    private final Color STATUS_GREEN = new Color(115, 214, 137);
    private final Color STATUS_GRAY = new Color(200, 200, 200);
    private final Color STATUS_RED = new Color(235, 138, 138);

    private JTextField txtNgay, txtTimKiem;
    private JComboBox<String> cboNhanVien, cboTrangThai;
    private JTable table;
    private DefaultTableModel tableModel;
    private LichHenDAO dao;

    public QuanLyLichHen() {
        dao = new LichHenDAO();
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

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ LỊCH HẸN SPA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(NAVY);
        titleRow.add(lblTitle, BorderLayout.WEST);

        JButton btnDatLich = new JButton("Đặt Lịch Mới");
        btnDatLich.setBackground(BLUE);
        btnDatLich.setForeground(Color.WHITE);
        btnDatLich.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDatLich.setFocusPainted(false);
        btnDatLich.setBorderPainted(false);
        btnDatLich.setPreferredSize(new Dimension(140, 38));
        btnDatLich.addActionListener(e -> hienFormDatLich(null));
        titleRow.add(btnDatLich, BorderLayout.EAST);

        panel.add(titleRow, BorderLayout.NORTH);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterRow.setBackground(Color.WHITE);
        filterRow.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));

        filterRow.add(createFilterLabel("Ngày"));
        txtNgay = new JTextField(8);
        filterRow.add(txtNgay);

        JButton btnLich = new JButton("Lịch");
        btnLich.setBackground(NAVY);
        btnLich.setForeground(Color.WHITE);
        btnLich.setFocusPainted(false);
        btnLich.setPreferredSize(new Dimension(60, 25));
        btnLich.addActionListener(e -> {
            DatePickerDialog picker = new DatePickerDialog((JFrame) SwingUtilities.getWindowAncestor(this));
            picker.setLocationRelativeTo(btnLich);
            picker.setVisible(true);
            if (picker.getSelectedDate() != null) {
                txtNgay.setText(picker.getSelectedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                locDuLieu();
            }
        });
        filterRow.add(btnLich);

        JButton btnXoaNgay = new JButton("x");
        btnXoaNgay.setBackground(Color.GRAY);
        btnXoaNgay.setForeground(Color.WHITE);
        btnXoaNgay.setFocusPainted(false);
        btnXoaNgay.setPreferredSize(new Dimension(40, 25));
        btnXoaNgay.addActionListener(e -> {
            txtNgay.setText("");
            locDuLieu();
        });
        filterRow.add(btnXoaNgay);

        filterRow.add(Box.createHorizontalStrut(5));

        filterRow.add(createFilterLabel("Nhân viên"));
        cboNhanVien = new JComboBox<>();
        cboNhanVien.setPreferredSize(new Dimension(140, 30));
        filterRow.add(cboNhanVien);

        filterRow.add(createFilterLabel("Trạng thái"));
        cboTrangThai = new JComboBox<>(new String[] { "Tất cả", "Xác nhận", "Chờ xác nhận", "Đã hủy" });
        cboTrangThai.setPreferredSize(new Dimension(110, 30));
        filterRow.add(cboTrangThai);

        filterRow.add(createFilterLabel("Tìm kiếm"));
        txtTimKiem = new JTextField(12);
        txtTimKiem.setToolTipText("Mã lịch, tên KH, thú cưng...");
        filterRow.add(txtTimKiem);

        JButton btnTimKiem = new JButton("Lọc");
        btnTimKiem.setBackground(STATUS_GREEN);
        btnTimKiem.setForeground(Color.WHITE);
        btnTimKiem.setFocusPainted(false);
        filterRow.add(btnTimKiem);

        btnTimKiem.addActionListener(e -> locDuLieu());
        txtTimKiem.addActionListener(e -> locDuLieu());
        txtNgay.addActionListener(e -> locDuLieu());
        cboNhanVien.addActionListener(e -> locDuLieu());
        cboTrangThai.addActionListener(e -> locDuLieu());

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

        // Đã xóa hoàn toàn cột Ghi Chú
        String[] columns = { "Mã Lịch", "Ngày & Giờ", "Tên Khách Hàng", "Tên Thú Cưng", "Nhân Viên Spa", "Trạng Thái",
                "Thao Tác" };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Cột Thao tác lùi về index 6
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(100, 40));

        // Canh chỉnh lại độ rộng cột cho đẹp
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(130);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        table.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionButtonEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void loadComboBox() {
        cboNhanVien.removeAllItems();
        cboNhanVien.addItem("Tất cả");
        for (String[] nv : dao.getDanhSachNhanVienSpa()) {
            cboNhanVien.addItem(nv[1]);
        }
    }

    public void loadData() {
        locDuLieu();
    }

    private void locDuLieu() {
        if (cboNhanVien == null || cboTrangThai == null)
            return;

        String ngayStr = txtNgay.getText().trim();
        String nhanVienLoc = cboNhanVien.getSelectedItem().toString();
        String trangThaiLoc = cboTrangThai.getSelectedItem().toString();
        String tuKhoa = txtTimKiem.getText().trim().toLowerCase();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate ngayLoc = null;

        try {
            if (!ngayStr.isEmpty())
                ngayLoc = LocalDate.parse(ngayStr, formatter);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày lọc phải có định dạng dd/MM/yyyy", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);

        for (LichHen lh : dao.getAll()) {
            boolean isMatch = true;

            if (ngayLoc != null) {
                if (lh.getNgayGioHen() == null || !lh.getNgayGioHen().toLocalDate().equals(ngayLoc)) {
                    isMatch = false;
                }
            }

            if (!nhanVienLoc.equals("Tất cả")) {
                if (lh.getTenNhanVien() == null || !lh.getTenNhanVien().equals(nhanVienLoc)) {
                    isMatch = false;
                }
            }

            if (!trangThaiLoc.equals("Tất cả")) {
                if (lh.getTrangThai() == null || !lh.getTrangThai().equals(trangThaiLoc)) {
                    isMatch = false;
                }
            }

            if (!tuKhoa.isEmpty()) {
                String maLH = lh.getMaLichHen() != null ? lh.getMaLichHen().toLowerCase() : "";
                String tenKH = lh.getTenKhachHang() != null ? lh.getTenKhachHang().toLowerCase() : "";
                String tenTC = lh.getTenThuCung() != null ? lh.getTenThuCung().toLowerCase() : "";

                if (!maLH.contains(tuKhoa) && !tenKH.contains(tuKhoa) && !tenTC.contains(tuKhoa)) {
                    isMatch = false;
                }
            }

            if (isMatch) {
                tableModel.addRow(new Object[] {
                        lh.getMaLichHen(), lh.getNgayGioHenFormat(), lh.getTenKhachHang(),
                        lh.getTenThuCung(), lh.getTenNhanVien(), lh.getTrangThai(), ""
                });
            }
        }
    }

    private LichHen getLichHenByMa(String maLichHen) {
        for (LichHen lh : dao.getAll()) {
            if (lh.getMaLichHen().equals(maLichHen)) {
                return lh;
            }
        }
        return null;
    }

    private void xoaLichHen(String maLichHen) {
        int confirm = JOptionPane.showConfirmDialog(this, "Hủy lịch hẹn " + maLichHen + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && dao.xoa(maLichHen)) {
            JOptionPane.showMessageDialog(this, "Đã xóa lịch hẹn!");
            loadData();
        }
    }

    private void hienFormDatLich(LichHen lhEdit) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        DatLichDialog dialog = new DatLichDialog(parentFrame, this, dao, lhEdit);
        dialog.setVisible(true);
    }

    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus,
                int r, int c) {
            Component comp = super.getTableCellRendererComponent(table, value, isSel, hasFocus, r, c);
            setHorizontalAlignment(SwingConstants.CENTER);
            String st = value != null ? value.toString() : "";
            if (st.equals("Xác nhận"))
                comp.setBackground(STATUS_GREEN);
            else if (st.equals("Chờ xác nhận"))
                comp.setBackground(STATUS_GRAY);
            else if (st.equals("Đã hủy"))
                comp.setBackground(STATUS_RED);
            else
                comp.setBackground(Color.WHITE);
            return comp;
        }
    }

    private class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton btnSua, btnXoa;

        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setOpaque(true);
            btnSua = new JButton("Sửa");
            btnSua.setBackground(NAVY);
            btnSua.setForeground(Color.WHITE);
            btnSua.setPreferredSize(new Dimension(60, 30));

            btnXoa = new JButton("Xóa");
            btnXoa.setBackground(new Color(220, 53, 69));
            btnXoa.setForeground(Color.WHITE);
            btnXoa.setPreferredSize(new Dimension(60, 30));

            add(btnSua);
            add(btnXoa);
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
        private String currentMaLich;

        public ActionButtonEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(Color.WHITE);

            JButton btnSua = new JButton("Sửa");
            btnSua.setBackground(NAVY);
            btnSua.setForeground(Color.WHITE);
            btnSua.setPreferredSize(new Dimension(60, 30));
            btnSua.addActionListener(e -> {
                fireEditingStopped();
                LichHen lhEdit = getLichHenByMa(currentMaLich);
                if (lhEdit != null) {
                    hienFormDatLich(lhEdit);
                }
            });

            JButton btnXoa = new JButton("Xóa");
            btnXoa.setBackground(new Color(220, 53, 69));
            btnXoa.setForeground(Color.WHITE);
            btnXoa.setPreferredSize(new Dimension(60, 30));
            btnXoa.addActionListener(e -> {
                fireEditingStopped();
                xoaLichHen(currentMaLich);
            });

            panel.add(btnSua);
            panel.add(btnXoa);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            currentMaLich = table.getValueAt(row, 0).toString();
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    public static class DatePickerDialog extends JDialog {
        private LocalDate selectedDate = null;
        private YearMonth currentMonth = YearMonth.now();
        private JPanel pnlDays;
        private JLabel lblMonthYear;

        public DatePickerDialog(Frame parent) {
            super(parent, "Chọn ngày", true);
            setSize(320, 280);
            setLayout(new BorderLayout());
            setResizable(false);
            getContentPane().setBackground(Color.WHITE);

            JPanel pnlHeader = new JPanel(new BorderLayout());
            pnlHeader.setBackground(Color.WHITE);
            pnlHeader.setBorder(new EmptyBorder(10, 10, 10, 10));

            JButton btnPrev = new JButton("◄");
            btnPrev.setFocusPainted(false);
            btnPrev.setBackground(new Color(229, 241, 252));
            btnPrev.addActionListener(e -> {
                currentMonth = currentMonth.minusMonths(1);
                updateCalendar();
            });

            lblMonthYear = new JLabel("", SwingConstants.CENTER);
            lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblMonthYear.setForeground(new Color(18, 48, 82));

            JButton btnNext = new JButton("►");
            btnNext.setFocusPainted(false);
            btnNext.setBackground(new Color(229, 241, 252));
            btnNext.addActionListener(e -> {
                currentMonth = currentMonth.plusMonths(1);
                updateCalendar();
            });

            pnlHeader.add(btnPrev, BorderLayout.WEST);
            pnlHeader.add(lblMonthYear, BorderLayout.CENTER);
            pnlHeader.add(btnNext, BorderLayout.EAST);

            add(pnlHeader, BorderLayout.NORTH);

            pnlDays = new JPanel(new GridLayout(0, 7, 2, 2));
            pnlDays.setBackground(Color.WHITE);
            pnlDays.setBorder(new EmptyBorder(0, 10, 10, 10));
            add(pnlDays, BorderLayout.CENTER);

            JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            pnlFooter.setBackground(Color.WHITE);

            JButton btnToday = new JButton("Hôm nay");
            btnToday.setBackground(new Color(229, 241, 252));
            btnToday.setFocusPainted(false);
            btnToday.addActionListener(e -> {
                selectedDate = LocalDate.now();
                dispose();
            });

            JButton btnCancel = new JButton("Hủy");
            btnCancel.setBackground(new Color(229, 241, 252));
            btnCancel.setFocusPainted(false);
            btnCancel.addActionListener(e -> dispose());

            pnlFooter.add(btnToday);
            pnlFooter.add(btnCancel);
            add(pnlFooter, BorderLayout.SOUTH);

            updateCalendar();
        }

        private void updateCalendar() {
            pnlDays.removeAll();
            lblMonthYear.setText("Tháng " + currentMonth.getMonthValue() + " / " + currentMonth.getYear());

            String[] daysOfWeek = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
            for (String day : daysOfWeek) {
                JLabel lbl = new JLabel(day, SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(new Color(18, 48, 82));
                pnlDays.add(lbl);
            }

            LocalDate firstDayOfMonth = currentMonth.atDay(1);
            int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue();
            int daysInMonth = currentMonth.lengthOfMonth();

            for (int i = 1; i < dayOfWeekValue; i++) {
                pnlDays.add(new JLabel(""));
            }

            for (int i = 1; i <= daysInMonth; i++) {
                JButton btnDay = new JButton(String.valueOf(i));
                btnDay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btnDay.setFocusPainted(false);
                btnDay.setBackground(Color.WHITE);
                btnDay.setBorder(new LineBorder(new Color(200, 200, 200)));

                LocalDate dateOfButton = currentMonth.atDay(i);
                if (dateOfButton.equals(LocalDate.now())) {
                    btnDay.setBackground(new Color(229, 241, 252));
                    btnDay.setFont(new Font("Segoe UI", Font.BOLD, 12));
                }

                btnDay.addActionListener(e -> {
                    selectedDate = dateOfButton;
                    dispose();
                });
                pnlDays.add(btnDay);
            }

            pnlDays.revalidate();
            pnlDays.repaint();
        }

        public LocalDate getSelectedDate() {
            return selectedDate;
        }
    }

    class DatLichDialog extends JDialog {
        private JTextField txtNgayDat, txtGioHen;
        private JComboBox<String> cboKhachHang, cboThuCung, cboNhanVienSpa, cboFormTrangThai;

        private LichHenDAO lichHenDAO;
        private QuanLyLichHen parentPanel;
        private LichHen lhEdit;

        public DatLichDialog(JFrame parent, QuanLyLichHen panel, LichHenDAO dao, LichHen lhEdit) {
            super(parent, lhEdit == null ? "Đặt Lịch Mới" : "Sửa Lịch Hẹn - " + lhEdit.getMaLichHen(), true);
            this.lichHenDAO = dao;
            this.parentPanel = panel;
            this.lhEdit = lhEdit;

            setSize(750, 380); // Đã thu nhỏ form cho vừa vặn khi không còn trường Ghi chú
            setLocationRelativeTo(parent);
            setResizable(false);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);

            JPanel pnlForm = new JPanel(new GridBagLayout());
            pnlForm.setBackground(Color.WHITE);
            pnlForm.setBorder(new EmptyBorder(20, 30, 20, 30));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(8, 10, 8, 10);
            gbc.weightx = 0.5;

            // 1. Ngày & Giờ
            gbc.gridx = 0;
            gbc.gridy = 0;
            pnlForm.add(taoLabel("Ngày đặt: (dd/MM/yyyy)"), gbc);
            gbc.gridx = 1;
            pnlForm.add(taoLabel("Giờ hẹn: (HH:mm)"), gbc);

            JPanel pnlNgay = new JPanel(new BorderLayout());
            pnlNgay.setOpaque(false);
            txtNgayDat = taoTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            pnlNgay.add(txtNgayDat, BorderLayout.CENTER);

            JButton btnLichForm = new JButton("Lịch");
            btnLichForm.setBackground(NAVY);
            btnLichForm.setForeground(Color.WHITE);
            btnLichForm.setFocusPainted(false);
            btnLichForm.addActionListener(e -> {
                DatePickerDialog picker = new DatePickerDialog((JFrame) SwingUtilities.getWindowAncestor(this));
                picker.setLocationRelativeTo(btnLichForm);
                picker.setVisible(true);
                if (picker.getSelectedDate() != null) {
                    txtNgayDat.setText(picker.getSelectedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            });
            pnlNgay.add(btnLichForm, BorderLayout.EAST);

            txtGioHen = taoTextField("09:00");
            gbc.gridy = 1;
            gbc.gridx = 0;
            pnlForm.add(pnlNgay, gbc);
            gbc.gridx = 1;
            pnlForm.add(txtGioHen, gbc);

            // Tiêu đề Phân cách 1
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            pnlForm.add(taoTitleLabel("Thông tin Khách hàng & Thú cưng"), gbc);
            gbc.gridwidth = 1;

            // 2. Khách hàng & Thú cưng
            gbc.gridx = 0;
            gbc.gridy = 3;
            pnlForm.add(taoLabel("Khách hàng:"), gbc);
            gbc.gridx = 1;
            pnlForm.add(taoLabel("Thú cưng:"), gbc);

            cboKhachHang = new JComboBox<>();
            cboThuCung = new JComboBox<>();
            styleCombo(cboKhachHang);
            styleCombo(cboThuCung);
            gbc.gridy = 4;
            gbc.gridx = 0;
            pnlForm.add(cboKhachHang, gbc);
            gbc.gridx = 1;
            pnlForm.add(cboThuCung, gbc);

            // Tiêu đề Phân cách 2
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 2;
            pnlForm.add(taoTitleLabel("Phân công & Trạng thái"), gbc);
            gbc.gridwidth = 1;

            // 3. Nhân viên & Trạng thái
            gbc.gridx = 0;
            gbc.gridy = 6;
            pnlForm.add(taoLabel("Nhân viên Spa:"), gbc);
            gbc.gridx = 1;
            pnlForm.add(taoLabel("Trạng thái:"), gbc);

            cboNhanVienSpa = new JComboBox<>();
            cboFormTrangThai = new JComboBox<>(new String[] { "Xác nhận", "Chờ xác nhận", "Đã hủy" });
            styleCombo(cboNhanVienSpa);
            styleCombo(cboFormTrangThai);
            gbc.gridy = 7;
            gbc.gridx = 0;
            pnlForm.add(cboNhanVienSpa, gbc);
            gbc.gridx = 1;
            pnlForm.add(cboFormTrangThai, gbc);

            // Đã xóa hoàn toàn Phần 4 (Ghi chú / Dịch vụ)

            add(pnlForm, BorderLayout.CENTER);

            // --- BOTTOM BUTTONS ---
            JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
            pnlBtn.setBackground(Color.WHITE);

            JButton btnHuy = new JButton("Hủy");
            btnHuy.setBackground(GRAY_BTN);
            btnHuy.setForeground(Color.WHITE);
            btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnHuy.setPreferredSize(new Dimension(100, 35));
            btnHuy.addActionListener(e -> dispose());

            JButton btnLuu = new JButton(lhEdit == null ? "Thêm mới" : "Cập nhật");
            btnLuu.setBackground(DARK_BLUE);
            btnLuu.setForeground(Color.WHITE);
            btnLuu.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnLuu.setPreferredSize(new Dimension(120, 35));
            btnLuu.addActionListener(e -> xuLyLuuDuLieu());

            pnlBtn.add(btnHuy);
            pnlBtn.add(btnLuu);
            add(pnlBtn, BorderLayout.SOUTH);

            loadComboData();
            dienDuLieuSua();
        }

        private void loadComboData() {
            for (String[] kh : lichHenDAO.getDanhSachKhachHang())
                cboKhachHang.addItem(kh[0] + " - " + kh[1]);
            for (String[] tc : lichHenDAO.getDanhSachThuCung())
                cboThuCung.addItem(tc[0] + " - " + tc[1]);
            for (String[] nv : lichHenDAO.getDanhSachNhanVienSpa())
                cboNhanVienSpa.addItem(nv[0] + " - " + nv[1]);
        }

        private void dienDuLieuSua() {
            if (lhEdit == null)
                return;

            if (lhEdit.getNgayGioHen() != null) {
                txtNgayDat.setText(lhEdit.getNgayGioHen().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                txtGioHen.setText(lhEdit.getNgayGioHen().format(DateTimeFormatter.ofPattern("HH:mm")));
            }

            setComboSelectedById(cboKhachHang, lhEdit.getMaKhachHang());
            setComboSelectedById(cboThuCung, lhEdit.getMaThuCung());
            setComboSelectedById(cboNhanVienSpa, lhEdit.getMaNhanVien());

            if (lhEdit.getTrangThai() != null) {
                cboFormTrangThai.setSelectedItem(lhEdit.getTrangThai());
            }
        }

        private void setComboSelectedById(JComboBox<String> cbo, String id) {
            if (id == null)
                return;
            for (int i = 0; i < cbo.getItemCount(); i++) {
                if (cbo.getItemAt(i).startsWith(id + " - ")) {
                    cbo.setSelectedIndex(i);
                    break;
                }
            }
        }

        private void xuLyLuuDuLieu() {
            try {
                String ngayStr = txtNgayDat.getText().trim();
                String gioStr = txtGioHen.getText().trim();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                LocalDateTime ngayGioHen = LocalDateTime.parse(ngayStr + " " + gioStr, formatter);

                String maKH = extractId(cboKhachHang.getSelectedItem().toString());
                String maTC = extractId(cboThuCung.getSelectedItem().toString());
                String maNV = extractId(cboNhanVienSpa.getSelectedItem().toString());
                String trangThai = cboFormTrangThai.getSelectedItem().toString();

                String maLH = (lhEdit == null) ? taoMaLichHenTuDong() : lhEdit.getMaLichHen();

                // Trực tiếp truyền chuỗi rỗng "" vào vị trí của biến ghi chú
                LichHen lh = new LichHen(maLH, ngayGioHen, "", maKH, maNV, maTC);
                lh.setTrangThai(trangThai);

                boolean thanhCong;
                if (lhEdit == null) {
                    thanhCong = lichHenDAO.them(lh);
                } else {
                    thanhCong = lichHenDAO.sua(lh);
                }

                if (thanhCong) {
                    JOptionPane.showMessageDialog(this,
                            lhEdit == null ? "Đã thêm lịch hẹn mới!" : "Đã cập nhật lịch hẹn!");
                    parentPanel.loadData();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Thao tác thất bại. Vui lòng kiểm tra lại!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng Ngày (dd/MM/yyyy) và Giờ (HH:mm)",
                        "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        }

        private String extractId(String item) {
            if (item == null)
                return "";
            int idx = item.indexOf(" - ");
            return idx > 0 ? item.substring(0, idx) : item;
        }

        private String taoMaLichHenTuDong() {
            int max = 0;
            for (LichHen lh : lichHenDAO.getAll()) {
                if (lh.getMaLichHen().startsWith("LH")) {
                    try {
                        int num = Integer.parseInt(lh.getMaLichHen().substring(2));
                        if (num > max)
                            max = num;
                    } catch (Exception e) {
                    }
                }
            }
            return String.format("LH%03d", max + 1);
        }

        private JLabel taoLabel(String text) {
            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(Color.GRAY);
            return lbl;
        }

        private JLabel taoTitleLabel(String text) {
            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lbl.setForeground(Color.BLACK);
            return lbl;
        }

        private JTextField taoTextField(String defaultText) {
            JTextField txt = new JTextField(defaultText);
            txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txt.setPreferredSize(new Dimension(200, 35));
            txt.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(5, 10, 5, 10)));
            return txt;
        }

        private void styleCombo(JComboBox<String> cbo) {
            cbo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cbo.setPreferredSize(new Dimension(200, 35));
            cbo.setBackground(Color.WHITE);
        }
    }
}