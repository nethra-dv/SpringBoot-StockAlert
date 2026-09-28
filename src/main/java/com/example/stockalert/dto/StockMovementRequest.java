package com.example.stockalert.dto;

import com.example.stockalert.entity.MovementReason;
import com.example.stockalert.entity.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockMovementRequest(
        @NotNull(message = "Movement type is required")
        MovementType movementType,

        @NotNull(message = "Movement reason is required")
        MovementReason reason,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity
) {
}
