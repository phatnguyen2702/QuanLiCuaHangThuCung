package UI;

import dao.KhachHangDAO;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class QuanLyKhachHang extends JPanel {

        private static final String[] COLS = { "Ma KH", "Khach hang", "So dien thoai", "Thu cung",
                        "Hang", "Diem", "Tong chi", "Gan nhat" };

        private final KhachHangDAO khachHangDAO = new KhachHangDAO();

        private final JTextField txtTim = new JTextField(30);

        private final JComboBox<String> cboHang = new JComboBox<>(
                        new String[] { "Tat ca hang", "Khong co the", "Silver", "Gold", "Platinum" });

        private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                        return false;
                }
        };

        private final JTable table = new JTable(model);

        private final JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));

        // moThem: chay khi bam nut "Them khach hang" (TrangChu chuyen sang man them)
        public QuanLyKhachHang(Runnable moThem) {

                setLayout(new BorderLayout());

                setBackground(UiKit.LIGHT_BLUE);

                JButton btnThem = UiKit.button("Them khach hang", UiKit.GREEN, Color.WHITE);

                btnThem.addActionListener(e -> moThem.run());

                add(UiKit.titleBar("QUAN LY KHACH HANG", btnThem), BorderLayout.NORTH);

                stats.setOpaque(false);

                // ---------------- the tim kiem ----------------
                JPanel search = UiKit.card(new FlowLayout(FlowLayout.LEFT, 12, 0));

                UiKit.field(txtTim);

                cboHang.setFont(UiKit.F);

                JButton btnTim = UiKit.button("Tim kiem", UiKit.NAVY, Color.WHITE);
                JButton btnXuat = UiKit.button("Xuat danh sach", Color.WHITE, UiKit.NAVY);

                btnTim.addActionListener(e -> taiLai());
                txtTim.addActionListener(e -> taiLai());
                btnXuat.addActionListener(e -> xuatCsv());

                JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

                nut.setOpaque(false);
                nut.setBorder(new EmptyBorder(16, 0, 0, 0));
                nut.add(btnTim);
                nut.add(btnXuat);

                search.add(UiKit.captioned("Tim khach hang (ten, SDT hoac ma KH)", txtTim));
                search.add(UiKit.captioned("Hang thanh vien", cboHang));
                search.add(nut);

                // ---------------- bang ----------------
                UiKit.styleTable(table);

                DefaultTableCellRenderer phai = new DefaultTableCellRenderer();

                phai.setHorizontalAlignment(SwingConstants.RIGHT);

                table.getColumnModel().getColumn(5).setCellRenderer(phai);
                table.getColumnModel().getColumn(6).setCellRenderer(phai);

                int[] w = { 70, 160, 110, 220, 80, 90, 120, 100 };

                for (int i = 0; i < w.length; i++) {
                        table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
                }

                JScrollPane sp = new JScrollPane(table);

                sp.setBorder(BorderFactory.createLineBorder(UiKit.BORDER));

                sp.getViewport().setBackground(Color.WHITE);

                JPanel top = new JPanel();

                top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

                top.setOpaque(false);

                top.add(stats);
                top.add(Box.createVerticalStrut(8));
                top.add(search);

                JPanel center = new JPanel(new BorderLayout(0, 8));

                center.setOpaque(false);

                center.setBorder(new EmptyBorder(12, 25, 15, 25));

                center.add(top, BorderLayout.NORTH);
                center.add(sp, BorderLayout.CENTER);

                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

                actions.setOpaque(false);

                JButton btnXoa = UiKit.button("Xoa khach hang", UiKit.RED, Color.WHITE);

                btnXoa.addActionListener(e -> xoaKhach());

                actions.add(btnXoa);

                center.add(actions, BorderLayout.SOUTH);

                add(center, BorderLayout.CENTER);

                taiLai();
        }

        private JPanel theThongKe(String title, String value) {

                JPanel p = UiKit.card(new GridLayout(2, 1));

                p.add(UiKit.label(title, UiKit.F, UiKit.MUTED));
                p.add(UiKit.label(value, UiKit.BIG, UiKit.NAVY));

                return p;
        }

        // Nap lai thong ke + bang (goi lai sau khi them khach)
        public void taiLai() {

                long[] tk = khachHangDAO.thongKe();

                stats.removeAll();

                stats.add(theThongKe("Tong khach hang", UiKit.tien(tk[0])));
                stats.add(theThongKe("Khach co the thanh vien", UiKit.tien(tk[1])));
                stats.add(theThongKe("Tong diem tich luy", UiKit.tien(tk[2])));
                stats.add(theThongKe("Khach mua thang nay", UiKit.tien(tk[3])));

                stats.revalidate();
                stats.repaint();

                ArrayList<KhachHang> ds = khachHangDAO.timKiem(
                                txtTim.getText(), (String) cboHang.getSelectedItem());

                model.setRowCount(0);

                for (KhachHang k : ds) {

                        model.addRow(new Object[] {
                                        k.getMaKhachHang(),
                                        k.getHoTen(),
                                        k.getSoDienThoai(),
                                        k.getThuCung() == null ? "" : k.getThuCung(),
                                        k.getHangThe() == null ? "-" : k.getHangThe(),
                                        UiKit.tien(k.getDiemTichLuy()),
                                        UiKit.tien(k.getTongChi()) + " d",
                                        k.getGanNhat() == null ? "" : k.getGanNhat() });
                }
        }

        private void xoaKhach() {

                int row = table.getSelectedRow();

                if (row < 0) {

                        JOptionPane.showMessageDialog(this, "Hay chon 1 khach hang trong bang.");

                        return;
                }

                String ma = String.valueOf(model.getValueAt(row, 0));
                String ten = String.valueOf(model.getValueAt(row, 1));

                int ok = JOptionPane.showConfirmDialog(this,
                                "Xoa khach hang " + ma + " - " + ten + "?\n"
                                                + "(Cung xoa the thanh vien va thu cung cua khach)",
                                "Xac nhan xoa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (ok != JOptionPane.YES_OPTION) {
                        return;
                }

                String loi = khachHangDAO.xoa(ma);

                if (loi != null) {

                        JOptionPane.showMessageDialog(this, loi, "Khong xoa duoc",
                                        JOptionPane.WARNING_MESSAGE);

                        return;
                }

                taiLai();
        }

        // Xuat file CSV (mo duoc bang Excel)
        private void xuatCsv() {

                JFileChooser fc = new JFileChooser();

                fc.setSelectedFile(new java.io.File("danh_sach_khach_hang.csv"));

                if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                        return;
                }

                try (Writer w = new OutputStreamWriter(
                                new FileOutputStream(fc.getSelectedFile()), StandardCharsets.UTF_8)) {

                        w.write('\uFEFF');

                        for (int c = 0; c < model.getColumnCount(); c++) {
                                w.write((c > 0 ? "," : "") + model.getColumnName(c));
                        }

                        w.write("\r\n");

                        for (int r = 0; r < model.getRowCount(); r++) {

                                for (int c = 0; c < model.getColumnCount(); c++) {

                                        String v = String.valueOf(model.getValueAt(r, c)).replace("\"", "\"\"");

                                        w.write((c > 0 ? "," : "") + "\"" + v + "\"");
                                }

                                w.write("\r\n");
                        }

                        JOptionPane.showMessageDialog(this, "Da xuat danh sach.");

                } catch (Exception ex) {

                        JOptionPane.showMessageDialog(this, "Xuat that bai: " + ex.getMessage(),
                                        "Loi", JOptionPane.ERROR_MESSAGE);
                }
        }
}
