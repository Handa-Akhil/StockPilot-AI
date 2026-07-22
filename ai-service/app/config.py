import os
import logging
from typing import List

# Setup structured logging
logging.basicConfig(
    level=logging.INFO,
    format='{"timestamp": "%(asctime)s", "level": "%(levelname)s", "logger": "%(name)s", "message": "%(message)s"}',
    datefmt='%Y-%m-%d %H:%M:%S'
)
logger = logging.getLogger("stockpilot-market-service")

class Settings:
    APP_NAME: str = "StockPilot Market Data Service"
    APP_ENV: str = os.getenv("APP_ENV", "development")
    
    # Server configuration
    HOST: str = os.getenv("AI_SERVICE_HOST", "0.0.0.0")
    PORT: int = int(os.getenv("AI_SERVICE_PORT", "8000"))
    
    # CORS Configuration
    CORS_ORIGINS: List[str] = ["*"]
    
    # Market Data Provider
    MARKET_PROVIDER: str = os.getenv("MARKET_PROVIDER", "yahoo")

settings = Settings()
