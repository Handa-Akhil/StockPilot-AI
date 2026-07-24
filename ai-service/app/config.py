import os
import logging
from typing import List
from dotenv import load_dotenv

# Load environment variables from .env if present
load_dotenv()

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

    # Gemini AI Configuration
    GEMINI_API_KEY: str = os.getenv("GEMINI_API_KEY", os.getenv("AI_API_KEY", ""))
    GEMINI_MODEL: str = os.getenv("GEMINI_MODEL", "gemini-1.5-flash")

    # Redis Cache Configuration
    REDIS_HOST: str = os.getenv("REDIS_HOST", "localhost")
    REDIS_PORT: int = int(os.getenv("REDIS_PORT", "6379"))
    REDIS_TTL_SECONDS: int = int(os.getenv("REDIS_TTL_SECONDS", "600"))  # 10 minutes cache

settings = Settings()
