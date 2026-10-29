package dao;

import UI.PhanQuyen;

import ConnectDB.connectDB;
import entity.ChiTietPhieuKiemKe;
import entity.PhieuKiemKe;
import entity.SanPham;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cua man hinh "Kiem ke kho": phieu kiem ke + danh sach san pham
 * kem ton he thong de kiem.
 */
public class KiemKeDAO {

    // Bổ sung các cột hỗ trợ thời điểm kiểm kê nếu cơ sở dữ liệu chưa có.
    private void damBaoCotKiemKe() throws SQLException {
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = ConnectDB.connectDB.getConnection(); Statement st = con.createStatement()) {
            st.execute("IF COL_LENGTH('dbo.PhieuKiemKe','ngayBatDau') IS NULL ALTER TABLE dbo.PhieuKiemKe ADD ngayBatDau DATETIME2 NULL");
            st.execute("IF COL_LENGTH('dbo.PhieuKiemKe','ngayChot') IS NULL ALTER TABLE dbo.PhieuKiemKe ADD ngayChot DATETIME2 NULL");
        }
    }

    // Lấy mã mới để hiển thị khi lập bản ghi.
    // Kết quả: chuỗi kết quả.
    public String taoMaMoi() throws SQLException {
        return taoMa("PhieuKiemKe", "maPhieuKiemKe", "KK", 3);
    }

    // Luu phieu + chi tiet trong 1 giao dich
    // Lưu dữ liệu kiểm kê trong cơ sở dữ liệu.
    // Tham số phieu: phiếu.
    // Tham số chiTiet: chi tiết.
    public void luu(PhieuKiemKe phieu,
                    List<ChiTietPhieuKiemKe> chiTiet) throws SQLException {
        PhanQuyen.yeuCau(PhanQuyen.Quyen.KIEM_KE);

        damBaoCotKiemKe();

        // sqlPhieu: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sqlPhieu = """
                INSERT INTO PhieuKiemKe
                (maPhieuKiemKe, ngayKiemKe, trangThai, maNhanVien, ngayBatDau, ngayChot)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        // sqlChiTiet: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sqlChiTiet = """
                INSERT INTO ChiTietPhieuKiemKe
                (maPhieuKiemKe, maSanPham, soLuongHeThong,
                 soLuongThucTe, trangThai)
                VALUES (?, ?, ?, ?, ?)
                """;

        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection()) {

            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
            con.setAutoCommit(false);

            try {
                int daCapNhat;
                try (PreparedStatement ps = con.prepareStatement("UPDATE PhieuKiemKe SET ngayKiemKe=?, trangThai=1, maNhanVien=?, ngayBatDau=?, ngayChot=? WHERE maPhieuKiemKe=? AND ISNULL(trangThai,0)=0")) {
                    ps.setTimestamp(1, Timestamp.valueOf(phieu.getNgayKiemKe())); ps.setString(2, phieu.getMaNhanVien());
                    ps.setTimestamp(3, Timestamp.valueOf(phieu.getNgayBatDau())); ps.setTimestamp(4, Timestamp.valueOf(phieu.getNgayChot()));
                    ps.setString(5, phieu.getMaPhieuKiemKe()); daCapNhat = ps.executeUpdate();
                }
                if (daCapNhat > 0) {
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietPhieuKiemKe WHERE maPhieuKiemKe=?")) {
                        ps.setString(1, phieu.getMaPhieuKiemKe()); ps.executeUpdate();
                    }
                } else try (PreparedStatement ps = con.prepareStatement(sqlPhieu)) {
                    ps.setString(1, phieu.getMaPhieuKiemKe());
                    ps.setTimestamp(2, Timestamp.valueOf(phieu.getNgayKiemKe()));
                    ps.setBoolean(3, phieu.isTrangThai());
                    ps.setString(4, phieu.getMaNhanVien());
                    ps.setTimestamp(5, Timestamp.valueOf(phieu.getNgayBatDau()));
                    ps.setTimestamp(6, Timestamp.valueOf(phieu.getNgayChot()));
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {
                    for (ChiTietPhieuKiemKe ct : chiTiet) {
                        ps.setString(1, phieu.getMaPhieuKiemKe());
                        ps.setString(2, ct.getMaSanPham());
                        ps.setInt(3, ct.getSoLuongHeThong());
                        if (ct.getSoLuongThucTe() == null) ps.setNull(4, Types.INTEGER);
                        else ps.setInt(4, ct.getSoLuongThucTe());
                        ps.setBoolean(5, ct.isTrangThai());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                con.commit();

            } catch (SQLException e) {
                // Hoàn tác các thay đổi chưa được xác nhận của giao dịch.
                con.rollback();
                throw e;
            }
        }
    }

    // Lưu nhập.
    // Tham số phieu: phiếu.
    // Tham số chiTiet: chi tiết.
    public void luuNhap(PhieuKiemKe phieu, List<ChiTietPhieuKiemKe> chiTiet) throws SQLException {
    PhanQuyen.yeuCau(PhanQuyen.Quyen.KIEM_KE);
        damBaoCotKiemKe();
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection con = connectDB.getConnection()) {
            // Bắt đầu giao dịch thủ công để các thay đổi được xác nhận hoặc hoàn tác cùng nhau.
            con.setAutoCommit(false);
            try {
                int capNhat;
                try (PreparedStatement ps = con.prepareStatement("UPDATE PhieuKiemKe SET ngayKiemKe=?,trangThai=0,maNhanVien=?,ngayBatDau=?,ngayChot=NULL WHERE maPhieuKiemKe=? AND ISNULL(trangThai,0)=0")) {
                    ps.setTimestamp(1, Timestamp.valueOf(phieu.getNgayBatDau())); ps.setString(2, phieu.getMaNhanVien());
                    ps.setTimestamp(3, Timestamp.valueOf(phieu.getNgayBatDau())); ps.setString(4, phieu.getMaPhieuKiemKe()); capNhat = ps.executeUpdate();
                }
                if (capNhat == 0) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO PhieuKiemKe(maPhieuKiemKe,ngayKiemKe,trangThai,maNhanVien,ngayBatDau) VALUES(?,?,0,?,?)")) {
                        ps.setString(1, phieu.getMaPhieuKiemKe()); ps.setTimestamp(2, Timestamp.valueOf(phieu.getNgayBatDau()));
                        ps.setString(3, phieu.getMaNhanVien()); ps.setTimestamp(4, Timestamp.valueOf(phieu.getNgayBatDau())); ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietPhieuKiemKe WHERE maPhieuKiemKe=?")) {
                    ps.setString(1, phieu.getMaPhieuKiemKe()); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO ChiTietPhieuKiemKe(maPhieuKiemKe,maSanPham,soLuongHeThong,soLuongThucTe,trangThai) VALUES(?,?,?,?,?)")) {
                    for (ChiTietPhieuKiemKe ct : chiTiet) {
                        ps.setString(1, phieu.getMaPhieuKiemKe()); ps.setString(2, ct.getMaSanPham()); ps.setInt(3, ct.getSoLuongHeThong());
                        if (ct.getSoLuongThucTe() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, ct.getSoLuongThucTe());
                        ps.setBoolean(5, ct.isTrangThai()); ps.addBatch();
                    }
                    ps.executeBatch();
                }
                // Xác nhận các thay đổi của giao dịch vào cơ sở dữ liệu.
                con.commit();
            } catch (SQLException e) { con.rollback(); throw e; }
        }
    }

    // Lấy danh sách phiếu đang lưu ở trạng thái nháp.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<PhieuKiemKe>.
    public ArrayList<PhieuKiemKe> dsPhieuNhap() throws SQLException {
        damBaoCotKiemKe();
        // ds: Danh sách chứa các phần tử kiểu PhieuKiemKe.
        ArrayList<PhieuKiemKe> ds = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT maPhieuKiemKe, ngayKiemKe, trangThai, maNhanVien, ngayBatDau, ngayChot FROM PhieuKiemKe WHERE ISNULL(trangThai,0)=0 ORDER BY ngayKiemKe DESC";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection c = ConnectDB.connectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (r.next()) {
                PhieuKiemKe x = new PhieuKiemKe();
                x.setMaPhieuKiemKe(r.getString(1));
                // t: Lưu bảng/thành phần được xử lý dưới dạng dữ liệu thời gian.
                Timestamp t = r.getTimestamp(2); x.setNgayKiemKe(t == null ? null : t.toLocalDateTime());
                x.setTrangThai(r.getBoolean(3)); x.setMaNhanVien(r.getString(4));
                t = r.getTimestamp(5); x.setNgayBatDau(t == null ? null : t.toLocalDateTime());
                t = r.getTimestamp(6); x.setNgayChot(t == null ? null : t.toLocalDateTime());
                ds.add(x);
            }
        }
        return ds;
    }

    // Lấy chi tiết.
    // Tham số maPhieu: mã phiếu.
    // Kết quả: đối tượng/giá trị kiểu ArrayList<ChiTietPhieuKiemKe>.
    public ArrayList<ChiTietPhieuKiemKe> layChiTiet(String maPhieu) throws SQLException {
        // ds: Danh sách chứa các phần tử kiểu ChiTietPhieuKiemKe.
        ArrayList<ChiTietPhieuKiemKe> ds = new ArrayList<>();
        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = "SELECT k.maSanPham,s.tenSanPham,k.soLuongHeThong,k.soLuongThucTe,k.trangThai FROM ChiTietPhieuKiemKe k JOIN SanPham s ON s.maSanPham=k.maSanPham WHERE k.maPhieuKiemKe=? ORDER BY k.maSanPham";
        // Mở kết nối trong try-with-resources để tự đóng tài nguyên khi kết thúc khối lệnh.
        try (Connection c=connectDB.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,maPhieu); try(ResultSet r=p.executeQuery()) { while(r.next()) {
                ChiTietPhieuKiemKe x=new ChiTietPhieuKiemKe(); x.setMaPhieuKiemKe(maPhieu); x.setMaSanPham(r.getString(1));
                x.setTenSanPham(r.getString(2)); x.setSoLuongHeThong(r.getInt(3)); int thuc=r.getInt(4);
                x.setSoLuongThucTe(r.wasNull()?null:thuc); x.setTrangThai(r.getBoolean(5)); ds.add(x);
            }}
        }
        return ds;
    }

    /** Toan bo san pham kem ton kho he thong (cong thuc ton lay tu SanPhamDAO de luon khop). */
    public ArrayList<SanPham> laySanPhamKemTon() throws SQLException {

        // list: Danh sách chứa các phần tử kiểu SanPham.
        ArrayList<SanPham> list = new ArrayList<>();

        // sql: Chuỗi SQL dùng để truy vấn hoặc cập nhật dữ liệu; dấu ? là vị trí nhận tham số nếu có.
        String sql = """
                SELECT sp.maSanPham,
                       sp.tenSanPham,
                """ + SanPhamDAO.BIEU_THUC_TON + """
                       AS tonKho
                FROM SanPham sp
                ORDER BY sp.maSanPham
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                SanPham sp = new SanPham();
                sp.setMaSanPham(rs.getString("maSanPham"));
                sp.setTenSanPham(rs.getString("tenSanPham"));
                sp.setTonKho(rs.getInt("tonKho"));
                list.add(sp);
            }
        }

        return list;
    }

    // Sinh ma tu dong dang TIENTO + so (DM005, SP009...): lay so lon nhat dang co roi cong 1
    // Tạo mã mới từ bảng, cột, tiền tố và số chữ số được yêu cầu.
    // Tham số bang: bảng dữ liệu.
    // Tham số cot: cột.
    // Tham số tienTo: tiền tố mã.
    // Tham số soChuSo: số chữ số của mã.
    // Kết quả: chuỗi kết quả.
    private static String taoMa(String bang, String cot,
                                String tienTo, int soChuSo) throws SQLException {

        int max = 0;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT " + cot + " FROM " + bang);
            ResultSet rs = ps.executeQuery()
        ) {
            // Duyệt từng dòng của kết quả truy vấn; next() trả về false khi hết dữ liệu.
            while (rs.next()) {
                // ma: Lưu mã.
                String ma = rs.getString(1);

                if (ma != null && ma.startsWith(tienTo)) {
                    String so = ma.substring(tienTo.length());

                    if (so.matches("\\d{1,9}")) {
                        max = Math.max(max, Integer.parseInt(so));
                    }
                }
            }
        }

        return tienTo + String.format("%0" + soChuSo + "d", max + 1);
    }
}
