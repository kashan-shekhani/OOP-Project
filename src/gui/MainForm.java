package gui;

import database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainForm extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private SupplierPanel supplierPanel;
    private InventoryPanel inventoryPanel;
    private TransactionPanel transactionPanel;
    private CustomerPanel customerPanel;
    private ReportsPanel reportsPanel;
    
    public MainForm() {
        setTitle("Tawakkal Huller and Traders Automation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Create menu bar
        createMenuBar();
        
        // Create main panel with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Initialize panels
        supplierPanel = new SupplierPanel();
        inventoryPanel = new InventoryPanel();
        transactionPanel = new TransactionPanel();
        customerPanel = new CustomerPanel();
        reportsPanel = new ReportsPanel();
        
        // Add panels for different modules
        mainPanel.add(supplierPanel, "Suppliers");
        mainPanel.add(inventoryPanel, "Inventory");
        mainPanel.add(transactionPanel, "Transactions");
        mainPanel.add(customerPanel, "Customers");
        mainPanel.add(reportsPanel, "Reports");
        
        add(mainPanel);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // Modules Menu
        JMenu modulesMenu = new JMenu("Modules");
        JMenuItem suppliersItem = new JMenuItem("Suppliers");
        JMenuItem inventoryItem = new JMenuItem("Inventory");
        JMenuItem transactionsItem = new JMenuItem("Transactions");
        JMenuItem customersItem = new JMenuItem("Customers");
        JMenuItem reportsItem = new JMenuItem("Reports");
        
        suppliersItem.addActionListener(e -> cardLayout.show(mainPanel, "Suppliers"));
        inventoryItem.addActionListener(e -> {
            cardLayout.show(mainPanel, "Inventory");
            inventoryPanel.refreshSuppliers();
            inventoryPanel.loadInventory();
        });
        transactionsItem.addActionListener(e -> {
            cardLayout.show(mainPanel, "Transactions");
            transactionPanel.refreshSuppliers();
            transactionPanel.loadTransactions();
        });
        customersItem.addActionListener(e -> {
            cardLayout.show(mainPanel, "Customers");
            customerPanel.loadCustomers();
        });
        reportsItem.addActionListener(e -> cardLayout.show(mainPanel, "Reports"));
        
        modulesMenu.add(suppliersItem);
        modulesMenu.add(customersItem);
        modulesMenu.add(inventoryItem);
        modulesMenu.add(transactionsItem);
        modulesMenu.add(reportsItem);
        
        menuBar.add(fileMenu);
        menuBar.add(modulesMenu);
        
        setJMenuBar(menuBar);
    }
    
    public TransactionPanel getTransactionPanel() {
        return transactionPanel;
    }
    
    public InventoryPanel getInventoryPanel() {
        return inventoryPanel;
    }
    
    public static void main(String[] args) {
        try {
            // Set look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Show main form
            SwingUtilities.invokeLater(() -> {
                MainForm form = new MainForm();
                form.setVisible(true);
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error initializing application: " + e.getMessage());
        }
    }
} 