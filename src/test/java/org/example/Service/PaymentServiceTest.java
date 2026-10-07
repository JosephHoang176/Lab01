package org.example.Service;

import org.example.Entity.Order;
import org.example.Payment.PaymentProvider;
import org.example.Payment.PaymentProviderException;
import org.example.Payment.PaymentResult;
import org.example.Repository.OrderJPARepository;
import org.example.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock OrderJPARepository orderRepository;
    @Mock PaymentProvider paymentProvider;

    @Test
    void retriesProviderFailureAndLeavesOrderPending() {
        Order order = new Order();
        order.setId(42);
        order.setStatus(OrderStatus.DRAFT);
        when(orderRepository.findById(42)).thenReturn(Optional.of(order));
        when(paymentProvider.createPayment(order))
                .thenThrow(new PaymentProviderException("temporary"))
                .thenReturn(new PaymentResult("pay_42"));

        PaymentService service = new PaymentService(
                orderRepository, paymentProvider, 1000, 2, 0, 0);

        var response = service.pay(42);

        assertEquals("pay_42", response.paymentId());
        assertEquals("PENDING_PAYMENT", response.status());
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
        verify(paymentProvider, times(2)).createPayment(order);
        verify(orderRepository).save(order);
    }
}
