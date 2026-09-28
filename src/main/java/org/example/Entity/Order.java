package org.example.Entity;

import org.example.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private String code;
    private int customerId;
    private String customerName;
    private int createdBy;
    private OrderStatus status;
    private List<Order_Item> lines = new ArrayList<>();
    private double subtotal;
    private double discountPercent;
    private double discountAmount;
    private double taxPercent;
    private double taxAmount;
    private double total;
    private String currency;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime paidAt;
    private OffsetDateTime fulfilledAt;
    private OffsetDateTime cancelledAt;

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
}
