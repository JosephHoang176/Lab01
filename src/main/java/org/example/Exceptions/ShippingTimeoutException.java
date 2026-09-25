package org.example.Exceptions;

public class ShippingTimeoutException extends RuntimeException {
    public ShippingTimeoutException(Throwable cause) {
        super(cause);
    }
}
