package UI;

import UI.TrangChu.PhienLamViec;
import UI.TrangChu.UIStyle;
import dao.KiemKeDAO;
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

/** Man hinh "Kiem ke kho" - Kiem ke ton kho (nam trong khung TrangChu). */
public class KiemKePanel extends JPanel {

    private final KiemKeDAO dao = new KiemKeDAO();
    private final Runnable moTraCuu;

    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã SP", "Tên sản phẩm", "Tồn hệ thống",
                    "Tồn thực tế", "Chênh lệch", "Trạng thái" },
            0) {
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
    private final JLabel lbBatDau = new JLabel("Thời điểm kiểm kê: —");
    private LocalDateTime ngayBatDau = LocalDateTime.now();

    // Toan bo san pham cua phieu dang kiem
    private final List<ChiTietPhieuKiemKe> tatCa = new ArrayList<>();
    // Cac dong dang hien tren bang (sau khi tim)
    private final List<ChiTietPhieuKiemKe> dangHien = new ArrayList<>();

    private String maPhieu = "";

    public KiemKePanel() {
        this(() -> { });
    }

    public KiemKePanel(Runnable moTraCuu) {
        this.moTraCuu = moTraCuu;

        setLayout(new BorderLayout(0, 12));
        setBackground(UIStyle.LIGHT_BLUE);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel phanDau = new JPanel();
        phanDau.setOpaque(false);
        phanDau.setLayout(new BoxLayout(phanDau, BoxLayout.Y_AXIS));
        phanDau.add(taoHangTieuDe());
        JPanel mocThoiGian = new JPanel(new GridLayout(1, 2, 18, 0));
        mocThoiGian.setOpaque(false);
        mocThoiGian.setAlignmentX(Component.LEFT_ALIGNMENT);
        mocThoiGian.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        lbBatDau.setFont(UIStyle.font(Font.PLAIN, 13));
        lbBatDau.setBorder(new EmptyBorder(4, 0, 4, 0));
        mocThoiGian.add(lbBatDau);
        phanDau.add(mocThoiGian);
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
        JButton btnLichSu = UIStyle.nutPhu("Phiếu nháp");
        JButton btnLuuNhap = UIStyle.nutPhu("Lưu nháp");
        JButton btnPdf = UIStyle.nutPhu("Xuất phiếu kiểm kê");
        JButton btnTraCuu = UIStyle.nutPhu("Tra cứu kiểm kê");
        btnPdf.addActionListener(e -> UIStyle.xuatPdf(this, "Phiếu kiểm kê kho", bang));
        btnTraCuu.addActionListener(e -> moTraCuu.run());
        btnPhieuMoi.addActionListener(e -> hoiPhieuMoi());
        btnLichSu.addActionListener(e -> xemLichSu());
        btnLuuNhap.addActionListener(e -> luuNhap());

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nut.setOpaque(false);
        nut.add(btnPhieuMoi);
        nut.add(btnLichSu);
        nut.add(btnLuuNhap);
        nut.add(btnPdf);
        nut.add(btnTraCuu);

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
        JButton btnChot = UIStyle.nutXanh("Chốt kiểm kê");
        btnTim.addActionListener(e -> veLaiBang());
        txtTim.addActionListener(e -> veLaiBang());
        btnNhap.addActionListener(e -> nhapSoLieu());
        btnChot.addActionListener(e -> chot());

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
        JPanel hanhDong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        hanhDong.setOpaque(false);
        hanhDong.add(btnChot);
        the.add(hanhDong, BorderLayout.SOUTH);

        return the;
    }

    // =========================================================
    // DU LIEU
    // =========================================================

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
            maPhieu = dao.taoMaMoi();
            lbTieuDe.setText("Kiểm kê kho — " + maPhieu);
            ngayBatDau = LocalDateTime.now();
            lbBatDau.setText("Thời điểm kiểm kê: "
                    + ngayBatDau.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            tatCa.clear();

            // Ton he thong = ton kho dang tinh tu du lieu nhap / ban / kiem ke
            for (SanPham sp : dao.laySanPhamKemTon()) {
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

            model.addRow(new Object[] {
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

        try {
            java.util.Map<String, Integer> tonMoi = new java.util.HashMap<>();
            for (SanPham sp : dao.laySanPhamKemTon())
                tonMoi.put(sp.getMaSanPham(), sp.getTonKho());
            for (ChiTietPhieuKiemKe ct : tatCa) {
                if (ct.daKiem() && tonMoi.containsKey(ct.getMaSanPham()))
                    ct.setSoLuongHeThong(tonMoi.get(ct.getMaSanPham()));
            }
            veLaiBang();
        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
            return;
        }

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

        String lyDo = "";
        if (daKiem.stream().anyMatch(ct -> ct.getChenhLech() != 0)) {
            lyDo = JOptionPane.showInputDialog(this, "Ghi lý do chênh lệch (bắt buộc):", "Lý do chênh lệch",
                    JOptionPane.QUESTION_MESSAGE);
            if (lyDo == null)
                return;
            lyDo = lyDo.trim();
            if (lyDo.isEmpty()) {
                UIStyle.canhBao(this, "Vui lòng ghi lý do chênh lệch.");
                return;
            }
        }
        LocalDateTime gioChot = LocalDateTime.now();
        PhieuKiemKe phieu = new PhieuKiemKe(
                maPhieu,
                gioChot,
                true,
                PhienLamViec.maNV);
        phieu.setNgayBatDau(ngayBatDau);
        phieu.setNgayChot(gioChot);
        phieu.setLyDoChenhLech(lyDo);
        phieu.setMaNhanVienXacNhan(PhienLamViec.maNV);

        try {
            dao.luu(phieu, daKiem);

            UIStyle.thongBao(this, "Đã chốt kiểm kê " + maPhieu + ".");

            phieuMoi();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    private void xemLichSu() {
        try {
            ArrayList<PhieuKiemKe> ds = dao.dsPhieuNhap();
            String[] colsPhieu = { "Mã phiếu", "Trạng thái", "Thời điểm kiểm kê", "Mã NV lập" };
            DefaultTableModel modelPhieu = new DefaultTableModel(colsPhieu, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            for (PhieuKiemKe p : ds)
                modelPhieu.addRow(new Object[] {
                        p.getMaPhieuKiemKe(), p.isTrangThai() ? "Đã chốt" : "Nháp",
                        dinhDangGio(p.getNgayBatDau()), p.getMaNhanVien()
                });
            JTable bangPhieu = new JTable(modelPhieu);
            UIStyle.taoBang(bangPhieu, 0);
            bangPhieu.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            int[] rongCot = { 95, 80, 170, 105 };
            for (int i = 0; i < rongCot.length; i++)
                bangPhieu.getColumnModel().getColumn(i).setPreferredWidth(rongCot[i]);

            DefaultTableModel modelChiTiet = new DefaultTableModel(
                    new String[] { "Mã SP", "Tên sản phẩm", "Tồn hệ thống", "Thực tế", "Chênh lệch", "Kết quả" }, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            JTable bangChiTiet = new JTable(modelChiTiet);
            UIStyle.taoBang(bangChiTiet, 2, 3, 4);
            JLabel lbThongTin = new JLabel("Chọn một phiếu để xem chi tiết.");
            JButton tiepTuc = UIStyle.nutChinh("Tiếp tục phiếu nháp");
            tiepTuc.setEnabled(false);

            JPanel chiTiet = new JPanel(new BorderLayout(0, 8));
            chiTiet.setBorder(new EmptyBorder(8, 8, 8, 8));
            chiTiet.add(lbThongTin, BorderLayout.NORTH);
            chiTiet.add(UIStyle.cuon(bangChiTiet), BorderLayout.CENTER);

            JSplitPane noiDung = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    UIStyle.cuon(bangPhieu), chiTiet);
            noiDung.setResizeWeight(0.57);
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            controls.add(tiepTuc);
            JPanel body = new JPanel(new BorderLayout(0, 8));
            body.setBorder(new EmptyBorder(12, 12, 12, 12));
            body.add(new JLabel("Phiếu nháp kiểm kê — chọn phiếu để xem hoặc tiếp tục kiểm kê."),
                    BorderLayout.NORTH);
            body.add(noiDung, BorderLayout.CENTER);
            body.add(controls, BorderLayout.SOUTH);
            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Phiếu nháp kiểm kê",
                    Dialog.ModalityType.APPLICATION_MODAL);
            dlg.setContentPane(body);
            dlg.setSize(1180, 620);
            dlg.setLocationRelativeTo(this);
            bangPhieu.getSelectionModel().addListSelectionListener(e -> {
                if (e.getValueIsAdjusting())
                    return;
                int row = bangPhieu.getSelectedRow();
                if (row < 0)
                    return;
                PhieuKiemKe p = ds.get(bangPhieu.convertRowIndexToModel(row));
                tiepTuc.setEnabled(!p.isTrangThai());
                try {
                    lbThongTin.setText("Phiếu " + p.getMaPhieuKiemKe() + " | " + (p.isTrangThai() ? "Đã chốt" : "Nháp")
                            + " | Thời điểm kiểm kê: " + dinhDangGio(p.getNgayBatDau())
                            + " | Mã NV lập: " + p.getMaNhanVien());
                    modelChiTiet.setRowCount(0);
                    for (ChiTietPhieuKiemKe ct : dao.layChiTiet(p.getMaPhieuKiemKe())) {
                        Integer tt = ct.getSoLuongThucTe();
                        modelChiTiet
                                .addRow(new Object[] { ct.getMaSanPham(), ct.getTenSanPham(), ct.getSoLuongHeThong(),
                                        tt == null ? "Chưa kiểm" : tt,
                                        tt == null ? "—" : (ct.getChenhLech() > 0 ? "+" : "") + ct.getChenhLech(),
                                        tt == null ? "Chưa kiểm" : ct.getChenhLech() == 0 ? "Khớp" : "Chênh lệch" });
                    }
                } catch (SQLException ex) {
                    UIStyle.loi(this, ex);
                }
            });
            tiepTuc.addActionListener(e -> {
                int row = bangPhieu.getSelectedRow();
                if (row < 0)
                    return;
                PhieuKiemKe p = ds.get(bangPhieu.convertRowIndexToModel(row));
                if (p.isTrangThai())
                    return;
                if (coDongDaKiem() && !UIStyle.xacNhan(this, "Tiếp tục phiếu nháp sẽ thay phiếu đang kiểm. Tiếp tục?"))
                    return;
                try {
                    maPhieu = p.getMaPhieuKiemKe();
                    lbTieuDe.setText("Kiểm kê kho — " + maPhieu);
                    LocalDateTime bd = p.getNgayBatDau() == null ? LocalDateTime.now() : p.getNgayBatDau();
                    ngayBatDau = bd;
                    lbBatDau.setText("Thời điểm kiểm kê: "
                            + bd.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                    tatCa.clear();
                    tatCa.addAll(dao.layChiTiet(maPhieu));
                    veLaiBang();
                    dlg.dispose();
                } catch (SQLException ex) {
                    UIStyle.loi(this, ex);
                }
            });
            dlg.setVisible(true);
        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    private String dinhDangGio(LocalDateTime thoiDiem) {
        return thoiDiem == null ? "—"
                : thoiDiem.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private void luuNhap() {
        try {
            PhieuKiemKe p = new PhieuKiemKe(maPhieu, ngayBatDau, false, PhienLamViec.maNV);
            p.setNgayBatDau(ngayBatDau);
            dao.luuNhap(p, tatCa);
            UIStyle.thongBao(this, "Đã lưu nháp phiếu " + maPhieu + ".");
        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }
}
