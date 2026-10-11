package UI;

import dao.PhieuKiemKeDAO;
import dao.SanPhamDAO;
import entity.ChiTietPhieuKiemKe;
import entity.PhieuKiemKe;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Tab "Kiem ke kho" - Kiem ke ton kho. */
public class KiemKePanel extends JPanel implements ManHinhKho {

    private final PhieuKiemKeDAO phieuDAO = new PhieuKiemKeDAO();
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã SP", "Tên sản phẩm", "Tồn hệ thống",
                    "Tồn thực tế", "Chênh lệch", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable bang = new JTable(model);
    private final JTextField txtTim = UIStyle.o(16);

    private final JLabel lbTieuDe = new JLabel("Kiểm kê kho");
    private final JLabel lbDaKiem = new JLabel("0 / 0");
    private final JLabel lbChuaKiem = new JLabel("0");
    private final JLabel lbChenhLech = new JLabel("0 mặt hàng");

    // Toan bo san pham cua phieu dang kiem
    private final List<ChiTietPhieuKiemKe> tatCa = new ArrayList<>();
    // Cac dong dang hien tren bang (sau khi tim)
    private final List<ChiTietPhieuKiemKe> dangHien = new ArrayList<>();

    private String maPhieu = "";

    public KiemKePanel() {

        setLayout(new BorderLayout(0, 12));
        setBackground(UIStyle.LIGHT_BLUE);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel phanDau = new JPanel();
        phanDau.setOpaque(false);
        phanDau.setLayout(new BoxLayout(phanDau, BoxLayout.Y_AXIS));
        phanDau.add(taoHangTieuDe());
        phanDau.add(Box.createVerticalStrut(12));
        phanDau.add(taoCacTheThongKe());

        add(phanDau, BorderLayout.NORTH);
        add(taoPhanBang(), BorderLayout.CENTER);

        phieuMoi();
    }

    // =========================================================
    // GIAO DIEN
    // =========================================================

    private JPanel taoHangTieuDe() {

        lbTieuDe.setFont(UIStyle.font(Font.BOLD, 26));

        JButton btnPhieuMoi = UIStyle.nutPhu("Phiếu mới");
        JButton btnChot = UIStyle.nutXanh("Chốt kiểm kê");
        btnPhieuMoi.addActionListener(e -> hoiPhieuMoi());
        btnChot.addActionListener(e -> chot());

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nut.setOpaque(false);
        nut.add(btnPhieuMoi);
        nut.add(btnChot);

        JPanel hang = new JPanel(new BorderLayout());
        hang.setOpaque(false);
        hang.add(lbTieuDe, BorderLayout.WEST);
        hang.add(nut, BorderLayout.EAST);
        hang.setAlignmentX(Component.LEFT_ALIGNMENT);

        return hang;
    }

    private JPanel taoCacTheThongKe() {

        JPanel hang = new JPanel(new GridLayout(1, 3, 15, 0));
        hang.setOpaque(false);
        hang.setAlignmentX(Component.LEFT_ALIGNMENT);
        hang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        hang.add(UIStyle.theThongKe("Đã kiểm", lbDaKiem));
        hang.add(UIStyle.theThongKe("Chưa kiểm", lbChuaKiem));
        hang.add(UIStyle.theThongKe("Mặt hàng chênh lệch", lbChenhLech));

        return hang;
    }

    private JPanel taoPhanBang() {

        JPanel the = UIStyle.the();
        the.setLayout(new BorderLayout(0, 12));

        JButton btnTim = UIStyle.nutChinh("Tìm");
        JButton btnNhap = UIStyle.nutChinh("Nhập số liệu kiểm");
        btnTim.addActionListener(e -> veLaiBang());
        txtTim.addActionListener(e -> veLaiBang());
        btnNhap.addActionListener(e -> nhapSoLieu());

        JPanel thanhCongCu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        thanhCongCu.setOpaque(false);
        thanhCongCu.add(UIStyle.nhan("Mã / tên SP:"));
        thanhCongCu.add(txtTim);
        thanhCongCu.add(btnTim);
        thanhCongCu.add(btnNhap);

        UIStyle.taoBang(bang, 2, 3, 4);

        // To mau cot trang thai
        bang.getColumnModel().getColumn(5).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object v, boolean sel, boolean foc,
                            int r, int c) {
                        super.getTableCellRendererComponent(
                                t, v, sel, false, r, c);
                        setBorder(new EmptyBorder(0, 10, 0, 10));
                        setHorizontalAlignment(SwingConstants.LEFT);

                        String s = String.valueOf(v);
                        if ("Chênh lệch".equals(s)) {
                            setForeground(UIStyle.RED);
                        } else if ("Khớp".equals(s)) {
                            setForeground(new Color(30, 140, 50));
                        } else {
                            setForeground(Color.DARK_GRAY);
                        }
                        return this;
                    }
                });

        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bang.getSelectedRow() >= 0) {
                    nhapSoLieu();
                }
            }
        });

        the.add(thanhCongCu, BorderLayout.NORTH);
        the.add(UIStyle.cuon(bang), BorderLayout.CENTER);

        return the;
    }

    // =========================================================
    // DU LIEU
    // =========================================================

    @Override
    public void napLai() {
        // Dang kiem do thi giu nguyen, chua kiem gi thi lay ton moi nhat
        if (!coDongDaKiem()) {
            phieuMoi();
        }
    }

    private boolean coDongDaKiem() {
        for (ChiTietPhieuKiemKe ct : tatCa) {
            if (ct.daKiem()) {
                return true;
            }
        }
        return false;
    }

    private void phieuMoi() {

        try {
            maPhieu = phieuDAO.taoMaMoi();
            lbTieuDe.setText("Kiểm kê kho — " + maPhieu);

            tatCa.clear();

            // Ton he thong = ton kho dang tinh trong SanPhamDAO
            for (SanPham sp : sanPhamDAO.getAll()) {
                ChiTietPhieuKiemKe ct = new ChiTietPhieuKiemKe();
                ct.setMaSanPham(sp.getMaSanPham());
                ct.setTenSanPham(sp.getTenSanPham());
                ct.setSoLuongHeThong(sp.getTonKho());
                ct.setSoLuongThucTe(null);
                tatCa.add(ct);
            }

            txtTim.setText("");
            veLaiBang();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    private void hoiPhieuMoi() {

        if (coDongDaKiem() && !UIStyle.xacNhan(this,
                "Phiếu đang kiểm chưa chốt sẽ bị bỏ. Tạo phiếu mới?")) {
            return;
        }

        phieuMoi();
    }

    private void veLaiBang() {

        String kw = txtTim.getText().trim().toLowerCase();

        dangHien.clear();
        model.setRowCount(0);

        for (ChiTietPhieuKiemKe ct : tatCa) {
            if (!kw.isEmpty()
                    && !ct.getMaSanPham().toLowerCase().contains(kw)
                    && !ct.getTenSanPham().toLowerCase().contains(kw)) {
                continue;
            }

            dangHien.add(ct);

            String thucTe = "-";
            String chenh = "-";
            String trangThai = "Chưa kiểm";

            if (ct.daKiem()) {
                int lech = ct.getChenhLech();
                thucTe = String.valueOf(ct.getSoLuongThucTe());
                chenh = lech > 0 ? "+" + lech : String.valueOf(lech);
                trangThai = lech == 0 ? "Khớp" : "Chênh lệch";
            }

            model.addRow(new Object[]{
                    ct.getMaSanPham(),
                    ct.getTenSanPham(),
                    ct.getSoLuongHeThong(),
                    thucTe,
                    chenh,
                    trangThai
            });
        }

        capNhatThongKe();
    }

    private void capNhatThongKe() {

        int daKiem = 0;
        int lech = 0;

        for (ChiTietPhieuKiemKe ct : tatCa) {
            if (ct.daKiem()) {
                daKiem++;
                if (ct.getChenhLech() != 0) {
                    lech++;
                }
            }
        }

        lbDaKiem.setText(daKiem + " / " + tatCa.size());
        lbChuaKiem.setText(String.valueOf(tatCa.size() - daKiem));
        lbChenhLech.setText(lech + " mặt hàng");
    }

    // =========================================================
    // NHAP SO LIEU KIEM
    // =========================================================

    private void nhapSoLieu() {

        int row = bang.getSelectedRow();

        if (row < 0) {
            UIStyle.canhBao(this, "Vui lòng chọn sản phẩm cần nhập số liệu kiểm.");
            return;
        }

        ChiTietPhieuKiemKe ct = dangHien.get(row);

        int giaTriBanDau = ct.daKiem() ? ct.getSoLuongThucTe()
                : ct.getSoLuongHeThong();

        JSpinner spn = new JSpinner(
                new SpinnerNumberModel(Math.max(giaTriBanDau, 0), 0, 1_000_000, 1));
        spn.setFont(UIStyle.font(Font.PLAIN, 14));

        JPanel p = new JPanel(new GridLayout(0, 1, 0, 8));
        p.add(new JLabel(ct.getMaSanPham() + " - " + ct.getTenSanPham()));
        p.add(new JLabel("Tồn hệ thống: " + ct.getSoLuongHeThong()));
        p.add(new JLabel("Số lượng thực tế đếm được:"));
        p.add(spn);

        int kq = JOptionPane.showConfirmDialog(this, p,
                "Nhập số liệu kiểm", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (kq != JOptionPane.OK_OPTION) {
            return;
        }

        ct.setSoLuongThucTe((Integer) spn.getValue());
        veLaiBang();

        // Chuyen sang dong ke tiep de nhap lien tuc
        int tiep = Math.min(row + 1, model.getRowCount() - 1);
        if (tiep >= 0) {
            bang.setRowSelectionInterval(tiep, tiep);
        }
    }

    // =========================================================
    // CHOT KIEM KE
    // =========================================================

    private void chot() {

        List<ChiTietPhieuKiemKe> daKiem = new ArrayList<>();

        for (ChiTietPhieuKiemKe ct : tatCa) {
            if (ct.daKiem()) {
                ct.setTrangThai(ct.getChenhLech() == 0);
                daKiem.add(ct);
            }
        }

        if (daKiem.isEmpty()) {
            UIStyle.canhBao(this, "Chưa có sản phẩm nào được nhập số liệu kiểm.");
            return;
        }

        int chua = tatCa.size() - daKiem.size();

        String hoi = "Chốt phiếu " + maPhieu + " với " + daKiem.size()
                + " mặt hàng đã kiểm?"
                + (chua > 0 ? "\n" + chua
                + " mặt hàng chưa kiểm sẽ không nằm trong phiếu." : "")
                + "\nTồn kho sẽ được điều chỉnh theo số lượng thực tế.";

        if (!UIStyle.xacNhan(this, hoi)) {
            return;
        }

        PhieuKiemKe phieu = new PhieuKiemKe(
                maPhieu,
                LocalDateTime.now(),
                true,
                PhienLamViec.maNV);

        try {
            phieuDAO.luu(phieu, daKiem);

            UIStyle.thongBao(this, "Đã chốt kiểm kê " + maPhieu + ".");

            phieuMoi();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }
}
