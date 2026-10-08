package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class connectDB {

    private static final String URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=ptudpetshop;"
            + "encrypt=true;"
            + "trustServerCertificate=true";

    private static final String USER = "sa";

    private static final String PASSWORD = "sapassword";

    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD);
    }

    // Test ket noi
    public static void main(String[] args) {

        try {

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