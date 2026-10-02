package ui;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import data.DuLieu;
import model.*;

public class PhieuNhapPanel extends JPanel implements Refreshable {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private PhieuNhapKho phieu;
    private final JTextField txtMa = new JTextField(), txtNgay = new JTextField(), txtNv = new JTextField();
    private final JComboBox<NhaCungCap> cboNcc = new JComboBox<>();
    private final DefaultTableModel model = Theme.model("Mã SP", "Tên mặt hàng", "Số lượng", "Đơn giá nhập", "Thành tiền");
    private final JTable table = new JTable(model);
    private final JLabel lblTong = new JLabel();

    public PhieuNhapPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Lập phiếu nhập hàng"); title.setFont(Theme.H1);

        JButton btnNcc = Theme.btn("+", Theme.NAVY, Color.WHITE);
        btnNcc.setToolTipText("Thêm nhà cung cấp mới");
        btnNcc.addActionListener(e -> {
            NhaCungCapDialog d = new NhaCungCapDialog(SwingUtilities.getWindowAncestor(this));
            d.setVisible(true);
            if (d.isSaved()) { napNcc(); cboNcc.setSelectedIndex(cboNcc.getItemCount() - 1); }
        });
        JPanel nccBox = new JPanel(new BorderLayout(4, 0)); nccBox.setOpaque(false);
        nccBox.add(cboNcc, BorderLayout.CENTER); nccBox.add(btnNcc, BorderLayout.EAST);
        txtMa.setEditable(false); txtNgay.setEditable(false); txtNv.setEditable(false);
        JPanel info = Theme.card(new GridLayout(2, 4, 12, 8));
        info.add(new JLabel("Mã phiếu")); info.add(txtMa); info.add(new JLabel("Ngày nhập")); info.add(txtNgay);
        info.add(new JLabel("Nhà cung cấp")); info.add(nccBox); info.add(new JLabel("Nhân viên lập")); info.add(txtNv);

        JPanel north = new JPanel(new BorderLayout(0, 10)); north.setOpaque(false);
        north.add(title, BorderLayout.NORTH); north.add(info, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        JButton btnThem = Theme.btn("+ Thêm mặt hàng", Theme.NAVY, Color.WHITE), btnXoa = Theme.btn("Xóa dòng", Color.WHITE, Theme.RED);
        btnThem.addActionListener(e -> themDong());
        btnXoa.addActionListener(e -> xoaDong());
        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)); tool.setBackground(Color.WHITE);
        tool.add(btnThem); tool.add(btnXoa);
        JPanel card = Theme.card(new BorderLayout(0, 8));
        card.add(tool, BorderLayout.NORTH); card.add(Theme.table(table), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        lblTong.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JButton btnNhap = Theme.btn("Lưu nháp", Color.WHITE, Theme.NAVY), btnXong = Theme.btn("Hoàn tất nhập kho", Theme.GREEN, Color.WHITE);
        btnNhap.addActionListener(e -> luu(false));
        btnXong.addActionListener(e -> luu(true));
        JPanel south = new JPanel(new BorderLayout()); south.setOpaque(false);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); btns.setOpaque(false);
        btns.add(btnNhap); btns.add(btnXong);
        south.add(lblTong, BorderLayout.WEST); south.add(btns, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        napNcc();
        phieuMoi();
    }

    private void napNcc() {
        NhaCungCap dangChon = (NhaCungCap) cboNcc.getSelectedItem();
        cboNcc.removeAllItems();
        for (NhaCungCap n : DuLieu.dsNhaCungCap) cboNcc.addItem(n);
        if (dangChon != null) cboNcc.setSelectedItem(dangChon);
    }

    private void phieuMoi() {
        phieu = new PhieuNhapKho(String.format("PN-%03d", DuLieu.dsPhieuNhap.size() + 1), LocalDateTime.now(),
                null, DuLieu.nguoiDung, false);
        txtMa.setText(phieu.getMaPhieuNhap());
        txtNgay.setText(phieu.getNgayNhap().format(FMT));
        txtNv.setText(DuLieu.nguoiDung.getHoTen());
        capNhatBang();
    }

    @Override public void refresh() { napNcc(); }

    private void capNhatBang() {
        model.setRowCount(0);
        for (ChiTietPhieuNhap c : phieu.getChiTiet())
            model.addRow(new Object[]{c.getMaSanPham().getMaSanPham(), c.getMaSanPham().getTenSanPham(), c.getSoLuongNhap(),
                    Theme.tien(c.getDonGiaNhap()), Theme.tien(c.thanhTien())});
        lblTong.setText("Tổng thanh toán: " + Theme.tien(phieu.tinhTongTienNhap()));
    }

    private void themDong() {
        ChiTietNhapDialog d = new ChiTietNhapDialog(SwingUtilities.getWindowAncestor(this), phieu);
        d.setVisible(true);
        if (d.getKetQua() != null) { phieu.getChiTiet().add(d.getKetQua()); capNhatBang(); }
    }

    private void xoaDong() {
        int r = table.getSelectedRow();
        if (r < 0) { Theme.loi(this, "Hãy chọn một dòng để xóa."); return; }
        phieu.getChiTiet().remove(r); capNhatBang();
    }

    private void luu(boolean hoanTat) {
        if (cboNcc.getSelectedItem() == null) { Theme.loi(this, "Vui lòng chọn nhà cung cấp (bấm + để thêm mới)."); return; }
        if (phieu.getChiTiet().isEmpty()) { Theme.loi(this, "Phiếu chưa có mặt hàng nào."); return; }
        phieu.setMaNhaCungCap((NhaCungCap) cboNcc.getSelectedItem());
        phieu.setTrangThai(hoanTat);
        if (hoanTat) {
            for (ChiTietPhieuNhap c : phieu.getChiTiet()) {
                c.setTrangThai(true);
                SanPham s = c.getMaSanPham();
                s.setSoLuongTon(s.getSoLuongTon() + c.getSoLuongNhap()); 
            }
        }
        DuLieu.dsPhieuNhap.add(phieu);
        JOptionPane.showMessageDialog(this, hoanTat ? "Đã nhập kho phiếu " + phieu.getMaPhieuNhap() : "Đã lưu nháp phiếu " + phieu.getMaPhieuNhap());
        phieuMoi();
    }
}
