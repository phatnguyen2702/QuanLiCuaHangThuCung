package UI;

import UI.TrangChuUI.UIStyle;
import control.DichVuControl;
import entity.DichVu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class QuanLyDichVuUI extends JPanel implements ActionListener {

    private JButton btnTim, btnLamMoi, btnThem, btnSua, btnXoa, btnBoChon;

    private final DichVuControl control = new DichVuControl();
    private final JTextField txtTim = UIStyle.o(18);
    private final JTextField txtMa = UIStyle.oChiDoc("(Tự động)");
    private final JTextField txtTen = UIStyle.o(19);
    private final JTextField txtGia = UIStyle.o(19);

    private final JTextArea txaMoTa = new JTextArea(4, 20);
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

    public QuanLyDichVuUI() {
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
        btnTim = UIStyle.nutChinh("Tìm kiếm");
        btnLamMoi = UIStyle.nutPhu("Làm mới");
        btnTim.addActionListener(this);
        txtTim.addActionListener(this);
        btnLamMoi.addActionListener(this);
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

        tblDichVu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                napDongDangChon();
            }
        });
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

        txaMoTa.setFont(UIStyle.font(Font.PLAIN, 13));
        txaMoTa.setLineWrap(true);
        txaMoTa.setWrapStyleWord(true);
        txaMoTa.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scrMoTa = new JScrollPane(txaMoTa);
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

        btnThem = UIStyle.nutXanh("+ Thêm dịch vụ");
        btnThem.addActionListener(this);
        JPanel pnlThem = new JPanel(new GridLayout(1, 1));
        pnlThem.setOpaque(false);
        pnlThem.add(btnThem);

        btnSua = UIStyle.nutChinh("Lưu");
        btnXoa = UIStyle.nutXoa("Xóa");
        btnBoChon = UIStyle.nutPhu("Bỏ chọn");
        btnSua.addActionListener(this);
        btnXoa.addActionListener(this);
        btnBoChon.addActionListener(this);
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

    private static void themHang(JPanel pnlCha, String nhan, JComponent truong, int cao) {
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
        txaMoTa.setText(dv.getMoTa() == null ? "" : dv.getMoTa());
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
            dsHienThi = control.timKiem(txtTim.getText());
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
            txtMa.setText(control.maDuKien());
        } catch (SQLException ex) {
            txtMa.setText("(Tự động)");
        }
    }

    private void boChon() {
        tblDichVu.clearSelection();
        maDangSua = null;
        txtTen.setText("");
        txtGia.setText("");
        txaMoTa.setText("");
        hienMaDuKien();
        capNhatNutHanhDong();
    }

    private DichVu docForm() {
        try {
            return control.taoDichVu(maDangSua, txtTen.getText(), txtGia.getText(), txaMoTa.getText());
        } catch (IllegalArgumentException ex) {
            UIStyle.canhBao(this, ex.getMessage());
            if (ex.getMessage().startsWith("Tên"))
                txtTen.requestFocus();
            else if (ex.getMessage().startsWith("Mô tả"))
                txaMoTa.requestFocus();
            else
                txtGia.requestFocus();
            return null;
        }
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
            String maMoi = control.them(dv);
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
            if (control.sua(dv)) {
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
            String lyDo = control.lyDoKhongTheXoa(dv.getMaDichVu());
            if (lyDo != null) {
                UIStyle.canhBao(this, lyDo);
                return;
            }
            if (!UIStyle.xacNhan(this, "Xóa dịch vụ " + dv.getMaDichVu() + " - " + dv.getTenDichVu() + "?"))
                return;
            if (control.xoa(dv.getMaDichVu())) {
                UIStyle.thongBao(this, "Đã xóa dịch vụ " + dv.getMaDichVu() + ".");
                boChon();
                taiLai(null);
            } else
                UIStyle.canhBao(this, "Dịch vụ không còn tồn tại.");
        } catch (IllegalStateException ex) {
            UIStyle.canhBao(this, ex.getMessage());
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 547) {
                UIStyle.canhBao(this, "Không thể xóa: dịch vụ đã được liên kết với dữ liệu nghiệp vụ.");
            } else
                UIStyle.loi(this, ex);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object nguon = e.getSource();
        if (nguon == btnTim || nguon == txtTim) {
            taiLai(maDangSua);
        } else if (nguon == btnLamMoi) {
            txtTim.setText("");
            boChon();
            taiLai(null);
        } else if (nguon == btnThem) {
            them();
        } else if (nguon == btnSua) {
            sua();
        } else if (nguon == btnXoa) {
            xoa();
        } else if (nguon == btnBoChon) {
            boChon();
        }
    }

}
