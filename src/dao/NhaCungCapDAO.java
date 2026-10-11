package dao;

import ConnectDB.connectDB;
import entity.NhaCungCap;

import java.sql.*;
import java.util.ArrayList;

public class NhaCungCapDAO {

    public ArrayList<NhaCungCap> getAll() throws SQLException {

        ArrayList<NhaCungCap> list = new ArrayList<>();

        String sql = """
                SELECT maNhaCungCap, tenNhaCungCap,
                       soDienThoai, email, diaChi
                FROM Nhacungcap
                ORDER BY maNhaCungCap
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                NhaCungCap n = new NhaCungCap();
                n.setMaNhaCungCap(rs.getString("maNhaCungCap"));
                n.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
                n.setSoDienThoai(rs.getString("soDienThoai"));
                n.setEmail(rs.getString("email"));
                n.setDiaChi(rs.getString("diaChi"));
                list.add(n);
            }
        }

        return list;
    }

    public String taoMaMoi() throws SQLException {
        return MaTuDong.taoMa("Nhacungcap", "maNhaCungCap", "NCC", 3);
    }

    public boolean them(NhaCungCap n) throws SQLException {

        String sql = """
                INSERT INTO Nhacungcap
                (maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, n.getMaNhaCungCap());
            ps.setString(2, n.getTenNhaCungCap());
            ps.setString(3, n.getSoDienThoai());
            ps.setString(4, n.getEmail());
            ps.setString(5, n.getDiaChi());

            return ps.executeUpdate() > 0;
        }
    }
}
