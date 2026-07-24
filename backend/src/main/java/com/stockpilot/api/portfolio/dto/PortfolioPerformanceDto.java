package com.stockpilot.api.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioPerformanceDto {
    private Long portfolioId;
    private String portfolioName;
    private BigDecimal totalInvestment;
    private BigDecimal currentValue;
    private BigDecimal totalProfitLoss;
    private BigDecimal totalProfitLossPercent;
    private BigDecimal unrealizedProfitLoss;
    private BigDecimal unrealizedProfitLossPercent;
    private BigDecimal realizedProfitLoss;
    private BigDecimal todaysGainLoss;
    private BigDecimal todaysReturnPercent;
    private BigDecimal dailyReturnPercent;
    private BigDecimal monthlyReturnPercent;
    private BigDecimal overallReturnPercent;
}
