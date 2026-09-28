package com.example.stockalert.controller;

import com.example.stockalert.dto.StockMovementRequest;
import com.example.stockalert.dto.StockMovementResponse;
import com.example.stockalert.service.StockMovementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse addMovement(
            @PathVariable Long productId,
            @Valid @RequestBody StockMovementRequest request
    ) {
        return stockMovementService.addMovement(productId, request);
    }

    @GetMapping
    public List<StockMovementResponse> getMovements(@PathVariable Long productId) {
        return stockMovementService.getMovements(productId);
    }

    @GetMapping("/current-stock")
    public CurrentStockResponse getCurrentStock(@PathVariable Long productId) {
        return new CurrentStockResponse(
                productId,
                stockMovementService.getCurrentStock(productId)
        );
    }

    public record CurrentStockResponse(Long productId, Integer currentStock) {
    }
}
