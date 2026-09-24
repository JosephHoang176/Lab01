package org.example.interfaces;

import org.example.Entity.Order;

import java.util.List;

public interface IOrderRepository {
    List<Order> getAllOrders();
}
