package org.example.DTO;

import org.example.enums.ShippingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record Shipment(
    int id,
    int orderId,
    String orderCode,
    String carrier,
    String trackingNumber,
    ShippingStatus status,
    LocalDate estimatedDelivery,
    OffsetDateTime lastUpdated
) {
}
