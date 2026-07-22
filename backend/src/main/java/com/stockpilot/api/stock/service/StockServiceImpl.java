package com.stockpilot.api.stock.service;

import com.stockpilot.api.common.exception.InvalidSymbolException;
import com.stockpilot.api.stock.client.MarketClient;
import com.stockpilot.api.stock.dto.StockCandleResponse;
import com.stockpilot.api.stock.dto.StockQuoteResponse;
import com.stockpilot.api.stock.dto.StockSearchResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Purpose: Business logic implementation for stock market data operations.
 * Responsibilities: Performs request parameter validation and coordinates fetching from MarketClient.
 * Dependencies: MarketClient.
 * Flow: Validates inputs, forwards calls to client, throws validation/format anomalies.
 */
@Service
public class StockServiceImpl implements StockService {

    private final MarketClient marketClient;
    private static final Set<String> SUPPORTED_RANGES = Set.of("1d", "5d", "1mo", "6mo", "1y");

    public StockServiceImpl(MarketClient marketClient) {
        this.marketClient = marketClient;
    }

    @Override
    public List<StockSearchResponse> searchStocks(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query parameter cannot be blank");
        }
        return marketClient.searchStocks(query.trim());
    }

    @Override
    public StockQuoteResponse getQuote(String symbol) {
        validateSymbol(symbol);
        try {
            StockQuoteResponse quote = marketClient.getQuote(symbol.trim().toUpperCase());
            if (quote == null || quote.getPrice() == null) {
                throw new InvalidSymbolException("Symbol not found or pricing unavailable: " + symbol);
            }
            return quote;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Could not retrieve ticker pricing")) {
                throw new InvalidSymbolException("Symbol not found or pricing unavailable: " + symbol);
            }
            throw e;
        }
    }

    @Override
    public List<StockCandleResponse> getHistory(String symbol, String range) {
        validateSymbol(symbol);
        if (range == null || !SUPPORTED_RANGES.contains(range.trim().toLowerCase())) {
            throw new IllegalArgumentException("Invalid range parameter. Supported ranges are: " + SUPPORTED_RANGES);
        }
        try {
            return marketClient.getHistory(symbol.trim().toUpperCase(), range.trim().toLowerCase());
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("No historical chart data found")) {
                throw new InvalidSymbolException("No historical chart data found for symbol: " + symbol);
            }
            throw e;
        }
    }

    private void validateSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol parameter cannot be blank");
        }
        String cleanSymbol = symbol.trim();
        // Regex: allows letters, numbers, dots, and hyphens (standard exchange symbols, e.g. TCS.NS, AAPL)
        if (!cleanSymbol.matches("^[A-Za-z0-9.\\-]+$")) {
            throw new IllegalArgumentException("Symbol contains invalid characters: " + symbol);
        }
        if (cleanSymbol.length() > 15) {
            throw new IllegalArgumentException("Symbol is too long (max 15 characters): " + symbol);
        }
    }
}
