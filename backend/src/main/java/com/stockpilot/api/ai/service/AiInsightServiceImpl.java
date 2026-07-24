package com.stockpilot.api.ai.service;

import com.stockpilot.api.ai.client.AiServiceClient;
import com.stockpilot.api.ai.dto.MarketSentimentResponse;
import com.stockpilot.api.ai.dto.PortfolioAiAnalysisResponse;
import com.stockpilot.api.ai.dto.StockAiAnalysisResponse;
import com.stockpilot.api.portfolio.dto.PortfolioSummaryResponse;
import com.stockpilot.api.portfolio.service.PortfolioService;
import org.springframework.stereotype.Service;

@Service
public class AiInsightServiceImpl implements AiInsightService {

    private final PortfolioService portfolioService;
    private final AiServiceClient aiServiceClient;

    public AiInsightServiceImpl(PortfolioService portfolioService, AiServiceClient aiServiceClient) {
        this.portfolioService = portfolioService;
        this.aiServiceClient = aiServiceClient;
    }

    @Override
    public PortfolioAiAnalysisResponse getPortfolioAnalysis(Long portfolioId, Long userId) {
        PortfolioSummaryResponse summary = portfolioService.getSummary(portfolioId, userId);
        return aiServiceClient.analyzePortfolio(portfolioId, summary.getHoldings());
    }

    @Override
    public StockAiAnalysisResponse getStockAnalysis(String symbol) {
        return aiServiceClient.analyzeStock(symbol);
    }

    @Override
    public MarketSentimentResponse getMarketSentiment() {
        return aiServiceClient.getMarketSentiment();
    }
}
