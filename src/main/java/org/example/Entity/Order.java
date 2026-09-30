package org.example.Entity;

import org.example.enums.OrderStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Orders", schema = "dbo")
public class Order {
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;
    @Column(name = "customer_id", nullable = false)
    private int customerId;
    @Column(name = "customer_name", length = 200)
    private String customerName;
    @Column(name = "created_by", nullable = false)
    private int createdBy;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order_Item> lines = new ArrayList<>();
    @Column(name = "subtotal", nullable = false)
    private double subtotal;
    @Column(name = "discount_percent", nullable = false)
    private double discountPercent;
    @Column(name = "discount_amount", nullable = false)
    private double discountAmount;
    @Column(name = "tax_percent", nullable = false)
    private double taxPercent;
    @Column(name = "tax_amount", nullable = false)
    private double taxAmount;
    @Column(name = "total", nullable = false)
    private double total;
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    @Column(name = "paid_at")
    private OffsetDateTime paidAt;
    @Column(name = "fulfilled_at")
    private OffsetDateTime fulfilledAt;
    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "pricing_status", nullable = false, length = 20)
    private String pricingStatus = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User creator;

    public Order() {
    }

    public Order(int id,
                 String code,
                 int customerId,
                 String customerName,
                 int createdBy,
                 OrderStatus status,
                 List<Order_Item> lines,
                 double subtotal,
                 double discountPercent,
                 double discountAmount,
                 double taxPercent,
                 double taxAmount,
                 double total,
                 String currency,
                 OffsetDateTime createdAt,
                 OffsetDateTime updatedAt,
                 OffsetDateTime paidAt,
                 OffsetDateTime fulfilledAt,
                 OffsetDateTime cancelledAt) {
        this.id = id;
        this.code = code;
        this.customerId = customerId;
        this.customerName = customerName;
        this.createdBy = createdBy;
        this.status = status;
        this.lines = lines == null ? new ArrayList<>() : new ArrayList<>(lines);
        this.subtotal = subtotal;
        this.discountPercent = discountPercent;
        this.discountAmount = discountAmount;
        this.taxPercent = taxPercent;
        this.taxAmount = taxAmount;
        this.total = total;
        this.currency = currency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.paidAt = paidAt;
        this.fulfilledAt = fulfilledAt;
        this.cancelledAt = cancelledAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public List<Order_Item> getLines() { return lines; }
    public void setLines(List<Order_Item> lines) {
        this.lines = lines == null ? new ArrayList<>() : new ArrayList<>(lines);
        for (Order_Item line : this.lines) {
            line.setOrder(this);
            line.setOrderId(this.id);
        }
    }
    public void addLine(Order_Item line) {
        if (line == null) {
            return;
        }
        line.setOrder(this);
        line.setOrderId(this.id);
        this.lines.add(line);
    }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public double getTaxPercent() { return taxPercent; }
    public void setTaxPercent(double taxPercent) { this.taxPercent = taxPercent; }
    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public OffsetDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(OffsetDateTime paidAt) { this.paidAt = paidAt; }
    public OffsetDateTime getFulfilledAt() { return fulfilledAt; }
    public void setFulfilledAt(OffsetDateTime fulfilledAt) { this.fulfilledAt = fulfilledAt; }
    public OffsetDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(OffsetDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getPricingStatus() { return pricingStatus; }
    public void setPricingStatus(String pricingStatus) { this.pricingStatus = pricingStatus; }
}
