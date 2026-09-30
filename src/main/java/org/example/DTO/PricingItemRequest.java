package org.example.DTO;

public record PricingItemRequest(
        int quantity,
        long unitPrice
) {
}
