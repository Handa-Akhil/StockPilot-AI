package com.stockpilot.api.ai.client;

import com.stockpilot.api.ai.dto.MarketSentimentResponse;
import com.stockpilot.api.ai.dto.PortfolioAiAnalysisResponse;
import com.stockpilot.api.ai.dto.StockAiAnalysisResponse;
import com.stockpilot.api.common.exception.ProviderUnavailableException;
import com.stockpilot.api.portfolio.dto.HoldingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class AiServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AiServiceClient(
            RestTemplate restTemplate,
            @Value("${stockpilot.market-service.url:http://localhost:8000}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public PortfolioAiAnalysisResponse analyzePortfolio(Long portfolioId, List<HoldingResponse> holdings) {
        String url = baseUrl + "/ai/analysis/portfolio/" + portfolioId;
        log.info("Requesting portfolio AI analysis from {}", url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<HoldingResponse>> entity = new HttpEntity<>(holdings, headers);

            return restTemplate.postForObject(url, entity, PortfolioAiAnalysisResponse.class);
        } catch (Exception ex) {
            log.error("AI service portfolio analysis call failed: {}", ex.getMessage());
            throw new ProviderUnavailableException("AI Analysis Service unavailable", ex);
        }
    }

    public StockAiAnalysisResponse analyzeStock(String symbol) {
        String url = baseUrl + "/ai/analysis/stock/" + symbol;
        log.info("Requesting stock AI analysis for {}", symbol);
        try {
            return restTemplate.getForObject(url, StockAiAnalysisResponse.class);
        } catch (Exception ex) {
            log.error("AI service stock analysis call failed for {}: {}", symbol, ex.getMessage());
            throw new ProviderUnavailableException("AI Analysis Service unavailable for symbol: " + symbol, ex);
        }
    }

    public MarketSentimentResponse getMarketSentiment() {
        String url = baseUrl + "/ai/analysis/sentiment";
        log.info("Requesting market sentiment AI analysis");
        try {
            return restTemplate.getForObject(url, MarketSentimentResponse.class);
        } catch (Exception ex) {
            log.error("AI service market sentiment call failed: {}", ex.getMessage());
            throw new ProviderUnavailableException("AI Market Sentiment Service unavailable", ex);
        }
    }
}
