package com.example.stockalert.service;

import com.example.stockalert.entity.Product;
import com.example.stockalert.repository.ProductRepository;
import com.example.stockalert.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public AnalyticsService(
            StockMovementRepository stockMovementRepository,
            ProductRepository productRepository
    ) {
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<FastMovingProduct> getFastMovingProducts(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("'to' date cannot be before 'from' date.");
        }

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        List<Object[]> rows =
                stockMovementRepository.findSalesVolumeByProduct(start, end);

        List<FastMovingProduct> result = new ArrayList<>();

        for (Object[] row : rows) {
            Long productId = ((Number) row[0]).longValue();
            Integer soldQuantity = ((Number) row[1]).intValue();

            Product product = productRepository.findById(productId).orElse(null);

            if (product != null) {
                result.add(new FastMovingProduct(
                        product.getId(),
                        product.getName(),
                        product.getSku(),
                        soldQuantity
                ));
            }
        }

        return result;
    }

    public record FastMovingProduct(
            Long productId,
            String productName,
            String sku,
            Integer salesVolume
    ) {
    }
}
