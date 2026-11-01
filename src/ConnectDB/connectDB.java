package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Cung cấp kết nối JDBC đến cơ sở dữ liệu SQL Server.
public class connectDB {

    // URL: Địa chỉ JDBC và các tùy chọn kết nối SQL Server.
    private static final String URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=00;"
            + "encrypt=true;"
            + "trustServerCertificate=true";

    // USER: Tên tài khoản dùng để kết nối cơ sở dữ liệu.
    private static final String USER = "sa";

    // PASSWORD: Mật khẩu của tài khoản kết nối cơ sở dữ liệu.
    private static final String PASSWORD = "sapassword";

    // Mở kết nối SQL Server theo cấu hình URL, tài khoản và mật khẩu của lớp.
    // Kết quả: đối tượng/giá trị kiểu Connection.
    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD);
    }

    // Test ket noi
    // Chạy kiểm tra kết nối đến cơ sở dữ liệu và in kết quả ra console.
    // Tham số args: các đối số dòng lệnh.
    public static void main(String[] args) {

        try {

            // con: Kết nối JDBC dùng để làm việc với cơ sở dữ liệu.
            Connection con = getConnection();

            if (con != null) {

                System.out.println(
                        "KET NOI DATABASE THANH CONG!");

                System.out.println(
                        "Database: "
                                + con.getCatalog());

                con.close();
            }

        } catch (SQLException e) {

            System.out.println(
                    "KET NOI DATABASE THAT BAI!");

            e.printStackTrace();
        }
    }
}