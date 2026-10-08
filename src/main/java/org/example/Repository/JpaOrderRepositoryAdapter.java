package org.example.Repository;

import org.example.Entity.Order;
import org.example.enums.OrderStatus;
import org.example.interfaces.IOrderRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
@Primary
public class JpaOrderRepositoryAdapter implements IOrderRepository {

    private final OrderJPARepository repository;

    public JpaOrderRepositoryAdapter(OrderJPARepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Order> getAllOrders() {
        return repository.findAllWithLines();
    }

    @Override
    public List<Order> findOrders(OrderStatus status,
                                  OffsetDateTime fromInclusive,
                                  OffsetDateTime toExclusive) {
        return repository.findFilteredWithLines(status, fromInclusive, toExclusive);
    }

    @Override
    public Order getOrderById(int orderId) {
        return repository.findByIdWithLines(orderId).orElse(null);
    }

    @Override
    public Order save(Order order) {
        return repository.save(order);
    }

    @Override
    public Order update(Order order) {
        return repository.save(order);
    }
}
