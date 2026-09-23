package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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
