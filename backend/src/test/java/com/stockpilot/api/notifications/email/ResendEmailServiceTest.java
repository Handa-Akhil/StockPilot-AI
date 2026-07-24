package com.stockpilot.api.notifications.email;

import com.stockpilot.api.alerts.model.AlertCondition;
import com.stockpilot.api.alerts.model.PriceAlert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResendEmailServiceTest {

    private ResendEmailService emailService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        emailService = new ResendEmailService(emailTemplateService, redisTemplate, restTemplate);
    }

    @Test
    @DisplayName("Should detect when Resend API Key is not configured")
    void testIsConfiguredFalseWhenEmpty() {
        assertFalse(emailService.isConfigured());
    }

    @Test
    @DisplayName("Should suppress duplicate email when Redis key exists")
    void testDuplicateEmailSuppression() {
        when(redisTemplate.hasKey("email-alert:100")).thenReturn(true);

        PriceAlert alert = PriceAlert.builder()
                .id(100L)
                .symbol("AAPL")
                .condition(AlertCondition.PRICE_ABOVE)
                .targetPrice(new BigDecimal("250.00"))
                .build();

        boolean result = emailService.sendPriceAlertEmail("user@example.com", "John Doe", alert, 255.00, "Bullish earnings momentum.");
        assertFalse(result, "Email should be suppressed when Redis key exists");
        verify(redisTemplate, times(1)).hasKey("email-alert:100");
    }
}
