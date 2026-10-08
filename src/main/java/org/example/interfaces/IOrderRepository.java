package org.example.interfaces;

import org.example.Entity.Order;
import org.example.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;

public interface IOrderRepository {
    List<Order> getAllOrders();

    /**
     * Implementations backed by a database should apply these predicates before
     * loading orders. The default keeps lightweight in-memory adapters compatible.
     */
    default List<Order> findOrders(OrderStatus status,
                                   OffsetDateTime fromInclusive,
                                   OffsetDateTime toExclusive) {
        return getAllOrders();
    }

    Order getOrderById(int orderId);

    Order save(Order order);

    Order update(Order order);
}
