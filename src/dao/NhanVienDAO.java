package dao;

import control.PhanQuyenControl;

import entity.NhanVien;

import java.sql.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

public class NhanVienDAO {

        private void kiemTraKichThuoc(NhanVien tk) throws SQLException {
                kiemTraDoDai("Mã nhân viên", tk.getMaNhanVien(), 10, true);
                kiemTraDoDai("Họ tên", tk.getHoTen(), 50, false);
                kiemTraDoDai("Email", tk.getEmail(), 30, false);
                kiemTraDoDai("Mật khẩu", tk.getMatKhau(), 255, false);
                kiemTraDoDai("Giới tính", tk.getGioiTinh(), 10, false);
                kiemTraDoDai("Vai trò", tk.getVaiTro(), 50, false);
        }

        private void kiemTraDoDai(String tenTruong, String giaTri, int gioiHan, boolean batBuoc)
                        throws SQLException {
                if ((giaTri == null || giaTri.isBlank()) && batBuoc)
                        throw new SQLException(tenTruong + " không được để trống.");
                if (giaTri != null && giaTri.length() > gioiHan)
                        throw new SQLException(tenTruong + " không được vượt quá " + gioiHan + " ký tự.");
        }

        public NhanVien xacThuc(String tenDangNhap, String matKhau) throws SQLException {

                String sql = "SELECT maNhanVien, hoTen, soDienThoai, email, matKhau, gioiTinh, vaiTro FROM NhanVien";

                try (Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {

                                String ma = rs.getString("maNhanVien");

                                String ten = rs.getString("hoTen");
                                if (ma == null || ten == null || ma.length() < 3)
                                        continue;
                                String normalized = java.text.Normalizer.normalize(ten, java.text.Normalizer.Form.NFD)
                                                .replaceAll("\\p{M}", "").replace('đ', 'd').replace('Đ', 'D')
                                                .toLowerCase(java.util.Locale.ROOT).replaceAll("\\s+", "")
                                                + ma.substring(ma.length() - 3);

                                if ((normalized.equalsIgnoreCase(tenDangNhap)
                                                || ma.equalsIgnoreCase(tenDangNhap))
                                                && rs.getString("matKhau") != null
                                                && rs.getString("matKhau").equals(matKhau)) {
                                        return new NhanVien(ma, ten, rs.getInt("soDienThoai"), rs.getString("email"),
                                                        rs.getString("matKhau"), chuanHoaGioiTinh(rs.getString("gioiTinh")),
                                                        rs.getString("vaiTro"));
                                }
                        }
                }
                return null;
        }

        public ArrayList<NhanVien> getAll() {
                PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.TAI_KHOAN);

                ArrayList<NhanVien> list = new ArrayList<>();

                String sql = "SELECT maNhanVien, hoTen, "
                                + "soDienThoai, email, matKhau, "
                                + "gioiTinh, vaiTro "
                                + "FROM NhanVien "
                                + "ORDER BY maNhanVien";

                try (
                                Connection con = ConnectDB.connectDB.getConnection();

                                PreparedStatement ps = con.prepareStatement(sql);

                                ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {

                                NhanVien tk = new NhanVien(
                                                rs.getString("maNhanVien"),
                                                rs.getString("hoTen"),
                                                rs.getInt("soDienThoai"),
                                                rs.getString("email"),
                                                rs.getString("matKhau"),
                                                chuanHoaGioiTinh(rs.getString("gioiTinh")),
                                                rs.getString("vaiTro"));

                                list.add(tk);
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list;
        }

        public boolean them(NhanVien tk) throws SQLException {
                PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.TAI_KHOAN);
                kiemTraKichThuoc(tk);

                String sql = "INSERT INTO NhanVien "
                                + "(maNhanVien, hoTen, soDienThoai, email, matKhau, gioiTinh, vaiTro) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        con.setAutoCommit(false);
                        try {

                                String maMoi = taoMaTrongGiaoDich(con);
                                try (PreparedStatement ps = con.prepareStatement(sql)) {
                                        ps.setString(1, maMoi);
                                        ps.setString(2, tk.getHoTen());
                                        ps.setInt(3, tk.getSoDienThoai());
                                        ps.setString(4, tk.getEmail());
                                        ps.setString(5, tk.getMatKhau());
                                        ps.setString(6, tk.getGioiTinh());
                                        ps.setString(7, tk.getVaiTro());
                                        if (ps.executeUpdate() != 1) {
                                                throw new SQLException("KhÃ´ng thá»ƒ thÃªm nhÃ¢n viÃªn.");
                                        }
                                }

                                con.commit();
                                tk.setMaNhanVien(maMoi);
                                return true;
                        } catch (SQLException ex) {

                                con.rollback();
                                throw ex;
                        }
                }
        }

        public boolean sua(
                        NhanVien tk) {
                PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.TAI_KHOAN);
                try {
                        kiemTraKichThuoc(tk);
                } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                }

                String sql = "UPDATE NhanVien SET "
                                + "hoTen = ?, "
                                + "soDienThoai = ?, "
                                + "email = ?, "
                                + "matKhau = ?, "
                                + "gioiTinh = ?, "
                                + "vaiTro = ? "
                                + "WHERE maNhanVien = ?";
                try (
                                Connection con = ConnectDB.connectDB.getConnection();

                                PreparedStatement ps = con.prepareStatement(sql)) {

                        ps.setString(1, tk.getHoTen());

                        ps.setInt(2, tk.getSoDienThoai());

                        ps.setString(3, tk.getEmail());

                        ps.setString(4, tk.getMatKhau());

                        ps.setString(5, tk.getGioiTinh());

                        ps.setString(6, tk.getVaiTro());

                        ps.setString(7, tk.getMaNhanVien());
                        return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                }
        }

        public boolean xoa(
                        String maNhanVien) {
                PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.TAI_KHOAN);

                String sql = "DELETE FROM NhanVien "
                                + "WHERE maNhanVien = ?";
                try (
                                Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, maNhanVien);
                        return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                }
        }

        public boolean xacMinhThongTinNhanVien(String maNhanVien, String hoTen,
                        String vaiTro, String email, String soDienThoai) throws SQLException {

                String sql = "SELECT hoTen, vaiTro, email, soDienThoai "
                                + "FROM NhanVien WHERE maNhanVien = ?";

                try (Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, maNhanVien.trim());

                        try (ResultSet rs = ps.executeQuery()) {
                                if (!rs.next())
                                        return false;
                                return chuanHoa(rs.getString("hoTen")).equals(chuanHoa(hoTen))
                                                && chuanHoa(rs.getString("vaiTro")).equals(chuanHoa(vaiTro))
                                                && chuanHoaEmail(rs.getString("email")).equals(chuanHoaEmail(email))
                                                && rs.getInt("soDienThoai") == chuanHoaSoDienThoai(soDienThoai);
                        }
                }
        }

        public boolean capLaiMatKhauCoPheDuyet(String maNV, String maQuanLy,
                        String matKhauQuanLy, String matKhauMoi) throws SQLException {
                PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.TAI_KHOAN);
                if (maNV == null || maNV.trim().isEmpty() || maQuanLy == null
                                || maQuanLy.trim().isEmpty() || matKhauQuanLy == null
                                || matKhauQuanLy.isEmpty() || matKhauMoi == null
                                || matKhauMoi.length() < 6) {
                        return false;
                }

                String sql = "UPDATE NhanVien SET matKhau = ? "
                                + "WHERE maNhanVien = ? AND EXISTS ( "
                                + " SELECT 1 FROM NhanVien q WHERE q.maNhanVien = ? "
                                + " AND q.matKhau = ? "
                                + " AND (LOWER(q.vaiTro) = N'quan ly' OR LOWER(q.vaiTro) = N'quáº£n lÃ½') )";

                try (Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, matKhauMoi);
                        ps.setString(2, maNV.trim());
                        ps.setString(3, maQuanLy.trim());
                        ps.setString(4, matKhauQuanLy);
                        return ps.executeUpdate() == 1;
                }
        }

        public boolean capLaiMatKhau(String maNhanVien, String matKhauMoi) throws SQLException {
                PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.TAI_KHOAN);
                if (maNhanVien == null || maNhanVien.trim().isEmpty()
                                || matKhauMoi == null || matKhauMoi.length() < 6) {
                        throw new IllegalArgumentException("Ma NV hoac mat khau moi khong hop le.");
                }

                String sql = "UPDATE NhanVien SET matKhau = ? WHERE maNhanVien = ?";

                try (Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, matKhauMoi);
                        ps.setString(2, maNhanVien.trim());
                        return ps.executeUpdate() == 1;
                }
        }

        public boolean capLaiMatKhauQuaTaiKhoanQuanLy(String maNhanVienCanKhoiPhuc,
                        String taiKhoanQuanLy, String matKhauQuanLy, String matKhauTamThoi)
                        throws SQLException {
                PhanQuyenControl.yeuCau(PhanQuyenControl.Quyen.TAI_KHOAN);
                if (maNhanVienCanKhoiPhuc == null || maNhanVienCanKhoiPhuc.trim().isEmpty()
                                || taiKhoanQuanLy == null || taiKhoanQuanLy.trim().isEmpty()
                                || matKhauQuanLy == null || matKhauQuanLy.isEmpty()
                                || matKhauTamThoi == null || matKhauTamThoi.length() < 6) {
                        return false;
                }

                String sqlQuanLy = "SELECT maNhanVien, hoTen, vaiTro FROM NhanVien WHERE matKhau = ?";

                String sqlCapLai = "UPDATE NhanVien SET matKhau = ? WHERE maNhanVien = ?";

                try (Connection con = ConnectDB.connectDB.getConnection()) {

                        con.setAutoCommit(false);
                        try {
                                boolean blnDungQuanLy = false;
                                try (PreparedStatement psQuanLy = con.prepareStatement(sqlQuanLy)) {
                                        psQuanLy.setString(1, matKhauQuanLy);

                                        try (ResultSet rs = psQuanLy.executeQuery()) {

                                                while (rs.next()) {
                                                        String strMaQuanLy = rs.getString("maNhanVien");
                                                        String strVaiTro = rs.getString("vaiTro");
                                                        if (!"quan ly".equals(chuanHoa(strVaiTro)))
                                                                continue;
                                                        String strTenDangNhap = taoTaiKhoanKhongDau(
                                                                        rs.getString("hoTen"), strMaQuanLy);
                                                        String strTaiKhoanNhap = taiKhoanQuanLy.trim();
                                                        if (strMaQuanLy.equalsIgnoreCase(strTaiKhoanNhap)
                                                                        || strTenDangNhap.equalsIgnoreCase(
                                                                                        strTaiKhoanNhap)) {
                                                                blnDungQuanLy = true;
                                                                break;
                                                        }
                                                }
                                        }
                                }
                                if (!blnDungQuanLy) {

                                        con.rollback();
                                        return false;
                                }

                                int intSoDong;
                                try (PreparedStatement psCapLai = con.prepareStatement(sqlCapLai)) {
                                        psCapLai.setString(1, matKhauTamThoi);
                                        psCapLai.setString(2, maNhanVienCanKhoiPhuc.trim());
                                        intSoDong = psCapLai.executeUpdate();
                                }
                                if (intSoDong != 1) {

                                        con.rollback();
                                        return false;
                                }

                                con.commit();
                                return true;
                        } catch (SQLException | RuntimeException ex) {

                                con.rollback();
                                throw ex;
                        }
                }
        }

        public boolean doiMatKhau(String maNhanVien, String matKhauCu,
                        String matKhauMoi) throws SQLException {

                if (maNhanVien == null || control.PhienLamViecControl.maNV == null
                                || !maNhanVien.trim().equals(control.PhienLamViecControl.maNV)) {
                        throw new SecurityException("Chá»‰ Ä‘Æ°á»£c Ä‘á»•i máº­t kháº©u cá»§a tÃ i khoáº£n Ä‘ang Ä‘Äƒng nháº­p.");
                }

                if (maNhanVien == null || maNhanVien.isBlank()
                                || matKhauCu == null || matKhauCu.isEmpty()
                                || matKhauMoi == null || matKhauMoi.length() < 6
                                || matKhauCu.equals(matKhauMoi)) {
                        return false;
                }

                String sql = "UPDATE NhanVien SET matKhau = ? "
                                + "WHERE maNhanVien = ? "
                                + "AND matKhau COLLATE Latin1_General_100_BIN2 = ?";

                try (Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, matKhauMoi);
                        ps.setString(2, maNhanVien.trim());
                        ps.setString(3, matKhauCu);
                        return ps.executeUpdate() == 1;
                }
        }

        private static String taoTaiKhoanKhongDau(String hoTen, String maNhanVien) {
                if (hoTen == null || maNhanVien == null || maNhanVien.length() < 3)
                        return "";
                String strTen = Normalizer.normalize(hoTen, Normalizer.Form.NFD)
                                .replaceAll("\\p{M}", "")
                                .replace('đ', 'd').replace('Đ', 'D')
                                .toLowerCase(Locale.ROOT)
                                .replaceAll("\\s+", "");
                return strTen + maNhanVien.substring(maNhanVien.length() - 3);
        }

        private static String chuanHoa(String str) {
                if (str == null)
                        return "";

                String s = Normalizer.normalize(str.trim(), Normalizer.Form.NFD)
                                .replaceAll("\\p{M}", "")
                                .replace('đ', 'd').replace('Đ', 'D')
                                .toLowerCase(Locale.ROOT);
                return s.replaceAll("\\s+", " ");
        }

        private static String chuanHoaEmail(String str) {
                return str == null ? "" : str.trim().toLowerCase(Locale.ROOT);
        }

        private static int chuanHoaSoDienThoai(String str) {
                String giaTri = str == null ? "" : str.replaceAll("[\\s.-]", "");
                if (!giaTri.matches("0\\d{9}")) {
                        return -1;
                }
                try {
                        return Integer.parseInt(giaTri.substring(1));
                } catch (NumberFormatException ex) {
                        return -1;
                }
        }

        private static String chuanHoaGioiTinh(String gioiTinh) {
                String giaTri = gioiTinh == null ? "" : gioiTinh.trim();
                if ("Nu".equalsIgnoreCase(giaTri) || "Nữ".equals(giaTri)) {
                        return "Nữ";
                }
                return giaTri;
        }

        public String goiYMa() {
                try (Connection con = ConnectDB.connectDB.getConnection()) {
                        return taoMa(con, false);
                } catch (SQLException ex) {
                        return "Tự động khi lưu";
                }
        }

        private static String taoMaTrongGiaoDich(Connection con) throws SQLException {
                if (con.getAutoCommit()) {
                        throw new SQLException("Phải bắt đầu transaction trước khi sinh mã nhân viên.");
                }
                return taoMa(con, true);
        }

        private static String taoMa(Connection con, boolean khoaBang) throws SQLException {
                String sql = "SELECT COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maNhanVien, 3, 3))), 0) "
                                + "FROM dbo.NhanVien" + (khoaBang ? " WITH (TABLOCKX, HOLDLOCK)" : "")
                                + " WHERE maNhanVien LIKE 'NV[0-9][0-9][0-9]'";
                try (PreparedStatement ps = con.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery()) {
                        if (!rs.next())
                                throw new SQLException("Không đọc được mã nhân viên mới.");
                        int tiepTheo = rs.getInt(1) + 1;
                        if (tiepTheo > 999)
                                throw new SQLException("Đã hết mã nhân viên trong giới hạn hiện tại.");
                        return String.format(java.util.Locale.ROOT, "NV%03d", tiepTheo);
                }
        }
}
