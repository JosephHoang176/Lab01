package org.example.interfaces;

import org.example.DTO.Order;

import java.util.List;

public interface IOrderRepository {
    List<Order> getAllOrders();
}
