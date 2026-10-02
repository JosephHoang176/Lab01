package org.example.Mapping;

import org.example.DTO.request.ProductDTO;
import org.example.Entity.Product;

public class ProductMapper {
    public static Product toEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }
        Product prod = new Product();
        prod.setId(dto.id());
        prod.setSku(dto.sku());
        prod.setName(dto.name());
        prod.setCategory(dto.category());
        prod.setUnitPrice(dto.unitPrice());
        prod.setCurrency(dto.currency());
        prod.setStock(dto.stock());
        prod.setActive(dto.isActive());
        return prod;
    }

    public static ProductDTO toDto(Product prod) {
        if (prod == null) {
            return null;
        }
        ProductDTO dto = new ProductDTO(
                prod.getId(),
                prod.getSku(),
                prod.getName(),
                prod.getCategory(),
                prod.getUnitPrice(),
                prod.getCurrency(),
                prod.getStock(),
                prod.isActive()
        );
        return dto;
    }
}
