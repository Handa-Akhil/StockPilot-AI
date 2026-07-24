package com.stockpilot.api.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalPriceDto {
    private String date;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
}
