package org.example.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Products", schema = "dbo")
public class Product {
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "sku", nullable = false, length = 100, unique = true)
    private String sku;
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    @Column(name = "category", length = 100)
    private String category;
    @Column(name = "unit_price", nullable = false)
    private long unitPrice;
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    @Column(name = "stock", nullable = false)
    private int stock;
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public Product() {
    }

    public Product(int id, String sku, String name, String category, long unitPrice, String currency, int stock, boolean isActive) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.currency = currency;
        this.stock = stock;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
