package com.stockpilot.api.portfolio.dto;

import com.stockpilot.api.portfolio.model.AssetClass;
import com.stockpilot.api.portfolio.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    @NotBlank(message = "Symbol cannot be blank")
    private String symbol;

    @NotNull(message = "Asset class is required")
    private AssetClass assetClass;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    @NotNull(message = "Price is required")
    private BigDecimal price;
}
