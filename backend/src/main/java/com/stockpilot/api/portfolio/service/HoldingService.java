package com.stockpilot.api.portfolio.service;

import com.stockpilot.api.portfolio.dto.TransactionRequest;
import com.stockpilot.api.portfolio.model.Holding;
import com.stockpilot.api.portfolio.model.Portfolio;

import java.math.BigDecimal;

public interface HoldingService {
    Holding updateHoldingForBuy(Portfolio portfolio, TransactionRequest request, Long userId);
    BigDecimal updateHoldingForSell(Portfolio portfolio, TransactionRequest request, Long userId);
}
