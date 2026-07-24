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
public class PortfolioAiAnalysisResponse {
    private Long portfolioId;
    private Integer healthScore;
    private String overallRiskLevel;
    private Integer diversificationScore;
    private Double confidenceScore;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> opportunities;
    private List<String> risks;
    private String diversificationAnalysis;
    private List<RecommendationDto> recommendations;
    private List<ImprovementSuggestionDto> improvementSuggestions;
    // Enhanced Gemini Insights
    private String portfolioSummary;
    private String riskAnalysis;
    private String diversificationAdvice;
    private String longTermOutlook;
    private String shortTermOutlook;
    private String investmentCommentary;
}
