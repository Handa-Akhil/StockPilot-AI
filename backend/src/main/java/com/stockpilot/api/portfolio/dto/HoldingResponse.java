package com.stockpilot.api.portfolio.dto;

import com.stockpilot.api.portfolio.model.AssetClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldingResponse {
    private String symbol;
    private AssetClass assetClass;
    private BigDecimal quantity;
    private BigDecimal averageBuyPrice;
    private BigDecimal costBasis;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal unrealizedPl;
    private BigDecimal unrealizedPlPercent;
    private BigDecimal allocationPercent;
}
