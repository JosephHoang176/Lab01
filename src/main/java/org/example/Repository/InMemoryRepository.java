package org.example.Repository;

import org.example.DTO.request.OrderDTO;
import org.example.Entity.Order;
import org.example.interfaces.IOrderRepository;

import java.util.List;

public class InMemoryRepository implements IOrderRepository {

    private final List<OrderDTO> orders;

    public InMemoryRepository(List<OrderDTO> orders) {
        this.orders = orders;
    }

    @Override
    public List<Order> getAllOrders() {
        return null;
    }

    @Override
    public Order getOrderById(int orderId) {
        return null;
    }

    @Override
    public Order save(Order order) {
        return null;
    }

    @Override
    public Order update(Order order) {
        return null;
    }
}