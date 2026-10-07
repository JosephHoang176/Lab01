package org.example.DTO.request;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

public record PaymentWebhookRequest(
        @JsonAlias({"event_id", "id"}) String eventId,
        @JsonAlias({"order_id"}) int orderId,
        @JsonAlias({"payment_id", "transaction_id"}) String paymentId,
        BigDecimal amount,
        String currency,
        String status
) {
}
