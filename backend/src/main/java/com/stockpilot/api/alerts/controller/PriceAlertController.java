package com.stockpilot.api.alerts.controller;

import com.stockpilot.api.alerts.dto.PriceAlertRequest;
import com.stockpilot.api.alerts.dto.PriceAlertResponse;
import com.stockpilot.api.alerts.service.PriceAlertService;
import com.stockpilot.api.auth.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class PriceAlertController {

    private final PriceAlertService priceAlertService;

    public PriceAlertController(PriceAlertService priceAlertService) {
        this.priceAlertService = priceAlertService;
    }

    @GetMapping
    public ResponseEntity<List<PriceAlertResponse>> getUserAlerts(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<PriceAlertResponse> alerts = priceAlertService.getUserAlerts(userDetails.getId());
        return ResponseEntity.ok(alerts);
    }

    @PostMapping
    public ResponseEntity<PriceAlertResponse> createAlert(
            @Valid @RequestBody PriceAlertRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PriceAlertResponse response = priceAlertService.createAlert(request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PriceAlertResponse> updateAlert(
            @PathVariable Long id,
            @Valid @RequestBody PriceAlertRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PriceAlertResponse response = priceAlertService.updateAlert(id, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<PriceAlertResponse> toggleAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PriceAlertResponse response = priceAlertService.toggleAlert(id, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        priceAlertService.deleteAlert(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
