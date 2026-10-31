package UI;

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

/** Xem ma trận quyền cố định: quản lý cấp vai trò tại Quản lý tài khoản. */
public final class MaTranPhanQuyenPanel extends JPanel implements ActionListener, MouseListener {
    // Lưu callback ban đầu, không thay đổi cách điều hướng tới Quản lý tài khoản.
    private final Runnable moQuanLyTaiKhoan;
    private JButton btnAccounts;

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    // Tham số moQuanLyTaiKhoan: mở quản lý tài khoản.
    public MaTranPhanQuyenPanel(Runnable moQuanLyTaiKhoan) {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.PHAN_QUYEN);
        this.moQuanLyTaiKhoan = moQuanLyTaiKhoan;
        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(229, 241, 252));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // pnlHeader2: Khung chứa thành phần giao diện đầu trang.
        JPanel pnlHeader2 = new JPanel(new BorderLayout());
        pnlHeader2.setOpaque(false);
        // lblTitle2: Nhãn hiển thị: MA TRẬN PHÂN QUYỀN.
        JLabel lblTitle2 = new JLabel("MA TRẬN PHÂN QUYỀN");
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle2.setForeground(new Color(18, 48, 82));
        pnlHeader2.add(lblTitle2, BorderLayout.NORTH);
        // lblDesc: Nhãn hiển thị: Quyền cố định theo vai trò. Đổi vai trò từng nhân
        // viên trong Quản lý tài khoản..
        JLabel lblDesc = new JLabel("Quyền cố định theo vai trò. Đổi vai trò từng nhân viên trong Quản lý tài khoản.");
        lblDesc.setBorder(new EmptyBorder(10, 0, 6, 0));
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlHeader2.add(lblDesc, BorderLayout.SOUTH);
        add(pnlHeader2, BorderLayout.NORTH);

        // headers: Mảng lưu tiêu đề cột.
        String[] headers = { "Chức năng", "Quản lý", "Thu ngân", "Nhân viên kho", "Thợ Spa" };
        // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            // Không cho phép sửa trực tiếp các ô trong bảng.
            // Tham số row: chỉ số dòng.
            // Tham số col: chỉ số cột.
            // Kết quả: false, bảng chỉ dùng để xem.
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        // roles: Mảng lưu vai trò.
        PhanQuyen.VaiTro[] roles = { PhanQuyen.VaiTro.QUAN_LY, PhanQuyen.VaiTro.THU_NGAN,
                PhanQuyen.VaiTro.NHAN_VIEN_KHO, PhanQuyen.VaiTro.NHAN_VIEN_SPA };
        for (PhanQuyen.Quyen q : PhanQuyen.Quyen.values()) {
            // values: Mảng lưu các giá trị.
            Object[] values = new Object[5];
            values[0] = PhanQuyen.tenQuyen(q);
            for (int i = 0; i < roles.length; i++) {
                values[i + 1] = PhanQuyen.duocPhep(roles[i], q) ? "Có quyền" : "—";
            }
            model.addRow(values);
        }
        // tblDuLieu: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
        JTable tblDuLieu = new JTable(model);
        tblDuLieu.addMouseListener(this); // Chỉ xem, không đổi nội dung hay quyền trong bảng.
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
        // scrSp: Khung cuộn SP.
        JScrollPane scrSp = new JScrollPane(tblDuLieu);
        scrSp.getViewport().setBackground(Color.WHITE);
        add(scrSp, BorderLayout.CENTER);

        // pnlBottom: Khung chứa thành phần giao diện dưới.
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBottom.setOpaque(false);
        // btnAccounts: Nút thao tác: Mở Quản lý tài khoản.
        btnAccounts = new JButton("Mở Quản lý tài khoản");
        btnAccounts.setBackground(new Color(18, 48, 82));
        btnAccounts.setForeground(Color.WHITE);
        btnAccounts.setFocusPainted(false);
        btnAccounts.setFont(new Font("Segoe UI", Font.BOLD, 13));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
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

    // MouseListener: bảng vẫn dùng ListSelectionListener cho cả chuột và bàn phím.
    // Không thực hiện lại xử lý chọn dòng trong mouseClicked để tránh chạy trùng.
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
