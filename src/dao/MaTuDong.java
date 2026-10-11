package dao;

import ConnectDB.connectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Sinh ma tu dong dang TIENTO + so (vi du DM005, SP009, NCC004, PN004, KK002)
 * bang cach lay so lon nhat dang co trong bang roi cong 1.
 */
public class MaTuDong {

    public static String taoMa(String bang, String cot,
                               String tienTo, int soChuSo) throws SQLException {

        int max = 0;

        String sql = "SELECT " + cot + " FROM " + bang;

        try (
            Connection con = connectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
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
