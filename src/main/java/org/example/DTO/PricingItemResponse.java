package org.example.DTO;

public record PricingItemResponse(
        int quantity,
        long unitPrice,
        long lineTotal
) {
}
