package com.stockpilot.api.alerts.scheduler;

import com.stockpilot.api.ai.client.AiServiceClient;
import com.stockpilot.api.ai.dto.StockAiAnalysisResponse;
import com.stockpilot.api.alerts.model.PriceAlert;
import com.stockpilot.api.alerts.repository.PriceAlertRepository;
import com.stockpilot.api.notifications.dto.NotificationResponse;
import com.stockpilot.api.notifications.email.EmailService;
import com.stockpilot.api.notifications.model.Notification;
import com.stockpilot.api.notifications.repository.NotificationRepository;
import com.stockpilot.api.stock.dto.StockQuoteResponse;
import com.stockpilot.api.stock.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class AlertMonitoringScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertMonitoringScheduler.class);

    private final PriceAlertRepository priceAlertRepository;
    private final NotificationRepository notificationRepository;
    private final StockService stockService;
    private final AiServiceClient aiServiceClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    public AlertMonitoringScheduler(
            PriceAlertRepository priceAlertRepository,
            NotificationRepository notificationRepository,
            StockService stockService,
            AiServiceClient aiServiceClient,
            SimpMessagingTemplate messagingTemplate,
            StringRedisTemplate redisTemplate,
            EmailService emailService) {
        this.priceAlertRepository = priceAlertRepository;
        this.notificationRepository = notificationRepository;
        this.stockService = stockService;
        this.aiServiceClient = aiServiceClient;
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 * * * * *") // Runs every minute
    @Transactional
    public void monitorActivePriceAlerts() {
        log.info("Starting background price alert evaluation job...");
        try {
            List<PriceAlert> activeAlerts = priceAlertRepository.findAllByEnabledTrueAndTriggeredFalse();
            if (activeAlerts.isEmpty()) {
                log.info("No active untriggered price alerts to evaluate.");
                return;
            }

            for (PriceAlert alert : activeAlerts) {
                try {
                    evaluateSingleAlert(alert);
                } catch (Exception ex) {
                    log.error("Failed evaluating alert id={} for symbol {}: {}",
                            alert.getId(), alert.getSymbol(), ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Scheduler error during price alert execution loop: {}", e.getMessage(), e);
        }
    }

    private void evaluateSingleAlert(PriceAlert alert) {
        // Prevent duplicate trigger execution via Redis lock key (5-minute TTL)
        String redisLockKey = "alert:triggered:" + alert.getId();
        Boolean isRecentlyTriggered = redisTemplate.hasKey(redisLockKey);
        if (Boolean.TRUE.equals(isRecentlyTriggered)) {
            return;
        }

        StockQuoteResponse quote = stockService.getQuote(alert.getSymbol());
        if (quote == null || quote.getPrice() == null) {
            return;
        }

        double currentPrice = quote.getPrice();
        double targetVal = alert.getTargetPrice().doubleValue();
        boolean conditionMatched = false;

        switch (alert.getCondition()) {
            case PRICE_ABOVE:
                conditionMatched = currentPrice >= targetVal;
                break;
            case PRICE_BELOW:
                conditionMatched = currentPrice <= targetVal;
                break;
            case PERCENTAGE_CHANGE:
                conditionMatched = Math.abs(quote.getChangePercent()) >= targetVal;
                break;
            case DAILY_GAIN_LOSS:
                conditionMatched = Math.abs(quote.getChange()) >= targetVal;
                break;
            case VOLUME_SPIKE:
                conditionMatched = quote.getVolume() != null && quote.getVolume() >= (long) targetVal;
                break;
        }

        if (conditionMatched) {
            log.info("MATCH! Alert id={} symbol={} condition={} target={} triggered by live price {}",
                    alert.getId(), alert.getSymbol(), alert.getCondition(), targetVal, currentPrice);

            // Mark alert as triggered
            alert.setTriggered(true);
            priceAlertRepository.save(alert);

            // Set Redis lock to prevent duplicate notifications for 5 minutes
            redisTemplate.opsForValue().set(redisLockKey, "true", Duration.ofMinutes(5));

            // Fetch Gemini AI Explanation
            String aiExplanation = null;
            try {
                StockAiAnalysisResponse aiReport = aiServiceClient.analyzeStock(alert.getSymbol());
                if (aiReport != null) {
                    aiExplanation = String.format("%s %s. %s",
                            alert.getSymbol(),
                            aiReport.getSuggestedAction() != null ? "suggests action " + aiReport.getSuggestedAction() : "crossed target threshold",
                            aiReport.getAiSummary() != null ? aiReport.getAiSummary() : "High market volume and buying momentum observed.");
                }
            } catch (Exception ex) {
                log.warn("Could not fetch Gemini AI explanation for alert trigger: {}", ex.getMessage());
                aiExplanation = String.format("%s crossed target %s threshold at $%.2f.",
                        alert.getSymbol(), alert.getCondition(), currentPrice);
            }

            // Construct notification title & message
            String title = String.format("Price Alert: %s %s $%.2f",
                    alert.getSymbol(), alert.getCondition().toString().replace("_", " "), targetVal);

            String message = String.format("%s is currently trading at $%.2f (%s%.2f%%). Your target condition of %s $%.2f was met.",
                    alert.getSymbol(), currentPrice,
                    quote.getChangePercent() >= 0 ? "+" : "", quote.getChangePercent(),
                    alert.getCondition().toString().replace("_", " "), targetVal);

            // Save Notification to Database
            Notification notification = Notification.builder()
                    .user(alert.getUser())
                    .title(title)
                    .message(message)
                    .type("PRICE_ALERT")
                    .readStatus(false)
                    .aiExplanation(aiExplanation)
                    .timestamp(LocalDateTime.now())
                    .build();

            Notification savedNotif = notificationRepository.save(notification);

            // Construct WebSocket response DTO & Send real-time notification if user enabled
            if (alert.getUser().getRealtimeNotificationsEnabled() == null || Boolean.TRUE.equals(alert.getUser().getRealtimeNotificationsEnabled())) {
                NotificationResponse wsDto = NotificationResponse.builder()
                        .id(savedNotif.getId())
                        .title(savedNotif.getTitle())
                        .message(savedNotif.getMessage())
                        .type(savedNotif.getType())
                        .readStatus(savedNotif.getReadStatus())
                        .timestamp(savedNotif.getTimestamp())
                        .aiExplanation(savedNotif.getAiExplanation())
                        .build();

                String userTopic = "/topic/notifications/" + alert.getUser().getId();
                messagingTemplate.convertAndSend(userTopic, wsDto);
                messagingTemplate.convertAndSend("/topic/notifications/broadcast", wsDto);
                log.info("WebSocket STOMP event pushed to {}", userTopic);
            }

            // Dispatch Email Notification using Resend if user enabled email notifications
            if (Boolean.TRUE.equals(alert.getUser().getEmailNotificationsEnabled())) {
                try {
                    emailService.sendPriceAlertEmail(
                            alert.getUser().getEmail(),
                            alert.getUser().getName(),
                            alert,
                            currentPrice,
                            aiExplanation
                    );
                } catch (Exception ex) {
                    log.error("Failed sending price alert email to {}: {}. Proceeding without failing scheduler.",
                            alert.getUser().getEmail(), ex.getMessage());
                }
            }
        }
    }
}
