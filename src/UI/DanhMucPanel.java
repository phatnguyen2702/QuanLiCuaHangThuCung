package UI;

import UI.TrangChu.PhienLamViec;
import UI.TrangChu.UIStyle;
import dao.DanhMucDAO;
import entity.DanhMuc;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Man hinh "Danh muc" - Quan ly danh muc (nam lblTrong khung TrangChu).
 * Form Them / Sua danh muc la lop long DanhMucDialog o cuoi file.
 */
public class DanhMucPanel extends JFrame implements ActionListener, MouseListener {
    @Override public void actionPerformed(ActionEvent e) { }
    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mousePressed(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }

    // dao: Đối tượng truy cập dữ liệu danh mục.
    private final DanhMucDAO dao = new DanhMucDAO();

    // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã", "Tên danh mục", "Mô tả", "Số sản phẩm", "Tổng tồn kho"}, 0) {
        // Không cho phép sửa trực tiếp các ô lblTrong bảng.
        // Tham số row: chỉ số dòng.
        // Tham số col: chỉ số cột.
        // Kết quả: false, bảng chỉ dùng để xem.
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    // tblDuLieu2: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
    private final JTable tblDuLieu2 = new JTable(model);
    // txtTim: Ô nhập/hiển thị văn bản tìm.
    private final JTextField txtTim = UIStyle.o(18);
    // lblTong: Nhãn hiển thị: 0.
    private final JLabel lblTong = new JLabel("0");

    // dsHienTai: Lưu danh sách hiện tại; phần tử có kiểu DanhMuc.
    private ArrayList<DanhMuc> dsHienTai = new ArrayList<>();

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    public DanhMucPanel() {

        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.LIGHT_BLUE);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(20, 25, 20, 25));

        add(taoPhanDau(), BorderLayout.NORTH);
        add(taoPhanBang(), BorderLayout.CENTER);

        napLai();
    }

    // =========================================================
    // GIAO DIEN
    // =========================================================

    // Tạo phần đầu.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPhanDau() {

        // pnlDau2: Khung chứa thành phần giao diện đầu.
        JPanel pnlDau2 = new JPanel();
        pnlDau2.setOpaque(false);
        pnlDau2.setLayout(new BoxLayout(pnlDau2, BoxLayout.Y_AXIS));

        // lblTieuDe2: Nhãn hiển thị: Quản lý danh mục.
        JLabel lblTieuDe2 = new JLabel("Quản lý danh mục");
        lblTieuDe2.setFont(UIStyle.font(Font.BOLD, 26));

        // btnThem: Nút thao tác thêm.
        JButton btnThem = UIStyle.nutXanh("+ Thêm danh mục");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnThem.addActionListener(e -> them());

        // pnlHang: Khung chứa thành phần giao diện hàng.
        JPanel pnlHang = new JPanel(new BorderLayout());
        pnlHang.setOpaque(false);
        pnlHang.add(lblTieuDe2, BorderLayout.WEST);
        pnlHang.add(btnThem, BorderLayout.EAST);

        // pnlThe: Khung chứa thành phần giao diện thẻ.
        JPanel pnlThe = UIStyle.theThongKe("Tổng danh mục", lblTong);
        pnlThe.setPreferredSize(new Dimension(250, 88));

        // pnlHangThe: Khung chứa thành phần giao diện hạng thẻ thành viên.
        JPanel pnlHangThe = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlHangThe.setOpaque(false);
        pnlHangThe.add(pnlThe);

        pnlDau2.add(pnlHang);
        pnlDau2.add(Box.createVerticalStrut(15));
        pnlDau2.add(pnlHangThe);
        pnlDau2.add(Box.createVerticalStrut(15));

        return pnlDau2;
    }

    // Tạo phần bảng dữ liệu.
    // Kết quả: đối tượng/giá trị kiểu JPanel.
    private JPanel taoPhanBang() {

        // pnlThe: Khung chứa thành phần giao diện thẻ.
        JPanel pnlThe = UIStyle.the();
        pnlThe.setLayout(new BorderLayout(0, 12));

        // pnlThanhCongCu: Khung chứa thành phần giao diện thanh công cụ.
        JPanel pnlThanhCongCu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlThanhCongCu.setOpaque(false);

        // btnLoc: Nút thao tác lọc.
        JButton btnLoc = UIStyle.nutChinh("Lọc");
        // btnSua: Nút thao tác sửa.
        JButton btnSua = UIStyle.nutPhu("Sửa");
        // btnXoa: Nút thao tác xóa.
        JButton btnXoa = UIStyle.nutXoa("Xóa");

        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLoc.addActionListener(e -> napLai());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        txtTim.addActionListener(e -> napLai());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnSua.addActionListener(e -> sua());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnXoa.addActionListener(e -> xoa());

        pnlThanhCongCu.add(UIStyle.nhan("Tìm danh mục:"));
        pnlThanhCongCu.add(txtTim);
        pnlThanhCongCu.add(btnLoc);
        pnlThanhCongCu.add(btnSua);
        pnlThanhCongCu.add(btnXoa);

        UIStyle.taoBang(tblDuLieu2, 3, 4);
        // Cho chon nhieu dong (Ctrl / Shift) de xoa cung luc
        tblDuLieu2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tblDuLieu2.addMouseListener(new MouseAdapter() {
            // Xử lý thao tác nhấn chuột trên thành phần đã gắn listener.
            // Tham số e: thông tin sự kiện.
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblDuLieu2.getSelectedRow() >= 0) {
                    sua();
                }
            }
        });

        pnlThe.add(pnlThanhCongCu, BorderLayout.NORTH);
        pnlThe.add(UIStyle.cuon(tblDuLieu2), BorderLayout.CENTER);

        return pnlThe;
    }

    // =========================================================
    // DU LIEU
    // =========================================================

    // Nạp lại dữ liệu và cập nhật nội dung màn hình.
    public void napLai() {

        try {
            dsHienTai = dao.timKiem(txtTim.getText());

            model.setRowCount(0);
            for (DanhMuc dm : dsHienTai) {
                model.addRow(new Object[]{
                        dm.getMaDanhMuc(),
                        dm.getTenDanhMuc(),
                        dm.getMoTa(),
                        dm.getSoSanPham(),
                        dm.getTongTon()
                });
            }

            // Cập nhật nội dung chữ hiển thị lblTrong control bằng setText().
            lblTong.setText(String.valueOf(dao.demTatCa()));

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // Lấy đối tượng tương ứng với dòng đang được chọn.
    // Kết quả: đối tượng/giá trị kiểu DanhMuc.
    private DanhMuc dangChon() {

        // rows: Mảng lưu các dòng dữ liệu.
        int[] rows = tblDuLieu2.getSelectedRows();

        if (rows.length == 0) {
            UIStyle.canhBao(this, "Vui lòng chọn một danh mục trong bảng.");
            return null;
        }

        if (rows.length > 1) {
            UIStyle.canhBao(this, "Chỉ sửa được từng danh mục một, vui lòng chỉ chọn 1 dòng.");
            return null;
        }

        return dsHienTai.get(rows[0]);
    }

    // =========================================================
    // CHUC NANG
    // =========================================================

    // Xử lý yêu cầu thêm dữ liệu từ giao diện.
    private void them() {

        try {
            String maMoi = dao.taoMaMoi();
            DanhMucDialog winDlg = new DanhMucDialog(
                    SwingUtilities.getWindowAncestor(this), null, maMoi);
            winDlg.setVisible(true);
            if (winDlg.isDaLuu()) napLai();
        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // Xử lý yêu cầu sửa bản ghi đang chọn.
    private void sua() {

        DanhMuc dm = dangChon();
        if (dm == null) {
            return;
        }

        DanhMucDialog winDlg = new DanhMucDialog(
                SwingUtilities.getWindowAncestor(this), dm, null);
        winDlg.setVisible(true);

        if (winDlg.isDaLuu()) {
            napLai();
        }
    }

    // Xoa BAT KY dong nao dang chon (chon nhieu dong cung duoc)
    // Xử lý yêu cầu xóa bản ghi đang chọn.
    private void xoa() {

        // rows: Mảng lưu các dòng dữ liệu.
        int[] rows = tblDuLieu2.getSelectedRows();

        if (rows.length == 0) {
            UIStyle.canhBao(this, "Vui lòng chọn dòng (danh mục) cần xóa.");
            return;
        }

        // dsXoa: Lưu danh sách xóa; phần tử có kiểu DanhMuc.
        ArrayList<DanhMuc> dsXoa = new ArrayList<>();
        for (int r : rows) {
            dsXoa.add(dsHienTai.get(r));
        }

        try {
            // Danh muc co san pham khong pnlThe xoa de bao toan lich su kho/ban pnlHang.
            int tongSp = 0;
            // ten: Bộ đệm ghép các đoạn chuỗi để tạo tên.
            StringBuilder ten = new StringBuilder();

            for (DanhMuc dm : dsXoa) {
                // c: Mảng lưu cột/thành phần được xử lý.
                int[] c = dao.demLienQuan(dm.getMaDanhMuc());
                tongSp += c[0];
                ten.append("\n  - ").append(dm.getMaDanhMuc())
                   .append(" ").append(dm.getTenDanhMuc());
            }

            // hoi: Bộ đệm ghép các đoạn chuỗi để tạo hỏi.
            StringBuilder hoi = new StringBuilder();
            hoi.append("Xóa ").append(dsXoa.size()).append(" danh mục:")
               .append(ten);

            if (tongSp > 0) {
                UIStyle.canhBao(this, "Không thể xóa danh mục đang có " + tongSp
                        + " sản phẩm. Hãy chuyển sản phẩm sang danh mục khác trước.");
                return;
            }

            hoi.append("\n\nBạn chắc chắn muốn xóa danh mục rỗng?");

            if (!UIStyle.xacNhan(this, hoi.toString())) {
                return;
            }

            for (DanhMuc dm : dsXoa) {
                dao.xoa(dm.getMaDanhMuc());
            }

            napLai();

        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
            napLai();
        }
    }

    // =========================================================
    // FORM THEM / SUA DANH MUC
    // =========================================================

    /**
     * Form Them / Sua danh muc: ma, ten, mo ta (3 cot cua tblDuLieu2 DanhMuc).
     * Khi SUA con co tblDuLieu2 san pham thuoc danh muc de ghi THEM so luong
     * (luu thanh phieu nhap kho hoan tat nen ton kho van khop voi database).
     */
    private static class DanhMucDialog extends JDialog {

        // dao: Đối tượng truy cập dữ liệu danh mục.
        private final DanhMucDAO dao = new DanhMucDAO();
        // dangSua: Cờ cho biết đang ở chế độ sửa.
        private final boolean dangSua;

        // txtMa: Ô nhập/hiển thị văn bản mã.
        private final JTextField txtMa = UIStyle.o(22);
        // txtTen: Ô nhập/hiển thị văn bản tên.
        private final JTextField txtTen = UIStyle.o(22);
        // txaMoTa: Vùng văn bản nhiều dòng mô tả.
        private final JTextArea txaMoTa = new JTextArea(4, 22);

        // modelSp: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
        private final DefaultTableModel modelSp = new DefaultTableModel(
                new String[]{"Mã SP", "Tên sản phẩm", "Tồn hiện tại", "Thêm số lượng"}, 0) {
            // Quy định ô của bảng có được chỉnh sửa trực tiếp hay không.
            // Tham số row: chỉ số dòng.
            // Tham số col: chỉ số cột.
            // Kết quả: true/false theo điều kiện xử lý.
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 3;
            }
        };
        // tblBangSp: Bảng hiển thị dữ liệu bảng sản phẩm.
        private final JTable tblBangSp = new JTable(modelSp);
        // dsSanPham: Lưu danh sách sản phẩm; phần tử có kiểu SanPham.
        private final List<SanPham> dsSanPham = new ArrayList<>();

        // daLuu: Cờ cho biết dữ liệu đã được lưu.
        private boolean daLuu = false;

        /** dm = null: them moi. maMoi: ma goi y khi them. */
        public DanhMucDialog(Window winCha, DanhMuc dm, String maMoi) {

            super(winCha, dm == null ? "Thêm danh mục" : "Sửa danh mục",
                    ModalityType.APPLICATION_MODAL);

            dangSua = dm != null;

            // pnlForm2: Khung chứa thành phần giao diện biểu mẫu.
            JPanel pnlForm2 = new JPanel(new GridBagLayout());
            pnlForm2.setBorder(new EmptyBorder(20, 25, 10, 25));

            txaMoTa.setFont(UIStyle.font(Font.PLAIN, 13));
            txaMoTa.setLineWrap(true);
            txaMoTa.setWrapStyleWord(true);
            // scrCuonMoTa: Khung cuộn cuộn mô tả.
            JScrollPane scrCuonMoTa = new JScrollPane(txaMoTa);

            UIStyle.hang(pnlForm2, 0, "Mã danh mục", txtMa, false);
            UIStyle.hang(pnlForm2, 1, "Tên danh mục", txtTen, false);
            UIStyle.hang(pnlForm2, 2, "Mô tả", scrCuonMoTa, true);

            JComponent phanSoLuong = dangSua ? taoBangSoLuong(dm) : null;

            if (dangSua) {
                // Cập nhật nội dung chữ hiển thị lblTrong control bằng setText().
                txtMa.setText(dm.getMaDanhMuc());
                txtMa.setEditable(false);
                txtMa.setBackground(UIStyle.READONLY);
                // Cập nhật nội dung chữ hiển thị lblTrong control bằng setText().
                txtTen.setText(dm.getTenDanhMuc());
                // Cập nhật nội dung chữ hiển thị lblTrong control bằng setText().
                txaMoTa.setText(dm.getMoTa());
            } else {
                // Cập nhật nội dung chữ hiển thị lblTrong control bằng setText().
                txtMa.setText(maMoi);
                txtMa.setEditable(false);
                txtMa.setBackground(UIStyle.READONLY);
            }

            // btnHuy: Nút thao tác hủy.
            JButton btnHuy = UIStyle.nutPhu("Hủy");
            // btnLuu: Nút thao tác lưu.
            JButton btnLuu = UIStyle.nutXanh("Lưu");
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnHuy.addActionListener(e -> dispose());
            // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
            btnLuu.addActionListener(e -> luu());

            // pnlNut2: Khung chứa thành phần giao diện nút.
            JPanel pnlNut2 = UIStyle.hangNut(btnHuy, btnLuu);
            pnlNut2.setBorder(new EmptyBorder(5, 25, 20, 25));

            setLayout(new BorderLayout());
            add(pnlForm2, BorderLayout.NORTH);
            if (phanSoLuong != null) {
                add(phanSoLuong, BorderLayout.CENTER);
            }
            add(pnlNut2, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(btnLuu);

            pack();
            setResizable(false);
            setLocationRelativeTo(winCha);
        }

        // Bang san pham cua danh muc: chi cot "Them so luong" duoc go
        // Tạo bảng số lượng.
        // Tham số dm: danh mục cần xử lý.
        // Kết quả: đối tượng/giá trị kiểu JComponent.
        private JComponent taoBangSoLuong(DanhMuc dm) {

            try {
                dsSanPham.addAll(dao.timSanPhamTheoDanhMuc(dm.getMaDanhMuc()));
            } catch (SQLException ex) {
                UIStyle.loi(this, ex);
            }

            for (SanPham sp : dsSanPham) {
                modelSp.addRow(new Object[]{
                        sp.getMaSanPham(), sp.getTenSanPham(), sp.getTonKho(), 0});
            }

            UIStyle.taoBang(tblBangSp, 2, 3);
            tblBangSp.setRowHeight(28);

            // lblTieuDe2: Nhãn hiển thị tiêu đề.
            JLabel lblTieuDe2 = UIStyle.nhan("Sản phẩm trong danh mục — nhập số lượng cần thêm vào cột bên phải");

            // scrCuon: Khung cuộn cuộn.
            JScrollPane scrCuon = UIStyle.cuon(tblBangSp);
            scrCuon.setPreferredSize(new Dimension(520, 170));

            // pnlKhung2: Khung dùng để nhóm và bố trí các thành phần giao diện.
            JPanel pnlKhung2 = new JPanel(new BorderLayout(0, 6));
            pnlKhung2.setBorder(new EmptyBorder(0, 25, 5, 25));
            pnlKhung2.add(lblTieuDe2, BorderLayout.NORTH);

            if (dsSanPham.isEmpty()) {
                // lblTrong: Nhãn hiển thị: Danh mục này chưa có sản phẩm nào..
                JLabel lblTrong = new JLabel("Danh mục này chưa có sản phẩm nào.");
                lblTrong.setBorder(new EmptyBorder(8, 0, 8, 0));
                pnlKhung2.add(lblTrong, BorderLayout.CENTER);
            } else {
                pnlKhung2.add(scrCuon, BorderLayout.CENTER);
            }

            return pnlKhung2;
        }

        // Doc cot "Them so luong"; tra ve null neu co o nhap sai
        // Đọc số lượng thêm.
        // Kết quả: đối tượng/giá trị kiểu List<Integer>.
        private List<Integer> docSoLuongThem() {

            if (tblBangSp.isEditing()) {
                tblBangSp.getCellEditor().stopCellEditing();
            }

            // kq: Lưu kết quả; phần tử có kiểu Integer.
            List<Integer> kq = new ArrayList<>();

            for (int i = 0; i < modelSp.getRowCount(); i++) {
                // chu: Lưu màu chữ.
                String chu = String.valueOf(modelSp.getValueAt(i, 3)).trim();

                if (chu.isEmpty()) {
                    chu = "0";
                }

                if (!chu.matches("\\d{1,7}")) {
                    UIStyle.canhBao(this, "Số lượng thêm của \""
                            + modelSp.getValueAt(i, 1)
                            + "\" phải là số nguyên không âm.\n"
                            + "(Muốn giảm tồn kho hãy dùng Kiểm kê kho.)");
                    return null;
                }

                kq.add(Integer.parseInt(chu));
            }

            return kq;
        }

        // Kiểm tra dữ liệu nhập và lưu thông tin của biểu mẫu.
        private void luu() {

            // ma: Đọc nội dung chữ từ ô nhập để xử lý mã.
            String ma = txtMa.getText().trim();
            // ten: Đọc nội dung chữ từ ô nhập để xử lý tên.
            String ten = txtTen.getText().trim();
            // moTa: Đọc nội dung chữ từ ô nhập để xử lý mô tả.
            String moTa = txaMoTa.getText().trim();

            if (ten.isEmpty()) {
                UIStyle.canhBao(this, "Vui lòng nhập tên danh mục.");
                return;
            }

            if (ma.length() > 10) {
                UIStyle.canhBao(this, "Mã danh mục tối đa 10 ký tự.");
                return;
            }

            if (ten.length() > 50) {
                UIStyle.canhBao(this, "Tên danh mục tối đa 50 ký tự.");
                return;
            }

            if (moTa.length() > 255) {
                UIStyle.canhBao(this, "Mô tả tối đa 255 ký tự.");
                return;
            }

            DanhMuc dm = new DanhMuc(ma, ten, moTa.isEmpty() ? null : moTa);

            // soLuongThem: Lưu số lượng thêm; phần tử có kiểu Integer.
            List<Integer> soLuongThem = dangSua ? docSoLuongThem() : null;
            if (dangSua && soLuongThem == null) {
                return;
            }

            try {
                if (dangSua) {
                    dao.sua(dm);
                    dao.nhapBoSung(
                            dsSanPham, soLuongThem, PhienLamViec.maNV);
                } else {
                    dao.them(dm);
                }
                daLuu = true;
                dispose();

            } catch (SQLException ex) {
                UIStyle.loi(this, ex);
            }
        }

        // Kiểm tra dữ liệu đã được lưu.
        // Kết quả: true/false theo điều kiện xử lý.
        public boolean isDaLuu() {
            return daLuu;
        }
    }
}
