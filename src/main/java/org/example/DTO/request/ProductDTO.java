package org.example.DTO.request;

public record ProductDTO (
        int id,
        String sku,
        String name,
        String category,
        long unitPrice,
        String currency,
        int stock,
        boolean isActive
) {}
