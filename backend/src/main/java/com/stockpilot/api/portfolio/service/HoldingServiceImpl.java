package com.stockpilot.api.portfolio.service;

import com.stockpilot.api.portfolio.dto.TransactionRequest;
import com.stockpilot.api.portfolio.exception.InsufficientSharesException;
import com.stockpilot.api.portfolio.model.Holding;
import com.stockpilot.api.portfolio.model.Portfolio;
import com.stockpilot.api.portfolio.repository.HoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class HoldingServiceImpl implements HoldingService {

    private final HoldingRepository holdingRepository;

    public HoldingServiceImpl(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    @Override
    @Transactional
    public Holding updateHoldingForBuy(Portfolio portfolio, TransactionRequest request, Long userId) {
        Optional<Holding> optionalHolding = holdingRepository.findByPortfolioIdAndSymbolAndUserId(
                portfolio.getId(), request.getSymbol(), userId
        );

        Holding holding;
        if (optionalHolding.isPresent()) {
            holding = optionalHolding.get();
            BigDecimal currentQty = holding.getQuantity();
            BigDecimal currentAvgPrice = holding.getAverageBuyPrice();

            BigDecimal requestQty = request.getQuantity();
            BigDecimal requestPrice = request.getPrice();

            BigDecimal newQty = currentQty.add(requestQty);
            
            BigDecimal currentCost = currentQty.multiply(currentAvgPrice);
            BigDecimal requestCost = requestQty.multiply(requestPrice);
            BigDecimal totalCost = currentCost.add(requestCost);
            BigDecimal newAvgPrice = totalCost.divide(newQty, 4, RoundingMode.HALF_UP);

            holding.setQuantity(newQty);
            holding.setAverageBuyPrice(newAvgPrice);
        } else {
            holding = Holding.builder()
                    .portfolio(portfolio)
                    .symbol(request.getSymbol())
                    .assetClass(request.getAssetClass())
                    .quantity(request.getQuantity())
                    .averageBuyPrice(request.getPrice())
                    .build();
        }

        return holdingRepository.save(holding);
    }

    @Override
    @Transactional
    public BigDecimal updateHoldingForSell(Portfolio portfolio, TransactionRequest request, Long userId) {
        Holding holding = holdingRepository.findByPortfolioIdAndSymbolAndUserId(
                portfolio.getId(), request.getSymbol(), userId
        ).orElseThrow(() -> new InsufficientSharesException("No holdings found for symbol: " + request.getSymbol()));

        BigDecimal currentQty = holding.getQuantity();
        BigDecimal requestQty = request.getQuantity();

        if (currentQty.compareTo(requestQty) < 0) {
            throw new InsufficientSharesException(
                    String.format("Insufficient shares to sell. Owned: %s, Requested: %s", currentQty, requestQty)
            );
        }

        BigDecimal averageBuyPrice = holding.getAverageBuyPrice();
        BigDecimal requestPrice = request.getPrice();
        BigDecimal priceDiff = requestPrice.subtract(averageBuyPrice);
        BigDecimal realizedPl = priceDiff.multiply(requestQty);

        BigDecimal newQty = currentQty.subtract(requestQty);
        if (newQty.compareTo(BigDecimal.ZERO) == 0) {
            holdingRepository.delete(holding);
        } else {
            holding.setQuantity(newQty);
            holdingRepository.save(holding);
        }

        return realizedPl;
    }
}
