package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_FILE = "TawakkalDB.accdb";
    private static final String DB_PATH = "src/database/" + DB_FILE;
    private static final String DB_URL = "jdbc:ucanaccess://" + DB_PATH;
    
    static {
        try {
            // Load the JDBC driver
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
} 