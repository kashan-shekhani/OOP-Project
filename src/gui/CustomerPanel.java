package gui;

import models.Customer;
import database.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerPanel extends JPanel {
    private JTextField nameField, contactField, addressField, cnicField;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private List<Customer> customers;
    
    public CustomerPanel() {
        setLayout(new BorderLayout());
        customers = new ArrayList<>();
        
        // Create form panel
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);
        
        // Create table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Load initial data
        loadCustomers();
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setBorder(BorderFactory.createTitledBorder("Add New Customer"));
        
        // Initialize fields
        nameField = new JTextField(20);
        contactField = new JTextField(20);
        addressField = new JTextField(20);
        cnicField = new JTextField(20);
        
        // Add components
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Contact:"), gbc);
        gbc.gridx = 1;
        panel.add(contactField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        panel.add(addressField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("CNIC:"), gbc);
        gbc.gridx = 1;
        panel.add(cnicField, gbc);
        
        // Add buttons
        JButton addButton = new JButton("Add Customer");
        JButton clearButton = new JButton("Clear");
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(addButton, gbc);
        
        gbc.gridy = 5;
        panel.add(clearButton, gbc);
        
        // Add action listeners
        addButton.addActionListener(e -> addCustomer());
        clearButton.addActionListener(e -> clearFields());
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Customer List"));
        
        // Create table model
        String[] columns = {"ID", "Name", "Contact", "Address", "CNIC"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        customerTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(customerTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add delete button
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteSelectedCustomer());
        panel.add(deleteButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    public void loadCustomers() {
        tableModel.setRowCount(0);
        customers.clear();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Customers")) {
            
            while (rs.next()) {
                Customer customer = new Customer(
                    rs.getInt("CustomerID"),
                    rs.getString("Name"),
                    rs.getString("Contact"),
                    rs.getString("Address"),
                    rs.getString("CNIC")
                );
                customers.add(customer);
                addCustomerToTable(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage());
        }
    }
    
    private void addCustomerToTable(Customer customer) {
        tableModel.addRow(new Object[]{
            customer.getId(),
            customer.getName(),
            customer.getContact(),
            customer.getAddress(),
            customer.getCnic()
        });
    }
    
    private void addCustomer() {
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String address = addressField.getText().trim();
        String cnic = cnicField.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter customer name");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO Customers (Name, Contact, Address, CNIC) VALUES (?, ?, ?, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.setString(3, address);
            pstmt.setString(4, cnic);
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Customer customer = new Customer(
                        rs.getInt(1),
                        name,
                        contact,
                        address,
                        cnic
                    );
                    customers.add(customer);
                    addCustomerToTable(customer);
                    clearFields();
                    
                    // Refresh customer list in TransactionPanel
                    Window window = SwingUtilities.getWindowAncestor(this);
                    if (window instanceof MainForm) {
                        MainForm mainForm = (MainForm) window;
                        mainForm.getTransactionPanel().refreshCustomers();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding customer: " + e.getMessage());
        }
    }
    
    private void clearFields() {
        nameField.setText("");
        contactField.setText("");
        addressField.setText("");
        cnicField.setText("");
    }
    
    private void deleteSelectedCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete");
            return;
        }
        
        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        String customerName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete " + customerName + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM Customers WHERE CustomerID = ?")) {
                
                pstmt.setInt(1, customerId);
                pstmt.executeUpdate();
                
                // Remove from table and list
                tableModel.removeRow(selectedRow);
                customers.removeIf(customer -> customer.getId() == customerId);
                
                // Refresh customer list in TransactionPanel
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window instanceof MainForm) {
                    MainForm mainForm = (MainForm) window;
                    mainForm.getTransactionPanel().refreshCustomers();
                }
                
                JOptionPane.showMessageDialog(this, "Customer deleted successfully");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage());
            }
        }
    }
} 