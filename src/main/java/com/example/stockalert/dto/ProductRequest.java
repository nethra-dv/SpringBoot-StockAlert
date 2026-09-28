package com.example.stockalert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        String name,

        @NotBlank(message = "SKU is required")
        String sku,

        @NotNull(message = "Reorder threshold is required")
        @PositiveOrZero(message = "Reorder threshold cannot be negative")
        Integer reorderThreshold,

        @NotNull(message = "Reorder quantity is required")
        @PositiveOrZero(message = "Reorder quantity cannot be negative")
        Integer reorderQuantity
) {
}
