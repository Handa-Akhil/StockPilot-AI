package com.stockpilot.api.market;

import com.stockpilot.api.market.client.MarketDataClient;
import com.stockpilot.api.market.dto.*;
import com.stockpilot.api.market.exception.InvalidSymbolException;
import com.stockpilot.api.market.service.MarketDataServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarketDataServiceTest {

    private MarketDataServiceImpl marketDataService;

    @Mock
    private MarketDataClient marketDataClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        marketDataService = new MarketDataServiceImpl(marketDataClient);
    }

    @Test
    @DisplayName("Should return stock search results")
    void testSearchStocks() {
        StockSearchResultDto item = StockSearchResultDto.builder()
                .symbol("AAPL")
                .name("Apple Inc.")
                .exchange("NASDAQ")
                .type("EQUITY")
                .build();

        when(marketDataClient.searchStocks("AAPL")).thenReturn(List.of(item));

        List<StockSearchResultDto> result = marketDataService.searchStocks("AAPL");
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        verify(marketDataClient, times(1)).searchStocks("AAPL");
    }

    @Test
    @DisplayName("Should fetch stock quote for valid symbol")
    void testGetQuoteValidSymbol() {
        StockQuoteDto quote = StockQuoteDto.builder()
                .symbol("NVDA")
                .name("NVIDIA Corporation")
                .price(125.50)
                .change(2.50)
                .changePercent(2.03)
                .build();

        when(marketDataClient.getQuote("NVDA")).thenReturn(quote);

        StockQuoteDto result = marketDataService.getQuote("NVDA");
        assertNotNull(result);
        assertEquals("NVDA", result.getSymbol());
        assertEquals(125.50, result.getPrice());
    }

    @Test
    @DisplayName("Should throw InvalidSymbolException when symbol is empty")
    void testGetQuoteEmptySymbolThrowsException() {
        assertThrows(InvalidSymbolException.class, () -> marketDataService.getQuote(""));
    }

    @Test
    @DisplayName("Should fetch market indices")
    void testGetIndices() {
        MarketIndexDto spy = MarketIndexDto.builder()
                .symbol("^GSPC")
                .name("S&P 500")
                .price(5450.25)
                .change(12.50)
                .changePercent(0.23)
                .build();

        when(marketDataClient.getIndices()).thenReturn(List.of(spy));

        List<MarketIndexDto> result = marketDataService.getIndices();
        assertEquals(1, result.size());
        assertEquals("^GSPC", result.get(0).getSymbol());
    }
}
