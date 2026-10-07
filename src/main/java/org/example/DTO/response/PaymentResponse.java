package org.example.DTO.response;

public record PaymentResponse(int orderId, String paymentId, String status) {
}
