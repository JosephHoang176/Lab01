package org.example.enums;

public enum OrderStatus {
    DRAFT,
    PENDING_PAYMENT,
    PAID,
    FULFILLED,
    CANCELLED,
    UNKNOWN;

    public static OrderStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) return UNKNOWN;
        try {
            return OrderStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
