import urllib.request
import urllib.parse
import json
import yfinance as yf
from app.services.base import BaseMarketProvider
from app.exceptions import MarketDataException
import logging

logger = logging.getLogger("stockpilot-market-service.services.yahoo")

class YahooMarketProvider(BaseMarketProvider):

    def search(self, query: str) -> list:
        if not query.strip():
            return []
        try:
            logger.info(f"Performing stock search for query: {query}")
            url = f"https://query1.finance.yahoo.com/v1/finance/search?q={urllib.parse.quote(query)}&quotesCount=10&newsCount=0"
            req = urllib.request.Request(
                url,
                headers={
                    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36"
                }
            )
            with urllib.request.urlopen(req) as response:
                data = json.loads(response.read().decode())
                results = []
                for q in data.get("quotes", []):
                    if q.get("quoteType") in ["EQUITY", "ETF"]:
                        results.append({
                            "symbol": q.get("symbol"),
                            "name": q.get("shortname") or q.get("longname") or q.get("symbol"),
                            "exchange": q.get("exchange"),
                            "type": q.get("quoteType")
                        })
                return results
        except Exception as e:
            logger.error(f"Search failed: {str(e)}", exc_info=True)
            raise MarketDataException(f"Failed to query stock list search: {str(e)}", status_code=500)

    def get_quote(self, symbol: str) -> dict:
        if not symbol.strip():
            raise MarketDataException("Symbol parameter is required", status_code=400)
        try:
            logger.info(f"Fetching stock quote for symbol: {symbol}")
            ticker = yf.Ticker(symbol)
            info = {}
            try:
                info = ticker.info or {}
            except Exception as e:
                logger.warning(f"Failed to pull direct info dictionary for {symbol}: {str(e)}")

            price = info.get("currentPrice") or info.get("regularMarketPrice")
            prev_close = info.get("regularMarketPreviousClose") or info.get("previousClose")

            # Fallback to history for pricing extraction
            if price is None:
                logger.info(f"Falling back to history for pricing of {symbol}")
                hist = ticker.history(period="5d")
                if not hist.empty:
                    price = float(hist['Close'].iloc[-1])
                    if len(hist) > 1:
                        prev_close = float(hist['Close'].iloc[-2])
                    else:
                        prev_close = price
                else:
                    raise ValueError(f"Could not retrieve ticker pricing for symbol: {symbol}")

            change = price - prev_close if prev_close else 0.0
            change_percent = (change / prev_close * 100) if prev_close else 0.0

            # Fetch headlines
            news_list = []
            try:
                raw_news = ticker.news or []
                for item in raw_news[:5]:
                    news_list.append({
                        "title": item.get("title"),
                        "publisher": item.get("publisher"),
                        "link": item.get("link"),
                        "publishTime": item.get("providerPublishTime")
                    })
            except Exception as e:
                logger.warning(f"Could not retrieve news headlines for {symbol}: {str(e)}")

            return {
                "symbol": symbol,
                "name": info.get("shortName") or info.get("longName") or symbol,
                "price": price,
                "change": change,
                "changePercent": change_percent,
                "pe": info.get("trailingPE") or info.get("forwardPE") or None,
                "eps": info.get("trailingEps") or None,
                "marketCap": info.get("marketCap") or None,
                "volume": info.get("regularMarketVolume") or info.get("volume") or 0,
                "high": info.get("dayHigh") or price,
                "low": info.get("dayLow") or price,
                "dividendYield": info.get("dividendYield") or None,
                "exchange": info.get("exchange") or "UNKNOWN",
                "currency": info.get("currency") or "USD",
                "news": news_list
            }
        except Exception as e:
            logger.error(f"Quote fetch failed: {str(e)}", exc_info=True)
            raise MarketDataException(str(e), status_code=500)

    def get_history(self, symbol: str, range_val: str) -> list:
        mapping = {
            "1d": ("1d", "5m"),
            "5d": ("5d", "15m"),
            "1mo": ("1mo", "1d"),
            "6mo": ("6mo", "1d"),
            "1y": ("1y", "1d")
        }

        period, interval = mapping.get(range_val, ("1mo", "1d"))

        try:
            logger.info(f"Fetching stock history for symbol: {symbol}, range: {range_val}")
            ticker = yf.Ticker(symbol)
            hist = ticker.history(period=period, interval=interval)

            if hist.empty:
                raise MarketDataException(f"No historical chart data found for {symbol}", status_code=404)

            candles = []
            for index, row in hist.iterrows():
                candles.append({
                    "date": index.strftime('%Y-%m-%d %H:%M:%S') if range_val in ["1d", "5d"] else index.strftime('%Y-%m-%d'),
                    "open": float(row["Open"]),
                    "high": float(row["High"]),
                    "low": float(row["Low"]),
                    "close": float(row["Close"]),
                    "volume": int(row["Volume"])
                })
            return candles
        except MarketDataException:
            raise
        except Exception as e:
            logger.error(f"History fetch failed: {str(e)}", exc_info=True)
            raise MarketDataException(str(e), status_code=500)
