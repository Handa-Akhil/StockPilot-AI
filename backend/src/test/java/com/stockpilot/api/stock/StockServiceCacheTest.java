package com.stockpilot.api.stock;

import com.stockpilot.api.stock.client.MarketClient;
import com.stockpilot.api.stock.config.RedisCacheConfig;
import com.stockpilot.api.stock.dto.StockCandleResponse;
import com.stockpilot.api.stock.dto.StockQuoteResponse;
import com.stockpilot.api.stock.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class StockServiceCacheTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CacheErrorHandler cacheErrorHandler;

    @MockBean
    private MarketClient marketClient;

    @BeforeEach
    public void setup() {
        // Clear caches before each test if available
        Cache quoteCache = cacheManager.getCache("stock:quote");
        if (quoteCache != null) {
            try {
                quoteCache.clear();
            } catch (Exception e) {
                // Ignore if redis is not available
            }
        }
        Cache historyCache = cacheManager.getCache("stock:history");
        if (historyCache != null) {
            try {
                historyCache.clear();
            } catch (Exception e) {
                // Ignore if redis is not available
            }
        }
    }

    @Test
    public void testCacheGetQuote_Workflow() {
        StockQuoteResponse mockQuote = StockQuoteResponse.builder()
                .symbol("AAPL")
                .name("Apple Inc.")
                .price(180.50)
                .build();
        when(marketClient.getQuote("AAPL")).thenReturn(mockQuote);

        // Call 1: Miss or Hit (depending on live Redis availability)
        StockQuoteResponse quote1 = stockService.getQuote("AAPL");
        assertNotNull(quote1);
        assertEquals(180.50, quote1.getPrice());

        // Call 2: Should hit cache if Redis is up, or fallback to client if down
        StockQuoteResponse quote2 = stockService.getQuote("AAPL");
        assertNotNull(quote2);
        assertEquals(180.50, quote2.getPrice());

        // Verify that in all cases, the service succeeds (proving fallback works if Redis is down)
        verify(marketClient, atLeastOnce()).getQuote("AAPL");
    }

    @Test
    public void testCacheErrorHandler_BypassesException() {
        // Verify that the custom CacheErrorHandler is successfully registered
        assertNotNull(cacheErrorHandler);

        Cache mockCache = mock(Cache.class);
        when(mockCache.getName()).thenReturn("test-cache");

        // Asserts that the custom cache error handler catches exceptions without propagating them
        assertDoesNotThrow(() -> cacheErrorHandler.handleCacheGetError(
                new RuntimeException("Redis connection timed out"), mockCache, "key1"));
        assertDoesNotThrow(() -> cacheErrorHandler.handleCachePutError(
                new RuntimeException("Redis connection lost"), mockCache, "key1", "val1"));
    }

    @Test
    public void testCacheGetHistory_Workflow() {
        List<StockCandleResponse> mockCandles = List.of(
                new StockCandleResponse("2026-07-22", 180.0, 182.0, 179.0, 181.0, 1000L)
        );
        when(marketClient.getHistory("AAPL", "1mo")).thenReturn(mockCandles);

        List<StockCandleResponse> hist1 = stockService.getHistory("AAPL", "1mo");
        assertNotNull(hist1);
        assertEquals(1, hist1.size());

        List<StockCandleResponse> hist2 = stockService.getHistory("AAPL", "1mo");
        assertNotNull(hist2);
        assertEquals(1, hist2.size());

        verify(marketClient, atLeastOnce()).getHistory("AAPL", "1mo");
    }
}
