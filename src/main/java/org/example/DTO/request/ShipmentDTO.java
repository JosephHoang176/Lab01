package org.example.DTO.request;

import jakarta.validation.constraints.NotBlank;
import org.example.enums.ShippingStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ShipmentDTO(
        @NotBlank(message = "Code khong duoc de trong") int id,
    int orderId,
    String orderCode,
    String carrier,
    String trackingNumber,
    ShippingStatus status,
    LocalDate estimatedDelivery,
    OffsetDateTime lastUpdated
) {
}
