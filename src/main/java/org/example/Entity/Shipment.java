package org.example.Entity;

import org.example.enums.ShippingStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "shipments", schema = "dbo")
public class Shipment {
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "order_id", nullable = false)
    private int orderId;
    @Column(name = "order_code", nullable = false, length = 50)
    private String orderCode;
    @Column(name = "carrier", length = 100)
    private String carrier;
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShippingStatus status;
    @Column(name = "estimated_delivery")
    private LocalDate estimatedDelivery;
    @Column(name = "last_updated", nullable = false)
    private OffsetDateTime lastUpdated;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    public Shipment() {
    }

    public Shipment(int id, int orderId, String orderCode, String carrier,
                    String trackingNumber, ShippingStatus status,
                    LocalDate estimatedDelivery, OffsetDateTime lastUpdated) {
        this.id = id;
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.estimatedDelivery = estimatedDelivery;
        this.lastUpdated = lastUpdated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public ShippingStatus getStatus() {
        return status;
    }

    public void setStatus(ShippingStatus status) {
        this.status = status;
    }

    public LocalDate getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(LocalDate estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
