package org.example.DTO;

import org.example.enums.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record OrderReport(
        ReportTotals totals,
        Map<OrderStatus, Integer> countByStatus,
        Map<OrderStatus, Double> sumOfTotalByStatus,
        Map<LocalDate, DayRevenue> revenueByDay,
        List<CustomerRevenue> revenueByCustomer
) {
}
