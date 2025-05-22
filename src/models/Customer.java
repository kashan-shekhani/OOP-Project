package models;

public class Customer {
    private int id;
    private String name;
    private String contact;
    private String address;
    private String cnic;
    
    public Customer(int id, String name, String contact, String address, String cnic) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.address = address;
        this.cnic = cnic;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCnic() { return cnic; }
    public void setCnic(String cnic) { this.cnic = cnic; }
    
    @Override
    public String toString() {
        return name;
    }
} 