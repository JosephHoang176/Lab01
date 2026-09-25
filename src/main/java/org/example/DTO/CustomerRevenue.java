package org.example.DTO;

public record CustomerRevenue(
        int customerId,
        String customerName,
        int orders,
        double revenue
) {
}
