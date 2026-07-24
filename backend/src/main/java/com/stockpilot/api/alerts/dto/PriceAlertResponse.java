package com.stockpilot.api.alerts.dto;

import com.stockpilot.api.alerts.model.AlertCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertResponse {
    private Long id;
    private Long portfolioId;
    private String symbol;
    private AlertCondition condition;
    private BigDecimal targetPrice;
    private Boolean enabled;
    private Boolean triggered;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
