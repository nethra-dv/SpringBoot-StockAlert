package com.example.stockalert.controller;

import com.example.stockalert.dto.ProductRequest;
import com.example.stockalert.dto.ProductResponse;
import com.example.stockalert.service.AnalyticsService;
import com.example.stockalert.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final AnalyticsService analyticsService;

    public ProductController(
            ProductService productService,
            AnalyticsService analyticsService
    ) {
        this.productService = productService;
        this.analyticsService = analyticsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    @GetMapping("/{id}/stock")
    public StockResponse getCurrentStock(@PathVariable Long id) {
        return new StockResponse(id, productService.getById(id).currentStock());
    }

    @GetMapping("/fast-moving")
    public List<AnalyticsService.FastMovingProduct> getFastMoving(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return analyticsService.getFastMovingProducts(from, to);
    }

    public record StockResponse(Long productId, Integer currentStock) {
    }
}
