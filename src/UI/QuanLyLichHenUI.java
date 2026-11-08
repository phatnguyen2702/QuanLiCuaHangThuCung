package UI;

import control.LichHenControl;
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
import javax.swing.text.MaskFormatter;
import java.text.ParseException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class QuanLyLichHenUI extends JPanel {

    private static final Color NAVY = new Color(18, 48, 82);
    private static final Color LIGHT_BLUE = new Color(229, 241, 252);
    private static final Color BLUE = new Color(24, 144, 255);
    private static final Color DARK_BLUE = new Color(11, 43, 64);
    private static final Color GRAY_BTN = new Color(136, 142, 148);
    private static final Color STATUS_GREEN = new Color(115, 214, 137);
    private static final Color STATUS_GRAY = new Color(200, 200, 200);
    private static final Color STATUS_RED = new Color(235, 138, 138);

    private JTextField txtNgay, txtTimKiem;
    private JComboBox<String> cmbNhanVien, cmbTrangThai;
    private JTable tblDuLieu;
    private DefaultTableModel tableModel;
    private final LichHenControl control = new LichHenControl();
    private final ArrayList<LichHen> danhSachLichHen = new ArrayList<>();

    public QuanLyLichHenUI() {
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

        JLabel lblTitle = new JLabel("QUẢN LÝ LỊCH HẸN SPA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(NAVY);
        pnlTitleRow.add(lblTitle, BorderLayout.WEST);

        JButton btnDatLich = new JButton("Đặt Lịch Mới");
        btnDatLich.setBackground(BLUE);
        btnDatLich.setForeground(Color.WHITE);
        btnDatLich.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDatLich.setFocusPainted(false);
        btnDatLich.setBorderPainted(false);
        btnDatLich.setPreferredSize(new Dimension(140, 38));
        btnDatLich.addActionListener(e -> hienFormDatLich(null));
        pnlTitleRow.add(btnDatLich, BorderLayout.EAST);

        pnlKhung.add(pnlTitleRow, BorderLayout.NORTH);

        JPanel pnlFilterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlFilterRow.setBackground(Color.WHITE);
        pnlFilterRow.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));

        pnlFilterRow.add(createFilterLabel("Ngày"));
        txtNgay = new JTextField(8);
        txtNgay.setEditable(false);
        txtNgay.setFocusable(false);
        txtNgay.setToolTipText("Chỉ chọn ngày bằng nút Lịch; nút x để bỏ lọc ngày");
        pnlFilterRow.add(txtNgay);

        JButton btnLich = new JButton("Lịch");
        btnLich.setBackground(NAVY);
        btnLich.setForeground(Color.WHITE);
        btnLich.setFocusPainted(false);
        btnLich.setPreferredSize(new Dimension(60, 25));
        btnLich.addActionListener(e -> {
            DatePickerDialog winPicker = new DatePickerDialog((JFrame) SwingUtilities.getWindowAncestor(this));
            winPicker.setLocationRelativeTo(btnLich);
            winPicker.setVisible(true);
            if (winPicker.getSelectedDate() != null) {
                txtNgay.setText(winPicker.getSelectedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                locDuLieu();
            }
        });
        pnlFilterRow.add(btnLich);
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

        pnlFilterRow.add(btnXoaNgay);

        pnlFilterRow.add(Box.createHorizontalStrut(5));

        pnlFilterRow.add(createFilterLabel("Nhân viên"));
        cmbNhanVien = new JComboBox<>();
        cmbNhanVien.setPreferredSize(new Dimension(140, 30));
        pnlFilterRow.add(cmbNhanVien);

        pnlFilterRow.add(createFilterLabel("Trạng thái"));
        cmbTrangThai = new JComboBox<>(new String[] { "Tất cả", "Xác nhận", "Đã hủy" });
        cmbTrangThai.setPreferredSize(new Dimension(110, 30));
        pnlFilterRow.add(cmbTrangThai);

        pnlFilterRow.add(createFilterLabel("Tìm kiếm"));
        txtTimKiem = new JTextField(12);
        txtTimKiem.setToolTipText("Mã lịch, tên KH, thú cưng...");
        pnlFilterRow.add(txtTimKiem);

        JButton btnTimKiem = new JButton("Lọc");
        btnTimKiem.setBackground(STATUS_GREEN);
        btnTimKiem.setForeground(Color.WHITE);
        btnTimKiem.setFocusPainted(false);
        pnlFilterRow.add(btnTimKiem);

        btnTimKiem.addActionListener(e -> locDuLieu());
        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setBackground(new Color(226, 233, 241));
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.addActionListener(e -> {
            txtNgay.setText("");
            txtTimKiem.setText("");
            cmbNhanVien.setSelectedIndex(0);
            cmbTrangThai.setSelectedIndex(0);
            loadComboBox();
            loadData();
        });
        pnlFilterRow.add(btnLamMoi);
        txtTimKiem.addActionListener(e -> locDuLieu());
        txtNgay.addActionListener(e -> locDuLieu());
        cmbNhanVien.addActionListener(e -> locDuLieu());
        cmbTrangThai.addActionListener(e -> locDuLieu());

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

        String[] columns = { "Mã Lịch", "Ngày & Giờ", "Tên Khách Hàng", "Tên Thú Cưng", "Nhân Viên Spa", "Trạng Thái",
                "Thao Tác" };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        tblDuLieu = new JTable(tableModel);
        tblDuLieu.setRowHeight(45);
        tblDuLieu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblDuLieu.getTableHeader().setBackground(Color.WHITE);
        tblDuLieu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDuLieu.getTableHeader().setPreferredSize(new Dimension(100, 40));

        tblDuLieu.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblDuLieu.getColumnModel().getColumn(1).setPreferredWidth(140);
        tblDuLieu.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblDuLieu.getColumnModel().getColumn(3).setPreferredWidth(150);
        tblDuLieu.getColumnModel().getColumn(4).setPreferredWidth(130);
        tblDuLieu.getColumnModel().getColumn(5).setPreferredWidth(100);
        tblDuLieu.getColumnModel().getColumn(6).setPreferredWidth(130);

        DefaultTableCellRenderer lblCenter = new DefaultTableCellRenderer();
        lblCenter.setHorizontalAlignment(SwingConstants.CENTER);
        tblDuLieu.getColumnModel().getColumn(0).setCellRenderer(lblCenter);
        tblDuLieu.getColumnModel().getColumn(1).setCellRenderer(lblCenter);
        tblDuLieu.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        tblDuLieu.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());
        tblDuLieu.getColumnModel().getColumn(6).setCellEditor(new ActionButtonEditor());

        JScrollPane scrCuon2 = new JScrollPane(tblDuLieu);
        scrCuon2.getViewport().setBackground(Color.WHITE);
        pnlKhung.add(scrCuon2, BorderLayout.CENTER);
        return pnlKhung;
    }

    private void loadComboBox() {
        cmbNhanVien.removeAllItems();
        cmbNhanVien.addItem("Tất cả");
        for (String[] nv : control.layDanhSachNhanVienSpa()) {
            cmbNhanVien.addItem(nv[1]);
        }
        cmbNhanVien.setSelectedItem("Tất cả");
    }

    public void loadData() {
        danhSachLichHen.clear();
        danhSachLichHen.addAll(control.layDanhSach());
        locDuLieu();
    }

    private void locDuLieu() {
        if (cmbNhanVien == null || cmbTrangThai == null || tableModel == null
                || cmbNhanVien.getSelectedItem() == null || cmbTrangThai.getSelectedItem() == null)
            return;

        try {
            ArrayList<LichHen> ketQua = control.locDanhSach(
                    danhSachLichHen, txtNgay.getText(), cmbNhanVien.getSelectedItem().toString(),
                    cmbTrangThai.getSelectedItem().toString(), txtTimKiem.getText());
            tableModel.setRowCount(0);
            for (LichHen lichHen : ketQua) {
                tableModel.addRow(new Object[] {
                        lichHen.getMaLichHen(), lichHen.getNgayGioHenFormat(),
                        lichHen.getTenKhachHang(), lichHen.getTenThuCung(),
                        lichHen.getTenNhanVien(), lichHen.getTrangThai(), ""
                });
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi lọc dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private LichHen getLichHenByMa(String maLichHen) {
        return control.timTheoMa(maLichHen);
    }

    private void xoaLichHen(String maLichHen) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hủy lịch hẹn " + maLichHen + "?\nLịch sẽ được giữ trong danh sách với trạng thái Đã hủy.", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (control.huy(maLichHen)) {
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
        if (lhEdit != null && !control.coTheSua(lhEdit)) {
            JOptionPane.showMessageDialog(this,
                    "Lịch hẹn đã hủy không thể sửa. Hãy tạo lịch mới.",
                    "Lịch đã hủy", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFrame winParentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        DatLichDialog winDialog = new DatLichDialog(winParentFrame, this, lhEdit);
        winDialog.setVisible(true);
    }

    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tblDuLieu, Object value, boolean isSel, boolean hasFocus,
                int r, int c) {
            Component comp = super.getTableCellRendererComponent(tblDuLieu, value, isSel, hasFocus, r, c);
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
        public Component getTableCellRendererComponent(JTable tblDuLieu, Object value, boolean isSelected,
                boolean hasFocus,
                int row, int column) {
            setBackground(isSelected ? tblDuLieu.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel pnlKhung;
        private String currentMaLich;

        public ActionButtonEditor() {
            pnlKhung = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            pnlKhung.setBackground(Color.WHITE);

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

            pnlKhung.add(btnSua);
            pnlKhung.add(btnXoa);
        }

        @Override
        public Component getTableCellEditorComponent(JTable tblDuLieu, Object value, boolean isSelected, int row,
                int column) {
            currentMaLich = tblDuLieu.getValueAt(row, 0).toString();
            pnlKhung.setBackground(tblDuLieu.getSelectionBackground());
            return pnlKhung;
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

        public DatePickerDialog(Frame winParent) {
            this(winParent, null, false);
        }

        public DatePickerDialog(Window winParent, LocalDate ngayHienTai, boolean chiTuHomNay) {
            super(winParent, "Chọn ngày", Dialog.ModalityType.APPLICATION_MODAL);
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
                JLabel lblLbl = new JLabel(day, SwingConstants.CENTER);
                lblLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblLbl.setForeground(new Color(18, 48, 82));
                pnlDays.add(lblLbl);
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
        private JComboBox<String> cmbKhachHang, cmbThuCung, cmbNhanVienSpa;
        private JTextField txtFormTrangThai;

        private final QuanLyLichHenUI pnlParentPanel;
        private final LichHen lhEdit;

        public DatLichDialog(JFrame winParent, QuanLyLichHenUI pnlKhung, LichHen lhEdit) {
            super(winParent, lhEdit == null ? "Đặt Lịch Mới" : "Sửa Lịch Hẹn - " + lhEdit.getMaLichHen(), true);
            this.pnlParentPanel = pnlKhung;
            this.lhEdit = lhEdit;

            setSize(800, 500);
            setLocationRelativeTo(winParent);
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

            gbc.gridx = 0;
            gbc.gridy = 0;
            pnlForm.add(taoLabel("Ngày đặt: (dd/MM/yyyy)"), gbc);
            gbc.gridx = 1;
            pnlForm.add(taoLabel("Giờ hẹn: (HH:mm)"), gbc);

            JPanel pnlNgay = new JPanel(new BorderLayout());
            pnlNgay.setOpaque(false);
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
                }
                DatePickerDialog winPicker = new DatePickerDialog(
                        (JFrame) SwingUtilities.getWindowAncestor(this), ngayCu, true);
                winPicker.setLocationRelativeTo(btnLichForm);
                winPicker.setVisible(true);
                if (winPicker.getSelectedDate() != null) {
                    txtNgayDat.setText(winPicker.getSelectedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            });
            pnlNgay.add(btnLichForm, BorderLayout.EAST);

            txtGioHen = taoOChonGio(macDinh.format(DateTimeFormatter.ofPattern("HH:mm")));
            gbc.gridy = 1;
            gbc.gridx = 0;
            pnlForm.add(pnlNgay, gbc);
            gbc.gridx = 1;
            pnlForm.add(txtGioHen, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            pnlForm.add(taoTitleLabel("Thông tin Khách hàng & Thú cưng"), gbc);
            gbc.gridwidth = 1;

            gbc.gridx = 0;
            gbc.gridy = 3;
            pnlForm.add(taoLabel("Khách hàng:"), gbc);
            gbc.gridx = 1;
            pnlForm.add(taoLabel("Thú cưng:"), gbc);

            cmbKhachHang = new JComboBox<>();
            cmbThuCung = new JComboBox<>();
            styleCombo(cmbKhachHang);
            styleCombo(cmbThuCung);
            gbc.gridy = 4;
            gbc.gridx = 0;
            pnlForm.add(cmbKhachHang, gbc);
            gbc.gridx = 1;
            pnlForm.add(cmbThuCung, gbc);

            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 2;
            pnlForm.add(taoTitleLabel("Phân công & Trạng thái"), gbc);
            gbc.gridwidth = 1;

            gbc.gridx = 0;
            gbc.gridy = 6;
            pnlForm.add(taoLabel("Nhân viên Spa:"), gbc);
            gbc.gridx = 1;
            pnlForm.add(taoLabel("Trạng thái:"), gbc);

            cmbNhanVienSpa = new JComboBox<>();
            txtFormTrangThai = taoTextField(
                    lhEdit == null ? "Xác nhận" : lhEdit.getTrangThai() == null ? "Xác nhận" : lhEdit.getTrangThai());
            txtFormTrangThai.setEditable(false);
            txtFormTrangThai.setFocusable(false);
            txtFormTrangThai.setBackground(new Color(244, 247, 250));
            txtFormTrangThai.setToolTipText("Tự xác nhận khi tạo mới; nút Hủy trong bảng dùng để hủy lịch");
            styleCombo(cmbNhanVienSpa);
            gbc.gridy = 7;
            gbc.gridx = 0;
            pnlForm.add(cmbNhanVienSpa, gbc);
            gbc.gridx = 1;
            pnlForm.add(txtFormTrangThai, gbc);

            add(pnlForm, BorderLayout.CENTER);

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
            cmbKhachHang.removeAllItems();
            for (String[] kh : control.layDanhSachKhachHang()) {
                cmbKhachHang.addItem(kh[0] + " - " + kh[1]);
            }
            cmbKhachHang.addActionListener(e -> napThuCungTheoKhachHang());
            if (cmbKhachHang.getItemCount() > 0)
                cmbKhachHang.setSelectedIndex(0);
            napThuCungTheoKhachHang();

            cmbNhanVienSpa.removeAllItems();
            for (String[] nv : control.layDanhSachNhanVienSpa()) {
                cmbNhanVienSpa.addItem(nv[0] + " - " + nv[1]);
            }
            if (cmbNhanVienSpa.getItemCount() > 0)
                cmbNhanVienSpa.setSelectedIndex(0);
        }

        private void dienDuLieuSua() {
            if (lhEdit == null)
                return;

            if (lhEdit.getNgayGioHen() != null) {
                txtNgayDat.setText(lhEdit.getNgayGioHen().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                txtGioHen.setText(lhEdit.getNgayGioHen().format(DateTimeFormatter.ofPattern("HH:mm")));
            }

            setComboSelectedById(cmbKhachHang, lhEdit.getMaKhachHang());
            napThuCungTheoKhachHang();
            setComboSelectedById(cmbThuCung, lhEdit.getMaThuCung());
            setComboSelectedById(cmbNhanVienSpa, lhEdit.getMaNhanVien());

            if (lhEdit.getTrangThai() != null) {
                txtFormTrangThai.setText(lhEdit.getTrangThai());
            }
        }

        private void napThuCungTheoKhachHang() {
            if (cmbThuCung == null || cmbKhachHang == null)
                return;
            Object khDaChon = cmbKhachHang.getSelectedItem();
            cmbThuCung.removeAllItems();
            if (khDaChon == null) {
                cmbThuCung.setToolTipText("Chọn khách hàng trước khi chọn thú cưng.");
                return;
            }
            String maKh = control.layMa(khDaChon.toString());
            for (String[] tc : control.layDanhSachThuCung(maKh)) {
                cmbThuCung.addItem(tc[0] + " - " + tc[1]);
            }
            if (cmbThuCung.getItemCount() > 0) {
                cmbThuCung.setSelectedIndex(0);
                cmbThuCung.setToolTipText(null);
            } else {
                cmbThuCung.setToolTipText("Khách hàng chưa có thú cưng. Đăng ký trong Danh mục > Thú cưng.");
            }
        }

        private JTextField taoOChonGio(String gioMacDinh) {
            try {
                MaskFormatter dinhDang = new MaskFormatter("##:##");
                dinhDang.setPlaceholderCharacter('_');
                JFormattedTextField txtTxt = new JFormattedTextField(dinhDang);
                txtTxt.setFocusLostBehavior(JFormattedTextField.PERSIST);
                txtTxt.setValue(gioMacDinh);
                txtTxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                txtTxt.setPreferredSize(new Dimension(200, 35));
                txtTxt.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        new EmptyBorder(5, 10, 5, 10)));
                txtTxt.setToolTipText("Giờ theo HH:mm, chỉ nhận từ 06:00 đến 22:00.");
                return txtTxt;
            } catch (ParseException e) {
                throw new IllegalStateException("Không khởi tạo được định dạng giờ HH:mm", e);
            }
        }

        private void setComboSelectedById(JComboBox<String> cmbCbo, String id) {
            if (id == null)
                return;
            for (int i = 0; i < cmbCbo.getItemCount(); i++) {
                if (cmbCbo.getItemAt(i).startsWith(id + " - ")) {
                    cmbCbo.setSelectedIndex(i);
                    break;
                }
            }
        }

        private void xuLyLuuDuLieu() {
            try {
                String maLichHen = lhEdit == null
                        ? control.taoMaTuDong()
                        : lhEdit.getMaLichHen();
                LichHen lichHen = control.taoLichHen(
                        maLichHen,
                        txtNgayDat.getText(),
                        txtGioHen.getText(),
                        control.layMa((String) cmbKhachHang.getSelectedItem()),
                        control.layMa((String) cmbThuCung.getSelectedItem()),
                        control.layMa((String) cmbNhanVienSpa.getSelectedItem()),
                        txtFormTrangThai.getText());
                boolean thanhCong = lhEdit == null
                        ? control.them(lichHen)
                        : control.sua(lichHen);
                if (!thanhCong) {
                    JOptionPane.showMessageDialog(this,
                            "Không thể lưu lịch hẹn. Kiểm tra thông tin và thử lại.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JOptionPane.showMessageDialog(this,
                        lhEdit == null ? "Đã thêm lịch hẹn mới!" : "Đã cập nhật lịch hẹn!");
                pnlParentPanel.loadData();
                dispose();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Dữ liệu chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
            }
        }

        private JLabel taoLabel(String text) {
            JLabel lblLbl = new JLabel(text);
            lblLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblLbl.setForeground(Color.GRAY);
            return lblLbl;
        }

        private JLabel taoTitleLabel(String text) {
            JLabel lblLbl = new JLabel(text);
            lblLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblLbl.setForeground(Color.BLACK);
            return lblLbl;
        }

        private JTextField taoTextField(String defaultText) {
            JTextField txtTxt = new JTextField(defaultText);
            txtTxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtTxt.setPreferredSize(new Dimension(200, 35));
            txtTxt.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(5, 10, 5, 10)));
            return txtTxt;
        }

        private void styleCombo(JComboBox<String> cmbCbo) {
            cmbCbo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cmbCbo.setPreferredSize(new Dimension(200, 35));
            cmbCbo.setBackground(Color.WHITE);
        }
    }
}
