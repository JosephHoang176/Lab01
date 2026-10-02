package org.example.interfaces;

import org.example.Entity.Shipment;

public interface ShippingClient {
    Shipment findShipmentStatusByOrderId(int orderId);
}
