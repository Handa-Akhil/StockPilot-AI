package com.stockpilot.api.stock.controller;

import com.stockpilot.api.stock.dto.StockCandleResponse;
import com.stockpilot.api.stock.dto.StockQuoteResponse;
import com.stockpilot.api.stock.dto.StockSearchResponse;
import com.stockpilot.api.stock.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Purpose: REST Controller exposing stock market endpoints to the frontend.
 * Responsibilities: Handles incoming stock search, quote details, and historical data queries.
 * Dependencies: StockService.
 * Flow: Receives request, calls service, returns ResponseEntity containing requested DTOs.
 */
@RestController
@RequestMapping("/api/v1/stocks")
public class StockController {
    private static final Logger log = LoggerFactory.getLogger(StockController.class);

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<StockSearchResponse>> searchStocks(@RequestParam String query) {
        log.info("Request received: GET /api/v1/stocks/search?query={}", query);
        List<StockSearchResponse> results = stockService.searchStocks(query);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<StockQuoteResponse> getQuote(@PathVariable String symbol) {
        log.info("Request received: GET /api/v1/stocks/{}", symbol);
        StockQuoteResponse quote = stockService.getQuote(symbol);
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/{symbol}/history")
    public ResponseEntity<List<StockCandleResponse>> getHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1mo") String range) {
        log.info("Request received: GET /api/v1/stocks/{}/history?range={}", symbol, range);
        List<StockCandleResponse> history = stockService.getHistory(symbol, range);
        return ResponseEntity.ok(history);
    }
}
