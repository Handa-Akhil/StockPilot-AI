package com.stockpilot.api.notifications.email;

import com.stockpilot.api.alerts.model.PriceAlert;

public interface EmailService {
    boolean sendPriceAlertEmail(String recipientEmail, String recipientName, PriceAlert alert, double currentPrice, String aiExplanation);
    boolean isConfigured();
}
