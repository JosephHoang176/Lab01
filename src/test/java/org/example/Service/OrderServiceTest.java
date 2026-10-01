package org.example.Service;

import org.example.Client.PricingClient;
import org.example.Entity.Order;
import org.example.interfaces.IOrderRepository;
import org.example.interfaces.ShippingClient;
import org.example.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private IOrderRepository jsonRepository;

    @Mock
    private IOrderRepository jpaRepository;

    @Mock
    private ShippingClient shippingClient;

    @Mock
    private PricingClient pricingClient;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                jsonRepository,
                shippingClient,
                jpaRepository,
                pricingClient
        );
    }

    @Test
    void statusTransitionUpdatesRepository() {
        Order order = new Order();
        order.setId(1);
        order.setStatus(OrderStatus.PAID);
        when(jpaRepository.getOrderById(1)).thenReturn(order);
        when(jpaRepository.update(order)).thenReturn(order);

        orderService.changeStatus(1, OrderStatus.FULFILLED);

        verify(jpaRepository).update(order);
    }

    @Test
    void fulfilledOrderCannotBeCancelled() {
        Order order = new Order();
        order.setId(1);
        order.setStatus(OrderStatus.FULFILLED);
        when(jpaRepository.getOrderById(1)).thenReturn(order);

        assertThrows(IllegalStateException.class,
                () -> orderService.cancelOrder(1));
    }
}
