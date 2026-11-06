package UI;
import control.PhienLamViecControl;
import UI.TrangChuUI.UIStyle;
import control.KiemKeControl;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
public class KiemKeUI extends JFrame {
private final Runnable moTraCuu;
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã SP", "Tên sản phẩm", "Đợt nhập", "Tồn hệ thống",
                    "Tồn thực tế", "Chênh lệch", "Trạng thái" },
            0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable tblDuLieu2 = new JTable(model);
    private final JTextField txtTim = UIStyle.o(16);
    private final JLabel lblTieuDe3 = new JLabel("Kiểm kê kho");
    private final JLabel lblDaKiem = new JLabel("0 / 0");
    private final JLabel lblChuaKiem = new JLabel("0");
    private final JLabel lblChenhLech = new JLabel("0 mặt hàng");
    private final JLabel lblBatDau = new JLabel("Bắt đầu: — | Đã trôi qua: 00:00:00");
    private LocalDateTime ngayBatDau = LocalDateTime.now();
    private final javax.swing.Timer tmrDemThoiGian;
    private final List<ChiTietPhieuKiemKe> tatCa = new ArrayList<>();
    private final List<ChiTietPhieuKiemKe> dangHien = new ArrayList<>();
    private String maPhieu = "";
    public KiemKeUI() {
        this(() -> {
        });
    }
    public KiemKeUI(Runnable moTraCuu) {
        this.moTraCuu = moTraCuu;
        setLayout(new BorderLayout(0, 12));
        getContentPane().setBackground(UIStyle.LIGHT_BLUE);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(20, 25, 20, 25));
        JPanel pnlPhanDau = new JPanel();
        pnlPhanDau.setOpaque(false);
        pnlPhanDau.setLayout(new BoxLayout(pnlPhanDau, BoxLayout.Y_AXIS));
        pnlPhanDau.add(taoHangTieuDe());
        JPanel pnlMocThoiGian = new JPanel(new GridLayout(1, 2, 18, 0));
        pnlMocThoiGian.setOpaque(false);
        pnlMocThoiGian.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlMocThoiGian.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        lblBatDau.setFont(UIStyle.font(Font.PLAIN, 13));
        lblBatDau.setBorder(new EmptyBorder(4, 0, 4, 0));
        lblBatDau.setToolTipText("Bắt đầu là lúc tạo phiếu; thời điểm chốt được lưu khi bấm Chốt kiểm kê.");
        pnlMocThoiGian.add(lblBatDau);
        pnlPhanDau.add(pnlMocThoiGian);
        pnlPhanDau.add(Box.createVerticalStrut(12));
        pnlPhanDau.add(taoCacTheThongKe());
        add(pnlPhanDau, BorderLayout.NORTH);
        add(taoPhanBang(), BorderLayout.CENTER);
        tmrDemThoiGian = new javax.swing.Timer(1000, e -> capNhatThoiGianKiemKe());
        taoPhieuKiemKeMoi();
        tmrDemThoiGian.start();
    }
    @Override
    public void dispose() {
        tmrDemThoiGian.stop();
        super.dispose();
    }
    private JPanel taoHangTieuDe() {
        lblTieuDe3.setFont(UIStyle.font(Font.BOLD, 26));
        JButton btnPhieuMoi = UIStyle.nutPhu("Phiếu mới");
        JButton btnLichSu = UIStyle.nutPhu("Phiếu nháp");
        JButton btnLuuNhap = UIStyle.nutPhu("Lưu nháp");
        JButton btnPdf = UIStyle.nutPhu("Xuất phiếu kiểm kê");
        JButton btnTraCuu = UIStyle.nutPhu("Tra cứu kiểm kê");
        btnPdf.addActionListener(e -> UIStyle.xuatPdf(this, "Phiếu kiểm kê kho", tblDuLieu2));
        btnTraCuu.addActionListener(e -> moTraCuu.run());
        btnPhieuMoi.addActionListener(e -> xacNhanTaoPhieuKiemKeMoi());
        btnLichSu.addActionListener(e -> xemLichSuKiemKe());
        btnLuuNhap.addActionListener(e -> luuSoLieuKiemKe());
        JPanel pnlNut2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlNut2.setOpaque(false);
        pnlNut2.add(btnPhieuMoi);
        pnlNut2.add(btnLichSu);
        pnlNut2.add(btnLuuNhap);
        pnlNut2.add(btnPdf);
        pnlNut2.add(btnTraCuu);
        JPanel pnlHang = new JPanel(new BorderLayout());
        pnlHang.setOpaque(false);
        pnlHang.add(lblTieuDe3, BorderLayout.WEST);
        pnlHang.add(pnlNut2, BorderLayout.EAST);
        pnlHang.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pnlHang;
    }
    private JPanel taoCacTheThongKe() {
        JPanel pnlHang = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlHang.setOpaque(false);
        pnlHang.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlHang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        pnlHang.add(UIStyle.theThongKe("Đã kiểm", lblDaKiem));
        pnlHang.add(UIStyle.theThongKe("Chưa kiểm", lblChuaKiem));
        pnlHang.add(UIStyle.theThongKe("Mặt hàng chênh lệch", lblChenhLech));
        return pnlHang;
    }
    private JPanel taoPhanBang() {
        JPanel pnlThe = UIStyle.the();
        pnlThe.setLayout(new BorderLayout(0, 12));
        JButton btnTim = UIStyle.nutChinh("Tìm");
        JButton btnNhap = UIStyle.nutChinh("Nhập số liệu kiểm");
        JButton btnChot = UIStyle.nutXanh("Chốt kiểm kê");
        btnTim.addActionListener(e -> capNhatBangKiemKe());
        txtTim.addActionListener(e -> capNhatBangKiemKe());
        btnNhap.addActionListener(e -> nhapSoLuongKiemKe());
        btnChot.addActionListener(e -> chotPhieuKiemKe());
        JPanel pnlThanhCongCu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlThanhCongCu.setOpaque(false);
        pnlThanhCongCu.add(UIStyle.nhan("Mã / tên SP:"));
        pnlThanhCongCu.add(txtTim);
        pnlThanhCongCu.add(btnTim);
        pnlThanhCongCu.add(btnNhap);
        UIStyle.taoBang(tblDuLieu2, 2, 3, 4);
        tblDuLieu2.getColumnModel().getColumn(5).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable tblDuLieu3, Object v, boolean sel, boolean foc,
                            int r, int c) {
                        super.getTableCellRendererComponent(
                                tblDuLieu3, v, sel, false, r, c);
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
        tblDuLieu2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblDuLieu2.getSelectedRow() >= 0) {
                    nhapSoLuongKiemKe();
                }
            }
        });
        pnlThe.add(pnlThanhCongCu, BorderLayout.NORTH);
        pnlThe.add(UIStyle.cuon(tblDuLieu2), BorderLayout.CENTER);
        JPanel pnlHanhDong2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        pnlHanhDong2.setOpaque(false);
        pnlHanhDong2.add(btnChot);
        pnlThe.add(pnlHanhDong2, BorderLayout.SOUTH);
        return pnlThe;
    }
    public void napLai() {
        if (!coSanPhamDaKiem()) {
            taoPhieuKiemKeMoi();
        }
    }
    private boolean coSanPhamDaKiem() {
        for (ChiTietPhieuKiemKe ct : tatCa) {
            if (ct.daKiem()) {
                return true;
            }
        }
        return false;
    }
    private void taoPhieuKiemKeMoi() {
        try {
            maPhieu = KiemKeControl.taoMaPhieuKiemKeMoi();
            lblTieuDe3.setText("Kiểm kê kho — " + maPhieu);
            ngayBatDau = LocalDateTime.now();
            capNhatThoiGianKiemKe();
            tatCa.clear();
            for (SanPham sp : KiemKeControl.layDanhSachSanPhamKemTonKho()) {
                ChiTietPhieuKiemKe ct = new ChiTietPhieuKiemKe();
                ct.setMaSanPham(sp.getMaSanPham());
                ct.setTenSanPham(sp.getTenSanPham());
                ct.setMaPhieuNhap(sp.getMaPhieuNhap());
                ct.setNgayNhap(sp.getNgayNhap());
                ct.setSoLuongHeThong(sp.getTonKho());
                ct.setSoLuongThucTe(null);
                tatCa.add(ct);
            }
            txtTim.setText("");
            capNhatBangKiemKe();
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private void capNhatThoiGianKiemKe() {
        long elapsed = Math.max(0, Duration.between(ngayBatDau, LocalDateTime.now()).getSeconds());
        long hours = elapsed / 3600;
        long minutes = (elapsed % 3600) / 60;
        long seconds = elapsed % 60;
        lblBatDau.setText("Bắt đầu: "
                + ngayBatDau.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                + "  |  Đã trôi qua: " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }
    private void xacNhanTaoPhieuKiemKeMoi() {
        if (coSanPhamDaKiem() && !UIStyle.xacNhan(this,
                "Phiếu đang kiểm chưa chốt sẽ bị bỏ. Tạo phiếu mới?")) {
            return;
        }
        taoPhieuKiemKeMoi();
    }
    private void capNhatBangKiemKe() {
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
                    ct.getMaPhieuNhap() + (ct.getNgayNhap() == null ? "" : " · "
                            + ct.getNgayNhap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                    ct.getSoLuongHeThong(),
                    thucTe,
                    chenh,
                    trangThai
            });
        }
        capNhatThongKeKiemKe();
    }
    private void capNhatThongKeKiemKe() {
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
        lblDaKiem.setText(daKiem + " / " + tatCa.size());
        lblChuaKiem.setText(String.valueOf(tatCa.size() - daKiem));
        lblChenhLech.setText(lech + " mặt hàng");
    }
    private void nhapSoLuongKiemKe() {
        int row = tblDuLieu2.getSelectedRow();
        if (row < 0) {
            UIStyle.canhBao(this, "Vui lòng chọn sản phẩm cần nhập số liệu kiểm.");
            return;
        }
        ChiTietPhieuKiemKe ct = dangHien.get(row);
        int giaTriBanDau = ct.daKiem() ? ct.getSoLuongThucTe()
                : ct.getSoLuongHeThong();
        JSpinner spnSpn = new JSpinner(
                new SpinnerNumberModel(Math.max(giaTriBanDau, 0), 0, 1_000_000, 1));
        spnSpn.setFont(UIStyle.font(Font.PLAIN, 14));
        JPanel pnlKhung2 = new JPanel(new GridLayout(0, 1, 0, 8));
        pnlKhung2.add(new JLabel(ct.getMaSanPham() + " - " + ct.getTenSanPham()));
        pnlKhung2.add(new JLabel("Tồn hệ thống: " + ct.getSoLuongHeThong()));
        pnlKhung2.add(new JLabel("Số lượng thực tế đếm được:"));
        pnlKhung2.add(spnSpn);
        int kq = JOptionPane.showConfirmDialog(this, pnlKhung2,
                "Nhập số liệu kiểm", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (kq != JOptionPane.OK_OPTION) {
            return;
        }
        ct.setSoLuongThucTe((Integer) spnSpn.getValue());
        capNhatBangKiemKe();
        int tiep = Math.min(row + 1, model.getRowCount() - 1);
        if (tiep >= 0) {
            tblDuLieu2.setRowSelectionInterval(tiep, tiep);
        }
    }
    private void chotPhieuKiemKe() {
        try {
            java.util.Map<String, Integer> tonMoi = new java.util.HashMap<>();
            for (SanPham sp : KiemKeControl.layDanhSachSanPhamKemTonKho())
                tonMoi.put(taoKhoaLoSanPham(sp.getMaSanPham(), sp.getMaPhieuNhap()), sp.getTonKho());
            boolean coLoKhongConTon = tatCa.removeIf(ct -> !tonMoi.containsKey(
                    taoKhoaLoSanPham(ct.getMaSanPham(), ct.getMaPhieuNhap())));
            if (coLoKhongConTon)
                UIStyle.thongBao(this, "Các đợt đã hết tồn được loại khỏi phiếu kiểm kê.");
            for (ChiTietPhieuKiemKe ct : tatCa) {
                String khoa = taoKhoaLoSanPham(ct.getMaSanPham(), ct.getMaPhieuNhap());
                if (ct.daKiem() && tonMoi.containsKey(khoa))
                    ct.setSoLuongHeThong(tonMoi.get(khoa));
            }
            capNhatBangKiemKe();
        } catch (Exception ex) {
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
        LocalDateTime gioChot = LocalDateTime.now();
        PhieuKiemKe phieu = new PhieuKiemKe(
                maPhieu,
                gioChot,
                true,
                PhienLamViecControl.maNV);
        phieu.setNgayBatDau(ngayBatDau);
        phieu.setNgayChot(gioChot);
        try {
            if (!KiemKeControl.luuPhieuKiemKe(phieu, daKiem)) throw new IllegalStateException("Không lưu được phiếu kiểm kê.");
            UIStyle.thongBao(this, "Đã chốt kiểm kê " + maPhieu + ".");
            taoPhieuKiemKeMoi();
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private String taoKhoaLoSanPham(String maSanPham, String maPhieuNhap) {
        return maSanPham + "|" + maPhieuNhap;
    }
    private void xemLichSuKiemKe() {
        try {
            ArrayList<PhieuKiemKe> ds = KiemKeControl.layDanhSachPhieuKiemKeNhap();
            String[] colsPhieu = { "Mã phiếu", "Trạng thái", "Thời điểm bắt đầu", "Mã NV lập" };
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
            JTable tblBangPhieu = new JTable(modelPhieu);
            UIStyle.taoBang(tblBangPhieu, 0);
            tblBangPhieu.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            int[] rongCot = { 95, 80, 170, 105 };
            for (int i = 0; i < rongCot.length; i++)
                tblBangPhieu.getColumnModel().getColumn(i).setPreferredWidth(rongCot[i]);
            DefaultTableModel modelChiTiet = new DefaultTableModel(
                    new String[] { "Mã SP", "Tên sản phẩm", "Tồn hệ thống", "Thực tế", "Chênh lệch", "Kết quả" }, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            JTable tblBangChiTiet = new JTable(modelChiTiet);
            UIStyle.taoBang(tblBangChiTiet, 2, 3, 4);
            JLabel lblThongTin2 = new JLabel("Chọn một phiếu để xem chi tiết.");
            JButton btnTiepTuc = UIStyle.nutChinh("Tiếp tục phiếu nháp");
            btnTiepTuc.setEnabled(false);
            JPanel pnlChiTiet = new JPanel(new BorderLayout(0, 8));
            pnlChiTiet.setBorder(new EmptyBorder(8, 8, 8, 8));
            pnlChiTiet.add(lblThongTin2, BorderLayout.NORTH);
            pnlChiTiet.add(UIStyle.cuon(tblBangChiTiet), BorderLayout.CENTER);
            JSplitPane noiDung = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    UIStyle.cuon(tblBangPhieu), pnlChiTiet);
            noiDung.setResizeWeight(0.57);
            JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            pnlControls.add(btnTiepTuc);
            JPanel pnlBody = new JPanel(new BorderLayout(0, 8));
            pnlBody.setBorder(new EmptyBorder(12, 12, 12, 12));
            pnlBody.add(new JLabel("Phiếu nháp kiểm kê — chọn phiếu để xem hoặc tiếp tục kiểm kê."),
                    BorderLayout.NORTH);
            pnlBody.add(noiDung, BorderLayout.CENTER);
            pnlBody.add(pnlControls, BorderLayout.SOUTH);
            JDialog winDlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Phiếu nháp kiểm kê",
                    Dialog.ModalityType.APPLICATION_MODAL);
            winDlg.setContentPane(pnlBody);
            winDlg.setSize(1180, 620);
            winDlg.setLocationRelativeTo(this);
            tblBangPhieu.getSelectionModel().addListSelectionListener(e -> {
                if (e.getValueIsAdjusting())
                    return;
                int row = tblBangPhieu.getSelectedRow();
                if (row < 0)
                    return;
                PhieuKiemKe p = ds.get(tblBangPhieu.convertRowIndexToModel(row));
                btnTiepTuc.setEnabled(!p.isTrangThai());
                try {
                    lblThongTin2
                            .setText("Phiếu " + p.getMaPhieuKiemKe() + " | " + (p.isTrangThai() ? "Đã chốt" : "Nháp")
                                    + " | Bắt đầu: " + dinhDangGio(p.getNgayBatDau())
                                    + " | Mã NV lập: " + p.getMaNhanVien());
                    modelChiTiet.setRowCount(0);
                    for (ChiTietPhieuKiemKe ct : KiemKeControl.layChiTietPhieuKiemKe(p.getMaPhieuKiemKe())) {
                        Integer tt = ct.getSoLuongThucTe();
                        modelChiTiet
                                .addRow(new Object[] { ct.getMaSanPham(), ct.getTenSanPham(), ct.getSoLuongHeThong(),
                                        tt == null ? "Chưa kiểm" : tt,
                                        tt == null ? "—" : (ct.getChenhLech() > 0 ? "+" : "") + ct.getChenhLech(),
                                        tt == null ? "Chưa kiểm" : ct.getChenhLech() == 0 ? "Khớp" : "Chênh lệch" });
                    }
                } catch (Exception ex) {
                    UIStyle.loi(this, ex);
                }
            });
            btnTiepTuc.addActionListener(e -> {
                int row = tblBangPhieu.getSelectedRow();
                if (row < 0)
                    return;
                PhieuKiemKe p = ds.get(tblBangPhieu.convertRowIndexToModel(row));
                if (p.isTrangThai())
                    return;
                if (coSanPhamDaKiem() && !UIStyle.xacNhan(this, "Tiếp tục phiếu nháp sẽ thay phiếu đang kiểm. Tiếp tục?"))
                    return;
                try {
                    maPhieu = p.getMaPhieuKiemKe();
                    lblTieuDe3.setText("Kiểm kê kho — " + maPhieu);
                    LocalDateTime bd = p.getNgayBatDau() == null ? LocalDateTime.now() : p.getNgayBatDau();
                    ngayBatDau = bd;
                    capNhatThoiGianKiemKe();
                    tatCa.clear();
                    tatCa.addAll(KiemKeControl.layChiTietPhieuKiemKe(maPhieu));
                    capNhatBangKiemKe();
                    winDlg.dispose();
                } catch (Exception ex) {
                    UIStyle.loi(this, ex);
                }
            });
            winDlg.setVisible(true);
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
    private String dinhDangGio(LocalDateTime thoiDiem) {
        return thoiDiem == null ? "—"
                : thoiDiem.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    private void luuSoLieuKiemKe() {
        try {
            PhieuKiemKe p = new PhieuKiemKe(maPhieu, ngayBatDau, false, PhienLamViecControl.maNV);
            p.setNgayBatDau(ngayBatDau);
            if (!KiemKeControl.luuPhieuKiemKeNhap(p, tatCa)) throw new IllegalStateException("Không lưu được phiếu nháp kiểm kê.");
            UIStyle.thongBao(this, "Đã lưu nháp phiếu " + maPhieu + ".");
        } catch (Exception ex) {
            UIStyle.loi(this, ex);
        }
    }
}
