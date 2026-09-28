package com.example.stockalert.repository;

import com.example.stockalert.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    @Query("""
           SELECT COALESCE(SUM(
               CASE WHEN s.movementType = com.example.stockalert.entity.MovementType.IN
                    THEN s.quantity
                    ELSE -s.quantity
               END
           ), 0)
           FROM StockMovement s
           WHERE s.product.id = :productId
           """)
    Integer calculateCurrentStock(Long productId);

    @Query("""
           SELECT s.product.id, COALESCE(SUM(s.quantity), 0)
           FROM StockMovement s
           WHERE s.movementType = com.example.stockalert.entity.MovementType.OUT
             AND s.reason = com.example.stockalert.entity.MovementReason.SALE
             AND s.createdAt >= :from
             AND s.createdAt <= :to
           GROUP BY s.product.id
           ORDER BY SUM(s.quantity) DESC
           """)
    List<Object[]> findSalesVolumeByProduct(LocalDateTime from, LocalDateTime to);
}
