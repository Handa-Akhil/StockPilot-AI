import os
import json
import logging
from typing import Optional, Dict, Any
import google.generativeai as genai
from app.config import settings

logger = logging.getLogger("stockpilot-market-service.services.gemini")

# Optional Redis connection setup
redis_client = None
try:
    import redis
    redis_client = redis.Redis(
        host=settings.REDIS_HOST,
        port=settings.REDIS_PORT,
        decode_responses=True,
        socket_connect_timeout=2
    )
    # Test ping
    redis_client.ping()
    logger.info("Successfully connected to Redis cache for Gemini insights")
except Exception as e:
    logger.warning(f"Redis cache connection unavailable for Gemini service: {e}. In-memory fallback will be used.")
    redis_client = None

# In-memory dictionary cache fallback if Redis container is unreachable
_memory_cache: Dict[str, Any] = {}


class GeminiInsightService:

    def __init__(self):
        self.api_key = settings.GEMINI_API_KEY.strip()
        self.model_name = settings.GEMINI_MODEL
        self.is_configured = bool(
            self.api_key and self.api_key != "YOUR_GEMINI_API_KEY_HERE"
        )
        if self.is_configured:
            try:
                genai.configure(api_key=self.api_key)
                self.model = genai.GenerativeModel(self.model_name)
                logger.info(f"Initialized Google Gemini service with model: {self.model_name}")
            except Exception as e:
                logger.error(f"Failed to configure Google Gemini SDK: {e}")
                self.is_configured = False
        else:
            logger.info("GEMINI_API_KEY is not set or placeholder. Gemini enhancement will operate in rule-based fallback mode.")

    def _get_cached(self, key: str) -> Optional[dict]:
        """Reads cached JSON result from Redis or memory."""
        try:
            if redis_client:
                data = redis_client.get(key)
                if data:
                    logger.info(f"[Redis Cache HIT] Key: {key}")
                    return json.loads(data)
            elif key in _memory_cache:
                logger.info(f"[Memory Cache HIT] Key: {key}")
                return _memory_cache[key]
        except Exception as e:
            logger.warning(f"Failed to read cache for key {key}: {e}")
        return None

    def _set_cached(self, key: str, value: dict, ttl: int = 600):
        """Writes JSON result to Redis or memory cache with TTL (default 10 minutes)."""
        try:
            val_str = json.dumps(value)
            if redis_client:
                redis_client.setex(key, ttl, val_str)
                logger.info(f"[Redis Cache SET] Key: {key} (TTL: {ttl}s)")
            else:
                _memory_cache[key] = value
        except Exception as e:
            logger.warning(f"Failed to set cache for key {key}: {e}")

    def enhance_portfolio_analysis(self, portfolio_id: int, holdings: list, rule_based_data: dict) -> Optional[dict]:
        """
        Enhances portfolio insights with Gemini natural language generation.
        Returns None on any error to trigger rule-based fallback cleanly.
        """
        cache_key = f"gemini:portfolio:{portfolio_id}"
        cached = self._get_cached(cache_key)
        if cached:
            return cached

        if not self.is_configured:
            return None

        try:
            logger.info(f"Calling Google Gemini API for portfolio #{portfolio_id} analysis...")
            prompt = f"""
You are StockPilot AI, an expert quantitative financial advisor.
Analyze the following portfolio holdings data and rule-based diagnostic metrics:

Portfolio ID: {portfolio_id}
Holdings Count: {len(holdings)}
Holdings Summary: {json.dumps(holdings)}
Rule-Based Metrics: {json.dumps(rule_based_data)}

Generate a detailed JSON analysis with EXACTLY the following keys:
1. "portfolioSummary": A professional 2-3 sentence overall narrative summary of the portfolio.
2. "riskAnalysis": A deep risk evaluation detailing concentration, market beta, and volatility exposure.
3. "diversificationAdvice": Strategic allocation advice to optimize risk-adjusted returns.
4. "longTermOutlook": Multi-year market growth and compounding outlook narrative (2 sentences).
5. "shortTermOutlook": Near-term 3-6 month macro & earnings outlook narrative (2 sentences).
6. "investmentCommentary": Executive advisor commentary with actionable steps.

Return ONLY a valid JSON object matching these 6 keys without markdown wrapper formatting.
"""

            response = self.model.generate_content(prompt)
            raw_text = response.text.strip()
            # Clean markdown codeblocks if returned
            if raw_text.startswith("```"):
                raw_text = raw_text.split("```")[1]
                if raw_text.startswith("json"):
                    raw_text = raw_text[4:]
                raw_text = raw_text.strip()

            result_json = json.loads(raw_text)
            self._set_cached(cache_key, result_json, ttl=settings.REDIS_TTL_SECONDS)
            return result_json

        except Exception as e:
            logger.error(f"[Gemini Error] Portfolio #{portfolio_id} analysis generation failed: {e}. Falling back to rule-based metrics.")
            return None

    def enhance_stock_analysis(self, symbol: str, quote: dict, rule_based_data: dict) -> Optional[dict]:
        """
        Enhances single-ticker analysis with Gemini financial insights.
        Returns None on any error to trigger rule-based fallback cleanly.
        """
        cache_key = f"gemini:stock:{symbol.upper()}"
        cached = self._get_cached(cache_key)
        if cached:
            return cached

        if not self.is_configured:
            return None

        try:
            logger.info(f"Calling Google Gemini API for ticker '{symbol.upper()}' analysis...")
            prompt = f"""
You are StockPilot AI, a senior equity research analyst.
Analyze the following stock market data for ticker {symbol.upper()}:

Quote Data: {json.dumps(quote)}
Diagnostic Rule Output: {json.dumps(rule_based_data)}

Generate a professional JSON research report with EXACTLY the following keys:
1. "businessSummary": Concise 2-sentence description of the company's core revenue driver and competitive moat.
2. "bullishFactors": Array of 3 specific positive catalysts or bullish growth drivers.
3. "bearishFactors": Array of 3 specific negative risks or bearish headwinds.
4. "technicalInterpretation": 2-sentence technical chart interpretation based on price and momentum.
5. "fundamentalInterpretation": 2-sentence balance sheet & valuation multiple interpretation (P/E, Market Cap).
6. "confidenceExplanation": 1-sentence rationale explaining the confidence rating.

Return ONLY a valid JSON object matching these 6 keys without markdown wrapper formatting.
"""

            response = self.model.generate_content(prompt)
            raw_text = response.text.strip()
            if raw_text.startswith("```"):
                raw_text = raw_text.split("```")[1]
                if raw_text.startswith("json"):
                    raw_text = raw_text[4:]
                raw_text = raw_text.strip()

            result_json = json.loads(raw_text)
            self._set_cached(cache_key, result_json, ttl=settings.REDIS_TTL_SECONDS)
            return result_json

        except Exception as e:
            logger.error(f"[Gemini Error] Ticker '{symbol}' analysis generation failed: {e}. Falling back to rule-based metrics.")
            return None

    def enhance_market_sentiment(self, spy_quote: dict, rule_based_data: dict) -> Optional[dict]:
        """
        Enhances macro market sentiment with Gemini macro intelligence.
        Returns None on any error to trigger rule-based fallback cleanly.
        """
        cache_key = "gemini:sentiment:market"
        cached = self._get_cached(cache_key)
        if cached:
            return cached

        if not self.is_configured:
            return None

        try:
            logger.info("Calling Google Gemini API for macro market sentiment...")
            prompt = f"""
You are StockPilot AI, a macro strategist.
Analyze the current S&P 500 benchmark quote and market sentiment rules:

SPY Benchmark Data: {json.dumps(spy_quote)}
Rule-Based Sentiment: {json.dumps(rule_based_data)}

Generate a high-level macro market commentary JSON object with EXACTLY the following keys:
1. "dailyMarketSummary": 2-sentence executive summary of today's market action and index movement.
2. "sectorRotation": 2-sentence assessment of leading vs lagging market sectors.
3. "macroeconomicCommentary": 2-sentence summary of interest rates, inflation, and central bank stance.
4. "riskOutlook": 2-sentence assessment of near-term market volatility and tail risks.
5. "investmentOpportunities": Array of 3 promising market sectors or asset classes for capital deployment.

Return ONLY a valid JSON object matching these 5 keys without markdown wrapper formatting.
"""

            response = self.model.generate_content(prompt)
            raw_text = response.text.strip()
            if raw_text.startswith("```"):
                raw_text = raw_text.split("```")[1]
                if raw_text.startswith("json"):
                    raw_text = raw_text[4:]
                raw_text = raw_text.strip()

            result_json = json.loads(raw_text)
            self._set_cached(cache_key, result_json, ttl=settings.REDIS_TTL_SECONDS)
            return result_json

        except Exception as e:
            logger.error(f"[Gemini Error] Macro market sentiment generation failed: {e}. Falling back to rule-based metrics.")
            return None
