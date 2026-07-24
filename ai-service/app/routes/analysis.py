from fastapi import APIRouter, Query, Path, Body
from typing import List, Optional
from app.schemas.insights import (
    PortfolioAiAnalysisResponse,
    StockAiAnalysisResponse,
    MarketSentimentResponse
)
from app.services.insights import AiInsightEngine

router = APIRouter(prefix="/ai/analysis", tags=["analysis"])
insight_engine = AiInsightEngine()

@router.post("/portfolio/{portfolio_id}", response_model=PortfolioAiAnalysisResponse)
def analyze_portfolio(
    portfolio_id: int = Path(..., description="Portfolio ID to analyze"),
    holdings: List[dict] = Body([], description="List of current portfolio holdings")
):
    return insight_engine.analyze_portfolio(portfolio_id, holdings)

@router.get("/stock/{symbol}", response_model=StockAiAnalysisResponse)
def analyze_stock(
    symbol: str = Path(..., description="Stock symbol to analyze")
):
    return insight_engine.analyze_stock(symbol)

@router.get("/sentiment", response_model=MarketSentimentResponse)
def get_market_sentiment():
    return insight_engine.get_market_sentiment()
