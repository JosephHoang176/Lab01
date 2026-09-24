package org.example.Entity;

import java.time.LocalDate;

public record ReportTotals(
        int orderCount,
        double revenue,
        int revenueOrderCount,
        LocalDate firstOrderDate,
        LocalDate lastOrderDate
) {

}
