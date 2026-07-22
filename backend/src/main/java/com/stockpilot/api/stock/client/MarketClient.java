package com.stockpilot.api.stock.client;

import com.stockpilot.api.common.exception.ProviderUnavailableException;
import com.stockpilot.api.stock.dto.StockCandleResponse;
import com.stockpilot.api.stock.dto.StockQuoteResponse;
import com.stockpilot.api.stock.dto.StockSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Purpose: Dedicated client for HTTP communications with FastAPI Market Data Service.
 * Responsibilities: Performs timeout management, retry execution, and responses mapping.
 * Dependencies: RestClient, application.yml configurations.
 * Flow: Receives request, initiates request factory, attempts query up to retry limit.
 */
@Component
public class MarketClient {
    private static final Logger log = LoggerFactory.getLogger(MarketClient.class);

    private final RestClient restClient;
    private final int retryCount;

    public MarketClient(
            @Value("${stockpilot.market-service.url}") String baseUrl,
            @Value("${stockpilot.market-service.connectTimeoutMs}") int connectTimeout,
            @Value("${stockpilot.market-service.readTimeoutMs}") int readTimeout,
            @Value("${stockpilot.market-service.retryCount}") int retryCount) {
        
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.retryCount = retryCount;
    }

    public List<StockSearchResponse> searchStocks(String query) {
        return executeWithRetry(() -> {
            long start = System.currentTimeMillis();
            List<StockSearchResponse> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/ai/market/search")
                            .queryParam("query", query)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StockSearchResponse>>() {});
            long duration = System.currentTimeMillis() - start;
            log.info("GET /ai/market/search - Success - Latency: {}ms", duration);
            return response;
        }, "searchStocks");
    }

    public StockQuoteResponse getQuote(String symbol) {
        return executeWithRetry(() -> {
            long start = System.currentTimeMillis();
            StockQuoteResponse response = restClient.get()
                    .uri("/ai/market/quote/{symbol}", symbol)
                    .retrieve()
                    .body(StockQuoteResponse.class);
            long duration = System.currentTimeMillis() - start;
            log.info("GET /ai/market/quote/{} - Success - Latency: {}ms", symbol, duration);
            return response;
        }, "getQuote");
    }

    public List<StockCandleResponse> getHistory(String symbol, String range) {
        return executeWithRetry(() -> {
            long start = System.currentTimeMillis();
            List<StockCandleResponse> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/ai/market/history/{symbol}")
                            .queryParam("range", range)
                            .build(symbol))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StockCandleResponse>>() {});
            long duration = System.currentTimeMillis() - start;
            log.info("GET /ai/market/history/{} - Success - Latency: {}ms", symbol, duration);
            return response;
        }, "getHistory");
    }

    private <T> T executeWithRetry(ApiCall<T> call, String actionName) {
        int attempts = 0;
        Exception lastException = null;
        while (attempts < retryCount) {
            try {
                return call.execute();
            } catch (Exception e) {
                attempts++;
                lastException = e;
                log.warn("MarketClient {} failed (attempt {}/{}). Reason: {}", 
                        actionName, attempts, retryCount, e.getMessage());
                if (attempts < retryCount) {
                    try {
                        Thread.sleep(100); // Backoff before retrying
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ProviderUnavailableException("Retry backoff interrupted", ie);
                    }
                }
            }
        }
        throw new ProviderUnavailableException("Market service provider is currently unavailable after " 
                + retryCount + " attempts. Last error: " + lastException.getMessage(), lastException);
    }

    @FunctionalInterface
    private interface ApiCall<T> {
        T execute() throws Exception;
    }
}
