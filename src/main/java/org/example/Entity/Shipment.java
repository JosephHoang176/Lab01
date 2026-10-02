package org.example.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.example.enums.ShippingStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "Shipments")
public class Shipment {
    @Id
    private int id;
    private int orderId;
    private String orderCode;
    private String carrier;
    private String trackingNumber;
    private ShippingStatus status;
    private LocalDate estimatedDelivery;
    private OffsetDateTime lastUpdated;

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
