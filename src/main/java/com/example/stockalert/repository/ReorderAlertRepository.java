package com.example.stockalert.repository;

import com.example.stockalert.entity.AlertStatus;
import com.example.stockalert.entity.ReorderAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReorderAlertRepository extends JpaRepository<ReorderAlert, Long> {

    Optional<ReorderAlert> findByProductIdAndStatus(Long productId, AlertStatus status);

    List<ReorderAlert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    List<ReorderAlert> findAllByOrderByCreatedAtDesc();
}
