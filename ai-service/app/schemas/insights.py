from pydantic import BaseModel
from typing import List, Optional

class RecommendationDto(BaseModel):
    symbol: str
    action: str  # BUY | HOLD | SELL
    confidence: float  # e.g. 88.5
    explanation: str
    expectedImpact: str
    riskLevel: str  # LOW | MEDIUM | HIGH

class ImprovementSuggestionDto(BaseModel):
    title: str
    category: str
    description: str
    priority: str  # HIGH | MEDIUM | LOW
    actionText: str

class PortfolioAiAnalysisResponse(BaseModel):
    portfolioId: int
    healthScore: int  # 0-100
    overallRiskLevel: str  # LOW | MODERATE | HIGH | AGGRESSIVE
    diversificationScore: int  # 0-100
    confidenceScore: float  # 0-100
    strengths: List[str]
    weaknesses: List[str]
    opportunities: List[str]
    risks: List[str]
    diversificationAnalysis: str
    recommendations: List[RecommendationDto]
    improvementSuggestions: List[ImprovementSuggestionDto]
    # Enhanced Gemini Insights (Optional)
    portfolioSummary: Optional[str] = None
    riskAnalysis: Optional[str] = None
    diversificationAdvice: Optional[str] = None
    longTermOutlook: Optional[str] = None
    shortTermOutlook: Optional[str] = None
    investmentCommentary: Optional[str] = None

class StockAiAnalysisResponse(BaseModel):
    symbol: str
    name: str
    aiSummary: str
    technicalOutlook: str
    fundamentalOutlook: str
    riskFactors: List[str]
    keyInsights: List[str]
    suggestedAction: str
    confidence: float
    # Enhanced Gemini Insights (Optional)
    businessSummary: Optional[str] = None
    bullishFactors: Optional[List[str]] = None
    bearishFactors: Optional[List[str]] = None
    technicalInterpretation: Optional[str] = None
    fundamentalInterpretation: Optional[str] = None
    confidenceExplanation: Optional[str] = None

class NewsSummaryDto(BaseModel):
    title: str
    source: str
    url: str
    publishedAt: str

class MarketSentimentResponse(BaseModel):
    sentiment: str  # BULLISH | NEUTRAL | BEARISH
    confidence: float
    summary: str
    supportingReasons: List[str]
    newsHighlights: List[NewsSummaryDto]
    # Enhanced Gemini Insights (Optional)
    dailyMarketSummary: Optional[str] = None
    sectorRotation: Optional[str] = None
    macroeconomicCommentary: Optional[str] = None
    riskOutlook: Optional[str] = None
    investmentOpportunities: Optional[List[str]] = None
