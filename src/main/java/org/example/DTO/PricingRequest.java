package org.example.DTO;

import java.util.List;

public record PricingRequest(
        List<PricingItemRequest> items,
        double discountPercent,
        double taxPercent
) {
}
