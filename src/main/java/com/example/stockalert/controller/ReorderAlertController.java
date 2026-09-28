package com.example.stockalert.controller;

import com.example.stockalert.dto.ReorderAlertResponse;
import com.example.stockalert.service.ReorderAlertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reorder-alerts")
public class ReorderAlertController {

    private final ReorderAlertService reorderAlertService;

    public ReorderAlertController(ReorderAlertService reorderAlertService) {
        this.reorderAlertService = reorderAlertService;
    }

    @GetMapping
    public List<ReorderAlertResponse> getAll() {
        return reorderAlertService.getAll();
    }

    @GetMapping("/open")
    public List<ReorderAlertResponse> getOpen() {
        return reorderAlertService.getOpen();
    }

    @PutMapping("/{id}/fulfill")
    public ReorderAlertResponse fulfill(@PathVariable Long id) {
        return reorderAlertService.fulfill(id);
    }
}
