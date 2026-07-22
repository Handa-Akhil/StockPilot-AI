package com.stockpilot.api.portfolio.controller;

import com.stockpilot.api.auth.security.UserDetailsImpl;
import com.stockpilot.api.portfolio.dto.*;
import com.stockpilot.api.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Purpose: REST Controller exposing Portfolio Management endpoints.
 * Responsibilities: Handles portfolio creation, transactions recording, paginated history, valuations, and soft deletes.
 * Dependencies: PortfolioService, UserDetailsImpl principal.
 * Flow: Receives request, injects authenticated user details, forwards to service layer.
 */
@RestController
@RequestMapping("/api/v1/portfolios")
public class PortfolioController {
    private static final Logger log = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(
            @Valid @RequestBody PortfolioCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Request received: POST /api/v1/portfolios from user id={}", userDetails.getId());
        PortfolioResponse response = portfolioService.createPortfolio(request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> listPortfolios(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Request received: GET /api/v1/portfolios from user id={}", userDetails.getId());
        List<PortfolioResponse> response = portfolioService.listPortfolios(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Map<String, Object>> deletePortfolio(
            @PathVariable Long portfolioId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Request received: DELETE /api/v1/portfolios/{} from user id={}", portfolioId, userDetails.getId());
        portfolioService.softDeletePortfolio(portfolioId, userDetails.getId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Portfolio soft-deleted successfully"
        ));
    }

    @PostMapping("/{portfolioId}/transactions")
    public ResponseEntity<TransactionResponse> recordTransaction(
            @PathVariable Long portfolioId,
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Request received: POST /api/v1/portfolios/{}/transactions from user id={}", portfolioId, userDetails.getId());
        TransactionResponse response = portfolioService.recordTransaction(portfolioId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{portfolioId}/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @PathVariable Long portfolioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Request received: GET /api/v1/portfolios/{}/transactions from user id={}", portfolioId, userDetails.getId());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionTime"));
        Page<TransactionResponse> response = portfolioService.getTransactions(portfolioId, userDetails.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioSummaryResponse> getSummary(
            @PathVariable Long portfolioId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Request received: GET /api/v1/portfolios/{} from user id={}", portfolioId, userDetails.getId());
        PortfolioSummaryResponse response = portfolioService.getSummary(portfolioId, userDetails.getId());
        return ResponseEntity.ok(response);
    }
}
