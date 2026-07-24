from fastapi import APIRouter, Query, Path
from typing import List, Dict, Any
from app.schemas.market import StockSearchResponse, StockQuoteResponse, StockCandleResponse
from app.services import market_provider

router = APIRouter(prefix="/ai/market", tags=["market"])

@router.get("/search", response_model=List[StockSearchResponse])
def search_stocks(query: str = Query(..., description="Stock symbol or name query")):
    return market_provider.search(query)

@router.get("/quote/{symbol}", response_model=StockQuoteResponse)
def get_quote(symbol: str = Path(..., description="Stock symbol to fetch quote for")):
    return market_provider.get_quote(symbol)

@router.get("/profile/{symbol}")
def get_profile(symbol: str = Path(..., description="Stock symbol to fetch company profile for")):
    return market_provider.get_profile(symbol)

@router.get("/history/{symbol}", response_model=List[StockCandleResponse])
def get_history(
    symbol: str = Path(..., description="Stock symbol to fetch history for"),
    range: str = Query("1mo", description="Historical duration, e.g. 1d, 5d, 1mo, 6mo, 1y")
):
    return market_provider.get_history(symbol, range)

@router.get("/trending")
def get_trending():
    return market_provider.get_trending()

@router.get("/indices")
def get_indices():
    return market_provider.get_indices()
