package models;

import java.util.Date;

public class Transaction {
    private int id;
    private String type;
    private Date date;
    private int itemId;
    private int supplierId;
    private int customerId;
    private double quantity;
    private double amount;
    private String status;
    
    public Transaction(int id, String type, Date date, int itemId, int supplierId, int customerId, 
                      double quantity, double amount, String status) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.itemId = itemId;
        this.supplierId = supplierId;
        this.customerId = customerId;
        this.quantity = quantity;
        this.amount = amount;
        this.status = status;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return String.format("%s - %s - %.2f", type, date, amount);
    }
} 