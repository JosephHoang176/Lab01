package org.example.interfaces;

import org.example.DTO.Shipment;

public interface ShippingClient {
    Shipment findShipmentStatusByOrderId(int orderId);
}
