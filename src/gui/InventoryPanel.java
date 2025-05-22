package gui;

import models.Inventory;
import models.Supplier;
import database.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InventoryPanel extends JPanel {
    private JTextField quantityField;
    private JComboBox<String> qualityCombo;
    private JComboBox<Supplier> supplierCombo;
    private JComboBox<String> statusCombo;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private List<Inventory> inventoryItems;
    
    public InventoryPanel() {
        setLayout(new BorderLayout());
        inventoryItems = new ArrayList<>();
        
        // Create form panel
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);
        
        // Create table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Load initial data
        loadInventory();
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setBorder(BorderFactory.createTitledBorder("Add New Inventory Item"));
        
        // Initialize fields
        quantityField = new JTextField(10);
        qualityCombo = new JComboBox<>(new String[]{"Grade A", "Grade B", "Grade C"});
        statusCombo = new JComboBox<>(new String[]{"Raw", "Cleaned", "Ready for Sale"});
        supplierCombo = new JComboBox<>();
        loadSuppliers();
        
        // Add components
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Quantity (kg):"), gbc);
        gbc.gridx = 1;
        panel.add(quantityField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Quality:"), gbc);
        gbc.gridx = 1;
        panel.add(qualityCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        panel.add(statusCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1;
        panel.add(supplierCombo, gbc);
        
        // Add buttons
        JButton addButton = new JButton("Add Inventory");
        JButton clearButton = new JButton("Clear");
        JButton deleteButton = new JButton("Delete Selected");
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(addButton, gbc);
        
        // Create a panel for clear and delete buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(clearButton);
        buttonPanel.add(deleteButton);
        
        gbc.gridy = 5;
        panel.add(buttonPanel, gbc);
        
        // Add action listeners
        addButton.addActionListener(e -> addInventory());
        clearButton.addActionListener(e -> clearFields());
        deleteButton.addActionListener(e -> deleteSelectedItem());
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Inventory List"));
        
        // Create table model
        String[] columns = {"ID", "Name", "Quantity", "Price", "Supplier"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        inventoryTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadSuppliers() {
        supplierCombo.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Suppliers ORDER BY Name")) {
            
            while (rs.next()) {
                Supplier supplier = new Supplier(
                    rs.getInt("SupplierID"),
                    rs.getString("Name"),
                    rs.getString("Contact"),
                    rs.getString("Address"),
                    rs.getString("CNIC")
                );
                supplierCombo.addItem(supplier);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + e.getMessage());
        }
    }
    
    public void loadInventory() {
        tableModel.setRowCount(0);
        inventoryItems.clear();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT i.*, s.Name as SupplierName " +
                 "FROM Inventory i " +
                 "LEFT JOIN Suppliers s ON i.SupplierID = s.SupplierID")) {
            
            while (rs.next()) {
                Inventory item = new Inventory(
                    rs.getInt("ItemID"),
                    rs.getString("ItemName"),
                    rs.getDouble("Quantity"),
                    rs.getDouble("Price"),
                    rs.getInt("SupplierID")
                );
                inventoryItems.add(item);
                addItemToTable(item, rs.getString("SupplierName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading inventory: " + e.getMessage());
        }
    }
    
    private void addItemToTable(Inventory item, String supplierName) {
        tableModel.addRow(new Object[]{
            item.getId(),
            item.getName(),
            item.getQuantity(),
            item.getPrice(),
            supplierName
        });
    }
    
    private void addInventory() {
        try {
            double quantity = Double.parseDouble(quantityField.getText().trim());
            String quality = (String) qualityCombo.getSelectedItem();
            String status = (String) statusCombo.getSelectedItem();
            Supplier supplier = (Supplier) supplierCombo.getSelectedItem();
            
            if (supplier == null) {
                JOptionPane.showMessageDialog(this, "Please select a supplier");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO Inventory (ItemName, Quantity, Price, SupplierID) " +
                     "VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
                
                pstmt.setString(1, quality);
                pstmt.setDouble(2, quantity);
                pstmt.setDouble(3, 0.0);
                pstmt.setInt(4, supplier.getId());
                
                pstmt.executeUpdate();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Inventory item = new Inventory(
                            rs.getInt(1),
                            quality,
                            quantity,
                            0.0,
                            supplier.getId()
                        );
                        inventoryItems.add(item);
                        addItemToTable(item, supplier.getName());
                        clearFields();
                    }
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding inventory: " + e.getMessage());
        }
    }
    
    private void clearFields() {
        quantityField.setText("");
        qualityCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        supplierCombo.setSelectedIndex(0);
    }
    
    private void deleteSelectedItem() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete");
            return;
        }
        
        int itemId = (int) tableModel.getValueAt(selectedRow, 0);
        String itemName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete " + itemName + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM Inventory WHERE ItemID = ?")) {
                
                pstmt.setInt(1, itemId);
                pstmt.executeUpdate();
                
                // Remove from table and list
                tableModel.removeRow(selectedRow);
                inventoryItems.removeIf(item -> item.getId() == itemId);
                
                JOptionPane.showMessageDialog(this, "Item deleted successfully");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting item: " + e.getMessage());
            }
        }
    }
    
    // Add method to refresh suppliers
    public void refreshSuppliers() {
        loadSuppliers();
    }
} 