package org.example.interfaces;

import org.example.DTO.Order;
import org.example.DTO.OrderReport;

import java.util.List;

public interface IOrderCalculator {

    double calculateRevenue(List<Order> orders);
}
