package gui;

import models.Transaction;
import models.Supplier;
import models.Customer;
import database.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionPanel extends JPanel {
    private JComboBox<String> typeCombo;
    private JTextField amountField, quantityField;
    private JComboBox<Supplier> supplierCombo;
    private JComboBox<Customer> customerCombo;
    private JComboBox<String> paymentStatusCombo;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private List<Transaction> transactions;
    
    public TransactionPanel() {
        setLayout(new BorderLayout());
        transactions = new ArrayList<>();
        
        // Create form panel
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);
        
        // Create table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Load initial data
        loadTransactions();
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setBorder(BorderFactory.createTitledBorder("Add New Transaction"));
        
        // Initialize fields
        typeCombo = new JComboBox<>(new String[]{"Purchase", "Sale"});
        amountField = new JTextField(10);
        quantityField = new JTextField(10);
        paymentStatusCombo = new JComboBox<>(new String[]{"Pending", "Paid"});
        supplierCombo = new JComboBox<>();
        customerCombo = new JComboBox<>();
        loadSuppliers();
        loadCustomers();
        
        // Add components
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        panel.add(typeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Quantity (kg):"), gbc);
        gbc.gridx = 1;
        panel.add(quantityField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Supplier (for Purchase):"), gbc);
        gbc.gridx = 1;
        panel.add(supplierCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Customer (for Sale):"), gbc);
        gbc.gridx = 1;
        panel.add(customerCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Payment Status:"), gbc);
        gbc.gridx = 1;
        panel.add(paymentStatusCombo, gbc);
        
        // Add buttons
        JButton addButton = new JButton("Add Transaction");
        JButton clearButton = new JButton("Clear");
        
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(addButton, gbc);
        
        gbc.gridy = 7;
        panel.add(clearButton, gbc);
        
        // Add action listeners
        addButton.addActionListener(e -> addTransaction());
        clearButton.addActionListener(e -> clearFields());
        
        // Show/hide supplier/customer fields based on type
        typeCombo.addActionListener(e -> updateFieldVisibility());
        updateFieldVisibility();
        
        return panel;
    }
    
    private void updateFieldVisibility() {
        String type = (String) typeCombo.getSelectedItem();
        supplierCombo.setEnabled("Purchase".equals(type));
        customerCombo.setEnabled("Sale".equals(type));
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Transaction List"));
        
        // Create table model
        String[] columns = {"ID", "Type", "Date", "Item", "Supplier", "Customer", "Quantity", "Amount", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        transactionTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add delete button
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteSelectedTransaction());
        panel.add(deleteButton, BorderLayout.SOUTH);
        
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
    
    private void loadCustomers() {
        customerCombo.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Customers ORDER BY Name")) {
            
            while (rs.next()) {
                Customer customer = new Customer(
                    rs.getInt("CustomerID"),
                    rs.getString("Name"),
                    rs.getString("Contact"),
                    rs.getString("Address"),
                    rs.getString("CNIC")
                );
                customerCombo.addItem(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage());
        }
    }
    
    public void loadTransactions() {
        tableModel.setRowCount(0);
        transactions.clear();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT t.TransactionID, t.TransactionType, t.TransactionDate, " +
                 "t.ItemID, t.SupplierID, t.CustomerID, t.Quantity, t.Amount, t.Status, " +
                 "i.ItemName, s.Name as SupplierName, c.Name as CustomerName " +
                 "FROM Transactions t " +
                 "LEFT JOIN Inventory i ON t.ItemID = i.ItemID " +
                 "LEFT JOIN Suppliers s ON t.SupplierID = s.SupplierID " +
                 "LEFT JOIN Customers c ON t.CustomerID = c.CustomerID")) {
            
            while (rs.next()) {
                int itemId = rs.getInt("ItemID");
                int supplierId = rs.getInt("SupplierID");
                int customerId = rs.getInt("CustomerID");
                
                // Convert 0 to -1 for NULL values
                if (rs.wasNull()) {
                    itemId = -1;
                    supplierId = -1;
                    customerId = -1;
                }
                
                Transaction transaction = new Transaction(
                    rs.getInt("TransactionID"),
                    rs.getString("TransactionType"),
                    rs.getDate("TransactionDate"),
                    itemId,
                    supplierId,
                    customerId,
                    rs.getDouble("Quantity"),
                    rs.getDouble("Amount"),
                    rs.getString("Status")
                );
                transactions.add(transaction);
                addTransactionToTable(transaction, 
                    rs.getString("ItemName"),
                    rs.getString("SupplierName"),
                    rs.getString("CustomerName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
        }
    }
    
    private void addTransactionToTable(Transaction transaction, String itemName, String supplierName, String customerName) {
        tableModel.addRow(new Object[]{
            transaction.getId(),
            transaction.getType(),
            transaction.getDate(),
            itemName,
            supplierName,
            customerName,
            transaction.getQuantity(),
            transaction.getAmount(),
            transaction.getStatus()
        });
    }
    
    private void addTransaction() {
        try {
            String type = (String) typeCombo.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText().trim());
            double quantity = Double.parseDouble(quantityField.getText().trim());
            String paymentStatus = (String) paymentStatusCombo.getSelectedItem();
            Supplier supplier = (Supplier) supplierCombo.getSelectedItem();
            Customer customer = (Customer) customerCombo.getSelectedItem();
            
            if ("Purchase".equals(type) && supplier == null) {
                JOptionPane.showMessageDialog(this, "Please select a supplier for purchase");
                return;
            }
            if ("Sale".equals(type) && customer == null) {
                JOptionPane.showMessageDialog(this, "Please select a customer for sale");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO Transactions (TransactionType, TransactionDate, ItemID, SupplierID, CustomerID, Quantity, Amount, Status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
                
                pstmt.setString(1, type);
                pstmt.setDate(2, new java.sql.Date(new Date().getTime()));
                pstmt.setNull(3, java.sql.Types.INTEGER); // ItemID is NULL
                
                if ("Purchase".equals(type)) {
                    pstmt.setInt(4, supplier.getId());
                    pstmt.setNull(5, java.sql.Types.INTEGER); // CustomerID is NULL for purchase
                } else {
                    pstmt.setNull(4, java.sql.Types.INTEGER); // SupplierID is NULL for sale
                    pstmt.setInt(5, customer.getId());
                }
                
                pstmt.setDouble(6, quantity);
                pstmt.setDouble(7, amount);
                pstmt.setString(8, paymentStatus);
                
                pstmt.executeUpdate();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Transaction tx = new Transaction(
                            rs.getInt(1),
                            type,
                            new Date(),
                            -1, // Use -1 to represent NULL ItemID
                            (supplier != null) ? supplier.getId() : -1,
                            (customer != null) ? customer.getId() : -1,
                            quantity,
                            amount,
                            paymentStatus
                        );
                        transactions.add(tx);
                        addTransactionToTable(tx, "", 
                            (supplier != null) ? supplier.getName() : "",
                            (customer != null) ? customer.getName() : "");
                        clearFields();
                    }
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid amount and quantity");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding transaction: " + e.getMessage());
        }
    }
    
    private void clearFields() {
        typeCombo.setSelectedIndex(0);
        amountField.setText("");
        quantityField.setText("");
        paymentStatusCombo.setSelectedIndex(0);
        supplierCombo.setSelectedIndex(0);
        customerCombo.setSelectedIndex(0);
        updateFieldVisibility();
    }
    
    private void deleteSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete");
            return;
        }
        
        int transactionId = (int) tableModel.getValueAt(selectedRow, 0);
        String transactionType = (String) tableModel.getValueAt(selectedRow, 1);
        String itemName = (String) tableModel.getValueAt(selectedRow, 3);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this " + transactionType.toLowerCase() + " transaction for " + itemName + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM Transactions WHERE TransactionID = ?")) {
                
                pstmt.setInt(1, transactionId);
                pstmt.executeUpdate();
                
                // Remove from table and list
                tableModel.removeRow(selectedRow);
                transactions.removeIf(t -> t.getId() == transactionId);
                
                JOptionPane.showMessageDialog(this, "Transaction deleted successfully");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting transaction: " + e.getMessage());
            }
        }
    }
    
    // Add this method to refresh suppliers
    public void refreshSuppliers() {
        loadSuppliers();
    }
    
    public void refreshCustomers() {
        loadCustomers();
    }
} 