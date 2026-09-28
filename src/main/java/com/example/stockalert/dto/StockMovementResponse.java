package com.example.stockalert.dto;

import com.example.stockalert.entity.MovementReason;
import com.example.stockalert.entity.MovementType;

import java.time.LocalDateTime;

public record StockMovementResponse(
        Long id,
        Long productId,
        MovementType movementType,
        MovementReason reason,
        Integer quantity,
        LocalDateTime createdAt
) {
}
