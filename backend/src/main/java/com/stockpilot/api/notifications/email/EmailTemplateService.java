package com.stockpilot.api.notifications.email;

import com.stockpilot.api.alerts.model.PriceAlert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailTemplateService {

    @Value("${frontend.base.url:http://localhost:3000}")
    private String frontendBaseUrl;

    public String buildPriceAlertHtml(String recipientName, PriceAlert alert, double currentPrice, String aiExplanation) {
        String timestampStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm:ss 'UTC'"));
        String conditionFormatted = alert.getCondition().toString().replace("_", " ");
        String dashboardUrl = frontendBaseUrl + "/dashboard";

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>StockPilot AI Price Alert</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #09090b; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #f4f4f5;">
              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #09090b; padding: 32px 16px;">
                <tr>
                  <td align="center">
                    <table role="presentation" width="100%%" style="max-width: 560px; background-color: #121217; border: 1px solid #27272a; border-radius: 12px; overflow: hidden; padding: 32px;">
                      
                      <!-- HEADER BRANDING -->
                      <tr>
                        <td style="padding-bottom: 24px; border-bottom: 1px solid #27272a;">
                          <div style="display: flex; align-items: center; gap: 8px;">
                            <span style="font-size: 20px; font-weight: 800; color: #818cf8; letter-spacing: -0.02em;">StockPilot AI</span>
                            <span style="font-size: 11px; background-color: rgba(99,102,241,0.15); color: #818cf8; padding: 2px 8px; border-radius: 12px; font-weight: 700;">REAL-TIME ALERT</span>
                          </div>
                        </td>
                      </tr>

                      <!-- TITLE & GREETING -->
                      <tr>
                        <td style="padding-top: 24px; padding-bottom: 16px;">
                          <h1 style="font-size: 20px; font-weight: 700; color: #ffffff; margin: 0 0 8px 0;">
                            Price Alert Triggered: <span style="color: #818cf8;">%s</span>
                          </h1>
                          <p style="font-size: 14px; color: #a1a1aa; margin: 0; line-height: 1.5;">
                            Hello %s, your quantitative rule monitor detected a price condition trigger on your watchlist asset.
                          </p>
                        </td>
                      </tr>

                      <!-- METRIC GRID TABLE -->
                      <tr>
                        <td style="padding-bottom: 24px;">
                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #18181b; border: 1px solid #27272a; border-radius: 8px; padding: 16px;">
                            <tr>
                              <td width="50%%" style="padding: 8px; font-size: 12px; color: #a1a1aa; text-transform: uppercase;">Symbol</td>
                              <td width="50%%" align="right" style="padding: 8px; font-size: 16px; font-weight: 800; color: #818cf8; font-family: monospace;">%s</td>
                            </tr>
                            <tr>
                              <td width="50%%" style="padding: 8px; font-size: 12px; color: #a1a1aa; text-transform: uppercase;">Condition</td>
                              <td width="50%%" align="right" style="padding: 8px; font-size: 13px; font-weight: 700; color: #ffffff;">%s</td>
                            </tr>
                            <tr>
                              <td width="50%%" style="padding: 8px; font-size: 12px; color: #a1a1aa; text-transform: uppercase;">Current Price</td>
                              <td width="50%%" align="right" style="padding: 8px; font-size: 15px; font-weight: 700; color: #10b981; font-family: monospace;">$%.2f</td>
                            </tr>
                            <tr>
                              <td width="50%%" style="padding: 8px; font-size: 12px; color: #a1a1aa; text-transform: uppercase;">Target Threshold</td>
                              <td width="50%%" align="right" style="padding: 8px; font-size: 15px; font-weight: 700; color: #f59e0b; font-family: monospace;">$%.2f</td>
                            </tr>
                            <tr>
                              <td width="50%%" style="padding: 8px; font-size: 12px; color: #a1a1aa; text-transform: uppercase;">Timestamp</td>
                              <td width="50%%" align="right" style="padding: 8px; font-size: 11px; color: #a1a1aa;">%s</td>
                            </tr>
                          </table>
                        </td>
                      </tr>

                      <!-- GEMINI AI EXPLANATION BOX -->
                      %s

                      <!-- CALL TO ACTION BUTTON -->
                      <tr>
                        <td align="center" style="padding-top: 24px; padding-bottom: 24px;">
                          <a href="%s" target="_blank" style="display: inline-block; background-color: #6366f1; color: #ffffff; text-decoration: none; font-size: 14px; font-weight: 700; padding: 12px 28px; border-radius: 8px; box-shadow: 0 4px 14px rgba(99,102,241,0.4);">
                            Open StockPilot Dashboard
                          </a>
                        </td>
                      </tr>

                      <!-- FOOTER -->
                      <tr>
                        <td style="border-top: 1px solid #27272a; padding-top: 16px; font-size: 11px; color: #71717a; text-align: center; line-height: 1.5;">
                          You are receiving this automated alert because email notifications are enabled in your StockPilot AI preferences.<br>
                          © 2026 StockPilot AI Inc. All rights reserved.
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(
                alert.getSymbol(),
                recipientName != null ? recipientName : "Investor",
                alert.getSymbol(),
                conditionFormatted,
                currentPrice,
                alert.getTargetPrice().doubleValue(),
                timestampStr,
                buildAiExplanationHtmlBlock(aiExplanation),
                dashboardUrl
            );
    }

    private String buildAiExplanationHtmlBlock(String aiExplanation) {
        if (aiExplanation == null || aiExplanation.isBlank()) {
            return "";
        }
        return """
            <tr>
              <td style="padding-bottom: 16px;">
                <div style="background-color: rgba(99,102,241,0.08); border: 1px solid rgba(99,102,241,0.25); border-radius: 8px; padding: 16px;">
                  <div style="font-size: 11px; font-weight: 800; color: #818cf8; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 6px;">
                    ✨ Gemini AI Intelligence Explanation
                  </div>
                  <div style="font-size: 13px; color: #e4e4e7; line-height: 1.5;">
                    "%s"
                  </div>
                </div>
              </td>
            </tr>
            """.formatted(aiExplanation);
    }
}
