package com.stockpilot.api.stock;

import com.stockpilot.api.common.exception.InvalidSymbolException;
import com.stockpilot.api.common.exception.ProviderUnavailableException;
import com.stockpilot.api.stock.client.MarketClient;
import com.stockpilot.api.stock.dto.StockCandleResponse;
import com.stockpilot.api.stock.dto.StockQuoteResponse;
import com.stockpilot.api.stock.dto.StockSearchResponse;
import com.stockpilot.api.stock.service.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StockServiceTest {

    private StockServiceImpl stockService;

    @Mock
    private MarketClient marketClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        stockService = new StockServiceImpl(marketClient);
    }

    @Test
    public void testSearchStocks_Success() {
        List<StockSearchResponse> mockResults = List.of(
                new StockSearchResponse("AAPL", "Apple Inc.", "NMS", "EQUITY")
        );
        when(marketClient.searchStocks("Apple")).thenReturn(mockResults);

        List<StockSearchResponse> results = stockService.searchStocks("Apple");
        assertEquals(1, results.size());
        assertEquals("AAPL", results.get(0).getSymbol());
    }

    @Test
    public void testSearchStocks_InvalidBlankQuery() {
        assertThrows(IllegalArgumentException.class, () -> stockService.searchStocks("   "));
        assertThrows(IllegalArgumentException.class, () -> stockService.searchStocks(null));
    }

    @Test
    public void testGetQuote_Success() {
        StockQuoteResponse mockQuote = StockQuoteResponse.builder()
                .symbol("AAPL")
                .name("Apple Inc.")
                .price(180.50)
                .build();
        when(marketClient.getQuote("AAPL")).thenReturn(mockQuote);

        StockQuoteResponse quote = stockService.getQuote("AAPL");
        assertNotNull(quote);
        assertEquals("AAPL", quote.getSymbol());
        assertEquals(180.50, quote.getPrice());
    }

    @Test
    public void testGetQuote_BlankSymbol() {
        assertThrows(IllegalArgumentException.class, () -> stockService.getQuote(""));
        assertThrows(IllegalArgumentException.class, () -> stockService.getQuote(null));
    }

    @Test
    public void testGetQuote_InvalidCharacters() {
        assertThrows(IllegalArgumentException.class, () -> stockService.getQuote("AAPL$"));
        assertThrows(IllegalArgumentException.class, () -> stockService.getQuote("AAPL;SELECT"));
    }

    @Test
    public void testGetQuote_TooLongSymbol() {
        assertThrows(IllegalArgumentException.class, () -> stockService.getQuote("VERYLONGSYMBOLNAME123"));
    }

    @Test
    public void testGetQuote_ProviderUnavailable() {
        when(marketClient.getQuote("AAPL")).thenThrow(new ProviderUnavailableException("Server down"));
        assertThrows(ProviderUnavailableException.class, () -> stockService.getQuote("AAPL"));
    }

    @Test
    public void testGetQuote_SymbolNotFound() {
        when(marketClient.getQuote("INVALID")).thenThrow(new RuntimeException("Could not retrieve ticker pricing"));
        assertThrows(InvalidSymbolException.class, () -> stockService.getQuote("INVALID"));
    }

    @Test
    public void testGetHistory_Success() {
        List<StockCandleResponse> mockCandles = List.of(
                new StockCandleResponse("2026-07-22", 180.0, 182.0, 179.0, 181.0, 1000L)
        );
        when(marketClient.getHistory("AAPL", "1mo")).thenReturn(mockCandles);

        List<StockCandleResponse> history = stockService.getHistory("AAPL", "1mo");
        assertEquals(1, history.size());
        assertEquals("2026-07-22", history.get(0).getDate());
    }

    @Test
    public void testGetHistory_InvalidRange() {
        assertThrows(IllegalArgumentException.class, () -> stockService.getHistory("AAPL", "10y"));
        assertThrows(IllegalArgumentException.class, () -> stockService.getHistory("AAPL", "invalid"));
    }
}
