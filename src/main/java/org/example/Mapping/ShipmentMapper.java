package org.example.Mapping;

import org.example.DTO.ShipmentDTO;
import org.example.Entity.Shipment;

public class ShipmentMapper {
    public static Shipment toEntity(ShipmentDTO dto) {

        if (dto == null) {
            return null;
        }

        Shipment shipment = new Shipment();

        shipment.setId(dto.id());
        shipment.setOrderId(dto.orderId());
        shipment.setOrderCode(dto.orderCode());
        shipment.setCarrier(dto.carrier());
        shipment.setTrackingNumber(dto.trackingNumber());
        shipment.setStatus(dto.status());
        shipment.setEstimatedDelivery(dto.estimatedDelivery());
        shipment.setLastUpdated(dto.lastUpdated());

        return shipment;
    }

    public static ShipmentDTO toDTO(Shipment entity) {

        if (entity == null) {
            return null;
        }

        return new ShipmentDTO(
                entity.getId(),
                entity.getOrderId(),
                entity.getOrderCode(),
                entity.getCarrier(),
                entity.getTrackingNumber(),
                entity.getStatus(),
                entity.getEstimatedDelivery(),
                entity.getLastUpdated()
        );
    }
}
