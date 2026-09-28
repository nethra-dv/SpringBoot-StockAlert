package com.example.stockalert.dto;

import com.example.stockalert.entity.AlertStatus;

import java.time.LocalDateTime;

public record ReorderAlertResponse(
        Long id,
        Long productId,
        String productName,
        Integer currentStock,
        Integer reorderQuantity,
        AlertStatus status,
        LocalDateTime createdAt,
        LocalDateTime fulfilledAt
) {
}
