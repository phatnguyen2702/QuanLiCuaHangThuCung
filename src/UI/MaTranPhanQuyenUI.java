package UI;

import control.PhanQuyenControl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


public final class MaTranPhanQuyenUI extends JPanel implements ActionListener, MouseListener {

    private final Runnable moQuanLyTaiKhoan;
    private JButton btnAccounts;

    public MaTranPhanQuyenUI(Runnable moQuanLyTaiKhoan) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.PHAN_QUYEN);
        this.moQuanLyTaiKhoan = moQuanLyTaiKhoan;
        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(229, 241, 252));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel pnlHeader2 = new JPanel(new BorderLayout());
        pnlHeader2.setOpaque(false);

        JLabel lblTitle2 = new JLabel("MA TRẬN PHÂN QUYỀN");
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle2.setForeground(new Color(18, 48, 82));
        pnlHeader2.add(lblTitle2, BorderLayout.NORTH);

        JLabel lblDesc = new JLabel("Quyền cố định theo vai trò. Đổi vai trò từng nhân viên trong Quản lý tài khoản.");
        lblDesc.setBorder(new EmptyBorder(10, 0, 6, 0));
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlHeader2.add(lblDesc, BorderLayout.SOUTH);
        add(pnlHeader2, BorderLayout.NORTH);

        String[] headers = { "Chức năng", "Quản lý", "Thu ngân", "Nhân viên kho", "Thợ Spa" };

        DefaultTableModel model = new DefaultTableModel(headers, 0) {

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        PhanQuyenControl.VaiTro[] roles = { PhanQuyenControl.VaiTro.QUAN_LY, PhanQuyenControl.VaiTro.THU_NGAN,
                PhanQuyenControl.VaiTro.NHAN_VIEN_KHO, PhanQuyenControl.VaiTro.NHAN_VIEN_SPA };
        for (PhanQuyenControl.Quyen q : PhanQuyenControl.Quyen.values()) {

            Object[] values = new Object[5];
            values[0] = PhanQuyenControl.tenQuyen(q);
            for (int i = 0; i < roles.length; i++) {
                values[i + 1] = PhanQuyenControl.duocPhep(roles[i], q) ? "Có quyền" : "—";
            }
            model.addRow(values);
        }

        JTable tblDuLieu = new JTable(model);
        tblDuLieu.addMouseListener(this);
        tblDuLieu.setRowHeight(30);
        tblDuLieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblDuLieu.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblDuLieu.getTableHeader().setBackground(new Color(18, 48, 82));
        tblDuLieu.getTableHeader().setForeground(Color.WHITE);
        tblDuLieu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDuLieu.getColumnModel().getColumn(0).setPreferredWidth(250);
        for (int c = 1; c < tblDuLieu.getColumnCount(); c++) {
            DefaultTableCellRenderer lblCentered = new DefaultTableCellRenderer();
            lblCentered.setHorizontalAlignment(SwingConstants.CENTER);
            tblDuLieu.getColumnModel().getColumn(c).setCellRenderer(lblCentered);
            tblDuLieu.getColumnModel().getColumn(c).setPreferredWidth(120);
        }

        JScrollPane scrSp = new JScrollPane(tblDuLieu);
        scrSp.getViewport().setBackground(Color.WHITE);
        add(scrSp, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBottom.setOpaque(false);

        btnAccounts = new JButton("Mở Quản lý tài khoản");
        btnAccounts.setBackground(new Color(18, 48, 82));
        btnAccounts.setForeground(Color.WHITE);
        btnAccounts.setFocusPainted(false);
        btnAccounts.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnAccounts.addActionListener(this);
        pnlBottom.add(btnAccounts);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAccounts) {
            moQuanLyTaiKhoan.run();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}
