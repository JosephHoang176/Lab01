package org.example.Mapping;

import org.example.DTO.request.OrderDTO;
import org.example.Entity.Order;

public class OrderMapper {
    public static Order toEntity(OrderDTO dto) {
        if (dto == null) {
            return null;
        }
        Order order = new Order();
        order.setId(dto.id());
        order.setCode(dto.code());
        order.setCustomerId(dto.customerId());
        order.setCustomerName(dto.customerName());
        order.setCreatedBy(dto.createdBy());
        order.setStatus(dto.status());
        order.setLines(dto.lines());
        order.setSubtotal(dto.subtotal());
        order.setDiscountPercent(dto.discountPercent());
        order.setDiscountAmount(dto.discountAmount());
        order.setTaxPercent(dto.taxPercent());
        order.setTaxAmount(dto.taxAmount());
        order.setTotal(dto.total());
        order.setCurrency(dto.currency());
        order.setCreatedAt(dto.createdAt());
        order.setUpdatedAt(dto.updatedAt());
        order.setPaidAt(dto.paidAt());
        order.setFulfilledAt(dto.fulfilledAt());
        order.setCancelledAt(dto.cancelledAt());
        return order;

    }

    public static OrderDTO toDTO(Order entity) {
        if (entity == null) {
            return null;
        }

        return new OrderDTO(
                entity.getId(),
                entity.getCode(),
                entity.getCustomerId(),
                entity.getCustomerName(),
                entity.getCreatedBy(),
                entity.getStatus(),
                entity.getLines(),
                entity.getSubtotal(),
                entity.getDiscountPercent(),
                entity.getDiscountAmount(),
                entity.getTaxPercent(),
                entity.getTaxAmount(),
                entity.getTotal(),
                entity.getCurrency(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getPaidAt(),
                entity.getFulfilledAt(),
                entity.getCancelledAt()
        );
    }
}
