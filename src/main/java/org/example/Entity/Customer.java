package org.example.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "customers", schema = "dbo")
public class Customer {
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    @Column(name = "tier", length = 50)
    private String tier;
    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;
    @Column(name = "city", length = 100)
    private String city;
    @Column(name = "contact_email", length = 320)
    private String contactEmail;
    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    public Customer() {
    }

    public Customer(int id, String code, String name, String tier, int discountPercent,
                    String city, String contactEmail, String contactPhone) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.tier = tier;
        this.discountPercent = BigDecimal.valueOf(discountPercent);
        this.city = city;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public int getDiscountPercent() {
        return discountPercent == null ? 0 : discountPercent.intValue();
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = BigDecimal.valueOf(discountPercent);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}
