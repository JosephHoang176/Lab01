package org.example.interfaces;

import org.example.Entity.Order;
import org.example.Entity.OrderReport;

import java.util.List;

public interface IOrderCalculator {

    double calculateRevenue(List<Order> orders);

    double calculateSumOfTotal(List<Order> orders);

    OrderReport generateReport(List<Order> orders);
}
