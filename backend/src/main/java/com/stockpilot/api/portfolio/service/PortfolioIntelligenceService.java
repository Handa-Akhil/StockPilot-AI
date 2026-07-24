package com.stockpilot.api.portfolio.service;

import com.stockpilot.api.portfolio.dto.*;

import java.util.List;

public interface PortfolioIntelligenceService {
    PortfolioSummaryResponse getSummary(Long userId, Long portfolioId);
    List<HoldingResponse> getHoldings(Long userId, Long portfolioId);
    PortfolioPerformanceDto getPerformance(Long userId, Long portfolioId);
    PortfolioAllocationDto getAllocation(Long userId, Long portfolioId);
    PortfolioRiskDto getRisk(Long userId, Long portfolioId);
    PortfolioHistoryDto getHistory(Long userId, Long portfolioId, String timeframe);
    PortfolioTopPerformersDto getTopPerformers(Long userId, Long portfolioId);
}
