package org.example.Repository;

import org.example.Entity.PaymentWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentWebhookEventRepository extends JpaRepository<PaymentWebhookEvent, Long> {
    boolean existsByEventId(String eventId);
}
