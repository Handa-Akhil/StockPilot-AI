package com.stockpilot.api.market.service;

import com.stockpilot.api.market.client.MarketDataClient;
import com.stockpilot.api.market.dto.*;
import com.stockpilot.api.market.exception.InvalidSymbolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketDataServiceImpl implements MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataServiceImpl.class);

    private final MarketDataClient marketDataClient;

    public MarketDataServiceImpl(MarketDataClient marketDataClient) {
        this.marketDataClient = marketDataClient;
    }

    @Override
    @Cacheable(value = "market:search", key = "#query", unless = "#result.isEmpty()")
    public List<StockSearchResultDto> searchStocks(String query) {
        log.info("Executing stock search for query: {}", query);
        return marketDataClient.searchStocks(query);
    }

    @Override
    @Cacheable(value = "market:quote", key = "#symbol.toUpperCase()", unless = "#result == null")
    public StockQuoteDto getQuote(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new InvalidSymbolException("Symbol parameter cannot be empty");
        }
        log.info("Fetching stock quote for symbol: {}", symbol);
        return marketDataClient.getQuote(symbol);
    }

    @Override
    @Cacheable(value = "market:profile", key = "#symbol.toUpperCase()", unless = "#result == null")
    public CompanyProfileDto getProfile(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new InvalidSymbolException("Symbol parameter cannot be empty");
        }
        log.info("Fetching company profile for symbol: {}", symbol);
        return marketDataClient.getProfile(symbol);
    }

    @Override
    @Cacheable(value = "market:history", key = "#symbol.toUpperCase() + ':' + #timeframe", unless = "#result.points.isEmpty()")
    public HistoricalDataResponse getHistory(String symbol, String timeframe) {
        if (symbol == null || symbol.isBlank()) {
            throw new InvalidSymbolException("Symbol parameter cannot be empty");
        }
        log.info("Fetching historical price points for symbol: {} timeframe: {}", symbol, timeframe);
        return marketDataClient.getHistory(symbol, timeframe);
    }

    @Override
    @Cacheable(value = "market:trending", unless = "#result.isEmpty()")
    public List<TrendingStockDto> getTrending() {
        log.info("Fetching trending market equities");
        return marketDataClient.getTrending();
    }

    @Override
    @Cacheable(value = "market:gainers", unless = "#result.isEmpty()")
    public List<MarketMoverDto> getGainers() {
        log.info("Fetching top market gainers");
        return marketDataClient.getGainers();
    }

    @Override
    @Cacheable(value = "market:losers", unless = "#result.isEmpty()")
    public List<MarketMoverDto> getLosers() {
        log.info("Fetching top market losers");
        return marketDataClient.getLosers();
    }

    @Override
    @Cacheable(value = "market:most-active", unless = "#result.isEmpty()")
    public List<MarketMoverDto> getMostActive() {
        log.info("Fetching most active market equities");
        return marketDataClient.getMostActive();
    }

    @Override
    @Cacheable(value = "market:indices", unless = "#result.isEmpty()")
    public List<MarketIndexDto> getIndices() {
        log.info("Fetching global market index benchmarks");
        return marketDataClient.getIndices();
    }
}
