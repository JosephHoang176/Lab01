package com.example.Service;

import com.example.Entity.Reports;
import com.example.Repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final String DEFAULT_CURRENCY = "VND";

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Reports getById(Long reportId) {
        requireId(reportId, "reportId");
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found: " + reportId));
    }

    public Optional<Reports> findByDateAndCurrency(LocalDate reportDate, String currency) {
        requireDate(reportDate);
        return reportRepository.findByReportDateAndCurrency(reportDate, normalizeCurrency(currency));
    }

    public List<Reports> getBetween(LocalDate fromDate, LocalDate toDate) {
        requireDateRange(fromDate, toDate);
        return reportRepository.findByReportDateBetweenOrderByReportDateDesc(fromDate, toDate);
    }

    public List<Reports> getByCurrency(String currency) {
        return reportRepository.findByCurrencyOrderByReportDateDesc(normalizeCurrency(currency));
    }

    public List<Reports> getDailyReports() {
        return reportRepository.findAllByOrderByReportDateDescGeneratedAtDesc();
    }

    @Transactional
    public Reports save(Reports report) {
        validate(report);
        report.setCurrency(normalizeCurrency(report.getCurrency()));
        return reportRepository.save(report);
    }

    @Transactional
    public Reports createOrReplace(Reports report) {
        validate(report);
        String currency = normalizeCurrency(report.getCurrency());
        Reports existing = reportRepository.findByReportDateAndCurrency(
                        report.getReportDate(), currency)
                .orElse(null);
        if (existing == null) {
            report.setCurrency(currency);
            return reportRepository.save(report);
        }
        existing.setOrdersCreatedCount(report.getOrdersCreatedCount());
        existing.setOrdersPaidCount(report.getOrdersPaidCount());
        existing.setOrdersFulfilledCount(report.getOrdersFulfilledCount());
        existing.setOrdersCancelledCount(report.getOrdersCancelledCount());
        existing.setPendingPaymentCountEod(report.getPendingPaymentCountEod());
        existing.setOrdersCreatedAmount(report.getOrdersCreatedAmount());
        existing.setPaymentsReceivedAmount(report.getPaymentsReceivedAmount());
        if (report.getGeneratedAt() != null) {
            existing.setGeneratedAt(report.getGeneratedAt());
        }
        return reportRepository.save(existing);
    }

    @Transactional
    public void delete(Long reportId) {
        Reports report = getById(reportId);
        reportRepository.delete(report);
    }

    private void validate(Reports report) {
        if (report == null) {
            throw new IllegalArgumentException("report must not be null");
        }
        requireDate(report.getReportDate());
        validateNonNegative(report.getOrdersCreatedCount(), "ordersCreatedCount");
        validateNonNegative(report.getOrdersPaidCount(), "ordersPaidCount");
        validateNonNegative(report.getOrdersFulfilledCount(), "ordersFulfilledCount");
        validateNonNegative(report.getOrdersCancelledCount(), "ordersCancelledCount");
        validateNonNegative(report.getPendingPaymentCountEod(), "pendingPaymentCountEod");
        validateNonNegative(report.getOrdersCreatedAmount(), "ordersCreatedAmount");
        validateNonNegative(report.getPaymentsReceivedAmount(), "paymentsReceivedAmount");
    }

    private static String normalizeCurrency(String currency) {
        String value = currency == null || currency.isBlank() ? DEFAULT_CURRENCY : currency.trim().toUpperCase();
        if (value.length() != 3) {
            throw new IllegalArgumentException("currency must contain exactly 3 characters");
        }
        return value;
    }

    private static void requireDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("reportDate must not be null");
        }
    }

    private static void requireDateRange(LocalDate fromDate, LocalDate toDate) {
        requireDate(fromDate);
        requireDate(toDate);
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must not be after toDate");
        }
    }

    private static void validateNonNegative(Number value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        if (value instanceof BigDecimal decimal && decimal.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        if (!(value instanceof BigDecimal) && value.longValue() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
    }

    private static void requireId(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }
}
