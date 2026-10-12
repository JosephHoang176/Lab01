package com.example.Service;

import com.example.Entity.Notifications;
import com.example.Repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private static final String ORDER_CREATED = "order.created";
    private static final String ORDER_STATUS_CHANGED = "order.status_changed";

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notifications getById(Long notificationId) {
        requireId(notificationId, "notificationId");
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Notification not found: " + notificationId));
    }

    public List<Notifications> getByOrderId(Long orderId) {
        requireId(orderId, "orderId");
        return notificationRepository.findByOrderIdOrderByOccurredAtDesc(orderId);
    }

    public List<Notifications> getByOrderCode(String orderCode) {
        requireText(orderCode, "orderCode");
        return notificationRepository.findByOrderCodeOrderByOccurredAtDesc(orderCode);
    }

    public List<Notifications> getBySourceService(String sourceService) {
        requireText(sourceService, "sourceService");
        return notificationRepository.findBySourceServiceOrderByOccurredAtDesc(sourceService);
    }

    public Notifications findBySourceEvent(String sourceService, Long sourceEventId) {
        requireText(sourceService, "sourceService");
        requireId(sourceEventId, "sourceEventId");
        return notificationRepository.findBySourceServiceAndSourceEventId(sourceService, sourceEventId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Notification not found for source service and event"));
    }

    @Transactional
    public Notifications create(Notifications notification) {
        validate(notification);
        if (notification.getNotificationId() != null) {
            throw new IllegalArgumentException("A new notification must not have an ID");
        }
        if (notification.getOccurredAt() == null) {
            notification.setOccurredAt(OffsetDateTime.now().withNano(0));
        }
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notifications update(Long notificationId, Notifications changes) {
        Notifications notification = getById(notificationId);
        validate(changes);
        notification.setSourceEventId(changes.getSourceEventId());
        notification.setSourceService(changes.getSourceService());
        notification.setEventType(changes.getEventType());
        notification.setOrderId(changes.getOrderId());
        notification.setOrderCode(changes.getOrderCode());
        notification.setPreviousStatus(changes.getPreviousStatus());
        notification.setNewStatus(changes.getNewStatus());
        if (changes.getOccurredAt() != null) {
            notification.setOccurredAt(changes.getOccurredAt());
        }
        return notificationRepository.save(notification);
    }

    @Transactional
    public void delete(Long notificationId) {
        Notifications notification = getById(notificationId);
        notificationRepository.delete(notification);
    }

    private void validate(Notifications notification) {
        if (notification == null) {
            throw new IllegalArgumentException("notification must not be null");
        }
        requireId(notification.getOrderId(), "orderId");
        requireText(notification.getOrderCode(), "orderCode");
        requireText(notification.getEventType(), "eventType");
        requireText(notification.getSourceService(), "sourceService");
        if (notification.getNewStatus() == null) {
            throw new IllegalArgumentException("newStatus must not be null");
        }
        if (ORDER_CREATED.equals(notification.getEventType())) {
            if (notification.getPreviousStatus() != null) {
                throw new IllegalArgumentException(
                        "previousStatus must be null for order.created");
            }
        } else if (ORDER_STATUS_CHANGED.equals(notification.getEventType())) {
            if (notification.getPreviousStatus() == null) {
                throw new IllegalArgumentException(
                        "previousStatus is required for order.status_changed");
            }
            if (notification.getNewStatus().name().equals(notification.getPreviousStatus().name())) {
                throw new IllegalArgumentException(
                        "previousStatus and newStatus must be different");
            }
        } else {
            throw new IllegalArgumentException("Unsupported eventType: " + notification.getEventType());
        }
    }

    private static void requireId(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
