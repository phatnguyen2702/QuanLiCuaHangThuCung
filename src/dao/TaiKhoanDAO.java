package dao;

import entity.TaiKhoan;

import java.sql.*;
import java.util.ArrayList;

public class TaiKhoanDAO {

        public boolean doiMatKhau(String maNhanVien, String matKhauCu, String matKhauMoi) throws SQLException {
                if (maNhanVien == null || maNhanVien.trim().isEmpty() || matKhauCu == null
                                || matKhauMoi == null || matKhauMoi.length() < 6 || matKhauCu.equals(matKhauMoi)) {
                        return false;
                }
                String sql = "UPDATE NhanVien SET matKhau = ? WHERE maNhanVien = ? AND matKhau COLLATE Latin1_General_100_BIN2 = ?";
                try (Connection con = ConnectDB.connectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, matKhauMoi);
                        ps.setString(2, maNhanVien.trim());
                        ps.setString(3, matKhauCu);
                        return ps.executeUpdate() == 1;
                }
        }

        /** Xác thực bằng tài khoản suy ra từ họ tên + 3 số cuối mã nhân viên. */
        public TaiKhoan xacThuc(String tenDangNhap, String matKhau) throws SQLException {
                String sql = "SELECT maNhanVien, hoTen, soDienThoai, email, matKhau, gioiTinh, vaiTro FROM NhanVien";
                try (Connection con = ConnectDB.connectDB.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                                String ma = rs.getString("maNhanVien");
                                String ten = rs.getString("hoTen");
                                if (ma == null || ten == null || ma.length() < 3) continue;
                                String normalized = java.text.Normalizer.normalize(ten, java.text.Normalizer.Form.NFD)
                                        .replaceAll("\\p{M}", "").replace("đ", "d").replace("Đ", "D")
                                        .toLowerCase(java.util.Locale.ROOT).replaceAll("\\s+", "")
                                        + ma.substring(ma.length() - 3);
                                if (normalized.equals(tenDangNhap) && rs.getString("matKhau") != null
                                        && rs.getString("matKhau").equals(matKhau)) {
                                        return new TaiKhoan(ma, ten, rs.getString("soDienThoai"), rs.getString("email"),
                                                rs.getString("matKhau"), rs.getString("gioiTinh"), rs.getString("vaiTro"));
                                }
                        }
                }
                return null;
        }

        // =========================================================
        // LAY TAT CA
        // =========================================================

        public ArrayList<TaiKhoan> getAll() {

                ArrayList<TaiKhoan> list = new ArrayList<>();

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

                                TaiKhoan tk = new TaiKhoan(
                                                rs.getString("maNhanVien"),
                                                rs.getString("hoTen"),
                                                rs.getString("soDienThoai"),
                                                rs.getString("email"),
                                                rs.getString("matKhau"),
                                                rs.getString("gioiTinh"),
                                                rs.getString("vaiTro"));

                                list.add(tk);
                        }

                } catch (SQLException e) {

                        e.printStackTrace();
                }

                return list;
        }

        // =========================================================
        // THEM
        // =========================================================

        // Mã nhân viên được cấp khi INSERT trong cùng transaction.
        public boolean them(TaiKhoan tk) throws SQLException {
                String sql = "INSERT INTO NhanVien "
                                + "(maNhanVien, hoTen, soDienThoai, email, matKhau, gioiTinh, vaiTro) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (Connection con = ConnectDB.connectDB.getConnection()) {
                        con.setAutoCommit(false);
                        try {
                                String maMoi = MaTuDongDAO.taoMaTrongGiaoDich(
                                                con, MaTuDongDAO.Loai.NHAN_VIEN);
                                try (PreparedStatement ps = con.prepareStatement(sql)) {
                                        ps.setString(1, maMoi);
                                        ps.setString(2, tk.getHoTen());
                                        ps.setString(3, tk.getSoDienThoai());
                                        ps.setString(4, tk.getEmail());
                                        ps.setString(5, tk.getMatKhau());
                                        ps.setString(6, tk.getGioiTinh());
                                        ps.setString(7, tk.getVaiTro());
                                        if (ps.executeUpdate() != 1) {
                                                throw new SQLException("Không thể thêm nhân viên.");
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

        // SUA
        public boolean sua(
                        TaiKhoan tk) {

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

                        ps.setString(2, tk.getSoDienThoai());

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

        // XOA
        public boolean xoa(
                        String maNhanVien) {
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

        /**
         * Doi chieu thong tin nhan vien. TUYET DOI KHONG doc/tra lai mat khau.
         * 5 thong tin nay khong du de tu cap lai mat khau; can phe duyet quan ly.
         */
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
                                                && chuanHoaSoDienThoai(rs.getString("soDienThoai"))
                                                                .equals(chuanHoaSoDienThoai(soDienThoai));
                        }
                }
        }

        /**
         * Cap mat khau TAM THOI sau khi quan ly xac thuc tai cho.
         * Cap nhat co dieu kien bang SQL, khong bao gio hien mat khau cu.
         */
        public boolean capLaiMatKhauCoPheDuyet(String maNV, String maQuanLy,
                        String matKhauQuanLy, String matKhauMoi) throws SQLException {
                if (maNV == null || maNV.trim().isEmpty() || maQuanLy == null
                                || maQuanLy.trim().isEmpty() || matKhauQuanLy == null
                                || matKhauQuanLy.isEmpty() || matKhauMoi == null
                                || matKhauMoi.length() < 6) {
                        return false;
                }
                // Bản SQL học tập hiện lưu matKhau dạng văn bản; cần nâng cấp băm mật khẩu.
                String sql = "UPDATE NhanVien SET matKhau = ? "
                                + "WHERE maNhanVien = ? AND EXISTS ( "
                                + " SELECT 1 FROM NhanVien q WHERE q.maNhanVien = ? "
                                + " AND q.matKhau = ? "
                                + " AND (LOWER(q.vaiTro) = N'quan ly' OR LOWER(q.vaiTro) = N'quản lý') )";
                try (Connection con = ConnectDB.connectDB.getConnection();
                                PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, matKhauMoi);
                        ps.setString(2, maNV.trim());
                        ps.setString(3, maQuanLy.trim());
                        ps.setString(4, matKhauQuanLy);
                        return ps.executeUpdate() == 1;
                }
        }

        /**
         * Giu tuong thich voi man hinh QuanLyTaiKhoan co nut "Cap lai MK".
         * Chi goi tu luong quan tri da duoc phan quyen; nen nang cap
         * kiem tra quyen o may chu trong phien ban thuc te.
         */
        public boolean capLaiMatKhau(String maNhanVien, String matKhauMoi) throws SQLException {
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

        /**
         * Cap lai mat khau cho nhan vien theo quy trinh:
         * (1) Nhan vien nhap ma NV; (2) quan ly dang nhap bang ten tai khoan
         * Pet Shop Pro (hoac ma NV) + mat khau; (3) cap mat khau tam thoi.
         * Chi phep tai khoan co vai tro Quan ly phe duyet. Thuc hien trong 1
         * transaction.
         * Chu y: du an hien tai luu mat khau dang text, chi phu hop bai tap.
         */
        public boolean capLaiMatKhauQuaTaiKhoanQuanLy(String maNhanVienCanKhoiPhuc,
                        String taiKhoanQuanLy, String matKhauQuanLy, String matKhauTamThoi)
                        throws SQLException {
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

        /**
         * Doi mat khau cua tai khoan dang dang nhap.
         * So sanh mat khau cu theo BIN2 de khong nham chu HOA/thuong
         * du SQL Server mac dinh co the dung collation khong phan biet hoa/thuong.
         * Khong tra lai / in ra mat khau cu.
         *
         * Luu y: CSDL cua do an dang luu mat khau dang chuoi thuan.
         * Khi nang cap len password hash can doi logic xac thuc va cap nhat.
         */
        public boolean doiMatKhau(String maNhanVien, String matKhauCu,
                        String matKhauMoi) throws SQLException {
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

        private static String chuanHoaSoDienThoai(String str) {
                return str == null ? "" : str.replaceAll("[\\s.-]", "");
        }
}