package com.stockpilot.api.ai.controller;

import com.stockpilot.api.ai.dto.MarketSentimentResponse;
import com.stockpilot.api.ai.dto.PortfolioAiAnalysisResponse;
import com.stockpilot.api.ai.dto.StockAiAnalysisResponse;
import com.stockpilot.api.ai.service.AiInsightService;
import com.stockpilot.api.auth.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiInsightController {

    private final AiInsightService aiInsightService;

    public AiInsightController(AiInsightService aiInsightService) {
        this.aiInsightService = aiInsightService;
    }

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<PortfolioAiAnalysisResponse> getPortfolioAnalysis(
            @PathVariable Long portfolioId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PortfolioAiAnalysisResponse response = aiInsightService.getPortfolioAnalysis(portfolioId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stock/{symbol}")
    public ResponseEntity<StockAiAnalysisResponse> getStockAnalysis(@PathVariable String symbol) {
        StockAiAnalysisResponse response = aiInsightService.getStockAnalysis(symbol);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sentiment")
    public ResponseEntity<MarketSentimentResponse> getMarketSentiment() {
        MarketSentimentResponse response = aiInsightService.getMarketSentiment();
        return ResponseEntity.ok(response);
    }
}
