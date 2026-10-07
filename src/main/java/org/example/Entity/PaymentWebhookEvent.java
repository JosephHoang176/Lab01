package org.example.Entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "PaymentWebhookEvents",
        uniqueConstraints = @UniqueConstraint(name = "UQ_PaymentWebhookEvents_EventId", columnNames = "event_id"))
public class PaymentWebhookEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 200, unique = true)
    private String eventId;

    @Column(name = "order_id", nullable = false)
    private int orderId;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    protected PaymentWebhookEvent() {
    }

    public PaymentWebhookEvent(String eventId, int orderId, OffsetDateTime receivedAt) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.receivedAt = receivedAt;
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public int getOrderId() { return orderId; }
    public OffsetDateTime getReceivedAt() { return receivedAt; }
}
