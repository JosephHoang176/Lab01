package org.example.Service;

import org.example.DTO.request.DateRange;
import org.example.Entity.Order;
import org.example.Entity.Shipment;
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

    public OrderService(IOrderCalculator calculator, IOrderRepository repository, ShippingClient shippingClient) {
        this.calculator = calculator;
        this.repository = repository;
        this.shippingClient = shippingClient;
    }

    private boolean matchesStatus(Order order, OrderStatus status) {
        if (status == null) {
            return true;
        }
        return order.getStatus() == status;
    }

    private boolean matchesDate(Order order, DateRange dateRange) {
        if (dateRange == null) {
            return true;
        }
        if (order.getCreatedAt() == null) {
            return false;
        }
        return dateRange.includes(
                order.getCreatedAt().toLocalDate()
        );
    }

    public List<Order> findFilteredOrders(OrderStatus status, DateRange dateRange) {
        List<Order> orders = repository.getAllOrders();
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }
        List<Order> filteredOrders = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (matchesStatus(order, status)
                    && matchesDate(order, dateRange)) {
                filteredOrders.add(order);
            }
        }
        return filteredOrders;
    }

    public double getRevenue(OrderStatus status, DateRange dateRange) {
        List<Order> filteredOrders = findFilteredOrders(status, dateRange);
        return calculator.calculateRevenue(filteredOrders);
    }


    //Phần giải quyết Lab 3
    public void getShipments(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < orders.size(); i++) {
                Order order = orders.get(i);
                Future<String> future = executor.submit(new Callable<String>() {
                            @Override
                            public String call() {
                                try {
                                    Shipment shipment = shippingClient.findShipmentStatusByOrderId(order.getId());
                                    if (shipment == null) {
                                        return order.getCode()
                                                + " -> NO_SHIPMENT";
                                    }
                                    return order.getCode()
                                            + " -> "
                                            + shipment.getStatus();
                                } catch (ShippingTimeoutException e) {
                                    return order.getCode()
                                            + " -> TIMEOUT";
                                }
                            }
                        }
                );
                futures.add(future);
            }
            for (int i = 0; i < futures.size(); i++) {
                System.out.println(
                        futures.get(i).get()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    //Phần giải quyết Lab 4
    public List<Order> findOrders(OrderStatus status, DateRange dateRange) {
        return findFilteredOrders(status, dateRange);
    }

    public Order findOrderById(int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Id khong hop le"
            );
        }
        Order order = repository.getOrderById(orderId);
        if (order == null) {throw new IllegalArgumentException("Order khong ton tai");}
        return order;
    }

    public Order createOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "Order khong hop le"
            );
        }
        if (order.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Id khong hop le"
            );
        }
        if (order.getStatus() == null || order.getStatus() == OrderStatus.UNKNOWN) {order.setStatus(OrderStatus.DRAFT);}
        return repository.save(order);
    }

    public Order changeStatus(int orderId, OrderStatus newStatus) {
        if (newStatus == null || newStatus == OrderStatus.UNKNOWN) {
            throw new IllegalArgumentException(
                    "Status khong hop le"
            );
        }
        Order order = findOrderById(orderId);
        OrderStatus currentStatus = order.getStatus();
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new IllegalStateException("Khong the chuyen status tu " + currentStatus + " sang " + newStatus);
        }
        order.setStatus(newStatus);
        return repository.update(order);
    }

    public Order cancelOrder(int orderId) {
        Order order = findOrderById(orderId);
        if (!isValidStatusTransition(order.getStatus(), OrderStatus.CANCELLED)) {
            throw new IllegalStateException("Khong the cancel order voi status " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        return repository.update(order);
    }

    private boolean isValidStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {

        if (currentStatus == null
                || newStatus == null) {
            return false;
        }
        if (currentStatus == OrderStatus.UNKNOWN
                || newStatus == OrderStatus.UNKNOWN) {
            return false;
        }
        if (currentStatus == newStatus) {
            return false;
        }
        switch (currentStatus) {
            case DRAFT:
                return newStatus == OrderStatus.PENDING_PAYMENT || newStatus == OrderStatus.CANCELLED;

            case PENDING_PAYMENT:
                return newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED;

            case PAID:
                return newStatus == OrderStatus.FULFILLED || newStatus == OrderStatus.CANCELLED;

            case FULFILLED:
                return false;

            case CANCELLED:
                return false;

            case UNKNOWN:
                return false;

            default:
                return false;
        }
    }
}