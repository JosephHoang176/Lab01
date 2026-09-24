package org.example.Entity;

import java.util.List;

public record FilteredOrderReport(
        int orderCount,
        double sumOfTotal,
        List<String> orderCodes
) {
}
