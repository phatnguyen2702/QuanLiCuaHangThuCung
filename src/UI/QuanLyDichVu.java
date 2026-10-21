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

    private final DichVuDAO dao = new DichVuDAO();
    private final JTextField txtTim = UIStyle.o(18);
    private final JTextField txtMa = UIStyle.oChiDoc("(Tự động)");
    private final JTextField txtTen = UIStyle.o(19);
    private final JTextField txtGia = UIStyle.o(19);
    private final JTextArea txtMoTa = new JTextArea(4, 20);
    private final JLabel lblSoLuong = UIStyle.nhan("0 dịch vụ");
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã dịch vụ", "Tên dịch vụ", "Đơn giá", "Mô tả" }, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable tblDichVu = new JTable(model);
    private final CardLayout cardHanhDong = new CardLayout();
    private final JPanel pnlHanhDong = new JPanel(cardHanhDong);
    private ArrayList<DichVu> dsHienThi = new ArrayList<>();
    private String maDangSua = null;
    private static final NumberFormat TIEN_VN = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    public QuanLyDichVu() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UIStyle.LIGHT_BLUE);
        setBorder(new EmptyBorder(18, 22, 18, 22));
        taoGiaoDien();
        lamMoi();
    }

    private void taoGiaoDien() {
        JLabel lblTieuDe = UIStyle.nhan("QUẢN LÝ DỊCH VỤ SPA");
        lblTieuDe.setFont(UIStyle.font(Font.BOLD, 25));
        lblTieuDe.setForeground(new Color(18, 48, 82));
        JPanel pnlTieuDe = new JPanel(new BorderLayout());
        pnlTieuDe.setOpaque(false);
        pnlTieuDe.add(lblTieuDe, BorderLayout.WEST);
        add(pnlTieuDe, BorderLayout.NORTH);

        JPanel pnlBang = UIStyle.the();
        pnlBang.setLayout(new BorderLayout(0, 12));
        JButton btnTim = UIStyle.nutChinh("Tìm kiếm");
        JButton btnLamMoi = UIStyle.nutPhu("Làm mới");
        btnTim.addActionListener(e -> taiLai(maDangSua));
        txtTim.addActionListener(e -> taiLai(maDangSua));
        btnLamMoi.addActionListener(e -> {
            txtTim.setText("");
            boChon();
            taiLai(null);
        });
        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTim.setOpaque(false);
        pnlTim.add(UIStyle.nhan("Mã / tên / mô tả:"));
        pnlTim.add(txtTim);
        pnlTim.add(btnTim);
        pnlTim.add(btnLamMoi);
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

        JPanel pnlForm = UIStyle.the();
        pnlForm.setLayout(new BorderLayout(0, 14));
        pnlForm.setPreferredSize(new Dimension(305, 100));
        JLabel lblForm = UIStyle.nhan("THÔNG TIN DỊCH VỤ");
        lblForm.setFont(UIStyle.font(Font.BOLD, 15));
        pnlForm.add(lblForm, BorderLayout.NORTH);

        txtMoTa.setFont(UIStyle.font(Font.PLAIN, 13));
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        txtMoTa.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scrMoTa = new JScrollPane(txtMoTa);
        scrMoTa.setPreferredSize(new Dimension(250, 95));
        txtGia.setToolTipText("Ví dụ: 150000 hoặc 150.000 (VNĐ)");
        txtMa.setToolTipText("Mã DV001... được sinh tự động khi thêm mới");

        JPanel pnlNhap = new JPanel();
        pnlNhap.setOpaque(false);
        pnlNhap.setLayout(new BoxLayout(pnlNhap, BoxLayout.Y_AXIS));
        themHang(pnlNhap, "Mã dịch vụ (tự động)", txtMa, 38);
        themHang(pnlNhap, "Tên dịch vụ *", txtTen, 38);
        themHang(pnlNhap, "Giá dịch vụ (VNĐ) *", txtGia, 38);
        themHang(pnlNhap, "Mô tả", scrMoTa, 96);
        pnlForm.add(pnlNhap, BorderLayout.CENTER);

        // Chưa chọn dịch vụ: chỉ có nút Thêm dịch vụ.
        JButton btnThem = UIStyle.nutXanh("+ Thêm dịch vụ");
        btnThem.addActionListener(e -> them());
        JPanel pnlThem = new JPanel(new GridLayout(1, 1));
        pnlThem.setOpaque(false);
        pnlThem.add(btnThem);

        // Đã chọn một dòng: ẩn hoàn toàn nút Thêm, chỉ hiện Lưu / Xóa / Bỏ chọn.
        JButton btnSua = UIStyle.nutChinh("Lưu");
        JButton btnXoa = UIStyle.nutXoa("Xóa");
        JButton btnBoChon = UIStyle.nutPhu("Bỏ chọn");
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnBoChon.addActionListener(e -> boChon());
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

        JPanel pnlGiua = new JPanel(new BorderLayout(14, 0));
        pnlGiua.setOpaque(false);
        pnlGiua.add(pnlBang, BorderLayout.CENTER);
        pnlGiua.add(pnlForm, BorderLayout.EAST);
        add(pnlGiua, BorderLayout.CENTER);
    }

    private static void themHang(JPanel cha, String nhan, JComponent truong, int cao) {
        JLabel lbl = UIStyle.nhan(nhan);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        truong.setAlignmentX(Component.LEFT_ALIGNMENT);
        truong.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao));
        truong.setPreferredSize(new Dimension(250, cao));
        cha.add(lbl);
        cha.add(Box.createVerticalStrut(5));
        cha.add(truong);
        cha.add(Box.createVerticalStrut(12));
    }

    private DichVu dongDangChon() {
        int i = tblDichVu.getSelectedRow();
        return i >= 0 && i < dsHienThi.size() ? dsHienThi.get(i) : null;
    }

    private void napDongDangChon() {
        DichVu dv = dongDangChon();
        if (dv == null)
            return;
        maDangSua = dv.getMaDichVu();
        txtMa.setText(dv.getMaDichVu());
        txtTen.setText(dv.getTenDichVu());
        txtGia.setText(dv.getGiaDichVu() == null ? "" : dv.getGiaDichVu().stripTrailingZeros().toPlainString());
        txtMoTa.setText(dv.getMoTa() == null ? "" : dv.getMoTa());
        capNhatNutHanhDong();
    }

    private void capNhatNutHanhDong() {
        cardHanhDong.show(pnlHanhDong, maDangSua == null ? "THEM" : "SUA");
    }

    public void lamMoi() {
        taiLai(maDangSua);
        if (maDangSua == null)
            hienMaDuKien();
    }

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

    private void hienMaDuKien() {
        try {
            txtMa.setText(dao.maDuKien());
        } catch (SQLException ex) {
            txtMa.setText("(Tự động)");
        }
    }

    private void boChon() {
        tblDichVu.clearSelection();
        maDangSua = null;
        txtTen.setText("");
        txtGia.setText("");
        txtMoTa.setText("");
        hienMaDuKien();
        capNhatNutHanhDong();
    }

    private DichVu docForm() {
        String ten = txtTen.getText().trim();
        String moTa = txtMoTa.getText().trim();
        if (ten.length() < 2 || ten.length() > 50) {
            UIStyle.canhBao(this, "Tên dịch vụ phải từ 2 đến 50 ký tự.");
            txtTen.requestFocus();
            return null;
        }
        if (moTa.length() > 255) {
            UIStyle.canhBao(this, "Mô tả không được quá 255 ký tự.");
            txtMoTa.requestFocus();
            return null;
        }
        String giaNhap = txtGia.getText().trim().replace(".", "").replace(" ", "");
        if (!giaNhap.matches("[0-9]{1,16}")) {
            UIStyle.canhBao(this, "Giá dịch vụ phải là số nguyên dương (VNĐ), ví dụ 150000.");
            txtGia.requestFocus();
            return null;
        }
        BigDecimal gia = new BigDecimal(giaNhap);
        if (gia.signum() <= 0 || gia.precision() > 16) {
            UIStyle.canhBao(this, "Giá dịch vụ phải lớn hơn 0 và vừa kiểu DECIMAL(18,2).");
            txtGia.requestFocus();
            return null;
        }
        return new DichVu(maDangSua, ten, gia, moTa.isEmpty() ? null : moTa);
    }

    private void them() {
        if (maDangSua != null) {
            UIStyle.canhBao(this, "Bạn đang chọn dịch vụ " + maDangSua + ". Hãy nhấn Bỏ chọn để thêm mới.");
            return;
        }
        DichVu dv = docForm();
        if (dv == null)
            return;
        try {
            String maMoi = dao.them(dv);
            UIStyle.thongBao(this, "Đã thêm dịch vụ " + maMoi + ".");
            txtTim.setText("");
            taiLai(maMoi);
        } catch (SQLException ex) {
            UIStyle.loi(this, ex);
        }
    }

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

    private void xoa() {
        DichVu dv = dongDangChon();
        if (dv == null) {
            UIStyle.canhBao(this, "Hãy chọn một dịch vụ cần xóa.");
            return;
        }
        try {
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
