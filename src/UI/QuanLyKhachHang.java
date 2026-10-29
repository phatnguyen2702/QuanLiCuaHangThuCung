package UI;

import UI.TrangChu.UIStyle;
import dao.KhachHangChiTietDAO;
import dao.KhachHangDAO;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;

// Man Quan ly khach hang: tim kiem, them / sua / btnXoa2, lam moi, chon 1 dong de xem thong tin
// Giao diện/chức năng quản lý khách hàng của ứng dụng.
public class QuanLyKhachHang extends JPanel {

        // COLS: Mảng lưu các cột.
        private static final String[] COLS = { "Mã KH", "Họ tên", "Số điện thoại", "Số thú cưng", "Tổng chi" };

        // khachHangDAO: Đối tượng truy cập dữ liệu khách hàng.
        private final KhachHangDAO khachHangDAO = new KhachHangDAO();
        // chiTietDAO: Đối tượng truy cập dữ liệu khách hàng chi tiết.
        private final KhachHangChiTietDAO chiTietDAO = new KhachHangChiTietDAO();

        // txtTim: Ô nhập/hiển thị văn bản tìm.
        private final JTextField txtTim = UIStyle.o(16);
        // lblDem: Nhãn hiển thị đếm.
        private final JLabel lblDem = UIStyle.nhan("0 khách hàng");

        // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
        private final javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(COLS, 0) {
                // Không cho phép sửa trực tiếp các ô pnlTrong bảng.
                // Tham số pnlKhung3: chỉ số dòng.
                // Tham số cmbC: chỉ số cột.
                // Kết quả: false, bảng chỉ dùng để xem.
                @Override
                public boolean isCellEditable(int r, int c) {
                        return false;
                }
        };

        // tblDuLieu: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
        private final JTable tblDuLieu = new JTable(model);

        // dòng i của bảng <-> dsHienThi.get(i)
        // dsHienThi: Lưu danh sách đang hiển thị; phần tử có kiểu KhachHang.
        private ArrayList<KhachHang> dsHienThi = new ArrayList<>();
        // ----- khung thông tin chi tiết (hiện khi chọn 1 dòng) -----
        // cardCT: Bộ bố trí cho phép chuyển giữa các màn hình pnlTrong cùng một vùng
        // chứa.
        private final CardLayout cardCT = new CardLayout();
        // pnlKhungCT: Khung chứa thành phần giao diện khung chi tiết.
        private final JPanel pnlKhungCT = new JPanel(cardCT);

        // lblVMa: Nhãn hiển thị mã.
        private final JLabel lblVMa = giaTri();
        // lblVTen: Nhãn hiển thị tên.
        private final JLabel lblVTen = giaTri();
        // lblVSdt: Nhãn hiển thị số điện thoại.
        private final JLabel lblVSdt = giaTri();
        // lblVTong: Nhãn hiển thị tổng.
        private final JLabel lblVTong = giaTri();
        // lblVThuCung: Nhãn hiển thị thú cưng.
        private final JLabel lblVThuCung = new JLabel();

        // Khởi tạo đối tượng với trạng thái ban đầu.
        public QuanLyKhachHang() {
                this(null);
        }

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        // Tham số maNV: mã nhân viên.
        public QuanLyKhachHang(String maNV) {
                setLayout(new BorderLayout(0, 14));
                setBackground(UIStyle.LIGHT_BLUE);
                setBorder(new EmptyBorder(18, 24, 18, 24));

                // ---------------- tiêu đề + nút Thêm ----------------
                // lblTieuDe2: Nhãn hiển thị: Quản lý khách hàng.
                JLabel lblTieuDe2 = new JLabel("Quản lý khách hàng");

                lblTieuDe2.setFont(UIStyle.font(Font.BOLD, 26));
                lblTieuDe2.setForeground(new Color(40, 40, 40));

                // btnThem: Nút thao tác thêm.
                JButton btnThem = UIStyle.nutXanh("+  Thêm khách hàng");

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnThem.addActionListener(e -> moDialog(null));

                // pnlDau2: Khung chứa thành phần giao diện đầu.
                JPanel pnlDau2 = new JPanel(new BorderLayout());

                pnlDau2.setOpaque(false);
                pnlDau2.add(lblTieuDe2, BorderLayout.WEST);
                pnlDau2.add(btnThem, BorderLayout.EAST);

                // ---------------- hàng tìm kiếm + nút ----------------
                // btnTim: Nút thao tác tìm.
                JButton btnTim = UIStyle.nutChinh("Tìm kiếm");
                // btnSua: Nút thao tác sửa.
                JButton btnSua = UIStyle.nutPhu("Sửa");
                // btnXoa: Nút thao tác xóa.
                JButton btnXoa = UIStyle.nutXoa("Xóa");
                // btnLamMoi: Nút thao tác làm mới.
                JButton btnLamMoi = UIStyle.nutPhu("Làm mới");

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnTim.addActionListener(e -> taiLai(maDangChon()));
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                txtTim.addActionListener(e -> taiLai(maDangChon()));
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnSua.addActionListener(e -> sua());
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnXoa.addActionListener(e -> xoa());
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnLamMoi.addActionListener(e -> lamMoiTatCa());

                // pnlLoc2: Khung chứa thành phần giao diện lọc.
                JPanel pnlLoc2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

                pnlLoc2.setOpaque(false);
                pnlLoc2.add(UIStyle.nhan("Mã / tên / SĐT:"));
                pnlLoc2.add(txtTim);
                pnlLoc2.add(btnTim);
                pnlLoc2.add(btnSua);
                pnlLoc2.add(btnXoa);
                pnlLoc2.add(btnLamMoi);

                // pnlHangTren: Khung chứa thành phần giao diện hàng trên.
                JPanel pnlHangTren = new JPanel(new BorderLayout(0, 8));

                pnlHangTren.setOpaque(false);
                pnlHangTren.add(pnlLoc2, BorderLayout.NORTH);
                pnlHangTren.add(lblDem, BorderLayout.SOUTH);

                // ---------------- bảng ----------------
                UIStyle.taoBang(tblDuLieu, 6, 7);

                // w: Mảng lưu chiều rộng.
                int[] w = { 70, 160, 110, 230, 120 };

                for (int i = 0; i < w.length; i++) {
                        tblDuLieu.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
                }

                tblDuLieu.getSelectionModel().addListSelectionListener(e -> {
                        if (!e.getValueIsAdjusting()) {
                                hienThongTin();
                        }
                });

                tblDuLieu.addMouseListener(new java.awt.event.MouseAdapter() {
                        // Xử lý thao tác nhấn chuột trên thành phần đã gắn listener.
                        // Tham số e: thông tin sự kiện.
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                                if (e.getClickCount() == 2 && dongDangChon() != null) {
                                        sua();
                                }
                        }
                });

                // ---------------- khung thông tin chi tiết ----------------
                pnlKhungCT.setOpaque(false);

                // lblGoiY: Nhãn hiển thị: Chọn một khách hàng pnlTrong bảng để xem thông tin
                // chi tiết..
                JLabel lblGoiY = new JLabel("Chọn một khách hàng trong bảng để xem thông tin chi tiết.");

                lblGoiY.setFont(UIStyle.font(Font.PLAIN, 13));
                lblGoiY.setForeground(Color.GRAY);

                // pnlTrong: Khung chứa thành phần giao diện pnlTrong.
                JPanel pnlTrong = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));

                pnlTrong.setOpaque(false);
                pnlTrong.add(lblGoiY);

                pnlKhungCT.add(pnlTrong, "TRONG");
                pnlKhungCT.add(taoNoiDungChiTiet(), "CO");
                pnlKhungCT.setPreferredSize(new Dimension(100, 150));

                // pnlChiTiet: Khung chứa thành phần giao diện chi tiết.
                JPanel pnlChiTiet = new JPanel(new BorderLayout(0, 6));

                pnlChiTiet.setOpaque(false);
                pnlChiTiet.setBorder(new javax.swing.border.MatteBorder(1, 0, 0, 0, UIStyle.LINE));

                // lblTdCT: Nhãn hiển thị tiêu đề chi tiết.
                JLabel lblTdCT = UIStyle.nhan("Thông tin khách hàng");

                lblTdCT.setBorder(new EmptyBorder(8, 0, 0, 0));

                pnlChiTiet.add(lblTdCT, BorderLayout.NORTH);
                pnlChiTiet.add(pnlKhungCT, BorderLayout.CENTER);

                // ---------------- thẻ trắng chứa tất cả ----------------
                // pnlThe: Khung chứa thành phần giao diện thẻ.
                JPanel pnlThe = UIStyle.the();

                pnlThe.setLayout(new BorderLayout(0, 10));

                pnlThe.add(pnlHangTren, BorderLayout.NORTH);
                pnlThe.add(UIStyle.cuon(tblDuLieu), BorderLayout.CENTER);
                pnlThe.add(pnlChiTiet, BorderLayout.SOUTH);

                add(pnlDau2, BorderLayout.NORTH);
                add(pnlThe, BorderLayout.CENTER);

                taiLai(null);
        }

        // =========================================================
        // GIAO DIỆN PHỤ
        // =========================================================

        // Tạo nhãn giá trị theo kiểu trình bày của màn hình.
        // Kết quả: đối tượng/giá trị kiểu JLabel.
        private static JLabel giaTri() {

                // lblNhan3: Nhãn hiển thị: -.
                JLabel lblNhan3 = new JLabel("-");

                lblNhan3.setFont(UIStyle.font(Font.BOLD, 13));

                return lblNhan3;
        }

        // Ghép tiêu đề và nhãn giá trị thành một nhóm thông tin.
        // Tham số lblTieuDe2: tiêu đề.
        // Tham số lblGt: giá trị.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private static JPanel cap(String tieuDe, JLabel lblGt) {

                // pnlKhung2: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung2 = new JPanel();

                pnlKhung2.setLayout(new BoxLayout(pnlKhung2, BoxLayout.Y_AXIS));
                pnlKhung2.setOpaque(false);

                // lblNhan3: Nhãn dùng để hiển thị chữ hoặc giá trị trên giao diện.
                JLabel lblNhan3 = new JLabel(tieuDe);

                lblNhan3.setFont(UIStyle.font(Font.PLAIN, 12));
                lblNhan3.setForeground(Color.GRAY);
                lblNhan3.setAlignmentX(Component.LEFT_ALIGNMENT);

                lblGt.setAlignmentX(Component.LEFT_ALIGNMENT);

                pnlKhung2.add(lblNhan3);
                pnlKhung2.add(Box.createVerticalStrut(2));
                pnlKhung2.add(lblGt);

                return pnlKhung2;
        }

        // Tạo nội dung chi tiết.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel taoNoiDungChiTiet() {

                // pnlLuoi2: Khung chứa thành phần giao diện lưới ngày.
                JPanel pnlLuoi2 = new JPanel(new GridLayout(2, 3, 18, 8));

                pnlLuoi2.setOpaque(false);

                pnlLuoi2.add(cap("Mã khách hàng", lblVMa));
                pnlLuoi2.add(cap("Họ tên", lblVTen));
                pnlLuoi2.add(cap("Số điện thoại", lblVSdt));
                pnlLuoi2.add(cap("Tổng chi / mua gần nhất", lblVTong));

                lblVThuCung.setFont(UIStyle.font(Font.PLAIN, 13));

                // pnlPets: Khung chứa thành phần giao diện thú cưng.
                JPanel pnlPets = new JPanel(new BorderLayout(0, 2));

                pnlPets.setOpaque(false);

                // lblNhan3: Nhãn hiển thị: Thú cưng.
                JLabel lblNhan3 = new JLabel("Thú cưng");

                lblNhan3.setFont(UIStyle.font(Font.PLAIN, 12));
                lblNhan3.setForeground(Color.GRAY);

                pnlPets.add(lblNhan3, BorderLayout.NORTH);
                pnlPets.add(lblVThuCung, BorderLayout.CENTER);

                // pnlKhung2: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung2 = new JPanel(new BorderLayout(0, 8));

                pnlKhung2.setOpaque(false);
                pnlKhung2.add(pnlLuoi2, BorderLayout.NORTH);
                pnlKhung2.add(pnlPets, BorderLayout.CENTER);

                return pnlKhung2;
        }

        // =========================================================
        // DỮ LIỆU
        // =========================================================

        // Lấy dòng đang được chọn trên bảng.
        // Kết quả: đối tượng/giá trị kiểu KhachHang.
        private KhachHang dongDangChon() {

                // row: Lưu chỉ số dòng.
                int row = tblDuLieu.getSelectedRow();

                if (row < 0 || row >= dsHienThi.size()) {
                        return null;
                }

                return dsHienThi.get(row);
        }

        // Lấy mã của bản ghi đang được chọn.
        // Kết quả: chuỗi kết quả.
        private String maDangChon() {

                KhachHang k = dongDangChon();

                return k == null ? null : k.getMaKhachHang();
        }

        // Gọi khi mở màn hình (TrangChu): nạp lại dữ liệu, giữ dòng đang chọn
        // Làm mới dữ liệu hoặc trạng thái của màn hình.
        public void lamMoi() {

                taiLai(maDangChon());
        }

        // Nút "Làm mới": xóa bộ lọc, bỏ chọn, nạp lại từ CSDL
        // Làm mới bộ lọc và tải lại dữ liệu màn hình.
        private void lamMoiTatCa() {

                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                txtTim.setText("");

                tblDuLieu.clearSelection();

                taiLai(null);
        }

        // Nạp bảng theo từ khóa; chonMa != null thì chọn lại dòng đó nếu còn
        // Tải lại danh sách và chọn lại bản ghi theo mã nếu có.
        // Tham số chonMa: mã cần chọn lại.
        public void taiLai(String chonMa) {

                dsHienThi = khachHangDAO.timKiem(txtTim.getText());

                tblDuLieu.clearSelection();

                model.setRowCount(0);

                for (KhachHang k : dsHienThi) {

                        model.addRow(new Object[] {
                                        k.getMaKhachHang(),
                                        k.getHoTen(),
                                        k.getSoDienThoai(),
                                        k.getThuCung() == null ? "0" : k.getThuCung(),
                                        UIStyle.tien(k.getTongChi()) });
                }

                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblDem.setText(dsHienThi.size() + " khách hàng");

                if (chonMa != null) {

                        for (int i = 0; i < dsHienThi.size(); i++) {

                                if (dsHienThi.get(i).getMaKhachHang().equals(chonMa)) {

                                        tblDuLieu.setRowSelectionInterval(i, i);

                                        tblDuLieu.scrollRectToVisible(tblDuLieu.getCellRect(i, 0, true));

                                        break;
                                }
                        }
                }

                hienThongTin();
        }

        // Hiện thông tin của dòng đang chọn ở khung bên dưới bảng
        // Hiển thị thông tin.
        private void hienThongTin() {

                KhachHang k = dongDangChon();

                if (k == null) {

                        cardCT.show(pnlKhungCT, "TRONG");

                        return;
                }

                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblVMa.setText(k.getMaKhachHang());
                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblVTen.setText(k.getHoTen());
                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblVSdt.setText(k.getSoDienThoai());
                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblVTong.setText(UIStyle.tien(k.getTongChi())
                                + (k.getGanNhat() == null ? "" : "  /  " + k.getGanNhat()));

                // sb: Bộ đệm ghép các đoạn chuỗi để tạo bộ đệm chuỗi.
                StringBuilder sb = new StringBuilder("<html><body style='width:760px'>");

                // pnlPets: Lưu thú cưng; phần tử có kiểu String[].
                ArrayList<String[]> pets = chiTietDAO.getThuCung(k.getMaKhachHang());

                if (pets.isEmpty()) {

                        sb.append("Chưa có thú cưng nào.");

                } else {

                        for (int i = 0; i < pets.size(); i++) {

                                // pnlKhung2: Mảng lưu pnlKhung2.
                                String[] p = pets.get(i);

                                if (i > 0) {
                                        sb.append("; ");
                                }

                                sb.append("<b>").append(p[0]).append("</b> (").append(p[1] == null ? "" : p[1]);

                                if (p[2] != null && !p[2].isEmpty()) {
                                        sb.append(", ").append(p[2]);
                                }

                                if (p[3] != null && !p[3].isEmpty()) {
                                        sb.append(", ").append(p[3]);
                                }

                                if (!p[4].isEmpty()) {
                                        sb.append(", ").append(p[4]).append(" kg");
                                }

                                sb.append(")");
                        }
                }

                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblVThuCung.setText(sb.append("</body></html>").toString());

                cardCT.show(pnlKhungCT, "CO");
        }

        // =========================================================
        // THÊM / SỬA / XÓA
        // =========================================================

        // Xử lý yêu cầu sửa bản ghi đang chọn.
        private void sua() {

                KhachHang k = dongDangChon();

                if (k == null) {

                        UIStyle.canhBao(this, "Hãy chọn một khách hàng trong bảng để sửa.");

                        return;
                }

                moDialog(k);
        }

        // Xử lý yêu cầu xóa bản ghi đang chọn.
        private void xoa() {

                KhachHang k = dongDangChon();

                if (k == null) {

                        UIStyle.canhBao(this, "Hãy chọn một khách hàng trong bảng để xóa.");

                        return;
                }

                if (!UIStyle.xacNhan(this, "Xóa khách hàng " + k.getMaKhachHang() + " - " + k.getHoTen() + "?\n"
                                + "(Cũng xóa thú cưng của khách này)")) {
                        return;
                }

                String loi = khachHangDAO.xoa(k.getMaKhachHang());

                if (loi != null) {

                        UIStyle.canhBao(this, loi);

                        return;
                }

                taiLai(null);
        }

        // goc = null -> thêm mới; goc != null -> sửa khách đó
        // Mở hộp thoại.
        // Tham số goc: đối tượng gốc trước khi chỉnh sửa.
        private void moDialog(KhachHang goc) {

                KhachHangDialog winDlg = new KhachHangDialog(
                                SwingUtilities.getWindowAncestor(this),
                                goc, khachHangDAO, chiTietDAO);

                winDlg.setVisible(true);

                if (winDlg.isDaLuu()) {
                        taiLai(winDlg.getMaDaLuu());
                }
        }

        // =========================================================
        // HỘP THOẠI THÊM / SỬA KHÁCH HÀNG (class lồng)
        // =========================================================

        // Hộp thoại nhập hoặc chỉnh sửa khách hàng.
        public static class KhachHangDialog extends JDialog {

                // Giá trị lưu vào CSDL (dùng Unicode escape cho chắc chắn)
                // LOAI: Mảng lưu loài.
                private static final String[] LOAI = { "Ch\u00f3", "M\u00e8o", "Kh\u00e1c" };
                // GIOI_TINH: Mảng lưu tính.
                private static final String[] GIOI_TINH = { "\u0110\u1ef1c", "C\u00e1i" };

                // VIEN: Màu dùng cho giao diện: viên.
                private static final Color VIEN = new Color(110, 130, 160);
                // XAM: Màu dùng cho giao diện: xám.
                private static final Color XAM = new Color(110, 120, 135);
                // F14: Phông chữ dùng cho 14.
                private static final Font F14 = new Font("Segoe UI", Font.PLAIN, 14);
                // NHAN: Phông chữ dùng cho nhân.
                private static final Font NHAN = new Font("Segoe UI", Font.BOLD, 12);

                // khachHangDAO: Đối tượng truy cập dữ liệu khách hàng.
                private final KhachHangDAO khachHangDAO;
                // chiTietDAO: Đối tượng truy cập dữ liệu khách hàng chi tiết.
                private final KhachHangChiTietDAO chiTietDAO;

                // goc: Đối tượng đối tượng gốc trước khi chỉnh sửa có kiểu KhachHang.
                private final KhachHang goc;

                // daLuu: Cờ cho biết dữ liệu đã được lưu.
                private boolean daLuu = false;
                // maDaLuu: Lưu mã bản ghi vừa lưu.
                private String maDaLuu = null;

                // txtTen: Ô nhập/hiển thị văn bản tên.
                private final JTextField txtTen = new JTextField();
                // txtSdt: Ô nhập/hiển thị văn bản số điện thoại.
                private final JTextField txtSdt = new JTextField();
                // pnlPetsBox: Khung chứa thành phần giao diện khung danh sách thú cưng.
                private final JPanel pnlPetsBox = new JPanel();
                // rows: Danh sách chứa các phần tử kiểu PetRow.
                private final ArrayList<PetRow> rows = new ArrayList<>();

                // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
                // Tham số winOwner: cửa sổ sở hữu.
                // Tham số goc: đối tượng gốc trước khi chỉnh sửa.
                // Tham số khachHangDAO: đối tượng truy cập dữ liệu khách hàng.
                // Tham số chiTietDAO: đối tượng truy cập dữ liệu khách hàng chi tiết.
                public KhachHangDialog(Window winOwner, KhachHang goc,
                                KhachHangDAO khachHangDAO, KhachHangChiTietDAO chiTietDAO) {

                        super(winOwner, goc == null ? "Thêm Khách Hàng" : "Sửa Khách Hàng",
                                        Dialog.ModalityType.APPLICATION_MODAL);

                        this.goc = goc;
                        this.khachHangDAO = khachHangDAO;
                        this.chiTietDAO = chiTietDAO;

                        setSize(760, goc == null ? 490 : 330);
                        setResizable(false);
                        setLocationRelativeTo(winOwner);

                        kieu(txtTen);
                        kieu(txtSdt);

                        // pnlBody: Khung chứa thành phần giao diện nội dung.
                        JPanel pnlBody = new JPanel();

                        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
                        pnlBody.setOpaque(false);
                        pnlBody.setBorder(new EmptyBorder(24, 40, 10, 40));

                        them(pnlBody, muc(goc == null ? "Thông tin Khách hàng"
                                        : "Thông tin Khách hàng (" + goc.getMaKhachHang() + ")"));
                        them(pnlBody, Box.createVerticalStrut(8));
                        them(pnlBody, hai(cot("Họ và tên: *", txtTen), cot("Số điện thoại: *", txtSdt)));
                        // Thú cưng: chỉ nhập khi thêm mới
                        if (goc == null) {

                                pnlPetsBox.setLayout(new BoxLayout(pnlPetsBox, BoxLayout.Y_AXIS));
                                pnlPetsBox.setOpaque(false);

                                // pnlPetsWrap: Khung chứa thành phần giao diện thú cưng bọc.
                                JPanel pnlPetsWrap = new JPanel(new BorderLayout());

                                pnlPetsWrap.setBackground(Color.WHITE);
                                pnlPetsWrap.add(pnlPetsBox, BorderLayout.NORTH);

                                // scrSp: Khung cuộn SP.
                                JScrollPane scrSp = new JScrollPane(pnlPetsWrap);

                                scrSp.setBorder(new LineBorder(new Color(220, 226, 235)));
                                scrSp.setPreferredSize(new Dimension(100, 130));
                                scrSp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
                                scrSp.getVerticalScrollBar().setUnitIncrement(16);

                                // btnThemTc: Nút thao tác thêm TC.
                                JButton btnThemTc = nut("+ Thêm thú cưng", new Color(15, 42, 68));

                                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                                btnThemTc.addActionListener(e -> themDongThuCung());

                                them(pnlBody, Box.createVerticalStrut(14));
                                them(pnlBody, muc("Thú cưng"));
                                them(pnlBody, Box.createVerticalStrut(6));
                                them(pnlBody, scrSp);
                                them(pnlBody, Box.createVerticalStrut(6));
                                them(pnlBody, btnThemTc);
                        }

                        // pnlTop2: Khung chứa thành phần giao diện trên.
                        JPanel pnlTop2 = new JPanel(new BorderLayout());

                        pnlTop2.setOpaque(false);
                        pnlTop2.add(pnlBody, BorderLayout.NORTH);

                        // ---------------- nút ----------------
                        // btnHuy: Nút thao tác hủy.
                        JButton btnHuy = nut("Hủy", new Color(138, 144, 153));
                        // btnLuu: Nút thao tác lưu.
                        JButton btnLuu = nut(goc == null ? "Thêm mới" : "Lưu thay đổi", new Color(15, 42, 68));

                        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                        btnHuy.addActionListener(e -> dispose());
                        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                        btnLuu.addActionListener(e -> luu());

                        // pnlHangNut: Khung chứa thành phần giao diện hàng nút.
                        JPanel pnlHangNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

                        pnlHangNut.setOpaque(false);
                        pnlHangNut.setBorder(new EmptyBorder(6, 40, 24, 40));
                        pnlHangNut.add(btnHuy);
                        pnlHangNut.add(btnLuu);

                        // pnlRoot: Khung chứa thành phần giao diện khung gốc.
                        JPanel pnlRoot = new JPanel(new BorderLayout());

                        pnlRoot.setBackground(Color.WHITE);
                        pnlRoot.add(pnlTop2, BorderLayout.CENTER);
                        pnlRoot.add(pnlHangNut, BorderLayout.SOUTH);

                        setContentPane(pnlRoot);

                        // ---- đổ dữ liệu khi sửa ----
                        if (goc != null) {

                                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                                txtTen.setText(goc.getHoTen());
                                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                                txtSdt.setText(goc.getSoDienThoai());
                        }
                }

                // Kiểm tra dữ liệu đã được lưu.
                // Kết quả: true/false theo điều kiện xử lý.
                public boolean isDaLuu() {

                        return daLuu;
                }

                // Trả về mã bản ghi vừa lưu của đối tượng.
                public String getMaDaLuu() {

                        return maDaLuu;
                }

                // ---------------- thành phần giao diện ----------------

                // Tạo một cột gồm nhãn và trường nhập.
                // Tham số nhan: nhãn.
                // Tham số cmbC: cột/thành phần được xử lý.
                // Kết quả: đối tượng/giá trị kiểu JPanel.
                private static JPanel cot(String nhan, JComponent c) {

                        // pnlKhung2: Khung dùng để nhóm và bố trí các thành phần giao diện.
                        JPanel pnlKhung2 = new JPanel(new BorderLayout(0, 4));

                        pnlKhung2.setOpaque(false);

                        // lblNhan3: Nhãn dùng để hiển thị chữ hoặc giá trị trên giao diện.
                        JLabel lblNhan3 = new JLabel(nhan);

                        lblNhan3.setFont(NHAN);
                        lblNhan3.setForeground(XAM);

                        pnlKhung2.add(lblNhan3, BorderLayout.NORTH);
                        pnlKhung2.add(c, BorderLayout.CENTER);

                        return pnlKhung2;
                }

                // Ghép hai thành phần thành một hàng hai cột.
                // Tham số a: a.
                // Tham số btnThaoTac2: btnThaoTac2.
                // Kết quả: đối tượng/giá trị kiểu JPanel.
                private static JPanel hai(JComponent a, JComponent b) {

                        // pnlKhung2: Khung dùng để nhóm và bố trí các thành phần giao diện.
                        JPanel pnlKhung2 = new JPanel(new GridLayout(1, 2, 20, 0));

                        pnlKhung2.setOpaque(false);

                        pnlKhung2.add(a);
                        pnlKhung2.add(b);

                        return pnlKhung2;
                }

                // Tạo nhãn tiêu đề cho một mục của biểu mẫu.
                // Tham số text: nội dung chữ.
                // Kết quả: đối tượng/giá trị kiểu JLabel.
                private static JLabel muc(String text) {

                        // lblNhan3: Nhãn dùng để hiển thị chữ hoặc giá trị trên giao diện.
                        JLabel lblNhan3 = new JLabel(text);

                        lblNhan3.setFont(new Font("Segoe UI", Font.BOLD, 15));
                        lblNhan3.setForeground(new Color(20, 30, 45));

                        return lblNhan3;
                }

                // Áp dụng kiểu hiển thị chung cho ô nhập văn bản.
                // Tham số txtVanBan: bảng/thành phần được xử lý.
                private static void kieu(JTextField txtVanBan) {

                        txtVanBan.setFont(F14);

                        txtVanBan.setBorder(new CompoundBorder(
                                        new LineBorder(VIEN, 1),
                                        new EmptyBorder(6, 10, 6, 10)));

                        txtVanBan.setPreferredSize(new Dimension(100, 36));
                }

                // Áp dụng kiểu hiển thị chung cho hộp lựa chọn.
                // Tham số cmbC: cột/thành phần được xử lý.
                private static void kieuCbo(JComboBox<?> cmbC) {

                        cmbC.setFont(F14);
                        cmbC.setBackground(Color.WHITE);
                        cmbC.setPreferredSize(new Dimension(100, 36));
                }

                // Tạo nút thao tác với nội dung và màu sắc được chỉ định.
                // Tham số text: nội dung chữ.
                // Tham số nen: màu nền.
                // Kết quả: đối tượng/giá trị kiểu JButton.
                private static JButton nut(String text, Color nen) {

                        // btnThaoTac2: Nút nhận thao tác nhấn của người dùng.
                        JButton btnThaoTac2 = new JButton(text);

                        btnThaoTac2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        btnThaoTac2.setForeground(Color.WHITE);
                        btnThaoTac2.setBackground(nen);
                        btnThaoTac2.setOpaque(true);
                        btnThaoTac2.setBorderPainted(false);
                        btnThaoTac2.setFocusPainted(false);
                        btnThaoTac2.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        btnThaoTac2.setPreferredSize(
                                        new Dimension(Math.max(110, btnThaoTac2.getPreferredSize().width + 10), 38));

                        return btnThaoTac2;
                }

                // Thêm thành phần vào khung giao diện và bố trí vị trí hiển thị.
                // Tham số pnlKhung2: pnlKhung2.
                // Tham số cmbC: cột/thành phần được xử lý.
                private static void them(JPanel pnlKhung2, Component c) {

                        if (c instanceof JComponent) {
                                ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
                        }

                        pnlKhung2.add(c);
                }

                // ---------------- dòng thú cưng (chỉ khi thêm mới) ----------------

                // Khung giao diện dành cho thú cưng dòng.
                private class PetRow extends JPanel {

                        // txtTen2: Ô nhập/hiển thị văn bản tên.
                        final JTextField txtTen2 = new JTextField();
                        // txtGiong2: Ô nhập/hiển thị văn bản giống thú cưng.
                        final JTextField txtGiong2 = new JTextField();
                        // txtCanNang2: Ô nhập/hiển thị văn bản cân nặng.
                        final JTextField txtCanNang2 = new JTextField();
                        // cmbLoai: Hộp lựa chọn loài/loại.
                        final JComboBox<String> cmbLoai = new JComboBox<>(LOAI);
                        // cmbGioiTinh: Hộp lựa chọn giới tính.
                        final JComboBox<String> cmbGioiTinh = new JComboBox<>(GIOI_TINH);

                        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
                        PetRow() {

                                super(new GridLayout(1, 6, 8, 0));

                                setOpaque(false);

                                setAlignmentX(Component.LEFT_ALIGNMENT);

                                setBorder(new EmptyBorder(4, 6, 4, 6));

                                kieu(txtTen2);
                                kieu(txtGiong2);
                                kieu(txtCanNang2);

                                kieuCbo(cmbLoai);
                                kieuCbo(cmbGioiTinh);

                                // btnXoa2: Nút thao tác xóa.
                                JButton btnXoa2 = nut("Xóa", new Color(210, 70, 70));

                                btnXoa2.setPreferredSize(new Dimension(60, 36));

                                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                                btnXoa2.addActionListener(e -> {
                                        pnlPetsBox.remove(this);
                                        rows.remove(this);
                                        pnlPetsBox.revalidate();
                                        pnlPetsBox.repaint();
                                });

                                // pnlXp: Khung dùng để nhóm và bố trí các thành phần giao diện.
                                JPanel pnlXp = new JPanel(new BorderLayout(0, 4));

                                pnlXp.setOpaque(false);

                                pnlXp.add(new JLabel(" "), BorderLayout.NORTH);
                                pnlXp.add(btnXoa2, BorderLayout.CENTER);

                                add(cot("Tên: *", txtTen2));
                                add(cot("Loại:", cmbLoai));
                                add(cot("Giống:", txtGiong2));
                                add(cot("Giới tính:", cmbGioiTinh));
                                add(cot("Cân nặng (kg):", txtCanNang2));
                                add(pnlXp);
                        }
                }

                // Thêm dòng thú cưng.
                private void themDongThuCung() {

                        PetRow pnlKhung3 = new PetRow();

                        rows.add(pnlKhung3);

                        pnlPetsBox.add(pnlKhung3);

                        pnlPetsBox.revalidate();
                        pnlPetsBox.repaint();
                }

                // ---------------- lưu ----------------

                // Hiển thị thông báo lỗi từ ngoại lệ nhận được.
                // Tham số msg: thông báo.
                private void loi(String msg) {

                        JOptionPane.showMessageDialog(this, msg, "Dữ liệu chưa hợp lệ",
                                        JOptionPane.WARNING_MESSAGE);
                }

                // Kiểm tra dữ liệu nhập và lưu thông tin của biểu mẫu.
                private void luu() {

                        // txtTen2: Đọc nội dung chữ từ ô nhập để xử lý tên.
                        String ten = txtTen.getText().trim();
                        // sdt: Đọc nội dung chữ từ ô nhập để xử lý số điện thoại.
                        String sdt = txtSdt.getText().replaceAll("\\s", "");

                        if (ten.isEmpty() || ten.length() > 50) {
                                loi("Họ tên bắt buộc nhập (tối đa 50 ký tự).");
                                return;
                        }

                        if (!sdt.matches("\\d{10,11}")) {
                                loi("Số điện thoại phải gồm 10 hoặc 11 chữ số.");
                                return;
                        }

                        boolean trung = goc == null
                                        ? khachHangDAO.tonTaiSdt(sdt)
                                        : chiTietDAO.tonTaiSdtKhac(sdt, goc.getMaKhachHang());

                        if (trung) {
                                loi("Số điện thoại " + sdt + " đã thuộc về khách hàng khác.");
                                return;
                        }

                        KhachHang kh = new KhachHang(goc == null ? null : goc.getMaKhachHang(), ten, sdt,
                                        null, null, 0);

                        // ---------- SỬA ----------
                        if (goc != null) {

                                if (!chiTietDAO.sua(kh)) {
                                        loi("Lưu thất bại. Xem lỗi chi tiết trong Console.");
                                        return;
                                }

                                JOptionPane.showMessageDialog(this,
                                                "Đã cập nhật khách hàng " + goc.getMaKhachHang() + ".");

                                daLuu = true;
                                maDaLuu = goc.getMaKhachHang();

                                dispose();

                                return;
                        }

                        // ---------- THÊM ----------
                        // pnlPets: Lưu thú cưng; phần tử có kiểu KhachHangDAO.ThuCungMoi.
                        ArrayList<KhachHangDAO.ThuCungMoi> pets = new ArrayList<>();

                        for (PetRow pnlKhung3 : rows) {

                                // tenTC: Đọc nội dung chữ từ ô nhập để xử lý tên TC.
                                String tenTC = pnlKhung3.txtTen2.getText().trim();
                                // txtGiong2: Đọc nội dung chữ từ ô nhập để xử lý giống thú cưng.
                                String giong = pnlKhung3.txtGiong2.getText().trim();

                                if (tenTC.isEmpty() || tenTC.length() > 50) {
                                        loi("Mỗi thú cưng cần có tên (bấm Xóa nếu không dùng).");
                                        return;
                                }

                                if (giong.length() > 25) {
                                        loi("Giống thú cưng tối đa 25 ký tự.");
                                        return;
                                }

                                Double kg = null;

                                // s: Đọc nội dung chữ từ ô nhập để xử lý chuỗi đầu vào.
                                String s = pnlKhung3.txtCanNang2.getText().trim().replace(',', '.');

                                if (!s.isEmpty()) {

                                        try {
                                                kg = Double.parseDouble(s);
                                        } catch (NumberFormatException ex) {
                                                loi("Cân nặng phải là số, ví dụ 4.5");
                                                return;
                                        }

                                        if (kg < 0 || kg >= 1000) {
                                                loi("Cân nặng phải nhỏ hơn 1000 kg.");
                                                return;
                                        }
                                }

                                pets.add(new KhachHangDAO.ThuCungMoi(tenTC,
                                                (String) pnlKhung3.cmbLoai.getSelectedItem(),
                                                giong, (String) pnlKhung3.cmbGioiTinh.getSelectedItem(), kg));
                        }

                        // ma: Lưu mã.
                        String ma = khachHangDAO.them(kh, pets);

                        if (ma == null) {
                                loi("Lưu thất bại. Xem lỗi chi tiết trong Console.");
                                return;
                        }

                        JOptionPane.showMessageDialog(this, "Đã thêm khách hàng " + ma + ".");

                        daLuu = true;
                        maDaLuu = ma;

                        dispose();
                }
        }
}
