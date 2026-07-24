package com.stockpilot.api.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalDataResponse {
    private String symbol;
    private String timeframe;
    private List<HistoricalPriceDto> points;
}
