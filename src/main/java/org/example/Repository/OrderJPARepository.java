package org.example.Repository;

import org.example.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface OrderJPARepository extends JpaRepository<Order, Integer> {
    @Modifying
    @Query("""
            update Order o
               set o.status = org.example.enums.OrderStatus.PAID,
                   o.paidAt = :paidAt
             where o.id = :orderId
               and o.status = org.example.enums.OrderStatus.PENDING_PAYMENT
            """)
    int markPaid(@Param("orderId") int orderId, @Param("paidAt") OffsetDateTime paidAt);
}
