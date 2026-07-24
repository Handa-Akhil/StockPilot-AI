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
public class PortfolioHistoryPointDto {
    private String date;
    private BigDecimal portfolioValue;
    private BigDecimal investmentValue;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercent;
}
