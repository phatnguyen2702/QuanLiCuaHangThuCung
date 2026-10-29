package UI;

import UI.TrangChu.UIStyle;
import dao.DichVuDAO;
import entity.DichVu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/** Man hinh Danh muc > Quan ly dich vu Spa. */
public class QuanLyDichVu extends JPanel {

    // dao: Đối tượng truy cập dữ liệu dịch vụ.
    private final DichVuDAO dao = new DichVuDAO();
    // txtTim: Ô nhập/hiển thị văn bản tìm.
    private final JTextField txtTim = UIStyle.o(18);
    // txtMa: Ô nhập/hiển thị văn bản mã.
    private final JTextField txtMa = UIStyle.oChiDoc("(Tự động)");
    // txtTen: Ô nhập/hiển thị văn bản tên.
    private final JTextField txtTen = UIStyle.o(19);
    // txtGia: Ô nhập/hiển thị văn bản giá.
    private final JTextField txtGia = UIStyle.o(19);
    // txaMoTa: Vùng văn bản nhiều dòng mô tả.
    private final JTextArea txaMoTa = new JTextArea(4, 20);
    // lblSoLuong: Nhãn hiển thị số lượng.
    private final JLabel lblSoLuong = UIStyle.nhan("0 dịch vụ");
    // model: Mô hình lưu dữ liệu và cung cấp các ô cho bảng giao diện.
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã dịch vụ", "Tên dịch vụ", "Đơn giá", "Mô tả" }, 0) {
        // Không cho phép sửa trực tiếp các ô trong bảng.
        // Tham số row: chỉ số dòng.
        // Tham số col: chỉ số cột.
        // Kết quả: false, bảng chỉ dùng để xem.
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    // tblDichVu: Bảng hiển thị dữ liệu dịch vụ.
    private final JTable tblDichVu = new JTable(model);
    // cardHanhDong: Bộ bố trí cho phép chuyển giữa các màn hình trong cùng một vùng chứa.
    private final CardLayout cardHanhDong = new CardLayout();
    // pnlHanhDong: Khung chứa thành phần giao diện hành động.
    private final JPanel pnlHanhDong = new JPanel(cardHanhDong);
    // dsHienThi: Lưu danh sách đang hiển thị; phần tử có kiểu DichVu.
    private ArrayList<DichVu> dsHienThi = new ArrayList<>();
    // maDangSua: Lưu mã bản ghi đang sửa.
    private String maDangSua = null;
    // TIEN_VN: Bộ định dạng số/tiền dùng để hiển thị dữ liệu trên giao diện.
    private static final NumberFormat TIEN_VN = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    // Khởi tạo giao diện và thiết lập các thành phần, sự kiện ban đầu.
    public QuanLyDichVu() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UIStyle.LIGHT_BLUE);
        setBorder(new EmptyBorder(18, 22, 18, 22));
        taoGiaoDien();
        lamMoi();
    }

    // Tạo giao diện.
    private void taoGiaoDien() {
        // lblTieuDe: Nhãn hiển thị tiêu đề.
        JLabel lblTieuDe = UIStyle.nhan("QUẢN LÝ DỊCH VỤ SPA");
        lblTieuDe.setFont(UIStyle.font(Font.BOLD, 25));
        lblTieuDe.setForeground(new Color(18, 48, 82));
        // pnlTieuDe: Khung chứa thành phần giao diện tiêu đề.
        JPanel pnlTieuDe = new JPanel(new BorderLayout());
        pnlTieuDe.setOpaque(false);
        pnlTieuDe.add(lblTieuDe, BorderLayout.WEST);
        add(pnlTieuDe, BorderLayout.NORTH);

        // pnlBang: Khung chứa thành phần giao diện bảng dữ liệu.
        JPanel pnlBang = UIStyle.the();
        pnlBang.setLayout(new BorderLayout(0, 12));
        // btnTim: Nút thao tác tìm.
        JButton btnTim = UIStyle.nutChinh("Tìm kiếm");
        // btnLamMoi: Nút thao tác làm mới.
        JButton btnLamMoi = UIStyle.nutPhu("Làm mới");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnTim.addActionListener(e -> taiLai(maDangSua));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        txtTim.addActionListener(e -> taiLai(maDangSua));
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnLamMoi.addActionListener(e -> {
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtTim.setText("");
            boChon();
            taiLai(null);
        });
        // pnlTim: Khung chứa thành phần giao diện tìm.
        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTim.setOpaque(false);
        pnlTim.add(UIStyle.nhan("Mã / tên / mô tả:"));
        pnlTim.add(txtTim);
        pnlTim.add(btnTim);
        pnlTim.add(btnLamMoi);
        // pnlLoc: Khung chứa thành phần giao diện lọc.
        JPanel pnlLoc = new JPanel(new BorderLayout(0, 9));
        pnlLoc.setOpaque(false);
        pnlLoc.add(pnlTim, BorderLayout.NORTH);
        pnlLoc.add(lblSoLuong, BorderLayout.SOUTH);
        pnlBang.add(pnlLoc, BorderLayout.NORTH);

        UIStyle.taoBang(tblDichVu, 2);
        tblDichVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDichVu.getColumnModel().getColumn(0).setPreferredWidth(95);
        tblDichVu.getColumnModel().getColumn(1).setPreferredWidth(175);
        tblDichVu.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblDichVu.getColumnModel().getColumn(3).setPreferredWidth(220);
        tblDichVu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                napDongDangChon();
        });
        pnlBang.add(UIStyle.cuon(tblDichVu), BorderLayout.CENTER);

        // pnlForm: Khung chứa thành phần giao diện biểu mẫu.
        JPanel pnlForm = UIStyle.the();
        pnlForm.setLayout(new BorderLayout(0, 14));
        pnlForm.setPreferredSize(new Dimension(305, 100));
        // lblForm: Nhãn hiển thị biểu mẫu.
        JLabel lblForm = UIStyle.nhan("THÔNG TIN DỊCH VỤ");
        lblForm.setFont(UIStyle.font(Font.BOLD, 15));
        pnlForm.add(lblForm, BorderLayout.NORTH);

        txaMoTa.setFont(UIStyle.font(Font.PLAIN, 13));
        txaMoTa.setLineWrap(true);
        txaMoTa.setWrapStyleWord(true);
        txaMoTa.setBorder(new EmptyBorder(8, 8, 8, 8));
        // scrMoTa: Khung cuộn mô tả.
        JScrollPane scrMoTa = new JScrollPane(txaMoTa);
        scrMoTa.setPreferredSize(new Dimension(250, 95));
        txtGia.setToolTipText("Ví dụ: 150000 hoặc 150.000 (VNĐ)");
        txtMa.setToolTipText("Mã DV001... được sinh tự động khi thêm mới");

        // pnlNhap: Khung chứa thành phần giao diện nhập.
        JPanel pnlNhap = new JPanel();
        pnlNhap.setOpaque(false);
        pnlNhap.setLayout(new BoxLayout(pnlNhap, BoxLayout.Y_AXIS));
        themHang(pnlNhap, "Mã dịch vụ (tự động)", txtMa, 38);
        themHang(pnlNhap, "Tên dịch vụ *", txtTen, 38);
        themHang(pnlNhap, "Giá dịch vụ (VNĐ) *", txtGia, 38);
        themHang(pnlNhap, "Mô tả", scrMoTa, 96);
        pnlForm.add(pnlNhap, BorderLayout.CENTER);

        // Chưa chọn dịch vụ: chỉ có nút Thêm dịch vụ.
        // btnThem: Nút thao tác thêm.
        JButton btnThem = UIStyle.nutXanh("+ Thêm dịch vụ");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnThem.addActionListener(e -> them());
        // pnlThem: Khung chứa thành phần giao diện thêm.
        JPanel pnlThem = new JPanel(new GridLayout(1, 1));
        pnlThem.setOpaque(false);
        pnlThem.add(btnThem);

        // Đã chọn một dòng: ẩn hoàn toàn nút Thêm, chỉ hiện Lưu / Xóa / Bỏ chọn.
        // btnSua: Nút thao tác sửa.
        JButton btnSua = UIStyle.nutChinh("Lưu");
        // btnXoa: Nút thao tác xóa.
        JButton btnXoa = UIStyle.nutXoa("Xóa");
        // btnBoChon: Nút thao tác bỏ chọn.
        JButton btnBoChon = UIStyle.nutPhu("Bỏ chọn");
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnSua.addActionListener(e -> sua());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnXoa.addActionListener(e -> xoa());
        // Đăng ký hành động xử lý khi người dùng kích hoạt control này.
        btnBoChon.addActionListener(e -> boChon());
        // pnlSua: Khung chứa thành phần giao diện sửa.
        JPanel pnlSua = new JPanel(new GridLayout(1, 3, 7, 0));
        pnlSua.setOpaque(false);
        pnlSua.add(btnSua);
        pnlSua.add(btnXoa);
        pnlSua.add(btnBoChon);

        pnlHanhDong.setOpaque(false);
        pnlHanhDong.add(pnlThem, "THEM");
        pnlHanhDong.add(pnlSua, "SUA");
        pnlForm.add(pnlHanhDong, BorderLayout.SOUTH);
        capNhatNutHanhDong();

        // pnlGiua: Khung chứa thành phần giao diện giữa.
        JPanel pnlGiua = new JPanel(new BorderLayout(14, 0));
        pnlGiua.setOpaque(false);
        pnlGiua.add(pnlBang, BorderLayout.CENTER);
        pnlGiua.add(pnlForm, BorderLayout.EAST);
        add(pnlGiua, BorderLayout.CENTER);
    }

    // Thêm hàng.
    // Tham số pnlCha: thành phần pnlCha.
    // Tham số nhan: nhãn.
    // Tham số truong: trường nhập liệu.
    // Tham số cao: cao.
    private static void themHang(JPanel pnlCha, String nhan, JComponent truong, int cao) {
        // lblLbl: Nhãn hiển thị nhãn.
        JLabel lblLbl = UIStyle.nhan(nhan);
        lblLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        truong.setAlignmentX(Component.LEFT_ALIGNMENT);
        truong.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao));
        truong.setPreferredSize(new Dimension(250, cao));
        pnlCha.add(lblLbl);
        pnlCha.add(Box.createVerticalStrut(5));
        pnlCha.add(truong);
        pnlCha.add(Box.createVerticalStrut(12));
    }

    // Lấy dòng đang được chọn trên bảng.
    // Kết quả: đối tượng/giá trị kiểu DichVu.
    private DichVu dongDangChon() {
        int i = tblDichVu.getSelectedRow();
        return i >= 0 && i < dsHienThi.size() ? dsHienThi.get(i) : null;
    }

    // Nạp dòng mục đang chọn.
    private void napDongDangChon() {
        DichVu dv = dongDangChon();
        if (dv == null)
            return;
        maDangSua = dv.getMaDichVu();
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtMa.setText(dv.getMaDichVu());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtTen.setText(dv.getTenDichVu());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtGia.setText(dv.getGiaDichVu() == null ? "" : dv.getGiaDichVu().stripTrailingZeros().toPlainString());
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txaMoTa.setText(dv.getMoTa() == null ? "" : dv.getMoTa());
        capNhatNutHanhDong();
    }

    // Cập nhật nút hành động.
    private void capNhatNutHanhDong() {
        cardHanhDong.show(pnlHanhDong, maDangSua == null ? "THEM" : "SUA");
    }

    // Làm mới dữ liệu hoặc trạng thái của màn hình.
    public void lamMoi() {
        taiLai(maDangSua);
        if (maDangSua == null)
            hienMaDuKien();
    }

    // Tải lại danh sách và chọn lại bản ghi theo mã nếu có.
    // Tham số maCanChon: mã cần chọn.
    private void taiLai(String maCanChon) {
        try {
            dsHienThi = dao.timKiem(txtTim.getText());
            tblDichVu.clearSelection();
            model.setRowCount(0);
            int chon = -1;
            for (int i = 0; i < dsHienThi.size(); i++) {
                DichVu dv = dsHienThi.get(i);
                String gia = dv.getGiaDichVu() == null ? "Chưa có giá"
                        : TIEN_VN.format(dv.getGiaDichVu()) + " ₫";
                model.addRow(new Object[] { dv.getMaDichVu(), dv.getTenDichVu(), gia,
                        dv.getMoTa() == null ? "" : dv.getMoTa() });
                if (dv.getMaDichVu().equals(maCanChon))
                    chon = i;
            }
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            lblSoLuong.setText("Hiển thị " + dsHienThi.size() + " dịch vụ");
            if (chon >= 0) {
                tblDichVu.setRowSelectionInterval(chon, chon);
                tblDichVu.scrollRectToVisible(tblDichVu.getCellRect(chon, 0, true));
            } else if (maCanChon != null) {
                boChon();
            }
        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // Hiển thị mã đủ kiện.
    private void hienMaDuKien() {
        try {
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtMa.setText(dao.maDuKien());
        } catch (SQLException ex) {
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtMa.setText("(Tự động)");
        }
    }

    // Bỏ chọn.
    private void boChon() {
        tblDichVu.clearSelection();
        maDangSua = null;
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtTen.setText("");
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txtGia.setText("");
        // Cập nhật nội dung chữ hiển thị trong control bằng setText().
        txaMoTa.setText("");
        hienMaDuKien();
        capNhatNutHanhDong();
    }

    // Đọc và kiểm tra dữ liệu nhập trên biểu mẫu.
    // Kết quả: đối tượng/giá trị kiểu DichVu.
    private DichVu docForm() {
        // ten: Đọc nội dung chữ từ ô nhập để xử lý tên.
        String ten = txtTen.getText().trim();
        // moTa: Đọc nội dung chữ từ ô nhập để xử lý mô tả.
        String moTa = txaMoTa.getText().trim();
        if (ten.length() < 2 || ten.length() > 50) {
            UIStyle.canhBao(this, "Tên dịch vụ phải từ 2 đến 50 ký tự.");
            txtTen.requestFocus();
            return null;
        }
        if (moTa.length() > 255) {
            UIStyle.canhBao(this, "Mô tả không được quá 255 ký tự.");
            txaMoTa.requestFocus();
            return null;
        }
        // giaNhap: Đọc nội dung chữ từ ô nhập để xử lý giá nhập.
        String giaNhap = txtGia.getText().trim().replace(".", "").replace(" ", "");
        if (!giaNhap.matches("[0-9]{1,16}")) {
            UIStyle.canhBao(this, "Giá dịch vụ phải là số nguyên dương (VNĐ), ví dụ 150000.");
            txtGia.requestFocus();
            return null;
        }
        // gia: Giá trị giá dùng số thập phân chính xác để tính toán tiền/tỷ lệ.
        BigDecimal gia = new BigDecimal(giaNhap);
        if (gia.signum() <= 0 || gia.precision() > 16) {
            UIStyle.canhBao(this, "Giá dịch vụ phải lớn hơn 0 và vừa kiểu DECIMAL(18,2).");
            txtGia.requestFocus();
            return null;
        }
        return new DichVu(maDangSua, ten, gia, moTa.isEmpty() ? null : moTa);
    }

    // Xử lý yêu cầu thêm dữ liệu từ giao diện.
    private void them() {
        if (maDangSua != null) {
            UIStyle.canhBao(this, "Bạn đang chọn dịch vụ " + maDangSua + ". Hãy nhấn Bỏ chọn để thêm mới.");
            return;
        }
        DichVu dv = docForm();
        if (dv == null)
            return;
        try {
            // maMoi: Lưu mã mới.
            String maMoi = dao.them(dv);
            UIStyle.thongBao(this, "Đã thêm dịch vụ " + maMoi + ".");
            // Cập nhật nội dung chữ hiển thị trong control bằng setText().
            txtTim.setText("");
            taiLai(maMoi);
        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // Xử lý yêu cầu sửa bản ghi đang chọn.
    private void sua() {
        if (maDangSua == null) {
            UIStyle.canhBao(this, "Hãy chọn một dịch vụ trong bảng trước khi sửa.");
            return;
        }
        DichVu dv = docForm();
        if (dv == null)
            return;
        try {
            if (dao.sua(dv)) {
                UIStyle.thongBao(this, "Đã cập nhật dịch vụ " + maDangSua + ".");
                taiLai(maDangSua);
            } else
                UIStyle.canhBao(this, "Dịch vụ không còn tồn tại trong cơ sở dữ liệu.");
        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

    // Xử lý yêu cầu xóa bản ghi đang chọn.
    private void xoa() {
        DichVu dv = dongDangChon();
        if (dv == null) {
            UIStyle.canhBao(this, "Hãy chọn một dịch vụ cần xóa.");
            return;
        }
        try {
            // lienQuan: Mảng lưu liên quan.
            int[] lienQuan = dao.demLienQuan(dv.getMaDichVu());
            if (lienQuan[0] + lienQuan[1] + lienQuan[2] > 0) {
                UIStyle.canhBao(this, "Không thể xóa " + dv.getMaDichVu() + " vì đã được sử dụng trong:\n"
                        + "• Lịch hẹn Spa: " + lienQuan[0] + "\n"
                        + "• Phiếu dịch vụ: " + lienQuan[1] + "\n"
                        + "• Hóa đơn: " + lienQuan[2] + "\n"
                        + "Cần giữ dịch vụ để bảo toàn lịch sử. Bạn có thể sửa tên/giá/mô tả.");
                return;
            }
            if (!UIStyle.xacNhan(this, "Xóa dịch vụ " + dv.getMaDichVu() + " - " + dv.getTenDichVu() + "?"))
                return;
            if (dao.xoa(dv.getMaDichVu())) {
                UIStyle.thongBao(this, "Đã xóa dịch vụ " + dv.getMaDichVu() + ".");
                boChon();
                taiLai(null);
            } else
                UIStyle.canhBao(this, "Dịch vụ không còn tồn tại.");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 547) {
                UIStyle.canhBao(this, "Không thể xóa: dịch vụ đã được liên kết với dữ liệu nghiệp vụ.");
            } else
                UIStyle.loi(this, ex);
        }
    }

}
