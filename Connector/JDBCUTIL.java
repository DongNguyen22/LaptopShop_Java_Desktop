package Connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUTIL {
    // url,username,password
    private static String url = "jdbc:mysql://localhost:3306/java?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh";
    private static String username = "root";
    private static String password = "123456";

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // nap driver
        Class.forName("com.mysql.cj.jdbc.Driver");
        return (Connection) DriverManager.getConnection(url, username, password);
    }

    public static void closeConnection(Connection c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
