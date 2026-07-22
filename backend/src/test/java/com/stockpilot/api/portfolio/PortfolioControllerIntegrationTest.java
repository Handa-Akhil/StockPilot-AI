package com.stockpilot.api.portfolio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockpilot.api.auth.security.UserDetailsImpl;
import com.stockpilot.api.portfolio.dto.*;
import com.stockpilot.api.portfolio.model.AssetClass;
import com.stockpilot.api.portfolio.model.TransactionType;
import com.stockpilot.api.portfolio.service.PortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PortfolioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PortfolioService portfolioService;

    @BeforeEach
    public void setup() {
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "user@stockpilot.com", "password", "Test User");
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void createPortfolio_Success() throws Exception {
        PortfolioCreateRequest request = new PortfolioCreateRequest("My Growth");
        PortfolioResponse response = PortfolioResponse.builder()
                .id(100L)
                .name("My Growth")
                .createdAt(LocalDateTime.now())
                .build();

        Mockito.when(portfolioService.createPortfolio(any(PortfolioCreateRequest.class), eq(1L)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.name").value("My Growth"));
    }

    @Test
    public void listPortfolios_Success() throws Exception {
        List<PortfolioResponse> mockList = List.of(
                PortfolioResponse.builder().id(100L).name("P1").build()
        );
        Mockito.when(portfolioService.listPortfolios(1L)).thenReturn(mockList);

        mockMvc.perform(get("/api/v1/portfolios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].name").value("P1"));
    }

    @Test
    public void deletePortfolio_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/portfolios/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Portfolio soft-deleted successfully"));

        Mockito.verify(portfolioService, Mockito.times(1)).softDeletePortfolio(100L, 1L);
    }

    @Test
    public void recordTransaction_Success() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .transactionType(TransactionType.BUY)
                .quantity(BigDecimal.TEN)
                .price(BigDecimal.valueOf(150.0))
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(500L)
                .portfolioId(100L)
                .symbol("AAPL")
                .assetClass(AssetClass.STOCK)
                .transactionType(TransactionType.BUY)
                .quantity(BigDecimal.TEN)
                .price(BigDecimal.valueOf(150.0))
                .transactionTime(LocalDateTime.now())
                .build();

        Mockito.when(portfolioService.recordTransaction(eq(100L), any(TransactionRequest.class), eq(1L)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/portfolios/100/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500L))
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.quantity").value(10));
    }
}
