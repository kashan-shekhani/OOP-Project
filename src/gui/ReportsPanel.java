package gui;

import database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportsPanel extends JPanel {
    private JTextArea reportArea;
    private JComboBox<String> reportTypeCombo;
    private JButton generateButton;
    private JButton printButton;
    
    public ReportsPanel() {
        setLayout(new BorderLayout());
        
        // Create control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Create report area
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(reportArea);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Report Controls"));
        
        reportTypeCombo = new JComboBox<>(new String[]{
            "Inventory Summary",
            "Sales Report",
            "Purchase Report",
            "Customer Summary",
            "Supplier Summary"
        });
        
        generateButton = new JButton("Generate Report");
        printButton = new JButton("Print Report");
        
        panel.add(new JLabel("Report Type:"));
        panel.add(reportTypeCombo);
        panel.add(generateButton);
        panel.add(printButton);
        
        generateButton.addActionListener(e -> generateReport());
        printButton.addActionListener(e -> printReport());
        
        return panel;
    }
    
    private void generateReport() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        StringBuilder report = new StringBuilder();
        report.append("Report: ").append(reportType).append("\n");
        report.append("Generated on: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            switch (reportType) {
                case "Inventory Summary":
                    generateInventoryReport(conn, report);
                    break;
                case "Sales Report":
                    generateSalesReport(conn, report);
                    break;
                case "Purchase Report":
                    generatePurchaseReport(conn, report);
                    break;
                case "Customer Summary":
                    generateCustomerReport(conn, report);
                    break;
                case "Supplier Summary":
                    generateSupplierReport(conn, report);
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            report.append("Error generating report: ").append(e.getMessage());
        }
        
        reportArea.setText(report.toString());
    }
    
    private void generateInventoryReport(Connection conn, StringBuilder report) throws SQLException {
        report.append("Inventory Summary\n");
        report.append("----------------\n\n");
        
        // Total items
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Inventory")) {
            if (rs.next()) {
                report.append("Total Items: ").append(rs.getInt(1)).append("\n");
            }
        }
        
        // Total quantity
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(Quantity) FROM Inventory")) {
            if (rs.next()) {
                report.append("Total Quantity: ").append(rs.getDouble(1)).append(" kg\n");
            }
        }
        
        // Items by supplier
        report.append("\nItems by Supplier:\n");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT s.Name, COUNT(i.ItemID) as ItemCount, SUM(i.Quantity) as TotalQuantity " +
                 "FROM Inventory i " +
                 "LEFT JOIN Suppliers s ON i.SupplierID = s.SupplierID " +
                 "GROUP BY s.Name")) {
            while (rs.next()) {
                report.append(String.format("%s: %d items, %.2f kg\n",
                    rs.getString("Name"),
                    rs.getInt("ItemCount"),
                    rs.getDouble("TotalQuantity")));
            }
        }
    }
    
    private void generateSalesReport(Connection conn, StringBuilder report) throws SQLException {
        report.append("Sales Report\n");
        report.append("------------\n\n");
        
        // Total sales
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT SUM(Amount) as TotalSales, COUNT(*) as TransactionCount " +
                 "FROM Transactions " +
                 "WHERE TransactionType = 'Sale'")) {
            if (rs.next()) {
                report.append(String.format("Total Sales: Rs. %.2f\n", rs.getDouble("TotalSales")));
                report.append(String.format("Number of Sales: %d\n", rs.getInt("TransactionCount")));
            }
        }
        
        // Sales by date
        report.append("\nSales by Date:\n");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT TransactionDate, SUM(Amount) as DailySales " +
                 "FROM Transactions " +
                 "WHERE TransactionType = 'Sale' " +
                 "GROUP BY TransactionDate " +
                 "ORDER BY TransactionDate DESC")) {
            while (rs.next()) {
                report.append(String.format("%s: Rs. %.2f\n",
                    rs.getDate("TransactionDate"),
                    rs.getDouble("DailySales")));
            }
        }
    }
    
    private void generatePurchaseReport(Connection conn, StringBuilder report) throws SQLException {
        report.append("Purchase Report\n");
        report.append("---------------\n\n");
        
        // Total purchases
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT SUM(Amount) as TotalPurchases, COUNT(*) as TransactionCount " +
                 "FROM Transactions " +
                 "WHERE TransactionType = 'Purchase'")) {
            if (rs.next()) {
                report.append(String.format("Total Purchases: Rs. %.2f\n", rs.getDouble("TotalPurchases")));
                report.append(String.format("Number of Purchases: %d\n", rs.getInt("TransactionCount")));
            }
        }
        
        // Purchases by supplier
        report.append("\nPurchases by Supplier:\n");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT s.Name, SUM(t.Amount) as TotalAmount " +
                 "FROM Transactions t " +
                 "LEFT JOIN Suppliers s ON t.SupplierID = s.SupplierID " +
                 "WHERE t.TransactionType = 'Purchase' " +
                 "GROUP BY s.Name")) {
            while (rs.next()) {
                report.append(String.format("%s: Rs. %.2f\n",
                    rs.getString("Name"),
                    rs.getDouble("TotalAmount")));
            }
        }
    }
    
    private void generateCustomerReport(Connection conn, StringBuilder report) throws SQLException {
        report.append("Customer Summary\n");
        report.append("----------------\n\n");
        
        // Total customers
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Customers")) {
            if (rs.next()) {
                report.append("Total Customers: ").append(rs.getInt(1)).append("\n\n");
            }
        }
        
        // Customer list
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Customers ORDER BY Name")) {
            while (rs.next()) {
                report.append(String.format("%s\n", rs.getString("Name")));
                report.append(String.format("  Contact: %s\n", rs.getString("Contact")));
                report.append(String.format("  Address: %s\n", rs.getString("Address")));
                report.append(String.format("  CNIC: %s\n\n", rs.getString("CNIC")));
            }
        }
    }
    
    private void generateSupplierReport(Connection conn, StringBuilder report) throws SQLException {
        report.append("Supplier Summary\n");
        report.append("----------------\n\n");
        
        // Total suppliers
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Suppliers")) {
            if (rs.next()) {
                report.append("Total Suppliers: ").append(rs.getInt(1)).append("\n\n");
            }
        }
        
        // Supplier list
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Suppliers ORDER BY Name")) {
            while (rs.next()) {
                report.append(String.format("%s\n", rs.getString("Name")));
                report.append(String.format("  Contact: %s\n", rs.getString("Contact")));
                report.append(String.format("  Address: %s\n", rs.getString("Address")));
                report.append(String.format("  CNIC: %s\n\n", rs.getString("CNIC")));
            }
        }
    }
    
    private void printReport() {
        try {
            reportArea.print();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error printing report: " + e.getMessage());
        }
    }
} 