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
public class PortfolioRiskDto {
    private Long portfolioId;
    private String portfolioName;
    private Double portfolioBeta;
    private Integer riskScore;
    private String riskLevel;
    private Integer diversityScore;
    private String diversificationAnalysis;
    private BigDecimal topHoldingConcentrationPercent;
    private String highestRiskAsset;
}
