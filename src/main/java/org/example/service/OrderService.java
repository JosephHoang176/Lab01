package org.example.service;

import org.example.model.DateRange;
import org.example.model.Order;
import org.example.model.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderService {
    public List<Order> filterOrders(List<Order> orders, OrderStatus status, DateRange dateRange) {
        return orders.stream()
                .filter(order -> status == null || order.status() == status)
                .filter(order -> {
                    if (dateRange == null) return true;
                    // Sử dụng Optional để xử lý an toàn trường hợp createdAt bị null
                    return Optional.ofNullable(order.createdAt())
                            .map(dt -> dateRange.includes(dt.toLocalDate()))
                            .orElse(false);
                })
                .toList();
    }

    /**
     * 1. Tính tổng doanh thu theo từng Trạng thái (Compute revenue per status)
     */
    public Map<OrderStatus, Double> computeRevenuePerStatus(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        Order::status,
                        Collectors.summingDouble(Order::total)
                ));
    }

    /**
     * 2. Tính tổng doanh thu theo từng Ngày (Compute revenue per day)
     */
    public Map<LocalDate, Double> computeRevenuePerDay(List<Order> orders) {
        return orders.stream()
                .filter(order -> order.createdAt() != null)
                .collect(Collectors.groupingBy(
                        order -> order.createdAt().toLocalDate(),
                        Collectors.summingDouble(Order::total)
                ));
    }

    /**
     * Tính tổng doanh thu của toàn bộ danh sách đơn hàng
     */
    public double calculateTotalRevenue(List<Order> orders) {
        return orders.stream()
                .mapToDouble(Order::total)
                .sum();
    }
}
