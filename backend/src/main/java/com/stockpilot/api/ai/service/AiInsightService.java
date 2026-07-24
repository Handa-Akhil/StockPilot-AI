package com.stockpilot.api.ai.service;

import com.stockpilot.api.ai.dto.MarketSentimentResponse;
import com.stockpilot.api.ai.dto.PortfolioAiAnalysisResponse;
import com.stockpilot.api.ai.dto.StockAiAnalysisResponse;

public interface AiInsightService {
    PortfolioAiAnalysisResponse getPortfolioAnalysis(Long portfolioId, Long userId);
    StockAiAnalysisResponse getStockAnalysis(String symbol);
    MarketSentimentResponse getMarketSentiment();
}
