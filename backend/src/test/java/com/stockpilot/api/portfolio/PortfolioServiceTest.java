package com.stockpilot.api.portfolio;

import com.stockpilot.api.auth.User;
import com.stockpilot.api.auth.UserRepository;
import com.stockpilot.api.portfolio.dto.*;
import com.stockpilot.api.portfolio.exception.PortfolioNotFoundException;
import com.stockpilot.api.portfolio.model.*;
import com.stockpilot.api.portfolio.repository.HoldingRepository;
import com.stockpilot.api.portfolio.repository.PortfolioRepository;
import com.stockpilot.api.portfolio.repository.TransactionRepository;
import com.stockpilot.api.portfolio.service.HoldingServiceImpl;
import com.stockpilot.api.portfolio.service.PortfolioServiceImpl;
import com.stockpilot.api.stock.dto.StockQuoteResponse;
import com.stockpilot.api.stock.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private HoldingRepository holdingRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HoldingServiceImpl holdingService;
    @Mock
    private StockService stockService;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    private User mockUser;
    private Portfolio mockPortfolio;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        mockPortfolio = Portfolio.builder()
                .id(100L)
                .name("Growth Portfolio")
                .user(mockUser)
                .build();
    }

    @Test
    void createPortfolio_Success() {
        PortfolioCreateRequest request = new PortfolioCreateRequest("Growth Portfolio");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(portfolioRepository.findAllActiveByUserId(1L)).thenReturn(Collections.emptyList());
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(mockPortfolio);

        PortfolioResponse response = portfolioService.createPortfolio(request, 1L);

        assertNotNull(response);
        assertEquals("Growth Portfolio", response.getName());
        verify(portfolioRepository, times(1)).save(any(Portfolio.class));
    }

    @Test
    void createPortfolio_DuplicateName_ThrowsException() {
        PortfolioCreateRequest request = new PortfolioCreateRequest("Growth Portfolio");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(portfolioRepository.findAllActiveByUserId(1L)).thenReturn(List.of(mockPortfolio));

        assertThrows(IllegalArgumentException.class, () -> portfolioService.createPortfolio(request, 1L));
        verify(portfolioRepository, never()).save(any(Portfolio.class));
    }

    @Test
    void softDeletePortfolio_Success() {
        when(portfolioRepository.findActiveByIdAndUserId(100L, 1L)).thenReturn(Optional.of(mockPortfolio));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(mockPortfolio);

        portfolioService.softDeletePortfolio(100L, 1L);

        assertNotNull(mockPortfolio.getDeletedAt());
        verify(portfolioRepository, times(1)).save(mockPortfolio);
    }

    @Test
    void softDeletePortfolio_NotFound_ThrowsException() {
        when(portfolioRepository.findActiveByIdAndUserId(100L, 1L)).thenReturn(Optional.empty());

        assertThrows(PortfolioNotFoundException.class, () -> portfolioService.softDeletePortfolio(100L, 1L));
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    void recordBuyTransaction_Success() {
        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .transactionType(TransactionType.BUY)
                .quantity(BigDecimal.TEN)
                .price(BigDecimal.valueOf(150.0))
                .build();

        Transaction savedTx = Transaction.builder()
                .id(500L)
                .portfolio(mockPortfolio)
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .transactionType(TransactionType.BUY)
                .quantity(BigDecimal.TEN)
                .price(BigDecimal.valueOf(150.0))
                .transactionTime(LocalDateTime.now())
                .build();

        when(portfolioRepository.findActiveByIdAndUserId(100L, 1L)).thenReturn(Optional.of(mockPortfolio));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = portfolioService.recordTransaction(100L, request, 1L);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals(TransactionType.BUY, response.getTransactionType());
        assertNull(response.getRealizedPl());
        verify(holdingService, times(1)).updateHoldingForBuy(eq(mockPortfolio), eq(request), eq(1L));
        verify(holdingService, never()).updateHoldingForSell(any(), any(), anyLong());
    }

    @Test
    void recordSellTransaction_Success() {
        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .transactionType(TransactionType.SELL)
                .quantity(BigDecimal.TEN)
                .price(BigDecimal.valueOf(180.0))
                .build();

        Transaction savedTx = Transaction.builder()
                .id(501L)
                .portfolio(mockPortfolio)
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .transactionType(TransactionType.SELL)
                .quantity(BigDecimal.TEN)
                .price(BigDecimal.valueOf(180.0))
                .realizedPl(BigDecimal.valueOf(300.0)) // (180 - 150) * 10
                .transactionTime(LocalDateTime.now())
                .build();

        when(portfolioRepository.findActiveByIdAndUserId(100L, 1L)).thenReturn(Optional.of(mockPortfolio));
        when(holdingService.updateHoldingForSell(eq(mockPortfolio), eq(request), eq(1L))).thenReturn(BigDecimal.valueOf(300.0));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = portfolioService.recordTransaction(100L, request, 1L);

        assertNotNull(response);
        assertEquals(501L, response.getId());
        assertEquals(TransactionType.SELL, response.getTransactionType());
        assertEquals(BigDecimal.valueOf(300.0), response.getRealizedPl());
        verify(holdingService, times(1)).updateHoldingForSell(eq(mockPortfolio), eq(request), eq(1L));
        verify(holdingService, never()).updateHoldingForBuy(any(), any(), anyLong());
    }

    @Test
    void getSummary_Success() {
        Holding mockHolding = Holding.builder()
                .id(1L)
                .portfolio(mockPortfolio)
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .quantity(BigDecimal.TEN)
                .averageBuyPrice(BigDecimal.valueOf(150.0))
                .build();

        StockQuoteResponse quote = new StockQuoteResponse();
        quote.setSymbol("AAPL");
        quote.setPrice(180.0);

        Transaction mockTx = Transaction.builder()
                .realizedPl(BigDecimal.valueOf(50.0))
                .build();

        when(portfolioRepository.findActiveByIdAndUserId(100L, 1L)).thenReturn(Optional.of(mockPortfolio));
        when(holdingRepository.findAllByPortfolioIdAndUserId(100L, 1L)).thenReturn(List.of(mockHolding));
        when(stockService.getQuote("AAPL")).thenReturn(quote);

        Page<Transaction> txPage = new PageImpl<>(List.of(mockTx));
        when(transactionRepository.findAllByPortfolioIdAndUserId(eq(100L), eq(1L), any(Pageable.class))).thenReturn(txPage);

        PortfolioSummaryResponse summary = portfolioService.getSummary(100L, 1L);

        assertNotNull(summary);
        assertEquals("Growth Portfolio", summary.getPortfolioName());
        assertEquals(1, summary.getHoldings().size());

        HoldingResponse hr = summary.getHoldings().getFirst();
        assertEquals("AAPL", hr.getSymbol());
        assertEquals(0, hr.getCostBasis().compareTo(BigDecimal.valueOf(1500.0))); // 10 * 150
        assertEquals(0, hr.getCurrentValue().compareTo(BigDecimal.valueOf(1800.0))); // 10 * 180
        assertEquals(0, hr.getUnrealizedPl().compareTo(BigDecimal.valueOf(300.0)));
        assertEquals(0, hr.getUnrealizedPlPercent().compareTo(BigDecimal.valueOf(20.0)));
        assertEquals(0, hr.getAllocationPercent().compareTo(BigDecimal.valueOf(100.0)));

        PortfolioMetricsResponse metrics = summary.getMetrics();
        assertEquals(0, metrics.getTotalCostBasis().compareTo(BigDecimal.valueOf(1500.0)));
        assertEquals(0, metrics.getCurrentValue().compareTo(BigDecimal.valueOf(1800.0)));
        assertEquals(0, metrics.getUnrealizedPl().compareTo(BigDecimal.valueOf(300.0)));
        assertEquals(0, metrics.getUnrealizedPlPercent().compareTo(BigDecimal.valueOf(20.0)));
        assertEquals(0, metrics.getRealizedPl().compareTo(BigDecimal.valueOf(50.0)));
    }
}
