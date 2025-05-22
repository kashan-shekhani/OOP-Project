package gui;

import database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ReportPanel extends JPanel {
    private JTextArea reportArea;
    
    public ReportPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Reports & Analytics"));
        
        reportArea = new JTextArea(20, 80);
        reportArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(reportArea);
        add(scrollPane, BorderLayout.CENTER);
        
        JButton refreshButton = new JButton("Refresh Reports");
        refreshButton.addActionListener(e -> loadReports());
        add(refreshButton, BorderLayout.NORTH);
        
        loadReports();
    }
    
    private void loadReports() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total suppliers
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Suppliers")) {
                if (rs.next()) {
                    sb.append("Total Suppliers: ").append(rs.getInt(1)).append("\n");
                }
            }
            // Total inventory
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT SUM(Quantity) FROM Inventory")) {
                if (rs.next()) {
                    sb.append("Total Inventory (kg): ").append(rs.getDouble(1)).append("\n");
                }
            }
            // Total sales
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT SUM(Amount) FROM Transactions WHERE Type='Sale'")) {
                if (rs.next()) {
                    sb.append("Total Sales Amount: ").append(rs.getDouble(1)).append("\n");
                }
            }
            // Total purchases
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT SUM(Amount) FROM Transactions WHERE Type='Purchase'")) {
                if (rs.next()) {
                    sb.append("Total Purchase Amount: ").append(rs.getDouble(1)).append("\n");
                }
            }
        } catch (SQLException e) {
            sb.append("Error loading reports: ").append(e.getMessage());
        }
        reportArea.setText(sb.toString());
    }
} 