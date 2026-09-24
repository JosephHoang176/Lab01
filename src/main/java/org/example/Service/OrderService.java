package org.example.Service;

import org.example.Entity.DateRange;
import org.example.Entity.FilteredOrderReport;
import org.example.Entity.Order;
import org.example.Entity.OrderReport;
import org.example.Entity.OrderStatus;
import org.example.interfaces.IOrderCalculator;
import org.example.interfaces.IOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final IOrderCalculator calculator;
    private final IOrderRepository repository;

    public OrderService(
            IOrderCalculator calculator,
            IOrderRepository repository
    ) {
        this.calculator = calculator;
        this.repository = repository;
    }

    public List<Order> findFilteredOrders(
            OrderStatus status,
            DateRange dateRange
    ) {

        List<Order> orders =
                repository.getAllOrders();

        if (orders == null || orders.isEmpty()) {
            return List.of();
        }

        return orders.stream()
                .filter(order ->
                        matchesStatus(order, status)
                )
                .filter(order ->
                        matchesDate(order, dateRange)
                )
                .toList();
    }

    public double getRevenueFilteredByStatusAndDate(
            OrderStatus status,
            DateRange dateRange
    ) {

        List<Order> filteredOrders =
                findFilteredOrders(
                        status,
                        dateRange
                );

        return calculator.calculateRevenue(
                filteredOrders
        );
    }

    public FilteredOrderReport getFilteredOrderReport(
            OrderStatus status,
            DateRange dateRange
    ) {

        List<Order> filteredOrders =
                findFilteredOrders(
                        status,
                        dateRange
                );

        double sumOfTotal =
                calculator.calculateSumOfTotal(
                        filteredOrders
                );

        List<String> orderCodes =
                filteredOrders.stream()
                        .map(Order::code)
                        .toList();

        return new FilteredOrderReport(
                filteredOrders.size(),
                sumOfTotal,
                orderCodes
        );
    }

    public OrderReport generateReport(
            OrderStatus status,
            DateRange dateRange
    ) {

        List<Order> filteredOrders =
                findFilteredOrders(
                        status,
                        dateRange
                );

        return calculator.generateReport(
                filteredOrders
        );
    }

    private boolean matchesStatus(
            Order order,
            OrderStatus status
    ) {

        if (status == null) {
            return true;
        }

        return order.status() == status;
    }

    private boolean matchesDate(
            Order order,
            DateRange dateRange
    ) {

        if (dateRange == null) {
            return true;
        }

        if (order.createdAt() == null) {
            return false;
        }

        return dateRange.includes(
                order.createdAt().toLocalDate()
        );
    }
}