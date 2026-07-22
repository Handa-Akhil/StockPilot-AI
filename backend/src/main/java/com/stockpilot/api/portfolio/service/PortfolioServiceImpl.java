package com.stockpilot.api.portfolio.service;

import com.stockpilot.api.auth.User;
import com.stockpilot.api.auth.UserRepository;
import com.stockpilot.api.portfolio.dto.*;
import com.stockpilot.api.portfolio.exception.PortfolioNotFoundException;
import com.stockpilot.api.portfolio.model.*;
import com.stockpilot.api.portfolio.repository.HoldingRepository;
import com.stockpilot.api.portfolio.repository.PortfolioRepository;
import com.stockpilot.api.portfolio.repository.TransactionRepository;
import com.stockpilot.api.stock.dto.StockQuoteResponse;
import com.stockpilot.api.stock.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioServiceImpl implements PortfolioService {
    private static final Logger log = LoggerFactory.getLogger(PortfolioServiceImpl.class);

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final HoldingService holdingService;
    private final StockService stockService;

    public PortfolioServiceImpl(
            PortfolioRepository portfolioRepository,
            HoldingRepository holdingRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            HoldingService holdingService,
            StockService stockService) {
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.holdingService = holdingService;
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public PortfolioResponse createPortfolio(PortfolioCreateRequest request, Long userId) {
        log.info("Creating portfolio named '{}' for user id={}", request.getName(), userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: id=" + userId));

        // Verify unique active portfolio name constraint
        List<Portfolio> activePortfolios = portfolioRepository.findAllActiveByUserId(userId);
        boolean nameExists = activePortfolios.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(request.getName().trim()));
        if (nameExists) {
            throw new IllegalArgumentException("A portfolio with name '" + request.getName() + "' already exists");
        }

        Portfolio portfolio = Portfolio.builder()
                .name(request.getName().trim())
                .user(user)
                .build();
        
        Portfolio saved = portfolioRepository.save(portfolio);
        return PortfolioResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .createdAt(saved.getCreatedAt() != null ? saved.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public void softDeletePortfolio(Long portfolioId, Long userId) {
        log.info("Soft deleting portfolio id={} for user id={}", portfolioId, userId);
        Portfolio portfolio = portfolioRepository.findActiveByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: id=" + portfolioId));
        
        portfolio.setDeletedAt(LocalDateTime.now());
        portfolioRepository.save(portfolio);
    }

    @Override
    public List<PortfolioResponse> listPortfolios(Long userId) {
        log.info("Listing active portfolios for user id={}", userId);
        return portfolioRepository.findAllActiveByUserId(userId).stream()
                .map(p -> PortfolioResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .createdAt(p.getCreatedAt() != null ? p.getCreatedAt() : LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponse recordTransaction(Long portfolioId, TransactionRequest request, Long userId) {
        log.info("Recording {} transaction of {} units for symbol {} in portfolio id={}", 
                request.getTransactionType(), request.getQuantity(), request.getSymbol(), portfolioId);
        
        Portfolio portfolio = portfolioRepository.findActiveByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: id=" + portfolioId));

        BigDecimal realizedPl = null;
        if (request.getTransactionType() == TransactionType.BUY) {
            holdingService.updateHoldingForBuy(portfolio, request, userId);
        } else if (request.getTransactionType() == TransactionType.SELL) {
            realizedPl = holdingService.updateHoldingForSell(portfolio, request, userId);
        }

        Transaction transaction = Transaction.builder()
                .portfolio(portfolio)
                .symbol(request.getSymbol().trim().toUpperCase())
                .assetClass(request.getAssetClass())
                .transactionType(request.getTransactionType())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .realizedPl(realizedPl)
                .transactionTime(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        return TransactionResponse.builder()
                .id(saved.getId())
                .portfolioId(portfolioId)
                .symbol(saved.getSymbol())
                .assetClass(saved.getAssetClass())
                .transactionType(saved.getTransactionType())
                .quantity(saved.getQuantity())
                .price(saved.getPrice())
                .realizedPl(saved.getRealizedPl())
                .transactionTime(saved.getTransactionTime())
                .build();
    }

    @Override
    public Page<TransactionResponse> getTransactions(Long portfolioId, Long userId, Pageable pageable) {
        log.info("Fetching transaction log for portfolio id={}, user id={}", portfolioId, userId);
        portfolioRepository.findActiveByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: id=" + portfolioId));

        return transactionRepository.findAllByPortfolioIdAndUserId(portfolioId, userId, pageable)
                .map(t -> TransactionResponse.builder()
                        .id(t.getId())
                        .portfolioId(portfolioId)
                        .symbol(t.getSymbol())
                        .assetClass(t.getAssetClass())
                        .transactionType(t.getTransactionType())
                        .quantity(t.getQuantity())
                        .price(t.getPrice())
                        .realizedPl(t.getRealizedPl())
                        .transactionTime(t.getTransactionTime())
                        .build());
    }

    @Override
    public PortfolioSummaryResponse getSummary(Long portfolioId, Long userId) {
        log.info("Loading dynamic portfolio summary for portfolio id={}, user id={}", portfolioId, userId);
        Portfolio portfolio = portfolioRepository.findActiveByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: id=" + portfolioId));

        List<Holding> databaseHoldings = holdingRepository.findAllByPortfolioIdAndUserId(portfolioId, userId);

        BigDecimal totalCostBasis = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        
        List<HoldingResponse> holdingsList = new ArrayList<>();

        for (Holding h : databaseHoldings) {
            BigDecimal qty = h.getQuantity();
            BigDecimal avgPrice = h.getAverageBuyPrice();
            BigDecimal costBasis = qty.multiply(avgPrice);
            
            BigDecimal currentPrice = avgPrice;
            try {
                StockQuoteResponse quote = stockService.getQuote(h.getSymbol());
                if (quote != null && quote.getPrice() != null) {
                    currentPrice = BigDecimal.valueOf(quote.getPrice());
                }
            } catch (Exception ex) {
                log.warn("Failed to fetch live price for symbol {}: {}. Falling back to cost basis.", 
                        h.getSymbol(), ex.getMessage());
            }

            BigDecimal currentValue = qty.multiply(currentPrice);
            BigDecimal unrealizedPl = currentValue.subtract(costBasis);
            BigDecimal unrealizedPlPercent = BigDecimal.ZERO;
            if (costBasis.compareTo(BigDecimal.ZERO) > 0) {
                unrealizedPlPercent = unrealizedPl.multiply(BigDecimal.valueOf(100))
                        .divide(costBasis, 4, RoundingMode.HALF_UP);
            }

            totalCostBasis = totalCostBasis.add(costBasis);
            totalCurrentValue = totalCurrentValue.add(currentValue);

            holdingsList.add(HoldingResponse.builder()
                    .symbol(h.getSymbol())
                    .assetClass(h.getAssetClass())
                    .quantity(qty)
                    .averageBuyPrice(avgPrice)
                    .costBasis(costBasis)
                    .currentPrice(currentPrice)
                    .currentValue(currentValue)
                    .unrealizedPl(unrealizedPl)
                    .unrealizedPlPercent(unrealizedPlPercent)
                    .allocationPercent(BigDecimal.ZERO)
                    .build());
        }

        for (HoldingResponse hr : holdingsList) {
            if (totalCurrentValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal alloc = hr.getCurrentValue().multiply(BigDecimal.valueOf(100))
                        .divide(totalCurrentValue, 4, RoundingMode.HALF_UP);
                hr.setAllocationPercent(alloc);
            }
        }

        BigDecimal totalUnrealizedPl = totalCurrentValue.subtract(totalCostBasis);
        BigDecimal totalUnrealizedPlPercent = BigDecimal.ZERO;
        if (totalCostBasis.compareTo(BigDecimal.ZERO) > 0) {
            totalUnrealizedPlPercent = totalUnrealizedPl.multiply(BigDecimal.valueOf(100))
                    .divide(totalCostBasis, 4, RoundingMode.HALF_UP);
        }

        Page<TransactionResponse> allTx = getTransactions(portfolioId, userId, Pageable.unpaged());
        BigDecimal totalRealizedPl = allTx.getContent().stream()
                .map(TransactionResponse::getRealizedPl)
                .filter(pl -> pl != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PortfolioMetricsResponse metrics = PortfolioMetricsResponse.builder()
                .totalCostBasis(totalCostBasis)
                .currentValue(totalCurrentValue)
                .unrealizedPl(totalUnrealizedPl)
                .unrealizedPlPercent(totalUnrealizedPlPercent)
                .realizedPl(totalRealizedPl)
                .build();

        return PortfolioSummaryResponse.builder()
                .portfolioId(portfolioId)
                .portfolioName(portfolio.getName())
                .metrics(metrics)
                .holdings(holdingsList)
                .build();
    }
}
