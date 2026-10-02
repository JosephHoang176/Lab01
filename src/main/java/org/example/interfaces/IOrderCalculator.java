package org.example.interfaces;

import org.example.Entity.Order;

import java.util.List;

public interface IOrderCalculator {

    double calculateRevenue(List<Order> orders);
}
