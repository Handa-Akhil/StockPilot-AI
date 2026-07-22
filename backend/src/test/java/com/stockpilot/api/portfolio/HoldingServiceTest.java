package com.stockpilot.api.portfolio;

import com.stockpilot.api.auth.User;
import com.stockpilot.api.portfolio.dto.TransactionRequest;
import com.stockpilot.api.portfolio.exception.InsufficientSharesException;
import com.stockpilot.api.portfolio.model.*;
import com.stockpilot.api.portfolio.repository.HoldingRepository;
import com.stockpilot.api.portfolio.service.HoldingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HoldingServiceTest {

    @Mock
    private HoldingRepository holdingRepository;

    @InjectMocks
    private HoldingServiceImpl holdingService;

    private User mockUser;
    private Portfolio mockPortfolio;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).email("test@example.com").build();
        mockPortfolio = Portfolio.builder().id(100L).user(mockUser).build();
    }

    @Test
    void buy_NewHolding_CreatesSuccessfully() {
        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .transactionType(TransactionType.BUY)
                .quantity(BigDecimal.valueOf(10))
                .price(BigDecimal.valueOf(150.0))
                .build();

        when(holdingRepository.findByPortfolioIdAndSymbolAndUserId(100L, "AAPL", 1L))
                .thenReturn(Optional.empty());
        when(holdingRepository.save(any(Holding.class))).thenAnswer(inv -> inv.getArgument(0));

        Holding result = holdingService.updateHoldingForBuy(mockPortfolio, request, 1L);

        assertNotNull(result);
        assertEquals("AAPL", result.getSymbol());
        assertEquals(0, result.getQuantity().compareTo(BigDecimal.valueOf(10)));
        assertEquals(0, result.getAverageBuyPrice().compareTo(BigDecimal.valueOf(150.0)));
        verify(holdingRepository, times(1)).save(any(Holding.class));
    }

    @Test
    void buy_ExistingHolding_RecalculatesAveragePrice() {
        Holding existing = Holding.builder()
                .portfolio(mockPortfolio)
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .quantity(BigDecimal.valueOf(10))
                .averageBuyPrice(BigDecimal.valueOf(150.0)) // cost = 1500
                .build();

        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .transactionType(TransactionType.BUY)
                .quantity(BigDecimal.valueOf(5))
                .price(BigDecimal.valueOf(180.0)) // cost = 900, totalCost = 2400, totalQty = 15, avg = 160
                .build();

        when(holdingRepository.findByPortfolioIdAndSymbolAndUserId(100L, "AAPL", 1L))
                .thenReturn(Optional.of(existing));
        when(holdingRepository.save(any(Holding.class))).thenAnswer(inv -> inv.getArgument(0));

        Holding result = holdingService.updateHoldingForBuy(mockPortfolio, request, 1L);

        assertNotNull(result);
        assertEquals(0, result.getQuantity().compareTo(BigDecimal.valueOf(15)));
        assertEquals(0, result.getAverageBuyPrice().compareTo(BigDecimal.valueOf(160.0)));
        verify(holdingRepository, times(1)).save(existing);
    }

    @Test
    void sell_PartialShares_UpdatesHolding() {
        Holding existing = Holding.builder()
                .portfolio(mockPortfolio)
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .quantity(BigDecimal.valueOf(10))
                .averageBuyPrice(BigDecimal.valueOf(150.0))
                .build();

        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .transactionType(TransactionType.SELL)
                .quantity(BigDecimal.valueOf(4))
                .price(BigDecimal.valueOf(180.0)) // profit = (180 - 150) * 4 = 120
                .build();

        when(holdingRepository.findByPortfolioIdAndSymbolAndUserId(100L, "AAPL", 1L))
                .thenReturn(Optional.of(existing));
        when(holdingRepository.save(any(Holding.class))).thenAnswer(inv -> inv.getArgument(0));

        BigDecimal realizedPl = holdingService.updateHoldingForSell(mockPortfolio, request, 1L);

        assertEquals(0, realizedPl.compareTo(BigDecimal.valueOf(120.0)));
        assertEquals(0, existing.getQuantity().compareTo(BigDecimal.valueOf(6)));
        verify(holdingRepository, times(1)).save(existing);
        verify(holdingRepository, never()).delete(any());
    }

    @Test
    void sell_AllShares_DeletesHolding() {
        Holding existing = Holding.builder()
                .portfolio(mockPortfolio)
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .quantity(BigDecimal.valueOf(10))
                .averageBuyPrice(BigDecimal.valueOf(150.0))
                .build();

        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .transactionType(TransactionType.SELL)
                .quantity(BigDecimal.valueOf(10))
                .price(BigDecimal.valueOf(180.0))
                .build();

        when(holdingRepository.findByPortfolioIdAndSymbolAndUserId(100L, "AAPL", 1L))
                .thenReturn(Optional.of(existing));

        BigDecimal realizedPl = holdingService.updateHoldingForSell(mockPortfolio, request, 1L);

        assertEquals(0, realizedPl.compareTo(BigDecimal.valueOf(300.0)));
        verify(holdingRepository, times(1)).delete(existing);
        verify(holdingRepository, never()).save(any());
    }

    @Test
    void sell_TooManyShares_ThrowsException() {
        Holding existing = Holding.builder()
                .portfolio(mockPortfolio)
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .quantity(BigDecimal.valueOf(10))
                .averageBuyPrice(BigDecimal.valueOf(150.0))
                .build();

        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .transactionType(TransactionType.SELL)
                .quantity(BigDecimal.valueOf(12))
                .price(BigDecimal.valueOf(180.0))
                .build();

        when(holdingRepository.findByPortfolioIdAndSymbolAndUserId(100L, "AAPL", 1L))
                .thenReturn(Optional.of(existing));

        assertThrows(InsufficientSharesException.class, 
                () -> holdingService.updateHoldingForSell(mockPortfolio, request, 1L));
        verify(holdingRepository, never()).save(any());
        verify(holdingRepository, never()).delete(any());
    }
}
