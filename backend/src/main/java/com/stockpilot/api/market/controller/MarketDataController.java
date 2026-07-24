package com.stockpilot.api.market.controller;

import com.stockpilot.api.market.dto.*;
import com.stockpilot.api.market.exception.InvalidSymbolException;
import com.stockpilot.api.market.service.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/market")
public class MarketDataController {

    private static final Logger log = LoggerFactory.getLogger(MarketDataController.class);

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<StockSearchResultDto>> searchStocks(@RequestParam String query) {
        log.info("Request received: GET /api/v1/market/search?query={}", query);
        List<StockSearchResultDto> results = marketDataService.searchStocks(query);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/quote/{symbol}")
    public ResponseEntity<StockQuoteDto> getQuote(@PathVariable String symbol) {
        log.info("Request received: GET /api/v1/market/quote/{}", symbol);
        StockQuoteDto quote = marketDataService.getQuote(symbol);
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/profile/{symbol}")
    public ResponseEntity<CompanyProfileDto> getProfile(@PathVariable String symbol) {
        log.info("Request received: GET /api/v1/market/profile/{}", symbol);
        CompanyProfileDto profile = marketDataService.getProfile(symbol);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/history/{symbol}")
    public ResponseEntity<HistoricalDataResponse> getHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1mo") String timeframe) {
        log.info("Request received: GET /api/v1/market/history/{}?timeframe={}", symbol, timeframe);
        HistoricalDataResponse history = marketDataService.getHistory(symbol, timeframe);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/trending")
    public ResponseEntity<List<TrendingStockDto>> getTrending() {
        log.info("Request received: GET /api/v1/market/trending");
        List<TrendingStockDto> trending = marketDataService.getTrending();
        return ResponseEntity.ok(trending);
    }

    @GetMapping("/indices")
    public ResponseEntity<List<MarketIndexDto>> getIndices() {
        log.info("Request received: GET /api/v1/market/indices");
        List<MarketIndexDto> indices = marketDataService.getIndices();
        return ResponseEntity.ok(indices);
    }

    @ExceptionHandler(InvalidSymbolException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidSymbolException(InvalidSymbolException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
