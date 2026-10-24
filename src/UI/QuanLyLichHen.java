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
import java.time.LocalTime;
import java.time.format.ResolverStyle;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;
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
        txtNgay.setEditable(false);
        txtNgay.setFocusable(false);
        txtNgay.setToolTipText("Chỉ chọn ngày bằng nút Lịch; nút x để bỏ lọc ngày");
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
        JButton btnXoaNgay = new JButton("Xóa ngày");
        btnXoaNgay.setBackground(new Color(108, 117, 125));
        btnXoaNgay.setForeground(Color.WHITE);
        btnXoaNgay.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnXoaNgay.setFocusPainted(false);
        btnXoaNgay.setPreferredSize(new Dimension(85, 30));
        btnXoaNgay.setToolTipText("Bỏ lọc ngày và hiển thị lịch của tất cả các ngày");

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
        cboTrangThai = new JComboBox<>(new String[] { "Tất cả", "Xác nhận", "Đã hủy" });
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
        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setBackground(new Color(226, 233, 241));
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.addActionListener(e -> {
            txtNgay.setText("");
            txtTimKiem.setText("");
            cboNhanVien.setSelectedIndex(0);
            cboTrangThai.setSelectedIndex(0);
            loadComboBox();
            locDuLieu();
        });
        filterRow.add(btnLamMoi);
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
        cboNhanVien.setSelectedItem("Tất cả");
    }

    public void loadData() {
        locDuLieu();
    }

    private void locDuLieu() {
        // Khi JComboBox đang tải lại, lựa chọn có thể tạm thời là null.
        if (cboNhanVien == null || cboTrangThai == null || tableModel == null)
            return;

        String ngayStr = txtNgay.getText().trim();
        Object nvDaChon = cboNhanVien.getSelectedItem();
        Object trangThaiDaChon = cboTrangThai.getSelectedItem();
        String nhanVienLoc = nvDaChon == null ? "Tất cả" : nvDaChon.toString();
        String trangThaiLoc = trangThaiDaChon == null ? "Tất cả" : trangThaiDaChon.toString();
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
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hủy lịch hẹn " + maLichHen + "?\nLịch sẽ được giữ trong danh sách với trạng thái Đã hủy.", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.huy(maLichHen)) {
                JOptionPane.showMessageDialog(this, "Đã hủy lịch hẹn. Lịch sử vẫn được giữ lại.");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Không thể hủy lịch đã hủy hoặc đã có phiếu dịch vụ.",
                        "Không thể hủy", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void hienFormDatLich(LichHen lhEdit) {
        if (lhEdit != null && "Đã hủy".equals(lhEdit.getTrangThai())) {
            JOptionPane.showMessageDialog(this,
                    "Lịch hẹn đã hủy không thể sửa. Hãy tạo lịch mới.",
                    "Lịch đã hủy", JOptionPane.WARNING_MESSAGE);
            return;
        }
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

            btnXoa = new JButton("Hủy");
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

            JButton btnXoa = new JButton("Hủy");
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
        private final boolean chiTuHomNay;
        private JButton btnPrev;
        private JPanel pnlDays;
        private JLabel lblMonthYear;

        public DatePickerDialog(Frame parent) {
            this(parent, null, false);
        }

        public DatePickerDialog(Frame parent, LocalDate ngayHienTai, boolean chiTuHomNay) {
            super(parent, "Chọn ngày", true);
            this.chiTuHomNay = chiTuHomNay;
            this.currentMonth = YearMonth.from(ngayHienTai != null ? ngayHienTai : LocalDate.now());
            setSize(320, 280);
            setLayout(new BorderLayout());
            setResizable(false);
            getContentPane().setBackground(Color.WHITE);

            JPanel pnlHeader = new JPanel(new BorderLayout());
            pnlHeader.setBackground(Color.WHITE);
            pnlHeader.setBorder(new EmptyBorder(10, 10, 10, 10));

            btnPrev = new JButton("◄");
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

            btnPrev.setEnabled(!chiTuHomNay || currentMonth.isAfter(YearMonth.now()));
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

                if (chiTuHomNay && dateOfButton.isBefore(LocalDate.now())) {
                    btnDay.setEnabled(false);
                    btnDay.setForeground(Color.GRAY);
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
        private JComboBox<String> cboKhachHang, cboThuCung, cboNhanVienSpa;
        private JTextField txtFormTrangThai;

        private LichHenDAO lichHenDAO;
        private QuanLyLichHen parentPanel;
        private LichHen lhEdit;

        public DatLichDialog(JFrame parent, QuanLyLichHen panel, LichHenDAO dao, LichHen lhEdit) {
            super(parent, lhEdit == null ? "Đặt Lịch Mới" : "Sửa Lịch Hẹn - " + lhEdit.getMaLichHen(), true);
            this.lichHenDAO = dao;
            this.parentPanel = panel;
            this.lhEdit = lhEdit;

            setSize(800, 500); // Đã thu nhỏ form cho vừa vặn khi không còn trường Ghi chú
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
            // Ngày giờ mặc định phải ở tương lai, kể cả khi đang gần nửa đêm.
            LocalDateTime macDinh = LocalDateTime.now().plusMinutes(15).withSecond(0).withNano(0);
            txtNgayDat = taoTextField(macDinh.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            txtNgayDat.setEditable(false);
            txtNgayDat.setFocusable(false);
            txtNgayDat.setBackground(new Color(244, 247, 250));
            txtNgayDat.setToolTipText("Chỉ thay đổi ngày hẹn bằng nút Lịch");
            pnlNgay.add(txtNgayDat, BorderLayout.CENTER);

            JButton btnLichForm = new JButton("Lịch");
            btnLichForm.setBackground(NAVY);
            btnLichForm.setForeground(Color.WHITE);
            btnLichForm.setFocusPainted(false);
            btnLichForm.addActionListener(e -> {
                LocalDate ngayCu = LocalDate.now();
                try {
                    ngayCu = LocalDate.parse(txtNgayDat.getText().trim(),
                            DateTimeFormatter.ofPattern("dd/MM/uuuu"));
                } catch (DateTimeParseException ex) {
                    // Sử dụng ngày hiện tại khi không có ngày hợp lệ.
                }
                DatePickerDialog picker = new DatePickerDialog(
                        (JFrame) SwingUtilities.getWindowAncestor(this), ngayCu, true);
                picker.setLocationRelativeTo(btnLichForm);
                picker.setVisible(true);
                if (picker.getSelectedDate() != null) {
                    txtNgayDat.setText(picker.getSelectedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            });
            pnlNgay.add(btnLichForm, BorderLayout.EAST);

            txtGioHen = taoOChonGio(macDinh.format(DateTimeFormatter.ofPattern("HH:mm")));
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
            txtFormTrangThai = taoTextField(
                    lhEdit == null ? "Xác nhận" : lhEdit.getTrangThai() == null ? "Xác nhận" : lhEdit.getTrangThai());
            txtFormTrangThai.setEditable(false);
            txtFormTrangThai.setFocusable(false);
            txtFormTrangThai.setBackground(new Color(244, 247, 250));
            txtFormTrangThai.setToolTipText("Tự xác nhận khi tạo mới; nút Hủy trong bảng dùng để hủy lịch");
            styleCombo(cboNhanVienSpa);
            gbc.gridy = 7;
            gbc.gridx = 0;
            pnlForm.add(cboNhanVienSpa, gbc);
            gbc.gridx = 1;
            pnlForm.add(txtFormTrangThai, gbc);

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
            // Nạp khách hàng trước, rồi mới nạp thú cưng tương ứng.
            // Nếu SQL không trả về khách hàng, JComboBox sẽ không có lựa chọn.
            cboKhachHang.removeAllItems();
            for (String[] kh : lichHenDAO.getDanhSachKhachHang()) {
                cboKhachHang.addItem(kh[0] + " - " + kh[1]);
            }
            cboKhachHang.addActionListener(e -> napThuCungTheoKhachHang());
            if (cboKhachHang.getItemCount() > 0)
                cboKhachHang.setSelectedIndex(0);
            napThuCungTheoKhachHang();

            cboNhanVienSpa.removeAllItems();
            for (String[] nv : lichHenDAO.getDanhSachNhanVienSpa()) {
                cboNhanVienSpa.addItem(nv[0] + " - " + nv[1]);
            }
            if (cboNhanVienSpa.getItemCount() > 0)
                cboNhanVienSpa.setSelectedIndex(0);
        }

        private void dienDuLieuSua() {
            if (lhEdit == null)
                return;

            if (lhEdit.getNgayGioHen() != null) {
                txtNgayDat.setText(lhEdit.getNgayGioHen().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                txtGioHen.setText(lhEdit.getNgayGioHen().format(DateTimeFormatter.ofPattern("HH:mm")));
            }

            setComboSelectedById(cboKhachHang, lhEdit.getMaKhachHang());
            napThuCungTheoKhachHang();
            setComboSelectedById(cboThuCung, lhEdit.getMaThuCung());
            setComboSelectedById(cboNhanVienSpa, lhEdit.getMaNhanVien());

            if (lhEdit.getTrangThai() != null) {
                txtFormTrangThai.setText(lhEdit.getTrangThai());
            }
        }

        private void napThuCungTheoKhachHang() {
            if (cboThuCung == null || cboKhachHang == null)
                return;
            Object khDaChon = cboKhachHang.getSelectedItem();
            cboThuCung.removeAllItems();
            if (khDaChon == null) {
                cboThuCung.setToolTipText("Chọn khách hàng trước khi chọn thú cưng.");
                return;
            }
            String maKh = extractId(khDaChon.toString());
            for (String[] tc : lichHenDAO.getDanhSachThuCungTheoKhachHang(maKh)) {
                cboThuCung.addItem(tc[0] + " - " + tc[1]);
            }
            if (cboThuCung.getItemCount() > 0) {
                cboThuCung.setSelectedIndex(0);
                cboThuCung.setToolTipText(null);
            } else {
                cboThuCung.setToolTipText("Khách hàng chưa có thú cưng. Đăng ký trong Danh mục > Thú cưng.");
            }
        }

        private JTextField taoOChonGio(String gioMacDinh) {
            try {
                MaskFormatter dinhDang = new MaskFormatter("##:##");
                dinhDang.setPlaceholderCharacter('_');
                JFormattedTextField txt = new JFormattedTextField(dinhDang);
                txt.setFocusLostBehavior(JFormattedTextField.PERSIST);
                txt.setValue(gioMacDinh);
                txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                txt.setPreferredSize(new Dimension(200, 35));
                txt.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        new EmptyBorder(5, 10, 5, 10)));
                txt.setToolTipText("Giờ theo HH:mm, ví dụ 09:30 (00:00-23:59)");
                return txt;
            } catch (ParseException e) {
                throw new IllegalStateException("Không khởi tạo được định dạng giờ HH:mm", e);
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
                LocalDate ngayHen = LocalDate.parse(ngayStr,
                        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
                LocalTime gioHen = LocalTime.parse(gioStr,
                        DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT));
                LocalDateTime ngayGioHen = LocalDateTime.of(ngayHen, gioHen);
                if (!ngayGioHen.isAfter(LocalDateTime.now())) {
                    JOptionPane.showMessageDialog(this,
                            "Giờ hẹn phải lớn hơn thời gian hiện tại. Vui lòng chọn giờ trong tương lai.",
                            "Ngày giờ không hợp lệ", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // Chỉ đọc lựa chọn một lần; không gọi .toString() khi chưa có dữ liệu.
                Object khDaChon = cboKhachHang.getSelectedItem();
                Object tcDaChon = cboThuCung.getSelectedItem();
                Object nvDaChon = cboNhanVienSpa.getSelectedItem();
                if (khDaChon == null) {
                    JOptionPane.showMessageDialog(this,
                            "Chưa có khách hàng để đặt lịch. Hãy kiểm tra dữ liệu KhachHang hoặc kết nối SQL Server.",
                            "Chưa chọn khách hàng", JOptionPane.WARNING_MESSAGE);
                    cboKhachHang.requestFocusInWindow();
                    return;
                }
                if (tcDaChon == null) {
                    JOptionPane.showMessageDialog(this,
                            "Khách hàng đã chọn chưa có thú cưng. Hãy thêm hồ sơ tại Danh mục > Thú cưng trước.",
                            "Chưa chọn thú cưng", JOptionPane.WARNING_MESSAGE);
                    cboThuCung.requestFocusInWindow();
                    return;
                }
                if (nvDaChon == null) {
                    JOptionPane.showMessageDialog(this,
                            "Chưa có thợ Spa để phân công. Hãy kiểm tra vai trò nhân viên và dữ liệu NhanVien.",
                            "Chưa chọn nhân viên Spa", JOptionPane.WARNING_MESSAGE);
                    cboNhanVienSpa.requestFocusInWindow();
                    return;
                }
                if ("Đã hủy".equals(txtFormTrangThai.getText())) {
                    JOptionPane.showMessageDialog(this, "Lịch đã hủy không thể sửa. Hãy tạo lịch mới.",
                            "Lịch đã hủy", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String maKH = extractId(khDaChon.toString());
                String maTC = extractId(tcDaChon.toString());
                String maNV = extractId(nvDaChon.toString());
                String trangThai = txtFormTrangThai.getText();

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
                JOptionPane.showMessageDialog(this, "Ngày phải chọn từ lịch và giờ phải đúng HH:mm (00:00–23:59).",
                        "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                // Hiển thị lý do do DAO trả về (quá giờ hoặc thợ Spa trùng lịch).
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Không thể lưu lịch hẹn", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Không thể lưu lịch hẹn: " + ex.getMessage() + "\nKiểm tra Console nếu lỗi tiếp tục xảy ra.",
                        "Lỗi đặt lịch", JOptionPane.ERROR_MESSAGE);
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