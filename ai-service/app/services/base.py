from abc import ABC, abstractmethod
from typing import List, Dict, Any

class BaseMarketProvider(ABC):
    
    @abstractmethod
    def search(self, query: str) -> List[Dict[str, Any]]:
        """Search for equity or ETF assets matching a query string."""
        pass

    @abstractmethod
    def get_quote(self, symbol: str) -> Dict[str, Any]:
        """Fetch real-time asset pricing, fundamentals, and relevant news."""
        pass

    @abstractmethod
    def get_history(self, symbol: str, range_val: str) -> List[Dict[str, Any]]:
        """Fetch historical candle intervals (OHLCV) for charting."""
        pass
