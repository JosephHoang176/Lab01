package org.example.enums;

public enum ShippingStatus {
    IN_TRANSIT,
    DELIVERED,
    PREPARING,
    UNKNOWN;

    public static ShippingStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            return UNKNOWN;
        }
        try {
            return ShippingStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
