package com.example.Repository;

import com.example.Entity.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notifications, Long> {

    List<Notifications> findByOrderIdOrderByOccurredAtDesc(Long orderId);

    List<Notifications> findByOrderCodeOrderByOccurredAtDesc(String orderCode);

    List<Notifications> findBySourceServiceOrderByOccurredAtDesc(String sourceService);

    Optional<Notifications> findBySourceServiceAndSourceEventId(
            String sourceService,
            Long sourceEventId
    );
}
