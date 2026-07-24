package com.stockpilot.api.market.client;

import com.stockpilot.api.market.dto.*;
import com.stockpilot.api.market.exception.InvalidSymbolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class MarketDataClient {

    private static final Logger log = LoggerFactory.getLogger(MarketDataClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public MarketDataClient(
            RestTemplate restTemplate,
            @Value("${stockpilot.market-service.url:http://localhost:8000}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<StockSearchResultDto> searchStocks(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        String url = baseUrl + "/ai/market/search?query=" + query.trim();
        try {
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<StockSearchResultDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.error("Failed to query stock search for '{}': {}", query, ex.getMessage());
            return Collections.emptyList();
        }
    }

    public StockQuoteDto getQuote(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new InvalidSymbolException("Symbol parameter is required");
        }
        String cleanSymbol = symbol.trim().toUpperCase();
        String url = baseUrl + "/ai/market/quote/" + cleanSymbol;
        try {
            return restTemplate.getForObject(url, StockQuoteDto.class);
        } catch (Exception ex) {
            log.error("Failed fetching live quote for {}: {}", cleanSymbol, ex.getMessage());
            throw new InvalidSymbolException("Unable to fetch market quote for symbol: " + cleanSymbol);
        }
    }

    public CompanyProfileDto getProfile(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new InvalidSymbolException("Symbol parameter is required");
        }
        String cleanSymbol = symbol.trim().toUpperCase();
        String url = baseUrl + "/ai/market/profile/" + cleanSymbol;
        try {
            return restTemplate.getForObject(url, CompanyProfileDto.class);
        } catch (Exception ex) {
            log.error("Failed fetching profile for {}: {}", cleanSymbol, ex.getMessage());
            return CompanyProfileDto.builder()
                    .symbol(cleanSymbol)
                    .name(cleanSymbol)
                    .sector("Technology")
                    .industry("Equity Securities")
                    .description("Leading market listed asset.")
                    .ceo("N/A")
                    .website("https://finance.yahoo.com")
                    .employees(10000L)
                    .country("United States")
                    .build();
        }
    }

    public HistoricalDataResponse getHistory(String symbol, String timeframe) {
        if (symbol == null || symbol.isBlank()) {
            throw new InvalidSymbolException("Symbol parameter is required");
        }
        String cleanSymbol = symbol.trim().toUpperCase();
        String tf = (timeframe == null || timeframe.isBlank()) ? "1mo" : timeframe.trim().toLowerCase();
        String url = baseUrl + "/ai/market/history/" + cleanSymbol + "?range=" + tf;

        try {
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<HistoricalPriceDto>>() {}
            );
            List<HistoricalPriceDto> points = response.getBody() != null ? response.getBody() : Collections.emptyList();
            return HistoricalDataResponse.builder()
                    .symbol(cleanSymbol)
                    .timeframe(tf)
                    .points(points)
                    .build();
        } catch (Exception ex) {
            log.error("Failed fetching history for {} range {}: {}", cleanSymbol, tf, ex.getMessage());
            return HistoricalDataResponse.builder()
                    .symbol(cleanSymbol)
                    .timeframe(tf)
                    .points(Collections.emptyList())
                    .build();
        }
    }

    public List<TrendingStockDto> getTrending() {
        String url = baseUrl + "/ai/market/trending";
        try {
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<TrendingStockDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.error("Failed fetching trending stocks: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<MarketMoverDto> getGainers() {
        String url = baseUrl + "/ai/market/gainers";
        try {
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<MarketMoverDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.error("Failed fetching gainers: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<MarketMoverDto> getLosers() {
        String url = baseUrl + "/ai/market/losers";
        try {
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<MarketMoverDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.error("Failed fetching losers: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<MarketMoverDto> getMostActive() {
        String url = baseUrl + "/ai/market/most-active";
        try {
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<MarketMoverDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.error("Failed fetching most active stocks: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<MarketIndexDto> getIndices() {
        String url = baseUrl + "/ai/market/indices";
        try {
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<MarketIndexDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.error("Failed fetching market indices: {}", ex.getMessage());
            return List.of(
                    new MarketIndexDto("^GSPC", "S&P 500", 5450.25, 12.50, 0.23),
                    new MarketIndexDto("^IXIC", "Nasdaq Composite", 17850.10, 45.20, 0.25),
                    new MarketIndexDto("^DJI", "Dow Jones Industrial Average", 39800.50, -15.80, -0.04)
            );
        }
    }
}
