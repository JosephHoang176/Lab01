package org.example.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Entity.Order;
import org.example.Repository.OrderJPARepository;
import org.example.Repository.PaymentWebhookEventRepository;
import org.example.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PaymentWebhookServiceTest {
    @Test
    void verifiesSignatureAndProcessesReplayOnlyOnce() throws Exception {
        PaymentWebhookEventRepository events = mock(PaymentWebhookEventRepository.class);
        OrderJPARepository orders = mock(OrderJPARepository.class);
        Order order = new Order();
        order.setId(42);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setPaymentReference("pay_42");
        when(orders.findById(42)).thenReturn(Optional.of(order));
        when(events.existsByEventId("evt_1")).thenReturn(false, true);
        when(orders.markPaid(eq(42), any())).thenReturn(1);

        String secret = "test-webhook-secret";
        PaymentWebhookService service = new PaymentWebhookService(
                new ObjectMapper(), events, orders, secret);
        byte[] payload = "{\"eventId\":\"evt_1\",\"orderId\":42,\"paymentId\":\"pay_42\",\"status\":\"succeeded\"}"
                .getBytes(StandardCharsets.UTF_8);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = HexFormat.of().formatHex(mac.doFinal(payload));

        assertTrue(service.process(payload, signature).transitioned());
        assertFalse(service.process(payload, signature).transitioned());
        verify(orders, times(1)).markPaid(eq(42), any());
    }
}
