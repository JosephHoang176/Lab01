package com.example.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "notifications",
        schema = "dbo",
        indexes = {
                @Index(name = "IX_notifications_order_timeline", columnList = "order_id, occurred_at DESC"),
                @Index(name = "IX_notifications_occurred_at", columnList = "occurred_at DESC")
        }
)
public class Notifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "source_event_id")
    private Long sourceEventId;

    @Column(name = "source_service", nullable = false, length = 50)
    private String sourceService = "core-api";

    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_code", nullable = false, length = 30)
    private String orderCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private PreviousStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private NewStatus newStatus;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getSourceEventId() {
        return sourceEventId;
    }

    public void setSourceEventId(Long sourceEventId) {
        this.sourceEventId = sourceEventId;
    }

    public String getSourceService() {
        return sourceService;
    }

    public void setSourceService(String sourceService) {
        this.sourceService = sourceService;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public PreviousStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(PreviousStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public NewStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(NewStatus newStatus) {
        this.newStatus = newStatus;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    @PrePersist
    private void setDefaultsBeforeInsert() {
        if (sourceService == null) {
            sourceService = "core-api";
        }
        if (recordedAt == null) {
            recordedAt = OffsetDateTime.now().withNano(0);
        }
    }
}
