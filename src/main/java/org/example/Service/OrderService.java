package org.example.Service;

import org.example.DTO.OrderDTO;
import org.example.DTO.PricingItemRequest;
import org.example.DTO.PricingRequest;
import org.example.DTO.PricingResponse;
import org.example.DTO.DateRange;
import org.example.Client.PricingClient;
import org.example.DTO.ShipmentDTO;
import org.example.Entity.Order;
import org.example.Entity.Shipment;
import org.example.Exceptions.ShippingTimeoutException;
import org.example.enums.OrderStatus;
import org.example.interfaces.IOrderCalculator;
import org.example.interfaces.IOrderRepository;
import org.example.interfaces.ShippingClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import org.slf4j.MDC;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class OrderService {

    @Qualifier("jsonReader")
    private final IOrderRepository repository;

    private final ShippingClient shippingClient;

    @Qualifier("orderJPARepo")
    private final IOrderRepository jpaRepository;

    private final PricingClient pricingClient;
    private final ExecutorService pricingExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public OrderService(
            @Qualifier("jsonReader") IOrderRepository repository,
            ShippingClient shippingClient,
            @Qualifier("orderJPARepo") IOrderRepository jpaRepository,
            PricingClient pricingClient
    ) {
        this.repository = repository; //repository đến orders.json (task của Lab 1 và Lab 2).
        this.shippingClient = shippingClient; //giả lập gọi api để lấy shippingStatus ở Lab 3
        this.jpaRepository = jpaRepository; // Repo sử dụng thư viện JPA đến Database SQL Server.
        this.pricingClient = pricingClient;
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
        List<Order> orders = jpaRepository.getAllOrders();
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }
        List<Order> filteredOrders = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (matchesStatus(order, status) && matchesDate(order, dateRange)) {
                filteredOrders.add(order);
            }
        }
        return filteredOrders;
    }

    public double getRevenue(OrderStatus status, DateRange dateRange) {
        List<Order> filteredOrders = findFilteredOrders(status, dateRange);
        double revenue = 0;
        for (Order order : filteredOrders) {
            revenue += order.getTotal();
        }
        return revenue;
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
        Order order = jpaRepository.getOrderById(orderId);
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
        OffsetDateTime now = OffsetDateTime.now();
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(now);
        }
        if (order.getUpdatedAt() == null) {
            order.setUpdatedAt(now);
        }
        PricingRequest pricingRequest = toPricingRequest(order);
        order.setPricingStatus("PENDING");
        Order createdOrder = jpaRepository.save(order);
        schedulePricing(createdOrder.getId(), pricingRequest);
        return createdOrder;
    }

    private void schedulePricing(int orderId, PricingRequest pricingRequest) {
        String correlationId = MDC.get("correlationId");
        CompletableFuture.runAsync(
                () -> {
                    if (correlationId != null) {
                        MDC.put("correlationId", correlationId);
                    }
                    try {
                        calculateAndPersistPricing(orderId, pricingRequest);
                    } finally {
                        MDC.remove("correlationId");
                    }
                },
                pricingExecutor
        );
    }

    private void calculateAndPersistPricing(int orderId, PricingRequest pricingRequest) {
        Order order = jpaRepository.getOrderById(orderId);
        if (order == null) {
            return;
        }

        try {
            PricingResponse pricing = calculateWithRetry(pricingRequest);
            order.setSubtotal(pricing.subtotal());
            order.setDiscountAmount(pricing.discountAmount());
            order.setTaxAmount(pricing.taxAmount());
            order.setTotal(pricing.total());
            order.setPricingStatus("COMPLETED");
        } catch (RuntimeException ex) {
            order.setPricingStatus("FAILED");
        }
        jpaRepository.update(order);
    }

    private PricingRequest toPricingRequest(Order order) {
        List<PricingItemRequest> items = new ArrayList<>();
        for (var line : order.getLines()) {
            items.add(new PricingItemRequest(line.getQuantity(), line.getUnitPrice()));
        }
        return new PricingRequest(items, order.getDiscountPercent(), order.getTaxPercent());
    }

    private PricingResponse calculateWithRetry(PricingRequest pricingRequest) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return CompletableFuture
                        .supplyAsync(() -> pricingClient.calculate(pricingRequest), pricingExecutor)
                        .orTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
                        .join();
            } catch (CompletionException ex) {
                lastFailure = new IllegalStateException(
                        "Pricing service unavailable on attempt " + attempt, ex);
            }
        }
        throw lastFailure;
    }

    @PreDestroy
    void shutdownPricingExecutor() {
        pricingExecutor.close();
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
        return jpaRepository.update(order);
    }

    public Order cancelOrder(int orderId) {
        Order order = findOrderById(orderId);
        if (!isValidStatusTransition(order.getStatus(), OrderStatus.CANCELLED)) {
            throw new IllegalStateException("Khong the cancel order voi status " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        return jpaRepository.update(order);
    }

    private boolean isValidStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {

        if (currentStatus == null || newStatus == null) {
            return false;
        }
        if (currentStatus == OrderStatus.UNKNOWN || newStatus == OrderStatus.UNKNOWN) {
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