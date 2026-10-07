package org.example.Controller;

import org.example.DTO.response.PaymentResponse;
import org.example.Service.PaymentService;
import org.example.Service.PaymentWebhookService;
import org.example.idempotency.IdemAnnotation.Idempotent;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentWebhookService webhookService;

    public PaymentController(PaymentService paymentService, PaymentWebhookService webhookService) {
        this.paymentService = paymentService;
        this.webhookService = webhookService;
    }

    @PostMapping("/orders/{orderId}/pay")
    @Idempotent(expiredIn = 60)
    @PreAuthorize("isAuthenticated()")
    public PaymentResponse pay(@PathVariable int orderId) {
        return paymentService.pay(orderId);
    }

    @PostMapping("/webhooks/payment")
    @ResponseStatus(HttpStatus.OK)
    public PaymentWebhookService.WebhookResult paymentWebhook(
            @RequestHeader(value = "X-Payment-Signature", required = false) String paymentSignature,
            @RequestHeader(value = "X-Webhook-Signature", required = false) String webhookSignature,
            @RequestHeader(value = "X-Signature", required = false) String genericSignature,
            @RequestBody byte[] payload) {
        String signature = paymentSignature != null ? paymentSignature
                : (webhookSignature != null ? webhookSignature : genericSignature);
        return webhookService.process(payload, signature);
    }
}
