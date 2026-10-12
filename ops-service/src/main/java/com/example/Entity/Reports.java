package com.example.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "reports",
        schema = "dbo",
        uniqueConstraints = @UniqueConstraint(
                name = "UQ_reports_report_date_currency",
                columnNames = {"report_date", "currency"}
        )
)
public class Reports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "currency", nullable = false, length = 3, columnDefinition = "char(3)")
    private String currency = "VND";

    @Column(name = "orders_created_count", nullable = false)
    private Integer ordersCreatedCount = 0;

    @Column(name = "orders_paid_count", nullable = false)
    private Integer ordersPaidCount = 0;

    @Column(name = "orders_fulfilled_count", nullable = false)
    private Integer ordersFulfilledCount = 0;

    @Column(name = "orders_cancelled_count", nullable = false)
    private Integer ordersCancelledCount = 0;

    @Column(name = "pending_payment_count_eod", nullable = false)
    private Integer pendingPaymentCountEod = 0;

    @Column(name = "orders_created_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal ordersCreatedAmount = BigDecimal.ZERO;

    @Column(name = "payments_received_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paymentsReceivedAmount = BigDecimal.ZERO;

    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt;

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getOrdersCreatedCount() {
        return ordersCreatedCount;
    }

    public void setOrdersCreatedCount(Integer ordersCreatedCount) {
        this.ordersCreatedCount = ordersCreatedCount;
    }

    public Integer getOrdersPaidCount() {
        return ordersPaidCount;
    }

    public void setOrdersPaidCount(Integer ordersPaidCount) {
        this.ordersPaidCount = ordersPaidCount;
    }

    public Integer getOrdersFulfilledCount() {
        return ordersFulfilledCount;
    }

    public void setOrdersFulfilledCount(Integer ordersFulfilledCount) {
        this.ordersFulfilledCount = ordersFulfilledCount;
    }

    public Integer getOrdersCancelledCount() {
        return ordersCancelledCount;
    }

    public void setOrdersCancelledCount(Integer ordersCancelledCount) {
        this.ordersCancelledCount = ordersCancelledCount;
    }

    public Integer getPendingPaymentCountEod() {
        return pendingPaymentCountEod;
    }

    public void setPendingPaymentCountEod(Integer pendingPaymentCountEod) {
        this.pendingPaymentCountEod = pendingPaymentCountEod;
    }

    public BigDecimal getOrdersCreatedAmount() {
        return ordersCreatedAmount;
    }

    public void setOrdersCreatedAmount(BigDecimal ordersCreatedAmount) {
        this.ordersCreatedAmount = ordersCreatedAmount;
    }

    public BigDecimal getPaymentsReceivedAmount() {
        return paymentsReceivedAmount;
    }

    public void setPaymentsReceivedAmount(BigDecimal paymentsReceivedAmount) {
        this.paymentsReceivedAmount = paymentsReceivedAmount;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(OffsetDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    @PrePersist
    private void setDefaultsBeforeInsert() {
        if (currency == null) {
            currency = "VND";
        }
        if (ordersCreatedCount == null) {
            ordersCreatedCount = 0;
        }
        if (ordersPaidCount == null) {
            ordersPaidCount = 0;
        }
        if (ordersFulfilledCount == null) {
            ordersFulfilledCount = 0;
        }
        if (ordersCancelledCount == null) {
            ordersCancelledCount = 0;
        }
        if (pendingPaymentCountEod == null) {
            pendingPaymentCountEod = 0;
        }
        if (ordersCreatedAmount == null) {
            ordersCreatedAmount = BigDecimal.ZERO;
        }
        if (paymentsReceivedAmount == null) {
            paymentsReceivedAmount = BigDecimal.ZERO;
        }
        if (generatedAt == null) {
            generatedAt = OffsetDateTime.now().withNano(0);
        }
    }
}
