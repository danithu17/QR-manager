package com.example.qrmanager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "qr_codes")
public class QRCode {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String name;
    private String category;
    private String imagePath;
    private String status; // Available, Pending, Sold, Pre-ordered
    private long timestamp;
    private String customerName;
    private String customerPhone;

    public QRCode(String name, String category, String imagePath, String status, long timestamp) {
        this.name = name;
        this.category = category;
        this.imagePath = imagePath;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getImagePath() { return imagePath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
}
