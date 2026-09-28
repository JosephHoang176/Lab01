package org.example.interfaces;

import org.example.DTO.ShipmentDTO;
import org.example.Entity.Shipment;

public interface ShippingClient {
    Shipment findShipmentStatusByOrderId(int orderId);
}
