package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Xem ma trận quyền cố định: quản lý cấp vai trò tại Quản lý tài khoản. */
public final class MaTranPhanQuyenPanel extends JPanel {
    public MaTranPhanQuyenPanel(Runnable moQuanLyTaiKhoan) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.PHAN_QUYEN);
        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(229, 241, 252));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("MA TRẬN PHÂN QUYỀN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(18, 48, 82));
        header.add(title, BorderLayout.NORTH);
        JLabel desc = new JLabel("Quyền cố định theo vai trò. Đổi vai trò từng nhân viên trong Quản lý tài khoản.");
        desc.setBorder(new EmptyBorder(10, 0, 6, 0));
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        header.add(desc, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        String[] headers = {"Chức năng", "Quản lý", "Thu ngân", "Nhân viên kho", "Thợ Spa"};
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        PhanQuyen.VaiTro[] roles = {PhanQuyen.VaiTro.QUAN_LY, PhanQuyen.VaiTro.THU_NGAN,
                PhanQuyen.VaiTro.NHAN_VIEN_KHO, PhanQuyen.VaiTro.NHAN_VIEN_SPA};
        for (PhanQuyen.Quyen q : PhanQuyen.Quyen.values()) {
            Object[] values = new Object[5];
            values[0] = PhanQuyen.tenQuyen(q);
            for (int i = 0; i < roles.length; i++) {
                values[i + 1] = PhanQuyen.duocPhep(roles[i], q) ? "Có quyền" : "—";
            }
            model.addRow(values);
        }
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(new Color(18, 48, 82));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getColumnModel().getColumn(0).setPreferredWidth(250);
        for (int c = 1; c < table.getColumnCount(); c++) {
            DefaultTableCellRenderer centered = new DefaultTableCellRenderer();
            centered.setHorizontalAlignment(SwingConstants.CENTER);
            table.getColumnModel().getColumn(c).setCellRenderer(centered);
            table.getColumnModel().getColumn(c).setPreferredWidth(120);
        }
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton btnAccounts = new JButton("Mở Quản lý tài khoản");
        btnAccounts.setBackground(new Color(18, 48, 82));
        btnAccounts.setForeground(Color.WHITE);
        btnAccounts.setFocusPainted(false);
        btnAccounts.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAccounts.addActionListener(e -> moQuanLyTaiKhoan.run());
        bottom.add(btnAccounts);
        add(bottom, BorderLayout.SOUTH);
    }
}
