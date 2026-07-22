package com.stockpilot.api.stock.service;

import com.stockpilot.api.stock.dto.StockCandleResponse;
import com.stockpilot.api.stock.dto.StockQuoteResponse;
import com.stockpilot.api.stock.dto.StockSearchResponse;

import java.util.List;

public interface StockService {
    List<StockSearchResponse> searchStocks(String query);
    StockQuoteResponse getQuote(String symbol);
    List<StockCandleResponse> getHistory(String symbol, String range);
}
