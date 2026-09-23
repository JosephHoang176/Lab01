package org.example.model;

public record LineItem(int lineNo,
                       int productId,
                       String sku,
                       String productName,
                       int quantity,
                       double unitPrice,
                       double lineTotal) {}
