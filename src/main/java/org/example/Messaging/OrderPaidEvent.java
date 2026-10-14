package org.example.Messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderPaidEvent(
        String eventId,
        String eventType,
        int eventVersion,
        OffsetDateTime occurredAt,
        int orderId,
        int customerId,
        String currency,
        BigDecimal amount
) {
    public static OrderPaidEvent from(
            String eventId,
            int orderId,
            int customerId,
            String currency,
            double amount,
            OffsetDateTime occurredAt) {
        return new OrderPaidEvent(
                eventId,
                "order.paid",
                1,
                occurredAt,
                orderId,
                customerId,
                currency,
                BigDecimal.valueOf(amount));
    }
}
