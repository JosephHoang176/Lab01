package org.example.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record Order(
        int id,
        String code,
        int customerId,
        String customerName,
        int createdBy,
        OrderStatus status,

        @JsonProperty("lines")
        List<LineItem> lines,

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
        OffsetDateTime cancelledAt
) {}
