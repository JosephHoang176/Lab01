package org.example.Payment;

import org.example.Entity.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * A deterministic local provider implementation. It does not contact an external
 * service; the webhook endpoint represents the provider callback in development.
 */
@Component
public class MockPaymentProvider implements PaymentProvider {
    private final long delayMs;

    public MockPaymentProvider(@Value("${payment.mock.delay-ms:0}") long delayMs) {
        this.delayMs = Math.max(0, delayMs);
    }

    @Override
    public PaymentResult createPayment(Order order) {
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PaymentProviderException("Payment provider interrupted", e);
            }
        }
        return new PaymentResult("mock_" + UUID.randomUUID());
    }
}
