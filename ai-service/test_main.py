import json
from unittest.mock import patch, MagicMock
from fastapi.testclient import TestClient
import pandas as pd
from main import app

client = TestClient(app)

def test_read_root():
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"message": "StockPilot Market Data Service is running"}

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "UP"}

@patch("urllib.request.urlopen")
def test_search_stocks(mock_urlopen):
    # Mock search API response
    mock_response = MagicMock()
    mock_json = {
        "quotes": [
            {"symbol": "AAPL", "shortname": "Apple Inc.", "exchange": "NMS", "quoteType": "EQUITY"},
            {"symbol": "TCS.NS", "shortname": "Tata Consultancy Services Limited", "exchange": "NSE", "quoteType": "EQUITY"}
        ]
    }
    mock_response.read.return_value = json.dumps(mock_json).encode("utf-8")
    mock_urlopen.return_value.__enter__.return_value = mock_response

    response = client.get("/ai/market/search?query=Apple")
    assert response.status_code == 200
    results = response.json()
    assert len(results) == 2
    assert results[0]["symbol"] == "AAPL"
    assert results[0]["exchange"] == "NMS"
    assert results[1]["symbol"] == "TCS.NS"
    assert results[1]["exchange"] == "NSE"

@patch("yfinance.Ticker")
def test_get_quote(mock_ticker):
    # Mock yfinance Info dictionary
    mock_instance = MagicMock()
    mock_instance.info = {
        "shortName": "Apple Inc.",
        "currentPrice": 180.50,
        "regularMarketPreviousClose": 178.00,
        "trailingPE": 28.5,
        "trailingEps": 6.30,
        "marketCap": 2800000000000,
        "regularMarketVolume": 50000000,
        "dayHigh": 182.00,
        "dayLow": 179.50,
        "dividendYield": 0.005,
        "exchange": "NMS",
        "currency": "USD"
    }
    # Mock ticker news
    mock_instance.news = [
        {"title": "Apple news 1", "publisher": "Yahoo Finance", "link": "https://example.com", "providerPublishTime": 12345}
    ]
    mock_ticker.return_value = mock_instance

    response = client.get("/ai/market/quote/AAPL")
    assert response.status_code == 200
    data = response.json()
    assert data["symbol"] == "AAPL"
    assert data["name"] == "Apple Inc."
    assert data["price"] == 180.50
    assert data["change"] == 2.50
    assert data["changePercent"] == (2.50 / 178.00 * 100)
    assert data["exchange"] == "NMS"
    assert len(data["news"]) == 1
    assert data["news"][0]["title"] == "Apple news 1"

@patch("yfinance.Ticker")
def test_get_history(mock_ticker):
    # Mock history dataframe
    mock_instance = MagicMock()
    
    # Create mock Pandas DataFrame with DatetimeIndex
    dates = pd.date_range(start="2026-07-01", periods=3, freq="D")
    df_data = {
        "Open": [100.0, 101.0, 102.0],
        "High": [105.0, 106.0, 107.0],
        "Low": [99.0, 100.0, 101.0],
        "Close": [102.0, 103.0, 104.0],
        "Volume": [1000, 1100, 1200]
    }
    mock_df = pd.DataFrame(df_data, index=dates)
    mock_instance.history.return_value = mock_df
    mock_ticker.return_value = mock_instance

    response = client.get("/ai/market/history/AAPL?range=1mo")
    assert response.status_code == 200
    candles = response.json()
    assert len(candles) == 3
    assert candles[0]["date"] == "2026-07-01"
    assert candles[0]["open"] == 100.0
    assert candles[0]["close"] == 102.0
    assert candles[0]["volume"] == 1000
