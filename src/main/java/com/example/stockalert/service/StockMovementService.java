package com.example.stockalert.service;

import com.example.stockalert.dto.StockMovementRequest;
import com.example.stockalert.dto.StockMovementResponse;
import com.example.stockalert.entity.*;
import com.example.stockalert.exception.BusinessRuleException;
import com.example.stockalert.repository.ReorderAlertRepository;
import com.example.stockalert.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ReorderAlertRepository reorderAlertRepository;
    private final ProductService productService;

    public StockMovementService(
            StockMovementRepository stockMovementRepository,
            ReorderAlertRepository reorderAlertRepository,
            ProductService productService
    ) {
        this.stockMovementRepository = stockMovementRepository;
        this.reorderAlertRepository = reorderAlertRepository;
        this.productService = productService;
    }

    public StockMovementResponse addMovement(Long productId, StockMovementRequest request) {
        Product product = productService.findProduct(productId);

        validateReasonAndType(request);

        int currentStock = stockMovementRepository.calculateCurrentStock(productId);

        if (request.movementType() == MovementType.OUT
                && request.quantity() > currentStock) {
            throw new BusinessRuleException(
                    "Insufficient stock. Current stock is " + currentStock
                            + " but requested OUT quantity is " + request.quantity() + "."
            );
        }

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(request.movementType());
        movement.setReason(request.reason());
        movement.setQuantity(request.quantity());

        movement = stockMovementRepository.save(movement);

        int newStock = stockMovementRepository.calculateCurrentStock(productId);

        // A new IN movement fulfils the currently open reorder request.
        if (request.movementType() == MovementType.IN) {
            reorderAlertRepository.findByProductIdAndStatus(productId, AlertStatus.OPEN)
                    .ifPresent(alert -> {
                        alert.setStatus(AlertStatus.FULFILLED);
                        alert.setFulfilledAt(LocalDateTime.now());
                        reorderAlertRepository.save(alert);
                    });
        }

        // If stock is at/below the threshold, create an alert only when
        // there is no OPEN alert already.
        if (newStock <= product.getReorderThreshold()) {
            boolean hasOpenAlert = reorderAlertRepository
                    .findByProductIdAndStatus(productId, AlertStatus.OPEN)
                    .isPresent();

            if (!hasOpenAlert) {
                ReorderAlert alert = new ReorderAlert();
                alert.setProduct(product);
                alert.setCurrentStock(newStock);
                alert.setReorderQuantity(product.getReorderQuantity());
                alert.setStatus(AlertStatus.OPEN);
                reorderAlertRepository.save(alert);
            }
        }

        return toResponse(movement);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovements(Long productId) {
        productService.findProduct(productId);

        return stockMovementRepository
                .findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public int getCurrentStock(Long productId) {
        productService.findProduct(productId);
        return stockMovementRepository.calculateCurrentStock(productId);
    }

    private void validateReasonAndType(StockMovementRequest request) {
        if (request.movementType() == MovementType.IN
                && request.reason() == MovementReason.SALE) {
            throw new BusinessRuleException("SALE must be an OUT movement.");
        }

        if (request.movementType() == MovementType.IN
                && request.reason() == MovementReason.DAMAGE) {
            throw new BusinessRuleException("DAMAGE must be an OUT movement.");
        }

        if (request.movementType() == MovementType.OUT
                && request.reason() == MovementReason.PURCHASE) {
            throw new BusinessRuleException("PURCHASE must be an IN movement.");
        }

        if (request.movementType() == MovementType.OUT
                && request.reason() == MovementReason.RETURN) {
            throw new BusinessRuleException("RETURN must be an IN movement.");
        }
    }

    private StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getMovementType(),
                movement.getReason(),
                movement.getQuantity(),
                movement.getCreatedAt()
        );
    }
}
