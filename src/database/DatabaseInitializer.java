package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    
    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Check and create tables only if they don't exist
            if (!tableExists(metaData, "Suppliers")) {
                createSuppliersTable(conn);
            }
            
            if (!tableExists(metaData, "Inventory")) {
                createInventoryTable(conn);
            }
            
            if (!tableExists(metaData, "Transactions")) {
                createTransactionsTable(conn);
            }
            
            if (!tableExists(metaData, "Customers")) {
                createCustomersTable(conn);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet rs = metaData.getTables(null, null, tableName, null)) {
            return rs.next();
        }
    }
    
    private static void createSuppliersTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE TABLE Suppliers (" +
                "SupplierID AUTOINCREMENT PRIMARY KEY, " +
                "Name TEXT(255), " +
                "Contact TEXT(255), " +
                "Address TEXT(255), " +
                "CNIC TEXT(255))"
            );
        }
    }
    
    private static void createInventoryTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE TABLE Inventory (" +
                "ItemID AUTOINCREMENT PRIMARY KEY, " +
                "ItemName TEXT(255), " +
                "Quantity DOUBLE, " +
                "Price DOUBLE, " +
                "SupplierID INTEGER, " +
                "FOREIGN KEY (SupplierID) REFERENCES Suppliers(SupplierID))"
            );
        }
    }
    
    private static void createTransactionsTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE TABLE Transactions (" +
                "TransactionID AUTOINCREMENT PRIMARY KEY, " +
                "TransactionType TEXT(50), " +
                "TransactionDate DATETIME, " +
                "ItemID INTEGER, " +
                "SupplierID INTEGER, " +
                "CustomerID INTEGER, " +
                "Quantity DOUBLE, " +
                "Amount DOUBLE, " +
                "Status TEXT(50), " +
                "FOREIGN KEY (ItemID) REFERENCES Inventory(ItemID), " +
                "FOREIGN KEY (SupplierID) REFERENCES Suppliers(SupplierID), " +
                "FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID))"
            );
        }
    }
    
    private static void createCustomersTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE TABLE Customers (" +
                "CustomerID AUTOINCREMENT PRIMARY KEY, " +
                "Name TEXT(255), " +
                "Contact TEXT(255), " +
                "Address TEXT(255), " +
                "CNIC TEXT(255))"
            );
        }
    }
} 