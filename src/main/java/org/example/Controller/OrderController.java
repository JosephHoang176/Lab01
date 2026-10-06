package org.example.Controller;

import jakarta.validation.Valid;
import org.example.DTO.request.DateRange;
import org.example.DTO.request.OrderDTO;
import org.example.Entity.Order;
import org.example.Mapping.OrderMapper;
import org.example.Service.OrderService;
import org.example.enums.OrderStatus;
import org.example.idempotency.IdemAnnotation.Idempotent;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public List<OrderDTO> findAllOrders(
            @Valid @RequestParam(required = false) String status,
            @Valid @RequestParam(required = false) LocalDate from,
            @Valid @RequestParam(required = false) LocalDate to
    ) {
        OrderStatus orderStatus = OrderStatus.fromString(status);
        DateRange dateRange = null;
        if (from != null || to != null) {
            dateRange = new DateRange(from, to);
        }
        List<Order> orders = orderService.findFilteredOrders(orderStatus, dateRange);
        List<OrderDTO> result = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            result.add(OrderMapper.toDTO(orders.get(i)));
        }
        return result;
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public OrderDTO getOrder(@Valid @PathVariable int orderId) {
        Order order = orderService.findOrderById(orderId);
        return OrderMapper.toDTO(order);
    }

    @Idempotent(expiredIn = 60)
    @PostMapping({"/orders", "/orders/create"})
    @PreAuthorize("isAuthenticated()")
    public OrderDTO createOrder(@Valid @RequestBody(required = true) OrderDTO order) {
        Order mappedOrder = OrderMapper.toEntity(order);
        Order createdOrder = orderService.createOrder(mappedOrder);
        return OrderMapper.toDTO(createdOrder);
    }

    @Idempotent(expiredIn = 60)
    @PatchMapping("/orders/status")
    @PreAuthorize("isAuthenticated()")
    public OrderDTO changeStatus(@Valid @RequestParam String status, @Valid @RequestParam int orderId) {
        OrderStatus newStatus = OrderStatus.fromString(status);
        Order updatedOrder = orderService.changeStatus(orderId, newStatus);
        return OrderMapper.toDTO(updatedOrder);
    }

    @Idempotent(expiredIn = 60)
    @PatchMapping("/orders/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public OrderDTO cancelOrder(@Valid @PathVariable int orderId) {
        Order cancelledOrder = orderService.cancelOrder(orderId);
        return OrderMapper.toDTO(cancelledOrder);
    }
}
