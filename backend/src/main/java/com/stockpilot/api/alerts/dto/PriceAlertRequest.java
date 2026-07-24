package com.stockpilot.api.alerts.dto;

import com.stockpilot.api.alerts.model.AlertCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertRequest {

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Condition type is required")
    private AlertCondition condition;

    @NotNull(message = "Target price/value is required")
    @Positive(message = "Target price must be a positive number")
    private BigDecimal targetPrice;

    private Long portfolioId;
}
