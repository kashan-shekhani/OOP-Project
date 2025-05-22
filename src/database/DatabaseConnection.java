package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

public class DatabaseConnection {
    private static final String DB_FILE = "TawakkalDB.accdb";
    private static final String DB_PATH = "C:\\Users\\kasha\\OneDrive - Ecomise\\Documents\\NetBeansProjects\\Project\\database\\" + DB_FILE;
    private static final String DB_URL = "jdbc:ucanaccess://" + DB_PATH;
    
    static {
        try {
            // Create database directory if it doesn't exist
            File dbDir = new File("C:\\Users\\kasha\\OneDrive - Ecomise\\Documents\\NetBeansProjects\\Project\\database");
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }
            
            // Load the JDBC driver
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            
            // Create database file if it doesn't exist
            File dbFile = new File(DB_PATH);
            if (!dbFile.exists()) {
                // Create a new empty database file
                Connection conn = DriverManager.getConnection(DB_URL);
                conn.close();
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
} 