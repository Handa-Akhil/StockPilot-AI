package com.stockpilot.api.portfolio.controller;

import com.stockpilot.api.auth.security.UserDetailsImpl;
import com.stockpilot.api.portfolio.dto.*;
import com.stockpilot.api.portfolio.service.PortfolioIntelligenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioIntelligenceController {

    private static final Logger log = LoggerFactory.getLogger(PortfolioIntelligenceController.class);

    private final PortfolioIntelligenceService portfolioIntelligenceService;

    public PortfolioIntelligenceController(PortfolioIntelligenceService portfolioIntelligenceService) {
        this.portfolioIntelligenceService = portfolioIntelligenceService;
    }

    @GetMapping("/summary")
    public ResponseEntity<PortfolioSummaryResponse> getSummary(
            @RequestParam(required = false) Long portfolioId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Request received: GET /api/v1/portfolio/summary for user id={} portfolioId={}", userDetails.getId(), portfolioId);
        PortfolioSummaryResponse response = portfolioIntelligenceService.getSummary(userDetails.getId(), portfolioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/holdings")
    public ResponseEntity<List<HoldingResponse>> getHoldings(
            @RequestParam(required = false) Long portfolioId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Request received: GET /api/v1/portfolio/holdings for user id={} portfolioId={}", userDetails.getId(), portfolioId);
        List<HoldingResponse> response = portfolioIntelligenceService.getHoldings(userDetails.getId(), portfolioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/performance")
    public ResponseEntity<PortfolioPerformanceDto> getPerformance(
            @RequestParam(required = false) Long portfolioId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Request received: GET /api/v1/portfolio/performance for user id={} portfolioId={}", userDetails.getId(), portfolioId);
        PortfolioPerformanceDto response = portfolioIntelligenceService.getPerformance(userDetails.getId(), portfolioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/allocation")
    public ResponseEntity<PortfolioAllocationDto> getAllocation(
            @RequestParam(required = false) Long portfolioId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Request received: GET /api/v1/portfolio/allocation for user id={} portfolioId={}", userDetails.getId(), portfolioId);
        PortfolioAllocationDto response = portfolioIntelligenceService.getAllocation(userDetails.getId(), portfolioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/risk")
    public ResponseEntity<PortfolioRiskDto> getRisk(
            @RequestParam(required = false) Long portfolioId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Request received: GET /api/v1/portfolio/risk for user id={} portfolioId={}", userDetails.getId(), portfolioId);
        PortfolioRiskDto response = portfolioIntelligenceService.getRisk(userDetails.getId(), portfolioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<PortfolioHistoryDto> getHistory(
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(defaultValue = "1M") String timeframe,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Request received: GET /api/v1/portfolio/history for user id={} portfolioId={} timeframe={}", userDetails.getId(), portfolioId, timeframe);
        PortfolioHistoryDto response = portfolioIntelligenceService.getHistory(userDetails.getId(), portfolioId, timeframe);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-performers")
    public ResponseEntity<PortfolioTopPerformersDto> getTopPerformers(
            @RequestParam(required = false) Long portfolioId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Request received: GET /api/v1/portfolio/top-performers for user id={} portfolioId={}", userDetails.getId(), portfolioId);
        PortfolioTopPerformersDto response = portfolioIntelligenceService.getTopPerformers(userDetails.getId(), portfolioId);
        return ResponseEntity.ok(response);
    }
}
