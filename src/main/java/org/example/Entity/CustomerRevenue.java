package org.example.Entity;

public record CustomerRevenue(
        int customerId,
        String customerName,
        int orders,
        double revenue
) {
}
