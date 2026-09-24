package org.example.Service;

import org.example.Entity.CustomerRevenue;
import org.example.Entity.DayRevenue;
import org.example.Entity.Order;
import org.example.Entity.OrderReport;
import org.example.Entity.OrderStatus;
import org.example.Entity.ReportTotals;
import org.example.interfaces.IOrderCalculator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class Calculate implements IOrderCalculator {

    @Override
    public double calculateRevenue(List<Order> orders) {

        if (orders == null || orders.isEmpty()) {
            return 0.0;
        }

        return orders.stream()
                .filter(this::isRevenueOrder)
                .mapToDouble(Order::total)
                .sum();
    }

    @Override
    public double calculateSumOfTotal(List<Order> orders) {

        if (orders == null || orders.isEmpty()) {
            return 0.0;
        }

        return orders.stream()
                .mapToDouble(Order::total)
                .sum();
    }

    @Override
    public OrderReport generateReport(List<Order> orders) {

        if (orders == null || orders.isEmpty()) {

            return new OrderReport(
                    new ReportTotals(
                            0,
                            0.0,
                            0,
                            null,
                            null
                    ),
                    emptyStatusCount(),
                    emptyStatusSum(),
                    Collections.emptyMap(),
                    List.of()
            );
        }

        List<Order> revenueOrders = orders.stream()
                .filter(this::isRevenueOrder)
                .toList();

        double revenue =
                revenueOrders.stream()
                        .mapToDouble(Order::total)
                        .sum();

        int revenueOrderCount =
                revenueOrders.size();

        LocalDate firstOrderDate =
                orders.stream()
                        .map(Order::createdAt)
                        .filter(createdAt -> createdAt != null)
                        .map(java.time.OffsetDateTime::toLocalDate)
                        .min(LocalDate::compareTo)
                        .orElse(null);

        LocalDate lastOrderDate =
                orders.stream()
                        .map(Order::createdAt)
                        .filter(createdAt -> createdAt != null)
                        .map(java.time.OffsetDateTime::toLocalDate)
                        .max(LocalDate::compareTo)
                        .orElse(null);

        ReportTotals totals =
                new ReportTotals(
                        orders.size(),
                        revenue,
                        revenueOrderCount,
                        firstOrderDate,
                        lastOrderDate
                );

        Map<OrderStatus, Integer> countByStatus =
                buildCountByStatus(orders);

        Map<OrderStatus, Double> sumOfTotalByStatus =
                buildSumOfTotalByStatus(orders);

        Map<LocalDate, DayRevenue> revenueByDay =
                buildRevenueByDay(revenueOrders);

        List<CustomerRevenue> revenueByCustomer =
                buildRevenueByCustomer(revenueOrders);

        return new OrderReport(
                totals,
                countByStatus,
                sumOfTotalByStatus,
                revenueByDay,
                revenueByCustomer
        );
    }

    private boolean isRevenueOrder(Order order) {

        return order.status() == OrderStatus.PAID
                || order.status() == OrderStatus.FULFILLED;
    }

    private Map<OrderStatus, Integer> buildCountByStatus(
            List<Order> orders
    ) {

        EnumMap<OrderStatus, Integer> result =
                new EnumMap<>(OrderStatus.class);

        for (OrderStatus status : OrderStatus.values()) {

            if (status != OrderStatus.UNKNOWN) {
                result.put(status, 0);
            }
        }

        for (Order order : orders) {

            OrderStatus status = order.status();

            if (status != null
                    && status != OrderStatus.UNKNOWN) {

                result.put(
                        status,
                        result.get(status) + 1
                );
            }
        }

        return result;
    }

    private Map<OrderStatus, Double> buildSumOfTotalByStatus(
            List<Order> orders
    ) {

        EnumMap<OrderStatus, Double> result =
                new EnumMap<>(OrderStatus.class);

        for (OrderStatus status : OrderStatus.values()) {

            if (status != OrderStatus.UNKNOWN) {
                result.put(status, 0.0);
            }
        }

        for (Order order : orders) {

            OrderStatus status = order.status();

            if (status != null
                    && status != OrderStatus.UNKNOWN) {

                result.put(
                        status,
                        result.get(status) + order.total()
                );
            }
        }

        return result;
    }

    private Map<LocalDate, DayRevenue> buildRevenueByDay(
            List<Order> revenueOrders
    ) {

        Map<LocalDate, DayRevenue> result =
                new TreeMap<>();

        for (Order order : revenueOrders) {

            if (order.createdAt() == null) {
                continue;
            }

            LocalDate day =
                    order.createdAt().toLocalDate();

            DayRevenue current =
                    result.get(day);

            if (current == null) {

                result.put(
                        day,
                        new DayRevenue(
                                1,
                                order.total()
                        )
                );

            } else {

                result.put(
                        day,
                        new DayRevenue(
                                current.orders() + 1,
                                current.revenue()
                                        + order.total()
                        )
                );
            }
        }

        return result;
    }

    private List<CustomerRevenue> buildRevenueByCustomer(
            List<Order> revenueOrders
    ) {

        Map<Integer, CustomerAccumulator> grouped =
                new LinkedHashMap<>();

        for (Order order : revenueOrders) {

            CustomerAccumulator accumulator =
                    grouped.computeIfAbsent(
                            order.customerId(),
                            id -> new CustomerAccumulator(
                                    order.customerId(),
                                    order.customerName()
                            )
                    );

            accumulator.orders++;
            accumulator.revenue += order.total();
        }

        List<CustomerRevenue> result =
                new ArrayList<>();

        for (CustomerAccumulator accumulator
                : grouped.values()) {

            result.add(
                    new CustomerRevenue(
                            accumulator.customerId,
                            accumulator.customerName,
                            accumulator.orders,
                            accumulator.revenue
                    )
            );
        }

        result.sort(
                (a, b) -> {
                    int revenueCompare =
                            Double.compare(
                                    b.revenue(),
                                    a.revenue()
                            );

                    if (revenueCompare != 0) {
                        return revenueCompare;
                    }

                    return Integer.compare(
                            a.customerId(),
                            b.customerId()
                    );
                }
        );

        return result;
    }

    private EnumMap<OrderStatus, Integer>
    emptyStatusCount() {

        EnumMap<OrderStatus, Integer> result =
                new EnumMap<>(OrderStatus.class);

        for (OrderStatus status : OrderStatus.values()) {

            if (status != OrderStatus.UNKNOWN) {
                result.put(status, 0);
            }
        }

        return result;
    }

    private EnumMap<OrderStatus, Double>
    emptyStatusSum() {

        EnumMap<OrderStatus, Double> result =
                new EnumMap<>(OrderStatus.class);

        for (OrderStatus status : OrderStatus.values()) {

            if (status != OrderStatus.UNKNOWN) {
                result.put(status, 0.0);
            }
        }

        return result;
    }

    private static class CustomerAccumulator {

        private final int customerId;
        private final String customerName;
        private int orders;
        private double revenue;

        private CustomerAccumulator(
                int customerId,
                String customerName
        ) {
            this.customerId = customerId;
            this.customerName = customerName;
        }
    }
}