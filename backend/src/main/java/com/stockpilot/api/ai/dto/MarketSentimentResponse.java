package com.stockpilot.api.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketSentimentResponse {
    private String sentiment; // BULLISH | NEUTRAL | BEARISH
    private Double confidence;
    private String summary;
    private List<String> supportingReasons;
    private List<NewsSummaryItem> newsHighlights;
    // Enhanced Gemini Insights
    private String dailyMarketSummary;
    private String sectorRotation;
    private String macroeconomicCommentary;
    private String riskOutlook;
    private List<String> investmentOpportunities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsSummaryItem {
        private String title;
        private String source;
        private String url;
        private String publishedAt;
    }
}
