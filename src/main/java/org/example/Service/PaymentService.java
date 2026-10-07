package org.example.Service;

import org.example.DTO.response.PaymentResponse;
import org.example.Entity.Order;
import org.example.Payment.PaymentProvider;
import org.example.Payment.PaymentProviderException;
import org.example.Payment.PaymentResult;
import org.example.Repository.OrderJPARepository;
import org.example.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.*;

@Service
public class PaymentService {
    private final OrderJPARepository orderRepository;
    private final PaymentProvider paymentProvider;
    private final long timeoutMs;
    private final int maxRetries;
    private final long initialBackoffMs;
    private final long maxBackoffMs;

    public PaymentService(
            OrderJPARepository orderRepository,
            PaymentProvider paymentProvider,
            @Value("${payment.provider.timeout-ms:2000}") long timeoutMs,
            @Value("${payment.provider.max-retries:3}") int maxRetries,
            @Value("${payment.provider.initial-backoff-ms:100}") long initialBackoffMs,
            @Value("${payment.provider.max-backoff-ms:2000}") long maxBackoffMs) {
        this.orderRepository = orderRepository;
        this.paymentProvider = paymentProvider;
        this.timeoutMs = Math.max(1, timeoutMs);
        this.maxRetries = Math.max(0, maxRetries);
        this.initialBackoffMs = Math.max(0, initialBackoffMs);
        this.maxBackoffMs = Math.max(this.initialBackoffMs, maxBackoffMs);
    }

    @Transactional
    public PaymentResponse pay(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order khong ton tai"));

        if (order.getStatus() == OrderStatus.PAID) {
            return new PaymentResponse(orderId, order.getPaymentReference(), "PAID");
        }
        if (order.getStatus() == OrderStatus.PENDING_PAYMENT
                && order.getPaymentReference() != null) {
            return new PaymentResponse(orderId, order.getPaymentReference(), "PENDING_PAYMENT");
        }
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Order must be in DRAFT before payment");
        }

        PaymentResult result = createPaymentWithRetry(order);
        order.setPaymentReference(result.paymentId());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        orderRepository.save(order);
        return new PaymentResponse(orderId, result.paymentId(), "PENDING_PAYMENT");
    }

    private PaymentResult createPaymentWithRetry(Order order) {
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
            try {
                Future<PaymentResult> future = executor.submit(() -> paymentProvider.createPayment(order));
                try {
                    PaymentResult result = future.get(timeoutMs, TimeUnit.MILLISECONDS);
                    if (result == null || result.paymentId() == null || result.paymentId().isBlank()) {
                        if (attempt == maxRetries) {
                            throw new PaymentProviderException("Payment provider returned an invalid response");
                        }
                    } else {
                        return result;
                    }
                } catch (TimeoutException e) {
                    future.cancel(true);
                    if (attempt == maxRetries) {
                        throw new PaymentProviderException("Payment provider timed out after retries", e);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new PaymentProviderException("Payment request interrupted", e);
                } catch (ExecutionException e) {
                    if (attempt == maxRetries) {
                        Throwable cause = e.getCause() == null ? e : e.getCause();
                        throw new PaymentProviderException("Payment provider failed after retries", cause);
                    }
                }
            } finally {
                // Do not wait for a provider that ignored interruption: the caller's
                // timeout must remain a hard upper bound for each attempt.
                executor.shutdownNow();
            }
            backoff(attempt);
        }
        throw new PaymentProviderException("Payment provider failed");
    }

    private void backoff(int attempt) {
        if (initialBackoffMs == 0) return;
        long multiplier = 1L << Math.min(attempt, 30);
        long delay = initialBackoffMs > maxBackoffMs / multiplier
                ? maxBackoffMs
                : initialBackoffMs * multiplier;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentProviderException("Payment retry interrupted", e);
        }
    }
}
