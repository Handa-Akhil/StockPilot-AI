package com.stockpilot.api.portfolio.service;

import com.stockpilot.api.portfolio.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PortfolioService {
    PortfolioResponse createPortfolio(PortfolioCreateRequest request, Long userId);
    void softDeletePortfolio(Long portfolioId, Long userId);
    List<PortfolioResponse> listPortfolios(Long userId);
    TransactionResponse recordTransaction(Long portfolioId, TransactionRequest request, Long userId);
    Page<TransactionResponse> getTransactions(Long portfolioId, Long userId, Pageable pageable);
    PortfolioSummaryResponse getSummary(Long portfolioId, Long userId);
}
