package org.example.DTO;

import java.util.List;

public record FilteredOrderReport(
        int orderCount,
        double sumOfTotal,
        List<String> orderCodes
) {
}
