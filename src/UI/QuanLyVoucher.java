package UI;

import UI.TrangChu.UIStyle;
import dao.VoucherDAO;
import entity.Voucher;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Locale;

// Man Ma giam gia va Voucher: tim kiem, tao / sua / xoa, kich hoat / btnHuy2, lam moi,
// chon 1 dong de xem thong tin voucher
// Giao diện/chức năng quản lý voucher của ứng dụng.
public class QuanLyVoucher extends JPanel {

        // D: Bộ định dạng ngày/giờ theo mẫu dd/MM/yyyy để chuyển giữa dữ liệu thời gian và chuỗi.
        private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // COLS: Mảng lưu các cột.
        private static final String[] COLS = { "Mã", "Tên chương trình", "Điều kiện",
                        "Mức giảm", "Đã dùng", "Hiệu lực", "Trạng thái" };

        // LOC_HIEN_THI: Mảng lưu thị.
        private static final String[] LOC_HIEN_THI = { "Tất cả trạng thái", "Đang chạy", "Sắp chạy",
                        "Hết hạn", "Hết lượt", "Đã hủy" };

        // mã trạng thái do Voucher.getTrangThaiText() trả về
        // LOC_MA: Mảng lưu mã.
        private static final String[] LOC_MA = { "Tat ca", "Dang chay", "Sap chay",
                        "Het han", "Het luot", "Da huy" };

        // voucherDAO: Đối tượng truy cập dữ liệu voucher.
        private final VoucherDAO voucherDAO = new VoucherDAO();

        // txtTim: Ô nhập/hiển thị văn bản tìm.
        private final JTextField txtTim = UIStyle.o(16);
        // cmbTt: Hộp lựa chọn trạng thái.
        private final JComboBox<String> cmbTt = new JComboBox<>(LOC_HIEN_THI);
        // lblDem: Nhãn hiển thị đếm.
        private final JLabel lblDem = UIStyle.nhan("0 voucher");

        // btnKichHoat: Nút thao tác kích hoạt.
        private final JButton btnKichHoat = UIStyle.nutPhu("Kích hoạt voucher");
        // btnHuyVc: Nút thao tác hủy voucher.
        private final JButton btnHuyVc = UIStyle.nutPhu("Hủy voucher");
        // btnDung: Nút thao tác dung.
        private final JButton btnDung = UIStyle.nutPhu("Dừng voucher");

        // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
        private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
                // Không cho phép sửa trực tiếp các ô pnlTrong bảng.
                // Tham số r: chỉ số dòng.
                // Tham số c: chỉ số cột.
                // Kết quả: false, bảng chỉ dùng để xem.
                @Override
                public boolean isCellEditable(int r, int c) {
                        return false;
                }
        };

        // tblDuLieu: Bảng hiển thị các dòng do mô hình dữ liệu cung cấp.
        private final JTable tblDuLieu = new JTable(model);

        // dòng i của bảng <-> dsHienThi.get(i)
        // dsHienThi: Lưu danh sách đang hiển thị; phần tử có kiểu Voucher.
        private ArrayList<Voucher> dsHienThi = new ArrayList<>();

        // ----- khung thông tin chi tiết (hiện khi chọn 1 dòng) -----
        // cardCT: Bộ bố trí cho phép chuyển giữa các màn hình pnlTrong cùng một vùng chứa.
        private final CardLayout cardCT = new CardLayout();
        // pnlKhungCT: Khung chứa thành phần giao diện khung chi tiết.
        private final JPanel pnlKhungCT = new JPanel(cardCT);

        // lblVMa: Nhãn hiển thị mã.
        private final JLabel lblVMa = giaTri();
        // lblVTen: Nhãn hiển thị tên.
        private final JLabel lblVTen = giaTri();
        // lblVGiam: Nhãn hiển thị mức giảm.
        private final JLabel lblVGiam = giaTri();
        // lblVTrangThai: Nhãn hiển thị trạng thái.
        private final JLabel lblVTrangThai = giaTri();
        // lblVTu: Nhãn hiển thị ngày bắt đầu.
        private final JLabel lblVTu = giaTri();
        // lblVDen: Nhãn hiển thị ngày kết thúc.
        private final JLabel lblVDen = giaTri();
        // lblVDaDung: Nhãn hiển thị số lượt đã dùng.
        private final JLabel lblVDaDung = giaTri();
        // lblVConLai: Nhãn hiển thị số lượt còn lại.
        private final JLabel lblVConLai = giaTri();
        // lblVDieuKien: Nhãn hiển thị điều kiện áp dụng.
        private final JLabel lblVDieuKien = new JLabel();

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        public QuanLyVoucher() {

                setLayout(new BorderLayout(0, 14));
                setBackground(UIStyle.LIGHT_BLUE);
                setBorder(new EmptyBorder(18, 24, 18, 24));

                // ---------------- tiêu đề + nút Tạo ----------------
                // lblTieuDe2: Nhãn hiển thị: Mã giảm giá và Voucher.
                JLabel lblTieuDe2 = new JLabel("Mã giảm giá và Voucher");

                lblTieuDe2.setFont(UIStyle.font(Font.BOLD, 26));
                lblTieuDe2.setForeground(new Color(40, 40, 40));

                // btnTao: Nút thao tác tạo.
                JButton btnTao = UIStyle.nutXanh("+  Tạo voucher");

                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnTao.addActionListener(e -> moDialog(null));

                // pnlDau2: Khung chứa thành phần giao diện đầu.
                JPanel pnlDau2 = new JPanel(new BorderLayout());

                pnlDau2.setOpaque(false);
                pnlDau2.add(lblTieuDe2, BorderLayout.WEST);
                pnlDau2.add(btnTao, BorderLayout.EAST);

                // ---------------- hàng tìm kiếm + nút ----------------
                cmbTt.setFont(UIStyle.font(Font.PLAIN, 13));

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
                cmbTt.addActionListener(e -> taiLai(maDangChon()));
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnSua.addActionListener(e -> sua());
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnXoa.addActionListener(e -> xoa());
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnKichHoat.addActionListener(e -> kichHoat());
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnHuyVc.addActionListener(e -> huyVoucher());
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnDung.addActionListener(e -> dungVoucher());
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnLamMoi.addActionListener(e -> lamMoiTatCa());

                // pnlLoc2: Khung chứa thành phần giao diện lọc.
                JPanel pnlLoc2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

                pnlLoc2.setOpaque(false);
                pnlLoc2.add(UIStyle.nhan("Mã / tên:"));
                pnlLoc2.add(txtTim);
                pnlLoc2.add(cmbTt);
                pnlLoc2.add(btnTim);
                pnlLoc2.add(btnSua);
                pnlLoc2.add(btnXoa);
                pnlLoc2.add(btnKichHoat);
                pnlLoc2.add(btnHuyVc);
                pnlLoc2.add(btnDung);
                pnlLoc2.add(btnLamMoi);

                // pnlHangTren: Khung chứa thành phần giao diện hàng trên.
                JPanel pnlHangTren = new JPanel(new BorderLayout(0, 8));

                pnlHangTren.setOpaque(false);
                pnlHangTren.add(pnlLoc2, BorderLayout.NORTH);
                pnlHangTren.add(lblDem, BorderLayout.SOUTH);

                // ---------------- bảng ----------------
                UIStyle.taoBang(tblDuLieu, 3, 4);

                tblDuLieu.getColumnModel().getColumn(6).setCellRenderer(new TrangThaiRenderer());

                // w: Mảng lưu chiều rộng.
                int[] w = { 60, 170, 230, 80, 80, 190, 110 };

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

                // lblGoiY: Nhãn hiển thị: Chọn một voucher pnlTrong bảng để xem thông tin chi tiết..
                JLabel lblGoiY = new JLabel("Chọn một voucher trong bảng để xem thông tin chi tiết.");

                lblGoiY.setFont(UIStyle.font(Font.PLAIN, 13));
                lblGoiY.setForeground(Color.GRAY);

                // pnlTrong: Khung chứa thành phần giao diện pnlTrong.
                JPanel pnlTrong = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));

                pnlTrong.setOpaque(false);
                pnlTrong.add(lblGoiY);

                pnlKhungCT.add(pnlTrong, "TRONG");
                pnlKhungCT.add(taoNoiDungChiTiet(), "CO");
                pnlKhungCT.setPreferredSize(new Dimension(100, 130));

                // pnlChiTiet: Khung chứa thành phần giao diện chi tiết.
                JPanel pnlChiTiet = new JPanel(new BorderLayout(0, 6));

                pnlChiTiet.setOpaque(false);
                pnlChiTiet.setBorder(new javax.swing.border.MatteBorder(1, 0, 0, 0, UIStyle.LINE));

                // lblTdCT: Nhãn hiển thị tiêu đề chi tiết.
                JLabel lblTdCT = UIStyle.nhan("Thông tin voucher");

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
                JPanel pnlLuoi2 = new JPanel(new GridLayout(2, 4, 18, 8));

                pnlLuoi2.setOpaque(false);

                pnlLuoi2.add(cap("Mã voucher", lblVMa));
                pnlLuoi2.add(cap("Tên chương trình", lblVTen));
                pnlLuoi2.add(cap("Mức giảm", lblVGiam));
                pnlLuoi2.add(cap("Trạng thái", lblVTrangThai));
                pnlLuoi2.add(cap("Từ ngày", lblVTu));
                pnlLuoi2.add(cap("Đến ngày", lblVDen));
                pnlLuoi2.add(cap("Đã dùng", lblVDaDung));
                pnlLuoi2.add(cap("Còn lại", lblVConLai));

                lblVDieuKien.setFont(UIStyle.font(Font.PLAIN, 13));

                // dk: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlDk = new JPanel(new BorderLayout(0, 2));

                pnlDk.setOpaque(false);

                // lblNhan3: Nhãn hiển thị: Điều kiện áp dụng.
                JLabel lblNhan3 = new JLabel("Điều kiện áp dụng");

                lblNhan3.setFont(UIStyle.font(Font.PLAIN, 12));
                lblNhan3.setForeground(Color.GRAY);

                pnlDk.add(lblNhan3, BorderLayout.NORTH);
                pnlDk.add(lblVDieuKien, BorderLayout.CENTER);

                // pnlKhung2: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlKhung2 = new JPanel(new BorderLayout(0, 8));

                pnlKhung2.setOpaque(false);
                pnlKhung2.add(pnlLuoi2, BorderLayout.NORTH);
                pnlKhung2.add(pnlDk, BorderLayout.CENTER);

                return pnlKhung2;
        }

        // Tô màu ô trạng thái
        // Quy định cách hiển thị ô hoặc thành phần trạng thái.
        private static class TrangThaiRenderer extends DefaultTableCellRenderer {

                // Thiết lập thành phần dùng để vẽ một ô của bảng.
                // Tham số t: bảng đang hiển thị.
                // Tham số v: giá trị của ô.
                // Tham số sel: trạng thái được chọn.
                // Tham số foc: trạng thái có tiêu điểm.
                // Tham số r: chỉ số dòng.
                // Tham số c: chỉ số cột.
                // Kết quả: đối tượng/giá trị kiểu Component.
                @Override
                public Component getTableCellRendererComponent(
                                JTable tblDuLieu3, Object v, boolean sel, boolean foc, int r, int c) {

                        // lblNhan3: Nhãn dùng để hiển thị chữ hoặc giá trị trên giao diện.
                        JLabel lblNhan3 = (JLabel) super.getTableCellRendererComponent(tblDuLieu3, v, false, false, r, c);

                        // nen: Màu dùng cho giao diện: màu nền.
                        Color nen;
                        // chu: Màu dùng cho giao diện: màu chữ.
                        Color chu;

                        switch (String.valueOf(v)) {
                                case "Đang chạy":
                                        nen = new Color(217, 245, 229);
                                        chu = new Color(12, 107, 64);
                                        break;
                                case "Sắp chạy":
                                        nen = new Color(230, 232, 236);
                                        chu = new Color(51, 51, 51);
                                        break;
                                case "Hết hạn":
                                case "Hết lượt":
                                        nen = new Color(255, 240, 205);
                                        chu = new Color(146, 90, 0);
                                        break;
                                case "Đã hủy":
                                        nen = new Color(255, 217, 217);
                                        chu = new Color(180, 35, 24);
                                        break;
                                default:
                                        nen = Color.WHITE;
                                        chu = Color.BLACK;
                        }

                        lblNhan3.setOpaque(true);
                        lblNhan3.setBackground(nen);
                        lblNhan3.setForeground(chu);
                        lblNhan3.setHorizontalAlignment(SwingConstants.CENTER);

                        return lblNhan3;
                }
        }

        // =========================================================
        // DỮ LIỆU
        // =========================================================

        // Định dạng tỷ lệ phần trăm thành chuỗi hiển thị.
        // Tham số v: giá trị đầu vào.
        // Kết quả: chuỗi kết quả.
        private static String phanTram(double v) {

                return (v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.US, "%.2f", v)) + "%";
        }

        // đổi mã trạng thái (không dấu) sang chữ hiển thị
        // Chuyển mã trạng thái thành tên dễ đọc trên giao diện.
        // Tham số ma: mã.
        // Kết quả: chuỗi kết quả.
        private static String tenTrangThai(String ma) {

                switch (ma) {
                        case "Dang chay":
                                return "Đang chạy";
                        case "Sap chay":
                                return "Sắp chạy";
                        case "Het han":
                                return "Hết hạn";
                        case "Het luot":
                                return "Hết lượt";
                        default:
                                return "Đã hủy";
                }
        }

        // Xác định thông tin hiệu lực của voucher để hiển thị.
        // Tham số v: giá trị đầu vào.
        // Kết quả: chuỗi kết quả.
        private static String hieuLuc(Voucher v) {

                // bd: Lưu bắt đầu dưới dạng dữ liệu thời gian.
                LocalDate bd = v.getNgayBatDau();
                // kt: Lưu kết thúc dưới dạng dữ liệu thời gian.
                LocalDate kt = v.getNgayKetThuc();

                if (bd == null && kt == null) {
                        return "Không giới hạn";
                }

                if (bd == null) {
                        return "Đến " + kt.format(D);
                }

                if (kt == null) {
                        return "Từ " + bd.format(D);
                }

                return bd.format(D) + " - " + kt.format(D);
        }

        // Lấy dòng đang được chọn trên bảng.
        // Kết quả: đối tượng/giá trị kiểu Voucher.
        private Voucher dongDangChon() {

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

                Voucher v = dongDangChon();

                return v == null ? null : v.getMaVoucher();
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

                cmbTt.setSelectedIndex(0);

                tblDuLieu.clearSelection();

                taiLai(null);
        }

        // Nạp bảng theo bộ lọc; chonMa != null thì chọn lại dòng đó nếu còn
        // Tải lại danh sách và chọn lại bản ghi theo mã nếu có.
        // Tham số chonMa: mã cần chọn lại.
        public void taiLai(String chonMa) {

                String loc = LOC_MA[cmbTt.getSelectedIndex()];

                // tatCa: Lưu toàn bộ dữ liệu; phần tử có kiểu Voucher.
                ArrayList<Voucher> tatCa = voucherDAO.getAll(txtTim.getText());

                dsHienThi = new ArrayList<>();

                tblDuLieu.clearSelection();

                model.setRowCount(0);

                for (Voucher v : tatCa) {

                        String tt = v.getTrangThaiText();

                        if (!loc.equals("Tat ca") && !loc.equals(tt)) {
                                continue;
                        }

                        dsHienThi.add(v);

                        // daDung: Lưu số lượt đã dùng.
                        String daDung = v.getSoLuongToiDa() == null
                                        ? String.valueOf(v.getDaDung())
                                        : v.getDaDung() + "/" + v.getSoLuongToiDa();

                        model.addRow(new Object[] {
                                        v.getMaVoucher(),
                                        v.getTenVoucher(),
                                        v.getDieuKien() == null ? "" : v.getDieuKien(),
                                        phanTram(v.getPhanTramGiam()),
                                        daDung,
                                        hieuLuc(v),
                                        tenTrangThai(tt) });
                }

                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblDem.setText(dsHienThi.size() + " voucher");

                if (chonMa != null) {

                        for (int i = 0; i < dsHienThi.size(); i++) {

                                if (dsHienThi.get(i).getMaVoucher().equals(chonMa)) {

                                        tblDuLieu.setRowSelectionInterval(i, i);

                                        tblDuLieu.scrollRectToVisible(tblDuLieu.getCellRect(i, 0, true));

                                        break;
                                }
                        }
                }

                hienThongTin();
        }

        // Hiện thông tin của dòng đang chọn ở khung bên dưới bảng + đổi chữ nút Kích
        // hoạt / Hủy
        // Hiển thị thông tin.
        private void hienThongTin() {

                Voucher v = dongDangChon();

                if (v == null) {

                        btnKichHoat.setEnabled(false);
                        btnHuyVc.setEnabled(false);
                        btnDung.setEnabled(false);

                        cardCT.show(pnlKhungCT, "TRONG");

                        return;
                }

                String ttMa = v.getTrangThaiText();

                btnKichHoat.setEnabled(!ttMa.equals("Dang chay") && !ttMa.equals("Sap chay"));
                btnHuyVc.setEnabled(v.isTrangThai());
                btnDung.setEnabled(ttMa.equals("Dang chay")
                                && (v.getNgayBatDau() == null || v.getNgayBatDau().isBefore(LocalDate.now())));

                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblVMa.setText(v.getMaVoucher());
                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblVTen.setText(v.getTenVoucher());
                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblVGiam.setText(phanTram(v.getPhanTramGiam()));
                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblVTrangThai.setText(tenTrangThai(v.getTrangThaiText()));
                // Định dạng dữ liệu thành chuỗi rồi hiển thị pnlTrong control bằng setText().
                lblVTu.setText(v.getNgayBatDau() == null ? "Không giới hạn" : v.getNgayBatDau().format(D));
                // Định dạng dữ liệu thành chuỗi rồi hiển thị pnlTrong control bằng setText().
                lblVDen.setText(v.getNgayKetThuc() == null ? "Không giới hạn" : v.getNgayKetThuc().format(D));
                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblVDaDung.setText(v.getDaDung() + " lượt");

                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblVConLai.setText(v.getSoLuongToiDa() == null
                                ? "Không giới hạn"
                                : Math.max(0, v.getSoLuongToiDa() - v.getDaDung()) + " lượt");

                String dk = v.getDieuKien() == null || v.getDieuKien().trim().isEmpty()
                                ? "Không có điều kiện."
                                : v.getDieuKien();

                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                lblVDieuKien.setText("<html><body style='width:760px'>" + dk
                                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                                + "</body></html>");

                cardCT.show(pnlKhungCT, "CO");
        }

        // =========================================================
        // TẠO / SỬA / XÓA / KÍCH HOẠT
        // =========================================================

        // Xử lý yêu cầu sửa bản ghi đang chọn.
        private void sua() {

                Voucher v = dongDangChon();

                if (v == null) {

                        UIStyle.canhBao(this, "Hãy chọn một voucher trong bảng để sửa.");

                        return;
                }

                moDialog(v);
        }

        // Xử lý yêu cầu xóa bản ghi đang chọn.
        private void xoa() {

                Voucher v = dongDangChon();

                if (v == null) {

                        UIStyle.canhBao(this, "Hãy chọn một voucher trong bảng để xóa.");

                        return;
                }

                if (!UIStyle.xacNhan(this, "Xóa hẳn voucher " + v.getMaVoucher() + " - " + v.getTenVoucher() + "?")) {
                        return;
                }

                String loi = voucherDAO.xoa(v.getMaVoucher());

                if (loi != null) {

                        UIStyle.canhBao(this, loi);

                        return;
                }

                taiLai(null);
        }

        // Kích hoạt: voucher đã hủy / hết hạn / hết lượt -> chạy lại
        // (ngày bắt đầu ở tương lai thì thành "Sắp chạy", còn lại là "Đang chạy")
        // Kích hoạt dữ liệu của màn hình.
        private void kichHoat() {

                Voucher v = dongDangChon();

                if (v == null) {

                        UIStyle.canhBao(this, "Hãy chọn một voucher trong bảng.");

                        return;
                }

                String tt = v.getTrangThaiText();

                if (tt.equals("Dang chay") || tt.equals("Sap chay")) {

                        UIStyle.canhBao(this, "Voucher này đang được kích hoạt rồi.");

                        return;
                }

                // btnHomNay2: Lưu hôm nay dưới dạng dữ liệu thời gian.
                LocalDate homNay = LocalDate.now();

                // hết hạn hoặc hết lượt thì kích hoạt cũng không chạy được -> mời người dùng
                // sửa
                boolean hetHan = v.getNgayKetThuc() != null && v.getNgayKetThuc().isBefore(homNay);
                boolean hetLuot = v.getSoLuongToiDa() != null && v.getDaDung() >= v.getSoLuongToiDa();

                if (hetHan || hetLuot) {

                        String lyDo = hetHan
                                        ? "đã quá ngày kết thúc (" + v.getNgayKetThuc().format(D) + ")"
                                        : "đã dùng hết lượt (" + v.getDaDung() + "/" + v.getSoLuongToiDa() + ")";

                        if (UIStyle.xacNhan(this, "Voucher " + v.getMaVoucher() + " " + lyDo
                                        + " nên chưa thể chạy.\nBạn có muốn mở form sửa để gia hạn / tăng lượt không?")) {
                                moDialog(v);
                        }

                        return;
                }

                if (!UIStyle.xacNhan(this, "Kích hoạt voucher " + v.getMaVoucher() + "?")) {
                        return;
                }

                if (!voucherDAO.doiTrangThai(v.getMaVoucher(), true)) {

                        UIStyle.canhBao(this, "Cập nhật thất bại. Xem lỗi chi tiết trong Console.");

                        return;
                }

                taiLai(v.getMaVoucher());
        }

        // Hủy: chuyển sang "Đã hủy", vẫn giữ pnlTrong danh sách, có thể kích hoạt lại
        // Hủy voucher.
        private void huyVoucher() {

                Voucher v = dongDangChon();

                if (v == null) {

                        UIStyle.canhBao(this, "Hãy chọn một voucher trong bảng.");

                        return;
                }

                if (!v.isTrangThai()) {

                        UIStyle.canhBao(this, "Voucher này đã hủy rồi.");

                        return;
                }

                if (!UIStyle.xacNhan(this, "Hủy voucher " + v.getMaVoucher()
                                + "? (Vẫn giữ trong danh sách, có thể kích hoạt lại)")) {
                        return;
                }

                if (!voucherDAO.doiTrangThai(v.getMaVoucher(), false)) {

                        UIStyle.canhBao(this, "Cập nhật thất bại. Xem lỗi chi tiết trong Console.");

                        return;
                }

                taiLai(v.getMaVoucher());
        }

        // Dừng: voucher đang chạy kết thúc ngay (ngày kết thúc = hôm qua -> "Hết hạn")
        // Dừng voucher.
        private void dungVoucher() {

                Voucher v = dongDangChon();

                if (v == null) {

                        UIStyle.canhBao(this, "Hãy chọn một voucher trong bảng.");

                        return;
                }

                String tt = v.getTrangThaiText();

                if (!tt.equals("Dang chay")) {

                        UIStyle.canhBao(this, tt.equals("Sap chay")
                                        ? "Voucher chưa chạy nên không cần dừng. Hãy dùng \"Hủy voucher\"."
                                        : "Chỉ có thể dừng voucher đang chạy.");

                        return;
                }

                // ketThuc: Lưu kết thực dưới dạng dữ liệu thời gian.
                LocalDate ketThuc = LocalDate.now().minusDays(1);

                if (v.getNgayBatDau() != null && ketThuc.isBefore(v.getNgayBatDau())) {

                        UIStyle.canhBao(this, "Voucher mới bắt đầu từ hôm nay nên không thể dừng theo ngày.\n"
                                        + "Hãy dùng \"Hủy voucher\".");

                        return;
                }

                if (!UIStyle.xacNhan(this, "Dừng voucher " + v.getMaVoucher() + " - " + v.getTenVoucher()
                                + "?\nVoucher sẽ kết thúc ngay (chuyển sang \"Hết hạn\"). "
                                + "Muốn chạy lại, hãy sửa ngày kết thúc rồi kích hoạt.")) {
                        return;
                }

                Voucher moi = new Voucher(
                                v.getMaVoucher(), v.getTenVoucher(), v.getPhanTramGiam(),
                                v.getDieuKien(), v.isTrangThai(), v.getNgayBatDau(), ketThuc, v.getSoLuongToiDa());

                if (!voucherDAO.sua(moi)) {

                        UIStyle.canhBao(this, "Cập nhật thất bại. Xem lỗi chi tiết trong Console.");

                        return;
                }

                taiLai(v.getMaVoucher());
        }

        // goc = null -> tạo mới; goc != null -> sửa voucher đó
        // Mở hộp thoại.
        // Tham số goc: đối tượng gốc trước khi chỉnh sửa.
        private void moDialog(Voucher goc) {

                VoucherDialog winDlg = new VoucherDialog(SwingUtilities.getWindowAncestor(this), goc, voucherDAO);

                winDlg.setVisible(true);

                if (winDlg.isDaLuu()) {
                        taiLai(winDlg.getMaDaLuu());
                }
        }

        // =========================================================
        // HỘP THOẠI TẠO / SỬA VOUCHER (class lồng)
        // =========================================================

        // Hộp thoại nhập hoặc chỉnh sửa voucher.
        public static class VoucherDialog extends JDialog {

                // VIEN: Màu dùng cho giao diện: viên.
                private static final Color VIEN = new Color(110, 130, 160);
                // XAM: Màu dùng cho giao diện: xám.
                private static final Color XAM = new Color(110, 120, 135);
                // F14: Phông chữ dùng cho 14.
                private static final Font F14 = new Font("Segoe UI", Font.PLAIN, 14);
                // NHAN: Phông chữ dùng cho nhân.
                private static final Font NHAN = new Font("Segoe UI", Font.BOLD, 12);

                // voucherDAO: Đối tượng truy cập dữ liệu voucher.
                private final VoucherDAO voucherDAO;
                // goc: Đối tượng đối tượng gốc trước khi chỉnh sửa có kiểu Voucher.
                private final Voucher goc;

                // daLuu: Cờ cho biết dữ liệu đã được lưu.
                private boolean daLuu = false;
                // maDaLuu: Lưu mã bản ghi vừa lưu.
                private String maDaLuu = null;

                // txtTen: Ô nhập/hiển thị văn bản tên.
                private final JTextField txtTen = new JTextField();
                // txtPt: Ô nhập/hiển thị văn bản phần trăm.
                private final JTextField txtPt = new JTextField();
                // txtBd: Ô nhập/hiển thị văn bản bắt đầu.
                private final JTextField txtBd = new JTextField();
                // txtKt: Ô nhập/hiển thị văn bản kết thúc.
                private final JTextField txtKt = new JTextField();
                // txtSl: Ô nhập/hiển thị văn bản số lượng.
                private final JTextField txtSl = new JTextField();
                // cmbTt: Hộp lựa chọn trạng thái.
                private final JComboBox<String> cmbTt = new JComboBox<>(
                                new String[] { "Đang kích hoạt", "Sắp chạy", "Đã hủy" });
                // txaDk: Vùng văn bản nhiều dòng điều kiện.
                private final JTextArea txaDk = new JTextArea(3, 30);

                // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
                // Tham số winOwner: cửa sổ sở hữu.
                // Tham số goc: đối tượng gốc trước khi chỉnh sửa.
                // Tham số voucherDAO: đối tượng truy cập dữ liệu voucher.
                public VoucherDialog(Window winOwner, Voucher goc, VoucherDAO voucherDAO) {

                        super(winOwner, goc == null ? "Tạo Voucher" : "Sửa Voucher",
                                        Dialog.ModalityType.APPLICATION_MODAL);

                        this.goc = goc;
                        this.voucherDAO = voucherDAO;

                        setSize(760, 600);
                        setResizable(false);
                        setLocationRelativeTo(winOwner);

                        kieu(txtTen);
                        kieu(txtPt);
                        kieu(txtBd);
                        kieu(txtKt);
                        kieu(txtSl);

                        cmbTt.setFont(F14);
                        cmbTt.setBackground(Color.WHITE);
                        cmbTt.setPreferredSize(new Dimension(100, 36));

                        txaDk.setFont(F14);
                        txaDk.setLineWrap(true);
                        txaDk.setWrapStyleWord(true);
                        txaDk.setBorder(new EmptyBorder(6, 10, 6, 10));

                        // dk: Khung cuộn điều kiện.
                        JScrollPane scrDk = new JScrollPane(txaDk);

                        scrDk.setBorder(new LineBorder(VIEN, 1));
                        scrDk.setPreferredSize(new Dimension(100, 80));

                        // lblGhiChu: Nhãn hiển thị: Để trống ngày / số lượt = không giới hạn..
                        JLabel lblGhiChu = new JLabel("Để trống ngày / số lượt = không giới hạn.");

                        lblGhiChu.setFont(NHAN);
                        lblGhiChu.setForeground(XAM);

                        // pnlBody: Khung chứa thành phần giao diện nội dung.
                        JPanel pnlBody = new JPanel();

                        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
                        pnlBody.setOpaque(false);
                        pnlBody.setBorder(new EmptyBorder(24, 40, 10, 40));

                        them(pnlBody, muc(goc == null ? "Thông tin Voucher"
                                        : "Thông tin Voucher (" + goc.getMaVoucher() + ")"));
                        them(pnlBody, Box.createVerticalStrut(8));
                        them(pnlBody, hai(cot("Tên chương trình: *", txtTen), cot("Mức giảm (%): *", txtPt)));
                        them(pnlBody, Box.createVerticalStrut(8));
                        them(pnlBody, cot("Điều kiện áp dụng:", scrDk));
                        them(pnlBody, Box.createVerticalStrut(14));
                        them(pnlBody, muc("Hiệu lực & Giới hạn"));
                        them(pnlBody, Box.createVerticalStrut(8));
                        them(pnlBody, hai(cot("Từ ngày: (dd/MM/yyyy)", oChonNgay(txtBd)),
                                        cot("Đến ngày: (dd/MM/yyyy)", oChonNgay(txtKt))));
                        them(pnlBody, Box.createVerticalStrut(8));
                        them(pnlBody, hai(cot("Số lượt tối đa:", txtSl), cot("Trạng thái:", cmbTt)));
                        them(pnlBody, Box.createVerticalStrut(6));
                        them(pnlBody, lblGhiChu);

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
                                txtTen.setText(goc.getTenVoucher());

                                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                                txtPt.setText(phanTram(goc.getPhanTramGiam()).replace("%", ""));

                                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                                txaDk.setText(goc.getDieuKien() == null ? "" : goc.getDieuKien());

                                // Định dạng dữ liệu thành chuỗi rồi hiển thị pnlTrong control bằng setText().
                                txtBd.setText(goc.getNgayBatDau() == null ? "" : goc.getNgayBatDau().format(D));

                                // Định dạng dữ liệu thành chuỗi rồi hiển thị pnlTrong control bằng setText().
                                txtKt.setText(goc.getNgayKetThuc() == null ? "" : goc.getNgayKetThuc().format(D));

                                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                                txtSl.setText(goc.getSoLuongToiDa() == null ? ""
                                                : String.valueOf(goc.getSoLuongToiDa()));

                                cmbTt.setSelectedIndex(!goc.isTrangThai() ? 2
                                                : goc.getTrangThaiText().equals("Sap chay") ? 1 : 0);
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
                // Tham số c: cột/thành phần được xử lý.
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
                // Tham số t: bảng/thành phần được xử lý.
                private static void kieu(JTextField txtVanBan) {

                        txtVanBan.setFont(F14);

                        txtVanBan.setBorder(new CompoundBorder(
                                        new LineBorder(VIEN, 1),
                                        new EmptyBorder(6, 10, 6, 10)));

                        txtVanBan.setPreferredSize(new Dimension(100, 36));
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
                        btnThaoTac2.setPreferredSize(new Dimension(Math.max(110, btnThaoTac2.getPreferredSize().width + 10), 38));

                        return btnThaoTac2;
                }

                // Thêm thành phần vào khung giao diện và bố trí vị trí hiển thị.
                // Tham số pnlKhung2: pnlKhung2.
                // Tham số c: cột/thành phần được xử lý.
                private static void them(JPanel pnlKhung2, Component c) {

                        if (c instanceof JComponent) {
                                ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
                        }

                        pnlKhung2.add(c);
                }

                // ---------------- ô ngày + nút Lịch ----------------

                // Ghép ô nhập ngày với nút "Lịch" bên cạnh; bấm nút mở bảng chọn ngày
                // Tạo vùng chọn ngày gắn với ô văn bản.
                // Tham số txtTxt: ô văn bản.
                // Kết quả: đối tượng/giá trị kiểu JPanel.
                private JPanel oChonNgay(JTextField txtTxt) {

                        // Chỉ cho chọn ngày qua lịch; không cho nhập/gõ/dán trực tiếp.
                        // setText(...) vẫn hoạt động khi sửa voucher hoặc chọn ngày từ lịch.
                        txtTxt.setEditable(false);
                        txtTxt.setFocusable(false);
                        txtTxt.setBackground(new Color(245, 247, 250));
                        txtTxt.setToolTipText("Nhấn nút Lịch để chọn ngày (dd/MM/yyyy)");

                        // btnLich: Nút thao tác lịch.
                        JButton btnLich = nut("Lịch", new Color(15, 42, 68));

                        btnLich.setPreferredSize(new Dimension(70, 36));

                        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                        btnLich.addActionListener(e -> {

                                // hienTai: Lưu hiện tại dưới dạng dữ liệu thời gian.
                                LocalDate hienTai = null;

                                try {
                                        // t: Đọc nội dung chữ từ ô nhập để xử lý bảng/thành phần được xử lý.
                                        String t = txtTxt.getText().trim();

                                        if (!t.isEmpty()) {
                                                // Phân tích chuỗi ngày thành LocalDate theo bộ định dạng được chỉ định.
                                                hienTai = LocalDate.parse(t, D);
                                        }
                                } catch (DateTimeParseException ex) {
                                        hienTai = null;
                                }

                                LichDialog winLich = new LichDialog(this, hienTai);

                                // hiện ngay bên cạnh nút Lịch, không để tràn màn hình
                                Point p = btnLich.getLocationOnScreen();
                                Rectangle mh = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                .getMaximumWindowBounds();

                                // x: Lưu tọa độ ngang.
                                int x = p.x + btnLich.getWidth() + 10;
                                // y: Lưu tọa độ dọc.
                                int y = p.y - 10;

                                if (x + winLich.getWidth() > mh.x + mh.width) {
                                        x = p.x - winLich.getWidth() - 10;
                                }

                                if (y + winLich.getHeight() > mh.y + mh.height) {
                                        y = mh.y + mh.height - winLich.getHeight();
                                }

                                winLich.setLocation(Math.max(mh.x, x), Math.max(mh.y, y));
                                winLich.setVisible(true);

                                if (winLich.getKetQua() != null) {
                                        // Định dạng dữ liệu thành chuỗi rồi hiển thị pnlTrong control bằng setText().
                                        txtTxt.setText(winLich.getKetQua().format(D));
                                }
                        });

                        // pnlKhung2: Khung dùng để nhóm và bố trí các thành phần giao diện.
                        JPanel pnlKhung2 = new JPanel(new BorderLayout(6, 0));

                        pnlKhung2.setOpaque(false);
                        pnlKhung2.add(txtTxt, BorderLayout.CENTER);
                        pnlKhung2.add(btnLich, BorderLayout.EAST);

                        return pnlKhung2;
                }

                // ---------------- bảng chọn ngày (Chọn ngày) ----------------

                // Hộp thoại nhập hoặc chỉnh sửa lịch.
                private static class LichDialog extends JDialog {

                        // NAVY: Màu dùng cho giao diện: xanh đậm.
                        private static final Color NAVY = new Color(15, 42, 68);
                        // XANH_NHAT: Màu dùng cho giao diện: nhất.
                        private static final Color XANH_NHAT = new Color(222, 236, 250);
                        // VIEN_O: Màu dùng cho giao diện: ô.
                        private static final Color VIEN_O = new Color(200, 205, 212);
                        // THU: Mảng lưu thú.
                        private static final String[] THU = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };

                        // dangChon: Lưu mục đang chọn dưới dạng dữ liệu thời gian.
                        private final LocalDate dangChon;
                        // thang: Lưu tháng đang hiển thị dưới dạng dữ liệu thời gian.
                        private YearMonth thang;
                        // ketQua: Lưu kết quả dưới dạng dữ liệu thời gian.
                        private LocalDate ketQua = null;

                        // lblThang: Nhãn hiển thị tháng đang hiển thị.
                        private final JLabel lblThang = new JLabel("", SwingConstants.CENTER);
                        // pnlLuoi2: Khung chứa thành phần giao diện lưới ngày.
                        private final JPanel pnlLuoi2 = new JPanel(new GridLayout(7, 7, 6, 6));

                        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
                        // Tham số cha: thành phần cha.
                        // Tham số batDau: bắt đầu.
                        LichDialog(Component cha, LocalDate batDau) {

                                super(SwingUtilities.getWindowAncestor(cha), "Chọn ngày",
                                                Dialog.ModalityType.APPLICATION_MODAL);

                                this.dangChon = batDau;
                                this.thang = YearMonth.from(batDau != null ? batDau : LocalDate.now());

                                setResizable(false);

                                lblThang.setFont(new Font("Segoe UI", Font.BOLD, 16));
                                lblThang.setForeground(NAVY);

                                // btnTruoc2: Nút thao tác trước.
                                JButton btnTruoc2 = nutNhat("◄");
                                // btnSau2: Nút thao tác btnSau2.
                                JButton btnSau2 = nutNhat("►");

                                btnTruoc2.setPreferredSize(new Dimension(70, 34));
                                btnSau2.setPreferredSize(new Dimension(70, 34));

                                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                                btnTruoc2.addActionListener(e -> {
                                        thang = thang.minusMonths(1);
                                        veLich();
                                });

                                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                                btnSau2.addActionListener(e -> {
                                        thang = thang.plusMonths(1);
                                        veLich();
                                });

                                // pnlDau2: Khung chứa thành phần giao diện đầu.
                                JPanel pnlDau2 = new JPanel(new BorderLayout());

                                pnlDau2.setOpaque(false);
                                pnlDau2.add(btnTruoc2, BorderLayout.WEST);
                                pnlDau2.add(lblThang, BorderLayout.CENTER);
                                pnlDau2.add(btnSau2, BorderLayout.EAST);

                                pnlLuoi2.setOpaque(false);
                                pnlLuoi2.setBorder(new EmptyBorder(14, 0, 10, 0));

                                // btnHomNay2: Nút thao tác hôm nay.
                                JButton btnHomNay2 = nutNhat("Hôm nay");
                                // btnHuy2: Nút thao tác hủy.
                                JButton btnHuy2 = nutNhat("Hủy");

                                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                                btnHomNay2.addActionListener(e -> {
                                        ketQua = LocalDate.now();
                                        dispose();
                                });

                                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                                btnHuy2.addActionListener(e -> dispose());

                                // pnlChan: Khung chứa thành phần giao diện chặn.
                                JPanel pnlChan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

                                pnlChan.setOpaque(false);
                                pnlChan.add(btnHomNay2);
                                pnlChan.add(btnHuy2);

                                // pnlRoot: Khung chứa thành phần giao diện khung gốc.
                                JPanel pnlRoot = new JPanel(new BorderLayout());

                                pnlRoot.setBackground(Color.WHITE);
                                pnlRoot.setBorder(new EmptyBorder(14, 16, 14, 16));
                                pnlRoot.add(pnlDau2, BorderLayout.NORTH);
                                pnlRoot.add(pnlLuoi2, BorderLayout.CENTER);
                                pnlRoot.add(pnlChan, BorderLayout.SOUTH);

                                setContentPane(pnlRoot);

                                veLich();

                                setSize(360, 400);
                        }

                        // Trả về kết quả của đối tượng.
                        LocalDate getKetQua() {

                                return ketQua;
                        }

                        // Tạo nút với màu nền nhạt dùng pnlTrong lịch chọn ngày.
                        // Tham số text: nội dung chữ.
                        // Kết quả: đối tượng/giá trị kiểu JButton.
                        private static JButton nutNhat(String text) {

                                // btnThaoTac2: Nút nhận thao tác nhấn của người dùng.
                                JButton btnThaoTac2 = new JButton(text);

                                btnThaoTac2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                                btnThaoTac2.setForeground(NAVY);
                                btnThaoTac2.setBackground(XANH_NHAT);
                                btnThaoTac2.setOpaque(true);
                                btnThaoTac2.setFocusPainted(false);
                                btnThaoTac2.setContentAreaFilled(true);
                                btnThaoTac2.setBorder(new LineBorder(new Color(160, 190, 225), 1));
                                btnThaoTac2.setCursor(new Cursor(Cursor.HAND_CURSOR));
                                btnThaoTac2.setPreferredSize(new Dimension(Math.max(80, btnThaoTac2.getPreferredSize().width + 16), 32));

                                return btnThaoTac2;
                        }

                        // Vẽ lịch.
                        private void veLich() {

                                // Cập nhật nội dung chữ hiển thị pnlTrong control bằng setText().
                                lblThang.setText("Tháng " + thang.getMonthValue() + " / " + thang.getYear());

                                pnlLuoi2.removeAll();

                                // hàng tiêu đề thứ
                                for (String t : THU) {

                                        // lblNhan3: Nhãn dùng để hiển thị chữ hoặc giá trị trên giao diện.
                                        JLabel lblNhan3 = new JLabel(t, SwingConstants.CENTER);

                                        lblNhan3.setFont(new Font("Segoe UI", Font.BOLD, 12));
                                        lblNhan3.setForeground(NAVY);

                                        pnlLuoi2.add(lblNhan3);
                                }

                                // tuần bắt đầu từ T2: T2 = 1 ... CN = 7
                                int lech = thang.atDay(1).getDayOfWeek().getValue() - 1;
                                int soNgay = thang.lengthOfMonth();
                                // btnHomNay2: Lưu hôm nay dưới dạng dữ liệu thời gian.
                                LocalDate homNay = LocalDate.now();

                                for (int i = 0; i < 42; i++) {

                                        int ngay = i - lech + 1;

                                        if (ngay < 1 || ngay > soNgay) {
                                                pnlLuoi2.add(new JLabel());
                                                continue;
                                        }

                                        // d: Lưu d dưới dạng dữ liệu thời gian.
                                        LocalDate d = thang.atDay(ngay);

                                        // btnThaoTac2: Nút nhận thao tác nhấn của người dùng.
                                        JButton btnThaoTac2 = new JButton(String.valueOf(ngay));

                                        btnThaoTac2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                                        btnThaoTac2.setForeground(new Color(40, 40, 40));
                                        btnThaoTac2.setBackground(Color.WHITE);
                                        btnThaoTac2.setOpaque(true);
                                        btnThaoTac2.setFocusPainted(false);
                                        btnThaoTac2.setContentAreaFilled(true);
                                        btnThaoTac2.setBorder(new LineBorder(VIEN_O, 1));
                                        btnThaoTac2.setCursor(new Cursor(Cursor.HAND_CURSOR));

                                        if (d.equals(homNay)) {
                                                btnThaoTac2.setBackground(XANH_NHAT);
                                                btnThaoTac2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                                        }

                                        if (d.equals(dangChon)) {
                                                btnThaoTac2.setBackground(NAVY);
                                                btnThaoTac2.setForeground(Color.WHITE);
                                                btnThaoTac2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                                        }

                                        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                                        btnThaoTac2.addActionListener(e -> {
                                                ketQua = d;
                                                dispose();
                                        });

                                        pnlLuoi2.add(btnThaoTac2);
                                }

                                pnlLuoi2.revalidate();
                                pnlLuoi2.repaint();
                        }
                }

                // ---------------- lưu ----------------

                // Cảnh báo dữ liệu của màn hình.
                // Tham số msg: thông báo.
                private void canhBao(String msg) {

                        JOptionPane.showMessageDialog(this, msg, "Dữ liệu chưa hợp lệ",
                                        JOptionPane.WARNING_MESSAGE);
                }

                // Kiểm tra dữ liệu nhập và lưu thông tin của biểu mẫu.
                private void luu() {

                        // ten: Đọc nội dung chữ từ ô nhập để xử lý tên.
                        String ten = txtTen.getText().trim();

                        if (ten.isEmpty() || ten.length() > 50) {
                                canhBao("Tên chương trình bắt buộc nhập (tối đa 50 ký tự).");
                                return;
                        }

                        double pt;

                        try {
                                pt = Double.parseDouble(txtPt.getText().trim().replace(',', '.'));
                        } catch (NumberFormatException ex) {
                                canhBao("Mức giảm phải là số, ví dụ 10 hoặc 12.5");
                                return;
                        }

                        if (pt <= 0 || pt > 100) {
                                canhBao("Mức giảm phải lớn hơn 0 và không quá 100 (%).");
                                return;
                        }

                        pt = Math.round(pt * 100) / 100.0;

                        // dk: Đọc nội dung chữ từ ô nhập để xử lý điều kiện.
                        String dk = txaDk.getText().trim();

                        if (dk.length() > 255) {
                                canhBao("Điều kiện tối đa 255 ký tự.");
                                return;
                        }

                        // bd: Lưu bắt đầu dưới dạng dữ liệu thời gian.
                        LocalDate bd;
                        // kt: Lưu kết thúc dưới dạng dữ liệu thời gian.
                        LocalDate kt;

                        try {

                                bd = txtBd.getText().trim().isEmpty() ? null
                                                // Phân tích chuỗi ngày thành LocalDate theo bộ định dạng được chỉ định.
                                                : LocalDate.parse(txtBd.getText().trim(), D);

                                kt = txtKt.getText().trim().isEmpty() ? null
                                                // Phân tích chuỗi ngày thành LocalDate theo bộ định dạng được chỉ định.
                                                : LocalDate.parse(txtKt.getText().trim(), D);

                        } catch (DateTimeParseException ex) {

                                canhBao("Ngày phải có dạng dd/MM/yyyy, ví dụ 01/11/2026.");

                                return;
                        }

                        if (bd != null && kt != null && kt.isBefore(bd)) {
                                canhBao("Ngày kết thúc phải sau ngày bắt đầu.");
                                return;
                        }

                        Integer sl = null;

                        // s: Đọc nội dung chữ từ ô nhập để xử lý chuỗi đầu vào.
                        String s = txtSl.getText().trim();

                        if (!s.isEmpty()) {

                                try {
                                        sl = Integer.parseInt(s);
                                } catch (NumberFormatException ex) {
                                        canhBao("Số lượt tối đa phải là số nguyên.");
                                        return;
                                }

                                if (sl <= 0) {
                                        canhBao("Số lượt tối đa phải lớn hơn 0.");
                                        return;
                                }

                                if (goc != null && sl < goc.getDaDung()) {
                                        canhBao("Voucher đã dùng " + goc.getDaDung()
                                                        + " lượt, số lượt tối đa không thể nhỏ hơn.");
                                        return;
                                }
                        }

                        // "Đang kích hoạt" mà ngày bắt đầu còn ở tương lai -> phải chọn "Sắp chạy"
                        if (cmbTt.getSelectedIndex() == 0 && bd != null && bd.isAfter(LocalDate.now())) {
                                canhBao("Từ ngày còn ở tương lai, hãy chọn trạng thái \"Sắp chạy\".");
                                return;
                        }

                        // "Sắp chạy" -> bắt buộc có ngày bắt đầu ở tương lai
                        if (cmbTt.getSelectedIndex() == 1 && (bd == null || !bd.isAfter(LocalDate.now()))) {
                                canhBao("Trạng thái \"Sắp chạy\" cần có Từ ngày sau hôm nay.");
                                return;
                        }

                        Voucher v = new Voucher(
                                        goc == null ? null : goc.getMaVoucher(),
                                        ten, pt, dk.isEmpty() ? null : dk,
                                        cmbTt.getSelectedIndex() != 2, bd, kt, sl);

                        if (goc == null) {

                                // ma: Lưu mã.
                                String ma = voucherDAO.them(v);

                                if (ma == null) {

                                        JOptionPane.showMessageDialog(this,
                                                        "Lưu thất bại. Xem lỗi chi tiết trong Console.",
                                                        "Lỗi", JOptionPane.ERROR_MESSAGE);

                                        return;
                                }

                                JOptionPane.showMessageDialog(this, "Đã tạo voucher " + ma + ".");

                                maDaLuu = ma;

                        } else {

                                if (!voucherDAO.sua(v)) {

                                        JOptionPane.showMessageDialog(this,
                                                        "Lưu thất bại. Xem lỗi chi tiết trong Console.",
                                                        "Lỗi", JOptionPane.ERROR_MESSAGE);

                                        return;
                                }

                                JOptionPane.showMessageDialog(this, "Đã cập nhật voucher " + goc.getMaVoucher() + ".");

                                maDaLuu = goc.getMaVoucher();
                        }

                        daLuu = true;

                        dispose();
                }
        }
}
