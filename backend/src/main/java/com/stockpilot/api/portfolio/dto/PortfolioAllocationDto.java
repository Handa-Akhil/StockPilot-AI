package com.stockpilot.api.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioAllocationDto {
    private Long portfolioId;
    private String portfolioName;
    private Map<String, BigDecimal> assetAllocationPercent;
    private Map<String, BigDecimal> assetAllocationValue;
    private Map<String, BigDecimal> sectorAllocationPercent;
    private Map<String, BigDecimal> sectorAllocationValue;
}
