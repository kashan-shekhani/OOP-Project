package models;

public class Inventory {
    private int id;
    private String name;
    private double quantity;
    private double price;
    private int supplierId;
    
    public Inventory(int id, String name, double quantity, double price, int supplierId) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.supplierId = supplierId;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    
    @Override
    public String toString() {
        return name;
    }
} 