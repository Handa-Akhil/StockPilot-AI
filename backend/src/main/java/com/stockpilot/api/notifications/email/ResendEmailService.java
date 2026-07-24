package com.stockpilot.api.notifications.email;

import com.stockpilot.api.alerts.model.PriceAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResendEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final EmailTemplateService emailTemplateService;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;

    @Value("${resend.api.key:${RESEND_API_KEY:}}")
    private String resendApiKey;

    @Value("${email.from:${EMAIL_FROM:StockPilot AI <onboarding@resend.dev>}}")
    private String emailFrom;

    @Value("${email.reply.to:${EMAIL_REPLY_TO:support@stockpilot.ai}}")
    private String emailReplyTo;

    public ResendEmailService(
            EmailTemplateService emailTemplateService,
            StringRedisTemplate redisTemplate,
            RestTemplate restTemplate) {
        this.emailTemplateService = emailTemplateService;
        this.redisTemplate = redisTemplate;
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isConfigured() {
        return resendApiKey != null &&
               !resendApiKey.isBlank() &&
               !resendApiKey.contains("YOUR_RESEND_KEY_HERE");
    }

    @Override
    public boolean sendPriceAlertEmail(String recipientEmail, String recipientName, PriceAlert alert, double currentPrice, String aiExplanation) {
        if (alert == null || alert.getId() == null) {
            log.warn("Cannot send price alert email: invalid alert parameter.");
            return false;
        }

        // Redis Deduplication Check (Key: email-alert:{alertId}, TTL: 300s)
        String redisKey = "email-alert:" + alert.getId();
        try {
            Boolean hasKey = redisTemplate.hasKey(redisKey);
            if (Boolean.TRUE.equals(hasKey)) {
                log.info("[Redis Cache HIT] Duplicate email suppressed for alert id={}", alert.getId());
                return false;
            }
        } catch (Exception e) {
            log.warn("Redis check failed in email service: {}. Proceeding to attempt send.", e.getMessage());
        }

        if (!isConfigured()) {
            log.info("RESEND_API_KEY is not configured. Skipping email dispatch for alert id={}", alert.getId());
            return false;
        }

        String subject = String.format("StockPilot AI • Price Alert Triggered for %s", alert.getSymbol());
        String htmlBody = emailTemplateService.buildPriceAlertHtml(recipientName, alert, currentPrice, aiExplanation);

        // Prepare Resend API payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("from", emailFrom);
        payload.put("to", List.of(recipientEmail));
        payload.put("reply_to", emailReplyTo);
        payload.put("subject", subject);
        payload.put("html", htmlBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey.trim());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        // Exponential backoff retry logic (Up to 3 attempts: 1s, 2s, 4s)
        int maxRetries = 3;
        long backoffDelayMs = 1000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Sending price alert email via Resend API to {} (Attempt {}/{})...", recipientEmail, attempt, maxRetries);
                var response = restTemplate.postForEntity(RESEND_API_URL, requestEntity, Map.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Successfully sent email via Resend API to {} for alert id={}. Resend ID: {}",
                            recipientEmail, alert.getId(), response.getBody() != null ? response.getBody().get("id") : "N/A");

                    // Set Redis deduplication key for 5 minutes (300s)
                    try {
                        redisTemplate.opsForValue().set(redisKey, "sent", Duration.ofMinutes(5));
                    } catch (Exception e) {
                        log.warn("Failed to record email deduplication key in Redis: {}", e.getMessage());
                    }
                    return true;
                }
            } catch (Exception e) {
                log.error("Failed email dispatch attempt {}/{} for alert id={}: {}", attempt, maxRetries, alert.getId(), e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(backoffDelayMs);
                        backoffDelayMs *= 2; // Exponential backoff multiplier
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("All {} attempts to send email via Resend for alert id={} failed. Continuing background job without breaking.", maxRetries, alert.getId());
        return false;
    }
}
