package com.stockpilot.api.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {
    private String symbol;
    private String action; // BUY | HOLD | SELL
    private Double confidence;
    private String explanation;
    private String expectedImpact;
    private String riskLevel; // LOW | MEDIUM | HIGH
}
