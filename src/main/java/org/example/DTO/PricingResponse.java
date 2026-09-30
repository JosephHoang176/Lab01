package org.example.DTO;

import java.util.List;

public record PricingResponse(
        List<PricingItemResponse> items,
        long subtotal,
        long discountAmount,
        long taxAmount,
        long total
) {
}
