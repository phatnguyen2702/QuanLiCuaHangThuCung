package dao;

import control.PhanQuyenControl;
import entity.NhanVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

public class NhanVienDAO {

    private void kiemTraKichThuoc(NhanVien nv) throws SQLException {
        kiemTraDoDai("Mã nhân viên", nv.getMaNhanVien(), 10, true);
        kiemTraDoDai("Họ tên", nv.getHoTen(), 50, false);
        kiemTraDoDai("Email", nv.getEmail(), 30, false);
        kiemTraDoDai("Mật khẩu", nv.getMatKhau(), 255, false);
        kiemTraDoDai("Giới tính", nv.getGioiTinh(), 10, false);
        kiemTraDoDai("Vai trò", nv.getVaiTro(), 50, false);
    }

    private void kiemTraDoDai(String tenTruong, String giaTri, int gioiHan, boolean batBuoc)
            throws SQLException {
        if ((giaTri == null || giaTri.isBlank()) && batBuoc)
            throw new SQLException(tenTruong + " không được để trống.");
        if (giaTri != null && giaTri.length() > gioiHan)
            throw new SQLException(tenTruong + " không được vượt quá " + gioiHan + " ký tự.");
    }

    private NhanVien docNhanVien(ResultSet rs) throws SQLException {
        return new NhanVien(
                rs.getString("maNhanVien"),
                rs.getString("hoTen"),
                rs.getString("soDienThoai"),
                rs.getString("email"),
                rs.getString("matKhau"),
                rs.getString("gioiTinh"),
                rs.getString("vaiTro"));
    }

    private void ganThongTinNhanVien(PreparedStatement ps, NhanVien nv)
            throws SQLException {
        ps.setString(1, nv.getHoTen());
        ps.setString(2, nv.getSoDienThoai());
        ps.setString(3, nv.getEmail());
        ps.setString(4, nv.getMatKhau());
        ps.setString(5, nv.getGioiTinh());
        ps.setString(6, nv.getVaiTro());
        ps.setString(7, nv.getMaNhanVien());
    }

    public ArrayList<NhanVien> getAll() {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.TAI_KHOAN);
        ArrayList<NhanVien> list = new ArrayList<>();
        String sql = "SELECT maNhanVien, hoTen, soDienThoai, email, matKhau, gioiTinh, vaiTro FROM NhanVien ORDER BY maNhanVien";

        try (Connection con = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(docNhanVien(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean them(NhanVien nv) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.TAI_KHOAN);
        kiemTraKichThuoc(nv);
        String sql = "INSERT INTO NhanVien(maNhanVien, hoTen, soDienThoai, email, matKhau, gioiTinh, vaiTro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConnectDB.connectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String maMoi = taoMa(con);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, maMoi);
                    ps.setString(2, nv.getHoTen());
                    ps.setString(3, nv.getSoDienThoai());
                    ps.setString(4, nv.getEmail());
                    ps.setString(5, nv.getMatKhau());
                    ps.setString(6, nv.getGioiTinh());
                    ps.setString(7, nv.getVaiTro());
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Không thể thêm nhân viên.");
                }
                con.commit();
                nv.setMaNhanVien(maMoi);
                return true;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public boolean sua(NhanVien nv) {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.TAI_KHOAN);
        try {
            kiemTraKichThuoc(nv);
            String sql = "UPDATE NhanVien SET hoTen=?, soDienThoai=?, email=?, matKhau=?, gioiTinh=?, vaiTro=? "
                    + "WHERE maNhanVien=?";
            try (Connection con = ConnectDB.connectDB.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)) {
                ganThongTinNhanVien(ps, nv);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String maNhanVien) throws SQLException {
        PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.TAI_KHOAN);
        String sql = "DELETE FROM NhanVien WHERE maNhanVien=?";
        try (Connection con = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean doiMatKhau(String maNhanVien, String matKhauCu, String matKhauMoi)
            throws SQLException {
        String maDangNhap = control.TrangChuControl.maNV;
        if (maNhanVien == null || maDangNhap == null)
            return false;
        String maCanDoi = maNhanVien.trim();
        if (!maCanDoi.equals(maDangNhap))
            throw new SecurityException("Chỉ được đổi mật khẩu của tài khoản đang đăng nhập.");

        if (maCanDoi.isEmpty() || matKhauCu == null || matKhauCu.isEmpty()
                || matKhauMoi == null || matKhauMoi.length() < 6 || matKhauCu.equals(matKhauMoi))
            return false;
        String sql = "UPDATE NhanVien SET matKhau=? WHERE maNhanVien=? "
                + "AND matKhau COLLATE Latin1_General_100_BIN2=?";
        try (Connection con = ConnectDB.connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matKhauMoi);
            ps.setString(2, maCanDoi);
            ps.setString(3, matKhauCu);
            return ps.executeUpdate() == 1;
        }
    }

    public String goiYMa() {
        try (Connection con = ConnectDB.connectDB.getConnection()) {
            return taoMa(con);
        } catch (SQLException ex) {
            return "Tự động khi lưu";
        }
    }

    private static String taoMa(Connection con) throws SQLException {
        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maNhanVien, 3, 3))), 0) "
                + "FROM dbo.NhanVien "
                + "WHERE maNhanVien LIKE 'NV[0-9][0-9][0-9]'";
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (!rs.next())
                throw new SQLException("Không đọc được mã nhân viên mới.");
            int tiepTheo = rs.getInt(1) + 1;
            return String.format(Locale.ROOT, "NV%03d", tiepTheo);
        }
    }
}
