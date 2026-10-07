package org.example.Payment;

import org.example.Entity.Order;

public interface PaymentProvider {
    PaymentResult createPayment(Order order);
}
