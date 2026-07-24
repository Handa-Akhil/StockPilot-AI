package com.stockpilot.api.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockQuoteDto {
    private String symbol;
    private String name;
    private Double price;
    private Double change;
    private Double changePercent;
    private Double pe;
    private Double eps;
    private Long marketCap;
    private Long volume;
    private Double high;
    private Double low;
    private Double dividendYield;
    private String exchange;
    private String currency;
}
