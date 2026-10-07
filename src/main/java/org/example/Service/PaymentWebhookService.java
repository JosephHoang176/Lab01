package org.example.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.DTO.request.PaymentWebhookRequest;
import org.example.Entity.Order;
import org.example.Entity.PaymentWebhookEvent;
import org.example.Repository.OrderJPARepository;
import org.example.Repository.PaymentWebhookEventRepository;
import org.example.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Locale;

@Service
public class PaymentWebhookService {
    private final ObjectMapper objectMapper;
    private final PaymentWebhookEventRepository eventRepository;
    private final OrderJPARepository orderRepository;
    private final String webhookSecret;

    public PaymentWebhookService(
            ObjectMapper objectMapper,
            PaymentWebhookEventRepository eventRepository,
            OrderJPARepository orderRepository,
            @Value("${payment.webhook.secret:}") String webhookSecret) {
        this.objectMapper = objectMapper;
        this.eventRepository = eventRepository;
        this.orderRepository = orderRepository;
        this.webhookSecret = webhookSecret == null ? "" : webhookSecret;
    }

    public void verifySignature(byte[] payload, String signature) {
        if (webhookSecret.isBlank() || signature == null || signature.isBlank()) {
            throw new IllegalArgumentException("Invalid webhook signature");
        }
        String supplied = signature.trim();
        if (supplied.regionMatches(true, 0, "sha256=", 0, 7)) {
            supplied = supplied.substring(7);
        }

        byte[] expected = hmac(payload);
        byte[] suppliedBytes = decodeSignature(supplied);
        if (suppliedBytes == null || !MessageDigest.isEqual(expected, suppliedBytes)) {
            throw new IllegalArgumentException("Invalid webhook signature");
        }
    }

    @Transactional
    public WebhookResult process(byte[] payload, String signature) {
        verifySignature(payload, signature);
        final PaymentWebhookRequest webhook;
        try {
            webhook = objectMapper.readValue(payload, PaymentWebhookRequest.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid payment webhook payload", e);
        }
        if (webhook.eventId() == null || webhook.eventId().isBlank() || webhook.orderId() <= 0) {
            throw new IllegalArgumentException("Webhook eventId and orderId are required");
        }
        if (!isSuccessful(webhook.status())) {
            throw new IllegalArgumentException("Unsupported payment event status");
        }
        if (eventRepository.existsByEventId(webhook.eventId())) {
            return new WebhookResult(webhook.eventId(), webhook.orderId(), false);
        }
        Order order = orderRepository.findById(webhook.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order khong ton tai"));
        validatePaymentDetails(webhook, order);

        try {
            eventRepository.saveAndFlush(new PaymentWebhookEvent(
                    webhook.eventId().trim(), webhook.orderId(), OffsetDateTime.now()));
        } catch (DataIntegrityViolationException duplicate) {
            return new WebhookResult(webhook.eventId(), webhook.orderId(), false);
        }

        int changed = orderRepository.markPaid(webhook.orderId(), OffsetDateTime.now());
        if (changed == 0 && order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Order is not awaiting payment");
        }
        return new WebhookResult(webhook.eventId(), webhook.orderId(), changed == 1);
    }

    private void validatePaymentDetails(PaymentWebhookRequest webhook, Order order) {
        if (webhook.paymentId() == null || webhook.paymentId().isBlank()
                || order.getPaymentReference() == null
                || !webhook.paymentId().trim().equals(order.getPaymentReference())) {
            throw new IllegalArgumentException("Webhook payment reference does not match order");
        }
        if (webhook.amount() != null
                && webhook.amount().compareTo(BigDecimal.valueOf(order.getTotal())) != 0) {
            throw new IllegalArgumentException("Webhook amount does not match order total");
        }
        if (webhook.currency() != null && order.getCurrency() != null
                && !webhook.currency().trim().equalsIgnoreCase(order.getCurrency().trim())) {
            throw new IllegalArgumentException("Webhook currency does not match order currency");
        }
    }

    private boolean isSuccessful(String status) {
        if (status == null) return true;
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("SUCCEEDED")
                || normalized.equals("SUCCESS")
                || normalized.equals("PAID")
                || normalized.equals("PAYMENT_SUCCEEDED")
                || normalized.equals("COMPLETED");
    }

    private byte[] hmac(byte[] payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to verify webhook signature", e);
        }
    }

    private byte[] decodeSignature(String signature) {
        try {
            if (signature.matches("(?i)[0-9a-f]{64}")) {
                byte[] result = new byte[32];
                for (int i = 0; i < result.length; i++) {
                    result[i] = (byte) Integer.parseInt(signature.substring(i * 2, i * 2 + 2), 16);
                }
                return result;
            }
            return Base64.getDecoder().decode(signature);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public record WebhookResult(String eventId, int orderId, boolean transitioned) {
    }
}
