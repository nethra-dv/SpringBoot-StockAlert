package com.example.stockalert.dto;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        Integer reorderThreshold,
        Integer reorderQuantity,
        Integer currentStock
) {
}
