package com.example.stockalert.service;

import com.example.stockalert.dto.ReorderAlertResponse;
import com.example.stockalert.entity.AlertStatus;
import com.example.stockalert.entity.ReorderAlert;
import com.example.stockalert.exception.BusinessRuleException;
import com.example.stockalert.exception.ResourceNotFoundException;
import com.example.stockalert.repository.ReorderAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReorderAlertService {

    private final ReorderAlertRepository reorderAlertRepository;

    public ReorderAlertService(ReorderAlertRepository reorderAlertRepository) {
        this.reorderAlertRepository = reorderAlertRepository;
    }

    @Transactional(readOnly = true)
    public List<ReorderAlertResponse> getAll() {
        return reorderAlertRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReorderAlertResponse> getOpen() {
        return reorderAlertRepository.findByStatusOrderByCreatedAtDesc(AlertStatus.OPEN)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ReorderAlertResponse fulfill(Long alertId) {
        ReorderAlert alert = reorderAlertRepository.findById(alertId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reorder alert not found with id: " + alertId));

        if (alert.getStatus() == AlertStatus.FULFILLED) {
            throw new BusinessRuleException("Reorder alert " + alertId + " is already fulfilled.");
        }

        alert.setStatus(AlertStatus.FULFILLED);
        alert.setFulfilledAt(LocalDateTime.now());

        return toResponse(reorderAlertRepository.save(alert));
    }

    private ReorderAlertResponse toResponse(ReorderAlert alert) {
        return new ReorderAlertResponse(
                alert.getId(),
                alert.getProduct().getId(),
                alert.getProduct().getName(),
                alert.getCurrentStock(),
                alert.getReorderQuantity(),
                alert.getStatus(),
                alert.getCreatedAt(),
                alert.getFulfilledAt()
        );
    }
}
