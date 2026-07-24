package com.stockpilot.api.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendingStockDto {
    private String symbol;
    private String name;
    private Double price;
    private Double changePercent;
    private Long volume;
    private String category; // GAINER | LOSER | MOST_ACTIVE
}
