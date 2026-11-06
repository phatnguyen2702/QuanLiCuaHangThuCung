package UI;
import control.NhapKhoControl;
import control.TraCuuKhoControl;
import entity.PhieuNhapKho;
import util.PhieuNhapPdfExporter;
import entity.ChiTietPhieuNhap;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
public class TraCuuKhoUI extends JFrame {
private static final Color NAVY = new Color(18, 48, 82);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private final boolean kiemKe;
    private final JTextField txtTuKhoa = new JTextField(20);
    private final JTextField txtTuNgay = new JTextField(10);
    private final JTextField txtDenNgay = new JTextField(10);
    private final JComboBox<String> cmbTrangThai2 = new JComboBox<>(
            new String[] { "Tất cả trạng thái", "Đã hoàn tất", "Phiếu nháp" });
    private final JComboBox<String> cmbNhaCungCap2 = new JComboBox<>(new String[] { "Tất cả nhà cung cấp" });
    private final JComboBox<String> cmbKetQuaKiemKe = new JComboBox<>(
            new String[] { "Tất cả kết quả", "Có chênh lệch", "Không chênh lệch", "Chưa kiểm đủ" });
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblDuLieu = new JTable(model);
    private List<String[]> ketQua = new ArrayList<>();
    public TraCuuKhoUI(boolean kiemKe, Runnable quayLai) {
        this.kiemKe = kiemKe;
        setLayout(new BorderLayout(0, 12));
        getContentPane().setBackground(new Color(229, 241, 252));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(20, 24, 20, 24));
        JLabel lblTitle2 = new JLabel(kiemKe ? "Tra cứu kiểm kê kho" : "Tra cứu nhập kho");
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblTitle2.setForeground(NAVY);
        add(lblTitle2, BorderLayout.NORTH);
        JPanel pnlContent = new JPanel(new BorderLayout(0, 10));
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new EmptyBorder(14, 14, 14, 14));
        pnlContent.add(taoBoLoc(), BorderLayout.NORTH);
        tblDuLieu.setRowHeight(29);
        tblDuLieu.setFillsViewportHeight(true);
        tblDuLieu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDuLieu.getTableHeader().setBackground(NAVY);
        tblDuLieu.getTableHeader().setForeground(Color.WHITE);
        tblDuLieu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDuLieu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2)
                    xemChiTietPhieuKho();
            }
        });
        pnlContent.add(new JScrollPane(tblDuLieu), BorderLayout.CENTER);
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlActions.setOpaque(false);
        JButton btnDetail = new JButton("Xem chi tiết");
        btnDetail.addActionListener(e -> xemChiTietPhieuKho());
        JButton btnBack = new JButton("Quay lại");
        btnBack.addActionListener(e -> quayLai.run());
        pnlActions.add(btnDetail);
        pnlActions.add(btnBack);
        pnlContent.add(pnlActions, BorderLayout.SOUTH);
        add(pnlContent, BorderLayout.CENTER);
        if (kiemKe) {
            cmbTrangThai2.setVisible(false);
            cmbNhaCungCap2.setVisible(false);
        } else {
            cmbNhaCungCap2.setPreferredSize(new Dimension(180, 26));
        }
        model.setColumnIdentifiers(kiemKe
                ? new String[] { "Mã phiếu", "Ngày chốt", "Mã NV lập", "Trạng thái", "Số mặt hàng", "Kết quả" }
                : new String[] { "Mã phiếu", "Ngày nhập", "Mã NV lập", "Mã NCC", "Nhà cung cấp", "Số mặt hàng",
                        "Tổng tiền", "Trạng thái" });
        txtTuKhoa.addActionListener(e -> timKiemPhieuKho());
        napDanhSachNhaCungCapTraCuu();
        timKiemPhieuKho();
    }
    private JPanel taoBoLoc() {
        JPanel pnlKhung = new JPanel(new GridBagLayout());
        pnlKhung.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 5, 4, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        them(pnlKhung, c, 0, 0, new JLabel("Từ khóa (mã phiếu, nhân viên, sản phẩm)"));
        them(pnlKhung, c, 1, 0, new JLabel("Từ ngày (dd/MM/yyyy)"));
        them(pnlKhung, c, 2, 0, new JLabel("Đến ngày (dd/MM/yyyy)"));
        them(pnlKhung, c, 3, 0, new JLabel(kiemKe ? "Kết quả kiểm kê" : "Trạng thái"));
        them(pnlKhung, c, 0, 1, txtTuKhoa);
        them(pnlKhung, c, 1, 1, taoOChonNgay(txtTuNgay));
        them(pnlKhung, c, 2, 1, taoOChonNgay(txtDenNgay));
        them(pnlKhung, c, 3, 1, kiemKe ? cmbKetQuaKiemKe : cmbTrangThai2);
        if (!kiemKe) {
            them(pnlKhung, c, 4, 0, new JLabel("Nhà cung cấp"));
            them(pnlKhung, c, 4, 1, cmbNhaCungCap2);
        }
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.addActionListener(e -> timKiemPhieuKho());
        JButton btnReset = new JButton("Làm mới");
        btnReset.addActionListener(e -> {
            txtTuKhoa.setText("");
            txtTuNgay.setText("");
            txtDenNgay.setText("");
            cmbTrangThai2.setSelectedIndex(0);
            cmbKetQuaKiemKe.setSelectedIndex(0);
            cmbNhaCungCap2.setSelectedIndex(0);
            timKiemPhieuKho();
        });
        them(pnlKhung, c, 5, 1, btnSearch);
        them(pnlKhung, c, 6, 1, btnReset);
        return pnlKhung;
    }
    private void them(JPanel pnlKhung, GridBagConstraints c, int x, int y, Component component) {
        c.gridx = x;
        c.gridy = y;
        c.weightx = component instanceof JTextField || component instanceof JPanel ? 1 : 0;
        pnlKhung.add(component, c);
    }
    private JPanel taoOChonNgay(JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.setOpaque(false);
        JButton button = new JButton("Chọn ngày");
        button.setToolTipText("Mở lịch để chọn ngày");
        button.addActionListener(e -> hienLich(field, button));
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }
    private void hienLich(JTextField field, Component anchor) {
        LocalDate selected = null;
        try {
            if (!field.getText().trim().isEmpty())
                selected = parseDate(field.getText());
        } catch (IllegalArgumentException ignored) {
        }
        LocalDate date = LichChonNgay.chonNgayLichHen(anchor, selected, false);
        if (date != null) {
            field.setText(date.format(DateTimeFormatter.ofPattern("dd/MM/uuuu")));
        }
    }
    private void napDanhSachNhaCungCapTraCuu() {
        if (kiemKe)
            return;
        try {
            for (dao.TraCuuKhoDAO.NhaCungCapTraCuu nhaCungCap : TraCuuKhoControl.layDanhSachNhaCungCap())
                cmbNhaCungCap2.addItem(nhaCungCap.toString());
        } catch (Exception e) {
            baoLoi(e);
        }
    }
    private void timKiemPhieuKho() {
        try {
            LocalDate from = parseDate(txtTuNgay.getText());
            LocalDate to = parseDate(txtDenNgay.getText());
            if (from != null && to != null && from.isAfter(to)) {
                JOptionPane.showMessageDialog(this, "Từ ngày phải trước hoặc bằng đến ngày.", "Bộ lọc ngày",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String keyword = txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);
            List<Object[]> phieuTraCuu = kiemKe
                    ? TraCuuKhoControl.timKiemPhieuKiemKe(keyword)
                    : TraCuuKhoControl.timKiemPhieuNhap(keyword);
            List<String[]> result = new ArrayList<>();
            for (Object[] row : phieuTraCuu) {
                        String id = String.valueOf(row[0]);
                        String staff = (String) row[2];
                        Timestamp timestamp = (Timestamp) row[1];
                        LocalDate date = timestamp == null ? null : timestamp.toLocalDateTime().toLocalDate();
                        if (from != null && (date == null || date.isBefore(from))
                                || to != null && (date == null || date.isAfter(to)))
                            continue;
                        Object trangThaiPhieu = row[kiemKe ? 3 : 7];
                        String status = Boolean.TRUE.equals(trangThaiPhieu)
                                ? (kiemKe ? "Đã chốt" : "Đã hoàn tất") : "Phiếu nháp";
                        int count = ((Number) row[kiemKe ? 4 : 5]).intValue();
                        String dateText = timestamp == null ? "" : DATE_TIME.format(timestamp.toLocalDateTime());
                        if (kiemKe) {
                            String delta = ((Number) row[5]).intValue() > 0 ? "Có chênh lệch"
                                    : ((Number) row[6]).intValue() > 0 ? "Chưa kiểm đủ" : "Không chênh lệch";
                            int filter = cmbKetQuaKiemKe.getSelectedIndex();
                            if (filter == 1 && !delta.equals("Có chênh lệch")
                                    || filter == 2 && !delta.equals("Không chênh lệch")
                                    || filter == 3 && !delta.equals("Chưa kiểm đủ"))
                                continue;
                            result.add(new String[] { id, dateText, staff, status, String.valueOf(count), delta });
                            continue;
                        }
                        int statusFilter = cmbTrangThai2.getSelectedIndex();
                        if (statusFilter == 1 && !status.equals("Đã hoàn tất")
                                || statusFilter == 2 && !status.equals("Phiếu nháp"))
                            continue;
                        String maNhaCungCap = (String) row[3];
                        if (cmbNhaCungCap2.getSelectedIndex() > 0) {
                            String selectedSupplier = cmbNhaCungCap2.getSelectedItem().toString().split(" - ", 2)[0];
                            if (!Objects.equals(selectedSupplier, maNhaCungCap))
                                continue;
                        }
                        result.add(new String[] { id, dateText, staff, maNhaCungCap, (String) row[4],
                                String.valueOf(count), String.format(Locale.forLanguageTag("vi-VN"), "%,.0f ₫", ((Number) row[6]).doubleValue()), status });
            }
            ketQua = result;
            model.setRowCount(0);
            for (String[] row : ketQua)
                model.addRow(row);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ngày không hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            baoLoi(ex);
        }
    }
    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty())
            return null;
        try {
            return LocalDate.parse(value.trim(), DATE_ONLY);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày phải đúng định dạng dd/MM/yyyy.");
        }
    }
    private void xemChiTietPhieuKho() {
        int row = tblDuLieu.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một phiếu để xem chi tiết.");
            return;
        }
        String id = String.valueOf(model.getValueAt(tblDuLieu.convertRowIndexToModel(row), 0));
        if (!kiemKe) {
            xemChiTietPhieuNhap(id);
            return;
        }
        DefaultTableModel detail = new DefaultTableModel(kiemKe
                ? new String[] { "Mã SP", "Tên sản phẩm", "Đợt nhập", "Ngày nhập", "Tồn hệ thống", "Tồn thực tế", "Chênh lệch", "Kết quả" }
                : new String[] { "Mã SP", "Tên sản phẩm", "Số lượng nhập", "Đơn giá", "Thành tiền", "Hạn sử dụng" },
                0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        try {
            for (Object[] rowData : TraCuuKhoControl.layChiTietPhieuKiemKe(id)) {
                int system = ((Number) rowData[4]).intValue();
                Integer actual = rowData[5] == null ? null : ((Number) rowData[5]).intValue();
                Timestamp lotDate = (Timestamp) rowData[3];
                detail.addRow(new Object[] { rowData[0], rowData[1],
                        rowData[2] == null ? "Lịch sử cũ" : rowData[2],
                        lotDate == null ? "—" : lotDate.toLocalDateTime().toLocalDate(), system,
                        actual == null ? "Chưa kiểm" : actual,
                        actual == null ? "—" : actual - system,
                        actual == null ? "Chưa kiểm" : actual == system ? "Khớp" : "Chênh lệch" });
            }
            JTable tblDetailTable = new JTable(detail);
            tblDetailTable.setRowHeight(28);
            tblDetailTable.setFillsViewportHeight(true);
            JScrollPane scrCuon2 = new JScrollPane(tblDetailTable);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            scrCuon2.setPreferredSize(new Dimension(
                    Math.min(950, (int) (screen.width * 0.85)),
                    Math.min(520, (int) (screen.height * 0.72))));
            JOptionPane.showMessageDialog(this, scrCuon2, "Chi tiết phiếu " + id, JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            baoLoi(e);
        }
    }
    private void xemChiTietPhieuNhap(String maPhieu) {
        try {
            dao.NhapKhoDAO.BanInPhieuNhap banIn = NhapKhoControl.layThongTinInPhieuNhap(maPhieu);
            PhieuNhapKho phieu = banIn.getPhieu();
            String trangThaiPhieu = phieu.isTrangThai() ? "ĐÃ HOÀN TẤT" : "PHIẾU NHÁP – CHƯA HOÀN TẤT";
            Window winWin = SwingUtilities.getWindowAncestor(this);
            JDialog winDialog = new JDialog(winWin, "Chi tiết phiếu nhập " + maPhieu,
                    Dialog.ModalityType.APPLICATION_MODAL);
            winDialog.setSize(1040, 650);
            winDialog.setMinimumSize(new Dimension(850, 480));
            winDialog.setLocationRelativeTo(this);
            winDialog.setLayout(new BorderLayout(0, 12));
            JPanel pnlHeader = new JPanel(new GridLayout(0, 2, 18, 8));
            pnlHeader.setBackground(Color.WHITE);
            pnlHeader.setBorder(new EmptyBorder(16, 20, 8, 20));
            pnlHeader.add(new JLabel("Mã phiếu nhập: " + phieu.getMaPhieuNhap()));
            pnlHeader.add(new JLabel("Trạng thái: " + trangThaiPhieu));
            pnlHeader.add(new JLabel("Ngày nhập: " +
                    (phieu.getNgayNhap() == null ? "Chưa có" : DATE_TIME.format(phieu.getNgayNhap()))));
            pnlHeader.add(new JLabel("Nhân viên lập: " + hienThi(banIn.getTenNhanVien())
                    + " (" + hienThi(phieu.getMaNhanVien()) + ")"));
            pnlHeader.add(new JLabel("Nhà cung cấp: " + hienThi(phieu.getTenNhaCungCap())
                    + " (" + hienThi(phieu.getMaNhaCungCap()) + ")"));
            pnlHeader.add(new JLabel("Số điện thoại NCC: " + hienThi(banIn.getSoDienThoaiNhaCungCap())));
            pnlHeader.add(new JLabel("Địa chỉ NCC: " + hienThi(banIn.getDiaChiNhaCungCap())));
            winDialog.add(pnlHeader, BorderLayout.NORTH);
            DefaultTableModel mdl = new DefaultTableModel(new String[] {
                    "STT", "Mã SP", "Tên sản phẩm", "Số lượng", "Đơn giá",
                    "Thành tiền", "Hạn sử dụng"
            }, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            int stt = 0;
            for (ChiTietPhieuNhap ct : banIn.getChiTiet()) {
                mdl.addRow(new Object[] {
                        ++stt, ct.getMaSanPham(), ct.getTenSanPham(), ct.getSoLuongNhap(),
                        tien(ct.getDonGiaNhap()), tien(ct.getThanhTien()),
                        ct.getHanSuDung() == null ? "—"
                                : ct.getHanSuDung().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                });
            }
            JTable tblChiTiet2 = new JTable(mdl);
            tblChiTiet2.setRowHeight(30);
            tblChiTiet2.setFillsViewportHeight(true);
            tblChiTiet2.setAutoCreateRowSorter(true);
            winDialog.add(new JScrollPane(tblChiTiet2), BorderLayout.CENTER);
            JPanel pnlFooter = new JPanel(new BorderLayout());
            pnlFooter.setBorder(new EmptyBorder(5, 20, 15, 20));
            pnlFooter.setBackground(Color.WHITE);
            JLabel lblTongTien = new JLabel("Tổng số lượng: " + banIn.getTongSoLuong()
                    + "     |     Tổng tiền: " + tien(banIn.getTongTien()));
            lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTongTien.setForeground(NAVY);
            pnlFooter.add(lblTongTien, BorderLayout.WEST);
            JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnlBtns.setOpaque(false);
            JButton btnPdf = new JButton("Xuất phiếu nhập");
            btnPdf.setBackground(NAVY);
            btnPdf.setForeground(Color.WHITE);
            btnPdf.setFocusPainted(false);
            btnPdf.addActionListener(e -> {
                try {
                    PhieuNhapPdfExporter.xuat(winDialog, NhapKhoControl.layThongTinInPhieuNhap(maPhieu));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(winDialog, ex.getMessage(),
                            "Không thể xuất phiếu nhập", JOptionPane.ERROR_MESSAGE);
                }
            });
            JButton btnDong = new JButton("Đóng");
            btnDong.addActionListener(e -> winDialog.dispose());
            pnlBtns.add(btnPdf);
            pnlBtns.add(btnDong);
            pnlFooter.add(pnlBtns, BorderLayout.EAST);
            winDialog.add(pnlFooter, BorderLayout.SOUTH);
            winDialog.setVisible(true);
        } catch (Exception e) {
            baoLoi(e);
        }
    }
    private String hienThi(String value) {
        return value == null || value.isBlank() ? "Chưa có dữ liệu" : value;
    }
    private String tien(double value) {
        return String.format(Locale.forLanguageTag("vi-VN"), "%,.0f ₫", value);
    }
    private void baoLoi(Exception e) {
        JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu kho.\n" + e.getMessage(), "Lỗi tra cứu",
                JOptionPane.ERROR_MESSAGE);
    }
    public void lamMoi() {
        timKiemPhieuKho();
    }
}
