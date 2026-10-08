package org.example.Repository;

import org.example.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.example.enums.OrderStatus;

public interface OrderJPARepository extends JpaRepository<Order, Integer> {
    @Query("select distinct o from Order o left join fetch o.lines")
    List<Order> findAllWithLines();

    @Query("select distinct o from Order o left join fetch o.lines where o.id = :orderId")
    Optional<Order> findByIdWithLines(@Param("orderId") int orderId);

    @Query("""
            select distinct o
              from Order o
              left join fetch o.lines
             where (:status is null or o.status = :status)
               and (:fromInclusive is null or o.createdAt >= :fromInclusive)
               and (:toExclusive is null or o.createdAt < :toExclusive)
            """)
    List<Order> findFilteredWithLines(
            @Param("status") OrderStatus status,
            @Param("fromInclusive") OffsetDateTime fromInclusive,
            @Param("toExclusive") OffsetDateTime toExclusive
    );

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
