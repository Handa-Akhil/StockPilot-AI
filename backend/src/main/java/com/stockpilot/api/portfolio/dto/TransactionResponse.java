package com.stockpilot.api.portfolio.dto;

import com.stockpilot.api.portfolio.model.AssetClass;
import com.stockpilot.api.portfolio.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private Long portfolioId;
    private String symbol;
    private AssetClass assetClass;
    private TransactionType transactionType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal realizedPl;
    private LocalDateTime transactionTime;
}
