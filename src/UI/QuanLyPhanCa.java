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
        // txtThongTinCa: Ô nhập/hiển thị văn bản thông tin ca làm.
        // txtLocNgay: Ô nhập/hiển thị văn bản lọc ngày.
        private JTextField txtMaPhanCa, txtNgayLamViec, txtHoTen, txtThongTinCa, txtLocNgay;
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
        private JButton btnLich, btnThem, btnBoChon;
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
                pnlTrai.add(btnXoa);
                pnlTrai.add(btnExcel);
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
                pnlPnl.setBackground(LIGHT_BLUE);
                pnlPnl.setPreferredSize(new Dimension(310, 0));
                pnlPnl.add(taoPnlForm(), BorderLayout.NORTH);
                pnlPnl.add(taoPnlThongTinCa(), BorderLayout.CENTER);
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
                // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
                cmbCa.addActionListener(this);
                themTruong(pnlPnl, "Ca làm việc", cmbCa);
                txtThongTinCa = new JTextField();
                txtThongTinCa.setEditable(false);
                themTruong(pnlPnl, "Thông tin ca", txtThongTinCa);

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

        // Tạo khung thông tin ca làm.
        // Kết quả: đối tượng/giá trị kiểu JPanel.
        private JPanel taoPnlThongTinCa() {
                // pnlPnl: Khung dùng để nhóm và bố trí các thành phần giao diện.
                JPanel pnlPnl = new JPanel(new BorderLayout(0, 8));
                pnlPnl.setBackground(Color.WHITE);
                pnlPnl.setBorder(new EmptyBorder(14, 15, 14, 15));
                // lblLbl: Nhãn hiển thị: Danh sách ca.
                JLabel lblLbl = new JLabel("Danh sách ca");
                lblLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblLbl.setForeground(NAVY);
                pnlPnl.add(lblLbl, BorderLayout.NORTH);
                // txaDanhSach: Vùng văn bản nhiều dòng danh sách.
                JTextArea txaDanhSach = new JTextArea();
                txaDanhSach.setName("txtDanhSachCa");
                txaDanhSach.setEditable(false);
                txaDanhSach.setLineWrap(true);
                txaDanhSach.setWrapStyleWord(true);
                txaDanhSach.setFont(fntNoiDung);
                txaDanhSach.setBackground(Color.WHITE);
                pnlPnl.add(new JScrollPane(txaDanhSach), BorderLayout.CENTER);
                return pnlPnl;
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
                        // txaCa: Vùng văn bản nhiều dòng ca.
                        JTextArea txaCa = timTxtDanhSachCa();
                        // sb: Bộ đệm ghép các đoạn chuỗi để tạo bộ đệm chuỗi.
                        StringBuilder sb = new StringBuilder();
                        for (String[] ca : dao.getDanhSachCa()) {
                                cmbCa.addItem(ca[0] + " - " + ca[1] + ": " + ca[2] + "–" + ca[3]);
                                cmbLocCa.addItem(ca[0] + " - " + ca[1]);
                                sb.append(ca[0]).append(" - ").append(ca[1]).append("\n")
                                                .append(ca[2]).append(" – ").append(ca[3]).append("\n\n");
                        }
                        if (txaCa != null) {
                                // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                                txaCa.setText(sb.toString());
                                txaCa.setCaretPosition(0);
                        }
                }
                chonTheoMa(cmbNhanVien, maNV);
                chonTheoMa(cmbCa, maCa);
                capNhatHoTen();
                capNhatThongTinCa();
        }

        // Tìm ô văn bản danh sách ca.
        // Kết quả: đối tượng/giá trị kiểu JTextArea.
        private JTextArea timTxtDanhSachCa() {
                return timTxt(this);
        }

        // Tìm ô văn bản.
        // Tham số container: khung chứa.
        // Kết quả: đối tượng/giá trị kiểu JTextArea.
        private JTextArea timTxt(Container container) {
                for (Component child : container.getComponents()) {
                        if (child instanceof JTextArea && "txtDanhSachCa".equals(child.getName()))
                                return (JTextArea) child;
                        if (child instanceof Container) {
                                // txaKq: Vùng văn bản nhiều dòng kết quả.
                                JTextArea txaKq = timTxt((Container) child);
                                if (txaKq != null)
                                        return txaKq;
                        }
                }
                return null;
        }

        // Đọc dữ liệu qua DAO và cập nhật giao diện.
        private void taiDuLieu() {
                hienThiDuLieu(dao.getAll());
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

        // Cập nhật thông tin ca làm.
        private void capNhatThongTinCa() {
                if (txtThongTinCa != null)
                        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
                        txtThongTinCa.setText(
                                        cmbCa.getSelectedItem() == null ? "" : cmbCa.getSelectedItem().toString());
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

        // Xuất Excel.
        private void xuatExcel() {
                // fchChonTep: Hộp chọn tệp bộ chọn.
                JFileChooser fchChonTep = new JFileChooser();
                fchChonTep.setDialogTitle("Lưu lịch phân ca thành Excel");
                fchChonTep.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
                fchChonTep.setSelectedFile(new File("LichPhanCa_" + LocalDate.now() + ".xlsx"));
                if (fchChonTep.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
                        return;
                // f: Đường dẫn tệp dùng cho f.
                File f = fchChonTep.getSelectedFile();
                if (!f.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".xlsx"))
                        f = new File(f.getParentFile(), f.getName() + ".xlsx");
                if (f.exists() && JOptionPane.showConfirmDialog(this, "Tệp đã tồn tại. Ghi đè?",
                                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;
                // rows: Danh sách chứa các phần tử kiểu String[].
                List<String[]> rows = new ArrayList<>();
                for (int r = 0; r < tblPhanCa.getRowCount(); r++) {
                        // row: Mảng lưu chỉ số dòng.
                        String[] row = new String[tblPhanCa.getColumnCount()];
                        for (int c = 0; c < row.length; c++) {
                                Object value = tblPhanCa.getValueAt(r, c);
                                row[c] = value == null ? "" : value.toString();
                        }
                        rows.add(row);
                }
                // cols: Mảng lưu các cột.
                String[] cols = { "Mã phân ca", "Ngày làm việc", "Mã nhân viên", "Họ tên", "Mã ca", "Ca làm" };
                try {
                        XlsxExporter.xuat(f.toPath(), cols, rows);
                        thongBao("Đã xuất " + rows.size() + " phân ca sang Excel:\n" + f.getAbsolutePath());
                } catch (IOException ex) {
                        baoLoi(ex);
                }
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
                } else if (nguon == cmbCa) {
                        capNhatThongTinCa();
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
