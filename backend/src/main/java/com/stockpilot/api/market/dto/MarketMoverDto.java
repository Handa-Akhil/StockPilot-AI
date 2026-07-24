package com.stockpilot.api.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketMoverDto {
    private String symbol;
    private String name;
    private Double price;
    private Double change;
    private Double changePercent;
    private Long volume;
}
