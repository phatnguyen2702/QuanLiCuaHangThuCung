package UI;

import dao.LichPhanCaDAO;
import entity.LichPhanCa;
import util.XlsxExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.table.TableCellRenderer;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/** Phân ca nhân viên: thêm, sửa, xóa, lọc, lịch chọn ngày và xuất Excel. */
public class QuanLyPhanCa extends JPanel implements ActionListener, MouseListener {
        // NAVY: Màu dùng cho giao diện: xanh đậm.
        private static final Color NAVY = new Color(18, 48, 82);
        // LIGHT_BLUE: Màu dùng cho giao diện: xanh dương.
        private static final Color LIGHT_BLUE = new Color(229, 241, 252);
        // BG: Màu dùng cho giao diện: nền.
        private static final Color BG = new Color(245, 247, 250);
        // GREEN: Màu dùng cho giao diện: xanh lá.
        private static final Color GREEN = new Color(72, 196, 62);
        // RED: Màu dùng cho giao diện: đỏ.
        private static final Color RED = new Color(210, 70, 70);
        // BLUE: Màu dùng cho giao diện: xanh dương.
        private static final Color BLUE = new Color(91, 180, 235);
        // DINH_DANG_NGAY: Bộ định dạng ngày/giờ theo mẫu dd/MM/uuuu để chuyển giữa dữ
        // liệu thời gian và chuỗi.
        private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                        .withResolverStyle(ResolverStyle.STRICT);

        // dao: Đối tượng truy cập dữ liệu lịch phân ca.
        private final LichPhanCaDAO dao = new LichPhanCaDAO();
        // fntNoiDung: Phông chữ dùng cho phông chữ nội dung.
        private final Font fntNoiDung = new Font("Segoe UI", Font.PLAIN, 12);
        // txtMaPhanCa: Ô nhập/hiển thị văn bản mã phân ca.
        // txtNgayLamViec: Ô nhập/hiển thị văn bản ngày làm việc.
        // txtHoTen: Ô nhập/hiển thị văn bản họ tên.
        // txtLocNgay: Ô nhập/hiển thị văn bản lọc ngày.
        private JTextField txtMaPhanCa, txtNgayLamViec, txtHoTen, txtLocNgay;
        // cmbNhanVien: Hộp lựa chọn nhân viên.
        // cmbCa: Hộp lựa chọn ca.
        // cmbLocNhanVien: Hộp lựa chọn lọc nhân viên.
        // cmbLocCa: Hộp lựa chọn lọc ca.
        private JComboBox<String> cmbNhanVien, cmbCa, cmbLocNhanVien, cmbLocCa;
        // tblPhanCa: Bảng hiển thị dữ liệu phân ca.
        private JTable tblPhanCa;
        // mdlPhanCa: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
        private DefaultTableModel mdlPhanCa;
        // lblTrangThai: Nhãn hiển thị trạng thái.
        private JLabel lblTrangThai;
        // btnSua: Nút thao tác sửa.
        private JButton btnSua;
        // Các nút chuyển qua xử lý tập trung tại actionPerformed().
        private JButton btnChonNgay, btnXoaLocNgay, btnLoc;
        private JButton btnXoa, btnExcel, btnLamMoiDanhSach;
        private JButton btnXemTuan, btnSaoChepTuan;
        private JButton btnLich, btnThem, btnBoChon;

        // Thống kê ca làm NGÀY HIỆN TẠI, không phụ thuộc các bộ lọc ở bảng bên trái.
        private JLabel lblNgayThongKe, lblSoNhanVienHomNay, lblSoCaHomNay;
        private JButton btnLamMoiThongKe;

        // maDangSua: Lưu mã bản ghi đang sửa.
        private String maDangSua;

        // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
        public QuanLyPhanCa() {
                setLayout(new BorderLayout());
                setBackground(BG);
                add(taoPnlTieuDe(), BorderLayout.NORTH);
                add(taoPnlNoiDung(), BorderLayout.CENTER);
                taiDanhMuc();
                taiDuLieu();
                lamMoiForm();
        }

        // Tạo khung tiêu đề.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel taoPnlTieuDe() {
                // pnlPnl: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlPnl = new JPanel(new BorderLayout());
                pnlPnl.setBackground(BG);
                pnlPnl.setBorder(new EmptyBorder(14, 24, 12, 24));
                // lblTitle: Nhãn hiển thị: PHÂN CA NHÂN VIÊN.
                JLabel lblTitle = new JLabel("PHÂN CA NHÂN VIÊN");
                lblTitle.setForeground(NAVY);
                lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
                pnlPnl.add(lblTitle, BorderLayout.WEST);
                return pnlPnl;
        }

        // Tạo khung nội dung.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel taoPnlNoiDung() {
                // pnlPnl: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlPnl = new JPanel(new BorderLayout(15, 0));
                pnlPnl.setBackground(LIGHT_BLUE);
                pnlPnl.setBorder(new EmptyBorder(12, 18, 15, 18));
                pnlPnl.add(taoPnlDanhSach(), BorderLayout.CENTER);
                pnlPnl.add(taoPnlBenPhai(), BorderLayout.EAST);
                return pnlPnl;
        }

        // Tạo khung danh sách.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel taoPnlDanhSach() {
                // pnlPnl: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlPnl = new JPanel(new BorderLayout(0, 9));
                pnlPnl.setBackground(Color.WHITE);
                pnlPnl.setBorder(new EmptyBorder(10, 10, 10, 10));
                pnlPnl.add(taoPnlLoc(), BorderLayout.NORTH);

                mdlPhanCa = new DefaultTableModel(new String[] {
                                "Mã PC", "Ngày làm việc", "Mã NV", "Họ tên", "Mã ca", "Ca làm"
                }, 0) {
                        // Không cho phép sửa trực tiếp các ô trong bảng.
                        // Tham số row: chỉ số dòng.
                        // Tham số col: chỉ số cột.
                        // Kết quả: false, bảng chỉ dùng để xem.
                        @Override
                        public boolean isCellEditable(int row, int col) {
                                return false;
                        }
                };
                tblPhanCa = new JTable(mdlPhanCa);
                tblPhanCa.setRowHeight(30);
                tblPhanCa.setFont(fntNoiDung);
                tblPhanCa.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                tblPhanCa.addMouseListener(this); // Không thay thế listener chọn dòng hiện tại.
                tblPhanCa.setAutoCreateRowSorter(true);
                tblPhanCa.setSelectionBackground(new Color(207, 222, 235));
                tblPhanCa.setFillsViewportHeight(true);
                tblPhanCa.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
                tblPhanCa.getTableHeader().setBackground(NAVY);
                tblPhanCa.getTableHeader().setForeground(Color.WHITE);
                tblPhanCa.getTableHeader().setOpaque(true);
                tblPhanCa.getTableHeader().setReorderingAllowed(false);
                DefaultTableCellRenderer lblCenter = new DefaultTableCellRenderer();
                lblCenter.setHorizontalAlignment(SwingConstants.CENTER);
                for (int i : new int[] { 0, 1, 2, 4 })
                        tblPhanCa.getColumnModel().getColumn(i).setCellRenderer(lblCenter);
                // doRong: Mảng lưu độ rỗng.
                int[] doRong = { 65, 105, 75, 140, 65, 170 };
                for (int i = 0; i < doRong.length; i++)
                        tblPhanCa.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
                tblPhanCa.getSelectionModel().addListSelectionListener(e -> {
                        if (!e.getValueIsAdjusting() && tblPhanCa.getSelectedRow() >= 0)
                                hienThiForm();
                });

                // scrScr: Khung cuộn scrScr.
                JScrollPane scrScr = new JScrollPane(tblPhanCa);
                scrScr.setBorder(new LineBorder(new Color(220, 225, 230)));
                pnlPnl.add(scrScr, BorderLayout.CENTER);
                pnlPnl.add(taoPnlNutDuoi(), BorderLayout.SOUTH);
                return pnlPnl;
        }

        // Tạo khung bộ lọc.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel taoPnlLoc() {
                // pnlPnl: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 4));
                pnlPnl.setBackground(Color.WHITE);
                txtLocNgay = new JTextField(9);
                txtLocNgay.setEditable(false);
                txtLocNgay.setFont(fntNoiDung);
                txtLocNgay.setToolTipText("Có thể lọc cả các ngày trong quá khứ");
                // btnChonNgay: Nút thao tác chọn ngày.
                btnChonNgay = taoNut("Lịch", NAVY);
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnChonNgay.addActionListener(this);
                // btnXoaLocNgay: Nút thao tác xóa lọc ngày.
                btnXoaLocNgay = taoNut("×", new Color(108, 117, 125));
                btnXoaLocNgay.setToolTipText("Bỏ lọc theo ngày");
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnXoaLocNgay.addActionListener(this);

                cmbLocNhanVien = new JComboBox<>();
                cmbLocNhanVien.setPreferredSize(new Dimension(145, 30));
                cmbLocCa = new JComboBox<>();
                cmbLocCa.setPreferredSize(new Dimension(110, 30));
                // btnLoc: Nút thao tác lọc.
                btnLoc = taoNut("Lọc", GREEN);
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnLoc.addActionListener(this);

                pnlPnl.add(new JLabel("Ngày làm việc"));
                pnlPnl.add(txtLocNgay);
                pnlPnl.add(btnChonNgay);
                pnlPnl.add(btnXoaLocNgay);
                pnlPnl.add(new JLabel("Nhân viên"));
                pnlPnl.add(cmbLocNhanVien);
                pnlPnl.add(new JLabel("Ca"));
                pnlPnl.add(cmbLocCa);
                pnlPnl.add(btnLoc);
                return pnlPnl;
        }

        // Tạo khung nút phía dưới.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel taoPnlNutDuoi() {
                // pnlPnl: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlPnl = new JPanel(new BorderLayout());
                pnlPnl.setBackground(Color.WHITE);
                // pnlTrai: Khung chứa thành phần giao diện trái.
                JPanel pnlTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                pnlTrai.setOpaque(false);
                // btnXoa: Nút thao tác xóa.
                btnXoa = taoNut("Xóa phân ca", RED);
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnXoa.addActionListener(this);
                // btnExcel: Nút thao tác Excel.
                btnExcel = taoNut("Xuất Excel", new Color(29, 133, 82));
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnExcel.addActionListener(this);
                btnXemTuan = taoNut("Xem lịch tuần", NAVY);
                btnXemTuan.addActionListener(this);
                btnXemTuan.setToolTipText("Xem toàn bộ lịch nhân viên theo thứ Hai – Chủ nhật");
                btnSaoChepTuan = taoNut("Sao chép tuần", new Color(80, 112, 156));
                btnSaoChepTuan.addActionListener(this);
                btnSaoChepTuan.setToolTipText("Sao chép lịch từ một tuần sang tuần khác, không ghi đè");
                pnlTrai.add(btnXoa);
                pnlTrai.add(btnExcel);
                pnlTrai.add(btnXemTuan);
                pnlTrai.add(btnSaoChepTuan);
                pnlPnl.add(pnlTrai, BorderLayout.WEST);
                // btnLamMoi: Nút thao tác làm mới.
                btnLamMoiDanhSach = taoNut("↻ Làm mới", BLUE);
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnLamMoiDanhSach.addActionListener(this);
                pnlPnl.add(btnLamMoiDanhSach, BorderLayout.EAST);
                return pnlPnl;
        }

        // Tạo khung bên phải.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel taoPnlBenPhai() {
                // pnlPnl: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlPnl = new JPanel(new BorderLayout(0, 12));
                pnlPnl.setBackground(Color.WHITE);
                pnlPnl.setPreferredSize(new Dimension(310, 0));
                pnlPnl.add(taoPnlForm(), BorderLayout.NORTH);
                pnlPnl.add(taoPnlThongKeHomNay(), BorderLayout.CENTER);
                return pnlPnl;
        }

        // Tạo khung biểu mẫu.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel taoPnlForm() {
                // pnlPnl: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlPnl = new JPanel();
                pnlPnl.setLayout(new BoxLayout(pnlPnl, BoxLayout.Y_AXIS));
                pnlPnl.setBackground(Color.WHITE);
                pnlPnl.setBorder(new EmptyBorder(10, 14, 12, 14));
                // lblTitle: Nhãn hiển thị: TẠO / SỬA PHÂN CA.
                JLabel lblTitle = new JLabel("TẠO / SỬA PHÂN CA");
                lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblTitle.setForeground(NAVY);
                pnlPnl.add(lblTitle);
                pnlPnl.add(Box.createVerticalStrut(8));

                txtMaPhanCa = new JTextField();
                txtMaPhanCa.setEditable(false);
                themTruong(pnlPnl, "Mã phân ca", txtMaPhanCa);

                txtNgayLamViec = new JTextField();
                txtNgayLamViec.setEditable(false);
                txtNgayLamViec.setBackground(Color.WHITE);
                // pnlChonNgay: Khung chứa thành phần giao diện chọn ngày.
                JPanel pnlChonNgay = new JPanel(new BorderLayout(4, 0));
                pnlChonNgay.setBackground(Color.WHITE);
                pnlChonNgay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));
                // btnLich: Nút thao tác lịch.
                btnLich = taoNut("Chọn lịch", NAVY);
                btnLich.setMargin(new Insets(2, 6, 2, 6));
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnLich.addActionListener(this);
                pnlChonNgay.add(txtNgayLamViec, BorderLayout.CENTER);
                pnlChonNgay.add(btnLich, BorderLayout.EAST);
                themTruong(pnlPnl, "Ngày làm việc (từ hôm nay)", pnlChonNgay);

                cmbNhanVien = new JComboBox<>();
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                cmbNhanVien.addActionListener(this);
                themTruong(pnlPnl, "Nhân viên", cmbNhanVien);
                txtHoTen = new JTextField();
                txtHoTen.setEditable(false);
                themTruong(pnlPnl, "Họ tên", txtHoTen);
                cmbCa = new JComboBox<>();
                themTruong(pnlPnl, "Ca làm việc", cmbCa);

                // pnlNut: Khung chứa thành phần giao diện nút.
                JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 0));
                pnlNut.setOpaque(false);
                // btnThem: Nút thao tác thêm.
                btnThem = taoNut("Thêm +", GREEN);
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnThem.addActionListener(this);
                btnSua = taoNut("Sửa", NAVY);
                btnSua.setEnabled(false);
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnSua.addActionListener(this);
                // btnLamMoi: Nút thao tác làm mới.
                btnBoChon = taoNut("Bỏ chọn", new Color(108, 117, 125));
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                btnBoChon.addActionListener(this);
                pnlNut.add(btnThem);
                pnlNut.add(btnSua);
                pnlNut.add(btnBoChon);
                pnlNut.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlPnl.add(Box.createVerticalStrut(8));
                pnlPnl.add(pnlNut);
                pnlPnl.add(Box.createVerticalStrut(6));
                return pnlPnl;
        }

        /**
         * Thẻ thống kê ở góc phải bên dưới form: số nhân viên được PHÂN CA hôm nay.
         * Đây là số người được xếp lịch, không phải thống kê chấm công thực tế.
         */
        private JPanel taoPnlThongKeHomNay() {
                JPanel khung = new JPanel(new BorderLayout(0, 8));
                khung.setBackground(Color.WHITE);
                khung.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(215, 226, 237)),
                                new EmptyBorder(12, 14, 12, 14)));

                JPanel dau = new JPanel(new BorderLayout(4, 4));
                dau.setOpaque(false);
                JLabel tieuDe = new JLabel("NHÂN VIÊN LÀM VIỆC HÔM NAY");
                tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 14));
                tieuDe.setForeground(NAVY);
                dau.add(tieuDe, BorderLayout.CENTER);
                btnLamMoiThongKe = new JButton("↻");
                btnLamMoiThongKe.setFocusable(false);
                btnLamMoiThongKe.setToolTipText("Cập nhật số nhân viên phân ca hôm nay từ SQL Server");
                btnLamMoiThongKe.setMargin(new Insets(1, 5, 1, 5));
                btnLamMoiThongKe.addActionListener(this);
                dau.add(btnLamMoiThongKe, BorderLayout.EAST);

                lblNgayThongKe = new JLabel();
                lblNgayThongKe.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblNgayThongKe.setForeground(new Color(91, 105, 120));
                JPanel chiTiet = new JPanel(new BorderLayout(6, 0));
                chiTiet.setOpaque(false);
                lblSoNhanVienHomNay = new JLabel("0");
                lblSoNhanVienHomNay.setForeground(new Color(29, 133, 82));
                lblSoNhanVienHomNay.setFont(new Font("Segoe UI", Font.BOLD, 34));
                chiTiet.add(lblSoNhanVienHomNay, BorderLayout.WEST);
                JPanel nhan = new JPanel();
                nhan.setOpaque(false);
                nhan.setLayout(new BoxLayout(nhan, BoxLayout.Y_AXIS));
                nhan.add(new JLabel("nhân viên được phân ca"));
                lblSoCaHomNay = new JLabel("0 ca làm việc");
                lblSoCaHomNay.setForeground(new Color(90, 105, 120));
                nhan.add(lblSoCaHomNay);
                chiTiet.add(nhan, BorderLayout.CENTER);

                JPanel thongTin = new JPanel();
                thongTin.setOpaque(false);
                thongTin.setLayout(new BoxLayout(thongTin, BoxLayout.Y_AXIS));
                lblNgayThongKe.setAlignmentX(Component.LEFT_ALIGNMENT);
                chiTiet.setAlignmentX(Component.LEFT_ALIGNMENT);
                thongTin.add(lblNgayThongKe);
                thongTin.add(Box.createVerticalStrut(5));
                thongTin.add(chiTiet);
                dau.setAlignmentX(Component.LEFT_ALIGNMENT);
                JPanel phanDau = new JPanel(new BorderLayout(0, 8));
                phanDau.setOpaque(false);
                phanDau.add(dau, BorderLayout.NORTH);
                phanDau.add(thongTin, BorderLayout.SOUTH);
                khung.add(phanDau, BorderLayout.NORTH);

                return khung;
        }

        /**
         * Lấy phân ca theo LocalDate.now(); đếm DISTINCT mã nhân viên, kể cả người có
         * nhiều ca.
         */
        private void taiThongKeHomNay() {
                LocalDate homNay = LocalDate.now();
                lblNgayThongKe.setText("Ngày " + homNay.format(DINH_DANG_NGAY));
                try {
                        List<LichPhanCa> lich = dao.layPhanCaTheoNgay(homNay);
                        java.util.Set<String> nhanVienKhacNhau = new java.util.HashSet<>();
                        for (LichPhanCa pc : lich) {
                                nhanVienKhacNhau.add(pc.getMaNhanVien());
                        }
                        // Một nhân viên có nhiều ca vẫn chỉ tính là 1 nhân viên.
                        lblSoNhanVienHomNay.setText(String.valueOf(nhanVienKhacNhau.size()));
                        // Mỗi phân ca trong danh sách tương ứng một ca làm việc.
                        lblSoCaHomNay.setText(lich.size() + " ca làm việc");
                } catch (Exception ex) {
                        // Không hiển thị 0 gây hiểu nhầm khi không kết nối được DB.
                        lblSoNhanVienHomNay.setText("—");
                        lblSoCaHomNay.setText("Không tải được số ca");
                        ex.printStackTrace();
                }
        }

        // Thêm trường nhập liệu.
        // Tham số pnlPnl: khung giao diện.
        // Tham số nhan: nhãn.
        // Tham số control: control.
        private void themTruong(JPanel pnlPnl, String nhan, JComponent control) {
                // lblLbl: Nhãn hiển thị nhãn.
                JLabel lblLbl = new JLabel(nhan);
                lblLbl.setFont(fntNoiDung);
                lblLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlPnl.add(lblLbl);
                pnlPnl.add(Box.createVerticalStrut(3));
                control.setFont(fntNoiDung);
                control.setAlignmentX(Component.LEFT_ALIGNMENT);
                control.setMaximumSize(new Dimension(10000, 29));
                control.setPreferredSize(new Dimension(225, 29));
                pnlPnl.add(control);
                pnlPnl.add(Box.createVerticalStrut(6));
        }

        // Tải danh mục.
        private void taiDanhMuc() {
                // maNV: Lưu mã nhân viên.
                String maNV = maDaChon(cmbNhanVien);
                // maCa: Lưu mã ca.
                String maCa = maDaChon(cmbCa);
                if (cmbNhanVien != null) {
                        cmbNhanVien.removeAllItems();
                        cmbLocNhanVien.removeAllItems();
                        cmbLocNhanVien.addItem("Tất cả");
                        for (String[] nv : dao.getDanhSachNhanVien()) {
                                String item = nv[0] + " - " + nv[1];
                                cmbNhanVien.addItem(item);
                                cmbLocNhanVien.addItem(item);
                        }
                }
                if (cmbCa != null) {
                        cmbCa.removeAllItems();
                        cmbLocCa.removeAllItems();
                        cmbLocCa.addItem("Tất cả");
                        for (String[] ca : dao.getDanhSachCa()) {
                                cmbCa.addItem(ca[0] + " - " + ca[1] + ": " + ca[2] + "–" + ca[3]);
                                cmbLocCa.addItem(ca[0] + " - " + ca[1]);
                        }
                }
                chonTheoMa(cmbNhanVien, maNV);
                chonTheoMa(cmbCa, maCa);
                capNhatHoTen();
        }

        // Đọc dữ liệu qua DAO và cập nhật giao diện.
        private void taiDuLieu() {
                hienThiDuLieu(dao.getAll());
                taiThongKeHomNay();
        }

        // Hiển thị thị dữ liệu.
        // Tham số ds: danh sách.
        private void hienThiDuLieu(List<LichPhanCa> ds) {
                mdlPhanCa.setRowCount(0);
                for (LichPhanCa pc : ds) {
                        String strNgay = pc.getNgayLamViec() == null ? "" : pc.getNgayLamViec().format(DINH_DANG_NGAY);
                        // thongTinCa: Lưu thông tin ca làm.
                        String thongTinCa = pc.getThongTinCa();
                        mdlPhanCa.addRow(new Object[] { pc.getMaPhanCa(), strNgay,
                                        pc.getMaNhanVien(), pc.getHoTen(), pc.getMaCa(), thongTinCa });
                }
                if (lblTrangThai != null)
                        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                        lblTrangThai.setText("Tổng: " + ds.size() + " phân ca");
        }

        // Hiển thị thị biểu mẫu.
        private void hienThiForm() {
                // row: Lưu chỉ số dòng.
                int row = tblPhanCa.getSelectedRow();
                if (row < 0)
                        return;
                int modelRow = tblPhanCa.convertRowIndexToModel(row);
                maDangSua = String.valueOf(mdlPhanCa.getValueAt(modelRow, 0));
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtMaPhanCa.setText(maDangSua);
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtNgayLamViec.setText(String.valueOf(mdlPhanCa.getValueAt(modelRow, 1)));
                chonTheoMa(cmbNhanVien, String.valueOf(mdlPhanCa.getValueAt(modelRow, 2)));
                chonTheoMa(cmbCa, String.valueOf(mdlPhanCa.getValueAt(modelRow, 4)));
                btnSua.setEnabled(true);
        }

        // Chọn theo mã.
        // Tham số cmbCbo: hộp lựa chọn.
        // Tham số ma: mã.
        private void chonTheoMa(JComboBox<String> cmbCbo, String ma) {
                if (cmbCbo == null || ma == null)
                        return;
                for (int i = 0; i < cmbCbo.getItemCount(); i++) {
                        if (cmbCbo.getItemAt(i).startsWith(ma + " - ")) {
                                cmbCbo.setSelectedIndex(i);
                                return;
                        }
                }
        }

        // Tách mã của mục đang được chọn trong hộp lựa chọn.
        // Tham số cmbCbo: hộp lựa chọn.
        // Kết quả: chuỗi kết quả.
        private String maDaChon(JComboBox<String> cmbCbo) {
                if (cmbCbo == null || cmbCbo.getSelectedItem() == null)
                        return "";
                // selected: Lưu trạng thái được chọn.
                String selected = cmbCbo.getSelectedItem().toString();
                int pos = selected.indexOf(" - ");
                return pos >= 0 ? selected.substring(0, pos) : selected;
        }

        // Cập nhật họ tên.
        private void capNhatHoTen() {
                if (txtHoTen == null)
                        return;
                // selected: Lưu trạng thái được chọn.
                String selected = cmbNhanVien.getSelectedItem() == null ? "" : cmbNhanVien.getSelectedItem().toString();
                int pos = selected.indexOf(" - ");
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtHoTen.setText(pos >= 0 ? selected.substring(pos + 3) : "");
        }

        // Lấy dữ liệu biểu mẫu.
        // Tham số sua: sửa.
        // Kết quả: đối tượng/giá trị kiểu LichPhanCa.
        private LichPhanCa layDuLieuForm(boolean sua) {
                if (cmbNhanVien.getSelectedItem() == null || cmbCa.getSelectedItem() == null) {
                        canhBao("Vui lòng chọn nhân viên và ca làm việc.");
                        return null;
                }
                // maNhanVien: Lưu mã nhân viên.
                String maNhanVien = maDaChon(cmbNhanVien);
                // maCa: Lưu mã ca.
                String maCa = maDaChon(cmbCa);
                if (!maNhanVien.matches("NV\\d{3}") || !maCa.matches("CA\\d{3}")) {
                        canhBao("Hãy chọn nhân viên và ca làm hợp lệ từ danh sách.");
                        return null;
                }
                if (sua && (maDangSua == null || maDangSua.isBlank())) {
                        canhBao("Vui lòng chọn phân ca cần sửa.");
                        return null;
                }
                // ngay: Lưu ngày dưới dạng dữ liệu thời gian.
                LocalDate ngay = docNgay(txtNgayLamViec.getText());
                if (ngay == null) {
                        canhBao("Vui lòng chọn ngày làm việc từ lịch.");
                        return null;
                }
                if (ngay.isBefore(LocalDate.now())) {
                        canhBao("Không được thêm hoặc sửa phân ca cho ngày đã qua.");
                        return null;
                }
                return new LichPhanCa(sua ? maDangSua : dao.taoMaPhanCa(),
                                ngay, maNhanVien, maCa);
        }

        // Xử lý yêu cầu thêm dữ liệu từ giao diện.
        private void them() {
                LichPhanCa pc = layDuLieuForm(false);
                if (pc == null)
                        return;
                try {
                        if (dao.trungGio(pc.getMaNhanVien(), pc.getMaCa(), pc.getNgayLamViec(), null)) {
                                canhBao("Nhân viên đã có ca trùng hoặc chồng giờ trong ngày này.");
                                return;
                        }
                        if (dao.them(pc)) {
                                thongBao("Thêm phân ca thành công.");
                                taiDuLieu();
                                lamMoiForm();
                        } else
                                canhBao("Không thể thêm phân ca. Kiểm tra kết nối hoặc mã đã tồn tại.");
                } catch (Exception ex) {
                        baoLoi(ex);
                }
        }

        // Xử lý yêu cầu sửa bản ghi đang chọn.
        private void sua() {
                if (maDangSua == null) {
                        canhBao("Vui lòng chọn dòng phân ca cần sửa.");
                        return;
                }
                LichPhanCa pc = layDuLieuForm(true);
                if (pc == null)
                        return;
                try {
                        if (dao.trungGio(pc.getMaNhanVien(), pc.getMaCa(), pc.getNgayLamViec(), pc.getMaPhanCa())) {
                                canhBao("Nhân viên đã có ca trùng hoặc chồng giờ với phân ca khác.");
                                return;
                        }
                        if (dao.sua(pc)) {
                                thongBao("Cập nhật phân ca " + pc.getMaPhanCa() + " thành công.");
                                taiDuLieu();
                                lamMoiForm();
                        } else
                                canhBao("Không thể sửa. Không cho sửa phân ca của ngày đã qua hoặc mã không tồn tại.");
                } catch (Exception ex) {
                        baoLoi(ex);
                }
        }

        // Xử lý yêu cầu xóa bản ghi đang chọn.
        private void xoa() {
                // row: Lưu chỉ số dòng.
                int row = tblPhanCa.getSelectedRow();
                if (row < 0) {
                        canhBao("Vui lòng chọn phân ca cần xóa.");
                        return;
                }
                int modelRow = tblPhanCa.convertRowIndexToModel(row);
                // ma: Lưu mã.
                String ma = String.valueOf(mdlPhanCa.getValueAt(modelRow, 0));
                if (JOptionPane.showConfirmDialog(this, "Xóa phân ca " + ma + "?",
                                "Xác nhận xóa", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;
                if (dao.xoa(ma)) {
                        thongBao("Đã xóa phân ca " + ma + ".");
                        taiDuLieu();
                        lamMoiForm();
                } else
                        canhBao("Xóa phân ca thất bại.");
        }

        // Lọc dữ liệu.
        private void locDuLieu() {
                // maNV: Lưu mã nhân viên.
                String maNV = maDaChon(cmbLocNhanVien);
                // maCa: Lưu mã ca.
                String maCa = maDaChon(cmbLocCa);
                // ngayNhap: Đọc nội dung chữ từ ô nhập để xử lý ngày giờ nhập kho.
                String ngayNhap = txtLocNgay.getText().trim();
                // ngay: Lưu ngày dưới dạng dữ liệu thời gian.
                LocalDate ngay = docNgay(ngayNhap);
                if (!ngayNhap.isEmpty() && ngay == null) {
                        canhBao("Ngày lọc không hợp lệ. Hãy chọn lại từ lịch (dd/MM/yyyy).");
                        txtLocNgay.requestFocusInWindow();
                        return;
                }
                // ketQua: Lưu kết quả; phần tử có kiểu LichPhanCa.
                List<LichPhanCa> ketQua = new ArrayList<>();
                for (LichPhanCa pc : dao.getAll()) {
                        if (ngay != null && !ngay.equals(pc.getNgayLamViec()))
                                continue;
                        if (!maNV.isEmpty() && !"Tất cả".equals(maNV) && !maNV.equalsIgnoreCase(pc.getMaNhanVien()))
                                continue;
                        if (!maCa.isEmpty() && !"Tất cả".equals(maCa) && !maCa.equalsIgnoreCase(pc.getMaCa()))
                                continue;
                        ketQua.add(pc);
                }
                hienThiDuLieu(ketQua);
                lamMoiFormKhongXoaBoLoc();
        }

        // Đọc chuỗi ngày từ bộ lọc thành đối tượng LocalDate.
        // Tham số text: nội dung chữ.
        // Kết quả: đối tượng/giá trị kiểu LocalDate.
        private LocalDate docNgay(String text) {
                if (text == null || text.trim().isEmpty())
                        return null;
                try {
                        return LocalDate.parse(text.trim(), DINH_DANG_NGAY);
                } catch (Exception ex) {
                        return null;
                }
        }

        // Làm mới biểu mẫu không xóa bỏ lọc.
        private void lamMoiFormKhongXoaBoLoc() {
                maDangSua = null;
                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                txtMaPhanCa.setText(dao.taoMaPhanCa());
                // Định dạng dữ liệu thành chuỗi rồi hiển thị trong control bằng setText().
                txtNgayLamViec.setText(LocalDate.now().format(DINH_DANG_NGAY));
                tblPhanCa.clearSelection();
                if (btnSua != null)
                        btnSua.setEnabled(false);
        }

        // Đưa biểu mẫu nhập liệu về trạng thái ban đầu.
        private void lamMoiForm() {
                lamMoiFormKhongXoaBoLoc();
                if (cmbNhanVien.getItemCount() > 0)
                        cmbNhanVien.setSelectedIndex(0);
                if (cmbCa.getItemCount() > 0)
                        cmbCa.setSelectedIndex(0);
                if (txtLocNgay != null)
                        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                        txtLocNgay.setText("");
                if (cmbLocNhanVien.getItemCount() > 0)
                        cmbLocNhanVien.setSelectedIndex(0);
                if (cmbLocCa.getItemCount() > 0)
                        cmbLocCa.setSelectedIndex(0);
        }

        // Xuất Excel theo NGÀY ĐANG CHỌN ở bộ lọc, không theo ngày hiện tại.
        // Không phụ thuộc vào việc người dùng đã nhấn nút Lọc hay chưa.
        private void xuatExcel() {
                LocalDate ngayXuat = docNgay(txtLocNgay.getText());
                if (ngayXuat == null) {
                        // Nếu chưa chọn ngày lọc, yêu cầu chọn ngày cần xuất.
                        // false: cho phép xuất cả lịch phân ca của ngày trong quá khứ.
                        LocalDate chon = LichChonNgay.chonNgay(this, null, false);
                        if (chon == null)
                                return;
                        ngayXuat = chon;
                        txtLocNgay.setText(ngayXuat.format(DINH_DANG_NGAY));
                }

                // Lọc dữ liệu thực tế theo ngày được chọn, không lấy các dòng đang
                // hiển thị (bảng có thể chưa được cập nhật sau khi đổi ngày lọc).
                String maNV = maDaChon(cmbLocNhanVien);
                String maCa = maDaChon(cmbLocCa);
                List<String[]> rows = new ArrayList<>();
                try {
                        for (LichPhanCa pc : dao.getAll()) {
                                if (!ngayXuat.equals(pc.getNgayLamViec()))
                                        continue;
                                if (!maNV.isEmpty() && !"Tất cả".equals(maNV)
                                                && !maNV.equalsIgnoreCase(pc.getMaNhanVien()))
                                        continue;
                                if (!maCa.isEmpty() && !"Tất cả".equals(maCa)
                                                && !maCa.equalsIgnoreCase(pc.getMaCa()))
                                        continue;
                                rows.add(new String[] {
                                                pc.getMaPhanCa(),
                                                pc.getNgayLamViec().format(DINH_DANG_NGAY),
                                                pc.getMaNhanVien(),
                                                pc.getHoTen(),
                                                pc.getMaCa(),
                                                pc.getThongTinCa()
                                });
                        }
                } catch (RuntimeException ex) {
                        baoLoi(ex);
                        return;
                }
                if (rows.isEmpty()) {
                        canhBao("Không có phân ca để xuất trong ngày "
                                        + ngayXuat.format(DINH_DANG_NGAY) + ".");
                        return;
                }

                JFileChooser fchChonTep = new JFileChooser();
                fchChonTep.setDialogTitle("Xuất lịch phân ca ngày " + ngayXuat.format(DINH_DANG_NGAY));
                fchChonTep.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
                fchChonTep.setSelectedFile(new File("LichPhanCa_" + ngayXuat + ".xlsx"));
                if (fchChonTep.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
                        return;

                File f = fchChonTep.getSelectedFile();
                if (!f.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".xlsx"))
                        f = new File(f.getParentFile(), f.getName() + ".xlsx");
                if (f.exists() && JOptionPane.showConfirmDialog(this, "Tệp đã tồn tại. Ghi đè?",
                                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;

                String[] cols = { "Mã phân ca", "Ngày làm việc", "Mã nhân viên", "Họ tên", "Mã ca", "Ca làm" };
                try {
                        XlsxExporter.xuat(f.toPath(), cols, rows);
                        thongBao("Đã xuất " + rows.size() + " phân ca ngày "
                                        + ngayXuat.format(DINH_DANG_NGAY)
                                        + " sang Excel:\n" + f.getAbsolutePath());
                } catch (IOException ex) {
                        baoLoi(ex);
                }
        }

        /** Lấy thứ Hai của tuần có chứa ngày đầu vào. */
        private LocalDate dauTuan(LocalDate ngay) {
                return ngay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        /** Hiển thị khoảng ngày theo cách viết quen thuộc của cửa hàng. */
        private String khoangTuan(LocalDate thuHai) {
                return thuHai.format(DINH_DANG_NGAY) + " – "
                                + thuHai.plusDays(6).format(DINH_DANG_NGAY);
        }

        /** Xem bảng lịch tuần: mỗi hàng là một nhân viên, mỗi cột là một ngày. */
        private void xemLichTheoTuan() {
                Window cha = SwingUtilities.getWindowAncestor(this);
                JDialog dialog = new JDialog(cha, "Lịch phân ca theo tuần", Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setLayout(new BorderLayout(10, 10));
                ((JPanel) dialog.getContentPane()).setBorder(new EmptyBorder(14, 14, 12, 14));

                LocalDate daLoc = docNgay(txtLocNgay.getText());
                LocalDate[] tuan = { dauTuan(daLoc == null ? LocalDate.now() : daLoc) };
                JLabel lblTuan = new JLabel();
                lblTuan.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lblTuan.setForeground(NAVY);
                JPanel thanhTren = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
                JButton truoc = taoNut("< Tuần trước", NAVY);
                JButton chon = taoNut("Chọn tuần", NAVY);
                JButton hienTai = taoNut("Tuần này", new Color(29, 133, 82));
                JButton sau = taoNut("Tuần sau >", NAVY);
                thanhTren.add(truoc);
                thanhTren.add(chon);
                thanhTren.add(hienTai);
                thanhTren.add(sau);
                thanhTren.add(lblTuan);
                dialog.add(thanhTren, BorderLayout.NORTH);

                DefaultTableModel modelTuan = new DefaultTableModel() {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                                return false;
                        }
                };
                JTable bangTuan = new JTable(modelTuan);
                bangTuan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                bangTuan.setRowHeight(67);
                bangTuan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                bangTuan.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
                bangTuan.getTableHeader().setBackground(NAVY);
                bangTuan.getTableHeader().setForeground(Color.WHITE);
                bangTuan.getTableHeader().setReorderingAllowed(false);
                bangTuan.setSelectionBackground(new Color(207, 222, 235));
                bangTuan.setFillsViewportHeight(true);
                // JTextArea cho phép hiển thị nhiều ca trong cùng một ngày.
                bangTuan.setDefaultRenderer(Object.class, new TableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value,
                                        boolean isSelected, boolean hasFocus, int row, int column) {
                                JTextArea area = new JTextArea(value == null ? "" : value.toString());
                                area.setLineWrap(true);
                                area.setWrapStyleWord(true);
                                area.setEditable(false);
                                area.setFont(table.getFont());
                                area.setBorder(new EmptyBorder(6, 6, 4, 4));
                                if (isSelected)
                                        area.setBackground(table.getSelectionBackground());
                                else
                                        area.setBackground(column == 0 ? new Color(232, 240, 249)
                                                        : (column >= 6 ? new Color(247, 250, 253) : Color.WHITE));
                                area.setForeground(NAVY);
                                return area;
                        }
                });
                JScrollPane cuon = new JScrollPane(bangTuan);
                cuon.setBorder(new LineBorder(new Color(210, 220, 230)));
                dialog.add(cuon, BorderLayout.CENTER);

                JLabel tongKet = new JLabel();
                tongKet.setFont(fntNoiDung);
                JButton dong = taoNut("Đóng", new Color(108, 117, 125));
                dong.addActionListener(e -> dialog.dispose());
                JPanel duoi = new JPanel(new BorderLayout());
                duoi.add(tongKet, BorderLayout.WEST);
                duoi.add(dong, BorderLayout.EAST);
                dialog.add(duoi, BorderLayout.SOUTH);

                Runnable taiTuan = () -> {
                        try {
                                LocalDate thuHai = tuan[0];
                                lblTuan.setText("Tuần: " + khoangTuan(thuHai));
                                String[] cot = new String[8];
                                cot[0] = "Nhân viên";
                                String[] thu = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
                                for (int i = 0; i < 7; i++)
                                        cot[i + 1] = thu[i] + " " + thuHai.plusDays(i)
                                                        .format(DateTimeFormatter.ofPattern("dd/MM"));
                                modelTuan.setColumnIdentifiers(cot);
                                bangTuan.getColumnModel().getColumn(0).setPreferredWidth(205);
                                for (int c = 1; c < 8; c++)
                                        bangTuan.getColumnModel().getColumn(c).setPreferredWidth(163);
                                modelTuan.setRowCount(0);
                                List<LichPhanCa> phanCaTuan = dao.layPhanCaTrongTuan(thuHai);
                                Map<String, StringBuilder[]> bangTheoNguoi = new LinkedHashMap<>();
                                for (String[] nv : dao.getDanhSachNhanVien()) {
                                        StringBuilder[] ngay = new StringBuilder[7];
                                        for (int i = 0; i < 7; i++)
                                                ngay[i] = new StringBuilder();
                                        bangTheoNguoi.put(nv[0] + " - " + nv[1], ngay);
                                }
                                for (LichPhanCa pc : phanCaTuan) {
                                        String nhanVien = pc.getMaNhanVien() + " - " + pc.getHoTen();
                                        if (!bangTheoNguoi.containsKey(nhanVien)) {
                                                StringBuilder[] ngay = new StringBuilder[7];
                                                for (int i = 0; i < 7; i++)
                                                        ngay[i] = new StringBuilder();
                                                bangTheoNguoi.put(nhanVien, ngay);
                                        }
                                        int thuIndex = pc.getNgayLamViec().getDayOfWeek().getValue() - 1;
                                        StringBuilder o = bangTheoNguoi.get(nhanVien)[thuIndex];
                                        if (o.length() > 0)
                                                o.append("\n");
                                        o.append(pc.getMaCa()).append(" - ").append(pc.getTenCa())
                                                        .append(" (").append(pc.getGioBatDau()).append("–")
                                                        .append(pc.getGioKetThuc()).append(")");
                                }
                                for (Map.Entry<String, StringBuilder[]> hang : bangTheoNguoi.entrySet()) {
                                        Object[] duLieu = new Object[8];
                                        duLieu[0] = hang.getKey();
                                        for (int i = 0; i < 7; i++)
                                                duLieu[i + 1] = hang.getValue()[i].toString();
                                        modelTuan.addRow(duLieu);
                                }
                                tongKet.setText("Tổng: " + phanCaTuan.size() + " phân ca / "
                                                + bangTheoNguoi.size() + " nhân viên");
                        } catch (Exception ex) {
                                baoLoi(ex);
                        }
                };
                truoc.addActionListener(e -> {
                        tuan[0] = tuan[0].minusWeeks(1);
                        taiTuan.run();
                });
                sau.addActionListener(e -> {
                        tuan[0] = tuan[0].plusWeeks(1);
                        taiTuan.run();
                });
                hienTai.addActionListener(e -> {
                        tuan[0] = dauTuan(LocalDate.now());
                        taiTuan.run();
                });
                chon.addActionListener(e -> {
                        LocalDate ngay = LichChonNgay.chonNgay(dialog, tuan[0], false);
                        if (ngay != null) {
                                tuan[0] = dauTuan(ngay);
                                taiTuan.run();
                        }
                });

                dialog.setSize(1350, 655);
                dialog.setMinimumSize(new Dimension(980, 450));
                dialog.setLocationRelativeTo(cha);
                taiTuan.run();
                dialog.setVisible(true);
        }

        /** Chọn tuần nguồn/đích và sao chép; không ghi đè các phân ca có sẵn. */
        private void hienThiSaoChepTuan() {
                Window cha = SwingUtilities.getWindowAncestor(this);
                JDialog dialog = new JDialog(cha, "Sao chép lịch phân ca", Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setLayout(new BorderLayout(10, 10));
                ((JPanel) dialog.getContentPane()).setBorder(new EmptyBorder(18, 18, 14, 18));
                LocalDate[] nguon = { dauTuan(LocalDate.now()) };
                LocalDate[] dich = { nguon[0].plusWeeks(1) };
                JTextField txtNguon = new JTextField(khoangTuan(nguon[0]), 22);
                JTextField txtDich = new JTextField(khoangTuan(dich[0]), 22);
                for (JTextField txt : new JTextField[] { txtNguon, txtDich }) {
                        txt.setEditable(false);
                        txt.setBackground(new Color(245, 247, 250));
                        txt.setFont(fntNoiDung);
                }
                JButton chonNguon = taoNut("Chọn tuần nguồn", NAVY);
                JButton chonDich = taoNut("Chọn tuần đích", NAVY);
                chonNguon.addActionListener(e -> {
                        LocalDate ngay = LichChonNgay.chonNgay(dialog, nguon[0], false);
                        if (ngay != null) {
                                nguon[0] = dauTuan(ngay);
                                txtNguon.setText(khoangTuan(nguon[0]));
                        }
                });
                chonDich.addActionListener(e -> {
                        LocalDate ngay = LichChonNgay.chonNgay(dialog, dich[0], false);
                        if (ngay != null) {
                                dich[0] = dauTuan(ngay);
                                txtDich.setText(khoangTuan(dich[0]));
                        }
                });
                JPanel noidung = new JPanel(new GridBagLayout());
                GridBagConstraints g = new GridBagConstraints();
                g.insets = new Insets(6, 4, 6, 4);
                g.fill = GridBagConstraints.HORIZONTAL;
                g.weightx = 0;
                g.gridx = 0;
                g.gridy = 0;
                noidung.add(new JLabel("Tuần nguồn:"), g);
                g.gridx = 1;
                g.weightx = 1;
                noidung.add(txtNguon, g);
                g.gridx = 2;
                g.weightx = 0;
                noidung.add(chonNguon, g);
                g.gridx = 0;
                g.gridy = 1;
                noidung.add(new JLabel("Tuần đích:"), g);
                g.gridx = 1;
                g.weightx = 1;
                noidung.add(txtDich, g);
                g.gridx = 2;
                g.weightx = 0;
                noidung.add(chonDich, g);
                dialog.add(noidung, BorderLayout.CENTER);

                JLabel huongDan = new JLabel("<html>Giữ nguyên nhân viên và ca làm; chuyển đúng thứ trong tuần.<br>"
                                + "Tự bỏ qua ca trùng giờ hoặc ngày quá khứ; không ghi đè lịch đích.</html>");
                huongDan.setForeground(NAVY);
                dialog.add(huongDan, BorderLayout.NORTH);
                JPanel nutDuoi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                JButton huy = taoNut("Hủy", new Color(108, 117, 125));
                JButton saoChep = taoNut("Sao chép lịch", GREEN);
                huy.addActionListener(e -> dialog.dispose());
                nutDuoi.add(huy);
                nutDuoi.add(saoChep);
                dialog.add(nutDuoi, BorderLayout.SOUTH);
                saoChep.addActionListener(e -> {
                        if (nguon[0].equals(dich[0])) {
                                JOptionPane.showMessageDialog(dialog, "Tuần nguồn và tuần đích phải khác nhau.",
                                                "Ngày không hợp lệ", JOptionPane.WARNING_MESSAGE);
                                return;
                        }
                        if (dich[0].plusDays(6).isBefore(LocalDate.now())) {
                                JOptionPane.showMessageDialog(dialog, "Tuần đích đã qua, vui lòng chọn tuần khác.",
                                                "Ngày không hợp lệ", JOptionPane.WARNING_MESSAGE);
                                return;
                        }
                        int xacNhan = JOptionPane.showConfirmDialog(dialog,
                                        "Sao chép từ tuần " + khoangTuan(nguon[0]) + "\n"
                                                        + "sang tuần " + khoangTuan(dich[0]) + "?\n\n"
                                                        + "Các ca đã tồn tại hoặc trùng giờ sẽ được giữ nguyên và bỏ qua.\n"
                                                        + "Không tạo phân ca cho các ngày đã qua.",
                                        "Xác nhận sao chép", JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE);
                        if (xacNhan != JOptionPane.YES_OPTION)
                                return;
                        saoChep.setEnabled(false);
                        chonNguon.setEnabled(false);
                        chonDich.setEnabled(false);
                        new SwingWorker<LichPhanCaDAO.KetQuaSaoChep, Void>() {
                                @Override
                                protected LichPhanCaDAO.KetQuaSaoChep doInBackground() throws Exception {
                                        return dao.saoChepLichTheoTuan(nguon[0], dich[0]);
                                }

                                @Override
                                protected void done() {
                                        saoChep.setEnabled(true);
                                        chonNguon.setEnabled(true);
                                        chonDich.setEnabled(true);
                                        try {
                                                LichPhanCaDAO.KetQuaSaoChep kq = get();
                                                JOptionPane.showMessageDialog(dialog,
                                                                "Tổng lịch tuần nguồn: " + kq.getTongNguon() + "\n"
                                                                                + "Đã sao chép: " + kq.getDaThem()
                                                                                + "\n"
                                                                                + "Bỏ qua do trùng giờ: "
                                                                                + kq.getBoQuaTrungGio() + "\n"
                                                                                + "Bỏ qua do ngày đã qua: "
                                                                                + kq.getBoQuaQuaKhu(),
                                                                "Kết quả sao chép", JOptionPane.INFORMATION_MESSAGE);
                                                if (kq.getDaThem() > 0) {
                                                        taiDuLieu();
                                                        lamMoiForm();
                                                        dialog.dispose();
                                                }
                                        } catch (InterruptedException ex) {
                                                Thread.currentThread().interrupt();
                                                baoLoi(ex);
                                        } catch (ExecutionException ex) {
                                                Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                                                JOptionPane.showMessageDialog(dialog,
                                                                "Không thể sao chép lịch: " + cause.getMessage(),
                                                                "Lỗi sao chép", JOptionPane.ERROR_MESSAGE);
                                        }
                                }
                        }.execute();
                });

                dialog.setSize(690, 260);
                dialog.setMinimumSize(new Dimension(630, 260));
                dialog.setLocationRelativeTo(cha);
                dialog.setVisible(true);
        }

        // Tạo nút.
        // Tham số text: nội dung chữ.
        // Tham số nen: màu nền.
        // Kết quả: đối tượng/giá trị kiểu JButton.
        private JButton taoNut(String text, Color nen) {
                // btnBtn: Nút thao tác nút.
                JButton btnBtn = new JButton(text);
                btnBtn.setBackground(nen);
                btnBtn.setForeground(Color.WHITE);
                btnBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnBtn.setFocusPainted(false);
                btnBtn.setBorder(new EmptyBorder(8, 10, 8, 10));
                btnBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return btnBtn;
        }

        // Thông báo dữ liệu của màn hình.
        // Tham số text: nội dung chữ.
        private void thongBao(String text) {
                JOptionPane.showMessageDialog(this, text, "Pet Shop Pro", JOptionPane.INFORMATION_MESSAGE);
        }

        // Cảnh báo dữ liệu của màn hình.
        // Tham số text: nội dung chữ.
        private void canhBao(String text) {
                JOptionPane.showMessageDialog(this, text, "Pet Shop Pro", JOptionPane.WARNING_MESSAGE);
        }

        // Báo lỗi dữ liệu của màn hình.
        // Tham số ex: ngoại lệ cần xử lý.
        private void baoLoi(Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Có lỗi khi xử lý phân ca:\n" + ex.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
                Object nguon = e.getSource();
                if (nguon == btnChonNgay) {
                        // Chọn ngày lọc: cho phép chọn cả ngày quá khứ.
                        LocalDate banDau = docNgay(txtLocNgay.getText());
                        LocalDate chon = LichChonNgay.chonNgay(this, banDau, false);
                        if (chon != null) {
                                txtLocNgay.setText(chon.format(DINH_DANG_NGAY));
                        }
                } else if (nguon == btnXoaLocNgay) {
                        txtLocNgay.setText("");
                } else if (nguon == btnLoc) {
                        locDuLieu();
                } else if (nguon == btnXoa) {
                        xoa();
                } else if (nguon == btnExcel) {
                        xuatExcel();
                } else if (nguon == btnXemTuan) {
                        xemLichTheoTuan();
                } else if (nguon == btnSaoChepTuan) {
                        hienThiSaoChepTuan();
                } else if (nguon == btnLamMoiThongKe) {
                        taiThongKeHomNay();
                } else if (nguon == btnLamMoiDanhSach) {
                        taiDanhMuc();
                        taiDuLieu();
                        lamMoiForm();
                } else if (nguon == btnLich) {
                        // Chọn ngày phân ca: không cho phép chọn ngày đã qua.
                        LocalDate chon = LichChonNgay.chonNgay(this, docNgay(txtNgayLamViec.getText()), true);
                        if (chon != null) {
                                txtNgayLamViec.setText(chon.format(DINH_DANG_NGAY));
                        }
                } else if (nguon == cmbNhanVien) {
                        capNhatHoTen();
                } else if (nguon == btnThem) {
                        them();
                } else if (nguon == btnSua) {
                        sua();
                } else if (nguon == btnBoChon) {
                        lamMoiForm();
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
