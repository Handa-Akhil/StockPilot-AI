package com.stockpilot.api.market.service;

import com.stockpilot.api.market.dto.*;

import java.util.List;

public interface MarketDataService {
    List<StockSearchResultDto> searchStocks(String query);
    StockQuoteDto getQuote(String symbol);
    CompanyProfileDto getProfile(String symbol);
    HistoricalDataResponse getHistory(String symbol, String timeframe);
    List<TrendingStockDto> getTrending();
    List<MarketMoverDto> getGainers();
    List<MarketMoverDto> getLosers();
    List<MarketMoverDto> getMostActive();
    List<MarketIndexDto> getIndices();
}
