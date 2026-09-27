package org.example.Service;

import org.example.DTO.Order;
import org.example.DTO.DateRange;
import org.example.DTO.Shipment;
import org.example.Exceptions.ShippingTimeoutException;
import org.example.enums.OrderStatus;
import org.example.interfaces.IOrderCalculator;
import org.example.interfaces.IOrderRepository;
import org.example.interfaces.ShippingClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class OrderService {

    private final IOrderCalculator calculator;
    private final IOrderRepository repository;
    private final ShippingClient shippingClient;
    public OrderService(
            IOrderCalculator calculator,
            IOrderRepository repository,
            ShippingClient shippingClient
    ) {
        this.calculator = calculator;
        this.repository = repository;
        this.shippingClient = shippingClient;
    }

    private boolean matchesStatus(Order order, OrderStatus status) {
        if (status == null) {
            return true;
        }
        return order.status() == status;
    }

    private boolean matchesDate(Order order, DateRange dateRange) {
        if (dateRange == null) {
            return true;
        }
        if (order.createdAt() == null) {
            return false;
        }
        return dateRange.includes(order.createdAt().toLocalDate()
        );
    }

    public List<Order> findFilteredOrders(OrderStatus status, DateRange dateRange) {
        List<Order> orders = repository.getAllOrders();
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }
        return orders.stream()
                .filter(order -> matchesStatus(order, status))
                .filter(order -> matchesDate(order, dateRange))
                .toList();
    }

    public double getRevenue(
            OrderStatus status,
            DateRange dateRange
    ) {
        List<Order> filteredOrders = findFilteredOrders(status, dateRange);
        return calculator.calculateRevenue(filteredOrders);
    }

    public void getShipments(List<Order> orders) {

        try (ExecutorService executor =
                     Executors.newVirtualThreadPerTaskExecutor()) {

            List<Future<String>> futures = new ArrayList<>();

            for (int i = 0; i < orders.size(); i++) {

                Order order = orders.get(i);

                Future<String> future = executor.submit(
                        new Callable<String>() {
                            @Override
                            public String call() {

                                try {
                                    Shipment shipment =
                                            shippingClient.findShipmentStatusByOrderId(
                                                    order.id()
                                            );

                                    if (shipment == null) {
                                        return order.code() + " -> NO_SHIPMENT";
                                    }

                                    return order.code()
                                            + " -> "
                                            + shipment.status();

                                } catch (ShippingTimeoutException e) {
                                    return order.code() + " -> TIMEOUT";
                                }
                            }
                        }
                );

                futures.add(future);
            }

            for (int i = 0; i < futures.size(); i++) {
                System.out.println(futures.get(i).get());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}