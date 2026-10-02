package ui;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import data.DuLieu;
import model.*;

public class KiemKePanel extends JPanel implements Refreshable {
    private PhieuKiemKe phieu;
    private final DefaultTableModel model = Theme.model("Mã SP", "Tên sản phẩm", "Tồn hệ thống", "Tồn thực tế", "Chênh lệch", "Trạng thái", "Ghi chú");
    private final JTable table = new JTable(model);
    private final JTextField txtTim = new JTextField(18);
    private final JLabel lblTitle = new JLabel(), lblDa = new JLabel(), lblChua = new JLabel(), lblLech = new JLabel();

    public KiemKePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        lblTitle.setFont(Theme.H1);
        JButton btnMoi = Theme.btn("Phiếu mới", Color.WHITE, Theme.NAVY), btnChot = Theme.btn("Chốt kiểm kê", Theme.GREEN, Color.WHITE);
        btnMoi.addActionListener(e -> { if (Theme.xacNhan(this, "Bỏ phiếu hiện tại và tạo phiếu mới?")) taoPhieuMoi(); });
        btnChot.addActionListener(e -> chot());
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); btnBox.setOpaque(false);
        btnBox.add(btnMoi); btnBox.add(btnChot);
        JPanel north = new JPanel(new BorderLayout()); north.setOpaque(false);
        north.add(lblTitle, BorderLayout.WEST); north.add(btnBox, BorderLayout.EAST);

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0)); stats.setOpaque(false);
        stats.add(Theme.statCard("Đã kiểm", lblDa)); stats.add(Theme.statCard("Chưa kiểm", lblChua)); stats.add(Theme.statCard("Mặt hàng chênh lệch", lblLech));
        JPanel top = new JPanel(new BorderLayout(0, 10)); top.setOpaque(false);
        top.add(north, BorderLayout.NORTH); top.add(stats, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JButton btnTim = Theme.btn("Tìm", Theme.NAVY, Color.WHITE), btnNhap = Theme.btn("Nhập số liệu kiểm", Theme.NAVY, Color.WHITE);
        btnTim.addActionListener(e -> refresh());
        txtTim.addActionListener(e -> refresh());
        btnNhap.addActionListener(e -> nhapSoLieu());
        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)); tool.setBackground(Color.WHITE);
        tool.add(new JLabel("Mã / tên SP:")); tool.add(txtTim); tool.add(btnTim); tool.add(btnNhap);
        JPanel card = Theme.card(new BorderLayout(0, 8));
        card.add(tool, BorderLayout.NORTH); card.add(Theme.table(table), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) nhapSoLieu(); }
        });
        taoPhieuMoi();
    }

    private void taoPhieuMoi() {
        String ma = "KK-" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy")) + "-" + String.format("%02d", DuLieu.dsKiemKe.size() + 1);
        phieu = new PhieuKiemKe(ma, DuLieu.nguoiDung, LocalDateTime.now(), false);
        refresh();
    }

    @Override public void refresh() {
        // đồng bộ sản phẩm mới / tồn hệ thống mới nhất vào phiếu đang kiểm
        for (SanPham sp : DuLieu.dsSanPham) {
            ChiTietPhieuKiemKe ct = null;
            for (ChiTietPhieuKiemKe c : phieu.getChiTiet()) if (c.getMaSanPham() == sp) ct = c;
            if (ct == null) phieu.getChiTiet().add(new ChiTietPhieuKiemKe(phieu, sp, sp.getSoLuongTon(), -1, ""));
            else if (!ct.daKiem()) ct.setSoLuongHeThong(sp.getSoLuongTon());
        }
        model.setRowCount(0);
        String k = txtTim.getText().trim().toLowerCase();
        int da = 0, lech = 0;
        for (ChiTietPhieuKiemKe c : phieu.getChiTiet()) {
            SanPham s = c.getMaSanPham();
            if (c.daKiem()) { da++; if (c.getChenhLech() != 0) lech++; }
            if (!k.isEmpty() && !s.getMaSanPham().toLowerCase().contains(k) && !s.getTenSanPham().toLowerCase().contains(k)) continue;
            String tt = !c.daKiem() ? "Chưa kiểm" : c.getChenhLech() == 0 ? "Khớp" : "Lệch";
            model.addRow(new Object[]{s.getMaSanPham(), s.getTenSanPham(), c.getSoLuongHeThong(),
                    c.daKiem() ? c.getSoLuongThucTe() : "-", c.daKiem() ? c.getChenhLech() : "-", tt, c.getGhiChu()});
        }
        lblTitle.setText("Kiểm kê kho — " + phieu.getMaPhieuKiemKe());
        lblDa.setText(da + " / " + phieu.getChiTiet().size());
        lblChua.setText(String.valueOf(phieu.getChiTiet().size() - da));
        lblLech.setText(lech + " mặt hàng");
    }

    private void nhapSoLieu() {
        int r = table.getSelectedRow();
        if (r < 0) { Theme.loi(this, "Hãy chọn một sản phẩm trong bảng để nhập số liệu."); return; }
        String ma = (String) model.getValueAt(r, 0);
        for (ChiTietPhieuKiemKe c : phieu.getChiTiet()) {
            if (c.getMaSanPham().getMaSanPham().equals(ma)) {
                KiemKeDialog d = new KiemKeDialog(SwingUtilities.getWindowAncestor(this), c);
                d.setVisible(true);
                if (d.isSaved()) refresh();
                return;
            }
        }
    }

    private void chot() {
        long chua = phieu.getChiTiet().stream().filter(c -> !c.daKiem()).count();
        String msg = chua > 0 ? "Còn " + chua + " sản phẩm chưa kiểm (sẽ giữ nguyên tồn). Vẫn chốt kiểm kê?" : "Chốt kiểm kê và cập nhật tồn kho?";
        if (!Theme.xacNhan(this, msg)) return;
        for (ChiTietPhieuKiemKe c : phieu.getChiTiet())
            if (c.daKiem()) c.getMaSanPham().setSoLuongTon(c.getSoLuongThucTe()); // điều chỉnh tồn theo thực tế
        phieu.setTrangThai(true);
        DuLieu.dsKiemKe.add(phieu);
        JOptionPane.showMessageDialog(this, "Đã chốt phiếu " + phieu.getMaPhieuKiemKe() + ". Tổng chênh lệch: " + phieu.tinhChenhLech());
        taoPhieuMoi();
    }
}
