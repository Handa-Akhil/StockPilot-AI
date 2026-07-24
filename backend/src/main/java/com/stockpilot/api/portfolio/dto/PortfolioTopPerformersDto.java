package com.stockpilot.api.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioTopPerformersDto {
    private Long portfolioId;
    private String portfolioName;
    private List<HoldingResponse> topPerformers;
    private List<HoldingResponse> worstPerformers;
}
