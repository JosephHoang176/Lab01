package org.example.Repository;

import org.example.Entity.Order;
import org.example.interfaces.IOrderRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

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
        return repository.findAll();
    }

    @Override
    public Order getOrderById(int orderId) {
        return repository.findById(orderId).orElse(null);
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
