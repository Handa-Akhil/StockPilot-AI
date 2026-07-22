package com.stockpilot.api.stock;

import com.stockpilot.api.stock.client.MarketClient;
import com.stockpilot.api.stock.dto.StockCandleResponse;
import com.stockpilot.api.stock.dto.StockQuoteResponse;
import com.stockpilot.api.stock.dto.StockSearchResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StockControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MarketClient marketClient;

    @Test
    @WithMockUser(username = "user@stockpilot.com")
    public void searchStocks_Success() throws Exception {
        List<StockSearchResponse> mockList = List.of(
                new StockSearchResponse("AAPL", "Apple Inc.", "NMS", "EQUITY")
        );
        Mockito.when(marketClient.searchStocks("Apple")).thenReturn(mockList);

        mockMvc.perform(get("/api/v1/stocks/search?query=Apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].name").value("Apple Inc."));
    }

    @Test
    @WithMockUser(username = "user@stockpilot.com")
    public void getQuote_Success() throws Exception {
        StockQuoteResponse mockQuote = StockQuoteResponse.builder()
                .symbol("AAPL")
                .name("Apple Inc.")
                .price(180.50)
                .change(2.5)
                .changePercent(1.4)
                .exchange("NMS")
                .currency("USD")
                .news(List.of())
                .build();
        Mockito.when(marketClient.getQuote("AAPL")).thenReturn(mockQuote);

        mockMvc.perform(get("/api/v1/stocks/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.price").value(180.50))
                .andExpect(jsonPath("$.change").value(2.5));
    }

    @Test
    @WithMockUser(username = "user@stockpilot.com")
    public void getHistory_Success() throws Exception {
        List<StockCandleResponse> mockCandles = List.of(
                new StockCandleResponse("2026-07-22", 180.0, 182.0, 179.0, 181.0, 1000L)
        );
        Mockito.when(marketClient.getHistory("AAPL", "1mo")).thenReturn(mockCandles);

        mockMvc.perform(get("/api/v1/stocks/AAPL/history?range=1mo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-07-22"))
                .andExpect(jsonPath("$[0].close").value(181.0));
    }

    @Test
    @WithMockUser(username = "user@stockpilot.com")
    public void searchStocks_ValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/stocks/search?query="))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.path").value("/api/v1/stocks/search"));
    }

    @Test
    @WithMockUser(username = "user@stockpilot.com")
    public void getQuote_ValidationError_SpecialChars() throws Exception {
        mockMvc.perform(get("/api/v1/stocks/AAPL$"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.path").value("/api/v1/stocks/AAPL$"));
    }

    @Test
    public void getQuote_Unauthorized_NoUser() throws Exception {
        mockMvc.perform(get("/api/v1/stocks/AAPL"))
                .andExpect(status().isUnauthorized());
    }
}
