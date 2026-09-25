package org.example.Repository;

import org.example.DTO.Order;
import org.example.interfaces.IOrderCalculator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Calculate implements IOrderCalculator {

    @Override
    public double calculateRevenue(List<Order> orders) {

        if (orders == null || orders.isEmpty()) {
            return 0.0;
        }

        double revenue = 0.0;
        for (Order order : orders) {
            revenue += order.total();
        }
        return revenue;
    }
}