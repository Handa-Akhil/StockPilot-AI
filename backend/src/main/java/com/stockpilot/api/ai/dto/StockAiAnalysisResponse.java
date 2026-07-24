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
public class StockAiAnalysisResponse {
    private String symbol;
    private String name;
    private String aiSummary;
    private String technicalOutlook;
    private String fundamentalOutlook;
    private List<String> riskFactors;
    private List<String> keyInsights;
    private String suggestedAction;
    private Double confidence;
    // Enhanced Gemini Insights
    private String businessSummary;
    private List<String> bullishFactors;
    private List<String> bearishFactors;
    private String technicalInterpretation;
    private String fundamentalInterpretation;
    private String confidenceExplanation;
}
