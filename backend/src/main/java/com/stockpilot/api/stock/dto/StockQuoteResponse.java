package com.stockpilot.api.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockQuoteResponse {
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
    private List<NewsArticle> news;
}
