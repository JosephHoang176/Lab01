package org.example.interfaces;

import org.example.DTO.OrderDTO;
import org.example.Entity.Order;

import java.util.List;

public interface IOrderRepository {
    List<Order> getAllOrders();

    Order getOrderById(int orderId);

    Order save(Order order);

    Order update(Order order);
}
