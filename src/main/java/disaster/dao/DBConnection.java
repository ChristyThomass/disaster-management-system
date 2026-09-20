package disaster.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static final String HOST = "localhost";
    public static final int PORT = 3306;
    public static final String DATABASE = "disaster_db";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "";
    public static final String JDBC_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {}
        }
        return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
    }

    public static void main(String[] args) {
        System.out.println(">>> TESTING JDBC CONNECTION <<<");
        System.out.println("URL: " + JDBC_URL);
        System.out.println("USER: " + USERNAME);
        try (Connection conn = getConnection()) {
            System.out.println(">>> SUCCESS: CONNECTED TO MYSQL (XAMPP) DATABASE! <<<");
        } catch (SQLException e) {
            System.out.println(">>> CONNECTION FAILED <<<");
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
