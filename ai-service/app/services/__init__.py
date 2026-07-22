from app.config import settings
from app.services.base import BaseMarketProvider
from app.services.yahoo import YahooMarketProvider

def get_market_provider() -> BaseMarketProvider:
    if settings.MARKET_PROVIDER == "yahoo":
        return YahooMarketProvider()
    # Default fallback
    return YahooMarketProvider()

market_provider = get_market_provider()
