package org.example.Repository;

import org.example.Entity.Order;
import org.example.interfaces.IOrderRepository;

import java.util.List;

public class InMemoryRepository implements IOrderRepository {

    private final List<Order> orders;

    public InMemoryRepository(List<Order> orders) {
        this.orders = orders;
    }

    @Override
    public List<Order> getAllOrders() {
        return orders;
    }
}