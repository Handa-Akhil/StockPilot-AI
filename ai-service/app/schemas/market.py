from pydantic import BaseModel, Field
from typing import List, Optional

class StockSearchResponse(BaseModel):
    symbol: str = Field(..., description="Stock symbol/ticker")
    name: str = Field(..., description="Short or long name of the company")
    exchange: str = Field(..., description="Exchange name")
    type: str = Field(..., description="Security type (e.g., EQUITY, ETF)")

class NewsArticle(BaseModel):
    title: str = Field(..., description="Article headline")
    publisher: str = Field(..., description="News agency/source")
    link: str = Field(..., description="Direct link URL")
    publishTime: int = Field(..., description="Unix timestamp of publication")

class StockQuoteResponse(BaseModel):
    symbol: str
    name: str
    price: float
    change: float
    changePercent: float
    pe: Optional[float] = None
    eps: Optional[float] = None
    marketCap: Optional[float] = None
    volume: int
    high: float
    low: float
    dividendYield: Optional[float] = None
    exchange: str
    currency: str
    news: List[NewsArticle] = []

class StockCandleResponse(BaseModel):
    date: str = Field(..., description="Candle timestamp or date string")
    open: float
    high: float
    low: float
    close: float
    volume: int
