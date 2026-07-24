package com.stockpilot.api.portfolio.service;

import com.stockpilot.api.market.client.MarketDataClient;
import com.stockpilot.api.market.dto.CompanyProfileDto;
import com.stockpilot.api.portfolio.dto.*;
import com.stockpilot.api.portfolio.exception.PortfolioNotFoundException;
import com.stockpilot.api.portfolio.model.Holding;
import com.stockpilot.api.portfolio.model.Portfolio;
import com.stockpilot.api.portfolio.repository.HoldingRepository;
import com.stockpilot.api.portfolio.repository.PortfolioRepository;
import com.stockpilot.api.stock.dto.StockQuoteResponse;
import com.stockpilot.api.stock.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioIntelligenceServiceImpl implements PortfolioIntelligenceService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioIntelligenceServiceImpl.class);

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final StockService stockService;
    private final MarketDataClient marketDataClient;

    public PortfolioIntelligenceServiceImpl(
            PortfolioRepository portfolioRepository,
            HoldingRepository holdingRepository,
            StockService stockService,
            MarketDataClient marketDataClient) {
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
        this.stockService = stockService;
        this.marketDataClient = marketDataClient;
    }

    private Portfolio resolvePortfolio(Long userId, Long portfolioId) {
        if (portfolioId != null) {
            return portfolioRepository.findActiveByIdAndUserId(portfolioId, userId)
                    .orElseThrow(() -> new PortfolioNotFoundException("Active portfolio not found with id: " + portfolioId));
        }
        List<Portfolio> list = portfolioRepository.findAllActiveByUserId(userId);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    @Cacheable(value = "portfolio:summary", key = "#userId + ':' + (#portfolioId != null ? #portfolioId : 'default')", unless = "#result == null")
    public PortfolioSummaryResponse getSummary(Long userId, Long portfolioId) {
        Portfolio portfolio = resolvePortfolio(userId, portfolioId);
        if (portfolio == null) {
            return PortfolioSummaryResponse.builder()
                    .portfolioId(null)
                    .portfolioName("No Active Portfolio")
                    .metrics(PortfolioMetricsResponse.builder()
                            .totalCostBasis(BigDecimal.ZERO)
                            .currentValue(BigDecimal.ZERO)
                            .unrealizedPl(BigDecimal.ZERO)
                            .unrealizedPlPercent(BigDecimal.ZERO)
                            .realizedPl(BigDecimal.ZERO)
                            .build())
                    .holdings(Collections.emptyList())
                    .build();
        }

        List<HoldingResponse> holdings = getHoldings(userId, portfolio.getId());
        BigDecimal totalCost = holdings.stream()
                .map(HoldingResponse::getCostBasis)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentValue = holdings.stream()
                .map(HoldingResponse::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unrealizedPl = currentValue.subtract(totalCost);
        BigDecimal unrealizedPlPct = (totalCost.compareTo(BigDecimal.ZERO) > 0)
                ? unrealizedPl.multiply(BigDecimal.valueOf(100)).divide(totalCost, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        PortfolioMetricsResponse metrics = PortfolioMetricsResponse.builder()
                .totalCostBasis(totalCost)
                .currentValue(currentValue)
                .unrealizedPl(unrealizedPl)
                .unrealizedPlPercent(unrealizedPlPct)
                .realizedPl(BigDecimal.ZERO)
                .build();

        return PortfolioSummaryResponse.builder()
                .portfolioId(portfolio.getId())
                .portfolioName(portfolio.getName())
                .metrics(metrics)
                .holdings(holdings)
                .build();
    }

    @Override
    public List<HoldingResponse> getHoldings(Long userId, Long portfolioId) {
        Portfolio portfolio = resolvePortfolio(userId, portfolioId);
        if (portfolio == null) {
            return Collections.emptyList();
        }

        List<Holding> entityHoldings = holdingRepository.findAllByPortfolioIdAndUserId(portfolio.getId(), userId);
        if (entityHoldings.isEmpty()) {
            return Collections.emptyList();
        }

        // Calculate total preliminary value for allocation %
        BigDecimal preliminaryTotal = BigDecimal.ZERO;
        Map<String, BigDecimal> currentPrices = new HashMap<>();

        for (Holding h : entityHoldings) {
            BigDecimal price = BigDecimal.ZERO;
            try {
                StockQuoteResponse q = stockService.getQuote(h.getSymbol());
                if (q != null && q.getPrice() != null) {
                    price = BigDecimal.valueOf(q.getPrice());
                }
            } catch (Exception ex) {
                log.warn("Unable to fetch price for holding {}: {}", h.getSymbol(), ex.getMessage());
                price = h.getAverageBuyPrice() != null ? h.getAverageBuyPrice() : BigDecimal.ZERO;
            }
            currentPrices.put(h.getSymbol(), price);
            BigDecimal val = h.getQuantity().multiply(price);
            preliminaryTotal = preliminaryTotal.add(val);
        }

        final BigDecimal finalTotal = preliminaryTotal;

        return entityHoldings.stream().map(h -> {
            BigDecimal price = currentPrices.getOrDefault(h.getSymbol(), BigDecimal.ZERO);
            BigDecimal val = h.getQuantity().multiply(price);
            BigDecimal cost = h.getQuantity().multiply(h.getAverageBuyPrice() != null ? h.getAverageBuyPrice() : BigDecimal.ZERO);
            BigDecimal unPl = val.subtract(cost);
            BigDecimal unPlPct = (cost.compareTo(BigDecimal.ZERO) > 0)
                    ? unPl.multiply(BigDecimal.valueOf(100)).divide(cost, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal allocPct = (finalTotal.compareTo(BigDecimal.ZERO) > 0)
                    ? val.multiply(BigDecimal.valueOf(100)).divide(finalTotal, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return HoldingResponse.builder()
                    .symbol(h.getSymbol())
                    .assetClass(h.getAssetClass())
                    .quantity(h.getQuantity())
                    .averageBuyPrice(h.getAverageBuyPrice())
                    .costBasis(cost)
                    .currentPrice(price)
                    .currentValue(val)
                    .unrealizedPl(unPl)
                    .unrealizedPlPercent(unPlPct)
                    .allocationPercent(allocPct)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "portfolio:analytics", key = "'perf:' + #userId + ':' + (#portfolioId != null ? #portfolioId : 'default')", unless = "#result == null")
    public PortfolioPerformanceDto getPerformance(Long userId, Long portfolioId) {
        Portfolio portfolio = resolvePortfolio(userId, portfolioId);
        if (portfolio == null) {
            return PortfolioPerformanceDto.builder()
                    .portfolioName("No Portfolio")
                    .totalInvestment(BigDecimal.ZERO)
                    .currentValue(BigDecimal.ZERO)
                    .totalProfitLoss(BigDecimal.ZERO)
                    .totalProfitLossPercent(BigDecimal.ZERO)
                    .unrealizedProfitLoss(BigDecimal.ZERO)
                    .unrealizedProfitLossPercent(BigDecimal.ZERO)
                    .realizedProfitLoss(BigDecimal.ZERO)
                    .todaysGainLoss(BigDecimal.ZERO)
                    .todaysReturnPercent(BigDecimal.ZERO)
                    .dailyReturnPercent(BigDecimal.ZERO)
                    .monthlyReturnPercent(BigDecimal.ZERO)
                    .overallReturnPercent(BigDecimal.ZERO)
                    .build();
        }

        List<HoldingResponse> holdings = getHoldings(userId, portfolio.getId());
        BigDecimal totalInvestment = holdings.stream().map(HoldingResponse::getCostBasis).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal currentValue = holdings.stream().map(HoldingResponse::getCurrentValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPl = currentValue.subtract(totalInvestment);
        BigDecimal totalPlPct = (totalInvestment.compareTo(BigDecimal.ZERO) > 0)
                ? totalPl.multiply(BigDecimal.valueOf(100)).divide(totalInvestment, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal todaysGainLoss = BigDecimal.ZERO;
        for (HoldingResponse h : holdings) {
            try {
                StockQuoteResponse q = stockService.getQuote(h.getSymbol());
                if (q != null && q.getChange() != null) {
                    BigDecimal change = BigDecimal.valueOf(q.getChange());
                    todaysGainLoss = todaysGainLoss.add(h.getQuantity().multiply(change));
                }
            } catch (Exception ignored) {}
        }

        BigDecimal prevValue = currentValue.subtract(todaysGainLoss);
        BigDecimal todaysReturnPct = (prevValue.compareTo(BigDecimal.ZERO) > 0)
                ? todaysGainLoss.multiply(BigDecimal.valueOf(100)).divide(prevValue, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return PortfolioPerformanceDto.builder()
                .portfolioId(portfolio.getId())
                .portfolioName(portfolio.getName())
                .totalInvestment(totalInvestment)
                .currentValue(currentValue)
                .totalProfitLoss(totalPl)
                .totalProfitLossPercent(totalPlPct)
                .unrealizedProfitLoss(totalPl)
                .unrealizedProfitLossPercent(totalPlPct)
                .realizedProfitLoss(BigDecimal.ZERO)
                .todaysGainLoss(todaysGainLoss)
                .todaysReturnPercent(todaysReturnPct)
                .dailyReturnPercent(todaysReturnPct)
                .monthlyReturnPercent(totalPlPct.multiply(BigDecimal.valueOf(0.35)).setScale(2, RoundingMode.HALF_UP))
                .overallReturnPercent(totalPlPct)
                .build();
    }

    @Override
    @Cacheable(value = "portfolio:analytics", key = "'alloc:' + #userId + ':' + (#portfolioId != null ? #portfolioId : 'default')", unless = "#result == null")
    public PortfolioAllocationDto getAllocation(Long userId, Long portfolioId) {
        Portfolio portfolio = resolvePortfolio(userId, portfolioId);
        if (portfolio == null) {
            return PortfolioAllocationDto.builder()
                    .portfolioName("No Portfolio")
                    .assetAllocationPercent(Collections.emptyMap())
                    .assetAllocationValue(Collections.emptyMap())
                    .sectorAllocationPercent(Collections.emptyMap())
                    .sectorAllocationValue(Collections.emptyMap())
                    .build();
        }

        List<HoldingResponse> holdings = getHoldings(userId, portfolio.getId());
        BigDecimal totalVal = holdings.stream().map(HoldingResponse::getCurrentValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> assetVal = new HashMap<>();
        Map<String, BigDecimal> sectorVal = new HashMap<>();

        for (HoldingResponse h : holdings) {
            String assetClass = h.getAssetClass() != null ? h.getAssetClass().name() : "EQUITY";
            assetVal.put(assetClass, assetVal.getOrDefault(assetClass, BigDecimal.ZERO).add(h.getCurrentValue()));

            String sector = "Technology";
            try {
                CompanyProfileDto profile = marketDataClient.getProfile(h.getSymbol());
                if (profile != null && profile.getSector() != null && !profile.getSector().isBlank()) {
                    sector = profile.getSector();
                }
            } catch (Exception ignored) {}
            sectorVal.put(sector, sectorVal.getOrDefault(sector, BigDecimal.ZERO).add(h.getCurrentValue()));
        }

        Map<String, BigDecimal> assetPct = new HashMap<>();
        assetVal.forEach((key, val) -> {
            BigDecimal pct = (totalVal.compareTo(BigDecimal.ZERO) > 0)
                    ? val.multiply(BigDecimal.valueOf(100)).divide(totalVal, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            assetPct.put(key, pct);
        });

        Map<String, BigDecimal> sectorPct = new HashMap<>();
        sectorVal.forEach((key, val) -> {
            BigDecimal pct = (totalVal.compareTo(BigDecimal.ZERO) > 0)
                    ? val.multiply(BigDecimal.valueOf(100)).divide(totalVal, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            sectorPct.put(key, pct);
        });

        return PortfolioAllocationDto.builder()
                .portfolioId(portfolio.getId())
                .portfolioName(portfolio.getName())
                .assetAllocationPercent(assetPct)
                .assetAllocationValue(assetVal)
                .sectorAllocationPercent(sectorPct)
                .sectorAllocationValue(sectorVal)
                .build();
    }

    @Override
    @Cacheable(value = "portfolio:analytics", key = "'risk:' + #userId + ':' + (#portfolioId != null ? #portfolioId : 'default')", unless = "#result == null")
    public PortfolioRiskDto getRisk(Long userId, Long portfolioId) {
        Portfolio portfolio = resolvePortfolio(userId, portfolioId);
        if (portfolio == null) {
            return PortfolioRiskDto.builder()
                    .portfolioName("No Portfolio")
                    .portfolioBeta(1.0)
                    .riskScore(50)
                    .riskLevel("MODERATE")
                    .diversityScore(50)
                    .diversificationAnalysis("No holdings present.")
                    .topHoldingConcentrationPercent(BigDecimal.ZERO)
                    .highestRiskAsset("N/A")
                    .build();
        }

        List<HoldingResponse> holdings = getHoldings(userId, portfolio.getId());
        if (holdings.isEmpty()) {
            return PortfolioRiskDto.builder()
                    .portfolioId(portfolio.getId())
                    .portfolioName(portfolio.getName())
                    .portfolioBeta(1.0)
                    .riskScore(50)
                    .riskLevel("MODERATE")
                    .diversityScore(20)
                    .diversificationAnalysis("Empty portfolio. Consider adding diversified index funds or blue-chip equities.")
                    .topHoldingConcentrationPercent(BigDecimal.ZERO)
                    .highestRiskAsset("N/A")
                    .build();
        }

        BigDecimal maxAlloc = holdings.stream().map(HoldingResponse::getAllocationPercent).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        HoldingResponse topHolding = holdings.stream().max(Comparator.comparing(HoldingResponse::getAllocationPercent)).orElse(holdings.get(0));

        int count = holdings.size();
        int diversityScore;
        if (count < 3 || maxAlloc.doubleValue() > 40.0) {
            diversityScore = Math.max(30, (int) (100 - maxAlloc.doubleValue()));
        } else if (count <= 8) {
            diversityScore = 85;
        } else {
            diversityScore = 95;
        }

        double weightedBeta = 1.0;
        int riskScore = 55;
        if (maxAlloc.doubleValue() > 50.0) {
            riskScore += 25;
            weightedBeta = 1.35;
        } else if (count >= 5) {
            riskScore = 45;
            weightedBeta = 1.05;
        }

        String riskLevel = riskScore <= 40 ? "LOW" : (riskScore <= 65 ? "MODERATE" : (riskScore <= 85 ? "HIGH" : "VERY_HIGH"));

        String analysis = String.format("Portfolio spans %d holdings. Largest holding (%s) represents %.1f%% of total capital.",
                count, topHolding.getSymbol(), maxAlloc.doubleValue());

        return PortfolioRiskDto.builder()
                .portfolioId(portfolio.getId())
                .portfolioName(portfolio.getName())
                .portfolioBeta(weightedBeta)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .diversityScore(diversityScore)
                .diversificationAnalysis(analysis)
                .topHoldingConcentrationPercent(maxAlloc)
                .highestRiskAsset(topHolding.getSymbol())
                .build();
    }

    @Override
    @Cacheable(value = "portfolio:history", key = "#userId + ':' + (#portfolioId != null ? #portfolioId : 'default') + ':' + #timeframe", unless = "#result == null")
    public PortfolioHistoryDto getHistory(Long userId, Long portfolioId, String timeframe) {
        Portfolio portfolio = resolvePortfolio(userId, portfolioId);
        String tf = (timeframe == null || timeframe.isBlank()) ? "1M" : timeframe.toUpperCase();

        if (portfolio == null) {
            return PortfolioHistoryDto.builder()
                    .portfolioName("No Portfolio")
                    .timeframe(tf)
                    .points(Collections.emptyList())
                    .build();
        }

        PortfolioPerformanceDto perf = getPerformance(userId, portfolio.getId());
        BigDecimal currentVal = perf.getCurrentValue();
        BigDecimal costBasis = perf.getTotalInvestment();

        int days = 30;
        switch (tf) {
            case "1W": days = 7; break;
            case "1M": days = 30; break;
            case "3M": days = 90; break;
            case "6M": days = 180; break;
            case "1Y": days = 365; break;
            case "ALL": days = 365; break;
            default: days = 30;
        }

        List<PortfolioHistoryPointDto> points = new ArrayList<>();
        LocalDate now = LocalDate.now();

        double baseValue = currentVal.doubleValue();
        double baseCost = costBasis.doubleValue();

        for (int i = days; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            double progress = (days - i) / (double) Math.max(days, 1);
            double simulatedVal = baseCost + (baseValue - baseCost) * progress + (Math.sin(i / 2.0) * (baseValue * 0.015));
            if (simulatedVal < 0) simulatedVal = 0;

            double pl = simulatedVal - baseCost;
            double plPct = (baseCost > 0) ? (pl / baseCost * 100.0) : 0.0;

            points.add(PortfolioHistoryPointDto.builder()
                    .date(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .portfolioValue(BigDecimal.valueOf(simulatedVal).setScale(2, RoundingMode.HALF_UP))
                    .investmentValue(BigDecimal.valueOf(baseCost).setScale(2, RoundingMode.HALF_UP))
                    .profitLoss(BigDecimal.valueOf(pl).setScale(2, RoundingMode.HALF_UP))
                    .profitLossPercent(BigDecimal.valueOf(plPct).setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        return PortfolioHistoryDto.builder()
                .portfolioId(portfolio.getId())
                .portfolioName(portfolio.getName())
                .timeframe(tf)
                .points(points)
                .build();
    }

    @Override
    @Cacheable(value = "portfolio:analytics", key = "'top:' + #userId + ':' + (#portfolioId != null ? #portfolioId : 'default')", unless = "#result == null")
    public PortfolioTopPerformersDto getTopPerformers(Long userId, Long portfolioId) {
        Portfolio portfolio = resolvePortfolio(userId, portfolioId);
        if (portfolio == null) {
            return PortfolioTopPerformersDto.builder()
                    .portfolioName("No Portfolio")
                    .topPerformers(Collections.emptyList())
                    .worstPerformers(Collections.emptyList())
                    .build();
        }

        List<HoldingResponse> holdings = getHoldings(userId, portfolio.getId());
        List<HoldingResponse> sorted = holdings.stream()
                .sorted(Comparator.comparing(HoldingResponse::getUnrealizedPlPercent).reversed())
                .collect(Collectors.toList());

        List<HoldingResponse> top = sorted.stream().limit(3).collect(Collectors.toList());
        List<HoldingResponse> worst = sorted.stream().sorted(Comparator.comparing(HoldingResponse::getUnrealizedPlPercent)).limit(3).collect(Collectors.toList());

        return PortfolioTopPerformersDto.builder()
                .portfolioId(portfolio.getId())
                .portfolioName(portfolio.getName())
                .topPerformers(top)
                .worstPerformers(worst)
                .build();
    }
}
