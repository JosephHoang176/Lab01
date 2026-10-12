package com.example.Dto;

import com.example.Entity.Reports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ReportResponse(
        Long reportId,
        LocalDate reportDate,
        String currency,
        Integer ordersCreatedCount,
        Integer ordersPaidCount,
        Integer ordersFulfilledCount,
        Integer ordersCancelledCount,
        Integer pendingPaymentCountEod,
        BigDecimal ordersCreatedAmount,
        BigDecimal paymentsReceivedAmount,
        OffsetDateTime generatedAt
) {
    public static ReportResponse from(Reports report) {
        return new ReportResponse(
                report.getReportId(), report.getReportDate(), report.getCurrency(),
                report.getOrdersCreatedCount(), report.getOrdersPaidCount(),
                report.getOrdersFulfilledCount(), report.getOrdersCancelledCount(),
                report.getPendingPaymentCountEod(), report.getOrdersCreatedAmount(),
                report.getPaymentsReceivedAmount(), report.getGeneratedAt());
    }
}
